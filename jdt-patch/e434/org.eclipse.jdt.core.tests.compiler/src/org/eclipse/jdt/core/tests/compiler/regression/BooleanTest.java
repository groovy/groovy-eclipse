/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
import junit.framework.Test;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

@SuppressWarnings({ "rawtypes" })
public class BooleanTest extends AbstractRegressionTest {

public BooleanTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  public Object getAccessibleSelection(int i) {\n" +
		"    int c, d;\n" +
		"    if ((this == null) || ((d = 4) > 0)) {\n" +
		"      c = 2;\n" +
		"    }\n" +
		"    else {\n" +
		"      if (this == null) {\n" +
		"        c = 3;\n" +
		"        i++;\n" +
		"      }\n" +
		"      i++;\n" +
		"    }\n" +
		"    return null;\n" +
		"  }\n" +
		"  public String getAccessibleSelection2(int i) {\n" +
		"    int c, d;\n" +
		"    return ((this == null) || ((d = 4) > 0))\n" +
		"      ? String.valueOf(c = 2)\n" +
		"      : String.valueOf(i++); \n" +
		"  }\n" +
		"}\n",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/H.java",
		"package p;\n" +
		"public class H {\n" +
		"  Thread fPeriodicSaveThread;\n" +
		"  public void bar() {\n" +
		"    int a = 0, b = 0;\n" +
		"    if (a == 0 || (b = 2) == 2) {\n" +
		"      //a = 1;\n" +
		"    }\n" +
		"    System.out.println(b);\n" +
		"    if (b != 0) {\n" +
		"      System.err.println(\"<bar>b should be equal to 0.\");\n" +
		"      System.exit(-1);\n" +
		"    }\n" +
		"  }\n" +
		"  public void bar2() {\n" +
		"    int a = 0, b = 0;\n" +
		"    if (a == 1 && (b = 2) == 2) {\n" +
		"      //a = 1;\n" +
		"    }\n" +
		"    System.out.println(b);\n" +
		"    if (b != 0) {\n" +
		"      System.err.println(\"<bar2>b should be equal to 0.\");\n" +
		"      System.exit(-1);\n" +
		"    }\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new H().bar();\n" +
		"    new H().bar2();\n" +
		"  }\n" +
		"}\n",
	});
}
public void test003() {
	this.runConformTest(new String[] {
		"p/I.java",
		"package p;\n" +
		"/**\n" +
		" * This test0 should run without producing a java.lang.ClassFormatError\n" +
		" */\n" +
		"public class I {\n" +
		"  public static void main(String[] args) {\n" +
		"    int i = 1, j;\n" +
		"    if (((i > 0) || ((j = 10) > j--)) && (i < 12)) {\n" +
		"      System.out.println(i);\n" +
		"    }\n" +
		"  }\n" +
		"  public static void main1(String[] args) {\n" +
		"    int i = 1, j;\n" +
		"    if (((i < 12) && ((j = 10) > j--)) || (i > 0)) {\n" +
		"      System.out.println(i);\n" +
		"    }\n" +
		"  }\n" +
		"  public static void main2(String[] args) {\n" +
		"    int i = 1, j;\n" +
		"    if (((i < 12) && ((j = 10) > j--)) && (i > 0)) {\n" +
		"      System.out.println(i);\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}
public void test004() {
	this.runConformTest(new String[] {
		"p/J.java",
		"package p;\n" +
		"/**\n" +
		" * This test0 should run without producing a java.lang.ClassFormatError\n" +
		" */\n" +
		"public class J {\n" +
		"  public static void main(String[] args) {\n" +
		"    int i = 1, j;\n" +
		"    if (((i > 0) || ((j = 10) > j--)) && (i < 12)) {\n" +
		"      System.out.println(i);\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}

public void test005() {
	this.runConformTest(new String[] {
		"p/M.java",
		"package p;\n" +
		"public class M {\n" +
		"  public static void main(String[] args) {\n" +
		"    int a = 0, b = 0;\n" +
		"    if (a == 0 || (b = 2) == 2) {\n" +
		"    }\n" +
		"    if (b != 0) {\n" +
		"      System.out.println(\"b should be equal to zero\");\n" +
		"      System.exit(-1);\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}

public void test006() {
	this.runConformTest(new String[] {
		"p/Q.java",
		"package p;\n" +
		"/**\n" +
		" * This test0 should run without producing a java.lang.VerifyError\n" +
		" */\n" +
		"public class Q {\n" +
		"  boolean bar() {\n" +
		"    if (false && foo()) {\n" +
		"      return true;\n" +
		"    }\n" +
		"    return false;\n" +
		"  }\n" +
		"  boolean foo() {\n" +
		"    return true;\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new Q().bar();\n" +
		"  }\n" +
		"}\n",
	});
}

// Bug 6596
public void test007() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	Object t;\n" +
			"	public static void main(String args[]) {\n" +
			"		new Test().testMethod();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	private void testMethod(){\n" +
			"		boolean a = false;\n" +
			"		boolean b = false;\n" +
			"		if (!(a&&b)){}\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");
}
// Bug 6596
public void test008() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	Object t;\n" +
			"	public static void main(String args[]) {\n" +
			"		new Test().testMethod();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	private void testMethod(){\n" +
			"		boolean a = false;\n" +
			"		boolean b = false;\n" +
			"		if (!(a||b)){}\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");
}
// Bug 6596
public void test009() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	Object t;\n" +
			"	public static void main(String args[]) {\n" +
			"		new Test().testMethod();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	private void testMethod(){\n" +
			"		final boolean a = false;\n" +
			"		boolean b = false;\n" +
			"		if (!(a&&b)){}\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");
}

// Bug 6596
public void test010() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	Object t;\n" +
			"	public static void main(String args[]) {\n" +
			"		new Test().testMethod();\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	private void testMethod(){\n" +
			"		boolean a = false;\n" +
			"		boolean b = false;\n" +
			"		if (a == b){}\n" +
			"	}\n" +
			"}\n",
		},
		"SUCCESS");
}

