/*******************************************************************************
 * Copyright (c) 2013, 2018 Jesper Steen Moller, IBM and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Moller - initial API and implementation
 *            Bug 416885 - [1.8][compiler]IncompatibleClassChange error (edit)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JSR335ClassFileTest extends AbstractComparableTest {

	String versionString = null;

public JSR335ClassFileTest(String name) {
	super(name);
}
// No need for a tearDown()
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.versionString = AbstractCompilerTest.getVersionString(this.complianceLevel);
}

/*
 * Toggle compiler in mode -1.8
 */
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test055" };
//	TESTS_NUMBERS = new int[] { 50, 51, 52, 53 };
//	TESTS_RANGE = new int[] { 34, 38 };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
private void verifyClassFile(String expectedOutput, String classFileName, int mode) throws IOException,
	ClassFormatException {
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
public void test001() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().referenceExpression.run();\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Runnable referenceExpression = Thread::yield;\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
		"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
		"public class X {\n" +
		"  Constant pool:\n" +
		"    constant #1 class: #2 X\n" +
		"    constant #2 utf8: \"X\"\n" +
		"    constant #3 class: #4 java/lang/Object\n" +
		"    constant #4 utf8: \"java/lang/Object\"\n" +
		"    constant #5 utf8: \"referenceExpression\"\n" +
		"    constant #6 utf8: \"Ljava/lang/Runnable;\"\n" +
		"    constant #7 utf8: \"<init>\"\n" +
		"    constant #8 utf8: \"()V\"\n" +
		"    constant #9 utf8: \"Code\"\n" +
		"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" +
		"    constant #11 name_and_type: #7.#8 <init> ()V\n" +
		"    constant #12 invoke dynamic: #0 #13 run ()Ljava/lang/Runnable;\n" +
		"    constant #13 name_and_type: #14.#15 run ()Ljava/lang/Runnable;\n" +
		"    constant #14 utf8: \"run\"\n" +
		"    constant #15 utf8: \"()Ljava/lang/Runnable;\"\n" +
		"    constant #16 field_ref: #1.#17 X.referenceExpression Ljava/lang/Runnable;\n" +
		"    constant #17 name_and_type: #5.#6 referenceExpression Ljava/lang/Runnable;\n" +
		"    constant #18 utf8: \"LineNumberTable\"\n" +
		"    constant #19 utf8: \"LocalVariableTable\"\n" +
		"    constant #20 utf8: \"this\"\n" +
		"    constant #21 utf8: \"LX;\"\n" +
		"    constant #22 utf8: \"SourceFile\"\n" +
		"    constant #23 utf8: \"X.java\"\n" +
		"    constant #24 utf8: \"BootstrapMethods\"\n" +
		"    constant #25 method_ref: #26.#28 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
		"    constant #26 class: #27 java/lang/invoke/LambdaMetafactory\n" +
		"    constant #27 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
		"    constant #28 name_and_type: #29.#30 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
		"    constant #29 utf8: \"metafactory\"\n" +
		"    constant #30 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
		"    constant #31 method handle: invokestatic (6) #25 \n" +
		"    constant #32 method type: #8 ()V\n" +
		"    constant #33 method_ref: #34.#36 java/lang/Thread.yield ()V\n" +
		"    constant #34 class: #35 java/lang/Thread\n" +
		"    constant #35 utf8: \"java/lang/Thread\"\n" +
		"    constant #36 name_and_type: #37.#8 yield ()V\n" +
		"    constant #37 utf8: \"yield\"\n" +
		"    constant #38 method handle: invokestatic (6) #33 \n" +
		"    constant #39 method type: #8 ()V\n" +
		"    constant #40 utf8: \"InnerClasses\"\n" +
		"    constant #41 class: #42 java/lang/invoke/MethodHandles$Lookup\n" +
		"    constant #42 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
		"    constant #43 class: #44 java/lang/invoke/MethodHandles\n" +
		"    constant #44 utf8: \"java/lang/invoke/MethodHandles\"\n" +
		"    constant #45 utf8: \"Lookup\"\n" +
		"  \n" +
		"  // Field descriptor #6 Ljava/lang/Runnable;\n" +
		"  public java.lang.Runnable referenceExpression;\n" +
		"  \n" +
		"  // Method descriptor #8 ()V\n" +
		"  // Stack: 2, Locals: 1\n" +
		"  public X();\n" +
		"     0  aload_0 [this]\n" +
		"     1  invokespecial java.lang.Object() [10]\n" +
		"     4  aload_0 [this]\n" +
		"     5  invokedynamic 0 run() : java.lang.Runnable [12]\n" +
		"    10  putfield X.referenceExpression : java.lang.Runnable [16]\n" +
		"    13  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 1]\n" +
		"        [pc: 4, line: 2]\n" +
		"        [pc: 13, line: 1]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
		"\n" +
		"  Inner classes:\n" +
		"    [inner class info: #41 java/lang/invoke/MethodHandles$Lookup, outer class info: #43 java/lang/invoke/MethodHandles\n" +
		"     inner name: #45 Lookup, accessflags: 25 public static final]\n" +
		"Bootstrap methods:\n" +
		"  0 : # 31 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
		"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
		"	Method arguments:\n" +
		"		#32 ()V\n" +
		"		#38 invokestatic java/lang/Thread.yield:()V\n" +
		"		#39 ()V\n" +
		"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test002() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().referenceExpression.consume(\"SUCCESS\");\n" +
			"    }\n" +
			"    public static void printIt(Object o) {\n" +
			"        System.out.println(o.toString());\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public ObjectConsumer referenceExpression = Main::printIt;\n" +
			"}\n",
			"ObjectConsumer.java",
			"public interface ObjectConsumer {\n" +
			"    void consume(Object obj);\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"referenceExpression\"\n" +
			"    constant #6 utf8: \"LObjectConsumer;\"\n" +
			"    constant #7 utf8: \"<init>\"\n" +
			"    constant #8 utf8: \"()V\"\n" +
			"    constant #9 utf8: \"Code\"\n" +
			"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" +
			"    constant #11 name_and_type: #7.#8 <init> ()V\n" +
			"    constant #12 invoke dynamic: #0 #13 consume ()LObjectConsumer;\n" +
			"    constant #13 name_and_type: #14.#15 consume ()LObjectConsumer;\n" +
			"    constant #14 utf8: \"consume\"\n" +
			"    constant #15 utf8: \"()LObjectConsumer;\"\n" +
			"    constant #16 field_ref: #1.#17 X.referenceExpression LObjectConsumer;\n" +
			"    constant #17 name_and_type: #5.#6 referenceExpression LObjectConsumer;\n" +
			"    constant #18 utf8: \"LineNumberTable\"\n" +
			"    constant #19 utf8: \"LocalVariableTable\"\n" +
			"    constant #20 utf8: \"this\"\n" +
			"    constant #21 utf8: \"LX;\"\n" +
			"    constant #22 utf8: \"SourceFile\"\n" +
			"    constant #23 utf8: \"X.java\"\n" +
			"    constant #24 utf8: \"BootstrapMethods\"\n" +
			"    constant #25 method_ref: #26.#28 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #26 class: #27 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #27 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #28 name_and_type: #29.#30 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #29 utf8: \"metafactory\"\n" +
			"    constant #30 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #31 method handle: invokestatic (6) #25 \n" +
			"    constant #32 utf8: \"(Ljava/lang/Object;)V\"\n" +
			"    constant #33 method type: #32 (Ljava/lang/Object;)V\n" +
			"    constant #34 method_ref: #35.#37 Main.printIt (Ljava/lang/Object;)V\n" +
			"    constant #35 class: #36 Main\n" +
			"    constant #36 utf8: \"Main\"\n" +
			"    constant #37 name_and_type: #38.#32 printIt (Ljava/lang/Object;)V\n" +
			"    constant #38 utf8: \"printIt\"\n" +
			"    constant #39 method handle: invokestatic (6) #34 \n" +
			"    constant #40 method type: #32 (Ljava/lang/Object;)V\n" +
			"    constant #41 utf8: \"InnerClasses\"\n" +
			"    constant #42 class: #43 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #43 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #44 class: #45 java/lang/invoke/MethodHandles\n" +
			"    constant #45 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #46 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 LObjectConsumer;\n" +
			"  public ObjectConsumer referenceExpression;\n" +
			"  \n" +
			"  // Method descriptor #8 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  public X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [10]\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokedynamic 0 consume() : ObjectConsumer [12]\n" +
			"    10  putfield X.referenceExpression : ObjectConsumer [16]\n" +
			"    13  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"        [pc: 4, line: 2]\n" +
			"        [pc: 13, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #42 java/lang/invoke/MethodHandles$Lookup, outer class info: #44 java/lang/invoke/MethodHandles\n" +
			"     inner name: #46 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 31 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup" +
			";Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#33 (Ljava/lang/Object;)V\n" +
			"		#39 invokestatic Main.printIt:(Ljava/lang/Object;)V\n" +
			"		#40 (Ljava/lang/Object;)V\n" +
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test003() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().referenceExpression.makeString(new Main()));\n" +
			"    }\n" +
			"    @Override\n" +
			"    public String toString() {\n" +
			"        return \"SUCCESS\";\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public ObjectToString referenceExpression = Object::toString;\n" +
			"}\n",
			"ObjectToString.java",
			"public interface ObjectToString {\n" +
			"    String makeString(Object obj);\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"referenceExpression\"\n" +
			"    constant #6 utf8: \"LObjectToString;\"\n" +
			"    constant #7 utf8: \"<init>\"\n" +
			"    constant #8 utf8: \"()V\"\n" +
			"    constant #9 utf8: \"Code\"\n" +
			"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" +
			"    constant #11 name_and_type: #7.#8 <init> ()V\n" +
			"    constant #12 invoke dynamic: #0 #13 makeString ()LObjectToString;\n" +
			"    constant #13 name_and_type: #14.#15 makeString ()LObjectToString;\n" +
			"    constant #14 utf8: \"makeString\"\n" +
			"    constant #15 utf8: \"()LObjectToString;\"\n" +
			"    constant #16 field_ref: #1.#17 X.referenceExpression LObjectToString;\n" +
			"    constant #17 name_and_type: #5.#6 referenceExpression LObjectToString;\n" +
			"    constant #18 utf8: \"LineNumberTable\"\n" +
			"    constant #19 utf8: \"LocalVariableTable\"\n" +
			"    constant #20 utf8: \"this\"\n" +
			"    constant #21 utf8: \"LX;\"\n" +
			"    constant #22 utf8: \"SourceFile\"\n" +
			"    constant #23 utf8: \"X.java\"\n" +
			"    constant #24 utf8: \"BootstrapMethods\"\n" +
			"    constant #25 method_ref: #26.#28 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #26 class: #27 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #27 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #28 name_and_type: #29.#30 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #29 utf8: \"metafactory\"\n" +
			"    constant #30 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #31 method handle: invokestatic (6) #25 \n" +
			"    constant #32 utf8: \"(Ljava/lang/Object;)Ljava/lang/String;\"\n" +
			"    constant #33 method type: #32 (Ljava/lang/Object;)Ljava/lang/String;\n" +
			"    constant #34 method_ref: #3.#35 java/lang/Object.toString ()Ljava/lang/String;\n" +
			"    constant #35 name_and_type: #36.#37 toString ()Ljava/lang/String;\n" +
			"    constant #36 utf8: \"toString\"\n" +
			"    constant #37 utf8: \"()Ljava/lang/String;\"\n" +
			"    constant #38 method handle: invokevirtual (5) #34 \n" +
			"    constant #39 method type: #32 (Ljava/lang/Object;)Ljava/lang/String;\n" +
			"    constant #40 utf8: \"InnerClasses\"\n" +
			"    constant #41 class: #42 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #42 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #43 class: #44 java/lang/invoke/MethodHandles\n" +
			"    constant #44 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #45 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 LObjectToString;\n" +
			"  public ObjectToString referenceExpression;\n" +
			"  \n" +
			"  // Method descriptor #8 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  public X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [10]\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokedynamic 0 makeString() : ObjectToString [12]\n" +
			"    10  putfield X.referenceExpression : ObjectToString [16]\n" +
			"    13  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"        [pc: 4, line: 2]\n" +
			"        [pc: 13, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #41 java/lang/invoke/MethodHandles$Lookup, outer class info: #43 java/lang/invoke/MethodHandles\n" +
			"     inner name: #45 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 31 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#33 (Ljava/lang/Object;)Ljava/lang/String;\n" +
			"		#38 java/lang/Object.toString:()Ljava/lang/String;\n" +
			"		#39 (Ljava/lang/Object;)Ljava/lang/String;\n" +
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test004() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().referenceExpression.produce());\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    String s = \"SUCCESS\";\n"+
			"    public StringProducer referenceExpression = s::toString;\n" +
			"}\n",
			"StringProducer.java",
			"public interface StringProducer {\n" +
			"    String produce();\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"s\"\n" +
			"    constant #6 utf8: \"Ljava/lang/String;\"\n" +
			"    constant #7 utf8: \"referenceExpression\"\n" +
			"    constant #8 utf8: \"LStringProducer;\"\n" +
			"    constant #9 utf8: \"<init>\"\n" +
			"    constant #10 utf8: \"()V\"\n" +
			"    constant #11 utf8: \"Code\"\n" +
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" +
			"    constant #13 name_and_type: #9.#10 <init> ()V\n" +
			"    constant #14 string: #15 \"SUCCESS\"\n" +
			"    constant #15 utf8: \"SUCCESS\"\n" +
			"    constant #16 field_ref: #1.#17 X.s Ljava/lang/String;\n" +
			"    constant #17 name_and_type: #5.#6 s Ljava/lang/String;\n" +
			"    constant #18 method_ref: #3.#19 java/lang/Object.getClass ()Ljava/lang/Class;\n" +
			"    constant #19 name_and_type: #20.#21 getClass ()Ljava/lang/Class;\n" +
			"    constant #20 utf8: \"getClass\"\n" +
			"    constant #21 utf8: \"()Ljava/lang/Class;\"\n" +
			"    constant #22 invoke dynamic: #0 #23 produce (Ljava/lang/String;)LStringProducer;\n" +
			"    constant #23 name_and_type: #24.#25 produce (Ljava/lang/String;)LStringProducer;\n" +
			"    constant #24 utf8: \"produce\"\n" +
			"    constant #25 utf8: \"(Ljava/lang/String;)LStringProducer;\"\n" +
			"    constant #26 field_ref: #1.#27 X.referenceExpression LStringProducer;\n" +
			"    constant #27 name_and_type: #7.#8 referenceExpression LStringProducer;\n" +
			"    constant #28 utf8: \"LineNumberTable\"\n" +
			"    constant #29 utf8: \"LocalVariableTable\"\n" +
			"    constant #30 utf8: \"this\"\n" +
			"    constant #31 utf8: \"LX;\"\n" +
			"    constant #32 utf8: \"SourceFile\"\n" +
			"    constant #33 utf8: \"X.java\"\n" +
			"    constant #34 utf8: \"BootstrapMethods\"\n" +
			"    constant #35 method_ref: #36.#38 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #36 class: #37 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #37 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #38 name_and_type: #39.#40 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #39 utf8: \"metafactory\"\n" +
			"    constant #40 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #41 method handle: invokestatic (6) #35 \n" +
			"    constant #42 utf8: \"()Ljava/lang/String;\"\n" +
			"    constant #43 method type: #42 ()Ljava/lang/String;\n" +
			"    constant #44 method_ref: #45.#47 java/lang/String.toString ()Ljava/lang/String;\n" +
			"    constant #45 class: #46 java/lang/String\n" +
			"    constant #46 utf8: \"java/lang/String\"\n" +
			"    constant #47 name_and_type: #48.#42 toString ()Ljava/lang/String;\n" +
			"    constant #48 utf8: \"toString\"\n" +
			"    constant #49 method handle: invokevirtual (5) #44 \n" +
			"    constant #50 method type: #42 ()Ljava/lang/String;\n" +
			"    constant #51 utf8: \"InnerClasses\"\n" +
			"    constant #52 class: #53 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #53 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #54 class: #55 java/lang/invoke/MethodHandles\n" +
			"    constant #55 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #56 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 Ljava/lang/String;\n" +
			"  java.lang.String s;\n" +
			"  \n" +
			"  // Field descriptor #8 LStringProducer;\n" +
			"  public StringProducer referenceExpression;\n" +
			"  \n" +
			"  // Method descriptor #10 ()V\n" +
			"  // Stack: 3, Locals: 1\n" +
			"  public X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [12]\n" +
			"     4  aload_0 [this]\n" +
			"     5  ldc <String \"SUCCESS\"> [14]\n" +
			"     7  putfield X.s : java.lang.String [16]\n" +
			"    10  aload_0 [this]\n" +
			"    11  aload_0 [this]\n" +
			"    12  getfield X.s : java.lang.String [16]\n" +
			"    15  dup\n" +
			"    16  invokevirtual java.lang.Object.getClass() : java.lang.Class [18]\n" +
			"    19  pop\n" +
			"    20  invokedynamic 0 produce(java.lang.String) : StringProducer [22]\n" +
			"    25  putfield X.referenceExpression : StringProducer [26]\n" +
			"    28  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"        [pc: 4, line: 2]\n" +
			"        [pc: 10, line: 3]\n" +
			"        [pc: 28, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 29] local: this index: 0 type: X\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #52 java/lang/invoke/MethodHandles$Lookup, outer class info: #54 java/lang/invoke/MethodHandles\n" +
			"     inner name: #56 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 41 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#43 ()Ljava/lang/String;\n" +
			"		#49 java/lang/String.toString:()Ljava/lang/String;\n" +
			"		#50 ()Ljava/lang/String;\n" +
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test005() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(X.allocatorExpression.produce());\n" +
			"    }\n" +
			"    @Override\n" +
			"    public String toString() {\n" +
			"        return \"SUCCESS\";" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public static MainProducer allocatorExpression = Main::new;\n" +
			"}\n",
			"MainProducer.java",
			"public interface MainProducer {\n" +
			"    Main produce();\n" +
			"}\n",
		},
	"SUCCESS"
	);
	verifyClassFile("// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"allocatorExpression\"\n" +
			"    constant #6 utf8: \"LMainProducer;\"\n" +
			"    constant #7 utf8: \"<clinit>\"\n" +
			"    constant #8 utf8: \"()V\"\n" +
			"    constant #9 utf8: \"Code\"\n" +
			"    constant #10 invoke dynamic: #0 #11 produce ()LMainProducer;\n" +
			"    constant #11 name_and_type: #12.#13 produce ()LMainProducer;\n" +
			"    constant #12 utf8: \"produce\"\n" +
			"    constant #13 utf8: \"()LMainProducer;\"\n" +
			"    constant #14 field_ref: #1.#15 X.allocatorExpression LMainProducer;\n" +
			"    constant #15 name_and_type: #5.#6 allocatorExpression LMainProducer;\n" +
			"    constant #16 utf8: \"LineNumberTable\"\n" +
			"    constant #17 utf8: \"LocalVariableTable\"\n" +
			"    constant #18 utf8: \"<init>\"\n" +
			"    constant #19 method_ref: #3.#20 java/lang/Object.<init> ()V\n" +
			"    constant #20 name_and_type: #18.#8 <init> ()V\n" +
			"    constant #21 utf8: \"this\"\n" +
			"    constant #22 utf8: \"LX;\"\n" +
			"    constant #23 utf8: \"SourceFile\"\n" +
			"    constant #24 utf8: \"X.java\"\n" +
			"    constant #25 utf8: \"BootstrapMethods\"\n" +
			"    constant #26 method_ref: #27.#29 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #27 class: #28 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #28 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #29 name_and_type: #30.#31 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #30 utf8: \"metafactory\"\n" +
			"    constant #31 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #32 method handle: invokestatic (6) #26 \n" +
			"    constant #33 utf8: \"()LMain;\"\n" +
			"    constant #34 method type: #33 ()LMain;\n" +
			"    constant #35 method_ref: #36.#20 Main.<init> ()V\n" +
			"    constant #36 class: #37 Main\n" +
			"    constant #37 utf8: \"Main\"\n" +
			"    constant #38 method handle: newinvokespecial (8) #35 \n" +
			"    constant #39 method type: #33 ()LMain;\n" +
			"    constant #40 utf8: \"InnerClasses\"\n" +
			"    constant #41 class: #42 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #42 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #43 class: #44 java/lang/invoke/MethodHandles\n" +
			"    constant #44 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #45 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 LMainProducer;\n" +
			"  public static MainProducer allocatorExpression;\n" +
			"  \n" +
			"  // Method descriptor #8 ()V\n" +
			"  // Stack: 1, Locals: 0\n" +
			"  static {};\n" +
			"    0  invokedynamic 0 produce() : MainProducer [10]\n" +
			"    5  putstatic X.allocatorExpression : MainProducer [14]\n" +
			"    8  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 2]\n" +
			"  \n" +
			"  // Method descriptor #8 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public X();\n" +
			"    0  aload_0 [this]\n" +
			"    1  invokespecial java.lang.Object() [19]\n" +
			"    4  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #41 java/lang/invoke/MethodHandles$Lookup, outer class info: #43 java/lang/invoke/MethodHandles\n" +
			"     inner name: #45 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 32 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#34 ()LMain;\n" +
			"		#38 Main.<init>:()V\n" +
			"		#39 ()LMain;\n" +
			"}", "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test006() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    String s1, s2;\n" +
			"    public Main(String val1, String val2) {" +
			"        s1 = val1;\n" +
			"        s2 = val2;\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        Main m = X.producer.apply(\"SUCC\", \"ESS\");\n" +
			"        System.out.println(m);\n" +
			"    }\n" +
			"    public String toString() {\n" +
			"        return s1 + s2;" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"        public static Function2<Main, String, String> producer = Main::new;\n" +
			"}\n",
			"Function2.java",
			"public interface Function2<R, T1, T2> {\n" +
			"    R apply(T1 a1, T2 a2);\n" +
			"}\n",
		},
	"SUCCESS"
	);
	String expected =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"producer\"\n" +
			"    constant #6 utf8: \"LFunction2;\"\n" +
			"    constant #7 utf8: \"Signature\"\n" +
			"    constant #8 utf8: \"LFunction2<LMain;Ljava/lang/String;Ljava/lang/String;>;\"\n" +
			"    constant #9 utf8: \"<clinit>\"\n" +
			"    constant #10 utf8: \"()V\"\n" +
			"    constant #11 utf8: \"Code\"\n" +
			"    constant #12 invoke dynamic: #0 #13 apply ()LFunction2;\n" +
			"    constant #13 name_and_type: #14.#15 apply ()LFunction2;\n" +
			"    constant #14 utf8: \"apply\"\n" +
			"    constant #15 utf8: \"()LFunction2;\"\n" +
			"    constant #16 field_ref: #1.#17 X.producer LFunction2;\n" +
			"    constant #17 name_and_type: #5.#6 producer LFunction2;\n" +
			"    constant #18 utf8: \"LineNumberTable\"\n" +
			"    constant #19 utf8: \"LocalVariableTable\"\n" +
			"    constant #20 utf8: \"<init>\"\n" +
			"    constant #21 method_ref: #3.#22 java/lang/Object.<init> ()V\n" +
			"    constant #22 name_and_type: #20.#10 <init> ()V\n" +
			"    constant #23 utf8: \"this\"\n" +
			"    constant #24 utf8: \"LX;\"\n" +
			"    constant #25 utf8: \"SourceFile\"\n" +
			"    constant #26 utf8: \"X.java\"\n" +
			"    constant #27 utf8: \"BootstrapMethods\"\n" +
			"    constant #28 method_ref: #29.#31 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #29 class: #30 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #30 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #31 name_and_type: #32.#33 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #32 utf8: \"metafactory\"\n" +
			"    constant #33 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #34 method handle: invokestatic (6) #28 \n" +
			"    constant #35 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" +
			"    constant #36 method type: #35 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
			"    constant #37 method_ref: #38.#40 Main.<init> (Ljava/lang/String;Ljava/lang/String;)V\n" +
			"    constant #38 class: #39 Main\n" +
			"    constant #39 utf8: \"Main\"\n" +
			"    constant #40 name_and_type: #20.#41 <init> (Ljava/lang/String;Ljava/lang/String;)V\n" +
			"    constant #41 utf8: \"(Ljava/lang/String;Ljava/lang/String;)V\"\n" +
			"    constant #42 method handle: newinvokespecial (8) #37 \n" +
			"    constant #43 utf8: \"(Ljava/lang/String;Ljava/lang/String;)LMain;\"\n" +
			"    constant #44 method type: #43 (Ljava/lang/String;Ljava/lang/String;)LMain;\n" +
			"    constant #45 utf8: \"InnerClasses\"\n" +
			"    constant #46 class: #47 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #47 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #48 class: #49 java/lang/invoke/MethodHandles\n" +
			"    constant #49 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #50 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 LFunction2;\n" +
			"  // Signature: LFunction2<LMain;Ljava/lang/String;Ljava/lang/String;>;\n" +
			"  public static Function2 producer;\n" +
			"  \n" +
			"  // Method descriptor #10 ()V\n" +
			"  // Stack: 1, Locals: 0\n" +
			"  static {};\n" +
			"    0  invokedynamic 0 apply() : Function2 [12]\n" +
			"    5  putstatic X.producer : Function2 [16]\n" +
			"    8  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 2]\n" +
			"  \n" +
			"  // Method descriptor #10 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public X();\n" +
			"    0  aload_0 [this]\n" +
			"    1  invokespecial java.lang.Object() [21]\n" +
			"    4  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #46 java/lang/invoke/MethodHandles$Lookup, outer class info: #48 java/lang/invoke/MethodHandles\n" +
			"     inner name: #50 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 34 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#36 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
			"		#42 Main.<init>:(Ljava/lang/String;Ljava/lang/String;)V\n" +
			"		#44 (Ljava/lang/String;Ljava/lang/String;)LMain;\n" +
			"}";
	verifyClassFile(expected, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test007() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().referenceExpression.run();\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Runnable referenceExpression = () -> {" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    };\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"referenceExpression\"\n" +
			"    constant #6 utf8: \"Ljava/lang/Runnable;\"\n" +
			"    constant #7 utf8: \"<init>\"\n" +
			"    constant #8 utf8: \"()V\"\n" +
			"    constant #9 utf8: \"Code\"\n" +
			"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" +
			"    constant #11 name_and_type: #7.#8 <init> ()V\n" +
			"    constant #12 invoke dynamic: #0 #13 run ()Ljava/lang/Runnable;\n" +
			"    constant #13 name_and_type: #14.#15 run ()Ljava/lang/Runnable;\n" +
			"    constant #14 utf8: \"run\"\n" +
			"    constant #15 utf8: \"()Ljava/lang/Runnable;\"\n" +
			"    constant #16 field_ref: #1.#17 X.referenceExpression Ljava/lang/Runnable;\n" +
			"    constant #17 name_and_type: #5.#6 referenceExpression Ljava/lang/Runnable;\n" +
			"    constant #18 utf8: \"LineNumberTable\"\n" +
			"    constant #19 utf8: \"LocalVariableTable\"\n" +
			"    constant #20 utf8: \"this\"\n" +
			"    constant #21 utf8: \"LX;\"\n" +
			"    constant #22 utf8: \"lambda$0\"\n" +
			"    constant #23 field_ref: #24.#26 java/lang/System.out Ljava/io/PrintStream;\n" +
			"    constant #24 class: #25 java/lang/System\n" +
			"    constant #25 utf8: \"java/lang/System\"\n" +
			"    constant #26 name_and_type: #27.#28 out Ljava/io/PrintStream;\n" +
			"    constant #27 utf8: \"out\"\n" +
			"    constant #28 utf8: \"Ljava/io/PrintStream;\"\n" +
			"    constant #29 string: #30 \"SUCCESS\"\n" +
			"    constant #30 utf8: \"SUCCESS\"\n" +
			"    constant #31 method_ref: #32.#34 java/io/PrintStream.println (Ljava/lang/String;)V\n" +
			"    constant #32 class: #33 java/io/PrintStream\n" +
			"    constant #33 utf8: \"java/io/PrintStream\"\n" +
			"    constant #34 name_and_type: #35.#36 println (Ljava/lang/String;)V\n" +
			"    constant #35 utf8: \"println\"\n" +
			"    constant #36 utf8: \"(Ljava/lang/String;)V\"\n" +
			"    constant #37 utf8: \"SourceFile\"\n" +
			"    constant #38 utf8: \"X.java\"\n" +
			"    constant #39 utf8: \"BootstrapMethods\"\n" +
			"    constant #40 method_ref: #41.#43 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #41 class: #42 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #42 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #43 name_and_type: #44.#45 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #44 utf8: \"metafactory\"\n" +
			"    constant #45 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #46 method handle: invokestatic (6) #40 \n" +
			"    constant #47 method type: #8 ()V\n" +
			"    constant #48 method_ref: #1.#49 X.lambda$0 ()V\n" +
			"    constant #49 name_and_type: #22.#8 lambda$0 ()V\n" +
			"    constant #50 method handle: invokestatic (6) #48 \n" +
			"    constant #51 method type: #8 ()V\n" +
			"    constant #52 utf8: \"InnerClasses\"\n" +
			"    constant #53 class: #54 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #54 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #55 class: #56 java/lang/invoke/MethodHandles\n" +
			"    constant #56 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #57 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 Ljava/lang/Runnable;\n" +
			"  public java.lang.Runnable referenceExpression;\n" +
			"  \n" +
			"  // Method descriptor #8 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  public X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [10]\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokedynamic 0 run() : java.lang.Runnable [12]\n" +
			"    10  putfield X.referenceExpression : java.lang.Runnable [16]\n" +
			"    13  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"        [pc: 4, line: 2]\n" +
			"        [pc: 13, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #8 ()V\n" +
			"  // Stack: 2, Locals: 0\n" +
			"  private static synthetic void lambda$0();\n" +
			"    0  getstatic java.lang.System.out : java.io.PrintStream [23]\n" +
			"    3  ldc <String \"SUCCESS\"> [29]\n" +
			"    5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [31]\n" +
			"    8  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 2]\n" +
			"        [pc: 8, line: 3]\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #53 java/lang/invoke/MethodHandles$Lookup, outer class info: #55 java/lang/invoke/MethodHandles\n" +
			"     inner name: #57 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 46 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#47 ()V\n" +
			"		#50 invokestatic X.lambda$0:()V\n" +
			"		#51 ()V\n" +
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test007a() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().referenceExpression.run();\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Runnable referenceExpression = () -> System.out.println(\"SUCCESS\");\n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"referenceExpression\"\n" +
			"    constant #6 utf8: \"Ljava/lang/Runnable;\"\n" +
			"    constant #7 utf8: \"<init>\"\n" +
			"    constant #8 utf8: \"()V\"\n" +
			"    constant #9 utf8: \"Code\"\n" +
			"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" +
			"    constant #11 name_and_type: #7.#8 <init> ()V\n" +
			"    constant #12 invoke dynamic: #0 #13 run ()Ljava/lang/Runnable;\n" +
			"    constant #13 name_and_type: #14.#15 run ()Ljava/lang/Runnable;\n" +
			"    constant #14 utf8: \"run\"\n" +
			"    constant #15 utf8: \"()Ljava/lang/Runnable;\"\n" +
			"    constant #16 field_ref: #1.#17 X.referenceExpression Ljava/lang/Runnable;\n" +
			"    constant #17 name_and_type: #5.#6 referenceExpression Ljava/lang/Runnable;\n" +
			"    constant #18 utf8: \"LineNumberTable\"\n" +
			"    constant #19 utf8: \"LocalVariableTable\"\n" +
			"    constant #20 utf8: \"this\"\n" +
			"    constant #21 utf8: \"LX;\"\n" +
			"    constant #22 utf8: \"lambda$0\"\n" +
			"    constant #23 field_ref: #24.#26 java/lang/System.out Ljava/io/PrintStream;\n" +
			"    constant #24 class: #25 java/lang/System\n" +
			"    constant #25 utf8: \"java/lang/System\"\n" +
			"    constant #26 name_and_type: #27.#28 out Ljava/io/PrintStream;\n" +
			"    constant #27 utf8: \"out\"\n" +
			"    constant #28 utf8: \"Ljava/io/PrintStream;\"\n" +
			"    constant #29 string: #30 \"SUCCESS\"\n" +
			"    constant #30 utf8: \"SUCCESS\"\n" +
			"    constant #31 method_ref: #32.#34 java/io/PrintStream.println (Ljava/lang/String;)V\n" +
			"    constant #32 class: #33 java/io/PrintStream\n" +
			"    constant #33 utf8: \"java/io/PrintStream\"\n" +
			"    constant #34 name_and_type: #35.#36 println (Ljava/lang/String;)V\n" +
			"    constant #35 utf8: \"println\"\n" +
			"    constant #36 utf8: \"(Ljava/lang/String;)V\"\n" +
			"    constant #37 utf8: \"SourceFile\"\n" +
			"    constant #38 utf8: \"X.java\"\n" +
			"    constant #39 utf8: \"BootstrapMethods\"\n" +
			"    constant #40 method_ref: #41.#43 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #41 class: #42 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #42 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #43 name_and_type: #44.#45 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #44 utf8: \"metafactory\"\n" +
			"    constant #45 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #46 method handle: invokestatic (6) #40 \n" +
			"    constant #47 method type: #8 ()V\n" +
			"    constant #48 method_ref: #1.#49 X.lambda$0 ()V\n" +
			"    constant #49 name_and_type: #22.#8 lambda$0 ()V\n" +
			"    constant #50 method handle: invokestatic (6) #48 \n" +
			"    constant #51 method type: #8 ()V\n" +
			"    constant #52 utf8: \"InnerClasses\"\n" +
			"    constant #53 class: #54 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #54 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #55 class: #56 java/lang/invoke/MethodHandles\n" +
			"    constant #56 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #57 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 Ljava/lang/Runnable;\n" +
			"  public java.lang.Runnable referenceExpression;\n" +
			"  \n" +
			"  // Method descriptor #8 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  public X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [10]\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokedynamic 0 run() : java.lang.Runnable [12]\n" +
			"    10  putfield X.referenceExpression : java.lang.Runnable [16]\n" +
			"    13  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"        [pc: 4, line: 2]\n" +
			"        [pc: 13, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #8 ()V\n" +
			"  // Stack: 2, Locals: 0\n" +
			"  private static synthetic void lambda$0();\n" +
			"    0  getstatic java.lang.System.out : java.io.PrintStream [23]\n" +
			"    3  ldc <String \"SUCCESS\"> [29]\n" +
			"    5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [31]\n" +
			"    8  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 2]\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #53 java/lang/invoke/MethodHandles$Lookup, outer class info: #55 java/lang/invoke/MethodHandles\n" +
			"     inner name: #57 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 46 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#47 ()V\n" +
			"		#50 invokestatic X.lambda$0:()V\n" +
			"		#51 ()V\n" +
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test008() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().lambda.get());\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public java.util.function.Supplier<String> lambda = () -> { return \"SUCCESS\"; }; \n" +
			"}\n",
		},
	"SUCCESS"
	);

	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"lambda\"\n" +
			"    constant #6 utf8: \"Ljava/util/function/Supplier;\"\n" +
			"    constant #7 utf8: \"Signature\"\n" +
			"    constant #8 utf8: \"Ljava/util/function/Supplier<Ljava/lang/String;>;\"\n" +
			"    constant #9 utf8: \"<init>\"\n" +
			"    constant #10 utf8: \"()V\"\n" +
			"    constant #11 utf8: \"Code\"\n" +
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" +
			"    constant #13 name_and_type: #9.#10 <init> ()V\n" +
			"    constant #14 invoke dynamic: #0 #15 get ()Ljava/util/function/Supplier;\n" +
			"    constant #15 name_and_type: #16.#17 get ()Ljava/util/function/Supplier;\n" +
			"    constant #16 utf8: \"get\"\n" +
			"    constant #17 utf8: \"()Ljava/util/function/Supplier;\"\n" +
			"    constant #18 field_ref: #1.#19 X.lambda Ljava/util/function/Supplier;\n" +
			"    constant #19 name_and_type: #5.#6 lambda Ljava/util/function/Supplier;\n" +
			"    constant #20 utf8: \"LineNumberTable\"\n" +
			"    constant #21 utf8: \"LocalVariableTable\"\n" +
			"    constant #22 utf8: \"this\"\n" +
			"    constant #23 utf8: \"LX;\"\n" +
			"    constant #24 utf8: \"lambda$0\"\n" +
			"    constant #25 utf8: \"()Ljava/lang/String;\"\n" +
			"    constant #26 string: #27 \"SUCCESS\"\n" +
			"    constant #27 utf8: \"SUCCESS\"\n" +
			"    constant #28 utf8: \"SourceFile\"\n" +
			"    constant #29 utf8: \"X.java\"\n" +
			"    constant #30 utf8: \"BootstrapMethods\"\n" +
			"    constant #31 method_ref: #32.#34 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #32 class: #33 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #33 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #34 name_and_type: #35.#36 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #35 utf8: \"metafactory\"\n" +
			"    constant #36 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #37 method handle: invokestatic (6) #31 \n" +
			"    constant #38 utf8: \"()Ljava/lang/Object;\"\n" +
			"    constant #39 method type: #38 ()Ljava/lang/Object;\n" +
			"    constant #40 method_ref: #1.#41 X.lambda$0 ()Ljava/lang/String;\n" +
			"    constant #41 name_and_type: #24.#25 lambda$0 ()Ljava/lang/String;\n" +
			"    constant #42 method handle: invokestatic (6) #40 \n" +
			"    constant #43 method type: #25 ()Ljava/lang/String;\n" +
			"    constant #44 utf8: \"InnerClasses\"\n" +
			"    constant #45 class: #46 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #46 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #47 class: #48 java/lang/invoke/MethodHandles\n" +
			"    constant #48 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #49 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 Ljava/util/function/Supplier;\n" +
			"  // Signature: Ljava/util/function/Supplier<Ljava/lang/String;>;\n" +
			"  public java.util.function.Supplier lambda;\n" +
			"  \n" +
			"  // Method descriptor #10 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  public X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [12]\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokedynamic 0 get() : java.util.function.Supplier [14]\n" +
			"    10  putfield X.lambda : java.util.function.Supplier [18]\n" +
			"    13  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"        [pc: 4, line: 2]\n" +
			"        [pc: 13, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #25 ()Ljava/lang/String;\n" +
			"  // Stack: 1, Locals: 0\n" +
			"  private static synthetic java.lang.String lambda$0();\n" +
			"    0  ldc <String \"SUCCESS\"> [26]\n" +
			"    2  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 2]\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #45 java/lang/invoke/MethodHandles$Lookup, outer class info: #47 java/lang/invoke/MethodHandles\n" +
			"     inner name: #49 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 37 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#39 ()Ljava/lang/Object;\n" +
			"		#42 invokestatic X.lambda$0:()Ljava/lang/String;\n" +
			"		#43 ()Ljava/lang/String;\n" +
			"}"
;

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test009() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().concat.apply(\"SUCC\",\"ESS\"));\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Function2<String, String, String> concat = (s1, s2) -> { return s1 + s2; }; \n" +
			"}\n",
			"Function2.java",
			"public interface Function2<R, T1, T2> {\n" +
			"    R apply(T1 a1, T2 a2);\n" +
			"}\n",

		},
	"SUCCESS"
	);

	String expectedOutput = this.complianceLevel < ClassFileConstants.JDK9 ?
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"concat\"\n" +
			"    constant #6 utf8: \"LFunction2;\"\n" +
			"    constant #7 utf8: \"Signature\"\n" +
			"    constant #8 utf8: \"LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\"\n" +
			"    constant #9 utf8: \"<init>\"\n" +
			"    constant #10 utf8: \"()V\"\n" +
			"    constant #11 utf8: \"Code\"\n" +
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" +
			"    constant #13 name_and_type: #9.#10 <init> ()V\n" +
			"    constant #14 invoke dynamic: #0 #15 apply ()LFunction2;\n" +
			"    constant #15 name_and_type: #16.#17 apply ()LFunction2;\n" +
			"    constant #16 utf8: \"apply\"\n" +
			"    constant #17 utf8: \"()LFunction2;\"\n" +
			"    constant #18 field_ref: #1.#19 X.concat LFunction2;\n" +
			"    constant #19 name_and_type: #5.#6 concat LFunction2;\n" +
			"    constant #20 utf8: \"LineNumberTable\"\n" +
			"    constant #21 utf8: \"LocalVariableTable\"\n" +
			"    constant #22 utf8: \"this\"\n" +
			"    constant #23 utf8: \"LX;\"\n" +
			"    constant #24 utf8: \"lambda$0\"\n" +
			"    constant #25 utf8: \"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" +
			"    constant #26 class: #27 java/lang/StringBuilder\n" +
			"    constant #27 utf8: \"java/lang/StringBuilder\"\n" +
			"    constant #28 method_ref: #29.#31 java/lang/String.valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" +
			"    constant #29 class: #30 java/lang/String\n" +
			"    constant #30 utf8: \"java/lang/String\"\n" +
			"    constant #31 name_and_type: #32.#33 valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" +
			"    constant #32 utf8: \"valueOf\"\n" +
			"    constant #33 utf8: \"(Ljava/lang/Object;)Ljava/lang/String;\"\n" +
			"    constant #34 method_ref: #26.#35 java/lang/StringBuilder.<init> (Ljava/lang/String;)V\n" +
			"    constant #35 name_and_type: #9.#36 <init> (Ljava/lang/String;)V\n" +
			"    constant #36 utf8: \"(Ljava/lang/String;)V\"\n" +
			"    constant #37 method_ref: #26.#38 java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" +
			"    constant #38 name_and_type: #39.#40 append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" +
			"    constant #39 utf8: \"append\"\n" +
			"    constant #40 utf8: \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\"\n" +
			"    constant #41 method_ref: #26.#42 java/lang/StringBuilder.toString ()Ljava/lang/String;\n" +
			"    constant #42 name_and_type: #43.#44 toString ()Ljava/lang/String;\n" +
			"    constant #43 utf8: \"toString\"\n" +
			"    constant #44 utf8: \"()Ljava/lang/String;\"\n" +
			"    constant #45 utf8: \"s1\"\n" +
			"    constant #46 utf8: \"Ljava/lang/String;\"\n" +
			"    constant #47 utf8: \"s2\"\n" +
			"    constant #48 utf8: \"SourceFile\"\n" +
			"    constant #49 utf8: \"X.java\"\n" +
			"    constant #50 utf8: \"BootstrapMethods\"\n" +
			"    constant #51 method_ref: #52.#54 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #52 class: #53 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #53 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #54 name_and_type: #55.#56 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #55 utf8: \"metafactory\"\n" +
			"    constant #56 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #57 method handle: invokestatic (6) #51 \n" +
			"    constant #58 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" +
			"    constant #59 method type: #58 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
			"    constant #60 method_ref: #1.#61 X.lambda$0 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"    constant #61 name_and_type: #24.#25 lambda$0 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"    constant #62 method handle: invokestatic (6) #60 \n" +
			"    constant #63 method type: #25 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"    constant #64 utf8: \"InnerClasses\"\n" +
			"    constant #65 class: #66 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #66 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #67 class: #68 java/lang/invoke/MethodHandles\n" +
			"    constant #68 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #69 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 LFunction2;\n" +
			"  // Signature: LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\n" +
			"  public Function2 concat;\n" +
			"  \n" +
			"  // Method descriptor #10 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  public X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [12]\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokedynamic 0 apply() : Function2 [14]\n" +
			"    10  putfield X.concat : Function2 [18]\n" +
			"    13  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"        [pc: 4, line: 2]\n" +
			"        [pc: 13, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #25 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"  // Stack: 3, Locals: 2\n" +
			"  private static synthetic java.lang.String lambda$0(java.lang.String s1, java.lang.String s2);\n" +
			"     0  new java.lang.StringBuilder [26]\n" +
			"     3  dup\n" +
			"     4  aload_0 [s1]\n" +
			"     5  invokestatic java.lang.String.valueOf(java.lang.Object) : java.lang.String [28]\n" +
			"     8  invokespecial java.lang.StringBuilder(java.lang.String) [34]\n" +
			"    11  aload_1 [s2]\n" +
			"    12  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [37]\n" +
			"    15  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [41]\n" +
			"    18  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 2]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 19] local: s1 index: 0 type: java.lang.String\n" +
			"        [pc: 0, pc: 19] local: s2 index: 1 type: java.lang.String\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #65 java/lang/invoke/MethodHandles$Lookup, outer class info: #67 java/lang/invoke/MethodHandles\n" +
			"     inner name: #69 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 57 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#59 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
			"		#62 invokestatic X.lambda$0:(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"		#63 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"}" :
				"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
				"public class X {\n" +
				"  Constant pool:\n" +
				"    constant #1 class: #2 X\n" +
				"    constant #2 utf8: \"X\"\n" +
				"    constant #3 class: #4 java/lang/Object\n" +
				"    constant #4 utf8: \"java/lang/Object\"\n" +
				"    constant #5 utf8: \"concat\"\n" +
				"    constant #6 utf8: \"LFunction2;\"\n" +
				"    constant #7 utf8: \"Signature\"\n" +
				"    constant #8 utf8: \"LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\"\n" +
				"    constant #9 utf8: \"<init>\"\n" +
				"    constant #10 utf8: \"()V\"\n" +
				"    constant #11 utf8: \"Code\"\n" +
				"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" +
				"    constant #13 name_and_type: #9.#10 <init> ()V\n" +
				"    constant #14 invoke dynamic: #0 #15 apply ()LFunction2;\n" +
				"    constant #15 name_and_type: #16.#17 apply ()LFunction2;\n" +
				"    constant #16 utf8: \"apply\"\n" +
				"    constant #17 utf8: \"()LFunction2;\"\n" +
				"    constant #18 field_ref: #1.#19 X.concat LFunction2;\n" +
				"    constant #19 name_and_type: #5.#6 concat LFunction2;\n" +
				"    constant #20 utf8: \"LineNumberTable\"\n" +
				"    constant #21 utf8: \"LocalVariableTable\"\n" +
				"    constant #22 utf8: \"this\"\n" +
				"    constant #23 utf8: \"LX;\"\n" +
				"    constant #24 utf8: \"lambda$0\"\n" +
				"    constant #25 utf8: \"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" +
				"    constant #26 invoke dynamic: #1 #27 makeConcatWithConstants (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #27 name_and_type: #28.#25 makeConcatWithConstants (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #28 utf8: \"makeConcatWithConstants\"\n" +
				"    constant #29 utf8: \"s1\"\n" +
				"    constant #30 utf8: \"Ljava/lang/String;\"\n" +
				"    constant #31 utf8: \"s2\"\n" +
				"    constant #32 utf8: \"SourceFile\"\n" +
				"    constant #33 utf8: \"X.java\"\n" +
				"    constant #34 utf8: \"BootstrapMethods\"\n" +
				"    constant #35 method_ref: #36.#38 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #36 class: #37 java/lang/invoke/LambdaMetafactory\n" +
				"    constant #37 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
				"    constant #38 name_and_type: #39.#40 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #39 utf8: \"metafactory\"\n" +
				"    constant #40 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
				"    constant #41 method handle: invokestatic (6) #35 \n" +
				"    constant #42 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" +
				"    constant #43 method type: #42 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
				"    constant #44 method_ref: #1.#45 X.lambda$0 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #45 name_and_type: #24.#25 lambda$0 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #46 method handle: invokestatic (6) #44 \n" +
				"    constant #47 method type: #25 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #48 method_ref: #49.#51 java/lang/invoke/StringConcatFactory.makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #49 class: #50 java/lang/invoke/StringConcatFactory\n" +
				"    constant #50 utf8: \"java/lang/invoke/StringConcatFactory\"\n" +
				"    constant #51 name_and_type: #28.#52 makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #52 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\"\n" +
				"    constant #53 method handle: invokestatic (6) #48 \n" +
				"    constant #54 string: #55 \"\\u0001\\u0001\"\n" +
				"    constant #55 utf8: \"\\u0001\\u0001\"\n" +
				"    constant #56 utf8: \"InnerClasses\"\n" +
				"    constant #57 class: #58 java/lang/invoke/MethodHandles$Lookup\n" +
				"    constant #58 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
				"    constant #59 class: #60 java/lang/invoke/MethodHandles\n" +
				"    constant #60 utf8: \"java/lang/invoke/MethodHandles\"\n" +
				"    constant #61 utf8: \"Lookup\"\n" +
				"  \n" +
				"  // Field descriptor #6 LFunction2;\n" +
				"  // Signature: LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\n" +
				"  public Function2 concat;\n" +
				"  \n" +
				"  // Method descriptor #10 ()V\n" +
				"  // Stack: 2, Locals: 1\n" +
				"  public X();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokespecial java.lang.Object() [12]\n" +
				"     4  aload_0 [this]\n" +
				"     5  invokedynamic 0 apply() : Function2 [14]\n" +
				"    10  putfield X.concat : Function2 [18]\n" +
				"    13  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"        [pc: 4, line: 2]\n" +
				"        [pc: 13, line: 1]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #25 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"  // Stack: 3, Locals: 2\n" +
				"  private static synthetic java.lang.String lambda$0(java.lang.String s1, java.lang.String s2);\n" +
				"    0  aload_0 [s1]\n" +
				"    1  aload_1 [s2]\n" +
				"    2  invokedynamic 1 makeConcatWithConstants(java.lang.String, java.lang.String) : java.lang.String [26]\n" +
				"    7  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 2]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 8] local: s1 index: 0 type: java.lang.String\n" +
				"        [pc: 0, pc: 8] local: s2 index: 1 type: java.lang.String\n" +
				"\n" +
				"  Inner classes:\n" +
				"    [inner class info: #57 java/lang/invoke/MethodHandles$Lookup, outer class info: #59 java/lang/invoke/MethodHandles\n" +
				"     inner name: #61 Lookup, accessflags: 25 public static final]\n" +
				"Bootstrap methods:\n" +
				"  0 : # 41 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
				"	Method arguments:\n" +
				"		#43 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
				"		#46 invokestatic X.lambda$0:(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"		#47 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;,\n" +
				"  1 : # 53 invokestatic java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"	Method arguments:\n" +
				"		#54 \n" +
				"}";
	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test010() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().concat.apply(\"UCC\",\"ESS\"));\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Function2<String, String, String> concat; \n" +
			"    {\n" +
			"        String s0 = new String(\"S\");\n" +
			"        concat = (s1, s2) -> { return s0 + s1 + s2; }; \n" +
			"    }\n" +
			"}\n",
			"Function2.java",
			"public interface Function2<R, T1, T2> {\n" +
			"    R apply(T1 a1, T2 a2);\n" +
			"}\n",

		},
	"SUCCESS"
	);

	String expectedOutput = this.complianceLevel < ClassFileConstants.JDK9 ?
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"concat\"\n" +
			"    constant #6 utf8: \"LFunction2;\"\n" +
			"    constant #7 utf8: \"Signature\"\n" +
			"    constant #8 utf8: \"LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\"\n" +
			"    constant #9 utf8: \"<init>\"\n" +
			"    constant #10 utf8: \"()V\"\n" +
			"    constant #11 utf8: \"Code\"\n" +
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" +
			"    constant #13 name_and_type: #9.#10 <init> ()V\n" +
			"    constant #14 class: #15 java/lang/String\n" +
			"    constant #15 utf8: \"java/lang/String\"\n" +
			"    constant #16 string: #17 \"S\"\n" +
			"    constant #17 utf8: \"S\"\n" +
			"    constant #18 method_ref: #14.#19 java/lang/String.<init> (Ljava/lang/String;)V\n" +
			"    constant #19 name_and_type: #9.#20 <init> (Ljava/lang/String;)V\n" +
			"    constant #20 utf8: \"(Ljava/lang/String;)V\"\n" +
			"    constant #21 invoke dynamic: #0 #22 apply (Ljava/lang/String;)LFunction2;\n" +
			"    constant #22 name_and_type: #23.#24 apply (Ljava/lang/String;)LFunction2;\n" +
			"    constant #23 utf8: \"apply\"\n" +
			"    constant #24 utf8: \"(Ljava/lang/String;)LFunction2;\"\n" +
			"    constant #25 field_ref: #1.#26 X.concat LFunction2;\n" +
			"    constant #26 name_and_type: #5.#6 concat LFunction2;\n" +
			"    constant #27 utf8: \"LineNumberTable\"\n" +
			"    constant #28 utf8: \"LocalVariableTable\"\n" +
			"    constant #29 utf8: \"this\"\n" +
			"    constant #30 utf8: \"LX;\"\n" +
			"    constant #31 utf8: \"s0\"\n" +
			"    constant #32 utf8: \"Ljava/lang/String;\"\n" +
			"    constant #33 utf8: \"lambda$0\"\n" +
			"    constant #34 utf8: \"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" +
			"    constant #35 class: #36 java/lang/StringBuilder\n" +
			"    constant #36 utf8: \"java/lang/StringBuilder\"\n" +
			"    constant #37 method_ref: #14.#38 java/lang/String.valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" +
			"    constant #38 name_and_type: #39.#40 valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" +
			"    constant #39 utf8: \"valueOf\"\n" +
			"    constant #40 utf8: \"(Ljava/lang/Object;)Ljava/lang/String;\"\n" +
			"    constant #41 method_ref: #35.#19 java/lang/StringBuilder.<init> (Ljava/lang/String;)V\n" +
			"    constant #42 method_ref: #35.#43 java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" +
			"    constant #43 name_and_type: #44.#45 append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" +
			"    constant #44 utf8: \"append\"\n" +
			"    constant #45 utf8: \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\"\n" +
			"    constant #46 method_ref: #35.#47 java/lang/StringBuilder.toString ()Ljava/lang/String;\n" +
			"    constant #47 name_and_type: #48.#49 toString ()Ljava/lang/String;\n" +
			"    constant #48 utf8: \"toString\"\n" +
			"    constant #49 utf8: \"()Ljava/lang/String;\"\n" +
			"    constant #50 utf8: \"s1\"\n" +
			"    constant #51 utf8: \"s2\"\n" +
			"    constant #52 utf8: \"SourceFile\"\n" +
			"    constant #53 utf8: \"X.java\"\n" +
			"    constant #54 utf8: \"BootstrapMethods\"\n" +
			"    constant #55 method_ref: #56.#58 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #56 class: #57 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #57 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #58 name_and_type: #59.#60 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #59 utf8: \"metafactory\"\n" +
			"    constant #60 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #61 method handle: invokestatic (6) #55 \n" +
			"    constant #62 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" +
			"    constant #63 method type: #62 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
			"    constant #64 method_ref: #1.#65 X.lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"    constant #65 name_and_type: #33.#34 lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"    constant #66 method handle: invokestatic (6) #64 \n" +
			"    constant #67 utf8: \"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" +
			"    constant #68 method type: #67 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"    constant #69 utf8: \"InnerClasses\"\n" +
			"    constant #70 class: #71 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #71 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #72 class: #73 java/lang/invoke/MethodHandles\n" +
			"    constant #73 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #74 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 LFunction2;\n" +
			"  // Signature: LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\n" +
			"  public Function2 concat;\n" +
			"  \n" +
			"  // Method descriptor #10 ()V\n" +
			"  // Stack: 3, Locals: 2\n" +
			"  public X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [12]\n" +
			"     4  new java.lang.String [14]\n" +
			"     7  dup\n" +
			"     8  ldc <String \"S\"> [16]\n" +
			"    10  invokespecial java.lang.String(java.lang.String) [18]\n" +
			"    13  astore_1 [s0]\n" +
			"    14  aload_0 [this]\n" +
			"    15  aload_1 [s0]\n" +
			"    16  invokedynamic 0 apply(java.lang.String) : Function2 [21]\n" +
			"    21  putfield X.concat : Function2 [25]\n" +
			"    24  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"        [pc: 4, line: 4]\n" +
			"        [pc: 14, line: 5]\n" +
			"        [pc: 24, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 25] local: this index: 0 type: X\n" +
			"        [pc: 14, pc: 24] local: s0 index: 1 type: java.lang.String\n" +
			"  \n" +
			"  // Method descriptor #34 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"  // Stack: 3, Locals: 3\n" +
			"  private static synthetic java.lang.String lambda$0(java.lang.String arg0, java.lang.String s1, java.lang.String s2);\n" +
			"     0  new java.lang.StringBuilder [35]\n" +
			"     3  dup\n" +
			"     4  aload_0 [arg0]\n" +
			"     5  invokestatic java.lang.String.valueOf(java.lang.Object) : java.lang.String [37]\n" +
			"     8  invokespecial java.lang.StringBuilder(java.lang.String) [41]\n" +
			"    11  aload_1 [s1]\n" +
			"    12  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [42]\n" +
			"    15  aload_2 [s2]\n" +
			"    16  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [42]\n" +
			"    19  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [46]\n" +
			"    22  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 23] local: s1 index: 1 type: java.lang.String\n" +
			"        [pc: 0, pc: 23] local: s2 index: 2 type: java.lang.String\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #70 java/lang/invoke/MethodHandles$Lookup, outer class info: #72 java/lang/invoke/MethodHandles\n" +
			"     inner name: #74 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 61 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#63 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
			"		#66 invokestatic X.lambda$0:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"		#68 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"}"
			:
				"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
				"public class X {\n" +
				"  Constant pool:\n" +
				"    constant #1 class: #2 X\n" +
				"    constant #2 utf8: \"X\"\n" +
				"    constant #3 class: #4 java/lang/Object\n" +
				"    constant #4 utf8: \"java/lang/Object\"\n" +
				"    constant #5 utf8: \"concat\"\n" +
				"    constant #6 utf8: \"LFunction2;\"\n" +
				"    constant #7 utf8: \"Signature\"\n" +
				"    constant #8 utf8: \"LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\"\n" +
				"    constant #9 utf8: \"<init>\"\n" +
				"    constant #10 utf8: \"()V\"\n" +
				"    constant #11 utf8: \"Code\"\n" +
				"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" +
				"    constant #13 name_and_type: #9.#10 <init> ()V\n" +
				"    constant #14 class: #15 java/lang/String\n" +
				"    constant #15 utf8: \"java/lang/String\"\n" +
				"    constant #16 string: #17 \"S\"\n" +
				"    constant #17 utf8: \"S\"\n" +
				"    constant #18 method_ref: #14.#19 java/lang/String.<init> (Ljava/lang/String;)V\n" +
				"    constant #19 name_and_type: #9.#20 <init> (Ljava/lang/String;)V\n" +
				"    constant #20 utf8: \"(Ljava/lang/String;)V\"\n" +
				"    constant #21 invoke dynamic: #0 #22 apply (Ljava/lang/String;)LFunction2;\n" +
				"    constant #22 name_and_type: #23.#24 apply (Ljava/lang/String;)LFunction2;\n" +
				"    constant #23 utf8: \"apply\"\n" +
				"    constant #24 utf8: \"(Ljava/lang/String;)LFunction2;\"\n" +
				"    constant #25 field_ref: #1.#26 X.concat LFunction2;\n" +
				"    constant #26 name_and_type: #5.#6 concat LFunction2;\n" +
				"    constant #27 utf8: \"LineNumberTable\"\n" +
				"    constant #28 utf8: \"LocalVariableTable\"\n" +
				"    constant #29 utf8: \"this\"\n" +
				"    constant #30 utf8: \"LX;\"\n" +
				"    constant #31 utf8: \"s0\"\n" +
				"    constant #32 utf8: \"Ljava/lang/String;\"\n" +
				"    constant #33 utf8: \"lambda$0\"\n" +
				"    constant #34 utf8: \"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" +
				"    constant #35 invoke dynamic: #1 #36 makeConcatWithConstants (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #36 name_and_type: #37.#34 makeConcatWithConstants (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #37 utf8: \"makeConcatWithConstants\"\n" +
				"    constant #38 utf8: \"s1\"\n" +
				"    constant #39 utf8: \"s2\"\n" +
				"    constant #40 utf8: \"SourceFile\"\n" +
				"    constant #41 utf8: \"X.java\"\n" +
				"    constant #42 utf8: \"BootstrapMethods\"\n" +
				"    constant #43 method_ref: #44.#46 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #44 class: #45 java/lang/invoke/LambdaMetafactory\n" +
				"    constant #45 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
				"    constant #46 name_and_type: #47.#48 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #47 utf8: \"metafactory\"\n" +
				"    constant #48 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
				"    constant #49 method handle: invokestatic (6) #43 \n" +
				"    constant #50 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" +
				"    constant #51 method type: #50 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
				"    constant #52 method_ref: #1.#53 X.lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #53 name_and_type: #33.#34 lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #54 method handle: invokestatic (6) #52 \n" +
				"    constant #55 utf8: \"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" +
				"    constant #56 method type: #55 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #57 method_ref: #58.#60 java/lang/invoke/StringConcatFactory.makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #58 class: #59 java/lang/invoke/StringConcatFactory\n" +
				"    constant #59 utf8: \"java/lang/invoke/StringConcatFactory\"\n" +
				"    constant #60 name_and_type: #37.#61 makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #61 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\"\n" +
				"    constant #62 method handle: invokestatic (6) #57 \n" +
				"    constant #63 string: #64 \"\\u0001\\u0001\\u0001\"\n" +
				"    constant #64 utf8: \"\\u0001\\u0001\\u0001\"\n" +
				"    constant #65 utf8: \"InnerClasses\"\n" +
				"    constant #66 class: #67 java/lang/invoke/MethodHandles$Lookup\n" +
				"    constant #67 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
				"    constant #68 class: #69 java/lang/invoke/MethodHandles\n" +
				"    constant #69 utf8: \"java/lang/invoke/MethodHandles\"\n" +
				"    constant #70 utf8: \"Lookup\"\n" +
				"  \n" +
				"  // Field descriptor #6 LFunction2;\n" +
				"  // Signature: LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\n" +
				"  public Function2 concat;\n" +
				"  \n" +
				"  // Method descriptor #10 ()V\n" +
				"  // Stack: 3, Locals: 2\n" +
				"  public X();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokespecial java.lang.Object() [12]\n" +
				"     4  new java.lang.String [14]\n" +
				"     7  dup\n" +
				"     8  ldc <String \"S\"> [16]\n" +
				"    10  invokespecial java.lang.String(java.lang.String) [18]\n" +
				"    13  astore_1 [s0]\n" +
				"    14  aload_0 [this]\n" +
				"    15  aload_1 [s0]\n" +
				"    16  invokedynamic 0 apply(java.lang.String) : Function2 [21]\n" +
				"    21  putfield X.concat : Function2 [25]\n" +
				"    24  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"        [pc: 4, line: 4]\n" +
				"        [pc: 14, line: 5]\n" +
				"        [pc: 24, line: 1]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 25] local: this index: 0 type: X\n" +
				"        [pc: 14, pc: 24] local: s0 index: 1 type: java.lang.String\n" +
				"  \n" +
				"  // Method descriptor #34 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"  // Stack: 4, Locals: 3\n" +
				"  private static synthetic java.lang.String lambda$0(java.lang.String arg0, java.lang.String s1, java.lang.String s2);\n" +
				"    0  aload_0 [arg0]\n" +
				"    1  aload_1 [s1]\n" +
				"    2  aload_2 [s2]\n" +
				"    3  invokedynamic 1 makeConcatWithConstants(java.lang.String, java.lang.String, java.lang.String) : java.lang.String [35]\n" +
				"    8  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 9] local: s1 index: 1 type: java.lang.String\n" +
				"        [pc: 0, pc: 9] local: s2 index: 2 type: java.lang.String\n" +
				"\n" +
				"  Inner classes:\n" +
				"    [inner class info: #66 java/lang/invoke/MethodHandles$Lookup, outer class info: #68 java/lang/invoke/MethodHandles\n" +
				"     inner name: #70 Lookup, accessflags: 25 public static final]\n" +
				"Bootstrap methods:\n" +
				"  0 : # 49 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
				"	Method arguments:\n" +
				"		#51 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
				"		#54 invokestatic X.lambda$0:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"		#56 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;,\n" +
				"  1 : # 62 invokestatic java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"	Method arguments:\n" +
				"		#63 \n" +
				"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test011() throws Exception {
	this.runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.println(new X().concat.apply(\"UCC\",\"ESS\"));\n" +
			"    }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    public Function2<String, String, String> concat; \n" +
			"    {\n" +
			"        String s0 = new String(\"S\");\n" +
			"        concat = (s1, s2) -> s0 + s1 + s2; \n" +
			"    }\n" +
			"}\n",
			"Function2.java",
			"public interface Function2<R, T1, T2> {\n" +
			"    R apply(T1 a1, T2 a2);\n" +
			"}\n",

		},
	"SUCCESS"
	);

	String expectedOutput = this.complianceLevel < ClassFileConstants.JDK9 ?
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"concat\"\n" +
			"    constant #6 utf8: \"LFunction2;\"\n" +
			"    constant #7 utf8: \"Signature\"\n" +
			"    constant #8 utf8: \"LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\"\n" +
			"    constant #9 utf8: \"<init>\"\n" +
			"    constant #10 utf8: \"()V\"\n" +
			"    constant #11 utf8: \"Code\"\n" +
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" +
			"    constant #13 name_and_type: #9.#10 <init> ()V\n" +
			"    constant #14 class: #15 java/lang/String\n" +
			"    constant #15 utf8: \"java/lang/String\"\n" +
			"    constant #16 string: #17 \"S\"\n" +
			"    constant #17 utf8: \"S\"\n" +
			"    constant #18 method_ref: #14.#19 java/lang/String.<init> (Ljava/lang/String;)V\n" +
			"    constant #19 name_and_type: #9.#20 <init> (Ljava/lang/String;)V\n" +
			"    constant #20 utf8: \"(Ljava/lang/String;)V\"\n" +
			"    constant #21 invoke dynamic: #0 #22 apply (Ljava/lang/String;)LFunction2;\n" +
			"    constant #22 name_and_type: #23.#24 apply (Ljava/lang/String;)LFunction2;\n" +
			"    constant #23 utf8: \"apply\"\n" +
			"    constant #24 utf8: \"(Ljava/lang/String;)LFunction2;\"\n" +
			"    constant #25 field_ref: #1.#26 X.concat LFunction2;\n" +
			"    constant #26 name_and_type: #5.#6 concat LFunction2;\n" +
			"    constant #27 utf8: \"LineNumberTable\"\n" +
			"    constant #28 utf8: \"LocalVariableTable\"\n" +
			"    constant #29 utf8: \"this\"\n" +
			"    constant #30 utf8: \"LX;\"\n" +
			"    constant #31 utf8: \"s0\"\n" +
			"    constant #32 utf8: \"Ljava/lang/String;\"\n" +
			"    constant #33 utf8: \"lambda$0\"\n" +
			"    constant #34 utf8: \"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" +
			"    constant #35 class: #36 java/lang/StringBuilder\n" +
			"    constant #36 utf8: \"java/lang/StringBuilder\"\n" +
			"    constant #37 method_ref: #14.#38 java/lang/String.valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" +
			"    constant #38 name_and_type: #39.#40 valueOf (Ljava/lang/Object;)Ljava/lang/String;\n" +
			"    constant #39 utf8: \"valueOf\"\n" +
			"    constant #40 utf8: \"(Ljava/lang/Object;)Ljava/lang/String;\"\n" +
			"    constant #41 method_ref: #35.#19 java/lang/StringBuilder.<init> (Ljava/lang/String;)V\n" +
			"    constant #42 method_ref: #35.#43 java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" +
			"    constant #43 name_and_type: #44.#45 append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" +
			"    constant #44 utf8: \"append\"\n" +
			"    constant #45 utf8: \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\"\n" +
			"    constant #46 method_ref: #35.#47 java/lang/StringBuilder.toString ()Ljava/lang/String;\n" +
			"    constant #47 name_and_type: #48.#49 toString ()Ljava/lang/String;\n" +
			"    constant #48 utf8: \"toString\"\n" +
			"    constant #49 utf8: \"()Ljava/lang/String;\"\n" +
			"    constant #50 utf8: \"s1\"\n" +
			"    constant #51 utf8: \"s2\"\n" +
			"    constant #52 utf8: \"SourceFile\"\n" +
			"    constant #53 utf8: \"X.java\"\n" +
			"    constant #54 utf8: \"BootstrapMethods\"\n" +
			"    constant #55 method_ref: #56.#58 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #56 class: #57 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #57 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #58 name_and_type: #59.#60 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #59 utf8: \"metafactory\"\n" +
			"    constant #60 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #61 method handle: invokestatic (6) #55 \n" +
			"    constant #62 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" +
			"    constant #63 method type: #62 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
			"    constant #64 method_ref: #1.#65 X.lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"    constant #65 name_and_type: #33.#34 lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"    constant #66 method handle: invokestatic (6) #64 \n" +
			"    constant #67 utf8: \"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" +
			"    constant #68 method type: #67 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"    constant #69 utf8: \"InnerClasses\"\n" +
			"    constant #70 class: #71 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #71 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #72 class: #73 java/lang/invoke/MethodHandles\n" +
			"    constant #73 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #74 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 LFunction2;\n" +
			"  // Signature: LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\n" +
			"  public Function2 concat;\n" +
			"  \n" +
			"  // Method descriptor #10 ()V\n" +
			"  // Stack: 3, Locals: 2\n" +
			"  public X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [12]\n" +
			"     4  new java.lang.String [14]\n" +
			"     7  dup\n" +
			"     8  ldc <String \"S\"> [16]\n" +
			"    10  invokespecial java.lang.String(java.lang.String) [18]\n" +
			"    13  astore_1 [s0]\n" +
			"    14  aload_0 [this]\n" +
			"    15  aload_1 [s0]\n" +
			"    16  invokedynamic 0 apply(java.lang.String) : Function2 [21]\n" +
			"    21  putfield X.concat : Function2 [25]\n" +
			"    24  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"        [pc: 4, line: 4]\n" +
			"        [pc: 14, line: 5]\n" +
			"        [pc: 24, line: 1]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 25] local: this index: 0 type: X\n" +
			"        [pc: 14, pc: 24] local: s0 index: 1 type: java.lang.String\n" +
			"  \n" +
			"  // Method descriptor #34 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"  // Stack: 3, Locals: 3\n" +
			"  private static synthetic java.lang.String lambda$0(java.lang.String arg0, java.lang.String s1, java.lang.String s2);\n" +
			"     0  new java.lang.StringBuilder [35]\n" +
			"     3  dup\n" +
			"     4  aload_0 [arg0]\n" +
			"     5  invokestatic java.lang.String.valueOf(java.lang.Object) : java.lang.String [37]\n" +
			"     8  invokespecial java.lang.StringBuilder(java.lang.String) [41]\n" +
			"    11  aload_1 [s1]\n" +
			"    12  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [42]\n" +
			"    15  aload_2 [s2]\n" +
			"    16  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [42]\n" +
			"    19  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [46]\n" +
			"    22  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 23] local: s1 index: 1 type: java.lang.String\n" +
			"        [pc: 0, pc: 23] local: s2 index: 2 type: java.lang.String\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #70 java/lang/invoke/MethodHandles$Lookup, outer class info: #72 java/lang/invoke/MethodHandles\n" +
			"     inner name: #74 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 61 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#63 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
			"		#66 invokestatic X.lambda$0:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"		#68 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
			"}"
			:
				"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
				"public class X {\n" +
				"  Constant pool:\n" +
				"    constant #1 class: #2 X\n" +
				"    constant #2 utf8: \"X\"\n" +
				"    constant #3 class: #4 java/lang/Object\n" +
				"    constant #4 utf8: \"java/lang/Object\"\n" +
				"    constant #5 utf8: \"concat\"\n" +
				"    constant #6 utf8: \"LFunction2;\"\n" +
				"    constant #7 utf8: \"Signature\"\n" +
				"    constant #8 utf8: \"LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\"\n" +
				"    constant #9 utf8: \"<init>\"\n" +
				"    constant #10 utf8: \"()V\"\n" +
				"    constant #11 utf8: \"Code\"\n" +
				"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" +
				"    constant #13 name_and_type: #9.#10 <init> ()V\n" +
				"    constant #14 class: #15 java/lang/String\n" +
				"    constant #15 utf8: \"java/lang/String\"\n" +
				"    constant #16 string: #17 \"S\"\n" +
				"    constant #17 utf8: \"S\"\n" +
				"    constant #18 method_ref: #14.#19 java/lang/String.<init> (Ljava/lang/String;)V\n" +
				"    constant #19 name_and_type: #9.#20 <init> (Ljava/lang/String;)V\n" +
				"    constant #20 utf8: \"(Ljava/lang/String;)V\"\n" +
				"    constant #21 invoke dynamic: #0 #22 apply (Ljava/lang/String;)LFunction2;\n" +
				"    constant #22 name_and_type: #23.#24 apply (Ljava/lang/String;)LFunction2;\n" +
				"    constant #23 utf8: \"apply\"\n" +
				"    constant #24 utf8: \"(Ljava/lang/String;)LFunction2;\"\n" +
				"    constant #25 field_ref: #1.#26 X.concat LFunction2;\n" +
				"    constant #26 name_and_type: #5.#6 concat LFunction2;\n" +
				"    constant #27 utf8: \"LineNumberTable\"\n" +
				"    constant #28 utf8: \"LocalVariableTable\"\n" +
				"    constant #29 utf8: \"this\"\n" +
				"    constant #30 utf8: \"LX;\"\n" +
				"    constant #31 utf8: \"s0\"\n" +
				"    constant #32 utf8: \"Ljava/lang/String;\"\n" +
				"    constant #33 utf8: \"lambda$0\"\n" +
				"    constant #34 utf8: \"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" +
				"    constant #35 invoke dynamic: #1 #36 makeConcatWithConstants (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #36 name_and_type: #37.#34 makeConcatWithConstants (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #37 utf8: \"makeConcatWithConstants\"\n" +
				"    constant #38 utf8: \"s1\"\n" +
				"    constant #39 utf8: \"s2\"\n" +
				"    constant #40 utf8: \"SourceFile\"\n" +
				"    constant #41 utf8: \"X.java\"\n" +
				"    constant #42 utf8: \"BootstrapMethods\"\n" +
				"    constant #43 method_ref: #44.#46 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #44 class: #45 java/lang/invoke/LambdaMetafactory\n" +
				"    constant #45 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
				"    constant #46 name_and_type: #47.#48 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #47 utf8: \"metafactory\"\n" +
				"    constant #48 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
				"    constant #49 method handle: invokestatic (6) #43 \n" +
				"    constant #50 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\"\n" +
				"    constant #51 method type: #50 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
				"    constant #52 method_ref: #1.#53 X.lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #53 name_and_type: #33.#34 lambda$0 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #54 method handle: invokestatic (6) #52 \n" +
				"    constant #55 utf8: \"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" +
				"    constant #56 method type: #55 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #57 method_ref: #58.#60 java/lang/invoke/StringConcatFactory.makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #58 class: #59 java/lang/invoke/StringConcatFactory\n" +
				"    constant #59 utf8: \"java/lang/invoke/StringConcatFactory\"\n" +
				"    constant #60 name_and_type: #37.#61 makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #61 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\"\n" +
				"    constant #62 method handle: invokestatic (6) #57 \n" +
				"    constant #63 string: #64 \"\\u0001\\u0001\\u0001\"\n" +
				"    constant #64 utf8: \"\\u0001\\u0001\\u0001\"\n" +
				"    constant #65 utf8: \"InnerClasses\"\n" +
				"    constant #66 class: #67 java/lang/invoke/MethodHandles$Lookup\n" +
				"    constant #67 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
				"    constant #68 class: #69 java/lang/invoke/MethodHandles\n" +
				"    constant #69 utf8: \"java/lang/invoke/MethodHandles\"\n" +
				"    constant #70 utf8: \"Lookup\"\n" +
				"  \n" +
				"  // Field descriptor #6 LFunction2;\n" +
				"  // Signature: LFunction2<Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;>;\n" +
				"  public Function2 concat;\n" +
				"  \n" +
				"  // Method descriptor #10 ()V\n" +
				"  // Stack: 3, Locals: 2\n" +
				"  public X();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokespecial java.lang.Object() [12]\n" +
				"     4  new java.lang.String [14]\n" +
				"     7  dup\n" +
				"     8  ldc <String \"S\"> [16]\n" +
				"    10  invokespecial java.lang.String(java.lang.String) [18]\n" +
				"    13  astore_1 [s0]\n" +
				"    14  aload_0 [this]\n" +
				"    15  aload_1 [s0]\n" +
				"    16  invokedynamic 0 apply(java.lang.String) : Function2 [21]\n" +
				"    21  putfield X.concat : Function2 [25]\n" +
				"    24  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"        [pc: 4, line: 4]\n" +
				"        [pc: 14, line: 5]\n" +
				"        [pc: 24, line: 1]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 25] local: this index: 0 type: X\n" +
				"        [pc: 14, pc: 24] local: s0 index: 1 type: java.lang.String\n" +
				"  \n" +
				"  // Method descriptor #34 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"  // Stack: 4, Locals: 3\n" +
				"  private static synthetic java.lang.String lambda$0(java.lang.String arg0, java.lang.String s1, java.lang.String s2);\n" +
				"    0  aload_0 [arg0]\n" +
				"    1  aload_1 [s1]\n" +
				"    2  aload_2 [s2]\n" +
				"    3  invokedynamic 1 makeConcatWithConstants(java.lang.String, java.lang.String, java.lang.String) : java.lang.String [35]\n" +
				"    8  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 9] local: s1 index: 1 type: java.lang.String\n" +
				"        [pc: 0, pc: 9] local: s2 index: 2 type: java.lang.String\n" +
				"\n" +
				"  Inner classes:\n" +
				"    [inner class info: #66 java/lang/invoke/MethodHandles$Lookup, outer class info: #68 java/lang/invoke/MethodHandles\n" +
				"     inner name: #70 Lookup, accessflags: 25 public static final]\n" +
				"Bootstrap methods:\n" +
				"  0 : # 49 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
				"	Method arguments:\n" +
				"		#51 (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
				"		#54 invokestatic X.lambda$0:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"		#56 (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;,\n" +
				"  1 : # 62 invokestatic java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"	Method arguments:\n" +
				"		#63 \n" +
				"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406627,  [1.8][compiler][codegen] Annotations on lambda parameters go the way of /dev/null
