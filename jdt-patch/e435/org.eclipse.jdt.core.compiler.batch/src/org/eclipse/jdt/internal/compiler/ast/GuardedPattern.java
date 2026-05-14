/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class GuardedPattern extends Pattern {

	public Pattern primaryPattern;
	public Expression condition;
	public int whenSourceStart = -1;

	public GuardedPattern(Pattern primaryPattern, Expression condition) {
		this.primaryPattern = primaryPattern;
		this.condition = condition;
		this.sourceStart = primaryPattern.sourceStart;
		this.sourceEnd = condition.sourceEnd;
	}

	@Override
	public LocalVariableBinding[] bindingsWhenTrue() {
		return LocalVariableBinding.merge(this.primaryPattern.bindingsWhenTrue(),
											this.condition.bindingsWhenTrue());
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		flowInfo = this.primaryPattern.analyseCode(currentScope, flowContext, flowInfo);
		FlowInfo mergedFlow = this.condition.analyseCode(currentScope, flowContext, flowInfo);
		return mergedFlow.safeInitsWhenTrue();
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, BranchLabel patternMatchLabel, BranchLabel matchFailLabel) {
		BranchLabel guardCheckLabel = new BranchLabel(codeStream);
		this.primaryPattern.setOuterExpressionType(this.outerExpressionType);
		this.primaryPattern.generateCode(currentScope, codeStream, guardCheckLabel, matchFailLabel);
		guardCheckLabel.place();
		this.condition.generateOptimizedBoolean(currentScope, codeStream, null, matchFailLabel, true);
	}

	@Override
	public boolean matchFailurePossible() {
		return !isUnguarded() || this.primaryPattern.matchFailurePossible();
	}

	@Override
	public boolean isUnguarded() {
		Constant cst = this.condition.optimizedBooleanConstant();
		return cst != null && cst != Constant.NotAConstant && cst.booleanValue() == true;
	}

	@Override
	public void setIsEitherOrPattern() {
		this.primaryPattern.setIsEitherOrPattern();
	}

	@Override
	public void setOuterExpressionType(TypeBinding expressionType) {
		super.setOuterExpressionType(expressionType);
		this.primaryPattern.setOuterExpressionType(expressionType);
	}

	@Override
	public boolean coversType(TypeBinding type, Scope scope) {
		return isUnguarded() && this.primaryPattern.coversType(type, scope);
	}

	@Override
	public boolean dominates(Pattern p) {
		return isUnguarded() && this.primaryPattern.dominates(p);
	}

	@Override
	public Pattern[] getAlternatives() {
		return this.primaryPattern.getAlternatives();
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		if (this.resolvedType != null || this.primaryPattern == null)
			return this.resolvedType;
		this.resolvedType = this.primaryPattern.resolveType(scope);

		try {
			scope.resolvingGuardExpression = true; // as guards cannot nest in the same scope, no save & restore called for
			this.condition.resolveTypeExpectingWithBindings(this.primaryPattern.bindingsWhenTrue(), scope, TypeBinding.BOOLEAN);
		} finally {
			scope.resolvingGuardExpression = false;
		}
		Constant cst = this.condition.optimizedBooleanConstant();
		if (cst.typeID() == TypeIds.T_boolean && cst.booleanValue() == false) {
			scope.problemReporter().falseLiteralInGuard(this.condition);
		}

		if (!isUnguarded())
			this.primaryPattern.setIsGuarded();

		return this.resolvedType = this.primaryPattern.resolvedType;
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		this.primaryPattern.print(indent, output).append(" when "); //$NON-NLS-1$
		return this.condition.print(indent, output);
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			this.primaryPattern.traverse(visitor, scope);
			this.condition.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

	@Override
	protected boolean isApplicable(TypeBinding expressionType, BlockScope scope, ASTNode location) {
		return this.primaryPattern.isApplicable(expressionType, scope, location);
	}
}