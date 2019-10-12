package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;

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
}
