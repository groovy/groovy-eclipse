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
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;

public class PreviewFeatureTest extends AbstractRegressionTest9 {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test001"};
	}

	public static Class<?> testClass() {
		return PreviewFeatureTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}
	public PreviewFeatureTest(String testName){
		super(testName);
	}
	@Override
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> options = super.getCompilerOptions();
		if (isJRE17Plus) {
			options.put(CompilerOptions.OPTION_Release, CompilerOptions.ENABLED);
		}
		return options;
	}
	private String[] getClasspathWithPreviewAPI() {
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		String jarPath = LIB_DIR + "/lib1.jar";
		try {
			Util.createJar(
				new String[] {
						"jdk/internal/javac/PreviewFeature.java",
						"package jdk.internal.javac;\n"
						+ "import java.lang.annotation.*;\n"
						+ "@Target({ElementType.METHOD,\n"
						+ "         ElementType.CONSTRUCTOR,\n"
						+ "         ElementType.FIELD,\n"
						+ "         ElementType.PACKAGE,\n"
						+ "         ElementType.MODULE,\n"
						+ "         ElementType.TYPE})\n"
						+ "@Retention(RetentionPolicy.CLASS)\n"
						+ "public @interface PreviewFeature {\n"
						+ "    public Feature feature();\n"
						+ "    public enum Feature {\n"
						+ "        /**\n"
						+ "         * A key for testing.\n"
						+ "         */\n"
						+ "        TEST;\n"
						+ "    }\n"
						+ "}",
						"p/ABC.java",
						"package p;\n"
						+ "import jdk.internal.javac.PreviewFeature;\n"
						+ "@PreviewFeature(feature=PreviewFeature.Feature.TEST)\n"
						+ "public class ABC {\n"
						+ "  @PreviewFeature(feature=PreviewFeature.Feature.TEST)\n"
						+ "  public void doSomething() {}\n"
						+ "}"
				},
				jarPath,
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		String [] javaClassLibs = Util.getJavaClassLibs();
		int javaClassLibsLength;
		String [] xClassLibs = new String[(javaClassLibsLength = javaClassLibs.length) + 1];
		System.arraycopy(javaClassLibs, 0, xClassLibs, 0, javaClassLibsLength);
		xClassLibs[javaClassLibsLength] = jarPath;
		return xClassLibs;
	}
	/*
	 * Preview API, --enable-preview=false, SuppressWarning=No
	 */
	public void test001() {
		if (this.complianceLevel >= ClassFileConstants.JDK17) {
			return;
		}
		String[] classLibs = getClasspathWithPreviewAPI();
		Map<String, String> options = getCompilerOptions();
		String old = options.get(CompilerOptions.OPTION_EnablePreviews);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		try {
			runNegativeTest(
					new String[] {
							"X.java",
							"import p.*;\n"+
							"public class X {\n"+
							"    Zork z = null;\n" +
							"    ABC abc = null;\n" +
							"   public void foo () {\n"+
							"      (new ABC()).doSomething();\n"+
							"   }\n"+
							"}\n",
					},
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	Zork z = null;\n" +
					"	^^^^\n" +
					"Zork cannot be resolved to a type\n" +
					"----------\n" +
					"2. WARNING in X.java (at line 6)\n" +
					"	(new ABC()).doSomething();\n" +
					"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"You are using an API that is part of a preview feature and may be removed in future\n" +
					"----------\n",
					classLibs,
					true,
					options);
		} finally {
			options.put(CompilerOptions.OPTION_EnablePreviews, old);
		}
	}
	/*
	 * Preview API, --enable-preview=false, SuppressWarning=yes
	 */
	public void test002() {
		if (this.complianceLevel >= ClassFileConstants.JDK17) {
			return;
		}
		Map<String, String> options = getCompilerOptions();
		String old = options.get(CompilerOptions.OPTION_EnablePreviews);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		String[] classLibs = getClasspathWithPreviewAPI();
		try {
			runNegativeTest(
					new String[] {
							"X.java",
							"import p.*;\n"+
							"@SuppressWarnings(\"preview\")\n"+
							"public class X {\n"+
									"    Zork z = null;\n" +
									"    ABC abc = null;\n" +
									"   public void foo () {\n"+
									"      (new ABC()).doSomething();\n"+
									"   }\n"+
									"}\n",
					},
					"----------\n" +
					"1. ERROR in X.java (at line 4)\n" +
					"	Zork z = null;\n" +
					"	^^^^\n" +
					"Zork cannot be resolved to a type\n" +
					"----------\n",
					classLibs,
					true,
					options);
		} finally {
			options.put(CompilerOptions.OPTION_EnablePreviews, old);
		}
	}
	/*
	 * Preview API, --enable-preview=true, SuppressWarning=No
	 */
	public void test003() {
		if (this.complianceLevel < ClassFileConstants.getLatestJDKLevel())
			return;
		Map<String, String> options = getCompilerOptions();
		String old = options.get(CompilerOptions.OPTION_EnablePreviews);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		String[] classLibs = getClasspathWithPreviewAPI();
		try {
			runNegativeTest(
					new String[] {
							"X.java",
							"import p.*;\n"+
							"public class X {\n"+
									"    Zork z = null;\n" +
									"    ABC abc = null;\n" +
									"   public void foo () {\n"+
									"      (new ABC()).doSomething();\n"+
									"   }\n"+
									"}\n",
					},
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	Zork z = null;\n" +
					"	^^^^\n" +
					"Zork cannot be resolved to a type\n" +
					"----------\n",
					classLibs,
					true,
					options);
		} finally {
			options.put(CompilerOptions.OPTION_EnablePreviews, old);
		}
	}
	/*
	 * Preview API, --enable-preview=true, SuppressWarning=Yes
	 */
	public void test004() {
		if (this.complianceLevel < ClassFileConstants.getLatestJDKLevel())
			return;
		Map<String, String> options = getCompilerOptions();
		String old = options.get(CompilerOptions.OPTION_EnablePreviews);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		String[] classLibs = getClasspathWithPreviewAPI();
		try {
			runNegativeTest(
					new String[] {
							"X.java",
							"import p.*;\n"+
							"@SuppressWarnings(\"preview\")\n"+
							"public class X {\n"+
									"    Zork z = null;\n" +
									"    ABC abc = null;\n" +
									"   public void foo () {\n"+
									"      (new ABC()).doSomething();\n"+
									"   }\n"+
									"}\n",
					},
					"----------\n" +
					"1. ERROR in X.java (at line 4)\n" +
					"	Zork z = null;\n" +
					"	^^^^\n" +
					"Zork cannot be resolved to a type\n" +
					"----------\n",
					classLibs,
					true,
					options);
		} finally {
			options.put(CompilerOptions.OPTION_EnablePreviews, old);
		}
	}
	public void test005() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		Map<String, String> options = getCompilerOptions();
		String old = options.get(CompilerOptions.OPTION_EnablePreviews);
		if (this.complianceLevel == ClassFileConstants.getLatestJDKLevel())
			options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		try {
			if (this.complianceLevel < ClassFileConstants.getLatestJDKLevel())
				assertFalse(JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(new CompilerOptions(options)));
			else
				assertTrue(JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(new CompilerOptions(options)));
		} finally {
			options.put(CompilerOptions.OPTION_EnablePreviews, old);
		}
	}
}
