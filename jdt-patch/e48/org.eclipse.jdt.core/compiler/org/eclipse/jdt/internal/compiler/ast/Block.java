/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *								bug 383368 - [compiler][null] syntactic null analysis for field references
 *								bug 402993 - [null] Follow up of bug 401088: Missing warning about redundant null check
 *								Bug 440282 - [resource] Resource leak detection false negative with empty finally block
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class Block extends Statement {

	public Statement[] statements;
	public int explicitDeclarations;
	// the number of explicit declaration , used to create scope
	public BlockScope scope;

public Block(int explicitDeclarations) {
	this.explicitDeclarations = explicitDeclarations;
}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// empty block
	if (this.statements == null)	return flowInfo;
	int complaintLevel = (flowInfo.reachMode() & FlowInfo.UNREACHABLE) != 0 ? Statement.COMPLAINED_FAKE_REACHABLE : Statement.NOT_COMPLAINED;
	boolean enableSyntacticNullAnalysisForFields = currentScope.compilerOptions().enableSyntacticNullAnalysisForFields;
	for (int i = 0, max = this.statements.length; i < max; i++) {
		Statement stat = this.statements[i];
		if ((complaintLevel = stat.complainIfUnreachable(flowInfo, this.scope, complaintLevel, true)) < Statement.COMPLAINED_UNREACHABLE) {
			flowInfo = stat.analyseCode(this.scope, flowContext, flowInfo);
		}
		// record the effect of stat on the finally block of an enclosing try-finally, if any:
		flowContext.mergeFinallyNullInfo(flowInfo);
		if (enableSyntacticNullAnalysisForFields) {
			flowContext.expireNullCheckedFieldInfo();
		}
	}
	if (this.scope != currentScope) {
		// if block is tracking any resources other than the enclosing 'currentScope', analyse them now:
		this.scope.checkUnclosedCloseables(flowInfo, flowContext, null, null);
	}
	if (this.explicitDeclarations > 0) {
		// cleanup assignment info for locals that are scoped to this block:
		LocalVariableBinding[] locals = this.scope.locals;
		if (locals != null) {
			int numLocals = this.scope.localIndex;
			for (int i = 0; i < numLocals; i++) {
				flowInfo.resetAssignmentInfo(locals[i]);
			}
		}
	}
	return flowInfo;
}
/**
 * Code generation for a block
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((this.bits & IsReachable) == 0) {
		return;
	}
	int pc = codeStream.position;
	if (this.statements != null) {
		for (int i = 0, max = this.statements.length; i < max; i++) {
			this.statements[i].generateCode(this.scope, codeStream);
		}
	} // for local variable debug attributes
	if (this.scope != currentScope) { // was really associated with its own scope
		codeStream.exitUserScope(this.scope);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

public boolean isEmptyBlock() {
	return this.statements == null;
}

public StringBuffer printBody(int indent, StringBuffer output) {
	if (this.statements == null) return output;
	for (int i = 0; i < this.statements.length; i++) {
		this.statements[i].printStatement(indent + 1, output);
		output.append('\n');
	}
	return output;
}

public StringBuffer printStatement(int indent, StringBuffer output) {
	printIndent(indent, output);
	output.append("{\n"); //$NON-NLS-1$
	printBody(indent, output);
	return printIndent(indent, output).append('}');
}

public void resolve(BlockScope upperScope) {
	if ((this.bits & UndocumentedEmptyBlock) != 0) {
		upperScope.problemReporter().undocumentedEmptyBlock(this.sourceStart, this.sourceEnd);
	}
	if (this.statements != null) {
		this.scope =
			this.explicitDeclarations == 0
				? upperScope
				: new BlockScope(upperScope, this.explicitDeclarations);
		for (int i = 0, length = this.statements.length; i < length; i++) {
			this.statements[i].resolve(this.scope);
		}
	}
}

public void resolveUsing(BlockScope givenScope) {
	if ((this.bits & UndocumentedEmptyBlock) != 0) {
		givenScope.problemReporter().undocumentedEmptyBlock(this.sourceStart, this.sourceEnd);
	}
	// this optimized resolve(...) is sent only on none empty blocks
	this.scope = givenScope;
	if (this.statements != null) {
		for (int i = 0, length = this.statements.length; i < length; i++) {
			this.statements[i].resolve(this.scope);
		}
	}
}

public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		if (this.statements != null) {
			for (int i = 0, length = this.statements.length; i < length; i++)
				this.statements[i].traverse(visitor, this.scope);
		}
	}
	visitor.endVisit(this, blockScope);
}

/**
 * Dispatch the call on its last statement.
 */
public void branchChainTo(BranchLabel label) {
	if (this.statements != null) {
		this.statements[this.statements.length - 1].branchChainTo(label);
	}
}

// A block does not complete normally if the last statement which we presume is reachable does not complete normally.
@Override
public boolean doesNotCompleteNormally() {
	int length = this.statements == null ? 0 : this.statements.length;
	return length > 0 && this.statements[length - 1].doesNotCompleteNormally();
}

@Override
public boolean completesByContinue() {
	int length = this.statements == null ? 0 : this.statements.length;
	return length > 0 && this.statements[length - 1].completesByContinue();
}
}
