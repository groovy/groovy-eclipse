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

public class ByteConstant extends Constant {
	private byte value;
	
public static Constant fromValue(byte value) {
	return new ByteConstant(value);
}
private ByteConstant(byte value) {
	this.value = value;
}
public byte byteValue() {
	return this.value;
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
	return value; // implicit cast to return type
}
public String stringValue() {
	//spec 15.17.11
	return String.valueOf(this.value) ;
}
public String toString(){

	return "(byte)" + value ; } //$NON-NLS-1$
public int typeID() {
	return T_byte;
}
}
