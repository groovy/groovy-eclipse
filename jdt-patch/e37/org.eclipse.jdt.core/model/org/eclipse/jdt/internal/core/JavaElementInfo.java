/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;

/**
 * Holds cached structure and properties for a Java element.
 * Subclassed to carry properties for specific kinds of elements.
 */
public class JavaElementInfo implements Cloneable {

	/**
	 * Shared empty collection used for efficiency.
	 */
	static Object[] NO_NON_JAVA_RESOURCES = new Object[] {};

	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new Error();
		}
	}
	public IJavaElement[] getChildren() {
		return JavaElement.NO_ELEMENTS;
	}
}
