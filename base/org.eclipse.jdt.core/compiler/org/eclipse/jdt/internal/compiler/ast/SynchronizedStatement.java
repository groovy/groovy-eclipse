/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SynchronizedStatement extends SubRoutineStatement {

	public Expression expression;
	public Block block;
	public BlockScope scope;
	public LocalVariableBinding synchroVariable;
	static final char[] SecretLocalDeclarationName = " syncValue".toCharArray(); //$NON-NLS-1$

	// for local variables table attributes
	int preSynchronizedInitStateIndex = -1;
	int mergedSynchronizedInitStateIndex = -1;

public SynchronizedStatement(
	Expression expression,
	Block statement,
	int s,
	int e) {

	this.expression = expression;
	this.block = statement;
	sourceEnd = e;
	sourceStart = s;
}

public FlowInfo analyseCode(
	BlockScope currentScope,
	FlowContext flowContext,
	FlowInfo flowInfo) {

	this.preSynchronizedInitStateIndex =
		currentScope.methodScope().recordInitializationStates(flowInfo);
    // TODO (philippe) shouldn't it be protected by a check whether reachable statement ?
    
	// mark the synthetic variable as being used
	synchroVariable.useFlag = LocalVariableBinding.USED;

	// simple propagation to subnodes
	flowInfo =
		block.analyseCode(
			scope,
			new InsideSubRoutineFlowContext(flowContext, this),
			expression.analyseCode(scope, flowContext, flowInfo));

	this.mergedSynchronizedInitStateIndex =
		currentScope.methodScope().recordInitializationStates(flowInfo);

	// optimizing code gen
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0) {
		this.bits |= ASTNode.BlockExit;
	}

	return flowInfo;
}

public boolean isSubRoutineEscaping() {
	return false;
}

/**
 * Synchronized statement code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((bits & IsReachable) == 0) {
		return;
	}
	// in case the labels needs to be reinitialized
	// when the code generation is restarted in wide mode
	this.anyExceptionLabel = null;

	int pc = codeStream.position;

	// generate the synchronization expression
	expression.generateCode(scope, codeStream, true);
	if (block.isEmptyBlock()) {
		if ((synchroVariable.type == TypeBinding.LONG)
			|| (synchroVariable.type == TypeBinding.DOUBLE)) {
			codeStream.dup2();
		} else {
			codeStream.dup();
		}
		// only take the lock
		codeStream.monitorenter();
		codeStream.monitorexit();
		if (scope != currentScope) {
			codeStream.exitUserScope(scope);
		}
	} else {
		// enter the monitor
		codeStream.store(synchroVariable, true);
		codeStream.addVariable(synchroVariable);
		codeStream.monitorenter();

		// generate  the body of the synchronized block
		this.enterAnyExceptionHandler(codeStream);
		block.generateCode(scope, codeStream);
		if (scope != currentScope) {
			// close all locals defined in the synchronized block except the secret local
			codeStream.exitUserScope(scope, synchroVariable);
		}

		BranchLabel endLabel = new BranchLabel(codeStream);
		if ((this.bits & ASTNode.BlockExit) == 0) {
			codeStream.load(synchroVariable);
			codeStream.monitorexit();
			this.exitAnyExceptionHandler();
			codeStream.goto_(endLabel);
			this.enterAnyExceptionHandler(codeStream);
		}
		// generate the body of the exception handler
		codeStream.pushExceptionOnStack(scope.getJavaLangThrowable());
		if (this.preSynchronizedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSynchronizedInitStateIndex);
		}
		this.placeAllAnyExceptionHandler();
		codeStream.load(synchroVariable);
		codeStream.monitorexit();
		this.exitAnyExceptionHandler();
		codeStream.athrow();
		// May loose some local variable initializations : affecting the local variable attributes
		if (this.mergedSynchronizedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedSynchronizedInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedSynchronizedInitStateIndex);
		}
		if (scope != currentScope) {
			codeStream.removeVariable(this.synchroVariable);
		}
		if ((this.bits & ASTNode.BlockExit) == 0) {
			endLabel.place();
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

/**
 * @see SubRoutineStatement#generateSubRoutineInvocation(BlockScope, CodeStream, Object, int, LocalVariableBinding)
 */
public boolean generateSubRoutineInvocation(BlockScope currentScope, CodeStream codeStream, Object targetLocation, int stateIndex, LocalVariableBinding secretLocal) {
	codeStream.load(this.synchroVariable);
	codeStream.monitorexit();
	exitAnyExceptionHandler();
	return false;
}

public void resolve(BlockScope upperScope) {
	// special scope for secret locals optimization.
	scope = new BlockScope(upperScope);
	TypeBinding type = expression.resolveType(scope);
	if (type == null)
		return;
	switch (type.id) {
		case T_boolean :
		case T_char :
		case T_float :
		case T_double :
		case T_byte :
		case T_short :
		case T_int :
		case T_long :
			scope.problemReporter().invalidTypeToSynchronize(expression, type);
			break;
		case T_void :
			scope.problemReporter().illegalVoidExpression(expression);
			break;
		case T_null :
			scope.problemReporter().invalidNullToSynchronize(expression);
			break; 
	}
	//continue even on errors in order to have the TC done into the statements
	synchroVariable = new LocalVariableBinding(SecretLocalDeclarationName, type, ClassFileConstants.AccDefault, false);
	scope.addLocalVariable(synchroVariable);
	synchroVariable.setConstant(Constant.NotAConstant); // not inlinable
	expression.computeConversion(scope, type, type);
	block.resolveUsing(scope);
}

public StringBuffer printStatement(int indent, StringBuffer output) {
	printIndent(indent, output);
	output.append("synchronized ("); //$NON-NLS-1$
	expression.printExpression(0, output).append(')');
	output.append('\n');
	return block.printStatement(indent + 1, output); 
}

public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		expression.traverse(visitor, scope);
		block.traverse(visitor, scope);
	}
	visitor.endVisit(this, blockScope);
}
}
