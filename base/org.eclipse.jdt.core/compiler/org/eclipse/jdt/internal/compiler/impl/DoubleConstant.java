/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

public class DoubleConstant extends Constant {
	
	private double value;

	public static Constant fromValue(double value) {

		return new DoubleConstant(value);
	}

	private DoubleConstant(double value) {
		this.value = value;
	}
	
	public byte byteValue() {
		return (byte) value;
	}
	
	public char charValue() {
		return (char) value;
	}
	
	public double doubleValue() {
		return this.value;
	}
	
	public float floatValue() {
		return (float) value;
	}
	
	public int intValue() {
		return (int) value;
	}
	
	public long longValue() {
		return (long) value;
	}
	
	public short shortValue() {
		return (short) value;
	}
	
	public String stringValue() {
		return String.valueOf(this.value);
	}
	
	public String toString() {
		if (this == NotAConstant)
			return "(Constant) NotAConstant"; //$NON-NLS-1$
		return "(double)" + value;  //$NON-NLS-1$
	}

	public int typeID() {
		return T_double;
	}
}
