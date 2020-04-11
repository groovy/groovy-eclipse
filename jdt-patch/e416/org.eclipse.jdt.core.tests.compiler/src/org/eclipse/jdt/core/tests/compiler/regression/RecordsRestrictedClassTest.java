/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
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

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class RecordsRestrictedClassTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug560798_002" };
	}

	public static Class<?> testClass() {
		return RecordsRestrictedClassTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_14);
	}
	public RecordsRestrictedClassTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_14); // FIXME
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_14);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_14);
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
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("14");
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview("14"));
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
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview("14") :
			JavacTestOptions.forReleaseWithPreview("14", javacAdditionalTestOptions);
		runner.runWarningTest();
	}

	private static void verifyClassFile(String expectedOutput, String classFileName, int mode)
			throws IOException, ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
			System.out.println("...");
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}

	public void testBug550750_001() {
		runConformTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int x, int y){\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n"+
				"abstract record Point(int x, int y){\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	abstract record Point(int x, int y){\n" +
			"	                ^^^^^\n" +
			"Illegal modifier for the record Point; only public, final and strictfp are permitted\n" +
			"----------\n");
	}
	/* A record declaration is implicitly final. It is permitted for the declaration of
	 * a record type to redundantly specify the final modifier. */
	public void testBug550750_003() {
		runConformTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"final record Point(int x, int y){\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n"+
				"final final record Point(int x, int y){\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	final final record Point(int x, int y){\n" +
			"	                   ^^^^^\n" +
			"Duplicate modifier for the type Point\n" +
			"----------\n");
	}
	public void testBug550750_005() {
		runConformTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"final record Point(int x, int y){\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public public record X(int x, int y){\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public public record X(int x, int y){\n" +
			"	                     ^\n" +
			"Duplicate modifier for the type X\n" +
			"----------\n");
	}
	public void testBug550750_007() {
		runConformTest(
				new String[] {
						"X.java",
						"final record Point(int x, int y){\n"+
						"  public void foo() {}\n"+
						"}\n"+
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_008() {
		runConformTest(
				new String[] {
						"X.java",
						"final record Point(int x, int y){\n"+
						"  public Point {}\n"+
						"}\n"+
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_009() {
		runConformTest(
				new String[] {
						"X.java",
						"final record Point(int x, int y){\n"+
						"  public Point {}\n"+
						"  public void foo() {}\n"+
						"}\n"+
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}"
				},
			"0");
	}
	 /* nested record implicitly static*/
	public void testBug550750_010() {
		runConformTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"  record Point(int x, int y){\n"+
						"  }\n"+
						"}\n"
				},
			"0");
	}
	 /* nested record explicitly static*/
	public void testBug550750_011() {
		runConformTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"  static record Point(int x, int y){\n"+
						"  }\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_012() {
		runConformTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int ... x){\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_013() {
		runConformTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.Target;\n"+
						"import java.lang.annotation.ElementType;\n"+
						"record Point(@MyAnnotation int myInt, char myChar) {}\n"+
						" @Target({ElementType.FIELD, ElementType.TYPE})\n"+
						" @interface MyAnnotation {}\n" +
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_014() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, char myChar) {\n"+
						"  public int myInt(){\n"+
						"     return this.myInt;\n" +
						"  }\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_015() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, char myChar) implements I {\n"+
						"  public int myInt(){\n"+
						"     return this.myInt;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"0");
	}
	public void testBug550750_016() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, char myChar) implements I {\n"+
						"}\n" +
						"interface I {}\n"
				},
			"0");
	}
	public void testBug550750_017() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, char myChar) implements I {\n"+
						"  public Point(int myInt, char myChar){\n"+
						"     this.myInt = myInt;\n" +
						"     this.myChar = myChar;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"0");
	}
	public void testBug550750_018() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, char myChar) implements I {\n"+
						"  public Point(int myInt, char myChar){\n"+
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public Point(int myInt, char myChar){\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The blank final field myChar may not have been initialized\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	public Point(int myInt, char myChar){\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The blank final field myInt may not have been initialized\n" +
			"----------\n");
	}
	public void testBug550750_019() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, char myChar) implements I {\n"+
						"  private Point {\n"+
						"     this.myInt = myInt;\n" +
						"     this.myChar = myChar;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	private Point {\n" +
			"	        ^^^^^\n" +
			"The canonical constructor Point of a record declaration must be declared public.\n" +
			"----------\n");
	}
	public void testBug550750_020() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, char myChar) implements I {\n"+
						"  protected Point {\n"+
						"     this.myInt = myInt;\n" +
						"     this.myChar = myChar;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	protected Point {\n" +
			"	          ^^^^^\n" +
			"The canonical constructor Point of a record declaration must be declared public.\n" +
			"----------\n");
	}
	public void testBug550750_021() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, char myChar) implements I {\n"+
						"  public Point {\n"+
						"     this.myInt = myInt;\n" +
						"     this.myChar = myChar;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"0");
	}
	public void testBug550750_022() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, char myChar) implements I {\n"+
						"  public Point {\n"+
						"     this.myInt = myInt;\n" +
						"     this.myChar = myChar;\n" +
						"     return;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	return;\n" +
			"	^^^^^^^\n" +
			"The body of a compact constructor must not contain a return statement\n" +
			"----------\n");
	}
	public void testBug550750_023() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int finalize) implements I {\n"+
						"  public Point {\n"+
						"     this.myInt = myInt;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	record Point(int myInt, int finalize) implements I {\n" +
			"	                            ^^^^^^^^\n" +
			"Illegal component name finalize in record Point;\n" +
			"----------\n");
	}
	public void testBug550750_024() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int finalize, int myZ) implements I {\n"+
						"  public Point {\n"+
						"     this.myInt = myInt;\n" +
						"     this.myZ = myZ;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	record Point(int myInt, int finalize, int myZ) implements I {\n" +
			"	                            ^^^^^^^^\n" +
			"Illegal component name finalize in record Point;\n" +
			"----------\n");
	}
	public void testBug550750_025() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myZ, int myZ) implements I {\n"+
						"  public Point {\n"+
						"     this.myInt = myInt;\n" +
						"     this.myZ = myZ;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	record Point(int myInt, int myZ, int myZ) implements I {\n" +
			"	                                     ^^^\n" +
			"Duplicate component myZ in record\n" +
			"----------\n");
	}
	public void testBug550750_026() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myInt, int myInt, int myZ) implements I {\n"+
						"  public Point {\n"+
						"     this.myInt = myInt;\n" +
						"     this.myZ = myZ;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	record Point(int myInt, int myInt, int myInt, int myZ) implements I {\n" +
			"	                            ^^^^^\n" +
			"Duplicate component myInt in record\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	record Point(int myInt, int myInt, int myInt, int myZ) implements I {\n" +
			"	                                       ^^^^^\n" +
			"Duplicate component myInt in record\n" +
			"----------\n");
	}
	public void testBug550750_027() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myZ) implements I {\n"+
						"  static final int z;\n"+
						"  public Point {\n"+
						"     this.myInt = myInt;\n" +
						"     this.myZ = myZ;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	static final int z;\n" +
			"	                 ^\n" +
			"The blank final field z may not have been initialized\n" +
			"----------\n");
	}
	public void testBug550750_028() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myZ) implements I {\n"+
						"  int z;\n"+
						"  public Point {\n"+
						"     this.myInt = myInt;\n" +
						"     this.myZ = myZ;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	int z;\n" +
			"	    ^\n" +
			"User declared non-static fields z are not permitted in a record\n" +
			"----------\n");
	}
	public void testBug550750_029() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myZ) implements I {\n"+
						"  public Point {\n"+
						"     this.myInt = myInt;\n" +
						"     this.myZ = myZ;\n" +
						"  }\n"+
						"  public native void foo();\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	public native void foo();\n" +
			"	                   ^^^^^\n" +
			"Illegal modifier native for method foo; native methods are not allowed in record\n" +
			"----------\n");
	}
	public void testBug550750_030() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myZ) implements I {\n"+
						"  {\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	{\n" +
			"     System.out.println(0);\n" +
			"  }\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Instance Initializer is not allowed in a record declaration\n" +
			"----------\n");
	}
	public void testBug550750_031() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myZ) implements I {\n"+
						"  static {\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"0");
	}
	public void testBug550750_032() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"class record {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class record {\n" +
			"	      ^^^^^^\n" +
			"Record is a restricted identifier and hence not a valid type name\n" +
			"----------\n");
	}
	public void testBug550750_033() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"class X<record> {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X<record> {\n" +
			"	        ^^^^^^\n" +
			"Record is a restricted identifier and hence not a valid type name\n" +
			"----------\n");
	}
	public void testBug550750_034() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"  public <record> void foo(record args){}\n"+
						"}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	public <record> void foo(record args){}\n" +
			"	        ^^^^^^\n" +
			"Record is a restricted identifier and hence not a valid type name\n" +
			"----------\n");
	}
	public void testBug550750_035() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"  public void foo(record args){}\n"+
						"}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	public void foo(record args){}\n" +
			"	                ^^^^^^\n" +
			"record cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	public void foo(record args){}\n" +
			"	                ^^^^^^\n" +
			"Record is a restricted identifier and hence not a valid type name\n" +
			"----------\n");
	}
	public void testBug550750_036() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"     I lambda = (record r) -> {};\n"+
						"  }\n"+
						"}\n" +
						"interface I {\n" +
						"  public void apply(int i);\n" +
						"}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	I lambda = (record r) -> {};\n" +
			"	           ^^^^^^^^^^^^^\n" +
			"This lambda expression refers to the missing type record\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	I lambda = (record r) -> {};\n" +
			"	            ^^^^^^\n" +
			"record cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	I lambda = (record r) -> {};\n" +
			"	            ^^^^^^\n" +
			"Record is a restricted identifier and hence not a valid type name\n" +
			"----------\n");
	}
	public void testBug550750_037() {
		runConformTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(){\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_038() {
		runConformTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(){\n"+
						"   public Point {}\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_039() {
		runConformTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(){\n"+
						"   public Point() {}\n"+
						"}\n"
				},
			"0");
	}
	public void testBug550750_040() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(){\n"+
						"   private int f;\n"+
 						"   public Point() {}\n"+
						"}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	private int f;\n" +
			"	            ^\n" +
			"User declared non-static fields f are not permitted in a record\n" +
			"----------\n");
	}
	public void testBug550750_041() {
		runConformTest(
				new String[] {
						"X.java",
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(){\n"+
						"   static int f;\n"+
						"   public Point() {}\n"+
						"}\n"
				},
			"0");
	}
	public void testBug553152_001() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myZ) implements I {\n"+
						"  public char myInt() {;\n" +
						"     return 'c';\n" +
						"  }\n"+
						"  public int getmyInt() {;\n" +
						"     return this.myInt;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public char myInt() {;\n" +
			"	       ^^^^\n" +
			"Illegal return type of accessor; should be the same as the declared type int of the record component\n" +
			"----------\n");
	}
	public void testBug553152_002() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  public java.lang.Integer myInt() {;\n" +
						"     return this.myInt;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"0");
	}
	public void testBug553152_003() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myZ) implements I {\n"+
						"  public <T> int myInt() {;\n" +
						"     return this.myInt;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public <T> int myInt() {;\n" +
			"	               ^^^^^^^\n" +
			"The accessor method must not be generic\n" +
			"----------\n");
	}
	public void testBug553152_004() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myZ) implements I {\n"+
						"  private int myInt() {;\n" +
						"     return this.myInt;\n" +
						"  }\n"+
						"  /* package */ int myZ() {;\n" +
						"     return this.myZ;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	private int myInt() {;\n" +
			"	            ^^^^^^^\n" +
			"The accessor method must be declared public\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	/* package */ int myZ() {;\n" +
			"	                  ^^^^^\n" +
			"The accessor method must be declared public\n" +
			"----------\n");
	}
	public void testBug553152_005() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myZ) implements I {\n"+
						"  public int myInt() throws Exception {;\n" +
						"     return this.myInt;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public int myInt() throws Exception {;\n" +
			"	           ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Throws clause not allowed for explicitly declared accessor method\n" +
			"----------\n");
	}
	public void testBug553152_006() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  public Point(Integer myInt, int myZ) {\n" +
						"     this.myInt = 0;\n" +
						"     this.myZ = 0;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"0");
	}
	public void testBug553152_007() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  public Point(Integer myInt, int myZ) {\n" +
						"     this.myInt = 0;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public Point(Integer myInt, int myZ) {\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The blank final field myZ may not have been initialized\n" +
			"----------\n");
	}
	public void testBug553152_008() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  public Point {\n" +
						"     this.myInt = 0;\n" +
						"     this.myZ = 0;\n" +
						"  }\n"+
						"  public Point(Integer myInt, int myZ) {\n" +
						"     this.myInt = 0;\n" +
						"     this.myZ = 0;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public Point {\n" +
			"	       ^^^^^\n" +
			"Duplicate method Point(Integer, int) in type Point\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	public Point(Integer myInt, int myZ) {\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Duplicate method Point(Integer, int) in type Point\n" +
			"----------\n");
	}
	public void testBug553152_009() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  Point(Integer myInt, int myZ) {\n" +
						"     this.myInt = 0;\n" +
						"     this.myZ = 0;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	Point(Integer myInt, int myZ) {\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The canonical constructor Point of a record declaration must be declared public.\n" +
			"----------\n");
	}
	public void testBug553152_010() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  public <T> Point(Integer myInt, int myZ) {\n" +
						"     this.myInt = 0;\n" +
						"     this.myZ = 0;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public <T> Point(Integer myInt, int myZ) {\n" +
			"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Canonical constructor Point of a record declaration should not be generic\n" +
			"----------\n");
	}
	public void testBug553152_011() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  public Point(Integer myInt, int myZ) throws Exception {\n" +
						"     this.myInt = 0;\n" +
						"     this.myZ = 0;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public Point(Integer myInt, int myZ) throws Exception {\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Throws clause not allowed for canonical constructor Point\n" +
			"----------\n");
	}
	public void testBug553152_012() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  public Point {\n" +
						"     this.myInt = 0;\n" +
						"     this.myZ = 0;\n" +
						"     return;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	return;\n" +
			"	^^^^^^^\n" +
			"The body of a compact constructor must not contain a return statement\n" +
			"----------\n");
	}
	public void testBug553152_013() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  public Point(Integer myInt, int myZ) {\n" +
						"     this.myInt = 0;\n" +
						"     this.myZ = 0;\n" +
						"     I i = () -> { return;};\n" +
						"     Zork();\n" +
						"  }\n"+
						"  public void apply() {}\n" +
						"}\n" +
						"interface I { void apply();}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type Point\n" +
			"----------\n");
	}
	public void testBug553152_014() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  public Point(Integer myInt, int myZ) {\n" +
						"     super();\n" +
						"     this.myInt = 0;\n" +
						"     this.myZ = 0;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"The body of a canonical constructor must not contain an explicit constructor call\n" +
			"----------\n");
	}
	public void testBug553152_015() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  public Point(Integer myInt, int myZ) {\n" +
						"     this.Point(0);\n" +
						"     this.myInt = 0;\n" +
						"     this.myZ = 0;\n" +
						"  }\n"+
						"  public Point(Integer myInt) {}\n" +
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	this.Point(0);\n" +
			"	     ^^^^^\n" +
			"The method Point(int) is undefined for the type Point\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 12)\n" +
			"	public Point(Integer myInt) {}\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^\n" +
			"The blank final field myInt may not have been initialized\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 12)\n" +
			"	public Point(Integer myInt) {}\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^\n" +
			"The blank final field myZ may not have been initialized\n" +
			"----------\n");
	}
	public void testBug553152_016() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(Integer myInt, int myZ) implements I {\n"+
						"  public Point {\n" +
						"     super();\n" +
						"     this.myInt = 0;\n" +
						"     this.myZ = 0;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"The body of a compact constructor must not contain an explicit constructor call\n" +
			"----------\n");
	}
	public void testBug553152_017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  public class Inner {\n"+
				"    record Point(int myInt, char myChar) {}\n"+
				"  }\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n"+
				"  }\n"+
				"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	record Point(int myInt, char myChar) {}\n" +
		"	       ^^^^^\n" +
		"Nested Record is (implicitly) static and hence enclosing type should be static\n" +
		"----------\n");
	}
	public void _testBug553152_018() {
		runConformTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.Target;\n"+
						"import java.lang.annotation.ElementType;\n"+
						"class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, char myChar) {}\n"+
						" @Target({ElementType.FIELD, ElementType.TYPE})\n"+
						" @interface MyAnnotation {}\n"
				},
			"0");
	}
	public void testBug553152_019() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n"+
						"  public static void main(String[] args){\n"+
						"     System.out.println(0);\n" +
						"  }\n"+
						"}\n"+
						"record Point(int myInt, int myZ) implements I {\n"+
						"  public static int myInt() {;\n" +
						"     return 0;\n" +
						"  }\n"+
						"}\n" +
						"interface I {}\n"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public static int myInt() {;\n" +
			"	                  ^^^^^^^\n" +
			"The accessor method must not be static\n" +
			"----------\n");
	}