// Bug 46675
public void test011() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = null;\n" +
			"		boolean b = s != null && (s.length() == 0 ? TestConst.c1 : TestConst.c2);\n" +
			"		if (!b) System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"\n" +
			"	public static class TestConst {\n" +
			"		public static final boolean c1 = true;\n" +
			"		public static final boolean c2 = true;\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}

// Bug 46675 - variation
public void test012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = \"aaa\";\n" +
			"		boolean b = s != null && (s.length() == 0 ? TestConst.c1 : TestConst.c2);\n" +
			"		if (b) System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"\n" +
			"	public static class TestConst {\n" +
			"		public static final boolean c1 = true;\n" +
			"		public static final boolean c2 = true;\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}

// Bug 46675 - variation
public void test013() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = \"aaa\";\n" +
			"		boolean b = s == null || (s.length() == 0 ? TestConst.c1 : TestConst.c2);\n" +
			"		if (!b) System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"\n" +
			"	public static class TestConst {\n" +
			"		public static final boolean c1 = false;\n" +
			"		public static final boolean c2 = false;\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}

// Bug 47881
public void test014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X  {\n" +
			"\n" +
			"    public static void main(String args[]) {\n" +
			"		boolean b = true;\n" +
			"		b = b && false;                 \n" +
			"		if (b) {\n" +
			"			System.out.println(\"FAILED\");\n" +
			"		} else {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"    }\n" +
			"}\n" +
			"\n",
		},
		"SUCCESS");
}

// Bug 47881 - variation
public void test015() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X  {\n" +
			"\n" +
			"    public static void main(String args[]) {\n" +
			"		boolean b = true;\n" +
			"		b = b || true;                 \n" +
			"		if (b) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		} else {\n" +
			"			System.out.println(\"FAILED\");\n" +
			"		}\n" +
			"    }\n" +
			"}\n" +
			"\n",
		},
		"SUCCESS");
}
// Bug 47881 - variation
public void test016() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X  {\n" +
			"\n" +
			"    public static void main(String args[]) {\n" +
			"		boolean b = false;\n" +
			"		b = b && true;                 \n" +
			"		if (b) {\n" +
			"			System.out.println(\"FAILED\");\n" +
			"		} else {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"    }\n" +
			"}\n" +
			"\n",
		},
		"SUCCESS");
}

// Bug 47881 - variation
public void test017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X  {\n" +
			"\n" +
			"    public static void main(String args[]) {\n" +
			"		boolean b = true;\n" +
			"		b = b || false;                 \n" +
			"		if (b) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		} else {\n" +
			"			System.out.println(\"FAILED\");\n" +
			"		}\n" +
			"    }\n" +
			"}\n" +
			"\n",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120
public void test018() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0++) || true) != ((true && true) && (!(false || true)))));\n" +
			"  }\n" +
			"}\n",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 3, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  getstatic X.f0 : float [26]\n" +
		"    10  fconst_1\n" +
		"    11  fadd\n" +
		"    12  putstatic X.f0 : float [26]\n" +
		"    15  iconst_1\n" +
		"    16  invokevirtual java.io.PrintStream.println(boolean) : void [28]\n" +
		"    19  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 16, line: 8]\n" +
		"        [pc: 19, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 20] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 20] local: l11 index: 1 type: long\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test019() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0++) || false) != true));\n" +
			"  }\n" +
			"}\n",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 5, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  lload_1 [l11]\n" +
		"     8  l2f\n" +
		"     9  getstatic X.f0 : float [26]\n" +
		"    12  dup\n" +
		"    13  fconst_1\n" +
		"    14  fadd\n" +
		"    15  putstatic X.f0 : float [26]\n" +
		"    18  fcmpg\n" +
		"    19  ifge 26\n" +
		"    22  iconst_0\n" +
		"    23  goto 27\n" +
		"    26  iconst_1\n" +
		"    27  invokevirtual java.io.PrintStream.println(boolean) : void [28]\n" +
		"    30  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 27, line: 8]\n" +
		"        [pc: 30, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 31] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 31] local: l11 index: 1 type: long\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test020() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0) | true) != false));\n" +
			"  }\n" +
			"}\n",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  iconst_1\n" +
		"     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]\n" +
		"    11  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 8, line: 8]\n" +
		"        [pc: 11, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 12] local: l11 index: 1 type: long\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test021() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0) && false) != true));\n" +
			"  }\n" +
			"}\n",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  iconst_1\n" +
		"     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]\n" +
		"    11  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 8, line: 8]\n" +
		"        [pc: 11, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 12] local: l11 index: 1 type: long\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test022() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0) & false) != true));\n" +
			"  }\n" +
			"}\n",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  iconst_1\n" +
		"     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]\n" +
		"    11  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 8, line: 8]\n" +
		"        [pc: 11, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 12] local: l11 index: 1 type: long\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120
