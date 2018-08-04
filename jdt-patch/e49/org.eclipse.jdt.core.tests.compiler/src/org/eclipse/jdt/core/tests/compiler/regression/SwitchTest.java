/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for bug 374605 - Unreasonable warning for enum-based switch statements
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SwitchTest extends AbstractRegressionTest {
	
	private static final long JDKLevelSupportingStringSwitch = ClassFileConstants.JDK1_7;

static {
//	TESTS_NUMBERS = new int[] { 22 };
//	TESTS_NAMES = new String[] { "testFor356002", "testFor356002_2", "testFor356002_3" };
}
public SwitchTest(String name) {
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
		"  public static void main(String args[]) {\n" +
		"    foo();\n" +
		"  }\n" +
		"  public static void foo() {\n" +
		"    try {\n" +
		"      switch(0) {\n" +
		"      case 0 :\n" +
		"      case 1 - (1 << 31) :\n" +
		"      case (1 << 30) :\n" +
		"      }\n" +
		"    } catch (OutOfMemoryError e) {\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}
public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  int k;\n" +
		"  public void foo() {\n" +
		"    int c;\n" +
		"    switch (k) {\n" +
		"      default :\n" +
		"        c = 2;\n" +
		"        break;\n" +
		"      case 2 :\n" +
		"        c = 3;\n" +
		"        break;\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}

public void test003() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  int i = 0;\n" +
		"  void foo() {\n" +
		"    switch (i) {\n" +
		"      case 1 :\n" +
		"        {\n" +
		"          int j;\n" +
		"          break;\n" +
		"        }\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}

public void test004() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  public static int foo() {\n" +
		"    int i = 0, j;\n" +
		"    switch (i) {\n" +
		"      default :\n" +
		"        int k = 2;\n" +
		"        j = k;\n" +
		"    }\n" +
		"    if (j != -2) {\n" +
		"      return 1;\n" +
		"    }\n" +
		"    return 0;\n" +
		"  }\n" +
		"}\n",
	});
}

public void test005() {
	this.runConformTest(new String[] {
		"p/BugJavaCase.java",
		"package p;\n" +
		"class BugJavaCase {\n" +
		"  public static final int BC_ZERO_ARG = 1;\n" +
		"  public void test01(int i) {\n" +
		"    switch (i) {\n" +
		"      case BC_ZERO_ARG :\n" +
		"        System.out.println(\"i = \" + i);\n" +
		"        break;\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}


public void test006() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  public static void main(String args[]) {\n" +
		"    foo(); \n" +
		"  } \n" +
		" \n" +
		"  public static void foo() { \n" +
		"    char x = 5;\n" +
		"    final short b = 5;\n" +
		"    int a;\n" +
		"    \n" +
		"    switch (x) {\n" +
		"      case b:        // compile time error\n" +
		"        a = 0;\n" +
		"        break; \n" +
		"      default:\n" +
		"        a=1;\n" +
		"    }\n" +
		"    \n" +
		"  }\n" +
		"}\n",
	});
}

public void test007() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"class X {\n" +
			"  void v() {\n" +
			"    switch (1) {\n" +
			"      case (int) (1.0 / 0.0) :\n" +
			"        break;\n" +
			"      case (int) (2.0 / 0.0) :\n" +
			"        break;\n" +
			"    }\n" +
			"  }\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in p\\X.java (at line 5)\n" +
		"	case (int) (1.0 / 0.0) :\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Duplicate case\n" +
		"----------\n" +
		"2. ERROR in p\\X.java (at line 7)\n" +
		"	case (int) (2.0 / 0.0) :\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Duplicate case\n" +
		"----------\n"
	);
}
public void test008() {
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		switch(args.length){\n" +
		"		}\n" +
		"		System.out.println(\"SUCCESS\");\n" +
		"	}\n" +
		"}\n",
	},
	"SUCCESS");
}
public void test009() {
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" +
		"    public static void main(String argv[]) {\n" +
		"        switch (81391861) {\n" +
		"        case (81391861) :\n" +
		"        	System.out.println(\"SUCCESS\");\n" +
		"            break;\n" +
		"        default:\n" +
		"        	System.out.println(\"FAILED\");\n" +
		"        }\n" +
		"    }\n" +
		"}\n",
	},
	"SUCCESS");
}
public void test010() {
	String newMessage =
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	switch(this){\n" +
			"	       ^^^^\n" +
			"Cannot switch on a value of type X. Only convertible int values, strings or enum variables are permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\n" +
			"	switch(x){\n" +
			"	       ^\n" +
			"x cannot be resolved to a variable\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 13)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n";
	String oldMessage =
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	switch(this){\n" +
			"	       ^^^^\n" +
			"Cannot switch on a value of type X. Only convertible int values or enum variables are permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\n" +
			"	switch(x){\n" +
			"	       ^\n" +
			"x cannot be resolved to a variable\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 13)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n";
	this.runNegativeTest(new String[] {
		"X.java",
		"public class X {\n" +
		"	\n" +
		"	void foo(){\n" +
		"		switch(this){\n" +
		"			case 0 : \n" +
		"				Zork z;\n" +
		"		}\n" +
		"	}\n" +
		"	\n" +
		"	void bar(){\n" +
		"		switch(x){\n" +
		"			case 0 : \n" +
		"				Zork z;\n" +
		"		}\n" +
		"	}	\n" +
		"}\n",
	},
	this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
	
}
public void test011() {
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" +
		"	public static void main(String args[]) {\n" +
		"		switch (args.length) {\n" +
		"			case 1 :\n" +
		"				System.out.println();\n" +
		"			case 3 :\n" +
		"				break;\n" +
		"			default :\n" +
		"		}\n" +
		"		System.out.println(\"SUCCESS\");\n" +
		"	}\n" +
		"}\n",
	},
	"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86813
public void test012() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" +
		"  public static void main(String[] args) {\n" +
		"    boolean x= true;\n" +
		"    try {\n" +
		"      int i= 1;\n" +
		"      switch (i) { // <-- breakpoint here\n" +
		"        case 1:\n" +
		"          break;      //step 1 \n" +
		"        case 2:\n" +
		"          x = false;   //step 2 \n" +
		"          break;\n" +
		"      }\n" +
		"    }catch(Exception e) {\n" +
		"    }\n" +
		"    System.out.println(\"SUCCESS\");\n" +
		"  }\n" +
		"}\n",
	},
	"SUCCESS");

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_1\n" +
		"     1  istore_1 [x]\n" +
		"     2  iconst_1\n" +
		"     3  istore_2 [i]\n" +
		"     4  iload_2 [i]\n" +
		"     5  tableswitch default: 33\n" +
		"          case 1: 28\n" +
		"          case 2: 31\n" +
		"    28  goto 37\n" +
		"    31  iconst_0\n" +
		"    32  istore_1 [x]\n" +
		"    33  goto 37\n" +
		"    36  astore_2\n" +
		"    37  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    40  ldc <String \"SUCCESS\"> [22]\n" +
		"    42  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    45  return\n" +
		"      Exception Table:\n" +
		"        [pc: 2, pc: 33] -> 36 when : java.lang.Exception\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 2, line: 5]\n" +
		"        [pc: 4, line: 6]\n" +
		"        [pc: 28, line: 8]\n" +
		"        [pc: 31, line: 10]\n" +
		"        [pc: 33, line: 13]\n" +
		"        [pc: 37, line: 15]\n" +
		"        [pc: 45, line: 16]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 46] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 2, pc: 46] local: x index: 1 type: boolean\n" +
		"        [pc: 4, pc: 33] local: i index: 2 type: int\n";

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
public void test013() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" +
		"\n" +
		"	public static void main(String[] args) {\n" +
		"		X x;\n" +
		"		Object o = null;\n" +
		"		for (int i = 0; i < 10; i++) {\n" +
		"			if (i < 90) {\n" +
		"				x = new X();\n" +
		"				if (i > 4) {\n" +
		"					o = new Object();\n" +
		"				} else {\n" +
		"					o = null;\n" +
		"				}\n" +
		"				switch (2) {\n" +
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
		"				System.out.print(\"3\");				\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n",
	},
	"333332323232323");

	String expectedOutput = new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6
		?	"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 5\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"      0  aconst_null\n" +
			"      1  astore_2 [o]\n" +
			"      2  iconst_0\n" +
			"      3  istore_3 [i]\n" +
			"      4  goto 103\n" +
			"      7  iload_3 [i]\n" +
			"      8  bipush 90\n" +
			"     10  if_icmpge 100\n" +
			"     13  new X [1]\n" +
			"     16  dup\n" +
			"     17  invokespecial X() [16]\n" +
			"     20  astore_1 [x]\n" +
			"     21  iload_3 [i]\n" +
			"     22  iconst_4\n" +
			"     23  if_icmple 37\n" +
			"     26  new java.lang.Object [3]\n" +
			"     29  dup\n" +
			"     30  invokespecial java.lang.Object() [8]\n" +
			"     33  astore_2 [o]\n" +
			"     34  goto 39\n" +
			"     37  aconst_null\n" +
			"     38  astore_2 [o]\n" +
			"     39  iconst_2\n" +
			"     40  tableswitch default: 76\n" +
			"          case 0: 60\n" +
			"     60  aload_2 [o]\n" +
			"     61  instanceof java.lang.String [17]\n" +
			"     64  ifeq 92\n" +
			"     67  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
			"     70  ldc <String \"1\"> [25]\n" +
			"     72  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]\n" +
			"     75  return\n" +
			"     76  aload_2 [o]\n" +
			"     77  astore 4 [diff]\n" +
			"     79  aload 4 [diff]\n" +
			"     81  ifnull 92\n" +
			"     84  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
			"     87  ldc <String \"2\"> [33]\n" +
			"     89  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]\n" +
			"     92  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
			"     95  ldc <String \"3\"> [35]\n" +
			"     97  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]\n" +
			"    100  iinc 3 1 [i]\n" +
			"    103  iload_3 [i]\n" +
			"    104  bipush 10\n" +
			"    106  if_icmplt 7\n" +
			"    109  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 2, line: 6]\n" +
			"        [pc: 7, line: 7]\n" +
			"        [pc: 13, line: 8]\n" +
			"        [pc: 21, line: 9]\n" +
			"        [pc: 26, line: 10]\n" +
			"        [pc: 34, line: 11]\n" +
			"        [pc: 37, line: 12]\n" +
			"        [pc: 39, line: 14]\n" +
			"        [pc: 60, line: 16]\n" +
			"        [pc: 67, line: 17]\n" +
			"        [pc: 75, line: 18]\n" +
			"        [pc: 76, line: 23]\n" +
			"        [pc: 79, line: 24]\n" +
			"        [pc: 84, line: 25]\n" +
			"        [pc: 92, line: 30]\n" +
			"        [pc: 100, line: 6]\n" +
			"        [pc: 109, line: 33]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 110] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 21, pc: 100] local: x index: 1 type: X\n" +
			"        [pc: 2, pc: 110] local: o index: 2 type: java.lang.Object\n" +
			"        [pc: 4, pc: 109] local: i index: 3 type: int\n" +
			"        [pc: 79, pc: 92] local: diff index: 4 type: java.lang.Object\n"
		:
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 5\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"      0  aconst_null\n" +
			"      1  astore_2 [o]\n" +
			"      2  iconst_0\n" +
			"      3  istore_3 [i]\n" +
			"      4  goto 103\n" +
			"      7  iload_3 [i]\n" +
			"      8  bipush 90\n" +
			"     10  if_icmpge 100\n" +
			"     13  new X [1]\n" +
			"     16  dup\n" +
			"     17  invokespecial X() [16]\n" +
			"     20  astore_1 [x]\n" +
			"     21  iload_3 [i]\n" +
			"     22  iconst_4\n" +
			"     23  if_icmple 37\n" +
			"     26  new java.lang.Object [3]\n" +
			"     29  dup\n" +
			"     30  invokespecial java.lang.Object() [8]\n" +
			"     33  astore_2 [o]\n" +
			"     34  goto 39\n" +
			"     37  aconst_null\n" +
			"     38  astore_2 [o]\n" +
			"     39  iconst_2\n" +
			"     40  tableswitch default: 76\n" +
			"          case 0: 60\n" +
			"     60  aload_2 [o]\n" +
			"     61  instanceof java.lang.String [17]\n" +
			"     64  ifeq 92\n" +
			"     67  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
			"     70  ldc <String \"1\"> [25]\n" +
			"     72  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]\n" +
			"     75  return\n" +
			"     76  aload_2 [o]\n" +
			"     77  astore 4 [diff]\n" +
			"     79  aload 4 [diff]\n" +
			"     81  ifnull 92\n" +
			"     84  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
			"     87  ldc <String \"2\"> [33]\n" +
			"     89  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]\n" +
			"     92  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
			"     95  ldc <String \"3\"> [35]\n" +
			"     97  invokevirtual java.io.PrintStream.print(java.lang.String) : void [27]\n" +
			"    100  iinc 3 1 [i]\n" +
			"    103  iload_3 [i]\n" +
			"    104  bipush 10\n" +
			"    106  if_icmplt 7\n" +
			"    109  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 2, line: 6]\n" +
			"        [pc: 7, line: 7]\n" +
			"        [pc: 13, line: 8]\n" +
			"        [pc: 21, line: 9]\n" +
			"        [pc: 26, line: 10]\n" +
			"        [pc: 34, line: 11]\n" +
			"        [pc: 37, line: 12]\n" +
			"        [pc: 39, line: 14]\n" +
			"        [pc: 60, line: 16]\n" +
			"        [pc: 67, line: 17]\n" +
			"        [pc: 75, line: 18]\n" +
			"        [pc: 76, line: 23]\n" +
			"        [pc: 79, line: 24]\n" +
			"        [pc: 84, line: 25]\n" +
			"        [pc: 92, line: 30]\n" +
			"        [pc: 100, line: 6]\n" +
			"        [pc: 109, line: 33]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 110] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 21, pc: 100] local: x index: 1 type: X\n" +
			"        [pc: 2, pc: 110] local: o index: 2 type: java.lang.Object\n" +
			"        [pc: 4, pc: 109] local: i index: 3 type: int\n" +
			"        [pc: 79, pc: 92] local: diff index: 4 type: java.lang.Object\n" +
			"      Stack map table: number of frames 8\n" +
			"        [pc: 7, full, stack: {}, locals: {java.lang.String[], _, java.lang.Object, int}]\n" +
			"        [pc: 37, full, stack: {}, locals: {java.lang.String[], X, java.lang.Object, int}]\n" +
			"        [pc: 39, same]\n" +
			"        [pc: 60, same]\n" +
			"        [pc: 76, same]\n" +
			"        [pc: 92, same]\n" +
			"        [pc: 100, full, stack: {}, locals: {java.lang.String[], _, java.lang.Object, int}]\n" +
			"        [pc: 103, same]\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245257
