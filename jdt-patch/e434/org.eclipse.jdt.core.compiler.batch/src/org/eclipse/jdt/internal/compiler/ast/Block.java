/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

public class Block extends Statement {

	public Statement[] statements;
	public int explicitDeclarations;
	// the number of explicit declaration , used to create scope
	public BlockScope scope;

public Block(int explicitDeclarations) {
	this.explicitDeclarations = explicitDeclarations;
}

@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// empty block
	if (this.statements == null)	return flowInfo;
	int complaintLevel = (flowInfo.reachMode() & FlowInfo.UNREACHABLE) != 0 ? Statement.COMPLAINED_FAKE_REACHABLE : Statement.NOT_COMPLAINED;
	CompilerOptions compilerOptions = currentScope.compilerOptions();
	boolean enableSyntacticNullAnalysisForFields = compilerOptions.enableSyntacticNullAnalysisForFields;
	for (Statement stat : this.statements) {
		if ((complaintLevel = stat.complainIfUnreachable(flowInfo, this.scope, complaintLevel, true)) < Statement.COMPLAINED_UNREACHABLE) {
			flowInfo = stat.analyseCode(this.scope, flowContext, flowInfo);
		}
		// record the effect of stat on the finally block of an enclosing try-finally, if any:
		flowContext.mergeFinallyNullInfo(flowInfo);
		if (enableSyntacticNullAnalysisForFields) {
			flowContext.expireNullCheckedFieldInfo();
		}
		if (compilerOptions.analyseResourceLeaks) {
			FakedTrackingVariable.cleanUpUnassigned(this.scope, stat, flowInfo, false);
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
@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((this.bits & IsReachable) == 0) {
		return;
	}
	int pc = codeStream.position;
	if (this.statements != null) {
		for (Statement stmt : this.statements) {
			stmt.generateCode(this.scope, codeStream);
		}
	} // for local variable debug attributes
	if (this.scope != currentScope) { // was really associated with its own scope
		codeStream.exitUserScope(this.scope);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

@Override
public boolean isEmptyBlock() {
	return this.statements == null;
}

public StringBuilder printBody(int indent, StringBuilder output) {
	if (this.statements == null) return output;
	for (Statement statement : this.statements) {
		statement.printStatement(indent + 1, output);
		output.append('\n');
	}
	return output;
}

@Override
public StringBuilder printStatement(int indent, StringBuilder output) {
	printIndent(indent, output);
	output.append("{\n"); //$NON-NLS-1$
	printBody(indent, output);
	return printIndent(indent, output).append('}');
}

@Override
public void resolve(BlockScope upperScope) {
	if ((this.bits & UndocumentedEmptyBlock) != 0) {
		upperScope.problemReporter().undocumentedEmptyBlock(this.sourceStart, this.sourceEnd);
	}
	if (this.statements != null) {
		this.scope =
			this.explicitDeclarations == 0
				? upperScope
				: new BlockScope(upperScope, this.explicitDeclarations);
		resolveStatements(this.statements, this.scope);
	}
}

public void resolveUsing(BlockScope givenScope) {
	if ((this.bits & UndocumentedEmptyBlock) != 0) {
		givenScope.problemReporter().undocumentedEmptyBlock(this.sourceStart, this.sourceEnd);
	}
	// this optimized resolve(...) is sent only on none empty blocks
	this.scope = givenScope;
	if (this.statements != null) {
		resolveStatements(this.statements, this.scope);
	}
}

@Override
public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		if (this.statements != null) {
			for (Statement statement : this.statements)
				statement.traverse(visitor, this.scope);
		}
	}
	visitor.endVisit(this, blockScope);
}

/**
 * Dispatch the call on its last statement.
 */
@Override
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

@Override
public boolean canCompleteNormally() {
	int length = this.statements == null ? 0 : this.statements.length;
	return length == 0 || this.statements[length - 1].canCompleteNormally();
}

@Override
public boolean continueCompletes() {
	int length = this.statements == null ? 0 : this.statements.length;
	return length > 0 && this.statements[length - 1].continueCompletes();
}

}