public void test023() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0++) || true) == ((true && true) && (!(false || true)))));\n" +
			"  }\n" +
			"}\n",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 3, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  getstatic X.f0 : float [26]\n" +
		"    10  fconst_1\n" +
		"    11  fadd\n" +
		"    12  putstatic X.f0 : float [26]\n" +
		"    15  iconst_0\n" +
		"    16  invokevirtual java.io.PrintStream.println(boolean) : void [28]\n" +
		"    19  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 16, line: 8]\n" +
		"        [pc: 19, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 20] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 20] local: l11 index: 1 type: long\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test024() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0++) || false) == true));\n" +
			"  }\n" +
			"}\n",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 5, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  lload_1 [l11]\n" +
		"     8  l2f\n" +
		"     9  getstatic X.f0 : float [26]\n" +
		"    12  dup\n" +
		"    13  fconst_1\n" +
		"    14  fadd\n" +
		"    15  putstatic X.f0 : float [26]\n" +
		"    18  fcmpg\n" +
		"    19  ifge 26\n" +
		"    22  iconst_1\n" +
		"    23  goto 27\n" +
		"    26  iconst_0\n" +
		"    27  invokevirtual java.io.PrintStream.println(boolean) : void [28]\n" +
		"    30  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 27, line: 8]\n" +
		"        [pc: 30, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 31] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 31] local: l11 index: 1 type: long\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test025() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0) | true) == false));\n" +
			"  }\n" +
			"}\n",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  iconst_0\n" +
		"     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]\n" +
		"    11  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 8, line: 8]\n" +
		"        [pc: 11, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 12] local: l11 index: 1 type: long\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test026() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0) && false) == true));\n" +
			"  }\n" +
			"}\n",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  iconst_0\n" +
		"     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]\n" +
		"    11  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 8, line: 8]\n" +
		"        [pc: 11, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 12] local: l11 index: 1 type: long\n" +
		"}";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test027() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0) & false) == true));\n" +
			"  }\n" +
			"}\n",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  iconst_0\n" +
		"     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]\n" +
		"    11  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 8, line: 8]\n" +
		"        [pc: 11, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 12] local: l11 index: 1 type: long\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test028() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0) || true) == false));\n" +
			"  }\n" +
			"}\n",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  iconst_0\n" +
		"     8  invokevirtual java.io.PrintStream.println(boolean) : void [26]\n" +
		"    11  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 8, line: 8]\n" +
		"        [pc: 11, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 12] local: l11 index: 1 type: long\n";

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

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test029() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"   	System.out.println(\n" +
			"   			((foo() || bar()) || true) && false); 		\n" +
			"  }\n" +
			"  static boolean foo(){ \n" +
			"	  System.out.print(\"foo\");\n" +
			"	  return false;\n" +
			"  }\n" +
			"  static boolean bar(){\n" +
			"	  System.out.print(\"bar\");\n" +
			"	  return true;\n" +
			"  }\n" +
			"}\n",
		},
		"foobarfalse");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  getstatic java.lang.System.out : java.io.PrintStream [18]\n" +
		"     3  invokestatic X.foo() : boolean [24]\n" +
		"     6  ifne 13\n" +
		"     9  invokestatic X.bar() : boolean [28]\n" +
		"    12  pop\n" +
		"    13  iconst_0\n" +
		"    14  invokevirtual java.io.PrintStream.println(boolean) : void [31]\n" +
		"    17  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 3, line: 7]\n" +
		"        [pc: 14, line: 6]\n" +
		"        [pc: 17, line: 8]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 18] local: args index: 0 type: java.lang.String[]\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117120 - variation
public void test030() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static float f0;\n" +
			"  \n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    \n" +
			"    System.out.println(\n" +
			"        (((l11 < f0++) || true) == ((foo() || bar()) || true)));\n" +
			"  }\n" +
			"  static boolean foo() {\n" +
			"	  System.out.print(\"foo\");\n" +
			"	  return false;\n" +
			"  }\n" +
			"  static boolean bar() {\n" +
			"	  System.out.print(\"bar\");\n" +
			"	  return true;\n" +
			"  }\n" +
			"}\n",
		},
		"foobartrue");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 3, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  ldc2_w <Long -26> [18]\n" +
		"     3  lstore_1 [l11]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
		"     7  getstatic X.f0 : float [26]\n" +
		"    10  fconst_1\n" +
		"    11  fadd\n" +
		"    12  putstatic X.f0 : float [26]\n" +
		"    15  invokestatic X.foo() : boolean [28]\n" +
		"    18  ifne 25\n" +
		"    21  invokestatic X.bar() : boolean [32]\n" +
		"    24  pop\n" +
		"    25  iconst_1\n" +
		"    26  invokevirtual java.io.PrintStream.println(boolean) : void [35]\n" +
		"    29  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 4, line: 8]\n" +
		"        [pc: 7, line: 9]\n" +
		"        [pc: 26, line: 8]\n" +
		"        [pc: 29, line: 10]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 30] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 30] local: l11 index: 1 type: long\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117451
