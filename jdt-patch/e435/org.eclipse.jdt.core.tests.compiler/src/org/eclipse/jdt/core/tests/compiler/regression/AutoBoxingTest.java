/*******************************************************************************
 * Copyright (c) 2005, 2021 IBM Corporation and others.
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

import java.io.File;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AutoBoxingTest extends AbstractComparableTest {

	public AutoBoxingTest(String name) {
		super(name);
	}

	@Override
	protected Map getCompilerOptions() {
		Map defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
		return defaultOptions;
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 78 };
//		TESTS_RANGE = new int[] { 151, -1 };
	}
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return AutoBoxingTest.class;
	}

	public void test001() { // constant cases of base type -> Number
		// int -> Integer
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(1);\n" +
				"	}\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// byte -> Byte
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test((byte)127);\n" +
				"	}\n" +
				"	public static void test(Byte b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// char -> Character
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test('b');\n" +
				"	}\n" +
				"	public static void test(Character c) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// float -> Float
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(-0.0f);\n" +
				"	}\n" +
				"	public static void test(Float f) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// double -> Double
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(0.0);\n" +
				"	}\n" +
				"	public static void test(Double d) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// long -> Long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(Long.MAX_VALUE);\n" +
				"	}\n" +
				"	public static void test(Long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// short -> Short
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(Short.MAX_VALUE);\n" +
				"	}\n" +
				"	public static void test(Short s) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// boolean -> Boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(false);\n" +
				"	}\n" +
				"	public static void test(Boolean b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test002() { // non constant cases of base type -> Number
		// int -> Integer
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// byte -> Byte
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static byte bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Byte b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// char -> Character
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static char bar() {return 'c';}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Character c) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// float -> Float
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static float bar() {return 0.0f;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Float f) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// double -> Double
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static double bar() {return 0.0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Double d) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// long -> Long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static long bar() {return 0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// short -> Short
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static short bar() {return 0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Short s) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// boolean -> Boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static boolean bar() {return true;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Boolean b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test003() { // Number -> base type
		// Integer -> int
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Integer(1));\n" +
				"	}\n" +
				"	public static void test(int i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Byte -> byte
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Byte((byte) 1));\n" +
				"	}\n" +
				"	public static void test(byte b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Byte -> long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Byte((byte) 1));\n" +
				"	}\n" +
				"	public static void test(long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Character -> char
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Character('c'));\n" +
				"	}\n" +
				"	public static void test(char c) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Float -> float
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Float(0.0f));\n" +
				"	}\n" +
				"	public static void test(float f) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Double -> double
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Double(0.0));\n" +
				"	}\n" +
				"	public static void test(double d) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Long -> long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Long(0L));\n" +
				"	}\n" +
				"	public static void test(long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Short -> short
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Short((short) 0));\n" +
				"	}\n" +
				"	public static void test(short s) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Boolean -> boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(Boolean.TRUE);\n" +
				"	}\n" +
				"	public static void test(boolean b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test004() { // autoboxing method is chosen over private exact match & visible varargs method
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(int i) { System.out.print('n'); }\n" +
				"	static void test(int... i) { System.out.print('n'); }\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private void test(int i) { System.out.print('n'); }\n" +
				"	void test(int... i) { System.out.print('n'); }\n" +
				"	public void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test005() { // this is NOT an ambiguous case as 'long' is matched before autoboxing kicks in
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i) { System.out.print('n'); }\n" +
				"	void test(long i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test006() {
		this.runNegativeTest( // Integers are not compatible with Longs, even though ints are compatible with longs
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Long i, int j) { System.out.print('n'); }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new Y().test(1, 1);\n" +
			"	        ^^^^\n" +
			"The method test(Long, int) in the type Y is not applicable for the arguments (int, int)\n" +
			"----------\n"
			// test(java.lang.Long,int) in Y cannot be applied to (int,int)
		);
		this.runNegativeTest( // likewise with Byte and Integer
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test((byte) 1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) { System.out.print('n'); }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new Y().test((byte) 1, 1);\n" +
			"	        ^^^^\n" +
			"The method test(Integer, int) in the type Y is not applicable for the arguments (byte, int)\n" +
			"----------\n"
			// test(java.lang.Integer,int) in Y cannot be applied to (byte,int)
		);
	}

	public void test007() {
		this.runConformTest( // this is NOT an ambiguous case as Long is not a match for int
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Long i, int j) { System.out.print('n'); }\n" +
				"	void test(long i, Integer j) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test008() { // test autoboxing AND varargs method match
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(1, new Integer(2), -3);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void test(int ... i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test009() {
		this.runNegativeTest( // 2 of these sends are ambiguous
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@SuppressWarnings(\"deprecation\")\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" + // reference to test is ambiguous, both method test(java.lang.Integer,int) in Y and method test(int,java.lang.Integer) in Y match
				"		new Y().test(Integer.valueOf(1), Integer.valueOf(1));\n" + // reference to test is ambiguous
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) {}\n" +
				"	void test(int i, Integer j) {}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	new Y().test(1, 1);\n" +
			"	        ^^^^\n" +
			"The method test(Integer, int) is ambiguous for the type Y\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	new Y().test(Integer.valueOf(1), Integer.valueOf(1));\n" +
			"	        ^^^^\n" +
			"The method test(Integer, int) is ambiguous for the type Y\n" +
			"----------\n"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(new Integer(1), 1);\n" +
				"		new Y().test(1, new Integer(1));\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) { System.out.print(1); }\n" +
				"	void test(int i, Integer j) { System.out.print(2); }\n" +
				"}\n",
			},
			"12"
		);
	}

	public void test010() { // local declaration assignment tests
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		int i = Y.test();\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static Byte test() { return new Byte((byte) 1); }\n" +
				"}\n",
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Object o = Y.test();\n" +
				"		System.out.print(o);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int test() { return 1; }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test011() { // field declaration assignment tests
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static int i = Y.test();\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static Byte test() { return new Byte((byte) 1); }\n" +
				"}\n",
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static Object o = Y.test();\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print(o);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int test() { return 1; }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test012() { // varargs and autoboxing
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer x = new Integer(15); \n" +
				"		int y = 32;\n" +
				"		System.out.printf(\"%x + %x\", x, y);\n" +
				"	}\n" +
				"}",
			},
			"f + 20"
		);
	}

	public void test013() { // foreach and autoboxing
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
				"		for (final Integer e : tab) {\n" +
				"			System.out.print(e);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"123456789"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer[] tab = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
				"		for (final int e : tab) {\n" +
				"			System.out.print(e);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"123456789"
		);
	}

	public void test014() { // switch
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Integer i = new Integer(1);\n" +
				"		switch(i) {\n" +
				"			case 1 : System.out.print('y');\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test015() { // return statement
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static Integer foo1() {\n" +
				"		return 0;\n" +
				"	}\n" +
				"	static int foo2() {\n" +
				"		return new Integer(0);\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(foo1());\n" +
				"		System.out.println(foo2());\n" +
				"	}\n" +
				"}\n",
			},
			"00"
		);
	}

	public void test016() { // conditional expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = args.length == 0 ? 0 : new Integer(1);\n" +
				"		System.out.println(i);\n" +
				"	}\n" +
				"}\n",
			},
			"0"
		);
	}

	public void test017() { // cast expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = new Integer(1);\n" +
				"		System.out.println((int)i);\n" +
				"	}\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test018() { // cast expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@SuppressWarnings(\"deprecation\")\n" +
				"	public static void main(String[] args) {\n" +
				"		Float f = args.length == 0 ? Float.valueOf(0) : 0;\n" +
				"		System.out.println((int)f);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	Float f = args.length == 0 ? Float.valueOf(0) : 0;\n" +
			"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The expression of type float is boxed into Float\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	Float f = args.length == 0 ? Float.valueOf(0) : 0;\n" +
			"	                             ^^^^^^^^^^^^^^^^\n" +
			"The expression of type Float is unboxed into float\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	System.out.println((int)f);\n" +
			"	                   ^^^^^^\n" +
			"Cannot cast from Float to int\n" +
			"----------\n");
	}

	public void test019() { // cast expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println((Integer) 0);\n" +
				"		System.out.println((Float) 0);\n" +
				"		\n" +
				"	}\n" +
				"}\n",
			},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	System.out.println((Integer) 0);\n" +
		"	                             ^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	System.out.println((Float) 0);\n" +
		"	                   ^^^^^^^^^\n" +
		"Cannot cast from int to Float\n" +
		"----------\n");
	}

	public void test020() { // binary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"      System.out.println(2 + b);\n" +
				"    }\n" +
				"}\n",
			},
			"3"
		);
	}

	public void test021() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    Integer i = +b + (-b);\n" +
				"		System.out.println(i);\n" +
				"    }\n" +
				"}\n",
			},
			"0"
		);
	}

	public void test022() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    Integer i = 0;\n" +
				"	    int n = b + i;\n" +
				"		System.out.println(n);\n" +
				"    }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test023() { // 78849
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Character cValue = new Character('c');\n" +
				"		if ('c' == cValue) System.out.println('y');\n" +
				"	}\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test024() { // 79254
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) { test(2); }\n" +
				"	static void test(Object o) { System.out.println('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test025() { // 79641
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) { test(true); }\n" +
				"	static void test(Object ... o) { System.out.println('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test026() { // compound assignment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    Integer i = 0;\n" +
				"	    i += b;\n" +
				"		System.out.println(i);\n" +
				"    }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test027() { // equal expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		if (0 == new X()) {\n" +
				"			System.out.println();\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	if (0 == new X()) {\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Incompatible operand types int and X\n" +
			"----------\n"
		);
	}

	public void test028() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    int i = +b;\n" +
				"		System.out.println(i);\n" +
				"    }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test029() { // generic type case
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	sum += iterator.next();\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}

	public void test030() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		Boolean b = Boolean.TRUE;\n" +
				"		\n" +
				"		if (b && !b) {\n" +
				"			System.out.print(\"THEN\");\n" +
				"		} else {\n" +
				"			System.out.print(\"ELSE\");\n" +
				"		}\n" +
				"    }\n" +
				"}",
			},
			"ELSE"
		);
	}

	public void test031() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Boolean foo() { return Boolean.FALSE; }\n" +
				"	public static void main(String[] args) {\n" +
				"		Boolean b = foo();\n" +
				"		\n" +
				"		if (!b) {\n" +
				"			System.out.print(\"THEN\");\n" +
				"		} else {\n" +
				"			System.out.print(\"ELSE\");\n" +
				"		}\n" +
				"    }\n" +
				"}",
			},
			"THEN"
		);
	}

	public void test032() throws Exception { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   public static void main(String[] args) {\n" +
				"      if (new Integer(1) == new Integer(0)) {\n" +
				"         System.out.println();\n" +
				"      }\n" +
				"      System.out.print(\"SUCCESS\");\n" +
				"   }\n" +
				"}",
			},
			"SUCCESS"
		);

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 4, Locals: 1\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  new java.lang.Integer [16]\n" +
			"     3  dup\n" +
			"     4  iconst_1\n" +
			"     5  invokespecial java.lang.Integer(int) [18]\n" +
			"     8  new java.lang.Integer [16]\n" +
			"    11  dup\n" +
			"    12  iconst_0\n" +
			"    13  invokespecial java.lang.Integer(int) [18]\n" +
			"    16  if_acmpne 25\n" +
			"    19  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
			"    22  invokevirtual java.io.PrintStream.println() : void [27]\n" +
			"    25  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
			"    28  ldc <String \"SUCCESS\"> [32]\n" +
			"    30  invokevirtual java.io.PrintStream.print(java.lang.String) : void [34]\n" +
			"    33  return\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test033() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   public static void main(String[] s) {\n" +
				"      System.out.print(Boolean.TRUE || Boolean.FALSE);\n" +
				"   }\n" +
				"}",
			},
			"true"
		);
	}

	public void test034() { // postfix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    int i = b++;\n" +
				"		System.out.print(i);\n" +
				"		System.out.print(b);\n" +
				"    }\n" +
				"}\n",
			},
			"12"
		);
	}

	public void test035() { // postfix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    int i = b--;\n" +
				"		System.out.print(i);\n" +
				"		System.out.print(b);\n" +
				"    }\n" +
				"}\n",
			},
			"10"
		);
	}

	public void test036() { // prefix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    int i = ++b;\n" +
				"		System.out.print(i);\n" +
				"		System.out.print(b);\n" +
				"    }\n" +
				"}\n",
			},
			"22"
		);
	}

	public void test037() { // prefix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    int i = --b;\n" +
				"		System.out.print(i);\n" +
				"		System.out.print(b);\n" +
				"    }\n" +
				"}\n",
			},
			"00"
		);
	}

	public void test038() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static boolean foo() { return false; }\n" +
				"   public static void main(String[] s) {\n" +
				"		boolean b = foo();\n" +
				"      System.out.print(b || Boolean.FALSE);\n" +
				"   }\n" +
				"}",
			},
			"false"
		);
	}

	public void test039() { // equal expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 0;\n" +
				"		if (i != null) {\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if (i != null) {\n" +
			"	    ^^^^^^^^^\n" +
			"The operator != is undefined for the argument type(s) int, null\n" +
			"----------\n"
		);
	}

	public void test040() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = new Integer(1);\n" +
				"		if (i == null)\n" +
				"			i++;\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}",
			},
			"1"
		);
	}

	public void test041() { // equal expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = 0;\n" +
				"		if (i != null) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS"
		);
	}

	public void test042() { // conditional expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Boolean bar() { return Boolean.TRUE; } \n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = bar() ? new Integer(1) : null;\n" +
				"		int j = i;\n" +
				"		System.out.print(j);\n" +
				"	}\n" +
				"}",
			},
			"1"
		);
	}

	public void test043() { // compound assignment
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = 0;\n" +
				"		i += \"aaa\";\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Integer i = 0;\n" +
			"	            ^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	i += \"aaa\";\n" +
			"	^^^^^^^^^^\n" +
			"The operator += is undefined for the argument type(s) Integer, String\n" +
			"----------\n");
	}

	public void test044() { // compound assignment
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = 0;\n" +
				"		i += null;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Integer i = 0;\n" +
			"	            ^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	i += null;\n" +
			"	^^^^^^^^^\n" +
			"The operator += is undefined for the argument type(s) Integer, null\n" +
			"----------\n");
	}

	public void test045() { // binary expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = 0;\n" +
				"		i = i + null;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Integer i = 0;\n" +
			"	            ^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	i = i + null;\n" +
			"	    ^^^^^^^^\n" +
			"The operator + is undefined for the argument type(s) Integer, null\n" +
			"----------\n");
	}

	public void test046() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Byte b = new Byte((byte)1);\n" +
				"		b++;\n" +
				"		System.out.println((Byte)b);\n" +
				"	}\n" +
				"}\n",
			},
			"2");
	}

	public void test047() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Byte b = new Byte((byte)1);\n" +
				"		b++;\n" +
				"		if (b instanceof Byte) {\n" +
				"			System.out.println(\"SUCCESS\" + b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS2");
	}

	public void test048() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Byte b = new Byte((byte)1);\n" +
				"	public static void main(String[] s) {\n" +
				"		b++;\n" +
				"		if (b instanceof Byte) {\n" +
				"			System.out.print(\"SUCCESS\" + b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS2");
	}

	public void test049() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static class Y {\n" +
				"		public static Byte b = new Byte((byte)1);\n" +
				"	}\n" +
				"	public static void main(String[] s) {\n" +
				"		X.Y.b++;\n" +
				"		if (X.Y.b instanceof Byte) {\n" +
				"			System.out.print(\"SUCCESS\" + X.Y.b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS2");
	}

	public void test050() { // prefix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Byte b = new Byte((byte)1);\n" +
				"	public static void main(String[] s) {\n" +
				"		++b;\n" +
				"		if (b instanceof Byte) {\n" +
				"			System.out.print(\"SUCCESS\" + b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS2");
	}

	public void test051() { // prefix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static class Y {\n" +
				"		public static Byte b = new Byte((byte)1);\n" +
				"	}\n" +
				"	public static void main(String[] s) {\n" +
				"		++X.Y.b;\n" +
				"		if (X.Y.b instanceof Byte) {\n" +
				"			System.out.print(\"SUCCESS\" + X.Y.b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS2");
	}

	public void test052() { // boxing in var decl
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Byte b = 0;\n" +
				"		++b;\n" +
				"		foo(0);\n" +
				"	}\n" +
				"	static void foo(Byte b) {\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Byte b = 0;\n" +
			"	         ^\n" +
			"The expression of type int is boxed into Byte\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	++b;\n" +
			"	^^^\n" +
			"The expression of type byte is boxed into Byte\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 4)\n" +
			"	++b;\n" +
			"	  ^\n" +
			"The expression of type Byte is unboxed into int\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	foo(0);\n" +
			"	^^^\n" +
			"The method foo(Byte) in the type X is not applicable for the arguments (int)\n" +
			"----------\n");
	}

	public void test053() { // boxing in var decl
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Byte b = 1;\n" +
				"		++b;\n" +
				"		if (b instanceof Byte) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test054() { // boxing in field decl
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static Byte b = 1;\n" +
				"	public static void main(String[] s) {\n" +
				"		++b;\n" +
				"		if (b instanceof Byte) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test055() { // boxing in foreach
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		byte[] bytes = {0, 1, 2};\n" +
				"		for(Integer i : bytes) {\n" +
				"			System.out.print(i);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	for(Integer i : bytes) {\n" +
			"	                ^^^^^\n" +
			"Type mismatch: cannot convert from element type byte to Integer\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	for(Integer i : bytes) {\n" +
			"	                ^^^^^\n" +
			"The expression of type byte is boxed into Integer\n" +
			"----------\n");
	}

	public void test056() { // boxing in foreach
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		int[] ints = {0, 1, 2};\n" +
				"		for(Integer i : ints) {\n" +
				"			System.out.print(i);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"012");
	}

	public void test057() { // boxing in foreach
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		byte[] bytes = {0, 1, 2};\n" +
				"		for(Byte b : bytes) {\n" +
				"			System.out.print(b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"012");
	}

	public void test058() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (Integer i : list) {\n" +
				"	    	sum += i;\n" +
				"	    }	    \n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}\n",
			},
			"10");
	}

	public void test059() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	if (1 == iterator.next()) {\n" +
				"	    		System.out.println(\"SUCCESS\");\n" +
				"	    		break;\n" +
				"	    	}\n" +
				"	    }\n" +
				"    }\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test060() { // autoboxing and boolean expr
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Boolean> list = new ArrayList<Boolean>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i % 2 == 0);\n" +
				"	    }\n" +
				"	    for (Iterator<Boolean> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	if (iterator.next()) {\n" +
				"	    		System.out.println(\"SUCCESS\");\n" +
				"	    		break;\n" +
				"	    	}\n" +
				"	    }\n" +
				"    }\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test061() { // autoboxing and boolean expr
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Boolean> list = new ArrayList<Boolean>();\n" +
				"		boolean b = true;\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add((i % 2 == 0) && b);\n" +
				"	    }\n" +
				"	    for (Iterator<Boolean> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	if (iterator.next()) {\n" +
				"	    		System.out.println(\"SUCCESS\");\n" +
				"	    		break;\n" +
				"	    	}\n" +
				"	    }\n" +
				"    }\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test062() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		boolean b = true;\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"		int sum = 0;\n" +
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	sum = sum + iterator.next();\n" +
				"	    }\n" +
				"	    System.out.println(sum);\n" +
				"    }\n" +
				"}\n",
			},
			"10");
	}

	public void test063() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		boolean b = true;\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"		int val = 0;\n" +
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	val = ~ iterator.next();\n" +
				"	    }\n" +
				"	    System.out.println(val);\n" +
				"    }\n" +
				"}\n",
			},
			"-5");
	}

	public void test064() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		boolean b = true;\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"		int val = 0;\n" +
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	val += (int) iterator.next();\n" +
				"	    }\n" +
				"	    System.out.println(val);\n" +
				"    }\n" +
				"}\n",
			},
			"10");
	}

	public void test065() { // generic type case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (int i : list) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}

	public void test066() { // array case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer[] tab = new Integer[] {0, 1, 2, 3, 4};\n" +
				"	    int sum = 0;\n" +
				"	    for (int i : tab) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}

	public void test067() { // array case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		int[] tab = new int[] {0, 1, 2, 3, 4};\n" +
				"	    int sum = 0;\n" +
				"	    for (Integer i : tab) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}

	public void test068() { // generic type case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (Integer i : list) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}

	public void test069() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Boolean bool = true;\n" +
				"		assert bool : \"failed\";\n" +
				"	    System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test070() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Boolean> lb = new ArrayList<Boolean>();\n" +
				"		lb.add(true);\n" +
				"		Iterator<Boolean> iterator = lb.iterator();\n" +
				"		assert iterator.next() : \"failed\";\n" +
				"	    System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test071() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Boolean> lb = new ArrayList<Boolean>();\n" +
				"		lb.add(true);\n" +
				"		Iterator<Boolean> iterator = lb.iterator();\n" +
				"		assert args != null : iterator.next();\n" +
				"	    System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81971
	public void test072() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        doFoo(getVoid());\n" +
				"    }\n" +
				"\n" +
				"    private static void doFoo(Object o) { }\n" +
				"\n" +
				"    private static void getVoid() { }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	doFoo(getVoid());\n" +
			"	^^^^^\n" +
			"The method doFoo(Object) in the type X is not applicable for the arguments (void)\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81571
	public void test073() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@SuppressWarnings(\"deprecation\")\n" +
				"    public static void main(String[] args) {\n" +
				"        a(Integer.valueOf(1), 2);\n" +
				"    }\n" +
				"    public static void a(int a, int b) { System.out.println(\"SUCCESS\"); }\n" +
				"    public static void a(Object a, Object b) {}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	a(Integer.valueOf(1), 2);\n" +
			"	^\n" +
			"The method a(int, int) is ambiguous for the type X\n" +
			"----------\n"
			// a is ambiguous, both method a(int,int) in X and method a(java.lang.Object,java.lang.Object) in X match
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432
	public void test074() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" Object e() {\n" +
				"  return \"\".compareTo(\"\") > 0;\n" +
				" }\n" +
				" public static void main(String[] args) {\n" +
				"  System.out.print(new X().e());\n" +
				" }\n" +
				"}",
			},
			"false");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432 - variation
	public void test075() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" Object e() {\n" +
				"  return \"\".compareTo(\"\") > 0;\n" +
				" }\n" +
				" public static void main(String[] args) {\n" +
				"  System.out.print(new X().e());\n" +
				" }\n" +
				" Zork z;\n" +
				"}",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	return \"\".compareTo(\"\") > 0;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432 - variation
	public void test076() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" Object e() {\n" +
				" int i = 12; \n" +
				"  boolean b = false;\n" +
				"  switch(i) {\n" +
				"    case 0: return i > 0;\n" +
				"    case 1: return i >= 0;\n" +
				"    case 2: return i < 0;\n" +
				"    case 3: return i <= 0;\n" +
				"    case 4: return i == 0;\n" +
				"    case 5: return i != 0;\n" +
				"    case 6: return i & 0;\n" +
				"    case 7: return i ^ 0;\n" +
				"    case 8: return i | 0;\n" +
				"    case 9: return b && b;\n" +
				"    default: return b || b;\n" +
				"  }\n" +
				" }\n" +
				" public static void main(String[] args) {\n" +
				"  System.out.print(new X().e());\n" +
				" }\n" +
				" Zork z;\n" +
				"}",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 6)\n" +
			"	case 0: return i > 0;\n" +
			"	               ^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 7)\n" +
			"	case 1: return i >= 0;\n" +
			"	               ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 8)\n" +
			"	case 2: return i < 0;\n" +
			"	               ^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 9)\n" +
			"	case 3: return i <= 0;\n" +
			"	               ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 10)\n" +
			"	case 4: return i == 0;\n" +
			"	               ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 11)\n" +
			"	case 5: return i != 0;\n" +
			"	               ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 12)\n" +
			"	case 6: return i & 0;\n" +
			"	               ^^^^^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 13)\n" +
			"	case 7: return i ^ 0;\n" +
			"	               ^^^^^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"9. WARNING in X.java (at line 14)\n" +
			"	case 8: return i | 0;\n" +
			"	               ^^^^^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"10. WARNING in X.java (at line 15)\n" +
			"	case 9: return b && b;\n" +
			"	               ^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"11. WARNING in X.java (at line 15)\n" +
			"	case 9: return b && b;\n" +
			"	               ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"12. WARNING in X.java (at line 16)\n" +
			"	default: return b || b;\n" +
			"	                ^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"13. WARNING in X.java (at line 16)\n" +
			"	default: return b || b;\n" +
			"	                ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"14. ERROR in X.java (at line 22)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432 - variation
	public void test077() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" Object e() {\n" +
				" int i = 12; \n" +
				"  boolean b = false;\n" +
				"  switch(i) {\n" +
				"    case 0: return i > 0;\n" +
				"    case 1: return i >= 0;\n" +
				"    case 2: return i < 0;\n" +
				"    case 3: return i <= 0;\n" +
				"    case 4: return i == 0;\n" +
				"    case 5: return i != 0;\n" +
				"    case 6: return i & 0;\n" +
				"    case 7: return i ^ 0;\n" +
				"    case 8: return i | 0;\n" +
				"    case 9: return b && b;\n" +
				"    default: return b || b;\n" +
				"  }\n" +
				" }\n" +
				" public static void main(String[] args) {\n" +
				"  System.out.print(new X().e());\n" +
				" }\n" +
				"}",
			},
			"false");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81923
	public void test078() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	public <A extends T> X(A... t) {}\n" +
				"	<T> void foo(T... t) {}\n" +
				"	<T> void zip(T t) {}\n" +
				"	void test() {\n" +
				"		new X<Integer>(10, 20);\n" +
				"		foo(10);\n" +
				"		foo(10, 20);\n" +
				"		zip(10);\n" +
				"	}\n" +
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82407
	public void _test079() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"public class X {\n" +
				"	static HashMap<Character, Character> substitutionList(String s1, String s2) {\n" +
				"		HashMap<Character, Character> subst = new HashMap<Character, Character>();\n" +
				"		for (int i = 0; i < s1.length(); i++) {\n" +
				"			char key = s1.charAt(i);\n" +
				"			char value = s2.charAt(i);\n" +
				"			if (subst.containsKey(key)) {\n" +
				"				if (value != subst.get(key)) {\n" +
				"					return null;\n" +
				"				}\n" +
				"			} else if (subst.containsValue(value)) {\n" +
				"				return null;\n" +
				"			} else {\n" +
				"				subst.put(key, value);\n" +
				"			}\n" +
				"		}\n" +
				"		return subst;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"Bogon\");\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82407 - variation
	public void test080() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		HashMap<Character, Character> subst = new HashMap<Character, Character>();\n" +
				"		subst.put(\'a\', \'a\');\n" +
				"		if (\'a\' == subst.get(\'a\')) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82407 - variation
	public void test081() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		HashMap<Byte, Byte> subst = new HashMap<Byte, Byte>();\n" +
				"		subst.put((byte)1, (byte)1);\n" +
				"		if (1 + subst.get((byte)1) > 0.f) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}		\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82859
	public void test082() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String argv[]) {\n" +
				"		System.out.println(void.class == Void.TYPE);\n" +
				"	}\n" +
				"}"
			},
			"true"
		);
	}

		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82647
	public void test083() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	int counter = 0;\n" +
				"\n" +
				"	public boolean wasNull() {\n" +
				"		return ++counter % 2 == 0;\n" +
				"	}\n" +
				"\n" +
				"	private Byte getByte() {\n" +
				"		return (byte) 0;\n" +
				"	}\n" +
				"\n" +
				"	private Short getShort() {\n" +
				"		return (short) 0;\n" +
				"	}\n" +
				"\n" +
				"	private Long getLong() {\n" +
				"		return 0L;\n" +
				"	}\n" +
				"\n" +
				"	private Integer getInt() {\n" +
				"		return 0; // autoboxed okay\n" +
				"	}\n" +
				"\n" +
				"	// This should be the same as the second one.\n" +
				"	private Byte getBytey() {\n" +
				"		byte value = getByte();\n" +
				"		return wasNull() ? null : value;\n" +
				"	}\n" +
				"\n" +
				"	private Byte getByteyNoBoxing() {\n" +
				"		byte value = getByte();\n" +
				"		return wasNull() ? null : (Byte) value;\n" +
				"	}\n" +
				"\n" +
				"	// This should be the same as the second one.\n" +
				"	private Short getShorty() {\n" +
				"		short value = getShort();\n" +
				"		return wasNull() ? null : value;\n" +
				"	}\n" +
				"\n" +
				"	private Short getShortyNoBoxing() {\n" +
				"		short value = getShort();\n" +
				"		return wasNull() ? null : (Short) value;\n" +
				"	}\n" +
				"\n" +
				"	// This should be the same as the second one.\n" +
				"	private Long getLongy() {\n" +
				"		long value = getLong();\n" +
				"		return wasNull() ? null : value;\n" +
				"	}\n" +
				"\n" +
				"	private Long getLongyNoBoxing() {\n" +
				"		long value = getLong();\n" +
				"		return wasNull() ? null : (Long) value;\n" +
				"	}\n" +
				"\n" +
				"	// This should be the same as the second one.\n" +
				"	private Integer getIntegery() {\n" +
				"		int value = getInt();\n" +
				"		return wasNull() ? null : value;\n" +
				"	}\n" +
				"\n" +
				"	private Integer getIntegeryNoBoxing() {\n" +
				"		int value = getInt();\n" +
				"		return wasNull() ? null : (Integer) value;\n" +
				"	}\n" +
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82647 - variation
	public void test084() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	Short foo() {\n" +
				"		short value = 0;\n" +
				"		return this == null ? null : value;\n" +
				"	}\n" +
				"	boolean bar() {\n" +
				"		short value = 0;\n" +
				"		return null == value;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	return this == null ? null : value;\n" +
			"	                             ^^^^^\n" +
			"The expression of type short is boxed into Short\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	return null == value;\n" +
			"	       ^^^^^^^^^^^^^\n" +
			"The operator == is undefined for the argument type(s) null, short\n" +
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83965
	public void test085() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	private static void checkByteConversions(Byte _byte) {\n" +
				"		short s = (short) _byte;\n" +
				"		short s2 = _byte;\n" +
				"		int i = (int) _byte;\n" +
				"		long l = (long) _byte;\n" +
				"		float f = (float) _byte;\n" +
				"		double d = (double) _byte;\n" +
				"		if ( _byte.byteValue() != s ) {\n" +
				"            System.err.println(\"Must be equal 0\");\n" +
				"        }\n" +
				"		if ( _byte.byteValue() != i ) {\n" +
				"            System.err.println(\"Must be equal 1\");\n" +
				"        }\n" +
				"		if ( _byte.byteValue() != l ) {\n" +
				"            System.err.println(\"Must be equal 2\");\n" +
				"        }\n" +
				"		if ( _byte.byteValue() != f ) {\n" +
				"            System.err.println(\"Must be equal 3\");\n" +
				"        }\n" +
				"		if ( _byte.byteValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 4\");\n" +
				"        }\n" +
				"	} \n" +
				"\n" +
				"	private static void checkCharacterConversions(Character _character) {\n" +
				"		int i = (int) _character;\n" +
				"		long l = (long) _character;\n" +
				"		float f = (float) _character;\n" +
				"		double d = (double) _character;\n" +
				"		if ( _character.charValue() != i ) {\n" +
				"            System.err.println(\"Must be equal 9\");\n" +
				"        }\n" +
				"		if ( _character.charValue() != l ) {\n" +
				"            System.err.println(\"Must be equal 10\");\n" +
				"        }\n" +
				"		if ( _character.charValue() != f ) {\n" +
				"            System.err.println(\"Must be equal 11\");\n" +
				"        }\n" +
				"		if ( _character.charValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 12\");\n" +
				"        }\n" +
				"	}\n" +
				"\n" +
				"	private static void checkFloatConversions(Float _float) {\n" +
				"		double d = (double) _float;\n" +
				"		if ( _float.floatValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 18\");\n" +
				"        }\n" +
				"	}\n" +
				"\n" +
				"	private static void checkIntegerConversions(Integer _integer) {\n" +
				"		long l = (long) _integer;\n" +
				"		float f = (float) _integer;\n" +
				"		double d = (double) _integer;\n" +
				"		if ( _integer.intValue() != l ) {\n" +
				"            System.err.println(\"Must be equal 13\");\n" +
				"        }\n" +
				"		if ( _integer.intValue() != f ) {\n" +
				"            System.err.println(\"Must be equal 14\");\n" +
				"        }\n" +
				"		if ( _integer.intValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 15\");\n" +
				"        }\n" +
				"	}\n" +
				"\n" +
				"	private static void checkIntegerConversions(Short _short) {\n" +
				"		int i = (int) _short;\n" +
				"		long l = (long) _short;\n" +
				"		float f = (float) _short;\n" +
				"		double d = (double) _short;\n" +
				"		if ( _short.shortValue() != i ) {\n" +
				"            System.err.println(\"Must be equal 5\");\n" +
				"        }\n" +
				"		if ( _short.shortValue() != l ) {\n" +
				"            System.err.println(\"Must be equal 6\");\n" +
				"        }\n" +
				"		if ( _short.shortValue() != f ) {\n" +
				"            System.err.println(\"Must be equal 7\");\n" +
				"        }\n" +
				"		if ( _short.shortValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 8\");\n" +
				"        }\n" +
				"	}\n" +
				"\n" +
				"	private static void checkLongConversions(Long _long) {\n" +
				"		float f = (float) _long;\n" +
				"		double d = (double) _long;\n" +
				"		if ( _long.longValue() != f ) {\n" +
				"            System.err.println(\"Must be equal 16\");\n" +
				"        }\n" +
				"		if ( _long.longValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 17\");\n" +
				"        }\n" +
				"	}\n" +
				"\n" +
				"    public static void main(String args[]) {\n" +
				"        Byte _byte = new Byte((byte)2);\n" +
				"        Character _character = new Character(\'@\');\n" +
				"        Short _short = new Short((short)255);\n" +
				"        Integer _integer = new Integer(12345678);\n" +
				"        Long _long = new Long(1234567890);\n" +
				"        Float _float = new Float(-0.0);\n" +
				"\n" +
				"        checkByteConversions(_byte);\n" +
				"        checkIntegerConversions(_short);\n" +
				"        checkCharacterConversions(_character);\n" +
				"        checkIntegerConversions(_integer);\n" +
				"        checkLongConversions(_long);\n" +
				"        checkFloatConversions(_float);\n" +
				"\n" +
				"        System.out.println(\"OK\");\n" +
				"      }\n" +
				"}\n"
			},
			"OK"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84055
	public void test086() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  private static void checkConversions(byte _byte) {\n" +
				"    Short s = (short) _byte; // cast is necessary\n" +
				"    Short s2 = _byte; // ko\n" +
				"  } \n" +
				"  public static void main(String args[]) {\n" +
				"    byte _byte = 2;\n" +
				"    checkConversions(_byte);\n" +
				"    System.out.println(\"OK\");\n" +
				"  }\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Short s = (short) _byte; // cast is necessary\n" +
			"	          ^^^^^^^^^^^^^\n" +
			"The expression of type short is boxed into Short\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Short s2 = _byte; // ko\n" +
			"	           ^^^^^\n" +
			"Type mismatch: cannot convert from byte to Short\n" +
			"----------\n"
        );
	}
    // autoboxing and type argument inference
    public void test087() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "public class X {\n" +
                "    <T> T foo(T t) { return t; }\n" +
                "    \n" +
                "    public static void main(String[] args) {\n" +
                "        int i = new X().foo(12);\n" +
                "        System.out.println(i);\n" +
                "    }\n" +
                "    Zork z;\n" +
                "}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	int i = new X().foo(12);\n" +
			"	        ^^^^^^^^^^^^^^^\n" +
			"The expression of type Integer is unboxed into int\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	int i = new X().foo(12);\n" +
			"	                    ^^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n"
        );
    }
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=84480 - variation with autoboxing diagnosis on
	 */
	public void test088() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	int f;\n" +
				"	void foo(int i) {\n" +
				"		i = i++;\n" +
				"		i = ++i;\n" +
				"		f = f++;\n" +
				"		f = ++f;\n" +
				"		Zork z;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	i = ++i;\n" +
			"	^^^^^^^\n" +
			"The assignment to variable i has no effect\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 7)\n" +
			"	f = ++f;\n" +
			"	^^^^^^^\n" +
			"The assignment to variable f has no effect\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true,
			customOptions);
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84345
    public void test089() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"  public Object foo() {\n" +
				"  	byte b = 0;\n" +
				"	Number n = (Number) b;\n" +
				"\n" +
				"    java.io.Serializable o = null;\n" +
				"    if (o == 0) return o;\n" +
				"    return this;\n" +
				"  }\n" +
				"}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	Number n = (Number) b;\n" +
			"	           ^^^^^^^^^^\n" +
			"Unnecessary cast from byte to Number\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	Number n = (Number) b;\n" +
			"	                    ^\n" +
			"The expression of type byte is boxed into Byte\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	if (o == 0) return o;\n" +
			"	    ^^^^^^\n" +
			"Incompatible operand types Serializable and int\n" +
			"----------\n"
        );
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84345 - variation
    public void test090() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"  public Object foo() {\n" +
				"  \n" +
				"  	Boolean b = null;\n" +
				"     if (b == true) return b;\n" +
				"     Object o = null;\n" +
				"    if (o == true) return o;\n" +
				"    return this;\n" +
				"  }\n" +
				"}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	if (b == true) return b;\n" +
			"	    ^\n" +
			"The expression of type Boolean is unboxed into boolean\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	if (o == true) return o;\n" +
			"	    ^^^^^^^^^\n" +
			"Incompatible operand types Object and boolean\n" +
			"----------\n"
        );
    }

    // type argument inference and autoboxing
    public void test091() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"\n" +
				"    public static void main(String[] args) {\n" +
				"        Comparable<?> c1 = foo(\"\", new Integer(5));\n" +
				"        Object o = foo(\"\", 5);\n" +
				"    }\n" +
				"    public static <T> T foo(T t1, T t2) { \n" +
				"    	System.out.print(\"foo(\"+t1.getClass().getSimpleName()+\",\"+t2.getClass().getSimpleName()+\")\");\n" +
				"    	return null; \n" +
				"    }\n" +
				"}\n"
            },
			"foo(String,Integer)foo(String,Integer)"
        );
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84669
    public void test092() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public class X\n" +
				"{\n" +
				"	public X()\n" +
				"	{\n" +
				"		super();\n" +
				"	}\n" +
				"\n" +
				"	public Object convert(Object value)\n" +
				"	{\n" +
				"		Double d = (Double)value;\n" +
				"		d = (d/100);\n" +
				"		return d;\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args)\n" +
				"	{\n" +
				"		X test = new X();\n" +
				"		Object value = test.convert(new Double(50));\n" +
				"		System.out.println(value);\n" +
				"	}\n" +
				"}\n"
            },
			"0.5"
        );
    }

    public void test093() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer someInteger = 12;\n" +
				"		System.out.println((args == null ? someInteger : \'A\') == \'A\');\n" +
				"	}\n" +
				"}\n"
            },
			"true"
        );
    }

    public void test094() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer someInteger = 12;\n" +
				"		System.out.println((args == null ? someInteger : \'A\') == \'A\');\n" +
				"		Zork z;\n" +
				"	}\n" +
				"}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Integer someInteger = 12;\n" +
			"	                      ^^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	System.out.println((args == null ? someInteger : \'A\') == \'A\');\n" +
			"	                                   ^^^^^^^^^^^\n" +
			"The expression of type Integer is unboxed into int\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n"
        );
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80630
    public void test095() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = true;\n" +
				"		Character _Character = new Character(\' \');\n" +
				"		char c = \' \';\n" +
				"		Integer _Integer = new Integer(2);\n" +
				"		if ((b ? _Character : _Integer) == c) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		} else {\n" +
				"			System.out.println(\"FAILURE\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
            },
			"SUCCESS"
        );
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80630 - variation
    public void test096() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	@SuppressWarnings(\"deprecation\")\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = true;\n" +
				"		Character _Character = Character.valueOf(\' \');\n" +
				"		char c = \' \';\n" +
				"		Integer _Integer = Integer.valueOf(2);\n" +
				"		if ((b ? _Character : _Integer) == c) {\n" +
				"			System.out.println(zork);\n" +
				"		} else {\n" +
				"			System.out.println(\"FAILURE\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	if ((b ? _Character : _Integer) == c) {\n" +
			"	         ^^^^^^^^^^\n" +
			"The expression of type Character is unboxed into int\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 8)\n" +
			"	if ((b ? _Character : _Integer) == c) {\n" +
			"	                      ^^^^^^^^\n" +
			"The expression of type Integer is unboxed into int\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 9)\n" +
			"	System.out.println(zork);\n" +
			"	                   ^^^^\n" +
			"zork cannot be resolved to a variable\n" +
			"----------\n"
        );
    }
    // conditional operator: bool ? Integer : Integer --> Integer (identical operand types)
    // but   bool ? Integer : Short --> unboxed int
    public void test097() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"    public static void main(String args[]) {\n" +
				"        Integer i = 1;\n" +
				"        Integer j = 2;\n" +
				"        Short s = 3;\n" +
				"        foo(args != null ? i : j);\n" +
				"        foo(args != null ? i : s);\n" +
				"    }\n" +
				"    static void foo(int i) {\n" +
				"        System.out.print(\"[int:\"+i+\"]\");\n" +
				"    }\n" +
				"    static void foo(Integer i) {\n" +
				"        System.out.print(\"[Integer:\"+i+\"]\");\n" +
				"    }\n" +
				"}\n"
            },
			"[Integer:1][int:1]"
        );
    }
    // conditional operator: bool ? Integer : Integer --> Integer (identical operand types)
    // but   bool ? Integer : Short --> unboxed int
    // check autoboxing warnings
    public void test098() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"    public static void main(String args[]) {\n" +
				"        Integer i = 1;\n" +
				"        Integer j = 2;\n" +
				"        Short s = 3;\n" +
				"        foo(args != null ? i : j);\n" +
				"        foo(args != null ? i : s);\n" +
				"		 Zork z;\n" +
				"    }\n" +
				"    static void foo(int i) {\n" +
				"        System.out.print(\"[int:\"+i+\"]\");\n" +
				"    }\n" +
				"    static void foo(Integer i) {\n" +
				"        System.out.print(\"[Integer:\"+i+\"]\");\n" +
				"    }\n" +
				"}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Integer i = 1;\n" +
			"	            ^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	Integer j = 2;\n" +
			"	            ^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 5)\n" +
			"	Short s = 3;\n" +
			"	          ^\n" +
			"The expression of type int is boxed into Short\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 7)\n" +
			"	foo(args != null ? i : s);\n" +
			"	                   ^\n" +
			"The expression of type Integer is unboxed into int\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 7)\n" +
			"	foo(args != null ? i : s);\n" +
			"	                       ^\n" +
			"The expression of type Short is unboxed into int\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 8)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n"
        );
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84801
	public void test099() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends A {\n" +
				"    public void m(Object o) { System.out.println(\"SUCCESS\"); }\n" +
				"    public static void main(String[] args) { ((A) new X()).m(1); }\n" +
				"}\n" +
				"interface I { void m(Object o); }\n" +
				"abstract class A implements I {\n" +
				"	public final void m(int i) {\n" +
				"		System.out.print(\"SUCCESS + \");\n" +
				"		m(new Integer(i));\n" +
				"	}\n" +
				"	public final void m(double d) {\n" +
				"		System.out.print(\"FAILED\");\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS + SUCCESS"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87267
	public void test100() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer[] integers = {};\n" +
				"		int[] ints = (int[]) integers;\n" +
				"		float[] floats = {};\n" +
				"		Float[] fs = (Float[]) floats;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	int[] ints = (int[]) integers;\n" +
			"	             ^^^^^^^^^^^^^^^^\n" +
			"Cannot cast from Integer[] to int[]\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	Float[] fs = (Float[]) floats;\n" +
			"	             ^^^^^^^^^^^^^^^^\n" +
			"Cannot cast from float[] to Float[]\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85491
	public void test101() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo(Object... i) { System.out.print(1); }\n" +
				"	void foo(int... i) { System.out.print(2); }\n" +
				"	@SuppressWarnings(\"deprecation\")\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(1);\n" +
				"		new X().foo(Integer.valueOf(1));\n" +
				"		new X().foo(1, Integer.valueOf(1));\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	new X().foo(1);\n" +
			"	        ^^^\n" +
			"The method foo(Object[]) is ambiguous for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	new X().foo(Integer.valueOf(1));\n" +
			"	        ^^^\n" +
			"The method foo(Object[]) is ambiguous for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	new X().foo(1, Integer.valueOf(1));\n" +
			"	        ^^^\n" +
			"The method foo(Object[]) is ambiguous for the type X\n" +
			"----------\n");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo(Number... i) { System.out.print(1); }\n" +
				"	void foo(int... i) { System.out.print(2); }\n" +
				"	@SuppressWarnings(\"deprecation\")\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(1);\n" +
				"		new X().foo(Integer.valueOf(1));\n" +
				"		new X().foo(1, Integer.valueOf(1));\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	new X().foo(1);\n" +
			"	        ^^^\n" +
			"The method foo(Number[]) is ambiguous for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	new X().foo(Integer.valueOf(1));\n" +
			"	        ^^^\n" +
			"The method foo(Number[]) is ambiguous for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	new X().foo(1, Integer.valueOf(1));\n" +
			"	        ^^^\n" +
			"The method foo(Number[]) is ambiguous for the type X\n" +
			"----------\n"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo(int i, Object... o) { System.out.print(1); }\n" +
				"	void foo(Integer o, int... i) { System.out.print(2); }\n" +
				"	@SuppressWarnings(\"deprecation\")\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(1);\n" +
				"		new X().foo(Integer.valueOf(1));\n" +
				"		new X().foo(1, Integer.valueOf(1));\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	new X().foo(1);\n" +
			"	        ^^^\n" +
			"The method foo(int, Object[]) is ambiguous for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	new X().foo(Integer.valueOf(1));\n" +
			"	        ^^^\n" +
			"The method foo(int, Object[]) is ambiguous for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	new X().foo(1, Integer.valueOf(1));\n" +
			"	        ^^^\n" +
			"The method foo(int, Object[]) is ambiguous for the type X\n" +
			"----------\n"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84801
	public void test102() {
		runConformTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"class Cla<A> {\n" +
				"	A val;\n" +
				"	public Cla(A x) { val = x; }\n" +
				"	A getVal() { return val; }\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	\n" +
				"	void proc0(Cla<Long> b0) {\n" +
				"		final Long t1 = b0.getVal();\n" +
				"		System.out.print(t1);\n" +
				"		final long t2 = b0.getVal();\n" +
				"		System.out.print(t2);\n" +
				"	}\n" +
				"\n" +
				"	void proc1(Cla<? extends Long> obj) {\n" +
				"		final Long t3 = obj.getVal();\n" +
				"		System.out.print(t3);\n" +
				"		final long t4 = obj.getVal();\n" +
				"		System.out.print(t4);\n" +
				"	}\n" +
				"	\n" +
				"	<U extends Long> void proc2(Cla<U> obj) {\n" +
				"		final Long t5 = obj.getVal();\n" +
				"		System.out.print(t5);\n" +
				"		final long t6 = obj.getVal();\n" +
				"		System.out.println(t6);\n" +
				"	}\n" +
				"	\n" +
				"	public static void main(String[] args) {\n" +
				"		X x = new X();\n" +
				"		x.proc0(new Cla<Long>(0l));\n" +
				"		x.proc1(new Cla<Long>(1l));\n" +
				"		x.proc2(new Cla<Long>(2l));\n" +
				"	}\n" +
				"}\n"
			},
			// compiler results
			null /* do not check compiler log */,
			// runtime results
			"001122" /* expected output string */,
			"" /* expected error string */,
			// javac options
			JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84801 - variation (check warnings)
	public void test103() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class Cla<A> {\n" +
				"	Zork z;\n" +
				"	A val;\n" +
				"	public Cla(A x) { val = x; }\n" +
				"	A getVal() { return val; }\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	\n" +
				"	void proc0(Cla<Long> b0) {\n" +
				"		final Long t1 = b0.getVal();\n" +
				"		System.out.print(t1);\n" +
				"		final long t2 = b0.getVal();\n" +
				"		System.out.print(t2);\n" +
				"	}\n" +
				"\n" +
				"	void proc1(Cla<? extends Long> obj) {\n" +
				"		final Long t3 = obj.getVal();\n" +
				"		System.out.print(t3);\n" +
				"		final long t4 = obj.getVal();\n" +
				"		System.out.print(t4);\n" +
				"	}\n" +
				"	\n" +
				"	<U extends Long> void proc2(Cla<U> obj) {\n" +
				"		final Long t5 = obj.getVal();\n" +
				"		System.out.print(t5);\n" +
				"		final long t6 = obj.getVal();\n" +
				"		System.out.printltn(t6);\n" +
				"	}\n" +
				"	\n" +
				"	public static void main(String[] args) {\n" +
				"		X x = new X();\n" +
				"		x.proc0(new Cla<Long>(0l));\n" +
				"		x.proc1(new Cla<Long>(1l));\n" +
				"		x.proc2(new Cla<Long>(2l));\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 13)\n" +
			"	final long t2 = b0.getVal();\n" +
			"	                ^^^^^^^^^^^\n" +
			"The expression of type Long is unboxed into long\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 20)\n" +
			"	final long t4 = obj.getVal();\n" +
			"	                ^^^^^^^^^^^^\n" +
			"The expression of type capture#2-of ? extends Long is unboxed into long\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 24)\n" +
			"	<U extends Long> void proc2(Cla<U> obj) {\n" +
			"	           ^^^^\n" +
			"The type parameter U should not be bounded by the final type Long. Final types cannot be further extended\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 27)\n" +
			"	final long t6 = obj.getVal();\n" +
			"	                ^^^^^^^^^^^^\n" +
			"The expression of type U is unboxed into long\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 28)\n" +
			"	System.out.printltn(t6);\n" +
			"	           ^^^^^^^^\n" +
			"The method printltn(long) is undefined for the type PrintStream\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 33)\n" +
			"	x.proc0(new Cla<Long>(0l));\n" +
			"	                      ^^\n" +
			"The expression of type long is boxed into Long\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 34)\n" +
			"	x.proc1(new Cla<Long>(1l));\n" +
			"	                      ^^\n" +
			"The expression of type long is boxed into Long\n" +
			"----------\n" +
			"9. WARNING in X.java (at line 35)\n" +
			"	x.proc2(new Cla<Long>(2l));\n" +
			"	                      ^^\n" +
			"The expression of type long is boxed into Long\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=95868
	public void test104() {
		this.runConformTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.JavacGeneratesIncorrectCode,
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			String x = \"\";\n" +
				"			HashMap<String, Integer> y = new HashMap<String, Integer>();\n" +
				"			Integer w = (x.equals(\"X\") ? 0 : y.get(\"yKey\"));\n" +
				"		} catch(NullPointerException e) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS");
	}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101779
