/*******************************************************************************
 * Copyright (c) 2010, 2018 GK Software AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.osgi.framework.Bundle;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class AbstractNullAnnotationTest extends AbstractComparableTest {

	// class libraries including our default null annotation types:
	String[] LIBS;

	// names and content of custom annotations used in a few tests:
	static final String CUSTOM_NONNULL_NAME = "org/foo/NonNull.java";
	static final String CUSTOM_NONNULL_CONTENT =
			"package org.foo;\n" +
			"import static java.lang.annotation.ElementType.*;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({METHOD,PARAMETER,LOCAL_VARIABLE})\n" +
			"public @interface NonNull {\n" +
			"}\n";
	static final String CUSTOM_NONNULL_CONTENT_JSR308 =
			"package org.foo;\n" +
			"import static java.lang.annotation.ElementType.*;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({METHOD,PARAMETER,LOCAL_VARIABLE,TYPE_USE})\n" +
			"public @interface NonNull {\n" +
			"}\n";
	static final String CUSTOM_NULLABLE_NAME = "org/foo/Nullable.java";
	static final String CUSTOM_NULLABLE_CONTENT = "package org.foo;\n" +
			"import static java.lang.annotation.ElementType.*;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({METHOD,PARAMETER,LOCAL_VARIABLE})\n" +
			"public @interface Nullable {\n" +
			"}\n";
	static final String CUSTOM_NULLABLE_CONTENT_JSR308 = "package org.foo;\n" +
			"import static java.lang.annotation.ElementType.*;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({METHOD,PARAMETER,LOCAL_VARIABLE,TYPE_USE})\n" +
			"public @interface Nullable {\n" +
			"}\n";
	static final String CUSTOM_NNBD_NAME = "org/foo/NonNullByDefault.java";
	static final String CUSTOM_NNBD_CONTENT = "package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"import static java.lang.annotation.ElementType.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({PACKAGE, TYPE, METHOD, CONSTRUCTOR })\n" +
			"public @interface NonNullByDefault {\n" + // has no details, so default default locations should be applied
			"}\n";

	public AbstractNullAnnotationTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setUpAnnotationLib();
	}

	/**
	 * @deprecated indirectly uses deprecated class PackageAdmin
	 */
	protected void setUpAnnotationLib() throws IOException {
		if (this.LIBS == null) {
			String[] defaultLibs = getDefaultClassPaths();
			int len = defaultLibs.length;
			this.LIBS = new String[len+1];
			System.arraycopy(defaultLibs, 0, this.LIBS, 0, len);
			this.LIBS[len] = getAnnotationLibPath();
		}
	}

	protected String getAnnotationLibPath() throws IOException {
		Bundle bundle = Platform.getBundle("org.eclipse.jdt.annotation");
		File bundleFile = FileLocator.getBundleFileLocation(bundle).get();
		if (bundleFile.isDirectory())
			return bundleFile.getPath()+"/bin";
		else
			return bundleFile.getPath();
	}

	public static String getAnnotationV1LibPath() throws IOException {
		URL libEntry = Platform.getBundle("org.eclipse.jdt.core.tests.compiler").getEntry("/lib/org.eclipse.jdt.annotation_1.2.100.v20241001-0914.jar");
		return FileLocator.toFileURL(libEntry).getPath();
	}

	// Conditionally augment problem detection settings
	static boolean setNullRelatedOptions = true;

	@Override
	protected Map getCompilerOptions() {
	    Map defaultOptions = super.getCompilerOptions();
	    if (setNullRelatedOptions) {
	    	defaultOptions.put(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
		    defaultOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		    defaultOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
			defaultOptions.put(JavaCore.COMPILER_PB_INCLUDE_ASSERTS_IN_NULL_ANALYSIS, JavaCore.ENABLED);

			defaultOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION_FOR_INTERFACE_METHOD_IMPLEMENTATION, JavaCore.DISABLED);

			// enable null annotations:
			defaultOptions.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			defaultOptions.put(CompilerOptions.OPTION_PessimisticNullAnalysisForFreeTypeVariables, JavaCore.ERROR);
			defaultOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, JavaCore.WARNING);

			// leave other new options at these defaults:
//			defaultOptions.put(CompilerOptions.OPTION_ReportNullContractViolation, JavaCore.ERROR);
//			defaultOptions.put(CompilerOptions.OPTION_ReportPotentialNullContractViolation, JavaCore.ERROR);
//			defaultOptions.put(CompilerOptions.OPTION_ReportNullContractInsufficientInfo, CompilerOptions.WARNING);

//			defaultOptions.put(CompilerOptions.OPTION_NullableAnnotationName, "org.eclipse.jdt.annotation.Nullable");
//			defaultOptions.put(CompilerOptions.OPTION_NonNullAnnotationName, "org.eclipse.jdt.annotation.NonNull");
	    }
	    return defaultOptions;
	}
	/** Test expecting a null-error from ecj, none from javac. */
	protected void runNegativeNullTest(String[] sourceFiles, String expectedCompileError, String[] libs, boolean shouldFlush, Map options) {
		runNegativeTest(
				sourceFiles,
				expectedCompileError,
				libs,
				shouldFlush,
				options,
				null /* do not check error string */,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	/** Test with JDT null annotations, expecting a null-error from ecj, none from javac. */
	void runNegativeTestWithLibs(String[] testFiles, String expectedErrorLog) {
		runNegativeTestWithLibs(
				false /*shouldFlush*/,
				testFiles,
				getCompilerOptions(),
				expectedErrorLog,
				false /*skipJavac*/);
	}
	/** Test with JDT null annotations, expecting a null-error from ecj, none from javac. */
	void runNegativeTestWithLibs(boolean shouldFlushOutputDirectory, String[] testFiles, Map customOptions, String expectedErrorLog) {
		runNegativeTestWithLibs(
				shouldFlushOutputDirectory,
				testFiles,
				customOptions,
				expectedErrorLog,
				// runtime options
			    false);
	}
	/** Test with JDT null annotations, expecting a null-error from ecj, none from javac. */
	void runNegativeTestWithLibs(boolean shouldFlushOutputDirectory, String[] testFiles, Map customOptions,
			String expectedErrorLog, boolean skipJavaC) {
		runNegativeTest(
				shouldFlushOutputDirectory,
				testFiles,
				this.LIBS,
				customOptions,
				expectedErrorLog,
				// runtime options
				skipJavaC ? JavacTestOptions.SKIP :
			    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	void runNegativeTestWithLibs(String[] testFiles, Map customOptions, String expectedErrorLog) {
		runNegativeTestWithLibs(false /* flush output directory */,	testFiles, customOptions, expectedErrorLog);
	}
	void runNegativeTestWithLibs(String[] testFiles, Map customOptions, String expectedErrorLog, boolean skipJavac) {
		runNegativeTestWithLibs(false /* flush output directory */,	testFiles, customOptions, expectedErrorLog, skipJavac);
	}
	void runConformTestWithLibs(String[] testFiles, Map customOptions, String expectedCompilerLog) {
		runConformTestWithLibs(true /* flush output directory */, testFiles, customOptions, expectedCompilerLog);
	}
	void runConformTestWithLibs(String[] testFiles, Map customOptions, String expectedCompilerLog, String expectedOutput) {
		runConformTestWithLibs(true/* flush output directory */, testFiles, customOptions, expectedCompilerLog, expectedOutput);
	}
	void runConformTestWithLibs(boolean shouldFlushOutputDirectory, String[] testFiles, Map customOptions,
								String expectedCompilerLog, String expectedOutput) {
		runConformTest(
				shouldFlushOutputDirectory,
				testFiles,
				this.LIBS,
				customOptions,
				expectedCompilerLog,
				expectedOutput,
				"",/* expected error */
			    JavacTestOptions.DEFAULT);
	}
	void runConformTestWithLibs(boolean shouldFlushOutputDirectory, String[] testFiles, Map customOptions, String expectedCompilerLog) {
		runConformTest(
				shouldFlushOutputDirectory,
				testFiles,
				this.LIBS,
				customOptions,
				expectedCompilerLog,
				"",/* expected output */
				"",/* expected error */
			    JavacTestOptions.DEFAULT);
	}
	/** Test with JDT null annotations, expecting a null-warning from ecj, none from javac. */
	void runWarningTestWithLibs(boolean shouldFlushOutputDirectory, String[] testFiles,
				Map customOptions, String expectedCompilerLog)
	{
		runWarningTestWithLibs(shouldFlushOutputDirectory, testFiles, customOptions, expectedCompilerLog, "");
	}
	void runWarningTestWithLibs(boolean shouldFlushOutputDirectory, String[] testFiles,
				Map customOptions, String expectedCompilerLog, String expectedOutput)
	{
		runConformTest(
				shouldFlushOutputDirectory,
				testFiles,
				this.LIBS,
				customOptions,
				expectedCompilerLog,
				expectedOutput,
				"",/* expected error */
				JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	}
	void runConformTest(String[] testFiles, Map customOptions, String expectedOutputString) {
		runConformTest(
				testFiles,
				expectedOutputString,
				null /*classLibraries*/,
				true /*shouldFlushOutputDirectory*/,
				null /*vmArguments*/,
				customOptions,
				null /*customRequestor*/);

	}
}
