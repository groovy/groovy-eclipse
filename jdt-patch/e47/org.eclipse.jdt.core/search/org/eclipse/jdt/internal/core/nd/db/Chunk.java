/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.db;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Caches the content of a piece of the database.
 */
final class Chunk {
	final private byte[] fBuffer= new byte[Database.CHUNK_SIZE];

	final Database fDatabase;
	final int fSequenceNumber;

	boolean fCacheHitFlag;
	boolean fDirty;
	boolean fLocked;	// locked chunks must not be released from cache.
	int fCacheIndex= -1;

	Chunk(Database db, int sequenceNumber) {
		this.fDatabase= db;
		this.fSequenceNumber= sequenceNumber;
	}

	void read() throws IndexException {
		try {
			final ByteBuffer buf= ByteBuffer.wrap(this.fBuffer);
			this.fDatabase.read(buf, (long) this.fSequenceNumber * Database.CHUNK_SIZE);
		} catch (IOException e) {
			throw new IndexException(new DBStatus(e));
		}
	}

	/**
	 * Uninterruptable. Returns true iff an attempt was made to interrupt the flush with
	 * {@link Thread#interrupt()}.
	 */
	boolean flush() throws IndexException {
		boolean wasCanceled = false;
		try {
			final ByteBuffer buf= ByteBuffer.wrap(this.fBuffer);
			wasCanceled = this.fDatabase.write(buf, (long) this.fSequenceNumber * Database.CHUNK_SIZE);
		} catch (IOException e) {
			throw new IndexException(new DBStatus(e));
		}
		this.fDirty= false;
		return wasCanceled;
	}

	private static int recPtrToIndex(final long offset) {
		return (int) (offset & Database.OFFSET_IN_CHUNK_MASK);
	}

	public void putByte(final long offset, final byte value) {
		assert this.fLocked;
		this.fDirty= true;
		this.fBuffer[recPtrToIndex(offset)]= value;
	}

	public byte getByte(final long offset) {
		return this.fBuffer[recPtrToIndex(offset)];
	}

	public byte[] getBytes(final long offset, final int length) {
		final byte[] bytes = new byte[length];
		System.arraycopy(this.fBuffer, recPtrToIndex(offset), bytes, 0, length);
		return bytes;
	}

	public void putBytes(final long offset, final byte[] bytes) {
		assert this.fLocked;
		this.fDirty= true;
		System.arraycopy(bytes, 0, this.fBuffer, recPtrToIndex(offset), bytes.length);
	}

	public void putInt(final long offset, final int value) {
		assert this.fLocked;
		this.fDirty= true;
		int idx= recPtrToIndex(offset);
		putInt(value, this.fBuffer, idx);
	}

	static final void putInt(final int value, final byte[] buffer, int idx) {
		buffer[idx]=   (byte) (value >> 24);
		buffer[++idx]= (byte) (value >> 16);
		buffer[++idx]= (byte) (value >> 8);
		buffer[++idx]= (byte) (value);
	}

	public int getInt(final long offset) {
		return getInt(this.fBuffer, recPtrToIndex(offset));
	}

	static final int getInt(final byte[] buffer, int idx) {
		return ((buffer[idx] & 0xff) << 24) |
				((buffer[++idx] & 0xff) << 16) |
				((buffer[++idx] & 0xff) <<  8) |
				((buffer[++idx] & 0xff) <<  0);
	}

	/**
	 * A free Record Pointer is a pointer to a raw block, i.e. the
	 * pointer is not moved past the BLOCK_HEADER_SIZE.
	 */
	static int compressFreeRecPtr(final long value) {
		// This assert verifies the alignment. We expect the low bits to be clear.
		assert (value & (Database.BLOCK_SIZE_DELTA - 1)) == 0;
		final int dense = (int) (value >> Database.BLOCK_SIZE_DELTA_BITS);
		return dense;
	}

	/**
	 * A free Record Pointer is a pointer to a raw block,
	 * i.e. the pointer is not moved past the BLOCK_HEADER_SIZE.
	 */
	static long expandToFreeRecPtr(int value) {
		/*
		 * We need to properly manage the integer that was read. The value will be sign-extended
		 * so if the most significant bit is set, the resulting long will look negative. By
		 * masking it with ((long)1 << 32) - 1 we remove all the sign-extended bits and just
		 * have an unsigned 32-bit value as a long. This gives us one more useful bit in the
		 * stored record pointers.
		 */
		long address = value & 0xFFFFFFFFL;
		return address << Database.BLOCK_SIZE_DELTA_BITS;
	}

	/**
	 * A Record Pointer is a pointer as returned by Database.malloc().
	 * This is a pointer to a block + BLOCK_HEADER_SIZE.
	 */
	public void putRecPtr(final long offset, final long value) {
		assert this.fLocked;
		this.fDirty = true;
		int idx = recPtrToIndex(offset);
		Database.putRecPtr(value, this.fBuffer, idx);
	}

	/**
	 * A free Record Pointer is a pointer to a raw block,
	 * i.e. the pointer is not moved past the BLOCK_HEADER_SIZE.
	 */
	public void putFreeRecPtr(final long offset, final long value) {
		assert this.fLocked;
		this.fDirty = true;
		int idx = recPtrToIndex(offset);
		putInt(compressFreeRecPtr(value), this.fBuffer, idx);
	}

	public long getRecPtr(final long offset) {
		final int idx = recPtrToIndex(offset);
		return Database.getRecPtr(this.fBuffer, idx);
	}

	public long getFreeRecPtr(final long offset) {
		final int idx = recPtrToIndex(offset);
		int value = getInt(this.fBuffer, idx);
		return expandToFreeRecPtr(value);
	}