public void test105() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"final class Pair<F, S> {\n" +
			"	public F first;\n" +
			"	public S second;\n" +
			"\n" +
			"	public static <F, S> Pair<F, S> create(F f, S s) {\n" +
			"		return new Pair<F, S>(f, s);\n" +
			"	}\n" +
			"\n" +
			"	public Pair(final F f, final S s) {\n" +
			"		first = f;\n" +
			"		second = s;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"	public void a() {\n" +
			"		Pair<Integer, Integer> p = Pair.create(1, 3);\n" +
			"		// p.first -= 1; // should be rejected ?\n" +
			"		p.first--;\n" +
			"		--p.first;\n" +
			"		p.first = p.first - 1;\n" +
			"		System.out.println(p.first);\n" +
			"	}\n" +
			"\n" +
			"	public static void main(final String[] args) {\n" +
			"		new X().a();\n" +
			"	}\n" +
			"}\n",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"-2" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101779 - variation
public void test106() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"class XSuper<T> {\n" +
			"	T value;\n" +
			"}\n" +
			"public class X extends XSuper<Integer>{\n" +
			"	public void a() {\n" +
			"		value--;\n" +
			"		--value;\n" +
			"		value -= 1;\n" +
			"		value = value - 1;\n" +
			"		System.out.println(value);\n" +
			"	}\n" +
			"\n" +
			"	public static void main(final String[] args) {\n" +
			"		X x = new X();\n" +
			"		x.value = 5;\n" +
			"		x.a();\n" +
			"	}\n" +
			"}\n",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101779 - variation
public void test107() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"class XSuper<T> {\n" +
			"	T value;\n" +
			"}\n" +
			"public class X extends XSuper<Integer>{\n" +
			"	public void a() {\n" +
			"		this.value--;\n" +
			"		--this.value;\n" +
			"		this.value -= 1;\n" +
			"		this.value = this.value - 1;\n" +
			"		System.out.println(this.value);\n" +
			"	}\n" +
			"\n" +
			"	public static void main(final String[] args) {\n" +
			"		X x = new X();\n" +
			"		x.value = 5;\n" +
			"		x.a();\n" +
			"	}\n" +
			"}\n",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101779 - variation
public void test108() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"class XSuper<T> {\n" +
			"	T value;\n" +
			"}\n" +
			"public class X extends XSuper<Integer>{\n" +
			"	public static void a(X x) {\n" +
			"		x.value--;\n" +
			"		--x.value;\n" +
			"		x.value -= 1;\n" +
			"		x.value = x.value - 1;\n" +
			"		System.out.println(x.value);\n" +
			"	}\n" +
			"\n" +
			"	public static void main(final String[] args) {\n" +
			"		X x = new X();\n" +
			"		x.value = 5;\n" +
			"		a(x);\n" +
			"	}\n" +
			"}\n",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100043
public void test109() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int foo = 0;\n" +
			"		String bar = \"zero\";\n" +
			"		System.out.println((foo != 0) ? foo : bar);\n" +
			"	}\n" +
			"}\n",
		},
		"zero");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100043 - variation
