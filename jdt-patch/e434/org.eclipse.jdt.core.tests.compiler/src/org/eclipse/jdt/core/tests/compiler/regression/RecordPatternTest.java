/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class RecordPatternTest extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 21");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testRecPatExhaust018" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return RecordPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_21);
	}
	public RecordPatternTest(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_21);
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
		runConformTest(testFiles, expectedOutput, getCompilerOptions(false));
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE21Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, new String[] {}, JAVAC_OPTIONS);
	}
	protected void runConformTest(
			String[] testFiles,
			String expectedOutputString,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			String[] vmArguments) {
			runTest(
		 		// test directory preparation
				shouldFlushOutputDirectory /* should flush output directory */,
				testFiles /* test files */,
				// compiler options
				classLibraries /* class libraries */,
				null /* no custom options */,
				false /* do not perform statements recovery */,
				null /* no custom requestor */,
				// compiler results
				false /* expecting no compiler errors */,
				null /* do not check compiler log */,
				// runtime options
				false /* do not force execution */,
				vmArguments /* vm arguments */,
				// runtime results
				expectedOutputString /* expected output string */,
				null /* do not check error string */,
				// javac options
				JavacTestOptions.DEFAULT /* default javac test options */);
		}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String javacLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = javacLog;
		runner.runNegativeTest();
	}
	/*
	 * Basic tests that accept a valid record pattern and make the pattern variable available
	 */
	public void test001() {
		Map<String, String> options = getCompilerOptions(false);
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr) ) {\n"
				+ "        System.out.println(\"Upper-left corner:\");\n"
				+ "    }\n"
				+ "  }\n"
				+ "  public static void main(String[] obj) {\n"
				+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),\n"
				+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"Upper-left corner:",
				options);
	}
	// Test that pattern variables are allowed for the nested patterns (not just the outermost record pattern)
	public void test002() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")"
				+ "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr)  -> {\n"
				+ "        		yield 1;\n"
				+ "        }\n"
				+ "        default -> 0;\n"
				+ "    };\n"
				+ "    System.out.println(res);\n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),\n"
				+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"1");
	}
	public void test003() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr)) {\n"
				+ "        System.out.println(\"Upper-left corner: \" + r1);\n"
				+ "    }\n"
				+ "  }\n"
				+ "  public static void main(String[] obj) {\n"
				+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),\n"
				+ "    new ColoredPoint(new Point(10, 15), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	System.out.println(\"Upper-left corner: \" + r1);\n" +
				"	                                           ^^\n" +
				"r1 cannot be resolved to a variable\n" +
				"----------\n");
	}
	public void test004() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr)) {\n"
				+ "    }\n"
				+ "  }\n"
				+ "  public static void main(String[] obj) {\n"
				+ "    print(new Rectangle(new ColoredPoint(new PointTypo(0, 0), Color.BLUE),\n"
				+ "    new ColoredPoint(new Point(10, 15), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	print(new Rectangle(new ColoredPoint(new PointTypo(0, 0), Color.BLUE),\n" +
				"	                                         ^^^^^^^^^\n" +
				"PointTypo cannot be resolved to a type\n" +
				"----------\n");
	}
	// Test that non record types are reported in a record pattern
	public void test005() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof Rectangle(ColoredPoint(Point(int i, int j), Color c),\n"
				+ "	    									ColoredPoint lr)) {\n"
				+ "	        System.out.println(\"Upper-left corner: \");\n"
				+ "	    }\n"
				+ "  }\n"
				+ "}\n"
				+ "class Point{}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (r instanceof Rectangle(ColoredPoint(Point(int i, int j), Color c),\n" +
				"	                                        ^^^^^\n" +
				"Only record types are permitted in a record pattern\n" +
				"----------\n");
	}
	// Test that record patterns that don't have same no of patterns as record components are reported
	public void test006() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof Rectangle(ColoredPoint(Point(int i), Color c),\n"
				+ "	    									ColoredPoint lr)) {\n"
				+ "	        System.out.println(\"Upper-left corner: \");\n"
				+ "	    }\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (r instanceof Rectangle(ColoredPoint(Point(int i), Color c),\n" +
				"	                                        ^^^^^^^^^^^^\n" +
				"Record pattern should match the signature of the record declaration\n" +
				"----------\n");
	}
	public void test007() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof Rectangle(ColoredPoint(Point(String o1, String o2), Color c),\n"
				+ "	    									ColoredPoint lr)) {\n"
				+ "	        System.out.println(\"Upper-left corner: \" );\n"
				+ "	    }\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (r instanceof Rectangle(ColoredPoint(Point(String o1, String o2), Color c),\n" +
				"	                                              ^^^^^^^^^\n" +
				"Record component with type int is not compatible with type String\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	if (r instanceof Rectangle(ColoredPoint(Point(String o1, String o2), Color c),\n" +
				"	                                                         ^^^^^^^^^\n" +
				"Record component with type int is not compatible with type String\n" +
				"----------\n");
	}
	// Test that pattern types that don't match record component's types are reported
	public void test008() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof Rectangle(ColoredPoint(Point(int i, int j), Color c), ColoredPoint lr, Object obj)) {\n"
				+ "	        System.out.println(\"Upper-left corner: \" );\n"
				+ "	    }\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	if (r instanceof Rectangle(ColoredPoint(Point(int i, int j), Color c), ColoredPoint lr, Object obj)) {\n" +
				"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Record pattern should match the signature of the record declaration\n" +
				"----------\n");
	}
	// Test that A pattern p dominates a record pattern with type R if p is unconditional at R.
	//	case Rectangle c -> {
	//		yield 0;
	//	}
	//	case Rectangle(ColoredPoint(Point(int x, int y), Color c),
	//			ColoredPoint(Point(int x1, int y1), Color c1)) r1 -> {
	//		yield r1.lowerRight().p().y();
	//	}
	public void test009() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       	case Rectangle c -> {\n"
				+ "			yield 0;\n"
				+ "		}\n"
				+ "		case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "				ColoredPoint(Point(int x1, int y1), Color c1)) -> {\n"
				+ "			yield 1;\n"
				+ "		}\n"
				+ "    };\n"
				+ "    System.out.println(res);\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n" +
				"				ColoredPoint(Point(int x1, int y1), Color c1)) -> {\n" +
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	// Test that nested pattern variables from record patterns are in scope in the case block
	public void test10() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")"
				+ "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr)  -> {\n"
				+ "    				System.out.println(\"x= \" + x);\n"
				+ "    				System.out.println(\"y= \" + y);\n"
				+ "    				System.out.println(\"lr= \" + lr);\n"
				+ "    				System.out.println(\"lr.c()= \" + lr.c());\n"
				+ "    				System.out.println(\"lr.p()= \" + lr.p());\n"
				+ "    				System.out.println(\"lr.p().x()= \" + lr.p().x());\n"
				+ "    				System.out.println(\"lr.p().y()= \" + lr.p().y());\n"
				+ "    				System.out.println(\"c= \" + c);\n"
				+ "        		yield x;\n"
				+ "        }\n"
				+ "        default -> 0;\n"
				+ "    };\n"
				+ "    System.out.println(\"Returns: \" + res);\n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),\n"
				+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"x= 15\n" +
				"y= 5\n" +
				"lr= ColoredPoint[p=Point[x=30, y=10], c=RED]\n" +
				"lr.c()= RED\n" +
				"lr.p()= Point[x=30, y=10]\n" +
				"lr.p().x()= 30\n" +
				"lr.p().y()= 10\n" +
				"c= BLUE\n" +
				"Returns: 15");
	}
	// Test that nested pattern variables from record patterns are in not scope outside the case block
	public void test11() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "		case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "				ColoredPoint(Point(int x1, int y1), Color c1)) -> {\n"
				+ "			yield 1;\n"
				+ "		}\n"
				+ "		default -> {yield x;}"
				+ "    };\n"
				+ "    System.out.println(res);\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	default -> {yield x;}    };\n" +
				"	                  ^\n" +
				"x cannot be resolved to a variable\n" +
				"----------\n");
	}
	// Test that nested pattern variables from record patterns are in not scope outside the case block
	public void test12() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "		case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "				ColoredPoint(Point(int x1, int y1), Color c1)) -> {\n"
						+ "			yield 1;\n"
						+ "		}\n"
						+ "		default -> {yield x1;}"
						+ "    };\n"
						+ "    System.out.println(res);\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
			},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	default -> {yield x1;}    };\n" +
				"	                  ^^\n" +
				"x1 cannot be resolved to a variable\n" +
				"----------\n");
	}
	// Test that when expressions are supported and pattern variables are available inside when expressions
	public void test13() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "  public static void printLowerRight(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "                               ColoredPoint lr)  when x > 0 -> {\n"
						+ "        		yield 1;\n"
						+ "        }\n"
						+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "                               ColoredPoint lr)  when x <= 0 -> {\n"
						+ "        		yield -1;\n"
						+ "        }\n"
						+ "        default -> 0;\n"
						+ "    };\n"
						+ "    System.out.println(\"Returns: \" + res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),\n"
						+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
						+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE),\n"
						+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
		},
				"Returns: -1\n" +
				"Returns: 1");
	}
	// Test that record patterns with 1 record components are accepted
	public void test14() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "  public static void print(Record r) {\n"
						+ "    int res = switch(r) {\n"
						+ "       case Record(int x) -> x ;\n"
						+ "        default -> 0;\n"
						+ "    };\n"
						+ "    System.out.println(\"Returns: \" + res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    print(new Record(3));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Record(int x) {}\n"
						},
				"Returns: 3");
	}
	// Test that record patterns with 0 record components are accepted
	public void test15() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "	 @SuppressWarnings(\"preview\")\n"
						+ "	public static void print(Pair p) {\n"
						+ "    if (p instanceof Pair(Teacher(Object n), Student(Object n1, Integer i)) ) {\n"
						+ "			 System.out.println(n1);\n"
						+ "		 } else {\n"
						+ "			 System.out.println(\"ELSE\");\n"
						+ "		 }\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    print(new Pair(new Teacher(\"123\"), new Student(\"abc\", 1)));\n"
						+ "  }\n"
						+ "}\n"
						+ "sealed interface Person permits Student, Teacher {\n"
						+ "    String name();\n"
						+ "}\n"
						+ " record Student(String name, Integer id) implements Person {}\n"
						+ " record Teacher(String name) implements Person {}\n"
						+ " record Pair(Person s, Person s1) {}\n"
						},
				"abc");
	}
	// Should not reach IF or throw CCE.
	// Should reach ELSE
	public void test16() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "	 @SuppressWarnings(\"preview\")\n"
						+ "	public static void print(Pair p) {\n"
						+ "    if (p instanceof Pair(Teacher(Object n), Student(Object n1, Integer i))) {\n"
						+ "			 System.out.println(\"IF\");\n"
						+ "		 } else {\n"
						+ "			 System.out.println(\"ELSE\");\n"
						+ "		 }\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    print(new Pair(new Student(\"abc\", 1), new Teacher(\"123\")));\n"
						+ "  }\n"
						+ "}\n"
						+ "sealed interface Person permits Student, Teacher {\n"
						+ "    String name();\n"
						+ "}\n"
						+ " record Student(String name, Integer id) implements Person {}\n"
						+ " record Teacher(String name) implements Person {}\n"
						+ " record Pair(Person s, Person s1) {}\n"
						},
				"ELSE");
	}
	public void test17() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "	 @SuppressWarnings(\"preview\")\n"
						+ "	public static void print(Pair p) {\n"
						+ "    if (p instanceof Pair(Teacher(Object n), Student(Object n1, Integer i))) {\n"
						+ "			 System.out.println(n1.getClass().getTypeName() + \":\" + n1 + \",\" + i);\n"
						+ "		 } else {\n"
						+ "			 System.out.println(\"ELSE\");\n"
						+ "		 }\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    print(new Pair(new Teacher(\"123\"), new Student(\"abc\", 10)));\n"
						+ "  }\n"
						+ "}\n"
						+ "sealed interface Person permits Student, Teacher {\n"
						+ "    String name();\n"
						+ "}\n"
						+ " record Student(String name, Integer id) implements Person {}\n"
						+ " record Teacher(String name) implements Person {}\n"
						+ " record Pair(Person s, Person s1) {}\n"
						},
				"java.lang.String:abc,10");
	}
	// Same as 17(), but base type instead of wrapper
	public void test18() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "	 @SuppressWarnings(\"preview\")\n"
						+ "	public static void print(Pair p) {\n"
						+ "    if (p instanceof Pair(Teacher(Object n), Student(Object n1, int i))) {\n"
						+ "			 System.out.println(n1.getClass().getTypeName() + \":\" + n1 + \",\" + i);\n"
						+ "		 } else {\n"
						+ "			 System.out.println(\"ELSE\");\n"
						+ "		 }\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    print(new Pair(new Teacher(\"123\"), new Student(\"abc\", 10)));\n"
						+ "  }\n"
						+ "}\n"
						+ "sealed interface Person permits Student, Teacher {\n"
						+ "    String name();\n"
						+ "}\n"
						+ " record Student(String name, int id) implements Person {}\n"
						+ " record Teacher(String name) implements Person {}\n"
						+ " record Pair(Person s, Person s1) {}\n"
						},
				"java.lang.String:abc,10");
	}
	public void test19() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	 @SuppressWarnings(\"preview\")\n"
						+ "	public static void print(Pair p) {\n"
						+ "		 int res1 = switch(p) {\n"
						+ "		 	case Pair(Student(Object n1, int i), Teacher(Object n)) -> {\n"
						+ "              	   yield i;\n"
						+ "                 }\n"
						+ "		 	default -> -1;\n"
						+ "		 };\n"
						+ "		 System.out.println(res1);\n"
						+ "  }\n"
						+ "	 public static void main(String[] args) {\n"
						+ "		print(new Pair( new Student(\"abc\", 15), new Teacher(\"123\")));\n"
						+ "		print(new Pair( new Teacher(\"123\"), new Student(\"abc\", 1)));\n"
						+ "	}\n"
						+ "}\n"
						+ "sealed interface Person permits Student, Teacher {\n"
						+ "    String name();\n"
						+ "}\n"
						+ " record Student(String name, int id) implements Person {}\n"
						+ " record Teacher(String name) implements Person {}\n"
						+ " record Pair(Person s, Person s1) {}\n"
						},
				"15\n"
				+ "-1");
	}
	// Test that Object being pattern-checked works in switch-case
	public void test20() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	 @SuppressWarnings(\"preview\")\n"
						+ "	public static void print(Object p) {\n"
						+ "		 int res1 = switch(p) {\n"
						+ "		 	case Pair(Student(Object n1, int i), Teacher(Object n)) -> {\n"
						+ "              	   yield i;\n"
						+ "                 }\n"
						+ "		 	default -> -1;\n"
						+ "		 };\n"
						+ "		 System.out.println(res1);\n"
						+ "  }\n"
						+ "	 public static void main(String[] args) {\n"
						+ "		print(new Pair( new Student(\"abc\", 15), new Teacher(\"123\")));\n"
						+ "		print(new Pair( new Teacher(\"123\"), new Student(\"abc\", 1)));\n"
						+ "	}\n"
						+ "}\n"
						+ "sealed interface Person permits Student, Teacher {\n"
						+ "    String name();\n"
						+ "}\n"
						+ " record Student(String name, int id) implements Person {}\n"
						+ " record Teacher(String name) implements Person {}\n"
						+ " record Pair(Person s, Person s1) {}\n"
						},
				"15\n"
				+ "-1");
	}
	// // Test that Object being pattern-checked works in 'instanceof'
	public void test21() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "	 @SuppressWarnings(\"preview\")\n"
						+ "	public static void print(Object p) {\n"
						+ "    if (p instanceof Pair(Student(Object n1, int i), Teacher(Object n))) {\n"
						+ "      System.out.println(i);\n"
						+ "    }\n"
						+ "  }\n"
						+ "	 public static void main(String[] args) {\n"
						+ "		print(new Pair( new Student(\"abc\", 15), new Teacher(\"123\")));\n"
						+ "		print(new Pair( new Teacher(\"123\"), new Student(\"abc\", 1)));\n"
						+ "	}\n"
						+ "}\n"
						+ "sealed interface Person permits Student, Teacher {\n"
						+ "    String name();\n"
						+ "}\n"
						+ " record Student(String name, int id) implements Person {}\n"
						+ " record Teacher(String name) implements Person {}\n"
						+ " record Pair(Person s, Person s1) {}\n"
						},
				"15");
	}
	// Nested record pattern with a simple (constant) 'when' clause
	public void test22() {
		runConformTest(new String[] {
				"X.java",
					"public class X {\n"
					+ "  public static void printLowerRight(Rectangle r) {\n"
					+ "    int res = switch(r) {\n"
					+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
					+ "                               ColoredPoint lr) when x > 1 -> {\n"
					+ "                            	   System.out.println(\"one\");\n"
					+ "        		yield x;\n"
					+ "        }\n"
					+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
					+ "                               ColoredPoint lr) when x <= 0 -> {\n"
					+ "                            	   System.out.println(\"two\");	\n"
					+ "        		yield x;\n"
					+ "        }\n"
					+ "        default -> 0;\n"
					+ "    };\n"
					+ "    System.out.println(\"Returns: \" + res);\n"
					+ "  }\n"
					+ "  public static void main(String[] args) {\n"
					+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),\n"
					+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
					+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE),\n"
					+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
					+ "  }\n"
					+ "}\n"
					+ "record Point(int x, int y) {}\n"
					+ "enum Color { RED, GREEN, BLUE }\n"
					+ "record ColoredPoint(Point p, Color c) {}\n"
					+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
						},
				"two\n" +
				"Returns: 0\n" +
				"one\n" +
				"Returns: 5",
				getCompilerOptions(false),
				null,
				JavacTestOptions.SKIP); // Javac crashes. Let's skip for no
	}
	// Nested record pattern with a method invocation in a 'when' clause
	public void test23 () {
		runConformTest(new String[] {
				"X.java",
					"@SuppressWarnings(\"preview\")"
					+ "public class X {\n"
					+ "  public static void printLowerRight(Rectangle r) {\n"
					+ "    int res = switch(r) {\n"
					+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
					+ "                               ColoredPoint lr) when x > value() -> {\n"
					+ "                            	   System.out.println(\"one\");\n"
					+ "        		yield x;\n"
					+ "        }\n"
					+ "        default -> 0;\n"
					+ "    };\n"
					+ "    System.out.println(\"Returns: \" + res);\n"
					+ "  }\n"
					+ "  public static int value() {\n"
					+ "    return 0;\n"
					+ "  }\n"
					+ "  public static void main(String[] args) {\n"
					+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),\n"
					+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
					+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE),\n"
					+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
					+ "  }\n"
					+ "}\n"
					+ "record Point(int x, int y) {}\n"
					+ "enum Color { RED, GREEN, BLUE }\n"
					+ "record ColoredPoint(Point p, Color c) {}\n"
					+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
						},
				"Returns: 0\n" +
				"one\n" +
				"Returns: 5");
	}
	// Nested record pattern with another switch expression + record pattern in a 'when' clause
	// Failing now.
	public void test24() {
		runConformTest(new String[] {
				"X.java",
					"public class X {\n"
					+ "  @SuppressWarnings(\"preview\")\n"
					+ "  public static void printLowerRight(Object r) {\n"
					+ "    int res = switch(r) {\n"
					+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
					+ "    		   				ColoredPoint lr) when x >\n"
					+ "								       switch(r) {\n"
					+ "								       	 case Rectangle(ColoredPoint c1,  ColoredPoint lr1) -> 2;\n"
					+ "								       	 default -> 3;\n"
					+ "								       }\n"
					+ "								       	-> x;\n"
					+ "								       default -> 0;\n"
					+ "    			};\n"
					+ "    			System.out.println(\"Returns: \" + res);\n"
					+ "  }\n"
					+ "  public static void main(String[] args) {\n"
					+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),\n"
					+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
					+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE),\n"
					+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
					+ "  }\n"
					+ "}\n"
					+ "record Point(int x, int y) {}\n"
					+ "enum Color { RED, GREEN, BLUE }\n"
					+ "record ColoredPoint(Point p, Color c) {}\n"
					+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
						},
				"Returns: 0\n" +
				"Returns: 5");
	}
	public void test24a() {
		runConformTest(new String[] {
				"X.java",
					"public class X {\n"
					+ "  @SuppressWarnings(\"preview\")\n"
					+ "  public static void printLowerRight(Object r) {\n"
					+ "    	  int x = 0;\n"
					+ "       if (r instanceof Rectangle(ColoredPoint c,  ColoredPoint lr) && x < switch(r) {\n"
					+ "    	 case Rectangle(ColoredPoint c1,  ColoredPoint lr1)  -> 2;\n"
					+ "    	 default -> 3;\n"
					+ "	  }) {\n"
					+ "		  System.out.println(\"IF\");\n"
					+ "	  }\n"
					+ "  }\n"
					+ "  public static void main(String[] args) {\n"
					+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE),\n"
					+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
					+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(5, 5), Color.BLUE),\n"
					+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
					+ "  }\n"
					+ "}\n"
					+ "record Point(int x, int y) {}\n"
					+ "enum Color { RED, GREEN, BLUE }\n"
					+ "record ColoredPoint(Point p, Color c) {}\n"
					+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
						},
				"IF\n" +
				"IF");
	}
	//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/157
	public void test25() {
		String currentWorkingDirectoryPath = System.getProperty("user.dir");
		this.extraLibPath = currentWorkingDirectoryPath + File.separator + "libtest25.jar";
		try {
		Util.createJar(
			new String[] {
				"p/RecordPattern1.java;\n",
				"package p;\n"
				+ "public class RecordPattern1 {}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color {\n"
				+ "	RED, GREEN, BLUE\n"
				+ "}\n",
				"p/ColoredPoint.java",
				"package p;\n"
				+ "public record ColoredPoint(Point p, Color c) {}\n",
				"p/Rectangle.java",
				"package p;\n"
				+ "public record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}\n",
			},
			this.extraLibPath,
			JavaCore.VERSION_21);
		this.runConformTest(
				new String[] {
						"p/X.java",
						"package p;\n"
						+ "public class X {\n"
						+ "	public static void printLowerRight(Rectangle r) {\n"
						+ "		int res = switch(r) {\n"
						+ "		case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "				ColoredPoint lr)  -> {\n"
						+ "					yield 1;\n"
						+ "				}\n"
						+ "				default -> 0;\n"
						+ "		};\n"
						+ "		System.out.println(res);\n"
						+ "	}\n"
						+ "	public static void main(String[] args) {\n"
						+ "			    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE),\n"
						+ "			        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
						+ "			  }\n"
						+ "}\n"
				},
				"1",
				getCompilerOptions(false),
				new String[0],
				JavacTestOptions.SKIP); // Too complicated to pass extra lib to Javac, let's skip
		} catch (IOException e) {
			System.err.println("RecordPatternTest.test25() could not write to current working directory " + currentWorkingDirectoryPath);
		} finally {
			new File(this.extraLibPath).delete();
		}
	}
	// Test that pattern variables declared in instanceof can't be used in a switch/case
	public void test26() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    	if (r instanceof Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "			ColoredPoint lr) && x > (switch(r) {\n"
						+ "										case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "												ColoredPoint lr) -> {\n"
						+ "													yield 1;\n"
						+ "												}\n"
						+ "												default -> 0;\n"
						+ "												})) {\n"
						+ "		System.out.println(x);\n"
						+ "	  }\n"
						+ "	}\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
			},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n" +
				"	                                      ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n" +
				"	                                             ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n" +
				"	                                                       ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	ColoredPoint lr) -> {\n" +
				"	             ^^\n" +
				"A pattern variable with the same name is already defined in the statement\n");
	}
	// Test that pattern variables declared in switch/case can't be used in an instanceof expression part of the 'when' clause
	// not relevant anymore since named record patterns are not there - 20
	public void test27() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "	int res = switch(r) {\n"
						+ "		case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {\n"
						+ "				yield 1;\n"
						+ "			}\n"
						+ "			default -> 0;\n"
						+ "	};\n"
						+ "	}\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
			},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {\n" +
				"	                                                                                                                      ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {\n" +
				"	                                                                                                                             ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 4)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {\n" +
				"	                                                                                                                                       ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 4)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {\n" +
				"	                                                                                                                                       ^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n");
	}
	// Test nested record patterns in 'instanceof' within a swith-case with similar record pattern
	public void test28() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "		case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when (r instanceof  Rectangle(ColoredPoint(Point(int x1, int y1), Color c1),\n"
				+ "				ColoredPoint lr1)) -> {\n"
				+ "				yield lr1.p().y();\n"
				+ "			}\n"
				+ "			default -> 0;\n"
				+ "     };\n"
				+ "   System.out.println(res);\n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    print(new Rectangle(new ColoredPoint(new Point(1,1), Color.RED), new ColoredPoint(new Point(5,5), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
			},
			"5");
	}
	// Test that a simple type pattern dominates a following record pattern of the same type
	public void test29() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	@SuppressWarnings(\"preview\")\n"
						+ "	public void foo(Object o) {\n"
						+ "       int res = switch (o) {\n"
						+ "        case R r -> 1;\n"
						+ "        case R(int a) -> 0;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "	}\n"
						+ "}\n"
						+ "record R(int i) {}"
		},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case R(int a) -> 0;\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	// Test that an identical record pattern dominates another record pattern
	public void test30() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	@SuppressWarnings(\"preview\")\n"
						+ "	public void foo(Object o) {\n"
						+ "       int res = switch (o) {\n"
						+ "        case R(int a) -> 1;\n"
						+ "        case R(int a) -> 0;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "	}\n"
						+ "}\n"
						+ "record R(int i) {}"
		},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case R(int a) -> 0;\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	// Test that a type pattern with 'when' does not dominate a record pattern of the same type
	public void test31() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	public boolean predicate() { return true; }\n"
						+ "	public void foo(Object o) {\n"
						+ "       int res = switch (o) {\n"
						+ "        case R r when predicate() -> 1;\n"
						+ "        case R(int a) -> 0;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "       System.out.println(res);\n"
						+ "	}\n"
						+ "  public static void main(String[] args) {\n"
						+ "    (new X()).foo(new R(10));\n"
						+ "  }\n"
						+ "}\n"
						+ "record R(int i) {}"
		},
			"1");
	}
	// Test that a type pattern with 'when' does not dominate a record pattern of the same type
	public void test31a() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	public boolean predicate() { return false; }\n"
						+ "	public void foo(Object o) {\n"
						+ "       int res = switch (o) {\n"
						+ "        case R r when predicate() -> 1;\n"
						+ "        case R(int a) -> 0;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "       System.out.println(res);\n"
						+ "	}\n"
						+ "  public static void main(String[] args) {\n"
						+ "    (new X()).foo(new R(10));\n"
						+ "  }\n"
						+ "}\n"
						+ "record R(int i) {}"
		},
			"0");
	}
	// Test that a record pattern with 'when' does not dominate an identical record pattern
	public void test32() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	public boolean predicate() { return true; }\n"
						+ "	public void foo(Object o) {\n"
						+ "       int res = switch (o) {\n"
						+ "        case R(int a)  when predicate() -> 1;\n"
						+ "        case R(int a) -> 0;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "       System.out.println(res);\n"
						+ "	}\n"
						+ "  public static void main(String[] args) {\n"
						+ "    (new X()).foo(new R(10));\n"
						+ "  }\n"
						+ "}\n"
						+ "record R(int i) {}"
		},
			"1");
	}
	// Test that a record pattern with 'when' does not dominate an identical record pattern
	public void test32a() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "	public boolean predicate() { return false; }\n"
				+ "	public void foo(Object o) {\n"
				+ "       int res = switch (o) {\n"
				+ "        case R(int a)  when predicate() -> 1;\n"
				+ "        case R(int a) -> 0;\n"
				+ "        default -> -1;\n"
				+ "       };\n"
				+ "       System.out.println(res);\n"
				+ "	}\n"
				+ "  public static void main(String[] args) {\n"
				+ "    (new X()).foo(new R(10));\n"
				+ "  }\n"
				+ "}\n"
				+ "record R(int i) {}"
		},
			"0");
	}
	// Test that a parenthesized type pattern dominates a record pattern of the same type
	public void test33() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	@SuppressWarnings(\"preview\")\n"
						+ "	public void foo(Object o) {\n"
						+ "       int res = switch (o) {\n"
						+ "        case R r -> 1;\n"
						+ "        case R(int a) -> 0;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "	}\n"
						+ "}\n"
						+ "record R(int i) {}"
		},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case R(int a) -> 0;\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	// Test that a parenthesized record pattern dominates an identical record pattern
	public void test34() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	@SuppressWarnings(\"preview\")\n"
						+ "	public void foo(Object o) {\n"
						+ "       int res = switch (o) {\n"
						+ "        case R(int a) -> 1;\n"
						+ "        case R(int a) -> 0;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "	}\n"
						+ "}\n"
						+ "record R(int i) {}"
		},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case R(int a) -> 0;\n" +
				"	     ^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	// Test that pattern dominance is reported on identical nested record pattern
	public void test35() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	@SuppressWarnings(\"preview\")\n"
						+ "	public void foo(Object o) {\n"
						+ "       int res = switch (o) {\n"
						+ "       case Pair(Teacher(Object n), Student(Object n1, Integer i)) -> 0;\n"
						+ "       case Pair(Teacher(Object n), Student(Object n1, Integer i)) -> 1;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "       System.out.println(res);\n"
						+ "	}\n"
						+ "	public static void main(String[] args) {\n"
						+ "		(new X()).foo(new Pair(new Teacher(\"123\"), new Student(\"abc\", 10)));\n"
						+ "	}\n"
						+ "}\n"
						+ "record R(int i) {}\n"
						+ "sealed interface Person permits Student, Teacher {\n"
						+ "	String name();\n"
						+ "}\n"
						+ "record Student(String name, Integer id) implements Person {}\n"
						+ "record Teacher(String name) implements Person {}\n"
						+ "record Pair(Person s, Person s1) {} "
		},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case Pair(Teacher(Object n), Student(Object n1, Integer i)) -> 1;\n" +
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	// Test that pattern dominance is reported on identical nested record pattern
	public void test36() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	@SuppressWarnings(\"preview\")\n"
						+ "	public void foo(Object o) {\n"
						+ "       int res = switch (o) {\n"
						+ "       case Pair(Teacher(Object n), Student(Object n1, Integer i)) -> 0;\n"
						+ "       case Pair(Teacher(Object n), Student(String n1, Integer i)) -> 1;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "       System.out.println(res);\n"
						+ "	}\n"
						+ "	public static void main(String[] args) {\n"
						+ "		(new X()).foo(new Pair(new Teacher(\"123\"), new Student(\"abc\", 10)));\n"
						+ "	}\n"
						+ "}\n"
						+ "record R(int i) {}\n"
						+ "sealed interface Person permits Student, Teacher {\n"
						+ "	String name();\n"
						+ "}\n"
						+ "record Student(String name, Integer id) implements Person {}\n"
						+ "record Teacher(String name) implements Person {}\n"
						+ "record Pair(Person s, Person s1) {} "
		},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case Pair(Teacher(Object n), Student(String n1, Integer i)) -> 1;\n" +
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This case label is dominated by one of the preceding case labels\n" +
				"----------\n");
	}
	// Test that pattern dominance is reported on identical nested record pattern
	public void test37() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	@SuppressWarnings(\"preview\")\n"
						+ "	public void foo(Object o) {\n"
						+ "       int res = switch (o) {\n"
						+ "       case Pair(Teacher(Object n), Student(String n1, Integer i)) -> 0;\n"
						+ "       case Pair(Teacher(Object n), Student(Object n1, Integer i)) -> 1;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "       System.out.println(res);\n"
						+ "	}\n"
						+ "	public static void main(String[] args) {\n"
						+ "		(new X()).foo(new Pair(new Teacher(\"123\"), new Student(\"abc\", 10)));\n"
						+ "	}\n"
						+ "}\n"
						+ "record R(int i) {}\n"
						+ "sealed interface Person permits Student, Teacher {\n"
						+ "	String name();\n"
						+ "}\n"
						+ "record Student(String name, Integer id) implements Person {}\n"
						+ "record Teacher(String name) implements Person {}\n"
						+ "record Pair(Person s, Person s1) {} "
		},
				"0");
	}
	// Test that null is not matched to any pattern
	public void test38() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "		case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when (r instanceof  Rectangle(ColoredPoint(Point(int x1, int y1), Color c1),\n"
						+ "				ColoredPoint lr1)) -> {\n"
						+ "				yield lr1.p().y();\n"
						+ "			}\n"
						+ "			default -> 0;\n"
						+ "     };\n"
						+ "   System.out.println(res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    print(new Rectangle(new ColoredPoint(null, Color.RED), new ColoredPoint(new Point(5,5), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
		},
		"0");
	}
	public void test39() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "		case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when (r instanceof  Rectangle(ColoredPoint(Point(int x1, int y1), Color c1),\n"
						+ "				ColoredPoint lr1)) -> {\n"
						+ "				yield lr1.p().y();\n"
						+ "			}\n"
						+ "			default -> 0;\n"
						+ "     };\n"
						+ "   System.out.println(res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    print(new Rectangle(null, null));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
		},
		"0");
	}
	public void test40() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "		case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when (r instanceof  Rectangle(ColoredPoint(Point(int x1, int y1), Color c1),\n"
						+ "				ColoredPoint lr1)) -> {\n"
						+ "				yield lr1.p().y();\n"
						+ "			}\n"
						+ "			default -> 0;\n"
						+ "     };\n"
						+ "   System.out.println(res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    try {\n"
						+ "		  print(null);\n"
						+ "	  } catch(NullPointerException e) {\n"
						+ "		  System.out.println(\"NPE with \" + e.toString());\n"
						+ "	  }\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
		},
		"NPE with java.lang.NullPointerException");
	}
	public void test41() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "  @SuppressWarnings(\"preview\")\n"
						+ "  public static void printLowerRight(Object r) {\n"
						+ "    long res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(var x, long y), Color c), \n"
						+ "    		   				ColoredPoint lr) when x > \n"
						+ "								       switch(r) {\n"
						+ "								       	 case Rectangle(ColoredPoint c1,  ColoredPoint lr1) -> 2;  \n"
						+ "								       	 default -> 10;   \n"
						+ "								       } \n"
						+ "								       	-> x + 10;  \n"
						+ "								       default -> 0;     \n"
						+ "    			};    \n"
						+ "    			System.out.println(\"Returns: \" + res);\n"
						+ "  } \n"
						+ "  public static void main(String[] args) {\n"
						+ "	printLowerRight(new Rectangle(new ColoredPoint(new Point(2, 4), Color.GREEN), new ColoredPoint(new Point(5, 4), Color.GREEN))); \n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(long x, long y) {}    \n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {} \n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
		},
		"Returns: 0");
	}
	public void test42() {
		runConformTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "  @SuppressWarnings(\"preview\")\n"
						+ "  public static void printLowerRight(Object r) {\n"
						+ "    long res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(var x, long y), Color c), \n"
						+ "    		   				ColoredPoint lr) when x > \n"
						+ "								       switch(r) {\n"
						+ "								       	 case Rectangle(ColoredPoint c1,  var lr1)  -> lr1.p().x();\n"
						+ "								       	 default -> 10;   \n"
						+ "								       } \n"
						+ "								       	-> x + 10;  \n"
						+ "								       default -> 0;     \n"
						+ "    			};    \n"
						+ "    			System.out.println(\"Returns: \" + res);\n"
						+ "  } \n"
						+ "  public static void main(String[] args) {\n"
						+ "	printLowerRight(new Rectangle(new ColoredPoint(new Point(10, 4), Color.GREEN), new ColoredPoint(new Point(5, 4), Color.GREEN))); \n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(long x, long y) {}    \n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {} \n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
		},
		"Returns: 20");
	}
	public void test43() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "  @SuppressWarnings(\"preview\")\n"
						+ "  public static void printLowerRight(Object r) {\n"
						+ "    long res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(var x, long y), Color c), \n"
						+ "    		   				ColoredPoint lr) when x > \n"
						+ "								       switch(r) {\n"
						+ "								       	 case Rectangle(ColoredPoint c1,  ColoredPoint lr1)  -> 2;  \n"
						+ "								       	 default -> 10;   \n"
						+ "								       } \n"
						+ "								       	-> x + 10;  \n"
						+ "								       default -> 0;     \n"
						+ "    			};    \n"
						+ "    			System.out.println(\"Returns: \" + res);\n"
						+ "  } \n"
						+ "  public static void main(String[] args) {\n"
						+ "	printLowerRight(new Rectangle(new ColoredPoint(new Point(2, 4), Color.GREEN), new ColoredPoint(new Point(5, 4), Color.GREEN))); \n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}    \n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {} \n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
		},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Rectangle(ColoredPoint(Point(var x, long y), Color c), \n" +
			"	                                         ^^^^^^\n" +
			"Record component with type int is not compatible with type long\n" +
			"----------\n");
	}
	public void test44() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "  @SuppressWarnings(\"preview\")\n"
						+ "  public static void printLowerRight(Object r) {\n"
						+ "    long res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(var x, int y), Color c), \n"
						+ "    		   				ColoredPoint lr) when x > \n"
						+ "								       switch(r) {\n"
						+ "								       	 case Rectangle(ColoredPoint c1,  ColoredPoint lr1)  -> 2;  \n"
						+ "								       	 default -> 10;   \n"
						+ "								       } \n"
						+ "								       	-> x + 10;  \n"
						+ "								       default -> 0;     \n"
						+ "    			};    \n"
						+ "    			System.out.println(\"Returns: \" + res);\n"
						+ "  } \n"
						+ "  public static void main(String[] args) {\n"
						+ "	printLowerRight(new Rectangle(new ColoredPoint(new Point(2, 4), Color.GREEN), new ColoredPoint(new Point(5, 4), Color.GREEN))); \n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(long x, long y) {}    \n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {} \n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
		},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	case Rectangle(ColoredPoint(Point(var x, int y), Color c), \n" +
			"	                                         ^^^^^\n" +
			"Record component with type long is not compatible with type int\n" +
			"----------\n");
	}
	public void test45() {
		runNegativeTest(new String[] {
				"X.java",
						"public class X {\n"
						+ "	static void print(Object r) {\n"
						+ "		switch (r) {\n"
						+ "			case Rectangle(var a, var b) when (r instanceof Rectangle(ColoredPoint upperLeft2, ColoredPoint lowerRight)):\n"
						+ "				System.out.println(r);// error should not be reported here\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}\n"
						+ "record ColoredPoint() {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {} "
		},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	switch (r) {\n" +
			"	        ^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n");
	}
	public void test46() {
		runConformTest(new String[] {
			"X.java",
				"  @SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "  static void printGenericBoxString1(Box<Object> objectBox) {\n"
				+ "	  if (objectBox instanceof Box<Object>(String s)) {\n"
				+ "		  System.out.println(s); \n"
				+ "	  }\n"
				+ "  }\n"
				+ "static void printGenericBoxString2(Box<String> stringBox) {\n"
				+ "    if (stringBox instanceof Box<String>(var s)) {\n"
				+ "      System.out.println(s);\n"
				+ "    }\n"
				+ "  }\n"
				+ "public static void main(String[] args) {\n"
				+ "	printGenericBoxString1(new Box(\"Hello\"));\n"
				+ "	Object o = new Integer(10);\n"
				+ "	Box<Object> box = new Box(o);\n"
				+ "	printGenericBoxString1(box);\n"
				+ "}\n"
				+ "}\n"
				+ "record Box<T>(T t) {} "
			},
				"Hello");
	}
	public void test47() {
		runNegativeTest(new String[] {
			"X.java",
				"  @SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "  static void printGenericBoxString1(Box<Object> objectBox) {\n"
				+ "    if (objectBox instanceof Box<String>(String s)) {\n"
				+ "      System.out.println(s);\n"
				+ "    }\n"
				+ "  }\n"
				+ "  public static void main(String[] args) {}\n"
				+ "}\n"
				+ "record Box<T>(T t) {} "
			},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	if (objectBox instanceof Box<String>(String s)) {\n" +
				"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types Box<Object> and Box<String>\n" +
				"----------\n");
	}
	public void test48() {
		runConformTest(new String[] {
			"X.java",
				"  @SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "	public static void main(String[] args) {\n"
				+ "		erroneousTest1(new Box<>(\"A\"));\n"
				+ "		erroneousTest2(new Box<>(\"B\"));\n"
				+ "	}\n"
				+ "	static void erroneousTest1(Box<Object> bo) {\n"
				+ "		if (bo instanceof Box(var s)) {\n"
				+ "			System.out.println(\"I'm a box of \" + s.getClass().getName());\n"
				+ "		}\n"
				+ "	}\n"
				+ "	static void erroneousTest2(Box b) {\n"
				+ "		if (b instanceof Box(var t)) {\n"
				+ "			System.out.println(\"I'm a box of \" + t.getClass().getName());\n"
				+ "		}\n"
				+ "	}\n"
				+ "	record Box<T> (T t) {\n"
				+ "	}\n"
				+ "}"
			},
				"I\'m a box of java.lang.String\n" +
				"I\'m a box of java.lang.String");
	}
	public void testIssue690_1() {
		runNegativeTest(new String[] {
				"X.java",
					"public class X {\n"
					+ "	public void foo(Object s) {\n"
					+ "		switch (s) {\n"
					+ "			case R(Integer i1, Double i1) -> {}\n"
					+ "			case OuterR(R(Integer i1, Double i2), R(Integer i2, Double i2)) -> {}\n"
					+ "			default -> {}\n"
					+ "		}\n"
					+ "	}\n"
					+ "} \n"
					+ "record R(Integer i1, Double i2) {}\n"
					+ "record OuterR(R r1, R r2) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case R(Integer i1, Double i1) -> {}\n" +
				"	                          ^^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	case OuterR(R(Integer i1, Double i2), R(Integer i2, Double i2)) -> {}\n" +
				"	                                                ^^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	case OuterR(R(Integer i1, Double i2), R(Integer i2, Double i2)) -> {}\n" +
				"	                                                           ^^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n");
	}
	public void testIssue690_2() {
		runNegativeTest(new String[] {
				"X.java",
					"public class X {\n"
					+ "	public void foo(Object s) {\n"
					+ "		if (s instanceof OuterR(R(Integer i1, Double i2), R(Integer i3, Double i4))) { \n"
					+ "				System.out.println(\"IF\");\n"
					+ "		}\n"
					+ "		if (s instanceof OuterR(R(Integer i1, Double i2), R(Integer i1, Double i4))) { \n"
					+ "				System.out.println(\"SECOND IF\");\n"
					+ "		}\n"
					+ "	}\n"
					+ "} \n"
					+ "record R(Integer i1, Double i2) {}\n"
					+ "record OuterR(R r1, R r2) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	if (s instanceof OuterR(R(Integer i1, Double i2), R(Integer i1, Double i4))) { \n" +
				"	                                                            ^^\n" +
				"A pattern variable with the same name is already defined in the statement\n" +
				"----------\n");
	}

	public void testIssue691_1() {
		runNegativeTest(new String[] {
				"X.java",
					"public class X {\n"
					+ "	public void foo(Number s) {\n"
					+ "		switch (s) {\n"
					+ "			case R(Integer i1, Integer i2) -> {}\n"
					+ "			default -> {}\n"
					+ "		}\n"
					+ "	}\n"
					+ "	public void foo(Object o) {\n"
					+ "		switch (o) {\n"
					+ "			case R(Number i1, Integer i2) -> {}\n"
					+ "			default -> {}\n"
					+ "		}\n"
					+ "	}\n"
					+ "	public void bar(Object o) {\n"
					+ "		switch (o) {\n"
					+ "		case R(Integer i1, Integer i2)-> {}\n"
					+ "			default -> {}\n"
					+ "		}\n"
					+ "	}\n"
					+ "} \n"
					+ "record R(Integer i1, Integer i2) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case R(Integer i1, Integer i2) -> {}\n" +
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type mismatch: cannot convert from Number to R\n" +
				"----------\n");
	}
	public void testRemoveNamedRecordPatterns_001() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n" +
				" public static void foo(Rectangle r) {\n" +
				"   int res = switch (r) {\n" +
				"     case Rectangle(int x, int y) r -> 1;\n" +
				"     default -> 0;\n" +
				"   };\n" +
				"   System.out.println(res);\n" +
				" }\n" +
				" public static void main(String[] args) {\n" +
				"   foo(new Rectangle(10, 20));\n" +
				" }\n" +
				"}\n" +
				"record Rectangle(int x, int y) {\n" +
				"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case Rectangle(int x, int y) r -> 1;\n" +
				"	                           ^\n" +
				"Syntax error, insert \":\" to complete SwitchLabel\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	case Rectangle(int x, int y) r -> 1;\n" +
				"	                                  ^\n" +
				"Syntax error, insert \"AssignmentOperator Expression\" to complete Expression\n" +
				"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2004
	// [Patterns] ECJ generates suspect code for switching over patterns
	public void testIssue2004() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				record A() {}
					record R<T>(T t) {}
					public class X {
					    private static boolean foo(R<? super A> r) {
					       return switch(r) {
					            case R(var x) -> true;
					            default -> false;
					       };
					    }
					    public static void main(String argv[]) {
					       System.out.println(foo(new R<A>(null)));
					    }
				}
				"""
				},
				"true");
	}
	public void testRecordPatternTypeInference_001() {
		runNegativeTest(new String[] {
			"X.java",
			"import java.util.function.UnaryOperator;\n" +
			"record Mapper<T>(T in, T out) implements UnaryOperator<T> {\n" +
			"    public T apply(T arg) { return in.equals(arg) ? out : null; }\n" +
			"}\n" +
			"public class X {\n" +
			" void test(UnaryOperator<? extends CharSequence> op) {\n" +
			"     if (op instanceof Mapper(var in, var out)) {\n" +
			"         boolean shorter = out.length() < in.length();\n" +
			"     }\n" +
			" } \n" +
			" Zork();\n"+
			"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	Zork();\n" +
			"	^^^^^^\n" +
			"Return type for the method is missing\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	Zork();\n" +
			"	^^^^^^\n" +
			"This method requires a body instead of a semicolon\n" +
			"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1796
	// [Patterns] Record Patterns can cause VerifyError
	public void testGH1796() {
		runConformTest(new String[] {
			"X.java",
			"""
			public record X(int i) {

			  public static void main(String[] args) {
			    new Printer().print(new X(42), new StringBuilder());
			  }

			  static final class Printer {

			    private void print(X e, StringBuilder buffer) {
			      if (e instanceof X(int i)) {
			          System.out.println(i);
			      }
			    }
			  }
			}
			"""
			},
			"42");
	}
	public void testRecordPatternTypeInference_002() {
		runConformTest(new String[] {
			"X.java",
			"import java.util.function.UnaryOperator;\n" +
			"record Mapper<T>(T in) implements UnaryOperator<T> {\n" +
			"    public T apply(T arg) { return in.equals(arg) ? in : null; }\n" +
			"}\n" +
			"public class X {\n" +
			" @SuppressWarnings(\"preview\")\n" +
			" public static boolean test(UnaryOperator<? extends CharSequence> op) {\n" +
			"     if (op instanceof Mapper(var in)) {\n" +
			"         return in.length() > 0;\n" +
			"     }\n" +
			"   return false;\n" +
			" }\n" +
			" public static void main(String[] args) {\n" +
			"   Mapper<CharSequence> op = new Mapper<>(new String(\"abcd\"));\n" +
			"   System.out.println(test(op));\n" +
			" }\n" +
			"}"
			},
			"true");
	}
	public void testRecordPatternTypeInference_003() {
		runConformTest(new String[] {
			"X.java",
				"  @SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "	public static void main(String[] args) {\n"
				+ "		foo(new Box<>(\"B\"));\n"
				+ "	}\n"
				+ "	static void foo(Box b) {\n"
				+ "		if (b instanceof Box(var t)) {\n"
				+ "			System.out.println(\"I'm a box of \" + t.getClass().getName());\n"
				+ "		}\n"
				+ "	}\n"
				+ "	record Box<T> (T t) {\n"
				+ "	}\n"
				+ "}"
			},
				"I\'m a box of java.lang.String");
	}
	public void testRecordPatternTypeInference_004() {
		runConformTest(new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"interface I {int a();}\n" +
			"record RecB(int a) implements I {}\n" +
			"record R<T>(T a) {}\n" +
			"public class X {\n" +
			"    private static boolean test(List<R<? extends I>> list) {\n" +
			"        if (list.get(0) instanceof R(var a))\n" +
			"         return a.a() > 0;\n" +
			"        return false;\n" +
			"    }  \n" +
			"    public static void main(String... args) {\n" +
			"        List<R<? extends I>> list = new ArrayList<>();\n" +
			"        list.add(new R<>(new RecB(2)));\n" +
			"        System.out.println(test(list));\n" +
			"    }\n" +
			"}"
			},
			"true");
	}
	public void testRecordPatternTypeInference_005() {
		runConformTest(new String[] {
			"X.java",
			"interface I {int a();}\n" +
			"record RecB(int a) implements I {}\n" +
			"record R<T>(T a) {}\n" +
			"public class X {\n" +
			"    private static boolean test(R<? extends I> op) {\n" +
			"        if (op instanceof R(var a)) {\n" +
			"         return a.a() > 0;\n" +
			"        }\n" +
			"        return false;\n" +
			"    }  \n" +
			"    public static void main(String[] args) {\n" +
			"        R<? extends I> op = new R<>(new RecB(2));\n" +
			"        System.out.println(test(op));\n" +
			"    }\n" +
			"}"
			},
			"true");
	}
	public void testRecordPatternTypeInference_006() {
		runConformTest(new String[] {
			"X.java",
			"public class X {\n" +
			"     public static <P> boolean test(P p) {\n" +
			"         if (p instanceof R(var a)) {\n" +
			"              return a.len() > 0;\n" +
			"         }\n" +
			"         return false;\n" +
			"     }\n" +
			"     public static void main(String argv[]) {\n" +
			"         System.out.println(test(new R<>(new Y())));\n" +
			"     }\n" +
			"}\n" +
			"record R<T extends Y>(T a) {}\n" +
			"class Y {\n" +
			" public int len() { return 10;}\n" +
			"}"
			},
			"true");
	}
	public void testRecordPatternTypeInference_007() {
		runConformTest(new String[] {
			"X.java",
			"interface I {\n" +
			"   int a();\n" +
			"}\n" +
			"record R<T>(T a) {}\n" +
			"public class X {\n" +
			"    public static boolean test(R<?> p) {\n" +
			"        if (p instanceof R(var a)) {\n" +
			"             return a instanceof I;\n" +
			"        }\n" +
			"        return false; \n" +
			"    }\n" +
			"    public static void main(String argv[]) {\n" +
			"       System.out.println(test(new R<>((I) () -> 0)));\n" +
			"    }\n" +
			"}"
			},
			"true");
	}
	public void testRecordPatternTypeInference_008() {
		runConformTest(new String[] {
			"X.java",
			"interface I {int a();}\n" +
			"record R<T>(T a) {}\n" +
			"public class X {\n" +
			"    public static boolean test(R<I> p) {\n" +
			"        return switch (p) {\n" +
			"            case R(var a) -> a instanceof I;\n" +
			"            default ->  false;\n" +
			"        };\n" +
			"    }\n" +
			"    public static void main(String argv[]) {\n" +
			"       System.out.println(test(new R<>((I) () -> 0)));\n" +
			"    }\n" +
			"}"
			},
			"true");
	}
	public void testRecordPatternTypeInference_009() {
		runNegativeTest(new String[] {
				"X.java",
				"interface I {\n" +
				"   int a();\n" +
				"}\n" +
				"record R<T>(T a) {}\n" +
				"public class X {\n" +
				"    private static boolean test(R<? extends I> p) {\n" +
				"        if (p instanceof R(String a)) {\n" +
				"             return a instanceof String;\n" +
				"        }\n" +
				"        return true;\n" +
				"    }\n" +
				"    public static void main(String argv[]) {\n" +
				"        System.out.println(test(new R<>((I) () -> 0))); \n" +
				"    }\n" +
				"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	if (p instanceof R(String a)) {\n" +
				"	                   ^^^^^^^^\n" +
				"Record component with type capture#2-of ? extends I is not compatible with type String\n" +
				"----------\n");
	}
	public void testRecordPatternTypeInference_010() {
		runConformTest(new String[] {
				"X.java",
				"interface I {\n" +
				"   int a();\n" +
				"}\n" +
				"record R<T>(T a) {}\n" +
				"public class X {\n" +
				"    private static boolean test(R<?> p) {\n" +
				"        if (p instanceof R(String a)) {\n" +
				"             return a instanceof String;\n" +
				"        }\n" +
				"        return true;\n" +
				"    }\n" +
				"    public static void main(String argv[]) {\n" +
				"        System.out.println(test(new R<>((I) () -> 0))); \n" +
				"    }\n" +
				"}"
				},
				"true");
	}
	public void testRecordPatternTypeInference_011() {
		runNegativeTest(new String[] {
				"X.java",
				"interface I {\n" +
				"   int a();\n" +
				"}\n" +
				"\n" +
				"record R<T>(T a) {}\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"    private static boolean test(R<? extends I> p) {\n" +
				"        if (p instanceof R<>(String a)) {\n" +
				"             return a instanceof String;\n" +
				"        }\n" +
				"        return true;\n" +
				"    }\n" +
				"\n" +
				"    public static void main(String argv[]) {\n" +
				"        System.out.println(test(new R<>((I) () -> 0))); \n" +
				"    }\n" +
				"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	if (p instanceof R<>(String a)) {\n" +
				"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types R<capture#1-of ? extends I> and R\n" +
				"----------\n");
	}
	public void testIssue900_1() {
		runConformTest(new String[] {
				"X.java",
				"class X {\n"
				+ "	record Box<T>(T t) {}\n"
				+ "	// no issues\n"
				+ "	static void test1(Box<String> bo) {\n"
				+ "		if (bo instanceof Box<String>(var s)) {\n"
				+ "			System.out.println(\"String \" + s);\n"
				+ "		}\n"
				+ "	}\n"
				+ "	// no issues\n"
				+ "	static void test2(Box<String> bo) {\n"
				+ "	    if (bo instanceof Box(var s)) {    // Inferred to be Box<String>(var s)\n"
				+ "	        System.out.println(\"String \" + s);\n"
				+ "	    }\n"
				+ "	}\n"
				+ "	// \"Errors occurred during the build\": \"info cannot be null\"\n"
				+ "	static void test3(Box<Box<String>> bo) {\n"
				+ "	    if (bo instanceof Box<Box<String>>(Box(var s))) {        \n"
				+ "	        System.out.println(\"String \" + s.getClass().toString());\n"
				+ "	    }\n"
				+ "	}    \n"
				+ "	// \"Errors occurred during the build\": \"info cannot be null\"\n"
				+ "	static void test4(Box<Box<String>> bo) {\n"
				+ "	    if (bo instanceof Box(Box(var s))) {    \n"
				+ "	        System.out.println(\"String \" + s);\n"
				+ "	    }\n"
				+ "	}\n"
				+ "	public static void main(String[] args) {\n"
				+ "		Box<Box<String>> bo = new Box(new Box(\"\"));\n"
				+ "		test3(bo);\n"
				+ "	}\n"
				+ "}"
				},
				"String class java.lang.String");
	}
	// The following code is accepted by ECJ, but it should really reject the code
	// at Box(String s1, String s2)
	public void _testIssue900_2() {
		runNegativeTest(new String[] {
				"X.java",
				"class X {\n"
				+ "	record Box<T, U>(T t1, U t2) {}\n"
				+ "	static void test3(Box<Box<String, Integer>, Box<Integer, String>> bo) {\n"
				+ "	    if (bo instanceof Box<Box<String, Integer>, Box<Integer, String>>(Box(String s1, String s2), Box b1)) {        \n"
				+ "	        System.out.println(\"String \" + s1.getClass().toString());\n"
				+ "	    }\n"
				+ "	}    \n"
				+ "	public static void main(String[] args) {\n"
				+ "		Box<Box<String, Integer>, Box<Integer, String>> bo = new Box(new Box(\"\", Integer.valueOf(0)), new Box(Integer.valueOf(0), \"\"));  \n"
				+ "		test3(bo);\n"
				+ "	}\n"
				+ "}"
				},
				"");
	}
	public void testIssue900_3() {
		Map<String,String> options = getCompilerOptions(false);
		String old1 = options.get(CompilerOptions.OPTION_ReportRawTypeReference);
		String old2 = options.get(CompilerOptions.OPTION_ReportUncheckedTypeOperation);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
		try {
			runNegativeTest(new String[] {
					"X.java",
					"class X {\n"
							+ "	record Box<T, U>(T t, U u) {}\n"
							+ "	static void test3(Box<Box<String>> bo) {\n"
							+ "	    if (bo instanceof Box<Box<String>>(Box(var s1, String s2), Box b1)) {        \n"
							+ "	        System.out.println(\"String \" + s1.getClass().toString());\n"
							+ "	    }\n"
							+ "	}    \n"
							+ "	public static void main(String[] args) {\n"
							+ "		Box<Box<String, Integer>, Box<Integer, String>> bo = new Box(new Box(\"\", Integer.valueOf(0)), new Box(Integer.valueOf(0), \"\"));\n"
							+ "		test3(bo);\n"
							+ "	}\n"
							+ "}"
			},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	static void test3(Box<Box<String>> bo) {\n" +
				"	                      ^^^\n" +
				"Incorrect number of arguments for type X.Box<T,U>; it cannot be parameterized with arguments <String>\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	if (bo instanceof Box<Box<String>>(Box(var s1, String s2), Box b1)) {        \n" +
				"	                      ^^^\n" +
				"Incorrect number of arguments for type X.Box<T,U>; it cannot be parameterized with arguments <String>\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 10)\n" +
				"	test3(bo);\n" +
				"	^^^^^\n" +
				"The method test3(X.Box<X.Box<String,Integer>,X.Box<Integer,String>>) is undefined for the type X\n" +
				"----------\n",
				"",
				null,
				false,
				options);
		} finally {
			options.put(CompilerOptions.OPTION_ReportRawTypeReference, old1);
			options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, old2);
		}
	}
	public void testRecordPatternMatchException_001() {
		runConformTest(new String[] {
				"X.java",
				"public class X  {\n"+
				"\n"+
				"    public record R(int x) {\n"+
				"        public int x() {\n"+
				"         return x < 10 ? 10/x : x;\n"+
				"        }\n"+
				"    }\n"+
				"\n"+
				"    @SuppressWarnings(\"preview\")\n"+
				" private static int foo(Object o) {\n"+
				"        int ret = -1;\n"+
				"        try {\n"+
				"            if (o instanceof R(int x)) {\n"+
				"                ret = 100;\n"+
				"            }\n"+
				"        } catch (MatchException e) {\n"+
				"            ret += 100;\n"+
				"        }\n"+
				"          return ret;\n"+
				"    } \n"+
				"    public static void main(String argv[]) { \n"+
				"        System.out.println(X.foo(new R(0))); \n"+
				"    } \n"+
				"}"
				},
				"99");
	}
	public void testRecordPatternMatchException_001_1() {
		runConformTest(new String[] {
				"X.java",
				"public class X  {\n"+
				"\n"+
				"    public record R(int x) {\n"+
				"        public int x() {\n"+
				"         return x < 10 ? 10/x : x;\n"+
				"        }\n"+
				"    }\n"+
				"\n"+
				"    @SuppressWarnings(\"preview\")\n"+
				" private static int foo(Object o) {\n"+
				"        int ret = -1;\n"+
				"        try {\n"+
				"            if (o instanceof R(int x)) {\n"+
				"                ret = 100;\n"+
				"            }\n"+
				"            return 10;\n" +
				"        } catch (MatchException e) {\n"+
				"            ret += 100;\n"+
				"        }\n"+
				"          return ret;\n"+
				"    } \n"+
				"    public static void main(String argv[]) { \n"+
				"        System.out.println(X.foo(new R(0))); \n"+
				"    } \n"+
				"}"
				},
				"99");
	}
	public void testRecordPatternMatchException_002() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				" public record R1(int x) {\n" +
				" }\n" +
				"\n" +
				" @SuppressWarnings(\"preview\")\n" +
				" public static int bar(Object o) {\n" +
				"   int res = 100;\n" +
				"   if (o instanceof R1(int x)) {\n" +
				"     res = x;\n" +
				"   }\n" +
				"     return res; \n" +
				" }            \n" +
				"\n" +
				" public static void main(String argv[]) {\n" +
				"   R1 r = new R1(0);\n" +
				"   int result = bar(r);   \n" +
				"   System.out.println(result);  \n" +
				" }\n" +
				"}"
				},
				"0");
	}
	public void testRecordPatternMatchException_003() {
		runConformTest(new String[] {
				"X.java",
				"record R(int x) {}\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" private int foo(Object o) {\n"+
				"   int ret = 10;\n"+
				"   try {\n"+
				"     if (o instanceof R(int x)) {\n"+
				"       ret = x;\n"+
				"     }\n"+
				"   } catch (MatchException e) {\n"+
				"     ret = -1;\n"+
				"   }\n"+
				"   return ret;\n"+
				" }\n"+
				"\n"+
				" public static void main(String argv[]) {\n"+
				"   int res = new X().foo(new R(100));\n"+
				"   System.out.println(res);\n"+
				" }\n"+
				"}"
				},
				"100");
	}
	public void testRecordPatternMatchException_004() {
		runConformTest(new String[] {
				"X.java",
				"record R(int x) {}\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" private static int foo(Object o) {\n"+
				"   int ret = 10;\n"+
				"   try {\n"+
				"     if (o instanceof R(int x)) {\n"+
				"       ret = x;\n"+
				"     }\n"+
				"   } catch (MatchException e) {\n"+
				"     ret = -1;\n"+
				"   }\n"+
				"   return ret;\n"+
				" }\n"+
				"\n"+
				" public static void main(String argv[]) {\n"+
				"   int res = foo(new R(100));\n"+
				"   System.out.println(res);\n"+
				" }\n"+
				"}"
				},
				"100");
	}
	public void testRecordPatternMatchException_005() {
		runConformTest(new String[] {
				"X.java",
				" public class X {\n" +
				"\n" +
				" public record R1(int x) {}\n" +
				"\n" +
				" public record R2(int x) {}\n" +
				"\n" +
				" public static void main(String argv[]) {\n" +
				"   R1 r = new R1(0);\n" +
				"   try {\n" +
				"     if (r instanceof R1(int x)) {\n" +
				"       System.out.println(\"matched\");\n" +
				"     }\n" +
				"   } catch (MatchException e) {\n" +
				"     System.out.println(\"caught exception\");\n" +
				"   }\n" +
				"\n" +
				"   if (r instanceof R1(int x)) {\n" +
				"     System.out.println(\"hello    \");\n" +
				"   }\n" +
				"\n" +
				"   System.out.println(\"done\");\n" +
				" }\n" +
				"}"
				},
				"matched\n" +
				"hello    \n" +
				"done");
	}
	public void testRecordPatternMatchException_006() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				" public record R1(int x) {\n" +
				"   public int x(){\n" +
				"     return x < 10 ? 10 / x : x;\n" +
				"   }\n" +
				" }\n" +
				" public record R2(R1 r1) {\n" +
				"   \n" +
				" }\n" +
				"\n" +
				" @SuppressWarnings(\"preview\")\n" +
				" public static int bar(Object o) {\n" +
				"   int res = 100;\n" +
				"   if (o instanceof R2(R1 r1)) {\n" +
				"     res = r1.x();\n" +
				"   }\n" +
				"   System.out.print(false);\n" +
				"     return res; \n" +
				" }            \n" +
				"\n" +
				" public static void main(String argv[]) {\n" +
				"   R1 r = new R1(0);\n" +
				"   int result = bar(r);   \n" +
				"   System.out.println(result);  \n" +
				" }      \n" +
				"}"
				},
				"false100");
	}
	public void testRecordPatternMatchException_007() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				" public record R1(int x) {\n" +
				" }\n" +
				" public record R2(R1 r1) {\n" +
				"   \n" +
				" }\n" +
				"\n" +
				" @SuppressWarnings(\"preview\")\n" +
				" public static int bar(Object o) {\n" +
				"   int res = 100;\n" +
				"   if (o instanceof R2(R1 r1)) {\n" +
				"     res = r1.x();\n" +
				"   }\n" +
				"     return res; \n" +
				" }\n" +
				"\n" +
				" public static void main(String argv[]) {\n" +
				"   R1 r = new R1(0);\n" +
				"   int result = bar(r);   \n" +
				"   System.out.println(result);  \n" +
				" }      \n" +
				"}"
				},
				"100");
	}
	public void testRecordPatternMatchException_008() {
		runConformTest(new String[] {
				"X.java",
				"record R(Integer a) {\n" +
				"    static R newRecord() {\n" +
				"        return new R(5);\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"public class X  {\n" +
				"\n" +
				"    @SuppressWarnings(\"preview\")\n" +
				"       private int test(Object o) {\n" +
				"        int ret = 0;\n" +
				"        try {\n" +
				"            switch (o) {\n" +
				"                case R(Integer a) -> ret =  a;\n" +
				"                default -> ret =  8;\n" +
				"            }\n" +
				"        } catch (MatchException ex) {\n" +
				"            ret = -1;\n" +
				"        }\n" +
				"        return ret;\n" +
				"    } \n" +
				"\n" +
				"    public static void main(String argv[]) {\n" +
				"        X test = new X();\n" +
				"        int res = test.test(R.newRecord());\n" +
				"        System.out.println(res);\n" +
				"} \n" +
				"}"
				},
				"5");
	}
	public void testRecordPatternMatchException_009() {
		runConformTest(new String[] {
				"X.java",
				"record R(Y s) {}\n"+
				"class Y{}\n"+
				"public class X  extends Y{\n"+
				"\n"+
				"    @SuppressWarnings({ \"preview\", \"unused\" })\n"+
				" public boolean foo(R r) {\n"+
				"        boolean ret = false; // keep this unused variable to see the error. \n"+
				"        switch (r) {\n"+
				"            case R(X s) : {\n"+
				"             return true;\n"+
				"            }\n"+
				"            default : {\n"+
				"                return false;\n"+
				"            }\n"+
				"        }\n"+
				"    }  \n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"        X x = new X();\n"+
				"        System.out.println(x.foo(new R(x)));\n"+
				"    }\n"+
				"}"

				},
				"true");
	}
	public void testIssue1224_1() {
		runNegativeTest(new String[] {
			"X.java",
			"interface I {}\n"
			+ "class Class implements I {}\n"
			+ "record Record(I s) {}\n"
			+ "public class X {\n"
			+ " @SuppressWarnings(\"preview\")\n"
			+ "    public static void foo(Record exp) {\n"
			+ "        switch (exp) {\n"
			+ "            case Record(Class s) -> {break;}\n"
			+ "        }\n"
			+ "    }\n"
			+ "}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	switch (exp) {\n" +
			"	        ^^^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n");
	}
	public void testIssue1224_2() {
		runConformTest(new String[] {
			"X.java",
			"interface I {}\n"
			+ "class Class implements I {}\n"
			+ "record Record(I s) {}\n"
			+ "public class X {\n"
			+ " @SuppressWarnings(\"preview\")\n"
			+ "  public static void foo(Record exp) {\n"
			+ "    switch (exp) {\n"
			+ "      case Record(I s) -> {break;}\n"
			+ "    }\n"
			+ "  }\n"
			+ "	public static void main(String[] args) {\n"
			+ "		foo(new Record(new Class()));\n"
			+ "	}\n"
			+ "}"
			},
			"");
	}
	public void testIssue1224_3() {
		runNegativeTest(new String[] {
			"X.java",
			"interface I {}\n"
			+ "record Record(long l) {}\n"
			+ "public class X {\n"
			+ " @SuppressWarnings(\"preview\")\n"
			+ "    public void foo(Record exp) {\n"
			+ "        switch (exp) {\n"
			+ "            case Record(int i) -> {break;}\n"
			+ "        }\n"
			+ "    }\n"
			+ "}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	switch (exp) {\n" +
			"	        ^^^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	case Record(int i) -> {break;}\n" +
			"	            ^^^^^\n" +
			"Record component with type long is not compatible with type int\n" +
			"----------\n");
	}
	public void testIssue1224_4() {
		runNegativeTest(new String[] {
			"X.java",
			"record Record<T>(Object o, T x){}\n"
			+ "public class X {\n"
			+ " @SuppressWarnings(\"preview\")\n"
			+ "    public static void foo(Record<String> rec) {\n"
			+ "        switch (rec) {\n"
			+ "            case Record<String>(Object o, StringBuilder s) -> {break;}\n"
			+ "        }\n"
			+ "    }\n"
			+ "}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	switch (rec) {\n" +
			"	        ^^^\n" +
			"An enhanced switch statement should be exhaustive; a default label expected\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	case Record<String>(Object o, StringBuilder s) -> {break;}\n" +
			"	                              ^^^^^^^^^^^^^^^\n" +
			"Record component with type String is not compatible with type StringBuilder\n" +
			"----------\n");
	}
	public void testIssue1224_5() {
		runConformTest(new String[] {
			"X.java",
			"record Record<T>(Object o, T x){}\n"
			+ "public class X {\n"
			+ " @SuppressWarnings(\"preview\")\n"
			+ "    public static void foo(Record<String> rec) {\n"
			+ "        switch (rec) {\n"
			+ "            case Record<String>(Object o, String s) -> {"
			+ "                System.out.println(s);"
			+ "                break;"
			+ "            }\n"
			+ "        }\n"
			+ "    }\n"
			+ "	public static void main(String[] args) {\n"
			+ "		foo(new Record<String>(args, \"PASS\"));\n"
			+ "	}\n"
			+ "}"
			},
			"PASS");
	}
	public void testIssue1224_6() {
		runConformTest(new String[] {
			"X.java",
			"record Record(String s){}\n"
			+ "public class X {\n"
			+ " @SuppressWarnings(\"preview\")\n"
			+ "    public static void foo(Record rec) {\n"
			+ "        switch (rec) {\n"
			+ "            case Record(String s) when true -> {"
			+ "                System.out.println(s);"
			+ "                break;"
			+ "            }\n"
			+ "            default -> {}"
			+ "        }\n"
			+ "    }\n"
			+ "	public static void main(String[] args) {\n"
			+ "		foo(new Record(\"PASS\"));\n"
			+ "	}\n"
			+ "}"
			},
				"PASS");
	}
	public void testIssue1224_7() {
		runConformTest(new String[] {
			"X.java",
			"interface I<T> {\n"
			+ "    T a();\n"
			+ "}\n"
			+ "record Record<T>(T a, T b) implements I<T> {}\n"
			+ "public class X {\n"
			+ "	public static void main(String[] args) {\n"
			+ "		foo(new Record(2, 3));\n"
			+ "	}\n"
			+ "	static void foo(I i) {\n"
			+ "        int res = 0;\n"
			+ "        switch (i) {\n"
			+ "            case Record(Integer a, Integer b) -> {\n"
			+ "                res = a + b;\n"
			+ "                break;\n"
			+ "            }\n"
			+ "            default -> {\n"
			+ "                res = 0;\n"
			+ "                break;\n"
			+ "            }\n"
			+ "        }\n"
			+ "		System.out.println(res);\n"
			+ "    }\n"
			+ "}"
			},
				"5");
	}
	// Fails with VerifyError since we allow the switch now but don't
	// generate a label/action for implicit default.
	public void testIssue1224_8() {
		runConformTest(new String[] {
			"X.java",
			"record Record(int a) {}\n"
			+ "public class X {\n"
			+ " @SuppressWarnings(\"preview\")\n"
			+ "  public boolean foo(Record rec) {\n"
			+ "        boolean res = switch (rec) {\n"
			+ "            case Record(int a) : {\n"
			+ "                yield a == 0; \n"
			+ "            }\n"
			+ "        };\n"
			+ "        return res;\n"
			+ "    }\n"
			+ "    public static void main(String argv[]) {\n"
			+ "        X t = new X();\n"
			+ "        if (t.foo(new Record(0))) {\n"
			+ "            System.out.println(\"SUCCESS\");\n"
			+ "            return;\n"
			+ "        }\n"
			+ "        System.out.println(\"FAIL\");\n"
			+ "    }\n"
			+ "}"
			},
			"SUCCESS");
	}
	public void testRecPatExhaust001() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I permits A, B, C {}\n"+
				"final class A   implements I {}\n"+
				"final class B   implements I {}\n"+
				"final class C implements I {}\n"+
				"record Box(I i) {}\n"+
				"\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" public static int testExhaustiveRecordPatterns(Box box) {\n"+
				"     return switch (box) {     // Exhaustive!\n"+
				"         case Box(A a) -> 0;\n"+
				"         case Box(B b) -> 1;\n"+
				"         case Box(C c) -> 2;\n"+
				"    };\n"+
				" } \n"+
				" \n"+
				"    public static void main(String argv[]) {\n"+
				"     Box b = new Box(new A());\n"+
				"        System.out.println(testExhaustiveRecordPatterns(b));\n"+
				"    }\n"+
				"}",
			},
			"0");
	}
	public void testRecPatExhaust002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I permits A, B, C {}\n"+
				"final class A   implements I {}\n"+
				"final class B   implements I {}\n"+
				"final class C implements I {}\n"+
				"record Box(I i) {}\n"+
				"\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" public static int testExhaustiveRecordPatterns(Box box) {\n"+
				"     return switch (box) {     // Not Exhaustive!\n"+
				"         case Box(A a) -> 0;\n"+
				"         case Box(B b) -> 1;\n"+
				"    };\n"+
				" } \n"+
				" \n"+
				"    public static void main(String argv[]) {\n"+
				"     Box b = new Box(new A());\n"+
				"        System.out.println(testExhaustiveRecordPatterns(b));\n"+
				"    }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	return switch (box) {     // Not Exhaustive!\n" +
			"	               ^^^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	public void testRecPatExhaust003() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I permits A, B, C {}\n"+
				"final class A   implements I {}\n"+
				"final class B   implements I {}\n"+
				"final class C implements I {}\n"+
				"sealed interface J permits D, E, F {}\n"+
				"final class D   implements J {}\n"+
				"final class E   implements J {}\n"+
				"final class F implements J {}\n"+
				"record Box(I i, J j) {}\n"+
				"\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" public static int testExhaustiveRecordPatterns(Box box) {\n"+
				"     return switch (box) {     // Not Exhaustive!\n"+
				"         case Box(A a, D d) -> 0;\n"+
				"         case Box(A a, E e) -> 10;\n"+
				"         case Box(A a, F f) -> 20;\n"+
				"         case Box(B b, D d) -> 1;\n"+
				"         case Box(B b, E e) -> 11;\n"+
				"         case Box(B b, F f) -> 21;\n"+
				"         case Box(C c, D d) -> 2;\n"+
				"         case Box(C c, E e) -> 12;\n"+
				"         case Box(C c, F f) -> 22;\n"+
				"    };\n"+
				" } \n"+
				" \n"+
				"    public static void main(String argv[]) {\n"+
				"     Box b = new Box(new A(), new D());\n"+
				"        System.out.println(testExhaustiveRecordPatterns(b));\n"+
				"    }\n"+
				"}",
			},
			"0");
	}
	public void testRecPatExhaust004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I permits A, B, C {}\n"+
				"final class A   implements I {}\n"+
				"final class B   implements I {}\n"+
				"final class C implements I {}\n"+
				"sealed interface J permits D, E, F {}\n"+
				"final class D   implements J {}\n"+
				"final class E   implements J {}\n"+
				"final class F implements J {}\n"+
				"record Box(I i, J j) {}\n"+
				"\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" public static int testExhaustiveRecordPatterns(Box box) {\n"+
				"     return switch (box) {     // Not Exhaustive!\n"+
				"         case Box(A a, D d) -> 0;\n"+
				"         case Box(A a, E e) -> 0;\n"+
				"         case Box(A a, F f) -> 0;\n"+
				"         case Box(B b, D d) -> 1;\n"+
				"         case Box(B b, E e) -> 1;\n"+
				"         case Box(B b, F f) -> 1;\n"+
				"         case Box(C c, D d) -> 2;\n"+
				"         case Box(C c, F f) -> 2;\n"+
				"    };\n"+
				" } \n"+
				" \n"+
				"    public static void main(String argv[]) {\n"+
				"     Box b = new Box(new A(), new D());\n"+
				"        System.out.println(testExhaustiveRecordPatterns(b));\n"+
				"    }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 16)\n" +
			"	return switch (box) {     // Not Exhaustive!\n" +
			"	               ^^^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	public void testRecPatExhaust005() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I permits A, B, C {}\n"+
				"final class A   implements I {}\n"+
				"final class B   implements I {}\n"+
				"record C(int j) implements I {}  // Implicitly final\n"+
				"record Box(I i) {}\n"+
				"\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" public static int testExhaustiveRecordPatterns(Box box) {\n"+
				"     return switch (box) {     // Exhaustive!\n"+
				"         case Box(A a) -> 0;\n"+
				"         case Box(B b) -> 1;\n"+
				"         case Box(C c) -> 2;\n"+
				"    };\n"+
				" } \n"+
				" \n"+
				"    public static void main(String argv[]) {\n"+
				"     Box b = new Box(new A());\n"+
				"        System.out.println(testExhaustiveRecordPatterns(b));\n"+
				"    }\n"+
				"}",
			},
			"0");
	}
	public void testRecPatExhaust006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I permits A, B, C {}\n"+
				"final class A   implements I {}\n"+
				"final class B   implements I {}\n"+
				"record C(int j) implements I {}  // Implicitly final\n"+
				"record Box(I i) {}\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" public static int testExhaustiveRecordPatterns(Box box) {\n"+
				"     return switch (box) {     // Not Exhaustive!\n"+
				"         case Box(A a) -> 0;\n"+
				"         case Box(B b) -> 1;\n"+
				"    };\n"+
				" } \n"+
				" \n"+
				"    public static void main(String argv[]) {\n"+
				"     Box b = new Box(new A());\n"+
				"        System.out.println(testExhaustiveRecordPatterns(b));\n"+
				"    }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	return switch (box) {     // Not Exhaustive!\n" +
			"	               ^^^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	public void testRecPatExhaust007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I permits A, B, C {}\n"+
				"final class A   implements I {}\n"+
				"final class B   implements I {}\n"+
				"record C(int j) implements I {}  // Implicitly final\n"+
				"record R(I i, I j) {}\n"+
				"\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" private static int testNonExhaustiveRecordPatterns(R p) {\n"+
				"     return switch (p) {     // Not Exhaustive!\n"+
				"         case R(A a1, A a2) -> 0;\n"+
				"         case R(B b1, B b2) -> 1;\n"+
				"    };\n"+
				" } \n"+
				"    public static void main(String argv[]) {\n"+
				"     R b = new R(new A(), new B());\n"+
				"        System.out.println(testNonExhaustiveRecordPatterns(b));\n"+
				"    }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	return switch (p) {     // Not Exhaustive!\n" +
			"	               ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	public void testRecPatExhaust008() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I permits A, B, C {}\n"+
				"final class A   implements I {}\n"+
				"final class B   implements I {}\n"+
				"record C(int j) implements I {}  // Implicitly final\n"+
				"record R(I i, I j) {}\n"+
				"\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" private static int testNonExhaustiveRecordPatterns(R p) {\n"+
				"     return switch (p) {     // Not Exhaustive!\n"+
				"         case R(A a1, A a2) -> 0;\n"+
				"         case R(B b1, B b2) -> 1;\n"+
				"         case R(C c1, C c2) -> 2;\n"+
				"    };\n"+
				" } \n"+
				"    public static void main(String argv[]) {\n"+
				"     R b = new R(new A(), new B());\n"+
				"        System.out.println(testNonExhaustiveRecordPatterns(b));\n"+
				"    }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	return switch (p) {     // Not Exhaustive!\n" +
			"	               ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	public void testRecPatExhaust009() {
		runConformTest(
			new String[] {
				"X.java",
				"record Test<T>(Object o, T x) {}\n"+
				"\n"+
				"public class X {\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" static int testExhaustiveRecordPattern(Test<String> r) {\n"+
				"   return switch (r) { // Exhaustive!\n"+
				"   case Test<String>(Object o, String s) -> 0;\n"+
				"   };\n"+
				" }\n"+
				"\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(testExhaustiveRecordPattern(new Test<String>(args, null)));\n"+
				" }\n"+
				"}"
			},
			"0");
	}
	public void testRecPatExhaust010() {
		runConformTest(
			new String[] {
				"X.java",
				"record R(Object t, Object u) {}\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				"    public static int foo(R r) {\n"+
				"        return \n"+
				"          switch (r) {\n"+
				"            case R(String x, Integer y) -> 1;\n"+
				"            case R(Object x, Integer y) -> 2;\n"+
				"            case R(Object x, Object y) -> 42;\n"+
				"        };\n"+
				"    }\n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"     System.out.println(foo(new R(new String(), new Object())));\n"+
				"    }\n"+
				"}"
			},
			"42");
	}
	// implicit permitted - interface
	public void testRecPatExhaust011() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I {}\n"+
				"\n"+
				"final class A implements I {}\n"+
				"final class B implements I {}\n"+
				"\n"+
				"record R(I d) {}\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				"    @SuppressWarnings(\"preview\")\n"+
				"       public static int foo(R r) {\n"+
				"        return switch (r) {\n"+
				"            case R(A x) -> 1;\n"+
				"        };\n"+
				"    } \n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"       System.out.println(X.foo(new R(new A())));\n"+
				"    } \n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	return switch (r) {\n" +
			"	               ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	// implicit permitted - class
	public void testRecPatExhaust012() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed class C {}\n"+
				"\n"+
				"final class A extends C {}\n"+
				"final class B extends C {}\n"+
				"\n"+
				"record R(C c) {}\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				"    @SuppressWarnings(\"preview\")\n"+
				"       public static int foo(R r) {\n"+
				"        return switch (r) {\n"+
				"            case R(A x) -> 1;\n"+
				"        };\n"+
				"    } \n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"       System.out.println(X.foo(new R(new A())));\n"+
				"    } \n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	return switch (r) {\n" +
			"	               ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	// implicit permitted - class - the class C missing
	public void testRecPatExhaust013() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed class C {}\n"+
				"\n"+
				"final class A extends C {}\n"+
				"final class B extends C {}\n"+
				"\n"+
				"record R(C c) {}\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				"    @SuppressWarnings(\"preview\")\n"+
				"       public static int foo(R r) {\n"+
				"        return switch (r) {\n"+
				"            case R(A x) -> 1;\n"+
				"            case R(B x) -> 1;\n"+
				"        };\n"+
				"    } \n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"       System.out.println(X.foo(new R(new A())));\n"+
				"    } \n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	return switch (r) {\n" +
			"	               ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	public void testRecPatExhaust014() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I {}\n"+
				"sealed class C {}\n"+
				"final class A extends C implements I {}\n"+
				"final class B extends C implements I {}\n"+
				"\n"+
				"record R(C c, I i){}\n"+
				"public class X { \n"+
				"\n"+
				" @SuppressWarnings(\"preview\")\n"+
				" public static int foo(R r) {\n"+
				"       return switch (r) {\n"+
				"            case R(A x, A y) -> 1;\n"+
				"            case R(A x, B y) -> 42;\n"+
				"            case R(B x, A y)-> 3;\n"+
				"            case R(B x, B y)-> 4;\n"+
				"        };\n"+
				"    }\n"+
				"    public static void main(String argv[]) {\n"+
				"     System.out.println(X.foo(new R(new A(), new B())));\n"+
				"    }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	return switch (r) {\n" +
			"	               ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	public void testRecPatExhaust015() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I {}\n"+
				"sealed class C {}\n"+
				"final class A extends C implements I {}\n"+
				"final class B extends C implements I {}\n"+
				"\n"+
				"record R(C c, I i){}\n"+
				"public class X { \n"+
				"\n"+
				"    @SuppressWarnings(\"preview\")\n"+
				" public static int foo(R r) {\n"+
				"       return switch (r) {\n"+
				"            case R(A x, A y) -> 1;\n"+
				"            case R(A x, B y) -> 42;\n"+
				"            case R(B x, A y) -> 3;\n"+
				"            case R(B x, B y) -> 4;\n"+
				"            case R(C x, A y) -> 5;\n"+
				"            case R(C x, B y) -> 6;\n"+
				"       };\n"+
				"    }\n"+
				"    public static void main(String argv[]) {\n"+
				"     System.out.println(X.foo(new R(new A(), new B())));\n"+
				"    }\n"+
				"}",
			},
			"42");
	}
	public void testRecPatExhaust016() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed abstract class C permits A, B {}\n"+
				"final class A extends C {}\n"+
				"final class B extends C {}\n"+
				"record R(C x, A y) {}\n"+
				"\n"+
				"public class X {\n"+
				"    public static int foo(R r) {\n"+
				"        return switch (r) {\n"+
				"            case R(A x, A y) -> 42;\n"+
				"            case R(B y, A x) -> 2;\n"+
				"        };\n"+
				"    }\n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"       System.out.println(X.foo(new R(new A(), new A())));\n"+
				"    }\n"+
				"}",
			},
			"42");
	}
	public void testRecPatExhaust017() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I permits A, B {}\n"+
				"sealed interface B extends I {}\n"+
				"\n"+
				"final class A implements I {}\n"+
				"\n"+
				"record R1() implements B {}\n"+
				"record R2(I i) {}\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				"    public static int foo(R2 r) {\n"+
				"        return switch (r) {\n"+
				"            case R2(A a) -> 42;\n"+
				"            case R2(B a) -> 1;\n"+
				"        };\n"+
				"    }\n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"        System.out.println(X.foo(new R2(new A())));;\n"+
				"    }\n"+
				"\n"+
				"}",
			},
			"42");
	}
	public void testRecPatExhaust018() {
		runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I permits A, B {}\n"+
				"sealed interface B extends I {}\n"+
				"\n"+
				"final class A implements I {}\n"+
				"\n"+
				"record R1() implements B {}\n"+
				"record R2(I i) {}\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				"    public static int foo(R2 r) {\n"+
				"        return switch (r) {\n"+
				"            case R2(A a) -> 42;\n"+
				"        };\n"+
				"    }\n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"        System.out.println(X.foo(new R2(new A())));;\n"+
				"    }\n"+
				"\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	return switch (r) {\n" +
			"	               ^\n" +
			"A switch expression should have a default case\n" +
			"----------\n");
	}
	public void testRecordPatternTypeInference_012() {
		runConformTest(
			new String[] {
				"X.java",
				"interface I {}\n"+
				"record R<T>(T t) {}\n"+
				"\n"+
				"public final class X implements I {\n"+
				"\n"+
				"    private static boolean test(R<? extends I> r) {\n"+
				"        if (r instanceof R(X x)) {\n"+
				"             return (x instanceof X);\n"+
				"        }\n"+
				"        return true;\n"+
				"    }\n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"        System.out.println(test(new R<>(null)));\n"+
				"    }\n"+
				"}",
			},
			"true");
	}
	public void testRecordPatternTypeInference_013() {
		runConformTest(
			new String[] {
				"X.java",
				"interface I {}\n"+
				"record R<T>(T t) {}\n"+
				"\n"+
				"public class X implements I {\n"+
				"\n"+
				"    private static boolean test(R<? extends I> r) {\n"+
				"        if (r instanceof R(X x)) {\n"+
				"             return (x instanceof X);\n"+
				"        }\n"+
				"        return true;\n"+
				"    }\n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"        System.out.println(test(new R<>(null)));\n"+
				"    }\n"+
				"}",
			},
			"true");
	}

	// a subclass of X could implement I - positive test case
	public void testRecordPatternTypeInference_014() {
		runConformTest(
			new String[] {
				"X.java",
				"interface I {}\n"+
				"record R<T>(T t) {}\n"+
				"\n"+
				"public class X {\n"+
				"\n"+
				"    private static boolean test(R<? extends I> r) {\n"+
				"        if (r instanceof R(X x)) {\n"+
				"             return (x instanceof X);\n"+
				"        }\n"+
				"        return true;\n"+
				"    }\n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"        System.out.println(test(new R<>(null)));\n"+
				"    }\n"+
				"}",
			},
			"true");
	}
	public void testRecordPatternTypeInference_015() {
		runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n"+
				"record R<T>(T t) {}\n"+
				"\n"+
				"public final class X {\n"+
				"\n"+
				"    private static boolean test(R<? extends I> r) {\n"+
				"        if (r instanceof R(X x)) {\n"+
				"             return (x instanceof X);\n"+
				"        }\n"+
				"        return true;\n"+
				"    }\n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"        System.out.println(test(new R<>(null)));\n"+
				"        Zork();\n"+
				"    }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	if (r instanceof R(X x)) {\n" +
			"	                   ^^^\n" +
			"Record component with type capture#2-of ? extends I is not compatible with type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 15)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testRecordPatternTypeInference_016() {
		runNegativeTest(
			new String[] {
				"X.java",
				"interface I {}\n"+
				"record R<T>(T t) {}\n"+
				"\n"+
				"public final class X {\n"+
				"\n"+
				"    private static boolean bar(R<? extends I> r) {\n"+
				"       return switch(r) {\n"+
				"               case R(X x) -> false;\n"+
				"               default -> true;\n"+
				"       };\n"+
				"    } \n"+
				"\n"+
				"    public static void main(String argv[]) {\n"+
				"        System.out.println(bar(new R<>(null)));\n"+
				"        Zork();\n"+
				"    }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	case R(X x) -> false;\n" +
			"	       ^^^\n" +
			"Record component with type capture#2-of ? extends I is not compatible with type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 15)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testIssue1328_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"    public int foo(Object o) {\n"+
					"        return switch (o) {\n"+
					"            case String s when false -> 1;\n"+
					"            case String s when true != true -> 2;\n"+
					"            case String s when false == true -> 3;\n"+
					"            case String s when 0 != 0 -> 3;\n"+
					"            default -> 0;\n"+
					"        };\n"+
					"    }\n"+
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	case String s when false -> 1;\n" +
				"	                   ^^^^^\n" +
				"A case label guard cannot have a constant expression with value as \'false\'\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 5)\n" +
				"	case String s when true != true -> 2;\n" +
				"	                   ^^^^^^^^^^^^\n" +
				"Comparing identical expressions\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	case String s when true != true -> 2;\n" +
				"	                   ^^^^^^^^^^^^\n" +
				"A case label guard cannot have a constant expression with value as \'false\'\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	case String s when false == true -> 3;\n" +
				"	                   ^^^^^^^^^^^^^\n" +
				"A case label guard cannot have a constant expression with value as \'false\'\n" +
				"----------\n" +
				"5. WARNING in X.java (at line 7)\n" +
				"	case String s when 0 != 0 -> 3;\n" +
				"	                   ^^^^^^\n" +
				"Comparing identical expressions\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 7)\n" +
				"	case String s when 0 != 0 -> 3;\n" +
				"	                   ^^^^^^\n" +
				"A case label guard cannot have a constant expression with value as \'false\'\n" +
				"----------\n");
	}
	public void testIssue1328_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"
					+ "    public int foo(Character c) {\n"
					+ "        int result = 0;\n"
					+ "        switch (c) {\n"
					+ "            case Character p when p.equals(\"c\") -> {\n"
					+ "                result = 6;\n"
					+ "            }\n"
					+ "        };\n"
					+ "        return result;\n"
					+ "    }\n"
					+ "}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	switch (c) {\n" +
				"	        ^\n" +
				"An enhanced switch statement should be exhaustive; a default label expected\n" +
				"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1792
	// [Patterns][records] Error in JDT Core during AST creation: info cannot be null
	public void testGH1792() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
				    private record R(Object o) {}
				    static void f(R [] rs) {
				        int i = 0;
				        while (rs[i++] instanceof R(String o)) {
				            System.out.println(o);
				        }
				    }
				    public static void main(String [] args) {
				    	R [] rs = { new R("So"), new R("far"), new R("so"), new R("good!"), null };
				    	f(rs);
				    }
				}
				"""
				},
				"So\n" +
				"far\n" +
				"so\n" +
				"good!");
	}
	public void testIssue1336_1() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					record R<T> ( T t) {}
					public final class X {
					private static boolean foo(R<?> r) {
						if (r instanceof R(String s)) {
							return true;
						}
						return false;
					}

					public static void main(String argv[]) {
						System.out.println(foo(new R<>(new String("hello"))));
					}

					}
					"""
			});
	}
	public void testIssue1336_2() {
		runConformTest(
				new String[] {
					"X.java",
					"""
					sealed interface I<TI> {}
					sealed interface J<TJ> {}
					class A {}
					record R<TR>(TR t) implements I<TR>, J<TR>{}

					public class X<TX extends I<? extends A> & J<? extends A>> {

						public boolean foo(TX t) {
							return switch(t) {
								case R(A a) -> true;
								default -> false;
							};
						}

						public static void main(String argv[]) {
						   System.out.println(new X<R<? extends A>>().foo(new R<>(new A())));
						}
					}
					"""
				});
	}
	public void testIssue1732_01() {
		runNegativeTest(
				new String[] {
				"X.java",
				"""
				record R(int x, int y) {}

				public class X {

					public int foo(R r) {
						return switch (r) {
							case R() -> 0;
						};
					}
				}
				"""
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	return switch (r) {\n"
				+ "	               ^\n"
				+ "A switch expression should have a default case\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	case R() -> 0;\n"
				+ "	     ^^^\n"
				+ "Record pattern should match the signature of the record declaration\n"
				+ "----------\n");
	}
	public void testIssue1732_02() {
		runNegativeTest(
				new String[] {
				"X.java",
				"""
				record R(int x, int y) {}

				public class X {

					public int foo(R r) {
						return switch (r) {
							case R(int x) -> 0;
						};
					}
				}
				"""
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	return switch (r) {\n"
				+ "	               ^\n"
				+ "A switch expression should have a default case\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	case R(int x) -> 0;\n"
				+ "	     ^^^^^^^^\n"
				+ "Record pattern should match the signature of the record declaration\n"
				+ "----------\n");
	}
	public void testIssue1732_03() {
		runNegativeTest(
				new String[] {
				"X.java",
				"""
				record R(int x, int y) {}

				public class X {

					public int foo(R r) {
						return switch (r) {
							case R(int x, int y, int z) -> 0;
						};
					}
				}
				"""
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 6)\n"
				+ "	return switch (r) {\n"
				+ "	               ^\n"
				+ "A switch expression should have a default case\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 7)\n"
				+ "	case R(int x, int y, int z) -> 0;\n"
				+ "	     ^^^^^^^^^^^^^^^^^^^^^^\n"
				+ "Record pattern should match the signature of the record declaration\n"
				+ "----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1788
	// Inference issue between the diamond syntax and pattern matching (switch on objects)
	public void testGHI1788() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

				  final static class Entry<K,V> {
				    K key;
				    V value;

				    public Entry(K key, V value) {
				      this.key = key;
				      this.value = value;
				    }

				    public K key() { return key; }
				    public V value() { return value; }
				    public void value(V value) { this.value = value; }
				    @Override
				    public String toString() {
				    	return "Key = " + key + " Value = " + value;
				    }
				  }

				  sealed interface I<V> {}
				  record A<V>(V value) implements I<V> {}
				  record B<V>(V value) implements I<V> {}

				  private static <K, V> Entry<K,V> foo(Entry<K, I<V>> entry) {
				    return new Entry<>(entry.key(), switch (entry.value()) {
				      case A<V>(V value) -> {
				        entry.value(new B<>(value));
				        yield value;
				      }
				      case B<V>(V value) -> value;
				    });
				  }

				  public static void main(String[] args) {
					  Entry<String, I<String>> entry = new Entry<>("KEY", new A<>("VALUE"));
					  System.out.println(entry);
					  System.out.println(foo(entry));
					  System.out.println(entry);
				  }
				}
				"""
				},
				"Key = KEY Value = A[value=VALUE]\n" +
				"Key = KEY Value = VALUE\n" +
				"Key = KEY Value = B[value=VALUE]");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1835
	// AssertionError at org.eclipse.jdt.internal.compiler.ast.YieldStatement.addSecretYieldResultValue(YieldStatement.java:120)
	public void testGH1835_minimal() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						try {
							String s = switch (new Object()) {
							case Integer i  -> "i";
							case String y  -> "y";
							default -> {
								try {
									throw new Exception();
								} catch (Exception e) {
									throw new RuntimeException("Expected");
								}
							}
							};
						} catch (RuntimeException e) {
							System.out.print("Caught runtime Exception ");
							if (e.getMessage().equals("Expected"))
								System.out.println ("(expected)");
							else
							 	System.out.println ("(unexpected!!!)");
						}
					}
				}
				"""
				},
				"Caught runtime Exception (expected)");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1835
	// AssertionError at org.eclipse.jdt.internal.compiler.ast.YieldStatement.addSecretYieldResultValue(YieldStatement.java:120)
	public void testGH1835() {
		runConformTest(
				new String[] {
				"Reproducer.java",
				"""
				public class Reproducer {

				    public class DataX {
				        String data = "DataX";
				    }

				    public class DataY {
				        String data1 = "DataY";
				    }

				    record X(DataX data) {}
				    record Y(DataY data) {}

				    Reproducer() {
				        DataX dataX = new DataX();
				        DataY dataY = new DataY();
				        X x = new X(dataX);
				        Y y = new Y(dataY);

				        foo(x);
				        foo(y);
				        foo(null);
				        foo("");
				    }

				    void foo(Object obj) {
				        String s = switch (obj) {
				            case X(var x) when x != null -> x.data;
				            case Y(var x) when x != null -> x.data1;
				            case null, default -> {
				                try {
				                    if (obj == null) yield "switch on null";
				                    throw new Exception();
				                } catch (Exception e) {
				                    yield "default threw exception";
				                }
				            }
				        };
				        System.out.println("s = " + s);
				    }
				    public static void main(String[] args) {
						new Reproducer();
					}

				}
				"""
				},
				"s = DataX\n" +
				"s = DataY\n" +
				"s = switch on null\n" +
				"s = default threw exception");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1796
	// [Patterns] Record Patterns can cause VerifyError
	public void testGH1796_full() {
		runConformTest(
				new String[] {
				"com/acme/Reproducer.java",
				"""
				package com.acme;

				import java.util.Objects;

				import com.acme.Reproducer.Expression.ConstantExpression;
				import com.acme.Reproducer.Expression.PlusExpression;
				import com.acme.Reproducer.Expression.TimesExpression;

				public class Reproducer {

				  public static void main(String[] args) {
				    SExpressionPrinter printer = new SExpressionPrinter21Record();
				    Expression twoPlusThree = new PlusExpression(new ConstantExpression(2), new ConstantExpression(3));
				    System.out.println(printer.print(twoPlusThree));
				  }

				  interface SExpressionPrinter {

				    String print(Expression e);

				  }

				  static final class SExpressionPrinter21Record implements SExpressionPrinter {

				    @Override
				    public String print(Expression e) {
				      Objects.requireNonNull(e);
				      StringBuilder buffer = new StringBuilder();
				      printTo(e, buffer);
				      return buffer.toString();
				    }

				    private void printTo(Expression e, StringBuilder buffer) {
				      if (e instanceof ConstantExpression(int i)) {
				        buffer.append(i);
				      } else {
				        buffer.append('(');
				        if (e instanceof PlusExpression(Expression a, Expression b)) {
				          buffer.append("+ ");
				          printTo(a, buffer);
				          buffer.append(' ');
				          printTo(b, buffer);
				        }
				        if (e instanceof TimesExpression(Expression a, Expression b)) {
				          buffer.append("* ");
				          printTo(a, buffer);
				          buffer.append(' ');
				          printTo(b, buffer);
				        }
				        buffer.append(')');
				      }
				    }
				  }

				  sealed interface Expression {

				    int evaluate();

				    record ConstantExpression(int i) implements Expression {

				      @Override
				      public int evaluate() {
				        return i;
				      }

				    }

				    record PlusExpression(Expression a, Expression b) implements Expression {

				      @Override
				      public int evaluate() {
				        return Math.addExact(a.evaluate(), b.evaluate());
				      }

				    }

				    record TimesExpression(Expression a, Expression b) implements Expression {

				      @Override
				      public int evaluate() {
				        return Math.multiplyExact(a.evaluate(), b.evaluate());
				      }

				    }

				  }

				}
				"""
				},
				"(+ 2 3)");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1796
	// [Patterns] Record Patterns can cause VerifyError
	public void testGH1796_reporter_reduced() {
		runConformTest(
				new String[] {
				"com/acme/Reproducer.java",
				"""
				package com.acme;

				import com.acme.Reproducer.Expression.ConstantExpression;

				public class Reproducer {

				  public static void main(String[] args) {
				    SExpressionPrinter printer = new SExpressionPrinter();
				    Expression constant = new ConstantExpression(2);
				    System.out.println(printer.print(constant));
				  }

				  static final class SExpressionPrinter {

				    public String print(Expression e) {
				      StringBuilder buffer = new StringBuilder();
				      printTo(e, buffer);
				      return buffer.toString();
				    }

				    private void printTo(Expression e, StringBuilder buffer) {
				      if (e instanceof ConstantExpression(int i)) {
				        buffer.append(i);
				      }
				    }
				  }

				  sealed interface Expression {

				    record ConstantExpression(int i) implements Expression {

				    }

				  }

				}
				"""
				},
				"2");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	public void testGH1977_method() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							if (new R(0) instanceof R(int i)) {
							}
						} catch (Throwable t) {
							System.out.println("Caught: " + t.getClass().getName());
						}
						if (new R(10) instanceof R(int i)) {

						}
					}
				}
				"""
				},
				"Caught: java.lang.MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	public void testGH1977_instance_initializer() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					{
						if (new R(0) instanceof R(int i)) {

						}
					}

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							new X();
						} catch (MatchException me) {
							System.out.println("Caught MatchException");
						}
					}
				}
				"""
				},
				"Caught MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	// javac reports ArithmeticException but that looks wrong
	public void testGH1977_instance_field() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					boolean b = new R(0) instanceof R(int i);

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							new X();
						} catch (MatchException me) {
							System.out.println("Caught MatchException");
						}
					}
				}
				"""
				},
				"Caught MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	public void testGH1977_constructor() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					X() {
						if (new R(0) instanceof R(int i)) {

						}
					}

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							new X();
						} catch (MatchException me) {
							System.out.println("Caught MatchException");
						}
					}
				}
				"""
				},
				"Caught MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	public void testGH1977_static_initializer() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					class Y {
						static {
							if (new R(0) instanceof R(int i)) {

							}
						}
					}

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							new X().new Y();
						} catch (ExceptionInInitializerError me) {
							System.out.println("ExceptionInInitializerError caused by " + me.getCause().getClass().getName());
						}
					}
				}
				"""
				},
				"ExceptionInInitializerError caused by java.lang.MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1977
	// [Patterns][records] ECJ generated code fails to raise MatchException properly
	// javac reports ExceptionInInitializerError caused by java.lang.ArithmeticException but that looks wrong
	public void testGH1977_static_field() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					class Y {
						static boolean b = new R(0) instanceof R(int i);
					}

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							new X().new Y();
						} catch (ExceptionInInitializerError me) {
							System.out.println("ExceptionInInitializerError caused by " + me.getCause().getClass().getName());
						}
					}
				}
				"""
				},
				"ExceptionInInitializerError caused by java.lang.MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/300
	// Revisit code generation for record patterns
	public void testIssue300() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {

					record Outer(Middle m1, Middle m2) {

					}

					record Middle (Inner i1, Inner i2) {

					}

					record Inner(String s, Long l, int k) {

					}

					public static void main(String[] args) {
						Outer o = new Outer(new Middle(new Inner("Hello", 11L, 22), new Inner(" World", 22L, 44)), new Middle(new Inner(" How is", 33L, 66), new Inner(" life?", 44L, 88)));
						if (o instanceof Outer(Middle(Inner(String s1, Long l1, int i1), Inner(String s2, Long l2, int i2)), Middle(Inner(String s3, Long l3, int i3), Inner(String s4, Long l4, int i4)))) {
							System.out.println(s1 + s2 + s3 + s4);
							System.out.println(l1 + l2 + l3 + l4);
							System.out.println(i1 + i2 + i3 + i4);
						}
					}
				}
				"""
				},
				"Hello World How is life?\n"
				+ "110\n"
				+ "220");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1804
	// Revisit code generation for record patterns
	public void testIssue1804() {
		runConformTest(
				new String[] {
				"X.java",
				"""
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String argv[]) {
						foo(null, null);
					}
					public static void foo(String abc, String def) {
						Box<?> p = new Box<>(new Paper(0));
						boolean b = false;
						switch (p) {
							case Box(Paper a) -> {
								b = true;
								break;
							}
							default -> {
								b = false;
								break;
							}
						}
						System.out.println(b);
					}
				}
				"""
				},
				"true");
	}
	public void testIssue1804_0() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box<?> b = new Box<>(null);
						boolean res = b instanceof Box(Paper a);
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is false");
	}
	public void testIssue1804_1() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box<?> b = new Box<>(new Paper(0));
						boolean res = b instanceof Box(Paper a);
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is true");
	}
	public void testIssue1804_2() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box<?> b = new Box<>(new Paper(0));
						boolean res = b instanceof Box(Paper a) && a == null;
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is false");
	}
	public void testIssue1804_3() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box b = new Box<>(null);
						System.out.println(b instanceof Box(Paper a));
						System.out.println(b instanceof Box(Object a));
					}
				}
				""" }, "false\ntrue");
	}
	public void testIssue1804_4() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String argv[]) {
						foo(null, null);
					}
					public static void foo(String abc, String def) {
						Box<?> p = new Box<>(new Paper(0));
						boolean b = false;
						switch (p) {
							case Box(Paper a) -> {
								b = true;
								break;
							}
							default -> {
								b = false;
								break;
							}
						}
						System.out.println(b);
					}
				}
				""" }, "true");
	}
	public void testIssue1804_5() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String argv[]) {
						foo(null, null);
					}
					public static void foo(String abc, String def) {
						Box<?> p = new Box<>(null);
						boolean b = false;
						switch (p) {
							case Box(Paper a) -> {
								b = true;
								break;
							}
							default -> {
								b = false;
								break;
							}
						}
						System.out.println(b);
					}
				}
				""" }, "false");
	}
	public void testIssue1804_6() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a, T b) {}
					public static void main(String argv[]) {
						foo(null, null);
					}
					public static void foo(String abc, String def) {
						Box<?> p = new Box<>(new Paper(0), new Paper(1));
						boolean c = false;
						switch (p) {
							case Box(Paper a, Paper b) -> {
								System.out.println(a.color);
								System.out.println(b.color);
								c = true;
								break;
							}
							default -> {
								c = false;
								break;
							}
						}
						System.out.println(c);
					}
				}
				""" }, "0\n1\ntrue");
	}
	public void testIssue1804_7() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box<?> b = new Box<>(new Paper(0));
						boolean res = b instanceof Box box;
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is true");
	}
	public void testIssue1804_8() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						Box<?> b = new Box<>(new Paper(0));
						boolean res = b instanceof Box(Paper paper) && paper.color != 0;
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is false");
	}
	public void testIssue1804_9() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						boolean res = new Box<>(new Paper(0)) instanceof Box(Paper(int c));
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is true");
	}
	public void testIssue1804_10() {
		runConformTest(new String[] { "X.java", """
				public class X {
					record Paper(int color) {}
					record Box<T>(T a) {}
					public static void main(String[] args) {
						boolean res = new Box<>(new Paper(0)) instanceof Box(Paper(int c)) && c == 0;
						if (res) {
							System.out.println("res is true");
						} else {
							System.out.println("res is false");
						}
					}
				}
				""" }, "res is true");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1985
	// [Patterns][records] ECJ fails to generate code to deconstruct record in pattern
	public void testIssue1985() {
		runConformTest(new String[] { "X.java", """
				public class X {

					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
						try {
							boolean b = new R(0) instanceof R(int i);
						} catch (Throwable t) {
							System.out.println(t.getClass().getName());
						}
					}
				}
				""" },
				"java.lang.MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1985
	// [Patterns][records] ECJ fails to generate code to deconstruct record in pattern
	public void testIssue1985_2() {
		runConformTest(new String[] { "X.java", """
				public class X {

					boolean b = new R(0) instanceof R(int i);
					record R(int x) {
						public int x() {
							return 100 / this.x;
						}
					}

					public static void main(String[] args) {
				            try {
				        	new X();
				            } catch (Throwable t) {
				        	System.out.println(t.getClass().getName());
				            }
					}
				}
				""" },
				"java.lang.MatchException");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2001
	// [Patterns][records] ECJ fails to reject incompatible pattern types.
	public void testIssue2001() {
		runNegativeTest(new String[] { "X.java",
				"""
				public class X {
					record R1(Long color) {}
					record R2(short color) {}

					public static void main(String[] args) {
						Object o = new R1(10L);
						if (o instanceof R1(long d)) {
							System.out.println(d);
						}
						if (o instanceof R2(Short d)) {
							System.out.println(d);
						}
						if (o instanceof R2(int d)) {
							System.out.println(d);
						}
					}
				}
				"""
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	if (o instanceof R1(long d)) {\n" +
				"	                    ^^^^^^\n" +
				"Record component with type Long is not compatible with type long\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 10)\n" +
				"	if (o instanceof R2(Short d)) {\n" +
				"	                    ^^^^^^^\n" +
				"Record component with type short is not compatible with type Short\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 13)\n" +
				"	if (o instanceof R2(int d)) {\n" +
				"	                    ^^^^^\n" +
				"Record component with type short is not compatible with type int\n" +
				"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1999
	// [Patterns][records] Instanceof with record deconstruction patterns should never be flagged as unnecessary
	public void testIssue1999() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
		runNegativeTest(
				true,
				new String[] {
				"X.java",
				"""
				interface I {
				}

				final class A implements I {
				}

				final class B implements I {
				}

				record R(I x, I y) {
				}

				public class X {
					public static boolean foo(R r) {
						if (r instanceof R(A a1, A a2))  // don't warn here.
							return true;
						A a = null;
						if (a instanceof A) {} // warn here
						if (a instanceof A a1) {} // don't warn here
						return false;
					}

					public static void main(String argv[]) {
						System.out.println(X.foo(new R(new A(), new A())));
						System.out.println(X.foo(new R(new A(), new B())));
					}
				}
				"""
				},
				null,
				options,
				"----------\n" +
				"1. ERROR in X.java (at line 18)\n" +
				"	if (a instanceof A) {} // warn here\n" +
				"	    ^^^^^^^^^^^^^^\n" +
				"The expression of type A is already an instance of type A\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2007
	public void testIssue2007() {
		runConformTest(new String[] { "X.java", """
				record R<T>(T t) {}
				public class X<T> {
				    public boolean foo(R<T> r) {
				        return (r instanceof R<?>(X x));
				    }
				    public static void main(String argv[]) {
				    	System.out.println(new X<>().foo(new R<>(new X())));
				    }
				}
				""" },
				"true");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2007
	public void testIssue2007_2() {
		runConformTest(new String[] { "X.java", """
				record R<T>(T t) {}
				public class X<T> {
				    public boolean foo(R<T> r) {
				         return (r instanceof R<? extends T>(X x));
				    }
				    public static void main(String argv[]) {
				    	System.out.println(new X<>().foo(new R<>(new X())));
				    }
				}
				""" },
				"true");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2007
	public void testIssue2007_3() {
		runConformTest(new String[] { "X.java", """
				record R<T>(T t) {}
				public class X<T> {
				    public boolean foo(R<T> r) {
				    	return switch (r) {
				    		case R<?>(X x) -> true;
				    		default -> false;
				    	};
				    }
				    public static void main(String argv[]) {
				    	System.out.println(new X<>().foo(new R<>(new X())));
				    }
				}
				""" },
				"true");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2007
	public void testIssue2007_4() {
		runConformTest(new String[] { "X.java", """
				record R<T>(T t) {}
				public class X<T> {
				    public boolean foo(R<T> r) {
				    	return switch (r) {
				    		case R<? extends T>(X x) -> true;
				    		default -> false;
				    	};
				    }
				    public static void main(String argv[]) {
				    	System.out.println(new X<>().foo(new R<>(new X())));
				    }
				}
				""" },
				"true");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2007
	public void testIssue2007_5() {
		runConformTest(new String[] { "X.java", """
				record R<T>(T t) {}
				public class X<T> {
				    public boolean foo(R<T> r) {
				    	return switch (r) {
				    		case R<? extends T>(Integer i) -> true;
				    		default -> false;
				    	};
				    }
				    public static void main(String argv[]) {
				    	System.out.println(new X<>().foo(new R<>(new X())));
				    }
				}
				""" },
				"false");
	}

	public void testIllegalFallThrough() {
		runNegativeTest(new String[] { "X.java", """
				public class X {
					record Point (int x, int y) {}

				  static void foo(Object o) {
				    switch (o) {
				      case Integer i_1: System.out.println("Integer");
				      case Point(int a, int b) : System.out.println("String");
				      default: System.out.println("Object");
				    }
				  }
				}
				""" },
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	case Point(int a, int b) : System.out.println(\"String\");\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Illegal fall-through to a pattern\n" +
				"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2118
	// [Patterns] ECJ allows illegal modifiers with RecordPattern
	public void testIllegalModifiers() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					record Point (int x, int y) {}

					static void foo(Object o) {
					    if (o instanceof public String) {}   // javac error, ecj error
					    if (o instanceof public String s) {} // javac error, ecj error
					    if (o instanceof public Point(int a, final int b)) {} // javac error, ECJ - NO ERROR!
					    if (o instanceof Point(public int a, final int b)) {} // javac error, ecj error

					    if (o instanceof final String) {}  // javac error, ecj error
					    if (o instanceof final String s) {} // javac NO error, ecj NO error
					    if (o instanceof final Point(int a, int b)) {} // javac NO error, ecj NO error

					    switch (o) {
					      case public Point(int a, int b) : System.out.println("String"); // javac error, ECJ: NO ERROR!
					      case public Object o1: System.out.println("Default"); // both compilers error
					    }
					    switch (o) {
					      case final Point(int a, int b) : System.out.println("String"); // NO ERROR in either
					      case final Object o2: System.out.println("Default");
					    }
					}
				}
				"""
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	if (o instanceof public String) {}   // javac error, ecj error\n" +
				"	                 ^^^^^^^^^^^^^\n" +
				"Syntax error, modifiers are not allowed here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	if (o instanceof public String s) {} // javac error, ecj error\n" +
				"	                               ^\n" +
				"Illegal modifier for the pattern variable s; only final is permitted\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	if (o instanceof public Point(int a, final int b)) {} // javac error, ECJ - NO ERROR!\n" +
				"	                 ^^^^^^\n" +
				"Syntax error, modifiers are not allowed here\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	if (o instanceof Point(public int a, final int b)) {} // javac error, ecj error\n" +
				"	                                  ^\n" +
				"Illegal modifier for the pattern variable a; only final is permitted\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 10)\n" +
				"	if (o instanceof final String) {}  // javac error, ecj error\n" +
				"	                 ^^^^^^^^^^^^\n" +
				"Syntax error, modifiers are not allowed here\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 12)\n" +
				"	if (o instanceof final Point(int a, int b)) {} // javac NO error, ecj NO error\n" +
				"	                 ^^^^^\n" +
				"Syntax error, modifiers are not allowed here\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 15)\n" +
				"	case public Point(int a, int b) : System.out.println(\"String\"); // javac error, ECJ: NO ERROR!\n" +
				"	     ^^^^^^\n" +
				"Syntax error, modifiers are not allowed here\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 16)\n" +
				"	case public Object o1: System.out.println(\"Default\"); // both compilers error\n" +
				"	                   ^^\n" +
				"Illegal modifier for the pattern variable o1; only final is permitted\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 19)\n" +
				"	case final Point(int a, int b) : System.out.println(\"String\"); // NO ERROR in either\n" +
				"	     ^^^^^\n" +
				"Syntax error, modifiers are not allowed here\n" +
				"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2119
	// [Patterns] ECJ allows record pattern to have dimensions
	public void testIssue2119() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					record Point (int x, int y) {}

					static void foo(Object o) {
						if (o instanceof Point [](int x, int y)) {}
					}
				}
				"""
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	if (o instanceof Point [](int x, int y)) {}\n" +
				"	                 ^^^^^^^^^^^^^^^^^^^^^^\n" +
				"A record pattern may not specify dimensions\n" +
				"----------\n");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2119
	// [Patterns] ECJ allows record pattern to have dimensions
	public void testIssue2119_2() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					record Point (int x, int y) {}

					static void foo(Object o) {
						if (o instanceof Point (int x, int y) []) {}
					}
				}
				"""
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	if (o instanceof Point (int x, int y) []) {}\n" +
				"	                                      ^^\n" +
				"Syntax error on tokens, delete these tokens\n" +
				"----------\n");
	}

	public void testIssue3066() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public record X<T>(int x) {
					public static void main(String[] args) {
						Object o = new Object();
						switch (o) {
						case X<String>(int x):
						default:
						}
					}
				}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 5)
				case X<String>(int x):
				     ^^^^^^^^^^^^^^^^
			Type Object cannot be safely cast to X<String>
			----------
			""");
	}

	public void testIssue3066_notApplicable() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public record X(int x) {
					public static void main(String[] args) {
						java.io.Serializable o = "";
						switch (o) {
						case X(int x):
						default:
						}
					}
				}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 5)
				case X(int x):
				     ^^^^^^^^
			Type mismatch: cannot convert from Serializable to X
			----------
			""");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3173
	// [21][Enhanced Switch] False error about allegedly non-exhaustive switch
	public void testIssue3173() {
		runConformTest(new String[] {
				"RecordPatternDemo.java",
				"""
				public class RecordPatternDemo {
				    public static void main(String[] args) {
				        record Box<T>(T contents) { }

				        Box<Box<String>> doubleBoxed = new Box<>(new Box<>("Contents"));
				        String unboxed = switch (doubleBoxed) {
				            case Box(Box(String s)) -> s;
				        };

				        System.out.println(unboxed);
				    }
				}
				"""
				},
				"Contents");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3173
	// [21][Enhanced Switch] False error about allegedly non-exhaustive switch
	public void testIssue3173_2() {
		runConformTest(new String[] {
				"RecordPatternDemo.java",
				"""
				public class RecordPatternDemo {
				    public static void main(String[] args) {
				        record Box<T>(T contents) { }

				        Box<Box<String>> doubleBoxed = new Box<>(new Box<>("Contents"));
				        String unboxed = switch (doubleBoxed) {
				            case Box(Box(String s)) -> s;
				            default -> "default";
				        };

				        System.out.println(unboxed);
				    }
				}
				"""
				},
				"Contents");
	}
}