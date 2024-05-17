/*******************************************************************************
 * Copyright (c) 2021, 2024 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SwitchPatternTest extends AbstractRegressionTest9 {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug575053_002"};
//		TESTS_NAMES = new String[] { "testBug575571_1"};
	}

	private static String previewLevel = "21";

	public static Class<?> testClass() {
		return SwitchPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_21);
	}
	public SwitchPatternTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
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
		runner.vmArguments = null;
		runner.customOptions = getCompilerOptions();
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
		runner.vmArguments = new String[] {};
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
			"	             ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s, X x : System.out.println(\"Integer, String or X\");\n" +
			"	                ^^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s, X x : System.out.println(\"Integer, String or X\");\n" +
			"	                       ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s, X x : System.out.println(\"Integer, String or X\");\n" +
			"	                          ^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s, X x : System.out.println(\"Integer, String or X\");\n" +
			"	                            ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 10)\n" +
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
			"	             ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n" +
			"	                ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n" +
			"	                       ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n" +
			"	                         ^^^^^^^^^^^^^^^^^\n" +
			"Syntax error on token(s), misplaced construct(s)\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n" +
			"	                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n" +
			"	                                              ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println(\"Integer, String or X\");\n" +
			"	                                                     ^\n" +
			"x cannot be resolved\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 10)\n" +
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
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String : System.out.println(\"Error should be flagged for String\");\n" +
			"	                ^^^^^^\n" +
			"String cannot be resolved to a variable\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
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
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case Integer t, String : System.out.println(\"Error should be flagged for Integer and String\");\n" +
			"	                ^^^^^^\n" +
			"String cannot be resolved to a variable\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 10)\n" +
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
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case String s, default : System.out.println(\"Error should be flagged for String and default\");\n" +
			"	               ^^^^^^^\n" +
			"A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default' \n" +
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
				"A pattern variable with the same name is already defined in the statement\n" +
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
				"A pattern variable with the same name is already defined in the statement\n" +
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
	public void testBug574719_001() {
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
			"Cannot mix pattern with other case labels\n" +
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
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
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
			"'var' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	         ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	            ^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	            ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	                ^\n" +
			"Named pattern variables are not allowed here\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	                   ^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	                   ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 7)\n" +
			"	case var i, var j, var k  -> System.out.println(0);\n" +
			"	                       ^\n" +
			"Named pattern variables are not allowed here\n" +
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
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case var i, 10  -> System.out.println(0);\n" +
			"	            ^^\n" +
			"Cannot mix pattern with other case labels\n" +
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
			"'var' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case var i, 10, var k  -> System.out.println(0);\n" +
			"	            ^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case var i, 10, var k  -> System.out.println(0);\n" +
			"	                ^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	case var i, 10, var k  -> System.out.println(0);\n" +
			"	                ^^^\n" +
			"'var' is not allowed here\n" +
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
			"	                ^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	case  10, null, var k  -> System.out.println(0);\n" +
			"	                ^^^\n" +
			"'var' is not allowed here\n" +
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
			"Cannot mix pattern with other case labels\n" +
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
			"Cannot mix pattern with other case labels\n" +
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
			"Cannot mix pattern with other case labels\n" +
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
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	               ^^^^^^^\n" +
			"A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default' \n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	                        ^^^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n"
);
	}
	public void testBug574564_010() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_20);
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
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	               ^^^^^^^\n" +
			"A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default' \n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	case String s, default, Integer i  -> System.out.println(0);\n" +
			"	                        ^^^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
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
			"	     ^^^^\n" +
			"Duplicate case\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case null, null  -> System.out.println(o);\n" +
			"	           ^^^^\n" +
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
			"Cannot mix pattern with other case labels\n" +
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
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case Integer i, null  -> System.out.println(0);\n" +
			"	                ^^^^\n" +
			"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
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
			"Cannot mix pattern with other case labels\n" +
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
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case Integer i when i > 10, null  -> System.out.println(0);\n" +
			"	                            ^^^^\n" +
			"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
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
				"	     ^^^^^^^^\n" +
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
				"		 	System.out.println(\"Null Pointer Exception Thrown\");\n" +
				"		}\n" +
				"	}\n"+
				"}",
			},
			"Null Pointer Exception Thrown");
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
				"		 	System.err.println(\"Null Pointer Exception Thrown\");\n" +
				"		}\n" +
				"	}\n"+
				"}",
			},
			"",
			"Null Pointer Exception Thrown");
	}
	public void testBug575249_01() {
		runNegativeTest(
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
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	case (String s) : yield 0;\n" +
			"	             ^\n" +
			"Syntax error on token \"s\", delete this token\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case (String s) : yield 0;\n" +
			"	                  ^^^^^\n" +
			"Syntax error on token \"yield\", AssignmentOperator expected after this token\n" +
			"----------\n");
	}
	public void testBug575249_02() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int foo(Object o) {\n" +
				"		return switch (o) {\n" +
				"		  case String s when s.length() < 10 : yield 0;\n" +
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
		runNegativeTest(
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
			"""
			----------
			1. ERROR in X.java (at line 4)
				case (String s) -> 0;
				^^^^
			Syntax error on token "case", ( expected after this token
			----------
			2. ERROR in X.java (at line 4)
				case (String s) -> 0;
				                   ^
			Syntax error, insert ":" to complete SwitchLabel
			----------
			"""
				);
	}
	public void testBug575249_04() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int foo(Object o) {\n" +
				"		return switch (o) {\n" +
				"		  case String s when s.length() < 10 -> 0;\n" +
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
	public void testBug575356_01() {
		this.runNegativeTest(
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
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case default -> System.out.println(\"hello\");\n" +
				"	     ^^^^^^^\n" +
				"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
				"----------\n");
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
	public void testBug575356_03() {
		this.runNegativeTest(
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
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case default, null -> System.out.println(\"hello\");\n" +
				"	     ^^^^^^^\n" +
				"A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\' \n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	case default, null -> System.out.println(\"hello\");\n" +
				"	              ^^^^\n" +
				"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
				"----------\n");
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
				"----------\n"
				+ "1. ERROR in X.java (at line 3)\n"
				+ "	switch (c) {\n"
				+ "	        ^\n"
				+ "Cannot switch on a value of type float. Only convertible int values, strings or enum variables are permitted\n"
				+ "----------\n");
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
				"Cannot mix pattern with other case labels\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
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
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n");
	}
	// Fails with Javac as it prints Javac instead of throwing NPE
	// https://bugs.openjdk.java.net/browse/JDK-8272776
	public void testBug575051_1() {
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
	public void testBug575571_1() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingDefaultCase, CompilerOptions.WARNING);
		runWarningTest(
		new String[] {
		"X.java",
		"public class X {\n" +
		"       public void foo(Color o) {\n" +
		"               switch (o) {\n" +
		"                 case Blue:\n" +
		"                       break;\n" +
		"               }\n" +
		"       }\n" +
		"       public static void main(String[] args) {}\n" +
		"}\n" +
		"enum Color {   Blue;  }\n",
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
				"Cannot mix pattern with other case labels\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	case Integer i2, 4.5:\n" +
				"	                 ^^^\n" +
				"Type mismatch: cannot convert from double to Number\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
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
				"	             ^^\n" +
				"Named pattern variables are not allowed here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	case Integer i1, String s1 ->\n" +
				"	                 ^^^^^^^^^\n" +
				"Cannot mix pattern with other case labels\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 4)\n" +
				"	case Integer i1, String s1 ->\n" +
				"	                        ^^\n" +
				"Named pattern variables are not allowed here\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 5)\n" +
				"	System.out.print(s1);\n" +
				"	                 ^^\n" +
				"s1 cannot be resolved to a variable\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	case Number n, null ->\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 7)\n" +
				"	case Number n, null ->\n" +
				"	               ^^^^\n" +
				"Cannot mix pattern with other case labels\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 7)\n" +
				"	case Number n, null ->\n" +
				"	               ^^^^\n" +
				"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 7)\n" +
				"	case Number n, null ->\n" +
				"	               ^^^^\n" +
				"Duplicate case\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	     ^^^^\n" +
				"Duplicate case\n" +
				"----------\n" +
				"10. ERROR in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	           ^^^^^^^\n" +
				"Cannot mix pattern with other case labels\n" +
				"----------\n" +
				"11. WARNING in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	           ^^^^^\n" +
				"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
				"----------\n" +
				"12. ERROR in X.java (at line 9)\n" +
				"	case null, Class c ->\n" +
				"	           ^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
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
				"1. ERROR in X.java (at line 9)\n" +
				"	return switch (ji) { // Exhaustive!\n" +
				"	               ^^\n" +
				"A switch expression should have a default case\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 16)\n" +
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
			"1. ERROR in X.java (at line 5)\n" +
			"	switch (ji) { // non-exhaustive\n" +
			"	        ^^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
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
			"1. ERROR in X.java (at line 11)\n" +
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
			"1. ERROR in X.java (at line 11)\n" +
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
			"1. ERROR in X.java (at line 11)\n" +
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
			"1. ERROR in X.java (at line 11)\n" +
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
				+ "	public boolean foo() {\n"
				+ "        String s = switch(literal) {\n"
				+ "            " + caseConstant
				+ "                yield \"a\";\n"
				+ "            }\n"
				+ "            default -> { \n"
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
	public void testBug578504_1() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case String a when (a.equals(\"a\") && a != null)  -> { \n")
					,
				},
				"true");
	}
	public void testBug578504_2() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case CharSequence a when (a instanceof String ss && (ss == null && ss != null))  -> {\n"),
				},
				"false");
	}
	public void testBug578504_3() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case CharSequence a when (a instanceof String ss && ss != null) && ss != null  -> {\n"),
				},
				"true");
	}
	public void testBug578504_6() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case CharSequence a when (a instanceof String ss && a instanceof String sss) && ss == sss  -> {\n"),
				},
				"true");
	}
	public void testBug578504_7() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case CharSequence a when (a instanceof String ss && a instanceof String sss) && ss != sss  -> {\n"),
				},
				"false");
	}
	public void testBug578553_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
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
				"""
				----------
				1. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					           ^
				Syntax error on token "l", delete this token
				----------
				2. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":: IdentifierOrNew" to complete ReferenceExpression
				----------
				3. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":" to complete SwitchLabel
				----------
				4. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					                                            ^^
				Syntax error on token "->", ; expected
				----------
				""");
	}
	public void testBug578553_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
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
				"""
				----------
				1. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					           ^
				Syntax error on token "l", delete this token
				----------
				2. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":: IdentifierOrNew" to complete ReferenceExpression
				----------
				3. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":" to complete SwitchLabel
				----------
				4. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					                                            ^^
				Syntax error on token "->", ; expected
				----------
				5. ERROR in X.java (at line 6)
					case Long l1 when l1.toString().equals(l1.toString()) -> {
					             ^^^^
				Syntax error on token "when", , expected
				----------
				""");
	}
	public void testBug578553_3() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
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
				"""
				----------
				1. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					           ^
				Syntax error on token "l", delete this token
				----------
				2. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":: IdentifierOrNew" to complete ReferenceExpression
				----------
				3. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":" to complete SwitchLabel
				----------
				4. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					                                            ^^
				Syntax error on token "->", ; expected
				----------
				5. ERROR in X.java (at line 6)
					case Long l1 when l.toString().equals(l1.toString()) -> {
					             ^^^^
				Syntax error on token "when", , expected
				----------
				""");
	}
	public void testBug578553_4() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
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
				"1. ERROR in X.java (at line 8)\n" +
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
				"1. ERROR in X.java (at line 8)\n" +
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
				"1. ERROR in X.java (at line 8)\n" +
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
				"1. ERROR in X.java (at line 9)\n" +
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
				"1. ERROR in X.java (at line 11)\n" +
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
				+ "   @SuppressWarnings({ \"rawtypes\" })\n"
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
				+ "   @SuppressWarnings({ \"rawtypes\" })\n"
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
					"     public static void foo1(String o) {\n"
					+ "    	boolean b = switch (o) {\n"
					+ "    		case \"abc\", null -> {\n"
					+ "    			yield false;\n"
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
				"1. ERROR in X.java (at line 4)\n" +
				"	case \"abc\", null -> {\n" +
				"	            ^^^^\n" +
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
				"	     ^^^^^^^^\n" +
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
					"public class X {\n"
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
					"import java.util.*;\n"
					+ "public class X {\n"
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
				"1. ERROR in X.java (at line 7)\n" +
				"	case ArrayList<? extends Number> aln -> // Error - dominated case label\n" +
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssue711_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n"
					+ "public class X {\n"
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
				"1. ERROR in X.java (at line 7)\n" +
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
					"public class X {\n"
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
				"1. ERROR in X.java (at line 7)\n" +
				"	case Integer i ->                     // Error - dominated case label\n" +
				"	     ^^^^^^^^^\n" +
				"The switch statement cannot have more than one unconditional pattern\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	case Integer i ->                     // Error - dominated case label\n" +
				"	     ^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssue742_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.*;\n"
					+ "public class X {\n"
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
				"1. ERROR in X.java (at line 7)\n" +
				"	case Integer i when true ->                     // Error - dominated case label\n" +
				"	     ^^^^^^^^^\n" +
				"The switch statement cannot have more than one unconditional pattern\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	case Integer i when true ->                     // Error - dominated case label\n" +
				"	     ^^^^^^^^^\n" +
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
				"1. ERROR in X.java (at line 5)\n" +
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
				"1. ERROR in X.java (at line 7)\n" +
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
					" public static void foo(Object o) {\n" +
					"   switch (o) {\n" +
					"   default: System.out.println(\"default\"); break;\n" +
					"   case Integer i: System.out.println(\"integer\"); break;\n" +
					"   }      \n" +
					" }\n" +
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
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
					" public static void foo(Object o) {\n" +
					"   switch (o) {\n" +
					"   default: System.out.println(\"default\"); break;\n" +
					"   case null: System.out.println(\"null\"); break;\n" +
					"   }      \n" +
					" }\n" +
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
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
				"1. ERROR in X.java (at line 7)\n" +
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
			"1. ERROR in X.java (at line 7)\n" +
			"	case Integer value when value > 0 -> System.out.println(\"positive integer: \" + i);\n" +
			"	     ^^^^^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 14)\n" +
			"	case null -> System.out.println(\"value unavailable: \" + i);\n" +
			"	     ^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 15)\n" +
			"	case Integer value when value > 0 -> System.out.println(\"positive integer: \" + i);\n" +
			"	     ^^^^^^^^^^^^^\n" +
			"This case label is dominated by one of the preceding case labels\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 23)\n" +
			"	case Integer value when value > 0 -> System.out.println(\"positive integer: \" + i);\n" +
			"	     ^^^^^^^^^^^^^\n" +
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
	public void testIssue587_001() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"  sealed interface I<T> permits A, B {}\n"+
					"  final static class A<T> implements I<String> {}\n"+
					"  final static class B<Y> implements I<Y> {}\n"+
					"\n"+
					"  static int testGenericSealedExhaustive(I<Integer> i) {\n"+
					"    return switch (i) {\n"+
					"      // Exhaustive as no A case possible!\n"+
					"      case B<Integer> bi -> 42;\n"+
					"    };\n"+
					"  }\n"+
					"  public static void main(String[] args) {\n"+
					"       System.out.println(testGenericSealedExhaustive(new B<Integer>()));\n"+
					"  }\n"+
					"}",
				},
				"42");
	}
	public void testIssueExhaustiveness_001() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" public static void main(String argv[]) {\n"+
					"   System.out.println(foo());\n"+
					" }\n"+
					"\n"+
					" public static int foo() {\n"+
					"   return switch (I.getIC()) {\n"+
					"     case IC c -> 42;\n"+
					"   };\n"+
					" }\n"+
					"}\n"+
					"\n"+
					"sealed interface I<T> permits IC {\n"+
					" public static I getIC() {\n"+
					"   return new IC(){};\n"+
					" }\n"+
					"}\n"+
					"\n"+
					"non-sealed interface IC<T> extends I {}",
				},
				"42");
	}
	public void testIssueExhaustiveness_002() {
		runConformTest(
				new String[] {
					"X.java",
					"record R(int i) {}\n"+
					"public class X {\n"+
					"\n"+
					"    public static int foo(R exp) {\n"+
					"        return switch (exp) {\n"+
					"            case R r -> 42;\n"+
					"        };\n"+
					"    }\n"+
					"    public static void main(String argv[]) {\n"+
					"       System.out.println(foo(new R(10)));\n"+
					"    }\n"+
					"}"
				},
				"42");
	}
	public void testIssueExhaustiveness_003() {
		runConformTest(
				new String[] {
					"X.java",
					"record R(X x) {}\n"+
					"public class X {\n"+
					"\n"+
					"    public static int foo(R exp) {\n"+
					"        return switch (exp) {\n"+
					"            case R(Object o) -> 42;\n"+
					"        };\n"+
					"    }\n"+
					"    public static void main(String argv[]) {\n"+
					"       System.out.println(foo(new R(new X())));\n"+
					"    }\n"+
					"}"
				},
				"42");
	}
	public void testIssueExhaustiveness_004() {
		runNegativeTest(
				new String[] {
					"X.java",
					"sealed interface I permits A, J {}\n"+
					"sealed interface J extends I {}\n"+
					"\n"+
					"final class A implements I {}\n"+
					"final record R() implements J {}\n"+
					"\n"+
					"public class X {\n"+
					"\n"+
					"    public static int foo(I i) {\n"+
					"        return switch (i) {\n"+
					"            case A a -> 0;\n"+
					"        };\n"+
					"    }\n"+
					"\n"+
					"    public static void main(String argv[]) {\n"+
					"       Zork();\n"+
					"    }\n"+
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	return switch (i) {\n" +
				"	               ^\n" +
				"A switch expression should have a default case\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 16)\n" +
				"	Zork();\n" +
				"	^^^^\n" +
				"The method Zork() is undefined for the type X\n" +
				"----------\n"
);
	}
	public void testIssueExhaustiveness_005() {
		runConformTest(
				new String[] {
					"X.java",
					"sealed interface I {}\n" +
					"final class A implements I {}\n" +
					"\n" +
					"record R<T extends I>(T x, T  y) {}\n" +
					"\n" +
					"public class X {\n" +
					"    public static int foo(R r) {\n" +
					"       return  switch (r) {\n" +
					"            case R(A a1, A a2) -> 0;\n" +
					"        };\n" +
					"    }\n" +
					"\n" +
					"    @SuppressWarnings(\"unchecked\")\n" +
					"       public static void main(String argv[]) {\n" +
					"       System.out.println(X.foo(new R(new A(), new A())));\n" +
					"    }\n" +
					"}"
				},
				"0");
	}
	public void testIssue1250_1() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"enum E {\n"
					+ "	A1, A2;\n"
					+ "}\n"
					+ "public class X {\n"
					+ "	public static void foo(E e) {\n"
					+ "		switch (e) {\n"
					+ "			case E.A1 -> {\n"
					+ "				System.out.println(\"A1\");\n"
					+ "			}\n"
					+ "			case E.A2 -> {\n"
					+ "				System.out.println(\"A2\");\n"
					+ "			}\n"
					+ "		}\n"
					+ "	}\n"
					+ "	public static void main(String[] args) {\n"
					+ "		foo(E.A1);\n"
					+ "	}\n"
					+ "}",
				},
				"A1");
	}
	public void testIssue1250_2() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"enum E {\n"
					+ "	A1, A2;\n"
					+ "	enum InnerE {\n"
					+ "		B1, B2;\n"
					+ "	}\n"
					+ "}\n"
					+ "public class X {\n"
					+ "	public static void foo(E.InnerE e) {\n"
					+ "		switch (e) {\n"
					+ "			case E.InnerE.B1 -> {\n"
					+ "				System.out.println(\"B1\"); //$NON-NLS-1$\n"
					+ "			} \n"
					+ "			default -> {}\n"
					+ "		}\n"
					+ "	}\n"
					+ "	public static void main(String[] args) { \n"
					+ "		foo(E.InnerE.B1);\n"
					+ "	}\n"
					+ "}",
				},
				"B1");
	}
	public void testIssue1250_3() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runNegativeTest(
				new String[] {
					"X.java",
					"enum E {\n"
					+ "	A1, A2;\n"
					+ "	enum InnerE {\n"
					+ "		B1, B2;\n"
					+ "	}\n"
					+ "}\n"
					+ "public class X {\n"
					+ "	public static void foo(E.InnerE e) {\n"
					+ "		switch (e) {\n"
					+ "			case E.A1 -> {\n"
					+ "				System.out.println(\"B1\"); //$NON-NLS-1$\n"
					+ "			} \n"
					+ "			default -> {}\n"
					+ "		}\n"
					+ "	}\n"
					+ "	public static void main(String[] args) { \n"
					+ "		foo(E.InnerE.B1);\n"
					+ "	}\n"
					+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	case E.A1 -> {\n" +
				"	     ^^^^\n" +
				"Type mismatch: cannot convert from E to E.InnerE\n" +
				"----------\n");
	}
	public void testIssue1250_4() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"interface I {}\n"
					+ "enum E implements I {\n"
					+ "	A0, A1, A2, A3, A4;\n"
					+ "}\n"
					+ "public class X {\n"
					+ "	public String testMethod(I exp) {\n"
					+ "		String res = \"\";\n"
					+ "		switch (exp) {\n"
					+ "			case E.A0 -> {\n"
					+ "				res = \"const A0\";\n"
					+ "				break;\n"
					+ "			}\n"
					+ "			case E.A1 -> {\n"
					+ "				res = \"const A1\";\n"
					+ "				break;\n"
					+ "			}\n"
					+ "			case E.A2 -> {\n"
					+ "				res = \"const A2\";\n"
					+ "				break;\n"
					+ "			}\n"
					+ "			case E.A3 -> {\n"
					+ "				res = \"const A3\";\n"
					+ "				break;\n"
					+ "			}\n"
					+ "			case E.A4 -> {\n"
					+ "				res = \"const A4\";\n"
					+ "				break;\n"
					+ "			}\n"
					+ "			default -> {\n"
					+ "				res = \"default\";\n"
					+ "			}\n"
					+ "		}\n"
					+ "		return res;\n"
					+ "	}\n"
					+ "	public static void main(String[] args) {\n"
					+ "		System.out.println((new X()).testMethod(E.A2));\n"
					+ "		System.out.println((new X()).testMethod(E.A3));\n"
					+ "		System.out.println((new X()).testMethod(E.A4));\n"
					+ "		System.out.println((new X()).testMethod(E.A0));\n"
					+ "		System.out.println((new X()).testMethod(E.A1));\n"
					+ "		System.out.println((new X()).testMethod(new I() {\n"
					+ "		}));\n"
					+ "	}\n"
					+ "}",
				},
				"const A2\n" +
				"const A3\n" +
				"const A4\n" +
				"const A0\n" +
				"const A1\n" +
				"default");
	}
	public void testIssue1250_5() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"interface I {\n"
					+ "}\n"
					+ "enum E implements I {\n"
					+ "	A0, A1, A2, A3, A4;\n"
					+ "}\n"
					+ "enum E1 implements I {\n"
					+ "	B0, B1;\n"
					+ "}\n"
					+ "public class X {\n"
					+ "	public String foo(I exp) {\n"
					+ "		String res = \"\";\n"
					+ "		switch (exp) {\n"
					+ "			case E.A0 -> {\n"
					+ "				res = \"const A0\";\n"
					+ "				break;\n"
					+ "			}\n"
					+ "			case E.A1 -> {\n"
					+ "				res = \"const A1\";\n"
					+ "				break;\n"
					+ "			}\n"
					+ "			case E1.B0 -> {\n"
					+ "				res = \"const B0\";\n"
					+ "				break;\n"
					+ "			}\n"
					+ "			case E e -> {\n"
					+ "				res = e.toString();\n"
					+ "			}\n"
					+ "			case E1 e1 -> {\n"
					+ "				res = e1.toString();\n"
					+ "			}\n"
					+ "			default -> {\n"
					+ "				res = \"default\";\n"
					+ "			}\n"
					+ "		}\n"
					+ "		return res;\n"
					+ "	}\n"
					+ "	public static void main(String[] args) {\n"
					+ "		System.out.println((new X()).foo(E.A0));\n"
					+ "		System.out.println((new X()).foo(E.A1));\n"
					+ "		System.out.println((new X()).foo(E.A2));\n"
					+ "		System.out.println((new X()).foo(E1.B0));\n"
					+ "		System.out.println((new X()).foo(E1.B1));\n"
					+ "		System.out.println((new X()).foo(new I() {\n"
					+ "		}));\n"
					+ "	}\n"
					+ "}",
				},
				"const A0\n"
				+ "const A1\n"
				+ "A2\n"
				+ "const B0\n"
				+ "B1\n"
				+ "default");
	}
	public void testIssue1250_6() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"interface I {\n"
					+ "}\n"
					+ "enum XEnum {\n"
					+ "    A, B;\n"
					+ "    interface I {}\n"
					+ "    enum E implements I {\n"
					+ "        A0, A1;\n"
					+ "    }\n"
					+ "}\n"
					+ "public class X {\n"
					+ "    public String foo(XEnum.I exp) {\n"
					+ "        String res = \"\";\n"
					+ "        switch (exp) {\n"
					+ "            case XEnum.E.A0 -> {\n"
					+ "                res = \"A0\";\n"
					+ "                break;\n"
					+ "            }\n"
					+ "            case XEnum.E.A1 -> {\n"
					+ "                res = \"A1\";\n"
					+ "                break;\n"
					+ "            }\n"
					+ "            default -> {\n"
					+ "                res = \"Ad\";\n"
					+ "            }\n"
					+ "        }\n"
					+ "        return res;\n"
					+ "    }\n"
					+ "	public static void main(String[] args) {\n"
					+ "		System.out.println((new X()).foo(XEnum.E.A1));\n"
					+ "	}\n"
					+ "}",
				},
				"A1");
	}
	public void testIssue1250_7() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"interface I {\n"
					+ "    interface InnerI {}\n"
					+ "    enum E implements InnerI {\n"
					+ "        A0, A1;\n"
					+ "    }\n"
					+ "}\n"
					+ "public class X {\n"
					+ "    public String foo(I.InnerI exp) {\n"
					+ "        String res = \"\";\n"
					+ "        res = switch (exp) {\n"
					+ "            case I.E.A0 -> {\n"
					+ "                yield \"A0\";\n"
					+ "            }\n"
					+ "            case I.E.A1 -> {\n"
					+ "                yield \"A1\";\n"
					+ "            }\n"
					+ "            default -> {\n"
					+ "                yield \"Ad\";\n"
					+ "            }\n"
					+ "        };\n"
					+ "        return res;\n"
					+ "    }\n"
					+ "	public static void main(String[] args) {\n"
					+ "		System.out.println((new X()).foo(I.E.A1));\n"
					+ "	}\n"
					+ "}",
				},
				"A1");
	}
	public void testIssue1250_8() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"p/q/X.java",
					"package p.q;\n"
					+ "interface I {\n"
					+ "}\n"
					+ "enum E implements I {\n"
					+ "	A0, A1, A2, A3, A4;\n"
					+ "}\n"
					+ "enum E1 implements I {\n"
					+ "	B0, B1;\n"
					+ "}\n"
					+ "public class X {\n"
					+ "	public String foo(I exp) {\n"
					+ "		String res = \"\";\n"
					+ "		switch (exp) {\n"
					+ "			case E.A0 -> {\n"
					+ "				res = \"const A0\";\n"
					+ "				break;\n"
					+ "			}\n"
					+ "			case E.A1 -> {\n"
					+ "				res = \"const A1\";\n"
					+ "				break;\n"
					+ "			}\n"
					+ "			case E1.B0 -> {\n"
					+ "				res = \"const B0\";\n"
					+ "				break;\n"
					+ "			}\n"
					+ "			case E e -> {\n"
					+ "				res = e.toString();\n"
					+ "			}\n"
					+ "			case E1 e1 -> {\n"
					+ "				res = e1.toString();\n"
					+ "			}\n"
					+ "			default -> {\n"
					+ "				res = \"default\";\n"
					+ "			}\n"
					+ "		}\n"
					+ "		return res;\n"
					+ "	}\n"
					+ "	public static void main(String[] args) {\n"
					+ "		System.out.println((new X()).foo(E.A0));\n"
					+ "		System.out.println((new X()).foo(E.A1));\n"
					+ "		System.out.println((new X()).foo(E.A2));\n"
					+ "		System.out.println((new X()).foo(E1.B0));\n"
					+ "		System.out.println((new X()).foo(E1.B1));\n"
					+ "		System.out.println((new X()).foo(new I() {\n"
					+ "		}));\n"
					+ "	}\n"
					+ "}",
				},
				"const A0\n"
				+ "const A1\n"
				+ "A2\n"
				+ "const B0\n"
				+ "B1\n"
				+ "default");
	}
	public void testIssue1351_1() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
								+ "	public static void foo() {\n"
								+ "		Object o = new String(\"\");\n"
								+ "		int len = 2;\n"
								+ "		switch (o) {\n"
								+ "		case String o1 when ((String) o).length() == o1.length() :\n"
								+ "			o = null;\n"
								+ "			o1 = null;\n"
								+ "			break;\n"
								+ "		default:\n"
								+ "			break;\n"
								+ "		}\n"
								+ "	}\n"
								+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case String o1 when ((String) o).length() == o1.length() :\n" +
				"	                              ^\n" +
				"Local variable o referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	public void testIssue1351_2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	public static void foo() {\n"
						+ "		Object o = new String(\"\");\n"
						+ "		int len = 2;\n"
						+ "		switch (o) {\n"
						+ "		case String o1 when o1.length() == ((String) o).length():\n"
						+ "			o = null;\n"
						+ "			o1 = null;\n"
						+ "			break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case String o1 when o1.length() == ((String) o).length():\n" +
				"	                    ^^\n" +
				"Local variable o1 referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	public void testIssue1351_3() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"class C {\n"
						+ "	int v;\n"
						+ "	public int value() { return this.v;\n}\n"
						+ "}\n"
						+ "public class X {\n"
						+ "	public void foo(C c) {\n"
						+ "		switch (c) {\n"
						+ "		case C c1 when c1.v == c1.value():\n"
						+ "			c1 = null;\n"
						+ "			break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	case C c1 when c1.v == c1.value():\n" +
				"	               ^^\n" +
				"Local variable c1 referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	public void testIssue1351_3a() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"class C {\n"
						+ "	int v;\n"
						+ "	public int value() { return this.v;\n}\n"
						+ "}\n"
						+ "public class X {\n"
						+ "	public void foo(C c) {\n"
						+ "		switch (c) {\n"
						+ "		case C c1 when c1.value() == c1.v:\n"
						+ "			c1 = null;\n"
						+ "			break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	case C c1 when c1.value() == c1.v:\n" +
				"	               ^^\n" +
				"Local variable c1 referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	public void testIssue1351_3b() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"interface I {\n"
						+ "	int v = 0;\n"
						+ "	public default int val() {\n"
						+ "		return v;\n"
						+ "	}\n"
						+ "}\n"
						+ "public class X {\n"
						+ "	public void foo(I intf) {\n"
						+ "		switch (intf) {\n"
						+ "		case I i1 when i1.v > i1.val():\n"
						+ "			i1 = null;\n"
						+ "			break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	case I i1 when i1.v > i1.val():\n" +
				"	               ^^\n" +
				"Local variable i1 referenced from a guard must be final or effectively final\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 10)\n" +
				"	case I i1 when i1.v > i1.val():\n" +
				"	                  ^\n" +
				"The static field I.v should be accessed in a static way\n" +
				"----------\n");
	}
	public void testIssue1351_3c() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"interface I {\n"
						+ "	int v = 1;\n"
						+ "	public default int val() {\n"
						+ "		return 0;\n"
						+ "	}\n"
						+ "}\n"
						+ "public class X {\n"
						+ "	public void foo(I intf) {\n"
						+ "		switch (intf) {\n"
						+ "		case I i1 when I.v > i1.val():\n"
						+ "			i1 = null;\n"
						+ "			break;\n"
						+ "		default:\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	case I i1 when I.v > i1.val():\n" +
				"	                     ^^\n" +
				"Local variable i1 referenced from a guard must be final or effectively final\n" +
				"----------\n");
	}
	public void testIssue1351_4() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"class C {\n"
						+ "	int v;\n"
						+ "	public int value() { return this.v;\n}\n"
						+ "}\n"
						+ "public class X {\n"
						+ "	C c0;\n"
						+ "	public void foo(C c) {\n"
						+ "		switch (c) {\n"
						+ "		case C c1 when c0.v == c0.value():\n"
						+ "			c0 = null;\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	switch (c) {\n" +
				"	        ^\n" +
				"An enhanced switch statement should be exhaustive; a default label expected\n" +
				"----------\n");
	}
	public void testIssue1351_5() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	public static void foo() {\n"
					+ "		Integer in = 0;\n"
					+ "		switch (in) {\n"
					+ "		    case Integer i ->\n"
					+ "		        System.out.println(\"Boxed\");\n"
					+ "		    case 95 ->\n"
					+ "		        System.out.println(\"Literal!\");\n"
					+ "		}\n"
					+ "	}\n"
					+ "} ",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	case 95 ->\n" +
				"	     ^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssue1351_6() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"	static String foo(Color o) {\n" +
					"		return switch (o) {\n" +
					"	     case Color s when true  -> s.toString();\n" +
					"	     case Red -> \"Red\";\n" +
					"	     case null -> \"\";\n" +
					"	   };\n" +
					"	}\n" +
					"} \n" +
					"enum Color {\n" +
					"	Red; \n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case Red -> \"Red\";\n" +
				"	     ^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	public void testIssue1351_7() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
				+ "	public int foo(Byte exp) {\n"
				+ "		int res = 0;\n"
				+ "		switch (exp) {\n"
				+ "			case Byte p when p.equals(exp), (byte) 0 -> {\n"
				+ "				res = 6;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			default -> {}\n"
				+ "		}\n"
				+ "		return res;\n"
				+ "	}\n"
				+ "}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Byte p when p.equals(exp), (byte) 0 -> {\n" +
			"	                                ^^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n");
	}
	public void testIssue1351_8() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
				+ "	public int foo(Byte exp) {\n"
				+ "		int res = 0;\n"
				+ "		switch (exp) {\n"
				+ "			case (byte) 0, Byte p when p.equals(exp) -> {\n"
				+ "				res = 6;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			default -> {}\n"
				+ "		}\n"
				+ "		return res;\n"
				+ "	}\n"
				+ "}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case (byte) 0, Byte p when p.equals(exp) -> {\n" +
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n");
	}
	public void testIssue1351_9() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
				+ "	public int foo(Byte exp) {\n"
				+ "		int res = 0;\n"
				+ "		switch (exp) {\n"
				+ "			case (byte) 0, (byte) 10, Byte p when p.equals(exp) -> {\n"
				+ "				res = 6;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			default -> {}\n"
				+ "		}\n"
				+ "		return res;\n"
				+ "	}\n"
				+ "}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case (byte) 0, (byte) 10, Byte p when p.equals(exp) -> {\n" +
			"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n");
	}
	public void testIssue1351_10() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
				+ "	public int foo(Byte exp) {\n"
				+ "		int res = 0;\n"
				+ "		switch (exp) {\n"
				+ "			case Byte p when p.equals(exp), null -> {\n"
				+ "				res = 6;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			default -> {}\n"
				+ "		}\n"
				+ "		return res;\n"
				+ "	}\n"
				+ "}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Byte p when p.equals(exp), null -> {\n" +
			"	                                ^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case Byte p when p.equals(exp), null -> {\n" +
			"	                                ^^^^\n" +
			"A null case label has to be either the only expression in a case label or the first expression followed only by a default\n" +
			"----------\n");
	}
	public void testIssue1351_11() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
				+ "	public int foo(Byte exp) {\n"
				+ "		int res = 0;\n"
				+ "		switch (exp) {\n"
				+ "			case Byte p when p.equals(exp), default -> {\n"
				+ "				res = 6;\n"
				+ "				break;\n"
				+ "			}\n"
				+ "		}\n"
				+ "		return res;\n"
				+ "	}\n"
				+ "}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Byte p when p.equals(exp), default -> {\n" +
			"	                                ^^^^^^^\n" +
			"Cannot mix pattern with other case labels\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	case Byte p when p.equals(exp), default -> {\n" +
			"	                                ^^^^^^^\n" +
			"A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default' \n" +
			"----------\n");
	}

	public void testDisambiguatedRestrictedIdentifierWhenAsFirstMethodInvokation() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	public static void main(String argv[]) {\n"
					+ "		when(\"Pass\");\n"
					+ "	}\n"
					+ "	static void when(String arg) {\n"
					+ "		System.out.println(arg);\n"
					+ "	}\n"
					+ "}"
				},
				"Pass");
	}

	public void testDisambiguatedRestrictedIdentifierWhenAsFirstVariableDeclaration() {
		runConformTest(
				new String[] {
					"when.java",
					"public class when {\n"
					+ "	public static void main(String argv[]) {\n"
					+ "		when x = new when();\n"
					+ "		System.out.println(x);\n"
					+ "	}\n"
					+ "	public String toString() {\n"
					+ "		return \"Pass\";\n"
					+ "	}\n"
					+ "}"
				},
				"Pass");
	}

	public void testDisambiguatedRestrictedIdentifierWhenAsTypeInACase() {
		runConformTest(
				new String[] {
					"when.java",
					"public class when {\n"
					+ "	public String toString() {\n"
					+ "		return switch((Object) this) {\n"
					+ "			case when x -> \"Pass\";\n"
					+ "			default -> \"Fail\";\n"
					+ "		};\n"
					+ "	}\n"
					+ "	public static void main(String argv[]) {\n"
					+ "		System.out.println(new when());\n"
					+ "	}\n"
					+ "}"
				},
				"Pass");
	}

	public void testDisambiguatedRestrictedIdentifierWhenAfterAParenthesis() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "	public static void main(String argv[]) {\n"
					+ "		System.out.println( (Boolean) when(true) );\n"
					+ "	}\n"
					+ "	static Object when(Object arg) {\n"
					+ "		return arg;\n"
					+ "	}\n"
					+ "}"
				},
				"true");
	}

	public void testValidCodeWithVeryAmbiguousUsageOfWhen() {
		runConformTest(
				new String[] {
					"when.java",
					"class when {\n"
					+ "  boolean when = true;\n"
					+ "  static boolean when(when arg) {\n"
					+ "    return switch(arg) {\n"
					+ "      case when when when when.when && when.when(null) -> when.when;\n"
					+ "      case null -> true;\n"
					+ "      default -> false;\n"
					+ "    };\n"
					+ "  }\n"
					+ "  public static void main(String[] args) {\n"
					+ "    System.out.println(when(new when()));\n"
					+ "  }\n"
					+ "}"
				},
				"true");
	}
	public void testIssue1466_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {

						  private static String foo(Integer i) {
						    return switch (i) {
						      case null -> "null";
						      case Integer value when value > 0 -> value.toString();
						      default -> i.toString();
						    };
						  }

						  public static void main(String[] args) {
						    System.out.println(foo(0));
						  }
						}

					""",
				},
				"0");
	}
	public void testIssue1466_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					  public static void main(String[] args) {
					    constantLabelMustAppearBeforePatternInteger(-1);
					    constantLabelMustAppearBeforePatternInteger(0);
					    constantLabelMustAppearBeforePatternInteger(42);
					    constantLabelMustAppearBeforePatternInteger(-99);
					    constantLabelMustAppearBeforePatternInteger(Integer.valueOf(123));
					    constantLabelMustAppearBeforePatternInteger(null);
					  }
					  static String constantLabelMustAppearBeforePatternInteger(Integer i) {
					    switch (i) {
					      case null -> System.out.println("value unavailable: " + i);
					      case -1, 1 -> System.out.println("absolute value 1: " + i);
					      case Integer value when value > 0 -> System.out.println("positive integer: " + i);
					      default -> System.out.println("other integer: " + i);
					    }
					    return i == null ? "null" : i.toString();
					  }
					}

					""",
				},
				"absolute value 1: -1\n" +
				"other integer: 0\n" +
				"positive integer: 42\n" +
				"other integer: -99\n" +
				"positive integer: 123\n" +
				"value unavailable: null"
);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1767
	// NPE in switch with case null
	public void testIssue1767() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					   public static void main(String[] args) {
						   Integer o = null;
						   switch (o) {
						     case null:
						       System.out.println("NULL");
						       break;
						     default : System.out.println(o);
						   }
					   }
					}
					""",
				},
				"NULL");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/277
	// [19] statement switch with a case null does not compile
	public void testIssue277() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					  enum Color { RED, BLACK }

					  public static void main(String[] args) {
					    Color color = null;
					    switch (color) {
					      case null -> System.out.println("NULL");
					      case RED -> System.out.println("RED");
					      case BLACK -> System.out.println("BLACK");
					    }
					  }
					}
					""",
				},
				"NULL");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/277
	// [19] statement switch with a case null does not compile
	public void testIssue277_original() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					  enum Color { RED, BLACK }

					  public static void main(String[] args) {
					    Color color = Color.RED;
					    switch (color) {
					      case null -> throw null;
					      case RED -> System.out.println("RED");
					      case BLACK -> System.out.println("BLACK");
					    }
					  }
					}
					""",
				},
				"RED");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/554
	// [19] statement switch with a case null does not compile
	public void testIssue554() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					    public static void main(String[] args) {
					        MyEnum val = null;
					        switch (val) {
					        case null:
					            System.out.println("val is null");
					            break;
					        }
					    }
					}
					enum MyEnum {
					    a
					}
					""",
				},
				"val is null");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/113
	// [switch] The Class file generated by ECJ for guarded patterns behaves incorrectly
	public void testGHI113() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {

						interface Shape {
							public double calculateArea();
						}

						record Triangle(int base, int height) implements Shape {

							@Override
							public double calculateArea() {
								return (0.5 * base * height);
							}

						}

						record Square(int side) implements Shape {

							@Override
							public double calculateArea() {
								return (side * side);
							}

						}

						static String evaluate(Shape s) {
							return switch(s) {
								case null ->
									"NULL";
								case Triangle T when (T.calculateArea() > 100) ->
								    "Large Triangle : " + T.calculateArea();
								case Triangle T ->
								    "Small Triangle : " + T.calculateArea();
								default ->
								    "shape : " + s.calculateArea();
							};
						}

						public static void main(String[] args) {
							System.out.println(evaluate(new Triangle(10, 10)));
							System.out.println(evaluate(new Triangle(20, 20)));
							System.out.println(evaluate(new Square(10)));
							System.out.println(evaluate(null));
						}
					}
					""",
				},
				"Small Triangle : 50.0\n"
				+ "Large Triangle : 200.0\n"
				+ "shape : 100.0\n"
				+ "NULL");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1853
	// [switch][pattern] Scope of pattern binding extends illegally resulting in wrong diagnostic
	public void testGH1853() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    public static void main(String[] args) {
						Object o = new Object();
						switch (o) {
						case String s :
							if (!(o instanceof String str))
								throw new RuntimeException();
						case null :
							if (!(o instanceof String str))
								throw new RuntimeException();
						default:
				            System.out.println("Default");
						}
					}
				}
				"""
			},
			"Default");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1856
	// [switch][record patterns] NPE: Cannot invoke "org.eclipse.jdt.internal.compiler.lookup.MethodBinding.isStatic()"
	public void testGHI1856() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
					public class X {

						public class Data {
						    String name;
						}

						record WrapperRec(ExhaustiveSwitch.Data data) {}


						public static void main(String[] args) {
						    switch (new Object()) {
						        case WrapperRec(var data) when data.name.isEmpty() -> { }
						        default -> {}
						    }
						}
					}
					""",
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 1)\n"
				+ "	public class X {\n"
				+ "	^\n"
				+ "Data cannot be resolved to a type\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	record WrapperRec(ExhaustiveSwitch.Data data) {}\n"
				+ "	                  ^^^^^^^^^^^^^^^^\n"
				+ "ExhaustiveSwitch cannot be resolved to a type\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 12)\n"
				+ "	case WrapperRec(var data) when data.name.isEmpty() -> { }\n"
				+ "	                ^^^^^^^^\n"
				+ "Data cannot be resolved to a type\n"
				+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1856
	// [switch][record patterns] NPE: Cannot invoke "org.eclipse.jdt.internal.compiler.lookup.MethodBinding.isStatic()"
	public void testGHI1856_2() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
					public class X {

						public class Data {
						    String name;
						}

						record WrapperRec(ExhaustiveSwitch.Data data) {}


						public static void main(String[] args) {
						    switch (new Object()) {
						        case WrapperRec(ExhaustiveSwitch.Data data) when data.name.isEmpty() -> { }
						        default -> {}
						    }
						}
					}
					""",
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 1)\n"
				+ "	public class X {\n"
				+ "	^\n"
				+ "Data cannot be resolved to a type\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	record WrapperRec(ExhaustiveSwitch.Data data) {}\n"
				+ "	                  ^^^^^^^^^^^^^^^^\n"
				+ "ExhaustiveSwitch cannot be resolved to a type\n"
				+ "----------\n"
				+ "3. ERROR in X.java (at line 12)\n"
				+ "	case WrapperRec(ExhaustiveSwitch.Data data) when data.name.isEmpty() -> { }\n"
				+ "	                ^^^^^^^^^^^^^^^^\n"
				+ "ExhaustiveSwitch cannot be resolved to a type\n"
				+ "----------\n"
				+ "4. ERROR in X.java (at line 12)\n"
				+ "	case WrapperRec(ExhaustiveSwitch.Data data) when data.name.isEmpty() -> { }\n"
				+ "	                ^^^^^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Record component with type Data is not compatible with type ExhaustiveSwitch.Data\n"
				+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1955
	// [Patterns] Redesign resolution of patterns to follow natural visitation
	public void testGH1955() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
					sealed interface I<T> {}
					record R<T extends A<B>>(T t) implements I<T> {}
					public class X {
					    @SuppressWarnings("rawtypes")
						public static <T extends I> int foo(T t) {
					        return switch(t) {
					            case R(A<? extends B> p) -> 0;
					            case R(var varp) -> 1;
					        };
					    }
					}
					class A<T> {}
					abstract class B {}
					class C extends B {}
					""",
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 8)\n"
				+ "	case R(var varp) -> 1;\n"
				+ "	     ^^^^^^^^^^^\n"
				+ "This case label is dominated by one of the preceding case labels\n"
				+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						Object o = null;
						foo(new R());
						foo(new S());
					}
					@SuppressWarnings("preview")
					public static void foo(Object o) {
						switch (o) {
						    case R():                         // Multiple case labels!
						        System.out.println("R Only");
						    case S():                         // Multiple case labels!
						        System.out.println("Either S or an R");
						        break;
						    default:
						}
					}
				}

				record R() {}
				record S() {}
				"""
			},
		 "R Only\n" +
		 "Either S or an R\n" +
		 "Either S or an R");

		String expectedOutput =
				"  // Method descriptor #22 (Ljava/lang/Object;)V\n" +
				"  // Stack: 2, Locals: 3\n" +
				"  public static void foo(java.lang.Object o);\n" +
				"     0  aload_0 [o]\n" +
				"     1  dup\n" +
				"     2  invokestatic java.util.Objects.requireNonNull(java.lang.Object) : java.lang.Object [30]\n" +
				"     5  pop\n" +
				"     6  astore_1\n" +
				"     7  iconst_0\n" +
				"     8  istore_2\n" +
				"     9  aload_1\n" +
				"    10  iload_2\n" +
				"    11  invokedynamic 0 typeSwitch(java.lang.Object, int) : int [36]\n" +
				"    16  tableswitch default: 56\n" +
				"          case 0: 40\n" +
				"          case 1: 48\n" +
				"    40  getstatic java.lang.System.out : java.io.PrintStream [40]\n" +
				"    43  ldc <String \"R Only\"> [46]\n" +
				"    45  invokevirtual java.io.PrintStream.println(java.lang.String) : void [48]\n" +
				"    48  getstatic java.lang.System.out : java.io.PrintStream [40]\n" +
				"    51  ldc <String \"Either S or an R\"> [54]\n" +
				"    53  invokevirtual java.io.PrintStream.println(java.lang.String) : void [48]\n" +
				"    56  return\n";

		SwitchPatternTest.verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773_2() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) -> 1;
							case R(B b1, B b2) -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "123");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773_3() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) when o == null -> 1;
							case R(B b1, B b2) when o == null -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "333");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773_4() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) when o != null -> 1;
							case R(B b1, B b2) when o != null -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "123");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773_5() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) when o != null -> 1;
							case R(B b1, B b2) when o == null -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "133");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773_6() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				   record CoffeeBreak() {}
				       public int recharge(CoffeeBreak c) {
				           int energyLevel = 0;
				           switch (c) {
				               case CoffeeBreak( ) -> {
				                   energyLevel = 3;
				               }
				               default->{
				                   energyLevel = -3;
				               }
				           }
				           return energyLevel;
				       }
				       public static void main(String argv[]) {
				           X t = new X();
				           CoffeeBreak c = new CoffeeBreak();
				           if (t.recharge(c) == 3) {
				        	   System.out.println("OK!");
				           } else {
				        	   System.out.println("!OK!");
				           }
				       }
				}
				"""
			},
		 "OK!");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2053
	// ECJ rejects guarded pattern in switch as being dominated by prior cases
	public void testIssue2053() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) when true -> 1;
							case R(B b1, B b2) when o != null -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "123");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2053
	// ECJ rejects guarded pattern in switch as being dominated by prior cases
	public void testIssue2053_2() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) when true -> 1;
							case R(B b1, B b2) when o == null -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "133");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2077
	// [Patterns] Incorrect complaint about non-final variable reference from guard expression
	public void testIssue2077() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    public static void main(String [] args) {
				        new X().foo("Hello ", 1);
				    }
				    public void foo(Object obj, int x) {
				        int y = 10;
				        y = 20;
				        y = 30;
				        switch (obj) {
				         case String s when switch (x) {
				                    case 1 -> { int y1 = 10; y1 = 30; yield y1!=20; }
				                    default -> { yield false; }
				                }
				                 -> {
				                    System.out.println(s + "OK");
				                    if (y == 0)
				                    	System.out.println(s + "OK");
				                 }

				         default -> {}
				        }
				    }
				}
				"""
			},
		 "Hello OK");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2077
	// [Patterns] Incorrect complaint about non-final variable reference from guard expression
	public void testIssue2077_2() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    public static void main(String [] args) {
				        new X().foo("Hello ", 1);
				    }
				    public void foo(Object obj, int x) {
				        int y = 10;
				        y = 20;
				        y = 30;
				        switch (obj) {
				         case String s when switch (x) {
				                    case 1 -> { int y1 = 10; y1 = 30; yield 30 != y1; }
				                    default -> { yield false; }
				                } && y != 0
				                 -> {
				                    System.out.println(s + "OK");
				                    if (y == 0)
				                    	System.out.println(s + "OK");
				                 }

				         default -> {}
				        }
				    }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 13)\r\n" +
			"	} && y != 0\r\n" +
			"	     ^\n" +
			"Local variable y referenced from a guard must be final or effectively final\n" +
			"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2077
	// [Patterns] Incorrect complaint about non-final variable reference from guard expression
	public void testIssue2077_3() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    public static void main(String [] args) {
				        new X().foo("Hello ", 1);
				    }
				    public void foo(Object obj, int x) {
				        int y = 10;
				        y = 20;
				        y = 30;
				        switch (obj) {
				         case String s when switch (x) {
				                    case 1 -> { int y1 = 10; y1 = 30; yield y != y1; }
				                    default -> { yield false; }
				                } && y != 0
				                 -> {
				                    System.out.println(s + "OK");
				                    if (y == 0)
				                    	System.out.println(s + "OK");
				                 }

				         default -> {}
				        }
				    }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\r\n" +
			"	case 1 -> { int y1 = 10; y1 = 30; yield y != y1; }\r\n" +
			"	                                        ^\n" +
			"Local variable y referenced from a guard must be final or effectively final\n" +
			"----------\n");
		    // We throw AbortMethod after first error, so second error doesn't surface
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2318
	// [Switch Expression] Assertion Error compiling switch + try + string concat at target platforms levels < 9
	public void testIssue2318() {
		Map<String,String> options = getCompilerOptions();
		String tpf = options.get(CompilerOptions.OPTION_TargetPlatform);
		try {
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
			String [] sourceFiles =
				new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String [] args) {\n" +
				"		int i = 123;\n" +
				"		System.out.println(\"\" + switch (args) {\n" +
				"			case null -> { try {\n" +
				"							throw new NullPointerException(\"Value in position \"+ i +\" must not be null\");\n" +
				"						} finally {\n" +
				"							yield \"exception\";\n" +
				"						}\n" +
				"						}\n" +
				"			default -> \"Hello\";\n" +
				"		});\n" +
				"	}\n" +
				"}\n",
			};
			this.runConformTest(sourceFiles, "Hello", options);
		} finally {
			options.put(CompilerOptions.OPTION_TargetPlatform, tpf);
		}
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2319
	// [Switch Expression] Verify error when using non-indy string concat
	public void testIssue2319() {
		Map<String,String> options = getCompilerOptions();
		String uscf = options.get(CompilerOptions.OPTION_UseStringConcatFactory);
		try {
			options.put(CompilerOptions.OPTION_UseStringConcatFactory, CompilerOptions.DISABLED);
			String [] sourceFiles =
				new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String [] args) {\n" +
				"		int i = 123;\n" +
				"		System.out.println(\"\" + switch (args) {\n" +
				"			case null -> { try {\n" +
				"							throw new NullPointerException(\"Value in position \"+ i +\" must not be null\");\n" +
				"						} finally {\n" +
				"							yield \"exception\";\n" +
				"						}\n" +
				"						}\n" +
				"			default -> \"Hello\";\n" +
				"		});\n" +
				"	}\n" +
				"}\n",
			};
			this.runConformTest(sourceFiles, "Hello", options);
		} finally {
			options.put(CompilerOptions.OPTION_UseStringConcatFactory, uscf);
		}
	}
}
