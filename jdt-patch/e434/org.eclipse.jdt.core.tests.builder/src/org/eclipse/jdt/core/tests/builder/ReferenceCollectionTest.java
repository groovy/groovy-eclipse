/*******************************************************************************
 * Copyright (c) 2019 Sebastian Zarnekow and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sebastian Zarnekow - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import static org.junit.Assert.assertArrayEquals;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.builder.ReferenceCollection;

import junit.framework.Test;

public class ReferenceCollectionTest extends BuilderTests {

	public ReferenceCollectionTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(ReferenceCollectionTest.class);
	}

	private static class TestableReferenceCollection extends ReferenceCollection {
		/*
		    Make the package visible fields reflectively available for testing
			char[][][] qualifiedNameReferences;
			char[][] simpleNameReferences;
			char[][] rootReferences;
		 */
		protected TestableReferenceCollection(char[][][] qualifiedNameReferences, char[][] simpleNameReferences,
				char[][] rootReferences) {
			super(qualifiedNameReferences, simpleNameReferences, rootReferences);
		}

		char[][][] getQualifiedNameReferences() {
			try {
				Field fld = ReferenceCollection.class.getDeclaredField("qualifiedNameReferences");
				fld.setAccessible(true);
				return (char[][][]) fld.get(this);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		char[][] getSimpleNameReferences() {
			try {
				Field fld = ReferenceCollection.class.getDeclaredField("simpleNameReferences");
				fld.setAccessible(true);
				return (char[][]) fld.get(this);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		char[][] getRootReferences() {
			try {
				Field fld = ReferenceCollection.class.getDeclaredField("rootReferences");
				fld.setAccessible(true);
				return (char[][]) fld.get(this);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void testInternQualifiedNamesSorts_01() {
		char[][][] qualifiedNames = new char[][][] {
			CharOperation.splitOn('.', "java.lang.RuntimeException".toCharArray()),
			CharOperation.splitOn('.', "a.a.a".toCharArray()),
			CharOperation.splitOn('.', "b.b.b".toCharArray()),
			CharOperation.splitOn('.', "a.a".toCharArray()),
			CharOperation.splitOn('.', "a.b".toCharArray()),
			CharOperation.splitOn('.', "com".toCharArray()),
			CharOperation.splitOn('.', "a".toCharArray()),
			CharOperation.splitOn('.', "b".toCharArray())
		};
		char[][][] expectation = qualifiedNames.clone();
		Collections.shuffle(Arrays.asList(qualifiedNames));
		char[][][] internQualifiedNames = ReferenceCollection.internQualifiedNames(qualifiedNames, true);
		assertArrayEquals("Should be sorted, longest first, alphanumeric later",
				toStringArray(expectation),
				toStringArray(internQualifiedNames));
	}

	public void testInternQualifiedNamesSorts_02() {
		char[][][] qualifiedNames = new char[][][] {
			CharOperation.splitOn('.', "java.lang.RuntimeException".toCharArray()),
			CharOperation.splitOn('.', "a.a.a".toCharArray()),
			CharOperation.splitOn('.', "b.b.b".toCharArray()),
			CharOperation.splitOn('.', "a.a".toCharArray()),
			CharOperation.splitOn('.', "a.b".toCharArray()),
			CharOperation.splitOn('.', "com".toCharArray()),
			CharOperation.splitOn('.', "a".toCharArray()),
			CharOperation.splitOn('.', "b".toCharArray())
		};
		char[][][] expectation = new char[][][] {
			CharOperation.splitOn('.', "a.a.a".toCharArray()),
			CharOperation.splitOn('.', "b.b.b".toCharArray()),
			CharOperation.splitOn('.', "a.a".toCharArray()),
			CharOperation.splitOn('.', "a.b".toCharArray()),
			CharOperation.splitOn('.', "a".toCharArray()),
			CharOperation.splitOn('.', "b".toCharArray())
		};
		Collections.shuffle(Arrays.asList(qualifiedNames));
		char[][][] internQualifiedNames = ReferenceCollection.internQualifiedNames(qualifiedNames, false);
		assertArrayEquals("Should be sorted, longest first, alphanumeric later",
				toStringArray(expectation),
				toStringArray(internQualifiedNames));
	}

	public void testInternSimpleNamesSorts_01() {
		char[][] simpleNames = new char[][] {
			"Throwable".toCharArray(),
			"aaa".toCharArray(),
			"bbb".toCharArray(),
			"ccc".toCharArray(),
			"aa".toCharArray(),
			"a".toCharArray()
		};
		char[][] expectation = simpleNames.clone();
		Collections.shuffle(Arrays.asList(simpleNames));
		char[][] internSimpleNames = ReferenceCollection.internSimpleNames(simpleNames, false);
		assertArrayEquals("Should be sorted, longest first, alphanumeric later",
				toStringArray(expectation),
				toStringArray(internSimpleNames));
	}

	public void testInternSimpleNamesSorts_02() {
		char[][] simpleNames = new char[][] {
			"aaa".toCharArray(),
			"bbb".toCharArray(),
			"Throwable".toCharArray(),
			"ccc".toCharArray(),
			"java".toCharArray(),
			"aa".toCharArray(),
			"a".toCharArray()
		};
		char[][] expectation = new char[][] {
			"aaa".toCharArray(),
			"bbb".toCharArray(),
			"ccc".toCharArray(),
			"aa".toCharArray(),
			"a".toCharArray()
		};
		Collections.shuffle(Arrays.asList(simpleNames));
		char[][] internSimpleNames = ReferenceCollection.internSimpleNames(simpleNames, true);
		assertArrayEquals("Should be sorted, longest first, alphanumeric later",
				toStringArray(expectation),
				toStringArray(internSimpleNames));
	}

	public void testIncludesWithBinarySearch() {
		char[][] simpleNames = ReferenceCollection.internSimpleNames(new char[][] {
			"a".toCharArray(),
			"b".toCharArray(),
			"c".toCharArray(),
			"d".toCharArray(),
			"e".toCharArray(),
			"f".toCharArray(),
			"g".toCharArray(),
			"h".toCharArray(),
			"i".toCharArray(),
			"j".toCharArray(),
			"k".toCharArray(),
			"l".toCharArray(),
			"m".toCharArray(),
			"n".toCharArray(),
			"o".toCharArray(),
			"p".toCharArray(),
			"q".toCharArray(),
			"r".toCharArray(),
			"s".toCharArray(),
			"t".toCharArray(),
			"u".toCharArray(),
			"v".toCharArray(),
			"w".toCharArray(),
			"x".toCharArray(),
			"y".toCharArray(),
			"z".toCharArray()
		}, false);
		ReferenceCollection collection = new TestableReferenceCollection(null, simpleNames, null);
		for(char[] simpleName: simpleNames) {
			assertTrue("Should include " + simpleName[0], collection.includes(simpleName));
			assertFalse("Should not include " + CharOperation.toUpperCase(simpleName)[0], collection.includes(CharOperation.toUpperCase(simpleName)));
		}
	}

	public void testIncludesWithLinearSearch() {
		char[][] simpleNames = ReferenceCollection.internSimpleNames(new char[][] {
			"a".toCharArray(),
			"b".toCharArray(),
			"c".toCharArray(),
			"d".toCharArray(),
			"e".toCharArray(),
			"f".toCharArray(),
			"g".toCharArray(),
			"h".toCharArray()
		}, false);
		ReferenceCollection collection = new TestableReferenceCollection(null, simpleNames, null);
		for(char[] simpleName: simpleNames) {
			assertTrue("Should include " + simpleName[0], collection.includes(simpleName));
			assertFalse("Should not include " + CharOperation.toUpperCase(simpleName)[0], collection.includes(CharOperation.toUpperCase(simpleName)));
		}
	}

	public void testIncludes01() {
		TestableReferenceCollection refColl = new TestableReferenceCollection(null, null, null);

		String[] array = new String[] { "a.a", "b.a", "b.b" };
		refColl.addDependencies(array);

		TestableReferenceCollection other = new TestableReferenceCollection(null, null, null);
		String[] array2 = new String[] { "a.a", "B.A", "b.b" };
		other.addDependencies(array2);

		assertTrue(refColl.includes(other.getQualifiedNameReferences(), other.getSimpleNameReferences(), other.getRootReferences()));
		assertTrue(other.includes(refColl.getQualifiedNameReferences(), refColl.getSimpleNameReferences(), refColl.getRootReferences()));
	}

	public void testIncludes02() {
		TestableReferenceCollection refColl = new TestableReferenceCollection(null, null, null);

		String[] array = new String[] { "a.x", "b.y" };
		refColl.addDependencies(array);

		TestableReferenceCollection other = new TestableReferenceCollection(null, null, null);
		String[] array2 = new String[] { "a.y", "b.x" };
		other.addDependencies(array2);

		assertTrue(refColl.includes(other.getQualifiedNameReferences(), other.getSimpleNameReferences(), other.getRootReferences()));
		assertTrue(other.includes(refColl.getQualifiedNameReferences(), refColl.getSimpleNameReferences(), refColl.getRootReferences()));
	}

	public void testIncludes03() {
		TestableReferenceCollection refColl = new TestableReferenceCollection(null, null, null);

		String[] array = new String[] { "a.d.y", "c.x" };
		refColl.addDependencies(array);

		TestableReferenceCollection other = new TestableReferenceCollection(null, null, null);
		String[] array2 = new String[] { "c.y" };
		other.addDependencies(array2);

		assertTrue(refColl.includes(other.getQualifiedNameReferences(), other.getSimpleNameReferences(), other.getRootReferences()));
		assertTrue(other.includes(refColl.getQualifiedNameReferences(), refColl.getSimpleNameReferences(), refColl.getRootReferences()));
	}

	public void testIncludes04() {
		TestableReferenceCollection refColl = new TestableReferenceCollection(null, null, null);

		String[] array = new String[] { "a.d.y" };
		refColl.addDependencies(array);

		TestableReferenceCollection other = new TestableReferenceCollection(null, null, null);
		String[] array2 = new String[] { "a.d" };
		other.addDependencies(array2);

		assertTrue(refColl.includes(other.getQualifiedNameReferences(), other.getSimpleNameReferences(), other.getRootReferences()));
		assertTrue(other.includes(refColl.getQualifiedNameReferences(), refColl.getSimpleNameReferences(), refColl.getRootReferences()));
	}

	public void testIncludesNot() {
		TestableReferenceCollection refColl = new TestableReferenceCollection(null, null, null);

		String[] array = new String[] {"b.a"};
		refColl.addDependencies(array);

		TestableReferenceCollection other = new TestableReferenceCollection(null, null, null);
		String[] array2 = new String[] {"B.A"};
		other.addDependencies(array2);

		assertFalse(refColl.includes(other.getQualifiedNameReferences(), other.getSimpleNameReferences(), other.getRootReferences()));
		assertFalse(other.includes(refColl.getQualifiedNameReferences(), refColl.getSimpleNameReferences(), refColl.getRootReferences()));
	}

	public void testAddDependencies() {
		TestableReferenceCollection refColl = new TestableReferenceCollection(null, null, null);

		String[] array = new String[] {"a.b.c.D"};
		refColl.addDependencies(array);

		char[][][] qualifiedNameReferences = refColl.getQualifiedNameReferences();
		String [] strings = toStringArray(qualifiedNameReferences);
		assertArrayEquals(new String[] {
			"a.b.c.D",
			"a.b.c",
			"a.b",
			"a"
		}, strings);

		char[][] simpleNameReferences = refColl.getSimpleNameReferences();
		assertArrayEquals(new String[] {
			"D",
			"a",
			"b",
			"c"
		}, CharOperation.toStrings(simpleNameReferences));

		char[][] rootReferences = refColl.getRootReferences();
		assertArrayEquals(new String[] {
			"a"
		}, CharOperation.toStrings(rootReferences));
	}

	private static String[] toStringArray(char[][][] qualifiedNameReferences) {
		return Arrays.stream(qualifiedNameReferences).map(CharOperation::toString).toArray(String[]::new);
	}

	private static String[] toStringArray(char[][] qualifiedNameReferences) {
		return Arrays.stream(qualifiedNameReferences).map(CharOperation::charToString).toArray(String[]::new);
	}

	public void testRegression01() {
		char[][][] qualifiedNames = new char[][][] {
			CharOperation.splitOn('.', "p1.IX".toCharArray()),
			CharOperation.splitOn('.', "p2.X".toCharArray())
		};
		char[][] simpleNames = new char[][] {
			"I__X".toCharArray(), "IX".toCharArray(), "p1".toCharArray(), "p2".toCharArray(), "X".toCharArray()
		};
		char[][] rootNames = new char[][] {
			"java".toCharArray(), "IX".toCharArray(), "p1".toCharArray(), "p2".toCharArray()
		};
		ReferenceCollection collection = new TestableReferenceCollection(qualifiedNames, simpleNames, rootNames);
		qualifiedNames = ReferenceCollection.internQualifiedNames(new char[][][] {
			CharOperation.splitOn('.', "p2".toCharArray())
		});
		simpleNames = ReferenceCollection.internSimpleNames(new char[][] {
			"X".toCharArray()
		}, true);
		rootNames = ReferenceCollection.internSimpleNames(new char[][] {
			"p2".toCharArray()
		}, false);

		assertTrue("Should include", collection.includes(qualifiedNames, simpleNames, rootNames));
	}

	public void testRegression02() {
		char[][][] qualifiedNames = new char[][][] {};
		char[][] simpleNames = new char[][] {
			"GeneratedAnnotation".toCharArray(), "Test".toCharArray()
		};
		char[][] rootNames = new char[][] {
			"java".toCharArray(), "GeneratedAnnotation".toCharArray()
		};
		ReferenceCollection collection = new TestableReferenceCollection(qualifiedNames, simpleNames, rootNames);

		qualifiedNames = null;
		simpleNames = ReferenceCollection.internSimpleNames(new char[][] {
			"GeneratedAnnotation".toCharArray()
		}, true);
		rootNames = ReferenceCollection.internSimpleNames(new char[][] {
			"GeneratedAnnotation".toCharArray()
		}, false);

		assertTrue("Should include", collection.includes(qualifiedNames, simpleNames, rootNames));
	}
}
