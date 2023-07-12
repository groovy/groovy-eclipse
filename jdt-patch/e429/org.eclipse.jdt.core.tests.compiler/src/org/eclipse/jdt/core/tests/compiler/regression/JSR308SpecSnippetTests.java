/*******************************************************************************
 * Copyright (c) 2011, 2020 IBM Corporation and others.
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
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 415541 - [1.8][compiler] Type annotations in the body of static initializer get dropped
 *                          Bug 415543 - [1.8][compiler] Incorrect bound index in RuntimeInvisibleTypeAnnotations attribute
 *                          Bug 415397 - [1.8][compiler] Type Annotations on wildcard type argument dropped
 *                          Bug 415399 - [1.8][compiler] Type annotations on constructor results dropped by the code generator
 *                          Bug 415470 - [1.8][compiler] Type annotations on class declaration go vanishing
 *                          Bug 414384 - [1.8] type annotation on abbreviated inner class is not marked as inner type
 *     Jesper S Moller - Contributions for
 *                          Bug 416885 - [1.8][compiler]IncompatibleClassChange error (edit)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JSR308SpecSnippetTests extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 19 };
//		TESTS_NAMES = new String [] { "test033" };
	}
	public static Class testClass() {
		return JSR308SpecSnippetTests.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public JSR308SpecSnippetTests(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	@Override
	protected Map getCompilerOptions() {
		Map defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
		return defaultOptions;
	}

	public void test001() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.List;\n" +
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class Document {}\n" +
				"public class X {\n" +
				"	Map<@NonNull String, @NonEmpty List<@Readonly Document>> files;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @NonNull(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"      #11 @NonEmpty(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(1)]\n" +
				"      )\n" +
				"      #12 @Readonly(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// note, javac 8b100 emits offset incorrectly.
	public void test002() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class Document {}\n" +
				"public class X {\n" +
				"	static void foo(X o) {\n" +
				"		o.<@NonNull String>m(\"...\");\n" +
				"	}\n" +
				"	<T> void m(String s) {}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #24 @NonNull(\n" +
				"        target type = 0x49 METHOD_INVOCATION_TYPE_ARGUMENT\n" +
				"        offset = 3\n" +
				"        type argument index = 0\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test003() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.Collection;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Existing {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class File {}\n" +
				"class X<F extends @Existing File> { \n" +
				"	Collection<? super @Existing File> c;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @Existing(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0), WILDCARD]\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #12 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [14]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 11]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X<F>\n" +
				"\n" +
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #10 @Existing(\n" +
				"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
				"      type parameter index = 0 type parameter bound index = 0\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test004() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.List;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Existing {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class File {}\n" +
				"abstract class X<T> implements @Readonly List<@Readonly T> { }\n",
		},
		"");
		String expectedOutput =
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #23 @Readonly(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"    )\n" +
				"    #23 @Readonly(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(0)]\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test005() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.List;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Critical {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class TemperatureException extends RuntimeException{}\n" +
				"class X {\n" +
				"	void monitorTemperature() throws @Critical TemperatureException {}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #19 @Critical(\n" +
				"        target type = 0x17 THROWS\n" +
				"        throws index = 0\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test006() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Interned {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Tainted {}\n" +
				"class MyObject {\n" +
				"	class NestedClass {}\n" +
				"}\n" +
				"class List<T> {}\n" +
				"class X {\n" +
				"	static void monitorTemperature(MyObject myVar) {\n" +
				"		new <String> @Interned MyObject();\n" +
				"		new @NonEmpty @Readonly List<String>();\n" +
				"		myVar.new @Tainted NestedClass();\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #33 @Interned(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 0\n" +
				"      )\n" +
				"      #34 @NonEmpty(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 6\n" +
				"      )\n" +
				"      #35 @Readonly(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 6\n" +
				"      )\n" +
				"      #36 @Tainted(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 12\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test007() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.Map;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Tainted {}\n" +
				"class MyObject {\n" +
				"	class NestedClass {}\n" +
				"}\n" +
				"class List<T> {}\n" +
				"class X {\n" +
				"		Map.@NonNull Entry e;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @NonNull(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test008() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface A {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface B {}\n" +
				"class MyObject {\n" +
				"	class NestedClass {}\n" +
				"}\n" +
				"class List<T> {}\n" +
				"class Type1 {}\n" +
				"interface Type2 {}\n" +
				"class X {\n" +
				"	static void monitorTemperature(Object myObject) {\n" +
				"		String myString = (@NonNull String) myObject;\n" +
				"		Type1 x = (@A Type1 & @B Type2) null;\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #29 @NonNull(\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 1\n" +
				"        type argument index = 0\n" +
				"      )\n" +
				"      #30 @B(\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 6\n" +
				"        type argument index = 1\n" +
				"      )\n" +
				"      #31 @A(\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 9\n" +
				"        type argument index = 0\n" +
				"      )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test009() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface A {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface B {}\n" +
				"class MyObject {\n" +
				"	class NestedClass {}\n" +
				"}\n" +
				"class List<T> {}\n" +
				"class Type1 {}\n" +
				"interface Type2 {}\n" +
				"class X {\n" +
				"	static void monitorTemperature(Object myObject) {\n" +
				"		boolean isNonNull = myObject instanceof @NonNull String;\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #23 @NonNull(\n" +
				"        target type = 0x43 INSTANCEOF\n" +
				"        offset = 1\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test010() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.Arrays;\n" +
				"import java.util.Date;\n" +
				"import java.util.List;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface English {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Vernal {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNegative {}\n" +
				"class MyObject {\n" +
				"	class NestedClass {}\n" +
				"}\n" +
				"class Type1 {}\n" +
				"interface I {\n" +
				"	int f(Date d);\n" +
				"}\n" +
				"interface J {\n" +
				"	int f(List l);\n" +
				"}\n" +
				"interface K {\n" +
				"	void s(int [] ia);\n" +
				"}\n" +
				"class X {\n" +
				"	static void monitorTemperature(Object myObject) {\n" +
				"		I i = @Vernal Date::getDay;\n" +
				"		J j  = List<@English String>::size;\n" +
				"		K k = Arrays::<@NonNegative Integer>sort;\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #36 @Vernal(\n" +
				"        target type = 0x46 METHOD_REFERENCE\n" +
				"        offset = 0\n" +
				"      )\n" +
				"      #37 @English(\n" +
				"        target type = 0x46 METHOD_REFERENCE\n" +
				"        offset = 6\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"      #38 @NonNegative(\n" +
				"        target type = 0x4b METHOD_REFERENCE_TYPE_ARGUMENT\n" +
				"        offset = 12\n" +
				"        type argument index = 0\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test011() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.*;\n" +
				"import java.io.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Immutable { int value() default 0; }\n" +
				"class X {\n" +
				"	List<@Immutable ? extends Comparable<X>> a;\n" +
				"	List<? extends @Immutable Comparable<X>> b;\n" +
				"	List<@Immutable(1) ? extends @Immutable(2) Comparable<X>> c;\n" +
				"	Map<@Immutable(1) ? extends Comparable<X>,@Immutable(2) ? extends @Immutable(3) Serializable> d;\n" +
				"}\n",
		},
		"");
		// javac b100
		// Field a:
		//   RuntimeInvisibleTypeAnnotations:
		//    0: #9(): FIELD, location=[TYPE_ARGUMENT(0)]
		// Field b:
		//   RuntimeInvisibleTypeAnnotations:
		//    0: #9(): FIELD, location=[TYPE_ARGUMENT(0), WILDCARD]
		// Field c:
		//   RuntimeInvisibleTypeAnnotations:
		//    0: #9(#12=I#13): FIELD, location=[TYPE_ARGUMENT(0)]
		//    1: #9(#12=I#14): FIELD, location=[TYPE_ARGUMENT(0), WILDCARD]
		// Field d:
		//   RuntimeInvisibleTypeAnnotations:
		//    0: #9(#12=I#13): FIELD, location=[TYPE_ARGUMENT(0)]
		//    1: #9(#12=I#14): FIELD, location=[TYPE_ARGUMENT(1)]
		//    2: #9(#12=I#18): FIELD, location=[TYPE_ARGUMENT(1), WILDCARD]
		String expectedOutput =
				"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" +
				"class X {\n" +
				"  Constant pool:\n" +
				"    constant #1 class: #2 X\n" +
				"    constant #2 utf8: \"X\"\n" +
				"    constant #3 class: #4 java/lang/Object\n" +
				"    constant #4 utf8: \"java/lang/Object\"\n" +
				"    constant #5 utf8: \"a\"\n" +
				"    constant #6 utf8: \"Ljava/util/List;\"\n" +
				"    constant #7 utf8: \"Signature\"\n" +
				"    constant #8 utf8: \"Ljava/util/List<+Ljava/lang/Comparable<LX;>;>;\"\n" +
				"    constant #9 utf8: \"RuntimeInvisibleTypeAnnotations\"\n" +
				"    constant #10 utf8: \"LImmutable;\"\n" +
				"    constant #11 utf8: \"b\"\n" +
				"    constant #12 utf8: \"c\"\n" +
				"    constant #13 utf8: \"value\"\n" +
				"    constant #14 integer: 1\n" +
				"    constant #15 integer: 2\n" +
				"    constant #16 utf8: \"d\"\n" +
				"    constant #17 utf8: \"Ljava/util/Map;\"\n" +
				"    constant #18 utf8: \"Ljava/util/Map<+Ljava/lang/Comparable<LX;>;+Ljava/io/Serializable;>;\"\n" +
				"    constant #19 integer: 3\n" +
				"    constant #20 utf8: \"<init>\"\n" +
				"    constant #21 utf8: \"()V\"\n" +
				"    constant #22 utf8: \"Code\"\n" +
				"    constant #23 method_ref: #3.#24 java/lang/Object.<init> ()V\n" +
				"    constant #24 name_and_type: #20.#21 <init> ()V\n" +
				"    constant #25 utf8: \"LineNumberTable\"\n" +
				"    constant #26 utf8: \"LocalVariableTable\"\n" +
				"    constant #27 utf8: \"this\"\n" +
				"    constant #28 utf8: \"LX;\"\n" +
				"    constant #29 utf8: \"SourceFile\"\n" +
				"    constant #30 utf8: \"X.java\"\n" +
				"  \n" +
				"  // Field descriptor #6 Ljava/util/List;\n" +
				"  // Signature: Ljava/util/List<+Ljava/lang/Comparable<LX;>;>;\n" +
				"  java.util.List a;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @Immutable(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"  \n" +
				"  // Field descriptor #6 Ljava/util/List;\n" +
				"  // Signature: Ljava/util/List<+Ljava/lang/Comparable<LX;>;>;\n" +
				"  java.util.List b;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @Immutable(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0), WILDCARD]\n" +
				"      )\n" +
				"  \n" +
				"  // Field descriptor #6 Ljava/util/List;\n" +
				"  // Signature: Ljava/util/List<+Ljava/lang/Comparable<LX;>;>;\n" +
				"  java.util.List c;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @Immutable(\n" +
				"        #13 value=(int) 1 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"      #10 @Immutable(\n" +
				"        #13 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0), WILDCARD]\n" +
				"      )\n" +
				"  \n" +
				"  // Field descriptor #17 Ljava/util/Map;\n" +
				"  // Signature: Ljava/util/Map<+Ljava/lang/Comparable<LX;>;+Ljava/io/Serializable;>;\n" +
				"  java.util.Map d;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @Immutable(\n" +
				"        #13 value=(int) 1 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"      #10 @Immutable(\n" +
				"        #13 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(1)]\n" +
				"      )\n" +
				"      #10 @Immutable(\n" +
				"        #13 value=(int) 3 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(1), WILDCARD]\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #21 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [23]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test012() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class Document {}\n" +
				"class X {\n" +
				"	@Readonly Document [][] docs1 = new @Readonly Document [2][12]; // array of arrays of read-only documents\n" +
				"	Document @Readonly [][] docs2 = new Document @Readonly [2][12]; // read-only array of arrays of documents\n" +
				"	Document[] @Readonly [] docs3 = new Document[2] @Readonly [12]; // array of read-only arrays of documents\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  Document[][] docs1;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @Readonly(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +
				"  \n" +
				"  // Field descriptor #6 [[LDocument;\n" +
				"  Document[][] docs2;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @Readonly(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n" +
				"  // Field descriptor #6 [[LDocument;\n" +
				"  Document[][] docs3;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @Readonly(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY]\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #12 ()V\n" +
				"  // Stack: 3, Locals: 1\n" +
				"  X();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokespecial java.lang.Object() [14]\n" +
				"     4  aload_0 [this]\n" +
				"     5  iconst_2\n" +
				"     6  bipush 12\n" +
				"     8  multianewarray Document[][] [16]\n" +
				"    12  putfield X.docs1 : Document[][] [17]\n" +
				"    15  aload_0 [this]\n" +
				"    16  iconst_2\n" +
				"    17  bipush 12\n" +
				"    19  multianewarray Document[][] [16]\n" +
				"    23  putfield X.docs2 : Document[][] [19]\n" +
				"    26  aload_0 [this]\n" +
				"    27  iconst_2\n" +
				"    28  bipush 12\n" +
				"    30  multianewarray Document[][] [16]\n" +
				"    34  putfield X.docs3 : Document[][] [21]\n" +
				"    37  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"        [pc: 4, line: 7]\n" +
				"        [pc: 15, line: 8]\n" +
				"        [pc: 26, line: 9]\n" +
				"        [pc: 37, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 38] local: this index: 0 type: X\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @Readonly(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 8\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #8 @Readonly(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 19\n" +
				"      )\n" +
				"      #8 @Readonly(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 30\n" +
				"        location = [ARRAY]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test013() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Immutable {}\n" +
				"class Document {}\n" +
				"class X {\n" +
				"	@Immutable X() {\n" +
				"	}\n" +
				"}\n",
		},
		"");
		// javac b100 gives:
		//		RuntimeInvisibleTypeAnnotations:
		//		      0: #9(): METHOD_RETURN
		String expectedOutput =
				"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" +
				"class X {\n" +
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
				"    constant #14 utf8: \"RuntimeInvisibleTypeAnnotations\"\n" +
				"    constant #15 utf8: \"LImmutable;\"\n" +
				"    constant #16 utf8: \"SourceFile\"\n" +
				"    constant #17 utf8: \"X.java\"\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [8]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"        [pc: 4, line: 8]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #15 @Immutable(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test014() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Immutable {}\n" +
				"class Document {}\n" +
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"class X {\n" +
				"	void foo (X this, X this) {\n" +
				"	}\n" +
				"	static void foo (X this) {\n" +
				"	}\n" +
				"	I i = (X this) -> {};\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	void foo(X this) {}\n" +
				"	void foo(Y this, int x) {}\n" +
				"	class Z {\n" +
				"		void foo(Y<T>.Z this) {}\n" +
				"	}\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	void foo (X this, X this) {\n" +
		"	                    ^^^^\n" +
		"Only the first formal parameter may be declared explicitly as \'this\'\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	static void foo (X this) {\n" +
		"	                   ^^^^\n" +
		"Explicit \'this\' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 14)\n" +
		"	I i = (X this) -> {};\n" +
		"	      ^^^^^^^^^^^\n" +
		"Lambda expression\'s signature does not match the signature of the functional interface method foo()\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 14)\n" +
		"	I i = (X this) -> {};\n" +
		"	         ^^^^\n" +
		"Lambda expressions cannot declare a this parameter\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 17)\n" +
		"	void foo(X this) {}\n" +
		"	         ^\n" +
		"The declared type of the explicit \'this\' parameter is expected to be Y<T>\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 18)\n" +
		"	void foo(Y this, int x) {}\n" +
		"	         ^\n" +
		"Y is a raw type. References to generic type Y<T> should be parameterized\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 18)\n" +
		"	void foo(Y this, int x) {}\n" +
		"	         ^\n" +
		"The declared type of the explicit \'this\' parameter is expected to be Y<T>\n" +
		"----------\n");
	}
	public void test015() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Immutable {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class Document {}\n" +
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"class X {\n" +
				"	class Y {\n" +
				"		void foo(@Immutable X.@Readonly Y this) {\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #21 @Immutable(\n" +
				"        target type = 0x15 METHOD_RECEIVER\n" +
				"      )\n" +
				"      #22 @Readonly(\n" +
				"        target type = 0x15 METHOD_RECEIVER\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X$Y.class", "Y", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test016() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface A {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface B {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface C {}\n" +
				"public class X {}\n" +
				"class Outer {\n" +
				"    class Middle {\n" +
				"        class Inner {\n" +
				"            void innerMethod(@A Outer.@B Middle.@C Inner this) { }\n" +
				"        }\n" +
				"    }\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #21 @A(\n" +
				"        target type = 0x15 METHOD_RECEIVER\n" +
				"      )\n" +
				"      #22 @B(\n" +
				"        target type = 0x15 METHOD_RECEIVER\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n" +
				"      #23 @C(\n" +
				"        target type = 0x15 METHOD_RECEIVER\n" +
				"        location = [INNER_TYPE, INNER_TYPE]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "Outer$Middle$Inner.class", "Inner", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test017() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Result {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Receiver {}\n" +
				"class Document {}\n" +
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"class X {\n" +
				"	class Y {\n" +
				"		 Y(@Receiver X X.this, boolean b) { }\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #22 @Receiver(\n" +
				"        target type = 0x15 METHOD_RECEIVER\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X$Y.class", "Y", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test018() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*;  \n" +
				"@interface Receiver {}\n" +
				"class Document {}\n" +
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"class X {\n" +
				"	void foo(@Receiver X this) {}\n" +
				"	class Y {\n" +
				"		 Y(@Receiver X X.this, boolean b) { }\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	void foo(@Receiver X this) {}\n" +
			"	         ^^^^^^^^^\n" +
			"Annotation types that do not specify explicit target element types cannot be applied here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	Y(@Receiver X X.this, boolean b) { }\n" +
			"	  ^^^^^^^^^\n" +
			"Annotation types that do not specify explicit target element types cannot be applied here\n" +
			"----------\n");
	}
	public void test019() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class X<@Readonly T> {\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #21 @Readonly(\n" +
				"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
				"      type parameter index = 0\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test020() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.@NotAllowed Date; // illegal!\n" +
				"import @IllegalSyntax java.util.Date; // illegal syntax\n" +
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Even {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NotAllowed {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface IllegalSyntax {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Legal {}\n" +
				"class X {\n" +
				"	static int staticField;\n" +
				"	static class StaticNestedClass {}\n" +
				"	void foo() {\n" +
				"		Object o = @Even int.class; // illegal!\n" +
				"		o = int @NonEmpty [].class; // illegal!\n" +
				"		int x = @IllegalSyntax X.staticField;\n" +
				"		StaticNestedClass snc = (@IllegalSyntax X.StaticNestedClass) null;\n" +
				"		X.@Legal StaticNestedClass lsnc = (X.@Legal StaticNestedClass) null;\n" +
				"	}\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import java.util.@NotAllowed Date; // illegal!\n" +
		"	                 ^^^^^^^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	import @IllegalSyntax java.util.Date; // illegal syntax\n" +
		"	       ^^^^^^^^^^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 2)\n" +
		"	import @IllegalSyntax java.util.Date; // illegal syntax\n" +
		"	                      ^^^^^^^^^^^^^^\n" +
		"The import java.util.Date is never used\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 19)\n" +
		"	Object o = @Even int.class; // illegal!\n" +
		"	           ^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 20)\n" +
		"	o = int @NonEmpty [].class; // illegal!\n" +
		"	        ^^^^^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 21)\n" +
		"	int x = @IllegalSyntax X.staticField;\n" +
		"	        ^^^^^^^^^^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 22)\n" +
		"	StaticNestedClass snc = (@IllegalSyntax X.StaticNestedClass) null;\n" +
		"	                         ^^^^^^^^^^^^^^\n" +
		"Type annotations are not allowed on type names used to access static members\n" +
		"----------\n");
	}
	public void test021() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.@NotAllowed Date; // illegal!\n" +
				"import @IllegalSyntax java.util.Date; // illegal syntax\n" +
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Even {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonEmpty {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NotAllowed {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface IllegalSyntax {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Legal {}\n" +
				"interface I {\n" +
				"	int f(Y y);\n" +
				"}\n" +
				"class Y {\n" +
				"	int f;\n" +
				"	int x(Y y) {}\n" +
				"}\n" +
				"class X extends Y {\n" +
				"	static int staticField;\n" +
				"	static class StaticNestedClass {}\n" +
				"	void foo() {\n" +
				"		Object o = @Even int.class; // illegal!\n" +
				"		o = int @NonEmpty [].class; // illegal!\n" +
				"		int x = @IllegalSyntax X.staticField;\n" +
				"		StaticNestedClass snc = (@IllegalSyntax X.StaticNestedClass) null;\n" +
				"		X.@Legal StaticNestedClass lsnc = (X.@Legal StaticNestedClass) null;\n" +
				"		int x2 = @IllegalSyntax X.super.f;\n" +
				"		I i = @IllegalSyntax X.super::x;\n" +
				"	}\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	import java.util.@NotAllowed Date; // illegal!\n" +
		"	                 ^^^^^^^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	import @IllegalSyntax java.util.Date; // illegal syntax\n" +
		"	       ^^^^^^^^^^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 2)\n" +
		"	import @IllegalSyntax java.util.Date; // illegal syntax\n" +
		"	                      ^^^^^^^^^^^^^^\n" +
		"The import java.util.Date is never used\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 26)\n" +
		"	Object o = @Even int.class; // illegal!\n" +
		"	           ^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 27)\n" +
		"	o = int @NonEmpty [].class; // illegal!\n" +
		"	        ^^^^^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 28)\n" +
		"	int x = @IllegalSyntax X.staticField;\n" +
		"	        ^^^^^^^^^^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 29)\n" +
		"	StaticNestedClass snc = (@IllegalSyntax X.StaticNestedClass) null;\n" +
		"	                         ^^^^^^^^^^^^^^\n" +
		"Type annotations are not allowed on type names used to access static members\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 31)\n" +
		"	int x2 = @IllegalSyntax X.super.f;\n" +
		"	         ^^^^^^^^^^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n" +
		"9. ERROR in X.java (at line 32)\n" +
		"	I i = @IllegalSyntax X.super::x;\n" +
		"	      ^^^^^^^^^^^^^^\n" +
		"Syntax error, type annotations are illegal here\n" +
		"----------\n");
	}
	public void test022() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"class X {\n" +
				"	@Readonly int foo() @Readonly [] {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #17 @Readonly(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"        location = [ARRAY]\n" +
				"      )\n" +
				"      #17 @Readonly(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test023() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"interface X {\n" +
				"	default @Readonly int foo() @Readonly [] {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #13 @Readonly(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"        location = [ARRAY]\n" +
				"      )\n" +
				"      #13 @Readonly(\n" +
				"        target type = 0x14 METHOD_RETURN\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test024() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Critical {}\n" +
				"class X {\n" +
				"	void foo() {\n" +
				"		try {\n" +
				"           System.out.println();\n" +
				"		} catch (@Readonly NullPointerException | @Critical ArrayIndexOutOfBoundsException e) {\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #34 @Readonly(\n" +
				"        target type = 0x42 EXCEPTION_PARAMETER\n" +
				"        exception table index = 0\n" +
				"      )\n" +
				"      #35 @Critical(\n" +
				"        target type = 0x42 EXCEPTION_PARAMETER\n" +
				"        exception table index = 1\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test025() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Critical {}\n" +
				"class X {\n" +
				"	void foo(@Readonly int [] [] @Critical ... x) {\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #19 @Readonly(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"        location = [ARRAY, ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #20 @Critical(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test026() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Readonly {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Critical {}\n" +
				"class X {\n" +
				"	void foo(@Readonly int [] [] @Critical ... x) {\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #19 @Readonly(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"        location = [ARRAY, ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #20 @Critical(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test027() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"class X {\n" +
				"	@NonNull String var1, arr2[];\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @NonNull(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"  \n" +
				"  // Field descriptor #10 [Ljava/lang/String;\n" +
				"  java.lang.String[] arr2;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @NonNull(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test028() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.List;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"class X<@NonNull T> {\n" +
				"	<@NonNull K> void foo() {}\n" +
				"	List<@NonNull ?> l;\n" +
				"}\n",
		},
		"");
		// javac b100
		// On the type declaration:
		//  RuntimeInvisibleTypeAnnotations:
		//   0: #9(): CLASS_TYPE_PARAMETER, param_index=0
		// On the method:
	    //  RuntimeInvisibleTypeAnnotations:
	    //   0: #9(): METHOD_TYPE_PARAMETER, param_index=0
		// On the field:
		//  RuntimeInvisibleTypeAnnotations:
		//   0: #9(): FIELD, location=[TYPE_ARGUMENT(0)]
		String expectedOutput =
				"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" +
				"// Signature: <T:Ljava/lang/Object;>Ljava/lang/Object;\n" +
				"class X {\n" +
				"  Constant pool:\n" +
				"    constant #1 class: #2 X\n" +
				"    constant #2 utf8: \"X\"\n" +
				"    constant #3 class: #4 java/lang/Object\n" +
				"    constant #4 utf8: \"java/lang/Object\"\n" +
				"    constant #5 utf8: \"l\"\n" +
				"    constant #6 utf8: \"Ljava/util/List;\"\n" +
				"    constant #7 utf8: \"Signature\"\n" +
				"    constant #8 utf8: \"Ljava/util/List<*>;\"\n" +
				"    constant #9 utf8: \"RuntimeInvisibleTypeAnnotations\"\n" +
				"    constant #10 utf8: \"LNonNull;\"\n" +
				"    constant #11 utf8: \"<init>\"\n" +
				"    constant #12 utf8: \"()V\"\n" +
				"    constant #13 utf8: \"Code\"\n" +
				"    constant #14 method_ref: #3.#15 java/lang/Object.<init> ()V\n" +
				"    constant #15 name_and_type: #11.#12 <init> ()V\n" +
				"    constant #16 utf8: \"LineNumberTable\"\n" +
				"    constant #17 utf8: \"LocalVariableTable\"\n" +
				"    constant #18 utf8: \"this\"\n" +
				"    constant #19 utf8: \"LX;\"\n" +
				"    constant #20 utf8: \"LocalVariableTypeTable\"\n" +
				"    constant #21 utf8: \"LX<TT;>;\"\n" +
				"    constant #22 utf8: \"foo\"\n" +
				"    constant #23 utf8: \"<K:Ljava/lang/Object;>()V\"\n" +
				"    constant #24 utf8: \"SourceFile\"\n" +
				"    constant #25 utf8: \"X.java\"\n" +
				"    constant #26 utf8: \"<T:Ljava/lang/Object;>Ljava/lang/Object;\"\n" +
				"  \n" +
				"  // Field descriptor #6 Ljava/util/List;\n" +
				"  // Signature: Ljava/util/List<*>;\n" +
				"  java.util.List l;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @NonNull(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #12 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [14]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X<T>\n" +
				"  \n" +
				"  // Method descriptor #12 ()V\n" +
				"  // Signature: <K:Ljava/lang/Object;>()V\n" +
				"  // Stack: 0, Locals: 1\n" +
				"  void foo();\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X<T>\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @NonNull(\n" +
				"        target type = 0x1 METHOD_TYPE_PARAMETER\n" +
				"        type parameter index = 0\n" +
				"      )\n" +
				"\n" +
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #10 @NonNull(\n" +
				"      target type = 0x0 CLASS_TYPE_PARAMETER\n" +
				"      type parameter index = 0\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test029() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target({TYPE_USE}) @interface TAnno { }\n" +
				"@Target({METHOD}) @interface MAnno { }\n" +
				"@Target({METHOD, TYPE_USE}) @interface MTAnno { }\n" +
				"@Target({FIELD}) @interface FAnno { }\n" +
				"@Target({FIELD, TYPE_USE}) @interface FTAnno { }\n" +
				"class X {\n" +
				"@FAnno Object field4; // legal, one field annotation\n" +
				"@TAnno Object field5; // legal, one type annotation\n" +
				"@FTAnno Object field6; // legal, one field annotation and one type annotation\n" +
				"@FAnno java.lang.Object field7; // legal, one field annotation\n" +
				"@TAnno java.lang.Object field8; // illegal\n" +
				"@FTAnno java.lang.Object field9; // legal, one field annotation\n" +
				"java.lang. @FAnno Object field10; // illegal\n" +
				"java.lang. @TAnno Object field11; // legal, one type annotation\n" +
				"java.lang. @FTAnno Object field12; // legal, one type annotation\n" +
				"@MAnno void myMethod1() { } // legal, one method annotation\n" +
				"@TAnno void myMethod2() { } // illegal\n" +
				"@MTAnno void myMethod3() { } // legal, one method annotation\n" +
				"@MAnno Object myMethod4() {  } // legal, one method annotation\n" +
				"@TAnno Object myMethod5() { } // legal, one type annotation\n" +
				"@MTAnno Object myMethod6() {  } // legal, one method annotation and one type annotation\n" +
				"@MAnno java.lang.Object myMethod7() {  } // legal, one method annotation\n" +
				"@TAnno java.lang.Object myMethod8() {  } // illegal\n" +
				"@MTAnno java.lang.Object myMethod9() {  } // legal, one method annotation\n" +
				"java.lang. @MAnno Object myMethod10() { } // illegal\n" +
				"java.lang. @TAnno Object myMethod11() {  } // legal, one type annotation\n" +
				"java.lang. @MTAnno Object myMethod12() {  } // legal, one type annotation\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	@TAnno java.lang.Object field8; // illegal\n" +
		"	^^^^^^\n" +
		"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 15)\n" +
		"	java.lang. @FAnno Object field10; // illegal\n" +
		"	           ^^^^^^\n" +
		"The annotation @FAnno is disallowed for this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 19)\n" +
		"	@TAnno void myMethod2() { } // illegal\n" +
		"	^^^^^^\n" +
		"Type annotation is illegal for a method that returns void\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 25)\n" +
		"	@TAnno java.lang.Object myMethod8() {  } // illegal\n" +
		"	^^^^^^\n" +
		"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 27)\n" +
		"	java.lang. @MAnno Object myMethod10() { } // illegal\n" +
		"	           ^^^^^^\n" +
		"The annotation @MAnno is disallowed for this location\n" +
		"----------\n");
	}
	public void test030() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target({TYPE_USE}) @interface TypeAnnotation { }\n" +
				"@Target({TYPE}) @interface Annotation { }\n" +
				"@Annotation @TypeAnnotation class X {\n" +
				"}\n",
		},
		"");
		// javac b100 produces:
		//		  RuntimeInvisibleAnnotations:
		//			    0: #11() LAnnotation;
		//			    1: #12() LTypeAnnotation;
		String expectedOutput =
				"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" +
				"class X {\n" +
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
				"    constant #14 utf8: \"SourceFile\"\n" +
				"    constant #15 utf8: \"X.java\"\n" +
				"    constant #16 utf8: \"RuntimeInvisibleAnnotations\"\n" +
				"    constant #17 utf8: \"LAnnotation;\"\n" +
				"    constant #18 utf8: \"LTypeAnnotation;\"\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [8]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"\n" +
				"  RuntimeInvisibleAnnotations: \n" +
				"    #17 @Annotation(\n" +
				"    )\n" +
				"    #18 @TypeAnnotation(\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test030a() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@Target({TYPE_USE}) @interface TypeAnnotation { }\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@Target({TYPE}) @interface Annotation { }\n" +
				"@Annotation @TypeAnnotation class X {\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" +
				"class X {\n" +
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
				"    constant #14 utf8: \"SourceFile\"\n" +
				"    constant #15 utf8: \"X.java\"\n" +
				"    constant #16 utf8: \"RuntimeVisibleAnnotations\"\n" +
				"    constant #17 utf8: \"LAnnotation;\"\n" +
				"    constant #18 utf8: \"LTypeAnnotation;\"\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [8]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"\n" +
				"  RuntimeVisibleAnnotations: \n" +
				"    #17 @Annotation(\n" +
				"    )\n" +
				"    #18 @TypeAnnotation(\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test030b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				// Only TYPE_USE annotations get this special treatment
				"@Target({TYPE_PARAMETER}) @interface TypeAnnotation { }\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@Target({TYPE}) @interface Annotation { }\n" +
				"@Annotation @TypeAnnotation class X {\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	@Annotation @TypeAnnotation class X {\n" +
		"	            ^^^^^^^^^^^^^^^\n" +
		"The annotation @TypeAnnotation is disallowed for this location\n" +
		"----------\n");
	}
	public void test030c() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@Target({TYPE_USE,TYPE_PARAMETER}) @interface TypeAnnotation { }\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@Target({TYPE}) @interface Annotation { }\n" +
				"@Annotation @TypeAnnotation class X {\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" +
				"class X {\n" +
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
				"    constant #14 utf8: \"SourceFile\"\n" +
				"    constant #15 utf8: \"X.java\"\n" +
				"    constant #16 utf8: \"RuntimeVisibleAnnotations\"\n" +
				"    constant #17 utf8: \"LAnnotation;\"\n" +
				"    constant #18 utf8: \"LTypeAnnotation;\"\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [8]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"\n" +
				"  RuntimeVisibleAnnotations: \n" +
				"    #17 @Annotation(\n" +
				"    )\n" +
				"    #18 @TypeAnnotation(\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// Test that annotations in initializer code are not attached to the field.
	public void test031() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target({TYPE_USE}) @interface NonNull { }\n" +
				"class X {\n" +
				"	X x = new @NonNull X();\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  X();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokespecial java.lang.Object() [10]\n" +
				"     4  aload_0 [this]\n" +
				"     5  new X [1]\n" +
				"     8  dup\n" +
				"     9  invokespecial X() [12]\n" +
				"    12  putfield X.x : X [13]\n" +
				"    15  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 4]\n" +
				"        [pc: 4, line: 5]\n" +
				"        [pc: 15, line: 4]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 16] local: this index: 0 type: X\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #19 @NonNull(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 5\n" +
				"      )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// Test co-existence of parameter annotations and type annotations.
	public void test032() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target({TYPE_USE}) @interface NonNull { }\n" +
				"@Target({PARAMETER}) @interface ParameterAnnot { }\n" +
				"class X {\n" +
				"	void foo(@NonNull X this, @ParameterAnnot @NonNull X x) {\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  void foo(X x);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 1] local: x index: 1 type: X\n" +
				"    RuntimeInvisibleParameterAnnotations: \n" +
				"      Number of annotations for parameter 0: 1\n" +
				"        #17 @ParameterAnnot(\n" +
				"        )\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #20 @NonNull(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"      #20 @NonNull(\n" +
				"        target type = 0x15 METHOD_RECEIVER\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// Test type annotations in initializer code.
	public void test033() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target({TYPE_USE}) @interface NonNull { }\n" +
				"class X {\n" +
				"	static {\n" +
				"		Object o = (@NonNull Object) new @NonNull Object();\n" +
				"	}\n" +
				"	{\n" +
				"		new @NonNull Object();\n" +
				"	}\n" +
				"	X() {\n" +
				"	}\n" +
				"	X (int x) {\n" +
				"	}\n" +
				"}\n",
		},
		"");
		// javac b100
		// For the annotations in the static {...} the clinit has:
		//		RuntimeInvisibleTypeAnnotations:
		//	        0: #11(): CAST, offset=0, type_index=0
		//	        1: #11(): NEW, offset=0
		// javac is skipping production of the cast so offset is 0. JDT is currently always producing the
		// checkcast for an annotated cast so the offset is 7.

		// For the annotations in the initializer {...} the constructors both have:
		//	      RuntimeInvisibleTypeAnnotations:
		//	          0: #11(): NEW, offset=4

		String expectedOutput =
				"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" +
				"class X {\n" +
				"  Constant pool:\n" +
				"    constant #1 class: #2 X\n" +
				"    constant #2 utf8: \"X\"\n" +
				"    constant #3 class: #4 java/lang/Object\n" +
				"    constant #4 utf8: \"java/lang/Object\"\n" +
				"    constant #5 utf8: \"<clinit>\"\n" +
				"    constant #6 utf8: \"()V\"\n" +
				"    constant #7 utf8: \"Code\"\n" +
				"    constant #8 method_ref: #3.#9 java/lang/Object.<init> ()V\n" +
				"    constant #9 name_and_type: #10.#6 <init> ()V\n" +
				"    constant #10 utf8: \"<init>\"\n" +
				"    constant #11 utf8: \"LineNumberTable\"\n" +
				"    constant #12 utf8: \"LocalVariableTable\"\n" +
				"    constant #13 utf8: \"RuntimeInvisibleTypeAnnotations\"\n" +
				"    constant #14 utf8: \"LNonNull;\"\n" +
				"    constant #15 utf8: \"this\"\n" +
				"    constant #16 utf8: \"LX;\"\n" +
				"    constant #17 utf8: \"(I)V\"\n" +
				"    constant #18 utf8: \"x\"\n" +
				"    constant #19 utf8: \"I\"\n" +
				"    constant #20 utf8: \"SourceFile\"\n" +
				"    constant #21 utf8: \"X.java\"\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 2, Locals: 1\n" +
				"  static {};\n" +
				"     0  new java.lang.Object [3]\n" +
				"     3  dup\n" +
				"     4  invokespecial java.lang.Object() [8]\n" +
				"     7  checkcast java.lang.Object [3]\n" +
				"    10  astore_0\n" +
				"    11  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"        [pc: 11, line: 7]\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #14 @NonNull(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 0\n" +
				"      )\n" +
				"      #14 @NonNull(\n" +
				"        target type = 0x47 CAST\n" +
				"        offset = 7\n" +
				"        type argument index = 0\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  X();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokespecial java.lang.Object() [8]\n" +
				"     4  new java.lang.Object [3]\n" +
				"     7  invokespecial java.lang.Object() [8]\n" +
				"    10  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 11]\n" +
				"        [pc: 4, line: 9]\n" +
				"        [pc: 10, line: 12]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 11] local: this index: 0 type: X\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #14 @NonNull(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 4\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #17 (I)V\n" +
				"  // Stack: 1, Locals: 2\n" +
				"  X(int x);\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokespecial java.lang.Object() [8]\n" +
				"     4  new java.lang.Object [3]\n" +
				"     7  invokespecial java.lang.Object() [8]\n" +
				"    10  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 13]\n" +
				"        [pc: 4, line: 9]\n" +
				"        [pc: 10, line: 14]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 11] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 11] local: x index: 1 type: int\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #14 @NonNull(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 4\n" +
				"      )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test034() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target({TYPE_USE}) @interface NonNull { }\n" +
				"class X <T extends @NonNull Comparable> {\n" +
				"}\n",
		},
		"");
		// javac b100
		//		  RuntimeInvisibleTypeAnnotations:
		//			    0: #13(): CLASS_TYPE_PARAMETER_BOUND, param_index=0, bound_index=1
		// bound_index is 1 because the bound is an interface, not a class
		String expectedOutput =
				"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" +
				"// Signature: <T::Ljava/lang/Comparable;>Ljava/lang/Object;\n" +
				"class X {\n" +
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
				"    constant #14 utf8: \"LocalVariableTypeTable\"\n" +
				"    constant #15 utf8: \"LX<TT;>;\"\n" +
				"    constant #16 utf8: \"SourceFile\"\n" +
				"    constant #17 utf8: \"X.java\"\n" +
				"    constant #18 utf8: \"Signature\"\n" +
				"    constant #19 utf8: \"<T::Ljava/lang/Comparable;>Ljava/lang/Object;\"\n" +
				"    constant #20 utf8: \"RuntimeInvisibleTypeAnnotations\"\n" +
				"    constant #21 utf8: \"LNonNull;\"\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [8]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 4]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X<T>\n" +
				"\n" +
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #21 @NonNull(\n" +
				"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
				"      type parameter index = 0 type parameter bound index = 1\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	// Bug 415543 - Incorrect bound index in RuntimeInvisibleTypeAnnotations attribute
	public void test034b() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.io.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target({TYPE_USE}) @interface NonNull { }\n" +
				"\n" +
				"class X <T extends Comparable & @NonNull Serializable> {\n" +
				"  <T extends @NonNull Comparable> void one(T t) {}\n" +
				"  <T extends Comparable & @NonNull Serializable> void two(T t) {}\n" +
				"  <T extends @NonNull Comparable & @NonNull Serializable> void three(T t) {}\n" +
				"  <T extends Object & @NonNull Serializable> void four(T t) {}\n" +
				"  <T extends Object & @NonNull Serializable & @NonNull Runnable> void five(T t) {}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"// Compiled from X.java (version 1.8 : 52.0, super bit)\n" +
				"// Signature: <T::Ljava/lang/Comparable;:Ljava/io/Serializable;>Ljava/lang/Object;\n" +
				"class X {\n" +
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
				"    constant #14 utf8: \"LocalVariableTypeTable\"\n" +
				"    constant #15 utf8: \"LX<TT;>;\"\n" +
				"    constant #16 utf8: \"one\"\n" +
				"    constant #17 utf8: \"(Ljava/lang/Comparable;)V\"\n" +
				"    constant #18 utf8: \"Signature\"\n" +
				"    constant #19 utf8: \"<T::Ljava/lang/Comparable;>(TT;)V\"\n" +
				"    constant #20 utf8: \"t\"\n" +
				"    constant #21 utf8: \"Ljava/lang/Comparable;\"\n" +
				"    constant #22 utf8: \"TT;\"\n" +
				"    constant #23 utf8: \"RuntimeInvisibleTypeAnnotations\"\n" +
				"    constant #24 utf8: \"LNonNull;\"\n" +
				"    constant #25 utf8: \"two\"\n" +
				"    constant #26 utf8: \"<T::Ljava/lang/Comparable;:Ljava/io/Serializable;>(TT;)V\"\n" +
				"    constant #27 utf8: \"three\"\n" +
				"    constant #28 utf8: \"four\"\n" +
				"    constant #29 utf8: \"(Ljava/lang/Object;)V\"\n" +
				"    constant #30 utf8: \"<T:Ljava/lang/Object;:Ljava/io/Serializable;>(TT;)V\"\n" +
				"    constant #31 utf8: \"Ljava/lang/Object;\"\n" +
				"    constant #32 utf8: \"five\"\n" +
				"    constant #33 utf8: \"<T:Ljava/lang/Object;:Ljava/io/Serializable;:Ljava/lang/Runnable;>(TT;)V\"\n" +
				"    constant #34 utf8: \"SourceFile\"\n" +
				"    constant #35 utf8: \"X.java\"\n" +
				"    constant #36 utf8: \"<T::Ljava/lang/Comparable;:Ljava/io/Serializable;>Ljava/lang/Object;\"\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [8]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X<T>\n" +
				"  \n" +
				"  // Method descriptor #17 (Ljava/lang/Comparable;)V\n" +
				"  // Signature: <T::Ljava/lang/Comparable;>(TT;)V\n" +
				"  // Stack: 0, Locals: 2\n" +
				"  void one(java.lang.Comparable t);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 1] local: t index: 1 type: java.lang.Comparable\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X<T>\n" +
				"        [pc: 0, pc: 1] local: t index: 1 type: T\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #24 @NonNull(\n" +
				"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
				"        type parameter index = 0 type parameter bound index = 1\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #17 (Ljava/lang/Comparable;)V\n" +
				"  // Signature: <T::Ljava/lang/Comparable;:Ljava/io/Serializable;>(TT;)V\n" +
				"  // Stack: 0, Locals: 2\n" +
				"  void two(java.lang.Comparable t);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 8]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 1] local: t index: 1 type: java.lang.Comparable\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X<T>\n" +
				"        [pc: 0, pc: 1] local: t index: 1 type: T\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #24 @NonNull(\n" +
				"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
				"        type parameter index = 0 type parameter bound index = 2\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #17 (Ljava/lang/Comparable;)V\n" +
				"  // Signature: <T::Ljava/lang/Comparable;:Ljava/io/Serializable;>(TT;)V\n" +
				"  // Stack: 0, Locals: 2\n" +
				"  void three(java.lang.Comparable t);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 9]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 1] local: t index: 1 type: java.lang.Comparable\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X<T>\n" +
				"        [pc: 0, pc: 1] local: t index: 1 type: T\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #24 @NonNull(\n" +
				"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
				"        type parameter index = 0 type parameter bound index = 1\n" +
				"      )\n" +
				"      #24 @NonNull(\n" +
				"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
				"        type parameter index = 0 type parameter bound index = 2\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #29 (Ljava/lang/Object;)V\n" +
				"  // Signature: <T:Ljava/lang/Object;:Ljava/io/Serializable;>(TT;)V\n" +
				"  // Stack: 0, Locals: 2\n" +
				"  void four(java.lang.Object t);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 10]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 1] local: t index: 1 type: java.lang.Object\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X<T>\n" +
				"        [pc: 0, pc: 1] local: t index: 1 type: T\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #24 @NonNull(\n" +
				"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
				"        type parameter index = 0 type parameter bound index = 1\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #29 (Ljava/lang/Object;)V\n" +
				"  // Signature: <T:Ljava/lang/Object;:Ljava/io/Serializable;:Ljava/lang/Runnable;>(TT;)V\n" +
				"  // Stack: 0, Locals: 2\n" +
				"  void five(java.lang.Object t);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 11]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 1] local: t index: 1 type: java.lang.Object\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X<T>\n" +
				"        [pc: 0, pc: 1] local: t index: 1 type: T\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #24 @NonNull(\n" +
				"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
				"        type parameter index = 0 type parameter bound index = 1\n" +
				"      )\n" +
				"      #24 @NonNull(\n" +
				"        target type = 0x12 METHOD_TYPE_PARAMETER_BOUND\n" +
				"        type parameter index = 0 type parameter bound index = 2\n" +
				"      )\n" +
				"\n" +
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #24 @NonNull(\n" +
				"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
				"      type parameter index = 0 type parameter bound index = 2\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test035() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target({TYPE_USE}) @interface NonNull { }\n" +
				"\n" +
				"class X {\n" +
				"	void foo() {\n" +
				"		@NonNull X [] x = new X[10];\n" +
				"		System.out.println(x);\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  void foo();\n" +
				"     0  bipush 10\n" +
				"     2  anewarray X [1]\n" +
				"     5  astore_1 [x]\n" +
				"     6  getstatic java.lang.System.out : java.io.PrintStream [15]\n" +
				"     9  aload_1 [x]\n" +
				"    10  invokevirtual java.io.PrintStream.println(java.lang.Object) : void [21]\n" +
				"    13  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"        [pc: 6, line: 8]\n" +
				"        [pc: 13, line: 9]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
				"        [pc: 6, pc: 14] local: x index: 1 type: X[]\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #30 @NonNull(\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 6, pc: 14] index: 1\n" +
				"        location = [ARRAY]\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// test that parameter index does not include explicit this parameter.
	public void test036() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target({TYPE_USE}) @interface NonNull { }\n" +
				"class X  {\n" +
				"	void foo(@NonNull X this, @NonNull X x) {\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  void foo(X x);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 1] local: x index: 1 type: X\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #18 @NonNull(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"      #18 @NonNull(\n" +
				"        target type = 0x15 METHOD_RECEIVER\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test037() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Readonly {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"public class X {\n" +
				"	X [] x = new @Readonly X @Readonly [10];\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  public X();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokespecial java.lang.Object() [10]\n" +
				"     4  aload_0 [this]\n" +
				"     5  bipush 10\n" +
				"     7  anewarray X [1]\n" +
				"    10  putfield X.x : X[] [12]\n" +
				"    13  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"        [pc: 4, line: 8]\n" +
				"        [pc: 13, line: 7]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 14] local: this index: 0 type: X\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #19 @Readonly(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 7\n" +
				"        location = [ARRAY]\n" +
				"      )\n" +
				"      #19 @Readonly(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 7\n" +
				"      )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// test anonymous class, the class itself should have class_extends target ?
	public void test038() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Readonly {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"public class X {\n" +
				"	X x = new @Readonly X() {\n" +
				"	};\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  public X();\n" +
				"     0  aload_0 [this]\n" +
				"     1  invokespecial java.lang.Object() [10]\n" +
				"     4  aload_0 [this]\n" +
				"     5  new X$1 [12]\n" +
				"     8  dup\n" +
				"     9  aload_0 [this]\n" +
				"    10  invokespecial X$1(X) [14]\n" +
				"    13  putfield X.x : X [17]\n" +
				"    16  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"        [pc: 4, line: 8]\n" +
				"        [pc: 16, line: 7]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 17] local: this index: 0 type: X\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #23 @Readonly(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 5\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test039() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.List;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Readonly {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"public class X  { \n" +
				"	void foo(List<@Readonly ?> l) {\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  void foo(java.util.List l);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 10]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 1] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 1] local: l index: 1 type: java.util.List\n" +
				"      Local variable type table:\n" +
				"        [pc: 0, pc: 1] local: l index: 1 type: java.util.List<?>\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #23 @Readonly(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test040() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Readonly {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"class X {\n" +
				"	class Y {}\n" +
				"	void foo() {\n" +
				"		@Readonly X x = new X();\n" +
				"		x.new @Readonly Y();\n" +
				"	}\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #27 @Readonly(\n" +
				"        target type = 0x44 NEW\n" +
				"        offset = 8\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n" +
				"      #27 @Readonly(\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 8, pc: 21] index: 1\n" +
				"      )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test041() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface B {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface C {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"class X<T extends @A Object & @B Comparable, U extends @C Cloneable> {\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #21 @A(\n" +
				"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
				"      type parameter index = 0 type parameter bound index = 0\n" +
				"    )\n" +
				"    #22 @B(\n" +
				"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
				"      type parameter index = 0 type parameter bound index = 1\n" +
				"    )\n" +
				"    #23 @C(\n" +
				"      target type = 0x11 CLASS_TYPE_PARAMETER_BOUND\n" +
				"      type parameter index = 1 type parameter bound index = 1\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// type path tests.
	public void test042() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.Map;\n" +
				"import java.util.List;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface B {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface C {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface D {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface E {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"class X {\n" +
				"	@A Map <@B ? extends @C String, @D List<@E Object>> f;\n" +
				"}\n",
		},
		"");
		String expectedOutput =
				"  java.util.Map f;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @A(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"      #11 @B(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"      #12 @C(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0), WILDCARD]\n" +
				"      )\n" +
				"      #13 @D(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(1)]\n" +
				"      )\n" +
				"      #14 @E(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"  \n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	// Bug 414384 - [1.8] type annotation on abbreviated inner class is not marked as inner type
	public void test043() throws Exception {
		this.runConformTest(
			new String[] {
				"pkg/Clazz.java",
				"package pkg;\n" +
				"import java.lang.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Target({TYPE_USE}) @interface P { }\n" +
				"@Target({TYPE_USE}) @interface O { }\n" +
				"@Target({TYPE_USE}) @interface I { }\n" +
				"\n" +
				"public abstract class Clazz {\n" +
				"  public class Inner {}\n" +
				"  public abstract void n1(@I Inner i1);\n" +
				"  public abstract void n2(@O Clazz.@I Inner i2);\n" +
				"  public abstract void n3(pkg.@O Clazz.@I Inner i3);\n" +
				"}\n",
		},
		"");
		// javac b100 produces for the methods:
		//		  public abstract void n1(pkg.Clazz$Inner);
		//		    RuntimeInvisibleTypeAnnotations:
		//		      0: #14(): METHOD_FORMAL_PARAMETER, param_index=0, location=[INNER_TYPE]
		//
		//		  public abstract void n2(pkg.Clazz$Inner);
		//		    RuntimeInvisibleTypeAnnotations:
		//		      0: #14(): METHOD_FORMAL_PARAMETER, param_index=0, location=[INNER_TYPE]
		//		      1: #16(): METHOD_FORMAL_PARAMETER, param_index=0
		//
		//		  public abstract void n3(pkg.Clazz$Inner);
		//		    RuntimeInvisibleTypeAnnotations:
		//		      0: #14(): METHOD_FORMAL_PARAMETER, param_index=0, location=[INNER_TYPE]
		//		      1: #16(): METHOD_FORMAL_PARAMETER, param_index=0
		String expectedOutput =
				"  // Method descriptor #15 (Lpkg/Clazz$Inner;)V\n" +
				"  public abstract void n1(pkg.Clazz.Inner arg0);\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #17 @pkg.I(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n" +
				"  \n" +

				"  // Method descriptor #15 (Lpkg/Clazz$Inner;)V\n" +
				"  public abstract void n2(pkg.Clazz.Inner arg0);\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #19 @pkg.O(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"      #17 @pkg.I(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n" +
				"  \n" +

				"  // Method descriptor #15 (Lpkg/Clazz$Inner;)V\n" +
				"  public abstract void n3(pkg.Clazz.Inner arg0);\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #19 @pkg.O(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"      #17 @pkg.I(\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n" +
				"\n" +
				"  Inner classes:\n" +
				"    [inner class info: #24 pkg/Clazz$Inner, outer class info: #1 pkg/Clazz\n" +
				"     inner name: #26 Inner, accessflags: 1 public]\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "pkg" + File.separator + "Clazz.class", "pkg.Clazz", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// More type path tests
	public void test044() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface I {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface F {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface G {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface H {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"class X {\n" +
				"	@I String @F [] @G [] @H [] f;\n" +
				"}\n",
		},
		"");

		String expectedOutput =
				"  // Field descriptor #6 [[[Ljava/lang/String;\n" +
				"  java.lang.String[][][] f;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @I(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #9 @F(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"      #10 @G(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY]\n" +
				"      )\n" +
				"      #11 @H(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [ARRAY, ARRAY]\n" +
				"      )\n" +
				"  \n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// More type path tests
	public void test045() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface M {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface L {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface K {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface J {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"class O1 {\n" +
				"	class O2 {\n" +
				"		class O3 {\n" +
				"			class Nested {}\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"class X {\n" +
				"	@M O1.@L O2.@K O3.@J Nested f = null;\n" +
				"}\n",
		},
		"");

		String expectedOutput =
				"  // Field descriptor #6 LO1$O2$O3$Nested;\n" +
				"  O1$O2$O3$Nested f;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #8 @M(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"      #9 @L(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n" +
				"      #10 @K(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [INNER_TYPE, INNER_TYPE]\n" +
				"      )\n" +
				"      #11 @J(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE]\n" +
				"      )\n" +
				"  \n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// More type path tests
	public void test046() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.Map;\n" +
				"import java.util.List;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface B {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface C {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface D {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface E {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface F {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface G {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface H {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"class Document {}\n" +
				"class X {\n" +
				"	@A Map<@B Comparable<@F Object @C [] @D [] @E[]>, @G List<@H Document>> f;\n" +
				"}\n",
		},
		"");

		String expectedOutput =
				"  java.util.Map f;\n" +
				"    RuntimeInvisibleTypeAnnotations: \n" +
				"      #10 @A(\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"      #11 @B(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"      #12 @F(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #13 @C(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"      #14 @D(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY]\n" +
				"      )\n" +
				"      #15 @E(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY]\n" +
				"      )\n" +
				"      #16 @G(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(1)]\n" +
				"      )\n" +
				"      #17 @H(\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]\n" +
				"      )\n" +
				"  \n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// More type path tests
	public void test047() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface A {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface B {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface C {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface D {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface E {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface F {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface G {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface H {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"class O1 {\n" +
				"	class O2<S, T> {\n" +
				"		class O3 {\n" +
				"			class Nested<K, V> {\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"	class S {}\n" +
				"	class T {}\n" +
				"	class U {}\n" +
				"	class V {}\n" +
				"class X {\n" +
				"	@H O1.@E O2<@F S, @G T>.@D O3.@A Nested<@B U, @C V> f;\n" +
				"}\n",
		},
		"");

		String expectedOutput =
			"  // Field descriptor #6 LO1$O2$O3$Nested;\n" +
			"  // Signature: LO1$O2<LS;LT;>.O3.Nested<LU;LV;>;\n" +
			"  O1$O2$O3$Nested f;\n" +
			"    RuntimeInvisibleTypeAnnotations: \n" +
			"      #10 @H(\n" +
			"        target type = 0x13 FIELD\n" +
			"      )\n" +
			"      #11 @E(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [INNER_TYPE]\n" +
			"      )\n" +
			"      #12 @D(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [INNER_TYPE, INNER_TYPE]\n" +
			"      )\n" +
			"      #13 @A(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE]\n" +
			"      )\n" +
			"      #14 @F(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [INNER_TYPE, TYPE_ARGUMENT(0)]\n" +
			"      )\n" +
			"      #15 @G(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [INNER_TYPE, TYPE_ARGUMENT(1)]\n" +
			"      )\n" +
			"      #16 @B(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE, TYPE_ARGUMENT(0)]\n" +
			"      )\n" +
			"      #17 @C(\n" +
			"        target type = 0x13 FIELD\n" +
			"        location = [INNER_TYPE, INNER_TYPE, INNER_TYPE, TYPE_ARGUMENT(1)]\n" +
			"      )\n" +
			"  \n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test048() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.List;\n" +
				"import java.util.Map;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface Readonly {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface NonNull {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface NonEmpty {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface D {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface E {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface F {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface G {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface H {\n" +
				"	String value() default \"default\";\n" +
				"}\n" +
				"\n" +
				"abstract class X implements @Readonly Map<@NonNull String, @NonEmpty List<@NonNull @Readonly String>> {}\n",
		},
		"");

		String expectedOutput =
				"  RuntimeInvisibleTypeAnnotations: \n" +
				"    #21 @Readonly(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"    )\n" +
				"    #22 @NonNull(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(0)]\n" +
				"    )\n" +
				"    #23 @NonEmpty(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(1)]\n" +
				"    )\n" +
				"    #22 @NonNull(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]\n" +
				"    )\n" +
				"    #21 @Readonly(\n" +
				"      target type = 0x10 CLASS_EXTENDS\n" +
				"      type index = 0\n" +
				"      location = [TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]\n" +
				"    )\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	public void test049() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface B { int value() default -1; }\n" +
				"public class X {\n" +
				"    class Y {\n" +
				"    }\n" +
				"    @B(1) X. @B(2) Y xy;\n" +
				"    void foo(@B(3) X. @B(4) Y xy) {\n" +
				"        @B(5) X. @B(6) Y local = null; \n" +
				"    }\n" +
				"}\n",
		},
		"");

		String expectedOutput =
				"  // Field descriptor #6 LX$Y;\n" +
				"  X$Y xy;\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @B(\n" +
				"        #9 value=(int) 1 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"      )\n" +
				"      #8 @B(\n" +
				"        #9 value=(int) 2 (constant type)\n" +
				"        target type = 0x13 FIELD\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n" +
				"  \n" +
				"  // Method descriptor #13 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public X();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [15]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 5] local: this index: 0 type: X\n" +
				"  \n" +
				"  // Method descriptor #22 (LX$Y;)V\n" +
				"  // Stack: 1, Locals: 3\n" +
				"  void foo(X.Y xy);\n" +
				"    0  aconst_null\n" +
				"    1  astore_2 [local]\n" +
				"    2  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 10]\n" +
				"        [pc: 2, line: 11]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 3] local: this index: 0 type: X\n" +
				"        [pc: 0, pc: 3] local: xy index: 1 type: X.Y\n" +
				"        [pc: 2, pc: 3] local: local index: 2 type: X.Y\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @B(\n" +
				"        #9 value=(int) 5 (constant type)\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 2, pc: 3] index: 2\n" +
				"      )\n" +
				"      #8 @B(\n" +
				"        #9 value=(int) 6 (constant type)\n" +
				"        target type = 0x40 LOCAL_VARIABLE\n" +
				"        local variable entries:\n" +
				"          [pc: 2, pc: 3] index: 2\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n" +
				"    RuntimeVisibleTypeAnnotations: \n" +
				"      #8 @B(\n" +
				"        #9 value=(int) 3 (constant type)\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"      )\n" +
				"      #8 @B(\n" +
				"        #9 value=(int) 4 (constant type)\n" +
				"        target type = 0x16 METHOD_FORMAL_PARAMETER\n" +
				"        method parameter index = 0\n" +
				"        location = [INNER_TYPE]\n" +
				"      )\n" +
				"\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "Z", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
}
