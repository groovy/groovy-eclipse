/*******************************************************************************
 * Copyright (c) 2020, 2024 IBM Corporation and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SealedTypesTests extends AbstractRegressionTest9 {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug564498_6"};
	}

	public static Class<?> testClass() {
		return SealedTypesTests.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}
	public SealedTypesTests(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("17");
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview("17"));
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
		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview("16") :
			JavacTestOptions.forReleaseWithPreview("16", javacAdditionalTestOptions);
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

	public void testBug563430_001() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class Y permits X{}\n" +
				"non-sealed class X extends Y {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n",
			},
			"0");
	}
	public void testBug563430_001a() {
		runConformTest(
			new String[] {
				"X.java",
				"non-sealed class X extends Y {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n",
				"Y.java",
				"sealed class Y permits X{}\n",
			},
			"0");
	}
	public void testBug563430_002() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I extends SI{}\n"+
				"non-sealed class X implements SI{\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n" +
				"sealed interface SI permits X, I{}\n" +
				"non-sealed interface I2 extends I{}\n"
			},
			"0");
	}
	public void testBug562715_001() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class X permits Y {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n" +
				"non-sealed class Y extends X {\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public sealed class X {\n"+
				"  public static void main(String[] args){\n"+
				"     int sealed = 100;\n" +
				"     System.out.println(sealed);\n" +
				"  }\n"+
				"}\n" +
				"non-sealed class Y extends X {\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_003() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed public class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n" +
				"non-sealed class Y extends X {\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_004() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I {}\n"+
				"sealed public class X<T> {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n" +
				"non-sealed class Y<T> extends X<T> {\n"+
				"}\n" +
				"non-sealed interface I2 extends I {}\n"
			},
			"100");
	}
	public void testBug562715_004a() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed public class X<T> {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n" +
				"non-sealed class Y extends X {\n"+
				"}\n"
			},
			"100");
	}
	public void testBug562715_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed public sealed class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	sealed public sealed class X {\n" +
			"	                           ^\n" +
			"Duplicate modifier for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	sealed public sealed class X {\n" +
			"	                           ^\n" +
			"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface\n" +
			"----------\n");
	}
	public void testBug562715_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X {\n"+
				"  public static sealed void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed class X {\n" +
			"	                    ^\n" +
			"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	public static sealed void main(String[] args){\n" +
			"	              ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n");
	}
	public void testBug562715_007() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed @MyAnnot public class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n" +
				"@interface MyAnnot {}\n" +
				"non-sealed class Y extends X{}"
			},
			"100");
	}
	public void testBug562715_008() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class X permits Y {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n"+
				"sealed class Y extends X {}\n" +
				"final class Z extends Y {}\n"
			},
			"100");
	}
	public void testBug562715_009() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class X permits Y,Z {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n"+
				"sealed class Y extends X {}\n" +
				"final class Z extends X {}\n" +
				"final class Y2 extends Y {}\n"
			},
			"100");
	}
	public void testBug562715_010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X permits {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits {\n" +
			"	                    ^\n" +
			"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits {\n" +
			"	                      ^^^^^^^\n" +
			"Syntax error on token \"permits\", { expected\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits {\n" +
			"	                              ^\n" +
			"Syntax error, insert \"}\" to complete Block\n" +
			"----------\n");
	}
	// TODO : Enable after error flag code implemented
	public void testBug562715_011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed enum Natural {ONE, TWO}\n"+
				"public sealed class X {\n"+
				"  public static sealed void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	sealed enum Natural {ONE, TWO}\n" +
			"	            ^^^^^^^\n" +
			"Illegal modifier for the enum Natural; only public is permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	public sealed class X {\n" +
			"	                    ^\n" +
			"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	public static sealed void main(String[] args){\n" +
			"	              ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n");
	}
	public void testBug562715_xxx() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed record R() {}\n"+
				"public sealed class X {\n"+
				"  public static sealed void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	sealed record R() {}\n" +
			"	              ^\n" +
			"Illegal modifier for the record R; only public, final and strictfp are permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	public sealed class X {\n" +
			"	                    ^\n" +
			"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	public static sealed void main(String[] args){\n" +
			"	              ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n");
	}
	public void testBug563806_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X permits Y, Z{\n"+
				"}\n"+
				"class Y {}\n"+
				"class Z {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits Y, Z{\n" +
			"	                              ^\n" +
			"Permitted class Y does not declare X as direct super class\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits Y, Z{\n" +
			"	                                 ^\n" +
			"Permitted class Z does not declare X as direct super class\n" +
			"----------\n");
	}
	public void testBug563806_002() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y{\n"+
				"}\n"+
				"class Y {}\n"+
				"class Z extends X{}",
				"p1/A.java",
				"package p1;\n"+
				"public sealed class A extends X{}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed class X permits Y{\n" +
			"	                              ^\n" +
			"Permitted class Y does not declare p1.X as direct super class\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 5)\n" +
			"	class Z extends X{}\n" +
			"	      ^\n" +
			"The class Z with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n" +
			"3. ERROR in p1\\X.java (at line 5)\n" +
			"	class Z extends X{}\n" +
			"	                ^\n" +
			"The type Z extending a sealed class X should be a permitted subtype of X\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in p1\\A.java (at line 2)\n" +
			"	public sealed class A extends X{}\n" +
			"	                    ^\n" +
			"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares A as its direct superclass or superinterface\n" +
			"----------\n" +
			"2. ERROR in p1\\A.java (at line 2)\n" +
			"	public sealed class A extends X{}\n" +
			"	                              ^\n" +
			"The type A extending a sealed class X should be a permitted subtype of X\n" +
			"----------\n");
	}
	public void testBug563806_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed interface X permits Y, Z{\n"+
				"}\n"+
				"class Y implements X{}\n"+
				"class Z {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed interface X permits Y, Z{\n" +
			"	                                     ^\n" +
			"Permitted type Z does not declare X as direct super interface \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	class Y implements X{}\n" +
			"	      ^\n" +
			"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_004() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, Z, Q{\n"+
				"}\n"+
				"class Y implements X{}\n" +
				"interface Z {}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, Z, Q{\n" +
			"	                                     ^\n" +
			"Permitted type Z does not declare p1.X as direct super interface \n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, Z, Q{\n" +
			"	                                        ^\n" +
			"Q cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in p1\\X.java (at line 4)\n" +
			"	class Y implements X{}\n" +
			"	      ^\n" +
			"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X permits Y, Y{\n"+
				"}\n"+
				"class Y extends X {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits Y, Y{\n" +
			"	                                 ^\n" +
			"Duplicate type Y for the type X in the permits clause\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	class Y extends X {}\n" +
			"	      ^\n" +
			"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_006() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y, p1.Y{\n"+
				"}\n"+
				"class Y extends X {}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed class X permits Y, p1.Y{\n" +
			"	                                 ^^^^\n" +
			"Duplicate type Y for the type X in the permits clause\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 4)\n" +
			"	class Y extends X {}\n" +
			"	      ^\n" +
			"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"}\n"+
				"non-sealed class Y extends X {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	non-sealed class Y extends X {}\n" +
			"	                 ^\n" +
			"A class Y declared as non-sealed should have either a sealed direct superclass or a sealed direct superinterface\n" +
			"----------\n");
	}
	public void testBug563806_008() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y {\n"+
				"}\n"+
				"class Y implements X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"non-sealed public interface Y {}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 4)\n" +
			"	class Y implements X{}\n" +
			"	      ^\n" +
			"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in p2\\Y.java (at line 2)\n" +
			"	non-sealed public interface Y {}\n" +
			"	                            ^\n" +
			"An interface Y declared as non-sealed should have a sealed direct superinterface\n" +
			"----------\n");
	}
	public void testBug563806_009() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public sealed class X {\n"+
				"  public static void main(String[] args){\n"+
				"     System.out.println(100);\n" +
				"  }\n"+
				"}\n"+
				"final class Y extends X {}",
			},
			"100");
	}
	public void testBug563806_010() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y {\n"+
				"}\n"+
				"final class Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y extends p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p2\\Y.java (at line 2)\n" +
			"	public final class Y extends p1.X{}\n" +
			"	                             ^^^^\n" +
			"Sealed type X and sub type Y in an unnamed module should be declared in the same package p1\n" +
			"----------\n");
	}
	public void testBug563806_011() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) { \n" +
				"    System.out.println(\"0\");\n" +
				"  }\n" +
				"}\n" +
				"sealed interface Y {\n"+
				"}\n"+
				"final class Z implements Y {}",
			},
			"0");
	}
	public void testBug563806_012() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y {\n"+
				"}\n"+
				"final class Y implements X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y implements p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p2\\Y.java (at line 2)\n" +
			"	public final class Y implements p1.X{}\n" +
			"	                                ^^^^\n" +
			"Sealed type X and sub type Y in an unnamed module should be declared in the same package p1\n" +
			"----------\n");
	}
	public void testBug563806_013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed interface X {\n"+
				"}\n"+
				"interface Y extends X {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	interface Y extends X {}\n" +
			"	          ^\n" +
			"The interface Y with a sealed direct superinterface X should be declared either sealed or non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_014() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y {\n"+
				"}\n"+
				"interface Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public interface Y extends p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 4)\n" +
			"	interface Y extends X{}\n" +
			"	          ^\n" +
			"The interface Y with a sealed direct superinterface X should be declared either sealed or non-sealed\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in p2\\Y.java (at line 2)\n" +
			"	public interface Y extends p1.X{}\n" +
			"	                 ^\n" +
			"The interface Y with a sealed direct superinterface X should be declared either sealed or non-sealed\n" +
			"----------\n" +
			"2. ERROR in p2\\Y.java (at line 2)\n" +
			"	public interface Y extends p1.X{}\n" +
			"	                           ^^^^\n" +
			"Sealed type X and sub type Y in an unnamed module should be declared in the same package p1\n" +
			"----------\n");
	}
	public void testBug563806_015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X permits Y{\n"+
				"}\n"+
				"final class Y extends X {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X permits Y{\n" +
			"	             ^\n" +
			"A type declaration X that has a permits clause should have a sealed modifier\n" +
			"----------\n");
	}
	public void testBug563806_016() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public class X permits Y {\n"+
				"}\n"+
				"final class Y extends X{}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public class X permits Y {\n" +
			"	             ^\n" +
			"A type declaration X that has a permits clause should have a sealed modifier\n" +
			"----------\n");
	}
	public void testBug563806_017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public interface X permits Y{\n"+
				"}\n"+
				"final class Y implements X {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public interface X permits Y{\n" +
			"	                 ^\n" +
			"A type declaration X that has a permits clause should have a sealed modifier\n" +
			"----------\n");
	}
	public void testBug563806_018() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public interface X permits Y {\n"+
				"}\n"+
				"final class Y implements X{}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public interface X permits Y {\n" +
			"	                 ^\n" +
			"A type declaration X that has a permits clause should have a sealed modifier\n" +
			"----------\n");
	}
	public void testBug563806_019() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y extends p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed class X permits Y, p2.Y {\n" +
			"	                                 ^^^^\n" +
			"Permitted type Y in an unnamed module should be declared in the same package p1 of declaring type X\n" +
			"----------\n");
	}
	public void testBug563806_020() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y implements X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y implements p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y in an unnamed module should be declared in the same package p1 of declaring type X\n" +
			"----------\n");
	}
	public void testBug563806_021() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"non-sealed interface Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public non-sealed interface Y extends p1.X{}",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y in an unnamed module should be declared in the same package p1 of declaring type X\n" +
			"----------\n");
	}
	public void testBug563806_022() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.two", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"requires mod.two;\n"+
				"}\n",
				"mod.two/module-info.java",
				"module mod.two {\n" +
				"exports p2;\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y {}",
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed class X permits Y, p2.Y {\n" +
			"	                                 ^^^^\n" +
			"Permitted type Y in a named module mod.one should be declared in the same module mod.one of declaring type X\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed class X permits Y, p2.Y {\n" +
			"	                                 ^^^^\n" +
			"Permitted class Y does not declare p1.X as direct super class\n" +
			"----------\n";
		runner.runNegativeTest();
	}
	public void testBug563806_023() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.two", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"requires mod.two;\n"+
				"}\n",
				"mod.two/module-info.java",
				"module mod.two {\n" +
				"exports p2;\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y implements X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y {}",
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y in a named module mod.one should be declared in the same module mod.one of declaring type X\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y does not declare p1.X as direct super interface \n" +
			"----------\n";
		runner.runNegativeTest();
	}
	public void testBug563806_024() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.two", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"requires mod.two;\n"+
				"}\n",
				"mod.two/module-info.java",
				"module mod.two {\n" +
				"exports p2;\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"non-sealed interface Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public interface Y {}",
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y in a named module mod.one should be declared in the same module mod.one of declaring type X\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed interface X permits Y, p2.Y {\n" +
			"	                                     ^^^^\n" +
			"Permitted type Y does not declare p1.X as direct super interface \n" +
			"----------\n";
		runner.runNegativeTest();
	}
	public void testBug563806_025() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.one", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed class X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y extends p1.X{}",
			};
		runner.runConformTest();
	}
	public void testBug563806_026() {
		associateToModule("mod.one", "p1/X.java", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"final class Y implements X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public final class Y implements p1.X{}",
			};
		runner.runConformTest();
	}
	public void testBug563806_027() {
		associateToModule("mod.one", "p1/X.java");
		associateToModule("mod.one", "p2/Y.java");
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"mod.one/module-info.java",
				"module mod.one {\n"+
				"}\n",
				"p1/X.java",
				"package p1;\n"+
				"public sealed interface X permits Y, p2.Y {\n"+
				"}\n"+
				"non-sealed interface Y extends X{}\n",
				"p2/Y.java",
				"package p2;\n"+
				"public non-sealed interface Y extends p1.X {}",
			};
		runner.runConformTest();
	}
	public void testBug563806_028() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public non-sealed enum X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public non-sealed enum X {\n" +
			"	                       ^\n" +
			"Illegal modifier for the enum X; only public is permitted\n" +
			"----------\n");
	}
	public void testBug563806_029() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed enum X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed enum X {\n" +
			"	                   ^\n" +
			"Illegal modifier for the enum X; only public is permitted\n" +
			"----------\n");
	}
	public void testBug563806_030() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public class X {\n"+
				"static sealed enum Y {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 3)\n" +
			"	static sealed enum Y {}\n" +
			"	                   ^\n" +
			"Illegal modifier for the member enum Y; only public, protected, private & static are permitted\n" +
			"----------\n");
	}
	public void testBug563806_031() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public class X {\n"+
				"static non-sealed enum Y {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 3)\n" +
			"	static non-sealed enum Y {}\n" +
			"	                       ^\n" +
			"Illegal modifier for the member enum Y; only public, protected, private & static are permitted\n" +
			"----------\n");
	}
	public void testBug563806_032() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed non-sealed interface X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed non-sealed interface X {\n" +
			"	                                   ^\n" +
			"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed non-sealed interface X {\n" +
			"	                                   ^\n" +
			"An interface X is declared both sealed and non-sealed\n" +
			"----------\n");
	}
	public void testBug563806_033() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public sealed  @interface X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public sealed  @interface X {\n" +
			"	       ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n");
	}
	public void testBug563806_034() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public  non-sealed @interface X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public  non-sealed @interface X {\n" +
			"	                              ^\n" +
			"An interface X declared as non-sealed should have a sealed direct superinterface\n" +
			"----------\n");
	}
	public void testBug563806_035() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public  non-sealed interface X {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	public  non-sealed interface X {\n" +
			"	                             ^\n" +
			"An interface X declared as non-sealed should have a sealed direct superinterface\n" +
			"----------\n");
	}
	public void testBug563806_036() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public  class X {\n"+
				"  public void foo() {\n"+
				"    sealed class Y{}\n"+
				"  }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 4)\n" +
			"	sealed class Y{}\n" +
			"	             ^\n" +
			"Illegal modifier for the local class Y; only abstract or final is permitted\n" +
			"----------\n");
	}
	public void testBug563806_037() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public  class X {\n"+
				"  public void foo() {\n"+
				"    non-sealed class Y{}\n"+
				"  }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 4)\n" +
			"	non-sealed class Y{}\n" +
			"	                 ^\n" +
			"Illegal modifier for the local class Y; only abstract or final is permitted\n" +
			"----------\n");
	}
	public void testBug563806_038() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"public  class X {\n"+
				"  public void foo() {\n"+
				"    non-sealed sealed class Y{}\n"+
				"  }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 4)\n" +
			"	non-sealed sealed class Y{}\n" +
			"	                        ^\n" +
			"Illegal modifier for the local class Y; only abstract or final is permitted\n" +
			"----------\n");
	}
	public void testBug563806_039() {
		this.runNegativeTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"sealed class A{}\n"+
				"public  class X {\n"+
				"  public void foo() {\n"+
				"    class Y extends A{}\n"+
				"  }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in p1\\X.java (at line 2)\n" +
			"	sealed class A{}\n" +
			"	             ^\n" +
			"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares A as its direct superclass or superinterface\n" +
			"----------\n" +
			"2. ERROR in p1\\X.java (at line 5)\n" +
			"	class Y extends A{}\n" +
			"	                ^\n" +
			"A local class Y cannot have a sealed direct superclass or a sealed direct superinterface A\n" +
			"----------\n");
	}
	public void testBug564191_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"p1/X.java",
				"package p1;\n"+
				"sealed class X permits Y, Z{\n" +
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n" +
				"final class Y extends X{}\n" +
				"final class Z extends X{}\n",
			},
			"0");
		String expectedOutput =
				"PermittedSubclasses:\n" +
				"   #33 p1/Y,\n" +
				"   #35 p1/Z\n" +
				"}";
		verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted (top-level) types make it to the .class file
	public void testBug564190_1() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X {\n" +
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}\n" +
					"final class Y extends X{}\n" +
					"final class Z extends X{}\n",
				},
				"0");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #33 p1/Y,\n" +
					"   #35 p1/Z\n" +
					"}";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted final (member) types make it to the .class file
	public void testBug564190_2() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X {\n" +
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"  final class Y extends X{}\n" +
					"  final class Z extends X{}\n" +
					"}",
				},
				"0");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #33 p1/X$Y,\n" +
					"   #35 p1/X$Z\n" +
					"}";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted non-sealed (member) types make it to the .class file
	public void testBug564190_3() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X {\n" +
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"  non-sealed class Y extends X{}\n" +
					"  non-sealed class Z extends X{}\n" +
					"}",
				},
				"0");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #33 p1/X$Y,\n" +
					"   #35 p1/X$Z\n" +
					"}";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that implicit permitted member type is reported without final, sealed or non-sealed
	public void testBug564190_4() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X  {\n" +
					"	class Y extends X {}\n" +
					"	final class Z extends Y {}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in p1\\X.java (at line 3)\n" +
				"	class Y extends X {}\n" +
				"	      ^\n" +
				"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
				"----------\n");
	}
	// Test that implicit permitted member type with implicit permitted types
	// is reported when its permitted type doesn't extend the member type
	public void testBug564190_5() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X {\n" +
					"	sealed class Y extends X {}\n" +
					"	final class Z {}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in p1\\X.java (at line 3)\n" +
				"	sealed class Y extends X {}\n" +
				"	             ^\n" +
				"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares Y as its direct superclass or superinterface\n" +
				"----------\n");
	}
	// Test that implicit permitted member type with explicit permits clause
	// is reported when its permitted type doesn't extend the member type
	public void testBug564190_6() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X  {\n" +
					"	sealed class Y extends X permits Z {}\n" +
					"	final class Z {}\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in p1\\X.java (at line 3)\n" +
				"	sealed class Y extends X permits Z {}\n" +
				"	                                 ^\n" +
				"Permitted class Z does not declare p1.X.Y as direct super class\n" +
				"----------\n");
	}
	// Test that implicit permitted member type with explicit permits clause
	// is reported when its permitted type doesn't extend the member type
	public void testBug564190_7() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed interface SI {}",
				},
				"----------\n" +
				"1. ERROR in p1\\X.java (at line 2)\n" +
				"	sealed interface SI {}\n" +
				"	                 ^^\n" +
				"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares SI as its direct superclass or superinterface\n" +
				"----------\n");
	}
	public void testBug564450_001() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"sealed class X permits Y{\n" +
					"}",
					"p1/Y.java",
					"package p1;\n"+
					"class Y extends X {\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in p1\\Y.java (at line 2)\n" +
				"	class Y extends X {\n" +
				"	      ^\n" +
				"The class Y with a sealed direct superclass or a sealed direct superinterface X should be declared either final, sealed, or non-sealed\n" +
				"----------\n");
	}
	public void testBug564047_001() throws CoreException, IOException {
		String outputDirectory = Util.getOutputDirectory();
		String lib1Path = outputDirectory + File.separator + "lib1.jar";
		try {
		Util.createJar(
				new String[] {
					"p/Y.java",
					"package p;\n" +
					"public sealed class Y permits Z{}",
					"p/Z.java",
					"package p;\n" +
					"public final class Z extends Y{}",
				},
				lib1Path,
				JavaCore.VERSION_17,
				false);
		String[] libs = getDefaultClassPaths();
		int len = libs.length;
		System.arraycopy(libs, 0, libs = new String[len+1], 0, len);
		libs[len] = lib1Path;
		this.runNegativeTest(
				new String[] {
					"src/p/X.java",
					"package p;\n" +
					"public class X extends Y {\n" +
					"  public static void main(String[] args){\n" +
					"     System.out.println(0);\n" +
					"  }\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in src\\p\\X.java (at line 2)\n" +
				"	public class X extends Y {\n" +
				"	             ^\n" +
				"The class X with a sealed direct superclass or a sealed direct superinterface Y should be declared either final, sealed, or non-sealed\n" +
				"----------\n" +
				"2. ERROR in src\\p\\X.java (at line 2)\n" +
				"	public class X extends Y {\n" +
				"	                       ^\n" +
				"The type X extending a sealed class Y should be a permitted subtype of Y\n" +
				"----------\n",
				libs,
		        true);
		} catch (IOException e) {
			System.err.println("could not write to current working directory ");
		} finally {
			new File(lib1Path).delete();
		}

	}
	public void testBug564492_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  public static void main(String[] args){\n"+
				"     new Y(){};\n" +
				"  }\n"+
				"}\n"+
				"sealed class Y{}\n"+
				"final class Z extends Y {\n"+
			"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new Y(){};\n" +
			"	    ^\n" +
			"An anonymous class cannot subclass a sealed type Y\n" +
			"----------\n");
	}
	public void testBug564492_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X  {\n"+
				"   public static void main(String[] args) {\n"+
				"        IY y = new IY(){};\n"+
				"   }\n"+
				"}\n"+
				"sealed interface I {}\n"+
				"sealed interface IY extends I {}\n"+
				"final class Z implements IY{}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	IY y = new IY(){};\n" +
			"	           ^^\n" +
			"An anonymous class cannot subclass a sealed type IY\n" +
			"----------\n");
	}
	public void testBug564492_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X permits A.Y {\n"+
				"       public static void main(String[] args) {\n"+
				"               new A.Y() {};\n"+
				"       }\n"+
				"}\n"+
				" \n"+
				"class A {\n"+
				"       static sealed class Y extends X permits Z {}\n"+
				"       final class Z extends Y{}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new A.Y() {};\n" +
			"	    ^^^\n" +
			"An anonymous class cannot subclass a sealed type A.Y\n" +
			"----------\n");
	}
	public void testBug564492_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public  class X {\n"+
				"       public static void main(String[] args) {\n"+
				"               new A.IY() {};\n"+
				"       }\n"+
				"}\n"+
				" \n"+
				"class A {\n"+
				"       sealed interface I permits IY{}\n"+
				"       sealed interface IY extends I permits Z {}\n"+
				"       final class Z implements IY{}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new A.IY() {};\n" +
			"	    ^^^^\n" +
			"An anonymous class cannot subclass a sealed type A.IY\n" +
			"----------\n");
	}
	public void testBug564498_1() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"public sealed class X permits A.Y {\n" +
					"	public static void main(String[] args) {}\n" +
					"}\n" +
					"class A {\n" +
					"	sealed class Y extends X {\n" +
					"		final class SubInnerY extends Y {}\n" +
					"	} \n" +
					"	final class Z extends Y {}\n" +
					"}",
				},
				"");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #24 p1/A$Y$SubInnerY,\n" +
					"   #26 p1/A$Z\n" +
					"}";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
			expectedOutput =
					"PermittedSubclasses:\n" +
					"   #21 p1/A$Y\n";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug564498_2() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"public sealed class X permits A.Y {\n" +
					"	public static void main(String[] args) {}\n" +
					"}\n" +
					"class A {\n" +
					"	sealed class Y extends X {} \n" +
					"	final class Z extends Y {}\n" +
					"   final class SubY extends Y {}" +
					"}",
				},
				"");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #22 p1/A$Z,\n" +
					"   #24 p1/A$SubY\n" +
					"}";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
			expectedOutput =
					"PermittedSubclasses:\n" +
					"   #21 p1/A$Y\n";
			verifyClassFile(expectedOutput, "p1/X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug564498_3() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"public sealed class X permits A.Y {\n" +
					"	public static void main(String[] args) {}\n" +
					"}\n" +
					"class A {\n" +
					"	sealed class Y extends X {\n" +
					"		final class SubInnerY extends Y {}\n" +
					"	} \n" +
					"	final class Z extends Y {}\n" +
					"   final class SubY extends Y {}" +
					"}",
				},
				"");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #24 p1/A$Y$SubInnerY,\n" +
					"   #26 p1/A$Z,\n" +
					"   #28 p1/A$SubY\n";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug564498_4() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"public sealed class X permits A.Y {\n" +
					"	public static void main(String[] args) {}\n" +
					"}\n" +
					"class A {\n" +
					"	sealed class Y extends X permits Y.SubInnerY {\n" +
					"		final class SubInnerY extends Y {}\n" +
					"	} \n" +
					"}",
				},
				"");
			String expectedOutput =
					"PermittedSubclasses:\n" +
					"   #24 p1/A$Y$SubInnerY\n";
			verifyClassFile(expectedOutput, "p1/A$Y.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// Reject references of membertype without qualifier of enclosing type in permits clause
	public void testBug564498_5() throws IOException, ClassFormatException {
		runNegativeTest(
				new String[] {
					"p1/X.java",
					"package p1;\n"+
					"public sealed class X permits A.Y {\n" +
					"	public static void main(String[] args) {}\n" +
					"}\n" +
					"class A {\n" +
					"	sealed class Y extends X permits SubInnerY {\n" +
					"		final class SubInnerY extends Y {}\n" +
					"	} \n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in p1\\X.java (at line 7)\n" +
				"	final class SubInnerY extends Y {}\n" +
				"	                              ^\n" +
				"The type SubInnerY extending a sealed class A.Y should be a permitted subtype of A.Y\n" +
				"----------\n");
	}
	// accept references of membertype without qualifier of enclosing type in permits clause
	// provided it is imported
	public void testBug564498_6() throws IOException, ClassFormatException {
		runConformTest(
				new String[] {
						"p1/X.java",
						"package p1;\n"+
						"import p1.Y.Z;\n" +
						"public class X {\n" +
						"	public static void main(String[] args) {}\n" +
						"}\n" +
						"sealed class Y permits Z {\n" +
						"	final class Z extends Y {}\n" +
						"}",
				},
				"");
	}
	public void testBug564613_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"\n"+
				"       public boolean permits( String s ) {\n"+
				"               return true;\n"+
				"       }\n"+
				"       public static void main(String[] args) {\n"+
				"               boolean b = new X().permits(\"hello\");\n"+
				"               System.out.println(b ? \"Hello\" : \"World\");\n"+
				"       }\n"+
				"}",
			},
			"Hello");
	}
	public void testBug564613_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X permits permits Y, Z {}\n"+
				"final class Y extends X{}\n" +
				"final class Z extends X{}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed class X permits permits Y, Z {}\n" +
			"	                      ^^^^^^^\n" +
			"Syntax error on token \"permits\", delete this token\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_001() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class permits {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class permits {\n" +
			"	      ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type permits\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class permits {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class permits {\n" +
			"	      ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type permits\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_003() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  permits p;\n" +
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
				"permits.java",
				"public class permits {\n"+
				"}",
			},
			"----------\n" +
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	permits p;\n" +
			"	^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in permits.java (at line 1)\n" +
			"	public class permits {\n" +
			"	             ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  permits p;\n" +
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
				"permits.java",
				"public class permits {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	permits p;\n" +
			"	^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in permits.java (at line 1)\n" +
			"	public class permits {\n" +
			"	             ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_005() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X<permits> {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X<permits> {\n" +
			"	        ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X<permits>\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X<permits> {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X<permits> {\n" +
			"	        ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X<permits>\n" +
			"----------\n");
	}
	public void testBug564638_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X extends permits {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"class permits {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X extends permits {\n" +
			"	                ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	class permits {\n" +
			"	      ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_008() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X extends permits {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"class permits {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X extends permits {\n" +
			"	                ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	class permits {\n" +
			"	      ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X implements permits {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface permits {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X implements permits {\n" +
			"	                   ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	interface permits {\n" +
			"	          ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_010() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X implements permits {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface permits {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X implements permits {\n" +
			"	                   ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	interface permits {\n" +
			"	          ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_011() {
		runNegativeTest(
			new String[] {
				"X.java",
				"interface X extends permits {\n"+
				"  default void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface permits {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	interface X extends permits {\n" +
			"	                    ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	interface permits {\n" +
			"	          ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_012() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X extends permits {\n"+
				"  default void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface permits {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	interface X extends permits {\n" +
			"	                    ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	interface permits {\n" +
			"	          ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_013() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X extends {\n"+
				"  permits foo() {\n" +
				"    Zork();\n" +
				"    return null;\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X extends {\n" +
			"	        ^^^^^^^\n" +
			"Syntax error on token \"extends\", Type expected after this token\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	permits foo() {\n" +
			"	^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_014() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  permits foo() {\n" +
				"    Zork();\n" +
				"    return null;\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	permits foo() {\n" +
			"	^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_015() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X  {\n"+
				"  void foo() throws permits{\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo() throws permits{\n" +
			"	                  ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_016() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  void foo() throws permits{\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo() throws permits{\n" +
			"	                  ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_017() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X <T extends permits> {\n"+
				"  <T> void foo(T extends permits) {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X <T extends permits> {\n" +
			"	                   ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	class X <T extends permits> {\n" +
			"	                            ^\n" +
			"Syntax error, insert \"}\" to complete ClassBody\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 2)\n" +
			"	<T> void foo(T extends permits) {\n" +
			"	 ^\n" +
			"The type parameter T is hiding the type T\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 2)\n" +
			"	<T> void foo(T extends permits) {\n" +
			"	               ^^^^^^^\n" +
			"Syntax error on token \"extends\", delete this token\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 5)\n" +
			"	}\n" +
			"	^\n" +
			"Syntax error on token \"}\", delete this token\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_018() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X <T extends permits>{\n"+
				"  <T> void foo(T extends permits) {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X <T extends permits>{\n" +
			"	                   ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 1)\n" +
			"	class X <T extends permits>{\n" +
			"	                           ^\n" +
			"Syntax error, insert \"}\" to complete ClassBody\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 2)\n" +
			"	<T> void foo(T extends permits) {\n" +
			"	 ^\n" +
			"The type parameter T is hiding the type T\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 2)\n" +
			"	<T> void foo(T extends permits) {\n" +
			"	               ^^^^^^^\n" +
			"Syntax error on token \"extends\", delete this token\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 5)\n" +
			"	}\n" +
			"	^\n" +
			"Syntax error on token \"}\", delete this token\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_019() {
		runNegativeTest(
			new String[] {
				"X.java",
				"enum X {\n"+
				"  ONE(1);\n" +
				"  private final permits p;\n" +
				"  X(int p) {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	private final permits p;\n" +
			"	              ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_020() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"enum X {\n"+
				"  ONE(1);\n" +
				"  private final permits p;\n" +
				"  X(int p) {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	private final permits p;\n" +
			"	              ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_021() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    I i = (permits p)-> {};\n" +
//				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface I {\n" +
				"  void apply(Object o);\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I i = (permits p)-> {};\n" +
			"	      ^^^^^^^^^^^^^\n" +
			"This lambda expression refers to the missing type permits\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	I i = (permits p)-> {};\n" +
			"	       ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_022() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    I i = (permits p)-> {};\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface I {\n" +
				"  void apply(Object o);\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I i = (permits p)-> {};\n" +
			"	      ^^^^^^^^^^^^^\n" +
			"This lambda expression refers to the missing type permits\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	I i = (permits p)-> {};\n" +
			"	       ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_023() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public void foo(permits this) {}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public void foo(permits this) {}\n" +
			"	                ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_024() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public void foo(permits this) {}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public void foo(permits this) {}\n" +
			"	                ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_025() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public void foo(permits this) {}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public void foo(permits this) {}\n" +
			"	                ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_026() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public void foo(permits this) {}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public void foo(permits this) {}\n" +
			"	                ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_027() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  class permits {\n"+
				"     public void foo(permits this) {}\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	class permits {\n" +
			"	      ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	public void foo(permits this) {}\n" +
			"	                ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_028() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  class permits {\n"+
				"     public void foo(permits this) {}\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	class permits {\n" +
			"	      ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	public void foo(permits this) {}\n" +
			"	                ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_029() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    permits p;\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	permits p;\n" +
			"	^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_030() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    permits p;\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	permits p;\n" +
			"	^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_031() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    for (permits i = 0; i < 10; ++i) {} \n" +
				"  }\n" +
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	for (permits i = 0; i < 10; ++i) {} \n" +
			"	     ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_032() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    for (permits i = 0; i < 10; ++i) {} \n" +
				"  }\n" +
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	for (permits i = 0; i < 10; ++i) {} \n" +
			"	     ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_033() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(permits[] args) {\n" +
				"    for (permits p : args) {} \n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public static void main(permits[] args) {\n" +
			"	                        ^^^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	for (permits p : args) {} \n" +
			"	     ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_034() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(permits[] args) {\n" +
				"    for (permits p : args) {} \n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public static void main(permits[] args) {\n" +
			"	                        ^^^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	for (permits p : args) {} \n" +
			"	     ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_035() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"               try (permits y = new Y()) {\n"+
				"                       \n"+
				"               } catch (Exception e) {\n"+
				"                       e.printStackTrace();\n"+
				"               } finally {\n"+
				"                       \n"+
				"               }\n"+
				"       }\n"+
				"}\n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	try (permits y = new Y()) {\n" +
			"	     ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_036() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"               try (permits y = new Y()) {\n"+
				"                       \n"+
				"               } catch (Exception e) {\n"+
				"                       e.printStackTrace();\n"+
				"               } finally {\n"+
				"                       \n"+
				"               }\n"+
				"       }\n"+
				"}\n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	try (permits y = new Y()) {\n" +
			"	     ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_037() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"               try (Y y = new Y()) {\n"+
				"                       \n"+
				"               } catch (permits e) {\n"+
				"                       e.printStackTrace();\n"+
				"               } finally {\n"+
				"                       \n"+
				"               }\n"+
				"       }\n"+
				"}\n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	} catch (permits e) {\n" +
			"	         ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_038() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"               try (Y y = new Y()) {\n"+
				"                       \n"+
				"               } catch (permits e) {\n"+
				"                       e.printStackTrace();\n"+
				"               } finally {\n"+
				"                       \n"+
				"               }\n"+
				"       }\n"+
				"}\n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	} catch (permits e) {\n" +
			"	         ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_039() {
		runNegativeTest(
			new String[] {
				"X.java",
				"record X(permits p) {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	record X(permits p) {\n" +
			"	^\n" +
			"permits cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	record X(permits p) {\n" +
			"	         ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_040() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"record X(permits p) {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	record X(permits p) {\n" +
			"	^\n" +
			"permits cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	record X(permits p) {\n" +
			"	         ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_041() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> X(T t) {}\n"+
				"       \n"+
				"       public X(int t, char c) {\n"+
				"               <permits>this(t);\n"+
				"       }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	<permits>this(t);\n" +
			"	 ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	<permits>this(t);\n" +
			"	         ^^^^^^^^\n" +
			"The parameterized constructor <permits>X(permits) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_042() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> X(T t) {}\n"+
				"       \n"+
				"       public X(int t, char c) {\n"+
				"               <permits>this(t);\n"+
				"       }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	<permits>this(t);\n" +
			"	 ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	<permits>this(t);\n" +
			"	         ^^^^^^^^\n" +
			"The parameterized constructor <permits>X(permits) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_043() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> X(T t) {}\n"+
				"       \n"+
				"       public X(int t, char c) {\n"+
				"           new <permits>X(t).foo();\n"+
				"       }\n"+
				"       public void foo() {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	new <permits>X(t).foo();\n" +
			"	^^^^^^^^^^^^^^^^^\n" +
			"The parameterized constructor <permits>X(permits) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	new <permits>X(t).foo();\n" +
			"	     ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_044() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> X(T t) {}\n"+
				"       \n"+
				"       public X(int t, char c) {\n"+
				"           new <permits>X(t).foo();\n"+
				"       }\n"+
				"       public void foo() {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	new <permits>X(t).foo();\n" +
			"	^^^^^^^^^^^^^^^^^\n" +
			"The parameterized constructor <permits>X(permits) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	new <permits>X(t).foo();\n" +
			"	     ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_045() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> void foo(T t) {}\n"+
				"       \n"+
				"       public X() {\n"+
				"               X x = new X();\n"+
				"               x.<permits>foo(0);\n"+
				"       }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	x.<permits>foo(0);\n" +
			"	   ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	x.<permits>foo(0);\n" +
			"	           ^^^\n" +
			"The parameterized method <permits>foo(permits) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_046() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> void foo(T t) {}\n"+
				"       \n"+
				"       public X() {\n"+
				"               X x = new X();\n"+
				"               x.<permits>foo(0);\n"+
				"       }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	x.<permits>foo(0);\n" +
			"	   ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	x.<permits>foo(0);\n" +
			"	           ^^^\n" +
			"The parameterized method <permits>foo(permits) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_047() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> void foo(T t) {}\n"+
				"       \n"+
				"       public X() {\n"+
				"               X x = new permits();\n"+
				"       }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	X x = new permits();\n" +
			"	          ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_048() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> void foo(T t) {}\n"+
				"       \n"+
				"       public X() {\n"+
				"               X x = new permits();\n"+
				"       }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	X x = new permits();\n" +
			"	          ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_049() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public X() {\n"+
				"               new permits() {\n"+
				"                       @Override\n"+
				"                       void foo() {}\n"+
				"               }.foo();\n"+
				"       }\n"+
				"}\n"+
				"abstract class permits {\n"+
				"       abstract void foo();\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new permits() {\n" +
			"	    ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	abstract class permits {\n" +
			"	               ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_050() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public X() {\n"+
				"       new permits() {\n"+
				"          @Override\n"+
				"          void foo() {\n"+
				"            Zork();\n"+
				"          }\n"+
				"       }.foo();\n"+
				"       }\n"+
				"}\n"+
				"abstract class permits {\n"+
				"       abstract void foo();\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new permits() {\n" +
			"	    ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type new permits(){}\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\n" +
			"	abstract class permits {\n" +
			"	               ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_051() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public X() {\n"+
				"    Object[] p = new permits[10];\n"+
			    "  }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object[] p = new permits[10];\n" +
			"	                 ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_052() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public X() {\n"+
				"    Object[] p = new permits[10];\n"+
			    "  }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object[] p = new permits[10];\n" +
			"	                 ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_053() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" public static void main(String[] args) {\n"+
				"   new X().foo((permits) null);\n"+
				" }\n"+
				" private void foo(permits o) {}\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new X().foo((permits) null);\n" +
			"	        ^^^\n" +
			"The method foo(permits) from the type X refers to the missing type permits\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	new X().foo((permits) null);\n" +
			"	             ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	private void foo(permits o) {}\n" +
			"	                 ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_054() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" public static void main(String[] args) {\n"+
				"   new X().foo((permits) null);\n"+
				" }\n"+
				" private void foo(permits o) {}\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new X().foo((permits) null);\n" +
			"	        ^^^\n" +
			"The method foo(permits) from the type X refers to the missing type permits\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	new X().foo((permits) null);\n" +
			"	             ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	private void foo(permits o) {}\n" +
			"	                 ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_055() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" private void foo(Object o) {\n"+
				"   if (o instanceof permits) {}\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	if (o instanceof permits) {}\n" +
			"	                 ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_056() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" private void foo(Object o) {\n"+
				"   if (o instanceof permits) {}\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	if (o instanceof permits) {}\n" +
			"	                 ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638_057() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" public static void main(String[] args) {\n"+
				"   @SuppressWarnings(\"unused\")\n"+
				"   I i = permits :: new;\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class permits{}\n" +
				"interface I {\n"+
				" Object gen();\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	class permits{}\n" +
			"	      ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638_058() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" public static void main(String[] args) {\n"+
				"   @SuppressWarnings(\"unused\")\n"+
				"   I i = permits :: new;\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class permits{}\n" +
				"interface I {\n"+
				" Object gen();\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	class permits{}\n" +
			"	      ^^^^^^^\n" +
			"\'permits\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_001() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class sealed {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class sealed {\n" +
			"	      ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type sealed\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class sealed {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class sealed {\n" +
			"	      ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type sealed\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_003() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  sealed p;\n" +
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
				"sealed.java",
				"public class sealed {\n"+
				"}",
			},
			"----------\n" +
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	sealed p;\n" +
			"	^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in sealed.java (at line 1)\n" +
			"	public class sealed {\n" +
			"	             ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  sealed p;\n" +
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
				"sealed.java",
				"public class sealed {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	sealed p;\n" +
			"	^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in sealed.java (at line 1)\n" +
			"	public class sealed {\n" +
			"	             ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_005() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X<sealed> {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X<sealed> {\n" +
			"	        ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X<sealed>\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X<sealed> {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X<sealed> {\n" +
			"	        ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X<sealed>\n" +
			"----------\n");
	}
	public void testBug564638b_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X extends sealed {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"class sealed {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X extends sealed {\n" +
			"	                ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	class sealed {\n" +
			"	      ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_008() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X extends sealed {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"class sealed {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X extends sealed {\n" +
			"	                ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	class sealed {\n" +
			"	      ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X implements sealed {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface sealed {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X implements sealed {\n" +
			"	                   ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	interface sealed {\n" +
			"	          ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_010() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X implements sealed {\n"+
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface sealed {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X implements sealed {\n" +
			"	                   ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	interface sealed {\n" +
			"	          ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_011() {
		runNegativeTest(
			new String[] {
				"X.java",
				"interface X extends sealed {\n"+
				"  default void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface sealed {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	interface X extends sealed {\n" +
			"	                    ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	interface sealed {\n" +
			"	          ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_012() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X extends sealed {\n"+
				"  default void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface sealed {\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	interface X extends sealed {\n" +
			"	                    ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	interface sealed {\n" +
			"	          ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_013() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X extends {\n"+
				"  sealed foo() {\n" +
				"    Zork();\n" +
				"    return null;\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X extends {\n" +
			"	        ^^^^^^^\n" +
			"Syntax error on token \"extends\", Type expected after this token\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	sealed foo() {\n" +
			"	^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_014() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  sealed foo() {\n" +
				"    Zork();\n" +
				"    return null;\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	sealed foo() {\n" +
			"	^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_015() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X  {\n"+
				"  void foo() throws sealed{\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo() throws sealed{\n" +
			"	                  ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_016() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  void foo() throws sealed{\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void foo() throws sealed{\n" +
			"	                  ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_017() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X <T extends sealed> {\n"+
				"  <T> void foo(T extends sealed) {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X <T extends sealed> {\n" +
			"	                   ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	class X <T extends sealed> {\n" +
			"	                           ^\n" +
			"Syntax error, insert \"}\" to complete ClassBody\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 2)\n" +
			"	<T> void foo(T extends sealed) {\n" +
			"	 ^\n" +
			"The type parameter T is hiding the type T\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 2)\n" +
			"	<T> void foo(T extends sealed) {\n" +
			"	               ^^^^^^^\n" +
			"Syntax error on token \"extends\", delete this token\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 5)\n" +
			"	}\n" +
			"	^\n" +
			"Syntax error on token \"}\", delete this token\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_018() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X <T extends sealed>{\n"+
				"  <T> void foo(T extends sealed) {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	class X <T extends sealed>{\n" +
			"	                   ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 1)\n" +
			"	class X <T extends sealed>{\n" +
			"	                          ^\n" +
			"Syntax error, insert \"}\" to complete ClassBody\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 2)\n" +
			"	<T> void foo(T extends sealed) {\n" +
			"	 ^\n" +
			"The type parameter T is hiding the type T\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 2)\n" +
			"	<T> void foo(T extends sealed) {\n" +
			"	               ^^^^^^^\n" +
			"Syntax error on token \"extends\", delete this token\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 5)\n" +
			"	}\n" +
			"	^\n" +
			"Syntax error on token \"}\", delete this token\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_019() {
		runNegativeTest(
			new String[] {
				"X.java",
				"enum X {\n"+
				"  ONE(1);\n" +
				"  private final sealed p;\n" +
				"  X(int p) {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	private final sealed p;\n" +
			"	              ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_020() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"enum X {\n"+
				"  ONE(1);\n" +
				"  private final sealed p;\n" +
				"  X(int p) {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	private final sealed p;\n" +
			"	              ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_021() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    I i = (sealed p)-> {};\n" +
//				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface I {\n" +
				"  void apply(Object o);\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I i = (sealed p)-> {};\n" +
			"	      ^^^^^^^^^^^^\n" +
			"This lambda expression refers to the missing type sealed\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	I i = (sealed p)-> {};\n" +
			"	       ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_022() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    I i = (sealed p)-> {};\n" +
				"    Zork();\n" +
				"  }\n" +
				"}\n" +
				"interface I {\n" +
				"  void apply(Object o);\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	I i = (sealed p)-> {};\n" +
			"	      ^^^^^^^^^^^^\n" +
			"This lambda expression refers to the missing type sealed\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	I i = (sealed p)-> {};\n" +
			"	       ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_023() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public void foo(sealed this) {}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public void foo(sealed this) {}\n" +
			"	                ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_024() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public void foo(sealed this) {}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public void foo(sealed this) {}\n" +
			"	                ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_025() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public void foo(sealed this) {}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public void foo(sealed this) {}\n" +
			"	                ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_026() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public void foo(sealed this) {}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public void foo(sealed this) {}\n" +
			"	                ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_027() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  class sealed {\n"+
				"     public void foo(sealed this) {}\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	class sealed {\n" +
			"	      ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	public void foo(sealed this) {}\n" +
			"	                ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_028() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  class sealed {\n"+
				"     public void foo(sealed this) {}\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	class sealed {\n" +
			"	      ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	public void foo(sealed this) {}\n" +
			"	                ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_029() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    sealed p;\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	sealed p;\n" +
			"	^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_030() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    sealed p;\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	sealed p;\n" +
			"	^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_031() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    for (sealed i = 0; i < 10; ++i) {} \n" +
				"  }\n" +
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	for (sealed i = 0; i < 10; ++i) {} \n" +
			"	     ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_032() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n" +
				"    for (sealed i = 0; i < 10; ++i) {} \n" +
				"  }\n" +
				"  void foo() {\n" +
				"    Zork();\n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	for (sealed i = 0; i < 10; ++i) {} \n" +
			"	     ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_033() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(sealed[] args) {\n" +
				"    for (sealed p : args) {} \n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public static void main(sealed[] args) {\n" +
			"	                        ^^^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	for (sealed p : args) {} \n" +
			"	     ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_034() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(sealed[] args) {\n" +
				"    for (sealed p : args) {} \n" +
				"  }\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public static void main(sealed[] args) {\n" +
			"	                        ^^^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	for (sealed p : args) {} \n" +
			"	     ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_035() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"               try (sealed y = new Y()) {\n"+
				"                       \n"+
				"               } catch (Exception e) {\n"+
				"                       e.printStackTrace();\n"+
				"               } finally {\n"+
				"                       \n"+
				"               }\n"+
				"       }\n"+
				"}\n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	try (sealed y = new Y()) {\n" +
			"	     ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_036() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"               try (sealed y = new Y()) {\n"+
				"                       \n"+
				"               } catch (Exception e) {\n"+
				"                       e.printStackTrace();\n"+
				"               } finally {\n"+
				"                       \n"+
				"               }\n"+
				"       }\n"+
				"}\n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	try (sealed y = new Y()) {\n" +
			"	     ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_037() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"               try (Y y = new Y()) {\n"+
				"                       \n"+
				"               } catch (sealed e) {\n"+
				"                       e.printStackTrace();\n"+
				"               } finally {\n"+
				"                       \n"+
				"               }\n"+
				"       }\n"+
				"}\n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	} catch (sealed e) {\n" +
			"	         ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_038() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public static void main(String[] args) {\n"+
				"               try (Y y = new Y()) {\n"+
				"                       \n"+
				"               } catch (sealed e) {\n"+
				"                       e.printStackTrace();\n"+
				"               } finally {\n"+
				"                       \n"+
				"               }\n"+
				"       }\n"+
				"}\n"+
				"class Y implements AutoCloseable {\n"+
				"       @Override\n"+
				"       public void close() throws Exception {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	} catch (sealed e) {\n" +
			"	         ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_039() {
		runNegativeTest(
			new String[] {
				"X.java",
				"record X(sealed p) {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	record X(sealed p) {\n" +
			"	^\n" +
			"sealed cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	record X(sealed p) {\n" +
			"	         ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_040() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"record X(sealed p) {\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	record X(sealed p) {\n" +
			"	^\n" +
			"sealed cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	record X(sealed p) {\n" +
			"	         ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_041() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> X(T t) {}\n"+
				"       \n"+
				"       public X(int t, char c) {\n"+
				"               <sealed>this(t);\n"+
				"       }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	<sealed>this(t);\n" +
			"	 ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	<sealed>this(t);\n" +
			"	        ^^^^^^^^\n" +
			"The parameterized constructor <sealed>X(sealed) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_042() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> X(T t) {}\n"+
				"       \n"+
				"       public X(int t, char c) {\n"+
				"               <sealed>this(t);\n"+
				"       }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	<sealed>this(t);\n" +
			"	 ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	<sealed>this(t);\n" +
			"	        ^^^^^^^^\n" +
			"The parameterized constructor <sealed>X(sealed) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_043() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> X(T t) {}\n"+
				"       \n"+
				"       public X(int t, char c) {\n"+
				"           new <sealed>X(t).foo();\n"+
				"       }\n"+
				"       public void foo() {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	new <sealed>X(t).foo();\n" +
			"	^^^^^^^^^^^^^^^^\n" +
			"The parameterized constructor <sealed>X(sealed) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	new <sealed>X(t).foo();\n" +
			"	     ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_044() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> X(T t) {}\n"+
				"       \n"+
				"       public X(int t, char c) {\n"+
				"           new <sealed>X(t).foo();\n"+
				"       }\n"+
				"       public void foo() {}\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	new <sealed>X(t).foo();\n" +
			"	^^^^^^^^^^^^^^^^\n" +
			"The parameterized constructor <sealed>X(sealed) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	new <sealed>X(t).foo();\n" +
			"	     ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_045() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> void foo(T t) {}\n"+
				"       \n"+
				"       public X() {\n"+
				"               X x = new X();\n"+
				"               x.<sealed>foo(0);\n"+
				"       }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	x.<sealed>foo(0);\n" +
			"	   ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	x.<sealed>foo(0);\n" +
			"	          ^^^\n" +
			"The parameterized method <sealed>foo(sealed) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_046() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> void foo(T t) {}\n"+
				"       \n"+
				"       public X() {\n"+
				"               X x = new X();\n"+
				"               x.<sealed>foo(0);\n"+
				"       }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	x.<sealed>foo(0);\n" +
			"	   ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	x.<sealed>foo(0);\n" +
			"	          ^^^\n" +
			"The parameterized method <sealed>foo(sealed) of type X is not applicable for the arguments (Integer)\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_047() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> void foo(T t) {}\n"+
				"       \n"+
				"       public X() {\n"+
				"               X x = new sealed();\n"+
				"       }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	X x = new sealed();\n" +
			"	          ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_048() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public <T> void foo(T t) {}\n"+
				"       \n"+
				"       public X() {\n"+
				"               X x = new sealed();\n"+
				"       }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	X x = new sealed();\n" +
			"	          ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_049() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"       public X() {\n"+
				"               new sealed() {\n"+
				"                       @Override\n"+
				"                       void foo() {}\n"+
				"               }.foo();\n"+
				"       }\n"+
				"}\n"+
				"abstract class sealed {\n"+
				"       abstract void foo();\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new sealed() {\n" +
			"	    ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	abstract class sealed {\n" +
			"	               ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_050() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public X() {\n"+
				"       new sealed() {\n"+
				"          @Override\n"+
				"          void foo() {\n"+
				"            Zork();\n"+
				"          }\n"+
				"       }.foo();\n"+
				"       }\n"+
				"}\n"+
				"abstract class sealed {\n"+
				"       abstract void foo();\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new sealed() {\n" +
			"	    ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type new sealed(){}\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\n" +
			"	abstract class sealed {\n" +
			"	               ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_051() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public X() {\n"+
				"    Object[] p = new sealed[10];\n"+
			    "  }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object[] p = new sealed[10];\n" +
			"	                 ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_052() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public X() {\n"+
				"    Object[] p = new sealed[10];\n"+
			    "  }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Object[] p = new sealed[10];\n" +
			"	                 ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_053() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" public static void main(String[] args) {\n"+
				"   new X().foo((sealed) null);\n"+
				" }\n"+
				" private void foo(sealed o) {}\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new X().foo((sealed) null);\n" +
			"	        ^^^\n" +
			"The method foo(sealed) from the type X refers to the missing type sealed\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	new X().foo((sealed) null);\n" +
			"	             ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	private void foo(sealed o) {}\n" +
			"	                 ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_054() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" public static void main(String[] args) {\n"+
				"   new X().foo((sealed) null);\n"+
				" }\n"+
				" private void foo(sealed o) {}\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new X().foo((sealed) null);\n" +
			"	        ^^^\n" +
			"The method foo(sealed) from the type X refers to the missing type sealed\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	new X().foo((sealed) null);\n" +
			"	             ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	private void foo(sealed o) {}\n" +
			"	                 ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_055() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" private void foo(Object o) {\n"+
				"   if (o instanceof sealed) {}\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	if (o instanceof sealed) {}\n" +
			"	                 ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_056() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" private void foo(Object o) {\n"+
				"   if (o instanceof sealed) {}\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	if (o instanceof sealed) {}\n" +
			"	                 ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug564638b_057() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" public static void main(String[] args) {\n"+
				"   @SuppressWarnings(\"unused\")\n"+
				"   I i = sealed :: new;\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class sealed{}\n" +
				"interface I {\n"+
				" Object gen();\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	class sealed{}\n" +
			"	      ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n");
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug564638b_058() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" public static void main(String[] args) {\n"+
				"   @SuppressWarnings(\"unused\")\n"+
				"   I i = sealed :: new;\n"+
				"   Zork();\n"+
				" }\n"+
				"}\n"+
				"class sealed{}\n" +
				"interface I {\n"+
				" Object gen();\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	class sealed{}\n" +
			"	      ^^^^^^\n" +
			"\'sealed\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 17\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug565561_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"public sealed class X permits Outer.Inner {\n" +
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n" +
				"class Outer{\n" +
				"   final class Inner extends X{}\n"+
				"}",
			},
			"0");
		String expectedOutput =
			"  Inner classes:\n" +
			"    [inner class info: #33 Outer$Inner, outer class info: #36 Outer\n" +
			"     inner name: #38 Inner, accessflags: 16 final]\n" +
			"\n" +
			"PermittedSubclasses:\n" +
			"   #33 Outer$Inner\n" +
			"}";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565116_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"permits/X.java",
				"package permits;\n"+
				"class X {\n"+
				"  public static void main(String[] args) {\n"+
				"    X x = new permits.X();\n"+
				"  }\n"+
				"}",
			},
			"");
	}
	public void testBug565638_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"sealed class X {\n"+
				"  public static void main(String[] args) {\n"+
				"    System.out.println(0);\n"+
				"  }\n"+
				"}\n"+
				"final class Outer {\n"+
				"    final class Inner extends X{\n"+
				"  }\n"+
				"}",
			},
			"0");
	}
	public void testBug565782_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I permits X {}\n"+
				"enum X implements I {\n"+
				"    ONE {};\n"+
				"    public static void main(String[] args) {\n"+
				"        System.out.println(0);\n"+
				"   }\n"+
				"}",
			},
			"0");
		String expectedOutput =
				"PermittedSubclasses:\n" +
				"   #14 X$1\n" +
				"}";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565782_002() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I permits X {}\n"+
				"public enum X implements I {\n"+
				"    ONE ;\n"+
				"    public static void main(String[] args) {\n"+
				"        System.out.println(0);\n"+
				"   }\n"+
				"}",
			},
			"0");
		String expectedOutput =	"public final enum X implements I {\n";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565782_003() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I {}\n"+
				"enum X implements I {\n"+
				"    ONE {};\n"+
				"    public static void main(String[] args) {\n"+
				"        System.out.println(0);\n"+
				"   }\n"+
				"}",
			},
			"0");
		String expectedOutput =
				"PermittedSubclasses:\n" +
				"   #14 X$1\n" +
				"}";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565782_004() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I {}\n"+
				"class X {\n"+
				"	enum E implements I {\n"+
				"   	ONE {};\n"+
				"	}\n"+
				"   public static void main(String[] args) {\n"+
				"      	System.out.println(0);\n"+
				"   }\n"+
				"}",
			},
			"0");
		String expectedOutput =
				"PermittedSubclasses:\n" +
				"   #14 X$E$1\n" +
				"}";
		verifyClassFile(expectedOutput, "X$E.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565782_005() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I permits X {}\n"+
				"enum X implements I {\n"+
				"    ONE {},\n"+
				"    TWO {},\n"+
				"    THREE {};\n"+
				"    public static void main(String[] args) {\n"+
				"        System.out.println(0);\n"+
				"   }\n"+
				"}",
			},
			"0");
		String expectedOutput =
				"PermittedSubclasses:\n" +
				"   #16 X$1,\n" +
				"   #25 X$2,\n" +
				"   #31 X$3\n" +
				"}";
		verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug565847_001() {
		Map<String, String> options =getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);

		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed class X  permits Y {" +
				"Zork();\n" +
				"}\n" +
				"final class  Y extends X{}\n" +
				"sealed interface I{}\n" +
				"final class Z implements I{}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed class X  permits Y {Zork();\n" +
			"	                                  ^^^^^^\n" +
			"Return type for the method is missing\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public sealed class X  permits Y {Zork();\n" +
			"	                                  ^^^^^^\n" +
			"This method requires a body instead of a semicolon\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "rawtypes" })
	public void testBug566979_001() {
		Map options = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public sealed void main(String[] args){ }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public sealed void main(String[] args){ }\n" +
			"	       ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug566979_002() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public sealed void main(String[] args){ }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public sealed void main(String[] args){ }\n" +
			"	       ^^^^^^\n" +
			"Syntax error on token \"sealed\", static expected\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "rawtypes" })
	public void testBug566980_001() {
		Map options = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public permits void main(String[] args){ }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public permits void main(String[] args){ }\n" +
			"	               ^^^^\n" +
			"Syntax error on token \"void\", delete this token\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBug566980_002() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public permits void main(String[] args){ }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public permits void main(String[] args){ }\n" +
			"	               ^^^^\n" +
			"Syntax error on token \"void\", delete this token\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	@SuppressWarnings({ "rawtypes" })
	public void testBug566846_001() {
		Map options = getCompilerOptions();
		this.runNegativeTest(
			new String[] {
				"X.java",
				"record X;\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	record X;\n" +
			"	^\n" +
			"The preview feature Implicitly Declared Classes and Instance Main Methods is only available with source level 22 and above\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	record X;\n" +
			"	^^^^^^\n" +
			"\'record\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 16\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 1)\n" +
			"	record X;\n" +
			"	^\n" +
			"Implicitly declared class must have a candidate main method\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug568428_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public void foo() {\n" +
				"        sealed interface I {}\n"+
				"    }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	sealed interface I {}\n" +
			"	                 ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
			"----------\n"
		);
	}
	public void testBug568428_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public void foo() {\n" +
				"        non-sealed interface I {}\n"+
				"    }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	non-sealed interface I {}\n" +
			"	                     ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
			"----------\n"
		);
	}
	public void testBug568514_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public void foo() {\n" +
				"        sealed enum I {}\n"+
				"    }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	sealed enum I {}\n" +
			"	            ^\n" +
			"Illegal modifier for local enum I; no explicit modifier is permitted\n" +
			"----------\n"
		);
	}
	public void testBug568514_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public void foo() {\n" +
				"        non-sealed enum I {}\n"+
				"    }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	non-sealed enum I {}\n" +
			"	                ^\n" +
			"Illegal modifier for local enum I; no explicit modifier is permitted\n" +
			"----------\n"
		);
	}
	public void testBug568758_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public sealed interface X{}\n",
				"Y.java",
				"public final class Y implements X{}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public sealed interface X{}\n" +
			"	                        ^\n" +
			"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares X as its direct superclass or superinterface\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in Y.java (at line 1)\n" +
			"	public final class Y implements X{}\n" +
			"	                                ^\n" +
			"The type Y that implements a sealed interface X should be a permitted subtype of X\n" +
			"----------\n");
	}
	public void testBug569522_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  sealed interface Foo<T> permits Bar { }\n"+
				"  final class Bar<T> implements Foo<T> { }\n"+
				"  public static void main(String[] args) {\n"+
				"       System.out.println(\"\");\n"+
				"  }\n"+
				"}",
			},
			"");
	}
	public void testBug569522_002() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				"  sealed class Foo<T> permits Bar { }\n"+
				"  final class Bar<T> extends Foo<T> { }\n"+
				"  public static void main(String[] args) {\n"+
				"       System.out.println(\"\");\n"+
				"  }\n"+
				"}",
			},
			"");
	}
	public void testBug570359_001() throws IOException, ClassFormatException {
		runConformTest(
			new String[] {
				"X.java",
				"import java.lang.reflect.Modifier;\n"+
				"\n"+
				"sealed interface I {\n"+
				" void foo();\n"+
				"}\n"+
				"\n"+
				"class Y {\n"+
				" enum E implements I {\n"+
				"   ONE() {\n"+
				"     public void foo() {\n"+
				"     }\n"+
				"   };\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"public class X {\n"+
				" public static void main(String argv[]) {\n"+
				"   Class<? extends Y.E> c = Y.E.ONE.getClass();\n"+
				"   System.out.println(c != null ? (c.getModifiers() & Modifier.FINAL) != 0 : false);\n"+
				" }\n"+
				"}",
			},
			"true");
		String expectedOutput = "final enum Y$E$1 {\n";
		SealedTypesTests.verifyClassFile(expectedOutput, "Y$E$1.class", ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
				"  Inner classes:\n" +
				"    [inner class info: #3 Y$E, outer class info: #20 Y\n" +
				"     inner name: #22 E, accessflags: 17416 abstract static],\n" +
				"    [inner class info: #1 Y$E$1, outer class info: #0\n" +
				"     inner name: #0, accessflags: 16400 final]\n" +
				"  Enclosing Method: #3  #0 Y$E\n";
		SealedTypesTests.verifyClassFile(expectedOutput, "Y$E$1.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug568854_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n"+
				"   sealed interface Foo permits A {}\n"+
				"   record A() implements Foo {}\n"+
				"   record B() implements Foo {}\n"+
				" }",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	record B() implements Foo {}\n" +
			"	                      ^^^\n" +
			"The type B that implements a sealed interface X.Foo should be a permitted subtype of X.Foo\n" +
			"----------\n");
	}
	public void testBug568854_002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" sealed interface Foo permits X.A {}\n"+
				" public class X {\n"+
				"   record A() implements Foo {}\n"+
				"   record B() implements Foo {}\n"+
				" }",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	record B() implements Foo {}\n" +
			"	                      ^^^\n" +
			"The type B that implements a sealed interface Foo should be a permitted subtype of Foo\n" +
			"----------\n");
	}
	public void testBug568854_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" sealed interface Foo permits A {}\n"+
				" record A() implements Foo {}\n"+
				" record B() implements Foo {}\n"+
				" public class X {\n"+
				" }",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	record B() implements Foo {}\n" +
			"	                      ^^^\n" +
			"The type B that implements a sealed interface Foo should be a permitted subtype of Foo\n" +
			"----------\n");
	}
	public void testBug568854_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n"+
				"   sealed interface Foo permits A {}\n"+
				"   class A implements Foo {}\n"+
				"   final class B implements Foo {}\n"+
				" }",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	class A implements Foo {}\n" +
			"	      ^\n" +
			"The class A with a sealed direct superclass or a sealed direct superinterface X.Foo should be declared either final, sealed, or non-sealed\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	final class B implements Foo {}\n" +
			"	                         ^^^\n" +
			"The type B that implements a sealed interface X.Foo should be a permitted subtype of X.Foo\n" +
			"----------\n");
	}
	public void testBug568854_005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" sealed interface Foo permits X.A {}\n"+
				" public class X {\n"+
				"   class A implements Foo {}\n"+
				"   final class B implements Foo {}\n"+
				" }",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	class A implements Foo {}\n" +
			"	      ^\n" +
			"The class A with a sealed direct superclass or a sealed direct superinterface Foo should be declared either final, sealed, or non-sealed\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	final class B implements Foo {}\n" +
			"	                         ^^^\n" +
			"The type B that implements a sealed interface Foo should be a permitted subtype of Foo\n" +
			"----------\n");
	}
	public void testBug568854_006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" sealed interface Foo permits A {}\n"+
				" class A implements Foo {}\n"+
				" final class B implements Foo {}\n"+
				" public class X {\n"+
				" }",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	class A implements Foo {}\n" +
			"	      ^\n" +
			"The class A with a sealed direct superclass or a sealed direct superinterface Foo should be declared either final, sealed, or non-sealed\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	final class B implements Foo {}\n" +
			"	                         ^^^\n" +
			"The type B that implements a sealed interface Foo should be a permitted subtype of Foo\n" +
			"----------\n");
	}
	public void testBug568854_007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I permits A {}\n"+
				"final class A implements I {}\n"+
				"enum B {\n"+
				"   ONE {\n"+
				"     class Y implements I {}\n"+
				"   }\n"+
				"}\n"+
				"public class    X {\n"+
				" public static void main(String[] args) {\n"+
				"   class Z implements I{}\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	class Y implements I {}\n" +
			"	                   ^\n" +
			"A local class Y cannot have a sealed direct superclass or a sealed direct superinterface I\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	class Z implements I{}\n" +
			"	                   ^\n" +
			"A local class Z cannot have a sealed direct superclass or a sealed direct superinterface I\n" +
			"----------\n");
	}
	public void testBug568854_008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I permits X.A {}\n"+
				"public class    X {\n"+
				"final class A implements I {}\n"+
				"enum B {\n"+
				"   ONE {\n"+
				"     class Y implements I {}\n"+
				"   }\n"+
				"}\n"+
				" public static void main(String[] args) {\n"+
				"   class Z implements I{}\n"+
				" }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	class Y implements I {}\n" +
			"	                   ^\n" +
			"A local class Y cannot have a sealed direct superclass or a sealed direct superinterface I\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	class Z implements I{}\n" +
			"	                   ^\n" +
			"A local class Z cannot have a sealed direct superclass or a sealed direct superinterface I\n" +
			"----------\n");
	}
	public void testBug571332_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"sealed interface I {\n"+
				"       void foo();\n"+
				"}\n"+
				"non-sealed interface I1 extends I {}\n"+
				"public class X {\n"+
				"    public static void main(String argv[]) {\n"+
				"        I lambda = () -> {};\n"+
				"    }\n"+
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	I lambda = () -> {};\n" +
			"	           ^^^^^\n" +
			"The target type of this expression must be a functional interface\n" +
			"----------\n");
	}
	public void testBug570605_001() {
		runNegativeTest(
				new String[] {
					"X.java",
					"sealed class Y {}\n"+
					"non-sealed class Z extends Y {}\n"+
					"public class X {\n"+
					" public void foo() {\n"+
					"        record R()  {\n"+
					"            class L extends Y {}\n"+
					"        }\n"+
					"    }\n"+
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	class L extends Y {}\n" +
				"	                ^\n" +
				"A local class L cannot have a sealed direct superclass or a sealed direct superinterface Y\n" +
				"----------\n");
	}
	public void testBug570218_001() {
		runConformTest(
			new String[] {
				"X.java",
				"interface I {}\n" +
				"sealed class A permits X {}\n"+
				"final class X extends A implements I { \n" +
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n",
			},
			"0");
	}
	public void testBug570218_002() {
		runConformTest(
			new String[] {
				"X.java",
				"sealed interface I permits X{}\n" +
				"class A  {}\n"+
				"final class X extends A implements I { \n" +
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n",
			},
			"0");
	}
	public void testBug572205_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X{\n" +
				"  public static void main(String[] args) {\n" +
				"	 class Circle implements Shape{}\n" +
				"  }\n" +
				"  sealed interface Shape {}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	class Circle implements Shape{}\n" +
			"	                        ^^^^^\n" +
			"A local class Circle cannot have a sealed direct superclass or a sealed direct superinterface X.Shape\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	sealed interface Shape {}\n" +
			"	                 ^^^^^\n" +
			"Sealed class or interface lacks the permits clause and no class or interface from the same compilation unit declares Shape as its direct superclass or superinterface\n" +
			"----------\n");
	}
	public void testBug573450_001() {
		runConformTest(
				new String[] {
					"X.java",
					"sealed interface Foo permits Foo.Bar {\n" +
					"	interface Interface {}\n" +
					"	record Bar() implements Foo, Interface { }\n" +
					"}\n" +
					"public class X { \n" +
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}",
				},
				"0");
	}

	public void testBug573450_002() {
		runConformTest(
				new String[] {
					"X.java",
					"interface Interface {}\n" +
					"sealed interface Foo extends Interface permits Foo.Bar {\n" +
					"	record Bar() implements Foo, Interface {}\n" +
					"}\n" +
					"public class X { \n" +
					"  public static void main(String[] args){\n"+
					"     System.out.println(0);\n" +
					"  }\n"+
					"}"
				},
				"0");
	}
	public void testBug573450_003() {
		runNegativeTest(
				new String[] {
					"X.java",
					"sealed interface Interface extends Foo{}\n" +
					"sealed interface Foo extends Interface permits Foo.Bar, Interface {\n" +
					"	record Bar() implements Foo, Interface {} \n" +
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	sealed interface Interface extends Foo{}\n" +
				"	                 ^^^^^^^^^\n" +
				"The hierarchy of the type Interface is inconsistent\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	sealed interface Foo extends Interface permits Foo.Bar, Interface {\n" +
				"	                             ^^^^^^^^^\n" +
				"Cycle detected: a cycle exists in the type hierarchy between Foo and Interface\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	record Bar() implements Foo, Interface {} \n" +
				"	       ^^^\n" +
				"The hierarchy of the type Bar is inconsistent\n" +
				"----------\n");
	}
	public void testBug573450_004() {
		runConformTest(
				new String[] {
					"X.java",
					"public sealed class X permits X.Y {\n" +
					"	final class Y extends X {}\n" +
					"	public static void main(String[] args){\n"+
					"		System.out.println(0);\n" +
					"	}\n"+
					"}"
				},
				"0");
	}
	public void testBug573450_005() {
		runNegativeTest(
				new String[] {
					"X.java",
					"public sealed class X permits Y {\n" +
					"	final class Y extends X {}\n" +
					"}"
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 2)\n"
				+ "	final class Y extends X {}\n"
				+ "	                      ^\n"
				+ "The type Y extending a sealed class X should be a permitted subtype of X\n"
				+ "----------\n");
	}
	public void testBug578619_1() {
		runConformTest(
				new String[] {
						"Bug578619.java",
						"public class Bug578619 {\n"
						+ "	public static void main(String[] args) {\n"
						+ "		System.out.println(\"Hola\");\n"
						+ "	}\n"
						+ "}\n"
						+ "sealed interface I1 permits I2, I3 {}\n"
						+ "non-sealed interface I2 extends I1 {}\n"
						+ "non-sealed interface I3 extends I2, I1 {}"
				},
				"Hola");
	}
	public void testBug578619_2() {
		runNegativeTest(
				new String[] {
						"Bug578619.java",
						"public class Bug578619 {\n"
						+ "	public static void main(String[] args) {\n"
						+ "		System.out.println(\"Hola\");\n"
						+ "	}\n"
						+ "}\n"
						+ "sealed interface I1 permits I2, I3 {}\n"
						+ "non-sealed interface I2 extends I1 {}\n"
						+ "non-sealed interface I3 extends I2 {}"
				},
				"----------\n" +
				"1. ERROR in Bug578619.java (at line 6)\n" +
				"	sealed interface I1 permits I2, I3 {}\n" +
				"	                                ^^\n" +
				"Permitted type I3 does not declare I1 as direct super interface \n" +
				"----------\n" +
				"2. ERROR in Bug578619.java (at line 8)\n" +
				"	non-sealed interface I3 extends I2 {}\n" +
				"	                     ^^\n" +
				"An interface I3 declared as non-sealed should have a sealed direct superinterface\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576378
	// [compiler] Wrong rawtype warning and wrong compilation of generic type reference in permits clause
	public void testBug576378() {
		runNegativeTest(
				new String[] {
						"X.java",
						"sealed interface I permits J {}\r\n" +
						"record J<T>() implements I {}\n" +
						"public class X {\n" +
						"    public static void main(String [] args) {\n" +
						"        J j; K k;\n" +
						"    }\n" +
						"}\n"
				},
				"----------\n"
				+ "1. WARNING in X.java (at line 5)\n"
				+ "	J j; K k;\n"
				+ "	^\n"
				+ "J is a raw type. References to generic type J<T> should be parameterized\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 5)\n"
				+ "	J j; K k;\n"
				+ "	     ^\n"
				+ "K cannot be resolved to a type\n"
				+ "----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576378
	// [compiler] Wrong rawtype warning and wrong compilation of generic type reference in permits clause
	public void testBug576378_2() {
		runNegativeTest(
				new String[] {
						"X.java",
						"sealed interface I permits J<Object> {}\r\n" +
						"record J<T>() implements I {}\n"
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 1)\n"
				+ "	sealed interface I permits J<Object> {}\n"
				+ "	                             ^^^^^^\n"
				+ "Type arguments are not allowed here\n"
				+ "----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576378
	// [compiler] Wrong rawtype warning and wrong compilation of generic type reference in permits clause
	public void testBug576378_3() {
		runNegativeTest(
				new String[] {
						"X.java",
						"sealed interface I permits J<Object>.K<String> {}\r\n" +
						"final class J<T> {\n" +
						"    final class K<P> implements I {}\n" +
						"}\n"
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 1)\n"
				+ "	sealed interface I permits J<Object>.K<String> {}\n"
				+ "	                             ^^^^^^\n"
				+ "Type arguments are not allowed here\n"
				+ "----------\n"
				+ "2. ERROR in X.java (at line 1)\n"
				+ "	sealed interface I permits J<Object>.K<String> {}\n"
				+ "	                                       ^^^^^^\n"
				+ "Type arguments are not allowed here\n"
				+ "----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576378
	// [compiler] Wrong rawtype warning and wrong compilation of generic type reference in permits clause
	public void testBug576378_4() {
		runNegativeTest(
				new String[] {
						"X.java",
						"sealed interface I permits J.K<String> {}\r\n" +
						"final class J<T> {\n" +
						"    final static class K<P> implements I {}\n" +
						"}\n"
				},
				"----------\n"
				+ "1. ERROR in X.java (at line 1)\n"
				+ "	sealed interface I permits J.K<String> {}\n"
				+ "	                               ^^^^^^\n"
				+ "Type arguments are not allowed here\n"
				+ "----------\n");
	}
}
