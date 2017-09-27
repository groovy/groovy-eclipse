/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;

import junit.framework.Test;

/**
 * Regression test for MethodHandle.invokeExact(..)/invokeGeneric(..) invocation
 */
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
				"		Object o = mh.invokeGeneric(new X(), (Object)\"foo:\", i);\n" +
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
				"				handle.invokeGeneric(null);\n" + 
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
				"				handle.invokeGeneric(null);\n" + 
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
				"				handle.invokeGeneric(new Object());\n" + 
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
				"				Object o = handle.invokeGeneric(new Object());\n" + 
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
	public void test008() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_DEPRECATION, JavaCore.ERROR);
		this.runNegativeTest(
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
				"				Object o = handle.invokeGeneric(new Object());\n" + 
				"			} catch (Throwable e) {\n" + 
				"				e.printStackTrace();\n" + 
				"			}\n" + 
				"		} catch (Throwable e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 14)\n" + 
			"	Object o = handle.invokeGeneric(new Object());\n" + 
			"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The method invokeGeneric(Object...) from the type MethodHandle is deprecated\n" + 
			"----------\n",
			null,
			true,
			options);
	}
}
