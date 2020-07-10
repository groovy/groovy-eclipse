/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
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

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExternalizeStringLiteralsTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 16 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public ExternalizeStringLiteralsTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public void test001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"A.java",
			"public class A {\n" +
			"	void foo() {\n" +
			"		System.out.println(\"a\");\n" +
			"	} //$NON-NLS-1$	\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in A.java (at line 3)\n" +
		"	System.out.println(\"a\");\n" +
		"	                   ^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n" +
		"2. ERROR in A.java (at line 4)\n" +
		"	} //$NON-NLS-1$	\n" +
		"	  ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

public void test002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"class X {\n" +
			"	String s = null; //$NON-NLS-1$\n" +
			"	String s2 = \"\"; //$NON-NLS-1$\n" +
			"	String s3 = \"\"; //$NON-NLS-1$//$NON-NLS-2$\n" +
			"	\n" +
			"	void foo() {\n" +
			"		String s4 = null; //$NON-NLS-1$\n" +
			"		String s5 = \"\"; //$NON-NLS-1$\n" +
			"		String s6 = \"\"; //$NON-NLS-2$//$NON-NLS-1$\n" +
			"		System.out.println(\"foo\");//$NON-NLS-1$//$NON-NLS-2$\n" +
			"	} //$NON-NLS-1$\n" +
			"	//$NON-NLS-1$\n" +
			"}//$NON-NLS-3$",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 2)\n" +
		"	String s = null; //$NON-NLS-1$\n" +
		"	                 ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	String s3 = \"\"; //$NON-NLS-1$//$NON-NLS-2$\n" +
		"	                             ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	String s4 = null; //$NON-NLS-1$\n" +
		"	                  ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 9)\n" +
		"	String s6 = \"\"; //$NON-NLS-2$//$NON-NLS-1$\n" +
		"	                ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 10)\n" +
		"	System.out.println(\"foo\");//$NON-NLS-1$//$NON-NLS-2$\n" +
		"	                                       ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 11)\n" +
		"	} //$NON-NLS-1$\n" +
		"	  ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 13)\n" +
		"	}//$NON-NLS-3$\n" +
		"	 ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test003() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"p/Foo.java",
			"package p;\n" +
			"public class Foo { \n" +
			"    public void foo() {\n" +
			"		System.out.println(\"string1\" + \"string2\" //$NON-NLS-1$\n" +
			"		);\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in p\\Foo.java (at line 4)\n" +
		"	System.out.println(\"string1\" + \"string2\" //$NON-NLS-1$\n" +
		"	                               ^^^^^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n" +
		"2. ERROR in p\\Foo.java (at line 6)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
public void test004() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"p/Foo.java",
			"package p;\n" +
			"public class Foo { \n" +
			"    public void foo() {\n" +
			"		//$NON-NLS-1$\n" +
			"	 };\n" +
			"}",
		},
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\r\n" +
			"	public static void main(String[] args) {\r\n" +
			"		String s = \"\"; //$NON-NLS-1$//$NON-NLS-1$\r\n" +
			"    }\r\n" +
			"}",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	String s = \"\"; //$NON-NLS-1$//$NON-NLS-1$\n" +
		"	                            ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\r\n" +
			"	public static void main(String[] args) {\r\n" +
			"		String s = \"\"; //$NON-NLS-1$//$NON-NLS-1$\r\n" +
			"    }\r\n" +
			"",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	String s = \"\"; //$NON-NLS-1$//$NON-NLS-1$\n" +
		"	                            ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	}\n" +
		"	^\n" +
		"Syntax error, insert \"}\" to complete ClassBody\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
public void test007() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\r\n" +
			"	public static void main(String[] args) {\r\n" +
			"		String s = null; //$NON-NLS-1$//$NON-NLS-1$\r\n" +
			"    }\r\n" +
			"}",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	String s = null; //$NON-NLS-1$//$NON-NLS-1$\n" +
		"	                 ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	String s = null; //$NON-NLS-1$//$NON-NLS-1$\n" +
		"	                              ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test008() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\r\n" +
			"	public static void main(String[] args) {\r\n" +
			"		String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\r\n" +
			"    }\r\n" +
			"}",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\n" +
		"	           ^^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\n" +
		"	                   ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\n" +
		"	                                ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"p/Foo.java",
			"package p;\n" +
			"public class Foo { \n" +
			"    public void foo(int i) {\n" +
			"		System.out.println(\"test1\" + i + \"test2\"); //$NON-NLS-2$//$NON-NLS-1$\r\n" +
			"	 };\n" +
			"}",
		},
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\n" +
			"		int i = s;\n" +
			"		System.out.println(s);\n" +
			"    }\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\n" +
		"	           ^^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\n" +
		"	                   ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\n" +
		"	                                ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	int i = s;\n" +
		"	        ^\n" +
		"Type mismatch: cannot convert from String to int\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
