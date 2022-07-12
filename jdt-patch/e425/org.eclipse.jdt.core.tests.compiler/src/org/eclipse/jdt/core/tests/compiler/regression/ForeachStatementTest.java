/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 388800 - [1.8] adjust tests to 1.8 JRE
 *								bug 393719 - [compiler] inconsistent warnings on iteration variables
 *     Jesper S Moller -  Contribution for
 *								bug 401853 - Eclipse Java compiler creates invalid bytecode (java.lang.VerifyError)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ForeachStatementTest extends AbstractComparableTest {

public ForeachStatementTest(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.5
 */
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test055" };
//	TESTS_NUMBERS = new int[] { 50, 51, 52, 53 };
//	TESTS_RANGE = new int[] { 34, 38 };
}
public static Test suite() {
	return buildComparableTestSuite(testClass());
}
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        \n" +
			"        for (char c : \"SUCCESS\".toCharArray()) {\n" +
			"            System.out.print(c);\n" +
			"        }\n" +
			"        System.out.println();\n" +
			"    }\n" +
			"}\n",
		},
		"SUCCESS");
}
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        \n" +
			"        for (int value : new int[] {value}) {\n" +
			"            System.out.println(value);\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	for (int value : new int[] {value}) {\n" +
		"	                            ^^^^^\n" +
		"value cannot be resolved to a variable\n" +
		"----------\n");
}
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        \n" +
			"        for (int value : value) {\n" +
			"            System.out.println(value);\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	for (int value : value) {\n" +
		"	                 ^^^^^\n" +
		"value cannot be resolved to a variable\n" +
		"----------\n");
}
public void test004() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
			"		int sum = 0;\n" +
			"		loop: for (final int e : tab) {\n" +
			"			sum += e;\n" +
			"			if (e == 3) {\n" +
			"				break loop;\n" +
			"			}\n" +
			"		}\n" +
			"		System.out.println(sum);\n" +
			"	}\n" +
			"}\n",
		},
		"6");
}
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"	    final int i;\n" +
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
			"		int sum = 0;\n" +
			"		loop: for (final int e : tab) {\n" +
			"			sum += e;\n" +
			"			if (e == 3) {\n" +
			"			    i = 1;\n" +
			"				break loop;\n" +
			"			}\n" +
			"		}\n" +
			"		System.out.println(sum + i);\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	System.out.println(sum + i);\n" +
		"	                         ^\n" +
		"The local variable i may not have been initialized\n" +
		"----------\n");
}
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"	    final int i;\n" +
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
			"		loop: for (final int e : tab) {\n" +
			"		    i = e;\n" +
			"			if (e == 3) {\n" +
			"			    i = 1;\n" +
			"				break loop;\n" +
			"			}\n" +
			"		}\n" +
			"		System.out.println(i);\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	i = e;\n" +
		"	^\n" +
		"The final local variable i may already have been assigned\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	i = 1;\n" +
		"	^\n" +
		"The final local variable i may already have been assigned\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 13)\n" +
		"	System.out.println(i);\n" +
		"	                   ^\n" +
		"The local variable i may not have been initialized\n" +
		"----------\n");
}
public void test007() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"	    int i;\n" +
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
			"		for (final int e : tab) {\n" +
			"		    i = e;\n" +
			"		}\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS",
		null,
		true,
		null,
		customOptions,
		null/*no custom requestor*/);

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 4, Locals: 7\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  bipush 9\n" +
		"     2  newarray int [10]\n" +
		"     4  dup\n" +
		"     5  iconst_0\n" +
		"     6  iconst_1\n" +
		"     7  iastore\n" +
		"     8  dup\n" +
		"     9  iconst_1\n" +
		"    10  iconst_2\n" +
		"    11  iastore\n" +
		"    12  dup\n" +
		"    13  iconst_2\n" +
		"    14  iconst_3\n" +
		"    15  iastore\n" +
		"    16  dup\n" +
		"    17  iconst_3\n" +
		"    18  iconst_4\n" +
		"    19  iastore\n" +
		"    20  dup\n" +
		"    21  iconst_4\n" +
		"    22  iconst_5\n" +
		"    23  iastore\n" +
		"    24  dup\n" +
		"    25  iconst_5\n" +
		"    26  bipush 6\n" +
		"    28  iastore\n" +
		"    29  dup\n" +
		"    30  bipush 6\n" +
		"    32  bipush 7\n" +
		"    34  iastore\n" +
		"    35  dup\n" +
		"    36  bipush 7\n" +
		"    38  bipush 8\n" +
		"    40  iastore\n" +
		"    41  dup\n" +
		"    42  bipush 8\n" +
		"    44  bipush 9\n" +
		"    46  iastore\n" +
		"    47  astore_2 [tab]\n" +
		"    48  aload_2 [tab]\n" +
		"    49  dup\n" +
		"    50  astore 6\n" +
		"    52  arraylength\n" +
		"    53  istore 5\n" +
		"    55  iconst_0\n" +
		"    56  istore 4\n" +
		"    58  goto 72\n" +
		"    61  aload 6\n" +
		"    63  iload 4\n" +
		"    65  iaload\n" +
		"    66  istore_3 [e]\n" +
		"    67  iload_3 [e]\n" +
		"    68  istore_1\n" +
		"    69  iinc 4 1\n" +
		"    72  iload 4\n" +
		"    74  iload 5\n" +
		"    76  if_icmplt 61\n" +
		"    79  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    82  ldc <String \"SUCCESS\"> [22]\n" +
		"    84  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    87  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 5]\n" +
		"        [pc: 48, line: 6]\n" +
		"        [pc: 67, line: 7]\n" +
		"        [pc: 69, line: 6]\n" +
		"        [pc: 79, line: 9]\n" +
		"        [pc: 87, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 88] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 48, pc: 88] local: tab index: 2 type: int[]\n" +
		"        [pc: 67, pc: 69] local: e index: 3 type: int\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(Iterable col) {\n" +
			"		for (X x : col) {\n" +
			"			System.out.println(x);\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	void foo(Iterable col) {\n" +
		"	         ^^^^^^^^\n" +
		"Iterable is a raw type. References to generic type Iterable<T> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	for (X x : col) {\n" +
		"	           ^^^\n" +
		"Type mismatch: cannot convert from element type Object to X\n" +
		"----------\n");
}
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(Iterable<String> col) {\n" +
			"		for (X x : col) {\n" +
			"			System.out.println(x);\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	for (X x : col) {\n" +
		"	           ^^^\n" +
		"Type mismatch: cannot convert from element type String to X\n" +
		"----------\n");
}
/*
 * Test implicit conversion to float. If missing, VerifyError
 */
public void test010() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
			"		int sum = 0;\n" +
			"		loop: for (final float e : tab) {\n" +
			"			sum += e;\n" +
			"			if (e == 3) {\n" +
			"				break loop;\n" +
			"			}\n" +
			"		}\n" +
			"		System.out.println(sum);\n" +
			"	}\n" +
			"}\n",
		},
		"6");
}
/*
 * Cannot convert int[] to int
 */
public void test011() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    \n" +
				"	public static void main(String[] args) {\n" +
				"		int[][] tab = new int[][] {\n" +
				"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" +
				"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" +
				"		};\n" +
				"		loop: for (final int e : tab) {\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	loop: for (final int e : tab) {\n" +
			"	                         ^^^\n" +
			"Type mismatch: cannot convert from element type int[] to int\n" +
			"----------\n");
}
/*
 * Ensure access to int[]
 */
public void test012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"		int[][] tab = new int[][] {\n" +
			"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" +
			"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" +
			"		};\n" +
			"		for (final int[] e : tab) {\n" +
			"			System.out.print(e.length);\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"99");
}
/*
 * Ensure access to int[]
 */
public void test013() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"		int[][] tab = new int[][] {\n" +
			"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" +
			"			new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 },\n" +
			"		};\n" +
			"		for (final int[] e : tab) {\n" +
			"			System.out.print(e[0]);\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"11");
}
/*
 * Empty block action
 */
public void test014() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"		int[] tab = new int[] { 1 };\n" +
			"		for (final int e : tab) {\n" +
			"		}\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 4, Locals: 2\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  newarray int [10]\n" +
		"     3  dup\n" +
		"     4  iconst_0\n" +
		"     5  iconst_1\n" +
		"     6  iastore\n" +
		"     7  astore_1 [tab]\n" +
		"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    11  ldc <String \"SUCCESS\"> [22]\n" +
		"    13  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    16  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 8, line: 7]\n" +
		"        [pc: 16, line: 8]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 8, pc: 17] local: tab index: 1 type: int[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
/*
 * Empty statement action
 */
public void test015() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"		int[] tab = new int[] { 1 };\n" +
			"		for (final int e : tab);\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 4, Locals: 2\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  newarray int [10]\n" +
		"     3  dup\n" +
		"     4  iconst_0\n" +
		"     5  iconst_1\n" +
		"     6  iastore\n" +
		"     7  astore_1 [tab]\n" +
		"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    11  ldc <String \"SUCCESS\"> [22]\n" +
		"    13  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    16  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 8, line: 6]\n" +
		"        [pc: 16, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 8, pc: 17] local: tab index: 1 type: int[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
/*
 * Empty block action
 */
