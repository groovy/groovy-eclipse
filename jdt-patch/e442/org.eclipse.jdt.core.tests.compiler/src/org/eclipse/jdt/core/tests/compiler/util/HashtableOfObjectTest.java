/*******************************************************************************
 * Copyright (c) 2022 Andrey Loskutov, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.util;

import static org.eclipse.jdt.internal.compiler.util.HashtableOfObject.MAX_ARRAY_SIZE;
import static org.eclipse.jdt.internal.compiler.util.HashtableOfObject.calculateNewSize;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.junit.Test;

public class HashtableOfObjectTest extends TestCase {

	public HashtableOfObjectTest(String name) {
		super(name);
	}

	@Test
	public void testCalculateNewSize() {
		int input;
		int expected;

		// Overflow
		input = Integer.MAX_VALUE * 2;
		try {
			calculateNewSize(input);
			fail("Should not accept " + input);
		} catch (NegativeArraySizeException e) {
			// expected
		}
		input = Integer.MAX_VALUE + 1;
		try {
			calculateNewSize(input);
			fail("Should not accept " + input);
		} catch (NegativeArraySizeException e) {
			// expected
		}
		input = -1;
		try {
			calculateNewSize(input);
			fail("Should not accept " + input);
		} catch (NegativeArraySizeException e) {
			// expected
		}

		// Regular size
		assertEquals(1, calculateNewSize(0));
		assertEquals(2, calculateNewSize(1));

		input = (MAX_ARRAY_SIZE / 2) - 10000;
		expected = input * 2;
		assertEquals(expected, calculateNewSize(input));

		input = (MAX_ARRAY_SIZE / 2) - 1;
		expected = input * 2;
		assertEquals(expected, calculateNewSize(input));

		input = (MAX_ARRAY_SIZE / 2);
		expected = input + (MAX_ARRAY_SIZE - input) / 2;
		assertEquals(expected, calculateNewSize(input));

		// Can't simply double the size
		input = (MAX_ARRAY_SIZE / 2) + 1;
		expected = input + (MAX_ARRAY_SIZE - input) / 2;
		assertEquals(expected, calculateNewSize(input));

		input = (MAX_ARRAY_SIZE / 2) + 10000;
		expected = input + (MAX_ARRAY_SIZE - input) / 2;
		assertEquals(expected, calculateNewSize(input));

		// Last few possible sizes
		input = MAX_ARRAY_SIZE - 4;
		expected = input + (MAX_ARRAY_SIZE - input) / 2;
		assertEquals(expected, calculateNewSize(input));

		input = MAX_ARRAY_SIZE - 3;
		expected = input + 1;
		assertEquals(expected, calculateNewSize(input));

		// No way to increase
		try {
			input = MAX_ARRAY_SIZE - 2;
			calculateNewSize(input);
			fail("Should not support table size " + input);
		} catch (OutOfMemoryError e) {
			// expected
		}
		try {
			input = MAX_ARRAY_SIZE - 1;
			calculateNewSize(input);
			fail("Should not support table size " + input);
		} catch (OutOfMemoryError e) {
			// expected
		}
		try {
			input = MAX_ARRAY_SIZE;
			calculateNewSize(input);
			fail("Should not support table size " + input);
		} catch (OutOfMemoryError e) {
			// expected
		}
		try {
			input = Integer.MAX_VALUE - 1;
			calculateNewSize(input);
			fail("Should not support table size " + input);
		} catch (OutOfMemoryError e) {
			// expected
		}
		try {
			input = Integer.MAX_VALUE;
			calculateNewSize(input);
			fail("Should not support table size " + input);
		} catch (OutOfMemoryError e) {
			// expected
		}
	}

	@Test
	public void testCreateNewTable() {
		int input;
		int expected;
		// Overflow
		input = Integer.MAX_VALUE + 1;
		try {
			new HashtableOfObject(input);
			fail("Should not accept " + input);
		} catch (NegativeArraySizeException e) {
			// expected
		}
		input = Integer.MAX_VALUE * 2;
		try {
			new HashtableOfObject(input);
			fail("Should not accept " + input);
		} catch (NegativeArraySizeException e) {
			// expected
		}
		input = Integer.MAX_VALUE * 2 + 1;
		try {
			new HashtableOfObject(input);
			fail("Should not accept " + input);
		} catch (NegativeArraySizeException e) {
			// expected
		}
		input = -1;
		try {
			new HashtableOfObject(input);
			fail("Should not accept " + input);
		} catch (NegativeArraySizeException e) {
			// expected
		}

		// Regular sizes
		assertEquals(1, new HashtableOfObject(0).storageSize());
		assertEquals(2, new HashtableOfObject(1).storageSize());
		assertEquals(3, new HashtableOfObject(2).storageSize());

		// No way to increase
		try {
			input = MAX_ARRAY_SIZE - 2;
			new HashtableOfObject(input);
			fail("Should support table size " + input);
		} catch (OutOfMemoryError e) {
			// expected
		}
		try {
			input = MAX_ARRAY_SIZE - 1;
			new HashtableOfObject(input);
			fail("Should support table size " + input);
		} catch (OutOfMemoryError e) {
			// expected
		}
		try {
			input = MAX_ARRAY_SIZE;
			new HashtableOfObject(input);
			fail("Should not accept " + input);
		} catch (OutOfMemoryError e) {
			// expected
		}
		try {
			input = Integer.MAX_VALUE - 1;
			new HashtableOfObject(input);
			fail("Should not accept " + input);
		} catch (OutOfMemoryError e) {
			// expected
		}
		try {
			input = Integer.MAX_VALUE;
			new HashtableOfObject(input);
			fail("Should not accept " + input);
		} catch (OutOfMemoryError e) {
			// expected
		}

		if(Runtime.getRuntime().maxMemory() / (1024 * 1024) < 31000) {
			// requires lot of heap
			return;
		}

		// Can't simply double the size
		input = (int)(MAX_ARRAY_SIZE / 1.75);
		expected = input + (MAX_ARRAY_SIZE - input) / 2;
		assertEquals(expected, new HashtableOfObject(input).storageSize());

		input = MAX_ARRAY_SIZE - 4;
		expected = input + (MAX_ARRAY_SIZE - input) / 2;
		assertEquals(expected, new HashtableOfObject(input).storageSize());

		// Last possible size
		input = MAX_ARRAY_SIZE - 3;
		expected = input + 1;
		assertEquals(expected, new HashtableOfObject(input).storageSize());

		System.gc();
	}
}
