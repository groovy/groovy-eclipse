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

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class Compliance_CLDC extends AbstractRegressionTest {

public Compliance_CLDC(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.3
 */
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_3);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_CLDC1_1);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
	return options;
}
public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(), ClassFileConstants.JDK1_3);
}
public static Class testClass() {
	return Compliance_CLDC.class;
}
// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "Bug58069" };
//		TESTS_NUMBERS = new int[] { 104 };
//		TESTS_RANGE = new int[] { 76, -1 };
}
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.awt.Image;\n" +
			"import java.awt.Toolkit;\n" +
			"import java.awt.image.ImageProducer;\n" +
			"import java.net.URL;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"	public Image loadImage(String name) {\n" +
			"		Toolkit toolkit= Toolkit.getDefaultToolkit();\n" +
			"		try {\n" +
			"			URL url= X.class.getResource(name);\n" +
			"			return toolkit.createImage((ImageProducer) url.getContent());\n" +
			"		} catch (Exception ex) {\n" +
			"		}\n" +
			"		return null;\n" +
			"	}\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"			System.out.println(\"OK\");\n" +
			"	}\n" +
			"}",
		},
		"OK");
}
public void test002() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"			System.out.print(X.class != null);\n" +
			"			System.out.print(String.class != null);\n" +
			"			System.out.print(Object.class != null);\n" +
			"			System.out.print(X.class != null);\n" +
			"	}\n" +
			"}",
		},
		"truetruetruetrue");

	String expectedOutput =
		"  // Method descriptor #20 ([Ljava/lang/String;)V\n" +
		"  // Stack: 3, Locals: 1\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"      0  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
		"      3  getstatic X.class$0 : java.lang.Class [27]\n" +
		"      6  dup\n" +
		"      7  ifnonnull 35\n" +
		"     10  pop\n" +
		"     11  ldc <String \"X\"> [29]\n" +
		"     13  invokestatic java.lang.Class.forName(java.lang.String) : java.lang.Class [30]\n" +
		"     16  dup\n" +
		"     17  putstatic X.class$0 : java.lang.Class [27]\n" +
		"     20  goto 35\n" +
		"     23  new java.lang.NoClassDefFoundError [36]\n" +
		"     26  dup_x1\n" +
		"     27  swap\n" +
		"     28  invokevirtual java.lang.Throwable.getMessage() : java.lang.String [38]\n" +
		"     31  invokespecial java.lang.NoClassDefFoundError(java.lang.String) [44]\n" +
		"     34  athrow\n" +
		"     35  ifnull 42\n" +
		"     38  iconst_1\n" +
		"     39  goto 43\n" +
		"     42  iconst_0\n" +
		"     43  invokevirtual java.io.PrintStream.print(boolean) : void [47]\n" +
		"     46  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
		"     49  getstatic X.class$1 : java.lang.Class [53]\n" +
		"     52  dup\n" +
		"     53  ifnonnull 81\n" +
		"     56  pop\n" +
		"     57  ldc <String \"java.lang.String\"> [55]\n" +
		"     59  invokestatic java.lang.Class.forName(java.lang.String) : java.lang.Class [30]\n" +
		"     62  dup\n" +
		"     63  putstatic X.class$1 : java.lang.Class [53]\n" +
		"     66  goto 81\n" +
		"     69  new java.lang.NoClassDefFoundError [36]\n" +
		"     72  dup_x1\n" +
		"     73  swap\n" +
		"     74  invokevirtual java.lang.Throwable.getMessage() : java.lang.String [38]\n" +
		"     77  invokespecial java.lang.NoClassDefFoundError(java.lang.String) [44]\n" +
		"     80  athrow\n" +
		"     81  ifnull 88\n" +
		"     84  iconst_1\n" +
		"     85  goto 89\n" +
		"     88  iconst_0\n" +
		"     89  invokevirtual java.io.PrintStream.print(boolean) : void [47]\n" +
		"     92  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
		"     95  getstatic X.class$2 : java.lang.Class [57]\n" +
		"     98  dup\n" +
		"     99  ifnonnull 127\n" +
		"    102  pop\n" +
		"    103  ldc <String \"java.lang.Object\"> [59]\n" +
		"    105  invokestatic java.lang.Class.forName(java.lang.String) : java.lang.Class [30]\n" +
		"    108  dup\n" +
		"    109  putstatic X.class$2 : java.lang.Class [57]\n" +
		"    112  goto 127\n" +
		"    115  new java.lang.NoClassDefFoundError [36]\n" +
		"    118  dup_x1\n" +
		"    119  swap\n" +
		"    120  invokevirtual java.lang.Throwable.getMessage() : java.lang.String [38]\n" +
		"    123  invokespecial java.lang.NoClassDefFoundError(java.lang.String) [44]\n" +
		"    126  athrow\n" +
		"    127  ifnull 134\n" +
		"    130  iconst_1\n" +
		"    131  goto 135\n" +
		"    134  iconst_0\n" +
		"    135  invokevirtual java.io.PrintStream.print(boolean) : void [47]\n" +
		"    138  getstatic java.lang.System.out : java.io.PrintStream [21]\n" +
		"    141  getstatic X.class$0 : java.lang.Class [27]\n" +
		"    144  dup\n" +
		"    145  ifnonnull 173\n" +
		"    148  pop\n" +
		"    149  ldc <String \"X\"> [29]\n" +
		"    151  invokestatic java.lang.Class.forName(java.lang.String) : java.lang.Class [30]\n" +
		"    154  dup\n" +
		"    155  putstatic X.class$0 : java.lang.Class [27]\n" +
		"    158  goto 173\n" +
		"    161  new java.lang.NoClassDefFoundError [36]\n" +
		"    164  dup_x1\n" +
		"    165  swap\n" +
		"    166  invokevirtual java.lang.Throwable.getMessage() : java.lang.String [38]\n" +
		"    169  invokespecial java.lang.NoClassDefFoundError(java.lang.String) [44]\n" +
		"    172  athrow\n" +
		"    173  ifnull 180\n" +
		"    176  iconst_1\n" +
		"    177  goto 181\n" +
		"    180  iconst_0\n" +
		"    181  invokevirtual java.io.PrintStream.print(boolean) : void [47]\n" +
		"    184  return\n" +
		"      Exception Table:\n" +
		"        [pc: 11, pc: 16] -> 23 when : java.lang.ClassNotFoundException\n" +
		"        [pc: 57, pc: 62] -> 69 when : java.lang.ClassNotFoundException\n" +
		"        [pc: 103, pc: 108] -> 115 when : java.lang.ClassNotFoundException\n" +
		"        [pc: 149, pc: 154] -> 161 when : java.lang.ClassNotFoundException\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 46, line: 5]\n" +
		"        [pc: 92, line: 6]\n" +
		"        [pc: 138, line: 7]\n" +
		"        [pc: 184, line: 8]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 185] local: args index: 0 type: java.lang.String[]\n" +
		"      Stack map : number of frames 16\n" +
		"        [pc: 23, full, stack: {java.lang.ClassNotFoundException}, locals: {java.lang.String[]}]\n" +
		"        [pc: 35, full, stack: {java.io.PrintStream, java.lang.Class}, locals: {java.lang.String[]}]\n" +
		"        [pc: 42, full, stack: {java.io.PrintStream}, locals: {java.lang.String[]}]\n" +
		"        [pc: 43, full, stack: {java.io.PrintStream, int}, locals: {java.lang.String[]}]\n" +
		"        [pc: 69, full, stack: {java.lang.ClassNotFoundException}, locals: {java.lang.String[]}]\n" +
		"        [pc: 81, full, stack: {java.io.PrintStream, java.lang.Class}, locals: {java.lang.String[]}]\n" +
		"        [pc: 88, full, stack: {java.io.PrintStream}, locals: {java.lang.String[]}]\n" +
		"        [pc: 89, full, stack: {java.io.PrintStream, int}, locals: {java.lang.String[]}]\n" +
		"        [pc: 115, full, stack: {java.lang.ClassNotFoundException}, locals: {java.lang.String[]}]\n" +
		"        [pc: 127, full, stack: {java.io.PrintStream, java.lang.Class}, locals: {java.lang.String[]}]\n" +
		"        [pc: 134, full, stack: {java.io.PrintStream}, locals: {java.lang.String[]}]\n" +
		"        [pc: 135, full, stack: {java.io.PrintStream, int}, locals: {java.lang.String[]}]\n" +
		"        [pc: 161, full, stack: {java.lang.ClassNotFoundException}, locals: {java.lang.String[]}]\n" +
		"        [pc: 173, full, stack: {java.io.PrintStream, java.lang.Class}, locals: {java.lang.String[]}]\n" +
		"        [pc: 180, full, stack: {java.io.PrintStream}, locals: {java.lang.String[]}]\n" +
		"        [pc: 181, full, stack: {java.io.PrintStream, int}, locals: {java.lang.String[]}]\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