public void test031() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public float f0;\n" +
			"\n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    X x = new X();\n" +
			"    System.out.println(\n" +
			"        (((l11 < x.f0) || true) != false));\n" +
			"  }\n" +
			"}\n",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput = this.complianceLevel == ClassFileConstants.JDK1_3
		?	"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  ldc2_w <Long -26> [18]\n" +
			"     3  lstore_1 [l11]\n" +
			"     4  new X [1]\n" +
			"     7  dup\n" +
			"     8  invokespecial X() [20]\n" +
			"    11  astore_3 [x]\n" +
			"    12  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
			"    15  aload_3 [x]\n" +
			"    16  invokevirtual java.lang.Object.getClass() : java.lang.Class [27]\n" +
			"    19  pop\n" +
			"    20  iconst_1\n" +
			"    21  invokevirtual java.io.PrintStream.println(boolean) : void [31]\n" +
			"    24  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 4, line: 7]\n" +
			"        [pc: 12, line: 8]\n" +
			"        [pc: 15, line: 9]\n" +
			"        [pc: 21, line: 8]\n" +
			"        [pc: 24, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 25] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 4, pc: 25] local: l11 index: 1 type: long\n" +
			"        [pc: 12, pc: 25] local: x index: 3 type: X\n"

		:	"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  ldc2_w <Long -26> [18]\n" +
			"     3  lstore_1 [l11]\n" +
			"     4  new X [1]\n" +
			"     7  dup\n" +
			"     8  invokespecial X() [20]\n" +
			"    11  astore_3 [x]\n" +
			"    12  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
			"    15  aload_3 [x]\n" +
			"    16  getfield X.f0 : float [27]\n" +
			"    19  pop\n" +
			"    20  iconst_1\n" +
			"    21  invokevirtual java.io.PrintStream.println(boolean) : void [29]\n" +
			"    24  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 4, line: 7]\n" +
			"        [pc: 12, line: 8]\n" +
			"        [pc: 15, line: 9]\n" +
			"        [pc: 21, line: 8]\n" +
			"        [pc: 24, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 25] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 4, pc: 25] local: l11 index: 1 type: long\n" +
			"        [pc: 12, pc: 25] local: x index: 3 type: X\n";


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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117451 - variation
public void test032() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  static float f0;\n" +
			"\n" +
			"  public static void main(String[] args)\n" +
			"  {\n" +
			"    long l11 = -26;\n" +
			"    System.out.println(\n" +
			"        (((l11 < (f0=13)) || true) != false));\n" +
			"  }\n" +
			"}\n",
		},
		"true");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
			"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  ldc2_w <Long -26> [18]\n" +
			"     3  lstore_1 [l11]\n" +
			"     4  getstatic java.lang.System.out : java.io.PrintStream [20]\n" +
			"     7  ldc <Float 13.0> [26]\n" +
			"     9  putstatic X.f0 : float [27]\n" +
			"    12  iconst_1\n" +
			"    13  invokevirtual java.io.PrintStream.println(boolean) : void [29]\n" +
			"    16  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 4, line: 7]\n" +
			"        [pc: 7, line: 8]\n" +
			"        [pc: 13, line: 7]\n" +
			"        [pc: 16, line: 9]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 17] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 4, pc: 17] local: l11 index: 1 type: long\n";

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

public void test033() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = true;\n" +
			"		System.out.print(b ^ b);\n" +
			"		System.out.println(b ^ true);\n" +
			"	} \n" +
			"}\n",
		},
		"falsefalse");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 3, Locals: 2\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  istore_1 [b]\n" +
		"     2  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"     5  iload_1 [b]\n" +
		"     6  iload_1 [b]\n" +
		"     7  ixor\n" +
		"     8  invokevirtual java.io.PrintStream.print(boolean) : void [22]\n" +
		"    11  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    14  iload_1 [b]\n" +
		"    15  iconst_1\n" +
		"    16  ixor\n" +
		"    17  invokevirtual java.io.PrintStream.println(boolean) : void [28]\n" +
		"    20  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 2, line: 4]\n" +
		"        [pc: 11, line: 5]\n" +
		"        [pc: 20, line: 6]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 21] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 2, pc: 21] local: b index: 1 type: boolean\n";

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
public void test034() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b = true;\n" +
			"		if ((b ^ true) || b) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
			"	} \n" +
			"}\n",
		},
		"SUCCESS");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 2\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  istore_1 [b]\n" +
		"     2  iload_1 [b]\n" +
		"     3  ifeq 10\n" +
		"     6  iload_1 [b]\n" +
		"     7  ifeq 18\n" +
		"    10  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    13  ldc <String \"SUCCESS\"> [22]\n" +
		"    15  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    18  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 2, line: 4]\n" +
		"        [pc: 10, line: 5]\n" +
		"        [pc: 18, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 19] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 2, pc: 19] local: b index: 1 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117451 - variation
public void test035() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static float f0;\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println((X.f0 > 0 || true) == false);\n" +
			"	} \n" +
			"}\n",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"    0  getstatic java.lang.System.out : java.io.PrintStream [18]\n" +
		"    3  iconst_0\n" +
		"    4  invokevirtual java.io.PrintStream.println(boolean) : void [24]\n" +
		"    7  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 7, line: 5]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 8] local: args index: 0 type: java.lang.String[]\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=117451 - variation