public void test110() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String args[]) {\n" +
			"    	if (new Boolean(true) ? true : new Boolean(false)) {\n" +
			"    		System.out.print(\"SUCCESS\");\n" +
			"    	} else {\n" +
			"    		System.out.print(\"FAILED\");\n" +
			"    	}\n" +
			"    }\n" +
			"}\n",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105524
public void test111() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"class Wrapper< T >\n" +
			"{\n" +
			"    public T value;\n" +
			"}\n" +
			"\n" +
			"public class X\n" +
			"{\n" +
			"    public static void main( final String[ ] args )\n" +
			"    {\n" +
			"        final Wrapper< Integer > wrap = new Wrapper< Integer >( );\n" +
			"        wrap.value = 0;\n" +
			"        wrap.value = wrap.value + 1; // works\n" +
			"        wrap.value++; // throws VerifyError\n" +
			"        wrap.value += 1; // throws VerifyError\n" +
			"    }\n" +
			"}\n",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105284
public void test112() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Short s;\n" +
			"		s = 5;  // Type mismatch: cannot convert from int to Short\n" +
			"		Short[] shorts = { 0, 1, 2, 3 };\n" +
			"		System.out.println(s+shorts[2]);\n" +
			"	}\n" +
			"}\n",
		},
		"7");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105284 - variation