public void test016() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"		int[] tab = new int[] { 1 };\n" +
			"		for (final int e : tab) {;\n" +
			"		}\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 4, Locals: 5\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  newarray int [10]\n" +
		"     3  dup\n" +
		"     4  iconst_0\n" +
		"     5  iconst_1\n" +
		"     6  iastore\n" +
		"     7  astore_1 [tab]\n" +
		"     8  aload_1 [tab]\n" +
		"     9  dup\n" +
		"    10  astore 4\n" +
		"    12  arraylength\n" +
		"    13  istore_3\n" +
		"    14  iconst_0\n" +
		"    15  istore_2\n" +
		"    16  goto 22\n" +
		"    19  iinc 2 1\n" +
		"    22  iload_2\n" +
		"    23  iload_3\n" +
		"    24  if_icmplt 19\n" +
		"    27  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    30  ldc <String \"SUCCESS\"> [22]\n" +
		"    32  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    35  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 8, line: 5]\n" +
		"        [pc: 27, line: 7]\n" +
		"        [pc: 35, line: 8]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 36] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 8, pc: 36] local: tab index: 1 type: int[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
/*
 * Ensure access to int[]
 */
public void test017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"		int[] tab = new int[] { 1 };\n" +
			"		for (final int e : tab) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");
}
/*
 * Break the loop
 */
public void test018() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"		int[] tab = new int[] { 1 };\n" +
			"		for (final int e : tab) {\n" +
			"			System.out.println(e);\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"1");
	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 4, Locals: 4\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  newarray int [10]\n" +
		"     3  dup\n" +
		"     4  iconst_0\n" +
		"     5  iconst_1\n" +
		"     6  iastore\n" +
		"     7  astore_1 [tab]\n" +
		"     8  aload_1 [tab]\n" +
		"     9  dup\n" +
		"    10  astore_3\n" +
		"    11  arraylength\n" +
		"    12  ifeq 26\n" +
		"    15  aload_3\n" +
		"    16  iconst_0\n" +
		"    17  iaload\n" +
		"    18  istore_2 [e]\n" +
		"    19  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    22  iload_2 [e]\n" +
		"    23  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
		"    26  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 8, line: 5]\n" +
		"        [pc: 19, line: 6]\n" +
		"        [pc: 26, line: 9]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 27] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 8, pc: 27] local: tab index: 1 type: int[]\n" +
		"        [pc: 19, pc: 26] local: e index: 2 type: int\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
/*
 * Break the loop
 */
public void test019() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"		int[] tab = new int[] {};\n" +
			"		System.out.print(\"SUC\");\n" +
			"		for (final int e : tab) {\n" +
			"			System.out.print(\"1x\");\n" +
			"			break;\n" +
			"		}\n" +
			"		System.out.println(\"CESS\");\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_0\n" +
		"     1  newarray int [10]\n" +
		"     3  astore_1 [tab]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"     7  ldc <String \"SUC\"> [22]\n" +
		"     9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
		"    12  aload_1 [tab]\n" +
		"    13  dup\n" +
		"    14  astore_2\n" +
		"    15  arraylength\n" +
		"    16  ifeq 27\n" +
		"    19  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    22  ldc <String \"1x\"> [30]\n" +
		"    24  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
		"    27  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    30  ldc <String \"CESS\"> [32]\n" +
		"    32  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]\n" +
		"    35  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 4, line: 5]\n" +
		"        [pc: 12, line: 6]\n" +
		"        [pc: 19, line: 7]\n" +
		"        [pc: 27, line: 10]\n" +
		"        [pc: 35, line: 11]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 36] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 36] local: tab index: 1 type: int[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
/*
 * Break the loop
 */
public void test020() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    \n" +
			"	public static void main(String[] args) {\n" +
			"		int[] tab = new int[] {};\n" +
			"		System.out.print(\"SUC\");\n" +
			"		loop: for (final int e : tab) {\n" +
			"			System.out.print(\"1x\");\n" +
			"			continue loop;\n" +
			"		}\n" +
			"		System.out.println(\"CESS\");\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 5\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_0\n" +
		"     1  newarray int [10]\n" +
		"     3  astore_1 [tab]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"     7  ldc <String \"SUC\"> [22]\n" +
		"     9  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
		"    12  aload_1 [tab]\n" +
		"    13  dup\n" +
		"    14  astore 4\n" +
		"    16  arraylength\n" +
		"    17  istore_3\n" +
		"    18  iconst_0\n" +
		"    19  istore_2\n" +
		"    20  goto 34\n" +
		"    23  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    26  ldc <String \"1x\"> [30]\n" +
		"    28  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
		"    31  iinc 2 1\n" +
		"    34  iload_2\n" +
		"    35  iload_3\n" +
		"    36  if_icmplt 23\n" +
		"    39  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    42  ldc <String \"CESS\"> [32]\n" +
		"    44  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]\n" +
		"    47  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 4, line: 5]\n" +
		"        [pc: 12, line: 6]\n" +
		"        [pc: 23, line: 7]\n" +
		"        [pc: 31, line: 6]\n" +
		"        [pc: 39, line: 10]\n" +
		"        [pc: 47, line: 11]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 48] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 48] local: tab index: 1 type: int[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
public void test021() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
			"		int sum = 0;\n" +
			"		int i = 0;\n" +
			"		loop1: while(true) {\n" +
			"			i++;\n" +
			"			loop: for (final int e : tab) {\n" +
			"				sum += e;\n" +
			"				if (i == 3) {\n" +
			"					break loop1;\n" +
			"				} else if (e == 5) {\n" +
			"					break loop;\n" +
			"				} else {\n" +
			"					continue;\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"		System.out.println(sum);\n" +
			"	}\n" +
			"}",
		},
		"31");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 4, Locals: 8\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"      0  bipush 9\n" +
		"      2  newarray int [10]\n" +
		"      4  dup\n" +
		"      5  iconst_0\n" +
		"      6  iconst_1\n" +
		"      7  iastore\n" +
		"      8  dup\n" +
		"      9  iconst_1\n" +
		"     10  iconst_2\n" +
		"     11  iastore\n" +
		"     12  dup\n" +
		"     13  iconst_2\n" +
		"     14  iconst_3\n" +
		"     15  iastore\n" +
		"     16  dup\n" +
		"     17  iconst_3\n" +
		"     18  iconst_4\n" +
		"     19  iastore\n" +
		"     20  dup\n" +
		"     21  iconst_4\n" +
		"     22  iconst_5\n" +
		"     23  iastore\n" +
		"     24  dup\n" +
		"     25  iconst_5\n" +
		"     26  bipush 6\n" +
		"     28  iastore\n" +
		"     29  dup\n" +
		"     30  bipush 6\n" +
		"     32  bipush 7\n" +
		"     34  iastore\n" +
		"     35  dup\n" +
		"     36  bipush 7\n" +
		"     38  bipush 8\n" +
		"     40  iastore\n" +
		"     41  dup\n" +
		"     42  bipush 8\n" +
		"     44  bipush 9\n" +
		"     46  iastore\n" +
		"     47  astore_1 [tab]\n" +
		"     48  iconst_0\n" +
		"     49  istore_2 [sum]\n" +
		"     50  iconst_0\n" +
		"     51  istore_3 [i]\n" +
		"     52  iinc 3 1 [i]\n" +
		"     55  aload_1 [tab]\n" +
		"     56  dup\n" +
		"     57  astore 7\n" +
		"     59  arraylength\n" +
		"     60  istore 6\n" +
		"     62  iconst_0\n" +
		"     63  istore 5\n" +
		"     65  goto 100\n" +
		"     68  aload 7\n" +
		"     70  iload 5\n" +
		"     72  iaload\n" +
		"     73  istore 4 [e]\n" +
		"     75  iload_2 [sum]\n" +
		"     76  iload 4 [e]\n" +
		"     78  iadd\n" +
		"     79  istore_2 [sum]\n" +
		"     80  iload_3 [i]\n" +
		"     81  iconst_3\n" +
		"     82  if_icmpne 88\n" +
		"     85  goto 110\n" +
		"     88  iload 4 [e]\n" +
		"     90  iconst_5\n" +
		"     91  if_icmpne 97\n" +
		"     94  goto 52\n" +
		"     97  iinc 5 1\n" +
		"    100  iload 5\n" +
		"    102  iload 6\n" +
		"    104  if_icmplt 68\n" +
		"    107  goto 52\n" +
		"    110  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    113  iload_2 [sum]\n" +
		"    114  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
		"    117  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 48, line: 4]\n" +
		"        [pc: 50, line: 5]\n" +
		"        [pc: 52, line: 7]\n" +
		"        [pc: 55, line: 8]\n" +
		"        [pc: 75, line: 9]\n" +
		"        [pc: 80, line: 10]\n" +
		"        [pc: 85, line: 11]\n" +
		"        [pc: 88, line: 12]\n" +
		"        [pc: 94, line: 13]\n" +
		"        [pc: 97, line: 8]\n" +
		"        [pc: 107, line: 6]\n" +
		"        [pc: 110, line: 19]\n" +
		"        [pc: 117, line: 20]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 118] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 48, pc: 118] local: tab index: 1 type: int[]\n" +
		"        [pc: 50, pc: 118] local: sum index: 2 type: int\n" +
		"        [pc: 52, pc: 118] local: i index: 3 type: int\n" +
		"        [pc: 75, pc: 97] local: e index: 4 type: int\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
