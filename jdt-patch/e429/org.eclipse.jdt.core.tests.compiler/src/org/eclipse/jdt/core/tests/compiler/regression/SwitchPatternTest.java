/*******************************************************************************
 * Copyright (c) 2021, 2023 IBM Corporation and others.
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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SwitchPatternTest extends AbstractRegressionTest9 {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testIssueDefaultDominance"};
	}

	private static String previewLevel = "20";

	public static Class<?> testClass() {
		return SwitchPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_20);
	}
	public SwitchPatternTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_20);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_20);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_20);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, "", getCompilerOptions());
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		runConformTest(testFiles, expectedOutput, "", customOptions);
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, String errorOutput) {
		runConformTest(testFiles, expectedOutput, errorOutput, getCompilerOptions());
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, String expectedErrorOutput, Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.expectedErrorString = expectedErrorOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel);
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, "");
	}
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog, String javacLog) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.expectedJavacOutputString = expectedCompilerLog;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = getCompilerOptions();
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel);
		runner.runNegativeTest();
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel));
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
		runWarningTest(testFiles, expectedCompilerLog, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
			Map<String, String> customOptions, String javacAdditionalTestOptions) {

		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel) :
			JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel, javacAdditionalTestOptions);
		runner.runWarningTest();
	}

	private static void verifyClassFile(String expectedOutput, String classFileName, int mode)
			throws IOException, ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
			System.out.println("...");
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}
	public void testIssue57_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer i     -> System.out.println(\"String:\");\n"+
				"     case String s     -> System.out.println(\"String: Hello World!\");\n"+
				"     default       -> System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				" }\n"+
				"}",
			},
			"String: Hello World!");
	}
	public void testIssue57_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer i when i > 10    -> System.out.println(\"Integer: greater than 10\");\n"+
				"     case String  s   -> System.out.println(\"String: Hello World!\");\n"+
				"     default       -> System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(12);\n"+
				" }\n"+
				"}",
			},
			"Integer: greater than 10");
	}
	public void testBug573516_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case String s     -> System.out.println(\"String:\");\n"+
				"     default       -> System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case String s  : System.out.println(\"String:\"); break;\n"+
				"     case Integer i  : System.out.println(\"Integer:\");break;\n"+
				"     default       : System.out.println(\"Object\");break;\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer t when t > 0 -> System.out.println(\"Integer && t > 0\");\n"+
				"     default       -> System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer t, String s, X x : System.out.println(\"Integer, String or X\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s, X x : System.out.println(\"Integer, String or X\");\n" +
			"	                ^^^^^^^^\n" +
			"A switch label may not have more than one pattern case label element\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n" +
			"	                ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"A switch label may not have more than one pattern case label element\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n" +
			"	                                ^^^^^^\n" +
			"length cannot be resolved or is not a field\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer t, String : System.out.println(\"Error should be flagged for String\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String : System.out.println(\"Error should be flagged for String\");\n" +
			"	                ^^^^^^\n" +
			"String cannot be resolved to a variable\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_008() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o.hashCode()) {\n"+
				"     case Integer t, String : System.out.println(\"Error should be flagged for Integer and String\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String : System.out.println(\"Error should be flagged for Integer and String\");\n" +
			"	     ^^^^^^^^^\n" +
			"Type mismatch: cannot convert from int to Integer\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String : System.out.println(\"Error should be flagged for Integer and String\");\n" +
			"	                ^^^^^^\n" +
			"String cannot be resolved to a variable\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o.hashCode()) {\n"+
				"     case null, default : System.out.println(\"Default\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case null, default : System.out.println(\"Default\");\n" +
			"	     ^^^^\n" +
			"Type mismatch: cannot convert from null to int\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	default : System.out.println(\"Object\");\n" +
			"	^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_010() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o.hashCode()) {\n"+
				"     case String s, default : System.out.println(\"Error should be flagged for String and default\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case String s, default : System.out.println(\"Error should be flagged for String and default\");\n" +
			"	     ^^^^^^^^\n" +
			"Type mismatch: cannot convert from int to String\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case String s, default : System.out.println(\"Error should be flagged for String and default\");\n" +
			"	               ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case String s, default : System.out.println(\"Error should be flagged for String and default\");\n" +
			"	               ^^^^^^^\n" +
			"A switch label may not have both a pattern case label element and a default case label element\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	default : System.out.println(\"Object\");\n" +
			"	^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug573516_011() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o.hashCode()) {\n"+
				"     case var s : System.out.println(\"Error should be ANY_PATTERN\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case var s : System.out.println(\"Error should be ANY_PATTERN\");\n" +
			"	     ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug574228_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case 1: System.out.println(\"Integer\"); break;\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				"   public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"     Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case 1: System.out.println(\"Integer\"); break;\n" +
			"	     ^\n" +
			"Type mismatch: cannot convert from int to Object\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}

	public void testBug573936_01() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(I); \n"+
					"     case String s when s.length()>1: \n"+
					"       System.out.println(\"String s && s.length()>1\"); \n"+
					"       System.out.println(s); \n"+
					"       break;// error no fallthrough allowed in pattern\n"+
					"     case X x:\n"+
					"       System.out.println(\"X\"); \n"+
					"       System.out.println(x);\n"+
					"       break;\n"+
					"     default : System.out.println(\"Object\"); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					"     foo(\"H\");\n"+
					"   foo(bar());\n"+
					" }\n"+
					"   public static Object bar() { return new Object();}\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	case String s when s.length()>1: \n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Illegal fall-through to a pattern\n" +
				"----------\n");
	}
	public void testBug573939_01() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer s : System.out.println(\"Integer\");\n"+
					"     case String s1: System.out.println(\"String \");\n"+
					"     default : System.out.println(\"Object\");\n"+
					"   }\n"+
					" }\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World\");\n"+
					"   Zork();\n"+
					" }\n"+
					"}\n"+
					"class Y {}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case String s1: System.out.println(\"String \");\n" +
				"	^^^^^^^^^^^^^^\n" +
				"Illegal fall-through to a pattern\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 11)\n" +
				"	Zork();\n" +
				"	^^^^\n" +
				"The method Zork() is undefined for the type X\n" +
				"----------\n");
	}
	public void testBug573939_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: System.out.println(\"Integer\"); break;\n"+
					"     case String s when s.length()>1: System.out.println(\"String > 1\"); break;\n"+
					"     case String s1: System.out.println(\"String\"); break;\n"+
					"     case X x: System.out.println(\"X\"); break;\n"+
					"     default : System.out.println(\"Object\");\n"+
					"   }\n"+
					" }\n"+
					"   public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					"   foo(\"H\");\n"+
					"   foo(bar());\n"+
					" }\n"+
					"   public static Object bar() { return new Object();}\n"+
					"}",
				},
				"String > 1\n" +
				"String\n" +
				"Object");
	}
	public void testBug573939_03() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(I); \n"+
					"       break; \n"+
					"     case String s when s.length()>1: \n"+
					"       System.out.println(\"String s when s.length()>1\"); \n"+
					"       System.out.println(s); \n"+
					"       break;\n"+
					"     case String s1: \n"+
					"       System.out.println(\"String\"); \n"+
					"       System.out.println(s1);\n"+
					"       break; \n"+
					"     case X x:\n"+
					"       System.out.println(\"X\"); \n"+
					"       System.out.println(x);\n"+
					"       break;\n"+
					"     default : System.out.println(\"Object\"); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					"     foo(\"H\");\n"+
					"   foo(bar());\n"+
					" }\n"+
					"   public static Object bar() { return new Object();}\n"+
					"}",
				},
				"String s when s.length()>1\n" +
				"Hello World!\n" +
				"String\n" +
				"H\n" +
				"Object");
	}
	public void testBug573939_03b() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(I); \n"+
					"       break; \n"+
					"     case String s when s.length()>1: \n"+
					"       System.out.println(\"String s when s.length()>1\"); \n"+
					"       System.out.println(s); \n"+
					"       break;\n"+
					"     case String s: \n"+
					"       System.out.println(\"String\"); \n"+
					"       System.out.println(s);\n"+
					"       break; \n"+
					"     case X x:\n"+
					"       System.out.println(\"X\"); \n"+
					"       System.out.println(x);\n"+
					"       break;\n"+
					"     default : System.out.println(\"Object\"); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					"     foo(\"H\");\n"+
					"   foo(bar());\n"+
					" }\n"+
					"   public static Object bar() { return new Object();}\n"+
					"}",
				},
				"String s when s.length()>1\n" +
				"Hello World!\n" +
				"String\n" +
				"H\n" +
				"Object");
	}
	public void test045() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    switch (args.length) {\n" +
				"      case 1:\n" +
				"        final int j = 1;\n" +
				"      case 2:\n" +
				"        switch (5) {\n" +
				"          case j:\n" +
				"        }\n" +
				"    }\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	case j:\n" +
			"	     ^\n" +
			"The local variable j may not have been initialized\n" +
			"----------\n");
	}
	public void testBug574525_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(I); \n"+
					"       break; \n"+
					"     case null:\n"+
					"       System.out.println(\"NULL\"); \n"+
					"       break;\n"+
					"     default : System.out.println(\"Object\"); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"     foo(null);\n"+
					" }\n"+
					"}",
				},
				"NULL");
	}
	public void testBug574525_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case Integer I: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(I); \n"+
					"       break; \n"+
					"     case String s when s.length()>1: \n"+
					"       System.out.println(\"String s when s.length()>1\"); \n"+
					"       System.out.println(s); \n"+
					"       break;\n"+
					"     case String s1: \n"+
					"       System.out.println(\"String\"); \n"+
					"       System.out.println(s1);\n"+
					"       break; \n"+
					"     case X x:\n"+
					"       System.out.println(\"X\"); \n"+
					"       System.out.println(x);\n"+
					"       break;\n"+
					"     case null:\n"+
					"       System.out.println(\"NULL\"); \n"+
					"       break;\n"+
					"     default : System.out.println(\"Object\"); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					"   foo(null);\n"+
					"   foo(bar());\n"+
					" }\n"+
					"   public static Object bar() { return new Object();}\n"+
					"}",
				},
				"String s when s.length()>1\n" +
				"Hello World!\n" +
				"NULL\n" +
				"Object");
	}
	public void testBug574525_03() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Integer o) {\n"+
					"   switch (o) {\n"+
					"     case 10: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(o); \n"+
					"       break; \n"+
					"     case null:\n"+
					"       System.out.println(\"NULL\"); \n"+
					"       break;\n"+
					"     default : System.out.println(o); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"     foo(0);\n"+
					" }\n"+
					"}",
				},
				"0");
	}
	public void testBug574525_04() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(int o) {\n"+
					"   switch (o) {\n"+
					"     case 10: \n"+
					"       System.out.println(\"Integer\"); \n"+
					"       System.out.println(o); \n"+
					"       break; \n"+
					"     case null:\n"+
					"       System.out.println(\"NULL\"); \n"+
					"       break;\n"+
					"     default : System.out.println(o); \n"+
					"   }\n"+
					" }   \n"+
					"   public static void main(String[] args) {\n"+
					"     foo(0);\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	case null:\n" +
				"	     ^^^^\n" +
				"Type mismatch: cannot convert from null to int\n" +
				"----------\n");
	}
	public void testBug574538_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(Integer.valueOf(11));\n"+
					"   foo(Integer.valueOf(9));\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"   case Integer i when i>10:\n"+
					"     System.out.println(\"Greater than 10:\" + o);\n"+
					"     break;\n"+
					"   case Integer j when j>0:\n"+
					"     System.out.println(\"Greater than 0:\" + o);\n"+
					"     break;\n"+
					"   default:\n"+
					"     System.out.println(\"Object\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"Greater than 10:11\n" +
				"Greater than 0:9");
	}
	public void testBug574538_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo1(Integer.valueOf(10));\n"+
					"   foo1(Integer.valueOf(11));\n"+
					"   foo1(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo1(Object o) {\n"+
					"   switch (o) {\n"+
					"   case Integer i when i>10 -> System.out.println(\"Greater than 10:\");\n"+
					"   case String s when s.equals(\"ff\") -> System.out.println(\"String:\" + s);\n"+
					"   default -> System.out.println(\"Object:\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"Object:10\n" +
				"Greater than 10:\n" +
				"Object:Hello World!");
	}

	public void testBug574549_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"    case null, default:\n"+
					"     System.out.println(\"Object: \" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"Object: Hello World!");
	}
	public void testBug574549_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(Integer.valueOf(11));\n"+
					"   foo(Integer.valueOf(9));\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"   case Integer i when i>10:\n"+
					"     System.out.println(\"Greater than 10:\" + o);\n"+
					"     break;\n"+
					"   case Integer j when j>0:\n"+
					"     System.out.println(\"Greater than 0:\" + o);\n"+
					"     break;\n"+
					"   case null,default:\n"+
					"     System.out.println(\"Give Me Some SunShine:\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"Greater than 10:11\n" +
				"Greater than 0:9\n" +
				"Give Me Some SunShine:Hello World!");
	}
	public void testBug574549_03() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"   case Integer i :\n"+
					"     System.out.println(\"Integer:\" + o);\n"+
					"     break;\n"+
					"   case null, default:\n"+
					"     System.out.println(\"Object\" + o);\n"+
					"   case null, default:\n"+
					"     System.out.println(\"Give me Some Sunshine\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	case null, default:\n" +
				"	     ^^^^\n" +
				"Duplicate case\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 13)\n" +
				"	case null, default:\n" +
				"	     ^^^^\n" +
				"Duplicate case\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 13)\n" +
				"	case null, default:\n" +
				"	           ^^^^^^^\n" +
				"The default case is already defined\n" +
				"----------\n");
	}
	public void testBug574549_04() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"   case Integer i :\n"+
					"     System.out.println(\"Integer:\" + o);\n"+
					"     break;\n"+
					"   case default:\n"+
					"     System.out.println(\"Object\" + o);\n"+
					"   default:\n"+
					"     System.out.println(\"Give me Some Sunshine\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	case default:\n" +
				"	     ^^^^^^^\n" +
				"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
				"----------\n" +
				"2. ERROR in X.java (at line 13)\n" +
				"	default:\n" +
				"	^^^^^^^\n" +
				"The default case is already defined\n" +
				"----------\n");
	}
	// Test that when a pattern variable is unused and when the OPTION_PreserveUnusedLocal
	// option is used, no issue is reported at runtime.
	public void testBug573937_1() {
		Map<String,String> options = getCompilerOptions();
		String opt = options.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		try {
			options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.DISABLED);
			this.runConformTest(
				new String[] {
				"X.java",
					"public class X {\n"
						+ "	public static void main(String[] args) {\n"
						+ "		System.out.println(\"Hello\");\n"
						+ "	}\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s:\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
					},
					"Hello",
					options);
		} finally {
			options.put(CompilerOptions.OPTION_PreserveUnusedLocal, opt);
		}
	}
	// A simple pattern variable in a case is not visible in the
	// following case statement
	public void testBug573937_2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s:\n"
						+ "				System.out.println(s);\n"
						+ "				break;\n"
						+ "			case Integer i:\n"
						+ "				System.out.println(s);\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n");
	}
	// Same as above, but without break statement
	public void testBug573937_3() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s:\n"
						+ "				System.out.println(s);\n"
						+ "			case Integer i:\n"
						+ "				System.out.println(s);\n"
						+ "			default:\n"
						+ "				System.out.println(s);\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 9)\n" +
				"	System.out.println(s);\n" +
				"	                   ^\n" +
				"s cannot be resolved to a variable\n" +
				"----------\n");
	}
	// Test that compiler rejects attempts to redeclare local variable
	// with same name as a pattern variable
	public void testBug573937_4() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s:\n"
						+ "				String s = null;\n"
						+ "				System.out.println(s);\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	String s = null;\n" +
				"	       ^\n" +
				"Duplicate local variable s\n" +
				"----------\n");
	}
	// Test that compiler allows local variable with same name as a
	// pattern variable in a different case statement
	public void testBug573937_5() {
		this.runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
								+ "	public static void foo(Object o) {\n"
								+ "		switch (o) {\n"
								+ "			case String s:\n"
								+ "				System.out.println(s);\n"
								+ "				break;\n"
								+ "			default:\n"
								+ "				String s = null;\n"
								+ "				break;\n"
								+ "		}\n"
								+ "	}\n"
								+ "	public static void main(String[] args) {\n"
								+ "		foo(\"hello\");\n"
								+ "	}\n"
								+ "}",
				},
				"hello");
	}
	// Test that a pattern variable can't use name of an already existing local
	// variable
	public void testBug573937_6() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String o:\n"
						+ "				System.out.println(o);\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case String o:\n" +
				"	            ^\n" +
				"Duplicate local variable o\n" +
				"----------\n");
	}
	// Test that compiler rejects attempts to redeclare another pattern
	// variable (instanceof) with same name as that a pattern variable in
	// that case statement
	public void testBug573937_7() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s1:\n"
						+ "				if (o instanceof String s1) {\n"
						+ "					System.out.println(s1);\n"
						+ "				}\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	if (o instanceof String s1) {\n" +
				"	                        ^^\n" +
				"Duplicate local variable s1\n" +
				"----------\n");
	}
	// Test that when multiple case statements declare pattern variables
	// with same name, correct ones are used in their respective scopes.
	public void testBug573937_8() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case String s1:\n"
						+ "				System.out.println(s1.length());\n"
						+ "				break;\n"
						+ "			case Integer s1:\n"
						+ "				System.out.println(s1.length());\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	System.out.println(s1.length());\n" +
				"	                      ^^^^^^\n" +
				"The method length() is undefined for the type Integer\n" +
				"----------\n");
	}
	// Test that a pattern variable declared in the preceding case statement
	// can't be used in the case statement itself
	public void testBug573937_9() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		switch (o) {\n"
						+ "			case Integer i1:\n"
						+ "				break;\n"
						+ "			case String s1 when s1.length() > i1:\n"
						+ "					System.out.println(s1.length());\n"
						+ "				break;\n"
						+ "			default:\n"
						+ "				break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case String s1 when s1.length() > i1:\n" +
				"	                                  ^^\n" +
				"i1 cannot be resolved to a variable\n" +
				"----------\n");
	}
	// Test that redefining pattern variables with null is allowed
	// and produce expected result (NPE) when run.
	public void testBug573937_10() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"public class X {\n"
				+ "@SuppressWarnings(\"null\")"
				+ "	public static void foo(Object o) {\n"
				+ "	  try {\n"
				+ "		switch (o) {\n"
				+ "			case String s1 when s1.length() == 0:\n"
				+ "					break;"
				+ "			case String s1:\n"
				+ "					s1 = null;\n"
				+ "					System.out.println(s1.length());\n"
				+ "				break;\n"
				+ "			default:\n"
				+ "				break;\n"
				+ "		}\n"
				+ "	  } catch(Exception e) {\n"
				+ "    System.out.println(e.getMessage());\n"
				+ "	  };\n"
				+ "	}\n"
				+ "	public static void main(String[] args) {\n"
				+ "		foo(\"hello\");\n"
				+ "	}\n"
				+ "}",
		};
		runner.expectedOutputString = "Cannot invoke \"String.length()\" because \"s1\" is null";
		runner.expectedJavacOutputString = "Cannot invoke \"String.length()\" because \"<local4>\" is null";
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = getCompilerOptions();
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel);
		runner.runConformTest();
	}
	// Test that a pattern variable is allowed in a switch label throw
	// statement and when run, produces expected result
	public void testBug573937_11() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"public class X {\n"
				+ "	public static void foo(Object o) throws Exception {\n"
				+ "		switch (o) {\n"
				+ "			case String s1:\n"
				+ "				throw new Exception(s1);\n"
				+ "			default:\n"
				+ "				break;\n"
				+ "		}\n"
				+ "	}\n"
				+ "	public static void main(String[] args) throws Exception {\n"
				+ "		try {\n"
				+ "		  foo(\"hello\");\n"
				+ "		} catch(Exception e) {\n"
				+ "		  e.printStackTrace(System.out);\n"
				+ "		};\n"
				+ "	}\n"
				+ "} ",
		};
		runner.expectedOutputString = "java.lang.Exception: hello\n" +
				"	at X.foo(X.java:5)\n" +
				"	at X.main(X.java:12)";
		runner.expectedJavacOutputString = "java.lang.Exception: hello\n"
				+ "	at X.foo(X.java:5)\n"
				+ "	at X.main(X.java:12)";
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = getCompilerOptions();
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel);
		runner.runConformTest();
	}
	// A non effectively final referenced from the RHS of the guarding expression
	// is reported by the compiler.
	public void testBug574612_1() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		int len = 2;\n"
						+ "		switch (o) {\n"
						+ "		case String o1 when o1.length() > len:\n"
						+ "			len = 0;\n"
						+ "		break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case String o1 when o1.length() > len:\n" +
				"	                                  ^^^\n" +
				"Local variable len referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	// A non effectively final referenced from the LHS of the guarding expression
	// is reported by the compiler.
	public void testBug574612_2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		int len = 2;\n"
						+ "		switch (o) {\n"
						+ "		case String o1 when len < o1.length():\n"
						+ "			len = 0;\n"
						+ "		break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case String o1 when len < o1.length():\n" +
				"	                    ^^^\n" +
				"Local variable len referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	// An explicitly final local variable, also referenced in a guarding expression of a pattern
	// and later on re-assigned is only reported for the explicit final being modified
	public void testBug574612_3() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		final int len = 2;\n"
						+ "		switch (o) {\n"
						+ "		case String o1 when len < o1.length():\n"
						+ "			len = 0;\n"
						+ "		break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	len = 0;\n" +
				"	^^^\n" +
				"The final local variable len cannot be assigned. It must be blank and not using a compound assignment\n" +
				"----------\n");
	}
	public void testBug574612_4() {
		this.runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo(Object o) {\n"
						+ "		int len = 2;\n"
						+ "		switch (o) {\n"
						+ "		case String o1 when len < o1.length():\n"
						+ "			System.out.println(o1);\n"
						+ "		break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "	public static void main(String[] args) throws Exception {\n"
						+ "		foo(\"hello\");\n"
						+ "	}\n"
						+ "} ",
				},
				"hello");
	}
	public void _testBug574719_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0, default   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100 ));\n"+
				" }\n"+
				"}",
			},
			"1");
	}
	public void _testBug574719_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0, default, 1   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100 ));\n"+
				" }\n"+
				"}",
			},
			"1");
	}
	public void _testBug574719_003() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case default, 1   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"1\n" +
			"1");
	}
	public void _testBug574719_004() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0  : k = 2; break;\n"+
				"     case default, 1   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"1\n" +
			"2");
	}
	public void _testBug574719_005() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0  : k = 2; break;\n"+
				"     case 1, default   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"1\n" +
			"2");
	}
	public void testBug574719_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0  : k = 2; break;\n"+
				"     case 1, default, default   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	case 1, default, default   : k = 1;\n" +
			"	        ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	case 1, default, default   : k = 1;\n" +
			"	                 ^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	case 1, default, default   : k = 1;\n" +
			"	                 ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n");
	}
	public void _testBug574719_007() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 10, default: k = 1;break;\n"+
				"     case 0  : k = 2; break;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				"   System.out.println(foo(10));\n"+
				" }\n"+
				"}",
			},
			"1\n"+
			"2\n"+
			"1");
	}
	public void testBug574561_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     default, default  : k = 2; break;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	default, default  : k = 2; break;\n" +
			"	       ^\n" +
			"Syntax error on token \",\", : expected\n" +
			"----------\n");
	}
	public void testBug574561_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case default, 1, default   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case default, 1, default   : k = 1;\n" +
			"	     ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case default, 1, default   : k = 1;\n" +
			"	                 ^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	case default, 1, default   : k = 1;\n" +
			"	                 ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n");
	}
	public void testBug574561_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case default, 1, default   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100));\n"+
				"   System.out.println(foo(0));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case default, 1, default   : k = 1;\n" +
			"	     ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case default, 1, default   : k = 1;\n" +
			"	                 ^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	case default, 1, default   : k = 1;\n" +
			"	                 ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n");
	}
	public void testBug574793_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {}\n"+
				" private static void foo1(int o) {\n"+
				"   switch (o) {\n"+
				"     case null  -> System.out.println(\"null\");\n"+
				"     case 20  -> System.out.println(\"20\");\n"+
				"   }\n"+
				" }\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"   case \"F\"  :\n"+
				"     break;\n"+
				"   case 2 :\n"+
				"     break;\n"+
				"   default:\n"+
				"     break;\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	switch (o) {\n" +
			"	        ^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case null  -> System.out.println(\"null\");\n" +
			"	     ^^^^\n" +
			"Type mismatch: cannot convert from null to int\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\n" +
			"	case \"F\"  :\n" +
			"	     ^^^\n" +
			"Type mismatch: cannot convert from String to Object\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 13)\n" +
			"	case 2 :\n" +
			"	     ^\n" +
			"Type mismatch: cannot convert from int to Object\n" +
			"----------\n");
	}
	public void testBug574559_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {}\n"+
				" public static void foo1(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case 1, Integer i  -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case 1, Integer i  -> System.out.println(o);\n" +
			"	        ^^^^^^^^^\n" +
			"Illegal fall-through to a pattern\n" +
			"----------\n");
	}
	public void testBug574559_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {}\n"+
				" private static void foo1(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case  Integer i, 30  -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case  Integer i, 30  -> System.out.println(o);\n" +
			"	                 ^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n");
	}
	// Test that fall-through to a pattern is not allowed (label statement group has one statement)
	public void testBug573940_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"public void foo(Number n) {\n"
					+ "	switch (n) {\n"
					+ "	case Integer i :\n"
					+ "		System.out.println(i);\n"
					+ "	case Float f :\n"
					+ "		System.out.println(f);\n"
					+ "	case Object o : break;\n"
					+ "	}\n"
					+ "}\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case Float f :\n" +
				"	^^^^^^^^^^^^\n" +
				"Illegal fall-through to a pattern\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	case Object o : break;\n" +
				"	^^^^^^^^^^^^^\n" +
				"Illegal fall-through to a pattern\n" +
				"----------\n");
	}
	// Test that fall-through to a pattern is not allowed (label statement group has zero statement)
	public void testBug573940_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"public void foo(Number n) {\n"
					+ "	switch (n) {\n"
					+ "	case Integer i :\n"
					+ "	case Float f :\n"
					+ "		System.out.println(f);\n"
					+ "     break;\n"
					+ "	default : break;\n"
					+ "	}\n"
					+ "}\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case Integer i :\n" +
				"	^^^^^^^^^^^^^^\n" +
				"Illegal fall-through from a case label pattern\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	case Float f :\n" +
				"	^^^^^^^^^^^^\n" +
				"Illegal fall-through to a pattern\n" +
				"----------\n");
	}
	// Test that fall-through to a pattern is not allowed (label statement group has zero statement)
	public void testBug573940_2a() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"public void foo(Number n) {\n"
					+ "	switch (n) {\n"
					+ "	default :\n"
					+ "	case Float f :\n"
					+ "		System.out.println(f);\n"
					+ "     break;\n"
					+ "	}\n"
					+ "}\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case Float f :\n" +
				"	     ^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	// Test that falling through from a pattern to a default is allowed
	public void testBug573940_3() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "public static void foo(Number n) {\n"
					+ "		switch (n) {\n"
					+ "		case Integer i :\n"
					+ "			System.out.println(i);\n"
					+ "		default:\n"
					+ "			System.out.println(\"null\");\n"
					+ "		}\n"
					+ "	}\n"
					+ "public static void main(String[] args) {\n"
					+ "		foo(Integer.valueOf(5));\n"
					+ "	}\n"
					+ "}",
				},
				"5\n" +
				"null");
	}
	// Test that a case statement with pattern is allowed when statement group ends
	// with an Throw statement instead of a break statement
	public void testBug573940_4() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "public static void foo(Number n) {\n"
					+ "		switch (n) {\n"
					+ "		case Integer i :\n"
					+ "			throw new IllegalArgumentException();\n"
					+ "		default:\n"
					+ "			System.out.println(\"null\");\n"
					+ "		}\n"
					+ "	}\n"
					+ "public static void main(String[] args) {\n"
					+ "		try{\n"
					+ "			foo(Integer.valueOf(5));\n"
					+ "		} catch(Exception e) {\n"
					+ "		 	e.printStackTrace(System.out);\n"
					+ "		}\n"
					+ "	}\n"
					+ "}",
				},
				"java.lang.IllegalArgumentException\n" +
				"	at X.foo(X.java:5)\n" +
				"	at X.main(X.java:12)");
	}
	// Test that switch expression with pattern variables is reported when a case statement
	// doesn't return any value.
	public void testBug573940_5() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	public static void foo(Number n) {\n"
					+ "		int j = \n"
					+ "			switch (n) {\n"
					+ "			case Integer i -> {\n"
					+ "			}\n"
					+ "			default -> {\n"
					+ "				yield 1;\n"
					+ "			}\n"
					+ "		};\n"
					+ "	}\n"
					+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	}\n" +
				"	^^\n" +
				"A switch labeled block in a switch expression should not complete normally\n" +
				"----------\n");
	}
	public void testBug574564_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(new String(\"Hello\"));\n"+
				" }\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case var i  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case var i  -> System.out.println(0);\n" +
			"	     ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n");
	}
	public void testBug574564_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(new String(\"Hello\"));\n"+
				" }\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case var i, var j, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	     ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	            ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	            ^^^^^\n" +
			"A switch label may not have more than one pattern case label element\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	                   ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n");
	}
	public void testBug574564_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case var i, 10  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case var i, 10  -> System.out.println(0);\n" +
			"	     ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n");
	}
	public void testBug574564_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case var i, 10, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case var i, 10, var k  -> System.out.println(0);\n" +
			"	     ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case var i, 10, var k  -> System.out.println(0);\n" +
			"	                ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	case var i, 10, var k  -> System.out.println(0);\n" +
			"	                ^^^^^\n" +
			"A switch label may not have more than one pattern case label element\n" +
			"----------\n");
	}
	public void testBug574564_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case  10, null, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case  10, null, var k  -> System.out.println(0);\n" +
			"	          ^^^^\n" +
			"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case  10, null, var k  -> System.out.println(0);\n" +
			"	                ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n");
	}
	public void testBug574564_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case  default, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case  default, var k  -> System.out.println(0);\n" +
			"	      ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case  default, var k  -> System.out.println(0);\n" +
			"	               ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case  default, var k  -> System.out.println(0);\n" +
			"	               ^^^^^\n" +
			"A switch label may not have both a pattern case label element and a default case label element\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	default -> System.out.println(o);\n" +
			"	^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n");
	}
	public void testBug574564_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case  default, default, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case  default, default, var k  -> System.out.println(0);\n" +
			"	      ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case  default, default, var k  -> System.out.println(0);\n" +
			"	               ^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case  default, default, var k  -> System.out.println(0);\n" +
			"	               ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	case  default, default, var k  -> System.out.println(0);\n" +
			"	                        ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 7)\n" +
			"	case  default, default, var k  -> System.out.println(0);\n" +
			"	                        ^^^^^\n" +
			"A switch label may not have both a pattern case label element and a default case label element\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 8)\n" +
			"	default -> System.out.println(o);\n" +
			"	^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n");
	}
	public void testBug574564_008() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(10);\n"+
				" }\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case  default, 1, var k  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	case  default, 1, var k  -> System.out.println(0);\n" +
			"	      ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case  default, 1, var k  -> System.out.println(0);\n" +
			"	                  ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case  default, 1, var k  -> System.out.println(0);\n" +
			"	                  ^^^^^\n" +
			"A switch label may not have both a pattern case label element and a default case label element\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	default -> System.out.println(o);\n" +
			"	^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n");
	}
	public void testBug574564_009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case String s, default, Integer i  -> System.out.println(0);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	               ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	               ^^^^^^^\n" +
			"A switch label may not have both a pattern case label element and a default case label element\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	                        ^^^^^^^^^\n" +
			"A switch label may not have more than one pattern case label element\n" +
			"----------\n");
	}
	public void testBug574564_010() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case String s, default, Integer i  -> System.out.println(0);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	            ^\n" +
			"Syntax error on token \"s\", delete this token\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	                                ^\n" +
			"Syntax error on token \"i\", delete this token\n" +
			"----------\n",
			null,
			true,
			options);
	}
	public void testBug574564_011() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case null  -> System.out.println(0);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case null  -> System.out.println(0);\n" +
			"	     ^^^^\n" +
			"Pattern Matching in Switch is a preview feature and disabled by default. Use --enable-preview to enable\n" +
			"----------\n",
			null,
			true,
			options);
	}
	public void testBug574564_012() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case 1, default, null  -> System.out.println(0);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case 1, default, null  -> System.out.println(0);\n" +
			"	        ^^^^^^^\n" +
			"Pattern Matching in Switch is a preview feature and disabled by default. Use --enable-preview to enable\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case 1, default, null  -> System.out.println(0);\n" +
			"	                 ^^^^\n" +
			"Pattern Matching in Switch is a preview feature and disabled by default. Use --enable-preview to enable\n" +
			"----------\n",
			null,
			true,
			options);
	}
	public void testBug574564_013() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case default, default -> System.out.println(0);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case default, default -> System.out.println(0);\n" +
			"	     ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case default, default -> System.out.println(0);\n" +
			"	              ^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case default, default -> System.out.println(0);\n" +
			"	              ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n");
	}
	public void testBug574563_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {}\n"+
				" private static void foo1(Integer o) {\n"+
				"   switch (o) {\n"+
				"     case null, null  -> System.out.println(o);\n"+
				"     default  -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case null, null  -> System.out.println(o);\n" +
			"	^^^^^^^^^^^^^^^\n" +
			"Duplicate case\n" +
			"----------\n");
	}
	public void testBug574563_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case null, Integer i  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				" }\n"+
				" public static void bar(Object o) {\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case null, Integer i  -> System.out.println(0);\n" +
			"	           ^^^^^^^^^\n" +
			"A null case label and patterns cannot co-exist in the same case label\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug574563_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer i, null  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case Integer i, null  -> System.out.println(0);\n" +
			"	                ^^^^\n" +
			"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug574563_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case null, Integer i when i > 10 -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case null, Integer i when i > 10 -> System.out.println(0);\n" +
			"	           ^^^^^^^^^^^^^^^^^^^^^\n" +
			"A null case label and patterns cannot co-exist in the same case label\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug574563_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer i when i > 10, null  -> System.out.println(0);\n"+
				"     default -> System.out.println(o);\n"+
				"   }\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case Integer i when i > 10, null  -> System.out.println(0);\n" +
			"	                            ^^^^\n" +
			"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug575030_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(String o) {\n"+
					"   switch (o) {\n"+
					"     case String s -> System.out.println(s);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"Hello World!");
	}
	public void testBug574614_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(Long.valueOf(10));\n"+
				" }\n"+
				" private static void foo(Object o) {\n"+
				"   String s1 = \" Hello \";\n"+
				"   String s2 = \"World!\";\n"+
				"   switch (o) {\n"+
				"     case Integer I when I > 10: break;\n"+
				"      case X J: break;\n"+
				"      case String s : break;\n"+
				"      default:\n"+
				"       s1 = new StringBuilder(String.valueOf(s1)).append(String.valueOf(s2)).toString();\n"+
				"       System.out.println(s1);\n"+
				"       break; \n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"Hello World!");
	}
	public void testBug574614_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   foo(Long.valueOf(0));\n"+
				" }\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"     case Integer I:\n"+
				"       break;\n"+
				"      case String s :\n"+
				"       break;\n"+
				"      case X J:\n"+
				"       break;\n"+
				"      default:\n"+
				"       String s1 = \"Hello \";\n"+
				"       String s2 = \"World!\";\n"+
				"       s1 = s1 +s2; \n"+
				"       System.out.println(s1);\n"+
				"       break;\n"+
				"   }\n"+
				" } \n"+
				"}",
			},
			"Hello World!");
	}
	public void testBug573921_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"		switch(o) {\n" +
					"			case CharSequence cs ->\n" +
					"			System.out.println(\"A sequence of length \" + cs.length());\n" +
					"			case String s when s.length() > 0 -> \n" +
					"			System.out.println(\"A string: \" + s);\n" +
					"			default -> {\n" +
					"				break;\n" +
					"			} \n" +
					"		}\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case String s when s.length() > 0 -> \n" +
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testBug573921_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" private static void foo(Object o) {\n"+
					"		switch(o) {\n" +
					"			case CharSequence cs:\n" +
					"				System.out.println(\"A sequence of length \" + cs.length());\n" +
					"				break;\n" +
					"			case String s:\n" +
					"				System.out.println(\"A string: \" + s);\n" +
					"				break;\n" +
					"			default: \n" +
					"				break;\n" +
					"		}\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	case String s:\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testBug573921_3() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello!\");\n"+
					" }\n"+
					" private static void foo(Object o) {\n"+
					"		switch(o) {\n" +
					"			case String s:\n" +
					"				System.out.println(\"String:\" + s);\n" +
					"				break;\n" +
					"			case CharSequence cs:\n" +
					"				System.out.println(\"A CS:\" + cs);\n" +
					"				break;\n" +
					"			default: \n" +
					"				break;\n" +
					"		}\n"+
					" }\n"+
					"}",
				},
				"String:Hello!");
	}
	public void testBug573921_4() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(new StringBuffer(\"Hello!\"));\n"+
					" }\n"+
					" private static void foo(Object o) {\n"+
					"		switch(o) {\n" +
					"			case String s:\n" +
					"				System.out.println(\"String:\" + s);\n" +
					"				break;\n" +
					"			case CharSequence cs:\n" +
					"				System.out.println(\"A CS:\" + cs.toString());\n" +
					"				break;\n" +
					"			default: \n" +
					"				break;\n" +
					"		}\n"+
					" }\n"+
					"}",
				},
				"A CS:Hello!");
	}
	public void testBug573921_5() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello\");\n"+
					" }\n"+
					" private static void foo(Object o) {\n"+
					"		switch(o) {\n" +
					"		case String s when s.length() < 5 :\n" +
					"			System.out.println(\"1:\" + s);\n" +
					"			break;\n" +
					"		case String s when s.length() == 5:\n" +
					"			System.out.println(\"2:\" + s);\n" +
					"			break;\n" +
					"		default : System.out.println(\"Object\");\n" +
					"	}\n"+
					" }\n"+
					"}",
				},
				"2:Hello");
	}
	public void testBug573921_6() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"\");\n"+
					" }\n"+
					" private static void foo(Object o) {\n"+
					"		switch(o) {\n" +
					"		case String s when s.length() < 5 :\n" +
					"			System.out.println(\"1:\" + s);\n" +
					"			break;\n" +
					"		case String s when s.length() == 5:\n" +
					"			System.out.println(\"2:\" + s);\n" +
					"			break;\n" +
					"		default : System.out.println(\"Object\");\n" +
					"	}\n"+
					" }\n"+
					"}",
				},
				"1:");
	}
	public void testBug573921_7() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n" +
					"public class X {\n"+
					" @SuppressWarnings(\"rawtypes\")\n" +
					" private static void foo(Object o) {\n"+
					"		switch(o) {\n"+
					"		case List cs:\n"+
					"			System.out.println(\"A sequence of length \" + cs.size());\n"+
					"			break;\n"+
					"		case List<String> s: \n"+
					"			System.out.println(\"A string: \" + s);\n"+
					"			break;\n"+
					"		} "+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	switch(o) {\n" +
				"	       ^\n" +
				"An enhanced switch statement should be exhaustive; a default label expected\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 9)\n" +
				"	case List<String> s: \n" +
				"	     ^^^^^^^^^^^^^^\n" +
				"Type Object cannot be safely cast to List<String>\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 9)\n" +
				"	case List<String> s: \n" +
				"	     ^^^^^^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testBug573921_8() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n" +
					"public class X {\n"+
					" @SuppressWarnings(\"rawtypes\")\n" +
					" private static void foo(Object o) {\n"+
					"		switch(o.hashCode()) {\n"+
					"		case String s:\n"+
					"			break;\n"+
					"		default: \n"+
					"			break;\n"+
					"		} "+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case String s:\n" +
				"	     ^^^^^^^^\n" +
				"Type mismatch: cannot convert from int to String\n" +
				"----------\n");
	}
	public void testBug573921_9() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n" +
					"public class X {\n"+
					" @SuppressWarnings(\"rawtypes\")\n" +
					" private static void foo(Object o) {\n"+
					"		switch(o) {\n"+
					"		case Object o1:\n"+
					"			break;\n"+
					"		default: \n"+
					"			break;\n"+
					"		} "+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	default: \n" +
				"	^^^^^^^\n" +
				"Switch case cannot have both unconditional pattern and default label\n" +
				"----------\n");
	}
	public void testBug573921_10() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n" +
					"public class X {\n"+
					" @SuppressWarnings(\"rawtypes\")\n" +
					" private static void foo(List<String> o) {\n"+
					"		switch(o) {\n"+
					"		case List o1:\n"+
					"			break;\n"+
					"		default: \n"+
					"			break;\n"+
					"		} "+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	default: \n" +
				"	^^^^^^^\n" +
				"Switch case cannot have both unconditional pattern and default label\n" +
				"----------\n");
	}
	public void testBug573921_11() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n" +
					"public class X {\n"+
					" @SuppressWarnings(\"rawtypes\")\n" +
					" private static void foo(String s) {\n"+
					"		switch(s) {\n"+
					"		case CharSequence cs:\n"+
					"			break;\n"+
					"		default: \n"+
					"			break;\n"+
					"		} "+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	default: \n" +
				"	^^^^^^^\n" +
				"Switch case cannot have both unconditional pattern and default label\n" +
				"----------\n");
	}
	public void testBug575049_001() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I permits A,B,C {}\n"+
				"final class A implements I {}\n"+
				"final class B implements I {}\n"+
				"record C(int j) implements I {} // Implicitly final\n"+
				"public class X {\n"+
				" static int testSealedCoverage(I i) {\n"+
				"   return switch (i) {\n"+
				"   case A a -> 0;\n"+
				"   case B b -> 1;\n"+
				"   case C c -> 2; // No default required!\n"+
				"   default -> 3;\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   A a = new A();\n"+
				"   System.out.println(testSealedCoverage(a));\n"+
				" }\n"+
				"}",
			},
			"0");
	}
	public void testBug575049_002() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I permits A,B,C {}\n"+
				"final class A implements I {}\n"+
				"final class B implements I {}\n"+
				"record C(int j) implements I {} // Implicitly final\n"+
				"public class X {\n"+
				" static int testSealedCoverage(I i) {\n"+
				"   return switch (i) {\n"+
				"   case A a -> 0;\n"+
				"   case B b -> 1;\n"+
				"   case C c -> 2; // No default required!\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   A a = new A();\n"+
				"   System.out.println(testSealedCoverage(a));\n"+
				" }\n"+
				"}",
			},
			"0");
	}
	public void testBug575049_003() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I permits A,B,C {}\n"+
				"final class A implements I {}\n"+
				"final class B implements I {}\n"+
				"record C(int j) implements I {} // Implicitly final\n"+
				"public class X {\n"+
				" static int testSealedCoverage(I i) {\n"+
				"   return switch (i) {\n"+
				"   case A a -> 0;\n"+
				"   case B b -> 1;\n"+
				"   default -> 2; // No default required!\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   A a = new A();\n"+
				"   System.out.println(testSealedCoverage(a));\n"+
				" }\n"+
				"}",
			},
			"0");
	}
	public void testBug575049_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I permits A,B,C {}\n"+
				"final class A implements I {}\n"+
				"final class B implements I {}\n"+
				"record C(int j) implements I {} // Implicitly final\n"+
				"public class X {\n"+
				" static int testSealedCoverage(I i) {\n"+
				"   return switch (i) {\n"+
				"   case A a -> 0;\n"+
				"   case B b -> 1;\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   A a = new A();\n"+
				"   System.out.println(testSealedCoverage(a));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	return switch (i) {\n" +
			"	               ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	public void testBug575048_01() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static int foo(Integer i) {\n"+
				"   return switch (i) {\n"+
				"     default -> 2;\n"+
				"     case Integer i1 -> 0;\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(1));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Integer i1 -> 0;\n" +
			"	^^^^^^^^^^^^^^^\n" +
			"Switch case cannot have both unconditional pattern and default label\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case Integer i1 -> 0;\n" +
			"	     ^^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n");
	}
	public void testBug575053_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(String o) {\n" +
				"		switch (o) {\n" +
				"		  case String s when s.length() > 0  -> {}\n" +
				"		  default -> {}\n" +
				"		} \n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		try{\n" +
				"		  (new X()).foo(null);\n" +
				"		} catch(Exception e) {\n" +
				"		 	e.printStackTrace(System.out);\n" +
				"		}\n" +
				"	}\n"+
				"}",
			},
			"java.lang.NullPointerException\n" +
			"	at java.base/java.util.Objects.requireNonNull(Objects.java:233)\n" +
			"	at X.foo(X.java:3)\n" +
			"	at X.main(X.java:10)");
	}
	public void testBug575053_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(Object o) {\n" +
				"		switch (o) {\n" +
				"		  case String s -> {}\n" +
				"		  default -> {}\n" +
				"		} \n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		try{\n" +
				"		  (new X()).foo(null);\n" +
				"		} catch(Exception t) {\n" +
				"		 	t.printStackTrace();\n" +
				"		}\n" +
				"	}\n"+
				"}",
			},
			"",
			"java.lang.NullPointerException\n" +
			"	at java.base/java.util.Objects.requireNonNull(Objects.java:233)\n" +
			"	at X.foo(X.java:3)\n" +
			"	at X.main(X.java:10)");
	}
	public void testBug575249_01() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int foo(Object o) {\n" +
				"		return switch (o) {\n" +
				"		  case (String s) : yield 0;\n" +
				"		  default : yield 1;\n" +
				"		};\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(\"Hello\"));\n" +
				"	}\n"+
				"}",
			},
			"0");
	}
	public void _testBug575249_02() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int foo(Object o) {\n" +
				"		return switch (o) {\n" +
				"		  case (String s when s.length() < 10) : yield 0;\n" +
				"		  default : yield 1;\n" +
				"		};\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(\"Hello\"));\n" +
				"	}\n"+
				"}",
			},
			"0");
	}
	public void testBug575249_03() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int foo(Object o) {\n" +
				"		return switch (o) {\n" +
				"		  case (String s) -> 0;\n" +
				"		  default -> 1;\n" +
				"		};\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(\"Hello\"));\n" +
				"	}\n"+
				"}",
			},
			"0");
	}
	public void _testBug575249_04() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int foo(Object o) {\n" +
				"		return switch (o) {\n" +
				"		  case (String s && s.length() < 10) -> 0;\n" +
				"		  default -> 1;\n" +
				"		};\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(\"Hello\"));\n" +
				"	}\n"+
				"}",
			},
			"0");
	}
	public void testBug575241_01() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static int foo(Integer i) {\n"+
				"   return switch (i) {\n"+
				"     case Integer i1 -> 0;\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(1));\n"+
				" }\n"+
				"}",
			},
			"0");
	}
	public void testBug575241_02() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static int foo(Integer i) {\n"+
				"   return switch (i) {\n"+
				"     case Object o -> 0;\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(1));\n"+
				" }\n"+
				"}",
			},
			"0");
	}
	public void testBug575241_03() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static int foo(Object myVar) {\n"+
				"   return switch (myVar) {\n"+
				"     case null  -> 0;\n"+
				"     case Integer o -> 1;\n"+
				"     case Object obj ->2;\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(Integer.valueOf(0)));\n"+
				"   System.out.println(foo(null));\n"+
				" }\n"+
				"}",
			},
			"1\n" +
			"0");
	}
	public void testBug575241_04() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static int foo(Object myVar) {\n"+
				"   return switch (myVar) {\n"+
				"     case Integer o -> 1;\n"+
				"     case Object obj ->2;\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(Integer.valueOf(0)));\n"+
				"   try {\n"+
				"   foo(null);\n"+
				"   } catch (NullPointerException e) {\n"+
				"     System.out.println(\"NPE\");\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"1\n" +
			"NPE");
	}
	public void testBug575241_05() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo(Integer myVar) {\n"+
				"    switch (myVar) {\n"+
				"     case  null  -> System.out.println(100);\n"+
				"     case Integer o -> System.out.println(o);\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(Integer.valueOf(0));\n"+
				"   try {\n"+
				"   foo(null);\n"+
				"   } catch (NullPointerException e) {\n"+
				"     System.out.println(\"NPE\");\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"0\n" +
			"100");
	}
	public void testBug575241_06() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo(Integer myVar) {\n"+
				"    switch (myVar) {\n"+
				"     case Integer o -> System.out.println(o);\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(Integer.valueOf(0));\n"+
				"   try {\n"+
				"   foo(null);\n"+
				"   } catch (NullPointerException e) {\n"+
				"     System.out.println(\"NPE\");\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"0\n" +
			"NPE");
	}
	public void testBug575241_07() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo(String myVar) {\n"+
				"    switch (myVar) {\n"+
				"     case  null  -> System.out.println(100);\n"+
				"     case String o -> System.out.println(o);\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello\");\n"+
				"   foo(null);\n"+
				" }\n"+
				"}",
			},
			"Hello\n" +
			"100");
	}
	public void testBug575241_08() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo(String myVar) {\n"+
				"    switch (myVar) {\n"+
				"     case String o -> System.out.println(o);\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello\");\n"+
				"   try {\n"+
				"   foo(null);\n"+
				"   } catch (NullPointerException e) {\n"+
				"     System.out.println(\"NPE\");\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"Hello\n" +
			"NPE");
	}
	public void _testBug575356_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static void foo(Integer myVar) {\n"+
					"    switch (myVar) {\n"+
					"     case default -> System.out.println(\"hello\");\n"+
					"   };   \n"+
					" }   \n"+
					"\n"+
					" public static  void main(String[] args) {\n"+
					"   foo(Integer.valueOf(10)); \n"+
					" } \n"+
					"}",
				},
				"hello");
	}
	public void testBug575356_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static void foo(Integer myVar) {\n"+
					"    switch (myVar) {\n"+
					"     case null, default -> System.out.println(\"hello\");\n"+
					"   };   \n"+
					" }   \n"+
					"\n"+
					" public static  void main(String[] args) {\n"+
					"   foo(Integer.valueOf(10)); \n"+
					" } \n"+
					"}",
				},
				"hello");
	}
	public void _testBug575356_03() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static void foo(Integer myVar) {\n"+
					"    switch (myVar) {\n"+
					"     case default, null -> System.out.println(\"hello\");\n"+
					"   };   \n"+
					" }   \n"+
					"\n"+
					" public static  void main(String[] args) {\n"+
					"   foo(Integer.valueOf(10)); \n"+
					" } \n"+
					"}",
				},
				"hello");
	}
	public void testBug575356_04() {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"    case Integer i ->\n"+
					"      System.out.println(\"Integer:\"+ i );\n"+
					"    case null, default -> System.out.println(o.toString() );\n"+
					"   }\n"+
					"}\n"+
					"\n"+
					" public static  void main(String[] args) {\n"+
					"   foo(Integer.valueOf(10)); \n"+
					"   foo(new String(\"Hello\")); \n"+
					" } \n"+
					"}",
				},
				"Integer:10\n" +
				"Hello");
	}
	public void testBug575052_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"   case String s -> System.out.println(s);\n"+
				"   default -> System.out.println(0);\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello\");\n"+
				" }\n"+
				"}",
			},
			"Hello");
	}
	public void testBug575052_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"   	case String s -> System.out.println(s);\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello\");\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	switch (o) {\n" +
			"	        ^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n");
	}
	public void testBug575052_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static int foo(Object o) {\n"+
				"   switch (o) {\n"+
				"   	case null -> System.out.println(0);\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello\");\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	switch (o) {\n" +
			"	        ^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n");
	}
	public void testBug575052_004() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static int foo(int i) {\n"+
				"   switch (i) {\n"+
				"   case 1:\n"+
				"     break;\n"+
				"   }\n"+
				"   return 0;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo(0));\n"+
				" }\n"+
				"}",
			},
			"0");
	}
	public void testBug575050_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static int foo(Object o) {\n"+
				"   return switch (o) {\n"+
				"   	case String s -> 0;\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello\");\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	return switch (o) {\n" +
			"	               ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	public void testBug575050_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static int  foo(Object o) {\n"+
				"   return switch (o) {\n"+
				"   	case null -> 0;\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello\");\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	return switch (o) {\n" +
			"	               ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	// From 14.11.1.2 - null to be handled separately - no dominance here
	public void testBug575047_01() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static int foo(Integer i) {\n"+
					"   return switch (i) {\n"+
					"     case Integer i1 -> 0;\n"+
					"     case null -> 2;\n"+
					"   };\n"+
					"   Zork();\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	Zork();\n" +
				"	^^^^\n" +
				"The method Zork() is undefined for the type X\n" +
				"----------\n");
	}
	// A switch label that has a pattern case label element p dominates another
	// switch label that has a constant case label element c if either of the
	// following is true:
	//   * the type of c is a primitive type and its wrapper class (5.1.7) is a subtype of the erasure of the type of p.
    //   * the type of c is a reference type and is a subtype of the erasure of the type of p.
	public void testBug575047_02() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static int foo(Integer i) {\n"+
					"   return switch (i) {\n"+
					"     case Integer i1 -> i1;\n"+
					"     case 0 -> 0;\n"+
					"   };\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case 0 -> 0;\n" +
				"	     ^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testBug575047_03() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static void foo(Color c) {\n"+
					"   switch (c) {\n" +
					"			case Color c1 : \n" +
					"				break;\n" +
					"			case Blue :\n" +
					"				break;\n" +
					"		}\n"+
					" }\n"+
					"enum Color { Blue, Red; }\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case Blue :\n" +
				"	     ^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testBug575047_04() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static int foo(Integer i) {\n"+
					"   return switch (i) {\n"+
					"     case null -> 2;\n"+
					"     case Integer i1 -> 0;\n"+
					"   };\n"+
					" }\n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(foo(null));\n"+
					"   System.out.println(foo(Integer.valueOf(0)));\n"+
					" }\n"+
					"}",
				},
				"2\n" +
				"0");
	}
	public void testBug575047_05() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static void foo(float c) {\n"+
					"   switch (c) {\n" +
					"			case 0 : \n" +
					"				break;\n" +
					"			default :\n" +
					"		}\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case 0 : \n" +
				"	     ^\n" +
				"Type mismatch: cannot convert from int to float\n" +
				"----------\n");
	}
	public void testBug575047_06() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static int foo(String o) {\n"+
					"    return switch (o) {\n" +
					"		     case String s when s.length() > 0 -> 3;\n" +
					"		     case String s1 -> 1;\n" +
					"		     case String s -> -1;\n"+
					"		   };\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case String s -> -1;\n" +
				"	     ^^^^^^^^\n" +
				"The switch statement cannot have more than one unconditional pattern\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	case String s -> -1;\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	// Test that when a literal is used as case constant
	// we report type mismatch error against the literal's type and
	// not on other types the case statement may have resolved too
	public void testBug575047_07() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static void foo(Number i) {\n"+
					"	    switch (i) {\n"+
					"		 case Integer j, \"\":\n"+
					"			 System.out.println(0);\n"+
					"		 default:\n"+
					"	   }\n"+
					"	}\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case Integer j, \"\":\n" +
				"	                ^^\n" +
				"Type mismatch: cannot convert from String to Number\n" +
				"----------\n");
	}
	public void testBug575047_08() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static int foo(Integer i) {\n"+
					"   return switch (i) {\n"+
					"     case 0 -> 0;\n"+
					"     case Integer i1 -> i1;\n"+
					"   };\n"+
					" }\n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(foo(3));\n"+
					"   System.out.println(foo(0));\n"+
					" }\n"+
					"}",
				},
				"3\n"+
				"0");
	}
	public void testBug575047_09() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" 	static int foo(String i) {\n"+
					"		return switch (i) {\n"+
					"	     case \"\" -> 0;\n"+
					"	     case String s -> -1;\n"+
					"	   };\n"+
					"	}\n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(foo(\"\"));\n"+
					"   System.out.println(foo(\"abc\"));\n"+
					" }\n"+
					"}",
				},
				"0\n" +
				"-1");
	}
	public void testBug575047_10() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"	static String foo(Object o) {\n" +
					"	   return switch (o) {\n" +
					"		 case String i when i.length() == 0 -> \"empty\";\n" +
					"	     case String i when i.length() > 0 -> \"zero+\";\n" +
					"	     case Color s -> s.toString();\n" +
					"		 default -> \"unknown\";\n" +
					"	   };\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(foo(\"abc\"));\n" +
					"		System.out.println(foo(\"\"));\n" +
					"		System.out.println(Color.Blue);\n" +
					"		System.out.println(foo(args));\n" +
					"	}\n" +
					"} \n" +
					"enum Color {\n" +
					"	Blue, Red; \n" +
					"}",
				},
				"zero+\n" +
				"empty\n" +
				"Blue\n" +
				"unknown");
	}
	// Positive - Mix enum constants as well as suitable pattern var
	public void testBug575047_11() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"	static String foo(Color o) {\n" +
					"		return switch (o) {\n" +
					"	     case Red -> \"Const:Red\";\n" +
					"	     case Color s -> s.toString();\n" +
					"	   };\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(foo(Color.Red));\n" +
					"		System.out.println(foo(Color.Blue));\n" +
					"	}\n" +
					"} \n" +
					"enum Color {\n" +
					"	Blue, Red; \n" +
					"}",
				},
				"Const:Red\n" +
				"Blue");
	}
	public void testBug575047_12() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"	static String foo(Color o) {\n" +
					"		return switch (o) {\n" +
					"	     case Red -> \"Red\";\n" +
					"	     case Color s when s == Color.Blue  -> s.toString();\n" +
					"	     case Color s -> s.toString();\n" +
					"	   };\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(Color.Red);\n" +
					"		System.out.println(Color.Blue);\n" +
					"	}\n" +
					"} \n" +
					"enum Color {\n" +
					"	Blue, Red; \n" +
					"}",
				},
				"Red\n" +
				"Blue");
	}
	public void testBug575047_13() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"	static String foo(Color o) {\n" +
					"		return switch (o) {\n" +
					"	     case Color s when s == Color.Blue  -> s.toString();\n" +
					"	     case Red -> \"Red\";\n" +
					"	     case null -> \"\";\n" +
					"	   };\n" +
					"	}\n" +
					"} \n" +
					"enum Color {\n" +
					"	Blue, Red; \n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	return switch (o) {\n" +
				"	               ^\n" +
				"A Switch expression should cover all possible values\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	case Red -> \"Red\";\n" +
				"	     ^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testBug575047_14() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"	static String foo(Color o) {\n" +
					"		return switch (o) {\n" +
					"	     case Color s when s == Color.Blue  -> s.toString();\n" +
					"	     case Red -> \"Red\";\n" +
					"	   };\n" +
					"	}\n" +
					"} \n" +
					"enum Color {\n" +
					"	Blue, Red; \n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	return switch (o) {\n" +
				"	               ^\n" +
				"A Switch expression should cover all possible values\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	case Red -> \"Red\";\n" +
				"	     ^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testBug575047_15() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	static void foo(Integer o) {\n" +
					"		switch (o) {\n" +
					"		  case 1: break;\n" +
					"		  case Integer s when s == 2:\n" +
					"			  System.out.println(s);break;\n" +
					"		  case null, default:\n" +
					"			  System.out.println(\"null/default\");\n" +
					"		}\n" +
					"	}\n" +
					"	public static  void main(String[] args) {\n" +
					"		foo(null);\n" +
					"	}\n" +
					"}",
				},
				"null/default");
	}
	public void testBug575360_001() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static void foo(String myVar) { // String\n"+
					"    switch (myVar) {\n"+
					"     case null, default : System.out.println(\"hello\");\n"+
					"   };   \n"+
					" }\n"+
					" public static  void main(String[] args) { \n"+
					"   foo(new String(\"Hello\")); \n"+
					" }\n"+
					"}",
				},
				"hello");
	}
	public void testBug575055_001() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"	public int foo(CharSequence c) {\n" +
					"		return switch (c) {\n" +
					"		   case CharSequence c1 when (c instanceof String c1 && c1.length() > 0) -> 0;\n" +
					"		   default -> 0;\n" +
					"		};\n" +
					"	}" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case CharSequence c1 when (c instanceof String c1 && c1.length() > 0) -> 0;\n" +
				"	                                               ^^\n" +
				"Duplicate local variable c1\n" +
				"----------\n");
	}
	// Fails with Javac as it prints Javac instead of throwing NPE
	// https://bugs.openjdk.java.net/browse/JDK-8272776
	public void _testBug575051_1() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public void foo(Object o) {\n" +
					"	  try{\n" +
					"		switch (o) {\n" +
					"		  default:\n" +
					"			  break;\n" +
					"		  case String s :\n" +
					"			  System.out.println(s);\n" +
					"		} \n" +
					"	  } catch(Exception t) {\n" +
					"		 t.printStackTrace(System.out);\n" +
					"	  }\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		  (new X()).foo(null);\n" +
					"	}\n" +
					"}",
				},
				"java.lang.NullPointerException\n"
				+ "	at java.base/java.util.Objects.requireNonNull(Objects.java:233)\n"
				+ "	at X.foo(X.java:4)\n"
				+ "	at X.main(X.java:15)");
	}
	// Test we don't report any illegal fall-through to null case
	public void testBug575051_2() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public void foo(Object o) {\n" +
					"		switch (o) {\n" +
					"		  case String s :\n" +
					"			  System.out.println(s);\n" +
					"				//$FALL-THROUGH$\n" +
					"		  case null:\n" +
					"			  break;\n" +
					"		  default : \n" +
					"				  break;\n" +
					"		}\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		(new X()).foo(null);\n" +
					"	}\n" +
					"}",
				},
				"");
	}
	// Test we do report illegal fall-through to pattern
	public void testBug575051_3() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
								"	public void foo(Object o) {\n" +
								"		switch (o) {\n" +
								"		  default : \n" +
								"		  case String s :\n" +
								"			  System.out.println();\n" +
								"			  break;\n" +
								"		}\n" +
								"	}\n" +
								"	public static void main(String[] args) {\n" +
								"		  (new X()).foo(null);\n" +
								"	}\n" +
								"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case String s :\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n",
				"");
	}
	public void _testBug575571_1() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingDefaultCase, CompilerOptions.WARNING);
		runWarningTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public void foo(Color o) {\n" +
						"		switch (o) {\n" +
						"		  case Blue:\n" +
						"			break;\n" +
						"		}\n" +
						"	}\n" +
						"	public static void main(String[] args) {}\n" +
						"}\n" +
						"enum Color {	Blue;  }\n",
				},
				"----------\n" +
				"1. WARNING in X.java (at line 3)\n" +
				"	switch (o) {\n" +
				"	        ^\n" +
				"The switch over the enum type Color should have a default case\n" +
				"----------\n",
				options);
	}
	public void testBug575571_2() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public void foo(Color o) {\n" +
						"		switch (o) {\n" +
						"		  case Blue:\n" +
						"		  case Color c:\n" +
						"			break;\n" +
						"		}\n" +
						"	}\n" +
						"	public static void main(String[] args) {}\n" +
						"}\n" +
						"enum Color {	Blue, Red;  }\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case Color c:\n" +
				"	^^^^^^^^^^^^\n" +
				"Illegal fall-through to a pattern\n" +
				"----------\n");
	}
	public void testBug575714_01() {
		runNegativeTest(
				new String[] {
						"X.java",
						"class X {\n"+
						" static Object foo(Object o) {\n"+
						"   switch (o) {\n"+
						"       case Object __ -> throw new AssertionError(); \n"+
						"   }\n"+
						" }\n"+
						" public static void main(String[] args) {\n"+
						"   Zork();\n"+
						" }\n"+
						"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	Zork();\n" +
				"	^^^^\n" +
				"The method Zork() is undefined for the type X\n" +
				"----------\n");
	}
	public void testBug575714_02() {
		runNegativeTest(
				new String[] {
					"X.java",
					"class X {\n"+
					" static Object foo(Object o) {\n"+
					"   switch (o) {\n"+
					"       case Object __ -> System.out.println(\"Hello\"); \n"+
					"   }\n"+
					" }\n"+
					" public static void main(String[] args) {\n"+
					"   X.foo(new X());\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	static Object foo(Object o) {\n" +
				"	              ^^^^^^^^^^^^^\n" +
				"This method must return a result of type Object\n" +
				"----------\n");
	}
	public void testBug575714_03() {
		runConformTest(
				new String[] {
					"X.java",
					"class X {\n"+
					" static Object foo(Object o) {\n"+
					"   switch (o) {\n"+
					"       case Object __ -> System.out.println(\"Hello\"); \n"+
					"   }\n"+
					"   return null;\n"+
					" }\n"+
					" public static void main(String[] args) {\n"+
					"   X.foo(new X());\n"+
					" }\n"+
					"}",
				},
				"Hello");
	}
	public void testBug575714_04() {
		runConformTest(
				new String[] {
					"X.java",
					"class X {\n"+
					" static Object foo(Object o) throws Exception {\n"+
					"   switch (o) {\n"+
					"       case Object __ -> throw new Exception(); \n"+
					"   }\n"+
					" }\n"+
					" public static void main(String[] args) {\n"+
					"   try {\n"+
					"     X.foo(new X());\n"+
					"   } catch (Exception e) {\n"+
					"     System.out.println(\"Hello\");\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"Hello");
	}
	public void testBug575687_1() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static void number(Number i) {\n" +
						"		switch (i) {\n" +
						"			case Integer i2, 4.5:\n" +
						"			case 4.3: System.out.println();\n" +
						"			default: System.out.println(\"nothing\");\n" +
						"		}\n" +
						"	}\n" +
						"	public static void main(String[] args) {}\n" +
						"}\n" +
						"enum Color {	Blue, Red;  }\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case Integer i2, 4.5:\n" +
				"	                 ^^^\n" +
				"Type mismatch: cannot convert from double to Number\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	case 4.3: System.out.println();\n" +
				"	     ^^^\n" +
				"Type mismatch: cannot convert from double to Number\n" +
				"----------\n");
	}
	public void testBug575686_1() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	void m(Object o) {\n" +
						"		switch (o) {\n" +
						"			case Integer i1, String s1 ->\n" +
						"				System.out.print(s1);\n" +
						"			default -> System.out.print(\"default\");\n" +
						"			case Number n, null ->\n" +
						"				System.out.print(o);\n" +
						"			case null, Class c ->\n" +
						"				System.out.print(o);\n" +
						"		}\n" +
						"	}\n" +
						"	public static void main(String[] args) {}\n" +
						"}\n" +
						"enum Color {	Blue, Red;  }\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case Integer i1, String s1 ->\n" +
				"	                 ^^^^^^^^^\n" +
				"A switch label may not have more than one pattern case label element\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	case Number n, null ->\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	case Number n, null ->\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	case Number n, null ->\n" +
				"	               ^^^^\n" +
				"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	case Number n, null ->\n" +
				"	               ^^^^\n" +
				"Duplicate case\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	     ^^^^\n" +
				"Duplicate case\n" +
				"----------\n" +
				"7. WARNING in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	           ^^^^^\n" +
				"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	           ^^^^^^^\n" +
				"A null case label and patterns cannot co-exist in the same case label\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	           ^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n" +
				"10. ERROR in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	           ^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n" +
				"11. ERROR in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	           ^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n" +
				"12. ERROR in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	           ^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n" +
				"13. ERROR in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	           ^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testBug575737_001() {
		Map<String, String> options =getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);

		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo1(String o) {\n"+
				"   switch (o) {\n"+
				"   case null -> System.out.println(\"null\");\n"+
				"   case String s -> String.format(\"String %s\", s);\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	case null -> System.out.println(\"null\");\n" +
			"	     ^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	case String s -> String.format(\"String %s\", s);\n" +
			"	     ^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 9)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug575737_002() {
		Map<String, String> options =getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.INFO);

		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo1(String o) {\n"+
				"   switch (o) {\n"+
				"   case null -> System.out.println(\"null\");\n"+
				"   case String s -> String.format(\"String %s\", s);\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. INFO in X.java (at line 4)\n" +
			"	case null -> System.out.println(\"null\");\n" +
			"	     ^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. INFO in X.java (at line 5)\n" +
			"	case String s -> String.format(\"String %s\", s);\n" +
			"	     ^^^^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 9)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug575738_001() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	private static void foo(Object o) {\n" +
						"	   switch (o.hashCode()) {\n" +
						"	     case int t: System.out.println(\"Integer\"); \n" +
						"	     default : System.out.println(\"Object\"); \n" +
						"	   }\n" +
						"	}\n" +
						"	public static void main(String[] args) { \n" +
						"		foo(\"Hello World\");\n" +
						"	}\n" +
						"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case int t: System.out.println(\"Integer\"); \n" +
				"	     ^^^^^\n" +
				"Unexpected type int, expected class or array type\n" +
				"----------\n");
	}
	public void testBug575738_002() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	private static void foo(Object o) {\n" +
						"	   switch (o.hashCode()) {\n" +
						"	     case Integer t: System.out.println(\"Integer\"); \n" +
						"	     default : System.out.println(\"Object\"); \n" +
						"	   }\n" +
						"	}\n" +
						"	public static void main(String[] args) { \n" +
						"		foo(\"Hello World\");\n" +
						"	}\n" +
						"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case Integer t: System.out.println(\"Integer\"); \n" +
				"	     ^^^^^^^^^\n" +
				"Type mismatch: cannot convert from int to Integer\n" +
				"----------\n");
	}

	public void testBug576075_001() throws Exception {
		runConformTest(
			new String[] {
				"p/Rec.java",
				"package p;\n"+
				"import p.Rec.MyInterface.MyClass1;\n"+
				"import p.Rec.MyInterface.MyClass2;\n"+
				"public record Rec(MyInterface c) {\n"+
				"	public static sealed interface MyInterface permits MyClass1, MyClass2 {\n"+
				"		public static final class MyClass1 implements MyInterface { }\n"+
				"        public static final class MyClass2 implements MyInterface { }\n"+
				"    }\n"+
				"    public boolean bla() {\n"+
				"        return switch (c) {\n"+
				"            case MyClass1 mc1 -> true;\n"+
				"            case MyClass2 mc2 -> false;\n"+
				"        };\n"+
				"    }\n"+
				"    public static void main(String[] args) {\n"+
				"        new Rec(new MyClass1()).hashCode();\n"+
				"        System.out.println(\"works\");\n"+
				"    }\n"+
				"}\n"
			},
		 "works");
		String expectedOutput =
				"Bootstrap methods:\n" +
				"  0 : # 95 invokestatic java/lang/runtime/SwitchBootstraps.typeSwitch:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"	Method arguments:\n" +
				"		#32 p/Rec$MyInterface$MyClass1\n" +
				"		#34 p/Rec$MyInterface$MyClass2,\n" +
				"  1 : # 102 invokestatic java/lang/runtime/ObjectMethods.bootstrap:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;\n" +
				"	Method arguments:\n" +
				"		#1 p/Rec\n" +
				"		#103 c\n" +
				"		#104 REF_getField c:Lp/Rec$MyInterface;";
		SwitchPatternTest.verifyClassFile(expectedOutput, "p/Rec.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug576785_001() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface J<X> permits D, E {}\n"+
				"final class D implements J<String> {}\n"+
				"final class E<X> implements J<X> {}\n"+
				"\n"+
				"public class X {\n"+
				"       static int testExhaustive2(J<Integer> ji) {\n"+
				"               return switch (ji) { // Exhaustive!\n"+
				"               case E<Integer> e -> 42;\n"+
				"               };\n"+
				"       }\n"+
				"       public static void main(String[] args) {\n"+
				"               J<Integer> ji = new E<>();\n"+
				"               System.out.println(X.testExhaustive2(ji));\n"+
				"       }\n"+
				"}",
			},
			"42");
	}
	public void testBug576785_002() {
		runNegativeTest(
				new String[] {
				"X.java",
				"@SuppressWarnings(\"rawtypes\")\n" +
				"sealed interface J<T> permits D, E, F {}\n"+
				"final class D implements J<String> {}\n"+
				"final class E<T> implements J<T> {}\n"+
				"final class F<T> implements J<T> {}\n"+
				"\n"+
				"public class X {\n"+
				"@SuppressWarnings(\"preview\")\n" +
				" static int testExhaustive2(J<Integer> ji) {\n"+
				"   return switch (ji) { // Exhaustive!\n"+
				"   case E<Integer> e -> 42;\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   J<Integer> ji = new E<>();\n"+
				"   System.out.println(X.testExhaustive2(ji));\n"+
				"   Zork();\n"+
				" }\n"+
				"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	return switch (ji) { // Exhaustive!\n" +
				"	               ^^\n" +
				"A switch expression should have a default case\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 17)\n" +
				"	Zork();\n" +
				"	^^^^\n" +
				"The method Zork() is undefined for the type X\n" +
				"----------\n");
	}
	public void testBug576830_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo(Object o) {\n"+
				"   switch (o) {\n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello\");\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	switch (o) {\n" +
			"	        ^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n");
	}
	public void testBug578107_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed class C permits D {}\n"+
				"final class D extends C {}\n"+
				"public class X {\n"+
				"       @SuppressWarnings(\"preview\")\n"+
				"       static  void foo(C ji) {\n"+
				"                switch (ji) { // non-exhaustive\n"+
				"                  case D d : System.out.println(\"D\"); break;\n"+
				"               }; \n"+
				"       } \n"+
				"       public static void main(String[] args) {\n"+
				"               X.foo(new D());\n"+
				"               Zork();\n"+
				"       }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	switch (ji) { // non-exhaustive\n" +
			"	        ^^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 12)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug578107_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"abstract sealed class C permits D {}\n"+
				"final class D extends C {}\n"+
				"public class X {\n"+
				"       @SuppressWarnings(\"preview\")\n"+
				"       static  void foo(C ji) {\n"+
				"                switch (ji) { // non-exhaustive\n"+
				"                  case D d : System.out.println(\"D\"); break;\n"+
				"               }; \n"+
				"       } \n"+
				"       public static void main(String[] args) {\n"+
				"               X.foo(new D());\n"+
				"               Zork();\n"+
				"       }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug578107_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface C permits D {}\n"+
				"final class D implements C {}\n"+
				"public class X {\n"+
				"       @SuppressWarnings(\"preview\")\n"+
				"       static  void foo(C ji) {\n"+
				"                switch (ji) { // non-exhaustive\n"+
				"                  case D d : System.out.println(\"D\"); break;\n"+
				"               }; \n"+
				"       } \n"+
				"       public static void main(String[] args) {\n"+
				"               X.foo(new D());\n"+
				"               Zork();\n"+
				"       }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug578108_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed abstract class C permits D {}\n"+
				"final class D extends C {}\n"+
				"public class X {\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" static <T extends C> void foo(T  ji) {\n"+
				"    switch (ji) { // exhaustive because C is sealed and abstract\n"+
				"      case D d : System.out.println(\"D\"); break;\n"+
				"   }; \n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   X.foo(new D());\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug578108_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface C permits D {}\n"+
				"final class D implements C {}\n"+
				"public class X {\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" static <T extends C> void foo(T  ji) {\n"+
				"    switch (ji) { // exhaustive because C is sealed and abstract\n"+
				"      case D d : System.out.println(\"D\"); break;\n"+
				"   }; \n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   X.foo(new D());\n"+
				"   Zork();\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug578143_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" static  int foo(Object o) {\n"+
				"   return switch (o) { \n"+
				"      case X x when true -> 0;\n"+
				"      default -> 1;\n"+
				"   }; \n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo(new X()));\n"+
				"   System.out.println(X.foo(new Object()));\n"+
				" }\n"+
				"}",
			},
			"0\n" +
			"1");
	}
	public void testBug578143_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" public static void main(String[] args) {\n"+
				"     Boolean input = false;\n"+
				"     int result = switch(input) {\n"+
				"       case Boolean p when true -> 1;\n"+
				"     };\n"+
				"     System.out.println(result);\n"+
				" }\n"+
				"}",
			},
			"1");
	}
	public void testBug578402() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X  {\n"
					+ "enum Color{BLUE, RED, YELLOW;}\n"
					+ "    public static void run(Color c) {\n"
					+ "        switch(c) {\n"
					+ "                case BLUE -> {\n"
					+ "                    System.out.println(\"BLUE\");\n"
					+ "                }\n"
					+ "                case RED -> {\n"
					+ "                    System.out.println(\"RED\");\n"
					+ "                }\n"
					+ "                case Object o -> {\n"
					+ "                    System.out.println(o.toString());\n"
					+ "                }\n"
					+ "            }\n"
					+ "    }"
					+ "	public static void main(String[] args) {\n"
					+ "		run(Color.RED);\n"
					+ "		run(Color.BLUE);\n"
					+ "	}\n"
					+ "}"
				},
				"RED\n" +
				"BLUE");
	}
	public void testBug578402_2() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X  {\n"
					+ "static final String CONST = \"abc\";\n"
					+ "    public static void main(String args[]) {\n"
					+ "        System.out.println(run());\n"
					+ "    }\n"
					+ "    public static int run() {\n"
					+ "        String s = \"abc\";\n"
					+ "        int a = -1;\n"
					+ "        switch (s) {\n"
					+ "            case CONST -> {\n"
					+ "                a = 2;\n"
					+ "                break;\n"
					+ "            }\n"
					+ "            case null -> {\n"
					+ "                a = 0;\n"
					+ "                break; \n"
					+ "            }\n"
					+ "            default -> {\n"
					+ "            	a = 1;\n"
					+ "            }\n"
					+ "        }\n"
					+ "        return a;\n"
					+ "    }\n"
					+ "}",
				},
				"2");
	}
	// to be enabled after bug 578417 is fixed.
	public void testBug578402_3() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X  {\n"
					+ "static final String CONST = \"abc\";\n"
					+ "    public static void main(String args[]) {\n"
					+ "        System.out.println(run());\n"
					+ "    }\n"
					+ "    public static int run() {\n"
					+ "        String s = \"abc\";\n"
					+ "        int a = -1;\n"
					+ "        switch (s) {\n"
					+ "            case CONST -> {\n"
					+ "                a = 2;\n"
					+ "                break;\n"
					+ "            }\n"
					+ "            case String s1 -> {\n"
					+ "                a = 0;\n"
					+ "                break; \n"
					+ "            }\n"
					+ "        }\n"
					+ "        return a;\n"
					+ "    }\n"
					+ "}",
				},
				"2");
	}
	public void testBug578241_1() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "    public static void foo(Object obj, int x) {\n"
					+ "    	switch (obj) {\n"
					+ "    		case String s when (switch (x) {\n"
					+ "					case 1 -> { yield true; }\n"
					+ "					default -> { yield false; }\n"
					+ "   	 									})	\n"
					+ "   	 		 			-> {\n"
					+ "   	 		 				System.out.println(\"true\");\n"
					+ "   	 		 			}\n"
					+ "					\n"
					+ "   	 		 default -> {\n"
					+ "   	 			System.out.println(\"false\");\n"
					+ "   	 		 }\n"
					+ "    	}	\n"
					+ "    }\n"
					+ "    public static void main(String[] args) {\n"
					+ "		foo(\"abc\", 1);\n"
					+ "	}\n"
					+ "}",
				},
				"true");
	}
	private String getTestCaseForBug578504 (String caseConstant) {
		return "public class X {\n"
				+ "    public Object literal = \"a\";\n"
				+ "    @SuppressWarnings(\"preview\")\n"
				+ "	public boolean foo() {\n"
				+ "        String s = switch(literal) {\n"
				+ "            " + caseConstant
				+ "                yield \"a\";\n"
				+ "            }\n"
				+ "            case default -> { \n"
				+ "                yield \"b\";\n"
				+ "            }\n"
				+ "        }; \n"
				+ "        return s.equals(\"a\");\n"
				+ "    }\n"
				+ "    public static void main(String[] argv) {\n"
				+ "    	X c = new X();\n"
				+ "    	System.out.println(c.foo());\n"
				+ "    }\n"
				+ "}";
	}
	public void _testBug578504_1() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case ((String a && a.equals(\"a\")) && a != null)  -> { \n")
					,
				},
				"true");
	}
	public void _testBug578504_2() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case ((CharSequence a && a instanceof String ss && ss == null) && ss != null)  -> {\n"),
				},
				"false");
	}
	public void _testBug578504_3() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case (CharSequence a && (a instanceof String ss && ss != null) && ss != null)  -> {\n"),
				},
				"true");
	}
	public void _testBug578504_4() {
		runNegativeTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case ((CharSequence a && (a == null || a instanceof String ss)) && ss != null)  -> {\n"),
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case ((CharSequence a && (a == null || a instanceof String ss)) && ss != null)  -> {\n" +
				"	                                                                   ^^\n" +
				"ss cannot be resolved to a variable\n" +
				"----------\n");
	}
	public void _testBug578504_5() {
		runNegativeTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case ((CharSequence a && (a instanceof String ss || a == null)) && ss != null)  -> {\n"),
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case ((CharSequence a && (a instanceof String ss || a == null)) && ss != null)  -> {\n" +
				"	                                                                   ^^\n" +
				"ss cannot be resolved to a variable\n" +
				"----------\n");
	}
	public void _testBug578504_6() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case ((CharSequence a && (a instanceof String ss && a instanceof String sss)) && ss == sss)  -> {\n"),
				},
				"true");
	}
	public void _testBug578504_7() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case ((CharSequence a && (a instanceof String ss && a instanceof String sss)) && ss != sss)  -> {\n"),
				},
				"false");
	}
	public void testBug578553_1() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	@SuppressWarnings(\"preview\")\n"
					+ "	static Long foo(Number n) {\n"
					+ "		return switch (n) {\n"
					+ "	     case (Long l) when l.toString().equals(\"0\") -> {\n"
					+ "	    	 yield ++l;\n"
					+ "	     }\n"
					+ "		default -> throw new IllegalArgumentException();\n"
					+ "	   };\n"
					+ "	}\n"
					+ "	public static void main(String[] args) {\n"
					+ "		System.out.println(foo(0L));\n"
					+ "	}\n"
					+ "}",
				},
				"1");
	}
	public void testBug578553_2() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "		@SuppressWarnings(\"preview\")\n"
					+ "	static Long foo(Number n) {  \n"
					+ "		return switch (n) { \n"
					+ "	     case (Long l) when l.toString().equals(\"0\") -> {\n"
					+ "	    	 yield switch(l) {\n"
					+ "	    	 case Long l1 when l1.toString().equals(l1.toString()) -> {\n"
					+ "	    	 	yield ++l + ++l1;\n"
					+ "	    	 }\n"
					+ "			default -> throw new IllegalArgumentException(\"Unexpected value: \" + l);\n"
					+ "	    	 };\n"
					+ "	     }\n"
					+ "		default -> throw new IllegalArgumentException();\n"
					+ "	   };\n"
					+ "	}\n"
					+ "	public static void main(String[] args) {\n"
					+ "		System.out.println(foo(0L));\n"
					+ "	}\n"
					+ "}",
				},
				"2");
	}
	public void testBug578553_3() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	@SuppressWarnings(\"preview\")\n"
					+ "	static Long foo(Number n) {  \n"
					+ "		return switch (n) { \n"
					+ "	     case (Long l) when l.toString().equals(\"0\") -> {\n"
					+ "	    	 yield switch(l) {\n"
					+ "	    	 case Long l1 when l.toString().equals(l1.toString()) -> {\n"
					+ "	    	 	yield ++l + ++l1;\n"
					+ "	    	 }\n"
					+ "			default -> throw new IllegalArgumentException(\"Unexpected value: \" + l);\n"
					+ "	    	 };\n"
					+ "	     }\n"
					+ "		default -> throw new IllegalArgumentException();\n"
					+ "	   };\n"
					+ "	}\n"
					+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	case Long l1 when l.toString().equals(l1.toString()) -> {\n" +
				"	                  ^\n" +
				"Local variable l referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	public void testBug578553_4() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	@SuppressWarnings(\"preview\")\n"
					+ "	static Long foo(Number n) {  \n"
					+ "	int i = 0;\n"
					+ "	return switch(n) {\n"
					+ "	  case Long l when (1 == switch(l) {\n"
					+ "		//case \n"
					+ "			default -> {  \n"
					+ "				yield (i++);\n"
					+ "			} \n"
					+ "		}) -> 1L; \n"
					+ "	  default -> throw new IllegalArgumentException(\"Unexpected value: \" + n);\n"
					+ "	  };\n"
					+ "	}\n"
					+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	yield (i++);\n" +
				"	       ^\n" +
				"Local variable i referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	public void testBug578553_5() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	@SuppressWarnings(\"preview\")\n"
					+ "	static Long foo(Number n) {  \n"
					+ "	int i = 0;\n"
					+ "	return switch(n) {\n"
					+ "	  case Long l when (1 == switch(l) {\n"
					+ "		//case \n"
					+ "			default -> {  \n"
					+ "				yield ++i;\n"
					+ "			} \n"
					+ "		}) -> 1L; \n"
					+ "	  default -> throw new IllegalArgumentException(\"Unexpected value: \" + n);\n"
					+ "	  };\n"
					+ "	}\n"
					+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	yield ++i;\n" +
				"	        ^\n" +
				"Local variable i referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	public void testBug578553_6() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	@SuppressWarnings(\"preview\")\n"
					+ "	static Long foo(Number n) {  \n"
					+ "	int i = 0;\n"
					+ "	return switch(n) {\n"
					+ "	  case Long l when (1 == switch(l) {\n"
					+ "		//case \n"
					+ "			default -> {  \n"
					+ "				yield (i=i+1);\n"
					+ "			} \n"
					+ "		}) -> 1L; \n"
					+ "	  default -> throw new IllegalArgumentException(\"Unexpected value: \" + n);\n"
					+ "	  };\n"
					+ "	}\n"
					+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	yield (i=i+1);\n" +
				"	       ^\n" +
				"Local variable i referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	public void testBug578553_7() {
		runNegativeTest(
				false /*skipJavac */,
				JavacTestOptions.JavacHasABug.JavacBug8299416,
				new String[] {
					"X.java",
					"public class X {\n"
					+ " static int bar() { return 1; }\n"
					+ "	@SuppressWarnings(\"preview\")\n"
					+ "	static Long foo(Number n) {  \n"
					+ "	int i = 0;\n"
					+ "	return switch(n) {\n"
					+ "	  case Long l when (1 == switch(l) {\n"
					+ "		//case \n"
					+ "			default -> {  \n"
					+ "				yield (i = bar());\n"
					+ "			} \n"
					+ "		}) -> 1L; \n"
					+ "	  default -> throw new IllegalArgumentException(\"Unexpected value: \" + n);\n"
					+ "	  };\n"
					+ "	}\n"
					+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	yield (i = bar());\n" +
				"	       ^\n" +
				"Local variable i referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	public void testBug578568_1() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
								+ "    @SuppressWarnings(\"preview\")\n"
								+ "	public static int foo(Number arg0) {\n"
								+ "        int result = 0;\n"
								+ "        result = \n"
								+ "         switch (arg0) {\n"
								+ "            case Object p -> {\n"
								+ "                switch (arg0) {\n"
								+ "                     case Number p1 -> {\n"
								+ "                        yield 1;\n"
								+ "                    }\n"
								+ "                }\n"
								+ "            }\n"
								+ "        }; \n"
								+ "        return result;\n"
								+ "    }\n"
								+ " public static void main(String[] args) {\n"
								+ "    	System.out.println(foo(0L));\n"
								+ "	}"
								+ "}",
				},
				"1");
	}
	public void testBug578568_2() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
								+ " @SuppressWarnings(\"preview\")\n"
								+ "	public static int foo(Number arg0) {\n"
								+ "        return switch (arg0) {\n"
								+ "            case Object p : {\n"
								+ "                switch (arg0) {\n"
								+ "                     case Number p1 : {\n"
								+ "                        yield 1;\n"
								+ "                    }\n"
								+ "                }\n"
								+ "            }\n"
								+ "        }; \n"
								+ "    }\n"
								+ " public static void main(String[] args) {\n"
								+ "    	System.out.println(foo(0L));\n"
								+ "	}"
								+ "}",
				},
				"1");
	}
	public void testBug578568_3() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
								+ " @SuppressWarnings(\"preview\")\n"
								+ "	public static int foo(Object arg0) {\n"
								+ "        return switch (arg0) {\n"
								+ "            case Object p : {\n"
								+ "                switch (arg0) {\n"
								+ "                    case Number p1 : {\n"
								+ "                        yield 1;\n"
								+ "                    }\n"
								+ "                    default: {\n"
								+ "                    }"
								+ "                }\n"
								+ "            }\n"
								+ "        }; \n"
								+ " }\n"
								+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 12)\n" +
				"	}\n" +
				"	^^\n" +
				"A switch labeled block in a switch expression should not complete normally\n" +
				"----------\n");
	}
	public void testBug578416() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "    public static int testMethod(I i) {\n"
				+ "       return switch (i) {\n"
				+ "            case I p1 when (p1 instanceof C p2) : {\n"
				+ "                yield p2.value(); // Error here\n"
				+ "            }\n"
				+ "            case I p3 : {\n"
				+ "                yield p3.value(); // No error here\n"
				+ "            }\n"
				+ "        };\n"
				+ "    }\n"
				+ "    interface I {\n"
				+ "        public int value();\n"
				+ "    }\n"
				+ "    class C implements I {\n"
				+ "    	@Override\n"
				+ "    	public int value() {\n"
				+ "    		return 0;\n"
				+ "    	}\n"
				+ "    }\n"
				+ "    public static void main(String[] args) {\n"
				+ "    	I i = new I() {\n"
				+ "    		public int value() {\n"
				+ "    			return 10;\n"
				+ "    		} \n"
				+ "    	}; \n"
				+ "    	System.out.println(testMethod(i));\n"
				+ "    	System.out.println(testMethod(new X().new C()));\n"
				+ "	}"
				+ "}\n"},
				"10\n" +
				"0");
	}
	public void testBug578416_1() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "    public static int testMethod(I i) {\n"
				+ "       return switch (i) {\n"
				+ "            case I p1 when (p1 instanceof C p2) : {\n"
				+ "                yield p2.value();\n"
				+ "            }\n"
				+ "            case I p3 : {\n"
				+ "                yield p3.value();\n"
				+ "            }\n"
				+ "        };\n"
				+ "    }\n"
				+ "    interface I {\n"
				+ "        public int value();\n"
				+ "    }\n"
				+ "    class C implements I {\n"
				+ "    	@Override\n"
				+ "    	public int value() {\n"
				+ "    		return 0;\n"
				+ "    	}\n"
				+ "    }\n"
				+ "    public static void main(String[] args) {\n"
				+ "    	I i = new I() {\n"
				+ "    		public int value() {\n"
				+ "    			return 10;\n"
				+ "    		} \n"
				+ "    	}; \n"
				+ "    	System.out.println(testMethod(i));\n"
				+ "    	System.out.println(testMethod(new X().new C()));\n"
				+ "	}"
				+ "}\n"},
				"10\n" +
				"0");
	}
	public void testBug578416_2() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "    public static int foo(Object o) {\n"
				+ "       return switch (o) {\n"
				+ "            case Number n when (n instanceof Integer i) : {\n"
				+ "                yield n.intValue() + i; // Error here\n"
				+ "            }\n"
				+ "            case Number n2 : {\n"
				+ "                yield n2.intValue();\n"
				+ "            }\n"
				+ "            default : {\n"
				+ "                yield -1;\n"
				+ "            }\n"
				+ "        };\n"
				+ "    }\n"
				+ "    public static void main(String[] args) {\n"
				+ "    	System.out.println(foo(new Integer(10)));\n"
				+ "    	System.out.println(foo(new Integer(5)));\n"
				+ "    	System.out.println(foo(new Long(5L)));\n"
				+ "    	System.out.println(foo(new Float(0)));\n"
				+ "	}"
				+ "}\n"},
				"20\n" +
				"10\n" +
				"5\n" +
				"0");
	}
	public void testBug578416_3() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "    public static int foo(Object o) {\n"
				+ "       return switch (o) {\n"
				+ "            case Number n when (n instanceof Integer i && i.equals(10)) : {\n"
				+ "                yield n.intValue() + i; // Error here\n"
				+ "            }\n"
				+ "            case Number n2 : {\n"
				+ "                yield n2.intValue();\n"
				+ "            }\n"
				+ "            default : {\n"
				+ "                yield -1;\n"
				+ "            }\n"
				+ "        };\n"
				+ "    }\n"
				+ "    public static void main(String[] args) {\n"
				+ "    	System.out.println(foo(new Integer(10)));\n"
				+ "    	System.out.println(foo(new Integer(5)));\n"
				+ "    	System.out.println(foo(new Long(5L)));\n"
				+ "    	System.out.println(foo(new Float(0)));\n"
				+ "	}"
				+ "}\n"},
				"20\n" +
				"5\n" +
				"5\n" +
				"0");
	}
	public void testBug578635_1() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "   @SuppressWarnings({ \"preview\", \"rawtypes\" })\n"
				+ "	public static boolean foo(Integer n) {\n"
				+ "    	return switch (n) {\n"
				+ "	    	case Integer x when x.equals(10) -> {\n"
				+ "	    		yield true;\n"
				+ "	    	}\n"
				+ "	    	case Comparable y -> {\n"
				+ "	    		yield false;\n"
				+ "	    	}\n"
				+ "    	};\n"
				+ "    }\n"
				+ "    public static void main(String[] argv) {\n"
				+ "    	System.out.println(foo(Integer.valueOf(0)));\n"
				+ "    	System.out.println(foo(Integer.valueOf(10)));\n"
				+ "    }\n"
				+ "}"},
				"false\n" +
				"true");
	}
	public void testBug578635_2() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ " @SuppressWarnings({ \"preview\", \"rawtypes\" })\n"
				+ "	public static boolean foo(Integer n) {\n"
				+ "    	return switch (n) {\n"
				+ "	    	case Integer x when x.equals(10) -> {\n"
				+ "	    		yield true;\n"
				+ "	    	}\n"
				+ "	    	case Comparable y -> {\n"
				+ "	    		yield false;\n"
				+ "	    	}\n"
				+ "	    	default -> {\n"
				+ "	    		yield false;\n"
				+ "	    	}\n"
				+ "    	};\n"
				+ "    }\n"
				+ "    public static void main(String[] argv) {\n"
				+ "    	System.out.println(foo(Integer.valueOf(0)));\n"
				+ "    	System.out.println(foo(Integer.valueOf(10)));\n"
				+ "    }\n"
				+ "}"},
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	default -> {\n" +
				"	^^^^^^^\n" +
				"Switch case cannot have both unconditional pattern and default label\n" +
				"----------\n");
	}
	public void testBug578635_3() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ " @SuppressWarnings({ \"preview\", \"rawtypes\" })\n"
				+ "	public static boolean foo(Integer n) {\n"
				+ "    	return switch (n) {\n"
				+ "	    	case Integer x when x.equals(10) -> {\n"
				+ "	    		yield true;\n"
				+ "	    	}\n"
				+ "	    	case Comparable y -> {\n"
				+ "	    		yield false;\n"
				+ "	    	}\n"
				+ "	    	default -> {\n"
				+ "	    		yield false;\n"
				+ "	    	}\n"
				+ "    	};\n"
				+ "    }\n"
				+ "    public static void main(String[] argv) {\n"
				+ "    	System.out.println(foo(Integer.valueOf(0)));\n"
				+ "    	System.out.println(foo(Integer.valueOf(10)));\n"
				+ "    }\n"
				+ "}"},
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	default -> {\n" +
				"	^^^^^^^\n" +
				"Switch case cannot have both unconditional pattern and default label\n" +
				"----------\n");
	}
	public void testBug578417_1() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "   @SuppressWarnings({ \"preview\", \"rawtypes\" })\n"
				+ "    static final String CONSTANT = \"abc\";\n"
				+ "    static String CON2 = \"abc\";\n"
				+ "    public static int foo() {\n"
				+ "        int res = 0;\n"
				+ "        switch (CON2) {\n"
				+ "            case CONSTANT -> {\n"
				+ "                res = 1;\n"
				+ "                break;\n"
				+ "            }\n"
				+ "            case String s -> {\n"
				+ "                res = 2;\n"
				+ "                break;\n"
				+ "            }\n"
				+ "        }\n"
				+ "        return res;\n"
				+ "    }\n"
				+ "    public static void main(String argv[]) {\n"
				+ "    	System.out.println(foo()); \n"
				+ "    }\n"
				+ "}"},
				"1" );
	}
	public void testBug578132_001() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" @SuppressWarnings(\"preview\")\n"+
					" static  int foo(Object o, boolean b) {\n"+
					"   return switch (o) { \n"+
					"      case X x when b -> 0; // compilation error\n"+
					"      default -> 1;\n"+
					"   }; \n"+
					" } \n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(X.foo(new X(), true));\n"+
					"   System.out.println(X.foo(new Object(), true));\n"+
					" }\n"+
					"}"
				},
				"0\n"+
				"1");
	}
	public void test576788_1() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" @SuppressWarnings(\"preview\")\n"+
					"     public static void foo1(Object o) {\n"
					+ "    	boolean b = switch (o) {\n"
					+ "    		case String s -> {\n"
					+ "    			yield s == null;\n"
					+ "    		}\n"
					+ "    		case null -> {\n"
					+ "    			yield true;\n"
					+ "    		}\n"
					+ "    		default -> true;\n"
					+ "    	};\n"
					+ "    	System.out.println(b);\n"
					+ "    } \n"
					+ "    public static void main(String[] argv) {\n"
					+ "    	foo1(null);\n"
					+ "    	foo1(\"abc\");\n"
					+ "    }\n"+
					"}"
				},
				"true\n"+
				"false");
	}
	public void testBug577374_001() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    sealed interface A {}\n"+
					"    sealed interface B1 extends A {}\n"+
					"    sealed interface B2 extends A {}\n"+
					"    sealed interface C extends A {}\n"+
					"    final class D1 implements B1, C {}\n"+
					"    final class D2 implements B2, C {}\n"+
					"    \n"+
					"    public static int test(A arg) {\n"+
					"        return switch (arg) {\n"+
					"            case B1 b1 -> 1;\n"+
					"            case B2 b2 -> 2;\n"+
					"        };\n"+
					"    }\n"+
					"    public static void main(String[] args) {\n"+
					"   X.D1 d1 = new X().new D1();\n"+
					"   System.out.println(X.test(d1));\n"+
					" }\n"+
					"}"
				},
				"1");
	}
	public void testBug579355_001() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"       static void constantLabelMustAppearBeforePattern(Integer o) {\n"+
					"               switch (o) {\n"+
					"               case -1, 1 -> System.out.println(\"special case:\" + o);\n"+
					"               case Integer i when i > 0 -> System.out.println(\"positive integer: \" + o);\n"+
					"               case Integer i -> System.out.println(\"other integer: \" + o);\n"+
					"               }\n"+
					"       }\n"+
					"\n"+
					"       public static void main(String[] args) {\n"+
					"               X.constantLabelMustAppearBeforePattern(-10);\n"+
					"               X.constantLabelMustAppearBeforePattern(-1);\n"+
					"               X.constantLabelMustAppearBeforePattern(0);\n"+
					"               X.constantLabelMustAppearBeforePattern(1);\n"+
					"               X.constantLabelMustAppearBeforePattern(10);\n"+
					"       } \n"+
					"}"
				},
				"other integer: -10\n" +
				"special case:-1\n" +
				"other integer: 0\n" +
				"special case:1\n" +
				"positive integer: 10");
	}
	public void testBug579355_002() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"       static void constantLabelMustAppearBeforePattern(Integer o) {\n"+
					"               switch (o) {\n"+
					"               case -1, 1 -> System.out.println(\"special case:\" + o);\n"+
					"               case null -> System.out.println(\"null\");\n"+
					"               case Integer i when i > 0 -> System.out.println(\"positive integer: \" + o);\n"+
					"               case Integer i -> System.out.println(\"other integer: \" + o);\n"+
					"               }\n"+
					"       }\n"+
					"\n"+
					"       public static void main(String[] args) {\n"+
					"               X.constantLabelMustAppearBeforePattern(-10);\n"+
					"               X.constantLabelMustAppearBeforePattern(-1);\n"+
					"               X.constantLabelMustAppearBeforePattern(0);\n"+
					"               X.constantLabelMustAppearBeforePattern(1);\n"+
					"               X.constantLabelMustAppearBeforePattern(10);\n"+
					"               X.constantLabelMustAppearBeforePattern(null);\n"+
					"       } \n"+
					"}"
				},
				"other integer: -10\n" +
				"special case:-1\n" +
				"other integer: 0\n" +
				"special case:1\n" +
				"positive integer: 10\n"+
				"null");
	}
	public void testBug579355_003() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"           public int foo(Integer i) {\n"
					+ "        int result = 0;\n"
					+ "        switch (i) {\n"
					+ "            case 0 -> {\n"
					+ "                result = 3;\n"
					+ "                break;\n"
					+ "            }\n"
					+ "            case null -> {\n"
					+ "                result = 5;\n"
					+ "                break;\n"
					+ "            }\n"
					+ "            case (Integer p) -> {\n"
					+ "                result = 6;\n"
					+ "                break;\n"
					+ "            }\n"
					+ "            case 10 -> {\n"
					+ "                result = 9;\n"
					+ "                break;\n"
					+ "            }\n"
					+ "        }   \n"
					+ "        return result;\n"
					+ "    }\n"+
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 17)\n" +
				"	case 10 -> {\n" +
				"	     ^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testBug579355_004() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public static Color color = Color.BLUE;\n"
					+ "    public static void main(String args[]) {\n"
					+ "        Color c; \n"
					+ "        var result = switch(color){\n"
					+ "                case BLUE ->  (c = color) == Color.BLUE;\n"
					+ "                case RED, GREEN ->  (c = color) + \"text\";\n"
					+ "                case YELLOW ->  new String((c = color) + \"text\");\n"
					+ "                default ->  (c = color);\n"
					+ "                };\n"
					+ "        if (result != null && c == Color.BLUE) {\n"
					+ "        	System.out.println(\"Pass\");\n"
					+ "        } else {\n"
					+ "        	System.out.println(\"Fail\");\n"
					+ "        }\n"
					+ "    } \n"
					+ "}\n"
					+ "enum Color{BLUE, RED, GREEN, YELLOW;}"
				},
				"Pass");
	}
	public void testBug579355_005() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public int foo(Character c) {\n"
					+ "        int result = 0;\n"
					+ "        result = switch (c) {\n"
					+ "            case Character c1 -> 1;\n"
					+ "            case (short)1 -> 5;\n"
					+ "        };\n"
					+ "        return result;\n"
					+ "    }\n"
					+ "    public static void main(String args[]) {\n"
					+ "    	X x = new X();\n"
					+ "    	if (x.foo('\\u0001') == 1) {\n"
					+ "            System.out.println(\"Pass\");\n"
					+ "        } else {\n"
					+ "        	System.out.println(\"Fail\");\n"
					+ "        }\n"
					+ "    }\n"
					+ "}"
				},
				"Pass");
	}
	public void testIssue449_001() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"  public static void main(String[] args) {\n"+
					"    Object obj = null;\n"+
					"    var a = switch (obj) {\n"+
					"        case null -> 1;\n"+
					"        default   -> 2;\n"+
					"    };\n"+
					"    System.out.println(a);\n"+
					"  }\n" +
					"}"
				},
				"1");
	}
	public void testIssue554_001() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"  public static void main(String[] args) {\n"+
					"    String obj = null;\n"+
					"    var a = switch (obj) {\n"+
					"        case null -> 1;\n"+
					"        default   -> 2;\n"+
					"    };\n"+
					"    System.out.println(a);\n"+
					"  }\n" +
					"}"
				},
				"1");
	}
	public void testIssue_556_001() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" @SuppressWarnings(\"preview\")\n"+
					"     public static void foo1(Object o) {\n"
					+ "    	boolean b = switch (o) {\n"
					+ "    		case String s, null -> {\n"
					+ "    			yield s == null;\n"
					+ "    		}\n"
					+ "    		default -> true;\n"
					+ "    	};\n"
					+ "    	System.out.println(b);\n"
					+ "    } \n"
					+ "    public static void main(String[] argv) {\n"
					+ "    	foo1(null);\n"
					+ "    	foo1(\"abc\");\n"
					+ "    }\n"+
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case String s, null -> {\n" +
				"	               ^^^^\n" +
				"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
				"----------\n");
	}
	public void testIssue_556_002() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"    case default:\n"+
					"     System.out.println(\"Object: \" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	case default:\n" +
				"	     ^^^^^^^\n" +
				"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
				"----------\n");
	}
	public void testIssue_556_003() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(Integer.valueOf(11));\n"+
					"   foo(Integer.valueOf(9));\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"   case Integer i when i>10:\n"+
					"     System.out.println(\"Greater than 10:\" + o);\n"+
					"     break;\n"+
					"   case Integer j when j>0:\n"+
					"     System.out.println(\"Greater than 0:\" + o);\n"+
					"     break;\n"+
					"   case default:\n"+
					"     System.out.println(\"Give Me Some SunShine:\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 16)\n" +
				"	case default:\n" +
				"	     ^^^^^^^\n" +
				"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
				"----------\n");
	}
	public void testIssue_556_004() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String[] args) {\n"+
					"   foo(\"Hello World!\");\n"+
					" }\n"+
					"\n"+
					" private static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"   case Integer i :\n"+
					"     System.out.println(\"Integer:\" + o);\n"+
					"     break;\n"+
					"   case default:\n"+
					"     System.out.println(\"Object\" + o);\n"+
					"   case default:\n"+
					"     System.out.println(\"Give me Some Sunshine\" + o);\n"+
					"   }\n"+
					" }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	case default:\n" +
				"	     ^^^^^^^\n" +
				"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
				"----------\n" +
				"2. ERROR in X.java (at line 13)\n" +
				"	case default:\n" +
				"	^^^^^^^^^^^^\n" +
				"The default case is already defined\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 13)\n" +
				"	case default:\n" +
				"	     ^^^^^^^\n" +
				"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
				"----------\n");
	}
	public void testIssue_556_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static void foo(Object o) {\n"+
				"   switch (o.hashCode()) {\n"+
				"     case default : System.out.println(\"Default\");\n"+
				"     default : System.out.println(\"Object\");\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   foo(\"Hello World\");\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class Y {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case default : System.out.println(\"Default\");\n" +
			"	     ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	default : System.out.println(\"Object\");\n" +
			"	^^^^^^^\n" +
			"The default case is already defined\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testIssue_556_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0, default   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100 ));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case 0, default   : k = 1;\n" +
			"	        ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n");
	}
	public void testIssue_556_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo(Integer o) {\n"+
				"   int k = 0;\n"+
				"   switch (o) {\n"+
				"     case 0, default, 1   : k = 1;\n"+
				"   }\n"+
				"   return k;\n"+
				" } \n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(foo(100 ));\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case 0, default, 1   : k = 1;\n" +
			"	        ^^^^^^^\n" +
			"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
			"----------\n");
	}
	public void testIssue_556_008() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public void foo(Object o) {\n" +
					"	  try{\n" +
					"		switch (o) {\n" +
					"		  default:\n" +
					"			  break;\n" +
					"		  case String s :\n" +
					"			  System.out.println(10);\n" +
					"			  break;\n" +
					"		  case String s when (s.length() == 10):\n" +
					"			  System.out.println(s);\n" +
					"		} \n" +
					"	  } catch(Exception t) {\n" +
					"		 t.printStackTrace(System.out);\n" +
					"	  }\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		  (new X()).foo(null);\n" +
					"	}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	case String s :\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 10)\n" +
				"	case String s when (s.length() == 10):\n" +
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssue_556_009() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public void foo(Object o) {\n" +
					"	  try{\n" +
					"		switch (o) {\n" +
					"		  case null, default:\n" +
					"			  break;\n" +
					"		  case String s :\n" +
					"			  System.out.println(s);\n" +
					"		} \n" +
					"	  } catch(Exception t) {\n" +
					"		 t.printStackTrace(System.out);\n" +
					"	  }\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		  (new X()).foo(null);\n" +
					"	}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	case String s :\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssue658() {
		runConformTest(
				new String[] {
					"X.java",
					"@SuppressWarnings(\"preview\")\n"
					+ "public class X {\n"
					+ "	public static void main(String argv[]) {\n"
					+ "		(new X()).foo(\"abc\");\n"
					+ "	}\n"
					+ "	public void foo(String s) {\n"
					+ "		int v = 0;\n"
					+ "		Boolean b1 = Boolean.valueOf(true);\n"
					+ "		switch (s) {\n"
					+ "			case String obj when b1 -> v = 1;\n"
					+ "			default -> v = 0;\n"
					+ "		}\n"
					+ "		System.out.println(v);\n"
					+ "		Boolean b2 = Boolean.valueOf(false);\n"
					+ "		switch (s) {\n"
					+ "			case String obj when b2 -> v = 1;\n"
					+ "			default -> v = 0;\n"
					+ "		}\n"
					+ "		System.out.println(v);\n"
					+ "	}\n"
					+ "}"
				},
				"1\n0");
	}
	public void testIssue711_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n" +
					"public class X {\n" +
					"@SuppressWarnings(\"preview\")\n"
					+ "public static void foo(List<Number> l) {\n"
					+ "	switch (l) {\n"
					+ "	    case ArrayList<Number> al -> \n"
					+ "	        System.out.println(\"An ArrayList of Number\");\n"
					+ "	    case ArrayList<? extends Number> aln -> // Error - dominated case label\n"
					+ "	        System.out.println(\"An ArrayList of Number\");\n"
					+ "	    default -> \n"
					+ "	        System.out.println(\"A List\");\n"
					+ "	}\n"
					+ "}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	case ArrayList<? extends Number> aln -> // Error - dominated case label\n" +
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssue711_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n" +
					"public class X {\n" +
					"@SuppressWarnings(\"preview\")\n"
					+ "public static void foo(List<Number> l) {\n"
					+ "	switch (l) {\n"
					+ "	    case ArrayList<? extends Number> aln ->\n"
					+ "	        System.out.println(\"An ArrayList of Number\");\n"
					+ "	    case ArrayList<Number> al ->  // Error - dominated case label\n"
					+ "	        System.out.println(\"An ArrayList of Number\");\n"
					+ "	    default -> \n"
					+ "	        System.out.println(\"A List\");\n"
					+ "	}\n"
					+ "}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	case ArrayList<Number> al ->  // Error - dominated case label\n" +
				"	     ^^^^^^^^^^^^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssue742_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n" +
					"public class X {\n" +
					"@SuppressWarnings(\"preview\")\n"
					+ "public static void foo(Integer n) {\n"
					+ "  switch (n) {\n"
					+ "    case Integer i when true -> // Allowed but why write this?\n"
					+ "        System.out.println(\"An integer\"); \n"
					+ "    case Integer i ->                     // Error - dominated case label\n"
					+ "        System.out.println(\"An integer\"); \n"
					+ "  }\n"
					+ "}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	case Integer i ->                     // Error - dominated case label\n" +
				"	     ^^^^^^^^^\n" +
				"The switch statement cannot have more than one unconditional pattern\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	case Integer i ->                     // Error - dominated case label\n" +
				"	     ^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssue742_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n" +
					"public class X {\n" +
					"@SuppressWarnings(\"preview\")\n"
					+ "public static void foo(Integer n) {\n"
					+ "  switch (n) {\n"
					+ "    case Integer i -> // Allowed but why write this?\n"
					+ "        System.out.println(\"An integer\"); \n"
					+ "    case Integer i when true ->                     // Error - dominated case label\n"
					+ "        System.out.println(\"An integer\"); \n"
					+ "  }\n"
					+ "}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	case Integer i when true ->                     // Error - dominated case label\n" +
				"	     ^^^^^^^^^^^^^^^^^^^\n" +
				"The switch statement cannot have more than one unconditional pattern\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	case Integer i when true ->                     // Error - dominated case label\n" +
				"	     ^^^^^^^^^^^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssue712_001() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"       \n"+
					"       public static void main(String[] args) {\n"+
					"               Object o = \"Hello World\";\n"+
					"               foo(o);\n"+
					"       }\n"+
					"       @SuppressWarnings(\"preview\")\n"+
					"       public static void foo(Object o) {\n"+
					"         switch (o) {\n"+
					"           case String s:\n"+
					"               System.out.println(s);        // No break!\n"+
					"           case R():\n"+
					"               System.out.println(\"It's either an R or a string\"); // Allowed\n"+
					"               break;\n"+
					"           default:\n"+
					"         }\n"+
					"       }\n"+
					"\n"+
					"}\n"+
					"\n"+
					"record R() {} \n"+
					"record S() {}\n"
				},
				"Hello World\n" +
				"It\'s either an R or a string");
	}
	public void testIssue712_002() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" \n"+
					" public static void main(String[] args) {\n"+
					"   Object o = new R();\n"+
					"   foo(o);\n"+
					" }\n"+
					" @SuppressWarnings(\"preview\")\n"+
					" public static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case R():\n"+
					"     case S():                         // Multiple case labels!\n"+
					"         System.out.println(\"Either R or an S\");\n"+
					"         break;\n"+
					"     default:\n"+
					" }\n"+
					" }\n"+
					"\n"+
					"}\n"+
					"\n"+
					"record R() {}\n"+
					"record S() {}\n"
				},
				"Either R or an S");
	}
	public void testIssue712_003() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" \n"+
					" public static void main(String[] args) {\n"+
					"   Object o = null;\n"+
					"   foo(o);\n"+
					" }\n"+
					" @SuppressWarnings(\"preview\")\n"+
					" public static void foo(Object o) {\n"+
					"   switch (o) {\n"+
					"     case null:\n"+
					"     case R():                         // Multiple case labels!\n"+
					"         System.out.println(\"Either null or an R\");\n"+
					"         break;\n"+
					"     default:\n"+
					" }\n"+
					" }\n"+
					"}\n"+
					"\n"+
					"record R() {}\n"+
					"record S() {}"
				},
				"Either null or an R");
	}
	public void testIssue712_004() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					" \n" +
					" @SuppressWarnings(\"preview\")\n" +
					" public static void foo(Object o) {\n" +
					"   switch (o) {\n" +
					"     case Integer i :\n" +
					"     case R():                         // Multiple case labels!\n" +
					"         System.out.println(\"R Only\");\n" +
					"     default:\n" +
					"   }\n" +
					" }\n" +
					"}\n" +
					" \n" +
					"record R() {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case Integer i :\n" +
				"	^^^^^^^^^^^^^^\n" +
				"Illegal fall-through from a case label pattern\n" +
				"----------\n");
	}
	public void testIssueDefaultDominance_001() {
		runNegativeTest(
				new String[] {
					"X.java",
					" public class X {\n" +
					" \n" +
					" @SuppressWarnings(\"preview\")\n" +
					" public static void foo(Object o) {\n" +
					"   switch (o) {\n" +
					"   case Float f: System.out.println(\"integer\"); break;\n" +
					"   default: System.out.println(\"default\"); break;\n" +
					"   case Integer i: System.out.println(\"integer\"); break;\n" +
					"   }      \n" +
					" }\n" +
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	case Integer i: System.out.println(\"integer\"); break;\n" +
				"	     ^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssueDefaultDominance_002() {
		runNegativeTest(
				new String[] {
					"X.java",
					" public class X {\n" +
					" \n" +
					" @SuppressWarnings(\"preview\")\n" +
					" public static void foo(Object o) {\n" +
					"   switch (o) {\n" +
					"   default: System.out.println(\"default\"); break;\n" +
					"   case Integer i: System.out.println(\"integer\"); break;\n" +
					"   }      \n" +
					" }\n" +
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	case Integer i: System.out.println(\"integer\"); break;\n" +
				"	     ^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssueDefaultDominance_003() {
		runNegativeTest(
				new String[] {
					"X.java",
					" public class X {\n" +
					" \n" +
					" @SuppressWarnings(\"preview\")\n" +
					" public static void foo(Object o) {\n" +
					"   switch (o) {\n" +
					"   default: System.out.println(\"default\"); break;\n" +
					"   case null: System.out.println(\"null\"); break;\n" +
					"   }      \n" +
					" }\n" +
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	case null: System.out.println(\"null\"); break;\n" +
				"	     ^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssueDefaultDominance_004() {
		runNegativeTest(
				new String[] {
					"X.java",
					" public class X {\n" +
					" \n" +
					" @SuppressWarnings(\"preview\")\n" +
					" public static void foo(Object o) {\n" +
					"   switch (o) {\n" +
					"   case Float f: System.out.println(\"integer\"); break;\n" +
					"   default: System.out.println(\"default\"); break;\n" +
					"   case null: System.out.println(\"null\"); break;\n" +
					"   }      \n" +
					" }\n" +
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	case null: System.out.println(\"null\"); break;\n" +
				"	     ^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssue919() {
		runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n"
				+ " @SuppressWarnings(\"preview\")\n"
				+ "   static void defaultCanAppearBeforePattern(Integer i) {\n"
				+ "	  switch (i) {\n"
				+ "	  case null -> System.out.println(\"value unavailable: \" + i);\n"
				+ "	  case -1, 1 -> System.out.println(\"absolute value 1: \" + i);\n"
				+ "	  default -> System.out.println(\"other integer: \" + i);\n"
				+ "	  case Integer value when value > 0 -> System.out.println(\"positive integer: \" + i);\n"
				+ "	  }\n"
				+ "  }\n"
				+ "  static void defaultCanAppearBeforeNull(Integer i) {\n"
				+ "	  switch (i) {\n"
				+ "	  case -1, 1 -> System.out.println(\"absolute value 1: \" + i);\n"
				+ "	  default -> System.out.println(\"other integer: \" + i);\n"
				+ "	  case null -> System.out.println(\"value unavailable: \" + i);\n"
				+ "	  case Integer value when value > 0 -> System.out.println(\"positive integer: \" + i);\n"
				+ "	  }\n"
				+ "  }\n"
				+ "  static void defaultCanAppearBeforeConstantLabel(Integer i) {\n"
				+ "	  switch (i) {\n"
				+ "	  case null -> System.out.println(\"value unavailable: \" + i);\n"
				+ "	  default -> System.out.println(\"other integer: \" + i);\n"
				+ "	  case -1, 1 -> System.out.println(\"absolute value 1: \" + i);\n"
				+ "	  case Integer value when value > 0 -> System.out.println(\"positive integer: \" + i);\n"
				+ "	  }\n"
				+ "  }\n"
				+ "}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	case Integer value when value > 0 -> System.out.println(\"positive integer: \" + i);\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 15)\n" +
			"	case null -> System.out.println(\"value unavailable: \" + i);\n" +
			"	     ^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 16)\n" +
			"	case Integer value when value > 0 -> System.out.println(\"positive integer: \" + i);\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 24)\n" +
			"	case Integer value when value > 0 -> System.out.println(\"positive integer: \" + i);\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n");
	}
	public void testIssue1126a() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" 	static int foo(String i) {\n"+
					"		return switch (i) {\n"+
					"	     case \"abc\" -> 0;\n"+
					"	     case \"abcd\" -> 1;\n"+
					"	     case String s -> -1;\n"+
					"	   };\n"+
					"	}\n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(foo(\"abcd\"));\n"+
					"   System.out.println(foo(\"abc\"));\n"+
					" }\n"+
					"}",
				},
				"1\n" +
				"0");
	}
	public void testIssue1126b() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" 	static int foo(String i) {\n"+
					"		return switch (i) {\n"+
					"	     case \"FB\" -> 0;\n"+
					"	     case \"Ea\" -> 1;\n"+
					"	     case String s -> -1;\n"+
					"	   };\n"+
					"	}\n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(foo(\"Ea\"));\n"+
					"   System.out.println(foo(\"FB\"));\n"+
					" }\n"+
					"}",
				},
				"1\n" +
				"0");
	}
}
