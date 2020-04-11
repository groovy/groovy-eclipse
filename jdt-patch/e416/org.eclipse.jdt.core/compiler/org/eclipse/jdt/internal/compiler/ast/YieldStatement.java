/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class YieldStatement extends BranchStatement {

	public Expression expression;
	public SwitchExpression switchExpression;
	public TryStatement tryStatement;
	/**
	 * @noreference This field is not intended to be referenced by clients.
	 */
	public boolean isImplicit;
	static final char[] SECRET_YIELD_RESULT_VALUE_NAME = " secretYieldValue".toCharArray(); //$NON-NLS-1$
	private LocalVariableBinding secretYieldResultValue = null;
	public BlockScope scope;

public YieldStatement(Expression exp, int sourceStart, int sourceEnd) {
	super(null, sourceStart, sourceEnd);
	this.expression = exp;
}
@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// this.switchExpression != null && this.expression != null true here.

	// here requires to generate a sequence of finally blocks invocations depending corresponding
	// to each of the traversed try statements, so that execution will terminate properly.


	// lookup the null label, this should answer the returnContext - for implicit yields, the nesting
	// doesn't occur since it immediately follow '->' and hence identical to default break - ie the
	// immediate breakable context is guaranteed to be the one intended;
	// while explicit yield should move up the parent to the switch expression.
	FlowContext targetContext = this.isImplicit ? flowContext.getTargetContextForDefaultBreak() :
		flowContext.getTargetContextForDefaultYield();

	flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo);
	this.expression.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
	if (flowInfo.reachMode() == FlowInfo.REACHABLE && currentScope.compilerOptions().isAnnotationBasedNullAnalysisEnabled)
		checkAgainstNullAnnotation(currentScope, flowContext, flowInfo, this.expression);

	targetContext.recordAbruptExit();
	targetContext.expireNullCheckedFieldInfo();

	this.initStateIndex =
		currentScope.methodScope().recordInitializationStates(flowInfo);

	this.targetLabel = targetContext.breakLabel();
	FlowContext traversedContext = flowContext;
	int subCount = 0;
	this.subroutines = new SubRoutineStatement[5];

	do {
		SubRoutineStatement sub;
		if ((sub = traversedContext.subroutine()) != null) {
			if (subCount == this.subroutines.length) {
				System.arraycopy(this.subroutines, 0, (this.subroutines = new SubRoutineStatement[subCount*2]), 0, subCount); // grow
			}
			this.subroutines[subCount++] = sub;
			if (sub.isSubRoutineEscaping()) {
				break;
			}
		}
		traversedContext.recordReturnFrom(flowInfo.unconditionalInits());
		traversedContext.recordBreakTo(targetContext);

		if (traversedContext instanceof InsideSubRoutineFlowContext) {
			ASTNode node = traversedContext.associatedNode;
			if (node instanceof TryStatement) {
				flowInfo.addInitializationsFrom(((TryStatement) node).subRoutineInits); // collect inits
			}
		} else if (traversedContext == targetContext) {
			// only record break info once accumulated through subroutines, and only against target context
			targetContext.recordBreakFrom(flowInfo);
			break;
		}
	} while ((traversedContext = traversedContext.getLocalParent()) != null);

	// resize subroutines
	if (subCount != this.subroutines.length) {
		System.arraycopy(this.subroutines, 0, (this.subroutines = new SubRoutineStatement[subCount]), 0, subCount);
	}
	return FlowInfo.DEAD_END;
}
@Override
protected void setSubroutineSwitchExpression(SubRoutineStatement sub) {
	sub.setSwitchExpression(this.switchExpression);
}
protected void addSecretYieldResultValue(BlockScope scope1) {
	SwitchExpression se = this.switchExpression;
	if (se == null || !se.containsTry)
		return;
	LocalVariableBinding local = new LocalVariableBinding(
			YieldStatement.SECRET_YIELD_RESULT_VALUE_NAME,
			se.resolvedType,
			ClassFileConstants.AccDefault,
			false);
	local.setConstant(Constant.NotAConstant);
	local.useFlag = LocalVariableBinding.USED;
	local.declaration = new LocalDeclaration(YieldStatement.SECRET_YIELD_RESULT_VALUE_NAME, 0, 0);
	assert se.yieldResolvedPosition >= 0;
	local.resolvedPosition = se.yieldResolvedPosition;
	assert local.resolvedPosition < this.scope.maxOffset;
	this.scope.addLocalVariable(local);
	this.secretYieldResultValue = local;
}

