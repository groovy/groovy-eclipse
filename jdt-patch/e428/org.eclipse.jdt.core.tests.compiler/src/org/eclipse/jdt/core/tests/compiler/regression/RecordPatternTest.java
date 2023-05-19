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
	public void testIssue900_1() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n"
				+ "class X {\n"
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
				"@SuppressWarnings(\"preview\")\n"
				+ "class X {\n"
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
		Map<String,String> options = getCompilerOptions(true);
		String old1 = options.get(CompilerOptions.OPTION_ReportRawTypeReference);
		String old2 = options.get(CompilerOptions.OPTION_ReportUncheckedTypeOperation);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
		try {
			runNegativeTest(new String[] {
					"X.java",
					"@SuppressWarnings(\"preview\")\n"
							+ "class X {\n"
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
				"1. ERROR in X.java (at line 4)\n" +
				"	static void test3(Box<Box<String>> bo) {\n" +
				"	                      ^^^\n" +
				"Incorrect number of arguments for type X.Box<T,U>; it cannot be parameterized with arguments <String>\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	if (bo instanceof Box<Box<String>>(Box(var s1, String s2), Box b1)) {        \n" +
				"	                      ^^^\n" +
				"Incorrect number of arguments for type X.Box<T,U>; it cannot be parameterized with arguments <String>\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 11)\n" +
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
  public void testIssue945_1() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n"
				+ "public class X {\n"
				+ "    record R0(int x) {}\n"
				+ "    record R1(R0 r, int x) {}\n"
				+ "    record R2(R1 r, int x) {}\n"
				+ "    record R3(R2 r, int x) {}\n"
				+ "    record R4(R3 r, int x) {}\n"
				+ "    record R5(R4 r, int x) {}\n"
				+ "    record R6(R5 r, int x) {}\n"
				+ "    record R7(R6 r, int x) {}\n"
				+ "    record R8(R7 r, int x) {}\n"
				+ "    record R9(R8 r, int x) {}\n"
				+ "    record R10(R9 r, int x) {}\n"
				+ "    record R11(R10 r, int x) {}\n"
				+ "    record R12(R11 r, int x) {}\n"
				+ "    record R13(R12 r, int x) {}\n"
				+ "    record R14(R13 r, int x) {}\n"
				+ "    record R15(R14 r, int x) {}\n"
				+ "    record R16(R15 r, int x) {}\n"
				+ "    record R17(R16 r, int x) {}\n"
				+ "    record R18(R17 r, int x) {}\n"
				+ "    record R19(R18 r, int x) {}\n"
				+ "    record R20(R19 r, int x) {}\n"
				+ "    record R21(R20 r, int x) {}\n"
				+ "    record R22(R21 r, int x) {}\n"
				+ "    record R23(R22 r, int x) {}\n"
				+ "    record R24(R23 r, int x) {}\n"
				+ "    record R25(R24 r, int x) {}\n"
				+ "    record R26(R25 r, int x) {}\n"
				+ "    record R27(R26 r, int x) {}\n"
				+ "    record R28(R27 r, int x) {}\n"
				+ "    record R29(R28 r, int x) {}\n"
				+ "    record R30(R29 r, int x) {}\n"
				+ "    record R31(R30 r, int x) {}\n"
				+ "    record R32(R31 r, int x) {}\n"
				+ "    record R33(R32 r, int x) {}\n"
				+ "    record R34(R33 r, int x) {}\n"
				+ "    record R35(R34 r, int x) {}\n"
				+ "    record R36(R35 r, int x) {}\n"
				+ "    record R37(R36 r, int x) {}\n"
				+ "    record R38(R37 r, int x) {}\n"
				+ "    record R39(R38 r, int x) {}\n"
				+ "    record R40(R39 r, int x) {}\n"
				+ "    record R41(R40 r, int x) {}\n"
				+ "    record R42(R41 r, int x) {}\n"
				+ "    record R43(R42 r, int x) {}\n"
				+ "    record R44(R43 r, int x) {}\n"
				+ "    record R45(R44 r, int x) {}\n"
				+ "    record R46(R45 r, int x) {}\n"
				+ "    record R47(R46 r, int x) {}\n"
				+ "    record R48(R47 r, int x) {}\n"
				+ "    record R49(R48 r, int x) {}\n"
				+ "    record R50(R49 r, int x) {}\n"
				+ "    record R51(R50 r, int x) {}\n"
				+ "    record R52(R51 r, int x) {}\n"
				+ "    record R53(R52 r, int x) {}\n"
				+ "    record R54(R53 r, int x) {}\n"
				+ "    record R55(R54 r, int x) {}\n"
				+ "    record R56(R55 r, int x) {}\n"
				+ "    record R57(R56 r, int x) {}\n"
				+ "    record R58(R57 r, int x) {}\n"
				+ "    record R59(R58 r, int x) {}\n"
				+ "    record R60(R59 r, int x) {}\n"
				+ "    record R61(R60 r, int x) {}\n"
				+ "    record R62(R61 r, int x) {}\n"
				+ "    record R63(R62 r, int x) {}\n"
				+ "    record R64(R63 r, int x) {}\n"
				+ "    record R65(R64 r, int x) {}\n"
				+ "    record R66(R65 r, int x) {}\n"
				+ "    record R67(R66 r, int x) {}\n"
				+ "    record R68(R67 r, int x) {}\n"
				+ "    record R69(R68 r, int x) {}\n"
				+ "    record R70(R69 r, int x) {}\n"
				+ "    record R71(R70 r, int x) {}\n"
				+ "    record R72(R71 r, int x) {}\n"
				+ "    record R73(R72 r, int x) {}\n"
				+ "    record R74(R73 r, int x) {}\n"
				+ "    record R75(R74 r, int x) {}\n"
				+ "    record R76(R75 r, int x) {}\n"
				+ "    record R77(R76 r, int x) {}\n"
				+ "    record R78(R77 r, int x) {}\n"
				+ "    record R79(R78 r, int x) {}\n"
				+ "    record R80(R79 r, int x) {}\n"
				+ "    record R81(R80 r, int x) {}\n"
				+ "    record R82(R81 r, int x) {}\n"
				+ "    record R83(R82 r, int x) {}\n"
				+ "    record R84(R83 r, int x) {}\n"
				+ "    record R85(R84 r, int x) {}\n"
				+ "    record R86(R85 r, int x) {}\n"
				+ "    record R87(R86 r, int x) {}\n"
				+ "    record R88(R87 r, int x) {}\n"
				+ "    record R89(R88 r, int x) {}\n"
				+ "    record R90(R89 r, int x) {}\n"
				+ "    record R91(R90 r, int x) {}\n"
				+ "    record R92(R91 r, int x) {}\n"
				+ "    record R93(R92 r, int x) {}\n"
				+ "    record R94(R93 r, int x) {}\n"
				+ "    record R95(R94 r, int x) {}\n"
				+ "    record R96(R95 r, int x) {}\n"
				+ "    record R97(R96 r, int x) {}\n"
				+ "    record R98(R97 r, int x) {}\n"
				+ "    record R99(R98 r, int x) {}\n"
				+ "    public static void main(String args[]) {\n"
				+ "        boolean match = false;\n"
				+ "        R99[] array = {new R99(new R98(new R97(new R96(new R95(new R94(new R93(new R92(new R91(new R90(new R89(new R88(new R87(new R86(new R85(new R84(new R83(new R82(new R81(new R80(new R79(new R78(new R77(new R76(new R75(new R74(new R73(new R72(new R71(new R70(new R69(new R68(new R67(new R66(new R65(new R64(new R63(new R62(new R61(new R60(new R59(new R58(new R57(new R56(new R55(new R54(new R53(new R52(new R51(new R50(new R49(new R48(new R47(new R46(new R45(new R44(new R43(new R42(new R41(new R40(new R39(new R38(new R37(new R36(new R35(new R34(new R33(new R32(new R31(new R30(new R29(new R28(new R27(new R26(new R25(new R24(new R23(new R22(new R21(new R20(new R19(new R18(new R17(new R16(new R15(new R14(new R13(new R12(new R11(new R10(new R9(new R8(new R7(new R6(new R5(new R4(new R3(new R2(new R1(new R0(\"\"), 1), 2), 3), 4), 5), 6), 7), 8), 9), 10), 11), 12), 13), 14), 15), 16), 17), 18), 19), 20), 21), 22), 23), 24), 25), 26), 27), 28), 29), 30), 31), 32), 33), 34), 35), 36), 37), 38), 39), 40), 41), 42), 43), 44), 45), 46), 47), 48), 49), 50), 51), 52), 53), 54), 55), 56), 57), 58), 59), 60), 61), 62), 63), 64), 65), 66), 67), 68), 69), 70), 71), 72), 73), 74), 75), 76), 77), 78), 79), 80), 81), 82), 83), 84), 85), 86), 87), 88), 89), 90), 91), 92), 93), 94), 95), 96), 97), 98), 99)};\n"
				+ "        for (R99(R98(R97(R96(R95(R94(R93(R92(R91(R90(R89(R88(R87(R86(R85(R84(R83(R82(R81(R80(R79(R78(R77(R76(R75(R74(R73(R72(R71(R70(R69(R68(R67(R66(R65(R64(R63(R62(R61(R60(R59(R58(R57(R56(R55(R54(R53(R52(R51(R50(R49(R48(R47(R46(R45(R44(R43(R42(R41(R40(R39(R38(R37(R36(R35(R34(R33(R32(R31(R30(R29(R28(R27(R26(R25(R24(R23(R22(R21(R20(R19(R18(R17(R16(R15(R14(R13(R12(R11(R10(R9(R8(R7(R6(R5(R4(R3(R2(R1(R0(int i0), int i1), int i2), int i3), int i4), int i5), int i6), int i7), int i8), int i9), int i10), int i11), int i12), int i13), int i14), int i15), int i16), int i17), int i18), int i19), int i20), int i21), int i22), int i23), int i24), int i25), int i26), int i27), int i28), int i29), int i30), int i31), int i32), int i33), int i34), int i35), int i36), int i37), int i38), int i39), int i40), int i41), int i42), int i43), int i44), int i45), int i46), int i47), int i48), int i49), int i50), int i51), int i52), int i53), int i54), int i55), int i56), int i57), int i58), int i59), int i60), int i61), int i62), int i63), int i64), int i65), int i66), int i67), int i68), int i69), int i70), int i71), int i72), int i73), int i74), int i75), int i76), int i77), int i78), int i79), int i80), int i81), int i82), int i83), int i84), int i85), int i86), int i87), int i88), int i89), int i90), int i91), int i92), int i93), int i94), int i95), int i96), int i97), int i98), int i99) : array) {\n"
				+ "            match = i0==0 && i1==1 && i2==2 && i3==3 && i4==4 && i5==5 && i6==6 && i7==7 && i8==8 && i9==9 && i10==10 && i11==11 && i12==12 && i13==13 && i14==14 && i15==15 && i16==16 && i17==17 && i18==18 && i19==19 && i20==20 && i21==21 && i22==22 && i23==23 && i24==24 && i25==25 && i26==26 && i27==27 && i28==28 && i29==29 && i30==30 && i31==31 && i32==32 && i33==33 && i34==34 && i35==35 && i36==36 && i37==37 && i38==38 && i39==39 && i40==40 && i41==41 && i42==42 && i43==43 && i44==44 && i45==45 && i46==46 && i47==47 && i48==48 && i49==49 && i50==50 && i51==51 && i52==52 && i53==53 && i54==54 && i55==55 && i56==56 && i57==57 && i58==58 && i59==59 && i60==60 && i61==61 && i62==62 && i63==63 && i64==64 && i65==65 && i66==66 && i67==67 && i68==68 && i69==69 && i70==70 && i71==71 && i72==72 && i73==73 && i74==74 && i75==75 && i76==76 && i77==77 && i78==78 && i79==79 && i80==80 && i81==81 && i82==82 && i83==83 && i84==84 && i85==85 && i86==86 && i87==87 && i88==88 && i89==89 && i90==90 && i91==91 && i92==92 && i93==93 && i94==94 && i95==95 && i96==96 && i97==97 && i98==98 && i99==99;\n"
				+ "        }\n"
				+ "        System.out.print(match);\n"
				+ "    }\n"
				+ "} "
				},
				"----------\n" +
				"1. ERROR in X.java (at line 105)\n" +
				"	R99[] array = {new R99(new R98(new R97(new R96(new R95(new R94(new R93(new R92(new R91(new R90(new R89(new R88(new R87(new R86(new R85(new R84(new R83(new R82(new R81(new R80(new R79(new R78(new R77(new R76(new R75(new R74(new R73(new R72(new R71(new R70(new R69(new R68(new R67(new R66(new R65(new R64(new R63(new R62(new R61(new R60(new R59(new R58(new R57(new R56(new R55(new R54(new R53(new R52(new R51(new R50(new R49(new R48(new R47(new R46(new R45(new R44(new R43(new R42(new R41(new R40(new R39(new R38(new R37(new R36(new R35(new R34(new R33(new R32(new R31(new R30(new R29(new R28(new R27(new R26(new R25(new R24(new R23(new R22(new R21(new R20(new R19(new R18(new R17(new R16(new R15(new R14(new R13(new R12(new R11(new R10(new R9(new R8(new R7(new R6(new R5(new R4(new R3(new R2(new R1(new R0(\"\"), 1), 2), 3), 4), 5), 6), 7), 8), 9), 10), 11), 12), 13), 14), 15), 16), 17), 18), 19), 20), 21), 22), 23), 24), 25), 26), 27), 28), 29), 30), 31), 32), 33), 34), 35), 36), 37), 38), 39), 40), 41), 42), 43), 44), 45), 46), 47), 48), 49), 50), 51), 52), 53), 54), 55), 56), 57), 58), 59), 60), 61), 62), 63), 64), 65), 66), 67), 68), 69), 70), 71), 72), 73), 74), 75), 76), 77), 78), 79), 80), 81), 82), 83), 84), 85), 86), 87), 88), 89), 90), 91), 92), 93), 94), 95), 96), 97), 98), 99)};\n" +
				"	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              ^^^^^^^^^^\n" +
				"The constructor X.R0(String) is undefined\n" +
				"----------\n");
	}
	public void testIssue945_2() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "    record R0(int x) {}\n"
				+ "    record R1(R0 r, int x) {}\n"
				+ "    record R2(R1 r, int x) {}\n"
				+ "    record R3(R2 r, int x) {}\n"
				+ "    record R4(R3 r, int x) {}\n"
				+ "    record R5(R4 r, int x) {}\n"
				+ "    record R6(R5 r, int x) {}\n"
				+ "    record R7(R6 r, int x) {}\n"
				+ "    record R8(R7 r, int x) {}\n"
				+ "    record R9(R8 r, int x) {}\n"
				+ "    record R10(R9 r, int x) {}\n"
				+ "    record R11(R10 r, int x) {}\n"
				+ "    record R12(R11 r, int x) {}\n"
				+ "    record R13(R12 r, int x) {}\n"
				+ "    record R14(R13 r, int x) {}\n"
				+ "    record R15(R14 r, int x) {}\n"
				+ "    record R16(R15 r, int x) {}\n"
				+ "    record R17(R16 r, int x) {}\n"
				+ "    record R18(R17 r, int x) {}\n"
				+ "    record R19(R18 r, int x) {}\n"
				+ "    record R20(R19 r, int x) {}\n"
				+ "    record R21(R20 r, int x) {}\n"
				+ "    record R22(R21 r, int x) {}\n"
				+ "    record R23(R22 r, int x) {}\n"
				+ "    record R24(R23 r, int x) {}\n"
				+ "    record R25(R24 r, int x) {}\n"
				+ "    record R26(R25 r, int x) {}\n"
				+ "    record R27(R26 r, int x) {}\n"
				+ "    record R28(R27 r, int x) {}\n"
				+ "    record R29(R28 r, int x) {}\n"
				+ "    record R30(R29 r, int x) {}\n"
				+ "    record R31(R30 r, int x) {}\n"
				+ "    record R32(R31 r, int x) {}\n"
				+ "    record R33(R32 r, int x) {}\n"
				+ "    record R34(R33 r, int x) {}\n"
				+ "    record R35(R34 r, int x) {}\n"
				+ "    record R36(R35 r, int x) {}\n"
				+ "    record R37(R36 r, int x) {}\n"
				+ "    record R38(R37 r, int x) {}\n"
				+ "    record R39(R38 r, int x) {}\n"
				+ "    record R40(R39 r, int x) {}\n"
				+ "    record R41(R40 r, int x) {}\n"
				+ "    record R42(R41 r, int x) {}\n"
				+ "    record R43(R42 r, int x) {}\n"
				+ "    record R44(R43 r, int x) {}\n"
				+ "    record R45(R44 r, int x) {}\n"
				+ "    record R46(R45 r, int x) {}\n"
				+ "    record R47(R46 r, int x) {}\n"
				+ "    record R48(R47 r, int x) {}\n"
				+ "    record R49(R48 r, int x) {}\n"
				+ "    record R50(R49 r, int x) {}\n"
				+ "    record R51(R50 r, int x) {}\n"
				+ "    record R52(R51 r, int x) {}\n"
				+ "    record R53(R52 r, int x) {}\n"
				+ "    record R54(R53 r, int x) {}\n"
				+ "    record R55(R54 r, int x) {}\n"
				+ "    record R56(R55 r, int x) {}\n"
				+ "    record R57(R56 r, int x) {}\n"
				+ "    record R58(R57 r, int x) {}\n"
				+ "    record R59(R58 r, int x) {}\n"
				+ "    record R60(R59 r, int x) {}\n"
				+ "    record R61(R60 r, int x) {}\n"
				+ "    record R62(R61 r, int x) {}\n"
				+ "    record R63(R62 r, int x) {}\n"
				+ "    record R64(R63 r, int x) {}\n"
				+ "    record R65(R64 r, int x) {}\n"
				+ "    record R66(R65 r, int x) {}\n"
				+ "    record R67(R66 r, int x) {}\n"
				+ "    record R68(R67 r, int x) {}\n"
				+ "    record R69(R68 r, int x) {}\n"
				+ "    record R70(R69 r, int x) {}\n"
				+ "    record R71(R70 r, int x) {}\n"
				+ "    record R72(R71 r, int x) {}\n"
				+ "    record R73(R72 r, int x) {}\n"
				+ "    record R74(R73 r, int x) {}\n"
				+ "    record R75(R74 r, int x) {}\n"
				+ "    record R76(R75 r, int x) {}\n"
				+ "    record R77(R76 r, int x) {}\n"
				+ "    record R78(R77 r, int x) {}\n"
				+ "    record R79(R78 r, int x) {}\n"
				+ "    record R80(R79 r, int x) {}\n"
				+ "    record R81(R80 r, int x) {}\n"
				+ "    record R82(R81 r, int x) {}\n"
				+ "    record R83(R82 r, int x) {}\n"
				+ "    record R84(R83 r, int x) {}\n"
				+ "    record R85(R84 r, int x) {}\n"
				+ "    record R86(R85 r, int x) {}\n"
				+ "    record R87(R86 r, int x) {}\n"
				+ "    record R88(R87 r, int x) {}\n"
				+ "    record R89(R88 r, int x) {}\n"
				+ "    record R90(R89 r, int x) {}\n"
				+ "    record R91(R90 r, int x) {}\n"
				+ "    record R92(R91 r, int x) {}\n"
				+ "    record R93(R92 r, int x) {}\n"
				+ "    record R94(R93 r, int x) {}\n"
				+ "    record R95(R94 r, int x) {}\n"
				+ "    record R96(R95 r, int x) {}\n"
				+ "    record R97(R96 r, int x) {}\n"
				+ "    record R98(R97 r, int x) {}\n"
				+ "    record R99(R98 r, int x) {}\n"
				+ "    @SuppressWarnings(\"preview\")\n"
				+ "    public static void main(String args[]) {\n"
				+ "        boolean match = false;\n"
				+ "        R99[] array = {new R99(new R98(new R97(new R96(new R95(new R94(new R93(new R92(new R91(new R90(new R89(new R88(new R87(new R86(new R85(new R84(new R83(new R82(new R81(new R80(new R79(new R78(new R77(new R76(new R75(new R74(new R73(new R72(new R71(new R70(new R69(new R68(new R67(new R66(new R65(new R64(new R63(new R62(new R61(new R60(new R59(new R58(new R57(new R56(new R55(new R54(new R53(new R52(new R51(new R50(new R49(new R48(new R47(new R46(new R45(new R44(new R43(new R42(new R41(new R40(new R39(new R38(new R37(new R36(new R35(new R34(new R33(new R32(new R31(new R30(new R29(new R28(new R27(new R26(new R25(new R24(new R23(new R22(new R21(new R20(new R19(new R18(new R17(new R16(new R15(new R14(new R13(new R12(new R11(new R10(new R9(new R8(new R7(new R6(new R5(new R4(new R3(new R2(new R1(new R0(0), 1), 2), 3), 4), 5), 6), 7), 8), 9), 10), 11), 12), 13), 14), 15), 16), 17), 18), 19), 20), 21), 22), 23), 24), 25), 26), 27), 28), 29), 30), 31), 32), 33), 34), 35), 36), 37), 38), 39), 40), 41), 42), 43), 44), 45), 46), 47), 48), 49), 50), 51), 52), 53), 54), 55), 56), 57), 58), 59), 60), 61), 62), 63), 64), 65), 66), 67), 68), 69), 70), 71), 72), 73), 74), 75), 76), 77), 78), 79), 80), 81), 82), 83), 84), 85), 86), 87), 88), 89), 90), 91), 92), 93), 94), 95), 96), 97), 98), 99)};\n"
				+ "        for (R99(R98(R97(R96(R95(R94(R93(R92(R91(R90(R89(R88(R87(R86(R85(R84(R83(R82(R81(R80(R79(R78(R77(R76(R75(R74(R73(R72(R71(R70(R69(R68(R67(R66(R65(R64(R63(R62(R61(R60(R59(R58(R57(R56(R55(R54(R53(R52(R51(R50(R49(R48(R47(R46(R45(R44(R43(R42(R41(R40(R39(R38(R37(R36(R35(R34(R33(R32(R31(R30(R29(R28(R27(R26(R25(R24(R23(R22(R21(R20(R19(R18(R17(R16(R15(R14(R13(R12(R11(R10(R9(R8(R7(R6(R5(R4(R3(R2(R1(R0(int i0), int i1), int i2), int i3), int i4), int i5), int i6), int i7), int i8), int i9), int i10), int i11), int i12), int i13), int i14), int i15), int i16), int i17), int i18), int i19), int i20), int i21), int i22), int i23), int i24), int i25), int i26), int i27), int i28), int i29), int i30), int i31), int i32), int i33), int i34), int i35), int i36), int i37), int i38), int i39), int i40), int i41), int i42), int i43), int i44), int i45), int i46), int i47), int i48), int i49), int i50), int i51), int i52), int i53), int i54), int i55), int i56), int i57), int i58), int i59), int i60), int i61), int i62), int i63), int i64), int i65), int i66), int i67), int i68), int i69), int i70), int i71), int i72), int i73), int i74), int i75), int i76), int i77), int i78), int i79), int i80), int i81), int i82), int i83), int i84), int i85), int i86), int i87), int i88), int i89), int i90), int i91), int i92), int i93), int i94), int i95), int i96), int i97), int i98), int i99) : array) {\n"
				+ "            match = i0==0 && i1==1 && i2==2 && i3==3 && i4==4 && i5==5 && i6==6 && i7==7 && i8==8 && i9==9 && i10==10 && i11==11 && i12==12 && i13==13 && i14==14 && i15==15 && i16==16 && i17==17 && i18==18 && i19==19 && i20==20 && i21==21 && i22==22 && i23==23 && i24==24 && i25==25 && i26==26 && i27==27 && i28==28 && i29==29 && i30==30 && i31==31 && i32==32 && i33==33 && i34==34 && i35==35 && i36==36 && i37==37 && i38==38 && i39==39 && i40==40 && i41==41 && i42==42 && i43==43 && i44==44 && i45==45 && i46==46 && i47==47 && i48==48 && i49==49 && i50==50 && i51==51 && i52==52 && i53==53 && i54==54 && i55==55 && i56==56 && i57==57 && i58==58 && i59==59 && i60==60 && i61==61 && i62==62 && i63==63 && i64==64 && i65==65 && i66==66 && i67==67 && i68==68 && i69==69 && i70==70 && i71==71 && i72==72 && i73==73 && i74==74 && i75==75 && i76==76 && i77==77 && i78==78 && i79==79 && i80==80 && i81==81 && i82==82 && i83==83 && i84==84 && i85==85 && i86==86 && i87==87 && i88==88 && i89==89 && i90==90 && i91==91 && i92==92 && i93==93 && i94==94 && i95==95 && i96==96 && i97==97 && i98==98 && i99==99;\n"
				+ "        }\n"
				+ "        System.out.print(match);\n"
				+ "    }\n"
				+ "} "
				},
				"true");
	}
}
