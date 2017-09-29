/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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

public class DebugAttributeTest extends AbstractRegressionTest {

	public DebugAttributeTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return DebugAttributeTest.class;
	}

/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=124212
 */
public void test001() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        String s;\n" +
			"        if(args.length == 0) {\n" +
			"          s = \"SUCCESS\";\n" +
			"        } else {\n" +
			"          return;\n" +
			"        }\n" +
			"        System.out.println(s);\n" +
			"    }\n" +
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
		"      Local variable table:\n" +
		"        [pc: 0, pc: 20] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 8, pc: 11] local: s index: 1 type: java.lang.String\n" +
		"        [pc: 12, pc: 20] local: s index: 1 type: java.lang.String\n";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=205046
public void test002() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.print(\"line 1\");\n" +
			"		myBlock: {\n" +
			"			System.out.print(\"line 2\");\n" +
			"			if (false) {\n" +
			"				break myBlock;\n" +
			"			}\n" +
			"			System.out.print(\"line 3\");\n" +
			"		}\n" +
			"		System.out.print(\"line 4\");\n" +
			"	}" +
			"}",
		},
		"line 1line 2line 3line 4");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"     3  ldc <String \"line 1\"> [22]\n" +
		"     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
		"     8  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    11  ldc <String \"line 2\"> [30]\n" +
		"    13  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
		"    16  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    19  ldc <String \"line 3\"> [32]\n" +
		"    21  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
		"    24  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    27  ldc <String \"line 4\"> [34]\n" +
		"    29  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
		"    32  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 8, line: 5]\n" +
		"        [pc: 16, line: 9]\n" +
		"        [pc: 24, line: 11]\n" +
		"        [pc: 32, line: 12]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 33] local: args index: 0 type: java.lang.String[]\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=258950
public void test003() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"import java.util.Iterator;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		List l = new ArrayList();\n" +
			"		List l2 = new ArrayList();\n" +
			"		l.add(new X());\n" +
			"		for (Iterator iterator = l.iterator(); iterator.hasNext() ;) {\n" +
			"			l2.add(((X) iterator.next()).toString()\n" + 
			"				.substring(3));\n" +
			"		}\n" + 
			"		for (Iterator iterator = l2.iterator(); iterator.hasNext() ;) {\n" +
			"			System.out.println(iterator.next());\n" +
			"		}\n" + 
			"	}" +
			"	public String toString() {\n" +
			"		return \"NO_SUCCESS\";\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 3, Locals: 4\n" + 
		"  public static void main(java.lang.String[] args);\n" + 
		"      0  new java.util.ArrayList [16]\n" + 
		"      3  dup\n" + 
		"      4  invokespecial java.util.ArrayList() [18]\n" + 
		"      7  astore_1 [l]\n" + 
		"      8  new java.util.ArrayList [16]\n" + 
		"     11  dup\n" + 
		"     12  invokespecial java.util.ArrayList() [18]\n" + 
		"     15  astore_2 [l2]\n" + 
		"     16  aload_1 [l]\n" + 
		"     17  new X [1]\n" + 
		"     20  dup\n" + 
		"     21  invokespecial X() [19]\n" + 
		"     24  invokeinterface java.util.List.add(java.lang.Object) : boolean [20] [nargs: 2]\n" + 
		"     29  pop\n" + 
		"     30  aload_1 [l]\n" + 
		"     31  invokeinterface java.util.List.iterator() : java.util.Iterator [26] [nargs: 1]\n" + 
		"     36  astore_3 [iterator]\n" + 
		"     37  goto 63\n" + 
		"     40  aload_2 [l2]\n" + 
		"     41  aload_3 [iterator]\n" + 
		"     42  invokeinterface java.util.Iterator.next() : java.lang.Object [30] [nargs: 1]\n" + 
		"     47  checkcast X [1]\n" + 
		"     50  invokevirtual X.toString() : java.lang.String [36]\n" + 
		"     53  iconst_3\n" + 
		"     54  invokevirtual java.lang.String.substring(int) : java.lang.String [40]\n" + 
		"     57  invokeinterface java.util.List.add(java.lang.Object) : boolean [20] [nargs: 2]\n" + 
		"     62  pop\n" + 
		"     63  aload_3 [iterator]\n" + 
		"     64  invokeinterface java.util.Iterator.hasNext() : boolean [46] [nargs: 1]\n" + 
		"     69  ifne 40\n" + 
		"     72  aload_2 [l2]\n" + 
		"     73  invokeinterface java.util.List.iterator() : java.util.Iterator [26] [nargs: 1]\n" + 
		"     78  astore_3 [iterator]\n" + 
		"     79  goto 94\n" + 
		"     82  getstatic java.lang.System.out : java.io.PrintStream [50]\n" + 
		"     85  aload_3 [iterator]\n" + 
		"     86  invokeinterface java.util.Iterator.next() : java.lang.Object [30] [nargs: 1]\n" + 
		"     91  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [56]\n" + 
		"     94  aload_3 [iterator]\n" + 
		"     95  invokeinterface java.util.Iterator.hasNext() : boolean [46] [nargs: 1]\n" + 
		"    100  ifne 82\n" + 
		"    103  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 6]\n" + 
		"        [pc: 8, line: 7]\n" + 
		"        [pc: 16, line: 8]\n" + 
		"        [pc: 30, line: 9]\n" + 
		"        [pc: 40, line: 10]\n" + 
		"        [pc: 53, line: 11]\n" + 
		"        [pc: 57, line: 10]\n" + 
		"        [pc: 63, line: 9]\n" + 
		"        [pc: 72, line: 13]\n" + 
		"        [pc: 82, line: 14]\n" + 
		"        [pc: 94, line: 13]\n" + 
		"        [pc: 103, line: 16]\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=262717
public void test004() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X{\n" + 
			"	public class Inner {\n" + 
			"		public void foo() {\n" + 
			"			int i = 0;\n" + 
			"			final int NEW = 1;\n" + 
			"			if (i == NEW) {\n" + 
			"				System.out.println();\n" + 
			"			}\n" + 
			"			bar();\n" + 
			"		}\n" + 
			"	}\n" + 
			"	public void bar() {\n" + 
			"		System.out.println(\"SUCCESS\");\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new X().new Inner().foo();\n" + 
			"	}\n" + 
			"}",
		},
		"SUCCESS");

	String expectedOutput =
		"    22  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 4]\n" + 
		"        [pc: 2, line: 5]\n" + 
		"        [pc: 4, line: 6]\n" + 
		"        [pc: 9, line: 7]\n" + 
		"        [pc: 15, line: 9]\n" + 
		"        [pc: 22, line: 10]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 23] local: this index: 0 type: X.Inner\n" + 
		"        [pc: 2, pc: 23] local: i index: 1 type: int\n" + 
		"        [pc: 4, pc: 23] local: NEW index: 2 type: int\n";

	File f = new File(OUTPUT_DIR + File.separator + "X$Inner.class");
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
}
