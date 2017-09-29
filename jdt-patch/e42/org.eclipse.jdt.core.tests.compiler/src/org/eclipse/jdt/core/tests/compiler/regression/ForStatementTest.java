/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ForStatementTest extends AbstractRegressionTest {

public ForStatementTest(String name) {
	super(name);
}

protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 45, 46 };
//	TESTS_RANGE = new int[] { 34, 38 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public void test001() {
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Object m(int[] arg) {\n" +
				"		yyLoop: for (int i = 0;; ++i) {\n" +
				"			yyInner: for (;;) {\n" +
				"				switch (arg[i]) {\n" +
				"					case 0:\n" +
				"						break;\n" +
				"					case 1:\n" +
				"						continue yyInner;\n" +
				"				}\n" +
				"				if (i == 32)\n" +
				"					return arg;\n" +
				"				if (i == 12)\n" +
				"					break;\n" +
				"				continue yyLoop;\n" +
				"			}\n" +
				"			if (i == 32)\n" +
				"				return null;\n" +
				"			if (i > 7)\n" +
				"				continue yyLoop;\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180471
public void test002() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo2(int[] array) {\n" +
			"		for (int i = 0; i < array.length; i++) {\n" +
			"			System.out.println(i);\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");

	String expectedOutput =
		"  // Method descriptor #15 ([I)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  void foo2(int[] array);\n" +
		"     0  iconst_0\n" +
		"     1  istore_2 [i]\n" +
		"     2  iload_2 [i]\n" +
		"     3  aload_1 [array]\n" +
		"     4  arraylength\n" +
		"     5  if_icmpge 15\n" +
		"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    11  iload_2 [i]\n" +
		"    12  invokevirtual java.io.PrintStream.println(int) : void [22]\n" +
		"    15  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 8, line: 4]\n" +
		"        [pc: 15, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 16] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 16] local: array index: 1 type: int[]\n" +
		"        [pc: 2, pc: 15] local: i index: 2 type: int\n";

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
public void test003() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo4(int[] array) {\n" +
			"		do {\n" +
			"			System.out.println();\n" +
			"			break;\n" +
			"		} while (array.length > 0);\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");

	String expectedOutput =
		"  // Method descriptor #15 ([I)V\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  void foo4(int[] array);\n" +
		"    0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    3  invokevirtual java.io.PrintStream.println() : void [22]\n" +
		"    6  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 6, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 7] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 7] local: array index: 1 type: int[]\n";

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
public void test004() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo1(int[] array) {\n" +
			"		while (array.length > 0) {\n" +
			"			System.out.println();\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n", // =================
		},
		"");

	String expectedOutput =
		"  // Method descriptor #15 ([I)V\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  void foo1(int[] array);\n" +
		"     0  aload_1 [array]\n" +
		"     1  arraylength\n" +
		"     2  ifle 11\n" +
		"     5  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"     8  invokevirtual java.io.PrintStream.println() : void [22]\n" +
		"    11  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 5, line: 4]\n" +
		"        [pc: 11, line: 7]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 12] local: this index: 0 type: X\n" +
		"        [pc: 0, pc: 12] local: array index: 1 type: int[]\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=195317
public void test005() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int mode = 1;\n" +
			"		loop: for (;;) {\n" +
			"			switch (mode) {\n" +
			"				case 2 :\n" +
			"					return;\n" +
			"				case 1:\n" +
			"					mode = 2;\n" +
			"					continue loop;\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 1, Locals: 2\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  istore_1 [mode]\n" +
		"     2  iload_1 [mode]\n" +
		"     3  tableswitch default: 27\n" +
		"          case 1: 25\n" +
		"          case 2: 24\n" +
		"    24  return\n" +
		"    25  iconst_2\n" +
		"    26  istore_1 [mode]\n" +
		"    27  goto 2\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 2, line: 5]\n" +
		"        [pc: 24, line: 7]\n" +
		"        [pc: 25, line: 9]\n" +
		"        [pc: 27, line: 4]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 30] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 2, pc: 30] local: mode index: 1 type: int\n";

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
	return ForStatementTest.class;
}
}
