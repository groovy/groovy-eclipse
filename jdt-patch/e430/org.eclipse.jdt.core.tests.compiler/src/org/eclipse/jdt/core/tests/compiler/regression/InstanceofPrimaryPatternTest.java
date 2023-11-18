/*******************************************************************************
 * Copyright (c) 2021, 2021 IBM Corporation and others.
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

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class InstanceofPrimaryPatternTest extends AbstractRegressionTest {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 17 --enable-preview -Xlint:-preview");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test005" };
	}

	public static Class<?> testClass() {
		return InstanceofPrimaryPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}
	public InstanceofPrimaryPatternTest(String testName){
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

	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(true);
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE17Plus)
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
	public void test001() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof String s) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			"Hello World!",
			options);
	}
	public void test002() {
		String expectedDiagnostics = this.complianceLevel < ClassFileConstants.JDK20 ?
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof (String s)) {\n" +
				"	                   ^\n" +
				"Syntax error on token \"(\", delete this token\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof (String s)) {\n" +
				"	                             ^\n" +
				"Syntax error on token \")\", delete this token\n" +
				"----------\n" :
							"----------\n" +
							"1. ERROR in X.java (at line 3)\n" +
							"	if (obj instanceof (String s)) {\n" +
							"	        ^^^^^^^^^^\n" +
							"Syntax error on token \"instanceof\", ReferenceType expected after this token\n" +
							"----------\n";
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof (String s)) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			expectedDiagnostics);
	}
	public void test003() {

		String expectedDiagnostics = this.complianceLevel < ClassFileConstants.JDK20 ?
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof ((String s))) {\n" +
				"	        ^^^^^^^^^^\n" +
				"Syntax error, insert \"Type\" to complete InstanceofClassic\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof ((String s))) {\n" +
				"	        ^^^^^^^^^^\n" +
				"Syntax error, insert \") Statement\" to complete BlockStatements\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof ((String s))) {\n" +
				"	                     ^^^^^^\n" +
				"Syntax error on token \"String\", ( expected after this token\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof ((String s))) {\n" +
				"	                               ^\n" +
				"Syntax error, insert \"AssignmentOperator Expression\" to complete Assignment\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 3)\n" +
				"	if (obj instanceof ((String s))) {\n" +
				"	                               ^\n" +
				"Syntax error, insert \";\" to complete Statement\n" +
				"----------\n" :
									"----------\n" +
									"1. ERROR in X.java (at line 3)\n" +
									"	if (obj instanceof ((String s))) {\n" +
									"	                   ^\n" +
									"Syntax error on token \"(\", invalid ReferenceType\n" +
									"----------\n" +
									"2. ERROR in X.java (at line 3)\n" +
									"	if (obj instanceof ((String s))) {\n" +
									"	                               ^\n" +
									"Syntax error on token \")\", delete this token\n" +
									"----------\n";

		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof ((String s))) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			expectedDiagnostics);
	}
	public void test007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof var s) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"		Zork();\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	if (obj instanceof var s) {\n" +
			"	                   ^^^\n" +
			"\'var\' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void test009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(String s) {\n" +
				"		if (s instanceof Object o) {\n" +
				"			System.out.println(s1);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	System.out.println(s1);\n" +
			"	                   ^^\n" +
			"s1 cannot be resolved to a variable\n" +
			"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1076
	// ECJ accepts invalid Java code instanceof final Type
	public void testGH1076() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class Test {\n" +
				"    void check() {\n" +
				"        Number n = Integer.valueOf(1);\n" +
				"        if (n instanceof final Integer) {}\n" +
				"        if (n instanceof final Integer x) {}\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if (n instanceof final Integer) {}\n" +
			"	                 ^^^^^^^^^^^^^\n" +
			"Syntax error, modifiers are not allowed here\n" +
			"----------\n");
	}
}