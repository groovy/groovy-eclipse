/*******************************************************************************
 * Copyright (c) 2010, 2012 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.osgi.framework.Bundle;

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

	public AbstractNullAnnotationTest(String name) {
		super(name);
	}

	/**
	 * @deprecated indirectly uses deprecated class PackageAdmin
	 */
	protected void setUp() throws Exception {
		super.setUp();
		if (this.LIBS == null) {
			String[] defaultLibs = getDefaultClassPaths();
			int len = defaultLibs.length;
			this.LIBS = new String[len+1];
			System.arraycopy(defaultLibs, 0, this.LIBS, 0, len);
			String version = this.complianceLevel >= ClassFileConstants.JDK1_8 ? "[2.0.0,3.0.0)" : "[1.1.0,2.0.0)";
			Bundle[] bundles = org.eclipse.jdt.core.tests.compiler.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", version);
			File bundleFile = FileLocator.getBundleFile(bundles[0]);
			if (bundleFile.isDirectory())
				this.LIBS[len] = bundleFile.getPath()+"/bin";
			else
				this.LIBS[len] = bundleFile.getPath();
		}
	}
	
	// Conditionally augment problem detection settings
	static boolean setNullRelatedOptions = true;
	
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
			// leave other new options at these defaults:
//			defaultOptions.put(CompilerOptions.OPTION_ReportNullContractViolation, JavaCore.ERROR);
//			defaultOptions.put(CompilerOptions.OPTION_ReportPotentialNullContractViolation, JavaCore.ERROR);
//			defaultOptions.put(CompilerOptions.OPTION_ReportNullContractInsufficientInfo, CompilerOptions.WARNING);

//			defaultOptions.put(CompilerOptions.OPTION_NullableAnnotationName, "org.eclipse.jdt.annotation.Nullable");
//			defaultOptions.put(CompilerOptions.OPTION_NonNullAnnotationName, "org.eclipse.jdt.annotation.NonNull");
	    }
	    return defaultOptions;
	}
	void runNegativeTestWithLibs(String[] testFiles, String expectedErrorLog) {
		runNegativeTest(
				testFiles,
				expectedErrorLog,
				this.LIBS,
				false /*shouldFlush*/);
	}
	void runNegativeTestWithLibs(boolean shouldFlushOutputDirectory, String[] testFiles, Map customOptions, String expectedErrorLog) {
		runNegativeTest(
				shouldFlushOutputDirectory,
				testFiles,
				this.LIBS,
				customOptions,
				expectedErrorLog,
				// runtime options
			    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	void runNegativeTestWithLibs(String[] testFiles, Map customOptions, String expectedErrorLog) {
		runNegativeTestWithLibs(false /* flush output directory */,	testFiles, customOptions, expectedErrorLog);
	}
	void runConformTestWithLibs(String[] testFiles, Map customOptions, String expectedCompilerLog) {
		runConformTestWithLibs(false /* flush output directory */, testFiles, customOptions, expectedCompilerLog);
	}
	void runConformTestWithLibs(String[] testFiles, Map customOptions, String expectedCompilerLog, String expectedOutput) {
		runConformTest(
				false, /* flush output directory */
				testFiles,
				this.LIBS,
				customOptions,
				expectedCompilerLog,
				expectedOutput,
				"",/* expected error */
			    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
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
			    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
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