public void test022() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		ArrayList<Integer> arrayList = new ArrayList<Integer>();\n" +
			"		for (int i = 0; i < 10; i++) {\n" +
			"			arrayList.add(new Integer(i));\n" +
			"		}\n" +
			"		int sum = 0;\n" +
			"		for (Integer e : arrayList) {\n" +
			"			sum += e.intValue();\n" +
			"		}\n" +
			"		System.out.println(sum);\n" +
			"	}\n" +
			"}",
		},
		"45");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 4, Locals: 5\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  new java.util.ArrayList [16]\n" +
		"     3  dup\n" +
		"     4  invokespecial java.util.ArrayList() [18]\n" +
		"     7  astore_1 [arrayList]\n" +
		"     8  iconst_0\n" +
		"     9  istore_2 [i]\n" +
		"    10  goto 29\n" +
		"    13  aload_1 [arrayList]\n" +
		"    14  new java.lang.Integer [19]\n" +
		"    17  dup\n" +
		"    18  iload_2 [i]\n" +
		"    19  invokespecial java.lang.Integer(int) [21]\n" +
		"    22  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [24]\n" +
		"    25  pop\n" +
		"    26  iinc 2 1 [i]\n" +
		"    29  iload_2 [i]\n" +
		"    30  bipush 10\n" +
		"    32  if_icmplt 13\n" +
		"    35  iconst_0\n" +
		"    36  istore_2 [sum]\n" +
		"    37  aload_1 [arrayList]\n" +
		"    38  invokevirtual java.util.ArrayList.iterator() : java.util.Iterator [28]\n" +
		"    41  astore 4\n" +
		"    43  goto 64\n" +
		"    46  aload 4\n" +
		"    48  invokeinterface java.util.Iterator.next() : java.lang.Object [32] [nargs: 1]\n" +
		"    53  checkcast java.lang.Integer [19]\n" +
		"    56  astore_3 [e]\n" +
		"    57  iload_2 [sum]\n" +
		"    58  aload_3 [e]\n" +
		"    59  invokevirtual java.lang.Integer.intValue() : int [38]\n" +
		"    62  iadd\n" +
		"    63  istore_2 [sum]\n" +
		"    64  aload 4\n" +
		"    66  invokeinterface java.util.Iterator.hasNext() : boolean [42] [nargs: 1]\n" +
		"    71  ifne 46\n" +
		"    74  getstatic java.lang.System.out : java.io.PrintStream [46]\n" +
		"    77  iload_2 [sum]\n" +
		"    78  invokevirtual java.io.PrintStream.println(int) : void [52]\n" +
		"    81  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 5]\n" +
		"        [pc: 8, line: 6]\n" +
		"        [pc: 13, line: 7]\n" +
		"        [pc: 26, line: 6]\n" +
		"        [pc: 35, line: 9]\n" +
		"        [pc: 37, line: 10]\n" +
		"        [pc: 57, line: 11]\n" +
		"        [pc: 64, line: 10]\n" +
		"        [pc: 74, line: 13]\n" +
		"        [pc: 81, line: 14]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 82] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 8, pc: 82] local: arrayList index: 1 type: java.util.ArrayList\n" +
		"        [pc: 10, pc: 35] local: i index: 2 type: int\n" +
		"        [pc: 37, pc: 82] local: sum index: 2 type: int\n" +
		"        [pc: 57, pc: 64] local: e index: 3 type: java.lang.Integer\n" +
		"      Local variable type table:\n" +
		"        [pc: 8, pc: 82] local: arrayList index: 1 type: java.util.ArrayList<java.lang.Integer>\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}

/*
 * Type mismatch, using non parameterized collection type (indirectly implementing parameterized type)
 */
public void test023() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"		for (Thread s : new AX()) {\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class AX implements Iterable<String> {\n" +
				"    \n" +
				"   public Iterator<String> iterator() {\n" +
				"        return null;\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	for (Thread s : new AX()) {\n" +
			"	                ^^^^^^^^\n" +
			"Type mismatch: cannot convert from element type String to Thread\n" +
			"----------\n");
}
public void test024() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String[] tab = new String[] {\"SUCCESS\"};\n" +
			"		List list = new ArrayList();\n" +
			"		for (String arg : tab) {		\n" +
			"			list.add(arg);\n" +
			"		}\n" +
			"		for (Object arg: list) {\n" +
			"			System.out.print(arg);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 4, Locals: 7\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  anewarray java.lang.String [16]\n" +
		"     4  dup\n" +
		"     5  iconst_0\n" +
		"     6  ldc <String \"SUCCESS\"> [18]\n" +
		"     8  aastore\n" +
		"     9  astore_1 [tab]\n" +
		"    10  new java.util.ArrayList [20]\n" +
		"    13  dup\n" +
		"    14  invokespecial java.util.ArrayList() [22]\n" +
		"    17  astore_2 [list]\n" +
		"    18  aload_1 [tab]\n" +
		"    19  dup\n" +
		"    20  astore 6\n" +
		"    22  arraylength\n" +
		"    23  istore 5\n" +
		"    25  iconst_0\n" +
		"    26  istore 4\n" +
		"    28  goto 48\n" +
		"    31  aload 6\n" +
		"    33  iload 4\n" +
		"    35  aaload\n" +
		"    36  astore_3 [arg]\n" +
		"    37  aload_2 [list]\n" +
		"    38  aload_3 [arg]\n" +
		"    39  invokeinterface java.util.List.add(java.lang.Object) : boolean [23] [nargs: 2]\n" +
		"    44  pop\n" +
		"    45  iinc 4 1\n" +
		"    48  iload 4\n" +
		"    50  iload 5\n" +
		"    52  if_icmplt 31\n" +
		"    55  aload_2 [list]\n" +
		"    56  invokeinterface java.util.List.iterator() : java.util.Iterator [29] [nargs: 1]\n" +
		"    61  astore 4\n" +
		"    63  goto 81\n" +
		"    66  aload 4\n" +
		"    68  invokeinterface java.util.Iterator.next() : java.lang.Object [33] [nargs: 1]\n" +
		"    73  astore_3 [arg]\n" +
		"    74  getstatic java.lang.System.out : java.io.PrintStream [39]\n" +
		"    77  aload_3 [arg]\n" +
		"    78  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [45]\n" +
		"    81  aload 4\n" +
		"    83  invokeinterface java.util.Iterator.hasNext() : boolean [51] [nargs: 1]\n" +
		"    88  ifne 66\n" +
		"    91  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 10, line: 7]\n" +
		"        [pc: 18, line: 8]\n" +
		"        [pc: 37, line: 9]\n" +
		"        [pc: 45, line: 8]\n" +
		"        [pc: 55, line: 11]\n" +
		"        [pc: 74, line: 12]\n" +
		"        [pc: 81, line: 11]\n" +
		"        [pc: 91, line: 14]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 92] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 10, pc: 92] local: tab index: 1 type: java.lang.String[]\n" +
		"        [pc: 18, pc: 92] local: list index: 2 type: java.util.List\n" +
		"        [pc: 37, pc: 45] local: arg index: 3 type: java.lang.String\n" +
		"        [pc: 74, pc: 81] local: arg index: 3 type: java.lang.Object\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
public void test025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void bug(List<String> lines) {\n" +
			"        for (int i=0; i<1; i++) {\n" +
			"           for (String test: lines) {\n" +
			"                System.out.print(test);\n" +
			"           }\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"    	ArrayList<String> tab = new ArrayList<String>();\n" +
			"    	tab.add(\"SUCCESS\");\n" +
			"    	bug(tab);\n" +
			"    }\n" +
			"}",
		},
		"SUCCESS");
}
// 68440 - verify error due to local variable invalid slot sharing
public void test026() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    Object[] array = {\n" +
			"    };\n" +
			"    void test() {\n" +
			"        for (Object object : array) {\n" +
			"            String str = object.toString();\n" +
			"            str += \"\";\n" + // force 'str' to be preserved during codegen
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().test();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n",
		},
		"SUCCESS");
}
// 68863 - missing local variable attribute after foreach statement
public void test027() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"    Object[] array = {\n" +
			"    };\n" +
			"		java.util.ArrayList i;	\n" +
			"		for (Object object : array) {\n" +
			"			if (args == null) {\n" +
			"				i = null;\n" +
			"				break;\n" +
			"			}\n" +
			"			return;\n" +
			"		};\n" +
			"		System.out.println(\"SUCCESS\");	\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_0\n" +
		"     1  anewarray java.lang.Object [3]\n" +
		"     4  astore_1 [array]\n" +
		"     5  aload_1 [array]\n" +
		"     6  dup\n" +
		"     7  astore_2\n" +
		"     8  arraylength\n" +
		"     9  ifeq 22\n" +
		"    12  aload_0 [args]\n" +
		"    13  ifnonnull 21\n" +
		"    16  aconst_null\n" +
		"    17  pop\n" +
		"    18  goto 22\n" +
		"    21  return\n" +
		"    22  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    25  ldc <String \"SUCCESS\"> [22]\n" +
		"    27  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    30  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 5, line: 6]\n" +
		"        [pc: 12, line: 7]\n" +
		"        [pc: 16, line: 8]\n" +
		"        [pc: 18, line: 9]\n" +
		"        [pc: 21, line: 11]\n" +
		"        [pc: 22, line: 13]\n" +
		"        [pc: 30, line: 14]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 31] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 5, pc: 31] local: array index: 1 type: java.lang.Object[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//72760 - missing local variable attribute after foreach statement
