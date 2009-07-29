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
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class InitializationFlowContext extends ExceptionHandlingFlowContext {

	public int exceptionCount;
	public TypeBinding[] thrownExceptions = new TypeBinding[5];
	public ASTNode[] exceptionThrowers = new ASTNode[5];
	public FlowInfo[] exceptionThrowerFlowInfos = new FlowInfo[5];
	
	public InitializationFlowContext(
		FlowContext parent,
		ASTNode associatedNode,
		BlockScope scope) {
		super(
			parent,
			associatedNode,
			Binding.NO_EXCEPTIONS, // no exception allowed by default
			scope, 
			FlowInfo.DEAD_END);
	}

	public void checkInitializerExceptions(
		BlockScope currentScope,
		FlowContext initializerContext,
		FlowInfo flowInfo) {
		for (int i = 0; i < exceptionCount; i++) {
			initializerContext.checkExceptionHandlers(
				thrownExceptions[i],
				exceptionThrowers[i],
				exceptionThrowerFlowInfos[i],
				currentScope);
		}
	}

	public String individualToString() {
		
		StringBuffer buffer = new StringBuffer("Initialization flow context"); //$NON-NLS-1$
		for (int i = 0; i < exceptionCount; i++) {
			buffer.append('[').append(thrownExceptions[i].readableName());
			buffer.append('-').append(exceptionThrowerFlowInfos[i].toString()).append(']');
		}
		return buffer.toString();
	}
	
	public void recordHandlingException(
		ReferenceBinding exceptionType,
		UnconditionalFlowInfo flowInfo,
		TypeBinding raisedException,
		ASTNode invocationSite,
		boolean wasMasked) {
			
		// even if unreachable code, need to perform unhandled exception diagnosis
		int size = thrownExceptions.length;
		if (exceptionCount == size) {
			System.arraycopy(
				thrownExceptions,
				0,
				(thrownExceptions = new TypeBinding[size * 2]),
				0,
				size);
			System.arraycopy(
				exceptionThrowers,
				0,
				(exceptionThrowers = new ASTNode[size * 2]),
				0,
				size);
			System.arraycopy(
				exceptionThrowerFlowInfos,
				0,
				(exceptionThrowerFlowInfos = new FlowInfo[size * 2]),
				0,
				size);
		}
		thrownExceptions[exceptionCount] = raisedException;
		exceptionThrowers[exceptionCount] = invocationSite;
		exceptionThrowerFlowInfos[exceptionCount++] = flowInfo.copy();
	}	
}
