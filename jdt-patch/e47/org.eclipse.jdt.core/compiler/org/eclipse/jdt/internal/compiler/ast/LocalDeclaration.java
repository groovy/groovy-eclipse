/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *							bug 319201 - [null] no warning when unboxing SingleNameReference causes NPE
 *							bug 292478 - Report potentially null across variable assignment
 *							bug 335093 - [compiler][null] minimal hook for future null annotation support
 *							bug 349326 - [1.7] new warning for missing try-with-resources
 *							bug 186342 - [compiler][null] Using annotations for null checking
 *							bug 358903 - Filter practically unimportant resource leak warnings
 *							bug 370639 - [compiler][resource] restore the default for resource leak warnings
 *							bug 365859 - [compiler][null] distinguish warnings based on flow analysis vs. null annotations
 *							bug 388996 - [compiler][resource] Incorrect 'potential resource leak'
 *							bug 394768 - [compiler][resource] Incorrect resource leak warning when creating stream in conditional
 *							bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *							bug 383368 - [compiler][null] syntactic null analysis for field references
 *							bug 400761 - [compiler][null] null may be return as boolean without a diagnostic
 *							Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *							Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *							Bug 427438 - [1.8][compiler] NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode(ConditionalExpression.java:280)
 *							Bug 430150 - [1.8][null] stricter checking against type variables
 *							Bug 453483 - [compiler][null][loop] Improve null analysis for loops
 *     Jesper S Moller - Contributions for
 *							Bug 378674 - "The method can be declared as static" is wrong
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *							Bug 409250 - [1.8][compiler] Various loose ends in 308 code generation
 *							Bug 426616 - [1.8][compiler] Type Annotations, multiple problems 
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.ASSIGNMENT_CONTEXT;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationCollector;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;

@SuppressWarnings("rawtypes")
public class LocalDeclaration extends AbstractVariableDeclaration {

	public LocalVariableBinding binding;

