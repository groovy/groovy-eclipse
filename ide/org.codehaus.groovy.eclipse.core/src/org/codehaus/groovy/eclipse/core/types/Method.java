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

import org.eclipse.jdt.core.Signature;

public class Method extends Member implements Comparable {
	public static final Parameter[] NO_PARAMETERS = new Parameter[0];

	private final String returnType;

	private final Parameter[] parameters;

	public Method(int modifiers, String name, Parameter[] parameters, String returnType, ClassType declaringClass) {
		this(modifiers, name, parameters, returnType, declaringClass, false);
	}
	
	public Method(int modifiers, String name, Parameter[] parameters, String returnType, ClassType declaringClass, boolean inferred) {
		super(Signature.createMethodSignature(collectParameterSignatures(parameters), returnType),
				modifiers, name, declaringClass, inferred);
		this.parameters = parameters;
		this.returnType = returnType;
		
		for (int i = 0; i < parameters.length; i++) {
			parameters[i].method = this;
		}
	}
	
	private static String[] collectParameterSignatures(Parameter[] parameters) {
		String[] results = new String[parameters.length];
		for (int i = 0; i < parameters.length; ++i) {
			results[i] = parameters[i].getSignature();
		}
		return results;
	}

	public String getReturnType() {
		return returnType;
	}
	
	public Parameter[] getParameters() {
		return parameters;
	}

	@Override
    public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		try {
			// Don't call super - it checks type which is the return type for Method and not taken into account here.
			Method method = (Method) obj;
			if (!name.equals(method.name) || !returnType.equals(method.returnType) || modifiers != method.modifiers) {
				return false;
			}

			if (!declaringClass.equals(method.declaringClass)) {
				return false;
			}

			if (parameters.length != method.parameters.length) {
				return false;
			}

			for (int i = 0; i < parameters.length; ++i) {
				if (!parameters[i].equals(method.parameters[i])) {
					return false;
				}
			}
		} catch (ClassCastException e) {
			return false;
		}
		return true;
	}

	@Override
    public int hashCode() {
		int code = super.hashCode();
		for (int i = 0; i < parameters.length; ++i) {
			code += parameters.hashCode();
		}
		return code;
	}

	@Override
    @SuppressWarnings("unchecked")
    public int compareTo(Object arg) {
		Method method = (Method) arg;
		int value = name.compareTo(method.name);
		if (value != 0) {
			return value;
		}

		value = parameters.length - method.parameters.length;

		if (value != 0) {
			return value;
		}

		// Note: at some stage with a decent type lookup database, the type resolution below will be avoided.
		try {
			// If related, figure out which is super class of other.
			Class cls = Class.forName(declaringClass.signature);
			Class clsOther = Class.forName(method.declaringClass.signature);
			if (cls.equals(clsOther)) {
				// Same parameters, same declaring class, same name - the same.
				// How can this be?
				if (parameters.length == method.parameters.length) {
					// TODO: emp - this seems like a bug, equal declaring classes?
					return 0;
				}
				// Just compare by first arg type alphabetically.
				return parameters[0].compareTo(method.parameters[0]);
			} else if (cls.isAssignableFrom(clsOther)) {
				// If cls is a super class, this it goes first.
				return 1;
			} else if (clsOther.isAssignableFrom(cls)) {
				// Else the other way around.
				return -1;
			} else {
				// Different unrelated classes. Compare by declaring type name.
				return declaringClass.compareTo(method.declaringClass);
			}
		} catch (ClassNotFoundException e) {
			// Ignore - assume "equal" which is good enough for display purposes.
			// For hashing
		}

		return 0;
	}

	@Override
    public String toString() {
		return "Method:" + name + "(" + getParameterString() + ")" + returnType + " - " + declaringClass;
	}

	private String getParameterString() {
		if (parameters.length == 0) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < parameters.length; ++i) {
			sb.append(parameters[i].getType()).append(' ').append(parameters[i].getName()).append(',');
		}

		return sb.substring(0, sb.length() - 1);
	}
}