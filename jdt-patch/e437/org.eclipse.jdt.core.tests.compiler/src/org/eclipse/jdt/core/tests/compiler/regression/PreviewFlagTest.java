/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.PreviewTest;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@PreviewTest
public class PreviewFlagTest extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 24");
	private static final String[] VMARGS = new String[] {"--enable-preview"};
	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testIssue3943_001" };
//		TESTS_NAMES = new String[] { "testIssue3614_001" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return PreviewFlagTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_24);
	}
	public PreviewFlagTest(String testName) {
		super(testName);
	}

	// ========= OPT-IN to run.javac mode: ===========
	@Override
	protected void setUp() throws Exception {
		this.runJavacOptIn = true;
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.runJavacOptIn = false; // do it last, so super can still clean up
	}
	// =================================================

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_24);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_24);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_24);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		return defaultOptions;
	}

	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(false);
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
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true), VMARGS, JAVAC_OPTIONS);
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE22Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, VMARGS, JAVAC_OPTIONS);
	}
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Map<String, String> customOptions = getCompilerOptions(true);
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}
	class Runner extends AbstractRegressionTest.Runner {
		public Runner(boolean reportPreview) {
			this();
			this.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, reportPreview ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
		}
		public Runner() {
			super();
			this.vmArguments = VMARGS;
			this.javacTestOptions = JAVAC_OPTIONS;
			this.customOptions = getCompilerOptions();
			this.customOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		}
	}
	public void testIssue3614_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							ScopedValue<Integer> si = ScopedValue.newInstance();
							System.out.println(si == null ? "hello" : "world");
						}
					}
				"""
			},
			"world");
		String expectedOutput =
				"version 24 : 68.65535"
				;
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testIssue3614_002() throws IOException, ClassFormatException {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(new String[] {
				"X.java",
					"""
					public class X {
						public static void main(String[] args) {
							ScopedValue<Integer> si = ScopedValue.newInstance();
							System.out.println(si == null ? "hello" : "world");
						}
						public int foo() {}
					}
					"""
			},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	ScopedValue<Integer> si = ScopedValue.newInstance();\n" +
				"	                          ^^^^^^^^^^^\n" +
				"This API is part of the preview feature 'Scoped Values' which is disabled by default. Use --enable-preview to enable\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	public int foo() {}\n" +
				"	           ^^^^^\n" +
				"This method must return a result of type int\n" +
				"----------\n",
			null,
			true,
			options);
	}
	public void testIssue3614_003() throws Exception {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions(false);
		runner.javacTestOptions = JavacTestOptions.DEFAULT; // don't enable preview
		runner.testFiles = new String[] {
				"X.java",
				"""
				import com.sun.source.tree.ImportTree;
				public class X {
					boolean foo(ImportTree tree) {
						return tree.isModule();
					}
					public static void main(String... args) {}
				}
				"""
			};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. WARNING in X.java (at line 4)\n" +
				"	return tree.isModule();\n" +
				"	       ^^^^^^^^^^^^^^^\n" +
				"This API is part of the preview feature 'Module Import Declarations' which is disabled by default. Use --enable-preview to enable\n" +
				"----------\n";
		runner.runConformTest();
	}
	public void testIssue3614_003_enabled() throws Exception {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions(true);
		runner.vmArguments = VMARGS;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.testFiles = new String[] {
				"X.java",
				"""
				import com.sun.source.tree.ImportTree;
				public class X {
					boolean foo(ImportTree tree) {
						return tree.isModule();
					}
					public void main(String... args) {
						System.out.print(42);
					}
				}
				"""
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	return tree.isModule();\n" +
			"	       ^^^^^^^^^^^^^^^\n" +
			"You are using an API that is part of the preview feature \'Module Import Declarations\' and may be removed in future\n" +
			"----------\n";
		runner.expectedOutputString = "42";
		runner.runConformTest();
	}
	public void testIssue3943_001() throws IOException, ClassFormatException {
		Map<String, String> options = getCompilerOptions();
		String str = options.get(CompilerOptions.OPTION_Compliance);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_23);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_23);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_23);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						ScopedValue<Integer> si = ScopedValue.newInstance();
						System.out.println(si == null ? "hello" : "world");
					}
					public int foo() {}
				}
				"""},
				"----------\n" +
				"1. WARNING in X.java (at line 3)\n" +
				"	ScopedValue<Integer> si = ScopedValue.newInstance();\n" +
				"	                          ^^^^^^^^^^^\n" +
				"You are using an API that is part of the preview feature \'Scoped Values\' and may be removed in future\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	public int foo() {}\n" +
				"	           ^^^^^\n" +
				"This method must return a result of type int\n" +
				"----------\n",
			null,
			true,
			options);
		options.put(CompilerOptions.OPTION_Compliance, str);
		options.put(CompilerOptions.OPTION_Source, str);
		options.put(CompilerOptions.OPTION_TargetPlatform, str);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
	}
	public void testIssue3943_002() throws IOException, ClassFormatException {
		Map<String, String> options = getCompilerOptions();
		String str = options.get(CompilerOptions.OPTION_Compliance);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_23);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_23);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_23);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						ScopedValue<Integer> si = ScopedValue.newInstance();
						System.out.println(si == null ? "hello" : "world");
					}
					public int foo() {}
				}
				"""},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	public int foo() {}\n" +
				"	           ^^^^^\n" +
				"This method must return a result of type int\n" +
				"----------\n",
			null,
			true,
			options);
		options.put(CompilerOptions.OPTION_Compliance, str);
		options.put(CompilerOptions.OPTION_Source, str);
		options.put(CompilerOptions.OPTION_TargetPlatform, str);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
	}
}