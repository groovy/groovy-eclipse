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
package org.eclipse.jdt.core.tests.compiler;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.util.CharDeduplication;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CharDeduplicationTest extends TestCase {

	public CharDeduplicationTest(String testName) {
		super(testName);
	}

	public static Test suite() {

		TestSuite suite = new TestSuite(CharDeduplicationTest.class.getPackageName());
		suite.addTest(new TestSuite(CharDeduplicationTest.class));
		return suite;
	}

	public void testDeduplication() {
		for (int i = 0; i < 3; i++) {
			assertDeduplication("a");
			// ..
			assertDeduplication("z");

			assertDeduplication("12");
			assertDeduplication("123");
			assertDeduplication("1234");
			assertDeduplication("12345");
			assertDeduplication("123456");
			assertNoDeduplication("1234567");

			// new:
			assertDeduplication("0"); // illegal identifier - but who cares.
			assertDeduplication("A"); // why not?
			assertDeduplication("$"); // legal
			assertDeduplication("_"); // note that "_" may become more common after JEP 302 as keyword for unused
										// lambda parameter!
			assertDeduplication("" + (char) 0);
			// ..
			assertDeduplication("" + (char) 127);
			assertNoDeduplication("" + (char) 128); // non-Ascii
		}
	}

	public void testDeduplicationMid() {
		String text = "abcdefghijklmn";
		for (int start = 0; start < text.length(); start++) {
			for (int end = start; end < text.length(); end++) {
				assertDedup(text, (end - start) <= CharDeduplication.OPTIMIZED_LENGTH, start, end);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void testDeduplicationTableSize() {
		CharDeduplication deduplication = CharDeduplication.getThreadLocalInstance();
		deduplication.reset();// to test the overflow we need to start empty
		for (int overload = 0; overload < 3; overload++) {
			HashMap<Integer, char[]> expecteds = new HashMap<>();
			for (int i = 0; i < CharDeduplication.TABLE_SIZE + overload; i++) {
				int numberWithFixedLength = 10000 + i;
				String string = "" + numberWithFixedLength;
				char[] a = string.toCharArray();
				char[] expected = deduplication.sharedCopyOfRange(a, 0, a.length);
				expecteds.put(i, expected);
			}
			for (int t = 0; t < 2; t++) {
				for (int i = 0; i < expecteds.size(); i++) {
					char[] expected = expecteds.get(i);
					char[] other = String.valueOf(expected).toCharArray();
					if (overload == 0 || i > 0) {
						assertDedup(true, 0, expected.length, expected, other);
					} else {
						// situation after table overflow:
						char[] actual = deduplication.sharedCopyOfRange(other, 0, expected.length);
						// both actual == expected or actual != expected may happen
						// but we can assert that the next deduplication works again:
						char[] other2 = String.valueOf(expected).toCharArray();
						assertDedup(true, 0, expected.length, actual, other2);
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		CharDeduplicationTest test=new CharDeduplicationTest("");
		System.out.println("min= ~"+ LongStream.range(0, 100).map(t->test.runPerformanceTest()).min());
		// min= ~0.36sec
	}

	public long runPerformanceTest() {
		long nanoTime = System.nanoTime();
		CharDeduplication deduplication = CharDeduplication.getThreadLocalInstance();
		for (int j = 0; j < 100; j++) {
			for (int i = 0; i < 100_000; i++) {
				String hexString = Integer.toHexString(i);
				char[] chars = hexString.toCharArray();
				deduplication.sharedCopyOfRange(chars, 0, chars.length);
			}
		}
		long nanoTime2 = System.nanoTime();
		long durationNanos = nanoTime2 - nanoTime;
		System.out.println(durationNanos);
		return durationNanos;
	}

	public void testAll() {
		testDeduplication();
		testDeduplicationMid();
		testDeduplicationTableSize();
	}

	public void testMultithreaded() throws Exception {
		int nThreads = 8;
		List<FutureTask<Object>> tasks = IntStream.range(0, nThreads * 2).mapToObj(i -> new FutureTask<Object>(() -> {
			testAll();
			return null;
		})).collect(Collectors.toList());
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		tasks.forEach(executor::submit);
		for (FutureTask<Object> task : tasks) {
			try {
				task.get();
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
		executor.shutdownNow();
	}

	private void assertDeduplication(String string) {
		assertDedup(string, true, 0, string.length());
	}

	private void assertNoDeduplication(String string) {
		assertDedup(string, false, 0, string.length());
	}

	private void assertDedup(String string, boolean same, int startPosition, int end) {
		char[] a = string.toCharArray();
		char[] b = string.toCharArray();
		assertNotSame(a, b);
		CharDeduplication deduplication = CharDeduplication.getThreadLocalInstance();
		char[] expected = deduplication.sharedCopyOfRange(a, startPosition, end);
		assertDedup(same, startPosition, end, expected, b);
	}

	private char[] assertDedup(boolean same, int startPosition, int end, char[] expected, char[] other) {
		assertNotSame(expected, other);
		CharDeduplication deduplication = CharDeduplication.getThreadLocalInstance();
		char[] actual = deduplication.sharedCopyOfRange(other, startPosition, end);
		String state = "expected=" + String.valueOf(expected) + ", actual=" + String.valueOf(actual);
		if (same) {
			assertSame(state, expected, actual);
		} else {
			assertNotSame("Expected different instances. But thats not a requirement but an implementation detail test:"
					+ state, expected, actual);
		}
		return actual;
	}
}
