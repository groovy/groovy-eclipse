/*******************************************************************************
 * Copyright (c) 2019, 2025 IBM Corporation and others.
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
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class RecordsRestrictedClassTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug3504_1"};
	}

	public static Class<?> testClass() {
		return RecordsRestrictedClassTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}
	public RecordsRestrictedClassTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if (!isJRE16Plus)
			return;
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forRelease("16");
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		if (!isJRE16Plus)
			return;
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.DEFAULT);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
		runWarningTest(testFiles, expectedCompilerLog, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
			Map<String, String> customOptions, String javacAdditionalTestOptions) {
		if (!isJRE16Plus)
			return;
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.vmArguments = new String[] {};
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forRelease("16") :
			JavacTestOptions.forRelease("16", javacAdditionalTestOptions);
		runner.runWarningTest();
	}

	private void verifyOutputNegative(String result, String expectedOutput) {
		verifyOutput(result, expectedOutput, false);
	}
	private void verifyOutput(String result, String expectedOutput, boolean positive) {
		int index = result.indexOf(expectedOutput);
		if (positive) {
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
				System.out.println("...");
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} else {
			if (index != -1) {
				assertEquals("Unexpected contents", "", result);
			}
		}
	}
	private String getClassFileContents( String classFileName, int mode) throws IOException,
	ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		return result;
	}

	public void testBug550750_001() {
		runConformTest(
				new String[] {
						"X.java",
						"public class X {\n"+
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
						"public class X {\n"+
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
			"Cannot reduce the visibility of a canonical constructor Point from that of the record\n" +
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
				"1. ERROR in X.java (at line 8)\n" +
				"	this.myInt = myInt;\n" +
				"	^^^^^^^^^^\n" +
				"Illegal explicit assignment of a final field myInt in compact constructor\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 9)\n" +
				"	this.myChar = myChar;\n" +
				"	^^^^^^^^^^^\n" +
				"Illegal explicit assignment of a final field myChar in compact constructor\n" +
				"----------\n");
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
		getPossibleComplianceLevels();
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
			"	                            ^^^\n" +
			"Duplicate component myZ in record\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	record Point(int myInt, int myZ, int myZ) implements I {\n" +
			"	                                     ^^^\n" +
			"Duplicate component myZ in record\n" +
			"----------\n");
	}
	public void testBug550750_026() {
		getPossibleComplianceLevels();
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
			"	                 ^^^^^\n" +
			"Duplicate component myInt in record\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	record Point(int myInt, int myInt, int myInt, int myZ) implements I {\n" +
			"	                            ^^^^^\n" +
			"Duplicate component myInt in record\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
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
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	this.myInt = myInt;\n" +
			"	^^^^^^^^^^\n" +
			"Illegal explicit assignment of a final field myInt in compact constructor\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	this.myZ = myZ;\n" +
			"	^^^^^^^^\n" +
			"Illegal explicit assignment of a final field myZ in compact constructor\n" +
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
			"Instance fields may not be declared in a record class\n" +
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
			"1. ERROR in X.java (at line 8)\n" +
			"	this.myInt = myInt;\n" +
			"	^^^^^^^^^^\n" +
			"Illegal explicit assignment of a final field myInt in compact constructor\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	this.myZ = myZ;\n" +
			"	^^^^^^^^\n" +
			"Illegal explicit assignment of a final field myZ in compact constructor\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\n" +
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
			"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
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
			"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
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
			"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	public <record> void foo(record args){}\n" +
			"	                         ^^^^^^\n" +
			"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
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
			"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
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
			"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
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
			"Instance fields may not be declared in a record class\n" +
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
		getPossibleComplianceLevels();
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
			"2. ERROR in X.java (at line 8)\n" +
			"	this.myInt = 0;\n" +
			"	^^^^^^^^^^\n" +
			"Illegal explicit assignment of a final field myInt in compact constructor\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 9)\n" +
			"	this.myZ = 0;\n" +
			"	^^^^^^^^\n" +
			"Illegal explicit assignment of a final field myZ in compact constructor\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 11)\n" +
			"	public Point(Integer myInt, int myZ) {\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Duplicate method Point(Integer, int) in type Point\n" +
			"----------\n");
	}
	public void testBug553152_009() {
		this.runConformTest(
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
			"0");
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
			"A non-canonical constructor must invoke another constructor of the same class\n" +
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
		this.runConformTest(
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
		"0");
	}
	public void testBug553152_018() {
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
		"1. ERROR in X.java (at line 8)\n" +
		"	this.myInt = myInt;\n" +
		"	^^^^^^^^^^\n" +
		"Illegal explicit assignment of a final field myInt in compact constructor\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	if (this.myInt > 0)  // conditional assignment\n" +
		"	         ^^^^^\n" +
		"The blank final field myInt may not have been initialized\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	this.myChar = myChar;\n" +
		"	^^^^^^^^^^^\n" +
		"Illegal explicit assignment of a final field myChar in compact constructor\n" +
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
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
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
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
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
	verifyClassFile(expectedOutput, "Forts.class", ClassFileBytesDisassembler.SYSTEM);
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
	verifyClassFile(expectedOutput, "Forts.class", ClassFileBytesDisassembler.SYSTEM);
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
			"	              ^\n" +
			"void is an invalid type for the variable k\n" +
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
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"  class Inner {\n"+
			"	  record Point(int x, int y){}\n"+
			"  }\n" +
			"}",
		},
		"0");
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
			(this.complianceLevel < ClassFileConstants.JDK9) ?
				"  0 : # 69 invokestatic java/lang/runtime/ObjectMethods.bootstrap:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;\n" +
				"	Method arguments:\n" +
				"		#1 Car\n" +
				"		#70 model;year\n" +
				"		#72 REF_getField model:Ljava/lang/String;\n" +
				"		#73 REF_getField year:I\n"
			:
				"  1 : # 59 invokestatic java/lang/runtime/ObjectMethods.bootstrap:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;\n" +
				"	Method arguments:\n" +
				"		#1 Car\n" +
				"		#60 model;year\n" +
				"		#62 REF_getField model:Ljava/lang/String;\n" +
				"		#63 REF_getField year:I";
	verifyClassFile(expectedOutput, "Car.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput = 			"  // Method descriptor #12 (Ljava/lang/String;I)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  public Car(java.lang.String model, int year);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [14]\n" +
			"     4  aload_0 [this]\n" +
			"     5  aload_1 [model]\n" +
			"     6  putfield Car.model : java.lang.String [17]\n" +
			"     9  aload_0 [this]\n" +
			"    10  iload_2 [year]\n" +
			"    11  putfield Car.year : int [19]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 4, line: 4]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 15] local: this index: 0 type: Car\n" +
			"        [pc: 0, pc: 15] local: model index: 1 type: java.lang.String\n" +
			"        [pc: 0, pc: 15] local: year index: 2 type: int\n" +
			"      Method Parameters:\n" +
			"        mandated model\n" +
			"        mandated year\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Car.class", ClassFileBytesDisassembler.SYSTEM);
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
	verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560496_002() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK17)
		return; // strictfp = nop
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
			"public final int hashCode();\n";
	verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560797_001() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK17)
		return; // strictfp = nop
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
			"public int x();\n";
	verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug560797_002() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK17)
		return; // strictfp = nop
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
			"public int x();\n";
	verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
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
				"           record R(int x, int y) {}\n"+
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
		"Cannot make a static reference to the non-static variable li\n" +
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
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
	this.runNegativeTest(
	new String[] {
			"X.java",
			"record R() {}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	record R() {}\n" +
		"	^\n" +
		"The Java feature 'Compact Source Files and Instance Main Methods' is only available with source level 25 and above\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	record R() {}\n" +
		"	^^^^^^\n" +
		"'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 1)\n" +
		"	record R() {}\n" +
		"	^\n" +
		"Implicitly declared class must have a candidate main method\n" +
		"----------\n",
		null,
		true,
		options
	);
}
public void testBug56180_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"record R () {} \n"+
			"class X {\n"+
			"       public static void main(String[] args) {\n"+
			"               System.out.println(new R().toString());\n"+
			"       }\n"+
			"}\n"
		},
	 "R[]");
	String expectedOutput =
			" public final java.lang.String toString();\n";
	verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
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
public void testBug561528_005() { // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3880 - second error is due to bad recovery
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
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	record R <N extends Node<AB<CD<N>>>>> (N value){\n" +
		"	                                         ^^^^^\n" +
		"Instance fields may not be declared in a record class\n" +
		"----------\n",
		null,
		true
	);
}
public void testBug561778_001() throws IOException, ClassFormatException {
	runConformTest(
			new String[] {
					"XTest.java",
					"public class XTest{\n" +
					"	static <T> T test(X<T> box) {\n" +
					"		return box.value(); /* */\n" +
					"	}\n" +
					"   public static void main(String[] args) {\n" +
					"       System.out.println(0);\n" +
					"   }\n" +
					"}\n",
					"X.java",
					"public record X<T>(T value) {\n" +
					"}"
			},
		"0");
	String expectedOutput =
			"  // Method descriptor #32 (Ljava/lang/Object;)V\n" +
			"  // Signature: (TT;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public X(java.lang.Object value);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [34]\n" +
			"     4  aload_0 [this]\n" +
			"     5  aload_1 [value]\n" +
			"     6  putfield X.value : java.lang.Object [12]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        value\n" +
			"\n";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);

	expectedOutput =
			"  // Method descriptor #9 ()Ljava/lang/Object;\n" +
			"  // Signature: ()TT;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public java.lang.Object value();\n";
	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug561778_002() throws IOException, ClassFormatException {
	runConformTest(
			new String[] {
					"XTest.java",
					"public class XTest{\n" +
					"	static <T> Y<T> test(X<T> box) {\n" +
					"		return box.value(); /* */\n" +
					"	}\n" +
					"   public static void main(String[] args) {\n" +
					"       System.out.println(0);\n" +
					"   }\n" +
					"}\n",
					"X.java",
					"public record X<T>(Y<T> value) {\n" +
					"}\n" +
					"class Y<T> {\n" +
					"}"
			},
		"0");
	String expectedOutput =
			"  // Method descriptor #9 ()LY;\n" +
			"  // Signature: ()LY<TT;>;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public Y value();\n";
	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562219_001() {
	runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"       public static void main(String[] args) {\n"+
				"               @SuppressWarnings(\"unused\")\n"+
				"               class Y {\n"+
				"                       class Z {\n"+
				"                               record R() {\n"+
				"                                       \n"+
				"                               }\n"+
				"                       }\n"+
				"               }\n"+
				"       }\n"+
				"}\n"
			},
		"");
}
public void testBug562219_002() {
	runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"    public static void main(String[] args) {\n"+
				"        @SuppressWarnings(\"unused\")\n"+
				"        class Y {\n"+
				"           record R() {}\n"+
				"        }\n"+
				"    }\n"+
				"}\n"
			},
		""
	);
}
/*
 * Test that annotation with implicit target as METHOD are included in the
 * generated bytecode on the record component and its accessor method
 */
public void test562250a() throws IOException, ClassFormatException {
	runConformTest(
			new String[] {
					"X.java",
					"import java.lang.annotation.*;\n" +
					"import java.lang.reflect.*;\n" +
					"\n" +
					"record Point(@Annot int a) {\n" +
					"}\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface Annot {\n" +
					"}\n" +
					"public class X {\n" +
					"	public static void main(String[] args) throws Exception {\n" +
					"			Class<?> cls = Class.forName(\"Point\");\n" +
					"			RecordComponent[] recordComponents = cls.getRecordComponents();\n" +
					"			for (RecordComponent recordComponent : recordComponents) {\n" +
					"				Annotation[] annotations = recordComponent.getAnnotations();\n" +
					"				System.out.println(\"RecordComponents:\");\n" +
					"				for (Annotation annot : annotations) {\n" +
					"					System.out.println(annot);\n" +
					"				}\n" +
					"				Method accessor = recordComponent.getAccessor();\n" +
					"				System.out.println(\"Accessors:\");\n" +
					"				annotations =accessor.getAnnotations();\n" +
					"				for (Annotation annot : annotations) {\n" +
					"					System.out.println(annot);\n" +
					"				}\n" +
					"			}\n" +
					"	}\n" +
					"}"
			},
		"RecordComponents:\n" +
		"@Annot()\n" +
		"Accessors:\n" +
		"@Annot()");
}
/*
 * Test that annotation with explicit target as METHOD are included in the
 * generated bytecode on its accessor method (and not on record component)
 */
