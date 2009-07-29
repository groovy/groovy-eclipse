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

/**
 * Type hierarchy - this is internal API for now. Changes are likely possible as things like DLTK are investigated.
 * 
 * @author empovazan
 */
public abstract class Type implements Modifiers, Comparable {
	public static final int CLASS = 1;

	public static final int MEMBER = 2;

	public static final int LOCAL_VARIABLE = 3;

	public static final int PARAMETER = 4;

	public static final int FIELD = 5;

	public static final int METHOD = 6;

	protected String signature;

	protected String name;

	protected int modifiers;

	private boolean inferred;

	public Type(String signature, int modifiers, String name) {
		this(signature, modifiers, name, false);
	}
	
	public Type(String signature, int modifiers, String name, boolean inferred) {
		this.signature = signature;
		this.name = name;
		this.modifiers = modifiers;
		this.inferred = inferred;
	}

	public abstract int getType();

	public boolean isGroovyType() {
		return true;
	}

	public String getSignature() {
		return signature;
	}

	public String getName() {
		return name;
	}

	public int getModifiers() {
		return modifiers;
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		try {
			Type rhs = (Type) obj;
			return getType() == rhs.getType() && isGroovyType() == rhs.isGroovyType() && name.equals(rhs.name)
					&& signature.equals(signature) && modifiers == rhs.modifiers;
		} catch (ClassCastException e) {
			return false;
		}
	}

	public int hashCode() {
		return name.hashCode() + signature.hashCode() + modifiers + (isGroovyType() ? 1 : 0);
	}

	public int compareTo(Object arg) {
		Type type = (Type) arg;
		return name.compareTo(type.name);
	}

	public String toString() {
		return "Type:" + name + " " + signature;
	}
	
	/**
	 * A type is inferred in two ways. Implicit, ie, it is already known. Or after type inference has determined a type.
	 * 
	 * @return
	 */

	public boolean isInferred() {
		return inferred;
	}
}