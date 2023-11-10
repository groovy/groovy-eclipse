/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class UnnammedPatternsAndVarsTest extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 21 --enable-preview -Xlint:-preview");
	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test005" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return UnnammedPatternsAndVarsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_21);
	}
	public UnnammedPatternsAndVarsTest(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		if (preview) {
			if (this.complianceLevel >= ClassFileConstants.getLatestJDKLevel()) {
				defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
			} else {
				defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_21);
				defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_21);
				defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_21);
			}
		}
		return defaultOptions;
	}

	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(true);
	}
	protected String[] getDefaultClassPaths() {
		String[] libs = DefaultJavaRuntimeEnvironment.getDefaultClassPaths();
		if (this.extraLibPath != null) {
			String[] l = new String[libs.length + 1];
			System.arraycopy(libs, 0, l, 0, libs.length);
			l[libs.length] = this.extraLibPath;
			return l;
		}
		return libs;
	}
	@Override
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		INameEnvironment[] classLibs = getClassLibs(false, options);
		for (INameEnvironment nameEnvironment : classLibs) {
			((FileSystem) nameEnvironment).scanForModules(createParser());
		}
		return new InMemoryNameEnvironment9(testFiles, this.moduleMap, classLibs);
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true));
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE21Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, new String[] {"--enable-preview"}, JAVAC_OPTIONS);
	}
	protected void runConformTest(
			String[] testFiles,
			String expectedOutputString,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			String[] vmArguments) {
			runTest(
		 		// test directory preparation
				shouldFlushOutputDirectory /* should flush output directory */,
				testFiles /* test files */,
				// compiler options
				classLibraries /* class libraries */,
				null /* no custom options */,
				false /* do not perform statements recovery */,
				null /* no custom requestor */,
				// compiler results
				false /* expecting no compiler errors */,
				null /* do not check compiler log */,
				// runtime options
				false /* do not force execution */,
				vmArguments /* vm arguments */,
				// runtime results
				expectedOutputString /* expected output string */,
				null /* do not check error string */,
				// javac options
				JavacTestOptions.DEFAULT /* default javac test options */);
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
	public void _test001() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo() {\n"+
				"   int _ = 1;\n"+
				"   return 0;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo());\n"+
				" }\n"+
				"}"
			},
			"0");
	}
	// Test that pattern variables are allowed for the nested patterns (not just the outermost record pattern)
	public void _test002() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public class X {\n"+
				" public static int foo() {\n"+
				"   int _ = bar();\n"+
				"   return 0;\n"+
				" }\n"+
				" public static int bar() {\n"+
				"   return 0;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo());\n"+
				" }\n"+
				"}"
			},
			"0");
	}
	public void _test003() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public class X {\n" +
				" public static int foo() {\n"+
				"   int _;\n"+ // Error should be thrown - uninitialized
				"   return 0;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo());\n"+
				" }\n"+
				"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"   int _;\\n" +
				"	    ^\n" +
				"local variable _ should be initialized\n" +
				"----------\n");
	}
	public void _test004() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public class X {\n" +
				" public static int foo() {\n"+
				"   int _ = 0;\n"+ // Error should be thrown - uninitialized
				"   return _;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo());\n"+
				" }\n"+
				"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"   int _;\\n" +
				"	    ^\n" +
				"_ can only be used in declarations\n" +
				"----------\n");
	}
	public void _test005() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public class X {\n" +
				"   public int _;\n"+ // Error should be thrown - Field not allowed
				" public static void main(String[] args) {\n"+
				"   System.out.println(0);\n"+
				" }\n"+
				"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"   int _;\\n" +
				"	    ^\n" +
				"_ can only be used in declarations\n" +
				"----------\n");
	}
}