public void test003() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"			System.out.print(int.class != null);\n" +
			"	}\n" +
			"}",
		},
		"true");

	String expectedOutput =
		"// Compiled from X.java (version 1.1 : 45.3, super bit)\n" +
		"public class X {\n" +
		"  \n" +
		"  // Method descriptor #6 ()V\n" +
		"  // Stack: 1, Locals: 1\n" +
		"  public X();\n" +
		"    0  aload_0 [this]\n" +
		"    1  invokespecial java.lang.Object() [8]\n" +
		"    4  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 1]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
		"  \n" +
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"     3  getstatic java.lang.Integer.TYPE : java.lang.Class [22]\n" +
		"     6  ifnull 13\n" +
		"     9  iconst_1\n" +
		"    10  goto 14\n" +
		"    13  iconst_0\n" +
		"    14  invokevirtual java.io.PrintStream.print(boolean) : void [28]\n" +
		"    17  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 4]\n" +
		"        [pc: 17, line: 5]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 18] local: args index: 0 type: java.lang.String[]\n" +
		"      Stack map : number of frames 2\n" +
		"        [pc: 13, full, stack: {java.io.PrintStream}, locals: {java.lang.String[]}]\n" +
		"        [pc: 14, full, stack: {java.io.PrintStream, int}, locals: {java.lang.String[]}]\n" +
		"}";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
public void test004() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.InputStream;\n" +
			"\n" +
			"public class X {\n" +
			"	private static final Y[] A = new Y[1];\n" +
			"\n" +
			"	public static void x() {\n" +
			"		for (int i = 0; i < 0; i++) {\n" +
			"			try {\n" +
			"				A[i] = foo(X.class.getResourceAsStream(\"\"), null);\n" +
			"			} catch (Throwable e) {\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public static boolean a = false;\n" +
			"\n" +
			"	private static int b = -1;\n" +
			"\n" +
			"	private static int C = 0;\n" +
			"\n" +
			"	public static void z(int c) {\n" +
			"		if (!a || (b == c && A[c].foo() == C)) {\n" +
			"			return;\n" +
			"		}\n" +
			"		y();\n" +
			"		b = c;\n" +
			"		try {\n" +
			"			A[c].bar();\n" +
			"		} catch (Throwable e) {\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public static void y() {\n" +
			"	}\n" +
			"\n" +
			"	static Y foo(InputStream stream, String s) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}",
			"Y.java",
			"interface Y {\n" +
			"	int foo();\n" +
			"	void bar();\n" +
			"}"
		},
		"");
	}
}