public void test012() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"interface I {\n" +
				"	void doit (Object o, Object p);\n" +
				"}\n" +
				"public class X {\n" +
				"   public static void main(String [] args) {\n" +
				"   int local1 = 0,  local2 = 1;\n" +
				"	I i = (@Annotation Object o, @Annotation Object p) -> {\n" +
				"       int j = args.length + local1 + local2;\n" +
				"	};\n" +
				"}\n" +
				"}\n" +
				"@Target(ElementType.PARAMETER)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Annotation {\n" +
				"}\n",
		},
		"");

	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"<init>\"\n" +
			"    constant #6 utf8: \"()V\"\n" +
			"    constant #7 utf8: \"Code\"\n" +
			"    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V\n" +
			"    constant #9 name_and_type: #5.#6 <init> ()V\n" +
			"    constant #10 utf8: \"LineNumberTable\"\n" +
			"    constant #11 utf8: \"LocalVariableTable\"\n" +
			"    constant #12 utf8: \"this\"\n" +
			"    constant #13 utf8: \"LX;\"\n" +
			"    constant #14 utf8: \"main\"\n" +
			"    constant #15 utf8: \"([Ljava/lang/String;)V\"\n" +
			"    constant #16 invoke dynamic: #0 #17 doit ([Ljava/lang/String;II)LI;\n" +
			"    constant #17 name_and_type: #18.#19 doit ([Ljava/lang/String;II)LI;\n" +
			"    constant #18 utf8: \"doit\"\n" +
			"    constant #19 utf8: \"([Ljava/lang/String;II)LI;\"\n" +
			"    constant #20 utf8: \"args\"\n" +
			"    constant #21 utf8: \"[Ljava/lang/String;\"\n" +
			"    constant #22 utf8: \"local1\"\n" +
			"    constant #23 utf8: \"I\"\n" +
			"    constant #24 utf8: \"local2\"\n" +
			"    constant #25 utf8: \"i\"\n" +
			"    constant #26 utf8: \"LI;\"\n" +
			"    constant #27 utf8: \"lambda$0\"\n" +
			"    constant #28 utf8: \"([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\"\n" +
			"    constant #29 utf8: \"RuntimeVisibleParameterAnnotations\"\n" +
			"    constant #30 utf8: \"LAnnotation;\"\n" +
			"    constant #31 utf8: \"o\"\n" +
			"    constant #32 utf8: \"Ljava/lang/Object;\"\n" +
			"    constant #33 utf8: \"p\"\n" +
			"    constant #34 utf8: \"SourceFile\"\n" +
			"    constant #35 utf8: \"X.java\"\n" +
			"    constant #36 utf8: \"BootstrapMethods\"\n" +
			"    constant #37 method_ref: #38.#40 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #38 class: #39 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #39 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #40 name_and_type: #41.#42 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #41 utf8: \"metafactory\"\n" +
			"    constant #42 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #43 method handle: invokestatic (6) #37 \n" +
			"    constant #44 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)V\"\n" +
			"    constant #45 method type: #44 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #46 method_ref: #1.#47 X.lambda$0 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #47 name_and_type: #27.#28 lambda$0 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #48 method handle: invokestatic (6) #46 \n" +
			"    constant #49 method type: #44 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #50 utf8: \"InnerClasses\"\n" +
			"    constant #51 class: #52 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #52 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #53 class: #54 java/lang/invoke/MethodHandles\n" +
			"    constant #54 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #55 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public X();\n" +
			"    0  aload_0 [this]\n" +
			"    1  invokespecial java.lang.Object() [8]\n" +
			"    4  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 3, Locals: 4\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [local1]\n" +
			"     2  iconst_1\n" +
			"     3  istore_2 [local2]\n" +
			"     4  aload_0 [args]\n" +
			"     5  iload_1 [local1]\n" +
			"     6  iload_2 [local2]\n" +
			"     7  invokedynamic 0 doit(java.lang.String[], int, int) : I [16]\n" +
			"    12  astore_3 [i]\n" +
			"    13  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 10]\n" +
			"        [pc: 4, line: 11]\n" +
			"        [pc: 13, line: 14]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 14] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 14] local: local1 index: 1 type: int\n" +
			"        [pc: 4, pc: 14] local: local2 index: 2 type: int\n" +
			"        [pc: 13, pc: 14] local: i index: 3 type: I\n" +
			"  \n" +
			"  // Method descriptor #28 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" +
			"  // Stack: 2, Locals: 6\n" +
			"  private static synthetic void lambda$0(java.lang.String[] arg0, int arg1, int arg2, java.lang.Object o, java.lang.Object p);\n" +
			"    0  aload_0 [arg0]\n" +
			"    1  arraylength\n" +
			"    2  iload_1 [arg1]\n" +
			"    3  iadd\n" +
			"    4  iload_2 [arg2]\n" +
			"    5  iadd\n" +
			"    6  istore 5\n" +
			"    8  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 12]\n" +
			"        [pc: 8, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 9] local: o index: 3 type: java.lang.Object\n" +
			"        [pc: 0, pc: 9] local: p index: 4 type: java.lang.Object\n" +
			"    RuntimeVisibleParameterAnnotations: \n" +
			"      Number of annotations for parameter 0: 0\n" +
			"      Number of annotations for parameter 1: 0\n" +
			"      Number of annotations for parameter 2: 0\n" +
			"      Number of annotations for parameter 3: 1\n" +
			"        #30 @Annotation(\n" +
			"        )\n" +
			"      Number of annotations for parameter 4: 1\n" +
			"        #30 @Annotation(\n" +
			"        )\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #51 java/lang/invoke/MethodHandles$Lookup, outer class info: #53 java/lang/invoke/MethodHandles\n" +
			"     inner name: #55 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 43 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#45 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"		#48 invokestatic X.lambda$0:([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" +
			"		#49 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"}"