public void test036() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	float f0;\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().foo();\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		System.out.println((this.f0 > 0 || true) == false);\n" +
			"	} \n" +
			"}\n",
		},
		"false");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #8 ()V\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  void foo();\n" +
		"    0  getstatic java.lang.System.out : java.io.PrintStream [24]\n" +
		"    3  iconst_0\n" +
		"    4  invokevirtual java.io.PrintStream.println(boolean) : void [30]\n" +
		"    7  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 7]\n" +
		"        [pc: 7, line: 8]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 8] local: this index: 0 type: X\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=147024
public void test037() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			" public class X {\n" +
			" public static final boolean T = true;\n" +
			"	public static final boolean F = false;\n" +
			"	\n" +
			"	public boolean getFlagBT() {\n" +
			"		boolean b = this.T;\n" +
			"		if (this.T)\n" +
			"			return true;\n" +
			"		else\n" +
			"			return false;\n" +
			"	}\n" +
			"\n" +
			"	public int getFlagIT() {\n" +
			"		boolean b = this.T;\n" +
			"		if (this.T)\n" +
			"			return 0;\n" +
			"		else\n" +
			"			return 1;\n" +
			"	}\n" +
			"\n" +
			"	public boolean getFlagBF() {\n" +
			"		boolean b = this.F;\n" +
			"		if (this.F)\n" +
			"			return true;\n" +
			"		else\n" +
			"			return false;\n" +
			"	}\n" +
			"\n" +
			"	public int getFlagIF() {\n" +
			"		boolean b = this.F;\n" +
			"		if (this.F)\n" +
			"			return 0;\n" +
			"		else\n" +
			"			return 1;\n" +
			"	}\n" +
			"	public boolean getFlagBT2() {\n" +
			"		boolean b = T;\n" +
			"		if (T)\n" +
			"			return true;\n" +
			"		else\n" +
			"			return false;\n" +
			"	}\n" +
			"\n" +
			"	public int getFlagIT2() {\n" +
			"		boolean b = T;\n" +
			"		if (T)\n" +
			"			return 0;\n" +
			"		else\n" +
			"			return 1;\n" +
			"	}\n" +
			"\n" +
			"	public boolean getFlagBF2() {\n" +
			"		boolean b = F;\n" +
			"		if (F)\n" +
			"			return true;\n" +
			"		else\n" +
			"			return false;\n" +
			"	}\n" +
			"\n" +
			"	public int getFlagIF2() {\n" +
			"		boolean b = F;\n" +
			"		if (F)\n" +
			"			return 0;\n" +
			"		else\n" +
			"			return 1;\n" +
			"	}\n" +
			"	public boolean getFlagBT3() {\n" +
			"		X self = this;\n" +
			"		boolean b = self.T;\n" +
			"		if (self.T)\n" +
			"			return true;\n" +
			"		else\n" +
			"			return false;\n" +
			"	}\n" +
			"\n" +
			"	public int getFlagIT3() {\n" +
			"		X self = this;\n" +
			"		boolean b = self.T;\n" +
			"		if (self.T)\n" +
			"			return 0;\n" +
			"		else\n" +
			"			return 1;\n" +
			"	}\n" +
			"\n" +
			"	public boolean getFlagBF3() {\n" +
			"		X self = this;\n" +
			"		boolean b = self.F;\n" +
			"		if (self.F)\n" +
			"			return true;\n" +
			"		else\n" +
			"			return false;\n" +
			"	}\n" +
			"	public int getFlagIF3() {\n" +
			"		X self = this;\n" +
			"		boolean b = self.F;\n" +
			"		if (self.F)\n" +
			"			return 0;\n" +
			"		else\n" +
			"			return 1;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"It worked.\");\n" +
			"	}\n" +
			"}", // =================
		},
		"It worked.");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #21 ()Z\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  public boolean getFlagBT();\n" +
		"    0  iconst_1\n" +
		"    1  istore_1 [b]\n" +
		"    2  iconst_1\n" +
		"    3  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 2, line: 8]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 4] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 4] local: b index: 1 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #24 ()I\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  public int getFlagIT();\n" +
		"    0  iconst_1\n" +
		"    1  istore_1 [b]\n" +
		"    2  iconst_0\n" +
		"    3  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 14]\n" +
		"        [pc: 2, line: 16]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 4] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 4] local: b index: 1 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #21 ()Z\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  public boolean getFlagBF();\n" +
		"    0  iconst_0\n" +
		"    1  istore_1 [b]\n" +
		"    2  iconst_0\n" +
		"    3  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 22]\n" +
		"        [pc: 2, line: 26]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 4] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 4] local: b index: 1 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #24 ()I\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  public int getFlagIF();\n" +
		"    0  iconst_0\n" +
		"    1  istore_1 [b]\n" +
		"    2  iconst_1\n" +
		"    3  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 30]\n" +
		"        [pc: 2, line: 34]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 4] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 4] local: b index: 1 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #21 ()Z\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  public boolean getFlagBT2();\n" +
		"    0  iconst_1\n" +
		"    1  istore_1 [b]\n" +
		"    2  iconst_1\n" +
		"    3  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 37]\n" +
		"        [pc: 2, line: 39]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 4] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 4] local: b index: 1 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #24 ()I\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  public int getFlagIT2();\n" +
		"    0  iconst_1\n" +
		"    1  istore_1 [b]\n" +
		"    2  iconst_0\n" +
		"    3  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 45]\n" +
		"        [pc: 2, line: 47]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 4] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 4] local: b index: 1 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #21 ()Z\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  public boolean getFlagBF2();\n" +
		"    0  iconst_0\n" +
		"    1  istore_1 [b]\n" +
		"    2  iconst_0\n" +
		"    3  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 53]\n" +
		"        [pc: 2, line: 57]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 4] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 4] local: b index: 1 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #24 ()I\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  public int getFlagIF2();\n" +
		"    0  iconst_0\n" +
		"    1  istore_1 [b]\n" +
		"    2  iconst_1\n" +
		"    3  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 61]\n" +
		"        [pc: 2, line: 65]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 4] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 4] local: b index: 1 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #21 ()Z\n" +
		"  // Stack: 1, Locals: 3\n" +
		"  public boolean getFlagBT3();\n" +
		"    0  aload_0 [this]\n" +
		"    1  astore_1 [self]\n" +
		"    2  iconst_1\n" +
		"    3  istore_2 [b]\n" +
		"    4  iconst_1\n" +
		"    5  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 68]\n" +
		"        [pc: 2, line: 69]\n" +
		"        [pc: 4, line: 71]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 6] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 6] local: self index: 1 type: X\n" +
		"        [pc: 4, pc: 6] local: b index: 2 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #24 ()I\n" +
		"  // Stack: 1, Locals: 3\n" +
		"  public int getFlagIT3();\n" +
		"    0  aload_0 [this]\n" +
		"    1  astore_1 [self]\n" +
		"    2  iconst_1\n" +
		"    3  istore_2 [b]\n" +
		"    4  iconst_0\n" +
		"    5  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 77]\n" +
		"        [pc: 2, line: 78]\n" +
		"        [pc: 4, line: 80]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 6] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 6] local: self index: 1 type: X\n" +
		"        [pc: 4, pc: 6] local: b index: 2 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #21 ()Z\n" +
		"  // Stack: 1, Locals: 3\n" +
		"  public boolean getFlagBF3();\n" +
		"    0  aload_0 [this]\n" +
		"    1  astore_1 [self]\n" +
		"    2  iconst_0\n" +
		"    3  istore_2 [b]\n" +
		"    4  iconst_0\n" +
		"    5  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 86]\n" +
		"        [pc: 2, line: 87]\n" +
		"        [pc: 4, line: 91]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 6] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 6] local: self index: 1 type: X\n" +
		"        [pc: 4, pc: 6] local: b index: 2 type: boolean\n" +
		"  \n" +
		"  // Method descriptor #24 ()I\n" +
		"  // Stack: 1, Locals: 3\n" +
		"  public int getFlagIF3();\n" +
		"    0  aload_0 [this]\n" +
		"    1  astore_1 [self]\n" +
		"    2  iconst_0\n" +
		"    3  istore_2 [b]\n" +
		"    4  iconst_1\n" +
		"    5  ireturn\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 94]\n" +
		"        [pc: 2, line: 95]\n" +
		"        [pc: 4, line: 99]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 6] local: this index: 0 type: X\n" +
		"        [pc: 2, pc: 6] local: self index: 1 type: X\n" +
		"        [pc: 4, pc: 6] local: b index: 2 type: boolean\n";

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
public void test038() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			" public class X {\n" +
			"	static boolean foo() { System.out.print(\"[foo]\"); return false; }\n" +
			"	static boolean bar() { System.out.print(\"[bar]\"); return true; }\n" +
			"	public static void main(String[] args) {\n" +
			"		if ((foo() || bar()) && false) {\n" +
			"			return;\n" +
			"		}\n" +
			"		System.out.println(\"[done]\");\n" +
			"	}\n" +
			"}", // =================
		},
		"[foo][bar][done]");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #34 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  invokestatic X.foo() : boolean [35]\n" +
		"     3  ifne 10\n" +
		"     6  invokestatic X.bar() : boolean [37]\n" +
		"     9  pop\n" +
		"    10  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    13  ldc <String \"[done]\"> [39]\n" +
		"    15  invokevirtual java.io.PrintStream.println(java.lang.String) : void [41]\n" +
		"    18  return\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162965
