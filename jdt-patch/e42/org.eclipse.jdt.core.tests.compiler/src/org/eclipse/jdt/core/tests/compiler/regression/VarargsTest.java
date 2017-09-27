/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class VarargsTest extends AbstractComparableTest {

	public VarargsTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test070b" };
//		TESTS_NUMBERS = new int[] { 61 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return VarargsTest.class;
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y y = new Y();\n" +
				"		y = new Y(null);\n" +
				"		y = new Y(1);\n" +
				"		y = new Y(1, 2, (byte) 3, 4);\n" +
				"		y = new Y(new int[] {1, 2, 3, 4 });\n" +
				"		\n" +
				"		Y.count();\n" +
				"		Y.count(null);\n" +
				"		Y.count(1);\n" +
				"		Y.count(1, 2, (byte) 3, 4);\n" +
				"		Y.count(new int[] {1, 2, 3, 4 });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public Y(int ... values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(' ');\n" +
				"	}\n" +
				"	public static void count(int ... values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(' ');\n" +
				"	}\n" +
				"}\n",
			},
			"<0 0 1 10 10 0 0 1 10 10 >");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y y = new Y();\n" +
				"		y = new Y(null);\n" +
				"		y = new Y(1);\n" +
				"		y = new Y(1, 2, (byte) 3, 4);\n" +
				"		y = new Y(new int[] {1, 2, 3, 4 });\n" +
				"		\n" +
				"		Y.count();\n" +
				"		Y.count(null);\n" +
				"		Y.count(1);\n" +
				"		Y.count(1, 2, (byte) 3, 4);\n" +
				"		Y.count(new int[] {1, 2, 3, 4 });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n",
			},
			"<0 0 1 10 10 0 0 1 10 10 >",
			null,
			false,
			null);
	}

	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y y = new Y();\n" +
				"		y = new Y(null);\n" +
				"		y = new Y(1);\n" +
				"		y = new Y(1, 2, (byte) 3, 4);\n" +
				"		y = new Y(new int[] {1, 2, 3, 4 });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y extends Z {\n" +
				"	public Y(int ... values) { super(values); }\n" +
				"}\n" +
				"class Z {\n" +
				"	public Z(int ... values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(' ');\n" +
				"	}\n" +
				"}\n",
			},
			"<0 0 1 10 10 >");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y y = new Y();\n" +
				"		y = new Y(null);\n" +
				"		y = new Y(1);\n" +
				"		y = new Y(1, 2, (byte) 3, 4);\n" +
				"		y = new Y(new int[] {1, 2, 3, 4 });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n",
			},
			"<0 0 1 10 10 >",
			null,
			false,
			null);
	}

	public void test003() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count();\n" +
				"		Y.count((int[]) null);\n" +
				"		Y.count((int[][]) null);\n" +
				"		Y.count(new int[] {1});\n" +
				"		Y.count(new int[] {1, 2}, new int[] {3, 4});\n" +
				"		Y.count(new int[][] {new int[] {1, 2, 3}, new int[] {4}});\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int count(int[] values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(' ');\n" +
				"		System.out.print(result);\n" +
				"		return result;\n" +
				"	}\n" +
				"	public static void count(int[] ... values) {\n" +
				"		int result = 0;\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += count(values[i]);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(result);\n" +
				"	}\n" +
				"}\n",
			},
			"<=0 0=0 1 3 7=10 6 4=10>");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count();\n" +
				"		Y.count((int[]) null);\n" +
				"		Y.count((int[][]) null);\n" +
				"		Y.count(new int[] {1});\n" +
				"		Y.count(new int[] {1, 2}, new int[] {3, 4});\n" +
				"		Y.count(new int[][] {new int[] {1, 2, 3}, new int[] {4}});\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n"
			},
			"<=0 0=0 1 3 7=10 6 4=10>",
			null,
			false,
			null);
	}

	public void test004() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count(0);\n" +
				"		Y.count(-1, (int[]) null);\n" +
				"		Y.count(-2, (int[][]) null);\n" +
				"		Y.count(1);\n" +
				"		Y.count(2, new int[] {1});\n" +
				"		Y.count(3, new int[] {1}, new int[] {2, 3}, new int[] {4});\n" +
				"		Y.count((byte) 4, new int[][] {new int[] {1}, new int[] {2, 3}, new int[] {4}});\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int count(int j, int[] values) {\n" +
				"		int result = j;\n" +
				"		System.out.print(' ');\n" +
				"		System.out.print('[');\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += values[i];\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(']');\n" +
				"		return result;\n" +
				"	}\n" +
				"	public static void count(int j, int[] ... values) {\n" +
				"		int result = j;\n" +
				"		System.out.print(' ');\n" +
				"		System.out.print(result);\n" +
				"		System.out.print(':');\n" +
				"		for (int i = 0, l = values == null ? 0 : values.length; i < l; i++)\n" +
				"			result += count(j, values[i]);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(result);\n" +
				"	}\n" +
				"}\n",
			},
			"< 0:=0 [-1] -2:=-2 1:=1 [3] 3: [4] [8] [7]=22 4: [5] [9] [8]=26>");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count(0);\n" +
				"		Y.count(-1, (int[]) null);\n" +
				"		Y.count(-2, (int[][]) null);\n" +
				"		Y.count(1);\n" +
				"		Y.count(2, new int[] {1});\n" +
				"		Y.count(3, new int[] {1}, new int[] {2, 3}, new int[] {4});\n" +
				"		Y.count((byte) 4, new int[][] {new int[] {1}, new int[] {2, 3}, new int[] {4}});\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n"
			},
			"< 0:=0 [-1] -2:=-2 1:=1 [3] 3: [4] [8] [7]=22 4: [5] [9] [8]=26>",
			null,
			false,
			null);
	}

	public void test005() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.print();\n" +
				"		Y.print(new Integer(1));\n" +
				"		Y.print(new Integer(1), new Byte((byte) 3), new Integer(7));\n" +
				"		Y.print(new Integer[] {new Integer(11) });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void print(Number ... values) {\n" +
				"		for (int i = 0, l = values.length; i < l; i++) {\n" +
				"			System.out.print(' ');\n" +
				"			System.out.print(values[i]);\n" +
				"		}\n" +
				"		System.out.print(',');\n" +
				"	}\n" +
				"}\n",
			},
			"<, 1, 1 3 7, 11,>");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.print();\n" +
				"		Y.print(new Integer(1));\n" +
				"		Y.print(new Integer(1), new Byte((byte) 3), new Integer(7));\n" +
				"		Y.print(new Integer[] {new Integer(11) });\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n",
			},
			"<, 1, 1 3 7, 11,>",
			null,
			false,
			null);
	}

	public void test006() { // 70056
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		String[] T_NAMES = new String[] {\"foo\"};\n" +
				"		String error = \"error\";\n" +
				"		Y.format(\"E_UNSUPPORTED_CONV\", new Integer(0));\n" +
				"		Y.format(\"E_SAVE\", T_NAMES[0], error);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static String format(String key) { return null; }\n" +
				"	public static String format(String key, Object ... args) { return null; }\n" +
				"}\n",
			},
			"");
	}

	public void test007() { // array dimension test compatibility with Object
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.byte2(null);\n" + // warning: inexact argument type for last parameter
				"		Y.byte2((byte) 1);\n" + // error
				"		Y.byte2(new byte[] {});\n" +
				"		Y.byte2(new byte[][] {});\n" +
				"		Y.byte2(new byte[][][] {});\n" + // error
				"\n" +
				"		Y.object(null);\n" + // warning
				"		Y.object((byte) 1);\n" +
				"		Y.object(new byte[] {});\n" +
				"		Y.object(new byte[][] {});\n" + // warning
				"		Y.object(new byte[][][] {});\n" + // warning
				"\n" +
				"		Y.object(new String());\n" +
				"		Y.object(new String[] {});\n" + // warning
				"		Y.object(new String[][] {});\n" + // warning
				"\n" +
				"		Y.object2(null);\n" + // warning
				"		Y.object2((byte) 1);\n" + // error
				"		Y.object2(new byte[] {});\n" + // error
				"		Y.object2(new byte[][] {});\n" +
				"		Y.object2(new byte[][][] {});\n" + // warning
				"\n" +
				"		Y.object2(new String());\n" + // error
				"		Y.object2(new String[] {});\n" +
				"		Y.object2(new String[][] {});\n" + // warning
				"\n" +
				"		Y.string(null);\n" + // warning
				"		Y.string(new String());\n" +
				"		Y.string(new String[] {});\n" +
				"		Y.string(new String[][] {});\n" + // error
				"\n" +
				"		Y.string(new Object());\n" + // error
				"		Y.string(new Object[] {});\n" + // error
				"		Y.string(new Object[][] {});\n" + // error
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void byte2(byte[] ... values) {}\n" +
				"	public static void object(Object ... values) {}\n" +
				"	public static void object2(Object[] ... values) {}\n" +
				"	public static void string(String ... values) {}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Y.byte2(null);\n" +
			"	^^^^^^^^^^^^^\n" +
			"The argument of type null should explicitly be cast to byte[][] for the invocation of the varargs method byte2(byte[]...) from type Y. It could alternatively be cast to byte[] for a varargs invocation\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Y.byte2((byte) 1);\n" +
			"	  ^^^^^\n" +
			"The method byte2(byte[]...) in the type Y is not applicable for the arguments (byte)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	Y.byte2(new byte[][][] {});\n" +
			"	  ^^^^^\n" +
			"The method byte2(byte[]...) in the type Y is not applicable for the arguments (byte[][][])\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 9)\n" +
			"	Y.object(null);\n" +
			"	^^^^^^^^^^^^^^\n" +
			"The argument of type null should explicitly be cast to Object[] for the invocation of the varargs method object(Object...) from type Y. It could alternatively be cast to Object for a varargs invocation\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 12)\n" +
			"	Y.object(new byte[][] {});\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The argument of type byte[][] should explicitly be cast to Object[] for the invocation of the varargs method object(Object...) from type Y. It could alternatively be cast to Object for a varargs invocation\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 13)\n" +
			"	Y.object(new byte[][][] {});\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The argument of type byte[][][] should explicitly be cast to Object[] for the invocation of the varargs method object(Object...) from type Y. It could alternatively be cast to Object for a varargs invocation\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 16)\n" +
			"	Y.object(new String[] {});\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The argument of type String[] should explicitly be cast to Object[] for the invocation of the varargs method object(Object...) from type Y. It could alternatively be cast to Object for a varargs invocation\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 17)\n" +
			"	Y.object(new String[][] {});\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The argument of type String[][] should explicitly be cast to Object[] for the invocation of the varargs method object(Object...) from type Y. It could alternatively be cast to Object for a varargs invocation\n" +
			"----------\n" +
			"9. WARNING in X.java (at line 19)\n" +
			"	Y.object2(null);\n" +
			"	^^^^^^^^^^^^^^^\n" +
			"The argument of type null should explicitly be cast to Object[][] for the invocation of the varargs method object2(Object[]...) from type Y. It could alternatively be cast to Object[] for a varargs invocation\n" +
			"----------\n" +
			"10. ERROR in X.java (at line 20)\n" +
			"	Y.object2((byte) 1);\n" +
			"	  ^^^^^^^\n" +
			"The method object2(Object[]...) in the type Y is not applicable for the arguments (byte)\n" +
			"----------\n" +
			"11. ERROR in X.java (at line 21)\n" +
			"	Y.object2(new byte[] {});\n" +
			"	  ^^^^^^^\n" +
			"The method object2(Object[]...) in the type Y is not applicable for the arguments (byte[])\n" +
			"----------\n" +
			"12. WARNING in X.java (at line 23)\n" +
			"	Y.object2(new byte[][][] {});\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The argument of type byte[][][] should explicitly be cast to Object[][] for the invocation of the varargs method object2(Object[]...) from type Y. It could alternatively be cast to Object[] for a varargs invocation\n" +
			"----------\n" +
			"13. ERROR in X.java (at line 25)\n" +
			"	Y.object2(new String());\n" +
			"	  ^^^^^^^\n" +
			"The method object2(Object[]...) in the type Y is not applicable for the arguments (String)\n" +
			"----------\n" +
			"14. WARNING in X.java (at line 27)\n" +
			"	Y.object2(new String[][] {});\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The argument of type String[][] should explicitly be cast to Object[][] for the invocation of the varargs method object2(Object[]...) from type Y. It could alternatively be cast to Object[] for a varargs invocation\n" +
			"----------\n" +
			"15. WARNING in X.java (at line 29)\n" +
			"	Y.string(null);\n" +
			"	^^^^^^^^^^^^^^\n" +
			"The argument of type null should explicitly be cast to String[] for the invocation of the varargs method string(String...) from type Y. It could alternatively be cast to String for a varargs invocation\n" +
			"----------\n" +
			"16. ERROR in X.java (at line 32)\n" +
			"	Y.string(new String[][] {});\n" +
			"	  ^^^^^^\n" +
			"The method string(String...) in the type Y is not applicable for the arguments (String[][])\n" +
			"----------\n" +
			"17. ERROR in X.java (at line 34)\n" +
			"	Y.string(new Object());\n" +
			"	  ^^^^^^\n" +
			"The method string(String...) in the type Y is not applicable for the arguments (Object)\n" +
			"----------\n" +
			"18. ERROR in X.java (at line 35)\n" +
			"	Y.string(new Object[] {});\n" +
			"	  ^^^^^^\n" +
			"The method string(String...) in the type Y is not applicable for the arguments (Object[])\n" +
			"----------\n" +
			"19. ERROR in X.java (at line 36)\n" +
			"	Y.string(new Object[][] {});\n" +
			"	  ^^^^^^\n" +
			"The method string(String...) in the type Y is not applicable for the arguments (Object[][])\n" +
			"----------\n");
	}

	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y y = new Y(null);\n" +
				"		y = new Y(true, null);\n" + // null warning
				"		y = new Y('i', null);\n" + // null warning
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public Y(int ... values) {}\n" +
				"	public Y(boolean b, Object ... values) {}\n" +
				"	public Y(char c, int[] ... values) {}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	y = new Y(true, null);\n" +
			"	    ^^^^^^^^^^^^^^^^^\n" +
			"The argument of type null should explicitly be cast to Object[] for the invocation of the varargs constructor Y(boolean, Object...). It could alternatively be cast to Object for a varargs invocation\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	y = new Y(\'i\', null);\n" +
			"	    ^^^^^^^^^^^^^^^^\n" +
			"The argument of type null should explicitly be cast to int[][] for the invocation of the varargs constructor Y(char, int[]...). It could alternatively be cast to int[] for a varargs invocation\n" +
			"----------\n");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y y = new Y(null);\n" +
				"		y = new Y(true, null);\n" + // null warning
				"		y = new Y('i', null);\n" + // null warning
				"	}\n" +
				"}\n" +
				"class Y extends Z {\n" +
				"	public Y(int ... values) { super(values); }\n" +
				"	public Y(boolean b, Object ... values) { super(b, values); }\n" +
				"	public Y(char c, int[] ... values) {}\n" +
				"}\n" +
				"class Z {\n" +
				"	public Z(int ... values) {}\n" +
				"	public Z(boolean b, Object ... values) {}\n" +
				"	public Z(char c, int[] ... values) {}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	y = new Y(true, null);\n" +
			"	    ^^^^^^^^^^^^^^^^^\n" +
			"The argument of type null should explicitly be cast to Object[] for the invocation of the varargs constructor Y(boolean, Object...). It could alternatively be cast to Object for a varargs invocation\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	y = new Y(\'i\', null);\n" +
			"	    ^^^^^^^^^^^^^^^^\n" +
			"The argument of type null should explicitly be cast to int[][] for the invocation of the varargs constructor Y(char, int[]...). It could alternatively be cast to int[] for a varargs invocation\n" +
			"----------\n");
	}

	public void test009() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count(null);\n" +
				"		Y.count(1);\n" +
				"		Y.count(1, 2);\n" +
				"\n" +
				"		Z.count(1L, 1);\n" + // only choice is Z.count(long, int)
				"		Z.count(1, 1);\n" + // chooses Z.count(long, long) over Z.count(int,int...)
				"		Z.count(1, null);\n" + // only choice is Z.count(int,int...)
				"		Z.count2(1, null);\n" + // better choice is Z.count(int,int[])
				"		Z.count2(1L, null);\n" + // better choice is Z.count(long,int...)
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(int values) { System.out.print('1'); }\n" +
				"	public static void count(int ... values) { System.out.print('2'); }\n" +
				"}\n" +
				"class Z {\n" +
				"	public static void count(long l, long values) { System.out.print('3'); }\n" +
				"	public static void count(int i, int ... values) { System.out.print('4'); }\n" +
				"	public static void count2(int i, int values) { System.out.print('5'); }\n" +
				"	public static void count2(long l, int ... values) { System.out.print('6'); }\n" +
				"}\n",
			},
			"<21233466>");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.test((Object[]) null);\n" + // cast to avoid null warning
				"		Y.test(null, null);\n" +
				"		Y.test(null, null, null);\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void test(Object o, Object o2) { System.out.print('1'); }\n" +
				"	public static void test(Object ... values) { System.out.print('2'); }\n" +
				"}\n",
			},
			"<212>");
	}

	public void test010() {
		// according to spec this should find count(Object) since it should consider count(Object...) as count(Object[]) until all fixed arity methods are ruled out
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count((Object) new Integer(1));\n" +
				"		Y.count(new Integer(1));\n" +
				"\n" +
				"		Y.count((Object) null);\n" +
				"		Y.count((Object[]) null);\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(Object values) { System.out.print('1'); }\n" +
				"	public static void count(Object ... values) { System.out.print('2'); }\n" +
				"}\n",
			},
			"<1112>");
		// according to spec this should find count(Object[]) since it should consider count(Object[]...) as count(Object[][]) until all fixed arity methods are ruled out
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print('<');\n" +
				"		Y.count(new Object[] {new Integer(1)});\n" +
				"		Y.count(new Integer[] {new Integer(1)});\n" +
				"\n" +
				"		Y.count((Object[]) null);\n" +
				"		Y.count((Object[][]) null);\n" +
				"		System.out.print('>');\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(Object[] values) { System.out.print('1'); }\n" +
				"	public static void count(Object[] ... values) { System.out.print('2'); }\n" +
				"}\n",
			},
			"<1112>");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.string(null);\n" +
				"		Y.string2(null);\n" +
				"		Y.int2(null);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void string(String values) { System.out.print('1'); }\n" +
				"	public static void string(String ... values) { System.out.print('2'); }\n" +
				"	public static void string2(String[] values) { System.out.print('1'); }\n" +
				"	public static void string2(String[] ... values) { System.out.print('2'); }\n" +
				"	public static void int2(int[] values) { System.out.print('1'); }\n" +
				"	public static void int2(int[] ... values) { System.out.print('2'); }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Y.string(null);\n" +
			"	  ^^^^^^\n" +
			"The method string(String) is ambiguous for the type Y\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Y.string2(null);\n" +
			"	  ^^^^^^^\n" +
			"The method string2(String[]) is ambiguous for the type Y\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	Y.int2(null);\n" +
			"	  ^^^^\n" +
			"The method int2(int[]) is ambiguous for the type Y\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83379
	public void test011() {
		runConformTest(
			true,
			new String[] {
				"X.java",
				"public class X { void count(int ... values) {} }\n" +
				"class Y extends X { void count(int[] values) {} }\n" +
				"class Z extends Y { void count(int... values) {} }\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 2)\n" +
			"	class Y extends X { void count(int[] values) {} }\n" +
			"	                         ^^^^^^^^^^^^^^^^^^^\n" +
			"Varargs methods should only override or be overridden by other varargs methods unlike Y.count(int[]) and X.count(int...)\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 2)\n" +
			"	class Y extends X { void count(int[] values) {} }\n" +
			"	                         ^^^^^^^^^^^^^^^^^^^\n" +
			"The method count(int[]) of type Y should be tagged with @Override since it actually overrides a superclass method\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 3)\n" +
			"	class Z extends Y { void count(int... values) {} }\n" +
			"	                         ^^^^^^^^^^^^^^^^^^^^\n" +
			"Varargs methods should only override or be overridden by other varargs methods unlike Z.count(int...) and Y.count(int[])\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 3)\n" +
			"	class Z extends Y { void count(int... values) {} }\n" +
			"	                         ^^^^^^^^^^^^^^^^^^^^\n" +
			"The method count(int...) of type Z should be tagged with @Override since it actually overrides a superclass method\n" +
			"----------\n",
			null,
			null,
			JavacTestOptions.EclipseHasABug.EclipseBug236379);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77084
	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
					"   public static void main (String ... args) {\n" +
					"       for (String a:args) {\n" +
					"           System.out.println(a);\n" +
					"       }\n" +
					"   }\n" +
					"}\n" +
					"\n"
			}
		);
	}

	public void test013() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count(1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(long i, int j) { System.out.print(1); }\n" +
				"	public static void count(int ... values) { System.out.print(2); }\n" +
				"}\n",
			},
			"1");
	}

	public void test014() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count(new int[0], 1);\n" +
				"		Y.count(new int[0], 1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(int[] array, int ... values) { System.out.print(1); }\n" +
				"	public static void count(Object o, int ... values) { System.out.print(2); }\n" +
				"}\n",
			},
			"11"
		);
	}

	public void test015() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count(new int[0]);\n" + // for some reason this is not ambiguous
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(int[] array, int ... values) { System.out.print(1); }\n" +
				"	public static void count(int[] array, int[] ... values) { System.out.print(2); }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Y.count(new int[0]);\n" + 
			"	  ^^^^^\n" + 
			"The method count(int[], int[]) is ambiguous for the type Y\n" + 
			"----------\n"
		);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test015_tolerate() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		Map options = getCompilerOptions();
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
			this.runNegativeTest(
					new String[] {
							"X.java",
							"public class X {\n" +
							"	public static void main(String[] s) {\n" +
							"		Y.count(new int[0]);\n" + // for some reason this is not ambiguous
							"	}\n" +
							"}\n" +
							"class Y {\n" +
							"	public static void count(int[] array, int ... values) { System.out.print(1); }\n" +
							"	public static void count(int[] array, int[] ... values) { System.out.print(2); }\n" +
							"}\n",
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	Y.count(new int[0]);\n" + 
				"	  ^^^^^\n" + 
				"The method count(int[], int[]) is ambiguous for the type Y\n" + 
				"----------\n",
				null, true, options);
			} else {
				this.runConformTest(
					new String[] {
							"X.java",
							"public class X {\n" +
							"	public static void main(String[] s) {\n" +
							"		Y.count(new int[0]);\n" + // for some reason this is not ambiguous
							"	}\n" +
							"}\n" +
							"class Y {\n" +
							"	public static void count(int[] array, int ... values) { System.out.print(1); }\n" +
							"	public static void count(int[] array, int[] ... values) { System.out.print(2); }\n" +
							"}\n",
				},
				"1", 
				null, true, null, options, null);
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}

	public void test016() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest( // but this call is ambiguous
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count(new int[0]);\n" + // reference to count is ambiguous, both method count(int[],int...) in Y and method count(int[],int[][]...) in Y match
				"		Y.count(new int[0], null);\n" + // reference to count is ambiguous, both method count(int[],int...) in Y and method count(int[],int[]...) in Y match
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(int[] array, int ... values) { System.out.print(0); }\n" +
				"	public static void count(int[] array, int[][] ... values) { System.out.print(1); }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Y.count(new int[0]);\n" +
			"	  ^^^^^\n" +
			"The method count(int[], int[]) is ambiguous for the type Y\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Y.count(new int[0], null);\n" +
			"	  ^^^^^\n" +
			"The method count(int[], int[]) is ambiguous for the type Y\n" +
			"----------\n"
		);
	}

	public void test017() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.count(new int[0], 1);\n" + // reference to count is ambiguous, both method count(int[],int...) in Y and method count(int[],int,int...) in Y match
				"		Y.count(new int[0], 1, 1);\n" + // reference to count is ambiguous, both method count(int[],int...) in Y and method count(int[],int,int...) in Y match
				"		Y.count(new int[0], 1, 1, 1);\n" + // reference to count is ambiguous, both method count(int[],int...) in Y and method count(int[],int,int...) in Y match
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void count(int[] array, int ... values) {}\n" +
				"	public static void count(int[] array, int[] ... values) {}\n" +
				"	public static void count(int[] array, int i, int ... values) {}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Y.count(new int[0], 1);\n" +
			"	  ^^^^^\n" +
			"The method count(int[], int[]) is ambiguous for the type Y\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Y.count(new int[0], 1, 1);\n" +
			"	  ^^^^^\n" +
			"The method count(int[], int[]) is ambiguous for the type Y\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	Y.count(new int[0], 1, 1, 1);\n" +
			"	  ^^^^^\n" +
			"The method count(int[], int[]) is ambiguous for the type Y\n" +
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81590
	public void test018() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		String[][] x = {{\"X\"}, {\"Y\"}};\n" +
				"		List l = Arrays.asList(x);\n" +
				"		System.out.println(l.size() + \" \" + l.get(0).getClass().getName());\n" +
				"	}\n" +
				"}\n",
			},
			"2 [Ljava.lang.String;");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81590 - variation
	public void test019() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		String[][] x = {{\"X\"}, {\"Y\"}};\n" +
				"		System.out.println(asList(x[0], x[1]).get(1).getClass().getName());\n" +
				"	}\n" +
				"	static <U> List<U> asList(U u1, U... us) {\n" +
				"		List<U> result = new ArrayList<U>();\n" +
				"		result.add(u1);\n" +
				"		result.add(us[0]);\n" +
				"		return result;\n" +
				"	}\n" +
				"}\n",
			},
			"java.lang.String");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81590 - variation
	public void test020() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		String[][] x = {{\"X\"}, {\"Y\"}};\n" +
				"		System.out.println(asList(x[0], x).get(1).getClass().getName());\n" +
				"	}\n" +
				"	static <U> List<U> asList(U u1, U... us) {\n" +
				"		List<U> result = new ArrayList<U>();\n" +
				"		result.add(u1);\n" +
				"		result.add(us[0]);\n" +
				"		return result;\n" +
				"	}\n" +
				"}\n",
			},
			"[Ljava.lang.String;");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81911
	public void test021() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"public class X {\n" +
				"   public static void main(String[] args) {\n" +
				"      String[][] arr = new String[][] { args };\n" +
				"      ArrayList<String[]> al = new ArrayList<String[]>(Arrays.asList(arr));\n" +
				"   }\n" +
				"}\n",
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83032
	public void test022() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	String[] args;\n" +
				"	public X(String... args) {\n" +
				"		this.args = args;\n" +
				"	}\n" +
				"	public static X foo() {\n" +
				"		return new X(\"SU\", \"C\", \"CE\", \"SS\"){};\n" +
				"	}\n" +
				"	public String bar() {\n" +
				"		if (this.args != null) {\n" +
				"			StringBuffer buffer = new StringBuffer();\n" +
				"			for (String s : this.args) {\n" +
				"				buffer.append(s);\n" +
				"			}\n" +
				"			return String.valueOf(buffer);\n" +
				"		}\n" +
				"		return null;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(foo().bar());\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83536
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main (String[] args) {\n" +
				"        new X().test (new byte[5]);\n" +
				"		 System.out.print(\"SUCCESS\");\n" +
				"    }\n" +
				"    private void test (Object... params) {\n" +
				"    }\n" +
				"}",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87042
	public void test024() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static boolean foo(Object... args) {\n" +
				"		return args == null;\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(null, null));\n" +
				"	}\n" +
				"}",
			},
			"false");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87042
	public void test025() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static boolean foo(Object... args) {\n" +
				"		return args == null;\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(null));\n" +
				"	}\n" +
				"}",
			},
			"true");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87318
	public void test026() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X {\n" +
				"	static void foo(int[] intarray) {\n" +
				"		List<int[]> l = Arrays.asList(intarray);\n" +
				"		System.out.print(l.get(0).length);\n" +
				"	}\n" +
				"	static void foo(String[] strarray) {\n" +
				"		List l = Arrays.asList(strarray);\n" +
				"		System.out.print(l);\n" +
				"	}	\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(new int[]{0, 1});\n" +
				"		foo(new String[]{\"a\",\"b\"});\n" +
				"		System.out.println(\"done\");\n" +
				"	}\n" +
				"}\n",
			},
			"2[a, b]done");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87900
	public void test027() { // ensure AccVarargs does not collide
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	transient private X() {}\n" +
				"	void test() { X x = new X(); }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\r\n" +
			"	transient private X() {}\r\n" +
			"	                  ^^^\n" +
			"Illegal modifier for the constructor in type X; only public, protected & private are permitted\n" +
			"----------\n"
		);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	transient private X(Object... o) {}\n" +
				"	void test() { X x = new X(1, 2); }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	transient private X(Object... o) {}\n" +
			"	                  ^^^^^^^^^^^^^^\n" +
			"Illegal modifier for the constructor in type X; only public, protected & private are permitted\n" +
			"----------\n"
		);
	}
	// check no offending unnecessary varargs cast gets diagnosed
	public void test028() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.reflect.Method;\n" +
				"\n" +
				"public class X {\n" +
				"	void test(Method method){ \n" +
				"		try {\n" +
				"			method.invoke(this);\n" +
				"			method.invoke(this, new Class[0]);\n" +
				"			method.invoke(this, (Object[])new Class[0]);\n" +
				"		} catch (Exception e) {\n" +
				"		}		\n" +
				"	}\n" +
				"  Zork z;\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 7)\n" +
			"	method.invoke(this, new Class[0]);\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The argument of type Class[] should explicitly be cast to Object[] for the invocation of the varargs method invoke(Object, Object...) from type Method. It could alternatively be cast to Object for a varargs invocation\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 12)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91467
	public void test029() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" +
				" * Whatever you do, eclipse doesn\'t like it.\n" +
				" */\n" +
				"public class X {\n" +
				"\n" +
				"	/**\n" +
				"	 * Passing a String vararg to a method needing an Object array makes eclipse\n" +
				"	 * either ask for a cast or complain that it is unnecessary. You cannot do\n" +
				"	 * it right.\n" +
				"	 * \n" +
				"	 * @param s\n" +
				"	 */\n" +
				"	public static void q(String... s) {\n" +
				"		 // OK reports: Varargs argument String[] should be cast to Object[] when passed to the method 	printf(String, Object...) from type PrintStream\n" +
				"		System.out.printf(\"\", s);\n" +
				"		// WRONG reports: Unnecessary cast from String[] to Object[]\n" +
				"		System.out.printf(\"\", (Object[]) s); \n" +
				"	}\n" +
				"  Zork z;\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 15)\n" +
			"	System.out.printf(\"\", s);\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The argument of type String[] should explicitly be cast to Object[] for the invocation of the varargs method printf(String, Object...) from type PrintStream. It could alternatively be cast to Object for a varargs invocation\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 19)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99260
	public void test030() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.Serializable;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		audit(\"osvaldo\", \"localhost\", \"logged\", \"X\", new Integer(0));\n" +
				"		audit(\"osvaldo\", \"localhost\", \"logged\", \"X\", \"Y\");\n" +
				"		audit(\"osvaldo\", \"localhost\", \"logged\", new Float(0), new java.awt.Point(0, 0));\n" +
				"	}\n" +
				"	public static <A extends Serializable> void audit(String login,\n" +
				"			String address, String event, A... args) {\n" +
				"		for (A a : args) {\n" +
				"			System.out.println(a.getClass());\n" +
				"		}\n" +
				"	}\n" +
				"}",
			},
			"class java.lang.String\n" +
			"class java.lang.Integer\n" +
			"class java.lang.String\n" +
			"class java.lang.String\n" +
			"class java.lang.Float\n" +
			"class java.awt.Point");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102181
	public void test031() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		Test<String> t = new Tester();\n" +
				"		t.method(\"SUCCESS\");\n" +
				"	}\n" +
				"\n" +
				"	static abstract class Test<A> {\n" +
				"		abstract void method(A... args);\n" +
				"	}\n" +
				"\n" +
				"	static class Tester extends Test<String> {\n" +
				"\n" +
				"		@Override void method(String... args) {\n" +
				"			call(args);\n" +
				"		}\n" +
				"\n" +
				"		void call(String[] args) {\n" +
				"			for (String str : args)\n" +
				"				System.out.println(str);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102278
	public void test032() {
		this.runConformTest(
			new String[] {
				"Functor.java",
				"public class Functor<T> {\n" +
				"	public void func(T... args) {\n" +
				"		// do noting;\n" +
				"	}\n" +
				"	\n" +
				"	public static void main(String... args) {\n" +
				"		Functor<String> functor = new Functor<String>() {\n" +
				"			public void func(String... args) {\n" +
				"				System.out.println(args.length);\n" +
				"			}\n" +
				"		};\n" +
				"		functor.func(\"Hello!\");\n" +
				"	}\n" +
				"}\n",
			},
			"1");
	}
 	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102631
	public void test033() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void a(boolean b, Object... o) {System.out.print(1);}\n" +
				"	void a(Object... o) {System.out.print(2);}\n" +
				"	public static void main(String[] args) {\n" +
				"		X x = new X();\n" +
				"		x.a(true);\n" +
				"		x.a(true, \"foobar\");\n" +
				"		x.a(\"foo\", \"bar\");\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	x.a(true);\n" + 
			"	  ^\n" + 
			"The method a(boolean, Object[]) is ambiguous for the type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	x.a(true, \"foobar\");\n" + 
			"	  ^\n" + 
			"The method a(boolean, Object[]) is ambiguous for the type X\n" + 
			"----------\n");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void b(boolean b, Object... o) {}\n" +
				"	void b(Boolean... o) {}\n" +
				"	void c(boolean b, boolean b2, Object... o) {}\n" +
				"	void c(Boolean b, Object... o) {}\n" +
				"	public static void main(String[] args) {\n" +
				"		X x = new X();\n" +
				"		x.b(true);\n" +
				"		x.b(true, false);\n" +
				"		x.c(true, true, true);\n" +
				"		x.c(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\r\n" +
			"	x.b(true);\r\n" +
			"	  ^\n" +
			"The method b(boolean, Object[]) is ambiguous for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\r\n" +
			"	x.b(true, false);\r\n" +
			"	  ^\n" +
			"The method b(boolean, Object[]) is ambiguous for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\r\n" +
			"	x.c(true, true, true);\r\n" +
			"	  ^\n" +
			"The method c(boolean, boolean, Object[]) is ambiguous for the type X\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 11)\r\n" +
			"	x.c(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);\r\n" +
			"	  ^\n" +
			"The method c(boolean, boolean, Object[]) is ambiguous for the type X\n" +
			"----------\n"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test033_tolerate() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		Map options = getCompilerOptions();
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
				this.runNegativeTest(
					new String[] {
						"X.java",
						"public class X {\n" +
						"	void a(boolean b, Object... o) {System.out.print(1);}\n" +
						"	void a(Object... o) {System.out.print(2);}\n" +
						"	public static void main(String[] args) {\n" +
						"		X x = new X();\n" +
						"		x.a(true);\n" +
						"		x.a(true, \"foobar\");\n" +
						"		x.a(\"foo\", \"bar\");\n" +
						"	}\n" +
						"}\n",
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 6)\n" + 
					"	x.a(true);\n" + 
					"	  ^\n" + 
					"The method a(boolean, Object[]) is ambiguous for the type X\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 7)\n" + 
					"	x.a(true, \"foobar\");\n" + 
					"	  ^\n" + 
					"The method a(boolean, Object[]) is ambiguous for the type X\n" + 
					"----------\n",
					null, true, options);
			} else {
				this.runConformTest(
						new String[] {
							"X.java",
							"public class X {\n" +
							"	void a(boolean b, Object... o) {System.out.print(1);}\n" +
							"	void a(Object... o) {System.out.print(2);}\n" +
							"	public static void main(String[] args) {\n" +
							"		X x = new X();\n" +
							"		x.a(true);\n" +
							"		x.a(true, \"foobar\");\n" +
							"		x.a(\"foo\", \"bar\");\n" +
							"	}\n" +
							"}\n",
						},
						"112",
						null, true, null, options, null);
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=106106
	public void test034() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*; \n" +
				"\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    double[][] d = { { 1 } , { 2 } }; \n" +
				"    List<double[]> l = Arrays.asList(d); // <T> List<T> asList(T... a)\n" +
				"    System.out.println(\"List size: \" + l.size());\n" +
				"  }\n" +
				"}\n",
			},
			"List size: 2");
	}
	//	https://bugs.eclipse.org/bugs/show_bug.cgi?id=108095
	public void test035() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static <T> void foo(T ... values) {\n" +
				"      System.out.print(values.getClass());\n" +
				"  }\n" +
				"	public static void main(String args[]) {\n" +
				"	   X.<String>foo(\"monkey\", \"cat\");\n" +
				"      X.<String>foo(new String[] { \"monkey\", \"cat\" });\n" +
				"	}\n" +
				"}",
			},
			"class [Ljava.lang.String;class [Ljava.lang.String;");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110563
	public void test036() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X {\n" +
				"    public void testBreak() {\n" +
				"        Collection<Class> classes = new ArrayList<Class>();\n" +
				"        classes.containsAll(Arrays.asList(String.class, Integer.class, Long.class));\n" +
				"    }\n" +
				"}\n",
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110783
	public void test037() {
		this.runConformTest(
			new String[] {
				"V.java",
				"public class V {\n" +
				"    public static void main(String[] s) {\n" +
				"        V v = new V();\n" +
				"        v.foo(\"\", v, null, \"\");\n" +
				"        v.foo(\"\", v, null, \"\", 1);\n" +
				"        v.foo2(\"\");\n" +
				"        v.foo2(\"\", null);\n" +
				"        v.foo2(\"\", null, null);\n" +
				"        v.foo3(\"\", v, null, \"\", null);\n" +
				"    }\n" +
				"    void foo(String s, V v, Object... obs) {System.out.print(1);}\n" +
				"    void foo(String s, V v, String r, Object o, Object... obs) {System.out.print(2);}\n" +
				"    void foo2(Object... a) {System.out.print(1);}\n" +
				"    void foo2(String s, Object... a) {System.out.print(2);}\n" +
				"    void foo2(String s, Object o, Object... a) {System.out.print(3);}\n" +
				"    void foo3(String s, V v, String... obs) {System.out.print(1);}\n" +
				"    void foo3(String s, V v, String r, Object o, Object... obs) {System.out.print(2);}\n" +
				"}\n",
			},
			"222232");
		this.runNegativeTest(
			new String[] {
				"V.java",
				"public class V {\n" +
				"    public static void main(String[] s) {\n" +
				"        V v = new V();\n" +
				"        v.foo2(null, \"\");\n" +
				"        v.foo2(null, \"\", \"\");\n" +
				"        v.foo3(\"\", v, null, \"\");\n" +
				"    }\n" +
				"    void foo2(String s, Object... a) {System.out.print(2);}\n" +
				"    void foo2(String s, Object o, Object... a) {System.out.print(3);}\n" +
				"    void foo3(String s, V v, String... obs) {System.out.print(1);}\n" +
				"    void foo3(String s, V v, String r, Object o, Object... obs) {System.out.print(2);}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in V.java (at line 4)\r\n" +
			"	v.foo2(null, \"\");\r\n" +
			"	  ^^^^\n" +
			"The method foo2(String, Object[]) is ambiguous for the type V\n" +
			"----------\n" +
			"2. ERROR in V.java (at line 5)\r\n" +
			"	v.foo2(null, \"\", \"\");\r\n" +
			"	  ^^^^\n" +
			"The method foo2(String, Object[]) is ambiguous for the type V\n" +
			"----------\n" +
			"3. ERROR in V.java (at line 6)\r\n" +
			"	v.foo3(\"\", v, null, \"\");\r\n" +
			"	  ^^^^\n" +
			"The method foo3(String, V, String[]) is ambiguous for the type V\n" +
			"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801
	public void test038() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.Serializable;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"public class X {\n" +
				"    static void varargs(Serializable... items) {\n" +
				"        System.out.println(Arrays.deepToString(items) + \" (argument wrapped)\");\n" +
				"    }\n" +
				"    @SuppressWarnings({\"boxing\"})\n" +
				"    public static void main(String[] args) {\n" +
				"	     varargs(new Object[] {1, 2}); //warns \"Varargs argument Object[] \n" +
				"	     //should be cast to Serializable[] ..\", but proposed cast to\n" +
				"	     //Serializable[] fails at runtime (javac does not warn here)\n" +
				"	     varargs((Serializable[])new Object[] {1, 2}); //warns \"Varargs argument Object[] \n" +
				"	     //should be cast to Serializable[] ..\", but proposed cast to\n" +
				"	     //Serializable[] fails at runtime (javac does not warn here)\n" +
				"        Zork z;\n" +
				"    }\n" +
				"}\n",
			},
			// check no varargs warning
			"----------\n" +
			"1. WARNING in X.java (at line 13)\n" +
			"	varargs((Serializable[])new Object[] {1, 2}); //warns \"Varargs argument Object[] \n" +
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Unnecessary cast from Object[] to Serializable[]\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 16)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test039() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.Serializable;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"public class X {\n" +
				"    static void varargs(Serializable... items) {\n" +
				"        System.out.print(Arrays.deepToString(items) + \" (argument wrapped)\");\n" +
				"    }\n" +
				"    @SuppressWarnings({\"boxing\"})\n" +
				"    public static void main(String[] args) {\n" +
				"    	try {\n" +
				"	        varargs(new Object[] {1, 2}); //warns \"Varargs argument Object[] \n" +
				"	        	//should be cast to Serializable[] ..\", but proposed cast to\n" +
				"	            //Serializable[] fails at runtime (javac does not warn here)\n" +
				"	        varargs((Serializable[])new Object[] {1, 2}); //warns \"Varargs argument Object[] \n" +
				"	    	//should be cast to Serializable[] ..\", but proposed cast to\n" +
				"	        //Serializable[] fails at runtime (javac does not warn here)\n" +
				"    	} catch(ClassCastException e) {\n" +
				"    		System.out.println(\"SUCCESS\");\n" +
				"    	}\n" +
				"    }\n" +
				"}\n",
			},
			"[[1, 2]] (argument wrapped)SUCCESS");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test040() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.Serializable;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"public class X {\n" +
				"    static void array(Serializable... items) {\n" +
				"        System.out.print(Arrays.deepToString(items));\n" +
				"    }\n" +
				"    @SuppressWarnings({\"boxing\"})\n" +
				"    public static void main(String[] args) {\n" +
				"        array(new Serializable[] {3, 4});\n" +
				"        array(new Integer[] {5, 6}); //warns (as javac does)\n" +
				"        array(null); //warns (as javac does)\n" +
				"    }\n" +
				"}\n",
			},
			"[3, 4][5, 6]null");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test041() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.Serializable;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"public class X {\n" +
				"    static void array(Serializable... items) {\n" +
				"        System.out.print(Arrays.deepToString(items));\n" +
				"    }\n" +
				"    @SuppressWarnings({\"boxing\"})\n" +
				"    public static void main(String[] args) {\n" +
				"        array(new Serializable[] {3, 4});\n" +
				"        array(new Integer[] {5, 6}); //warns (as javac does)\n" +
				"        array(null); //warns (as javac does)\n" +
				"        Zork z;\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 11)\n" +
			"	array(new Integer[] {5, 6}); //warns (as javac does)\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The argument of type Integer[] should explicitly be cast to Serializable[] for the invocation of the varargs method array(Serializable...) from type X. It could alternatively be cast to Serializable for a varargs invocation\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 12)\n" +
			"	array(null); //warns (as javac does)\n" +
			"	^^^^^^^^^^^\n" +
			"The argument of type null should explicitly be cast to Serializable[] for the invocation of the varargs method array(Serializable...) from type X. It could alternatively be cast to Serializable for a varargs invocation\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 13)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test042() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.Serializable;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"public class X {\n" +
				"    static void varargs(Serializable... items) {\n" +
				"        System.out.print(Arrays.deepToString(items) + \" (argument wrapped)\");\n" +
				"    }\n" +
				"    @SuppressWarnings({\"boxing\"})\n" +
				"    public static void main(String[] args) {\n" +
				"        varargs((Serializable) new Object[] {1, 2});\n" +
				"        varargs((Serializable) new Serializable[] {3, 4}); //warns about\n" +
				"            //unnecessary cast, although cast is necessary (causes varargs call)\n" +
				"        varargs((Serializable) new Integer[] {5, 6});\n" +
				"        varargs((Serializable) null);\n" +
				"    }\n" +
				"}\n",
			},
			"[[1, 2]] (argument wrapped)[[3, 4]] (argument wrapped)[[5, 6]] (argument wrapped)[null] (argument wrapped)");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test043() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.Serializable;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"public class X {\n" +
				"    static void varargs(Serializable... items) {\n" +
				"        System.out.print(Arrays.deepToString(items) + \" (argument wrapped)\");\n" +
				"    }\n" +
				"    @SuppressWarnings({\"boxing\"})\n" +
				"    public static void main(String[] args) {\n" +
				"        varargs((Serializable) new Object[] {1, 2});\n" +
				"        varargs((Serializable) new Serializable[] {3, 4}); //warns about\n" +
				"            //unnecessary cast, although cast is necessary (causes varargs call)\n" +
				"        varargs((Serializable) new Integer[] {5, 6});\n" +
				"        varargs((Serializable) null);\n" +
				"        Zork z;\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 10)\n" +
			"	varargs((Serializable) new Object[] {1, 2});\n" +
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Unnecessary cast from Object[] to Serializable\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 15)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test044() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.Serializable;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"public class X {\n" +
				"    static void array(Serializable... items) {\n" +
				"        System.out.print(Arrays.deepToString(items));\n" +
				"    }\n" +
				"    @SuppressWarnings({\"boxing\"})\n" +
				"    public static void main(String[] args) {\n" +
				"        array((Serializable[]) new Serializable[] {3, 4}); //warns about unnecessary cast\n" +
				"        array((Serializable[]) new Integer[] {5, 6});\n" +
				"        array((Serializable[]) null);\n" +
				"        try {\n" +
				"	        array((Serializable[]) new Object[] {1, 2}); // CCE at run time\n" +
				"        } catch(ClassCastException e) {\n" +
				"        	System.out.println(\"SUCCESS\");\n" +
				"        }\n" +
				"    }\n" +
				"}\n",
			},
			"[3, 4][5, 6]nullSUCCESS");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105801 - variation
	public void test045() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.io.Serializable;\n" +
					"import java.util.Arrays;\n" +
					"\n" +
					"public class X {\n" +
					"    static void array(Serializable... items) {\n" +
					"        System.out.print(Arrays.deepToString(items));\n" +
					"    }\n" +
					"    @SuppressWarnings({\"boxing\"})\n" +
					"    public static void main(String[] args) {\n" +
					"        array((Serializable[]) new Serializable[] {3, 4}); //warns about unnecessary cast\n" +
					"        array((Serializable[]) new Integer[] {5, 6});\n" +
					"        array((Serializable[]) null);\n" +
					"	     array((Serializable[]) new Object[] {1, 2}); // CCE at run time\n" +
					"        Zork z;\n" +
					"    }\n" +
					"}\n",
				},
				"----------\n" +
				"1. WARNING in X.java (at line 10)\n" +
				"	array((Serializable[]) new Serializable[] {3, 4}); //warns about unnecessary cast\n" +
				"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Unnecessary cast from Serializable[] to Serializable[]\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 13)\n" +
				"	array((Serializable[]) new Object[] {1, 2}); // CCE at run time\n" +
				"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Unnecessary cast from Object[] to Serializable[]\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 14)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133918
	public void test046() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	void foo(Throwable... exceptions) {\n" +
					"	}\n" +
					"	void bar(Exception[] exceptions) {\n" +
					"		foo((Throwable[])exceptions);\n" +
					"	}\n" +
					"	Zork z;\n" +
					"}\n",
				},
				"----------\n" +
				"1. WARNING in X.java (at line 5)\n" +
				"	foo((Throwable[])exceptions);\n" +
				"	    ^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Unnecessary cast from Exception[] to Throwable[]\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140168
	public void test047() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo(Object id, Object value, String... groups) {}\n" +
				"	void foo(Y y, String... groups) {System.out.println(true);}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(new Y(), \"a\", \"b\");\n" +
				"	}\n" +
				"}\n" +
				"class Y {}",
			},
			"true");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo(Y y, Object value, String... groups) {}\n" +
				"	void foo(Object id, String... groups) {}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo(new Y(), \"a\", \"b\");\n" +
				"	}\n" +
				"}\n" +
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\r\n" +
			"	new X().foo(new Y(), \"a\", \"b\");\r\n" +
			"	        ^^^\n" +
			"The method foo(Y, Object, String[]) is ambiguous for the type X\n" +
			"----------\n"
			//reference to foo is ambiguous, both method foo(Y,java.lang.Object,java.lang.String...) in X and method foo(java.lang.Object,java.lang.String...) in X match
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=139931
	public void test048() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"        Y<String> [] foo() {\n" +
					"                return null;\n" +
					"        }\n" +
					"        void bar(Y... y) {\n" +
					"        }\n" +
					"        void fred() {\n" +
					"                bar(foo());\n" +
					"                bar((Y[])foo());\n" +
					"                Zork z;\n" +
					"        }\n" +
					"}\n" +
					"class Y<E> {\n" +
					"}\n",
				},
				"----------\n" +
				"1. WARNING in X.java (at line 5)\n" +
				"	void bar(Y... y) {\n" +
				"	         ^\n" +
				"Y is a raw type. References to generic type Y<E> should be parameterized\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 9)\n" +
				"	bar((Y[])foo());\n" +
				"	    ^^^^^^^^^^\n" +
				"Unnecessary cast from Y<String>[] to Y[]\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 10)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141704
	public void test049() {
		this.runConformTest(
				new String[] {
					"Y.java",
					"public class Y extends X {\n" +
					"	public static void main(String[] args) {\n" +
					"		Y y = new Y();\n" +
					"		y.a(null, \"\");\n" +
					"		y.a(null);\n" +
					"		y.a(y, \"\");\n" +
					"		y.a(y);\n" +
					"		y.a(y, \"\", y, y);\n" +
					"		y.a(y, y, y);\n" +
					"	}\n" +
					"	@Override public void a(Object anObject, String aString, Object... args) { super.a(anObject, aString, this, args); }\n" +
					"	@Override public void a(Object anObject, Object... args) { super.a(anObject, this, args); }\n" +
					"}\n" +
					"class X implements I {\n" +
					"	public void a(Object anObject, String aString, Object... args) { System.out.print(1); }\n" +
					"	public void a(Object anObject, Object... args) { System.out.print(2); }\n" +
					"}\n" +
					"interface I {\n" +
					"	void a(Object anObject, String aString, Object... args);\n" +
					"	void a(Object anObject, Object... args);\n" +
					"}\n",
				},
				"121212");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141800
	public void test050() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					" import java.util.Arrays;\n" +
					" public class X {\n" +
					"   public static void main( String args[] ) {\n" +
					"      Object test = new Object[] { \"Hello\", \"World\" };\n" +
					"      System.out.println(Arrays.asList(test));\n" +
					"      System.out.println(Arrays.asList((Object[])test)); // Warning here\n" +
					"	   Zork z;\n" +
					"   }\n" +
					"}",
				},
				// ensure no complaint about unnecessary cast
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141800 - variation
	public void test051() {
		this.runConformTest(
				new String[] {
					"X.java",
					" import java.util.Arrays;\n" +
					" public class X {\n" +
					"   public static void main( String args[] ) {\n" +
					"      Object test = new Object[] { \"Hello\", \"World\" };\n" +
					"      System.out.print(Arrays.asList(test).size());\n" +
					"      System.out.println(Arrays.asList((Object[])test).size()); // Warning here\n" +
					"   }\n" +
					"}",
				},
				"12");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159607
	public void test052() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"	void addChildren(Widget w) {\n" +
					"		if (w instanceof Composite) {\n" +
					"			Composite composite = (Composite) w;\n" +
					"			addAll((Widget[]) composite.getChildren());\n" +
					"			addAll(composite.getChildren());\n" +
					"		}\n" +
					"		Zork z;\n" +
					"	}\n" +
					"	void addAll(Widget... widgets) {\n" +
					"	}\n" +
					"}\n" +
					"\n" +
					"class Widget {}\n" +
					"class Control extends Widget {}\n" +
					"class Composite extends Control {\n" +
					"	Control[] getChildren() {\n" +
					"		return null;\n" +
					"	}\n" +
					"}", // =================,
				},
				"----------\n" +
				"1. WARNING in X.java (at line 5)\n" +
				"	addAll((Widget[]) composite.getChildren());\n" +
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Unnecessary cast from Control[] to Widget[]\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159607 - variation
	public void test053() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"	void addChildren(Widget w) {\n" +
					"		if (w instanceof Composite) {\n" +
					"			Composite composite = (Composite) w;\n" +
					"			addAll((Control[]) composite.getChildren());\n" +
					"			addAll(composite.getChildren());\n" +
					"		}\n" +
					"		Zork z;\n" +
					"	}\n" +
					"	void addAll(Control... widgets) {\n" +
					"	}\n" +
					"}\n" +
					"\n" +
					"class Widget {}\n" +
					"class Control extends Widget {}\n" +
					"class Composite extends Control {\n" +
					"	Control[] getChildren() {\n" +
					"		return null;\n" +
					"	}\n" +
					"}", // =================,
				},
				"----------\n" +
				"1. WARNING in X.java (at line 5)\n" +
				"	addAll((Control[]) composite.getChildren());\n" +
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Unnecessary cast from Control[] to Control[]\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	public void test054() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Zork z;\n" +
					"	public static void varargs(Object... args) {\n" +
					"		if (args == null) {\n" +
					"			System.out.println(\"args is null\");\n" +
					"			return;\n" +
					"		}\n" +
					"		if (args.length == 0) {\n" +
					"			System.out.println(\"args is of length 0\");\n" +
					"			return;\n" +
					"		}\n" +
					"\n" +
					"		System.out.println(args.length + \" \" + args[0]);\n" +
					"	}\n" +
					"\n" +
					"	public static void main(String[] args) {\n" +
					"		@SuppressWarnings(\"boxing\")\n" +
					"		Integer[] i = { 0, 1, 2, 3, 4 };\n" +
					"		varargs(i);\n" +
					"		varargs((Object[]) i);\n" +
					"		varargs((Object) i);\n" +
					"		varargs(i.clone());\n" +
					"	}\n" +
					"}\n", // =================
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 19)\n" +
				"	varargs(i);\n" +
				"	^^^^^^^^^^\n" +
				"The argument of type Integer[] should explicitly be cast to Object[] for the invocation of the varargs method varargs(Object...) from type X. It could alternatively be cast to Object for a varargs invocation\n" +
				"----------\n" +
				"3. WARNING in X.java (at line 22)\n" +
				"	varargs(i.clone());\n" +
				"	^^^^^^^^^^^^^^^^^^\n" +
				"The argument of type Integer[] should explicitly be cast to Object[] for the invocation of the varargs method varargs(Object...) from type X. It could alternatively be cast to Object for a varargs invocation\n" +
				"----------\n");
	}
	public void test055() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	private static int elementCount(Object... elements) {\n" +
					"     return elements == null ? 0 : elements.length;\n" +
					"   }\n" +
					"   public static void main(String... args) {\n" +
					"     System.out.print(\"null length array: \" + elementCount(null));\n" +
					"     System.out.print(\"/[null] length array: \" + elementCount((Object)null));\n" +
					"     System.out.print(\"/empty length array: \" + elementCount());\n" +
					"     System.out.println(\"/[a,b,c] length array: \" + elementCount(\"a\", \"b\", \"c\"));\n" +
					"   }\n" +
					"}", // =================
				},
				"null length array: 0/[null] length array: 1/empty length array: 0/[a,b,c] length array: 3");
	}
	public void test056() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Zork z;\n" +
					"	private static int elementCount(Object... elements) {\n" +
					"     return elements == null ? 0 : elements.length;\n" +
					"   }\n" +
					"   public static void main(String... args) {\n" +
					"     System.out.print(\"null length array: \" + elementCount(null));\n" +
					"     System.out.print(\"/[null] length array: \" + elementCount((Object)null));\n" +
					"     System.out.print(\"/empty length array: \" + elementCount());\n" +
					"     System.out.println(\"/[a,b,c] length array: \" + elementCount(\"a\", \"b\", \"c\"));\n" +
					"   }\n" +
					"}", // =================
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 7)\n" +
				"	System.out.print(\"null length array: \" + elementCount(null));\n" +
				"	                                         ^^^^^^^^^^^^^^^^^^\n" +
				"The argument of type null should explicitly be cast to Object[] for the invocation of the varargs method elementCount(Object...) from type X. It could alternatively be cast to Object for a varargs invocation\n" +
				"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=163889
	public void test057() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.RetentionPolicy;\n" +
					"\n" +
					"public class X {\n" +
					"\n" +
					"  void a(Enum<?>...enums) {}\n" +
					"\n" +
					"  void b () {\n" +
					"    RetentionPolicy[] t = null;\n" +
					"    a(t);\n" +
					"    a((Enum<?>[])t);\n" +
					"    Zork z;\n" +
					"  }\n" +
					"}\n", // =================
				},
				"----------\n" +
				"1. WARNING in X.java (at line 10)\n" +
				"	a((Enum<?>[])t);\n" +
				"	  ^^^^^^^^^^^^\n" +
				"Unnecessary cast from RetentionPolicy[] to Enum<?>[]\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 11)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162171
	public void test058() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    public void testPassingSubclassArrayAsVararg() {\n" +
					"        // The argument of type VarargsTest.Subclass[] should explicitly be\n" +
					"        // cast to VarargsTest.Parent[] for the invocation of the varargs\n" +
					"        // method processVararg(VarargsTest.Parent...) from type VarargsTest.\n" +
					"        // It could alternatively be cast to VarargsTest.Parent for a varargs\n" +
					"        // invocation\n" +
					"        processVararg(new Subclass[] {});\n" +
					"    }\n" +
					"\n" +
					"    public void testPassingSubclassArrayAsVarargWithCast() {\n" +
					"        // Unnecessary cast from VarargsTest.Subclass[] to\n" +
					"        // VarargsTest.Parent[]\n" +
					"        processVararg((Parent[]) new Subclass[] {});\n" +
					"        processVararg(new Subclass[] {});\n" +
					"        Zork z;\n" +
					"    }\n" +
					"\n" +
					"    private void processVararg(Parent... objs) {\n" +
					"    }\n" +
					"\n" +
					"    class Parent {\n" +
					"    }\n" +
					"\n" +
					"    class Subclass extends Parent {\n" +
					"    }\n" +
					"}\n", // =================
				},
				"----------\n" +
				"1. WARNING in X.java (at line 14)\n" +
				"	processVararg((Parent[]) new Subclass[] {});\n" +
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Unnecessary cast from X.Subclass[] to X.Parent[]\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 16)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=170765
	public void test059() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public void foo() {\n" +
					"		Integer[] array = null;\n" +
					"		varargs(array);\n" +
					"	}\n" +
					"\n" +
					"	public void varargs(Number... o) {\n" +
					"	}\n" +
					"    Zork z;\n" +
					"}\n", // =================
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=186181
	public void test060() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"   public static String varargMethod( Object... objects ) {\r\n" +
					"      String s = \"\";\n" +
					"      for (Object object : objects)\n" +
					"         s += \",\" + object.toString();\n" +
					"      return s;\n" +
					"   }\n" +
					"}",
				},
				"",
				null,
				true,
				null,
				options,
				null);

		// make sure that this file contains the varargs attribute
		IClassFileReader reader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		IMethodInfo[] methodInfos = reader.getMethodInfos();
		assertEquals("Wrong size", 2, methodInfos.length);
		IMethodInfo methodInfo = null;
		if (CharOperation.equals(methodInfos[0].getName(), "varargMethod".toCharArray())) {
			methodInfo = methodInfos[0];
		} else if (CharOperation.equals(methodInfos[1].getName(), "varargMethod".toCharArray())) {
			methodInfo = methodInfos[1];
		}
		assertTrue("ACC_VARARGS is not set", (methodInfo.getAccessFlags() & ClassFileConstants.AccVarargs) == 0);
		assertNotNull("Method varargMethodshould be there", methodInfo);
		assertEquals("2", 2, methodInfo.getAttributeCount());
		IClassFileAttribute[] attributes = methodInfo.getAttributes();
		assertTrue("varargs attribute not found", CharOperation.equals(attributes[0].getAttributeName(), "Varargs".toCharArray())
				|| CharOperation.equals(attributes[1].getAttributeName(), "Varargs".toCharArray()));

		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
		this.runConformTest(
				new String[] {
					"UseVararg.java",
					"public class UseVararg {\r\n" +
					"   public static void main( String[] args ) {\n" +
					"      String arg = \"SUCCESS\";\n" +
					"      String results = X.varargMethod(arg);\n" +
					"      System.out.println( results );\n" +
					"   }\r\n" +
					"}",
				},
				",SUCCESS",
				null,
				false,
				null,
				options,
				null);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223427
	public void test061() {
		String expectedOutput = 
				"----------\n" +
				"1. WARNING in X.java (at line 5)\n" +
				"	Collections.addAll(constantClassSet, String.class, Object.class);\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type safety: A generic array of Class<? extends Object> is created for a varargs parameter\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n";
		if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
			expectedOutput = 
					"----------\n" +
					"1. ERROR in X.java (at line 6)\n" +
					"	Zork z;\n" +
					"	^^^^\n" +
					"Zork cannot be resolved to a type\n" +
					"----------\n";
		}
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		HashSet<Class<?>> constantClassSet = new HashSet<Class<?>>();\n" +
					"		Collections.addAll(constantClassSet, String.class, Object.class);\n" +
					"		Zork z;\n" +
					"	}\n" +
					"}\n", // =================
				},
				expectedOutput);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328247
	public void test062() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\r\n" + 
				"	private static final String CONST = \"\";\r\n" + 
				"\r\n" + 
				"	public static class A {\r\n" + 
				"		A(Integer i, String... tab) {}\r\n" + 
				"	}\r\n" + 
				"	\r\n" + 
				"	Object foo(final Float f) {\r\n" + 
				"		return new A(new Integer(0), CONST) {\r\n" + 
				"			public String toString() {\r\n" + 
				"				return f.toString();\r\n" + 
				"			}\r\n" + 
				"		};\r\n" + 
				"	}\r\n" + 
				"}",
			},
			"");
		String expectedOutput =
			"  // Method descriptor #10 (LX;Ljava/lang/Integer;[Ljava/lang/String;Ljava/lang/Float;)V\n" + 
			"  // Stack: 3, Locals: 5\n" + 
			"  X$1(X arg0, java.lang.Integer $anonymous0, java.lang.String... $anonymous1, java.lang.Float arg3);\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X$1.class", "X$1", expectedOutput);
	}
	//safe varargs support
	public void test063() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
			new String[] {
				"java/lang/SafeVarargs.java",
				"package java.lang;\n" +
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
				"import static java.lang.annotation.ElementType.CONSTRUCTOR;\n" + 
				"import static java.lang.annotation.ElementType.METHOD;\n" + 
				"\n" + 
				"@Retention(value=RUNTIME)\n" + 
				"@Target(value={CONSTRUCTOR,METHOD})\n" + 
				"public @interface SafeVarargs {}",
				"Y.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"public class Y {\r\n" +
				"	@SafeVarargs\n" +
				"	public static <T> List<T> asList(T... a) {\n" + 
				"		return null;\n" + 
				"	}\n" +
				"}",
			},
			"");
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"public class X {\r\n" +
				"	public void bar() {\n" +
				"		List<? extends Class<?>> classes = Y.asList(String.class, Boolean.class);\n" +
				"	}\n" +
				"}",
			},
			"",
			null,
			false,
			null,
			options,
			null);
	}
	public void test064() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
			new String[] {
				"java/lang/SafeVarargs.java",
				"package java.lang;\n" +
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
				"import static java.lang.annotation.ElementType.CONSTRUCTOR;\n" + 
				"import static java.lang.annotation.ElementType.METHOD;\n" + 
				"\n" + 
				"@Retention(value=RUNTIME)\n" + 
				"@Target(value={CONSTRUCTOR,METHOD})\n" + 
				"public @interface SafeVarargs {}",
			},
			"");
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"public class X {\r\n" +
				"	@SafeVarargs\n" +
				"	public static <T> List<T> asList(T... a) {\n" + 
				"		return null;\n" + 
				"	}\n" +
				"	public void bar() {\n" +
				"		List<? extends Class<?>> classes = X.asList(String.class, Boolean.class);\n" +
				"	}\n" +
				"}",
			},
			"",
			null,
			false,
			null,
			options,
			null);
	}
	public void test065() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
			new String[] {
				"java/lang/SafeVarargs.java",
				"package java.lang;\n" +
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
				"import static java.lang.annotation.ElementType.CONSTRUCTOR;\n" + 
				"import static java.lang.annotation.ElementType.METHOD;\n" + 
				"\n" + 
				"@Retention(value=RUNTIME)\n" + 
				"@Target(value={CONSTRUCTOR,METHOD})\n" + 
				"public @interface SafeVarargs {}",
			},
			"");
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"public class X {\r\n" +
				"	@SafeVarargs\n" +
				"	public static <T> List<T> asList(T... a) {\n" + 
				"		return null;\n" + 
				"	}\n" +
				"	public void bar() {\n" +
				"		List<List<String>> classes = X.asList(X.asList(\"Hello\", \"World\"));\n" +
				"	}\n" +
				"}",
			},
			"",
			null,
			false,
			null,
			options,
			null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337093
	public void test066() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.Collection;\n" +
					"import java.util.Iterator;\n" +
					"public class X {\n" +
					"    public static class IteratorChain<T> implements Iterator<T> {\n" +
					"       public IteratorChain(Collection<? extends T> a, Collection<? extends T> b, Collection<? extends T> ... collections) {\n" +
					"        }\n" +
					"        public boolean hasNext() {\n" +
					"            return false;\n" +
					"        }\n" +
					"        public T next() {\n" +
					"            return null;\n" +
					"        }\n" +
					"        public void remove() {\n" +
					"            throw new UnsupportedOperationException();\n" +
					"        }\n" +
					"    }\n" +
					"    public static void main(String[] args) {\n" +
					"        new IteratorChain<Number>(null, null);\n" +
					"    }\n" +
					"}\n", // =================
				},
				this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"----------\n" + 
				"1. WARNING in X.java (at line 18)\n" + 
				"	new IteratorChain<Number>(null, null);\n" + 
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Type safety: A generic array of Collection<? extends Number> is created for a varargs parameter\n" + 
				"----------\n":
				"----------\n" + 
				"1. WARNING in X.java (at line 5)\n" + 
				"	public IteratorChain(Collection<? extends T> a, Collection<? extends T> b, Collection<? extends T> ... collections) {\n" + 
				"	                                                                                                       ^^^^^^^^^^^\n" + 
				"Type safety: Potential heap pollution via varargs parameter collections\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 18)\n" + 
				"	new IteratorChain<Number>(null, null);\n" + 
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Type safety: A generic array of Collection<? extends Number> is created for a varargs parameter\n" + 
				"----------\n",
				null, 
				true,
				options);		
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337799
	public void test067() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
			new String[] {
				"java/lang/SafeVarargs.java",
				"package java.lang;\n" +
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
				"import static java.lang.annotation.ElementType.CONSTRUCTOR;\n" + 
				"import static java.lang.annotation.ElementType.METHOD;\n" + 
				"\n" + 
				"@Retention(value=RUNTIME)\n" + 
				"@Target(value={CONSTRUCTOR,METHOD})\n" + 
				"public @interface SafeVarargs {}",
			},
			"");
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"	@SafeVarargs\n" +
				"	public static <T> List<T> asList() {  // Error, not varargs\n" +
				"		return null;\n" +
				"	}\n" +
				"	@SafeVarargs\n" +
				"	public <T> List<T> asList2(T ... a) {    // error not static or final\n" +
				"		return null;\n" +
				"	}\n" +
				"	@SafeVarargs\n" +
				"	public static <T> List<T> asList3(T ... a) {  // OK, varargs & static\n" +
				"		return null;\n" +
				"	}\n" +
				"	@SafeVarargs\n" +
				"	public final <T> List<T> asList4(T ... a) {  // OK varargs & final\n" +
				"		return null;\n" +
				"	}\n" +
				"	@SafeVarargs\n" +
				"	public final static <T> List<T> asList5(T ... a) {  // OK, varargs & static & final\n" +
				"		return null;\n" +
				"	}\n" +
				"	@SafeVarargs\n" +
				"	public int b;\n" +
				"}\n" +
				"interface I {\n" +
				"	@SafeVarargs\n" +
				"	public  <T> List<T> asList(T ... t);\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	public static <T> List<T> asList() {  // Error, not varargs\n" + 
			"	                          ^^^^^^^^\n" + 
			"@SafeVarargs annotation cannot be applied to fixed arity method asList\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	public <T> List<T> asList2(T ... a) {    // error not static or final\n" + 
			"	                   ^^^^^^^^^^^^^^^^\n" + 
			"@SafeVarargs annotation cannot be applied to non-final instance method asList2\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 23)\n" + 
			"	@SafeVarargs\n" + 
			"	^^^^^^^^^^^^\n" + 
			"The annotation @SafeVarargs is disallowed for this location\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 28)\n" + 
			"	public  <T> List<T> asList(T ... t);\n" + 
			"	                    ^^^^^^^^^^^^^^^\n" + 
			"@SafeVarargs annotation cannot be applied to non-final instance method asList\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337799
	public void test067b() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
			new String[] {
				"java/lang/SafeVarargs.java",
				"package java.lang;\n" +
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
				"import static java.lang.annotation.ElementType.CONSTRUCTOR;\n" + 
				"import static java.lang.annotation.ElementType.METHOD;\n" + 
				"\n" + 
				"@Retention(value=RUNTIME)\n" + 
				"@Target(value={CONSTRUCTOR,METHOD})\n" + 
				"public @interface SafeVarargs {}",
			},
			"");
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"	@SafeVarargs\n" +
				"	public X() {  // Error, not varargs\n" +
				"	}\n" +
				"	@SafeVarargs\n" +
				"	public <T> X(T ... a) {\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	public X() {  // Error, not varargs\n" + 
			"	       ^^^\n" + 
			"@SafeVarargs annotation cannot be applied to fixed arity method X\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795 (make sure there is no warning if vararg parameter is reifiable)
	public void test068() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
			new String[] {
				"java/lang/SafeVarargs.java",
				"package java.lang;\n" +
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
				"import static java.lang.annotation.ElementType.CONSTRUCTOR;\n" + 
				"import static java.lang.annotation.ElementType.METHOD;\n" + 
				"\n" + 
				"@Retention(value=RUNTIME)\n" + 
				"@Target(value={CONSTRUCTOR,METHOD})\n" + 
				"public @interface SafeVarargs {}",
			},
			"");
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"	public <T> X(String ... a) {\n" +
				"	}\n" +
				"	public <T> X(int i, String ... a) {\n" +
				"	}\n" +
				"   public <T> List<T> asList(String ... a) {\n" +
				"       return null;\n" +
				"   }\n" +
				"   public <T> List<T> asList(Zork t, String ... a) {\n" +
				"       return null;\n" +
				"   }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	public <T> List<T> asList(Zork t, String ... a) {\n" + 
			"	                          ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795 (make sure there is a warning if vararg parameter is not reifiable)
	public void test068b() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
			new String[] {
				"java/lang/SafeVarargs.java",
				"package java.lang;\n" +
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
				"import static java.lang.annotation.ElementType.CONSTRUCTOR;\n" + 
				"import static java.lang.annotation.ElementType.METHOD;\n" + 
				"\n" + 
				"@Retention(value=RUNTIME)\n" + 
				"@Target(value={CONSTRUCTOR,METHOD})\n" + 
				"public @interface SafeVarargs {}",
			},
			"");
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"	public <T> X(T ... a) {\n" +
				"	}\n" +
				"	public <T> X(int i, T ... a) {\n" +
				"	}\n" +
				"   public <T> List<T> asList(T ... a) {\n" +
				"       return null;\n" +
				"   }\n" +
				"   public <T> List<T> asList(T t, T ... a) {\n" +
				"       return null;\n" +
				"   }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public <T> X(T ... a) {\n" + 
			"	                   ^\n" + 
			"Type safety: Potential heap pollution via varargs parameter a\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	public <T> X(int i, T ... a) {\n" + 
			"	                          ^\n" + 
			"Type safety: Potential heap pollution via varargs parameter a\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 7)\n" + 
			"	public <T> List<T> asList(T ... a) {\n" + 
			"	                                ^\n" + 
			"Type safety: Potential heap pollution via varargs parameter a\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 10)\n" + 
			"	public <T> List<T> asList(T t, T ... a) {\n" + 
			"	                                     ^\n" + 
			"Type safety: Potential heap pollution via varargs parameter a\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795
	public void test068c() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
			new String[] {
				"java/lang/SafeVarargs.java",
				"package java.lang;\n" +
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
				"import static java.lang.annotation.ElementType.CONSTRUCTOR;\n" + 
				"import static java.lang.annotation.ElementType.METHOD;\n" + 
				"\n" + 
				"@Retention(value=RUNTIME)\n" + 
				"@Target(value={CONSTRUCTOR,METHOD})\n" + 
				"public @interface SafeVarargs {}",
			},
			"");
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"   @SafeVarargs\n" +
				"	public <T> X(T ... a) {\n" +
				"	}\n" +
				"   @SafeVarargs\n" +
				"	public <T> X(int i, T ... a) {\n" +
				"	}\n" +
				"   @SafeVarargs\n" +
				"   public <T> List<T> asList(T ... a) {\n" +
				"       return null;\n" +
				"   }\n" +
				"   @SafeVarargs\n" +
				"   public <T> List<T> asList(T t, T ... a) {\n" +
				"       return null;\n" +
				"   }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	public <T> List<T> asList(T ... a) {\n" + 
			"	                   ^^^^^^^^^^^^^^^\n" + 
			"@SafeVarargs annotation cannot be applied to non-final instance method asList\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\n" + 
			"	public <T> List<T> asList(T t, T ... a) {\n" + 
			"	                   ^^^^^^^^^^^^^^^^^^^^\n" + 
			"@SafeVarargs annotation cannot be applied to non-final instance method asList\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795
	public void test068d() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
			new String[] {
				"java/lang/SafeVarargs.java",
				"package java.lang;\n" +
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
				"import static java.lang.annotation.ElementType.CONSTRUCTOR;\n" + 
				"import static java.lang.annotation.ElementType.METHOD;\n" + 
				"\n" + 
				"@Retention(value=RUNTIME)\n" + 
				"@Target(value={CONSTRUCTOR,METHOD})\n" + 
				"public @interface SafeVarargs {}",
			},
			"");
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"   @SafeVarargs\n" +
				"   public static <T> List<T> asList(T ... a) {\n" +
				"       return null;\n" +
				"   }\n" +
				"   public static <T> List<T> asList2(T ... a) {\n" +
				"       return null;\n" +
				"   }\n" +
				"	List<? extends Class<?>> classes; \n" +
				"   {\n" +
				"     classes = X.asList(String.class, Boolean.class);\n" +
				"	  classes = X.asList2(String.class, Boolean.class);\n" +
				"   }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	public static <T> List<T> asList2(T ... a) {\n" + 
			"	                                        ^\n" + 
			"Type safety: Potential heap pollution via varargs parameter a\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 13)\n" + 
			"	classes = X.asList2(String.class, Boolean.class);\n" + 
			"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: A generic array of Class<? extends Object&Serializable&Comparable<?>> is created for a varargs parameter\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795 (test effect of SuppressWarnings (should suppress at declaration site, but not at call site)
	public void test068e() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
			new String[] {
				"java/lang/SafeVarargs.java",
				"package java.lang;\n" +
				"import java.lang.annotation.Retention;\n" + 
				"import java.lang.annotation.Target;\n" + 
				"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
				"import static java.lang.annotation.ElementType.CONSTRUCTOR;\n" + 
				"import static java.lang.annotation.ElementType.METHOD;\n" + 
				"\n" + 
				"@Retention(value=RUNTIME)\n" + 
				"@Target(value={CONSTRUCTOR,METHOD})\n" + 
				"public @interface SafeVarargs {}",
			},
			"");
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.ERROR);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"   @SafeVarargs\n" +
				"   public static <T> List<T> asList(T ... a) {\n" +
				"       return null;\n" +
				"   }\n" +
				"   @SuppressWarnings(\"unchecked\")\n" +
				"   public static <T> List<T> asList2(T ... a) {\n" +
				"       return null;\n" +
				"   }\n" +
				"	List<? extends Class<?>> classes; \n" +
				"   {\n" +
				"     classes = X.asList(String.class, Boolean.class);\n" +
				"	  classes = X.asList2(String.class, Boolean.class);\n" +
				"   }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 14)\n" + 
			"	classes = X.asList2(String.class, Boolean.class);\n" + 
			"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: A generic array of Class<? extends Object&Serializable&Comparable<?>> is created for a varargs parameter\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346042
	public void test069() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		this.runNegativeTest(
			new String[] {
				"p1/B.java",
				"package p1;\n" +
				"class A {\n" +
				"}\n" +
				"public class B extends A {\n" +
				" public void foo(A... args) {\n" +
				" }\n" +
				"}\n",
				"p2/C.java",
				"package p2;\n" +
				"import p1.B;\n" +
				"public class C {\n" +
				"\n" +
				" public static final void main(String[] args) {\n" +
				"   (new B()).foo(new B(), new B());\n" +
				" }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in p2\\C.java (at line 6)\n" + 
			"	(new B()).foo(new B(), new B());\n" + 
			"	          ^^^\n" + 
			"The method foo(A...) of type B is not applicable as the formal varargs element type A is not accessible here\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346038
	public void test070() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"        public static void foo(int ...i) {}\n" +
				"        public static void foo(double...d) {}\n" +
				"        public static void main(String[] args) {\n" +
				"            foo(1, 2, 3);\n" +
				"            System.out.println (\"Done\");\n" +
				"        }\n" +
				"}\n"
			},
			"Done");
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test070_tolerate() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		Map options = getCompilerOptions();
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel < ClassFileConstants.JDK1_7) {
				this.runNegativeTest(
					new String[] {
						"X.java",
						"public class X {\n" +
						"        public static void foo(int ...i) {}\n" +
						"        public static void foo(double...d) {}\n" +
						"        public static void main(String[] args) {\n" +
						"            foo(1, 2, 3);\n" +
						"            System.out.println (\"Done\");\n" +
						"        }\n" +
						"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 5)\n" + 
					"	foo(1, 2, 3);\n" + 
					"	^^^\n" + 
					"The method foo(int[]) is ambiguous for the type X\n" + 
					"----------\n", 
					null, true, options);
			} else {
				this.runConformTest(
						new String[] {
							"X.java",
							"public class X {\n" +
							"        public static void foo(int ...i) {}\n" +
							"        public static void foo(double...d) {}\n" +
							"        public static void main(String[] args) {\n" +
							"            foo(1, 2, 3);\n" +
							"            System.out.println (\"Done\");\n" +
							"        }\n" +
							"}\n"
						},
						"Done", 
						null, true, null, options, null);
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
		
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test070_tolerate2() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		Map options = getCompilerOptions();
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
				this.runNegativeTest(
					new String[] {
						"X.java",
						"import java.util.Arrays;\n" +
						"public class X {\n" +
						"        public static void test(int... a) {\n" +
						"			System.out.println(Arrays.toString(a));\n}\n" +
						"        public static <T> void test(Object... a) {\n" +
						"			System.out.println(Arrays.toString(a));\n}\n" +
						"        public static void main(String[] args) {\n" +
						"            test(1);\n" +
						"        }\n" +
						"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 10)\n" + 
					"	test(1);\n" + 
					"	^^^^\n" + 
					"The method test(int[]) is ambiguous for the type X\n" + 
					"----------\n", 
					null, true, options);
			} else {
				this.runConformTest(
						new String[] {
								"X.java",
								"import java.util.Arrays;\n" +
								"public class X {\n" +
								"        public static void test(int... a) {\n" +
								"			System.out.println(Arrays.toString(a));\n}\n" +
								"        public static <T> void test(Object... a) {\n" +
								"			System.out.println(Arrays.toString(a));\n}\n" +
								"        public static void main(String[] args) {\n" +
								"            test(1);\n" +
								"        }\n" +
								"}\n"
						},
						"[1]", 
						null, true, null, options, null);
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
		
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346038
	public void test070a() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"        public static <T> void foo(int ...i) {}\n" +
				"        public static <T> void foo(double...d) {}\n" +
				"        public static void main(String[] args) {\n" +
				"            foo(1, 2, 3);\n" +
				"            System.out.println (\"Done\");\n" +
				"        }\n" +
				"}\n"
			},
			"Done");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test070a_tolerate() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		Map options = getCompilerOptions();
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel < ClassFileConstants.JDK1_7) {
				this.runNegativeTest(
					new String[] {
						"X.java",
						"public class X {\n" +
						"        public static <T> void foo(int ...i) {}\n" +
						"        public static <T> void foo(double...d) {}\n" +
						"        public static void main(String[] args) {\n" +
						"            foo(1, 2, 3);\n" +
						"            System.out.println (\"Done\");\n" +
						"        }\n" +
						"}\n"
					},
					"----------\n" + 
					"1. ERROR in X.java (at line 5)\n" + 
					"	foo(1, 2, 3);\n" + 
					"	^^^\n" + 
					"The method foo(int[]) is ambiguous for the type X\n" + 
					"----------\n", 
					null, true, options);
			} else {
				this.runConformTest(
						new String[] {
							"X.java",
							"public class X {\n" +
							"        public static <T> void foo(int ...i) {}\n" +
							"        public static <T> void foo(double...d) {}\n" +
							"        public static void main(String[] args) {\n" +
							"            foo(1, 2, 3);\n" +
							"            System.out.println (\"Done\");\n" +
							"        }\n" +
							"}\n"
						},
						"Done", 
						null, true, null, options, null);
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346038
	public void test070b() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"        public static void foo(int ...i) {}\n" +
				"        public static void foo(double d1, double...d) {}\n" +
				"        public static void main(String[] args) {\n" +
				"            foo(1, 2, 3);     // foo NOT flagged ambiguous\n" +
				"        }\n" +
				"}\n" 
			},
			"");
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test070b_tolerate() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		String[] src = new String[] {
				"X.java",
				"public class X {\n" +
				"        public static void foo(int ...i) {}\n" +
				"        public static void foo(double d1, double...d) {}\n" +
				"        public static void main(String[] args) {\n" +
				"            foo(1, 2, 3);     // foo NOT flagged ambiguous\n" +
				"        }\n" +
				"}\n" 
			};
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
				this.runConformTest(
					src,
					"");
			} else {
				this.runNegativeTest(
						src,
						"----------\n" + 
						"1. ERROR in X.java (at line 5)\n" + 
						"	foo(1, 2, 3);     // foo NOT flagged ambiguous\n" + 
						"	^^^\n" + 
						"The method foo(int[]) is ambiguous for the type X\n" + 
						"----------\n");
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346038
	public void test070c() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String[] s) {\n" +
				"        count(1);\n" +
				"        count(1, 1);\n" +
				"        count(1, 1, 1);\n" +
				"    }\n" +
				"    public static void count(int ... values) {}\n" +
				"    public static void count(int i, int ... values) {}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	count(1);\n" + 
			"	^^^^^\n" + 
			"The method count(int[]) is ambiguous for the type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	count(1, 1);\n" + 
			"	^^^^^\n" + 
			"The method count(int[]) is ambiguous for the type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	count(1, 1, 1);\n" + 
			"	^^^^^\n" + 
			"The method count(int[]) is ambiguous for the type X\n" + 
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346038
	public void test070d() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void b(boolean b, Object... o) {}\n" +
				"	void b(Boolean... o) {}\n" +
				"	void c(boolean b, boolean b2, Object... o) {}\n" +
				"	void c(Boolean b, Object... o) {}\n" +
				"	public static void main(String[] args) {\n" +
				"		X x = new X();\n" +
				"		x.b(true);\n" +
				"		x.b(true, false);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\r\n" +
			"	x.b(true);\r\n" +
			"	  ^\n" +
			"The method b(boolean, Object[]) is ambiguous for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\r\n" +
			"	x.b(true, false);\r\n" +
			"	  ^\n" +
			"The method b(boolean, Object[]) is ambiguous for the type X\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346039
	public void test071() { // check behaviour of Scope.mostSpecificMethodBinding()
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements IClass{\n" +
				"    X(IClass c, X t, IType... args) {\n" +
				"	     System.out.println (\"1\");\n" +
				"    }\n" +
				"    X(IClass c, IType... args) {\n" +
				"	    System.out.println (\"2\");\n" +
				"    }\n" +
				"    public static void main(String args[]) {\n" +
				"        IClass c = null;\n" +
				"        X t = null;\n" +
				"        X t2 = new X(c, t);     // incorrectly flagged ambiguous\n" +
				"    }\n" +
				"}\n" +
				"interface IType{}\n" +
				"interface IClass extends IType{}\n"
			},
			"1");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383780
	public void test071_tolerate() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		String[] src = 
			new String[] {
				"X.java",
				"public class X implements IClass{\n" +
				"    X(IClass c, X t, IType... args) {\n" +
				"	     System.out.println (\"1\");\n" +
				"    }\n" +
				"    X(IClass c, IType... args) {\n" +
				"	    System.out.println (\"2\");\n" +
				"    }\n" +
				"    public static void main(String args[]) {\n" +
				"        IClass c = null;\n" +
				"        X t = null;\n" +
				"        X t2 = new X(c, t);     // incorrectly flagged ambiguous\n" +
				"    }\n" +
				"}\n" +
				"interface IType{}\n" +
				"interface IClass extends IType{}\n"
			};
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
				this.runConformTest(
					src,
					"1");
			} else {
				this.runNegativeTest(
						src,
						"----------\n" + 
						"1. ERROR in X.java (at line 11)\n" + 
						"	X t2 = new X(c, t);     // incorrectly flagged ambiguous\n" + 
						"	       ^^^^^^^^^^^\n" + 
						"The constructor X(IClass, X, IType[]) is ambiguous\n" + 
						"----------\n");
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=364672
	public void test072() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	private class Z {}\n" + 
				"	public void foo() {\n" + 
				"			Z[] zs = null;\n" + 
				"			Y.bar(zs, new Z());\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {}\n" + 
				"}",
				"Y.java",
				"public class Y {\n" + 
				"	public native static <T> void bar(T[] t, T t1, T... t2);\n" + 
				"}"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=364672
	public void test073() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static final String CONSTANT = \"\";\n" + 
				"	private static class A {\n" + 
				"		A(String s, String s2, String s3, A... a) {}\n" + 
				"	}\n" + 
				"	private static class B extends A {\n" + 
				"		B(String s, String s2) {\n" + 
				"			super(s, s2, CONSTANT);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	private static void foo(Object o, A ... a) {\n" + 
				"	}\n" + 
				"	private static B bar() {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		Object o = null;\n" + 
				"		foo(o, bar(), bar());\n" + 
				"	}\n" + 
				"}"
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=364672
	public void test074() throws Exception {
		this.runNegativeTest(
			new String[] {
				"p1/B.java",
				"package p1;\n" +
				"class A {}\n" +
				"public class B extends A {\n" +
				" public B(A... args) {}\n" +
				" public B() {}\n" +
				"}\n",
				"p2/C.java",
				"package p2;\n" +
				"import p1.B;\n" +
				"public class C {\n" +
				"	public static final void main(String[] args) {\n" +
				"		new B(new B(), new B());\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in p2\\C.java (at line 5)\n" + 
			"	new B(new B(), new B());\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor B(A...) of type B is not applicable as the formal varargs element type A is not accessible here\n" + 
			"----------\n");
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382469
	public void testBug382469() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		String[] src = 
			new String[] {
				"X.java",
				"public class X {\n" +
				"    private static void bar(Object... objs) {\n" +
				"	     System.out.println (\"1\");\n" +
				"    }\n" +
				"    private static void bar(int intValue, Object... objs) {\n" +
				"	     System.out.println (\"2\");\n" +
				"    }\n" +
				"    public static void main(String args[]) {\n" +
				"        bar(5);\n" +
				"    }\n" +
				"}\n"
			};
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel < ClassFileConstants.JDK1_7) {
				this.runConformTest(
					src,
					"2");
			} else {
				this.runNegativeTest(
						src,
						"----------\n" + 
						"1. WARNING in X.java (at line 5)\n" + 
						"	private static void bar(int intValue, Object... objs) {\n" + 
						"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
						"The method bar(int, Object...) from the type X is never used locally\n" + 
						"----------\n" + 
						"2. ERROR in X.java (at line 9)\n" + 
						"	bar(5);\n" + 
						"	^^^\n" + 
						"The method bar(Object[]) is ambiguous for the type X\n" + 
						"----------\n");
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=386361
	public void testBug386361() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
		String[] src = 
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void test(int i, Object... objects) {\n" +
				"	     System.out.println (\"1\");\n" +
				"    }\n" +
				"    public static void test(Object... objects) {\n" +
				"	     System.out.println (\"2\");\n" +
				"    }\n" +
				"    public static void main(String args[]) {\n" +
				"        test(1,\"test\");\n" +
				"    }\n" +
				"}\n"
			};
		try {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "true");
			if (this.complianceLevel < ClassFileConstants.JDK1_7) {
				this.runConformTest(
					src,
					"1");
			} else {
				this.runNegativeTest(
						src,
						"----------\n" + 
						"1. ERROR in X.java (at line 9)\n" + 
						"	test(1,\"test\");\n" + 
						"	^^^^\n" + 
						"The method test(int, Object[]) is ambiguous for the type X\n" + 
						"----------\n");
			}
		} finally {
			System.setProperty("tolerateIllegalAmbiguousVarargsInvocation", "false");
		}
	}
}