public void test014() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"public class X {\n" + 
		"	void foo1(int i) {\n" + 
		"		switch (i) {\n" + 
		"			case 0://OK\n" + 
		"			case 1://OK\n" + 
		"				System.out.println();\n" + 
		"				//$FALL-THROUGH$\n" + 
		"			case 2://OK\n" + 
		"				System.out.println(); //$FALL-THROUGH$\n" + 
		"			case 3://OK\n" + 
		"				System.out.println();\n" + 
		"				//$FALL-THROUGH$ - some allowed explanation\n" + 
		"			case 4://OK\n" + 
		"			case 5://OK\n" + 
		"				System.out.println();\n" + 
		"				//$FALL-THROUGH$ - not last comment, thus inoperant\n" + 
		"				// last comment is not fall-through explanation\n" + 
		"			case 6://WRONG\n" + 
		"				//$FALL-THROUGH$ - useless since not leading the case\n" + 
		"				System.out.println();\n" + 
		"				/*$FALL-THROUGH$ - block comment, is also allowed */\n" + 
		"			case 7://OK\n" + 
		"				System.out.println(\"aa\"); //$NON-NLS-1$\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n",
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 18)\n" + 
	"	case 6://WRONG\n" + 
	"	^^^^^^\n" + 
	"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" + 
	"----------\n",
	null,
	true,
	options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245257 - variation
public void test015() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"public class X {\n" + 
		"	void foo1(int i) {\n" + 
		"		switch (i) {\n" + 
		"			case 0://OK\n" + 
		"			case 1://OK\n" + 
		"				System.out.println();\n" + 
		"				//	  $FALL-THROUGH$\n" + 
		"			case 2://OK\n" + 
		"				System.out.println(); // 	 $FALL-THROUGH$\n" + 
		"			case 3://OK\n" + 
		"				System.out.println();\n" + 
		"				//	$FALL-THROUGH$ - some allowed explanation\n" + 
		"			case 4://OK\n" + 
		"			case 5://OK\n" + 
		"				System.out.println();\n" + 
		"				// $FALL-THROUGH$ - not last comment, thus inoperant\n" + 
		"				// last comment is not fall-through explanation\n" + 
		"			case 6://WRONG\n" + 
		"				// $FALL-THROUGH$ - useless since not leading the case\n" + 
		"				System.out.println();\n" + 
		"				/* $FALL-THROUGH$ - block comment, is also allowed */\n" + 
		"			case 7://OK\n" + 
		"				System.out.println(\"aa\"); //$NON-NLS-1$\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n",
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 18)\n" + 
	"	case 6://WRONG\n" + 
	"	^^^^^^\n" + 
	"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" + 
	"----------\n",
	null,
	true,
	options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245257 - variation
public void test016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"public class X {\n" + 
		"	void foo1(int i) {\n" + 
		"		switch (i) {\n" + 
		"			case 0://OK\n" + 
		"			case 1://OK\n" + 
		"				System.out.println();\n" + 
		"				//	  $FALL-THROUGH - missing trailing $ in tag\n" + 
		"			case 2://WRONG\n" + 
		"				System.out.println();\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n",
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 8)\n" + 
	"	case 2://WRONG\n" + 
	"	^^^^^^\n" + 
	"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" + 
	"----------\n",
	null,
	true,
	options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245257 - variation
public void test017() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"public class X {\n" + 
		"	void foo1(char previousChar) {\n" + 
		"		switch(previousChar) {\n" + 
		"			case \'/\':\n" + 
		"				if (previousChar == \'*\') {\n" + 
		"					// End of javadoc\n" + 
		"					break;\n" + 
		"					//$FALL-THROUGH$ into default case\n" + 
		"				}\n" + 
		"			default :\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n",
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 10)\n" + 
	"	default :\n" + 
	"	^^^^^^^\n" + 
	"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" + 
	"----------\n",
	null,
	true,
	options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=286682
