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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Compliance_CLDC extends AbstractRegressionTest {

public Compliance_CLDC(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.3
 */
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.getFirstSupportedJavaVersion());
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.getFirstSupportedJavaVersion());
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.getFirstSupportedJavaVersion());
	return options;
}
public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(), CompilerOptions.getFirstSupportedJdkLevel());
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
		"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" +
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
		"      Stack map table: number of frames 2\n" +
		"        [pc: 13, same_locals_1_stack_item, stack: {java.io.PrintStream}]\n" +
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
