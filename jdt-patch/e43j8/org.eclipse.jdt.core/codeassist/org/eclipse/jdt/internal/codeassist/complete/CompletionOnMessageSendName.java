/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CompletionOnMessageSendName extends MessageSend {
	public CompletionOnMessageSendName(char[] selector, int start, int end) {
		super();
		this.selector = selector;
		this.sourceStart = start;
		this.sourceEnd = end;
		this.nameSourcePosition = end;
	}

	public TypeBinding resolveType(BlockScope scope) {

		this.constant = Constant.NotAConstant;
		
		if (this.receiver.isImplicitThis())
			throw new CompletionNodeFound();

		this.actualReceiverType = this.receiver.resolveType(scope);
		if (this.actualReceiverType == null || this.actualReceiverType.isBaseType() || this.actualReceiverType.isArrayType())
			throw new CompletionNodeFound();

		// resolve type arguments
		if (this.typeArguments != null) {
			int length = this.typeArguments.length;
			this.genericTypeArguments = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				this.genericTypeArguments[i] = this.typeArguments[i].resolveType(scope, true /* check bounds*/);
			}
		}

		throw new CompletionNodeFound(this, this.actualReceiverType, scope);
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		output.append("<CompleteOnMessageSendName:"); //$NON-NLS-1$
		if (!this.receiver.isImplicitThis()) this.receiver.printExpression(0, output).append('.');
		if (this.typeArguments != null) {
			output.append('<');
			int max = this.typeArguments.length - 1;
			for (int j = 0; j < max; j++) {
				this.typeArguments[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			this.typeArguments[max].print(0, output);
			output.append('>');
		}
		output.append(this.selector).append('(');
		return output.append(")>"); //$NON-NLS-1$
	}
}
