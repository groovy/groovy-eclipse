/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;

public class ClassFileReaderTest_1_4 extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 78, 79 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(), ClassFileConstants.JDK1_4);
	}
	public static Class testClass() {
		return ClassFileReaderTest_1_4.class;
	}

	public ClassFileReaderTest_1_4(String name) {
		super(name);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=15051
	 */
	public void test001() throws Exception {
		String source =
			"public class A001 {\n" +
			"	private int i = 6;\n" +
			"	public int foo() {\n" +
			"		class A {\n" +
			"			int get() {\n" +
			"				return i;\n" +
			"			}\n" +
			"		}\n" +
			"		return new A().get();\n" +
			"	}\n" +
			"};";
		String expectedOutput =
			"  // Method descriptor #19 ()I\n" +
			"  // Stack: 3, Locals: 1\n" +
			"  public int foo();\n" +
			"     0  new A001$1$A [20]\n" +
			"     3  dup\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokespecial A001$1$A(A001) [22]\n" +
			"     8  invokevirtual A001$1$A.get() : int [25]\n" +
			"    11  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 12] local: this index: 0 type: A001\n";
		checkClassFile("A001", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25188
	 */
	public void test002() throws Exception {
		String source =
			"public class A002 {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(); /* \\u000d: CARRIAGE RETURN */\n" +
			"		System.out.println();\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     3  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"    12  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 6, line: 4]\n" +
			"        [pc: 12, line: 5]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n";
		checkClassFile("A002", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26098
	 */
	public void test003() throws Exception {
		String source =
			"public class A003 {\n" +
			"\n" +
			"	public int bar() {\n" +
			"		return 0;\n" +
			"	}\n" +
			"	\n" +
			"	public void foo() {\n" +
			"		System.out.println(bar());\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ()I\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public int bar();\n" +
			"    0  iconst_0\n" +
			"    1  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 2] local: this index: 0 type: A003\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  public void foo();\n" +
			"     0  getstatic java.lang.System.out : java.io.PrintStream [17]\n" +
			"     3  aload_0 [this]\n" +
			"     4  invokevirtual A003.bar() : int [23]\n" +
			"     7  invokevirtual java.io.PrintStream.println(int) : void [25]\n" +
			"    10  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 8]\n" +
			"        [pc: 10, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 11] local: this index: 0 type: A003\n";
		checkClassFile("A003", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test004() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   && !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  bipush 6\n" +
			"     4  istore_2 [i]\n" +
			"     5  iload_2 [i]\n" +
			"     6  bipush 6\n" +
			"     8  if_icmpne 22\n" +
			"    11  iload_1 [b]\n" +
			"    12  ifne 22\n" +
			"    15  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    18  iload_2 [i]\n" +
			"    19  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    22  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 4]\n" +
			"        [pc: 5, line: 5]\n" +
			"        [pc: 11, line: 6]\n" +
			"        [pc: 15, line: 7]\n" +
			"        [pc: 22, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 23] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 23] local: b index: 1 type: boolean\n" +
			"        [pc: 5, pc: 23] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test005() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   && true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpne 16\n" +
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    12  iload_1 [i]\n" +
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    16  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 9, line: 6]\n" +
			"        [pc: 16, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test006() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   && false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"    0  bipush 6\n" +
			"    2  istore_1 [i]\n" +
			"    3  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 4] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 4] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test007() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   && !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  iload_1 [b]\n" +
			"     3  ifne 12\n" +
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"    12  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 6, line: 6]\n" +
			"        [pc: 12, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test008() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   && !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"    0  iconst_0\n" +
			"    1  istore_1 [b]\n" +
			"    2  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 3] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 3] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test009() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   || !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  bipush 6\n" +
			"     4  istore_2 [i]\n" +
			"     5  iload_2 [i]\n" +
			"     6  bipush 6\n" +
			"     8  if_icmpeq 15\n" +
			"    11  iload_1 [b]\n" +
			"    12  ifne 22\n" +
			"    15  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    18  iload_2 [i]\n" +
			"    19  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    22  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 4]\n" +
			"        [pc: 5, line: 5]\n" +
			"        [pc: 11, line: 6]\n" +
			"        [pc: 15, line: 7]\n" +
			"        [pc: 22, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 23] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 23] local: b index: 1 type: boolean\n" +
			"        [pc: 5, pc: 23] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test010() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   || true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     6  iload_1 [i]\n" +
			"     7  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    10  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 6]\n" +
			"        [pc: 10, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 11] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 11] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test011() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   || false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpne 16\n" +
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    12  iload_1 [i]\n" +
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    16  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 9, line: 6]\n" +
			"        [pc: 16, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test012() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   || !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"    0  iconst_0\n" +
			"    1  istore_1 [b]\n" +
			"    2  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    5  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"    8  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 6]\n" +
			"        [pc: 8, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 9] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 9] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test013() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   || !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  iload_1 [b]\n" +
			"     3  ifne 12\n" +
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"    12  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 6, line: 6]\n" +
			"        [pc: 12, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test014() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   == !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  bipush 6\n" +
			"     4  istore_2 [i]\n" +
			"     5  iload_2 [i]\n" +
			"     6  bipush 6\n" +
			"     8  if_icmpne 15\n" +
			"    11  iconst_1\n" +
			"    12  goto 16\n" +
			"    15  iconst_0\n" +
			"    16  iload_1 [b]\n" +
			"    17  ifeq 24\n" +
			"    20  iconst_0\n" +
			"    21  goto 25\n" +
			"    24  iconst_1\n" +
			"    25  if_icmpne 35\n" +
			"    28  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    31  iload_2 [i]\n" +
			"    32  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    35  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 4]\n" +
			"        [pc: 5, line: 5]\n" +
			"        [pc: 16, line: 6]\n" +
			"        [pc: 28, line: 7]\n" +
			"        [pc: 35, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 36] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 36] local: b index: 1 type: boolean\n" +
			"        [pc: 5, pc: 36] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test015() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   == true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpne 16\n" +
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    12  iload_1 [i]\n" +
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    16  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 9, line: 6]\n" +
			"        [pc: 16, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test016() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   == false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpeq 16\n" +
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    12  iload_1 [i]\n" +
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    16  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 9, line: 6]\n" +
			"        [pc: 16, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test017() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   == !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  iload_1 [b]\n" +
			"     3  ifne 12\n" +
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"    12  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 6, line: 6]\n" +
			"        [pc: 12, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test018() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   == !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  iload_1 [b]\n" +
			"     3  ifeq 12\n" +
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"    12  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 6, line: 6]\n" +
			"        [pc: 12, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 * http:  //bugs.eclipse.org/bugs/show_bug.cgi?id=26881
	 */
	public void test019() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 5)\n" +
			"			? b : !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  bipush 6\n" +
			"     4  istore_2 [i]\n" +
			"     5  iload_2 [i]\n" +
			"     6  iconst_5\n" +
			"     7  if_icmpne 17\n" +
			"    10  iload_1 [b]\n" +
			"    11  ifeq 28\n" +
			"    14  goto 21\n" +
			"    17  iload_1 [b]\n" +
			"    18  ifne 28\n" +
			"    21  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    24  iload_2 [i]\n" +
			"    25  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    28  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 4]\n" +
			"        [pc: 5, line: 5]\n" +
			"        [pc: 10, line: 6]\n" +
			"        [pc: 21, line: 7]\n" +
			"        [pc: 28, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 29] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 29] local: b index: 1 type: boolean\n" +
			"        [pc: 5, pc: 29] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test020() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			>= 5) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  iconst_5\n" +
			"     5  if_icmplt 15\n" +
			"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    11  iload_1 [i]\n" +
			"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    15  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 4, line: 5]\n" +
			"        [pc: 8, line: 6]\n" +
			"        [pc: 15, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 16] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test021() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			>= 0) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  iflt 14\n" +
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    10  iload_1 [i]\n" +
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 14, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test022() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (0\n" +
			"			>= i) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  ifgt 14\n" +
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    10  iload_1 [i]\n" +
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 5]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 14, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test023() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			> 0) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  ifle 14\n" +
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    10  iload_1 [i]\n" +
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 14, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test024() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (0\n" +
			"			> i) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  ifge 14\n" +
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    10  iload_1 [i]\n" +
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 5]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 14, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test025() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			> 5) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  iconst_5\n" +
			"     5  if_icmple 15\n" +
			"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    11  iload_1 [i]\n" +
			"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    15  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 4, line: 5]\n" +
			"        [pc: 8, line: 6]\n" +
			"        [pc: 15, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 16] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test026() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			< 0) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  ifge 14\n" +
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    10  iload_1 [i]\n" +
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 14, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test027() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (0\n" +
			"			< i) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  ifle 14\n" +
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    10  iload_1 [i]\n" +
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 5]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 14, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test028() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			< 5) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  iconst_5\n" +
			"     5  if_icmpge 15\n" +
			"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    11  iload_1 [i]\n" +
			"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    15  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 4, line: 5]\n" +
			"        [pc: 8, line: 6]\n" +
			"        [pc: 15, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 16] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test029() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			<= 0) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  ifgt 14\n" +
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    10  iload_1 [i]\n" +
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 14, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test030() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (0\n" +
			"			<= i) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  iflt 14\n" +
			"     7  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    10  iload_1 [i]\n" +
			"    11  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 5]\n" +
			"        [pc: 7, line: 6]\n" +
			"        [pc: 14, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 15] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test031() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			<= 5) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  iconst_5\n" +
			"     5  if_icmpgt 15\n" +
			"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    11  iload_1 [i]\n" +
			"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    15  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 4, line: 5]\n" +
			"        [pc: 8, line: 6]\n" +
			"        [pc: 15, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 16] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test032() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if (i\n" +
			"			<= 5) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  iconst_5\n" +
			"     5  if_icmpgt 15\n" +
			"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    11  iload_1 [i]\n" +
			"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    15  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 4, line: 5]\n" +
			"        [pc: 8, line: 6]\n" +
			"        [pc: 15, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 16] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test033() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   & !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  bipush 6\n" +
			"     4  istore_2 [i]\n" +
			"     5  iload_2 [i]\n" +
			"     6  bipush 6\n" +
			"     8  if_icmpne 15\n" +
			"    11  iconst_1\n" +
			"    12  goto 16\n" +
			"    15  iconst_0\n" +
			"    16  iload_1 [b]\n" +
			"    17  ifeq 24\n" +
			"    20  iconst_0\n" +
			"    21  goto 25\n" +
			"    24  iconst_1\n" +
			"    25  iand\n" +
			"    26  ifeq 36\n" +
			"    29  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    32  iload_2 [i]\n" +
			"    33  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    36  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 4]\n" +
			"        [pc: 5, line: 5]\n" +
			"        [pc: 16, line: 6]\n" +
			"        [pc: 29, line: 7]\n" +
			"        [pc: 36, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 37] local: b index: 1 type: boolean\n" +
			"        [pc: 5, pc: 37] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test034() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   & true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpne 16\n" +
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    12  iload_1 [i]\n" +
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    16  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 9, line: 6]\n" +
			"        [pc: 16, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test035() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   & false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"    0  bipush 6\n" +
			"    2  istore_1 [i]\n" +
			"    3  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 4] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 4] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test036() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   & !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  iload_1 [b]\n" +
			"     3  ifne 12\n" +
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"    12  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 6, line: 6]\n" +
			"        [pc: 12, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test037() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   & !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"    0  iconst_0\n" +
			"    1  istore_1 [b]\n" +
			"    2  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 3] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 3] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test038() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   | !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  bipush 6\n" +
			"     4  istore_2 [i]\n" +
			"     5  iload_2 [i]\n" +
			"     6  bipush 6\n" +
			"     8  if_icmpne 15\n" +
			"    11  iconst_1\n" +
			"    12  goto 16\n" +
			"    15  iconst_0\n" +
			"    16  iload_1 [b]\n" +
			"    17  ifeq 24\n" +
			"    20  iconst_0\n" +
			"    21  goto 25\n" +
			"    24  iconst_1\n" +
			"    25  ior\n" +
			"    26  ifeq 36\n" +
			"    29  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    32  iload_2 [i]\n" +
			"    33  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    36  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 4]\n" +
			"        [pc: 5, line: 5]\n" +
			"        [pc: 16, line: 6]\n" +
			"        [pc: 29, line: 7]\n" +
			"        [pc: 36, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 37] local: b index: 1 type: boolean\n" +
			"        [pc: 5, pc: 37] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test039() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   | true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     6  iload_1 [i]\n" +
			"     7  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    10  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 6]\n" +
			"        [pc: 10, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 11] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 11] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test040() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   | false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpne 16\n" +
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    12  iload_1 [i]\n" +
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    16  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 9, line: 6]\n" +
			"        [pc: 16, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test041() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   | !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"    0  iconst_0\n" +
			"    1  istore_1 [b]\n" +
			"    2  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    5  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"    8  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 6]\n" +
			"        [pc: 8, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 9] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 9] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test042() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   | !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  iload_1 [b]\n" +
			"     3  ifne 12\n" +
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"    12  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 6, line: 6]\n" +
			"        [pc: 12, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test043() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   ^ !b) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  bipush 6\n" +
			"     4  istore_2 [i]\n" +
			"     5  iload_2 [i]\n" +
			"     6  bipush 6\n" +
			"     8  if_icmpne 15\n" +
			"    11  iconst_1\n" +
			"    12  goto 16\n" +
			"    15  iconst_0\n" +
			"    16  iload_1 [b]\n" +
			"    17  ifeq 24\n" +
			"    20  iconst_0\n" +
			"    21  goto 25\n" +
			"    24  iconst_1\n" +
			"    25  ixor\n" +
			"    26  ifeq 36\n" +
			"    29  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    32  iload_2 [i]\n" +
			"    33  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    36  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 4]\n" +
			"        [pc: 5, line: 5]\n" +
			"        [pc: 16, line: 6]\n" +
			"        [pc: 29, line: 7]\n" +
			"        [pc: 36, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 37] local: b index: 1 type: boolean\n" +
			"        [pc: 5, pc: 37] local: i index: 2 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test044() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   ^ true) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpeq 16\n" +
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    12  iload_1 [i]\n" +
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    16  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 9, line: 6]\n" +
			"        [pc: 16, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test045() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) \n" +
			"		   ^ false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  bipush 6\n" +
			"     2  istore_1 [i]\n" +
			"     3  iload_1 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpne 16\n" +
			"     9  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    12  iload_1 [i]\n" +
			"    13  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
			"    16  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 3, line: 4]\n" +
			"        [pc: 9, line: 6]\n" +
			"        [pc: 16, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 17] local: i index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test046() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (true\n" +
			"		   ^ !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  iload_1 [b]\n" +
			"     3  ifeq 12\n" +
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"    12  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 6, line: 6]\n" +
			"        [pc: 12, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26753
	 */
	public void test047() throws Exception {
		String source =
			"public class A {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = false;\n" +
			"		if (false\n" +
			"		   ^ !b) {   	\n" +
			"		   	System.out.println();\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [b]\n" +
			"     2  iload_1 [b]\n" +
			"     3  ifne 12\n" +
			"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     9  invokevirtual java.io.PrintStream.println() : void [22]\n" +
			"    12  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 2, line: 5]\n" +
			"        [pc: 6, line: 6]\n" +
			"        [pc: 12, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 13] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 13] local: b index: 1 type: boolean\n";
		checkClassFile("A", source, expectedOutput);
	}

	public void test048() throws Exception {
		String source =
			"public class A {\n" +
			"\n" +
			"	static int foo(boolean bool) {\n" +
			"	  int j;\n" +
			"	  try {\n" +
			"	    if (bool) return 1;\n" +
			"	    j = 2;\n" +
			"	  } finally {\n" +
			"	    j = 3;\n" +
			"	  }\n" +
			"	  return j;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		foo(false);\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 (Z)I\n" +
			"  // Stack: 1, Locals: 4\n" +
			"  static int foo(boolean bool);\n" +
			"     0  iload_0 [bool]\n" +
			"     1  ifeq 9\n" +
			"     4  jsr 20\n" +
			"     7  iconst_1\n" +
			"     8  ireturn\n" +
			"     9  iconst_2\n" +
			"    10  istore_1 [j]\n" +
			"    11  goto 25\n" +
			"    14  astore_3\n" +
			"    15  jsr 20\n" +
			"    18  aload_3\n" +
			"    19  athrow\n" +
			"    20  astore_2\n" +
			"    21  iconst_3\n" +
			"    22  istore_1 [j]\n" +
			"    23  ret 2\n" +
			"    25  jsr 20\n" +
			"    28  iload_1 [j]\n" +
			"    29  ireturn\n" +
			"      Exception Table:\n" +
			"        [pc: 0, pc: 7] -> 14 when : any\n" +
			"        [pc: 9, pc: 14] -> 14 when : any\n" +
			"        [pc: 25, pc: 28] -> 14 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 9, line: 7]\n" +
			"        [pc: 11, line: 8]\n" +
			"        [pc: 18, line: 10]\n" +
			"        [pc: 20, line: 8]\n" +
			"        [pc: 21, line: 9]\n" +
			"        [pc: 23, line: 10]\n" +
			"        [pc: 28, line: 11]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 30] local: bool index: 0 type: boolean\n" +
			"        [pc: 11, pc: 14] local: j index: 1 type: int\n" +
			"        [pc: 23, pc: 30] local: j index: 1 type: int\n";
		checkClassFile("A", source, expectedOutput);
	}

	public void test049() throws Exception {
		String source =
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		foo();\n" +
			"	}\n" +
			"	static void foo() {\n" +
			"		int i = 5;\n" +
			"		if ((i == 6) && false) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  static void foo();\n" +
			"    0  iconst_5\n" +
			"    1  istore_0 [i]\n" +
			"    2  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 2, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 3] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test050() throws Exception {
		String source =
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		foo();\n" +
			"	}\n" +
			"	static void foo() {\n" +
			"		int i = 5;\n" +
			"		if ((i == 6) && false) {}\n" +
			"		else {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void foo();\n" +
			"     0  iconst_5\n" +
			"     1  istore_0 [i]\n" +
			"     2  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
			"     5  iload_0 [i]\n" +
			"     6  invokevirtual java.io.PrintStream.println(int) : void [27]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 2, line: 9]\n" +
			"        [pc: 9, line: 11]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 10] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test051() throws Exception {
		String source =
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		bar();\n" +
			"	}\n" +
			"	static void bar() {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) || true) {\n" +
			"		} else {\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  static void bar();\n" +
			"    0  bipush 6\n" +
			"    2  istore_0 [i]\n" +
			"    3  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 3, line: 11]\n" +
			"      Local variable table:\n" +
			"        [pc: 3, pc: 4] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test052() throws Exception {
		String source =
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		bar();\n" +
			"	}\n" +
			"	static void bar() {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) || true) {\n" +
			"		   	System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void bar();\n" +
			"     0  bipush 6\n" +
			"     2  istore_0 [i]\n" +
			"     3  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
			"     6  iload_0 [i]\n" +
			"     7  invokevirtual java.io.PrintStream.println(int) : void [27]\n" +
			"    10  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 3, line: 8]\n" +
			"        [pc: 10, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 3, pc: 11] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test053() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		foo2();\n" +
			"	}\n" +
			"	static void foo2() {\n" +
			"		int i = 5;\n" +
			"		if ((i == 6) && (boom() && false)) {\n" +
			"		   	System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void foo2();\n" +
			"     0  iconst_5\n" +
			"     1  istore_0 [i]\n" +
			"     2  iload_0 [i]\n" +
			"     3  bipush 6\n" +
			"     5  if_icmpne 12\n" +
			"     8  invokestatic X.boom() : boolean [26]\n" +
			"    11  pop\n" +
			"    12  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 2, line: 10]\n" +
			"        [pc: 12, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 13] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test054() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		foo2();\n" +
			"	}\n" +
			"	static void foo2() {\n" +
			"		int i = 5;\n" +
			"		if ((i == 6) && (boom() && false)) {\n" +
			"		} else {\n" +
			"		   	System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void foo2();\n" +
			"     0  iconst_5\n" +
			"     1  istore_0 [i]\n" +
			"     2  iload_0 [i]\n" +
			"     3  bipush 6\n" +
			"     5  if_icmpne 12\n" +
			"     8  invokestatic X.boom() : boolean [26]\n" +
			"    11  pop\n" +
			"    12  getstatic java.lang.System.out : java.io.PrintStream [28]\n" +
			"    15  iload_0 [i]\n" +
			"    16  invokevirtual java.io.PrintStream.println(int) : void [34]\n" +
			"    19  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 2, line: 10]\n" +
			"        [pc: 12, line: 12]\n" +
			"        [pc: 19, line: 14]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 20] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test055() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		bar2();\n" +
			"	}\n" +
			"	static void bar2() {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) || (boom() || true)) {\n" +
			"		} else {\n" +
			"			System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void bar2();\n" +
			"     0  bipush 6\n" +
			"     2  istore_0 [i]\n" +
			"     3  iload_0 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpeq 13\n" +
			"     9  invokestatic X.boom() : boolean [26]\n" +
			"    12  pop\n" +
			"    13  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 3, line: 10]\n" +
			"        [pc: 13, line: 14]\n" +
			"      Local variable table:\n" +
			"        [pc: 3, pc: 14] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test056() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		bar2();\n" +
			"	}\n" +
			"	static void bar2() {\n" +
			"		int i = 6;\n" +
			"		if ((i == 6) || (boom() || true)) {\n" +
			"			System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void bar2();\n" +
			"     0  bipush 6\n" +
			"     2  istore_0 [i]\n" +
			"     3  iload_0 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpeq 13\n" +
			"     9  invokestatic X.boom() : boolean [26]\n" +
			"    12  pop\n" +
			"    13  getstatic java.lang.System.out : java.io.PrintStream [28]\n" +
			"    16  iload_0 [i]\n" +
			"    17  invokevirtual java.io.PrintStream.println(int) : void [34]\n" +
			"    20  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 3, line: 10]\n" +
			"        [pc: 13, line: 11]\n" +
			"        [pc: 20, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 3, pc: 21] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test057() throws Exception {
		String source =
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		foo3();\n" +
			"	}\n" +
			"	static void foo3() {\n" +
			"		int i = 5;\n" +
			"		if (false && (i == 6)) {\n" +
			"		   	System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  static void foo3();\n" +
			"    0  iconst_5\n" +
			"    1  istore_0 [i]\n" +
			"    2  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 2, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 3] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test058() throws Exception {
		String source =
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		foo3();\n" +
			"	}\n" +
			"	static void foo3() {\n" +
			"		int i = 5;\n" +
			"		if (false && (i == 6)) {\n" +
			"		} else {\n" +
			"		   	System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void foo3();\n" +
			"     0  iconst_5\n" +
			"     1  istore_0 [i]\n" +
			"     2  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
			"     5  iload_0 [i]\n" +
			"     6  invokevirtual java.io.PrintStream.println(int) : void [27]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 2, line: 9]\n" +
			"        [pc: 9, line: 11]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 10] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test059() throws Exception {
		String source =
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		bar3();\n" +
			"	}\n" +
			"	static void bar3() {\n" +
			"		int i = 6;\n" +
			"		if (true || (i == 6)) {\n" +
			"		} else {\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  static void bar3();\n" +
			"    0  bipush 6\n" +
			"    2  istore_0 [i]\n" +
			"    3  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 3, line: 11]\n" +
			"      Local variable table:\n" +
			"        [pc: 3, pc: 4] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test060() throws Exception {
		String source =
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		bar3();\n" +
			"	}\n" +
			"	static void bar3() {\n" +
			"		int i = 6;\n" +
			"		if (true || (i == 6)) {\n" +
			"		   System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void bar3();\n" +
			"     0  bipush 6\n" +
			"     2  istore_0 [i]\n" +
			"     3  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
			"     6  iload_0 [i]\n" +
			"     7  invokevirtual java.io.PrintStream.println(int) : void [27]\n" +
			"    10  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 3, line: 8]\n" +
			"        [pc: 10, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 3, pc: 11] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test061() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		foo4();\n" +
			"	}\n" +
			"	static void foo4() {\n" +
			"		int i = 5;\n" +
			"		if ((false && boom()) && (i == 6)) {   	\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  static void foo4();\n" +
			"    0  iconst_5\n" +
			"    1  istore_0 [i]\n" +
			"    2  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 2, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 3] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test062() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		foo4();\n" +
			"	}\n" +
			"	static void foo4() {\n" +
			"		int i = 5;\n" +
			"		if ((false && boom()) && (i == 6)) {\n" +
			"		} else {  	\n" +
			"		   System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void foo4();\n" +
			"     0  iconst_5\n" +
			"     1  istore_0 [i]\n" +
			"     2  getstatic java.lang.System.out : java.io.PrintStream [26]\n" +
			"     5  iload_0 [i]\n" +
			"     6  invokevirtual java.io.PrintStream.println(int) : void [32]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 2, line: 12]\n" +
			"        [pc: 9, line: 14]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 10] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test063() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		bar4();\n" +
			"	}\n" +
			"	static void bar4() {\n" +
			"		int i = 6;\n" +
			"		if ((true || boom()) || (i == 6)) {\n" +
			"		} else {\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  static void bar4();\n" +
			"    0  bipush 6\n" +
			"    2  istore_0 [i]\n" +
			"    3  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 3, line: 14]\n" +
			"      Local variable table:\n" +
			"        [pc: 3, pc: 4] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test064() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		bar4();\n" +
			"	}\n" +
			"	static void bar4() {\n" +
			"		int i = 6;\n" +
			"		if ((true || boom()) || (i == 6)) {\n" +
			"		   	System.out.println(i);\n" +
			"		   }\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void bar4();\n" +
			"     0  bipush 6\n" +
			"     2  istore_0 [i]\n" +
			"     3  getstatic java.lang.System.out : java.io.PrintStream [26]\n" +
			"     6  iload_0 [i]\n" +
			"     7  invokevirtual java.io.PrintStream.println(int) : void [32]\n" +
			"    10  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 3, line: 11]\n" +
			"        [pc: 10, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 3, pc: 11] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test065() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		foo5();\n" +
			"	}\n" +
			"	static void foo5() {\n" +
			"		int i = 5;\n" +
			"		if (((i == 6) && (boom() && false)) && false) {\n" +
			"			System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void foo5();\n" +
			"     0  iconst_5\n" +
			"     1  istore_0 [i]\n" +
			"     2  iload_0 [i]\n" +
			"     3  bipush 6\n" +
			"     5  if_icmpne 12\n" +
			"     8  invokestatic X.boom() : boolean [26]\n" +
			"    11  pop\n" +
			"    12  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 2, line: 10]\n" +
			"        [pc: 12, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 13] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test066() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		foo5();\n" +
			"	}\n" +
			"	static void foo5() {\n" +
			"		int i = 5;\n" +
			"		if (((i == 6) && (boom() && false)) && false) {\n" +
			"		} else {\n" +
			"			System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void foo5();\n" +
			"     0  iconst_5\n" +
			"     1  istore_0 [i]\n" +
			"     2  iload_0 [i]\n" +
			"     3  bipush 6\n" +
			"     5  if_icmpne 12\n" +
			"     8  invokestatic X.boom() : boolean [26]\n" +
			"    11  pop\n" +
			"    12  getstatic java.lang.System.out : java.io.PrintStream [28]\n" +
			"    15  iload_0 [i]\n" +
			"    16  invokevirtual java.io.PrintStream.println(int) : void [34]\n" +
			"    19  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 2, line: 10]\n" +
			"        [pc: 12, line: 12]\n" +
			"        [pc: 19, line: 14]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 20] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test067() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		bar5();\n" +
			"	}\n" +
			"	static void bar5() {\n" +
			"		int i = 6;\n" +
			"		if (((i == 6) || (boom() || true)) && true) {\n" +
			"		} else {\n" +
			"			System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void bar5();\n" +
			"     0  bipush 6\n" +
			"     2  istore_0 [i]\n" +
			"     3  iload_0 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpeq 13\n" +
			"     9  invokestatic X.boom() : boolean [26]\n" +
			"    12  pop\n" +
			"    13  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 3, line: 10]\n" +
			"        [pc: 13, line: 14]\n" +
			"      Local variable table:\n" +
			"        [pc: 3, pc: 14] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	public void test068() throws Exception {
		String source =
			"public class X {\n" +
			"	static boolean boom() { \n" +
			"		throw new NullPointerException();\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		bar5();\n" +
			"	}\n" +
			"	static void bar5() {\n" +
			"		int i = 6;\n" +
			"		if (((i == 6) || (boom() || true)) && true) {\n" +
			"			System.out.println(i);\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  static void bar5();\n" +
			"     0  bipush 6\n" +
			"     2  istore_0 [i]\n" +
			"     3  iload_0 [i]\n" +
			"     4  bipush 6\n" +
			"     6  if_icmpeq 13\n" +
			"     9  invokestatic X.boom() : boolean [26]\n" +
			"    12  pop\n" +
			"    13  getstatic java.lang.System.out : java.io.PrintStream [28]\n" +
			"    16  iload_0 [i]\n" +
			"    17  invokevirtual java.io.PrintStream.println(int) : void [34]\n" +
			"    20  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 3, line: 10]\n" +
			"        [pc: 13, line: 11]\n" +
			"        [pc: 20, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 3, pc: 21] local: i index: 0 type: int\n";
		checkClassFile("X", source, expectedOutput);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47886
	 */
	public void test069() throws Exception {
		String source =
			"public interface I {\n" +
			"}";
		String expectedOutput =
			"// Compiled from I.java (version 1.2 : 46.0, no super bit)\n" +
			"public abstract interface I {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 I\n" +
			"    constant #2 utf8: \"I\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"SourceFile\"\n" +
			"    constant #6 utf8: \"I.java\"\n" +
			"}";
		checkClassFile("I", source, expectedOutput);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test072() throws Exception {
		String source =
			"package p;\n" +
			"public abstract class X {\n" +
			"	public static final double CONST = Double.POSITIVE_INFINITY;\n" +
			"	X(X x) {}\n" +
			"	int foo() { return 0; }\n" +
			"	double foo2() { return 0; }\n" +
			"	byte foo3() { return 0; }\n" +
			"	char foo4() { return 0; }\n" +
			"	float foo5() { return 0; }\n" +
			"	long foo6() { return 0; }\n" +
			"	short foo7() { return 0; }\n" +
			"	Object foo8() { return null; }\n" +
			"	boolean foo9() { return false; }\n" +
			"	void foo10() {}\n" +
			"	native void foo11();\n" +
			"	abstract String foo12();\n" +
			"}";
		String expectedOutput =
			"package p;\n" +
			"public abstract class X {\n" +
			"  \n" +
			"  public static final double CONST = 1.0 / 0.0;\n" +
			"  \n" +
			"  X(p.X x) {\n" +
			"  }\n" +
			"  \n" +
			"  int foo() {\n" +
			"    return 0;\n" +
			"  }\n" +
			"  \n" +
			"  double foo2() {\n" +
			"    return 0;\n" +
			"  }\n" +
			"  \n" +
			"  byte foo3() {\n" +
			"    return 0;\n" +
			"  }\n" +
			"  \n" +
			"  char foo4() {\n" +
			"    return 0;\n" +
			"  }\n" +
			"  \n" +
			"  float foo5() {\n" +
			"    return 0;\n" +
			"  }\n" +
			"  \n" +
			"  long foo6() {\n" +
			"    return 0;\n" +
			"  }\n" +
			"  \n" +
			"  short foo7() {\n" +
			"    return 0;\n" +
			"  }\n" +
			"  \n" +
			"  java.lang.Object foo8() {\n" +
			"    return null;\n" +
			"  }\n" +
			"  \n" +
			"  boolean foo9() {\n" +
			"    return false;\n" +
			"  }\n" +
			"  \n" +
			"  void foo10() {\n" +
			"  }\n" +
			"  \n" +
			"  native void foo11();\n" +
			"  \n" +
			"  abstract java.lang.String foo12();\n" +
			"}";
		checkClassFile("p", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test073() throws Exception {
		String source =
			"public class X {\n" +
			"	public static final double CONST = Double.POSITIVE_INFINITY;\n" +
			"	X(X x) {}\n" +
			"}";
		String expectedOutput =
			"public class X {\n" +
			"  \n" +
			"  public static final double CONST = 1.0 / 0.0;\n" +
			"  \n" +
			"  X(X x) {\n" +
			"  }\n" +
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test074() throws Exception {
		String source =
			"package p;\n" +
			"public class X {\n" +
			"	public static final double CONST = Double.POSITIVE_INFINITY;\n" +
			"	X(X x) {}\n" +
			"}";
		String expectedOutput =
			"package p;\n" +
			"public class X {\n" +
			"  \n" +
			"  public static final double CONST = 1.0 / 0.0;\n" +
			"  \n" +
			"  X(X x) {\n" +
			"  }\n" +
			"}";
		checkClassFile("p", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY | ClassFileBytesDisassembler.COMPACT);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test075() throws Exception {
		String source =
			"package p;\n" +
			"public class X {\n" +
			"	public static final String CONST = \"\";\n" +
			"	X(X x) {}\n" +
			"}";
		String expectedOutput =
			"package p;\n" +
			"public class X {\n" +
			"  \n" +
			"  public static final String CONST = \"\";\n" +
			"  \n" +
			"  X(X x) {\n" +
			"  }\n" +
			"}";
		checkClassFile("p", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY | ClassFileBytesDisassembler.COMPACT);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111219
	public void test076() throws Exception {
		String source =
			"public class X {\n" +
			"	void foo() {\n" +
			"		try {\n" +
			"			System.out.println(\"Hello\");\n" +
			"		} catch(Exception e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"      Exception Table:\n" +
			"        [pc: 0, pc: 8] -> 11 when : Exception\n";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=34373
	public void test077() throws Exception {
		String source =
			"package p;\n" +
			"public class X {\n" +
			"	private static class A {}\n" +
			"}";
		String expectedOutput =
			"private static class p.X$A {\n";
		checkClassFile("p", "X", "X$A", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102473
	public void test078() throws Exception {
		String source =
			"public class X {\n" +
			"	X(int i, int j) {}\n" +
			"	void foo(String s, double d) {}\n" +
			"}";
		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methodInfos = classFileReader.getMethods();
		assertNotNull("No method infos", methodInfos);
		int length = methodInfos.length;
		assertTrue("At least one method", length > 0);
		for (int i = 0; i < length; i++) {
			char[][] argNames = methodInfos[i].getArgumentNames();
			assertNotNull("No names", argNames);
			assertEquals("Wrong size", 2, argNames.length);
			if (CharOperation.equals(methodInfos[i].getSelector(), "<init>".toCharArray())) {
				assertEquals("Wrong argument name", "i", new String(argNames[0]));
				assertEquals("Wrong argument name", "j", new String(argNames[1]));
			} else if (CharOperation.equals(methodInfos[i].getSelector(), "foo".toCharArray())) {
				assertEquals("Wrong argument name", "s", new String(argNames[0]));
				assertEquals("Wrong argument name", "d", new String(argNames[1]));
			}
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102473
	public void test079() throws Exception {
		String source =
			"public class X {\n" +
			"	X(int i, int j) {}\n" +
			"	void foo(String s, double d) throws Exception {\n" +
			"		try {\n" +
			"			System.out.println(s + d);\n" +
			"		} catch(Exception e) {\n" +
			"			e.printStackTrace();\n" +
			"			throw e;\n" +
			"		} finally {\n" +
			"			System.out.println(\"done\");\n" +
			"		}\n" +
			"	}\n" +
			"}";
		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methodInfos = classFileReader.getMethods();
		assertNotNull("No method infos", methodInfos);
		int length = methodInfos.length;
		assertTrue("At least one method", length > 0);
		for (int i = 0; i < length; i++) {
			char[][] argNames = methodInfos[i].getArgumentNames();
			assertNotNull("No names", argNames);
			assertEquals("Wrong size", 2, argNames.length);
			if (CharOperation.equals(methodInfos[i].getSelector(), "<init>".toCharArray())) {
				assertEquals("Wrong argument name", "i", new String(argNames[0]));
				assertEquals("Wrong argument name", "j", new String(argNames[1]));
			} else if (CharOperation.equals(methodInfos[i].getSelector(), "foo".toCharArray())) {
				assertEquals("Wrong argument name", "s", new String(argNames[0]));
				assertEquals("Wrong argument name", "d", new String(argNames[1]));
			}
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102473
	public void test080() throws Exception {
		String source =
			"public class X {\n" +
			"	X(int i, int j) {}\n" +
			"	void foo(String s, double d) throws Exception {\n" +
			"		try {\n" +
			"			int k = 0;\n" +
			"			System.out.println(s + d + k);\n" +
			"		} catch(Exception e) {\n" +
			"			e.printStackTrace();\n" +
			"			throw e;\n" +
			"		} finally {\n" +
			"			System.out.println(\"done\");\n" +
			"		}\n" +
			"	}\n" +
			"}";
		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methodInfos = classFileReader.getMethods();
		assertNotNull("No method infos", methodInfos);
		int length = methodInfos.length;
		assertTrue("At least one method", length > 0);
		for (int i = 0; i < length; i++) {
			char[][] argNames = methodInfos[i].getArgumentNames();
			assertNotNull("No names", argNames);
			assertEquals("Wrong size", 2, argNames.length);
			if (CharOperation.equals(methodInfos[i].getSelector(), "<init>".toCharArray())) {
				assertEquals("Wrong argument name", "i", new String(argNames[0]));
				assertEquals("Wrong argument name", "j", new String(argNames[1]));
			} else if (CharOperation.equals(methodInfos[i].getSelector(), "foo".toCharArray())) {
				assertEquals("Wrong argument name", "s", new String(argNames[0]));
				assertEquals("Wrong argument name", "d", new String(argNames[1]));
			}
		}
	}

}
