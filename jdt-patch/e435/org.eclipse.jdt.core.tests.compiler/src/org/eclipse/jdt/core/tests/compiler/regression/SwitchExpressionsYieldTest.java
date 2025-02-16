/*******************************************************************************
 * Copyright (c) 2019, 2023 IBM Corporation and others.
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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.Excuse;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.JavacHasABug;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;


public class SwitchExpressionsYieldTest extends AbstractRegressionTest {
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug545567_18" };
	}

	public static Class<?> testClass() {
		return SwitchExpressionsYieldTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_14);
	}
	public SwitchExpressionsYieldTest(String testName){
		super(testName);
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
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forRelease(JavaCore.VERSION_14);
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forRelease(JavaCore.VERSION_14));
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
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forRelease(JavaCore.VERSION_14) :
			JavacTestOptions.forRelease(JavaCore.VERSION_14, javacAdditionalTestOptions);
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
						"	public static void main(String[] args) {\n" +
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
			"A switch expression should have at least one result expression\n" +
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
			"Case constant of type String is incompatible with switch selector type int\n" +
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
	public void testBug544073_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
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
			"1. ERROR in X.java (at line 6)\n" +
			"	int tw = switch (i) {\n" +
			"	                 ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 13)\n" +
			"	case \"hello\" -> throw new IOException(\"hello\");\n" +
			"	     ^^^^^^^\n" +
			"Case constant of type String is incompatible with switch selector type int\n" +
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
				"	          ^^^^^\n" +
				"Invalid expression as statement\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	case 1 -> 2;\n" +
				"	          ^\n" +
				"Invalid expression as statement\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	default -> 3;\n" +
				"	           ^\n" +
				"Invalid expression as statement\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_011() {
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
				"	           ^\n" +
				"Invalid expression as statement\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_012() {
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
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
					"Preview features enabled at an invalid source release level "+CompilerOptions.VERSION_11+", preview can be enabled only at source level "+AbstractRegressionTest.PREVIEW_ALLOWED_LEVEL+"\n" +
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
					"	            ^^\n" +
					"A switch labeled block in a switch expression must yield a value or throw an an exception\n" +
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
		Runner runner = new Runner();
		runner.testFiles = new String[] {
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

		runner.expectedCompilerLog =
						"----------\n" +
						"1. WARNING in X.java (at line 5)\n" +
						"	switch (day) {\n" +
						"	        ^^^\n" +
						"The enum constant TUESDAY needs a corresponding case label in this enum switch on Day\n" +
						"----------\n";
		runner.javacTestOptions = Excuse.EclipseHasSomeMoreWarnings;
		runner.runWarningTest();
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
				"1. ERROR in X.java (at line 7)\n" +
				"	case SUNDAY : System.out.println(Day.SUNDAY);\n" +
				"	     ^^^^^^\n" +
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
						"2. ERROR in X.java (at line 7)\n" +
						"	case SUNDAY, SATURDAY : \n" +
						"	     ^^^^^^\n" +
						"Duplicate case\n" +
						"----------\n" +
						"3. ERROR in X.java (at line 7)\n" +
						"	case SUNDAY, SATURDAY : \n" +
						"	             ^^^^^^^^\n" +
						"Duplicate case\n" +
						"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 */
	public void testBug544073_021() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
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

		runner.expectedCompilerLog =
				"----------\n" +
				"1. WARNING in X.java (at line 3)\n" +
				"	switch (day) {\n" +
				"	        ^^^\n" +
				"The enum constant MONDAY needs a corresponding case label in this enum switch on Day\n" +
				"----------\n";
		runner.javacTestOptions = Excuse.EclipseHasSomeMoreWarnings;
		runner.runWarningTest();
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
				"Case constant of type String is incompatible with switch selector type int\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * Switch multi-constant without break statement, reported
	 */
	public void testBug544073_028() {
		Map<String, String> options = getCompilerOptions();
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
				"1. ERROR in X.java (at line 8)\n" +
				"	case 3, 4: \n" +
				"	     ^\n" +
				"Duplicate case\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * Switch multi-constant with duplicate String literals
	 */
	public void testBug544073_031() {
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
				"1. ERROR in X.java (at line 8)\n" +
				"	case \"b\", \"c\": \n" +
				"	     ^^^\n" +
				"Duplicate case\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	/*
	 * Switch multi-constant with illegal qualified enum constant
	 */
	public void testBug544073_032() {
		if (this.complianceLevel >= ClassFileConstants.JDK21)
			return;
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
				expectedProblemLog);
	}
	public void testBug544073_033() {
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
				expectedProblemLog);
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
			"Breaking out of switch expressions not permitted\n" +
			"----------\n");
	}
	public void testBug544073_036() {
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
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
				expectedProblemLog);
	}
	public void testBug544073_037() {
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
		runConformTest(testFiles, expectedOutput);
	}
	public void testBug544073_038() {
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
		runConformTest(testFiles, expectedOutput);
	}
	public void testBug544073_039() {
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
		runConformTest(testFiles, expectedOutput);
	}
	public void testBug544073_040() {
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
		runConformTest(testFiles, expectedOutput);
	}
	public void testBug544073_041() {
		// require resolving/inferring of poly-switch-expression during ASTNode.resolvePolyExpressionArguments()
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
		runConformTest(testFiles, expectedOutput);
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
				expectedProblemLog);
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
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	return v;\n" +
				"	       ^\n" +
				"Type mismatch: cannot convert from Object to int\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug544073_057() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" +
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
				"\n" +
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
		runner.javacTestOptions = JavacHasABug.JavacBug8179483_switchExpression;
		runner.runNegativeTest();
	}
	public void testBug544073_070() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
						"\n" +
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
			getCompilerOptions(),
			"-Xlint:preview");
	}
	public void testBug544073_072() {
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
	public void testBug544073_074() {
		runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n"+
				"    A, B; \n"+
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
				"public enum X {\n"+
				"    A, B;\n"+
				"     \n"+
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
				"\n" +
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
			"Continue out of switch expressions not permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	continue;\n" +
			"	       ^^\n" +
			"A switch labeled block in a switch expression must yield a value or throw an an exception\n" +
			"----------\n");
	}
	public void testBug544073_077() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"\n"+
				"public class X {\n"+
				"\n"+
				"\n" +
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
				"		return 100;\n"+
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
			"Return within switch expressions not permitted\n" +
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
			"	                       ^^^^^^\n" +
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
			"1. ERROR in X.java (at line 6)\n" +
			"	case MONDAY, SUNDAY:\n" +
			"	     ^^^^^^\n" +
			"Duplicate case\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	case MONDAY, SUNDAY:\n" +
			"	             ^^^^^^\n" +
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
					"\n" +
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
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2323
	// [Switch Expression] Internal compiler error: java.lang.ClassCastException while compiling switch expression
	public void testIssue2323() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void f() {
						int[] array = null;
						(array = new int[1])[0] = 42;
					}
					public static int g() {
						int[] array = null;
						System.out.println(switch(10) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
					});
						return (array = new int[1])[0];
					}
				}
				"""
				},
				"");
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
				"1. ERROR in X.java (at line 4)\n" +
				"	yield();\n" +
				"	^^^^^^^\n" +
				"restricted identifier yield not allowed here - method calls need to be qualified\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	Zork();\n" +
				"	^^^^\n" +
				"The method Zork() is undefined for the type X\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
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
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n");
	}
	public void testBug547891_04() {
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
				"2. ERROR in X.java (at line 7)\n" +
				"	class yield {\n" +
				"	      ^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
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
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	class yield {\n" +
				"	      ^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n");
	}
	public void testBug547891_06() {
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
				"1. ERROR in X.java (at line 3)\n" +
				"	yield y;\n" +
				"	^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	Zork();\n" +
				"	^^^^\n" +
				"The method Zork() is undefined for the type X\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	class yield {\n" +
				"	      ^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
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
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	class yield {\n" +
				"	      ^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n");
	}
	public void testBug547891_08() {
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
				"1. ERROR in X.java (at line 3)\n" +
				"	yield y = null;\n" +
				"	^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	Zork();\n" +
				"	^^^^\n" +
				"The method Zork() is undefined for the type X\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	class yield {\n" +
				"	      ^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug547891_09() {
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
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n");
	}
	public void testBug547891_10() {
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
				"2. ERROR in X.java (at line 6)\n" +
				"	class yield {\n" +
				"	      ^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
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
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	class yield {\n" +
				"	      ^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n");
	}
	public void testBug547891_12() {
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
				"1. ERROR in X.java (at line 3)\n" +
				"	new yield();\n" +
				"	    ^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	Zork();\n" +
				"	^^^^\n" +
				"The method Zork() is undefined for the type X\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	class yield {\n" +
				"	      ^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
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
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	class yield {\n" +
				"	      ^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n");
	}
	public void testBug547891_14() {
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
				"1. ERROR in X.java (at line 3)\n" +
				"	yield[] y;\n" +
				"	^^^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	Zork();\n" +
				"	^^^^\n" +
				"The method Zork() is undefined for the type X\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	class yield {\n" +
				"	      ^^^^^\n" +
				"'yield' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 14\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog);
	}
	public void testBug547891_15() {
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
				"	\n"+
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
			message);
	}
	public void testBug547891_16() {
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
				"	\n"+
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
			message);
	}
	public void testBug547891_17() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"\n"+
					"	\n"+
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
					"	\n"+
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
					"	\n"+
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
					"	\n"+
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
				"	\n"+
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
			message);
	}
	public void testBug547891_22() {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"\n"+
					"	static int yield = 100;\n"+
					"	\n"+
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
					"	\n"+
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
				"	\n"+
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
				"	\n"+
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
				"	\n"+
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
				"	\n"+
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
				"	\n"+
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
				"	\n"+
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
				"  \n"+
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
				"  @SuppressWarnings({\"unused\" })\n"+
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
				"  \n"+
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
				"  \n"+
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
				"	\n" +
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
				"	\n" +
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
	public void testBug552764_001() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
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
				"Arrow in case statement supported from Java 14 onwards only\n" +
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
	public void testBug552764_002() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
				"	static int twice(int i) {\n" +
				"		return switch (i) {\n" +
				"			default -> 3;\n" +
				"		};\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(twice(3));\n" +
				"	}\n" +
				"}\n",
		};

		String expectedProblemLog =
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	return switch (i) {\n" +
				"			default -> 3;\n" +
				"		};\n" +
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Switch Expressions are supported from Java 14 onwards only\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	default -> 3;\n" +
				"	^^^^^^^\n" +
				"Arrow in case statement supported from Java 14 onwards only\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	public void testBug552764_003() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
		String[] testFiles = new String[] {
				"X.java",
				"public class X {\n" +
				"	static int twice(int i) {\n" +
				"		switch (i) {\n" +
				"			case 1, 2 : break;\n" +
				"			default : break;\n" +
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
				"	case 1, 2 : break;\n" +
				"	^^^^^^^^^\n" +
				"Multi-constant case labels supported from Java 14 onwards only\n" +
				"----------\n";
		this.runNegativeTest(
				testFiles,
				expectedProblemLog,
				null,
				true,
				options);
	}
	public void testBug558067_001() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"    public int foo(int i, int e) {\n"+
				"               LABEL: while (i == 0) {\n"+
				"            i = switch (e) {\n"+
				"                case 0 : {\n"+
				"                    for (;;) {\n"+
				"                        break LABEL; // NO error flagged\n"+
				"                    }\n"+
				"                    yield 1;\n"+
				"                }\n"+
				"                default : yield 2;\n"+
				"            };\n"+
				"        }\n"+
				"    return i;\n"+
				"    }\n"+
				"    public static void main(String argv[]) {\n"+
				"        new X().foo(0, 1);\n"+
				"     }\n"+
				"}\n"
			},
				"----------\n" +
				"1. WARNING in X.java (at line 3)\n" +
				"	LABEL: while (i == 0) {\n" +
				"	^^^^^\n" +
				"The label LABEL is never explicitly referenced\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	break LABEL; // NO error flagged\n" +
				"	^^^^^^^^^^^^\n" +
				"Breaking out of switch expressions not permitted\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 9)\n" +
				"	yield 1;\n" +
				"	^^^^^^^^\n" +
				"Unreachable code\n" +
				"----------\n");
	}
	public void testBug558067_002() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"    public int foo(int i, int e) {\n"+
				"   TOP:System.out.println(\"hello\");\n"+
				"          int x = switch(i) {\n"+
				"       case 0:\n"+
				"               LABEL: while (i == 0) {\n"+
				"            i = switch (e) {\n"+
				"                case 0 : {\n"+
				"                    for (;;) {\n"+
				"                        break LABEL;\n"+
				"                    }\n"+
				"                    yield 1;\n"+
				"                }\n"+
				"                default : yield 2;\n"+
				"            };\n"+
				"        }\n"+
				"       case 2: for(;;) break TOP;\n"+
				"       default: yield 0;\n"+
				"       };\n"+
				"    return i;\n"+
				"    }\n"+
				"    public static void main(String argv[]) {\n"+
				"        new X().foo(0, 1);\n"+
				"     }\n"+
				"} \n"
			},
				"----------\n" +
				"1. WARNING in X.java (at line 3)\n" +
				"	TOP:System.out.println(\"hello\");\n" +
				"	^^^\n" +
				"The label TOP is never explicitly referenced\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 6)\n" +
				"	LABEL: while (i == 0) {\n" +
				"	^^^^^\n" +
				"The label LABEL is never explicitly referenced\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 10)\n" +
				"	break LABEL;\n" +
				"	^^^^^^^^^^^^\n" +
				"Breaking out of switch expressions not permitted\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 12)\n" +
				"	yield 1;\n" +
				"	^^^^^^^^\n" +
				"Unreachable code\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 17)\n" +
				"	case 2: for(;;) break TOP;\n" +
				"	                ^^^^^^^^^^\n" +
				"Breaking out of switch expressions not permitted\n" +
				"----------\n");
	}
	public void testBug558067_003() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"    public int foo(int i, int e) {\n"+
				"               LABEL: while (i == 0) {\n"+
				"            i = switch (e) {\n"+
				"                case 0 : {\n"+
				"                    for (;;) {\n"+
				"                        continue LABEL;\n"+
				"                    }\n"+
				"                    yield 1;\n"+
				"                }\n"+
				"                default : yield 2;\n"+
				"            };\n"+
				"        }\n"+
				"    return i;\n"+
				"    }\n"+
				"    public static void main(String argv[]) {\n"+
				"        new X().foo(0, 1);\n"+
				"     }\n"+
				"}\n"
			},
				"----------\n" +
				"1. WARNING in X.java (at line 3)\n" +
				"	LABEL: while (i == 0) {\n" +
				"	^^^^^\n" +
				"The label LABEL is never explicitly referenced\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	continue LABEL;\n" +
				"	^^^^^^^^^^^^^^^\n" +
				"Continue out of switch expressions not permitted\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 9)\n" +
				"	yield 1;\n" +
				"	^^^^^^^^\n" +
				"Unreachable code\n" +
				"----------\n");
	}
	public void testBug558067_004() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"    public int foo(int i, int e) {\n"+
				"               LABEL: while (i == 0) {\n"+
				"            i = switch (e) {\n"+
				"                case 0 : {\n"+
				"                    switch(e) {\n"+
				"                      case 0 : {\n"+
				"                          break LABEL;\n"+
				"                      }\n"+
				"                    }\n"+
				"                    yield 1;\n"+
				"                }\n"+
				"                default : yield 2;\n"+
				"            };\n"+
				"        }\n"+
				"    return i;\n"+
				"    }\n"+
				"    public static void main(String argv[]) {\n"+
				"        new X().foo(0, 1);\n"+
				"     }\n"+
				"}\n"
			},
				"----------\n" +
				"1. WARNING in X.java (at line 3)\n" +
				"	LABEL: while (i == 0) {\n" +
				"	^^^^^\n" +
				"The label LABEL is never explicitly referenced\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	break LABEL;\n" +
				"	^^^^^^^^^^^^\n" +
				"Breaking out of switch expressions not permitted\n" +
				"----------\n");
	}
	public void testBug558067_005() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"    public int foo(int i, int e) {\n"+
				"               LABEL: while (i == 0) {\n"+
				"            i = switch (e) {\n"+
				"                case 0 : {\n"+
				"                    switch(e) {\n"+
				"                      case 0 : {\n"+
				"                          continue LABEL;\n"+
				"                      }\n"+
				"                    }\n"+
				"                    yield 1;\n"+
				"                }\n"+
				"                default : yield 2;\n"+
				"            };\n"+
				"        }\n"+
				"    return i;\n"+
				"    }\n"+
				"    public static void main(String argv[]) {\n"+
				"        new X().foo(0, 1);\n"+
				"     }\n"+
				"}\n"
			},
				"----------\n" +
				"1. WARNING in X.java (at line 3)\n" +
				"	LABEL: while (i == 0) {\n" +
				"	^^^^^\n" +
				"The label LABEL is never explicitly referenced\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	continue LABEL;\n" +
				"	^^^^^^^^^^^^^^^\n" +
				"Continue out of switch expressions not permitted\n" +
				"----------\n");
	}
		public void testConversion1() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	 public static int i = 0;\n" +
						"	 private static String typeName(byte arg){ return \"byte\"; }\n" +
						"    private static String typeName(char arg){ return \"char\"; }\n" +
						"    private static String typeName(short arg){ return \"short\"; }\n" +
						"    private static String typeName(int arg){ return \"int\"; }\n" +
						"    private static String typeName(float arg){ return \"float\"; }\n" +
						"    private static String typeName(long arg){ return \"long\"; }\n" +
						"    private static String typeName(double arg){ return \"double\"; }\n" +
						"    private static String typeName(String arg){ return \"String\"; }\n" +
						"		public static void main(String[] args) {\n" +
						"		 byte v1 = (byte)0;\n" +
						"        char v2 = ' ';\n" +
						"        var v = switch(i+1){\n" +
						"                    case 1 -> v2;\n" +
						"                    case 5 -> v1;\n" +
						"                    default -> v2;\n" +
						"        };\n" +
						"        System.out.print(typeName(v));\n" +
						"	}\n" +
						"}\n"
				},
				"int");
	}
	public void testConversion2() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	 public static int i = 0;\n" +
						"	 private static String typeName(byte arg){ return \"byte\"; }\n" +
						"    private static String typeName(char arg){ return \"char\"; }\n" +
						"    private static String typeName(short arg){ return \"short\"; }\n" +
						"    private static String typeName(int arg){ return \"int\"; }\n" +
						"    private static String typeName(float arg){ return \"float\"; }\n" +
						"    private static String typeName(long arg){ return \"long\"; }\n" +
						"    private static String typeName(double arg){ return \"double\"; }\n" +
						"    private static String typeName(String arg){ return \"String\"; }\n" +
						"		public static void main(String[] args) {\n" +
						"		 long v1 = 0L;\n" +
						"        double v2 = 0.;\n" +
						"        var v = switch(i+1){\n" +
						"                    case 1 -> v2;\n" +
						"                    case 5 -> v1;\n" +
						"                    default -> v2;\n" +
						"        };\n" +
						"        System.out.print(typeName(v));\n" +
						"	}\n" +
						"}\n"
				},
				"double");
	}
	public void testConversion3() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	 public static int i = 0;\n" +
						"	 private static String typeName(byte arg){ return \"byte\"; }\n" +
						"    private static String typeName(char arg){ return \"char\"; }\n" +
						"    private static String typeName(short arg){ return \"short\"; }\n" +
						"    private static String typeName(int arg){ return \"int\"; }\n" +
						"    private static String typeName(float arg){ return \"float\"; }\n" +
						"    private static String typeName(long arg){ return \"long\"; }\n" +
						"    private static String typeName(double arg){ return \"double\"; }\n" +
						"    private static String typeName(String arg){ return \"String\"; }\n" +
						"		public static void main(String[] args) {\n" +
						"		 long v1 = 0L;\n" +
						"        float v2 = 0.f;\n" +
						"        var v = switch(i+1){\n" +
						"                    case 1 -> v2;\n" +
						"                    case 5 -> v1;\n" +
						"                    default -> v2;\n" +
						"        };\n" +
						"        System.out.print(typeName(v));\n" +
						"	}\n" +
						"}\n"
				},
				"float");
	}
	public void testConversion4() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	 public static int i = 0;\n" +
						"	 private static String typeName(byte arg){ return \"byte\"; }\n" +
						"    private static String typeName(char arg){ return \"char\"; }\n" +
						"    private static String typeName(short arg){ return \"short\"; }\n" +
						"    private static String typeName(int arg){ return \"int\"; }\n" +
						"    private static String typeName(float arg){ return \"float\"; }\n" +
						"    private static String typeName(long arg){ return \"long\"; }\n" +
						"    private static String typeName(double arg){ return \"double\"; }\n" +
						"    private static String typeName(String arg){ return \"String\"; }\n" +
						"		public static void main(String[] args) {\n" +
						"		 short v1 = 0;\n" +
						"        char v2 = ' ';\n" +
						"        var v = switch(i+1){\n" +
						"                    case 1 -> v2;\n" +
						"                    case 5 -> v1;\n" +
						"                    default -> v2;\n" +
						"        };\n" +
						"        System.out.print(typeName(v));\n" +
						"	}\n" +
						"}\n"
				},
				"int");
	}
	public void testConversion5() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	 public static int i = 0;\n" +
						"    private static String typeName(char arg){ return \"char\"; }\n" +
						"    private static String typeName(int arg){ return \"int\"; }\n" +
						"    private static String typeName(float arg){ return \"float\"; }\n" +
						"    private static String typeName(long arg){ return \"long\"; }\n" +
						"    private static String typeName(double arg){ return \"double\"; }\n" +
						"    private static String typeName(String arg){ return \"String\"; }\n" +
						"		public static void main(String[] args) {\n" +
						"		 char v1 = 'a';\n" +
						"        var v = switch(i+1){\n" +
						"                    case 1 -> 200;\n" +
						"                    case 5 -> v1;\n" +
						"                    default -> v1;\n" +
						"        };\n" +
						"        System.out.print(typeName(v));\n" +
						"	}\n" +
						"}\n"
				},
				"char");
	}
	public void testBug545567_1() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    @SuppressWarnings({\"finally\"})\n" +
						"	public static void main(String[] args) {\n" +
						"    	int t = switch (0) {\n" +
						"        default -> {\n" +
						"            try {\n" +
						"                yield 1;\n" +
						"            }\n" +
						"            finally {\n" +
						"                yield 3;\n" +
						"            }\n" +
						"        }\n" +
						"     };\n" +
						"     System.out.println(t);\n" +
						"    }\n" +
						"}\n" +
						"\n"
				},
				"3");
	}
	public void testBug545567_2() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    @SuppressWarnings({ \"finally\"})\n" +
						"	public static void main(String[] args) {\n" +
						"    	float t = switch (0) {\n" +
						"        default -> {\n" +
						"            try {\n" +
						"                yield 1;\n" +
						"            }\n" +
						"            finally {\n" +
						"                yield 3;\n" +
						"            }\n" +
						"        }\n" +
						"     };\n" +
						"     System.out.println(t);\n" +
						"    }\n" +
						"}\n" +
						"\n"
				},
				"3.0");
	}
	public void testBug545567_3() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    @SuppressWarnings({ \"finally\"})\n" +
						"	public static void main(String[] args) {\n" +
						"    	String t = switch (0) {\n" +
						"        default -> {\n" +
						"            try {\n" +
						"                yield \"one\";\n" +
						"            }\n" +
						"            finally {\n" +
						"                yield \"three\";\n" +
						"            }\n" +
						"        }\n" +
						"     };\n" +
						"     System.out.println(t);\n" +
						"    }\n" +
						"}\n" +
						"\n"
				},
				"three");
	}
	public void testBug545567_4() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    @SuppressWarnings({\"finally\" })\n" +
						"	public static void main(String[] args) {\n" +
						"    	String t = switch (0) {\n" +
						"        default -> {\n" +
						"            try {\n" +
						"                yield \"one\";\n" +
						"            }\n" +
						"            catch (Exception ex) {\n" +
						"                yield \"two\";\n" +
						"            }\n" +
						"            finally {\n" +
						"                yield \"three\";\n" +
						"            }\n" +
						"        }\n" +
						"     };\n" +
						"     System.out.println(t);\n" +
						"    }\n" +
						"}\n" +
						"\n"
				},
				"three");
	}
	public void testBug545567_5() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    @SuppressWarnings({ \"finally\" })\n" +
						"	public static void main(String[] args) {\n" +
						"    	String t = switch (0) {\n" +
						"        default -> {\n" +
						"            try {\n" +
						"                yield \"one\";\n" +
						"            }\n" +
						"            catch (Exception ex) {\n" +
						"            }\n" +
						"            yield \"zero\";\n" +
						"        }\n" +
						"     };\n" +
						"     System.out.print(t);\n" +
						"    }\n" +
						"}\n" +
						"\n"
				},
				"one");
	}

	public void testBug545567_5_1() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    public String toString() { return "some X"; }
						    public static void main(String[] args) {
						    	String t = switch (0) {
						        default -> {
						            try {
						                yield new X().toString();
						            }
						            catch (Exception ex) {
						            }
						            yield "zero";
						        }
						     };
						     System.out.print(t);
						    }

						    static int foo() {
						    	try {
						    		return 42;
						    	} catch (Exception ex) {

						    	}
						    	return -1;
						    }
						}
						"""
				},
				"some X");
	}

	public void testBug545567_6() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    @SuppressWarnings({ \"finally\"})\n" +
						"	public static void main(String[] args) {\n" +
						"    	(new X()).foo(switch (0) {\n" +
						"        default -> {\n" +
						"            try {\n" +
						"                yield \"one\";\n" +
						"            }\n" +
						"            finally {\n" +
						"            	yield \"zero\";\n" +
						"            }\n" +
						"        }\n" +
						"     });\n" +
						"    }\n" +
						"     public void foo (String str) {\n" +
						"     	System.out.print(str);\n" +
						"    }\n" +
						"}\n" +
						"\n"
				},
				"zero");
	}
	public void testBug545567_7() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    @SuppressWarnings({ \"finally\"})\n" +
						"	public static void main(String[] args) {\n" +
						"    	System.out.print(switch (0) {\n" +
						"        default -> {\n" +
						"            try {\n" +
						"                yield \"one\";\n" +
						"            }\n" +
						"            finally {\n" +
						"            	yield \"zero\";\n" +
						"            }\n" +
						"        }\n" +
						"     });\n" +
						"    }\n" +
						"}\n" +
						"\n"
				},
				"zero");
	}
	public void testBug545567_8() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    @SuppressWarnings({ \"finally\"})\n" +
						"	public static void main(String[] args) {\n" +
						"    	System.out.print(switch (0) {\n" +
						"        default -> {\n" +
						"            try {\n" +
						"                yield 1;\n" +
						"            }\n" +
						"            catch (Exception ex) {\n" +
						"                yield 2;\n" +
						"            }\n" +
						"            finally {\n" +
						"                yield 3;\n" +
						"            }\n" +
						"        }\n" +
						"     });\n" +
						"    }\n" +
						"}\n" +
						"\n"
				},
				"3");
	}
	public void testBug545567_9() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"       public static void main(String[] args) {\n"+
					"       new X().foo(args);\n"+
					"    }\n"+
					"    @SuppressWarnings({ \"finally\" })\n"+
					"       public void foo(String[] args) {\n"+
					"       int t = switch (0) {\n"+
					"        default -> {\n"+
					"             try {\n"+
					"                yield 1;\n"+
					"            }\n"+
					"            catch (Exception ex) {\n"+
					"                yield 2; \n"+
					"            }\n"+
					"            finally {\n"+
					"                yield 3;\n"+
					"            }\n"+
					"        }       \n"+
					"     };\n"+
					"       t += switch (0) {\n"+
					"    default -> {\n"+
					"         try {\n"+
					"            yield 1;\n"+
					"        }\n"+
					"        catch (Exception ex) {\n"+
					"            yield 2; \n"+
					"        }\n"+
					"        finally {\n"+
					"            yield 3;\n"+
					"        }\n"+
					"    }       \n"+
					" };\n"+
					"     System.out.println(t);\n"+
					"    } \n"+
					"}\n"
				},
				"6");
	}
	public void testBug545567_10() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
								"       public static void main(String[] args) {\n"+
								"       new X().foo(args);\n"+
								"    }\n"+
								"    @SuppressWarnings({ \"finally\" })\n"+
								"       public void foo(String[] args) {\n"+
								"       int k = 0;\n"+
								"       int t = switch (0) {\n"+
								"        default -> {\n"+
								"             try {\n"+
								"                k = switch (0) {\n"+
								"                   default -> {\n"+
								"                        try {\n"+
								"                           yield 10;\n"+
								"                       }\n"+
								"                       catch (Exception ex) {\n"+
								"                           yield 20; \n"+
								"                       }\n"+
								"                       finally {\n"+
								"                           yield 30;\n"+
								"                       }\n"+
								"                   }       \n"+
								"                };\n"+
								"            }\n"+
								"            catch (Exception ex) {\n"+
								"                yield 2; \n"+
								"            }\n"+
								"            finally {\n"+
								"                yield 3;\n"+
								"            }\n"+
								"        }       \n"+
								"     };\n"+
								"     System.out.println(t + k);\n"+
								"    } \n"+
								"}\n"
				},
				"33");
	}
	public void testBug545567_11() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
								"       public static void main(String[] args) {\n"+
								"       new X().foo(args);\n"+
								"    }\n"+
								"    @SuppressWarnings({ \"finally\" })\n"+
								"       public void foo(String[] args) {\n"+
								"       int k = 0;\n"+
								"       int t = switch (0) {\n"+
								"        default -> {\n"+
								"             try {\n"+
								"                k = switch (0) {\n"+
								"                   default -> {\n"+
								"                        try {\n"+
								"                           yield 10;\n"+
								"                       }\n"+
								"                       catch (Exception ex) {\n"+
								"                           yield 20; \n"+
								"                       }\n"+
								"                   }       \n"+
								"                };\n"+
								"            }\n"+
								"            catch (Exception ex) {\n"+
								"                yield 2; \n"+
								"            }\n"+
								"            finally {\n"+
								"                yield 3;\n"+
								"            }\n"+
								"        }       \n"+
								"     };\n"+
								"     System.out.println(t + k);\n"+
								"    } \n"+
								"}\n"
				},
				"13");
	}
	public void testBug545567_12() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
								"       public static void main(String[] args) {\n"+
								"       new X().foo(args);\n"+
								"    }\n"+
								"    @SuppressWarnings({ \"finally\" })\n"+
								"       public void foo(String[] args) {\n"+
								"       int k = 0;\n"+
								"       int t = switch (0) {\n"+
								"        default -> {\n"+
								"             try {\n"+
								"                k = switch (0) {\n"+
								"                   default -> {\n"+
								"                        try {\n"+
								"                           yield 10;\n"+
								"                       }\n"+
								"                       catch (Exception ex) {\n"+
								"                           yield 20; \n"+
								"                       }\n"+
								"                       finally {\n"+
								"                           yield 30;\n"+
								"                       }\n"+
								"                   }       \n"+
								"                };\n"+
								"            }\n"+
								"            finally {\n"+
								"                yield 3;\n"+
								"            }\n"+
								"        }       \n"+
								"     };\n"+
								"     System.out.println(t + k);\n"+
								"    } \n"+
								"}\n"
				},
				"33");
	}
	public void testBug545567_13() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    @SuppressWarnings({ \"finally\" })\n"+
				"       public static void main(String[] args) {\n"+
				"        System.out.println(switch (1) {\n"+
				"        case 0 -> {yield 100;}\n"+
				"           default -> {  \n"+
				"                try {\n"+
				"                   yield 1;\n"+
				"               }\n"+
				"               catch (Exception ex) {\n"+
				"                   yield 2;\n"+
				"                }\n"+
				"               finally {\n"+
				"                   yield 3; \n"+
				"               }\n"+
				"           }  \n"+
				"        } + switch (10) {\n"+
				"        case 0 -> {yield 1024;}\n"+
				"        default -> {  \n"+
				"             try {\n"+
				"                yield 10;\n"+
				"            }\n"+
				"            catch (Exception ex) {\n"+
				"                yield 20;\n"+
				"             }\n"+
				"            finally {\n"+
				"                yield 30; \n"+
				"            }\n"+
				"        }  \n"+
				"     });  \n"+
				"    }\n"+
				"}\n"
			},
			"33");
	}
	public void testBug545567_14() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    @SuppressWarnings({ \"finally\" })\n"+
				"       public static void main(String[] args) {\n"+
				"        System.out.println(switch (1) {\n"+
				"        case 0 -> {yield 100;}\n"+
				"           default -> {  \n"+
				"                try {\n"+
				"                   yield 1;\n"+
				"               }\n"+
				"               catch (Exception ex) {\n"+
				"                   yield 2;\n"+
				"                }\n"+
				"               finally {\n"+
				"                 yield switch (10) {\n"+
				"                   case 0 -> {yield 1024;}\n"+
				"                   default -> {  \n"+
				"                        try {\n"+
				"                           yield 10;\n"+
				"                       }\n"+
				"                       catch (Exception ex) {\n"+
				"                           yield 20;\n"+
				"                        }\n"+
				"                       finally {\n"+
				"                           yield 30; \n"+
				"                       }\n"+
				"                   }  \n"+
				"                };               }\n"+
				"           }  \n"+
				"        });  \n"+
				"    }\n"+
				"}\n"
			},
			"30");
	}
	public void testBug545567_15() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    @SuppressWarnings({ \"finally\" })\n"+
				"       public static void main(String[] args) {\n"+
				"        System.out.println(switch (1) {\n"+
				"        case 0 -> {yield 100;}\n"+
				"           default -> {  \n"+
				"                try {\n"+
				"                       yield 1;\n"+
				"               }\n"+
				"               catch (Exception ex) {\n"+
				"                   yield 2;\n"+
				"                }\n"+
				"               finally {\n"+
				"                   System.out.println(switch (1) {\n"+
				"                    default -> {yield 100;}});\n"+
				"                  yield 1;\n"+
				"                }\n"+
				"           }  \n"+
				"        });  \n"+
				"    }\n"+
				"}\n"
			},
			"100\n1");
	}
	public void testBug545567_16() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    @SuppressWarnings({ \"finally\" })\n"+
				"       public static void main(String[] args) {\n"+
				"        System.out.println(switch (1) {\n"+
				"        case 0 -> {yield 100;}\n"+
				"           default -> {   \n"+
				"                try {\n"+
				"                    yield switch (10) {\n"+
				"                    case 0 -> {yield 1024;}\n"+
				"                    default -> {   \n"+
				"                         try {\n"+
				"                            yield 10; \n"+
				"                        }   \n"+
				"                        catch (Exception ex) {\n"+
				"                            yield 20; \n"+
				"                         }   \n"+
				"                        finally {\n"+
				"                            yield 30; \n"+
				"                        }   \n"+
				"                    }   \n"+
				"                 };                 \n"+
				"               }   \n"+
				"               catch (Exception ex) {\n"+
				"                   yield 2;\n"+
				"                }   \n"+
				"               finally {\n"+
				"                 yield 3;               }   \n"+
				"           }   \n"+
				"        });  \n"+
				"    }   \n"+
				"}\n"
			},
			"3");
	}
	public void testBug545567_17() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X { \n"+
				"    @SuppressWarnings({ \"finally\" })\n"+
				"       public static void main(String[] args) {\n"+
				"        System.out.println(switch (1) {\n"+
				"        case 0 -> {yield 100;}\n"+
				"           default -> {   \n"+
				"                try {\n"+
				"                    System.out.println( switch (10) {\n"+
				"                    case 0 -> {yield 1024;}\n"+
				"                    default -> {   \n"+
				"                         try {\n"+
				"                            yield 10; \n"+
				"                        }   \n"+
				"                        catch (Exception ex) {\n"+
				"                            yield 20; \n"+
				"                         }    \n"+
				"                        finally {\n"+
				"                            yield 30; \n"+
				"                        }   \n"+
				"                    }   \n"+
				"                 }); \n"+
				"                   yield 1;   \n"+
				"               }   \n"+
				"               catch (Exception ex) {\n"+
				"                   yield 2;\n"+
				"                }   \n"+
				"               finally {\n"+
				"                 yield 3;               }   \n"+
				"           }   \n"+
				"        });  \n"+
				"    }   \n"+
				"}\n"
			},
			"30\n"+
			"3");
	}
	public void testBug545567_18() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X { \n"+
				"       public static void main(String[] args) {\n"+
				"       new X().foo(args);\n"+
				"    }   \n"+
				"    @SuppressWarnings({ \"finally\" })\n"+
				"       public void foo(String[] args) {\n"+
				"       int t = 0;\n"+
				"       t += switch (200) {\n"+
				"       case 0 -> {yield 100;}\n"+
				"        default -> {\n"+
				"             try {\n"+
				"                yield 1;\n"+
				"            }   \n"+
				"            catch (Exception ex) {\n"+
				"                yield 2;  \n"+
				"            }   \n"+
				"            finally {\n"+
				"                yield 3;\n"+
				"            }   \n"+
				"        }\n"+
				"     };\n"+
				"     System.out.println(t);\n"+
				"    }   \n"+
				"}\n"
			},
			"3");
	}
	public void testBug545567_19() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    @SuppressWarnings({ \"finally\" })\n"+
				"       public static void main(String[] args) {\n"+
				"        System.out.println(switch (1) {\n"+
				"           default -> {   \n"+
				"                try {  \n"+
				"                    yield switch (10) {\n"+
				"                    default -> {   \n"+
				"                         try {\n"+
				"                            yield 10; \n"+
				"                        }   \n"+
				"                        catch (Exception ex) {\n"+
				"                            yield 20; \n"+
				"                         }   \n"+
				"                        finally {\n"+
				"                            yield 30; \n"+
				"                         }   \n"+
				"                    }   \n"+
				"                 };                 \n"+
				"               }   \n"+
				"               catch (Exception ex) {\n"+
				"                   yield 2;\n"+
				"                }   \n"+
				"               finally {\n"+
				"                 yield 3;               }     \n"+
				"           }   \n"+
				"        });   \n"+
				"    }   \n"+
				"} \n"+
				"\n"
			},
			"3");
	}
	// test with Autocloseable
	public void testBug545567_20() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    @SuppressWarnings({ \"finally\" })\n"+
				"       public static void main(String[] args) {\n"+
				"        System.out.println(switch (1) {\n"+
				"           default -> {   \n"+
				"                try(Y y = new Y();) { \n"+
				"                       yield  1;\n"+
				"                }\n"+
				"               catch (Exception ex) {\n"+
				"                   yield 2;\n"+
				"                }   \n"+
				"               finally {\n"+
				"                 yield 3;\n" +
				"               }\n"+
				"           }\n"+
				"        });\n"+
				"    }\n"+
				"} \n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {\n"+
				"               // do nothing\n"+
				"       }\n"+
				"}\n"
			},
			"3");
	}
	public void testBug545567_21() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    @SuppressWarnings({ \"finally\" })\n"+
				"       public static void main(String[] args) {\n"+
				"        System.out.println(switch (1) {\n"+
				"           default -> {   \n"+
				"                try(Y y = new Y();) { \n"+
				"                       yield  10;\n"+
				"                }\n"+
				"               catch (Exception ex) {\n"+
				"                }   \n"+
				"                 yield 3;\n" +
				"           }\n"+
				"        });\n"+
				"    }\n"+
				"} \n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {\n"+
				"               // do nothing\n"+
				"       }\n"+
				"}\n"
			},
			"10");
	}
	// Disabled until https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3259 and
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3258 are comprehensively
	// resolved. Same issue, but was "compensated" for earlier
	public void _testBug545567_22() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"       @SuppressWarnings({ \"finally\" })\n"+
				"       public static void main(String[] args) {\n"+
				"               int argslength = args.length;\n"+
				"               int t = switch (1) {\n"+
				"                       case 0 -> {\n"+
				"                               yield 100;\n"+
				"                       }\n"+
				"                       default -> {\n"+
				"                               try (Y y = new Y();){\n"+
				"                                               if (argslength < 1)\n"+
				"                                               yield 10;\n"+
				"                                               else\n"+
				"                                                       yield 12;\n"+
				"                               } catch (Exception ex) {\n"+
				"                                       yield 2;\n"+
				"                               } finally {\n"+
				"                                       yield 3;\n"+
				"                               }\n"+
				"                       }\n"+
				"               };   \n"+
				"               System.out.println(t);\n"+
				"       }\n"+
				"}\n"+
				"      \n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {\n"+
				"               // do nothing\n"+
				"       } \n"+
				"}\n"
			},
			"3");
	}
	public void testBug545567_23() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"       @SuppressWarnings({ \"finally\" })\n"+
				"       public static void main(String[] args) {\n"+
				"               int t = switch (1) {\n"+
				"                       case 0 -> {\n"+
				"                               yield 100;\n"+
				"                       }\n"+
				"                       default -> {\n"+
				"                               try {\n"+
				"                                       throw new Exception();\n"+
				"                               } catch (Exception ex) {\n"+
				"                                       yield 2;\n"+
				"                               } finally {\n"+
				"                                       yield 3;\n"+
				"                               }\n"+
				"                       }\n"+
				"               };   \n"+
				"               System.out.println(t);\n"+
				"       }\n"+
				"}\n"+
				"      \n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {\n"+
				"               // do nothing\n"+
				"       } \n"+
				"}\n"
			},
			"3");
	}
	public void testBug545567_24() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   new X().foo();\n"+
				" }\n"+
				" @SuppressWarnings({ \"finally\" })\n"+
				" public  void foo() {\n"+
				"   int t = switch (1) {\n"+
				"     case 0 -> {\n"+
				"       yield bar(100);\n"+
				"     }\n"+
				"     default -> {\n"+
				"       final Y y2 = new Y();\n"+
				"       try (Y y = new Y(); y2){\n"+
				"           yield bar(10);\n"+
				"       } catch (Exception ex) {\n"+
				"         yield bar(2);\n"+
				"       } finally {\n"+
				"         yield bar(3);\n"+
				"       }\n"+
				"     }\n"+
				"   };   \n"+
				"   System.out.println(t);\n"+
				" }\n"+
				" public int bar(int i) {\n"+
				"   return i;\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"class Y implements AutoCloseable {\n"+
				" @Override\n"+
				" public void close() throws Exception {\n"+
				"   // do nothing\n"+
				" }\n"+
				"}"
			},
			"3");
	}
	public void testBug545567_25() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   new X().foo();\n"+
				" }\n"+
				" @SuppressWarnings({ \"finally\" })\n"+
				" public  void foo() {\n"+
				"   int t = switch (1) {\n"+
				"     case 0 -> {\n"+
				"       yield bar(100);\n"+
				"     }\n"+
				"     default -> {\n"+
				"       final Y y2 = new Y();\n"+
				"       try (Y y = new Y(); y2){\n"+
				"           yield new X().bar(10);\n"+
				"       } catch (Exception ex) {\n"+
				"         yield bar(2);\n"+
				"       } finally {\n"+
				"         yield new X().bar(3);\n"+
				"       }\n"+
				"     }\n"+
				"   };   \n"+
				"   System.out.println(t);\n"+
				" }\n"+
				" public int bar(int i) {\n"+
				"   return i;\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"class Y implements AutoCloseable {\n"+
				" @Override\n"+
				" public void close() throws Exception {\n"+
				"   // do nothing\n"+
				" }\n"+
				"}"
			},
			"3");
	}
	public void testBug571929_normal() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" public static void main(String[] args) {\n" +
				"   System.out.println(foo(\"a\"));\n" +
				" }\n" +
				" private static boolean foo(String s) {\n" +
				"  bar(0L);\n" +
				"  return switch (s) {\n" +
				"    case \"a\" -> {\n" +
				"      try {\n" +
				"        yield true;\n" +
				"      } finally {\n" +
				"      }\n" +
				"    }\n" +
				"    default -> false;\n" +
				"  };\n" +
				" }\n" +
				" private static void bar(long l) {}\n" +
				"}"
			},
			"true");
	}
	public void testBug571929_lambda() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" public static void main(String[] args) {\n" +
				"   System.out.println(foo(\"a\"));\n" +
				" }\n" +
				" static long m = 0L;\n" +
				" private static boolean foo(String s) {\n" +
				"  long l = m;\n" +
				"  // capture l\n" +
				"  Runnable r = () -> bar(l);\n" +
				"  return switch (s) {\n" +
				"    case \"a\" -> {\n" +
				"      try {\n" +
				"        yield true;\n" +
				"      } finally {\n" +
				"      }\n" +
				"    }\n" +
				"    default -> false;\n" +
				"  };\n" +
				" }\n" +
				" private static void bar(long l) {}\n" +
				"}"
			},
			"true");
	}
	public void testBug561762_001() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"       public static void main(String[] args) {\n"+
					"               new X().foo(1);\n"+
					"       }\n"+
					"       @SuppressWarnings({ \"finally\" })\n"+
					"       public  void foo(int i) {\n"+
					"               int t = switch (1) { \n"+
					"                       case 0 -> {\n"+
					"                               yield 0;\n"+
					"                       }\n"+
					"                       default -> {\n"+
					"                               I lam2 = (x) ->  {\n"+
					"                                               yield 2000;\n"+
					"                               };\n"+
					"                               yield 1;\n"+
					"                       }\n"+
					"               };\n"+
					"               System.out.println(t);\n"+
					"       }\n"+
					"}\n"+
					"interface I {\n"+
					"       public int apply(int i);\n"+
					"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 13)\n" +
				"	yield 2000;\n" +
				"	^^^^^^^^^^^\n" +
				"yield outside of switch expression\n" +
				"----------\n");

	}
	public void testBug561766_001() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    @SuppressWarnings({ \"finally\" })\n"+
					"       public static void main(String[] args) {\n"+
					"        System.out.println(switch (1) {\n"+
					"        case 0 -> {yield switch(0) {}\n"+
					"        } \n"+
					"           default -> {\n"+
					"                  yield 3;\n"+
					"           }\n"+
					"        });\n"+
					"    }\n"+
					"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case 0 -> {yield switch(0) {}\n" +
				"	                            ^\n" +
				"Syntax error, insert \";\" to complete BlockStatements\n" +
				"----------\n");

	}
	public void testBug561766_002() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    @SuppressWarnings({ \"finally\" })\n"+
					"       public static void main(String[] args) {\n"+
					"        System.out.println(switch (1) {\n"+
					"        case 0 -> {yield 100;}\n"+
					"           default -> {  \n"+
					"                try {\n"+
					"                       yield switch(0) {\n"+
					"               }\n"+
					"               catch (Exception ex) {\n"+
					"                   yield 2;\n"+
					"                }\n"+
					"               finally {\n"+
					"                  yield 3;\n"+
					"                }\n"+
					"           }  \n"+
					"        });  \n"+
					"    }\n"+
					"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	}\n" +
				"	^\n" +
				"Syntax error, insert \";\" to complete YieldStatement\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 9)\n" +
				"	}\n" +
				"	^\n" +
				"Syntax error, insert \"}\" to complete Block\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 18)\n" +
				"	}\n" +
				"	^\n" +
				"Syntax error on token \"}\", delete this token\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 19)\n" +
				"	}\n" +
				"	^\n" +
				"Syntax error, insert \"}\" to complete ClassBody\n" +
				"----------\n");

	}

	public void testBug562129() {
		if (this.complianceLevel < ClassFileConstants.JDK14) return;
		runNegativeTest(
			new String[] {
				"SwitchExpressionError.java",
				"class SwitchExpressionError {\n" +
				"\n" +
				"    static boolean howMany(int k) {\n" +
				"        return false || switch (k) {\n" +
				"            case 1 -> true;\n" +
				"            case 2 -> Boolean.FALSE;\n" +
				"            case 3 -> r;\n" +
				"        };\n" +
				"    }\n" +
				"\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in SwitchExpressionError.java (at line 4)\n" +
			"	return false || switch (k) {\n" +
			"	                        ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n" +
			"2. ERROR in SwitchExpressionError.java (at line 7)\n" +
			"	case 3 -> r;\n" +
			"	          ^\n" +
			"r cannot be resolved to a variable\n" +
			"----------\n");
	}
	public void testBug572121() {
		Map<String, String> compilerOptions = getCompilerOptions();
		// must disable this option to trigger compilation restart
		compilerOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.DISABLED);
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						" private void foo(int i) {\n" +
						" }\n" +
						"\n" +
						" private static void bar() {\n" +
						" }\n" +
						"\n" +
						" public static void main(String[] args) {\n" +
						"  if (f) {\n" +
						"   Object o = switch (j) {\n" +
						"    default -> {\n" +
						"     try {\n" +
						"      bar();\n" +
						"     } catch (Throwable e) {\n" +
						"     }\n" +
						"     yield null;\n" +
						"    }\n" +
						"   };\n" +
						"  }\n" +
						"  int i = 0;\n" +
						"  x.foo(i++);\n" +
						" }\n" +
						"\n" +
						" private static boolean f = true;\n" +
						" private static int j;\n" +
						" private static X x = new X();\n" +
						"}"
				},
				"",
				compilerOptions
				);
	}
	public void testBug562198_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    int a[] = {1, 2, 3};\n"+
				"    public int foo() {\n"+
				"        return switch (0) {\n"+
				"               case 0 -> {\n"+
				"                       yield a[0];\n"+
				"               }\n"+
				"            default -> {\n"+
				"                try {\n"+
				"                    // do nothing\n"+
				"                } finally {\n"+
				"                    // do nothing\n"+
				"                }\n"+
				"                yield 0;\n"+
				"            }\n"+
				"        };\n"+
				"    }\n"+
				"    public static void main(String[] args) {\n"+
				"               System.out.println(new X().foo());\n"+
				"       }\n"+
				"}\n"
			},
			"1");
	}
	public void testBug562728_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"       static public void main (String[] args) {\n"+
				"               int a = 0x21;\n"+
				"               int b = 0xff;\n"+
				"               switch (a) {\n"+
				"               case 0x21 -> {\n"+
				"                       switch (b) {\n"+
				"                       default -> System.out.println(\"default\");\n"+
				"                       }\n"+
				"               }\n"+
				"               case 0x3b -> System.out.println(\"3b <- WTH?\");\n"+
				"               }\n"+
				"       }\n"+
				"}\n"
			},
			"default");
	}
	public void testBug562728_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static public void main (String[] args) {\n"+
				"   int a = 0x21;\n"+
				"   int b = 0xff;\n"+
				"   switch (a) {\n"+
				"     case 0x21 -> {\n"+
				"       switch (b) {\n"+
				"         default -> System.out.println(\"default\");\n"+
				"       }\n"+
				"       return;\n"+
				"     }\n"+
				"     case 0x3b -> System.out.println(\"3b <- WTH?\");\n"+
				"   }\n"+
				" }\n"+
				"}\n"
			},
			"default");
	}
	public void testBug562728_003() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					" static public void main (String[] args) throws Exception {\n"+
					"   int a = 0x21;\n"+
					"   int b = 0xff;\n"+
					"   switch (a) {\n"+
					"     case 0x21 -> {\n"+
					"       switch (b) {\n"+
					"         default -> throw new Exception();\n"+
					"       }\n"+
					"       return; \n"+
					"     }\n"+
					"     case 0x3b -> System.out.println(\"3b <- WTH?\");\n"+
					"   }\n"+
					" }\n"+
					"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	return; \n" +
				"	^^^^^^^\n" +
				"Unreachable code\n" +
				"----------\n");

	}
	public void testBug562728_004() {
		this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"       static public void main (String[] args) throws Exception {\n"+
			"               int a = 0x21;\n"+
			"               int b = 0xff;\n"+
			"               Zork();\n"+
			"               switch (a) {\n"+
			"               case 0x21 -> {\n"+
			"                       switch (b) {\n"+
			"                       default -> {\n"+
			"                               for (;;) {\n"+
			"                                       if (b > 1)\n"+
			"                                       throw new Exception();\n"+
			"                               }\n"+
			"                       }\n"+
			"                       }\n"+
			"               }\n"+
			"               case 0x3b -> System.out.println(\"3b <- WTH?\");\n"+
			"               }\n"+
			"       }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	Zork();\n" +
		"	^^^^\n" +
		"The method Zork() is undefined for the type X\n" +
		"----------\n");
	}
	public void testBug562728_005() {
		this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {                        \n"+
			"        public static int foo(int i) {  \n"+
			"                int v;                  \n"+
			"                int t = switch (i) {    \n"+
			"                case 0 : {              \n"+
			"                        yield 0;        \n"+
			"                }                       \n"+
			"                case 2 :v = 2;\n"+
			"                default :v = 2;\n"+
			"                };                      \n"+
			"                return t;               \n"+
			"        }                               \n"+
			"                                        \n"+
			"        public boolean bar() {          \n"+
			"                return true;            \n"+
			"        }\n"+
			"        public static void main(String[] args) {\n"+
			"                System.out.println(foo(3));\n"+
			"        }                               \n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	default :v = 2;\n" +
		"	            ^^\n" +
		"A switch labeled block in a switch expression must yield a value or throw an an exception\n" +
		"----------\n");
	}
	public void testBug562728_006() {
		this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {                        \n"+
			"        public static int foo(int i) {  \n"+
			"                int v;                  \n"+
			"                int t = switch (i) {    \n"+
			"                case 0 -> {              \n"+
			"                        yield 0;        \n"+
			"                }                       \n"+
			"                case 2 ->{v = 2;}\n"+
			"                default ->{v = 2;}\n"+
			"                };                      \n"+
			"                return t;               \n"+
			"        }                               \n"+
			"                                        \n"+
			"        public boolean bar() {          \n"+
			"                return true;            \n"+
			"        }\n"+
			"        public static void main(String[] args) {\n"+
			"                System.out.println(foo(3));\n"+
			"        }                               \n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	case 2 ->{v = 2;}\n" +
		"	               ^^\n" +
		"A switch labeled block in a switch expression must yield a value or throw an an exception\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	default ->{v = 2;}\n" +
		"	                ^^\n" +
		"A switch labeled block in a switch expression must yield a value or throw an an exception\n" +
		"----------\n");
	}
    public void testBug562728_007() {
        this.runNegativeTest(
        new String[] {
                "X.java",
                "public class X {                        \n"+
                "        public static int foo(int i) {  \n"+
                "                int v;                  \n"+
                "                int t = switch (i) {    \n"+
                "                case 0 -> {              \n"+
                "                     return 1;\n"+
                "                }                       \n"+
                "                default ->100;\n"+
                "                };                      \n"+
                "                return t;               \n"+
                "        }                               \n"+
                "                                        \n"+
                "        public boolean bar() {          \n"+
                "                return true;            \n"+
                "        }\n"+
                "        public static void main(String[] args) {\n"+
                "                System.out.println(foo(3));\n"+
                "        }                               \n"+
                "}\n"
        },
        "----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	return 1;\n" +
		"	^^^^^^^^^\n" +
		"Return within switch expressions not permitted\n" +
        "----------\n");
}
	public void testBug563023_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X { \n"+
				" static public int foo(int a, int b){\n"+
				"   int t = switch (a) {\n"+
				"     default -> {\n"+
				"       switch (b) {\n"+
				"            default -> {\n"+
				"              yield 0;\n"+
				"            }\n"+
				"       }      \n"+
				"     }\n"+
				"   };\n"+
				"   return t;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo(0, 0));\n"+
				" }\n"+
				"}\n"
			},
			"0");
	}
    public void testBug563023_002() {
        this.runNegativeTest(
    		new String[] {
    			"X.java",
    			"public class X { \n"+
    			" static public int foo(int a, int b){\n"+
    			"   int t = switch (a) {\n"+
    			"     default -> {\n"+
    			"       switch (b) {\n"+
    			"            case 0 -> {\n"+
    			"              break;\n"+
    			"            }\n"+
    			"            default -> {\n"+
    			"              yield 0;\n"+
    			"            }\n"+
    			"       }      \n"+
    			"     }\n"+
    			"   };\n"+
    			"   return t;\n"+
    			" }\n"+
    			" public static void main(String[] args) {\n"+
    			"   System.out.println(X.foo(0, 0));\n"+
    			" }\n"+
    			"}\n"
    		},
        "----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	}\n" +
		"	^^\n" +
		"A switch labeled block in a switch expression must yield a value or throw an an exception\n" +
        "----------\n");
}
    public void testBug563023_003() {
        this.runNegativeTest(
    		new String[] {
    			"X.java",
    			"public class X { \n"+
    			" static public int foo(int a, int b){\n"+
    			"   int t = switch (a) {\n"+
    			"     default -> {\n"+
    			"       switch (b) {\n"+
    			"            case 0 -> {\n"+
    			"              yield 0;\n"+
    			"            }\n"+
    			"       }      \n"+
    			"     }\n"+
    			"   };\n"+
    			"   return t;\n"+
    			" }\n"+
    			" public static void main(String[] args) {\n"+
    			"   System.out.println(X.foo(0, 0));\n"+
    			" }\n"+
    			"}\n"
    		},
        "----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	}\n" +
		"	^^\n" +
		"A switch labeled block in a switch expression must yield a value or throw an an exception\n" +
        "----------\n");
}
    public void testBug563023_004() {
        this.runNegativeTest(
    		new String[] {
    			"X.java",
    			"public class X { \n"+
    			" static public int foo(int a, int b){\n"+
    			"   int t = switch (a) {\n"+
    			"     default -> {\n"+
    			"       switch (b) {\n"+
    			"            case 0 -> {\n"+
    			"              break;\n"+
    			"            }\n"+
    			"            default -> yield 0;\n"+
    			"       }      \n"+
    			"     }\n"+
    			"   };\n"+
    			"   return t;\n"+
    			" }\n"+
    			" public static void main(String[] args) {\n"+
    			"   System.out.println(X.foo(0, 0));\n"+
    			" }\n"+
    			"}\n"
    		},
        "----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	default -> yield 0;\n" +
		"	                 ^\n" +
		"Syntax error on token \"0\", delete this token\n" +
        "----------\n");
}
    public void testBug563023_005() {
        this.runNegativeTest(
    		new String[] {
    			"X.java",
    			"public class X { \n"+
    			" static public int foo(int a, int b){\n"+
    			"   int t = switch (a) {\n"+
    			"     default -> {\n"+
    			"       switch (b) {\n"+
    			"            case 0 -> {\n"+
    			"              break;\n"+
    			"            }\n"+
    			"            default ->{ yield 0;}\n"+
    			"       }      \n"+
    			"     }\n"+
    			"   };\n"+
    			"   return t;\n"+
    			" }\n"+
    			" public static void main(String[] args) {\n"+
    			"   System.out.println(X.foo(0, 0));\n"+
    			" }\n"+
    			"}\n"
    		},
        "----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	}\n" +
		"	^^\n" +
		"A switch labeled block in a switch expression must yield a value or throw an an exception\n" +
        "----------\n");
}
	public void testBug563023_006() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X { \n"+
				" static public int foo(MyEnum a, MyEnum b){\n"+
				"   int t = switch (a) {\n"+
				"     default -> {\n"+
				"       switch (b) {\n"+
				"       case ONE -> { \n"+
				"              yield 0;\n"+
				"            }\n"+
				"       default -> {yield 1;}\n"+
				"       }      \n"+
				"     }\n"+
				"   };\n"+
				"   return t;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo(MyEnum.ONE, MyEnum.TWO));\n"+
				" }\n"+
				"} \n"+
				"enum MyEnum {\n"+
				" ONE,\n"+
				" TWO\n"+
				"}\n"
			},
			"1");
	}
    public void testBug563023_007() {
        this.runNegativeTest(
    		new String[] {
    			"X.java",
    			"public class X { \n"+
    			" static public int foo(MyEnum a, MyEnum b){\n"+
    			"   int t = switch (a) {\n"+
    			"     default -> {\n"+
    			"       switch (b) {\n"+
    			"       case ONE -> { \n"+
    			"              yield 0;\n"+
    			"            }\n"+
    			"       }      \n"+
    			"     }\n"+
    			"   };\n"+
    			"   return t;\n"+
    			" }\n"+
    			" public static void main(String[] args) {\n"+
    			"   System.out.println(X.foo(MyEnum.ONE, MyEnum.TWO));\n"+
    			" }\n"+
    			"} \n"+
    			"enum MyEnum {\n"+
    			" ONE,\n"+
    			" TWO\n"+
    			"}\n"
    		},
        "----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	switch (b) {\n" +
		"	        ^\n" +
		"The enum constant TWO needs a corresponding case label in this enum switch on MyEnum\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	}\n" +
		"	^^\n" +
		"A switch labeled block in a switch expression must yield a value or throw an an exception\n" +
        "----------\n");
}
	public void testBug563147_001() {
		runConformTest(
			new String[] {
				"X.java",
				"interface I {\n"+
				" public int apply();\n"+
				"}\n"+
				"public class X { \n"+
				" static public int foo(int a){\n"+
				"   int t = switch (a) {\n"+
				"     default -> {\n"+
				"       I lambda = () -> { return 0;};\n"+
				"       yield lambda.apply();\n"+
				"     }\n"+
				"   };\n"+
				"   return t;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo(1));\n"+
				" }\n"+
				"} \n"
			},
			"0");
	}
	public void testBug563147_002() {
		runConformTest(
			new String[] {
				"X.java",
				"interface FI {\n"+
				"  public int foo();\n"+
				"}\n"+
				"public class X {\n"+
				"  public int field = 0;\n"+
				"  public int test() {\n"+
				"   var v = switch (field) {\n"+
				"     case 0 -> {\n"+
				"       yield ((FI  ) () -> {\n"+
				"         int i = 0;\n"+
				"         while (true) {\n"+
				"           i++;\n"+
				"           if (i == 7) {\n"+
				"             break;\n"+
				"           }\n"+
				"         }\n"+
				"         return i;\n"+
				"       });   \n"+
				"     }\n"+
				"     default -> {\n"+
				"       yield null;\n"+
				"     }\n"+
				"   }; \n"+
				"   return 0;\n"+
				"  }\n"+
				"  public static void main(String[] args) {\n"+
				" int t = new X().test();\n"+
				" System.out.println(t);\n"+
				"}\n"+
				"}\n"
			},
			"0");
	}
	public void testBug563147_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface FI {\n"+
				"  public int foo();\n"+
				"}\n"+
				"public class X {\n"+
				"  public int field = 0;\n"+
				"  public int test() {\n"+
				"   var v = switch (field) {\n"+
				"     case 0 -> {\n"+
				"       yield ((F  ) () -> {\n"+
				"         int i = 0;\n"+
				"         while (true) {\n"+
				"           i++;\n"+
				"           if (i == 7) {\n"+
				"             break;\n"+
				"           }\n"+
				"         }\n"+
				"         return i;\n"+
				"       });   \n"+
				"     }\n"+
				"     default -> {\n"+
				"       yield null;\n"+
				"     }\n"+
				"   }; \n"+
				"   return 0;\n"+
				"  }\n"+
				"  public static void main(String[] args) {\n"+
				" int t = new X().test();\n"+
				" System.out.println(t);\n"+
				"}\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	yield ((F  ) () -> {\n" +
			"	        ^\n" +
			"F cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	yield ((F  ) () -> {\n" +
			"	             ^^^^^\n" +
			"The target type of this expression must be a functional interface\n" +
			"----------\n");
	}
	public void testBug565156_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  public int test() {\n"+
				"    return switch (0) {\n"+
				"      default -> {\n"+
				"        try {\n"+
				"          yield 0;\n"+
				"        }\n"+
				"        catch (RuntimeException e) {\n"+
				"          throw e;\n"+
				"        }\n"+
				"      }\n"+
				"    };\n"+
				"  }    \n"+
				"  public static void main(String[] args) {\n"+
				"       int i = new X().test();\n"+
				"       System.out.println(i);\n"+
				" }\n"+
				"}\n"
			},
			"0");
	}
	public void testBug565156_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  public int test() {\n"+
				"    return switch (0) {\n"+
				"      default -> {\n"+
				"        try {\n"+
				"          yield 0;\n"+
				"        }\n"+
				"        finally {\n"+
				"          //do nothing\n"+
				"        }\n"+
				"      }\n"+
				"    };\n"+
				"  }    \n"+
				"  public static void main(String[] args) {\n"+
				"       int i = new X().test();\n"+
				"       System.out.println(i);\n"+
				" }\n"+
				"}\n"
			},
			"0");
	}
	public void testBug565156_003() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  public int test() {\n"+
				"    return switch (0) {\n"+
				"      default -> {\n"+
				"        try {\n"+
				"          yield 0;\n"+
				"        }\n"+
				"        finally {\n"+
				"          int i = 20;"+
				"          yield 20;"+
				"        }\n"+
				"      }\n"+
				"    };\n"+
				"  }    \n"+
				"  public static void main(String[] args) {\n"+
				"       int i = new X().test();\n"+
				"       System.out.println(i);\n"+
				" }\n"+
				"}\n"
			},
			"20");
	}
	public void testBug565156_004() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  public int test()  {\n"+
				"    return switch (0) {\n"+
				"      default -> {\n"+
				"        try {\n"+
				"          yield switch (0) {\n"+
				"          default -> {\n"+
				"              try {\n"+
				"                yield 100;\n"+
				"              }\n"+
				"              finally {\n"+
				"                   yield 200;       \n"+
				"               }\n"+
				"            }\n"+
				"          };\n"+
				"        }\n"+
				"        finally {\n"+
				"             yield 20;\n"+
				"         }\n"+
				"      }\n"+
				"    };\n"+
				"  }\n"+
				"  public static void main(String[] args){\n"+
				"       int i = new X().test();\n"+
				"       System.out.println(i);\n"+
				"  }\n"+
				"}"
			},
			"20");
	}
	public void testBug565156_005() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  public int test()  {\n"+
				"    return switch (0) {\n"+
				"      default -> {\n"+
				"        try {\n"+
				"          yield switch (0) {\n"+
				"          default -> {\n"+
				"              try {\n"+
				"                yield 100;\n"+
				"              }\n"+
				"              finally {\n"+
				"                   // do nothing\n"+
				"               }\n"+
				"            }\n"+
				"          };\n"+
				"        }\n"+
				"        finally {\n"+
				"           // do nothing\n"+
				"         }\n"+
				"      }\n"+
				"    };\n"+
				"  }\n"+
				"  public static void main(String[] args){\n"+
				"       int i = new X().test();\n"+
				"       System.out.println(i);\n"+
				"  }\n"+
				"}"
			},
			"100");
	}
	public void testBug565156_006() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    public static void main(String[] args) {\n"+
				"            new X().foo(args);\n"+
				"    }\n"+
				"\n"+
				"  @SuppressWarnings({ \"finally\" })\n"+
				"  public void foo(String[] args) {\n"+
				"     int t = switch (0) {\n"+
				"     default -> {\n"+
				"        try {\n"+
				"            if (args == null)\n"+
				"            yield 1;\n"+
				"            else if (args.length ==2)\n"+
				"                    yield 2; \n"+
				"            else if (args.length == 4)\n"+
				"                    yield 4;\n"+
				"            else yield 5; \n"+
				"        } finally {\n"+
				"                yield 3; \n"+
				"        }\n"+
				"     }\n"+
				"     }; \n"+
				"     t = switch (100) {\n"+
				"     default -> {\n"+
				"             try {\n"+
				"                     yield 10;\n"+
				"             } finally {\n"+
				"             }\n"+
				"     }  \n"+
				"     };      \n"+
				"     System.out.println(t);\n"+
				"  }\n"+
				"}"
			},
			"10");
	}
	public void testBug565156_007() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    public static void main(String[] args) {\n"+
				"            new X().foo(args);\n"+
				"    }\n"+
				"\n"+
				"  @SuppressWarnings({ \"finally\" })\n"+
				"  public void foo(String[] args) {\n"+
				"     int t = switch (0) {\n"+
				"     case 101 -> {yield 101;}\n"+
				"     default -> {\n"+
				"        try {\n"+
				"            if (args == null)\n"+
				"            yield 1;\n"+
				"            else if (args.length ==2)\n"+
				"                    yield 2; \n"+
				"            else if (args.length == 4)\n"+
				"                    yield 4;\n"+
				"            else yield 5; \n"+
				"        } finally {\n"+
				"                yield 3; \n"+
				"        }\n"+
				"     }\n"+
				"     }; \n"+
				"     t = switch (100) {\n"+
				"     default -> {\n"+
				"             try {\n"+
				"                     yield 10;\n"+
				"             } finally {\n"+
				"             }\n"+
				"     }  \n"+
				"     };      \n"+
				"     System.out.println(t);\n"+
				"  }\n"+
				"}"
			},
			"10");
	}
	public void testBug547193_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  public static void main(String[] args) {\n"+
				"    System.out.println(switch (0) {default -> {\n"+
				"      try {\n"+
				"        yield 1;\n"+
				"      } catch (Exception ex) {\n"+
				"        yield 2;\n"+
				"      }\n"+
				"    }});\n"+
				"  }\n"+
				"}"
			},
			"1");
	}
	public void testBug565844_01() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public final static int j = 5;\n" +
					"    public static void main(String argv[]) {\n" +
					"    	boolean b = \n" +
					"    			switch (j) {\n" +
					"    				case j != 1 ? 2 : 3 ->  true;\n" +
					"    				default -> false;\n" +
					"    			}; \n" +
					"    	System.out.println(b);\n" +
					"    }\n"+
					"}"
				},
				"false");
	}
	public void testBug565844_02() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public final static int j = 2;\n" +
					"    public static void main(String argv[]) {\n" +
					"    	boolean b = \n" +
					"    			switch (j) {\n" +
					"    				case j != 1 ? 2 : (j == 2 ? 4 : 5) ->  true;\n" +
					"    				default -> false;\n" +
					"    			}; \n" +
					"    	System.out.println(b);\n" +
					"    }\n"+
					"}"
				},
				"true");
	}
	public void testBug565844_03() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public final static int j = 5;\n" +
					"    public static void main(String argv[]) {\n" +
					"    	boolean b = \n" +
					"    			switch (j) {\n" +
					"    				case j != 1 ? 2 : 3 ->  {\n" +
					"    						yield true;\n" +
					"    					}\n" +
					"    				default -> { yield false;}\n" +
					"    			}; \n" +
					"    	System.out.println(b);\n" +
					"    }\n"+
					"}"
				},
				"false");
	}
	public void testBug565844_04() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public final static int j = 5;\n" +
					"    public static void main(String argv[]) {\n" +
					"    	boolean b = \n" +
					"    			switch (j) {\n" +
					"    				case j != 1 ? 2 : 3 :  {\n" +
					"    						yield true;\n" +
					"    					}\n" +
					"    				default : { yield false;}\n" +
					"    			}; \n" +
					"    	System.out.println(b);\n" +
					"    }\n"+
					"}"
				},
				"false");
	}
	public void testBug565844_05() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public static int j = 5;\n" +
					"    public static void main(String argv[]) {\n" +
					"    	boolean b = \n" +
					"    			switch (j) {\n" +
					"    				case j != 1 ? 2 : 3 ->  {\n" +
					"    						yield true;\n" +
					"    					}\n" +
					"    				default -> { yield false;}\n" +
					"    			}; \n" +
					"    	System.out.println(b);\n" +
					"    }\n"+
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case j != 1 ? 2 : 3 ->  {\n" +
				"	     ^^^^^^^^^^^^^^\n" +
				"case expressions must be constant expressions\n" +
				"----------\n");
	}
	public void testBug565844_06() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public final static int j = 5;\n" +
					"    public static void main(String argv[]) {\n" +
					"    	boolean b = \n" +
					"    			switch (j) {\n" +
					"    				case j != 1 ? ( j != 1 ? 2: 3 ) : 3 -> false;\n" +
					"    				default -> false;\n" +
					"    			}; \n" +
					"    	System.out.println(b);\n" +
					"    }\n"+
					"}"
				},
				"false");
	}
	public void testBug565844_07() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"\n"+
					"       void foo() {\n"+
					"               Object value2 = switch(1) {\n"+
					"                       case AAABBB -> 1;\n"+
					"                               (I)()->();\n"+
					"                       default -> 0;\n"+
					"               };\n"+
					"       }\n"+
					"}\n"+
					"interface I {\n"+
					"       void apply();\n"+
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case AAABBB -> 1;\n" +
				"	                ^\n" +
				"Syntax error on token \";\", [ expected\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	(I)()->();\n" +
				"	  ^^^^^\n" +
				"Syntax error on token(s), misplaced construct(s)\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	(I)()->();\n" +
				"	        ^\n" +
				"Syntax error, insert \")\" to complete Expression\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	(I)()->();\n" +
				"	        ^\n" +
				"Syntax error, insert \"]\" to complete ArrayAccess\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 6)\n" +
				"	(I)()->();\n" +
				"	        ^\n" +
				"Syntax error, insert \":\" to complete SwitchLabel\n" +
				"----------\n");
	}
	public void _testBug565844SwitchConst_07() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public final static int j = 5;\n" +
					"    public static void main(String argv[]) {\n" +
					"    	boolean b = \n" +
					"    			switch (j) {\n" +
					"    				case switch(1) {default -> 2;} -> false;\n" +
					"    				default -> false;\n" +
					"    			}; \n" +
					"    	System.out.println(b);\n" +
					"    }\n"+
					"}"
				},
				"false");
	}
	public void _testBug565844SwitchConst_08() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public final static int j = 5;\n" +
					"    public static void main(String argv[]) {\n" +
					"    	boolean b = \n" +
					"    			switch (j) {\n" +
					"    				case switch(1) {case 1 -> 2; default -> 0;} -> false;\n" +
					"    				default -> false;\n" +
					"    			}; \n" +
					"    	System.out.println(b);\n" +
					"    }\n"+
					"}"
				},
				"false");
	}
	public void _testBug565844SwitchConst_09() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public final static int j = 5;\n" +
					"    public static void main(String argv[]) {\n" +
					"    	boolean b = \n" +
					"    			switch (j) {\n" +
					"    				case switch(1) {default -> 2;}, switch(2) {default -> 3;}  -> false;\n" +
					"    				default -> false;\n" +
					"    			}; \n" +
					"    	System.out.println(b);\n" +
					"    }\n"+
					"}"
				},
				"false");
	}
	public void _testBug565844SwitchConst_10() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public final static int j = 5;\n" +
					"    public static void main(String argv[]) {\n" +
					"    	boolean b = \n" +
					"    			switch (j) {\n" +
					"    				case switch(1) {case 1 -> 2; default -> 0;}," +
					" 							switch(2) {case 1 -> 3; default -> 4;}  -> false;\n" +
					"    				default -> false;\n" +
					"    			}; \n" +
					"    	System.out.println(b);\n" +
					"    }\n"+
					"}"
				},
				"false");
	}
	public void testBug566125_01() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X  {\n" +
						"	public static void main(String[] args) {\n" +
						"		new X().bar(0);\n" +
						"	}\n" +
						"    @SuppressWarnings(\"deprecation\")\n" +
						"    public void bar(int i) {\n" +
						"		boolean b = foo( switch(i+1) {\n" +
						"	    	case 0 -> new Short((short)0);\n" +
						"	    	case 2 -> new Double(2.0d);\n" +
						"	    	default -> new Integer((short)6);\n" +
						"    	});\n" +
						"    	System.out.println(b);\n" +
						"    }\n" +
						"    boolean foo(short data){ return false; }\n" +
						"    boolean foo(byte data){ return false; }\n" +
						"    boolean foo(int data){ return false; }\n" +
						"    boolean foo(float data){ return false; }\n" +
						"    boolean foo(long data){ return false; }\n" +
						"    boolean foo(double data){ return true; }\n" +
						"}"
				},
				"true");

	}
	// Same as above, but with explicit yield
	public void testBug566125_02() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X  {\n" +
						"	public static void main(String[] args) {\n" +
						"		new X().bar(0);\n" +
						"	}\n" +
						"    @SuppressWarnings(\"deprecation\")\n" +
						"    public void bar(int i) {\n" +
						"		boolean b = foo( switch(i+1) {\n" +
						"	    	case 0 : yield new Short((short)0);\n" +
						"	    	case 2 : yield new Double(2.0d);\n" +
						"	    	default : yield new Integer((short)6);\n" +
						"    	});\n" +
						"    	System.out.println(b);\n" +
						"    }\n" +
						"    boolean foo(short data){ return false; }\n" +
						"    boolean foo(byte data){ return false; }\n" +
						"    boolean foo(int data){ return false; }\n" +
						"    boolean foo(float data){ return false; }\n" +
						"    boolean foo(long data){ return false; }\n" +
						"    boolean foo(double data){ return true; }\n" +
						"}"
				},
				"true");

	}
	public void testBug566125_03() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X  {\n" +
						"	public static void main(String[] args) {\n" +
						"		new X().bar(0);\n" +
						"	}\n" +
						"    @SuppressWarnings(\"deprecation\")\n" +
						"    public void bar(int i) {\n" +
						"		boolean b = foo( switch(i+1) {\n" +
						"	    	case 0 -> new Short((short)0);\n" +
						"	    	case 2 -> 2.0d;\n" +
						"	    	default -> new Integer((short)6);\n" +
						"    	});\n" +
						"    	System.out.println(b);\n" +
						"    }\n" +
						"    boolean foo(short data){ return false; }\n" +
						"    boolean foo(byte data){ return false; }\n" +
						"    boolean foo(int data){ return false; }\n" +
						"    boolean foo(float data){ return false; }\n" +
						"    boolean foo(long data){ return false; }\n" +
						"    boolean foo(double data){ return true; }\n" +
						"}"
				},
				"true");

	}
	// Long -> float is accepted
	public void testBug566125_04() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X  {\n" +
						"	public static void main(String[] args) {\n" +
						"		new X().bar(0);\n" +
						"	}\n" +
						"    @SuppressWarnings(\"deprecation\")\n" +
						"    public void bar(int i) {\n" +
						"		boolean b = foo( switch(i+1) {\n" +
						"	    	case 0 -> new Integer((short)0);\n" +
						"	    	default -> 2l;\n" +
						"    	});\n" +
						"    	System.out.println(b);\n" +
						"    }\n" +
						"	boolean foo(int data){ return false; }\n" +
						"    boolean foo(long data){ return true; }\n" +
						"}"
				},
				"true");

	}
	public void testBug566125_05() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X  {\n" +
						"	public static void main(String[] args) {\n" +
						"		new X().bar(0);\n" +
						"	}\n" +
						"    @SuppressWarnings(\"deprecation\")\n" +
						"    public void bar(int i) {\n" +
						"		boolean b = foo(\n" +
						"    				switch(i%2)  {\n" +
						"    					case 1 -> switch(i) {\n" +
						"    								case 1 -> new Byte((byte)1);\n" +
						"    								case 3 -> new Float(3);\n" +
						"    								case 5 -> new Long(5);\n" +
						"    								default -> new Short((short)6);\n" +
						"    							}; \n" +
						"    					default -> switch(i) {\n" +
						"									case 0 -> new Integer((byte)2);\n" +
						"									case 2 -> new Double(4);\n" +
						"									case 4 -> new Long(6);\n" +
						"									default -> new Short((short)8);\n" +
						"    							};\n" +
						"    				}\n" +
						"    			);\n" +
						"    	System.out.println(b);\n" +
						"    }\n" +
						"    boolean foo(short data){ return false; }\n" +
						"    boolean foo(byte data){ return false; }\n" +
						"    boolean foo(int data){ return false; }\n" +
						"    boolean foo(float data){ return false; }\n" +
						"    boolean foo(long data){ return false; }\n" +
						"    boolean foo(double data){ return true; }\n" +
						"}"
				},
				"true"
				);

	}
	public void testBug566125_06() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X  {\n" +
						"	public static void main(String[] args) {\n" +
						"		new X().bar(0);\n" +
						"	}\n" +
						"    @SuppressWarnings(\"deprecation\")\n" +
						"    public void bar(int i) {\n" +
						"		boolean b = foo( switch(i+1) {\n" +
						"	    	case 0 -> Short.valueOf((short)0);\n" +
						"	    	default -> Double.valueOf(2.0d);\n" +
						"    	});\n" +
						"    	System.out.println(b);\n" +
						"    }\n" +
						"    boolean foo(short data){ return false; }\n" +
						"    boolean foo(byte data){ return false; }\n" +
						"    boolean foo(int data){ return false; }\n" +
						"    boolean foo(float data){ return false; }\n" +
						"    boolean foo(long data){ return false; }\n" +
						"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	boolean b = foo( switch(i+1) {\n" +
				"	            ^^^\n" +
				"The method foo(short) in the type X is not applicable for the arguments (switch ((i + 1)) { ... })\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 9)\n" +
				"	default -> Double.valueOf(2.0d);\n" +
				"	           ^^^^^^^^^^^^^^^^^^^^\n" +
				"Type mismatch: cannot convert from Double to short\n" +
				"----------\n"
				);
	}
	public void testBug566125_07() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X  {\n" +
						"	public static void main(String[] args) {\n" +
						"		new X().bar(0);\n" +
						"	}\n" +
						"    @SuppressWarnings(\"deprecation\")\n" +
						"    public void bar(int i) {\n" +
						"		boolean b = foo( switch(i+1) {\n" +
						"	    	case 0 -> Short.valueOf((short)0);\n" +
						"	    	default -> 2.0d;\n" +
						"    	});\n" +
						"    	System.out.println(b);\n" +
						"    }\n" +
						"    boolean foo(short data){ return false; }\n" +
						"    boolean foo(byte data){ return false; }\n" +
						"    boolean foo(int data){ return false; }\n" +
						"    boolean foo(float data){ return false; }\n" +
						"    boolean foo(long data){ return false; }\n" +
						"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	boolean b = foo( switch(i+1) {\n" +
				"	            ^^^\n" +
				"The method foo(short) in the type X is not applicable for the arguments (switch ((i + 1)) { ... })\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 9)\n" +
				"	default -> 2.0d;\n" +
				"	           ^^^^\n" +
				"Type mismatch: cannot convert from double to short\n" +
				"----------\n"
				);
	}
	// Same as 07() but with explicit yield
	public void testBug566125_08() {
		runNegativeTest(
				new String[] {
						"X.java",
						"public class X  {\n" +
						"	public static void main(String[] args) {\n" +
						"		new X().bar(0);\n" +
						"	}\n" +
						"    @SuppressWarnings(\"deprecation\")\n" +
						"    public void bar(int i) {\n" +
						"		boolean b = foo( switch(i+1) {\n" +
						"	    	case 0 : yield Short.valueOf((short)0);\n" +
						"	    	default : yield 2.0d;\n" +
						"    	});\n" +
						"    	System.out.println(b);\n" +
						"    }\n" +
						"    boolean foo(short data){ return false; }\n" +
						"    boolean foo(byte data){ return false; }\n" +
						"    boolean foo(int data){ return false; }\n" +
						"    boolean foo(float data){ return false; }\n" +
						"    boolean foo(long data){ return false; }\n" +
						"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	boolean b = foo( switch(i+1) {\n" +
				"	            ^^^\n" +
				"The method foo(short) in the type X is not applicable for the arguments (switch ((i + 1)) { ... })\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 9)\n" +
				"	default : yield 2.0d;\n" +
				"	                ^^^^\n" +
				"Type mismatch: cannot convert from double to short\n" +
				"----------\n"
				);
	}
	public void testBug567112_001() {
		runNegativeTest(
				new String[] {
						"X.java",
						"import java.util.ArrayList;\n"+
						"\n"+
						"public class X {\n"+
						"    public void foo() {\n"+
						"        new ArrayList<>().stream().filter(p -> p != null)\n"+
						"        switch (\"\") {\n"+
						"        case \"\":\n"+
						"        }\n"+
						"    }\n"+
						"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	new ArrayList<>().stream().filter(p -> p != null)\n" +
				"	                                            ^^^^^\n" +
				"Syntax error on tokens, delete these tokens\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	}\n" +
				"	^\n" +
				"Syntax error, insert \")\" to complete Expression\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 8)\n" +
				"	}\n" +
				"	^\n" +
				"Syntax error, insert \";\" to complete BlockStatements\n" +
				"----------\n"
				);
	}
	public void testBug571833_01() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private static int foo(int a) {\n"+
				"   int b = (int) switch (a) {\n"+
				"     case 1 -> 1.0;\n"+
				"     default -> 0;\n"+
				"   };\n"+
				"   return b;\n"+
				" }\n"+
				"\n"+
				" public static void main(String[] args) {\n"+
				"   int b = foo(2);\n"+
				"   System.out.println(b);\n"+
				" }\n"+
				"}"
			},
			"0"
		);

	}
	public void testBug572382() {
		runConformTest(
				new String[] {
						"X.java",
						"import java.lang.invoke.MethodHandle;\n"+
						"\n"+
						"public class X {\n"+
						"\n"+
						"	Object triggerBug(MethodHandle method) throws Throwable {\n"+
						"		return switch (0) {\n"+
						"		case 0 -> method.invoke(\"name\");\n"+
						"		default -> null;\n"+
						"		};\n"+
						"	}\n"+
						"}\n"
				},
				(String)null
				);

	}
	public void testBug576026() {
		this.runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"
						+ "	enum E { A }\n"
						+ "	static class C {\n"
						+ "		E e = E.A;\n"
						+ "	}\n"
						+ "	public static void main(String[] args) {\n"
						+ "		C c = new C();\n"
						+ "		switch (c.e) {\n"
						+ "		case A -> {\n"
						+ "			System.out.println(\"Success\");\n"
						+ "		}\n"
						+ "		default -> System.out.println(\"Wrong\");\n"
						+ "		}\n"
						+ "	}\n"
						+ "}",
				},
				"Success");
	}
	public void testBug576861_001() {
		this.runConformTest(
				new String[] {
				"X.java",
				"import java.util.Comparator;\n"+
				"\n"+
				"public class X {\n"+
				" public static void foo(Comparator<? super Long> comparator) {}\n"+
				"\n"+
				" public static void main(String[] args) {\n"+
				"   int someSwitchCondition = 10;\n"+
				"   X.foo(switch (someSwitchCondition) {\n"+
				"   case 10 -> Comparator.comparingLong(Long::longValue);\n"+
				"   default -> throw new IllegalArgumentException(\"Unsupported\");\n"+
				" });\n"+
				"   System.out.println(\"hello\");\n"+
				" }\n"+
				"}"
				},
				"hello");
	}
	public void testBug577220_001() {
		this.runNegativeTest(
			new String[] {
				"module-info.java",
				"public class X {\n"+
				" void main(Integer i) {\n"+
				"   Object a = switch (i) {\n"+
				"   default -> {\n"+
				"     yield i.toString();\n"+
				"   }\n"+
				"   }\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in module-info.java (at line 1)\n" +
			"	public class X {\n" +
			"	             ^\n" +
			"The public type X must be defined in its own file\n" +
			"----------\n" +
			"2. ERROR in module-info.java (at line 5)\n" +
			"	yield i.toString();\n" +
			"	       ^\n" +
			"Syntax error on token \".\", ; expected\n" +
			"----------\n" +
			"3. ERROR in module-info.java (at line 7)\n" +
			"	}\n" +
			"	^\n" +
			"Syntax error, insert \";\" to complete BlockStatements\n" +
			"----------\n");
	}
	public void testIssue966_001() {
		this.runConformTest(
				new String[] {
				"X.java",
				"public class X {\n"+
				"    private static final String SOME_CONSTANT = \"PASS\";\n"+
				"    public static void main(String[] args) {\n"+
				"        switch (\"\") {\n"+
				"            case (SOME_CONSTANT) -> {}\n"+
				"            default -> {}\n"+
				"        }\n"+
				"        System.out.println(SOME_CONSTANT);\n"+
				"    }\n"+
				"}"
				},
				"PASS");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/53
	// continue without label is incorrectly handled in a switch expression
	public void testGHIssue53() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	interface I {\n" +
				"		void foo();\n" +
				"	}\n" +
				"	public static String string = \"a\";\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		loop: for (;;) {\n" +
				"			System.out.println(\"In loop before switch\");\n" +
				"			\n" +
				"			int result = 123 + switch (string) {\n" +
				"			case \"a\" -> {\n" +
				"				if (string == null)\n" +
				"					continue; // incorrectly compiles in JDT\n" +
				"				else \n" +
				"					continue loop; // correctly flagged as error (\"Continue out of switch\n" +
				"				// expressions not permitted\")\n" +
				"				// javac (correctly) outputs \"error: attempt to continue out of a switch\n" +
				"				// expression\" for both continue statements\n" +
				"				yield 789;\n" +
				"			}\n" +
				"			default -> 456;\n" +
				"			};\n" +
				"			System.out.println(\"After switch. result: \" + result);\n" +
				"		}\n" +
				"	}\n" +
				"}\n"

				},
				"----------\n" +
				"1. WARNING in X.java (at line 8)\n" +
				"	loop: for (;;) {\n" +
				"	^^^^\n" +
				"The label loop is never explicitly referenced\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 14)\n" +
				"	continue; // incorrectly compiles in JDT\n" +
				"	^^^^^^^^^\n" +
				"Continue out of switch expressions not permitted\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 16)\n" +
				"	continue loop; // correctly flagged as error (\"Continue out of switch\n" +
				"	^^^^^^^^^^^^^^\n" +
				"Continue out of switch expressions not permitted\n" +
				"----------\n");
	}

	public void testGH520() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void test(int i) {\n" +
				"		foo(switch (i) {\n" +
				"			case 0 -> m.call();\n" +
				"			default -> null;\n" +
				"		});\n" +
				"	}\n" +
				"	<T> void foo(T t) { }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	foo(switch (i) {\n" +
			"	^^^\n" +
			"The method foo(T) in the type X is not applicable for the arguments (switch (i) { ... })\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	case 0 -> m.call();\n" +
			"	          ^\n" +
			"m cannot be resolved\n" +
			"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1394
	// switch statement with yield and try/catch produces EmptyStackException
	public void testGHI1394() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_UseStringConcatFactory, CompilerOptions.ENABLED);
		this.runConformTest(
				new String[] {
				"X.java",
				"public class X {\n"
				+ "	protected static int switchWithYield() {\n"
				+ "		String myStringNumber = \"1\";\n"
				+ "		return switch (myStringNumber) {\n"
				+ "		case \"1\" -> {\n"
				+ "			try {\n"
				+ "				yield Integer.parseInt(myStringNumber);\n"
				+ "			} catch (NumberFormatException e) {\n"
				+ "				throw new RuntimeException(\"Failed parsing number\", e); //$NON-NLS-1$\n"
				+ "			}\n"
				+ "		}\n"
				+ "		default -> throw new IllegalArgumentException(\"Unexpected value: \" + myStringNumber);\n"
				+ "		};\n"
				+ "	}\n"
				+ "	public static void main(String[] args) {\n"
				+ "		System.out.println(switchWithYield());\n"
				+ "	}\n"
				+ "} "
				},
				"1",
				options);
	}
	public void testGHI1394_2() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_UseStringConcatFactory, CompilerOptions.ENABLED);
		this.runConformTest(
				new String[] {
				"X.java",
				"public class X {\n"
				+ "	static String myStringNumber = \"1\";\n"
				+ "	protected static int switchWithYield() {\n"
				+ "		return switch (myStringNumber) {\n"
				+ "		case \"10\" -> {\n"
				+ "			try {\n"
				+ "				yield Integer.parseInt(myStringNumber);\n"
				+ "			} catch (NumberFormatException e) {\n"
				+ "				throw new RuntimeException(\"Failed parsing number\", e); //$NON-NLS-1$\n"
				+ "			}\n"
				+ "		}\n"
				+ "		default -> throw new IllegalArgumentException(\"Unexpected value: \" + myStringNumber);\n"
				+ "		};\n"
				+ "	}\n"
				+ "	public static void main(String[] args) {\n"
				+ "     try {\n"
				+ "		    System.out.println(switchWithYield());\n"
				+ "     } catch(IllegalArgumentException iae) {\n"
				+ "         if (!iae.getMessage().equals(\"Unexpected value: \" + myStringNumber))\n"
				+ "             throw iae;\n"
				+ "     }\n"
				+ "     System.out.println(\"Done\");\n"
				+ "	}\n"
				+ "} "
				},
				"Done",
				options);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1394
	// switch statement with yield and try/catch produces EmptyStackException
	public void testGHI1394_min() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_UseStringConcatFactory, CompilerOptions.ENABLED);
		this.runConformTest(
				new String[] {
				"X.java",
				"public class X {\n" +
				"	static int foo() {\n" +
				"		int x = 1;\n" +
				"		return switch (x) {\n" +
				"		case 1 -> {\n" +
				"			try {\n" +
				"				yield x;\n" +
				"			} finally  {\n" +
				"				\n" +
				"			}\n" +
				"		}\n" +
				"		default -> throw new RuntimeException(\"\" + x + \" \".toLowerCase());\n" +
				"		};\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo());\n" +
				"	}\n" +
				"}\n"
				},
				"1",
				options);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1727
	// Internal compiler error: java.util.EmptyStackException
	public void testGHI1727() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"class X {\n" +
				"	private Object foo(Object value) {\n" +
				"		return switch (value) {\n" +
				"			case String string -> {\n" +
				"				try {\n" +
				"					yield string;\n" +
				"				} catch (IllegalArgumentException exception) {\n" +
				"					yield string;\n" +
				"				}\n" +
				"			}\n" +
				"			default -> throw new IllegalArgumentException(\"Argument of type \" + value.getClass());\n" +
				"		};\n" +
				"	}\n" +
				"}\n"
				},
				"");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1686
	// Switch statement with yield in synchronized and try-catch blocks results in ArrayIndexOutOfBoundsException
	public void testGHI1686() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					public String demo(String input) {
						return switch (input) {
							case "red" -> {
								synchronized (this) {
									yield "apple";
								}
							}
							default -> {
								try {
									yield "banana";
								}
								catch (Exception ex) {
									throw new IllegalStateException(ex);
							    }
						    }
					    };
				    }
				}
				"""
				},
				"");
		}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1686
	// Switch statement with yield in synchronized and try-catch blocks results in ArrayIndexOutOfBoundsException
	public void testGHI1686_works() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					public String demo(String input) {
						return switch (input) {
							case "red" -> {
								synchronized (this) {
									yield "apple";
								}
							}
							default -> {
								yield "banana";
						    }
					    };
				    }
				}
				"""
				},
				"");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1767
	// NPE in switch with Enum
	public void testGHI1767() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static enum TestEnum {
						E1,
						E2,
						E3
					}

					public static void main(String[] theArgs) {
						System.out.println("Test case 1");
						new X().test1SwitchWithNull(TestEnum.E1);
						new X().test1SwitchWithNull(TestEnum.E2);
						new X().test1SwitchWithNull((TestEnum) null);

						System.out.println("Test case 2");
						new X().test2SwitchWithNull(TestEnum.E1);
						new X().test2SwitchWithNull(TestEnum.E2);
						new X().test2SwitchWithNull((TestEnum) null);

						System.out.println("Test case 3");
						new X().test3SwitchWithNull(TestEnum.E1);
						new X().test3SwitchWithNull(TestEnum.E2);
						new X().test3SwitchWithNull((TestEnum) null);
					}

					private void test1SwitchWithNull(TestEnum theEnum) {
						switch (theEnum) {
							case TestEnum e when e == TestEnum.E1 -> System.out.println(e);
							case null -> System.out.println("Enum: null");
							default -> System.out.println("Enum: default");
						}
					}

					private void test2SwitchWithNull(TestEnum theEnum) {
						switch (theEnum) {
							case TestEnum e -> System.out.println(e);
							case null -> System.out.println("Enum: null");
						}
					}

					private void test3SwitchWithNull(TestEnum theEnum) {
						switch (theEnum) {
							case TestEnum.E1 -> System.out.println(theEnum);
							case null -> System.out.println("Enum: null");
							default -> System.out.println("Enum: default -> " + theEnum);
						}
					}
				}
				"""
				},
				"Test case 1\n" +
				"E1\n" +
				"Enum: default\n" +
				"Enum: null\n" +
				"Test case 2\n" +
				"E1\n" +
				"E2\n" +
				"Enum: null\n" +
				"Test case 3\n" +
				"E1\n" +
				"Enum: default -> E2\n" +
				"Enum: null");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1767
	// NPE in switch with Enum
	public void testGHI1767_minimal() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					enum E {
						E
					}

					public static void main(String[] args) {
						E e = null;
						switch(e) {
						case E.E -> {}
						case null -> {}
						}
					}
				}
				"""
				},
				"");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1820
	// [switch] Switch expression fails with instanceof + ternary operator combo
	public void testGHI1820() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String method(Object element, int columnIndex) {
						return element instanceof String data ?
							switch (columnIndex) {
								case 0 -> data;
								case 1 -> data.toUpperCase();
								default -> "Done";
							} : "";
					}
					public static void main(String[] args) {
						System.out.println(method("Blah", 0));
						System.out.println(method("Blah", 1));
						System.out.println(method("Blah", 10));
					}
				}
				"""
				},
				"Blah\n"
				+ "BLAH\n"
				+ "Done");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1820
	// [switch] Switch expression fails with instanceof + ternary operator combo
	public void testGHI1820_2() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String method(Object element, int columnIndex) {
						return element instanceof String data ? "" :
							switch (columnIndex) {
								case 0 -> data;
								case 1 -> data.toUpperCase();
								default -> "";
							};
					}
					public static void main(String[] args) {
						System.out.println(method("Blah", 1));
					}
				}
				"""
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 5)\n"
				+ "	case 0 -> data;\n"
				+ "	          ^^^^\n"
				+ "data cannot be resolved to a variable\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 6)\n"
				+ "	case 1 -> data.toUpperCase();\n"
				+ "	          ^^^^\n"
				+ "data cannot be resolved\n"
				+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1820
	// [switch] Switch expression fails with instanceof + ternary operator combo
	public void testGHI1820_3() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String method(Object element, int columnIndex) {
						return element instanceof String data ?
							switch (columnIndex) {
								case 0 -> { yield data; }
								case 1 -> data.toUpperCase();
								default -> "";
							} : "";
					}
					public static void main(String[] args) {
						System.out.println(method("Blah", 1));
					}
				}
				"""
				},
				"BLAH");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1820
	// [switch] Switch expression fails with instanceof + ternary operator combo
	public void testGHI1820_4() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String method(Object element, int columnIndex) {
						return !(element instanceof String data) ? "" :
							switch (columnIndex) {
								case 0 -> data;
								case 1 -> data.toUpperCase();
								default -> "Done";
							};
					}
					public static void main(String[] args) {
						System.out.println(method("Blah", 0));
						System.out.println(method("Blah", 1));
						System.out.println(method("Blah", 10));
					}
				}
				"""
				},
				"Blah\n"
				+ "BLAH\n"
				+ "Done");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1820
	// [switch] Switch expression fails with instanceof + ternary operator combo
	public void testGHI1820_5() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String method(Object element, int columnIndex) {
						if (element instanceof String string) {
							return element instanceof String data ?
								switch (columnIndex) {
									case 0 -> data;
									case 1 -> string.toUpperCase();
									default -> "Done";
								} : "";
						}
						return null;
					}
					public static void main(String[] args) {
						System.out.println(method("Blah", 0));
						System.out.println(method("Blah", 1));
						System.out.println(method("Blah", 10));
					}
				}
				"""
				},
				"Blah\n"
				+ "BLAH\n"
				+ "Done");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/377
	// [switch-expression] Invalid compiler error with switch expression
	public void testGH377() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  enum TestEnum {
				    A, B;
				  }

				  @SuppressWarnings("unused")
				  private int switcher(final TestEnum e) throws Exception {
				    return switch (e) {
				      case A -> 0;
				      case B -> {
				        try {
				          yield 1;
				        } finally {
				          throwingFn();
				        }
				      }
				    };
				  }

				  private void throwingFn() throws Exception {}

				  public static void main(String [] args) throws Exception {
					  System.out.println("Switcher :" + new X().switcher(TestEnum.A));
					  System.out.println("Switcher :" + new X().switcher(TestEnum.B));
				  }
			    }
				"""
				},
				"Switcher :0\n"
				+ "Switcher :1");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2231
	// [Switch-Expression] Assertion failure compiling array access + switch expressions with try blocks
	public void testIssue2231() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void foo(String... ss) {
						System.out.println("Entry = " + switch(ss) {
															case null -> "None";
															case String [] s when s.length == 0 -> "none";
															default ->  {
																try {
																	yield ss[0];
																} finally {

																}
															}
						});
					}

					public static void main(String[] args) {
						foo("Hello");
					}
				}
				"""
				},
				"Entry = Hello");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2231
	// [Switch-Expression] Assertion failure compiling array access + switch expressions with try blocks
	public void testIssue2231_2() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void foo(String [] ss) {
						System.out.println("Entry = " + switch(ss) {
															case null -> "None";
															case String [] s when s.length == 0 -> "none";
															default ->  {
																try {
																	yield ss[0];
																} finally {

																}
															}
						});
					}

					public static void main(String[] args) {
						foo(new String [] { "Hello" });
					}
				}
				"""
				},
				"Entry = Hello");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					int k;
					void foo() {
						new X() {
							{
								System.out.println ("Switch Expr = " +  switch (X.this.k) {
								default -> {
									try {
										yield throwing();
									} catch (NumberFormatException nfe) {
										yield 10;
									}
									finally {
										System.out.println("Finally");
									}
								}
							});
							}

							private Object throwing() {
								throw new NumberFormatException();
							}
						};
					}

					public static void main(String[] args) {
						new X().foo();
					}
				}
				"""
				},
				"Finally\n"
				+ "Switch Expr = 10");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228_2() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					int k;
					void foo() {
						int fooLocal = 10;
						class Local {
							Local() {
								System.out.println("Switch result = " + switch(X.this.k) {
																			default -> {
																				try {
																					System.out.println("Try");
																					yield 10;
																				} catch (Exception e) {
																					System.out.println("Catch");
																					yield 20;
																				} finally {
																					System.out.println("Finally");
																				}
																			}
																		});
							}
						}
						new Local();
					}
					public static void main(String[] args) {
						new X().foo();
					}
				}
				"""
				},
				"Try\n" +
				"Finally\n"
				+ "Switch result = 10");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228_3() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					int k;

					{
						System.out.println ("Switch Expr = " +  switch (k) {
						default -> {
							try {
								yield throwing();
							} catch (NumberFormatException nfe) {
								yield 10;
							}
							finally {
								System.out.println("Finally");
							}
						}
					});
					}
					private Object throwing() {
						throw new NumberFormatException();
					}
					public static void main(String[] args) {
						new X();
					}
				}
				"""
				},
				"Finally\n"
				+ "Switch Expr = 10");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228_4() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						args = new String [] { "one" , "two"};
						System.out.println(switch (args) {
							case null ->  0;
							default -> switch(args.length) {
											case 0 -> 0;
											case 1 -> "One";
											default -> new X();
										};
							});
					}
					public String toString() {
						return "some X()";
					}
				}
				"""
				},
				"some X()");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228_5() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					int k;
					void foo() {
						String val = "123";
						new X() {
							{
								System.out.println ("Switch Expr = " +  switch (X.this.k) {
								default -> {
									try {
										yield throwing();
									} catch (NumberFormatException nfe) {
										yield val;
									}
									finally {
										System.out.println("Finally");
									}
								}
							});
							}

							private Object throwing() {
								throw new NumberFormatException();
							}
						};
					}

					public static void main(String[] args) {
						new X().foo();
					}
				}
				"""
				},
				"Finally\n"
				+ "Switch Expr = 123");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2228
	// [Switch-expression] Internal inconsistency warning at compile time & verify error at runtime
	public void testIssue2228_6() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					static int k;

					static {
						System.out.println ("Switch Expr = " +  switch (k) {
						default -> {
							try {
								yield throwing();
							} catch (NumberFormatException nfe) {
								yield 10;
							}
							finally {
								System.out.println("Finally");
							}
						}
					});
					}
					private static Object throwing() {
						throw new NumberFormatException();
					}
					public static void main(String[] args) {
						new X();
					}
				}
				"""
				},
				"Finally\n"
				+ "Switch Expr = 10");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2233
	// [Switch-Expression] Assertion failure while compiling enum class that uses switch expression with try block
	public void testIssue2233() {
		if (this.complianceLevel < ClassFileConstants.JDK16)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public enum X {
					PARAMETER, FIELD, METHOD;
					X() {
						System.out.println(switch (this) {
												default -> {
													try {
														yield 10;
													} finally {

													}
												}
											});
					}

				    public static void notmain(String [] args) {
				        X x = PARAMETER;
				        System.out.println(x);
				    }
				}
				"""
				},
				"");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2322
	// [Switch Expression] Internal compiler error: java.util.EmptyStackException at java.base/java.util.Stack.peek
	public void testIssue2322() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String [] args) {
				    int lineCount = 10;
				    long time = 1000;
				    print((int) (lineCount * 10000.0 / time));
				    print((double) (lineCount * 10000.0 / time));
				    System.out.println(switch(lineCount) {
				        default -> {
				    	try {
				    		yield "OK";
				    	} finally {

				    	}
				        }
				    });
				  }
				  static void print(double d) {}
				}
				"""
				},
				"OK");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2335
	// [Switch Expression] Internal compiler error: java.lang.ClassCastException: class org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding cannot be cast to class org.eclipse.jdt.internal.compiler.lookup.ArrayBinding
	public void testIssue2335() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public final class X {

				  public void show() {

				    int size1 = 1;
				    int size2 = 2;
				    int size3 = 3;

				    short[][][] array = new short[size1][size2][size3];

				    for (int i = 0; i < size1; i++) {
				      for (int j = 0; j < size2; j++) {
				        boolean on = false;
				        for (int k = 0; k < size3; k++) {
				          array[i][j][k] = on ? (short) 1 : (short) 0;
				        }
				      }
				    }
				    System.out.println(switch(42) {
				    	default -> {
				    		try {
				    			yield 42;
				    		} finally {

				    		}
				    	}
				    });

				  }

				  public static void main(String[] args) {
				    new X().show();
				  }
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2335
	// [Switch Expression] Internal compiler error: java.lang.ClassCastException: class org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding cannot be cast to class org.eclipse.jdt.internal.compiler.lookup.ArrayBinding
	public void testIssue2335_min() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public final class X {
				  public static void main(String[] args) {
					   short[] array = new short[10];

					    for (int i = 0; i < 10; i++) {
					        boolean on = false;
					          array[i] = on ? (short) 1 : (short) 0;
					    }
					    System.out.println(switch(42) {
					    	default -> {
					    		try {
					    			yield 42;
					    		} finally {

					    		}
					    	}
					    });
				  }
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2335
	// [Switch Expression] Internal compiler error: java.lang.ClassCastException: class org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding cannot be cast to class org.eclipse.jdt.internal.compiler.lookup.ArrayBinding
	public void testIssue2335_other() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						System.out.println(switch (1) {
						default -> {
							try {
								System.out.println(switch (10) { default -> { try { yield 10; } finally {} } });
							} finally {}
							yield 1;
						}
						});
					}
					X() {}
				}
				"""
				},
				"10\n" +
				"1");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2349
	// [Switch Expression] Verify error at runtime with switch expression and exception handling inside lambda expression
	public void testIssue2349() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				interface I {
					int doit();
				}
				public class X {
					public static void main(String[] args) {
						I i = () -> {
							return 10 + switch (10) {
								default -> { try { yield 32; } catch (NullPointerException npe) { yield -10; } }
						};
						};
						System.out.println(i.doit());
					}
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2360
	// [Switch Expression] Internal compiler error: java.lang.NullPointerException: Cannot read field "binding" because "this.methodDeclaration" is null
	public void testIssue2360() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				interface I {
					void foo(int p, int q);
				}
				public class X {
				   int f;
					void foo(int a) {
				       int loc = 10;
						I i = (int p, int q)  -> {
				           I i2 = new I() { public void foo(int f, int p0) {};};
				           System.out.println(10 + switch (10) {
							default -> { try { yield 32; } catch (NullPointerException npe) { yield -10; } }});
							System.out.println(10 + switch (loc) {
							default -> { try { yield 0; } catch (NullPointerException npe) { yield -10; } }});
							System.out.println(10 + switch (p) {
							default -> { try { yield p; } catch (NullPointerException npe) { yield -10; } }});
							System.out.println(10 + switch (q) {
							default -> { try { yield q; } catch (NullPointerException npe) { yield -10; } }});
						};
						i.foo(10,  20);
					}

					public static void main(String[] args) {
						new X().foo(42);
					}
				}
				"""
				},
				"42\n" +
				"10\n" +
				"20\n" +
				"30");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2360
	// [Switch Expression] Internal compiler error: java.lang.NullPointerException: Cannot read field "binding" because "this.methodDeclaration" is null
	public void testIssue2360_2() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				interface I {
					void foo(int p, int q);
				}
				public class X {
				   int f;
					void foo(int a) {
				       int loc;
						I i = (int p, int q)  -> {
				           I i2 = new I() { public void foo(int f, int p0) {};};
				           System.out.println(10 + switch (10) {
							default -> { try { yield 32; } catch (NullPointerException npe) { yield -10; } }});
						};
						i.foo(10,  20);
					}

					public static void main(String[] args) {
						new X().foo(42);
					}
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2363
	// [Switch Expressions] Compiler crashes with Switch expressions mixed with exception handling
	public void testIssue2363() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String argv[]) {
						System.out.println(void.class == Void.TYPE);
						System.out.println(switch(42) {
				    	default -> {
				    		try {
				    			yield 42;
				    		} finally {

				    		}
				    	}
				    });

					}
				}
				"""
				},
				"true\n42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2366
	// [Switch Expression] Assertion fails when IDE is launched with JVM option -ea
	public void testIssue2366() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
				  public X() {
				    super();
				  }
				  public static void foo() {
				    X z;
				    while (((z = getObject()) != null))      {
				        z.bar();
				      }
				    System.out.println(switch(42) {
					  default -> {
						try {
							yield 42;
						} finally {

						}
					  }
				    });
				  }
				  public void bar() {
				  }
				  public static X getObject() {
				    return null;
				  }
				  public static void main(String[] args) {
				    new X().foo();
				  }
				}
				"""
				},
				"42");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  public static void main(String[] args) {
				    double d = 3;

				    double r = switch (d) {
				      case 1.0 -> 0.2;
				      case 2.0 -> 0.5;
				      case 8.0 -> 2;
				      case 9.0 -> 3;
				      default -> 3;
				    };
				    System.out.println(r);
				  }

				  X() {}

				}
				"""
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	double r = switch (d) {\n"
				+ "	                   ^\n"
				+ "Cannot switch on a value of type double. Only convertible int values, strings or enum variables are permitted\n"
				+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_2() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  public static void main(String[] args) {
				    long d = 3;

				    double r = switch (d) {
				      case 1.0 -> 0.2;
				      case 2.0 -> 0.5;
				      case 8.0 -> 2;
				      case 9.0 -> 3;
				      default -> 3;
				    };
				    System.out.println(r);
				  }

				  X() {}

				}
				"""
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	double r = switch (d) {\n"
				+ "	                   ^\n"
				+ "Cannot switch on a value of type long. Only convertible int values, strings or enum variables are permitted\n"
				+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_3() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  public static void main(String[] args) {
				    float d = 3;

				    double r = switch (d) {
				      case 1.0 -> 0.2;
				      case 2.0 -> 0.5;
				      case 8.0 -> 2;
				      case 9.0 -> 3;
				      default -> 3;
				    };
				    System.out.println(r);
				  }

				  X() {}

				}
				"""
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	double r = switch (d) {\n"
				+ "	                   ^\n"
				+ "Cannot switch on a value of type float. Only convertible int values, strings or enum variables are permitted\n"
				+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_4() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  public static void main(String[] args) {
				    boolean d = true;

				    double r = switch (d) {
				      default -> 3;
				    };
				    System.out.println(r);
				  }

				  X() {}

				}
				"""
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	double r = switch (d) {\n"
				+ "	                   ^\n"
				+ "Cannot switch on a value of type boolean. Only convertible int values, strings or enum variables are permitted\n"
				+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_5() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  public static void main(String[] args) {
				    double d = 3;

				    switch (d) {
				      case 1.0 -> System.out.println(d);
				    };

				  }

				  X() {}

				}
				"""
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	switch (d) {\n"
				+ "	        ^\n"
				+ "Cannot switch on a value of type double. Only convertible int values, strings or enum variables are permitted\n"
				+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_6() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

                  static void foo() {}
				  public static void main(String[] args) {
				    double d = 3;

				    switch (foo()) {
				      case 1.0 -> System.out.println(d);
				    };

				  }

				  X() {}

				}
				"""
				},
				this.complianceLevel < ClassFileConstants.JDK21 ?
				"----------\n"
				+ "1. ERROR in X.java (at line 7)\n"
				+ "	switch (foo()) {\n"
				+ "	        ^^^^^\n"
				+ "Cannot switch on a value of type void. Only convertible int values, strings or enum variables are permitted\n"
				+ "----------\n" :
						"----------\n"
						+ "1. ERROR in X.java (at line 8)\n"
						+ "	case 1.0 -> System.out.println(d);\n"
						+ "	     ^^^\n"
						+ "Case constant of type double is incompatible with switch selector type void\n"
						+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_7() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

                  static void foo() {}
				  public static void main(String[] args) {
				    double d = 3;

				    switch (null) {
				      case null -> System.out.println(d);
				    };

				  }

				  X() {}

				}
				"""
				},
				this.complianceLevel < ClassFileConstants.JDK21 ?
				"----------\n"
				+ "1. ERROR in X.java (at line 7)\n"
				+ "	switch (null) {\n"
				+ "	        ^^^^\n"
				+ "Cannot switch on a value of type null. Only convertible int values, strings or enum variables are permitted\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 8)\n"
				+ "	case null -> System.out.println(d);\n"
				+ "	     ^^^^\n"
				+ "The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n"
				+ "----------\n" :
						"----------\n"
						+ "1. ERROR in X.java (at line 7)\n"
						+ "	switch (null) {\n"
						+ "	        ^^^^\n"
						+ "An enhanced switch statement should be exhaustive; a default label expected\n"
						+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_8() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

                  static void foo() {}
				  public static void main(String[] args) {
				    double d = 3;

				    switch (null) {
				      case null -> System.out.println(d);
				      default -> System.out.println("Default");
				    };

				  }

				  X() {}

				}
				"""
				},
				"3.0");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2382
	// VerifyError in switch expression on double
	public void testIssue2382_9() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

                  static void foo() {}
				  public static void main(String[] args) {
				    double d = 3;

				    switch (foo()) {
				      case null -> System.out.println(d);
				    };

				  }

				  X() {}

				}
				"""
				},
				this.complianceLevel < ClassFileConstants.JDK21 ?
				"----------\n"
				+ "1. ERROR in X.java (at line 7)\n"
				+ "	switch (foo()) {\n"
				+ "	        ^^^^^\n"
				+ "Cannot switch on a value of type void. Only convertible int values, strings or enum variables are permitted\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 8)\n"
				+ "	case null -> System.out.println(d);\n"
				+ "	     ^^^^\n"
				+ "The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above\n"
				+ "----------\n" :
						"----------\n" +
						"1. ERROR in X.java (at line 7)\n" +
						"	switch (foo()) {\n" +
						"	        ^^^^^\n" +
						"An enhanced switch statement should be exhaustive; a default label expected\n" +
						"----------\n" +
						"2. ERROR in X.java (at line 8)\n" +
						"	case null -> System.out.println(d);\n" +
						"	     ^^^^\n" +
						"Case constant of type null is incompatible with switch selector type void\n" +
						"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2387
	// [Switch Expression] Empty Stack exception compiling switch expression with exception handling
	public void testIssue2387() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static class Y {}
					void foo() {
						new X().new Y(){};
						System.out.println(switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
					}
				}
				"""
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 4)\n"
				+ "	new X().new Y(){};\n"
				+ "	^^^^^^^\n"
				+ "Illegal enclosing instance specification for type X.Y\n"
				+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2423
	// [Switch-expression] Internal compiler error: java.lang.ClassCastException while compiling switch expression with exception handling
	public void testIssue2423() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static String getString(int i) {
						System.out.println(switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
						return new String[] { "Hello", "World" }[i];
					}
					public static void main(String [] args) {
						System.out.println(getString(0));
					}
				}
				"""
				},
				"42\n"
				+ "Hello");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2447
	// [Switch-expressions] Internal inconsistency warning at compile time and verify error at runtime
	public void testIssue2447() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					static void foo(long l) {

					}
					public static void main(String[] args) {
						long [] larray = { 10 };

						foo(larray[0] = 10);
						System.out.println(switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
					}
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2451
	// Internal compiler error: java.lang.AssertionError: Anomalous/Inconsistent operand stack!
	public void testIssue2451() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				import java.util.HashMap;
				import java.util.Map;

				public class X {

					static void foo(long l) {

					}

					private static Map<String, Long> getLevelMapTable() {
						Map<String, Long> t = new HashMap<>();
						t.put(null, 0l);


						System.out.println(switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
						return null;
					}

					public static void main(String[] args) {
						getLevelMapTable();
					}
				}
				"""
				},
				"42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2453
	// [Switch-expressions] Internal inconsistency warning at compile time and verify error at runtime
	public void testIssue2453() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						System.out.println(double.class);
						System.out.println(switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
					}
				}
				"""
				},
				"double\n42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2455
	// [Switch-expressions] java.lang.VerifyError: Bad type on operand stack
	public void testIssue2455() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					static void foo(String s, int i) {
						System.out.println("String = " + s + " int = " + i);
					}
					public static void main(String[] args) {

						foo("Hello", switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						});
					}
				}
				"""
				},
				"String = Hello int = 42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2485
	// Empty stack error compiling project with broken classpath
	public void testIssue2485() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
				    public static int convertOpcode(int from, int to) {
				        return switch (from) {
				            case 42 ->
			                    switch (to) {
			                        case 42 -> 42;
			                        default -> throw new UnsupportedOperationException();
			                    };
				            default -> throw new UnsupportedOperationException();
				        };
				    }
				    public static void main(String [] args) {
				        System.out.println("With 42 & 42 = " + convertOpcode(42, 42));
			        }
				}
				"""
				},
				"With 42 & 42 = 42");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2476
	// [Switch expressions] java.lang.VerifyError: Bad type on operand stack
	public void testIssue2476() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						boolean b = args != null;
						foo(X[].class, b ? switch (42) {
						default -> {
							try {
								yield 42;
							} finally {

							}
						}
						} : 10);
					}

					static void foo(Class<?> c, int v) {
						System.out.println(c);
						System.out.println(v);
					}
				}
				"""
				},
				"class [LX;\n42");
	}

	// test mixing of -> and : in the same switch
	public void testMixingCaseStyles() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						 int i = switch(args.length) {
							 case 2: yield 2;
							 default -> 1;
							 case 1 : yield 2;
						 };
					}
				}
				"""
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	default -> 1;\n" +
				"	^^^^^^^\n" +
				"Mixing of '->' and ':' case statement styles is not allowed within a switch\n" +
				"----------\n");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3205
	// [Switch Expressions] ECJ accepts ambiguous method invocation involving switch expressions with poly type result expression
	public void testIssue3205() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				interface I {
					void foo();
				}

				interface J {
					void foo();
				}

				public class X implements I, J {

					public void foo() {
					}

					static void doit() {}

					public static void foo(Object o, J j) {
						System.out.println("Object");
					}

					public static void foo(String s, I i) {
						System.out.println(s);
					}

					public static void main(String[] args) {

						// Compiles with both ECJ and javac
						foo("OK", () -> {});

						// Compiles with both ECJ and javac
						foo("OK", args == null ? () -> {} : () -> {});

						// Compiles with both ECJ and javac
						foo("OK", X::doit);

						// Rejected by both ECJ and javac.
						//foo("OK", new X());

						// Rejected by javac, accepted by ECJ.
						foo("OK", switch (0) {
									 	case 0 -> () -> {};
									 	default -> () -> {};
						});
					}
				}
				"""
				},
				"OK\nOK\nOK\nOK");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3205
	// [Switch Expressions] ECJ accepts ambiguous method invocation involving switch expressions with poly type result expression
	public void testIssue3205_2() {
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				interface I {
					void foo();
				}

				interface J {
					void foo();
				}

				public class X implements I, J {

					public void foo() {
					}

					static void doit() {}

					public static void foo(Object o, J j) {
						System.out.println("Object");
					}

					public static void foo(String s, I i) {
						System.out.println(s);
					}

					public static void main(String[] args) {

						// Compiles with both ECJ and javac
						foo("OK", () -> {});

						// Compiles with both ECJ and javac
						foo("OK", args == null ? () -> {} : () -> {});

						// Compiles with both ECJ and javac
						foo("OK", X::doit);

						// Rejected by both ECJ and javac.
						foo("OK", new X());

						// Rejected by javac, accepted by ECJ.
						foo("OK", switch (0) {
									 	case 0 -> () -> {};
									 	default -> () -> {};
						});
					}
				}
				"""
				},
				"----------\n" +
				"1. ERROR in X.java (at line 36)\n" +
				"	foo(\"OK\", new X());\n" +
				"	^^^\n" +
				"The method foo(Object, J) is ambiguous for the type X\n" +
				"----------\n");
	}

	// fails when run as org.eclipse.jdt.core.tests.model.JavaSearchBugs14SwitchExpressionTests.testBug542559_0012
	public void testBug542559_0012() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				import java.util.function.Supplier;
				interface I0 { void i(); }
				interface I1 extends I0 {}
				interface I2 extends I0 {}
				public class X {
					I1 n1() { return null; }
					<I extends I2> I n2() { return null; }

					void test(int i, boolean b) {
						m(switch (i) {
							case 1 -> this::n1;
							default -> this::n2;
						}).i();
					}

					<M> M m(Supplier<M> m) { return m.get(); }

					public static void main(String[] args) {
						try {
							new X().test(1, true);
						} catch (NullPointerException e) {
							System.out.println("NPE as expected!");
						}
					}
				}
				"""
				},
				"NPE as expected!");
	}

	// Disabled until https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3259 and
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3258 are comprehensively
	// resolved. Same issue, but was "compensated" for earlier
	public void _testBug545567_22_minimal() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X implements AutoCloseable {
				   public static void main(String[] args) {
				       int t = switch (1) {
				               default -> {
				                   try (X x = new X()) {
				                       if (args.length < 1)
				                    	   yield 10;
				                       else
				                           yield 12;
				                       } finally {
				                            yield 3;
				                       }
				               }
				       };
				       System.out.println(t);
				   }

				   public void close() throws Exception {}
				}
				"""
				},
				"3");
	}

	public void testConditionalSwitchLabel() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
				    public static void main(String[] args) {
				        var foo = switch (10) {
				            case (10 > 20 ? 10 :  10) : yield "special" ;
				            default : yield "default value";
				        };
				        System.out.println(foo);
				    }

				}
				"""
				},
				"special");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3283
	// ECJ fails to recognize "yield" as a contextual keyword when there is a case fall-through
	public void testIssue3283() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static int var = 0;
					public static void main(String argv[]) {
				        int j = switch(var){
				        case 0: yield (0 + 42); // If this is removed, there's a different error
				        case 1: // If I remove this line, the error goes away
				        case 2: yield 1; // multiple errors
				        default:
				          throw new IllegalArgumentException("Unexpected value: " + var);
				    };
				    System.out.println("Yield = " + j);
				    }
				}
				"""
				},
				"Yield = 42");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=566124
	// Widening conversions combined with method invocation and switch expressions doesn't work
	public void testBug566124() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X  {
				    @SuppressWarnings("deprecation")
				    public void bar(int i) {
					boolean isNumeric = foo( switch(i+1) {
					   case 0 -> new Short((short)0);
					   case 2 -> new Double(2.0d);
					   default -> new Integer((short)6);
				    	});
				    	System.out.println(isNumeric);
				    }
				    boolean foo(short data){ return false; }
				    boolean foo(byte data){ return false; }
				    boolean foo(int data){ return false; }
				    boolean foo(float data){ return false; }
				    boolean foo(long data){ return false; }
				    boolean foo(double data){ return true; }

				    public static void main(String[] args) {
						X x = new X();
						x.bar(-1);
					}
				}
				"""
				},
				"true");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3481
	// [Switch Expression] yield does not work with bitwise complement ~
	public void testIssue3481() {
		this.runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
				    int a = switch(0) {
				        default -> {
				            yield ~1;
				        }
				    };
				    int b = switch(0) {
				        default -> {
				            int x = 0;
				            yield ~x;
				        }
				    };
				    int c = switch (0) {
				        case 0 -> {
				            yield ~2;
				        }
				        default -> 1;
				    };

				    {
				        System.out.println("a = " + a + " b = " + b + " c = " + c);
				    }

				    public static void main(String [] args) {
				        new X();
				    }
				}
				"""
				},
				"a = -2 b = -1 c = -3");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3554
	// [Switch Expressions] ArrayIndexOutOfBoundsException in Scope.leastContainingInvocation for sealed class and switch
	public void testIssue3554() {
		if (this.complianceLevel < ClassFileConstants.JDK21)
			return;
		this.runNegativeTest(
				new String[] {
				"X.java",
				"""
				public class X {

					sealed interface Index {

						enum TimeIndex implements Index {
							SECONDS;
						}
					}

					public class AbstractLine<S extends Index> {}

					public class AbstractTimeLine extends AbstractLine<X.Index.TimeIndex> {}

					@SuppressWarnings("unchecked")
					public static <S extends Index> AbstractLine<S> create(int id, S index, int owner) {
						return (AbstractLine<S>) switch (index) {
							case X.Index.TimeIndex tindex when tindex == X.Index.TimeIndex.SECONDS -> new AbstractTimeLine();
							default -> new AbstractLine<>(state().newId(), index, owner);
						};
					}
				}
				"""
				},
				"----------\n" +
				"1. ERROR in X.java (at line 18)\n" +
				"	default -> new AbstractLine<>(state().newId(), index, owner);\n" +
				"	                              ^^^^^\n" +
				"The method state() is undefined for the type X\n" +
				"----------\n");
	}
}