;

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406627,  [1.8][compiler][codegen] Annotations on lambda parameters go the way of /dev/null
public void test013() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"interface I {\n" +
				"	void doit (Object o, Object p);\n" +
				"}\n" +
				"public class X {\n" +
				"   public static void main(String [] args) {\n" +
				"	I i = (@Annotation Object o, @Annotation Object p) -> {\n" +
				"	};\n" +
				"}\n" +
				"}\n" +
				"@Target(ElementType.PARAMETER)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Annotation {\n" +
				"}\n",
		},
		"");

	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"<init>\"\n" +
			"    constant #6 utf8: \"()V\"\n" +
			"    constant #7 utf8: \"Code\"\n" +
			"    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V\n" +
			"    constant #9 name_and_type: #5.#6 <init> ()V\n" +
			"    constant #10 utf8: \"LineNumberTable\"\n" +
			"    constant #11 utf8: \"LocalVariableTable\"\n" +
			"    constant #12 utf8: \"this\"\n" +
			"    constant #13 utf8: \"LX;\"\n" +
			"    constant #14 utf8: \"main\"\n" +
			"    constant #15 utf8: \"([Ljava/lang/String;)V\"\n" +
			"    constant #16 invoke dynamic: #0 #17 doit ()LI;\n" +
			"    constant #17 name_and_type: #18.#19 doit ()LI;\n" +
			"    constant #18 utf8: \"doit\"\n" +
			"    constant #19 utf8: \"()LI;\"\n" +
			"    constant #20 utf8: \"args\"\n" +
			"    constant #21 utf8: \"[Ljava/lang/String;\"\n" +
			"    constant #22 utf8: \"i\"\n" +
			"    constant #23 utf8: \"LI;\"\n" +
			"    constant #24 utf8: \"lambda$0\"\n" +
			"    constant #25 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)V\"\n" +
			"    constant #26 utf8: \"RuntimeVisibleParameterAnnotations\"\n" +
			"    constant #27 utf8: \"LAnnotation;\"\n" +
			"    constant #28 utf8: \"o\"\n" +
			"    constant #29 utf8: \"Ljava/lang/Object;\"\n" +
			"    constant #30 utf8: \"p\"\n" +
			"    constant #31 utf8: \"SourceFile\"\n" +
			"    constant #32 utf8: \"X.java\"\n" +
			"    constant #33 utf8: \"BootstrapMethods\"\n" +
			"    constant #34 method_ref: #35.#37 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #35 class: #36 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #36 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #37 name_and_type: #38.#39 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #38 utf8: \"metafactory\"\n" +
			"    constant #39 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #40 method handle: invokestatic (6) #34 \n" +
			"    constant #41 method type: #25 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #42 method_ref: #1.#43 X.lambda$0 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #43 name_and_type: #24.#25 lambda$0 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #44 method handle: invokestatic (6) #42 \n" +
			"    constant #45 method type: #25 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #46 utf8: \"InnerClasses\"\n" +
			"    constant #47 class: #48 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #48 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #49 class: #50 java/lang/invoke/MethodHandles\n" +
			"    constant #50 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #51 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public X();\n" +
			"    0  aload_0 [this]\n" +
			"    1  invokespecial java.lang.Object() [8]\n" +
			"    4  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"    0  invokedynamic 0 doit() : I [16]\n" +
			"    5  astore_1 [i]\n" +
			"    6  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 10]\n" +
			"        [pc: 6, line: 12]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 7] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 6, pc: 7] local: i index: 1 type: I\n" +
			"  \n" +
			"  // Method descriptor #25 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"  // Stack: 0, Locals: 2\n" +
			"  private static synthetic void lambda$0(java.lang.Object o, java.lang.Object p);\n" +
			"    0  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 11]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 1] local: o index: 0 type: java.lang.Object\n" +
			"        [pc: 0, pc: 1] local: p index: 1 type: java.lang.Object\n" +
			"    RuntimeVisibleParameterAnnotations: \n" +
			"      Number of annotations for parameter 0: 1\n" +
			"        #27 @Annotation(\n" +
			"        )\n" +
			"      Number of annotations for parameter 1: 1\n" +
			"        #27 @Annotation(\n" +
			"        )\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #47 java/lang/invoke/MethodHandles$Lookup, outer class info: #49 java/lang/invoke/MethodHandles\n" +
			"     inner name: #51 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 40 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#41 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"		#44 invokestatic X.lambda$0:(Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"		#45 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void test014() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"interface I {\n" +
				"	void doit (Object o, Object p);\n" +
				"}\n" +
				"public class X {\n" +
				"	I i = (@Annotation Object o, @Annotation Object p) -> {\n" +
				"	};\n" +
				"   public static void main(String [] args) {\n" +
				"   int local1 = 0,  local2 = 1;\n" +
				"	I i = (@Annotation Object o, @Annotation Object p) -> {\n" +
				"       int j = args.length + local1 + local2;\n" +
				"	};\n" +
				"}\n" +
				"}\n" +
				"@Target(ElementType.PARAMETER)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Annotation {\n" +
				"}\n",
		},
		"");

	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"i\"\n" +
			"    constant #6 utf8: \"LI;\"\n" +
			"    constant #7 utf8: \"<init>\"\n" +
			"    constant #8 utf8: \"()V\"\n" +
			"    constant #9 utf8: \"Code\"\n" +
			"    constant #10 method_ref: #3.#11 java/lang/Object.<init> ()V\n" +
			"    constant #11 name_and_type: #7.#8 <init> ()V\n" +
			"    constant #12 invoke dynamic: #0 #13 doit ()LI;\n" +
			"    constant #13 name_and_type: #14.#15 doit ()LI;\n" +
			"    constant #14 utf8: \"doit\"\n" +
			"    constant #15 utf8: \"()LI;\"\n" +
			"    constant #16 field_ref: #1.#17 X.i LI;\n" +
			"    constant #17 name_and_type: #5.#6 i LI;\n" +
			"    constant #18 utf8: \"LineNumberTable\"\n" +
			"    constant #19 utf8: \"LocalVariableTable\"\n" +
			"    constant #20 utf8: \"this\"\n" +
			"    constant #21 utf8: \"LX;\"\n" +
			"    constant #22 utf8: \"main\"\n" +
			"    constant #23 utf8: \"([Ljava/lang/String;)V\"\n" +
			"    constant #24 invoke dynamic: #1 #25 doit ([Ljava/lang/String;II)LI;\n" +
			"    constant #25 name_and_type: #14.#26 doit ([Ljava/lang/String;II)LI;\n" +
			"    constant #26 utf8: \"([Ljava/lang/String;II)LI;\"\n" +
			"    constant #27 utf8: \"args\"\n" +
			"    constant #28 utf8: \"[Ljava/lang/String;\"\n" +
			"    constant #29 utf8: \"local1\"\n" +
			"    constant #30 utf8: \"I\"\n" +
			"    constant #31 utf8: \"local2\"\n" +
			"    constant #32 utf8: \"lambda$0\"\n" +
			"    constant #33 utf8: \"(Ljava/lang/Object;Ljava/lang/Object;)V\"\n" +
			"    constant #34 utf8: \"RuntimeVisibleParameterAnnotations\"\n" +
			"    constant #35 utf8: \"LAnnotation;\"\n" +
			"    constant #36 utf8: \"o\"\n" +
			"    constant #37 utf8: \"Ljava/lang/Object;\"\n" +
			"    constant #38 utf8: \"p\"\n" +
			"    constant #39 utf8: \"lambda$1\"\n" +
			"    constant #40 utf8: \"([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\"\n" +
			"    constant #41 utf8: \"SourceFile\"\n" +
			"    constant #42 utf8: \"X.java\"\n" +
			"    constant #43 utf8: \"BootstrapMethods\"\n" +
			"    constant #44 method_ref: #45.#47 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #45 class: #46 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #46 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #47 name_and_type: #48.#49 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #48 utf8: \"metafactory\"\n" +
			"    constant #49 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #50 method handle: invokestatic (6) #44 \n" +
			"    constant #51 method type: #33 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #52 method_ref: #1.#53 X.lambda$0 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #53 name_and_type: #32.#33 lambda$0 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #54 method handle: invokestatic (6) #52 \n" +
			"    constant #55 method type: #33 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #56 method type: #33 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #57 method_ref: #1.#58 X.lambda$1 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #58 name_and_type: #39.#40 lambda$1 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #59 method handle: invokestatic (6) #57 \n" +
			"    constant #60 method type: #33 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"    constant #61 utf8: \"InnerClasses\"\n" +
			"    constant #62 class: #63 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #63 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #64 class: #65 java/lang/invoke/MethodHandles\n" +
			"    constant #65 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #66 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Field descriptor #6 LI;\n" +
			"  I i;\n" +
			"  \n" +
			"  // Method descriptor #8 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  public X();\n" +
			"     0  aload_0 [this]\n" +
			"     1  invokespecial java.lang.Object() [10]\n" +
			"     4  aload_0 [this]\n" +
			"     5  invokedynamic 0 doit() : I [12]\n" +
			"    10  putfield X.i : I [16]\n" +
			"    13  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 8]\n" +
			"        [pc: 4, line: 9]\n" +
			"        [pc: 13, line: 8]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #23 ([Ljava/lang/String;)V\n" +
			"  // Stack: 3, Locals: 4\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"     0  iconst_0\n" +
			"     1  istore_1 [local1]\n" +
			"     2  iconst_1\n" +
			"     3  istore_2 [local2]\n" +
			"     4  aload_0 [args]\n" +
			"     5  iload_1 [local1]\n" +
			"     6  iload_2 [local2]\n" +
			"     7  invokedynamic 1 doit(java.lang.String[], int, int) : I [24]\n" +
			"    12  astore_3 [i]\n" +
			"    13  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 12]\n" +
			"        [pc: 4, line: 13]\n" +
			"        [pc: 13, line: 16]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 14] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 2, pc: 14] local: local1 index: 1 type: int\n" +
			"        [pc: 4, pc: 14] local: local2 index: 2 type: int\n" +
			"        [pc: 13, pc: 14] local: i index: 3 type: I\n" +
			"  \n" +
			"  // Method descriptor #33 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"  // Stack: 0, Locals: 2\n" +
			"  private static synthetic void lambda$0(java.lang.Object o, java.lang.Object p);\n" +
			"    0  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 10]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 1] local: o index: 0 type: java.lang.Object\n" +
			"        [pc: 0, pc: 1] local: p index: 1 type: java.lang.Object\n" +
			"    RuntimeVisibleParameterAnnotations: \n" +
			"      Number of annotations for parameter 0: 1\n" +
			"        #35 @Annotation(\n" +
			"        )\n" +
			"      Number of annotations for parameter 1: 1\n" +
			"        #35 @Annotation(\n" +
			"        )\n" +
			"  \n" +
			"  // Method descriptor #40 ([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" +
			"  // Stack: 2, Locals: 6\n" +
			"  private static synthetic void lambda$1(java.lang.String[] arg0, int arg1, int arg2, java.lang.Object o, java.lang.Object p);\n" +
			"    0  aload_0 [arg0]\n" +
			"    1  arraylength\n" +
			"    2  iload_1 [arg1]\n" +
			"    3  iadd\n" +
			"    4  iload_2 [arg2]\n" +
			"    5  iadd\n" +
			"    6  istore 5\n" +
			"    8  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 14]\n" +
			"        [pc: 8, line: 15]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 9] local: o index: 3 type: java.lang.Object\n" +
			"        [pc: 0, pc: 9] local: p index: 4 type: java.lang.Object\n" +
			"    RuntimeVisibleParameterAnnotations: \n" +
			"      Number of annotations for parameter 0: 0\n" +
			"      Number of annotations for parameter 1: 0\n" +
			"      Number of annotations for parameter 2: 0\n" +
			"      Number of annotations for parameter 3: 1\n" +
			"        #35 @Annotation(\n" +
			"        )\n" +
			"      Number of annotations for parameter 4: 1\n" +
			"        #35 @Annotation(\n" +
			"        )\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #62 java/lang/invoke/MethodHandles$Lookup, outer class info: #64 java/lang/invoke/MethodHandles\n" +
			"     inner name: #66 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 50 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;" +
			"Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#51 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"		#54 invokestatic X.lambda$0:(Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"		#55 (Ljava/lang/Object;Ljava/lang/Object;)V,\n" +
			"  1 : # 50 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;" +
			"Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#56 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"		#59 invokestatic X.lambda$1:([Ljava/lang/String;IILjava/lang/Object;Ljava/lang/Object;)V\n" +
			"		#60 (Ljava/lang/Object;Ljava/lang/Object;)V\n" +
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406641, [1.8][compiler][codegen] Code generation for intersection cast.
public void test015() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"    void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main( String [] args) { \n" +
				"		I i = (I & java.io.Serializable) () -> {};\n" +
				"	}\n" +
				"}\n",
		},
		"");

	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"public class X {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X\n" +
			"    constant #2 utf8: \"X\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"<init>\"\n" +
			"    constant #6 utf8: \"()V\"\n" +
			"    constant #7 utf8: \"Code\"\n" +
			"    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V\n" +
			"    constant #9 name_and_type: #5.#6 <init> ()V\n" +
			"    constant #10 utf8: \"LineNumberTable\"\n" +
			"    constant #11 utf8: \"LocalVariableTable\"\n" +
			"    constant #12 utf8: \"this\"\n" +
			"    constant #13 utf8: \"LX;\"\n" +
			"    constant #14 utf8: \"main\"\n" +
			"    constant #15 utf8: \"([Ljava/lang/String;)V\"\n" +
			"    constant #16 invoke dynamic: #0 #17 foo ()LI;\n" +
			"    constant #17 name_and_type: #18.#19 foo ()LI;\n" +
			"    constant #18 utf8: \"foo\"\n" +
			"    constant #19 utf8: \"()LI;\"\n" +
			"    constant #20 utf8: \"args\"\n" +
			"    constant #21 utf8: \"[Ljava/lang/String;\"\n" +
			"    constant #22 utf8: \"i\"\n" +
			"    constant #23 utf8: \"LI;\"\n" +
			"    constant #24 utf8: \"lambda$0\"\n" +
			"    constant #25 utf8: \"$deserializeLambda$\"\n" +
			"    constant #26 utf8: \"(Ljava/lang/invoke/SerializedLambda;)Ljava/lang/Object;\"\n" +
			"    constant #27 method_ref: #28.#30 java/lang/invoke/SerializedLambda.getImplMethodName ()Ljava/lang/String;\n" +
			"    constant #28 class: #29 java/lang/invoke/SerializedLambda\n" +
			"    constant #29 utf8: \"java/lang/invoke/SerializedLambda\"\n" +
			"    constant #30 name_and_type: #31.#32 getImplMethodName ()Ljava/lang/String;\n" +
			"    constant #31 utf8: \"getImplMethodName\"\n" +
			"    constant #32 utf8: \"()Ljava/lang/String;\"\n" +
			"    constant #33 method_ref: #34.#36 java/lang/String.hashCode ()I\n" +
			"    constant #34 class: #35 java/lang/String\n" +
			"    constant #35 utf8: \"java/lang/String\"\n" +
			"    constant #36 name_and_type: #37.#38 hashCode ()I\n" +
			"    constant #37 utf8: \"hashCode\"\n" +
			"    constant #38 utf8: \"()I\"\n" +
			"    constant #39 string: #24 \"lambda$0\"\n" +
			"    constant #40 method_ref: #34.#41 java/lang/String.equals (Ljava/lang/Object;)Z\n" +
			"    constant #41 name_and_type: #42.#43 equals (Ljava/lang/Object;)Z\n" +
			"    constant #42 utf8: \"equals\"\n" +
			"    constant #43 utf8: \"(Ljava/lang/Object;)Z\"\n" +
			"    constant #44 method_ref: #28.#45 java/lang/invoke/SerializedLambda.getImplMethodKind ()I\n" +
			"    constant #45 name_and_type: #46.#38 getImplMethodKind ()I\n" +
			"    constant #46 utf8: \"getImplMethodKind\"\n" +
			"    constant #47 method_ref: #28.#48 java/lang/invoke/SerializedLambda.getFunctionalInterfaceClass ()Ljava/lang/String;\n" +
			"    constant #48 name_and_type: #49.#32 getFunctionalInterfaceClass ()Ljava/lang/String;\n" +
			"    constant #49 utf8: \"getFunctionalInterfaceClass\"\n" +
			"    constant #50 string: #51 \"I\"\n" +
			"    constant #51 utf8: \"I\"\n" +
			"    constant #52 method_ref: #3.#41 java/lang/Object.equals (Ljava/lang/Object;)Z\n" +
			"    constant #53 method_ref: #28.#54 java/lang/invoke/SerializedLambda.getFunctionalInterfaceMethodName ()Ljava/lang/String;\n" +
			"    constant #54 name_and_type: #55.#32 getFunctionalInterfaceMethodName ()Ljava/lang/String;\n" +
			"    constant #55 utf8: \"getFunctionalInterfaceMethodName\"\n" +
			"    constant #56 string: #18 \"foo\"\n" +
			"    constant #57 method_ref: #28.#58 java/lang/invoke/SerializedLambda.getFunctionalInterfaceMethodSignature ()Ljava/lang/String;\n" +
			"    constant #58 name_and_type: #59.#32 getFunctionalInterfaceMethodSignature ()Ljava/lang/String;\n" +
			"    constant #59 utf8: \"getFunctionalInterfaceMethodSignature\"\n" +
			"    constant #60 string: #6 \"()V\"\n" +
			"    constant #61 method_ref: #28.#62 java/lang/invoke/SerializedLambda.getImplClass ()Ljava/lang/String;\n" +
			"    constant #62 name_and_type: #63.#32 getImplClass ()Ljava/lang/String;\n" +
			"    constant #63 utf8: \"getImplClass\"\n" +
			"    constant #64 string: #2 \"X\"\n" +
			"    constant #65 method_ref: #28.#66 java/lang/invoke/SerializedLambda.getImplMethodSignature ()Ljava/lang/String;\n" +
			"    constant #66 name_and_type: #67.#32 getImplMethodSignature ()Ljava/lang/String;\n" +
			"    constant #67 utf8: \"getImplMethodSignature\"\n" +
			"    constant #68 class: #69 java/lang/IllegalArgumentException\n" +
			"    constant #69 utf8: \"java/lang/IllegalArgumentException\"\n" +
			"    constant #70 string: #71 \"Invalid lambda deserialization\"\n" +
			"    constant #71 utf8: \"Invalid lambda deserialization\"\n" +
			"    constant #72 method_ref: #68.#73 java/lang/IllegalArgumentException.<init> (Ljava/lang/String;)V\n" +
			"    constant #73 name_and_type: #5.#74 <init> (Ljava/lang/String;)V\n" +
			"    constant #74 utf8: \"(Ljava/lang/String;)V\"\n" +
			"    constant #75 utf8: \"StackMapTable\"\n" +
			"    constant #76 utf8: \"SourceFile\"\n" +
			"    constant #77 utf8: \"X.java\"\n" +
			"    constant #78 utf8: \"BootstrapMethods\"\n" +
			"    constant #79 method_ref: #80.#82 java/lang/invoke/LambdaMetafactory.altMetafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #80 class: #81 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #81 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #82 name_and_type: #83.#84 altMetafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #83 utf8: \"altMetafactory\"\n" +
			"    constant #84 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #85 method handle: invokestatic (6) #79 \n" +
			"    constant #86 method type: #6 ()V\n" +
			"    constant #87 method_ref: #1.#88 X.lambda$0 ()V\n" +
			"    constant #88 name_and_type: #24.#6 lambda$0 ()V\n" +
			"    constant #89 method handle: invokestatic (6) #87 \n" +
			"    constant #90 method type: #6 ()V\n" +
			"    constant #91 integer: 1\n" +
			"    constant #92 utf8: \"InnerClasses\"\n" +
			"    constant #93 class: #94 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #94 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #95 class: #96 java/lang/invoke/MethodHandles\n" +
			"    constant #96 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #97 utf8: \"Lookup\"\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public X();\n" +
			"    0  aload_0 [this]\n" +
			"    1  invokespecial java.lang.Object() [8]\n" +
			"    4  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
			"  \n" +
			"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 1, Locals: 2\n" +
			"  public static void main(java.lang.String[] args);\n" +
			"    0  invokedynamic 0 foo() : I [16]\n" +
			"    5  astore_1 [i]\n" +
			"    6  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"        [pc: 6, line: 7]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 7] local: args index: 0 type: java.lang.String[]\n" +
			"        [pc: 6, pc: 7] local: i index: 1 type: I\n" +
			"  \n" +
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 0\n" +
			"  private static synthetic void lambda$0();\n" +
			"    0  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"  \n" +
			"  // Method descriptor #26 (Ljava/lang/invoke/SerializedLambda;)Ljava/lang/Object;\n" +
			"  // Stack: 3, Locals: 3\n" +
			"  private static synthetic java.lang.Object $deserializeLambda$(java.lang.invoke.SerializedLambda arg0);\n" +
			"      0  aload_0 [arg0]\n" +
			"      1  invokevirtual java.lang.invoke.SerializedLambda.getImplMethodName() : java.lang.String [27]\n" +
			"      4  astore_1\n" +
			"      5  iconst_m1\n" +
			"      6  istore_2\n" +
			"      7  aload_1\n" +
			"      8  invokevirtual java.lang.String.hashCode() : int [33]\n" +
			"     11  lookupswitch default: 39\n" +
			"          case -1647345005: 28\n" +
			"     28  aload_1\n" +
			"     29  ldc <String \"lambda$0\"> [39]\n" +
			"     31  invokevirtual java.lang.String.equals(java.lang.Object) : boolean [40]\n" +
			"     34  ifeq 39\n" +
			"     37  iconst_0\n" +
			"     38  istore_2\n" +
			"     39  iload_2\n" +
			"     40  lookupswitch default: 135\n" +
			"          case 0: 60\n" +
			"     60  aload_0 [arg0]\n" +
			"     61  invokevirtual java.lang.invoke.SerializedLambda.getImplMethodKind() : int [44]\n" +
			"     64  bipush 6\n" +
			"     66  if_icmpne 135\n" +
			"     69  aload_0 [arg0]\n" +
			"     70  invokevirtual java.lang.invoke.SerializedLambda.getFunctionalInterfaceClass() : java.lang.String [47]\n" +
			"     73  ldc <String \"I\"> [50]\n" +
			"     75  invokevirtual java.lang.Object.equals(java.lang.Object) : boolean [52]\n" +
			"     78  ifeq 135\n" +
			"     81  aload_0 [arg0]\n" +
			"     82  invokevirtual java.lang.invoke.SerializedLambda.getFunctionalInterfaceMethodName() : java.lang.String [53]\n" +
			"     85  ldc <String \"foo\"> [56]\n" +
			"     87  invokevirtual java.lang.Object.equals(java.lang.Object) : boolean [52]\n" +
			"     90  ifeq 135\n" +
			"     93  aload_0 [arg0]\n" +
			"     94  invokevirtual java.lang.invoke.SerializedLambda.getFunctionalInterfaceMethodSignature() : java.lang.String [57]\n" +
			"     97  ldc <String \"()V\"> [60]\n" +
			"     99  invokevirtual java.lang.Object.equals(java.lang.Object) : boolean [52]\n" +
			"    102  ifeq 135\n" +
			"    105  aload_0 [arg0]\n" +
			"    106  invokevirtual java.lang.invoke.SerializedLambda.getImplClass() : java.lang.String [61]\n" +
			"    109  ldc <String \"X\"> [64]\n" +
			"    111  invokevirtual java.lang.Object.equals(java.lang.Object) : boolean [52]\n" +
			"    114  ifeq 135\n" +
			"    117  aload_0 [arg0]\n" +
			"    118  invokevirtual java.lang.invoke.SerializedLambda.getImplMethodSignature() : java.lang.String [65]\n" +
			"    121  ldc <String \"()V\"> [60]\n" +
			"    123  invokevirtual java.lang.Object.equals(java.lang.Object) : boolean [52]\n" +
			"    126  ifeq 135\n" +
			"    129  invokedynamic 0 foo() : I [16]\n" +
			"    134  areturn\n" +
			"    135  new java.lang.IllegalArgumentException [68]\n" +
			"    138  dup\n" +
			"    139  ldc <String \"Invalid lambda deserialization\"> [70]\n" +
			"    141  invokespecial java.lang.IllegalArgumentException(java.lang.String) [72]\n" +
			"    144  athrow\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Stack map table: number of frames 4\n" +
			"        [pc: 28, append: {java.lang.String, int}]\n" +
			"        [pc: 39, same]\n" +
			"        [pc: 60, same]\n" +
			"        [pc: 135, chop 2 local(s)]\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #93 java/lang/invoke/MethodHandles$Lookup, outer class info: #95 java/lang/invoke/MethodHandles\n" +
			"     inner name: #97 Lookup, accessflags: 25 public static final]\n" +
			"Bootstrap methods:\n" +
			"  0 : # 85 invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#86 ()V\n" +
			"		#89 invokestatic X.lambda$0:()V\n" +
			"		#90 ()V\n" +
			"		#91 1\n" +
			"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406392, [1.8][compiler][codegen] Improve identification of lambdas that must capture enclosing instance
