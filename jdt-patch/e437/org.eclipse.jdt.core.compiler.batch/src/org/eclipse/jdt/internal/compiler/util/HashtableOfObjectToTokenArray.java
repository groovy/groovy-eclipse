/*******************************************************************************
 * Copyright (c) 2025 Jesper S Moller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper S Moller <jesper@selskabet.org> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.util;

import org.eclipse.jdt.internal.compiler.parser.TerminalToken;

/**
 * Hashtable of {Object --> TerminalTokens[] }
 */
public final class HashtableOfObjectToTokenArray implements Cloneable {

	// to avoid using Enumerations, walk the individual tables skipping nulls
	public Object[] keyTable;
	public TerminalToken[][] valueTable;

	public int elementSize; // number of elements in the table
	int threshold;

	public HashtableOfObjectToTokenArray() {
		this(13);
	}

	public HashtableOfObjectToTokenArray(int size) {

		this.elementSize = 0;
		this.threshold = size; // size represents the expected number of elements
		int extraRoom = (int) (size * 1.75f);
		if (this.threshold == extraRoom)
			extraRoom++;
		this.keyTable = new Object[extraRoom];
		this.valueTable = new TerminalToken[extraRoom][];
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		HashtableOfObjectToTokenArray result = (HashtableOfObjectToTokenArray) super.clone();
		result.elementSize = this.elementSize;
		result.threshold = this.threshold;

		int length = this.keyTable.length;
		result.keyTable = new Object[length];
		System.arraycopy(this.keyTable, 0, result.keyTable, 0, length);

		length = this.valueTable.length;
		result.valueTable = new TerminalToken[length][];
		System.arraycopy(this.valueTable, 0, result.valueTable, 0, length);
		return result;
	}

	public boolean containsKey(Object key) {
		int length = this.keyTable.length,
			index = (key.hashCode()& 0x7FFFFFFF) % length;
		Object currentKey;
		while ((currentKey = this.keyTable[index]) != null) {
			if (currentKey.equals(key))
				return true;
			if (++index == length) {
				index = 0;
			}
		}
		return false;
	}

	public TerminalToken[] get(Object key) {
		int length = this.keyTable.length,
			index = (key.hashCode()& 0x7FFFFFFF) % length;
		Object currentKey;
		while ((currentKey = this.keyTable[index]) != null) {
			if (currentKey.equals(key))
				return this.valueTable[index];
			if (++index == length) {
				index = 0;
			}
		}
		return null;
	}

	public void keysToArray(Object[] array) {
		int index = 0;
		for (Object key : this.keyTable) {
			if (key != null)
				array[index++] = key;
		}
	}

	public TerminalToken[] put(Object key, TerminalToken[] value) {
		int length = this.keyTable.length,
			index = (key.hashCode()& 0x7FFFFFFF) % length;
		Object currentKey;
		while ((currentKey = this.keyTable[index]) != null) {
			if (currentKey.equals(key))
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

	public TerminalToken[] removeKey(Object key) {
		int length = this.keyTable.length,
			index = (key.hashCode()& 0x7FFFFFFF) % length;
		Object currentKey;
		while ((currentKey = this.keyTable[index]) != null) {
			if (currentKey.equals(key)) {
				TerminalToken[] value = this.valueTable[index];
				this.elementSize--;
				this.keyTable[index] = null;
				rehash();
				return value;
			}
			if (++index == length) {
				index = 0;
			}
		}
		return null;
	}

	private void rehash() {

		HashtableOfObjectToTokenArray newHashtable = new HashtableOfObjectToTokenArray(this.elementSize * 2);		// double the number of expected elements
		Object currentKey;
		for (int i = this.keyTable.length; --i >= 0;)
			if ((currentKey = this.keyTable[i]) != null)
				newHashtable.put(currentKey, this.valueTable[i]);

		this.keyTable = newHashtable.keyTable;
		this.valueTable = newHashtable.valueTable;
		this.threshold = newHashtable.threshold;
	}

	public int size() {
		return this.elementSize;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		Object key;
		for (int i = 0, length = this.keyTable.length; i < length; i++) {
			if ((key = this.keyTable[i]) != null) {
				buffer.append(key).append(" -> "); //$NON-NLS-1$
				TerminalToken[] ints = this.valueTable[i];
				buffer.append('[');
				if (ints != null) {
					for (int j = 0, max = ints.length; j < max; j++) {
						if (j > 0) {
							buffer.append(',');
						}
						buffer.append(ints[j]);
					}
				}
				buffer.append("]\n"); //$NON-NLS-1$
			}
		}
		return String.valueOf(buffer);
	}
}
