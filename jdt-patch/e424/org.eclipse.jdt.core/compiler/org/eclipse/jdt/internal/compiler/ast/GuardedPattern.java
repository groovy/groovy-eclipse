/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class GuardedPattern extends Pattern {

	public Pattern primaryPattern;
	public Expression condition;
	/* package */ BranchLabel thenTarget;
	int thenInitStateIndex1 = -1;
	int thenInitStateIndex2 = -1;

	public GuardedPattern(Pattern primaryPattern, Expression conditionalAndExpression) {
		this.primaryPattern = primaryPattern;
		this.condition = conditionalAndExpression;
		this.sourceStart = primaryPattern.sourceStart;
		this.sourceEnd = conditionalAndExpression.sourceEnd;
	}
	@Override
	public void collectPatternVariablesToScope(LocalVariableBinding[] variables, BlockScope scope) {
		this.primaryPattern.collectPatternVariablesToScope(variables, scope);
		addPatternVariablesWhenTrue(this.primaryPattern.getPatternVariablesWhenTrue());
		this.condition.collectPatternVariablesToScope(getPatternVariablesWhenTrue(), scope);
		addPatternVariablesWhenTrue(this.condition.getPatternVariablesWhenTrue());
	}

	@Override
	public LocalDeclaration getPatternVariableIntroduced() {
		return this.primaryPattern.getPatternVariableIntroduced();
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		flowInfo = this.primaryPattern.analyseCode(currentScope, flowContext, flowInfo);
		this.thenInitStateIndex1 = currentScope.methodScope().recordInitializationStates(flowInfo);
		FlowInfo mergedFlow = this.condition.analyseCode(currentScope, flowContext, flowInfo);
		mergedFlow = mergedFlow.safeInitsWhenTrue();
		this.thenInitStateIndex2 = currentScope.methodScope().recordInitializationStates(mergedFlow);
		return mergedFlow;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
 		this.primaryPattern.generateCode(currentScope, codeStream);

		Constant cst =  this.condition.optimizedBooleanConstant();
		this.thenTarget = new BranchLabel(codeStream);
		this.condition.generateOptimizedBoolean(
				currentScope,
				codeStream,
				this.thenTarget,
				null,
				cst == Constant.NotAConstant);
		if (this.thenInitStateIndex2 != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.thenInitStateIndex2);
			codeStream.addDefinitelyAssignedVariables(currentScope, this.thenInitStateIndex2);
		}
	}

	public boolean isGuardTrueAlways() {
		Constant cst = this.condition.optimizedBooleanConstant();
		return cst != Constant.NotAConstant && cst.booleanValue() == true;
	}
	@Override
	public boolean isTotalForType(TypeBinding type) {
		return this.primaryPattern.isTotalForType(type) && isGuardTrueAlways();
	}
	@Override
	public Pattern primary() {
		return this.primaryPattern;
	}

	@Override
	public void resolve(BlockScope scope) {
		this.resolveType(scope);
	}

	@Override
	public boolean dominates(Pattern p) {
		// Guarded pattern can never dominate another, even if the guards are identical
		return false;
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		if (this.resolvedType != null || this.primaryPattern == null)
			return this.resolvedType;
		this.resolvedType = this.primaryPattern.resolveType(scope);
		this.condition.resolveType(scope);
		LocalDeclaration PatternVar = this.primaryPattern.getPatternVariableIntroduced();
		LocalVariableBinding lvb = PatternVar.binding;
		this.condition.traverse(new ASTVisitor() {
			@Override
			public boolean visit(
					SingleNameReference ref,
					BlockScope skope) {
				LocalVariableBinding local = ref.localVariableBinding();
				if (local != null && local != lvb) {
					ref.bits |= ASTNode.IsUsedInPatternGuard;
				}
				return false;
			}
		}, scope);
		return this.resolvedType = this.primaryPattern.resolvedType;
	}

	@Override
	public TypeBinding resolveAtType(BlockScope scope, TypeBinding u) {
		if (this.resolvedType == null || this.primaryPattern == null)
			return null;
		if (this.primaryPattern.isTotalForType(u))
			return this.primaryPattern.resolveAtType(scope, u);

		return this.resolvedType; //else leave the pattern untouched for now.
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		this.primaryPattern.print(indent, output).append(" && "); //$NON-NLS-1$
		return this.condition.print(indent, output);
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.primaryPattern != null)
				this.primaryPattern.traverse(visitor, scope);
			if (this.condition != null)
				this.condition.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	public void suspendVariables(CodeStream codeStream, BlockScope scope) {
		codeStream.removeNotDefinitelyAssignedVariables(scope, this.thenInitStateIndex1);
	}
	public void resumeVariables(CodeStream codeStream, BlockScope scope) {
		codeStream.addDefinitelyAssignedVariables(scope, this.thenInitStateIndex2);
	}
}
