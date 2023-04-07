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

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class RecordPatternTest extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 20 --enable-preview -Xlint:-preview");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };

//		TESTS_NAMES = new String[] { "testRecordPatternTypeInference_011" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return RecordPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_20);
	}
	public RecordPatternTest(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		if (preview) {
			if (this.complianceLevel >= ClassFileConstants.getLatestJDKLevel()) {
				defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
			} else {
				defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_20);
				defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_20);
				defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_20);
			}
		}
		return defaultOptions;
	}

	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(true);
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
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true));
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE20Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, new String[] {"--enable-preview"}, JAVAC_OPTIONS);
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
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr) )) {\n"
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
				"@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr))) {\n"
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
				"1. ERROR in X.java (at line 6)\n" +
				"	System.out.println(\"Upper-left corner: \" + r1);\n" +
				"	                                           ^^\n" +
				"r1 cannot be resolved to a variable\n" +
				"----------\n");
	}
	public void test004() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr))) {\n"
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
				"1. ERROR in X.java (at line 9)\n" +
				"	print(new Rectangle(new ColoredPoint(new PointTypo(0, 0), Color.BLUE),\n" +
				"	                                         ^^^^^^^^^\n" +
				"PointTypo cannot be resolved to a type\n" +
				"----------\n");
	}
	// Test that non record types are reported in a record pattern
	public void test005() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int i, int j), Color c),\n"
				+ "	    									ColoredPoint lr))) {\n"
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
				"1. ERROR in X.java (at line 4)\n" +
				"	if (r instanceof (Rectangle(ColoredPoint(Point(int i, int j), Color c),\n" +
				"	                                         ^^^^^\n" +
				"Only record types are permitted in a record pattern\n" +
				"----------\n");
	}
	// Test that record patterns that don't have same no of patterns as record components are reported
	public void test006() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int i), Color c),\n"
				+ "	    									ColoredPoint lr))) {\n"
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
				"1. ERROR in X.java (at line 4)\n" +
				"	if (r instanceof (Rectangle(ColoredPoint(Point(int i), Color c),\n" +
				"	                                         ^^^^^^^^^^^^\n" +
				"Record pattern should match the signature of the record declaration\n" +
				"----------\n");
	}
	public void test007() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof (Rectangle(ColoredPoint(Point(String o1, String o2), Color c),\n"
				+ "	    									ColoredPoint lr))) {\n"
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
				"1. ERROR in X.java (at line 4)\n" +
				"	if (r instanceof (Rectangle(ColoredPoint(Point(String o1, String o2), Color c),\n" +
				"	                                               ^^^^^^^^^\n" +
				"Pattern of type int is not compatible with type java.lang.String\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	if (r instanceof (Rectangle(ColoredPoint(Point(String o1, String o2), Color c),\n" +
				"	                                                          ^^^^^^^^^\n" +
				"Pattern of type int is not compatible with type java.lang.String\n" +
				"----------\n");
	}
	// Test that pattern types that don't match record component's types are reported
	public void test008() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int i, int j), Color c), ColoredPoint lr, Object obj))) {\n"
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
				"1. ERROR in X.java (at line 4)\n" +
				"	if (r instanceof (Rectangle(ColoredPoint(Point(int i, int j), Color c), ColoredPoint lr, Object obj))) {\n" +
				"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
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
				"@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
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
				"1. ERROR in X.java (at line 8)\n" +
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
				"@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
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
				"1. ERROR in X.java (at line 9)\n" +
				"	default -> {yield x;}    };\n" +
				"	                  ^\n" +
				"x cannot be resolved to a variable\n" +
				"----------\n");
	}
	// Test that nested pattern variables from record patterns are in not scope outside the case block
	public void test12() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
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
				"1. ERROR in X.java (at line 9)\n" +
				"	default -> {yield x1;}    };\n" +
				"	                  ^^\n" +
				"x1 cannot be resolved to a variable\n" +
				"----------\n");
	}
	// Test that when expressions are supported and pattern variables are available inside when expressions
	public void test13() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
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
					"@SuppressWarnings(\"preview\")"
					+ "public class X {\n"
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
				getCompilerOptions(true),
				new String[] {"--enable-preview"},
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
				+ "}\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}\n"
			},
			this.extraLibPath,
			JavaCore.VERSION_20,
			true);
		this.runConformTest(
				new String[] {
						"p/X.java",
						"package p;\n"
						+ "@SuppressWarnings(\"preview\")\n"
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
				getCompilerOptions(true),
				new String[] {"--enable-preview"},
				JavacTestOptions.SKIP); // Too complicated to pass extra lib to Javac, let's skip
		} catch (IOException e) {
			System.err.println("RecordPatternTest.test25() could not write to current working directory " + currentWorkingDirectoryPath);
		} finally {
			new File(this.extraLibPath).delete();
		}
	}
	// Test that pattern variables declared in instanceof can't be used in a switch/case
	// Error messages need to rechecked - too many - ref https://github.com/eclipse-jdt/eclipse.jdt.core/issues/777
	public void test26() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
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
				"1. ERROR in X.java (at line 6)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n" +
				"	                                      ^\n" +
				"Duplicate local variable x\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n" +
				"	                                             ^\n" +
				"Duplicate local variable y\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n" +
				"	                                                       ^\n" +
				"Duplicate local variable c\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	ColoredPoint lr) -> {\n" +
				"	             ^^\n" +
				"Duplicate local variable lr\n" +
				"----------\n");
	}
	// Test that pattern variables declared in switch/case can't be used in an instanceof expression part of the 'when' clause
	// not relevant anymore since named record patterns are not there - 20
	public void test27() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
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
				"1. ERROR in X.java (at line 5)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {\n" +
				"	                                                                                                                      ^\n" +
				"Duplicate local variable x\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {\n" +
				"	                                                                                                                             ^\n" +
				"Duplicate local variable y\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {\n" +
				"	                                                                                                                                       ^\n" +
				"Duplicate local variable c\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 5)\n" +
				"	case Rectangle(ColoredPoint(Point(int x, int y), Color c), ColoredPoint lr) when lr instanceof ColoredPoint(Point(int x, int y), Color c) -> {\n" +
				"	                                                                                                                                       ^\n" +
				"Duplicate local variable c\n" +
				"----------\n");
	}
	// Test nested record patterns in 'instanceof' within a swith-case with similar record pattern
	public void test28() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
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
						"@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
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
						"@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
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
						"@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
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
				"@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
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
						+ "        case ((R r)) -> 1;\n"
						+ "        case ((R(int a))) -> 0;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "	}\n"
						+ "}\n"
						+ "record R(int i) {}"
		},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case ((R(int a))) -> 0;\n" +
				"	       ^^^^^^^^\n" +
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
						+ "        case ((R(int a))) -> 1;\n"
						+ "        case ((R(int a))) -> 0;\n"
						+ "        default -> -1;\n"
						+ "       };\n"
						+ "	}\n"
						+ "}\n"
						+ "record R(int i) {}"
		},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	case ((R(int a))) -> 0;\n" +
				"	       ^^^^^^^^\n" +
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
						"@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
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
						"@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
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
						"@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
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
			"Pattern of type int is not compatible with type long\n" +
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
			"Pattern of type long is not compatible with type int\n" +
			"----------\n");
	}
	public void test45() {
		runNegativeTest(new String[] {
				"X.java",
						"@SuppressWarnings(\"preview\")\n"
						+ "public class X {\n"
						+ "	static void print(Object r) {\n"
						+ "		switch (r) {\n"
						+ "			case Rectangle(var a, var b) when (r instanceof (Rectangle(ColoredPoint upperLeft2, ColoredPoint lowerRight))):\n"
						+ "				System.out.println(r);// error should not be reported here\n"
						+ "			break;\n"
						+ "		}\n"
						+ "	}\n"
						+ "}\n"
						+ "record ColoredPoint() {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {} "
		},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
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
				+ "      System.out.println(s); // this one should report an unsafe cast error\n"
				+ "    }\n"
				+ "  }\n"
				+ "  public static void main(String[] args) {}\n"
				+ "}\n"
				+ "record Box<T>(T t) {} "
			},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	if (objectBox instanceof Box<String>(String s)) {\n" +
				"	    ^^^^^^^^^\n" +
				"Type Box<Object> cannot be safely cast to Box<String>\n" +
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
					"@SuppressWarnings(\"preview\")\n"
					+ "public class X {\n"
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
				"1. ERROR in X.java (at line 5)\n" +
				"	case R(Integer i1, Double i1) -> {}\n" +
				"	                          ^^\n" +
				"Duplicate local variable i1\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	case OuterR(R(Integer i1, Double i2), R(Integer i2, Double i2)) -> {}\n" +
				"	                                                ^^\n" +
				"Duplicate local variable i2\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	case OuterR(R(Integer i1, Double i2), R(Integer i2, Double i2)) -> {}\n" +
				"	                                                           ^^\n" +
				"Duplicate local variable i2\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	case OuterR(R(Integer i1, Double i2), R(Integer i2, Double i2)) -> {}\n" +
				"	                                                           ^^\n" +
				"Duplicate local variable i2\n" +
				"----------\n");
	}
	public void testIssue690_2() {
		runNegativeTest(new String[] {
				"X.java",
					"@SuppressWarnings(\"preview\")\n"
					+ "public class X {\n"
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
				"1. ERROR in X.java (at line 7)\n" +
				"	if (s instanceof OuterR(R(Integer i1, Double i2), R(Integer i1, Double i4))) { \n" +
				"	                                                            ^^\n" +
				"Duplicate local variable i1\n" +
				"----------\n");
	}
	public void testIssue691_1() {
		runNegativeTest(new String[] {
				"X.java",
					"@SuppressWarnings(\"preview\")\n"
					+ "public class X {\n"
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
				"1. ERROR in X.java (at line 5)\n" +
				"	case R(Integer i1, Integer i2) -> {}\n" +
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type mismatch: cannot convert from Number to R\n" +
				"----------\n");
	}
	public void testRemoveNamedRecordPatterns_001() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
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
				"1. ERROR in X.java (at line 5)\n" +
				"	case Rectangle(int x, int y) r -> 1;\n" +
				"	                             ^\n" +
				"Syntax error on token \"r\", delete this token\n" +
				"----------\n");
	}
	public void testEnhancedForWithRecordPattern_001() {
		runConformTest(new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"@SuppressWarnings(\"preview\")\n" +
			"public class X {\n" +
			" public static void foo(List<R> rList) {\n" +
			"   for (R(Integer a) : rList) { \n" +
			"     System.out.println(a);  \n" +
			"   }\n" +
			" }\n" +
			" public static void main(String[] args) {\n" +
			"   List<R> rList = new ArrayList<>();\n" +
			"   rList.add(new R(1));\n" +
			"   rList.add(new R(2));\n" +
			"   foo(rList);\n" +
			" }\n" +
			"}\n" +
			"record R(Integer i) {}"
			},
			"1\n" +
			"2");
	}
	public void testEnhancedForWithRecordPattern_002() {
		runConformTest(new String[] {
			"X.java",
			"@SuppressWarnings(\"preview\")\n" +
			"public class X {\n" +
			"    public static boolean foo() {\n" +
			"        boolean ret = false;\n" +
			"        R[] recArray = {new R(0)};\n" +
			"        for (R(int x) : recArray) {\n" +
			"            ret = true;\n" +
			"        }\n" +
			"        return ret;\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"   System.out.println(foo());\n" +
			" }\n" +
			"}\n" +
			"record R(int i) {}"
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
			"@SuppressWarnings(\"preview\")\n" +
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
			"1. ERROR in X.java (at line 12)\n" +
			"	Zork();\n" +
			"	^^^^^^\n" +
			"Return type for the method is missing\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 12)\n" +
			"	Zork();\n" +
			"	^^^^^^\n" +
			"This method requires a body instead of a semicolon\n" +
			"----------\n");
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
	// TODO : STACK VERIFICATION ERROR
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
	// TODO: failing
	public void _testRecordPatternTypeInference_009() {
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
				"1. WARNING in X.java (at line 10)\n" +
				"	if (p instanceof R(String a)) {\n" +
				"	                 ^^^^^^^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 10)\n" +
				"	if (p instanceof R(String a)) {\n" +
				"	                   ^^^^^^^^\n" +
				"Pattern of type ? extends I is not compatible with type java.lang.String\n" +
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
	public void testIssue882() {
		Map<String, String> options = getCompilerOptions(false);
		runNegativeTest(new String[] {
				"X.java",
				"import java.util.ArrayList;\n"
				+ "import java.util.List;\n"
				+ "@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "	public static void foo(List<R> rList) {\n"
				+ "		for(R(Integer abcs):rList) {\n"
				+ "			System.out.println(abcs);\n"
				+ "		}\n"
				+ "	}\n"
				+ "	record R(int i) {}\n"
				+ "}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	for(R(Integer abcs):rList) {\n" +
				"	    ^^^^^^^^^^^^^^^\n" +
				"Record Pattern is a preview feature and disabled by default. Use --enable-preview to enable\n" +
				"----------\n",
				"",
				null,
				false,
				options);
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
				"----------\n" +
				"2. WARNING in X.java (at line 10)\n" +
				"	if (p instanceof R<>(String a)) {\n" +
				"	                 ^^^^^^^^^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n");
	}
}