public void test028() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"    public static void main(String args[]) {\n" +
			"    	ArrayList<ArrayList<String>> slist = new ArrayList<ArrayList<String>>();\n" +
			"    	\n" +
			"    	slist.add(new ArrayList<String>());\n" +
			"    	slist.get(0).add(\"SU\");\n" +
			"    	slist.get(0).add(\"C\");\n" +
			"    	slist.get(0).add(\"C\");\n" +
			"    	\n" +
			"    	slist.add(new ArrayList<String>());\n" +
			"    	slist.get(1).add(\"E\");\n" +
			"    	slist.get(1).add(\"S\");\n" +
			"    	slist.get(1).add(\"S\");\n" +
			"    	\n" +
			"    	for (int i=0; i<slist.size(); i++){\n" +
			"    		for (String s : slist.get(i)){\n" +
			"    			System.out.print(s);\n" +
			"    		}\n" +
			"    	}\n" +
			"    } \n" +
			"} \n" +
			"",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 3, Locals: 5\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"      0  new java.util.ArrayList [16]\n" +
		"      3  dup\n" +
		"      4  invokespecial java.util.ArrayList() [18]\n" +
		"      7  astore_1 [slist]\n" +
		"      8  aload_1 [slist]\n" +
		"      9  new java.util.ArrayList [16]\n" +
		"     12  dup\n" +
		"     13  invokespecial java.util.ArrayList() [18]\n" +
		"     16  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]\n" +
		"     19  pop\n" +
		"     20  aload_1 [slist]\n" +
		"     21  iconst_0\n" +
		"     22  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]\n" +
		"     25  checkcast java.util.ArrayList [16]\n" +
		"     28  ldc <String \"SU\"> [27]\n" +
		"     30  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]\n" +
		"     33  pop\n" +
		"     34  aload_1 [slist]\n" +
		"     35  iconst_0\n" +
		"     36  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]\n" +
		"     39  checkcast java.util.ArrayList [16]\n" +
		"     42  ldc <String \"C\"> [29]\n" +
		"     44  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]\n" +
		"     47  pop\n" +
		"     48  aload_1 [slist]\n" +
		"     49  iconst_0\n" +
		"     50  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]\n" +
		"     53  checkcast java.util.ArrayList [16]\n" +
		"     56  ldc <String \"C\"> [29]\n" +
		"     58  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]\n" +
		"     61  pop\n" +
		"     62  aload_1 [slist]\n" +
		"     63  new java.util.ArrayList [16]\n" +
		"     66  dup\n" +
		"     67  invokespecial java.util.ArrayList() [18]\n" +
		"     70  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]\n" +
		"     73  pop\n" +
		"     74  aload_1 [slist]\n" +
		"     75  iconst_1\n" +
		"     76  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]\n" +
		"     79  checkcast java.util.ArrayList [16]\n" +
		"     82  ldc <String \"E\"> [31]\n" +
		"     84  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]\n" +
		"     87  pop\n" +
		"     88  aload_1 [slist]\n" +
		"     89  iconst_1\n" +
		"     90  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]\n" +
		"     93  checkcast java.util.ArrayList [16]\n" +
		"     96  ldc <String \"S\"> [33]\n" +
		"     98  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]\n" +
		"    101  pop\n" +
		"    102  aload_1 [slist]\n" +
		"    103  iconst_1\n" +
		"    104  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]\n" +
		"    107  checkcast java.util.ArrayList [16]\n" +
		"    110  ldc <String \"S\"> [33]\n" +
		"    112  invokevirtual java.util.ArrayList.add(java.lang.Object) : boolean [19]\n" +
		"    115  pop\n" +
		"    116  iconst_0\n" +
		"    117  istore_2 [i]\n" +
		"    118  goto 168\n" +
		"    121  aload_1 [slist]\n" +
		"    122  iload_2 [i]\n" +
		"    123  invokevirtual java.util.ArrayList.get(int) : java.lang.Object [23]\n" +
		"    126  checkcast java.util.ArrayList [16]\n" +
		"    129  invokevirtual java.util.ArrayList.iterator() : java.util.Iterator [35]\n" +
		"    132  astore 4\n" +
		"    134  goto 155\n" +
		"    137  aload 4\n" +
		"    139  invokeinterface java.util.Iterator.next() : java.lang.Object [39] [nargs: 1]\n" +
		"    144  checkcast java.lang.String [45]\n" +
		"    147  astore_3 [s]\n" +
		"    148  getstatic java.lang.System.out : java.io.PrintStream [47]\n" +
		"    151  aload_3 [s]\n" +
		"    152  invokevirtual java.io.PrintStream.print(java.lang.String) : void [53]\n" +
		"    155  aload 4\n" +
		"    157  invokeinterface java.util.Iterator.hasNext() : boolean [59] [nargs: 1]\n" +
		"    162  ifne 137\n" +
		"    165  iinc 2 1 [i]\n" +
		"    168  iload_2 [i]\n" +
		"    169  aload_1 [slist]\n" +
		"    170  invokevirtual java.util.ArrayList.size() : int [63]\n" +
		"    173  if_icmplt 121\n" +
		"    176  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 8, line: 8]\n" +
		"        [pc: 20, line: 9]\n" +
		"        [pc: 34, line: 10]\n" +
		"        [pc: 48, line: 11]\n" +
		"        [pc: 62, line: 13]\n" +
		"        [pc: 74, line: 14]\n" +
		"        [pc: 88, line: 15]\n" +
		"        [pc: 102, line: 16]\n" +
		"        [pc: 116, line: 18]\n" +
		"        [pc: 121, line: 19]\n" +
		"        [pc: 148, line: 20]\n" +
		"        [pc: 155, line: 19]\n" +
		"        [pc: 165, line: 18]\n" +
		"        [pc: 176, line: 23]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 177] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 8, pc: 177] local: slist index: 1 type: java.util.ArrayList\n" +
		"        [pc: 118, pc: 176] local: i index: 2 type: int\n" +
		"        [pc: 148, pc: 155] local: s index: 3 type: java.lang.String\n" +
		"      Local variable type table:\n" +
		"        [pc: 8, pc: 177] local: slist index: 1 type: java.util.ArrayList<java.util.ArrayList<java.lang.String>>\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86487
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"    public static void main(String args[]) {\n" +
			"        ArrayList<Integer> arr = new ArrayList<Integer>();\n" +
			"    	 arr.add(0);\n" +
			"    	 arr.add(1);\n" +
			"		 int counter = 0;\n" +
			"        // tested statement:\n" +
			"        for (int i : arr){\n" +
			"            ++counter;\n" +
			"        }\n" +
			"        System.out.print(\"SUCCESS\");\n" +
			"    }\n" +
			"}",
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86487
public void test030() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"    public static void main(String args[]) {\n" +
			"        int[] arr = new int[2];\n" +
			"    	 arr[0]= 0;\n" +
			"    	 arr[1]= 1;\n" +
			"		 int counter = 0;\n" +
			"        // tested statement:\n" +
			"        for (int i : arr){\n" +
			"            ++counter;\n" +
			"        }\n" +
			"        System.out.print(\"SUCCESS\");\n" +
			"    }\n" +
			"}",
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86487
public void test031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"    public static void main(String args[]) {\n" +
			"        ArrayList arr = new ArrayList();\n" +
			"    	 arr.add(new Object());\n" +
			"		 int counter = 0;\n" +
			"        // tested statement:\n" +
			"        for (Object o : arr){\n" +
			"            ++counter;\n" +
			"        }\n" +
			"        System.out.print(\"SUCCESS\");\n" +
			"    }\n" +
			"}",
		},
		"SUCCESS");
}
public void test032() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	abstract class Member implements Iterable<String> {\n" +
			"	}\n" +
			"	void foo(Member m) {\n" +
			"		for(String s : m) {\n" +
			"			return;\n" +
			"		} \n" +
			"	}\n" +
			"}\n",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108783
public void test033() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	abstract class Member implements Iterable<String> {\n" +
			"	}\n" +
			"	void foo(Member m) {\n" +
			"		for(String s : m) {\n" +
			"			return;\n" +
			"		} \n" +
			"	}\n" +
			"}\n",
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108783 - variation
public void test034() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"\n" +
			"public class X <T extends Bar> {\n" +
			"	public static void main(String[] args) {\n" +
			"		new X<Bar>().foo(new Bar());\n" +
			"	}\n" +
			"	void foo(T t) {\n" +
			"		for (String s : t) {\n" +
			"			System.out.print(s);\n" +
			"		}\n" +
			"		System.out.println();\n" +
			"	}\n" +
			"}\n" +
			"class ArrayIterator<T> implements Iterator<T> {\n" +
			"	T[] values;\n" +
			"	int count;\n" +
			"	ArrayIterator(T[] values) {\n" +
			"		this.values = values;\n" +
			"		this.count = 0;\n" +
			"	}\n" +
			"	public boolean hasNext() {\n" +
			"		return this.count < this.values.length;\n" +
			"	}\n" +
			"	public T next() {\n" +
			"		if (this.count >= this.values.length) throw new NoSuchElementException();\n" +
			"		T value = this.values[this.count];\n" +
			"		this.values[this.count++] = null; // clear\n" +
			"		return value;\n" +
			"	}\n" +
			"	public void remove() {\n" +
			"	}\n" +
			"}\n" +
			"class Bar implements Iterable<String> {\n" +
			"	public Iterator<String> iterator() {\n" +
			"		return new ArrayIterator<String>(new String[]{\"a\",\"b\"});\n" +
			"	}\n" +
			"}\n",
		},
		"ab");
	// 	ensure proper declaring class (Bar): 1  invokevirtual Bar.iterator() : java.util.Iterator  [33]
	String expectedOutput =
		"  // Method descriptor #25 (LBar;)V\n" +
		"  // Signature: (TT;)V\n" +
		"  // Stack: 2, Locals: 4\n" +
		"  void foo(Bar t);\n" +
		"     0  aload_1 [t]\n" +
		"     1  invokevirtual Bar.iterator() : java.util.Iterator [30]\n" +
		"     4  astore_3\n" +
		"     5  goto 25\n" +
		"     8  aload_3\n" +
		"     9  invokeinterface java.util.Iterator.next() : java.lang.Object [34] [nargs: 1]\n" +
		"    14  checkcast java.lang.String [40]\n" +
		"    17  astore_2 [s]\n" +
		"    18  getstatic java.lang.System.out : java.io.PrintStream [42]\n" +
		"    21  aload_2 [s]\n" +
		"    22  invokevirtual java.io.PrintStream.print(java.lang.String) : void [48]\n" +
		"    25  aload_3\n" +
		"    26  invokeinterface java.util.Iterator.hasNext() : boolean [54] [nargs: 1]\n" +
		"    31  ifne 8\n" +
		"    34  getstatic java.lang.System.out : java.io.PrintStream [42]\n" +
		"    37  invokevirtual java.io.PrintStream.println() : void [58]\n" +
		"    40  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 8]\n" +
		"        [pc: 18, line: 9]\n" +
		"        [pc: 25, line: 8]\n" +
		"        [pc: 34, line: 11]\n" +
		"        [pc: 40, line: 12]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 41] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 41] local: t index: 1 type: Bar\n" +
		"        [pc: 18, pc: 25] local: s index: 2 type: java.lang.String\n" +
		"      Local variable type table:\n" +
		"        [pc: 0, pc: 41] local: this index: 0 type: X<T>\n" +
		"        [pc: 0, pc: 41] local: t index: 1 type: T\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108783 - variation