public void test016() throws Exception {
	// This test proves that when a lambda body references a type variable of an enclosing method, it can still be emitted as a static method.
	this.runConformTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	void doit();\n" +
				"}\n" +
				"public class X  {\n" +
				"	<T> void foo() {\n" +
				"		class Y {\n" +
				"			T goo() {\n" +
				"				((I) () -> {\n" +
				"	    			T t = null;\n" +
				"		    		System.out.println(\"Lambda\");\n" +
				"				}).doit();\n" +
				"				return null;\n" +
				"			}\n" +
				"		}\n" +
				"		new Y().goo();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().<String>foo(); \n" +
				"	}\n" +
				"}\n",
		},
		"Lambda");

	String nestConstant = "";
	String nestHost = "";
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.complianceLevel >= ClassFileConstants.JDK11) {
		nestConstant = "    constant #77 utf8: \"NestHost\"\n";
		nestHost = "\n" +
				"Nest Host: #53 X\n";
	}
	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"class X$1Y {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X$1Y\n" +
			"    constant #2 utf8: \"X$1Y\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"this$0\"\n" +
			"    constant #6 utf8: \"LX;\"\n" +
			"    constant #7 utf8: \"<init>\"\n" +
			"    constant #8 utf8: \"(LX;)V\"\n" +
			"    constant #9 utf8: \"Code\"\n" +
			"    constant #10 field_ref: #1.#11 X$1Y.this$0 LX;\n" +
			"    constant #11 name_and_type: #5.#6 this$0 LX;\n" +
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" +
			"    constant #13 name_and_type: #7.#14 <init> ()V\n" +
			"    constant #14 utf8: \"()V\"\n" +
			"    constant #15 utf8: \"LineNumberTable\"\n" +
			"    constant #16 utf8: \"LocalVariableTable\"\n" +
			"    constant #17 utf8: \"this\"\n" +
			"    constant #18 utf8: \"LX$1Y;\"\n" +
			"    constant #19 utf8: \"goo\"\n" +
			"    constant #20 utf8: \"()Ljava/lang/Object;\"\n" +
			"    constant #21 utf8: \"Signature\"\n" +
			"    constant #22 utf8: \"()TT;\"\n" +
			"    constant #23 invoke dynamic: #0 #24 doit ()LI;\n" +
			"    constant #24 name_and_type: #25.#26 doit ()LI;\n" +
			"    constant #25 utf8: \"doit\"\n" +
			"    constant #26 utf8: \"()LI;\"\n" +
			"    constant #27 interface_method_ref: #28.#30 I.doit ()V\n" +
			"    constant #28 class: #29 I\n" +
			"    constant #29 utf8: \"I\"\n" +
			"    constant #30 name_and_type: #25.#14 doit ()V\n" +
			"    constant #31 utf8: \"lambda$0\"\n" +
			"    constant #32 field_ref: #33.#35 java/lang/System.out Ljava/io/PrintStream;\n" +
			"    constant #33 class: #34 java/lang/System\n" +
			"    constant #34 utf8: \"java/lang/System\"\n" +
			"    constant #35 name_and_type: #36.#37 out Ljava/io/PrintStream;\n" +
			"    constant #36 utf8: \"out\"\n" +
			"    constant #37 utf8: \"Ljava/io/PrintStream;\"\n" +
			"    constant #38 string: #39 \"Lambda\"\n" +
			"    constant #39 utf8: \"Lambda\"\n" +
			"    constant #40 method_ref: #41.#43 java/io/PrintStream.println (Ljava/lang/String;)V\n" +
			"    constant #41 class: #42 java/io/PrintStream\n" +
			"    constant #42 utf8: \"java/io/PrintStream\"\n" +
			"    constant #43 name_and_type: #44.#45 println (Ljava/lang/String;)V\n" +
			"    constant #44 utf8: \"println\"\n" +
			"    constant #45 utf8: \"(Ljava/lang/String;)V\"\n" +
			"    constant #46 utf8: \"t\"\n" +
			"    constant #47 utf8: \"Ljava/lang/Object;\"\n" +
			"    constant #48 utf8: \"LocalVariableTypeTable\"\n" +
			"    constant #49 utf8: \"TT;\"\n" +
			"    constant #50 utf8: \"SourceFile\"\n" +
			"    constant #51 utf8: \"X.java\"\n" +
			"    constant #52 utf8: \"EnclosingMethod\"\n" +
			"    constant #53 class: #54 X\n" +
			"    constant #54 utf8: \"X\"\n" +
			"    constant #55 name_and_type: #56.#14 foo ()V\n" +
			"    constant #56 utf8: \"foo\"\n" +
			"    constant #57 utf8: \"BootstrapMethods\"\n" +
			"    constant #58 method_ref: #59.#61 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #59 class: #60 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #60 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #61 name_and_type: #62.#63 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #62 utf8: \"metafactory\"\n" +
			"    constant #63 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #64 method handle: invokestatic (6) #58 \n" +
			"    constant #65 method type: #14 ()V\n" +
			"    constant #66 method_ref: #1.#67 X$1Y.lambda$0 ()V\n" +
			"    constant #67 name_and_type: #31.#14 lambda$0 ()V\n" +
			"    constant #68 method handle: invokestatic (6) #66 \n" +
			"    constant #69 method type: #14 ()V\n" +
			"    constant #70 utf8: \"InnerClasses\"\n" +
			"    constant #71 utf8: \"Y\"\n" +
			"    constant #72 class: #73 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #73 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #74 class: #75 java/lang/invoke/MethodHandles\n" +
			"    constant #75 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #76 utf8: \"Lookup\"\n" +
			nestConstant +
			"  \n" +
			"  // Field descriptor #6 LX;\n" +
			"  final synthetic X this$0;\n" +
			"  \n" +
			"  // Method descriptor #8 (LX;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  X$1Y(X arg0);\n" +
			"     0  aload_0 [this]\n" +
			"     1  aload_1 [arg0]\n" +
			"     2  putfield X$1Y.this$0 : X [10]\n" +
			"     5  aload_0 [this]\n" +
			"     6  invokespecial java.lang.Object() [12]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 10] local: this index: 0 type: new X(){}\n" +
			"  \n" +
			"  // Method descriptor #20 ()Ljava/lang/Object;\n" +
			"  // Signature: ()TT;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  java.lang.Object goo();\n" +
			"     0  invokedynamic 0 doit() : I [23]\n" +
			"     5  invokeinterface I.doit() : void [27] [nargs: 1]\n" +
			"    10  aconst_null\n" +
			"    11  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 8]\n" +
			"        [pc: 5, line: 11]\n" +
			"        [pc: 10, line: 12]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 12] local: this index: 0 type: new X(){}\n" +
			"  \n" +
			"  // Method descriptor #14 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  private static synthetic void lambda$0();\n" +
			"     0  aconst_null\n" +
			"     1  astore_0 [t]\n" +
			"     2  getstatic java.lang.System.out : java.io.PrintStream [32]\n" +
			"     5  ldc <String \"Lambda\"> [38]\n" +
			"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [40]\n" +
			"    10  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 2, line: 10]\n" +
			"        [pc: 10, line: 11]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 10] local: t index: 0 type: java.lang.Object\n" +
			"      Local variable type table:\n" +
			"        [pc: 2, pc: 10] local: t index: 0 type: T\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #1 X$1Y, outer class info: #0\n" +
			"     inner name: #71 Y, accessflags: 0 default],\n" +
			"    [inner class info: #72 java/lang/invoke/MethodHandles$Lookup, outer class info: #74 java/lang/invoke/MethodHandles\n" +
			"     inner name: #76 Lookup, accessflags: 25 public static final]\n" +
			"  Enclosing Method: #53  #55 X.foo()V\n" +
			nestHost +
			"Bootstrap methods:\n" +
			"  0 : # 64 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;" +
			"Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#65 ()V\n" +
			"		#68 invokestatic X$1Y.lambda$0:()V\n" +
			"		#69 ()V\n" +
			"}";

	verifyClassFile(expectedOutput, "X$1Y.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406392, [1.8][compiler][codegen] Improve identification of lambdas that must capture enclosing instance
public void test017() throws Exception {
	// This test proves that when a lambda body references a type variable of an enclosing class, it can still be emitted as a static method.
	this.runConformTest(
		new String[] {
				"X.java",
				"interface I {\n" +
				"	void doit();\n" +
				"}\n" +
				"public class X<T>  {\n" +
				"	void foo() {\n" +
				"		class Y {\n" +
				"			T goo() {\n" +
				"				((I) () -> {\n" +
				"				T t = null;\n" +
				"				System.out.println(\"Lambda\");     \n" +
				"				}).doit();\n" +
				"				return null;\n" +
				"			}\n" +
				"		}\n" +
				"		new Y().goo();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().<String>foo(); \n" +
				"	}\n" +
				"}\n",
		},
		"Lambda");

	String nestConstant = "";
	String nestHost = "";
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.complianceLevel >= ClassFileConstants.JDK11) {
		nestConstant = "    constant #77 utf8: \"NestHost\"\n";
		nestHost = "\n" +
				"Nest Host: #53 X\n";
	}
	String expectedOutput =
			"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
			"class X$1Y {\n" +
			"  Constant pool:\n" +
			"    constant #1 class: #2 X$1Y\n" +
			"    constant #2 utf8: \"X$1Y\"\n" +
			"    constant #3 class: #4 java/lang/Object\n" +
			"    constant #4 utf8: \"java/lang/Object\"\n" +
			"    constant #5 utf8: \"this$0\"\n" +
			"    constant #6 utf8: \"LX;\"\n" +
			"    constant #7 utf8: \"<init>\"\n" +
			"    constant #8 utf8: \"(LX;)V\"\n" +
			"    constant #9 utf8: \"Code\"\n" +
			"    constant #10 field_ref: #1.#11 X$1Y.this$0 LX;\n" +
			"    constant #11 name_and_type: #5.#6 this$0 LX;\n" +
			"    constant #12 method_ref: #3.#13 java/lang/Object.<init> ()V\n" +
			"    constant #13 name_and_type: #7.#14 <init> ()V\n" +
			"    constant #14 utf8: \"()V\"\n" +
			"    constant #15 utf8: \"LineNumberTable\"\n" +
			"    constant #16 utf8: \"LocalVariableTable\"\n" +
			"    constant #17 utf8: \"this\"\n" +
			"    constant #18 utf8: \"LX$1Y;\"\n" +
			"    constant #19 utf8: \"goo\"\n" +
			"    constant #20 utf8: \"()Ljava/lang/Object;\"\n" +
			"    constant #21 utf8: \"Signature\"\n" +
			"    constant #22 utf8: \"()TT;\"\n" +
			"    constant #23 invoke dynamic: #0 #24 doit ()LI;\n" +
			"    constant #24 name_and_type: #25.#26 doit ()LI;\n" +
			"    constant #25 utf8: \"doit\"\n" +
			"    constant #26 utf8: \"()LI;\"\n" +
			"    constant #27 interface_method_ref: #28.#30 I.doit ()V\n" +
			"    constant #28 class: #29 I\n" +
			"    constant #29 utf8: \"I\"\n" +
			"    constant #30 name_and_type: #25.#14 doit ()V\n" +
			"    constant #31 utf8: \"lambda$0\"\n" +
			"    constant #32 field_ref: #33.#35 java/lang/System.out Ljava/io/PrintStream;\n" +
			"    constant #33 class: #34 java/lang/System\n" +
			"    constant #34 utf8: \"java/lang/System\"\n" +
			"    constant #35 name_and_type: #36.#37 out Ljava/io/PrintStream;\n" +
			"    constant #36 utf8: \"out\"\n" +
			"    constant #37 utf8: \"Ljava/io/PrintStream;\"\n" +
			"    constant #38 string: #39 \"Lambda\"\n" +
			"    constant #39 utf8: \"Lambda\"\n" +
			"    constant #40 method_ref: #41.#43 java/io/PrintStream.println (Ljava/lang/String;)V\n" +
			"    constant #41 class: #42 java/io/PrintStream\n" +
			"    constant #42 utf8: \"java/io/PrintStream\"\n" +
			"    constant #43 name_and_type: #44.#45 println (Ljava/lang/String;)V\n" +
			"    constant #44 utf8: \"println\"\n" +
			"    constant #45 utf8: \"(Ljava/lang/String;)V\"\n" +
			"    constant #46 utf8: \"t\"\n" +
			"    constant #47 utf8: \"Ljava/lang/Object;\"\n" +
			"    constant #48 utf8: \"LocalVariableTypeTable\"\n" +
			"    constant #49 utf8: \"TT;\"\n" +
			"    constant #50 utf8: \"SourceFile\"\n" +
			"    constant #51 utf8: \"X.java\"\n" +
			"    constant #52 utf8: \"EnclosingMethod\"\n" +
			"    constant #53 class: #54 X\n" +
			"    constant #54 utf8: \"X\"\n" +
			"    constant #55 name_and_type: #56.#14 foo ()V\n" +
			"    constant #56 utf8: \"foo\"\n" +
			"    constant #57 utf8: \"BootstrapMethods\"\n" +
			"    constant #58 method_ref: #59.#61 java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #59 class: #60 java/lang/invoke/LambdaMetafactory\n" +
			"    constant #60 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
			"    constant #61 name_and_type: #62.#63 metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"    constant #62 utf8: \"metafactory\"\n" +
			"    constant #63 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\"\n" +
			"    constant #64 method handle: invokestatic (6) #58 \n" +
			"    constant #65 method type: #14 ()V\n" +
			"    constant #66 method_ref: #1.#67 X$1Y.lambda$0 ()V\n" +
			"    constant #67 name_and_type: #31.#14 lambda$0 ()V\n" +
			"    constant #68 method handle: invokestatic (6) #66 \n" +
			"    constant #69 method type: #14 ()V\n" +
			"    constant #70 utf8: \"InnerClasses\"\n" +
			"    constant #71 utf8: \"Y\"\n" +
			"    constant #72 class: #73 java/lang/invoke/MethodHandles$Lookup\n" +
			"    constant #73 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
			"    constant #74 class: #75 java/lang/invoke/MethodHandles\n" +
			"    constant #75 utf8: \"java/lang/invoke/MethodHandles\"\n" +
			"    constant #76 utf8: \"Lookup\"\n" +
			nestConstant +
			"  \n" +
			"  // Field descriptor #6 LX;\n" +
			"  final synthetic X this$0;\n" +
			"  \n" +
			"  // Method descriptor #8 (LX;)V\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  X$1Y(X arg0);\n" +
			"     0  aload_0 [this]\n" +
			"     1  aload_1 [arg0]\n" +
			"     2  putfield X$1Y.this$0 : X [10]\n" +
			"     5  aload_0 [this]\n" +
			"     6  invokespecial java.lang.Object() [12]\n" +
			"     9  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 6]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 10] local: this index: 0 type: new X(){}\n" +
			"  \n" +
			"  // Method descriptor #20 ()Ljava/lang/Object;\n" +
			"  // Signature: ()TT;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  java.lang.Object goo();\n" +
			"     0  invokedynamic 0 doit() : I [23]\n" +
			"     5  invokeinterface I.doit() : void [27] [nargs: 1]\n" +
			"    10  aconst_null\n" +
			"    11  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 8]\n" +
			"        [pc: 5, line: 11]\n" +
			"        [pc: 10, line: 12]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 12] local: this index: 0 type: new X(){}\n" +
			"  \n" +
			"  // Method descriptor #14 ()V\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  private static synthetic void lambda$0();\n" +
			"     0  aconst_null\n" +
			"     1  astore_0 [t]\n" +
			"     2  getstatic java.lang.System.out : java.io.PrintStream [32]\n" +
			"     5  ldc <String \"Lambda\"> [38]\n" +
			"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [40]\n" +
			"    10  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 2, line: 10]\n" +
			"        [pc: 10, line: 11]\n" +
			"      Local variable table:\n" +
			"        [pc: 2, pc: 10] local: t index: 0 type: java.lang.Object\n" +
			"      Local variable type table:\n" +
			"        [pc: 2, pc: 10] local: t index: 0 type: T\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #1 X$1Y, outer class info: #0\n" +
			"     inner name: #71 Y, accessflags: 0 default],\n" +
			"    [inner class info: #72 java/lang/invoke/MethodHandles$Lookup, outer class info: #74 java/lang/invoke/MethodHandles\n" +
			"     inner name: #76 Lookup, accessflags: 25 public static final]\n" +
			"  Enclosing Method: #53  #55 X.foo()V\n" +
			nestHost +
			"Bootstrap methods:\n" +
			"  0 : # 64 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;" +
			"Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n" +
			"	Method arguments:\n" +
			"		#65 ()V\n" +
			"		#68 invokestatic X$1Y.lambda$0:()V\n" +
			"		#69 ()V\n" +
			"}";

	verifyClassFile(expectedOutput, "X$1Y.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424444, [1.8] VerifyError when constructor reference used with array
public void test424444() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"interface Functional<T> {\n" +
				"    T foo(int size);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String argv[]) {\n" +
				"    	int [] a = goo(10);\n" +
				"    	Functional<int[]> contr = int[]::new;\n" +
				"        System.out.println(\"Done\");\n" +
				"    }\n" +
				"    static int [] goo(int x) {\n" +
				"    	return new int [x];\n" +
				"    }\n" +
				"}\n",
		},
		"Done");

	String expectedOutput =
			"  // Method descriptor #19 (I)[I\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  private static synthetic int[] lambda$0(int arg0);\n" +
			"    0  iload_0 [arg0]\n" +
			"    1  newarray int [10]\n" +
			"    3  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"\n";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424444, [1.8] VerifyError when constructor reference used with array
