/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 365519 - editorial cleanup after bug 186342 and bug 365387
 *								bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
 *								bug 382353 - [1.8][compiler] Implementation property modifiers should be accepted on default methods.
 *								bug 383368 - [compiler][null] syntactic null analysis for field references
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *								Bug 416176 - [1.8][compiler][null] null type annotations cause grief on type variables
 *								Bug 438012 - [1.8][null] Bogus Warning: The nullness annotation is redundant with a default that applies to this location
 *								Bug 435805 - [1.8][compiler][null] Java 8 compiler does not recognize declaration style null annotations
 *								Bug 466713 - Null Annotations: NullPointerException using <int @Nullable []> as Type Param
 *     Jesper S Moller <jesper@selskabet.org> - Contributions for
 *								bug 378674 - "The method can be declared as static" is wrong
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationCollector;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationPosition;

public class MethodDeclaration extends AbstractMethodDeclaration {

	public TypeReference returnType;
	public TypeParameter[] typeParameters;

	/**
	 * MethodDeclaration constructor comment.
	 */
	public MethodDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
		this.bits |= ASTNode.CanBeStatic; // Start with this assumption, will course correct during resolve and analyseCode.
	}

	public void analyseCode(ClassScope classScope, FlowContext flowContext, FlowInfo flowInfo) {
		// starting of the code analysis for methods
		if (this.ignoreFurtherInvestigation)
			return;
		try {
			if (this.binding == null)
				return;

			if (!this.binding.isUsed() && !this.binding.isAbstract()) {
				if (this.binding.isPrivate()
					|| (((this.binding.modifiers & (ExtraCompilerModifiers.AccOverriding|ExtraCompilerModifiers.AccImplementing)) == 0)
						&& this.binding.isOrEnclosedByPrivateType())) {
					if (!classScope.referenceCompilationUnit().compilationResult.hasSyntaxError) {
						this.scope.problemReporter().unusedPrivateMethod(this);
					}
				}
			}

			// skip enum implicit methods
			if (this.binding.declaringClass.isEnum() && (this.selector == TypeConstants.VALUES || this.selector == TypeConstants.VALUEOF))
				return;

			// may be in a non necessary <clinit> for innerclass with static final constant fields
			if (this.binding.isAbstract() || this.binding.isNative())
				return;
			
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385780
			if (this.typeParameters != null &&
					!this.scope.referenceCompilationUnit().compilationResult.hasSyntaxError) {
				for (int i = 0, length = this.typeParameters.length; i < length; ++i) {
					TypeParameter typeParameter = this.typeParameters[i];
					if ((typeParameter.binding.modifiers  & ExtraCompilerModifiers.AccLocallyUsed) == 0) {
						this.scope.problemReporter().unusedTypeParameter(typeParameter);						
					}
				}
			}
			ExceptionHandlingFlowContext methodContext =
				new ExceptionHandlingFlowContext(
					flowContext,
					this,
					this.binding.thrownExceptions,
					null,
					this.scope,
					FlowInfo.DEAD_END);

			// nullity and mark as assigned
			analyseArguments(classScope.environment(), flowInfo, this.arguments, this.binding);

			if (this.binding.declaringClass instanceof MemberTypeBinding && !this.binding.declaringClass.isStatic()) {
				// method of a non-static member type can't be static.
				this.bits &= ~ASTNode.CanBeStatic;
			}
			// propagate to statements
			if (this.statements != null) {
				boolean enableSyntacticNullAnalysisForFields = this.scope.compilerOptions().enableSyntacticNullAnalysisForFields;
				int complaintLevel = (flowInfo.reachMode() & FlowInfo.UNREACHABLE) == 0 ? Statement.NOT_COMPLAINED : Statement.COMPLAINED_FAKE_REACHABLE;
				for (int i = 0, count = this.statements.length; i < count; i++) {
					Statement stat = this.statements[i];
					if ((complaintLevel = stat.complainIfUnreachable(flowInfo, this.scope, complaintLevel, true)) < Statement.COMPLAINED_UNREACHABLE) {
						flowInfo = stat.analyseCode(this.scope, methodContext, flowInfo);
					}
					if (enableSyntacticNullAnalysisForFields) {
						methodContext.expireNullCheckedFieldInfo();
					}
				}
			} else {
				// method with empty body should not be flagged as static.
				this.bits &= ~ASTNode.CanBeStatic;
			}
			// check for missing returning path
			TypeBinding returnTypeBinding = this.binding.returnType;
			if ((returnTypeBinding == TypeBinding.VOID) || isAbstract()) {
				if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) == 0) {
					this.bits |= ASTNode.NeedFreeReturn;
				}
			} else {
				if (flowInfo != FlowInfo.DEAD_END) {
					this.scope.problemReporter().shouldReturn(returnTypeBinding, this);
				}
			}
			// check unreachable catch blocks
			methodContext.complainIfUnusedExceptionHandlers(this);
			// check unused parameters
			this.scope.checkUnusedParameters(this.binding);
			// check if the method could have been static
			if (!this.binding.isStatic() && (this.bits & ASTNode.CanBeStatic) != 0 && !this.isDefaultMethod()) {
				if(!this.binding.isOverriding() && !this.binding.isImplementing()) {
					if (this.binding.isPrivate() || this.binding.isFinal() || this.binding.declaringClass.isFinal()) {
						this.scope.problemReporter().methodCanBeDeclaredStatic(this);
					} else {
						this.scope.problemReporter().methodCanBePotentiallyDeclaredStatic(this);
					}
				}
					
			}
			this.scope.checkUnclosedCloseables(flowInfo, null, null/*don't report against a specific location*/, null);
		} catch (AbortMethod e) {
			this.ignoreFurtherInvestigation = true;
		}
	}

	@Override
	public void getAllAnnotationContexts(int targetType, List allAnnotationContexts) {
		AnnotationCollector collector = new AnnotationCollector(this.returnType, targetType, allAnnotationContexts);
		for (int i = 0, max = this.annotations.length; i < max; i++) {
			Annotation annotation = this.annotations[i];
			annotation.traverse(collector, (BlockScope) null);
		}
	}
	
	public boolean hasNullTypeAnnotation(AnnotationPosition position) {
		// parser associates SE8 annotations to the declaration
		return TypeReference.containsNullAnnotation(this.annotations) || 
				(this.returnType != null && this.returnType.hasNullTypeAnnotation(position)); // just in case
	}

	@Override
	public boolean isDefaultMethod() {
		return (this.modifiers & ExtraCompilerModifiers.AccDefaultMethod) != 0;
	}

	@Override
	public boolean isMethod() {
		return true;
	}

	@Override
	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		//fill up the method body with statement
		parser.parse(this, unit);
	}

	@Override
	public StringBuffer printReturnType(int indent, StringBuffer output) {
		if (this.returnType == null) return output;
		return this.returnType.printExpression(0, output).append(' ');
	}

	@Override
	public void resolveStatements() {
		// ========= abort on fatal error =============
		if (this.returnType != null && this.binding != null) {
			this.bits |= (this.returnType.bits & ASTNode.HasTypeAnnotations);
			this.returnType.resolvedType = this.binding.returnType;
			// record the return type binding
		}
		// check if method with constructor name
		if (CharOperation.equals(this.scope.enclosingSourceType().sourceName, this.selector)) {
			this.scope.problemReporter().methodWithConstructorName(this);
		}
		// to check whether the method returns a type parameter not declared by it.
		boolean returnsUndeclTypeVar = false;
		if (this.returnType != null && this.returnType.resolvedType instanceof TypeVariableBinding) {
			returnsUndeclTypeVar = true;
		}
		if (this.typeParameters != null) {
			for (int i = 0, length = this.typeParameters.length; i < length; i++) {
				TypeParameter typeParameter = this.typeParameters[i];
				this.bits |= (typeParameter.bits & ASTNode.HasTypeAnnotations);
				// typeParameter is already resolved from Scope#connectTypeVariables()
				if (returnsUndeclTypeVar && TypeBinding.equalsEquals(this.typeParameters[i].binding, this.returnType.resolvedType)) {
					returnsUndeclTypeVar = false;
				}
			}
		}

		// check @Override annotation
		final CompilerOptions compilerOptions = this.scope.compilerOptions();
		checkOverride: {
			if (this.binding == null) break checkOverride;
			long complianceLevel = compilerOptions.complianceLevel;
			if (complianceLevel < ClassFileConstants.JDK1_5) break checkOverride;
			int bindingModifiers = this.binding.modifiers;
			boolean hasOverrideAnnotation = (this.binding.tagBits & TagBits.AnnotationOverride) != 0;
			boolean hasUnresolvedArguments = (this.binding.tagBits & TagBits.HasUnresolvedArguments) != 0;
			if (hasOverrideAnnotation  && !hasUnresolvedArguments) {
				// no static method is considered overriding
				if ((bindingModifiers & (ClassFileConstants.AccStatic|ExtraCompilerModifiers.AccOverriding)) == ExtraCompilerModifiers.AccOverriding)
					break checkOverride;
				//	in 1.5, strictly for overriding superclass method
				//	in 1.6 and above, also tolerate implementing interface method
				if (complianceLevel >= ClassFileConstants.JDK1_6
						&& ((bindingModifiers & (ClassFileConstants.AccStatic|ExtraCompilerModifiers.AccImplementing)) == ExtraCompilerModifiers.AccImplementing))
					break checkOverride;
				// claims to override, and doesn't actually do so
				this.scope.problemReporter().methodMustOverride(this, complianceLevel);
			} else {
				//In case of  a concrete class method, we have to check if it overrides(in 1.5 and above) OR implements a method(1.6 and above).
				//Also check if the method has a signature that is override-equivalent to that of any public method declared in Object.
				if (!this.binding.declaringClass.isInterface()){
						if((bindingModifiers & (ClassFileConstants.AccStatic|ExtraCompilerModifiers.AccOverriding)) == ExtraCompilerModifiers.AccOverriding) {
							this.scope.problemReporter().missingOverrideAnnotation(this);
						} else {
							if(complianceLevel >= ClassFileConstants.JDK1_6
								&& compilerOptions.reportMissingOverrideAnnotationForInterfaceMethodImplementation
								&& this.binding.isImplementing()) {
									// actually overrides, but did not claim to do so
									this.scope.problemReporter().missingOverrideAnnotationForInterfaceMethodImplementation(this);
							}
							
						}
				}
				else {	//For 1.6 and above only
					//In case of a interface class method, we have to check if it overrides a method (isImplementing returns true in case it overrides)
					//Also check if the method has a signature that is override-equivalent to that of any public method declared in Object.
					if(complianceLevel >= ClassFileConstants.JDK1_6
							&& compilerOptions.reportMissingOverrideAnnotationForInterfaceMethodImplementation
							&& (((bindingModifiers & (ClassFileConstants.AccStatic|ExtraCompilerModifiers.AccOverriding)) == ExtraCompilerModifiers.AccOverriding) || this.binding.isImplementing())){
						// actually overrides, but did not claim to do so
						this.scope.problemReporter().missingOverrideAnnotationForInterfaceMethodImplementation(this);
					}
				}
			}
		}

		switch (TypeDeclaration.kind(this.scope.referenceType().modifiers)) {
			case TypeDeclaration.ENUM_DECL :
				if (this.selector == TypeConstants.VALUES) break;
				if (this.selector == TypeConstants.VALUEOF) break;
				//$FALL-THROUGH$
			case TypeDeclaration.CLASS_DECL :
				// if a method has an semicolon body and is not declared as abstract==>error
				// native methods may have a semicolon body
				if ((this.modifiers & ExtraCompilerModifiers.AccSemicolonBody) != 0) {
					if ((this.modifiers & ClassFileConstants.AccNative) == 0)
						if ((this.modifiers & ClassFileConstants.AccAbstract) == 0)
							this.scope.problemReporter().methodNeedBody(this);
				} else {
					// the method HAS a body --> abstract native modifiers are forbidden
					if (((this.modifiers & ClassFileConstants.AccNative) != 0) || ((this.modifiers & ClassFileConstants.AccAbstract) != 0))
						this.scope.problemReporter().methodNeedingNoBody(this);
					else if (this.binding == null || this.binding.isStatic() || (this.binding.declaringClass instanceof LocalTypeBinding) || returnsUndeclTypeVar) {
						// Cannot be static for one of the reasons stated above
						this.bits &= ~ASTNode.CanBeStatic;
					}
				}
				break;
			case TypeDeclaration.INTERFACE_DECL :
				if (compilerOptions.sourceLevel >= ClassFileConstants.JDK1_8
						&& (this.modifiers & (ExtraCompilerModifiers.AccSemicolonBody | ClassFileConstants.AccAbstract)) == ExtraCompilerModifiers.AccSemicolonBody) {
					boolean isPrivateMethod = compilerOptions.sourceLevel >= ClassFileConstants.JDK9 && (this.modifiers & ClassFileConstants.AccPrivate) != 0;
					if (isPrivateMethod || ((this.modifiers & (ClassFileConstants.AccStatic | ExtraCompilerModifiers.AccDefaultMethod)) != 0)) {
							this.scope.problemReporter().methodNeedBody(this);
					}
				}
				break;
		}
		super.resolveStatements();

		// TagBits.OverridingMethodWithSupercall is set during the resolveStatements() call
		if (compilerOptions.getSeverity(CompilerOptions.OverridingMethodWithoutSuperInvocation) != ProblemSeverities.Ignore) {
			if (this.binding != null) {
        		int bindingModifiers = this.binding.modifiers;
        		if ((bindingModifiers & (ExtraCompilerModifiers.AccOverriding|ExtraCompilerModifiers.AccImplementing)) == ExtraCompilerModifiers.AccOverriding
        				&& (this.bits & ASTNode.OverridingMethodWithSupercall) == 0) {
        			this.scope.problemReporter().overridesMethodWithoutSuperInvocation(this.binding);
        		}
			}
		}
	}

	@Override
	public void traverse(
		ASTVisitor visitor,
		ClassScope classScope) {

		if (visitor.visit(this, classScope)) {
			if (this.javadoc != null) {
				this.javadoc.traverse(visitor, this.scope);
			}
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, this.scope);
			}
			if (this.typeParameters != null) {
				int typeParametersLength = this.typeParameters.length;
				for (int i = 0; i < typeParametersLength; i++) {
					this.typeParameters[i].traverse(visitor, this.scope);
				}
			}
			if (this.returnType != null)
				this.returnType.traverse(visitor, this.scope);
			if (this.arguments != null) {
				int argumentLength = this.arguments.length;
				for (int i = 0; i < argumentLength; i++)
					this.arguments[i].traverse(visitor, this.scope);
			}
			if (this.thrownExceptions != null) {
				int thrownExceptionsLength = this.thrownExceptions.length;
				for (int i = 0; i < thrownExceptionsLength; i++)
					this.thrownExceptions[i].traverse(visitor, this.scope);
			}
			if (this.statements != null) {
				int statementsLength = this.statements.length;
				for (int i = 0; i < statementsLength; i++)
					this.statements[i].traverse(visitor, this.scope);
			}
		}
		visitor.endVisit(this, classScope);
	}
	@Override
	public TypeParameter[] typeParameters() {
	    return this.typeParameters;
	}
}