public void test018() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  public static void foo(int i) { \n" +
		"    switch (i) {\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	},
	new ASTVisitor() {
		public boolean visit(SingleNameReference reference, BlockScope scope) {
			assertNotNull("No scope", scope);
			return true;
		}
	}
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=314830
public void test019() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		try {\n" +
		"			switch((Integer) null) {};\n" + 
		"			System.out.println(\"FAILED\");\n" + 
		"		} catch(NullPointerException e) {\n" + 
		"			System.out.println(\"SUCCESS\");\n" + 
		"		}\n" +
		"	}\n" +
		"}\n",
	},
	"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=314830
public void test020() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		try {\n" +
		"			switch(foo()) {};\n" + 
		"			System.out.println(\"FAILED\");\n" + 
		"		} catch(NullPointerException e) {\n" + 
		"			System.out.println(\"SUCCESS\");\n" + 
		"		}\n" +
		"	}" +
		"	static Integer foo() {\n" +
		"		return (Integer) null;\n" +
		"	}\n" +
		"}\n",
	},
	"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=314830
public void test021() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		try {\n" +
		"			switch((Character) null) {\n" + 
		"				default: System.out.println(\"FAILED\");\n" + 
		"			}\n" + 
		"		} catch(NullPointerException e) {\n" + 
		"			System.out.println(\"SUCCESS\");\n" + 
		"		}\n" +
		"	}\n" +
		"}\n",
	},
	"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=314830
public void test022() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		java.math.RoundingMode mode = null;\n" + 
		"		try {\n" +
		"			switch (mode) {}\n" + 
		"			System.out.println(\"FAILED\");\n" + 
		"		} catch(NullPointerException e) {\n" + 
		"			System.out.println(\"SUCCESS\");\n" + 
		"		}\n" +
		"	}\n" +
		"}\n",
	},
	"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=314830
public void test023() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		java.math.RoundingMode mode = java.math.RoundingMode.FLOOR;\n" + 
		"		try {\n" +
		"			switch (mode) {\n" +
		"				default: System.out.println(\"SUCCESS\");\n" + 
		"			}\n" + 
		"		} catch(NullPointerException e) {\n" + 
		"			System.out.println(\"FAILED\");\n" + 
		"		}\n" +
		"	}\n" +
		"}\n",
	},
	"SUCCESS");
}

