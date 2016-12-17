/*******************************************************************************
 * Copyright (c) 2006, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.db;

import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

/**
 * This is for strings that fit inside a single chunk.
 */
public class ShortString implements IString {
	private final Database db;
	private final long record;
	private int hash;

	private static final int LENGTH = 0;
	private static final int CHARS = 4;

	public static final int MAX_BYTE_LENGTH = Database.MAX_SINGLE_BLOCK_MALLOC_SIZE - CHARS;

	public ShortString(Database db, long offset) {
		this.db = db;
		this.record = offset;
	}

	public ShortString(Database db, char[] chars, boolean useBytes) throws IndexException {
		final int n = chars.length;
		this.db = db;

		this.record = db.malloc(CHARS + (useBytes ? n : 2 * n), Database.POOL_STRING_SHORT);
		Chunk chunk = db.getChunk(this.record);
		chunk.putInt(this.record + LENGTH, useBytes ? -n : n);
		long p = this.record + CHARS;
		if (useBytes) {
			chunk.putCharsAsBytes(p, chars, 0, n);
		} else {
			chunk.putChars(p, chars, 0, n);
		}
	}

	@Override
	public long getRecord() {
		return this.record;
	}

	@Override
	public void delete() throws IndexException {
		this.db.free(this.record, Database.POOL_STRING_SHORT);
	}

	@Override
	public char[] getChars() throws IndexException {
		final Chunk chunk = this.db.getChunk(this.record);
		final int l = chunk.getInt(this.record + LENGTH);
		final int length = Math.abs(l);
		final char[] chars = new char[length];
		if (l < 0) {
			chunk.getCharsFromBytes(this.record + CHARS, chars, 0, length);
		} else {
			chunk.getChars(this.record + CHARS, chars, 0, length);
		}
		return chars;
	}

	@Override
	public String getString() throws IndexException {
		return new String(getChars());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		try {
			if (obj instanceof ShortString) {
				ShortString string = (ShortString)obj;
				if (this.db == string.db && this.record == string.record)
					return true;

				Chunk chunk1 = this.db.getChunk(this.record);
				Chunk chunk2 = string.db.getChunk(string.record);

				int n1 = chunk1.getInt(this.record);
				int n2 = chunk2.getInt(string.record);
				if (n1 != n2)
					return false;

				return CharArrayUtils.equals(getChars(), string.getChars());
			}
			if (obj instanceof char[]) {
				char[] chars = (char[])obj;

				// Make sure size is the same
				if (length() != chars.length)
					return false;

				return CharArrayUtils.equals(getChars(), chars);
			} else if (obj instanceof String) {
				String string = (String)obj;
				if (length() != string.length())
					return false;

				return CharArrayUtils.equals(getChars(), string.toCharArray());
			}
		} catch (IndexException e) {
			Package.log(e);
		}
		return false;
	}

	/**
	 * Compatible with {@link String#hashCode()}
	 */
	@Override
	public int hashCode() {
		int h = this.hash;
		if (h == 0) {
			char chars[];
			chars = getChars();
			final int len = chars.length;
			for (int i = 0; i < len; i++) {
				h = 31 * h + chars[i];
			}
			this.hash = h;
		}
		return h;
	}

	public static int compare(final char[] chars, char[] other, boolean caseSensitive) {
		final int n = Math.min(chars.length, other.length);
		for (int i = 0; i < n; i++) {
			int cmp= compareChars(chars[i], other[i], caseSensitive);
			if (cmp != 0)
				return cmp;
		}
		return chars.length - other.length;
	}

	@Override
	public int compare(char[] other, boolean caseSensitive) throws IndexException {
		return compare(getChars(), other, caseSensitive);
	}

	@Override
	public int compare(IString string, boolean caseSensitive) throws IndexException {
		return compare(getChars(), string.getChars(), caseSensitive);
	}

	@Override
	public int compare(String other, boolean caseSensitive) throws IndexException {
		return compare(getChars(), other.toCharArray(), caseSensitive);
	}

	@Override
	public int compareCompatibleWithIgnoreCase(IString string) throws IndexException {
		return compareCompatibleWithIgnoreCase(string.getChars());
	}

	@Override
	public int compareCompatibleWithIgnoreCase(char[] other) throws IndexException {
		return compareCompatibleWithIgnoreCase(getChars(), other);
	}

	public static int compareCompatibleWithIgnoreCase(final char[] chars, char[] other) {
		final int n = Math.min(chars.length, other.length);
		int sensitiveCmp= 0;

		for (int i = 0; i < n; i++) {
			final char c1= chars[i];
			final char c2= other[i];
			if (c1 != c2) {
				int cmp= compareChars(c1, c2, false); // insensitive
				if (cmp != 0)
					return cmp;

				if (sensitiveCmp == 0) {
					if (c1 < c2) {
						sensitiveCmp= -1;
					} else {
						sensitiveCmp= 1;
					}
				}
			}
		}
		int cmp= chars.length - other.length;
		if (cmp != 0)
			return cmp;

		return sensitiveCmp;
	}

	@Override
	public int comparePrefix(char[] other, boolean caseSensitive) throws IndexException {
		return comparePrefix(getChars(), other, caseSensitive);
	}

	public static int comparePrefix(final char[] chars, char[] other, boolean caseSensitive) {
		final int n = Math.min(chars.length, other.length);

		for (int i = 0; i < n; i++) {
			int cmp= compareChars(chars[i], other[i], caseSensitive);
			if (cmp != 0)
				return cmp;
		}
		if (chars.length < other.length)
			return -1;

		return 0;
	}

	/**
	 * Compare characters case-sensitively, or case-insensitively.
	 *
	 * <b>Limitation</b> This only maps the range a-z,A-Z onto each other
	 * @param a a character
	 * @param b a character
	 * @param caseSensitive whether to compare case-sensitively
	 * @return
	 * <ul>
	 * <li>-1 if a < b
	 * <li>0 if a == b
	 * <li>1 if a > b
	 * </ul>
	 */
	public static int compareChars(char a, char b, boolean caseSensitive) {
		if (caseSensitive) {
			if (a < b)
				return -1;
			if (a > b)
				return 1;
		} else {
			if (a != b) {
				a= a >= 'a' && a <='z' ? (char) (a - 32) : a;
				b= b >= 'a' && b <='z' ? (char) (b - 32) : b;
				if (a < b)
					return -1;
				if (a > b)
					return 1;
			}
		}
		return 0;
	}

/* TODO - this is more correct than the above implementation, but we need to
 * benchmark first.
 *
 * public static int compareChars(char a, char b, boolean caseSensitive) {
		if (caseSensitive) {
			if (a < b)
				return -1;
			if (a > b)
				return 1;
		} else {
			if (a != b) {
				a = Character.toUpperCase(a);
				b = Character.toUpperCase(b);
				if (a != b) {
					a = Character.toLowerCase(a);
					b = Character.toLowerCase(b);
					if (a != b) {
						if (a < b)
							return -1;
						if (a > b)
							return 1;
					}
				}
			}
		}
		return 0;
	}
*/

	@Override
	public String toString() {
		try {
			return getString();
		} catch (IndexException e) {
			return super.toString();
		}
	}

	@Override
	public int length() {
		return Math.abs(this.db.getInt(this.record + LENGTH));
	}
}