	public void put3ByteUnsignedInt(final long offset, final int value) {
		assert this.fLocked;
		this.fDirty= true;
		int idx= recPtrToIndex(offset);
		this.fBuffer[idx]= (byte) (value >> 16);
		this.fBuffer[++idx]= (byte) (value >> 8);
		this.fBuffer[++idx]= (byte) (value);
	}

	public int get3ByteUnsignedInt(final long offset) {
		int idx= recPtrToIndex(offset);
		return ((this.fBuffer[idx] & 0xff) << 16) |
				((this.fBuffer[++idx] & 0xff) <<  8) |
				((this.fBuffer[++idx] & 0xff) <<  0);
	}

	public void putShort(final long offset, final short value) {
		assert this.fLocked;
		this.fDirty= true;
		int idx= recPtrToIndex(offset);
		this.fBuffer[idx]= (byte) (value >> 8);
		this.fBuffer[++idx]= (byte) (value);
	}

	public short getShort(final long offset) {
		int idx= recPtrToIndex(offset);
		return (short) (((this.fBuffer[idx] << 8) | (this.fBuffer[++idx] & 0xff)));
	}

	public long getLong(final long offset) {
		int idx= recPtrToIndex(offset);
		return ((((long) this.fBuffer[idx] & 0xff) << 56) |
				(((long) this.fBuffer[++idx] & 0xff) << 48) |
				(((long) this.fBuffer[++idx] & 0xff) << 40) |
				(((long) this.fBuffer[++idx] & 0xff) << 32) |
				(((long) this.fBuffer[++idx] & 0xff) << 24) |
				(((long) this.fBuffer[++idx] & 0xff) << 16) |
				(((long) this.fBuffer[++idx] & 0xff) <<  8) |
				(((long) this.fBuffer[++idx] & 0xff) <<  0));
	}

	public double getDouble(long offset) {
		return Double.longBitsToDouble(getLong(offset));
	}

	public float getFloat(long offset) {
		return Float.intBitsToFloat(getInt(offset));
	}

	public void putLong(final long offset, final long value) {
		assert this.fLocked;
		this.fDirty= true;
		int idx= recPtrToIndex(offset);

		this.fBuffer[idx]=   (byte) (value >> 56);
		this.fBuffer[++idx]= (byte) (value >> 48);
		this.fBuffer[++idx]= (byte) (value >> 40);
		this.fBuffer[++idx]= (byte) (value >> 32);
		this.fBuffer[++idx]= (byte) (value >> 24);
		this.fBuffer[++idx]= (byte) (value >> 16);
		this.fBuffer[++idx]= (byte) (value >> 8);
		this.fBuffer[++idx]= (byte) (value);
	}

	public void putChar(final long offset, final char value) {
		assert this.fLocked;
		this.fDirty= true;
		int idx= recPtrToIndex(offset);
		this.fBuffer[idx]= (byte) (value >> 8);
		this.fBuffer[++idx]= (byte) (value);
	}

	public void putChars(final long offset, char[] chars, int start, int len) {
		assert this.fLocked;
		this.fDirty= true;
		int idx= recPtrToIndex(offset)-1;
		final int end= start + len;
		for (int i = start; i < end; i++) {
			char value= chars[i];
			this.fBuffer[++idx]= (byte) (value >> 8);
			this.fBuffer[++idx]= (byte) (value);
		}
	}

	public void putCharsAsBytes(final long offset, char[] chars, int start, int len) {
		assert this.fLocked;
		this.fDirty= true;
		int idx= recPtrToIndex(offset)-1;
		final int end= start + len;
		for (int i = start; i < end; i++) {
			char value= chars[i];
			this.fBuffer[++idx]= (byte) (value);
		}
	}

	public void putDouble(final long offset, double value) {
		putLong(offset, Double.doubleToLongBits(value));
	}

	public void putFloat(final long offset, float value) {
		putInt(offset, Float.floatToIntBits(value));
	}

	public char getChar(final long offset) {
		int idx= recPtrToIndex(offset);
		return (char) (((this.fBuffer[idx] << 8) | (this.fBuffer[++idx] & 0xff)));
	}

	public void getChars(final long offset, final char[] result, int start, int len) {
		final ByteBuffer buf= ByteBuffer.wrap(this.fBuffer);
		buf.position(recPtrToIndex(offset));
		buf.asCharBuffer().get(result, start, len);
	}

	public void getCharsFromBytes(final long offset, final char[] result, int start, int len) {
		final int pos = recPtrToIndex(offset);
		for (int i = 0; i < len; i++) {
			result[start + i] =  (char) (this.fBuffer[pos + i] & 0xff);
		}
	}

	void clear(final long offset, final int length) {
		assert this.fLocked;
		this.fDirty= true;
		int idx = recPtrToIndex(offset);
		final int end = idx + length;
		for (; idx < end; idx++) {
			this.fBuffer[idx] = 0;
		}
	}

	void put(final long offset, final byte[] data, final int len) {
		put(offset, data, 0, len);
	}

	void put(final long offset, final byte[] data, int dataPos, final int len) {
		assert this.fLocked;
		this.fDirty = true;
		int idx = recPtrToIndex(offset);
		System.arraycopy(data, dataPos, this.fBuffer, idx, len);
	}

	public void get(final long offset, byte[] data) {
		get(offset, data, 0, data.length);
	}

	public void get(final long offset, byte[] data, int dataPos, int len) {
		int idx = recPtrToIndex(offset);
		System.arraycopy(this.fBuffer, idx, data, dataPos, len);
	}
}