public void test562250b() throws IOException, ClassFormatException {
	runConformTest(
			new String[] {
					"X.java",
					"import java.lang.annotation.*;\n" +
					"import java.lang.reflect.*;\n" +
					"\n" +
					"record Point(@Annot int a) {\n" +
					"}\n" +
					"@Target({ElementType.METHOD})\n"+
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface Annot {\n" +
					"}\n" +
					"public class X {\n" +
					"	public static void main(String[] args) throws Exception {\n" +
					"			Class<?> cls = Class.forName(\"Point\");\n" +
					"			RecordComponent[] recordComponents = cls.getRecordComponents();\n" +
					"			for (RecordComponent recordComponent : recordComponents) {\n" +
					"				Annotation[] annotations = recordComponent.getAnnotations();\n" +
					"				System.out.println(\"RecordComponents:\");\n" +
					"				for (Annotation annot : annotations) {\n" +
					"					System.out.println(annot);\n" +
					"				}\n" +
					"				Method accessor = recordComponent.getAccessor();\n" +
					"				System.out.println(\"Accessors:\");\n" +
					"				annotations =accessor.getAnnotations();\n" +
					"				for (Annotation annot : annotations) {\n" +
					"					System.out.println(annot);\n" +
					"				}\n" +
					"			}\n" +
					"	}\n" +
					"}"
			},
		"RecordComponents:\n" +
		"Accessors:\n" +
		"@Annot()");
}
/*
 * Test that even though annotations with FIELD as a target are permitted by the
 * compiler on a record component, the generated bytecode doesn't contain these annotations
 * on the record component.
 */
public void test562250c() throws IOException, ClassFormatException {
	runConformTest(
			new String[] {
					"X.java",
					"import java.lang.annotation.*;\n" +
					"import java.lang.reflect.*;\n" +
					"\n" +
					"record Point(@Annot int a) {\n" +
					"}\n" +
					"@Target({ElementType.FIELD})\n"+
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface Annot {\n" +
					"}\n" +
					"public class X {\n" +
					"	public static void main(String[] args) throws Exception {\n" +
					"			Class<?> cls = Class.forName(\"Point\");\n" +
					"			RecordComponent[] recordComponents = cls.getRecordComponents();\n" +
					"			for (RecordComponent recordComponent : recordComponents) {\n" +
					"				Annotation[] annotations = recordComponent.getAnnotations();\n" +
					"				System.out.println(\"RecordComponents:\");\n" +
					"				for (Annotation annot : annotations) {\n" +
					"					System.out.println(annot);\n" +
					"				}\n" +
					"				Method accessor = recordComponent.getAccessor();\n" +
					"				System.out.println(\"Accessors:\");\n" +
					"				annotations =accessor.getAnnotations();\n" +
					"				for (Annotation annot : annotations) {\n" +
					"					System.out.println(annot);\n" +
					"				}\n" +
					"			}\n" +
					"	}\n" +
					"}"
			},
		"RecordComponents:\n" +
		"Accessors:");
}
public void testBug562439_001() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"      Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RC int myInt, char myChar) { \n"+
			"}   \n"+
			"\n"+
			"@Target({ElementType.RECORD_COMPONENT})\n"+
			"@interface RC {}\n"
		},
		"100");
	String expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeInvisibleAnnotations: \n" +
			"    #60 @RC(\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_001_1() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"""
			import java.lang.annotation.Annotation;
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Retention;
			import java.lang.annotation.RetentionPolicy;
			import java.lang.annotation.Target;
			import java.lang.reflect.RecordComponent;

			public class X {

			  public static void main(String[] args){
			      RecordComponent[] recordComponents = Point.class.getRecordComponents();
			      if (recordComponents.length != 2)
			    	 throw new AssertionError("Wrong number of components");
			      Annotation[] annotations = recordComponents[0].getAnnotations();
			      if (annotations.length != 1)
			     	 throw new AssertionError("Wrong number of annotations");
			      if (!annotations[0].toString().equals("@RC()"))
			    	  throw new AssertionError("Wrong annotation " + annotations[0]);
			      annotations = recordComponents[1].getAnnotations();
			      if (annotations.length != 0)
			     	 throw new AssertionError("Wrong number of annotations");
		     	  System.out.println("All well!");
		      }
			}

			record Point(@RC int myInt, char myChar) {
			}

			@Target({ElementType.RECORD_COMPONENT})
			@Retention(RetentionPolicy.RUNTIME)
			@interface RC {}
			"""
		},
		"All well!");
}
public void testBug562439_002() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Retention;\n"+
			"import java.lang.annotation.RetentionPolicy;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RC int myInt, char myChar) { \n"+
			"}   \n"+
			"\n"+
			"@Target({ElementType.RECORD_COMPONENT})\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n"+
			"@interface RC {}\n"
		},
		"100");
	String expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeVisibleAnnotations: \n" +
			"    #60 @RC(\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_003() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RCF int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD})\n"+
			"@interface RCF {}\n"
		},
		"100");
	String expectedOutput = "  // Field descriptor #6 I\n" +
			"  private final int myInt;\n" +
			"    RuntimeInvisibleAnnotations: \n" +
			"      #8 @RCF(\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeInvisibleAnnotations: \n" +
			"    #8 @RCF(\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_004() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Retention;\n"+
			"import java.lang.annotation.RetentionPolicy;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RCF int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD})\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface RCF {}\n"
		},
		"100");
	String expectedOutput = "  // Field descriptor #6 I\n" +
			"  private final int myInt;\n" +
			"    RuntimeVisibleAnnotations: \n" +
			"      #8 @RCF(\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeVisibleAnnotations: \n" +
			"    #8 @RCF(\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_005() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RF int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ElementType.FIELD})\n"+
			"@interface RF {}\n"
		},
		"100");
	String expectedOutput = "  // Field descriptor #6 I\n" +
			"  private final int myInt;\n" +
			"    RuntimeInvisibleAnnotations: \n" +
			"      #8 @RF(\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"// Component descriptor #10 C\n" +
			"char myChar;\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_006() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Retention;\n"+
			"import java.lang.annotation.RetentionPolicy;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RF int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ElementType.FIELD})\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface RF {}\n"
		},
		"100");
	String expectedOutput = "  // Field descriptor #6 I\n" +
			"  private final int myInt;\n" +
			"    RuntimeVisibleAnnotations: \n" +
			"      #8 @RF(\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"// Component descriptor #10 C\n" +
			"char myChar;\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_007() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RCFU int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.TYPE_USE})\n"+
			"@interface RCFU {}\n"
		},
		"100");
	String expectedOutput = 			"  // Field descriptor #6 I\n" +
			"  private final int myInt;\n" +
			"    RuntimeInvisibleAnnotations: \n" +
			"      #8 @RCFU(\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #8 @RCFU(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput = 			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeInvisibleAnnotations: \n" +
			"    #8 @RCFU(\n" +
			"    )\n" +
			"  RuntimeInvisibleTypeAnnotations: \n" +
			"    #8 @RCFU(\n" +
			"      target type = 0x13 FIELD\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_008() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Retention;\n"+
			"import java.lang.annotation.RetentionPolicy;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RCFU int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.TYPE_USE})\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface RCFU {}\n"
		},
		"100");
	String expectedOutput =
			"  // Field descriptor #6 I\n" +
			"  private final int myInt;\n" +
			"    RuntimeVisibleAnnotations: \n" +
			"      #8 @RCFU(\n" +
			"      )\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #8 @RCFU(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeVisibleAnnotations: \n" +
			"    #8 @RCFU(\n" +
			"    )\n" +
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #8 @RCFU(\n" +
			"      target type = 0x13 FIELD\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_009() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RCM int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD})\n"+
			"@interface RCM {}\n"
		},
		"100");
	String expectedOutput =
			"  // Method descriptor #9 ()I\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public int myInt();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Point.myInt : int [13]\n" +
			"    4  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 11]\n" +
			"    RuntimeInvisibleAnnotations: \n" +
			"      #11 @RCM(\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeInvisibleAnnotations: \n" +
			"    #11 @RCM(\n" +
			"    )\n" +
			"// Component descriptor #8 C\n" +
			"char myChar;\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_010() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Retention;\n"+
			"import java.lang.annotation.RetentionPolicy;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RCM int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD})\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface RCM {}\n"
		},
		"100");
	String expectedOutput =
			"  public int myInt();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Point.myInt : int [13]\n" +
			"    4  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 13]\n" +
			"    RuntimeVisibleAnnotations: \n" +
			"      #11 @RCM(\n" +
			"      )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeVisibleAnnotations: \n" +
			"    #11 @RCM(\n" +
			"    )\n" +
			"// Component descriptor #8 C\n" +
			"char myChar;\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_011() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@M int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ElementType.METHOD})\n"+
			"@interface M {}\n"
		},
		"100");
	String expectedOutput =
			"  // Method descriptor #9 ()I\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public int myInt();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Point.myInt : int [13]\n" +
			"    4  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 11]\n" +
			"    RuntimeInvisibleAnnotations: \n" +
			"      #11 @M(\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"// Component descriptor #8 C\n" +
			"char myChar;\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_012() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Retention;\n"+
			"import java.lang.annotation.RetentionPolicy;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@M int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ElementType.METHOD})\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface M {}\n"
		},
		"100");
	String expectedOutput =
			"  public int myInt();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Point.myInt : int [13]\n" +
			"    4  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 13]\n" +
			"    RuntimeVisibleAnnotations: \n" +
			"      #11 @M(\n" +
			"      )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"// Component descriptor #8 C\n" +
			"char myChar;\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_013() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RCMU int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.TYPE_USE})\n"+
			"@interface RCMU {}\n"
		},
		"100");
	String expectedOutput =
			"  // Field descriptor #6 I\n" +
			"  private final int myInt;\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #8 @RCMU(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"  // Method descriptor #11 ()I\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public int myInt();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Point.myInt : int [14]\n" +
			"    4  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 11]\n" +
			"    RuntimeInvisibleAnnotations: \n" +
			"      #8 @RCMU(\n" +
			"      )\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #8 @RCMU(\n" +
			"        target type = 0x14 METHOD_RETURN\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeInvisibleAnnotations: \n" +
			"    #8 @RCMU(\n" +
			"    )\n" +
			"  RuntimeInvisibleTypeAnnotations: \n" +
			"    #8 @RCMU(\n" +
			"      target type = 0x13 FIELD\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_014() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Retention;\n"+
			"import java.lang.annotation.RetentionPolicy;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RCMU int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.TYPE_USE})\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface RCMU {}\n"
		},
		"100");
	String expectedOutput =
			"  // Field descriptor #6 I\n" +
			"  private final int myInt;\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #8 @RCMU(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"  // Method descriptor #11 ()I\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public int myInt();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Point.myInt : int [14]\n" +
			"    4  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 13]\n" +
			"    RuntimeVisibleAnnotations: \n" +
			"      #8 @RCMU(\n" +
			"      )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeVisibleAnnotations: \n" +
			"    #8 @RCMU(\n" +
			"    )\n" +
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #8 @RCMU(\n" +
			"      target type = 0x13 FIELD\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_015() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"      Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@T int myInt, char myChar) { \n"+
			"}   \n"+
			"\n"+
			"@Target({ElementType.TYPE_USE})\n"+
			"@interface T {}\n"
		},
		"100");
	String expectedOutput =
			"  // Field descriptor #6 I\n" +
			"  private final int myInt;\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #8 @T(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"  // Method descriptor #11 ()I\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public int myInt();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Point.myInt : int [13]\n" +
			"    4  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 11]\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #8 @T(\n" +
			"        target type = 0x14 METHOD_RETURN\n" +
			"      )\n" +
			"  ";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeInvisibleTypeAnnotations: \n" +
			"    #8 @T(\n" +
			"      target type = 0x13 FIELD\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"  Point(int myInt, char myChar);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [36]\n" +
			"     4  aload_0 [this]\n" +
			"     5  iload_1 [myInt]\n" +
			"     6  putfield Point.myInt : int [13]\n" +
			"     9  aload_0 [this]\n" +
			"    10  iload_2 [myChar]\n" +
			"    11  putfield Point.myChar : char [18]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        myInt\n" +
			"        myChar\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #8 @T(\n" +
			"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
			"        method parameter index = 0\n" +
			"      )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_016() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Retention;\n"+
			"import java.lang.annotation.RetentionPolicy;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@T int myInt, char myChar) { \n"+
			"}   \n"+
			"\n"+
			"@Target({ElementType.TYPE_USE})\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n"+
			"@interface T {}\n"
		},
		"100");
	String expectedOutput =
			"  // Field descriptor #6 I\n" +
			"  private final int myInt;\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #8 @T(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"  public int myInt();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Point.myInt : int [13]\n" +
			"    4  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 13]\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #8 @T(\n" +
			"        target type = 0x14 METHOD_RETURN\n" +
			"      )\n" +
			"  ";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #8 @T(\n" +
			"      target type = 0x13 FIELD\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"  Point(int myInt, char myChar);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [36]\n" +
			"     4  aload_0 [this]\n" +
			"     5  iload_1 [myInt]\n" +
			"     6  putfield Point.myInt : int [13]\n" +
			"     9  aload_0 [this]\n" +
			"    10  iload_2 [myChar]\n" +
			"    11  putfield Point.myChar : char [18]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        myInt\n" +
			"        myChar\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #8 @T(\n" +
			"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
			"        method parameter index = 0\n" +
			"      )\n" +
			"\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_017() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RCP int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ ElementType.RECORD_COMPONENT, ElementType.PARAMETER})\n"+
			"@interface RCP {}\n"
		},
		"100");
	String expectedOutput =
			"  Point(int myInt, char myChar);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [36]\n" +
			"     4  aload_0 [this]\n" +
			"     5  iload_1 [myInt]\n" +
			"     6  putfield Point.myInt : int [11]\n" +
			"     9  aload_0 [this]\n" +
			"    10  iload_2 [myChar]\n" +
			"    11  putfield Point.myChar : char [16]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        myInt\n" +
			"        myChar\n" +
			"    RuntimeInvisibleParameterAnnotations: \n" +
			"      Number of annotations for parameter 0: 1\n" +
			"        #35 @RCP(\n" +
			"        )\n" +
			"      Number of annotations for parameter 1: 0\n" +
			"\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeInvisibleAnnotations: \n" +
			"    #35 @RCP(\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_018() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Retention;\n"+
			"import java.lang.annotation.RetentionPolicy;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@RCP int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ ElementType.RECORD_COMPONENT, ElementType.PARAMETER})\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface RCP {}\n"
		},
		"100");
	String expectedOutput =
			"  Point(int myInt, char myChar);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [36]\n" +
			"     4  aload_0 [this]\n" +
			"     5  iload_1 [myInt]\n" +
			"     6  putfield Point.myInt : int [11]\n" +
			"     9  aload_0 [this]\n" +
			"    10  iload_2 [myChar]\n" +
			"    11  putfield Point.myChar : char [16]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        myInt\n" +
			"        myChar\n" +
			"    RuntimeVisibleParameterAnnotations: \n" +
			"      Number of annotations for parameter 0: 1\n" +
			"        #35 @RCP(\n" +
			"        )\n" +
			"      Number of annotations for parameter 1: 0\n" +
			"\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeVisibleAnnotations: \n" +
			"    #35 @RCP(\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_019() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@Annot int myInt, char myChar) { \n"+
			"}   \n"+
			"@interface Annot {}\n"
		},
		"100");
	String expectedOutput =
			"  // Field descriptor #6 I\n" +
			"  private final int myInt;\n" +
			"    RuntimeInvisibleAnnotations: \n" +
			"      #8 @Annot(\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"  Point(int myInt, char myChar);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [37]\n" +
			"     4  aload_0 [this]\n" +
			"     5  iload_1 [myInt]\n" +
			"     6  putfield Point.myInt : int [13]\n" +
			"     9  aload_0 [this]\n" +
			"    10  iload_2 [myChar]\n" +
			"    11  putfield Point.myChar : char [18]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        myInt\n" +
			"        myChar\n" +
			"    RuntimeInvisibleParameterAnnotations: \n" +
			"      Number of annotations for parameter 0: 1\n" +
			"        #8 @Annot(\n" +
			"        )\n" +
			"      Number of annotations for parameter 1: 0\n" +
			"\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"  // Method descriptor #11 ()I\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public int myInt();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Point.myInt : int [13]\n" +
			"    4  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 11]\n" +
			"    RuntimeInvisibleAnnotations: \n" +
			"      #8 @Annot(\n" +
			"      )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeInvisibleAnnotations: \n" +
			"    #8 @Annot(\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug562439_020() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.ElementType;\n"+
			"import java.lang.annotation.Retention;\n"+
			"import java.lang.annotation.RetentionPolicy;\n"+
			"import java.lang.annotation.Target;\n"+
			"                          \n"+
			"public class X { \n"+
			"  public static void main(String[] args){\n"+
			"         Point p = new Point(100, 'a');\n"+
			"      System.out.println(p.myInt());\n"+
			"  } \n"+
			"}\n"+
			"\n"+
			"record Point(@Annot int myInt, char myChar) { \n"+
			"}   \n"+
			"@Target({ ElementType.RECORD_COMPONENT, ElementType.PARAMETER})\n"+
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface Annot {}\n"
		},
		"100");
	String expectedOutput =
			"  Point(int myInt, char myChar);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [36]\n" +
			"     4  aload_0 [this]\n" +
			"     5  iload_1 [myInt]\n" +
			"     6  putfield Point.myInt : int [11]\n" +
			"     9  aload_0 [this]\n" +
			"    10  iload_2 [myChar]\n" +
			"    11  putfield Point.myChar : char [16]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        myInt\n" +
			"        myChar\n" +
			"    RuntimeVisibleParameterAnnotations: \n" +
			"      Number of annotations for parameter 0: 1\n" +
			"        #35 @Annot(\n" +
			"        )\n" +
			"      Number of annotations for parameter 1: 0\n" +
			"\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput =
			"Record: #Record\n" +
			"Components:\n" +
			"  \n" +
			"// Component descriptor #6 I\n" +
			"int myInt;\n" +
			"  RuntimeVisibleAnnotations: \n" +
			"    #35 @Annot(\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug563178_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"+
			"record Point(final int x, int y){\n"+
		"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	record Point(final int x, int y){\n" +
		"	                       ^\n" +
		"A record component x cannot have modifiers\n" +
		"----------\n");
}
public void testBug563183_001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public record X() {\n"+
			"  public X() {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_002() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public record X() {\n"+
			"  public X {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public record X() {\n"+
			"  protected X() {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	protected X() {}\n" +
		"	          ^^^\n" +
		"Cannot reduce the visibility of a canonical constructor X from that of the record\n" +
		"----------\n");
}
public void testBug563183_004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public record X() {\n"+
			"  protected X {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	protected X {}\n" +
		"	          ^\n" +
		"Cannot reduce the visibility of a canonical constructor X from that of the record\n" +
		"----------\n");
}
public void testBug563183_005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public record X() {\n"+
			"  /*package */ X() {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	/*package */ X() {}\n" +
		"	             ^^^\n" +
		"Cannot reduce the visibility of a canonical constructor X from that of the record\n" +
		"----------\n");
}
public void testBug563183_006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public record X() {\n"+
			"  /*package */ X {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	/*package */ X {}\n" +
		"	             ^\n" +
		"Cannot reduce the visibility of a canonical constructor X from that of the record\n" +
		"----------\n");
}
public void testBug563183_007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public record X() {\n"+
			"  private X() {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	private X() {}\n" +
		"	        ^^^\n" +
		"Cannot reduce the visibility of a canonical constructor X from that of the record\n" +
		"----------\n");
}
public void testBug563183_008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public record X() {\n"+
			"  private X {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	private X {}\n" +
		"	        ^\n" +
		"Cannot reduce the visibility of a canonical constructor X from that of the record\n" +
		"----------\n");
}
public void testBug563183_009() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  protected record R() {\n"+
			"    public R() {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_010() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  protected record R() {\n"+
			"    public R {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_011() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  protected record R() {\n"+
			"    protected R() {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  protected record R() {\n"+
			"    protected R {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  protected record R() {\n"+
			"    /*package */ R() {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	/*package */ R() {}\n" +
		"	             ^^^\n" +
		"Cannot reduce the visibility of a canonical constructor R from that of the record\n" +
		"----------\n");
}
public void testBug563183_014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  protected record R() {\n"+
			"    /*package */ R {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	/*package */ R {}\n" +
		"	             ^\n" +
		"Cannot reduce the visibility of a canonical constructor R from that of the record\n" +
		"----------\n");
}
public void testBug563183_015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  protected record R() {\n"+
			"    private R() {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	private R() {}\n" +
		"	        ^^^\n" +
		"Cannot reduce the visibility of a canonical constructor R from that of the record\n" +
		"----------\n");
}
public void testBug563183_016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  protected record R() {\n"+
			"    private R {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	private R {}\n" +
		"	        ^\n" +
		"Cannot reduce the visibility of a canonical constructor R from that of the record\n" +
		"----------\n");
}
public void testBug563183_017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"/*package */ record X() {\n"+
			"  public X() {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_018() {
	this.runConformTest(
		new String[] {
			"X.java",
			"/*package */ record X() {\n"+
			"  public X {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"record X() {\n"+
			"  protected X() {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_020() {
	this.runConformTest(
		new String[] {
			"X.java",
			"record X() {\n"+
			"  protected X {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_021() {
	this.runConformTest(
		new String[] {
			"X.java",
			" record X() {\n"+
			"  /*package */ X() {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_022() {
	this.runConformTest(
		new String[] {
			"X.java",
			" record X() {\n"+
			"  /*package */ X {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"record X() {\n"+
			"  private X() {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	private X() {}\n" +
		"	        ^^^\n" +
		"Cannot reduce the visibility of a canonical constructor X from that of the record\n" +
		"----------\n");
}
public void testBug563183_024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"record X() {\n"+
			"  private X {}\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	private X {}\n" +
		"	        ^\n" +
		"Cannot reduce the visibility of a canonical constructor X from that of the record\n" +
		"----------\n");
}
public void testBug563183_025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  private record R() {\n"+
			"    public R() {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_026() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  private record R() {\n"+
			"    protected R {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  private record R() {\n"+
			"    /* package */ R() {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563183_028() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  private record R() {\n"+
			"    private R {}\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug563184_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"record X(int angel) {\n"+
			"  X(int devil) {\n"+
			"     this.angel = devil;\n" +
			"  }\n"+
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	X(int devil) {\n" +
		"	      ^^^^^\n" +
		"Illegal parameter name devil in canonical constructor, expected angel, the corresponding component name\n" +
		"----------\n");
}
public void testBug563184_002() {
	this.runConformTest(
		new String[] {
			"X.java",
			"record X(int myInt) {\n"+
			"  X(int myInt) {\n"+
			"     this.myInt = myInt;\n" +
			"  }\n"+
			"  X(int i, int j) {\n"+
			"    this(i);\n"+
			"  }\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}",
		},
		"0");
}
public void testBug562637_001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public record X(int i) {\n"+
			"    public X {\n"+
			"            i = i/2;\n"+
			"    }\n"+
			"    public static void main(String[] args) {\n"+
			"            System.out.println(new X(10).i());\n"+
			"    }\n"+
			"}",
		},
		"5");
}
	public void testBug563181_01() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n"+
						"import java.lang.annotation.Target;\n"+
						"public class X { \n"+
						"  public static void main(String[] args){}\n"+
						"}\n"+
						"record Point(@RCMU int myInt, char myChar) { \n"+
						"  public int myInt(){\n"+
						"     return this.myInt;\n" +
						"  }\n"+
						"}   \n"+
						"@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.TYPE_USE})\n"+
						"@interface RCMU {}\n"
				},
				"");
		String expectedOutput =
				"  // Method descriptor #11 ()I\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public int myInt();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Point.myInt : int [13]\n" +
				"    4  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 8]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: Point\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug563181_02() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n"+
						"import java.lang.annotation.Target;\n"+
						"public class X { \n"+
						"  public static void main(String[] args){}\n"+
						"}\n"+
						"record Point(@RCMU int myInt, char myChar) {\n"+
						"  @RCMU public int myInt(){\n"+
						"     return this.myInt;\n" +
						"  }\n"+
						"}\n"+
						"@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD, ElementType.TYPE_USE})\n"+
						"@interface RCMU {}\n"
				},
				"");
		String expectedOutput =
				"  // Method descriptor #11 ()I\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public int myInt();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Point.myInt : int [14]\n" +
				"    4  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 8]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: Point\n" +
				"    RuntimeInvisibleAnnotations: \n" +
				"      #8 @RCMU(\n" +
				"      )\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @RCMU(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug563181_03() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.*;\n"+
						"public class X { \n"+
						"  public static void main(String[] args){}\n"+
						"}\n"+
						"record Point(@TypeAnnot @SimpleAnnot int myInt, char myChar) {}\n"+
						"@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD})\n"+
						"@Retention(RetentionPolicy.RUNTIME)\n" +
						"@interface SimpleAnnot {}\n" +
						"@Target({ ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})\n"+
						"@Retention(RetentionPolicy.RUNTIME)\n" +
						"@interface TypeAnnot {}\n"
				},
				"");
		String expectedOutput =
				"  // Method descriptor #11 ()I\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public int myInt();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Point.myInt : int [15]\n" +
				"    4  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n" +
				"    RuntimeVisibleAnnotations: \n" +
				"      #13 @SimpleAnnot(\n" +
				"      )\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @TypeAnnot(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug563181_04() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.*;\n"+
						"public class X { \n"+
						"  public static void main(String[] args){}\n"+
						"}\n"+
						"record Point(@TypeAnnot @SimpleAnnot int myInt, char myChar) {\n"+
						"  @TypeAnnot @SimpleAnnot public int myInt(){\n"+
						"     return this.myInt;\n" +
						"  }\n"+
						"}\n"+
						"@Target({ ElementType.RECORD_COMPONENT, ElementType.METHOD})\n"+
						"@Retention(RetentionPolicy.RUNTIME)\n" +
						"@interface SimpleAnnot {}\n" +
						"@Target({ ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})\n"+
						"@Retention(RetentionPolicy.RUNTIME)\n" +
						"@interface TypeAnnot {}\n"
				},
				"");
		String expectedOutput =
				" // Method descriptor #11 ()I\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public int myInt();\n" +
				"    0  aload_0 [this]\n" +
				"    1  getfield Point.myInt : int [15]\n" +
				"    4  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: Point\n" +
				"    RuntimeVisibleAnnotations: \n" +
				"      #13 @SimpleAnnot(\n" +
				"      )\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @TypeAnnot(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"  \n";
		verifyClassFile(expectedOutput, "Point.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565104_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"public class X { \n"+
				"  public record R() {}\n"+
				"  public static void main(String[] args){}\n"+
				"}\n"
			},
		"");
		String expectedOutput =
				"  // Stack: 1, Locals: 1\n" +
				"  public X$R();\n" +
				"    0  aload_0 [this]\n";
		verifyClassFile(expectedOutput, "X$R.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565104_002() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"public class X { \n"+
				"  record R() {}\n"+
				"  public static void main(String[] args){}\n"+
				"}\n"
			},
		"");
		String expectedOutput =
				"  // Stack: 1, Locals: 1\n" +
				"  X$R();\n" +
				"    0  aload_0 [this]\n";
		verifyClassFile(expectedOutput, "X$R.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565104_003() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"public class X { \n"+
				"  protected record R() {}\n"+
				"  public static void main(String[] args){}\n"+
				"}\n"
			},
		"");
		String expectedOutput =
				"  // Stack: 1, Locals: 1\n" +
				"  protected X$R();\n" +
				"    0  aload_0 [this]\n";
		verifyClassFile(expectedOutput, "X$R.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565104_004() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"public class X { \n"+
				"  private record R() {}\n"+
				"  public static void main(String[] args){}\n"+
				"}\n"
			},
		"");
		String expectedOutput =
				"  // Stack: 1, Locals: 1\n" +
				"  private X$R();\n" +
				"    0  aload_0 [this]\n";
		verifyClassFile(expectedOutput, "X$R.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug564146_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public record X(int i) {\n"+
				"  public X() {\n"+
				"    this.i = 10;\n"+
				"  }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public X() {\n" +
			"	       ^^^\n" +
			"A non-canonical constructor must invoke another constructor of the same class\n" +
			"----------\n");
	}
	public void testBug564146_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public record X(int i) {\n"+
				"  public X() {\n"+
				"    super();\n"+
				"    this.i = 10;\n"+
				"  }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"A non-canonical constructor must invoke another constructor of the same class\n" +
			"----------\n");
	}
	public void testBug564146_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public record X(int i) {\n"+
				"  public X(int i) {\n"+
				"    this.i = 10;\n"+
				"    Zork();\n"+
				"  }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	public void testBug564146_004() {
		runConformTest(
			new String[] {
				"X.java",
				"public record X(int i) {\n"+
					" public X() {\n"+
					"   this(10);\n"+
					" }\n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(new X().i());\n"+
					" }\n"+
					"}"
				},
			"10");
	}
	public void testBug564146_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public record X() {\n"+
				" public X(int i) {\n"+
				"   this(10);\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	this(10);\n" +
			"	^^^^^^^^^\n" +
			"Recursive constructor invocation X(int)\n" +
			"----------\n");
	}
	public void testBug564146_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public record X() {\n"+
				" public X() {\n"+
				"   this(10);\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	this(10);\n" +
			"	^^^^^^^^^\n" +
			"The body of a canonical constructor must not contain an explicit constructor call\n" +
			"----------\n");
	}
	public void testBug564146_007() {
		runConformTest(
			new String[] {
				"X.java",
				"public record X(int i) {\n"+
				" public X() {\n"+
				"   this(10);\n"+
				" }\n"+
				" public X(int i, int k) {\n"+
				"   this();\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(new X(2, 3).i());\n"+
				" }\n"+
				"}"
				},
			"10");
	}

public void testBug564672_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X extends record {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n" +
			"class record {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	class X extends record {\n" +
		"	                ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	class record {}\n" +
		"	      ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X extends record {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	class X extends record {\n" +
		"	                ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X implements record {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"interface record {}\n;" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	class X implements record {\n" +
		"	                   ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	interface record {}\n" +
		"	          ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X implements record {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	class X implements record {\n" +
		"	                   ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  class Y extends record {\n"+
			"  }\n" +
			"  class record {}\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y extends record {\n" +
		"	                ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	class record {}\n" +
		"	      ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  class Y extends record {\n"+
			"  }\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y extends record {\n" +
		"	                ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  class Y implements record {\n"+
			"  }\n" +
			"  interface record {}\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y implements record {\n" +
		"	                   ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	interface record {}\n" +
		"	          ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  class Y implements record {\n"+
			"  }\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y implements record {\n" +
		"	                   ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface Y extends record {\n"+
			"}\n" +
			"interface record {}\n" +
			"class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	interface Y extends record {\n" +
		"	                    ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	interface record {}\n" +
		"	          ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface Y extends record {\n"+
			"}\n" +
			"class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	interface Y extends record {\n" +
		"	                    ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  interface Y extends record {\n"+
			"  }\n" +
			"  interface record {}\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	interface Y extends record {\n" +
		"	                    ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	interface record {}\n" +
		"	          ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  interface Y extends record {\n"+
			"  }\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	interface Y extends record {\n" +
		"	                    ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n"+
			"  class Y extends record {\n"+
			"  }\n" +
			"  class record {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y extends record {\n" +
		"	                ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	class record {}\n" +
		"	      ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n"+
			"  class Y extends record {\n"+
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y extends record {\n" +
		"	                ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n"+
			"  class Y implements record {\n"+
			"  }\n" +
			"  interface record {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y implements record {\n" +
		"	                   ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	interface record {}\n" +
		"	          ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n"+
			"  class Y implements record {\n"+
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y implements record {\n" +
		"	                   ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n"+
			"  interface Y extends record {\n"+
			"  }\n" +
			"  interface record {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	interface Y extends record {\n" +
		"	                    ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	interface record {}\n" +
		"	          ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n"+
			"  interface Y extends record {\n"+
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	interface Y extends record {\n" +
		"	                    ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static record a(int i, int j) {\n" +
			"		record r=new record(i,j);\n" +
			"		return r;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	record r=new record(i,j);\n" +
		"	^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	record r=new record(i,j);\n" +
		"	       ^\n" +
		"Instance fields may not be declared in a record class\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	record r=new record(i,j);\n" +
		"	             ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	return r;\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"return\", byte expected\n" +
		"----------\n");
}
public void testBug564672_020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	class record {};\n" +
			"	static record a(int i, int j) {\n" +
			"		record r=new record();\n" +
			"		return r;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class record {};\n" +
		"	      ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	record r=new record();\n" +
		"	^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	record r=new record();\n" +
		"	       ^\n" +
		"Instance fields may not be declared in a record class\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	record r=new record();\n" +
		"	             ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 5)\n" +
		"	return r;\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"return\", byte expected\n" +
		"----------\n");
}
public void testBug564672_021() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	interface IPoint {\n" +
			"	}\n" +
			"	record Point(int x, int y) implements IPoint {}\n" +
			"	static IPoint a(int i, int j) {\n" +
			"		Point record=new Point(i,j);\n" +
			"		return record;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(a(5,10));\n" +
			"	}\n" +
			"}\n"
		},
		"Point[x=5, y=10]");
}
public void testBug564672_022() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	record R(int i){} \n" +
			"	interface IPoint {\n" +
			"		record a(int i) {\n" +
			"       	System.out.println(0);\n" +
			"           return new R(i);\n" +
			"		}\n" +
			"	}\n" +
			"	record Point(int x, int y) implements IPoint {}\n" +
			"	static IPoint a(int i, int j) {\n" +
			"		Point record=new Point(i,j);\n" +
			"		record.a(1);\n" +
			"		return record;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	System.out.println(0);\n" +
		"	          ^\n" +
		"Syntax error on token \".\", @ expected after this token\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	System.out.println(0);\n" +
		"           return new R(i);\n" +
		"	                   ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Syntax error on tokens, delete these tokens\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	return new R(i);\n" +
		"	              ^\n" +
		"Syntax error, insert \")\" to complete SingleMemberAnnotation\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 6)\n" +
		"	return new R(i);\n" +
		"	              ^\n" +
		"Syntax error, insert \"SimpleName\" to complete QualifiedName\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 6)\n" +
		"	return new R(i);\n" +
		"	              ^\n" +
		"Syntax error, insert \"Identifier (\" to complete MethodHeaderName\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 6)\n" +
		"	return new R(i);\n" +
		"	              ^\n" +
		"Syntax error, insert \")\" to complete MethodDeclaration\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 12)\n" +
		"	record.a(1);\n" +
		"	       ^\n" +
		"The method a(int) is undefined for the type X.Point\n" +
		"----------\n");
}
public void testBug564672_023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	interface IPoint {\n" +
			"	}\n" +
			"	record Point(int x, int y) implements IPoint {}\n" +
			"	static IPoint a(int i, int j) throws record{\n" +
			"		Point record=new Point(i,j);\n" +
			"		return record;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	static IPoint a(int i, int j) throws record{\n" +
		"	                                     ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X() throws record {} \n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	X() throws record {} \n" +
		"	           ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n" +
			"	int a() throws record; \n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int a() throws record; \n" +
		"	               ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;" +
			"public class X {\n" +
			"	List<record> R = new List<record>();\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	List<record> R = new List<record>();\n" +
		"	     ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	List<record> R = new List<record>();\n" +
		"	                     ^^^^\n" +
		"Cannot instantiate the type List<record>\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	List<record> R = new List<record>();\n" +
		"	                          ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_027() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I<S> {\n" +
			"	void print(S arg);\n" +
			"}\n" +
			"public class X implements I<record>{\n" +
			"	void print(record arg){\n" +
			"		System.out.println(arg);\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	public class X implements I<record>{\n" +
		"	             ^\n" +
		"The type X must implement the inherited abstract method I<record>.print(record)\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	public class X implements I<record>{\n" +
		"	                            ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	void print(record arg){\n" +
		"	           ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_028() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Y<record> {\n" +
			"	void equal(record R) {}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	class Y<record> {\n" +
		"	        ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void equal(record R) {}\n" +
		"	           ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_029() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Y<record> {\n" +
			"	Y(record R) {}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	class Y<record> {\n" +
		"	        ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	Y(record R) {}\n" +
		"	  ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_030() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static record i= 0;\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	static record i= 0;\n" +
		"	       ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_031() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	record i=0;\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	record i=0;\n" +
		"	^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_032() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static int sum(record i, int param){\n" +
			"		return 1;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	static int sum(record i, int param){\n" +
		"	               ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_033() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X(record i, int param){\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	X(record i, int param){\n" +
		"	  ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_034() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int sum(record i, int num);\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int sum(record i, int num);\n" +
		"	        ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_035() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface Greetings {\n" +
				"  void greet(String head, String tail);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    Greetings g = (record, y) -> {\n" +
				"      System.out.println(record + y);\n" +
				"    };\n" +
				"    g.greet(\"Hello, \", \"World!\");\n" +
				"  }\n" +
				"}\n",
			},
			"Hello, World!"
			);
}
public void testBug564672_036() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Y {\n" +
			"	int sum(record this, int i, int num) {}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int sum(record this, int i, int num) {}\n" +
		"	        ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static record i;\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	static record i;\n" +
		"	       ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_038() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		for (record i = 0; i<10; i++) {\n" +
			"			System.out.println(0);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	for (record i = 0; i<10; i++) {\n" +
		"	     ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int rec[] = {1,2,3,4,5,6,7,8,9};\n" +
			"		for (record i: rec) {\n" +
			"			System.out.println(0);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	for (record i: rec) {\n" +
		"	     ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	for (record i: rec) {\n" +
		"	               ^^^\n" +
		"Type mismatch: cannot convert from element type int to record\n" +
		"----------\n");
}
public void testBug564672_040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try (record i = 0){\n" +
			"		}\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	try (record i = 0){\n" +
		"	     ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_041() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try{\n" +
			"		}\n" +
			"		catch (record e) {}\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	catch (record e) {}\n" +
		"	       ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_042() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"record Point(record x, int i) { }\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	record Point(record x, int i) { }\n" +
		"	             ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_043() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Point {\n" +
			"	<T> Point(T i) {\n" +
			"	}\n" +
			"	Point (int i, int j) {\n" +
			"		<record> this(null);\n" +
			"	}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	<record> this(null);\n" +
		"	 ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	<record> this(null);\n" +
		"	         ^^^^^^^^^^^\n" +
		"The constructor Point(record) refers to the missing type record\n" +
		"----------\n");
}
public void testBug564672_044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Point {\n" +
			"	<T> Point(T i) {\n" +
			"	}\n" +
			"}\n" +
			"class PointEx extends Point {\n" +
			"	PointEx (int i, int j) {\n" +
			"		<record> super(null);\n" +
			"	}\n;" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	<record> super(null);\n" +
		"	 ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	<record> super(null);\n" +
		"	         ^^^^^^^^^^^^\n" +
		"The constructor Point(record) refers to the missing type record\n" +
		"----------\n");
}
public void testBug564672_045() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Y {\n" +
			"	void m1() {} \n" +
			"	void m2() {\n" +
			"		this.<record>m1();" +
			"	}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	this.<record>m1();	}\n" +
		"	      ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	this.<record>m1();	}\n" +
		"	      ^^^^^^\n" +
		"Unused type arguments for the non generic method m1() of type Y; it should not be parameterized with arguments <record>\n" +
		"----------\n");
}
public void testBug564672_046() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Y{\n" +
			"	void a() {\n" +
			"		System.out.println(\"1\");\n" +
			"	}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		new <record>Y().a();\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	new <record>Y().a();\n" +
		"	     ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 8)\n" +
		"	new <record>Y().a();\n" +
		"	     ^^^^^^\n" +
		"Unused type arguments for the non generic constructor Y() of type Y; it should not be parameterized with arguments <record>\n" +
		"----------\n");
}
public void testBug564672_047() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface Y{}\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		new <record>Y() {\n" +
			"			void a() {\n" +
			"				System.out.println(\"1\");\n" +
			"			}\n" +
			"		}.a();\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	new <record>Y() {\n" +
		"	     ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	new <record>Y() {\n" +
		"	     ^^^^^^\n" +
		"Unused type arguments for the non generic constructor Object() of type Object; it should not be parameterized with arguments <record>\n" +
		"----------\n");
}
public void testBug564672_048() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Y{}\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		new <record>Y() {\n" +
			"			void a() {\n" +
			"				System.out.println(\"1\");\n" +
			"			}\n" +
			"		}.a();\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	new <record>Y() {\n" +
		"	     ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	new <record>Y() {\n" +
		"	     ^^^^^^\n" +
		"Unused type arguments for the non generic constructor Y() of type Y; it should not be parameterized with arguments <record>\n" +
		"----------\n");
}
public void testBug564672_049() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		record[] y= new record[3]; \n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	record[] y= new record[3]; \n" +
		"	^^^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	record[] y= new record[3]; \n" +
		"	                ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_050() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s=\"Hello\";\n" +
			"		record y= (record)s; \n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	record y= (record)s; \n" +
		"	^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	record y= (record)s; \n" +
		"	           ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_051() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s=\"Hello\";\n" +
			"		if (s instanceof record) { \n" +
			"			System.out.println(1);\n" +
			"		}\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (s instanceof record) { \n" +
		"	                 ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
public void testBug564672_052() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		List<String> messages = Arrays.asList(\"hello\", \"java\", \"testers!\");\n" +
			"		messages.forEach(record::length);\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	messages.forEach(record::length);\n" +
		"	                 ^^^^^^\n" +
		"record cannot be resolved\n" +
		"----------\n");
}
public void testBug564672_053() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		List<String> messages = Arrays.asList(\"hello\", \"java\", \"testers!\");\n" +
			"		messages.stream().map(record::new).toArray(record[]::new);\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	messages.stream().map(record::new).toArray(record[]::new);\n" +
		"	                      ^^^^^^\n" +
		"record cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	messages.stream().map(record::new).toArray(record[]::new);\n" +
		"	                                           ^^^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n");
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_001() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class X extends record {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n" +
			"class record {}\n"
		},
		"0",
		options
	);
}
public void testBug564672b_002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X extends record {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	class X extends record {\n" +
		"	                ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n",
		null,
		true);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_003() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class X implements record {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"+
			"interface record {}\n;"
		},
		"0",
		options
	);
}
public void testBug564672b_004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X implements record {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	class X implements record {\n" +
		"	                   ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n",
		null,
		true);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_005() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  class Y extends record {\n"+
			"  }\n" +
			"  class record {}\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"0",
		options
	);
}
public void testBug564672b_006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  class Y extends record {\n"+
			"  }\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y extends record {\n" +
		"	                ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n",
		null,
		true);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_007() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  class Y implements record {\n"+
			"  }\n" +
			"  interface record {}\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"0",
		options
	);
}
public void testBug564672b_008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  class Y implements record {\n"+
			"  }\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y implements record {\n" +
		"	                   ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n",
		null,
		true);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_009() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"interface Y extends record {\n"+
			"}\n" +
			"interface record {}\n" +
			"class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"0",
		options
	);
}
public void testBug564672b_010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface Y extends record {\n"+
			"}\n" +
			"class X {\n"+
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	interface Y extends record {\n" +
		"	                    ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n",
		null,
		true);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_011() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  interface Y extends record {\n"+
			"  }\n" +
			"  interface record {}\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"0",
		options
	);
}
public void testBug564672b_012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  interface Y extends record {\n"+
			"  }\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	interface Y extends record {\n" +
		"	                    ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n",
		null,
		true);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_013() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"interface Z {\n"+
			"  class Y extends record {\n"+
			"  }\n" +
			"  class record {}\n" +
			"}\n" +
			"class X {\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n" +
			"}\n"
		},
		"0",
		options
	);
}
public void testBug564672b_014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n"+
			"  class Y extends record {\n"+
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y extends record {\n" +
		"	                ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n",
		null,
		true);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_015() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"interface Z {\n"+
			"  class Y implements record {\n"+
			"  }\n" +
			"  interface record {}\n" +
			"}\n" +
			"class X {\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n" +
			"}\n"
		},
		"0",
		options
	);
}
public void testBug564672b_016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n"+
			"  class Y implements record {\n"+
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y implements record {\n" +
		"	                   ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n",
		null,
		true);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_017() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"interface Z {\n"+
			"  interface Y extends record {\n"+
			"  }\n" +
			"  interface record {}\n" +
			"}\n" +
			"class X {\n" +
			"  public static void main(String[] args){\n"+
			"     System.out.println(0);\n" +
			"  }\n" +
			"}\n"
		},
		"0",
		options
	);
}
public void testBug564672b_018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n"+
			"  interface Y extends record {\n"+
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	interface Y extends record {\n" +
		"	                    ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n",
		null,
		true);
}
public void testBug564672b_019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static record a(int i, int j) {\n" +
			"		record r=new record(i,j);\n" +
			"		return r;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	record r=new record(i,j);\n" +
		"	^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	record r=new record(i,j);\n" +
		"	       ^\n" +
		"Instance fields may not be declared in a record class\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	record r=new record(i,j);\n" +
		"	             ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	return r;\n" +
		"	^^^^^^\n" +
		"Syntax error on token \"return\", byte expected\n" +
		"----------\n",
		null,
		true);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_020() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	class record {}\n" +
			"\n" +
			"	static record a(int i, int j) {\n" +
			"		record r = new X().new record();\n" +
			"		return r;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_021() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X() throws record {} \n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class record extends Exception {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_022() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"interface Y {\n" +
			"	int a() throws record;\n" +
			"}\n" +
			"\n" +
			"class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class record extends Exception {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_023() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	List<record> R = new ArrayList<record>();\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record{}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_024() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I<S> {\n" +
			"	void print(S arg);\n" +
			"}\n" +
			"\n" +
			"public class X implements I<record> {\n" +
			"	public void print(record arg) {\n" +
			"		System.out.println(arg);\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record {\n" +
			"}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_025() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class Y<record> {\n" +
			"	void equal(record R) {}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_026() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class Y<record> {\n" +
			"	Y(record R) {}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_027() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static record i;\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record {}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_028() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	record i = new record(0);\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record {\n" +
			"	int i;\n" +
			"	record (int i) {\n" +
			"		this.i=i;\n" +
			"	}\n" +
			"}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_029() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static int sum(record i, int param) {\n" +
			"		return 1;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record{}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_030() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X(record i, int param){\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record{}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_031() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"	int sum(record i, int num);\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record{}"
		},
		"0",
		options
	);
}
public void testBug564672b_032() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface Greetings {\n" +
				"  void greet(String head, String tail);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    Greetings g = (record, y) -> {\n" +
				"      System.out.println(record + y);\n" +
				"    };\n" +
				"    g.greet(\"Hello, \", \"World!\");\n" +
				"  }\n" +
				"}\n",
			},
			"Hello, World!");
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_033() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class record {\n" +
			"	int sum(record this, int i, int num) {\n" +
			"		return 0;\n" +
			"	}\n" +
			"}"
		},
		"0",
		options
	);
}
public void testBug564672b_034() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static Rec record;\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class Rec {}\n"
		},
		"0");
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_035() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.Iterator;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int rec[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
			"		String s=\"\";\n" +
			"		List <record> recList= new ArrayList<>();\n" +
			"		for (int i:rec) {\n" +
			"			recList.add(new record(i));\n" +
			"		}\n" +
			"		for (Iterator<record> i =recList.iterator(); i.hasNext();) {\n" +
			"			s=s+i.next()+\" \";\n" +
			"		}\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class record {\n" +
			"	int i;\n" +
			"	record (int i) {\n" +
			"		this.i=i;\n" +
			"	}\n" +
			"	public String toString (){\n" +
			"		return Integer.toString(i);\n" +
			"	}\n" +
			"}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_036() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int rec[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
			"		String s=\"\";\n" +
			"		List <record> recList= new ArrayList<>();\n" +
			"		for (int i:rec) {\n" +
			"			recList.add(new record(i));\n" +
			"		}\n" +
			"		for (record i : recList) {\n" +
			"			s=s+i+\" \";\n" +
			"		}\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class record {\n" +
			"	int i;\n" +
			"	record (int i) {\n" +
			"		this.i=i;\n" +
			"	}\n" +
			"	public String toString (){\n" +
			"		return Integer.toString(i);\n" +
			"	}\n" +
			"}\n"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_037() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try (record i = new record (0)){\n" +
			"		} catch (Exception e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record implements AutoCloseable{\n" +
			"	int i;\n" +
			"	record (int i) {\n" +
			"		this.i=i;\n" +
			"	}\n" +
			"	@Override\n" +
			"	public void close() throws Exception {}\n" +
			"}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_038() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		try {\n" +
			"			throw new record();\n" +
			"		} catch (record e) {\n" +
			"			System.out.println(\"0\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class record extends Exception {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		},
		"0",
		options
	);
}
public void testBug564672b_039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"record Point(record x, int i) { }\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record {}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	record Point(record x, int i) { }\n" +
		"	             ^^^^^^\n" +
		"'record' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	class record {}\n" +
		"	      ^^^^^^\n" +
		"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
		"----------\n",
		null,
		true);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_040() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class Point {\n" +
			"	<T> Point(T i) {\n" +
			"	}\n" +
			"	Point (int i, int j) {\n" +
			"		<record> this(null);\n" +
			"	}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record {}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_041() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class Point {\n" +
			"	<T> Point(T i) {\n" +
			"	}\n" +
			"}\n" +
			"class PointEx extends Point {\n" +
			"	PointEx (int i, int j) {\n" +
			"		<record> super(null);\n" +
			"	}\n;" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record {}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_042() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class Y {\n" +
			"	<T> void m1() {} \n" +
			"	void m2() {\n" +
			"		this.<record>m1();" +
			"	}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record {}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_043() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class Y{\n" +
			"	<T> Y() {}\n" +
			"	void a() {\n" +
			"		System.out.println(\"1\");\n" +
			"	}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		new <record>Y().a();\n" +
			"	}\n" +
			"}\n" +
			"class record {}"
		},
		"1",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_044() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"interface Y{\n" +
			"}\n" +
			"\n" +
			"class Z implements Y {\n" +
			"	<T> Z() {\n" +
			"		\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		new <record>Z() {\n" +
			"			void a() {\n" +
			"				System.out.println(\"1\");\n" +
			"			}\n" +
			"		}.a();\n" +
			"	}\n" +
			"}\n" +
			"class record {}"
		},
		"1",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_045() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"class Y{" +
			"	<T> Y() {\n" +
			"	}" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		new <record>Y() {\n" +
			"			void a() {\n" +
			"				System.out.println(\"1\");\n" +
			"			}\n" +
			"		}.a();\n" +
			"	}\n" +
			"}\n" +
			"class record {}"
		},
		"1",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_046() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		record[] y= new record[3]; \n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}" +
			"class record {}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_047() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		StrRec s = new StrRec(\"Hello\");\n" +
			"		record y = (record) s;\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class record {\n" +
			"}\n" +
			"\n" +
			"class StrRec extends record {\n" +
			"	String s;\n" +
			"\n" +
			"	StrRec(String s) {\n" +
			"		this.s = s;\n" +
			"	}\n" +
			"}"
		},
		"0",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_048() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		StrRec s=new StrRec(\"Hello\");\n" +
			"		if (s instanceof record) { \n" +
			"			System.out.println(1);\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class record {}\n" +
			"\n" +
			"class StrRec extends record {\n" +
			"	String s;\n" +
			"\n" +
			"	StrRec(String s) {\n" +
			"		this.s = s;\n" +
			"	}\n" +
			"}"
		},
		"1",
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testBug564672b_049() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_15);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_15);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		List<String> messages = Arrays.asList(\"hello\", \"java\", \"testers!\");\n" +
			"		\n" +
			"		messages.stream().map(record::new).toArray(record[]::new);;\n" +
			"		System.out.println(0);\n" +
			"	}\n" +
			"}\n" +
			"class record {\n" +
			"	String s;\n" +
			"\n" +
			"	record(String s) {\n" +
			"		this.s = s;\n" +
			"	}\n" +
			"}"
		},
		"0",
		options
	);
}
public void testBug565388_001() {
	if (this.complianceLevel < ClassFileConstants.JDK17) return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public non-sealed record X() {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public non-sealed record X() {}\n" +
		"	                         ^\n" +
		"Illegal modifier for the record X; only public, final and strictfp are permitted\n" +
		"----------\n",
		null,
		true);
}
public void testBug565388_002() {
	if (this.complianceLevel < ClassFileConstants.JDK17) return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public sealed record X() {}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public sealed record X() {}\n" +
		"	                     ^\n" +
		"Illegal modifier for the record X; only public, final and strictfp are permitted\n" +
		"----------\n",
		null,
		true);
}
public void testBug565786_001() throws IOException, ClassFormatException {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n"+
			"        System.out.println(0);\n"+
			"   }\n"+
			"}\n"+
			"interface I {\n"+
			"    record R() {}\n"+
			"}",
		},
		"0");
	String expectedOutput =
			"  // Method descriptor #24 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public I$R();\n";
	verifyClassFile(expectedOutput, "I$R.class", ClassFileBytesDisassembler.SYSTEM);
}
// Test that without an explicit canonical constructor, we
// report the warning on the record type.
public void testBug563182_01() {
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> {\n" +
			"	record Point<T> (T ... args) { // 1\n" +
			"	}\n" +
			"   public static void main(String[] args) {}\n"+
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	record Point<T> (T ... args) { // 1\n" +
		"	                       ^^^^\n" +
		"Type safety: Potential heap pollution via varargs parameter args\n" +
		"----------\n",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of an explicit canonical constructor that is NOT annotated with @SafeVarargs,
// we don't report the warning on the record type but report on the explicit canonical constructor
public void testBug563182_02() {
	getPossibleComplianceLevels();
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> {\n" +
			"	record Point<T> (T ... args) { // 1\n" +
			"		Point(T ... args) { // 2\n" +
			"			this.args = args;\n" +
			"		}\n" +
			"	}\n" +
			"   public static void main(String[] args) {}\n"+
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	Point(T ... args) { // 2\n" +
		"	            ^^^^\n" +
		"Type safety: Potential heap pollution via varargs parameter args\n" +
		"----------\n",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of an explicit canonical constructor that IS annotated with @SafeVarargs,
//we don't report the warning on neither the record type nor the explicit canonical constructor
public void testBug563182_03() {
	getPossibleComplianceLevels();
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> {\n" +
			"	record Point<T> (T ... args) { // 1\n" +
			"		@SafeVarargs\n" +
			"		Point(T ... args) { // 2\n" +
			"			this.args = args;\n" +
			"		}\n" +
			"	}\n" +
			"   public static void main(String[] args) {}\n"+
			"}\n",
		},
		"",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of a compact canonical constructor that is NOT annotated with @SafeVarargs,
//we don't report the warning on the compact canonical constructor but report on the record type
public void testBug563182_04() {
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> {\n" +
			"	record Point<T> (T ... args) { // 1\n" +
			"		Point { // 2\n" +
			"		}\n" +
			"	}\n" +
			"   public static void main(String[] args) {}\n"+
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	record Point<T> (T ... args) { // 1\n" +
		"	                       ^^^^\n" +
		"Type safety: Potential heap pollution via varargs parameter args\n" +
		"----------\n",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of a compact canonical constructor that IS annotated with @SafeVarargs,
//we don't report the warning on neither the record type nor the compact canonical constructor
public void testBug563182_05() {
	getPossibleComplianceLevels();
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> {\n" +
			"	record Point<T> (T ... args) { // 1\n" +
			"		@SafeVarargs\n" +
			"		Point { // 2\n" +
			"		}\n" +
			"	}\n" +
			"   public static void main(String[] args) {}\n"+
			"}\n",
		},
		"",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of a non-canonical constructor that is annotated with @SafeVarargs,
//we don't report the warning on the non-canonical constructor but report on the record type
public void testBug563182_06() {
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> {\n" +
			"	record Point<T> (T ... args) { // 1\n" +
			"		@SafeVarargs\n" +
			"		Point (String s, T ... t) {\n" +
			"			this(t);\n" +
			"		}\n" +
			"	}\n" +
			"   public static void main(String[] args) {}\n"+
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	record Point<T> (T ... args) { // 1\n" +
		"	                       ^^^^\n" +
		"Type safety: Potential heap pollution via varargs parameter args\n" +
		"----------\n",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
//Test that in presence of a non-canonical constructor that is NOT annotated with @SafeVarargs,
//we don't report the warning on the non-canonical constructor but report on the record type
public void testBug563182_07() {
	Map<String, String> customOptions = getCompilerOptions();
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> {\n" +
			"	record Point<T> (T ... args) { // 1\n" +
			"		Point (String s, T ... t) {\n" +
			"			this(t);\n" +
			"		}\n" +
			"	}\n" +
			"   public static void main(String[] args) {}\n"+
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	record Point<T> (T ... args) { // 1\n" +
		"	                       ^^^^\n" +
		"Type safety: Potential heap pollution via varargs parameter args\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 3)\n" +
		"	Point (String s, T ... t) {\n" +
		"	                       ^\n" +
		"Type safety: Potential heap pollution via varargs parameter t\n" +
		"----------\n",
		null,
		true,
		new String[] {"--enable-preview"},
		customOptions);
}
	public void testBug563186_01() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"  private record Point(int myInt){\n"+
					"  	 @Override\n" +
					"  	 public int myInt(){\n"+
					"      return this.myInt;\n" +
					"    }\n"+
					"  }\n"+
					"    public static void main(String[] args) {\n"+
					"        System.out.println(0);\n"+
					"   }\n"+
					"}\n"
				},
			 "0");
	}
	public void testBug563186_02() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"  private record Point(int myInt){\n"+
					"  	 public int myInt(){\n"+
					"      return this.myInt;\n" +
					"    }\n"+
					"  }\n"+
					"    public static void main(String[] args) {\n"+
					"        System.out.println(0);\n"+
					"   }\n"+
					"}\n"
				},
			 "0");
	}
	public void testBug563186_03() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"  private record Point(int myInt){\n"+
					"  	 @Override\n" +
					"  	 public int myInt(int i){\n"+
					"      return this.myInt;\n" +
					"    }\n"+
					"  }\n"+
					"    public static void main(String[] args) {\n"+
					"        System.out.println(0);\n"+
					"   }\n"+
					"}\n"
				},
				"----------\n" +
				"1. WARNING in X.java (at line 2)\n" +
				"	private record Point(int myInt){\n" +
				"	               ^^^^^\n" +
				"The type X.Point is never used locally\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	public int myInt(int i){\n" +
				"	           ^^^^^^^^^^^^\n" +
				"The method myInt(int) of type X.Point must override or implement a supertype method\n" +
				"----------\n",
				null,
				true);
	}
	public void testBug563186_04() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n"+
					"  private record Point(int myInt){\n"+
					"  	 public int myInt(int i){\n"+
					"      return this.myInt;\n" +
					"    }\n"+
					"  }\n"+
					"    public static void main(String[] args) {\n"+
					"        System.out.println(0);\n"+
					"   }\n"+
					"}\n"
				},
			 "0");
	}
	public void testBug565732_01() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public record X {\n" +
					"} "
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public record X {\n" +
				"	              ^\n" +
				"Syntax error, insert \"RecordHeader\" to complete RecordHeaderPart\n" +
				"----------\n",
				null,
				true);
	}
	public void testBug565732_02() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public record X<T> {\n" +
					"} "
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public record X<T> {\n" +
				"	                 ^\n" +
				"Syntax error, insert \"RecordHeader\" to complete RecordHeaderPart\n" +
				"----------\n",
				null,
				true);
	}
	// Test that a record without any record components was indeed compiled
	// to be a record at runtime
	public void testBug565732_03() {
		runConformTest(
				new String[] {
					"X.java",
					"public record X() {\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(X.class.getSuperclass().getName());\n" +
					"	}\n" +
					"}"
				},
			 "java.lang.Record");
	}
	// Test that a record without any record components was indeed compiled
	// to be a record at runtime
	public void testBug565732_04() {
		runConformTest(
				new String[] {
					"X.java",
					"public record X<T>() {\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(X.class.getSuperclass().getName());\n" +
					"	}\n" +
					"}"
				},
			 "java.lang.Record");
	}
	// Test that a "record" can be used as a method name and invoked inside a record
	public void testBug565732_05() {
		runConformTest(
				new String[] {
					"X.java",
					"public record X<T>() {\n" +
					"	public static void main(String[] args) {\n" +
					"		record();\n" +
					"	}\n" +
					"	public static void record() {\n" +
					"		System.out.println(\"record()\");\n" +
					"	}\n" +
					"}"
				},
			 "record()");
	}
	// Test that a "record" can be used as a label and invoked inside a record
	public void testBug565732_06() {
		runConformTest(
				new String[] {
					"X.java",
					"public record X<T>() {\n" +
					"	public static void main(String[] args) {\n" +
					"		boolean flag = true;\n" +
					"		record: {\n" +
					"			if (flag) {\n" +
					"				System.out.println(\"record:\");\n" +
					"				flag = false;\n" +
					"				break record;\n" +
					"			}\n" +
					"		}\n" +
					"	}\n" +
					"}"
				},
			 "record:");
	}
	public void testBug565732_07() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	record R {};\n" +
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	record R {};\n" +
				"	       ^\n" +
				"Syntax error, insert \"RecordHeader\" to complete RecordHeaderPart\n" +
				"----------\n",
				null,
				true);
	}
	public void testBug565732_08() {
		runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(R.class.getSuperclass().getName());\n" +
					"	}\n" +
					"	record R() {};\n" +
					"}"
				},
			 "java.lang.Record");
	}
	public void testBug565830_01() {
		runConformTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"    void bar() throws Exception {\n"+
			"        record Bar(int x) implements java.io.Serializable {\n"+
			"            void printMyFields() {\n"+
			"                for (var field : this.getClass().getDeclaredFields()) {\n"+
			"                    System.out.println(field);\n"+
			"                }\n"+
			"            }\n"+
			"        }\n"+
			"        var bar = new Bar(1);\n"+
			"        bar.printMyFields();\n"+
			"        new java.io.ObjectOutputStream(java.io.OutputStream.nullOutputStream()).writeObject(bar);\n"+
			"    }\n"+
			"    public static void main(String[] args) throws Exception {\n"+
			"        new X().bar();\n"+
			"    }\n"+
			"}",
		},
		"private final int X$1Bar.x");
	}
public void testBug566063_001() {
	runConformTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    void bar() throws Exception {\n"+
				"        enum E {\n"+
				"               ONE,\n"+
				"               TWO\n"+
				"        }\n"+
				"        interface I {}\n"+
				"        record Bar(E x) implements I{}\n"+
				"        E e = new Bar(E.ONE).x();\n"+
				"        System.out.println(e);\n"+
				"    }\n"+
				"    public static void main(String[] args) throws Exception {\n"+
				"       new X().bar();\n"+
				"    }\n"+
				"}"
			},
			"ONE");
}
public void testBug566063_002() {
	runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    void bar() throws Exception {\n"+
				"        static enum E {\n"+
				"               ONE,\n"+
				"               TWO\n"+
				"        }\n"+
				"        interface I {}\n"+
				"        record Bar(E x) implements I{}\n"+
				"        E e = new Bar(E.ONE).x();\n"+
				"        System.out.println(e);\n"+
				"    }\n"+
				"    public static void main(String[] args) throws Exception {\n"+
				"       new X().bar();\n"+
				"    }\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	static enum E {\n" +
			"	            ^\n" +
			"Illegal modifier for local enum E; no explicit modifier is permitted\n" +
			"----------\n",
			null,
			true);
}
public void testBug566063_003() {
	runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    void bar() throws Exception {\n"+
				"        static enum E {\n"+
				"               ONE,\n"+
				"               TWO\n"+
				"        }\n"+
				"        static interface I {}\n"+
				"        static record Bar(E x) implements I{}\n"+
				"        E e = new Bar(E.ONE).x();\n"+
				"        System.out.println(e);\n"+
				"    }\n"+
				"    public static void main(String[] args) throws Exception {\n"+
				"       new X().bar();\n"+
				"    }\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	static enum E {\n" +
			"	            ^\n" +
			"Illegal modifier for local enum E; no explicit modifier is permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	static interface I {}\n" +
			"	                 ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	static record Bar(E x) implements I{}\n" +
			"	              ^^^\n" +
			"A local class or interface Bar is implicitly static; cannot have explicit static declaration\n" +
			"----------\n",
			null,
			true);
}
public void testBug566063_004() {
	this.runConformTest(
			new String[] {
					"X.java",
					"class X {\n"+
					"    void bar() throws Exception {\n"+
					"        enum E {\n"+
					"               ONE,\n"+
					"               TWO\n"+
					"        }\n"+
					"		 interface I {}\n" +
					"        record Bar(E x) implements I{}\n"+
					"        E e = new Bar(E.ONE).x();\n"+
					"        System.out.println(e);\n"+
					"    }\n"+
					"    public static void main(String[] args) throws Exception {\n"+
					"       new X().bar();\n"+
					"    }\n"+
					"}"
				},
				"ONE");
}
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testBug566418_001() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	this.runNegativeTest(
	new String[] {
			"X.java",
			"public class X {\n"+
			" static void foo() {\n"+
			"   record R() {\n"+
			"     static int create(int lo) {\n"+
			"       return lo;\n"+
			"     }\n"+
			"   }\n"+
			"   System.out.println(R.create(0));\n"+
			"   }\n"+
			"   Zork();\n"+
			"}",
		},
	"----------\n" +
	"1. ERROR in X.java (at line 10)\n" +
	"	Zork();\n" +
	"	^^^^^^\n" +
	"Return type for the method is missing\n" +
	"----------\n" +
	"2. ERROR in X.java (at line 10)\n" +
	"	Zork();\n" +
	"	^^^^^^\n" +
	"This method requires a body instead of a semicolon\n" +
	"----------\n",
		null,
		true,
		options
	);
}
public void testBug565787_01() {
	runConformTest(
		new String[] {
			"X.java",
			"public record X(String s)   {\n"+
			"    public X  {\n"+
			"        s.codePoints()\n"+
			"        .forEach(cp -> System.out.println((java.util.function.Predicate<String>) \"\"::equals));\n"+
			"    }\n"+
			"    public static void main(String[] args) {\n"+
			"        X a = new X(\"\");\n"+
			"        a.equals(a);\n"+
			"    }\n"+
			"}",
		},
		"");
}
public void testBug566554_01() {
	runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"	public static void main(String[] args) {\n" +
			"		final Margin margins = new Margin(0);\n" +
			"		System.out.println(margins.left()); \n" +
			"	}\n" +
			"}\n" +
			"record Margin(int left) {\n" +
			"	public Margin left(int value) {\n" +
			"		return new Margin(value);\n" +
			"	}\n" +
			"	public String toString() {\n" +
			"		return \"Margin[left=\" + this.left + \"]\";\n" +
			"	}\n" +
			"}",
		},
		"0");
}
public void testBug566554_02() {
	runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"	public static void main(String[] args) {\n" +
			"		final Margin margins = new Margin(0);\n" +
			"		System.out.println(margins.left()); \n" +
			"	}\n" +
			"}\n" +
			"record Margin(int left) {\n" +
			"	public Margin left(int value) {\n" +
			"		return new Margin(value);\n" +
			"	}\n" +
			"	public int left() {\n" +
			"		return this.left;\n" +
			"	}\n" +
			"	public String toString() {\n" +
			"		return \"Margin[left=\" + this.left + \"]\";\n" +
			"	}\n" +
			"}",
		},
		"0");
}
public void testBug566554_03() {
	runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"	public static void main(String[] args) {\n" +
			"		final Margin margins = new Margin(0);\n" +
			"		System.out.println(margins.left(0)); \n" +
			"	}\n" +
			"}\n" +
			"record Margin(int left) {\n" +
			"	public Margin left(int value) {\n" +
			"		return new Margin(value);\n" +
			"	}\n" +
			"	public int left() {\n" +
			"		return this.left;\n" +
			"	}\n" +
			"	public String toString() {\n" +
			"		return \"Margin[left=\" + this.left + \"]\";\n" +
			"	}\n" +
			"}",
		},
		"Margin[left=0]");
}
public void testBug566554_04() {
	runNegativeTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"	public static void main(String[] args) {\n" +
			"		final Margin margins = new Margin(0);\n" +
			"		int l = margins.left(0); \n" +
			"	}\n" +
			"}\n" +
			"record Margin(int left) {\n" +
			"	public Margin left(int value) {\n" +
			"		return new Margin(value);\n" +
			"	}\n" +
			"	public int left() {\n" +
			"		return this.left;\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in Main.java (at line 4)\n" +
		"	int l = margins.left(0); \n" +
		"	        ^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Margin to int\n" +
		"----------\n");
}
public void testBug567731_001() {
	if (this.complianceLevel < ClassFileConstants.JDK17)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  non-sealed record R() {}\n" +
			"  public static void main(String[] args) {\n" +
			"	  sealed record B() { }  \n" +
			"  }" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	non-sealed record R() {}\n" +
		"	                  ^\n" +
		"Illegal modifier for the record R; only public, private, protected, static, final and strictfp are permitted\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	sealed record B() { }  \n" +
		"	              ^\n" +
		"Illegal modifier for the local record B; only final and strictfp are permitted\n" +
		"----------\n",
		null,
		true);
}
public void testBug567731_002() {
	if (this.complianceLevel < ClassFileConstants.JDK17)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  sealed record R1() {}\n" +
			"  public static void main(String[] args) {\n" +
			"	  non-sealed record R2() { }  \n" +
			"  }" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	sealed record R1() {}\n" +
		"	              ^^\n" +
		"Illegal modifier for the record R1; only public, private, protected, static, final and strictfp are permitted\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	non-sealed record R2() { }  \n" +
		"	                  ^^\n" +
		"Illegal modifier for the local record R2; only final and strictfp are permitted\n" +
		"----------\n",
		null,
		true);
}
public void testBug566846_1() {
	if (this.complianceLevel < ClassFileConstants.JDK24)
		return;
	runNegativeTest(
			new String[] {
				"X.java",
				"public record X;\n"
			},
			(this.complianceLevel < ClassFileConstants.JDK25 ?
					"----------\n"
					+ "1. ERROR in X.java (at line 1)\n"
					+ "	public record X;\n"
					+ "	^\n"
					+ "The Java feature 'Compact Source Files and Instance Main Methods' is only available with source level 25 and above\n"
					: "") +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public record X;\n" +
			"	^\n" +
			"Implicitly declared class must have a candidate main method\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 1)\n" +
			"	public record X;\n" +
			"	       ^^^^^^\n" +
			"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
			"----------\n",
			null,
			true);
}
public void testBug566846_2() {
	if (this.complianceLevel < ClassFileConstants.JDK24)
		return;
	runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"
				+ "} \n"
				+ "record R1;\n"
			},
			(this.complianceLevel < ClassFileConstants.JDK25 ?
					"----------\n"
					+ "1. ERROR in X.java (at line 1)\n"
					+ "	public class X {\n"
					+ "	^\n"
					+ "The Java feature 'Compact Source Files and Instance Main Methods' is only available with source level 25 and above\n"
					: "") +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public class X {\n" +
			"	^\n" +
			"Implicitly declared class must have a candidate main method\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	record R1;\n" +
			"	^^^^^^\n" +
			"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
			"----------\n",
			null,
			true);
}
public void testBug561199_001() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.ERROR);
	runNegativeTest(
			new String[] {
				"R.java",
				"record R() implements java.io.Serializable {}\n",
				"X.java",
				"class X implements java.io.Serializable {}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X implements java.io.Serializable {}\n" +
			"	      ^\n" +
			"The serializable class X does not declare a static final serialVersionUID field of type long\n" +
			"----------\n",
			null,
			true,
			new String[] {"--enable-preview"},
			options);
}
public void testBug568922_001() {
	runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   record R() {\n"+
				"     R  {\n"+
				"       super();\n"+
				"       System.out.println(\"helo\");\n"+
				"     }\n"+
				"   }\n"+
				"   new R();\n"+
				" }\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"The body of a compact constructor must not contain an explicit constructor call\n" +
			"----------\n",
			null,
			true);
}
public void testBug568922_002() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			" public static void main(String[] args) {\n"+
			"   record R() {\n"+
			"     R  {\n"+
			"       System.out.println(\"helo\");\n"+
			"     }\n"+
			"   }\n"+
			"   new R();\n"+
			" }\n"+
			"}"
		},
		"helo");
}
public void testBug570243_001() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.reflect.Parameter;\n"+
			"  \n"+
			"public record X(int myCompOne) {\n"+
			"       public static void main(String[] x1) {\n"+
			"        try {\n"+
			"            Parameter param = Class.forName(\"X\").getConstructors()[0].getParameters()[0];\n"+
			"               System.out.println(param.getType().getSimpleName()+\" \"+ param.getName());\n"+
			"        } catch(ClassNotFoundException e) {\n"+
			"               // do nothing\n"+
			"        }\n"+
			"       }\n"+
			"}"
		},
		"int myCompOne");
}
public void testBug570243_002() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.reflect.Parameter;\n"+
			"  \n"+
			"public record X(int myCompOne, char myCompChar) {\n"+
			"       public static void main(String[] x1) {\n"+
			"        try {\n"+
			"            Parameter[] params = Class.forName(\"X\").getConstructors()[0].getParameters();\n"+
			"            for (Parameter param : params)\n"+
			"               System.out.println(param.getType().getSimpleName()+\" \"+ param.getName());\n"+
			"        } catch(ClassNotFoundException e) {\n"+
			"               // do nothing\n"+
			"        }\n"+
			"       }\n"+
			"}"
		},
		"int myCompOne\n"+
		"char myCompChar");
}
public void testBug570243_003() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.reflect.Parameter;\n"+
			"  \n"+
			"public record X(int myCompOne, char ...myCompChar) {\n"+
			"       public static void main(String[] x1) {\n"+
			"        try {\n"+
			"            Parameter[] params = Class.forName(\"X\").getConstructors()[0].getParameters();\n"+
			"            for (Parameter param : params)\n"+
			"               System.out.println(param.getType().getSimpleName()+\" \"+ param.getName());\n"+
			"        } catch(ClassNotFoundException e) {\n"+
			"               // do nothing\n"+
			"        }\n"+
			"       }\n"+
			"}"
		},
		"int myCompOne\n"+
		"char[] myCompChar");
}
public void testBug570230_001() {
	runNegativeTest(
			new String[] {
				"X.java",
				"public record X(int marr[]) {}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public record X(int marr[]) {}\n" +
			"	                    ^^^^\n" +
			"Extended dimensions are illegal for a record component\n" +
			"----------\n");
}
public void testBug571015_001() {
	runNegativeTest(
			new String[] {
				"X.java",
				"record R() {\n"+
				"       R(I<T> ... t) {}\n"+
				"}\n"+
				"interface I{}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	R(I<T> ... t) {}\n" +
			"	  ^\n" +
			"The type I is not generic; it cannot be parameterized with arguments <T>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	R(I<T> ... t) {}\n" +
			"	    ^\n" +
			"T cannot be resolved to a type\n" +
			"----------\n");
}
public void testBug571015_002() {
	runNegativeTest(
			new String[] {
				"X.java",
				"record R() {\n"+
				"       R(I<X> ... t) {}\n"+
				"}\n"+
				"interface I<T>{}\n"+
				"class X{}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	R(I<X> ... t) {}\n" +
			"	^^^^^^^^^^^^^\n" +
			"A non-canonical constructor must invoke another constructor of the same class\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 2)\n" +
			"	R(I<X> ... t) {}\n" +
			"	           ^\n" +
			"Type safety: Potential heap pollution via varargs parameter t\n" +
			"----------\n");
}
public void testBug571038_1() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"
			+ " public static void main(String[] args) {\n"
			+ "   System.out.println(\"hello\");\n"
			+ " }\n"
			+ "}\n"
			+ "record MyRecord<T> (MyIntf<T>... t) {\n"
			+ "	public MyRecord(MyIntf<T>... t) {\n"
			+ "		this.t = null;\n"
			+ "	}\n"
			+ "}\n"
			+ "interface MyIntf<T> {}\n"
		},
	 "hello");
	String expectedOutput = "  // Method descriptor #25 ()[LMyIntf;\n"
			+ "  // Signature: ()[LMyIntf<TT;>;\n"
			+ "  // Stack: 1, Locals: 1\n"
			+ "  public MyIntf[] t();\n";
	verifyClassFile(expectedOutput, "MyRecord.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug571038_2() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"
			+ " public static void main(String[] args) {\n"
			+ "   System.out.println(\"hello\");\n"
			+ " }\n"
			+ "}\n"
			+ "record MyRecord<T> (MyIntf<T>... t) {\n"
			+ "	@SafeVarargs\n"
			+ "	public MyRecord(MyIntf<T>... t) {\n"
			+ "		this.t = null;\n"
			+ "	}\n"
			+ "}\n"
			+ "interface MyIntf<T> {}\n"
		},
	 "hello");
	String expectedOutput = "  // Method descriptor #27 ()[LMyIntf;\n"
			+ "  // Signature: ()[LMyIntf<TT;>;\n"
			+ "  // Stack: 1, Locals: 1\n"
			+ "  public MyIntf[] t();\n";
	verifyClassFile(expectedOutput, "MyRecord.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug571038_3() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.*;\n"
			+ "public class X {\n"
			+ " public static void main(String[] args) {\n"
			+ "   System.out.println(\"hello\");\n"
			+ " }\n"
			+ "}\n"
			+ "record MyRecord<T> (MyIntf<T>... t) {\n"
			+ "	@SafeVarargs\n"
			+ "	public MyRecord(@MyAnnot MyIntf<T>... t) {\n"
			+ "		this.t = null;\n"
			+ "	}\n"
			+ "}\n"
			+ "interface MyIntf<T> {}\n"
			+ "@Retention(RetentionPolicy.RUNTIME)\n"
			+ "@interface MyAnnot {}\n"
		},
	 "hello");
	String expectedOutput = "  // Method descriptor #29 ()[LMyIntf;\n"
			+ "  // Signature: ()[LMyIntf<TT;>;\n"
			+ "  // Stack: 1, Locals: 1\n"
			+ "  public MyIntf[] t();\n";
	verifyClassFile(expectedOutput, "MyRecord.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug571038_4() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.*;\n"
			+ "public class X {\n"
			+ " public static void main(String[] args) {\n"
			+ "   System.out.println(\"hello\");\n"
			+ " }\n"
			+ "}\n"
			+ "record MyRecord<T> (MyIntf<T>... t) {\n"
			+ "	@SafeVarargs\n"
			+ "	public MyRecord(MyIntf<@MyAnnot T>... t) {\n"
			+ "		this.t = null;\n"
			+ "	}\n"
			+ "}\n"
			+ "interface MyIntf<T> {}\n"
			+ "@Retention(RetentionPolicy.RUNTIME)\n"
			+ "@java.lang.annotation.Target(ElementType.TYPE_USE)\n"
			+ "@interface MyAnnot {}\n"
		},
	 "hello");
	String expectedOutput = "  // Method descriptor #29 ()[LMyIntf;\n"
			+ "  // Signature: ()[LMyIntf<TT;>;\n"
			+ "  // Stack: 1, Locals: 1\n"
			+ "  public MyIntf[] t();\n";
	verifyClassFile(expectedOutput, "MyRecord.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug571454() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"    public static void main(String argv[]) {\n"+
					"       R rec = new R(3);\n"+
					"		if (rec.x() == 3) {\n" +
					"			// do nothing\n" +
					"		}\n" +
					"    }\n"+
					"}\n",
					"R.java",
					"record R(int x) {\n"+
					"       R {\n"+
					"               super();\n"+
					"       }\n"+
					"}",
				},
	        "----------\n"
	        + "1. ERROR in R.java (at line 3)\n"
	        + "	super();\n"
	        + "	^^^^^^^^\n"
	        + "The body of a compact constructor must not contain an explicit constructor call\n"
	        + "----------\n");
}
public void testBug570399_001() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			" public static void main(String[] args) {\n"+
			"    R r1 = new R( 2, 3); // Wrong error: The constructor MyRecord(int, int) is undefined\n"+
			"    R r2 = new R();      // works\n"+
			"    int total = r1.x()+r2.x()+r1.y()+r2.y();\n"+
			"    System.out.println(\"Hi\"+total);\n"+
			"  }\n"+
			"}",
			"R.java",
			"public record R(int x, int y) {\n"+
			"    R() {\n"+
			"        this(0, 0);\n"+
			"    }\n"+
			"}",
		},
	 "Hi5");
}
public void testBug570399_002() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"record R(int x) {\n"+
			"}\n" +
			"public class X {\n"+
			" public static void main(String[] args) {\n"+
			"    R r2 = new R(5);      // works\n"+
			"    int total = r2.x();\n"+
			"    System.out.println(\"Hi\"+total);\n"+
			"  }\n"+
			"}",
		},
	 "Hi5");
}
public void testBug571141_1() {
	runConformTest(new String[] { "X.java",
			"public class X {\n" +
			" public static void main(String[] args) {\n" +
			"   System.out.println(\"helo\");\n" +
			" }\n" +
			"}\n" +
			"record MyRecord(boolean equals){\n" +
			"    public boolean equals() {\n" +
			"        return equals;\n" +
			"    }\n" +
			"}" },
		"helo");
}
public void testBug571141_2() {
	runConformTest(new String[] { "X.java",
			"public class X {\n" +
			" public static void main(String[] args) {\n" +
			"   System.out.println(\"helo\");\n" +
			" }\n" +
			"}\n" +
			"record MyRecord(boolean equals){\n" +
			"    public boolean equals() {\n" +
			"        return equals;\n" +
			"    }\n" +
			"    public boolean equals(Object obj) {\n" +
			"      return equals;\n" +
			"    } \n" +
			"}" },
		"helo");
}
public void testBug571141_3() throws IOException, ClassFormatException {
	runConformTest(new String[] { "X.java",
			"public class X {\n" +
			" public static void main(String[] args) {\n" +
			"   System.out.println(\"helo\");\n" +
			" }\n" +
			"}\n" +
			"record MyRecord(boolean b){\n" +
			"    public boolean equals(Object other) {\n" +
			"        return true;\n" +
			"    }\n" +
			"}" },
		"helo");
	String unExpectedOutput =
			 "  public final boolean equals(java.lang.Object arg0);\n"
			 + "    0  aload_0 [this]\n"
			 + "    1  aload_1 [arg0]\n"
			 + "    2  invokedynamic 0 equals(MyRecord, java.lang.Object) : boolean [35]\n"
			 + "    7  ireturn\n"
			 + "";
	String rFile = getClassFileContents("MyRecord.class", ClassFileBytesDisassembler.SYSTEM);
	verifyOutputNegative(rFile, unExpectedOutput);
}
public void testBugLazyCanon_001() throws IOException, ClassFormatException {
	runConformTest(new String[] { "X.java",
			"record X(int xyz, int y2k) {\n"+
					" public X(int xyz, int y2k) {\n"+
					"     this.xyz = xyz;\n"+
					"     this.y2k = y2k;\n"+
					"   }\n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(new X(33,1).xyz());\n"+
					" }\n"+
					"}"
	},
		"33");
}
public void testBugLazyCanon_002() throws IOException, ClassFormatException {
	runConformTest(new String[] { "X.java",
			"record X(int xyz, int y2k) {\n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(new X(33,1).xyz());\n"+
					" }\n"+
					"}"
	},
		"33");
}
public void testBugLazyCanon_003() throws IOException, ClassFormatException {
	runConformTest(new String[] { "X.java",
			"class X {\n"+
					"  record Point (int  args) {\n"+
					"    Point (String s, int t) {\n"+
					"      this(t);\n"+
					"    }\n"+
					"  }\n"+
					"   public static void main(String[] args) {\n"+
					"    System.out.println(new X.Point(null, 33).args());\n"+
					"    \n"+
					"   }\n"+
					"}"
	},
	"33");
}
public void testBugLazyCanon_004() throws IOException, ClassFormatException {
	runConformTest(new String[] {
			"X.java",
			"record X<T> (T args) {\n"+
			" public static void main(String[] args) {\n"+
			"   System.out.println(new X<Integer>(100).args());\n"+
			"   \n"+
			" }\n"+
			"}"
	},
	"100");
}
public void testBugLazyCanon_005() throws IOException, ClassFormatException {
	runConformTest(new String[] {
			"X.java",
			"record X<T> (T args) {\n"+
			" X(String s, T t) {\n"+
			"   this(t);\n"+
			" }\n"+
			" public static void main(String[] args) {\n"+
			"   System.out.println(100);\n"+
			"   \n"+
			" }\n"+
			"}"
	},
	"100");
}
public void testBugLazyCanon_006() throws IOException, ClassFormatException {
	runConformTest(new String[] {
			"X.java",
			"record X<T> (T args) {\n"+
			" X(String s, T t) {\n"+
			"   this(t);\n"+
			" }\n"+
			" public static void main(String[] args) {\n"+
			"   System.out.println(new X<Integer>(100).args());\n"+
			"   \n"+
			" }\n"+
			"}"
	},
	"100");
}
// Disabled waiting for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3347
public void testBug571765_001() {
	if (this.complianceLevel < ClassFileConstants.JDK24)
		return;
	this.runNegativeTest(
			new String[] {
					"module-info.java",
					"public record R() {}\n",
				},
			(this.complianceLevel < ClassFileConstants.JDK25 ?
				"----------\n"
				+ "1. ERROR in module-info.java (at line 1)\n"
				+ "	public record R() {}\n"
				+ "	^\n"
				+ "The Java feature 'Compact Source Files and Instance Main Methods' is only available with source level 25 and above\n"
				: "") +
			"----------\n" +
			"2. ERROR in module-info.java (at line 1)\n" +
			"	public record R() {}\n" +
			"	^\n" +
			"Implicitly declared class must have a candidate main method\n" +
			"----------\n" +
			"3. ERROR in module-info.java (at line 1)\n" +
			"	public record R() {}\n" +
			"	       ^^^^^^\n" +
			"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
			"----------\n");
}
public void testBug571905_01() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.*;\n" +
			"record X( int @MyAnnot [] j) {\n" +
			" public static void main(String[] args) {\n" +
			"   System.out.println(\"helo\");\n" +
			" }\n" +
			"}\n" +
			"@Target({ElementType.TYPE_USE})\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface MyAnnot {}\n"
		},
	 "helo");
	String expectedOutput = // constructor
			"  \n" +
			"  // Method descriptor #49 ([I)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  X(int[] j);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [50]\n" +
			"     4  aload_0 [this]\n" +
			"     5  aload_1 [j]\n" +
			"     6  putfield X.j : int[] [31]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        j\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #8 @MyAnnot(\n" +
			"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
			"        method parameter index = 0\n" +
			"      )\n";
	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput = // accessor
			"  public int[] j();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield X.j : int[] [31]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 2]\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #8 @MyAnnot(\n" +
			"        target type = 0x14 METHOD_RETURN\n" +
			"      )\n" ;
	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug571905_02() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"import java.lang.annotation.*;\n" +
			"record X( int @MyAnnot ... j) {\n" +
			" public static void main(String[] args) {\n" +
			"   System.out.println(\"helo\");\n" +
			" }\n" +
			"}\n" +
			"@Target({ElementType.TYPE_USE})\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface MyAnnot {}\n"
		},
	 "helo");
	String expectedOutput = // constructor
			"  \n" +
			"  // Method descriptor #49 ([I)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  X(int... j);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [50]\n" +
			"     4  aload_0 [this]\n" +
			"     5  aload_1 [j]\n" +
			"     6  putfield X.j : int[] [31]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        j\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #8 @MyAnnot(\n" +
			"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
			"        method parameter index = 0\n" +
			"      )\n";
	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput = // accessor
			"  public int[] j();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield X.j : int[] [31]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 2]\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #8 @MyAnnot(\n" +
			"        target type = 0x14 METHOD_RETURN\n" +
			"      )\n" ;
	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug572204_001() {
	runNegativeTest(
			new String[] {
					"R.java",
					"record R (@SafeVarargs String... s) {}\n"
				},
				"----------\n" +
				"1. ERROR in R.java (at line 1)\n" +
				"	record R (@SafeVarargs String... s) {}\n" +
				"	                                 ^\n" +
				"@SafeVarargs annotation cannot be applied to record component without explicit accessor method s\n" +
				"----------\n");
}
public void testBug572204_002() {
	runConformTest(
			new String[] {
					"R.java",
					"record R (@SafeVarargs String... s) {\n" +
					" public static void main(String[] args) {\n" +
					"   System.out.println(\"helo\");\n" +
					" }\n" +
					" public String[] s() {\n" +
					"  return this.s;\n" +
					" }\n" +
					"}\n"
				},
				"helo");
}
public void testBug572204_003() {
	runNegativeTest(
			new String[] {
					"R.java",
					"record R (@SafeVarargs String... s) {\n" +
					" public static void main(String[] args) {\n" +
					"   System.out.println(\"helo\");\n" +
					" }\n" +
					" R (@SafeVarargs String... s) {\n" +
					"   this.s=s;\n" +
					" }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in R.java (at line 1)\n" +
				"	record R (@SafeVarargs String... s) {\n" +
				"	                                 ^\n" +
				"@SafeVarargs annotation cannot be applied to record component without explicit accessor method s\n" +
				"----------\n" +
				"2. ERROR in R.java (at line 5)\n" +
				"	R (@SafeVarargs String... s) {\n" +
				"	   ^^^^^^^^^^^^\n" +
				"The annotation @SafeVarargs is disallowed for this location\n" +
				"----------\n");
}
public void testBug572204_004() {
	runNegativeTest(
			new String[] {
					"R.java",
					"record R (@SafeVarargs String... s) {\n" +
					" public static void main(String[] args) {\n" +
					"   System.out.println(\"helo\");\n" +
					" }\n" +
					" R (@SafeVarargs String... s) {\n" +
					"   this.s=s;\n" +
					" }\n" +
					" public String[] s() {\n" +
					"  return this.s;\n" +
					" }\n" +
					"}\n"
				},
			"----------\n" +
			"1. ERROR in R.java (at line 5)\n" +
			"	R (@SafeVarargs String... s) {\n" +
			"	   ^^^^^^^^^^^^\n" +
			"The annotation @SafeVarargs is disallowed for this location\n" +
			"----------\n");
}
public void testBug572204_005() {
	runNegativeTest(
			new String[] {
					"R.java",
					"record R (@SafeVarargs String... s) {\n" +
					"@SafeVarargs" +
					" R (String... s) {\n" +
					"   this.s = s;\n" +
					" }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in R.java (at line 1)\n" +
				"	record R (@SafeVarargs String... s) {\n" +
				"	                                 ^\n" +
				"@SafeVarargs annotation cannot be applied to record component without explicit accessor method s\n" +
				"----------\n");
}
public void testBug572204_006() {
	runConformTest(
			new String[] {
					"R.java",
					"record R (@SafeVarargs String... s) {\n" +
					" public static void main(String[] args) {\n" +
					"   System.out.println(\"helo\");\n" +
					" }\n" +
					"@SafeVarargs" +
					" R (String... s) {\n" +
					"   this.s = s;\n" +
					" }\n" +
					" public String[] s() {\n" +
					"  return this.s;\n" +
					" }\n" +
					"}\n"
				},
			"helo");
}
public void testBug572204_007() throws Exception {
	runConformTest(
			new String[] {
					"R.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.PARAMETER) \n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface I {}\r\n" +
					"record R(@I String... s) {\n" +
					" public static void main(String[] args) {\n" +
					"   System.out.println(\"helo\");\n" +
					" }\n" +
					"}\n"
				},
				"helo");
	String expectedOutput = // constructor
			"  \n"	+
			"  // Method descriptor #8 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  R(java.lang.String... s);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [48]\n" +
			"     4  aload_0 [this]\n" +
			"     5  aload_1 [s]\n" +
			"     6  putfield R.s : java.lang.String[] [28]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        s\n" +
			"    RuntimeVisibleParameterAnnotations: \n" +
			"      Number of annotations for parameter 0: 1\n" +
			"        #47 @I(\n" +
			"        )\n";
	verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
	expectedOutput = // accessor
			"  \n" +
			"  // Method descriptor #27 ()[Ljava/lang/String;\n" +
	 		"  // Stack: 1, Locals: 1\n" +
			"  public java.lang.String[] s();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield R.s : java.lang.String[] [28]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n";
	verifyClassFile(expectedOutput, "R.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug572934_001() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.ENABLED);
	//This test should not report any error
	this.runConformTest(
		new String[] {
			"X.java",
			"public record X(int param) {\n" +
			"	public X(int param) {\n" +
			"		this.param = param;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X abc= new X(10);\n" +
			"		System.out.println(abc.param());\n" +
			"	}\n" +
			"}\n"
		},
		"10",
		options
	);
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.DISABLED);
}
public void testBug572934_002() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public record X(int param) {\n" +
			"	public X(int param) {\n" +
			"		this.param = param;\n" +
			"	}\n" +
			"	public void main(int param) {\n" +
			"		System.out.println(param);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	public void main(int param) {\n" +
		"	                     ^^^^^\n" +
		"The parameter param is hiding a field from type X\n" +
		"----------\n",
		null,
		true,
		options
	);
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.DISABLED);
}
public void testBug572934_003() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public record X(int param) {\n" +
			"	public X(int param) {\n" +
			"		this.param = param;\n" +
			"	}" +
			"	public void setParam(int param) {\n" +
			"		\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	}	public void setParam(int param) {\n" +
		"	 	                         ^^^^^\n" +
		"The parameter param is hiding a field from type X\n" +
		"----------\n",
		null,
		true,
		options
	);
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.DISABLED);
}
public void testBug573195_001() throws Exception {
	getPossibleComplianceLevels();
	runConformTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"    protected record R(int i) {\n"+
					"        public R(int i, int j) {\n"+
					"            this(i);\n"+
					"        }\n"+
					"    }\n"+
					"    public static void main(String[] args) {\n"+
					"   R r = new R(1, 2);\n"+
					"   System.out.println(r.i());\n"+
					" }\n"+
					"}"
				},
				"1");
	String expectedOutput = // constructor
			"  // Method descriptor #12 (I)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  protected X$R(int i);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [36]\n" +
			"     4  aload_0 [this]\n" +
			"     5  iload_1 [i]\n" +
			"     6  putfield X$R.i : int [20]\n" +
			"     9  return\n";
	verifyClassFile(expectedOutput, "X$R.class", ClassFileBytesDisassembler.SYSTEM);
}

public void testBug574284_001() throws Exception {
	getPossibleComplianceLevels();
	runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"\n" +
					"    public static void main(String[] args) {\n" +
					"        new X.Rec(false); // fails\n" +
					"        new X.Rec(false, new int[0]);\n" +
					"        System.out.println(0);\n" +
					"    }\n" +
					"\n" +
					"    record Rec(boolean isHidden, int... indexes) {\n" +
					"        Rec(int... indexes) {\n" +
					"            this(false, indexes);\n" +
					"        }\n" +
					"    }\n" +
					"}"
			},
		"0");
	String expectedOutput = // constructor
			"  // Method descriptor #14 (Z[I)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  X$Rec(boolean isHidden, int... indexes);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [41]\n" +
			"     4  aload_0 [this]\n" +
			"     5  iload_1 [isHidden]\n" +
			"     6  putfield X$Rec.isHidden : boolean [21]\n" +
			"     9  aload_0 [this]\n" +
			"    10  aload_2 [indexes]\n" +
			"    11  putfield X$Rec.indexes : int[] [24]\n" +
			"    14  return\n";
	verifyClassFile(expectedOutput, "X$Rec.class", ClassFileBytesDisassembler.SYSTEM);

}
public void testBug574284_002() {
	runConformTest(
			new String[] {
					"X.java",
					"public class X {\n" +
					"\n" +
					"    public static void main(String[] args) {\n" +
					"        new X.Rec(false); // fails\n" +
					"        new X.Rec(false, new int[0]);\n" +
					"        System.out.println(0);\n" +
					"    }\n" +
					"\n" +
					"    record Rec(boolean isHidden, int... indexes) {\n" +
					"    }\n" +
					"}"
			},
		"0");
}

public void testBug574282_001() {
	runConformTest(
			new String[] {
					"X.java",
					"record Rec(String name) {\n" +
					"\n" +
					"    Rec() {\n" +
					"        this(\"\");\n" +
					"    }\n" +
					"\n" +
					"    @Override\n" +
					"    public boolean equals(Object obj) {\n" +
					"        return false;\n" +
					"    }\n" +
					"}\n" +
					"public class X {\n"+
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n"
			},
		"0");
}
public void testBug576519_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X extends Point{\n"+
			"  public X(int x, int y){\n"+
			"     \n" +
			"  }\n"+
			"}\n"+
			"record Point(int x, int y){\n"+
		"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	class X extends Point{\n" +
		"	                ^^^^^\n" +
		"The record Point cannot be the superclass of X; a record is final and cannot be extended\n" +
		"----------\n");
}
public void testBug577251_001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n"+
			"  record Entry<T> (int value, Entry<T> entry) {\n"+
			"     Entry(int value, Entry entry) { // Entry is a raw type here\n" +
			"  }\n"+
			"}\n"+
		"}",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	Entry(int value, Entry entry) { // Entry is a raw type here\n" +
		"	                 ^^^^^\n" +
		"X.Entry is a raw type. References to generic type X.Entry<T> should be parameterized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	Entry(int value, Entry entry) { // Entry is a raw type here\n" +
		"	                 ^^^^^\n" +
		"Type or arity incompatibility in argument X.Entry of canonical constructor in record class\n" +
		"----------\n");
}

public void testBug576806_001() { // behavior amended for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3316
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
	this.runNegativeTest(
		false /* skipJavac */,
		new JavacTestOptions("Xlint:empty"),
		new String[] {
				"X.java",
				"public class X {\n"+
				"  public static void main(String[] args){\n"+
				"  }\n"+
				"}\n"+
				"record Empty(){\n"+
				"}\n"+
				"record DocumentedEmpty(){\n"+
				"  // intentionally empty\n"+
				"}\n"+
				"record Point(int x, int y){\n"+
				"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public static void main(String[] args){\n" +
		"  }\n" +
		"	                                      ^^^^^\n" +
		"Empty block should be documented\n" +
		"----------\n",
		null,
		false,
		options);
}

public void testIssue365_001() throws Exception {
	getPossibleComplianceLevels();
	runConformTest(
			new String[] {
					"A.java",
					"import java.util.Collections;\n" +
					"import java.util.List;\n" +
					"public record A(List<String> names) {\n" +
					"    public A(String name) {\n" +
					"        this(Collections.singletonList(name));\n" +
					"    }\n" +
					"    public static void main(String[] args) {\n" +
					"        System.out.println(0);\n" +
					"    }" +
					"}\n"
			},
		"0");
	String expectedOutput = // constructor
			"  // Method descriptor #20 (Ljava/util/List;)V\n" +
			"  // Signature: (Ljava/util/List<Ljava/lang/String;>;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public A(java.util.List names);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [64]\n" +
			"     4  aload_0 [this]\n" +
			"     5  aload_1 [names]\n" +
			"     6  putfield A.names : java.util.List [46]\n" +
			"     9  return\n";
	verifyClassFile(expectedOutput, "A.class", ClassFileBytesDisassembler.SYSTEM);

}

/**
 * Test that the following code doesn't result in generating byte code after the throw statement:
 * <pre>
 * record X(String s) {
 *    X {
 *        throw new RuntimeException();
 *    }
 * }
 * </pre>
 */
public void testRecordConstructorWithExceptionGh487() throws Exception {
	getPossibleComplianceLevels();
	runConformTest(
			// test directory preparation
			true /* should flush output directory */,
			new String[] { /* test files */
					"X.java",
					"""
					public record X(String s) {
					    public X {
					        throw new RuntimeException();
					    }
					    public static void main(String[] args) throws Exception {
					        new X("");
					    }
					}
					""",
			},
			// compiler results
			"" /* expected compiler log */,
			// runtime results
			"" /* expected output string */,
			"""
			java.lang.RuntimeException
				at X.<init>(X.java:3)
				at X.main(X.java:6)
			""" /* expected error string */,
			// javac options
			JavacTestOptions.forRelease("16"));
	String expectedOutput = // constructor
			"""
			  // Method descriptor #8 (Ljava/lang/String;)V
			  // Stack: 2, Locals: 2
			  public X(java.lang.String s);
			     0  aload_0 [this]
			     1  invokespecial java.lang.Record() [10]
			     4  new java.lang.RuntimeException [13]
			     7  dup
			     8  invokespecial java.lang.RuntimeException() [15]
			    11  athrow
			""";
	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1092
// Duplicate Annotation Error for Records
public void testGH1092() throws Exception {
	runConformTest(
			new String[] {
					"X.java",
					"import java.lang.annotation.*;\n" +
					"import java.lang.annotation.Target;\n" +
					"import java.util.List;\n" +
					"import java.lang.reflect.AnnotatedParameterizedType;\n" +
					"\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@Retention(RetentionPolicy.RUNTIME)\n" +
					"@interface Ann {\n" +
					"}\n" +
					"\n" +
					"record Record(\n" +
					"    @Ann\n" +
					"    List<@Ann String> list\n" +
					") {\n" +
					"}\n" +
					"\n" +
					"public class X {\n" +
					"\n" +
					"	static void assertDoesNotThrow(Runnable exe, String message) {\n" +
					"		exe.run();\n" +
					"	}\n" +
					"	\n" +
					"    public static void main(String [] args) throws Exception {\n" +
					"        AnnotatedParameterizedType listField = (AnnotatedParameterizedType) Record.class.getDeclaredMethod(\"list\").getAnnotatedReturnType();\n" +
					"        assertDoesNotThrow(listField::getAnnotatedActualTypeArguments, \"Should not throw duplicate annotation exception.\");\n" +
					"    }\n" +
					"}\n"
				},
		"");

	// verify annotations on field
	String expectedOutput =
			"  // Field descriptor #6 Ljava/util/List;\n" +
			"  // Signature: Ljava/util/List<Ljava/lang/String;>;\n" +
			"  private final java.util.List list;\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #10 @Ann(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"      #10 @Ann(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [TYPE_ARGUMENT(0)]\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);

	// verify annotations on constructor
	expectedOutput =
			"  // Method descriptor #34 (Ljava/util/List;)V\n" +
			"  // Signature: (Ljava/util/List<Ljava/lang/String;>;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  Record(java.util.List list);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [36]\n" +
			"     4  aload_0 [this]\n" +
			"     5  aload_1 [list]\n" +
			"     6  putfield Record.list : java.util.List [14]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        list\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #10 @Ann(\n" +
			"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
			"        method parameter index = 0\n" +
			"      )\n" +
			"      #10 @Ann(\n" +
			"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
			"        method parameter index = 0\n" +
			"        location = [TYPE_ARGUMENT(0)]\n" +
			"      )\n" +
			"\n" ;
	verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);

	// verify annotations on accessor
	expectedOutput =
			"  // Method descriptor #11 ()Ljava/util/List;\n" +
			"  // Signature: ()Ljava/util/List<Ljava/lang/String;>;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public java.util.List list();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Record.list : java.util.List [14]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 13]\n" +
			"    RuntimeVisibleTypeAnnotations: \n" +
			"      #10 @Ann(\n" +
			"        target type = 0x14 METHOD_RETURN\n" +
			"      )\n" +
			"      #10 @Ann(\n" +
			"        target type = 0x14 METHOD_RETURN\n" +
			"        location = [TYPE_ARGUMENT(0)]\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);

	// verify annotations on record component
	expectedOutput =
			"// Component descriptor #6 Ljava/util/List;\n" +
			"// Signature: Ljava/util/List<Ljava/lang/String;>;\n" +
			"java.util.List list;\n" +
			"  RuntimeVisibleTypeAnnotations: \n" +
			"    #10 @Ann(\n" +
			"      target type = 0x13 FIELD\n" +
			"    )\n" +
			"    #10 @Ann(\n" +
			"      target type = 0x13 FIELD\n" +
			"      location = [TYPE_ARGUMENT(0)]\n" +
			"    )\n";
	verifyClassFile(expectedOutput, "Record.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576719
// Useless warning in compact constructor of a record
public void testBug576719() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportParameterAssignment, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"Rational.java",
			"public record Rational(int num, int denom) {\n" +
			"    public Rational {\n" +
			"        int gcd = gcd(num, denom);\n" +
			"        num /= gcd;\n" +
			"        denom /= gcd;\n" +
			"    }\n" +
			"    \n" +
			"    private static int gcd(int a, int b) {\n" +
			"        a = 10;\n" +
			"        throw new UnsupportedOperationException();\n" +
			"    }\n" +
			"}\n",
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n"
		+ "1. ERROR in Rational.java (at line 9)\n"
		+ "	a = 10;\n"
		+ "	^\n"
		+ "The parameter a should not be assigned\n"
		+ "----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void testGH1258() {
	runConformTest(
		new String[] {
			"Main.java",
			"""
			public class Main {
				public static void main(String[] args) {
				MyRecord test = new MyRecord(0, 0);
				System.out.println(test.field1());
				}
			}

			@Deprecated(since = MyRecord.STATIC_VALUE)
			record MyRecord(int field1, int field2) {
				public static final String STATIC_VALUE = "test";
			}
			"""});
}
public void testIssue1218_001() {
	runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				" record R(T x);\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	record R(T x);\n" +
			"	         ^\n" +
			"Cannot make a static reference to the non-static type T\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	record R(T x);\n" +
			"	            ^\n" +
			"Syntax error, insert \"ClassBody\" to complete ClassBodyDeclarations\n" +
			"----------\n");
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testIssue1641_001() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
	record InterfaceInRecord() {
	    sealed interface I {
	        enum Empty implements I {
	            INSTANCE;
	        }
	        record Single(double value) implements I {
	        }
	    }
	}
		class X {
		void foo() {
			Zork();
		}
	}

		"""},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	Zork();\n" +
		"	^^^^\n" +
		"The method Zork() is undefined for the type X\n" +
		"----------\n",
		null,
		true,
		options
	);

}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testIssue1641_002() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
			record InterfaceInRecord() {
			    sealed interface I  {
			        final class C implements I {
			        }
			    }
			}
			class X {
				void foo() {
					Zork();
				}
			}

		"""},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	Zork();\n" +
		"	^^^^\n" +
		"The method Zork() is undefined for the type X\n" +
		"----------\n",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testIssue1641_003() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
		enum InterfaceInEnum {
        	INSTANCE;
			sealed interface I  {
		      	final class C implements I {}
			}
			final class D implements I {}
			final class E implements InterfaceInEnum.I {}
		}
		class X {
			void foo() {
				Zork();
			}
		}
		"""},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	Zork();\n" +
		"	^^^^\n" +
		"The method Zork() is undefined for the type X\n" +
		"----------\n",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "rawtypes", "unchecked" })