public void test039() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                boolean a = true, b;\n" +
			"                if (a ? false : (b = true))\n" +
			"                        a = b;\n" +
			"                System.out.println(\"SUCCESS\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  istore_1 [a]\n" +
		"     2  iload_1 [a]\n" +
		"     3  ifeq 9\n" +
		"     6  goto 17\n" +
		"     9  iconst_1\n" +
		"    10  dup\n" +
		"    11  istore_2 [b]\n" +
		"    12  ifeq 17\n" +
		"    15  iload_2 [b]\n" +
		"    16  istore_1 [a]\n" +
		"    17  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    20  ldc <String \"SUCCESS\"> [22]\n" +
		"    22  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    25  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 2, line: 4]\n" +
		"        [pc: 15, line: 5]\n" +
		"        [pc: 17, line: 6]\n" +
		"        [pc: 25, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 26] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 2, pc: 26] local: a index: 1 type: boolean\n" +
		"        [pc: 12, pc: 17] local: b index: 2 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162965 - variation
public void test040() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                boolean a = true, b = false;\n" +
			"                if (!(a ? true : (b = true)))\n" +
			"                        a = b;\n" +
			"                System.out.println(\"SUCCESS\");\n" +
			"        }\n" +
			"}\n", // =================
		},
		"SUCCESS");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  istore_1 [a]\n" +
		"     2  iconst_0\n" +
		"     3  istore_2 [b]\n" +
		"     4  iload_1 [a]\n" +
		"     5  ifeq 11\n" +
		"     8  goto 19\n" +
		"    11  iconst_1\n" +
		"    12  dup\n" +
		"    13  istore_2 [b]\n" +
		"    14  ifne 19\n" +
		"    17  iload_2 [b]\n" +
		"    18  istore_1 [a]\n" +
		"    19  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    22  ldc <String \"SUCCESS\"> [22]\n" +
		"    24  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    27  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 4, line: 4]\n" +
		"        [pc: 17, line: 5]\n" +
		"        [pc: 19, line: 6]\n" +
		"        [pc: 27, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 28] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 2, pc: 28] local: a index: 1 type: boolean\n" +
		"        [pc: 4, pc: 28] local: b index: 2 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162965 - variation
