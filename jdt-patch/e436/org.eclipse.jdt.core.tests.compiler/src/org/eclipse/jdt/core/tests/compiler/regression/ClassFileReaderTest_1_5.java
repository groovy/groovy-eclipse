/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import junit.framework.Test;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

@SuppressWarnings({ "rawtypes" })
public class ClassFileReaderTest_1_5 extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 16 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
	}
	public static Class testClass() {
		return ClassFileReaderTest_1_5.class;
	}

	public ClassFileReaderTest_1_5(String name) {
		super(name);
	}

	/**
	 * @deprecated
	 */
	private void checkClassFileUsingInputStream(String directoryName, String className, String source, String expectedOutput, int mode) throws IOException {
		compileAndDeploy(source, directoryName, className, false);
		BufferedInputStream inputStream = null;
		try {
			File directory = new File(EVAL_DIRECTORY, directoryName);
			if (!directory.exists()) {
				assertTrue(".class file not generated properly in " + directory, false);
			}
			File f = new File(directory, className + ".class");
			inputStream = new BufferedInputStream(new FileInputStream(f));
			IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(inputStream, IClassFileReader.ALL);
			assertNotNull(classFileReader);
			String result = ToolFactory.createDefaultClassFileDisassembler().disassemble(classFileReader, "\n", mode);
			int index = result.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
			removeTempClass(className);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76440
	 */
	public void test001() throws Exception {
		String source =
			"public class X {\n" +
			"	X(String s) {\n" +
			"	}\n" +
			"	public void foo(int i, long l, String[][]... args) {\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #18 (IJ[[[Ljava/lang/String;)V\n" +
			"  // Stack: 0, Locals: 5\n" +
			"  public void foo(int i, long l, java.lang.String[][]... args);\n" +
			"    0  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
			"        [pc: 0, pc: 1] local: i index: 1 type: int\n" +
			"        [pc: 0, pc: 1] local: l index: 2 type: long\n" +
			"        [pc: 0, pc: 1] local: args index: 4 type: java.lang.String[][][]\n" +
			"}";
		checkClassFile("X", source, expectedOutput);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76472
	 */
	public void test002() throws Exception {
		String source =
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		long[] tab = new long[] {};\n" +
			"		System.out.println(tab.clone());\n" +
			"		System.out.println(tab.clone());\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  newarray long [11]\n" +
			"     3  astore_1 [tab]\n" +
			"     4  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     7  aload_1 [tab]\n" +
			"     8  invokevirtual long[].clone() : java.lang.Object [22]\n" +
			"    11  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [28]\n" +
			"    14  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"    17  aload_1 [tab]\n" +
			"    18  invokevirtual long[].clone() : java.lang.Object [22]\n" +
			"    21  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [28]\n" +
			"    24  return\n";
		checkClassFile("X", source, expectedOutput);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111420
	public void test003() throws Exception {
		String source =
			"public class Y<W, U extends java.io.Reader & java.io.Serializable> {\n" +
			"  U field;\n" +
			"  String field2;\n" +
			"  <T> Y(T t) {}\n" +
			"  <T> T foo(T t, String... s) {\n" +
			"    return t;\n" +
			"  }\n" +
			"}";
		String expectedOutput =
			"public class Y<W,U extends Reader & Serializable> {\n" +
			"  \n" +
			"  U field;\n" +
			"  \n" +
			"  String field2;\n" +
			"  \n" +
			"  <T> Y(T t) {\n" +
			"  }\n" +
			"  \n" +
			"  <T> T foo(T t, String... s) {\n" +
			"    return null;\n" +
			"  }\n" +
			"}";
		checkClassFile("", "Y", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY | ClassFileBytesDisassembler.COMPACT);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=111420
	public void test004() throws Exception {
		String source =
			"public class Y<W, U extends java.io.Reader & java.io.Serializable> {\n" +
			"  U field;\n" +
			"  String field2;\n" +
			"  <T> Y(T t) {}\n" +
			"  <T> T foo(T t, String... s) {\n" +
			"    return t;\n" +
			"  }\n" +
			"}";
		String expectedOutput =
			"public class Y<W,U extends java.io.Reader & java.io.Serializable> {\n" +
			"  \n" +
			"  U field;\n" +
			"  \n" +
			"  java.lang.String field2;\n" +
			"  \n" +
			"  <T> Y(T t) {\n" +
			"  }\n" +
			"  \n" +
			"  <T> T foo(T t, java.lang.String... s) {\n" +
			"    return null;\n" +
			"  }\n" +
			"}";
		checkClassFile("", "Y", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76440
	 */
	public void test005() throws Exception {
		String source =
			"public class X {\n" +
			"	X(String s) {\n" +
			"	}\n" +
			"	public static void foo(int i, long l, String[][]... args) {\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"  // Method descriptor #18 (IJ[[[Ljava/lang/String;)V\n" +
			"  // Stack: 0, Locals: 4\n" +
			"  public static void foo(int i, long l, java.lang.String[][]... args);\n" +
			"    0  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 1] local: i index: 0 type: int\n" +
			"        [pc: 0, pc: 1] local: l index: 1 type: long\n" +
			"        [pc: 0, pc: 1] local: args index: 3 type: java.lang.String[][][]\n" +
			"}";
		checkClassFile("X", source, expectedOutput);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111494
	 */
	public void test006() throws Exception {
		String source =
			"public enum X { \n" +
			"	\n" +
			"	BLEU(10),\n" +
			"	BLANC(20),\n" +
			"	ROUGE(30);\n" +
			"	X(int i) {}\n" +
			"}\n";
		String expectedOutput =
			"public enum X {\n" +
			"  \n" +
			"  BLEU(0),\n" +
			"  \n" +
			"  BLANC(0),\n" +
			"  \n" +
			"  ROUGE(0),;\n" +
			"  \n" +
			"  private X(int i) {\n" +
			"  }\n" +
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111494
	 * TODO corner case that doesn't produce the right source
	 */
	public void test007() throws Exception {
		String source =
			"public enum X {\n" +
			"	BLEU(0) {\n" +
			"		public String colorName() {\n" +
			"			return \"BLEU\";\n" +
			"		}\n" +
			"	},\n" +
			"	BLANC(1) {\n" +
			"		public String colorName() {\n" +
			"			return \"BLANC\";\n" +
			"		}\n" +
			"	},\n" +
			"	ROUGE(2) {\n" +
			"		public String colorName() {\n" +
			"			return \"ROUGE\";\n" +
			"		}\n" +
			"	},;\n" +
			"	\n" +
			"	X(int i) {\n" +
			"	}\n" +
			"	abstract public String colorName();\n" +
			"}";
		String expectedOutput =
			"public enum X {\n" +
			"  \n" +
			"  BLEU(0),\n" +
			"  \n" +
			"  BLANC(0),\n" +
			"  \n" +
			"  ROUGE(0),;\n" +
			"  \n" +
			"  private X(int i) {\n" +
			"  }\n" +
			"  \n" +
			"  public abstract java.lang.String colorName();\n" +
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111494
	 * TODO corner case that doesn't produce the right source
	 */
	public void test008() throws Exception {
		String source =
			"interface I {\n" +
			"	String colorName();\n" +
			"}\n" +
			"public enum X implements I {\n" +
			"	BLEU(0) {\n" +
			"		public String colorName() {\n" +
			"			return \"BLEU\";\n" +
			"		}\n" +
			"	},\n" +
			"	BLANC(1) {\n" +
			"		public String colorName() {\n" +
			"			return \"BLANC\";\n" +
			"		}\n" +
			"	},\n" +
			"	ROUGE(2) {\n" +
			"		public String colorName() {\n" +
			"			return \"ROUGE\";\n" +
			"		}\n" +
			"	},;\n" +
			"	\n" +
			"	X(int i) {\n" +
			"	}\n" +
			"}";
		String expectedOutput =
			"public enum X implements I {\n" +
			"  \n" +
			"  BLEU(0),\n" +
			"  \n" +
			"  BLANC(0),\n" +
			"  \n" +
			"  ROUGE(0),;\n" +
			"  \n" +
			"  private X(int i) {\n" +
			"  }\n" +
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111767
	 */
	public void test009() throws Exception {
		String source =
			"@interface X {\n" +
			"	String firstName();\n" +
			"	String lastName() default \"Smith\";\n" +
			"}\n";
		String expectedOutput =
			"abstract @interface X {\n" +
			"  \n" +
			"  public abstract java.lang.String firstName();\n" +
			"  \n" +
			"  public abstract java.lang.String lastName() default \"Smith\";\n" +
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=111767
	 * @deprecated Using deprecated API
	 */
	public void test010() throws Exception {
		String source =
			"@interface X {\n" +
			"	String firstName();\n" +
			"	String lastName() default \"Smith\";\n" +
			"}\n";
		String expectedOutput =
			"abstract @interface X {\n" +
			"  \n" +
			"  public abstract java.lang.String firstName();\n" +
			"  \n" +
			"  public abstract java.lang.String lastName() default \"Smith\";\n" +
			"}";
		checkClassFileUsingInputStream("", "X", source, expectedOutput, ClassFileBytesDisassembler.WORKING_COPY);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=203577
	 */
	public void test011() throws Exception {
		String source =
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(value={})\n" +
			"@Retention(value=RetentionPolicy.RUNTIME)\n" +
			"public @interface X {}";
		String expectedOutput =
			"public abstract @interface X extends java.lang.annotation.Annotation {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 class: #6 java/lang/annotation/Annotation\n" +
			"    constant #6 utf8: \"java/lang/annotation/Annotation\"\n" +
			"    constant #7 utf8: \"SourceFile\"\n" +
			"    constant #8 utf8: \"X.java\"\n" +
			"    constant #9 utf8: \"RuntimeVisibleAnnotations\"\n" +
			"    constant #10 utf8: \"Ljava/lang/annotation/Target;\"\n" +
			"    constant #11 utf8: \"value\"\n" +
			"    constant #12 utf8: \"Ljava/lang/annotation/Retention;\"\n" +
			"    constant #13 utf8: \"Ljava/lang/annotation/RetentionPolicy;\"\n" +
			"    constant #14 utf8: \"RUNTIME\"\n" +
			"\n" +
			"  RuntimeVisibleAnnotations: \n" +
			"    #10 @java.lang.annotation.Target(\n" +
			"      #11 value=[\n" +
			"        ]\n" +
			"    )\n" +
			"    #12 @java.lang.annotation.Retention(\n" +
			"      #11 value=java.lang.annotation.RetentionPolicy.RUNTIME(enum type #13.#14)\n" +
			"    )\n" +
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=203609
	 */
	public void test012() throws Exception {
		String source =
			"@Deprecated\n" +
			"package p;";
		String expectedOutput =
			"abstract interface p.package-info {\n" +
			"}";
		if (this.complianceLevel > ClassFileConstants.JDK1_5) {
			expectedOutput = "abstract synthetic interface p.package-info {\n" +
			"}";
		}
		checkClassFile("p", "package-info", source, expectedOutput, ClassFileBytesDisassembler.DEFAULT);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=217907
	 */
	public void test013() throws Exception {
		String source =
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(value={})\n" +
			"@Retention(value=RetentionPolicy.RUNTIME)\n" +
			"public @interface X {}";
		String expectedOutput =
			"public abstract @interface X extends Annotation {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 class: #6 java/lang/annotation/Annotation\n" +
			"    constant #6 utf8: \"java/lang/annotation/Annotation\"\n" +
			"    constant #7 utf8: \"SourceFile\"\n" +
			"    constant #8 utf8: \"X.java\"\n" +
			"    constant #9 utf8: \"RuntimeVisibleAnnotations\"\n" +
			"    constant #10 utf8: \"Ljava/lang/annotation/Target;\"\n" +
			"    constant #11 utf8: \"value\"\n" +
			"    constant #12 utf8: \"Ljava/lang/annotation/Retention;\"\n" +
			"    constant #13 utf8: \"Ljava/lang/annotation/RetentionPolicy;\"\n" +
			"    constant #14 utf8: \"RUNTIME\"\n" +
			"\n" +
			"  RuntimeVisibleAnnotations: \n" +
			"    #10 @Target(\n" +
			"      #11 value=[\n" +
			"        ]\n" +
			"    )\n" +
			"    #12 @Retention(\n" +
			"      #11 value=RetentionPolicy.RUNTIME(enum type #13.#14)\n" +
			"    )\n" +
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.SYSTEM | ClassFileBytesDisassembler.COMPACT);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=217907
	 */
	public void test014() throws Exception {
		String source =
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(value={})\n" +
			"@Retention(value=RetentionPolicy.RUNTIME)\n" +
			"public @interface X {}";
		String expectedOutput =
			"@Target(value={})\n" +
			"@Retention(value=RetentionPolicy.RUNTIME)\n" +
			"public abstract @interface X extends Annotation {\n" +
			"\n" +
			"}";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=217910
	 */
	public void test015() throws Exception {
		String source =
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.*;\n" +
			"public class X {\n" +
			"        public void foo(@Deprecated @Annot(2) int i) {}\n" +
			"}\n" +
			"@Retention(CLASS)\n" +
			"@interface Annot {\n" +
			"        int value() default -1;\n" +
			"}";
		String expectedOutput =
			"  public void foo(@Deprecated @Annot(value=(int) 2) int i);";
		checkClassFile("", "X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=286405
	 */
	public void test016() throws Exception {
		String source =
			"public @interface MonAnnotation {\n" +
			"	String test1() default \"\\0\";\n" +
			"	char test2() default '\\0';\n" +
			"}\n" +
			"";
		String expectedOutput =
			"  public abstract char test2() default \'\\u0000\';";
		checkClassFile("", "MonAnnotation", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
	}

	public void testBug504031() throws Exception {
		String source =
				"package test;\n" +
				"@RunWith(JUnitPlatform.class)\n" +
				"@SelectPackages(\"test.dynamic.TODO\")\n" +
				"public class AllTests {\n" +
				"	@Test\n" +
				"	void test1() {\n" +
				"	}\n" +
				"} \n" +
				"\n" +
				"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n" +
				"@interface RunWith {\n" +
				"    Class<? extends Runner> value();\n" +
				"}\n" +
				"@interface SelectPackages {\n" +
				"}\n" +
				"class Runner {}\n" +
				"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n" +
				"@interface Test {}\n";

		String expectedOutput =
				"@RunWith(value=JUnitPlatform)\n" +
				"public class test.AllTests {\n" +
				"  Constant pool:\n" +
				"    constant #1 class: #2 test/AllTests\n" +
				"    constant #2 utf8: \"test/AllTests\"\n" +
				"    constant #3 class: #4 java/lang/Object\n" +
				"    constant #4 utf8: \"java/lang/Object\"\n" +
				"    constant #5 utf8: \"<init>\"\n" +
				"    constant #6 utf8: \"()V\"\n" +
				"    constant #7 utf8: \"Code\"\n" +
				"    constant #8 class: #9 java/lang/Error\n" +
				"    constant #9 utf8: \"java/lang/Error\"\n" +
				"    constant #10 string: #11 \"Unresolved compilation problems: \\n\\tJUnitPlatform cannot be resolved to a type\\n\\tClass<JUnitPlatform> cannot be resolved to a type\\n\\tThe attribute value is undefined for the annotation type SelectPackages\\n\"\n" +
				"    constant #11 utf8: \"Unresolved compilation problems: \\n\\tJUnitPlatform cannot be resolved to a type\\n\\tClass<JUnitPlatform> cannot be resolved to a type\\n\\tThe attribute value is undefined for the annotation type SelectPackages\\n\"\n" +
				"    constant #12 method_ref: #8.#13 java/lang/Error.<init> (Ljava/lang/String;)V\n" +
				"    constant #13 name_and_type: #5.#14 <init> (Ljava/lang/String;)V\n" +
				"    constant #14 utf8: \"(Ljava/lang/String;)V\"\n" +
				"    constant #15 utf8: \"LineNumberTable\"\n" +
				"    constant #16 utf8: \"LocalVariableTable\"\n" +
				"    constant #17 utf8: \"this\"\n" +
				"    constant #18 utf8: \"Ltest/AllTests;\"\n" +
				"    constant #19 utf8: \"test1\"\n" +
				"    constant #20 utf8: \"RuntimeVisibleAnnotations\"\n" +
				"    constant #21 utf8: \"Ltest/Test;\"\n" +
				"    constant #22 string: #23 \"Unresolved compilation problem: \\n\"\n" +
				"    constant #23 utf8: \"Unresolved compilation problem: \\n\"\n" +
				"    constant #24 utf8: \"SourceFile\"\n" +
				"    constant #25 utf8: \"AllTests.java\"\n" +
				"    constant #26 utf8: \"RuntimeInvisibleAnnotations\"\n" + // unused but tolerated
				"    constant #27 utf8: \"Ltest/SelectPackages;\"\n" + // unused but tolerated
				"    constant #28 utf8: \"value\"\n" +
				"    constant #29 utf8: \"Ltest/RunWith;\"\n" +
				"    constant #30 utf8: \"LJUnitPlatform;\"\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 3, Locals: 1\n" +
				"  public AllTests();\n" +
				"     0  new Error [8]\n" +
				"     3  dup\n" +
				"     4  ldc <String \"Unresolved compilation problems: \\n\\tJUnitPlatform cannot be resolved to a type\\n\\tClass<JUnitPlatform> cannot be resolved to a type\\n\\tThe attribute value is undefined for the annotation type SelectPackages\\n\"> [10]\n" +
				"     6  invokespecial Error(String) [12]\n" +
				"     9  athrow\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 2]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 10] local: this index: 0 type: AllTests\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 3, Locals: 1\n" +
				"  @Test\n" +
				"  void test1();\n" +
				"     0  new Error [8]\n" +
				"     3  dup\n" +
				"     4  ldc <String \"Unresolved compilation problem: \\n\"> [22]\n" +
				"     6  invokespecial Error(String) [12]\n" +
				"     9  athrow\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 10] local: this index: 0 type: AllTests\n" +
				"    RuntimeVisibleAnnotations: \n" +
				"      #21 @Test(\n" +
				"      )\n" +
				"\n" +
				"  RuntimeVisibleAnnotations: \n" +
				"    #29 @RunWith(\n" +
				"      #28 value=JUnitPlatform (#30 class type)\n" +
				"    )\n" +
				"}";
		int mode = ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT | ClassFileBytesDisassembler.SYSTEM;
		checkClassFile("test", "AllTests", "AllTests", source, expectedOutput, mode, true/*suppress expected errors*/);
	}

}
