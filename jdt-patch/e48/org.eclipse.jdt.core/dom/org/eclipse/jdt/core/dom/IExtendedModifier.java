/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

/**
 * Common interface for AST nodes that represent modifiers or
 * annotations.
 * <pre>
 * IExtendedModifier:
 *   Modifier
 *   Annotation
 * </pre>
 * @since 3.1
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IExtendedModifier {

	/**
	 * Returns whether this extended modifier is a standard modifier.
	 *
	 * @return <code>true</code> if this is a standard modifier
	 * (instance of {@link Modifier}), and <code>false</code> otherwise
	 */
	public boolean isModifier();

	/**
	 * Returns whether this extended modifier is an annotation.
	 *
	 * @return <code>true</code> if this is an annotation
	 * (instance of a subclass of {@link Annotation}), and
	 * <code>false</code> otherwise
	 */
	public boolean isAnnotation();
}

