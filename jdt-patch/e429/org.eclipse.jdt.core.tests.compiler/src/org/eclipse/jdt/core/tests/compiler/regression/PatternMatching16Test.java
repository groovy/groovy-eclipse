/*******************************************************************************
 * Copyright (c) 2020, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class PatternMatching16Test extends AbstractRegressionTest {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 16 --enable-preview -Xlint:-preview");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug575035" };
	}

	public static Class<?> testClass() {
		return PatternMatching16Test.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}
	public PatternMatching16Test(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		if (this.complianceLevel >= ClassFileConstants.getLatestJDKLevel()
				&& preview) {
			defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		}
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE16Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, new String[] {"--enable-preview"}, JAVAC_OPTIONS);
	}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String javacLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = javacLog;
		runner.runNegativeTest();
	}
	public void test000a() {
		Map<String, String> options = getCompilerOptions(false);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
		runNegativeTest(
				new String[] {
						"X1.java",
						"public class X1 {\n" +
						"  public void foo(Object obj) {\n" +
						"		if (obj instanceof String s) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X1.java (at line 3)\n" +
				"	if (obj instanceof String s) {\n" +
				"	                   ^^^^^^^^\n" +
				"The Java feature 'Pattern Matching in instanceof Expressions' is only available with source level 16 and above\n" +
				"----------\n",
				null,
				true,
				options);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
	}
	public void test000b() {
		if (this.complianceLevel < ClassFileConstants.getLatestJDKLevel())
			return;
		Map<String, String> options = getCompilerOptions(true);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_14);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_14);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_14);
		runNegativeTest(
				new String[] {
						"X1.java",
						"public class X1 {\n" +
						"  public void foo(Object obj) {\n" +
						"		if (obj instanceof String s) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X1.java (at line 0)\n" +
				"	public class X1 {\n" +
				"	^\n" +
				"Preview features enabled at an invalid source release level 14, preview can be enabled only at source level "+PREVIEW_ALLOWED_LEVEL+"\n" +
				"----------\n",
				null,
				true,
				options);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
	}
	// No longer negative since pattern matching is a standard feature now.
	public void test001() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X1.java",
						"public class X1 {\n" +
						"  public void foo(Object obj) {\n" +
						"		if (obj instanceof String s) {\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"	}\n" +
						"}\n",
				},
				"",
				options);
	}
	public void test002() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X2.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X2 {\n" +
						"  public void foo(Integer obj) {\n" +
						"		if (obj instanceof String s) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X2.java (at line 4)\n" +
				"	if (obj instanceof String s) {\n" +
				"	    ^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types Integer and String\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test003() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X3.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X3 {\n" +
						"  public void foo(Number num) {\n" +
						"		if (num instanceof Integer s) {\n" +
						"		} else if (num instanceof String) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X3.java (at line 5)\n" +
				"	} else if (num instanceof String) {\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types Number and String\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test003a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X3.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X3 {\n" +
						"  public void foo(Number num) {\n" +
						"		if (num instanceof int) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X3.java (at line 4)\n" +
				"	if (num instanceof int) {\n" +
				"	    ^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types Number and int\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test004() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X4.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X4 {\n" +
						"  public void foo(Object obj) {\n" +
						"		String s = null;\n" +
						"		if (obj instanceof Integer s) {\n" +
						"		} else if (obj instanceof String) {\n" +
						"		}\n " +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X4.java (at line 5)\n" +
				"	if (obj instanceof Integer s) {\n" +
				"	                           ^\n" +
				"Duplicate local variable s\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test005() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X5.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X5 {\n" +
						"@SuppressWarnings(\"preview\")\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(s);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"	}\n" +
						"}\n",
				},
				"100",
				options);
	}
	public void test006() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X6.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6 {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(s);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"abcd",
				options);
	}
	public void test006a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6a {\n" +
						"	public static void foo(Object obj) {\n" +
						"		if (obj != null) {\n" +
						"			if (obj instanceof Integer i) {\n" +
						"				System.out.print(i);\n" +
						"			} else if (obj instanceof String s) {\n" +
						"				System.out.print(i);\n" +
						"			}\n " +
						"		}\n " +
						"		System.out.print(i);\n" +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X6a.java (at line 8)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"i cannot be resolved to a variable\n" +
				"----------\n" +
				"2. ERROR in X6a.java (at line 11)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"i cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test006b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6b {\n" +
						"	public static void foo(Object obj) {\n" +
						"		if (obj != null) {\n" +
						"			if (obj instanceof Integer i) {\n" +
						"				System.out.print(i);\n" +
						"			} else if (obj instanceof String s) {\n" +
						"				System.out.print(i);\n" +
						"			}\n " +
						"		}\n " +
						"		System.out.print(s);\n" +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X6b.java (at line 8)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"i cannot be resolved to a variable\n" +
				"----------\n" +
				"2. ERROR in X6b.java (at line 11)\n" +
				"	System.out.print(s);\n" +
				"	                 ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test006c() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6c.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6c {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(i);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X6c.java (at line 7)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"i cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test006d() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X6d.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X6d {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else {\n" +
						"			System.out.print(i);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X6d.java (at line 7)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"i cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test007() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X7.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X7 {\n" +
						"  public static void foo(Object obj) {\n" +
						"		if (obj instanceof Integer i) {\n" +
						"			System.out.print(i);\n" +
						"		} else if (obj instanceof String s) {\n" +
						"			System.out.print(i);\n" +
						"		}\n " +
						"	}\n" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X7.java (at line 7)\n" +
				"	System.out.print(i);\n" +
				"	                 ^\n" +
				"i cannot be resolved to a variable\n" +
				"----------\n",
				"X7.java:4: warning: [preview] pattern matching in instanceof is a preview feature and may be removed in a future release.\n" +
				"		if (obj instanceof Integer i) {\n" +
				"		                           ^\n" +
				"X7.java:6: warning: [preview] pattern matching in instanceof is a preview feature and may be removed in a future release.\n" +
				"		} else if (obj instanceof String s) {\n" +
				"		                                 ^\n" +
				"X7.java:7: error: cannot find symbol\n" +
				"			System.out.print(i);\n" +
				"			                 ^\n" +
				"  symbol:   variable i\n" +
				"  location: class X7\n" +
				"1 error\n" +
				"2 warnings",
				null,
				true,
				options);
	}
	public void test008() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X8.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X8 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = null;\n" +
						"		if (b != c) {\n" +
						"			if ((b instanceof String s) && (s.length() != 0))\n" +
						"				System.out.println(\"s:\" + s);\n" +
						"			else \n" +
						"				System.out.println(\"b:\" + b);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"b:100\n" +
				"s:abcd",
				options);
	}
	public void test009() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X9.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X9 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = null;\n" +
						"		if (b != c) {\n" +
						"			if ((b instanceof String s) && (s.length() != 0))\n" +
						"				System.out.println(\"s:\" + s);\n" +
						"			else if ((b instanceof Integer i2))\n" +
						"				System.out.println(\"i2:\" + i2);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"i2:100\n" +
				"s:abcd",
				options);
	}
	public void test010() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X10.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X10 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = null;\n" +
						"		if (b != c) {\n" +
						"			if (b != null && (b instanceof String s))\n" +
						"				System.out.println(\"s:\" + s);\n" +
						"			else " +
						"				System.out.println(\"b:\" + b);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"b:100\n" +
				"s:abcd",
				options);
	}
	public void test011() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X11.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X11 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = null;\n" +
						"		if (b == null && (b instanceof String s)) {\n" +
						"		} else {" +
						"		}\n" +
						"		System.out.println(s);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X11.java (at line 7)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test012() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X12.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X12 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = new Object();\n" +
						"		if (b != c) {\n" +
						"			if (b == null && (b instanceof String s)) {\n" +
						"				System.out.println(\"s:\" + s);\n" +
						"			} else {\n" +
						"				System.out.println(\"b:\" + b);\n" +
						"			}\n" +
						"			s = null;\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X12.java (at line 11)\n" +
				"	s = null;\n" +
				"	^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test013() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X13.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X13 {\n" +
						"  	public static void foo(Object b) {\n" +
						"		Object c = null;\n" +
						"		if (b != c) {\n" +
						"			if (b == null && (b instanceof String s))\n" +
						"				System.out.println(\"s:\" + s);\n" +
						"			else " +
						"				System.out.println(\"b:\" + b);\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(100);\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X13.java (at line 9)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14 {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!(o instanceof String s)) {\n" +
						"			System.out.print(\"then:\" + s);\n" +
						"		} else {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X14.java (at line 5)\n" +
				"	System.out.print(\"then:\" + s);\n" +
				"	                           ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14a {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!(o instanceof String s)) {\n" +
						"			System.out.print(\"then:\" + s);\n" +
						"		} else {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X14a.java (at line 5)\n" +
				"	System.out.print(\"then:\" + s);\n" +
				"	                           ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14b {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!!(o instanceof String s)) {\n" +
						"			System.out.print(\"then:\" + s);\n" +
						"		} else {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X14b.java (at line 7)\n" +
				"	System.out.print(\"else:\" + s);\n" +
				"	                           ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014c() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X14c.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14c {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (o == null) {\n" +
						"			System.out.print(\"null\");\n" +
						"		} else if(!(o instanceof String s)) {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X14c.java (at line 7)\n" +
				"	System.out.print(\"else:\" + s);\n" +
				"	                           ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test014d() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X14d.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14d {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (o == null) {\n" +
						"			System.out.print(\"null\");\n" +
						"		} else if(!!(o instanceof String s)) {\n" +
						"			System.out.print(\"else:\" + s);\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"else:abcd",
				options);
	}
	public void test014e() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X14a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X14a {\n" +
						"  	public static void foo(Object o) {\n" +
						"		 if (!(!(o instanceof String s))) {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		} else {\n" +
						"		}\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"s:abcd",
				options);
	}
	/*
	 * Test that when pattern tests for false and if doesn't complete
	 * normally, then the variable is available beyond the if statement
	 */
	public void test015() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X15.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X15 {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!(o instanceof String s)) {\n" +
						"			throw new IllegalArgumentException();\n" +
						"		} else {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" +
						"		System.out.print(s);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"s:abcdabcd",
				options);
	}
	/*
	 * Test that when pattern tests for false and if doesn't complete
	 * normally, then the variable is available beyond the if statement
	 */
	public void test015a() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X15a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X15a {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!(o instanceof String s)) {\n" +
						"			throw new IllegalArgumentException();\n" +
						"		}\n" +
						"		System.out.print(s);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"abcd",
				options);
	}
	/*
	 * Test that when pattern tests for false and if completes
	 * normally, then the variable is not available beyond the if statement
	 */
	public void test015b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X15b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X15b {\n" +
						"  	public static void foo(Object o) {\n" +
						"		if (!(o instanceof String s)) {\n" +
						"			//throw new IllegalArgumentException();\n" +
						"		} else {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" +
						"		System.out.print(s);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(\"abcd\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X15b.java (at line 9)\n" +
				"	System.out.print(s);\n" +
				"	                 ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test016() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X16.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X16 {\n" +
						"  	public static void foo(Object o) {\n" +
						"		boolean b = (o instanceof String[] s && s.length == 1);\n" +
						"		System.out.print(b);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(new String[]{\"one\"});\n" +
						"	}\n" +
						"}\n",
				},
				"true",
				options);
	}
	public void test017() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X17.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X17 {\n" +
						"  	public static void foo(Object o) {\n" +
						"		boolean b = (o instanceof String[] s && s.length == 1);\n" +
						"		System.out.print(s[0]);\n" +
						"	}" +
						"  public static void main(String[] obj) {\n" +
						"		foo(new String[]{\"one\"});\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X17.java (at line 5)\n" +
				"	System.out.print(s[0]);\n" +
				"	                 ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/* Test that the scopes of pattern variable in a block doesn't affect
	 * another outside but declared after the block
	 */
	public void test018() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X18.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X18 {\n" +
						"  public static void main(String[] obj) {\n" +
						"  		foo(obj);\n" +
						"  }\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		boolean a = true;\n" +
						"		{\n" +
						"			boolean b = (obj instanceof String[] s && s.length == 0);\n" +
						"			System.out.print(b + \",\");\n" +
						"		}\n" +
						"		boolean b = a ? false : (obj instanceof String[] s && s.length == 0);\n" +
						"		System.out.print(b);\n" +
						"	}\n" +
						"}\n",
				},
				"true,false",
				options);
	}
	/* Test that the scopes of pattern variable in a block doesn't affect
	 * another outside but declared before the block
	 */
	public void test019() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X19.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X19 {\n" +
						"  public static void main(String[] obj) {\n" +
						"  		foo(obj);\n" +
						"  }\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		boolean a = true;\n" +
						"		boolean b = a ? false : (obj instanceof String[] s && s.length == 0);\n" +
						"		System.out.print(b + \",\");\n" +
						"		{\n" +
						"			b = (obj instanceof String[] s && s.length == 0);\n" +
						"			System.out.print(b);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"false,true",
				options);
	}
	/* Test that we still detect duplicate pattern variable declarations
	 */
	public void test019b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X19b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X19b {\n" +
						"  public static void main(String[] obj) {\n" +
						"  		foo(obj);\n" +
						"  }\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		boolean a = true;\n" +
						"		if (obj instanceof String[] s && s.length == 0) {\n" +
						"			boolean b = (obj instanceof String[] s && s.length == 0);\n" +
						"			System.out.print(b);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X19b.java (at line 9)\n" +
				"	boolean b = (obj instanceof String[] s && s.length == 0);\n" +
				"	                                     ^\n" +
				"Duplicate local variable s\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/* Test that we report subtypes of pattern variables used in the same stmt
	 * As of Java 19, we no longer report error for the above
	 */
	public void test020() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X20.java",
						"public class X20 {\n" +
						"  public static void main(String[] obj) {\n" +
						"  		foo(obj);\n" +
						"  }\n" +
						"  public static void foo(Object[] o) {\n" +
						"		boolean b = (o instanceof String[] s) && s instanceof CharSequence[] s2;\n" +
						"		System.out.print(b1);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X20.java (at line 7)\n" +
				"	System.out.print(b1);\n" +
				"	                 ^^\n" +
				"b1 cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/* Test that we allow consequent pattern expressions in the same statement
	 */
	public void test020a() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X20.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X20 {\n" +
						"  public static void main(String[] obj) {\n" +
						"  		foo(obj);\n" +
						"  }\n" +
						"  public static void foo(Object[] o) {\n" +
						"		boolean b = (o instanceof CharSequence[] s) && s instanceof String[] s2;\n" +
						"		System.out.print(b);\n" +
						"	}\n" +
						"}\n",
				},
				"true",
				options);
	}
	/* Test that we allow consequent pattern expressions in the same statement
	 */
	public void test021() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X21.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X21 {\n" +
						"  public static void main(String[] obj) {\n" +
						"  		foo(obj);\n" +
						"  }\n" +
						"  public static void foo(Object[] o) {\n" +
						"		boolean b = (o instanceof CharSequence[] s) && s instanceof String[] s2;\n" +
						"		System.out.print(s);\n" +
						"		System.out.print(s2);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X21.java (at line 8)\n" +
				"	System.out.print(s);\n" +
				"	                 ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n" +
				"2. ERROR in X21.java (at line 9)\n" +
				"	System.out.print(s2);\n" +
				"	                 ^^\n" +
				"s2 cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/* Test that we allow pattern expressions in a while statement
	 */
	public void test022() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X22.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X22 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while ((o instanceof String s) && s.length() > 0) {\n" +
						"			o = s.substring(0, s.length() - 1);\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"one\non\no",
				options);
	}
	public void test022a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X22a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X22a {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		do {\n" +
						"			o = s.substring(0, s.length() - 1);\n" +
						"			System.out.println(s);\n" +
						"		} while ((o instanceof String s) && s.length() > 0);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X22a.java (at line 8)\n" +
				"	o = s.substring(0, s.length() - 1);\n" +
				"	    ^\n" +
				"s cannot be resolved\n" +
				"----------\n" +
				"2. ERROR in X22a.java (at line 8)\n" +
				"	o = s.substring(0, s.length() - 1);\n" +
				"	                   ^\n" +
				"s cannot be resolved\n" +
				"----------\n" +
				"3. ERROR in X22a.java (at line 9)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				null,
				true,
				options);
	}
	public void test022b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X22b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X22b {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		do {\n" +
						"			// nothing\n" +
						"		} while ((o instanceof String s));\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X22b.java (at line 10)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				null,
				true,
				options);
	}
	public void test022c() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X22c.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X22c {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		do {\n" +
						"			// nothing\n" +
						"		} while (!(o instanceof String s));\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				options);
	}
	/* Test pattern expressions in a while statement with break
	 */
	public void test023() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X23 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while (!(o instanceof String s) && s.length() > 0) {\n" +
						"			System.out.println(s);\n" +
						"			break;\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X23.java (at line 7)\n" +
				"	while (!(o instanceof String s) && s.length() > 0) {\n" +
				"	                                   ^\n" +
				"s cannot be resolved\n" +
				"----------\n" +
				"2. ERROR in X23.java (at line 8)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test023a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X23a {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		do {\n" +
						"			System.out.println(s);\n" +
						"			break;\n" +
						"		} while (!(o instanceof String s) && s.length() > 0);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X23a.java (at line 8)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n" +
				"2. ERROR in X23a.java (at line 10)\n" +
				"	} while (!(o instanceof String s) && s.length() > 0);\n" +
				"	                                     ^\n" +
				"s cannot be resolved\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/* Test pattern expressions in a while statement with no break
	 */
	public void test023b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23b.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X23b {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while (!(o instanceof String s) && s.length() > 0) {\n" +
						"			System.out.println(s);\n" +
						"			//break;\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X23b.java (at line 7)\n" +
				"	while (!(o instanceof String s) && s.length() > 0) {\n" +
				"	                                   ^\n" +
				"s cannot be resolved\n" +
				"----------\n" +
				"2. ERROR in X23b.java (at line 8)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	// Same as above but with do while
	public void test023c() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X23c.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X23c {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		do {\n" +
						"			System.out.println(s);\n" +
						"			//break;\n" +
						"		}while (!(o instanceof String s) && s.length() > 0);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X23c.java (at line 8)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n" +
				"2. ERROR in X23c.java (at line 10)\n" +
				"	}while (!(o instanceof String s) && s.length() > 0);\n" +
				"	                                    ^\n" +
				"s cannot be resolved\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test024a() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X24a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X24a {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		while (!(o instanceof String s)) {\n" +
						"			throw new IllegalArgumentException();\n" +
						"		}\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				options);
	}
	public void test024b() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X24a.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X24a {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		for (;!(o instanceof String s);) {\n" +
						"			 throw new IllegalArgumentException();\n" +
						"		}\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				options);
	}
	/*
	 * It's not a problem to define the same var in two operands of a binary expression,
	 * but then it is not in scope below.
	 */
	public void test025() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X25.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X25 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\", \"two\");\n" +
						"	}\n" +
						"  public static void foo(Object o, Object p) {\n" +
						"		if ((o instanceof String s) != p instanceof String s) {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X25.java (at line 8)\n" +
				"	System.out.print(\"s:\" + s);\n" +
				"	                        ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test025a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X25.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X25 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		if ( (o instanceof String a) || (! (o instanceof String a)) ) {\n" +
						"			System.out.print(\"a:\" + a);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X25.java (at line 8)\n" +
				"	System.out.print(\"a:\" + a);\n" +
				"	                        ^\n" +
				"a cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test025b() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X25.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X25 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object o) {\n" +
						"		if ( (o instanceof String a) || (! (o instanceof String a)) ) {\n" +
						"			System.out.println(\"none\");\n" +
						"		} else {\n" +
						"			System.out.print(\"a:\" + a);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"none",
				options);
	}
	public void test025c() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X25.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X25 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\", new Integer(0));\n" +
						"	}\n" +
						"  public static void foo(Object o, Object p) {\n" +
						"		if ( (o instanceof String a) || (! (p instanceof String a)) ) {\n" +
						"			System.out.println(\"none\");\n" +
						"		} else {\n" +
						"			System.out.print(\"a:\" + a);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"none",
				options);
	}
	/*
	 * It's not allowed to have two pattern variables with same name in the
	 * same scope
	 */
	public void test026() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X26.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X26 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\", \"two\");\n" +
						"	}\n" +
						"  public static void foo(Object o, Object p) {\n" +
						"		if ((o instanceof String s) && (p instanceof String s)) {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X26.java (at line 7)\n" +
				"	if ((o instanceof String s) && (p instanceof String s)) {\n" +
				"	                                                    ^\n" +
				"Duplicate local variable s\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/*
	 * It's not allowed to have two pattern variables with same name in the
	 * same scope
	 */
	public void test026a() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X26.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X26 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\", \"two\");\n" +
						"	}\n" +
						"  public static void foo(Object o, Object p) {\n" +
						"		if ((o instanceof String s) && (!(o instanceof String s))) {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X26.java (at line 7)\n" +
				"	if ((o instanceof String s) && (!(o instanceof String s))) {\n" +
				"	                                                      ^\n" +
				"Duplicate local variable s\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	/*
	 * It's not a problem to define the same var in two operands of a binary expression,
	 * but then it is not in scope below.
	 */
	public void test026b() {
		Map<String, String> options = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X26.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X26 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\", \"two\");\n" +
						"	}\n" +
						"  public static void foo(Object o, Object p) {\n" +
						"		if ((o instanceof String s) == p instanceof String s) {\n" +
						"			System.out.print(\"s:\" + s);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X26.java (at line 8)\n" +
				"	System.out.print(\"s:\" + s);\n" +
				"	                        ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				options);
	}
	public void test027() {
		runConformTest(
				new String[] {
						"X27.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X27 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; (obj instanceof String[] s && s.length > 0 && i < s.length); i++) {\n" +
						"			System.out.println(s[i]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"",
				getCompilerOptions(true));
	}
	public void test028() {
		runConformTest(
				new String[] {
						"X28.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X28 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"one\", \"two\"});\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; (obj instanceof String[] s && s.length > 0 && i < s.length); i++) {\n" +
						"			System.out.println(s[i]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"one\ntwo",
				getCompilerOptions(true));
	}
	public void test029() {
		runConformTest(
				new String[] {
						"X29.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X29 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"one\", \"two\"});\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; (obj instanceof String[] s) && s.length > 0 && i < s.length; i = (s != null ? i + 1 : i)) {\n" +
						"			System.out.println(s[i]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"one\ntwo",
				getCompilerOptions(true));
	}
	/*
	 * Test that pattern variables are accepted in initialization of a for statement,
	 * but unavailable in the body if uncertain which if instanceof check was true
	 */
	public void test030() {
		runNegativeTest(
				new String[] {
						"X30.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X30 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0, length = (obj instanceof String s) ? s.length() : 0; i < length; i++) {\n" +
						"			System.out.print(s.charAt(i));\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X30.java (at line 8)\n" +
				"	System.out.print(s.charAt(i));\n" +
				"	                 ^\n" +
				"s cannot be resolved\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test031() {
		runNegativeTest(
				new String[] {
						"X31.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X31 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {\n" +
						"			System.out.println(s[i]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X31.java (at line 7)\n" +
				"	for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {\n" +
				"	                                               ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n" +
				"2. ERROR in X31.java (at line 7)\n" +
				"	for(int i = 0; !(obj instanceof String[] s) && s.length > 0 && i < s.length; i++) {\n" +
				"	                                                                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n" +
				"3. ERROR in X31.java (at line 8)\n" +
				"	System.out.println(s[i]);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test032() {
		runConformTest(
				new String[] {
						"X32.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X32 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		String res = null;\n" +
						"		int i = 0;\n" +
						"		switch(i) {\n" +
						"		case 0:\n" +
						"			res = (obj instanceof String) ? null : null;\n" +
						"		default:\n" +
						"			break;\n" +
						"		}\n" +
						"		System.out.println(res);\n" +
						"	}\n" +
						"}\n",
				},
				"null",
				getCompilerOptions(true));
	}
	public void test032a() {
		runConformTest(
				new String[] {
						"X32.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X32 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		String res = null;\n" +
						"		int i = 0;\n" +
						"		switch(i) {\n" +
						"		case 0:\n" +
						"			res = (obj instanceof String s) ? s : null;\n" +
						"		default:\n" +
						"			break;\n" +
						"		}\n" +
						"		System.out.println(res);\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				getCompilerOptions(true));
	}
	public void test033() {
		runNegativeTest(
				new String[] {
						"X33.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X33 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		String res = null;\n" +
						"		int i = 0;\n" +
						"		switch(i) {\n" +
						"		case 0:\n" +
						"			res = (obj instanceof String s) ? s : null;\n" +
						"			res = s.substring(1);\n" +
						"		default:\n" +
						"			break;\n" +
						"		}\n" +
						"		System.out.println(res);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X33.java (at line 12)\n" +
				"	res = s.substring(1);\n" +
				"	      ^\n" +
				"s cannot be resolved\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test034() {
		runNegativeTest(
				new String[] {
						"X34.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X34 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		int i = 0;\n" +
						"		String result = switch(i) {\n" +
						"			case 0 -> {\n" +
						"				result = (obj instanceof String s) ? s : null;\n" +
						"				yield result;\n" +
						"			}\n" +
						"			default -> {\n" +
						"				yield result;\n" +
						"			}\n" +
						"		};\n" +
						"		System.out.println(result);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X34.java (at line 14)\n" +
				"	yield result;\n" +
				"	      ^^^^^^\n" +
				"The local variable result may not have been initialized\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test035() {
		runNegativeTest(
				new String[] {
						"X35.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X35 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		int i = 0;\n" +
						"		String result = switch(i) {\n" +
						"			case 0 -> {\n" +
						"				result = (obj instanceof String s) ? s : null;\n" +
						"				yield s;\n" +
						"			}\n" +
						"			default -> {\n" +
						"				yield s;\n" +
						"			}\n" +
						"		};\n" +
						"		System.out.println(result);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X35.java (at line 11)\n" +
				"	yield s;\n" +
				"	      ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n" +
				"2. ERROR in X35.java (at line 14)\n" +
				"	yield s;\n" +
				"	      ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test036() {
		runConformTest(
				new String[] {
						"X36.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X36 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(\"one\");\n" +
						"	}\n" +
						"  public static void foo(Object obj) {\n" +
						"		int i = 0;\n" +
						"		String result = switch(i) {\n" +
						"			default -> {\n" +
						"				result = (obj instanceof String s) ? s : null;\n" +
						"				yield result;\n" +
						"			}\n" +
						"		};\n" +
						"		System.out.println(result);\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				getCompilerOptions(true));
	}
	public void test037() {
		runNegativeTest(
				new String[] {
						"X37.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X37 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"abcd\"});\n" +
						"	}\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		for(int i = 0; (obj[i] instanceof String s) && s.length() > 0 ; i++) {\n" +
						"			System.out.println(s[i]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X37.java (at line 8)\n" +
				"	System.out.println(s[i]);\n" +
				"	                   ^^^^\n" +
				"The type of the expression must be an array type but it resolved to String\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test038() {
		runNegativeTest(
				new String[] {
						"X38.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X38 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"abcd\"});\n" +
						"	}\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		for(int i = 0; (obj[i] instanceof String s) && s.length() > 0 ;) {\n" +
						"			throw new IllegalArgumentException();\n" +
						"		}\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X38.java (at line 10)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				getCompilerOptions(true));
	}
	public void test039() {
		runConformTest(
				new String[] {
						"X39.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X39 {\n" +
						"  public static void main(String[] o) {\n" +
						"		foo(new String[] {\"one\"});;\n" +
						"	}\n" +
						"  public static void foo(Object[] obj) {\n" +
						"		for(int i = 0; i < obj.length && (obj[i] instanceof String s) && i < s.length(); i++) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"one",
				getCompilerOptions(true));
	}
	public void test040() {
		runConformTest(
				new String[] {
						"X40.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X40 {\n" +
						"	String a;\n" +
						"    Object o1 = \"x\";\n" +
						"    public static void main(String argv[]) {\n" +
						"        System.out.println(new X40().foo());\n" +
						"    }\n" +
						"    public String foo() {\n" +
						"        String res = \"\";\n" +
						"    	 Object o2 = \"x\";\n" +
						"        if (o1 instanceof String s) { \n" +
						"            res = \"then_\" + s;\n" +
						"        } else {\n" +
						"            res = \"else_\";\n" +
						"        }\n" +
						"        return res;\n" +
						"    }\n" +
						"}\n",
				},
				"then_x",
				getCompilerOptions(true));
	}
	public void test041() {
		runConformTest(
				new String[] {
						"X41.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X41 {\n" +
						"	String a;\n" +
						"    Object o1 = \"x\";\n" +
						"    public static void main(String argv[]) {\n" +
						"        System.out.println(new X41().foo());\n" +
						"    }\n" +
						"    public String foo() {\n" +
						"        String res = \"\";\n" +
						"        Object o2 = \"x\";\n" +
						"        if ( !(o1 instanceof String s) || !o1.equals(s) ) { \n" +
						"            res = \"then_\";\n" +
						"        } else {\n" +
						"            res = \"else_\" + s;\n" +
						"        }\n" +
						"        return res;\n" +
						"    }\n" +
						"}\n",
				},
				"else_x",
				getCompilerOptions(true));
	}
	public void test042() {
		runConformTest(
				new String[] {
						"X42.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X42 {\n" +
						"	 String a;\n" +
						"    Object o1 = \"x\";\n" +
						"    public static void main(String argv[]) {\n" +
						"        System.out.println(new X42().foo());\n" +
						"    }\n" +
						"    public String foo() {\n" +
						"        String res = \"\";\n" +
						"        Object o2 = o1;\n" +
						"        if ( !(o1 instanceof String s) || !o1.equals(s) ) { \n" +
						"            res = \"then_\";\n" +
						"        } else {\n" +
						"            res = \"else_\" + s;\n" +
						"        }\n" +
						"        return res;\n" +
						"    }\n" +
						"}\n",
				},
				"else_x",
				getCompilerOptions(true));
	}
	public void test043() {
		runConformTest(
				new String[] {
						"X43.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X43 {\n" +
						"	 public static void main(String argv[]) {\n" +
						"		System.out.println(new X43().foo(\"foo\", \"test\"));\n" +
						"	}\n" +
						"	public boolean foo(Object obj, String s) {\n" +
						"		class Inner {\n" +
						"			public boolean foo(Object obj) {\n" +
						"				if (obj instanceof String s) {\n" +
						"					// s is shadowed now\n" +
						"					if (!\"foo\".equals(s))\n" +
						"						return false;\n" +
						"				}\n" +
						"				// s is not shadowed\n" +
						"				return \"test\".equals(s);\n" +
						"			}\n" +
						"		}\n" +
						"		return new Inner().foo(obj);\n" +
						"	}\n" +
						"}\n",
				},
				"true",
				getCompilerOptions(true));
	}
	public void test044() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X44.java",
						"@SuppressWarnings(\"preview\")\n" +
						"class Inner<T> {\n" +
						"    public boolean foo(Object obj) {\n" +
						"        if (obj instanceof Inner<?> p) {\n" +
						"            return true;\n" +
						"        }\n" +
						"        return false;\n" +
						"    }\n" +
						"} \n" +
						"public class X44  {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	Inner<String> param = new Inner<>();\n" +
						"    	System.out.println(new Inner<String>().foo(param));\n" +
						"    }\n" +
						"}\n",
				},
				"true",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test045() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X45.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X45 {\n" +
						"    Object s = \"test\";\n" +
						"    boolean result = s instanceof String s1;\n" +
						"	 public static void main(String argv[]) {\n" +
						"    	System.out.println(\"true\");\n" +
						"    }\n" +
						"}\n",
				},
				"true",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test046() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X46.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X46 {\n" +
						"    Object s = \"test\";\n" +
						"    boolean result = (s instanceof String s1 && s1 != null);\n" +
						"	 public static void main(String argv[]) {\n" +
						"    	System.out.println(\"true\");\n" +
						"    }\n" +
						"}\n",
				},
				"true",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test047() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"public class InstanceOfPatternTest {\n" +
						"	public static void main(String[] args) {\n" +
						"		if (getChars() instanceof String s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"	static CharSequence getChars() {\n" +
						"		return \"xyz\";\n" +
						"	}\n" +
						"}\n",
				},
				"xyz",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test048() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"public class InstanceOfPatternTest {\n" +
						"	public static void main(String[] args) {\n" +
						"		if (getChars() instanceof String s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"	static CharSequence getChars() {\n" +
						"		return \"xyz\";\n" +
						"	}\n" +
						"}\n",
				},
				"xyz",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test049() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"public class InstanceOfPatternTest {\n" +
						"	public static void main(String[] args) {\n" +
						"		if ( ((CharSequence) getChars()) instanceof String s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"	static Object getChars() {\n" +
						"		return \"xyz\";\n" +
						"	}\n" +
						"}\n",
				},
				"xyz",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test050() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runNegativeTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class InstanceOfPatternTest {\n" +
						"	public static void main(String[] args) {\n" +
						"		if ( ((s) -> {return s;}) instanceof I s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"} \n" +
						"interface I {\n" +
						"	public String foo(String s);\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in InstanceOfPatternTest.java (at line 4)\n" +
				"	if ( ((s) -> {return s;}) instanceof I s) {\n" +
				"	     ^^^^^^^\n" +
				"The target type of this expression must be a functional interface\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test051() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"InstanceOfPatternTest.java",
						"public class InstanceOfPatternTest {\n" +
						"	static String STR = \"2\";\n" +
						"	public static void main(String[] args) {\n" +
						"		if ( switch(STR) {\n" +
						"				case \"1\" -> (CharSequence) \"one\";\n" +
						"				default -> (CharSequence) \"Unknown\";\n" +
						"			  } \n" +
						"				instanceof String s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"Unknown",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void test052() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		String old = compilerOptions.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	@SuppressWarnings(\"preview\")\n" +
						"	public static void main(String args[]) {\n" +
						"		String result = null;\n" +
						"		Object obj = \"abc\";\n" +
						"		int i = switch (0) {\n" +
						"			case 1 -> {\n" +
						"				yield 1;\n" +
						"			}\n" +
						"			default -> {\n" +
						"				for (int j = 0; !(obj instanceof String s);) {\n" +
						"					obj = null;\n" +
						"				}\n" +
						"				result = s;\n" +
						"				System.out.println(result);\n" +
						"				yield 2;\n" +
						"			}\n" +
						"		};\n" +
						"		System.out.println(i);\n" +
						"	}\n" +
						"}\n",
				},
				"abc\n" +
				"2",
				compilerOptions);
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, old);
	}
	public void testBug562392a() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X<T> {\n" +
						"	public boolean foo(T obj) {\n" +
						"		if (obj instanceof String s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"		return true;\n" +
						"	}\n" +
						"	public static void main(String argv[]) {\n" +
						"		String s = \"x\";\n" +
						"		System.out.println(new X<Object>().foo(s));\n" +
						"	}\n" +
						"}\n",
				},
				"x\n" +
				"true",
				compilerOptions);
		}
	public void testBug562392b() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X<T> {\n" +
						"	public boolean foo(Object obj) {\n" +
						"        if (obj instanceof T) {\n" +
						"            return false;\n" +
						"        }\n" +
						"        return true;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"		System.out.println(\"\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	if (obj instanceof T) {\n" +
				"	    ^^^\n" +
				"Type Object cannot be safely cast to T\n" +
				"----------\n",
				"X.java:4: error: Object cannot be safely cast to T\n" +
				"        if (obj instanceof T) {\n" +
				"            ^\n" +
				"  where T is a type-variable:\n" +
				"    T extends Object declared in class X",
				null,
				true,
				compilerOptions);
		}
	public void testBug562392c() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X<T> {\n" +
						"	public boolean foo(Object obj) {\n" +
						"        if (obj instanceof T t) {\n" +
						"            return false;\n" +
						"        }\n" +
						"        return true;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"		System.out.println(\"\");\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	if (obj instanceof T t) {\n" +
				"	    ^^^\n" +
				"Type Object cannot be safely cast to T\n" +
				"----------\n",
				"X.java:4: error: Object cannot be safely cast to T\n" +
				"        if (obj instanceof T t) {\n" +
				"            ^\n" +
				"  where T is a type-variable:\n" +
				"    T extends Object declared in class X",
				null,
				true,
				compilerOptions);
		}
	public void testBug562392d() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X<T> {\n" +
						"	public boolean foo(Object obj) {\n" +
						"        if (null instanceof T t) {\n" +
						"            return false;\n" +
						"        }\n" +
						"        return true;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"		System.out.println(abc);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	System.out.println(abc);\n" +
				"	                   ^^^\n" +
				"abc cannot be resolved to a variable\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	public void testBug562392e() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X<T> {\n" +
						"	public boolean foo(X<?> obj) {\n" +
						"        if (obj instanceof X<String> p) {\n" +
						"            return true;\n" +
						"        }\n" +
						"        return false;\n" +
						"    }\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	if (obj instanceof X<String> p) {\n" +
				"	    ^^^\n" +
				"Type X<capture#1-of ?> cannot be safely cast to X<String>\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	public void testBug562392f() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"class Outer<T> {\n" +
						"    static class Inner<T> {\n" +
						"    }\n" +
						"}\n" +
						"@SuppressWarnings({\"preview\", \"rawtypes\"})\n" +
						"class X<T> {\n" +
						"    public boolean foo(Outer.Inner obj) {\n" +
						"        if (obj instanceof Outer<?> p) {\n" +
						"            return true;\n" +
						"        }\n" +
						"        return false;\n" +
						"    }\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	if (obj instanceof Outer<?> p) {\n" +
				"	    ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types Outer.Inner and Outer<?>\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	public void testBug562392g() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"class Outer<T> {\n" +
						"    static class Inner<T> {\n" +
						"    }\n" +
						"}\n" +
						"@SuppressWarnings({\"preview\", \"rawtypes\"})\n" +
						"class X<T> {\n" +
						"    public boolean foo(Object obj) {\n" +
						"        if (obj instanceof Outer.Inner<?> p) {\n" +
						"            return true;\n" +
						"        }\n" +
						"        return false;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"		Outer.Inner inn = new Outer.Inner();\n" +
						"    	System.out.println(new X<String>().foo(inn));\n" +
						"	}\n" +
						"}\n",
				},
				"true",
				compilerOptions);
	}
	public void testBug562392h() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings({\"rawtypes\"})\n" +
						"class Y extends X {}\n" +
						"@SuppressWarnings({\"rawtypes\"})\n" +
						"public class X<T> {\n" +
						"	public boolean foo(X[] obj) {\n" +
						"        if (obj instanceof Y[] p) {\n" +
						"            return true;\n" +
						"        }\n" +
						"        return false;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"		Object[] param = {new X()};\n" +
						"       System.out.println(new X<String>().foo(param));\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 13)\n" +
				"	System.out.println(new X<String>().foo(param));\n" +
				"	                                   ^^^\n" +
				"The method foo(X[]) in the type X<String> is not applicable for the arguments (Object[])\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	public void testBug562392i() {
		Map<String, String> options = getCompilerOptions(false);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
		runNegativeTest(
				new String[] {
						"Test.java",
						"import java.util.ArrayList;\n" +
								"import java.util.List;\n" +
								"import java.util.function.Function;\n" +
								"import java.util.function.UnaryOperator;\n" +
								"@SuppressWarnings({\"preview\"})\n" +
								"public class Test<T> {\n" +
								"    public boolean foo(Function<ArrayList<T>, ArrayList<T>> obj) {\n" +
								"        if (obj instanceof UnaryOperator<? extends List<T>>) {\n" +
								"            return false;\n" +
								"        }\n" +
								"        return true;\n" +
								"    }\n" +
								"}\n",
				},
				"----------\n" +
					"1. ERROR in Test.java (at line 8)\n" +
					"	if (obj instanceof UnaryOperator<? extends List<T>>) {\n" +
					"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Cannot perform instanceof check against parameterized type UnaryOperator<? extends List<T>>. Use the form UnaryOperator<?> instead since further generic type information will be erased at runtime\n" +
					"----------\n",
					"",
					null,
					true,
					options);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
	}
	public void testBug562392j() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"Test.java",
						"import java.util.ArrayList;\n" +
						"import java.util.List;\n" +
						"import java.util.function.Function;\n" +
						"import java.util.function.UnaryOperator;\n" +
						"@SuppressWarnings({\"preview\", \"rawtypes\"})\n" +
						"public class Test<T> {\n" +
						"    public boolean foo(Function<ArrayList<T>, ArrayList<T>> obj) {\n" +
						"        if (obj instanceof UnaryOperator<? extends List<T>>) {\n" +
						"            return false;\n" +
						"        }\n" +
						"        return true;\n" +
						"    }\n" +
						"	public static void main(String argv[]) {\n" +
						"       System.out.println(\"\");\n" +
						"	}\n" +
						"}\n",
				},
				"",
				compilerOptions);
	}
	public void test053() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	@SuppressWarnings(\"preview\")\n" +
						"	public static void main(String argv[]) {\n" +
						"		Object obj = \"x\";\n" +
						"		if (obj instanceof String s) {\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"		String s = \"y\";\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"x\n" +
				"y",
				compilerOptions);
	}
	public void test054() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	@SuppressWarnings(\"preview\")\n" +
						"	public static void main(String argv[]) {\n" +
						"		Object obj = \"x\";\n" +
						"		while (!(obj instanceof String s)) {\n" +
						"			String s = \"y\";\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"x",
				compilerOptions);
	}
	public void test055() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"public static void main(String argv[]) {\n" +
						"		String result = \"\";\n" +
						"		Object obj = \"abc\";\n" +
						"		for (; !(obj instanceof String a);) {\n" +
						"			String a = \"\";\n" +
						"			result = a;\n" +
						"			obj = null;\n" +
						"		}\n" +
						"		if (!result.equals(\"abc\")) {\n" +
						"			System.out.println(\"PASS\");\n" +
						"		} else {\n" +
						"			System.out.println(\"FAIL\");\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in for loop
	public void test056() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X {\n" +
						"	public static int impl(I a) {\n" +
						"		return a.foo(\"Default\");\n" +
						"	}\n" +
						"	public static void main(String argv[]) {\n" +
						"		String result = \"\";\n" +
						"		Object obj = \"a\";\n" +
						"		for (int i = 0; !(obj instanceof String a); i = impl(a -> a.length())) {\n" +
						"			obj = null;\n" +
						"		}\n" +
						"		if (!result.equals(\"\"))\n" +
						"			System.out.println(\"FAIL\");\n" +
						"		else\n" +
						"			System.out.println(\"PASS\");\n" +
						"	}\n" +
						"}\n" +
						"interface I {\n" +
						"	int foo(String s);\n" +
						"}\n",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in for loop (block)
	public void test056a() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X {\n" +
						"	public static int impl(I a) {\n" +
						"		return a.foo(\"Default\");\n" +
						"	}\n" +
						"	public static void main(String argv[]) {\n" +
						"		String result = \"\";\n" +
						"		Object obj = \"a\";\n" +
						"		for (int i = 0; !(obj instanceof String a); i = impl(x -> {\n" +
						"															String a = \"\";\n" +
						"															return a.length();\n" +
						"														})) {\n" +
						"			obj = null;\n" +
						"		}\n" +
						"		if (!result.equals(\"\"))\n" +
						"			System.out.println(\"FAIL\");\n" +
						"		else\n" +
						"			System.out.println(\"PASS\");\n" +
						"	}\n" +
						"}\n" +
						"interface I {\n" +
						"	int foo(String s);\n" +
						"}\n",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in if
	public void test056b() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X {\n" +
						"	public static int impl(I a) {\n" +
						"		return a.foo(\"Default\");\n" +
						"	}\n" +
						"	public static void main(String argv[]) {\n" +
						"		String result = \"\";\n" +
						"		Object obj = \"a\";\n" +
						"		if (!(obj instanceof String a)) {\n" +
						"			  int i = impl(a -> a.length());\n" +
						"		}\n" +
						"		if (!result.equals(\"\"))\n" +
						"			System.out.println(\"FAIL\");\n" +
						"		else\n" +
						"			System.out.println(\"PASS\");\n" +
						"	}\n" +
						"}\n" +
						"interface I {\n" +
						"	int foo(String s);\n" +
						"}\n",
				},
				"PASS",
				compilerOptions);
	}
	// Positive - Test conflicting pattern variable and lambda argument in if
	public void test056d() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n" +
						"public class X {\n" +
						"	public static int impl(I a) {\n" +
						"		return a.foo(\"Default\");\n" +
						"	}\n" +
						"	public static void main(String argv[]) {\n" +
						"		String result = \"\";\n" +
						"		Object obj = \"a\";\n" +
						"		for (int i = 0; (obj instanceof String a); i = impl(a -> a.length())) {\n" +
						"			obj = null;\n" +
						"		}\n" +
						"		if (!result.equals(\"\"))\n" +
						"			System.out.println(\"FAIL\");\n" +
						"		else\n" +
						"			System.out.println(\"PASS\");\n" +
						"	}\n" +
						"}\n" +
						"interface I {\n" +
						"	int foo(String s);\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	for (int i = 0; (obj instanceof String a); i = impl(a -> a.length())) {\n" +
				"	                                                    ^\n" +
				"Lambda expression\'s parameter a cannot redeclare another local variable defined in an enclosing scope. \n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	/*
	 * Test we report only one duplicate variable, i.e., in THEN stmt
	 * where pattern variable is in scope.
	 */
	public void test057() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	@SuppressWarnings(\"preview\")\n" +
						"	public static void main(String argv[]) {\n" +
						"		Object obj = \"x\";\n" +
						"		if (obj instanceof String s) {\n" +
						"			String s = \"\";\n" +
						"			System.out.println(s);\n" +
						"		}\n" +
						"		String s = \"y\";\n" +
						"		System.out.println(s);\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	String s = \"\";\n" +
				"	       ^\n" +
				"Duplicate local variable s\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
		}
	public void test058() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	@SuppressWarnings(\"preview\")\n" +
						"	public static void main(String[] s) {\n" +
						"		Object obj = \"x\";\n" +
						"		if (obj instanceof String[] s && s.length > 0) {\n" +
						"			System.out.println(s[0]);\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	if (obj instanceof String[] s && s.length > 0) {\n" +
				"	                            ^\n" +
				"Duplicate local variable s\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	public void test059() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						" static int count;\n"+
						" public static void main(String[] args) {\n"+
						"   int i = 10;\n"+
						"   if (foo() instanceof String s) {\n"+
						"     ++i;\n"+
						"   }\n"+
						"   System.out.println(\"count:\"+X.count+\" i:\"+i);\n"+
						" }\n"+
						" public static Object foo() {\n"+
						"   ++X.count;\n"+
						"   return new Object();\n"+
						" }  \n"+
						"}",
				},
				"count:1 i:10",
				compilerOptions);
	}
	public void test060() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						" static int count;\n"+
						" public static void main(String[] args) {\n"+
						"   int i = 10;\n"+
						"   if (foo() instanceof String s) {\n"+
						"     ++i;\n"+
						"   }\n"+
						"   System.out.println(\"count:\"+X.count+\" i:\"+i);\n"+
						" }\n"+
						" public static Object foo() {\n"+
						"   ++X.count;\n"+
						"   return new String(\"hello\");\n"+
						" }  \n"+
						"}",
				},
				"count:1 i:11",
				compilerOptions);
	}
	public void test061() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						" static int count;\n"+
						" static String STR = \"FAIL\";\n"+
						" @SuppressWarnings(\"preview\")\n"+
						" public static void main(String[] args) {\n"+
						"   if ( switch(STR) {\n"+
						"       default -> (CharSequence)\"PASS\";\n"+
						"       } instanceof String s) {\n"+
						"     System.out.println(s);\n"+
						"   }\n"+
						" }\n"+
						"}",
				},
				"PASS",
				compilerOptions);
	}
	public void test062() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						" @SuppressWarnings(\"preview\")\n"+
						" public void foo(Object o) {\n"+
						"   int len  = (o instanceof String p) ? test(p -> p.length()) : test(p -> p.length());\n"+
						" }\n"+
						"  public int test(FI fi) {\n" +
						"	  return fi.length(\"\");\n" +
						"  } \n" +
						"  interface FI {\n" +
						"	  public int length(String str);\n" +
						"  }" +
						"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	int len  = (o instanceof String p) ? test(p -> p.length()) : test(p -> p.length());\n" +
				"	                                          ^\n" +
				"Lambda expression\'s parameter p cannot redeclare another local variable defined in an enclosing scope. \n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	// Same as above, but pattern variable in scope in false of conditional expression
	public void test063() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						" @SuppressWarnings(\"preview\")\n"+
						" public void foo(Object o) {\n"+
						"   int len  = !(o instanceof String p) ? test(p -> p.length()) : test(p -> p.length());\n"+
						" }\n"+
						"  public int test(FI fi) {\n" +
						"	  return fi.length(\"\");\n" +
						"  } \n" +
						"  interface FI {\n" +
						"	  public int length(String str);\n" +
						"  }" +
						"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	int len  = !(o instanceof String p) ? test(p -> p.length()) : test(p -> p.length());\n" +
				"	                                                                   ^\n" +
				"Lambda expression\'s parameter p cannot redeclare another local variable defined in an enclosing scope. \n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	// Test that pattern variables are seen by body of lamda expressions
	public void test063a() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						" @SuppressWarnings(\"preview\")\n"+
						" public void foo(Object o) {\n"+
						"   int len  = (o instanceof String p) ? test(p1 -> p.length()) : test(p2 -> p.length());\n"+
						" }\n"+
						"  public int test(FI fi) {\n" +
						"	  return fi.length(\"\");\n" +
						"  } \n" +
						"  interface FI {\n" +
						"	  public int length(String str);\n" +
						"  }" +
						"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	int len  = (o instanceof String p) ? test(p1 -> p.length()) : test(p2 -> p.length());\n" +
				"	                                                                         ^\n" +
				"p cannot be resolved\n" +
				"----------\n",
				"",
				null,
				true,
				compilerOptions);
	}
	// Test that pattern variables are seen by body of anonymous class creation
	public void test063b() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						" @SuppressWarnings(\"preview\")\n"+
						" public void foo(Object o) {\n"
						+ "		int len = (o instanceof String p) ? test(new X.FI() {\n"
						+ "			@Override\n"
						+ "			public int length(String p1) {\n"
						+ "				return p.length();\n"
						+ "			}\n"
						+ "		}) : test(new X.FI() {\n"
						+ "			@Override\n"
						+ "			public int length(String p2) {\n"
						+ "				return p.length();\n"
						+ "			}\n"
						+ "		});\n"
						+ "	}\n"
						+ "	public int test(FI fi) {\n"
						+ "		return fi.length(\"\");\n"
						+ "	}\n"
						+ "	interface FI {\n"
						+ "		public int length(String str);\n"
						+ "	}" +
						"}",
					},
					"----------\n" +
					"1. ERROR in X.java (at line 12)\n" +
					"	return p.length();\n" +
					"	       ^\n" +
					"p cannot be resolved\n" +
					"----------\n",
					"",
					null,
					true,
					compilerOptions);
	}
	// Test that pattern variables are shadowed by parameters in an anonymous class
	// creation
	public void test063c() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"	public static void main(String argv[]) {\n" +
						"		System.out.println(new X().foo(\"test\"));\n" +
						"	}\n" +
						" @SuppressWarnings(\"preview\")\n"+
						" public int foo(Object o) {\n"
						+ "		int len = (o instanceof String p) ? test(new X.FI() {\n"
						+ "			String s = p; // allowed\n"
						+ "			@Override\n"
						+ "			public int length(String p) {\n"
						+ "				return p.length();\n"
						+ "			}\n"
						+ "		}) : test(new X.FI() {\n"
						+ "			@Override\n"
						+ "			public int length(String p) {\n"
						+ "				return p.length();\n"
						+ "			}\n"
						+ "		});\n"
						+ "		return len;\n"
						+ "	}\n"
						+ "	public int test(FI fi) {\n"
						+ "		return fi.length(\"fi\");\n"
						+ "	}\n"
						+ "	interface FI {\n"
						+ "		public int length(String str);\n"
						+ "	}" +
						"}",
					},
					"2",
					compilerOptions);
	}
	public void test064() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
						"public class X {\n"+
						" 	public static void main(String argv[]) {\n" +
						"		System.out.println(new X().foo(\"foo\", \"test\"));\n" +
						"	}\n" +
						"	public boolean foo(Object obj, String s) {\n" +
						"		class Inner {\n" +
						"			public boolean foo(Object obj) {\n" +
						"				if (obj instanceof String s) {\n" +
						"					// s is shadowed now\n" +
						"					if (\"foo\".equals(s))\n" +
						"						return false;\n" +
						"				} else if (obj instanceof String s) { \n" +
						"				}\n"+
						"				// s is not shadowed\n" +
						"				return \"test\".equals(s);\n" +
						"			}\n" +
						"		}\n" +
						"		return new Inner().foo(obj);\n" +
						"	}" +
						"}",
				},
				"false",
				compilerOptions);
	}
	public void test065() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
						"public class X {\n"+
						" 	public static void main(String argv[]) {\n"
						+ "		new X().foo(\"foo\");\n"
						+ "	}\n"
						+ "	public void foo(Object o) {\n"
						+ "		if ((o instanceof String s)) {\n"
						+ "			System.out.println(\"if:\" + s);\n"
						+ "		} else {\n"
						+ "			throw new IllegalArgumentException();\n"
						+ "		}\n"
						+ "		System.out.println(\"after:\" + s);\n"
						+ "	}" +
						"}",
				},
				"if:foo\n" +
				"after:foo",
				compilerOptions);
	}
	public void test066() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
						"public class X {\n"
						+ "    protected Object x = \"FIELD X\";\n"
						+ "    public void f(Object obj, boolean b) {\n"
						+ "        if ((x instanceof String x)) {\n"
						+ "            System.out.println(x.toLowerCase());\n"
						+ "        }\n"
						+ "    }\n"
						+ "	public static void main(String[] args) {\n"
						+ "		new X().f(Integer.parseInt(\"1\"), false);\n"
						+ "	}\n"
						+ "}",
				},
				"field x",
				compilerOptions);
	}
	public void test067() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
						"public class X {\n"
						+ "    protected Object x = \"FIELD X\";\n"
						+ "    public void f(Object obj, boolean b) {\n"
						+ "        if ((x instanceof String x) && x.length() > 0) {\n"
						+ "            System.out.println(x.toLowerCase());\n"
						+ "        }\n"
						+ "    }\n"
						+ "	public static void main(String[] args) {\n"
						+ "		new X().f(Integer.parseInt(\"1\"), false);\n"
						+ "	}\n"
						+ "}",
				},
				"field x",
				compilerOptions);
	}
	public void test068() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
						"public class X {\n"
						+ "    static void foo(Object o) {\n"
						+ "		if (o instanceof X x || o instanceof X) {\n"
						+ "            System.out.println(\"X\");\n"
						+ "		}\n"
						+ "	}\n"
						+ "	public static void main(String[] args) {\n"
						+ "		foo(new X());\n"
						+ "	}\n"
						+ "}",
				},
				"X",
				compilerOptions);
	}
	public void test069() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
						"class XPlus extends X {}\n"
						+ "public class X {\n"
						+ "    static void foo(Object o) {\n"
						+ "		if (o instanceof X x && x instanceof XPlus x) {\n"
						+ "            System.out.println(\"X\");\n"
						+ "		}\n"
						+ "	}\n"
						+ "	public static void main(String[] args) {\n"
						+ "		foo(new X());\n"
						+ "	}\n"
						+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	if (o instanceof X x && x instanceof XPlus x) {\n" +
				"	                                           ^\n" +
				"Duplicate local variable x\n" +
				"----------\n",
				null,
				true,
				compilerOptions);
	}
	// Javac rejects this. Need to check with the spec authors
	public void test070() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
						"public class X {\n"
						+ "    static void foo(Object o) {\n"
						+ "		if (o instanceof X x || o instanceof X x) {\n"
						+ "            System.out.println(\"X\");\n"
						+ "		}\n"
						+ "	}\n"
						+ "	public static void main(String[] args) {\n"
						+ "		foo(new X());\n"
						+ "	}\n"
						+ "}",
				},
				"X",
				compilerOptions);
	}
	// Javac rejects the code on the IF itself (same as above)
	public void test071() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
								"public class X {\n"
								+ "    static void foo(Object o) {\n"
								+ "		if (o instanceof X x || o instanceof X x) {\n"
								+ "            System.out.println(x);\n"
								+ "		}\n"
								+ "	}\n"
								+ "	public static void main(String[] args) {\n"
								+ "		foo(new X());\n"
								+ "	}\n"
								+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	System.out.println(x);\n" +
				"	                   ^\n" +
				"x cannot be resolved to a variable\n" +
				"----------\n",
				null,
				true,
				compilerOptions);
	}
	// Javac rejects the code on the IF itself (same as above)
	public void test072() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
								"public class X {\n"
								+ "    static void foo(Object o) {\n"
								+ "		if (o instanceof X x || o instanceof X x) {\n"
								+ "			throw new IllegalArgumentException();\n"
								+ "		}\n"
								+ "     System.out.println(x);\n"
								+ "	}\n"
								+ "	public static void main(String[] args) {\n"
								+ "		foo(new X());\n"
								+ "	}\n"
								+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	System.out.println(x);\n" +
				"	                   ^\n" +
				"x cannot be resolved to a variable\n" +
				"----------\n",
				null,
				true,
				compilerOptions);
	}
	public void test073() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
								"public class X {\n"
								+ " static void foo(Object o) {\n"
								+ "		try {\n"
								+ "			if (!(o instanceof X x) || x != null || x!= null) { // allowed \n"
								+ "				throw new IllegalArgumentException();\n"
								+ "			}\n"
								+ "    	 	System.out.println(x); // allowed \n"
								+ "	  	} catch (Throwable e) {\n"
								+ "	  		e.printStackTrace(System.out);\n"
								+ "	  	}\n"
								+ "	}\n"
								+ "	public static void main(String[] args) {\n"
								+ "		foo(new X());\n"
								+ "	}\n"
								+ "}",
				},
				"java.lang.IllegalArgumentException\n" +
				"	at X.foo(X.java:6)\n" +
				"	at X.main(X.java:14)",
				compilerOptions);
	}
	public void test074() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
								"public class X {\n"
								+ "   static void foo(Object o) {\n"
								+ "		if (!(o instanceof X x) || x != null || x!= null) {\n"
								+ "     	System.out.println(x); // not allowed\n"
								+ "		}\n"
								+ "	  }\n"
								+ "	public static void main(String[] args) {\n"
								+ "		foo(new X());\n"
								+ "	}\n"
								+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	System.out.println(x); // not allowed\n" +
				"	                   ^\n" +
				"x cannot be resolved to a variable\n" +
				"----------\n",
				null,
				true,
				compilerOptions);
	}
	public void test075() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"+
								"public class X {\n"
								+ " public boolean isMyError(Exception e) {\n"
								+ "        return e instanceof MyError my && (my.getMessage().contains(\"something\") || my.getMessage().contains(\"somethingelse\"));\n"
								+ " }\n"
								+ "	public static void main(String[] args) {\n"
								+ "		System.out.println(\"hello\");\n"
								+ "	}\n"
								+ "}\n"
								+ "class MyError extends Exception {}\n",
				},
				"hello",
				compilerOptions);
	}
	public void test076() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
						"@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
						+ "   static void foo(Object o) {\n"
						+ "	   if ( (! (o instanceof String a)) || (o instanceof String a) ) {\n"
						+ "		   // Nothing\n"
						+ "	   }\n"
						+ "	  }\n"
						+ "	public static void main(String[] args) {\n"
						+ "		System.out.println(\"hello\");\n"
						+ "	}\n"
						+ "}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	if ( (! (o instanceof String a)) || (o instanceof String a) ) {\n" +
				"	                                                         ^\n" +
				"Duplicate local variable a\n" +
				"----------\n",
				null,
				true,
				compilerOptions);
	}
	// Test that a non final pattern variable can be assigned again
	public void test077() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
								"public class X {\n"
								+ "    static void foo(Object o) {\n"
								+ "		if (o instanceof X x) {\n"
								+ "			x = null;\n"
								+ "         System.out.println(x);\n"
								+ "		}\n"
								+ "	}\n"
								+ "	public static void main(String[] args) {\n"
								+ "		foo(new X());\n"
								+ "	}\n"
								+ "}",
				},
				"null",
				compilerOptions);
	}
	// Test that a final pattern variable cannot be assigned again
	public void test078() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
								"public class X {\n"
								+ "    static void foo(Object o) {\n"
								+ "		if (o instanceof final X x) {\n"
								+ "			x = null;\n"
								+ "		}\n"
								+ "	}\n"
								+ "	public static void main(String[] args) {\n"
								+ "		foo(new X());\n"
								+ "	}\n"
								+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	x = null;\n" +
				"	^\n" +
				"The pattern variable x is final and cannot be assigned again\n" +
				"----------\n",
				null,
				true,
				compilerOptions);
	}
	public void test079() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
								"public class X {\n"
								+ "    static void foo(Object o) {\n"
								+ "		if (o instanceof public X x) {\n"
								+ "			x = null;\n"
								+ "		}\n"
								+ "	}\n"
								+ "	public static void main(String[] args) {\n"
								+ "		foo(new X());\n"
								+ "	}\n"
								+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (o instanceof public X x) {\n" +
				"	                          ^\n" +
				"Illegal modifier for the pattern variable x; only final is permitted\n" +
				"----------\n",
				null,
				true,
				compilerOptions);
	}
	// test that we allow final for a pattern instanceof variable
	public void test080() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "    static void foo(Object o) {\n"
						+ "		if (o instanceof final X x) {\n"
						+ "            System.out.println(\"X\");\n"
						+ "		}\n"
						+ "	}\n"
						+ "	public static void main(String[] args) {\n"
						+ "		foo(new X());\n"
						+ "	}\n"
						+ "}",
				},
				"X",
				compilerOptions);
	}
	public void test081() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
								"public class X<T> {\n"
								+ "	public void foo(T o) {\n"
								+ "		// Rejected\n"
								+ "		boolean b1 = (o instanceof String a) ? (o instanceof String a) : false;\n"
								+ "		boolean b2 = !(o instanceof String a) ? (o instanceof String a) : false;\n"
								+ "		boolean b3 = (o instanceof String a) ? !(o instanceof String a) : false;\n"
								+ "		boolean b4 = !(o instanceof String a) ? !(o instanceof String a) : false;\n"
								+ "		\n"
								+ "		boolean b5 = (o instanceof String a) ? true : (o instanceof String a);\n"
								+ "		boolean b6 = !(o instanceof String a) ? true : (o instanceof String a);\n"
								+ "		boolean b7 = (o instanceof String a) ? true : !(o instanceof String a);\n"
								+ "		boolean b8 = !(o instanceof String a) ? true : !(o instanceof String a);\n"
								+ "		\n"
								+ "		boolean b9 = (o instanceof String) ? (o instanceof String a) : (o instanceof String a);\n"
								+ "		boolean b10 = (o instanceof String) ? !(o instanceof String a) : !(o instanceof String a);\n"
								+ "		\n"
								+ "		// These are allowed\n"
								+ "		boolean b11 = (o instanceof String) ? !(o instanceof String a) : !!(o instanceof String a);\n"
								+ "		boolean b12 = (o instanceof String) ? !(o instanceof String a) : (o instanceof String a);\n"
								+ "		boolean b21 = (o instanceof String a) ? false : ((o instanceof String a) ? false : true); \n"
								+ "	} \n"
								+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	boolean b1 = (o instanceof String a) ? (o instanceof String a) : false;\n" +
				"	                                                            ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	boolean b2 = !(o instanceof String a) ? (o instanceof String a) : false;\n" +
				"	                                                             ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	boolean b3 = (o instanceof String a) ? !(o instanceof String a) : false;\n" +
				"	                                                             ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	boolean b4 = !(o instanceof String a) ? !(o instanceof String a) : false;\n" +
				"	                                                              ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 9)\n" +
				"	boolean b5 = (o instanceof String a) ? true : (o instanceof String a);\n" +
				"	                                                                   ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 10)\n" +
				"	boolean b6 = !(o instanceof String a) ? true : (o instanceof String a);\n" +
				"	                                                                    ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 11)\n" +
				"	boolean b7 = (o instanceof String a) ? true : !(o instanceof String a);\n" +
				"	                                                                    ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 12)\n" +
				"	boolean b8 = !(o instanceof String a) ? true : !(o instanceof String a);\n" +
				"	                                                                     ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 14)\n" +
				"	boolean b9 = (o instanceof String) ? (o instanceof String a) : (o instanceof String a);\n" +
				"	                                                                                    ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"10. ERROR in X.java (at line 15)\n" +
				"	boolean b10 = (o instanceof String) ? !(o instanceof String a) : !(o instanceof String a);\n" +
				"	                                                                                       ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n",
				null,
				true,
				compilerOptions);
	}
	public void testBug570831a() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
							"public class X {\n"
							+ "	public static void run() {\n"
							+ "		String s = \"s\";\n"
							+ "		Object o = null;\n"
							+ "		{\n"
							+ "			while (!(o instanceof String v)) {\n"
							+ "				o = null;\n"
							+ "			}\n"
							+ "			s = s + v; // allowed\n"
							+ "		}\n"
							+ "		for (int i = 0; i < 1; i++) {\n"
							+ "			s = s + v; // not allowed\n"
							+ "		}\n"
							+ "	}\n"
							+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 12)\n" +
				"	s = s + v; // not allowed\n" +
				"	        ^\n" +
				"v cannot be resolved to a variable\n" +
				"----------\n",
				null,
				true,
				compilerOptions);
	}
	public void testBug570831b() {
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
							"public class X {\n"
							+ "	public static void run() {\n"
							+ "		String s = \"s\";\n"
							+ "		Object o = null;\n"
							+ "		{\n"
							+ "			int local = 0;\n"
							+ "			while (!(o instanceof String v)) {\n"
							+ "				o = null;\n"
							+ "			}\n"
							+ "			s = s + v; // allowed\n"
							+ "		}\n"
							+ "		for (int i = 0; i < 1; i++) {\n"
							+ "			s = s + v; // not allowed\n"
							+ "		}\n"
							+ "	}\n"
							+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 13)\n" +
				"	s = s + v; // not allowed\n" +
				"	        ^\n" +
				"v cannot be resolved to a variable\n" +
				"----------\n",
				null,
				true,
				compilerOptions);
	}
	public void testBug572380_1() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X1.java",
						"\n"
						+ "public class X1 {\n"
						+ "    boolean b1, b2, b3;\n"
						+ "\n"
						+ "    static boolean bubbleOut(Object obj) {\n"
						+ "	       return obj instanceof X1 that && that.b1 && that.b2 && that.b3;\n"
						+ "    }\n"
						+ "\n"
						+ "    static boolean propagateTrueIn(Object obj) {\n"
						+ "        return obj instanceof X1 that && (that.b1 && that.b2 && that.b3);\n"
						+ "    }\n"
						+ "\n"
						+ "    public static void main(String[] obj) {\n"
						+ "        var ip = new X1();\n"
						+ "        ip.b1 = ip.b2 = ip.b3 = true;\n"
						+ "        System.out.println(bubbleOut(ip) && propagateTrueIn(ip));\n"
						+ "    }\n"
						+ "\n"
						+ "}\n",
				},
				"true",
				options);
	}
	public void testBug572380_2() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X1.java",
						"\n"
						+ "public class X1 {\n"
						+ "    boolean b1, b2, b3;\n"
						+ "    static boolean testErrorOr(Object obj) {\n"
						+ "        return (!(obj instanceof X1 that)) || that.b1 && that.b2;\n"
						+ "    }\n"
						+ "    \n"
						+ "    public static void main(String[] obj) {\n"
						+ "        var ip = new X1();\n"
						+ "        ip.b1 = ip.b2 = ip.b3 = true;\n"
						+ "        System.out.println(testErrorOr(ip));\n"
						+ "    }\n"
						+ "\n"
						+ "}\n",
				},
				"true",
				options);
	}
    public void testBug574892() {
        Map<String, String> options = getCompilerOptions(false);
        runConformTest(
                new String[] {
                        "X1.java",
                        "\n"
                        + "public class X1 {\n"
                        + "    static boolean testConditional(Object obj) {\n"
                        + "        return obj instanceof Integer other\n"
                        + "                && ( other.intValue() > 100\n"
                        + "                   ? other.intValue() < 200 : other.intValue() < 50);\n"
                        + "    }\n"
                        + "    public static void main(String[] obj) {\n"
                        + "        System.out.println(testConditional(101));\n"
                        + "    }\n"
                        + "}\n",
                },
                "true",
                options);
    }
	public void testBug572431_1() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "			   static public void something () {\n"
						+ "			      boolean bool = true;\n"
						+ "			      Object object = null;\n"
						+ "			      if (object instanceof String string) {\n"
						+ "			      } else if (bool && object instanceof Integer integer) {\n"
						+ "			      }\n"
						+ "			   }\n"
						+ "			   static public void main (String[] args) throws Exception {\n"
						+ "			   }\n"
						+ "			}",
				},
				"",
				options);

	}
	public void testBug572431_2() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "  static public void something () {\n"
						+ "    boolean bool = true;\n"
						+ "    Object object = null;\n"
						+ "    if (object instanceof String string) {\n"
						+ "    } else if (bool) {\n"
						+ "      if (object instanceof Integer integer) {\n"
						+ "      }\n"
						+ "    }\n"
						+ "  }\n"
						+ "  static public void main (String[] args) throws Exception {\n"
						+ "  }\n"
						+ "}",
				},
				"",
				options);

	}
	public void testBug572431_3() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "  static public void something () {\n"
						+ "    boolean bool = true;\n"
						+ "    Object object = null;\n"
						+ "    if (bool && object instanceof Integer i) {\n"
						+ "	   }\n"
						+ "  }\n"
						+ "  static public void main (String[] args) throws Exception {\n"
						+ "  }\n"
						+ "}",
				},
				"",
				options);

	}
	public void testBug572431_4() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "  static public void something () {\n"
						+ "    boolean bool = true;\n"
						+ "    Object object = null;\n"
						+ "    if (!(object instanceof Integer i)) {\n"
						+ "	   }\n"
						+ "  }\n"
						+ "  static public void main (String[] args) throws Exception {\n"
						+ "  }\n"
						+ "}",
				},
				"",
				options);

	}
	public void testBug572431_5() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "  static public void something () {\n"
						+ "    boolean bool = true;\n"
						+ "    Object object = null;\n"
						+ "    if (false) {\n"
						+ "	   } else if (!(object instanceof Integer i)) {\n"
						+ "	   }\n"
						+ "  }\n"
						+ "  static public void main (String[] args) throws Exception {\n"
						+ "  }\n"
						+ "}",
				},
				"",
				options);

	}
	public void testBug572431_6() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "  static public void something () {\n"
						+ "    boolean bool = true;\n"
						+ "		Object object = null;\n"
						+ "		for (int i = 0; i < 10; i++) {\n"
						+ "			if (object instanceof String string) {\n"
						+ "				System.out.println(i);\n"
						+ "			} else if (bool) {\n"
						+ "				if (i == 4) continue;\n"
						+ "				System.out.println(i);\n"
						+ "			}\n"
						+ "		}\n"
						+ "  }\n"
						+ "  static public void main (String[] args) throws Exception {\n"
						+ "  }\n"
						+ "}",
				},
				"",
				options);

	}
	public void testBug573880() {
		if (this.complianceLevel < ClassFileConstants.JDK17)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runNegativeTest(
				new String[] {
						"X.java",
							"public class X {\n"
							+ "	public void foo(Object o) {\n"
							+ "		if (o instanceof var s) {\n"
							+ "			System.out.println(s);\n"
							+ "		}\n"
							+ "	}\n"
							+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (o instanceof var s) {\n" +
				"	                 ^^^\n" +
				"\'var\' is not allowed here\n" +
				"----------\n",
				null,
				true,
				compilerOptions);
	}
    public void testBug574906() {
        Map<String, String> options = getCompilerOptions(false);
        runConformTest(
                new String[] {
                        "X1.java",
                        "\n"
                        + "public class X1 {\n"
                        + "    static boolean testConditional(Object obj) {\n"
                        + "        return obj instanceof Number oNum && oNum.intValue() < 0 && !(oNum instanceof Integer);\n"
                        + "    }\n"
                        + "    public static void main(String[] obj) {\n"
                        + "        System.out.println(testConditional(-2f));\n"
                        + "    }\n"
                        + "}\n",
                },
                "true",
                options);
    }
    public void testBug575035() throws ClassFormatException, IOException {
        Map<String, String> options = getCompilerOptions(false);
    	String source =
    			"import java.lang.annotation.ElementType;\n" +
    			"import java.lang.annotation.Retention;\n" +
    			"import java.lang.annotation.RetentionPolicy;\n" +
    			"import java.lang.annotation.Target;\n" +
    			" \n" +
    			"public class Test {\n" +
    			"    @Target({ ElementType.LOCAL_VARIABLE})\n" +
    			"    @Retention(RetentionPolicy.RUNTIME)\n" +
    			"    @interface Var {}\n" +
    			"    @Target({ ElementType.TYPE_USE})\n" +
    			"    @Retention(RetentionPolicy.RUNTIME)\n" +
    			"    @interface Type {}\n" +
    			"    public static void main(String[] args) {" +
    			"        @Var @Type String y = \"OK: \";\n" +
    			"        if (((Object)\"local\") instanceof @Var @Type String x) {\n" +
    			"            System.out.println(new StringBuilder(y).append(x));\n" +
    			"        }\n" +
    		    "    }\n" +
    			"}";
    	String expectedOutput =  "  // Stack: 4, Locals: 5\n" +
    			"  public static void main(String[] args);\n" +
    			"     0  ldc <String \"OK: \"> [16]\n" +
    			"     2  astore_1 [y]\n" +
    			"     3  ldc <String \"local\"> [18]\n" +
    			"     5  astore 4\n" +
    			"     7  aload 4\n" +
    			"     9  instanceof String [20]\n" +
    			"    12  ifeq 46\n" +
    			"    15  aload 4\n" +
    			"    17  checkcast String [20]\n" +
    			"    20  dup\n" +
    			"    21  astore_2\n" +
    			"    22  aload 4\n" +
    			"    24  checkcast String [20]\n" +
    			"    27  pop2\n" +
    			"    28  getstatic System.out : PrintStream [22]\n" +
    			"    31  new StringBuilder [28]\n" +
    			"    34  dup\n" +
    			"    35  aload_1 [y]\n" +
    			"    36  invokespecial StringBuilder(String) [30]\n" +
    			"    39  aload_2 [x]\n" +
    			"    40  invokevirtual StringBuilder.append(String) : StringBuilder [33]\n" +
    			"    43  invokevirtual PrintStream.println(Object) : void [37]\n" +
    			"    46  return\n" +
    			"      Line numbers:\n" +
    			"        [pc: 0, line: 13]\n" +
    			"        [pc: 3, line: 14]\n" +
    			"        [pc: 28, line: 15]\n" +
    			"        [pc: 46, line: 17]\n" +
    			"      Local variable table:\n" +
    			"        [pc: 0, pc: 47] local: args index: 0 type: String[]\n" +
    			"        [pc: 3, pc: 47] local: y index: 1 type: String\n" +
    			"        [pc: 28, pc: 46] local: x index: 2 type: String\n" +
    			"      Stack map table: number of frames 1\n" +
    			"        [pc: 46, append: {String}]\n" +
    			"    RuntimeVisibleTypeAnnotations: \n" +
    			"      #50 @Type(\n" +
    			"        target type = 0x40 LOCAL_VARIABLE\n" +
    			"        local variable entries:\n" +
    			"          [pc: 3, pc: 47] index: 1\n" +
    			"      )\n" +
    			"      #50 @Type(\n" +
    			"        target type = 0x40 LOCAL_VARIABLE\n" +
    			"        local variable entries:\n" +
    			"          [pc: 28, pc: 46] index: 2\n" +
    			"      )\n";
    	checkClassFile("Test", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
        runConformTest(
                new String[] {
                        "Test.java",
                        source,
                },
                "OK: local",
                options);

    }
	public void testBug578628_1() {
		if (this.complianceLevel < ClassFileConstants.JDK18)
			return;
		runNegativeTest(
				new String[] {
						"X.java",
							"public class X {\n"
							+ "    public static Object str = \"a\";\n"
							+ "    public static void foo() {\n"
							+ "    	if (str instanceof (String a && a == null)) {\n"
							+ "            System.out.println(true);\n"
							+ "        } else {\n"
							+ "        	System.out.println(false);\n"
							+ "        }\n"
							+ "    } \n"
							+ "    public static void main(String[] argv) {\n"
							+ "    	foo();\n"
							+ "    }\n"
							+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	if (str instanceof (String a && a == null)) {\n" +
				"	                           ^\n" +
				"Syntax error, insert \")\" to complete ParenthesizedPattern\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	if (str instanceof (String a && a == null)) {\n" +
				"	                                          ^\n" +
				"Syntax error on token \")\", delete this token\n" +
				"----------\n",
				false);
	}
	public void testBug578628_1a() {
		if (this.complianceLevel < ClassFileConstants.JDK18)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
							"public class X {\n"
							+ "    public static Object str = \"a\";\n"
							+ "    public static void foo() {\n"
							+ "    	if (str instanceof String a && a == null) {\n"
							+ "            System.out.println(true);\n"
							+ "        } else {\n"
							+ "        	System.out.println(false);\n"
							+ "        }\n"
							+ "    } \n"
							+ "    public static void main(String[] argv) {\n"
							+ "    	foo();\n"
							+ "    }\n"
							+ "}",
				},
				"false",
				compilerOptions);
	}
	public void testBug578628_2() {
		if (this.complianceLevel < ClassFileConstants.JDK18)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
							"public class X {\n"
							+ "    public static Object str = \"a\";\n"
							+ "    public static void foo() {\n"
							+ "    	if (str instanceof String a && a != null) {\n"
							+ "            System.out.println(true);\n"
							+ "        } else {\n"
							+ "        	System.out.println(false);\n"
							+ "        }\n"
							+ "    } \n"
							+ "    public static void main(String[] argv) {\n"
							+ "    	foo();\n"
							+ "    }\n"
							+ "}",
				},
				"true",
				compilerOptions);
	}
	public void testBug578628_3() {
		if (this.complianceLevel < ClassFileConstants.JDK18)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
							"public class X {\n"
							+ "    public static Object str = \"a\";\n"
							+ "    public static void foo() {\n"
							+ "    	bar(str instanceof String a && a == null);\n"
							+ "    } \n"
							+ "    public static void bar(boolean arg) {\n"
							+ "    	System.out.println(arg);\n"
							+ "    }\n"
							+ "    public static void main(String[] argv) {\n"
							+ "    	foo();\n"
							+ "    }\n"
							+ "}",
				},
				"false",
				compilerOptions);
	}
	public void testBug578628_4() {
		if (this.complianceLevel < ClassFileConstants.JDK20)
			return;
		Map<String, String> compilerOptions = getCompilerOptions(true);
		runConformTest(
				new String[] {
						"X.java",
							"public class X {\n"
							+ "    public static Object str = \"a\";\n"
							+ "public static void foo() {\n"
							+ "    	boolean b = switch (str) {\n"
							+ "    		case String s -> {\n"
							+ "    			yield (str instanceof String a && a != null);\n"
							+ "    		}\n"
							+ "    		default -> false;\n"
							+ "    	};\n"
							+ "    	System.out.println(b);\n"
							+ "    }\n"
							+ "    public static void main(String[] argv) {\n"
							+ "    	foo();\n"
							+ "    }\n"
							+ "}",
				},
				"true",
				compilerOptions);
	}
}