// JDK7: Strings in Switch.
public void testStringSwitchAtJDK6() {
		String newMessage = 		
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	default: return args;\n" + 
			"	         ^^^^^^^^^^^^\n" + 
			"Void methods cannot return a value\n" + 
			"----------\n";
		String oldMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	switch(args[0]) {\n" + 
			"	       ^^^^^^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	default: return args;\n" + 
			"	         ^^^^^^^^^^^^\n" + 
			"Void methods cannot return a value\n" + 
			"----------\n";
			
		this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		switch(args[0]) {\n" + 
			"		default: return args;\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}

//JDK7: Strings in Switch.
public void testCaseTypeMismatch() {
	String newMessage = 	
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	case 123: break;\n" + 
		"	     ^^^\n" + 
		"Type mismatch: cannot convert from int to String\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	case (byte) 1: break;\n" + 
		"	     ^^^^^^^^\n" + 
		"Type mismatch: cannot convert from byte to String\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 6)\n" + 
		"	case (char) 2: break;\n" + 
		"	     ^^^^^^^^\n" + 
		"Type mismatch: cannot convert from char to String\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 7)\n" + 
		"	case (short)3: break;\n" + 
		"	     ^^^^^^^^\n" + 
		"Type mismatch: cannot convert from short to String\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 8)\n" + 
		"	case (int) 4: break;\n" + 
		"	     ^^^^^^^\n" + 
		"Type mismatch: cannot convert from int to String\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 9)\n" + 
		"	case (long) 5: break;\n" + 
		"	     ^^^^^^^^\n" + 
		"Type mismatch: cannot convert from long to String\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 10)\n" + 
		"	case (float) 6: break;\n" + 
		"	     ^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from float to String\n" + 
		"----------\n" + 
		"8. ERROR in X.java (at line 11)\n" + 
		"	case (double) 7: break;\n" + 
		"	     ^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from double to String\n" + 
		"----------\n" + 
		"9. ERROR in X.java (at line 12)\n" + 
		"	case (boolean) 8: break;\n" + 
		"	     ^^^^^^^^^^^\n" + 
		"Cannot cast from int to boolean\n" + 
		"----------\n" + 
		"10. ERROR in X.java (at line 12)\n" + 
		"	case (boolean) 8: break;\n" + 
		"	     ^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from boolean to String\n" + 
		"----------\n";
		String oldMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	switch(args[0]) {\n" + 
			"	       ^^^^^^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 12)\n" + 
			"	case (boolean) 8: break;\n" + 
			"	     ^^^^^^^^^^^\n" + 
			"Cannot cast from int to boolean\n" + 
			"----------\n";
				
		this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		switch(args[0]) {\n" + 
			"		case 123: break;\n" +
			"       case (byte) 1: break;\n" +
			"       case (char) 2: break;\n" +
			"       case (short)3: break;\n" +
			"       case (int) 4: break;\n" +
			"       case (long) 5: break;\n" +
			"       case (float) 6: break;\n" +
			"       case (double) 7: break;\n" +
			"       case (boolean) 8: break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
// JDK7: Strings in Switch.
public void testCaseTypeMismatch2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	String newMessage = 	
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	case Days.Sunday: break;\n" + 
		"	     ^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Days to String\n" + 
		"----------\n";
		String oldMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	switch (\"Sunday\") {\n" + 
			"	        ^^^^^^^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n";
				
		this.runNegativeTest(new String[] {
			"X.java",
			"enum Days { Sunday, Monday, Tuesday, Wednesday, Thuresday, Friday, Satuday };\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"    public static void main(String argv[]) {\n" +
			"        switch (\"Sunday\") {\n" +
			"            case Days.Sunday: break;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
// JDK7: Strings in Switch.
public void testCaseTypeMismatch3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	String newMessage = 	
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	case \"0\": break;\n" + 
		"	     ^^^\n" + 
		"Type mismatch: cannot convert from String to int\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	case \"Sunday\": break;\n" + 
		"	     ^^^^^^^^\n" + 
		"Type mismatch: cannot convert from String to Days\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 13)\n" + 
		"	case \"0\": break;\n" + 
		"	     ^^^\n" + 
		"Type mismatch: cannot convert from String to Integer\n" + 
		"----------\n";
				
		this.runNegativeTest(new String[] {
			"X.java",
			"enum Days { Sunday, Monday, Tuesday, Wednesday, Thuresday, Friday, Satuday };\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"    public static void main(String argv[]) {\n" +
			"        switch (argv.length) {\n" +
			"            case \"0\": break;\n" +
			"        }\n" +
			"        switch(Days.Sunday) {\n" +
			"            case \"Sunday\": break;\n" +
			"        }\n" +
			"        switch (Integer.valueOf(argv.length)) {\n" +
			"            case \"0\": break;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		newMessage);
}
// JDK7: Strings in Switch.
public void testDuplicateCase() {
		String newMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	case \"123\": break;\n" + 
			"	^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	case \"123\": break;\n" + 
			"	^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	default: return args;\n" + 
			"	         ^^^^^^^^^^^^\n" + 
			"Void methods cannot return a value\n" + 
			"----------\n";
		
		String oldMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	switch(args[0]) {\n" + 
			"	       ^^^^^^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	default: return args;\n" + 
			"	         ^^^^^^^^^^^^\n" + 
			"Void methods cannot return a value\n" + 
			"----------\n";
				
		this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		switch(args[0]) {\n" + 
			"		case \"123\": break;\n" +
			"		case \"123\": break;\n" +
			"       default: return args;\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}

// JDK7: Strings in Switch.
public void testDuplicateCase2() {
		String newMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	case \"123\": break;\n" + 
			"	^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	case \"123\": break;\n" + 
			"	^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 11)\n" + 
			"	case \"1\" + \"2\" + \"3\": break;\n" + 
			"	^^^^^^^^^^^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 13)\n" + 
			"	case local: break;\n" + 
			"	^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 14)\n" + 
			"	case field: break;\n" + 
			"	^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 15)\n" + 
			"	case ifield: break;\n" + 
			"	     ^^^^^^\n" + 
			"Cannot make a static reference to the non-static field ifield\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 16)\n" + 
			"	case inffield: break;\n" + 
			"	     ^^^^^^^^\n" + 
			"Cannot make a static reference to the non-static field inffield\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 19)\n" + 
			"	default: break;\n" + 
			"	^^^^^^^\n" + 
			"The default case is already defined\n" + 
			"----------\n";
		
		String oldMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	switch(args[0]) {\n" + 
			"	       ^^^^^^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 15)\n" + 
			"	case ifield: break;\n" + 
			"	     ^^^^^^\n" + 
			"Cannot make a static reference to the non-static field ifield\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 16)\n" + 
			"	case inffield: break;\n" + 
			"	     ^^^^^^^^\n" + 
			"Cannot make a static reference to the non-static field inffield\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 19)\n" + 
			"	default: break;\n" + 
			"	^^^^^^^\n" + 
			"The default case is already defined\n" + 
			"----------\n";
				
		this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"    static final String field = \"123\";\n" +
			"    final String ifield = \"123\";\n" +
			"    String inffield = \"123\";\n" +
			"    static String nffield = \"123\";\n" +
			"    public static void main(String [] args, final String argument) {\n" +
			"        final String local = \"123\";\n" +
			"	switch(args[0]) {\n" + 
			"	   case \"123\": break;\n" +
			"      case \"\u0031\u0032\u0033\": break;\n" +
			"	   case \"1\" + \"2\" + \"3\": break;\n" +
			"           default: break;\n" +
			"	   case local: break;\n" +
			"           case field: break;\n" +
			"           case ifield: break;\n" +
			"           case inffield: break;\n" +
			"           case nffield: break;\n" +
			"           case argument: break;\n" +
			"           default: break;\n" +
			"	}\n" +
			"    }\n" +
			"}\n"
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
// JDK7: Strings in Switch.
public void testVariableCase() {
		String newMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	case local: break;\n" + 
			"	     ^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	case argument: break;\n" + 
			"	     ^^^^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	case inffield: break;\n" + 
			"	     ^^^^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	case nffield: break;\n" + 
			"	     ^^^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 11)\n" + 
			"	case argument: break;\n" + 
			"	     ^^^^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n";
		
		String oldMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	switch(args[0]) {\n" + 
			"	       ^^^^^^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n";
				
		this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"    String inffield = \"123\";\n" +
			"    static String nffield = \"123\";\n" +
			"    public void main(String [] args, final String argument) {\n" +
			"        String local = \"123\";\n" +
			"	switch(args[0]) {\n" + 
			"	   case local: break;\n" +
			"	   case argument: break;\n" +
			"      case inffield: break;\n" +
			"      case nffield: break;\n" +
			"      case argument: break;\n" +
			"	}\n" +
			"    }\n" +
			"}\n"
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
// JDK7: Strings in Switch.
public void testVariableCaseFinal() {
		String newMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	case argument: break;\n" + 
			"	     ^^^^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 11)\n" + 
			"	case argument: break;\n" + 
			"	     ^^^^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n";
		
		String oldMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	switch(args[0]) {\n" + 
			"	       ^^^^^^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n";
				
		this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"    final String inffield = \"12312\";\n" +
			"    final static String nffield = \"123123\";\n" +
			"    public void main(String [] args, final String argument) {\n" +
			"        final String local = \"1233\";\n" +
			"	switch(args[0]) {\n" + 
			"	   case local: break;\n" +
			"	   case argument: break;\n" +
			"      case inffield: break;\n" +
			"      case nffield: break;\n" +
			"      case argument: break;\n" +
			"	}\n" +
			"    }\n" +
			"}\n"
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
//JDK7: Strings in Switch.
public void testNullCase() {
		String newMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	case local: break;\n" + 
			"	     ^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	case argument: break;\n" + 
			"	     ^^^^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	case inffield: break;\n" + 
			"	     ^^^^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	case nffield: break;\n" + 
			"	     ^^^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 11)\n" + 
			"	case (String) null: break;\n" + 
			"	     ^^^^^^^^^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 12)\n" + 
			"	case true ? (String) null : (String) null : break;\n" + 
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"case expressions must be constant expressions\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 12)\n" + 
			"	case true ? (String) null : (String) null : break;\n" + 
			"	                            ^^^^^^^^^^^^^\n" + 
			"Dead code\n" + 
			"----------\n";
		
		String oldMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	switch(args[0]) {\n" + 
			"	       ^^^^^^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n";
				
		this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"    final String inffield = null;\n" +
			"    final static String nffield = null;\n" +
			"    public void main(String [] args, final String argument) {\n" +
			"        final String local = null;\n" +
			"	switch(args[0]) {\n" + 
			"	   case local: break;\n" +
			"	   case argument: break;\n" +
			"      case inffield: break;\n" +
			"      case nffield: break;\n" +
			"      case (String) null: break;\n" +
			"      case true ? (String) null : (String) null : break;\n" +
			"	}\n" +
			"    }\n" +
			"}\n"
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}
// JDK7: Strings in Switch.
public void testDuplicateCase3() {
		String newMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	case \"123\": break;\n" + 
			"	^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	case \"1\" + \"2\" + \"3\": break;\n" + 
			"	^^^^^^^^^^^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 12)\n" + 
			"	case local: break;\n" + 
			"	^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 13)\n" + 
			"	case field: break;\n" + 
			"	^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 14)\n" + 
			"	case ifield: break;\n" + 
			"	^^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 18)\n" + 
			"	default: break;\n" + 
			"	^^^^^^^\n" + 
			"The default case is already defined\n" + 
			"----------\n";
		
		String oldMessage = 
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	switch(args[0]) {\n" + 
			"	       ^^^^^^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 18)\n" + 
			"	default: break;\n" + 
			"	^^^^^^^\n" + 
			"The default case is already defined\n" + 
			"----------\n";
				
		this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" +
			"    static final String field = \"123\";\n" +
			"    final String ifield = \"123\";\n" +
			"    String inffield = \"123\";\n" +
			"    static String nffield = \"123\";\n" +
			"    public  void main(String [] args, final String argument) {\n" +
			"        final String local = \"123\";\n" +
			"	switch(args[0]) {\n" + 
			"	   case \"123\": break;\n" +
			"	   case \"1\" + \"2\" + \"3\": break;\n" +
			"           default: break;\n" +
			"	   case local: break;\n" +
			"           case field: break;\n" +
			"           case ifield: break;\n" +
			"           case inffield: break;\n" +
			"           case nffield: break;\n" +
			"           case argument: break;\n" +
			"           default: break;\n" +
			"	}\n" +
			"    }\n" +
			"}\n"
		},
		this.complianceLevel >= JDKLevelSupportingStringSwitch ? newMessage : oldMessage);
}

public void testDuplicateHashCode() {
	String errorMsg = 		
		"----------\n" + 
		"1. ERROR in testDuplicateHashCode.java (at line 5)\n" + 
		"	switch (dispatcher) {\n" + 
		"	        ^^^^^^^^^^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"testDuplicateHashCode.java",
		"public class testDuplicateHashCode {\n" +
		"	public static void main(String[] argv) {\n" +
		"		String dispatcher = \"\u0000\";\n" +
		"		outer: for (int i = 0; i < 100; i++) {\n" +
		"			switch (dispatcher) {\n" +
		"			case \"\u0000\":\n" +
		"				System.out.print(\"1 \");\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\":\n" +
		"				System.out.print(\"2 \");\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"3 \");\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"4 \");\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"5 \");\n" +
		"				break;\n" +
		"			default:\n" +
		"				System.out.println(\"Default\");\n" +
		"				break outer;\n" +
		"			case \"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"8 \");\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"7 \");\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\u0000\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"6 \");\n" +
		"				break;\n" +
		"			}\n" +
		"			dispatcher += \"\u0000\";\n" +
		"		}\n" +
		"	}\n" +
		"}\n",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "1 2 3 4 5 6 7 8 Default");
	}
}
public void testDuplicateHashCode2() {
	String errorMsg = 		
		"----------\n" + 
		"1. ERROR in testDuplicateHashCode.java (at line 5)\n" + 
		"	switch (dispatcher) {\n" + 
		"	        ^^^^^^^^^^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"testDuplicateHashCode.java",
		"public class testDuplicateHashCode {\n" +
		"	public static void main(String[] argv) {\n" +
		"		String dispatcher = \"\u0000\";\n" +
		"		outer: while(true) {\n" +
		"			switch (dispatcher) {\n" +
		"			case \"\u0000\":\n" +
		"				System.out.print(\"1 \");\n" +
		"               dispatcher += \"\u0000\u0000\";\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\":\n" +
		"				System.out.print(\"2 \");\n" +
		"               dispatcher = \"\";\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"3 \");\n" +
		"               dispatcher += \"\u0000\u0000\";\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"4 \");\n" +
		"               dispatcher = \"\u0000\u0000\";\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"5 \");\n" +
		"               dispatcher += \"\u0000\u0000\";\n" +
		"				break;\n" +
		"			default:\n" +
		"				System.out.println(\"Default\");\n" +
		"				break outer;\n" +
		"			case \"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"8 \");\n" +
		"               dispatcher = \"\u0000\u0000\u0000\u0000\u0000\u0000\";\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"7 \");\n" +
		"               dispatcher += \"\u0000\";\n" +
		"				break;\n" +
		"			case \"\u0000\u0000\u0000\u0000\u0000\u0000\":\n" +
		"				System.out.print(\"6 \");\n" +
		"               dispatcher = \"\u0000\u0000\u0000\u0000\";\n" +
		"				break;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "1 3 5 7 8 6 4 2 Default");
	}
}
public void testSwitchOnNull() {
	String errorMsg = 		
		"----------\n" + 
		"1. ERROR in testSwitchOnNull.java (at line 13)\n" + 
		"	switch (s) {\n" + 
		"	        ^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n" + 
		"2. ERROR in testSwitchOnNull.java (at line 23)\n" + 
		"	switch ((String) null) {\n" + 
		"	        ^^^^^^^^^^^^^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n" + 
		"3. ERROR in testSwitchOnNull.java (at line 33)\n" + 
		"	switch (someMethod()) {\n" + 
		"	        ^^^^^^^^^^^^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n" + 
		"4. ERROR in testSwitchOnNull.java (at line 40)\n" + 
		"	switch (nullString) {\n" + 
		"	        ^^^^^^^^^^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n" + 
		"5. ERROR in testSwitchOnNull.java (at line 47)\n" + 
		"	switch (someMethod()) {\n" + 
		"	        ^^^^^^^^^^^^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"testSwitchOnNull.java",
		"public class testSwitchOnNull {\n" +
		"\n" +
		"    private static String someMethod() {\n" +
		"        return null;\n" +
		"    }\n" +
		"\n" +
		"    static String nullString = null;\n" +
		"    public static void main(String [] args) {\n" +
		"\n" +
		"        String s = null;\n" +
		"\n" +
		"        try {\n" +
		"            switch (s) {\n" +
		"                default: \n" +
		"                    System.out.println(\"OOPS\");\n" +
		"	            break;\n" +
		"            }\n" +
		"            System.out.println(\"OOPS\");\n" +
		"        } catch (NullPointerException e) {\n" +
		"            System.out.print(\"NPE1\");\n" +
		"        }\n" +
		"        try {\n" +
		"            switch ((String) null) {\n" +
		"                default: \n" +
		"                    System.out.println(\"OOPS\");\n" +
		"	            break;\n" +
		"            }\n" +
		"            System.out.println(\"OOPS\");\n" +
		"        } catch (NullPointerException e) {\n" +
		"            System.out.print(\"NPE2\");\n" +
		"        }\n" +
		"        try {\n" +
		"            switch (someMethod()) {\n" +
		"            }\n" +
		"            System.out.println(\"OOPS\");\n" +
		"        } catch (NullPointerException e) {\n" +
		"            System.out.print(\"NPE3\");\n" +
		"        }\n" +
		"        try {\n" +
		"            switch (nullString) {\n" +
		"            }\n" +
		"            System.out.println(\"OOPS\");\n" +
		"        } catch (NullPointerException e) {\n" +
		"            System.out.print(\"NPE4\");\n" +
		"        }\n" +
		"        try {\n" +
		"            switch (someMethod()) {\n" +
		"                default: \n" +
		"                    System.out.println(\"OOPS\");\n" +
		"	            break;\n" +
		"            }\n" +
		"            System.out.println(\"OOPS\");\n" +
		"        } catch (NullPointerException e) {\n" +
		"            System.out.print(\"NPE5\");\n" +
		"        }\n" +
		"    }\n" +
		"}\n",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "NPE1NPE2NPE3NPE4NPE5");
	}
}
public void testSideEffect() {
	String errorMsg = 		
		"----------\n" + 
		"1. ERROR in testSideEffect.java (at line 11)\n" + 
		"	switch(dispatcher()) {\n" + 
		"	       ^^^^^^^^^^^^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"testSideEffect.java",
		"public class testSideEffect {\n" +
		"    static boolean firstTime = true;\n" +
		"	private static String dispatcher() {\n" +
		"    	if (!firstTime) {\n" +
		"		System.out.print(\"OOPS\");\n" +
		"    	}\n" +
		"    	firstTime = false;\n" +
		"    	return \"\u0000\";\n" +
		"    }\n" +
		"    public static void main(String [] args) {\n" +
		"    		switch(dispatcher()) {\n" +
		"    		case \"\u0000\u0000\": break;\n" +
		"    		case \"\u0000\u0000\u0000\":	break;\n" +
		"    		case \"\u0000\u0000\u0000\u0000\": break;\n" +
		"    		case \"\u0000\u0000\u0000\u0000\u0000\": break;\n" +
		"    		default: System.out.println(\"DONE\");\n" +
		"    		}\n" +
		"    }\n" +
		"}\n",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "DONE");
	}
}
public void testFallThrough() {
	String errorMsg = 		
		"----------\n" + 
		"1. ERROR in testFallThrough.java (at line 11)\n" + 
		"	switch(s = dispatcher()) {\n" + 
		"	       ^^^^^^^^^^^^^^^^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"testFallThrough.java",
		"public class testFallThrough {\n" +
		"    static int index = -1;\n" +
		"    static String string = \"0123456789*\";\n" +
		"    private static String dispatcher() {\n" +
		"    	index++;\n" +
		"     	return string.substring(index,index + 1);\n" +
		"    }\n" +
		"    public static void main(String [] args) {\n" +
		"    	outer: while (true) {\n" +
		"    		String s = null;\n" +
		"    		switch(s = dispatcher()) {\n" +
		"    		case \"2\":\n" +
		"    		case \"0\":\n" +
		"    		case \"4\":\n" +
		"    		case \"8\":\n" +
		"    		case \"6\":\n" +
		"    				System.out.print(s + \"(even) \");\n" +
		"    				break;\n" +
		"    		case \"1\":\n" +
		"    		case \"3\":\n" +
		"    		case \"9\":\n" +
		"    		case \"5\":\n" +
		"    		case \"7\":\n" +
		"    				System.out.print(s + \"(odd) \");\n" +
		"    				break;\n" +
		"    		default: System.out.print(\"DONE\");\n" +
		"    				break outer;\n" +
		"    		}\n" +
		"    	}\n" +
		"    }\n" +
		"}\n",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "0(even) 1(odd) 2(even) 3(odd) 4(even) 5(odd) 6(even) 7(odd) 8(even) 9(odd) DONE");
	}
}
public void testFallThrough2() {
	String errorMsg = 		
		"----------\n" + 
		"1. ERROR in testFallThrough.java (at line 11)\n" + 
		"	switch(s = dispatcher()) {\n" + 
		"	       ^^^^^^^^^^^^^^^^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"testFallThrough.java",
		"public class testFallThrough {\n" +
		"    static int index = -1;\n" +
		"    static String string = \"0123456789*\";\n" +
		"    private static String dispatcher() {\n" +
		"    	index++;\n" +
		"     	return string.substring(index,index + 1);\n" +
		"    }\n" +
		"    public static void main(String [] args) {\n" +
		"    	outer: while (true) {\n" +
		"    		String s = null;\n" +
		"    		switch(s = dispatcher()) {\n" +
		"    		case \"4\": System.out.print(s);\n" +
		"    		case \"3\": System.out.print(s);\n" +
		"    		case \"2\": System.out.print(s);\n" +
		"    		case \"1\": System.out.print(s + \" \");\n" +
		"    		case \"0\": break;\n" +
		"    		default: System.out.print(\"DONE\");\n" +
		"    				break outer;\n" +
		"    		}\n" +
		"    	}\n" +
		"    }\n" +
		"}\n",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "1 22 333 4444 DONE");
	}
}
public void testMarysLamb() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
		
	String errorMsg = 		
		"----------\n" + 
		"1. ERROR in testMarysLamb.java (at line 4)\n" + 
		"	switch(s) {\n" + 
		"	       ^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"testMarysLamb.java",
		"public class testMarysLamb {\n" +
		"    public static void main(String [] args) {\n" +
		"    	for (String s : new String [] { \"Mary\", \"Had\", \"A\", \"Little\", \"Lamb\" }) {\n" +
		"    		switch(s) {\n" +
		"    			default: System.out.print(s + \" \");\n" +
		"    		}\n" +
		"    	}\n" +
		"    }\n" +
		"}\n",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "Mary Had A Little Lamb");
	}
}
public void testBreakOut() {	
	String errorMsg = 		
		"----------\n" + 
		"1. ERROR in testBreakOut.java (at line 5)\n" + 
		"	switch(s) {\n" + 
		"	       ^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"testBreakOut.java",
		"public class testBreakOut {\n" +
		"    public static void main(String [] args) {\n" +
		"    	junk: while (true) {\n" +
		"    		String s = \"\";\n" +
		"    		switch(s) {\n" +
		"    		case \"7\":\n" +
		"    				System.out.print(s + \"(odd) \");\n" +
		"    				break;\n" +
		"    		default: System.out.print(\"DONE\");\n" +
		"    				 break junk;\n" +
		"    		}\n" +
		"    	}\n" +
		"    }\n" +
		"}\n",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "DONE");
	}
}
public void testMultipleSwitches() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	String errorMsg = 		
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	switch (s) {\n" + 
		"	        ^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 35)\n" + 
		"	switch (s) {\n" + 
		"	        ^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 51)\n" + 
		"	switch (s) {\n" + 
		"	        ^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"X.java",
		"public class X {\n" +
		"\n" +
		"	public static void main(String[] args) {\n" +
		"		\n" +
		"		for (String s: new String [] { \"Sunday\", \"Monday\", \"Tuesday\", \"Wednesday\", \"Thursday\", \"Friday\", \"Saturday\", \"DONE\"}) {\n" +
		"			switch (s) {\n" +
		"			case \"Sunday\" : \n" +
		"				System.out.print(\"Sunday\");\n" +
		"				break;\n" +
		"			case \"Monday\" :\n" +
		"				System.out.print(\"Monday\");\n" +
		"				break;\n" +
		"			case \"Tuesday\" :\n" +
		"				System.out.print(\"Tuesday\");\n" +
		"				break;\n" +
		"			case \"Wednesday\":\n" +
		"				System.out.print(\"Wednesday\");\n" +
		"				break;\n" +
		"			case \"Thursday\":\n" +
		"				System.out.print(\"Thursday\");\n" +
		"				break;\n" +
		"			case \"Friday\":\n" +
		"				System.out.print(\"Friday\");\n" +
		"				break;\n" +
		"			case \"Saturday\":\n" +
		"				System.out.print(\"Saturday\");\n" +
		"				break;\n" +
		"			default:\n" +
		"				System.out.print(\" ---- \");\n" +
		"				break;\n" +
		"			}\n" +
		"		}\n" +
		"	  \n" +
		"		for (String s: new String [] { \"Sunday\", \"Monday\", \"Tuesday\", \"Wednesday\", \"Thursday\", \"Friday\", \"Saturday\", \"DONE\"}) {\n" +
		"			switch (s) {\n" +
		"			case \"Sunday\" : \n" +
		"			case \"Monday\" :\n" +
		"			case \"Tuesday\" :\n" +
		"			case \"Wednesday\":\n" +
		"			case \"Thursday\":\n" +
		"			case \"Friday\":\n" +
		"			case \"Saturday\":\n" +
		"				System.out.print(s);\n" +
		"				break;\n" +
		"			default:\n" +
		"				System.out.print(\" ---- \");\n" +
		"				break;\n" +
		"			}	\n" +
		"		}\n" +
		"		for (String s: new String [] { \"Sunday\", \"Monday\", \"Tuesday\", \"Wednesday\", \"Thursday\", \"Friday\", \"Saturday\", \"DONE\"}) {\n" +
		"			switch (s) {\n" +
		"			case \"Saturday\":\n" +
		"			case \"Sunday\" : \n" +
		"				System.out.print(\"Holiday\");\n" +
		"				break;\n" +
		"			case \"Monday\" :\n" +
		"			case \"Tuesday\" :\n" +
		"			case \"Wednesday\":\n" +
		"			case \"Thursday\":\n" +
		"			case \"Friday\":\n" +
		"				System.out.print(\"Workday\");\n" +
		"				break;\n" +
		"			default:\n" +
		"				System.out.print(\" DONE\");\n" +
		"				break;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"\n" +
		"}\n",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "SundayMondayTuesdayWednesdayThursdayFridaySaturday ---- SundayMondayTuesdayWednesdayThursdayFridaySaturday ---- HolidayWorkdayWorkdayWorkdayWorkdayWorkdayHoliday DONE");
	}
}
public void testNestedSwitches() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	String errorMsg = 		
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	switch (s) {\n" + 
		"	        ^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	switch (s) {\n" + 
		"	        ^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 18)\n" + 
		"	switch (s) {\n" + 
		"	        ^\n" + 
		"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
		"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"X.java",
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		for (String s: new String [] { \"Sunday\", \"Monday\", \"Tuesday\", \"Wednesday\", \"Thursday\", \"Friday\", \"Saturday\", \"DONE\"}) {\n" +
		"			switch (s) {\n" +
		"			case \"Saturday\":\n" +
		"			case \"Sunday\" : \n" +
		"				switch (s) {\n" +
		"					case \"Saturday\" : System.out.println (\"Saturday is a holiday\"); break;\n" +
		"					case \"Sunday\"  :  System.out.println (\"Sunday is a holiday\"); break;\n" +
		"					default:          System.out.println(\"Broken\");\n" +
		"				}\n" +
		"				break;\n" +
		"			case \"Monday\" :\n" +
		"			case \"Tuesday\" :\n" +
		"			case \"Wednesday\":\n" +
		"			case \"Thursday\":\n" +
		"			case \"Friday\":\n" +
		"				switch (s) {\n" +
		"					case \"Monday\" :  System.out.println (\"Monday is a workday\"); break;\n" +
		"					case \"Tuesday\" : System.out.println (\"Tuesday is a workday\"); break;\n" +
		"					case \"Wednesday\": System.out.println (\"Wednesday is a workday\"); break;\n" +
		"					case \"Thursday\": System.out.println (\"Thursday is a workday\"); break;\n" +
		"					case \"Friday\":System.out.println (\"Friday is a workday\"); break;\n" +
		"					default: System.out.println(\"Broken\");\n" +
		"				}\n" +
		"				break;\n" +
		"			default:\n" +
		"				System.out.println(\"DONE\");\n" +
		"				break;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "Sunday is a holiday\n" +
										 "Monday is a workday\n" +
										 "Tuesday is a workday\n" +
										 "Wednesday is a workday\n" +
										 "Thursday is a workday\n" +
										 "Friday is a workday\n" +
										 "Saturday is a holiday\n" +
										 "DONE");
	}
}
public void testFor356002() {
	String errorMsg = 		
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	switch (foo()) {\n" + 
			"	        ^^^^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"X.java",
		"public class X {\n" + 
		"	private static String foo() {\n" + 
		"		return \"\";\n" + 
		"	}\n" + 
		"	public static void main(String[] args) {\n" + 
		"		switch (foo()) {\n" + 
		"			default: {\n" + 
		"				int j = 0;\n" + 
		"				if (j <= 0)\n" + 
		"					System.out.println(\"DONE\");\n" +
		"			}\n" + 
		"			return;\n" + 
		"		}\n" + 
		"	}\n" + 
		"}",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "DONE");
	}
}
public void testFor356002_2() {
	String errorMsg = 		
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	switch (\"\") {\n" + 
			"	        ^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"X.java",
		"public class X {\n" + 
		"	public static void main(String[] args) {\n" + 
		"		switch (\"\") {\n" + 
		"			default: {\n" + 
		"				int j = 0;\n" + 
		"				if (j <= 0)\n" + 
		"					System.out.println(\"DONE\");\n" +
		"			}\n" + 
		"			return;\n" + 
		"		}\n" + 
		"	}\n" + 
		"}",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "DONE");
	}
}
public void testFor356002_3() {
	String errorMsg =
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	switch (foo()) {\n" + 
			"	        ^^^^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n";
	
	String [] sourceFiles =
		new String[] {
		"X.java",
		"public class X {\n" + 
		"	private static String foo() {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"	public static void main(String[] args) {\n" +
		"		try {\n" +
		"			switch (foo()) {\n" + 
		"				default: {\n" + 
		"					int j = 0;\n" + 
		"					if (j <= 0)\n" + 
		"						;\n" +
		"				}\n" + 
		"				return;\n" + 
		"			}\n" + 
		"		} catch(NullPointerException e) {\n" +
		"			System.out.println(\"DONE\");\n" +
		"		}\n" +
		"	}\n" + 
		"}",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "DONE");
	}
}
public void testBug374605() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"class X {\n" +
				"  void v(int i) {\n" +
				"    switch (i) {\n" +
				"      case 1 :\n" +
				"        break;\n" +
				"      case 2 :\n" +
				"        break;\n" +
				"    }\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. WARNING in p\\X.java (at line 4)\n" +
			"	switch (i) {\n" +
			"	        ^\n" +
			"The switch statement should have a default case\n" +
			"----------\n",
			null,
			true,
			options
		);	
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927() {
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
				"        System.out.println(\"Success\");\n" +
				"    }\n" +
				"}\n",
			},
			"Success");	
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public final static Object f() {\n" +
				"        final Object a = null;\n" +
				"        Object b;\n" +
				"        label: while (true) {\n" +
				"            switch (0) {\n" +
				"            case 1: {\n" +
				"                b = a;\n" +
				"            }\n" +
				"                break;\n" +
				"            default:\n" +
				"                break label;\n" +
				"            }\n" +
				"        }\n" +
				"        return a;\n" +
				"    }\n" +
				"    public static void main(final String[] args) {\n" +
				"        f();\n" +
				"        System.out.println(\"Success\");\n" +
				"    }\n" +
				"}\n",
			},
			"Success");	
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public final static Object f() {\n" +
				"        final Object a = null;\n" +
				"        Object b;\n" +
				"        label: for(;;) {\n" +
				"            switch (0) {\n" +
				"            case 1: {\n" +
				"                b = a;\n" +
				"            }\n" +
				"                break;\n" +
				"            default:\n" +
				"                break label;\n" +
				"            }\n" +
				"        }\n" +
				"        return a;\n" +
				"    }\n" +
				"    public static void main(final String[] args) {\n" +
				"        f();\n" +
				"        System.out.println(\"Success\");\n" +
				"    }\n" +
				"}\n",
			},
			"Success");	
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public final static Object f() {\n" +
				"        final Object a = null;\n" +
				"        Object b;\n" +
				"        label: for(int i : new int [] { 10 }) {\n" +
				"            switch (0) {\n" +
				"            case 1: {\n" +
				"                b = a;\n" +
				"            }\n" +
				"                break;\n" +
				"            default:\n" +
				"                break label;\n" +
				"            }\n" +
				"        }\n" +
				"        return a;\n" +
				"    }\n" +
				"    public static void main(final String[] args) {\n" +
				"        f();\n" +
				"        System.out.println(\"Success\");\n" +
				"    }\n" +
				"}\n",
			},
			"Success");	
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927d() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        Object b;\n" +
				"        label: do {\n" +
				"            switch (0) {\n" +
				"            case 1:\n" +
				"                b = null;\n" +
				"                break;\n" +
				"            default:\n" +
				"                break label;\n" +
				"            }\n" +
				"        } while (true);\n" +
				"        System.out.println(b);\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	System.out.println(b);\n" + 
			"	                   ^\n" + 
			"The local variable b may not have been initialized\n" + 
			"----------\n");	
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927e() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        Object b;\n" +
				"        label: while (true) {\n" +
				"            switch (0) {\n" +
				"            case 1:\n" +
				"                b = null;\n" +
				"                break;\n" +
				"            default:\n" +
				"                break label;\n" +
				"            }\n" +
				"        }\n" +
				"        System.out.println(b);\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	System.out.println(b);\n" + 
			"	                   ^\n" + 
			"The local variable b may not have been initialized\n" + 
			"----------\n");	
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927f() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        Object b;\n" +
				"        label: for(;;) {\n" +
				"            switch (0) {\n" +
				"            case 1:\n" +
				"                b = null;\n" +
				"                break;\n" +
				"            default:\n" +
				"                break label;\n" +
				"            }\n" +
				"        }\n" +
				"        System.out.println(b);\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	System.out.println(b);\n" + 
			"	                   ^\n" + 
			"The local variable b may not have been initialized\n" + 
			"----------\n");	
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=380927
public void testBug380927g() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        Object b;\n" +
				"        label: for(int i : new int [] { 10 }) {\n" +
				"            switch (0) {\n" +
				"            case 1:\n" +
				"                b = null;\n" +
				"                break;\n" +
				"            default:\n" +
				"                break label;\n" +
				"            }\n" +
				"        }\n" +
				"        System.out.println(b);\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	System.out.println(b);\n" + 
			"	                   ^\n" + 
			"The local variable b may not have been initialized\n" + 
			"----------\n");	
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383629
// To check that code gen is ok
public void testBug383629() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	  public static void main(String[] args) {\n" +           
			"	    char  chc;         \n" +
			"	     do {      \n" +                   
			"	        if (args == null) {      \n" +                                       
			"	           switch ('a') {     \n" +                                        
			"	           case '\\n':      \n" +            
			"	                 chc = 'b';\n" +
			"	           }               \n" +
			"	        } else {            \n" +   
			"	           switch ('a') {       \n" +           
			"	              case '\\r':\n" +
			"	           }          \n" +     
			"	        }\n" +
			"	     } while (false);\n" +
			"	     System.out.println(\"Done\");\n" +
			"	  }\n" +
			"}",
		}); // custom requestor
	
	String expectedOutput = this.complianceLevel < ClassFileConstants.JDK1_6 ?
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 61] local: args index: 0 type: java.lang.String[]\n":
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 61] local: args index: 0 type: java.lang.String[]\n" + 
				"      Stack map table: number of frames 4\n" + 
				"        [pc: 24, same]\n" + 
				"        [pc: 27, same]\n" + 
				"        [pc: 30, same]\n" + 
				"        [pc: 52, same]\n";
	
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

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=381172
// To check that code gen is ok
public void testBug381172() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public static void main(String[] args){\n" + 
			"        System.out.println(\"Test\");\n" + 
			"    }\n" + 
			"    public void method() {\n" + 
			"        try {\n" + 
			"            int rc;\n" + 
			"            switch ( 0 )\n" + 
			"            {\n" + 
			"                case 0:\n" + 
			"                    rc = 0;\n" + 
			"                    setRC( rc );\n" + 
			"                    break;\n" + 
			"                case 1:\n" + 
			"                    rc = 1;\n" + 
			"                    setRC( 0 );\n" + 
			"                    break;\n" + 
			"                case 2:\n" + 
			"                    rc = 2;\n" + 
			"                    setRC( 0 );\n" + 
			"                    break;\n" + 
			"                default:\n" + 
			"                    break;\n" + 
			"            }\n" + 
			"        }\n" + 
			"        catch ( final Exception ex ) {}\n" + 
			"    }\n" + 
			"    private void setRC(int rc) {}\n" + 
			"}",
		}); // custom requestor
	
	String expectedOutput = this.complianceLevel < ClassFileConstants.JDK1_6 ?
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 1] local: this index: 0 type: X\n" + 
			"        [pc: 0, pc: 1] local: rc index: 1 type: int\n":
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 63] local: this index: 0 type: X\n" + 
				"        [pc: 30, pc: 38] local: rc index: 1 type: int\n" + 
				"        [pc: 40, pc: 48] local: rc index: 1 type: int\n" + 
				"        [pc: 50, pc: 58] local: rc index: 1 type: int\n" + 
				"      Stack map table: number of frames 6\n" + 
				"        [pc: 28, same]\n" + 
				"        [pc: 38, same]\n" + 
				"        [pc: 48, same]\n" + 
				"        [pc: 58, same]\n" + 
				"        [pc: 61, same_locals_1_stack_item, stack: {java.lang.Exception}]\n" + 
				"        [pc: 62, same]\n";
	
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383643, NPE in problem reporter.
public void test383643() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"    void foo() {\n" +
					"        String s;\n" +
					"        switch (p) {\n" +
					"            case ONE:\n" +
					"                s= \"1\";\n" +
					"                break;\n" +
					"            case TWO:\n" +
					"                s= \"2\";\n" +
					"                break;\n" +
					"        }\n" +
					"\n" +
					"        s.toString();\n" +
					"    }\n" +
					"}\n",
				},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	switch (p) {\n" + 
			"	        ^\n" + 
			"p cannot be resolved to a variable\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	switch (p) {\n" + 
			"	        ^\n" + 
			"The switch statement should have a default case\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	case ONE:\n" + 
			"	     ^^^\n" + 
			"ONE cannot be resolved to a variable\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 8)\n" + 
			"	case TWO:\n" + 
			"	     ^^^\n" + 
			"TWO cannot be resolved to a variable\n" + 
			"----------\n",
			null,
			true,
			options
		);	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=387146 - the fall-through comment is ignored