public void test041() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean a = true, b = false;\n" +
			"		if (a ? true : (b = false))\n" +
			"			a = b;\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n", // =================
		},
		"SUCCESS");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  istore_1 [a]\n" +
		"     2  iconst_0\n" +
		"     3  istore_2 [b]\n" +
		"     4  iload_1 [a]\n" +
		"     5  ifeq 11\n" +
		"     8  goto 17\n" +
		"    11  iconst_0\n" +
		"    12  dup\n" +
		"    13  istore_2 [b]\n" +
		"    14  ifeq 19\n" +
		"    17  iload_2 [b]\n" +
		"    18  istore_1 [a]\n" +
		"    19  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    22  ldc <String \"SUCCESS\"> [22]\n" +
		"    24  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    27  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 4, line: 4]\n" +
		"        [pc: 17, line: 5]\n" +
		"        [pc: 19, line: 6]\n" +
		"        [pc: 27, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 28] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 2, pc: 28] local: a index: 1 type: boolean\n" +
		"        [pc: 4, pc: 28] local: b index: 2 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162965 - variation
public void test042() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean a = true, b;\n" +
			"		if (a ? (b = true) : false)\n" +
			"		a = b;\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"    }        \n" +
			"}\n", // =================
		},
		"SUCCESS");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  istore_1 [a]\n" +
		"     2  iload_1 [a]\n" +
		"     3  ifeq 14\n" +
		"     6  iconst_1\n" +
		"     7  dup\n" +
		"     8  istore_2 [b]\n" +
		"     9  ifeq 14\n" +
		"    12  iload_2 [b]\n" +
		"    13  istore_1 [a]\n" +
		"    14  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    17  ldc <String \"SUCCESS\"> [22]\n" +
		"    19  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    22  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 2, line: 4]\n" +
		"        [pc: 12, line: 5]\n" +
		"        [pc: 14, line: 6]\n" +
		"        [pc: 22, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 23] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 2, pc: 23] local: a index: 1 type: boolean\n" +
		"        [pc: 9, pc: 14] local: b index: 2 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567