public void test113() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Short s;\n" +
			"		s = 5;  // Type mismatch: cannot convert from int to Short\n" +
			"\n" +
			"		int i = 0;\n" +
			"		s = i; // not a constant\n" +
			"		\n" +
			"		bar(4);\n" +
			"		Short[] shorts = { 0, 1, 2, 3 };\n" +
			"	}\n" +
			"	void bar(Short s) {}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	s = 5;  // Type mismatch: cannot convert from int to Short\n" +
		"	    ^\n" +
		"The expression of type int is boxed into Short\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	s = i; // not a constant\n" +
		"	    ^\n" +
		"Type mismatch: cannot convert from int to Short\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	bar(4);\n" +
		"	^^^\n" +
		"The method bar(Short) in the type X is not applicable for the arguments (int)\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 10)\n" +
		"	Short[] shorts = { 0, 1, 2, 3 };\n" +
		"	                   ^\n" +
		"The expression of type int is boxed into Short\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 10)\n" +
		"	Short[] shorts = { 0, 1, 2, 3 };\n" +
		"	                      ^\n" +
		"The expression of type int is boxed into Short\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 10)\n" +
		"	Short[] shorts = { 0, 1, 2, 3 };\n" +
		"	                         ^\n" +
		"The expression of type int is boxed into Short\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 10)\n" +
		"	Short[] shorts = { 0, 1, 2, 3 };\n" +
		"	                            ^\n" +
		"The expression of type int is boxed into Short\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100182
