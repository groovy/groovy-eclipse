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

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.core.util.DeduplicationUtil;

import junit.framework.Test;
import junit.framework.TestSuite;

public class DeduplicationUtilTest extends TestCase {

	public DeduplicationUtilTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(DeduplicationUtilTest.class.getPackageName());
		suite.addTest(new TestSuite(DeduplicationUtilTest.class));
		return suite;
	}

	public void testDeduplication() {
		assertDeduplicationString("aNeverUsedBefore".toCharArray());
		assertDeduplicationCharArray("aNeverUsedBefore");
		assertDeduplicationObject(() -> List.of("aNeverUsedBefore"));

		assertDeduplicationString("bNeverUsedBefore".toCharArray());
		assertDeduplicationCharArray("bNeverUsedBefore");
		assertDeduplicationObject(() -> List.of("bNeverUsedBefore"));
	}

	private void assertDeduplicationString(char[] stringTemplate) {
		// do not change order of weak/strong test,
		{ // test weak behaviour:
			String a = new String(stringTemplate);
			String b = new String(stringTemplate);
			assertNotSame(a, b);
			String expected = DeduplicationUtil.intern(b);
			assertSame(b, expected);
			b = null;
			expected = null;
			forceGc();
			String actual = DeduplicationUtil.intern(a);
			assertSame(a, actual); // i.e. actual != expected since "expected" was garbage collected.
		}
		{ // test strong:
			String a = new String(stringTemplate);
			String b = new String(stringTemplate);
			assertNotSame(a, b);
			String actual = DeduplicationUtil.intern(a);
			String expected = DeduplicationUtil.intern(b);
			assertSame(expected, actual);
		}
	}

	private void assertDeduplicationCharArray(String charArrayTemplate) {
		{ // weak
			char[] a = charArrayTemplate.toCharArray();
			char[] b = charArrayTemplate.toCharArray();
			assertNotSame(a, b);
			char[] expected = DeduplicationUtil.intern(b);
			assertSame(b, expected);
			b = null;
			expected = null;
			forceGc();
			char[] actual = DeduplicationUtil.intern(a);
			assertSame(a, actual);
		}
		{ // strong
			char[] a = charArrayTemplate.toCharArray();
			char[] b = charArrayTemplate.toCharArray();
			assertNotSame(a, b);
			char[] actual = DeduplicationUtil.intern(a);
			char[] expected = DeduplicationUtil.intern(b);
			assertSame(expected, actual);
		}
	}

	private void assertDeduplicationObject(Supplier<Object> supplier) {
		{ // weak
			Object a = supplier.get();
			Object b = supplier.get();
			assertNotSame(a, b);
			assertEquals(a, b);
			Object expected = DeduplicationUtil.internObject(b);
			assertSame(b, expected);
			b = null;
			expected = null;

			forceGc();

			// Now "b" is not referenced anymore, can be garbage collected
			// and DeduplicationUtil is supposed to release weak reference to it
			// so after trying to intern "a" we will get "a" and not previously set "b"
			Object actual = DeduplicationUtil.internObject(a);

			// It is impossible to rely on GC to run immediately, so loop few times
			for (int i = 0; i < 42; i++) {
				if(actual != a) {
					forceGc();
					actual = DeduplicationUtil.internObject(a);
				} else {
					break;
				}
			}
			assertSame(a, actual);
		}
		{ // strong
			Object a = supplier.get();
			Object b = supplier.get();
			assertNotSame(a, b);
			assertEquals(a, b);
			Object actual = DeduplicationUtil.internObject(a);
			Object expected = DeduplicationUtil.internObject(b);

			// since "a" is still referenced, we should get it
			assertSame(expected, actual);
		}
	}

	private void forceGc() {
		System.gc();
		System.runFinalization();
		try {
			// give gc some time to run
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// don't care
		}
	}
}
