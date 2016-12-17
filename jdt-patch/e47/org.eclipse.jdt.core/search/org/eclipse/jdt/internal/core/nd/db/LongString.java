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

/**
 * This is for strings that take up more than on chunk.
 * The string will need to be broken up into sections and then
 * reassembled when necessary.
 *
 * @author Doug Schaefer
 */
public class LongString implements IString {
	private final Database db;
	private final long record;
	private int hash;

	// Additional fields of first record.
	private static final int LENGTH = 0; // Must be first to match ShortString.
	private static final int NEXT1 = 4;
	private static final int CHARS1 = 8;

	private static final int NUM_CHARS1 = (Database.MAX_SINGLE_BLOCK_MALLOC_SIZE - CHARS1) / 2;

	// Additional fields of subsequent records.
	private static final int NEXTN = 0;
	private static final int CHARSN = 4;

	private static final int NUM_CHARSN = (Database.MAX_SINGLE_BLOCK_MALLOC_SIZE - CHARSN) / 2;

	public LongString(Database db, long record) {
		this.db = db;
		this.record = record;
	}

	public LongString(Database db, final char[] chars, boolean useBytes) throws IndexException {
		final int numChars1 = useBytes ? NUM_CHARS1 * 2 : NUM_CHARS1;
		final int numCharsn = useBytes ? NUM_CHARSN * 2 : NUM_CHARSN;

		this.db = db;
		this.record = db.malloc(Database.MAX_SINGLE_BLOCK_MALLOC_SIZE, Database.POOL_STRING_LONG);

		// Write the first record.
		final int length = chars.length;
		db.putInt(this.record, useBytes ? -length : length);
		Chunk chunk= db.getChunk(this.record);

		if (useBytes) {
			chunk.putCharsAsBytes(this.record + CHARS1, chars, 0, numChars1);
		} else {
			chunk.putChars(this.record + CHARS1, chars, 0, numChars1);
		}

		// Write the subsequent records.
		long lastNext = this.record + NEXT1;
		int start = numChars1;
		while (length - start > numCharsn) {
			long nextRecord = db.malloc(Database.MAX_SINGLE_BLOCK_MALLOC_SIZE, Database.POOL_STRING_LONG);
			db.putRecPtr(lastNext, nextRecord);
			chunk= db.getChunk(nextRecord);
			if (useBytes) {
				chunk.putCharsAsBytes(nextRecord + CHARSN, chars, start, numCharsn);
			} else {
				chunk.putChars(nextRecord + CHARSN, chars, start, numCharsn);
			}
			start += numCharsn;
			lastNext = nextRecord + NEXTN;
		}

		// Write the last record.
		int remaining= length - start;
		long nextRecord = db.malloc(CHARSN + (useBytes ? remaining : remaining * 2), Database.POOL_STRING_LONG);
		db.putRecPtr(lastNext, nextRecord);
		chunk= db.getChunk(nextRecord);
		if (useBytes) {
			chunk.putCharsAsBytes(nextRecord + CHARSN, chars, start, remaining);
		} else {
			chunk.putChars(nextRecord + CHARSN, chars, start, remaining);
		}
	}

	@Override
	public long getRecord() {
		return this.record;
	}

	@Override
	public char[] getChars() throws IndexException {
		int length = this.db.getInt(this.record + LENGTH);
		final boolean useBytes = length < 0;
		int numChars1 = NUM_CHARS1;
		int numCharsn = NUM_CHARSN;
		if (useBytes) {
			length= -length;
			numChars1 *= 2;
			numCharsn *= 2;
		}

		final char[] chars = new char[length];

		// First record
		long p = this.record;
		Chunk chunk= this.db.getChunk(p);
		if (useBytes) {
			chunk.getCharsFromBytes(p + CHARS1, chars, 0, numChars1);
		} else {
			chunk.getChars(p + CHARS1, chars, 0, numChars1);
		}

		int start= numChars1;
		p= this.record + NEXT1;

		// Other records
		while (start < length) {
			p = this.db.getRecPtr(p);
			int partLen= Math.min(length - start, numCharsn);
			chunk= this.db.getChunk(p);
			if (useBytes) {
				chunk.getCharsFromBytes(p + CHARSN, chars, start, partLen);
			} else {
				chunk.getChars(p + CHARSN, chars, start, partLen);
			}
			start += partLen;
			p= p + NEXTN;
		}
		return chars;
	}

	@Override
	public void delete() throws IndexException {
		int length = this.db.getInt(this.record + LENGTH);
		final boolean useBytes = length < 0;
		int numChars1 = NUM_CHARS1;
		int numCharsn = NUM_CHARSN;
		if (useBytes) {
			length= -length;
			numChars1 *= 2;
			numCharsn *= 2;
		}
		long nextRecord = this.db.getRecPtr(this.record + NEXT1);
		this.db.free(this.record, Database.POOL_STRING_LONG);
		length -= numChars1;

		// Middle records.
		while (length > numCharsn) {
			length -= numCharsn;
			long nextnext = this.db.getRecPtr(nextRecord + NEXTN);
			this.db.free(nextRecord, Database.POOL_STRING_LONG);
			nextRecord = nextnext;
		}

		// Last record.
		this.db.free(nextRecord, Database.POOL_STRING_LONG);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		try {
			if (obj instanceof LongString) {
				LongString lstr = (LongString)obj;
				if (this.db == lstr.db && this.record == lstr.record)
					return true;
				return compare(lstr, true) == 0;
			}
			if (obj instanceof char[]) {
				return compare((char[]) obj, true) == 0;
			}
			if (obj instanceof String) {
				return compare((String) obj, true) == 0;
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

	@Override
	public int compare(IString string, boolean caseSensitive) throws IndexException {
		return ShortString.compare(getChars(), string.getChars(), caseSensitive);
	}

	@Override
	public int compare(String other, boolean caseSensitive) throws IndexException {
		return ShortString.compare(getChars(), other.toCharArray(), caseSensitive);
	}

	@Override
	public int compare(char[] other, boolean caseSensitive) throws IndexException {
		return ShortString.compare(getChars(), other, caseSensitive);
	}

	@Override
	public int compareCompatibleWithIgnoreCase(IString string) throws IndexException {
		return ShortString.compareCompatibleWithIgnoreCase(getChars(), string.getChars());
	}

	@Override
	public int comparePrefix(char[] other, boolean caseSensitive) throws IndexException {
		return ShortString.comparePrefix(getChars(), other, caseSensitive);
	}

	@Override
	public String getString() throws IndexException {
		return new String(getChars());
	}

	@Override
	public int compareCompatibleWithIgnoreCase(char[] other) throws IndexException {
		return ShortString.compareCompatibleWithIgnoreCase(getChars(), other);
	}

	@Override
	public int length() {
		return this.db.getInt(this.record + LENGTH);
	}
}
