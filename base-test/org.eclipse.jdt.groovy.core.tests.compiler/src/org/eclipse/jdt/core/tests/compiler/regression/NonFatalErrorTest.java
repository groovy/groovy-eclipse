/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class NonFatalErrorTest extends AbstractRegressionTest {
	public NonFatalErrorTest(String name) {
		super(name);
	}
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 5 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {  
		return NonFatalErrorTest.class;
	}

	public void test001() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.DISABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */, 
			new String[] { /* test files */
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"		 public static void main(String argv[]) {\n" +
				"				 System.out.println(\"SUCCESS\");\n" +
				"		 }\n" +
				"}"
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			// compiler results
			"----------\n" + /* expected compiler log */ 
			"1. ERROR in X.java (at line 1)\n" + 
			"	import java.util.*;\n" + 
			"	       ^^^^^^^^^\n" + 
			"The import java.util is never used\n" + 
			"----------\n",
			// runtime results
			"SUCCESS" /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);		
	}
	
	public void test002() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.ENABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */, 
			new String[] { /* test files */
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"		 public static void main(String argv[]) {\n" +
				"				 System.out.println(\"SUCCESS\");\n" +
				"		 }\n" +
				"}"
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			// compiler results
			"----------\n" + /* expected compiler log */ 
			"1. ERROR in X.java (at line 1)\n" + 
			"	import java.util.*;\n" + 
			"	       ^^^^^^^^^\n" + 
			"The import java.util is never used\n" + 
			"----------\n",
			// runtime results
			"" /* expected output string */,
			"java.lang.Error: Unresolved compilation problem: \n" + /* expectedErrorString */ 
			"\n",
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);		
	}
	
	public void test003() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.DISABLED);
		customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */, 
			new String[] { /* test files */
				"X.java",
				"public class X {\n" +
				"		 public static void main(String argv[]) {\n" +
				"				 System.out.println(\"SUCCESS\");\n" +
				"		 }\n" +
				"}"
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			// compiler results
			"----------\n" + /* expected compiler log */ 
			"1. ERROR in X.java (at line 3)\n" + 
			"	System.out.println(\"SUCCESS\");\n" + 
			"	                   ^^^^^^^^^\n" + 
			"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" + 
			"----------\n",
			// runtime results
			"SUCCESS" /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);		
	}
	
	public void test004() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.DISABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */, 
			new String[] { /* test files */
				"X.java",
				"public class X {\n" +
				"		 public static void foo() {}\n" +
				"		 public static void main(String argv[]) {\n" +
				"				foo();\n" +	
				"				System.out.println(\"SUCCESS\");\n" +
				"		 }\n" +
				"}"
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			// compiler results
			"----------\n" + /* expected compiler log */ 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public static void foo() {}\n" + 
			"	                         ^^\n" + 
			"Empty block should be documented\n" + 
			"----------\n",
			// runtime results
			"SUCCESS" /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);		
	}
	
	public void test005() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.ENABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */, 
			new String[] { /* test files */
				"X.java",
				"public class X {\n" +
				"		 public static void foo() {}\n" +
				"		 public static void main(String argv[]) {\n" +
				"				foo();\n" +	
				"				System.out.println(\"SUCCESS\");\n" +
				"		 }\n" +
				"}"
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			// compiler results
			"----------\n" + /* expected compiler log */ 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public static void foo() {}\n" + 
			"	                         ^^\n" + 
			"Empty block should be documented\n" + 
			"----------\n",
			// runtime results
			"" /* expected output string */,
			"java.lang.Error: Unresolved compilation problem: \n" + /* expectedErrorString */ 
			"	Empty block should be documented\n" +  
			"\n",
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);		
	}
}
