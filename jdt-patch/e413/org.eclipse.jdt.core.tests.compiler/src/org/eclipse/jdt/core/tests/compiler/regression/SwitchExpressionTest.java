/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
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

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.JavacHasABug;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SwitchExpressionTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_NAMES = new String[] { "testBug543240_1" };
	}
	
	public static Class<?> testClass() {
		return SwitchExpressionTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_12);
	}
	public SwitchExpressionTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_12); // FIXME
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_12);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_12);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("12");
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview("12"));
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("12");
		runner.runWarningTest();
	}

	public void testSimpleExpressions() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static int twice(int i) {\n" +
						"		int tw = switch (i) {\n" +
						"			case 0 -> i * 0;\n" +
						"			case 1 -> 2;\n" +
						"			default -> 3;\n" +
						"		};\n" +
						"		return tw;\n" +
						"	}\n" +
						"	public static void main(String... args) {\n" +
						"		System.out.print(twice(3));\n" +
						"	}\n" +
						"}\n"
				},
				"3");
	}
	public void testSwitchExpression_531714_002() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
								"	static int twice(int i) throws Exception {\n"+
								"		int tw = switch (i) {\n"+
								"			case 0 -> 0;\n"+
								"			case 1 -> { \n"+
								"				System.out.println(\"do_not_print\");\n"+
								"				break 1;\n"+
								"			} \n"+
								"			case 3 -> throw new Exception();\n"+
								"			default -> throw new Exception();\n"+
								"		};\n"+
								"		return tw;\n"+
								"	}\n"+
								"	public static void main(String[] args) {\n"+
								"		try {\n"+
								"		    try {\n"+
								"				System.out.print(twice(3));\n"+
								"			} catch (Exception e) {\n"+
								"				System.out.print(\"Got Exception - expected\");\n"+
								"			}\n"+
								"		} catch (Exception e) {\n"+
								"		System.out.print(\"Got Exception\");\n"+
								"		}\n"+
								"	}\n"+
								"}\n"
				},
				"Got Exception - expected");
	}
	public void testBug531714_error_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    twice(1);\n" +
				"  }\n" +
				"	public static int twice(int i) {\n" +
				"		int tw = switch (i) {\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	int tw = switch (i) {\n" + 
			"		};\n" + 
			"	         ^^^^^^^^^^^^^^^^\n" + 
			"A switch expression should have a non-empty switch block\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	int tw = switch (i) {\n" + 
			"	                 ^\n" + 
			"A switch expression should have a default case\n" + 
			"----------\n");
	}
	public void testBug531714_error_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    twice(1);\n" +
				"  }\n" +
				"	public static int twice(int i) {\n" +
				"		int tw = switch (i) {\n" +
				"			case 0 -> 0;\n" +
				"			case 1 -> { \n" +
				"				System.out.println(\"heel\");\n" +
				"				break 1;\n" +
				"			} \n" +
				"			case \"hello\" -> throw new java.io.IOException(\"hello\");\n" +
				"			default -> throw new java.io.IOException(\"world\");\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\n" + 
			"	case \"hello\" -> throw new java.io.IOException(\"hello\");\n" + 
			"	     ^^^^^^^\n" + 
			"Type mismatch: cannot convert from String to int\n" + 
			"----------\n");
	}
	public void testBug531714_error_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    twice(1);\n" +
				"  }\n" +
				"	public static int twice(int i) {\n" +
				"		int tw = switch (i) {\n" +
				"			case 0 -> 0;\n" +
				"			case 1 -> { \n" +
				"				System.out.println(\"heel\");\n" +
				"				break 1;\n" +
				"			} \n" +
				"		    case 2 -> 2;\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	int tw = switch (i) {\n" + 
			"	                 ^\n" + 
			"A switch expression should have a default case\n" + 
			"----------\n");
	}
	/**
	 * Add a test case for enum
	 * If the type of the selector expression is an enum type, 
	 * then the set of all the case constants associated with the switch block
	 *  must contain all the enum constants of that enum type
	 *  Add a missing enum test case
	 */
	public void _testBug531714_error_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    int x, y;\n" +
				"    I i = () -> {\n" +
				"      int z = 10;\n" +
				"    };\n" +
				"    i++;\n" +
				"  }\n" +
				"	public static int twice(int i) {\n" +
				"		int tw = switch (i) {\n" +
				"			case 0 -> 0;\n" +
				"			case 1 -> { \n" +
				"				System.out.println(\"heel\");\n" +
				"				break 1;\n" +
				"			} \n" +
				"		//	case 2 -> 2;\n" +
				"			case \"hello\" -> throw new IOException(\"hello\");\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	int tw = switch (i) {\n" + 
			"	      ^^^^^\n" + 
			" The switch expression should have a default case\n" + 
			"----------\n");
	}
	/*
	 * should compile - test for adding additional nesting in variables
	 * dev note: ref consumeToken().case Switch 
	 */
	public void testBug531714_error_007() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"	static int foo(int i) {\n"+
						"		int tw = \n"+
						"		switch (i) {\n"+
						"			case 1 -> \n"+
						"			 {\n"+
						" 				int z = 100;\n"+
						" 				break z;\n"+
						"			}\n"+
						"			default -> {\n"+
						"				break 12;\n"+
						"			}\n"+
						"		};\n"+
						"		return tw;\n"+
						"	}\n"+
						"	public static void main(String[] args) {\n"+
						"		System.out.print(foo(1));\n"+
						"	}\n"+
						"}\n"
				},
				"100");
	}
	public void testBug531714_008() {
		Map<String, String> disablePreviewOptions = getCompilerOptions();
		disablePreviewOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
				"	static int twice(int i) {\n" +
				"		int tw = switch (i) {\n" +
				"			case 0 -> i * 0;\n" +
				"			case 1 -> 2;\n" +
				"			default -> 3;\n" +
				"		};\n" +
				"		return tw;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(twice(3));\n" +
				"	}\n" +
				"}\n",
		};

		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	int tw = switch (i) {\n" + 
				"			case 0 -> i * 0;\n" + 
				"			case 1 -> 2;\n" + 
				"			default -> 3;\n" + 
				"		};\n" + 
				"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Switch Expressions is a preview feature and disabled by default. Use --enable-preview to enable\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	case 0 -> i * 0;\n" + 
				"	^^^^^^\n" + 
				"Case Labels with '->' is a preview feature and disabled by default. Use --enable-preview to enable\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	case 1 -> 2;\n" + 
				"	^^^^^^\n" + 
				"Case Labels with '->' is a preview feature and disabled by default. Use --enable-preview to enable\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	default -> 3;\n" + 
				"	^^^^^^^\n" + 
				"Case Labels with '->' is a preview feature and disabled by default. Use --enable-preview to enable\n" + 
				"----------\n";

		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				disablePreviewOptions);
	}
	public void testBug543667_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void bar(int  i) {\n" +
				"		switch (i) {\n" +
				"		case 1 -> System.out.println(\"hello\");\n" +
				"		default -> System.out.println(\"DEFAULT\");\n" +
				"		}\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		bar(1);\n" +
				"	}\n" +
				"}\n"
			},
			"hello");
	}
	public void testBug531714_009() {
		Map<String, String> disablePreviewOptions = getCompilerOptions();
		disablePreviewOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
				"	static int twice(int i) {\n" +
				"		switch (i) {\n" +
				"			case 0 -> i * 0;\n" +
				"			case 1 -> 2;\n" +
				"			default -> 3;\n" +
				"		}\n" +
				"		return 0;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(twice(3));\n" +
				"	}\n" +
				"}\n",
		};

		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	case 0 -> i * 0;\n" + 
				"	^^^^^^\n" + 
				"Case Labels with \'->\' is a preview feature and disabled by default. Use --enable-preview to enable\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	case 1 -> 2;\n" + 
				"	^^^^^^\n" + 
				"Case Labels with \'->\' is a preview feature and disabled by default. Use --enable-preview to enable\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	default -> 3;\n" + 
				"	^^^^^^^\n" + 
				"Case Labels with \'->\' is a preview feature and disabled by default. Use --enable-preview to enable\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				disablePreviewOptions);
	}
	public void testBug531714_010() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.ERROR);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
				"	static int twice(int i) {\n" +
				"		switch (i) {\n" +
				"			default -> 3;\n" +
				"		}\n" +
				"		return 0;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(twice(3));\n" +
				"	}\n" +
				"}\n",
		};

		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	default -> 3;\n" + 
				"	^^^^^^^\n" + 
				"You are using a preview language feature that may or may not be supported in a future release\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	default -> 3;\n" + 
				"	           ^\n" + 
				"Invalid expression as statement\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	public void testBug531714_011() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
				"	@SuppressWarnings(\"preview\")\n" +
				"	static int twice(int i) {\n" +
				"		switch (i) {\n" +
				"			default -> 3;\n" +
				"		}\n" +
				"		return 0;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(twice(3));\n" +
				"	}\n" +
				"}\n",
		};

		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	default -> 3;\n" + 
				"	           ^\n" + 
				"Invalid expression as statement\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug531714_012() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		String release = options.get(CompilerOptions.OPTION_Release);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
		try {
			String[] testFiles = new String[] {
					"X.java",
					"public class X {\n" +
					"	static int twice(int i) {\n" +
					"		switch (i) {\n" +
					"			default -> 3;\n" +
					"		}\n" +
					"		return 0;\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.print(twice(3));\n" +
					"	}\n" +
					"}\n",
			};

			String expectedProblemLog =
					"----------\n" + 
					"1. ERROR in X.java (at line 4)\n" + 
					"	default -> 3;\n" + 
					"	^^^^^^^\n" + 
					"The preview feature Case Labels with \'->\' is only available with source level 12 and above\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 4)\n" + 
					"	default -> 3;\n" + 
					"	           ^\n" + 
					"Invalid expression as statement\n" + 
					"----------\n";
			this.runNegativeTest(
					testFiles,
					expectedProblemLog,
					null,
					true,
					options);
		} finally {
			options.put(CompilerOptions.OPTION_Source, release);
		}
	}
	public void testBug531714_013() {
			String[] testFiles = new String[] {
					"X.java",
					"public class X {\n" +
					"	public static int foo(int i) {\n" +
					"		int v;\n" +
					"		int t = switch (i) {\n" +
					"		case 0 : {\n" +
					"			break 0;\n" +
					"		}\n" +
					"		default :v = 2;\n" +
					"		};\n" +
					"		return t;\n" +
					"	}\n" +
					"	\n" +
					"	public boolean bar() {\n" +
					"		return true;\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(foo(3));\n" +
					"	}\n" +
					"}\n",
			};

			String expectedProblemLog =
					"----------\n" + 
					"1. ERROR in X.java (at line 8)\n" + 
					"	default :v = 2;\n" + 
					"	         ^^^^^\n" + 
					"A switch labeled block in a switch expression should not complete normally\n" + 
					"----------\n";
			this.runNegativeTest(
					testFiles,
					expectedProblemLog,
					null,
					true,
					getCompilerOptions());
	}
	public void testBug531714_014() {
		// switch expression is not a Primary
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.testFiles = new String[] {
			"X.java",
			"public class X {\n" +
			"	void test(int i) {\n" +
			"		System.out.println(switch (i) {\n" +
			"			case 1 -> \"one\";\n" +
			"			default -> null;\n" +
			"		}.toLowerCase());\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().test(1);\n" +
			"	}\n" +
			"}\n"
		};
		runner.expectedCompilerLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	}.toLowerCase());\n" + 
				"	 ^\n" + 
				"Syntax error on token \".\", , expected\n" + 
				"----------\n";
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("12");
		runner.runNegativeTest();
	}
	public void testBug543673_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int foo(Day day) {\n" +
				"\n" +
				"		var len= switch (day) {\n" +
				"			case SUNDAY-> 6;\n" +
				"			default -> 10;\n" +
				"		};\n" +
				"\n" +
				"		return len;\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(Day.SUNDAY));\n" +
				"	}\n" +
				"}\n" +
				"enum Day {\n" +
				"	MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;\n" +
				"}\n"
			},
			"6");
	}
	/*
	 * A simple multi constant case statement, compiled and run as expected
	 */
	public void testBug543240_1() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
						"public static void bar(Day day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATURDAY, SUNDAY: \n" + 
						"			System.out.println(Day.SUNDAY);\n" + 
						"			break;\n" + 
						"		case MONDAY : System.out.println(Day.MONDAY);\n" + 
						"					break;\n" + 
						"		}\n" + 
						"	}" +
						"	public static void main(String[] args) {\n" +
						"		bar(Day.SATURDAY);\n" +
						"	}\n" +
						"}\n" +
						"enum Day { SATURDAY, SUNDAY, MONDAY;}",
		};

		String expectedOutput =
				"SUNDAY";
		this.runConformTest(
				testFiles,
				expectedOutput);
	}
	/*
	 * A simple multi constant case statement, compiler reports missing enum constants
	 */
	public void testBug543240_1a() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
						"	public static void main(String[] args) {\n" + 
						"	}\n" + 
						"public static void bar(Day day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATURDAY, SUNDAY: \n" + 
						"			System.out.println(Day.SUNDAY);\n" + 
						"			break;\n" + 
						"		case MONDAY : System.out.println(Day.MONDAY);\n" + 
						"					break;\n" + 
						"		}\n" + 
						"	}" +
						"}\n" +
						"enum Day { SATURDAY, SUNDAY, MONDAY, TUESDAY;}",
		};

		String expectedProblemLog =
						"----------\n" + 
						"1. WARNING in X.java (at line 5)\n" + 
						"	switch (day) {\n" + 
						"	        ^^^\n" + 
						"The enum constant TUESDAY needs a corresponding case label in this enum switch on Day\n" + 
						"----------\n";
		this.runWarningTest(
				testFiles,
				expectedProblemLog,
				options);
	}
	/*
	 * A simple multi constant case statement with duplicate enums
	 */
	public void testBug543240_2() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
						"public static void bar(Day day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATURDAY, SUNDAY: \n" + 
						"			System.out.println(Day.SUNDAY);\n" + 
						"			break;\n" + 
						"		case SUNDAY : System.out.println(Day.SUNDAY);\n" + 
						"					break;\n" + 
						"		}\n" + 
						"	}" +
						"	public static void main(String[] args) {\n" +
						"		bar(Day.SATURDAY);\n" +
						"	}\n" +
						"}\n" +
						"enum Day { SATURDAY, SUNDAY, MONDAY;}",
		};

		String expectedProblemLog =
				"----------\n" + 
						"1. ERROR in X.java (at line 4)\n" + 
						"	case SATURDAY, SUNDAY: \n" + 
						"	^^^^^^^^^^^^^^^^^^^^^\n" + 
						"Duplicate case\n" + 
						"----------\n" + 
						"2. ERROR in X.java (at line 7)\n" + 
						"	case SUNDAY : System.out.println(Day.SUNDAY);\n" + 
						"	^^^^^^^^^^^\n" + 
						"Duplicate case\n" + 
						"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	/*
	 * A simple multi constant case statement with duplicate enums
	 */
	public void testBug543240_2a() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
						"public static void bar(Day day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATURDAY, SUNDAY: \n" + 
						"			System.out.println(Day.SUNDAY);\n" + 
						"			break;\n" + 
						"		case SUNDAY, SATURDAY : \n" +
						"			System.out.println(Day.SUNDAY);\n" + 
						"			break;\n" + 
						"		}\n" + 
						"	}" +
						"}\n" +
						"enum Day { SATURDAY, SUNDAY, MONDAY;}",
		};

		String expectedProblemLog =
						"----------\n" + 
						"1. WARNING in X.java (at line 3)\n" + 
						"	switch (day) {\n" + 
						"	        ^^^\n" + 
						"The enum constant MONDAY needs a corresponding case label in this enum switch on Day\n" + 
						"----------\n" + 
						"2. ERROR in X.java (at line 4)\n" + 
						"	case SATURDAY, SUNDAY: \n" + 
						"	^^^^^^^^^^^^^^^^^^^^^\n" +
						"Duplicate case\n" + 
						"----------\n" + 
						"3. ERROR in X.java (at line 7)\n" + 
						"	case SUNDAY, SATURDAY : \n" + 
						"	^^^^^^^^^^^^^^^^^^^^^\n" + 
						"Duplicate case\n" + 
						"----------\n" + 
						"4. ERROR in X.java (at line 7)\n" + 
						"	case SUNDAY, SATURDAY : \n" + 
						"	^^^^^^^^^^^^^^^^^^^^^\n" + 
						"Duplicate case\n" + 
						"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	/*
	 * 
	 */
	public void testBug543240_3() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
						"public static void bar(Day day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATURDAY, SUNDAY: \n" + 
						"			System.out.println(Day.SUNDAY);\n" + 
						"			break;\n" + 
						"		case TUESDAY : System.out.println(Day.SUNDAY);\n" + 
						"					break;\n" + 
						"		}\n" + 
						"	}" +
						"	public static void main(String[] args) {\n" +
						"		bar(Day.SATURDAY);\n" +
						"	}\n" +
						"}\n" +
						"enum Day { SATURDAY, SUNDAY, MONDAY, TUESDAY;}",
		};

		String expectedProblemLog =
				"----------\n" + 
				"1. WARNING in X.java (at line 3)\n" + 
				"	switch (day) {\n" + 
				"	        ^^^\n" + 
				"The enum constant MONDAY needs a corresponding case label in this enum switch on Day\n" + 
				"----------\n";
		this.runWarningTest(
				testFiles,
				expectedProblemLog,
				options);
	}
	public void testBug543240_4() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
						"public static void bar(Day day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATURDAY, SUNDAY: \n" + 
						"			System.out.println(day);\n" + 
						"			break;\n" + 
						"		case MONDAY : System.out.println(0);\n" + 
						"					break;\n" + 
						"		}\n" + 
						"	}" +
						"	public static void main(String[] args) {\n" +
						"		bar(Day.SATURDAY);\n" +
						"		bar(Day.MONDAY);\n" +
						"		bar(Day.SUNDAY);\n" +
						"	}\n" +
						"}\n" +
						"enum Day { SATURDAY, SUNDAY, MONDAY;}",
		};

		String expectedProblemLog =
				"SATURDAY\n" + 
				"0\n" + 
				"SUNDAY";
		this.runConformTest(
				testFiles,
				expectedProblemLog,
				options);
	}
	/*
	 * Simple switch case with string literals
	 */
	public void testBug543240_5() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(\"a\");\n" + 
						"		bar(\"b\");\n" + 
						"		bar(\"c\");\n" + 
						"		bar(\"d\");\n" + 
						"	}\n" + 
						"	public static void bar(String s) {\n" + 
						"		switch(s) {\n" + 
						"		case \"a\":\n" + 
						"		case \"b\":\n" + 
						"			System.out.println(\"A/B\");\n" + 
						"			break;\n" + 
						"		case \"c\":\n" + 
						"			System.out.println(\"C\");\n" + 
						"			break;\n" + 
						"		default:\n" + 
						"			System.out.println(\"NA\");\n" + 
						"		}\n" + 
						"	}\n" + 
						"}",
		};
		String expectedProblemLog =
				"A/B\n" + 
				"A/B\n" + 
				"C\n" + 
				"NA";
		this.runConformTest(
				testFiles,
				expectedProblemLog,
				options,
				new String[] {"--enable-preview"});
	}
	public void testBug543240_6() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(\"a\");\n" + 
						"		bar(\"b\");\n" + 
						"		bar(\"c\");\n" + 
						"		bar(\"d\");\n" + 
						"	}\n" + 
						"	public static void bar(String s) {\n" + 
						"		switch(s) {\n" + 
						"		case \"a\", \"b\":\n" + 
						"			System.out.println(\"A/B\");\n" + 
						"			break;\n" + 
						"		case \"c\":\n" + 
						"			System.out.println(\"C\");\n" + 
						"			break;\n" + 
						"		default:\n" + 
						"			System.out.println(\"NA\");\n" + 
						"		}\n" + 
						"	}\n" + 
						"}",
		};
		String expectedProblemLog =
				"A/B\n" + 
				"A/B\n" + 
				"C\n" + 
				"NA";
		this.runConformTest(
				testFiles,
				expectedProblemLog,
				options);
	}
	/*
	 * Switch with multi constant case statements with string literals
	 * two string literals with same hashcode
	 */
	public void testBug543240_7() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(\"FB\");\n" + 
						"		bar(\"Ea\");\n" + 
						"		bar(\"c\");\n" + 
						"		bar(\"D\");\n" + 
						"	}\n" + 
						"	public static void bar(String s) {\n" + 
						"		switch(s) {\n" + 
						"		case \"FB\", \"c\":\n" + 
						"			System.out.println(\"A\");\n" + 
						"			break;\n" + 
						"		case \"Ea\":\n" + 
						"			System.out.println(\"B\");\n" + 
						"			break;\n" + 
						"		default:\n" + 
						"			System.out.println(\"NA\");\n" + 
						"		}\n" + 
						"	}\n" + 
						"}",
		};
		String expectedProblemLog =
				"A\n" + 
				"B\n" + 
				"A\n" + 
				"NA";
		this.runConformTest(
				testFiles,
				expectedProblemLog,
				options);
	}
	/*
	 * Switch with multi constant case statements with integer constants
	 */
	public void testBug543240_8() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(1);\n" + 
						"		bar(2);\n" + 
						"		bar(3);\n" + 
						"		bar(4);\n" + 
						"		bar(5);\n" + 
						"	}\n" + 
						"	public static void bar(int i) {\n" + 
						"		switch (i) {\n" + 
						"		case 1, 3: \n" + 
						"			System.out.println(\"Odd\");\n" + 
						"			break;\n" + 
						"		case 2, 4: \n" + 
						"			System.out.println(\"Even\");\n" + 
						"			break;\n" + 
						"		default:\n" + 
						"			System.out.println(\"Out of range\");\n" + 
						"		}\n" + 
						"	}\n" + 
						"}",
		};
		String expectedProblemLog =
				"Odd\n" + 
				"Even\n" + 
				"Odd\n" + 
				"Even\n" + 
				"Out of range";
		this.runConformTest(
				testFiles,
				expectedProblemLog,
				options);
	}
	/*
	 * Switch multi-constant with mixed constant types, reported
	 */
	public void testBug543240_9() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
						"	public static void main(String[] args) {\n" + 
						"	}\n" + 
						"	public static void bar(int i) {\n" + 
						"		switch (i) {\n" + 
						"		case 1, 3: \n" + 
						"			System.out.println(\"Odd\");\n" + 
						"			break;\n" + 
						"		case \"2\": \n" + 
						"			System.out.println(\"Even\");\n" + 
						"			break;\n" + 
						"		default:\n" + 
						"				System.out.println(\"Out of range\");\n" + 
						"		}\n" + 
						"	}\n" + 
						"}",
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	case \"2\": \n" + 
				"	     ^^^\n" + 
				"Type mismatch: cannot convert from String to int\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	/*
	 * Switch multi-constant without break statement, reported
	 */
	public void testBug543240_10() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportFallthroughCase, CompilerOptions.WARNING);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"	}\n" + 
				"	public static void bar(int i) {\n" + 
				"		switch (i) {\n" + 
				"		case 1, 3: \n" + 
				"			System.out.println(\"Odd\");\n" + 
				"		case 2, 4: \n" + 
				"			System.out.println(\"Even\");\n" + 
				"			break;\n" +
				"		default:\n" + 
				"				System.out.println(\"Out of range\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"}",
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. WARNING in X.java (at line 8)\n" + 
				"	case 2, 4: \n" + 
				"	^^^^^^^^^\n" + 
				"Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above\n" + 
				"----------\n";
		this.runWarningTest(
				testFiles,
				expectedProblemLog,
				options);
	}
	/*
	 * Switch multi-constant without break statement, reported
	 */
	public void testBug543240_11() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportMissingDefaultCase, CompilerOptions.WARNING);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"	}\n" + 
				"	public static void bar(int i) {\n" + 
				"		switch (i) {\n" + 
				"		case 1, 3: \n" + 
				"			System.out.println(\"Odd\");\n" + 
				"		case 2, 4: \n" + 
				"			System.out.println(\"Even\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"}",
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. WARNING in X.java (at line 5)\n" + 
				"	switch (i) {\n" + 
				"	        ^\n" + 
				"The switch statement should have a default case\n" + 
				"----------\n";
		this.runWarningTest(
				testFiles,
				expectedProblemLog,
				options);
	}
	/*
	 * Switch multi-constant with duplicate int constants
	 */
	public void testBug543240_12() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
						"	public static void main(String[] args) {\n" + 
						"	}\n" + 
						"	public static void bar(int i) {\n" + 
						"		switch (i) {\n" + 
						"		case 1, 3: \n" + 
						"			System.out.println(\"Odd\");\n" + 
						"		case 3, 4: \n" + 
						"			System.out.println(\"Odd\");\n" + 
						"		}\n" + 
						"	}\n" + 
						"}",
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	case 1, 3: \n" + 
				"	^^^^^^^^^\n" + 
				"Duplicate case\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	case 3, 4: \n" + 
				"	^^^^^^^^^\n" + 
				"Duplicate case\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	/*
	 * Switch multi-constant with duplicate String literals
	 */
	public void testBug543240_13() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
						"	public static void main(String[] args) {\n" + 
						"	}\n" + 
						"	public static void bar(String s) {\n" + 
						"		switch (s) {\n" + 
						"		case \"a\", \"b\": \n" + 
						"			System.out.println(\"Odd\");\n" + 
						"		case \"b\", \"c\": \n" + 
						"			System.out.println(\"Odd\");\n" + 
						"		}\n" + 
						"	}\n" + 
						"}",
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	case \"a\", \"b\": \n" + 
				"	^^^^^^^^^^^^^\n" + 
				"Duplicate case\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	case \"b\", \"c\": \n" + 
				"	^^^^^^^^^^^^^\n" + 
				"Duplicate case\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	/*
	 * Switch multi-constant with illegal qualified enum constant
	 */
	public void testBug543240_14() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
						"	public static void main(String[] args) {\n" + 
						"	}\n" + 
						"	public static void bar(Num s) {\n" + 
						"		switch (s) {\n" + 
						"		case ONE, Num.TWO: \n" + 
						"			System.out.println(\"Odd\");\n" + 
						"		}\n" + 
						"	}\n" + 
						"}\n" +
						"enum Num { ONE, TWO}\n",
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	case ONE, Num.TWO: \n" + 
				"	          ^^^^^^^\n" + 
				"The qualified case label Num.TWO must be replaced with the unqualified enum constant TWO\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	public void testBug543240_15() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void bar(int s) {\n" + 
				"		int j = switch (s) {\n" + 
				"			case 1, 2, 3 -> (s+1);\n" +
				"			default -> j;\n" + 
				"		};\n" + 
				"	}\n" + 
				"}\n",
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	default -> j;\n" + 
				"	           ^\n" + 
				"The local variable j may not have been initialized\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	public void testBug543240_16() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"	}\n" + 
				"	public void bar(int s) {\n" +
				"		int j = 0;" + 
				"		j = switch (s) {\n" + 
				"			case 1, 2, 3 -> (s+1);\n" +
				"			default -> j;\n" + 
				"		};\n" + 
				"	}\n" + 
				"}\n",
		};
		this.runConformTest(
				testFiles,
				"",
				options);
	}
	public void testBug543795_01() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"\n" +
				"public class X {\n" +
				"	public static int foo(int i) throws IOException {\n" +
				"		int t = switch (i) {\n" +
				"		case 0 : {\n" +
				"			break 0;\n" +
				"		}\n" +
				"		case 2 : {\n" +
				"			break;\n" +
				"		}\n" +
				"		default : break 10;\n" +
				"		};\n" +
				"		return t;\n" +
				"	}\n" +
				"	\n" +
				"	public boolean bar() {\n" +
				"		return true;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			System.out.println(foo(3));\n" +
				"		} catch (IOException e) {\n" +
				"			// TODO Auto-generated catch block\n" +
				"			e.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	break;\n" + 
			"	^^^^^^\n" + 
			"Break of a switch expression should have a value\n" + 
			"----------\n");
	}
	public void testBug543691() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" + 
				"	@SuppressWarnings(\"preview\")\n" + 
				"	public static void bar(int  i) {\n" + 
				"		i = switch (i+0) {\n" + 
				"			default: System.out.println(0);\n" + 
				"		}; " + 
				"	}\n" + 
				"}",
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	i = switch (i+0) {\n" + 
				"			default: System.out.println(0);\n" + 
				"		}; 	}\n" + 
				"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"A switch expression should have at least one result expression\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	public void testBug543799_1() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
			"X.java",
			"public class X {\n" +
			"	void test(int i) {\n" + 
			"		need(switch (i) {\n" + 
			"			case 1 -> \"\";\n" + 
			"			default -> i == 3 ? null : \"\";\n" + 
			"		}); \n" + 
			"	}\n" + 
			"	void need(String s) {\n" + 
			"		System.out.println(s.toLowerCase());\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new X().need(\"Hello World\");\n" + 
			"	}\n" + 
			"}\n"
		};
		String expectedOutput = "hello world";
		runConformTest(testFiles, expectedOutput, options);
	}
	public void testBug543799_2() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
			"X.java",
			"public class X {\n" +
			"	void test(int i) {\n" + 
			"		need(switch (i) {\n" + 
			"			case 1: break \"\";\n" + 
			"			default: break i == 3 ? null : \"\";\n" + 
			"		}); \n" + 
			"	}\n" + 
			"	void need(String s) {\n" + 
			"		System.out.println(s.toLowerCase());\n" + 
			"	}\n" +
			"	public static void main(String[] args) {\n" + 
			"		new X().need(\"Hello World\");\n" + 
			"	}\n" + 
			"}\n"
		};
		String expectedOutput = "hello world";
		runConformTest(testFiles, expectedOutput, options);
	}
	public void testBug543799_3() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
			"X.java",
			"interface I0 { void i(); }\n" + 
			"interface I1 extends I0 {}\n" + 
			"interface I2 extends I0 {}\n" +
			"public class X {\n" +
			"	I1 n1() { return null; }\n" + 
			"	<I extends I2> I n2() { return null; }\n" + 
			"	<M> M m(M m) { return m; }\n" + 
			"	void test(int i, boolean b) {\n" + 
			"		m(switch (i) {\n" + 
			"			case 1 -> n1();\n" + 
			"			default -> b ? n1() : n2();\n" + 
			"		}).i(); \n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		try {\n" +
			"			new X().test(1, true);\n" +
			"		} catch (NullPointerException e) {\n" +
			"			System.out.println(\"NPE as expected\");\n" +
			"		}\n" +
			"	}\n" + 
			"}\n"
		};
		String expectedOutput = "NPE as expected";
		runConformTest(testFiles, expectedOutput, options);
	}
	public void testBug543799_4() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
			"X.java",
			"import java.util.function.Supplier;\n" +
			"interface I0 { void i(); }\n" + 
			"interface I1 extends I0 {}\n" + 
			"interface I2 extends I0 {}\n" +
			"public class X {\n" +
			"	I1 n1() { return null; }\n" + 
			"	<I extends I2> I n2() { return null; }\n" + 
			"	<M> M m(Supplier<M> m) { return m.get(); }\n" + 
			"	void test(int i, boolean b) {\n" + 
			"		m(switch (i) {\n" + 
			"			case 1 -> this::n1;\n" + 
			"			default -> this::n2;\n" + 
			"		}).i(); \n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		try {\n" +
			"			new X().test(1, true);\n" +
			"		} catch (NullPointerException e) {\n" +
			"			System.out.println(\"NPE as expected\");\n" +
			"		}\n" +
			"	}\n" + 
			"}\n"
		};
		String expectedOutput = "NPE as expected";
		runConformTest(testFiles, expectedOutput, options);
	}
	public void testBug543799_5() {
		// require resolving/inferring of poly-switch-expression during ASTNode.resolvePolyExpressionArguments()
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
			"X.java",
			"public class X {\n" +
			"		void test(int i) {\n" + 
			"		need(switch (i) {\n" + 
			"			case 1 -> 1.0f;\n" + 
			"			default -> i == 3 ? 3 : 5.0d;\n" + 
			"		}); \n" + 
			"	}\n" + 
			"	<N extends Number> void need(N s) {\n" + 
			"		System.out.println(s.toString());\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new X().need(3);\n" +
			"	}\n" + 
			"}\n"
		};
		String expectedOutput = "3";
		runConformTest(testFiles, expectedOutput, options);
	}
	public void testSwitchStatementWithBreakExpression() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	static int twice(int i) throws Exception {\n" + 
					"		switch (i) {\n" + 
					"			case 0 -> System.out.println(\"hellow\");\n" + 
					"			case 1 -> foo();\n" + 
					"			default -> throw new Exception();\n" + 
					"		};\n" + 
					"		return 0;\n" + 
					"	}\n" + 
					"\n" + 
					"	static int foo() {\n" + 
					"		System.out.println(\"inside foo\");\n" + 
					"		return 1;\n" + 
					"	}\n" + 
					"\n" + 
					"	public static void main(String[] args) {\n" + 
					"		try {\n" + 
					"			System.out.print(twice(1));\n" + 
					"		} catch (Exception e) {\n" + 
					"			System.out.print(\"Got Exception\");\n" + 
					"		}\n" + 
					"	}\n" + 
					"}"
			},
			"inside foo\n"
			+ "0");
	}
	public void testSwitchStatementWithEnumValues() {
		runConformTest(
			new String[] {
					"X.java",
					"enum SomeDays {\n" + 
					"	Mon, Wed, Fri\n" + 
					"}\n" + 
					"\n" + 
					"public class X {\n" + 
					"	int testEnum(boolean b) {\n" + 
					"		SomeDays day = b ? SomeDays.Mon : null;\n" + 
					"		return switch(day) {\n" + 
					"			case Mon -> 1;\n" + 
					"			case Wed -> 2;\n" + 
					"			case Fri -> 3;\n" + 
					"		};\n" + 
					"	}\n" + 
					"\n" + 
					"	public static void main(String[] args) {\n" + 
					"		System.out.println(new X().testEnum(true));\n" + 
					"	}\n" + 
					"}\n" + 
					""
			},
			"1");
	}
	public void testBug543967_01() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
				"	static int foo(int i) {\n" +
				"		switch (i) {\n" +
				"			default -> 3; // should flag an error\n" +
				"			\n" +
				"		};\n" +
				"		return 0;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(1);\n" +
				"	}\n" +
				"}\n",
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	default -> 3; // should flag an error\n" + 
				"	           ^\n" + 
				"Invalid expression as statement\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				new String[] { "--enable-preview"},
				options);
	}
	public void testBug544204() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public void foo(int i) {\n" + 
					"		int j = switch (i) {\n" + 
					"			case 1 -> i;\n" + 
					"			default -> i;\n" + 
					"		};\n" + 
					"		System.out.println(j);\n" + 
					"	}\n" + 
					"	\n" + 
					"	public static void main(String[] args) {\n" + 
					"		new X().foo(1);\n" + 
					"	}\n" + 
					"}"
			},
			"1");
	}
	public void testBug544204_2() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public void foo(int i) {\n" + 
					"		long j = switch (i) {\n" + 
					"			case 1 -> 10L;\n" + 
					"			default -> 20L;\n" + 
					"		};\n" + 
					"		System.out.println(j);\n" + 
					"	}\n" + 
					"	\n" + 
					"	public static void main(String[] args) {\n" + 
					"		new X().foo(1);\n" + 
					"	}\n" + 
					"}"
			},
			"10");
	}
	public void testBug544223() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public int foo(String s) throws Exception {\n" + 
					"		int i = switch (s) {\n" + 
					"			case \"hello\" -> 1;\n" + 
					"			default -> throw new Exception();\n" + 
					"		};\n" + 
					"		return i;\n" + 
					"	}\n" + 
					"\n" + 
					"	public static void main(String[] argv) {\n" + 
					"		try {\n" + 
					"			System.out.print(new X().foo(\"hello\"));\n" + 
					"		} catch (Exception e) {\n" + 
					"			//\n" + 
					"		}\n" + 
					"	}\n" + 
					"}"
			},
			"1");
	}
	public void testBug544258_01() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public void foo(Day day) {\n" +
				"    	var today = 1;\n" +
				"    	today =  switch (day) {\n" +
				"    		      case SATURDAY,SUNDAY :\n" +
				"    		         today=1;\n" +
				"    		         break today;\n" +
				"    		      case MONDAY,TUESDAY,WEDNESDAY,THURSDAY :\n" +
				"    			 today=2;\n" +
				"    			 break today;\n" +
				"    		};\n" +
				"    }\n" +
				"    public static void main(String argv[]) {\n" +
				"    	new X().foo(Day.FRIDAY);\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"enum Day {\n" +
				"	SUNDAY,\n" +
				"	MONDAY,\n" +
				"	TUESDAY,\n" +
				"	WEDNESDAY,\n" +
				"	THURSDAY,\n" +
				"	FRIDAY,\n" +
				"	SATURDAY\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	today =  switch (day) {\n" + 
			"	                 ^^^\n" + 
			"A Switch expression should cover all possible values\n" + 
			"----------\n");
	}
	public void testBug544253() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"    public void foo(int i ) {\n" + 
					"        boolean b = switch (i) {\n" + 
					"            case 0 -> i == 1;\n" + 
					"            default -> true;\n" + 
					"        };\n" + 
					"        System.out.println( b ? \" true\" : \"false\");\n" + 
					"    }\n" + 
					"    public static void main(String[] argv) {\n" + 
					"    	new X().foo(0);\n" + 
					"    }\n" + 
					"}"
			},
			"false");
	}
	public void testBug544254() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"    public void foo(String s) {\n" + 
					"        try {\n" + 
					"            int i = switch (s) {\n" + 
					"                case \"hello\" -> 0;\n" + 
					"                default -> 2;\n" + 
					"            };\n" + 
					"        } finally {\n" + 
					"        	System.out.println(s);\n" + 
					"        }\n" + 
					"    }\n" + 
					"    public static void main(String argv[]) {\n" + 
					"    	new X().foo(\"hello\");\n" + 
					"    }\n" + 
					"}"
			},
			"hello");
	}
	public void testBug544254_2() {
		Map<String, String> customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"    public void foo(String s) {\n" + 
					"        try {\n" + 
					"            int i = switch (s) {\n" + 
					"                case \"hello\" -> 0;\n" + 
					"                default -> 2;\n" + 
					"            };\n" + 
					"        } finally {\n" + 
					"        	System.out.println(s);\n" + 
					"        }\n" + 
					"    }\n" + 
					"    public static void main(String argv[]) {\n" + 
					"    	new X().foo(\"hello\");\n" + 
					"    }\n" + 
					"}"
			},
			"hello");
	}
	public void testBug544254_3() {
		Map<String, String> customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"    public void foo(String s) {\n" + 
					"        try {\n" + 
					"            long l = switch (s) {\n" + 
					"                case \"hello\" -> 0;\n" + 
					"                default -> 2;\n" + 
					"            };\n" + 
					"        } finally {\n" + 
					"        	System.out.println(s);\n" + 
					"        }\n" + 
					"    }\n" + 
					"    public static void main(String argv[]) {\n" + 
					"    	new X().foo(\"hello\");\n" + 
					"    }\n" + 
					"}"
			},
			"hello");
	}
	public void testBug544224_1() {
		Map<String, String> customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"    public int foo(int i)  {\n" +
					"        int j = (switch (i) {\n" +
					"            case 1 -> 1;\n" +
					"            default -> 2;\n" +
					"        });\n" +
					"        return j;\n" +
					"    }\n" +
					"    public static void main(String[] argv) {\n" +
					"    	new X().foo(1);\n" +
					"    }\n" +
					"}\n"
			},
			"",
			customOptions);
	}
	public void testBug544298() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	enum MyEnum {\n" + 
					"		FIRST;\n" + 
					"	}\n" + 
					"\n" + 
					"	public void foo(MyEnum myEnum) {\n" + 
					"		int i = switch (myEnum) {\n" + 
					"			case FIRST ->  1;\n" + 
					"		};\n" + 
					"			System.out.println( \"i:\" + i);\n" + 
					"	}\n" + 
					"\n" + 
					"	public static void main(String argv[]) {\n" + 
					"		new X().foo(MyEnum.FIRST);\n" + 
					"	}\n" + 
					"}"
			},
			"i:1");
	}
	public void testBug544298_2() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	enum MyEnum {\n" + 
					"		FIRST;\n" + 
					"	}\n" + 
					"\n" + 
					"	public void foo(MyEnum myEnum) {\n" + 
					"		int i = switch (myEnum) {\n" + 
					"			case FIRST ->  1;\n" + 
					"			default ->  0;\n" + 
					"		};\n" + 
					"			System.out.println( \"i:\" + i);\n" + 
					"	}\n" + 
					"\n" + 
					"	public static void main(String argv[]) {\n" + 
					"		new X().foo(MyEnum.FIRST);\n" + 
					"	}\n" + 
					"}"
			},
			"i:1");
	}
	public void testBug544428_01() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
				"    public int foo(int i) {\n" +
				"    	var v = switch(i) {\n" +
				"    	case 0 -> x;\n" +
				"    	default -> 1;\n" +
				"    	};\n" +
				"    	return v;\n" +
				"    }\n" +
				"    public static void main(String[] argv) {\n" +
				"       System.out.println(new X().foo(0));\n" +
				"    }\n" +
				"}",
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	case 0 -> x;\n" + 
				"	          ^\n" + 
				"x cannot be resolved to a variable\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				new String[] { "--enable-preview"},
				options);
	}
	public void testBug544523_01() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"    @SuppressWarnings(\"preview\")\n" +
					"	public int foo(int i) {\n" +
					"    	int v = switch(i) {\n" +
					"    	case 0 -> switch(i) {\n" +
					"    			case 0 -> 0;\n" +
					"    			default -> 1;\n" +
					"    		};\n" +
					"    	default -> 1;\n" +
					"    	};\n" +
					"    	return v;\n" +
					"    }\n" +
					"    public static void main(String[] argv) {\n" +
					"       System.out.println(new X().foo(0));\n" +
					"    }\n" +
					"}"
			},
			"0");
	}
	public void testBug544560_01() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"    public int foo(int i) {\n" +
					"    	@SuppressWarnings(\"preview\")\n" +
					"    	int v = switch(switch(i) {\n" +
					"        		default -> 1;\n" +
					"        		}) {\n" +
					"        	default -> 1;\n" +
					"        };\n" +
					"       return v;\n" +
					"    }\n" +
					"\n" +
					"    public static void main(String[] argv) {\n" +
					"       System.out.println(new X().foo(0));\n" +
					"    }\n" +
					"}\n"
			},
			"1");
	}
	public void testBug544458() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static int foo(int i) {\n" + 
					"		boolean v = switch (i) {\n" + 
					"			case 1: i = 10; break true;\n" + 
					"			default: break false;\n" + 
					"		};\n" + 
					"		return v ? 0 : 1;\n" + 
					"	}\n" + 
					"	public static void main(String[] argv) {\n" + 
					"		System.out.println(X.foo(0));\n" + 
					"	}\n" + 
					"}"
			},
			"1");
	}
	public void testBug544458_2() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static int foo(int i) {\n" + 
					"		boolean v = switch (i) {\n" + 
					"			case 1: i++; break true;\n" + 
					"			default: break false;\n" + 
					"		};\n" + 
					"		return v ? 0 : 1;\n" + 
					"	}\n" + 
					"	public static void main(String[] argv) {\n" + 
					"		System.out.println(X.foo(1));\n" + 
					"	}\n" + 
					"}"
			},
			"0");
	}
	public void testBug544458_3() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static int foo(int i) {\n" + 
					"		boolean v = switch (i) {\n" + 
					"			case 1: i+= 10; break true;\n" + 
					"			default: break false;\n" + 
					"		};\n" + 
					"		return v ? 0 : 1;\n" + 
					"	}\n" + 
					"	public static void main(String[] argv) {\n" + 
					"		System.out.println(X.foo(1));\n" + 
					"	}\n" + 
					"}"
			},
			"0");
	}
	public void testBug544458_4() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static int foo(int i) {\n" + 
					"		boolean v = switch (i) {\n" + 
					"			case 1: switch(i) {case 4: break;}; break true;\n" + 
					"			default: break false;\n" + 
					"		};\n" + 
					"		return v ? 0 : 1;\n" + 
					"	}\n" + 
					"	public static void main(String[] argv) {\n" + 
					"		System.out.println(X.foo(1));\n" + 
					"	}\n" + 
					"}"
			},
			"0");
	}
	public void testBug544458_5() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static int foo(int i) {\n" + 
					"		boolean v = switch (i) {\n" + 
					"			case 1: foo(5); break true;\n" + 
					"			default: break false;\n" + 
					"		};\n" + 
					"		return v ? 0 : 1;\n" + 
					"	}\n" + 
					"	public static void main(String[] argv) {\n" + 
					"		System.out.println(X.foo(1));\n" + 
					"	}\n" + 
					"}"
			},
			"0");
	}
	public void testBug544601_1() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"    public int foo(int i) {\n" +
					"    @SuppressWarnings(\"preview\")\n" +
					"	boolean v = switch (i) {\n" +
					"        case 1:\n" +
					"        	switch (i) {\n" +
					"        		case 1 : i = 10;\n" +
					"        			break;\n" +
					"        		default :\n" +
					"        			i = 2;\n" +
					"        			break;\n" +
					"        		}\n" +
					"        break true;\n" +
					"        default: break false;\n" +
					"    };\n" +
					"    return v ? 0 : 1;\n" +
					"    }\n" +
					"\n" +
					"    public static void main(String[] argv) {\n" +
					"       System.out.println(new X().foo(0));\n" +
					"    }\n" +
					"}\n"
			},
			"1");
	}
	public void testBug544556() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public int foo(int i) {\n" + 
				"		@SuppressWarnings(\"preview\")\n" + 
				"		int v =\n" + 
				"			switch(switch(i) {\n" + 
				"					case 0 -> { break 2; }\n" + 
				"					default -> { break 3; }\n" + 
				"				}) {\n" + 
				"			case 0 -> { break 0; }\n" + 
				"			default -> { break 1; }\n" + 
				"		};\n" + 
				"	return v == 1 ? v : 0;\n" + 
				"	}\n" + 
				"	public static void main(String[] argv) {\n" + 
				"		System.out.println(new X().foo(0));\n" + 
				"	}\n" + 
				"}"
		},
		"1");
	}
	public void testBug544702_01() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    @SuppressWarnings(\"preview\")\n" +
				"	public int foo(int i) {\n" +
				"    	int k = 10;\n" +
				"    	switch (i) {\n" +
				"    		case 0 -> { k = 0;}\n" +
				"    		default -> k = -1;\n" +
				"    	}\n" +
				"        return k;\n" +
				"    }\n" +
				"    public static void main(String[] argv) {\n" +
				"        System.out.println(new X().foo(0) == 0 ? \"Success\" : \"Failure\");\n" +
				"    }\n" +
				"\n" +
				"}\n"
		},
		"Success");
	}
	public void testBug545168_01() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@SuppressWarnings(\"preview\")\n" +
				"	public static void foo(Day day) {\n" +
				"		switch (day) {\n" +
				"		case MONDAY, FRIDAY -> System.out.println(Day.SUNDAY);\n" +
				"		case TUESDAY                -> System.out.println(7);\n" +
				"		case THURSDAY, SATURDAY     -> System.out.println(8);\n" +
				"		case WEDNESDAY              -> System.out.println(9);\n" +
				"		default -> {}\n" +
				"		}     \n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X.foo(Day.WEDNESDAY);\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"enum Day {\n" +
				"	MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;\n" +
				"}\n"
		},
		"9");
	}
	public void testBug545255_01() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void foo (int i) {\n"+
				"		int v = switch (i) {\n"+
				"			case 60, 600: break 6;\n"+
				"			case 70: break 7;\n"+
				"			case 80: break 8;\n"+
				"			case 90, 900: break 9;\n"+
				"			default: break 0;\n"+
				"		};\n"+
				"		System.out.println(v);\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		X.foo(10);\n"+
				"	}\n"+
				"}\n"
		},
		"0");
	}
	// see comment 12 in the bug 
	public void testBug513766_01() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"public class X {\n"+
				"    @SuppressWarnings(\"preview\")\n"+
				"    public void foo(int i) {\n"+
				"    	if (switch(i) { default -> magic(); })\n"+
				"            System.out.println(\"true\");\n"+
				"        if (magic())\n"+
				"            System.out.println(\"true, too\");\n"+
				"    }\n"+
				"    <T> T magic() { return null; }\n"+
				"}\n"};
		runner.expectedCompilerLog =
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	if (switch(i) { default -> magic(); })\n" + 
			"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to boolean\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	if (magic())\n" + 
			"	    ^^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to boolean\n" + 
			"----------\n";
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = JavacHasABug.JavacBug8179483_switchExpression;
		runner.runNegativeTest();
	}
	public void testBug545333() {
		Runner runner = new Runner();
		runner.testFiles = 	new String[] {
				"X.java",
				"public class X {\n"+
				"    @SuppressWarnings(\"preview\")\n"+
				"	public static int foo(int i) throws MyException {\n"+
				"    	int v = switch (i) {\n"+
				"    		default -> throw new MyException();\n"+
				"    	};\n"+
				"        return v;\n"+
				"    }\n"+
				"    public static void main(String argv[]) {\n"+
				"    	try {\n"+
				"			System.out.println(X.foo(1));\n"+
				"		} catch (MyException e) {\n"+
				"			System.out.println(\"Exception thrown as expected\");\n"+
				"		}\n"+
				"	}\n"+
				"}\n"+
				"class MyException extends Exception {\n"+
				"	private static final long serialVersionUID = 3461899582505930473L;	\n"+
				"}\n"
		};
		runner.expectedCompilerLog =
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	int v = switch (i) {\n" + 
			"    		default -> throw new MyException();\n" + 
			"    	};\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"A switch expression should have at least one result expression\n" + 
			"----------\n";
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = JavacHasABug.JavacBug8226510_switchExpression;
		runner.runNegativeTest();
	}
	public void testBug545518() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return;
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		String message = 
				"----------\n" + 
				"1. WARNING in X.java (at line 5)\n" + 
				"	case \"ABC\", (false ? (String) \"c\" : (String) \"d\") : break;\n" + 
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"You are using a preview language feature that may or may not be supported in a future release\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 5)\n" + 
				"	case \"ABC\", (false ? (String) \"c\" : (String) \"d\") : break;\n" + 
				"	                     ^^^^^^^^^^^^\n" + 
				"Dead code\n" + 
				"----------\n";
		
		this.runWarningTest(new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String [] args) {\n" +
				"  	 String arg = \"ABD\";\n" +
				"    switch(arg) {\n" + 
				"      case \"ABC\", (false ? (String) \"c\" : (String) \"d\") : break;\n" +
				"	 }\n" +
				"  }\n" +
				"}\n"
			},
			message,
			options);
	}
	public void testBug545518a() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return;
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String message = 
				"----------\n" + 
				"1. WARNING in X.java (at line 5)\n" + 
				"	case \"ABC\", (false ? (String) \"c\" : (String) \"d\") : break;\n" + 
				"	                     ^^^^^^^^^^^^\n" + 
				"Dead code\n" + 
				"----------\n";
		
		this.runWarningTest(new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String [] args) {\n" +
				"  	 String arg = \"ABD\";\n" +
				"    switch(arg) {\n" + 
				"      case \"ABC\", (false ? (String) \"c\" : (String) \"d\") : break;\n" +
				"	 }\n" +
				"  }\n" +
				"}\n"
			},
			message,
			options);
	}
	public void testBug545518b() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		String message = 
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	case \"ABC\", (false ? (String) \"c\" : (String) \"d\") : break;\n" + 
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Multi constant case is a preview feature and disabled by default. Use --enable-preview to enable\n" + 
				"----------\n";
		
		this.runNegativeTest(new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String [] args) {\n" +
				"  	 String arg = \"ABD\";\n" +
				"    switch(arg) {\n" + 
				"      case \"ABC\", (false ? (String) \"c\" : (String) \"d\") : break;\n" +
				"	 }\n" +
				"  }\n" +
				"}\n"
			},
			message,
			null,
			true,
			new String[] { "--enable-preview"},
			options);
	}
	public void testBug545715_01() {
		runConformTest(
			new String[] {
				"X.java",
				"enum X {\n"+
				"    A, B; \n"+
				"    @SuppressWarnings(\"preview\")\n"+
				"    public static void main(String[] args) {\n"+
				"         X myEnum = X.A;\n"+
				"         int o;\n"+
				"         switch(myEnum) {\n"+
				"             case A -> o = 5;\n"+
				"             case B -> o = 10;\n"+
				"             default -> o = 0;\n"+
				"         }\n"+
				"         System.out.println(o);\n"+
				"     }\n"+
				"}\n"
		},
		"5");
	}
	public void testBug545716_01() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"X.java",
			"enum X {\n"+
			"    A, B;\n"+
			"     \n"+
			"    @SuppressWarnings(\"preview\")\n"+
			"    public static void main(String[] args) {\n"+
			"         X myEnum = X.A;\n"+
			"         int o;\n"+
			"         var f = switch(myEnum) {\n"+
			"             case A -> o = 5;\n"+
			"             case B -> o = 10;\n"+
			"         };\n"+
			"         System.out.println(o);\n"+
			"     }\n"+
			"} \n"
		};
		runner.expectedOutputString = "5";
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = JavacHasABug.JavacBug8221413_switchExpression;
		runner.runConformTest();
	}
	public void testBug545983_01() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"\n"+
				"public class X {\n"+
				"\n"+
				"	@SuppressWarnings(\"preview\")\n"+
				"	public static int foo() {\n"+
				"	for (int i = 0; i < 1; ++i) {\n"+
				"			int k = switch (i) {\n"+
				"				case 0:\n"+
				"					break 1;\n"+
				"				default:\n"+
				"					continue;\n"+
				"			};\n"+
				"			System.out.println(k);\n"+
				"		}\n"+
				"		return 1;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		X.foo();\n"+
				"	}\n"+
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	continue;\n" + 
			"	^^^^^^^^^\n" + 
			"'continue' or 'return' cannot be the last statement in a Switch expression case body\n" + 
			"----------\n");
	}
	public void testBug545983_02() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"\n"+
				"public class X {\n"+
				"\n"+
				"	@SuppressWarnings(\"preview\")\n"+
				"	public static int foo() {\n"+
				"	for (int i = 0; i < 1; ++i) {\n"+
				"			int k = switch (i) {\n"+
				"				case 0:\n"+
				"					break 1;\n"+
				"				default:\n"+
				"					return 2;\n"+
				"			};\n"+
				"			System.out.println(k);\n"+
				"		}\n"+
				"		return 1;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		X.foo();\n"+
				"	}\n"+
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	return 2;\n" + 
			"	^^^^^^^^^\n" + 
			"'continue' or 'return' cannot be the last statement in a Switch expression case body\n" + 
			"----------\n");
	}
	public void testBug547125_01() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(Day day) {\n" + 
				"		switch (day) {\n" + 
				"		case SATURDAY, SUNDAY, SUNDAY:\n" + 
				"			System.out.println(\"Weekend\");\n" + 
				"		case MONDAY:\n" + 
				"			System.out.println(\"Weekday\");\n" +
				"		default: \n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"enum Day {\n" + 
				"	MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;\n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	case SATURDAY, SUNDAY, SUNDAY:\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n");
	}
	public void testBug547125_02() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(Day day) {\n" + 
				"		switch (day) {\n" + 
				"		case SATURDAY, SUNDAY, MONDAY:\n" + 
				"			System.out.println(\"Weekend\");\n" + 
				"		case MONDAY, SUNDAY:\n" + 
				"			System.out.println(\"Weekday\");\n" +
				"		default: \n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"enum Day {\n" + 
				"	MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;\n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	case SATURDAY, SUNDAY, MONDAY:\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	case MONDAY, SUNDAY:\n" + 
			"	^^^^^^^^^^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	case MONDAY, SUNDAY:\n" + 
			"	^^^^^^^^^^^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n");
	}
}