public void test114() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] s) {\n" +
			"		char c = \'a\';\n" +
			"		System.out.printf(\"%c\",c);		\n" +
			"		System.out.printf(\"%d\\n\",(int)c);		\n" +
			"	}\n" +
			"	Zork z;\n" +
			"}\n" ,
		},
		// ensure no unnecessary cast warning
		"----------\n" +
		"1. WARNING in X.java (at line 4)\r\n" +
		"	System.out.printf(\"%c\",c);		\r\n" +
		"	                       ^\n" +
		"The expression of type char is boxed into Character\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\r\n" +
		"	System.out.printf(\"%d\\n\",(int)c);		\r\n" +
		"	                         ^^^^^^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\r\n" +
		"	Zork z;\r\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100182 - variation
public void test115() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] s) {\n" +
			"		char c = \'a\';\n" +
			"		System.out.printf(\"%c\",c);		\n" +
			"		System.out.printf(\"%d\\n\",(int)c);		\n" +
			"	}\n" +
			"}\n",
		},
		"a97");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=106870
public void test116() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    boolean foo(Long l, Float f) {\n" +
			"    	return f == l;\n" +
			"    }\n" +
			"    float bar(Long l, Float f) {\n" +
			"    	return this == null ? f : l;\n" +
			"    }\n" +
			"    double baz(Long l, Float f) {\n" +
			"    	return this == null ? f : l;\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\r\n" +
		"	return f == l;\r\n" +
		"	       ^^^^^^\n" +
		"Incompatible operand types Float and Long\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\r\n" +
		"	return this == null ? f : l;\r\n" +
		"	                      ^\n" +
		"The expression of type Float is unboxed into float\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\r\n" +
		"	return this == null ? f : l;\r\n" +
		"	                          ^\n" +
		"The expression of type Long is unboxed into float\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 9)\r\n" +
		"	return this == null ? f : l;\r\n" +
		"	                      ^\n" +
		"The expression of type Float is unboxed into float\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 9)\r\n" +
		"	return this == null ? f : l;\r\n" +
		"	                          ^\n" +
		"The expression of type Long is unboxed into float\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122987
public void test117() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args)\n" +
			"    {\n" +
			"        Object obj = true ? true : 17.3;\n" +
			"		 Zork z;\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	Object obj = true ? true : 17.3;\n" +
		"	                    ^^^^\n" +
		"The expression of type boolean is boxed into Boolean\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	Object obj = true ? true : 17.3;\n" +
		"	                           ^^^^\n" +
		"The expression of type double is boxed into Double\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}

// Integer array and method with T extends Integer bound
public void test118() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"    public static <T extends Integer> void foo(final T[] p) {\n" +
			// we have a warning here, since no class can extend Integer, but the code
			// still needs to execute
			"        System.out.println(p[0] / 4);\n" +
			"    }\n" +
			"    public static void main(final String[] args) {\n" +
			"        X.foo(new Integer[] { 4, 8, 16 });\n" +
			"    }\n" +
			"}",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBug6575821 /* javac test options */);
}