public void test035() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"\n" +
			"public class X <T extends IFoo> {\n" +
			"	public static void main(String[] args) {\n" +
			"		new X<IFoo>().foo(new Bar());\n" +
			"	}\n" +
			"	void foo(T t) {\n" +
			"		for (String s : t) {\n" +
			"			System.out.print(s);\n" +
			"		}\n" +
			"		System.out.println();\n" +
			"	}\n" +
			"}\n" +
			"class ArrayIterator<T> implements Iterator<T> {\n" +
			"	T[] values;\n" +
			"	int count;\n" +
			"	ArrayIterator(T[] values) {\n" +
			"		this.values = values;\n" +
			"		this.count = 0;\n" +
			"	}\n" +
			"	public boolean hasNext() {\n" +
			"		return this.count < this.values.length;\n" +
			"	}\n" +
			"	public T next() {\n" +
			"		if (this.count >= this.values.length) throw new NoSuchElementException();\n" +
			"		T value = this.values[this.count];\n" +
			"		this.values[this.count++] = null; // clear\n" +
			"		return value;\n" +
			"	}\n" +
			"	public void remove() {\n" +
			"	}\n" +
			"}\n" +
			"interface IFoo extends Iterable<String> {\n" +
			"}\n" +
			"class Bar implements IFoo {\n" +
			"	public Iterator<String> iterator() {\n" +
			"		return new ArrayIterator<String>(new String[]{\"a\",\"b\"});\n" +
			"	}\n" +
			"}\n",
		},
		"ab");
	// 	ensure proper declaring class (IFoo): 1  invokeinterface IFoo.iterator() : java.util.Iterator  [35] [nargs: 1]
	String expectedOutput =
		"  // Method descriptor #25 (LIFoo;)V\n" +
		"  // Signature: (TT;)V\n" +
		"  // Stack: 2, Locals: 4\n" +
		"  void foo(IFoo t);\n" +
		"     0  aload_1 [t]\n" +
		"     1  invokeinterface IFoo.iterator() : java.util.Iterator [30] [nargs: 1]\n" +
		"     6  astore_3\n" +
		"     7  goto 27\n" +
		"    10  aload_3\n" +
		"    11  invokeinterface java.util.Iterator.next() : java.lang.Object [36] [nargs: 1]\n" +
		"    16  checkcast java.lang.String [42]\n" +
		"    19  astore_2 [s]\n" +
		"    20  getstatic java.lang.System.out : java.io.PrintStream [44]\n" +
		"    23  aload_2 [s]\n" +
		"    24  invokevirtual java.io.PrintStream.print(java.lang.String) : void [50]\n" +
		"    27  aload_3\n" +
		"    28  invokeinterface java.util.Iterator.hasNext() : boolean [56] [nargs: 1]\n" +
		"    33  ifne 10\n" +
		"    36  getstatic java.lang.System.out : java.io.PrintStream [44]\n" +
		"    39  invokevirtual java.io.PrintStream.println() : void [60]\n" +
		"    42  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 8]\n" +
		"        [pc: 20, line: 9]\n" +
		"        [pc: 27, line: 8]\n" +
		"        [pc: 36, line: 11]\n" +
		"        [pc: 42, line: 12]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 43] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 43] local: t index: 1 type: IFoo\n" +
		"        [pc: 20, pc: 27] local: s index: 2 type: java.lang.String\n" +
		"      Local variable type table:\n" +
		"        [pc: 0, pc: 43] local: this index: 0 type: X<T>\n" +
		"        [pc: 0, pc: 43] local: t index: 1 type: T\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=108783
public void test036() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.Arrays;\n" +
			"import java.util.Iterator;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class X implements Iterable<String>, Runnable {\n" +
			"	public <T extends Runnable & Iterable<String>> void foo(T t) {\n" +
			"		for (String s : t)\n" +
			"			System.out.print(s);\n" +
			"	}\n" +
			"	public void run() {	/* */ }\n" +
			"	private List<String> list = Arrays.asList(new String[] { \"a\", \"b\" });\n" +
			"	public Iterator<String> iterator() {\n" +
			"		return this.list.iterator();\n" +
			"	}\n" +
			"	public static void main(String... args) {\n" +
			"		X x = new X();\n" +
			"		x.foo(x);\n" +
			"	}\n" +
			"}",
		},
		"ab");
	String expectedOutput =
		"  // Method descriptor #37 (Ljava/lang/Runnable;)V\n" +
		"  // Signature: <T::Ljava/lang/Runnable;:Ljava/lang/Iterable<Ljava/lang/String;>;>(TT;)V\n" +
		"  // Stack: 2, Locals: 4\n" +
		"  public void foo(java.lang.Runnable t);\n" +
		"     0  aload_1 [t]\n" +
		"     1  checkcast java.lang.Iterable [5]\n" +
		"     4  invokeinterface java.lang.Iterable.iterator() : java.util.Iterator [39] [nargs: 1]\n" +
		"     9  astore_3\n" +
		"    10  goto 30\n" +
		"    13  aload_3\n" +
		"    14  invokeinterface java.util.Iterator.next() : java.lang.Object [43] [nargs: 1]\n" +
		"    19  checkcast java.lang.String [18]\n" +
		"    22  astore_2 [s]\n" +
		"    23  getstatic java.lang.System.out : java.io.PrintStream [49]\n" +
		"    26  aload_2 [s]\n" +
		"    27  invokevirtual java.io.PrintStream.print(java.lang.String) : void [55]\n" +
		"    30  aload_3\n" +
		"    31  invokeinterface java.util.Iterator.hasNext() : boolean [61] [nargs: 1]\n" +
		"    36  ifne 13\n" +
		"    39  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 7]\n" +
		"        [pc: 23, line: 8]\n" +
		"        [pc: 30, line: 7]\n" +
		"        [pc: 39, line: 9]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 40] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 40] local: t index: 1 type: java.lang.Runnable\n" +
		"        [pc: 23, pc: 30] local: s index: 2 type: java.lang.String\n" +
		"      Local variable type table:\n" +
		"        [pc: 0, pc: 40] local: t index: 1 type: T\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108783
