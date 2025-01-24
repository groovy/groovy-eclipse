/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.codegen;

public class FloatCache {
	private static final int[] EMPTY_INTS = new int[0];
	private static final float[] EMPTY_FLOATS = new float[0];
	private float keyTable[];
	private int valueTable[];
	private int elementSize;
/**
 * Constructs a new, empty hashtable. A default capacity and
 * load factor is used. Note that the hashtable will automatically
 * grow when it gets full.
 */
public FloatCache() {
	this.elementSize = 0;
	this.keyTable = EMPTY_FLOATS;
	this.valueTable = EMPTY_INTS;
}
/**
 * Constructs a new, empty hashtable with the specified initial
 * capacity.
 * @param initialCapacity int
 *  the initial number of buckets
 */
public FloatCache(int initialCapacity) {
	this.elementSize = 0;
	this.keyTable = new float[initialCapacity];
	this.valueTable = new int[initialCapacity];
}
/**
 * Clears the hash table so that it has no more elements in it.
 */
public void clear() {
	this.elementSize = 0;
	this.keyTable = EMPTY_FLOATS;
	this.valueTable = EMPTY_INTS;
}
/** Returns true if the collection contains an element for the key.
 *
 * @param key <CODE>float</CODE> the key that we are looking for
 * @return boolean
 */
public boolean containsKey(float key) {
	if (key == 0.0f) {
		for (int i = 0, max = this.elementSize; i < max; i++) {
			if (this.keyTable[i] == 0.0f) {
				int value1 = Float.floatToIntBits(key);
				int value2 = Float.floatToIntBits(this.keyTable[i]);
				if (value1 == -2147483648 && value2 == -2147483648)
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
 * @param key <CODE>float</CODE> the specified key in the hashtable
 * @param value <CODE>int</CODE> the specified element
 * @return int value
 */
public int putIfAbsent(float key, int value) {
	if (key == 0.0f) {
		// see Float#equals() +0.0 vs -0.0
		int value1 = Float.floatToIntBits(key);
		for (int i = 0, max = this.elementSize; i < max; i++) {
			if (this.keyTable[i] == 0.0f) {
				int value2 = Float.floatToIntBits(this.keyTable[i]);
				if (value1 == value2) {
					return this.valueTable[i];
				}
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
		int newSize = Math.max(13, this.elementSize * 2);
		System.arraycopy(this.keyTable, 0, (this.keyTable = new float[newSize]), 0, this.elementSize);
		System.arraycopy(this.valueTable, 0, (this.valueTable = new int[newSize]), 0, this.elementSize);
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
@Override
public String toString() {
	int max = this.elementSize;
	StringBuilder buf = new StringBuilder();
	buf.append("{"); //$NON-NLS-1$
	for (int i = 0; i < max; ++i) {
		if ((this.keyTable[i] != 0) || ((this.keyTable[i] == 0) && (this.valueTable[i] != 0))) {
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
