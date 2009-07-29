/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

abstract public class ClassFileStruct {
	byte[] reference;
	int[] constantPoolOffsets;
	int structOffset;
public ClassFileStruct(byte[] classFileBytes, int[] offsets, int offset) {
	reference = classFileBytes;
	constantPoolOffsets = offsets;
	structOffset = offset;
}
public double doubleAt(int relativeOffset) {
	return (Double.longBitsToDouble(this.i8At(relativeOffset)));
}
public float floatAt(int relativeOffset) {
	return (Float.intBitsToFloat(this.i4At(relativeOffset)));
}
public int i4At(int relativeOffset) {
	int position = relativeOffset + structOffset;
	return ((reference[position++] & 0xFF) << 24) | ((reference[position++] & 0xFF) << 16) | ((reference[position++] & 0xFF) << 8) + (reference[position] & 0xFF);
}
public long i8At(int relativeOffset) {
	int position = relativeOffset + structOffset;
	return (((long) (reference[position++] & 0xFF)) << 56) 
					| (((long) (reference[position++] & 0xFF)) << 48) 
					| (((long) (reference[position++] & 0xFF)) << 40) 
					| (((long) (reference[position++] & 0xFF)) << 32) 
					| (((long) (reference[position++] & 0xFF)) << 24) 
					| (((long) (reference[position++] & 0xFF)) << 16) 
					| (((long) (reference[position++] & 0xFF)) << 8) 
					| (reference[position++] & 0xFF);
}
protected void reset() {
	this.reference = null;
	this.constantPoolOffsets = null;
}
public int u1At(int relativeOffset) {
	return (reference[relativeOffset + structOffset] & 0xFF);
}
public int u2At(int relativeOffset) {
	int position = relativeOffset + structOffset;
	return ((reference[position++] & 0xFF) << 8) | (reference[position] & 0xFF);
}
public long u4At(int relativeOffset) {
	int position = relativeOffset + structOffset;
	return (((reference[position++] & 0xFFL) << 24) | ((reference[position++] & 0xFF) << 16) | ((reference[position++] & 0xFF) << 8) | (reference[position] & 0xFF));
}
public char[] utf8At(int relativeOffset, int bytesAvailable) {
	int length = bytesAvailable;
	char outputBuf[] = new char[bytesAvailable];
	int outputPos = 0;
	int readOffset = this.structOffset + relativeOffset;
	
	while (length != 0) {
		int x = this.reference[readOffset++] & 0xFF;
		length--;
		if ((0x80 & x) != 0) {
			if ((x & 0x20) != 0) {
				length-=2;
				x = ((x & 0xF) << 12) | ((this.reference[readOffset++] & 0x3F) << 6) | (this.reference[readOffset++] & 0x3F);
			} else {
				length--;
				x = ((x & 0x1F) << 6) | (this.reference[readOffset++] & 0x3F);
			}
		}
		outputBuf[outputPos++] = (char) x;
	}

	if (outputPos != bytesAvailable) {
		System.arraycopy(outputBuf, 0, (outputBuf = new char[outputPos]), 0, outputPos);
	}
	return outputBuf;
}
}
