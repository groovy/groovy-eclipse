/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

/*
 * Element info for constructor from source.
 */
public class SourceConstructorInfo extends SourceMethodElementInfo {

	private static final char[] RETURN_TYPE_NAME = new char[]{'v', 'o','i', 'd'};

	@Override
	public boolean isAnnotationMethod() {
		// a constructor cannot be an annotation method
		return false;
	}

	@Override
	public boolean isConstructor() {
		return true;
	}

	@Override
	public char[] getReturnTypeName() {
		return RETURN_TYPE_NAME;
	}

	@Override
	protected void setReturnType(char[] type) {
		// ignore (always void)
	}

}
