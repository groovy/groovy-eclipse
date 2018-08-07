/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java.model;

import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;

public class IndexBinaryNestedType implements IBinaryNestedType {
	private char[] enclosingTypeName;
	private char[] name;
	private int modifiers;

	public IndexBinaryNestedType(char[] name, char[] enclosingTypeName, int modifiers) {
		super();
		this.name = name;
		this.enclosingTypeName = enclosingTypeName;
		this.modifiers = modifiers;
	}

	@Override
	public char[] getEnclosingTypeName() {
		return this.enclosingTypeName;
	}

	@Override
	public int getModifiers() {
		return this.modifiers;
	}

	@Override
	public char[] getName() {
		return this.name;
	}

}