public void test387146a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"public class X {\n" +
		"	private Object someLock;\n" +
		"	public void foo1(int i) {\n" +
		"		switch (i) {\n" +
		"		case 1:\n" +
		"			synchronized (someLock) {\n" +
		"				System.out.println();\n" +
		"			}\n" +
		"			//$FALL-THROUGH$\n" +
		"		case 2:\n" +
		"			System.out.println();\n" +
		"			break;\n" +
		"		default:\n" +
		"			System.out.println();\n" +
		"		}\n" +
		"	}\n" +
		"}\n",
	},
	"",
	null,
	true,
	options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=387146 - the fall-through comment is respected
public void test387146b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
		"X.java",
		"public class X {\n" +
		"	private boolean someFlag;\n" +
		"	public void foo1(int i) {\n" +
		"		switch (i) {\n" +
		"		case 1:\n" +
		"			if (someFlag) {\n" +
		"				System.out.println();\n" +
		"			}\n" +
		"			//$FALL-THROUGH$\n" +
		"		case 2:\n" +
		"			System.out.println();\n" +
		"			break;\n" +
		"		default:\n" +
		"			System.out.println();\n" +
		"		}\n" +
		"	}\n" +
		"}\n",
	},
	"",
	null,
	true,
	options);
}
//JDK7: Strings in Switch.
public void test393537() {
	String errorMsg = 		
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	switch (\"\") {\n" + 
			"	        ^^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"X.java",
		"public class X {\n" + 
		"	public static void main(String[] args) {\n" + 
		"		switch (\"\") {\n" + 
		"			case \"\":\n" + 
		"			default:\n" + 
		"		}\n" + 
		"	}\n" + 
		"}",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		this.runConformTest(sourceFiles, "");
	}
}
//JDK7: Strings in Switch.
public void test410892() {
	String errorMsg = 		
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	switch (s) {\n" + 
			"	        ^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"X.java",
		"public class X {\n" + 
		"   public void testFunction(String s) {\n" + 
		"        int var1 = 0;\n" + 
		"        int var2 = 0;\n" + 
		"        switch (s) {\n" + 
		"        case \"test\": \n" + 
		"            var2 = ++var1 % 2;\n" + 
		"            break;\n" + 
		"        }\n" + 
		"   }\n" + 
		"}",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		this.runConformTest(sourceFiles, options);
	}
}
//JDK7: Strings in Switch.
public void test410892_2() {
	String errorMsg = 		
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	switch (s) {\n" + 
			"	        ^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n";

	String [] sourceFiles = 
		new String[] {
		"X.java",
		"public class X {\n" + 
		"   public X(String s) {\n" + 
		"        int var1 = 0;\n" + 
		"        int var2 = 0;\n" + 
		"        switch (s) {\n" + 
		"        case \"test\": \n" + 
		"            var2 = ++var1 % 2;\n" + 
		"            break;\n" + 
		"        }\n" + 
		"   }\n" + 
		"}",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		this.runConformTest(sourceFiles, options);
	}
}
//JDK7: Strings in Switch.
public void test410892_3() {
	String errorMsg = 		
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	switch (s) {\n" + 
			"	        ^\n" + 
			"Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum variables are permitted\n" + 
			"----------\n";
	
	String [] sourceFiles = 
		new String[] {
		"X.java",
		"public class X {\n" + 
		"   static {\n" + 
		"        int var1 = 0;\n" + 
		"        int var2 = 0;\n" +
		"        String s = \"test2\";\n" +
		"        switch (s) {\n" + 
		"        case \"test\": \n" + 
		"            var2 = ++var1 % 2;\n" + 
		"            break;\n" + 
		"        }\n" + 
		"   }\n" + 
		"}",
	};
	if (this.complianceLevel < JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles, errorMsg);
	} else {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		this.runConformTest(sourceFiles, options);
	}
}
//JDK7: Strings in Switch.
public void test410892_4() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	String errorMsg = 		
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	int var2 = 0;\n" + 
			"	    ^^^^\n" + 
			"The value of the local variable var2 is not used\n" + 
			"----------\n";
	String [] sourceFiles = 
		new String[] {
		"X.java",
		"public class X {\n" + 
		"   public void testFunction(String s) {\n" + 
		"        int var1 = 0;\n" + 
		"        int var2 = 0;\n" + 
		"        switch (s) {\n" + 
		"        case \"test\": \n" + 
		"            var2 = ++var1 % 2;\n" + 
		"            break;\n" + 
		"        }\n" + 
		"   }\n" + 
		"}",
	};
	if (this.complianceLevel >= JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles,
			errorMsg,
			null,
			true,
			options);
	}
}
//JDK7: Strings in Switch.
public void test410892_5() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	String errorMsg = 		
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	int var2 = 0;\n" + 
			"	    ^^^^\n" + 
			"The value of the local variable var2 is not used\n" + 
			"----------\n";
	String [] sourceFiles = 
		new String[] {
		"X.java",
		"public class X {\n" + 
		"   public X(String s) {\n" + 
		"        int var1 = 0;\n" + 
		"        int var2 = 0;\n" + 
		"        switch (s) {\n" + 
		"        case \"test\": \n" + 
		"            var2 = ++var1 % 2;\n" + 
		"            break;\n" + 
		"        }\n" + 
		"   }\n" + 
		"}",
	};
	if (this.complianceLevel >= JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles,
			errorMsg,
			null,
			true,
			options);
	}
}
//JDK7: Strings in Switch.
public void test410892_6() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	String errorMsg = 		
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	int var2 = 0;\n" + 
			"	    ^^^^\n" + 
			"The value of the local variable var2 is not used\n" + 
			"----------\n";
	String [] sourceFiles = 
		new String[] {
		"X.java",
		"public class X {\n" + 
		"   static {\n" + 
		"        int var1 = 0;\n" + 
		"        int var2 = 0;\n" + 
		"        String s = \"Test2\";\n" + 
		"        switch (s) {\n" + 
		"        case \"test\": \n" + 
		"            var2 = ++var1 % 2;\n" + 
		"            break;\n" + 
		"        }\n" + 
		"   }\n" + 
		"}",
	};
	if (this.complianceLevel >= JDKLevelSupportingStringSwitch) {
		this.runNegativeTest(sourceFiles,
			errorMsg,
			null,
			true,
			options);
	}
}
public void test526911() {
	String [] sourceFiles = 
		new String[] {
		"Main.java",
		"public class Main {\n" + 
		"	public static void main(String[] args) {\n" + 
		"		new Main().run();\n" + 
		"	}\n" + 
		"	\n" + 
		"	private void run() {\n" + 
		"		V v = new VA();\n" + 
		"		I i = I.create(v);\n" + 
		"		System.out.printf(\"%d %d\", i.m1(), i.m2());\n" + 
		"	}\n" + 
		"}\n",
		"XI.java",
		"public class XI implements I {\n" + 
		"	V v;\n" + 
		"	public XI(V v) {\n" + 
		"		this.v = v;\n" + 
		"	}\n" + 
		"	@Override\n" + 
		"	public int m1() {\n" + 
		"		return 1;\n" + 
		"	}\n" + 
		"	@Override\n" + 
		"	public int m2() {\n" + 
		"		return 11;\n" + 
		"	}\n" + 
		"}\n",
		"YI.java",
		"public class YI implements I {\n" + 
		"	V v;\n" + 
		"	public YI(V v) {\n" + 
		"		this.v = v;\n" + 
		"	}\n" + 
		"	@Override\n" + 
		"	public int m1() {\n" + 
		"		return 2;\n" + 
		"	}\n" + 
		"	@Override\n" + 
		"	public int m2() {\n" + 
		"		return 22;\n" + 
		"	}\n" + 
		"}\n",
		"V.java",
		"public class V {\n" + 
		"	public enum T { A, B, C }\n" + 
		"	private T t;\n" + 
		"	public V(T t) {\n" + 
		"		this.t = t;\n" + 
		"	}\n" + 
		"	public T getT() { return t; }\n" + 
		"}\n" +
		"class VA extends V {\n" + 
		"	VA() {\n" + 
		"		super(T.A);\n" + 
		"	}\n" + 
		"}",
		"I.java",
		"enum H { X, Y }\n" + 
		"public interface I {\n" + 
		"	public static final int i = 0;\n" + 
		"	public int m1();\n" + 
		"	public int m2();\n" + 
		"	public static I create(V v) { \n" + 
		"		V.T t = v.getT();\n" + 
		"		H h = getH(t);\n" + 
		"		switch (h) { // depending on H i need different implementations of I. XI and YI provide them\n" + 
		"		case X:\n" + 
		"			return new XI(v);\n" + 
		"		case Y:\n" + 
		"			return new YI(v);\n" + 
		"		default:\n" + 
		"			throw new Error();\n" + 
		"		}	\n" + 
		"	}\n" + 
		"	static H getH(V.T t) { // different T's require different H's to handle them\n" + 
		"		switch (t) {\n" + 
		"		case A:\n" + 
		"			return H.X;\n" + 
		"		case B:\n" + 
		"		case C:\n" + 
		"			return H.Y;\n" + 
		"		}\n" + 
		"		throw new Error();\n" + 
		"	}\n" + 
		"}",
		"X.java",
		"public class X {\n" + 
		"   static {\n" + 
		"        int var1 = 0;\n" + 
		"        int var2 = 0;\n" + 
		"        String s = \"Test2\";\n" + 
		"        switch (s) {\n" + 
		"        case \"test\": \n" + 
		"            var2 = ++var1 % 2;\n" + 
		"            break;\n" + 
		"        }\n" + 
		"   }\n" + 
		"}",
	};
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		this.runConformTest(sourceFiles, "1 11");
	}
}
public void test526911a() {
	// target 1.8, run with 9, should work fine 
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
	String [] sourceFiles = 
		new String[] {
		"Main.java",
		"public class Main {\n" + 
		"	public static void main(String[] args) {\n" + 
		"		new Main().run();\n" + 
		"	}\n" + 
		"	\n" + 
		"	private void run() {\n" + 
		"		V v = new VA();\n" + 
		"		I i = I.create(v);\n" + 
		"		System.out.printf(\"%d %d\", i.m1(), i.m2());\n" + 
		"	}\n" + 
		"}\n",
		"XI.java",
		"public class XI implements I {\n" + 
		"	V v;\n" + 
		"	public XI(V v) {\n" + 
		"		this.v = v;\n" + 
		"	}\n" + 
		"	@Override\n" + 
		"	public int m1() {\n" + 
		"		return 1;\n" + 
		"	}\n" + 
		"	@Override\n" + 
		"	public int m2() {\n" + 
		"		return 11;\n" + 
		"	}\n" + 
		"}\n",
		"YI.java",
		"public class YI implements I {\n" + 
		"	V v;\n" + 
		"	public YI(V v) {\n" + 
		"		this.v = v;\n" + 
		"	}\n" + 
		"	@Override\n" + 
		"	public int m1() {\n" + 
		"		return 2;\n" + 
		"	}\n" + 
		"	@Override\n" + 
		"	public int m2() {\n" + 
		"		return 22;\n" + 
		"	}\n" + 
		"}\n",
		"V.java",
		"public class V {\n" + 
		"	public enum T { A, B, C }\n" + 
		"	private T t;\n" + 
		"	public V(T t) {\n" + 
		"		this.t = t;\n" + 
		"	}\n" + 
		"	public T getT() { return t; }\n" + 
		"}\n" +
		"class VA extends V {\n" + 
		"	VA() {\n" + 
		"		super(T.A);\n" + 
		"	}\n" + 
		"}",
		"I.java",
		"enum H { X, Y }\n" + 
		"public interface I {\n" + 
		"	public static final int i = 0;\n" + 
		"	public int m1();\n" + 
		"	public int m2();\n" + 
		"	public static I create(V v) { \n" + 
		"		V.T t = v.getT();\n" + 
		"		H h = getH(t);\n" + 
		"		switch (h) { // depending on H i need different implementations of I. XI and YI provide them\n" + 
		"		case X:\n" + 
		"			return new XI(v);\n" + 
		"		case Y:\n" + 
		"			return new YI(v);\n" + 
		"		default:\n" + 
		"			throw new Error();\n" + 
		"		}	\n" + 
		"	}\n" + 
		"	static H getH(V.T t) { // different T's require different H's to handle them\n" + 
		"		switch (t) {\n" + 
		"		case A:\n" + 
		"			return H.X;\n" + 
		"		case B:\n" + 
		"		case C:\n" + 
		"			return H.Y;\n" + 
		"		}\n" + 
		"		throw new Error();\n" + 
		"	}\n" + 
		"}",
		"X.java",
		"public class X {\n" + 
		"   static {\n" + 
		"        int var1 = 0;\n" + 
		"        int var2 = 0;\n" + 
		"        String s = \"Test2\";\n" + 
		"        switch (s) {\n" + 
		"        case \"test\": \n" + 
		"            var2 = ++var1 % 2;\n" + 
		"            break;\n" + 
		"        }\n" + 
		"   }\n" + 
		"}",
	};
	this.runConformTest(sourceFiles, "1 11", options);
}
public static Class testClass() {
	return SwitchTest.class;
}
}