public void test424444a() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"interface Functional<T> {\n" +
				"    T foo(int size);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String argv[]) {\n" +
				"    	int [] a = goo(10);\n" +
				"    	Functional<int[][]> contr = int[][]::new;\n" +
				"        System.out.println(\"Done\");\n" +
				"    }\n" +
				"    static int [] goo(int x) {\n" +
				"    	return new int [x];\n" +
				"    }\n" +
				"}\n",
		},
		"Done");

	String expectedOutput =
			"  // Method descriptor #49 (I)[[I\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  private static synthetic int[][] lambda$0(int arg0);\n" +
			"    0  iload_0 [arg0]\n" +
			"    1  anewarray int[] [50]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"\n";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424444, [1.8] VerifyError when constructor reference used with array
public void test424444b() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"interface Functional<T> {\n" +
				"    T foo(int size);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String argv[]) {\n" +
				"    	int [] a = goo(10);\n" +
				"    	Functional<String []> contr = String[]::new;\n" +
				"        System.out.println(\"Done\");\n" +
				"    }\n" +
				"    static int [] goo(int x) {\n" +
				"    	return new int [x];\n" +
				"    }\n" +
				"}\n",
		},
		"Done");

	String expectedOutput =
			"  // Method descriptor #49 (I)[Ljava/lang/String;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  private static synthetic java.lang.String[] lambda$0(int arg0);\n" +
			"    0  iload_0 [arg0]\n" +
			"    1  anewarray java.lang.String [50]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"\n";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424444, [1.8] VerifyError when constructor reference used with array
