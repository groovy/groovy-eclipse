/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

/**
 * Represents an array of long.
 */
public class LongArray {
	private static final int MIN_CAPACITY = 8;
	private long[] contents;
	private int size;

	long get(int index) {
		if (index >= this.size) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		return this.contents[index];
	}

	long removeLast() {
		return this.contents[--this.size];
	}

	void addLast(long toAdd) {
		ensureCapacity(this.size + 1);
		this.contents[this.size++] = toAdd;
	}

	private void ensureCapacity(int capacity) {
		if (this.contents == null) {
			this.contents = new long[Math.max(MIN_CAPACITY, capacity)];
		}

		if (this.contents.length >= capacity) {
			return;
		}

		int newSize = capacity * 2;
		long[] newContents = new long[newSize];

		System.arraycopy(this.contents, 0, newContents, 0, this.contents.length);
		this.contents = newContents;
	}

	int size() {
		return this.size;
	}

	public boolean isEmpty() {
		return this.size == 0;
	}
}