public void testIssue1641_004() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
		enum InterfaceInEnum {
        	INSTANCE;
			sealed interface I  {
		      	final class C implements I {}
			}
			final class D implements I {}
		}
		class X {
			void foo() {
				Zork();
			}
		}
		"""},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	Zork();\n" +
		"	^^^^\n" +
		"The method Zork() is undefined for the type X\n" +
		"----------\n",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testIssue1641_005() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
		interface I  {
			enum E {
				First {}
			};
		}
		class X {
			void foo() {
				Zork();
			}
		}
		"""},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	Zork();\n" +
		"	^^^^\n" +
		"The method Zork() is undefined for the type X\n" +
		"----------\n",
		null,
		true,
		options
	);
}
@SuppressWarnings({ "unchecked", "rawtypes" })
public void testIssue1641_006() {
	if (!isJRE17Plus)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
	this.runNegativeTest(
		new String[] {
		"X.java",
		"""
		interface I  {
			enum E {
				First {
					@SuppressWarnings("unused")
					enum F {
						FirstOne {
							interface J {
								enum G {
									FirstTwo {}
								}
							}
						}
					};
				}
			};
		}
		class X {
			void foo() {
				Zork();
			}
		}
		"""},
		"----------\n" +
		"1. ERROR in X.java (at line 19)\n" +
		"	Zork();\n" +
		"	^^^^\n" +
		"The method Zork() is undefined for the type X\n" +
		"----------\n",
		null,
		true,
		options
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1806
// Parameters of compact canonical constructor are not marked as mandated
public void testGH1806() {
	runConformTest(
			new String[] {
					"MyRecord.java",
					"""
					public record MyRecord(int a) {

						public static void main(String[] args) {
							var ctor = MyRecord.class.getConstructors()[0];
							System.out.println(ctor.getParameters()[0].isImplicit());
						}

						public MyRecord {

						}

					}
					"""
			},
		"true");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1939
// [records] Class MethodBinding has a NullPointerException
public void testGH1939() {
	runConformTest(
			new String[] {
					"X.java",
					"""
					public class X {

					    interface Foo {}

					    interface A {
					        <T extends Foo> Class<T> clazz() ;
					    }

					    record AA<T extends Foo>( Class<T> clazz ) implements A {}

					    public static void main(String [] args) {
					        System.out.println("OK!");
					    }
					}
					"""
			},
		"OK!");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3745
// [Records] ClassCastException when saving a file with a record syntax error
public void testIssue3745() {
	runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    public static void main(String[] args) {}
				    record R(int x,) {}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	record R(int x,) {}\n" +
			"	              ^\n" +
			"Syntax error on token \",\", SingleVariableDeclarator expected after this token\n" +
			"----------\n");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3745
// Records] ClassCastException when saving a file with a record syntax error
public void testIssue3745_full() {
	runNegativeTest(
			new String[] {
				"X.java",
				"""
				package test;

				public class Test {

				    private static final PathReplacement[] REPLACEMENTS = {
				            new PathReplacement("", ""),
				    };

				    public static void main(String[] args) {

				    }

				    private record PathReplacement(String absolutePathPrefix, String bazelPath) {}

				    private record BuildProperties(
				            int resourceDirsToSkip,
				    ) {}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	public class Test {\n" +
			"	             ^^^^\n" +
			"The public type Test must be defined in its own file\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 16)\n" +
			"	int resourceDirsToSkip,\n" +
			"	                      ^\n" +
			"Syntax error on token \",\", SingleVariableDeclarator expected after this token\n" +
			"----------\n");
}
public void testBug3504_1() {
	runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       public static void main(String[] args) {
					           record R(int x) {
					           		static {
					                	static int i = 0;
					          		}
					        	}
					        	R r =  new R(100);
					        	System.out.println(r.x());
					    	}
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	static int i = 0;\n" +
			"	           ^\n" +
			"Illegal modifier for the variable i; only final is permitted\n" +
			"----------\n");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/pull/3675
public void testPR3675() {
	runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					       record Y() {}
					       X {}
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	X {}\n" +
			"	^\n" +
			"A compact constructor is allowed only in record classes\n" +
			"----------\n");
}

public void testPR3675_2() {
	runNegativeTest(
			new String[] {
				"X.java",
				"""
					public record X() {
					       class Y {}
					       X {}
					}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	X {}\n" +
			"	^\n" +
			"Cannot reduce the visibility of a canonical constructor X from that of the record\n" +
			"----------\n");
}

public void testGH3891() {
	runNegativeTest(new String[] {
		"Test.java",
		"""
		public class Test {
			{
				super();
			}
		}
		"""
		},
		"""
		----------
		1. ERROR in Test.java (at line 3)
			super();
			^^^^^^^^
		Constructor call must be the first statement in a constructor
		----------
		""");
}
public void testGH3891_preview() {
	if (this.complianceLevel < ClassFileConstants.JDK25) return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	runner.testFiles = new String[] {
		"Test.java",
		"""
		public class Test {
			{
				super();
			}
		}
		"""
		};
	runner.expectedCompilerLog =
		"""
		----------
		1. ERROR in Test.java (at line 3)
			super();
			^^^^^^^^
		Constructor call must be the first statement in a constructor
		----------
		""";
	runner.runNegativeTest();
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3904
// Unused parameters warning reported for record components
public void testIssue3904() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	this.runNegativeTest(
	new String[] {
			"X.java",
			"class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Pair p = new Pair(\"4\", \"2\");\n" +
			"		System.out.println(p.fTag() + p.fContent());\n" +
			"	}\n" +
			"	public record Pair(String fTag, String fContent) {}\n" + // should NOT warn on implicit constructor
			"   public void foo(int unused) {\n" + // should warn on unused parameter of non-constructor
			"   }\n" +
			"   public record Person(String name, int age) {\n" +
			"       public Person(String name, int age) {\n" + // Should warn here
			"           this.name = null; this.age = 0;\n" +
			"       }\n" +
			"   }\n" +
			"   public record Point (int x, int y) {\n" +
			"       public Point {}\n" + // no warning here
			"   }\n"+
			"\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	public void foo(int unused) {\n" +
		"	                    ^^^^^^\n" +
		"The value of the parameter unused is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	public Person(String name, int age) {\n" +
		"	                     ^^^^\n" +
		"The value of the parameter name is not used\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	public Person(String name, int age) {\n" +
		"	                               ^^^\n" +
		"The value of the parameter age is not used\n" +
		"----------\n",
		null,
		true,
		options
	);
}
public void testAnnotationsOnConstructor() {
	runConformTest(
			new String[] {
					"X.java",
					"""
					import java.lang.annotation.*;
					import java.lang.reflect.*;

					@Retention(RetentionPolicy.RUNTIME)
					@Target(ElementType.PARAMETER)
					@interface ParameterAnnot {
					}

					public record X(@ParameterAnnot int x) {

						public static void main(String[] args) {
							try {
								Class<?> c = X.class;
								Constructor<?> constructor = c.getConstructor(int.class);
								Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
								Parameter[] parameters = constructor.getParameters();

								for (int i = 0; i < parameters.length; i++) {
									System.out.print("Parameter " + parameters[i].getName() + ": ");
									if (paramAnnotations[i] == null || paramAnnotations[i].length == 0) {
										System.out.println(" No Annotations!");
									} else {
										for (Annotation annotation : paramAnnotations[i]) {
											if (annotation instanceof ParameterAnnot) {
												System.out.println("Found Parameter annotation");
											}
										}
									}
								}

							} catch (NoSuchMethodException e) {
								e.printStackTrace();
							}
						}

					}
					"""
			},
		"Parameter x: Found Parameter annotation");
}
public void testAnnotationsOnConstructor_2() {
	runConformTest(
			new String[] {
					"X.java",
					"""
					import java.lang.annotation.*;
					import java.lang.reflect.*;

					@Retention(RetentionPolicy.RUNTIME)
					@Target(ElementType.PARAMETER)
					@interface ParameterAnnot {
					}

					public record X(@ParameterAnnot int x) {

					    public X {
					    }

						public static void main(String[] args) {
							try {
								Class<?> c = X.class;
								Constructor<?> constructor = c.getConstructor(int.class);
								Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
								Parameter[] parameters = constructor.getParameters();

								for (int i = 0; i < parameters.length; i++) {
									System.out.print("Parameter " + parameters[i].getName() + ": ");
									if (paramAnnotations[i] == null || paramAnnotations[i].length == 0) {
										System.out.println(" No Annotations!");
									} else {
										for (Annotation annotation : paramAnnotations[i]) {
											if (annotation instanceof ParameterAnnot) {
												System.out.println("Found Parameter annotation");
											}
										}
									}
								}

							} catch (NoSuchMethodException e) {
								e.printStackTrace();
							}
						}

					}
					"""
			},
		"Parameter x: Found Parameter annotation");
}
public void testAnnotationsOnConstructor_3() {
	runConformTest(
			new String[] {
					"X.java",
					"""
					import java.lang.annotation.*;
					import java.lang.reflect.*;

					@Retention(RetentionPolicy.RUNTIME)
					@Target(ElementType.PARAMETER)
					@interface ParameterAnnot {
					}

					public record X(@ParameterAnnot int x) {

					    public X (int x) {
					    	this.x = x;
					    }

						public static void main(String[] args) {
							try {
								Class<?> c = X.class;
								Constructor<?> constructor = c.getConstructor(int.class);
								Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
								Parameter[] parameters = constructor.getParameters();

								for (int i = 0; i < parameters.length; i++) {
									System.out.print("Parameter " + parameters[i].getName() + ": ");
									if (paramAnnotations[i] == null || paramAnnotations[i].length == 0) {
										System.out.println(" No Annotations!");
									} else {
										for (Annotation annotation : paramAnnotations[i]) {
											if (annotation instanceof ParameterAnnot) {
												System.out.println("Found Parameter annotation");
											}
										}
									}
								}

							} catch (NoSuchMethodException e) {
								e.printStackTrace();
							}
						}

					}
					"""
			},
		"Parameter x:  No Annotations!");
}
public void testAnnotationsOnConstructor_4() {
	runConformTest(
			new String[] {
					"X.java",
					"""
					import java.lang.annotation.*;
					import java.lang.reflect.*;

					@Retention(RetentionPolicy.RUNTIME)
					@Target(ElementType.PARAMETER)
					@interface ParameterAnnot {
					}

					public record X(@ParameterAnnot int x) {

					    public X (@ParameterAnnot int x) {
					    	this.x = x;
					    }

						public static void main(String[] args) {
							try {
								Class<?> c = X.class;
								Constructor<?> constructor = c.getConstructor(int.class);
								Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
								Parameter[] parameters = constructor.getParameters();

								for (int i = 0; i < parameters.length; i++) {
									System.out.print("Parameter " + parameters[i].getName() + ": ");
									if (paramAnnotations[i] == null || paramAnnotations[i].length == 0) {
										System.out.println(" No Annotations!");
									} else {
										for (Annotation annotation : paramAnnotations[i]) {
											if (annotation instanceof ParameterAnnot) {
												System.out.println("Found Parameter annotation");
											}
										}
									}
								}

							} catch (NoSuchMethodException e) {
								e.printStackTrace();
							}
						}

					}
					"""
			},
		"Parameter x: Found Parameter annotation");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3664
// [Records] ECJ diagnostics are totally off-key when a records declares multiple compact constructors
public void testIssue3664() {
	this.runNegativeTest(
	new String[] {
			"X.java",
			"""
			record R(int x) {

				R {

				}

				R {

				}

			}
			""",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	R {\n" +
		"	^\n" +
		"Duplicate method R(int) in type R\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	R {\n" +
		"	^\n" +
		"Duplicate method R(int) in type R\n" +
		"----------\n");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3662
// [Records] ECJ issues errors about methods generated by it
public void testIssue3662() {
	this.runNegativeTest(
	new String[] {
			"Y.java",
			"""
			public protected  record Y() {

			}
			""",
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 1)\n" +
		"	public protected  record Y() {\n" +
		"	                         ^\n" +
		"Illegal modifier for the record Y; only public, final and strictfp are permitted\n" +
		"----------\n");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3927
// I-Build failure with PR https://github.com/eclipse-jdt/eclipse.jdt.core/pull/3896 integrated in
public void testIssue3927() {
	runConformTest(
			new String[] {
					"Test.java",
					"""
					import java.util.Arrays;
					import java.util.List;

					import snippet.*;

					public class Test {
						List<ElementAtZoom<ImageData>> loadFromByteStream(int fileZoom, int targetZoom) {
							return Arrays.stream(loadFromByteStream()).map(d -> new ElementAtZoom<>(d, fileZoom)).toList();
						}
						ImageData[] loadFromByteStream() {
							return null;
						}
						public static void main(String [] args) {
							System.out.println("Ok!");
						}
					}
					""",
					"ElementAtZoom.java",
					"""
					package snippet;

					public record ElementAtZoom<T>(T element, int zoom) {
						public ElementAtZoom {
						}
					}
					""",
					"ImageData.java",
					"""
					package snippet;
					public interface ImageData {
					}
					"""
			},
		"Ok!");
}
public void testIssue3927_2() {
	runConformTest(
			new String[] {
					"Test.java",
					"""
					import java.util.Arrays;
					import java.util.List;

					import snippet.*;

					public class Test {
						List<ElementAtZoom<ImageData>> loadFromByteStream(int fileZoom, int targetZoom) {
							return Arrays.stream(loadFromByteStream()).map(d -> new ElementAtZoom<>(d, fileZoom)).toList();
						}
						ImageData[] loadFromByteStream() {
							return null;
						}
						public static void main(String [] args) {
							System.out.println("Ok!");
						}
					}
					""",
					"ElementAtZoom.java",
					"""
					package snippet;

					public record ElementAtZoom<T>(T element, int zoom) {
					}
					""",
					"ImageData.java",
					"""
					package snippet;
					public interface ImageData {
					}
					"""
			},
		"Ok!");
}
public void testIssue3927_3() {
	runNegativeTest(
			new String[] {
					"Test.java",
					"""
					import java.util.Arrays;
					import java.util.List;

					import snippet.*;

					public class Test {
						List<ElementAtZoom<ImageData>> loadFromByteStream(int fileZoom, int targetZoom) {
							return Arrays.stream(loadFromByteStream()).map(d -> new ElementAtZoom<>(d, "wrong-argument-type")).toList();
						}
						ImageData[] loadFromByteStream() {
							return null;
						}
						public static void main(String [] args) {
							System.out.println("Ok!");
						}
					}
					""",
					"ElementAtZoom.java",
					"""
					package snippet;

					public record ElementAtZoom<T>(T element, int zoom) {
					}
					""",
					"ImageData.java",
					"""
					package snippet;
					public interface ImageData {
					}
					"""
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 8)\n" +
			"	return Arrays.stream(loadFromByteStream()).map(d -> new ElementAtZoom<>(d, \"wrong-argument-type\")).toList();\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from List<Object> to List<ElementAtZoom<ImageData>>\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 8)\n" +
			"	return Arrays.stream(loadFromByteStream()).map(d -> new ElementAtZoom<>(d, \"wrong-argument-type\")).toList();\n" +
			"	                                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Cannot infer type arguments for ElementAtZoom<>\n" +
			"----------\n");
}
public void testSafeVarargs() {
	runNegativeTest(
			new String[] {
					"X.java",
					"""
					public record X (@SafeVarargs int ... x) {
					}
					"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\r\n" +
			"	public record X (@SafeVarargs int ... x) {\r\n" +
			"	                                      ^\n" +
			"@SafeVarargs annotation cannot be applied to record component without explicit accessor method x\n" +
			"----------\n");
	runConformTest(
			new String[] {
					"X.java",
					"""
					public record X (@SafeVarargs int ... x) {
					    public int [] x() {
					    	return this.x;
					    }
					    public static void main(String [] args) {
					    	System.out.println("Ok!");
				    	}
					}
					"""
			},
			"Ok!");

}
public void testUnderscoreName() {
	if (this.complianceLevel < ClassFileConstants.JDK21)
		return;

	runNegativeTest(
			new String[] {
					"X.java",
					"""
					public record X (int _) {
					}
					"""
			},
			this.complianceLevel < ClassFileConstants.JDK22 ?
					"----------\n" +
					"1. ERROR in X.java (at line 1)\n" +
					"	public record X (int _) {\n" +
					"	                     ^\n" +
					"'_' is a keyword from source level 9 onwards, cannot be used as identifier\n" +
					"----------\n" :

						"----------\n" +
						"1. ERROR in X.java (at line 1)\n" +
						"	public record X (int _) {\n" +
						"	                     ^\n" +
						"As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters\n" +
						"----------\n");
}
public void testCompactConstuctorTypeAnnotations() {
	runConformTest(
			new String[] {
					"X.java",
					"""
					import java.lang.annotation.*;
					import java.lang.reflect.*;

					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface MyTypeAnno {
					    String value();
					}

					public record X(@MyTypeAnno("constructor param") int x) {

					    // Constructor with a type annotation on its parameter
					    public X {
					        // no-op
					    }

					    public static void main(String[] args) throws Exception {
					        // Get the Constructor object for X(int)
					        Constructor<X> constructor = X.class.getConstructor(int.class);

					        // Get annotated types of the parameters
					        AnnotatedType[] annotatedParams = constructor.getAnnotatedParameterTypes();

					        // Print each annotation on each parameter
					        for (int i = 0; i < annotatedParams.length; i++) {
					            System.out.println("Constructor parameter " + i + " annotations:");
					            for (Annotation annotation : annotatedParams[i].getAnnotations()) {
					                System.out.println("  " + annotation);
					            }
					        }
					    }
					}
					"""
			},
			"Constructor parameter 0 annotations:\n" +
					"  @MyTypeAnno(\"constructor param\")"
);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3951
// [Records] Generic signature is not preserved for compact constructors by PR #3928
public void testIssue3951() throws Exception {
	runConformTest(
		new String[] {
			"X.java",
			"""
			import java.lang.annotation.Annotation;
			import java.util.List;

			public record X (List<Class<? extends Annotation>> classes) {
				public X {

				}

				public static void main(String [] args) {
					System.out.println("Ok!");
				}
			}
			"""
		},
	 "Ok!");
	String expectedOutput =
			"  // Method descriptor #10 (Ljava/util/List;)V\n" +
			"  // Signature: (Ljava/util/List<Ljava/lang/Class<+Ljava/lang/annotation/Annotation;>;>;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public X(java.util.List classes);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [13]\n" +
			"     4  aload_0 [this]\n" +
			"     5  aload_1 [classes]\n" +
			"     6  putfield X.classes : java.util.List [16]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"        [pc: 4, line: 7]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 10] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 10] local: classes index: 1 type: java.util.List\n" +
			"      Local variable type table:\n" +
			"        [pc: 0, pc: 10] local: classes index: 1 type: java.util.List<java.lang.Class<? extends java.lang.annotation.Annotation>>\n" +
			"      Method Parameters:\n" +
			"        mandated classes\n";
	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3957
// [Records] Missing @Override annotation on component accessors not complained about by ECJ
public void testIssue3957() {
	Map<String, String> customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);

	this.runNegativeTest(
			true,
    		new String[] {
					"X.java",
					"""
					public record X(int x) {
					    public int x() {
					        return this.x;
				        }
					}
					""",
	            },
	null, customOptions,
	"----------\n" +
	"1. ERROR in X.java (at line 2)\n" +
	"	public int x() {\n" +
	"	           ^^^\n" +
	"The component accessor method x() of record class X should be tagged with @Override\n" +
	"----------\n",
	JavacTestOptions.SKIP);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3663
// [Records] ECJ compiles arity mismatched canonical constructor
public void testIssue3663() {
	this.runNegativeTest(
 		new String[] {
					"X.java",
					"""
					record R(int ... x) {

						R(int [] x) {
							this.x = x;
						}

					}
					""",
	            },

 		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	R(int [] x) {\n" +
		"	  ^^^^^^\n" +
		"Type or arity incompatibility in argument int[] of canonical constructor in record class\n" +
		"----------\n");

	this.runNegativeTest(
	 		new String[] {
						"X.java",
						"""
						record R(int [] x) {

							R(int ... x) {
								this.x = x;
							}

						}
						""",
		            },

	 		"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	R(int ... x) {\n" +
			"	  ^^^^^^^\n" +
			"Type or arity incompatibility in argument int[] of canonical constructor in record class\n" +
			"----------\n");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4015
// [Records] Incorrect error "Cannot instantiate local class 'ARecord' in a static context"
public void testIssue4015() {
	this.runConformTest(
		new String[] {
					"X.java",
					"""
					import java.util.function.Supplier;

					public class X {

						public String method() {

							record ARecord() {
								public static ARecord of() {
									return new ARecord();
								}
							}

							ARecord ar = ARecord.of();
							return ar.toString();
						}

						public static Supplier<Object> test() {
							class A {}
							return () -> new A();
						}

					    public static void main(String [] args) {
					        System.out.println(new X().method());
					    }
					}
					""",
	            },

		"ARecord[]");
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4015
// [Records] Incorrect error "Cannot instantiate local class 'ARecord' in a static context"
public void testIssue4015_2() {
	// test again with static enclosing method
	this.runConformTest(
			new String[] {
						"X.java",
						"""
						import java.util.function.Supplier;

						public class X {

							public static String staticMethod() {

								record ARecord() {
									public static ARecord of() {
										return new ARecord();
									}
								}

								ARecord ar = ARecord.of();
								return ar.toString();
							}

							public static Supplier<Object> test() {
								class A {}
								return () -> new A();
							}

						    public static void main(String [] args) {
						        System.out.println(staticMethod());
						    }
						}
						""",
		            },

			"ARecord[]");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4015
// [Records] Incorrect error "Cannot instantiate local class 'ARecord' in a static context"
public void testIssue4015_3() {
	this.runConformTest(
			new String[] {
						"X.java",
						"""
						import java.util.function.Supplier;

						public class X {

							interface I {}

							public String method() {

								record ARecord() implements I {
									public static ARecord of() {
										Supplier<I> si = ARecord::new;
										return (ARecord) si.get();
									}
								}
								return ARecord.of().toString();
							}

							public static void main(String [] args) {
						        System.out.println(new X().method());
						    }
						}
						""",
		            },

			"ARecord[]");
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4015
// [Records] Incorrect error "Cannot instantiate local class 'ARecord' in a static context"
public void testIssue4015_4() {
	// test again with static enclosing method
	this.runConformTest(
		new String[] {
					"X.java",
					"""
					import java.util.function.Supplier;

					public class X {

						interface I {}

						public static String staticMethod() {

							record ARecord() implements I {
								public static ARecord of() {
									Supplier<I> si = ARecord::new;
									return (ARecord) si.get();
								}
							}
							return ARecord.of().toString();
						}

						public static void main(String [] args) {
					        System.out.println(staticMethod());
					    }
					}
					""",
	            },

		"ARecord[]");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4025
// [Records] Record component by name equals with an explicit accessor leads to ClassFormatError
public void testIssue4025() {
	this.runConformTest(
		new String[] {
					"X.java",
					"""
					public record X(boolean equals)  {
					    public boolean equals() {
					        return equals;
					    }

					    public static void main(String argv[]) {
					        System.out.println("Ok!");
					    }
					}
					""",
	            },

		"Ok!");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4094
// Error on Eclipse 4.36 when compiling Record with field usage
public void testIssue4094() {
	this.runConformTest(
		new String[] {
					"X.java",
					"""
					public class X {
					    public static void main(String [] args) {
					        System.out.println(ClassB.B);
					    }
					}
					""",
					"ClassB.java",
					"""
					import java.util.List;
					import java.util.function.Predicate;


					public class ClassB {

					  private final Predicate<RecordA> predicate;

					  public static final ClassB B = new ClassB(recordA -> recordA.test.isEmpty());

					  public ClassB(Predicate<RecordA> predicate) {
					    this.predicate = predicate;
					  }

					  public String toString() {
					  	  return "ClassB instance";
					  }

					  record RecordA(List<Object> test, Integer i) {
					  }
					}
					"""
	            },

		"ClassB instance");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4106
// @Override-annotation on records not correctly handled by Eclipse 4.36
public void testIssue4106() {
	Map<String, String> customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);

	this.runNegativeTest(
			true,
 		new String[] {
					"X.java",
					"""
					public record X(String withoutOverride, String withOverride) {

					  public String withoutOverride() {
					    return withoutOverride;
					  }

					  @Override
					  public String withOverride() {
					    return withOverride;
					  }
					}
					""",
	            },
	null, customOptions,
	"----------\n" +
	"1. ERROR in X.java (at line 3)\n" +
	"	public String withoutOverride() {\n" +
	"	              ^^^^^^^^^^^^^^^^^\n" +
	"The component accessor method withoutOverride() of record class X should be tagged with @Override\n" +
	"----------\n",
	JavacTestOptions.SKIP);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4118
// Record with compact ctor - Internal compiler error: java.lang.RuntimeException: Internal Error compiling
public void testIssue4118() {
	this.runConformTest(
		new String[] {
					"RecordCompactWithReader.java",
					"""
					import java.io.Reader;
					import java.time.Instant;
					import java.util.Objects;

					public record RecordCompactWithReader(Instant modified, Reader reader) {
					    public RecordCompactWithReader {
					        Objects.requireNonNull(modified);
					        Objects.requireNonNull(reader);
					    }
					    public static void main(String [] args) {
					    	System.out.println("OK!");
					    }
					}
					""",
	            },
		"OK!"
		);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4070
// Resource closure analysis triggers NPE with compact constructors
public void testIssue4070() {
	this.runConformTest(
		new String[] {
					"Config.java",
					"""
					import java.net.URI;
					import java.net.http.HttpClient;
					import java.util.Objects;

					public record Config(HttpClient httpClient, URI base, String defaultContentType) {

						  @SuppressWarnings("resource")
						  public Config {
						    Objects.requireNonNull(httpClient, "httpClient");
						  }

						  public static void main(String [] args) {
					    	System.out.println("OK!");
					    }
					}
					""",
	            },
		"OK!"
		);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4146
// Unable to build Record
public void testIssue4146() {
	this.runNegativeTest(
		new String[] {
					"Segment.java",
					"""
					package repro;

					import com.fasterxml.jackson.annotation.JsonInclude;
					import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

					public record Segment(@JacksonXmlProperty(isAttribute = true) String id,
					                      String source,
					                      @JsonInclude(JsonInclude.Include.NON_NULL) String target) {

					}
					""",
	            },
		"----------\n" +
		"1. ERROR in Segment.java (at line 3)\r\n" +
		"	import com.fasterxml.jackson.annotation.JsonInclude;\r\n" +
		"	       ^^^^^^^^^^^^^\n" +
		"The import com.fasterxml cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in Segment.java (at line 4)\r\n" +
		"	import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;\r\n" +
		"	       ^^^^^^^^^^^^^\n" +
		"The import com.fasterxml cannot be resolved\n" +
		"----------\n" +
		"3. ERROR in Segment.java (at line 6)\r\n" +
		"	public record Segment(@JacksonXmlProperty(isAttribute = true) String id,\r\n" +
		"	                       ^^^^^^^^^^^^^^^^^^\n" +
		"JacksonXmlProperty cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in Segment.java (at line 8)\r\n" +
		"	@JsonInclude(JsonInclude.Include.NON_NULL) String target) {\r\n" +
		"	 ^^^^^^^^^^^\n" +
		"JsonInclude cannot be resolved to a type\n" +
		"----------\n" +
		"5. ERROR in Segment.java (at line 8)\r\n" +
		"	@JsonInclude(JsonInclude.Include.NON_NULL) String target) {\r\n" +
		"	             ^^^^^^^^^^^\n" +
		"JsonInclude cannot be resolved to a variable\n" +
		"----------\n");
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4146
// Unable to build Record
public void testIssue4146_2() {
	this.runNegativeTest(
		new String[] {
					"Segment.java",
					"""
					package repro;

					import com.fasterxml.jackson.annotation.JsonInclude;
					import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

					public record Segment(@JacksonXmlProperty(isAttribute = true) String id,
					                      @JsonInclude(JsonInclude.Include.NON_NULL) String target,
					                      String source) {

					}
					""",
	            },
		"----------\n" +
		"1. ERROR in Segment.java (at line 3)\n" +
		"	import com.fasterxml.jackson.annotation.JsonInclude;\n" +
		"	       ^^^^^^^^^^^^^\n" +
		"The import com.fasterxml cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in Segment.java (at line 4)\n" +
		"	import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;\n" +
		"	       ^^^^^^^^^^^^^\n" +
		"The import com.fasterxml cannot be resolved\n" +
		"----------\n" +
		"3. ERROR in Segment.java (at line 6)\n" +
		"	public record Segment(@JacksonXmlProperty(isAttribute = true) String id,\n" +
		"	                       ^^^^^^^^^^^^^^^^^^\n" +
		"JacksonXmlProperty cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in Segment.java (at line 7)\n" +
		"	@JsonInclude(JsonInclude.Include.NON_NULL) String target,\n" +
		"	 ^^^^^^^^^^^\n" +
		"JsonInclude cannot be resolved to a type\n" +
		"----------\n" +
		"5. ERROR in Segment.java (at line 7)\n" +
		"	@JsonInclude(JsonInclude.Include.NON_NULL) String target,\n" +
		"	             ^^^^^^^^^^^\n" +
		"JsonInclude cannot be resolved to a variable\n" +
		"----------\n");
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4146
// Unable to build Record
public void testIssue4146_3() throws Exception {
	this.runConformTest(
		new String[] {
					"Segment.java",
					"""
					import jackson.stuff.JacksonXmlProperty;
					import jackson.stuff.JsonInclude;

					public record Segment(@JacksonXmlProperty(isAttribute = true) String id,
					                      String source,
					                      @JsonInclude(JsonInclude.Include.NON_NULL) String target) {

						public static void main(String [] args) {
							System.out.println("OK!");
						}
					}
					""",
					"jackson/stuff/JacksonXmlProperty.java",
					"""
					package jackson.stuff;
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					import java.lang.annotation.Target;

					@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
					@Retention(RetentionPolicy.RUNTIME)
					public @interface JacksonXmlProperty {
					    boolean isAttribute() default false;
					}
					""",
					"jackson/stuff/JsonInclude.java",
					"""
					package jackson.stuff;

					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;

					@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
					@JacksonAnnotation
					public @interface JsonInclude {
					    public enum Include {
					    	ALWAYS,
					        NON_NULL;
					    }
					    public Include value() default Include.ALWAYS;
					}
					""",
					"jackson/stuff/JacksonAnnotation.java",
					"""
					package jackson.stuff;

					import java.lang.annotation.ElementType;
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					import java.lang.annotation.Target;

					@Target({ElementType.ANNOTATION_TYPE})
					@Retention(RetentionPolicy.RUNTIME)
					public @interface JacksonAnnotation {

					}
					"""
	            },
				"OK!");

	String expectedOutput =
					"  // Field descriptor #6 Ljava/lang/String;\n" +
					"  private final java.lang.String id;\n" +
					"    RuntimeVisibleAnnotations: \n" +
					"      #8 @jackson.stuff.JacksonXmlProperty(\n" +
					"        #9 isAttribute=true (constant type)\n" +
					"      )\n" +
					"  \n" +
					"  // Field descriptor #6 Ljava/lang/String;\n" +
					"  private final java.lang.String source;\n" +
					"  \n" +
					"  // Field descriptor #6 Ljava/lang/String;\n" +
					"  private final java.lang.String target;\n" +
					"    RuntimeInvisibleAnnotations: \n" +
					"      #14 @jackson.stuff.JsonInclude(\n" +
					"        #15 value=jackson.stuff.JsonInclude.Include.NON_NULL(enum type #16.#17)\n" +
					"      )\n";
	verifyClassFile(expectedOutput, "Segment.class", ClassFileBytesDisassembler.SYSTEM);

	expectedOutput =
			"  // Method descriptor #39 ()Ljava/lang/String;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public java.lang.String id();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Segment.id : java.lang.String [40]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"    RuntimeVisibleAnnotations: \n" +
			"      #8 @jackson.stuff.JacksonXmlProperty(\n" +
			"        #9 isAttribute=true (constant type)\n" +
			"      )\n" +
			"  \n" +
			"  // Method descriptor #39 ()Ljava/lang/String;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public java.lang.String source();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Segment.source : java.lang.String [42]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"  \n" +
			"  // Method descriptor #39 ()Ljava/lang/String;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public java.lang.String target();\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield Segment.target : java.lang.String [44]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"    RuntimeInvisibleAnnotations: \n" +
			"      #14 @jackson.stuff.JsonInclude(\n" +
			"        #15 value=jackson.stuff.JsonInclude.Include.NON_NULL(enum type #16.#17)\n" +
			"      )\n" +
			"  \n";
	verifyClassFile(expectedOutput, "Segment.class", ClassFileBytesDisassembler.SYSTEM);

	expectedOutput =
			"  // Method descriptor #61 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  public Segment(java.lang.String id, java.lang.String source, java.lang.String target);\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Record() [64]\n" +
			"     4  aload_0 [this]\n" +
			"     5  aload_1 [id]\n" +
			"     6  putfield Segment.id : java.lang.String [40]\n" +
			"     9  aload_0 [this]\n" +
			"    10  aload_2 [source]\n" +
			"    11  putfield Segment.source : java.lang.String [42]\n" +
			"    14  aload_0 [this]\n" +
			"    15  aload_3 [target]\n" +
			"    16  putfield Segment.target : java.lang.String [44]\n" +
			"    19  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        id\n" +
			"        source\n" +
			"        target\n" +
			"    RuntimeVisibleParameterAnnotations: \n" +
			"      Number of annotations for parameter 0: 1\n" +
			"        #8 @jackson.stuff.JacksonXmlProperty(\n" +
			"          #9 isAttribute=true (constant type)\n" +
			"        )\n" +
			"      Number of annotations for parameter 1: 0\n" +
			"      Number of annotations for parameter 2: 0\n" +
			"    RuntimeInvisibleParameterAnnotations: \n" +
			"      Number of annotations for parameter 0: 0\n" +
			"      Number of annotations for parameter 1: 0\n" +
			"      Number of annotations for parameter 2: 1\n" +
			"        #14 @jackson.stuff.JsonInclude(\n" +
			"          #15 value=jackson.stuff.JsonInclude.Include.NON_NULL(enum type #16.#17)\n" +
			"        )\n" +
			"\n";
	verifyClassFile(expectedOutput, "Segment.class", ClassFileBytesDisassembler.SYSTEM);

}
public void testIssue4290() throws Exception {
	this.runConformTest(
		new String[] {
					"X.java",
					"""
					public class X {
					    public Object a() {
					        return new Object() {
					            static record A(Object  a, Object b) {}
					        };
					    }
					    public static void main(String[] args) {
							System.out.println("OK");
						}
					}
					""",
	            },
				"OK");

}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4412
// Internal Compile error problem since 2025-09
public void testIssue4412() throws Exception {
	this.runConformTest(
		new String[] {
					"R.java",
					"""
					import java.util.Collection;
					import java.util.stream.Collectors;

					@interface A {

					    Id use();

					    public enum Id {
					        CLASS;
					    }
					}

					public record R<V>(String name, @A(use = A.Id.CLASS) V value) {

					    @Override
					    public String toString() {
					        return String.format("[%s] %s",
					                             name,
					                             (value instanceof Collection<?> coll
					                                     ? coll.stream().map(Object::toString).collect(Collectors.joining(", ", "[ ", " ]"))
					                                     : value.toString()));
					    }

					    public static void main(String [] args) {
					    	System.out.println("OK!");
					    }
					}
					""",
	            },
				"OK!");

}
public void testDeprecation_type() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"X.java",
		"""
		@Deprecated record R(int i, boolean f) {}
		public class X {
			R test() {
				return new R(1,false);
			}
		}
		"""
		};
	runner.expectedCompilerLog =
		"""
		----------
		1. WARNING in X.java (at line 3)
			R test() {
			^
		The type R is deprecated
		----------
		2. WARNING in X.java (at line 4)
			return new R(1,false);
			           ^
		The type R is deprecated
		----------
		""";
	runner.runWarningTest();
}
public void testDeprecation_altCtor() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"X.java",
		"""
		record R(int i, boolean f) {
			@Deprecated R(int i) {
				this(i, i>0);
			}
			R {
				i = Math.abs(i);
			}
		}
		public class X {
			R test(int in) {
				return switch(in) {
					case 0 -> new R(0);
					case 1 -> new R(1, false);
					default -> null;
				};
			}
		}
		"""
		};
	runner.expectedCompilerLog =
		"""
		----------
		1. WARNING in X.java (at line 12)
			case 0 -> new R(0);
			              ^
		The constructor R(int) is deprecated
		----------
		""";
	runner.runWarningTest();
}
public void testDeprecation_compactCtor() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"X.java",
		"""
		record R(int i, boolean f) {
			R(int i) {
				this(i, i>0);
			}
			@Deprecated R {
				i = Math.abs(i);
			}
		}
		public class X {
			R test(int in) {
				return switch(in) {
					case 0 -> new R(0);
					case 1 -> new R(1, false);
					default -> null;
				};
			}
		}
		"""
		};
	runner.expectedCompilerLog =
		"""
		----------
		1. WARNING in X.java (at line 13)
			case 1 -> new R(1, false);
			              ^
		The constructor R(int, boolean) is deprecated
		----------
		""";
	runner.runWarningTest();
}
public void testDeprecation_accessor() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
		"X.java",
		"""
		record R(int i, boolean f) {
			@Deprecated public int i() {
				return this.i;
			}
		}
		public class X {
			int test1() {
				return new R(1,false).i();
			}
			boolean test2() {
				return new R(1,false).f();
			}
		}
		"""
		};
	runner.expectedCompilerLog =
		"""
		----------
		1. WARNING in X.java (at line 8)
			return new R(1,false).i();
			                      ^
		The method i() from the type R is deprecated
		----------
		""";
	runner.runWarningTest();
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4551
// ECJ fails on Record used within Enum in Annotation
public void testIssue4551() throws Exception {
	this.runNegativeTest(
		new String[] {
					"X.java",
					"""
					public record X(
					  @ExampleAnnotation(value = { ExampleEnum.VALUE })
					  String string) {

					  @Target(ElementType.FIELD)
					  public @interface ExampleAnnotation {
					  	ExampleEnum[] value();
					  }
					}

					enum ExampleEnum {
					  VALUE(new SecondExampleRecord());

					  private ExampleEnum(SecondExampleRecord exampleRecord) { }
					}

					record SecondExampleRecord() {
					}
					""",
	            },
				"----------\n" +
				"1. ERROR in X.java (at line 5)\r\n" +
				"	@Target(ElementType.FIELD)\r\n" +
				"	 ^^^^^^\n" +
				"Target cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\r\n" +
				"	@Target(ElementType.FIELD)\r\n" +
				"	        ^^^^^^^^^^^\n" +
				"ElementType cannot be resolved to a variable\n" +
				"----------\n");
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4551
// ECJ fails on Record used within Enum in Annotation
public void testIssue4551_2() throws Exception {
	this.runConformTest(
		new String[] {
					"mypackage/Test.java",
					"""
					package mypackage;


					import java.lang.annotation.Target;
					import java.lang.reflect.Method;
					import java.lang.reflect.Modifier;

					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					import java.math.BigDecimal;

					import static mypackage.Test.TOPIC;


					public record Test(
					        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
					        @JsonProperty("test") BigDecimal test

					) {
					        public static final String TOPIC = "test";

					        public static void main(String[] args) {
					        	Method[] methods = Test.class.getDeclaredMethods();

					            for (Method method : methods) {
					                // Get modifiers, return type, and name
					                String modifiers = Modifier.toString(method.getModifiers());
					                String returnType = method.getReturnType().getSimpleName();
					                String name = method.getName();

					                // Get parameter types
					                Class<?>[] params = method.getParameterTypes();
					                StringBuilder paramList = new StringBuilder();
					                for (int i = 0; i < params.length; i++) {
					                    if (i > 0) paramList.append(", ");
					                    paramList.append(params[i].getSimpleName());
					                }

					                // Print it out
					                System.out.printf("%s %s %s(%s)%n", modifiers, returnType, name, paramList);
					            }
							}
					}


					@Target({FIELD, METHOD, PARAMETER, TYPE, ANNOTATION_TYPE})
					@Retention(RetentionPolicy.RUNTIME)
					@interface Schema {
						enum RequiredMode {
						    AUTO,
						    REQUIRED,
						    NOT_REQUIRED;
						}
						static enum AccessMode {
					        AUTO,
					        READ_ONLY,
					        WRITE_ONLY,
					        READ_WRITE;
					    }
					    String name() default "";
					    String title() default "";
					    String description() default "";
					    Class<?> implementation() default Void.class;
					    AccessMode accessMode() default AccessMode.AUTO;
					    RequiredMode requiredMode() default RequiredMode.AUTO;
					    boolean hidden() default false;
					    // … many more elements …
					}

					@Target({ANNOTATION_TYPE, FIELD, METHOD, PARAMETER})
					@Retention(RetentionPolicy.RUNTIME)
					@interface JsonProperty {
					    String value() default "";
					    boolean required() default false;
					    int index() default -1;
					    String defaultValue() default "";
					   // Access access() default Access.AUTO;
					    String namespace() default "";
					    // …
					}
					""",
	            },
				"public static void main(String[])\n" +
				"public final boolean equals(Object)\n" +
				"public final String toString()\n" +
				"public final int hashCode()\n" +
				"public BigDecimal test()");

}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4616
// NPE because annotation.resolvedType is null
public void testIssue4616() throws Exception {
	this.runConformTest(
		new String[] {
					"test/Broken.java",
					"""
					package test;

					import test.Schema.RequiredMode;

					public record Broken(@Schema(description = "str", requiredMode = RequiredMode.REQUIRED) @JsonProperty("str")  Broken.UpsertEnvironmentDto environment) {
						public record UpsertEnvironmentDto() {}
						public static void main(String [] args) {
						    System.out.println("OK!");
						}
					}

					@interface JsonProperty {
						String value();
					}

					@interface Schema {

					    RequiredMode requiredMode();

					    String description();

					    enum RequiredMode {
					        REQUIRED,
					    }
					}
					""",
	            },
				"OK!");

}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4616
// NPE because annotation.resolvedType is null
public void testIssue4616_fuller() throws Exception {
	this.runConformTest(
		new String[] {
					"test/Broken.java",
					"""
					package test;

					import test.Schema.RequiredMode;

					import java.util.Map;

					public record Broken(

					        @Schema(description = "str", requiredMode = RequiredMode.NOT_REQUIRED) @JsonProperty("str")  String externalAccountId,

					        @Schema(description = "str", requiredMode = RequiredMode.REQUIRED) @JsonProperty("str") String productId,

					        @Schema(description = "str", requiredMode = RequiredMode.REQUIRED) @JsonProperty("str") Broken.UpsertEnvironmentDto environment,

					        @Schema(description = \"\"\"
					            str\"\"\", requiredMode = RequiredMode.NOT_REQUIRED) @JsonProperty("str") Map<String, String> metadata) {
					    public record UpsertEnvironmentDto(

					            @Schema(description = "str", requiredMode = RequiredMode.REQUIRED) @JsonProperty("str") String id,

					            @Schema(description = "str", requiredMode = RequiredMode.REQUIRED) @JsonProperty("str") String url) {

					    }
					    public static void main(String [] args) {
						    System.out.println("OK!");
						}
					}


					@interface JsonProperty {
						String value();
					}

					@interface Schema {

					    RequiredMode requiredMode();

					    String description();

					    enum RequiredMode {
					        REQUIRED,
					        NOT_REQUIRED,
					    }
					}
					""",
	            },
				"OK!");
	String expectedOutput =
					"  // Field descriptor #6 Ljava/lang/String;\n" +
					"  private final java.lang.String externalAccountId;\n" +
					"    RuntimeInvisibleAnnotations: \n" +
					"      #8 @test.Schema(\n" +
					"        #9 description=\"str\" (constant type)\n" +
					"        #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
					"      )\n" +
					"      #14 @test.JsonProperty(\n" +
					"        #15 value=\"str\" (constant type)\n" +
					"      )\n" +
					"  \n" +
					"  // Field descriptor #6 Ljava/lang/String;\n" +
					"  private final java.lang.String productId;\n" +
					"    RuntimeInvisibleAnnotations: \n" +
					"      #8 @test.Schema(\n" +
					"        #9 description=\"str\" (constant type)\n" +
					"        #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
					"      )\n" +
					"      #14 @test.JsonProperty(\n" +
					"        #15 value=\"str\" (constant type)\n" +
					"      )\n" +
					"  \n" +
					"  // Field descriptor #19 Ltest/Broken$UpsertEnvironmentDto;\n" +
					"  private final test.Broken$UpsertEnvironmentDto environment;\n" +
					"    RuntimeInvisibleAnnotations: \n" +
					"      #8 @test.Schema(\n" +
					"        #9 description=\"str\" (constant type)\n" +
					"        #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
					"      )\n" +
					"      #14 @test.JsonProperty(\n" +
					"        #15 value=\"str\" (constant type)\n" +
					"      )\n" +
					"  \n" +
					"  // Field descriptor #21 Ljava/util/Map;\n" +
					"  // Signature: Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;\n" +
					"  private final java.util.Map metadata;\n" +
					"    RuntimeInvisibleAnnotations: \n" +
					"      #8 @test.Schema(\n" +
					"        #9 description=\"str\" (constant type)\n" +
					"        #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
					"      )\n" +
					"      #14 @test.JsonProperty(\n" +
					"        #15 value=\"str\" (constant type)\n" +
					"      )\n" +
					"  \n" +
					"  // Method descriptor #25 ([Ljava/lang/String;)V\n" +
					"  // Stack: 2, Locals: 1\n" +
					"  public static void main(java.lang.String[] args);\n" +
					"    0  getstatic java.lang.System.out : java.io.PrintStream [27]\n" +
					"    3  ldc <String \"OK!\"> [33]\n" +
					"    5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [35]\n" +
					"    8  return\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 25]\n" +
					"        [pc: 8, line: 26]\n" +
					"      Local variable table:\n" +
					"        [pc: 0, pc: 9] local: args index: 0 type: java.lang.String[]\n" +
					"  \n" +
					"  // Method descriptor #45 ()Ljava/lang/String;\n" +
					"  // Stack: 1, Locals: 1\n" +
					"  public java.lang.String externalAccountId();\n" +
					"    0  aload_0 [this]\n" +
					"    1  getfield test.Broken.externalAccountId : java.lang.String [46]\n" +
					"    4  areturn\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 9]\n" +
					"    RuntimeInvisibleAnnotations: \n" +
					"      #8 @test.Schema(\n" +
					"        #9 description=\"str\" (constant type)\n" +
					"        #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
					"      )\n" +
					"      #14 @test.JsonProperty(\n" +
					"        #15 value=\"str\" (constant type)\n" +
					"      )\n" +
					"  \n" +
					"  // Method descriptor #45 ()Ljava/lang/String;\n" +
					"  // Stack: 1, Locals: 1\n" +
					"  public java.lang.String productId();\n" +
					"    0  aload_0 [this]\n" +
					"    1  getfield test.Broken.productId : java.lang.String [48]\n" +
					"    4  areturn\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 11]\n" +
					"    RuntimeInvisibleAnnotations: \n" +
					"      #8 @test.Schema(\n" +
					"        #9 description=\"str\" (constant type)\n" +
					"        #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
					"      )\n" +
					"      #14 @test.JsonProperty(\n" +
					"        #15 value=\"str\" (constant type)\n" +
					"      )\n" +
					"  \n" +
					"  // Method descriptor #50 ()Ltest/Broken$UpsertEnvironmentDto;\n" +
					"  // Stack: 1, Locals: 1\n" +
					"  public test.Broken.UpsertEnvironmentDto environment();\n" +
					"    0  aload_0 [this]\n" +
					"    1  getfield test.Broken.environment : test.Broken.UpsertEnvironmentDto [51]\n" +
					"    4  areturn\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 13]\n" +
					"    RuntimeInvisibleAnnotations: \n" +
					"      #8 @test.Schema(\n" +
					"        #9 description=\"str\" (constant type)\n" +
					"        #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
					"      )\n" +
					"      #14 @test.JsonProperty(\n" +
					"        #15 value=\"str\" (constant type)\n" +
					"      )\n" +
					"  \n" +
					"  // Method descriptor #53 ()Ljava/util/Map;\n" +
					"  // Signature: ()Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;\n" +
					"  // Stack: 1, Locals: 1\n" +
					"  public java.util.Map metadata();\n" +
					"    0  aload_0 [this]\n" +
					"    1  getfield test.Broken.metadata : java.util.Map [55]\n" +
					"    4  areturn\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 16]\n" +
					"    RuntimeInvisibleAnnotations: \n" +
					"      #8 @test.Schema(\n" +
					"        #9 description=\"str\" (constant type)\n" +
					"        #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
					"      )\n" +
					"      #14 @test.JsonProperty(\n" +
					"        #15 value=\"str\" (constant type)\n" +
					"      )\n" +
					"  \n" +
					"  // Method descriptor #45 ()Ljava/lang/String;\n" +
					"  // Stack: 2, Locals: 1\n" +
					"  public final java.lang.String toString();\n" +
					"    0  aload_0 [this]\n" +
					"    1  invokedynamic 0 toString(test.Broken) : java.lang.String [58]\n" +
					"    6  areturn\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 1]\n" +
					"  \n" +
					"  // Method descriptor #62 ()I\n" +
					"  // Stack: 2, Locals: 1\n" +
					"  public final int hashCode();\n" +
					"    0  aload_0 [this]\n" +
					"    1  invokedynamic 0 hashCode(test.Broken) : int [63]\n" +
					"    6  ireturn\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 1]\n" +
					"  \n" +
					"  // Method descriptor #67 (Ljava/lang/Object;)Z\n" +
					"  // Stack: 2, Locals: 2\n" +
					"  public final boolean equals(java.lang.Object arg0);\n" +
					"    0  aload_0 [this]\n" +
					"    1  aload_1 [arg0]\n" +
					"    2  invokedynamic 0 equals(test.Broken, java.lang.Object) : boolean [68]\n" +
					"    7  ireturn\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 1]\n" +
					"  \n" +
					"  // Method descriptor #72 (Ljava/lang/String;Ljava/lang/String;Ltest/Broken$UpsertEnvironmentDto;Ljava/util/Map;)V\n" +
					"  // Signature: (Ljava/lang/String;Ljava/lang/String;Ltest/Broken$UpsertEnvironmentDto;Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;)V\n" +
					"  // Stack: 2, Locals: 5\n" +
					"  public Broken(java.lang.String externalAccountId, java.lang.String productId, test.Broken.UpsertEnvironmentDto environment, java.util.Map metadata);\n" +
					"     0  aload_0 [this]\n" +
					"     1  invokespecial java.lang.Record() [75]\n" +
					"     4  aload_0 [this]\n" +
					"     5  aload_1 [externalAccountId]\n" +
					"     6  putfield test.Broken.externalAccountId : java.lang.String [46]\n" +
					"     9  aload_0 [this]\n" +
					"    10  aload_2 [productId]\n" +
					"    11  putfield test.Broken.productId : java.lang.String [48]\n" +
					"    14  aload_0 [this]\n" +
					"    15  aload_3 [environment]\n" +
					"    16  putfield test.Broken.environment : test.Broken.UpsertEnvironmentDto [51]\n" +
					"    19  aload_0 [this]\n" +
					"    20  aload 4 [metadata]\n" +
					"    22  putfield test.Broken.metadata : java.util.Map [55]\n" +
					"    25  return\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 1]\n" +
					"      Method Parameters:\n" +
					"        externalAccountId\n" +
					"        productId\n" +
					"        environment\n" +
					"        metadata\n" +
					"    RuntimeInvisibleParameterAnnotations: \n" +
					"      Number of annotations for parameter 0: 2\n" +
					"        #8 @test.Schema(\n" +
					"          #9 description=\"str\" (constant type)\n" +
					"          #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
					"        )\n" +
					"        #14 @test.JsonProperty(\n" +
					"          #15 value=\"str\" (constant type)\n" +
					"        )\n" +
					"      Number of annotations for parameter 1: 2\n" +
					"        #8 @test.Schema(\n" +
					"          #9 description=\"str\" (constant type)\n" +
					"          #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
					"        )\n" +
					"        #14 @test.JsonProperty(\n" +
					"          #15 value=\"str\" (constant type)\n" +
					"        )\n" +
					"      Number of annotations for parameter 2: 2\n" +
					"        #8 @test.Schema(\n" +
					"          #9 description=\"str\" (constant type)\n" +
					"          #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
					"        )\n" +
					"        #14 @test.JsonProperty(\n" +
					"          #15 value=\"str\" (constant type)\n" +
					"        )\n" +
					"      Number of annotations for parameter 3: 2\n" +
					"        #8 @test.Schema(\n" +
					"          #9 description=\"str\" (constant type)\n" +
					"          #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
					"        )\n" +
					"        #14 @test.JsonProperty(\n" +
					"          #15 value=\"str\" (constant type)\n" +
					"        )\n" +
					"\n" +
					"  Inner classes:\n" +
					"    [inner class info: #96 java/lang/invoke/MethodHandles$Lookup, outer class info: #98 java/lang/invoke/MethodHandles\n" +
					"     inner name: #100 Lookup, accessflags: 25 public static final],\n" +
					"    [inner class info: #101 test/Broken$UpsertEnvironmentDto, outer class info: #1 test/Broken\n" +
					"     inner name: #103 UpsertEnvironmentDto, accessflags: 25 public static final],\n" +
					"    [inner class info: #104 test/Schema$RequiredMode, outer class info: #106 test/Schema\n" +
					"     inner name: #108 RequiredMode, accessflags: 16409 public static final]\n" +
					"\n" +
					"Nest Members:\n" +
					"   #101 test/Broken$UpsertEnvironmentDto\n" +
					"\n" +
					"Record: #Record\n" +
					"Components:\n" +
					"  \n" +
					"// Component descriptor #6 Ljava/lang/String;\n" +
					"java.lang.String externalAccountId;\n" +
					"  RuntimeInvisibleAnnotations: \n" +
					"    #8 @test.Schema(\n" +
					"      #9 description=\"str\" (constant type)\n" +
					"      #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
					"    )\n" +
					"    #14 @test.JsonProperty(\n" +
					"      #15 value=\"str\" (constant type)\n" +
					"    )\n" +
					"// Component descriptor #6 Ljava/lang/String;\n" +
					"java.lang.String productId;\n" +
					"  RuntimeInvisibleAnnotations: \n" +
					"    #8 @test.Schema(\n" +
					"      #9 description=\"str\" (constant type)\n" +
					"      #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
					"    )\n" +
					"    #14 @test.JsonProperty(\n" +
					"      #15 value=\"str\" (constant type)\n" +
					"    )\n" +
					"// Component descriptor #19 Ltest/Broken$UpsertEnvironmentDto;\n" +
					"test.Broken$UpsertEnvironmentDto environment;\n" +
					"  RuntimeInvisibleAnnotations: \n" +
					"    #8 @test.Schema(\n" +
					"      #9 description=\"str\" (constant type)\n" +
					"      #11 requiredMode=test.Schema.RequiredMode.REQUIRED(enum type #12.#17)\n" +
					"    )\n" +
					"    #14 @test.JsonProperty(\n" +
					"      #15 value=\"str\" (constant type)\n" +
					"    )\n" +
					"// Component descriptor #21 Ljava/util/Map;\n" +
					"// Signature: Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;\n" +
					"java.util.Map metadata;\n" +
					"  RuntimeInvisibleAnnotations: \n" +
					"    #8 @test.Schema(\n" +
					"      #9 description=\"str\" (constant type)\n" +
					"      #11 requiredMode=test.Schema.RequiredMode.NOT_REQUIRED(enum type #12.#13)\n" +
					"    )\n" +
					"    #14 @test.JsonProperty(\n" +
					"      #15 value=\"str\" (constant type)\n" +
					"    )\n";
	verifyClassFile(expectedOutput, "test/Broken.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4622
// Inconsistent classfile encountered on annotated generic types
public void testIssue4622() throws Exception {
	this.runConformTest(
		new String[] {
					"test/Record.java",
					"""
					package test;

					import java.lang.annotation.Target;

					public record Record(
					        Patch<@ValidUrlTemplate(value = UrlValidationType.AA, message = "{}") String> labelUrl) {

					        public static void main(String [] args) {
						    	System.out.println("OK!");
						}
					}

					class Patch<T> {
					    public T field;
					}

					enum UrlValidationType {
					    AA, BB
					}

					@Target(java.lang.annotation.ElementType.TYPE_USE)
					@interface ValidUrlTemplate {
					    UrlValidationType value();
					    String message();
					}
					""",
	            },
				"OK!");
	String expectedOutput =
					"  // Field descriptor #6 Ltest/Patch;\n" +
					"  // Signature: Ltest/Patch<Ljava/lang/String;>;\n" +
					"  private final test.Patch labelUrl;\n" +
					"    RuntimeInvisibleTypeAnnotations: \n" +
					"      #10 @test.ValidUrlTemplate(\n" +
					"        #11 value=test.UrlValidationType.AA(enum type #12.#13)\n" +
					"        #14 message=\"{}\" (constant type)\n" +
					"        target type = 0x13 FIELD\n" +
					"        location = [TYPE_ARGUMENT(0)]\n" +
					"      )\n" +
					"  \n" +
					"  // Method descriptor #17 ([Ljava/lang/String;)V\n" +
					"  // Stack: 2, Locals: 1\n" +
					"  public static void main(java.lang.String[] args);\n" +
					"    0  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
					"    3  ldc <String \"OK!\"> [25]\n" +
					"    5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [27]\n" +
					"    8  return\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 9]\n" +
					"        [pc: 8, line: 10]\n" +
					"      Local variable table:\n" +
					"        [pc: 0, pc: 9] local: args index: 0 type: java.lang.String[]\n" +
					"  \n" +
					"  // Method descriptor #37 ()Ltest/Patch;\n" +
					"  // Signature: ()Ltest/Patch<Ljava/lang/String;>;\n" +
					"  // Stack: 1, Locals: 1\n" +
					"  public test.Patch labelUrl();\n" +
					"    0  aload_0 [this]\n" +
					"    1  getfield test.Record.labelUrl : test.Patch [39]\n" +
					"    4  areturn\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 6]\n" +
					"    RuntimeInvisibleTypeAnnotations: \n" +
					"      #10 @test.ValidUrlTemplate(\n" +
					"        #11 value=test.UrlValidationType.AA(enum type #12.#13)\n" +
					"        #14 message=\"{}\" (constant type)\n" +
					"        target type = 0x14 METHOD_RETURN\n" +
					"        location = [TYPE_ARGUMENT(0)]\n" +
					"      )";
	verifyClassFile(expectedOutput, "test/Record.class", ClassFileBytesDisassembler.SYSTEM);
}
}