/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.JavaModelException;

public class ResolvedLambdaExpression extends LambdaExpression {

	private String uniqueKey;
	LambdaExpression unresolved;

	public ResolvedLambdaExpression(JavaElement parent, LambdaExpression unresolved, String uniqueKey) {
		super(parent, unresolved.interphase, unresolved.sourceStart, unresolved.sourceEnd, unresolved.arrowPosition, unresolved.lambdaMethod);
		this.uniqueKey = uniqueKey;
		this.unresolved = unresolved;
	}

	public String getFullyQualifiedParameterizedName() throws JavaModelException {
		return getFullyQualifiedParameterizedName(getFullyQualifiedName('.'), this.uniqueKey);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.SourceType#getKey()
	 */
	public String getKey() {
		return this.uniqueKey;
	}

	@Override
	public boolean equals(Object o) {
		return this.unresolved.equals(o);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.SourceType#isResolved()
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
			buffer.append(this.getKey());
			buffer.append("}"); //$NON-NLS-1$
		}
	}

	public JavaElement unresolved() {
		return this.unresolved;
	}
}