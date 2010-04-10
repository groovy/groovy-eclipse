/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
 * Hashtable of {char[] --> Object }
 */
public class HashtableOfObject implements Cloneable {

	// to avoid using Enumerations, walk the individual tables skipping nulls
	public char[] keyTable[];
	public Object valueTable[];

	public int elementSize; // number of elements in the table
	protected int threshold;

	public HashtableOfObject() {
		this(13);
	}

	public HashtableOfObject(int size) {

		this.elementSize = 0;
		this.threshold = size; // size represents the expected number of elements
		int extraRoom = (int) (size * 1.75f);
		if (this.threshold == extraRoom)
			extraRoom++;
		this.keyTable = new char[extraRoom][];
		this.valueTable = new Object[extraRoom];
	}

	public void clear() {
		for (int i = this.keyTable.length; --i >= 0;) {
			this.keyTable[i] = null;
			this.valueTable[i] = null;
		}
		this.elementSize = 0;
	}

	public Object clone() throws CloneNotSupportedException {
		HashtableOfObject result = (HashtableOfObject) super.clone();
		result.elementSize = this.elementSize;
		result.threshold = this.threshold;

		int length = this.keyTable.length;
		result.keyTable = new char[length][];
		System.arraycopy(this.keyTable, 0, result.keyTable, 0, length);

		length = this.valueTable.length;
		result.valueTable = new Object[length];
		System.arraycopy(this.valueTable, 0, result.valueTable, 0, length);
		return result;
	}

	public boolean containsKey(char[] key) {
		int length = this.keyTable.length,
			index = hashCode(key) % length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = this.keyTable[index]) != null) {
			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
				return true;
			if (++index == length) {
				index = 0;
			}
		}
		return false;
	}

	public Object get(char[] key) {
		int length = this.keyTable.length,
			index = hashCode(key) % length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = this.keyTable[index]) != null) {
			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
				return this.valueTable[index];
			if (++index == length) {
				index = 0;
			}
		}
		return null;
	}

	/*
	 * This method implementation is an inline of the {@link CharOperation#hashCode(char[])} method...
	 */
	protected int hashCode(char[] key) {
		int length = key.length;
		int hash = length == 0 ? 31 : key[0];
		if (length < 8) {
			for (int i = length; --i > 0;)
				hash = (hash * 31) + key[i];
		} else {
			// 8 characters is enough to compute a decent hash code, don't waste time examining every character
			for (int i = length - 1, last = i > 16 ? i - 16 : 0; i > last; i -= 2)
				hash = (hash * 31) + key[i];
		}
		return hash & 0x7FFFFFFF;
	}

	public Object put(char[] key, Object value) {
		int length = this.keyTable.length,
			index = hashCode(key) % length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = this.keyTable[index]) != null) {
			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
				return this.valueTable[index] = value;
			if (++index == length) {
				index = 0;
			}
		}
		this.keyTable[index] = key;
		this.valueTable[index] = value;

		// assumes the threshold is never equal to the size of the table
		if (++this.elementSize > this.threshold)
			rehash();
		return value;
	}

	/**
	 * Put a value at the index of the given using the local hash code computation.
	 * <p>
	 * Note that this is an unsafe put as there's no prior verification whether
	 * the given key already exists in the table or not.
	 * </p>
	 * @param key The key of the table entry
	 * @param value The value of the table entry
	 */
	public void putUnsafely(char[] key, Object value) {
		int length = this.keyTable.length,
			index = hashCode(key) % length;
		while (this.keyTable[index] != null) {
			if (++index == length) {
				index = 0;
			}
		}
		this.keyTable[index] = key;
		this.valueTable[index] = value;
	
		// assumes the threshold is never equal to the size of the table
		if (++this.elementSize > this.threshold) {
			rehash();
		}
	}

	public Object removeKey(char[] key) {
		int length = this.keyTable.length,
			index = hashCode(key) % length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = this.keyTable[index]) != null) {
			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key)) {
				Object value = this.valueTable[index];
				this.elementSize--;
				this.keyTable[index] = null;
				this.valueTable[index] = null;
				rehash();
				return value;
			}
			if (++index == length) {
				index = 0;
			}
		}
		return null;
	}

	protected void rehash() {

		HashtableOfObject newHashtable = new HashtableOfObject(this.elementSize * 2);		// double the number of expected elements
		char[] currentKey;
		for (int i = this.keyTable.length; --i >= 0;)
			if ((currentKey = this.keyTable[i]) != null)
				newHashtable.putUnsafely(currentKey, this.valueTable[i]);

		this.keyTable = newHashtable.keyTable;
		this.valueTable = newHashtable.valueTable;
		this.threshold = newHashtable.threshold;
	}

	public int size() {
		return this.elementSize;
	}

	public String toString() {
		String s = ""; //$NON-NLS-1$
		Object object;
		for (int i = 0, length = this.valueTable.length; i < length; i++)
			if ((object = this.valueTable[i]) != null)
				s += new String(this.keyTable[i]) + " -> " + object.toString() + "\n"; 	//$NON-NLS-2$ //$NON-NLS-1$
		return s;
	}
}