public void test011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = null;\n" +
			"		String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\n" +
			"		System.out.println(s + i);\n" +
			"    }\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	int i = null;\n" +
		"	        ^^^^\n" +
		"Type mismatch: cannot convert from null to int\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\n" +
		"	           ^^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\n" +
		"	                   ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	String s = \"test\"; //$NON-NLS-2$//$NON-NLS-3$\n" +
		"	                                ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
public void test012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = null;\n" +
			"		String s = null; //$NON-NLS-2$//$NON-NLS-3$\n" +
			"		System.out.println(s + i);\n" +
			"    }\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	int i = null;\n" +
		"	        ^^^^\n" +
		"Type mismatch: cannot convert from null to int\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	String s = null; //$NON-NLS-2$//$NON-NLS-3$\n" +
		"	                 ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	String s = null; //$NON-NLS-2$//$NON-NLS-3$\n" +
		"	                              ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
public void test013() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = \"test1\";\n" +
			"		System.out.println(s);\n" +
			"    }\n" +
			"}",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	String s = \"test1\";\n" +
		"	           ^^^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=112973
public void test014() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = \"test1\"; //$NON-NLS-?$\n" +
			"		System.out.println(s);\n" +
			"    }\n" +
			"}",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	String s = \"test1\"; //$NON-NLS-?$\n" +
		"	           ^^^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	String s = \"test1\"; //$NON-NLS-?$\n" +
		"	                    ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114077
public void test015() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public void foo() {\n" +
			"		String s1= null; //$NON-NLS-1$\n" +
			"		String s2= \"\";\n" +
			"	}\n" +
			"}",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	String s1= null; //$NON-NLS-1$\n" +
		"	                 ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	String s2= \"\";\n" +
		"	           ^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114077
public void test016() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	private String s1= null; //$NON-NLS-1$\n" +
			"	\n" +
			"	public void foo() {\n" +
			"		String s2= \"\";\n" +
			"	}\n" +
			"}",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 2)\n" +
		"	private String s1= null; //$NON-NLS-1$\n" +
		"	                         ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	String s2= \"\";\n" +
		"	           ^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148352
public void test017() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(String locationInAST) {\n" +
			"		String enclosingType= \"\"; //$NON-NLS-1$\n" +
			"		if (locationInAST != null) {\n" +
			"			enclosingType.toString()\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	enclosingType.toString()\n" +
		"	                       ^\n" +
		"Syntax error, insert \";\" to complete BlockStatements\n" +
		"----------\n",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test018() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	#\n" +
			"	String s1= \"1\"; //$NON-NLS-1$\n" +
			"	public void foo() {\n" +
			"		String s2= \"2\"; //$NON-NLS-1$\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test019() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String s1= \"1\"; //$NON-NLS-1$\n" +
			"	#\n" +
			"	public void foo() {\n" +
			"		String s2= \"2\"; //$NON-NLS-1$\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test020() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String s1= \"1\"; //$NON-NLS-1$\n" +
			"	public void foo() {\n" +
			"		#\n" +
			"		String s2= \"2\"; //$NON-NLS-1$\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test021() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String s1= \"1\"; //$NON-NLS-1$\n" +
			"	public void foo() {\n" +
			"		String s2= \"2\"; //$NON-NLS-1$\n" +
			"		#\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test022() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	#\n" +
			"	String s1= \"1\"; //$NON-NLS-1$\n" +
			"	public void foo() {\n" +
			"		#\n" +
			"		String s2= \"2\"; //$NON-NLS-1$\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	#\n" +
		"	^\n" +
		"Syntax error on token \"Invalid Character\", delete this token\n" +
		"----------\n",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=213692
public void test023() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public String toString() {\n" +
			"                StringBuffer output = new StringBuffer(10);\n" +
			"                output.append(this != null) ? null : \"<no type>\"); //$NON-NLS-1$\n" +
			"                output.append(\" \"); //$NON-NLS-1$\n" +
			"                return output.toString();\n" +
			"        }       \n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	output.append(this != null) ? null : \"<no type>\"); //$NON-NLS-1$\n" +
		"	                          ^\n" +
		"Syntax error on token \")\", delete this token\n" +
		"----------\n",
		null,
		true,
		customOptions,
		false,
		false,
		false,
		false,
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=443456, [1.8][compiler][lambda] $NON-NLS$ in lambda statement used as argument does not work
public void test443456() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.concurrent.Callable;\n" +
			"public class X {\n" +
			"    Callable<String> c;\n" +
			"    void setC(Callable<String> c) {\n" +
			"        this.c = c;\n" +
			"    }\n" +
			"    X() {\n" +
			"        setC(() -> \"ee\"); //$NON-NLS-1$\n" +
			"    }\n" +
			"}\n",
		},
		"",
		null,
		true,
		customOptions);
}
public static Class testClass() {
	return ExternalizeStringLiteralsTest.class;
}
}
