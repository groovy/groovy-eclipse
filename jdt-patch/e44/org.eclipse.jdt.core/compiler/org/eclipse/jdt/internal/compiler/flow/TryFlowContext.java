/*******************************************************************************
 * Copyright (c) 2013 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

/**
 * Shared implementation for try-statement-related flow contexts.
 */
public abstract class TryFlowContext extends FlowContext {

	/**
	 * For a try statement nested inside a finally block this reference
	 * points to the flow context of the outer try block, for access to its initsOnFinally.
	 */
	public FlowContext outerTryContext;

	public TryFlowContext(FlowContext parent, ASTNode associatedNode) {
		super(parent, associatedNode);
	}
	
	public void markFinallyNullStatus(LocalVariableBinding local, int nullStatus) {
		if (this.outerTryContext != null) {
			this.outerTryContext.markFinallyNullStatus(local, nullStatus);
		}
		super.markFinallyNullStatus(local, nullStatus);
	}

	public void mergeFinallyNullInfo(FlowInfo flowInfo) {
		if (this.outerTryContext != null) {
			this.outerTryContext.mergeFinallyNullInfo(flowInfo);
		}
		super.mergeFinallyNullInfo(flowInfo);
	}
}
