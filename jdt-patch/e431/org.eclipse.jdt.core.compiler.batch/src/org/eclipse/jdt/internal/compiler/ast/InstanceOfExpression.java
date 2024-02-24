/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 383368 - [compiler][null] syntactic null analysis for field references
 *								Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *								Bug 416307 - [1.8][compiler][null] subclass with type parameter substitution confuses null checking
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 417295 - [1.8[[null] Massage type annotated null analysis to gel well with deep encoded type bindings.
 *								Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *								Bug 435570 - [1.8][null] @NonNullByDefault illegally tries to affect "throws E"
 *								Bug 466713 - Null Annotations: NullPointerException using <int @Nullable []> as Type Param
 *        Andy Clement - Contributions for
 *                          Bug 383624 - [1.8][compiler] Revive code generation support for type annotations (from Olivier's work)
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationPosition;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class InstanceOfExpression extends OperatorExpression {

	public Expression expression;
	public TypeReference type;
	public LocalDeclaration elementVariable;
	public Pattern pattern;
	static final char[] SECRET_EXPRESSION_VALUE = " secretExpressionValue".toCharArray(); //$NON-NLS-1$

	private LocalVariableBinding secretExpressionValue = null;

public InstanceOfExpression(Expression expression, TypeReference type) {
	this.expression = expression;
	this.type = type;
	type.bits |= IgnoreRawTypeCheck; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=282141
	this.bits |= INSTANCEOF << OperatorSHIFT;
	this.sourceStart = expression.sourceStart;
	this.sourceEnd = type.sourceEnd;
}
public InstanceOfExpression(Expression expression, Pattern pattern) {
	this.expression = expression;
	this.pattern = pattern;
	this.elementVariable = pattern.getPatternVariable();
	this.type = pattern.getType();
	this.type.bits |= IgnoreRawTypeCheck;
	this.bits |= INSTANCEOF << OperatorSHIFT;
	this.sourceStart = expression.sourceStart;
	this.sourceEnd = this.pattern.sourceEnd;
}

@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	LocalVariableBinding local = this.expression.localVariableBinding();
	FlowInfo initsWhenTrue = null;
	if (local != null && (local.type.tagBits & TagBits.IsBaseType) == 0) {
		flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo).
			unconditionalInits();
		initsWhenTrue = flowInfo.copy();
		initsWhenTrue.markAsComparedEqualToNonNull(local);
		flowContext.recordUsingNullReference(currentScope, local,
				this.expression, FlowContext.CAN_ONLY_NULL | FlowContext.IN_INSTANCEOF, flowInfo);
		// no impact upon enclosing try context
		flowInfo =  FlowInfo.conditional(initsWhenTrue.copy(), flowInfo.copy());
	} else if (this.expression instanceof Reference) {
		if (currentScope.compilerOptions().enableSyntacticNullAnalysisForFields) {
			FieldBinding field = ((Reference)this.expression).lastFieldBinding();
			if (field != null && (field.type.tagBits & TagBits.IsBaseType) == 0) {
				flowContext.recordNullCheckedFieldReference((Reference) this.expression, 1);
			}
		}
	}
	if (initsWhenTrue == null) {
		flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo).
				unconditionalInits();
	}
	if (this.pattern != null) {
		FlowInfo patternFlow = this.pattern.analyseCode(currentScope, flowContext, (initsWhenTrue == null) ? flowInfo : initsWhenTrue);
		initsWhenTrue = initsWhenTrue == null ? patternFlow : initsWhenTrue.addInitializationsFrom(patternFlow);
	}
	return (initsWhenTrue == null) ? flowInfo :
			FlowInfo.conditional(initsWhenTrue, flowInfo.copy());
}
/**
 * Code generation for instanceOfExpression
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
*/
@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	if (this.elementVariable != null && this.elementVariable.binding != null) {
		this.elementVariable.binding.modifiers &= ~ExtraCompilerModifiers.AccOutOfFlowScope;
	}
	addPatternVariables(currentScope, codeStream);

	int pc = codeStream.position;

	this.expression.generateCode(currentScope, codeStream, true);
	if (this.secretExpressionValue != null) {
		codeStream.store(this.secretExpressionValue, true);
		codeStream.addVariable(this.secretExpressionValue);
	}
	codeStream.instance_of(this.type, this.type.resolvedType);
	if (this.pattern != null) {
		BranchLabel falseLabel = new BranchLabel(codeStream);
		BranchLabel trueLabel = new BranchLabel(codeStream);
		BranchLabel continueLabel = new BranchLabel(codeStream);
		codeStream.ifeq(falseLabel);

		if (this.secretExpressionValue != null) {
			codeStream.load(this.secretExpressionValue);
			codeStream.removeVariable(this.secretExpressionValue);
		} else {
			this.expression.generateCode(currentScope, codeStream, true);
		}
		this.pattern.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel);

		trueLabel.place();
		codeStream.iconst_1();
		codeStream.goto_(continueLabel);
		falseLabel.place();
		for (LocalVariableBinding binding : this.pattern.bindingsWhenTrue()) {
			binding.recordInitializationEndPC(codeStream.position);
		}
		codeStream.iconst_0();
		continueLabel.place();

	}
	if (valueRequired) {
		codeStream.generateImplicitConversion(this.implicitConversion);
	} else {
		codeStream.pop();
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
@Override
public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
	// a label valued to nil means: by default we fall through the case...
	// both nil means we leave the value on the stack

	if (this.elementVariable == null && this.pattern == null) {
		super.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
		return;
	}
	addPatternVariables(currentScope, codeStream);

	int pc = codeStream.position;

	this.expression.generateCode(currentScope, codeStream, true);
	if (this.secretExpressionValue != null) {
		codeStream.store(this.secretExpressionValue, true);
		codeStream.addVariable(this.secretExpressionValue);
	}

	BranchLabel nextSibling = falseLabel != null ? falseLabel : new BranchLabel(codeStream);
	codeStream.instance_of(this.type, this.type.resolvedType);
	codeStream.ifeq(nextSibling);
	if (this.secretExpressionValue != null) {
		codeStream.load(this.secretExpressionValue);
		codeStream.removeVariable(this.secretExpressionValue);
	} else {
		this.expression.generateCode(currentScope, codeStream, true);
	}

	if (this.pattern instanceof RecordPattern) {
		this.pattern.generateOptimizedBoolean(currentScope, codeStream, trueLabel, nextSibling);
	} else {
		codeStream.checkcast(this.type, this.type.resolvedType, codeStream.position);
		codeStream.store(this.elementVariable.binding, false);
	}

	if (valueRequired) {
		codeStream.generateImplicitConversion(this.implicitConversion);
	} else {
		codeStream.pop();
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);

	int position = codeStream.position;
	if (valueRequired) {
		if (falseLabel == null) {
			if (trueLabel != null) {
				// Implicit falling through the FALSE case
				codeStream.goto_(trueLabel);
			}
		} else {
			if (trueLabel == null) {
				// Implicit falling through the TRUE case
			} else {
				// No implicit fall through TRUE/FALSE --> should never occur
			}
		}
	}
	codeStream.recordPositionsFrom(position, this.sourceEnd);

	if (nextSibling != falseLabel)
		nextSibling.place();
}

