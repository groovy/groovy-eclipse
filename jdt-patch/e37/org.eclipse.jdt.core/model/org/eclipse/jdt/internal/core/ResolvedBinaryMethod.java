/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

/**
 * Handle representing a binary method that is resolved.
 * The uniqueKey contains the genericSignature of the resolved method. Use BindingKey to decode it.
 */
public class ResolvedBinaryMethod extends BinaryMethod {

	private String uniqueKey;

	/*
	 * See class comments.
	 */
	public ResolvedBinaryMethod(JavaElement parent, String name, String[] parameterTypes, String uniqueKey) {
		super(parent, name, parameterTypes);
		this.uniqueKey = uniqueKey;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.BinaryMethod#getKey()
	 */
	public String getKey() {
		return this.uniqueKey;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#isResolved()
	 */
	public boolean isResolved() {
		return true;
	}

	/**
	 * @private Debugging purposes
	 */
	protected void toStringInfo(int tab, StringBuffer buffer, Object info, boolean showResolvedInfo) {
		super.toStringInfo(tab, buffer, info, showResolvedInfo);
		if (showResolvedInfo) {
			buffer.append(" {key="); //$NON-NLS-1$
			buffer.append(this.uniqueKey);
			buffer.append("}"); //$NON-NLS-1$
		}
	}

	public JavaElement unresolved() {
		SourceRefElement handle = new BinaryMethod(this.parent, this.name, this.parameterTypes);
		handle.occurrenceCount = this.occurrenceCount;
		return handle;
	}
}