public void test037() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.Arrays;\n" +
			"import java.util.Iterator;\n" +
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"	public static <T extends ArrayList<String>> void foo(T t) {\n" +
			"		for (String s : t)\n" +
			"			System.out.print(s);\n" +
			"	}\n" +
			"	private static ArrayList<String> list = new ArrayList<String>();\n" +
			"	static {\n" +
			"		list.addAll(Arrays.asList(new String[] { \"a\", \"b\" }));\n" +
			"	}\n" +
			"	public static void main(String... args) {\n" +
			"		foo(list);\n" +
			"	}\n" +
			"}",
		},
		"ab");

	String expectedOutput =
		"  // Method descriptor #41 (Ljava/util/ArrayList;)V\n" +
		"  // Signature: <T:Ljava/util/ArrayList<Ljava/lang/String;>;>(TT;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void foo(java.util.ArrayList t);\n" +
		"     0  aload_0 [t]\n" +
		"     1  invokevirtual java.util.ArrayList.iterator() : java.util.Iterator [43]\n" +
		"     4  astore_2\n" +
		"     5  goto 25\n" +
		"     8  aload_2\n" +
		"     9  invokeinterface java.util.Iterator.next() : java.lang.Object [47] [nargs: 1]\n" +
		"    14  checkcast java.lang.String [19]\n" +
		"    17  astore_1 [s]\n" +
		"    18  getstatic java.lang.System.out : java.io.PrintStream [53]\n" +
		"    21  aload_1 [s]\n" +
		"    22  invokevirtual java.io.PrintStream.print(java.lang.String) : void [59]\n" +
		"    25  aload_2\n" +
		"    26  invokeinterface java.util.Iterator.hasNext() : boolean [65] [nargs: 1]\n" +
		"    31  ifne 8\n" +
		"    34  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 8]\n" +
		"        [pc: 18, line: 9]\n" +
		"        [pc: 25, line: 8]\n" +
		"        [pc: 34, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 35] local: t index: 0 type: java.util.ArrayList\n" +
		"        [pc: 18, pc: 25] local: s index: 1 type: java.lang.String\n" +
		"      Local variable type table:\n" +
		"        [pc: 0, pc: 35] local: t index: 0 type: T\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=119175
public void test038() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.HashSet;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X();\n" +
			"		x.foo();\n" +
			"		System.out.println(\"SUCCESS\");	\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"	    for(Object o : new HashSet<Object>()) {\n" +
			"	    	System.out.println(o);\n" +
			"	    	continue;\n" +
			"	    }\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor #6 ()V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public void foo();\n" +
		"     0  new java.util.HashSet [37]\n" +
		"     3  dup\n" +
		"     4  invokespecial java.util.HashSet() [39]\n" +
		"     7  invokevirtual java.util.HashSet.iterator() : java.util.Iterator [40]\n" +
		"    10  astore_2\n" +
		"    11  goto 28\n" +
		"    14  aload_2\n" +
		"    15  invokeinterface java.util.Iterator.next() : java.lang.Object [44] [nargs: 1]\n" +
		"    20  astore_1 [o]\n" +
		"    21  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"    24  aload_1 [o]\n" +
		"    25  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [50]\n" +
		"    28  aload_2\n" +
		"    29  invokeinterface java.util.Iterator.hasNext() : boolean [53] [nargs: 1]\n" +
		"    34  ifne 14\n" +
		"    37  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 9]\n" +
		"        [pc: 21, line: 10]\n" +
		"        [pc: 28, line: 9]\n" +
		"        [pc: 37, line: 13]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 38] local: this index: 0 type: X\n" +
		"        [pc: 21, pc: 28] local: o index: 1 type: java.lang.Object\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test039() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.HashSet;\n" +
			"import java.util.Set;\n" +
			"import java.util.Iterator;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"        public static void main(String[] args) {\n" +
			"                for (Object o : initForEach()) {\n" +
			"                }\n" +
			"        }\n" +
			"\n" +
			"		static class MyIterator<T> implements Iterator<T> {\n" +
			"			Iterator<T> iterator;\n" +
			"			\n" +
			"			MyIterator(Iterator<T> it) {\n" +
			"				this.iterator = it;\n" +
			"			}\n" +
			"			public boolean hasNext() {\n" +
			"				System.out.println(\"hasNext\");\n" +
			"				return this.iterator.hasNext();\n" +
			"			}			\n" +
			"			public T next() {\n" +
			"				System.out.println(\"next\");\n" +
			"				return this.iterator.next();\n" +
			"			}\n" +
			"			public void remove() {\n" +
			"				System.out.println(\"remove\");\n" +
			"				this.iterator.remove();\n" +
			"			}\n" +
			"		}\n" +
			"		\n" +
			"        static Set<Object> initForEach()        {\n" +
			"                System.out.println(\"initForEach\");\n" +
			"                HashSet<Object> set = new HashSet<Object>() {\n" +
			"                	private static final long serialVersionUID = 1L;\n" +
			"                	public Iterator<Object> iterator() {\n" +
			"                		System.out.println(\"iterator\");\n" +
			"                		return new MyIterator<Object>(super.iterator());\n" +
			"                	}\n" +
			"                };\n" +
			"                for (int i = 0; i < 3; i++) set.add(i);\n" +
			"                return set;\n" +
			"        }\n" +
			"}",
		},
		"initForEach\n" +
		"iterator\n" +
		"hasNext\n" +
		"next\n" +
		"hasNext\n" +
		"next\n" +
		"hasNext\n" +
		"next\n" +
		"hasNext");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  invokestatic X.initForEach() : java.util.Set [16]\n" +
		"     3  invokeinterface java.util.Set.iterator() : java.util.Iterator [20] [nargs: 1]\n" +
		"     8  astore_1\n" +
		"     9  goto 19\n" +
		"    12  aload_1\n" +
		"    13  invokeinterface java.util.Iterator.next() : java.lang.Object [26] [nargs: 1]\n" +
		"    18  pop\n" +
		"    19  aload_1\n" +
		"    20  invokeinterface java.util.Iterator.hasNext() : boolean [32] [nargs: 1]\n" +
		"    25  ifne 12\n" +
		"    28  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 8]\n" +
		"        [pc: 28, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 29] local: args index: 0 type: java.lang.String[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test040() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.HashSet;\n" +
			"import java.util.Set;\n" +
			"import java.util.Iterator;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"        public static void main(String[] args) {\n" +
			"                for (Object o : initForEach()) {\n" +
			"                }\n" +
			"        }\n" +
			"\n" +
			"		static class MyIterator<T> implements Iterator<T> {\n" +
			"			Iterator<T> iterator;\n" +
			"			\n" +
			"			MyIterator(Iterator<T> it) {\n" +
			"				this.iterator = it;\n" +
			"			}\n" +
			"			public boolean hasNext() {\n" +
			"				System.out.println(\"hasNext\");\n" +
			"				return this.iterator.hasNext();\n" +
			"			}			\n" +
			"			public T next() {\n" +
			"				System.out.println(\"next\");\n" +
			"				return this.iterator.next();\n" +
			"			}\n" +
			"			public void remove() {\n" +
			"				System.out.println(\"remove\");\n" +
			"				this.iterator.remove();\n" +
			"			}\n" +
			"		}\n" +
			"		\n" +
			"        static Set<Object> initForEach()        {\n" +
			"                System.out.println(\"initForEach\");\n" +
			"                HashSet<Object> set = new HashSet<Object>() {\n" +
			"                	private static final long serialVersionUID = 1L;\n" +
			"                	public Iterator<Object> iterator() {\n" +
			"                		System.out.println(\"iterator\");\n" +
			"                		return new MyIterator<Object>(super.iterator());\n" +
			"                	}\n" +
			"                };\n" +
			"                for (int i = 0; i < 3; i++) set.add(i);\n" +
			"                return set;\n" +
			"        }\n" +
			"}",
		},
		"initForEach\n" +
		"iterator\n" +
		"hasNext\n" +
		"next\n" +
		"hasNext\n" +
		"next\n" +
		"hasNext\n" +
		"next\n" +
		"hasNext",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 1, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  invokestatic X.initForEach() : java.util.Set [16]\n" +
		"     3  invokeinterface java.util.Set.iterator() : java.util.Iterator [20] [nargs: 1]\n" +
		"     8  astore_2\n" +
		"     9  goto 19\n" +
		"    12  aload_2\n" +
		"    13  invokeinterface java.util.Iterator.next() : java.lang.Object [26] [nargs: 1]\n" +
		"    18  astore_1\n" +
		"    19  aload_2\n" +
		"    20  invokeinterface java.util.Iterator.hasNext() : boolean [32] [nargs: 1]\n" +
		"    25  ifne 12\n" +
		"    28  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 8]\n" +
		"        [pc: 28, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 29] local: args index: 0 type: java.lang.String[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test041() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                for (int i : initForEach()) {\n" +
			"                }\n" +
			"        }\n" +
			"        static int[] initForEach() {\n" +
			"                System.out.println(\"initForEach\");\n" +
			"                return new int[] {1, 2, 3, 4};\n" +
			"        }\n" +
			"}",
		},
		"initForEach");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 1, Locals: 1\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"    0  invokestatic X.initForEach() : int[] [16]\n" +
		"    3  pop\n" +
		"    4  return\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test042() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                for (int i : initForEach()) {\n" +
			"                }\n" +
			"        }\n" +
			"        static int[] initForEach() {\n" +
			"                System.out.println(\"initForEach\");\n" +
			"                return new int[] {1, 2, 3, 4};\n" +
			"        }\n" +
			"}",
		},
		"initForEach",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 5\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  invokestatic X.initForEach() : int[] [16]\n" +
		"     3  dup\n" +
		"     4  astore 4\n" +
		"     6  arraylength\n" +
		"     7  istore_3\n" +
		"     8  iconst_0\n" +
		"     9  istore_2\n" +
		"    10  goto 21\n" +
		"    13  aload 4\n" +
		"    15  iload_2\n" +
		"    16  iaload\n" +
		"    17  istore_1\n" +
		"    18  iinc 2 1\n" +
		"    21  iload_2\n" +
		"    22  iload_3\n" +
		"    23  if_icmplt 13\n" +
		"    26  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 26, line: 5]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 27] local: args index: 0 type: java.lang.String[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test043() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"			 foo();\n" +
			"        }\n" +
			"        public static void foo() {\n" +
			"                for (int i : initForEach()) {\n" +
			"                }\n" +
			"        }\n" +
			"        static int[] initForEach() {\n" +
			"                System.out.println(\"initForEach\");\n" +
			"                return new int[] {1, 2, 3, 4};\n" +
			"        }\n" +
			"}",
		},
		"initForEach");

	String expectedOutput =
		"  // Method descriptor #6 ()V\n" +
		"  // Stack: 1, Locals: 0\n" +
		"  public static void foo();\n" +
		"    0  invokestatic X.initForEach() : int[] [21]\n" +
		"    3  pop\n" +
		"    4  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test044() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"			 foo();\n" +
			"        }\n" +
			"        public static void foo() {\n" +
			"                for (int i : initForEach()) {\n" +
			"                }\n" +
			"        }\n" +
			"        static int[] initForEach() {\n" +
			"                System.out.println(\"initForEach\");\n" +
			"                return new int[] {1, 2, 3, 4};\n" +
			"        }\n" +
			"}",
		},
		"initForEach",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"  // Method descriptor #6 ()V\n" +
		"  // Stack: 2, Locals: 4\n" +
		"  public static void foo();\n" +
		"     0  invokestatic X.initForEach() : int[] [21]\n" +
		"     3  dup\n" +
		"     4  astore_3\n" +
		"     5  arraylength\n" +
		"     6  istore_2\n" +
		"     7  iconst_0\n" +
		"     8  istore_1\n" +
		"     9  goto 19\n" +
		"    12  aload_3\n" +
		"    13  iload_1\n" +
		"    14  iaload\n" +
		"    15  istore_0\n" +
		"    16  iinc 1 1\n" +
		"    19  iload_1\n" +
		"    20  iload_2\n" +
		"    21  if_icmplt 12\n" +
		"    24  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 24, line: 8]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test045() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                for (int i : initForEach()) {\n" +
			"                	System.out.print(\'a\');\n" +
			"                }\n" +
			"        }\n" +
			"        static int[] initForEach() {\n" +
			"                return new int[] {1, 2, 3, 4};\n" +
			"        }\n" +
			"}",
		},
		"aaaa",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 5\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  invokestatic X.initForEach() : int[] [16]\n" +
		"     3  dup\n" +
		"     4  astore 4\n" +
		"     6  arraylength\n" +
		"     7  istore_3\n" +
		"     8  iconst_0\n" +
		"     9  istore_2\n" +
		"    10  goto 29\n" +
		"    13  aload 4\n" +
		"    15  iload_2\n" +
		"    16  iaload\n" +
		"    17  istore_1 [i]\n" +
		"    18  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"    21  bipush 97\n" +
		"    23  invokevirtual java.io.PrintStream.print(char) : void [26]\n" +
		"    26  iinc 2 1\n" +
		"    29  iload_2\n" +
		"    30  iload_3\n" +
		"    31  if_icmplt 13\n" +
		"    34  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 18, line: 4]\n" +
		"        [pc: 26, line: 3]\n" +
		"        [pc: 34, line: 6]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 35] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 18, pc: 26] local: i index: 1 type: int\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150074
