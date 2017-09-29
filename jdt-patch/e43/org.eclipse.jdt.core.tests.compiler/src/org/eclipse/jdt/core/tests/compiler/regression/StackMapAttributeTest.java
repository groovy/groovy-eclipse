/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - contribution for
 *								Bug 412203 - [compiler] Internal compiler error: java.lang.IllegalArgumentException: info cannot be null
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class StackMapAttributeTest extends AbstractRegressionTest {
	public StackMapAttributeTest(String name) {
		super(name);
	}

	public static Class testClass() {
		return StackMapAttributeTest.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug95521";
//		TESTS_NAMES = new String[] { "testBug412076_" };
//		TESTS_NUMBERS = new int[] { 53 };
//		TESTS_RANGE = new int[] { 23 -1,};
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_6);
	}
	public void test001() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	X() {}\n" +
					"	X(double i) {\n" +
					"		this(i > 0 ? null : new Object());\n" +
					"		try {\n" +
					"			foo(6, false);\n" +
					"		} catch(Exception e) {\n" +
					"			e.printStackTrace();\n" +
					"		}\n" +
					"	}\n" +
					"	X(Object o) {}\n" +
					"	int foo(int i, boolean b) {\n" +
					"		try {\n" +
					"			if (b) {\n" +
					"				return i;\n" +
					"			}\n" +
					"			return i + 1;\n" +
					"		} catch(Exception e) {\n" +
					"			return 5;\n" +
					"		}\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		new X().foo(2, false);\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"	}\n" +
					"}",
				},
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #14 (D)V\n" +
				"  // Stack: 5, Locals: 4\n" +
				"  X(double i);\n" +
				"     0  aload_0 [this]\n" +
				"     1  dload_1 [i]\n" +
				"     2  dconst_0\n" +
				"     3  dcmpl\n" +
				"     4  ifle 11\n" +
				"     7  aconst_null\n" +
				"     8  goto 18\n" +
				"    11  new java.lang.Object [3]\n" +
				"    14  dup\n" +
				"    15  invokespecial java.lang.Object() [8]\n" +
				"    18  invokespecial X(java.lang.Object) [15]\n" +
				"    21  aload_0 [this]\n" +
				"    22  bipush 6\n" +
				"    24  iconst_0\n" +
				"    25  invokevirtual X.foo(int, boolean) : int [18]\n" +
				"    28  pop\n" +
				"    29  goto 37\n" +
				"    32  astore_3 [e]\n" +
				"    33  aload_3 [e]\n" +
				"    34  invokevirtual java.lang.Exception.printStackTrace() : void [22]\n" +
				"    37  return\n" +
				"      Exception Table:\n" +
				"        [pc: 21, pc: 29] -> 32 when : java.lang.Exception\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 4]\n" +
				"        [pc: 21, line: 6]\n" +
				"        [pc: 29, line: 7]\n" +
				"        [pc: 33, line: 8]\n" +
				"        [pc: 37, line: 10]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 38] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 38] local: i index: 1 type: double\n" +
				"        [pc: 33, pc: 37] local: e index: 3 type: java.lang.Exception\n" +
				"      Stack map table: number of frames 4\n" +
				"        [pc: 11, same_locals_1_stack_item, stack: {uninitialized_this}]\n" +
				"        [pc: 18, full, stack: {uninitialized_this, java.lang.Object}, locals: {uninitialized_this, double}]\n" +
				"        [pc: 32, full, stack: {java.lang.Exception}, locals: {X, double}]\n" +
				"        [pc: 37, same]\n" +
				"  \n" +
				"  // Method descriptor #17 (Ljava/lang/Object;)V\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  X(java.lang.Object o);\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [8]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 11]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 5] local: o index: 1 type: java.lang.Object\n" +
				"  \n" +
				"  // Method descriptor #21 (IZ)I\n" +
				"  // Stack: 2, Locals: 4\n" +
				"  int foo(int i, boolean b);\n" +
				"     0  iload_2 [b]\n" +
				"     1  ifeq 6\n" +
				"     4  iload_1 [i]\n" +
				"     5  ireturn\n" +
				"     6  iload_1 [i]\n" +
				"     7  iconst_1\n" +
				"     8  iadd\n" +
				"     9  ireturn\n" +
				"    10  astore_3 [e]\n" +
				"    11  iconst_5\n" +
				"    12  ireturn\n" +
				"      Exception Table:\n" +
				"        [pc: 0, pc: 5] -> 10 when : java.lang.Exception\n" +
				"        [pc: 6, pc: 9] -> 10 when : java.lang.Exception\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 14]\n" +
				"        [pc: 4, line: 15]\n" +
				"        [pc: 6, line: 17]\n" +
				"        [pc: 10, line: 18]\n" +
				"        [pc: 11, line: 19]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 13] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 13] local: i index: 1 type: int\n" +
				"        [pc: 0, pc: 13] local: b index: 2 type: boolean\n" +
				"        [pc: 11, pc: 13] local: e index: 3 type: java.lang.Exception\n" +
				"      Stack map table: number of frames 2\n" +
				"        [pc: 6, same]\n" +
				"        [pc: 10, same_locals_1_stack_item, stack: {java.lang.Exception}]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	public void test002() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void foo(double d, boolean b) {\n" +
					"		double i;\n" +
					"		try {\n" +
					"			i = 0;\n" +
					"			i++;\n" +
					"			int j = (int) (i - 1);\n" +
					"			if (b) {\n" +
					"				double d1 = 0;\n" +
					"				if (!b) {\n" +
					"					d1 = 0;\n" +
					"				}\n" +
					"				double d2 = d + d1;\n" +
					"			}\n" +
					"			bar(j);\n" +
					"		} catch(NullPointerException e) {\n" +
					"			i = 2;\n" +
					"		} finally {\n" +
					"			i = 1;\n" +
					"		}\n" +
					"		long j = (long) (i + 1);\n" +
					"		int k = (int) j;\n" +
					"		k += j;\n" +
					"	}\n" +
					"	public static void bar(int i) {}\n" +
					"	public static void main(String[] args) {\n" +
					"		foo(0, true);\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"	}\n" +
					"}",
				},
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #15 (DZ)V\n" +
				"  // Stack: 4, Locals: 11\n" +
				"  public static void foo(double d, boolean b);\n" +
				"     0  dconst_0\n" +
				"     1  dstore_3 [i]\n" +
				"     2  dload_3 [i]\n" +
				"     3  dconst_1\n" +
				"     4  dadd\n" +
				"     5  dstore_3 [i]\n" +
				"     6  dload_3 [i]\n" +
				"     7  dconst_1\n" +
				"     8  dsub\n" +
				"     9  d2i\n" +
				"    10  istore 5 [j]\n" +
				"    12  iload_2 [b]\n" +
				"    13  ifeq 32\n" +
				"    16  dconst_0\n" +
				"    17  dstore 6 [d1]\n" +
				"    19  iload_2 [b]\n" +
				"    20  ifne 26\n" +
				"    23  dconst_0\n" +
				"    24  dstore 6 [d1]\n" +
				"    26  dload_0 [d]\n" +
				"    27  dload 6 [d1]\n" +
				"    29  dadd\n" +
				"    30  dstore 8\n" +
				"    32  iload 5 [j]\n" +
				"    34  invokestatic X.bar(int) : void [16]\n" +
				"    37  goto 58\n" +
				"    40  astore 5 [e]\n" +
				"    42  ldc2_w <Double 2.0> [20]\n" +
				"    45  dstore_3 [i]\n" +
				"    46  dconst_1\n" +
				"    47  dstore_3 [i]\n" +
				"    48  goto 60\n" +
				"    51  astore 10\n" +
				"    53  dconst_1\n" +
				"    54  dstore_3 [i]\n" +
				"    55  aload 10\n" +
				"    57  athrow\n" +
				"    58  dconst_1\n" +
				"    59  dstore_3 [i]\n" +
				"    60  dload_3 [i]\n" +
				"    61  dconst_1\n" +
				"    62  dadd\n" +
				"    63  d2l\n" +
				"    64  lstore 5 [j]\n" +
				"    66  lload 5 [j]\n" +
				"    68  l2i\n" +
				"    69  istore 7 [k]\n" +
				"    71  iload 7 [k]\n" +
				"    73  i2l\n" +
				"    74  lload 5 [j]\n" +
				"    76  ladd\n" +
				"    77  l2i\n" +
				"    78  istore 7 [k]\n" +
				"    80  return\n" +
				"      Exception Table:\n" +
				"        [pc: 0, pc: 37] -> 40 when : java.lang.NullPointerException\n" +
				"        [pc: 0, pc: 46] -> 51 when : any\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n" +
				"        [pc: 2, line: 6]\n" +
				"        [pc: 6, line: 7]\n" +
				"        [pc: 12, line: 8]\n" +
				"        [pc: 16, line: 9]\n" +
				"        [pc: 19, line: 10]\n" +
				"        [pc: 23, line: 11]\n" +
				"        [pc: 26, line: 13]\n" +
				"        [pc: 32, line: 15]\n" +
				"        [pc: 37, line: 16]\n" +
				"        [pc: 42, line: 17]\n" +
				"        [pc: 46, line: 19]\n" +
				"        [pc: 51, line: 18]\n" +
				"        [pc: 53, line: 19]\n" +
				"        [pc: 55, line: 20]\n" +
				"        [pc: 58, line: 19]\n" +
				"        [pc: 60, line: 21]\n" +
				"        [pc: 66, line: 22]\n" +
				"        [pc: 71, line: 23]\n" +
				"        [pc: 80, line: 24]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 81] local: d index: 0 type: double\n" +
				"        [pc: 0, pc: 81] local: b index: 2 type: boolean\n" +
				"        [pc: 2, pc: 40] local: i index: 3 type: double\n" +
				"        [pc: 46, pc: 51] local: i index: 3 type: double\n" +
				"        [pc: 55, pc: 81] local: i index: 3 type: double\n" +
				"        [pc: 12, pc: 37] local: j index: 5 type: int\n" +
				"        [pc: 19, pc: 32] local: d1 index: 6 type: double\n" +
				"        [pc: 42, pc: 46] local: e index: 5 type: java.lang.NullPointerException\n" +
				"        [pc: 66, pc: 81] local: j index: 5 type: long\n" +
				"        [pc: 71, pc: 81] local: k index: 7 type: int\n" +
				"      Stack map table: number of frames 6\n" +
				"        [pc: 26, append: {double, int, double}]\n" +
				"        [pc: 32, chop 1 local(s)]\n" +
				"        [pc: 40, full, stack: {java.lang.NullPointerException}, locals: {double, int}]\n" +
				"        [pc: 51, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" +
				"        [pc: 58, append: {double}]\n" +
				"        [pc: 60, same]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	public void test003() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void foo(boolean b) {\n" +
					"		int i = 0;\n" +
					"		try {\n" +
					"			System.out.println(\"FINALLY\");\n" +
					"			i++;\n" +
					"			int j = i -1;\n" +
					"			bar(j);\n" +
					"		} catch(NullPointerException e) {\n" +
					"			e.printStackTrace();\n" +
					"		} finally {\n" +
					"			System.out.println(\"FINALLY\");\n" +
					"		}\n" +
					"	}\n" +
					"	public static void bar(int i) {}\n" +
					"	\n" +
					"	public static void main(String[] args) {\n" +
					"		foo(true);\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"	}\n" +
					"}",
				},
				"FINALLY\n" +
				"FINALLY\n" +
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #15 (Z)V\n" +
				"  // Stack: 2, Locals: 4\n" +
				"  public static void foo(boolean b);\n" +
				"     0  iconst_0\n" +
				"     1  istore_1 [i]\n" +
				"     2  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
				"     5  ldc <String \"FINALLY\"> [22]\n" +
				"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
				"    10  iinc 1 1 [i]\n" +
				"    13  iload_1 [i]\n" +
				"    14  iconst_1\n" +
				"    15  isub\n" +
				"    16  istore_2 [j]\n" +
				"    17  iload_2 [j]\n" +
				"    18  invokestatic X.bar(int) : void [30]\n" +
				"    21  goto 51\n" +
				"    24  astore_2 [e]\n" +
				"    25  aload_2 [e]\n" +
				"    26  invokevirtual java.lang.NullPointerException.printStackTrace() : void [34]\n" +
				"    29  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
				"    32  ldc <String \"FINALLY\"> [22]\n" +
				"    34  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
				"    37  goto 59\n" +
				"    40  astore_3\n" +
				"    41  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
				"    44  ldc <String \"FINALLY\"> [22]\n" +
				"    46  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
				"    49  aload_3\n" +
				"    50  athrow\n" +
				"    51  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
				"    54  ldc <String \"FINALLY\"> [22]\n" +
				"    56  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
				"    59  return\n" +
				"      Exception Table:\n" +
				"        [pc: 2, pc: 21] -> 24 when : java.lang.NullPointerException\n" +
				"        [pc: 2, pc: 29] -> 40 when : any\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 3]\n" +
				"        [pc: 2, line: 5]\n" +
				"        [pc: 10, line: 6]\n" +
				"        [pc: 13, line: 7]\n" +
				"        [pc: 17, line: 8]\n" +
				"        [pc: 21, line: 9]\n" +
				"        [pc: 25, line: 10]\n" +
				"        [pc: 29, line: 12]\n" +
				"        [pc: 40, line: 11]\n" +
				"        [pc: 41, line: 12]\n" +
				"        [pc: 49, line: 13]\n" +
				"        [pc: 51, line: 12]\n" +
				"        [pc: 59, line: 14]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 60] local: b index: 0 type: boolean\n" +
				"        [pc: 2, pc: 60] local: i index: 1 type: int\n" +
				"        [pc: 17, pc: 21] local: j index: 2 type: int\n" +
				"        [pc: 25, pc: 29] local: e index: 2 type: java.lang.NullPointerException\n" +
				"      Stack map table: number of frames 4\n" +
				"        [pc: 24, full, stack: {java.lang.NullPointerException}, locals: {int, int}]\n" +
				"        [pc: 40, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" +
				"        [pc: 51, same]\n" +
				"        [pc: 59, same]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test004() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void foo(boolean b) {\n" +
					"		C c;\n" +
					"		if (b) {\n" +
					"			c = new C1();\n" +
					"		} else {\n" +
					"			c = new C2();\n" +
					"		}\n" +
					"		System.out.println();\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		foo(true);\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"	}\n" +
					"}\n" +
					"class C {\n" +
					"	void foo() {}\n" +
					"}\n" +
					"class C1 extends C {\n" +
					"}\n" +
					"class C2 extends C {\n" +
					"}",
				},
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #15 (Z)V\n" +
				"  // Stack: 2, Locals: 2\n" +
				"  public static void foo(boolean b);\n" +
				"     0  iload_0 [b]\n" +
				"     1  ifeq 15\n" +
				"     4  new C1 [16]\n" +
				"     7  dup\n" +
				"     8  invokespecial C1() [18]\n" +
				"    11  astore_1 [c]\n" +
				"    12  goto 23\n" +
				"    15  new C2 [19]\n" +
				"    18  dup\n" +
				"    19  invokespecial C2() [21]\n" +
				"    22  astore_1 [c]\n" +
				"    23  getstatic java.lang.System.out : java.io.PrintStream [22]\n" +
				"    26  invokevirtual java.io.PrintStream.println() : void [28]\n" +
				"    29  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 4]\n" +
				"        [pc: 4, line: 5]\n" +
				"        [pc: 12, line: 6]\n" +
				"        [pc: 15, line: 7]\n" +
				"        [pc: 23, line: 9]\n" +
				"        [pc: 29, line: 10]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 30] local: b index: 0 type: boolean\n" +
				"        [pc: 12, pc: 15] local: c index: 1 type: C\n" +
				"        [pc: 23, pc: 30] local: c index: 1 type: C\n" +
				"      Stack map table: number of frames 2\n" +
				"        [pc: 15, same]\n" +
				"        [pc: 23, append: {C}]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	public void test005() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"  public static void main(String args[]) {\n" +
					"    int i = 0, j, k, l;\n" +
					"    boolean b;\n" +
					"    if (i == 0 && (j = ++i) > 0)\n" +
					"      i += j;\n" +
					"    while (true) {\n" +
					"      k = 3;\n" +
					"      break;\n" +
					"    } \n" +
					"    i -= k;\n" +
					"    b = false && (i = l) > 0;\n" +
					"    if (i > 0)\n" +
					"      l = i;\n" +
					"    else\n" +
					"      l = k;\n" +
					"    j = l;\n" +
					"    if (i != -1 || j != 3 || k != 3 || l != 3)\n" +
					"      System.out.println(\"FAILED\");\n" +
					"    System.out.println(\"SUCCESS\");\n" +
					"  }\n" +
					"}",
				},
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
				"  // Stack: 2, Locals: 6\n" +
				"  public static void main(java.lang.String[] args);\n" +
				"     0  iconst_0\n" +
				"     1  istore_1 [i]\n" +
				"     2  iload_1 [i]\n" +
				"     3  ifne 19\n" +
				"     6  iinc 1 1 [i]\n" +
				"     9  iload_1 [i]\n" +
				"    10  dup\n" +
				"    11  istore_2 [j]\n" +
				"    12  ifle 19\n" +
				"    15  iload_1 [i]\n" +
				"    16  iload_2 [j]\n" +
				"    17  iadd\n" +
				"    18  istore_1 [i]\n" +
				"    19  iconst_3\n" +
				"    20  istore_3 [k]\n" +
				"    21  iload_1 [i]\n" +
				"    22  iload_3 [k]\n" +
				"    23  isub\n" +
				"    24  istore_1 [i]\n" +
				"    25  iconst_0\n" +
				"    26  istore 5 [b]\n" +
				"    28  iload_1 [i]\n" +
				"    29  ifle 38\n" +
				"    32  iload_1 [i]\n" +
				"    33  istore 4 [l]\n" +
				"    35  goto 41\n" +
				"    38  iload_3 [k]\n" +
				"    39  istore 4 [l]\n" +
				"    41  iload 4 [l]\n" +
				"    43  istore_2 [j]\n" +
				"    44  iload_1 [i]\n" +
				"    45  iconst_m1\n" +
				"    46  if_icmpne 65\n" +
				"    49  iload_2 [j]\n" +
				"    50  iconst_3\n" +
				"    51  if_icmpne 65\n" +
				"    54  iload_3 [k]\n" +
				"    55  iconst_3\n" +
				"    56  if_icmpne 65\n" +
				"    59  iload 4 [l]\n" +
				"    61  iconst_3\n" +
				"    62  if_icmpeq 73\n" +
				"    65  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
				"    68  ldc <String \"FAILED\"> [22]\n" +
				"    70  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
				"    73  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
				"    76  ldc <String \"SUCCESS\"> [30]\n" +
				"    78  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
				"    81  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 3]\n" +
				"        [pc: 2, line: 5]\n" +
				"        [pc: 15, line: 6]\n" +
				"        [pc: 19, line: 8]\n" +
				"        [pc: 21, line: 9]\n" +
				"        [pc: 22, line: 11]\n" +
				"        [pc: 25, line: 12]\n" +
				"        [pc: 28, line: 13]\n" +
				"        [pc: 32, line: 14]\n" +
				"        [pc: 38, line: 16]\n" +
				"        [pc: 41, line: 17]\n" +
				"        [pc: 44, line: 18]\n" +
				"        [pc: 65, line: 19]\n" +
				"        [pc: 73, line: 20]\n" +
				"        [pc: 81, line: 21]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 82] local: args index: 0 type: java.lang.String[]\n" +
				"        [pc: 2, pc: 82] local: i index: 1 type: int\n" +
				"        [pc: 12, pc: 19] local: j index: 2 type: int\n" +
				"        [pc: 44, pc: 82] local: j index: 2 type: int\n" +
				"        [pc: 21, pc: 82] local: k index: 3 type: int\n" +
				"        [pc: 35, pc: 38] local: l index: 4 type: int\n" +
				"        [pc: 41, pc: 82] local: l index: 4 type: int\n" +
				"        [pc: 28, pc: 82] local: b index: 5 type: boolean\n" +
				"      Stack map table: number of frames 5\n" +
				"        [pc: 19, append: {int}]\n" +
				"        [pc: 38, full, stack: {}, locals: {java.lang.String[], int, _, int, _, int}]\n" +
				"        [pc: 41, full, stack: {}, locals: {java.lang.String[], int, _, int, int, int}]\n" +
				"        [pc: 65, full, stack: {}, locals: {java.lang.String[], int, int, int, int, int}]\n" +
				"        [pc: 73, same]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test006() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	void foo (int n) {\n" +
					"   	synchronized (this) {\n" +
					"       	switch (n) {\n" +
					"       		case 1:\n" +
					"       		throw new NullPointerException();\n" +
					"			}\n" +
					"		}\n" +
					"	}\n" +
					"    public static void main(String args[]) {\n" +
					"    	try {\n" +
					"	    	new X().foo(1);\n" +
					"    	} catch(Exception e) {\n" +
					"	        System.out.println(\"SUCCESS\"); \n" +
					"    	}\n" +
					"    } \n" +
					"}",
				},
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #15 (I)V\n" +
				"  // Stack: 2, Locals: 3\n" +
				"  void foo(int n);\n" +
				"     0  aload_0 [this]\n" +
				"     1  dup\n" +
				"     2  astore_2\n" +
				"     3  monitorenter\n" +
				"     4  iload_1 [n]\n" +
				"     5  tableswitch default: 32\n" +
				"          case 1: 24\n" +
				"    24  new java.lang.NullPointerException [16]\n" +
				"    27  dup\n" +
				"    28  invokespecial java.lang.NullPointerException() [18]\n" +
				"    31  athrow\n" +
				"    32  aload_2\n" +
				"    33  monitorexit\n" +
				"    34  goto 40\n" +
				"    37  aload_2\n" +
				"    38  monitorexit\n" +
				"    39  athrow\n" +
				"    40  return\n" +
				"      Exception Table:\n" +
				"        [pc: 4, pc: 34] -> 37 when : any\n" +
				"        [pc: 37, pc: 39] -> 37 when : any\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 3]\n" +
				"        [pc: 4, line: 4]\n" +
				"        [pc: 24, line: 6]\n" +
				"        [pc: 32, line: 3]\n" +
				"        [pc: 40, line: 9]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 41] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 41] local: n index: 1 type: int\n" +
				"      Stack map table: number of frames 4\n" +
				"        [pc: 24, append: {X}]\n" +
				"        [pc: 32, same]\n" +
				"        [pc: 37, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" +
				"        [pc: 40, chop 1 local(s)]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test007() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	StringBuffer foo2(boolean b) {\n" +
					"		System.out.println(\"foo2\");\n" +
					"		return new StringBuffer(b ? \"true\" : \"false\");\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"	}\n" +
					"}",
				},
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #15 (Z)Ljava/lang/StringBuffer;\n" +
				"  // Stack: 3, Locals: 2\n" +
				"  java.lang.StringBuffer foo2(boolean b);\n" +
				"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
				"     3  ldc <String \"foo2\"> [22]\n" +
				"     5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [23]\n" +
				"     8  new java.lang.StringBuffer [29]\n" +
				"    11  dup\n" +
				"    12  iload_1 [b]\n" +
				"    13  ifeq 21\n" +
				"    16  ldc <String \"true\"> [31]\n" +
				"    18  goto 23\n" +
				"    21  ldc <String \"false\"> [33]\n" +
				"    23  invokespecial java.lang.StringBuffer(java.lang.String) [35]\n" +
				"    26  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 3]\n" +
				"        [pc: 8, line: 4]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 27] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 27] local: b index: 1 type: boolean\n" +
				"      Stack map table: number of frames 2\n" +
				"        [pc: 21, full, stack: {uninitialized(8), uninitialized(8)}, locals: {X, int}]\n" +
				"        [pc: 23, full, stack: {uninitialized(8), uninitialized(8), java.lang.String}, locals: {X, int}]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141252
	public void test008() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		int foo = 0;\n" +
					"		String bar = \"zero\";\n" +
					"		System.out.println((foo != 0) ? foo : bar);\n" +
					"	}\n" +
					"	<T extends Comparable<?>> void foo(T foo) {\n" +
					"		T bar = null;\n" +
					"		System.out.println((foo != null) ? foo : bar);\n" +
					"	}	\n" +
					"}\n",
				},
				"zero");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
					"  // Stack: 2, Locals: 3\n" +
					"  public static void main(java.lang.String[] args);\n" +
					"     0  iconst_0\n" +
					"     1  istore_1 [foo]\n" +
					"     2  ldc <String \"zero\"> [16]\n" +
					"     4  astore_2 [bar]\n" +
					"     5  getstatic java.lang.System.out : java.io.PrintStream [18]\n" +
					"     8  iload_1 [foo]\n" +
					"     9  ifeq 19\n" +
					"    12  iload_1 [foo]\n" +
					"    13  invokestatic java.lang.Integer.valueOf(int) : java.lang.Integer [24]\n" +
					"    16  goto 20\n" +
					"    19  aload_2 [bar]\n" +
					"    20  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [30]\n" +
					"    23  return\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 3]\n" +
					"        [pc: 2, line: 4]\n" +
					"        [pc: 5, line: 5]\n" +
					"        [pc: 23, line: 6]\n" +
					"      Local variable table:\n" +
					"        [pc: 0, pc: 24] local: args index: 0 type: java.lang.String[]\n" +
					"        [pc: 2, pc: 24] local: foo index: 1 type: int\n" +
					"        [pc: 5, pc: 24] local: bar index: 2 type: java.lang.String\n" +
					"      Stack map table: number of frames 2\n" +
					"        [pc: 19, full, stack: {java.io.PrintStream}, locals: {java.lang.String[], int, java.lang.String}]\n" +
					"        [pc: 20, full, stack: {java.io.PrintStream, java.lang.Comparable}, locals: {java.lang.String[], int, java.lang.String}]\n" +
					"  \n" +
					"  // Method descriptor #48 (Ljava/lang/Comparable;)V\n" +
					"  // Signature: <T::Ljava/lang/Comparable<*>;>(TT;)V\n" +
					"  // Stack: 2, Locals: 3\n" +
					"  void foo(java.lang.Comparable foo);\n" +
					"     0  aconst_null\n" +
					"     1  astore_2 [bar]\n" +
					"     2  getstatic java.lang.System.out : java.io.PrintStream [18]\n" +
					"     5  aload_1 [foo]\n" +
					"     6  ifnull 13\n" +
					"     9  aload_1 [foo]\n" +
					"    10  goto 14\n" +
					"    13  aload_2 [bar]\n" +
					"    14  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [30]\n" +
					"    17  return\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 8]\n" +
					"        [pc: 2, line: 9]\n" +
					"        [pc: 17, line: 10]\n" +
					"      Local variable table:\n" +
					"        [pc: 0, pc: 18] local: this index: 0 type: X\n" +
					"        [pc: 0, pc: 18] local: foo index: 1 type: java.lang.Comparable\n" +
					"        [pc: 2, pc: 18] local: bar index: 2 type: java.lang.Comparable\n" +
					"      Local variable type table:\n" +
					"        [pc: 0, pc: 18] local: foo index: 1 type: T\n" +
					"        [pc: 2, pc: 18] local: bar index: 2 type: T\n" +
					"      Stack map table: number of frames 2\n" +
					"        [pc: 13, full, stack: {java.io.PrintStream}, locals: {X, java.lang.Comparable, java.lang.Comparable}]\n" +
					"        [pc: 14, full, stack: {java.io.PrintStream, java.lang.Comparable}, locals: {X, java.lang.Comparable, java.lang.Comparable}]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test009() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	String s;\n" +
					"	X() {\n" +
					"        int i = 0;\n" +
					"        if (s == null) {\n" +
					"        	System.out.print(\"PASSED\");\n" +
					"        } else {\n" +
					"        	System.out.print(\"FAILED\");\n" +
					"        }\n" +
					"        System.out.print(\"DONE\" + i);\n" +
					"	}\n" +
					"    public static void main(String argv[]) {\n" +
					"    	new X();\n" +
					"    }\n" +
					"}",
				},
				"PASSEDDONE0");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #8 ()V\n" +
				"  // Stack: 4, Locals: 2\n" +
				"  X();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokespecial java.lang.Object() [10]\n" +
				"     4  iconst_0\n" +
				"     5  istore_1 [i]\n" +
				"     6  aload_0 [this]\n" +
				"     7  getfield X.s : java.lang.String [12]\n" +
				"    10  ifnonnull 24\n" +
				"    13  getstatic java.lang.System.out : java.io.PrintStream [14]\n" +
				"    16  ldc <String \"PASSED\"> [20]\n" +
				"    18  invokevirtual java.io.PrintStream.print(java.lang.String) : void [22]\n" +
				"    21  goto 32\n" +
				"    24  getstatic java.lang.System.out : java.io.PrintStream [14]\n" +
				"    27  ldc <String \"FAILED\"> [28]\n" +
				"    29  invokevirtual java.io.PrintStream.print(java.lang.String) : void [22]\n" +
				"    32  getstatic java.lang.System.out : java.io.PrintStream [14]\n" +
				"    35  new java.lang.StringBuilder [30]\n" +
				"    38  dup\n" +
				"    39  ldc <String \"DONE\"> [32]\n" +
				"    41  invokespecial java.lang.StringBuilder(java.lang.String) [34]\n" +
				"    44  iload_1 [i]\n" +
				"    45  invokevirtual java.lang.StringBuilder.append(int) : java.lang.StringBuilder [36]\n" +
				"    48  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [40]\n" +
				"    51  invokevirtual java.io.PrintStream.print(java.lang.String) : void [22]\n" +
				"    54  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 3]\n" +
				"        [pc: 4, line: 4]\n" +
				"        [pc: 6, line: 5]\n" +
				"        [pc: 13, line: 6]\n" +
				"        [pc: 21, line: 7]\n" +
				"        [pc: 24, line: 8]\n" +
				"        [pc: 32, line: 10]\n" +
				"        [pc: 54, line: 11]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 55] local: this index: 0 type: X\n" +
				"        [pc: 6, pc: 55] local: i index: 1 type: int\n" +
				"      Stack map table: number of frames 2\n" +
				"        [pc: 24, full, stack: {}, locals: {X, int}]\n" +
				"        [pc: 32, same]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test010() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		int[] tab = new int[0];\n" +
					"		Object o = tab;\n" +
					"		Object o1 = null;\n" +
					"		if (tab.length == 0) {\n" +
					"			System.out.println(tab.getClass());\n" +
					"		}\n" +
					"		o1 = tab.clone();\n" +
					"	}\n" +
					"}",
				},
				"class [I");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
				"  // Stack: 2, Locals: 4\n" +
				"  public static void main(java.lang.String[] args);\n" +
				"     0  iconst_0\n" +
				"     1  newarray int [10]\n" +
				"     3  astore_1 [tab]\n" +
				"     4  aload_1 [tab]\n" +
				"     5  astore_2 [o]\n" +
				"     6  aconst_null\n" +
				"     7  astore_3 [o1]\n" +
				"     8  aload_1 [tab]\n" +
				"     9  arraylength\n" +
				"    10  ifne 23\n" +
				"    13  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
				"    16  aload_1 [tab]\n" +
				"    17  invokevirtual java.lang.Object.getClass() : java.lang.Class [22]\n" +
				"    20  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [26]\n" +
				"    23  aload_1 [tab]\n" +
				"    24  invokevirtual int[].clone() : java.lang.Object [32]\n" +
				"    27  astore_3 [o1]\n" +
				"    28  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 3]\n" +
				"        [pc: 4, line: 4]\n" +
				"        [pc: 6, line: 5]\n" +
				"        [pc: 8, line: 6]\n" +
				"        [pc: 13, line: 7]\n" +
				"        [pc: 23, line: 9]\n" +
				"        [pc: 28, line: 10]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 29] local: args index: 0 type: java.lang.String[]\n" +
				"        [pc: 4, pc: 29] local: tab index: 1 type: int[]\n" +
				"        [pc: 6, pc: 29] local: o index: 2 type: java.lang.Object\n" +
				"        [pc: 8, pc: 29] local: o1 index: 3 type: java.lang.Object\n" +
				"      Stack map table: number of frames 1\n" +
				"        [pc: 23, append: {int[], java.lang.Object, java.lang.Object}]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test011() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Object o;\n" +
					"\n" +
					"	public static void main(String[] args) {\n" +
					"		X x;\n" +
					"		for (int i = 0; i < 10; i++) {\n" +
					"			if (i < 90) {\n" +
					"				x = new X();\n" +
					"				if (i > 4) {\n" +
					"					x.o = new Object();\n" +
					"				} else {\n" +
					"					x.o = \"0\";\n" +
					"				}\n" +
					"				switch (i) {\n" +
					"					case 0:\n" +
					"						if (x.o instanceof String) {\n" +
					"							System.out.print(\"1\");\n" +
					"						}\n" +
					"						break;\n" +
					"					default: {\n" +
					"						Object diff = x.o;\n" +
					"						if (diff != null) {\n" +
					"							System.out.print(\"2\");\n" +
					"						}\n" +
					"					}\n" +
					"				}\n" +
					"			}\n" +
					"		}\n" +
					"	}\n" +
					"}",
				},
				"1222222222");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
				"  // Stack: 3, Locals: 4\n" +
				"  public static void main(java.lang.String[] args);\n" +
				"      0  iconst_0\n" +
				"      1  istore_2 [i]\n" +
				"      2  goto 105\n" +
				"      5  iload_2 [i]\n" +
				"      6  bipush 90\n" +
				"      8  if_icmpge 102\n" +
				"     11  new X [1]\n" +
				"     14  dup\n" +
				"     15  invokespecial X() [18]\n" +
				"     18  astore_1 [x]\n" +
				"     19  iload_2 [i]\n" +
				"     20  iconst_4\n" +
				"     21  if_icmple 38\n" +
				"     24  aload_1 [x]\n" +
				"     25  new java.lang.Object [3]\n" +
				"     28  dup\n" +
				"     29  invokespecial java.lang.Object() [10]\n" +
				"     32  putfield X.o : java.lang.Object [19]\n" +
				"     35  goto 44\n" +
				"     38  aload_1 [x]\n" +
				"     39  ldc <String \"0\"> [21]\n" +
				"     41  putfield X.o : java.lang.Object [19]\n" +
				"     44  iload_2 [i]\n" +
				"     45  tableswitch default: 85\n" +
				"          case 0: 64\n" +
				"     64  aload_1 [x]\n" +
				"     65  getfield X.o : java.lang.Object [19]\n" +
				"     68  instanceof java.lang.String [23]\n" +
				"     71  ifeq 102\n" +
				"     74  getstatic java.lang.System.out : java.io.PrintStream [25]\n" +
				"     77  ldc <String \"1\"> [31]\n" +
				"     79  invokevirtual java.io.PrintStream.print(java.lang.String) : void [33]\n" +
				"     82  goto 102\n" +
				"     85  aload_1 [x]\n" +
				"     86  getfield X.o : java.lang.Object [19]\n" +
				"     89  astore_3 [diff]\n" +
				"     90  aload_3 [diff]\n" +
				"     91  ifnull 102\n" +
				"     94  getstatic java.lang.System.out : java.io.PrintStream [25]\n" +
				"     97  ldc <String \"2\"> [39]\n" +
				"     99  invokevirtual java.io.PrintStream.print(java.lang.String) : void [33]\n" +
				"    102  iinc 2 1 [i]\n" +
				"    105  iload_2 [i]\n" +
				"    106  bipush 10\n" +
				"    108  if_icmplt 5\n" +
				"    111  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"        [pc: 5, line: 7]\n" +
				"        [pc: 11, line: 8]\n" +
				"        [pc: 19, line: 9]\n" +
				"        [pc: 24, line: 10]\n" +
				"        [pc: 35, line: 11]\n" +
				"        [pc: 38, line: 12]\n" +
				"        [pc: 44, line: 14]\n" +
				"        [pc: 64, line: 16]\n" +
				"        [pc: 74, line: 17]\n" +
				"        [pc: 82, line: 19]\n" +
				"        [pc: 85, line: 21]\n" +
				"        [pc: 90, line: 22]\n" +
				"        [pc: 94, line: 23]\n" +
				"        [pc: 102, line: 6]\n" +
				"        [pc: 111, line: 29]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 112] local: args index: 0 type: java.lang.String[]\n" +
				"        [pc: 19, pc: 102] local: x index: 1 type: X\n" +
				"        [pc: 2, pc: 111] local: i index: 2 type: int\n" +
				"        [pc: 90, pc: 102] local: diff index: 3 type: java.lang.Object\n" +
				"      Stack map table: number of frames 7\n" +
				"        [pc: 5, full, stack: {}, locals: {java.lang.String[], _, int}]\n" +
				"        [pc: 38, full, stack: {}, locals: {java.lang.String[], X, int}]\n" +
				"        [pc: 44, same]\n" +
				"        [pc: 64, same]\n" +
				"        [pc: 85, same]\n" +
				"        [pc: 102, full, stack: {}, locals: {java.lang.String[], _, int}]\n" +
				"        [pc: 105, same]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test012() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		X x;\n" +
					"		Object o;\n" +
					"		for (int i = 0; i < 10; i++) {\n" +
					"			if (i < 90) {\n" +
					"				x = new X();\n" +
					"				if (i > 4) {\n" +
					"					o = new Object();\n" +
					"				} else {\n" +
					"					o = null;\n" +
					"				}\n" +
					"				switch (i) {\n" +
					"					case 0:\n" +
					"						if (o instanceof String) {\n" +
					"							System.out.print(\"1\");\n" +
					"							return;\n" +
					"						} else {\n" +
					"							break;\n" +
					"						}\n" +
					"					default: {\n" +
					"						Object diff = o;\n" +
					"						if (diff != null) {\n" +
					"							System.out.print(\"2\");\n" +
					"						}\n" +
					"						break;\n" +
					"					}\n" +
					"				}\n" +
					"				System.out.print(\"3\");\n" +
					"			}\n" +
					"		}\n" +
					"	}\n" +
					"}",
				},
				"333332323232323");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
				"  // Stack: 2, Locals: 5\n" +
				"  public static void main(java.lang.String[] args);\n" +
				"      0  iconst_0\n" +
				"      1  istore_3 [i]\n" +
				"      2  goto 99\n" +
				"      5  iload_3 [i]\n" +
				"      6  bipush 90\n" +
				"      8  if_icmpge 96\n" +
				"     11  new X [1]\n" +
				"     14  dup\n" +
				"     15  invokespecial X() [16]\n" +
				"     18  astore_1 [x]\n" +
				"     19  iload_3 [i]\n" +
				"     20  iconst_4\n" +
				"     21  if_icmple 35\n" +
				"     24  new java.lang.Object [3]\n" +
				"     27  dup\n" +
				"     28  invokespecial java.lang.Object() [8]\n" +
				"     31  astore_2 [o]\n" +
				"     32  goto 37\n" +
				"     35  aconst_null\n" +
				"     36  astore_2 [o]\n" +
				"     37  iload_3 [i]\n" +
				"     38  tableswitch default: 72\n" +
				"          case 0: 56\n" +
				"     56  aload_2 [o]\n" +
				"     57  instanceof java.lang.String [17]\n" +
				"     60  ifeq 88\n" +
				"     63  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
				"     66  ldc <String \"1\"> [25]\n" +
				"     68  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]\n" +
				"     71  return\n" +
				"     72  aload_2 [o]\n" +
				"     73  astore 4 [diff]\n" +
				"     75  aload 4 [diff]\n" +
				"     77  ifnull 88\n" +
				"     80  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
				"     83  ldc <String \"2\"> [33]\n" +
				"     85  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]\n" +
				"     88  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
				"     91  ldc <String \"3\"> [35]\n" +
				"     93  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]\n" +
				"     96  iinc 3 1 [i]\n" +
				"     99  iload_3 [i]\n" +
				"    100  bipush 10\n" +
				"    102  if_icmplt 5\n" +
				"    105  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n" +
				"        [pc: 5, line: 6]\n" +
				"        [pc: 11, line: 7]\n" +
				"        [pc: 19, line: 8]\n" +
				"        [pc: 24, line: 9]\n" +
				"        [pc: 32, line: 10]\n" +
				"        [pc: 35, line: 11]\n" +
				"        [pc: 37, line: 13]\n" +
				"        [pc: 56, line: 15]\n" +
				"        [pc: 63, line: 16]\n" +
				"        [pc: 71, line: 17]\n" +
				"        [pc: 72, line: 22]\n" +
				"        [pc: 75, line: 23]\n" +
				"        [pc: 80, line: 24]\n" +
				"        [pc: 88, line: 29]\n" +
				"        [pc: 96, line: 5]\n" +
				"        [pc: 105, line: 32]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 106] local: args index: 0 type: java.lang.String[]\n" +
				"        [pc: 19, pc: 96] local: x index: 1 type: X\n" +
				"        [pc: 32, pc: 35] local: o index: 2 type: java.lang.Object\n" +
				"        [pc: 37, pc: 96] local: o index: 2 type: java.lang.Object\n" +
				"        [pc: 2, pc: 105] local: i index: 3 type: int\n" +
				"        [pc: 75, pc: 88] local: diff index: 4 type: java.lang.Object\n" +
				"      Stack map table: number of frames 8\n" +
				"        [pc: 5, full, stack: {}, locals: {java.lang.String[], _, _, int}]\n" +
				"        [pc: 35, full, stack: {}, locals: {java.lang.String[], X, _, int}]\n" +
				"        [pc: 37, full, stack: {}, locals: {java.lang.String[], X, java.lang.Object, int}]\n" +
				"        [pc: 56, same]\n" +
				"        [pc: 72, same]\n" +
				"        [pc: 88, same]\n" +
				"        [pc: 96, full, stack: {}, locals: {java.lang.String[], _, _, int}]\n" +
				"        [pc: 99, same]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	public void test013() throws Exception {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"public class X {\n" +
            		"\n" +
            		"  void foo() {\n" +
            		"      synchronized (this) {\n" +
            		"        int n=0;\n" +
            		"        try {\n" +
            		"           Thread.sleep(n); \n" +
            		"        } catch (Exception e ) {\n" +
            		"        }\n" +
            		"     }\n" +
            		"  }\n" +
            		"  \n" +
            		"  public static void main(String[] args) {}\n" +
            		"}",
            },
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  void foo();\n" +
			"     0  aload_0 [this]\n" +
			"     1  dup\n" +
			"     2  astore_1\n" +
			"     3  monitorenter\n" +
			"     4  iconst_0\n" +
			"     5  istore_2 [n]\n" +
			"     6  iload_2 [n]\n" +
			"     7  i2l\n" +
			"     8  invokestatic java.lang.Thread.sleep(long) : void [15]\n" +
			"    11  goto 15\n" +
			"    14  astore_3\n" +
			"    15  aload_1\n" +
			"    16  monitorexit\n" +
			"    17  goto 23\n" +
			"    20  aload_1\n" +
			"    21  monitorexit\n" +
			"    22  athrow\n" +
			"    23  return\n" +
			"      Exception Table:\n" +
			"        [pc: 6, pc: 11] -> 14 when : java.lang.Exception\n" +
			"        [pc: 4, pc: 17] -> 20 when : any\n" +
			"        [pc: 20, pc: 22] -> 20 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"        [pc: 4, line: 5]\n" +
			"        [pc: 6, line: 7]\n" +
			"        [pc: 11, line: 8]\n" +
			"        [pc: 15, line: 4]\n" +
			"        [pc: 23, line: 11]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 24] local: this index: 0 type: X\n" +
			"        [pc: 6, pc: 15] local: n index: 2 type: int\n" +
			"      Stack map table: number of frames 4\n" +
			"        [pc: 14, full, stack: {java.lang.Exception}, locals: {X, X, int}]\n" +
			"        [pc: 15, chop 1 local(s)]\n" +
			"        [pc: 20, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" +
			"        [pc: 23, chop 1 local(s)]\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test014() throws Exception {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"public class X {\n" +
            		"    X() {\n" +
            		"		final int i;\n" +
            		"		synchronized (this) {\n" +
            		"		    i = 8;\n" +
            		"		}\n" +
            		"    }  \n" +
            		"  public static void main(String[] args) {}\n" +
            		"}",
            },
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [8]\n" +
			"     4  aload_0 [this]\n" +
			"     5  dup\n" +
			"     6  astore_2\n" +
			"     7  monitorenter\n" +
			"     8  bipush 8\n" +
			"    10  istore_1 [i]\n" +
			"    11  aload_2\n" +
			"    12  monitorexit\n" +
			"    13  goto 19\n" +
			"    16  aload_2\n" +
			"    17  monitorexit\n" +
			"    18  athrow\n" +
			"    19  return\n" +
			"      Exception Table:\n" +
			"        [pc: 8, pc: 13] -> 16 when : any\n" +
			"        [pc: 16, pc: 18] -> 16 when : any\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 2]\n" +
			"        [pc: 4, line: 4]\n" +
			"        [pc: 8, line: 5]\n" +
			"        [pc: 11, line: 4]\n" +
			"        [pc: 19, line: 7]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 20] local: this index: 0 type: X\n" +
			"        [pc: 11, pc: 16] local: i index: 1 type: int\n" +
			"        [pc: 19, pc: 20] local: i index: 1 type: int\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 16, full, stack: {java.lang.Throwable}, locals: {X, _, X}]\n" +
			"        [pc: 19, full, stack: {}, locals: {X, int}]\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test015() throws Exception {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"public enum X {\n" +
            		"    a1(1), a2(5), a3(11);\n" +
            		"    int value;\n" +
            		"    X(int a) {\n" +
            		"        value = a;\n" +
            		"    }\n" +
            		"    int value () {\n" +
            		"    	return value;\n" +
            		"    }\n" +
            		"    public static void main(String argv[]) {\n" +
            		"    }\n" +
            		"    public static int foo() {\n" +
            		"        int val;\n" +
            		"        int res = 0;\n" +
            		"        int n = 0;\n" +
            		"        X[] vals = X.values();\n" +
            		"		for (int i = 0, max = vals.length; i < max; i++) {\n" +
            		"			X e = vals[i];\n" +
            		"           if ( n == 1) {\n" +
            		"               continue;\n" +
            		"           }\n" +
            		"           val = e.value();\n" +
            		"			System.out.println(val);\n" +
            		"        }\n" +
            		"        return res;\n" +
            		"    }\n" +
            		"}",
            },
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #40 ()I\n" +
			"  // Stack: 2, Locals: 7\n" +
			"  public static int foo();\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [res]\n" +
			"     2  iconst_0\n" +
			"     3  istore_2 [n]\n" +
			"     4  invokestatic X.values() : X[] [46]\n" +
			"     7  astore_3 [vals]\n" +
			"     8  iconst_0\n" +
			"     9  istore 4 [i]\n" +
			"    11  aload_3 [vals]\n" +
			"    12  arraylength\n" +
			"    13  istore 5 [max]\n" +
			"    15  goto 48\n" +
			"    18  aload_3 [vals]\n" +
			"    19  iload 4 [i]\n" +
			"    21  aaload\n" +
			"    22  astore 6 [e]\n" +
			"    24  iload_2 [n]\n" +
			"    25  iconst_1\n" +
			"    26  if_icmpne 32\n" +
			"    29  goto 45\n" +
			"    32  aload 6 [e]\n" +
			"    34  invokevirtual X.value() : int [50]\n" +
			"    37  istore_0 [val]\n" +
			"    38  getstatic java.lang.System.out : java.io.PrintStream [52]\n" +
			"    41  iload_0 [val]\n" +
			"    42  invokevirtual java.io.PrintStream.println(int) : void [58]\n" +
			"    45  iinc 4 1 [i]\n" +
			"    48  iload 4 [i]\n" +
			"    50  iload 5 [max]\n" +
			"    52  if_icmplt 18\n" +
			"    55  iload_1 [res]\n" +
			"    56  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 14]\n" +
			"        [pc: 2, line: 15]\n" +
			"        [pc: 4, line: 16]\n" +
			"        [pc: 8, line: 17]\n" +
			"        [pc: 18, line: 18]\n" +
			"        [pc: 24, line: 19]\n" +
			"        [pc: 29, line: 20]\n" +
			"        [pc: 32, line: 22]\n" +
			"        [pc: 38, line: 23]\n" +
			"        [pc: 45, line: 17]\n" +
			"        [pc: 55, line: 25]\n" +
			"      Local variable table:\n" +
			"        [pc: 38, pc: 45] local: val index: 0 type: int\n" +
			"        [pc: 2, pc: 57] local: res index: 1 type: int\n" +
			"        [pc: 4, pc: 57] local: n index: 2 type: int\n" +
			"        [pc: 8, pc: 57] local: vals index: 3 type: X[]\n" +
			"        [pc: 11, pc: 55] local: i index: 4 type: int\n" +
			"        [pc: 15, pc: 55] local: max index: 5 type: int\n" +
			"        [pc: 24, pc: 45] local: e index: 6 type: X\n" +
			"      Stack map table: number of frames 4\n" +
			"        [pc: 18, full, stack: {}, locals: {_, int, int, X[], int, int}]\n" +
			"        [pc: 32, append: {X}]\n" +
			"        [pc: 45, chop 1 local(s)]\n" +
			"        [pc: 48, same]\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test016() throws Exception {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"public enum X {\n" +
            		"    a1(1), a2(5), a3(11);\n" +
            		"    int value;\n" +
            		"    X(int a) {\n" +
            		"        value = a;\n" +
            		"    }\n" +
            		"    int value () {\n" +
            		"    	return value;\n" +
            		"    }\n" +
            		"    public static void main(String argv[]) {\n" +
            		"    }\n" +
            		"    public static int foo() {\n" +
            		"        int val;\n" +
            		"        int res = 0;\n" +
            		"        int n = 0;\n" +
            		"        for (X e : X.values()) {\n" +
            		"            if ( n == 1) {\n" +
            		"                continue;\n" +
            		"            }\n" +
            		"            val = e.value();\n" +
            		"			 System.out.println(val);\n" +
            		"        }\n" +
            		"        return res;\n" +
            		"    }\n" +
            		"}",
            },
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #40 ()I\n" +
			"  // Stack: 2, Locals: 7\n" +
			"  public static int foo();\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [res]\n" +
			"     2  iconst_0\n" +
			"     3  istore_2 [n]\n" +
			"     4  invokestatic X.values() : X[] [46]\n" +
			"     7  dup\n" +
			"     8  astore 6\n" +
			"    10  arraylength\n" +
			"    11  istore 5\n" +
			"    13  iconst_0\n" +
			"    14  istore 4\n" +
			"    16  goto 48\n" +
			"    19  aload 6\n" +
			"    21  iload 4\n" +
			"    23  aaload\n" +
			"    24  astore_3 [e]\n" +
			"    25  iload_2 [n]\n" +
			"    26  iconst_1\n" +
			"    27  if_icmpne 33\n" +
			"    30  goto 45\n" +
			"    33  aload_3 [e]\n" +
			"    34  invokevirtual X.value() : int [50]\n" +
			"    37  istore_0 [val]\n" +
			"    38  getstatic java.lang.System.out : java.io.PrintStream [52]\n" +
			"    41  iload_0 [val]\n" +
			"    42  invokevirtual java.io.PrintStream.println(int) : void [58]\n" +
			"    45  iinc 4 1\n" +
			"    48  iload 4\n" +
			"    50  iload 5\n" +
			"    52  if_icmplt 19\n" +
			"    55  iload_1 [res]\n" +
			"    56  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 14]\n" +
			"        [pc: 2, line: 15]\n" +
			"        [pc: 4, line: 16]\n" +
			"        [pc: 25, line: 17]\n" +
			"        [pc: 30, line: 18]\n" +
			"        [pc: 33, line: 20]\n" +
			"        [pc: 38, line: 21]\n" +
			"        [pc: 45, line: 16]\n" +
			"        [pc: 55, line: 23]\n" +
			"      Local variable table:\n" +
			"        [pc: 38, pc: 45] local: val index: 0 type: int\n" +
			"        [pc: 2, pc: 57] local: res index: 1 type: int\n" +
			"        [pc: 4, pc: 57] local: n index: 2 type: int\n" +
			"        [pc: 25, pc: 45] local: e index: 3 type: X\n" +
			"      Stack map table: number of frames 4\n" +
			"        [pc: 19, full, stack: {}, locals: {_, int, int, _, int, int, X[]}]\n" +
			"        [pc: 33, full, stack: {}, locals: {_, int, int, X, int, int, X[]}]\n" +
			"        [pc: 45, full, stack: {}, locals: {_, int, int, _, int, int, X[]}]\n" +
			"        [pc: 48, same]\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test017() throws Exception {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"public class X {\n" +
            		"	public static void main(String argv[]) {\n" +
            		"		int i;\n" +
            		"		\n" +
            		"		switch (i = 0) {\n" +
            		"			case 0 :\n" +
            		"				i = 1;\n" +
            		"				break;\n" +
            		"			default :\n" +
            		"				;\n" +
            		"		}\n" +
            		"		System.out.print(i);\n" +
            		"	}\n" +
            		"}",
            },
			"1");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] argv);\n" +
			"     0  iconst_0\n" +
			"     1  dup\n" +
			"     2  istore_1 [i]\n" +
			"     3  tableswitch default: 22\n" +
			"          case 0: 20\n" +
			"    20  iconst_1\n" +
			"    21  istore_1 [i]\n" +
			"    22  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    25  iload_1 [i]\n" +
			"    26  invokevirtual java.io.PrintStream.print(int) : void [22]\n" +
			"    29  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 20, line: 7]\n" +
			"        [pc: 22, line: 12]\n" +
			"        [pc: 29, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 30] local: argv index: 0 type: java.lang.String[]\n" +
			"        [pc: 3, pc: 30] local: i index: 1 type: int\n" +
			"      Stack map table: number of frames 2\n" +
			"        [pc: 20, append: {int}]\n" +
			"        [pc: 22, same]\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test018() {
		this.runConformTest(
            new String[] {
            		"X.java",
            		"import java.util.*;\n" +
            		"\n" +
            		"interface Sequence<Value_Type> extends Iterable<Value_Type>{\n" +
            		"\n" +
            		"    Value_Type get(int i);\n" +
            		"    int length();\n" +
            		"    Value_Type set(int i, Value_Type value);\n" +
            		"}\n" +
            		"\n" +
            		"class ArraySequence<Value_Type> implements Sequence<Value_Type> {\n" +
            		"\n" +
            		"    public ArraySequence(int length) {}\n" +
            		"    public Value_Type get(int i) {\n" +
            		"        return null;\n" +
            		"    }\n" +
            		"    public int length() {\n" +
            		"        return 0;\n" +
            		"    }\n" +
            		"    public Value_Type set(int i, Value_Type value) {\n" +
            		"        return value;\n" +
            		"    }\n" +
            		"    public Iterator<Value_Type> iterator() {\n" +
            		"        return null;\n" +
            		"    }\n" +
            		"}\n" +
            		"\n" +
            		"class BirBlock {\n" +
            		"    void setRole(IrBlock.Role role) {}\n" +
            		"}\n" +
            		"\n" +
            		"class CatchChain {\n" +
            		"    int dispatcherAddress() {\n" +
            		"        return 0;\n" +
            		"    }\n" +
            		"}\n" +
            		"\n" +
            		"class ExceptionHandlerInfo {\n" +
            		"    int handlerProgramCounter() {\n" +
            		"        return 0;\n" +
            		"    }\n" +
            		"}\n" +
            		"\n" +
            		"interface IrBlock {\n" +
            		"    enum Role {\n" +
            		"        EXCEPTION_DISPATCHER\n" +
            		"    }\n" +
            		"}\n" +
            		"\n" +
            		"class ClassMethodActor {\n" +
            		"    Sequence<ExceptionHandlerInfo> exceptionHandlerInfos() {\n" +
            		"        return null;\n" +
            		"    }\n" +
            		"}\n" +
            		"\n" +
            		"public class X {\n" +
            		"\n" +
            		"    private Sequence<CatchChain> _catchChains;\n" +
            		"    private ClassMethodActor _classMethodActor;\n" +
            		"\n" +
            		"    public Sequence<BirBlock> getExceptionDispatchers(final BirBlock[] blockMap) {\n" +
            		"        final ArraySequence<BirBlock> dispatchers = new ArraySequence<BirBlock>(_catchChains.length());\n" +
            		"        for (int i = 0; i < _catchChains.length(); i++) {\n" +
            		"            final BirBlock dispatcher = blockMap[_catchChains.get(i).dispatcherAddress()];\n" +
            		"            dispatcher.setRole(IrBlock.Role.EXCEPTION_DISPATCHER);\n" +
            		"            dispatchers.set(i, dispatcher);\n" +
            		"        }\n" +
            		"        for (ExceptionHandlerInfo exceptionHandlerInfo : _classMethodActor.exceptionHandlerInfos()) {\n" +
            		"            blockMap[exceptionHandlerInfo.handlerProgramCounter()].setRole(IrBlock.Role.EXCEPTION_DISPATCHER);\n" +
            		"        }\n" +
            		"        return dispatchers;\n" +
            		"    }\n" +
            		"    public static void main(String[] args) {\n" +
            		"		System.out.print(\"SUCCESS\");\n" +
            		"	}\n" +
            		"}",
            },
			"SUCCESS");
	}

	public void test019() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"final public class X\n" +
        		"{\n" +
        		"    final class MyClass\n" +
        		"    {\n" +
        		"        void method1(final String s)\n" +
        		"        {\n" +
        		"        }\n" +
        		"    }\n" +
        		"\n" +
        		"    Object method1()\n" +
        		"    {\n" +
        		"        try\n" +
        		"        {\n" +
        		"            final MyClass myClass = null;\n" +
        		"\n" +
        		"            try\n" +
        		"            {\n" +
        		"                return null;\n" +
        		"            }\n" +
        		"            catch (final Throwable ex)\n" +
        		"            {\n" +
        		"                myClass.method1(this == null ? \"\" : \"\");\n" +
        		"            }\n" +
        		"\n" +
        		"            return null;\n" +
        		"        }\n" +
        		"        finally\n" +
        		"        {\n" +
        		"            {\n" +
        		"            }\n" +
        		"        }\n" +
        		"    }\n" +
        		"    public static void main(String[] args) {\n" +
        		"		System.out.print(\"SUCCESS\");\n" +
        		"	}\n" +
        		"}",
            },
			"SUCCESS");
	}
	public void test020() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"import java.util.*;\n" +
        		"public class X {\n" +
        		"    public static Map make(boolean sorted) {\n" +
        		"        return (sorted) ? new TreeMap() : new HashMap();\n" +
        		"    }\n" +
        		"    public static void main(String[] args) {\n" +
        		"       make(false);\n" +
        		"		System.out.print(\"SUCCESS\");\n" +
        		"    }\n" +
        		"}",
            },
			"SUCCESS");
	}
	// 155423
	public void test021() throws Exception {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"public class X {\n" +
        		"   {\n" +
        		"      if (true) throw new NullPointerException();\n" +
        		"   }\n" +
        		"   X() {\n" +
        		"      System.out.println();\n" +
        		"   }\n" +
        		"}",
            },
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
                 classFileBytes,
                 "\n",
                 ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [8]\n" +
			"     4  new java.lang.NullPointerException [10]\n" +
			"     7  dup\n" +
			"     8  invokespecial java.lang.NullPointerException() [12]\n" +
			"    11  athrow\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 4, line: 3]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 12] local: this index: 0 type: X\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	// 157247
	public void test022() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"public class X {\n" +
        		"	public static void main(String[] args) {\n" +
        		"		String errorMessage;\n" +
        		"		try {\n" +
        		"			foo();\n" +
        		"			errorMessage = \"No exception thrown\";\n" +
        		"		} catch (Exception e) {\n" +
        		"			if (e instanceof NullPointerException) {\n" +
        		"				System.out.println(\"SUCCESS\");\n" +
        		"				return;\n" +
        		"			}\n" +
        		"			errorMessage = \"Exception thrown\" + e;\n" +
        		"		}\n" +
        		"		System.out.println(errorMessage);\n" +
        		"	}\n" +
        		"	public static void foo() {\n" +
        		"		throw new NullPointerException();\n" +
        		"	}\n" +
        		"}",
            },
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162965
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean a = true, x;\n" +
				"		if (a ? false : (x = true))\n" +
				"			a = x;\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}",
			},
			"SUCCESS");
	}

	public void test024() {
		this.runConformTest(
            new String[] {
        		"X.java",
        		"public class X {\n" +
        		"	public static final int MAX_PROPERTIES = 25;\n" +
        		"	public C c = new C();\n" +
        		"	void foo(int i) {\n" +
        		"		final int len = c.foo2();\n" +
        		"		A f = new A(\" Test \", i, 1, MAX_PROPERTIES) {\n" +
        		"			@Override\n" +
        		"			public double bar() {\n" +
        		"				return len;\n" +
        		"			}\n" +
        		"			@Override\n" +
        		"			public String toString() {\n" +
        		"				return \"SUCCESS\";\n" +
        		"			}\n" +
        		"		};\n" +
        		"		System.out.println(f);\n" +
        		"	}\n" +
        		"	public static void main(String[] args) {\n" +
        		"		new X().foo(0);\n" +
        		"	}\n" +
        		"}",
        		"A.java",
        		"class A {\n" +
        		"	A(String s, double d, double d1, double d2) {}\n" +
        		"	public double bar() {\n" +
        		"		return 0.0;\n" +
        		"	}\n" +
        		"}",
        		"C.java",
        		"class C {\n" +
        		"	public int foo2() {\n" +
        		"		return 0;\n" +
        		"	}\n" +
        		"}",
            },
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169596
	public void test025() {
		this.runConformTest(
	        new String[] {
	    		"X.java",
	    		"public class X {\n" +
	    		"	public static void main(String[] args) {\n" +
	    		"		System.out.println(\"SUCCESS\");\n" +
	    		"	}\n" +
	    		"	\n" +
	    		"	void foo(Object[] o) {}\n" +
	    		"\n" +
	    		"	void bar(boolean b) {\n" +
	    		"		foo(new Object[] {\"\", \"\", b ? \"\" : \"\"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ \n" +
	    		"	}\n" +
	    		"}"
	        },
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169017
	public void test026() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static final Object EXIT_OK = new Object();\n" +
				"	public static final Object EXIT_RELAUNCH = new Object();\n" +
				"	public static final Object EXIT_RESTART = new Object();\n" +
				"	public static final int RETURN_RESTART = 1;\n" +
				"	public static final String PROP_EXIT_CODE = \"\";\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"	private static int createAndRunWorkbench(Display display, IDEWorkbenchAdvisor advisor) {\n" +
				"		return 0;\n" +
				"	}\n" +
				" \n" +
				"    public Object run(Object args) throws Exception {\n" +
				"        Display display = createDisplay();\n" +
				"        try {\n" +
				"            Shell shell = new Shell(display, SWT.ON_TOP);\n" +
				"            try {\n" +
				"                if (!checkInstanceLocation(shell)) {\n" +
				"                    Platform.endSplash();\n" +
				"                    return EXIT_OK;\n" +
				"                }\n" +
				"            } finally {\n" +
				"                if (shell != null) {\n" +
				"					shell.dispose();\n" +
				"				}\n" +
				"            }\n" +
				"            int returnCode = X.createAndRunWorkbench(display,\n" +
				"                    new IDEWorkbenchAdvisor());\n" +
				"            if (returnCode != X.RETURN_RESTART) {\n" +
				"				return EXIT_OK;\n" +
				"			}\n" +
				"            return EXIT_RELAUNCH.equals(Integer.getInteger(PROP_EXIT_CODE)) ? EXIT_RELAUNCH\n" +
				"                    : EXIT_RESTART;\n" +
				"        } finally {\n" +
				"            if (display != null) {\n" +
				"				display.dispose();\n" +
				"			}\n" +
				"        }\n" +
				"    }\n" +
				"	private boolean checkInstanceLocation(Shell shell) {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return false;\n" +
				"	}\n" +
				"	private Display createDisplay() {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return null;\n" +
				"	}\n" +
				"}",
				"Display.java",
				"class Display {\n" +
				"\n" +
				"	public void dispose() {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		\n" +
				"	}\n" +
				"}",
				"Shell.java",
				"class Shell {\n" +
				"	public Shell(Display display, int i) {\n" +
				"		// TODO Auto-generated constructor stub\n" +
				"	}\n" +
				"\n" +
				"	public void dispose() {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		\n" +
				"	}\n" +
				"}",
				"Platform.java",
				"class Platform {\n" +
				"\n" +
				"	public static void endSplash() {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		\n" +
				"	}\n" +
				"}",
				"SWT.java",
				"class SWT {\n" +
				"	public static final int ON_TOP = 1; \n" +
				"}",
				"IDEWorkbenchAdvisor.java",
				"class IDEWorkbenchAdvisor {\n" +
				"}"
    	},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169017
	public void test027() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"import java.io.InputStream;\n" +
				"public class X {\n" +
				"	private static final int BUF_SIZE = 8192;\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"	BundleActivator activator;\n" +
				"	BundleHost bundle;\n" +
				"	public byte[] getBytes() throws IOException {\n" +
				"		InputStream in = getInputStream();\n" +
				"		int length = (int) getSize();\n" +
				"		byte[] classbytes;\n" +
				"		int bytesread = 0;\n" +
				"		int readcount;\n" +
				"		if (Debug.DEBUG && Debug.DEBUG_LOADER)\n" +
				"			Debug.println(\"  about to read \" + length + \" bytes from \" + getName()); //$NON-NLS-1$ //$NON-NLS-2$\n" +
				"		try {\n" +
				"			if (length > 0) {\n" +
				"				classbytes = new byte[length];\n" +
				"				for (; bytesread < length; bytesread += readcount) {\n" +
				"					readcount = in.read(classbytes, bytesread, length - bytesread);\n" +
				"					if (readcount <= 0)\n" +
				"						break;\n" +
				"				}\n" +
				"			} else {\n" +
				"				length = BUF_SIZE;\n" +
				"				classbytes = new byte[length];\n" +
				"				readloop: while (true) {\n" +
				"					for (; bytesread < length; bytesread += readcount) {\n" +
				"						readcount = in.read(classbytes, bytesread, length - bytesread);\n" +
				"						if (readcount <= 0)\n" +
				"							break readloop;\n" +
				"					}\n" +
				"					byte[] oldbytes = classbytes;\n" +
				"					length += BUF_SIZE;\n" +
				"					classbytes = new byte[length];\n" +
				"					System.arraycopy(oldbytes, 0, classbytes, 0, bytesread);\n" +
				"				}\n" +
				"			}\n" +
				"			if (classbytes.length > bytesread) {\n" +
				"				byte[] oldbytes = classbytes;\n" +
				"				classbytes = new byte[bytesread];\n" +
				"				System.arraycopy(oldbytes, 0, classbytes, 0, bytesread);\n" +
				"			}\n" +
				"		} finally {\n" +
				"			try {\n" +
				"				in.close();\n" +
				"			} catch (IOException ee) {\n" +
				"				// ignore\n" +
				"			}\n" +
				"		}\n" +
				"		return classbytes;\n" +
				"	}\n" +
				"	protected void stop(Throwable t) throws BundleException {\n" +
				"			String clazz = \"\";//(activator == null) ? \"\" : activator.getClass().getName(); //$NON-NLS-1$\n" +
				"			throw new BundleException(NLS.bind(Msg.BUNDLE_ACTIVATOR_EXCEPTION, new Object[] {clazz, \"stop\", bundle.getSymbolicName() == null ? \"\" + bundle.getBundleId() : bundle.getSymbolicName()}), t); //$NON-NLS-1$ //$NON-NLS-2$ \n" +
				"	}\n" +
				"	private String getName() {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return null;\n" +
				"	}\n" +
				"	private int getSize() {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return 0;\n" +
				"	}\n" +
				"	private InputStream getInputStream() {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return null;\n" +
				"	}\n" +
				"}",
				"Debug.java",
				"class Debug {\n" +
				"	public static final boolean DEBUG = false;\n" +
				"	public static final boolean DEBUG_LOADER = false;\n" +
				"	public static final boolean DEBUG_GENERAL = false;\n" +
				"	public static void println(String string) {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		\n" +
				"	}\n" +
				"	public static void printStackTrace(Throwable t) {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		\n" +
				"	}\n" +
				"}",
				"AccessController.java",
				"class AccessController {\n" +
				"	static void doPrivileged(Object o) {\n" +
				"	}\n" +
				"}",
				"BundleException.java",
				"class BundleException extends Exception {\n" +
				"	private static final long serialVersionUID = 5758882959559471648L;\n" +
				"\n" +
				"	public BundleException(String bind, Throwable t) {\n" +
				"		// TODO Auto-generated constructor stub\n" +
				"	}\n" +
				"}",
				"PrivilegedExceptionAction.java",
				"class PrivilegedExceptionAction {\n" +
				"}",
				"BundleActivator.java",
				"class BundleActivator {\n" +
				"	public void stop(X x) {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		\n" +
				"	}\n" +
				"}",
				"BundleHost.java",
				"class BundleHost {\n" +
				"	public Object getSymbolicName() {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return null;\n" +
				"	}\n" +
				"	public String getBundleId() {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return null;\n" +
				"	}\n" +
				"}",
				"NLS.java",
				"class NLS {\n" +
				"	public static String bind(String bundleActivatorException, Object[] objects) {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return null;\n" +
				"	}\n" +
				"}",
				"PrivilegedActionException.java",
				"class PrivilegedActionException extends Exception {\n" +
				"	private static final long serialVersionUID = 3919969055057660822L;\n" +
				"	public Throwable getException() {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return null;\n" +
				"	}\n" +
				"}",
				"Msg.java",
				"class Msg {\n" +
				"	public static final String BUNDLE_ACTIVATOR_EXCEPTION = \"\";\n" +
				"}"
    	},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169017
	public void test028() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.StringReader;\n" +
				"\n" +
				"public class X {\n" +
				"	public void loadVariablesAndContainers() {\n" +
				"		// backward compatibility, consider persistent property	\n" +
				"		String qName = \"1\";\n" +
				"		String xmlString = \"2\";\n" +
				"		\n" +
				"		try {\n" +
				"			if (xmlString != null){\n" +
				"				StringReader reader = new StringReader(xmlString);\n" +
				"				Object o;\n" +
				"				try {\n" +
				"					StringBuffer buffer = null;\n" +
				"					o = new Object();\n" +
				"				} catch(RuntimeException e) {\n" +
				"					return;\n" +
				"				} catch(Exception e){\n" +
				"					return;\n" +
				"				} finally {\n" +
				"					reader.close();\n" +
				"				}\n" +
				"				System.out.println(reader);\n" +
				"			}\n" +
				"		} catch(Exception e){\n" +
				"			// problem loading xml file: nothing we can do\n" +
				"		} finally {\n" +
				"			if (xmlString != null){\n" +
				"				System.out.println(xmlString);\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}"
		},
		"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171472
	public void test029() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public Object foo() {\n" +
				"		Object status;\n" +
				"		try {\n" +
				"			status= bar();\n" +
				"		} catch (RuntimeException x) {\n" +
				"			status= foo2(x);\n" +
				"		} finally {\n" +
				"			System.out.println();\n" +
				"		}\n" +
				"		return status;\n" +
				"	}\n" +
				"	public Object bar() {\n" +
				"		return null;\n" +
				"	}\n" +
				"	public Object foo2(Exception e) {\n" +
				"		return null;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}"
		},
		"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171472
	public void test030() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Collections;\n" +
				"import java.util.List;\n" +
				"public class X {\n" +
				"\n" +
				"	private static final String COMPUTE_COMPLETION_PROPOSALS= \"computeCompletionProposals()\"; //$NON-NLS-1$\n" +
				"	private Object fLastError;\n" +
				"	private boolean fIsReportingDelay;\n" +
				"	private CompletionProposalComputerRegistry fRegistry;\n" +
				"	public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {\n" +
				"		if (!isEnabled())\n" +
				"			return Collections.EMPTY_LIST;\n" +
				"\n" +
				"		IStatus status;\n" +
				"		try {\n" +
				"			IJavaCompletionProposalComputer computer= getComputer();\n" +
				"			if (computer == null) // not active yet\n" +
				"				return Collections.EMPTY_LIST;\n" +
				"			\n" +
				"			try {\n" +
				"				PerformanceStats stats= startMeter(context, computer);\n" +
				"				List proposals= computer.computeCompletionProposals(context, monitor);\n" +
				"				stopMeter(stats, COMPUTE_COMPLETION_PROPOSALS);\n" +
				"				\n" +
				"				if (proposals != null) {\n" +
				"					fLastError= computer.getErrorMessage();\n" +
				"					return proposals;\n" +
				"				}\n" +
				"			} finally {\n" +
				"				fIsReportingDelay= true;\n" +
				"			}\n" +
				"			status= createAPIViolationStatus(COMPUTE_COMPLETION_PROPOSALS);\n" +
				"		} catch (InvalidRegistryObjectException x) {\n" +
				"			status= createExceptionStatus(x);\n" +
				"		} catch (CoreException x) {\n" +
				"			status= createExceptionStatus(x);\n" +
				"		} catch (RuntimeException x) {\n" +
				"			status= createExceptionStatus(x);\n" +
				"		} finally {\n" +
				"			monitor.done();\n" +
				"		}\n" +
				"\n" +
				"		fRegistry.informUser(this, status);\n" +
				"\n" +
				"		return Collections.EMPTY_LIST;\n" +
				"	}\n" +
				"\n" +
				"	private IStatus createExceptionStatus(Exception x) {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"	private IStatus createAPIViolationStatus(String computeCompletionProposals) {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"	private void stopMeter(PerformanceStats stats, String computeCompletionProposals) {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		\n" +
				"	}\n" +
				"\n" +
				"	private PerformanceStats startMeter(ContentAssistInvocationContext context, IJavaCompletionProposalComputer computer) {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"	private IJavaCompletionProposalComputer getComputer() throws CoreException, InvalidRegistryObjectException {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"	private boolean isEnabled() {\n" +
				"		// TODO Auto-generated method stub\n" +
				"		return false;\n" +
				"	}\n" +
				"	\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}",
				"IProgressMonitor.java",
				"interface IProgressMonitor {\n" +
				"	void done();\n" +
				"}",
				"ContentAssistInvocationContext.java",
				"class ContentAssistInvocationContext {\n" +
				"}",
				"IStatus.java",
				"interface IStatus {}",
				"IJavaCompletionProposalComputer.java",
				"import java.util.List;\n" +
				"interface IJavaCompletionProposalComputer {\n" +
				"	List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor);\n" +
				"	Object getErrorMessage();\n" +
				"}",
				"PerformanceStats.java",
				"class PerformanceStats {}",
				"CompletionProposalComputerRegistry.java",
				"class CompletionProposalComputerRegistry {\n" +
				"	public void informUser(X x, IStatus status) {\n" +
				"	}\n" +
				"}",
				"InvalidRegistryObjectException.java",
				"class InvalidRegistryObjectException extends Exception {\n" +
				"	private static final long serialVersionUID = 8943194846421947853L;\n" +
				"}",
				"CoreException.java",
				"class CoreException extends Exception {\n" +
				"	private static final long serialVersionUID = 3996792687633449517L;\n" +
				"}"
		},
		"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=168665
	public void test031() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	String s;\n" +
				"	X() {\n" +
				"		this.s = \"\";\n" +
				"	}\n" +
				"	X(String s, double d, double d2, double i) {\n" +
				"		this.s = s;\n" +
				"	}\n" +
				"	public static final int CONST = 1;\n" +
				"	public int foo() {\n" +
				"		return 0;\n" +
				"	}\n" +
				"	public double value(double d1) {\n" +
				"		return d1;\n" +
				"	}\n" +
				"	public void bar(int start) {\n" +
				"		final int len = foo();\n" +
				"		X x = new X(\"SUCCESS\", start, 1, CONST) {\n" +
				"			@Override\n" +
				"			public double value(double newValue) {\n" +
				"				return len;\n" +
				"			}\n" +
				"		};\n" +
				"		System.out.println(x);\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().bar(1);\n" +
				"	}\n" +
				"	public String toString() {\n" +
				"		return this.s;\n" +
				"	}\n" +
				"}",
			},
			"SUCCESS");
	}
	public void test032() {
		this.runConformTest(
				new String[] {
						"X.java",
						"import java.util.*;\n" +
						"public class X {\n" +
						"    public static void main(String[] args) {\n" +
						"		int i = args.length;\n" +
						"       X[] array = new X[] { i == 0 ? null : null };\n" +
						"		System.out.print(\"SUCCESS\" + array.length);\n" +
						"    }\n" +
						"}",
				},
		"SUCCESS1");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184102
	public void test033() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" +
				"	C { @Override public boolean test() { return true; } };\n" +
				"	static {\n" +
				"		for (int i = 0; i < 1; i++) {}\n" +
				"	}\n" +
				"	public boolean test() {\n" +
				"	return false;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(\"SUCCESS\");\n" +
				"	}\n" +
				"}",
			},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184102
	public void test034() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" +
				"	C;\n" +
				"	static {\n" +
				"		for (int i = 0; i < 1; i++) {}\n" +
				"	}\n" +
				"	public boolean test() {\n" +
				"	return false;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(\"SUCCESS\");\n" +
				"	}\n" +
				"}",
			},
		"SUCCESS");
	}

	// add more bytecode coverage: fneg, lneg, dneg, dstore_0, f2l, fstore_0, fstore_2, lstore_0 and saload
	public void test035() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static double foo() {\n" +
				"		double d = 3.0;\n" +
				"		d = -d;\n" +
				"		return d > 1.0 ? d : -d;\n" +
				"	}\n" +
				"\n" +
				"	static float foo2() {\n" +
				"		float f = 3.0f;\n" +
				"		int i = 0;\n" +
				"		float f2 = f+ i;\n" +
				"		long l = (long) f;\n" +
				"		l += f2;\n" +
				"		return i == 0 ? f : -f + (float) l;\n" +
				"	}\n" +
				"\n" +
				"	static long foo3() {\n" +
				"		long l = Long.MAX_VALUE - 3;\n" +
				"		boolean b = true;\n" +
				"		return b ? l : -l;\n" +
				"	}\n" +
				"	\n" +
				"	static short foo4() {\n" +
				"		short[] tab = new short[] { 1, 2, 3 };\n" +
				"		return tab.length == 3 ? tab[2] : (tab.length == 2 ? tab[1] : tab[0]);\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String args[]) {\n" +
				"		foo();\n" +
				"		foo2();\n" +
				"		foo3();\n" +
				"		foo4();\n" +
				"		System.out.print(\"SUCCESS\");\n" +
				"	}\n" +
				"}",
			},
		"SUCCESS");
	}

	// fix verify error
	public void test036() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.Set;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(\"SUCCESS\");\n" +
				"	}\n" +
				"	public void foo(Object o, boolean b) {\n" +
				"		String[] models = new String[] {};\n" +
				"		Map map = null;\n" +
				"		Set set  = null;\n" +
				"		for (int n = 0; n < models.length; n++) {	bar(models[n]); }\n" +
				"		foo2(new Object(),\n" +
				"				set,\n" +
				"				map,\n" +
				"				!b);\n" +
				"	}\n" +
				"	void bar(String s) {}\n" +
				"	void foo2(Object o, Object s, Object m, boolean b) {}\n" +
				"}",
			},
		"SUCCESS");
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=236336
	public void test037() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.print(\"SUCCESS\");\n" +
				"	}\n" + 
				"	String field;\n" + 
				"	String field2;\n" + 
				"	public void foo(int index, Object[] objs, Object[] objs2) {\n" + 
				"		String methodName = \"foo\";\n" + 
				"		int i = -1;\n" + 
				"		try {\n" + 
				"			switch (index) {\n" + 
				"				case 1130: {\n" + 
				"					int j = ((Integer) objs[0]).intValue();\n" + 
				"					int k = ((Integer) objs[0]).intValue();\n" + 
				"					{\n" + 
				"						String s = field;\n" + 
				"						String s2 = field2;\n" + 
				"						synchronized (s2) {\n" + 
				"							String s3 = s2;\n" + 
				"							try {\n" + 
				"								System.out.println(s);\n" + 
				"								System.out.println(s2);\n" + 
				"								System.out.println(s3);\n" + 
				"							} finally {\n" + 
				"								s2.toString();\n" + 
				"							}\n" + 
				"						}\n" + 
				"						}\n" + 
				"				}\n" + 
				"				break;\n" + 
				"				case 0 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 2 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 3 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 4 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 5 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 6 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 7 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 8 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 9 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 10 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 11 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 12 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 13 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 14 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 15 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 16 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 17 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 18 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 19 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 20 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 21 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 22 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 23 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 24 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 25 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 26 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 27 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 28 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 29 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 30 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 31 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 32 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 33 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 34 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 35 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 36 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 37 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 38 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 39 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 40 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 41 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 42 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 43 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 44 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 45 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 46 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 47 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 48 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 49 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 50 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 51 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 52 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 53 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 54 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 55 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 56 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 57 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 58 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 59 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 60 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 61 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 62 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 63 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 64 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 65 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 66 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 67 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 68 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 69 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 70 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 71 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 72 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 73 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 74 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 75 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 76 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 77 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 78 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 79 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 80 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 81 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 82 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 83 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 84 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 85 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 86 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 87 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 88 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 89 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 90 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 91 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 92 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 93 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 94 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 95 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 96 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 97 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 98 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 99 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 100 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 101 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 102 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 103 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 104 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 105 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 106 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 107 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 108 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 109 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 110 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 111 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 112 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 113 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 114 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 115 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 116 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 117 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 118 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 119 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 120 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 121 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 122 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 123 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 124 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 125 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 126 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 127 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 128 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 129 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 130 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 131 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 132 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 133 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 134 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 135 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 136 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 137 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 138 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 139 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 140 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 141 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 142 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 143 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 144 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 145 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 146 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 147 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 148 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 149 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 150 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 151 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 152 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 153 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 154 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 155 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 156 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 157 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 158 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 159 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 160 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 161 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 162 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 163 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 164 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 165 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 166 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 167 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 168 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 169 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 170 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 171 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 172 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 173 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 174 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 175 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 176 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 177 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 178 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 179 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 180 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 181 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 182 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 183 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 184 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 185 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 186 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 187 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 188 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 189 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 190 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 191 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 192 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 193 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 194 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 195 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 196 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 197 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 198 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 199 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 200 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 201 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 202 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 203 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 204 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 205 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 206 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 207 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 208 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 209 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 210 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 211 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 212 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 213 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 214 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 215 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 216 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 217 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 218 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 219 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 220 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 221 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 222 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 223 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 224 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 225 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 226 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 227 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 228 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 229 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 230 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 231 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 232 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 233 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 234 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 235 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 236 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 237 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 238 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 239 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 240 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 241 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 242 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 243 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 244 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 245 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 246 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 247 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 248 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 249 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 250 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 251 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 252 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 253 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 254 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 255 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 256 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 257 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 258 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 259 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 260 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 261 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 262 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 263 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 264 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 265 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 266 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 267 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 268 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 269 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 270 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 271 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 272 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 273 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 274 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 275 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 276 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 277 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 278 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 279 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 280 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 281 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 282 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 283 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 284 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 285 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 286 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 287 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 288 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 289 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 290 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 291 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 292 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 293 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 294 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 295 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 296 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 297 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 298 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 299 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 300 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 301 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 302 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 303 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 304 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 305 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 306 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 307 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 308 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 309 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 310 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 311 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 312 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 313 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 314 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 315 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 316 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 317 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 318 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 319 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 320 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 321 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 322 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 323 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 324 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 325 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 326 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 327 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 328 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 329 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 330 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 331 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 332 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 333 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 334 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 335 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 336 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 337 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 338 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 339 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 340 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 341 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 342 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 343 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 344 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 345 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 346 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 347 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 348 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 349 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 350 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 351 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 352 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 353 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 354 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 355 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 356 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 357 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 358 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 359 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 360 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 361 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 362 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 363 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 364 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 365 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 366 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 367 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 368 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 369 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 370 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 371 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 372 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 373 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 374 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 375 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 376 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 377 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 378 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 379 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 380 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 381 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 382 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 383 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 384 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 385 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 386 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 387 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 388 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 389 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 390 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 391 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 392 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 393 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 394 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 395 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 396 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 397 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 398 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 399 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 400 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 401 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 402 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 403 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 404 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 405 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 406 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 407 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 408 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 409 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 410 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 411 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 412 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 413 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 414 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 415 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 416 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 417 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 418 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 419 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 420 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 421 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 422 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 423 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 424 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 425 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 426 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 427 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 428 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 429 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 430 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 431 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 432 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 433 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 434 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 435 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 436 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 437 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 438 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 439 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 440 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 441 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 442 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 443 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 444 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 445 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 446 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 447 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 448 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 449 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 450 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 451 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 452 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 453 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 454 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 455 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 456 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 457 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 458 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 459 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 460 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 461 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 462 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 463 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 464 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 465 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 466 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 467 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 468 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 469 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 470 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 471 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 472 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 473 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 474 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 475 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 476 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 477 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 478 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 479 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 480 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 481 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 482 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 483 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 484 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 485 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 486 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 487 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 488 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 489 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 490 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 491 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 492 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 493 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 494 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 495 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 496 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 497 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 498 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 499 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 500 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 501 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 502 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 503 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 504 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 505 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 506 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 507 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 508 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 509 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 510 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 511 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 512 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 513 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 514 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 515 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 516 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 517 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 518 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 519 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 520 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 521 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 522 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 523 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 524 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 525 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 526 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 527 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 528 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 529 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 530 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 531 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 532 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 533 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 534 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 535 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 536 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 537 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 538 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 539 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 540 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 541 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 542 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 543 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 544 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 545 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 546 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 547 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 548 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 549 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 550 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 551 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 552 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 553 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 554 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 555 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 556 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 557 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 558 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 559 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 560 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 561 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 562 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 563 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 564 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 565 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 566 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 567 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 568 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 569 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 570 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 571 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 572 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 573 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 574 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 575 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 576 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 577 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 578 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 579 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 580 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 581 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 582 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 583 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 584 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 585 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 586 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 587 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 588 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 589 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 590 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 591 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 592 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 593 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 594 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 595 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 596 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 597 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 598 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 599 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 600 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 601 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 602 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 603 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 604 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 605 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 606 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 607 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 608 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 609 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 610 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 611 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 612 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 613 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 614 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 615 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 616 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 617 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 618 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 619 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 620 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 621 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 622 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 623 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 624 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 625 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 626 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 627 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 628 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 629 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 630 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 631 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 632 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 633 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 634 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 635 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 636 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 637 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 638 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 639 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 640 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 641 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 642 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 643 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 644 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 645 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 646 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 647 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 648 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 649 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 650 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 651 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 652 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 653 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 654 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 655 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 656 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 657 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 658 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 659 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 660 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 661 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 662 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 663 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 664 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 665 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 666 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 667 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 668 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 669 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 670 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 671 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 672 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 673 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 674 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 675 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 676 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 677 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 678 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 679 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 680 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 681 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 682 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 683 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 684 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 685 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 686 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 687 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 688 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 689 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 690 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 691 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 692 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 693 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 694 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 695 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 696 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 697 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 698 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 699 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 700 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 701 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 702 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 703 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 704 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 705 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 706 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 707 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 708 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 709 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 710 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 711 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 712 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 713 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 714 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 715 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 716 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 717 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 718 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 719 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 720 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 721 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 722 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 723 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 724 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 725 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 726 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 727 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 728 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 729 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 730 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 731 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 732 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 733 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 734 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 735 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 736 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 737 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 738 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 739 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 740 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 741 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 742 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 743 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 744 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 745 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 746 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 747 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 748 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 749 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 750 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 751 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 752 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 753 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 754 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 755 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 756 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 757 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 758 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 759 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 760 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 761 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 762 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 763 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 764 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 765 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 766 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 767 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 768 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 769 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 770 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 771 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 772 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 773 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 774 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 775 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 776 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 777 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 778 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 779 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 780 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 781 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 782 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 783 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 784 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 785 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 786 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 787 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 788 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 789 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 790 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 791 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 792 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 793 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 794 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 795 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 796 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 797 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 798 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 799 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 800 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 801 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 802 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 803 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 804 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 805 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 806 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 807 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 808 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 809 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 810 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 811 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 812 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 813 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 814 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 815 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 816 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 817 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 818 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 819 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 820 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 821 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 822 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 823 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 824 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 825 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 826 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 827 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 828 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 829 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 830 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 831 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 832 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 833 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 834 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 835 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 836 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 837 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 838 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 839 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 840 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 841 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 842 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 843 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 844 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 845 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 846 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 847 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 848 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 849 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 850 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 851 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 852 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 853 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 854 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 855 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 856 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 857 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 858 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 859 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 860 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 861 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 862 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 863 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 864 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 865 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 866 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 867 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 868 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 869 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 870 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 871 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 872 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 873 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 874 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 875 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 876 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 877 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 878 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 879 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 880 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 881 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 882 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 883 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 884 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 885 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 886 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 887 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 888 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 889 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 890 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 891 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 892 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 893 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 894 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 895 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 896 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 897 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 898 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 899 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 900 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 901 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 902 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 903 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 904 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 905 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 906 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 907 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 908 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 909 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 910 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 911 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 912 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 913 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 914 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 915 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 916 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 917 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 918 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 919 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 920 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 921 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 922 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 923 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 924 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 925 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 926 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 927 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 928 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 929 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 930 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 931 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 932 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 933 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 934 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 935 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 936 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 937 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 938 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 939 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 940 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 941 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 942 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 943 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 944 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 945 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 946 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 947 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 948 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 949 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 950 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 951 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 952 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 953 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 954 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 955 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 956 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 957 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 958 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 959 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 960 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 961 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 962 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 963 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 964 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 965 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 966 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 967 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 968 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 969 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 970 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 971 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 972 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 973 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 974 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 975 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 976 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 977 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 978 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 979 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 980 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 981 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 982 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 983 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 984 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 985 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 986 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 987 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 988 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 989 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 990 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 991 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 992 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 993 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 994 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 995 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 996 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 997 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 998 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 999 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1000 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1001 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1002 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1003 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1004 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1005 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1006 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1007 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1008 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1009 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1010 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1011 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1012 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1013 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1014 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1015 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1016 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1017 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1018 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1019 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1020 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1021 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1022 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1023 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1024 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1025 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1026 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1027 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1028 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1029 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1030 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1031 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1032 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1033 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1034 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1035 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1036 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1037 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1038 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1039 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1040 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1041 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1042 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1043 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1044 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1045 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1046 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1047 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1048 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1049 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1050 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1051 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1052 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1053 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1054 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1055 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1056 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1057 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1058 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1059 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1060 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1061 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1062 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1063 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1064 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1065 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1066 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1067 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1068 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1069 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1070 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1071 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1072 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1073 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1074 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1075 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1076 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1077 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1078 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1079 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1080 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1081 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1082 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1083 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1084 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1085 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1086 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1087 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1088 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1089 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1090 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1091 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1092 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1093 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1094 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1095 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1096 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1097 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1098 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1099 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1100 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1101 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1102 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1103 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1104 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1105 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1106 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1107 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1108 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1109 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1110 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1111 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1112 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1113 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1114 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1115 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1116 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1117 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1118 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1119 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1120 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1121 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1122 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1123 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1124 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1125 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1126 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1127 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1128 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" + 
				"				case 1129 :\n" + 
				"					System.out.println(methodName + i);\n" + 
				"				break;\n" +
				"			}\n" +
				"		} catch(Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}\n" +
				"	}" +
				"}",
			},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=238923
	public void test038() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	{\n" + 
				"		for (boolean b : new boolean[] {}) {}\n" + 
				"	}\n" +
				"	public X() {}\n" +
				"	public X(boolean b) {}\n" +
				"	public static void main(String[] args) {\n" + 
				"		System.out.print(\"SUCCESS\");\n" +
				"	}\n" + 
				"}",
			},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=237931
	public void test039() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public String[][] foo(String s) {\n" + 
				"		return\n" + 
				"			new String[][] { {\" \", s != null ? s : \"\" },\n" + 
				"				{\" \", s != null ? s : \"\" },\n" + 
				"				{\" \", s != null ? s : \"\" },\n" + 
				"				{\" \", s != null ? s : \"\" } };\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}",
			},
		"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=251539
	public void test040() throws Exception {
		this.runConformTest(
			new String[] {
				"I.java",
				"public interface I {\n" + 
				"\n" + 
				"	public Object foo();\n" + 
				"\n" + 
				"	public static class B implements I {\n" + 
				"		public Object foo() {\n" + 
				"			return X.myI.foo();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}",
				"X.java",
				"public class X {\n" + 
				"	public static final I myI = new I.B() {\n" + 
				"		int a = 0;\n" + 
				"		int b = 1;\n" + 
				"	};\n" + 
				"\n" + 
				"	private Object bar2() {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"	private Object bar() {\n" + 
				"		Object o = bar2();\n" + 
				"		if (o != null) {\n" + 
				"			o.toString();\n" + 
				"		}\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"\n" + 
				"}",
			},
		"");
		String expectedOutput =
			"  // Method descriptor #23 ()Ljava/lang/Object;\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  private java.lang.Object bar();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial X.bar2() : java.lang.Object [25]\n" + 
			"     4  astore_1 [o]\n" + 
			"     5  aload_1 [o]\n" + 
			"     6  ifnull 14\n" + 
			"     9  aload_1 [o]\n" + 
			"    10  invokevirtual java.lang.Object.toString() : java.lang.String [27]\n" + 
			"    13  pop\n" + 
			"    14  aconst_null\n" + 
			"    15  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 11]\n" + 
			"        [pc: 5, line: 12]\n" + 
			"        [pc: 9, line: 13]\n" + 
			"        [pc: 14, line: 15]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 16] local: this index: 0 type: X\n" + 
			"        [pc: 5, pc: 16] local: o index: 1 type: java.lang.Object\n" + 
			"      Stack map table: number of frames 1\n" + 
			"        [pc: 14, append: {java.lang.Object}]\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=251539
	public void test041() throws Exception {
		this.runConformTest(
			new String[] {
				"I.java",
				"public interface I {\n" + 
				"\n" + 
				"	public Object foo();\n" + 
				"\n" + 
				"	public static class B implements I {\n" + 
				"		public Object foo() {\n" + 
				"			return String.valueOf(X.myI.foo()) + String.valueOf(X.myU.foo());\n" + 
				"		}\n" + 
				"	}\n" + 
				"}",
				"X.java",
				"public class X {\n" + 
				"	public static final I myI = new I.B() {\n" + 
				"		int a = 0;\n" + 
				"		int b = 1;\n" + 
				"	};\n" + 
				"	public static final I myU = new I.B() {\n" + 
				"		int a = 0;\n" + 
				"		int b = 1;\n" + 
				"		int c = 2;\n" + 
				"	};\n" +
				"	private Object bar2() {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"	private Object bar() {\n" + 
				"		Object o = bar2();\n" + 
				"		if (o != null) {\n" + 
				"			o.toString();\n" + 
				"		}\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}",
			},
		"");
	
		String expectedOutput =
			"  // Method descriptor #29 ()Ljava/lang/Object;\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  private java.lang.Object bar();\n" + 
			"     0  aload_0 [this]\n" + 
			"     1  invokespecial X.bar2() : java.lang.Object [31]\n" + 
			"     4  astore_1 [o]\n" + 
			"     5  aload_1 [o]\n" + 
			"     6  ifnull 14\n" + 
			"     9  aload_1 [o]\n" + 
			"    10  invokevirtual java.lang.Object.toString() : java.lang.String [33]\n" + 
			"    13  pop\n" + 
			"    14  aconst_null\n" + 
			"    15  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 15]\n" + 
			"        [pc: 5, line: 16]\n" + 
			"        [pc: 9, line: 17]\n" + 
			"        [pc: 14, line: 19]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 16] local: this index: 0 type: X\n" + 
			"        [pc: 5, pc: 16] local: o index: 1 type: java.lang.Object\n" + 
			"      Stack map table: number of frames 1\n" + 
			"        [pc: 14, append: {java.lang.Object}]\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=260031
	public void test042() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	private static void foo(Class<?> c, int n) { }\n" + 
				"	public static void main(String... args) {\n" + 
				"		foo(Integer.class, (args == null ? -1 : 1));\n" + 
				"	}\n" + 
				"}",
			},
		"");
	
		String expectedOutput =
			"  // Stack: 2, Locals: 1\n" + 
			"  public static void main(java.lang.String... args);\n" + 
			"     0  ldc <Class java.lang.Integer> [26]\n" + 
			"     2  aload_0 [args]\n" + 
			"     3  ifnonnull 10\n" + 
			"     6  iconst_m1\n" + 
			"     7  goto 11\n" + 
			"    10  iconst_1\n" + 
			"    11  invokestatic X.foo(java.lang.Class, int) : void [28]\n" + 
			"    14  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 4]\n" + 
			"        [pc: 14, line: 5]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" + 
			"      Stack map table: number of frames 2\n" + 
			"        [pc: 10, same_locals_1_stack_item, stack: {java.lang.Class}]\n" + 
			"        [pc: 11, full, stack: {java.lang.Class, int}, locals: {java.lang.String[]}]\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=279183
	public void test043() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(\"ok\");\n" + 
				"	}\n" + 
				"	private static int willNotVerify() {\n" + 
				"		int limit = 100;\n" + 
				"		int match;\n" + 
				"		int result = 200;\n" + 
				"		do {\n" + 
				"			if (limit > 0) {\n" + 
				"				continue;\n" + 
				"			}\n" + 
				"			match = 0;\n" + 
				"			while (++match < 100) {\n" + 
				"				System.out.println();\n" + 
				"			}\n" + 
				"		} while (--limit != 0);\n" + 
				"		return result;\n" + 
				"	}\n" + 
				"}",
			},
		"ok");
	
		String expectedOutput =
			"  // Method descriptor #33 ()I\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  private static int willNotVerify();\n" + 
			"     0  bipush 100\n" + 
			"     2  istore_0 [limit]\n" + 
			"     3  sipush 200\n" + 
			"     6  istore_2 [result]\n" + 
			"     7  iload_0 [limit]\n" + 
			"     8  ifle 14\n" + 
			"    11  goto 34\n" + 
			"    14  iconst_0\n" + 
			"    15  istore_1 [match]\n" + 
			"    16  goto 25\n" + 
			"    19  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    22  invokevirtual java.io.PrintStream.println() : void [34]\n" + 
			"    25  iinc 1 1 [match]\n" + 
			"    28  iload_1 [match]\n" + 
			"    29  bipush 100\n" + 
			"    31  if_icmplt 19\n" + 
			"    34  iinc 0 -1 [limit]\n" + 
			"    37  iload_0 [limit]\n" + 
			"    38  ifne 7\n" + 
			"    41  iload_2 [result]\n" + 
			"    42  ireturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 3, line: 8]\n" + 
			"        [pc: 7, line: 10]\n" + 
			"        [pc: 11, line: 11]\n" + 
			"        [pc: 14, line: 13]\n" + 
			"        [pc: 16, line: 14]\n" + 
			"        [pc: 19, line: 15]\n" + 
			"        [pc: 25, line: 14]\n" + 
			"        [pc: 34, line: 17]\n" + 
			"        [pc: 41, line: 18]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 43] local: limit index: 0 type: int\n" + 
			"        [pc: 16, pc: 34] local: match index: 1 type: int\n" + 
			"        [pc: 7, pc: 43] local: result index: 2 type: int\n" + 
			"      Stack map table: number of frames 5\n" + 
			"        [pc: 7, full, stack: {}, locals: {int, _, int}]\n" + 
			"        [pc: 14, same]\n" + 
			"        [pc: 19, full, stack: {}, locals: {int, int, int}]\n" + 
			"        [pc: 25, same]\n" + 
			"        [pc: 34, full, stack: {}, locals: {int, _, int}]\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=279183
	public void test044() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(\"ok\");\n" + 
				"	}\n" + 
				"	private static int willNotVerify() {\n" + 
				"		int limit = 100;\n" + 
				"		int match;\n" + 
				"		int result = 200;\n" + 
				"		do {\n" + 
				"			if (limit > 0) {\n" + 
				"				continue;\n" + 
				"			}\n" + 
				"			match = 0;\n" + 
				"			while (++match < 100) {\n" + 
				"				// empty\n" + 
				"			}\n" + 
				"		} while (--limit != 0);\n" + 
				"		return result;\n" + 
				"	}\n" + 
				"}",
			},
		"ok");
	
		String expectedOutput =
			"  // Method descriptor #33 ()I\n" + 
			"  // Stack: 2, Locals: 3\n" + 
			"  private static int willNotVerify();\n" + 
			"     0  bipush 100\n" + 
			"     2  istore_0 [limit]\n" + 
			"     3  sipush 200\n" + 
			"     6  istore_2 [result]\n" + 
			"     7  iload_0 [limit]\n" + 
			"     8  ifle 14\n" + 
			"    11  goto 25\n" + 
			"    14  iconst_0\n" + 
			"    15  istore_1 [match]\n" + 
			"    16  iinc 1 1 [match]\n" + 
			"    19  iload_1 [match]\n" + 
			"    20  bipush 100\n" + 
			"    22  if_icmplt 16\n" + 
			"    25  iinc 0 -1 [limit]\n" + 
			"    28  iload_0 [limit]\n" + 
			"    29  ifne 7\n" + 
			"    32  iload_2 [result]\n" + 
			"    33  ireturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 6]\n" + 
			"        [pc: 3, line: 8]\n" + 
			"        [pc: 7, line: 10]\n" + 
			"        [pc: 11, line: 11]\n" + 
			"        [pc: 14, line: 13]\n" + 
			"        [pc: 16, line: 14]\n" + 
			"        [pc: 25, line: 17]\n" + 
			"        [pc: 32, line: 18]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 3, pc: 34] local: limit index: 0 type: int\n" + 
			"        [pc: 16, pc: 25] local: match index: 1 type: int\n" + 
			"        [pc: 7, pc: 34] local: result index: 2 type: int\n" + 
			"      Stack map table: number of frames 4\n" + 
			"        [pc: 7, full, stack: {}, locals: {int, _, int}]\n" + 
			"        [pc: 14, same]\n" + 
			"        [pc: 16, full, stack: {}, locals: {int, int, int}]\n" + 
			"        [pc: 25, full, stack: {}, locals: {int, _, int}]\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=279183
	public void test045() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String args[]) {\n" + 
				"		int i;\n" + 
				"		do {\n" + 
				"		} while ((i = 2) < 0);\n" + 
				"		if (i != 2) {\n" + 
				"			System.out.println(\"FAILED\");\n" + 
				"		} else {\n" + 
				"			System.out.println(\"SUCCESS\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"}",
			},
		"SUCCESS");
	
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  public static void main(java.lang.String[] args);\n" + 
			"     0  iconst_2\n" + 
			"     1  dup\n" + 
			"     2  istore_1 [i]\n" + 
			"     3  iflt 0\n" + 
			"     6  iload_1 [i]\n" + 
			"     7  iconst_2\n" + 
			"     8  if_icmpeq 22\n" + 
			"    11  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    14  ldc <String \"FAILED\"> [22]\n" + 
			"    16  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" + 
			"    19  goto 30\n" + 
			"    22  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
			"    25  ldc <String \"SUCCESS\"> [30]\n" + 
			"    27  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" + 
			"    30  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 5]\n" + 
			"        [pc: 3, line: 4]\n" + 
			"        [pc: 6, line: 6]\n" + 
			"        [pc: 11, line: 7]\n" + 
			"        [pc: 19, line: 8]\n" + 
			"        [pc: 22, line: 9]\n" + 
			"        [pc: 30, line: 11]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 31] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 3, pc: 31] local: i index: 1 type: int\n" + 
			"      Stack map table: number of frames 3\n" + 
			"        [pc: 0, same]\n" + 
			"        [pc: 22, append: {int}]\n" + 
			"        [pc: 30, same]\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	// 298250
	public void test046() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	class E1 extends RuntimeException {\n" + 
				"		private static final long serialVersionUID = 1L;\n" + 
				"	}\n" + 
				"	static Object bar() {\n" + 
				"		return new Object() {\n" + 
				"			public void foo() {\n" + 
				"				if (condition())\n" + 
				"					throw new E1();\n" + 
				"			}\n" + 
				"		};\n" + 
				"	}\n" + 
				"	static boolean condition() {\n" + 
				"		return false;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	}\n" + 
				"}\n" + 
				"",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	throw new E1();\n" + 
			"	      ^^^^^^^^\n" + 
			"No enclosing instance of type X is accessible. Must qualify the allocation with an enclosing instance of type X (e.g. x.new A() where x is an instance of X).\n" + 
			"----------\n",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324848
	public void test047() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void test() {\n" + 
				"		final boolean x = true;\n" + 
				"		new Runnable() {\n" + 
				"			@Override\n" + 
				"			public void run() {\n" + 
				"				synchronized (X.this) {\n" + 
				"					System.out.println(x);\n" + 
				"				}\n" + 
				"			}\n" + 
				"		};\n" + 
				"	}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	synchronized (X.this) {\n" + 
			"	              ^^^^^^\n" + 
			"No enclosing instance of the type X is accessible in scope\n" + 
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=351653
	public void test048() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String[] p) {\n" +
				"        int i;\n" +
				"        try {\n" +
				"          if (p == null || p == null)\n" +
				"            return;\n" +
				"          i = 0;\n" +
				"        } finally {\n" +
				"            i = 0;\n" +
				"        }\n" +
				"    }\n" +
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=351653
	public void test049() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" + 
				"import java.io.InputStream;\n" + 
				"\n" + 
				"public class X implements Runnable {\n" + 
				"\n" + 
				"	private boolean contentEquals(final String src, final String tar)\n" + 
				"			throws IOException {\n" + 
				"		if (src == null && tar == null) {\n" + 
				"			return true;\n" + 
				"		}\n" + 
				"		if (!isFile(src) || !isFile(tar))\n" + 
				"			throw new IOException(\"cannot compare non-files\");\n" + 
				"		if (size(src) != size(tar))\n" + 
				"			return false;\n" + 
				"		final byte[] baSrc = new byte[8192];\n" + 
				"		final byte[] baTar = new byte[baSrc.length];\n" + 
				"		int lrSrc;\n" + 
				"		int lrTar;\n" + 
				"		InputStream isSrc = null;\n" + 
				"		InputStream isTar = null;\n" + 
				"		try {\n" + 
				"			isSrc = newInputStream(src);\n" + 
				"			if (isSrc == null)\n" + 
				"				return false;\n" + 
				"			isTar = newInputStream(tar);\n" + 
				"			if (isTar == null)\n" + 
				"				return false;\n" + 
				"			do {\n" + 
				"				lrSrc = isSrc.read(baSrc);\n" + 
				"				lrTar = isTar.read(baTar);\n" + 
				"				if (lrSrc != lrTar)\n" + 
				"					return false;\n" + 
				"				for (int i = 0; i < lrSrc; i++)\n" + 
				"					if (baSrc[i] != baTar[i])\n" + 
				"						return false;\n" + 
				"			} while ((lrSrc >= 0) && (lrSrc == lrTar));\n" + 
				"		} finally {\n" + 
				"			try {\n" + 
				"				close(isSrc);\n" + 
				"			} finally {\n" + 
				"				close(isTar);\n" + 
				"			}\n" + 
				"		}\n" + 
				"		return true;\n" + 
				"	}\n" + 
				"	private void close(final InputStream isSrc) {\n" + 
				"	}\n" + 
				"	private boolean isFile(final String src) {\n" + 
				"		return false;\n" + 
				"	}\n" + 
				"	public void run() {\n" + 
				"		try {\n" + 
				"			System.out.println(contentEquals(null, null));\n" + 
				"		} catch (final IOException e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n" + 
				"	}\n" +
				"	static InputStream newInputStream(String path) {\n" +
				"		return null;\n" +
				"	}\n" + 
				"	static int size(String path) {\n" +
				"		return 0;\n" +
				"	}\n" + 
				"	public static void main(final String[] args) {\n" + 
				"		new X().run();\n" + 
				"	}\n" + 
				"}"
			},
			"true");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=352145
	public void test050() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.File;\n" + 
				"import java.io.FileFilter;\n" + 
				"import java.io.FileNotFoundException;\n" + 
				"import java.io.IOException;\n" + 
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"	public static final List<File> copyDir(List<File> lf,\n" + 
				"			final boolean overwrite, final boolean recursive,\n" + 
				"			final boolean returnSrc, final File src, final File tar,\n" + 
				"			final FileFilter filter) throws IOException {\n" + 
				"		if (!src.isDirectory())\n" + 
				"			throw new FileNotFoundException(\"not a directory: \" + src);\n" + 
				"		if (!tar.isDirectory())\n" + 
				"			throw new FileNotFoundException(\"not a directory: \" + tar);\n" + 
				"		final File[] fa = src.listFiles();\n" + 
				"		if (fa == null)\n" + 
				"			throw new FileNotFoundException(\"directory not accessible: \" + src);\n" + 
				"		if (lf == null)\n" + 
				"			lf = new ArrayList<File>(fa.length);\n" + 
				"		for (final File f : fa) {\n" + 
				"			final File right = new File(tar, f.getName());\n" + 
				"			if (f.isDirectory()) {\n" + 
				"				if (recursive && filter.accept(f)) {\n" + 
				"					if (!right.exists())\n" + 
				"						right.mkdir();\n" + 
				"					copyDir(lf, overwrite, recursive, returnSrc, f, right,\n" + 
				"							filter);\n" + 
				"				}\n" + 
				"			} else {\n" + 
				"				if (overwrite || (!right.exists() && filter.accept(f))) {\n" + 
				"					lf.add(returnSrc ? f : right);\n" + 
				"				}\n" + 
				"			}\n" + 
				"		}\n" + 
				"		return lf;\n" + 
				"	}\n" + 
				"\n" + 
				"	public static void main(final String[] args) {\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"\n" + 
				"}"
			},
			"SUCCESS");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=352145
	public void test051() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.File;\n" + 
				"import java.io.IOException;\n" + 
				"import java.util.List;\n" + 
				"\n" + 
				"public class X {\n" + 
				"\n" + 
				"	public static final List<File> copyDir(int j, List<File> lf,\n" + 
				"			final boolean returnSrc, final File[] fa) throws IOException {\n" + 
				"		if (lf == null)\n" + 
				"			lf = null;\n" + 
				"		for (int i = 0, max = fa.length; i < max; i++) {\n" + 
				"			final File f = fa[i];\n" + 
				"			final File right = new File(f.getName());\n" + 
				"			if (f.isDirectory()) {\n" + 
				"			} else {\n" + 
				"				lf.add(returnSrc ? f : right);\n" + 
				"			}\n" + 
				"		}\n" + 
				"		return lf;\n" + 
				"	}\n" + 
				"\n" + 
				"	public static void main(final String[] args) {\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"\n" + 
				"}"
			},
			"SUCCESS");
	}
	public void test052() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	X(int i) {}\n" +
				"	void foo() {}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X(args.length == 2 ? 1 : 2).foo();\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}",
			},
			"SUCCESS");
	}
	// 352665
	public void test053() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
		customOptions.put(JavaCore.COMPILER_PB_SYNTHETIC_ACCESS_EMULATION, JavaCore.IGNORE);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	static {\n" + 
				"		for(int i = 0; i < 10; i++){\n" + 
				"			A a = new A();\n" + 
				"			a.foo();\n" + 
				"		}\n" + 
				"	}\n" + 
				"	private class A {\n" + 
				"		private A() {\n" + 
				"		}\n" + 
				"		void foo() {}\n" + 
				"	}\n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	A a = new A();\n" + 
			"	      ^^^^^^^\n" + 
			"No enclosing instance of type X is accessible. Must qualify the allocation with an enclosing instance of type X (e.g. x.new A() where x is an instance of X).\n" + 
			"----------\n",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354052
	public void test054() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"public static void foo() {\n" +
			    "     X z;\n" +
			    "     while ((z = getObject()) != null) {\n" +
			    "         z.bar();\n" +
			    "     }\n" +
			    "	  System.out.println(\"SUCCESS\");\n" +
			    " }\n" +
			    " public void bar() {}\n" +
			    " public static X getObject() {\n" +
			    "     return null;\n" +
			    " }\n" +
				"   public static void main(String[] args) {\n"+
				"       new X().foo();\n"+
				"   }\n"+
				"}",
			},
			"SUCCESS");
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=359495
	public void testBug359495a() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.util.List;\n" +
					"import java.util.concurrent.locks.Lock;\n" +
					"import java.util.Arrays;\n" +
					"import java.util.concurrent.locks.ReentrantLock;\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		final Lock lock = new ReentrantLock();\n" +
					"		final List<String> strings = Arrays.asList(args);\n" +
					"		lock.lock();\n" +
					"		try{\n" +
					"			for (final String string:strings){\n" +
					"				return;\n" +
					"			}\n" +
					"			return;\n" +
					"		} finally {\n" +
					"			lock.unlock();\n" +
					"		}" +
					"	}\n" +
					"}",
				},
				"");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
					"  // Stack: 2, Locals: 6\n" + 
					"  public static void main(java.lang.String[] args);\n" + 
					"     0  new java.util.concurrent.locks.ReentrantLock [16]\n" + 
					"     3  dup\n" + 
					"     4  invokespecial java.util.concurrent.locks.ReentrantLock() [18]\n" + 
					"     7  astore_1 [lock]\n" + 
					"     8  aload_0 [args]\n" + 
					"     9  invokestatic java.util.Arrays.asList(java.lang.Object[]) : java.util.List [19]\n" + 
					"    12  astore_2 [strings]\n" + 
					"    13  aload_1 [lock]\n" + 
					"    14  invokeinterface java.util.concurrent.locks.Lock.lock() : void [25] [nargs: 1]\n" + 
					"    19  aload_2 [strings]\n" + 
					"    20  invokeinterface java.util.List.iterator() : java.util.Iterator [30] [nargs: 1]\n" + 
					"    25  astore 4\n" + 
					"    27  aload 4\n" + 
					"    29  invokeinterface java.util.Iterator.hasNext() : boolean [36] [nargs: 1]\n" + 
					"    34  ifeq 55\n" + 
					"    37  aload 4\n" + 
					"    39  invokeinterface java.util.Iterator.next() : java.lang.Object [42] [nargs: 1]\n" + 
					"    44  checkcast java.lang.String [46]\n" + 
					"    47  astore_3 [string]\n" + 
					"    48  aload_1 [lock]\n" + 
					"    49  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [48] [nargs: 1]\n" + 
					"    54  return\n" + 
					"    55  aload_1 [lock]\n" + 
					"    56  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [48] [nargs: 1]\n" + 
					"    61  return\n" + 
					"    62  astore 5\n" + 
					"    64  aload_1 [lock]\n" + 
					"    65  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [48] [nargs: 1]\n" + 
					"    70  aload 5\n" + 
					"    72  athrow\n" + 
					"      Exception Table:\n" + 
					"        [pc: 19, pc: 48] -> 62 when : any\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 7]\n" + 
					"        [pc: 8, line: 8]\n" + 
					"        [pc: 13, line: 9]\n" + 
					"        [pc: 19, line: 11]\n" + 
					"        [pc: 48, line: 16]\n" + 
					"        [pc: 54, line: 12]\n" + 
					"        [pc: 55, line: 16]\n" + 
					"        [pc: 61, line: 14]\n" + 
					"        [pc: 62, line: 15]\n" + 
					"        [pc: 64, line: 16]\n" + 
					"        [pc: 70, line: 17]\n" + 
					"      Local variable table:\n" + 
					"        [pc: 0, pc: 73] local: args index: 0 type: java.lang.String[]\n" + 
					"        [pc: 8, pc: 73] local: lock index: 1 type: java.util.concurrent.locks.Lock\n" + 
					"        [pc: 13, pc: 73] local: strings index: 2 type: java.util.List\n" + 
					"        [pc: 48, pc: 55] local: string index: 3 type: java.lang.String\n" + 
					"      Local variable type table:\n" + 
					"        [pc: 13, pc: 73] local: strings index: 2 type: java.util.List<java.lang.String>\n" + 
					"      Stack map table: number of frames 2\n" + 
					"        [pc: 55, append: {java.util.concurrent.locks.Lock, java.util.List}]\n" + 
					"        [pc: 62, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" ;

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=359495
	public void testBug359495b() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.util.List;\n" +
					"import java.util.Iterator;\n" +
					"import java.util.concurrent.locks.Lock;\n" +
					"import java.util.Arrays;\n" +
					"import java.util.concurrent.locks.ReentrantLock;\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		final Lock lock = new ReentrantLock();\n" +
					"		final List<String> strings = Arrays.asList(args);\n" +
					"		lock.lock();\n" +
					"		try{\n" +
					"			for (Iterator i = strings.iterator(); i.hasNext();){\n" +
					"				return;\n" +
					"			}\n" +
					"			return;\n" +
					"		} finally {\n" +
					"			lock.unlock();\n" +
					"		}" +
					"	}\n" +
					"}",
				},
				"");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
					"  // Stack: 2, Locals: 5\n" + 
					"  public static void main(java.lang.String[] args);\n" + 
					"     0  new java.util.concurrent.locks.ReentrantLock [16]\n" + 
					"     3  dup\n" + 
					"     4  invokespecial java.util.concurrent.locks.ReentrantLock() [18]\n" + 
					"     7  astore_1 [lock]\n" + 
					"     8  aload_0 [args]\n" + 
					"     9  invokestatic java.util.Arrays.asList(java.lang.Object[]) : java.util.List [19]\n" + 
					"    12  astore_2 [strings]\n" + 
					"    13  aload_1 [lock]\n" + 
					"    14  invokeinterface java.util.concurrent.locks.Lock.lock() : void [25] [nargs: 1]\n" + 
					"    19  aload_2 [strings]\n" + 
					"    20  invokeinterface java.util.List.iterator() : java.util.Iterator [30] [nargs: 1]\n" + 
					"    25  astore_3 [i]\n" + 
					"    26  aload_3 [i]\n" + 
					"    27  invokeinterface java.util.Iterator.hasNext() : boolean [36] [nargs: 1]\n" + 
					"    32  ifeq 42\n" + 
					"    35  aload_1 [lock]\n" + 
					"    36  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [42] [nargs: 1]\n" + 
					"    41  return\n" + 
					"    42  aload_1 [lock]\n" + 
					"    43  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [42] [nargs: 1]\n" + 
					"    48  return\n" + 
					"    49  astore 4\n" + 
					"    51  aload_1 [lock]\n" + 
					"    52  invokeinterface java.util.concurrent.locks.Lock.unlock() : void [42] [nargs: 1]\n" + 
					"    57  aload 4\n" + 
					"    59  athrow\n" + 
					"      Exception Table:\n" + 
					"        [pc: 19, pc: 35] -> 49 when : any\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 8]\n" + 
					"        [pc: 8, line: 9]\n" + 
					"        [pc: 13, line: 10]\n" + 
					"        [pc: 19, line: 12]\n" + 
					"        [pc: 35, line: 17]\n" + 
					"        [pc: 41, line: 13]\n" + 
					"        [pc: 42, line: 17]\n" + 
					"        [pc: 48, line: 15]\n" + 
					"        [pc: 49, line: 16]\n" + 
					"        [pc: 51, line: 17]\n" + 
					"        [pc: 57, line: 18]\n" + 
					"      Local variable table:\n" + 
					"        [pc: 0, pc: 60] local: args index: 0 type: java.lang.String[]\n" + 
					"        [pc: 8, pc: 60] local: lock index: 1 type: java.util.concurrent.locks.Lock\n" + 
					"        [pc: 13, pc: 60] local: strings index: 2 type: java.util.List\n" + 
					"        [pc: 26, pc: 42] local: i index: 3 type: java.util.Iterator\n" + 
					"      Local variable type table:\n" + 
					"        [pc: 13, pc: 60] local: strings index: 2 type: java.util.List<java.lang.String>\n" + 
					"      Stack map table: number of frames 2\n" + 
					"        [pc: 42, append: {java.util.concurrent.locks.Lock, java.util.List}]\n" + 
					"        [pc: 49, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=362591
	public void test055() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static void main(String[] args) {\n" + 
					"		testError(3, 4, \"d\");\n" + 
					"	}\n" + 
					"	public static void testError(Number n0, Number n1, String refValue) {\n" + 
					"		Number result = refValue.equals(\"ttt\") ? n0 : (n1 == null ? null : n1.intValue());\n" + 
					"		System.out.println(String.valueOf(result));\n" + 
					"	}\n" + 
					"}",
				},
				"4");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
				"  // Method descriptor #27 (Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/String;)V\n" + 
				"  // Stack: 2, Locals: 4\n" + 
				"  public static void testError(java.lang.Number n0, java.lang.Number n1, java.lang.String refValue);\n" + 
				"     0  aload_2 [refValue]\n" + 
				"     1  ldc <String \"ttt\"> [30]\n" + 
				"     3  invokevirtual java.lang.String.equals(java.lang.Object) : boolean [32]\n" + 
				"     6  ifeq 13\n" + 
				"     9  aload_0 [n0]\n" + 
				"    10  goto 28\n" + 
				"    13  aload_1 [n1]\n" + 
				"    14  ifnonnull 21\n" + 
				"    17  aconst_null\n" + 
				"    18  goto 28\n" + 
				"    21  aload_1 [n1]\n" + 
				"    22  invokevirtual java.lang.Number.intValue() : int [38]\n" + 
				"    25  invokestatic java.lang.Integer.valueOf(int) : java.lang.Integer [16]\n" + 
				"    28  astore_3 [result]\n" + 
				"    29  getstatic java.lang.System.out : java.io.PrintStream [44]\n" + 
				"    32  aload_3 [result]\n" + 
				"    33  invokestatic java.lang.String.valueOf(java.lang.Object) : java.lang.String [50]\n" + 
				"    36  invokevirtual java.io.PrintStream.println(java.lang.String) : void [53]\n" + 
				"    39  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 6]\n" + 
				"        [pc: 29, line: 7]\n" + 
				"        [pc: 39, line: 8]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 40] local: n0 index: 0 type: java.lang.Number\n" + 
				"        [pc: 0, pc: 40] local: n1 index: 1 type: java.lang.Number\n" + 
				"        [pc: 0, pc: 40] local: refValue index: 2 type: java.lang.String\n" + 
				"        [pc: 29, pc: 40] local: result index: 3 type: java.lang.Number\n" + 
				"      Stack map table: number of frames 3\n" + 
				"        [pc: 13, same]\n" + 
				"        [pc: 21, same]\n" + 
				"        [pc: 28, same_locals_1_stack_item, stack: {java.lang.Number}]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	
	public void test055a() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    public static void main(String[] args) {\n" +
					"        Object o = args != null ? args : (args == null ? null : args.length);\n" +
					"    }\n" +
					"}\n",
				},
				"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=366999
	public void test056() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.io.BufferedReader;\n" + 
					"import java.io.Closeable;\n" + 
					"import java.io.File;\n" + 
					"import java.io.FileReader;\n" + 
					"import java.io.IOException;\n" + 
					"\n" + 
					"public class X {\n" + 
					"\n" + 
					"	static class C implements Closeable {\n" + 
					"		@Override\n" + 
					"		public void close() throws IOException {\n" + 
					"			//\n" + 
					"		}\n" + 
					"	}\n" + 
					"\n" + 
					"	int run() throws IOException {\n" + 
					"		int lcnt = 0;\n" + 
					"		try (C c = new C();) {\n" + 
					"			try (final BufferedReader br = new BufferedReader(new FileReader(\n" + 
					"					new File(\"logging.properties\")))) {\n" + 
					"				String s = null;\n" + 
					"				while ((s = br.readLine()) != null)\n" + 
					"					lcnt++;\n" + 
					"				return lcnt;\n" + 
					"			}\n" + 
					"		} finally {\n" + 
					"			System.out.println(\"read \" + lcnt + \" lines\");\n" + 
					"		}\n" + 
					"	}\n" + 
					"\n" + 
					"	public static void main(final String[] args) throws IOException {\n" + 
					"		System.out.println(\"SUCCESS\");\n" + 
					"	}\n" + 
					"}",
				},
				"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test057() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    public void run() {\n" +
					"        String s;\n" +
					"        label1: do {\n" +
					"            for (;;) {\n" +
					"                s = \"\";\n" +
					"                if (s == null) \n" +
					"                    continue label1;\n" +
					"            }\n" +
					"        } while (s != null);\n" +
					"}\n" +
					"    public static void main(String [] args) {\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"    }\n" +
					"}\n",
				},
				"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test058() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    public void run() {\n" +
					"        String s;\n" +
					"        label1: do {\n" +
					"            for (;true;) {\n" +
					"                s = \"\";\n" +
					"                if (s == null) \n" +
					"                    continue label1;\n" +
					"            }\n" +
					"        } while (s != null);\n" +
					"}\n" +
					"    public static void main(String [] args) {\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"    }\n" +
					"}\n"				},
				"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test059() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    public void run() {\n" +
					"        String s;\n" +
					"        label1: do {\n" +
					"            for (;false;) {\n" +
					"                s = \"\";\n" +
					"                if (s == null) \n" +
					"                    continue label1;\n" +
					"            }\n" +
					"        } while (s != null);\n" +
					"}\n" +
					"    public static void main(String [] args) {\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"    }\n" +
					"}\n"				},
					"----------\n" + 
					"1. WARNING in X.java (at line 4)\n" + 
					"	label1: do {\n" + 
					"	^^^^^^\n" + 
					"The label label1 is never explicitly referenced\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 5)\n" + 
					"	for (;false;) {\n" + 
					"                s = \"\";\n" + 
					"                if (s == null) \n" + 
					"                    continue label1;\n" + 
					"            }\n" + 
					"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
					"Unreachable code\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 10)\n" + 
					"	} while (s != null);\n" + 
					"	         ^\n" + 
					"The local variable s may not have been initialized\n" + 
						"----------\n");
	}	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test060() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    public void run() {\n" +
					"        String s;\n" +
					"        label1: do {\n" +
					"            for (; 5 < 10;) {\n" +
					"                s = \"\";\n" +
					"                if (s == null) \n" +
					"                    continue label1;\n" +
					"            }\n" +
					"        } while (s != null);\n" +
					"}\n" +
					"    public static void main(String [] args) {\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"    }\n" +
					"}\n"				},
				"SUCCESS");
	}	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test061() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    public void run() {\n" +
					"        int five = 5, ten = 10;\n" +
					"        String s;\n" +
					"        label1: do {\n" +
					"            for (; five < ten;) {\n" +
					"                s = \"\";\n" +
					"                if (s == null) \n" +
					"                    continue label1;\n" +
					"            }\n" +
					"        } while (s != null);\n" +
					"}\n" +
					"    public static void main(String [] args) {\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"    }\n" +
					"}\n"				},
					"----------\n" + 
					"1. WARNING in X.java (at line 9)\n" + 
					"	continue label1;\n" + 
					"	^^^^^^^^^^^^^^^^\n" + 
					"Dead code\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 11)\n" + 
					"	} while (s != null);\n" + 
					"	         ^\n" + 
					"The local variable s may not have been initialized\n" + 
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test062() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public void run() {\n" +
				"        final int five = 5, ten = 10;\n" +
				"        String s;\n" +
				"        label1: do {\n" +
				"            for (; five < ten;) {\n" +
				"                s = \"\";\n" +
				"                if (s == null) \n" +
				"                    continue label1;\n" +
				"            }\n" +
				"        } while (s != null);\n" +
				"}\n" +
				"    public static void main(String [] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"    }\n" +
				"}\n"				},
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test063() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public void run() {\n" +
				"        final int five = 5, ten = 10;\n" +
				"        String s;\n" +
				"        label1: do {\n" +
				"            for (; five > ten;) {\n" +
				"                s = \"\";\n" +
				"                if (s == null) \n" +
				"                    continue label1;\n" +
				"            }\n" +
				"        } while (s != null);\n" +
				"}\n" +
				"    public static void main(String [] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"    }\n" +
				"}\n"				},
				"----------\n" + 
				"1. WARNING in X.java (at line 5)\n" + 
				"	label1: do {\n" + 
				"	^^^^^^\n" + 
				"The label label1 is never explicitly referenced\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	for (; five > ten;) {\n" + 
				"                s = \"\";\n" + 
				"                if (s == null) \n" + 
				"                    continue label1;\n" + 
				"            }\n" + 
				"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Unreachable code\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 11)\n" + 
				"	} while (s != null);\n" + 
				"	         ^\n" + 
				"The local variable s may not have been initialized\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test064() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    public void run() {\n" +
					"        String s;\n" +
					"        label1: do {\n" +
					"            while (true) {\n" +
					"                s = \"\";\n" +
					"                if (s == null) \n" +
					"                    continue label1;\n" +
					"            }\n" +
					"        } while (s != null);\n" +
					"}\n" +
					"    public static void main(String [] args) {\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"    }\n" +
					"}\n",
				},
				"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test065() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    public void run() {\n" +
					"        String s;\n" +
					"        label1: do {\n" +
					"            while (false) {\n" +
					"                s = \"\";\n" +
					"                if (s == null) \n" +
					"                    continue label1;\n" +
					"            }\n" +
					"        } while (s != null);\n" +
					"}\n" +
					"    public static void main(String [] args) {\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"    }\n" +
					"}\n"				},
					"----------\n" + 
					"1. WARNING in X.java (at line 4)\n" + 
					"	label1: do {\n" + 
					"	^^^^^^\n" + 
					"The label label1 is never explicitly referenced\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 5)\n" + 
					"	while (false) {\n" + 
					"                s = \"\";\n" + 
					"                if (s == null) \n" + 
					"                    continue label1;\n" + 
					"            }\n" + 
					"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
					"Unreachable code\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 10)\n" + 
					"	} while (s != null);\n" + 
					"	         ^\n" + 
					"The local variable s may not have been initialized\n" + 
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test066() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    public void run() {\n" +
					"        String s;\n" +
					"        label1: do {\n" +
					"            while(5 < 10) {\n" +
					"                s = \"\";\n" +
					"                if (s == null) \n" +
					"                    continue label1;\n" +
					"            }\n" +
					"        } while (s != null);\n" +
					"}\n" +
					"    public static void main(String [] args) {\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"    }\n" +
					"}\n"				},
				"SUCCESS");
	}	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test067() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    public void run() {\n" +
					"        int five = 5, ten = 10;\n" +
					"        String s;\n" +
					"        label1: do {\n" +
					"            while (five < ten) {\n" +
					"                s = \"\";\n" +
					"                if (s == null) \n" +
					"                    continue label1;\n" +
					"            }\n" +
					"        } while (s != null);\n" +
					"}\n" +
					"    public static void main(String [] args) {\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"    }\n" +
					"}\n"				},
					"----------\n" + 
					"1. WARNING in X.java (at line 9)\n" + 
					"	continue label1;\n" + 
					"	^^^^^^^^^^^^^^^^\n" + 
					"Dead code\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 11)\n" + 
					"	} while (s != null);\n" + 
					"	         ^\n" + 
					"The local variable s may not have been initialized\n" + 
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test068() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public void run() {\n" +
				"        final int five = 5, ten = 10;\n" +
				"        String s;\n" +
				"        label1: do {\n" +
				"            while (five < ten) {\n" +
				"                s = \"\";\n" +
				"                if (s == null) \n" +
				"                    continue label1;\n" +
				"            }\n" +
				"        } while (s != null);\n" +
				"}\n" +
				"    public static void main(String [] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"    }\n" +
				"}\n"				},
			"SUCCESS");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test069() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public void run() {\n" +
				"        final int five = 5, ten = 10;\n" +
				"        String s;\n" +
				"        label1: do {\n" +
				"            while (five > ten) {\n" +
				"                s = \"\";\n" +
				"                if (s == null) \n" +
				"                    continue label1;\n" +
				"            }\n" +
				"        } while (s != null);\n" +
				"}\n" +
				"    public static void main(String [] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"    }\n" +
				"}\n"				},
				"----------\n" + 
				"1. WARNING in X.java (at line 5)\n" + 
				"	label1: do {\n" + 
				"	^^^^^^\n" + 
				"The label label1 is never explicitly referenced\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	while (five > ten) {\n" + 
				"                s = \"\";\n" + 
				"                if (s == null) \n" + 
				"                    continue label1;\n" + 
				"            }\n" + 
				"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Unreachable code\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 11)\n" + 
				"	} while (s != null);\n" + 
				"	         ^\n" + 
				"The local variable s may not have been initialized\n" + 
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=367023
	public void test070() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.Arrays;\n" +
				"import java.util.Iterator;\n" +
				"import java.util.List;\n" +
				"import java.util.Properties;\n" +
				"import org.w3c.dom.*;\n" +
				"public class X extends Object {\n" +
				"        public static void main(String [] args) {\n" +
				"            System.out.println (\"SUCCESS\");\n" +
				"        }\n" +
				"	private static class Handler extends Object {\n" +
				"		public int getStuff() {\n" +
				"			return 1;\n" +
				"		}\n" +
				"		public void handle(Element element) {\n" +
				"			Properties properties = new Properties();\n" +
				"			NamedNodeMap atts = element.getAttributes();\n" +
				"			if (atts != null) {\n" +
				"				for (int a = 0; a < atts.getLength(); a++) {\n" +
				"					Node att = atts.item(a);\n" +
				"					String name = att.getNodeName();\n" +
				"					String value = att.getNodeValue();\n" +
				"					if (\"foo\".equals(name)) {\n" +
				"						name = value;\n" +
				"					} else {\n" +
				"						if (!\"bar\".equals(name))\n" +
				"							continue;\n" +
				"						name = value;\n" +
				"					}\n" +
				"					properties.put(name, value);\n" +
				"				}\n" +
				"			}\n" +
				"			label0: do {\n" +
				"				Node node;\n" +
				"				String nodeName;\n" +
				"				label1: do {\n" +
				"					for (Iterator i = (new ArrayList(1)).iterator(); i\n" +
				"							.hasNext(); members.add(equals(node))) {\n" +
				"						node = (Node) i.next();\n" +
				"						nodeName = \"\" + equals(node.getNodeName());\n" +
				"						if (!\"foo\".equals(nodeName))\n" +
				"							continue label1;\n" +
				"					}\n" +
				"					break label0;\n" +
				"				} while (!\"bar\".equals(nodeName));\n" +
				"				Iterator i = (new ArrayList(1)).iterator();\n" +
				"				while (i.hasNext()) {\n" +
				"					Node n = (Node) i.next();\n" +
				"					String name = toString() + n.getNodeName();\n" +
				"					if (\"wtf\".equals(name)) {\n" +
				"						String propertyName = (toString() + n.getAttributes()\n" +
				"								.getNamedItem(\"broken\")).trim();\n" +
				"						String value = toString() + n;\n" +
				"						properties.put(propertyName, value);\n" +
				"					}\n" +
				"				}\n" +
				"			} while (true);\n" +
				"			propertiesBuilder.equals(properties);\n" +
				"			builder.equals(propertiesBuilder.hashCode());\n" +
				"			builder.equals(members);\n" +
				"		}\n" +
				"		private final Object c;\n" +
				"		private Object builder;\n" +
				"		private List members;\n" +
				"		private Object propertiesBuilder;\n" +
				"		public Handler(Object c) {\n" +
				"			this.c = c;\n" +
				"			builder = Arrays.asList(Object.class);\n" +
				"			builder.equals(\"foo\");\n" +
				"			builder.equals(\"bar\");\n" +
				"			members = new ArrayList();\n" +
				"			propertiesBuilder = Arrays.asList(Object.class);\n" +
				"			Object beanDefinition = propertiesBuilder.toString();\n" +
				"			Object holder = new String(\"stirng\");\n" +
				"			Arrays.asList(holder, c.toString());\n" +
				"		}\n" +
				"	}\n" +
				"	public X() {\n" +
				"	}\n" +
				"	protected Object parseInternal(Element element, Object c) {\n" +
				"		Handler h = new Handler(c);\n" +
				"		h.handle(element);\n" +
				"		return h.getStuff();\n" +
				"	}\n" +
				"}\n"
				},
				"SUCCESS");
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380313
	// Verify the generated code does not have same branch target for the 2 return statements
	public void testBug380313() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"public void foo() throws Exception {\n" + 
						"        int i = 1;\n" + 
						"        try {\n" + 
						"            if (i == 1) {\n" + 
						"                int n = bar();\n" + 
						"                if (n == 35)\n" + 
						"                   return;\n" + 
						"            } else {\n" + 
						"                throw new Exception();\n" + 
						"            }\n" + 
						"            if (i == 0)\n" + 
						"               return;\n" + 
						"        } finally {\n" + 
						"            bar();\n" + 
						"        }\n" + 
						"    }\n" + 
						"\n" + 
						"    private int bar() {\n" + 
						"        return 0;\n" + 
						"    }\n" + 
						"\n" + 
						"    public static void main(String[] args) {\n" +
						"		System.out.println(\"SUCCESS\");\n" + 
						"    }\n" +
						"}\n"
				},
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"  // Method descriptor #6 ()V\n" + 
					"  // Stack: 2, Locals: 4\n" + 
					"  public void foo() throws java.lang.Exception;\n" + 
					"     0  iconst_1\n" + 
					"     1  istore_1 [i]\n" + 
					"     2  iload_1 [i]\n" + 
					"     3  iconst_1\n" + 
					"     4  if_icmpne 24\n" + 
					"     7  aload_0 [this]\n" + 
					"     8  invokespecial X.bar() : int [18]\n" + 
					"    11  istore_2 [n]\n" + 
					"    12  iload_2 [n]\n" + 
					"    13  bipush 35\n" + 
					"    15  if_icmpne 32\n" + 
					"    18  aload_0 [this]\n" + 
					"    19  invokespecial X.bar() : int [18]\n" + 
					"    22  pop\n" + 
					"    23  return\n" + 
					"    24  new java.lang.Exception [16]\n" + 
					"    27  dup\n" + 
					"    28  invokespecial java.lang.Exception() [22]\n" + 
					"    31  athrow\n" + 
					"    32  iload_1 [i]\n" + 
					"    33  ifne 50\n" + 
					"    36  aload_0 [this]\n" + 
					"    37  invokespecial X.bar() : int [18]\n" + 
					"    40  pop\n" + 
					"    41  return\n" + 
					"    42  astore_3\n" + 
					"    43  aload_0 [this]\n" + 
					"    44  invokespecial X.bar() : int [18]\n" + 
					"    47  pop\n" + 
					"    48  aload_3\n" + 
					"    49  athrow\n" + 
					"    50  aload_0 [this]\n" + 
					"    51  invokespecial X.bar() : int [18]\n" + 
					"    54  pop\n" + 
					"    55  return\n" + 
					"      Exception Table:\n" + 
					"        [pc: 2, pc: 18] -> 42 when : any\n" + 
					"        [pc: 24, pc: 36] -> 42 when : any\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 3]\n" + 
					"        [pc: 2, line: 5]\n" + 
					"        [pc: 7, line: 6]\n" + 
					"        [pc: 12, line: 7]\n" + 
					"        [pc: 18, line: 15]\n" + 
					"        [pc: 23, line: 8]\n" + 
					"        [pc: 24, line: 10]\n" + 
					"        [pc: 32, line: 12]\n" + 
					"        [pc: 36, line: 15]\n" + 
					"        [pc: 41, line: 13]\n" + 
					"        [pc: 42, line: 14]\n" + 
					"        [pc: 43, line: 15]\n" + 
					"        [pc: 48, line: 16]\n" + 
					"        [pc: 50, line: 15]\n" + 
					"        [pc: 55, line: 17]\n" + 
					"      Local variable table:\n" + 
					"        [pc: 0, pc: 56] local: this index: 0 type: X\n" + 
					"        [pc: 2, pc: 56] local: i index: 1 type: int\n" + 
					"        [pc: 12, pc: 24] local: n index: 2 type: int\n" + 
					"      Stack map table: number of frames 4\n" + 
					"        [pc: 24, append: {int}]\n" + 
					"        [pc: 32, same]\n" + 
					"        [pc: 42, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" + 
					"        [pc: 50, same]\n";
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380313
	// Verify the generated code does not have same branch target for the 2 return statements
	public void testBug380313b() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"import java.io.FileInputStream;\n" +
						"import java.io.IOException;\n" +
						"public class X {\n" +
						"public void foo() throws Exception {\n" + 
						"        int i = 1;\n" + 
						"        try {\n" + 
						"            try (FileInputStream fis = new FileInputStream(\"\")) {\n" +
						"				 if (i == 2)" + 
						"                	return;\n" + 
						" 			 }\n" + 
						"            if (i == 35) \n" + 
						"                return;\n" + 
						"        } catch(IOException e) {\n" + 
						"            bar();\n" + 
						"        } finally {\n" + 
						"            bar();\n" + 
						"        }\n" + 
						"    }\n" + 
						"\n" + 
						"    private int bar() {\n" + 
						"        return 0;\n" + 
						"    }\n" + 
						"\n" + 
						"    public static void main(String[] args) {\n" +
						"		System.out.println(\"SUCCESS\");\n" + 
						"    }\n" +
						"}\n"
					}, 
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"  // Method descriptor #6 ()V\n" + 
					"  // Stack: 3, Locals: 6\n" + 
					"  public void foo() throws java.lang.Exception;\n" + 
					"      0  iconst_1\n" + 
					"      1  istore_1 [i]\n" + 
					"      2  aconst_null\n" + 
					"      3  astore_2\n" + 
					"      4  aconst_null\n" + 
					"      5  astore_3\n" + 
					"      6  new java.io.FileInputStream [18]\n" + 
					"      9  dup\n" + 
					"     10  ldc <String \"\"> [20]\n" + 
					"     12  invokespecial java.io.FileInputStream(java.lang.String) [22]\n" + 
					"     15  astore 4 [fis]\n" + 
					"     17  iload_1 [i]\n" + 
					"     18  iconst_2\n" + 
					"     19  if_icmpne 38\n" + 
					"     22  aload 4 [fis]\n" + 
					"     24  ifnull 32\n" + 
					"     27  aload 4 [fis]\n" + 
					"     29  invokevirtual java.io.FileInputStream.close() : void [25]\n" + 
					"     32  aload_0 [this]\n" + // return 1
					"     33  invokespecial X.bar() : int [28]\n" + 
					"     36  pop\n" + 
					"     37  return\n" + 
					"     38  aload 4 [fis]\n" + 
					"     40  ifnull 86\n" + 
					"     43  aload 4 [fis]\n" + 
					"     45  invokevirtual java.io.FileInputStream.close() : void [25]\n" + 
					"     48  goto 86\n" + 
					"     51  astore_2\n" + 
					"     52  aload 4 [fis]\n" + 
					"     54  ifnull 62\n" + 
					"     57  aload 4 [fis]\n" + 
					"     59  invokevirtual java.io.FileInputStream.close() : void [25]\n" + 
					"     62  aload_2\n" + 
					"     63  athrow\n" + 
					"     64  astore_3\n" + 
					"     65  aload_2\n" + 
					"     66  ifnonnull 74\n" + 
					"     69  aload_3\n" + 
					"     70  astore_2\n" + 
					"     71  goto 84\n" + 
					"     74  aload_2\n" + 
					"     75  aload_3\n" + 
					"     76  if_acmpeq 84\n" + 
					"     79  aload_2\n" + 
					"     80  aload_3\n" + 
					"     81  invokevirtual java.lang.Throwable.addSuppressed(java.lang.Throwable) : void [32]\n" + 
					"     84  aload_2\n" + 
					"     85  athrow\n" + 
					"     86  iload_1 [i]\n" + 
					"     87  bipush 35\n" + 
					"     89  if_icmpne 122\n" + 
					"     92  aload_0 [this]\n" + 	// return 2
					"     93  invokespecial X.bar() : int [28]\n" + 
					"     96  pop\n" + 
					"     97  return\n" + 
					"     98  astore_2 [e]\n" + 
					"     99  aload_0 [this]\n" + 
					"    100  invokespecial X.bar() : int [28]\n" + 
					"    103  pop\n" + 
					"    104  aload_0 [this]\n" + 
					"    105  invokespecial X.bar() : int [28]\n" + 
					"    108  pop\n" + 
					"    109  goto 127\n" + 
					"    112  astore 5\n" + 
					"    114  aload_0 [this]\n" + 
					"    115  invokespecial X.bar() : int [28]\n" + 
					"    118  pop\n" + 
					"    119  aload 5\n" + 
					"    121  athrow\n" + 
					"    122  aload_0 [this]\n" + 
					"    123  invokespecial X.bar() : int [28]\n" + 
					"    126  pop\n" + 
					"    127  return\n" + 
					"      Exception Table:\n" + 
					"        [pc: 17, pc: 22] -> 51 when : any\n" + 
					"        [pc: 32, pc: 38] -> 51 when : any\n" + 
					"        [pc: 6, pc: 64] -> 64 when : any\n" + 
					"        [pc: 2, pc: 32] -> 98 when : java.io.IOException\n" + 
					"        [pc: 38, pc: 92] -> 98 when : java.io.IOException\n" + 
					"        [pc: 2, pc: 32] -> 112 when : any\n" + 
					"        [pc: 38, pc: 92] -> 112 when : any\n" + 
					"        [pc: 98, pc: 104] -> 112 when : any\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 5]\n" + 
					"        [pc: 2, line: 7]\n" + 
					"        [pc: 17, line: 8]\n" + 
					"        [pc: 22, line: 9]\n" + 
					"        [pc: 32, line: 15]\n" + 
					"        [pc: 37, line: 8]\n" + 
					"        [pc: 38, line: 9]\n" + 
					"        [pc: 86, line: 10]\n" + 
					"        [pc: 92, line: 15]\n" + 
					"        [pc: 97, line: 11]\n" + 
					"        [pc: 98, line: 12]\n" + 
					"        [pc: 99, line: 13]\n" + 
					"        [pc: 104, line: 15]\n" + 
					"        [pc: 112, line: 14]\n" + 
					"        [pc: 114, line: 15]\n" + 
					"        [pc: 119, line: 16]\n" + 
					"        [pc: 122, line: 15]\n" + 
					"        [pc: 127, line: 17]\n" + 
					"      Local variable table:\n" + 
					"        [pc: 0, pc: 128] local: this index: 0 type: X\n" + 
					"        [pc: 2, pc: 128] local: i index: 1 type: int\n" + 
					"        [pc: 17, pc: 62] local: fis index: 4 type: java.io.FileInputStream\n" + 
					"        [pc: 99, pc: 104] local: e index: 2 type: java.io.IOException\n" + 
					"      Stack map table: number of frames 12\n" + 
					"        [pc: 32, full, stack: {}, locals: {X, int, java.lang.Throwable, java.lang.Throwable, java.io.FileInputStream}]\n" + 
					"        [pc: 38, same]\n" + 
					"        [pc: 51, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" + 
					"        [pc: 62, chop 1 local(s)]\n" + 
					"        [pc: 64, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" + 
					"        [pc: 74, same]\n" + 
					"        [pc: 84, same]\n" + 
					"        [pc: 86, chop 2 local(s)]\n" + 
					"        [pc: 98, same_locals_1_stack_item, stack: {java.io.IOException}]\n" + 
					"        [pc: 112, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" + 
					"        [pc: 122, same]\n" + 
					"        [pc: 127, same]\n";
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
	// Verify the reduced range of locals.
	public void testBug380927() throws Exception {
		this.runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    public final static Object f() {\n" +
						"        final Object a = null;\n" +
						"        Object b;\n" +
						"        label: do {\n" +
						"            switch (0) {\n" +
						"            case 1: {\n" +
						"                b = a;\n" +
						"            }\n" +
						"                break;\n" +
						"            default:\n" +
						"                break label;\n" +
						"            }\n" +
						"        } while (true);\n" +
						"        return a;\n" +
						"    }\n" +
						"    public static void main(final String[] args) {\n" +
						"        f();\n" +
						"        System.out.println(\"SUCCESS\");\n" +
						"    }\n" +
						"}\n"
				},
				"SUCCESS");

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"  // Method descriptor #15 ()Ljava/lang/Object;\n" + 
					"  // Stack: 1, Locals: 2\n" + 
					"  public static final java.lang.Object f();\n" + 
					"     0  aconst_null\n" + 
					"     1  astore_0 [a]\n" + 
					"     2  iconst_0\n" + 
					"     3  tableswitch default: 25\n" + 
					"          case 1: 20\n" + 
					"    20  aload_0 [a]\n" + 
					"    21  astore_1 [b]\n" + 
					"    22  goto 2\n" + 
					"    25  aload_0 [a]\n" + 
					"    26  areturn\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 3]\n" + 
					"        [pc: 2, line: 6]\n" + 
					"        [pc: 20, line: 8]\n" + 
					"        [pc: 22, line: 10]\n" + 
					"        [pc: 25, line: 15]\n" + 
					"      Local variable table:\n" + 
					"        [pc: 2, pc: 27] local: a index: 0 type: java.lang.Object\n" + 
					"        [pc: 22, pc: 25] local: b index: 1 type: java.lang.Object\n" + 
					"      Stack map table: number of frames 3\n" + 
					"        [pc: 2, append: {java.lang.Object}]\n" + 
					"        [pc: 20, same]\n" + 
					"        [pc: 25, same]\n" + 
					"  \n"; 
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	// from https://bugs.eclipse.org/bugs/show_bug.cgi?id=385593#c1
	public void test385593_1() throws Exception {
		this.runConformTest(
			new String[] {
					"stackmap/StackMapTableFormatError.java",
					"package stackmap;\n" + 
					"\n" + 
					"import java.util.Collection;\n" + 
					"import java.util.Collections;\n" + 
					"\n" + 
					"/**\n" + 
					" * If compiled with Eclipse (compiler target >= 1.6) this snippet causes the\n" + 
					" * error \"java.lang.ClassFormatError: StackMapTable format error: bad\n" + 
					" * verification type\" when executed with JaCoCo code coverage. JaCoCo seems to\n" + 
					" * get confused by unexpected stackmap frames generated by ECJ.\n" + 
					" */\n" + 
					"public class StackMapTableFormatError {\n" + 
					"\n" + 
					"	public static Object exec(Collection<Object> set, Object a,\n" + 
					"			boolean b) {\n" + 
					"		for (Object e : set) {\n" + 
					"			if (a != null && (e == null || b)) {\n" + 
					"				continue;\n" + 
					"			}\n" + 
					"			return null;\n" + 
					"		}\n" + 
					"		return null;\n" + 
					"	}\n" + 
					"	\n" + 
					"	public static void main(String[] args) {\n" + 
					"		exec(Collections.emptySet(), null, false);\n" + 
					"	}\n" + 
					"\n" + 
					"}\n"
			});

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR+File.separator+"stackmap"+File.separator+"StackMapTableFormatError.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
				"  // Method descriptor #15 (Ljava/util/Collection;Ljava/lang/Object;Z)Ljava/lang/Object;\n" + 
				"  // Signature: (Ljava/util/Collection<Ljava/lang/Object;>;Ljava/lang/Object;Z)Ljava/lang/Object;\n" + 
				"  // Stack: 1, Locals: 5\n" + 
				"  public static java.lang.Object exec(java.util.Collection set, java.lang.Object a, boolean b);\n" + 
				"     0  aload_0 [set]\n" + 
				"     1  invokeinterface java.util.Collection.iterator() : java.util.Iterator [18] [nargs: 1]\n" + 
				"     6  astore 4\n" + 
				"     8  goto 36\n" + 
				"    11  aload 4\n" + 
				"    13  invokeinterface java.util.Iterator.next() : java.lang.Object [24] [nargs: 1]\n" + 
				"    18  astore_3 [e]\n" + 
				"    19  aload_1 [a]\n" + 
				"    20  ifnull 34\n" + 
				"    23  aload_3 [e]\n" + 
				"    24  ifnull 36\n" + 
				"    27  iload_2 [b]\n" + 
				"    28  ifeq 34\n" + 
				"    31  goto 36\n" + 
				"    34  aconst_null\n" + 
				"    35  areturn\n" + 
				"    36  aload 4\n" + 
				"    38  invokeinterface java.util.Iterator.hasNext() : boolean [30] [nargs: 1]\n" + 
				"    43  ifne 11\n" + 
				"    46  aconst_null\n" + 
				"    47  areturn\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 16]\n" + 
				"        [pc: 19, line: 17]\n" + 
				"        [pc: 31, line: 18]\n" + 
				"        [pc: 34, line: 20]\n" + 
				"        [pc: 36, line: 16]\n" + 
				"        [pc: 46, line: 22]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 48] local: set index: 0 type: java.util.Collection\n" + 
				"        [pc: 0, pc: 48] local: a index: 1 type: java.lang.Object\n" + 
				"        [pc: 0, pc: 48] local: b index: 2 type: boolean\n" + 
				"        [pc: 19, pc: 36] local: e index: 3 type: java.lang.Object\n" + 
				"      Local variable type table:\n" + 
				"        [pc: 0, pc: 48] local: set index: 0 type: java.util.Collection<java.lang.Object>\n" + 
				"      Stack map table: number of frames 3\n" + 
				"        [pc: 11, full, stack: {}, locals: {java.util.Collection, java.lang.Object, int, _, java.util.Iterator}]\n" + 
				"        [pc: 34, full, stack: {}, locals: {java.util.Collection, java.lang.Object, int, java.lang.Object, java.util.Iterator}]\n" + 
				"        [pc: 36, full, stack: {}, locals: {java.util.Collection, java.lang.Object, int, _, java.util.Iterator}]"; 
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	// from https://bugs.eclipse.org/bugs/show_bug.cgi?id=385593#c11
	public void test385593_2() throws Exception {
		this.runConformTest(
			new String[] {
					"snippet/X.java",
					"package snippet;\n" + 
					"\n" + 
					"\n" + 
					"public class X { \n" + 
					"	private void foo(boolean delete) { \n" + 
					"		 \n" + 
					"		String s = bar(); \n" + 
					"		StringBuffer buffer =new StringBuffer(); \n" + 
					"		 \n" + 
					"		try { \n" + 
					"			 \n" + 
					"			String[] datas = new String[] { \"\" }; \n" + 
					"			Object[] data= new Object[] { s }; \n" + 
					"			try { \n" + 
					"				buffer.append(datas).append(data); \n" + 
					"			} catch (Exception e) { \n" + 
					"				if (e != null) \n" + 
					"					throw e; \n" + 
					"				return; \n" + 
					"			} \n" + 
					"				 \n" + 
					"			if (delete) \n" + 
					"				buffer.delete(0, buffer.length()); \n" + 
					"			 \n" + 
					"		} catch (Exception x) { \n" + 
					"		} finally { \n" + 
					"			buffer = null; \n" + 
					"		} \n" + 
					"	} \n" + 
					"	 \n" + 
					"	String bar() { \n" + 
					"		return \"\"; \n" + 
					"	} \n" + 
					"	 \n" + 
					"	public static void main(String[] args) { \n" + 
					"		new X().foo(false); \n" + 
					"		System.out.println(\"SUCCESS\"); \n" + 
					"	} \n" + 
					"}\n"
			});

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR+File.separator+"snippet"+File.separator+"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
				"Stack map table: number of frames 7\n" + 
				"        [pc: 49, full, stack: {java.lang.Exception}, locals: {snippet.X, int, java.lang.String, java.lang.StringBuffer, java.lang.String[], java.lang.Object[]}]\n" + 
				"        [pc: 59, append: {java.lang.Exception}]\n" + 
				"        [pc: 62, chop 1 local(s)]\n" + 
				"        [pc: 79, full, stack: {java.lang.Exception}, locals: {snippet.X, int, java.lang.String, java.lang.StringBuffer}]\n" + 
				"        [pc: 86, same_locals_1_stack_item, stack: {java.lang.Throwable}]\n" + 
				"        [pc: 93, same]\n" + 
				"        [pc: 95, same]\n"; 
		
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
	// from https://bugs.eclipse.org/bugs/show_bug.cgi?id=394718
	public void test394718() throws Exception {
		this.runConformTest(
			new String[] {
					"X.java",
					"public class X\n" + 
					"{\n" + 
					"	public static Boolean test() throws Exception\n" + 
					"	{\n" + 
					"		try\n" + 
					"		{\n" + 
					"			for (int i = 0; i < 1; i++)\n" + 
					"			{\n" + 
					"				long status = System.currentTimeMillis();\n" + 
					"				if (status < 0)\n" + 
					"					return false;\n" + 
					"				if (status == 1)\n" + 
					"					return false;\n" + 
					"			}\n" + 
					"			\n" + 
					"			return false;\n" + 
					"		}\n" + 
					"		finally\n" + 
					"		{\n" + 
					"			System.currentTimeMillis();\n" + 
					"		}\n" + 
					"	}\n" + 
					"	\n" + 
					"	public static void main(String[] args) throws Exception\n" + 
					"	{\n" + 
					"		System.out.print(\"Starting\");\n" + 
					"		test();\n" + 
					"		System.out.println(\"Done\");\n" + 
					"	}\n" + 
					"}"
			},
			"StartingDone");
	}
	
	// https://bugs.eclipse.org/412076
	public void testBug412076_a() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // using <>
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
		this.runConformTest(
				new String[] {
					"X2.java",
					"import java.util.*;\n" + 
					"\n" + 
					"import org.eclipse.jdt.annotation.NonNullByDefault;\n" + 
					"\n" + 
					"class Y {\n" + 
					"	public Y() { }\n" + 
					"}\n" + 
					"\n" + 
					"@NonNullByDefault\n" + 
					"abstract class X1 {\n" + 
					"\n" + 
					"	private Object a;\n" + 
					"	private Object b;\n" + 
					"	private Object c;\n" + 
					"	private Object d;\n" + 
					"	private Object e;\n" + 
					"	private Object f;\n" + 
					"\n" + 
					"	protected Object name;\n" + 
					"\n" + 
					"	@SuppressWarnings(\"null\")\n" + 
					"	protected X1() {\n" + 
					"		super ();\n" + 
					"	}\n" + 
					"\n" + 
					"}\n" + 
					"public class X2 extends X1 {\n" + 
					"\n" + 
					"\n" + 
					"	public static final int ID = 4711;\n" + 
					"\n" + 
					"	private Object x;\n" + 
					"	private Object y;\n" + 
					"	private Object z;\n" + 
					"\n" + 
					"	private Runnable runable = new Runnable () {\n" + 
					"		@Override\n" + 
					"		public void run () {\n" + 
					"			// whatever\n" + 
					"		}\n" + 
					"	};\n" + 
					"\n" + 
					"	private void init () {\n" + 
					"		final Object selector = new Object ();\n" + 
					"		this.name = new Object ();\n" + 
					"		LinkedList<Character> invalidCharactersList = new LinkedList<> ();\n" + 
					"		char[] invalidCharacters = new char[invalidCharactersList.size ()];\n" + 
					"		for (int i = 0; i < invalidCharacters.length; i++) {\n" + 
					"			invalidCharacters[i] = invalidCharactersList.get (i).charValue ();\n" + 
					"		}\n" + 
					"		Y inputVerifier = new Y();\n" + 
					"	}\n" + 
					"\n" + 
					"}\n",
				},
				"",
				getLibsWithNullAnnotations(),
				true/*flush*/,
				null/*vmArgs*/,
				options,
				null/*requestor*/,
				true/*skipJavac*/);

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X2.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"  // Method descriptor #16 ()V\n" + 
					"  // Stack: 4, Locals: 5\n" + 
					"  private void init();\n" + 
					"     0  new java.lang.Object [32]\n" + 
					"     3  dup\n" + 
					"     4  invokespecial java.lang.Object() [34]\n" + 
					"     7  astore_1 [selector]\n" + 
					"     8  aload_0 [this]\n" + 
					"     9  new java.lang.Object [32]\n" + 
					"    12  dup\n" + 
					"    13  invokespecial java.lang.Object() [34]\n" + 
					"    16  putfield X2.name : java.lang.Object [35]\n" + 
					"    19  new java.util.LinkedList [38]\n" + 
					"    22  dup\n" + 
					"    23  invokespecial java.util.LinkedList() [40]\n" + 
					"    26  astore_2 [invalidCharactersList]\n" + 
					"    27  aload_2 [invalidCharactersList]\n" + 
					"    28  invokevirtual java.util.LinkedList.size() : int [41]\n" + 
					"    31  newarray char [5]\n" + 
					"    33  astore_3 [invalidCharacters]\n" + 
					"    34  iconst_0\n" + 
					"    35  istore 4 [i]\n" + 
					"    37  goto 59\n" + 
					"    40  aload_3 [invalidCharacters]\n" + 
					"    41  iload 4 [i]\n" + 
					"    43  aload_2 [invalidCharactersList]\n" + 
					"    44  iload 4 [i]\n" + 
					"    46  invokevirtual java.util.LinkedList.get(int) : java.lang.Object [45]\n" + 
					"    49  checkcast java.lang.Character [49]\n" + 
					"    52  invokevirtual java.lang.Character.charValue() : char [51]\n" + 
					"    55  castore\n" + 
					"    56  iinc 4 1 [i]\n" + 
					"    59  iload 4 [i]\n" + 
					"    61  aload_3 [invalidCharacters]\n" + 
					"    62  arraylength\n" + 
					"    63  if_icmplt 40\n" + 
					"    66  new Y [55]\n" + 
					"    69  dup\n" + 
					"    70  invokespecial Y() [57]\n" + 
					"    73  astore 4 [inputVerifier]\n" + 
					"    75  return\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 44]\n" + 
					"        [pc: 8, line: 45]\n" + 
					"        [pc: 19, line: 46]\n" + 
					"        [pc: 27, line: 47]\n" + 
					"        [pc: 34, line: 48]\n" + 
					"        [pc: 40, line: 49]\n" + 
					"        [pc: 56, line: 48]\n" + 
					"        [pc: 66, line: 51]\n" + 
					"        [pc: 75, line: 52]\n" + 
					"      Local variable table:\n" + 
					"        [pc: 0, pc: 76] local: this index: 0 type: X2\n" + 
					"        [pc: 8, pc: 76] local: selector index: 1 type: java.lang.Object\n" + 
					"        [pc: 27, pc: 76] local: invalidCharactersList index: 2 type: java.util.LinkedList\n" + 
					"        [pc: 34, pc: 76] local: invalidCharacters index: 3 type: char[]\n" + 
					"        [pc: 37, pc: 66] local: i index: 4 type: int\n" + 
					"        [pc: 75, pc: 76] local: inputVerifier index: 4 type: Y\n" + 
					"      Local variable type table:\n" + 
					"        [pc: 27, pc: 76] local: invalidCharactersList index: 2 type: java.util.LinkedList<java.lang.Character>\n" + 
					"      Stack map table: number of frames 2\n" + 
					"        [pc: 40, full, stack: {}, locals: {X2, java.lang.Object, java.util.LinkedList, char[], int}]\n" + 
					"        [pc: 59, same]\n";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	// https://bugs.eclipse.org/412076
	// yet simplified version - using FieldReference
	public void testBug412076_b() throws Exception {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
		this.runConformTest(
				new String[] {
					"X2.java",
					"import java.util.LinkedList;\n" + 
					"\n" + 
					"import org.eclipse.jdt.annotation.NonNull;\n" + 
					"\n" + 
					"abstract class X1 {\n" + 
					"	protected @NonNull Object name = new Object();\n" + 
					"}\n" + 
					"\n" + 
					"public class X2 extends X1 {\n" + 
					"	void init () {\n" + 
					"		this.name = new Object ();\n" + 
					"		LinkedList<Character> l = new LinkedList<Character> ();\n" + 
					"		char[] cs = new char[l.size ()];\n" + 
					"		for (int i = 0; i < cs.length; i++) {\n" + 
					"			cs[i] = l.get (i).charValue ();\n" + 
					"		}\n" + 
					"		Object o2 = new Object();\n" + 
					"	}\n" + 
					"}\n",
				},
				"",
				getLibsWithNullAnnotations(),
				true/*flush*/,
				null/*vmArgs*/,
				options,
				null/*requestor*/,
				true/*skipJavac*/);

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X2.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"  // Method descriptor #6 ()V\n" + 
					"  // Stack: 4, Locals: 4\n" + 
					"  void init();\n" + 
					"     0  aload_0 [this]\n" + 
					"     1  new java.lang.Object [15]\n" + 
					"     4  dup\n" + 
					"     5  invokespecial java.lang.Object() [17]\n" + 
					"     8  putfield X2.name : java.lang.Object [18]\n" + 
					"    11  new java.util.LinkedList [22]\n" + 
					"    14  dup\n" + 
					"    15  invokespecial java.util.LinkedList() [24]\n" + 
					"    18  astore_1 [l]\n" + 
					"    19  aload_1 [l]\n" + 
					"    20  invokevirtual java.util.LinkedList.size() : int [25]\n" + 
					"    23  newarray char [5]\n" + 
					"    25  astore_2 [cs]\n" + 
					"    26  iconst_0\n" + 
					"    27  istore_3 [i]\n" + 
					"    28  goto 48\n" + 
					"    31  aload_2 [cs]\n" + 
					"    32  iload_3 [i]\n" + 
					"    33  aload_1 [l]\n" + 
					"    34  iload_3 [i]\n" + 
					"    35  invokevirtual java.util.LinkedList.get(int) : java.lang.Object [29]\n" + 
					"    38  checkcast java.lang.Character [33]\n" + 
					"    41  invokevirtual java.lang.Character.charValue() : char [35]\n" + 
					"    44  castore\n" + 
					"    45  iinc 3 1 [i]\n" + 
					"    48  iload_3 [i]\n" + 
					"    49  aload_2 [cs]\n" + 
					"    50  arraylength\n" + 
					"    51  if_icmplt 31\n" + 
					"    54  new java.lang.Object [15]\n" + 
					"    57  dup\n" + 
					"    58  invokespecial java.lang.Object() [17]\n" + 
					"    61  astore_3 [o2]\n" + 
					"    62  return\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 11]\n" + 
					"        [pc: 11, line: 12]\n" + 
					"        [pc: 19, line: 13]\n" + 
					"        [pc: 26, line: 14]\n" + 
					"        [pc: 31, line: 15]\n" + 
					"        [pc: 45, line: 14]\n" + 
					"        [pc: 54, line: 17]\n" + 
					"        [pc: 62, line: 18]\n" + 
					"      Local variable table:\n" + 
					"        [pc: 0, pc: 63] local: this index: 0 type: X2\n" + 
					"        [pc: 19, pc: 63] local: l index: 1 type: java.util.LinkedList\n" + 
					"        [pc: 26, pc: 63] local: cs index: 2 type: char[]\n" + 
					"        [pc: 28, pc: 54] local: i index: 3 type: int\n" + 
					"        [pc: 62, pc: 63] local: o2 index: 3 type: java.lang.Object\n" + 
					"      Local variable type table:\n" + 
					"        [pc: 19, pc: 63] local: l index: 1 type: java.util.LinkedList<java.lang.Character>\n" + 
					"      Stack map table: number of frames 2\n" + 
					"        [pc: 31, append: {java.util.LinkedList, char[], int}]\n" + 
					"        [pc: 48, same]\n" + 
					"}";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}

	// https://bugs.eclipse.org/412076
	// yet simplified version - using SingleNameReference
	public void testBug412076_c() throws Exception {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
		this.runConformTest(
				new String[] {
					"X2.java",
					"import java.util.LinkedList;\n" + 
					"\n" + 
					"import org.eclipse.jdt.annotation.NonNull;\n" + 
					"\n" + 
					"abstract class X1 {\n" + 
					"	protected @NonNull Object name = new Object();\n" + 
					"}\n" + 
					"\n" + 
					"public class X2 extends X1 {\n" + 
					"	void init () {\n" + 
					"		name = new Object ();\n" + 
					"		LinkedList<Character> l = new LinkedList<Character> ();\n" + 
					"		char[] cs = new char[l.size ()];\n" + 
					"		for (int i = 0; i < cs.length; i++) {\n" + 
					"			cs[i] = l.get (i).charValue ();\n" + 
					"		}\n" + 
					"		Object o2 = new Object();\n" + 
					"	}\n" + 
					"}\n",
				},
				"",
				getLibsWithNullAnnotations(),
				true/*flush*/,
				null/*vmArgs*/,
				options,
				null/*requestor*/,
				true/*skipJavac*/);

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X2.class"));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"  // Method descriptor #6 ()V\n" + 
					"  // Stack: 4, Locals: 4\n" + 
					"  void init();\n" + 
					"     0  aload_0 [this]\n" + 
					"     1  new java.lang.Object [15]\n" + 
					"     4  dup\n" + 
					"     5  invokespecial java.lang.Object() [17]\n" + 
					"     8  putfield X2.name : java.lang.Object [18]\n" + 
					"    11  new java.util.LinkedList [22]\n" + 
					"    14  dup\n" + 
					"    15  invokespecial java.util.LinkedList() [24]\n" + 
					"    18  astore_1 [l]\n" + 
					"    19  aload_1 [l]\n" + 
					"    20  invokevirtual java.util.LinkedList.size() : int [25]\n" + 
					"    23  newarray char [5]\n" + 
					"    25  astore_2 [cs]\n" + 
					"    26  iconst_0\n" + 
					"    27  istore_3 [i]\n" + 
					"    28  goto 48\n" + 
					"    31  aload_2 [cs]\n" + 
					"    32  iload_3 [i]\n" + 
					"    33  aload_1 [l]\n" + 
					"    34  iload_3 [i]\n" + 
					"    35  invokevirtual java.util.LinkedList.get(int) : java.lang.Object [29]\n" + 
					"    38  checkcast java.lang.Character [33]\n" + 
					"    41  invokevirtual java.lang.Character.charValue() : char [35]\n" + 
					"    44  castore\n" + 
					"    45  iinc 3 1 [i]\n" + 
					"    48  iload_3 [i]\n" + 
					"    49  aload_2 [cs]\n" + 
					"    50  arraylength\n" + 
					"    51  if_icmplt 31\n" + 
					"    54  new java.lang.Object [15]\n" + 
					"    57  dup\n" + 
					"    58  invokespecial java.lang.Object() [17]\n" + 
					"    61  astore_3 [o2]\n" + 
					"    62  return\n" + 
					"      Line numbers:\n" + 
					"        [pc: 0, line: 11]\n" + 
					"        [pc: 11, line: 12]\n" + 
					"        [pc: 19, line: 13]\n" + 
					"        [pc: 26, line: 14]\n" + 
					"        [pc: 31, line: 15]\n" + 
					"        [pc: 45, line: 14]\n" + 
					"        [pc: 54, line: 17]\n" + 
					"        [pc: 62, line: 18]\n" + 
					"      Local variable table:\n" + 
					"        [pc: 0, pc: 63] local: this index: 0 type: X2\n" + 
					"        [pc: 19, pc: 63] local: l index: 1 type: java.util.LinkedList\n" + 
					"        [pc: 26, pc: 63] local: cs index: 2 type: char[]\n" + 
					"        [pc: 28, pc: 54] local: i index: 3 type: int\n" + 
					"        [pc: 62, pc: 63] local: o2 index: 3 type: java.lang.Object\n" + 
					"      Local variable type table:\n" + 
					"        [pc: 19, pc: 63] local: l index: 1 type: java.util.LinkedList<java.lang.Character>\n" + 
					"      Stack map table: number of frames 2\n" + 
					"        [pc: 31, append: {java.util.LinkedList, char[], int}]\n" + 
					"        [pc: 48, same]\n" + 
					"}";

			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 2));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
	}
}