@Override
public StringBuilder printExpressionNoParenthesis(int indent, StringBuilder output) {
	this.expression.printExpression(indent, output).append(" instanceof "); //$NON-NLS-1$
	return this.pattern == null ? this.type.print(0, output) : this.pattern.printExpression(0, output);
}

@Override
public void addPatternVariables(BlockScope currentScope, CodeStream codeStream) {
	for (LocalVariableBinding local: bindingsWhenTrue()) {
		codeStream.addVisibleLocalVariable(local);
	}
}
@Override
public LocalVariableBinding[] bindingsWhenTrue() {
	return this.pattern != null ? this.pattern.bindingsWhenTrue() : NO_VARIABLES;
}
@Override
public boolean containsPatternVariable() {
	return this.elementVariable != null || this.pattern != null;
}
@Override
public LocalDeclaration getPatternVariable() {
	return this.elementVariable;
}
@Override
public TypeBinding resolveType(BlockScope scope) {
	this.constant = Constant.NotAConstant;

	TypeBinding checkedType = this.type.resolveType(scope, true /* check bounds*/);
	if (this.expression instanceof CastExpression) {
		((CastExpression) this.expression).setInstanceofType(checkedType); // for cast expression we need to know instanceof type to not tag unnecessary when needed
	}
	TypeBinding expressionType = this.expression.resolveType(scope);
	if (this.pattern != null) {
		this.pattern.setExpressionContext(ExpressionContext.INSTANCEOF_CONTEXT);
		this.pattern.setExpectedType(this.expression.resolvedType);
		this.pattern.resolveType(scope);

		if ((this.expression.bits & ASTNode.RestrictiveFlagMASK) != Binding.LOCAL) {
			// reevaluation may double jeopardize as side effects may recur, compute once and cache
			LocalVariableBinding local =
					new LocalVariableBinding(
						InstanceOfExpression.SECRET_EXPRESSION_VALUE,
						TypeBinding.wellKnownType(scope, T_JavaLangObject), // good enough, no need for sharper type.
						ClassFileConstants.AccDefault,
						false);
			local.setConstant(Constant.NotAConstant);
			local.useFlag = LocalVariableBinding.USED;
			scope.addLocalVariable(local);
			this.secretExpressionValue = local;
			if (expressionType != TypeBinding.NULL)
				this.secretExpressionValue.type = expressionType;
		}
	}
	if (expressionType != null && checkedType != null && this.type.hasNullTypeAnnotation(AnnotationPosition.ANY)) {
		// don't complain if the entire operation is redundant anyway
		if (!expressionType.isCompatibleWith(checkedType) || NullAnnotationMatching.analyse(checkedType, expressionType, -1).isAnyMismatch())
			scope.problemReporter().nullAnnotationUnsupportedLocation(this.type);
	}

	if (expressionType == null || checkedType == null)
		return null;

	if (!checkedType.isReifiable()) {
		CompilerOptions options = scope.compilerOptions();
		// Report same as before for older compliances
		if (options.complianceLevel < ClassFileConstants.JDK16) {
			scope.problemReporter().illegalInstanceOfGenericType(checkedType, this);
		} else {
			if (expressionType != TypeBinding.NULL) {
				boolean isLegal = checkCastTypesCompatibility(scope, checkedType, expressionType, this.expression, true);
				if (!isLegal || (this.bits & ASTNode.UnsafeCast) != 0) {
					scope.problemReporter().unsafeCastInInstanceof(this.expression, checkedType, expressionType);
				}
			}
		}
	} else if (checkedType.isValidBinding()) {
		// if not a valid binding, an error has already been reported for unresolved type
		if ((expressionType != TypeBinding.NULL && expressionType.isBaseType()) // disallow autoboxing
				|| checkedType.isBaseType()
				|| !checkCastTypesCompatibility(scope, checkedType, expressionType, null, true)) {
			scope.problemReporter().notCompatibleTypesError(this, expressionType, checkedType);
		}
	}

	return this.resolvedType = TypeBinding.BOOLEAN;
}
@Override
public boolean checkUnsafeCast(Scope scope, TypeBinding castType, TypeBinding expressionType, TypeBinding match, boolean isNarrowing) {
	if (!castType.isReifiable())
		return CastExpression.checkUnsafeCast(this, scope, castType, expressionType, match, isNarrowing);
	else
		return super.checkUnsafeCast(scope, castType, expressionType, match, isNarrowing);
}
/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#tagAsUnnecessaryCast(Scope,TypeBinding)
 */

@Override
public void tagAsUnnecessaryCast(Scope scope, TypeBinding castType) {
	// null is not instanceof Type, recognize direct scenario
	if (this.expression.resolvedType != TypeBinding.NULL)
		scope.problemReporter().unnecessaryInstanceof(this, castType);
}

@Override
public void traverse(ASTVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		this.expression.traverse(visitor, scope);
		if (this.pattern != null) {
			this.pattern.traverse(visitor, scope);
		} else {
			this.type.traverse(visitor, scope);
		}
	}
	visitor.endVisit(this, scope);
}
}
