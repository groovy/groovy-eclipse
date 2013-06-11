/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 384380 - False positive on a « Potential null pointer access » after a continue
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;

public interface InvocationSite {

	TypeBinding[] genericTypeArguments();
	boolean isSuperAccess();
	boolean isTypeAccess();
	// in case the receiver type does not match the actual receiver type
	// e.g. pkg.Type.C (receiver type of C is type of source context,
	//		but actual receiver type is pkg.Type)
	// e.g2. in presence of implicit access to enclosing type
	void setActualReceiverType(ReferenceBinding receiverType);
	void setDepth(int depth);
	void setFieldIndex(int depth);
	int sourceEnd();
	int sourceStart();
	TypeBinding expectedType();
	
	static class EmptyWithAstNode implements InvocationSite {
		ASTNode node;
		public EmptyWithAstNode(ASTNode node) {
			this.node = node;
		}
		public TypeBinding[] genericTypeArguments() { return null;}
		public boolean isSuperAccess() {return false;}
		public boolean isTypeAccess() {return false;}
		public void setActualReceiverType(ReferenceBinding receiverType) {/* empty */}
		public void setDepth(int depth) {/* empty */ }
		public void setFieldIndex(int depth) {/* empty */ }
		public int sourceEnd() {return this.node.sourceEnd; }
		public int sourceStart() {return this.node.sourceStart; }
		public TypeBinding expectedType() { return null; }
	}
}
