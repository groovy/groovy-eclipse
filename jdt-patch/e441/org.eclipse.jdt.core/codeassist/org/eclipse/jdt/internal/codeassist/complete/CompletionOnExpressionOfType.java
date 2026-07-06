/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CompletionOnExpressionOfType extends FieldReference implements CompletionNode {
    public Expression methodCall;

    public CompletionOnExpressionOfType(Expression methodCall, char[] completionToken, long pos) {
        super(completionToken, pos);
        this.methodCall = methodCall;
        this.receiver = methodCall; // The method call is the receiver
    }

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		output.append("<CompleteOnExpressionOfType:"); //$NON-NLS-1$
		return super.printExpression(0, output).append('>');
	}

    @Override
    public TypeBinding resolveType(BlockScope scope) {
    	// Resolve the method call to get its return type
    	TypeBinding returnType = null;
    	if (this.methodCall instanceof MessageSend messageSend && messageSend.binding != null) {
    		returnType = messageSend.binding.declaringClass;
    	} else {
    		returnType = this.methodCall.resolveType(scope);
    	}

    	if (returnType == null || !returnType.isValidBinding() || returnType.isPrimitiveType()) {
			throw new CompletionNodeFound();
		}

		// Throw completion with the return type as the receiver type
		throw new CompletionNodeFound(this, returnType, scope);
    }
}

