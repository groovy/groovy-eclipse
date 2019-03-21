/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.codegen;

public class DoubleCache {
	private double keyTable[];
	private int valueTable[];
	private int elementSize;
/**
 * Constructs a new, empty hashtable. A default capacity and
 * load factor is used. Note that the hashtable will automatically
 * grow when it gets full.
 */
public DoubleCache() {
	this(13);
}
/**
 * Constructs a new, empty hashtable with the specified initial
 * capacity.
 * @param initialCapacity int
 *  the initial number of buckets
 */
public DoubleCache(int initialCapacity) {
	this.elementSize = 0;
	this.keyTable = new double[initialCapacity];
	this.valueTable = new int[initialCapacity];
}
/**
 * Clears the hash table so that it has no more elements in it.
 */
public void clear() {
	for (int i = this.keyTable.length; --i >= 0;) {
		this.keyTable[i] = 0.0;
		this.valueTable[i] = 0;
	}
	this.elementSize = 0;
}
/** Returns true if the collection contains an element for the key.
 *
 * @param key <CODE>double</CODE> the key that we are looking for
 * @return boolean
 */
public boolean containsKey(double key) {
	if (key == 0.0) {
		for (int i = 0, max = this.elementSize; i < max; i++) {
			if (this.keyTable[i] == 0.0) {
				long value1 = Double.doubleToLongBits(key);
				long value2 = Double.doubleToLongBits(this.keyTable[i]);
				if (value1 == -9223372036854775808L && value2 == -9223372036854775808L)
					return true;
				if (value1 == 0 && value2 == 0)
					return true;
			}
		}
	} else {
		for (int i = 0, max = this.elementSize; i < max; i++) {
			if (this.keyTable[i] == key) {
				return true;
			}
		}
	}
	return false;
}
/**
 * Puts the specified element into the hashtable, using the specified
 * key.  The element may be retrieved by doing a get() with the same key.
 *
 * @param key <CODE>double</CODE> the specified key in the hashtable
 * @param value <CODE>int</CODE> the specified element
 * @return int value
 */
public int put(double key, int value) {
	if (this.elementSize == this.keyTable.length) {
		// resize
		System.arraycopy(this.keyTable, 0, (this.keyTable = new double[this.elementSize * 2]), 0, this.elementSize);
		System.arraycopy(this.valueTable, 0, (this.valueTable = new int[this.elementSize * 2]), 0, this.elementSize);
	}
	this.keyTable[this.elementSize] = key;
	this.valueTable[this.elementSize] = value;
	this.elementSize++;
	return value;
}
/**
 * Puts the specified element into the hashtable, using the specified
 * key.  The element may be retrieved by doing a get() with the same key.
 *
 * @param key <CODE>double</CODE> the specified key in the hashtable
 * @param value <CODE>int</CODE> the specified element
 * @return int value
 */
public int putIfAbsent(double key, int value) {
	if (key == 0.0) {
		for (int i = 0, max = this.elementSize; i < max; i++) {
			if (this.keyTable[i] == 0.0) {
				long value1 = Double.doubleToLongBits(key);
				long value2 = Double.doubleToLongBits(this.keyTable[i]);
				if (value1 == -9223372036854775808L && value2 == -9223372036854775808L)
					return this.valueTable[i];
				if (value1 == 0 && value2 == 0)
					return this.valueTable[i];
			}
		}
	} else {
		for (int i = 0, max = this.elementSize; i < max; i++) {
			if (this.keyTable[i] == key) {
				return this.valueTable[i];
			}
		}
	}
	if (this.elementSize == this.keyTable.length) {
		// resize
		System.arraycopy(this.keyTable, 0, (this.keyTable = new double[this.elementSize * 2]), 0, this.elementSize);
		System.arraycopy(this.valueTable, 0, (this.valueTable = new int[this.elementSize * 2]), 0, this.elementSize);
	}
	this.keyTable[this.elementSize] = key;
	this.valueTable[this.elementSize] = value;
	this.elementSize++;
	return -value; // negative when added, assumes value is > 0
}
/**
 * Converts to a rather lengthy String.
 *
 * @return String the ascii representation of the receiver
 */
public String toString() {
	int max = this.elementSize;
	StringBuffer buf = new StringBuffer();
	buf.append("{"); //$NON-NLS-1$
	for (int i = 0; i < max; ++i) {
		if ((this.keyTable[i] != 0) || ((this.keyTable[i] == 0) &&(this.valueTable[i] != 0))) {
			buf.append(this.keyTable[i]).append("->").append(this.valueTable[i]); //$NON-NLS-1$
		}
		if (i < max) {
			buf.append(", "); //$NON-NLS-1$
		}
	}
	buf.append("}"); //$NON-NLS-1$
	return buf.toString();
}
}
