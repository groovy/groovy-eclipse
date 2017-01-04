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

/*
 * Element info for method from source.
 */
public class SourceMethodInfo extends SourceMethodElementInfo {

	/*
	 * Return type name for this method. The return type of
	 * constructors is equivalent to void.
	 */
	protected char[] returnType;

	public boolean isAnnotationMethod() {
		return false;
	}

	public boolean isConstructor() {
		return false;
	}

	public char[] getReturnTypeName() {
		return this.returnType;
	}

	protected void setReturnType(char[] type) {
		this.returnType = type;
	}

}
