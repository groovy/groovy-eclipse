/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *     							bug 319201 - [null] no warning when unboxing SingleNameReference causes NPE
 *     							bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
 *								bug 370639 - [compiler][resource] restore the default for resource leak warnings
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *								bug 388996 - [compiler][resource] Incorrect 'potential resource leak'
 *								bug 395977 - [compiler][resource] Resource leak warning behavior possibly incorrect for anonymous inner class
 *								bug 403147 - [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
 *								Bug 415850 - [1.8] Ensure RunJDTCoreTests can cope with null annotations enabled
 *								Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *								Bug 417295 - [1.8[[null] Massage type annotated null analysis to gel well with deep encoded type bindings.
 *								Bug 416267 - NPE in QualifiedAllocationExpression.resolveType
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 424415 - [1.8][compiler] Eventual resolution of ReferenceExpression is not seen to be happening.
 *								Bug 427438 - [1.8][compiler] NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode(ConditionalExpression.java:280)
 *     Jesper S Moller <jesper@selskabet.org> - Contributions for
 *								bug 378674 - "The method can be declared as static" is wrong
 *     Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 383624 - [1.8][compiler] Revive code generation support for type annotations (from Olivier's work)
 *                          Bug 409245 - [1.8][compiler] Type annotations dropped when call is routed through a synthetic bridge method
 *     Till Brychcy - Contributions for
 *     							bug 413460 - NonNullByDefault is not inherited to Constructors when accessed via Class File
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.INVOCATION_CONTEXT;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.ImplicitNullAnnotationVerifier;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * Variation on allocation, where can optionally be specified any of:
 * - leading enclosing instance
 * - trailing anonymous type
 * - generic type arguments for generic constructor invocation
 */
public class QualifiedAllocationExpression extends AllocationExpression {

	//qualification may be on both side
	public Expression enclosingInstance;
	public TypeDeclaration anonymousType;

	public QualifiedAllocationExpression() {
		// for subtypes
	}

	public QualifiedAllocationExpression(TypeDeclaration anonymousType) {
		this.anonymousType = anonymousType;
		anonymousType.allocation = this;
	}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		// analyse the enclosing instance
		if (this.enclosingInstance != null) {
			flowInfo = this.enclosingInstance.analyseCode(currentScope, flowContext, flowInfo);
		} else {
			if (this.binding != null && this.binding.declaringClass != null) {
				ReferenceBinding superclass = this.binding.declaringClass.superclass();
				if (superclass != null && superclass.isMemberType() && !superclass.isStatic()) {
					// creating an anonymous type of a non-static member type without an enclosing instance of parent type
					currentScope.tagAsAccessingEnclosingInstanceStateOf(superclass.enclosingType(), false /* type variable access */);
					// Reviewed for https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674 :
					// The corresponding problem (when called from static) is not produced until during code generation
				}
			}
			
		}

		// check captured variables are initialized in current context (26134)
		checkCapturedLocalInitializationIfNecessary(
			(ReferenceBinding)(this.anonymousType == null
				? this.binding.declaringClass.erasure()
				: this.binding.declaringClass.superclass().erasure()),
			currentScope,
			flowInfo);

		// process arguments
		if (this.arguments != null) {
			boolean analyseResources = currentScope.compilerOptions().analyseResourceLeaks;
			boolean hasResourceWrapperType = analyseResources 
						&& this.resolvedType instanceof ReferenceBinding 
						&& ((ReferenceBinding)this.resolvedType).hasTypeBit(TypeIds.BitWrapperCloseable);
			for (int i = 0, count = this.arguments.length; i < count; i++) {
				flowInfo = this.arguments[i].analyseCode(currentScope, flowContext, flowInfo);
				if (analyseResources && !hasResourceWrapperType) { // allocation of wrapped closeables is analyzed specially
					// if argument is an AutoCloseable insert info that it *may* be closed (by the target method, i.e.)
					flowInfo = FakedTrackingVariable.markPassedToOutside(currentScope, this.arguments[i], flowInfo, flowContext, false);
				}
				this.arguments[i].checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
			}
			analyseArguments(currentScope, flowContext, flowInfo, this.binding, this.arguments);
		}

		// analyse the anonymous nested type
		if (this.anonymousType != null) {
			flowInfo = this.anonymousType.analyseCode(currentScope, flowContext, flowInfo);
		}

		// record some dependency information for exception types
		ReferenceBinding[] thrownExceptions;
		if (((thrownExceptions = this.binding.thrownExceptions).length) != 0) {
			if ((this.bits & ASTNode.Unchecked) != 0 && this.genericTypeArguments == null) {
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=277643, align with javac on JLS 15.12.2.6
				thrownExceptions = currentScope.environment().convertToRawTypes(this.binding.thrownExceptions, true, true);
			}			
			// check exception handling
			flowContext.checkExceptionHandlers(
				thrownExceptions,
				this,
				flowInfo.unconditionalCopy(),
				currentScope);
		}

		// after having analysed exceptions above start tracking newly allocated resource:
		if (currentScope.compilerOptions().analyseResourceLeaks && FakedTrackingVariable.isAnyCloseable(this.resolvedType)) {
			FakedTrackingVariable.analyseCloseableAllocation(currentScope, flowInfo, this);
		}

		manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
		manageSyntheticAccessIfNecessary(currentScope, flowInfo);

		// account for possible exceptions thrown by constructor execution:
		flowContext.recordAbruptExit();

		return flowInfo;
	}

	public Expression enclosingInstance() {

		return this.enclosingInstance;
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		if (!valueRequired)
			currentScope.problemReporter().unusedObjectAllocation(this);
		int pc = codeStream.position;
		MethodBinding codegenBinding = this.binding.original();
		ReferenceBinding allocatedType = codegenBinding.declaringClass;
		codeStream.new_(this.type, allocatedType);
		boolean isUnboxing = (this.implicitConversion & TypeIds.UNBOXING) != 0;
		if (valueRequired || isUnboxing) {
			codeStream.dup();
		}
		// better highlight for allocation: display the type individually
		if (this.type != null) { // null for enum constant body
			codeStream.recordPositionsFrom(pc, this.type.sourceStart);
		} else {
			// push enum constant name and ordinal
			codeStream.ldc(String.valueOf(this.enumConstant.name));
			codeStream.generateInlinedValue(this.enumConstant.binding.id);
		}
		// handling innerclass instance allocation - enclosing instance arguments
		if (allocatedType.isNestedType()) {
			codeStream.generateSyntheticEnclosingInstanceValues(
				currentScope,
				allocatedType,
				enclosingInstance(),
				this);
		}
		// generate the arguments for constructor
		generateArguments(this.binding, this.arguments, currentScope, codeStream);
		// handling innerclass instance allocation - outer local arguments
		if (allocatedType.isNestedType()) {
			codeStream.generateSyntheticOuterArgumentValues(
				currentScope,
				allocatedType,
				this);
		}

		// invoke constructor
		if (this.syntheticAccessor == null) {
			codeStream.invoke(Opcodes.OPC_invokespecial, codegenBinding, null /* default declaringClass */, this.typeArguments);
		} else {
			// synthetic accessor got some extra arguments appended to its signature, which need values
			for (int i = 0,
				max = this.syntheticAccessor.parameters.length - codegenBinding.parameters.length;
				i < max;
				i++) {
				codeStream.aconst_null();
			}
			codeStream.invoke(Opcodes.OPC_invokespecial, this.syntheticAccessor, null /* default declaringClass */, this.typeArguments);
		}
		if (valueRequired) {
			codeStream.generateImplicitConversion(this.implicitConversion);
		} else if (isUnboxing) {
			// conversion only generated if unboxing
			codeStream.generateImplicitConversion(this.implicitConversion);
			switch (postConversionType(currentScope).id) {
				case T_long :
				case T_double :
					codeStream.pop2();
					break;
				default :
					codeStream.pop();
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);

		if (this.anonymousType != null) {
			this.anonymousType.generateCode(currentScope, codeStream);
		}
	}

	public boolean isSuperAccess() {

		// necessary to lookup super constructor of anonymous type
		return this.anonymousType != null;
	}

	/* Inner emulation consists in either recording a dependency
	 * link only, or performing one level of propagation.
	 *
	 * Dependency mechanism is used whenever dealing with source target
	 * types, since by the time we reach them, we might not yet know their
	 * exact need.
	 */
	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) == 0)	{
		ReferenceBinding allocatedTypeErasure = (ReferenceBinding) this.binding.declaringClass.erasure();

		// perform some extra emulation work in case there is some and we are inside a local type only
		if (allocatedTypeErasure.isNestedType()
			&& (currentScope.enclosingSourceType().isLocalType() || currentScope.isLambdaSubscope())) {

			if (allocatedTypeErasure.isLocalType()) {
				((LocalTypeBinding) allocatedTypeErasure).addInnerEmulationDependent(currentScope, this.enclosingInstance != null);
			} else {
				// locally propagate, since we already now the desired shape for sure
				currentScope.propagateInnerEmulation(allocatedTypeErasure, this.enclosingInstance != null);
			}
		}
		}
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {
		if (this.enclosingInstance != null)
			this.enclosingInstance.printExpression(0, output).append('.');
		super.printExpression(0, output);
		if (this.anonymousType != null) {
			this.anonymousType.print(indent, output);
		}
		return output;
	}

	public TypeBinding resolveType(BlockScope scope) {
		// added for code assist...cannot occur with 'normal' code
		if (this.anonymousType == null && this.enclosingInstance == null) {
			return super.resolveType(scope);
		}
		TypeBinding result = resolveTypeForQualifiedAllocationExpression(scope);
		if (result != null && !result.isPolyType() && this.binding != null) {
			final CompilerOptions compilerOptions = scope.compilerOptions();
			if (compilerOptions.isAnnotationBasedNullAnalysisEnabled) {
				if ((this.binding.tagBits & TagBits.IsNullnessKnown) == 0) {
					new ImplicitNullAnnotationVerifier(scope.environment(), compilerOptions.inheritNullAnnotations)
							.checkImplicitNullAnnotations(this.binding, null/*srcMethod*/, false, scope);
				}
				if (compilerOptions.sourceLevel >= ClassFileConstants.JDK1_8) {
					if (this.binding instanceof ParameterizedGenericMethodBinding && this.typeArguments != null) {
						TypeVariableBinding[] typeVariables = this.binding.original().typeVariables();
						for (int i = 0; i < this.typeArguments.length; i++)
							this.typeArguments[i].checkNullConstraints(scope, typeVariables, i);
					}
				}
			}
		}
		return result;
	}
	
	private TypeBinding resolveTypeForQualifiedAllocationExpression(BlockScope scope) {
		// Propagate the type checking to the arguments, and checks if the constructor is defined.
		// ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
		// ClassInstanceCreationExpression ::= Name '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
		final boolean isDiamond = this.type != null && (this.type.bits & ASTNode.IsDiamond) != 0;
		TypeBinding enclosingInstanceType = null;
		TypeBinding receiverType = null;
		long sourceLevel = scope.compilerOptions().sourceLevel;
		if (this.constant != Constant.NotAConstant) {
			this.constant = Constant.NotAConstant;
			ReferenceBinding enclosingInstanceReference = null;
			boolean hasError = false;
			boolean enclosingInstanceContainsCast = false;

			if (this.enclosingInstance != null) {
				if (this.enclosingInstance instanceof CastExpression) {
					this.enclosingInstance.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
					enclosingInstanceContainsCast = true;
				}
				if ((enclosingInstanceType = this.enclosingInstance.resolveType(scope)) == null){
					hasError = true;
				} else if (enclosingInstanceType.isBaseType() || enclosingInstanceType.isArrayType()) {
					scope.problemReporter().illegalPrimitiveOrArrayTypeForEnclosingInstance(
							enclosingInstanceType,
							this.enclosingInstance);
					hasError = true;
				} else if (this.type instanceof QualifiedTypeReference) {
					scope.problemReporter().illegalUsageOfQualifiedTypeReference((QualifiedTypeReference)this.type);
					hasError = true;
				} else if (!(enclosingInstanceReference = (ReferenceBinding) enclosingInstanceType).canBeSeenBy(scope)) {
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317212
					enclosingInstanceType = new ProblemReferenceBinding(
							enclosingInstanceReference.compoundName,
							enclosingInstanceReference,
							ProblemReasons.NotVisible);
					scope.problemReporter().invalidType(this.enclosingInstance, enclosingInstanceType);
					hasError = true;
				} else {
					this.resolvedType = receiverType = ((SingleTypeReference) this.type).resolveTypeEnclosing(scope, (ReferenceBinding) enclosingInstanceType);
					checkIllegalNullAnnotation(scope, receiverType);
					if (receiverType != null && enclosingInstanceContainsCast) {
						CastExpression.checkNeedForEnclosingInstanceCast(scope, this.enclosingInstance, enclosingInstanceType, receiverType);
					}
				}
			} else {
				if (this.type == null) {
					// initialization of an enum constant
					receiverType = scope.enclosingSourceType();
				} else {
					receiverType = this.type.resolveType(scope, true /* check bounds*/);
					checkIllegalNullAnnotation(scope, receiverType);
					checkParameterizedAllocation: {
						if (receiverType == null || !receiverType.isValidBinding()) break checkParameterizedAllocation;
						if (this.type instanceof ParameterizedQualifiedTypeReference) { // disallow new X<String>.Y<Integer>()
							ReferenceBinding currentType = (ReferenceBinding)receiverType;
							do {
								// isStatic() is answering true for toplevel types
								if ((currentType.modifiers & ClassFileConstants.AccStatic) != 0) break checkParameterizedAllocation;
								if (currentType.isRawType()) break checkParameterizedAllocation;
							} while ((currentType = currentType.enclosingType())!= null);
							ParameterizedQualifiedTypeReference qRef = (ParameterizedQualifiedTypeReference) this.type;
							for (int i = qRef.typeArguments.length - 2; i >= 0; i--) {
								if (qRef.typeArguments[i] != null) {
									scope.problemReporter().illegalQualifiedParameterizedTypeAllocation(this.type, receiverType);
									break;
								}
							}
						}
					}
				}
			}
			if (receiverType == null || !receiverType.isValidBinding()) {
				hasError = true;
			}

			// resolve type arguments (for generic constructor call)
			if (this.typeArguments != null) {
				int length = this.typeArguments.length;
				this.argumentsHaveErrors = sourceLevel < ClassFileConstants.JDK1_5;
				this.genericTypeArguments = new TypeBinding[length];
				for (int i = 0; i < length; i++) {
					TypeReference typeReference = this.typeArguments[i];
					if ((this.genericTypeArguments[i] = typeReference.resolveType(scope, true /* check bounds*/)) == null) {
						this.argumentsHaveErrors = true;
					}
					if (this.argumentsHaveErrors && typeReference instanceof Wildcard) {
						scope.problemReporter().illegalUsageOfWildcard(typeReference);
					}
				}
				if (isDiamond) {
					scope.problemReporter().diamondNotWithExplicitTypeArguments(this.typeArguments);
					return null;
				}
				if (this.argumentsHaveErrors) {
					if (this.arguments != null) { // still attempt to resolve arguments
						for (int i = 0, max = this.arguments.length; i < max; i++) {
							this.arguments[i].resolveType(scope);
						}
					}
					return null;
				}
			}

			// will check for null after args are resolved
			this.argumentTypes = Binding.NO_PARAMETERS;
			if (this.arguments != null) {
				int length = this.arguments.length;
				this.argumentTypes = new TypeBinding[length];
				for (int i = 0; i < length; i++) {
					Expression argument = this.arguments[i];
					if (argument instanceof CastExpression) {
						argument.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
						this.argsContainCast = true;
					}
					argument.setExpressionContext(INVOCATION_CONTEXT);
					if ((this.argumentTypes[i] = argument.resolveType(scope)) == null){
						this.argumentsHaveErrors = hasError = true;
					}
				}
			}

			// limit of fault-tolerance
			if (hasError) {
				/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=345359, if arguments have errors, completely bail out in the <> case.
			       No meaningful type resolution is possible since inference of the elided types is fully tied to argument types. Do
			       not return the partially resolved type.
				 */
				if (isDiamond) {
					return null; // not the partially cooked this.resolvedType
				}
				if (receiverType instanceof ReferenceBinding) {
					ReferenceBinding referenceReceiver = (ReferenceBinding) receiverType;
					if (receiverType.isValidBinding()) {
						// record a best guess, for clients who need hint about possible contructor match
						int length = this.arguments  == null ? 0 : this.arguments.length;
						TypeBinding[] pseudoArgs = new TypeBinding[length];
						for (int i = length; --i >= 0;) {
							pseudoArgs[i] = this.argumentTypes[i] == null ? TypeBinding.NULL : this.argumentTypes[i]; // replace args with errors with null type
						}
						this.binding = scope.findMethod(referenceReceiver, TypeConstants.INIT, pseudoArgs, this, false);
						if (this.binding != null && !this.binding.isValidBinding()) {
							MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
							// record the closest match, for clients who may still need hint about possible method match
							if (closestMatch != null) {
								if (closestMatch.original().typeVariables != Binding.NO_TYPE_VARIABLES) { // generic method
									// shouldn't return generic method outside its context, rather convert it to raw method (175409)
									closestMatch = scope.environment().createParameterizedGenericMethod(closestMatch.original(), (RawTypeBinding)null);
								}
								this.binding = closestMatch;
								MethodBinding closestMatchOriginal = closestMatch.original();
								if (closestMatchOriginal.isOrEnclosedByPrivateType() && !scope.isDefinedInMethod(closestMatchOriginal)) {
									// ignore cases where method is used from within inside itself (e.g. direct recursions)
									closestMatchOriginal.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
								}
							}
						}
					}
					if (this.anonymousType != null) {
						// insert anonymous type in scope (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=210070)
						scope.addAnonymousType(this.anonymousType, referenceReceiver);
						this.anonymousType.resolve(scope);
						return this.resolvedType = this.anonymousType.binding;
					}
				}
				return this.resolvedType = receiverType;
			}
			if (this.anonymousType == null) {
				// qualified allocation with no anonymous type
				if (!receiverType.canBeInstantiated()) {
					scope.problemReporter().cannotInstantiate(this.type, receiverType);
					return this.resolvedType = receiverType;
				}
			} else {
				if (isDiamond) {
					scope.problemReporter().diamondNotWithAnoymousClasses(this.type);
					return null;
				}	
				ReferenceBinding superType = (ReferenceBinding) receiverType;
				if (superType.isTypeVariable()) {
					superType = new ProblemReferenceBinding(new char[][]{superType.sourceName()}, superType, ProblemReasons.IllegalSuperTypeVariable);
					scope.problemReporter().invalidType(this, superType);
					return null;
				} else if (this.type != null && superType.isEnum()) { // tolerate enum constant body
					scope.problemReporter().cannotInstantiate(this.type, superType);
					return this.resolvedType = superType;
				}
				// anonymous type scenario
				// an anonymous class inherits from java.lang.Object when declared "after" an interface
				ReferenceBinding anonymousSuperclass = superType.isInterface() ? scope.getJavaLangObject() : superType;
				// insert anonymous type in scope
				scope.addAnonymousType(this.anonymousType, superType);
				this.anonymousType.resolve(scope);

				// find anonymous super constructor
				this.resolvedType = this.anonymousType.binding; // 1.2 change
				if ((this.resolvedType.tagBits & TagBits.HierarchyHasProblems) != 0) {
					return null; // stop secondary errors
				}
				MethodBinding inheritedBinding = findConstructorBinding(scope, this, anonymousSuperclass, this.argumentTypes);

				if (!inheritedBinding.isValidBinding()) {
					if (inheritedBinding.declaringClass == null) {
						inheritedBinding.declaringClass = anonymousSuperclass;
					}
					if (this.type != null && !this.type.resolvedType.isValidBinding()) {
						// problem already got signaled on type reference, do not report secondary problem
						return null;
					}
					scope.problemReporter().invalidConstructor(this, inheritedBinding);
					return this.resolvedType;
				}
				if ((inheritedBinding.tagBits & TagBits.HasMissingType) != 0) {
					scope.problemReporter().missingTypeInConstructor(this, inheritedBinding);
				}
				if (this.enclosingInstance != null) {
					ReferenceBinding targetEnclosing = inheritedBinding.declaringClass.enclosingType();
					if (targetEnclosing == null) {
						scope.problemReporter().unnecessaryEnclosingInstanceSpecification(this.enclosingInstance, superType);
						return this.resolvedType;
					} else if (!enclosingInstanceType.isCompatibleWith(targetEnclosing) && !scope.isBoxingCompatibleWith(enclosingInstanceType, targetEnclosing)) {
						scope.problemReporter().typeMismatchError(enclosingInstanceType, targetEnclosing, this.enclosingInstance, null);
						return this.resolvedType;
					}
					this.enclosingInstance.computeConversion(scope, targetEnclosing, enclosingInstanceType);
				}
				if (this.arguments != null) {
					if (checkInvocationArguments(scope, null, anonymousSuperclass, inheritedBinding, this.arguments, this.argumentTypes, this.argsContainCast, this)) {
						this.bits |= ASTNode.Unchecked;
					}
				}
				if (this.typeArguments != null && inheritedBinding.original().typeVariables == Binding.NO_TYPE_VARIABLES) {
					scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(inheritedBinding, this.genericTypeArguments, this.typeArguments);
				}
				// Update the anonymous inner class : superclass, interface
				this.binding = this.anonymousType.createDefaultConstructorWithBinding(inheritedBinding, 	(this.bits & ASTNode.Unchecked) != 0 && this.genericTypeArguments == null);
				return this.resolvedType;
			}
		} else {
			if (this.enclosingInstance != null) {
				enclosingInstanceType = this.enclosingInstance.resolvedType;
				this.resolvedType = receiverType = this.type.resolvedType;
			}
		}
		if (isDiamond) {
			this.binding = inferConstructorOfElidedParameterizedType(scope);
			if (this.binding == null || !this.binding.isValidBinding()) {
				scope.problemReporter().cannotInferElidedTypes(this);
				return this.resolvedType = null;
			}
			if (this.typeExpected == null && sourceLevel >= ClassFileConstants.JDK1_8 && this.expressionContext.definesTargetType()) {
				return new PolyTypeBinding(this);
			}
			this.resolvedType = this.type.resolvedType = receiverType = this.binding.declaringClass;
			resolvePolyExpressionArguments(this, this.binding, this.argumentTypes, scope);
		} else {
			this.binding = findConstructorBinding(scope, this, (ReferenceBinding) receiverType, this.argumentTypes);
		}

		if (this.binding.isValidBinding()) {	
			if (isMethodUseDeprecated(this.binding, scope, true)) {
				scope.problemReporter().deprecatedMethod(this.binding, this);
			}
			if (checkInvocationArguments(scope, null, receiverType, this.binding, this.arguments, this.argumentTypes, this.argsContainCast, this)) {
				this.bits |= ASTNode.Unchecked;
			}
			if (this.typeArguments != null && this.binding.original().typeVariables == Binding.NO_TYPE_VARIABLES) {
				scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(this.binding, this.genericTypeArguments, this.typeArguments);
			}
		} else {
			if (this.binding.declaringClass == null) {
				this.binding.declaringClass = (ReferenceBinding) receiverType;
			}
			if (this.type != null && !this.type.resolvedType.isValidBinding()) {
				// problem already got signaled on type reference, do not report secondary problem
				return null;
			}
			scope.problemReporter().invalidConstructor(this, this.binding);
			return this.resolvedType = receiverType;
		}
		if ((this.binding.tagBits & TagBits.HasMissingType) != 0) {
			scope.problemReporter().missingTypeInConstructor(this, this.binding);
		}
		if (!isDiamond && receiverType.isParameterizedTypeWithActualArguments()) {
			checkTypeArgumentRedundancy((ParameterizedTypeBinding)receiverType, scope);
		}
		// The enclosing instance must be compatible with the innermost enclosing type
		ReferenceBinding expectedType = this.binding.declaringClass.enclosingType();
		if (TypeBinding.notEquals(expectedType, enclosingInstanceType)) // must call before computeConversion() and typeMismatchError()
			scope.compilationUnitScope().recordTypeConversion(expectedType, enclosingInstanceType);
		if (enclosingInstanceType.isCompatibleWith(expectedType) || scope.isBoxingCompatibleWith(enclosingInstanceType, expectedType)) {
			this.enclosingInstance.computeConversion(scope, expectedType, enclosingInstanceType);
			return this.resolvedType = receiverType;
		}
		scope.problemReporter().typeMismatchError(enclosingInstanceType, expectedType, this.enclosingInstance, null);
		return this.resolvedType = receiverType;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.enclosingInstance != null)
				this.enclosingInstance.traverse(visitor, scope);
			if (this.typeArguments != null) {
				for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
					this.typeArguments[i].traverse(visitor, scope);
				}
			}
			if (this.type != null) // case of enum constant
				this.type.traverse(visitor, scope);
			if (this.arguments != null) {
				int argumentsLength = this.arguments.length;
				for (int i = 0; i < argumentsLength; i++)
					this.arguments[i].traverse(visitor, scope);
			}
			if (this.anonymousType != null)
				this.anonymousType.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}