// Integer as member of a parametrized class
public void test119() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X<T> {\n" +
			"    T m;\n" +
			"    X(T p) {\n" +
			"        this.m = p;\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        X<Integer> l = new X<Integer>(0);\n" + // boxing
			"        l.m++;\n" + // boxing + unboxing
			"        System.out.println(l.m);\n" +
			"    }\n" +
			"}",
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"1" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=137918
public void test120() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int a = 100;\n" +
			"		boolean c = a instanceof Integer;\n" +
			"		Integer i = (Integer) a;\n" +
			"		System.out.println(c);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	boolean c = a instanceof Integer;\n" +
		"	            ^^^^^^^^^^^^^^^^^^^^\n" +
		"Incompatible conditional operand types int and Integer\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	Integer i = (Integer) a;\n" +
		"	            ^^^^^^^^^^^\n" +
		"Unnecessary cast from int to Integer\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 5)\n" +
		"	Integer i = (Integer) a;\n" +
		"	                      ^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156108
public void test121() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		final int i = -128;\n" +
			"		Byte b = i;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		Byte no = 127; // warning: int boxed to Byte > fine\n" +
			"		switch (no) { // warning: Byte is unboxed into int > why in int??? output\n" +
			"			case -128: // error: cannot convert int to Byte > needs a explicit (byte)cast.\n" +
			"				break;\n" +
			"			case (byte) 127: // works\n" +
			"				break;\n" +
			"		}\n" +
			"		no = new Byte(127);\n" +
			"	}\n" +
			"}", // =================
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	Byte b = i;\n" +
		"	         ^\n" +
		"The expression of type int is boxed into Byte\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	Byte no = 127; // warning: int boxed to Byte > fine\n" +
		"	          ^^^\n" +
		"The expression of type int is boxed into Byte\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 8)\n" +
		"	switch (no) { // warning: Byte is unboxed into int > why in int??? output\n" +
		"	        ^^\n" +
		"The expression of type Byte is unboxed into int\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 14)\n" +
		"	no = new Byte(127);\n" +
		"	     ^^^^^^^^^^^^^\n" +
		"The constructor Byte(int) is undefined\n" +
		"----------\n"
);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156108 - variation
public void test122() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	Byte foo() {\n" +
			"		final int i = -128;\n" +
			"		return i;\n" +
			"	}\n" +
			"	Byte bar() {\n" +
			"		final int i = 1000;\n" +
			"		return i;\n" +
			"	}	\n" +
			"}", // =================
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	return i;\n" +
		"	       ^\n" +
		"The expression of type int is boxed into Byte\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	return i;\n" +
		"	       ^\n" +
		"Type mismatch: cannot convert from int to Byte\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155255
public void test123() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		foo1();\n" +
			"		foo2();\n" +
			"		foo3();\n" +
			"		foo4();\n" +
			"		System.out.println(\"[done]\");\n" +
			"	}\n" +
			"	static void foo1() {\n" +
			"		Object x = true ? true : \"\";\n" +
			"		System.out.print(\"[1:\"+ x + \",\" + x.getClass().getCanonicalName() + \"]\");\n" +
			"	}\n" +
			"	static void foo2() {\n" +
			"		Object x = Boolean.TRUE != null ? true : \"\";\n" +
			"		System.out.print(\"[2:\"+ x + \",\" + x.getClass().getCanonicalName() + \"]\");\n" +
			"	}\n" +
			"	static void foo3() {\n" +
			"		Object x = false ? \"\" : false;\n" +
			"		System.out.print(\"[3:\"+ x + \",\" + x.getClass().getCanonicalName() + \"]\");\n" +
			"	}\n" +
			"	static void foo4() {\n" +
			"		Object x = Boolean.TRUE == null ? \"\" : false;\n" +
			"		System.out.print(\"[4:\"+ x + \",\" + x.getClass().getCanonicalName() + \"]\");\n" +
			"	}\n" +
			"}", // =================
		},
		"[1:true,java.lang.Boolean][2:true,java.lang.Boolean][3:false,java.lang.Boolean][4:false,java.lang.Boolean][done]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155255 - variation
public void test124() {
	String specVersion = System.getProperty("java.specification.version");
	isJRE15Plus =  Integer.valueOf(specVersion) >= Integer.valueOf(CompilerOptions.VERSION_15);
	String bounds = isJRE15Plus ? "Object&Serializable&Comparable<?>&Constable" : "Object&Serializable&Comparable<?>";

	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static void foo5() {\n" +
			"		boolean x = false ? \"\" : false;\n" +
			"		System.out.print(\"[4:\"+ x + \",\" + x.getClass().getCanonicalName() + \"]\");\n" +
			"	}	\n" +
			"}", // =================
		},
		this.complianceLevel >= ClassFileConstants.JDK1_8 ?
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	boolean x = false ? \"\" : false;\n" +
				"	                    ^^\n" +
				"Type mismatch: cannot convert from String to boolean\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	System.out.print(\"[4:\"+ x + \",\" + x.getClass().getCanonicalName() + \"]\");\n" +
				"	                                  ^^^^^^^^^^^^\n" +
				"Cannot invoke getClass() on the primitive type boolean\n" +
				"----------\n" :
						"----------\n" +
						"1. ERROR in X.java (at line 3)\n" +
						"	boolean x = false ? \"\" : false;\n" +
						"	            ^^^^^^^^^^^^^^^^^^\n" +
						"Type mismatch: cannot convert from "+ bounds +" to boolean\n" +
						"----------\n" +
						"2. WARNING in X.java (at line 3)\n" +
						"	boolean x = false ? \"\" : false;\n" +
						"	                         ^^^^^\n" +
						"The expression of type boolean is boxed into Boolean\n" +
						"----------\n" +
						"3. ERROR in X.java (at line 4)\n" +
						"	System.out.print(\"[4:\"+ x + \",\" + x.getClass().getCanonicalName() + \"]\");\n" +
						"	                                  ^^^^^^^^^^^^\n" +
						"Cannot invoke getClass() on the primitive type boolean\n" +
						"----------\n");
	}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155255 - variation
public void test125() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		foo1();\n" +
			"		foo2();\n" +
			"		foo3();\n" +
			"		System.out.println(\"[done]\");\n" +
			"	}\n" +
			"	static void foo1() {\n" +
			"		Object x = true ? 3.0f : false;\n" +
			"		System.out.print(\"[1:\"+ x + \",\" + x.getClass().getCanonicalName() + \"]\");\n" +
			"	}\n" +
			"	static void foo2() {\n" +
			"		Object x = true ? 2 : false;\n" +
			"		System.out.print(\"[2:\"+ x + \",\" + x.getClass().getCanonicalName() + \"]\");\n" +
			"	}\n" +
			"	static void foo3() {\n" +
			"		Object x = false ? 2 : false;\n" +
			"		System.out.print(\"[3:\"+ x + \",\" + x.getClass().getCanonicalName() + \"]\");\n" +
			"	}\n" +
			"}\n", // =================
		},
		"[1:3.0,java.lang.Float][2:2,java.lang.Integer][3:false,java.lang.Boolean][done]");
	}
