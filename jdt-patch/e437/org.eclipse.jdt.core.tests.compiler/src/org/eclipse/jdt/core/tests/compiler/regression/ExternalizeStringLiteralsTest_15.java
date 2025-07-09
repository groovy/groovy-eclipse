/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
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
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ExternalizeStringLiteralsTest_15 extends AbstractRegressionTest {

private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 15");

static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 6 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public ExternalizeStringLiteralsTest_15(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_15);
}

public void test001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);

	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"    String x = \"\"\"\n" +
			"        abcdefg\n" +
			"        hijklmn\n" +
			"        \"\"\";\n" +
			"}"
		},
		null,
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	String x = \"\"\"\n" +
		"        abcdefg\n" +
		"        hijklmn\n" +
		"        \"\"\";\n" +
		"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n",
		JAVAC_OPTIONS);
}
public void test002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);

	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"    String x = \"\"\"\n" +
			"        abcdefg\n" +
			"        hijklmn\n" +
			"        \"\"\"; //$NON-NLS-1$ //$NON-NLS-2$\n" +
			"}"
		},
		null,
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	\"\"\"; //$NON-NLS-1$ //$NON-NLS-2$\n" +
		"	                   ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n",
		JAVAC_OPTIONS);
}
public void test003() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);

	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"    String x = \"\"\"\n" +
			"        abcdefg\n" +
			"        hijklmn\n" +
			"        \"\"\";\n" +
			"    @SuppressWarnings(\"nls\")\n" +
			"    void foo() {\n" +
			"        String x2 = \"\"\"\n" +
			"            abcdefg\n" +
			"            hijklmn\n" +
			"            \"\"\";\n" +
			"    }\n" +
			"}"
		},
		null,
		customOptions,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	String x = \"\"\"\n" +
		"        abcdefg\n" +
		"        hijklmn\n" +
		"        \"\"\";\n" +
		"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n",
		JAVAC_OPTIONS);
}
public void test004() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"class X {\n" +
			"\n" +
			"	void foo() {\n" +
			"		String s6 = \"\"\"\n" +
			"			SUCCESS\n" +
			"			\"\"\";\n" +
			"		System.out.println(s6);\n" +
			"	}\n" +
			"}",
		},
		null, customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	String s6 = \"\"\"\n" +
		"			SUCCESS\n" +
		"			\"\"\";\n" +
		"	            ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public void test005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		String s6 = \"\"\"\n" +
			"			SUCCESS\n" +
			"			\"\"\"; //$NON-NLS-1$\n" +
			"		System.out.println(s6);\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS",
		null,
		true,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=237245
public void test006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	@Annot({\n" +
			"		@A(name = \"\"\"\n" +
			"           name\n" +
			"           \"\"\", //$NON-NLS-1$\n" +
			" 		value = \"\"\"\n" +
			"           Test\n" +
			"           \"\"\") //$NON-NLS-1$\n" +
			"	})\n" +
			"	@X2(\"\"\"\n" +
			"   \"\"\") //$NON-NLS-1$\n" +
			"	void foo() {\n" +
			"	}\n" +
			"}\n" +
			"@interface Annot {\n" +
			"	A[] value();\n" +
			"}\n" +
			"@interface A {\n" +
			"	String name();\n" +
			"	String value();\n" +
			"}\n" +
			"@interface X2 {\n" +
			"	String value();\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	\"\"\", //$NON-NLS-1$\n" +
		"	     ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	\"\"\") //$NON-NLS-1$\n" +
		"	     ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 11)\n" +
		"	\"\"\") //$NON-NLS-1$\n" +
		"	     ^^^^^^^^^^^^^\n" +
		"Unnecessary $NON-NLS$ tag\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
public static Class testClass() {
	return ExternalizeStringLiteralsTest_15.class;
}
}
