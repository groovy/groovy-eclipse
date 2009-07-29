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
package org.eclipse.jdt.internal.compiler.util;

import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Hashtable of {char[] --> int}
 */
public final class HashtableOfIntValues implements Cloneable {
	
	public static final int NO_VALUE = Integer.MIN_VALUE;
	
	// to avoid using Enumerations, walk the individual tables skipping nulls
	public char[] keyTable[];
	public int valueTable[];

	public int elementSize; // number of elements in the table
	int threshold;

	public HashtableOfIntValues() {
		this(13);
	}

	public HashtableOfIntValues(int size) {

		this.elementSize = 0;
		this.threshold = size; // size represents the expected number of elements
		int extraRoom = (int) (size * 1.75f);
		if (this.threshold == extraRoom)
			extraRoom++;
		this.keyTable = new char[extraRoom][];
		this.valueTable = new int[extraRoom];
	}

	public Object clone() throws CloneNotSupportedException {
		HashtableOfIntValues result = (HashtableOfIntValues) super.clone();
		result.elementSize = this.elementSize;
		result.threshold = this.threshold;

		int length = this.keyTable.length;
		result.keyTable = new char[length][];
		System.arraycopy(this.keyTable, 0, result.keyTable, 0, length);

		length = this.valueTable.length;
		result.valueTable = new int[length];
		System.arraycopy(this.valueTable, 0, result.valueTable, 0, length);
		return result;
	}

	public boolean containsKey(char[] key) {
		int length = keyTable.length, 
			index = CharOperation.hashCode(key) % length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
				return true;
			if (++index == length) {
				index = 0;
			}
		}
		return false;
	}

	public int get(char[] key) {
		int length = keyTable.length, 
			index = CharOperation.hashCode(key) % length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
				return valueTable[index];
			if (++index == length) {
				index = 0;
			}
		}
		return NO_VALUE;
	}

	public int put(char[] key, int value) {
		int length = keyTable.length, 
			index = CharOperation.hashCode(key) % length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
				return valueTable[index] = value;
			if (++index == length) {
				index = 0;
			}
		}
		keyTable[index] = key;
		valueTable[index] = value;

		// assumes the threshold is never equal to the size of the table
		if (++elementSize > threshold)
			rehash();
		return value;
	}

	public int removeKey(char[] key) {
		int length = keyTable.length, 
			index = CharOperation.hashCode(key) % length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key)) {
				int value = valueTable[index];
				elementSize--;
				keyTable[index] = null;
				valueTable[index] = NO_VALUE;
				rehash();
				return value;
			}
			if (++index == length) {
				index = 0;
			}
		}
		return NO_VALUE;
	}

	private void rehash() {

		HashtableOfIntValues newHashtable = new HashtableOfIntValues(elementSize * 2);		// double the number of expected elements
		char[] currentKey;
		for (int i = keyTable.length; --i >= 0;)
			if ((currentKey = keyTable[i]) != null)
				newHashtable.put(currentKey, valueTable[i]);

		this.keyTable = newHashtable.keyTable;
		this.valueTable = newHashtable.valueTable;
		this.threshold = newHashtable.threshold;
	}

	public int size() {
		return elementSize;
	}

	public String toString() {
		String s = ""; //$NON-NLS-1$
		char[] key;
		for (int i = 0, length = valueTable.length; i < length; i++)
			if ((key = keyTable[i]) != null)
				s += new String(key) + " -> " + valueTable[i] + "\n"; 	//$NON-NLS-2$ //$NON-NLS-1$
		return s;
	}
}