public void test046() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                for (int i : initForEach()) {\n" +
			"                	System.out.print(\'a\');\n" +
			"                }\n" +
			"        }\n" +
			"        static int[] initForEach() {\n" +
			"                return new int[] {1, 2, 3, 4};\n" +
			"        }\n" +
			"}",
		},
		"aaaa",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 4\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  invokestatic X.initForEach() : int[] [16]\n" +
		"     3  dup\n" +
		"     4  astore_3\n" +
		"     5  arraylength\n" +
		"     6  istore_2\n" +
		"     7  iconst_0\n" +
		"     8  istore_1\n" +
		"     9  goto 23\n" +
		"    12  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"    15  bipush 97\n" +
		"    17  invokevirtual java.io.PrintStream.print(char) : void [26]\n" +
		"    20  iinc 1 1\n" +
		"    23  iload_1\n" +
		"    24  iload_2\n" +
		"    25  if_icmplt 12\n" +
		"    28  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 12, line: 4]\n" +
		"        [pc: 20, line: 3]\n" +
		"        [pc: 28, line: 6]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 29] local: args index: 0 type: java.lang.String[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471
public void test047() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo3(int[] array) {\n" +
			"		for (int i : array) {\n" +
			"			System.out.println(i);\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");

	String expectedOutput =
		"  // Method descriptor #15 ([I)V\n" +
		"  // Stack: 2, Locals: 4\n" +
		"  void foo3(int[] array);\n" +
		"     0  aload_1 [array]\n" +
		"     1  dup\n" +
		"     2  astore_3\n" +
		"     3  arraylength\n" +
		"     4  ifeq 18\n" +
		"     7  aload_3\n" +
		"     8  iconst_0\n" +
		"     9  iaload\n" +
		"    10  istore_2 [i]\n" +
		"    11  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    14  iload_2 [i]\n" +
		"    15  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
		"    18  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 11, line: 4]\n" +
		"        [pc: 18, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 19] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 19] local: array index: 1 type: int[]\n" +
		"        [pc: 11, pc: 18] local: i index: 2 type: int\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471 - variation
public void test048() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo3(java.util.List<String> ls) {\n" +
			"		for (String s : ls) {\n" +
			"			System.out.println(s);\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");

	String expectedOutput =
		"  // Method descriptor #15 (Ljava/util/List;)V\n" +
		"  // Signature: (Ljava/util/List<Ljava/lang/String;>;)V\n" +
		"  // Stack: 2, Locals: 4\n" +
		"  void foo3(java.util.List ls);\n" +
		"     0  aload_1 [ls]\n" +
		"     1  invokeinterface java.util.List.iterator() : java.util.Iterator [18] [nargs: 1]\n" +
		"     6  astore_3\n" +
		"     7  aload_3\n" +
		"     8  invokeinterface java.util.Iterator.hasNext() : boolean [24] [nargs: 1]\n" +
		"    13  ifeq 33\n" +
		"    16  aload_3\n" +
		"    17  invokeinterface java.util.Iterator.next() : java.lang.Object [30] [nargs: 1]\n" +
		"    22  checkcast java.lang.String [34]\n" +
		"    25  astore_2 [s]\n" +
		"    26  getstatic java.lang.System.out : java.io.PrintStream [36]\n" +
		"    29  aload_2 [s]\n" +
		"    30  invokevirtual java.io.PrintStream.println(java.lang.String) : void [42]\n" +
		"    33  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 26, line: 4]\n" +
		"        [pc: 33, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 34] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 34] local: ls index: 1 type: java.util.List\n" +
		"        [pc: 26, pc: 33] local: s index: 2 type: java.lang.String\n" +
		"      Local variable type table:\n" +
		"        [pc: 0, pc: 34] local: ls index: 1 type: java.util.List<java.lang.String>\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471 - variation
public void test049() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo3(java.util.List l) {\n" +
			"		for (Object o : l) {\n" +
			"			System.out.println(o);\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");

	String expectedOutput =
		"  // Method descriptor #15 (Ljava/util/List;)V\n" +
		"  // Stack: 2, Locals: 4\n" +
		"  void foo3(java.util.List l);\n" +
		"     0  aload_1 [l]\n" +
		"     1  invokeinterface java.util.List.iterator() : java.util.Iterator [16] [nargs: 1]\n" +
		"     6  astore_3\n" +
		"     7  aload_3\n" +
		"     8  invokeinterface java.util.Iterator.hasNext() : boolean [22] [nargs: 1]\n" +
		"    13  ifeq 30\n" +
		"    16  aload_3\n" +
		"    17  invokeinterface java.util.Iterator.next() : java.lang.Object [28] [nargs: 1]\n" +
		"    22  astore_2 [o]\n" +
		"    23  getstatic java.lang.System.out : java.io.PrintStream [32]\n" +
		"    26  aload_2 [o]\n" +
		"    27  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [38]\n" +
		"    30  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 23, line: 4]\n" +
		"        [pc: 30, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 31] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 31] local: l index: 1 type: java.util.List\n" +
		"        [pc: 23, pc: 30] local: o index: 2 type: java.lang.Object\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=291472
public void test050() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	private T values;\n" +
			"	public X(T values) {\n" +
			"		this.values = values;\n" +
			"	}\n" +
			"	public T getValues() {\n" +
			"		return values;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<short[]> x = new X<short[]>(new short[] { 1, 2, 3, 4, 5 });\n" +
			"		for (int i : x.getValues()) {\n" +
			"			System.out.print(i);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"12345");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=291472
