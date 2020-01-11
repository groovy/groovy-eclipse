/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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

public class SwitchExpressionsYieldTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug550861_01" };
	}
	
	public static Class<?> testClass() {
		return SwitchExpressionsYieldTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_13);
	}
	public SwitchExpressionsYieldTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_13); // FIXME
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_13);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		return defaultOptions;
	}
	
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("13");
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview("13"));
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
		runWarningTest(testFiles, expectedCompilerLog, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
			Map<String, String> customOptions, String javacAdditionalTestOptions) {

		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview("13") :
			JavacTestOptions.forReleaseWithPreview("13", javacAdditionalTestOptions);
		runner.runWarningTest();
	}
	public void testBug544073_000() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"\n"+
						"	public static int yield() {\n"+
						"		return 1;\n"+
						"	}\n"+
						"	@SuppressWarnings(\"preview\")\n"+
						"	public static int foo(int val) {\n"+
						"		int k = switch (val) {\n"+
						"		case 1 -> { yield 1; }\n"+
						"		default -> { yield 2; }\n"+
						"		};\n"+
						"		return k;\n"+
						"	}\n"+
						"	public static void main(String[] args) {\n"+
						"		System.out.println(X.foo(1));\n"+
						"	}\n"+
						"}\n"
				},
				"1");
	}
	public void testBug544073_001() {
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
	public void testBug544073_002() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
								"	static int twice(int i) throws Exception {\n"+
								"		int tw = switch (i) {\n"+
								"			case 0 -> 0;\n"+
								"			case 1 -> { \n"+
								"				System.out.println(\"do_not_print\");\n"+
								"				yield 1;\n"+
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
	public void testBug544073_003() {
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
	public void testBug544073_004() {
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
				"				yield 1;\n" +
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
	public void testBug544073_005() {
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
				"				yield 1;\n" +
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
	public void _testBug544073_006() {
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
				"				yield 1;\n" +
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
	public void testBug544073_007() {
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
						" 				yield z;\n"+
						"			}\n"+
						"			default -> {\n"+
						"				yield 12;\n"+
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
	public void testBug544073_008() {
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
	public void testBug544073_009() {
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
	public void testBug544073_010() {
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
	public void testBug544073_011() {
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
	public void testBug544073_012() {
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
	public void testBug544073_013() {
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
					"1. ERROR in X.java (at line 0)\n" + 
					"	public class X {\n" + 
					"	^\n" + 
					"Preview features enabled at an invalid source release level 11, preview can be enabled only at source level 13\n" + 
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
	public void testBug544073_014() {
			String[] testFiles = new String[] {
					"X.java",
					"public class X {\n" +
					"	public static int foo(int i) {\n" +
					"		int v;\n" +
					"		int t = switch (i) {\n" +
					"		case 0 : {\n" +
					"			yield 0;\n" +
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
					expectedProblemLog);
	}
	public void testBug544073_015() {
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
		runner.runNegativeTest();
	}
	public void testBug544073_016() {
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
	public void testBug544073_017() {
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

		String expectedProblemLog =
				"SUNDAY";
		this.runConformTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * A simple multi constant case statement, compiler reports missing enum constants
	 */
	public void testBug544073_018() {
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
				expectedProblemLog);
	}
	/*
	 * A simple multi constant case statement with duplicate enums
	 */
	public void testBug544073_019() {
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
				expectedProblemLog);
	}
	/*
	 * A simple multi constant case statement with duplicate enums
	 */
	public void testBug544073_020() {
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
				expectedProblemLog);
	}
	/*
	 * 
	 */
	public void testBug544073_021() {
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
				expectedProblemLog);
	}
	public void testBug544073_022() {
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
				expectedProblemLog);
	}
	/*
	 * Simple switch case with string literals
	 */
	public void testBug544073_023() {
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
				expectedProblemLog);
	}
	public void testBug544073_024() {
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
				expectedProblemLog);
	}
	/*
	 * Switch with multi constant case statements with string literals
	 * two string literals with same hashcode
	 */
	public void testBug544073_025() {
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
				expectedProblemLog);
	}
	/*
	 * Switch with multi constant case statements with integer constants
	 */
	public void testBug544073_026() {
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
				expectedProblemLog);
	}
	/*
	 * Switch multi-constant with mixed constant types, reported
	 */
	public void testBug544073_027() {
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
	public void testBug544073_028() {
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
				options,
				"-Xlint:fallthrough");
	}
	/*
	 * Switch multi-constant without yield statement, reported
	 */
	public void testBug544073_029() {
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
	public void testBug544073_030() {
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
	public void testBug544073_031() {
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
	public void testBug544073_032() {
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
	public void testBug544073_033() {
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
	public void testBug544073_034() {
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
				"");
	}
	public void testBug544073_035() {
		// TODO: Fix me
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"\n" +
				"public class X {\n" +
				"	public static int foo(int i) throws IOException {\n" +
				"		int t = switch (i) {\n" +
				"		case 0 : {\n" +
				"			yield 0;\n" +
				"		}\n" +
				"		case 2 : {\n" +
				"			break;\n" +
				"		}\n" +
				"		default : yield 10;\n" +
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
			"break out of switch expression not allowed\n" + 
			"----------\n");
	}
	public void testBug544073_036() {
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
	public void testBug544073_037() {
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
	public void testBug544073_038() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		String[] testFiles = new String[] {
			"X.java",
			"public class X {\n" +
			"	void test(int i) {\n" + 
			"		need(switch (i) {\n" + 
			"			case 1: yield \"\";\n" + 
			"			default: yield i == 3 ? null : \"\";\n" + 
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
	public void testBug544073_039() {
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
	public void testBug544073_040() {
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
	public void testBug544073_041() {
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
	public void testBug544073_042() {
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
	public void testBug544073_043() {
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
	public void testBug544073_044() {
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
	public void testBug544073_045() {
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
	public void testBug544073_046() {
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
	public void testBug544073_047() {
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
	public void testBug544073_048() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public void foo(Day day) {\n" +
				"    	var today = 1;\n" +
				"    	today =  switch (day) {\n" +
				"    		      case SATURDAY,SUNDAY :\n" +
				"    		         today=1;\n" +
				"    		         yield today;\n" +
				"    		      case MONDAY,TUESDAY,WEDNESDAY,THURSDAY :\n" +
				"    			 today=2;\n" +
				"    			 yield today;\n" +
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
	public void testBug544073_049() {
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
	public void testBug544073_050() {
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
	public void testBug544073_051() {
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
	public void testBug544073_052() {
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
	public void testBug544073_053() {
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
	public void testBug544073_054() {
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
	public void testBug544073_055() {
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
	public void testBug544073_056() {
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
	public void testBug544073_057() {
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
	public void testBug544073_058() {
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
	public void testBug544073_059() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static int foo(int i) {\n" + 
					"		boolean v = switch (i) {\n" + 
					"			case 1: i = 10; yield true;\n" + 
					"			default: yield false;\n" + 
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
	public void testBug544073_060() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static int foo(int i) {\n" + 
					"		boolean v = switch (i) {\n" + 
					"			case 1: i++; yield true;\n" + 
					"			default: yield false;\n" + 
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
	public void testBug544073_061() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static int foo(int i) {\n" + 
					"		boolean v = switch (i) {\n" + 
					"			case 1: i+= 10; yield true;\n" + 
					"			default: yield false;\n" + 
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
	public void testBug544073_062() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static int foo(int i) {\n" + 
					"		boolean v = switch (i) {\n" + 
					"			case 1: switch(i) {case 4: break;}; yield true;\n" + 
					"			default: yield false;\n" + 
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
	public void testBug544073_063() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" + 
					"	public static int foo(int i) {\n" + 
					"		boolean v = switch (i) {\n" + 
					"			case 1: foo(5); yield true;\n" + 
					"			default: yield false;\n" + 
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
	public void testBug544073_064() {
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
					"        yield true;\n" +
					"        default: yield false;\n" +
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
	public void testBug544073_065() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public int foo(int i) {\n" + 
				"		@SuppressWarnings(\"preview\")\n" + 
				"		int v =\n" + 
				"			switch(switch(i) {\n" + 
				"					case 0 -> { yield 2; }\n" + 
				"					default -> { yield 3; }\n" + 
				"				}) {\n" + 
				"			case 0 -> { yield 0; }\n" + 
				"			default -> { yield 1; }\n" + 
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
	public void testBug544073_066() {
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
	public void testBug544073_067() {
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
	public void testBug544073_068() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void foo (int i) {\n"+
				"		int v = switch (i) {\n"+
				"			case 60, 600: yield 6;\n"+
				"			case 70: yield 7;\n"+
				"			case 80: yield 8;\n"+
				"			case 90, 900: yield 9;\n"+
				"			default: yield 0;\n"+
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
				"}\n",
			};
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
	public void testBug544073_070() {
		runNegativeTest(
			new String[] {
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
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	int v = switch (i) {\n" + 
			"    		default -> throw new MyException();\n" + 
			"    	};\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"A switch expression should have at least one result expression\n" + 
			"----------\n");
	}
	public void testBug544073_071() {
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
			options,
			"-Xlint:preview");
	}
	public void testBug544073_072() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return;
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
			message);
	}
	public void testBug544073_073() {
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
	public void testBug544073_074() {
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
	public void testBug544073_075() {
		runConformTest(
			new String[] {
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
		},
		"5");
	}
	public void testBug544073_076() {
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
				"					yield 1;\n"+
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
	public void testBug544073_077() {
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
				"					yield 1;\n"+
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
	public void testBug544073_078() {
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
	public void testBug544073_079() {
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
	public void testBug544073_80() {
		runConformTest(
				new String[] {
						"X.java",
						"\n"+
						"public class X {\n"+
						"\n"+
						"	public static int yield() {\n"+
						"		return 1;\n"+
						"	}\n"+
						"	@SuppressWarnings(\"preview\")\n"+
						"	public static int foo(int val) {\n"+
						"		return bar (switch (val) {\n"+
						"		case 1 : { yield val == 1 ? 2 : 3; }\n"+
						"		default : { yield 2; }\n"+
						"		});\n"+
						"	}\n"+
						"	public static int bar(int val) {\n"+
						"		return val;\n"+
						"	}\n"+
						"	public static void main(String[] args) {\n"+
						"		System.out.println(X.foo(1));\n"+
						"	}\n"+
						"}\n"
				},
				"2");
	}
	public void testBug544073_81() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public static int foo(int val) {\n"+
					"		int k = switch (val) {\n"+
					"		case 1 : { break 1; }\n"+
					"		default : { break 2; }\n"+
					"		};\n"+
					"		return k;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(1));\n"+
					"	}\n"+
					"}\n", 
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	case 1 : { break 1; }\n" + 
				"	                 ^\n" + 
				"Syntax error on token \"1\", delete this token\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	default : { break 2; }\n" + 
				"	                  ^\n" + 
				"Syntax error on token \"2\", delete this token\n" + 
				"----------\n");
	}
	public void testBug547891_01() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"	public static void yield() {}\n"+
					"	public static void main(String[] args) {\n"+
					"		yield();\n"+
					"		X.yield();\n"+
					"	}\n"+
					"}\n", 
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	yield();\n" + 
				"	^^^^^^^\n" + 
				"restricted identifier yield not allowed here - method calls need to be qualified\n" + 
				"----------\n");
	}
	public void testBug547891_02() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void yield() {}\n"+
				"	public static void main(String[] args) {\n"+
				"		yield();\n"+
				"	}\n"+
				"	public static void bar() {\n"+
				"		Zork();\n"+
				"	}\n"+
				"}\n", 
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. WARNING in X.java (at line 4)\n" + 
				"	yield();\n" + 
				"	^^^^^^^\n" + 
				"yield may be disallowed in future - qualify method calls to avoid this message\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 7)\n" + 
				"	Zork();\n" + 
				"	^^^^\n" + 
				"The method Zork() is undefined for the type X\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				new String[] {""},
				options);
	}
	public void testBug547891_03() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"		yield 1;\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	yield 1;\n" + 
				"	^^^^^\n" + 
				"Syntax error on token \"yield\", AssignmentOperator expected after this token\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield is a restricted identifier and cannot be used as type name\n" + 
				"----------\n");
	}
	public void testBug547891_04() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"		yield 1;\n"+
				"		Zork();\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	yield 1;\n" + 
				"	^^^^^\n" + 
				"Syntax error on token \"yield\", AssignmentOperator expected after this token\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 7)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield may be a restricted identifier in future and may be disallowed as a type name\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				new String[] {""},
				options);
	}
	public void testBug547891_05() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"		yield y;\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	yield y;\n" + 
				"	^^^^^\n" + 
				"yield is a restricted identifier and cannot be used as type name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield is a restricted identifier and cannot be used as type name\n" + 
				"----------\n");
	}
	public void testBug547891_06() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"		yield y;\n"+
				"		Zork();\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. WARNING in X.java (at line 3)\n" + 
				"	yield y;\n" + 
				"	^^^^^\n" + 
				"yield may be a restricted identifier in future and may be disallowed as a type name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	Zork();\n" + 
				"	^^^^\n" + 
				"The method Zork() is undefined for the type X\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 7)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield may be a restricted identifier in future and may be disallowed as a type name\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				new String[] {""},
				options);
	}
	public void testBug547891_07() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"		yield y = null;\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	yield y = null;\n" + 
				"	^^^^^\n" + 
				"yield is a restricted identifier and cannot be used as type name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield is a restricted identifier and cannot be used as type name\n" + 
				"----------\n");
	}
	public void testBug547891_08() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"		yield y = null;\n"+
				"		Zork();\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. WARNING in X.java (at line 3)\n" + 
				"	yield y = null;\n" + 
				"	^^^^^\n" + 
				"yield may be a restricted identifier in future and may be disallowed as a type name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	Zork();\n" + 
				"	^^^^\n" + 
				"The method Zork() is undefined for the type X\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 7)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield may be a restricted identifier in future and may be disallowed as a type name\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				new String[] {""},
				options);
	}	public void testBug547891_09() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield is a restricted identifier and cannot be used as type name\n" + 
				"----------\n");
	}
	public void testBug547891_10() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"		Zork();\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	Zork();\n" + 
				"	^^^^\n" + 
				"The method Zork() is undefined for the type X\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 6)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield may be a restricted identifier in future and may be disallowed as a type name\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				new String[] {""},
				options);
	}
	public void testBug547891_11() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"		new yield();\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	new yield();\n" + 
				"	    ^^^^^\n" + 
				"yield is a restricted identifier and cannot be used as type name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield is a restricted identifier and cannot be used as type name\n" + 
				"----------\n");
	}
	public void testBug547891_12() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"		new yield();\n"+
				"		Zork();\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. WARNING in X.java (at line 3)\n" + 
				"	new yield();\n" + 
				"	    ^^^^^\n" + 
				"yield may be a restricted identifier in future and may be disallowed as a type name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	Zork();\n" + 
				"	^^^^\n" + 
				"The method Zork() is undefined for the type X\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 7)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield may be a restricted identifier in future and may be disallowed as a type name\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				new String[] {""},
				options);
	}
	public void testBug547891_13() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"		yield[] y;\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	yield[] y;\n" + 
				"	^^^^^^^\n" + 
				"yield is a restricted identifier and cannot be used as type name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield is a restricted identifier and cannot be used as type name\n" + 
				"----------\n");
	}
	public void testBug547891_14() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n"+
				"	public static void main(String[] args) {\n"+
				"		yield[] y;\n"+
				"		Zork();\n"+
				"	}\n"+
				"}\n"+
				"class yield {\n" +
				"}\n", 
		};
		String expectedProblemLog =
				"----------\n" + 
				"1. WARNING in X.java (at line 3)\n" + 
				"	yield[] y;\n" + 
				"	^^^^^^^\n" + 
				"yield may be a restricted identifier in future and may be disallowed as a type name\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	Zork();\n" + 
				"	^^^^\n" + 
				"The method Zork() is undefined for the type X\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 7)\n" + 
				"	class yield {\n" + 
				"	      ^^^^^\n" + 
				"yield may be a restricted identifier in future and may be disallowed as a type name\n" + 
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				new String[] {""},
				options);
	}
	public void testBug547891_15() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return;
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		String message = 
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	case 1 -> yield();\n" + 
				"	          ^^^^^^^\n" + 
				"restricted identifier yield not allowed here - method calls need to be qualified\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 8)\n" + 
				"	case 3 -> {yield yield();}\n" + 
				"	                 ^^^^^^^\n" + 
				"restricted identifier yield not allowed here - method calls need to be qualified\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 10)\n" + 
				"	default -> { yield yield();}\n" + 
				"	                   ^^^^^^^\n" + 
				"restricted identifier yield not allowed here - method calls need to be qualified\n" + 
				"----------\n";
		
		this.runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"+
				"\n"+
				"	@SuppressWarnings(\"preview\")\n"+
				"	public static int foo(int i) {\n"+
				"		int r = switch(i) {\n"+
				"			case 1 -> yield();\n"+
				"			case 2 -> X.yield();\n"+
				"			case 3 -> {yield yield();}\n"+
				"			case 4 -> {yield X.yield();}\n"+
				"			default -> { yield yield();}\n"+
				"		};\n"+
				"		return r;\n"+
				"	}\n"+
				"	public static int yield() {\n"+
				"		return 0;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println(X.foo(0));\n"+
				"	}\n"+
				"}\n"
			},
			message,
			null,
			true,
			new String[] { "--enable-preview"},
			options);
	}
	public void testBug547891_16() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return;
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		String message = 
				"----------\n" + 
				"1. ERROR in X.java (at line 9)\n" + 
				"	case 3 -> {yield yield();}\n" + 
				"	                 ^^^^^^^\n" + 
				"restricted identifier yield not allowed here - method calls need to be qualified\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 11)\n" + 
				"	default -> { yield yield();}\n" + 
				"	                   ^^^^^^^\n" + 
				"restricted identifier yield not allowed here - method calls need to be qualified\n" + 
				"----------\n";
		
		this.runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"+
				"\n"+
				"	@SuppressWarnings(\"preview\")\n"+
				"	public  int foo(int i) {\n"+
				"		X x = new X();\n"+
				"		int r = switch(i) {\n"+
				"			case 1 -> this.yield();\n"+
				"			case 2 -> x.new Y().yield();\n"+
				"			case 3 -> {yield yield();}\n"+
				"			case 4 -> {yield new X().yield() + x.new Y().yield();}\n"+
				"			default -> { yield yield();}\n"+
				"		};\n"+
				"		return r;\n"+
				"	}\n"+
				"	public  int yield() {\n"+
				"		return 0;\n"+
				"	}\n"+
				"	class Y {\n"+
				"		public  int yield() {\n"+
				"			return 0;\n"+
				"		}	\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println(new X().foo(0));\n"+
				"	}\n"+
				"}\n"
			},
			message,
			null,
			true,
			new String[] { "--enable-preview"},
			options);
	}
	public void testBug547891_17() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"		int yield = 100;\n"+
					"		int r = switch(i) {\n"+
					"			default -> yield - 1;\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public  int yield() {\n"+
					"		return 0;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}"
			},
			"99");
	}
	public void testBug547891_18() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"		int yield = 100;\n"+
					"		int r = switch(i) {\n"+
					"			default -> {yield - 1;}\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public  int yield() {\n"+
					"		return 0;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}"
			},
			"-1");
	}
	public void testBug547891_19() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"   static int yield = 100;\n"+
					"\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"		int r = switch(i) {\n"+
					"			default -> yield - 1;\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public  int yield() {\n"+
					"		return 0;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}"
			},
			"99");
	}
	public void testBug547891_20() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"   static int yield = 100;\n"+
					"\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"		int r = switch(i) {\n"+
					"			default -> {yield - 1;}\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public  int yield() {\n"+
					"		return 0;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}"
			},
			"-1");
	}
	public void testBug547891_21() {
		if (this.complianceLevel < ClassFileConstants.JDK12)
			return;
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		String message = 
				"----------\n" + 
				"1. ERROR in X.java (at line 7)\n" + 
				"	default -> yield - 1;\n" + 
				"	           ^^^^^\n" + 
				"Cannot make a static reference to the non-static field yield\n" + 
				"----------\n";
		
		this.runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"+
				"   int yield = 100;\n"+
				"\n"+
				"	@SuppressWarnings(\"preview\")\n"+
				"	public  static int foo(int i) {\n"+
				"		int r = switch(i) {\n"+
				"			default -> yield - 1;\n"+
				"		};\n"+
				"		return r;\n"+
				"	}\n"+
				"	public  int yield() {\n"+
				"		return 0;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println(X.foo(0));\n"+
				"	}\n"+
				"}"
			},
			message,
			null,
			true,
			new String[] { "--enable-preview"},
			options);
	}
	public void testBug547891_22() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"\n"+
					"	static int yield = 100;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"	int r = switch(i) {\n"+
					"			default -> X.yield();\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public static  int yield() {\n"+
					"		yield: while (X.yield == 100) {\n"+
					"			yield = 256;\n"+
					"			break yield;\n"+
					"		}\n"+
					"		return yield;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}\n"
			},
			"256");
	}
	public void testBug547891_23() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"\n"+
					"	static int yield =100 ;\n"+
					"	@SuppressWarnings(\"preview\")\n"+
					"	public  static int foo(int i) {\n"+
					"	int r = switch(i) {\n"+
					"			default -> X.yield();\n"+
					"		};\n"+
					"		return r;\n"+
					"	}\n"+
					"	public static  int yield() {\n"+
					"		int yield = 500 ;\n"+
					"		yield: while (yield == 500) {\n"+
					"			yield = 1024;\n"+
					"			break yield;\n"+
					"		}\n"+
					"		return yield;\n"+
					"	}\n"+
					"	public static void main(String[] args) {\n"+
					"		System.out.println(X.foo(0));\n"+
					"	}\n"+
					"}\n"
			},
			"1024");
	}
	public void testBug547891_24() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"\n"+
				"	@SuppressWarnings(\"preview\")\n"+
				"	public  static int foo(int i) {\n"+
				"		int yield = 100;\n"+
				"		int r = switch(i) {\n"+
				"			default -> {yield yield + 1;}\n"+
				"		};\n"+
				"		return r;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println(X.foo(0));\n"+
				"	}\n"+
				"}"
			},
			"101");
	}
	public void testBug547891_25() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"\n"+
				"	@SuppressWarnings(\"preview\")\n"+
				"	public  static int foo(int i) {\n"+
				"		int yield = 100;\n"+
				"		int r = switch(i) {\n"+
				"			default -> {yield yield + yield + yield * yield;}\n"+
				"		};\n"+
				"		return r;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println(X.foo(0));\n"+
				"	}\n"+
				"}"
			},
			"10200");
	}
	public void testBug547891_26() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"\n"+
				"	@SuppressWarnings(\"preview\")\n"+
				"	public  static int foo(int i) {\n"+
				"		int yield = 100;\n"+
				"		int r = switch(i) {\n"+
				"			default -> {yield + yield + yield + yield * yield;}\n"+
				"		};\n"+
				"		return r;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println(X.foo(0));\n"+
				"	}\n"+
				"}"
			},
			"10200");
	}
	public void testBug547891_27() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"\n"+
				"	@SuppressWarnings(\"preview\")\n"+
				"	public  static int foo(int i) {\n"+
				"		int yield = 100;\n"+
				"		int r = switch(i) {\n"+
				"			default ->0 + yield + 10;\n"+
				"		};\n"+
				"		return r;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println(X.foo(0));\n"+
				"	}\n"+
				"}"
			},
			"110");
	}
	public void testBug547891_28() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"	@SuppressWarnings(\"preview\")\n"+
				"	public  static int foo(int i) {\n"+
				"		int yield = 100;\n"+
				"		int r = switch(i) {\n"+
				"			 case 0 : yield 100;\n"+
				"			 case 1 : yield yield;\n"+
				"			 default: yield 0;\n"+
				"		};\n"+
				"		return r;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println(X.foo(0));\n"+
				"	}\n"+
				"}"
			},
			"100");
	}
	public void testBug547891_29() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"	@SuppressWarnings(\"preview\")\n"+
				"	public  static int foo(int i) {\n"+
				"		int yield = 100;\n"+
				"		int r = switch(i) {\n"+
				"			 case 0 : yield 100;\n"+
				"			 case 1 : yield yield;\n"+
				"			 default: yield 0;\n"+
				"		};\n"+
				"		return r > 100 ? yield + 1 : yield + 200;\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println(X.foo(0));\n"+
				"	}\n"+
				"}"
			},
			"300");
	}
	public void testBug550354_01() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  @SuppressWarnings({ \"preview\" })\n"+
				"  public static int foo(int i) throws Exception {\n"+
				"    int v = switch (i) {\n"+
				"        default ->  {if (i > 0) yield 1;\n"+
				"        else yield 2;}\n"+
				"    };\n"+
				"    return v;\n"+
				"  }\n"+
				"  public static void main(String argv[]) throws Exception {\n"+
				"    System.out.println(X.foo(1));\n"+
				"  }\n"+
				"}"
			},
			"1");
	}
	public void testBug548418_01() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  @SuppressWarnings({ \"preview\", \"unused\" })\n"+
				"  public static void main(String[] args) {\n"+
				"	int day =10;\n"+
				"    int i = switch (day) {\n"+
				"      default -> {\n"+
				"        for(int j = 0; j < 3; j++) {\n"+
				"        	yield 99;\n"+
				"        }\n"+
				"        yield 0;\n"+
				"      }\n"+
				"    };\n"+
				"    System.out.println(i);\n"+
				"  }\n"+
				"}\n"
			},
			"99");
	}
	public void testBug550853_01() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  @SuppressWarnings({ \"preview\" })\n"+
				"  public static int foo(int i) throws Exception {\n"+
				"    int v = switch (i) {\n"+
				"        default : {yield switch (i) {\n"+
				"        		default -> { yield 0; } \n"+
				"        		};\n"+
				"        	}\n"+
				"    };\n"+
				"    return v;\n"+
				"  }\n"+
				"  public static void main(String argv[]) throws Exception {\n"+
				"    System.out.println(X.foo(1));\n"+
				"  }\n"+
				"}\n"
			},
			"0");
	}
	public void testBug550861_01() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  @SuppressWarnings({ \"preview\" })\n"+
				"  public static void foo(int i) throws Exception {\n"+
				"	  System.out.println(switch(0) {\n"+
				"	  default -> {\n"+
				"	    do yield 1; while(false);\n"+
				"	  }\n"+
				"	  });\n"+
				"  }\n"+
				"  public static void main(String argv[]) throws Exception {\n"+
				"	  X.foo(1);\n"+
				"  }\n"+
				"}\n"
			},
			"1");
	}
	public void testBug551030a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@SuppressWarnings(\"nls\")\n" + 
				"	static final String MONDAY = \"MONDAY\";\n" + 
				"	public static void main(String[] args) {\n" + 
				"		int num = switch (day) {\n" + 
				"		case MONDAY: \n" + 
				"			// Nothing\n" + 
				"		default:\n" + 
				"			yield \";     \n" + 
				"		}; \n" + 
				"	}\n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	yield \";     \n" + 
			"	      ^^^^^^^\n" + 
			"String literal is not properly closed by a double-quote\n" + 
			"----------\n");
	}
	public void testBug551030b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@SuppressWarnings(\"nls\")\n" + 
				"	static final String MONDAY = \"MONDAY\";\n" + 
				"	public static void main(String[] args) {\n" + 
				"		int num = switch (day) {\n" + 
				"		case MONDAY: \n" + 
				"			// Nothing\n" + 
				"		default:\n" + 
				"			yield \"\"\";     \n" + 
				"		}; \n" + 
				"	}\n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	yield \"\"\";     \n" + 
			"	        ^^^^^^^\n" + 
			"String literal is not properly closed by a double-quote\n" + 
			"----------\n");
	}
	public void testBug544943() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@SuppressWarnings(\"preview\")\n" + 
				"	public static int foo(int i) throws MyException {\n" + 
				"		int v = -1;\n" + 
				"		try {\n" + 
				"			v = switch (i) {\n" + 
				"				case 0 -> switch(i) {\n" + 
				"							case 0 -> 1;\n" + 
				"							default -> throw new MyException();\n" + 
				"						  };\n" + 
				"				default -> 1;\n" + 
				"			};\n" + 
				"		} finally {\n" + 
				"			// do nothing\n" + 
				"		}\n" + 
				"		return v;\n" + 
				"	} \n" + 
				"	public static void main(String argv[]) {\n" + 
				"		try {\n" + 
				"			System.out.println(X.foo(0));\n" + 
				"		} catch (MyException e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n" + 
				"class MyException extends Exception {\n" + 
				"	private static final long serialVersionUID = 3461899582505930473L;	\n" + 
				"}"
			},
			"1");
	}
	public void testBug544943_2() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	@SuppressWarnings({ \"preview\" })\n" + 
				"	public static int foo(int i) throws Exception {\n" + 
				"		int v = switch (i) {\n" + 
				"			case 0 -> switch (i) {\n" + 
				"				case 0 -> 0;\n" + 
				"				default-> throw new Exception();\n" + 
				"				case 3 -> 3;\n" + 
				"				case 2 -> throw new Exception();\n" + 
				"				};\n" + 
				"			default -> 0;\n" + 
				"		};\n" + 
				"		return v;\n" + 
				"	}\n" + 
				"	public static void main(String argv[]) throws Exception {\n" + 
				"		System.out.println(X.foo(1));\n" + 
				"	}\n" +
				"}"
			},
			"0");
	}
}