public void testBug553153_01() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n"+
				"record Point(int myInt, char myChar) implements I {\n"+
				"  public Point {\n"+
				"     this.myInt = myInt;\n" +
				"  }\n"+
				"}\n" +
				"interface I {}\n"
			},
			"0");
	}
	public void testBug553153_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n"+
				"  }\n"+
				"}\n"+
				"record Point(int myInt, char myChar) implements I {\n"+
				"  public Point {\n"+
				"	this.myInt = myInt;\n" +
				"	if (this.myInt > 0)  // conditional assignment\n" +
				"		this.myChar = myChar;\n" +
				"  }\n"+
				"}\n" +
				"interface I {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	public Point {\n" +
		"	       ^^^^^\n" +
		"The blank final field myChar may not have been initialized\n" +
		"----------\n");
}
public void testBug553153_003() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"+
			"record Point(int myInt, char myChar) implements I {\n"+
			"  static int f;\n"+
			"  public Point {\n"+
			"     this.myInt = myInt;\n" +
			"  }\n"+
			"}\n" +
			"interface I {}\n"
		},
	 "0");
}
public void testBug553153_004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n"+
			"  }\n"+
			"}\n"+
			"record Point(int myInt, char myChar) implements I {\n"+
			"  public Point(int myInt, char myChar) {\n"+
			"	this.myInt = myInt;\n" +
			"  }\n"+
			"}\n" +
			"interface I {}\n"
	},
	"----------\n" +
	"1. ERROR in X.java (at line 7)\n" +
	"	public Point(int myInt, char myChar) {\n" +
	"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
	"The blank final field myChar may not have been initialized\n" +
	"----------\n");
}
public void testBug558069_001() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"+
					"private record Point(){\n"+
					"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	private record Point(){\n" +
			"	               ^^^^^\n" +
			"Illegal modifier for the record Point; only public, final and strictfp are permitted\n" +
			"----------\n");
}
public void testBug558069_002() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"private record Point(){\n"+
			"}\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
	 "0");
}
public void testBug558069_003() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"private record Point(int myInt){\n"+
			"}\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
	 "0");
}
public void testBug558343_001() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"private record Point(int myInt){\n"+
			"  @Override\n"+
			"  public boolean equals(Object obj){\n"+
			"     return false;\n" +
			"  }\n"+
			"}\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
	 "0");
}
public void testBug558343_002() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"private record Point(int myInt){\n"+
			"  @Override\n"+
			"  public int hashCode(){\n"+
			"     return java.util.Arrays.hashCode(new int[]{Integer.valueOf(this.myInt).hashCode()});\n" +
			"  }\n"+
			"}\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
	 "0");
}
public void testBug558343_003() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n" +
			"record Point(int myInt){\n"+
			"  @Override\n"+
			"  public String toString(){\n"+
			"     return \"Point@1\";\n" +
			"  }\n"+
			"}\n"
		},
	 "0");
}
public void testBug558343_004() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(new Point(0).myInt());\n" +
			"  }\n"+
			"}\n" +
			"record Point(int myInt){\n"+
			"  @Override\n"+
			"  public String toString(){\n"+
			"     return \"Point@1\";\n" +
			"  }\n"+
			"}\n"
		},
	 "0");
}
public void testBug558494_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(new Point(0).heyPinkCity());\n" +
			"  }\n"+
			"}\n" +
			"record Point(int heyPinkCity){\n"+
			"  @Override\n"+
			"  public String toString(){\n"+
			"     return \"Point@1\";\n" +
			"  }\n"+
			"}\n"
		},
	 "0");
	String expectedOutput = "Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int heyPinkCity;\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug558494_002() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(new Point().toString());\n" +
			"  }\n"+
			"}\n" +
			"record Point(){\n"+
			"  @Override\n"+
			"  public String toString(){\n"+
			"     return \"Point@1\";\n" +
			"  }\n"+
			"}\n"
		},
	 "Point@1");
	String expectedOutput = "Record: #Record\n" +
			"Components:\n" +
			"  \n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug558494_003() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"record Forts(String...wonders){\n"+
			"}\n"+
			"public class X {\n"+
			"       public static void main(String[] args) {\n"+
			"               Forts p = new Forts(new String[] {\"Amber\", \"Nahargarh\", \"Jaigarh\"});\n"+
			"               if (!p.toString().startsWith(\"Forts[wonders=[Ljava.lang.String;@\"))\n"+
			"                   System.out.println(\"Error\");\n"+
			"       }\n"+
			"}\n"
		},
		"");
	String expectedOutput = "Record: #Record\n" +
			"Components:\n" +
			"  \n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Forts.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug558494_004() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"record Forts(int x, String[] wonders){\n"+
			"}\n"+
			"public class X {\n"+
			"       public static void main(String[] args) {\n"+
			"               Forts p = new Forts(3, new String[] {\"Amber\", \"Nahargarh\", \"Jaigarh\"});\n"+
			"               if (!p.toString().startsWith(\"Forts[x=3, wonders=[Ljava.lang.String;@\"))\n"+
			"                   System.out.println(\"Error\");\n"+
			"       }\n"+
			"}\n"
		},
		"");
	String expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int x;\n" +
			"// Component descriptor #8 [Ljava/lang/String;\n" +
			"java.lang.String[] wonders;\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Forts.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug558764_001() {
	runConformTest(
			new String[] {
					"X.java",
					"import java.lang.annotation.Target;\n"+
					"import java.lang.annotation.ElementType;\n"+
					"record Point(@MyAnnotation int myInt, char myChar) {}\n"+
					" @Target({ElementType.FIELD})\n"+
					" @interface MyAnnotation {}\n" +
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"
			},
		"0");
}
public void testBug558764_002() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.lang.annotation.Target;\n"+
					"import java.lang.annotation.ElementType;\n"+
					"record Point(@MyAnnotation int myInt, char myChar) {}\n"+
					" @Target({ElementType.TYPE})\n"+
					" @interface MyAnnotation {}\n" +
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	record Point(@MyAnnotation int myInt, char myChar) {}\n" +
			"	             ^^^^^^^^^^^^^\n" +
			"The annotation @MyAnnotation is disallowed for this location\n" +
			"----------\n");
}
public void testBug558764_003() {
	runConformTest(
			new String[] {
					"X.java",
					"import java.lang.annotation.Target;\n"+
					"import java.lang.annotation.ElementType;\n"+
					"record Point(@MyAnnotation int myInt, char myChar) {}\n"+
					" @Target({ElementType.RECORD_COMPONENT})\n"+
					" @interface MyAnnotation {}\n" +
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"
			},
		"0");
}
public void testBug558764_004() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"import java.lang.annotation.Target;\n"+
					"import java.lang.annotation.ElementType;\n"+
					"record Point(@MyAnnotation int myInt, char myChar) {}\n"+
					" @Target({ElementType.RECORD_COMPONENT})\n"+
					" @interface MyAnnotation {}\n" +
					"class X {\n"+
					"  public @MyAnnotation String f = \"hello\";\n" +
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	public @MyAnnotation String f = \"hello\";\n" +
			"	       ^^^^^^^^^^^^^\n" +
			"The annotation @MyAnnotation is disallowed for this location\n" +
			"----------\n");
}
public void testBug553567_001() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"class X extends Record{\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X extends Record{\n" +
			"	                ^^^^^^\n" +
			"The type X may not subclass Record explicitly\n" +
			"----------\n");
}
public void testBug553567_002() {
	runConformTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n" +
					"class Record {\n"+
					"}\n"
			},
		"0");
}
public void testBug559281_001() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"record X(void k) {}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	record X(void k) {}\n" +
			"	       ^\n" +
			"void is an invalid type for the component k of a record\n" +
			"----------\n");
}
public void testBug559281_002() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"record X(int clone, int wait) {}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	record X(int clone, int wait) {}\n" +
			"	             ^^^^^\n" +
			"Illegal component name clone in record X;\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	record X(int clone, int wait) {}\n" +
			"	                        ^^^^\n" +
			"Illegal component name wait in record X;\n" +
			"----------\n");
}
public void testBug559448_001() {
	runConformTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"+
					"record Point(int x, int... y){\n"+
					"}\n"
			},
		"0");
}
public void testBug559448_002() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"+
					"record Point(int... x, int y){\n"+
					"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	record Point(int... x, int y){\n" +
			"	                    ^\n" +
			"The variable argument type int of the record Point must be the last parameter\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	record Point(int... x, int y){\n" +
			"	                    ^\n" +
			"The variable argument type int of the method Point must be the last parameter\n" +
			"----------\n");
}
public void testBug559448_003() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"+
					"record Point(int... x, int... y){\n"+
					"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	record Point(int... x, int... y){\n" +
			"	                    ^\n" +
			"The variable argument type int of the record Point must be the last parameter\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	record Point(int... x, int... y){\n" +
			"	                    ^\n" +
			"The variable argument type int of the method Point must be the last parameter\n" +
			"----------\n");
}
public void testBug559574_001() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"record X(int x, int XX3) {\n"+
					"       public XX3  {}\n"+
					"       public XX3(int x, int y, int z) {\n"+
					"               this.x = x;\n"+
					"               this.y = y;\n"+
					"       }\n"+
					"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public XX3  {}\n" +
			"	       ^^^\n" +
			"Return type for the method is missing\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	public XX3(int x, int y, int z) {\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Return type for the method is missing\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 3)\n" +
			"	public XX3(int x, int y, int z) {\n" +
			"	               ^\n" +
			"The parameter x is hiding a field from type X\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	this.y = y;\n" +
			"	     ^\n" +
			"y cannot be resolved or is not a field\n" +
			"----------\n");
}
public void testBug559992_001() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"record R() {\n"+
					"  public R throws Exception {\n" +
					"  }\n"+
					"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public R throws Exception {\n" +
		"	       ^^^^^^^^^^^^^^^^^^\n" +
		"Throws clause not allowed for canonical constructor R\n" +
		"----------\n");
}
public void testBug559992_002() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"record R() {\n"+
					"  public R() throws Exception {\n" +
					"  }\n"+
					"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public R() throws Exception {\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^\n" +
		"Throws clause not allowed for canonical constructor R\n" +
		"----------\n");
}
public void testBug560256_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"+
			"final protected record Point(int x, int y){\n"+
		"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	final protected record Point(int x, int y){\n" +
		"	                       ^^^^^\n" +
		"Illegal modifier for the record Point; only public, final and strictfp are permitted\n" +
		"----------\n");
}
public void testBug560256_002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"+
			"native record Point(int x, int y){\n"+
		"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	native record Point(int x, int y){\n" +
		"	              ^^^^^\n" +
		"Illegal modifier for the record Point; only public, final and strictfp are permitted\n" +
		"----------\n");
}
public void testBug560256_003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  class Inner {\n"+
			"	  record Point(int x, int y){}\n"+
			"  }\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	record Point(int x, int y){}\n" +
		"	       ^^^^^\n" +
		"Nested Record is (implicitly) static and hence enclosing type should be static\n" +
		"----------\n");
}
public void testBug560256_004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  static class Inner {\n"+
			"	  native record Point(int x, int y){}\n"+
			"  }\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	native record Point(int x, int y){}\n" +
		"	              ^^^^^\n" +
		"Illegal modifier for the record Point; only public, private, protected, static, final and strictfp are permitted\n" +
		"----------\n");
}
public void testBug560531_001() {
	runConformTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"+
					"record Point<T>(T t){\n"+
					"}\n"
			},
		"0");
}
public void testBug560531_002() {
	runConformTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"+
					"record R <T extends Integer, S extends String> (int x, T t, S s){\n"+
					"}\n"
			},
		"0");
}
public void testBug560569_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"interface Rentable { int year(); }\n"+
			"record Car(String model, int year) implements Rentable {\n"+
			"  public Car {\n"+
			"  }\n"+
			"  public String toString() {\n"+
			"    return model + \" \" + year;\n"+
			"  }\n"+
			"}\n"+
			"record Camel(int year) implements Rentable { }\n"+
			"\n"+
			"class X {\n"+
			"       String model;\n"+
			"       int year;\n"+
			"       public String toString() {\n"+
			"          return model + \" \" + year;\n"+
			"       }\n"+
			"       public static void main(String[] args) {\n"+
			"               Car car = new Car(\"Maruti\", 2000);\n"+
			"               System.out.println(car.hashCode() != 0);\n"+
			"       }\n"+
			"}\n"
		},
	 "true");
	String expectedOutput =
			"Bootstrap methods:\n" +
			"  0 : # 68 invokestatic java/lang/runtime/ObjectMethods.bootstrap:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;\n" +
			"	Method arguments:\n" +
			"		#1 Car\n" +
			"		#69 model;year\n" +
			"		#71 REF_getField model:Ljava/lang/String;\n" +
			"		#72 REF_getField year:I\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "Car.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560496_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"record R () {} \n"+
			"class X {\n"+
			"       public static void main(String[] args) {\n"+
			"               System.out.println(new R().hashCode());\n"+
			"       }\n"+
			"}\n"
		},
	 "0");
	String expectedOutput =
			"public final int hashCode();\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560496_002() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"strictfp record R () {} \n"+
			"class X {\n"+
			"       public static void main(String[] args) {\n"+
			"               System.out.println(new R().hashCode());\n"+
			"       }\n"+
			"}\n"
		},
	 "0");
	String expectedOutput =
			"public final strictfp int hashCode();\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560797_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"strictfp record R (int x, int y) {} \n"+
			"class X {\n"+
			"       public static void main(String[] args) {\n"+
			"               System.out.println(new R(100, 200).hashCode() != 0);\n"+
			"       }\n"+
			"}\n"
		},
	 "true");
	String expectedOutput =
			"public strictfp int x();\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560797_002() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"strictfp record R (int x, int y) { \n"+
			"public int x() { return this.x;}\n"+
			"}\n"+
			"class X {\n"+
			"       public static void main(String[] args) {\n"+
			"               System.out.println(new R(100, 200).hashCode() != 0);\n"+
			"       }\n"+
			"}\n"
		},
	 "true");
	String expectedOutput =
			"public strictfp int x();\n";
	RecordsRestrictedClassTest.verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560798_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.Target;\n"+
			"import java.lang.annotation.ElementType;\n"+
			"@Target({ElementType.PARAMETER})\n"+
			"@interface MyAnnot {}\n"+
			"record R(@MyAnnot()  int i, int j) {}\n" +
			"class X {\n"+
			"       public static void main(String[] args) {\n"+
			"           System.out.println(new R(100, 200).hashCode() != 0);\n"+
			"       }\n"+
			"}\n"
		},
	 "true");
}
public void testBug560798_002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.Target;\n"+
			"import java.lang.annotation.ElementType;\n"+
			"@Target({ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE,\n" +
			"	ElementType.MODULE, ElementType.PACKAGE, ElementType.TYPE, ElementType.TYPE_PARAMETER})\n"+
			"@interface MyAnnot {}\n"+
			"record R(@MyAnnot()  int i, int j) {}\n" +
			"class X {\n"+
			"       public static void main(String[] args) {\n"+
			"       }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	record R(@MyAnnot()  int i, int j) {}\n" +
		"	         ^^^^^^^^\n" +
		"The annotation @MyAnnot is disallowed for this location\n" +
		"----------\n");
}
public void testBug560798_003() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.Target;\n"+
			"import java.lang.annotation.ElementType;\n"+
			"@Target({ElementType.METHOD})\n"+
			"@interface MyAnnot {}\n"+
			"record R(@MyAnnot()  int i, int j) {}\n" +
			"class X {\n"+
			"       public static void main(String[] args) {\n"+
			"           System.out.println(new R(100, 200).hashCode() != 0);\n"+
			"       }\n"+
			"}\n"
		},
	 "true");
}
public void testBug560798_004() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.Target;\n"+
			"import java.lang.annotation.ElementType;\n"+
			"@Target({ElementType.RECORD_COMPONENT})\n"+
			"@interface MyAnnot {}\n"+
			"record R(@MyAnnot()  int i, int j) {}\n" +
			"class X {\n"+
			"       public static void main(String[] args) {\n"+
			"           System.out.println(new R(100, 200).hashCode() != 0);\n"+
			"       }\n"+
			"}\n"
		},
	 "true");
}
public void testBug560798_005() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.Target;\n"+
			"import java.lang.annotation.ElementType;\n"+
			"@Target({ElementType.TYPE_USE})\n"+
			"@interface MyAnnot {}\n"+
			"record R(@MyAnnot()  int i, int j) {}\n" +
			"class X {\n"+
			"       public static void main(String[] args) {\n"+
			"           System.out.println(new R(100, 200).hashCode() != 0);\n"+
			"       }\n"+
			"}\n"
		},
	 "true");
}
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testBug560770_001() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.ERROR);
	this.runNegativeTest(
	new String[] {
			"X.java",
			"record R() {}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	record R() {}\n" +
		"	       ^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n",
		null,
		true,
		options
	);
}
public void testBug560893_001() {
	runConformTest(
			new String[] {
				"X.java",
				"interface I{\n"+
				"record R(int x, int y) {}\n"+
				"}\n" +
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"           System.out.println(0);\n"+
				"       }\n"+
				"}\n"
			},
		 "0");
}
public void testBug560893_002() {
	runConformTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"           record R(int x, int y) {}\n"+
				"           System.out.println(0);\n"+
				"       }\n"+
				"}\n"
			},
		 "0");
}
public void testBug560893_003() {
	runConformTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"           record R(int x, int y) {}\n"+
				"           R r =  new R(100,200);\n"+
				"           System.out.println(r.x());\n"+
				"       }\n"+
				"}\n"
			},
		 "100");
}
public void testBug560893_004() {
	runConformTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"           record R(int x, int y) {\n"+
				"               static int i;\n"+
				"       	}\n"+
				"           R r =  new R(100,200);\n"+
				"           System.out.println(r.x());\n"+
				"       }\n"+
				"}\n"
			},
		 "100");
}
public void testBug560893_005() {
	runConformTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"           record R(int x, int y) {\n"+
				"               static int i;\n"+
				"               public void ff() {\n"+
				"                	int jj;\n"+
				"       		}\n"+
				"               static int ii;\n"+
				"       	}\n"+
				"           R r =  new R(100,200);\n"+
				"           System.out.println(r.x());\n"+
				"       }\n"+
				"}\n"
			},
		 "100");
}
public void testBug560893_006() {
	runConformTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"           static record R(int x, int y) {}\n"+
				"           R r =  new R(100,200);\n"+
				"           System.out.println(r.x());\n"+
				"       }\n"+
				"}\n"
			},
		 "100");
}
public void testBug560893_007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"    static int si;\n"+
			"    int nsi;\n"+
			"\n"+
			"    void m() {\n"+
			"        int li;\n"+
			"\n"+
			"        record R(int r) {\n"+
			"            void print() {\n"+
			"                System.out.println(li);  // error, local variable\n"+
			"                System.out.println(nsi); // error, non-static member\n"+
			"                System.out.println(si);  // ok, static member of enclosing class\n"+
			"            }\n"+
			"        }\n"+
			"        R r = new R(10);\n"+
			"    }\n"+
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	System.out.println(li);  // error, local variable\n" +
		"	                   ^^\n" +
		"Cannot make a static reference to the non-static variable li from a local record\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 11)\n" +
		"	System.out.println(nsi); // error, non-static member\n" +
		"	                   ^^^\n" +
		"Cannot make a static reference to the non-static field nsi\n" +
		"----------\n");
}
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testBug558718_001() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
	new String[] {
			"X.java",
			"record R() {}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	record R() {}\n" +
		"	^^^^^^\n" +
		"record is a preview feature and disabled by default. Use --enable-preview to enable\n" +
		"----------\n",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testBug558718_002() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_13);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
	new String[] {
			"X.java",
			"record R() {}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	record R() {}\n" + 
		"	^^^^^^\n" + 
		"The preview feature record is only available with source level 14 and above\n" +
		"----------\n",
		null,
		true,
		options
	);
}
public void testBug561528_001() {
	runConformTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"+
					"interface Node<N> {}\n\n"+
					"record R <N extends Node<?>> (N value){\n"+
					"}\n"
			},
		"0");
}
public void testBug561528_002() {
	runConformTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"+
					"interface Node<N> {}\n\n"+
					"record R <N extends Node<N>> (R<N> parent, N element){\n"+
					"}\n"
			},
		"0");
}
public void testBug561528_003() {
	runConformTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"+
					"interface Node<N> {}\n\n"+
					"interface AB<N> {}\n\n"+
					"record R <N extends Node<AB<N>>> (N value){\n"+
					"}\n"
			},
		"0");
}
public void testBug561528_004() {
	runConformTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"+
					"interface Node<N> {}\n\n"+
					"interface AB<N> {}\n\n"+
					"interface CD<N> {}\n\n"+
					"record R <N extends Node<AB<CD<N>>>> (N value){\n"+
					"}\n"
			},
		"0");
}
public void testBug561528_005() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"+
					"interface Node<N> {}\n\n"+
					"interface AB<N> {}\n\n"+
					"interface CD<N> {}\n\n"+
					"record R <N extends Node<AB<CD<N>>>>> (N value){\n"+
					"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	record R <N extends Node<AB<CD<N>>>>> (N value){\n" +
		"	                                ^^^\n" +
		"Syntax error on token \">>>\", >> expected\n" +
		"----------\n",
		null,
		true
	);
}
}