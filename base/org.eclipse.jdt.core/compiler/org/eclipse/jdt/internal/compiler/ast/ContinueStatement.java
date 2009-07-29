/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ContinueStatement extends BranchStatement {

public ContinueStatement(char[] label, int sourceStart, int sourceEnd) {
	super(label, sourceStart, sourceEnd);
}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	// here requires to generate a sequence of finally blocks invocations depending corresponding
	// to each of the traversed try statements, so that execution will terminate properly.

	// lookup the label, this should answer the returnContext
	FlowContext targetContext = (label == null)
			? flowContext.getTargetContextForDefaultContinue()
			: flowContext.getTargetContextForContinueLabel(label);

	if (targetContext == null) {
		if (label == null) {
			currentScope.problemReporter().invalidContinue(this);
		} else {
			currentScope.problemReporter().undefinedLabel(this); 
		}
		return flowInfo; // pretend it did not continue since no actual target			
	} 

	if (targetContext == FlowContext.NotContinuableContext) {
		currentScope.problemReporter().invalidContinue(this);
		return flowInfo; // pretend it did not continue since no actual target
	}
	this.initStateIndex =
		currentScope.methodScope().recordInitializationStates(flowInfo);

	targetLabel = targetContext.continueLabel();
	FlowContext traversedContext = flowContext;
	int subCount = 0;
	subroutines = new SubRoutineStatement[5];

	do {
		SubRoutineStatement sub;
		if ((sub = traversedContext.subroutine()) != null) {
			if (subCount == subroutines.length) {
				System.arraycopy(subroutines, 0, subroutines = new SubRoutineStatement[subCount*2], 0, subCount); // grow
			}
			subroutines[subCount++] = sub;
			if (sub.isSubRoutineEscaping()) {
				break;
			}
		}
		traversedContext.recordReturnFrom(flowInfo.unconditionalInits());

		if (traversedContext instanceof InsideSubRoutineFlowContext) {
			ASTNode node = traversedContext.associatedNode;
			if (node instanceof TryStatement) {
				TryStatement tryStatement = (TryStatement) node;
				flowInfo.addInitializationsFrom(tryStatement.subRoutineInits); // collect inits			
			}		
		} else if (traversedContext == targetContext) {
			// only record continue info once accumulated through subroutines, and only against target context
			targetContext.recordContinueFrom(flowContext, flowInfo);
			break;
		}
	} while ((traversedContext = traversedContext.parent) != null);
	
	// resize subroutines
	if (subCount != subroutines.length) {
		System.arraycopy(subroutines, 0, subroutines = new SubRoutineStatement[subCount], 0, subCount);
	}
	return FlowInfo.DEAD_END;
}

public StringBuffer printStatement(int tab, StringBuffer output) {
	printIndent(tab, output).append("continue "); //$NON-NLS-1$
	if (label != null) output.append(label);
	return output.append(';');
}

public void traverse(ASTVisitor visitor, 	BlockScope blockScope) {
	visitor.visit(this, blockScope);
	visitor.endVisit(this, blockScope);
}
}