public void test051() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	private T values;\n" +
			"	public X(T values) {\n" +
			"		this.values = values;\n" +
			"	}\n" +
			"	public T getValues() {\n" +
			"		return values;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<short[]> x = new X<short[]>(new short[] { 1, 2, 3, 4, 5 });\n" +
			"		for (long l : x.getValues()) {\n" +
			"			System.out.print(l);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"12345");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=291472
public void test052() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	private T values;\n" +
			"	public X(T values) {\n" +
			"		this.values = values;\n" +
			"	}\n" +
			"	public T getValues() {\n" +
			"		return values;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<Short[]> x = new X<Short[]>(new Short[] { 1, 2, 3, 4, 5 });\n" +
			"		for (int i : x.getValues()) {\n" +
			"			System.out.print(i);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"12345");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=291472
public void test053() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	private T values;\n" +
			"	public X(T values) {\n" +
			"		this.values = values;\n" +
			"	}\n" +
			"	public T getValues() {\n" +
			"		return values;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<Short[]> x = new X<Short[]>(new Short[] { 1, 2, 3, 4, 5 });\n" +
			"		for (long i : x.getValues()) {\n" +
			"			System.out.print(i);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"12345");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321085
public void test054() throws Exception {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.HashSet;\n" +
				"import java.util.Set;\n" +
				"public class X {\n" +
				"    void foo() {\n" +
				"       HashSet<String> x = new HashSet<String>();\n" +
				"        x.add(\"a\");\n" +
				"        HashSet<Integer> y = new HashSet<Integer>();\n" +
				"        y.add(1);\n" +
				"        Set<String> [] OK= new Set[] { x, y };\n" +
				"        for (Set<String> BUG : new Set[] { x, y }) {\n" +
				"            for (String str : BUG)\n" +
				"                System.out.println(str);\n" +
				"        }\n" +
				"        Set [] set = new Set[] { x, y };\n" +
				"        for (Set<String> BUG : set) {\n" +
				"            for (String str : BUG)\n" +
				"                System.out.println(str);\n" +
				"        }\n" +
				"    }\n" +
				"    Zork z;\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 9)\n" +
			"	Set<String> [] OK= new Set[] { x, y };\n" +
			"	                   ^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type Set[] needs unchecked conversion to conform to Set<String>[]\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 10)\n" +
			"	for (Set<String> BUG : new Set[] { x, y }) {\n" +
			"	                       ^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Elements of type Set need unchecked conversion to conform to Set<String>\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 14)\n" +
			"	Set [] set = new Set[] { x, y };\n" +
			"	^^^\n" +
			"Set is a raw type. References to generic type Set<E> should be parameterized\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 15)\n" +
			"	for (Set<String> BUG : set) {\n" +
			"	                       ^^^\n" +
			"Type safety: Elements of type Set need unchecked conversion to conform to Set<String>\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 20)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
// https://bugs.eclipse.org/393719
// like test054 but suppressing the warnings.
public void test055() throws Exception {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.HashSet;\n" +
				"import java.util.Set;\n" +
				"public class X {\n" +
				"    void foo() {\n" +
				"       HashSet<String> x = new HashSet<String>();\n" +
				"        x.add(\"a\");\n" +
				"        HashSet<Integer> y = new HashSet<Integer>();\n" +
				"        y.add(1);\n" +
				"        @SuppressWarnings(\"unchecked\") Set<String> [] OK= new Set[] { x, y };\n" +
				"        for (@SuppressWarnings(\"unchecked\") Set<String> BUG : new Set[] { x, y }) {\n" +
				"            for (String str : BUG)\n" +
				"                System.out.println(str);\n" +
				"        }\n" +
				"        @SuppressWarnings({\"rawtypes\", \"unchecked\"}) Set [] set = new Set[] { x, y };\n" +
				"        for (@SuppressWarnings(\"unchecked\") Set<String> BUG : set) {\n" +
				"            for (String str : BUG)\n" +
				"                System.out.println(str);\n" +
				"        }\n" +
				"    }\n" +
				"    Zork z;\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 20)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
// https://bugs.eclipse.org/393719
// "unchecked" warning against the collection (raw Iterable)
public void test056() throws Exception {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"    void testRawType(@SuppressWarnings(\"rawtypes\") List<List> lists) {\n" +
				"		List<String> stringList = lists.get(0); // (1)\n" +
				"		for (List<String> strings : lists)      // (2)\n" +
				"			stringList = strings;\n" +
				"		for (@SuppressWarnings(\"unchecked\") List<String> strings : lists) // no warning\n" +
				"			stringList = strings;\n" +
				"		System.out.println(stringList.get(0));\n" +
				"	 }\n" +
				"    Zork z;\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	List<String> stringList = lists.get(0); // (1)\n" +
			"	                          ^^^^^^^^^^^^\n" +
			"Type safety: The expression of type List needs unchecked conversion to conform to List<String>\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	for (List<String> strings : lists)      // (2)\n" +
			"	                            ^^^^^\n" +
			"Type safety: Elements of type List need unchecked conversion to conform to List<String>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401853
// Eclipse Java compiler creates invalid bytecode (java.lang.VerifyError)
public void test057() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);

	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] argv) {\n" +
			"		for (long l : new ArrayList<Long>()) {}\n" +
			"	}\n" +
			"}",
		},
		"",
		null,
		true,
		null,
		options,
		null);

	String expectedOutput =
		"public class X {\n" +
		"  \n" +
		"  // Method descriptor #6 ()V\n" +
		"  // Stack: 1, Locals: 1\n" +
		"  public X();\n" +
		"    0  aload_0 [this]\n" +
		"    1  invokespecial java.lang.Object() [8]\n" +
		"    4  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 2\n" +
		"  public static void main(java.lang.String[] argv);\n" +
		"     0  new java.util.ArrayList [16]\n" +
		"     3  dup\n" +
		"     4  invokespecial java.util.ArrayList() [18]\n" +
		"     7  invokevirtual java.util.ArrayList.iterator() : java.util.Iterator [19]\n" +
		"    10  astore_1\n" +
		"    11  goto 27\n" +
		"    14  aload_1\n" +
		"    15  invokeinterface java.util.Iterator.next() : java.lang.Object [23] [nargs: 1]\n" +
		"    20  checkcast java.lang.Long [29]\n" +
		"    23  invokevirtual java.lang.Long.longValue() : long [31]\n" +
		"    26  pop2\n" +
		"    27  aload_1\n" +
		"    28  invokeinterface java.util.Iterator.hasNext() : boolean [35] [nargs: 1]\n" +
		"    33  ifne 14\n" +
		"    36  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 5]\n" +
		"        [pc: 36, line: 6]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 37] local: argv index: 0 type: java.lang.String[]\n";

	File f = new File(OUTPUT_DIR + File.separator + "X.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425632, [1.8][compiler] Compiler gets the scope of enhanced for loop's expression wrong.
public void test425632() throws Exception {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static int[] i = {1, 2, 3};\n" +
				"	public static void main(String [] args) {\n" +
				"		for (int i : i) {\n" +
				"			System.out.println(i);\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			"1\n2\n3");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=508215
public void testBug508215() throws Exception {
	this.runConformTest(
		new String[] {
				"linenumber/DebugErrorVarargs1Arg.java",
				"package linenumber;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"public class DebugErrorVarargs1Arg {\n" +
				"	public static void main(String[] args) {\n" +
				"		for (Integer i : Arrays.asList(1)) {\n" +
				"			System.out.println(i);\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"",
		}
	);

	String expectedOutput =
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  anewarray java.lang.Integer [16]\n" +
		"     4  dup\n" +
		"     5  iconst_0\n" +
		"     6  iconst_1\n" +
		"     7  invokestatic java.lang.Integer.valueOf(int) : java.lang.Integer [18]\n" +
		"    10  aastore\n" +
		"    11  invokestatic java.util.Arrays.asList(java.lang.Object[]) : java.util.List [22]\n" +
		"    14  invokeinterface java.util.List.iterator() : java.util.Iterator [28] [nargs: 1]\n" +
		"    19  astore_2\n" +
		"    20  goto 40\n" +
		"    23  aload_2\n" +
		"    24  invokeinterface java.util.Iterator.next() : java.lang.Object [34] [nargs: 1]\n" +
		"    29  checkcast java.lang.Integer [16]\n" +
		"    32  astore_1 [i]\n" +
		"    33  getstatic java.lang.System.out : java.io.PrintStream [40]\n" +
		"    36  aload_1 [i]\n" +
		"    37  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [46]\n" +
		"    40  aload_2\n" +
		"    41  invokeinterface java.util.Iterator.hasNext() : boolean [52] [nargs: 1]\n" +
		"    46  ifne 23\n" +
		"    49  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 33, line: 7]\n" +
		"        [pc: 40, line: 6]\n" +
		"        [pc: 49, line: 9]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 50] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 33, pc: 40] local: i index: 1 type: java.lang.Integer\n" +
		"";

	File f = new File(OUTPUT_DIR + File.separator + "linenumber" + File.separator + "DebugErrorVarargs1Arg.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
public static Class testClass() {
	return ForeachStatementTest.class;
}
}