public void test424444c() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"interface Functional<T> {\n" +
				"    T foo(int size);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String argv[]) {\n" +
				"    	int [] a = goo(10);\n" +
				"    	Functional<String [][]> contr = String[][]::new;\n" +
				"        System.out.println(\"Done\");\n" +
				"    }\n" +
				"    static int [] goo(int x) {\n" +
				"    	return new int [x];\n" +
				"    }\n" +
				"}\n",
		},
		"Done");

	String expectedOutput =
			"  // Method descriptor #49 (I)[[Ljava/lang/String;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  private static synthetic java.lang.String[][] lambda$0(int arg0);\n" +
			"    0  iload_0 [arg0]\n" +
			"    1  anewarray java.lang.String[] [50]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"\n";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424444, [1.8] VerifyError when constructor reference used with array
public void test424444d() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"interface Functional<T> {\n" +
				"    T foo(int size);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String argv[]) {\n" +
				"    	int [] a = goo(10);\n" +
				"    	Functional<Object []> contr = String[][]::new;\n" +
				"        System.out.println(\"Done\");\n" +
				"    }\n" +
				"    static int [] goo(int x) {\n" +
				"    	return new int [x];\n" +
				"    }\n" +
				"}\n",
		},
		"Done");

	String expectedOutput =
			"  // Method descriptor #49 (I)[[Ljava/lang/String;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  private static synthetic java.lang.String[][] lambda$0(int arg0);\n" +
			"    0  iload_0 [arg0]\n" +
			"    1  anewarray java.lang.String[] [50]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"\n";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430015, [1.8] NPE trying to disassemble classfile with lambda method and MethodParameters
