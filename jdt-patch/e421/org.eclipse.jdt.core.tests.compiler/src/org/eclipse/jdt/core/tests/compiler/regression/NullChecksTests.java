/*******************************************************************************
 * Copyright (c) 2016, 2020 Stephan Herrmann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class NullChecksTests extends AbstractNullAnnotationTest {

	public NullChecksTests(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testAssertNonNull1" };
//			TESTS_NUMBERS = new int[] { 561 };
//			TESTS_RANGE = new int[] { 1, 2049 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public static Class<NullChecksTests> testClass() {
		return NullChecksTests.class;
	}

	public void testAssertNonNull1() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import static org.eclipse.jdt.annotation.Checks.*;\n" +
				"public class X {\n" +
				"	@SuppressWarnings(\"null\")\n" +
				"	static @NonNull String hide(String some) {\n" +
				"		return some;\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		@NonNull String myHiddenNull = hide(null);\n" +
				"		try {\n" +
				"			assertNonNull(\"foo\", myHiddenNull);\n" +
				"		} catch (NullPointerException npe) {\n" +
				"			System.out.println(npe.getMessage());\n" +
				"		}\n" +
				"		try {\n" +
				"			assertNonNullWithMessage(\"Shouldn't!\", \"foo\", myHiddenNull);\n" +
				"		} catch (NullPointerException npe) {\n" +
				"			System.out.println(npe.getMessage());\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"",
			"Value in position 1 must not be null\n" +
			"Shouldn\'t!");
	}

	public void testAssertNonNullElements() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import static org.eclipse.jdt.annotation.Checks.*;\n" +
				"import java.util.*;\n" +
				"public class X {\n" +
				"	@SuppressWarnings(\"null\")\n" +
				"	static @NonNull String hide(String some) {\n" +
				"		return some;\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		@NonNull List<String> myList = new ArrayList<>();\n" +
				"		myList.add(\"foo\");\n" +
				"		myList.add(null);\n" +
				"		try {\n" +
				"			assertNonNullElements(myList);\n" +
				"		} catch (NullPointerException npe) {\n" +
				"			System.out.println(npe.getMessage());\n" +
				"		}\n" +
				"		@NonNull List<@NonNull String> myList2 = new ArrayList<>();\n" +
				"		myList2.add(\"foo\");\n" +
				"		myList2.add(hide(null));\n" +
				"		try {\n" +
				"			assertNonNullElements(myList2, \"Shouldn't!\");\n" +
				"		} catch (NullPointerException npe) {\n" +
				"			System.out.println(npe.getMessage());\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"",
			"Value in position 1 must not be null\n" +
			"Shouldn\'t!");
	}

	public void testRequireNonNull() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import static org.eclipse.jdt.annotation.Checks.*;\n" +
				"public class X {\n" +
				"	@SuppressWarnings(\"null\")\n" +
				"	static @NonNull String hide(String some) {\n" +
				"		return some;\n" +
				"	}\n" +
				"	static void test(@Nullable String str, @Nullable X x) {\n" +
				"		@NonNull String nnStr;\n" +
				"		@NonNull X nnX;\n" +
				"		try {\n" +
				"			nnStr = requireNonNull(str);\n" +
				"			nnX = requireNonNull(null, \"Shouldn\'t!\");\n" +
				"		} catch (NullPointerException npe) {\n" +
				"			System.out.println(npe.getMessage());\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		test(\"foo\", null);\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"",
			"Shouldn\'t!");
	}

	public void testRequireNonEmptyString() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import static org.eclipse.jdt.annotation.Checks.*;\n" +
				"public class X {\n" +
				"	@SuppressWarnings(\"null\")\n" +
				"	static @NonNull String hide(String some) {\n" +
				"		return some;\n" +
				"	}\n" +
				"	static void test(@Nullable String str1, @Nullable String str2) {\n" +
				"		@NonNull String nnStr;\n" +
				"		try {\n" +
				"			nnStr = requireNonEmpty(str1);\n" +
				"		} catch (NullPointerException npe) {\n" +
				"			System.out.println(\"npe:\"+npe.getMessage());\n" +
				"		}\n" +
				"		try {\n" +
				"			nnStr = requireNonEmpty(str2, \"Shouldn't!\");\n" +
				"		} catch (NullPointerException npe) {\n" +
				"			System.out.println(\"npe\"+npe.getMessage());\n" +
				"		} catch (IllegalArgumentException iae) {\n" +
				"			System.out.println(iae.getMessage());\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		test(null, \"\");\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"",
			"npe:null\n" +
			"Shouldn\'t!");
	}

	public void testRequireNonEmptyCollection() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import static org.eclipse.jdt.annotation.Checks.*;\n" +
				"public class X {\n" +
				"	static void test(@Nullable Collection<String> strs, @Nullable Collection<String> strs1, Collection<String> strs2) {\n" +
				"		@NonNull Collection<String> nnStrs;\n" +
				"		try {\n" +
				"			nnStrs = requireNonEmpty(strs);\n" +
				"		} catch (NullPointerException npe) {\n" +
				"			System.out.println(\"NPE:\"+npe.getMessage());\n" +
				"		}\n" +
				"		try {\n" +
				"			nnStrs = requireNonEmpty(strs1);\n" +
				"		} catch (NullPointerException npe) {\n" +
				"			System.out.println(\"npe:\"+npe.getMessage());\n" +
				"		}\n" +
				"		try {\n" +
				"			nnStrs = requireNonEmpty(strs2, \"Shouldn't!\");\n" +
				"		} catch (NullPointerException npe) {\n" +
				"			System.out.println(\"npe\"+npe.getMessage());\n" +
				"		} catch (IllegalArgumentException iae) {\n" +
				"			System.out.println(iae.getMessage());\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		test(Collections.singletonList(\"good\"), null, Collections.emptyList());\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"",
			"npe:null\n" +
			"Shouldn\'t!");
	}

	public void testIsNull() {
		Map<String, String> compilerOptions = getCompilerOptions();
		compilerOptions.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import static org.eclipse.jdt.annotation.Checks.*;\n" +
				"public class X {\n" +
				"	@SuppressWarnings(\"null\")\n" +
				"	static <T> @NonNull T hide(T some) {\n" +
				"		return some;\n" +
				"	}\n" +
				"	static void test(@NonNull X x1, @NonNull X x2, @NonNull X x3) {\n" +
				"		if (isNull(x1))\n" +
				"			System.out.println(\"IS NULL\");\n" +
				"		if (isAnyNull(x2, x1))\n" +
				"			System.out.println(\"IS ANY NULL 1\");\n" +
				"		if (isAnyNull(x2, x3))\n" +
				"			System.out.println(\"IS ANY NULL 2\");\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		test(hide(null), new X(), new X());\n" +
				"	}\n" +
				"}\n"
			},
			compilerOptions,
			"",
			"IS NULL\n" +
			"IS ANY NULL 1");
	}

	public void testAsNullable() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"import static org.eclipse.jdt.annotation.Checks.*;\n" +
				"public class X {\n" +
				"	static void test(Optional<X> xopt) {\n" +
				"		if (xopt != null) {\n" +
				"			X x = asNullable(xopt);\n" +
				"			if (x == null)\n" +
				"				System.out.println(\"NULL\");\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		test(Optional.ofNullable(null));\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"",
			"NULL");
	}

	public void testNonNullElse() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import java.util.function.*;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import static org.eclipse.jdt.annotation.Checks.*;\n" +
				"public class X {\n" +
				"	static void test(String str, String noStr, @NonNull Supplier<@NonNull String> prov) {\n" +
				"		System.out.println(nonNullElse(str, \"ELSE1\"));\n" +
				"		System.out.println(nonNullElse(noStr, \"ELSE2\"));\n" +
				"		System.out.println(nonNullElseGet(str, () -> \"ELSE3\"));\n" +
				"		System.out.println(nonNullElseGet(noStr, prov));\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		test(\"good\", null, () -> \"ELSE4\");\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"",
			"good\n" +
			"ELSE2\n" +
			"good\n" +
			"ELSE4");
	}

	public void _testIfNonNull() { // FIXME: see https://bugs.eclipse.org/489609 - [1.8][null] null annotation on wildcard is dropped during inference
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import static org.eclipse.jdt.annotation.Checks.*;\n" +
				"public class X {\n" +
				"	static void test(@Nullable String str) {\n" +
				"		ifNonNull(str, s -> print(s));\n" +
				"	}\n" +
				"	static void print(@NonNull String s) {\n" +
				"		System.out.print(s);\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		test(\"good\");\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"",
			"good");
	}

	public void testBooleanNullAssertions() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.util.Objects;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X {\n" +
				"	public void demonstrateNotWorkingNullCheck() {\n" +
				"        Object mayBeNull = null;\n" +
				"        if (Math.random() > 0.5) {\n" +
				"            mayBeNull = new Object();\n" +
				"        }\n" +
				"        if (Object.class.isInstance(mayBeNull)) {\n" +
				"            mayBeNull.toString();\n" +
				"        }\n" +
				"    }\n" +
				"	public void negatedNullCheck() {\n" +
				"        Object mayBeNull = null;\n" +
				"        if (Math.random() > 0.5) {\n" +
				"            mayBeNull = new Object();\n" +
				"        }\n" +
				"        if (!Objects.nonNull(mayBeNull)) {\n" +
				"            System.out.println(\"not\");\n" +
				"        } else {\n" +
				"            mayBeNull.toString();\n" +
				"        }\n" +
				"        if (!(Integer.class.isInstance(mayBeNull) || Long.class.isInstance(mayBeNull))) {\n" +
				"            mayBeNull.toString(); // still only a potential problem\n" +
				"        }\n" +
				"    }\n" +
				"	public void nullCheckAlgegra() {\n" +
				"        Object mayBeNull = null;\n" +
				"        if (Math.random() > 0.5) {\n" +
				"            mayBeNull = new Object();\n" +
				"        }\n" +
				"        if (Math.random() > 0.5 && Object.class.isInstance(mayBeNull)) {\n" +
				"            mayBeNull.toString();\n" + // both operands are true
				"        }\n" +
				"        if (!Object.class.isInstance(mayBeNull) || Math.random() > 0.5) {\n" +
				"            System.out.println(\"not\");\n" +
				"        } else {\n" +
				"            mayBeNull.toString();\n" + // both operands are false
				"        }\n" +
				"        if (Object.class.isInstance(mayBeNull) && mayBeNull.equals(\"hi\"))\n" + // second evaluated only when first is true
				"            System.out.println(\"equal\");\n" +
				"        if (Objects.isNull(mayBeNull) || mayBeNull.equals(\"hi\"))\n" + // second evaluated only when first is false
				"            System.out.println(\"equal or null\");\n" +
				"    }\n" +
				"	public void objectsUtils() {\n" +
				"        Object mayBeNull = null;\n" +
				"        if (Math.random() > 0.5) {\n" +
				"            mayBeNull = new Object();\n" +
				"        }\n" +
				"        String s = Objects.nonNull(mayBeNull) ? mayBeNull.toString(): null;\n" +
				"        if (Objects.isNull(mayBeNull) || Math.random() > 0.5) {\n" +
				"            System.out.println(\"not\");\n" +
				"        } else {\n" +
				"            mayBeNull.toString();\n" +
				"        }\n" +
				"    }\n" +
				"	public void loops() {\n" +
				"        Object mayBeNull = null;\n" +
				"        if (Math.random() > 0.5) {\n" +
				"            mayBeNull = new Object();\n" +
				"        }\n" +
				"        for (; Objects.nonNull(mayBeNull); mayBeNull=next(mayBeNull)) {\n" +
				"            mayBeNull.toString();\n" + // guarded by the condition
				"        }\n" +
				"        mayBeNull.toString(); // can only be null after the loop\n" +
				"        Object initiallyNN = new Object();\n" +
				"        while (Objects.nonNull(initiallyNN)) {\n" +
				"            initiallyNN.toString();\n" + // guarded by the condition
				"            initiallyNN = next(initiallyNN);\n" +
				"        }\n" +
				"        initiallyNN.toString(); // can only be null after the loop\n" +
				"    }\n" +
				"    @Nullable Object next(Object o) { return o; }\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 24)\n" +
			"	mayBeNull.toString(); // still only a potential problem\n" +
			"	^^^^^^^^^\n" +
			"Potential null pointer access: The variable mayBeNull may be null at this location\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 65)\n" +
			"	mayBeNull.toString(); // can only be null after the loop\n" +
			"	^^^^^^^^^\n" +
			"Null pointer access: The variable mayBeNull can only be null at this location\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 71)\n" +
			"	initiallyNN.toString(); // can only be null after the loop\n" +
			"	^^^^^^^^^^^\n" +
			"Null pointer access: The variable initiallyNN can only be null at this location\n" +
			"----------\n");
	}
	public void testBug465085_comment12() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
		runConformTest(
			new String[] {
				"Snippet.java",
				"import java.util.Collection;\n" +
				"\n" +
				"public class Snippet {\n" +
				"	int instanceCount(Collection<?> elements, Class<?> clazz) {\n" +
				"		int count = 0;\n" +
				"		for (Object o : elements) {  // warning here: \"The value of the local variable o is not used\"\n" +
				"			if (clazz.isInstance(o)) {\n" +
				"				count++;\n" +
				"			}\n" +
				"		}\n" +
				"		return count;\n" +
				"	}\n" +
				"}\n"
			},
			options);
	}
}
