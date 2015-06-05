/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

/**
 * @deprecated Use {@link org.eclipse.jdt.core.CompletionRequestor} instead
 */
//TODO remove this class once no more clients
public interface IExtendedCompletionRequestor extends org.eclipse.jdt.core.ICompletionRequestor {
	void acceptPotentialMethodDeclaration(
			char[] declaringTypePackageName,
			char[] declaringTypeName,
			char[] selector,
			int completionStart,
			int completionEnd,
			int relevance);
}
