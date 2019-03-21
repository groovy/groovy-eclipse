/*******************************************************************************
 * Copyright (c) 2014, 2017 GoPivotal, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 405104 - [1.8][compiler][codegen] Implement support for serializeable lambdas
 *                          Bug 439889 - [1.8][compiler] [lambda] Deserializing lambda fails with IllegalArgumentException: "Invalid lambda deserialization"
 *                          Bug 442416 - $deserializeLambda$ missing cases for nested lambdas
 *                          Bug 442418 - $deserializeLambda$ off-by-one error when deserializing the captured arguments of a lambda that also capture this
 *                          Bug 449467 - [1.8][compiler] Invalid lambda deserialization with anonymous class
 *        Olivier Tardieu tardieu@us.ibm.com - Contributions for
 *                          Bug 442416 - $deserializeLambda$ missing cases for nested lambdas
 *                          Bug 442418 - $deserializeLambda$ off-by-one error when deserializing the captured arguments of a lambda that also capture this
 *        IBM Corporation - Additional tests
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IBootstrapMethodsEntry;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IConstantPoolEntry2;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.util.BootstrapMethodsAttribute;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SerializableLambdaTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_NAMES = new String[] { "testTypeVariable" };
	}
	
	public static Class testClass() {
		return SerializableLambdaTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public SerializableLambdaTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map getCompilerOptions() {
		Map defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
		return defaultOptions;
	}
	
	public static final String RUNNER_CLASS = 
		"public class Y {\n"+
		"  public static void main(String[]args) {\n"+
		"    com.foo.X.main(args);\n"+
		"  }\n"+
		"}";
	
	private static final String HELPER_CLASS =
		"package util;\n"+
		"import java.io.*;\n"+
		"public class Helper {\n"+
		"public static void print(Object o ) {System.err.println(o);}\n"+
	    "static byte[][] data;\n"+
	    "\n"+
	    "public static void write(Object o) { write(0,o); }\n"+
	    "public static void write(int i, Object o) {\n"+
	    "    if (data==null) data=new byte[10][];\n"+
	    "    try {\n"+
	    "        ByteArrayOutputStream baos = new ByteArrayOutputStream();\n"+
	    "        ObjectOutputStream oos = new ObjectOutputStream(baos);\n"+
	    "        oos.writeObject(o);\n"+
	    "        oos.flush();\n"+
	    "        oos.close();\n"+
	    "        data[i] = baos.toByteArray();\n"+
	    "    } catch (Exception e) {\n"+
	    "    }\n"+
	    "}\n"+
	    "\n"+
	    "public static Object read() { return read(0); }\n"+
	    "public static Object read(int i) {\n"+
	    "    try {\n"+
	    "        ByteArrayInputStream bais = new ByteArrayInputStream(data[i]);\n"+
	    "        ObjectInputStream ois = new ObjectInputStream(bais);\n"+
	    "        Object o = ois.readObject();\n"+
	    "        ois.close();\n"+
	    "        return o;\n"+
	    "    } catch (Exception e) {\n"+
	    "    }\n"+
	    "    return null;\n"+
	    "}\n"+
		"}\n";	

	/**
	 * Verifies that after deserializing it is usable, also that the bootstrap methods attribute indicates use of altMetafactory
	 */
	public void test001_simple() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.*;\n"+
					"public class X {\n"+
					"    interface Foo extends Serializable { int m(); }\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"        Foo f1 = null;\n"+
					"        f1 = () -> 3;\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m());\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"3",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	/**
	 * Sanity test, non serializable should have bootstrap methods attribute reference to metafactory.
	 */
	public void test002_simpleNonSerializable() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.*;\n"+
					"public class X {\n"+
					"    interface Foo { int m(); }\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"        Foo f1 = null;\n"+
					"        f1 = () -> 3;\n"+
					"        System.out.println(f1.m());\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"3");
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	/**
	 * Basic test that deserializeLambda can cope with two lambda expressions.
	 */
	public void test003_twoSerializedLambdas() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.*;\n"+
					"public class X {\n"+
					"    interface Foo extends Serializable { int m(); }\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"        Foo f1 = null, f2 = null;\n"+
					"        f1 = () -> 33;\n"+
					"        f2 = () -> 99;\n"+
					"        util.Helper.write(0,f1);\n"+
					"        util.Helper.write(1,f2);\n"+
					"        f2 = (Foo)util.Helper.read(1);\n"+
					"        f1 = (Foo)util.Helper.read(0);\n"+
					"        System.out.println(f1.m());\n"+
					"        System.out.println(f2.m());\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"33\n99",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    1\n"+
				"1: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$1:()I\n"+
				"    ()I\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	public void test004_lambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",
					"public class Y {\n"+
					"  public static void main(String[]args) {\n"+
					"    com.foo.X.main(args);\n"+
					"  }\n"+
					"}",
					"X.java",
					"package com.foo;\n"+
					"import java.io.*;\n"+
					"public class X {\n"+
					"    interface Foo extends Serializable { int m(int i); }\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"        Foo f1 = null, f2 = null;\n"+
					"        f1 = (i) -> i*2;\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m(4));\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"8",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    (I)I\n"+
				"    invokestatic com/foo/X.lambda$0:(I)I\n"+
				"    (I)I\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}
	
	public void test005_capturingVariableLambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",
					"public class Y {\n"+
					"  public static void main(String[]args) {\n"+
					"    com.foo.X.main(args);\n"+
					"  }\n"+
					"}",
					"X.java",
					"package com.foo;\n"+
					"import java.io.*;\n"+
					"public class X {\n"+
					"    interface Foo extends Serializable { int m(int i); }\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"        Foo f1 = null;\n"+
					"        int multiplier = 3;\n"+
					"        f1 = (i) -> i * multiplier;\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m(4));\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"12",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    (I)I\n"+
				"    invokestatic com/foo/X.lambda$0:(II)I\n"+
				"    (I)I\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}

	// differing types, not just int
	public void test006_capturingVariableLambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",
					"public class Y {\n"+
					"  public static void main(String[]args) {\n"+
					"    com.foo.X.main(args);\n"+
					"  }\n"+
					"}",
					"X.java",
					"package com.foo;\n"+
					"import java.io.*;\n"+
					"public class X {\n"+
					"    interface Foo extends Serializable { int m(String n); }\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"        Foo f1 = null;\n"+
					"        int multiplier = 3;\n"+
					"        f1 = (n) -> Integer.valueOf(n) * multiplier;\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m(\"33\"));\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"99",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    (Ljava/lang/String;)I\n"+
				"    invokestatic com/foo/X.lambda$0:(ILjava/lang/String;)I\n"+
				"    (Ljava/lang/String;)I\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}
	
	// Fails the same way as javac right now... with NPE (b120)
	public void xtest007_capturingFieldLambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",
					"public class Y {\n"+
					"  public static void main(String[]args) {\n"+
					"    com.foo.X.main(args);\n"+
					"  }\n"+
					"}",
					"X.java",
					"package com.foo;\n"+
					"import java.io.*;\n"+
					"public class X {\n"+
					"    int multiplier = 3;\n"+
					"    interface Foo extends Serializable { int m(int i); }\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"      new X().run();\n"+
					"    }\n"+
					"    public void run() {\n"+
					"        Foo f1 = null;\n"+
					"        f1 = (i) -> i * this.multiplier;\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m(4));\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"12",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    (I)I\n"+
				"    invokestatic com/foo/X.lambda$0:(II)I\n"+
				"    (I)I\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}
	
	public void test008_capturingTwoVariableLambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",
					"public class Y {\n"+
					"  public static void main(String[]args) {\n"+
					"    com.foo.X.main(args);\n"+
					"  }\n"+
					"}",
					"X.java",
					"package com.foo;\n"+
					"import java.io.*;\n"+
					"public class X {\n"+
					"    interface Foo extends Serializable { float m(int i, float f); }\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"      new X().run();\n"+
					"    }\n"+
					"    public void run() {\n"+
					"        Foo f1 = null;\n"+
					"        f1 = (i,f) -> ((float)i) * f;\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m(3,4.0f));\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"12.0",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    (IF)F\n"+
				"    invokestatic com/foo/X.lambda$0:(IF)F\n"+
				"    (IF)F\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}
	
	public void test009_capturingTwoSlotVariablesLambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",RUNNER_CLASS,
					"X.java",
					"package com.foo;\n"+
					"import java.io.*;\n"+
					"public class X {\n"+
					"    interface Foo extends Serializable { double m(int i, long l); }\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"      new X().run();\n"+
					"    }\n"+
					"    public void run() {\n"+
					"        Foo f1 = null;\n"+
					"        f1 = (i,l) -> (double)(i*l);\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m(3,40L));\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"120.0",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    (IJ)D\n"+
				"    invokestatic com/foo/X.lambda$0:(IJ)D\n"+
				"    (IJ)D\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}
	
	public void test010_VarargsLambdaExpression() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",RUNNER_CLASS,
					"X.java",
					"package com.foo;\n"+
					"import java.io.*;\n"+
					"public class X {\n"+
					"    interface Foo extends Serializable { String m(String... ss); }\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"      new X().run();\n"+
					"    }\n"+
					"    public void run() {\n"+
					"        Foo f1 = null;\n"+
					"        f1 = (strings) -> strings[0]+strings[1];\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m(\"abc\",\"def\"));\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"abcdef",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ([Ljava/lang/String;)Ljava/lang/String;\n"+
				"    invokestatic com/foo/X.lambda$0:([Ljava/lang/String;)Ljava/lang/String;\n"+
				"    ([Ljava/lang/String;)Ljava/lang/String;\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}
	
	// Fails same way as javac right now... with an NPE (b120)
	public void xtest011_CapturingInstance() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",RUNNER_CLASS,
					"X.java",
					"package com.foo;\n"+
					"import java.io.*;\n"+
					"public class X {\n"+
					"    interface Foo extends Serializable { String m(); }\n"+
					"\n"+
					"    String fieldValue = \"hello\";\n"+
					"    public static void main(String[] args) {\n"+
					"      new X().run();\n"+
					"    }\n"+
					"    public void run() {\n"+
					"        Foo f1 = null;\n"+
					"        f1 = () -> this.fieldValue;\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m());\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"abcdef",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ([Ljava/lang/String;)Ljava/lang/String;\n"+
				"    invokestatic com/foo/X.lambda$0:([Ljava/lang/String;)Ljava/lang/String;\n"+
				"    ([Ljava/lang/String;)Ljava/lang/String;\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}

	public void test012_intersectionCast() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.*;\n"+
					"public class X {\n"+
					"    interface Foo extends Serializable { int m(); }\n"+
					"    interface Marker {}\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"        Foo f1 = null;\n"+
					"        f1 = (Foo & Marker) () -> 3;\n"+
					"        System.out.println(\"isMarker?\"+(f1 instanceof Marker));\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m());\n"+
					"        System.out.println(\"isMarker?\"+(f1 instanceof Marker));\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"isMarker?true\n3\nisMarker?true",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    3\n"+ // BitFlags: 0x01 = FLAG_SERIALIZABLE 0x02 = FLAG_MARKER
				"    1\n"+ // Marker interface count
				"    X$Marker\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	public void test013_intersectionCast() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.*;\n"+
					"interface Goo {}\n"+
					"public class X {\n"+
					"    interface Foo extends Serializable { int m(); }\n"+
					"    interface Marker {}\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"        Foo f1 = null;\n"+
					"        f1 = (Foo & Goo & Serializable & Marker) () -> 3;\n"+
					"        System.out.println(\"isMarker?\"+(f1 instanceof Marker));\n"+
					"        System.out.println(\"isGoo?\"+(f1 instanceof Goo));\n"+
					"        System.out.println(\"isSerializable?\"+(f1 instanceof Serializable));\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m());\n"+
					"        System.out.println(\"isMarker?\"+(f1 instanceof Marker));\n"+
					"        System.out.println(\"isGoo?\"+(f1 instanceof Goo));\n"+
					"        System.out.println(\"isSerializable?\"+(f1 instanceof Serializable));\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"isMarker?true\nisGoo?true\nisSerializable?true\n3\nisMarker?true\nisGoo?true\nisSerializable?true",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    3\n"+ // BitFlags: 0x01 = FLAG_SERIALIZABLE 0x02 = FLAG_MARKER
				"    2\n"+ // Marker interface count
				"    Goo\n"+
				"    X$Marker\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	public void test014_intersectionCastAndNotSerializable() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.*;\n"+
					"interface Goo {}\n"+
					"public class X {\n"+
					"    interface Foo { int m(); }\n"+
					"    interface Marker {}\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"        Foo f1 = null;\n"+
					"        f1 = (Foo & Goo & Marker) () -> 3;\n"+
					"        System.out.println(\"isMarker?\"+(f1 instanceof Marker));\n"+
					"        System.out.println(\"isGoo?\"+(f1 instanceof Goo));\n"+
					"        System.out.println(\"isSerializable?\"+(f1 instanceof Serializable));\n"+
					"        System.out.println(f1.m());\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"isMarker?true\nisGoo?true\nisSerializable?false\n3",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    2\n"+ // BitFlags: 0x02 = FLAG_MARKER
				"    2\n"+ // Marker interface count
				"    Goo\n"+
				"    X$Marker\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	public void test015_serializableViaIntersectionCast() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.*;\n"+
					"interface Goo {}\n"+
					"public class X {\n"+
					"    interface Foo { int m(); }\n"+
					"    interface Marker {}\n"+
					"\n"+
					"    public static void main(String[] args) {\n"+
					"        Foo f1 = null;\n"+
					"        f1 = (Foo & Goo & Serializable & Marker) () -> 3;\n"+
					"        System.out.println(\"isMarker?\"+(f1 instanceof Marker));\n"+
					"        System.out.println(\"isGoo?\"+(f1 instanceof Goo));\n"+
					"        System.out.println(\"isSerializable?\"+(f1 instanceof Serializable));\n"+
					"        util.Helper.write(f1);\n"+
					"        f1 = (Foo)util.Helper.read();\n"+
					"        System.out.println(f1.m());\n"+
					"        System.out.println(\"isMarker?\"+(f1 instanceof Marker));\n"+
					"        System.out.println(\"isGoo?\"+(f1 instanceof Goo));\n"+
					"        System.out.println(\"isSerializable?\"+(f1 instanceof Serializable));\n"+
					"    }\n"+
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"isMarker?true\nisGoo?true\nisSerializable?true\n3\nisMarker?true\nisGoo?true\nisSerializable?true",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    3\n"+ // BitFlags: 0x01 = FLAG_SERIALIZABLE 0x02 = FLAG_MARKER
				"    2\n"+ // Marker interface count
				"    Goo\n"+
				"    X$Marker\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	// SAM type not first in intersection cast
	public void test016_bug424211() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.Serializable;\n"+
					"public class X {\n"+
					"	public static void main(String argv[]) throws Exception {\n"+
					"		AutoCloseable one = ((Serializable & AutoCloseable) (() -> {}));\n"+
					"		one.close();\n"+
					"	}\n"+
					"}"
					},
					"",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()V\n"+
				"    invokestatic X.lambda$0:()V\n"+
				"    ()V\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	// Now SAM type first
	public void test017_bug424211() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.Serializable;\n"+
					"public class X {\n"+
					"	public static void main(String argv[]) throws Exception {\n"+
					"		AutoCloseable one = ((AutoCloseable & Serializable) (() -> {}));\n"+
					"		one.close();\n"+
					"	}\n"+
					"}"
					},
					"",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()V\n"+
				"    invokestatic X.lambda$0:()V\n"+
				"    ()V\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	// Not Serializable but a regular marker interface
	public void test018_bug424211() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.Serializable;\n"+
					"interface Marker {}\n"+
					"public class X {\n"+
					"	public static void main(String argv[]) throws Exception {\n"+
					"		AutoCloseable one = ((Marker & AutoCloseable) (() -> {}));\n"+
					"		one.close();\n"+
					"	}\n"+
					"}"
					},
					"",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()V\n"+
				"    invokestatic X.lambda$0:()V\n"+
				"    ()V\n"+
				"    2\n"+
				"    1\n"+
				"    Marker\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	// Now SAM type not first and serialization occurring
	public void test019_bug424211() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.Serializable;\n"+
					"interface SAM {int m();}\n"+
					"public class X {\n"+
					"	public static void main(String argv[]) throws Exception {\n"+
					"		SAM one = ((Serializable & SAM) (() -> 3));\n"+
					"        System.out.println(one.m());\n"+
					"        util.Helper.write(one);\n"+
					"        one = (SAM)util.Helper.read();\n"+
					"        System.out.println(one.m());\n"+
					"	}\n"+
					"}",
					"Helper.java",HELPER_CLASS,
					},
					"3\n3",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	public void test020_lambdaNames() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.Serializable;\n"+
					"interface Foo {int m();}\n"+
					"interface FooN extends Serializable {int m();}\n"+
					"public class X {\n"+
					"	public static void main(String argv[]) throws Exception {\n"+
					"		AutoCloseable one = () -> {};\n"+
					"       new X().m();\n"+
					"       one.close();\n"+
					"	}\n"+
					"   public void m() { Foo f = () -> 3; System.out.println(f.m());}\n"+
					"   public void n() { FooN f = () -> 3; System.out.println(f.m());}\n"+
					"}"
					},
					"3",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
			"  private static synthetic void lambda$0() throws java.lang.Exception;\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
			"  private static synthetic int lambda$1();\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	
	public void test021_lambdaNamesVariants() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.Serializable;\n"+
					"interface Foo {int m();}\n"+
					"interface FooSer extends Serializable {int m();}\n"+
					"interface FooI {int m(int i);}\n"+
					"interface FooSerI extends Serializable {int m(int i);}\n"+
					"public class X {\n"+
					"\n"+
					"   Foo instanceField = () -> 1;\n"+
					"   FooSer instanceFieldSer = () -> 2;\n"+
					"   static Foo staticField = () -> 3;\n"+
					"   static FooSer staticFieldSer = () -> 4;\n"+
					"   FooI instanceFieldI = (i) -> 5;\n"+
					"   FooSerI instanceFieldSerI = (i) -> 6;\n"+
					"\n"+
					"	public static void main(String argv[]) throws Exception {\n"+
					"     int x = 4;\n"+
					"     Foo a = () -> 1;\n"+
					"     FooSer b = () -> 2;\n"+
					"     FooI c = (i) -> 3;\n"+
					"     FooSerI d = (i) -> 4;\n"+
					"     Foo e = () -> x;\n"+
					"     FooSer f = () -> x+1;\n"+
					"     FooI g = (i) -> x+2;\n"+
					"     FooSerI h = (i) -> x+3;\n"+
					"	}\n"+
					"}"
					},
					"",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"static lambda$2()I\n"+
				"static lambda$3()I\n"+
				"static lambda$0()I\n"+
				"static lambda$1()I\n"+
				"static lambda$4(I)I\n"+
				"static lambda$5(I)I\n"+
				"static lambda$6()I\n"+
				"static lambda$7()I\n"+
				"static lambda$8(I)I\n"+
				"static lambda$9(I)I\n"+
				"static lambda$10(I)I\n"+
				"static lambda$11(I)I\n"+
				"static lambda$12(II)I\n"+
				"static lambda$13(II)I\n";
		String actualOutput = printLambdaMethods(OUTPUT_DIR + File.separator + "X.class");
		if (!actualOutput.equals(expectedOutput)) {
			printIt(actualOutput);
			assertEquals(expectedOutput,actualOutput);
		}
	}
	
	public void test022_nestedLambdas() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.Serializable;\n"+
					"interface Foo extends Serializable {int m();}\n"+
					"public class X {\n"+
					"	public static void main(String argv[]) throws Exception {\n"+
					"		Foo f = () -> { return ((Foo)()->33).m();};\n"+
					"       System.out.println(f.m());\n"+
					"       util.Helper.write(f);\n"+
					"       f = (Foo)util.Helper.read();\n"+
					"       System.out.println(f.m());\n"+
					"	}\n"+
					"}",
					"Helper.java",HELPER_CLASS,
					},
					"33\n33",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    1\n"+
				"1: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$1:()I\n"+
				"    ()I\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	public void test023_lambdasInOtherPlaces_Field() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.Serializable;\n"+
					"interface Foo extends Serializable {int m();}\n"+
					"public class X {\n"+
					"   Foo f = () -> 99;\n" +
					"	public static void main(String argv[]) throws Exception {\n"+
					"     new X().run();\n"+
					"   }\n"+
					"   public void run() {\n"+
					"       System.out.println(f.m());\n"+
					"       util.Helper.write(f);\n"+
					"       f = (Foo)util.Helper.read();\n"+
					"       System.out.println(f.m());\n"+
					"	}\n"+
					"}",
					"Helper.java",HELPER_CLASS,
					},
					"99\n99",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	public void test024_lambdasInOtherPlaces_MethodParameter() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.Serializable;\n"+
					"interface Foo extends Serializable {int m();}\n"+
					"public class X {\n"+
					"	public static void main(String argv[]) throws Exception {\n"+
					"       new X().run(()->33);\n"+
					"   }\n"+
					"   public void run(Foo f) {\n"+
					"       System.out.println(f.m());\n"+
					"       util.Helper.write(f);\n"+
					"       f = (Foo)util.Helper.read();\n"+
					"       System.out.println(f.m());\n"+
					"	}\n"+
					"}",
					"Helper.java",HELPER_CLASS,
					},
					"33\n33",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	public void test025_lambdasWithGenericInferencing() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.Serializable;\n"+
					"import java.util.function.*;\n"+
					"public class X {\n"+
					"	public static void main(String argv[]) throws Exception {\n"+
					"       new X().run();\n"+
					"   }\n"+
					"   public void run() {\n"+
					"       IntFunction<Integer> times3 = (IntFunction<Integer> & Serializable) (triple) -> 3 * triple;\n"+
					"       System.out.println(times3.apply(4));\n"+
					"       util.Helper.write(times3);\n"+
					"       times3 = (IntFunction<Integer>)util.Helper.read();\n"+
					"       System.out.println(times3.apply(4));\n"+
					"	}\n"+
					"}",
					"Helper.java",HELPER_CLASS,
					},
					"12\n12",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    (I)Ljava/lang/Object;\n"+
				"    invokestatic X.lambda$0:(I)Ljava/lang/Integer;\n"+
				"    (I)Ljava/lang/Integer;\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	public void test026_lambdasInOtherPlaces_Clinit() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.Serializable;\n"+
					"interface Foo extends Serializable {int m();}\n"+
					"public class X {\n"+
					"   static {\n"+
					"     Foo f = () -> 99;\n" +
					"   }\n"+
					"	public static void main(String argv[]) throws Exception {\n"+
					"     new X().run();\n"+
					"   }\n"+
					"   public void run() {\n"+
					"       Foo f = ()->99;\n"+
					"       System.out.println(f.m());\n"+
					"       util.Helper.write(f);\n"+
					"       f = (Foo)util.Helper.read();\n"+
					"       System.out.println(f.m());\n"+
					"	}\n"+
					"}",
					"Helper.java",HELPER_CLASS,
					},
					"99\n99",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    1\n"+
				"1: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$1:()I\n"+
				"    ()I\n"+
				"    1\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449467 - [1.8][compiler] Invalid lambda deserialization with anonymous class
	public void test449467() throws Exception {
		this.runConformTest(
				new String[]{
					"TestClass.java",
					"import java.io.ByteArrayInputStream;\n"+
					"import java.io.ByteArrayOutputStream;\n"+
					"import java.io.ObjectInputStream;\n"+
					"import java.io.ObjectOutputStream;\n"+
					"import java.io.Serializable;\n"+
					"\n"+
					"public class TestClass implements Serializable {\n"+
					"  String msg = \"HEY!\";\n"+
					"  OtherClass other;\n"+
					"\n"+
					"  public TestClass(StringBuilder sb) {\n"+
					"    other = new OtherClass() {\n"+
					"      {\n"+
					"        other2 = new OtherClass2((Runnable & Serializable) () -> {\n"+
					"          sb.length();\n"+
					"          say();\n"+
					"        });\n"+
					"      }\n"+
					"    };\n"+
					"  }\n"+
					"\n"+
					"  public void say() {\n"+
					"    System.out.println(msg);\n"+
					"  }\n"+
					"\n"+
					"  public static void main(String[] args) throws Exception {\n"+
					"    ByteArrayOutputStream buffer = new ByteArrayOutputStream();\n"+
					"    try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {\n"+
					"      out.writeObject(new TestClass(new StringBuilder()));\n"+
					"    }\n"+
					"    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {\n"+
					"      TestClass s = (TestClass) in.readObject();\n"+
					"      s.say();\n"+
					"    }\n"+
					"  }\n"+
					"}\n"+
					"\n"+
					"class OtherClass implements Serializable {\n"+
					"  OtherClass2 other2;\n"+
					"}\n"+
					"\n"+
					"class OtherClass2 implements Serializable {\n"+
					"  Runnable runnable;\n"+
					"\n"+
					"  public OtherClass2(Runnable runnable) {\n"+
					"    this.runnable = runnable;\n"+
					"  }\n"+
					"}\n"
					},
					"HEY!",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.		
	}
	
	public void test449467_2() throws Exception {
		this.runConformTest(
				new String[]{
					"com/foo/TestClass.java",
					"package com.foo;\n"+
					"import java.io.ByteArrayInputStream;\n"+
					"import java.io.ByteArrayOutputStream;\n"+
					"import java.io.ObjectInputStream;\n"+
					"import java.io.ObjectOutputStream;\n"+
					"import java.io.Serializable;\n"+
					"public class TestClass implements Serializable {\n"+
					"  String msg = \"HEY!\";\n"+
					"  OtherClass other;\n"+
					"\n"+
					"  public TestClass(StringBuilder sb) {\n"+
					"    other = new OtherClass() {\n"+
					"      {\n"+
					"        other2 = new OtherClass2((Runnable & Serializable) () -> {\n"+
					"          sb.length();\n"+
					"          say();\n"+
					"        });\n"+
					"      }\n"+
					"    };\n"+
					"  }\n"+
					"\n"+
					"  public void say() {\n"+
					"    System.out.println(msg);\n"+
					"  }\n"+
					"\n"+
					"  public static void main(String[] args) throws Exception {\n"+
					"    ByteArrayOutputStream buffer = new ByteArrayOutputStream();\n"+
					"    try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {\n"+
					"      out.writeObject(new TestClass(new StringBuilder()));\n"+
					"    }\n"+
					"    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {\n"+
					"      TestClass s = (TestClass) in.readObject();\n"+
					"      s.say();\n"+
					"    }\n"+
					"  }\n"+
					"}\n"+
					"\n"+
					"class OtherClass implements Serializable {\n"+
					"  OtherClass2 other2;\n"+
					"}\n"+
					"\n"+
					"class OtherClass2 implements Serializable {\n"+
					"  Runnable runnable;\n"+
					"\n"+
					"  public OtherClass2(Runnable runnable) {\n"+
					"    this.runnable = runnable;\n"+
					"  }\n"+
					"}\n"
					},
					"HEY!",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.		
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428552,  [1.8][compiler][codegen] Serialization does not work for method references
	public void test428552() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.*;\n" +
					"public class X {\n" +
					"	interface Example extends Serializable {\n" +
					"		String convert(X o);\n" +
					"	}\n" +
					"	public static void main(String[] args) throws IOException {\n" +
					"		Example e=X::toString;\n" +
					"       util.Helper.write(e);\n"+
					"       e = (Example)util.Helper.read();\n"+
					"       System.out.println(e.convert(new X()));\n"+
					"	}\n" +
					"   public String toString() {\n" +
					"       return \"XItIs\";\n" +
					"   }\n" +
					"}\n",
					"Helper.java",HELPER_CLASS,
					},
					"XItIs",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428642
	public void test428642() throws Exception {
		this.runConformTest(
				new String[]{
					"QuickSerializedLambdaTest.java",
					"import java.io.*;\n"+
					"import java.util.function.IntConsumer;\n"+
					"\n"+
					"public class QuickSerializedLambdaTest {\n"+
					"	interface X extends IntConsumer,Serializable{}\n"+
					"	public static void main(String[] args) throws IOException, ClassNotFoundException {\n"+
					"		X x2 = System::exit; // method reference\n"+
					"		ByteArrayOutputStream debug=new ByteArrayOutputStream();\n"+
					"		try(ObjectOutputStream oo=new ObjectOutputStream(debug))\n"+
					"		{\n"+
					"			oo.writeObject(x2);\n"+
					"		}\n"+
					"		try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray())))\n"+
					"		{\n"+
					"			X x=(X)oi.readObject();\n"+
					"			x.accept(0);// shall exit\n"+
					"		}\n"+
					"		throw new AssertionError(\"should not reach this point\");\n"+
					"	}\n"+
					"}\n",
					"Helper.java",
					"public class Helper {\n"+
					"  public static String tostring(java.lang.invoke.SerializedLambda sl) {\n"+
					"    return sl.toString();\n"+
					"  }\n"+
					"}"
				},
				"",
				null,true,
				new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}
	
	public void test428642_2() throws Exception {
		this.runConformTest(
				new String[]{
					"Helper.java",
					"public class Helper {\n"+
					"  public static String tostring(java.lang.invoke.SerializedLambda sl) {\n"+
					"    return sl.toString();\n"+
					"  }\n"+
					"  public static void main(String[]argv) throws Exception {\n"+
					"    foo.QuickSerializedLambdaTest.main(argv);\n"+
					"  }\n"+
					"}",
					"QuickSerializedLambdaTest.java",
					"package foo;\n"+
					"import java.io.*;\n"+
					"import java.util.function.IntConsumer;\n"+
					"\n"+
					"public class QuickSerializedLambdaTest {\n"+
					"	interface X extends IntConsumer,Serializable{}\n"+
					"	public static void main(String[] args) throws IOException, ClassNotFoundException {\n"+
					"		X x1 = i -> System.out.println(i);// lambda expression\n"+
					"		X x2 = System::exit; // method reference\n"+
					"		ByteArrayOutputStream debug=new ByteArrayOutputStream();\n"+
					"		try(ObjectOutputStream oo=new ObjectOutputStream(debug))\n"+
					"		{\n"+
					"			oo.writeObject(x1);\n"+
					"			oo.writeObject(x2);\n"+
					"		}\n"+
					"		try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray())))\n"+
					"		{\n"+
					"			X x=(X)oi.readObject();\n"+
					"			x.accept(42);// shall print \"42\"\n"+
					"			x=(X)oi.readObject();\n"+
					"			x.accept(0);// shall exit\n"+
					"		}\n"+
					"		throw new AssertionError(\"should not reach this point\");\n"+
					"	}\n"+
					"}\n"
				},
				"42",
				null,true,
				new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429112, [1.8][compiler] Exception when compiling Serializable array constructor reference
	public void test429112() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"import java.io.*;\n" +
					"import java.util.function.IntFunction;\n" +
					"public class X {\n" +
					"  interface IF extends IntFunction<Object>, Serializable {}\n" +
					"  public static void main(String[] args) throws IOException, ClassNotFoundException {\n" +
					"    IF factory=String[]::new;\n" +
					"    Object o = factory.apply(1234);\n" +
					"		ByteArrayOutputStream debug=new ByteArrayOutputStream();\n"+
					"		try(ObjectOutputStream oo=new ObjectOutputStream(debug))\n"+
					"		{\n"+
					"			oo.writeObject(factory);\n"+
					"		}\n"+
					"		try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray())))\n"+
					"		{\n"+
					"			IF x = (IF)oi.readObject();\n"+
					"			Object p = x.apply(1234);\n"+
					"           System.out.println(p.getClass());\n" +
					"           String [] sa = (String []) p;\n" +
					"           System.out.println(sa.length);\n" +
					"		}\n"+
					"	}\n"+
					"}\n",
				},
				"class [Ljava.lang.String;\n" + 
				"1234",
				null,true,
				new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439889 - [1.8][compiler] [lambda] Deserializing lambda fails with IllegalArgumentException: "Invalid lambda deserialization"
	public void test439889() throws Exception {
		this.runConformTest(
				new String[]{
					"SerializationTest.java",
					"import java.io.*;\n"+
					"\n"+
					"public class SerializationTest implements Serializable {\n"+
					"	interface SerializableRunnable extends Runnable, Serializable {\n"+
					"	}\n"+
					"\n"+
					"	SerializableRunnable runnable;\n"+
					"\n"+
					"	public SerializationTest() {\n"+
					"		final SerializationTest self = this;\n"+
					"		// runnable = () -> self.doSomething();\n"+
					"		runnable = () -> this.doSomething();\n"+ // results in this method handle: #166 invokespecial SerializationTest.lambda$0:()V
					"       }\n"+
					"\n"+
					"	public void doSomething() {\n"+
					"		System.out.println(\"Hello,world!\");\n"+
					"	}\n"+
					"\n"+
					"	public static void main(String[] args) throws Exception {\n"+
					"		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();\n"+
					"		try (ObjectOutputStream out = new ObjectOutputStream(buffer) ) {\n"+
					"			out.writeObject(new SerializationTest());\n"+
					"		}\n"+
					"		try (ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream(buffer.toByteArray()))) {\n"+
					"			final SerializationTest s = (SerializationTest) in.readObject();\n"+
					"			s.doSomething();\n"+
					"		}\n"+
					"	}\n"+
					"}\n"
					},
					"Hello,world!",
					null,true,
					new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}
	
	public void test439889_2() throws Exception {
		this.runConformTest(
				new String[]{
					"SerializationTest.java",
					"import java.io.*;\n"+
					"\n"+
					"public class SerializationTest implements Serializable {\n"+
					"	interface SerializableRunnable extends Runnable, Serializable {\n"+
					"	}\n"+
					"\n"+
					"	SerializableRunnable runnable;\n"+
					"\n"+
					"	public SerializationTest() {\n"+
					"		final SerializationTest self = this;\n"+
					"		runnable = () -> self.doSomething();\n"+ // results in this method handle: #168 invokestatic SerializationTest.lambda$0:(LSerializationTest;)V
					"		// runnable = () -> this.doSomething();\n"+
					"       }\n"+
					"\n"+
					"	public void doSomething() {\n"+
					"		System.out.println(\"Hello,world!\");\n"+
					"	}\n"+
					"\n"+
					"	public static void main(String[] args) throws Exception {\n"+
					"		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();\n"+
					"		try (ObjectOutputStream out = new ObjectOutputStream(buffer) ) {\n"+
					"			out.writeObject(new SerializationTest());\n"+
					"		}\n"+
					"		try (ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream(buffer.toByteArray()))) {\n"+
					"			final SerializationTest s = (SerializationTest) in.readObject();\n"+
					"			s.doSomething();\n"+
					"		}\n"+
					"	}\n"+
					"}\n"
					},
					"Hello,world!",
					null,true,
					new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}
	
	public void testNestedLambdas_442416() throws Exception {
		this.runConformTest(
				new String[]{
					"Foo.java",
					"import java.io.*;\n"+
					"public class Foo {\n"+
					"   static byte[] toSer(Object o) {\n"+
					"       try {\n"+
					"			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();\n"+
					"			try (ObjectOutputStream out = new ObjectOutputStream(buffer) ) {\n"+
					"				out.writeObject(o);\n"+
					"			}\n"+
					"			return buffer.toByteArray();\n"+
					"       } catch (Exception e) {e.printStackTrace();return null;}\n"+
					"   }\n"+
					"   static Object fromSer(byte[] bs) {\n"+
					"       try {\n"+
					"			try (ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream(bs))) {\n"+
					"				final Object s = in.readObject();\n"+
					"				return s;\n"+
					"			}\n"+
					"       } catch (Exception e) {e.printStackTrace();return null;}\n"+
					"   }\n"+
					"	public static void main(String[] args) throws Exception {\n"+
					"       Runnable nested1,nested2;\n"+
					"		Runnable lambda0 = (java.io.Serializable & Runnable) () -> {\n"+
					"			Runnable lambda1 = (java.io.Serializable & Runnable) () -> {\n"+
					"				Runnable lambda2 = (java.io.Serializable & Runnable) () -> {\n"+
					"					System.out.println(\"Hello,world!\");\n"+
					"				};\n"+
					"       		byte[] bs = toSer(lambda2);\n"+
					"				Runnable r = (Runnable)fromSer(bs);\n"+
					"       		r.run();\n"+
					"			};\n"+
					"       	byte[] bs = toSer(lambda1);\n"+
					"			Runnable r = (Runnable)fromSer(bs);\n"+
					"       	r.run();\n"+
					"		};\n"+
					"       byte[] bs = toSer(lambda0);\n"+
					"		Runnable r = (Runnable)fromSer(bs);\n"+
					"       r.run();\n"+
					"	}\n"+
					"}\n",
				},
				"Hello,world!",
				null,true,
				new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}
	
	public void testBindingThis_442418() throws Exception {
		this.runConformTest(
				new String[]{
					"Foo.java",
					"import java.io.*;\n"+
					"public class Foo implements Serializable {\n"+
					"   static byte[] toSer(Object o) {\n"+
					"       try {\n"+
					"			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();\n"+
					"			try (ObjectOutputStream out = new ObjectOutputStream(buffer) ) {\n"+
					"				out.writeObject(o);\n"+
					"			}\n"+
					"			return buffer.toByteArray();\n"+
					"		} catch (Exception e) {e.printStackTrace();return null;}\n"+
					"	}\n"+
					"	static Object fromSer(byte[] bs) {\n"+
					"	try {\n"+
					"			try (ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream(bs))) {\n"+
					"				final Object s = in.readObject();\n"+
					"				return s;\n"+
					"			}\n"+
					"       } catch (Exception e) {e.printStackTrace();return null;}\n"+
					"   }\n"+
					"		void m(int i) {\n"+
					"			System.out.println(i);\n"+
					"		}\n"+
					"		void n(int i) {\n"+
					"			Runnable lambda = (java.io.Serializable & Runnable) () -> { this.m(i); };\n"+
					"			byte[] bs = toSer(lambda);\n"+
					"			Runnable r = (Runnable)fromSer(bs);\n"+
					"			r.run();\n"+
					"		}\n"+
					"	public static void main(String[] args) throws Exception {\n"+
					"		new Foo().n(42);\n"+
					"	}\n"+
					"}\n",
				},
				"42",
				null,true,
				new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	public void testbug479119() {
		this.runConformTest(
			new String[]{
				"Testbed.java",
				"import java.io.ObjectStreamClass;\n" + 
				"import java.io.Serializable;\n" + 
				"import java.lang.invoke.SerializedLambda;\n" + 
				"import java.lang.reflect.Method;\n" + 
				"import java.util.function.IntFunction;\n" + 
				"import java.util.stream.Stream;\n" + 
				"public class Testbed {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(getMethod(Testbed::foo).equals(getMethod(Testbed::foo)));\n" + 
				"	}\n" + 
				"	private static void foo() { }\n" + 
				"	static interface MethodRef extends Runnable, Serializable { }\n" + 
				"	private static Method getMethod(MethodRef methodRef) {\n" + 
				"		try {\n" + 
				"			final Method invokeWriteReplaceMethod = ObjectStreamClass.class.getDeclaredMethod(\"invokeWriteReplace\", Object.class);\n" + 
				"			invokeWriteReplaceMethod.setAccessible(true);\n" + 
				"			final SerializedLambda l = (SerializedLambda)invokeWriteReplaceMethod.invoke(\n" + 
				"					ObjectStreamClass.lookupAny(methodRef.getClass()),\n" + 
				"					methodRef\n" + 
				"				);\n" + 
				"			System.out.println(\"Looking for \" + l.getImplClass() + \".\" + l.getImplMethodName());\n" + 
				"			final Method[] methods = Stream.of(Class.forName(l.getImplClass()).getDeclaredMethods()).\n" + 
				"				filter(m -> m.getName().equals(l.getImplMethodName())).\n" + 
				"				toArray(Method[]::new);\n" + 
				"			if(methods.length != 1) throw new AssertionError(\"TODO: check signature\");\n" + 
				"			return methods[0];\n" + 
				"		} catch(Exception e) {\n" + 
				"			throw new RuntimeException(e);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
		},
		"Looking for Testbed.foo\n" +
		"Looking for Testbed.foo\n" +
		"true",
		null,true,
		(isJRE9Plus 
		? new String[] { "--add-opens", "java.base/java.io=ALL-UNNAMED" } 
		: new String [] { "-Ddummy" })
		);

		
		String bootstrapEntries = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "Testbed.class");
		String expectedOutput = 
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()V\n"+
				"    invokestatic Testbed.foo:()V\n"+
				"    ()V\n"+
				"    1\n"+
				"1: invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    (Ljava/lang/Object;)Z\n"+
				"    invokestatic Testbed.lambda$2:(Ljava/lang/invoke/SerializedLambda;Ljava/lang/reflect/Method;)Z\n"+
				"    (Ljava/lang/reflect/Method;)Z\n"+
				"2: invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    (I)Ljava/lang/Object;\n"+
				"    invokestatic Testbed.lambda$3:(I)[Ljava/lang/reflect/Method;\n"+
				"    (I)[Ljava/lang/reflect/Method;\n";

		checkExpected(expectedOutput, bootstrapEntries);
	}

	public void testbug479119a() {
		this.runConformTest(
			new String[]{
				"Testbed.java",
				"import java.io.ObjectStreamClass;\n" + 
				"import java.io.Serializable;\n" + 
				"import java.lang.invoke.SerializedLambda;\n" + 
				"import java.lang.reflect.Constructor;\n" + 
				"import java.lang.reflect.Executable;\n" + 
				"import java.lang.reflect.Method;\n" + 
				"import java.util.function.IntFunction;\n" + 
				"import java.util.stream.Stream;\n" + 
				"public class Testbed {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(getMethod(Testbed::foo).equals(getMethod(Testbed::foo)));\n" + 
				"		System.out.println(getMethod(new Foo()::method).equals(getMethod(new Bar()::method)));\n" + 
				"		System.out.println(getMethod(MethodRefImpl::new).equals(getMethod(MethodRefImpl::new)));\n" + 
				"	}\n" + 
				"	static class MethodRefImpl implements MethodRef {\n" + 
				"		@Override\n" + 
				"		public void run() {}\n" + 
				"	}\n" + 
				"	public static class Base {\n" + 
				"        public void method () {}\n" + 
				"    }\n" + 
				"    public static class Foo extends Base {}\n" + 
				"    public static class Bar extends Base {}\n" + 
				"	private static void foo() { }\n" + 
				"	static interface MethodRef extends Runnable, Serializable { }\n" + 
				"	private static Executable getMethod(MethodRef methodRef) {\n" + 
				"		try {\n" + 
				"			final Method invokeWriteReplaceMethod = ObjectStreamClass.class.getDeclaredMethod(\"invokeWriteReplace\", Object.class);\n" + 
				"			invokeWriteReplaceMethod.setAccessible(true);\n" + 
				"			final SerializedLambda l = (SerializedLambda)invokeWriteReplaceMethod.invoke(\n" + 
				"					ObjectStreamClass.lookupAny(methodRef.getClass()),\n" + 
				"					methodRef\n" + 
				"				);\n" + 
				"			System.out.println(\"Looking for \" + l.getImplClass() + \".\" + l.getImplMethodName());\n" + 
				"			boolean isConstructor = l.getImplMethodName().indexOf(\"<init>\") >= 0;\n" + 
				"			final Executable[] methods = Stream.of(isConstructor ? Class.forName(l.getImplClass()).getDeclaredConstructors() : Class.forName(l.getImplClass()).getDeclaredMethods()).\n" + 
				"				filter(m -> m.getName().equals(isConstructor ? l.getImplClass() : l.getImplMethodName())).\n" + 
				"				toArray(isConstructor ? Constructor[]::new : Method[]::new);\n" + 
				"			if(methods.length != 1) throw new AssertionError(\"TODO: check signature\");\n" + 
				"			return methods[0];\n" + 
				"		} catch(Exception e) {\n" + 
				"			throw new RuntimeException(e);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
		},
		"Looking for Testbed.foo\n" +
		"Looking for Testbed.foo\n" +
		"true\n" +
		"Looking for Testbed$Base.method\n" +
		"Looking for Testbed$Base.method\n" +
		"true\n" +
		"Looking for Testbed$MethodRefImpl.<init>\n" +
		"Looking for Testbed$MethodRefImpl.<init>\n" +
		"true",
		null,true,
		(isJRE9Plus 
		? new String[] { "--add-opens", "java.base/java.io=ALL-UNNAMED" } 
		: new String [] { "-Ddummy" })
		);
	}

	// Serializable reference expressions that share the same name
	public void testbug479119b() {
		this.runConformTest(
			new String[]{
				"X.java",
				"import java.io.ByteArrayInputStream;\n" + 
				"import java.io.ByteArrayOutputStream;\n" + 
				"import java.io.IOException;\n" + 
				"import java.io.ObjectInputStream;\n" + 
				"import java.io.ObjectOutputStream;\n" + 
				"import java.io.Serializable;\n" + 
				"public class X {\n" + 
				"    public static interface Consumer<T> extends Serializable {\n" + 
				"        void accept(T t);\n" + 
				"    }\n" + 
				"    public static class Foo {\n" + 
				"    	public void method () {\n" + 
				"        	System.out.println(\"Foo\");\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static class Bar {\n" + 
				"    	public void method () {\n" + 
				"        	System.out.println(\"Bar\");\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main (String[] args) throws IOException, ClassNotFoundException {\n" + 
				"        Consumer<Foo> foo = Foo::method;\n" + 
				"        Consumer<Bar> bar = Bar::method;\n" + 
				"        Consumer<Foo> baz = (b) -> {b.method();};\n" + 
				"        ByteArrayOutputStream debug=new ByteArrayOutputStream();\n" + 
				"		try(ObjectOutputStream oo=new ObjectOutputStream(debug)) {\n" + 
				"			oo.writeObject(bar);\n" + 
				"		}\n" + 
				"		try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray()))) {\n" + 
				"			Consumer<Bar> x = (Consumer)oi.readObject();\n" + 
				"			x.accept(new Bar());\n" + 
				"		}\n" + 
				"		debug.reset();\n" + 
				"		try(ObjectOutputStream oo=new ObjectOutputStream(debug)) {\n" + 
				"			oo.writeObject(foo);\n" + 
				"		}\n" + 
				"		try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray()))) {\n" + 
				"			Consumer<Foo> x = (Consumer)oi.readObject();\n" + 
				"			x.accept(new Foo());\n" + 
				"		}\n" + 
				"    }\n" + 
				"}\n"
		},
		"Bar\n" +
		"Foo",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug479119_comment20() {
		this.runConformTest(
			new String[]{
				"Testbed.java",
				"import java.io.ByteArrayInputStream;\n" + 
				"import java.io.ByteArrayOutputStream;\n" + 
				"import java.io.IOException;\n" + 
				"import java.io.ObjectInputStream;\n" + 
				"import java.io.ObjectOutputStream;\n" + 
				"import java.io.Serializable;\n" + 
				"interface FI extends Serializable{\n" + 
				"	void run(Testbed args);\n" + 
				"}\n" + 
				"interface IF extends Serializable{\n" + 
				"	void run();\n" + 
				"}\n" + 
				"public class Testbed implements Serializable{\n" + 
				"	String f;\n" + 
				"	Testbed(String str) {\n" + 
				"		f = str;\n" + 
				"	}\n" + 
				"	void test() throws IOException, ClassNotFoundException {\n" + 
				"		accept(Testbed::foo);\n" + 
				"		accept(this::foo);		\n" + 
				"	}\n" + 
				"	void foo() {\n" + 
				"		System.out.println(this.f);\n" + 
				"	}\n" + 
				"	void accept(FI fi) {\n" + 
				"		fi.run(this);\n" + 
				"	}\n" + 
				"	void accept(IF i) {\n" + 
				"		i.run();\n" + 
				"	}\n" + 
				"	public static void main(String[] args) throws ClassNotFoundException, IOException {\n" + 
				"		Testbed t = new Testbed(\"IF\");\n" + 
				"		Testbed t2 = new Testbed(\"FI\");\n" + 
				"		IF i = t::foo;\n" + 
				"		FI f = Testbed::foo;\n" + 
				"		ByteArrayOutputStream debug=new ByteArrayOutputStream();\n" + 
				"		try(ObjectOutputStream oo=new ObjectOutputStream(debug))\n" + 
				"		{\n" + 
				"			oo.writeObject(i);\n" + 
				"		}\n" + 
				"		try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray())))\n" + 
				"		{\n" + 
				"			IF x = (IF)oi.readObject();\n" + 
				"			t.accept(x);\n" + 
				"		}\n" + 
				"		debug=new ByteArrayOutputStream();\n" + 
				"		try(ObjectOutputStream oo=new ObjectOutputStream(debug))\n" + 
				"		{\n" + 
				"			oo.writeObject(f);\n" + 
				"		}\n" + 
				"		try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray())))\n" + 
				"		{\n" + 
				"			FI x = (FI)oi.readObject();\n" + 
				"			t2.accept(x);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}"
		},
		"IF\n" +
		"FI",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug485333() {
		this.runConformTest(
			new String[]{
				"Test.java",
				"import java.io.ByteArrayInputStream;\n" + 
				"import java.io.ByteArrayOutputStream;\n" + 
				"import java.io.ObjectInputStream;\n" + 
				"import java.io.ObjectOutputStream;\n" + 
				"import java.io.Serializable;\n" + 
				"interface Func<T> extends Serializable {\n" + 
				"  T get();\n" + 
				"}\n" + 
				"class Impl implements Serializable {\n" + 
				"  int val = 0;\n" + 
				"  public int next() {\n" + 
				"    val += 1;\n" + 
				"    return val;\n" + 
				"  }\n" + 
				"}\n" + 
				"public class Test {\n" + 
				"  final Impl impl = new Impl();\n" + 
				"  final Func<Integer> func = (Func<Integer> & Cloneable)impl::next;\n" + 
				"  public void test() throws Throwable {\n" + 
				"    byte[] bytes = write(func);//25\n" + 
				"    Func<Integer> func = read(bytes);\n" + 
				"    System.out.println(func.get());\n" + 
				"  }\n" + 
				"  public static void main(String[] args) throws Throwable {\n" + 
				"	new Test().test();\n" + 
				"}\n" + 
				"  @SuppressWarnings(\"unchecked\")\n" + 
				"  private static Func<Integer> read(byte[] bytes) throws Exception {\n" + 
				"    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);\n" + 
				"    try (ObjectInputStream ois = new ObjectInputStream(bis)) {\n" + 
				"      return (Func<Integer>) ois.readObject();\n" + 
				"    }\n" + 
				"  }\n" + 
				"  private static byte[] write(Func<Integer> func) throws Exception {\n" + 
				"    ByteArrayOutputStream bos = new ByteArrayOutputStream();\n" + 
				"    System.out.println(func.get());\n" + 
				"    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {\n" + 
				"      oos.writeObject(func);//42\n" + 
				"    }\n" + 
				"    return bos.toByteArray();\n" + 
				"  }\n" + 
				"}"
		},
		"1\n" +
		"2",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug494487() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE);
		this.runConformTest(
			new String[]{
				"Test.java",
				"import java.io.IOException;\n" + 
				"import java.io.Serializable;\n" + 
				"public class Test {\n" + 
				"  class AnException extends Exception {\n" + 
				"  }\n" + 
				"  class Asd {\n" + 
				"    public Asd(String asd) { data = asd; }\n" + 
				"    private final String data;\n" + 
				"    @Override\n" + 
				"    public String toString() {\n" + 
				"      return data;\n" + 
				"    }\n" + 
				"  }\n" + 
				"  public interface Test1 extends Serializable {\n" + 
				"    void test() throws IOException;\n" + 
				"  }\n" + 
				"  public interface Test2 {\n" + 
				"    void test() throws AnException;\n" + 
				"  }\n" + 
				"  public void test1( Test1 test ) {\n" + 
				"    try {\n" + 
				"      test.test();\n" + 
				"    } catch( IOException e ) {\n" + 
				"      e.printStackTrace();\n" + 
				"    }\n" + 
				"  }\n" + 
				"  public void test2( Test2 test ) {\n" + 
				"    try {\n" + 
				"      test.test();\n" + 
				"    } catch( AnException e ) {\n" + 
				"      System.out.println( e );\n" + 
				"    }\n" + 
				"  }\n" + 
				"  public void lambdas() {\n" + 
				"    test1( () -> System.out.println( \"test a\" ) );\n" + 
				"    test1( () -> System.out.println( \"test b\" ) );\n" + 
				"    test2( () -> System.out.println( \"test c\" ) );\n" + 
				"    test2( () -> System.out.println( \"test d\" ) );\n" + 
				"  }\n" + 
				"  public void print( CharSequence a, String b, long c ) {\n" + 
				"    System.out.println( a );\n" + 
				"    System.out.println( b );\n" + 
				"    System.out.println( c );\n" + 
				"  }\n" + 
				"  public void filler() {\n" + 
				"    System.out.println( \"Now we need to get this class file closer to 3000 bytes boundary\" );\n" + 
				"    filler1();\n" + 
				"    filler2();\n" + 
				"    filler3();\n" + 
				"    filler4();\n" + 
				"    filler5();\n" + 
				"    filler6();\n" + 
				"    filler7();\n" + 
				"    filler8();\n" + 
				"    filler9();\n" + 
				"    filler10();\n" + 
				"    filler11();\n" + 
				"    filler12();\n" + 
				"    filler13();\n" + 
				"    filler14();\n" + 
				"    filler15();\n" + 
				"    filler16();\n" + 
				"    filler17();\n" + 
				"    filler18();\n" + 
				"    filler19();\n" + 
				"    filler20();\n" + 
				"    filler21();\n" + 
				"    filler22();\n" + 
				"    filler23();\n" + 
				"    filler24();\n" + 
				"    filler25();\n" + 
				"    filler26();\n" + 
				"    filler27();\n" + 
				"    filler28();\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler28() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler27() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler26() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler25() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler24() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler23() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler22() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler21() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler20() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler19() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler18() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler17() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler16() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler15() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler14() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler13() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler12() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler11() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler10() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler9() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler8() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler7() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler6() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler5() {\n" + 
				"    print( c.toString(), d.toString(), System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler4() {\n" + 
				"    print( a.toString(), b.toString(), System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler3() {\n" + 
				"    print( \"a\", System.getenv( \"asd\" ), System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler2() {\n" + 
				"    print( \"a\", System.lineSeparator(), System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private void filler1() {\n" + 
				"    print( \"a\", \"b\", System.currentTimeMillis() );\n" + 
				"  }\n" + 
				"  private final Asd a = new Asd(\"a\");\n" + 
				"  private final Asd b = new Asd(\"b\");\n" + 
				"  private final Asd c = new Asd(\"c\");\n" + 
				"  private final Asd d = new Asd(\"d\");\n" + 
				"}\n"
		},
		options);
	}
	public void testbug497879() {
		this.runConformTest(
			new String[]{
				"LambdaSerializationTest.java",
				"import java.io.ByteArrayInputStream;\n" + 
				"import java.io.ByteArrayOutputStream;\n" + 
				"import java.io.IOException;\n" + 
				"import java.io.ObjectInputStream;\n" + 
				"import java.io.ObjectOutputStream;\n" + 
				"import java.io.Serializable;\n" + 
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"import java.util.function.Supplier;\n" + 
				"public class LambdaSerializationTest {\n" + 
				"    interface SerializableSupplier<T> extends Supplier<T>, Serializable {}\n" + 
				"    public static void constructorReferenceSerialization() throws IOException, ClassNotFoundException {\n" + 
				"        SerializableSupplier<List<?>> function = ArrayList::new; //Collections::emptyList;\n" + 
				"        Object result = serializeDeserialize(function);\n" + 
				"        Class<?>[] infs = result.getClass().getInterfaces();\n" +
				"        for(int i = 0; i < infs.length; i++) {\n" +
				"            System.out.println(infs[i]);\n" +
				"        }\n" +
				"    }\n" + 
				"    private static Object serializeDeserialize(Object obj) throws IOException, ClassNotFoundException {\n" + 
				"        try (\n" + 
				"            ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //\n" + 
				"            ObjectOutputStream output = new ObjectOutputStream(buffer)) {\n" + 
				"            output.writeObject(obj);\n" + 
				"            try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {\n" + 
				"                return input.readObject();\n" + 
				"            }\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		try {\n" + 
				"			LambdaSerializationTest.constructorReferenceSerialization();\n" + 
				"		} catch (ClassNotFoundException | IOException e) {\n" + 
				"			// TODO Auto-generated catch block\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}"
		},
		"interface LambdaSerializationTest$SerializableSupplier",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug497879a() {
		this.runConformTest(
			new String[]{
				"LambdaSerializationTest.java",
				"import java.io.ByteArrayInputStream;\n" + 
				"import java.io.ByteArrayOutputStream;\n" + 
				"import java.io.IOException;\n" + 
				"import java.io.ObjectInputStream;\n" + 
				"import java.io.ObjectOutputStream;\n" + 
				"import java.io.Serializable;\n" + 
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"import java.util.function.Supplier;\n" + 
				"public class LambdaSerializationTest {\n" + 
				"    interface SerializableSupplier<T> extends Supplier<T>, Serializable {}\n" + 
				"    static class Junk {\n" + 
				"    	private Junk() {}\n" + 
				"    }\n" + 
				"    public static void constructorReferenceSerialization() throws IOException, ClassNotFoundException {\n" + 
				"        SerializableSupplier<Junk> function = Junk::new;\n" + 
				"        Object result = serializeDeserialize(function);\n" + 
				"        Class<?>[] infs = result.getClass().getInterfaces();\n" +
				"        for(int i = 0; i < infs.length; i++) {\n" +
				"            System.out.println(infs[i]);\n" +
				"        }\n" + 
				"    }\n" + 
				"    private static Object serializeDeserialize(Object obj) throws IOException, ClassNotFoundException {\n" + 
				"        try (\n" + 
				"            ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //\n" + 
				"            ObjectOutputStream output = new ObjectOutputStream(buffer)) {\n" + 
				"            output.writeObject(obj);\n" + 
				"            try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {\n" + 
				"                return input.readObject();\n" + 
				"            }\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		try {\n" + 
				"			LambdaSerializationTest.constructorReferenceSerialization();\n" + 
				"		} catch (ClassNotFoundException | IOException e) {\n" + 
				"			// TODO Auto-generated catch block\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}"
		},
		"interface LambdaSerializationTest$SerializableSupplier",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug497879b() {
		this.runConformTest(
			new String[]{
				"LambdaSerializationTest.java",
				"import java.io.ByteArrayInputStream;\n" + 
				"import java.io.ByteArrayOutputStream;\n" + 
				"import java.io.IOException;\n" + 
				"import java.io.ObjectInputStream;\n" + 
				"import java.io.ObjectOutputStream;\n" + 
				"import java.io.Serializable;\n" + 
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"import java.util.function.Supplier;\n" + 
				"public class LambdaSerializationTest {\n" + 
				"    interface SerializableSupplier<T> extends Serializable {\n" +
				"        T get(int count);\n" +
				"    }\n" + 
				"    public static void constructorReferenceSerialization() throws IOException, ClassNotFoundException {\n" + 
				"        SerializableSupplier<List[]> function = ArrayList[]::new;\n" + 
				"        Object result = serializeDeserialize(function);\n" + 
				"        Class<?>[] infs = result.getClass().getInterfaces();\n" +
				"        for(int i = 0; i < infs.length; i++) {\n" +
				"            System.out.println(infs[i]);\n" +
				"        }\n" + 
				"    }\n" + 
				"    private static Object serializeDeserialize(Object obj) throws IOException, ClassNotFoundException {\n" + 
				"        try (\n" + 
				"            ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //\n" + 
				"            ObjectOutputStream output = new ObjectOutputStream(buffer)) {\n" + 
				"            output.writeObject(obj);\n" + 
				"            try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {\n" + 
				"                return input.readObject();\n" + 
				"            }\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		try {\n" + 
				"			LambdaSerializationTest.constructorReferenceSerialization();\n" + 
				"		} catch (ClassNotFoundException | IOException e) {\n" + 
				"			// TODO Auto-generated catch block\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}"
		},
		"interface LambdaSerializationTest$SerializableSupplier",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug503118() {
		this.runConformTest(
			new String[]{
				"lambdabug/App.java",
				"package lambdabug;\n" + 
				"import java.io.ByteArrayInputStream;\n" + 
				"import java.io.ByteArrayOutputStream;\n" + 
				"import java.io.ObjectInputStream;\n" + 
				"import java.io.ObjectOutputStream;\n" + 
				"import java.io.Serializable;\n" + 
				"import java.util.function.Function;\n" + 
				"public class App {\n" + 
				"	public static interface SerialFunction<T, R> extends Function<T, R>, Serializable {\n" + 
				"	}\n" + 
				"	public static interface TestInterface extends Serializable {\n" + 
				"		public Integer method(Integer i);\n" + 
				"	}\n" + 
				"	public static class TestClass implements TestInterface {\n" + 
				"		private static final long serialVersionUID = 1L;\n" + 
				"		@Override\n" + 
				"		public Integer method(Integer i) {\n" + 
				"			return i;\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) throws Exception {\n" + 
				"		TestInterface testService = getService();\n" + 
				"		SerialFunction<Integer, Integer> sf = testService::method;\n" + 
				"		ByteArrayOutputStream bos = new ByteArrayOutputStream();\n" + 
				"		new ObjectOutputStream(bos).writeObject(sf);\n" + 
				"		Object o = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();\n" + 
				"		System.out.println(o.getClass().getInterfaces()[0]);\n" + 
				"	}\n" + 
				"	private static TestInterface getService() {\n" + 
				"		return new TestClass();\n" + 
				"	}\n" + 
				"}\n"
		},
		"interface lambdabug.App$SerialFunction",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug507011() {
		this.runConformTest(
			new String[]{
				"VerifyErrorDerived.java",
				"import java.io.Serializable;\n" + 
				"import java.util.function.Function;\n" + 
				"public class VerifyErrorDerived extends VerifyErrorBase {\n" + 
				"	public static void main(String [] args) {\n" + 
				"		System.out.println(\"hello world\");\n" + 
				"	}\n" + 
				"	public int derivedMethod(String param) {\n" + 
				"		SerializableFunction<String, Integer> f = super::baseMethod;\n" + 
				"		return f.apply(param);\n" + 
				"	}\n" + 
				"}\n" + 
				"interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}",
				"VerifyErrorBase.java",
				"public class VerifyErrorBase {\n" + 
				"	public int baseMethod(String param) {\n" + 
				"		return 7;\n" + 
				"	}\n" + 
				"}\n"
		},
		"hello world",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug509782() {
		this.runConformTest(
			new String[]{
				"compilertest/BaseType.java",
				"package compilertest;\n" + 
				"import java.io.ByteArrayInputStream;\n" + 
				"import java.io.ByteArrayOutputStream;\n" + 
				"import java.io.ObjectInputStream;\n" + 
				"import java.io.ObjectOutputStream;\n" + 
				"import java.io.Serializable;\n" + 
				"import compilertest.sub.SubType;\n" + 
				"public class BaseType implements Serializable {\n" + 
				"    protected void doSomething() {\n" + 
				"    }\n" + 
				"    public static void main(String[] args) throws Exception {\n" + 
				"        SubType instance = new SubType();\n" + 
				"        ByteArrayOutputStream bs = new ByteArrayOutputStream();\n" + 
				"        ObjectOutputStream out = new ObjectOutputStream(bs);\n" + 
				"        out.writeObject(instance);\n" + 
				"        byte[] data = bs.toByteArray();\n" + 
				"        ObjectInputStream in = new ObjectInputStream(\n" + 
				"                new ByteArrayInputStream(data));\n" + 
				"        in.readObject();\n" + 
				"        System.out.println(\"Done\");\n" + 
				"    }\n" + 
				"}",
				"compilertest/sub/SubType.java",
				"package compilertest.sub;\n" + 
				"import java.io.Serializable;\n" + 
				"import compilertest.BaseType;\n" + 
				"public class SubType extends BaseType {\n" + 
				"    Runnable task = (Runnable & Serializable) this::doSomething;\n" + 
				"}\n"
		},
		"Done",
		null,true,
		new String[]{"-Ddummy"});
	}
	// ---
	
	private void checkExpected(String expected, String actual) {
		if (!expected.equals(actual)) {
			printIt(actual);
		}
		assertEquals(expected,actual);
	}
	
	/**
	 * Print a piece of text with the necessary extra quotes and newlines so that it can be cut/pasted into
	 * the test source file.
	 */
	private void printIt(String text) {
		String quotedText = text;
		if (!quotedText.startsWith("\"")) {
			quotedText = "\""+quotedText.replaceAll("\n", "\\\\n\"+\n\"");
			quotedText = quotedText.substring(0,quotedText.length()-3);
		}
		System.out.println(quotedText);
	}
	
	/**
	 * Print the bootstrap methods attribute in a very similar fashion to javap for checking.
	 * Unlike javap the constant pool indexes are not included, to make the test a little less
	 * fragile.
	 */
	private String printBootstrapMethodsAttribute(String filepath) {
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(filepath, IClassFileReader.CLASSFILE_ATTRIBUTES);
		BootstrapMethodsAttribute bootstrapMethodsAttribute = null;
		IClassFileAttribute[] attrs = cfr.getAttributes();
		for (int i=0,max=attrs.length;i<max;i++) {
			if (new String(attrs[i].getAttributeName()).equals("BootstrapMethods")) {
				bootstrapMethodsAttribute = (BootstrapMethodsAttribute)attrs[i];
			}
		}
		if (bootstrapMethodsAttribute==null) {
			return "";
		}
		IConstantPool cp = cfr.getConstantPool();
		StringBuffer sb = new StringBuffer();
		int bmaLength = bootstrapMethodsAttribute.getBootstrapMethodsLength();
		for (int i=0;i<bmaLength;i++) {
			IBootstrapMethodsEntry entry = bootstrapMethodsAttribute.getBootstrapMethods()[i];
			int mr = entry.getBootstrapMethodReference();
			IConstantPoolEntry2 icpe = (IConstantPoolEntry2)cfr.getConstantPool().decodeEntry(mr);
			
			sb.append(i).append(": ").append(formatReferenceKind(icpe.getReferenceKind()));
			sb.append(" ").append(format(cp,icpe.getReferenceIndex()));	
			sb.append("\n");
			int[] args = entry.getBootstrapArguments();
			sb.append("  Method arguments:\n");
			for (int a=0;a<args.length;a++) {
				sb.append("    ").append(format(cp,args[a])).append("\n");
			}
		}
		return sb.toString();
	}
	
	private String printLambdaMethods(String filepath) {
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(filepath, IClassFileReader.METHOD_INFOS);
		IMethodInfo[] methodInfos = cfr.getMethodInfos();
		StringBuffer buf = new StringBuffer();
		for (int i = 0, max = methodInfos.length; i < max; i++) {
			IMethodInfo methodInfo = methodInfos[i];
			if (!new String(methodInfo.getName()).startsWith("lambda")) 
				continue;
			int accessFlags = methodInfo.getAccessFlags();
			if (Modifier.isStatic(accessFlags)) {
				buf.append("static ");
			}
			buf.append(methodInfo.getName());
			buf.append(methodInfo.getDescriptor());
			buf.append("\n");
		}
		return buf.toString();
	}
	
	String formatReferenceKind(int kind) {
		switch (kind) {
			case IConstantPoolConstant.METHOD_TYPE_REF_InvokeStatic:
				return "invokestatic";
			default:
				throw new IllegalStateException("nyi for "+kind);
		}
	}
	
	String format(IConstantPool cp, int entryNumber) {
		IConstantPoolEntry entry = cp.decodeEntry(entryNumber);
		if (entry == null) {
			return "null";
		}
		switch (entry.getKind()) {
			case IConstantPoolConstant.CONSTANT_Integer:
				return Integer.toString(entry.getIntegerValue());
			case IConstantPoolConstant.CONSTANT_Utf8:
				return new String(entry.getUtf8Value());
			case IConstantPoolConstant.CONSTANT_Methodref:
				return new String(entry.getClassName())+"."+new String(entry.getMethodName())+":"+new String(entry.getMethodDescriptor());
			case IConstantPoolConstant.CONSTANT_MethodHandle:
				IConstantPoolEntry2 entry2 = (IConstantPoolEntry2)entry;
				return formatReferenceKind(entry2.getReferenceKind())+" "+format(cp,entry2.getReferenceIndex());
			case IConstantPoolConstant.CONSTANT_MethodType:
				return format(cp,((IConstantPoolEntry2)entry).getDescriptorIndex());
			case IConstantPoolConstant.CONSTANT_Class:
				return new String(entry.getClassInfoName());
			default:
					throw new IllegalStateException("nyi for "+entry.getKind());
		}
	}
	
}