public void test043() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	boolean b;\n" +
			"	X(boolean b1) {\n" +
			"		if (b1 || (false && b1)) {\n" +
			"			System.out.println(b);\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #8 (Z)V\n" +
		"  // Stack: 2, Locals: 2\n" +
		"  X(boolean b1);\n" +
		"     0  aload_0 [this]\n" +
		"     1  invokespecial java.lang.Object() [10]\n" +
		"     4  iload_1 [b1]\n" +
		"     5  ifne 11\n" +
		"     8  goto 21\n" +
		"    11  getstatic java.lang.System.out : java.io.PrintStream [13]\n" +
		"    14  aload_0 [this]\n" +
		"    15  getfield X.b : boolean [19]\n" +
		"    18  invokevirtual java.io.PrintStream.println(boolean) : void [21]\n" +
		"    21  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 4, line: 4]\n" +
		"        [pc: 11, line: 5]\n" +
		"        [pc: 21, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 22] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 22] local: b1 index: 1 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test044() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X(boolean b1) {\n" +
			"		if (b1 || !(true || b1)) {\n" +
			"			System.out.println(b1);\n" +
			"		}\n" +
			"	} 	\n" +
			"}\n", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #6 (Z)V\n" +
		"  // Stack: 2, Locals: 2\n" +
		"  X(boolean b1);\n" +
		"     0  aload_0 [this]\n" +
		"     1  invokespecial java.lang.Object() [8]\n" +
		"     4  iload_1 [b1]\n" +
		"     5  ifne 11\n" +
		"     8  goto 18\n" +
		"    11  getstatic java.lang.System.out : java.io.PrintStream [11]\n" +
		"    14  iload_1 [b1]\n" +
		"    15  invokevirtual java.io.PrintStream.println(boolean) : void [17]\n" +
		"    18  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 2]\n" +
		"        [pc: 4, line: 3]\n" +
		"        [pc: 11, line: 4]\n" +
		"        [pc: 18, line: 6]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 19] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 19] local: b1 index: 1 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test045() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(boolean b1, boolean b2){\n" +
			"		if (b1 || ((b1 && b2) && false)) {\n" +
			"			System.out.println(b1);	\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #15 (ZZ)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  void foo(boolean b1, boolean b2);\n" +
		"     0  iload_1 [b1]\n" +
		"     1  ifne 15\n" +
		"     4  iload_1 [b1]\n" +
		"     5  ifeq 22\n" +
		"     8  iload_2 [b2]\n" +
		"     9  ifeq 22\n" +
		"    12  goto 22\n" +
		"    15  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    18  iload_1 [b1]\n" +
		"    19  invokevirtual java.io.PrintStream.println(boolean) : void [22]\n" +
		"    22  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 15, line: 4]\n" +
		"        [pc: 22, line: 6]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 23] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 23] local: b1 index: 1 type: boolean\n" +
		"        [pc: 0, pc: 23] local: b2 index: 2 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test046() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo2(boolean b1, boolean b2){\n" +
			"		if (b1 || ((b1 || b2) && false)) {\n" +
			"			System.out.println(b1);	\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #15 (ZZ)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  void foo2(boolean b1, boolean b2);\n" +
		"     0  iload_1 [b1]\n" +
		"     1  ifne 15\n" +
		"     4  iload_1 [b1]\n" +
		"     5  ifne 22\n" +
		"     8  iload_2 [b2]\n" +
		"     9  ifeq 22\n" +
		"    12  goto 22\n" +
		"    15  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    18  iload_1 [b1]\n" +
		"    19  invokevirtual java.io.PrintStream.println(boolean) : void [22]\n" +
		"    22  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 15, line: 4]\n" +
		"        [pc: 22, line: 6]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 23] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 23] local: b1 index: 1 type: boolean\n" +
		"        [pc: 0, pc: 23] local: b2 index: 2 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test047() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X(boolean b1) {\n" +
			"		int i;\n" +
			"		if (((b1 && false) && true) || true) {\n" +
			"			System.out.println(b1);\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #6 (Z)V\n" +
		"  // Stack: 2, Locals: 2\n" +
		"  X(boolean b1);\n" +
		"     0  aload_0 [this]\n" +
		"     1  invokespecial java.lang.Object() [8]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [11]\n" +
		"     7  iload_1 [b1]\n" +
		"     8  invokevirtual java.io.PrintStream.println(boolean) : void [17]\n" +
		"    11  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 2]\n" +
		"        [pc: 4, line: 5]\n" +
		"        [pc: 11, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 12] local: b1 index: 1 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test048() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X(boolean b1) {\n" +
			"		int i;\n" +
			"		if (((false && b1) && false) || true) {\n" +
			"			System.out.println(b1);\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #6 (Z)V\n" +
		"  // Stack: 2, Locals: 2\n" +
		"  X(boolean b1);\n" +
		"     0  aload_0 [this]\n" +
		"     1  invokespecial java.lang.Object() [8]\n" +
		"     4  getstatic java.lang.System.out : java.io.PrintStream [11]\n" +
		"     7  iload_1 [b1]\n" +
		"     8  invokevirtual java.io.PrintStream.println(boolean) : void [17]\n" +
		"    11  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 2]\n" +
		"        [pc: 4, line: 5]\n" +
		"        [pc: 11, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 12] local: b1 index: 1 type: boolean\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185567 - variation
public void test049() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X(boolean b1) {\n" +
			"		int i;\n" +
			"		if (((b1 && b1) && false) || true) {\n" +
			"			System.out.println(b1);\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #6 (Z)V\n" +
		"  // Stack: 2, Locals: 2\n" +
		"  X(boolean b1);\n" +
		"     0  aload_0 [this]\n" +
		"     1  invokespecial java.lang.Object() [8]\n" +
		"     4  iload_1 [b1]\n" +
		"     5  ifeq 8\n" +
		"     8  getstatic java.lang.System.out : java.io.PrintStream [11]\n" +
		"    11  iload_1 [b1]\n" +
		"    12  invokevirtual java.io.PrintStream.println(boolean) : void [17]\n" +
		"    15  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 2]\n" +
		"        [pc: 4, line: 4]\n" +
		"        [pc: 8, line: 5]\n" +
		"        [pc: 15, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 16] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 16] local: b1 index: 1 type: boolean\n";

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

public void test050() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean t1 = true, t2 = true;\n" +
			"		if (t1){\n" +
			"		    if (t2){\n" +
			"		       return;\n" +
			"		    }\n" +
			"		    // dead goto bytecode\n" +
			"		}else{\n" +
			"			System.out.println();\n" +
			"		}		\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");
	// 	ensure optimized boolean codegen sequence
	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 1, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  istore_1 [t1]\n" +
		"     2  iconst_1\n" +
		"     3  istore_2 [t2]\n" +
		"     4  iload_1 [t1]\n" +
		"     5  ifeq 13\n" +
		"     8  iload_2 [t2]\n" +
		"     9  ifeq 19\n" +
		"    12  return\n" +
		"    13  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    16  invokevirtual java.io.PrintStream.println() : void [22]\n" +
		"    19  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 4, line: 4]\n" +
		"        [pc: 8, line: 5]\n" +
		"        [pc: 12, line: 6]\n" +
		"        [pc: 13, line: 10]\n" +
		"        [pc: 19, line: 12]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 20] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 2, pc: 20] local: t1 index: 1 type: boolean\n" +
		"        [pc: 4, pc: 20] local: t2 index: 2 type: boolean\n";

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
public static Class testClass() {
	return BooleanTest.class;
}
}
