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

public class ShortConstant extends Constant {
private short value;

public static Constant fromValue(short value) {
	return new ShortConstant(value);
}
private ShortConstant(short value) {
	this.value = value;
}
public byte byteValue() {
	return (byte) value;
}
public char charValue() {
	return (char) value;
}
public double doubleValue() {
	return value; // implicit cast to return type
}
public float floatValue() {
	return value; // implicit cast to return type
}
public int intValue() {
	return value; // implicit cast to return type
}
public long longValue() {
	return value; // implicit cast to return type
}
public short shortValue() {
	return value;
}
public String stringValue() {
	//spec 15.17.11
	return String.valueOf(this.value);
}
public String toString(){

	return "(short)" + value ; } //$NON-NLS-1$
public int typeID() {
	return T_short;
}
}
