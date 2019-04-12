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
 *     Stephan Herrmann - Contribution for
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class BreakStatement extends BranchStatement {

	public Expression expression;
	public SwitchExpression switchExpression;
	public boolean isImplicit;

public BreakStatement(char[] label, int sourceStart, int e) {
	super(label, sourceStart, e);
}
@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	// here requires to generate a sequence of finally blocks invocations depending corresponding
	// to each of the traversed try statements, so that execution will terminate properly.

	// lookup the label, this should answer the returnContext
	FlowContext targetContext = (this.label == null)
		? flowContext.getTargetContextForDefaultBreak()
		: flowContext.getTargetContextForBreakLabel(this.label);

	if (targetContext == null) {
		if (this.label == null) {
			currentScope.problemReporter().invalidBreak(this);
		} else {
			if (this.switchExpression == null)
				currentScope.problemReporter().undefinedLabel(this);
		}
		return flowInfo; // pretend it did not break since no actual target
	}

	if (this.switchExpression != null && this.expression != null) {
		flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo);
		this.expression.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
		if (flowInfo.reachMode() == FlowInfo.REACHABLE && currentScope.compilerOptions().isAnnotationBasedNullAnalysisEnabled)
			checkAgainstNullAnnotation(currentScope, flowContext, flowInfo, this.expression);
	}

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
				TryStatement tryStatement = (TryStatement) node;
				flowInfo.addInitializationsFrom(tryStatement.subRoutineInits); // collect inits
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
protected void generateExpressionResultCode(BlockScope currentScope, CodeStream codeStream) {
	if (this.label == null && this.expression != null) {
		this.expression.generateCode(currentScope, codeStream, this.switchExpression != null);
	}
}
@Override
protected void adjustStackSize(BlockScope currentScope, CodeStream codeStream) {
	if (this.label == null && this.expression != null && this.switchExpression != null) {
		TypeBinding postConversionType = this.expression.postConversionType(currentScope);
		switch(postConversionType.id) {
			case TypeIds.T_long :
			case TypeIds.T_double :
				codeStream.decrStackSize(2);
				break;
			case TypeIds.T_void :
				break;
			default :
				codeStream.decrStackSize(1);
				break;
		}
	}
}
@Override
public void resolve(BlockScope scope) {
	super.resolve(scope);
	if  (this.expression != null && (this.switchExpression != null || this.isImplicit)) {
		if (this.switchExpression == null && this.isImplicit && !this.expression.statementExpression()) {
			if (scope.compilerOptions().enablePreviewFeatures) {
				/* JLS 12 14.11.2
				Switch labeled rules in switch statements differ from those in switch expressions (15.28).
				In switch statements they must be switch labeled statement expressions, ... */
				scope.problemReporter().invalidExpressionAsStatement(this.expression);
				return;
			}
		}
		this.expression.resolveType(scope);
	} else if (this.expression == null && this.switchExpression != null) {
		scope.problemReporter().switchExpressionBreakMissingValue(this);
	}
}

@Override
public TypeBinding resolveExpressionType(BlockScope scope) {
	return this.expression != null ? this.expression.resolveType(scope) : null;
}

@Override
public StringBuffer printStatement(int tab, StringBuffer output) {
	if (!this.isImplicit) // implicit for SwitchLabeledExpressions
		printIndent(tab, output).append("break"); //$NON-NLS-1$
	if (this.label != null) 
		output.append(' ').append(this.label);
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
