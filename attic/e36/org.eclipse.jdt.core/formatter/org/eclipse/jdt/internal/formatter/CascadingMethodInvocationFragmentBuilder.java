/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import java.util.ArrayList;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

class CascadingMethodInvocationFragmentBuilder
	extends ASTVisitor {

	ArrayList fragmentsList;

	CascadingMethodInvocationFragmentBuilder() {
		this.fragmentsList = new ArrayList();
	}

	public MessageSend[] fragments() {
		MessageSend[] fragments = new MessageSend[this.fragmentsList.size()];
		this.fragmentsList.toArray(fragments);
		return fragments;
	}

	public int size() {
		return this.fragmentsList.size();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#visit(org.eclipse.jdt.internal.compiler.ast.MessageSend, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(MessageSend messageSend, BlockScope scope) {
		if ((messageSend.receiver.bits & ASTNode.ParenthesizedMASK) >> ASTNode.ParenthesizedSHIFT == 0) {
			if (messageSend.receiver instanceof MessageSend) {
				this.fragmentsList.add(0, messageSend);
				messageSend.receiver.traverse(this, scope);
				return false;
			}
			this.fragmentsList.add(0, messageSend);
			this.fragmentsList.add(1, messageSend);
		} else {
			this.fragmentsList.add(0, messageSend);
			this.fragmentsList.add(1, messageSend);
		}
		return false;
	}
}
