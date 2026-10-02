/*******************************************************************************
 * Copyright (c) , 2024 IBM Corporation and others.
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
 *     Advantest R & D - Switch Expressions 2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InsideStatementWithFinallyBlockFlowContext;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class YieldStatement extends BranchStatement {

	public Expression expression;
	public SwitchExpression switchExpression;
	public boolean isImplicit;

public YieldStatement(Expression expression, boolean isImplicit, int sourceStart, int sourceEnd) {
	super(null, sourceStart, sourceEnd);
	this.expression = expression;
	this.isImplicit = isImplicit;
}

@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	/* Lookup target context: since an implicit yield immediately follows '->', it cannot be wrapped by another context,
	   so the immediate breakable context is guaranteed to be the one intended, while explicit yield should move up the parents
	   to the immediate enclosing switch **expression**
	*/
	FlowContext targetContext = flowContext.getTargetContextForYield(this.isImplicit);

	flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo);
	this.expression.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);

	targetContext.recordAbruptExit();
	targetContext.expireNullCheckedFieldInfo();

	this.initStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);

	this.targetLabel = targetContext.breakLabel();
	FlowContext traversedContext = flowContext;

	int finallys = 0;
	this.statementsWithFinallyBlock = new StatementWithFinallyBlock[5];
	do {
		StatementWithFinallyBlock stmt;
		if ((stmt = traversedContext.statementWithFinallyBlock()) != null) {
			if (finallys == this.statementsWithFinallyBlock.length)
				System.arraycopy(this.statementsWithFinallyBlock, 0, (this.statementsWithFinallyBlock = new StatementWithFinallyBlock[finallys*2]), 0, finallys); // grow
			this.statementsWithFinallyBlock[finallys++] = stmt;
			if (stmt.isFinallyBlockEscaping()) {
				this.bits |= ASTNode.IsAnyFinallyBlockEscaping;
				break;
			}
		}
		traversedContext.recordReturnFrom(flowInfo.unconditionalInits());
		traversedContext.recordBreakTo(targetContext);

		if (traversedContext instanceof InsideStatementWithFinallyBlockFlowContext) {
			if (traversedContext.associatedNode instanceof TryStatement ts)
				flowInfo.addInitializationsFrom(ts.finallyBlockInits); // collect inits
		} else if (traversedContext == targetContext) {
			targetContext.recordBreakFrom(flowInfo);
			break;
		}
	} while ((traversedContext = traversedContext.getLocalParent()) != null);

	if (finallys != this.statementsWithFinallyBlock.length)
		System.arraycopy(this.statementsWithFinallyBlock, 0, (this.statementsWithFinallyBlock = new StatementWithFinallyBlock[finallys]), 0, finallys);

	return FlowInfo.DEAD_END;
}

@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream) {

	if ((this.bits & ASTNode.IsReachable) == 0)
		return;

	if (this.switchExpression != null)
		this.switchExpression.refillOperandStackIfNeeded(codeStream, this);

	int pc = codeStream.position;
	boolean expressionGenerationDeferred = true; // If possible defer generating the expression to until after inlining of enclosing try's finally {}
	boolean valueRequired;
	if (this.expression.hasSideEffects() || this.statementsWithFinallyBlock.length == 0) { // can't defer or no need to defer
		expressionGenerationDeferred = false;
		valueRequired = this.switchExpression != null && (this.bits & ASTNode.IsAnyFinallyBlockEscaping) == 0; // no value needed if finally completes abruptly or for a statement switch
		this.expression.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired && codeStream.operandStack.peek() == TypeBinding.NULL)
			codeStream.operandStack.cast(this.switchExpression.resolvedType);
	} else
		codeStream.nop(); // prevent exception ranges from being empty on account of deferral : try { yield 42; } catch (Exception ex) {}  ...

	// inline finally blocks in sequence
	for (int i = 0, max = this.statementsWithFinallyBlock.length; i < max; i++) {
		StatementWithFinallyBlock stmt = this.statementsWithFinallyBlock[i];
		boolean didEscape = stmt.generateFinallyBlock(currentScope, codeStream, this.initStateIndex);
		if (didEscape) {
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			StatementWithFinallyBlock.reenterAllExceptionHandlers(this.statementsWithFinallyBlock, i, codeStream);
			if (this.initStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.initStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, this.initStateIndex);
			}
			return;
		}
	}

	if (expressionGenerationDeferred) {
		this.expression.generateCode(currentScope, codeStream, valueRequired = this.switchExpression != null);
		if (valueRequired && codeStream.operandStack.peek() == TypeBinding.NULL)
			codeStream.operandStack.cast(this.switchExpression.resolvedType);
	}

	codeStream.goto_(this.targetLabel);
	if (this.initStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.initStateIndex);
		codeStream.addDefinitelyAssignedVariables(currentScope, this.initStateIndex);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
	StatementWithFinallyBlock.reenterAllExceptionHandlers(this.statementsWithFinallyBlock, -1, codeStream);
}

@Override
public void resolve(BlockScope scope) {

	if (this.switchExpression == null) {
		this.switchExpression = enclosingSwitchExpression(scope);
		if (this.switchExpression != null) {
			this.expression.setExpressionContext(this.switchExpression.expressionContext); // result expressions feature in same context ...
			this.expression.setExpectedType(this.switchExpression.expectedType);           // ... with the same target type
		}
	}

	TypeBinding expressionType = this.expression.resolveType(scope);
	if (this.switchExpression != null)
		this.switchExpression.results.add(this.expression, expressionType);

	if (this.isImplicit) {
		if (this.switchExpression == null && !this.expression.statementExpression()) {
			scope.problemReporter().invalidExpressionAsStatement(this.expression);
		}
	} else if (this.switchExpression == null)
		scope.problemReporter().yieldOutsideSwitchExpression(this);
}

@Override
public StringBuilder printStatement(int tab, StringBuilder output) {
	if (this.isImplicit) {
		this.expression.print(tab, output);
	} else {
		printIndent(tab, output).append("yield "); //$NON-NLS-1$
		this.expression.printExpression(tab, output);
	}
	return output.append(';');
}

@Override
public void traverse(ASTVisitor visitor, BlockScope blockscope) {
	if (visitor.visit(this, blockscope)) {
		this.expression.traverse(visitor, blockscope);
	}
	visitor.endVisit(this, blockscope);
}

@Override
public boolean doesNotCompleteNormally() {
	return true;
}
}