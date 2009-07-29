/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.types;

public class Property extends Member {
	private boolean readable;

	private boolean writable;

	public Property(String signature, int modifiers, String name, boolean readable, boolean writable, ClassType declaringClass) {
		this(signature, modifiers, name, readable, writable, declaringClass, false);
	}
	
	public Property(String signature, int modifiers, String name, boolean readable, boolean writable,
			ClassType declaringClass, boolean inferred) {
		super(signature, modifiers, name, declaringClass, inferred);
		this.readable = readable;
		this.writable = writable;
	}

	public boolean isReadable() {
		return readable;
	}

	public boolean isWritable() {
		return writable;
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		try {
			Property property = (Property) obj;
			return super.equals(obj) && readable == property.readable && writable == property.writable;
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	public int hashCode() {
		int code = super.hashCode();
		code += readable ? 1 : -1;
		code += writable ? 1 : -1;
		return code;
	}

	public String toString() {
		return "Property:" + name + " " + signature + " - " + declaringClass;
	}
}
