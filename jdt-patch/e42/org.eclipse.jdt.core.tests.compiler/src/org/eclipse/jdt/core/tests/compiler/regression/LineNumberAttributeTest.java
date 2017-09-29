/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

public class LineNumberAttributeTest extends AbstractRegressionTest {

public LineNumberAttributeTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=173800
public void test001() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X next;\n" +
			"\n" +
			"	X(X next) {\n" +
			"		this.next = next;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String args[]) {\n" +
			"		try {\n" +
			"			X x = new X(new X(new X(null)));\n" +
			"			x.\n" +
			"				next.\n" +
			"					next.\n" +
			"						next.\n" +
			"							next.\n" +
			"								next.\n" +
			"									toString();\n" +
			"		} catch(NullPointerException e) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
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
		"  // Method descriptor #19 ([Ljava/lang/String;)V\n" +
		"  // Stack: 7, Locals: 2\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  new X [1]\n" +
		"     3  dup\n" +
		"     4  new X [1]\n" +
		"     7  dup\n" +
		"     8  new X [1]\n" +
		"    11  dup\n" +
		"    12  aconst_null\n" +
		"    13  invokespecial X(X) [20]\n" +
		"    16  invokespecial X(X) [20]\n" +
		"    19  invokespecial X(X) [20]\n" +
		"    22  astore_1 [x]\n" +
		"    23  aload_1 [x]\n" +
		"    24  getfield X.next : X [13]\n" +
		"    27  getfield X.next : X [13]\n" +
		"    30  getfield X.next : X [13]\n" +
		"    33  getfield X.next : X [13]\n" +
		"    36  getfield X.next : X [13]\n" +
		"    39  invokevirtual java.lang.Object.toString() : java.lang.String [22]\n" +
		"    42  pop\n" +
		"    43  goto 55\n" +
		"    46  astore_1 [e]\n" +
		"    47  getstatic java.lang.System.out : java.io.PrintStream [26]\n" +
		"    50  ldc <String \"SUCCESS\"> [32]\n" +
		"    52  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]\n" +
		"    55  return\n" +
		"      Exception Table:\n" +
		"        [pc: 0, pc: 43] -> 46 when : java.lang.NullPointerException\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 10]\n" +
		"        [pc: 23, line: 11]\n" +
		"        [pc: 24, line: 12]\n" +
		"        [pc: 27, line: 13]\n" +
		"        [pc: 30, line: 14]\n" +
		"        [pc: 33, line: 15]\n" +
		"        [pc: 36, line: 16]\n" +
		"        [pc: 39, line: 17]\n" +
		"        [pc: 43, line: 18]\n" +
		"        [pc: 47, line: 19]\n" +
		"        [pc: 55, line: 21]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 56] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 23, pc: 43] local: x index: 1 type: X\n" +
		"        [pc: 47, pc: 55] local: e index: 1 type: java.lang.NullPointerException\n";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=173800
public void test002() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X x;\n" +
			"\n" +
			"	X next;\n" +
			"\n" +
			"	X(X next) {\n" +
			"		this.next = next;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String args[]) {\n" +
			"		X x = new X(new X(new X(null)));\n" +
			"		x.x = x;\n" +
			"		x.foo();\n" +
			"	}\n" +
			"\n" +
			"	public void foo() {\n" +
			"		try {\n" +
			"			this.\n" +
			"				x.\n" +
			"					next.\n" +
			"						next.\n" +
			"							next.\n" +
			"								next.\n" +
			"									next.\n" +
			"										toString();\n" +
			"		} catch(NullPointerException e) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		}\n" +
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
		"  // Method descriptor #13 ()V\n" +
		"  // Stack: 2, Locals: 2\n" +
		"  public void foo();\n" +
		"     0  aload_0 [this]\n" +
		"     1  getfield X.x : X [23]\n" +
		"     4  getfield X.next : X [14]\n" +
		"     7  getfield X.next : X [14]\n" +
		"    10  getfield X.next : X [14]\n" +
		"    13  getfield X.next : X [14]\n" +
		"    16  getfield X.next : X [14]\n" +
		"    19  invokevirtual java.lang.Object.toString() : java.lang.String [30]\n" +
		"    22  pop\n" +
		"    23  goto 35\n" +
		"    26  astore_1 [e]\n" +
		"    27  getstatic java.lang.System.out : java.io.PrintStream [34]\n" +
		"    30  ldc <String \"SUCCESS\"> [40]\n" +
		"    32  invokevirtual java.io.PrintStream.println(java.lang.String) : void [42]\n" +
		"    35  return\n" +
		"      Exception Table:\n" +
		"        [pc: 0, pc: 23] -> 26 when : java.lang.NullPointerException\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 18]\n" +
		"        [pc: 1, line: 19]\n" +
		"        [pc: 4, line: 20]\n" +
		"        [pc: 7, line: 21]\n" +
		"        [pc: 10, line: 22]\n" +
		"        [pc: 13, line: 23]\n" +
		"        [pc: 16, line: 24]\n" +
		"        [pc: 19, line: 25]\n" +
		"        [pc: 23, line: 26]\n" +
		"        [pc: 27, line: 27]\n" +
		"        [pc: 35, line: 29]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 36] local: this index: 0 type: X\n" +
		"        [pc: 27, pc: 35] local: e index: 1 type: java.lang.NullPointerException\n";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
public static Class testClass() {
	return LineNumberAttributeTest.class;
}
}
