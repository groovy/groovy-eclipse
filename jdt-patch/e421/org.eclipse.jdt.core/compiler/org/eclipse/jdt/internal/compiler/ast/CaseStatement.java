/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
//import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CaseStatement extends Statement {

	public BranchLabel targetLabel;
	public Expression[] constantExpressions; // case with multiple expressions
	public BranchLabel[] targetLabels; // for multiple expressions
	public boolean isExpr = false;

public CaseStatement(Expression constantExpression, int sourceEnd, int sourceStart) {
	this(sourceEnd, sourceStart, constantExpression != null ? new Expression[] {constantExpression} : null);
}

public CaseStatement(int sourceEnd, int sourceStart, Expression[] constantExpressions) {
	this.constantExpressions = constantExpressions;
	this.sourceEnd = sourceEnd;
	this.sourceStart = sourceStart;
}

@Override
public FlowInfo analyseCode(
	BlockScope currentScope,
	FlowContext flowContext,
	FlowInfo flowInfo) {
	if (this.constantExpressions != null) {
		for (Expression e : this.constantExpressions) {
			analyseConstantExpression(currentScope, flowContext, flowInfo, e);
		}
	}
	return flowInfo;
}
private void analyseConstantExpression(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		Expression e) {
	if (e.constant == Constant.NotAConstant
			&& !e.resolvedType.isEnum()) {
		currentScope.problemReporter().caseExpressionMustBeConstant(e);
	}
	e.analyseCode(currentScope, flowContext, flowInfo);
}

@Override
public StringBuffer printStatement(int tab, StringBuffer output) {
	printIndent(tab, output);
	if (this.constantExpressions == null) {
		output.append("default "); //$NON-NLS-1$
		output.append(this.isExpr ? "->" : ":"); //$NON-NLS-1$ //$NON-NLS-2$
	} else {
		output.append("case "); //$NON-NLS-1$
		for (int i = 0, l = this.constantExpressions.length; i < l; ++i) {
			this.constantExpressions[i].printExpression(0, output);
			if (i < l -1) output.append(',');
		}
		output.append(this.isExpr ? " ->" : " :"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	return output;
}

/**
 * Case code generation
 *
 */
@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((this.bits & ASTNode.IsReachable) == 0) {
		return;
	}
	int pc = codeStream.position;
	if (this.targetLabels != null) {
		for (int i = 0, l = this.targetLabels.length; i < l; ++i) {
			this.targetLabels[i].place();
		}
	} else {
		this.targetLabel.place();
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

/**
 * No-op : should use resolveCase(...) instead.
 */
@Override
public void resolve(BlockScope scope) {
	// no-op : should use resolveCase(...) instead.
}

/**
 * Returns the constant intValue or ordinal for enum constants. If constant is NotAConstant, then answers Float.MIN_VALUE
 * see org.eclipse.jdt.internal.compiler.ast.Statement#resolveCase(org.eclipse.jdt.internal.compiler.lookup.BlockScope, org.eclipse.jdt.internal.compiler.lookup.TypeBinding, org.eclipse.jdt.internal.compiler.ast.SwitchStatement)
 */
@Override
public Constant[] resolveCase(BlockScope scope, TypeBinding switchExpressionType, SwitchStatement switchStatement) {
	// switchExpressionType maybe null in error case
	scope.enclosingCase = this; // record entering in a switch case block
	Expression[] constExprs = this.constantExpressions;
	Expression constExpr = constExprs != null && constExprs.length > 0 ? constExprs[0] : null;
	if (constExpr == null) {
		// remember the default case into the associated switch statement
		if (switchStatement.defaultCase != null)
			scope.problemReporter().duplicateDefaultCase(this);

		// on error the last default will be the selected one ...
		switchStatement.defaultCase = this;
		return Constant.NotAConstantList;
	}
	// add into the collection of cases of the associated switch statement
	switchStatement.cases[switchStatement.caseCount++] = this;
	if (switchExpressionType != null && switchExpressionType.isEnum() && (constExpr instanceof SingleNameReference)) {
		((SingleNameReference) constExpr).setActualReceiverType((ReferenceBinding)switchExpressionType);
	}
	TypeBinding caseType = constExpr.resolveType(scope);
	if (caseType == null || switchExpressionType == null) return Constant.NotAConstantList;
	// tag constant name with enum type for privileged access to its members

	List<Constant> cases = new ArrayList<>();
	for (Expression e : constExprs) {
		if (e != constExpr) {
			if (switchExpressionType.isEnum() && (e instanceof SingleNameReference)) {
				((SingleNameReference) e).setActualReceiverType((ReferenceBinding)switchExpressionType);
			}
			e.resolveType(scope);
		}
		Constant con = resolveConstantExpression(scope, caseType, switchExpressionType, switchStatement, e);
		if (con != Constant.NotAConstant) {
			cases.add(con);
		}
	}
	if (cases.size() > 0) {
		return cases.toArray(new Constant[cases.size()]);
	}

	return Constant.NotAConstantList;
}
public Constant resolveConstantExpression(BlockScope scope,
											TypeBinding caseType,
											TypeBinding switchExpressionType,
											SwitchStatement switchStatement,
											Expression expression) {

	if (expression.isConstantValueOfTypeAssignableToType(caseType, switchExpressionType)
			|| caseType.isCompatibleWith(switchExpressionType)) {
		if (caseType.isEnum()) {
			if (((expression.bits & ASTNode.ParenthesizedMASK) >> ASTNode.ParenthesizedSHIFT) != 0) {
				scope.problemReporter().enumConstantsCannotBeSurroundedByParenthesis(expression);
			}

			if (expression instanceof NameReference
					&& (expression.bits & ASTNode.RestrictiveFlagMASK) == Binding.FIELD) {
				NameReference reference = (NameReference) expression;
				FieldBinding field = reference.fieldBinding();
				if ((field.modifiers & ClassFileConstants.AccEnum) == 0) {
					 scope.problemReporter().enumSwitchCannotTargetField(reference, field);
				} else 	if (reference instanceof QualifiedNameReference) {
					 scope.problemReporter().cannotUseQualifiedEnumConstantInCaseLabel(reference, field);
				}
				return IntConstant.fromValue(field.original().id + 1); // (ordinal value + 1) zero should not be returned see bug 141810
			}
		} else {
			return expression.constant;
		}
	} else if (isBoxingCompatible(caseType, switchExpressionType, expression, scope)) {
		// constantExpression.computeConversion(scope, caseType, switchExpressionType); - do not report boxing/unboxing conversion
		return expression.constant;
	}
	scope.problemReporter().typeMismatchError(caseType, switchExpressionType, expression, switchStatement.expression);
	return Constant.NotAConstant;
}

@Override
public void traverse(ASTVisitor visitor, 	BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		if (this.constantExpressions != null) {
			for (Expression e : this.constantExpressions) {
				e.traverse(visitor, blockScope);
			}
		}

	}
	visitor.endVisit(this, blockScope);
}
}
