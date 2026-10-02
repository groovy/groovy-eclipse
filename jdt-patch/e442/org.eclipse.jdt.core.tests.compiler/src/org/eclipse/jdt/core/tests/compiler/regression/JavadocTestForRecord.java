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

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class JavadocTestForRecord extends JavadocTest {

	static {
//		 TESTS_NAMES = new String[] { "testBug549855a" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	public JavadocTestForRecord(String name) {
		super(name);
	}

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportInvalidJavadocTags = CompilerOptions.ENABLED;
	String reportInavlidJavadocTagsVisibility = CompilerOptions.PRIVATE;
	String reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocTagsOverriding = CompilerOptions.ENABLED;
	String reportMissingJavadocComments = CompilerOptions.ERROR;
	String reportMissingJavadocCommentsVisibility = CompilerOptions.PROTECTED;

	public static Class<JavadocTestForRecord> testClass() {
		return JavadocTestForRecord.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug95521";
//		TESTS_NAMES = new String[] { "testBug331872d" };
//		TESTS_NUMBERS = new int[] { 101283 };
//		TESTS_RANGE = new int[] { 23, -1 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}

	protected Map<String, String> getCompilerOptions() {
		Map<String, String> options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
		if (!CompilerOptions.IGNORE.equals(this.reportInvalidJavadoc)) {
			options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, this.reportInvalidJavadocVisibility);
		}
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportInvalidJavadoc);
		if (this.reportMissingJavadocCommentsVisibility != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility,
					this.reportMissingJavadocCommentsVisibility);
		if (this.reportMissingJavadocTags != null) {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
			if (this.reportMissingJavadocTagsOverriding != null) {
				options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding,
						this.reportMissingJavadocTagsOverriding);
			}
		} else {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
		}
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, this.reportInvalidJavadocTags);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, this.reportInavlidJavadocTagsVisibility);
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_Release, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16); // FIXME
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		return options;
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */

	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview("16"));
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
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("16");
		runner.runConformTest();
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.docCommentSupport = CompilerOptions.ENABLED;
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocTags = CompilerOptions.ENABLED;
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsOverriding = CompilerOptions.ENABLED;
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
	}

	public void test001() {
		this.runNegativeTest(new String[] { "X.java", "public record X() {\n" + "}\n" },
				"----------\n" + "1. ERROR in X.java (at line 1)\n" + "	public record X() {\n" + "	              ^\n"
						+ "Javadoc: Missing comment for public declaration\n" + "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test002() {
		this.runNegativeTest(
				new String[] { "X.java",
						"	/**\n" + "	 * @param radius radius of X\n" + "	 */\n" + "public record X(int radius) {\n"
								+ "	public void foo() {\n" + "	}\n" + "}\n" },
				"----------\n" + "1. ERROR in X.java (at line 5)\n" + "	public void foo() {\n" + "	            ^^^^^\n"
						+ "Javadoc: Missing comment for public declaration\n" + "----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test003() {
		runConformTest(new String[] { "X.java",
				"		/**  \n" + "		 *   \n" + "		 */  \n" + "public record X() {\n" + "		/**  \n"
						+ "		 *   @param args \n" + "		 */  \n" + "  public static void main(String[] args){\n"
						+ "     System.out.println(0);\n" + "  }\n" + "}" },
				"0");
	}

	public void test004() {
		runConformTest(new String[] { "X.java",
				"		/**  \n" +
				"		 * @param a\n" +
				"		 */  \n" +
				"		public record X(int a) {\n" +
				"			/**  \n" +
				"			 *   @param args \n" + "		 */  \n" +
				"			public static void main(String[] args){\n" +
				"				System.out.println(0);\n" +
				"			}\n" +
				"		}" },
				"0");
	}

	public void test005() {
		runNegativeTest(new String[] { "X.java",
				"		/**  \n" +
				"		 */  \n" +
				"		public record X(int a) {\n" +
				"			/**  \n" +
				"			 *   @param args \n" +
				"			 */  \n" +
				"			public static void main(String[] args){\n" +
				"				System.out.println(0);\n" +
				"			}\n" +
				"		}" },
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	public record X(int a) {\n" +
				"	                    ^\n" +
				"Javadoc: Missing tag for parameter a\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test006() {
		runNegativeTest(new String[] { "X.java",
				"		/**  \n" +
				"		 * @param a\n" +
				"		 * @param a\n" +
				"		 */  \n" +
				"		public record X(int a) {\n" +
				"			/**  \n" +
				"			 *   @param args \n" +
				"			 */  \n" +
				"			public static void main(String[] args){\n" +
				"				System.out.println(0);\n" +
				"			}\n" +
				"		}" },
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param a\n" +
				"	         ^\n" +
				"Javadoc: Duplicate tag for parameter\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	public void test007() {
		runNegativeTest(new String[] { "X.java",
				"		/**  \n" +
				"		 * @param a\n" +
				"		 * @param b\n" +
				"		 */  \n" +
				"		public record X(int a) {\n" +
				"			/**  \n" +
				"			 *   @param args \n" +
				"			 */  \n" +
				"			public static void main(String[] args){\n" +
				"				System.out.println(0);\n" +
				"			}\n" +
				"		}" },
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param b\n" +
				"	         ^\n" +
				"Javadoc: Parameter b is not declared\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test_bug572367() {
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.PRIVATE;
		runConformTest(new String[] { "X.java",
				"		/**  \n" +
				"		 * @param a\n" +
				"		 */  \n" +
				"		public record X(int a) {\n" +
				"			/**  \n" +
				"			 *   @param args \n" + "		 */  \n" +
				"			public static void main(String[] args){\n" +
				"				System.out.println(0);\n" +
				"			}\n" +
				"		}" },
				"0");
	}
	public void testGHIssue4158_1() {
		runNegativeTest(new String[] { "X.java",
				"""
					/**
					 * @param abc
					 * @param abc
					 * @param def
					 * @param xyz
					 * @return no return
					 */
					public record X(int abc, int def) {}
					""" },
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param abc\n" +
				"	         ^^^\n" +
				"Javadoc: Duplicate tag for parameter\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	* @param abc\n" +
				"	         ^^^\n" +
				"Javadoc: Duplicate tag for parameter\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param xyz\n" +
				"	         ^^^\n" +
				"Javadoc: Parameter xyz is not declared\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	* @return no return\n" +
				"	   ^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testGHIssue4158_2() {
		runNegativeTest(new String[] { "X.java",
					"""
					/**
					 * @param <T> a type param
					 * @param <S> a type param
					 * @param abc first
					 * Javadoc with missing param tags
					 */
					public record X<U, V>(int abc, int def) {}
					""" },
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	* @param <T> a type param\n" +
				"	          ^\n" +
				"Javadoc: T cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	* @param <S> a type param\n" +
				"	          ^\n" +
				"Javadoc: S cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	public record X<U, V>(int abc, int def) {}\n" +
				"	                ^\n" +
				"Javadoc: Missing tag for parameter U\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	public record X<U, V>(int abc, int def) {}\n" +
				"	                   ^\n" +
				"Javadoc: Missing tag for parameter V\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	public record X<U, V>(int abc, int def) {}\n" +
				"	                                   ^^^\n" +
				"Javadoc: Missing tag for parameter def\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}