	public LocalDeclaration(
		char[] name,
		int sourceStart,
		int sourceEnd) {

		this.name = name;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.declarationEnd = sourceEnd;
	}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// record variable initialization if any
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) == 0) {
		this.bits |= ASTNode.IsLocalDeclarationReachable; // only set if actually reached
	}
	if (this.initialization == null) {
		return flowInfo;
	}
	this.initialization.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
	
	FlowInfo preInitInfo = null;
	boolean shouldAnalyseResource = this.binding != null 
			&& flowInfo.reachMode() == FlowInfo.REACHABLE
			&& currentScope.compilerOptions().analyseResourceLeaks
			&& FakedTrackingVariable.isAnyCloseable(this.initialization.resolvedType);
	if (shouldAnalyseResource) {
		preInitInfo = flowInfo.unconditionalCopy();
		// analysis of resource leaks needs additional context while analyzing the RHS:
		FakedTrackingVariable.preConnectTrackerAcrossAssignment(this, this.binding, this.initialization, flowInfo);
	}

	flowInfo =
		this.initialization
			.analyseCode(currentScope, flowContext, flowInfo)
			.unconditionalInits();

	if (shouldAnalyseResource)
		FakedTrackingVariable.handleResourceAssignment(currentScope, preInitInfo, flowInfo, flowContext, this, this.initialization, this.binding);
	else
		FakedTrackingVariable.cleanUpAfterAssignment(currentScope, Binding.LOCAL, this.initialization);

	int nullStatus = this.initialization.nullStatus(flowInfo, flowContext);
	if (!flowInfo.isDefinitelyAssigned(this.binding)){// for local variable debug attributes
		this.bits |= FirstAssignmentToLocal;
	} else {
		this.bits &= ~FirstAssignmentToLocal;  // int i = (i = 0);
	}
	flowInfo.markAsDefinitelyAssigned(this.binding);
	if (currentScope.compilerOptions().isAnnotationBasedNullAnalysisEnabled) {
		nullStatus = NullAnnotationMatching.checkAssignment(currentScope, flowContext, this.binding, flowInfo, nullStatus, this.initialization, this.initialization.resolvedType);
	}
	if ((this.binding.type.tagBits & TagBits.IsBaseType) == 0) {
		flowInfo.markNullStatus(this.binding, nullStatus);
		// no need to inform enclosing try block since its locals won't get
		// known by the finally block
	}
	return flowInfo;
}

	public void checkModifiers() {

		//only potential valid modifier is <<final>>
		if (((this.modifiers & ExtraCompilerModifiers.AccJustFlag) & ~ClassFileConstants.AccFinal) != 0)
			//AccModifierProblem -> other (non-visibility problem)
			//AccAlternateModifierProblem -> duplicate modifier
			//AccModifierProblem | AccAlternateModifierProblem -> visibility problem"

			this.modifiers = (this.modifiers & ~ExtraCompilerModifiers.AccAlternateModifierProblem) | ExtraCompilerModifiers.AccModifierProblem;
	}

	/**
	 * Code generation for a local declaration:
	 *	i.e.&nbsp;normal assignment to a local variable + unused variable handling
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		// even if not reachable, variable must be added to visible if allocated (28298)
		if (this.binding.resolvedPosition != -1) {
			codeStream.addVisibleLocalVariable(this.binding);
		}
		if ((this.bits & IsReachable) == 0) {
			return;
		}
		int pc = codeStream.position;

		// something to initialize?
		generateInit: {
			if (this.initialization == null)
				break generateInit;
			// forget initializing unused or final locals set to constant value (final ones are inlined)
			if (this.binding.resolvedPosition < 0) {
				if (this.initialization.constant != Constant.NotAConstant)
					break generateInit;
				// if binding unused generate then discard the value
				this.initialization.generateCode(currentScope, codeStream, false);
				break generateInit;
			}
			this.initialization.generateCode(currentScope, codeStream, true);
			// 26903, need extra cast to store null in array local var
			if (this.binding.type.isArrayType()
				&& ((this.initialization instanceof CastExpression)	// arrayLoc = (type[])null
						&& (((CastExpression)this.initialization).innermostCastedExpression().resolvedType == TypeBinding.NULL))){
				codeStream.checkcast(this.binding.type);
			}
			codeStream.store(this.binding, false);
			if ((this.bits & ASTNode.FirstAssignmentToLocal) != 0) {
				/* Variable may have been initialized during the code initializing it
					e.g. int i = (i = 1);
				*/
				this.binding.recordInitializationStartPC(codeStream.position);
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration#getKind()
	 */
	public int getKind() {
		return LOCAL_VARIABLE;
	}

	// for local variables
	public void getAllAnnotationContexts(int targetType, LocalVariableBinding localVariable, List allAnnotationContexts) {
		AnnotationCollector collector = new AnnotationCollector(this, targetType, localVariable, allAnnotationContexts);
		this.traverseWithoutInitializer(collector, (BlockScope) null);
	}

	// for arguments
	public void getAllAnnotationContexts(int targetType, int parameterIndex, List allAnnotationContexts) {
		AnnotationCollector collector = new AnnotationCollector(this, targetType, parameterIndex, allAnnotationContexts);
		this.traverse(collector, (BlockScope) null);
	}
		
	public boolean isArgument() {
		return false;
	}
	public boolean isReceiver() {
		return false;
	}
	public void resolve(BlockScope scope) {

		// create a binding and add it to the scope
		TypeBinding variableType = this.type.resolveType(scope, true /* check bounds*/);

		this.bits |= (this.type.bits & ASTNode.HasTypeAnnotations);
		checkModifiers();
		if (variableType != null) {
			if (variableType == TypeBinding.VOID) {
				scope.problemReporter().variableTypeCannotBeVoid(this);
				return;
			}
			if (variableType.isArrayType() && ((ArrayBinding) variableType).leafComponentType == TypeBinding.VOID) {
				scope.problemReporter().variableTypeCannotBeVoidArray(this);
				return;
			}
		}

		Binding existingVariable = scope.getBinding(this.name, Binding.VARIABLE, this, false /*do not resolve hidden field*/);
		if (existingVariable != null && existingVariable.isValidBinding()){
			boolean localExists = existingVariable instanceof LocalVariableBinding; 
			if (localExists && (this.bits & ASTNode.ShadowsOuterLocal) != 0 && scope.isLambdaSubscope() && this.hiddenVariableDepth == 0) {
					scope.problemReporter().lambdaRedeclaresLocal(this);
			} else if (localExists && this.hiddenVariableDepth == 0) {
					scope.problemReporter().redefineLocal(this);
			} else {
				scope.problemReporter().localVariableHiding(this, existingVariable, false);
			}
		}

		if ((this.modifiers & ClassFileConstants.AccFinal)!= 0 && this.initialization == null) {
			this.modifiers |= ExtraCompilerModifiers.AccBlankFinal;
		}
		this.binding = new LocalVariableBinding(this, variableType, this.modifiers, false /*isArgument*/);
		scope.addLocalVariable(this.binding);
		this.binding.setConstant(Constant.NotAConstant);
		// allow to recursivelly target the binding....
		// the correct constant is harmed if correctly computed at the end of this method

		if (variableType == null) {
			if (this.initialization != null)
				this.initialization.resolveType(scope); // want to report all possible errors
			return;
		}

		// store the constant for final locals
		if (this.initialization != null) {
			if (this.initialization instanceof ArrayInitializer) {
				TypeBinding initializationType = this.initialization.resolveTypeExpecting(scope, variableType);
				if (initializationType != null) {
					((ArrayInitializer) this.initialization).binding = (ArrayBinding) initializationType;
					this.initialization.computeConversion(scope, variableType, initializationType);
				}
			} else {
				this.initialization.setExpressionContext(ASSIGNMENT_CONTEXT);
				this.initialization.setExpectedType(variableType);
				TypeBinding initializationType = this.initialization.resolveType(scope);
				if (initializationType != null) {
					if (TypeBinding.notEquals(variableType, initializationType)) // must call before computeConversion() and typeMismatchError()
						scope.compilationUnitScope().recordTypeConversion(variableType, initializationType);
					if (this.initialization.isConstantValueOfTypeAssignableToType(initializationType, variableType)
						|| initializationType.isCompatibleWith(variableType, scope)) {
						this.initialization.computeConversion(scope, variableType, initializationType);
						if (initializationType.needsUncheckedConversion(variableType)) {
						    scope.problemReporter().unsafeTypeConversion(this.initialization, initializationType, variableType);
						}
						if (this.initialization instanceof CastExpression
								&& (this.initialization.bits & ASTNode.UnnecessaryCast) == 0) {
							CastExpression.checkNeedForAssignedCast(scope, variableType, (CastExpression) this.initialization);
						}
					} else if (isBoxingCompatible(initializationType, variableType, this.initialization, scope)) {
						this.initialization.computeConversion(scope, variableType, initializationType);
						if (this.initialization instanceof CastExpression
								&& (this.initialization.bits & ASTNode.UnnecessaryCast) == 0) {
							CastExpression.checkNeedForAssignedCast(scope, variableType, (CastExpression) this.initialization);
						}
					} else {
						if ((variableType.tagBits & TagBits.HasMissingType) == 0) {
							// if problem already got signaled on type, do not report secondary problem
							scope.problemReporter().typeMismatchError(initializationType, variableType, this.initialization, null);
						}
					}
				}
			}
			// check for assignment with no effect
			if (this.binding == Expression.getDirectBinding(this.initialization)) {
				scope.problemReporter().assignmentHasNoEffect(this, this.name);
			}
			// change the constant in the binding when it is final
			// (the optimization of the constant propagation will be done later on)
			// cast from constant actual type to variable type
			this.binding.setConstant(
				this.binding.isFinal()
					? this.initialization.constant.castTo((variableType.id << 4) + this.initialization.constant.typeID())
					: Constant.NotAConstant);
		}
		// only resolve annotation at the end, for constant to be positioned before (96991)
		resolveAnnotations(scope, this.annotations, this.binding, true);
		Annotation.isTypeUseCompatible(this.type, scope, this.annotations);
		if (!scope.validateNullAnnotation(this.binding.tagBits, this.type, this.annotations))
			this.binding.tagBits &= ~TagBits.AnnotationNullMASK;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, scope);
			}
			this.type.traverse(visitor, scope);
			if (this.initialization != null)
				this.initialization.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	
	private void traverseWithoutInitializer(ASTVisitor visitor, BlockScope scope) {		
		if (visitor.visit(this, scope)) {
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, scope);
			}
			this.type.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

	public boolean isRecoveredFromLoneIdentifier() { // recovered from lonely identifier or identifier cluster ?
		return this.name == RecoveryScanner.FAKE_IDENTIFIER && 
				(this.type instanceof SingleTypeReference || (this.type instanceof QualifiedTypeReference && !(this.type instanceof ArrayQualifiedTypeReference))) && this.initialization == null && !this.type.isBaseTypeReference();
	}
}
