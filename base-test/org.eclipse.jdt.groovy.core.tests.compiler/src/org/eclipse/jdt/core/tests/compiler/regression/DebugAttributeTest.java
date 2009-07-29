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
}
