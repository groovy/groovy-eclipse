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

import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public abstract class BranchStatement extends Statement {

	public char[] label;
	public BranchLabel targetLabel;
	public StatementWithFinallyBlock[] statementsWithFinallyBlock;
	public int initStateIndex = -1;

/**
 * BranchStatement constructor comment.
 */
public BranchStatement(char[] label, int sourceStart,int sourceEnd) {
	this.label = label ;
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
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
	int pc = codeStream.position;

	// generation of code responsible for invoking the finally
	// blocks in sequence
	if (this.statementsWithFinallyBlock != null){
		for (int i = 0, max = this.statementsWithFinallyBlock.length; i < max; i++){
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
	}
//	checkAndLoadSyntheticVars(codeStream);
	codeStream.goto_(this.targetLabel);
	codeStream.recordPositionsFrom(pc, this.sourceStart);
	StatementWithFinallyBlock.reenterAllExceptionHandlers(this.statementsWithFinallyBlock, -1, codeStream);
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
