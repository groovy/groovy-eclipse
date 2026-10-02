/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for Bug 464615 - [dom] ASTParser.createBindings() ignores parameterization of a method invocation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.JavaModelException;

/**
 * Handle representing a binary type that is resolved.
 * The uniqueKey contains the genericTypeSignature of the resolved type. Use BindingKey to decode it.
 */
public class ResolvedBinaryType extends BinaryType {

	private final String uniqueKey;

	/*
	 * See class comments.
	 */
	public ResolvedBinaryType(JavaElement parent, String name, String uniqueKey) {
		super(parent, name);
		this.uniqueKey = uniqueKey;
	}
	public ResolvedBinaryType(JavaElement parent, String name, String uniqueKey, int occurrenceCount) {
		super(parent, name, occurrenceCount);
		this.uniqueKey = uniqueKey;
	}
	@Override
	public String getFullyQualifiedParameterizedName() throws JavaModelException {
		return getFullyQualifiedParameterizedName(getFullyQualifiedName('.'), this.uniqueKey);
	}

	@Override
	public String getKey() {
		return this.uniqueKey;
	}

	@Override
	public String getKey(boolean forceOpen) throws JavaModelException {
		return this.uniqueKey;
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	/**
	 * for debugging only
	 */
	@Override
	protected void toStringInfo(int tab, StringBuilder buffer, Object info, boolean showResolvedInfo) {
		super.toStringInfo(tab, buffer, info, showResolvedInfo);
		if (showResolvedInfo) {
			buffer.append(" {key="); //$NON-NLS-1$
			buffer.append(this.uniqueKey);
			buffer.append("}"); //$NON-NLS-1$
		}
	}

	@Override
	public BinaryType unresolved() {
		return new BinaryType(this.getParent(), this.name, this.getOccurrenceCount());
	}
}
