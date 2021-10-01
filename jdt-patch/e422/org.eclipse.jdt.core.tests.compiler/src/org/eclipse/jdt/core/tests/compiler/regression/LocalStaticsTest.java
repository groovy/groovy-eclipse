/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
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

public class LocalStaticsTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug569444_001"};
	}

	public static Class<?> testClass() {
		return LocalStaticsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}
	public LocalStaticsTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16); // FIXME
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
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
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, null);
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
		runner.runWarningTest();
	}

	private static void verifyClassFile(String expectedOutput, String classFileName)
			throws IOException, ClassFormatException {
		String result = getClassfileContent(classFileName);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
			System.out.println("...");
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}
	private static String getClassfileContent(String classFileName) throws IOException, ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.SYSTEM);
		return result;
	}

	public void testBug566284_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo() {\n"+
				"   interface F {\n"+
				"     static int create(int lo) {\n"+
				"       I myI = s -> lo;\n"+
				"       return myI.bar(0);\n"+
				"     }\n"+
				"   }\n"+
				"   System.out.println(F.create(0));\n"+
				"     }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"0");
	}


	public void testBug566284_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo() {\n"+
				"   record R() {\n"+
				"     static int create(int lo) {\n"+
				"       I myI = s -> lo;\n"+
				"       return myI.bar(0);\n"+
				"     }\n"+
				"   }\n"+
				"   System.out.println(R.create(0));\n"+
				"     }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"0");
	}
	public void testBug566284_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" static int si;\n"+
				" int nsi;\n"+
				"\n"+
				" void m() {\n"+
				"   int li;\n"+
				"\n"+
				"   interface F {\n"+
				"     static int fi = 0;\n"+
				"\n"+
				"     default void foo(int i) {\n"+
				"       System.out.println(li); // error, local variable of method of outer enclosing class\n"+
				"       System.out.println(nsi); // error, non-static member\n"+
				"       System.out.println(fi); // ok, static member of current class\n"+
				"       System.out.println(si); // ok, static member of enclosing class\n"+
				"       System.out.println(i); // ok, local variable of current method\n"+
				"     }\n"+
				"\n"+
				"     static void bar(int lo) {\n"+
				"       int k = lo; // ok\n"+
				"       int j = fi; // ok\n"+
				"       I myI = s -> lo; // ok, local var of method\n"+
				"     }\n"+
				"\n"+
				"     static void bar2(int lo) {\n"+
				"       I myI = s -> li; // error - local var of outer class\n"+
				"     }\n"+
				"   }\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	System.out.println(li); // error, local variable of method of outer enclosing class\n" +
			"	                   ^^\n" +
			"Cannot make a static reference to the non-static variable li\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 13)\n" +
			"	System.out.println(nsi); // error, non-static member\n" +
			"	                   ^^^\n" +
			"Cannot make a static reference to the non-static field nsi\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 26)\n" +
			"	I myI = s -> li; // error - local var of outer class\n" +
			"	             ^^\n" +
			"Cannot make a static reference to the non-static variable li\n" +
			"----------\n"
			);
	}

	public void testBug566518_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo() {\n"+
				"   int f = switch (5) {\n"+
				"			case 5: {\n"+
				"				interface I{\n"+
				"					\n"+
				"				}\n"+
				"				class C implements I{\n"+
				"					public int j = 5;\n"+
				"				}\n"+
				"				\n"+
				"				yield new C().j;\n"+
				"			}\n"+
				"			default:\n"+
				"				throw new IllegalArgumentException(\"Unexpected value: \" );\n"+
				"			};\n"+
				"	System.out.println(f);\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}"
			},
			"5");
	}

	public void testBug566518_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" static void foo() {\n"+
				"   class F {\n"+
				"     int create(int lo) {\n"+
				"       I myI = s -> lo;\n"+
				"       return myI.bar(0);\n"+
				"     }\n"+
				"   }\n"+
				"   System.out.println(new F().create(0));\n"+
				"     }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"0");
	}

	public void testBug566518_003() {
		runConformTest(
			new String[] {
				"X.java",
				"public interface X {\n"+
				" static void foo() {\n"+
				"   class F {\n"+
				"     int create(int lo) {\n"+
				"       I myI = s -> lo;\n"+
				"       return myI.bar(0);\n"+
				"     }\n"+
				"   }\n"+
				"   System.out.println(new F().create(0));\n"+
				"     }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"0");
	}

	public void testBug566518_004() {
		runConformTest(
			new String[] {
				"X.java",
				"public interface X {\n"+
				" static void foo() {\n"+
				"   interface F {\n"+
				"     static int create(int lo) {\n"+
				"       I myI = s -> lo;\n"+
				"       return myI.bar(0);\n"+
				"     }\n"+
				"   }\n"+
				"   System.out.println(F.create(0));\n"+
				"     }\n"+
				" public static void main(String[] args) {\n"+
				"   X.foo();\n"+
				" }\n"+
				"}\n"+
				"\n"+
				"interface I {\n"+
				" int bar(int l);\n"+
				"}"
			},
			"0");
	}

	public void testBug566518_005() {
		runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					" static void foo() {\n"+
					"   int f = switch (5) {\n"+
					"			case 5: {\n"+
					"				interface I{\n"+
					"					\n"+
					"				}\n"+
					"				class C implements I{\n"+
					"					public int j = 5;\n"+
					"				}\n"+
					"				\n"+
					"				yield new C().j;\n"+
					"			}\n"+
					"			default:\n"+
					"				throw new IllegalArgumentException(\"Unexpected value: \" );\n"+
					"			};\n"+
					"	System.out.println(f);\n"+
					"	class C1 implements I{\n"+
					"		public int j = 5;\n"+
					"	}\n"+
					" }\n"+
					" public static void main(String[] args) {\n"+
					"   X.foo();\n"+
					" }\n"+
					"}\n"
			},
			"----------\n"+
			"1. ERROR in X.java (at line 18)\n"+
			"	class C1 implements I{\n"+
			"	                    ^\n" +
		    "I cannot be resolved to a type\n"+
		 	"----------\n"
			);
	}

	public void testBug566518_006() {
		runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n"+
				" A, B, C;\n"+
				" public void foo() {\n"+
				"   int f = switch (5) {\n"+
				"			case 5: {\n"+
				"				interface I{\n"+
				"					\n"+
				"				}\n"+
				"				class C implements I{\n"+
				"					public int j = 5;\n"+
				"				}\n"+
				"				\n"+
				"				yield new C().j;\n"+
				"			}\n"+
				"			default:\n"+
				"				throw new IllegalArgumentException(\"Unexpected value: \" );\n"+
				"			};\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   X x = X.A;\n"+
				"	System.out.println();\n"+
				" }\n"+
				"}"
			},
			"");
	}
	// 6.5.5.1
	public void testBug566715_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				" static void foo() {\n"+
				"	interface I {\n"+
				"		X<T> supply();\n"+
				"	}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n"+
			"1. WARNING in X.java (at line 3)\n" +
			"	interface I {\n" +
			"	          ^\n" +
			"The type I is never used locally\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	X<T> supply();\n" +
			"	  ^\n" +
			"Cannot make a static reference to the non-static type T\n" +
		 	"----------\n"
			);
	}
	// 6.5.5.1
	public void testBug566715_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	interface I {\n"+
				"		X<T> supply();\n"+
				"	}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n"+
			"1. WARNING in X.java (at line 3)\n" +
			"	interface I {\n" +
			"	          ^\n" +
			"The type I is never used locally\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	X<T> supply();\n" +
			"	  ^\n" +
			"Cannot make a static reference to the non-static type T\n" +
		 	"----------\n"
			);
	}
	// 6.5.5.1
	public void testBug566715_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	record R(X<T> x) {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	record R(X<T> x) {}\n" +
			"	           ^\n" +
			"Cannot make a static reference to the non-static type T\n" +
		 	"----------\n"
			);
	}
	// 9.1.1/14.3
	public void testBug566720_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	public interface I {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	public interface I {}\n" +
			"	                 ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
		 	"----------\n"
			);
	}
	// 9.1.1/14.3
	public void testBug566720_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	private interface I {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	private interface I {}\n" +
			"	                  ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
		 	"----------\n"
			);
	}
	// 9.1.1
	public void testBug566720_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	protected interface I {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	protected interface I {}\n" +
			"	                    ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
		 	"----------\n"
			);
	}
	// 9.1.1
	public void testBug566720_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  void foo() {\n"+
				"	final interface I {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	final interface I {}\n" +
			"	                ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
		 	"----------\n"
			);
	}
	// 9.1.1
	public void testBug566720_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n"+
				"  public static void main(String[] args) {\n"+
				"	static interface I {}\n"+
				" }\n"+
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	static interface I {}\n" +
			"	                 ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
			"----------\n");
	}
	public void testBug566748_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X<T> {\n"+
				"        int count;\n"+
				"        void doNothing() {}\n"+
				"     void foo1(String s) {\n"+
				"        int i;\n"+
				"       interface I {\n"+
				"               default X<T> bar() {\n"+
				"                       if (count > 0 || i > 0 || s == null)\n"+
				"                               return null;\n"+
				"                       doNothing();\n"+
				"                               return null;\n"+
				"               }\n"+
				"       } \n"+
				"    }\n"+
				"     void foo2(String s) {\n"+
				"       try {\n"+
				"               throw new Exception();\n"+
				"       } catch (Exception e) {\n"+
				"               interface I {\n"+
				"                       default int bar() {\n"+
				"                         return e != null ? 0 : 1;\n"+
				"                       }\n"+
				"               } \n"+
				"               \n"+
				"       }\n"+
				"    }\n"+
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 6)\n" +
			"	interface I {\n" +
			"	          ^\n" +
			"The type I is never used locally\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	default X<T> bar() {\n" +
			"	          ^\n" +
			"Cannot make a static reference to the non-static type T\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	if (count > 0 || i > 0 || s == null)\n" +
			"	    ^^^^^\n" +
			"Cannot make a static reference to the non-static field count\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	if (count > 0 || i > 0 || s == null)\n" +
			"	                 ^\n" +
			"Cannot make a static reference to the non-static variable i\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 8)\n" +
			"	if (count > 0 || i > 0 || s == null)\n" +
			"	                          ^\n" +
			"Cannot make a static reference to the non-static variable s\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 10)\n" +
			"	doNothing();\n" +
			"	^^^^^^^^^\n" +
			"Cannot make a static reference to the non-static method doNothing() from the type X<T>\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 19)\n" +
			"	interface I {\n" +
			"	          ^\n" +
			"The type I is never used locally\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 21)\n" +
			"	return e != null ? 0 : 1;\n" +
			"	       ^\n" +
			"Cannot make a static reference to the non-static variable e\n" +
		 	"----------\n"
			);
	}
	public void testBug566748_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"interface X<T> {\n"+
				" int count = 0;\n"+
				"\n"+
				" default void doNothing() {}\n"+
				"\n"+
				" default void foo1(String s) {\n"+
				"   int i;\n"+
				"   interface I {\n"+
				"     default X<T> bar() {\n"+
				"       if (count > 0 || i > 0 || s == null)\n"+
				"         return null;\n"+
				"       doNothing();\n"+
				"       return null;\n"+
				"     }\n"+
				"   }\n"+
				" }\n"+
				"\n"+
				" default void foo2(String s) {\n"+
				"       try {\n"+
				"               throw new Exception();\n"+
				"       } catch (Exception e) {\n"+
				"               interface I { \n"+
				"                       default int bar() {\n"+
				"                         return e != null ? 0 : 1;\n"+
				"                       }   \n"+
				"               }   \n"+
				"                   \n"+
				"       }   \n"+
				"    }\n"+
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	interface I {\n" +
			"	          ^\n" +
			"The type I is never used locally\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	default X<T> bar() {\n" +
			"	          ^\n" +
			"Cannot make a static reference to the non-static type T\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	if (count > 0 || i > 0 || s == null)\n" +
			"	                 ^\n" +
			"Cannot make a static reference to the non-static variable i\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 10)\n" +
			"	if (count > 0 || i > 0 || s == null)\n" +
			"	                          ^\n" +
			"Cannot make a static reference to the non-static variable s\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 12)\n" +
			"	doNothing();\n" +
			"	^^^^^^^^^\n" +
			"Cannot make a static reference to the non-static method doNothing() from the type X<T>\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 22)\n" +
			"	interface I { \n" +
			"	          ^\n" +
			"The type I is never used locally\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 24)\n" +
			"	return e != null ? 0 : 1;\n" +
			"	       ^\n" +
			"Cannot make a static reference to the non-static variable e\n" +
		 	"----------\n"
			);
	}
	// 9.6
	public void testBug564557AnnotInterface_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" void foo() {\n"+
				"   class I {\n"+
				"     @interface Annot {\n"+
				"     }\n"+
				"   }\n"+
				" }\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	@interface Annot {\n" +
			"	           ^^^^^\n" +
			"The member annotation Annot can only be defined inside a top-level class or interface or in a static context\n" +
		 	"----------\n"
			);
	}
	// 9.6
	public void testBug564557AnnotInterface_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" void foo() {\n"+
				"   interface I {\n"+
				"     @interface Annot {\n"+
				"     }\n"+
				"   }\n"+
				" }\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	@interface Annot {\n" +
			"	           ^^^^^\n" +
			"The member annotation Annot can only be defined inside a top-level class or interface or in a static context\n" +
		 	"----------\n"
			);
	}
	// 9.4 && 15.12.3
	public void testBug564557MethodInvocation_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" void foo() {\n"+
				"   Zork();\n"+
				"   interface I {\n"+
				"     default void bar() {}\n"+
				"     default void b1() {\n"+
				"       class J {\n"+
				"          void jb2() {\n"+
				"           bar();\n"+
				"         }\n"+
				"       }\n"+
				"     }\n"+
				"   }\n"+
				" }\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
		 	"----------\n"
			);
	}
	// 9.4 && 15.12.3
	public void testBug564557MethodInvocation_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" void foo() {\n"+
				"   interface I {\n"+
				"     default void bar() {}\n"+
				"     default void b1() {\n"+
				"       interface J {\n"+
				"          default void jb2() {\n"+
				"           bar();\n"+
				"         }\n"+
				"       }\n"+
				"     }\n"+
				"   }\n"+
				" }\n"+
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	interface I {\n" +
			"	          ^\n" +
			"The type I is never used locally\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	default void b1() {\n" +
			"	             ^^^^\n" +
			"The method b1() from the type I is never used locally\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 6)\n" +
			"	interface J {\n" +
			"	          ^\n" +
			"The type J is never used locally\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	bar();\n" +
			"	^^^\n" +
			"Cannot make a static reference to the non-static method bar() from the type I\n" +
		 	"----------\n"
			);
	}
	// 13.1
	public void testBug564557BinaryForm_005() throws Exception {
		runConformTest(
			new String[] {
					"X.java",
					"class X {\n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(\"\");\n"+
					" }\n"+
					" void foo() {\n"+
					"   interface I {\n"+
					"   }\n"+
					" }\n"+
					"}"
			},
			"");
		String expectedOutput = "abstract static interface X$1I {\n";
		LocalStaticsTest.verifyClassFile(expectedOutput, "X$1I.class");
	}
	// 14.3 for enum
	public void testBug564557BinaryForm_006() throws Exception {
		runConformTest(
			new String[] {
					"X.java",
					"class X {\n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(\"\");\n"+
					" }\n"+
					" void foo() {\n"+
					"   enum I {\n"+
					"   }\n"+
					" }\n"+
					"}"
			},
			"");
		String expectedOutput = "static final enum X$1I {\n";
		LocalStaticsTest.verifyClassFile(expectedOutput, "X$1I.class");
	}
	// 15.8.3
	public void testBug564557thisInStatic_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" void foo() {\n"+
				"   interface I {\n"+
				"     int count = 0;\n"+
				"     static void bar() {\n"+
				"       int i = this.count;\n"+
				"     }\n"+
				"   }\n"+
				" }\n"+
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	interface I {\n" +
			"	          ^\n" +
			"The type I is never used locally\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	int count = 0;\n" +
			"	    ^^^^^\n" +
			"The value of the field I.count is not used\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	int i = this.count;\n" +
			"	        ^^^^\n" +
			"Cannot use this in a static context\n" +
		 	"----------\n"
			);
	}
	// 15.8.3
	public void testBug564557thisInStatic_008() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" int count = 0;\n"+
				" void foo() {\n"+
				"   interface I {\n"+
				"     static void bar() {\n"+
				"       int i = X.this.count;\n"+
				"     }\n"+
				"   }\n"+
				" }\n"+
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	interface I {\n" +
			"	          ^\n" +
			"The type I is never used locally\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	int i = X.this.count;\n" +
			"	        ^^^^^^\n" +
			"No enclosing instance of the type X is accessible in scope\n" +
		 	"----------\n"
			);
	}
	public void testBug568514LocalEnums_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public void foo() {\n" +
				"        public enum I {}\n"+
				"    }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	public enum I {}\n" +
			"	            ^\n" +
			"Illegal modifier for local enum I; no explicit modifier is permitted\n" +
			"----------\n"
		);
	}
	public void testBug568514LocalEnums_002() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public void foo() {\n" +
				"        public enum I {}\n"+
				"    }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	public enum I {}\n" +
			"	            ^\n" +
			"Illegal modifier for local enum I; no explicit modifier is permitted\n" +
			"----------\n",
			null,
			true,
			options
		);
	}
	public void testBug568514LocalEnums_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public void foo() {\n" +
				"        public enum I {}\n"+
				"    Zork;\n"+
				"    }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Zork;\n" +
			"	^^^^\n" +
			"Syntax error, insert \"VariableDeclarators\" to complete LocalVariableDeclaration\n" +
			"----------\n"
		);
	}

	public void testBug568514LocalEnums_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"    public void foo() {\n" +
				"        public strictfp enum I {}\n"+
				"    Zork;\n"+
				"    }\n"+
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Zork;\n" +
			"	^^^^\n" +
			"Syntax error, insert \"VariableDeclarators\" to complete LocalVariableDeclaration\n" +
			"----------\n"
		);
	}

	public void testBug566579_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private String I=null;\n"+
				" public void foo() {\n"+
				"   int f = switch (5) {\n"+
				"			case 5: {\n"+
				"				interface I{\n"+
				"					public int getVal();\n"+
				"				}\n"+
				"				class C implements I{\n"+
				"					private int j=5;\n"+
				"					@Override\n"+
				"					public int getVal() {\n"+
				"						return j;\n"+
				"					}\n"+
				"				}\n"+
				"				\n"+
				"				I abc= new C();"+
				"				yield abc.getVal();\n"+
				"			}\n"+
				"			default:\n"+
				"				yield (I==null ? 0 : I.length());\n"+
				"			};\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   X x = new X();\n"+
				"   x.I = \"abc\";\n"+
				"	System.out.println();\n"+
				" }\n"+
				"}"
			},
			"");
	}
	public void testBug566579_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	public void main5(int i) {\n"+
				"		interface i{\n"+
				"			public static int i=0;\n"+
				"		}\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println();\n"+
				"	}\n"+
				"}"
			},
			"");
	}
	public void testBug566579_003() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	public void main5() {\n"+
				"		int i=10;\n"+
				"		interface i{\n"+
				"			public static int i=0;\n"+
				"		}\n"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println();\n"+
				"	}\n"+
				"}"
			},
			"");
	}
	public void testBug566579_004() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	public void main5() {\n"+
				"		try {\n"+
				"			int i=10;\n"+
				"		} catch(NullPointerException npe) {\n"+
				"			interface i{\n"+
				"				public static int npe=0;\n"+
				"			}\n"+
				"		}"+
				"	}\n"+
				"	public static void main(String[] args) {\n"+
				"		System.out.println();\n"+
				"	}\n"+
				"}"
			},
			"");
	}
	public void testBug569444_001() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(\"hello\");\n"+
				"   class Y{\n"+
				"     static int field;\n"+
				"     public static void foo() {}\n"+
				"   }\n"+
				"   record R() {}\n"+
				" }\n"+
				" class Z {\n"+
				"   static int f2;\n"+
				"   static {};\n"+
				" }\n"+
				"}"
			},
			"hello");
	}
	public void testBug569444_002() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" private void foo() {\n"+
				"   class Y {\n"+
				"     static record R() {}\n"+
				"     static class Z{}\n"+
				"     interface I{}\n"+
				"     static interface II{}\n"+
				"   }\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(\"hello\");\n"+
				" }\n"+
				"}"
			},
			"hello");
	}
	public void testBug569444_003() {
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public void foo() {\n"+
				"   @SuppressWarnings(\"unused\")\n"+
				"   class Y {\n"+
				"     static record R() {}\n"+
				"      class Z{\n"+
				"       static class zz{}\n"+
				"     }\n"+
				"     interface I{\n"+
				"       abstract int bar();\n"+
				"     }\n"+
				"   }\n"+
				"    new Y.I() {\n"+
				"     @Override\n"+
				"     public int bar() {\n"+
				"       return 0;\n"+
				"     }\n"+
				"     \n"+
				"   };\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(\"hello\");\n"+
				" }\n"+
				"}"
			},
			"hello");
	}
	public void testBug569444_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public void foo() {\n"+
				"    @SuppressWarnings(\"unused\")\n"+
				"    static class zzz{}\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(\"hello\");\n"+
				" }\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	static class zzz{}\n" +
			"	             ^^^\n" +
			"Illegal modifier for the local class zzz; only abstract or final is permitted\n" +
			"----------\n");
	}
	public void testBug569444_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public void foo() {\n"+
				"     static class Z{} //  static not allowed\n"+
				"     class Y{\n"+
				"       static class ZZ{} // static allowed\n"+
				"     }\n"+
				"   static record R() {} // explicit static not allowed\n"+
				"   static interface I {} // explicit static not allowed\n"+
				"    }\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	static class Z{} //  static not allowed\n" +
			"	             ^\n" +
			"Illegal modifier for the local class Z; only abstract or final is permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	static record R() {} // explicit static not allowed\n" +
			"	              ^\n" +
			"A local class or interface R is implicitly static; cannot have explicit static declaration\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	static interface I {} // explicit static not allowed\n" +
			"	                 ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
			"----------\n");
	}
	public void testBug569444_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n"+
				" public void foo() {\n"+
				"   for (;;) {\n"+
				"     static class Y  {}\n"+
				"     static record R() {}\n"+
				"     static interface I{}\n"+
				"     break;\n"+
				"   }\n"+
				"    }\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	static class Y  {}\n" +
			"	             ^\n" +
			"Illegal modifier for the local class Y; only abstract or final is permitted\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	static record R() {}\n" +
			"	              ^\n" +
			"A local class or interface R is implicitly static; cannot have explicit static declaration\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	static interface I{}\n" +
			"	                 ^\n" +
			"Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly \n" +
			"----------\n");
	}
	public void testBug571163_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				" class X {\n"+
				"    public void foo() {\n"+
				"        class Y {\n"+
				"            static Y y;\n"+
				"             static {\n"+
				"                y = Y.this;\n"+
				"            }\n"+
				"            class Z {\n"+
				"                static Y yy;\n"+
				"                static {\n"+
				"                       yy = Y.this; //error not flagged here\n"+
				"                }\n"+
				"            }\n"+
				"        } \n"+
				"     }\n"+
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	y = Y.this;\n" +
			"	    ^^^^^^\n" +
			"Cannot use this in a static context\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	yy = Y.this; //error not flagged here\n" +
			"	     ^^^^^^\n" +
			"Cannot use this in a static context\n" +
			"----------\n");
	}
	public void testBug571300_001() {
		runConformTest(
			new String[] {
				"X.java",
				"class X {\n"+
				"  public static void main(String[] args) {\n"+
				"   new X().foo();  \n"+
				" }\n"+
				" public void foo() {\n"+
				"   interface I {\n"+
				"     class Z {}\n"+
				"   }\n"+
				"    I.Z z = new I.Z() { // error flagged incorrectly\n"+
				"     public String toString() {\n"+
				"       return \"I.Z\";\n"+
				"     }\n"+
				"    };\n"+
				"    System.out.println(z.toString());\n"+
				"  }\n"+
				"}"
			},
			"I.Z");
	}
	public void testBug571274_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n"+
				" void m() {\n"+
				"   interface Y<T> {\n"+
				"     class Z {\n"+
				"        T foo() {// T should not be allowed\n"+
				"         return null;\n"+
				"       }\n"+
				"     }\n"+
				"   }\n"+
				" }\n"+
				" }"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	interface Y<T> {\n" +
			"	          ^\n" +
			"The type Y<T> is never used locally\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	class Z {\n" +
			"	      ^\n" +
			"The type Y<T>.Z is never used locally\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	T foo() {// T should not be allowed\n" +
			"	^\n" +
			"Cannot make a static reference to the non-static type T\n" +
			"----------\n");
	}
	public void testBug566774_001() {
		runNegativeTest(
				new String[] {
					"X.java",
					"class X {\n"+
					" static String a;\n"+
					" String b;\n"+
					" static String concat() {\n"+
					"        return a + b;\n"+
					" }\n"+
					" }"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	return a + b;\n" +
				"	           ^\n" +
				"Cannot make a static reference to the non-static field b\n" +
				"----------\n");
	}
	public void testBug566774_002() {
		runNegativeTest(
				new String[] {
					"X.java",
					"class X {\n"+
					" static String a;\n"+
					" String b;\n"+
					" int index() {\n"+
					"     interface I {\n"+
					"		class Matcher {\n" +
					"			void check() {\n" +
					"				if (a == null || b == null) {\n" +
					"					throw new IllegalArgumentException();\n" +
					"				}\n" +
					"			}\n" +
					"		}\n" +
					"	   }\n" +
					"	I.Matcher matcher = new I.Matcher();\n" +
					"	matcher.check();\n" +
					"	return 0;\n" +
					" }\n"+
					" }"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	if (a == null || b == null) {\n" +
				"	                 ^\n" +
				"Cannot make a static reference to the non-static field b\n" +
				"----------\n");
	}

	public void testBug566774_003() {
		runNegativeTest(
				new String[] {
					"X.java",
					"class X {\n"+
					" public static void main(String[] args) {\n"+
					" 	class Checker1 {\n"+
					" 		void checkWhitespace(int x) {\n"+
					"     		String arg = args[x];\n"+
					"			if (!arg.trim().equals(arg)) {\n" +
					"				throw new IllegalArgumentException();\n" +
					"			}\n" +
					"		}\n" +
					"	}\n" +
					"	final Checker1 c1 = new Checker1();\n" +
					"	for (int i = 1; i < args.length; i++) {\n" +
					"		Runnable r = () -> {\n" +
					"			c1.checkWhitespace(i);\n" +
					"		};\n" +
					"	}\n" +
					" }\n"+
					" }"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 14)\n" +
				"	c1.checkWhitespace(i);\n" +
				"	                   ^\n" +
				"Local variable i defined in an enclosing scope must be final or effectively final\n" +
				"----------\n");
	}
	public void testBug566774_004() {
		runNegativeTest(
				new String[] {
						"X.java",
						"class X {\n"+
						" public static void main(String[] args) {\n"+
						" 	interface I {\n"+
						" 		class Checker2 {\n"+
						" 			void checkFlag(int x) {\n"+
						"     			String arg = args[x];\n"+
						"				if (!arg.startsWith(\"-\")) {\n" +
						"					throw new IllegalArgumentException();\n" +
						"				}\n" +
						"			}\n" +
						"		}\n" +
						"	}\n" +
						"	I.Checker2 c2 = new I.Checker2();\n" +
						"	for (int i = 1; i < args.length; i++) {\n" +
						"		Runnable r = () -> {\n" +
						"			c2.checkFlag(i);\n" +
						"		};\n" +
						"	}\n" +
						" }\n"+
						" }"
					},
					"----------\n" +
					"1. ERROR in X.java (at line 6)\n" +
					"	String arg = args[x];\n" +
					"	             ^^^^\n" +
					"Cannot make a static reference to the non-static variable args\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 16)\n" +
					"	c2.checkFlag(i);\n" +
					"	             ^\n" +
					"Local variable i defined in an enclosing scope must be final or effectively final\n" +
					"----------\n");
	}
	public void testBug572994_001() {
		runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"\n"+
					" public class Singleton {\n"+
					"   private static Singleton pinstance = new Singleton();\n"+
					"   public static Singleton instance() {\n"+
					"     return pinstance;\n"+
					"   }\n"+
					"   public String message() {\n"+
					"     return \"Hello world!\";\n"+
					"   }\n"+
					" }\n"+
					" \n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(Singleton.instance().message());\n"+
					" }\n"+
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	private static Singleton pinstance = new Singleton();\n" +
				"	                                     ^^^^^^^^^^^^^^^\n" +
				"No enclosing instance of type X is accessible. Must qualify the allocation with an enclosing instance of type X (e.g. x.new A() where x is an instance of X).\n" +
				"----------\n");
	}
	public void testBug572994_002() {
		runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"\n"+
					" public class Singleton {\n"+
					"   private static Singleton pinstance = this;\n"+
					"   public static Singleton instance() {\n"+
					"     return pinstance;\n"+
					"   }\n"+
					"   public String message() {\n"+
					"     return \"Hello world!\";\n"+
					"   }\n"+
					" }\n"+
					" \n"+
					" public static void main(String[] args) {\n"+
					"   System.out.println(Singleton.instance().message());\n"+
					" }\n"+
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	private static Singleton pinstance = this;\n" +
				"	                                     ^^^^\n" +
				"Cannot use this in a static context\n" +
				"----------\n");
	}
	public void testBug572994_003() {
		runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n"+
					"\n"+
					" public class Singleton {\n"+
					"   private static Y pinstance = new Y();\n"+
					"   public static Y instance() {\n"+
					"     return pinstance;\n"+
					"   }\n"+
					"   public String message() {\n"+
					"     return \"Hello world!\";\n"+
					"   }\n"+
					" }\n"+
					" \n"+
					" public static void main(String[] args) {\n"+
					" }\n"+
					" class Y {}\n"+
					"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	private static Y pinstance = new Y();\n" +
				"	                             ^^^^^^^\n" +
				"No enclosing instance of type X is accessible. Must qualify the allocation with an enclosing instance of type X (e.g. x.new A() where x is an instance of X).\n" +
				"----------\n");
	}
	// Test that static field inside inner types are properly initialized
	public void testBug574791_1() {
		runConformTest(
			new String[] {
					"EnumTester.java",
					"public class EnumTester {\n"
					+ "	public static void main(String[] args) {\n"
					+ "		Test e = Test.ONE;\n"
					+ "		System.out.println(e.value());\n"
					+ "		System.out.println(MyTest.TWO.value());\n"
					+ "		I TWO = new I() {\n"
					+ "			private static final String value = getString();\n"
					+ "			@Override\n"
					+ "			public String value() {\n"
					+ "				return value;\n"
					+ "			}\n"
					+ "		};\n"
					+ "		System.out.println(TWO.value());\n"
					+ "	}\n"
					+ "	private static String getString() {\n"
					+ "		return \"Hi from EnumTester\";\n"
					+ "	}\n"
					+ "	class MyTest {\n"
					+ "		public static final String value = getString();\n"
					+ "		private static String getString() {\n"
					+ "			return \"Hi from MyTest\";\n"
					+ "		}\n"
					+ "		public static I TWO = new I() {\n"
					+ "			private static final String value = getString();\n"
					+ "			@Override\n"
					+ "			public String value() {\n"
					+ "				return value;\n"
					+ "			}\n"
					+ "		};\n"
					+ "	}\n"
					+ "	interface I {\n"
					+ "		public String value();\n"
					+ "	}\n"
					+ "}\n"
					+ "enum Test {\n"
					+ "	ONE {\n"
					+ "		private static final String value = getString();\n"
					+ "		@Override\n"
					+ "		String value() {\n"
					+ "			return value;\n"
					+ "		}\n"
					+ "	};\n"
					+ "	abstract String value();\n"
					+ "	private static String getString() {\n"
					+ "		return \"Hi from Test\";\n"
					+ "	}\n"
					+ "}"
				},
			"Hi from Test\n" +
			"Hi from MyTest\n" +
			"Hi from EnumTester");
	}
	// Test that the static initializer is generated only when required
	// i.e., when the (anonymous) inner class contains a static field
	public void testBug574791_2() throws Exception {
		runConformTest(
			new String[] {
					"X.java",
					"public class X {\n"
					+ "	public static void main(String[] args) {\n"
					+ " }\n"
					+ "}\n"
					+ "enum Test {\n"
					+ "	ONE {\n"
					+ "		private static final String value = getString();\n"
					+ "		@Override\n"
					+ "		String value() {\n"
					+ "			return value;\n"
					+ "		}\n"
					+ "	},\n"
					+ "	TWO {\n"
					+ "		String value() {\n"
					+ "			return \"TWO\";\n"
					+ "		}\n"
					+ "	},\n"
					+ "	;\n"
					+ "	abstract String value();\n"
					+ "	private static String getString() {\n"
					+ "		return \"default\";\n"
					+ "	}\n"
					+ "}"
				},
			"");
			String expectedOutput =
					"  // Method descriptor #17 ()V\n"
					+ "  // Stack: 1, Locals: 0\n"
					+ "  static {};\n"
					+ "    0  invokestatic Test.getString() : java.lang.String [18]\n"
					+ "    3  putstatic Test$1.value : java.lang.String [22]\n"
					+ "    6  return";
			String content = getClassfileContent("Test$1.class");
			assertTrue("Expected code not found", content.indexOf(expectedOutput) != -1);
			expectedOutput = "  static {};";
			content = getClassfileContent("Test$2.class");
			assertTrue("Unexpected code found", content.indexOf(expectedOutput) == -1);
	}
	public void testBug574791_3() {
		runConformTest(
			new String[] {
					"EnumTester.java",
					"public class EnumTester {\n"
					+ "	public static void main(String[] args) {\n"
					+ "		Test e = Test.ONE;\n"
					+ "		System.out.println(e.value());\n"
					+ "		System.out.println(MyTest.TWO.value());\n"
					+ "		I TWO = new I() {\n"
					+ "			private static final String value = getString();\n"
					+ "			@Override\n"
					+ "			public String value() {\n"
					+ "				return value;\n"
					+ "			}\n"
					+ "		};\n"
					+ "		System.out.println(TWO.value());\n"
					+ "	}\n"
					+ "	private static String getString() {\n"
					+ "		return \"Hi from EnumTester\";\n"
					+ "	}\n"
					+ "	class MyTest {\n"
					+ "		public static String value;\n"
					+ "     static {\n"
					+ "       value = getString();\n"
					+ "     }\n"
					+ "		private static String getString() {\n"
					+ "			return \"Hi from MyTest\";\n"
					+ "		}\n"
					+ "		public static I TWO = new I() {\n"
					+ "			private static final String value = getString();\n"
					+ "			@Override\n"
					+ "			public String value() {\n"
					+ "				return value;\n"
					+ "			}\n"
					+ "		};\n"
					+ "	}\n"
					+ "	interface I {\n"
					+ "		public String value();\n"
					+ "	}\n"
					+ "}\n"
					+ "enum Test {\n"
					+ "	ONE {\n"
					+ "		public static String value;\n"
					+ "     static {\n"
					+ "       value = getString();\n"
					+ "     }\n"
					+ "		@Override\n"
					+ "		String value() {\n"
					+ "			return value;\n"
					+ "		}\n"
					+ "	};\n"
					+ "	abstract String value();\n"
					+ "	private static String getString() {\n"
					+ "		return \"Hi from Test\";\n"
					+ "	}\n"
					+ "}"
				},
			"Hi from Test\n" +
			"Hi from MyTest\n" +
			"Hi from EnumTester");
	}
}