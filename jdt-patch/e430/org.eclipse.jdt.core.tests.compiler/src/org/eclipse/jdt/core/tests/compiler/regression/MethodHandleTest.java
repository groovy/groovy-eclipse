/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

/**
 * Regression test for MethodHandle.invokeExact(..)/invokeGeneric(..) invocation
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MethodHandleTest extends AbstractRegressionTest {
	public MethodHandleTest(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_7);
	}

	public static Class testClass() {
		return MethodHandleTest.class;
	}

	static {
//		TESTS_NAMES = new String [] { "test009" };
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.MethodHandle;\n" +
				"import java.lang.invoke.MethodHandles;\n" +
				"import java.lang.invoke.MethodType;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) throws Throwable {\n" +
				"		MethodHandles.Lookup lookup = MethodHandles.lookup();\n" +
				"\n" +
				"		MethodType mt = MethodType.methodType(String.class, String.class, char.class);\n" +
				"		MethodHandle mh = lookup.findStatic(X.class, \"append\", mt);\n" +
				"		String s = (String) mh.invokeExact(\"follo\",'w');\n" +
				"		System.out.println(s);\n" +
				"\n" +
				"		mt = MethodType.methodType(int.class, Object[].class);\n" +
				"		mh = lookup.findVirtual(X.class, \"arrayLength\", mt);\n" +
				"		int i = (int) mh.invokeExact(new X(), new Object[] {1, 'A', \"foo\"});\n" +
				"		System.out.println(i);\n" +
				"\n" +
				"		mt = MethodType.methodType(void.class, String.class);\n" +
				"		mh = lookup.findStatic(X.class, \"hello\", mt);\n" +
				"		mh.invokeExact(\"world\");\n" +
				"\n" +
				"		mt = MethodType.methodType(Object.class, String.class, int.class);\n" +
				"		mh = lookup.findVirtual(X.class, \"foo\", mt);\n" +
				"		Object o = mh.invoke(new X(), (Object)\"foo:\", i);\n" +
				"\n" +
				"		mt = MethodType.methodType(void.class);\n" +
				"		mh = lookup.findStatic(X.class, \"bar\", mt);\n" +
				"		mh.invokeExact();\n" +
				"	}\n" +
				"	public static void bar() {\n" +
				"		System.out.println(\"bar\");\n" +
				"	}\n" +
				"	public Object foo(String s, int i) {\n" +
				"		System.out.println(s + i);\n" +
				"		return s + i;\n" +
				"	}\n" +
				"	public static String append(String s, char c) {\n" +
				"		return s + c;\n" +
				"	}\n" +
				"	public int arrayLength(Object[] array) {\n" +
				"		return array.length;\n" +
				"	}\n" +
				"	public static void hello(String name) {\n" +
				"		System.out.println(\"Hello, \"+ name);\n" +
				"	}\n" +
				"}"
			},
			"follow\n" +
			"3\n" +
			"Hello, world\n" +
			"foo:3\n" +
			"bar");
	}
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.MethodHandle;\n" +
				"import java.lang.invoke.MethodHandles;\n" +
				"import java.lang.invoke.MethodType;\n" +
				"import java.lang.invoke.WrongMethodTypeException;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void foo() {\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			MethodHandle handle = MethodHandles.lookup().findStatic(X.class, \"foo\", MethodType.methodType(void.class));\n" +
				"			try {\n" +
				"				handle.invoke(null);\n" +
				"			} catch (WrongMethodTypeException ok) {\n" +
				"				System.out.println(\"This is ok\");\n" +
				"			} catch (Throwable e) {\n" +
				"				e.printStackTrace();\n" +
				"			}\n" +
				"		} catch (Throwable e) {\n" +
				"			e.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"This is ok");
	}
	public void test003() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.MethodHandle;\n" +
				"import java.lang.invoke.MethodHandles;\n" +
				"import java.lang.invoke.MethodType;\n" +
				"import java.lang.invoke.WrongMethodTypeException;\n" +
				"\n" +
				"public class X {\n" +
				"	public static <T> T foo(T param){\n" +
				"		return null;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			MethodHandle handle = MethodHandles.lookup().findStatic(X.class, \"foo\", MethodType.methodType(Object.class, Object.class));\n" +
				"			try {\n" +
				"				handle.invoke(null);\n" +
				"			} catch (Throwable e) {\n" +
				"				e.printStackTrace();\n" +
				"			}\n" +
				"		} catch (Throwable e) {\n" +
				"			e.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"");
	}
	public void test004() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.MethodHandle;\n" +
				"import java.lang.invoke.MethodHandles;\n" +
				"import java.lang.invoke.MethodType;\n" +
				"import java.lang.invoke.WrongMethodTypeException;\n" +
				"\n" +
				"public class X {\n" +
				"	public static <T> T foo(T param){\n" +
				"		return null;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			MethodHandle handle = MethodHandles.lookup().findStatic(X.class, \"foo\", MethodType.methodType(Object.class, Object.class));\n" +
				"			try {\n" +
				"				handle.invoke(new Object());\n" +
				"			} catch (Throwable e) {\n" +
				"				e.printStackTrace();\n" +
				"			}\n" +
				"		} catch (Throwable e) {\n" +
				"			e.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"");
	}
	public void test005() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.MethodHandle;\n" +
				"import java.lang.invoke.MethodHandles;\n" +
				"import java.lang.invoke.MethodType;\n" +
				"import java.lang.invoke.WrongMethodTypeException;\n" +
				"\n" +
				"public class X {\n" +
				"	public static <T> T foo(T param){\n" +
				"		return null;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			MethodHandle handle = MethodHandles.lookup().findStatic(X.class, \"foo\", MethodType.methodType(Object.class, Object.class));\n" +
				"			try {\n" +
				"				Object o = handle.invoke(new Object());\n" +
				"			} catch (Throwable e) {\n" +
				"				e.printStackTrace();\n" +
				"			}\n" +
				"		} catch (Throwable e) {\n" +
				"			e.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"");
	}
	public void test006() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.MethodHandle;\n" +
				"import java.lang.invoke.MethodHandles;\n" +
				"import java.lang.invoke.MethodType;\n" +
				"import java.lang.invoke.WrongMethodTypeException;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) throws Throwable {\n" +
				"		MethodHandles.Lookup lookup = MethodHandles.lookup();\n" +
				"\n" +
				"		MethodType mt = MethodType.methodType(String.class, String.class, char.class);\n" +
				"		MethodHandle mh = lookup.findStatic(X.class, \"append\", mt);\n" +
				"		String s = (String) mh.invokeExact(\"follo\",'w');\n" +
				"		System.out.println(s);\n" +
				"		MethodType mt2 = MethodType.methodType(String.class, String.class, char.class);\n" +
				"		MethodHandle mh2 = lookup.findStatic(X.class, \"append\", mt2);\n" +
				"		try {\n" +
				"			mh2.invokeExact(\"follo\",'w');\n" +
				"		} catch(WrongMethodTypeException e) {\n" +
				"			System.out.println(\"Expected exception\");\n" +
				"		}\n" +
				"	}\n" +
				"	public static String append(String s, char c) {\n" +
				"		return s + c;\n" +
				"	}\n" +
				"}"
			},
			"follow\n" +
			"Expected exception");
	}
	public void test007() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import static java.lang.invoke.MethodHandles.lookup;\n" +
				"import static java.lang.invoke.MethodType.methodType;\n" +
				"import java.lang.invoke.MethodHandle;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) throws Throwable {\n" +
				"		MethodHandle fooMH = lookup().findStatic(X.class, \"foo\", methodType(String.class));\n" +
				"		String s = (String) fooMH.invokeExact();\n" +
				"		System.out.println(s);\n" +
				"		fooMH.asType(methodType(void.class)).invokeExact();\n" +
				"	}\n" +
				"	public static String foo() {\n" +
				"		System.out.println(\"Inside foo\");\n" +
				"		return \"foo\";\n" +
				"	}\n" +
				"}"
			},
			"Inside foo\n" +
			"foo\n" +
			"Inside foo");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=386259, wrong unnecessary cast warning.
	public void test009() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
		runNegativeTest(
				// test directory preparation
				true /* flush output directory */,
				new String[] { /* test files */
						"X.java",
						"import java.lang.invoke.MethodHandle;\n" +
						"import java.lang.invoke.MethodHandles;\n" +
						"import java.lang.invoke.MethodType;\n" +
						"public class X {\n" +
						"  public static void main(String[] args) throws Throwable {\n" +
						"    String str = \"test\";\n" +
						"    MethodHandle mh = MethodHandles.lookup().findVirtual(String.class, \"toString\", \n" +
						"        MethodType.methodType(String.class));\n" +
						"    String actual = (String) mh.invoke(str);\n" +
						"    assert \"test\".equals(actual);\n" +
						"    Zork z;\n" +
						"  }\n" +
						"}\n"
				},
				// compiler options
				null /* no class libraries */,
				customOptions /* custom options */,
				// compiler results
				"----------\n" + /* expected compiler log */
				"1. ERROR in X.java (at line 11)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n",
				// javac options
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=386259 variation.
	public void test010() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
		runNegativeTest(
				// test directory preparation
				true /* flush output directory */,
				new String[] { /* test files */
						"X.java",
						"import java.lang.invoke.MethodHandle;\n" +
						"import java.lang.invoke.MethodHandles;\n" +
						"import java.lang.invoke.MethodType;\n" +
						"public class X {\n" +
						"  public static void main(String[] args) throws Throwable {\n" +
						"    String str = \"test\";\n" +
						"    MethodHandle mh = MethodHandles.lookup().findVirtual(String.class, \"toString\", \n" +
						"        MethodType.methodType(String.class));\n" +
						"    Object actual = (Object) mh.invoke(str);\n" +
						"    assert \"test\".equals(actual);\n" +
						"    Zork z;\n" +
						"  }\n" +
						"}\n"
				},
				// compiler options
				null /* no class libraries */,
				customOptions /* custom options */,
				// compiler results
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	Object actual = (Object) mh.invoke(str);\n" +
				"	                ^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Unnecessary cast from Object to Object\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 11)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n",
				// javac options
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=466748
	public void test011() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.MethodHandle;\n" +
				"import java.lang.invoke.MethodHandles;\n" +
				"import java.lang.reflect.Method;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void test1(Integer i){\n" +
				"		System.out.println(\"test1:\" + i);\n" +
				"	}\n" +
				"	public static void test2(int i){\n" +
				"		System.out.println(\"test2:\" + i);\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args) throws Throwable{\n" +
				"		Method m1 = X.class.getMethod(\"test1\", Integer.class);\n" +
				"		Method m2 = X.class.getMethod(\"test2\", int.class);\n" +
				"\n" +
				"		MethodHandle test1Handle = MethodHandles.lookup().unreflect(m1);\n" +
				"		MethodHandle test2Handle = MethodHandles.lookup().unreflect(m2);\n" +
				"		\n" +
				"		Integer arg_Integer = 1;\n" +
				"		int arg_int = 1;\n" +
				"		\n" +
				"		// results in a java.lang.VerifyError - but should work without error\n" +
				"		test1Handle.invokeExact(Integer.class.cast(arg_int));\n" +
				"		\n" +
				"		// The following line also results in a java.lang.VerifyError, but should actually throw a ClassCastException\n" +
				"		try {\n" +
				"			test2Handle.invokeExact(int.class.cast(arg_Integer)); \n" +
				"		} catch(ClassCastException e) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"test1:1\n" +
			"SUCCESS");
	}
}