public void test126() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(boolean b) {\n" +
			"		int i = 12;\n" +
			"		Integer r1 = b ? null : i;\n" +
			"		int r2 = b ? null : i;\n" +
			"	}\n" +
			"	Zork z;\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	Integer r1 = b ? null : i;\n" +
		"	                        ^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	int r2 = b ? null : i;\n" +
		"	         ^^^^^^^^^^^^\n" +
		"The expression of type Integer is unboxed into int\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 5)\n" +
		"	int r2 = b ? null : i;\n" +
		"	                    ^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 7)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
public void test127() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public static void main(String[] s) {\n" +
			"                Object[] os1 = new Object[] {(long)1234567};\n" +
			"                Object[] os2 = new Object[] {1234567};\n" +
			"                Object o1 = os1[0], o2 = os2[0];\n" +
			"                if (o1.getClass().equals(o2.getClass())) {\n" +
			"                    System.out.println(\"FAILED:o1[\"+o1.getClass().getName()+\"],o2:[\"+o2.getClass()+\"]\");\n" +
			"                } else {\n" +
			"                    System.out.println(\"SUCCESS:o1[\"+o1.getClass().getName()+\"],o2:[\"+o2.getClass()+\"]\");\n" +
			"                }\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS:o1[java.lang.Long],o2:[class java.lang.Integer]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159987
public void test128() {
	// check there is no unncessary cast warning when autoboxing, even in array initializer
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] s) {\n" +
			"		Object o1 = (long) 1234567;\n" +
			"		Object[] os1 = new Object[] { (long) 1234567 };\n" +
			"		Object[] os2 = { (long) 1234567 };\n" +
			"		foo((long) 1234567);\n" +
			"	}\n" +
			"	static void foo(Object o) {\n" +
			"	}\n" +
			"	Zork z;\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	Object o1 = (long) 1234567;\n" +
		"	            ^^^^^^^^^^^^^^\n" +
		"The expression of type long is boxed into Long\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	Object[] os1 = new Object[] { (long) 1234567 };\n" +
		"	                              ^^^^^^^^^^^^^^\n" +
		"The expression of type long is boxed into Long\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 5)\n" +
		"	Object[] os2 = { (long) 1234567 };\n" +
		"	                 ^^^^^^^^^^^^^^\n" +
		"The expression of type long is boxed into Long\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 6)\n" +
		"	foo((long) 1234567);\n" +
		"	    ^^^^^^^^^^^^^^\n" +
		"The expression of type long is boxed into Long\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 10)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155104
public void test129() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X{\n" +
			"   java.io.Serializable field=this==null?8:\"\".getBytes();\n" +
			"	Zork z;\n" +
			"}\n", // =================
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\r\n" +
		"	java.io.Serializable field=this==null?8:\"\".getBytes();\r\n" +
		"	                                      ^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\r\n" +
		"	Zork z;\r\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174879
public void test130() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	Boolean[] myBool = new Boolean[1];\n" +
			"	void foo() {\n" +
			"		if (this.myBool[0]) {}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			new X().foo();\n" +
			"			System.out.println(\"FAILURE\");\n" +
			"		} catch(NullPointerException e) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174879
public void test131() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	Boolean myBool = null;\n" +
			"	void foo() {\n" +
			"		if (myBool) {}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			new X().foo();\n" +
			"			System.out.println(\"FAILURE\");\n" +
			"		} catch(NullPointerException e) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174879
public void test132() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static Boolean myBool = null;\n" +
			"	static void foo() {\n" +
			"		if (myBool) {}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			foo();\n" +
			"			System.out.println(\"FAILURE\");\n" +
			"		} catch(NullPointerException e) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174879
public void test133() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	Boolean myBool = null;\n" +
			"	void foo() {\n" +
			"		if (this.myBool) {}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			new X().foo();\n" +
			"			System.out.println(\"FAILURE\");\n" +
			"		} catch(NullPointerException e) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174879
public void test134() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static Boolean MyBool = null;\n" +
			"	static void foo() {\n" +
			"		if (X.MyBool) {}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			foo();\n" +
			"			System.out.println(\"FAILURE\");\n" +
			"		} catch(NullPointerException e) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372
public void test135() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"        public T foo() { return null; }\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                A<Long> a = new A<Long>();\n" +
			"				 A ua = a;\n" +
			"                try {\n" +
			"	                long s = a.foo();\n" +
			"                } catch(NullPointerException e) {\n" +
			"                	System.out.println(\"SUCCESS\");\n" +
			"                	return;\n" +
			"                }\n" +
			"            	System.out.println(\"FAILED\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test136() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"        public T foo(Object o) {\n" +
			"                return (T) o; // should get unchecked warning\n" +
			"        }\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                A<Long> a = new A<Long>();\n" +
			"                try {\n" +
			"	                long s = a.foo(new Object());\n" +
			"                } catch(ClassCastException e) {\n" +
			"                	System.out.println(\"SUCCESS\");\n" +
			"                	return;\n" +
			"                }\n" +
			"            	System.out.println(\"FAILED\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test137() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"        public T foo;\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                A<Long> a = new A<Long>();\n" +
			"				 A ua = a;\n" +
			"				 ua.foo = new Object();\n" +
			"                try {\n" +
			"	                long s = a.foo;\n" +
			"                } catch(ClassCastException e) {\n" +
			"                	System.out.println(\"SUCCESS\");\n" +
			"                	return;\n" +
			"                }\n" +
			"            	System.out.println(\"FAILED\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test138() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"        public T foo;\n" +
			"}\n" +
			"\n" +
			"public class X extends A<Long>{\n" +
			"        public static void main(String[] args) {\n" +
			"			new X().foo();\n" +
			"		 }\n" +
			" 		 public void foo() {\n" +
			"				 A ua = this;\n" +
			"				 ua.foo = new Object();\n" +
			"                try {\n" +
			"	                long s = foo;\n" +
			"                } catch(ClassCastException e) {\n" +
			"                	System.out.println(\"SUCCESS\");\n" +
			"                	return;\n" +
			"                }\n" +
			"            	System.out.println(\"FAILED\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test139() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"        public T foo;\n" +
			"}\n" +
			"\n" +
			"public class X extends A<Long>{\n" +
			"        public static void main(String[] args) {\n" +
			"			new X().foo();\n" +
			"		 }\n" +
			" 		 public void foo() {\n" +
			"				 A ua = this;\n" +
			"				 ua.foo = new Object();\n" +
			"                try {\n" +
			"	                long s = this.foo;\n" +
			"                } catch(ClassCastException e) {\n" +
			"                	System.out.println(\"SUCCESS\");\n" +
			"                	return;\n" +
			"                }\n" +
			"            	System.out.println(\"FAILED\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test140() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A {\n" +
			"        long foo() {\n" +
			"                return 0L;\n" +
			"        }\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                A a = new A();\n" +
			"	             Long s = a.foo();\n" +
			"                System.out.println(\"SUCCESS\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test141() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A {\n" +
			"        long foo = 0L;\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                A a = new A();\n" +
			"	             Long s = a.foo;\n" +
			"                System.out.println(\"SUCCESS\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test142() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A {\n" +
			"        long foo = 0L;\n" +
			"}\n" +
			"\n" +
			"public class X extends A {\n" +
			"        public static void main(String[] args) {\n" +
			"			new X().bar();\n" +
			"        }\n" +
			"		void bar() {\n" +
			"	             Long s = foo;\n" +
			"                System.out.println(\"SUCCESS\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test143() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A {\n" +
			"        long foo = 0L;\n" +
			"}\n" +
			"\n" +
			"public class X extends A {\n" +
			"        public static void main(String[] args) {\n" +
			"			new X().bar();\n" +
			"        }\n" +
			"		void bar() {\n" +
			"	             Long s = this.foo;\n" +
			"                System.out.println(\"SUCCESS\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test144() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"        public T[] foo;\n" +
			"}\n" +
			"\n" +
			"public class X extends A<Long>{\n" +
			"        public static void main(String[] args) {\n" +
			"			new X().foo();\n" +
			"		 }\n" +
			" 		 public void foo() {\n" +
			"				 A ua = this;\n" +
			"				 ua.foo = new Object[1];\n" +
			"                try {\n" +
			"	                long s = this.foo[0];\n" +
			"                } catch(ClassCastException e) {\n" +
			"                	System.out.println(\"SUCCESS\");\n" +
			"                	return;\n" +
			"                }\n" +
			"            	System.out.println(\"FAILED\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test145() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A {\n" +
			"        long[] foo = { 0L };\n" +
			"}\n" +
			"\n" +
			"public class X extends A {\n" +
			"        public static void main(String[] args) {\n" +
			"			new X().bar();\n" +
			"        }\n" +
			"		void bar() {\n" +
			"	             Long s = this.foo[0];\n" +
			"                System.out.println(\"SUCCESS\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=177372 - variation
public void test146() {
	Map settings = getCompilerOptions();
	settings.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"        public T foo;\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"            A<Long> a = new A<Long>();\n" +
			"	         long s = a.foo.MAX_VALUE;\n" +
			"            System.out.println(\"SUCCESS\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS",
		null,
		true,
		null,
		settings,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test147() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		if(new Integer(2) == 0) {}\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test148() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Z test = new Z(1, 1);\n" +
			"		System.out.println(\"SUCCESS\" + test.foo());\n" +
			"	}\n" +
			"}",
			"Z.java",
			"class Z {\n" +
			"	public <A, B extends A> Z(A a, B b) {\n" +
			"	}\n" +
			"	public int foo() {\n" +
			"		return 0;\n" +
			"	}\n" +
			"}"
		},
		"SUCCESS0");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test149() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Z test = new Z(1, 1);\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}",
			"Z.java",
			"class Z {\n" +
			"	public <A, B extends A> Z(A a, B b) {\n" +
			"	}\n" +
			"}"
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test150() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		if(new Integer(2) == 0) {\n" +
			"			System.out.println(\"FAILED\");\n" +
			"		} else {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test151() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		if(new Double(2.0) == 0.0) {\n" +
			"			System.out.println(\"FAILED\");\n" +
			"		} else {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=184957
public void test152() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		if(new Double(2.0) == 0.0) {}\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223685
public void test153() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		Integer a = 0;\n" +
			"		char b = (char)((int)a);\n" +
			"		char c = (char)(a + 1);\n" +
			"		char d = (char)(a);\n" +
			"		int e = (int) a;\n" +
			"		Integer f = (Integer) e;\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		X x = (X) null;\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	Integer a = 0;\n" +
		"	            ^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	char b = (char)((int)a);\n" +
		"	                     ^\n" +
		"The expression of type Integer is unboxed into int\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 5)\n" +
		"	char c = (char)(a + 1);\n" +
		"	                ^\n" +
		"The expression of type Integer is unboxed into int\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 6)\n" +
		"	char d = (char)(a);\n" +
		"	         ^^^^^^^^^\n" +
		"Cannot cast from Integer to char\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 7)\n" +
		"	int e = (int) a;\n" +
		"	        ^^^^^^^\n" +
		"Unnecessary cast from Integer to int\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 7)\n" +
		"	int e = (int) a;\n" +
		"	              ^\n" +
		"The expression of type Integer is unboxed into int\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 8)\n" +
		"	Integer f = (Integer) e;\n" +
		"	            ^^^^^^^^^^^\n" +
		"Unnecessary cast from int to Integer\n" +
		"----------\n" +
		"8. WARNING in X.java (at line 8)\n" +
		"	Integer f = (Integer) e;\n" +
		"	                      ^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"9. WARNING in X.java (at line 11)\n" +
		"	X x = (X) null;\n" +
		"	      ^^^^^^^^\n" +
		"Unnecessary cast from null to X\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565
public void test154() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"        T counter;\n" +
			"        public static void main(String[] args) {\n" +
			"        	 bar(new X<Integer>());\n" +
			"        	 new Y().foo();\n" +
			"        	 new Y().baz();\n" +
			"        }\n" +
			"        static void bar(X<Integer> x) {\n" +
			"        	x.counter = 0;\n" +
			"            System.out.print(Integer.toString(x.counter++));\n" +
			"        }\n" +
			"}\n" +
			"\n" +
			"class Y extends X<Integer> {\n" +
			"	Y() {\n" +
			"		this.counter = 0;\n" +
			"	}\n" +
			"    void foo() {\n" +
			"        System.out.print(Integer.toString(counter++));\n" +
			"    }\n" +
			"    void baz() {\n" +
			"        System.out.println(Integer.toString(this.counter++));\n" +
			"    }\n" +
			"}\n",
		},
		"000");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test155() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"        T[] counter;\n" +
			"        public static void main(String[] args) {\n" +
			"        	 bar(new X<Integer>());\n" +
			"        	 new Y().foo();\n" +
			"        	 new Y().baz();\n" +
			"        }\n" +
			"        static void bar(X<Integer> x) {\n" +
			"        	x.counter = new Integer[]{ 0 };\n" +
			"            System.out.print(Integer.toString(x.counter[0]++));\n" +
			"        }\n" +
			"}\n" +
			"\n" +
			"class Y extends X<Integer> {\n" +
			"	Y() {\n" +
			"		this.counter =  new Integer[]{ 0 };\n" +
			"	}\n" +
			"    void foo() {\n" +
			"        System.out.print(Integer.toString(counter[0]++));\n" +
			"    }\n" +
			"    void baz() {\n" +
			"        System.out.println(Integer.toString(this.counter[0]++));\n" +
			"    }\n" +
			"}\n",
		},
		"000");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test156() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static void print(Character c) {\n" +
			"		System.out.print((char) c);\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		char c = \'H\';\n" +
			"		print(c++);\n" +
			"		print(c++);\n" +
			"		System.out.println(\"done\");\n" +
			"    }\n" +
			"}\n",
		},
		"HIdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test157() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	char c = \'H\';\n" +
			"	static void print(Character c) {\n" +
			"		System.out.print((char) c);\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X();\n" +
			"		print(x.c++);\n" +
			"		print(x.c++);\n" +
			"		System.out.println(\"done\");\n" +
			"    }\n" +
			"}\n",
		},
		"HIdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test158() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static X singleton = new X();\n" +
			"	static X singleton() { return singleton; }\n" +
			"	char c = \'H\';\n" +
			"	\n" +
			"	static void print(Character c) {\n" +
			"		System.out.print((char) c);\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		print(singleton().c++);\n" +
			"		print(singleton().c++);\n" +
			"		System.out.println(\"done\");\n" +
			"    }\n" +
			"}\n",
		},
		"HIdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236019
public void test159() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"public class X {\n" +
			"    ArrayList params;\n" +
			"    public int getSqlParamCount() {\n" +
			"        return params == null ? null:params.size();\n" +
			"    }\n" +
			"    public int getSqlParamCount2() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	ArrayList params;\n" +
		"	^^^^^^^^^\n" +
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	return params == null ? null:params.size();\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The expression of type Integer is unboxed into int\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 5)\n" +
		"	return params == null ? null:params.size();\n" +
		"	                             ^^^^^^^^^^^^^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 8)\n" +
		"	return null;\n" +
		"	       ^^^^\n" +
		"Type mismatch: cannot convert from null to int\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test160() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"        T counter;\n" +
			"        public static void main(String[] args) {\n" +
			"        	 bar(new X<Integer>());\n" +
			"        	 new Y().foo();\n" +
			"        	 new Y().baz();\n" +
			"        }\n" +
			"        static void bar(X<Integer> x) {\n" +
			"        	x.counter = 0;\n" +
			"            System.out.print(Integer.toString(++x.counter));\n" +
			"        }\n" +
			"}\n" +
			"\n" +
			"class Y extends X<Integer> {\n" +
			"	Y() {\n" +
			"		this.counter = 0;\n" +
			"	}\n" +
			"    void foo() {\n" +
			"        System.out.print(Integer.toString(++counter));\n" +
			"    }\n" +
			"    void baz() {\n" +
			"        System.out.println(Integer.toString(++this.counter));\n" +
			"    }\n" +
			"}\n",
		},
		"111");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test161() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"        T[] counter;\n" +
			"        public static void main(String[] args) {\n" +
			"        	 bar(new X<Integer>());\n" +
			"        	 new Y().foo();\n" +
			"        	 new Y().baz();\n" +
			"        }\n" +
			"        static void bar(X<Integer> x) {\n" +
			"        	x.counter = new Integer[]{ 0 };\n" +
			"            System.out.print(Integer.toString(++x.counter[0]));\n" +
			"        }\n" +
			"}\n" +
			"\n" +
			"class Y extends X<Integer> {\n" +
			"	Y() {\n" +
			"		this.counter =  new Integer[]{ 0 };\n" +
			"	}\n" +
			"    void foo() {\n" +
			"        System.out.print(Integer.toString(++counter[0]));\n" +
			"    }\n" +
			"    void baz() {\n" +
			"        System.out.println(Integer.toString(++this.counter[0]));\n" +
			"    }\n" +
			"}\n",
		},
		"111");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test162() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static void print(Character c) {\n" +
			"		System.out.print((char) c);\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		char c = \'H\';\n" +
			"		print(++c);\n" +
			"		print(++c);\n" +
			"		System.out.println(\"done\");\n" +
			"    }\n" +
			"}\n",
		},
		"IJdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test163() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	char c = \'H\';\n" +
			"	static void print(Character c) {\n" +
			"		System.out.print((char) c);\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X();\n" +
			"		print(++x.c);\n" +
			"		print(++x.c);\n" +
			"		System.out.println(\"done\");\n" +
			"    }\n" +
			"}\n",
		},
		"IJdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=232565 - variation
