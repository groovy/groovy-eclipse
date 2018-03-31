/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NonFatalErrorTest extends AbstractRegressionTest {
	public NonFatalErrorTest(String name) {
		super(name);
	}
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 7 };
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
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319626
	public void test006() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.DISABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"public class X {\n" +
				"	{     }\n" +
				"	static {  }\n" +
				" 	X() { }\n" +
				" 	X(int a) {}\n" +
				" 	public void foo() {}\n" +
				"	public static void main(String argv[]) {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n"
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			// compiler results
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	{     }\n" + 
			"	^^^^^^^\n" + 
			"Empty block should be documented\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	static {  }\n" + 
			"	       ^^^^\n" + 
			"Empty block should be documented\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	X(int a) {}\n" + 
			"	         ^^\n" + 
			"Empty block should be documented\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 6)\n" + 
			"	public void foo() {}\n" + 
			"	                  ^^\n" + 
			"Empty block should be documented\n" + 
			"----------\n",
			// runtime results
			"SUCCESS" /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
	public void test007() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) {
			return;
		}
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError,
				CompilerOptions.ENABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal,
				CompilerOptions.ERROR);
		customOptions.put(CompilerOptions.OPTION_SuppressWarnings,
				CompilerOptions.ENABLED);
		customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors,
				CompilerOptions.ENABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUnusedWarningToken,
				CompilerOptions.ERROR);
		runConformTest(
				new String[] { /* test files */
						"X.java",
						"public class X {\n" +
								"        @SuppressWarnings(\"unused\")\n" +
								"        static void foo() {\n" +
								"            String s = null;\n" +
								"            System.out.println(\"SUCCESS\");\n" +
								"        }\n" +
								"        public static void main(String argv[]) {\n" +
								"            foo();\n" +
								"        }\n" +
								"}"
				},
				"SUCCESS" /* expected output string */,
				null /* no class libraries */,
				true,
				null,
				customOptions /* custom options */,
				// compiler results
				null /* do not check error string */);
	}
	public void testImportUnresolved() {
		Map<String,String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.ERROR);
		runNegativeTest(
			true, // flush dir
			new String[] {
				"X.java",
				"import com.bogus.Missing;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().test();\n" +
				"	}\n" +
				"	void test() {\n" +
				"		System.out.println(\"OK\");\n" +
				"	}\n" +
				"}\n"
			},
			null, // libs
			options,
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	import com.bogus.Missing;\n" + 
			"	       ^^^^^^^^^\n" + 
			"The import com.bogus cannot be resolved\n" + 
			"----------\n",
			"OK",
			"",
			JavacTestOptions.SKIP);
	}
	public void testImportUnresolved_fatal() {
		Map<String,String> options = getCompilerOptions();
		try {
			options.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.ERROR);
			options.put(JavaCore.COMPILER_PB_FATAL_OPTIONAL_ERROR, JavaCore.ENABLED);
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
			runNegativeTest(
					true, // flush dir
					new String[] {
							"p/Z.java",
							"package p;\n" +
									"public class Z {\n" +
									"	public static void main(String[] args) throws Exception {\n" +
									"		try {\n" +
									"			Class.forName(\"X\").newInstance();\n" + // forward reference, workaround by using reflection
									"		} catch (java.lang.Error e) {\n" +
									"			System.err.println(e.getMessage());\n" +
									"		}\n" +
									"	}\n" +
									"}\n",
									"X.java",
									"import com.bogus.Missing;\n" +
											"public class X {\n" +
											"	public static void main(String[] args) {\n" +
											"		new X().test();\n" +
											"	}\n" +
											"	void test() {\n" +
											"		System.out.println(\"OK\");\n" +
											"	}\n" +
											"}\n"
					},
					null, // libs
					options,
					"----------\n" + 
							"1. ERROR in X.java (at line 1)\n" + 
							"	import com.bogus.Missing;\n" + 
							"	       ^^^^^^^^^\n" + 
							"The import com.bogus cannot be resolved\n" + 
							"----------\n",
							"",
							"Unresolved compilation problem: \n" + 
									"	The import com.bogus cannot be resolved",
									JavacTestOptions.SKIP);
		} finally {
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		}
	}
	public void testPackageConflict() {
		Map<String,String> options = getCompilerOptions();
		try {
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);

			runNegativeTest(
					true, // flush dir
					new String[] {
							"p/z.java",
							"package p;\n" +
									"public class z {\n" +
									"	public static void main(String[] args) throws Exception {\n" +
									"		try {\n" +
									"			Class.forName(\"p.z.X\").newInstance();\n" +
									"		} catch (ClassNotFoundException e) {\n" +
									"			System.out.println(e.getClass().getName());\n" +
									"		}\n" +
									"	}\n" +
									"}\n",
									"p/z/X.java",
									"package p.z;\n" +
											"public class X {\n" +
											"	public X() {\n" +
											"		System.out.println(\"OK\");\n" +
											"	}\n" +
											"}\n",
					},
					null, // libs
					options,
					"----------\n" + 
							"1. ERROR in p\\z\\X.java (at line 1)\n" + 
							"	package p.z;\n" + 
							"	        ^^^\n" + 
							"The package p.z collides with a type\n" + 
							"----------\n",
							"java.lang.ClassNotFoundException", // cannot generate code in presence of the above error
							"",
							JavacTestOptions.SKIP);
		} finally {
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		}
	}
	public void testImportVariousProblems() {
		Map<String,String> options = getCompilerOptions();
		try {
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);

			runNegativeTest(
					true, // flush dir
					new String[] {
							"p/Z.java",
							"package p;\n" +
									"public class Z {\n" +
									"	public static void main(String[] args) throws Exception {\n" +
									"		try {\n" +
									"			Class.forName(\"X\").newInstance();\n" + // forward reference, workaround by using reflection
									"		} catch (ClassNotFoundException e) {\n" +
									"			System.out.println(e.getClass().getName());\n" +
									"		}\n" +
									"	}\n" +
									"}\n",
									"p1/Y.java",
									"package p1;\n" +
											"public class Y {}\n",
											"p2/Y.java",
											"package p2;\n" +
													"public class Y {}\n",
													"X.java",
													"import java.util;\n" +
															"import p.Z;\n" +
															"import p1.Y;\n" +
															"import p2.Y;\n" +
															"public class X {\n" +
															"	public X() {\n" +
															"		System.out.println(\"OK\");\n" +
															"	}\n" +
															"}\n" +
															"class Z {}\n"
					},
					null, // libs
					options,
					"----------\n" + 
							"1. ERROR in X.java (at line 1)\n" + 
							"	import java.util;\n" + 
							"	       ^^^^^^^^^\n" + 
							"Only a type can be imported. java.util resolves to a package\n" + 
							"----------\n" + 
							"2. ERROR in X.java (at line 2)\n" + 
							"	import p.Z;\n" + 
							"	       ^^^\n" + 
							"The import p.Z conflicts with a type defined in the same file\n" + 
							"----------\n" + 
							"3. ERROR in X.java (at line 4)\n" + 
							"	import p2.Y;\n" + 
							"	       ^^^^\n" + 
							"The import p2.Y collides with another import statement\n" + 
							"----------\n",
							"OK",
							"",
							JavacTestOptions.SKIP);
		} finally {
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		}
	}
	public void testImportStaticProblems() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses static imports
		Map<String,String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
		runNegativeTest(
			true, // flush dir
			new String[] {
				"p/Z.java",
				"package p;\n" +
				"public class Z {\n" +
				"	public static void main(String[] args) throws Exception {\n" +
				"		try {\n" +
				"			Class.forName(\"X\").newInstance();\n" + // forward reference, workaround by using reflection
				"		} catch (ClassNotFoundException e) {\n" +
				"			System.out.println(e.getClass().getName());\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
				"p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"	static int f;\n" +
				"}\n",
				"X.java",
				"import static p1.Y;\n" +
				"import static p1.Y.f;\n" +
				"public class X {\n" +
				"	public X() {\n" +
				"		System.out.println(\"OK\");\n" +
				"	}\n" +
				"}\n" +
				"class Z {}\n"
			},
			null, // libs
			options,
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	import static p1.Y;\n" + 
			"	              ^^^^\n" + 
			"The static import p1.Y must be a field or member type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	import static p1.Y.f;\n" + 
			"	              ^^^^^^\n" + 
			"The field Y.p1.Y.f is not visible\n" + 
			"----------\n",
			"OK",
			"",
			JavacTestOptions.SKIP);
	}
	public void testDuplicateImports1() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses static imports
		runConformTest(
			new String[] {
				"Test.java",
				"import java.lang.Character.Subset;\n" + 
				"import static java.lang.Character.Subset;\n" + 
				"\n" + 
				"public class Test {\n" + 
				"	Subset s = null;\n" + 
				"}\n"
			});
	}
	public void testDuplicateImports2() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses static imports
		runConformTest(
			new String[] {
				"Test.java",
				"import static java.awt.geom.Line2D.Double;\n" + 
				"import static java.awt.geom.Line2D.Double;\n" + 
				"\n" + 
				"public class Test {\n" + 
				"	Double d = null;\n" + 
				"}\n"
			});
	}
	public void testDuplicateImports3() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses static imports
		runNegativeTest(
			new String[] {
				"Test.java",
				// JLS doesn't really allow this duplication, but also javac defers the error to the use site, see:
				// https://bugs.openjdk.java.net/browse/JDK-8133619?focusedCommentId=14133759&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14133759
				"import static java.awt.geom.Line2D.Double;\n" + 
				"import static java.awt.geom.Point2D.Double;\n" + 
				"\n" + 
				"public class Test {\n" +
				"	Double d = null;\n" + 
				"}\n"
			},
			(this.complianceLevel < ClassFileConstants.JDK1_8
			?
				"----------\n" + 
				"1. ERROR in Test.java (at line 2)\n" + 
				"	import static java.awt.geom.Point2D.Double;\n" + 
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"The import java.awt.geom.Point2D.Double collides with another import statement\n" + 
				"----------\n"
			:
				"----------\n" + 
				"1. ERROR in Test.java (at line 5)\n" + 
				"	Double d = null;\n" + 
				"	^^^^^^\n" + 
				"The type Double is ambiguous\n" + 
				"----------\n"));
	}
}
