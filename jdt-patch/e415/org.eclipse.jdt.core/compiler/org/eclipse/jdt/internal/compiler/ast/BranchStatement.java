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

import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public abstract class BranchStatement extends Statement {

	public char[] label;
	public BranchLabel targetLabel;
	public SubRoutineStatement[] subroutines;
	public int initStateIndex = -1;

/**
 * BranchStatement constructor comment.
 */
public BranchStatement(char[] label, int sourceStart,int sourceEnd) {
	this.label = label ;
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
}

protected void generateExpressionResultCode(BlockScope currentScope, CodeStream codeStream) {
	// do nothing here
}
protected void adjustStackSize(BlockScope currentScope, CodeStream codeStream) {
	// do nothing here
}
/**
 * Branch code generation
 *
 *   generate the finallyInvocationSequence.
 */
@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((this.bits & ASTNode.IsReachable) == 0) {
		return;
	}
	generateExpressionResultCode(currentScope, codeStream);
	int pc = codeStream.position;

	// generation of code responsible for invoking the finally
	// blocks in sequence
	if (this.subroutines != null){
		for (int i = 0, max = this.subroutines.length; i < max; i++){
			SubRoutineStatement sub = this.subroutines[i];
			boolean didEscape = sub.generateSubRoutineInvocation(currentScope, codeStream, this.targetLabel, this.initStateIndex, null);
			if (didEscape) {
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					SubRoutineStatement.reenterAllExceptionHandlers(this.subroutines, i, codeStream);
					if (this.initStateIndex != -1) {
						codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.initStateIndex);
						codeStream.addDefinitelyAssignedVariables(currentScope, this.initStateIndex);
					}
					return;
			}
		}
	}
	codeStream.goto_(this.targetLabel);
	adjustStackSize(currentScope, codeStream);
	codeStream.recordPositionsFrom(pc, this.sourceStart);
	SubRoutineStatement.reenterAllExceptionHandlers(this.subroutines, -1, codeStream);
	if (this.initStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.initStateIndex);
		codeStream.addDefinitelyAssignedVariables(currentScope, this.initStateIndex);
	}
}

@Override
public void resolve(BlockScope scope) {
	// nothing to do during name resolution
}
}
