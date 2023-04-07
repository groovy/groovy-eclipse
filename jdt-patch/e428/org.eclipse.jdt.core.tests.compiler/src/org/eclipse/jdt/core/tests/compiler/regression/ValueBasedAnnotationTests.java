/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
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
public class ValueBasedAnnotationTests extends AbstractRegressionTest {
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug562219_001"};
	}
	public static Class<?> testClass() {
		return ValueBasedAnnotationTests.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}
	public ValueBasedAnnotationTests(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	@Override
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		if (this.complianceLevel >= ClassFileConstants.getLatestJDKLevel()) {
			defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		}
		return defaultOptions;
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
		runWarningTest(testFiles, expectedCompilerLog, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
			Map<String, String> customOptions, String javacAdditionalTestOptions) {
		if (!isJRE16Plus)
			return;
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview("16") :
			JavacTestOptions.forReleaseWithPreview("16", javacAdditionalTestOptions);
		runner.runWarningTest();
	}
	protected void runConformTest(String[] testFiles) {
		runConformTest(testFiles, (Map<String, String>)null, null);
	}
	protected void runConformTest(String[] testFiles, Map<String, String> customOptions, String javacAdditionalTestOptions) {
		if (!isJRE16Plus)
			return;
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.customOptions = customOptions;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview("16") :
			JavacTestOptions.forReleaseWithPreview("16", javacAdditionalTestOptions);
		runner.runConformTest();
	}
	public void testBug571507_001() {
		this.runWarningTest(
			new String[] {
				"X.java",
				"class X {\n" +
				"  public static void main(String[] args){\n" +
				"		Integer abc= Integer.valueOf(10);\n" +
				"		synchronized(abc) {\n" +
				"			\n" +
				"		}" +
				"  }\n"+
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	synchronized(abc) {\n" +
			"	             ^^^\n" +
			"Integer is a value-based type which is a discouraged argument for the synchronized statement\n" +
			"----------\n");
	}
	public void testBug571507_002() {
		this.runWarningTest(
			new String[] {
				"X.java",
				"import java.util.Optional;\n\n" +
				"class X {\n" +
				"  public static void main(String[] args){\n" +
				"		String[] sentence = new String[10];\n" +
				"       Optional<String> abc = Optional.ofNullable(sentence[9]);  \n" +
				"		synchronized (abc) { // no error given here.\n" +
				"		}\n" +
				"  }\n"+
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 7)\n" +
			"	synchronized (abc) { // no error given here.\n" +
			"	              ^^^\n" +
			"Optional<T> is a value-based type which is a discouraged argument for the synchronized statement\n" +
			"----------\n");
	}
	public void testBug571507_003() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.HashSet;\n\n" +
				"class X {\n" +
				"  public static void main(String[] args){\n" +
				"		String[] sentence = new String[10];\n" +
				"       HashSet<String> abc = new HashSet<>();  \n" +
				"		synchronized (abc) { // no error given here.\n" +
				"		}\n" +
				"  }\n"+
				"}\n",
			});
	}
}