public void test430015() throws IOException, ClassFormatException {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.reflect.Method;\n" +
				"import java.lang.reflect.Parameter;\n" +
				"import java.util.Arrays;\n" +
				"import java.util.function.IntConsumer;\n" +
				"public class X {\n" +
				"    IntConsumer xx(int a) {\n" +
				"        return i -> { };\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"        Method[] methods = X.class.getDeclaredMethods();\n" +
				"        for (Method method : methods) {\n" +
				"        	if (method.getName().contains(\"lambda\")) {\n" +
				"         		Parameter[] parameters = method.getParameters();\n" +
				"        		System.out.println(Arrays.asList(parameters));\n" +
				"        	}\n" +
				"        }\n" +
				"    }\n" +
				"}\n"
			},
			"[int arg0]");

		String expectedOutput =
				"  // Method descriptor #78 (I)V\n" +
				"  // Stack: 0, Locals: 1\n" +
				"  private static synthetic void lambda$0(int i);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: i index: 0 type: int\n" +
				"\n";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430015, [1.8] NPE trying to disassemble classfile with lambda method and MethodParameters
public void test430015a() throws IOException, ClassFormatException {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_MethodParametersAttribute, CompilerOptions.GENERATE);
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.reflect.Method;\n" +
				"import java.lang.reflect.Parameter;\n" +
				"import java.util.Arrays;\n" +
				"import java.util.function.IntConsumer;\n" +
				"public class X {\n" +
				"    IntConsumer xx(int a) {\n" +
				"        return i -> { };\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"    }\n" +
				"}\n"
			},
			"",
			customOptions);

		String expectedOutput =
				"  // Method descriptor #28 (I)V\n" +
				"  // Stack: 0, Locals: 1\n" +
				"  private static synthetic void lambda$0(int i);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: i index: 0 type: int\n" +
				"\n";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430035, [1.8][compiler][codegen] Bridge methods are not generated for lambdas/method references