@Override
protected void restartExceptionLabels(CodeStream codeStream) {
	SubRoutineStatement.reenterAllExceptionHandlers(this.subroutines, -1, codeStream);
}
@Override
protected void generateExpressionResultCode(BlockScope currentScope, CodeStream codeStream) {
	SwitchExpression se = this.switchExpression;
	if (se != null && se.containsTry && se.resolvedType != null ) {
		addSecretYieldResultValue(this.scope);
		assert this.secretYieldResultValue != null;
		codeStream.record(this.secretYieldResultValue);
		SingleNameReference lhs = new SingleNameReference(this.secretYieldResultValue.name, 0);
		lhs.binding = this.secretYieldResultValue;
		lhs.bits &= ~ASTNode.RestrictiveFlagMASK;  // clear bits
		lhs.bits |= Binding.LOCAL;
		lhs.bits |= ASTNode.IsSecretYieldValueUsage;
		((LocalVariableBinding) lhs.binding).markReferenced(); // TODO : Can be skipped?
		Assignment assignment = new Assignment(lhs, this.expression, 0);
		assignment.generateCode(this.scope, codeStream);
		int l = this.subroutines == null ? 0 : this.subroutines.length;
		boolean foundFinally = false;
		if (l > 0) {
			for (int i = 0; i < l; ++i) {
				SubRoutineStatement srs = this.subroutines[i];
				srs.exitAnyExceptionHandler();
				srs.exitDeclaredExceptionHandlers(codeStream);
				if (srs instanceof TryStatement) {
					TryStatement ts = (TryStatement) srs;
					if (ts.finallyBlock != null) {
						foundFinally = true;
					}
				}
			}
		}
		if (!foundFinally) {
			 // no finally - TODO: Check for SynSta?
			se.loadStoredTypesAndKeep(codeStream);
			codeStream.load(this.secretYieldResultValue);
		}
		codeStream.removeVariable(this.secretYieldResultValue);
	} else {
		this.expression.generateCode(this.scope, codeStream, se != null);
	}
}
private boolean isInsideTry() {
	return this.switchExpression != null && this.switchExpression.containsTry;
}
@Override
public void resolve(BlockScope skope) {
	this.scope = isInsideTry() ? new BlockScope(skope) : skope;
	super.resolve(this.scope);
	if (this.expression == null) {
		return;

	}
	if (this.switchExpression != null || this.isImplicit) {
		if (this.switchExpression == null && this.isImplicit && !this.expression.statementExpression()) {
			if (this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK14) {
				/* JLS 13 14.11.2
				Switch labeled rules in switch statements differ from those in switch expressions (15.28).
				In switch statements they must be switch labeled statement expressions, ... */
				this.scope.problemReporter().invalidExpressionAsStatement(this.expression);
				return;
			}
		}
	} else {
		if (this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK14) {
			this.scope.problemReporter().switchExpressionsYieldOutsideSwitchExpression(this);
		}
	}
	this.expression.resolveType(this.scope);
}

@Override
public TypeBinding resolveExpressionType(BlockScope scope1) {
	return this.expression != null ? this.expression.resolveType(scope1) : null;
}

@Override
public StringBuffer printStatement(int tab, StringBuffer output) {
	if (!this.isImplicit)
		printIndent(tab, output).append("yield"); //$NON-NLS-1$
	if (this.expression != null) {
		output.append(' ');
		this.expression.printExpression(tab, output);
	}
	return output.append(';');
}

@Override
public void traverse(ASTVisitor visitor, BlockScope blockscope) {
	if (visitor.visit(this, blockscope)) {
		if (this.expression != null)
			this.expression.traverse(visitor, blockscope);
	}
	visitor.endVisit(this, blockscope);
}
@Override
public boolean doesNotCompleteNormally() {
	return true;
}
}
