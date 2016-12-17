/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IndexException;

public abstract class TagTreeReader {
	public static final int[] UNUSED_RESULT = new int[1];

	public static abstract class TagHandler<T> {
		abstract public T read(Nd nd, long address, TagTreeReader reader, int[] bytesRead);
		abstract public void write(Nd nd, long address, TagTreeReader reader, T toWrite, int[] bytesWritten);
		abstract public int getSize(Nd nd, T object, TagTreeReader reader);
		public void destruct(Nd nd, long address, TagTreeReader reader) {
			// Nothing to do by default
		}
	}

	public static abstract class FixedSizeTagHandler<T> extends TagHandler<T> {
		protected abstract T read(Nd nd, long address);
		protected abstract void write(Nd nd, long address, T value);
		protected abstract int getSize();
		protected void destruct(Nd nd, long address) {
			// Nothing to do by default
		}

		public final T read(Nd nd, long address, TagTreeReader reader, int[] bytesRead) {
			bytesRead[0] = getSize();
			return read(nd, address);
		}

		@Override
		public final void write(Nd nd, long address, TagTreeReader reader, T value, int[] bytesWritten) {
			bytesWritten[0] = getSize();
			write(nd, address, value);
		}

		@Override
		public final int getSize(Nd nd, T object, TagTreeReader reader) {
			return getSize();
		}

		@Override
		public final void destruct(Nd nd, long address, TagTreeReader reader) {
			destruct(nd, address);
		}
	}

	private TagHandler<?> readers[] = new TagHandler[256];
	private Map<TagHandler<?>, Integer> values = new HashMap<>();

	public final void add(byte key, TagHandler<?> reader) {
		this.readers[key] = reader;
		this.values.put(reader, (int) key);
	}

	public final Object read(Nd nd, long address) {
		return read(nd, address, UNUSED_RESULT);
	}

	public final Object read(Nd nd, long address, int[] bytesRead) {
		long readAddress = address;
		Database db = nd.getDB();
		byte nextByte = db.getByte(address);
		readAddress += Database.BYTE_SIZE;
		TagHandler<?> reader = this.readers[nextByte];
		if (reader == null) {
			throw new IndexException("Found unknown tag with value " + nextByte + " at address " + address); //$NON-NLS-1$//$NON-NLS-2$
		}

		return reader.read(nd, readAddress, this, bytesRead);
	}

	protected abstract byte getKeyFor(Object toWrite);

	public final void write(Nd nd, long address, Object toWrite) {
		write(nd, address, toWrite, UNUSED_RESULT);
	}

	@SuppressWarnings("unchecked")
	public final void write(Nd nd, long address, Object toWrite, int[] bytesWritten) {
		byte key = getKeyFor(toWrite);

		@SuppressWarnings("rawtypes")
		TagHandler handler = this.readers[key];

		if (handler == null) {
			throw new IndexException("Invalid key " + key + " returned from getKeyFor(...)"); //$NON-NLS-1$//$NON-NLS-2$
		}

		handler.write(nd, address, this, toWrite, bytesWritten);
	}

	public final void destruct(Nd nd, long address) {
		Database db = nd.getDB();
		long readAddress = address;
		byte nextByte = db.getByte(readAddress);
		readAddress += Database.BYTE_SIZE;

		TagHandler<?> handler = this.readers[nextByte];
		if (handler == null) {
			throw new IndexException("Found unknown tag with value " + nextByte + " at address " + address); //$NON-NLS-1$//$NON-NLS-2$
		}

		handler.destruct(nd, readAddress, this);
	}

	@SuppressWarnings("unchecked")
	public final int getSize(Nd nd, Object toMeasure) {
		byte key = getKeyFor(toMeasure);

		@SuppressWarnings("rawtypes")
		TagHandler handler = this.readers[key];
		if (handler == null) {
			throw new IndexException("Attempted to get size of object " + toMeasure.toString() + " with unknown key " //$NON-NLS-1$//$NON-NLS-2$
					+ key);
		}

		return handler.getSize(nd, toMeasure, this);
	}
}