public void test430035() throws IOException, ClassFormatException {
	this.runConformTest(
			new String[] {
						"X.java",
						"interface I<T> {\n" +
						"	void foo(String t, T u);\n" +
						"}\n" +
						"interface J<T> {\n" +
						"	void foo(T t, String u);\n" +
						"}\n" +
						"interface K extends I<String>, J<String> {\n" +
						"	void foo(String t, String u);\n" +
						"}\n" +
						"public class X {\n" +
						"    public static void main(String... x) {\n" +
						"      K k = (s, u) -> System.out.println(\"m(\"+ s + u + ')');\n" +
						"      k.foo(\"direct\", \" call\");\n" +
						"      J<String> j = k;\n" +
						"      j.foo(\"bridge\",  \" method(j)\");\n" +
						"      I<String> i = k;\n" +
						"      i.foo(\"bridge\",  \" method(i)\");\n" +
						"    }\n" +
						"}\n"
			},
			"m(direct call)\n" +
			"m(bridge method(j))\n" +
			"m(bridge method(i))");

		String expectedOutput = this.complianceLevel < ClassFileConstants.JDK9 ?
				"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
				"public class X {\n" +
				"  Constant pool:\n" +
				"    constant #1 class: #2 X\n" +
				"    constant #2 utf8: \"X\"\n" +
				"    constant #3 class: #4 java/lang/Object\n" +
				"    constant #4 utf8: \"java/lang/Object\"\n" +
				"    constant #5 utf8: \"<init>\"\n" +
				"    constant #6 utf8: \"()V\"\n" +
				"    constant #7 utf8: \"Code\"\n" +
				"    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V\n" +
				"    constant #9 name_and_type: #5.#6 <init> ()V\n" +
				"    constant #10 utf8: \"LineNumberTable\"\n" +
				"    constant #11 utf8: \"LocalVariableTable\"\n" +
				"    constant #12 utf8: \"this\"\n" +
				"    constant #13 utf8: \"LX;\"\n" +
				"    constant #14 utf8: \"main\"\n" +
				"    constant #15 utf8: \"([Ljava/lang/String;)V\"\n" +
				"    constant #16 invoke dynamic: #0 #17 foo ()LK;\n" +
				"    constant #17 name_and_type: #18.#19 foo ()LK;\n" +
				"    constant #18 utf8: \"foo\"\n" +
				"    constant #19 utf8: \"()LK;\"\n" +
				"    constant #20 string: #21 \"direct\"\n" +
				"    constant #21 utf8: \"direct\"\n" +
				"    constant #22 string: #23 \" call\"\n" +
				"    constant #23 utf8: \" call\"\n" +
				"    constant #24 interface_method_ref: #25.#27 K.foo (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #25 class: #26 K\n" +
				"    constant #26 utf8: \"K\"\n" +
				"    constant #27 name_and_type: #18.#28 foo (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #28 utf8: \"(Ljava/lang/String;Ljava/lang/String;)V\"\n" +
				"    constant #29 string: #30 \"bridge\"\n" +
				"    constant #30 utf8: \"bridge\"\n" +
				"    constant #31 string: #32 \" method(j)\"\n" +
				"    constant #32 utf8: \" method(j)\"\n" +
				"    constant #33 interface_method_ref: #34.#36 J.foo (Ljava/lang/Object;Ljava/lang/String;)V\n" +
				"    constant #34 class: #35 J\n" +
				"    constant #35 utf8: \"J\"\n" +
				"    constant #36 name_and_type: #18.#37 foo (Ljava/lang/Object;Ljava/lang/String;)V\n" +
				"    constant #37 utf8: \"(Ljava/lang/Object;Ljava/lang/String;)V\"\n" +
				"    constant #38 string: #39 \" method(i)\"\n" +
				"    constant #39 utf8: \" method(i)\"\n" +
				"    constant #40 interface_method_ref: #41.#43 I.foo (Ljava/lang/String;Ljava/lang/Object;)V\n" +
				"    constant #41 class: #42 I\n" +
				"    constant #42 utf8: \"I\"\n" +
				"    constant #43 name_and_type: #18.#44 foo (Ljava/lang/String;Ljava/lang/Object;)V\n" +
				"    constant #44 utf8: \"(Ljava/lang/String;Ljava/lang/Object;)V\"\n" +
				"    constant #45 utf8: \"x\"\n" +
				"    constant #46 utf8: \"[Ljava/lang/String;\"\n" +
				"    constant #47 utf8: \"k\"\n" +
				"    constant #48 utf8: \"LK;\"\n" +
				"    constant #49 utf8: \"j\"\n" +
				"    constant #50 utf8: \"LJ;\"\n" +
				"    constant #51 utf8: \"i\"\n" +
				"    constant #52 utf8: \"LI;\"\n" +
				"    constant #53 utf8: \"LocalVariableTypeTable\"\n" +
				"    constant #54 utf8: \"LJ<Ljava/lang/String;>;\"\n" +
				"    constant #55 utf8: \"LI<Ljava/lang/String;>;\"\n" +
				"    constant #56 utf8: \"lambda$0\"\n" +
				"    constant #57 field_ref: #58.#60 java/lang/System.out Ljava/io/PrintStream;\n" +
				"    constant #58 class: #59 java/lang/System\n" +
				"    constant #59 utf8: \"java/lang/System\"\n" +
				"    constant #60 name_and_type: #61.#62 out Ljava/io/PrintStream;\n" +
				"    constant #61 utf8: \"out\"\n" +
				"    constant #62 utf8: \"Ljava/io/PrintStream;\"\n" +
				"    constant #63 class: #64 java/lang/StringBuilder\n" +
				"    constant #64 utf8: \"java/lang/StringBuilder\"\n" +
				"    constant #65 string: #66 \"m(\"\n" +
				"    constant #66 utf8: \"m(\"\n" +
				"    constant #67 method_ref: #63.#68 java/lang/StringBuilder.<init> (Ljava/lang/String;)V\n" +
				"    constant #68 name_and_type: #5.#69 <init> (Ljava/lang/String;)V\n" +
				"    constant #69 utf8: \"(Ljava/lang/String;)V\"\n" +
				"    constant #70 method_ref: #63.#71 java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" +
				"    constant #71 name_and_type: #72.#73 append (Ljava/lang/String;)Ljava/lang/StringBuilder;\n" +
				"    constant #72 utf8: \"append\"\n" +
				"    constant #73 utf8: \"(Ljava/lang/String;)Ljava/lang/StringBuilder;\"\n" +
				"    constant #74 method_ref: #63.#75 java/lang/StringBuilder.append (C)Ljava/lang/StringBuilder;\n" +
				"    constant #75 name_and_type: #72.#76 append (C)Ljava/lang/StringBuilder;\n" +
				"    constant #76 utf8: \"(C)Ljava/lang/StringBuilder;\"\n" +
				"    constant #77 method_ref: #63.#78 java/lang/StringBuilder.toString ()Ljava/lang/String;\n" +
				"    constant #78 name_and_type: #79.#80 toString ()Ljava/lang/String;\n" +
				"    constant #79 utf8: \"toString\"\n" +
				"    constant #80 utf8: \"()Ljava/lang/String;\"\n" +
				"    constant #81 method_ref: #82.#84 java/io/PrintStream.println (Ljava/lang/String;)V\n" +
				"    constant #82 class: #83 java/io/PrintStream\n" +
				"    constant #83 utf8: \"java/io/PrintStream\"\n" +
				"    constant #84 name_and_type: #85.#69 println (Ljava/lang/String;)V\n" +
				"    constant #85 utf8: \"println\"\n" +
				"    constant #86 utf8: \"s\"\n" +
				"    constant #87 utf8: \"Ljava/lang/String;\"\n" +
				"    constant #88 utf8: \"u\"\n" +
				"    constant #89 utf8: \"SourceFile\"\n" +
				"    constant #90 utf8: \"X.java\"\n" +
				"    constant #91 utf8: \"BootstrapMethods\"\n" +
				"    constant #92 method_ref: #93.#95 java/lang/invoke/LambdaMetafactory.altMetafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #93 class: #94 java/lang/invoke/LambdaMetafactory\n" +
				"    constant #94 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
				"    constant #95 name_and_type: #96.#97 altMetafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #96 utf8: \"altMetafactory\"\n" +
				"    constant #97 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\"\n" +
				"    constant #98 method handle: invokestatic (6) #92 \n" +
				"    constant #99 method type: #28 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #100 method_ref: #1.#101 X.lambda$0 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #101 name_and_type: #56.#28 lambda$0 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #102 method handle: invokestatic (6) #100 \n" +
				"    constant #103 method type: #28 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #104 integer: 4\n" +  // flag bridge
				"    constant #105 integer: 2\n" +  // two bridges
				"    constant #106 method type: #44 (Ljava/lang/String;Ljava/lang/Object;)V\n" +  // first bridge
				"    constant #107 method type: #37 (Ljava/lang/Object;Ljava/lang/String;)V\n" +  // next bridge.
				"    constant #108 utf8: \"InnerClasses\"\n" +
				"    constant #109 class: #110 java/lang/invoke/MethodHandles$Lookup\n" +
				"    constant #110 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
				"    constant #111 class: #112 java/lang/invoke/MethodHandles\n" +
				"    constant #112 utf8: \"java/lang/invoke/MethodHandles\"\n" +
				"    constant #113 utf8: \"Lookup\"\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [8]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 10]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
				"  // Stack: 3, Locals: 4\n" +
				"  public static void main(java.lang.String... x);\n" +
				"     0  invokedynamic 0 foo() : K [16]\n" +
				"     5  astore_1 [k]\n" +
				"     6  aload_1 [k]\n" +
				"     7  ldc <String \"direct\"> [20]\n" +
				"     9  ldc <String \" call\"> [22]\n" +
				"    11  invokeinterface K.foo(java.lang.String, java.lang.String) : void [24] [nargs: 3]\n" +
				"    16  aload_1 [k]\n" +
				"    17  astore_2 [j]\n" +
				"    18  aload_2 [j]\n" +
				"    19  ldc <String \"bridge\"> [29]\n" +
				"    21  ldc <String \" method(j)\"> [31]\n" +
				"    23  invokeinterface J.foo(java.lang.Object, java.lang.String) : void [33] [nargs: 3]\n" +
				"    28  aload_1 [k]\n" +
				"    29  astore_3 [i]\n" +
				"    30  aload_3 [i]\n" +
				"    31  ldc <String \"bridge\"> [29]\n" +
				"    33  ldc <String \" method(i)\"> [38]\n" +
				"    35  invokeinterface I.foo(java.lang.String, java.lang.Object) : void [40] [nargs: 3]\n" +
				"    40  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 12]\n" +
				"        [pc: 6, line: 13]\n" +
				"        [pc: 16, line: 14]\n" +
				"        [pc: 18, line: 15]\n" +
				"        [pc: 28, line: 16]\n" +
				"        [pc: 30, line: 17]\n" +
				"        [pc: 40, line: 18]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 41] local: x index: 0 type: java.lang.String[]\n" +
				"        [pc: 6, pc: 41] local: k index: 1 type: K\n" +
				"        [pc: 18, pc: 41] local: j index: 2 type: J\n" +
				"        [pc: 30, pc: 41] local: i index: 3 type: I\n" +
				"      Local variable type table:\n" +
				"        [pc: 18, pc: 41] local: j index: 2 type: J<java.lang.String>\n" +
				"        [pc: 30, pc: 41] local: i index: 3 type: I<java.lang.String>\n" +
				"  \n" +
				"  // Method descriptor #28 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"  // Stack: 4, Locals: 2\n" +
				"  private static synthetic void lambda$0(java.lang.String s, java.lang.String u);\n" +
				"     0  getstatic java.lang.System.out : java.io.PrintStream [57]\n" +
				"     3  new java.lang.StringBuilder [63]\n" +
				"     6  dup\n" +
				"     7  ldc <String \"m(\"> [65]\n" +
				"     9  invokespecial java.lang.StringBuilder(java.lang.String) [67]\n" +
				"    12  aload_0 [s]\n" +
				"    13  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [70]\n" +
				"    16  aload_1 [u]\n" +
				"    17  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder [70]\n" +
				"    20  bipush 41\n" +
				"    22  invokevirtual java.lang.StringBuilder.append(char) : java.lang.StringBuilder [74]\n" +
				"    25  invokevirtual java.lang.StringBuilder.toString() : java.lang.String [77]\n" +
				"    28  invokevirtual java.io.PrintStream.println(java.lang.String) : void [81]\n" +
				"    31  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 12]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 32] local: s index: 0 type: java.lang.String\n" +
				"        [pc: 0, pc: 32] local: u index: 1 type: java.lang.String\n" +
				"\n" +
				"  Inner classes:\n" +
				"    [inner class info: #109 java/lang/invoke/MethodHandles$Lookup, outer class info: #111 java/lang/invoke/MethodHandles\n" +
				"     inner name: #113 Lookup, accessflags: 25 public static final]\n" +
				"Bootstrap methods:\n" +
				"  0 : # 98 invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"	Method arguments:\n" +
				"		#99 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"		#102 invokestatic X.lambda$0:(Ljava/lang/String;Ljava/lang/String;)V\n" +
				"		#103 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"		#104 4\n" +
				"		#105 2\n" +
				"		#106 (Ljava/lang/String;Ljava/lang/Object;)V\n" +
				"		#107 (Ljava/lang/Object;Ljava/lang/String;)V\n" +
				"}"
				:
				"// Compiled from X.java (" + this.versionString + ", super bit)\n" +
				"public class X {\n" +
				"  Constant pool:\n" +
				"    constant #1 class: #2 X\n" +
				"    constant #2 utf8: \"X\"\n" +
				"    constant #3 class: #4 java/lang/Object\n" +
				"    constant #4 utf8: \"java/lang/Object\"\n" +
				"    constant #5 utf8: \"<init>\"\n" +
				"    constant #6 utf8: \"()V\"\n" +
				"    constant #7 utf8: \"Code\"\n" +
				"    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V\n" +
				"    constant #9 name_and_type: #5.#6 <init> ()V\n" +
				"    constant #10 utf8: \"LineNumberTable\"\n" +
				"    constant #11 utf8: \"LocalVariableTable\"\n" +
				"    constant #12 utf8: \"this\"\n" +
				"    constant #13 utf8: \"LX;\"\n" +
				"    constant #14 utf8: \"main\"\n" +
				"    constant #15 utf8: \"([Ljava/lang/String;)V\"\n" +
				"    constant #16 invoke dynamic: #0 #17 foo ()LK;\n" +
				"    constant #17 name_and_type: #18.#19 foo ()LK;\n" +
				"    constant #18 utf8: \"foo\"\n" +
				"    constant #19 utf8: \"()LK;\"\n" +
				"    constant #20 string: #21 \"direct\"\n" +
				"    constant #21 utf8: \"direct\"\n" +
				"    constant #22 string: #23 \" call\"\n" +
				"    constant #23 utf8: \" call\"\n" +
				"    constant #24 interface_method_ref: #25.#27 K.foo (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #25 class: #26 K\n" +
				"    constant #26 utf8: \"K\"\n" +
				"    constant #27 name_and_type: #18.#28 foo (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #28 utf8: \"(Ljava/lang/String;Ljava/lang/String;)V\"\n" +
				"    constant #29 string: #30 \"bridge\"\n" +
				"    constant #30 utf8: \"bridge\"\n" +
				"    constant #31 string: #32 \" method(j)\"\n" +
				"    constant #32 utf8: \" method(j)\"\n" +
				"    constant #33 interface_method_ref: #34.#36 J.foo (Ljava/lang/Object;Ljava/lang/String;)V\n" +
				"    constant #34 class: #35 J\n" +
				"    constant #35 utf8: \"J\"\n" +
				"    constant #36 name_and_type: #18.#37 foo (Ljava/lang/Object;Ljava/lang/String;)V\n" +
				"    constant #37 utf8: \"(Ljava/lang/Object;Ljava/lang/String;)V\"\n" +
				"    constant #38 string: #39 \" method(i)\"\n" +
				"    constant #39 utf8: \" method(i)\"\n" +
				"    constant #40 interface_method_ref: #41.#43 I.foo (Ljava/lang/String;Ljava/lang/Object;)V\n" +
				"    constant #41 class: #42 I\n" +
				"    constant #42 utf8: \"I\"\n" +
				"    constant #43 name_and_type: #18.#44 foo (Ljava/lang/String;Ljava/lang/Object;)V\n" +
				"    constant #44 utf8: \"(Ljava/lang/String;Ljava/lang/Object;)V\"\n" +
				"    constant #45 utf8: \"x\"\n" +
				"    constant #46 utf8: \"[Ljava/lang/String;\"\n" +
				"    constant #47 utf8: \"k\"\n" +
				"    constant #48 utf8: \"LK;\"\n" +
				"    constant #49 utf8: \"j\"\n" +
				"    constant #50 utf8: \"LJ;\"\n" +
				"    constant #51 utf8: \"i\"\n" +
				"    constant #52 utf8: \"LI;\"\n" +
				"    constant #53 utf8: \"LocalVariableTypeTable\"\n" +
				"    constant #54 utf8: \"LJ<Ljava/lang/String;>;\"\n" +
				"    constant #55 utf8: \"LI<Ljava/lang/String;>;\"\n" +
				"    constant #56 utf8: \"lambda$0\"\n" +
				"    constant #57 field_ref: #58.#60 java/lang/System.out Ljava/io/PrintStream;\n" +
				"    constant #58 class: #59 java/lang/System\n" +
				"    constant #59 utf8: \"java/lang/System\"\n" +
				"    constant #60 name_and_type: #61.#62 out Ljava/io/PrintStream;\n" +
				"    constant #61 utf8: \"out\"\n" +
				"    constant #62 utf8: \"Ljava/io/PrintStream;\"\n" +
				"    constant #63 invoke dynamic: #1 #64 makeConcatWithConstants (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #64 name_and_type: #65.#66 makeConcatWithConstants (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\n" +
				"    constant #65 utf8: \"makeConcatWithConstants\"\n" +
				"    constant #66 utf8: \"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\"\n" +
				"    constant #67 method_ref: #68.#70 java/io/PrintStream.println (Ljava/lang/String;)V\n" +
				"    constant #68 class: #69 java/io/PrintStream\n" +
				"    constant #69 utf8: \"java/io/PrintStream\"\n" +
				"    constant #70 name_and_type: #71.#72 println (Ljava/lang/String;)V\n" +
				"    constant #71 utf8: \"println\"\n" +
				"    constant #72 utf8: \"(Ljava/lang/String;)V\"\n" +
				"    constant #73 utf8: \"s\"\n" +
				"    constant #74 utf8: \"Ljava/lang/String;\"\n" +
				"    constant #75 utf8: \"u\"\n" +
				"    constant #76 utf8: \"SourceFile\"\n" +
				"    constant #77 utf8: \"X.java\"\n" +
				"    constant #78 utf8: \"BootstrapMethods\"\n" +
				"    constant #79 method_ref: #80.#82 java/lang/invoke/LambdaMetafactory.altMetafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #80 class: #81 java/lang/invoke/LambdaMetafactory\n" +
				"    constant #81 utf8: \"java/lang/invoke/LambdaMetafactory\"\n" +
				"    constant #82 name_and_type: #83.#84 altMetafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #83 utf8: \"altMetafactory\"\n" +
				"    constant #84 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\"\n" +
				"    constant #85 method handle: invokestatic (6) #79 \n" +
				"    constant #86 method type: #28 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #87 method_ref: #1.#88 X.lambda$0 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #88 name_and_type: #56.#28 lambda$0 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #89 method handle: invokestatic (6) #87 \n" +
				"    constant #90 method type: #28 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"    constant #91 integer: 4\n" +
				"    constant #92 integer: 2\n" +
				"    constant #93 method type: #44 (Ljava/lang/String;Ljava/lang/Object;)V\n" +
				"    constant #94 method type: #37 (Ljava/lang/Object;Ljava/lang/String;)V\n" +
				"    constant #95 method_ref: #96.#98 java/lang/invoke/StringConcatFactory.makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #96 class: #97 java/lang/invoke/StringConcatFactory\n" +
				"    constant #97 utf8: \"java/lang/invoke/StringConcatFactory\"\n" +
				"    constant #98 name_and_type: #65.#99 makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"    constant #99 utf8: \"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\"\n" +
				"    constant #100 method handle: invokestatic (6) #95 \n" +
				"    constant #101 string: #102 \"m(\\u0001\\u0001)\"\n" +
				"    constant #102 utf8: \"m(\\u0001\\u0001)\"\n" +
				"    constant #103 utf8: \"InnerClasses\"\n" +
				"    constant #104 class: #105 java/lang/invoke/MethodHandles$Lookup\n" +
				"    constant #105 utf8: \"java/lang/invoke/MethodHandles$Lookup\"\n" +
				"    constant #106 class: #107 java/lang/invoke/MethodHandles\n" +
				"    constant #107 utf8: \"java/lang/invoke/MethodHandles\"\n" +
				"    constant #108 utf8: \"Lookup\"\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [8]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 10]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
				"  // Stack: 3, Locals: 4\n" +
				"  public static void main(java.lang.String... x);\n" +
				"     0  invokedynamic 0 foo() : K [16]\n" +
				"     5  astore_1 [k]\n" +
				"     6  aload_1 [k]\n" +
				"     7  ldc <String \"direct\"> [20]\n" +
				"     9  ldc <String \" call\"> [22]\n" +
				"    11  invokeinterface K.foo(java.lang.String, java.lang.String) : void [24] [nargs: 3]\n" +
				"    16  aload_1 [k]\n" +
				"    17  astore_2 [j]\n" +
				"    18  aload_2 [j]\n" +
				"    19  ldc <String \"bridge\"> [29]\n" +
				"    21  ldc <String \" method(j)\"> [31]\n" +
				"    23  invokeinterface J.foo(java.lang.Object, java.lang.String) : void [33] [nargs: 3]\n" +
				"    28  aload_1 [k]\n" +
				"    29  astore_3 [i]\n" +
				"    30  aload_3 [i]\n" +
				"    31  ldc <String \"bridge\"> [29]\n" +
				"    33  ldc <String \" method(i)\"> [38]\n" +
				"    35  invokeinterface I.foo(java.lang.String, java.lang.Object) : void [40] [nargs: 3]\n" +
				"    40  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 12]\n" +
				"        [pc: 6, line: 13]\n" +
				"        [pc: 16, line: 14]\n" +
				"        [pc: 18, line: 15]\n" +
				"        [pc: 28, line: 16]\n" +
				"        [pc: 30, line: 17]\n" +
				"        [pc: 40, line: 18]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 41] local: x index: 0 type: java.lang.String[]\n" +
				"        [pc: 6, pc: 41] local: k index: 1 type: K\n" +
				"        [pc: 18, pc: 41] local: j index: 2 type: J\n" +
				"        [pc: 30, pc: 41] local: i index: 3 type: I\n" +
				"      Local variable type table:\n" +
				"        [pc: 18, pc: 41] local: j index: 2 type: J<java.lang.String>\n" +
				"        [pc: 30, pc: 41] local: i index: 3 type: I<java.lang.String>\n" +
				"  \n" +
				"  // Method descriptor #28 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"  // Stack: 4, Locals: 2\n" +
				"  private static synthetic void lambda$0(java.lang.String s, java.lang.String u);\n" +
				"     0  getstatic java.lang.System.out : java.io.PrintStream [57]\n" +
				"     3  aload_0 [s]\n" +
				"     4  aload_1 [u]\n" +
				"     5  invokedynamic 1 makeConcatWithConstants(java.lang.String, java.lang.String) : java.lang.String [63]\n" +
				"    10  invokevirtual java.io.PrintStream.println(java.lang.String) : void [67]\n" +
				"    13  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 12]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 14] local: s index: 0 type: java.lang.String\n" +
				"        [pc: 0, pc: 14] local: u index: 1 type: java.lang.String\n" +
				"\n" +
				"  Inner classes:\n" +
				"    [inner class info: #104 java/lang/invoke/MethodHandles$Lookup, outer class info: #106 java/lang/invoke/MethodHandles\n" +
				"     inner name: #108 Lookup, accessflags: 25 public static final]\n" +
				"Bootstrap methods:\n" +
				"  0 : # 85 invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"	Method arguments:\n" +
				"		#86 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"		#89 invokestatic X.lambda$0:(Ljava/lang/String;Ljava/lang/String;)V\n" +
				"		#90 (Ljava/lang/String;Ljava/lang/String;)V\n" +
				"		#91 4\n" +
				"		#92 2\n" +
				"		#93 (Ljava/lang/String;Ljava/lang/Object;)V\n" +
				"		#94 (Ljava/lang/Object;Ljava/lang/String;)V,\n" +
				"  1 : # 100 invokestatic java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n" +
				"	Method arguments:\n" +
				"		#101 m()\n" +
				"}";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430571,  [1.8][compiler] Lambda parameter names and annotations don't make it to class files.
public void test430571() throws IOException, ClassFormatException {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_MethodParametersAttribute, CompilerOptions.GENERATE);
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@Target(ElementType.TYPE_USE) @interface A {}\n" +
				"\n" +
				"interface I { int foo(int x); }\n" +
				"\n" +
				"class X {\n" +
				"  	I x = (@A int x) -> x + 1;\n" +
				"}"
			}, customOptions);

		String expectedOutput =
				"  // Method descriptor #23 (I)I\n" +
				"  // Stack: 2, Locals: 1\n" +
				"  private static synthetic int lambda$0(int x);\n" +
				"    0  iload_0 [x]\n" +
				"    1  iconst_1\n" +
				"    2  iadd\n" +
				"    3  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 12]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 4] local: x index: 0 type: int\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" + // Annotations
				"      #26 @A(\n" + // <---
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )";

	verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435869, [1.8][compiler]AIOOB with annotated intersection cast
public void test435869() throws IOException, ClassFormatException {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.io.Serializable;\n" +
				"\n" +
				"public class X {\n" +
				"      Object o = (@Marker1 @Marker2 Serializable & I & @Marker3 @Marker1 J) () -> {};\n" +
				"      public void foo(Object o) {\n" +
				"    	   Serializable oo = (Serializable & @Marker3 @Marker1 @Marker2 I & J) o;\n" +
				"          I i = (@Marker1 @Marker2 Serializable & I & @Marker3 @Marker1 J) () -> {};\n" +
				"      }\n" +
				"}\n" +
				"interface I {\n" +
				"  public void foo(); \n" +
				"}\n" +
				"interface J {\n" +
				"  public void foo();\n" +
				"  public default void bar() {}\n" +
				"}\n" +
				"interface K {\n" +
				"  public void foo();\n" +
				"  public void bar();\n" +
				"}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" +
				"@interface Marker1 {}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n" +
				"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" +
				"@interface Marker3 {}\n"
	});
}
public static Class testClass() {
	return JSR335ClassFileTest.class;
}
}
