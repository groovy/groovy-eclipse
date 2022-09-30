/*******************************************************************************
 * Copyright (c) 2021 jkubitz and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     jkubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import org.eclipse.jdt.internal.core.builder.CompressedReader;
import org.eclipse.jdt.internal.core.builder.CompressedWriter;

import junit.framework.Test;

public class CompressedWriterTest extends BuilderTests {

	private CompressedWriter writer;
	private CompressedReader reader;

	public CompressedWriterTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(CompressedWriterTest.class);
	}

	private void start() throws Exception {
		DataOutputStream out;
		DataInputStream in;
		PipedInputStream pis;
		pis = new PipedInputStream(1000000);
		PipedOutputStream pos = new PipedOutputStream(pis);

		out = new DataOutputStream(pos);
		this.writer = new CompressedWriter(out);

		in = new DataInputStream(pis);
		this.reader = new CompressedReader(in);
	}

	private void stop() throws Exception {
		// not realy necessary since its only "resource" in memory
		this.writer = null;
		this.reader = null;
	}

	public void testWriteRead() throws Exception {
		start();
		try {
			// test multiple times since output depends on context (lastName, dictionary)
			for (int i = 1; i < 3; i++) {
				assertReadWhatsWritten();
			}
		} finally {
			stop();
		}
	}

	private void assertReadWhatsWritten() throws Exception {
		assertSame(new Integer[] { 0, 1, -1, 2, -2, (int) Byte.MAX_VALUE, (int) Byte.MIN_VALUE },
				(w, v) -> w.writeByte(v.intValue()), r -> Integer.valueOf(r.readByte()));
		assertSame(new Boolean[] { true, false }, (w, v) -> w.writeBoolean(v.booleanValue()),
				r -> Boolean.valueOf(r.readBoolean()));
		assertSame(new Integer[] { 0, 1, -1, 2, -2, Integer.MAX_VALUE, Integer.MIN_VALUE },
				(w, v) -> w.writeInt(v.intValue()), r -> Integer.valueOf(r.readInt()));
		assertSame(new Long[] { 0L, 1L, -1L, 2L, -2L, Long.MAX_VALUE, Long.MIN_VALUE },
				(w, v) -> w.writeLong(v.longValue()), r -> Long.valueOf(r.readLong()));

		int[] ranges = new int[] { 5, Byte.MAX_VALUE, Byte.MAX_VALUE - 1, Byte.MAX_VALUE + 1, 254, 255, 256, 0xffff - 1,
				0xffff, 0xffff + 1, 0xfffff, 0xfffff + 1, 0xffffff - 1, 0xffffff, 0xffffff + 1, 0xffffffff - 1,
				0xffffffff, 0x12, 0x1234, 0x123456, 0x12345678 };
		for (int range : ranges) {
			assertSame(new Integer[] { 0, 1, 2, range - 1 }, (w, v) -> w.writeIntInRange(v.intValue(), range),
					r -> Integer.valueOf(r.readIntInRange(range)));
		}

		int[] typical1 = new int[] { 0, 1, -1, 2, -2, Integer.MAX_VALUE, Integer.MIN_VALUE };
		int[] typical2 = new int[] { 3 };
		int[] typical3 = new int[] {};
		int[] typical4 = new int[] { 1, 1 };
		assertSame(new Integer[] { 0, 1, -1, 2, -2, Integer.MAX_VALUE, Integer.MIN_VALUE },
				(w, v) -> w.writeIntWithHint(v.intValue(), typical1),
				r -> Integer.valueOf(r.readIntWithHint(typical1)));
		assertSame(new Integer[] { 0, 1, -1, 2, -2, Integer.MAX_VALUE, Integer.MIN_VALUE },
				(w, v) -> w.writeIntWithHint(v.intValue(), typical2),
				r -> Integer.valueOf(r.readIntWithHint(typical2)));
		assertSame(new Integer[] { 0, 1, -1, 2, -2, Integer.MAX_VALUE, Integer.MIN_VALUE },
				(w, v) -> w.writeIntWithHint(v.intValue(), typical3),
				r -> Integer.valueOf(r.readIntWithHint(typical3)));
		assertSame(new Integer[] { 0, 1, -1, 2, -2, Integer.MAX_VALUE, Integer.MIN_VALUE },
				(w, v) -> w.writeIntWithHint(v.intValue(), typical4),
				r -> Integer.valueOf(r.readIntWithHint(typical4)));

		String[] strings = new String[] { "nix", "xx", "xxxy", "xyz", "", "xxy", "z", "z", "xyz", "xu", "-".repeat(254),
				"-".repeat(255), "-".repeat(256), "-".repeat(257), "-".repeat(60000) };
		char[][] chars = Arrays.stream(strings).map(String::toCharArray).toArray(char[][]::new);
		assertSame(strings, (w, v) -> w.writeStringUsingDictionary(v), r -> r.readStringUsingDictionary());
		assertSame(chars, (w, v) -> w.writeChars(v), r -> r.readChars());
		assertSame(chars, (w, v) -> w.writeCharsUsingLast(v), r -> r.readCharsUsingLast());
		assertSame(strings, (w, v) -> w.writeStringUsingLast(v), r -> r.readStringUsingLast());
	}

	@FunctionalInterface
	public interface ExceptionalBiConsumer<T, U> {
		void accept(T t, U u) throws Exception;
	}

	@FunctionalInterface
	public interface ExceptionalFunction<T, R> {
		R apply(T t) throws Exception;
	}

	private <V> void assertSame(V[] vs, ExceptionalBiConsumer<CompressedWriter, V> write,
			ExceptionalFunction<CompressedReader, V> read) throws Exception {
		for (V v : vs) {
			assertEquals(v, write, read);
		}

	}

	private <V> void assertEquals(V in, ExceptionalBiConsumer<CompressedWriter, V> write,
			ExceptionalFunction<CompressedReader, V> read) throws Exception {
		Object input = charToStringOrSame(in);
//		System.out.println("writing " + input);
		write.accept(this.writer, in);
		V out = read.apply(this.reader); // may block if written too less
		Object output = charToStringOrSame(out);
		assertEquals(in.getClass(), out.getClass());
		assertEquals(input, output);
	}

	private Object charToStringOrSame(Object v) {
		return (v instanceof char[]) ? new String((char[]) v) : v;
	}
}