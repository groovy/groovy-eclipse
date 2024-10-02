/*******************************************************************************
* Copyright (c) 2024 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SealedTypesSpecReviewTest extends AbstractRegressionTest9 {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug564498_6"};
	}

	public static Class<?> testClass() {
		return SealedTypesSpecReviewTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}
	public SealedTypesSpecReviewTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("17");
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview("17"));
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
		runWarningTest(testFiles, expectedCompilerLog, (Map<String, String>) null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
	}

	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, String expectedOutput) {

		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.expectedOutputString = expectedOutput;
		runner.customOptions = getCompilerOptions();
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("16");
		runner.runWarningTest();
	}

	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
			Map<String, String> customOptions, String javacAdditionalTestOptions) {

		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview("16") :
			JavacTestOptions.forReleaseWithPreview("16", javacAdditionalTestOptions);
		runner.runWarningTest();
	}


	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2709
	// [Sealed types] Disjointness behavior difference vis a vis javac
	/* A class named C is disjoint from an interface named I if (i) it is not the case that C <: I, and (ii) one of the following cases applies:
	– C is freely extensible (§8.1.1.2), and I is sealed, and C is disjoint from all of the permitted direct subclasses and subinterfaces of I.
	*/
	public void testIssue2709() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    sealed interface I permits C1 {}
						    non-sealed class C1 implements I {}
						    class C2 extends C1 {}
						    class C3 {}
						    {
						        I i;
						        i = (I) (C1) null;
						        i = (I) (C2) null;
						        i = (I) (C3) null;
						        i = (C2) (C3) null;
						        i = (C1) (C3) null;
						    }
						}
						"""
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	i = (I) (C3) null;\n" +
				"	    ^^^^^^^^^^^^^\n" +
				"Cannot cast from X.C3 to X.I\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 11)\n" +
				"	i = (C2) (C3) null;\n" +
				"	    ^^^^^^^^^^^^^^\n" +
				"Cannot cast from X.C3 to X.C2\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 12)\n" +
				"	i = (C1) (C3) null;\n" +
				"	    ^^^^^^^^^^^^^^\n" +
				"Cannot cast from X.C3 to X.C1\n" +
				"----------\n");
	}
}