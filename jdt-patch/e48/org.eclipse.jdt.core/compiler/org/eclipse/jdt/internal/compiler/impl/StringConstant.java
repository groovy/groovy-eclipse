/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

public class StringConstant extends Constant {

	private String value;

	public static Constant fromValue(String value) {
		return new StringConstant(value);
	}

	private StringConstant(String value) {
		this.value = value;
	}

	@Override
	public String stringValue() {
		// spec 15.17.11

		// the next line do not go into the toString() send....!
		return this.value;
		/*
		 * String s = value.toString() ; if (s == null) return "null"; else return s;
		 */
	}

	@Override
	public String toString() {
		return "(String)\"" + this.value + "\""; //$NON-NLS-2$ //$NON-NLS-1$
	}

	@Override
	public int typeID() {
		return T_JavaLangString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StringConstant other = (StringConstant) obj;
		if (this.value == null) {
			return other.value == null;
		} else {
			return this.value.equals(other.value);
		}
	}
}