public void test164() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static X singleton = new X();\n" +
			"	static X singleton() { return singleton; }\n" +
			"	char c = \'H\';\n" +
			"	\n" +
			"	static void print(Character c) {\n" +
			"		System.out.print((char) c);\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		print(++singleton().c);\n" +
			"		print(++singleton().c);\n" +
			"		System.out.println(\"done\");\n" +
			"    }\n" +
			"}\n",
		},
		"IJdone");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=231709
public void test165() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void foo() {\n" +
			"        Integer i1 = 10 ;\n" +
			"        final short s = 100;\n" +
			"        i1 = s;\n" +
			"        switch (i1)\n" +
			"        {\n" +
			"            case s:\n" +
			"        }\n" +
			"    }\n" +
			"    public void bar() {\n" +
			"        Integer i2 = 10 ;\n" +
			"        final byte b = 100;\n" +
			"        i2 = b;\n" +
			"        switch (i2)\n" +
			"        {\n" +
			"            case b:\n" +
			"        }\n" +
			"    }   \n" +
			"    public void baz() {\n" +
			"        Integer i3 = 10 ;\n" +
			"        final char c = 100;\n" +
			"        i3 = c;\n" +
			"        switch (i3)\n" +
			"        {\n" +
			"            case c:\n" +
			"        }\n" +
			"    }     \n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	Integer i1 = 10 ;\n" +
		"	             ^^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	i1 = s;\n" +
		"	     ^\n" +
		"Type mismatch: cannot convert from short to Integer\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\n" +
		"	switch (i1)\n" +
		"	        ^^\n" +
		"The expression of type Integer is unboxed into int\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 8)\n" +
		"	case s:\n" +
		"	     ^\n" +
		"Case constant of type short is incompatible with switch selector type Integer\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 12)\n" +
		"	Integer i2 = 10 ;\n" +
		"	             ^^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 14)\n" +
		"	i2 = b;\n" +
		"	     ^\n" +
		"Type mismatch: cannot convert from byte to Integer\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 15)\n" +
		"	switch (i2)\n" +
		"	        ^^\n" +
		"The expression of type Integer is unboxed into int\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 17)\n" +
		"	case b:\n" +
		"	     ^\n" +
		"Case constant of type byte is incompatible with switch selector type Integer\n" +
		"----------\n" +
		"9. WARNING in X.java (at line 21)\n" +
		"	Integer i3 = 10 ;\n" +
		"	             ^^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"10. ERROR in X.java (at line 23)\n" +
		"	i3 = c;\n" +
		"	     ^\n" +
		"Type mismatch: cannot convert from char to Integer\n" +
		"----------\n" +
		"11. WARNING in X.java (at line 24)\n" +
		"	switch (i3)\n" +
		"	        ^^\n" +
		"The expression of type Integer is unboxed into int\n" +
		"----------\n" +
		"12. ERROR in X.java (at line 26)\n" +
		"	case c:\n" +
		"	     ^\n" +
		"Case constant of type char is incompatible with switch selector type Integer\n" +
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=231709 - variation
public void test166() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(short s, byte b, char c) {\n" +
			"		Integer is = s;\n" +
			"		Integer ib = b;\n" +
			"		Integer ic = c;	\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		final short s = 0;\n" +
			"		final byte b = 0;\n" +
			"		final char c = 0;\n" +
			"		Integer is = s;\n" +
			"		Integer ib = b;\n" +
			"		Integer ic = c;	\n" +
			"	}\n" +
			"	void foo2() {\n" +
			"		Integer is = (short)0;\n" +
			"		Integer ib = (byte)0;\n" +
			"		Integer ic = (char)0;	\n" +
			"	}\n" +
			"	void foo3() {\n" +
			"		Short si = 0;\n" +
			"		Byte bi = 0;\n" +
			"		Character ci = 0;\n" +
			"	}\n" +
			"	void foo4() {\n" +
			"		Short si = (byte) 0;\n" +
			"		Byte bi = (short) 0;\n" +
			"		Character ci = (short) 0;\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	Integer is = s;\n" +
		"	             ^\n" +
		"Type mismatch: cannot convert from short to Integer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	Integer ib = b;\n" +
		"	             ^\n" +
		"Type mismatch: cannot convert from byte to Integer\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	Integer ic = c;	\n" +
		"	             ^\n" +
		"Type mismatch: cannot convert from char to Integer\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	Integer is = s;\n" +
		"	             ^\n" +
		"Type mismatch: cannot convert from short to Integer\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 12)\n" +
		"	Integer ib = b;\n" +
		"	             ^\n" +
		"Type mismatch: cannot convert from byte to Integer\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 13)\n" +
		"	Integer ic = c;	\n" +
		"	             ^\n" +
		"Type mismatch: cannot convert from char to Integer\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 16)\n" +
		"	Integer is = (short)0;\n" +
		"	             ^^^^^^^^\n" +
		"Type mismatch: cannot convert from short to Integer\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 17)\n" +
		"	Integer ib = (byte)0;\n" +
		"	             ^^^^^^^\n" +
		"Type mismatch: cannot convert from byte to Integer\n" +
		"----------\n" +
		"9. ERROR in X.java (at line 18)\n" +
		"	Integer ic = (char)0;	\n" +
		"	             ^^^^^^^\n" +
		"Type mismatch: cannot convert from char to Integer\n" +
		"----------\n" +
		"10. WARNING in X.java (at line 21)\n" +
		"	Short si = 0;\n" +
		"	           ^\n" +
		"The expression of type int is boxed into Short\n" +
		"----------\n" +
		"11. WARNING in X.java (at line 22)\n" +
		"	Byte bi = 0;\n" +
		"	          ^\n" +
		"The expression of type int is boxed into Byte\n" +
		"----------\n" +
		"12. WARNING in X.java (at line 23)\n" +
		"	Character ci = 0;\n" +
		"	               ^\n" +
		"The expression of type int is boxed into Character\n" +
		"----------\n" +
		"13. WARNING in X.java (at line 26)\n" +
		"	Short si = (byte) 0;\n" +
		"	           ^^^^^^^^\n" +
		"The expression of type byte is boxed into Short\n" +
		"----------\n" +
		"14. WARNING in X.java (at line 27)\n" +
		"	Byte bi = (short) 0;\n" +
		"	          ^^^^^^^^^\n" +
		"The expression of type short is boxed into Byte\n" +
		"----------\n" +
		"15. WARNING in X.java (at line 28)\n" +
		"	Character ci = (short) 0;\n" +
		"	               ^^^^^^^^^\n" +
		"The expression of type short is boxed into Character\n" +
		"----------\n");
}
public void test167() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"     String foo(Comparable<String> x) {\n" +
			"       System.out.println( \"one\" );" +
			"		return null;\n" +
			"     }\n" +
			"     void foo(int x) {\n" +
			"       System.out.println( \"two\" );\n" +
			"     }\n" +
			"	void bar() {\n" +
			"       Integer i = 1;\n" +
			"       String s = foo(i); \n" +
			"     }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 9)\n" +
		"	Integer i = 1;\n" +
		"	            ^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	String s = foo(i); \n" +
		"	           ^^^^^^\n" +
		"Type mismatch: cannot convert from void to String\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 10)\n" +
		"	String s = foo(i); \n" +
		"	               ^\n" +
		"The expression of type Integer is unboxed into int\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=264843
public void test168() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"@SuppressWarnings(\"deprecation\")\n" +
			"public class X {\n" +
			"    <T extends Integer> T a() { return 35; }\n" +
			"    <T extends Integer> T[] b() { return new int[]{35}; }\n" +
			"    <T extends Integer> T c() { return Integer.valueOf(35); }\n" +
			"    <T extends Integer> T[] d() { return new Integer[]{35}; }\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	<T extends Integer> T a() { return 35; }\n" +
		"	           ^^^^^^^\n" +
		"The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	<T extends Integer> T a() { return 35; }\n" +
		"	                                   ^^\n" +
		"Type mismatch: cannot convert from int to T\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 4)\n" +
		"	<T extends Integer> T[] b() { return new int[]{35}; }\n" +
		"	           ^^^^^^^\n" +
		"The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	<T extends Integer> T[] b() { return new int[]{35}; }\n" +
		"	                                     ^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from int[] to T[]\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 5)\n" +
		"	<T extends Integer> T c() { return Integer.valueOf(35); }\n" +
		"	           ^^^^^^^\n" +
		"The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 5)\n" +
		"	<T extends Integer> T c() { return Integer.valueOf(35); }\n" +
		"	                                   ^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Integer to T\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 6)\n" +
		"	<T extends Integer> T[] d() { return new Integer[]{35}; }\n" +
		"	           ^^^^^^^\n" +
		"The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 6)\n" +
		"	<T extends Integer> T[] d() { return new Integer[]{35}; }\n" +
		"	                                     ^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Integer[] to T[]\n" +
		"----------\n" +
		"9. WARNING in X.java (at line 6)\n" +
		"	<T extends Integer> T[] d() { return new Integer[]{35}; }\n" +
		"	                                                   ^^\n" +
		"The expression of type int is boxed into Integer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=264843
public void test169() {
	String expectedCompilerLog = this.complianceLevel >= ClassFileConstants.JDK21 ?
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public class X<T extends Integer> {\n" +
			"	                         ^^^^^^^\n" +
			"The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	T x = 12;\n" +
			"	      ^^\n" +
			"Type mismatch: cannot convert from int to T\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 3)\n" +
			"	Byte y = 12;\n" +
			"	         ^^\n" +
			"The expression of type int is boxed into Byte\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	t = 5;\n" +
			"	    ^\n" +
			"Type mismatch: cannot convert from int to T\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 6)\n" +
			"	switch (t) {\n" +
			"	        ^\n" +
			"The expression of type T is unboxed into int\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 6)\n" +
			"	switch (t) {\n" +
			"	        ^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 7)\n" +
			"	case 1:\n" +
			"	     ^\n" +
			"Case constant of type int is incompatible with switch selector type T\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 12)\n" +
			"	t = 5;\n" +
			"	    ^\n" +
			"The expression of type int is boxed into Byte\n" +
			"----------\n" +
			"9. WARNING in X.java (at line 13)\n" +
			"	switch (t) {\n" +
			"	        ^\n" +
			"The expression of type Byte is unboxed into int\n" +
			"----------\n"
	  :
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public class X<T extends Integer> {\n" +
			"	                         ^^^^^^^\n" +
			"The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	T x = 12;\n" +
			"	      ^^\n" +
			"Type mismatch: cannot convert from int to T\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 3)\n" +
			"	Byte y = 12;\n" +
			"	         ^^\n" +
			"The expression of type int is boxed into Byte\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	t = 5;\n" +
			"	    ^\n" +
			"Type mismatch: cannot convert from int to T\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 6)\n" +
			"	switch (t) {\n" +
			"	        ^\n" +
			"The expression of type T is unboxed into int\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 7)\n" +
			"	case 1:\n" +
			"	     ^\n" +
			"Case constant of type int is incompatible with switch selector type T\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 12)\n" +
			"	t = 5;\n" +
			"	    ^\n" +
			"The expression of type int is boxed into Byte\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 13)\n" +
			"	switch (t) {\n" +
			"	        ^\n" +
			"The expression of type Byte is unboxed into int\n" +
			"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T extends Integer> {\n" +
			"    T x = 12;\n" +
			"    Byte y = 12;\n" +
			"    	void x(T t) {\n" +
			"    		t = 5;\n" +
			"    		switch (t) {\n" +
			"    		case 1:\n" +
			"    			break;\n" +
			"    		}\n" +
			"    	}\n" +
			"    	void y(Byte t) {\n" +
			"    		t = 5;\n" +
			"    		switch (t) {\n" +
			"    		case 1:\n" +
			"    			break;\n" +
			"    		}\n" +
			"    	}\n" +
			"}\n",
		},
		expectedCompilerLog);
}
}
