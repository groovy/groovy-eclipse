/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.internal.core.util.Util;

public class ClasspathAttribute implements IClasspathAttribute {

	private String name;
	private String value;

	public ClasspathAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ClasspathAttribute)) return false;
		ClasspathAttribute other = (ClasspathAttribute) obj;
		return this.name.equals(other.name) && this.value.equals(other.value);
	}

    public String getName() {
		return this.name;
    }

    public String getValue() {
		return this.value;
    }

    public int hashCode() {
     	return Util.combineHashCodes(this.name.hashCode(), this.value.hashCode());
    }

    public String toString() {
    	return this.name + "=" + this.value; //$NON-NLS-1$
    }

}
