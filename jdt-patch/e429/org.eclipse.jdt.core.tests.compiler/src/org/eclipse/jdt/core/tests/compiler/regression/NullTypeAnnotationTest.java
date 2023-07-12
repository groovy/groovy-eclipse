/*******************************************************************************
 * Copyright (c) 2012, 2021 GK Software AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     IBM Corporation
 *     Till Brychcy - Contribution for
 *								Bug 467032 - TYPE_USE Null Annotations: IllegalStateException with annotated arrays of Enum when accessed via BinaryTypeBinding
 *								Bug 467482 - TYPE_USE null annotations: Incorrect "Redundant null check"-warning
 *								Bug 473713 - [1.8][null] Type mismatch: cannot convert from @NonNull A1 to @NonNull A1
 *								Bug 467430 - TYPE_USE Null Annotations: Confusing error message with known null value
 *     Pierre-Yves B. <pyvesdev@gmail.com> - Contribution for
 *                              Bug 559618 - No compiler warning for import from same package
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.Excuse;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NullTypeAnnotationTest extends AbstractNullAnnotationTest {

	public NullTypeAnnotationTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testBug456584" };
//			TESTS_NUMBERS = new int[] { 561 };
//			TESTS_RANGE = new int[] { 1, 2049 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public static Class testClass() {
		return NullTypeAnnotationTest.class;
	}

	// a list with nullable elements is used
	public void test_nonnull_list_elements_01() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "import java.util.List;\n" +
				  "public class X {\n" +
				  "    void foo(List<@Nullable Object> l) {\n" +
				  "        System.out.print(l.get(0).toString()); // problem: retrieved element can be null\n" +
				  "        l.add(null);\n" +
				  "    }\n" +
				  "    void bar(java.util.List<@Nullable Object> l) {\n" +
				  "        System.out.print(l.get(1).toString()); // problem: retrieved element can be null\n" +
				  "        l.add(null);\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	System.out.print(l.get(0).toString()); // problem: retrieved element can be null\n" +
			"	                 ^^^^^^^^\n" +
			"Potential null pointer access: The method get(int) may return null\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	System.out.print(l.get(1).toString()); // problem: retrieved element can be null\n" +
			"	                 ^^^^^^^^\n" +
			"Potential null pointer access: The method get(int) may return null\n" +
			"----------\n");
	}

	// a list with nullable elements is used, custom annotations
	public void test_nonnull_list_elements_01a() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
		customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
		runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
			new String[] {
				CUSTOM_NULLABLE_NAME,
				CUSTOM_NULLABLE_CONTENT_JSR308,
				CUSTOM_NONNULL_NAME,
				CUSTOM_NONNULL_CONTENT_JSR308,
				"X.java",
				  "import org.foo.*;\n" +
				  "import java.util.List;\n" +
				  "public class X {\n" +
				  "    void foo(List<@Nullable Object> l) {\n" +
				  "        System.out.print(l.get(0).toString()); // problem: retrieved element can be null\n" +
				  "        l.add(null);\n" +
				  "    }\n" +
				  "    void bar(java.util.List<@Nullable Object> l) {\n" +
				  "        System.out.print(l.get(1).toString()); // problem: retrieved element can be null\n" +
				  "        l.add(null);\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	System.out.print(l.get(0).toString()); // problem: retrieved element can be null\n" +
			"	                 ^^^^^^^^\n" +
			"Potential null pointer access: The method get(int) may return null\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	System.out.print(l.get(1).toString()); // problem: retrieved element can be null\n" +
			"	                 ^^^^^^^^\n" +
			"Potential null pointer access: The method get(int) may return null\n" +
			"----------\n",
			null,
			true, /* shouldFlush*/
			customOptions);
	}

	// a list with nullable elements is used, @Nullable is second annotation
	public void test_nonnull_list_elements_02() {
		runNegativeTestWithLibs(
			new String[] {
				"Dummy.java",
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.*;\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@Target({METHOD,PARAMETER,LOCAL_VARIABLE,TYPE_USE})\n" +
				"public @interface Dummy {\n" +
				"}\n",
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "import java.util.List;\n" +
				  "public class X {\n" +
				  "    void foo(List<@Dummy @Nullable Object> l) {\n" +
				  "        System.out.print(l.get(0).toString()); // problem: retrieved element can be null\n" +
				  "        l.add(null);\n" +
				  "    }\n" +
				  "    void bar(java.util.List<@Dummy @Nullable Object> l) {\n" +
				  "        System.out.print(l.get(1).toString()); // problem: retrieved element can be null\n" +
				  "        l.add(null);\n" +
				  "    }\n" +
				  "    void bar2(java.util.List<java.lang.@Dummy @Nullable Object> l2) {\n" +
				  "        System.out.print(l2.get(1).toString()); // problem: retrieved element can be null\n" +
				  "        l2.add(null);\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	System.out.print(l.get(0).toString()); // problem: retrieved element can be null\n" +
			"	                 ^^^^^^^^\n" +
			"Potential null pointer access: The method get(int) may return null\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	System.out.print(l.get(1).toString()); // problem: retrieved element can be null\n" +
			"	                 ^^^^^^^^\n" +
			"Potential null pointer access: The method get(int) may return null\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 13)\n" +
			"	System.out.print(l2.get(1).toString()); // problem: retrieved element can be null\n" +
			"	                 ^^^^^^^^^\n" +
			"Potential null pointer access: The method get(int) may return null\n" +
			"----------\n");
	}

	// a list with non-null elements is used, list itself is nullable
	public void test_nonnull_list_elements_03() {
		runNegativeTestWithLibs(
			new String[] {
				"Dummy.java",
				  "import static java.lang.annotation.ElementType.*;\n" +
				  "import java.lang.annotation.*;\n" +
				  "@Retention(RetentionPolicy.CLASS)\n" +
				  "@Target({METHOD,PARAMETER,LOCAL_VARIABLE,TYPE_USE})\n" +
				  "public @interface Dummy {\n" +
				  "}\n",
				"p/List.java",
				  "package p;\n" +
				  "public interface List<T> {\n" +
				  "	T get(int i);\n" + // avoid IProblem.NonNullTypeVariableFromLegacyMethod against unannotated j.u.List
				  " void add(T e);\n" +
				  " void add(int i, T e);\n" +
				  "}\n",
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "import p.List;\n" +
				  "public class X {\n" +
				  "    void foo(@Nullable List<@NonNull Object> l) {\n" +
				  "        System.out.print(l.get(0).toString()); // problem: l may be null\n" +
				  "        l.add(null); // problem: cannot insert 'null' into this list\n" +
				  "    }\n" +
				  "    void bar(@Nullable List<java.lang.@NonNull Object> l) {\n" +
				  "        System.out.print(l.get(0).toString()); // problem: l may be null\n" +
				  "        l.add(0, null); // problem: cannot insert 'null' into this list\n" +
				  "    }\n" +
				  "    void bar2(@Dummy p.@Nullable List<java.lang.@NonNull Object> l2) {\n" +
				  "        System.out.print(l2.get(0).toString()); // problem: l2 may be null\n" +
				  "        l2.add(0, null); // problem: cannot insert 'null' into this list\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	System.out.print(l.get(0).toString()); // problem: l may be null\n" +
			"	                 ^\n" +
			"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	l.add(null); // problem: cannot insert \'null\' into this list\n" +
			"	      ^^^^\n" +
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 9)\n" +
			"	System.out.print(l.get(0).toString()); // problem: l may be null\n" +
			"	                 ^\n" +
			"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 10)\n" +
			"	l.add(0, null); // problem: cannot insert \'null\' into this list\n" +
			"	         ^^^^\n" +
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 13)\n" +
			"	System.out.print(l2.get(0).toString()); // problem: l2 may be null\n" +
			"	                 ^^\n" +
			"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 14)\n" +
			"	l2.add(0, null); // problem: cannot insert \'null\' into this list\n" +
			"	          ^^^^\n" +
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
			"----------\n");
	}

	// an outer and inner class both have a type parameter,
	// client instantiates with nullable/nonnull actual type arguments
	public void test_nestedType_01() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class A<X> {\n" +
				  "    public class I<Y> {\n" +
				  "        X anX;\n" +
				  "        public X foo(Y l) {\n" +
				  "            return anX;\n" +
				  "        }\n" +
				  "        public I(X x) {\n" +
				  "            anX = x;\n" +
				  "        }\n" +
				  "    }\n" +
				  "    void bar(A<@Nullable Object>.I<@NonNull Object> i) {\n" + // legal instantiation
				  "        @NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in A.java (at line 13)\n" +
			"	@NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts\n" +
			"	                    ^^^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required '@NonNull Object' but this expression has type '@Nullable Object'\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 13)\n" +
			"	@NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts\n" +
			"	                          ^^^^\n" +
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
			"----------\n");
	}

	// an outer and inner class both have a type parameter,
	// a subclass instantiates with nullable/nonnull actual type arguments
	// and correctly implements an abstract inherited method
	// compile errors only inside that method
	public void test_nestedType_02() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  "public class A<X> {\n" +
				  "    public abstract class I<Y> {\n" +
				  "        public abstract X foo(Y l);\n" +
				  "        public X idX(X in) { return in; }\n" +
				  "        public Y idY(Y in) { return in; }\n" +
				  "    }\n" +
				  "}\n",
				"B.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class B extends A<@NonNull Object> {\n" +
				  "    public class J extends I<@Nullable String> {\n" +
				  "        @Override\n" +
				  "        public @NonNull Object foo(@Nullable String l) {\n" +
				  "            System.out.print(idX(null));\n" +
				  "            return idY(null);\n" +
				  "        }\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in B.java (at line 6)\n" +
			"	System.out.print(idX(null));\n" +
			"	                     ^^^^\n" +
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
			"----------\n" +
			"2. ERROR in B.java (at line 7)\n" +
			"	return idY(null);\n" +
			"	       ^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required '@NonNull Object' but this expression has type '@Nullable String'\n" +
			"----------\n");
	}

	// an outer and inner class both have a type parameter,
	// a subclass instantiates with nullable/nonnull actual type arguments
	// and incorrectly implements an abstract inherited method
	public void test_nestedType_03() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  "public class A<X> {\n" +
				  "    public abstract class I<Y> {\n" +
				  "        public abstract X foo(Y l);\n" +
				  "    }\n" +
				  "}\n",
				"B.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class B extends A<@NonNull Object> {\n" +
				  "    public class J extends I<@Nullable String> {\n" +
				  "        @Override\n" +
				  "        public @Nullable Object foo(@NonNull String l) {\n" +
				  "            return null;\n" +
				  "        }\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in B.java (at line 5)\n" +
			"	public @Nullable Object foo(@NonNull String l) {\n" +
			"	       ^^^^^^^^^^^^^^^^\n" +
			"The return type is incompatible with '@NonNull Object' returned from A<Object>.I<String>.foo(String) (mismatching null constraints)\n" +
			"----------\n" +
			"2. ERROR in B.java (at line 5)\n" +
			"	public @Nullable Object foo(@NonNull String l) {\n" +
			"	                            ^^^^^^^^^^^^^^^\n" +
			"Illegal redefinition of parameter l, inherited method from A<Object>.I<String> declares this parameter as @Nullable\n" +
			"----------\n");
	}

	// a reference to a nested type has annotations for both types
	public void test_nestedType_04() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  "public class A<X> {\n" +
				  "    public abstract class I<Y> {\n" +
				  "        public abstract X foo(Y l);\n" +
				  "    }\n" +
				  "}\n",
				"B.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class B {\n" +
				  "    public void foo(A<Object>.@Nullable I<@NonNull String> ai) {\n" +
				  "            ai.foo(null); // problems: ai can be null, arg must not be null\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in B.java (at line 4)\n" +
			"	ai.foo(null); // problems: ai can be null, arg must not be null\n" +
			"	^^\n" +
			"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
			"----------\n" +
			"2. ERROR in B.java (at line 4)\n" +
			"	ai.foo(null); // problems: ai can be null, arg must not be null\n" +
			"	       ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n");
	}

	// a reference to a nested type has annotations for both types, mismatch in detail of outer
	public void test_nestedType_05() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  "public class A<X> {\n" +
				  "    public abstract class I<Y> {\n" +
				  "        public abstract X foo(Y l);\n" +
				  "    }\n" +
				  "}\n",
				"B.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class B {\n" +
				  "    public void foo(A<@NonNull Object>.@Nullable I<@NonNull String> ai1) {\n" +
				  "		A<@Nullable Object>.@Nullable I<@NonNull String> ai2 = ai1;\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in B.java (at line 4)\n" +
			"	A<@Nullable Object>.@Nullable I<@NonNull String> ai2 = ai1;\n" +
			"	                                                       ^^^\n" +
			"Null type mismatch (type annotations): required \'A<@Nullable Object>.@Nullable I<@NonNull String>\' but this expression has type \'A<@NonNull Object>.@Nullable I<@NonNull String>\'\n" +
			"----------\n");
	}

	public void testMissingAnnotationTypes_01() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public class U {}\n" +
				"   @Missing1 X.@Missing2 U fU;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	@Missing1 X.@Missing2 U fU;\n" +
			"	 ^^^^^^^^\n" +
			"Missing1 cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	@Missing1 X.@Missing2 U fU;\n" +
			"	             ^^^^^^^^\n" +
			"Missing2 cannot be resolved to a type\n" +
			"----------\n",
			this.LIBS,
			true/*shouldFlush*/);
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on leaf type in 1-dim array
	public void testArrayType_01() {
		runNegativeTestWithLibs(
			new String[] {
				"Wrapper.java",
				  "public class Wrapper<T> {\n" +
				  "	T content;" +
				  "	public Wrapper(T t) { content = t; }\n" +
				  "	public T content() { return content; }\n" +
				  "}\n",
				"A.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class A {\n" +
// Using Wrapper is a workaround until bug 391331 is fixed (to force the interesting annotation to be consumed as a type annotation):
				  "    void bar(Wrapper<@NonNull String[]> realStrings, Wrapper<@Nullable String[]> maybeStrings) {\n" +
				  "        System.out.println(realStrings.content()[0].toUpperCase()); // no problem\n" +
				  "        realStrings.content()[0] = null; // problem: cannot assign null as @NonNull element\n" +
				  "        System.out.println(maybeStrings.content()[0].toUpperCase()); // problem: element can be null\n" +
				  "        maybeStrings.content()[0] = null; // no problem\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in A.java (at line 5)\n" +
			"	realStrings.content()[0] = null; // problem: cannot assign null as @NonNull element\n" +
			"	                           ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 6)\n" +
			"	System.out.println(maybeStrings.content()[0].toUpperCase()); // problem: element can be null\n" +
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Potential null pointer access: array element may be null\n" +
			"----------\n");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on leaf type in 2-dim array
	public void testArrayType_02() {
		runNegativeTestWithLibs(
			new String[] {
				"Wrapper.java",
				  "public class Wrapper<T> {\n" +
				  "	T content;" +
				  "	public Wrapper(T t) { content = t; }\n" +
				  "	public T content() { return content; }\n" +
				  "}\n",
				"A.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class A {\n" +
// Using Wrapper is a workaround until bug 391331 is fixed (to force the interesting annotation to be consumed as a type annotation):
				  "    void bar(Wrapper<@NonNull String[][]> realStrings, Wrapper<@Nullable String[][]> maybeStrings) {\n" +
				  "        System.out.println(realStrings.content()[0][0].toUpperCase()); // no problem\n" +
				  "        realStrings.content()[0][0] = null; // problem: cannot assign null as @NonNull element\n" +
				  "        System.out.println(maybeStrings.content()[0][0].toUpperCase()); // problem: element can be null\n" +
				  "        maybeStrings.content()[0][0] = null; // no problem\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in A.java (at line 5)\n" +
			"	realStrings.content()[0][0] = null; // problem: cannot assign null as @NonNull element\n" +
			"	                              ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 6)\n" +
			"	System.out.println(maybeStrings.content()[0][0].toUpperCase()); // problem: element can be null\n" +
			"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Potential null pointer access: array element may be null\n" +
			"----------\n");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on array type (1-dim array)
	public void testArrayType_03() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class A {\n" +
				  "    void array(String @NonNull[] realStringArray, String @Nullable[] maybeStringArray) {\n" +
				  "        @NonNull Object array;\n" +
				  "        array = realStringArray;  // no problem\n" +
				  "        realStringArray = null; 	 // problem: cannot assign null as @NonNull array\n" +
				  "        array = maybeStringArray; // problem: array can be null\n" +
				  "        maybeStringArray = null;  // no problem\n" +
				  "    }\n" +
				  "    void leaf(String @NonNull[] realStringArray, String @Nullable[] maybeStringArray, boolean b) {\n" +
				  "        @NonNull String string;\n" +
				  "        string = realStringArray[0];  // problem: unchecked conversion\n" +
				  "        realStringArray[0] = null; 	 // no problem\n" +
				  "        if (b)\n" +
				  "            string = maybeStringArray[0]; // problems: indexing nullable array & unchecked conversion\n" +
				  "        else\n" +
				  "            maybeStringArray[0] = null; 	 // problem: indexing nullable array\n" +
				  "        maybeStringArray[0] = null; 	 // problem protected by previous dereference\n" +
				  "    }\n" +
				  "}\n"},
		    "----------\n" +
    		"1. ERROR in A.java (at line 6)\n" +
    		"	realStringArray = null; 	 // problem: cannot assign null as @NonNull array\n" +
    		"	                  ^^^^\n" +
    		"Null type mismatch: required \'String @NonNull[]\' but the provided value is null\n" +
		    "----------\n" +
    		"2. ERROR in A.java (at line 7)\n" +
    		"	array = maybeStringArray; // problem: array can be null\n" +
    		"	        ^^^^^^^^^^^^^^^^\n" +
    		"Null type mismatch (type annotations): required '@NonNull Object' but this expression has type 'String @Nullable[]'\n" +
    		"----------\n" +
    		"3. WARNING in A.java (at line 12)\n" +
    		"	string = realStringArray[0];  // problem: unchecked conversion\n" +
    		"	         ^^^^^^^^^^^^^^^^^^\n" +
    		"Null type safety (type annotations): The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'\n" +
    		"----------\n" +
    		"4. ERROR in A.java (at line 15)\n" +
    		"	string = maybeStringArray[0]; // problems: indexing nullable array & unchecked conversion\n" +
    		"	         ^^^^^^^^^^^^^^^^\n" +
    		"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
    		"----------\n" +
    		"5. WARNING in A.java (at line 15)\n" +
    		"	string = maybeStringArray[0]; // problems: indexing nullable array & unchecked conversion\n" +
    		"	         ^^^^^^^^^^^^^^^^^^^\n" +
    		"Null type safety (type annotations): The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'\n" +
    		"----------\n" +
    		"6. ERROR in A.java (at line 17)\n" +
    		"	maybeStringArray[0] = null; 	 // problem: indexing nullable array\n" +
    		"	^^^^^^^^^^^^^^^^\n" +
    		"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
			"----------\n");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on intermediate type in 2-dim array
	public void testArrayType_04() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class A {\n" +
				  "    void outer(String [] @NonNull[] realArrays, String [] @Nullable[] maybeArrays) {\n" +
				  "        @NonNull Object array;\n" +
				  "        array = realArrays; 		// problem: unchecked conversion\n" +
				  "        realArrays = null; 		// no problem, outer array is unspecified\n" +
				  "        array = maybeArrays; 	// problem: unchecked conversion\n" +
				  "        maybeArrays = null; 		// no problem\n" +
				  "    }\n" +
				  "    void inner(String [] @NonNull[] realArrays, String [] @Nullable[] maybeArrays) {\n" +
				  "        @NonNull Object array;\n" +
				  "        array = realArrays[0]; 	// no problem\n" +
				  "        realArrays[0] = null; 	// problem: cannot assign null to @NonNull array\n" +
				  "        array = maybeArrays[0]; 	// problem: element can be null\n" +
				  "        maybeArrays[0] = null; 	// no problem\n" +
				  "    }\n" +
				  "    void leaf(String [] @NonNull[] realArrays, String [] @Nullable[] maybeArrays) {\n" +
				  "        @NonNull Object array;\n" +
				  "        array = realArrays[0][0]; // problem: unchecked conversion\n" +
				  "        realArrays[0][0] = null;  // no problem, element type is unspecified\n" +
				  "        array = maybeArrays[0][0]; // problems: indexing nullable array & unchecked conversion\n" +
				  "        maybeArrays[0][0] = null; // problem: indexing nullable array\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. WARNING in A.java (at line 5)\n" +
			"	array = realArrays; 		// problem: unchecked conversion\n" +
			"	        ^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String [] @NonNull[]\' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
			"----------\n" +
			"2. WARNING in A.java (at line 7)\n" +
			"	array = maybeArrays; 	// problem: unchecked conversion\n" +
			"	        ^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String [] @Nullable[]\' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
			"----------\n" +
			"3. ERROR in A.java (at line 13)\n" +
			"	realArrays[0] = null; 	// problem: cannot assign null to @NonNull array\n" +
			"	                ^^^^\n" +
			"Null type mismatch: required \'String @NonNull[]\' but the provided value is null\n" +
			"----------\n" +
			"4. ERROR in A.java (at line 14)\n" +
			"	array = maybeArrays[0]; 	// problem: element can be null\n" +
			"	        ^^^^^^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'String @Nullable[]\'\n" +
			"----------\n" +
			"5. WARNING in A.java (at line 19)\n" +
			"	array = realArrays[0][0]; // problem: unchecked conversion\n" +
			"	        ^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
			"----------\n" +
			"6. ERROR in A.java (at line 21)\n" +
			"	array = maybeArrays[0][0]; // problems: indexing nullable array & unchecked conversion\n" +
			"	        ^^^^^^^^^^^^^^\n" +
			"Potential null pointer access: array element may be null\n" +
			"----------\n" +
			"7. WARNING in A.java (at line 21)\n" +
			"	array = maybeArrays[0][0]; // problems: indexing nullable array & unchecked conversion\n" +
			"	        ^^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
			"----------\n" +
			"8. ERROR in A.java (at line 22)\n" +
			"	maybeArrays[0][0] = null; // problem: indexing nullable array\n" +
			"	^^^^^^^^^^^^^^\n" +
			"Potential null pointer access: array element may be null\n" +
			"----------\n");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// mismatches against outer array type, test display of type annotation in error messages
	public void testArrayType_05() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class A {\n" +
				  "    void outer(String @NonNull[] @NonNull[] realArrays, String @NonNull[] @Nullable[] maybeArrays, String @Nullable[][] unknownArrays) {\n" +
				  "        realArrays[0] = maybeArrays[0];		// problem: inner array can be null\n" +
				  "        realArrays[0] = unknownArrays[0];	// problems: inner array is unspecified, outer can be null\n" +
				  "    }\n" +
				  "    void oneDim(String @Nullable[] maybeStrings, String[] unknownStrings) {\n" +
				  "        String @NonNull[] s = maybeStrings;\n" +
				  "        s = unknownStrings;\n" +
				  "        consume(maybeStrings);\n" +
				  "        consume(unknownStrings);\n" +
				  "    }\n" +
				  "    void consume(String @NonNull[] s) {};\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in A.java (at line 4)\n" +
			"	realArrays[0] = maybeArrays[0];		// problem: inner array can be null\n" +
			"	                ^^^^^^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'String @NonNull[]\' but this expression has type \'String @Nullable[]\'\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 5)\n" +
			"	realArrays[0] = unknownArrays[0];	// problems: inner array is unspecified, outer can be null\n" +
			"	                ^^^^^^^^^^^^^\n" +
			"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
			"----------\n" +
			"3. WARNING in A.java (at line 5)\n" +
			"	realArrays[0] = unknownArrays[0];	// problems: inner array is unspecified, outer can be null\n" +
			"	                ^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'String @NonNull[]\'\n" +
			"----------\n" +
			"4. ERROR in A.java (at line 8)\n" +
			"	String @NonNull[] s = maybeStrings;\n" +
			"	                      ^^^^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'String @NonNull[]\' but this expression has type \'String @Nullable[]\'\n" +
			"----------\n" +
			"5. WARNING in A.java (at line 9)\n" +
			"	s = unknownStrings;\n" +
			"	    ^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'String @NonNull[]\'\n" +
			"----------\n" +
			"6. ERROR in A.java (at line 10)\n" +
			"	consume(maybeStrings);\n" +
			"	        ^^^^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'String @NonNull[]\' but this expression has type \'String @Nullable[]\'\n" +
			"----------\n" +
			"7. WARNING in A.java (at line 11)\n" +
			"	consume(unknownStrings);\n" +
			"	        ^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'String @NonNull[]\'\n" +
			"----------\n");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// more compiler messages
	public void testArrayType_10() {
		runNegativeTestWithLibs(
			new String[] {
				"A.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "public class A {\n" +
				  "    void outer(String @NonNull[] @NonNull[] realArrays, String @NonNull[] @Nullable[] maybeArrays, String @Nullable[][] unknownArrays, String @NonNull[][] mixedArrays) {\n" +
				  "        realArrays = maybeArrays;			// problem on inner dimension!\n" +
				  "        realArrays = unknownArrays; 			// problems on both dimensions\n" +
				  "        maybeArrays = realArrays;			// problem on inner dimension\n" +
				  "        unknownArrays = maybeArrays;			// no problem: outer @NonNull is compatible to expected @Nullable, inner @Nullable is compatible to inner unspecified\n" +
				  "        realArrays = mixedArrays;			// problem on inner\n" +
				  "        maybeArrays = mixedArrays;			// problem on inner\n" +
				  "        consume(maybeArrays, mixedArrays, maybeArrays);\n" +
				  "    }\n" +
				  "    void consume(String @NonNull[] @NonNull[] realStrings, String @NonNull[] @Nullable[] maybeArrays, String @Nullable[][] unknownArrays) {\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" +
			"1. ERROR in A.java (at line 4)\n" +
			"	realArrays = maybeArrays;			// problem on inner dimension!\n" +
			"	             ^^^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'String @NonNull[] @NonNull[]\' but this expression has type \'String @NonNull[] @Nullable[]\'\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 5)\n" +
			"	realArrays = unknownArrays; 			// problems on both dimensions\n" +
			"	             ^^^^^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'String @NonNull[] @NonNull[]\' but this expression has type \'String @Nullable[] []\'\n" +
			"----------\n" +
			"3. ERROR in A.java (at line 6)\n" +
			"	maybeArrays = realArrays;			// problem on inner dimension\n" +
			"	              ^^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'String @NonNull[] @Nullable[]\' but this expression has type \'String @NonNull[] @NonNull[]\'\n" +
			"----------\n" +
			"4. WARNING in A.java (at line 8)\n" +
			"	realArrays = mixedArrays;			// problem on inner\n" +
			"	             ^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @NonNull[]\'\n" +
			"----------\n" +
			"5. WARNING in A.java (at line 9)\n" +
			"	maybeArrays = mixedArrays;			// problem on inner\n" +
			"	              ^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @Nullable[]\'\n" +
			"----------\n" +
			"6. ERROR in A.java (at line 10)\n" +
			"	consume(maybeArrays, mixedArrays, maybeArrays);\n" +
			"	        ^^^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'String @NonNull[] @NonNull[]\' but this expression has type \'String @NonNull[] @Nullable[]\'\n" +
			"----------\n" +
			"7. WARNING in A.java (at line 10)\n" +
			"	consume(maybeArrays, mixedArrays, maybeArrays);\n" +
			"	                     ^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @Nullable[]\'\n" +
			"----------\n");
	}

	// combine flow info on outer type with annotation analysis for inners
	public void testArrayType_11() {
		runNegativeTestWithLibs(
			new String[] {
				"ArrayTest.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"public class ArrayTest {\n" +
				"	\n" +
				"	@NonNull Object @NonNull[] test1(@NonNull Object @Nullable[] in) {\n" +
				"		if (in == null) throw new NullPointerException(); \n" +
				"		return in; // array needs check, element is OK\n" +
				"	}\n" +
				"	@NonNull Object @NonNull[] test2(@Nullable Object @Nullable[] in) {\n" +
				"		if (in == null) throw new NullPointerException(); \n" +
				"		return in; // array needs check, element is NOK\n" +
				"	}\n" +
				"	@NonNull Object @NonNull[]@NonNull[] test3(@NonNull Object @Nullable[][] in) {\n" +
				"		if (in == null) throw new NullPointerException(); \n" +
				"		return in; // outer needs check, inner is unchecked, element is OK\n" +
				"	}\n" +
				"	@NonNull Object @NonNull[]@NonNull[] test4(@Nullable Object @Nullable[][] in) {\n" +
				"		if (in == null) throw new NullPointerException(); \n" +
				"		return in; // outer needs check, inner is unchecked, element is NOK\n" +
				"	}\n" +
				"	@NonNull Object @NonNull[]@NonNull[] test5(@NonNull Object @Nullable[]@Nullable[] in) {\n" +
				"		if (in == null) throw new NullPointerException(); \n" +
				"		return in; // outer needs check, inner is NOK, element is OK\n" +
				"	}\n" +
				"	@NonNull Object @NonNull[]@NonNull[] test6(@NonNull Object @Nullable[]@NonNull[] in) {\n" +
				"		if (in == null) throw new NullPointerException(); \n" +
				"		return in; // outer needs check, inner is OK, element is OK\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in ArrayTest.java (at line 11)\n" +
			"	return in; // array needs check, element is NOK\n" +
			"	       ^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull Object @NonNull[]\' but this expression has type \'@Nullable Object @Nullable[]\'\n" +
			"----------\n" +
			"2. WARNING in ArrayTest.java (at line 15)\n" +
			"	return in; // outer needs check, inner is unchecked, element is OK\n" +
			"	       ^^\n" +
			"Null type safety (type annotations): The expression of type \'@NonNull Object @Nullable[] []\' needs unchecked conversion to conform to \'@NonNull Object @NonNull[] @NonNull[]\'\n" +
			"----------\n" +
			"3. ERROR in ArrayTest.java (at line 19)\n" +
			"	return in; // outer needs check, inner is unchecked, element is NOK\n" +
			"	       ^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull Object @NonNull[] @NonNull[]\' but this expression has type \'@Nullable Object @Nullable[] []\'\n" +
			"----------\n" +
			"4. ERROR in ArrayTest.java (at line 23)\n" +
			"	return in; // outer needs check, inner is NOK, element is OK\n" +
			"	       ^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull Object @NonNull[] @NonNull[]\' but this expression has type \'@NonNull Object @Nullable[] @Nullable[]\'\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/403216 - [1.8][null] TypeReference#captureTypeAnnotations treats type annotations as type argument annotations
	public void testBug403216_1() {
		runConformTestWithLibs(
			new String[] {
				"Test.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"public class Test {}\n" +
				"\n" +
				"class X {\n" +
				"	class Y {\n" +
				"		public void foo( @A X. @B Y this) {}\n" +
				"	}\n" +
				"}\n" +
				"@Target(value={ElementType.TYPE_USE})\n" +
				"@interface A {}\n" +
				"@Target(value={ElementType.TYPE_USE})\n" +
				"@interface B {}\n"
			},
			null,
			"");
	}

	// issue from https://bugs.eclipse.org/bugs/show_bug.cgi?id=403216#c7
	public void testBug403216_2() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.IGNORE);
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import java.util.*;\n" +
				"public class X {\n" +
				"    void test(List<@NonNull String> strings) {\n" +
				"        List<String> someStrings;\n" +
				"        someStrings = strings;\n" +
				"    }\n" +
				"}\n"
			},
			options,
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=403216#c9
	public void testBug403216_3a() {
		runNegativeTestWithLibs(
			new String[] {
				"Test.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"public class Test {}\n" +
				"\n" +
				"class X {\n" +
				"	class Y {\n" +
				"		public void foo( @A X. @NonNull Y this) {}\n" +
				"	}\n" +
				"}\n" +
				"@Target(value={ElementType.TYPE_USE})\n" +
				"@interface A {}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in Test.java (at line 9)\n" +
			"	public void foo( @A X. @NonNull Y this) {}\n" +
			"	                 ^^^^^^^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=403216#c9
	public void testBug403216_3b() {
		runConformTestWithLibs(
			new String[] {
				"Test.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"public class Test {}\n" +
				"\n" +
				"class X {\n" +
				"	class Y {\n" +
				"		public void foo( @A X. @A Y this) {}\n" +
				"	}\n" +
				"}\n" +
				"@Target(value={ElementType.TYPE_USE})\n" +
				"@interface A {}\n"
			},
			getCompilerOptions(),
			"");
	}

	// https://bugs.eclipse.org/403457 - [1.8][compiler] NPE in WildcardBinding.signature
	public void testBug403457_1() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"public class X {\n" +
				"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" +
				"   void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" +
				"}\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface Marker {\n" +
				"	\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	void foo(Map<@Marker ? super @Marker Object, @Marker ? extends @Marker String> m){}\n" +
			"	         ^^^\n" +
			"Map cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	void goo(Map<@Marker ? extends @Marker Object, @Marker ? super @Marker String> m){}\n" +
			"	         ^^^\n" +
			"Map cannot be resolved to a type\n" +
			"----------\n",
			this.LIBS,
			true/*flush*/);
	}

	// https://bugs.eclipse.org/403457 - [1.8][compiler] NPE in WildcardBinding.signature
	// variant with null annotations
	public void testBug403457_2() {
		runNegativeTest(
			new String[] {
				"X.java",
				"// import java.util.Map;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"public class X {\n" +
				"	void foo(Map<@Nullable ? super @Nullable Object, @Nullable ? extends @Nullable String> m){}\n" +
				"   void goo(Map<@Nullable ? extends @Nullable Object, @Nullable ? super @Nullable String> m){}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	void foo(Map<@Nullable ? super @Nullable Object, @Nullable ? extends @Nullable String> m){}\n" +
			"	         ^^^\n" +
			"Map cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	void goo(Map<@Nullable ? extends @Nullable Object, @Nullable ? super @Nullable String> m){}\n" +
			"	         ^^^\n" +
			"Map cannot be resolved to a type\n" +
			"----------\n",
			this.LIBS,
			true/*flush*/);
	}

	// storing and decoding null-type-annotations to/from classfile: RETURN_TYPE
	public void testBinary01() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runConformTestWithLibs(
				new String[] {
					"p/X.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class X {\n" +
					"	public List<@Nullable String> getSomeStrings() { return null; }\n" +
					"}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y.java",
					"import p.X;\n" +
					"public class Y {\n" +
					"	public void test(X x) {\n" +
					"		String s0 = x.getSomeStrings().get(0);\n" +
					"		System.out.println(s0.toUpperCase());\n" +
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in Y.java (at line 5)\n" +
				"	System.out.println(s0.toUpperCase());\n" +
				"	                   ^^\n" +
				"Potential null pointer access: The variable s0 may be null at this location\n" +
				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: METHOD_FORMAL_PARAMETER & METHOD_RECEIVER
	// Note: receiver annotation is not evaluated by the compiler, this part of the test only serves debugging purposes.
	public void testBinary02() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runConformTestWithLibs(
				new String[] {
					"p/X.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"import java.lang.annotation.*;\n" +
					"@Retention(RetentionPolicy.CLASS)\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Immutable {}\n" +
					"public class X {\n" +
					"	public void setAllStrings(@Immutable X this, int dummy, List<@NonNull String> ss) { }\n" +
					"}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y.java",
					"import p.X;\n" +
					"import java.util.List;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class Y {\n" +
					"	public void test(X x, List<@Nullable String> ss) {\n" +
					"		x.setAllStrings(-1, ss);\n" +
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in Y.java (at line 6)\n" +
				"	x.setAllStrings(-1, ss);\n" +
				"	                    ^^\n" +
				"Null type mismatch (type annotations): required \'List<@NonNull String>\' but this expression has type \'List<@Nullable String>\'\n" +
				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: FIELD
	public void testBinary03() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public abstract class X1 {\n" +
					"	public static String @Nullable [] f1 = null;\n" +
					"	public static String [] @Nullable [] f2 = new String[] @Nullable[] { null };\n" +
					"}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"public class Y1 {\n" +
					"	public void test() {\n" +
					"		System.out.println(p.X1.f1.length);\n" +
					"		System.out.println(X1.f2[0].length);\n" +
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in Y1.java (at line 4)\n" +
				"	System.out.println(p.X1.f1.length);\n" +
				"	                        ^^\n" +
				"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
				"----------\n" +
				"2. ERROR in Y1.java (at line 5)\n" +
				"	System.out.println(X1.f2[0].length);\n" +
				"	                   ^^^^^^^^\n" +
				"Potential null pointer access: array element may be null\n" +
				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: SUPER_TYPE
	public void testBinary04() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import java.util.ArrayList;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"@SuppressWarnings(\"serial\")\n" +
					"public abstract class X1 extends ArrayList<@Nullable String> {\n" +
					"}\n",
					"p/X2.java",
					"package p;\n" +
					"import java.util.List;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public abstract class X2 implements List<@Nullable String> {\n" +
					"}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"public class Y1 {\n" +
					"	public void test(X1 x) {\n" +
					"		String s0 = x.get(0);\n" +
					"		System.out.println(s0.toUpperCase());\n" +
					"	}\n" +
					"}\n",
					"Y2.java",
					"import p.X2;\n" +
					"public class Y2 {\n" +
					"	public void test(X2 x) {\n" +
					"		String s0 = x.get(0);\n" +
					"		System.out.println(s0.toUpperCase());\n" +
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in Y1.java (at line 5)\n" +
				"	System.out.println(s0.toUpperCase());\n" +
				"	                   ^^\n" +
				"Potential null pointer access: The variable s0 may be null at this location\n" +
				"----------\n" +
				"----------\n" +
				"1. ERROR in Y2.java (at line 5)\n" +
				"	System.out.println(s0.toUpperCase());\n" +
				"	                   ^^\n" +
				"Potential null pointer access: The variable s0 may be null at this location\n" +
				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: CLASS_TYPE_PARAMETER & METHOD_TYPE_PARAMETER
	public void testBinary05() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import java.util.ArrayList;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"@SuppressWarnings(\"serial\")\n" +
					"public abstract class X1<@NonNull T> extends ArrayList<T> {\n" +
					"    public <@Nullable S> void foo(S s) {}\n" +
					"}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class Y1 {\n" +
					"	X1<@Nullable String> maybeStrings;\n" + // incompatible: T is constrained to @NonNull
					"	void test(X1<@NonNull String> x) {\n" + // OK
					"		x.<@NonNull Object>foo(new Object());\n" + // incompatible: S is constrained to @Nullable
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in Y1.java (at line 4)\n" +
				"	X1<@Nullable String> maybeStrings;\n" +
				"	   ^^^^^^^^^^^^^^^^\n" +
				"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T extends Object\'\n" +
				"----------\n" +
				"2. ERROR in Y1.java (at line 6)\n" +
				"	x.<@NonNull Object>foo(new Object());\n" +
				"	   ^^^^^^^^^^^^^^^\n" +
				"Null constraint mismatch: The type \'@NonNull Object\' is not a valid substitute for the type parameter \'@Nullable S extends Object\'\n" +
				"----------\n");
	}

	// storing and decoding null-type-annotations to/from classfile: CLASS_TYPE_PARAMETER_BOUND & METHOD_TYPE_PARAMETER_BOUND
	public void testBinary06() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runNegativeTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import java.util.ArrayList;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"@SuppressWarnings(\"serial\")\n" +
					"public abstract class X1<T extends @NonNull Object> extends ArrayList<T> {\n" +
					"    public <U, V extends @Nullable Object> void foo(U u, V v) {}\n" +
					"}\n",
					"p/X2.java",
					"package p;\n"+
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class X2<@NonNull W extends @Nullable Object> {}\n" // incompatible constraints
				},
				customOptions,
				"----------\n" +
				"1. ERROR in p\\X2.java (at line 3)\n" +
				"	public class X2<@NonNull W extends @Nullable Object> {}\n" +
				"	                                   ^^^^^^^^^\n" +
				"This nullness annotation conflicts with a \'@NonNull\' annotation which is effective on the same type parameter \n" +
				"----------\n");
		// fix the bug:
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import java.util.ArrayList;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"@SuppressWarnings(\"serial\")\n" +
					"public abstract class X1<T extends @NonNull Object> extends ArrayList<T> {\n" +
					"    public <U, V extends @Nullable Object> void foo(U u, V v) {}\n" +
					"}\n",
					"p/X2.java",
					"package p;\n"+
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class X2<@Nullable W extends Object> {}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"import p.X2;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class Y1 {\n" +
					"	X1<@Nullable String> maybeStrings;\n" + // incompatible: T has a bound constrained to @NonNull
					"   X2<@NonNull String> strings;\n" +       // incompatible: W is constrained to @Nullable
					"	void test(X1<@NonNull String> x) {\n" + // OK
					"		x.<Y1, @NonNull Object>foo(this, new Object());\n" + // OK: 'extends @Nullable' is no restriction
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in Y1.java (at line 5)\n" +
				"	X1<@Nullable String> maybeStrings;\n" +
				"	   ^^^^^^^^^^^^^^^^\n" +
				"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'\n" +
				"----------\n" +
				"2. ERROR in Y1.java (at line 6)\n" +
				"	X2<@NonNull String> strings;\n" +
				"	   ^^^^^^^^^^^^^^^\n" +
				"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable W extends Object\'\n" +
				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: CLASS_TYPE_PARAMETER_BOUND & METHOD_TYPE_PARAMETER_BOUND
	// variant: qualified type references
	public void testBinary06b() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runNegativeTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import java.util.ArrayList;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"@SuppressWarnings(\"serial\")\n" +
					"public abstract class X1<T extends java.lang.@NonNull Object> extends ArrayList<T> {\n" +
					"    public <U, V extends java.lang.@Nullable Object> void foo(U u, V v) {}\n" +
					"}\n",
					"p/X2.java",
					"package p;\n"+
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class X2<@NonNull W extends java.lang.@Nullable Object> {}\n" // incompatible constraints
				},
				customOptions,
				"----------\n" +
				"1. ERROR in p\\X2.java (at line 3)\n" +
				"	public class X2<@NonNull W extends java.lang.@Nullable Object> {}\n" +
				"	                                             ^^^^^^^^^\n" +
				"This nullness annotation conflicts with a \'@NonNull\' annotation which is effective on the same type parameter \n" +
				"----------\n");
		// fix the bug:
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import java.util.ArrayList;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"@SuppressWarnings(\"serial\")\n" +
					"public abstract class X1<T extends java.lang.@NonNull Object> extends ArrayList<T> {\n" +
					"    public <U, V extends java.lang.@Nullable Object> void foo(U u, V v) {}\n" +
					"}\n",
					"p/X2.java",
					"package p;\n"+
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class X2<@Nullable W extends Object> {}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class Y1 {\n" +
					"	p.X1<java.lang.@Nullable String> maybeStrings;\n" + // incompatible: T has a bound constrained to @NonNull
					"   p.X2<java.lang.@NonNull String> strings;\n" +       // incompatible: W is constrained to @Nullable
					"	void test(p.X1<java.lang.@NonNull String> x) {\n" + // OK
					"		x.<Y1, java.lang.@NonNull Object>foo(this, new Object());\n" + // // OK: 'extends @Nullable' is no restriction
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in Y1.java (at line 3)\n" +
				"	p.X1<java.lang.@Nullable String> maybeStrings;\n" +
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'\n" +
				"----------\n" +
				"2. ERROR in Y1.java (at line 4)\n" +
				"	p.X2<java.lang.@NonNull String> strings;\n" +
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable W extends Object\'\n" +
				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: method with all kinds of type annotations
	public void testBinary07() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
				new String[] {
					"p/List.java",
					"package p;\n" +
					"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
					"public interface List<T> {\n" +
					"	T get(int i);\n" + // avoid IProblem.NonNullTypeVariableFromLegacyMethod against unannotated j.u.List
					"}\n",
					"p/X1.java",
					"package p;\n" +
					"import java.util.Map;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"import java.lang.annotation.*;\n" +
					"@Retention(RetentionPolicy.CLASS)\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Immutable {}\n" +
					"public abstract class X1 {\n" +
					"    public <@NonNull U, V extends @Nullable Object> List<@NonNull Map<Object, @NonNull String>> foo(@Immutable X1 this, U u, V v) { return null; }\n" +
					"}\n"
				},
				customOptions,
				"");
		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class Y1 {\n" +
					"	void test(X1 x) {\n" +
					"		x.<@NonNull Y1, @NonNull Object>foo(this, new Object())\n" + // // OK: 'extends @Nullable' is no restriction
					"			.get(0).put(null, null);\n" + // second null is illegal
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in Y1.java (at line 6)\n" +
				"	.get(0).put(null, null);\n" +
				"	                  ^^^^\n" +
				"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
				"----------\n");
	}

	// storing and decoding null-type-annotations to/from classfile: details
	public void testBinary08() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runNegativeTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import java.util.*;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public abstract class X1 {\n" +
					"    public class Inner {}\n" +
					"    public Object []@NonNull[] arrays(Object @NonNull[][] oa1) { return null; }\n" +
					"    public void nesting(@NonNull Inner i1, X1.@Nullable Inner i2) { }\n" +
					"    public void wildcard1(List<@Nullable ? extends @NonNull X1> l) { } // contradiction\n" +
					"    public void wildcard2(List<? super @NonNull X1> l) { }\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in p\\X1.java (at line 8)\n" +
				"	public void wildcard1(List<@Nullable ? extends @NonNull X1> l) { } // contradiction\n" +
				"	                                               ^^^^^^^^\n" +
				"This nullness annotation conflicts with a \'@Nullable\' annotation which is effective on the same type parameter \n" +
				"----------\n");
		// fix the error:
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import java.util.*;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public abstract class X1 {\n" +
					"    public class Inner {}\n" +
					"    public Object []@NonNull[] arrays(Object @NonNull[][] oa1) { return null; }\n" +
					"    public void nesting(@NonNull Inner i1, X1.@Nullable Inner i2) { }\n" +
					"    public void wildcard1(List<@Nullable ? extends X1> l) { }\n" +
					"    public void wildcard2(List<? super @NonNull X1> l) { }\n" +
					"}\n"
				},
				customOptions,
				"");

		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"import java.util.*;\n" +
					"public class Y1 {\n" +
					"	void test(X1 x) {\n" +
					"		Object @NonNull[][] a = new Object[0][]; // safe: new is never null\n" +
					"		x.arrays(a)[0] = null; // illegal\n" +
					"		x.nesting(null, null); // 1st null is illegal\n" +
					"		x.wildcard2(new ArrayList<@NonNull Object>());\n" +
					"		x.wildcard2(new ArrayList<@Nullable Object>()); // OK\n" +
					"		x.wildcard1(new ArrayList<@NonNull X1>()); // incompatible\n" +
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in Y1.java (at line 7)\n" +
				"	x.arrays(a)[0] = null; // illegal\n" +
				"	                 ^^^^\n" +
				"Null type mismatch: required \'Object @NonNull[]\' but the provided value is null\n" +
				"----------\n" +
				"2. ERROR in Y1.java (at line 8)\n" +
				"	x.nesting(null, null); // 1st null is illegal\n" +
				"	          ^^^^\n" +
				"Null type mismatch: required \'X1.@NonNull Inner\' but the provided value is null\n" +
				"----------\n" +
				"3. ERROR in Y1.java (at line 11)\n" +
				"	x.wildcard1(new ArrayList<@NonNull X1>()); // incompatible\n" +
				"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Null type mismatch (type annotations): required \'List<@Nullable ? extends X1>\' but this expression has type \'@NonNull ArrayList<@NonNull X1>\', corresponding supertype is \'List<@NonNull X1>\'\n" +
				"----------\n");
	}

	// storing and decoding null-type-annotations to/from classfile: details
	// variant: qualified references
	public void testBinary08b() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runNegativeTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public abstract class X1 {\n" +
					"    public class Inner {}\n" +
					"    public java.lang.Object []@NonNull[] arrays(java.lang.Object @NonNull[][] oa1) { return null; }\n" +
					"    public void nesting(@NonNull Inner i1, X1.@Nullable Inner i2) { }\n" +
					"    public void wildcard1(java.util.List<@Nullable ? extends p.@NonNull X1> l) { } // contradiction\n" +
					"    public void wildcard2(java.util.List<? super p.@NonNull X1> l) { }\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in p\\X1.java (at line 7)\n" +
				"	public void wildcard1(java.util.List<@Nullable ? extends p.@NonNull X1> l) { } // contradiction\n" +
				"	                                                           ^^^^^^^^\n" +
				"This nullness annotation conflicts with a \'@Nullable\' annotation which is effective on the same type parameter \n" +
				"----------\n");
		// fix the error:
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import java.util.*;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public abstract class X1 {\n" +
					"    public class Inner {}\n" +
					"    public java.lang.Object []@NonNull[] arrays(java.lang.Object @NonNull[][] oa1) { return null; }\n" +
					"    public void nesting(@NonNull Inner i1, p.X1.@Nullable Inner i2) { }\n" +
					"    public void wildcard1(List<@Nullable ? extends p.X1> l) { }\n" +
					"    public void wildcard2(List<? super p.@NonNull X1> l) { }\n" +
					"}\n"
				},
				customOptions,
				"");

		runNegativeTestWithLibs(
				new String[] {
					"Y1.java",
					"import p.X1;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"import java.util.*;\n" +
					"public class Y1 {\n" +
					"	void test(X1 x) {\n" +
					"		java.lang.Object @NonNull[][] a = new java.lang.Object[0][]; // safe: new is never null\n" +
					"		x.arrays(a)[0] = null; // illegal\n" +
					"		x.nesting(null, null); // 1st null is illegal\n" +
					"		x.wildcard2(new ArrayList<java.lang.@NonNull Object>());\n" +
					"		x.wildcard2(new ArrayList<java.lang.@Nullable Object>());\n" +
					"		x.wildcard1(new ArrayList<p.@NonNull X1>()); // incompatible\n" +
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in Y1.java (at line 7)\n" +
				"	x.arrays(a)[0] = null; // illegal\n" +
				"	                 ^^^^\n" +
				"Null type mismatch: required \'Object @NonNull[]\' but the provided value is null\n" +
				"----------\n" +
				"2. ERROR in Y1.java (at line 8)\n" +
				"	x.nesting(null, null); // 1st null is illegal\n" +
				"	          ^^^^\n" +
				"Null type mismatch: required \'X1.@NonNull Inner\' but the provided value is null\n" +
				"----------\n" +
				"3. ERROR in Y1.java (at line 11)\n" +
				"	x.wildcard1(new ArrayList<p.@NonNull X1>()); // incompatible\n" +
				"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Null type mismatch (type annotations): required \'List<@Nullable ? extends X1>\' but this expression has type \'@NonNull ArrayList<@NonNull X1>\', corresponding supertype is \'List<@NonNull X1>\'\n" +
				"----------\n");
	}

	// storing and decoding null-type-annotations to/from classfile: EXTENDED DIMENSIONS.
	public void testBinary09() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runNegativeTestWithLibs(
				new String[] {
					"X.java",
					"import org.eclipse.jdt.annotation.NonNull;\n" +
					"import org.eclipse.jdt.annotation.Nullable;\n" +
					"public class X {\n" +
					"	@NonNull String @Nullable [] f @NonNull [] = null;\n" +
					"	static void foo(@NonNull String @Nullable [] p @NonNull []) {\n" +
					"		p = null;\n" +
					"		@NonNull String @Nullable [] l @NonNull [] = null;\n" +
					"	}\n" +
					"}\n"

				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	@NonNull String @Nullable [] f @NonNull [] = null;\n" +
				"	                                             ^^^^\n" +
				"Null type mismatch: required \'@NonNull String @NonNull[] @Nullable[]\' but the provided value is null\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	p = null;\n" +
				"	    ^^^^\n" +
				"Null type mismatch: required \'@NonNull String @NonNull[] @Nullable[]\' but the provided value is null\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	@NonNull String @Nullable [] l @NonNull [] = null;\n" +
				"	                                             ^^^^\n" +
				"Null type mismatch: required \'@NonNull String @NonNull[] @Nullable[]\' but the provided value is null\n" +
				"----------\n");
		// fix the error:
		runConformTestWithLibs(
				new String[] {
						"X.java",
						"import org.eclipse.jdt.annotation.NonNull;\n" +
						"import org.eclipse.jdt.annotation.Nullable;\n" +
						"public class X {\n" +
						"	@NonNull String @Nullable [] f @NonNull [] = new @NonNull String @NonNull [0] @Nullable [];\n" +
						"}\n"
				},
				customOptions,
				"");

		runNegativeTestWithLibs(
				new String[] {
					"Y.java",
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class Y {\n" +
					"	void test(X x) {\n" +
					"       x.f = null;\n" +
					"       x.f[0] = null;\n" +
					"       x.f[0][0] = null;\n" +
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. WARNING in Y.java (at line 1)\n" +
				"	import org.eclipse.jdt.annotation.*;\n" +
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"The import org.eclipse.jdt.annotation is never used\n" +
				"----------\n" +
				"2. ERROR in Y.java (at line 4)\n" +
				"	x.f = null;\n" +
				"	      ^^^^\n" +
				"Null type mismatch: required \'@NonNull String @NonNull[] @Nullable[]\' but the provided value is null\n" +
				"----------\n" +
				"3. ERROR in Y.java (at line 6)\n" +
				"	x.f[0][0] = null;\n" +
				"	^^^^^^\n" +
				"Potential null pointer access: array element may be null\n" +
				"----------\n" +
				"4. ERROR in Y.java (at line 6)\n" +
				"	x.f[0][0] = null;\n" +
				"	            ^^^^\n" +
				"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
				"----------\n");
}

	// storing and decoding null-type-annotations to/from classfile: array annotations.
	public void testBinary10() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		runNegativeTestWithLibs(
				new String[] {
					"X.java",
					"import java.util.ArrayList;\n" +
					"import org.eclipse.jdt.annotation.NonNull;\n" +
					"public class X  {\n" +
					"	void foo(ArrayList<String> @NonNull [] p) {\n" +
					"	}\n" +
					"}\n" +
					"class Y extends X {\n" +
					"	void foo() {\n" +
					"		super.foo(null);\n" +
					"	}\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	super.foo(null);\n" +
				"	          ^^^^\n" +
				"Null type mismatch: required \'ArrayList<String> @NonNull[]\' but the provided value is null\n" +
				"----------\n");
		// fix the error:
		runConformTestWithLibs(
				new String[] {
						"X.java",
						"import java.util.ArrayList;\n" +
						"import org.eclipse.jdt.annotation.NonNull;\n" +
						"public class X  {\n" +
						"	void foo(ArrayList<String> @NonNull [] p) {\n" +
						"	}\n" +
						"}\n"
				},
				customOptions,
				"");

		runNegativeTestWithLibs(
				new String[] {
					"Y.java",
					"public class Y extends X {\n" +
					"	void foo() {\n" +
					"		super.foo(null);\n" +
					"	}\n" +
					"}\n"
				},
				customOptions,
				"----------\n" +
				"1. ERROR in Y.java (at line 3)\n" +
				"	super.foo(null);\n" +
				"	          ^^^^\n" +
				"Null type mismatch: required \'ArrayList<String> @NonNull[]\' but the provided value is null\n" +
				"----------\n");
	}

	public void testConditional1() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n"
				+ "import java.util.*;\n"
				+ "public class X {\n"
				+ "	List<@NonNull String> foo(List<@NonNull String> good, List<String> dubious, int f) {\n"
				+ "		if (f < 2)\n"
				+ "			return f == 0 ? good : dubious;\n"
				+ "		if (f < 4)\n"
				+ "			return f == 2 ? dubious : good;\n"
				+ "		if (f < 6)\n"
				+ "			return f == 4 ? good : good;\n"
				+ "		return null;\n"
				+ "	}\n"
				+ "}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 6)\n" +
			"	return f == 0 ? good : dubious;\n" +
			"	                       ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 8)\n" +
			"	return f == 2 ? dubious : good;\n" +
			"	                ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'\n" +
			"----------\n");
	}

	public void testConditional2() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n"
				+ "import java.util.*;\n"
				+ "public class X {\n"
				+ "	List<@NonNull String> foo(List<@NonNull String> good, ArrayList<String> dubious, int f) {\n"
				+ "		if (f < 2)\n"
				+ "			return f == 0 ? good : dubious;\n"
				+ "		if (f < 4)\n"
				+ "			return f == 2 ? dubious : good;\n"
				+ "		if (f < 6)\n"
				+ "			return f == 4 ? good : good;\n"
				+ "		return null;\n"
				+ "	}\n"
				+ "}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 6)\n" +
			"	return f == 0 ? good : dubious;\n" +
			"	                       ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\', corresponding supertype is 'List<String>'\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 8)\n" +
			"	return f == 2 ? dubious : good;\n" +
			"	                ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\', corresponding supertype is 'List<String>'\n" +
			"----------\n");
	}

	// conditional in argument position
	public void testConditional3() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n"
				+ "import java.util.*;\n"
				+ "public class X {\n"
				+ "	void foo(List<@NonNull String> good, List<String> dubious, int f) {\n"
				+ "		consume(f == 0 ? good : dubious);\n"
				+ "		consume(f == 2 ? dubious : good);\n"
				+ "		consume(f == 4 ? good : good);\n"
				+ "	}\n" +
				"	void consume(List<@NonNull String> strings) {}\n"
				+ "}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	consume(f == 0 ? good : dubious);\n" +
			"	                        ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 6)\n" +
			"	consume(f == 2 ? dubious : good);\n" +
			"	                 ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'\n" +
			"----------\n");
	}

	// types with null annotations on details (type parameter) are compatible to equal types
	public void testCompatibility1() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n"
				+ "import java.util.*;\n"
				+ "public class X {\n"
				+ "	List<@NonNull String> return1(List<@NonNull String> noNulls) {\n"
				+ "		return noNulls;\n"
				+ "	}\n"
				+ "	List<@Nullable String> return2(List<@Nullable String> withNulls) {\n"
				+ "		return withNulls;\n"
				+ "	}\n"
				+ "	void assigns(List<@NonNull String> noNulls, List<String> dubious, List<@Nullable String> withNulls) {\n"
				+ "		List<@NonNull String> l1 = noNulls;\n"
				+ "		List<@Nullable String> l2 = withNulls;\n"
				+ "		List<String> l3 = dubious;\n"
				+ "	}\n"
				+ "	void arguments(List<@NonNull String> noNulls, List<String> dubious, List<@Nullable String> withNulls) {\n"
				+ "		assigns(noNulls, dubious, withNulls);\n"
				+ "	}\n"
				+ "}\n"
			},
			getCompilerOptions(),
			"");
	}

	// types with null annotations on details (array content) are compatible to equal types
	public void testCompatibility1a() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n"
				+ "public class X {\n"
				+ "	@NonNull String[] return1(@NonNull String[] noNulls) {\n"
				+ "		return noNulls;\n"
				+ "	}\n"
				+ "	@Nullable String[] return2(@Nullable String[] noNulls) {\n"
				+ "		return noNulls;\n"
				+ "	}\n"
				+ "	void assigns(@NonNull String[] noNulls, String dubious[], @Nullable String[] withNulls) {\n"
				+ "		@NonNull String[] l1 = noNulls;\n"
				+ "		@Nullable String[] l2 = withNulls;\n"
				+ "		String[] l3 = dubious;\n"
				+ "	}\n"
				+ "	void arguments(@NonNull String[] noNulls, String[] dubious, @Nullable String[] withNulls) {\n"
				+ "		assigns(noNulls, dubious, withNulls);\n"
				+ "	}\n"
				+ "}\n"
			},
			getCompilerOptions(),
			"");
	}

	// types with null annotations on details (type parameter) are compatible to types lacking the annotation
	public void testCompatibility2() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.IGNORE);
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n"
				+ "import java.util.*;\n"
				+ "public class X {\n"
				+ "	List<String> return1(List<@NonNull String> noNulls) {\n"
				+ "		return noNulls;\n"
				+ "	}\n"
				+ "	List<String> return2(List<String> dubious) {\n"
				+ "		return dubious;\n"
				+ "	}\n"
				+ "	List<String> return3(List<@Nullable String> withNulls) {\n"
				+ "		return withNulls;\n"
				+ "	}\n"
				+ "	void assigns(List<@NonNull String> noNulls, List<String> dubious, List<@Nullable String> withNulls) {\n"
				+ "		List<String> l1 = noNulls;\n"
				+ "		List<String> l2 = dubious;\n"
				+ "		List<String> l3 = withNulls;\n"
				+ "	}\n"
				+ "	void arguments(List<@NonNull String> noNulls, List<String> dubious, List<@Nullable String> withNulls) {\n"
				+ "		takeAny(noNulls);\n"
				+ "		takeAny(dubious);\n"
				+ "		takeAny(withNulls);\n"
				+ "	}\n"
				+ "	void takeAny(List<String> any) {}\n"
				+ "}\n"
			},
			options,
			"");
	}

	// types with null annotations on details (array content) are compatible to types lacking the annotation
	public void testCompatibility2a() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n"
				+ "public class X {\n"
				+ "	String[] return1(@NonNull String[] noNulls) {\n"
				+ "		return noNulls;\n"
				+ "	}\n"
				+ "	String[] return2(String[] dubious) {\n"
				+ "		return dubious;\n"
				+ "	}\n"
				+ "	String[] return3(@Nullable String[] withNulls) {\n"
				+ "		return withNulls;\n"
				+ "	}\n"
				+ "	void assigns(@NonNull String[] noNulls, String[] dubious, @Nullable String[] withNulls) {\n"
				+ "		String[] l1 = noNulls;\n"
				+ "		String[] l2 = dubious;\n"
				+ "		String[] l3 = withNulls;\n"
				+ "	}\n"
				+ "	void arguments(@NonNull String[] noNulls, String[] dubious, @Nullable String[] withNulls) {\n"
				+ "		takeAny(noNulls);\n"
				+ "		takeAny(dubious);\n"
				+ "		takeAny(withNulls);\n"
				+ "	}\n"
				+ "	void takeAny(String[] any) {}\n"
				+ "}\n"
			},
			getCompilerOptions(),
			"");
	}

	// types without null annotations are converted (unsafe) to types with detail annotations (type parameter)
	public void testCompatibility3() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n"
				+ "import java.util.*;\n"
				+ "public class X {\n"
				+ "	List<@NonNull String> return1(List<String> dubious) {\n"
				+ "		return dubious;\n"
				+ "	}\n"
				+ "	List<@Nullable String> return2(List<String> dubious) {\n"
				+ "		return dubious;\n"
				+ "	}\n"
				+ "	void assigns(List<String> dubious) {\n"
				+ "		List<@Nullable String> l1 = dubious;\n"
				+ "		List<@NonNull String> l2 = dubious;\n"
				+ "	}\n"
				+ "	void arguments(List<String> dubious) {\n"
				+ "		acceptNulls(dubious);\n"
				+ "		acceptNoNulls(dubious);\n"
				+ "	}\n"
				+ "	void acceptNulls(List<@NonNull String> noNulls) {}\n"
				+ "	void acceptNoNulls(List<@NonNull String> noNulls) {}\n"
				+ "}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	return dubious;\n" +
			"	       ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 8)\n" +
			"	return dubious;\n" +
			"	       ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@Nullable String>\'\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 11)\n" +
			"	List<@Nullable String> l1 = dubious;\n" +
			"	                            ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@Nullable String>\'\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 12)\n" +
			"	List<@NonNull String> l2 = dubious;\n" +
			"	                           ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 15)\n" +
			"	acceptNulls(dubious);\n" +
			"	            ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 16)\n" +
			"	acceptNoNulls(dubious);\n" +
			"	              ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'List<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'\n" +
			"----------\n");
	}

	// types without null annotations are converted (unsafe) to types with detail annotations (array content)
	public void testCompatibility3a() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n"
				+ "public class X {\n"
				+ "	@NonNull String[] return1(String[] dubious) {\n"
				+ "		return dubious;\n"
				+ "	}\n"
				+ "	@Nullable String[] return2(String[] dubious) {\n"
				+ "		return dubious;\n"
				+ "	}\n"
				+ "	void assigns(String[] dubious) {\n"
				+ "		@Nullable String[] l1 = dubious;\n"
				+ "		@NonNull String[] l2 = dubious;\n"
				+ "	}\n"
				+ "	void arguments(String[] dubious) {\n"
				+ "		acceptNulls(dubious);\n"
				+ "		acceptNoNulls(dubious);\n"
				+ "	}\n"
				+ "	void acceptNulls(@Nullable String[] withNulls) {}\n"
				+ "	void acceptNoNulls(@NonNull String[] noNulls) {}\n"
				+ "}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	return dubious;\n" +
			"	       ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String []\'\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 7)\n" +
			"	return dubious;\n" +
			"	       ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@Nullable String []\'\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 10)\n" +
			"	@Nullable String[] l1 = dubious;\n" +
			"	                        ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@Nullable String []\'\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 11)\n" +
			"	@NonNull String[] l2 = dubious;\n" +
			"	                       ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String []\'\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 14)\n" +
			"	acceptNulls(dubious);\n" +
			"	            ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@Nullable String []\'\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 15)\n" +
			"	acceptNoNulls(dubious);\n" +
			"	              ^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String []\'\n" +
			"----------\n");
	}

	// types with null annotations on details (type parameter) are incompatible to opposite types
	public void testCompatibility4() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n"
				+ "import java.util.*;\n"
				+ "public class X {\n"
				+ "	List<@Nullable String> return1(List<@NonNull String> noNulls) {\n"
				+ "		return noNulls;\n"
				+ "	}\n"
				+ "	List<@NonNull String> return2(List<@Nullable String> withNulls) {\n"
				+ "		return withNulls;\n"
				+ "	}\n"
				+ "	void assigns(List<@NonNull String> noNulls, List<@Nullable String> withNulls) {\n"
				+ "		List<@NonNull String> l1 = withNulls;\n"
				+ "		List<@Nullable String> l2 = noNulls;\n"
				+ "	}\n"
				+ "	void arguments(List<@NonNull String> noNulls, List<@Nullable String> withNulls) {\n"
				+ "		assigns(withNulls, noNulls);\n"
				+ "	}\n"
				+ "}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	return noNulls;\n" +
			"	       ^^^^^^^\n" +
			"Null type mismatch (type annotations): required 'List<@Nullable String>' but this expression has type 'List<@NonNull String>'\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	return withNulls;\n" +
			"	       ^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required 'List<@NonNull String>' but this expression has type 'List<@Nullable String>'\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\n" +
			"	List<@NonNull String> l1 = withNulls;\n" +
			"	                           ^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required 'List<@NonNull String>' but this expression has type 'List<@Nullable String>'\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 12)\n" +
			"	List<@Nullable String> l2 = noNulls;\n" +
			"	                            ^^^^^^^\n" +
			"Null type mismatch (type annotations): required 'List<@Nullable String>' but this expression has type 'List<@NonNull String>'\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 15)\n" +
			"	assigns(withNulls, noNulls);\n" +
			"	        ^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required 'List<@NonNull String>' but this expression has type 'List<@Nullable String>'\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 15)\n" +
			"	assigns(withNulls, noNulls);\n" +
			"	                   ^^^^^^^\n" +
			"Null type mismatch (type annotations): required 'List<@Nullable String>' but this expression has type 'List<@NonNull String>'\n" +
			"----------\n");
	}

	// types with null annotations on details (array content) are incompatible to opposite types
	public void testCompatibility4a() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n"
				+ "public class X {\n"
				+ "	@Nullable String[] return1(@NonNull String[] noNulls) {\n"
				+ "		return noNulls;\n"
				+ "	}\n"
				+ "	@NonNull String[] return2(@Nullable String[] withNulls) {\n"
				+ "		return withNulls;\n"
				+ "	}\n"
				+ "	void assigns(@NonNull String[] noNulls, @Nullable String[] withNulls) {\n"
				+ "		@NonNull String[] l1 = withNulls;\n"
				+ "		@Nullable String[] l2 = noNulls;\n"
				+ "	}\n"
				+ "	void arguments(@NonNull String[] noNulls, @Nullable String[] withNulls) {\n"
				+ "		assigns(withNulls, noNulls);\n"
				+ "	}\n"
				+ "}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	return noNulls;\n" +
			"	       ^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@Nullable String []\' but this expression has type \'@NonNull String []\'\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	return withNulls;\n" +
			"	       ^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull String []\' but this expression has type \'@Nullable String []\'\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	@NonNull String[] l1 = withNulls;\n" +
			"	                       ^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull String []\' but this expression has type \'@Nullable String []\'\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 11)\n" +
			"	@Nullable String[] l2 = noNulls;\n" +
			"	                        ^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@Nullable String []\' but this expression has type \'@NonNull String []\'\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 14)\n" +
			"	assigns(withNulls, noNulls);\n" +
			"	        ^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull String []\' but this expression has type \'@Nullable String []\'\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 14)\n" +
			"	assigns(withNulls, noNulls);\n" +
			"	                   ^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@Nullable String []\' but this expression has type \'@NonNull String []\'\n" +
			"----------\n");
	}

	// challenge parameterized type with partial substitution of super's type parameters
	public void testCompatibility5() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"abstract public class X<Y> implements Map<@NonNull String,Y> {\n" +
				"	void foo(X<Object> x) {\n" +
				"		Map<@NonNull String, Object> m1 = x; // OK\n" +
				"		Map<@Nullable String, Object> m2 = x; // NOK\n" +
				"	}\n" +
				"}"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	Map<@Nullable String, Object> m2 = x; // NOK\n" +
			"	                                   ^\n" +
			"Null type mismatch (type annotations): required \'Map<@Nullable String,Object>\' but this expression has type \'X<Object>\', corresponding supertype is \'Map<@NonNull String,Object>\'\n" +
			"----------\n");
	}

	// challenge parameterized type with partial substitution of super's type parameters
	public void testCompatibility6() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"abstract public class X<@Nullable Y> implements Map<@Nullable String,Y> {\n" +
				"	void foo(X<@Nullable Object> x) {\n" +
				"		Map<@Nullable String, @Nullable Object> m1 = x; // OK\n" +
				"		Map<@Nullable String, @NonNull Object> m2 = x; // NOK\n" +
				"	}\n" +
				"}"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	Map<@Nullable String, @NonNull Object> m2 = x; // NOK\n" +
			"	                                            ^\n" +
			"Null type mismatch (type annotations): required \'Map<@Nullable String,@NonNull Object>\' but this expression has type \'X<@Nullable Object>\', corresponding supertype is \'Map<@Nullable String,@Nullable Object>\'\n" +
			"----------\n");
	}

	// illegal for type declaration
	public void testUnsupportedLocation01() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public @NonNull class X {}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public @NonNull class X {}\n" +
			"	       ^^^^^^^^\n" +
			"The nullness annotation \'NonNull\' is not applicable at this location\n" +
			"----------\n");
	}

	// illegal for enclosing class (locations: field, argument, return type, local
	public void testUnsupportedLocation02() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X {\n" +
				"    class Inner {}\n" +
				"    @NonNull X.Inner f;\n" +
				"    @NonNull X.Inner foo(@NonNull X.Inner arg) {\n" +
				"        @NonNull X.Inner local = arg;\n" +
				"        return local;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	@NonNull X.Inner f;\n" +
			"	^^^^^^^^\n" +
			"The nullness annotation \'NonNull\' is not applicable at this location, it must be placed directly before the nested type name.\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	@NonNull X.Inner foo(@NonNull X.Inner arg) {\n" +
			"	^^^^^^^^\n" +
			"The nullness annotation \'NonNull\' is not applicable at this location, it must be placed directly before the nested type name.\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	@NonNull X.Inner foo(@NonNull X.Inner arg) {\n" +
			"	                     ^^^^^^^^\n" +
			"The nullness annotation \'NonNull\' is not applicable at this location, it must be placed directly before the nested type name.\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 6)\n" +
			"	@NonNull X.Inner local = arg;\n" +
			"	^^^^^^^^\n" +
			"The nullness annotation \'NonNull\' is not applicable at this location, it must be placed directly before the nested type name.\n" +
			"----------\n");
	}

	// illegal / unchecked for cast & instanceof with scalar type
	public void testUnsupportedLocation03() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X {\n" +
				"    @NonNull X foo(X arg) {\n" +
				"        if (!(arg instanceof @NonNull X))\n" +
				"			return (@NonNull X)arg;\n" +
				"        return arg;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	if (!(arg instanceof @NonNull X))\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The expression of type X is already an instance of type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	if (!(arg instanceof @NonNull X))\n" +
			"	                     ^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n" +
			"3. WARNING in X.java (at line 5)\n" +
			"	return (@NonNull X)arg;\n" +
			"	       ^^^^^^^^^^^^^^^\n" +
			"Null type safety: Unchecked cast from X to @NonNull X\n" +
			"----------\n");
	}

	// illegal / unchecked for cast & instanceof with complex type
	public void testUnsupportedLocation04() {
		runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import java.util.*;\n" +
				"public class X {\n" +
				"    List<@NonNull X> parameterized(List<X> arg) {\n" +
				"        if (!(arg instanceof List<@NonNull X>))\n" +
				"			return (java.util.List<@NonNull X>)arg;\n" +
				"        return arg;\n" +
				"    }\n" +
				"    X @NonNull[] arrays(X[] arg) {\n" +
				"        if (!(arg instanceof X @NonNull[]))\n" +
				"			return (p.X @NonNull[])arg;\n" +
				"        return arg;\n" +
				"    }\n" +
				"	ArrayList<@NonNull String> foo(List<@NonNull String> l) {\n" +
				"		return (ArrayList<@NonNull String>) l;\n" + // OK
				"	}" +
				"	ArrayList<@NonNull String> foo2(List<@NonNull String> l) {\n" +
				"		return (ArrayList<String>) l;\n" + // warn, TODO(stephan) with flow analysis (bug 415292) we might recover the original @NonNull...
				"	}" +
				"}\n"
			},
			((this.complianceLevel >= ClassFileConstants.JDK16) ?
					"----------\n" +
					"1. WARNING in p\\X.java (at line 6)\n" +
					"	if (!(arg instanceof List<@NonNull X>))\n" +
					"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"The expression of type List<X> is already an instance of type List<X>\n"
					:
						"----------\n" +
						"1. ERROR in p\\X.java (at line 6)\n" +
						"	if (!(arg instanceof List<@NonNull X>))\n" +
						"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
						"Cannot perform instanceof check against parameterized type List<X>. Use the form List<?> instead since further generic type information will be erased at runtime\n"
					) +

			"----------\n" +
			"2. ERROR in p\\X.java (at line 6)\n" +
			"	if (!(arg instanceof List<@NonNull X>))\n" +
			"	                     ^^^^^^^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n" +
			"3. WARNING in p\\X.java (at line 7)\n" +
			"	return (java.util.List<@NonNull X>)arg;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety: Unchecked cast from List<X> to List<@NonNull X>\n" +
			"----------\n" +
			"4. WARNING in p\\X.java (at line 11)\n" +
			"	if (!(arg instanceof X @NonNull[]))\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The expression of type X[] is already an instance of type X[]\n" +
			"----------\n" +
			"5. ERROR in p\\X.java (at line 11)\n" +
			"	if (!(arg instanceof X @NonNull[]))\n" +
			"	                     ^^^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n" +
			"6. WARNING in p\\X.java (at line 12)\n" +
			"	return (p.X @NonNull[])arg;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety: Unchecked cast from X[] to X @NonNull[]\n" +
			"----------\n" +
			"7. WARNING in p\\X.java (at line 18)\n" +
			"	return (ArrayList<String>) l;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'ArrayList<@NonNull String>\'\n" +
			"----------\n",
			this.LIBS,
			true/*flush*/);
	}

	// illegal instanceof check with annotated type argument
	public void testUnsupportedLocation04a() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import java.util.*;\n" +
				"public class X {\n" +
				"	boolean instanceOf2(Object o) {\n" +
				"		return o instanceof List<@Nullable ?>;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	return o instanceof List<@Nullable ?>;\n" +
			"	                    ^^^^^^^^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n");
	}

	// illegal for allocation expression
	public void testUnsupportedLocation05() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X {\n" +
				"	X x = new @NonNull X();\n" +
				"	class Inner {}\n" +
				"   Inner i = this.new @Nullable Inner();\n" +
				"	java.util.List<@NonNull String> s = new java.util.ArrayList<@NonNull String>();\n" + // OK
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	X x = new @NonNull X();\n" +
			"	          ^^^^^^^^\n" +
			"The nullness annotation \'NonNull\' is not applicable at this location\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	Inner i = this.new @Nullable Inner();\n" +
			"	                   ^^^^^^^^^\n" +
			"The nullness annotation \'Nullable\' is not applicable at this location\n" +
			"----------\n");
	}

	// method receiver
	public void testUnsupportedLocation06() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X {\n" +
				"	void receiver(@Nullable X this, Object o) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	void receiver(@Nullable X this, Object o) {}\n" +
			"	              ^^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n");
	}

	// receiver type in method/constructor reference
	public void testUnsupportedLocation07() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import java.util.function.Supplier;\n" +
				"public class X {\n" +
				"	void consume(Supplier<Object> c) {}\n" +
				"	static Object supply() { return null; }\n" +
				"	void consumeSupplied() {\n" +
				"		consume(@NonNull X::supply);\n" +
				"		consume(@NonNull X::new);\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	consume(@NonNull X::supply);\n" +
			"	        ^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	consume(@NonNull X::new);\n" +
			"	        ^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n");
	}

	// exceptions (throws & catch)
	public void testUnsupportedLocation08() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import java.io.*;\n" +
				"public class X {\n" +
				"	void throwsDecl() throws @Nullable IOException {}\n" +
				"	void excParam() {\n" +
				"		try {\n" +
				"			throwsDecl();\n" +
				"		} catch (@NonNull IOException ioe) {}\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	void throwsDecl() throws @Nullable IOException {}\n" +
			"	                         ^^^^^^^^^^^^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	} catch (@NonNull IOException ioe) {}\n" +
			"	                  ^^^^^^^^^^^\n" +
			"Nullness annotations are not applicable at this location \n" +
			"----------\n");
	}

	public void testForeach() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import java.util.*;\n" +
				"public class X {\n" +
				"	void foo(List<@NonNull String> nns) {\n" +
				"		for(String s1 : nns) {\n" +
				"			logMsg(s1);\n" +
				"		}\n" +
				"		for(String s2 : getStrings()) {\n" +
				"			logMsg(s2);\n" +
				"		}\n" +
				"	}\n" +
				"	Collection<@Nullable String> getStrings() { return null; }\n" +
				"	void logMsg(@NonNull String msg) { }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	logMsg(s2);\n" +
			"	       ^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is inferred as @Nullable\n" +
			"----------\n");
	}

	// poly-null method
	public void testNullTypeInference1() {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, CompilerOptions.IGNORE);
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import java.util.*;\n" +
				"public class X {\n" +
				"	<T> List<T> polyNullMethod(List<T> in) { return in; }\n" +
				"	@NonNull String test1(List<@NonNull String> strings) {\n" +
				"		 return polyNullMethod(strings).get(0);\n" +
				"	}\n" +
				"	@NonNull String test2(List<@Nullable String> strings) {\n" +
				"		 return polyNullMethod(strings).get(0);\n" +
				"	}\n" +
				"	@Nullable String test3(List<@NonNull String> strings) {\n" +
				"		 return polyNullMethod(strings).get(0);\n" +
				"	}\n" +
				"	@Nullable String test4(List<@Nullable String> strings) {\n" +
				"		 return polyNullMethod(strings).get(0);\n" +
				"	}\n" +
				"}\n"
			},
			compilerOptions,
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	return polyNullMethod(strings).get(0);\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
			"----------\n");
	}

	// functional interface with explicit nullness
	public void testNullTypeInference2a() {
		runNegativeTestWithLibs(
			new String[] {
				"PolyNull.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"interface NNFunc {\n" +
				"	@NonNull String a(@NonNull String i);\n" +
				"}\n" +
				"public class PolyNull {\n" +
				"	@NonNull String extract(NNFunc f, @NonNull String s) { return f.a(s); }\n" +
				"	@NonNull String testOK() {\n" +
				"		return extract(i -> i, \"hallo\");\n" +
				"	}\n" +
				"	@NonNull String testERR() {\n" +
				"		return extract(i -> null, \"hallo\"); // err\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in PolyNull.java (at line 12)\n" +
			"	return extract(i -> null, \"hallo\"); // err\n" +
			"	                    ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n");
	}

	// functional interface with nullness inferred from target type with explicit nullness
	public void testNullTypeInference2b() {
		runNegativeTestWithLibs(
			new String[] {
				"PolyNull.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"interface Func<T>  {\n" +
				"	T a(T i);\n" +
				"}\n" +
				"public class PolyNull {\n" +
				"	@NonNull String extract(Func<@NonNull String> f, @NonNull String s) { return f.a(s); }\n" +
				"	@NonNull String testOK() {\n" +
				"		return extract(i -> i, \"hallo\");\n" +
				"	}\n" +
				"	@NonNull String testERR() {\n" +
				"		return extract(i -> null, \"hallo\"); // err\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in PolyNull.java (at line 12)\n" +
			"	return extract(i -> null, \"hallo\"); // err\n" +
			"	                    ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n");
	}

	// functional interface with unspecified nullness matched against lambda parameter with explicit type & nullness
	public void testNullTypeInference2c() {
		runNegativeTestWithLibs(
			new String[] {
				"PolyNull.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"interface Func<T>  {\n" +
				"	T a(T i);\n" +
				"}\n" +
				"public class PolyNull {\n" +
				"	<X> X extract(Func<X> f, X s) { return f.a(s); }\n" +
				"	@NonNull String testOK() {\n" +
				"		return extract((@NonNull String i) -> i, \"hallo\");\n" +
				"	}\n" +
				"	@NonNull String testERR() {\n" +
				"		return extract((@NonNull String i) -> null, \"hallo\"); // err\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in PolyNull.java (at line 12)\n" +
			"	return extract((@NonNull String i) -> null, \"hallo\"); // err\n" +
			"	                                      ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n");
	}

	// the only null annotation is on the target type, which propagates into the implicitly typed lambda argument
	public void testNullTypeInference2d() {
		runNegativeTestWithLibs(
			new String[] {
				"PolyNull.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"interface Func<T>  {\n" +
				"	T a(T i);\n" +
				"}\n" +
				"public class PolyNull {\n" +
				"	<X> X extract(Func<X> f, X s) { return f.a(s); }\n" +
				"	@NonNull String testOK() {\n" +
				"		return extract(i -> i, \"hallo\");\n" +
				"	}\n" +
				"	@NonNull String testERR() {\n" +
				"		return extract(i -> null, \"hallo\"); // err\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in PolyNull.java (at line 12)\n" +
			"	return extract(i -> null, \"hallo\"); // err\n" +
			"	                    ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n");
	}

	// demonstrate that null annotations from the functional interface win, resulting in successful inference but null-safety issues
	public void testNullTypeInference2e() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"PolyNull.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"interface Func<T>  {\n" +
				"	T a(T i);\n" +
				"}\n" +
				"public class PolyNull {\n" +
				"	String extract(Func<@Nullable String> f, @Nullable String s) { return f.a(s); }\n" +
				"	@NonNull String testWARN() {\n" +
				"		return extract(i -> null, \"hallo\"); // OK to pass null\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in PolyNull.java (at line 9)\n" +
			"	return extract(i -> null, \"hallo\"); // OK to pass null\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'\n" +
			"----------\n");
	}

	// demonstrate that null annotations from the functional interface win, resulting in successful inference but null-safety issues
	public void testNullTypeInference2f() {
		runNegativeTestWithLibs(
			new String[] {
				"PolyNull.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"interface Func<T>  {\n" +
				"	T a(T i);\n" +
				"}\n" +
				"public class PolyNull {\n" +
				"	<X> X extract(Func<@Nullable X> f, @Nullable X s) { return f.a(s); }\n" +
				"	@NonNull String testERR() {\n" +
				"		return extract(i -> needNN(i), \"ola\");\n" +
				"	}\n" +
				"	@NonNull String needNN(@NonNull String s) { return \"\"; }\n" +
				"" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in PolyNull.java (at line 7)\n" +
			"	<X> X extract(Func<@Nullable X> f, @Nullable X s) { return f.a(s); }\n" +
			"	                                                           ^^^^^^\n" +
			"Null type mismatch (type annotations): required \'X\' but this expression has type \'@Nullable X\', where 'X' is a free type variable\n" +
			"----------\n" +
			"2. ERROR in PolyNull.java (at line 9)\n" +
			"	return extract(i -> needNN(i), \"ola\");\n" +
			"	                           ^\n" +
			"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
			"----------\n");
	}

	// seemingly conflicting annotations from type variable application and type variable substitution
	// -> ignore @Nullable which overrides the type variable's nullness for this one location
	public void testNullTypeInference3() {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
		runNegativeTestWithLibs(
			new String[] {
				"Generics.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"public class Generics {\n" +
				"	<X> X m(@Nullable X a) { return null; }\n" +
				"	void test(@NonNull String in) {\n" +
				"		@NonNull String s = m(in);\n" +  // inferred OK as 'm(@Nullable String) -> @NonNull String'
				"		System.out.println(s.toLowerCase());\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new Generics().test(\"hallo\");\n" +
				"	}\n" +
				"}\n"
			},
			compilerOptions,
			"----------\n" +
			"1. ERROR in Generics.java (at line 4)\n" +
			"	<X> X m(@Nullable X a) { return null; }\n" +
			"	                                ^^^^\n" +
			"Null type mismatch (type annotations): \'null\' is not compatible to the free type variable 'X'\n" +
			"----------\n");
	}

	// conflicting annotations from type variable application and type variable substitution -> exclude null annotations from inference
	public void testNullTypeInference3b() {
		runNegativeTestWithLibs(
			new String[] {
				"Generics.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"public class Generics {\n" +
				"	<X> @Nullable X m1(@Nullable X a) { return null; }\n" +
				"	<X> @Nullable X m2(X a) { return null; }\n" +
				"	void test(@NonNull String in) {\n" +
				"		@NonNull String s1 = m1(in);\n" +
				"		@NonNull String s2 = m2(in);\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new Generics().test(\"hallo\");\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in Generics.java (at line 7)\n" +
			"	@NonNull String s1 = m1(in);\n" +
			"	                     ^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
			"----------\n" +
			"2. ERROR in Generics.java (at line 8)\n" +
			"	@NonNull String s2 = m2(in);\n" +
			"	                     ^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
			"----------\n");
	}

	// conflicting annotations from type variable application and type variable substitution
	public void testNullTypeInference3c() {
		runNegativeTestWithLibs(
			new String[] {
				"Generics.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import java.util.*;\n" +
				"\n" +
				"interface Function<I,O> { }\n" +
				"abstract class MyFunc implements Function<@NonNull Object, @Nullable String> { }\n" +
				"  \n" +
				"public class Generics {\n" +
				"  <@NonNull I,@Nullable O> \n" +
				"  Collection<O> map1(Collection<I> in, Function<I, O> f) { return null; }\n" +
				"  <@Nullable I,@NonNull O> \n" +
				"  Collection<O> map2(Collection<I> in, Function<I, O> f) { return null; }\n" +
				"	void test(@NonNull List<Object> inList, MyFunc f) {\n" +
				"		Collection<@Nullable String> result = map1(inList, f);\n" +
				"		map2(inList, f);\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in Generics.java (at line 13)\n" +
			"	Collection<@Nullable String> result = map1(inList, f);\n" +
			"	                                           ^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'@NonNull List<Object>\' needs unchecked conversion to conform to \'Collection<@NonNull Object>\', corresponding supertype is 'Collection<Object>'\n" +
			"----------\n" +
			"2. WARNING in Generics.java (at line 14)\n" +
			"	map2(inList, f);\n" +
			"	     ^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'@NonNull List<Object>\' needs unchecked conversion to conform to \'Collection<@Nullable Object>\', corresponding supertype is 'Collection<Object>'\n" +
			"----------\n" +
			"3. ERROR in Generics.java (at line 14)\n" +
			"	map2(inList, f);\n" +
			"	             ^\n" +
			"Null type mismatch (type annotations): required \'Function<@Nullable Object,@NonNull String>\' but this expression has type \'MyFunc\', corresponding supertype is \'Function<@NonNull Object,@Nullable String>\'\n" +
			"----------\n");
	}

	// missing return type should not cause NPE
	public void testBug415850_01() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X {\n" +
				"	@NonNull foo() {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	@NonNull foo() {}\n" +
			"	         ^^^^^\n" +
			"Return type for the method is missing\n" +
			"----------\n",
			this.LIBS,
			true/*flush*/);
	}

	// enum constant inside raw type: initialization must be recognized as conform to the implicitly @NonNull declaration
	public void testBug415850_02(){
		runConformTestWithLibs(
			new String[] {
				"Callable.java",
				"interface Callable<T> {\n" +
				"	public enum Result {\n" +
				"		GOOD, BAD\n" +
				"	};\n" +
				"	public Result call(T arg);\n" +
				"}\n"
			},
			getCompilerOptions(),
			"");
	}

	// when mapping 1st parameter to method receiver, avoid AIOOBE in ReferenceExpression#resolveType(..)
	public void testBug415850_03() throws Exception {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, JavaCore.IGNORE);
		runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("deprecation");
		runner.classLibraries = this.LIBS;
		runner.testFiles =
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.util.Date;\n" +
				"import static java.lang.annotation.ElementType.*; \n" +
				"@Target(TYPE_USE)\n" +
				"@interface Vernal {}\n" +
				"interface I {\n" +
				"	int f(Date d);\n" +
				"}\n" +
				"class X {\n" +
				"	static void monitorTemperature(Object myObject) {\n" +
				"		I i = @Vernal Date::getDay;\n" +
				"	}\n" +
				"}\n",
			};
		runner.runConformTest();
	}

	// ensure annotation type has super types connected, to avoid NPE in ImplicitNullAnnotationVerifier.collectOverriddenMethods(..)
	public void testBug415850_04() throws Exception {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"public class X implements @B @C('i') J { }",
				"B.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(CLASS)\n" +
				"@interface B {\n" +
				"	int value() default -1;\n" +
				"}",
				"C.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"@Target(TYPE_USE)\n" +
				"@Retention(RUNTIME)\n" +
				"@interface C {\n" +
				"	char value() default '-';\n" +
				"}\n",
				"J.java",
				"interface J {}\n"
			},
			getCompilerOptions(),
			"");
	}

	// don't let type annotations on array dimensions spoil type compatibility
	public void testBug415850_05() {
		runNegativeTest(
			new String[]{
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"public class X {\n" +
				"	public void foo() {\n" +
				"		int @Marker [][][] i = new @Marker int @Marker [2] @Marker [@Marker bar()] @Marker [];\n" +
				"	}\n" +
				"	public int bar() {\n" +
				"		return 2;\n" +
				"	}\n" +
				"}\n" +
				"@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	int @Marker [][][] i = new @Marker int @Marker [2] @Marker [@Marker bar()] @Marker [];\n" +
			"	                                                            ^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n",
			this.LIBS,
			true/*flush*/);
	}

	// don't let type annotations on array dimensions spoil type compatibility
	// case without any error
	public void testBug415850_06() {
		runConformTestWithLibs(
			new String[]{
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"public class X {\n" +
				"	public void foo() {\n" +
				"		int @Marker [][][] i = new @Marker int @Marker [2] @Marker [bar()] @Marker [];\n" +
				"	}\n" +
				"	public int bar() {\n" +
				"		return 2;\n" +
				"	}\n" +
				"}\n" +
				"@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n"
			},
			getCompilerOptions(),
			"");
	}

	public void testBug416172() {
        runNegativeTestWithLibs(
            new String[] {
                "X.java",
                "import org.eclipse.jdt.annotation.NonNull;\n" +
                "\n" +
                "public class X {\n" +
                "   class Y {}\n" +
                "   X.@NonNull Y  foo(X.@NonNull Y xy) {\n" +
                "       return new X().new Y();\n" +
                "   }\n" +
                "}\n" +
                "\n" +
                "class Z extends X {\n" +
                "   @Override\n" +
                "   X.@NonNull Y  foo(X.Y xy) {\n" +
                "       return null;\n" +
                "   }\n" +
                "}\n"
            },
            getCompilerOptions(),
            "----------\n" +
    		"1. WARNING in X.java (at line 12)\n" +
    		"	X.@NonNull Y  foo(X.Y xy) {\n" +
    		"	                  ^^^\n" +
    		"Missing non-null annotation: inherited method from X specifies this parameter as @NonNull\n" +
    		"----------\n" +
    		"2. ERROR in X.java (at line 13)\n" +
    		"	return null;\n" +
    		"	       ^^^^\n" +
    		"Null type mismatch: required \'X.@NonNull Y\' but the provided value is null\n" +
    		"----------\n");
    }

	// incompatible null constraints on parameters
	public void testBug416174() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_NONNULL_PARAMETER_ANNOTATION_DROPPED, JavaCore.IGNORE);
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"public class X {\n" +
				"	void  foo1(List<X> lx) {}\n" +
				"	void  foo2(List<@NonNull X> lx) {}\n" +
				"	void  foo3(List<@Nullable X> lx) {}\n" +
				"	void  foo4(@NonNull List<@Nullable X> lx) {}\n" +
				"}\n" +
				"\n" +
				"class Z extends X {\n" +
				"	@Override void foo1(List<@NonNull X> xy) {}\n" +
				"	@Override void foo2(List<X> lx) {}\n" +
				"	@Override void foo3(List<X> lx) {}\n" +
				"	@Override void foo4(List<@Nullable X> lx) {}\n" + // omitting annotation at toplevel can be tolerated (via option)
				"}\n"
			},
			options,
			"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	@Override void foo1(List<@NonNull X> xy) {}\n" +
			"	                    ^^^^\n" +
			"Illegal redefinition of parameter xy, inherited method from X declares this parameter as \'List<X>\' (mismatching null constraints)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 14)\n" +
			"	@Override void foo2(List<X> lx) {}\n" +
			"	                    ^^^^\n" +
			"Illegal redefinition of parameter lx, inherited method from X declares this parameter as \'List<@NonNull X>\' (mismatching null constraints)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 15)\n" +
			"	@Override void foo3(List<X> lx) {}\n" +
			"	                    ^^^^\n" +
			"Illegal redefinition of parameter lx, inherited method from X declares this parameter as \'List<@Nullable X>\' (mismatching null constraints)\n" +
			"----------\n");
	}

	// incompatibility at return type, which should be shown here in the error message
	public void testBug416174b() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"public abstract class X {\n" +
				"	List<X> foo1() {\n" +
				"		return null;\n" +
				"	}\n" +
				"	List<@Nullable X> foo2() {\n" +
				"		return null;\n" +
				"	}\n" +
				"	abstract @NonNull List<@NonNull X> foo3();\n" +
				"	List<@Nullable X> foo4() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"abstract class Z extends X {\n" +
				"	@Override\n" +
				"	List<@NonNull X> foo1() {\n" +
				"		return null;\n" +
				"	}\n" +
				"	@Override\n" +
				"	List<@NonNull X> foo2() {\n" +
				"		return null;\n" +
				"	}\n" +
				"	@Override\n" +
				"	@NonNull List<X> foo3() {\n" +
				"		return new ArrayList<>();\n" +
				"	}\n" +
				"	@Override\n" +
				"	@NonNull List<@Nullable X> foo4() {\n" + // OK
				"		return new ArrayList<>();\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 20)\n" +
			"	List<@NonNull X> foo1() {\n" +
			"	^^^^\n" +
			"The return type is incompatible with \'List<X>\' returned from X.foo1() (mismatching null constraints)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 24)\n" +
			"	List<@NonNull X> foo2() {\n" +
			"	^^^^\n" +
			"The return type is incompatible with \'List<@Nullable X>\' returned from X.foo2() (mismatching null constraints)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 28)\n" +
			"	@NonNull List<X> foo3() {\n" +
			"	         ^^^^\n" +
			"The return type is incompatible with \'@NonNull List<@NonNull X>\' returned from X.foo3() (mismatching null constraints)\n" +
			"----------\n");
	}

	// overriding an unconstrained return with nullable
	public void testNullableReturn() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"public abstract class X {\n" +
				"	X foo1() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"abstract class Z extends X {\n" +
				"	@Override\n" +
				"	@Nullable X foo1() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"");
	}

	public void testBug416175() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<@NonNull ? extends @NonNull String> ls = new ArrayList<String>();\n" +
				"		ls.add(null);\n" +
				"		@NonNull String s = ls.get(0);\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	List<@NonNull ? extends @NonNull String> ls = new ArrayList<String>();\n" +
			"	                                              ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'@NonNull ArrayList<String>\' needs unchecked conversion to conform to \'List<@NonNull ? extends @NonNull String>\', corresponding supertype is 'List<String>'\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	ls.add(null);\n" +
			"	       ^^^^\n" +
			"Null type mismatch: required \'@NonNull ? extends @NonNull String\' but the provided value is null\n" +
			"----------\n" +
			"3. INFO in X.java (at line 10)\n" +
			"	@NonNull String s = ls.get(0);\n" +
			"	                    ^^^^^^^^^\n" +
			"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<@NonNull capture#of ? extends @NonNull String>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind\n" +
			"----------\n");
	}

	// original test (was throwing stack overflow)
	public void testBug416176() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public class X<@NonNull T> {\n" +
				"	T foo(T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"");
	}

	// variant to challenge merging of annotation on type variable and its use
	public void testBug416176a() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"public class X<@NonNull T> {\n" +
				"	T foo(T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	@NonNull T bar1(@NonNull T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	@NonNull T bar2(@Nullable T t) { // argument: no contradiction (1)\n" +
				"		return t; // mismatch (1)\n" +
				"	}\n" +
				"	@Nullable T bar3(T t) { // return type: no contradiction (2)\n" +
				"		@Nullable T l = t; // local: no contradiction (3)\n" +
				"		return l;\n" +
				"	}\n" +
				"	class Inner {\n" +
				"		@Nullable T f; // field: no contradiction (4)\n" +
				"	}\n" +
				"	T bar3() {\n" +
				"		return null; // mismatch (2)\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	return t; // mismatch (1)\n" +
			"	       ^\n" +
			"Null type mismatch (type annotations): required \'@NonNull T\' but this expression has type \'@Nullable T\'\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 22)\n" +
			"	return null; // mismatch (2)\n" +
			"	       ^^^^\n" +
			"Null type mismatch: required \'@NonNull T\' but the provided value is null\n" +
			"----------\n");
	}

	// variant to challenge duplicate methods, though with different parameter annotations
	public void testBug416176b() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"public class X<T> {\n" +
				"	@NonNull T bar(@NonNull T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	@NonNull T bar(@Nullable T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	@NonNull T bar(@NonNull T t) {\n" +
			"	           ^^^^^^^^^^^^^^^^^\n" +
			"Duplicate method bar(T) in type X<T>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	@NonNull T bar(@Nullable T t) {\n" +
			"	           ^^^^^^^^^^^^^^^^^^\n" +
			"Duplicate method bar(T) in type X<T>\n" +
			"----------\n",
			this.LIBS,
			true/*flush*/);
	}

	public void testBug416180() {
		runWarningTestWithLibs(
			true,
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public class X<T> {\n" +
				"	T foo(T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	\n" +
				"	public static void main(String[] args) {\n" +
				"		X<String> x = new Y();\n" +
				"	}\n" +
				"} \n" +
				"\n" +
				"class Y extends X<@NonNull String> {\n" +
				"   @Override\n" +
				"	@NonNull String foo(java.lang.@NonNull String t) {\n" +
				"		return \"\";\n" +
				"	};\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. INFO in X.java (at line 9)\n" +
			"	X<String> x = new Y();\n" +
			"	              ^^^^^^^\n" +
			"Unsafe null type conversion (type annotations): The value of type '@NonNull Y' is made accessible using the less-annotated type 'X<String>', corresponding supertype is 'X<@NonNull String>'\n" +
			"----------\n");
	}

	public void testBug416181() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public class X<T> {\n" +
				"	class Y {\n" +
				"		\n" +
				"	}\n" +
				"	\n" +
				"	X<String>.@NonNull Y y = null; // 1st error here.\n" +
				"	\n" +
				"	@NonNull Y y2 = null; // 2nd error here.\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	X<String>.@NonNull Y y = null; // 1st error here.\n" +
			"	                         ^^^^\n" +
			"Null type mismatch: required \'X<String>.@NonNull Y\' but the provided value is null\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	@NonNull Y y2 = null; // 2nd error here.\n" +
			"	                ^^^^\n" +
			"Null type mismatch: required \'X<T>.@NonNull Y\' but the provided value is null\n" +
			"----------\n");
	}

	public void testBug416182() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"public class X<T> {\n" +
				"	T foo(@NonNull T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X<@Nullable String> xs = new X<String>();\n" +
				"		xs.foo(null);\n" +
				"	}\n" +
				"	\n" +
				"	public void test(X<String> x) {\n" +
				"		X<@Nullable String> xs = x;\n" +
				"		xs.bar(null);\n" +
				"	}\n" +
				"	public void bar(T t) {}\n" +
				"\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 9)\n" +
			"	X<@Nullable String> xs = new X<String>();\n" +
			"	                         ^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'@NonNull X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	xs.foo(null);\n" +
			"	       ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 14)\n" +
			"	X<@Nullable String> xs = x;\n" +
			"	                         ^\n" +
			"Null type safety (type annotations): The expression of type \'X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'\n" +
			"----------\n");
	}

	// introduce unrelated method lookup before the bogus one
	public void testBug416182a() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"public class X<T> {\n" +
				"	T foo(@NonNull T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	void foo() {}\n" +
				"	public static void main(String[] args) {\n" +
				"		X<@Nullable String> xs = new X<String>();\n" +
				"		xs.foo();\n" +
				"		xs.foo(null);\n" +
				"	}\n" +
				"	\n" +
				"	public void test(X<String> x) {\n" +
				"		X<@Nullable String> xs = x;\n" +
				"		xs.bar(null);\n" +
				"	}\n" +
				"	public void bar(T t) {}\n" +
				"\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 10)\n" +
			"	X<@Nullable String> xs = new X<String>();\n" +
			"	                         ^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'@NonNull X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 12)\n" +
			"	xs.foo(null);\n" +
			"	       ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 16)\n" +
			"	X<@Nullable String> xs = x;\n" +
			"	                         ^\n" +
			"Null type safety (type annotations): The expression of type \'X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'\n" +
			"----------\n");
	}

	// avoid extra warning by use of diamond.
	public void testBug416182b() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"public class X<T> {\n" +
				"	T foo(@NonNull T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X<@Nullable String> xs = new X<>();\n" +
				"		xs.foo(null);\n" +
				"	}\n" +
				"	\n" +
				"	public void test(X<String> x) {\n" +
				"		X<@Nullable String> xs = x;\n" +
				"		xs.bar(null);\n" +
				"	}\n" +
				"	public void bar(T t) {}\n" +
				"\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	xs.foo(null);\n" +
			"	       ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 14)\n" +
			"	X<@Nullable String> xs = x;\n" +
			"	                         ^\n" +
			"Null type safety (type annotations): The expression of type \'X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'\n" +
			"----------\n");
	}

	public void testBug416183() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public class X<T> {\n" +
				"	T foo(@NonNull T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X<String> xs = new X<String>();\n" +
				"		xs.foo(\"\");\n" +
				"	}\n" +
				"	\n" +
				"}\n"
			},
			getCompilerOptions(),
			"");
	}
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=417113#c25, point 4.
	public void testSubstitution() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import java.util.List;\n" +
				"import java.util.ArrayList;\n" +
				"public class X<T> {\n" +
				"	T foo(@NonNull List<@NonNull T> l) {\n" +
				"		return l.get(0);\n" +
				"	}	\n" +
				"	public static void main(String[] args) {\n" +
				"		X<String> s = new X<>();\n" +
				"		s.foo(new ArrayList<String>()); // (1)\n" +
				"		s.foo(null); // (2)\n" +
				"	}\n" +
				"}\n"

			},
			getCompilerOptions(),
			"----------\n" +
			"1. INFO in X.java (at line 6)\n" +
			"	return l.get(0);\n" +
			"	       ^^^^^^^^\n" +
			"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull List<@NonNull T>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 10)\n" +
			"	s.foo(new ArrayList<String>()); // (1)\n" +
			"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'@NonNull ArrayList<String>\' needs unchecked conversion to conform to \'@NonNull List<@NonNull String>\', corresponding supertype is 'List<String>'\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\n" +
			"	s.foo(null); // (2)\n" +
			"	      ^^^^\n" +
			"Null type mismatch: required \'@NonNull List<@NonNull String>\' but the provided value is null\n" +
			"----------\n");
	}
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=417113#c25, point 4.
	public void testSubstitution2() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"public class X<T> {\n" +
				"	T foo(@NonNull T @NonNull [] l) {\n" +
				"		return l[0];\n" +
				"	}	\n" +
				"	public static void main(String[] args) {\n" +
				"		X<String> s = new X<>();\n" +
				"       s.foo(new String [] { null });\n" +
				"       s.foo(new String @Nullable [] { null });\n" +
				"       s.foo(new String @NonNull [] { null });\n" +
				"       s.foo(new @Nullable String @NonNull [] { null });\n" +
				"       s.foo(new @NonNull String @NonNull [] { \"\" });\n" +
				"		s.foo(null); // (2)\n" +
				"	}\n" +
				"}\n"

			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 9)\n" +
			"	s.foo(new String [] { null });\n" +
			"	      ^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String @NonNull[]\'\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 10)\n" +
			"	s.foo(new String @Nullable [] { null });\n" +
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String @Nullable[]\' needs unchecked conversion to conform to \'@NonNull String @NonNull[]\'\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 11)\n" +
			"	s.foo(new String @NonNull [] { null });\n" +
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String @NonNull[]\' needs unchecked conversion to conform to \'@NonNull String @NonNull[]\'\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 12)\n" +
			"	s.foo(new @Nullable String @NonNull [] { null });\n" +
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull String @NonNull[]\' but this expression has type \'@Nullable String @NonNull[]\'\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 14)\n" +
			"	s.foo(null); // (2)\n" +
			"	      ^^^^\n" +
			"Null type mismatch: required \'@NonNull String @NonNull[]\' but the provided value is null\n" +
			"----------\n");
	}
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=417113#c25, point 4.
	public void testSubstitution3() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"public class X<T> {\n" +
				"	T foo(@NonNull T l) {\n" +
				"		return l;\n" +
				"	}	\n" +
				"	public static void main(String[] args) {\n" +
				"		X<String> s = new X<>();\n" +
				"       s.foo(null);\n" +
				"	}\n" +
				"}\n"

			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	s.foo(null);\n" +
			"	      ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n");
	}
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=417113#c25, point 4.
	public void testSubstitution4() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface TypeAnnotation {\n" +
				"}\n" +
				"public class X<T> {\n" +
				"    class Y {}\n" +
				"    void foo(@TypeAnnotation X<T>.@NonNull Y l) {\n" +
				"    }	\n" +
				"    public static void main(String[] args) {\n" +
				"        X<String> s = new X<>();\n" +
				"        s.foo(null);\n" +
				"    }\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	s.foo(null);\n" +
			"	      ^^^^\n" +
			"Null type mismatch: required \'X<String>.@NonNull Y\' but the provided value is null\n" +
			"----------\n");
	}
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=417113#c25, point 4.
	public void testSubstitution5() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"public class X<T> {\n" +
				"    void foo(@NonNull X<@NonNull ? extends T> p) {\n" +
				"    }	\n" +
				"    public static void main(String[] args) {\n" +
				"        X<String> s = new X<>();\n" +
				"        X<@NonNull String> s2 = new X<@NonNull String>();\n" +
				"        s.foo(s);\n" +
				"        s.foo(s2);\n" +
				"    }\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	s.foo(s);\n" +
			"	      ^\n" +
			"Null type safety (type annotations): The expression of type \'X<String>\' needs unchecked conversion to conform to \'@NonNull X<@NonNull ? extends String>\'\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/417758 - [1.8][null] Null safety compromise during array creation.
	// original test case
	public void testArray1() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public class X<T> {\n" +
				"   \n" +
				"	public static void main(String[] args) {\n" +
				"		@NonNull String @NonNull [] s = new @NonNull String [] { null };\n" +
				"		if (s != null && s[0] != null) {\n" +
				"			System.out.println(\"Not null\");\n" +
				"		}\n" +
				"		System.out.println(\"Length = \" + s[0].length());\n" +
				"	}\n" +
				"}"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	@NonNull String @NonNull [] s = new @NonNull String [] { null };\n" +
			"	                                                         ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	if (s != null && s[0] != null) {\n" +
			"	    ^\n" +
			"Redundant null check: comparing \'@NonNull String @NonNull[]\' against null\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	if (s != null && s[0] != null) {\n" +
			"	                 ^^^^\n" +
			"Redundant null check: comparing \'@NonNull String\' against null\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/417758 - [1.8][null] Null safety compromise during array creation.
	// two-dim array with annotations on dimensions
	public void testArray2() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public class X<T> {\n" +
				"   \n" +
				"	public static void main(String[] args) {\n" +
				"		@NonNull String @NonNull [][] s1 = new @NonNull String @NonNull [][] { null, { null} }; // problem at inner null\n" +
				"		@NonNull String @NonNull [][] s2 = new @NonNull String [] @NonNull [] { null, { null} }; // problem at both nulls\n" +
				"	}\n" +
				"}"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	@NonNull String @NonNull [][] s1 = new @NonNull String @NonNull [][] { null, { null} }; // problem at inner null\n" +
			"	                                                                               ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n" +
			"2. INFO in X.java (at line 7)\n" +
			"	@NonNull String @NonNull [][] s2 = new @NonNull String [] @NonNull [] { null, { null} }; // problem at both nulls\n" +
			"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Unsafe null type conversion (type annotations): The value of type \'@NonNull String [] @NonNull[]\' is made accessible using the less-annotated type \'@NonNull String @NonNull[] []\'\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	@NonNull String @NonNull [][] s2 = new @NonNull String [] @NonNull [] { null, { null} }; // problem at both nulls\n" +
			"	                                                                        ^^^^\n" +
			"Null type mismatch: required \'@NonNull String @NonNull[]\' but the provided value is null\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	@NonNull String @NonNull [][] s2 = new @NonNull String [] @NonNull [] { null, { null} }; // problem at both nulls\n" +
			"	                                                                                ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/417758 - [1.8][null] Null safety compromise during array creation.
	// three-dim array with annotations on dimensions, also assignment has a problem
	public void testArray3() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.WARNING);
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public class X<T> {\n" +
				"   \n" +
				"	public static void main(String[] args) {\n" +
				"		@NonNull String [][] @NonNull [] s = new @NonNull String []@NonNull [][] { null, { {null}, null/*ok*/ } };\n" +
				"	}\n" +
				"}"
			},
			options,
			"----------\n" +
			"1. WARNING in X.java (at line 6)\n" +
			"	@NonNull String [][] @NonNull [] s = new @NonNull String []@NonNull [][] { null, { {null}, null/*ok*/ } };\n" +
			"	                                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Unsafe null type conversion (type annotations): The value of type \'@NonNull String [] @NonNull[] []\' is made accessible using the less-annotated type \'@NonNull String [] [] @NonNull[]\'\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	@NonNull String [][] @NonNull [] s = new @NonNull String []@NonNull [][] { null, { {null}, null/*ok*/ } };\n" +
			"	                                                                           ^^^^\n" +
			"Null type mismatch: required \'@NonNull String @NonNull[] []\' but the provided value is null\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	@NonNull String [][] @NonNull [] s = new @NonNull String []@NonNull [][] { null, { {null}, null/*ok*/ } };\n" +
			"	                                                                                    ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n");
	}

	public void testArray4() {
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.WARNING);
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public class X<T> {\n" +
				"   \n" +
				"	void ok() {\n" +
				"		@NonNull String @NonNull [] s0 = new @NonNull String @NonNull [0];\n" + // array exists, but no contents that could be null
				"		@NonNull String @NonNull[]@NonNull[] s2 = new @NonNull String @NonNull[2]@NonNull[0];\n" + // inner arrays will be null
				"		String []@NonNull[] s4 = new String [getDims()]@NonNull[1];\n" + // leaves are null but that's ok
				"		@NonNull String @NonNull[][] s5 = new @NonNull String @NonNull[5][];\n" + // OK: outer array exists, inner arrays are unannotated
				"	}\n" +
				"	void nok() {\n" +
				"		@NonNull String @NonNull [] s1 = new @NonNull String @NonNull [1];\n" +
				"		@NonNull String @NonNull[]@NonNull[] s2 = new @NonNull String @NonNull[2]@NonNull[];\n" + // inner arrays will be null
				"		@NonNull String @NonNull[][] s3 = new @NonNull String @NonNull[1][3];\n" + // leaf cells will be null
				"		@NonNull String @NonNull[]@NonNull[] s6 = new @NonNull String @NonNull[5]@NonNull[];\n" +
				"	}\n" +
				"	int getDims() { return 1; }\n" +
				"}"
			},
			options,
			"----------\n" +
			"1. INFO in X.java (at line 12)\n" +
			"	@NonNull String @NonNull [] s1 = new @NonNull String @NonNull [1];\n" +
			"	                                                              ^^^\n" +
			"This array dimension with declared element type @NonNull String will be initialized with \'null\' entries\n" +
			"----------\n" +
			"2. INFO in X.java (at line 13)\n" +
			"	@NonNull String @NonNull[]@NonNull[] s2 = new @NonNull String @NonNull[2]@NonNull[];\n" +
			"	                                                                      ^^^\n" +
			"This array dimension with declared element type @NonNull String @NonNull[] will be initialized with \'null\' entries\n" +
			"----------\n" +
			"3. INFO in X.java (at line 14)\n" +
			"	@NonNull String @NonNull[][] s3 = new @NonNull String @NonNull[1][3];\n" +
			"	                                                                 ^^^\n" +
			"This array dimension with declared element type @NonNull String will be initialized with \'null\' entries\n" +
			"----------\n" +
			"4. INFO in X.java (at line 15)\n" +
			"	@NonNull String @NonNull[]@NonNull[] s6 = new @NonNull String @NonNull[5]@NonNull[];\n" +
			"	                                                                      ^^^\n" +
			"This array dimension with declared element type @NonNull String @NonNull[] will be initialized with \'null\' entries\n" +
			"----------\n");
	}

	public void testBug417759() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"public class X<T> {\n" +
				"	void foo(@NonNull X<@NonNull ?> l) { \n" +
				"	}	\n" +
				"	public static void main(String[] args) {\n" +
				"		@NonNull X<String> s = new X<>();\n" +
				"       s.foo(s);  // String vs. @NonNull ?\n" +
				"       @NonNull X<@Nullable String> s2 = new X<>();\n" +
				"		s.foo(s2); // @Nullable String vs. @NonNull ?\n" +
				"       @NonNull X<@NonNull String> s3 = new X<>();\n" +
				"		s.foo(s3); // good\n" +
				"	}\n" +
				"}"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	s.foo(s);  // String vs. @NonNull ?\n" +
			"	      ^\n" +
			"Null type safety (type annotations): The expression of type \'@NonNull X<String>\' needs unchecked conversion to conform to \'@NonNull X<@NonNull ?>\'\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	s.foo(s2); // @Nullable String vs. @NonNull ?\n" +
			"	      ^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull X<@NonNull ?>\' but this expression has type \'@NonNull X<@Nullable String>\'\n" +
			"----------\n");
	}
	public void testTypeVariable1() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface Junk {\n" +
				"}\n" +
				"public class X<@NonNull T> {\n" +
				"	T t = null;\n" +
				"	@Junk T t2 = null;\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	T t = null;\n" +
			"	      ^^^^\n" +
			"Null type mismatch: required \'@NonNull T\' but the provided value is null\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	@Junk T t2 = null;\n" +
			"	             ^^^^\n" +
			"Null type mismatch: required \'@NonNull T\' but the provided value is null\n" +
			"----------\n");
	}
	// free type variable does not ensure @NonNull, but cannot accept null either, unbounded type variable
	public void testTypeVariable2() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X<T> {\n" +
				"	void consumeAny(T t) {\n" +
				"		consume(t); // error, t can be null\n" +
				"		consumeObject(t); // error, t can be null\n" +
				"	}\n" +
				"	void consumeNullable(@Nullable T t) {\n" +
				"		consume(t); // error, both sides explicit, mismatch\n" +
				"		consumeObject(t); // error, both sides explicit, mismatch\n" +
				"	}\n" +
				"	void consume(@NonNull T t) {}\n" +
				"	void consumeObject(@NonNull Object o) {}\n" +
				"	T produce() {\n" +
				"		return null; // error, T may not accept null\n" +
				"	}\n" +
				"	T produceFromNullable(@Nullable T t) {\n" +
				"		return t; // error, T may not accept nullable\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	consume(t); // error, t can be null\n" +
			"	        ^\n" +
			"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	consumeObject(t); // error, t can be null\n" +
			"	              ^\n" +
			"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	consume(t); // error, both sides explicit, mismatch\n" +
			"	        ^\n" +
			"Null type mismatch (type annotations): required \'@NonNull T\' but this expression has type \'@Nullable T\'\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 9)\n" +
			"	consumeObject(t); // error, both sides explicit, mismatch\n" +
			"	              ^\n" +
			"Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable T\'\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 14)\n" +
			"	return null; // error, T may not accept null\n" +
			"	       ^^^^\n" +
			"Null type mismatch (type annotations): \'null\' is not compatible to the free type variable 'T'\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 17)\n" +
			"	return t; // error, T may not accept nullable\n" +
			"	       ^\n" +
			"Null type mismatch (type annotations): required \'T\' but this expression has type \'@Nullable T\', where \'T\' is a free type variable\n" +
			"----------\n");
	}
	// free type variable does not ensure @NonNull, but cannot accept null either, type variable with upper bound
	public void testTypeVariable3() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X<T extends Number> {\n" +
				"	void consumeAny(T t) {\n" +
				"		consume(t); // error, t can be null\n" +
				"		consumeObject(t); // error, t can be null\n" +
				"	}\n" +
				"	void consumeNullable(@Nullable T t) {\n" +
				"		consume(t); // error, both sides explicit, mismatch\n" +
				"		consumeObject(t); // error, both sides explicit, mismatch\n" +
				"	}\n" +
				"	void consume(@NonNull T t) {}\n" +
				"	void consumeObject(@NonNull Object o) {}\n" +
				"	T produce() {\n" +
				"		return null; // error, T may not accept null\n" +
				"	}\n" +
				"	T produceFromNullable(@Nullable T t) {\n" +
				"		return t; // error, T may not accept nullable\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	consume(t); // error, t can be null\n" +
			"	        ^\n" +
			"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	consumeObject(t); // error, t can be null\n" +
			"	              ^\n" +
			"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	consume(t); // error, both sides explicit, mismatch\n" +
			"	        ^\n" +
			"Null type mismatch (type annotations): required \'@NonNull T extends Number\' but this expression has type \'@Nullable T extends Number\'\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 9)\n" +
			"	consumeObject(t); // error, both sides explicit, mismatch\n" +
			"	              ^\n" +
			"Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable T extends Number\'\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 14)\n" +
			"	return null; // error, T may not accept null\n" +
			"	       ^^^^\n" +
			"Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 17)\n" +
			"	return t; // error, T may not accept nullable\n" +
			"	       ^\n" +
			"Null type mismatch (type annotations): required \'T\' but this expression has type \'@Nullable T extends Number\', where \'T\' is a free type variable\n" +
			"----------\n");
	}
	// free type variable is compatible to itself even with different not null-related type annotations
	public void testTypeVariable4() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE) @interface TypeMarker {}\n" +
				"public class X<T> {\n" +
				"	T passThrough1(@TypeMarker T t) {\n" +
				"		return t; // OK\n" +
				"	}\n" +
				"	@TypeMarker T passThrough2(T t) {\n" +
				"		return t; // OK\n" +
				"	}\n" +
				"	@TypeMarker T passThrough3(@Nullable @TypeMarker T t) {\n" +
				"		return t; // Not OK\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	return t; // Not OK\n" +
			"	       ^\n" +
			"Null type mismatch (type annotations): required \'T\' but this expression has type \'@Nullable T\', where 'T' is a free type variable\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/433906
	public void testTypeVariable5() {
		runConformTestWithLibs(
			new String[] {
				"ExFunction.java",
				"@FunctionalInterface\n" +
				"public interface ExFunction<T, R, E extends Exception> {\n" +
				"	R apply(T t1) throws E;\n" +
				"\n" +
				"	default <V>  ExFunction<V, R, E> compose(ExFunction<? super V, ? extends T, E> before) {\n" +
				"		java.util.Objects.requireNonNull(before);\n" +
				"		//warning on before.apply(v):\n" +
				"		//Null type safety (type annotations): The expression of type 'capture#of ? extends T' needs unchecked conversion to conform to 'T'\n" +
				"		return (V v) -> apply(before.apply(v));\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"");
	}
	public void testSE7AnnotationCopy() { // we were dropping annotations here, but null analysis worked already since the tagbits were not "dropped", just the same capturing in a test
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n" +
				"public class X {\n" +
				"	class Y {}\n" +
				"	void foo(@T X.@NonNull Y p) {\n" +
				"		foo(null);\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	foo(null);\n" +
			"	    ^^^^\n" +
			"Null type mismatch: required \'X.@NonNull Y\' but the provided value is null\n" +
			"----------\n");
	}
	public void testWildcardCapture() {
		runWarningTestWithLibs(
			true/*flush*/,
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"       List<X> ax = new ArrayList<X>();\n" +
				"       ax.add(new X());\n" +
				"		List<? extends X> lx = ax;\n" +
				"		getAdd(lx);\n" +
				"	}\n" +
				"	static <@NonNull P>  void getAdd(List<P> lt) {\n" +
				"		lt.add(lt.get(0));\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 16)\n" +
			"	getAdd(lx);\n" +
			"	       ^^\n" +
			"Null type safety (type annotations): The expression of type \'List<capture#of ? extends X>\' needs unchecked conversion to conform to \'List<@NonNull capture#of ? extends X>\'\n" +
			"----------\n");
	}
	public void testWildcardCapture2() {
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"       List<@NonNull X> ax = new ArrayList<@NonNull X>();\n" +
				"       ax.add(new X());\n" +
				"		List<@NonNull ? extends X> lx = ax;\n" +
				"		getAdd(lx);\n" +
				"	}\n" +
				"	static <@NonNull P>  void getAdd(List<P> lt) {\n" +
				"		lt.add(lt.get(0));\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"");
	}
	public void testWildcardCapture3() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"       List<@Nullable X> ax = new ArrayList<@Nullable X>();\n" +
				"       ax.add(new X());\n" +
				"		List<@Nullable ? extends X> lx = ax;\n" +
				"		getAdd(lx);\n" +
				"	}\n" +
				"	static <@NonNull P>  void getAdd(List<P> lt) {\n" +
				"		lt.add(lt.get(0));\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 17)\n" +
			"	getAdd(lx);\n" +
			"	       ^^\n" +
			"Null type mismatch (type annotations): required \'List<@NonNull capture#of ? extends X>\' but this expression has type \'List<@Nullable capture#of ? extends X>\'\n" +
			"----------\n");
	}
	public void testLocalArrays() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"       class L {};\n" +
				"       L @NonNull [] @Nullable [] la = new L[5][];\n" +
				"       L @Nullable [] @NonNull [] la2 = new L[3][];\n" +
				"       la = la2;\n" +
				"   }\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 13)\n" +
			"	L @NonNull [] @Nullable [] la = new L[5][];\n" +
			"	                                ^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'L[][]\' needs unchecked conversion to conform to \'L @NonNull[] @Nullable[]\'\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 14)\n" +
			"	L @Nullable [] @NonNull [] la2 = new L[3][];\n" +
			"	                                 ^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'L[][]\' needs unchecked conversion to conform to \'L @Nullable[] @NonNull[]\'\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 15)\n" +
			"	la = la2;\n" +
			"	     ^^^\n" +
			"Null type mismatch (type annotations): required \'L @NonNull[] @Nullable[]\' but this expression has type \'L @Nullable[] @NonNull[]\'\n" +
			"----------\n");

		// Without annotations.
		runConformTestWithLibs(
				false /* don't flush output dir */,
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"       class L {};\n" +
					"       L [] [] la = new L[5][];\n" +
					"       L []  [] la2 = new L[3][];\n" +
					"       la = la2;\n" +
					"       System.out.println(\"Done\");\n" +
					"   }\n" +
					"}\n"
				},
				getCompilerOptions(),
				"",
				"Done");
	}
	public void testRawType() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"public class X<T> {\n" +
				"	class Y <P> {}\n" +
				"	public static void main(String[] args) {\n" +
				"		@NonNull X x = null;\n" +
				"		X.@NonNull Y xy = null;\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	@NonNull X x = null;\n" +
			"	         ^\n" +
			"X is a raw type. References to generic type X<T> should be parameterized\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	@NonNull X x = null;\n" +
			"	               ^^^^\n" +
			"Null type mismatch: required \'@NonNull X\' but the provided value is null\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 6)\n" +
			"	X.@NonNull Y xy = null;\n" +
			"	^^^^^^^^^^^^\n" +
			"X.Y is a raw type. References to generic type X<T>.Y<P> should be parameterized\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 6)\n" +
			"	X.@NonNull Y xy = null;\n" +
			"	                  ^^^^\n" +
			"Null type mismatch: required \'X.@NonNull Y\' but the provided value is null\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420456, [1.8][null] AIOOB in null analysis code.
	public void test420456() {
		final Map compilerOptions = getCompilerOptions();
		compilerOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
			new String[] {
				"X.java",
				"import java.util.Arrays;\n" +
				"public class X {\n" +
				"	public static void main(String [] args) {\n" +
				"		Integer [] array = new Integer[] { 1234, 5678, 789 };\n" +
				"		Arrays.sort(array, Integer::compare);\n" +
				"       System.out.println(\"\" + array[0] + array[1] + array[2]);\n" +
				"	}\n" +
				"}\n"
			},
			compilerOptions,
			"",
			"78912345678");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422134, [1.8] NPE in NullAnnotationMatching with inlined lambda expression used with a raw type
	public void test422134() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.Collections;\n" +
				"public class X {\n" +
				"	public static void main(String args[]) {\n" +
				"		Collections.sort(new ArrayList(), (o1, o2) -> {\n" +
				"			return o1.compareToIgnoreCase(o1);\n" +
				"		});\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	Collections.sort(new ArrayList(), (o1, o2) -> {\n" +
			"			return o1.compareToIgnoreCase(o1);\n" +
			"		});\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked invocation sort(ArrayList, (<no type> o1, <no type> o2) -> {})" +
			" of the generic method sort(List<T>, Comparator<? super T>) of type Collections\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	Collections.sort(new ArrayList(), (o1, o2) -> {\n" +
			"	                 ^^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type ArrayList needs unchecked conversion to conform to List<Object>\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 5)\n" +
			"	Collections.sort(new ArrayList(), (o1, o2) -> {\n" +
			"	                     ^^^^^^^^^\n" +
			"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 6)\n" +
			"	return o1.compareToIgnoreCase(o1);\n" +
			"	          ^^^^^^^^^^^^^^^^^^^\n" +
			"The method compareToIgnoreCase(Object) is undefined for the type Object\n" +
			"----------\n",
			this.LIBS,
			true/*flush*/);
	}

	// should not try to analyze arguments of a polymorphic method call
	public void testBug424725() {
		runConformTestWithLibs(
			new String[] {
				"AnnotatedRecordMapper.java",
				"import java.lang.invoke.MethodHandle;\n" +
				"\n" +
				"public final class AnnotatedRecordMapper<T> {\n" +
				"  private MethodHandle afterLoadStore;\n" +
				"\n" +
				"  public void invokeAfterLoadStore(Object object, Object database) {\n" +
				"    if(afterLoadStore != null) {\n" +
				"      try {\n" +
				"        afterLoadStore.invoke(object, database);\n" +
				"      }\n" +
				"      catch(Throwable e) {\n" +
				"        throw new RuntimeException(e);\n" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"}"
			},
			null,
			"");
	}

	public void testBug424727() {
		runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"@org.eclipse.jdt.annotation.NonNull public class X {\n" +
				"	static X singleton = new X();\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	@org.eclipse.jdt.annotation.NonNull public class X {\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The nullness annotation \'NonNull\' is not applicable at this location\n" +
			"----------\n");
		// note: to be updated with https://bugs.eclipse.org/415918
	}

public void testBug424637() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"import java.nio.file.Files;\n" +
			"import java.nio.file.Path;\n" +
			"import java.util.function.Function;\n" +
			"import java.util.stream.Stream;\n" +
			"\n" +
			"public class X {\n" +
			"  public static void method() {\n" +
			"    Function<Path, Stream<Path>> method = Files::walk;\n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	Function<Path, Stream<Path>> method = Files::walk;\n" +
		"	                                      ^^^^^^^^^^^\n" +
		"Unhandled exception type IOException\n" +
		"----------\n",
		this.LIBS,
		true/*flush*/);
}

public void testBug424637a() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import java.nio.file.FileVisitOption;\n" +
			"import java.nio.file.Path;\n" +
			"import java.util.function.BiFunction;\n" +
			"import java.util.stream.Stream;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"interface TriFunc<A,B,C,D> { D apply(A a, B b, C c); }\n" +
			"public class X {\n" +
			"  public static Stream<Path> myWalk(Path p, @NonNull FileVisitOption ... options) { return null; }\n" +
			"  public static void method() {\n" +
			"    BiFunction<Path, @Nullable FileVisitOption, Stream<Path>> method1 = X::myWalk;\n" + // one element varargs - nullity mismatch
			"    BiFunction<Path, @Nullable FileVisitOption[], Stream<Path>> method2 = X::myWalk;\n" + // pass-through array varargs - nullity mismatch
			"    BiFunction<Path, FileVisitOption[], Stream<Path>> method3 = X::myWalk;\n" + // pass-through array varargs - unchecked
			" 	 TriFunc<Path, @NonNull FileVisitOption, @Nullable FileVisitOption, Stream<Path>> method4 = X::myWalk;\n" + // two-element varargs - nullity mismatch on one of them
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	BiFunction<Path, @Nullable FileVisitOption, Stream<Path>> method1 = X::myWalk;\n" +
		"	                                                                    ^^^^^^^^^\n" +
		"Null type mismatch at parameter 2: required \'@NonNull FileVisitOption\' but provided \'@Nullable FileVisitOption\' via method descriptor BiFunction<Path,FileVisitOption,Stream<Path>>.apply(Path, FileVisitOption)\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	BiFunction<Path, @Nullable FileVisitOption[], Stream<Path>> method2 = X::myWalk;\n" +
		"	                                                                      ^^^^^^^^^\n" +
		"Null type mismatch at parameter 2: required \'@NonNull FileVisitOption []\' but provided \'@Nullable FileVisitOption []\' via method descriptor BiFunction<Path,FileVisitOption[],Stream<Path>>.apply(Path, FileVisitOption[])\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 13)\n" +
		"	BiFunction<Path, FileVisitOption[], Stream<Path>> method3 = X::myWalk;\n" +
		"	                                                            ^^^^^^^^^\n" +
		"Null type safety: parameter 2 provided via method descriptor BiFunction<Path,FileVisitOption[],Stream<Path>>.apply(Path, FileVisitOption[]) needs unchecked conversion to conform to \'@NonNull FileVisitOption []\'\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 14)\n" +
		"	TriFunc<Path, @NonNull FileVisitOption, @Nullable FileVisitOption, Stream<Path>> method4 = X::myWalk;\n" +
		"	                                                                                           ^^^^^^^^^\n" +
		"Null type mismatch at parameter 3: required \'@NonNull FileVisitOption\' but provided \'@Nullable FileVisitOption\' via method descriptor TriFunc<Path,FileVisitOption,FileVisitOption,Stream<Path>>.apply(Path, FileVisitOption, FileVisitOption)\n" +
		"----------\n");
}

public void testBug424637_comment3() {
	runConformTestWithLibs(
		new String[] {
			"VarArgsMethodReferenceTest.java",
			"import java.util.function.Consumer;\n" +
			"public class VarArgsMethodReferenceTest {\n" +
			"  public static void main(String[] argv) {\n" +
			"    Consumer<String> printffer;\n" +
			"    printffer = System.out::printf;\n" +
			"  }\n" +
			"}"
		},
		null,
		"");
}
public void testBug427163() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	void consume(@NonNull String @Nullable... strings) {\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		""
	);
}
public void testBug427163b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	void consume1(@NonNull @Nullable String @Nullable[] strings) {}\n" +
			"	void consume2(@Nullable String @NonNull @Nullable... strings) {}\n" +
			"	void consume3(@Nullable String[] @NonNull @Nullable[] strings) {}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	void consume1(@NonNull @Nullable String @Nullable[] strings) {}\n" +
		"	                       ^^^^^^^^^\n" +
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	void consume2(@Nullable String @NonNull @Nullable... strings) {}\n" +
		"	                               ^^^^^^^^^^^^^^^^^^\n" +
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	void consume3(@Nullable String[] @NonNull @Nullable[] strings) {}\n" +
		"	                                 ^^^^^^^^^^^^^^^^^^\n" +
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" +
		"----------\n"
	);
}
public void testBug427163c() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	String[][] strings0 = new @NonNull String @Nullable[] @Nullable[] {};\n" +
			"	String[] strings1 = new String @NonNull @Nullable[] {};\n" +
			"	Object[] objects2 = new Object @NonNull @Nullable[1];\n" +
			"	String[] strings3 = new @NonNull @Nullable String [1];\n" +
			"	String[] strings4 = new @NonNull String  @Nullable @NonNull[1];\n" +
			"	String[][] strings5 = new String[] @NonNull @Nullable[] {};\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	String[] strings1 = new String @NonNull @Nullable[] {};\n" +
		"	                               ^^^^^^^^^^^^^^^^^^\n" +
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	Object[] objects2 = new Object @NonNull @Nullable[1];\n" +
		"	                               ^^^^^^^^^^^^^^^^^^\n" +
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	String[] strings3 = new @NonNull @Nullable String [1];\n" +
		"	                        ^^^^^^^^^^^^^^^^^^\n" +
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 7)\n" +
		"	String[] strings4 = new @NonNull String  @Nullable @NonNull[1];\n" +
		"	                                         ^^^^^^^^^^^^^^^^^^\n" +
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" +
		"----------\n" +
		"5. INFO in X.java (at line 7)\n" +
		"	String[] strings4 = new @NonNull String  @Nullable @NonNull[1];\n" +
		"	                                                           ^^^\n" +
		"This array dimension with declared element type @NonNull String will be initialized with \'null\' entries\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 8)\n" +
		"	String[][] strings5 = new String[] @NonNull @Nullable[] {};\n" +
		"	                                   ^^^^^^^^^^^^^^^^^^\n" +
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" +
		"----------\n"
	);
}
// assorted tests with upper-bounded wildcards with null annotations
public void testTypeBounds1() {
	runNegativeTestWithLibs(
		new String[] {
			"C.java",
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class A { }\n" +
			"class B extends A {}\n" +
			"public class C {\n" +
			"	\n" +
			"	@NonNull A testExtends(List<@NonNull B> lb1, List<@Nullable B> lb2, boolean f) {\n" +
			"		List<? extends @NonNull A> la1 = lb1;\n" +
			"		la1.add(null); // ERR1\n" +
			"		if (la1.size() > 0)\n" +
			"			return la1.get(0); // OK\n" +
			"		la1 = lb2; // ERR2\n" +
			"		List<? extends @Nullable A> la2 = lb1; // OK\n" +
			"		la2.add(null); // ERR3\n" +
			"		if (la2.size() > 0)\n" +
			"			return la2.get(0); // ERR4\n" +
			"		la2 = lb2; // OK\n" +
			"		if (f)\n" +
			"			return mExtends1(lb1); // OK, since we infer T to @NonNull B\n" +
			"		return mExtends2(lb1);\n" +
			"	}\n" +
			"	<T extends @Nullable A> T mExtends1(List<T> t) { return null; /*ERR5*/ }\n" +
			"	<T extends @NonNull A> T mExtends2(List<T> t) { return null; /*ERR6*/ }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in C.java (at line 12)\n" +
		"	la1.add(null); // ERR1\n" +
		"	        ^^^^\n" +
		"Null type mismatch: required \'? extends @NonNull A\' but the provided value is null\n" +
		"----------\n" +
		"2. INFO in C.java (at line 14)\n" +
		"	return la1.get(0); // OK\n" +
		"	       ^^^^^^^^^^\n" +
		"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<capture#of ? extends @NonNull A>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind\n" +
		"----------\n" +
		"3. ERROR in C.java (at line 15)\n" +
		"	la1 = lb2; // ERR2\n" +
		"	      ^^^\n" +
		"Null type mismatch (type annotations): required \'List<? extends @NonNull A>\' but this expression has type \'List<@Nullable B>\'\n" +
		"----------\n" +
		"4. ERROR in C.java (at line 17)\n" +
		"	la2.add(null); // ERR3\n" +
		"	        ^^^^\n" +
		"Null type mismatch: required \'? extends @Nullable A\' but the provided value is null\n" +
		"----------\n" +
		"5. ERROR in C.java (at line 19)\n" +
		"	return la2.get(0); // ERR4\n" +
		"	       ^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull A\' but this expression has type \'capture#of ? extends @Nullable A\'\n" +
		"----------\n" +
		"6. ERROR in C.java (at line 25)\n" +
		"	<T extends @Nullable A> T mExtends1(List<T> t) { return null; /*ERR5*/ }\n" +
		"	                                                        ^^^^\n" +
		"Null type mismatch: required \'T extends @Nullable A\' but the provided value is null\n" +
		"----------\n" +
		"7. ERROR in C.java (at line 26)\n" +
		"	<T extends @NonNull A> T mExtends2(List<T> t) { return null; /*ERR6*/ }\n" +
		"	                                                       ^^^^\n" +
		"Null type mismatch: required \'T extends @NonNull A\' but the provided value is null\n" +
		"----------\n"
	);
}
// assorted tests with lower-bounded wildcards with null annotations
public void testTypeBounds2() {
	runNegativeTestWithLibs(
		new String[] {
			"C.java",
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class A { }\n" +
			"class B extends A {}\n" +
			"public class C {\n" +
			"	\n" +
			"	@NonNull Object testSuper(List<@Nullable A> la1, List<@NonNull A> la2, boolean f) {\n" +
			"		List<? super @NonNull B> lb1 = la1; // OK\n" +
			"		lb1.add(null); // ERR1\n" +
			"		if (lb1.size() > 0)\n" +
			"			return lb1.get(0); // ERR2\n" +
			"		lb1 = la2; // OK\n" +
			"		List<? super @Nullable B> lb2 = la1;\n" +
			"		lb2.add(null);\n" +
			"		if (lb2.size() > 0)\n" +
			"			return lb2.get(0); // ERR3\n" +
			"		lb2 = la2; // ERR4\n" +
			"		if (f)\n" +
			"			return mSuper1(la1); // ERR5\n" +
			"		return mSuper2(la1); // ERR6 on arg\n" +
			"	}\n" +
			"	<T extends @Nullable A> T mSuper1(List<T> t) { return null; /*ERR7*/ }\n" +
			"	<T extends @NonNull A> T mSuper2(List<T> t) { return null; /*ERR8*/ }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in C.java (at line 12)\n" +
		"	lb1.add(null); // ERR1\n" +
		"	        ^^^^\n" +
		"Null type mismatch: required \'? super @NonNull B\' but the provided value is null\n" +
		"----------\n" +
		"2. ERROR in C.java (at line 14)\n" +
		"	return lb1.get(0); // ERR2\n" +
		"	       ^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'capture#of ? super @NonNull B\'\n" +
		"----------\n" +
		"3. ERROR in C.java (at line 19)\n" +
		"	return lb2.get(0); // ERR3\n" +
		"	       ^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'capture#of ? super @Nullable B\'\n" +
		"----------\n" +
		"4. ERROR in C.java (at line 20)\n" +
		"	lb2 = la2; // ERR4\n" +
		"	      ^^^\n" +
		"Null type mismatch (type annotations): required \'List<? super @Nullable B>\' but this expression has type \'List<@NonNull A>\'\n" +
		"----------\n" +
		"5. ERROR in C.java (at line 22)\n" +
		"	return mSuper1(la1); // ERR5\n" +
		"	       ^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable A\'\n" +
		"----------\n" +
		"6. ERROR in C.java (at line 23)\n" +
		"	return mSuper2(la1); // ERR6 on arg\n" +
		"	               ^^^\n" +
		"Null type mismatch (type annotations): required \'List<@NonNull A>\' but this expression has type \'List<@Nullable A>\'\n" +
		"----------\n" +
		"7. ERROR in C.java (at line 25)\n" +
		"	<T extends @Nullable A> T mSuper1(List<T> t) { return null; /*ERR7*/ }\n" +
		"	                                                      ^^^^\n" +
		"Null type mismatch: required \'T extends @Nullable A\' but the provided value is null\n" +
		"----------\n" +
		"8. ERROR in C.java (at line 26)\n" +
		"	<T extends @NonNull A> T mSuper2(List<T> t) { return null; /*ERR8*/ }\n" +
		"	                                                     ^^^^\n" +
		"Null type mismatch: required \'T extends @NonNull A\' but the provided value is null\n" +
		"----------\n"
	);
}
// assigning values upper bounded wildcard types carrying null annotations
public void testTypeBounds3() {
	runNegativeTestWithLibs(
		new String[] {
			"C.java",
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class A { }\n" +
			"class B extends A {}\n" +
			"public class C {\n" +
			"	\n" +
			"	void testExtends(List<? extends @NonNull B> lb1, List<? extends @Nullable B> lb2) {\n" +
			"		List<? extends @NonNull A> la1 = lb1;\n" +
			"		la1 = lb2; // ERR\n" +
			"		List<? extends @Nullable A> la2 = lb1;\n" +
			"		la2 = lb2;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in C.java (at line 12)\n" +
		"	la1 = lb2; // ERR\n" +
		"	      ^^^\n" +
		"Null type mismatch (type annotations): required \'List<? extends @NonNull A>\' but this expression has type \'List<capture#of ? extends @Nullable B>\'\n" +
		"----------\n"
	);
}
// assigning values lower bounded wildcard types carrying null annotations
public void testTypeBounds4() {
	runNegativeTestWithLibs(
		new String[] {
			"C.java",
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class A { }\n" +
			"class B extends A {}\n" +
			"public class C {\n" +
			"	\n" +
			"	void testSuper(List<? super @Nullable A> la1, List<? super @NonNull A> la2) {\n" +
			"		List<? super @NonNull B> lb1 = la1; // OK\n" +
			"		lb1 = la2; // OK\n" +
			"		List<? super @Nullable B> lb2 = la1;\n" +
			"		lb2 = la2; // ERR4\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in C.java (at line 14)\n" +
		"	lb2 = la2; // ERR4\n" +
		"	      ^^^\n" +
		"Null type mismatch (type annotations): required \'List<? super @Nullable B>\' but this expression has type \'List<capture#of ? super @NonNull A>\'\n" +
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429387, [1.8][compiler] AIOOBE in AbstractMethodDeclaration.createArgumentBindings
public void test429387() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.function.BiFunction;\n" +
			"import java.util.function.Supplier;\n" +
			"import java.util.function.ToIntFunction;\n" +
			"import java.util.stream.IntStream;\n" +
			"import java.util.stream.Stream;\n" +
			"public interface X {\n" +
			"static <BT, T extends BT, IS extends IntStream, E extends Exception> IntStreamy<E>\n" +
			"internalFlatMapToInt(Functionish<BT, IS, E> mapper,\n" +
			"Class<E> classOfE,\n" +
			"Supplier<Stream<T>> maker) {\n" +
			"BiFunction<Stream<T>, ToIntFunction<BT>, IntStream> func = (Stream<T> t, ToIntFunction<BT, IS> m) -> t.flatmmapToInt(m);\n" +
			"return IntStreamy.fromFlatMap(func, mapper, classOfE, maker);\n" +
			"}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	static <BT, T extends BT, IS extends IntStream, E extends Exception> IntStreamy<E>\n" +
		"	                                                                     ^^^^^^^^^^\n" +
		"IntStreamy cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	internalFlatMapToInt(Functionish<BT, IS, E> mapper,\n" +
		"	                     ^^^^^^^^^^^\n" +
		"Functionish cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 11)\n" +
		"	BiFunction<Stream<T>, ToIntFunction<BT>, IntStream> func = (Stream<T> t, ToIntFunction<BT, IS> m) -> t.flatmmapToInt(m);\n" +
		"	                                                                         ^^^^^^^^^^^^^\n" +
		"Incorrect number of arguments for type ToIntFunction<T>; it cannot be parameterized with arguments <BT, IS>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	BiFunction<Stream<T>, ToIntFunction<BT>, IntStream> func = (Stream<T> t, ToIntFunction<BT, IS> m) -> t.flatmmapToInt(m);\n" +
		"	                                                                                                                     ^\n" +
		"m cannot be resolved to a variable\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 12)\n" +
		"	return IntStreamy.fromFlatMap(func, mapper, classOfE, maker);\n" +
		"	       ^^^^^^^^^^\n" +
		"IntStreamy cannot be resolved\n" +
		"----------\n",
		this.LIBS,
		true/*flush*/);
}
public void testBug429403() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"class Person {}\n" +
			"public class X {\n" +
			"	List<@NonNull Person> l = new ArrayList<@Nullable Person>();" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	List<@NonNull Person> l = new ArrayList<@Nullable Person>();}\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'List<@NonNull Person>\' but this expression has type \'@NonNull ArrayList<@Nullable Person>\', corresponding supertype is \'List<@Nullable Person>\'\n" +
		"----------\n");
}
public void testBug430219() {
    runNegativeTest(
        new String[] {
            "X.java",
            "import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
            "@NonNullByDefault\n" +
            "public class X {\n" +
            "       void foo(int @NonNull [] x) {}\n" +
            "}\n"
        },
        "----------\n" +
	   "1. ERROR in X.java (at line 4)\n" +
		"	void foo(int @NonNull [] x) {}\n" +
        "	              ^^^^^^^\n" +
	   "NonNull cannot be resolved to a type\n" +
	   "----------\n",
	   this.LIBS,
	   true/*flush*/);
}
public void testBug430219a() {
    runConformTestWithLibs(
        new String[] {
            "X.java",
            "import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.TYPE_USE) @interface Marker{}\n" +
            "@NonNullByDefault\n" +
            "public class X {\n" +
            "       void foo(int @Marker[] x) {}\n" +
            "}\n"
        },
        getCompilerOptions(),
        "");
}

// apply null default to type arguments:
public void testDefault01() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"@NonNullByDefault(DefaultLocation.TYPE_ARGUMENT)\n" +
			"public class X {\n" +
			"	List<Number> test1(List<Number> in) {\n" +
			"		in.add(null); // ERR\n" +
			"		return new ArrayList<@Nullable Number>(); // ERR\n" +
			"	}\n" +
			"	java.util.List<java.lang.Number> test2(java.util.List<java.lang.Number> in) {\n" +
			"		in.add(null); // ERR\n" +
			"		return new ArrayList<java.lang.@Nullable Number>(); // ERR\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	in.add(null); // ERR\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	return new ArrayList<@Nullable Number>(); // ERR\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'List<@NonNull Number>\' but this expression has type \'@NonNull ArrayList<@Nullable Number>\', corresponding supertype is \'List<@Nullable Number>\'\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	in.add(null); // ERR\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	return new ArrayList<java.lang.@Nullable Number>(); // ERR\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'List<@NonNull Number>\' but this expression has type \'@NonNull ArrayList<@Nullable Number>\', corresponding supertype is \'List<@Nullable Number>\'\n" +
		"----------\n");
}

// apply null default to type arguments - no effect on type variable or wildcard, but apply strict checking assuming nothing
public void testDefault01b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"@NonNullByDefault(DefaultLocation.TYPE_ARGUMENT)\n" +
			"public class X<T> {\n" +
			"	List<T> test(List<? extends Number> in) {\n" +
			"		in.add(null); // NOK, cannot assume nullable\n" +
			"		needNN(in.get(0)); // NOK, cannot assume nonnull\n" +
			"		return new ArrayList<@Nullable T>(); // NOK, cannot assume nullable for T in List<T>\n" +
			"	}\n" +
			"	void needNN(@NonNull Number n) {}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	in.add(null); // NOK, cannot assume nullable\n" +
		"	       ^^^^\n" +
		"Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'? extends Number\'\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	needNN(in.get(0)); // NOK, cannot assume nonnull\n" +
		"	       ^^^^^^^^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'capture#2-of ? extends java.lang.Number\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 8)\n" +
		"	return new ArrayList<@Nullable T>(); // NOK, cannot assume nullable for T in List<T>\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'List<T>\' but this expression has type \'@NonNull ArrayList<@Nullable T>\', corresponding supertype is \'List<@Nullable T>\'\n" +
		"----------\n");
}

// apply null default to parameters:
public void testDefault02() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"public class X {\n" +
			"	Number test1(Number in) {\n" +
			"		System.out.print(in.intValue()); // OK\n" +
			"		test1(null); // ERR\n" +
			"		return null; // OK\n" +
			"	}\n" +
			"	java.lang.Number test2(java.lang.Number in) {\n" +
			"		System.out.print(in.intValue()); // OK\n" +
			"		test2(null); // ERR\n" +
			"		return null; // OK\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	test1(null); // ERR\n" +
		"	      ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 11)\n" +
		"	test2(null); // ERR\n" +
		"	      ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n");
}

// apply null default to return type - annotation at method:
public void testDefault03() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"	Number test(Number in) {\n" +
			"		System.out.print(in.intValue());\n" +
			"		test(null); // OK\n" +
			"		return null; // ERR\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	return null; // ERR\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n");
}

// apply null default to field
public void testDefault04() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault(DefaultLocation.FIELD)\n" +
			"public class X {\n" +
			"	Number field; // ERR since uninitialized\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	Number field; // ERR since uninitialized\n" +
		"	       ^^^^^\n" +
		"The @NonNull field field may not have been initialized\n" +
		"----------\n");
}

// default default
public void testDefault05() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"	Number field; // ERR since uninitialized\n" +
			"	void test1(Number[] ns) {\n" +
			"		ns[0] = null; // OK since not affected by default\n" +
			"	}\n" +
			"	void test2(java.lang.Number[] ns) {\n" +
			"		ns[0] = null; // OK since not affected by default\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	Number field; // ERR since uninitialized\n" +
		"	       ^^^^^\n" +
		"The @NonNull field field may not have been initialized\n" +
		"----------\n");
}

//default default
public void testDefault05_custom() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	runner.customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	runner.customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "org.foo.NonNullByDefault");
	runner.testFiles =
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT,
			CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT,
			CUSTOM_NNBD_NAME,
			CUSTOM_NNBD_CONTENT,
			"test/package-info.java",
			"@org.foo.NonNullByDefault\n" +
			"package test;\n",
			"test/X.java",
			"package test;\n" +
			"public class X {\n" +
			"	Number field; // ERR since uninitialized\n" +
			"	void test1(Number[] ns) {\n" +
			"		ns[0] = null; // OK since not affected by default\n" +
			"	}\n" +
			"	void test2(java.lang.Number[] ns) {\n" +
			"		ns[0] = null; // OK since not affected by default\n" +
			"	}\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in test\\X.java (at line 3)\n" +
		"	Number field; // ERR since uninitialized\n" +
		"	       ^^^^^\n" +
		"The @NonNull field field may not have been initialized\n" +
		"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

//default default
public void testDefault05_custom2() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	runner.customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	runner.customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "org.foo.NonNullByDefault");
	runner.testFiles =
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT,
			CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT,
			CUSTOM_NNBD_NAME,
			CUSTOM_NNBD_CONTENT
		};
	runner.runConformTest();
	runner.shouldFlushOutputDirectory = false;
	runner.testFiles =
		new String[] {
			"test/package-info.java",
			"@org.foo.NonNullByDefault\n" +
			"package test;\n",
			"test/X.java",
			"package test;\n" +
			"public class X {\n" +
			"	Number field; // ERR since uninitialized\n" +
			"	void test1(Number[] ns) {\n" +
			"		ns[0] = null; // OK since not affected by default\n" +
			"	}\n" +
			"	void test2(java.lang.Number[] ns) {\n" +
			"		ns[0] = null; // OK since not affected by default\n" +
			"	}\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in test\\X.java (at line 3)\n" +
		"	Number field; // ERR since uninitialized\n" +
		"	       ^^^^^\n" +
		"The @NonNull field field may not have been initialized\n" +
		"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// apply default to type parameter - inner class
public void testDefault06() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
			"public class X {\n" +
			"	class Inner<T> {\n" +
			"		T process(T t) {\n" +
			"			@NonNull T t2 = t; // OK\n" +
			"			return null; // ERR\n" +
			" 		}\n" +
			"	}\n" +
			"	void test(Inner<Number> inum) {\n" +
			"		@NonNull Number nnn = inum.process(null); // ERR on argument\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	return null; // ERR\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull T\' but the provided value is null\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	void test(Inner<Number> inum) {\n" +
		"	                ^^^^^^\n" +
		"Null constraint mismatch: The type \'Number\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 11)\n" +
		"	@NonNull Number nnn = inum.process(null); // ERR on argument\n" +
		"	                                   ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n");
}

//apply default to type parameter - class above
public void testDefault06_b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault({DefaultLocation.TYPE_PARAMETER, DefaultLocation.TYPE_ARGUMENT})\n" +
			"	class Inner<T> {\n" +
			"		T process(T t) {\n" +
			"			@NonNull T t2 = t; // OK\n" +
			"			return null; // ERR\n" +
			" 		}\n" +
			"	}\n" +
			"@NonNullByDefault({DefaultLocation.TYPE_PARAMETER, DefaultLocation.TYPE_ARGUMENT})\n" +
			"public class X {\n" +
			"	void test(Inner<Number> inum) {\n" +
			"		@NonNull Number nnn = inum.process(null); // ERR on argument\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	return null; // ERR\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull T\' but the provided value is null\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	@NonNull Number nnn = inum.process(null); // ERR on argument\n" +
		"	                                   ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n");
}

// apply default to type bound - method in inner class
public void testDefault07() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"@NonNullByDefault(DefaultLocation.TYPE_BOUND)\n" +
			"public class X {\n" +
			"	class Inner {\n" +
			"		<T extends Number> T process(T t, List<? extends Number> l) {\n" +
			"			@NonNull T t2 = t; // OK\n" +
			"			@NonNull Number n = l.get(0); // OK\n" +
			"			return null; // ERR\n" +
			" 		}\n" +
			"	}\n" +
			"	void test(Inner inner) {\n" +
			"		@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. INFO in X.java (at line 8)\n" +
		"	@NonNull Number n = l.get(0); // OK\n" +
		"	                    ^^^^^^^^\n" +
		"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<capture#of ? extends @NonNull Number>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	return null; // ERR\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'T extends @NonNull Number\' but the provided value is null\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 13)\n" +
		"	@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg\n" +
		"	                                    ^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Integer\'\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 13)\n" +
		"	@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg\n" +
		"	                                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'List<? extends @NonNull Number>\' but this expression has type \'@NonNull ArrayList<@Nullable Integer>\', corresponding supertype is \'List<@Nullable Integer>\'\n" +
		"----------\n");
}

//apply null default to type arguments:
public void testDefault01_bin() {
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"import java.util.*;\n" +
				"import java.lang.annotation.*;\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.CLASS) @interface Important {}\n" +
				"\n" +
				"@NonNullByDefault(DefaultLocation.TYPE_ARGUMENT)\n" +
				"public class X {\n" +
				"	List<Number> test1(List<@Important Number> in) {\n" +
				"		return new ArrayList<@NonNull Number>();\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in X.java (at line 10)\n" +
			"	return new ArrayList<@NonNull Number>();\n" +
			"	                     ^^^^^^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n",
			"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"public class Y {\n" +
			"	void test(List<Number> in, X x) {\n" +
			"		x.test1(new ArrayList<@Nullable Number>()) // ERR at arg\n" +
			"			.add(null); // ERR\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Y.java (at line 5)\n" +
		"	x.test1(new ArrayList<@Nullable Number>()) // ERR at arg\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'List<@NonNull Number>\' but this expression has type \'@NonNull ArrayList<@Nullable Number>\', corresponding supertype is \'List<@Nullable Number>\'\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 6)\n" +
		"	.add(null); // ERR\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n");
}

//apply null default to parameters:
public void testDefault02_bin() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"public class X {\n" +
			"	Number test1(Number in) {\n" +
			"		return null; // OK\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y {\n" +
			"	@NonNull Number test(X x) {\n" +
			"		return x.test1(null); // error at arg, unchecked at return\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Y.java (at line 4)\n" +
		"	return x.test1(null); // error at arg, unchecked at return\n" +
		"	       ^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'Number\' needs unchecked conversion to conform to \'@NonNull Number\'\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 4)\n" +
		"	return x.test1(null); // error at arg, unchecked at return\n" +
		"	               ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n");
}

//apply null default to return type - annotation at method:
public void testDefault03_bin() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"	Number test(Number in) {\n" +
			"		return new Integer(13);\n" +
			"	}\n" +
			"}\n"
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("deprecation");
	runner.runConformTest();

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles =
		new String[] {
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y {\n" +
			"	@NonNull Number test(X x) {\n" +
			"		return x.test(null); // both OK\n" +
			"	}\n" +
			"}\n"
		};
	runner.runConformTest();
}

// apply null default to field - also test mixing of explicit annotation with default @NonNull (other annot is not rendered in error)
public void testDefault04_bin() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.CLASS) @interface Important {}\n" +
			"@NonNullByDefault(DefaultLocation.FIELD)\n" +
			"public class X {\n" +
			"	@Important Number field = new Double(1.1);\n" +
			"}\n"
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("deprecation");
	runner.runConformTest();

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles =
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"	void test(X x) {\n" +
			"		x.field = null; // ERR\n" +
			"	}\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in Y.java (at line 3)\n" +
		"	x.field = null; // ERR\n" +
		"	          ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// default default
public void testDefault05_bin() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"	Number field = new Long(13);\n" +
			"	void test1(Number[] ns) {\n" +
			"		ns[0] = null; // OK since not affected by default\n" +
			"	}\n" +
			"}\n"
		};
	runner.runConformTest();

	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y {\n" +
			"	void test(X x, @Nullable Number @NonNull[] ns) {\n" +
			"		x.test1(ns); // OK since not affected by default\n" +
			"		x.field = null; // ERR\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Y.java (at line 5)\n" +
		"	x.field = null; // ERR\n" +
		"	          ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n");}

// apply default to type parameter - inner class
public void testDefault06_bin() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
			"public class X {\n" +
			"	static class Inner<T> {\n" +
			"		T process(T t) {\n" +
			"			return t;\n" +
			" 		}\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y {\n" +
			"	void test(X.Inner<Number> inum) { // illegal substitution\n" +
			"		@NonNull Number nnn = inum.process(null); // ERR on argument\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Y.java (at line 3)\n" +
		"	void test(X.Inner<Number> inum) { // illegal substitution\n" +
		"	                  ^^^^^^\n" +
		"Null constraint mismatch: The type \'Number\' is not a valid substitute for the type parameter \'@NonNull T extends Object\'\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 4)\n" +
		"	@NonNull Number nnn = inum.process(null); // ERR on argument\n" +
		"	                                   ^^^^\n" +
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" +
		"----------\n");}

// apply default to type bound - method in inner class
public void testDefault07_bin() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"@NonNullByDefault(DefaultLocation.TYPE_BOUND)\n" +
			"public class X {\n" +
			"	static class Inner {\n" +
			"		<T extends Number> T process(T t, List<? extends Number> l) {\n" +
			"			return t;\n" +
			" 		}\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"public class Y {\n" +
			"	void test(X.Inner inner) {\n" +
			"		@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Y.java (at line 5)\n" +
		"	@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg\n" +
		"	                                    ^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Integer\'\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 5)\n" +
		"	@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg\n" +
		"	                                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'List<? extends @NonNull Number>\' but this expression has type \'@NonNull ArrayList<@Nullable Integer>\', corresponding supertype is \'List<@Nullable Integer>\'\n" +
		"----------\n");
}
public void testBug431269() {
	runNegativeTest(
		new String[] {
			"p/QField.java",
			"package p;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class QField<R extends QField<R, ? >, T> {\n" +
			"	@NonNull\n" +
			"	protected R m_root;\n" +
			"\n" +
			"	public QField(@Nullable R root, @Nullable QField<R, ? > parent, @Nullable String propertyNameInParent) {\n" +
			"		m_root = root;\n" +
			"	}\n" +
			"}\n",
			"p/PLogLine.java",
			"package p;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class PLogLine<R extends QField<R, ? >> extends QField<R, LogLine> {\n" +
			"	public PLogLine(@Nullable R root, @Nullable QField<R, ? > parent, @Nullable String name) {\n" +
			"		super(root, parent, name);\n" +
			"	}\n" +
			"\n" +
			"	@NonNull\n" +
			"	public final QField<R, java.lang.String> lastName() {\n" +
			"		return new QField<R, java.lang.Long>(m_root, this, \"lastName\");\n" +
			"	}\n" +
			"\n" +
			"}\n",
			"p/LogLine.java",
			"package p;\n" +
			"\n" +
			"public class LogLine {\n" +
			"	private String m_lastName;\n" +
			"\n" +
			"	public String getLastName() {\n" +
			"		return m_lastName;\n" +
			"	}\n" +
			"\n" +
			"	public void setLastName(String property) {\n" +
			"		m_lastName = property;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in p\\QField.java (at line 10)\n" +
		"	m_root = root;\n" +
		"	         ^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull R extends QField<R extends QField<R,?>,?>\' but this expression has type \'@Nullable R extends QField<R extends QField<R,?>,?>\'\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in p\\PLogLine.java (at line 12)\n" +
		"	return new QField<R, java.lang.Long>(m_root, this, \"lastName\");\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from QField<R,Long> to QField<R,String>\n" +
		"----------\n",
		this.LIBS,
		true/*flush*/);
}
// was inferring null type annotations too aggressively
public void testBug432223() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	String val;\n" +
			"	public static @NonNull <T> T assertNotNull(@Nullable T object) {\n" +
			"		return assertNotNull(null, object);\n" +
			"	}\n" +
			"\n" +
			"	public static @NonNull <T> T assertNotNull(@Nullable String message, @Nullable T object) {\n" +
			"		if (object == null) {\n" +
			"			throw new NullPointerException(message);\n" +
			"		}\n" +
			"		return object;\n" +
			"	}\n" +
			"	void test(@Nullable X x) {\n" +
			"		@NonNull X safe = assertNotNull(x);\n" +
			"		System.out.println(safe.val);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug432977() {
	runConformTestWithLibs(
		new String[] {
			"Bar.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Bar {\n" +
			"	private String prop = \"\";\n" +
			"\n" +
			"	public String getProp() {\n" +
			"		return prop;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false /* flush */,
		new String[] {
			"Fu.java",
			"public class Fu {\n" +
			"	private Bar fubar = new Bar();\n" +
			"	\n" +
			"	public void method() {\n" +
			"		fubar.getProp().equals(\"\");\n" +
			"	}	\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug433586() {
	runConformTestWithLibs(
		new String[] {
			"NullConversionWarning.java",
			"import java.util.function.Consumer;\n" +
			"public class NullConversionWarning<T> {\n" +
			"\n" +
			"	public Consumer<T> peek2(Consumer<? super T> action) {\n" +
			"		// Null type safety: parameter 1 provided via\n" +
			"		// method descriptor Consumer<T>.accept(T) needs\n" +
			"		// unchecked conversion to conform to 'capture#of ? super T'\n" +
			"		Consumer<T> action2 = action::accept;\n" +
			"		return action2;\n" +
			"	}\n" +
			"	void foo(Consumer<? super T> action, T t) {\n" +
			"	  Consumer<T> action2 = t2 -> action.accept(t2);\n" +
			"	  action.accept(t);\n" +
			"	  action2.accept(t);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// NPE without the fix.
public void testBug433478() {
	runNegativeTestWithLibs(
            new String[] {
                "X.java",
                "import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
                "import org.eclipse.jdt.annotation.Nullable;\n" +
                "\n" +
                "@NonNullByDefault class Y { }\n" +
                "\n" +
                "interface I<T> {\n" +
                "       @Nullable T foo();\n" +
                "}\n" +
                "\n" +
                "@NonNullByDefault \n" +
                "class X implements I<Y> {\n" +
                "       @Override\n" +
                "       public Y foo() {\n" +
                "               return null;\n" +
                "       }\n" +
                "}\n"
            },
            "----------\n" +
    		"1. ERROR in X.java (at line 14)\n" +
    		"	return null;\n" +
    		"	       ^^^^\n" +
    		"Null type mismatch: required \'@NonNull Y\' but the provided value is null\n" +
    		"----------\n");
}
// https://bugs.eclipse.org/434899
public void testTypeVariable6() {
	runNegativeTestWithLibs(
		new String[] {
			"Assert.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Assert {\n" +
			"	public static void caller() {\n" +
			"		assertNotNull(\"not null\");	// Compiler error\n" +
			"		assertNotNull(null);		// Compiler error\n" +
			"	}\n" +
			"	private static @NonNull <T> T assertNotNull(@Nullable T object) {\n" +
			"		return object; // this IS bogus\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Assert.java (at line 8)\n" +
		"	return object; // this IS bogus\n" +
		"	       ^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull T\' but this expression has type \'@Nullable T\'\n" +
		"----------\n");
}
// https://bugs.eclipse.org/434899 - variant which has always worked
public void testTypeVariable6a() {
	runConformTestWithLibs(
		new String[] {
			"Assert.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Assert {\n" +
			"	public static Object caller() {\n" +
			"		@NonNull Object result = assertNotNull(\"not null\");\n" +
			"		result = assertNotNull(null);\n" +
			"		return result;\n" +
			"	}\n" +
			"	private static @NonNull <T> T assertNotNull(@Nullable T object) {\n" +
			"		if (object == null) throw new NullPointerException();\n" +
			"		return object;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// Bug 438458 - [1.8][null] clean up handling of null type annotations wrt type variables
// - type parameter with explicit nullness, cannot infer otherwise
public void testTypeVariable7() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface I1 <@NonNull T> { T get(); }\n" +
			"public class X {\n" +
			"	<U> U m(I1<U> in) { return in.get(); }\n" +
			"	public void test(I1<@NonNull String> in) {\n" +
			"		@NonNull String s = m(in);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	<U> U m(I1<U> in) { return in.get(); }\n" +
		"	           ^\n" +
		"Null constraint mismatch: The type \'U\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n");
}
// Bug 438458 - [1.8][null] clean up handling of null type annotations wrt type variables
// - type parameter with explicit nullness, nullness must not spoil inference
public void testTypeVariable7a() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING); // allow ignoring bad substitution
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface I1 <@NonNull T> { T get(); }\n" +
			"public class X {\n" +
			"	<U> U m(I1<U> in) { return in.get(); }\n" +
			"	public void test1() {\n" +
			"		@Nullable String s = m(() -> \"OK\");\n" +
			"		System.out.println(s);\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().test1();\n" +
			"	}\n" +
			"}\n"
		},
		compilerOptions,
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	<U> U m(I1<U> in) { return in.get(); }\n" +
		"	           ^\n" +
		"Null constraint mismatch: The type \'U\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	@Nullable String s = m(() -> \"OK\");\n" +
		"	                       ^^^^^^^^^^\n" +
		"Contradictory null annotations: function type was inferred as \'@NonNull @Nullable String ()\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n",
		"OK");
}
// Bug 438458 - [1.8][null] clean up handling of null type annotations wrt type variables
// - type parameter with explicit nullness, nullness must not spoil inference
public void testTypeVariable7err() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface I1 <@Nullable T> { T get(); }\n" +
			"public class X {\n" +
			"	<U> U m(I1<U> in) { return in.get(); }\n" +
			"	public void test1() {\n" +
			"		@NonNull String s = m(() -> \"\");\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	<U> U m(I1<U> in) { return in.get(); }\n" +
		"	           ^\n" +
		"Null constraint mismatch: The type \'U\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	<U> U m(I1<U> in) { return in.get(); }\n" +
		"	                           ^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'U\' but this expression has type \'@Nullable U\', where \'U\' is a free type variable\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	@NonNull String s = m(() -> \"\");\n" +
		"	                      ^^^^^^^^\n" +
		"Contradictory null annotations: function type was inferred as \'@Nullable @NonNull String ()\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n");
}
//Bug 435570 - [1.8][null] @NonNullByDefault illegally tries to affect "throws E"
public void testTypeVariable8() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class Test<E extends Exception> {\n" +
			"	void test() throws E {}\n" + // was: Nullness annotations are not applicable at this location
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// Bug 438012 - Bogus Warning: The nullness annotation is redundant with a default that applies to this location
public void testTypeVariable9() {
	runConformTestWithLibs(
		new String[] {
			"Bar.java",
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT, TYPE_PARAMETER })\n" +
			"interface Bar<V> {\n" +
			"    V getV(V in);\n" +
			"    void setV(V v);\n" +
			"}"
		},
		getCompilerOptions(),
		"");
}
// Bug 439516 - [1.8][null] NonNullByDefault wrongly applied to implicit type bound of binary type
public void testTypeVariable10() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class X<T> {\n" +
			"	void test(T t) {}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false,
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"	void foo(X<@org.eclipse.jdt.annotation.Nullable String> xs) {\n" +
			"		xs.test(null);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// Bug 439516 - [1.8][null] NonNullByDefault wrongly applied to implicit type bound of binary type
// Problem 1 from: Bug 438971 - [1.8][null] @NonNullByDefault/@Nullable on parameter of generic interface
public void testTypeVariable10a() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X<T> {\n" +
			"	void test(@Nullable T t) {}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false,
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"	void foo(X<String> xs) {\n" +
			"		xs.test(\"OK\");\n" + // was: Contradictory null annotations: method was inferred as  ...
			"		xs.test(null);\n" + // was: Contradictory null annotations: method was inferred as  ...
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// Bug 439516 - [1.8][null] NonNullByDefault wrongly applied to implicit type bound of binary type
// warning for explicit "<T extends Object>"
public void testTypeVariable11() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault({DefaultLocation.TYPE_BOUND})\n" + // not: PARAMETER
			"public class X<T extends Object> {\n" +
			"	void test(T t) {}\n" +
			"}\n",
			"Y.java",
			"public class Y {\n" +
			"	void foo(X<@org.eclipse.jdt.annotation.Nullable String> xs) {\n" +
			"		xs.test(null);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	public class X<T extends Object> {\n" +
		"	                         ^^^^^^\n" +
		"The explicit type bound \'Object\' is not affected by the nullness default for DefaultLocation.TYPE_BOUND.\n" +
		"----------\n");
}
// Bug 438179 - [1.8][null] 'Contradictory null annotations' error on type variable with explicit null-annotation.
public void testTypeVariable12() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"	private Fu<String> fu = new Fu<>();\n" +
			"	public void foo() {\n" +
			"		fu.method();   // 'Contradictory null annotations' error\n" +
			"	}\n" +
			"}\n" +
			"class Fu<T> {\n" +
			"	@Nullable T method() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// Bug 438250 - [1.8][null] NPE trying to report bogus null annotation conflict
public void testTypeVariable13() {
	runConformTestWithLibs(
		new String[] {
			"FooBar.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault(org.eclipse.jdt.annotation.DefaultLocation.TYPE_BOUND)\n" +
			"public interface FooBar {\n" +
			"    <@org.eclipse.jdt.annotation.Nullable R extends Runnable> R foobar(R r);\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// Bug 438469 - [null] How-to use null type annotations with generic methods from interfaces in some library you only have as binary JAR?
public void testTypeVariable14() {
	runConformTestWithLibs(
		new String[] {
			"ITest.java",
			"interface ITest {\n" +
			"	<T> T foo(T arg); // or arg Class<T> or TypeToken<T> + return TypeAdapter<T>, etc.\n" +
			"}"
		},
		getCompilerOptions(),
		"");
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	runConformTestWithLibs(
		false,
		new String[] {
			"Test.java",
			"class Test implements ITest {\n" +
			"	@Override\n" +
			"	@SuppressWarnings(\"null\")\n" +
			"	public <T> @org.eclipse.jdt.annotation.Nullable T foo(T arg) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"");
}
// Bug 438467 - [compiler][null] Better error position for "The method _ cannot implement the corresponding method _ due to incompatible nullness constraints"
public void testTypeVariable15() {
	runNegativeTestWithLibs(
		new String[] {
			"ITest.java",
			"interface ITest {\n" +
			"	<T> T foo(T arg); // or arg Class<T> or TypeToken<T> + return TypeAdapter<T>, etc.\n" +
			"}",
			"Test.java",
			"class Test implements ITest {\n" +
			"	@Override\n" +
			"	public <T> @org.eclipse.jdt.annotation.Nullable T foo(T arg) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n",
			"Test2.java",
			"class Test2 implements ITest {\n" +
			"	@Override\n" +
			"	public <T> T foo(@org.eclipse.jdt.annotation.NonNull T arg) {\n" +
			"		return arg;\n" +
			"	}\n" +
			"}\n",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Test.java (at line 3)\n" +
		"	public <T> @org.eclipse.jdt.annotation.Nullable T foo(T arg) {\n" +
		"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The return type is incompatible with the free type variable 'T' returned from ITest.foo(T) (mismatching null constraints)\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in Test2.java (at line 3)\n" +
		"	public <T> T foo(@org.eclipse.jdt.annotation.NonNull T arg) {\n" +
		"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter arg, inherited method from ITest does not constrain this parameter\n" +
		"----------\n");
}
// Bug 438467 - [compiler][null] Better error position for "The method _ cannot implement the corresponding method _ due to incompatible nullness constraints"
public void testTypeVariable15a() {
	runNegativeTestWithLibs(
		new String[] {
			"ITest.java",
			"import java.util.List;\n" +
			"interface ITest {\n" +
			"	<T> T foo(List<T> arg); // or arg Class<T> or TypeToken<T> + return TypeAdapter<T>, etc.\n" +
			"}",
			"Test.java",
			"import java.util.List;\n" +
			"class Test implements ITest {\n" +
			"	@Override\n" +
			"	public <T> T foo(List<@org.eclipse.jdt.annotation.NonNull T> arg) {\n" +
			"		return arg.get(0);\n" +
			"	}\n" +
			"}\n",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Test.java (at line 4)\n" +
		"	public <T> T foo(List<@org.eclipse.jdt.annotation.NonNull T> arg) {\n" +
		"	                 ^^^^\n" +
		"Illegal redefinition of parameter arg, inherited method from ITest declares this parameter as \'List<T>\' (mismatching null constraints)\n" +
		"----------\n" +
		"2. INFO in Test.java (at line 5)\n" +
		"	return arg.get(0);\n" +
		"	       ^^^^^^^^^^\n" +
		"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<@NonNull T>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434602
// Possible error with inferred null annotations leading to contradictory null annotations
public void testTypeVariable16() {
	runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"class Y { void doit() {} }\n" +
				"@NonNullByDefault\n" +
				"class X {\n" +
				"	void foo() {\n" +
				"		X x = new X();\n" +
				"		Y y = x.bar(); // Error: Contradictory null annotations before the fix\n" +
				"		y.doit(); // check that @Nullable from bar's declaration has effect on 'y'\n" +
				"	}\n" +
				"\n" +
				"	public <T extends Y> @Nullable T bar() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	y.doit(); // check that @Nullable from bar's declaration has effect on 'y'\n" +
			"	^\n" +
			"Potential null pointer access: The variable y may be null at this location\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434602
// Possible error with inferred null annotations leading to contradictory null annotations
// Method part of parameterized class.
public void testTypeVariable16a() {
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"class Y {}\n" +
				"@NonNullByDefault\n" +
				"public class X <T> {\n" +
				"	void foo() {\n" +
				"		X<Y> x = new X<Y>();\n" +
				"		x.bar(); // Error: Contradictory null annotations before the fix\n" +
				"	}\n" +
				"\n" +
				"	public @Nullable T bar() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"");
}
public void testTypeVariable16b() {
	runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"class Y {}\n" +
				"class Y2 extends Y {}\n" +
				"\n" +
				"class X {\n" +
				"	void foo() {\n" +
				"		X x = new X();\n" +
				"		x.bar(null); // null arg is illegal\n" +
				"	}\n" +
				"	public <T extends @NonNull Y> @Nullable T bar(T t) {\n" +
				"		return null; // OK\n" +
				"	}\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	x.bar(null); // null arg is illegal\n" +
			"	      ^^^^\n" +
			"Null type mismatch: required \'@NonNull Y\' but the provided value is null\n" +
			"----------\n");
}
// Bug 440143 - [1.8][null] one more case of contradictory null annotations regarding type variables
public void testTypeVariable17() {
	runNegativeTestWithLibs(
		new String[] {
			"Test7.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class Test7<@Nullable E> {\n" +
			"	E e;\n" +
			"\n" +
			"	@Nullable\n" +
			"	E test() {\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"	@NonNull\n" +
			"	E getNotNull() {\n" +
			"		if (e == null)\n" +
			"			throw new NullPointerException();\n" +
			"		return e;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Test7.java (at line 15)\n" +
		"	return e;\n" +
		"	       ^\n" +
		"Null type mismatch (type annotations): required \'@NonNull E\' but this expression has type \'@Nullable E\'\n" +
		"----------\n");
}
// Bug 440143 - [1.8][null] one more case of contradictory null annotations regarding type variables
// use local variable to avoid the null type mismatch
public void testTypeVariable17a() {
	runConformTestWithLibs(
		new String[] {
			"Test7.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class Test7<@Nullable E> {\n" +
			"	E e;\n" +
			"\n" +
			"	@Nullable\n" +
			"	E test() {\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"	@NonNull\n" +
			"	E getNotNull() {\n" +
			"		E el = e;\n" +
			"		if (el == null)\n" +
			"			throw new NullPointerException();\n" +
			"		return el;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// NPE reported in https://bugs.eclipse.org/bugs/show_bug.cgi?id=438458#c5
public void testTypeVariable18() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"interface Lib1 {\n" +
			"    <T extends Collection<?>> T constrainedTypeParameter(@NonNull T in);\n" +
			"}\n" +
			"\n" +
			"public class Test {\n" +
			"  @NonNull Collection<?> test4(Lib1 lib, @Nullable Collection<String> in) {\n" +
			"    return lib.constrainedTypeParameter(in);\n" +
			"  }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Test.java (at line 10)\n" +
		"	return lib.constrainedTypeParameter(in);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'Collection<String>\' needs unchecked conversion to conform to \'@NonNull Collection<?>\'\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 10)\n" +
		"	return lib.constrainedTypeParameter(in);\n" +
		"	                                    ^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Collection<String>\' but this expression has type \'@Nullable Collection<String>\'\n" +
		"----------\n");
}
public void testTypeVariable18raw() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"interface Lib1 {\n" +
			"    <T extends Collection<?>> T constrainedTypeParameter(@NonNull T in);\n" +
			"}\n" +
			"\n" +
			"public class Test {\n" +
			"  @SuppressWarnings(\"rawtypes\")\n" +
			"  @NonNull Collection test4(Lib1 lib, @Nullable Collection in) {\n" +
			"    return lib.constrainedTypeParameter(in);\n" +
			"  }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Test.java (at line 11)\n" +
		"	return lib.constrainedTypeParameter(in);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'Collection\' needs unchecked conversion to conform to \'@NonNull Collection\'\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 11)\n" +
		"	return lib.constrainedTypeParameter(in);\n" +
		"	                                    ^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Collection\' but this expression has type \'@Nullable Collection\'\n" +
		"----------\n");
}
// top-level annotation is overridden at use-site, details remain - parameterized type
public void testTypeVariable19() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, CompilerOptions.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"interface I<T,U extends List<T>> {\n" +
			"	U get0();\n" +
			"	@Nullable U get1();\n" +
			"	@NonNull U get2();\n" +
			"}\n" +
			"class X {\n" +
			"	static String test (I<@Nullable String, @NonNull ArrayList<@Nullable String>> i1,\n" +
			"						I<@NonNull String, @Nullable ArrayList<@NonNull String>> i2, int s) {\n" +
			"		switch(s) {\n" +
			"			case 0 : return i1.get0().get(0).toUpperCase(); // problem at detail\n" +
			"			case 1 : return i1.get1().get(0).toUpperCase(); // 2 problems\n" +
			"			case 2 : return i1.get2().get(0).toUpperCase(); // problem at detail\n" +
			"			case 3 : return i2.get0().get(0).toUpperCase(); // problem at top\n" +
			"			case 4 : return i2.get1().get(0).toUpperCase(); // problem at top\n" +
			"			case 5 : return i2.get2().get(0).toUpperCase(); // OK\n" +
			"			default : return \"\";" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		compilerOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	case 0 : return i1.get0().get(0).toUpperCase(); // problem at detail\n" +
		"	                ^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: The method get(int) may return null\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 16)\n" +
		"	case 1 : return i1.get1().get(0).toUpperCase(); // 2 problems\n" +
		"	                ^^^^^^^^^\n" +
		"Potential null pointer access: The method get1() may return null\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 16)\n" +
		"	case 1 : return i1.get1().get(0).toUpperCase(); // 2 problems\n" +
		"	                ^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: The method get(int) may return null\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 17)\n" +
		"	case 2 : return i1.get2().get(0).toUpperCase(); // problem at detail\n" +
		"	                ^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: The method get(int) may return null\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 18)\n" +
		"	case 3 : return i2.get0().get(0).toUpperCase(); // problem at top\n" +
		"	                ^^^^^^^^^\n" +
		"Potential null pointer access: The method get0() may return null\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 19)\n" +
		"	case 4 : return i2.get1().get(0).toUpperCase(); // problem at top\n" +
		"	                ^^^^^^^^^\n" +
		"Potential null pointer access: The method get1() may return null\n" +
		"----------\n");
}
// top-level annotation is overridden at use-site, array with anotations on dimensions
public void testTypeVariable19a() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"interface I1<T> {\n" +
			"	T @Nullable[] get0();\n" +
			"	@Nullable T @NonNull[] get1();\n" +
			"	@Nullable T @Nullable[] get2();\n" +
			"}\n" +
			"interface I2<T> {\n" +
			"	T @NonNull[] get0();\n" +
			"	@NonNull T @NonNull[] get1();\n" +
			"	@NonNull T @Nullable[] get2();\n" +
			"}\n" +
			"class X {\n" +
			"	static String test (I1<@NonNull String> i1, I2<@Nullable String> i2, int s) {\n" +
			"		switch (s) {\n" +
			"			case 0: return i1.get0()[0].toUpperCase(); // problem on array\n" +
			"			case 1: return i1.get1()[0].toUpperCase(); // problem on element\n" +
			"			case 2: return i1.get2()[0].toUpperCase(); // 2 problems\n" +
			"			case 3: return i2.get0()[0].toUpperCase(); // problem on element\n" +
			"			case 4: return i2.get1()[0].toUpperCase(); // OK\n" +
			"			case 5: return i2.get2()[0].toUpperCase(); // problem on array\n" +
			"			default: return \"\";\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 16)\n" +
		"	case 0: return i1.get0()[0].toUpperCase(); // problem on array\n" +
		"	               ^^^^^^^^^\n" +
		"Potential null pointer access: The method get0() may return null\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 17)\n" +
		"	case 1: return i1.get1()[0].toUpperCase(); // problem on element\n" +
		"	               ^^^^^^^^^^^^\n" +
		"Potential null pointer access: array element may be null\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 18)\n" +
		"	case 2: return i1.get2()[0].toUpperCase(); // 2 problems\n" +
		"	               ^^^^^^^^^\n" +
		"Potential null pointer access: The method get2() may return null\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 18)\n" +
		"	case 2: return i1.get2()[0].toUpperCase(); // 2 problems\n" +
		"	               ^^^^^^^^^^^^\n" +
		"Potential null pointer access: array element may be null\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 19)\n" +
		"	case 3: return i2.get0()[0].toUpperCase(); // problem on element\n" +
		"	               ^^^^^^^^^^^^\n" +
		"Potential null pointer access: array element may be null\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 21)\n" +
		"	case 5: return i2.get2()[0].toUpperCase(); // problem on array\n" +
		"	               ^^^^^^^^^\n" +
		"Potential null pointer access: The method get2() may return null\n" +
		"----------\n");
}
public void testTypeVariable20() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"interface I<@Nullable T> { }\n" +
			"public class X implements I<String> {}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public class X implements I<String> {}\n" +
		"	                            ^^^^^^\n" +
		"Null constraint mismatch: The type \'String\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n");
}
public void testBug434600() {
	runConformTestWithLibs(
		new String[] {
			"bug/Main.java",
			"package bug;\n" +
			"public class Main {\n" +
			"	public static void main(final String[] args) {\n" +
			"		System.out.println(\"Hello World\");\n" +
			"	}\n" +
			"}\n",
			"bug/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package bug;\n",
			"bug/ExpressionNode.java",
			"package bug;\n" +
			"\n" +
			"public interface ExpressionNode extends CopyableNode<ExpressionNode> {\n" +
			"	\n" +
			"}\n",
			"bug/ExtendedNode.java",
			"package bug;\n" +
			"\n" +
			"public interface ExtendedNode {\n" +
			"	\n" +
			"}\n",
			"bug/CopyableNode.java",
			"package bug;\n" +
			"\n" +
			"public interface CopyableNode<T extends ExtendedNode> extends ExtendedNode {\n" +
			"	\n" +
			"}\n"
		},
		getCompilerOptions(),
		"",
		"Hello World");
}
public void testBug434600a() {
	runConformTestWithLibs(
		new String[] {
			"I.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface I<S, T extends @Nullable List<@NonNull List<S>>> {\n" +
			"}\n",
			"C.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class C implements I<@Nullable String, @Nullable ArrayList<@NonNull List<@Nullable String>>> {}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug434600a_qualified() {
	runConformTestWithLibs(
		new String[] {
			"p/I.java",
			"package p;\n" +
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface I<S, T extends @Nullable List<@NonNull List<S>>> {\n" +
			"}\n",
			"C.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class C implements p.I<java.lang.@Nullable String, java.util.@Nullable ArrayList<java.util.@NonNull List<java.lang.@Nullable String>>> {}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug434600b() {
	runNegativeTestWithLibs(
		new String[] {
			"I.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface I<S, T extends @NonNull List<@NonNull List<S>>> {\n" +
			"}\n",
			"C.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class C implements I<@Nullable String, ArrayList<@NonNull List<@Nullable String>>> {}\n" +
			"class C1 {\n" +
			"	I<String, @NonNull ArrayList<@Nullable List<String>>> field;\n" +
			"}\n" +
			"class C2 implements I<@NonNull String, @NonNull ArrayList<@NonNull List<@Nullable String>>> {}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in C.java (at line 3)\n" +
		"	public class C implements I<@Nullable String, ArrayList<@NonNull List<@Nullable String>>> {}\n" +
		"	                                              ^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'ArrayList<@NonNull List<@Nullable String>>\' is not a valid substitute for the type parameter \'T extends @NonNull List<@NonNull List<S>>\'\n" +
		"----------\n" +
		"2. ERROR in C.java (at line 5)\n" +
		"	I<String, @NonNull ArrayList<@Nullable List<String>>> field;\n" +
		"	          ^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull ArrayList<@Nullable List<String>>\' is not a valid substitute for the type parameter \'T extends @NonNull List<@NonNull List<S>>\'\n" +
		"----------\n" +
		"3. ERROR in C.java (at line 7)\n" +
		"	class C2 implements I<@NonNull String, @NonNull ArrayList<@NonNull List<@Nullable String>>> {}\n" +
		"	                                       ^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull ArrayList<@NonNull List<@Nullable String>>\' is not a valid substitute for the type parameter \'T extends @NonNull List<@NonNull List<S>>\'\n" +
		"----------\n");
}
public void testBug434600b_qualified() {
	runNegativeTestWithLibs(
		new String[] {
			"p/I.java",
			"package p;\n" +
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface I<S, T extends @Nullable List<@NonNull List<S>>> {\n" +
			"}\n",
			"C.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class C implements p.I<@Nullable String, ArrayList<@NonNull List<@Nullable String>>> {}\n" +
			"class C1 {\n" +
			"	p.I<String, @Nullable ArrayList<@Nullable List<String>>> field;\n" +
			"}\n" +
			"class C2 implements p.I<@NonNull String, @Nullable ArrayList<@NonNull List<@Nullable String>>> {}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in C.java (at line 5)\n" +
		"	p.I<String, @Nullable ArrayList<@Nullable List<String>>> field;\n" +
		"	            ^^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable ArrayList<@Nullable List<String>>\' is not a valid substitute for the type parameter \'T extends @Nullable List<@NonNull List<S>>\'\n" +
		"----------\n" +
		"2. ERROR in C.java (at line 7)\n" +
		"	class C2 implements p.I<@NonNull String, @Nullable ArrayList<@NonNull List<@Nullable String>>> {}\n" +
		"	                                         ^^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable ArrayList<@NonNull List<@Nullable String>>\' is not a valid substitute for the type parameter \'T extends @Nullable List<@NonNull List<S>>\'\n" +
		"----------\n");
}
public void testBug435399() {
	runConformTestWithLibs(
		new String[] {
			"bug/Bug1.java",
			"package bug;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class Bug1 {\n" +
			"    public static <T> void method(@Nullable T value, T defaultValue) {\n" +
			"    }\n" +
			"    public void invoke() {\n" +
			"        method(Integer.valueOf(1), Boolean.TRUE);\n" +
			"    }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug435962() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"interfaces/CopyableNode.java",
			"package interfaces;\n" +
			"public interface CopyableNode<T extends ExtendedNode> extends ExtendedNode {\n" +
			"	public T deepCopy();\n" +
			"}\n",
			"interfaces/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package interfaces;\n",
			"interfaces/ExtendedNode.java",
			"package interfaces;\n" +
			"import java.util.ArrayList;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"public interface ExtendedNode {\n" +
			"	ExtendedNode getParent();\n" +
			"	void setParent(ExtendedNode newParent);\n" +
			"	int numChildren();\n" +
			"	void mutateNode(ExtendedNode root);\n" +
			"	void getAllNodes(ArrayList<ExtendedNode> array);\n" +
			"	ExtendedNode getNode(int nodeIndex);\n" +
			"	<N extends ExtendedNode> void getNodesOfType(Class<N> desiredType,\n" +
			"			ArrayList<N> array);\n" +
			"	<N extends ExtendedNode> @Nullable N getRandomNodeOfType(\n" +
			"			Class<N> desiredType, ExtendedNode root, ExtendedNode caller);\n" +
			"}\n",
			"interfaces/ValueNode.java",
			"package interfaces;\n" +
			"public interface ValueNode extends ExtendedNode {\n" +
			"}\n",
			"framework/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package framework;\n",
			"framework/BinaryOpNode.java",
			"package framework;\n" +
			"\n" +
			"import interfaces.CopyableNode;\n" +
			"import interfaces.ValueNode;\n" +
			"public abstract class BinaryOpNode<T extends ValueNode & CopyableNode<T>, O>\n" +
			"		extends EqualBinaryNode<T> implements ValueNode {\n" +
			"	@SuppressWarnings(\"unused\") private O op;\n" +
			"	\n" +
			"	protected BinaryOpNode(final T left, @org.eclipse.jdt.annotation.NonNull final O op, final T right) {\n" +
			"		super(left, right);\n" +
			"		this.op = op;\n" +
			"	}\n" +
			"}\n",
			"framework/EqualBinaryNode.java",
			"package framework;\n" +
			"\n" +
			"import interfaces.CopyableNode;\n" +
			"import interfaces.ExtendedNode;\n" +
			"public abstract class EqualBinaryNode<T extends ExtendedNode & CopyableNode<T>>\n" +
			"		implements ExtendedNode {\n" +
			"	protected T left;\n" +
			"	protected T right;\n" +
			"	\n" +
			"	protected EqualBinaryNode(final T left, final T right) {\n" +
			"		this.left = left;\n" +
			"		this.right = right;\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"");
}
public void testBug440462() {
	runConformTestWithLibs(
		new String[]{
			"CompilerError.java",
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"import java.util.*;\n" +
			"@NonNullByDefault\n" +
			"public class CompilerError {\n" +
			"\n" +
			"    List<@Nullable ? extends Integer> list = new ArrayList<@Nullable Integer>();\n" + // FIXME: should be able to use diamond!
			"\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug440773() {
	runConformTestWithLibs(
		new String[] {
			"CountingComparator.java",
			"import java.util.Comparator;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class CountingComparator<T> implements Comparator<T> {\n" +
			"\n" +
			"    private int m_accessCount = 0;\n" +
			"\n" +
			"    private final Comparator<T> m_wrapped;\n" +
			"\n" +
			"    public CountingComparator(final Comparator<T> wrapped) {\n" +
			"        m_wrapped = wrapped;\n" +
			"    }\n" +
			"\n" +
			"    @Override\n" +
			"    @NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"    public int compare(final T element1, final T element2) {\n" +
			"        m_accessCount++;\n" +
			"        return m_wrapped.compare(element1, element2);\n" +
			"    }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug439298_comment2() {
	runConformTestWithLibs(
		new String[] {
			"Extract.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"class R<T> {\n" +
			"	R(@Nullable T t) {}\n" +
			"}\n" +
			"class A {}\n" +
			"@NonNullByDefault\n" +
			"public class Extract {\n" +
			"	R<A> test() {\n" +
			"		return new R<A>(null);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug439298_comment3() {
	runWarningTestWithLibs(
		true,
		new String[] {
			"Extract.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"class R<T> {\n" +
			"	R(@Nullable T t) {}\n" +
			"}\n" +
			"class A {}\n" +
			"public class Extract {\n" +
			"	R<A> test() {\n" +
			"		return new R<@NonNull A>(null);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. INFO in Extract.java (at line 9)\n" +
		"	return new R<@NonNull A>(null);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unsafe null type conversion (type annotations): The value of type '@NonNull R<@NonNull A>' is made accessible using the less-annotated type 'R<A>'\n" +
		"----------\n");
}
public void testBug439298_comment4() {
	runConformTestWithLibs(
		new String[] {
			"Extract.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"class R<T> {\n" +
			"    R(@Nullable T t) {}\n" +
			"}\n" +
			"class A {}\n" +
			"public class Extract {\n" +
			"    R<@NonNull A> test() {\n" +
			"        return new R<>(null);\n" +
			"    }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// this code raised: java.lang.IllegalArgumentException: Type doesn't have its own method?
// at org.eclipse.jdt.internal.compiler.lookup.SyntheticFactoryMethodBinding.applyTypeArgumentsOnConstructor(SyntheticFactoryMethodBinding.java:40)
public void testBug440764() {
	runNegativeTestWithLibs(
		new String[] {
			"Extract.java",
			"import java.util.Comparator;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault({ DefaultLocation.TYPE_PARAMETER })\n" +
			"public class Extract<T> implements Comparator<@NonNull T>  {\n" + // FIXME: annot on 'T' shouldn't be needed
			"	public Extract(Comparator<T> wrapped) {\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	public int compare(T o1, T o2) {\n" +
			"		return 0;\n" +
			"	}\n" +
			"	\n" +
			"	void test(final Comparator<@Nullable Integer> c) {\n" +
			"		new Extract<>(c).compare(1, null);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Extract.java (at line 16)\n" +
		"	new Extract<>(c).compare(1, null);\n" +
		"	              ^\n" +
		"Null type mismatch (type annotations): required \'Comparator<@NonNull Integer>\' but this expression has type \'Comparator<@Nullable Integer>\'\n" +
		"----------\n" +
		"2. ERROR in Extract.java (at line 16)\n" +
		"	new Extract<>(c).compare(1, null);\n" +
		"	                            ^^^^\n" +
		"Null type mismatch: required \'@NonNull Integer\' but the provided value is null\n" +
		"----------\n");
}
public void testBug440759a() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X<T> {\n" +
			"	T test(T t) {\n" +
			"		@NonNull T localT = t; // err#1\n" +
			"		return null; // err must mention free type variable, not @NonNull\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	@NonNull T localT = t; // err#1\n" +
		"	                    ^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	return null; // err must mention free type variable, not @NonNull\n" +
		"	       ^^^^\n" +
		"Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'\n" +
		"----------\n");
}
// involves overriding, work done in ImplicitNullAnnotationVerifier.checkNullSpecInheritance()
public void testBug440759b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface Y<T> {\n" +
			"	T test(T t);\n" +
			"}\n" +
			"@NonNullByDefault\n" +
			"public class X<T> implements Y<T> {\n" +
			"	public T test(T t) {\n" +
			"		@NonNull T localT = t; // err#1\n" +
			"		return null; // err must mention free type variable, not @NonNull\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	@NonNull T localT = t; // err#1\n" +
		"	                    ^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	return null; // err must mention free type variable, not @NonNull\n" +
		"	       ^^^^\n" +
		"Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'\n" +
		"----------\n");
}
public void testBug438383() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"import java.util.*;\n" +
			"import java.util.function.Supplier;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault public class Foo {\n" +
			"    static void foo(Supplier<List<?>> f) { }\n" +
			"    \n" +
			"    static void test() {\n" +
			"        foo(ArrayList::new);\n" +
			"    }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug437270() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Foo {\n" +
			"	void test(String[] arguments) {\n" +
			"		if (arguments != null) {\n" +
			"			String @NonNull [] temp = arguments;\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug437270_comment3() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Foo {\n" +
			"    void test()  {\n" +
			"        @NonNull Object b = new Object();\n" +
			"        Object @NonNull[] c = { new Object() };\n" +
			"        \n" +
			"        test2( b );\n" +
			"        test3( c );\n" +
			"    }\n" +
			"    \n" +
			"    void test2(@Nullable Object z)  {  }\n" +
			"    \n" +
			"    void test3(Object @Nullable[] z)  {  }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug435841() {
	runConformTestWithLibs(
		new String[] {
			"ArrayProblem.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class ArrayProblem {\n" +
			"	private String[] data = new String[0];\n" +
			"	\n" +
			"	void error1() {\n" +
			"		foo(data);  // Compiler error: required 'String @Nullable[]', but this expression has type 'String @NonNull[]'\n" +
			"	}\n" +
			"	\n" +
			"	private String[] foo(String @Nullable[] input) {\n" +
			"		return new String[0];\n" +
			"	}\n" +
			"	\n" +
			"	String @Nullable[] error2() {\n" +
			"		String @NonNull[] nonnull = new String[0];\n" +
			"		return nonnull;  // Compiler error: required 'String @Nullable[]' but this expression has type 'String @NonNull[]'\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug441693() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault({})\n" +
			"public abstract class Foo {\n" +
			"    \n" +
			"    abstract <T> @NonNull T requireNonNull(@Nullable T obj);\n" +
			"    \n" +
			"    @NonNull Iterable<@NonNull String> iterable;\n" +
			"    \n" +
			"    Foo(@Nullable Iterable<@NonNull String> iterable) {\n" +
			"        this.iterable = requireNonNull(iterable); // (*)\n" +
			"    }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug441693other() {
	runNegativeTestWithLibs(
		new String[] {
			"Foo.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"import java.util.*;\n" +
			"\n" +
			"@NonNullByDefault({})\n" +
			"public abstract class Foo {\n" +
			"    \n" +
			"    abstract <T> @NonNull T requireNonNull(@Nullable T obj);\n" +
			"    \n" +
			"    @NonNull String @NonNull[] array;\n" +
			"    \n" +
			"    Foo(@NonNull String @Nullable[] arr) {\n" +
			"        this.array = requireNonNull(arr); // (*)\n" +
			"    }\n" +
			"    @NonNull Foo testWild1(@Nullable List<? extends @NonNull Foo> foos) {\n" +
			"        return requireNonNull(foos).get(0);\n" +
			"    }\n" +
			"    @NonNull Foo testWild2(@Nullable List<@Nullable ? extends List<@NonNull Foo>> foos) {\n" +
			"        return requireNonNull(foos.get(0)).get(0);\n" +
			"    }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. INFO in Foo.java (at line 17)\n" +
		"	return requireNonNull(foos).get(0);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull List<capture#of ? extends @NonNull Foo>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind\n" +
		"----------\n" +
		"2. INFO in Foo.java (at line 20)\n" +
		"	return requireNonNull(foos.get(0)).get(0);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<@NonNull Foo>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind\n" +
		"----------\n" +
		"3. ERROR in Foo.java (at line 20)\n" +
		"	return requireNonNull(foos.get(0)).get(0);\n" +
		"	                      ^^^^\n" +
		"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439158, [1.8][compiler][null] Adding null annotation to return type causes IllegalStateException and sometimes InvocationTargetException
public void testBug439158() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"import java.util.Collection;\n" +
			"import java.util.List;\n" +
			"import java.util.Set;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class Test {\n" +
			"	class X {\n" +
			"		\n" +
			"	}\n" +
			"	\n" +
			"	public static <C extends Collection<?>, A extends C, B extends C>\n" +
			"			@Nullable A transform(B arg) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		List<X> list = null;\n" +
			"		Set<X> result = transform(list);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434579, [1.8][compiler][null] Annotation-based null analysis causes incorrect type errors
public void testBug434579() {
	Map options = getCompilerOptions();
	runConformTestWithLibs(
		new String[] {
			"AbstractNode.java",
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
		    "@NonNullByDefault\n" +
			"interface ExtendedNode {\n" +
			"	ExtendedNode getParent();\n" +
			"	void setParent(ExtendedNode newParent);\n" +
			"}\n" +
			"@NonNullByDefault\n" +
			"public class AbstractNode implements ExtendedNode {\n" +
			"	private ExtendedNode parent;\n" +
			"	protected AbstractNode() {\n" +
			"		parent = this;\n" +
			"	}\n" +
			"	@Override\n" +
			"	public ExtendedNode getParent() {\n" +
			"		return parent;\n" +
			"	}\n" +
			"	@Override\n" +
			"	public void setParent(final ExtendedNode newParent) {\n" +
			"		parent = newParent;\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"");
	runNegativeTestWithLibs(
		new String[] {
			"UnequalBinaryNode.java",
			"public class UnequalBinaryNode<L extends ExtendedNode, R extends ExtendedNode>\n" +
			"		extends AbstractNode {\n" +
			"	private L left;\n" +
			"	private R right;\n" +
			"	public UnequalBinaryNode(final L initialLeft, final R initialRight) {\n" +
			"		left = initialLeft;\n" +
			"		right = initialRight;\n" +
			"		left.setParent(this);\n" +
			"		right.setParent(this); // error on this line without fix\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. ERROR in UnequalBinaryNode.java (at line 8)\n" +
		"	left.setParent(this);\n" +
		"	^^^^\n" +
		"Potential null pointer access: this expression has type \'L\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. ERROR in UnequalBinaryNode.java (at line 9)\n" +
		"	right.setParent(this); // error on this line without fix\n" +
		"	^^^^^\n" +
		"Potential null pointer access: this expression has type \'R\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=434582,
//[1.8][compiler][null] @Nullable annotation in type parameter causes NullPointerException in JDT core
public void testBug434582() {
	runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"@NonNullByDefault\n" +
				"class ProgramNode {}\n" +
				"@NonNullByDefault\n" +
				"interface ConcreteNodeVisitor<R, P> {\n" +
				"	R visit(ProgramNode node, P extraParameter);\n" +
				"}\n" +
				"public class X implements\n" +
				"		ConcreteNodeVisitor<Boolean, @Nullable Object> {\n" +
				"	public Boolean visit(ProgramNode node, Object extraParameter) {\n" +
				"		return Boolean.FALSE;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 11)\n" +
			"	public Boolean visit(ProgramNode node, Object extraParameter) {\n" +
			"	                     ^^^^^^^^^^^\n" +
			"Missing non-null annotation: inherited method from ConcreteNodeVisitor<Boolean,Object> specifies this parameter as @NonNull\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	public Boolean visit(ProgramNode node, Object extraParameter) {\n" +
			"	                                       ^^^^^^\n" +
			"Missing nullable annotation: inherited method from ConcreteNodeVisitor<Boolean,Object> specifies this parameter as @Nullable\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=434582,
//[1.8][compiler][null] @Nullable annotation in type parameter causes NullPointerException in JDT core
public void testBug434582a() {
	runNegativeTestWithLibs(
		new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"@NonNullByDefault\n" +
				"class ProgramNode {}\n" +
				"@NonNullByDefault\n" +
				"interface ConcreteNodeVisitor<R, P> {\n" +
				"	void visit(ProgramNode node, P extraParameter);\n" +
				"}\n" +
				"public class X implements\n" +
				"		ConcreteNodeVisitor<Boolean, @Nullable Object> {\n" +
				"	public void visit(ProgramNode node, Object extraParameter) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 11)\n" +
			"	public void visit(ProgramNode node, Object extraParameter) {}\n" +
			"	                  ^^^^^^^^^^^\n" +
			"Missing non-null annotation: inherited method from ConcreteNodeVisitor<Boolean,Object> specifies this parameter as @NonNull\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	public void visit(ProgramNode node, Object extraParameter) {}\n" +
			"	                                    ^^^^^^\n" +
			"Missing nullable annotation: inherited method from ConcreteNodeVisitor<Boolean,Object> specifies this parameter as @Nullable\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=443467, [1.8][null]InternalError: Unexpected binding type
public void test443467() throws Exception {
	runNegativeTest(
		new String[] {
			"BuildIdeMain.java",
			"import java.nio.file.Path;\n" +
			"import java.time.Instant;\n" +
			"import java.util.AbstractMap.SimpleEntry;\n" +
			"import java.util.HashMap;\n" +
			"import java.util.stream.Stream;\n" +
			"\n" +
			"public class BuildIdeMain {\n" +
			"static void writeUpdates(Stream<Path> filter2, HashMap<Path, SimpleEntry<byte[], Instant>> ideFiles, HashMap<Path, Path> updateToFile) {\n" +
			"   filter2.map(p -> new SimpleEntry<>(updateToFile.get(p), p->ideFiles.get(p)));\n" +
			"}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in BuildIdeMain.java (at line 9)\n" +
		"	filter2.map(p -> new SimpleEntry<>(updateToFile.get(p), p->ideFiles.get(p)));\n" +
		"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for SimpleEntry<>\n" +
		"----------\n",
		this.LIBS,
		true/*flush*/);
}
public void testBug445227() {
	runConformTestWithLibs(
		new String[] {
			"Bar.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"class Bar<E extends Bar.Foo<E>> {\n" +
			"    final Iterable<E> list;\n" +
			"\n" +
			"    Bar() {\n" +
			"        this((Iterable<E>) emptyList());\n" +
			"    }\n" +
			"\n" +
			"    Bar(Iterable<E> list) { this.list = list; }\n" +
			"\n" +
			"    private static <X extends Foo<X>> Iterable<X> emptyList() { throw new UnsupportedOperationException(); }\n" +
			"\n" +
			"    interface Foo<F extends Foo<F>> { }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Bar.java (at line 6)\n" +
		"	this((Iterable<E>) emptyList());\n" +
		"	     ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked cast from Iterable<Bar.Foo<Bar.Foo<X>>> to Iterable<E>\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446715, [compiler] org.eclipse.jdt.internal.compiler.lookup.TypeSystem.cacheDerivedType
public void test446715() {
	Map options = getCompilerOptions();
	runConformTestWithLibs(
		new String[] {
			"Y.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"public class Y {\n" +
			"	public Z.ZI @NonNull [] zz = new Z.ZI[0];\n" +
			"}\n",
			"Z.java",
			"public class Z {\n" +
			"	public class ZI {\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"");
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Y y = new Y();\n" +
			"		y.zz = null;\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	y.zz = null;\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'Z.ZI @NonNull[]\' but the provided value is null\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=445669, java.lang.IllegalStateException at org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding.clone
public void test445669() {
	Map options = getCompilerOptions();
	runConformTestWithLibs(
		new String[] {
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault(DefaultLocation.FIELD)\n" +
			"public class Y {\n" +
			"	public Z.ZI zzi = new Z().new ZI();\n" +
			"	public Z z = new Z();\n" +
			"}\n",
			"Z.java",
			"public class Z {\n" +
			"	public class ZI {\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"");
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Y y = new Y();\n" +
			"		y.zzi = null;\n" +
			"       y.z = null;\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	y.zzi = null;\n" +
		"	        ^^^^\n" +
		"Null type mismatch: required \'Z.@NonNull ZI\' but the provided value is null\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	y.z = null;\n" +
		"	      ^^^^\n" +
		"Null type mismatch: required \'@NonNull Z\' but the provided value is null\n" +
		"----------\n");
}
public void testArrayOfArrays() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.Arrays;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"   public static void main(String[] args) {\n" +
			"      String [] @Nullable [] @NonNull [] arr = new String[][][] {};\n" +
			"      ArrayList<String[][]> al = new ArrayList<String [][]>(Arrays.asList(arr));\n" +
			"   }\n" +
			"}\n",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in X.java (at line 6)\n" +
		"	String [] @Nullable [] @NonNull [] arr = new String[][][] {};\n" +
		"	                                         ^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'String[][][]\' needs unchecked conversion to conform to \'String [] @Nullable[] @NonNull[]\'\n" +
		"----------\n");
}
public void testBug447088() {
	runConformTestWithLibs(
		new String[] {
			"FullyQualifiedNullable.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class FullyQualifiedNullable {\n" +
			"	java.lang.@Nullable String text;\n" +
			"	java.lang.@Nullable String getText() {\n" +
			"		return text;\n" +
			"	}\n" +
			"}\n"
		},
		null,
		"");
}
public void testBug448777() {
	runNegativeTestWithLibs(
		new String[] {
			"DoubleInference.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class DoubleInference {\n" +
			"\n" +
			"	@FunctionalInterface\n" +
			"	interface Func<@Nullable T>  {\n" +
			"		T a(T i);\n" +
			"	}\n" +
			"\n" +
			"	<X> X applyWith(Func<X> f, X x) { return x; }\n" +
			"\n" +
			"	@NonNull String test1() {\n" +
			"		return applyWith(i -> i, \"hallo\");\n" +
			"	}\n" +
			"	void test2(Func<String> f1, Func<@NonNull String> f2) {\n" +
			"		f1.a(null);\n" +
			"		f2.a(null);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in DoubleInference.java (at line 10)\n" +
		"	<X> X applyWith(Func<X> f, X x) { return x; }\n" +
		"	                     ^\n" +
		"Null constraint mismatch: The type \'X\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"2. ERROR in DoubleInference.java (at line 13)\n" +
		"	return applyWith(i -> i, \"hallo\");\n" +
		"	                 ^^^^^^\n" +
		"Contradictory null annotations: function type was inferred as \'@Nullable @NonNull String (@Nullable @NonNull String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n" +
		"3. ERROR in DoubleInference.java (at line 15)\n" +
		"	void test2(Func<String> f1, Func<@NonNull String> f2) {\n" +
		"	                ^^^^^^\n" +
		"Null constraint mismatch: The type \'String\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"4. ERROR in DoubleInference.java (at line 15)\n" +
		"	void test2(Func<String> f1, Func<@NonNull String> f2) {\n" +
		"	                                 ^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"5. ERROR in DoubleInference.java (at line 17)\n" +
		"	f2.a(null);\n" +
		"	^^^^^^^^^^\n" +
		"Contradictory null annotations: method was inferred as \'@Nullable @NonNull String a(@Nullable @NonNull String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n");
}
public void testBug446442_comment2a() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface Foo<T, N extends Number> {\n" +
			"	void m(@NonNull N arg2);\n" +
			"\n" +
			"	void m(@Nullable T arg1);\n" +
			"}\n" +
			"\n" +
			"interface Baz extends Foo<Integer, Integer> {}\n" +
			"\n" +
			"class Impl implements Baz {\n" +
			"  public void m(@NonNull Integer i) {}\n" +
			"}\n" +
			"\n" +
			"public class Test {\n" +
			"	Baz baz= x -> {\n" +
			"		x= null;\n" +
			"	}; \n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 11)\n" +
		"	public void m(@NonNull Integer i) {}\n" +
		"	              ^^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter i, inherited method from Foo<Integer,Integer> declares this parameter as @Nullable\n" +
		"----------\n");
}
// swapped order of method declarations
public void testBug446442_comment2b() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface Foo<T, N extends Number> {\n" +
			"	void m(@Nullable T arg1);\n" +
			"\n" +
			"	void m(@NonNull N arg2);\n" +
			"}\n" +
			"\n" +
			"interface Baz extends Foo<Integer, Integer> {}\n" +
			"\n" +
			"class Impl implements Baz {\n" +
			"  public void m(@NonNull Integer i) {}\n" +
			"}\n" +
			"\n" +
			"public class Test {\n" +
			"	Baz baz= x -> {\n" +
			"		x= null;\n" +
			"	}; \n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 11)\n" +
		"	public void m(@NonNull Integer i) {}\n" +
		"	              ^^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter i, inherited method from Foo<Integer,Integer> declares this parameter as @Nullable\n" +
		"----------\n");
}
// inherit from two different supers
public void testBug446442_comment2c() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface Foo0<T, N extends Number> {\n" +
			"	void m(@Nullable T arg1);\n" +
			"}\n" +
			"\n" +
			"interface Foo1<T, N extends Number> {\n" +
			"	void m(@NonNull N arg2);\n" +
			"}\n" +
			"\n" +
			"interface Baz extends Foo1<Integer, Integer>,  Foo0<Integer, Integer> {}\n" +
			"\n" +
			"class Impl implements Baz {\n" +
			"  public void m(@NonNull Integer i) {}\n" +
			"}\n" +
			"\n" +
			"public class Test {\n" +
			"	Baz baz= x -> {\n" +
			"		x= null;\n" +
			"	}; \n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 13)\n" +
		"	public void m(@NonNull Integer i) {}\n" +
		"	              ^^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter i, inherited method from Foo0<Integer,Integer> declares this parameter as @Nullable\n" +
		"----------\n");
}
// merging @NonNull & unannotated in arg-position must answer unannotated
public void testBug446442_2a() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface Foo<T, N extends Number> {\n" +
			"	void m(@NonNull N arg2);\n" +
			"\n" +
			"	void m(T arg1);\n" +
			"}\n" +
			"\n" +
			"interface Baz extends Foo<Integer, Integer> {}\n" +
			"\n" +
			"public class Test {\n" +
			"	Baz baz= x -> {\n" +
			"		@NonNull Object o = x;\n" +
			"	}; \n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Test.java (at line 12)\n" +
		"	@NonNull Object o = x;\n" +
		"	                    ^\n" +
		"Null type safety (type annotations): The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n");
}
// merging @NonNull & unannotated in arg-position must answer unannotated - swapped order
public void testBug446442_2b() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface Foo<T, N extends Number> {\n" +
			"	void m(T arg1);\n" +
			"\n" +
			"	void m(@NonNull N arg2);\n" +
			"}\n" +
			"\n" +
			"interface Baz extends Foo<Integer, Integer> {}\n" +
			"\n" +
			"public class Test {\n" +
			"	Baz baz= x -> {\n" +
			"		@NonNull Object o = x;\n" +
			"	}; \n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Test.java (at line 12)\n" +
		"	@NonNull Object o = x;\n" +
		"	                    ^\n" +
		"Null type safety (type annotations): The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n");
}
// using inherited implementation to fulfill both contracts
public void testBug446442_3() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface Foo<T, N extends Number> {\n" +
			"	void m(@NonNull N arg2);\n" +
			"\n" +
			"	void m(T arg1);\n" +
			"}\n" +
			"\n" +
			"interface Baz extends Foo<Integer, Integer> {}\n" +
			"class Impl {\n" +
			"  public void m(Integer a) {}\n" +
			"}\n" +
			"class BazImpl extends Impl implements Baz {}\n" +
			"\n" +
			"public class Test {\n" +
			"	void test(BazImpl b) {\n" +
			"		b.m(null);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// unsuccessful attempt to trigger use of MostSpecificExceptionMethodBinding
public void testBug446442_4() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface Foo<T, N extends Number> {\n" +
			"	abstract void m(@NonNull N arg2) throws Exception;\n" +
			"\n" +
			"	default void m(T arg1) throws java.io.IOException {}\n" +
			"}\n" +
			"\n" +
			"interface Baz extends Foo<Integer, Integer> {}\n" +
			"abstract class Impl {\n" +
			"  public void m(Integer a) throws java.io.IOException {}\n" +
			"}\n" +
			"class BazImpl extends Impl implements Baz {}\n" +
			"\n" +
			"public class Test {\n" +
			"	void test(BazImpl b) throws java.io.IOException {\n" +
			"		b.m(null);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// annotated return types
public void testBug446442_5() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface Foo<T, N extends Number> {\n" +
			"	T m(T t);\n" +
			"\n" +
			"	@NonNull N m(N n);\n" +
			"}\n" +
			"\n" +
			"interface Baz extends Foo<Integer, Integer> {}\n" +
			"\n" +
			"class Impl implements Baz {\n" +
			"  public Integer m(Integer i) { return Integer.valueOf(0); }\n" +
			"}\n" +
			"\n" +
			"public class Test {\n" +
			"	Baz baz= x -> null;\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 11)\n" +
		"	public Integer m(Integer i) { return Integer.valueOf(0); }\n" +
		"	       ^^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Integer\' returned from Foo<Integer,Integer>.m(Integer) (mismatching null constraints)\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 15)\n" +
		"	Baz baz= x -> null;\n" +
		"	              ^^^^\n" +
		"Null type mismatch: required \'@NonNull Integer\' but the provided value is null\n" +
		"----------\n");
}
// conflicting annotations on type arguments
public void testBug446442_6a() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"interface Foo<T,C1 extends Collection<T>, C2 extends List<T>> {\n" +
			"	void m(C1 a1);\n" +
			"\n" +
			"	void m(C2 a2);\n" +
			"}\n" +
			"\n" +
			"interface Baz extends Foo<Integer, ArrayList<@NonNull Integer>, ArrayList<@Nullable Integer>> {}\n" +
			"\n" +
			"class Impl implements Baz {\n" +
			"  public void m(ArrayList<@NonNull Integer> i) {} // contradictory type cannot be implemented\n" +
			"}\n" +
			"\n" +
			"public class Test {\n" +
			"	Baz baz= x -> { // contradictory type cannot be used as SAM\n" +
			"		x.add(null); // contradictory type cause errors at call sites\n" +
			"	}; \n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 12)\n" +
		"	public void m(ArrayList<@NonNull Integer> i) {} // contradictory type cannot be implemented\n" +
		"	              ^^^^^^^^^\n" +
		"Illegal redefinition of parameter i, inherited method from Foo<Integer,ArrayList<Integer>,ArrayList<Integer>> declares this parameter as \'ArrayList<@Nullable Integer>\' (mismatching null constraints)\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 16)\n" +
		"	Baz baz= x -> { // contradictory type cannot be used as SAM\n" +
		"		x.add(null); // contradictory type cause errors at call sites\n" +
		"	}; \n" +
		"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Contradictory null annotations: function type was inferred as \'void (ArrayList<@NonNull @Nullable Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n" +
		"3. ERROR in Test.java (at line 17)\n" +
		"	x.add(null); // contradictory type cause errors at call sites\n" +
		"	^^^^^^^^^^^\n" +
		"Contradictory null annotations: method was inferred as \'boolean add(@NonNull @Nullable Integer)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n");
}
// swapped order of method declarations + added return type
public void testBug446442_6b() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"interface Foo<T,C1 extends Collection<T>, C2 extends List<T>> {\n" +
			"	C2 m(C2 a2);\n" +
			"\n" +
			"	C1 m(C1 a1);\n" +
			"}\n" +
			"\n" +
			"interface Baz extends Foo<Integer, ArrayList<@NonNull Integer>, ArrayList<@Nullable Integer>> {}\n" +
			"\n" +
			"class Impl implements Baz {\n" +
			"  public ArrayList<@NonNull Integer> m(ArrayList<@Nullable Integer> i) { return i; }\n" +
			"}\n" +
			"\n" +
			"public class Test {\n" +
			"	Baz baz= x -> {\n" +
			"		x.add(null);\n" +
			"		x.get(0);\n" +
			"		return x;\n" +
			"	};\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 12)\n" +
		"	public ArrayList<@NonNull Integer> m(ArrayList<@Nullable Integer> i) { return i; }\n" +
		"	       ^^^^^^^^^\n" +
		"The return type is incompatible with \'ArrayList<@Nullable Integer>\' returned from Foo<Integer,ArrayList<Integer>,ArrayList<Integer>>.m(ArrayList<Integer>) (mismatching null constraints)\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 12)\n" +
		"	public ArrayList<@NonNull Integer> m(ArrayList<@Nullable Integer> i) { return i; }\n" +
		"	                                     ^^^^^^^^^\n" +
		"Illegal redefinition of parameter i, inherited method from Foo<Integer,ArrayList<Integer>,ArrayList<Integer>> declares this parameter as \'ArrayList<@NonNull Integer>\' (mismatching null constraints)\n" +
		"----------\n" +
		"3. ERROR in Test.java (at line 12)\n" +
		"	public ArrayList<@NonNull Integer> m(ArrayList<@Nullable Integer> i) { return i; }\n" +
		"	                                                                              ^\n" +
		"Null type mismatch (type annotations): required \'ArrayList<@NonNull Integer>\' but this expression has type \'ArrayList<@Nullable Integer>\'\n" +
		"----------\n" +
		"4. ERROR in Test.java (at line 16)\n" +
		"	Baz baz= x -> {\n" +
		"		x.add(null);\n" +
		"		x.get(0);\n" +
		"		return x;\n" +
		"	};\n" +
		"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Contradictory null annotations: function type was inferred as \'ArrayList<@NonNull @Nullable Integer> (ArrayList<@Nullable @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n" +
		"5. ERROR in Test.java (at line 17)\n" +
		"	x.add(null);\n" +
		"	^^^^^^^^^^^\n" +
		"Contradictory null annotations: method was inferred as \'boolean add(@Nullable @NonNull Integer)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n" +
		"6. ERROR in Test.java (at line 18)\n" +
		"	x.get(0);\n" +
		"	^^^^^^^^\n" +
		"Contradictory null annotations: method was inferred as \'@Nullable @NonNull Integer get(int)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n");
}
public void testBug453475() {
	runConformTestWithLibs(
		new String[] {
			"TestMap.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public abstract class TestMap extends AbstractMap<String,@Nullable String> {\n" +
			"\n" +
			"}\n"
		}, null, "");
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"\n" +
			"  public static final void test(TestMap testMap) {\n" +
			"    testMap.putAll(new HashMap<String,@Nullable String>()); // Error: Contradictory null annotations: method was inferred as 'void putAll(Map<? extends @NonNull String,? extends @NonNull @Nullable String>)', but only one of '@NonNull' and '@Nullable' can be effective at any location\n" +
			"  }\n" +
			"\n" +
			"}\n"
		}, null, "");
}
// also: don't apply default to use of type variable
public void testBug453475a() {
	runConformTestWithLibs(
		new String[] {
			"NamespaceStorage.java",
			"import java.util.*;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public interface NamespaceStorage<T> \n" +
			"{\n" +
			"\n" +
			"	Set<T> getObjects(); \n" + // here <T> was wrongly read from .class as <@NonNull T>
			"	T getObject(T in);\n" +
			"}\n"
		}, null, "");
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"NamespaceStorageImpl.java",
			"import java.util.*;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class NamespaceStorageImpl<T> implements NamespaceStorage<T>\n" +
			"{\n" +
			"	@Override\n" +
			"	public  Set<T> getObjects() \n" +
			"	{\n" +
			"		return new TreeSet<T>();\n" +
			"	}\n" +
			"	@Override\n" +
			"	public T getObject(T in)\n" +
			"	{\n" +
			"		return in;\n" +
			"	}\n" +
			"}\n"
		},
		null, "");
}
// also: don't apply default to wildcard
public void testBug453475b() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public interface X {\n" +
			"\n" +
			"	void test(List<?> list);\n" +  // here <?> was wrongly read from .class as <@NonNull ?>
			"	\n" +
			"}\n"
		}, null, "");
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"Y.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y {\n" +
			"	public void run(X x, @NonNull List<@Nullable String> s) \n" +
			"	{\n" +
			"		x.test(s);\n" +
			"	}\n" +
			"}\n"
		},
		null, "");
}
public void testBug456236() {
	runConformTestWithLibs(
		new String[] {
			"Nullsafe.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"class Nullsafe<T> {\n" +
			"	final @Nullable T t;\n" +
			"\n" +
			"	Nullsafe(@Nullable T t) {\n" +
			"		this.t = t;\n" +
			"	}\n" +
			"	public static <U> Nullsafe<U> of(@Nullable U u) {\n" +
			"		return new Nullsafe<>(u); // compile error\n" +
			"	}\n" +
			"}\n"
		},
		null,
		"");
}

public void testBug456497() throws Exception {
	runConformTestWithLibs(
		new String[] {
			"libs/Lib1.java",
			"package libs;\n" +
			"\n" +
			"import java.util.Collection;\n" +
			"import java.util.Iterator;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public interface Lib1 {\n" +
			"	<T> Iterator<T> unconstrainedTypeArguments1(Collection<@Nullable T> in);\n" +
			"	Iterator<@NonNull String> unconstrainedTypeArguments2(Collection<String> in);\n" +
			"}\n",
			"tests/Test1.java",
			"package tests;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"import java.util.Collection;\n" +
			"import java.util.Iterator;\n" +
			"\n" +
			"import libs.Lib1;\n" +
			"\n" +
			"public class Test1 {\n" +
			"	Iterator<@NonNull String> test1(Lib1 lib, Collection<@Nullable String> coll) {\n" +
			"		return lib.unconstrainedTypeArguments1(coll);\n" +
			"	}\n" +
			"	Iterator<@NonNull String> test2(Lib1 lib, Collection<@Nullable String> coll) {\n" +
			"		return lib.unconstrainedTypeArguments2(coll);\n" +
			"	}\n" +
			"}\n"
		},
		null,
		"");
}
// original case
public void testBug456487a() {
	runConformTestWithLibs(
		new String[]{
			"Optional.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Optional<@NonNull T> {\n" +
			"  @Nullable T value;\n" +
			"  private Optional(T value) { this.value = value; }\n" +
			"  public static <@NonNull T> Optional<T> of(T value) { return new Optional<T>(value); }\n" +
			"  public T get() { \n" +
			"    @Nullable T t = this.value;\n" +
			"    if (t != null) return t; \n" +
			"    throw new RuntimeException(\"No value present\");\n" +
			"  }\n" +
			"  public @Nullable T orElse(@Nullable T other) { return (this.value != null) ? this.value : other; }\n" +
			"}\n"
		},
		null,
		"");
}
// witness for NPE in NullAnnotationMatching.providedNullTagBits:
public void testBug456487b() {
	runNegativeTestWithLibs(
		new String[]{
			"Optional.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Optional<@Nullable T> {\n" +
			"  @Nullable T value;\n" +
			"  private Optional(T value) { this.value = value; }\n" +
			"  public static <@NonNull T> Optional<T> of(T value) { return new Optional<T>(value); }\n" +
			"  public T get() { \n" +
			"    @Nullable T t = this.value;\n" +
			"    if (t != null) return t; \n" +
			"    throw new RuntimeException(\"No value present\");\n" +
			"  }\n" +
			"  public @Nullable T orElse(@Nullable T other) { return (this.value != null) ? this.value : other; }\n" +
			"}\n",
			"OTest.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"class OTest {\n" +
			"  public static void good() {\n" +
			"    Optional<String> os1 = Optional.of(\"yes\");\n" +
			"    @NonNull String s = os1.get();\n" +
			"    @Nullable String ns = os1.orElse(null);\n" +
			"  }\n" +
			"  public static void bad() {\n" +
			"    Optional<String> os = Optional.of(null);\n" +
			"    @NonNull String s = os.orElse(null);\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Optional.java (at line 5)\n" +
		"	public static <@NonNull T> Optional<T> of(T value) { return new Optional<T>(value); }\n" +
		"	                                    ^\n" +
		"Null constraint mismatch: The type \'@NonNull T\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"2. ERROR in Optional.java (at line 5)\n" +
		"	public static <@NonNull T> Optional<T> of(T value) { return new Optional<T>(value); }\n" +
		"	                                                            ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Contradictory null annotations: method was inferred as \'void <init>(@Nullable @NonNull T)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n" +
		"3. ERROR in Optional.java (at line 5)\n" +
		"	public static <@NonNull T> Optional<T> of(T value) { return new Optional<T>(value); }\n" +
		"	                                                                         ^\n" +
		"Null constraint mismatch: The type \'@NonNull T\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in OTest.java (at line 5)\n" +
		"	Optional<String> os1 = Optional.of(\"yes\");\n" +
		"	         ^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"2. ERROR in OTest.java (at line 6)\n" +
		"	@NonNull String s = os1.get();\n" +
		"	                    ^^^^^^^^^\n" +
		"Contradictory null annotations: method was inferred as \'@Nullable @NonNull String get()\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n" +
		"3. ERROR in OTest.java (at line 7)\n" +
		"	@Nullable String ns = os1.orElse(null);\n" +
		"	                      ^^^^^^^^^^^^^^^^\n" +
		"Contradictory null annotations: method was inferred as \'@Nullable @NonNull String orElse(@Nullable @NonNull String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n" +
		"4. ERROR in OTest.java (at line 10)\n" +
		"	Optional<String> os = Optional.of(null);\n" +
		"	         ^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"5. ERROR in OTest.java (at line 10)\n" +
		"	Optional<String> os = Optional.of(null);\n" +
		"	                                  ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"6. ERROR in OTest.java (at line 11)\n" +
		"	@NonNull String s = os.orElse(null);\n" +
		"	                    ^^^^^^^^^^^^^^^\n" +
		"Contradictory null annotations: method was inferred as \'@Nullable @NonNull String orElse(@Nullable @NonNull String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n");
}
public void testBug454182() {

	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annot.NonNullByDefault");
	String[] libs = this.LIBS.clone();
	libs[libs.length-1] = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test454182.jar";
	runConformTest(
		new String[] {
			"p/package-info.java",
			"@annot.NonNullByDefault package p;\n"
		},
		"",
		libs,
		false,
		null,
		options,
		null);
}
public void testBug443870() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface Listener<T> {}\n" +
			"interface I0<T,U extends Listener<T>> {}\n" +
			"interface I1<T> extends I0<T,Listener<T>> {}\n" +
			"class Y<S> {\n" +
			"	private @NonNull I0<S,Listener<S>> f;\n" +
			"	Y (@NonNull I0<S,Listener<S>> in) { this.f = in; }\n" +
			"	@NonNull I0<S,Listener<S>> getI() { return f; }\n" +
			"}\n" +
			"public class X<V> extends Y<V> {\n" +
			"	private @NonNull I1<V> f;\n" +
			"	X (@NonNull I1<V> in) { super(in); this.f = in; }\n" +
			"	@Override\n" +
			"	@NonNull I1<V> getI() { return f; }\n" +
			"}\n"
		},
		null,
		"");
}
public void testBug437072() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	@NonNull int[][] ints = new int[3][4];\n" +
			"	@NonNull int[][] test1() { return new int[3][4]; }\n" +
			"	void test2(@NonNull boolean[][] bools) {\n" +
			"		@NonNull boolean[][] bools2 = bools;\n" +
			"	}\n" +
			"	List<@NonNull int[]> intslist;\n" +
			"	List<@NonNull int> intlist;\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	@NonNull int[][] ints = new int[3][4];\n" +
		"	^^^^^^^^\n" +
		"The nullness annotation @NonNull is not applicable for the primitive type int\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	@NonNull int[][] test1() { return new int[3][4]; }\n" +
		"	^^^^^^^^\n" +
		"The nullness annotation @NonNull is not applicable for the primitive type int\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	void test2(@NonNull boolean[][] bools) {\n" +
		"	           ^^^^^^^^\n" +
		"The nullness annotation @NonNull is not applicable for the primitive type boolean\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 7)\n" +
		"	@NonNull boolean[][] bools2 = bools;\n" +
		"	^^^^^^^^\n" +
		"The nullness annotation @NonNull is not applicable for the primitive type boolean\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 9)\n" +
		"	List<@NonNull int[]> intslist;\n" +
		"	     ^^^^^^^^\n" +
		"The nullness annotation @Nullable is not applicable for the primitive type int\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 10)\n" +
		"	List<@NonNull int> intlist;\n" +
		"	              ^^^\n" +
		"Syntax error, insert \"Dimensions\" to complete ReferenceType\n" +
		"----------\n",
		this.LIBS,
		true/*flush*/);
}
public void testBug448709() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING); // ensure program is runnable
	compilerOptions.put(JavaCore.COMPILER_PB_PESSIMISTIC_NULL_ANALYSIS_FOR_FREE_TYPE_VARIABLES, JavaCore.WARNING); // ensure program is runnable
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class Test {\n" +
			"\n" +
			"  /**\n" +
			"   * A null-annotated version of {@link Objects#requireNonNull(Object)}.\n" +
			"   */\n" +
			"  public static final <T> @NonNull T requireNonNull(final @Nullable T obj) {\n" +
			"    if (obj == null) throw new NullPointerException();\n" +
			"    return obj;\n" +
			"  }\n" +
			"\n" +
			"  /**\n" +
			"   * A null-annotated version of {@link Optional#map(Function)}.\n" +
			"   */\n" +
			"  public static final <T,U> @NonNull Optional<U> map(final @NonNull Optional<T> optional, final Function<@NonNull ? super T,? extends U> mapper) {\n" +
			"    if (!optional.isPresent()) return requireNonNull(Optional.empty());\n" +
			"    final T source = optional.get();\n" +
			"    final U result = mapper.apply(source);\n" +
			"    System.out.println(source+\"->\"+result);\n" +
			"    return requireNonNull(Optional.<U> ofNullable(result));\n" +
			"  }\n" +
			"\n" +
			"  /**\n" +
			"   * A method with a {@link NonNull} {@link DefaultLocation#PARAMETER} and {@link DefaultLocation#RETURN_TYPE}.\n" +
			"   */\n" +
			"  public static final @NonNull Integer testMethod(final @NonNull String s) {\n" +
			"    final Integer r = Integer.valueOf(s);\n" +
			"    if (r == null) throw new NullPointerException();\n" +
			"    return r+1;\n" +
			"  }\n" +
			"\n" +
			"  public static void main(final String[] args) {\n" +
			"    final @NonNull Optional<@Nullable String> optNullableString = requireNonNull(Optional.ofNullable(\"1\"));\n" +
			"\n" +
			"    final Function<@NonNull String,@NonNull Integer> testMethodRef = Test::testMethod;\n" +
			"    map(optNullableString, testMethodRef);\n" +
			"\n" +
			"    map(optNullableString, Test::testMethod); // Error: Null type mismatch at parameter 1: required '@NonNull String' but provided '@Nullable String' via method descriptor Function<String,Integer>.apply(String)\n" +
			"\n" +
			"    map(optNullableString, (s) -> Test.testMethod(s));\n" +
			"  }\n" +
			"\n" +
			"}\n"
		},
		compilerOptions,
		"----------\n" +
		"1. WARNING in Test.java (at line 21)\n" +
		"	final U result = mapper.apply(source);\n" +
		"	                              ^^^^^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. WARNING in Test.java (at line 39)\n" +
		"	map(optNullableString, testMethodRef);\n" +
		"	                       ^^^^^^^^^^^^^\n" +
		"Contradictory null annotations: method was inferred as \'@NonNull Optional<@NonNull Integer> map(@NonNull Optional<@Nullable String>, Function<@NonNull ? super @Nullable String,? extends @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n" +
		"3. WARNING in Test.java (at line 41)\n" +
		"	map(optNullableString, Test::testMethod); // Error: Null type mismatch at parameter 1: required \'@NonNull String\' but provided \'@Nullable String\' via method descriptor Function<String,Integer>.apply(String)\n" +
		"	                       ^^^^^^^^^^^^^^^^\n" +
		"Contradictory null annotations: method was inferred as \'@NonNull Optional<@NonNull Integer> map(@NonNull Optional<@Nullable String>, Function<@NonNull ? super @Nullable String,? extends @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n" +
		"4. WARNING in Test.java (at line 43)\n" +
		"	map(optNullableString, (s) -> Test.testMethod(s));\n" +
		"	                       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Contradictory null annotations: method was inferred as \'@NonNull Optional<@NonNull Integer> map(@NonNull Optional<@Nullable String>, Function<@NonNull ? super @Nullable String,? extends @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" +
		"----------\n" +
		"5. WARNING in Test.java (at line 43)\n" +
		"	map(optNullableString, (s) -> Test.testMethod(s));\n" +
		"	                                              ^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
		"----------\n",
		"1->2\n" +
		"1->2\n" +
		"1->2");
}
public void testBug448709b() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class Test {\n" +
			"\n" +
			"  public static final <T,U> void map(final @NonNull Optional<T> optional, final Function<@NonNull ? super T,? extends U> mapper) {\n" +
			"    final T source = optional.get();\n" +
			"    if (source != null) {\n" +
			"      final U result = mapper.apply(source);\n" +
			"      System.out.println(source+\"->\"+result);\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug459967_Array_constructor() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface FI<T> {\n" +
			"	T @NonNull[] getArray(int size);" +
			"}\n" +
			"public class X {\n" +
			"	void consumer(FI<String> fis) {}\n" +
			"	void test() {\n" +
			"		consumer(String[]::new);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug459967_Array_constructor_b() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface FI<T> {\n" +
			"	@NonNull T @NonNull[] getArray(int size);" +
			"}\n" +
			"public class X {\n" +
			"	void consumer(FI<String> fis) {}\n" +
			"	void test() {\n" +
			"		consumer(String[]::new);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in X.java (at line 7)\n" +
		"	consumer(String[]::new);\n" +
		"	         ^^^^^^^^^^^^^\n" +
		"Null type safety at method return type: Method descriptor FI<String>.getArray(int) promises \'@NonNull String @NonNull[]\' but referenced method provides \'String @NonNull[]\'\n" +
		"----------\n");
}
public void testBug459967_Array_clone() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface FI<T> {\n" +
			"	T @NonNull[] getArray(T[] orig);" +
			"}\n" +
			"public class X {\n" +
			"	void consumer(FI<String> fis) {}\n" +
			"	void test() {\n" +
			"		consumer(String[]::clone);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug459967_Array_clone_b() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface FI<T> {\n" +
			"	@NonNull T @NonNull[] getArray(T[] orig);" +
			"}\n" +
			"public class X {\n" +
			"	void consumer(FI<String> fis) {}\n" +
			"	void test() {\n" +
			"		consumer(String[]::clone);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in X.java (at line 7)\n" +
		"	consumer(String[]::clone);\n" +
		"	         ^^^^^^^^^^^^^^^\n" +
		"Null type safety at method return type: Method descriptor FI<String>.getArray(String[]) promises \'@NonNull String @NonNull[]\' but referenced method provides \'String @NonNull[]\'\n" +
		"----------\n");
}
public void testBug448709_allocationExpression1() {
	// inference prioritizes constraint (<@Nullable T>) over expected type (@NonNull String), hence a null type mismatch results
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface F0<T> {}\n" +
			"class FI<@Nullable T> implements F0<T> {\n" +
			"}\n" +
			"public abstract class X {\n" +
			"	abstract <Z> Z zork(F0<Z> f);\n" +
			"	@NonNull String test() {\n" +
			"		 return zork(new FI<>());\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	return zork(new FI<>());\n" +
		"	            ^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'F0<@NonNull String>\' but this expression has type \'@NonNull FI<@Nullable String>\', corresponding supertype is \'F0<@Nullable String>\'\n" +
		"----------\n");
}
public void testBug448709_allocationExpression2() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"class F {\n" +
			"	<@Nullable U> F(U arg1, U arg2) {}\n" +
			"}\n" +
			"public class X {\n" +
			"	F f = new <@NonNull Integer>F(1,2);\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	F f = new <@NonNull Integer>F(1,2);\n" +
		"	           ^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull Integer\' is not a valid substitute for the type parameter \'@Nullable U\'\n" +
		"----------\n");
}
public void testBug448709_allocationExpression3() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	class F {\n" +
			"		<@Nullable U> F(U arg1, U arg2) {}\n" +
			"	}\n" +
			"	F f = this.new <@NonNull Integer>F(1,2);\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	F f = this.new <@NonNull Integer>F(1,2);\n" +
		"	                ^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull Integer\' is not a valid substitute for the type parameter \'@Nullable U\'\n" +
		"----------\n");
}
public void testBug465513() {
	runConformTestWithLibs(
		new String[] {
			"pack1/A.java",
			"package pack1;\r\n" +
			"import java.math.BigInteger;\r\n" +
			"\r\n" +
			"interface A { Object m(Class c); }\r\n" +
			"interface B<S extends Number> { Object m(Class<S> c); }\r\n" +
			"interface C<T extends BigInteger> { Object m(Class<T> c); }\r\n" +
			"@FunctionalInterface\r\n" +
			"interface D<S,T> extends A, B<BigInteger>, C<BigInteger> {}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in pack1\\A.java (at line 4)\n" +
		"	interface A { Object m(Class c); }\n" +
		"	                       ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n");
}

public void testBug455180() {
    runConformTestWithLibs( // same warning from ecj & javac
    		true/*flush*/,
            new String[] {
                "projA/GenericType.java",
                "package projA;\n"+
                "public class GenericType<T> {\n" +
                "}\n",
                "projA/ClassWithRawUsage.java",
                "package projA;\n"+
                "@org.eclipse.jdt.annotation.NonNullByDefault\n"+
                "public class ClassWithRawUsage {\n"+
                "   public java.util.List<GenericType> method() {\n"+
                "           throw new RuntimeException();\n"+
                "   }\n"+
                "}\n"
            },
            getCompilerOptions(),
            "----------\n" +
    		"1. WARNING in projA\\ClassWithRawUsage.java (at line 4)\n" +
    		"	public java.util.List<GenericType> method() {\n" +
    		"	                      ^^^^^^^^^^^\n" +
            "GenericType is a raw type. References to generic type GenericType<T> should be parameterized\n" +
            "----------\n");
    runConformTestWithLibs( // same warning from ecj & javac
    		false/*flush*/,
            new String[] {
                "projB/ClassThatImports.java",
                "package projB;\n" +
                "import projA.ClassWithRawUsage;\n" +
                "import projA.GenericType;\n" +
                "import org.eclipse.jdt.annotation.*;\n" +
                "public class ClassThatImports {\n" +
                "	void test(ClassWithRawUsage cwru) {\n" +
                "		@NonNull GenericType gt = cwru.method().get(0);\n" +
                "	}\n" +
                "}\n"
            },
            getCompilerOptions(),
            "----------\n" +
    		"1. WARNING in projB\\ClassThatImports.java (at line 7)\n" +
    		"	@NonNull GenericType gt = cwru.method().get(0);\n" +
    		"	         ^^^^^^^^^^^\n" +
    		"GenericType is a raw type. References to generic type GenericType<T> should be parameterized\n" +
    		"----------\n" +
    		"2. INFO in projB\\ClassThatImports.java (at line 7)\n" +
    		"	@NonNull GenericType gt = cwru.method().get(0);\n" +
    		"	                          ^^^^^^^^^^^^^^^^^^^^\n" +
    		"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull List<@NonNull GenericType>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind\n" +
    		"----------\n");
}

public void testBug455180WithOtherAnnotation() {
	runConformTestWithLibs(
			new String[] {
				"proj0/MyAnnotation.java",
				"package proj0;\n"+
				"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)"+
				"@java.lang.annotation.Target({ java.lang.annotation.ElementType.TYPE_USE })"+
				"public @interface MyAnnotation {}"
			}, null, "");
	runConformTestWithLibs( // same warning from ecj & javac
			false/*flush*/,
			new String[] {
				"projA/GenericType.java",
				"package projA;\n"+
				"public class GenericType<T> {\n" +
				"}\n",
				"projA/ClassWithRawUsage.java",
				"package projA;\n"+
				"public class ClassWithRawUsage {\n"+
				"   public java.util.List<@proj0.MyAnnotation GenericType> method() {\n"+
				"      		throw new RuntimeException();\n"+
				"   }\n"+
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in projA\\ClassWithRawUsage.java (at line 3)\n" +
			"	public java.util.List<@proj0.MyAnnotation GenericType> method() {\n" +
			"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"GenericType is a raw type. References to generic type GenericType<T> should be parameterized\n" +
			"----------\n");
	runWarningTestWithLibs(
			false/*flush*/,
			new String[] {
				"projB/ClassThatImports.java",
				"package projB;\n" +
				"import projA.ClassWithRawUsage;\n" +
				"import projA.GenericType;\n" +
				"public class ClassThatImports {\n" +
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in projB\\ClassThatImports.java (at line 2)\n" +
			"	import projA.ClassWithRawUsage;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The import projA.ClassWithRawUsage is never used\n" +
			"----------\n" +
			"2. WARNING in projB\\ClassThatImports.java (at line 3)\n" +
			"	import projA.GenericType;\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"The import projA.GenericType is never used\n" +
			"----------\n");
}
// original test, witnessing NPE
public void testBug466713() {
	runConformTestWithLibs(
		new String[] {
			"Bug.java",
			"class Bug {\n" +
			"    java.util.Iterator<int @org.eclipse.jdt.annotation.Nullable []> x;\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// variant to ensure we are still reporting the error at the other location
public void testBug466713b() {
	runNegativeTestWithLibs(
		new String[] {
			"Bug.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"class Bug {\n" +
			"    java.util.Iterator<@Nullable int @Nullable []> x;\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Bug.java (at line 3)\n" +
		"	java.util.Iterator<@Nullable int @Nullable []> x;\n" +
		"	                   ^^^^^^^^^\n" +
		"The nullness annotation @Nullable is not applicable for the primitive type int\n" +
		"----------\n");
}
// variant to ensure we are not complaining against an unrelated annotation
public void testBug466713c() {
	runConformTestWithLibs(
		new String[] {
			"MyAnnot.java",
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface MyAnnot {}\n",
			"Bug.java",
			"class Bug {\n" +
			"    java.util.Iterator<@MyAnnot int @org.eclipse.jdt.annotation.Nullable []> x;\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// variant for https://bugs.eclipse.org/bugs/show_bug.cgi?id=466713#c5
public void testBug466713d() {
	runNegativeTest(
		new String[] {
			"MyAnnot.java",
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface MyAnnot {}\n",
			"Bug.java",
			"class Bug {\n" +
			"	boolean test(Object o) {\n" +
			"		return o instanceof java.util.Iterator<java.lang. @MyAnnot @org.eclipse.jdt.annotation.Nullable String>;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Bug.java (at line 3)\n" +
		"	return o instanceof java.util.Iterator<java.lang. @MyAnnot @org.eclipse.jdt.annotation.Nullable String>;\n" +
		((this.complianceLevel >= ClassFileConstants.JDK16) ?
				"	       ^\n" +
				"Type Object cannot be safely cast to Iterator<String>\n"
				:
					"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Cannot perform instanceof check against parameterized type Iterator<String>. Use the form Iterator<?> instead since further generic type information will be erased at runtime\n"
				) +
		"----------\n" +
		"2. ERROR in Bug.java (at line 3)\n" +
		"	return o instanceof java.util.Iterator<java.lang. @MyAnnot @org.eclipse.jdt.annotation.Nullable String>;\n" +
		"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Nullness annotations are not applicable at this location \n" +
		"----------\n",
		this.LIBS,
		true/*flush*/);
}
public void testBug466969() {
	runConformTestWithLibs(
		new String[] {
			"GenericType.java",
			"public abstract class GenericType<T extends @org.eclipse.jdt.annotation.NonNull Runnable> {\n" +
			"	abstract T get();\n"+
			"}",
			"WildcardUsage.java",
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault({ ARRAY_CONTENTS, FIELD, PARAMETER, RETURN_TYPE, TYPE_ARGUMENT, TYPE_BOUND, TYPE_PARAMETER })\n" +
			"public class WildcardUsage {\n" +
			"	void f(GenericType<?> p) {\n" +
			"		p.get().run();\n" +
			"	}\n" +
			"}"
			}, getCompilerOptions(), "");
}
public void testBug467032() {
	runConformTestWithLibs(
			new String[] {
				"Class1.java",
				"class Class1 {;\n"+
				"   enum E {}\n"+
				"   void m1(E @org.eclipse.jdt.annotation.Nullable [] a) {}\n"+
				"}\n"
			}, getCompilerOptions(), "");
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"Class2.java",
				"class Class2 {;\n"+
				"  Class1 x;"+
				"}\n"
			}, getCompilerOptions(), "");
}
public void testBug467430() {
	runConformTestWithLibs(
		new String[] {
				"A.java",
				"public class A {\n" +
				"	@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"	void m(java.util.@org.eclipse.jdt.annotation.Nullable Map<String, Integer> map) {\n" +
				"	}\n" +
				"	void m2(A a) {\n" +
				"		final java.util.Map<String, Integer> v = null;\n" +
				"		a.m(v);\n" +
				"	}\n" +
				"}"
			},
			getCompilerOptions(),
			"");
}
public void testBug467430mismatch() {
	runConformTestWithLibs(
		new String[] {
				"A.java",
				"public class A {\n" +
				"	@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"	void m(java.util.@org.eclipse.jdt.annotation.Nullable Map<String, Integer> map) {\n" +
				"	}\n" +
				"	void m2(A a) {\n" +
				"		final java.util.Map<String, @org.eclipse.jdt.annotation.Nullable Integer> v = null;\n" +
				"		a.m(v);\n" +
				"	}\n" +
				"}"
			},
			getCompilerOptions(),
			"");
}
public void testBug467430array() {
	runConformTestWithLibs(
		new String[] {
				"A.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class A {\n" +
				"	@NonNullByDefault\n" +
				"	void m(@NonNull String @Nullable [] array) {\n" +
				"	}\n" +
				"	void m2(A a) {\n" +
				"		final String[] v = null;\n" +
				"		a.m(v);\n" +
				"	}\n" +
				"}"
			},
			getCompilerOptions(),
			"");
}
public void testBug467430arrayMismatch() {
	runConformTestWithLibs(
		new String[] {
				"A.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class A {\n" +
				"	@NonNullByDefault\n" +
				"	void m(@NonNull String @Nullable [] array) {\n" +
				"	}\n" +
				"	void m2(A a) {\n" +
				"		final @Nullable String @Nullable [] v = null;\n" +
				"		a.m(v);\n" +
				"	}\n" +
				"}"
			},
			getCompilerOptions(),
			"");
}

public void testBug446217() {
	Runner runner = new Runner();
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"sol/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package sol;",
			"sol/FuncList.java",
			"\n" +
			"package sol;\n" +
			"\n" +
			"interface FuncList<A> {}\n" +
			"	\n"	+
			"@SuppressWarnings(\"unused\")\n" +
			"final class Node<A> implements FuncList<A> {\n" +
			"	private final A a;\n" +
			"	private final FuncList<A> tail;\n" +
			"	\n" +
			"	Node(final A a, final FuncList<A> tail) {\n" +
			"		this.a = a;\n" +
			"		this.tail = tail;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"final class Empty<A> implements FuncList<A> {\n" +
			"	Empty() {}\n" +
			"}\n",
			"sol/Test.java",
			"package sol;\n" +
			"\n" +
			"public class Test {\n" +
			"	public static void main(final String[] args) {\n" +
			"		 System.out.println(new Node<>(\"A\", new Empty<>()));\n" +
			"	}\n" +
			"}\n"
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("auxiliaryclass");
	runner.runConformTest();
}
public void testBug456584orig() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_PESSIMISTIC_NULL_ANALYSIS_FOR_FREE_TYPE_VARIABLES, JavaCore.WARNING);
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"MyObjects.java",
			"public class MyObjects {\n" +
			"	public static <T> T requireNonNull(T in) { return in; }\n" +
			"}\n",
			"Test.java",
			"\n" +
			"import java.util.function.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"\n" +
			"  public static final <T,R> @NonNull R applyRequired(final T input, final Function<? super T,? extends R> function) { // Warning on '@NonNull R': \"The nullness annotation is redundant with a default that applies to this location\"\n" +
			"    return MyObjects.requireNonNull(function.apply(input));\n" +
			"  }\n" +
			"\n" +
			"}\n"
		},
		compilerOptions,
		"----------\n" +
		"1. WARNING in Test.java (at line 9)\n" +
		"	return MyObjects.requireNonNull(function.apply(input));\n" +
		"	                                ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'capture#2-of ? extends R\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n");
}
public void testBug456584() {
	// the compiler now has special information regarding Objects.requireNonNull
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_PESSIMISTIC_NULL_ANALYSIS_FOR_FREE_TYPE_VARIABLES, JavaCore.WARNING);
	runConformTestWithLibs(
		true/*flush*/,
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"import java.util.function.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"\n" +
			"  public static final <T,R> @NonNull R applyRequired(final T input, final Function<? super T,? extends R> function) { // Warning on '@NonNull R': \"The nullness annotation is redundant with a default that applies to this location\"\n" +
			"    return Objects.requireNonNull(function.apply(input));\n" +
			"  }\n" +
			"\n" +
			"}\n"
		},
		compilerOptions,
		"");
}
public void testBug447661() {
	runConformTestWithLibs(
		new String[] {
			"Two.java",
			"import java.util.*;\n" +
			"public class Two {\n" +
			"\n" +
			"	@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"	public static Set<String> getSet() {\n" +
			"		return new HashSet<>();\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"One.java",
			"import java.util.*;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class One {\n" +
			"\n" +
			"	public void test() {\n" +
			"		Set<String> set = Two.getSet();\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug436091() {
	runConformTestWithLibs(
		new String[] {
			"p/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p;\n",

			"p/Program.java",
			"package p;\n" +
			"public class Program {\n" +
			"	private final ProgramNode program;\n" +
			"	" +
			"	public Program(final ProgramNode astRoot) {\n" +
			"		program = astRoot;\n" +
			"	}\n" +
			"	" +
			"	public Integer execute() {\n" +
			"		return program.accept(ExecutionEvaluationVisitor.VISITOR);\n" +
			"	}\n" +
			"	" +
			"	class ProgramNode {\n" +
			"		public <R> R accept(final ConcreteNodeVisitor<R> visitor) {\n" +
			"			return visitor.visit(this);\n" +
			"		}\n" +
			"	}\n" +
			"}\n",

			"p/ConcreteNodeVisitor.java",
			"package p;\n" +
			"import p.Program.ProgramNode;\n" +
			"public interface ConcreteNodeVisitor<R> {\n" +
			"	R visit(ProgramNode node);\n" +
			"}\n",

			"p/ExecutionEvaluationVisitor.java",
			"package p;\n" +
			"" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"" +
			"import p.Program.ProgramNode;\n" +
			"" +
			"public enum ExecutionEvaluationVisitor implements ConcreteNodeVisitor<Integer> {\n" +
			"	" +
			"	VISITOR;\n" +
			"	" +
			"	@Override" +
			"	public Integer visit(final ProgramNode node) {\n" +
			"		@SuppressWarnings(\"null\")\n" +
			"		@NonNull\n" +
			"		final Integer i = Integer.valueOf(0);\n" +
			"		return i;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	// re-compile only one of the above:
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"p/Program.java",
			"package p;\n" +
			"public class Program {\n" +
			"	private final ProgramNode program;\n" +
			"	" +
			"	public Program(final ProgramNode astRoot) {\n" +
			"		program = astRoot;\n" +
			"	}\n" +
			"	" +
			"	public Integer execute() {\n" +
			"		return program.accept(ExecutionEvaluationVisitor.VISITOR);\n" +
			"	}\n" +
			"	" +
			"	class ProgramNode {\n" +
			"		public <R> R accept(final ConcreteNodeVisitor<R> visitor) {\n" +
			"			return visitor.visit(this);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug474239() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	static String s1 = null, s2 = null;\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		int val = (int) System.currentTimeMillis();\n" +
			"		switch (val % 2) {\n" +
			"		case 0:\n" +
			"			if (s1 != null)\n" +
			"				s2 = \"\";\n" +
			"			break;\n" +
			"		case 1:\n" +
			"			if (s1 != null) // compiler thinks s1 is never null at this point\n" +
			"				throw new RuntimeException(\"\");\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"");
}

public void testBug467482() {
	runConformTestWithLibs(
		new String[]{
			"Util.java",
			"public abstract class Util {\n" +
			"	public static <T> @org.eclipse.jdt.annotation.Nullable T f(T[] valuesArray, java.util.Comparator<T> comparator) {\n" +
			"		@org.eclipse.jdt.annotation.Nullable\n" +
			"		T winner = null;\n" +
			"		for (T value : valuesArray) {\n" +
			"			if (winner == null) {\n" +
			"				winner = value;\n" +
			"			} else {\n" +
			"				if (comparator.compare(winner, value) < 0) {\n" +
			"					winner = value;\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"		return winner;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug467482simple() {
	// reduced example without generics that still exhibits the bug
	runConformTestWithLibs(
		new String[]{
			"Util.java",
			"public abstract class Util {\n" +
				"public static void f(Object unknown) {\n" +
				"	@org.eclipse.jdt.annotation.Nullable\n" +
				"	Object winner = null;\n" +
				"	for (int i = 0; i < 1; i++) {\n" +
				"			winner = unknown;\n" +
				"	}\n" +
				"	if (winner == null) {\n" +
				"		assert false;\n" +
				"	}\n" +
				"}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug467482while() {
	// even simpler with while loop
	runConformTestWithLibs(
		new String[]{
			"Util.java",
			"public abstract class Util {\n" +
				"public static void f(Object unknown, boolean flag) {\n" +
				"	@org.eclipse.jdt.annotation.Nullable\n" +
				"	Object winner = null;\n" +
				"	while (flag) {\n" +
				"			winner = unknown;\n" +
				"			flag = false;\n" +
				"	}\n" +
				"	if (winner == null) {\n" +
				"		assert false;\n" +
				"	}\n" +
				"}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug467482switch() {
	// bug behaviour visible via switch
	runConformTestWithLibs(
		new String[]{
			"Util.java",
			"public abstract class Util {\n" +
				"public static void f(Object unknown, boolean flag) {\n" +
				"	@org.eclipse.jdt.annotation.Nullable\n" +
				"	Object winner = null;\n" +
				"	switch (1) {\n" +
				"	case 1:	winner = unknown;\n" +
				"	}\n" +
				"	if (winner == null) {\n" +
				"		assert false;\n" +
				"	}\n" +
				"}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}

public void testBug467482regression() {
	// simple regression test that verifies that possibly be the patch affected messages stay unchanged
	runNegativeTestWithLibs(
		new String[]{
			"Check.java",
			"public abstract class Check {\n" +
			"	public static void check(@org.eclipse.jdt.annotation.NonNull Object x) {\n" +
			"	}\n" +

			"	public static void f(Object unknown, boolean flag) {\n" +
			"		check(unknown); // expected: null type safety warning\n" +
			"		@org.eclipse.jdt.annotation.Nullable\n" +
			"		Object nullable = unknown;\n" +
			"		check(nullable); // expected: null type mismatch error\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Check.java (at line 5)\n" +
		"	check(unknown); // expected: null type safety warning\n" +
		"	      ^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'Object\' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n" +
		"2. ERROR in Check.java (at line 8)\n" +
		"	check(nullable); // expected: null type mismatch error\n" +
		"	      ^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable Object\'\n" +
		"----------\n");
}
public void testBug484735() {
	runConformTestWithLibs(
		new String[] {
			"test/NullabilityLoopBug.java",
			"package test;\n" +
			"\n" +
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"import java.util.Map.Entry;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class NullabilityLoopBug {\n" +
			"\n" +
			"	public static void main(String[] args)\n" +
			"	{\n" +
			"		Map<String, String> map = new HashMap<>();\n" +
			"		\n" +
			"		map.put(\"key\", \"value\");\n" +
			"		\n" +
			"		System.out.println(getKeyByValue(map, \"value\"));\n" +
			"	}\n" +
			"	\n" +
			"	private static <K, V> K getKeyByValue(Map<K, V> map, @Nullable V value)\n" +
			"	{\n" +
			"		@Nullable K result = null; // some nullability bug? assigning null results in compiler complaining 'result can only be null' below\n" +
			"		for (Entry<K, V> entry : map.entrySet())\n" +
			"		{\n" +
			"			boolean equals;\n" +
			"			if (value == null)\n" +
			"				equals = (entry.getValue() == null);\n" +
			"			else\n" +
			"				equals = value.equals(entry.getValue());\n" +
			"			\n" +
			"			if (equals)\n" +
			"			{\n" +
			"				if (result == null) // Incorrect warning: Redundant null check: The variable result can only be null at this location\n" +
			"					result = entry.getKey();\n" +
			"				else\n" +
			"					throw new IllegalStateException(\"Multiple matches for looking up key via value [\" + value + \"]: [\" + result + \"] and [\" + entry.getKey() + \"]\");\n" +
			"			}\n" +
			"		}\n" +
			"		\n" +
			"		if (result == null) // Incorrect warning: Redundant null check: The variable result can only be null at this location\n" +
			"			throw new IllegalStateException(\"No matches for looking up key via value [\" + value + \"]\");\n" +
			"		\n" +
			"		return result; // Incorrect warning: Dead code\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"",
		"key");
}
public void testBug474239b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	static String s2 = null;\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		int val = (int) System.currentTimeMillis();\n" +
			"		switch (val % 2) {\n" +
			"		case 0:\n" +
			"			s2 = \"\";\n" +
			"			break;\n" +
			"		case 1:\n" +
			"			if (s2 != null)\n" +
			"				throw new RuntimeException(\"\");\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"");
}
public void testBug472663() {
	runConformTestWithLibs(
		new String[] {
			"test/Callee.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Callee {\n" +
			"	public static String staticOtherClass(String foo) {\n" +
			"		return foo;\n" +
			"	}\n" +
			"\n" +
			"	public String instanceOtherClass(String foo) {\n" +
			"		return foo;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	// and now consume Callee.class:
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test/Caller.java",
			"package test;\n" +
			"\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Caller {\n" +
			"	public void foo(final Callee callee) {\n" +
			"		Function<String, String> function;\n" +
			"\n" +
			"		// assignments with warnings (wrong):\n" +
			"		function = Callee::staticOtherClass;\n" +
			"		function = callee::instanceOtherClass;\n" +
			"\n" +
			"		// assignments with no warnings (ok):\n" +
			"		function = foo -> Callee.staticOtherClass(foo);\n" +
			"		function = foo -> callee.instanceOtherClass(foo);\n" +
			"		function = Caller::staticSameClass;\n" +
			"		function = this::instanceSameClass;\n" +
			"	}\n" +
			"\n" +
			"	public static String staticSameClass(String foo) {\n" +
			"		return foo;\n" +
			"	}\n" +
			"\n" +
			"	public String instanceSameClass(String foo) {\n" +
			"		return foo;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug467094() {
	runConformTestWithLibs(
		new String[] {
			"A.java",
			"class A {;\n"+
			"   @org.eclipse.jdt.annotation.NonNull String @org.eclipse.jdt.annotation.Nullable [] x;\n"+
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug467094_local() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"public class X {\n"+
			"	void foo(@org.eclipse.jdt.annotation.NonNull Object[] o) {\n"+
			"		o.hashCode();\n"+
			"		if (o != null) {\n"+
			"			System.out.print(o.toString());\n"+
			"		}\n"+
			"	}\n"+
			"}\n"
		},
		getCompilerOptions(),
		"----------\n"+
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n");
}
public void testBug467094_method() {
	runConformTestWithLibs(
			new String[] {
				"A.java",
				"class A {;\n"+
				"	@org.eclipse.jdt.annotation.NonNull String @org.eclipse.jdt.annotation.Nullable [] m(){\n" +
				"		return null;\n" +
				"	}\n" +
				"	int usage(){\n" +
				"		if(m() == null) return 1; \n" +
				"		return 0; \n" +
				"	}\n"+
				"}\n"
			}, getCompilerOptions(), "");
}
public void testBug440398() {
	runConformTestWithLibs(
		new String[] {
			"NullTest.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault({})\r\n" +
			"public class NullTest {\r\n" +
			"    public static @NonNull Object[] obj = null;\r\n" +
			"    public static void main(String[] args) {\r\n" +
			"        obj = null;\r\n" +
			"        if (obj == null) { // WARNING 1\r\n" +
			"            System.out.println(\"NULL\"); // WARNING 2\r\n" +
			"        }\r\n" +
			"    }\r\n" +
			"}\n"
		},
		getCompilerOptions(),
		"",
		"NULL");
}
public void testBug440398_comment2() {
	runConformTestWithLibs(
		new String[] {
			"MyClass.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault({DefaultLocation.FIELD})\n" +
			"public class MyClass {\n" +
			"    private @NonNull String [] names = new @NonNull String[]{\"Alice\", \"Bob\", \"Charlie\"};\n" +
			"\n" +
			"    public String getName(int index) {\n" +
			"        String name = names[index];\n" +
			"        return name; /* statement A */\n" +
			"    }\n" +
			"}",
		},
		getCompilerOptions(),
		"");
}
public void testBug440398_comment2a() {
	runConformTestWithLibs(
		new String[] {
			"p/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault({org.eclipse.jdt.annotation.DefaultLocation.FIELD})\n" +
			"package p;\n",
			"p/MyClass.java",
			"package p;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class MyClass {\n" +
			"    private @NonNull String [] names = new @NonNull String[]{\"Alice\", \"Bob\", \"Charlie\"};\n" +
			"\n" +
			"    public String getName(int index) {\n" +
			"        String name = names[index];\n" +
			"        return name; /* statement A */\n" +
			"    }\n" +
			"}",
		},
		getCompilerOptions(),
		"");
}
public void testBug481332() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	public void foo() {\n" +
			"		@Nullable\n" +
			"		List<@NonNull String> list = new ArrayList<>();\n" +
			"		checkNotNull(list); // OK\n" +
			"\n" +
			"		@Nullable\n" +
			"		Map<@NonNull String, @NonNull String> map = new HashMap<>();\n" +
			"		checkNotNull(map); // OK\n" +
			"\n" +
			"		@NonNull\n" +
			"		Object @Nullable [] objects = new @NonNull Object[0];\n" +
			"		// Error: Null type mismatch (type annotations): required '@NonNull Object @NonNull[]' but this expression ...\n" +
			"		checkNotNull(objects);\n" +
			"	}\n" +
			"	\n" +
			"	public static <@Nullable T> T[] checkNotNull(T @Nullable [] array) {\n" +
			"		if (array == null) {\n" +
			"			throw new NullPointerException();\n" +
			"		}\n" +
			"		return array;\n" +
			"	}\n" +
			"\n" +
			"	public static <@Nullable T, C extends Iterable<T>> C checkNotNull(@Nullable C container) {\n" +
			"		if (container == null) {\n" +
			"			throw new NullPointerException();\n" +
			"		}\n" +
			"		return container;\n" +
			"	}\n" +
			"\n" +
			"	public static <@Nullable K, @Nullable V, M extends Map<K, V>> M checkNotNull(@Nullable M map) {\n" +
			"		if (map == null) {\n" +
			"			throw new NullPointerException();\n" +
			"		}\n" +
			"		return map;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	checkNotNull(list); // OK\n" +
		"	^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'List<@NonNull String>\' is not a valid substitute for the type parameter \'C extends Iterable<@Nullable T>\'\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 11)\n" +
		"	checkNotNull(map); // OK\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'Map<@NonNull String,@NonNull String>\' is not a valid substitute for the type parameter \'M extends Map<@Nullable K,@Nullable V>\'\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 16)\n" +
		"	checkNotNull(objects);\n" +
		"	             ^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@Nullable Object @Nullable[]\' but this expression has type \'@NonNull Object @Nullable[]\'\n" +
		"----------\n");
}
public void testBug481322a() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class Super<S, T extends List<S>> {\n" +
			"	S pick(T list) {\n" +
			"		return list.get(0);\n" +
			"	}\n" +
			"}\n" +
			"public class X extends Super<@NonNull String, List<@Nullable String>> {\n" +
			"	@Override\n" +
			"	public @NonNull String pick(List<@Nullable String> list) {\n" +
			"		return super.pick(list);\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		List<@Nullable String> withNulls = new ArrayList<@Nullable String>();\n" +
			"		withNulls.add(null);\n" +
			"		System.out.println(new X().pick(withNulls).toUpperCase());\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	return list.get(0);\n" +
		"	       ^^^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	public class X extends Super<@NonNull String, List<@Nullable String>> {\n" +
		"	                                              ^^^^\n" +
		"Null constraint mismatch: The type \'List<@Nullable String>\' is not a valid substitute for the type parameter \'T extends List<S>\'\n" +
		"----------\n");
}
public void testBug477719() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	@NonNull Number instantiate(@NonNull Class<? extends @NonNull Number> c) throws Exception {\n" +
			"		return c.newInstance();\n" +
			"	}\n" +
			"	void test(Double d) throws Exception {\n" +
			"		instantiate(Integer.class);\n" +
			"		instantiate(d.getClass());\n" +
			"	}\n" +
			"}\n"
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("deprecation");
	runner.runConformTest();
}
public void testBug482247() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	@NonNull String @NonNull[] s1 = new String[0];\n" + // expected type drives typing
			"	@Nullable String @NonNull[] s2 = new String[0];\n" + // expected type drives typing
			"	<T> @NonNull T first(@NonNull T @NonNull[] arr) {\n" +
			"		return arr[0];\n" +
			"	}\n" +
			"	void other(@Nullable String[] s) {\n" +
			"		s[0] = null;\n" +
			"	}\n" +
			"	@NonNull String test()  {\n" +
			"		other(new String[0]);\n" + // unchanged semantics
			"		return first(new String[0]);\n" + // unchanged semantics
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in X.java (at line 12)\n" +
		"	other(new String[0]);\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@Nullable String []\'\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 13)\n" +
		"	return first(new String[0]);\n" +
		"	             ^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String @NonNull[]\'\n" +
		"----------\n");
}
public void testBug482247_comment5() {
	runConformTestWithLibs(
		new String[] {
			"Snippet.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Snippet {\n" +
			"	@NonNull String[] s1 = new String[0]; // No warning\n" +
			"	public void handleIncidentBeforeCreate() {\n" +
			"		@NonNull String[] s = new String[0]; // Warning\n" +
			"		String [] @NonNull[] s2 = new String[0][];\n" +
			"		String [] @NonNull[] @Nullable[] s3 = new String[0][][];\n" +
			"	}\n" +
			"}"
		},
		getCompilerOptions(),
		"");
}
public void testBug483146() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class Foo {\n" +
			"\n" +
			"	void example1() {\n" +
			"        @Nullable List<String> container = new ArrayList<>();\n" +
			"        @NonNull List<String> list = checkNotNull(container);\n" +
			"	}\n" +
			"\n" +
			"	void example2() {\n" +
			"        @Nullable List<String> container= new ArrayList<>();\n" +
			"        @NonNull List<String> list = checkNotNull(container);\n" +
			"	}\n" +
			"    \n" +
			"    @NonNull <T, C extends  Iterable<T>> C checkNotNull(C container) {\n" +
			"        if (container == null) {\n" +
			"            throw new NullPointerException();\n" +
			"        }\n" +
			"		return container;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug483146b() {
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class Foo {\n" +
			"\n" +
			"	void example1() {\n" +
			"        @Nullable List<String> container = new ArrayList<>();\n" +
			"        @NonNull List<String> list = checkNotNull(container);\n" +
			"	}\n" +
			"\n" +
			"	void example2() {\n" +
			"        @Nullable List<String> container= new ArrayList<>();\n" +
			"        @NonNull List<String> list = checkNotNull(container);\n" +
			"	}\n" +
			"    \n" +
			"    <T, C extends  Iterable<T>> @NonNull C checkNotNull(@Nullable C container) {\n" + // <- variation: param is @Nullable
			"        if (container == null) {\n" +
			"            throw new NullPointerException();\n" +
			"        }\n" +
			"		return container;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug473713() {
	runConformTestWithLibs(
		new String[] {
			"a/A1.java",
			"package a;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class A1 {\n" +
			"	public class NestedInA1 {\n" +
			"	}\n" +
			"}\n",
			"a/A2.java",
			"package a;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class A2 {\n" +
			"	public static abstract class NestedInA2 {\n" +
			"		public final A1 a1 = new A1();\n" +
			"		protected abstract void handleApplicationSpecific(A1.NestedInA1 detail);\n" +
			"	}\n" +
			"}\n",
		}, getCompilerOptions(), "");
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"b/B.java",
			"package b;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class B {\n" +
			"	public static a.A1 m(a.A2.NestedInA2 nestedInA2) {\n" +
			"		return nestedInA2.a1;\n" +
			"	}\n" +
			"}\n",
		}, getCompilerOptions(), "");
}
public void testBug482228() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class Super<S> {\n" +
			"	<T extends List<S>> S pick(T list) {\n" +
			"		return list.get(0);\n" +
			"	}\n" +
			"}\n" +
			"public class X extends Super<@NonNull String> {\n" +
			"	@Override\n" +
			"	<T extends List<@Nullable String>> @NonNull String pick(T list) {\n" +
			"		return super.pick(list);\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		List<@Nullable String> withNulls = new ArrayList<>();\n" +
			"		withNulls.add(null);\n" +
			"		System.out.println(new X().pick(withNulls).toUpperCase());\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	return list.get(0);\n" +
		"	       ^^^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	<T extends List<@Nullable String>> @NonNull String pick(T list) {\n" +
		"	 ^\n" +
		"Cannot redefine null constraints of type variable \'T extends List<@NonNull String>\' declared in \'Super<String>.pick(T)\'\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 15)\n" +
		"	return super.pick(list);\n" +
		"	       ^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'T extends List<@Nullable String>\' is not a valid substitute for the type parameter \'T extends List<@NonNull String>\'\n" +
		"----------\n");
}
public void testBug483527() {
	final Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"public class Test  {\n" +
			"    static final short foo;\n" +
			"	static {\n" +
			"		foo = 1;\n" +
			"		for (int i=0; i<10; i++) {\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		compilerOptions,
		"");
}
public void testMultipleAnnotations1() {
	Map options1 = new HashMap<>(getCompilerOptions());
	options1.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	options1.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	runConformTest(
		new String[] {
			"org/foo/Nullable.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({ElementType.TYPE_USE})\n" +
			"public @interface Nullable {}\n",
			"org/foo/NonNull.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({ElementType.TYPE_USE})\n" +
			"public @interface NonNull {}\n",
			"p1/TestNulls.java",
			"package p1;\n" +
			"import org.foo.*;\n" +
			"\n" +
			"public class TestNulls {\n" +
			"	public @Nullable String weaken(@NonNull String theValue) {\n" +
			"		return theValue;\n" +
			"	}\n" +
			"\n" +
			"}"
		},
		options1);
	Map options2 = getCompilerOptions();
	options2.put(CompilerOptions.OPTION_NonNullAnnotationSecondaryNames, "org.foo.NonNull");
	options2.put(CompilerOptions.OPTION_NullableAnnotationSecondaryNames, "org.foo.Nullable");
	runNegativeTestWithLibs(
		new String[] {
			"p2/Test.java",
			"package p2;\n" +
			"import p1.TestNulls;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Test {\n" +
			"	@NonNull String test(TestNulls test, @Nullable String input) {\n" +
			"		return test.weaken(input);\n" +
			"	}\n" +
			"}\n"
		},
		options2,
		"----------\n" +
		"1. ERROR in p2\\Test.java (at line 6)\n" +
		"	return test.weaken(input);\n" +
		"	       ^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
		"----------\n" +
		"2. ERROR in p2\\Test.java (at line 6)\n" +
		"	return test.weaken(input);\n" +
		"	                   ^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
 		"----------\n");
}
public void test483952 () {
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"import java.util.function.Function;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"public class Test {\n" +
			"	void test1() {\n" +
			"		Function function = x -> x;\n" +
			"		String @Nullable [] z = test2(function, \"\");\n" +
			"	}\n" +
			"	<T> T @Nullable [] test2(Function<T, T> function, T t) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}"

		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in test\\Test.java (at line 6)\n" +
		"	Function function = x -> x;\n" +
		"	^^^^^^^^\n" +
		"Function is a raw type. References to generic type Function<T,R> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in test\\Test.java (at line 7)\n" +
		"	String @Nullable [] z = test2(function, \"\");\n" +
		"	                        ^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked invocation test2(Function, String) of the generic method test2(Function<T,T>, T) of type Test\n" +
		"----------\n" +
		"3. WARNING in test\\Test.java (at line 7)\n" +
		"	String @Nullable [] z = test2(function, \"\");\n" +
		"	                              ^^^^^^^^\n" +
		"Type safety: The expression of type Function needs unchecked conversion to conform to Function<String,String>\n" +
		"----------\n");
}
public void test484055() {
	runConformTestWithLibs(
		new String[] {
			"B.java",
			"interface A {\n" +
			"	public void f(String[] x);\n" +
			"\n" +
			"	public void f2(String x);\n" +
			"}\n" +
			"\n" +
			"public class B implements A {\n" +
			"	public void f(String @org.eclipse.jdt.annotation.Nullable [] x) {\n" +
			"	}\n" +
			"\n" +
			"	public void f2(@org.eclipse.jdt.annotation.Nullable String x) {\n" +
			"	}\n" +
			"}"
		},
		null,
		"");
}
public void testBug484108() {
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public interface Test <T0 extends Other> {\n" +
			"    public void a ( @NonNull T0 test );\n" +
			"}\n",
			"test/Other.java",
			"package test;\n" +
			"\n" +
			"public interface Other { }\n"
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false /* don't flush output dir */,
		new String[] {
			"test/TestImpl.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import java.lang.reflect.*;\n" +
			"\n" +
			"public class TestImpl <T extends Other> implements Test<T> {\n" +
			"\n" +
			"    /**\n" +
			"     * {@inheritDoc}\n" +
			"     *\n" +
			"     * @see test.Test#a(java.lang.Object)\n" +
			"     */\n" +
			"    @Override\n" +
			"    public void a ( @NonNull T test ) {\n" +
			"    }\n" +
			"	public static void main(String... args) {\n" +
			"		Class<?> c = TestImpl.class;\n" +
			"		Method[] ms = c.getDeclaredMethods();\n" +
			"		System.out.println(ms.length);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"",
		"2");
}
public void testBug484954() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTest(
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT, // sic: declaration annotation
			CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT, // sic: declaration annotation
			"Snippet.java",
			"import java.util.function.*;\n" +
			"import org.foo.*;\n" +
			"\n" +
			"public class Snippet {\n" +
			"\n" +
			"	public void test() {\n" +
			"		doStuff((@NonNull Object[] data) -> updateSelectionData(data)); \n" +
			"	}\n" +
			"\n" +
			"	private void doStuff(Consumer<Object[]> postAction) { }\n" +
			"	private void updateSelectionData(final @NonNull Object data) { }\n" +
			"}\n"
		},
		customOptions,
		"");
}
public void testBug484981() {
	runNegativeTestWithLibs(
		new String[] {
			"test1/GenericWithNullableBound.java",
			"package test1;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +

			"@NonNullByDefault\n" +
			"public class GenericWithNullableBound<T extends @Nullable Number> {\n" +
			"}\n",

			"test1/GenericWithNullableBound2.java",
			"package test1;\n" +
			"import static java.lang.annotation.ElementType.TYPE_USE;\n" +
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"import java.lang.annotation.Target;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +

			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({ TYPE_USE })\n" +
			"@interface SomeAnnotation {\n" +
			"}\n" +

			"@NonNullByDefault\n" +
			"public class GenericWithNullableBound2<@SomeAnnotation T extends @Nullable Number> {\n" +
			"}\n",

			"test1/GenericWithNullable.java",
			"package test1;\n" +

			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +

			"@NonNullByDefault\n" +
			"public class GenericWithNullable<@Nullable T> {\n" +
			"}\n",

			"test1/GenericWithNonNullBound.java",
			"package test1;\n" +

			"import org.eclipse.jdt.annotation.NonNull;\n" +

			"public class GenericWithNonNullBound<T extends @NonNull Number> {\n" +
			"}\n",
			"test1/ClassInSameProject.java",
			"package test1;\n" +

			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +

			"public class ClassInSameProject {\n" +
			"	static void f1() {\n" +
			"		new GenericWithNullableBound<@NonNull Number>();\n" +
			"	}\n" +

			"	static void f2() {\n" +
			"		new GenericWithNullableBound2<@NonNull Number>();\n" +
			"	}\n" +

			"	static void f3() {\n" +
			"		new GenericWithNonNullBound<@Nullable Number>(); // error 1 expected\n" +
			"	}\n" +

			"	static void f4() {\n" +
			"		new GenericWithNullable<@NonNull Number>(); // error 2 expected\n" +
			"	}\n" +
			"}"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test1\\ClassInSameProject.java (at line 12)\n" +
		"	new GenericWithNonNullBound<@Nullable Number>(); // error 1 expected\n" +
		"	                            ^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable Number\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'\n" +
		"----------\n" +
		"2. ERROR in test1\\ClassInSameProject.java (at line 15)\n" +
		"	new GenericWithNullable<@NonNull Number>(); // error 2 expected\n" +
		"	                        ^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull Number\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n"
	);
	runNegativeTestWithLibs(
			new String[] {
				"test2/ClassInOtherProject.java",
				"package test2;\n" +

				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +

				"import test1.GenericWithNonNullBound;\n" +
				"import test1.GenericWithNullable;\n" +
				"import test1.GenericWithNullableBound;\n" +
				"import test1.GenericWithNullableBound2;\n" +

				"public class ClassInOtherProject {\n" +
				"	static void g1() {\n" +
				"		new GenericWithNullableBound<@NonNull Number>();\n" +
				"	}\n" +

				"	static void g2() {\n" +
				"		new GenericWithNullableBound2<@NonNull Number>();\n" +
				"	}\n" +

				"	static void g3() {\n" +
				"		new GenericWithNonNullBound<@Nullable Number>(); // error 3 expected\n" +
				"	}\n" +

				"	static void g4() {\n" +
				"		new GenericWithNullable<@NonNull Number>(); // error 4 expected\n" +
				"	}\n" +
				"}"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in test2\\ClassInOtherProject.java (at line 16)\n" +
			"	new GenericWithNonNullBound<@Nullable Number>(); // error 3 expected\n" +
			"	                            ^^^^^^^^^^^^^^^^\n" +
			"Null constraint mismatch: The type \'@Nullable Number\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'\n" +
			"----------\n" +
			"2. ERROR in test2\\ClassInOtherProject.java (at line 19)\n" +
			"	new GenericWithNullable<@NonNull Number>(); // error 4 expected\n" +
			"	                        ^^^^^^^^^^^^^^^\n" +
			"Null constraint mismatch: The type \'@NonNull Number\' is not a valid substitute for the type parameter \'@Nullable T extends Object\'\n" +
			"----------\n"
	);
}
// same testBinary06 but via SourceTypeBindings
public void testBug484981b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
	runNegativeTestWithLibs(
			new String[] {
				"p/X1.java",
				"package p;\n" +
				"import java.util.ArrayList;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"public abstract class X1<T extends @NonNull Object> extends ArrayList<T> {\n" +
				"    public <U, V extends @Nullable Object> void foo(U u, V v) {}\n" +
				"}\n",
				"p/X2.java",
				"package p;\n"+
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X2<@Nullable W extends Object> {}\n",
				"Y1.java",
				"import p.X1;\n" +
				"import p.X2;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class Y1 {\n" +
				"	X1<@Nullable String> maybeStrings;\n" + // incompatible: T has a bound constrained to @NonNull
				"   X2<@NonNull String> strings;\n" +       // incompatible: W is constrained to @Nullable
				"	void test(X1<@NonNull String> x) {\n" + // OK
				"		x.<Y1, @NonNull Object>foo(this, new Object());\n" + // OK: 'extends @Nullable' is no restriction
				"	}\n" +
				"}\n"
			},
			customOptions,
			"----------\n" +
			"1. ERROR in Y1.java (at line 5)\n" +
			"	X1<@Nullable String> maybeStrings;\n" +
			"	   ^^^^^^^^^^^^^^^^\n" +
			"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'\n" +
			"----------\n" +
			"2. ERROR in Y1.java (at line 6)\n" +
			"	X2<@NonNull String> strings;\n" +
			"	   ^^^^^^^^^^^^^^^\n" +
			"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable W extends Object\'\n" +
			"----------\n"
			);
}

// same testBinary06b but via SourceTypeBindings
public void testBug484981c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
	// fix the bug:
	runNegativeTestWithLibs(
			new String[] {
				"p/X1.java",
				"package p;\n" +
				"import java.util.ArrayList;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"public abstract class X1<T extends java.lang.@NonNull Object> extends ArrayList<T> {\n" +
				"    public <U, V extends java.lang.@Nullable Object> void foo(U u, V v) {}\n" +
				"}\n",
				"p/X2.java",
				"package p;\n"+
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X2<@Nullable W extends Object> {}\n",
				"Y1.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class Y1 {\n" +
				"	p.X1<java.lang.@Nullable String> maybeStrings;\n" + // incompatible: T has a bound constrained to @NonNull
				"   p.X2<java.lang.@NonNull String> strings;\n" +       // incompatible: W is constrained to @Nullable
				"	void test(p.X1<java.lang.@NonNull String> x) {\n" + // OK
				"		x.<Y1, java.lang.@NonNull Object>foo(this, new Object());\n" + // OK: 'extends @Nullable' is no restriction
				"	}\n" +
				"}\n"
			},
			customOptions,
			"----------\n" +
			"1. ERROR in Y1.java (at line 3)\n" +
			"	p.X1<java.lang.@Nullable String> maybeStrings;\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'\n" +
			"----------\n" +
			"2. ERROR in Y1.java (at line 4)\n" +
			"	p.X2<java.lang.@NonNull String> strings;\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable W extends Object\'\n" +
			"----------\n"
			);
}

// same testBinary07 but via SourceTypeBindings
public void testBug484981d() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
	runNegativeTestWithLibs(
			new String[] {
				"p/List.java",
				"package p;\n" +
				"public interface List<T> {\n" +
				"	T get(int i);\n" + // avoid IProblem.NonNullTypeVariableFromLegacyMethod against unannotated j.u.List
				"}\n",
				"p/X1.java",
				"package p;\n" +
				"import java.util.Map;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"import java.lang.annotation.*;\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Immutable {}\n" +
				"public abstract class X1 {\n" +
				"    public <@NonNull U, V extends @Nullable Object> List<@NonNull Map<Object, @NonNull String>> foo(@Immutable X1 this, U u, V v) { return null; }\n" +
				"}\n",
				"Y1.java",
				"import p.X1;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class Y1 {\n" +
				"	void test(X1 x) {\n" +
				"		x.<@NonNull Y1, @NonNull Object>foo(this, new Object())\n" + // OK: 'extends @Nullable' is no restriction
				"			.get(0).put(null, null);\n" + // second null is illegal
				"	}\n" +
				"}\n"
			},
			customOptions,
			"----------\n" +
			"1. ERROR in Y1.java (at line 6)\n" +
			"	.get(0).put(null, null);\n" +
			"	                  ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n");
}
public void testBug466562() {
	runNegativeTestWithLibs(
		new String[] {
			"x/C.java",
			"package x;\n" +

			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +

			"@NonNullByDefault({})\n" +
			"public class C <T1 extends @Nullable Number> {\n" +
			"    String consume(T1 t) {\n" +
			"        @NonNull Object x = t; // error, should warn?\n" +
			"        x.toString();\n" +
			"        return t.toString(); // legal???\n" +
			"    }\n" +

			"    void y() {\n" +
			"        consume(null);  // illegal - OK\n" +
			"        @NonNull Object t = provide();  // error, should warn?\n" +
			"        t.toString();\n" +
			"    }\n" +

			"    T1 provide() {\n" +
			"        return null; // error, should warn?\n" +
			"    }\n" +

			"    C<Integer> cString;  // OK - Null constraint mismatch: The type 'Integer' is not a valid substitute for the type parameter 'T1 extends @Nullable Number'\n" +
			"    C<@NonNull Integer> c1String;  // Wrong: Null constraint mismatch: The type '@NonNull Integer' is not a valid substitute for the type parameter 'T1 extends @Nullable Number'\n" +
			"    C<@Nullable Integer> c2String; // legal - OK\n" +
			"}"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in x\\C.java (at line 5)\n" +
		"	@NonNullByDefault({})\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing package x\n" +
		"----------\n" +
		"2. ERROR in x\\C.java (at line 8)\n" +
		"	@NonNull Object x = t; // error, should warn?\n" +
		"	                    ^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T1\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"3. ERROR in x\\C.java (at line 10)\n" +
		"	return t.toString(); // legal???\n" +
		"	       ^\n" +
		"Potential null pointer access: this expression has type \'T1\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"4. ERROR in x\\C.java (at line 13)\n" +
		"	consume(null);  // illegal - OK\n" +
		"	        ^^^^\n" +
		"Null type mismatch: required \'T1 extends @Nullable Number\' but the provided value is null\n" +
		"----------\n" +
		"5. ERROR in x\\C.java (at line 14)\n" +
		"	@NonNull Object t = provide();  // error, should warn?\n" +
		"	                    ^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'T1 extends @Nullable Number\'\n" +
		"----------\n" +
		"6. ERROR in x\\C.java (at line 18)\n" +
		"	return null; // error, should warn?\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'T1 extends @Nullable Number\' but the provided value is null\n" +
		"----------\n"
	);
}
public void testBug485056() {
	runConformTestWithLibs(
		new String[] {
			"TestExplainedValue.java",
			"import java.io.Serializable;\n" +

			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +

			"class ExplainedValue<T extends Serializable> {\n" +
			"	public @Nullable T featureValue;\n" +
			"}\n" +

			"public class TestExplainedValue {\n" +
			"	static @Nullable Serializable g(ExplainedValue<? extends @NonNull Serializable> explainedValue) {\n" +
			"		return explainedValue.featureValue;\n" +
			"	}\n" +
			"}"
		},
		getCompilerOptions(),
		""
	);
}
public void testBug484741() {
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"public class Test {\n" +
			"	static <T, E extends T> void f(java.util.ArrayList<T> list, E element) {\n" +
			"		list.add(element);" +
			"	}\n" +
			"\n" +
			"	static <A> void g(A a) {\n" +
			"		f(new java.util.ArrayList<A>(), a);\n" +
			"	}\n" +
			"\n" +
			"	static <T1, E1 extends T1> void h(E1 element1, java.util.ArrayList<T1> list1) {\n" +
			"		f(list1, element1);\n" +
			"	}\n" +
			"}"
	}, getCompilerOptions(), "");
}
public void testBug484741b() {
	runConformTestWithLibs(
		new String[] {
			"test/TestDep.java",
			"package test;\n" +
			"public class TestDep {\n" +
			"	static <T, E extends T> T f(E e) {\n" +
			"		return e;\n" +
			"	}\n" +
			"}"
	}, getCompilerOptions(), "");
}
public void testBug484741c() {
	runConformTestWithLibs(
		new String[] {
			"test/Test3.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +

			"@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE, DefaultLocation.FIELD, DefaultLocation.TYPE_ARGUMENT })\n" +
			"class Feature3<ValueType extends java.io.Serializable, PartitionKeyType> {\n" +
			"}\n" +

			"@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE, DefaultLocation.FIELD, DefaultLocation.TYPE_ARGUMENT })\n" +
			"public class Test3 {\n" +
			"	public static <T extends java.io.Serializable, F extends Feature3<T, ?>> T[] getValues(F feature) {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +

			"	public static void f(Feature3<?, ?> feature) {\n" +
			"		getValues(feature);\n" +
			"	}\n" +
			"}"
	}, getCompilerOptions(), "");
}
public void testBug484741d() {
	runConformTestWithLibs(
		new String[] {
			"BaseNNBD.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class BaseNNBD<S extends Runnable, I extends S> {\n" +
			"}\n",
			"DerivedNNBD.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class DerivedNNBD<S1 extends Runnable, I1 extends S1> extends BaseNNBD<S1, I1> {	\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug484741e() {
	runConformTestWithLibs(
		new String[] {
			"test/AbstractFeature.java",
			"package test;\n" +
			"import java.io.Serializable;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n"+
			"abstract class AbstractFeature<T extends @NonNull Serializable> {\n" +
			"}\n",
			"test/SubFeature.java",
			"package test;\n" +
			"import java.io.Serializable;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n"+
			"public class SubFeature<T1 extends @NonNull Serializable> extends AbstractFeature<T1> {\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug484741Invoke() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestInterdepInvoke.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class TestInterdepInvoke {\n" +
			"	static <T, E extends T> T f1(E e) {\n" +
			"		return e;\n" +
			"	}\n" +
			"\n" +
			"	static <T, @Nullable E extends T> T f2(E e) {\n" +
			"		return e; // error 1 expected\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T, E extends T> T f3(E e) {\n" +
			"		return e;\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T, @Nullable E extends T> T f4(E e) {\n" +
			"		return e;\n" +
			"	}\n" +
			"\n" +
			"	// -------- invocations of f1 --------\n" +
			"\n" +
			"	static <T11, E11 extends T11> T11 g11(E11 e) {\n" +
			"		return f1(e);\n" +
			"	}\n" +
			"\n" +
			"	static <T12, @Nullable E12 extends T12> T12 g12(E12 e) {\n" +
			"		return f1(e); // error 2 expected\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T13, E13 extends T13> T13 g13(E13 e) {\n" +
			"		return f1(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T14, @Nullable E14 extends T14> T14 g14(E14 e) {\n" +
			"		return f1(e);\n" +
			"	}\n" +
			"\n" +
			"	// -------- invocations of f2 --------\n" +
			"\n" +
			"	static <T21, E21 extends T21> T21 g21(E21 e) {\n" +
			"		return f2(e);\n" +
			"	}\n" +
			"\n" +
			"	static <T22, @Nullable E22 extends T22> T22 g22(E22 e) {\n" +
			"		return f2(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T23, E23 extends T23> T23 g23(E23 e) {\n" +
			"		return f2(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T24, @Nullable E24 extends T24> T24 g24(E24 e) {\n" +
			"		return f2(e);\n" +
			"	}\n" +
			"\n" +
			"	// -------- invocations of f3 --------\n" +
			"\n" +
			"	static <T31, E31 extends T31> T31 g31(E31 e) {\n" +
			"		return f3(e); // error 3 expected\n" +
			"	}\n" +
			"\n" +
			"	static <T32, @Nullable E32 extends T32> T32 g32(E32 e) {\n" +
			"		return f3(e); // error 4 expected\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T33, E33 extends T33> T33 g33(E33 e) {\n" +
			"		return f3(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T34, @Nullable E34 extends T34> T34 g34(E34 e) {\n" +
			"		return f3(e);\n" +
			"	}\n" +
			"\n" +
			"	// -------- invocations of f4 --------\n" +
			"\n" +
			"	static <T41, E41 extends T41> T41 g41(E41 e) {\n" +
			"		return f4(e); /// error 5 expected\n" +
			"	}\n" +
			"\n" +
			"	static <T42, @Nullable E42 extends T42> T42 g42(E42 e) {\n" +
			"		return f4(e); // error 6 expected\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T43, E43 extends T43> T43 g43(E43 e) {\n" +
			"		return f4(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T44, @Nullable E44 extends T44> T44 g44(E44 e) {\n" +
			"		return f4(e);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\TestInterdepInvoke.java (at line 11)\n" +
		"	return e; // error 1 expected\n" +
		"	       ^\n" +
		"Null type mismatch (type annotations): required \'T\' but this expression has type \'@Nullable E extends T\', where \'T\' is a free type variable\n" +
		"----------\n" +
		"2. ERROR in test\\TestInterdepInvoke.java (at line 29)\n" +
		"	return f1(e); // error 2 expected\n" +
		"	       ^^^^^\n" +
		"Null type mismatch (type annotations): required \'T12\' but this expression has type \'@Nullable E12 extends T12\', where \'T12\' is a free type variable\n" +
		"----------\n" +
		"3. ERROR in test\\TestInterdepInvoke.java (at line 61)\n" +
		"	return f3(e); // error 3 expected\n" +
		"	       ^^^^^\n" +
		"Null type mismatch (type annotations): required \'T31\' but this expression has type \'@Nullable E31 extends T31\', where \'T31\' is a free type variable\n" +
		"----------\n" +
		"4. ERROR in test\\TestInterdepInvoke.java (at line 65)\n" +
		"	return f3(e); // error 4 expected\n" +
		"	       ^^^^^\n" +
		"Null type mismatch (type annotations): required \'T32\' but this expression has type \'@Nullable E32 extends T32\', where \'T32\' is a free type variable\n" +
		"----------\n" +
		"5. ERROR in test\\TestInterdepInvoke.java (at line 79)\n" +
		"	return f4(e); /// error 5 expected\n" +
		"	       ^^^^^\n" +
		"Null type mismatch (type annotations): required \'T41\' but this expression has type \'@Nullable E41 extends T41\', where \'T41\' is a free type variable\n" +
		"----------\n" +
		"6. ERROR in test\\TestInterdepInvoke.java (at line 83)\n" +
		"	return f4(e); // error 6 expected\n" +
		"	       ^^^^^\n" +
		"Null type mismatch (type annotations): required \'T42\' but this expression has type \'@Nullable E42 extends T42\', where \'T42\' is a free type variable\n" +
		"----------\n"
	);
}
public void testBug484741Invoke2() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestInterdepInvokeNN.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"@java.lang.annotation.Target({ java.lang.annotation.ElementType.TYPE_USE })\n" +
			"@interface SomeAnnotation {\n" +
			"	// just needed as workaround if bug 484981 is not fixed\n" +
			"}\n" +
			"\n" +
			"public class TestInterdepInvokeNN {\n" +
			"	static <T, @SomeAnnotation E extends T> T f1(E e) {\n" +
			"		return e;\n" +
			"	}\n" +
			"\n" +
			"	static <T, @NonNull E extends T> T f2(E e) {\n" +
			"		return e;\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T, @SomeAnnotation E extends T> T f3(E e) {\n" +
			"		return e;\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T, @NonNull E extends T> T f4(E e) {\n" +
			"		return e;\n" +
			"	}\n" +
			"\n" +
			"	// -------- invocations of f1 --------\n" +
			"\n" +
			"	static <T11, @SomeAnnotation E11 extends T11> T11 g11(E11 e) {\n" +
			"		return f1(e);\n" +
			"	}\n" +
			"\n" +
			"	static <T12, @NonNull E12 extends T12> T12 g12(E12 e) {\n" +
			"		return f1(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T13, @SomeAnnotation E13 extends T13> T13 g13(E13 e) {\n" +
			"		return f1(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T14, @NonNull E14 extends T14> T14 g14(E14 e) {\n" +
			"		return f1(e);\n" +
			"	}\n" +
			"\n" +
			"	// -------- invocations of f2 --------\n" +
			"\n" +
			"	static <T21, @SomeAnnotation E21 extends T21> T21 g21(E21 e) {\n" +
			"		return f2(e); // error 1 expected\n" +
			"	}\n" +
			"\n" +
			"	static <T22, @NonNull E22 extends T22> T22 g22(E22 e) {\n" +
			"		return f2(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T23, @SomeAnnotation E23 extends T23> T23 g23(E23 e) {\n" +
			"		return f2(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T24, @NonNull E24 extends T24> T24 g24(E24 e) {\n" +
			"		return f2(e);\n" +
			"	}\n" +
			"\n" +
			"	// -------- invocations of f3 --------\n" +
			"\n" +
			"	static <T31, @SomeAnnotation E31 extends T31> T31 g31(E31 e) {\n" +
			"		return f3(e); // error 2 expected\n" +
			"	}\n" +
			"\n" +
			"	static <T32, @NonNull E32 extends T32> T32 g32(E32 e) {\n" +
			"		return f3(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T33, @SomeAnnotation E33 extends T33> T33 g33(E33 e) {\n" +
			"		return f3(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T34, @NonNull E34 extends T34> T34 g34(E34 e) {\n" +
			"		return f3(e);\n" +
			"	}\n" +
			"\n" +
			"	// -------- invocations of f4 --------\n" +
			"\n" +
			"	static <T41, @SomeAnnotation E41 extends T41> T41 g41(E41 e) {\n" +
			"		return f4(e); // error 3 expected\n" +
			"	}\n" +
			"\n" +
			"	static <T42, @NonNull E42 extends T42> T42 g42(E42 e) {\n" +
			"		return f4(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T43, @SomeAnnotation E43 extends T43> T43 g43(E43 e) {\n" +
			"		return f4(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T44, @NonNull E44 extends T44> T44 g44(E44 e) {\n" +
			"		return f4(e);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\TestInterdepInvokeNN.java (at line 48)\n" +
		"	return f2(e); // error 1 expected\n" +
		"	          ^\n" +
		"Null type safety: required '@NonNull' but this expression has type 'E21', a free type variable that may represent a '@Nullable' type\n" +
		"----------\n" +
		"2. ERROR in test\\TestInterdepInvokeNN.java (at line 66)\n" +
		"	return f3(e); // error 2 expected\n" +
		"	          ^\n" +
		"Null type safety: required '@NonNull' but this expression has type 'E31', a free type variable that may represent a '@Nullable' type\n" +
		"----------\n" +
		"3. ERROR in test\\TestInterdepInvokeNN.java (at line 84)\n" +
		"	return f4(e); // error 3 expected\n" +
		"	          ^\n" +
		"Null type safety: required '@NonNull' but this expression has type 'E41', a free type variable that may represent a '@Nullable' type\n" +
		"----------\n"
	);
}
public void testBug484741Invoke3() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestInterdepInvoke.java",
			"package test;\n" +
			"\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class TestInterdepInvoke {\n" +
			"	static <T1, E1 extends T1> void f1(ArrayList<T1> list, E1 e) {\n" +
			"		list.add(e);\n" +
			"	}\n" +
			"\n" +
			"	static <T2, @Nullable E2 extends T2> void f2(ArrayList<T2> list, E2 e) {\n" +
			"		list.add(e); // error expected\n" +
			"	}\n" +
			"\n" +
			"	static <T3, @NonNull E3 extends T3> void f3(ArrayList<T3> list, E3 e) {\n" +
			"		list.add(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T4, E4 extends T4> void f4(ArrayList<T4> list, E4 e) {\n" +
			"		list.add(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T5, @Nullable E5 extends T5> void f5(ArrayList<T5> list, E5 e) {\n" +
			"		list.add(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@Nullable T6, @NonNull E6 extends T6> void f6(ArrayList<T6> list, E6 e) {\n" +
			"		list.add(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T7, E7 extends T7> void f7(ArrayList<T7> list, E7 e) {\n" +
			"		list.add(e);\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T8, @Nullable E8 extends T8> void f8(ArrayList<T8> list, E8 e) {\n" +
			"		list.add(e); // error expected\n" +
			"	}\n" +
			"\n" +
			"	static <@NonNull T9, @NonNull E9 extends T9> void f9(ArrayList<T9> list, E9 e) {\n" +
			"		list.add(e);\n" +
			"	}\n" +
			"\n" +
			"	// -------- invocations, but all of the 81 combinations removed, that were already handled correctly  -----\n" +
			"\n" +
			"	static <S1, F1 extends S1> void g1(ArrayList<S1> list, F1 e) {\n" +
			"		f1(list, e);\n" +
			"		f2(list, e);\n" +
			"	}\n" +
			"\n" +
			"	static <S2, @Nullable F2 extends S2> void g2(ArrayList<S2> list, F2 e) {\n" +
			"		f1(list, e);\n" +
			"		f2(list, e);\n" +
			"	}\n" +
			"\n" +
			"	static <S3, @NonNull F3 extends S3> void g3(ArrayList<S3> list, F3 e) {\n" +
			"		f2(list, e);\n" +
			"	}\n" +
			"}\n",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\TestInterdepInvoke.java (at line 14)\n" +
		"	list.add(e); // error expected\n" +
		"	         ^\n" +
		"Null type mismatch (type annotations): required \'T2\' but this expression has type \'@Nullable E2 extends T2\', where \'T2\' is a free type variable\n" +
		"----------\n" +
		"2. ERROR in test\\TestInterdepInvoke.java (at line 38)\n" +
		"	list.add(e); // error expected\n" +
		"	         ^\n" +
		"Null type mismatch (type annotations): required \'@NonNull T8\' but this expression has type \'@Nullable E8 extends @NonNull T8\'\n" +
		"----------\n"
	);
}


public void testBug484471SubclassNullable() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestInterdepSubClass.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class TestInterdepSubClass {\n" +
			"	static class A1<T, E extends T> {\n" +
			"	}\n" +
			"\n" +
			"	static class A2<T, @Nullable E extends T> {\n" +
			"	}\n" +
			"\n" +
			"	static class A3<@Nullable T, E extends T> {\n" +
			"	}\n" +
			"\n" +
			"	static class A4<@Nullable T, @Nullable E extends T> {\n" +
			"	}\n" +
			"\n" +
			"	// -------- subclasses of A1<T, E extends T> --------\n" +
			"\n" +
			"	static class B11<T11, E11 extends T11> extends A1<T11, E11> {\n" +
			"	}\n" +
			"\n" +
			"	static class B12<T12, @Nullable E12 extends T12> extends A1<T12, E12> {\n" +
			"	}\n" +
			"\n" +
			"	static class B13<@Nullable T13, E13 extends T13> extends A1<T13, E13> {\n" +
			"	}\n" +
			"\n" +
			"	static class B14<@Nullable T14, @Nullable E14 extends T14> extends A1<T14, E14> {\n" +
			"	}\n" +
			"\n" +
			"	// -------- subclasses of A2<T, @Nullable E extends T> --------\n" +
			"\n" +
			"	static class B21<T21, E21 extends T21> extends A2<T21, E21> { // expect error 1\n" +
			"	}\n" +
			"\n" +
			"	static class B22<T22, @Nullable E22 extends T22> extends A2<T22, E22> {\n" +
			"	}\n" +
			"\n" +
			"	static class B23<@Nullable T23, E23 extends T23> extends A2<T23, E23> { // expect error 2\n" +
			"	}\n" +
			"\n" +
			"	static class B24<@Nullable T24, @Nullable E24 extends T24> extends A2<T24, E24> {\n" +
			"	}\n" +
			"\n" +
			"	// -------- subclasses of A3<@Nullable T, E extends T> --------\n" +
			"\n" +
			"	static class B31<T31, E31 extends T31> extends A3<T31, E31> { // expect error 3\n" +
			"	}\n" +
			"\n" +
			"	static class B32<T32, @Nullable E32 extends T32> extends A3<T32, E32> { // expect error 4\n" +
			"	}\n" +
			"\n" +
			"	static class B33<@Nullable T33, E33 extends T33> extends A3<T33, E33> {\n" +
			"	}\n" +
			"\n" +
			"	static class B34<@Nullable T34, @Nullable E34 extends T34> extends A3<T34, E34> {\n" +
			"	}\n" +
			"\n" +
			"	// -------- subclasses of A4<@Nullable T, @Nullable E extends T> --------\n" +
			"\n" +
			"	static class B41<T41, E41 extends T41> extends A4<T41, E41> { // expect errors 5 & 6\n" +
			"	}\n" +
			"\n" +
			"	static class B42<T42, @Nullable E42 extends T42> extends A4<T42, E42> { // expect error 7\n" +
			"	}\n" +
			"\n" +
			"	static class B43<@Nullable T43, E43 extends T43> extends A4<T43, E43> { // expect error 8\n" +
			"	}\n" +
			"\n" +
			"	static class B44<@Nullable T44, @Nullable E44 extends T44> extends A4<T44, E44> {\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\TestInterdepSubClass.java (at line 34)\n" +
		"	static class B21<T21, E21 extends T21> extends A2<T21, E21> { // expect error 1\n" +
		"	                                                       ^^^\n" +
		"Null constraint mismatch: The type \'E21 extends T21\' is not a valid substitute for the type parameter \'@Nullable E extends T\'\n" +
		"----------\n" +
		"2. ERROR in test\\TestInterdepSubClass.java (at line 40)\n" +
		"	static class B23<@Nullable T23, E23 extends T23> extends A2<T23, E23> { // expect error 2\n" +
		"	                                                                 ^^^\n" +
		"Null constraint mismatch: The type \'E23 extends @Nullable T23\' is not a valid substitute for the type parameter \'@Nullable E extends T\'\n" +
		"----------\n" +
		"3. ERROR in test\\TestInterdepSubClass.java (at line 48)\n" +
		"	static class B31<T31, E31 extends T31> extends A3<T31, E31> { // expect error 3\n" +
		"	                                                  ^^^\n" +
		"Null constraint mismatch: The type \'T31\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"4. ERROR in test\\TestInterdepSubClass.java (at line 51)\n" +
		"	static class B32<T32, @Nullable E32 extends T32> extends A3<T32, E32> { // expect error 4\n" +
		"	                                                            ^^^\n" +
		"Null constraint mismatch: The type \'T32\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"5. ERROR in test\\TestInterdepSubClass.java (at line 62)\n" +
		"	static class B41<T41, E41 extends T41> extends A4<T41, E41> { // expect errors 5 & 6\n" +
		"	                                                  ^^^\n" +
		"Null constraint mismatch: The type \'T41\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"6. ERROR in test\\TestInterdepSubClass.java (at line 62)\n" +
		"	static class B41<T41, E41 extends T41> extends A4<T41, E41> { // expect errors 5 & 6\n" +
		"	                                                       ^^^\n" +
		"Null constraint mismatch: The type \'E41 extends T41\' is not a valid substitute for the type parameter \'@Nullable E extends @Nullable T\'\n" +
		"----------\n" +
		"7. ERROR in test\\TestInterdepSubClass.java (at line 65)\n" +
		"	static class B42<T42, @Nullable E42 extends T42> extends A4<T42, E42> { // expect error 7\n" +
		"	                                                            ^^^\n" +
		"Null constraint mismatch: The type \'T42\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"8. ERROR in test\\TestInterdepSubClass.java (at line 68)\n" +
		"	static class B43<@Nullable T43, E43 extends T43> extends A4<T43, E43> { // expect error 8\n" +
		"	                                                                 ^^^\n" +
		"Null constraint mismatch: The type \'E43 extends @Nullable T43\' is not a valid substitute for the type parameter \'@Nullable E extends @Nullable T\'\n" +
		"----------\n"
	);
}
public void testBug484471SubclassNonNull() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestInterdepSubClassNN.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"@java.lang.annotation.Target({ java.lang.annotation.ElementType.TYPE_USE })\n" +
			"@interface SomeAnnotation {\n" +
			"	// just needed as workaround if bug 484981 is not fixed\n" +
			"}\n" +
			"\n" +
			"public class TestInterdepSubClassNN {\n" +
			"	static class A1<T, @SomeAnnotation E extends T> {\n" +
			"	}\n" +
			"\n" +
			"	static class A2<T, @NonNull E extends T> {\n" +
			"	}\n" +
			"\n" +
			"	static class A3<@NonNull T, @SomeAnnotation E extends T> {\n" +
			"	}\n" +
			"\n" +
			"	static class A4<@NonNull T, @NonNull E extends T> {\n" +
			"	}\n" +
			"\n" +
			"	// -------- subclasses of A1<T, E extends T> --------\n" +
			"\n" +
			"	static class B11<T11, @SomeAnnotation E11 extends T11> extends A1<T11, E11> {\n" +
			"	}\n" +
			"\n" +
			"	static class B12<T12, @NonNull E12 extends T12> extends A1<T12, E12> {\n" +
			"	}\n" +
			"\n" +
			"	static class B13<@NonNull T13, @SomeAnnotation E13 extends T13> extends A1<T13, E13> {\n" +
			"	}\n" +
			"\n" +
			"	static class B14<@NonNull T14, @NonNull E14 extends T14> extends A1<T14, E14> {\n" +
			"	}\n" +
			"\n" +
			"	// -------- subclasses of A2<T, @NonNull E extends T> --------\n" +
			"\n" +
			"	static class B21<T21, @SomeAnnotation E21 extends T21> extends A2<T21, E21> { // expect error 1\n" +
			"	}\n" +
			"\n" +
			"	static class B22<T22, @NonNull E22 extends T22> extends A2<T22, E22> {\n" +
			"	}\n" +
			"\n" +
			"	static class B23<@NonNull T23, @SomeAnnotation E23 extends T23> extends A2<T23, E23> {\n" +
			"	}\n" +
			"\n" +
			"	static class B24<@NonNull T24, @NonNull E24 extends T24> extends A2<T24, E24> {\n" +
			"	}\n" +
			"\n" +
			"	// -------- subclasses of A3<@NonNull T, E extends T> --------\n" +
			"\n" +
			"	static class B31<T31, @SomeAnnotation E31 extends T31> extends A3<T31, E31> { // expect errors 2 & 3\n" +
			"	}\n" +
			"\n" +
			"	static class B32<T32, @NonNull E32 extends T32> extends A3<T32, E32> { // expect error 4\n" +
			"	}\n" +
			"\n" +
			"	static class B33<@NonNull T33, @SomeAnnotation E33 extends T33> extends A3<T33, E33> {\n" +
			"	}\n" +
			"\n" +
			"	static class B34<@NonNull T34, @NonNull E34 extends T34> extends A3<T34, E34> {\n" +
			"	}\n" +
			"\n" +
			"	// -------- subclasses of A4<@NonNull T, @NonNull E extends T> --------\n" +
			"\n" +
			"	static class B41<T41, @SomeAnnotation E41 extends T41> extends A4<T41, E41> { // expect error 5 & 6\n" +
			"	}\n" +
			"\n" +
			"	static class B42<T42, @NonNull E42 extends T42> extends A4<T42, E42> { // expect error 7\n" +
			"	}\n" +
			"\n" +
			"	static class B43<@NonNull T43, @SomeAnnotation E43 extends T43> extends A4<T43, E43> {\n" +
			"	}\n" +
			"\n" +
			"	static class B44<@NonNull T44, @NonNull E44 extends T44> extends A4<T44, E44> {\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
				"1. ERROR in test\\TestInterdepSubClassNN.java (at line 39)\n" +
				"	static class B21<T21, @SomeAnnotation E21 extends T21> extends A2<T21, E21> { // expect error 1\n" +
				"	                                                                       ^^^\n" +
				"Null constraint mismatch: The type \'E21 extends T21\' is not a valid substitute for the type parameter \'@NonNull E extends T\'\n" +
				"----------\n" +
				"2. ERROR in test\\TestInterdepSubClassNN.java (at line 53)\n" +
				"	static class B31<T31, @SomeAnnotation E31 extends T31> extends A3<T31, E31> { // expect errors 2 & 3\n" +
				"	                                                                  ^^^\n" +
				"Null constraint mismatch: The type \'T31\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
				"----------\n" +
				"3. ERROR in test\\TestInterdepSubClassNN.java (at line 53)\n" +
				"	static class B31<T31, @SomeAnnotation E31 extends T31> extends A3<T31, E31> { // expect errors 2 & 3\n" +
				"	                                                                       ^^^\n" +
				"Null constraint mismatch: The type \'E31 extends T31\' is not a valid substitute for the type parameter \'E extends @NonNull T\'\n" +
				"----------\n" +
				"4. ERROR in test\\TestInterdepSubClassNN.java (at line 56)\n" +
				"	static class B32<T32, @NonNull E32 extends T32> extends A3<T32, E32> { // expect error 4\n" +
				"	                                                           ^^^\n" +
				"Null constraint mismatch: The type \'T32\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
				"----------\n" +
				"5. ERROR in test\\TestInterdepSubClassNN.java (at line 67)\n" +
				"	static class B41<T41, @SomeAnnotation E41 extends T41> extends A4<T41, E41> { // expect error 5 & 6\n" +
				"	                                                                  ^^^\n" +
				"Null constraint mismatch: The type \'T41\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
				"----------\n" +
				"6. ERROR in test\\TestInterdepSubClassNN.java (at line 67)\n" +
				"	static class B41<T41, @SomeAnnotation E41 extends T41> extends A4<T41, E41> { // expect error 5 & 6\n" +
				"	                                                                       ^^^\n" +
				"Null constraint mismatch: The type \'E41 extends T41\' is not a valid substitute for the type parameter \'@NonNull E extends @NonNull T\'\n" +
				"----------\n" +
				"7. ERROR in test\\TestInterdepSubClassNN.java (at line 70)\n" +
				"	static class B42<T42, @NonNull E42 extends T42> extends A4<T42, E42> { // expect error 7\n" +
				"	                                                           ^^^\n" +
				"Null constraint mismatch: The type \'T42\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
				"----------\n"
	);
}
public void testBug485058() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test4.java",
			"package test;\n" +
			"\n" +
			"import java.io.Serializable;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class Feature4<Q extends Serializable> {\n" +
			"		Q q() {\n" +
			"			throw new RuntimeException();\n" +
			"		}\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test4 {\n" +
			"	public static <Q1 extends java.io.Serializable, F extends Feature4<Q1>> Q1[] getValues(F feature) {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"\n" +
			"	public static void f(Feature4<?> feature) {\n" +
			"		getValues(feature);\n" +
			"	}\n" +
			"\n" +
			"	public static void g(Feature4<@Nullable ? extends @NonNull Serializable> feature) {\n" +
			"		getValues(feature);\n" +
			"	}\n" +
			"}"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\Test4.java (at line 25)\n" +
		"	public static void g(Feature4<@Nullable ? extends @NonNull Serializable> feature) {\n" +
		"	                              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable ? extends @NonNull Serializable\' is not a valid substitute for the type parameter \'Q extends @NonNull Serializable\'\n" +
		"----------\n" +
		"2. WARNING in test\\Test4.java (at line 25)\n" +
		"	public static void g(Feature4<@Nullable ? extends @NonNull Serializable> feature) {\n" +
		"	                                                  ^^^^^^^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n" +
		"2. ERROR in test\\Test4.java (at line 25)\n" +
		"	public static void g(Feature4<@Nullable ? extends @NonNull Serializable> feature) {\n" +
		"	                                                  ^^^^^^^^\n" +
		"This nullness annotation conflicts with a \'@Nullable\' annotation which is effective on the same type parameter \n" +
		"----------\n" +
		"3. ERROR in test\\Test4.java (at line 26)\n" +
		"	getValues(feature);\n" +
		"	^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull Feature4<@Nullable capture#of ? extends @NonNull Serializable>\' is not a valid substitute for the type parameter \'F extends @NonNull Feature4<Q1 extends @NonNull Serializable>\'\n" +
		"----------\n"
	);
}
public void testBug485030() {
	runConformTestWithLibs(new String[] {
			"SomeAnnotation.java",
			"import static java.lang.annotation.ElementType.TYPE_USE;\n" +
			"import java.lang.annotation.Target;\n" +

			"@Target({ TYPE_USE })\n" +
			"@interface SomeAnnotation {\n" +
			"}\n",

			"TestContradictoryOnGenericArray.java",
			"import java.io.Serializable;\n" +

			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +

			"@NonNullByDefault\n" +
			"public class TestContradictoryOnGenericArray {\n" +
			"	public <@SomeAnnotation Q extends Serializable> void f() {\n" +
			"		final @Nullable Q[] array = null;\n" +
			"	}\n" +
			"}\n"
	}, getCompilerOptions(), "");
}
public void testBug485302() {
	runNegativeTestWithLibs(
		new String[] {
		"WildCardNullable.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class WildCardNullable {\n" +
			"	static class A<T> {\n" +
			"		@Nullable\n" +
			"		T returnNull() {\n" +
			"			return null;\n" +
			"		}\n" +
			"\n" +
			"		void acceptNonNullT(@NonNull T t) {\n" +
			"		}\n" +
			"\n" +
			"		void acceptNonNullObject(@NonNull Object x) {\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	static @NonNull Number g(A<? extends @NonNull Number> a) {\n" +
			"		return a.returnNull(); // error 1 expected\n" +
			"	}\n" +
			"\n" +
			"	public static final <T> void map(final A<@NonNull ? super T> a, T t) {\n" +
			"		a.acceptNonNullT(t); // warning 2 expected\n" +
			"		a.acceptNonNullObject(t); // warning 3 expected\n" +
			"	}\n" +
			"}\n" +
			""
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in WildCardNullable.java (at line 21)\n" +
		"	return a.returnNull(); // error 1 expected\n" +
		"	       ^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Number\' but this expression has type \'@Nullable capture#of ? extends Number\'\n" +
		"----------\n" +
		"2. ERROR in WildCardNullable.java (at line 25)\n" +
		"	a.acceptNonNullT(t); // warning 2 expected\n" +
		"	                 ^\n" +
		"Null type safety: required '@NonNull' but this expression has type 'T', a free type variable that may represent a '@Nullable' type\n" +
		"----------\n" +
		"3. ERROR in WildCardNullable.java (at line 26)\n" +
		"	a.acceptNonNullObject(t); // warning 3 expected\n" +
		"	                      ^\n" +
		"Null type safety: required '@NonNull' but this expression has type 'T', a free type variable that may represent a '@Nullable' type\n" +
		"----------\n"
	);
}

public void testBug485027() {
	runConformTestWithLibs(new String[] {
			"SomeAnnotation.java",
			"import static java.lang.annotation.ElementType.TYPE_USE;\n" +
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"import java.lang.annotation.Target;\n" +

			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({ TYPE_USE })\n" +
			"@interface SomeAnnotation {\n" +
			"}\n",

			"Base.java",
			"import java.io.Serializable;\n" +

			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +

			"@NonNullByDefault\n" +
			"public class Base {\n" +
			"	public <@SomeAnnotation Q extends Serializable> void setValuesArray(Q @Nullable [] value) {\n" +
			"	}\n" +
			"}\n"
	}, getCompilerOptions(), "");

	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"Derived.java",
			"import java.io.Serializable;\n" +

			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +

			"@NonNullByDefault\n" +
			"public class Derived extends Base {\n" +
			"	@Override\n" +
			"	public final <@SomeAnnotation Q1 extends Serializable> void setValuesArray(Q1 @Nullable [] value) {\n" +
			"	}\n" +
			"}"
	}, getCompilerOptions(), "");
}
public void testBug485565() {
	runConformTestWithLibs(
			new String[] {
			"test2/ClassWithRegistry.java",
			"package test2;\n" +
			"\n" +
			"import java.rmi.registry.Registry;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class ClassWithRegistry {\n" +
			"    @Nullable\n" +
			"    public Registry registry;\n" +
			"}\n"
			},
			getCompilerOptions(),
			""
		);
		runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"test1/ClassWithLambda.java",
				"package test1;\n" +
				"\n" +
				"import test2.ClassWithRegistry;\n" +
				"\n" +
				"// must be compiled before ZClassWithBug\n" +
				"public class ClassWithLambda {\n" +
				"	interface Lambda {\n" +
				"		void f();\n" +
				"	}\n" +
				"\n" +
				"	public static void invoke(Lambda lambda) {\n" +
				"		lambda.f();\n" +
				"	}\n" +
				"\n" +
				"	public void f() {\n" +
				"		new ClassWithRegistry(); // must be accessed as class file\n" +
				"		invoke(() -> java.rmi.registry.Registry.class.hashCode());\n" +
				"	}\n" +
				"}\n",
				"test1/ZClassWithBug.java",
				"package test1;\n" +
				"\n" +
				"import java.rmi.registry.Registry;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"public abstract class ZClassWithBug {\n" +
				"\n" +
				"	@Nullable\n" +
				"	public Registry rmiregistry;\n" +
				"}\n"
			},
			getCompilerOptions(),
			""
	);
}
public void testBug485814() {
	runConformTestWithLibs(
		new String[] {
			"test/ExplainedResult.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class ExplainedResult<V2> extends Result<V2> {\n" +
			"\n" +
			"	public ExplainedResult(int score, V2 extractedValue2) {\n" +
			"		super(score, extractedValue2);\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	public <OtherV2> ExplainedResult<OtherV2> withValue(OtherV2 otherValue2) {\n" +
			"		return new ExplainedResult<OtherV2>(this.score, otherValue2);\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/Result.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Result<V1> {\n" +
			"\n" +
			"	public final int score;\n" +
			"\n" +
			"	public final V1 extractedValue;\n" +
			"\n" +
			"	public Result(int score, V1 extractedValue1) {\n" +
			"		this.score = score;\n" +
			"		this.extractedValue = extractedValue1;\n" +
			"	}\n" +
			"\n" +
			"	public <OtherV1> Result<OtherV1> withValue(OtherV1 otherValue1) {\n" +
			"		return new Result<OtherV1>(score, otherValue1);\n" +
			"	}\n" +
			"}\n" +
			""
		},
		getCompilerOptions(),
		""
	);
}
public void testBug485581() {
	runConformTestWithLibs(
		new String[] {
		"test/MatchResult.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class MatchResult<V> implements Comparable<MatchResult<?>> {\n" +
			"	public final int score;\n" +
			"	public final V value;\n" +
			"\n" +
			"	public MatchResult(int score, V value) {\n" +
			"		this.score = score;\n" +
			"		this.value = value;\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	public int compareTo(MatchResult<?> o) {\n" +
			"		return score - o.score;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test/FVEHandler.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class FVEHandler {\n" +
			"	public static void process(MatchResult<?> matchResult) {\n" +
			"		if (matchResult.value != null) {\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug482752_lambda() {
	runConformTestWithLibs(
		new String[] {
			"test/StringProcessor.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public interface StringProcessor {\n" +
			"	void process(String value);\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"test/Foo.java",
				"package test;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public final class Foo {\n" +
				"\n" +
				"	public static StringProcessor createProcessorLambdaExpression() {\n" +
				"		return (@NonNull String value) -> Foo.test(value);\n" +
				"	}\n" +
				"\n" +
				"	public static void test(@NonNull String value) {\n" +
				"		System.out.println(value);\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
}

public void testBug482752_methodref() {
	runConformTestWithLibs(
		new String[] {
			"test/StringProcessor.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public interface StringProcessor {\n" +
			"	void process(String value);\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"test/Foo.java",
				"package test;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"\n" +
				"public final class Foo {\n" +
				"\n" +
				"	public static StringProcessor createProcessorMethodReference() {\n" +
				"		return Foo::test;\n" +
				"	}\n" +
				"\n" +
				"	public static void test(@NonNull String value) {\n" +
				"		System.out.println(value);\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
}

public void testBug485374() {
	runConformTestWithLibs(
		new String[] {
			"test/I.java",
			"package test;\n" +
			"public interface I<W> {\n" +
			"    public class Nested {\n" +
			"    }\n" +
			"}\n" +
			"",
			"test/D.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class D implements I<I.@NonNull Nested> {\n" +
			"}\n" +
			""
		},
		getCompilerOptions(),
		""
	);
	runWarningTestWithLibs(
			false/*flush*/,
			new String[] {
			"test2/Import.java",
				"package test2;\n" +
				"import test.D;\n" +
				"class Import {}\n"
			},
			getCompilerOptions(),
			"----------\n" +
			"1. WARNING in test2\\Import.java (at line 2)\n" +
			"	import test.D;\n" +
			"	       ^^^^^^\n" +
			"The import test.D is never used\n" +
			"----------\n"
		);
}

public void testBug466556a() {
	runNegativeTestWithLibs(
		new String[] {
			"test/C.java",
			"package test;\n" +
			"class C<T extends Number> {\n" +
			"    int consume(T t) {\n" +
			"        return t.intValue(); // NOT OK since T could be nullable\n" +
			"    }\n" +
			"    T provide() {\n" +
			"        return null;         // NOT OK since T could require nonnull\n" +
			"    }\n" +
			"}\n",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\C.java (at line 4)\n" +
		"	return t.intValue(); // NOT OK since T could be nullable\n" +
		"	       ^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. ERROR in test\\C.java (at line 7)\n" +
		"	return null;         // NOT OK since T could require nonnull\n" +
		"	       ^^^^\n" +
		"Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'\n" +
		"----------\n"
	);
}
public void testBug466556nonfree() {
	runNegativeTestWithLibs(
		new String[] {
			"test/C.java",
			"package test;\n" +
			"class C<T extends @org.eclipse.jdt.annotation.NonNull Number> {\n" +
			"    int consume(T t) {\n" +
			"        return t.intValue(); // OK since T has upper bound with @NonNull\n" +
			"    }\n" +
			"    T provide() {\n" +
			"        return null;         // NOT OK since T could require nonnull\n" +
			"    }\n" +
			"}\n",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\C.java (at line 7)\n" +
		"	return null;         // NOT OK since T could require nonnull\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'T extends @NonNull Number\' but the provided value is null\n" +
		"----------\n"
	);
}
public void testBug466556b() {
	runNegativeTestWithLibs(
		new String[] {
			"test/C.java",
			"package test;\n" +
			"\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"class C<T> {\n" +
			"	int consume(T t) {\n" +
			"		return t.hashCode();\n" +
			"	}\n" +
			"	void consume2(Supplier<T> s) {\n" +
			"		s.get().hashCode();\n" +
			"	}\n" +
			"}\n",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\C.java (at line 7)\n" +
		"	return t.hashCode();\n" +
		"	       ^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. ERROR in test\\C.java (at line 10)\n" +
		"	s.get().hashCode();\n" +
		"	^^^^^^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n"
	);
}
public void testBug466556c() {
	runNegativeTestWithLibs(
		new String[] {
			"test/C.java",
			"package test;\n" +
			"\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"class C<T extends Number> {\n" +
			"	int consume(T t) {\n" +
			"		Number n = t;\n" +
			"		return n.intValue();\n" +
			"	}\n" +
			"\n" +
			"	int consume2(Supplier<T> s) {\n" +
			"		Number n = s.get();\n" +
			"		return n.intValue();\n" +
			"	}\n" +
			"}\n",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\C.java (at line 8)\n" +
		"	return n.intValue();\n" +
		"	       ^\n" +
		"Potential null pointer access: The variable n may be null at this location\n" +
		"----------\n" +
		"2. ERROR in test\\C.java (at line 13)\n" +
		"	return n.intValue();\n" +
		"	       ^\n" +
		"Potential null pointer access: The variable n may be null at this location\n" +
		"----------\n"
	);
}
public void testBug466556field() {
	runNegativeTestWithLibs(
		new String[] {
			"test/D.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class E<T> {\n" +
			"	T t;\n" +
			"}\n" +
			"\n" +
			"class D<T> {\n" +
			"	enum X {\n" +
			"		x\n" +
			"	};\n" +
			"\n" +
			"	T t1;\n" +
			"	T t2;\n" +
			"	T t3;\n" +
			"	@Nullable\n" +
			"	T t4;\n" +
			"	@NonNull\n" +
			"	T t5;\n" +
			"	@NonNull\n" +
			"	T t6;\n" +
			"	@NonNull\n" +
			"	T t7;\n" +
			"\n" +
			"	D(@NonNull T t) {\n" +
			"		t2 = t;\n" +
			"		switch (X.x) {\n" +
			"		case x:\n" +
			"			t1 = t;\n" +
			"			t5 = t;\n" +
			"		}\n" +
			"		t6 = t;\n" +
			"	}\n" +
			"\n" +
			"	void f() {\n" +
			"		t1.hashCode();\n" +
			"		t2.hashCode();\n" +
			"		t3.hashCode();\n" +
			"		t4.hashCode();\n" +
			"		t5.hashCode();\n" +
			"		t6.hashCode();\n" +
			"		t7.hashCode();\n" +
			"		T t = t1;\n" +
			"		t.hashCode();\n" +
			"	}\n" +
			"	void g() {\n" +
			"		if(t1 != null)\n" +
			"			t1.hashCode();\n // problem report expected because syntactic null analysis for fields is off\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\D.java (at line 7)\n" +
		"	T t;\n" +
		"	  ^\n" +
		"The field t may not have been initialized, whereas its type \'T\' is a free type variable that may represent a \'@NonNull\' type\n" +
		"----------\n" +
		"2. ERROR in test\\D.java (at line 27)\n" +
		"	D(@NonNull T t) {\n" +
		"	^^^^^^^^^^^^^^^\n" +
		"The field t1 may not have been initialized, whereas its type \'T\' is a free type variable that may represent a \'@NonNull\' type. Note that a problem regarding missing \'default:\' on \'switch\' has been suppressed, which is perhaps related to this problem\n" +
		"----------\n" +
		"3. ERROR in test\\D.java (at line 27)\n" +
		"	D(@NonNull T t) {\n" +
		"	^^^^^^^^^^^^^^^\n" +
		"The field t3 may not have been initialized, whereas its type \'T\' is a free type variable that may represent a \'@NonNull\' type. Note that a problem regarding missing \'default:\' on \'switch\' has been suppressed, which is perhaps related to this problem\n" +
		"----------\n" +
		"4. ERROR in test\\D.java (at line 27)\n" +
		"	D(@NonNull T t) {\n" +
		"	^^^^^^^^^^^^^^^\n" +
		"The @NonNull field t5 may not have been initialized. Note that a problem regarding missing \'default:\' on \'switch\' has been suppressed, which is perhaps related to this problem\n" +
		"----------\n" +
		"5. ERROR in test\\D.java (at line 27)\n" +
		"	D(@NonNull T t) {\n" +
		"	^^^^^^^^^^^^^^^\n" +
		"The @NonNull field t7 may not have been initialized. Note that a problem regarding missing \'default:\' on \'switch\' has been suppressed, which is perhaps related to this problem\n" +
		"----------\n" +
		"6. ERROR in test\\D.java (at line 38)\n" +
		"	t1.hashCode();\n" +
		"	^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"7. ERROR in test\\D.java (at line 39)\n" +
		"	t2.hashCode();\n" +
		"	^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"8. ERROR in test\\D.java (at line 40)\n" +
		"	t3.hashCode();\n" +
		"	^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"9. ERROR in test\\D.java (at line 41)\n" +
		"	t4.hashCode();\n" +
		"	^^\n" +
		"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
		"----------\n" +
		"10. ERROR in test\\D.java (at line 46)\n" +
		"	t.hashCode();\n" +
		"	^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"11. ERROR in test\\D.java (at line 50)\n" +
		"	t1.hashCode();\n" +
		"	^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n"
	);
}
public void testBug466556withRaw() {
	runConformTestWithLibs(
		new String[] {
			"test/TestWithRaw.java",
			"package test;\n" +
			"\n" +
			"public class TestWithRaw {\n" +
			"	@SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n" +
			"	public static void uncheckedEnumValueOf(final Class<?> valueClass, final String value) {\n" +
			"		Class valueClass2 = valueClass;\n" +
			"		Enum.valueOf(valueClass2, value).name();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug466556withPGMB() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestWithParameterizedGenericMethodBinding.java",
			"package test;\n" +
			"\n" +
			"public class TestWithParameterizedGenericMethodBinding {\n" +
			"	static <T, E extends T> T f1(E e) {\n" +
			"		return e;\n" +
			"	}\n" +
			"\n" +
			"	static <T11, E11 extends T11> void g11(E11 e) {\n" +
			"		f1(e).hashCode();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\TestWithParameterizedGenericMethodBinding.java (at line 9)\n" +
		"	f1(e).hashCode();\n" +
		"	^^^^^\n" +
		"Potential null pointer access: this expression has type \'E11\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n"
	);
}
public void testBug466556captures() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestCapture.java",
			"package test;\n" +
			"\n" +
			"class I {\n" +
			"	int i;\n" +
			"\n" +
			"	String s() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class KE<E extends I> {\n" +
			"	public final E e;\n" +
			"\n" +
			"	public E getE() {\n" +
			"		return e;\n" +
			"	}\n" +
			"\n" +
			"	public KE(E element) {\n" +
			"		this.e = element;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class TestFreeTypeVariable<E2 extends I> {\n" +
			"	public void test(KE<E2> ke) {\n" +
			"		int i1 = ke.e.i; // error 1\n" +
			"		ke.e.s().substring(i1); // error 2\n" +
			"		int i2 = ke.getE().i; // error 3\n" +
			"		ke.getE().s().substring(i2); // error 4\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class TestCapture {\n" +
			"	public void test(KE<? extends I> ke) {\n" +
			"		int i1 = ke.e.i; // error 5\n" +
			"		ke.e.s().substring(i1); // error 6\n" +
			"		int i2 = ke.getE().i; // error 7\n" +
			"		ke.getE().s().substring(i2); // error 8\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\TestCapture.java (at line 25)\n" +
		"	int i1 = ke.e.i; // error 1\n" +
		"	            ^\n" +
		"Potential null pointer access: this expression has type \'E2\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. ERROR in test\\TestCapture.java (at line 26)\n" +
		"	ke.e.s().substring(i1); // error 2\n" +
		"	   ^\n" +
		"Potential null pointer access: this expression has type \'E2\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"3. ERROR in test\\TestCapture.java (at line 27)\n" +
		"	int i2 = ke.getE().i; // error 3\n" +
		"	         ^^^^^^^^^\n" +
		"Potential null pointer access: this expression has type \'E2\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"4. ERROR in test\\TestCapture.java (at line 28)\n" +
		"	ke.getE().s().substring(i2); // error 4\n" +
		"	^^^^^^^^^\n" +
		"Potential null pointer access: this expression has type \'E2\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"5. ERROR in test\\TestCapture.java (at line 34)\n" +
		"	int i1 = ke.e.i; // error 5\n" +
		"	            ^\n" +
		"Potential null pointer access: this expression has type \'capture#1-of ? extends test.I\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"6. ERROR in test\\TestCapture.java (at line 35)\n" +
		"	ke.e.s().substring(i1); // error 6\n" +
		"	   ^\n" +
		"Potential null pointer access: this expression has type \'capture#2-of ? extends test.I\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"7. ERROR in test\\TestCapture.java (at line 36)\n" +
		"	int i2 = ke.getE().i; // error 7\n" +
		"	         ^^^^^^^^^\n" +
		"Potential null pointer access: this expression has type \'capture#3-of ? extends test.I\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"8. ERROR in test\\TestCapture.java (at line 37)\n" +
		"	ke.getE().s().substring(i2); // error 8\n" +
		"	^^^^^^^^^\n" +
		"Potential null pointer access: this expression has type \'capture#4-of ? extends test.I\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n"
	);
}
public void testBug466556Loops() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestLoop.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class TestLoop<T> {\n" +
			"	boolean b;\n" +
			"\n" +
			"	public static void nn(@NonNull Object value) {\n" +
			"		assert value != null;\n" +
			"	}\n" +
			"\n" +
			"	public void testDoWhile(T t1) {\n" +
			"		nn(t1); // 1: unchecked warning\n" +
			"		do {\n" +
			"			nn(t1); // 2: unchecked warning\n" +
			"			t1.hashCode(); // 3: Potential null pointer access...free type variable\n" +
			"		} while (b);\n" +
			"	}\n" +
			"\n" +
			"	public void testWhileWithBreak(T t1) {\n" +
			"		while (true) {\n" +
			"			nn(t1); // 4: unchecked warning\n" +
			"			t1.hashCode(); // 5: Potential null pointer access...free type variable\n" +
			"			if (b)\n" +
			"				break;\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testWhile(T t1) {\n" +
			"		while (TestLoop.class.hashCode() == 4711) {\n" +
			"			nn(t1); // 6: unchecked warning\n" +
			"			t1.hashCode(); // 7: Potential null pointer access...free type variable\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testFor(T t1) {\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			nn(t1); // 8: unchecked warning\n" +
			"			t1.hashCode(); // 9: Potential null pointer access...free type variable\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testForEach(T t1) {\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			nn(t1); // 10: unchecked warning\n" +
			"			t1.hashCode(); // 11: Potential null pointer access: The variable t1 may be null at this location\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\TestLoop.java (at line 13)\n" +
		"	nn(t1); // 1: unchecked warning\n" +
		"	   ^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. ERROR in test\\TestLoop.java (at line 15)\n" +
		"	nn(t1); // 2: unchecked warning\n" +
		"	   ^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"3. ERROR in test\\TestLoop.java (at line 16)\n" +
		"	t1.hashCode(); // 3: Potential null pointer access...free type variable\n" +
		"	^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"4. ERROR in test\\TestLoop.java (at line 22)\n" +
		"	nn(t1); // 4: unchecked warning\n" +
		"	   ^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"5. ERROR in test\\TestLoop.java (at line 23)\n" +
		"	t1.hashCode(); // 5: Potential null pointer access...free type variable\n" +
		"	^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"6. ERROR in test\\TestLoop.java (at line 31)\n" +
		"	nn(t1); // 6: unchecked warning\n" +
		"	   ^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"7. ERROR in test\\TestLoop.java (at line 32)\n" +
		"	t1.hashCode(); // 7: Potential null pointer access...free type variable\n" +
		"	^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"8. ERROR in test\\TestLoop.java (at line 38)\n" +
		"	nn(t1); // 8: unchecked warning\n" +
		"	   ^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"9. ERROR in test\\TestLoop.java (at line 39)\n" +
		"	t1.hashCode(); // 9: Potential null pointer access...free type variable\n" +
		"	^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"10. ERROR in test\\TestLoop.java (at line 45)\n" +
		"	nn(t1); // 10: unchecked warning\n" +
		"	   ^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"11. ERROR in test\\TestLoop.java (at line 46)\n" +
		"	t1.hashCode(); // 11: Potential null pointer access: The variable t1 may be null at this location\n" +
		"	^^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n"
	);
}
public void testBug461268() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, JavaCore.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_PessimisticNullAnalysisForFreeTypeVariables, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	void test(List<@NonNull String> list) {\n" +
			"		@NonNull String s = list.get(0);\n" +
			"	}\n" +
			"}\n"
		},
		compilerOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	@NonNull String s = list.get(0);\n" +
		"	                    ^^^^^^^^^^^\n" +
		"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'List<@NonNull String>\'. Type \'List<E>\' doesn\'t seem to be designed with null type annotations in mind\n" +
		"----------\n");
}
public void testBug461268invoke() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import java.util.Map;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	void test(Map<Object, @NonNull String> map) {\n" +
			"		map.get(this).length();\n" +
			"	}\n" +
			"}\n"
		},
		compilerOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	map.get(this).length();\n" +
		"	^^^^^^^^^^^^^\n" +
		"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'Map<Object,@NonNull String>\'. Type \'Map<K,V>\' doesn\'t seem to be designed with null type annotations in mind\n" +
		"----------\n");
}
public void testBug461268nnbd() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportNonNullTypeVariableFromLegacyInvocation, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"test2/Container.java",
			"package test2;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Container<T> {\n" +
			"	public static <T> T getFrom(Container<T> container) {\n" +
			"		return container.get();\n" +
			"	}\n" +
			"\n" +
			"	private final T t;\n" +
			"\n" +
			"	public Container(T t) {\n" +
			"		this.t = t;\n" +
			"	}\n" +
			"\n" +
			"	private T get() {\n" + // we really mean 'T' unannotated, believe it due to @NonNullByDefault
			"		return this.t;\n" +
			"	}\n" +
			"}\n",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"import test2.Container;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"	String f(Container<String> c) {\n" +
			"		return Container.getFrom(c);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug485988WildcardOverride() {
	runConformTestWithLibs(
		new String[] {
			"test/Result.java",
			"package test;\n" +
			"\n" +
			"public class Result<V> implements Comparable<Result<?>> {\n" +
			"	public final int score;\n" +
			"	public final V value;\n" +
			"\n" +
			"	protected Result(int score, V value) {\n" +
			"		this.score = score;\n" +
			"		this.value = value;\n" +
			"	}\n" +
			"	@Override\n" +
			"	public int compareTo(Result<?> o) {\n" +
			"		return score - o.score;\n" +
			"	}\n" +
			"}\n",
			"test/Base.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public abstract class Base {\n" +
			"	public abstract Result<?> matches();\n" +
			"}\n",
			"test/Derived.java",
			"package test;\n" +
			"\n" +
			"import java.math.BigDecimal;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Derived extends Base {\n" +
			"	@Override\n" +
			"	public Result<BigDecimal> matches() {\n" +
			"		return new Result<BigDecimal>(0, new BigDecimal(\"1\"));\n" +
			"	}\n" +
			"}\n",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug485988neutral() {
	runConformTestWithLibs(
		new String[] {
			"neutral/WildcardTest.java",
			"package neutral;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class A<T> {\n" +
			"}\n" +
			"\n" +
			"abstract class X {\n" +
			"	abstract A<?> g1();\n" +
			"\n" +
			"	abstract A<?> g2();\n" +
			"\n" +
			"	abstract A<?> g2b();\n" +
			"\n" +
			"	abstract A<?> g3();\n" +
			"\n" +
			"	abstract A<?> h1();\n" +
			"\n" +
			"	abstract A<?> h2();\n" +
			"\n" +
			"	abstract A<?> h2b();\n" +
			"\n" +
			"	abstract A<?> h3();\n" +
			"}\n" +
			"\n" +
			"class Y extends X {\n" +
			"	@Override\n" +
			"	A<?> g1() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull ?> g2() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? extends @NonNull Number> g2b() {\n" +
			"		return new A<@NonNull Integer>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull String> g3() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<?> h1() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable ?> h2() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? super @Nullable Number> h2b() {\n" +
			"		return new A<@Nullable Object>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable String> h3() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class WildcardTest {\n" +
			"	void f(A<?> a) {\n" +
			"	}\n" +
			"\n" +
			"	<T1> void g(A<T1> a) {\n" +
			"	}\n" +
			"\n" +
			"	<T2> void invoke(T2 t) {\n" +
			"		f(new A<T2>());\n" +
			"		g(new A<T2>());\n" +
			"\n" +
			"		f(new A<@NonNull T2>());\n" +
			"		g(new A<@NonNull T2>());\n" +
			"\n" +
			"		f(new A<@Nullable T2>());\n" +
			"		g(new A<@Nullable T2>());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug485988nonnull() {
	runNegativeTestWithLibs(
		new String[] {
			"nonnull/WildcardNonNullTest.java",
			"package nonnull;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class A<@NonNull T> {\n" +
			"}\n" +
			"\n" +
			"abstract class X {\n" +
			"	abstract A<@NonNull ?> g1();\n" +
			"\n" +
			"	abstract A<@NonNull ?> g2();\n" +
			"\n" +
			"	abstract A<@NonNull ?> g2b();\n" +
			"\n" +
			"	abstract A<@NonNull ?> g3();\n" +
			"\n" +
			"	abstract A<@NonNull ?> h1();\n" +
			"\n" +
			"	abstract A<@NonNull ?> h2();\n" +
			"\n" +
			"	abstract A<@NonNull ?> h2b();\n" +
			"\n" +
			"	abstract A<@NonNull ?> h3();\n" +
			"}\n" +
			"\n" +
			"class Y extends X {\n" +
			"	@Override\n" +
			"	A<?> g1() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull ?> g2() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? extends @NonNull Number> g2b() {\n" +
			"		return new A<@NonNull Integer>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull String> g3() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<?> h1() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable ?> h2() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? super @Nullable String> h2b() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable String> h3() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class WildcardNonNullTest {\n" +
			"	void f(A<?> a) {\n" +
			"	}\n" +
			"\n" +
			"	<@NonNull T1> void g(A<T1> a) {\n" +
			"	}\n" +
			"\n" +
			"	<T2> void invoke(T2 t) {\n" +
			"		f(new A<T2>());\n" +
			"		g(new A<T2>());\n" +
			"\n" +
			"		f(new A<@NonNull T2>());\n" +
			"		g(new A<@NonNull T2>());\n" +
			"\n" +
			"		f(new A<@Nullable T2>());\n" +
			"		g(new A<@Nullable T2>());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in nonnull\\WildcardNonNullTest.java (at line 52)\n" +
		"	return new A<@Nullable String>();\n" +
		"	             ^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"2. ERROR in nonnull\\WildcardNonNullTest.java (at line 56)\n" +
		"	A<@Nullable ?> h2() {\n" +
		"	^\n" +
		"The return type is incompatible with \'A<@NonNull ?>\' returned from X.h2() (mismatching null constraints)\n" +
		"----------\n" +
		"3. ERROR in nonnull\\WildcardNonNullTest.java (at line 56)\n" +
		"	A<@Nullable ?> h2() {\n" +
		"	  ^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable ?\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"4. ERROR in nonnull\\WildcardNonNullTest.java (at line 57)\n" +
		"	return new A<@Nullable String>();\n" +
		"	             ^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"5. ERROR in nonnull\\WildcardNonNullTest.java (at line 61)\n" +
		"	A<? super @Nullable String> h2b() {\n" +
		"	^\n" +
		"The return type is incompatible with \'A<@NonNull ?>\' returned from X.h2b() (mismatching null constraints)\n" +
		"----------\n" +
		"6. ERROR in nonnull\\WildcardNonNullTest.java (at line 61)\n" +
		"	A<? super @Nullable String> h2b() {\n" +
		"	  ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'? super @Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"7. ERROR in nonnull\\WildcardNonNullTest.java (at line 62)\n" +
		"	return new A<@Nullable String>();\n" +
		"	             ^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"8. ERROR in nonnull\\WildcardNonNullTest.java (at line 66)\n" +
		"	A<@Nullable String> h3() {\n" +
		"	^\n" +
		"The return type is incompatible with \'A<@NonNull ?>\' returned from X.h3() (mismatching null constraints)\n" +
		"----------\n" +
		"9. ERROR in nonnull\\WildcardNonNullTest.java (at line 66)\n" +
		"	A<@Nullable String> h3() {\n" +
		"	  ^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"10. ERROR in nonnull\\WildcardNonNullTest.java (at line 67)\n" +
		"	return new A<@Nullable String>();\n" +
		"	             ^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"11. ERROR in nonnull\\WildcardNonNullTest.java (at line 80)\n" +
		"	f(new A<T2>());\n" +
		"	        ^^\n" +
		"Null constraint mismatch: The type \'T2\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"12. WARNING in nonnull\\WildcardNonNullTest.java (at line 81)\n" +
		"	g(new A<T2>());\n" +
		"	  ^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'@NonNull A<T2>\' needs unchecked conversion to conform to \'@NonNull A<@NonNull T2>\'\n" +
		"----------\n" +
		"13. ERROR in nonnull\\WildcardNonNullTest.java (at line 81)\n" +
		"	g(new A<T2>());\n" +
		"	        ^^\n" +
		"Null constraint mismatch: The type \'T2\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"14. ERROR in nonnull\\WildcardNonNullTest.java (at line 86)\n" +
		"	f(new A<@Nullable T2>());\n" +
		"	        ^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable T2\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n" +
		"15. ERROR in nonnull\\WildcardNonNullTest.java (at line 87)\n" +
		"	g(new A<@Nullable T2>());\n" +
		"	  ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull A<@NonNull T2>\' but this expression has type \'@NonNull A<@Nullable T2>\'\n" +
		"----------\n" +
		"16. ERROR in nonnull\\WildcardNonNullTest.java (at line 87)\n" +
		"	g(new A<@Nullable T2>());\n" +
		"	        ^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable T2\' is not a valid substitute for the type parameter \'@NonNull T\'\n" +
		"----------\n"
	);
}
public void testBug485988nullable() {
	runNegativeTestWithLibs(
		new String[] {
			"nullable/WildcardNullableTest.java",
			"package nullable;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class A<@Nullable T> {\n" +
			"}\n" +
			"\n" +
			"abstract class X {\n" +
			"	abstract A<@Nullable ?> g1();\n" +
			"\n" +
			"	abstract A<@Nullable ?> g2();\n" +
			"\n" +
			"	abstract A<@Nullable ?> g2b();\n" +
			"\n" +
			"	abstract A<@Nullable ?> g3();\n" +
			"\n" +
			"	abstract A<@Nullable ?> h1();\n" +
			"\n" +
			"	abstract A<@Nullable ?> h2();\n" +
			"\n" +
			"	abstract A<@Nullable ?> h2b();\n" +
			"\n" +
			"	abstract A<@Nullable ?> h3();\n" +
			"}\n" +
			"\n" +
			"class Y extends X {\n" +
			"	@Override\n" +
			"	A<?> g1() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull ?> g2() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? extends @NonNull Number> g2b() {\n" +
			"		return new A<@NonNull Integer>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@NonNull String> g3() {\n" +
			"		return new A<@NonNull String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<?> h1() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable ?> h2() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<? super @Nullable String> h2b() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"\n" +
			"	@Override\n" +
			"	A<@Nullable String> h3() {\n" +
			"		return new A<@Nullable String>();\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class WildcardNullableTest {\n" +
			"	void f(A<?> a) {\n" +
			"	}\n" +
			"\n" +
			"	<@Nullable T1> void g(A<T1> a) {\n" +
			"	}\n" +
			"\n" +
			"	<T2> void invoke(T2 t) {\n" +
			"		f(new A<T2>());\n" +
			"		g(new A<T2>());\n" +
			"\n" +
			"		f(new A<@NonNull T2>());\n" +
			"		g(new A<@NonNull T2>());\n" +
			"\n" +
			"		f(new A<@Nullable T2>());\n" +
			"		g(new A<@Nullable T2>());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in nullable\\WildcardNullableTest.java (at line 32)\n" +
		"	return new A<@NonNull String>();\n" +
		"	             ^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"2. ERROR in nullable\\WildcardNullableTest.java (at line 36)\n" +
		"	A<@NonNull ?> g2() {\n" +
		"	^\n" +
		"The return type is incompatible with \'A<@Nullable ?>\' returned from X.g2() (mismatching null constraints)\n" +
		"----------\n" +
		"3. ERROR in nullable\\WildcardNullableTest.java (at line 36)\n" +
		"	A<@NonNull ?> g2() {\n" +
		"	  ^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull ?\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"4. ERROR in nullable\\WildcardNullableTest.java (at line 37)\n" +
		"	return new A<@NonNull String>();\n" +
		"	             ^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"5. ERROR in nullable\\WildcardNullableTest.java (at line 41)\n" +
		"	A<? extends @NonNull Number> g2b() {\n" +
		"	^\n" +
		"The return type is incompatible with \'A<@Nullable ?>\' returned from X.g2b() (mismatching null constraints)\n" +
		"----------\n" +
		"6. ERROR in nullable\\WildcardNullableTest.java (at line 41)\n" +
		"	A<? extends @NonNull Number> g2b() {\n" +
		"	  ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'? extends @NonNull Number\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"7. ERROR in nullable\\WildcardNullableTest.java (at line 42)\n" +
		"	return new A<@NonNull Integer>();\n" +
		"	             ^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull Integer\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"8. ERROR in nullable\\WildcardNullableTest.java (at line 46)\n" +
		"	A<@NonNull String> g3() {\n" +
		"	^\n" +
		"The return type is incompatible with \'A<@Nullable ?>\' returned from X.g3() (mismatching null constraints)\n" +
		"----------\n" +
		"9. ERROR in nullable\\WildcardNullableTest.java (at line 46)\n" +
		"	A<@NonNull String> g3() {\n" +
		"	  ^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"10. ERROR in nullable\\WildcardNullableTest.java (at line 47)\n" +
		"	return new A<@NonNull String>();\n" +
		"	             ^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"11. ERROR in nullable\\WildcardNullableTest.java (at line 80)\n" +
		"	f(new A<T2>());\n" +
		"	        ^^\n" +
		"Null constraint mismatch: The type \'T2\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"12. WARNING in nullable\\WildcardNullableTest.java (at line 81)\n" +
		"	g(new A<T2>());\n" +
		"	  ^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'@NonNull A<T2>\' needs unchecked conversion to conform to \'@NonNull A<@Nullable T2>\'\n" +
		"----------\n" +
		"13. ERROR in nullable\\WildcardNullableTest.java (at line 81)\n" +
		"	g(new A<T2>());\n" +
		"	        ^^\n" +
		"Null constraint mismatch: The type \'T2\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"14. ERROR in nullable\\WildcardNullableTest.java (at line 83)\n" +
		"	f(new A<@NonNull T2>());\n" +
		"	        ^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull T2\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n" +
		"15. ERROR in nullable\\WildcardNullableTest.java (at line 84)\n" +
		"	g(new A<@NonNull T2>());\n" +
		"	  ^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull A<@Nullable T2>\' but this expression has type \'@NonNull A<@NonNull T2>\'\n" +
		"----------\n" +
		"16. ERROR in nullable\\WildcardNullableTest.java (at line 84)\n" +
		"	g(new A<@NonNull T2>());\n" +
		"	        ^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@NonNull T2\' is not a valid substitute for the type parameter \'@Nullable T\'\n" +
		"----------\n"
	);
}
public void testBug485988WildCardForTVWithNonNullBound() {
	runConformTestWithLibs(
		new String[] {
			"test/WildCard.java",
			"package test;\n" +
			"\n" +
			"import java.io.Serializable;\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class F<T extends Serializable> {\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class WildCard {\n" +
			"	void f(ArrayList<F<?>> list) {\n" +
			"		for (F<? extends Serializable> f : list) {\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug485988WildcardWithGenericBound() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"test/Test1.java",
			"package test;\n" +
			"import java.util.Collection;\n" +
			"import java.util.Iterator;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"interface LibA {\n" +
			"	<T> Iterator<? extends T> constrainedWildcards(Collection<? extends T> in);\n" +
			"}\n" +
			"public class Test1 {\n" +
			"	Iterator<? extends @NonNull String> test3(LibA lib, Collection<String> coll) {\n" +
			"		return lib.constrainedWildcards(coll);\n" +
			"	}\n" +
			"}\n" +
			"\n",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in test\\Test1.java (at line 11)\n" +
		"	return lib.constrainedWildcards(coll);\n" +
		"	                                ^^^^\n" +
		"Null type safety (type annotations): The expression of type \'Collection<String>\' needs unchecked conversion to conform to \'Collection<? extends @NonNull String>\'\n" +
		"----------\n"
	);
}
public void testBug485988Contradictory() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test1.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface A<T> {\n" +
			"}\n" +
			"public class Test1{\n" +
			"	void f1(A<@Nullable @NonNull ?> a) {\n" +
			"	}\n" +
			"	void f2(A<@NonNull @Nullable ?> a) {\n" +
			"	}\n" +
			"	void f3(A<@Nullable ? extends @NonNull Object> a) {\n" +
			"	}\n" +
			"	void f4(A<@NonNull ? super @Nullable Integer> a) {\n" +
			"	}\n" +
			"	void f5(A<@Nullable ? super @Nullable Integer> a) {\n" + // OK
			"	}\n" +
			"	@NonNullByDefault void f6(A<@Nullable ? extends Integer> a) {\n" + // OK
			"	}\n" +
			"}\n" +
			"\n",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\Test1.java (at line 7)\n" +
		"	void f1(A<@Nullable @NonNull ?> a) {\n" +
		"	                    ^^^^^^^^\n" +
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" +
		"----------\n" +
		"2. ERROR in test\\Test1.java (at line 9)\n" +
		"	void f2(A<@NonNull @Nullable ?> a) {\n" +
		"	                   ^^^^^^^^^\n" +
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" +
		"----------\n" +
		"3. ERROR in test\\Test1.java (at line 11)\n" +
		"	void f3(A<@Nullable ? extends @NonNull Object> a) {\n" +
		"	                              ^^^^^^^^\n" +
		"This nullness annotation conflicts with a \'@Nullable\' annotation which is effective on the same type parameter \n" +
		"----------\n" +
		"4. ERROR in test\\Test1.java (at line 13)\n" +
		"	void f4(A<@NonNull ? super @Nullable Integer> a) {\n" +
		"	                           ^^^^^^^^^\n" +
		"This nullness annotation conflicts with a \'@NonNull\' annotation which is effective on the same type parameter \n" +
		"----------\n"
	);
}
public void testBug485988bound() {
	runConformTestWithLibs(
		new String[] {
			"C.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"interface I<T> { }\n" +
			"\n" +
			"public class C {\n" +
			"	I<@NonNull ?> m1(I<? extends @NonNull C> i) {\n" +
			"		return i;\n" +
			"	}\n" +
			"	I<? extends @NonNull C> m2(I<@NonNull ? extends C> i) {\n" +
			"		return i;\n" +
			"	}\n" +
			"	\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug466585_comment_0() {
	runConformTestWithLibs(
		new String[] {
			"C3.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"class C3<T extends @NonNull Number> {\n" +
			"    C3<?> x; // Null constraint mismatch: The type '?' is not a valid substitute for the type parameter 'T extends @NonNull Number'\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug466585_comment_4() {
	runNegativeTestWithLibs(
		new String[] {
			"C3.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"class C4<T extends @NonNull Number> {\n" +
			"  C4<@Nullable ?> err1;\n" +
			"  C4<@Nullable ? extends Integer> err2;\n" +
			"  C4<? super @Nullable Integer> err3;\n" +
			"  C4<@Nullable ? super Integer> err4;\n" +
			"  C4<@NonNull ? super Integer> ok1;\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in C3.java (at line 4)\n" +
		"	C4<@Nullable ?> err1;\n" +
		"	   ^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable ?\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'\n" +
		"----------\n" +
		"2. ERROR in C3.java (at line 5)\n" +
		"	C4<@Nullable ? extends Integer> err2;\n" +
		"	   ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable ? extends Integer\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'\n" +
		"----------\n" +
		"3. ERROR in C3.java (at line 6)\n" +
		"	C4<? super @Nullable Integer> err3;\n" +
		"	   ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'? super @Nullable Integer\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'\n" +
		"----------\n" +
		"4. ERROR in C3.java (at line 7)\n" +
		"	C4<@Nullable ? super Integer> err4;\n" +
		"	   ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'@Nullable ? super Integer\' is not a valid substitute for the type parameter \'T extends @NonNull Number\'\n" +
		"----------\n");
}
public void testBug489978() {
	runConformTestWithLibs(
		new String[] {
			"test/BinaryClass.java",
			"package test;\n" +
			"\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class BinaryClass {\n" +
			"	public ArrayList<Object> list;\n" +
			"\n" +
			"	public BinaryClass(ArrayList<Object> list) {\n" +
			"		this.list = list;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test/Usage.java",
			"package test;\n" +
			"\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Usage {\n" +
			"	ArrayList<Object> f(BinaryClass b) {\n" +
			"		return b.list;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug489245() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_PessimisticNullAnalysisForFreeTypeVariables, JavaCore.INFO);
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"test/TestBogusProblemReportOnlyAsInfo.java",
			"package test;\n" +
			"\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class TestBogusProblemReportOnlyAsInfo {\n" +
			"	static <U> void get(Supplier<U> supplier, @NonNull U defaultValue) {\n" +
			"	}\n" +
			"\n" +
			"	static void f() {\n" +
			"		get(() -> {\n" +
			"			return null; // bogus problem report only as info\n" +
			"		}, \"\");\n" +
			"	}\n" +
			"\n" +
			"	static <T> void h(@NonNull T t) {\n" +
			"		get(() -> {\n" +
			"			return null; // correctly reported (but twice with the bug)\n" +
			"		}, t);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		compilerOptions,
		"----------\n" +
		"1. INFO in test\\TestBogusProblemReportOnlyAsInfo.java (at line 21)\n" +
		"	return null; // correctly reported (but twice with the bug)\n" +
		"	       ^^^^\n" +
		"Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'\n" +
		"----------\n"
	);
}
public void testBug489674() {
	Map options = new HashMap<>(getCompilerOptions());
	options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_SECONDARY_NAMES, "org.foo.NonNull");
	options.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_SECONDARY_NAMES, "org.foo.Nullable");
	runConformTest(
		new String[] {
			"org/foo/Nullable.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"import static java.lang.annotation.ElementType.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({ FIELD, METHOD, PARAMETER, LOCAL_VARIABLE })\n" +
			"public @interface Nullable {}\n",
			"org/foo/NonNull.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"import static java.lang.annotation.ElementType.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({ FIELD, METHOD, PARAMETER, LOCAL_VARIABLE })\n" +
			"public @interface NonNull {}\n",
		},
		options);
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"with_other_nullable/P1.java",
				"package with_other_nullable;\n" +
				"\n" +
				"import org.foo.Nullable;\n" +
				"\n" +
				"public class P1 {\n" +
				"	public static @Nullable String f0() {\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"	public static <T> T check(T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"}\n" +
				"",
				"with_other_nullable/P2.java",
				"package with_other_nullable;\n" +
				"\n" +
				"import org.foo.NonNull;\n" +
				"\n" +
				"public class P2 {\n" +
				"	public static void f(@NonNull String s) {\n" +
				"	}\n" +
				"\n" +
				"	public static <T> T check(T t) {\n" +
				"		return t;\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			options,
			""
	);
	runNegativeTestWithLibs(
			false /* don't flush */,
			new String[] {
				"test/Test4.java",
				"package test;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"\n" +
				"import with_other_nullable.P1;\n" +
				"import with_other_nullable.P2;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"public class Test4 {\n" +
				"	void m1(String s) {\n" +
				"		P1.f0().hashCode();\n" +
				"		s = P1.check(s);\n" +
				"	}\n" +
				"	void m2(String s) {\n" +
				"		P2.f(null);\n" +
				"		s = P2.check(s);\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			options,
			"----------\n" +
			"1. ERROR in test\\Test4.java (at line 11)\n" +
			"	P1.f0().hashCode();\n" +
			"	^^^^^^^\n" +
			"Potential null pointer access: The method f0() may return null\n" +
			"----------\n" +
			"2. ERROR in test\\Test4.java (at line 15)\n" +
			"	P2.f(null);\n" +
			"	     ^^^^\n" +
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
			"----------\n",
			false
		);
}
public void testBug492327() {
	runConformTestWithLibs(
		new String[] {
			"WatchEvent.java",
			"public interface WatchEvent<T> {\n" +
			"	public static interface Modifier {\n" +
			"	}\n" +
			"}\n",
			"Watchable.java",
			"public interface Watchable {\n" +
			"	void register(WatchEvent.Modifier[] modifiers);\n" +
			"}\n",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"Path.java",
			"public interface Path extends Watchable {\n" +
			"  @Override\n" +
			"  void register(WatchEvent.Modifier[] modifiers);\n" +
			"}\n",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug488495collector() {
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"interface Collector<A, R> {\n" +
			"}\n" +
			"\n" +
			"interface Stream {\n" +
			"    <A1, R1> R1 collect(Collector<A1, R1> collector);\n" +
			"}\n" +
			"\n" +
			"interface List<E> {\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"    public static <T> Collector<?, List<T>> toList() {\n" +
			"        return new Collector<Object, List<T>>(){};\n" +
			"    }\n" +
			"\n" +
			"    public static List<String> myMethod(Stream stream) {\n" +
			"        List<String> list = stream.collect(toList());\n" +
			"        return list;\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}

public void testBug496591() {
	runConformTestWithLibs(
		new String[] {
			"test2/Descriptors.java",
			"package test2;\n" +
			"\n" +
			"public final class Descriptors {\n" +
			"	public static final class FieldDescriptor implements FieldSet.FieldDescriptorLite<FieldDescriptor> { }\n" +
			"}\n" +
			"",
			"test2/FieldSet.java",
			"package test2;\n" +
			"\n" +
			"public final class FieldSet<F1 extends FieldSet.FieldDescriptorLite<F1>> {\n" +
			"	public interface FieldDescriptorLite<F2 extends FieldDescriptorLite<F2>> { }\n" +
			"\n" +
			"	void f(final Map.Entry<F1> entry) { }\n" +
			"}\n" +
			"",
			"test2/Map.java",
			"package test2;\n" +
			"\n" +
			"public class Map<K> {\n" +
			"	interface Entry<K1> { }\n" +
			"}\n" +
			"",
			"test2/MessageOrBuilder.java",
			"package test2;\n" +
			"\n" +
			"public interface MessageOrBuilder {\n" +
			"	Map<Descriptors.FieldDescriptor> getAllFields();\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test1/GeneratedMessage.java",
			"package test1;\n" +
			"\n" +
			"import test2.Descriptors.FieldDescriptor;\n" +
			"import test2.Map;\n" +
			"import test2.MessageOrBuilder;\n" +
			"\n" +
			"public abstract class GeneratedMessage implements MessageOrBuilder {\n" +
			"	@Override\n" +
			"	public abstract Map<FieldDescriptor> getAllFields();\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug497698() {
	runNegativeTest(
		new String[] {
			"test/And.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class And {\n" +
			"	public static void createAnd() {\n" +
			"		Or.create();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/Or.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Or<D, V> {\n" +
			"	public static <V> Or<V> create() {\n" +
			"		return new Or<V, V>();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"----------\n" +
		"1. ERROR in test\\Or.java (at line 7)\n" +
		"	public static <V> Or<V> create() {\n" +
		"	                  ^^\n" +
		"Incorrect number of arguments for type Or<D,V>; it cannot be parameterized with arguments <V>\n" +
		"----------\n",
		this.LIBS,
		true/*flush*/
	);
}
public void testBug497698raw() {
	runNegativeTest(
		new String[] {
			"test/And.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class And {\n" +
			"	public static void createAnd() {\n" +
			"		new Or().create();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/Or.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Or<D, V> {\n" +
			"	public <V1> Or<V1> create() {\n" +
			"		return new Or<V1, V1>();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"----------\n" +
		"1. WARNING in test\\And.java (at line 8)\n" +
		"	new Or().create();\n" +
		"	    ^^\n" +
		"Or is a raw type. References to generic type Or<D,V> should be parameterized\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in test\\Or.java (at line 7)\n" +
		"	public <V1> Or<V1> create() {\n" +
		"	            ^^\n" +
		"Incorrect number of arguments for type Or<D,V>; it cannot be parameterized with arguments <V1>\n" +
		"----------\n",
		this.LIBS,
		false/*shouldFlush*/
	);
}
public void testBug497698nestedinraw() {
	runNegativeTest(
		new String[] {
			"test/And.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class And {\n" +
			"	public static void createAnd(X.Or x) {\n" +
			"		x.create();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class X<Z> {\n" +
			"	public class Or<D, V> {\n" +
			"		public <V1> Or<V1> create() {\n" +
			"			return new Or<V1,V1>();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"----------\n" +
		"1. WARNING in test\\And.java (at line 7)\n" +
		"	public static void createAnd(X.Or x) {\n" +
		"	                             ^^^^\n" +
		"X.Or is a raw type. References to generic type X<Z>.Or<D,V> should be parameterized\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in test\\X.java (at line 8)\n" +
		"	public <V1> Or<V1> create() {\n" +
		"	            ^^\n" +
		"Incorrect number of arguments for type X<Z>.Or<D,V>; it cannot be parameterized with arguments <V1>\n" +
		"----------\n",
		this.LIBS,
		true/*flush*/
	);
}
public void testBug492322() {
	runConformTestWithLibs(
		new String[] {
			"test1/Base.java",
			"package test1;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public abstract class Base {\n" +
			"  public class GenericInner<T> {\n" +
			"  }\n" +
			"\n" +
			"  @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"  public Object method(@Nullable GenericInner<Object> nullable) {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test2/Derived.java",
			"package test2;\n" +
			"\n" +
			"import test1.Base;\n" +
			"\n" +
			"class Derived extends Base {\n" +
			"  void test() {\n" +
			"    method(null);\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug492322field() {
	runConformTestWithLibs(
		new String[] {
			"test1/Base.java",
			"package test1;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public abstract class Base {\n" +
			"  public class GenericInner<T> {\n" +
			"  }\n" +
			"\n" +
			"  protected @Nullable GenericInner<Object> field;\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"test2/Derived.java",
				"package test2;\n" +
				"\n" +
				"import test1.Base;\n" +
				"\n" +
				"class Derived extends Base {\n" +
				"  void test() {\n" +
				"    field = null;\n" +
				"  }\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
}
public void testBug492322deep() {
	runConformTestWithLibs(
		new String[] {
			"test1/Base.java",
			"package test1;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public abstract class Base {\n" +
			"  public static class Static {\n" +
			"   public class Middle1 {\n" +
			"     public class Middle2<M> {\n" +
			"       public class Middle3 {\n" +
			"        public class GenericInner<T> {\n" +
			"        }\n" +
			"       }\n" +
			"     }\n" +
			"   }\n" +
			"  }\n" +
			"\n" +
			"  @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"  public Object method( Static.Middle1.Middle2<Object>.Middle3.@Nullable GenericInner<String> nullable) {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test2/Derived.java",
			"package test2;\n" +
			"\n" +
			"import test1.Base;\n" +
			"\n" +
			"class Derived extends Base {\n" +
			"  void test() {\n" +
			"    method(null);\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug492322withGenericBase() {
	runConformTestWithLibs(
		new String[] {
			"test1/Base.java",
			"package test1;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public abstract class Base<B> {\n" +
			"   static public class Static {\n" +
			"    public class Middle1 {\n" +
			"     public class Middle2<M> {\n" +
			"       public class Middle3 {\n" +
			"        public class GenericInner<T> {\n" +
			"        }\n" +
			"       }\n" +
			"     }\n" +
			"   }\n" +
			"  }\n" +
			"\n" +
			"  @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"  public Object method( Static.Middle1.Middle2<Object>.Middle3.@Nullable GenericInner<String> nullable) {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test2/Derived.java",
			"package test2;\n" +
			"\n" +
			"import test1.Base;\n" +
			"\n" +
			"class Derived extends Base<Number> {\n" +
			"  void test() {\n" +
			"    method(null);\n" +
			"  }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug499862a() {
	runConformTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	static void printChecked(Collection<? extends @Nullable String> collection) {\n" +
			"		for(String s : collection)\n" +
			"			if (s != null)\n" +
			"				System.out.println(s.toString());\n" +
			"			else\n" +
			"				System.out.println(\"NULL\");\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
public void testBug499862b() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	static void printChecked(Collection<? extends @Nullable String> collection) {\n" +
			"		for(String s : collection)\n" +
			"			System.out.println(s.toString());\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Test.java (at line 6)\n" +
		"	System.out.println(s.toString());\n" +
		"	                   ^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n");
}
public void testBug499862c() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	static <T> void printUnchecked(Collection<T> collection) {\n" +
			"		for(T t : collection)\n" +
			"			System.out.println(t.toString());\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Test.java (at line 5)\n" +
		"	System.out.println(t.toString());\n" +
		"	                   ^\n" +
		"Potential null pointer access: this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n");
}
public void testBug499597simplified() {
	runConformTestWithLibs(
		new String[] {
			"Foo2.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class Foo2 {\n" +
			"	static <T> T of(T t) {\n" +
			"		return t;\n" +
			"	}\n" +
			"\n" +
			"	static String foo() {\n" +
			"		return Foo2.<String>of(\"\"); // <-- warning here\n" +
			"	}\n" +
			"\n" +
			"	static String bar() {\n" +
			"		return Foo2.<@NonNull String>of(\"\"); // <-- no warning\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Foo2.java (at line 15)\n" +
		"	return Foo2.<@NonNull String>of(\"\"); // <-- no warning\n" +
		"	             ^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n",
		""
	);
}
public void testBug499597original() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Foo.java",
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"import java.util.Collection;\n" +
			"import java.util.Collections;\n" +
			"\n" +
			"class Foo {\n" +
			"	static @NonNull String @NonNull [] X = { \"A\" };\n" +
			"\n" +
			"	@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS })\n" +
			"	@SafeVarargs\n" +
			"	static <T> Collection<T> of(@NonNull T @NonNull... elements) {\n" +
			"		return Collections.singleton(elements[0]);\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS })\n" +
			"	static Collection<String[]> foo() {\n" +
			"		return Foo.<String[]>of(X); // <-- warning here\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS })\n" +
			"	static Collection<String[]> bar() {\n" +
			"		return Foo.<String @NonNull []>of(X); // <-- no warning\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Foo.java (at line 12)\n" +
		"	static <T> Collection<T> of(@NonNull T @NonNull... elements) {\n" +
		"	                            ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n" +
		"2. WARNING in Foo.java (at line 12)\n" +
		"	static <T> Collection<T> of(@NonNull T @NonNull... elements) {\n" +
		"	                                       ^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n" +
		"3. WARNING in Foo.java (at line 13)\n" +
		"	return Collections.singleton(elements[0]);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'Set<@NonNull T>\' needs unchecked conversion to conform to \'@NonNull Collection<@NonNull T>\', corresponding supertype is \'Collection<@NonNull T>\'\n" +
		"----------\n" +
		"4. WARNING in Foo.java (at line 23)\n" +
		"	return Foo.<String @NonNull []>of(X); // <-- no warning\n" +
		"	                   ^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n"
	);
}
public void testBug501031() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
			"class X {\n" +
			"	<T> @NonNull Object identity(T t) {\n" +
			"		return t;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}

public void testBug501031return() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
			"class X {\n" +
			"	<T> T identity() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	return null;\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull T\' but the provided value is null\n" +
		"----------\n"
	);
}
public void testBug501031btb() {
	// this already worked without the patch for bug 501031.
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"\n" +
				"@NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
				"class X {\n" +
				"	<T> void identity(T t) {\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
	runNegativeTestWithLibs(
			new String[] {
				"Y.java",
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"class Y {\n" +
				"	void test(X x, @Nullable String string) {\n" +
				"		x.identity(string);\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			"----------\n" +
			"1. ERROR in Y.java (at line 7)\n" +
			"	x.identity(string);\n" +
			"	           ^^^^^^\n" +
			"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
			"----------\n"
		);
}
public void testBug501449() {
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class Test {\n" +
			"	<T, S extends T> void f(T[] objects, @Nullable T nullableValue, T value, S subclassValue) {\n" +
			"		objects[0] = null;\n" +
			"		objects[1] = nullableValue;\n" +
			"		objects[2] = value;\n" +
			"		objects[3] = subclassValue;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Test.java (at line 5)\n" +
		"	objects[0] = null;\n" +
		"	             ^^^^\n" +
		"Null type mismatch (type annotations): \'null\' is not compatible to the free type variable \'T\'\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 6)\n" +
		"	objects[1] = nullableValue;\n" +
		"	             ^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'T\' but this expression has type \'@Nullable T\', where \'T\' is a free type variable\n" +
		"----------\n"
	);
}
public void testBug502112() {
	runConformTest(
		new String[] {
			"org/foo/Nullable.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"public @interface Nullable {}\n",
		},
		getCompilerOptions());
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"util/Optional.java",
				"package util;\n" +
				"\n" +
				"import org.foo.Nullable;\n" +
				"\n" +
				"public class Optional {\n" +
				"	public static <T> T fromNullable(@Nullable T nullableReference, @Nullable T nullableReference2) {\n" +
				"		return nullableReference;\n" +
				"	}\n" +
				"	@Nullable\n" +
				"	public static <T> T returnNull(T nullableReference) {\n" +
				"		return nullableReference;\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
	Map options = new HashMap<>(getCompilerOptions());
	options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_SECONDARY_NAMES, "org.foo.NonNull");
	options.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_SECONDARY_NAMES, "org.foo.Nullable");
	runNegativeTestWithLibs(
	new String[] {
		"test/Test.java",
		"package test;\n" +
		"\n" +
		"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
		"import org.eclipse.jdt.annotation.Nullable;\n" +
		"\n" +
		"import util.Optional;\n" +
		"\n" +
		"@NonNullByDefault\n" +
		"public class Test {\n" +
		"	void f(@Nullable String s) {\n" +
		"		Optional.<String>fromNullable(s, null);\n" +
		"	}\n" +
		"	String g(@Nullable String s) {\n" +
		"		return Optional.<String>returnNull(s);\n" +
		"	}\n" +
		"}\n" +
		"",
	},
	options,
	"----------\n" +
	"1. ERROR in test\\Test.java (at line 14)\n" +
	"	return Optional.<String>returnNull(s);\n" +
	"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
	"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
	"----------\n" +
	"2. ERROR in test\\Test.java (at line 14)\n" +
	"	return Optional.<String>returnNull(s);\n" +
	"	                                   ^\n" +
	"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
	"----------\n"
);
}
public void testBug502112b() {
	runConformTest(
		new String[] {
		"org/foo/NonNull.java",
		"package org.foo;\n" +
		"import java.lang.annotation.*;\n" +
		"@Retention(RetentionPolicy.CLASS)\n" +
		"public @interface NonNull {}\n",
		"org/foo/Nullable.java",
		"package org.foo;\n" +
		"import java.lang.annotation.*;\n" +
		"@Retention(RetentionPolicy.CLASS)\n" +
		"public @interface Nullable {}\n",
		},
		getCompilerOptions());
	runConformTestWithLibs(
			false /* don't flush */,
			new String[] {
				"util/X.java",
				"package util;\n" +
				"\n" +
				"import org.foo.NonNull;\n" +
				"import org.foo.Nullable;\n" +
				"\n" +
				"public class X {\n" +
				"	@NonNull\n" +
				"	public <T> T nonNull(@Nullable T t, @Nullable T t2) {\n" +
				"		return java.util.Objects.requireNonNull(t);\n" +
				"	}\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
	Map options = new HashMap<>(getCompilerOptions());
	options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_SECONDARY_NAMES, "org.foo.NonNull");
	options.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_SECONDARY_NAMES, "org.foo.Nullable");
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"import util.X;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test extends X{\n" +
			"	@Override\n" +
			"	public <T> @Nullable T nonNull(@NonNull T t, T t2) {\n" +
			"		return t;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		options,
		"----------\n" +
		"1. ERROR in test\\Test.java (at line 12)\n" +
		"	public <T> @Nullable T nonNull(@NonNull T t, T t2) {\n" +
		"	           ^^^^^^^^^^^\n" +
		"The return type is incompatible with \'@NonNull T extends Object\' returned from X.nonNull(T, T) (mismatching null constraints)\n" +
		"----------\n" +
		"2. ERROR in test\\Test.java (at line 12)\n" +
		"	public <T> @Nullable T nonNull(@NonNull T t, T t2) {\n" +
		"	                               ^^^^^^^^^^\n" +
		"Illegal redefinition of parameter t, inherited method from X declares this parameter as @Nullable\n" +
		"----------\n" +
		"3. ERROR in test\\Test.java (at line 12)\n" +
		"	public <T> @Nullable T nonNull(@NonNull T t, T t2) {\n" +
		"	                                             ^\n" +
		"Missing nullable annotation: inherited method from X specifies this parameter as @Nullable\n" +
		"----------\n"
	);
}
public void testBug484926locals() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"test/NNBDOnLocalOrField.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"class AtomicReference<T> {\n" +
			"\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class NNBDOnLocalOrField {\n" +
			"	void someMethod() {\n" +
			"		AtomicReference<String> x1 = new AtomicReference<>();\n" +
			"		AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> y1 = new AtomicReference<>();\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> y2 = new AtomicReference<@NonNull String>(), y3=new AtomicReference<@Nullable String>();\n" +
			"		x1.set(null);\n" +
			"		x2.set(null);\n" +
			"		x3.set(null);\n" +
			"		y1.set(null);\n" +
			"		y2.set(null);\n" +
			"		y3.set(null);\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. WARNING in test\\NNBDOnLocalOrField.java (at line 16)\n" +
		"	AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();\n" +
		"	                                                 ^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n" +
		"2. ERROR in test\\NNBDOnLocalOrField.java (at line 16)\n" +
		"	AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();\n" +
		"	                                                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'AtomicReference<@NonNull String>\' but this expression has type \'@NonNull AtomicReference<@Nullable String>\'\n" +
		"----------\n" +
		"3. ERROR in test\\NNBDOnLocalOrField.java (at line 21)\n" +
		"	x1.set(null);\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"4. ERROR in test\\NNBDOnLocalOrField.java (at line 22)\n" +
		"	x2.set(null);\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"5. ERROR in test\\NNBDOnLocalOrField.java (at line 23)\n" +
		"	x3.set(null);\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n"
	);
}
public void testBug484926fields() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"test/NNBDOnLocalOrField.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"class AtomicReference<T> {\n" +
			"\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class NNBDOnLocalOrField {\n" +
			"	AtomicReference<String> x1 = new AtomicReference<>();\n" +
			"	AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();\n" +
			"	@NonNullByDefault({})\n" +
			"	AtomicReference<String> y1 = new AtomicReference<>();\n" +
			"	@NonNullByDefault({})\n" +
			"	AtomicReference<String> y2 = new AtomicReference<@NonNull String>(), y3=new AtomicReference<@Nullable String>();\n" +
			"	void someMethod() {\n" +
			"		x1.set(null);\n" +
			"		x2.set(null);\n" +
			"		x3.set(null);\n" +
			"		y1.set(null);\n" +
			"		y2.set(null);\n" +
			"		y3.set(null);\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. WARNING in test\\NNBDOnLocalOrField.java (at line 15)\n" +
		"	AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();\n" +
		"	                                                 ^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n" +
		"2. ERROR in test\\NNBDOnLocalOrField.java (at line 15)\n" +
		"	AtomicReference<String> x2 = new AtomicReference<@NonNull String>(), x3=new AtomicReference<@Nullable String>();\n" +
		"	                                                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull AtomicReference<@NonNull String>\' but this expression has type \'@NonNull AtomicReference<@Nullable String>\'\n" +
		"----------\n" +
		"3. ERROR in test\\NNBDOnLocalOrField.java (at line 21)\n" +
		"	x1.set(null);\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"4. ERROR in test\\NNBDOnLocalOrField.java (at line 22)\n" +
		"	x2.set(null);\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"5. ERROR in test\\NNBDOnLocalOrField.java (at line 23)\n" +
		"	x3.set(null);\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n"
	);
}
public void testBug484926() {
	runConformTestWithLibs(
		new String[] {
			"test/NNBDOnLocalOrField.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"class AtomicReference<T> {\n" +
			"\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class NNBDOnLocalOrField {\n" +
			"	@NonNullByDefault({})\n" +
			"	AtomicReference<String> f = new AtomicReference<>();\n" +
			"\n" +
			"	{\n" +
			"		f.set(null);\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault({})\n" +
			"	Runnable r = () -> {\n" +
			"		AtomicReference<String> x1 = new AtomicReference<>();\n" +
			"		x1.set(null);\n" +
			"	};\n" +
			"\n" +
			"	Object someMethod() {\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> x2 = new AtomicReference<>();\n" +
			"		x2.set(null);\n" +
			"\n" +
			"		@NonNullByDefault({})\n" +
			"		Runnable r1 = () -> {\n" +
			"			AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"			x3.set(null);\n" +
			"		};\n" +
			"		\n" +
			"		return r1;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug484926nesting() {
	runNegativeTestWithLibs(
		new String[] {
			"test/NNBDOnLocalOrField.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"class AtomicReference<T> {\n" +
			"\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"\n" +
			"public class NNBDOnLocalOrField {\n" +
			"	@NonNullByDefault()\n" +
			"	Runnable r = () -> {\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> x1 = new AtomicReference<>();\n" +
			"		x1.set(null);\n" +
			"	};\n" +
			"	@NonNullByDefault\n" +
			"	Object someMethod() {\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> x2 = new AtomicReference<>();\n" +
			"		x2.set(null);\n" +
			"\n" +
			"		@NonNullByDefault({})\n" +
			"		Runnable r1 = () -> {\n" +
			"			@NonNullByDefault\n" +
			"			AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"			x3.set(null);\n" +
			"		};\n" +
			"		\n" +
			"		return r1;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\NNBDOnLocalOrField.java (at line 29)\n" +
		"	x3.set(null);\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n"
	);
}
public void testBug484926localDeclarationInForLoop() {
	runConformTestWithLibs(
		new String[] {
			"test/NNBDOnLocalOrField.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"class AtomicReference<T> {\n" +
			"\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class NNBDOnLocalOrField {\n" +
			"	void someMethod() {\n" +
			"		for(@NonNullByDefault({})\n" +
			"		Runnable r1 = () -> {\n" +
			"			AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"			x3.set(null);\n" +
			"		}, r2 = () -> {\n" +
			"			AtomicReference<String> x4 = new AtomicReference<>();\n" +
			"			x4.set(null);\n" +
			"		};;) {\n" +
			"			r1.run();\n" +
			"			r2.run();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug484926redundantNNBD() {
	runNegativeTestWithLibs(
		new String[] {
			"testnnbd/NNBDRedundantOnLocalOrField.java",
			"package testnnbd;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault({})\n" +
			"class AtomicReference<T> {\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class NNBDRedundantOnLocalOrField {\n" +
			"	@NonNullByDefault\n" +
			"	Runnable r1 = () -> {\n" +
			"		@NonNullByDefault\n" +
			"		AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"		x3.set(null);\n" +
			"	}, r2 = () -> {\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> x4 = new AtomicReference<String>() {\n" +
			"			@NonNullByDefault({})\n" +
			"			public void set(String object) {\n" +
			"			}\n" +
			"		};\n" +
			"		x4.set(null);\n" +
			"	};\n" +
			"\n" +
			"	@NonNullByDefault\n" +
			"	class X1 {\n" +
			"		@NonNullByDefault\n" +
			"		Runnable r = () -> {\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			class Local extends AtomicReference<String> {\n" +
			"				@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"				class X2 {\n" +
			"					@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"					String s;\n" +
			"					\n" +
			"					{\n" +
			"						set(null);\n" +
			"					}\n" +
			"				}\n" +
			"				{\n" +
			"				new X2().hashCode();\n" +
			"				}\n" +
			"			}\n" +
			"			Local x1 = new Local();\n" +
			"			x1.set(null);\n" +
			"		};\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"	Object someMethod() {\n" +
			"		@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"		AtomicReference<String> x2 = new AtomicReference<>();\n" +
			"		x2.set(null);\n" +
			"\n" +
			"		@NonNullByDefault({})\n" +
			"		Runnable r = () -> {\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"			x3.set(null);\n" +
			"		};\n" +
			"\n" +
			"		@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"		Runnable r2 = new Runnable() {\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			@Override\n" +
			"			public void run() {\n" +
			"			}\n" +
			"		};\n" +
			"\n" +
			"		r2.run();\n" +
			"		return r;\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"	void forLoopVariable() {\n" +
			"		{\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			Runnable r = () -> {\n" +
			"				AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"				x3.set(null);\n" +
			"			}, r2 = () -> {\n" +
			"				AtomicReference<String> x4 = new AtomicReference<>();\n" +
			"				x4.set(null);\n" +
			"			};\n" +
			"			r.run();\n" +
			"			r2.run();\n" +
			"		}\n" +
			"		for (@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"		Runnable r = () -> {\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			AtomicReference<String> x3 = new AtomicReference<>();\n" +
			"			x3.set(null);\n" +
			"		}, r2 = () -> {\n" +
			"			@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"			AtomicReference<String> x4 = new AtomicReference<>();\n" +
			"			x4.set(null);\n" +
			"		};;) {\n" +
			"			r.run();\n" +
			"			r2.run();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
			"testnnbd/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package testnnbd;\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 13)\n" +
		"	@NonNullByDefault\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing package testnnbd\n" +
		"----------\n" +
		"2. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 15)\n" +
		"	@NonNullByDefault\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the field r1\n" +
		"----------\n" +
		"3. ERROR in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 17)\n" +
		"	x3.set(null);\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"4. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 21)\n" +
		"	@NonNullByDefault({})\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the variable x4\n" +
		"----------\n" +
		"5. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 22)\n" +
		"	public void set(String object) {\n" +
		"	            ^^^^^^^^^^^^^^^^^^\n" +
		"The method set(String) of type new AtomicReference<String>(){} should be tagged with @Override since it actually overrides a superclass method\n" +
		"----------\n" +
		"6. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 28)\n" +
		"	@NonNullByDefault\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing package testnnbd\n" +
		"----------\n" +
		"7. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 30)\n" +
		"	@NonNullByDefault\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type NNBDRedundantOnLocalOrField.X1\n" +
		"----------\n" +
		"8. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 34)\n" +
		"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type Local\n" +
		"----------\n" +
		"9. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 36)\n" +
		"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type Local.X2\n" +
		"----------\n" +
		"10. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 37)\n" +
		"	String s;\n" +
		"	       ^\n" +
		"The value of the field Local.X2.s is not used\n" +
		"----------\n" +
		"11. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 54)\n" +
		"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing method someMethod()\n" +
		"----------\n" +
		"12. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 65)\n" +
		"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing method someMethod()\n" +
		"----------\n" +
		"13. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 66)\n" +
		"	Runnable r2 = new Runnable() {\n" +
		"	         ^^\n" +
		"The local variable r2 is hiding a field from type NNBDRedundantOnLocalOrField\n" +
		"----------\n" +
		"14. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 67)\n" +
		"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the variable r2\n" +
		"----------\n" +
		"15. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 80)\n" +
		"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing method forLoopVariable()\n" +
		"----------\n" +
		"16. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 84)\n" +
		"	}, r2 = () -> {\n" +
		"	   ^^\n" +
		"The local variable r2 is hiding a field from type NNBDRedundantOnLocalOrField\n" +
		"----------\n" +
		"17. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 91)\n" +
		"	for (@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
		"	     ^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing method forLoopVariable()\n" +
		"----------\n" +
		"18. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 91)\n" +
		"	for (@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
		"	     ^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing method forLoopVariable()\n" +
		"----------\n" +
		"19. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 93)\n" +
		"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the variable r\n" +
		"----------\n" +
		"20. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 96)\n" +
		"	}, r2 = () -> {\n" +
		"	   ^^\n" +
		"The local variable r2 is hiding a field from type NNBDRedundantOnLocalOrField\n" +
		"----------\n" +
		"21. WARNING in testnnbd\\NNBDRedundantOnLocalOrField.java (at line 97)\n" +
		"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the variable r2\n" +
		"----------\n"
	);
}
public void testBug484926BTB() {
	runConformTestWithLibs(
		new String[] {
			"test/ClassWithNNBDOnField.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"class AtomicReference<T> {\n" +
			"	public void set(T object) {\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault()\n" +
			"public class ClassWithNNBDOnField {\n" +
			"	@NonNullByDefault({})\n" +
			"	AtomicReference<String> f = new AtomicReference<>();\n" +
			"	{\n" +
			"		f.set(null);\n" +
			"	}\n" +
			"\n" +
			"	public static class X {\n" +
			"		@NonNullByDefault({})\n" +
			"		AtomicReference<String> nested = new AtomicReference<>();\n" +
			"		{\n" +
			"			nested.set(null);\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public X x = new X();\n" +
			"	\n" +
			"	void test() {\n" +
			"		new ClassWithNNBDOnField().f.set(null);\n" +
			"		new ClassWithNNBDOnField().f = null;\n" +
			"		new ClassWithNNBDOnField().x.nested.set(null);\n" +
			"		new ClassWithNNBDOnField().x.nested = null;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"public class Test {\n" +
			"	void test() {\n" +
			"		new ClassWithNNBDOnField().f.set(null);\n" +
			"		new ClassWithNNBDOnField().f = null;\n" +
			"		new ClassWithNNBDOnField().x.nested.set(null);\n" +
			"		new ClassWithNNBDOnField().x.nested = null;\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug500885() {
	runConformTest(
		new String[] {
			"annot/NonNull.java",
			"package annot;\n" +
			"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n" +
			"public @interface NonNull {}\n",
			"annot/NonNullByDefault.java",
			"package annot;\n" +
			"@annot.NonNull\n" +
			"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n" +
			"public @interface NonNullByDefault {}\n",
			"annot/package-info.java",
			"@annot.NonNullByDefault package annot;\n",
			"test/package-info.java",
			"@annot.NonNullByDefault package test;\n",
			"test/X.java",
			"package test;\n" +
			"public interface X {\n" +
			"	public String get();\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annot.NonNullByDefault");
	options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_SECONDARY_NAMES, "annot.NonNull");
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test2/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault package test2;\n",
			"test2/Y.java",
			"package test2;\n" +
			"import test.X;\n" +
			"public class Y implements X {\n" +
			"	public String get() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}\n"
		},
		options,
		"");
}
public void testBug505671() {
	runConformTestWithLibs(
		new String[] {
			"snippet/Pair.java",
			"package snippet;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Pair {\n" +
			"	public static <S, T> S make(S left, T right, Object x) {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
	getCompilerOptions(),
	""
	);
	runNegativeTestWithLibs(
		new String[] {
			"snippet/Snippet.java",
			"package snippet;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class Snippet {\n" +
			"	public static final @NonNull Object FALSE = new Object();\n" +
			"\n" +
			"	public static @NonNull Object abbreviateExplained0() {\n" +
			"		return Pair.<String, @NonNull Object>make(null, FALSE, null);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in snippet\\Snippet.java (at line 9)\n" +
		"	return Pair.<String, @NonNull Object>make(null, FALSE, null);\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n" +
		"2. ERROR in snippet\\Snippet.java (at line 9)\n" +
		"	return Pair.<String, @NonNull Object>make(null, FALSE, null);\n" +
		"	                                                       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n"
	);
}
public void testBug501564() {
	runNegativeTestWithLibs(
		new String[] {
			"xxx/Foo.java",
			"package xxx;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"\n" +
			"class Generic<E1 extends Generic<E1>> { \n" +
			"}\n" +
			"class Foo { \n" +
			"    static <E2 extends Generic<E2>> Bar<E2> foo() {\n" +
			"        return new Bar<>();\n" +
			"    }\n" +
			"\n" +
			"    @NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
			"    static class Bar<E3 extends Generic<E3>> { }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in xxx\\Foo.java (at line 8)\n" +
		"	static <E2 extends Generic<E2>> Bar<E2> foo() {\n" +
		"	                                    ^^\n" +
		"Null constraint mismatch: The type \'E2 extends Generic<E2>\' is not a valid substitute for the type parameter \'@NonNull E3 extends Generic<E3 extends Generic<E3>>\'\n" +
		"----------\n"
	);
}
public void testBug501564interface() {
	runNegativeTestWithLibs(
		new String[] {
			"xxx/Foo.java",
			"package xxx;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"\n" +
			"interface Generic<E1 extends Generic<E1>> { \n" +
			"}\n" +
			"class Foo { \n" +
			"    static <E2 extends Generic<E2>> Bar<E2> foo() {\n" +
			"        return new Bar<>();\n" +
			"    }\n" +
			"\n" +
			"    @NonNullByDefault(DefaultLocation.TYPE_PARAMETER)\n" +
			"    static class Bar<E3 extends Generic<E3>> { }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in xxx\\Foo.java (at line 8)\n" +
		"	static <E2 extends Generic<E2>> Bar<E2> foo() {\n" +
		"	                                    ^^\n" +
		"Null constraint mismatch: The type \'E2 extends Generic<E2>\' is not a valid substitute for the type parameter \'@NonNull E3 extends Generic<E3 extends Generic<E3>>\'\n" +
		"----------\n"
	);
}
public void testBug501464() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Foo.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"interface MyList<T> { @NonNull T getAny(); }\n" +
			"\n" +
			"@NonNullByDefault({})\n" +
			"class Foo {\n" +
			"    @Nullable Object b;\n" +
			"    \n" +
			"    void foo() {\n" +
			"        @Nullable Object f = b;\n" +
			"        ((@NonNull Object)f).hashCode(); // Error (unexpected): Potential null pointer access: this expression has a '@Nullable' type\n" +
			"    }\n" +
			"    \n" +
			"    void workaround() {\n" +
			"        @Nullable Object f = b;\n" +
			"        @NonNull Object g = (@NonNull Object)f; // Warning (expected): Null type safety: Unchecked cast from @Nullable Object to @NonNull Object\n" +
			"        g.hashCode();\n" +
			"    }\n" +
			"	 String three(@NonNull MyList<@Nullable String> list) {\n" +
			"		return ((@NonNull MyList<@NonNull String>) list).getAny().toUpperCase();\n" +
			"	 }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Foo.java (at line 11)\n" +
		"	((@NonNull Object)f).hashCode(); // Error (unexpected): Potential null pointer access: this expression has a \'@Nullable\' type\n" +
		"	^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety: Unchecked cast from @Nullable Object to @NonNull Object\n" +
		"----------\n" +
		"2. WARNING in Foo.java (at line 16)\n" +
		"	@NonNull Object g = (@NonNull Object)f; // Warning (expected): Null type safety: Unchecked cast from @Nullable Object to @NonNull Object\n" +
		"	                    ^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety: Unchecked cast from @Nullable Object to @NonNull Object\n" +
		"----------\n" +
		"3. WARNING in Foo.java (at line 20)\n" +
		"	return ((@NonNull MyList<@NonNull String>) list).getAny().toUpperCase();\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety: Unchecked cast from @NonNull MyList<@Nullable String> to @NonNull MyList<@NonNull String>\n" +
		"----------\n"
	);
}
public void testBug507840() {
	runConformTestWithLibs(
		new String[] {
			"nnbd_on_typevar/AtomicReference.java",
			"package nnbd_on_typevar;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class AtomicReference<T> {\n" +
			"	public void set(T t) {\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"nnbd_on_typevar/Usage.java",
			"package nnbd_on_typevar;\n" +
			"\n" +
			"public class Usage {\n" +
			"	void m(AtomicReference<String> ref) {\n" +
			"		ref.set(null);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug508497() {
	runConformTestWithLibs(
		new String[] {
			"Reference.java",
			"interface Fluent<SELF extends Fluent<SELF>> {\n" +
			"	SELF self();\n" +
			"}\n" +
			"abstract class Reference<T> {\n" +
			"	abstract T get();\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"B2.java",
			"class B2 {\n" +
			"	void b1(Fluent f) {\n" +
			"		f.self();\n" +
			"	}\n" +
			"\n" +
			"	void b2(Reference<@org.eclipse.jdt.annotation.NonNull Fluent> ref) {\n" +
			"		ref.get().self();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in B2.java (at line 2)\n" +
		"	void b1(Fluent f) {\n" +
		"	        ^^^^^^\n" +
		"Fluent is a raw type. References to generic type Fluent<SELF> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in B2.java (at line 6)\n" +
		"	void b2(Reference<@org.eclipse.jdt.annotation.NonNull Fluent> ref) {\n" +
		"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Fluent is a raw type. References to generic type Fluent<SELF> should be parameterized\n" +
		"----------\n" +
		"3. INFO in B2.java (at line 7)\n" +
		"	ref.get().self();\n" +
		"	^^^^^^^^^\n" +
		"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'Reference<@NonNull Fluent>\'. Type \'Reference<T>\' doesn\'t seem to be designed with null type annotations in mind\n" +
		"----------\n"
	);
}
public void testBug509025_a() {
	runConformTestWithLibs(
		new String[] {
			"MyAnno.java",
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@NonNullByDefault(DefaultLocation.ARRAY_CONTENTS)\n" +
			"public @interface MyAnno {\n" +
			"	@NonNull String[] items();\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in MyAnno.java (at line 10)\n" +
		"	@NonNull String[] items();\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n",
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"AnnoLoop.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"@NonNullByDefault(DefaultLocation.ARRAY_CONTENTS)\n" +
			"public class AnnoLoop {\n" +
			"	@NonNull String[] test(MyAnno anno) {\n" +
			"		return anno.items();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in AnnoLoop.java (at line 6)\n" +
		"	@NonNull String[] test(MyAnno anno) {\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n",
		""
	);
}
public void testBug509025_b() {
	runConformTestWithLibs(
		new String[] {
			"MyAnno.java",
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@NonNullByDefault\n" +
			"public @interface MyAnno {\n" +
			"	String @NonNull[] items();\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in MyAnno.java (at line 10)\n" +
		"	String @NonNull[] items();\n" +
		"	       ^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n",
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"AnnoLoop.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class AnnoLoop {\n" +
			"	String @NonNull[] test(MyAnno anno) {\n" +
			"		return anno.items();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in AnnoLoop.java (at line 6)\n" +
		"	String @NonNull[] test(MyAnno anno) {\n" +
		"	       ^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n",
		""
	);
}
public void testBug509025_c() {
	runConformTestWithLibs(
		new String[] {
			"MyAnno.java",
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@NonNullByDefault\n" +
			"public @interface MyAnno {\n" +
			"	@NonNull String[] items();\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"",
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"AnnoLoop.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class AnnoLoop {\n" +
			"	@NonNull String[] test(MyAnno anno) {\n" +
			"		return anno.items();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"",
		""
	);
}
public void testBug501598() {
	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"Foo.java",
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class Foo {\n" +
			"	static <T> @NonNull List<?> f() {\n" +
			"		throw new Error();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Foo.java (at line 8)\n" +
		"	static <T> @NonNull List<?> f() {\n" +
		"	           ^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n"
	);
}
public void testBug509328() {
	runConformTestWithLibs(
		new String[] {
			"test/Feature.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Feature {\n" +
			"	public Feature(String name) {\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"	public static void f() {\n" +
			"		new Feature(null) {\n" +
			"			// anonymous subclass\n" +
			"		};\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\Test.java (at line 8)\n" +
		"	new Feature(null) {\n" +
		"	            ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n"
	);
}
public void testBug510799() {
	runConformTestWithLibs(
		new String[] {
			"test/TestNNBDBreaksDimensionAnnotation.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"class TestNNBDBreaksDimensionAnnotation {\n" +
			"	Object f(String[] @NonNull [] a) {\n" +
			"		return a[0];\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug490403() {
	runConformTestWithLibs(
		new String[] {
			"test/TestNullInt.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class TestNullInt {\n" +
			"\n" +
			"	public void test() {\n" +
			"		@NonNull Integer[] keys = new @NonNull Integer[12];\n" +
			"		@NonNull Integer index = 0;\n" +
			"		for (int i = 0; i < 10; i++) {\n" +
			"			keys[index] = index;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug490403while() {
	runConformTestWithLibs(
		new String[] {
			"test/TestNullInt.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public abstract class TestNullInt {\n" +
			"	public abstract boolean b();\n" +
			"\n" +
			"	public void test(@NonNull Object[] keys, @NonNull String o) {\n" +
			"		while (b()) {\n" +
			"			keys[0] = o;\n" +
			"			keys[1] = b() ? o : o;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}

public void testBug490403negative() {
	runNegativeTestWithLibs(
		new String[] {
			"test/TestNullInt.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public abstract class TestNullInt {\n" +
			"	public abstract boolean b();\n" +
			"\n" +
			"	public void warning(@NonNull Object[] keys, String o) {\n" +
			"		while (b()) {\n" +
			"			keys[0] = o;\n" +
			"			keys[1] = b() ? o : \"\";\n" +
			"		}\n" +
			"	}\n" +
			"	public void error(@NonNull Object[] keys, @Nullable String o) {\n" +
			"		while (b()) {\n" +
			"			keys[0] = o;\n" +
			"			keys[1] = b() ? \"\" : o;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in test\\TestNullInt.java (at line 9)\n" +
		"	keys[0] = o;\n" +
		"	          ^\n" +
		"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n" +
		"2. WARNING in test\\TestNullInt.java (at line 10)\n" +
		"	keys[1] = b() ? o : \"\";\n" +
		"	                ^\n" +
		"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n" +
		"3. ERROR in test\\TestNullInt.java (at line 15)\n" +
		"	keys[0] = o;\n" +
		"	          ^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable String\'\n" +
		"----------\n" +
		"4. ERROR in test\\TestNullInt.java (at line 16)\n" +
		"	keys[1] = b() ? \"\" : o;\n" +
		"	                     ^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable String\'\n" +
		"----------\n"
	);
}
public void testBug490403typeArgAnnotationMismatch() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class Ref<T> {\n" +
			"}\n" +
			"\n" +
			"public abstract class Test {\n" +
			"    abstract boolean b();\n" +
			"\n" +
			"    public void testAnnotationMismatch(@NonNull Ref<@Nullable String> x, @NonNull Ref<@NonNull String>[] keys) {\n" +
			"        keys[0] = x;\n" +
			"        while (b()) {\n" +
			"            keys[0] = x;\n" +
			"            keys[1] = b() ? keys[0] : x;\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\Test.java (at line 13)\n" +
		"	keys[0] = x;\n" +
		"	          ^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String>\' but this expression has type \'@NonNull Ref<@Nullable String>\'\n" +
		"----------\n" +
		"2. ERROR in test\\Test.java (at line 15)\n" +
		"	keys[0] = x;\n" +
		"	          ^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String>\' but this expression has type \'@NonNull Ref<@Nullable String>\'\n" +
		"----------\n" +
		"3. ERROR in test\\Test.java (at line 16)\n" +
		"	keys[1] = b() ? keys[0] : x;\n" +
		"	                          ^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String>\' but this expression has type \'@NonNull Ref<@Nullable String>\'\n" +
		"----------\n"
	);
}
public void testBug499589() {
	runConformTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void a(String[] array) {\n" +
			"		x(array[0]); // <----- bogus warning\n" +
			"	}\n" +
			"\n" +
			"	static void x(String s) {\n" +
			"		System.out.println(s);\n" +
			"	}\n" +
			"\n" +
			"	static void b(String[][] array) {\n" +
			"		y(array[0]); // <----- bogus warning\n" +
			"	}\n" +
			"\n" +
			"	static void y(String[] s) {\n" +
			"		System.out.println(s[0]);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug499589multidim() {
	runNegativeTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void foo(String[] @Nullable [] array) {\n" +
			"		x(array[0]);\n" +
			"	}\n" +
			"	static void x(String[] s) {\n" +
			"		System.out.println(s[0]);\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\BogusWarning.java (at line 11)\n" +
		"	x(array[0]);\n" +
		"	  ^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[]\' but this expression has type \'@NonNull String @Nullable[]\'\n" +
		"----------\n"
	);
}

public void testBug499589leafTypeNullable() {
	runNegativeTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void foo(@Nullable String[] array) {\n" +
			"		x(array[0]);\n" +
			"	}\n" +
			"\n" +
			"	static void x(String s) {\n" +
			"		System.out.println(s);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\BogusWarning.java (at line 11)\n" +
		"	x(array[0]);\n" +
		"	  ^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
		"----------\n"
	);
}

public void testBug499589qualified() {
	runConformTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void foo(java.lang.String[] array) {\n" +
			"		x(array[0]);\n" +
			"	}\n" +
			"\n" +
			"	static void x(String s) {\n" +
			"		System.out.println(s);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}

public void testBug499589qualified_leafTypeNullable() {
	runNegativeTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void foo(java.lang.@Nullable String[] array) {\n" +
			"		x(array[0]);\n" +
			"	}\n" +
			"\n" +
			"	static void x(String s) {\n" +
			"		System.out.println(s);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\BogusWarning.java (at line 11)\n" +
		"	x(array[0]);\n" +
		"	  ^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" +
		"----------\n"
	);
}
public void testBug499589qualified_multidim() {
	runNegativeTestWithLibs(
		new String[] {
			"test/BogusWarning.java",
			"package test;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, ARRAY_CONTENTS })\n" +
			"class BogusWarning {\n" +
			"	static void foo(java.lang.String[] @Nullable [] array) {\n" +
			"		x(array[0]);\n" +
			"	}\n" +
			"	static void x(java.lang.String[] s) {\n" +
			"		System.out.println(s[0]);\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\BogusWarning.java (at line 11)\n" +
		"	x(array[0]);\n" +
		"	  ^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[]\' but this expression has type \'@NonNull String @Nullable[]\'\n" +
		"----------\n"
	);
}
public void testBug499589STB() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Ref.java",
			"package test;\n" +
			"\n" +
			"public class Ref<T> {\n" +
			"	T get() {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@SuppressWarnings({ \"unchecked\" })\n" +
			"@NonNullByDefault({ FIELD, RETURN_TYPE, PARAMETER, ARRAY_CONTENTS, TYPE_ARGUMENT })\n" +
			"public abstract class X {\n" +
			"	public final String[][] field = {};\n" +
			"	public final @Nullable String[][] fieldWithNullable1 = {};\n" +
			"	public final String[] @Nullable [] fieldWithNullable2 = {};\n" +
			"\n" +
			"	public final Ref<String[][]> list = new Ref<>();\n" +
			"	public final Ref<@Nullable String[][]> listWithNullable1 = new Ref<>();\n" +
			"	public final Ref<String[] @Nullable []> listWithNullable2 = new Ref<>();\n" +
			"\n" +
			"	public abstract String[][] method();\n" +
			"	public abstract @Nullable String[][] methodWithNullable1();\n" +
			"	public abstract String[] @Nullable [] methodWithNullable2();\n" +
			"\n" +
			"	public final Ref<String[][]>[][] genericField = new Ref[0][];\n" +
			"	public final @Nullable Ref<@Nullable String[][]>[][] genericFieldWithNullable1 = new Ref[0][];\n" +
			"	public final Ref<String[] @Nullable []>[] @Nullable [] genericFieldWithNullable2 = new Ref[0][];\n" +
			"}\n" +
			"\n" +
			"class SourceUsage {\n" +
			"	void check(@NonNull String @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void checkGeneric(@NonNull Ref<@NonNull String @NonNull [] @NonNull []> @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void f(X x) {\n" +
			"		check(x.field);\n" +
			"		check(x.fieldWithNullable1);\n" +
			"		check(x.fieldWithNullable2);\n" +
			"		check(x.list.get());\n" +
			"		check(x.listWithNullable1.get());\n" +
			"		check(x.listWithNullable2.get());\n" +
			"		check(x.method());\n" +
			"		check(x.methodWithNullable1());\n" +
			"		check(x.methodWithNullable2());\n" +
			"		checkGeneric(x.genericField);\n" +
			"		checkGeneric(x.genericFieldWithNullable1);\n" +
			"		checkGeneric(x.genericFieldWithNullable2);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\X.java (at line 36)\n" +
		"	check(x.fieldWithNullable1);\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"2. ERROR in test\\X.java (at line 37)\n" +
		"	check(x.fieldWithNullable2);\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"3. ERROR in test\\X.java (at line 39)\n" +
		"	check(x.listWithNullable1.get());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"4. ERROR in test\\X.java (at line 40)\n" +
		"	check(x.listWithNullable2.get());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"5. ERROR in test\\X.java (at line 42)\n" +
		"	check(x.methodWithNullable1());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"6. ERROR in test\\X.java (at line 43)\n" +
		"	check(x.methodWithNullable2());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"7. ERROR in test\\X.java (at line 45)\n" +
		"	checkGeneric(x.genericFieldWithNullable1);\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@Nullable Ref<@Nullable String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"8. ERROR in test\\X.java (at line 46)\n" +
		"	checkGeneric(x.genericFieldWithNullable2);\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@NonNull Ref<@NonNull String @NonNull[] @Nullable[]> @NonNull[] @Nullable[]\'\n" +
		"----------\n"
	);
}
public void testBug499589BTB() {
	Runner runner = new Runner();
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"test/Ref.java",
			"package test;\n" +
			"\n" +
			"public class Ref<T> {\n" +
			"	T get() {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.FIELD;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@SuppressWarnings({ \"unchecked\" })\n" +
			"@NonNullByDefault({ FIELD, RETURN_TYPE, PARAMETER, ARRAY_CONTENTS, TYPE_ARGUMENT })\n" +
			"public abstract class X {\n" +
			"	public final String[][] field = {};\n" +
			"	public final @Nullable String[][] fieldWithNullable1 = {};\n" +
			"	public final String[] @Nullable [] fieldWithNullable2 = {};\n" +
			"\n" +
			"	public final Ref<String[][]> list = new Ref<>();\n" +
			"	public final Ref<@Nullable String[][]> listWithNullable1 = new Ref<>();\n" +
			"	public final Ref<String[] @Nullable []> listWithNullable2 = new Ref<>();\n" +
			"\n" +
			"	public abstract String[][] method();\n" +
			"	public abstract @Nullable String[][] methodWithNullable1();\n" +
			"	public abstract String[] @Nullable [] methodWithNullable2();\n" +
			"\n" +
			"	public final Ref<String[][]>[][] genericField = new Ref[0][];\n" +
			"	public final @Nullable Ref<@Nullable String[][]>[][] genericFieldWithNullable1 = new Ref[0][];\n" +
			"	public final Ref<String[] @Nullable []>[] @Nullable [] genericFieldWithNullable2 = new Ref[0][];\n" +
			"}\n" +
			"",
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("rawtypes"); // javac detects rawtypes at new Ref[0][0]
	runner.runConformTest();
	runNegativeTestWithLibs(
		new String[] {
			"test/BinaryUsage.java",
			"package test;\n" +
			"\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"class BinaryUsage {\n" +
			"	void check(@NonNull String @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void checkGeneric(@NonNull Ref<@NonNull String @NonNull [] @NonNull []> @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void f(X x) {\n" +
			"		check(x.field);\n" +
			"		check(x.fieldWithNullable1);\n" +
			"		check(x.fieldWithNullable2);\n" +
			"		check(x.list.get());\n" +
			"		check(x.listWithNullable1.get());\n" +
			"		check(x.listWithNullable2.get());\n" +
			"		check(x.method());\n" +
			"		check(x.methodWithNullable1());\n" +
			"		check(x.methodWithNullable2());\n" +
			"		checkGeneric(x.genericField);\n" +
			"		checkGeneric(x.genericFieldWithNullable1);\n" +
			"		checkGeneric(x.genericFieldWithNullable2);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\BinaryUsage.java (at line 15)\n" +
		"	check(x.fieldWithNullable1);\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"2. ERROR in test\\BinaryUsage.java (at line 16)\n" +
		"	check(x.fieldWithNullable2);\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"3. ERROR in test\\BinaryUsage.java (at line 18)\n" +
		"	check(x.listWithNullable1.get());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"4. ERROR in test\\BinaryUsage.java (at line 19)\n" +
		"	check(x.listWithNullable2.get());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"5. ERROR in test\\BinaryUsage.java (at line 21)\n" +
		"	check(x.methodWithNullable1());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@Nullable String @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"6. ERROR in test\\BinaryUsage.java (at line 22)\n" +
		"	check(x.methodWithNullable2());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull String @NonNull[] @NonNull[]\' but this expression has type \'@NonNull String @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"7. ERROR in test\\BinaryUsage.java (at line 24)\n" +
		"	checkGeneric(x.genericFieldWithNullable1);\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@Nullable Ref<@Nullable String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"8. ERROR in test\\BinaryUsage.java (at line 25)\n" +
		"	checkGeneric(x.genericFieldWithNullable2);\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Ref<@NonNull String @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@NonNull Ref<@NonNull String @NonNull[] @Nullable[]> @NonNull[] @Nullable[]\'\n" +
		"----------\n"
	);
}

public void testBug499589STBqualified() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Ref.java",
			"package test;\n" +
			"\n" +
			"public class Ref<T> {\n" +
			"	T get() {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/A.java",
			"package test;\n" +
			"\n" +
			"public class A {\n" +
			"	class B {\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@SuppressWarnings({ \"unchecked\" })\n" +
			"@NonNullByDefault({ FIELD, RETURN_TYPE, PARAMETER, ARRAY_CONTENTS, TYPE_ARGUMENT })\n" +
			"public abstract class X {\n" +
			"	public final test.A.B[][] field = {};\n" +
			"	public final test.A.@Nullable B[][] fieldWithNullable1 = {};\n" +
			"	public final test.A.B[] @Nullable [] fieldWithNullable2 = {};\n" +
			"\n" +
			"	public final test.Ref<test.A.B[][]> list = new Ref<>();\n" +
			"	public final test.Ref<test.A.@Nullable B[][]> listWithNullable1 = new Ref<>();\n" +
			"	public final test.Ref<test.A.B[] @Nullable []> listWithNullable2 = new Ref<>();\n" +
			"\n" +
			"	public abstract test.A.B[][] method();\n" +
			"	public abstract test.A.@Nullable B[][] methodWithNullable1();\n" +
			"	public abstract test.A.B[] @Nullable [] methodWithNullable2();\n" +
			"\n" +
			"	public final test.Ref<test.A.B[][]>[][] genericField = new Ref[0][];\n" +
			"	public final test.@Nullable Ref<test.A.@Nullable B[][]>[][] genericFieldWithNullable1 = new Ref[0][];;\n" +
			"	public final test.Ref<test.A.B[] @Nullable []>[] @Nullable[] genericFieldWithNullable2 = new Ref[0][];;\n" +
			"}\n" +
			"\n" +
			"class SourceUsage {\n" +
			"	void check(test.A.@NonNull B @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"	void checkGeneric(test.@NonNull Ref<test.A.@NonNull B @NonNull [] @NonNull []> @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void f(X x) {\n" +
			"		check(x.field);\n" +
			"		check(x.fieldWithNullable1);\n" +
			"		check(x.fieldWithNullable2);\n" +
			"		check(x.list.get());\n" +
			"		check(x.listWithNullable1.get());\n" +
			"		check(x.listWithNullable2.get());\n" +
			"		check(x.method());\n" +
			"		check(x.methodWithNullable1());\n" +
			"		check(x.methodWithNullable2());\n" +
			"		checkGeneric(x.genericField);\n" +
			"		checkGeneric(x.genericFieldWithNullable1);\n" +
			"		checkGeneric(x.genericFieldWithNullable2);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\X.java (at line 35)\n" +
		"	check(x.fieldWithNullable1);\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"2. ERROR in test\\X.java (at line 36)\n" +
		"	check(x.fieldWithNullable2);\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"3. ERROR in test\\X.java (at line 38)\n" +
		"	check(x.listWithNullable1.get());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"4. ERROR in test\\X.java (at line 39)\n" +
		"	check(x.listWithNullable2.get());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"5. ERROR in test\\X.java (at line 41)\n" +
		"	check(x.methodWithNullable1());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"6. ERROR in test\\X.java (at line 42)\n" +
		"	check(x.methodWithNullable2());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"7. ERROR in test\\X.java (at line 44)\n" +
		"	checkGeneric(x.genericFieldWithNullable1);\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Ref<A.@NonNull B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@Nullable Ref<A.@Nullable B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"8. ERROR in test\\X.java (at line 45)\n" +
		"	checkGeneric(x.genericFieldWithNullable2);\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Ref<A.@NonNull B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@NonNull Ref<A.@NonNull B @NonNull[] @Nullable[]> @NonNull[] @Nullable[]\'\n" +
		"----------\n"
	);
}
public void testBug499589BTBqualified() {
	Runner runner = new Runner();
	runner.classLibraries = this.LIBS;
	runner.testFiles =
		new String[] {
			"test/Ref.java",
			"package test;\n" +
			"\n" +
			"public class Ref<T> {\n" +
			"	T get() {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/A.java",
			"package test;\n" +
			"\n" +
			"public class A {\n" +
			"	class B {\n" +
			"	}\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@SuppressWarnings({ \"unchecked\" })\n" +
			"@NonNullByDefault({ FIELD, RETURN_TYPE, PARAMETER, ARRAY_CONTENTS, TYPE_ARGUMENT })\n" +
			"public abstract class X {\n" +
			"	public final test.A.B[][] field = {};\n" +
			"	public final test.A.@Nullable B[][] fieldWithNullable1 = {};\n" +
			"	public final test.A.B[] @Nullable [] fieldWithNullable2 = {};\n" +
			"\n" +
			"	public final test.Ref<test.A.B[][]> list = new Ref<>();\n" +
			"	public final test.Ref<test.A.@Nullable B[][]> listWithNullable1 = new Ref<>();\n" +
			"	public final test.Ref<test.A.B[] @Nullable []> listWithNullable2 = new Ref<>();\n" +
			"\n" +
			"	public abstract test.A.B[][] method();\n" +
			"	public abstract test.A.@Nullable B[][] methodWithNullable1();\n" +
			"	public abstract test.A.B[] @Nullable [] methodWithNullable2();\n" +
			"\n" +
			"	public final test.Ref<test.A.B[][]>[][] genericField = new Ref[0][];\n" +
			"	public final test.@Nullable Ref<test.A.@Nullable B[][]>[][] genericFieldWithNullable1 = new Ref[0][];;\n" +
			"	public final test.Ref<test.A.B[] @Nullable []>[] @Nullable[] genericFieldWithNullable2 = new Ref[0][];;\n" +
			"}\n" +
			"",
		};
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("rawtypes"); // javac detects rawtypes at new Ref[0][0]
	runner.runConformTest();
	runNegativeTestWithLibs(
		new String[] {
			"test/BinaryUsage.java",
			"package test;\n" +
			"\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"class BinaryUsage {\n" +
			"	void check(test.A.@NonNull B @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"	void checkGeneric(test.@NonNull Ref<test.A.@NonNull B @NonNull [] @NonNull []> @NonNull [] @NonNull [] s) {\n" +
			"	}\n" +
			"\n" +
			"	void f(X x) {\n" +
			"		check(x.field);\n" +
			"		check(x.fieldWithNullable1);\n" +
			"		check(x.fieldWithNullable2);\n" +
			"		check(x.list.get());\n" +
			"		check(x.listWithNullable1.get());\n" +
			"		check(x.listWithNullable2.get());\n" +
			"		check(x.method());\n" +
			"		check(x.methodWithNullable1());\n" +
			"		check(x.methodWithNullable2());\n" +
			"		checkGeneric(x.genericField);\n" +
			"		checkGeneric(x.genericFieldWithNullable1);\n" +
			"		checkGeneric(x.genericFieldWithNullable2);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\BinaryUsage.java (at line 14)\n" +
		"	check(x.fieldWithNullable1);\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"2. ERROR in test\\BinaryUsage.java (at line 15)\n" +
		"	check(x.fieldWithNullable2);\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"3. ERROR in test\\BinaryUsage.java (at line 17)\n" +
		"	check(x.listWithNullable1.get());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"4. ERROR in test\\BinaryUsage.java (at line 18)\n" +
		"	check(x.listWithNullable2.get());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"5. ERROR in test\\BinaryUsage.java (at line 20)\n" +
		"	check(x.methodWithNullable1());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@Nullable B @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"6. ERROR in test\\BinaryUsage.java (at line 21)\n" +
		"	check(x.methodWithNullable2());\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'A.@NonNull B @NonNull[] @NonNull[]\' but this expression has type \'A.@NonNull B @NonNull[] @Nullable[]\'\n" +
		"----------\n" +
		"7. ERROR in test\\BinaryUsage.java (at line 23)\n" +
		"	checkGeneric(x.genericFieldWithNullable1);\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Ref<A.@NonNull B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@Nullable Ref<A.@Nullable B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\'\n" +
		"----------\n" +
		"8. ERROR in test\\BinaryUsage.java (at line 24)\n" +
		"	checkGeneric(x.genericFieldWithNullable2);\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch (type annotations): required \'@NonNull Ref<A.@NonNull B @NonNull[] @NonNull[]> @NonNull[] @NonNull[]\' but this expression has type \'@NonNull Ref<A.@NonNull B @NonNull[] @Nullable[]> @NonNull[] @Nullable[]\'\n" +
		"----------\n"
	);
}
public void testBug499589arrayAllocation() {
	runNegativeTestWithLibs(
		new String[] {
			"test/ArrayAllocation.java",
			"package test;\n" +
			"\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault({ ARRAY_CONTENTS })\n" +
			"public class ArrayAllocation {\n" +
			"	public Integer[] x1 = { 1, 2, 3, null };\n" +
			"	public Integer[] x2 = new Integer[] { 1, 2, 3 };\n" +
			"	public Integer[] x3 = new Integer[] { 1, 2, 3, null };\n" +
			"	public Integer[] x4 = new Integer[3];\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\ArrayAllocation.java (at line 9)\n" +
		"	public Integer[] x1 = { 1, 2, 3, null };\n" +
		"	                                 ^^^^\n" +
		"Null type mismatch: required \'@NonNull Integer\' but the provided value is null\n" +
		"----------\n" +
		"2. ERROR in test\\ArrayAllocation.java (at line 11)\n" +
		"	public Integer[] x3 = new Integer[] { 1, 2, 3, null };\n" +
		"	                                               ^^^^\n" +
		"Null type mismatch: required \'@NonNull Integer\' but the provided value is null\n" +
		"----------\n" +
		"3. WARNING in test\\ArrayAllocation.java (at line 12)\n" +
		"	public Integer[] x4 = new Integer[3];\n" +
		"	                      ^^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'Integer[]\' needs unchecked conversion to conform to \'@NonNull Integer []\'\n" +
		"----------\n"
	);
}
public void testBug499589generics() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Methods.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault({ DefaultLocation.TYPE_ARGUMENT, DefaultLocation.ARRAY_CONTENTS })\n" +
			"public class Methods {\n" +
			"	static interface List<T> {\n" +
			"		T get(int i);\n" +
			"	}\n" +
			"\n" +
			"	public static List<String> f0(List<String> list) {\n" +
			"		@NonNull\n" +
			"		Object o = list.get(0);\n" +
			"		return list;\n" +
			"	}\n" +
			"\n" +
			"	public static String[] f1(String[] array) {\n" +
			"		@NonNull\n" +
			"		Object o = array[0];\n" +
			"		return array;\n" +
			"	}\n" +
			"\n" +
			"	public static <T> List<T> g0(List<T> list) {\n" +
			"		@NonNull\n" +
			"		Object o = list.get(0); // problem\n" +
			"		return list;\n" +
			"	}\n" +
			"\n" +
			"	public static <T> T[] g1(T[] array) {\n" +
			"		@NonNull\n" +
			"		Object o = array[0]; // problem\n" +
			"		return array;\n" +
			"	}\n" +
			"\n" +
			"	public static <@NonNull T> List<@NonNull T> h0(List<T> list) {\n" +
			"		@NonNull\n" +
			"		Object o = list.get(0);\n" +
			"		return list;\n" +
			"	}\n" +
			"\n" +
			"	public static <@NonNull T> @NonNull T[] h1(T[] array) {\n" +
			"		@NonNull\n" +
			"		Object o = array[0];\n" +
			"		return array;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\Methods.java (at line 26)\n" +
		"	Object o = list.get(0); // problem\n" +
		"	           ^^^^^^^^^^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. ERROR in test\\Methods.java (at line 32)\n" +
		"	Object o = array[0]; // problem\n" +
		"	           ^^^^^^^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n"
	);
}
public void testBug511723() {
	runNegativeTestWithLibs(
		new String[] {
			"test/ArrayVsList.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class ArrayVsList {\n" +
			"	static interface List<T> {\n" +
			"		T get(int i);\n" +
			"	}\n" +
			"	public static <T> void f(List<T> list) {\n" +
			"		@NonNull\n" +
			"		Object o = list.get(0); // problem\n" +
			"		o.hashCode();\n" +
			"	}\n" +
			"\n" +
			"	public static <T> void g(T[] array) {\n" +
			"		@NonNull\n" +
			"		Object o = array[0]; // problem\n" +
			"		o.hashCode();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\ArrayVsList.java (at line 11)\n" +
		"	Object o = list.get(0); // problem\n" +
		"	           ^^^^^^^^^^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n" +
		"2. ERROR in test\\ArrayVsList.java (at line 17)\n" +
		"	Object o = array[0]; // problem\n" +
		"	           ^^^^^^^^\n" +
		"Null type safety: required \'@NonNull\' but this expression has type \'T\', a free type variable that may represent a \'@Nullable\' type\n" +
		"----------\n"
	);
}
public void testBug498084() {
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"\n" +
			"	protected static final <K, V> V cache(final Map<K, V> cache, final V value, final Function<V, K> keyFunction) {\n" +
			"		cache.put(keyFunction.apply(value), value);\n" +
			"		return value;\n" +
			"	}\n" +
			"\n" +
			"	public static final void main(final String[] args) {\n" +
			"		Map<Integer, String> cache = new HashMap<>();\n" +
			"		cache(cache, \"test\", String::length); // Warning: Null type safety at\n" +
			"											// method return type: Method\n" +
			"											// descriptor\n" +
			"											// Function<String,Integer>.apply(String)\n" +
			"											// promises '@NonNull Integer'\n" +
			"											// but referenced method\n" +
			"											// provides 'int'\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug498084b() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test2.java",
			"package test;\n" +
			"\n" +
			"import java.util.function.Consumer;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class Test2 {\n" +
			"	static void f(int i) {\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		Consumer<@Nullable Integer> sam = Test2::f;\n" +
			"		sam.accept(null); // <- NullPointerExpection when run\n" +
			"		Consumer<Integer> sam2 = Test2::f;\n" +
			"		sam2.accept(null); // variation: unchecked \n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\Test2.java (at line 12)\n" +
		"	Consumer<@Nullable Integer> sam = Test2::f;\n" +
		"	                                  ^^^^^^^^\n" +
		"Null type mismatch at parameter 1: required \'int\' but provided \'@Nullable Integer\' via method descriptor Consumer<Integer>.accept(Integer)\n" +
		"----------\n" +
		"2. WARNING in test\\Test2.java (at line 14)\n" +
		"	Consumer<Integer> sam2 = Test2::f;\n" +
		"	                         ^^^^^^^^\n" +
		"Null type safety: parameter 1 provided via method descriptor Consumer<Integer>.accept(Integer) needs unchecked conversion to conform to \'int\'\n" +
		"----------\n"
	);
}
public void testBug513495() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test3.java",
			"package test;\n" +
			"\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class Test3 {\n" +
			"	public static void main(String[] args) {\n" +
			"		Function<@Nullable Integer, Object> sam = Integer::intValue;\n" +
			"		sam.apply(null); // <- NullPointerExpection\n" +
			"		Function<Integer, Object> sam2 = Integer::intValue;\n" +
			"		sam2.apply(null); // variation: unchecked, so intentionally no warning reported, but would give NPE too \n" +
			"	}\n" +
			"	void wildcards(Class<?>[] params) { // unchecked case with wildcards\n" +
			"		java.util.Arrays.stream(params).map(Class::getName).toArray(String[]::new);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\Test3.java (at line 9)\n" +
		"	Function<@Nullable Integer, Object> sam = Integer::intValue;\n" +
		"	                                          ^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch at parameter 'this': required \'@NonNull Integer\' but provided \'@Nullable Integer\' via method descriptor Function<Integer,Object>.apply(Integer)\n" +
		"----------\n"
	);
}
public void testBug513855() {
	runConformTestWithLibs(
		new String[] {
			"test1/X.java",
			"package test1;\n" +
			"\n" +
			"import java.math.BigDecimal;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"	interface Sink<T extends Number> {\n" +
			"		void receive(T t);\n" +
			"	}\n" +
			"\n" +
			"	interface Source<U extends BigDecimal> {\n" +
			"		U get();\n" +
			"	}\n" +
			"\n" +
			"	void nn(Object x) {\n" +
			"	}\n" +
			"\n" +
			"	void f(Source<?> source) {\n" +
			"		nn(source.get());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug513855lambda() {
	runConformTestWithLibs(
		new String[] {
			"test1/Lambda3.java",
			"package test1;\n" +
			"\n" +
			"import java.math.BigDecimal;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Lambda3 {\n" +
			"	interface Sink<T extends Number> {\n" +
			"		void receive(T t);\n" +
			"	}\n" +
			"\n" +
			"	interface Source<U extends BigDecimal> {\n" +
			"		void sendTo(Sink<? super U> c);\n" +
			"	}\n" +
			"\n" +
			"	void f(Source<?> source) {\n" +
			"		source.sendTo(a -> a.scale());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug514091() {
	runConformTestWithLibs(
		new String[] {
			"test1/SAM.java",
			"package test1;\n" +
			"\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"interface SAM<A> {\n" +
			"	void f(A[] a);\n" +
			"}\n" +
			""
		},
		getCompilerOptions(),
		""
	);
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"test1/LambdaNN.java",
			"package test1;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class LambdaNN {\n" +
			"	void g1() {\n" +
			"		SAM<? super Number> sam = (Number @NonNull [] a) -> {};\n" +
			"		sam.f(new Number[0]);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug514570() {
	final Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import java.util.List;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * {@link #bug()}\n" +
			"	 */\n" +
			"	<E, T extends List<@NonNull E>> void bug() {\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		compilerOptions,
		""
	);
}
public void testBug514977() {
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.DefaultLocation;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"public class Test {\n" +
			"	static void nn(@NonNull Object i) {\n" +
			"		i.hashCode();\n" +
			"	}\n" +
			"\n" +
			"	static void f(@NonNull Integer @NonNull... args) {\n" +
			"		nn(args);\n" +
			"		for (Integer s : args) {\n" +
			"			nn(s);\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	@NonNullByDefault({ DefaultLocation.ARRAY_CONTENTS, DefaultLocation.PARAMETER })\n" +
			"	static void g(Integer... args) {\n" +
			"		nn(args);\n" +
			"		for (Integer s : args) {\n" +
			"			nn(s);\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		Integer i = args.length == 0 ? null : 1;\n" +
			"		Integer[] array = i == null ? null : new Integer[] {i};\n" +
			"		f(array);\n" +
			"		f(i);\n" +
			"		f(1, i);\n" +
			"		g(array);\n" +
			"		g(i);\n" +
			"		g(1, i);\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in test\\Test.java (at line 30)\n" +
		"	f(array);\n" +
		"	  ^^^^^\n" +
		"Null type mismatch: required \'@NonNull Integer @NonNull[]\' but the provided value is inferred as @Nullable\n" +
		"----------\n" +
		"2. ERROR in test\\Test.java (at line 31)\n" +
		"	f(i);\n" +
		"	  ^\n" +
		"Null type mismatch: required \'@NonNull Integer\' but the provided value is inferred as @Nullable\n" +
		"----------\n" +
		"3. ERROR in test\\Test.java (at line 32)\n" +
		"	f(1, i);\n" +
		"	     ^\n" +
		"Null type mismatch: required \'@NonNull Integer\' but the provided value is inferred as @Nullable\n" +
		"----------\n" +
		"4. ERROR in test\\Test.java (at line 33)\n" +
		"	g(array);\n" +
		"	  ^^^^^\n" +
		"Null type mismatch: required \'@NonNull Integer @NonNull[]\' but the provided value is inferred as @Nullable\n" +
		"----------\n" +
		"5. ERROR in test\\Test.java (at line 34)\n" +
		"	g(i);\n" +
		"	  ^\n" +
		"Null type mismatch: required \'@NonNull Integer\' but the provided value is inferred as @Nullable\n" +
		"----------\n" +
		"6. ERROR in test\\Test.java (at line 35)\n" +
		"	g(1, i);\n" +
		"	     ^\n" +
		"Null type mismatch: required \'@NonNull Integer\' but the provided value is inferred as @Nullable\n" +
		"----------\n"
	);
}
public void testBug515292() {
	runConformTestWithLibs(
		new String[] {
			"test/BoundedByFinal.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public abstract class BoundedByFinal {\n" +
			"	abstract <T extends @Nullable String> void setSelection(T[] selectedObjects);\n" +
			"\n" +
			"	abstract @NonNull String @NonNull [] toArray1();\n" +
			"\n" +
			"	abstract @Nullable String @NonNull [] toArray2();\n" +
			"\n" +
			"	void test() {\n" +
			"		setSelection(toArray1());\n" +
			"		setSelection(toArray2());\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug526555() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING);

	runWarningTestWithLibs(
		true/*flush*/,
		new String[] {
			"ztest/OverrideTest.java",
			"package ztest;\n" +
			"\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.FIELD;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"import static org.eclipse.jdt.annotation.DefaultLocation.TYPE_BOUND;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"interface X509TrustManager {\n" +
			"	void checkClientTrusted(String[] arg0, String arg1);\n" +
			"\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS })\n" +
			"public class OverrideTest implements X509TrustManager {\n" +
			"	@Override\n" +
			"	public void checkClientTrusted(String @Nullable [] arg0, @Nullable String arg1) {\n" +
			"	}\n" +
			"}",
		},
		customOptions,
		"----------\n" +
		"1. WARNING in ztest\\OverrideTest.java (at line 21)\n" +
		"	public void checkClientTrusted(String @Nullable [] arg0, @Nullable String arg1) {\n" +
		"	                               ^^^^^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter arg0, inherited method from X509TrustManager declares this parameter as \'String[]\' (mismatching null constraints)\n" +
		"----------\n"
	);
}
public void testBug530913() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runConformTestWithLibs(
		false,
		new String[] {
			"nnbd_test2/Data.java",
			"package nnbd_test2;\n" +
			"\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"import annotation.DefaultLocation;\n" +
			"import annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Data {\n" +
			"    public void f(@NonNullByDefault({}) String s1, String s2) {\n" +
			"        s1.equals(s2);\n" +
			"    }\n" +
			"\n" +
			"    public void g(String s1, @NonNullByDefault({}) String s2) {\n" +
			"        s1.equals(s2);\n" +
			"    }\n" +
			"    \n" +
			"    @NonNullByDefault({})\n" +
			"    public void h(@NonNullByDefault({ DefaultLocation.PARAMETER }) Supplier<String> s1, @NonNullByDefault Supplier<String> s2) {\n" +
			"        s1.equals(s2);\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"nnbd_test1/Test.java",
			"package nnbd_test1;\n" +
			"\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"import annotation.DefaultLocation;\n" +
			"import annotation.NonNullByDefault;\n" +
			"import nnbd_test2.Data;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"    void f(@NonNullByDefault({}) String s1, String s2) {\n" +
			"        if (s1 == null) {\n" +
			"            System.out.println(\"s is null\");\n" +
			"        }\n" +
			"        if (s2 == null) { // warning expected\n" +
			"            System.out.println(\"s2 is null\");\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    void g(String s1, @NonNullByDefault({}) String s2) {\n" +
			"        if (s1 == null) { // warning expected\n" +
			"            System.out.println(\"s is null\");\n" +
			"        }\n" +
			"        if (s2 == null) {\n" +
			"            System.out.println(\"s2 is null\");\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    @NonNullByDefault({})\n" +
			"    void h(@NonNullByDefault({ DefaultLocation.PARAMETER }) Supplier<String> s1, @NonNullByDefault Supplier<String> s2) {\n" +
			"        if (s1 == null) { // warning expected\n" +
			"            System.out.println(\"s is null\");\n" +
			"            return;\n" +
			"        }\n" +
			"        if (s2 == null) { // warning expected\n" +
			"            System.out.println(\"s2 is null\");\n" +
			"            return;\n" +
			"        }\n" +
			"        if (s1.get() == null) {\n" +
			"            System.out.println(\"s is null\");\n" +
			"        }\n" +
			"        if (s2.get() == null) { // warning expected\n" +
			"            System.out.println(\"s2 is null\");\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    void checkInvocation() {\n" +
			"        Test d = new Test();\n" +
			"        d.f(null, null); // warning on the second null expected\n" +
			"        d.g(null, null); // warning on the first null expected\n" +
			"    }\n" +
			"\n" +
			"    void checkBTBInvocation() {\n" +
			"        Data d = new Data();\n" +
			"        d.f(null, null); // warning on the second null expected\n" +
			"        d.g(null, null); // warning on the first null expected\n" +
			"    }\n" +
			"\n" +
			"    void checkInheritance() {\n" +
			"        Test t = new Test() {\n" +
			"            @Override\n" +
			"            void f(String s1, String s2) { // warning on the first parameter expected\n" +
			"                super.f(null, null); // warning on the second null expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            void g(String s1, String s2) { // warning on the second parameter expected\n" +
			"                super.g(null, null); // warning on the first null expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            void h(Supplier<String> s1, Supplier<String> s2) { // warning on the first parameter expected\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"\n" +
			"    void checkBTBInheritance() {\n" +
			"        Data d = new Data() {\n" +
			"            @Override\n" +
			"            public void f(String s1, String s2) { // warning on the first parameter expected\n" +
			"                super.f(null, null); // warning on the second null expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public void g(String s1, String s2) { // warning on the second parameter expected\n" +
			"                super.g(null, null); // warning on the first null expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public void h(Supplier<String> s1, Supplier<String> s2) { // warning on the first parameter expected\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		"----------\n" +
		"1. ERROR in nnbd_test1\\Test.java (at line 15)\n" +
		"	if (s2 == null) { // warning expected\n" +
		"	    ^^\n" +
		"Redundant null check: comparing \'@NonNull String\' against null\n" +
		"----------\n" +
		"2. ERROR in nnbd_test1\\Test.java (at line 21)\n" +
		"	if (s1 == null) { // warning expected\n" +
		"	    ^^\n" +
		"Redundant null check: comparing \'@NonNull String\' against null\n" +
		"----------\n" +
		"3. ERROR in nnbd_test1\\Test.java (at line 31)\n" +
		"	if (s1 == null) { // warning expected\n" +
		"	    ^^\n" +
		"Redundant null check: comparing \'@NonNull Supplier<String>\' against null\n" +
		"----------\n" +
		"4. ERROR in nnbd_test1\\Test.java (at line 35)\n" +
		"	if (s2 == null) { // warning expected\n" +
		"	    ^^\n" +
		"Redundant null check: comparing \'@NonNull Supplier<@NonNull String>\' against null\n" +
		"----------\n" +
		"5. ERROR in nnbd_test1\\Test.java (at line 42)\n" +
		"	if (s2.get() == null) { // warning expected\n" +
		"	    ^^^^^^^^\n" +
		"Redundant null check: comparing \'@NonNull String\' against null\n" +
		"----------\n" +
		"6. ERROR in nnbd_test1\\Test.java (at line 49)\n" +
		"	d.f(null, null); // warning on the second null expected\n" +
		"	          ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"7. ERROR in nnbd_test1\\Test.java (at line 50)\n" +
		"	d.g(null, null); // warning on the first null expected\n" +
		"	    ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"8. ERROR in nnbd_test1\\Test.java (at line 55)\n" +
		"	d.f(null, null); // warning on the second null expected\n" +
		"	          ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"9. ERROR in nnbd_test1\\Test.java (at line 56)\n" +
		"	d.g(null, null); // warning on the first null expected\n" +
		"	    ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"10. ERROR in nnbd_test1\\Test.java (at line 62)\n" +
		"	void f(String s1, String s2) { // warning on the first parameter expected\n" +
		"	       ^^^^^^\n" +
		"Illegal redefinition of parameter s1, inherited method from Test does not constrain this parameter\n" +
		"----------\n" +
		"11. ERROR in nnbd_test1\\Test.java (at line 63)\n" +
		"	super.f(null, null); // warning on the second null expected\n" +
		"	              ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"12. ERROR in nnbd_test1\\Test.java (at line 67)\n" +
		"	void g(String s1, String s2) { // warning on the second parameter expected\n" +
		"	                  ^^^^^^\n" +
		"Illegal redefinition of parameter s2, inherited method from Test does not constrain this parameter\n" +
		"----------\n" +
		"13. ERROR in nnbd_test1\\Test.java (at line 68)\n" +
		"	super.g(null, null); // warning on the first null expected\n" +
		"	        ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"14. ERROR in nnbd_test1\\Test.java (at line 72)\n" +
		"	void h(Supplier<String> s1, Supplier<String> s2) { // warning on the first parameter expected\n" +
		"	       ^^^^^^^^\n" +
		"Illegal redefinition of parameter s1, inherited method from Test declares this parameter as \'@NonNull Supplier<String>\' (mismatching null constraints)\n" +
		"----------\n" +
		"15. ERROR in nnbd_test1\\Test.java (at line 80)\n" +
		"	public void f(String s1, String s2) { // warning on the first parameter expected\n" +
		"	              ^^^^^^\n" +
		"Illegal redefinition of parameter s1, inherited method from Data does not constrain this parameter\n" +
		"----------\n" +
		"16. ERROR in nnbd_test1\\Test.java (at line 81)\n" +
		"	super.f(null, null); // warning on the second null expected\n" +
		"	              ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"17. ERROR in nnbd_test1\\Test.java (at line 85)\n" +
		"	public void g(String s1, String s2) { // warning on the second parameter expected\n" +
		"	                         ^^^^^^\n" +
		"Illegal redefinition of parameter s2, inherited method from Data does not constrain this parameter\n" +
		"----------\n" +
		"18. ERROR in nnbd_test1\\Test.java (at line 86)\n" +
		"	super.g(null, null); // warning on the first null expected\n" +
		"	        ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"19. ERROR in nnbd_test1\\Test.java (at line 90)\n" +
		"	public void h(Supplier<String> s1, Supplier<String> s2) { // warning on the first parameter expected\n" +
		"	              ^^^^^^^^\n" +
		"Illegal redefinition of parameter s1, inherited method from Data declares this parameter as \'@NonNull Supplier<String>\' (mismatching null constraints)\n" +
		"----------\n"
	);
}
public void testBug530913b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NNBDField,annotation.NNBDParam,annotation.NNBDReturn,annotation.NNBDTypeArg,annotation.NNBDTypeBound");
	customOptions.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	customOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"    DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	Runner runner = new Runner();
	runner.classLibraries = this.LIBS;
	runner.shouldFlushOutputDirectory = false;
	runner.testFiles =
		new String[] {
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import annotation.*;\n" +
			"\n" +
			"interface C<T1, T2> {\n" +
			"}\n" +
			"\n" +
			"abstract class X {\n" +
			"    @NonNullByDefault(DefaultLocation.RETURN_TYPE) abstract void f2(@NonNullByDefault C<Object, ? extends Number> p1);\n" +
			"}\n"
		};
	runner.customOptions = customOptions;
	runner.javacTestOptions = new JavacTestOptions.SuppressWarnings("auxiliaryclass");
	runner.runConformTest();

	runner.testFiles =
		new String[] {
			"test/ExplicitNonNull.java",
			"package test;\n" +
			"\n" +
			"import annotation.*;\n" +
			"\n" +
			"\n" +
			"class ExplicitNonNull extends X {\n" +
			"    void f2(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1) {\n" +
			"    }\n" +
			"}\n" +
			"",
		};
	runner.runConformTest();
}
public void testBug530971() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NNBDField,annotation.NNBDParam,annotation.NNBDReturn,annotation.NNBDTypeArg,annotation.NNBDTypeBound");
	customOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NNBDField.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			" \n" +
			"public @interface NNBDField {\n" +
			"	DefaultLocation[] value() default { FIELD };\n" +
			"}\n" +
			"",
			"annotation/NNBDParam.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"public @interface NNBDParam {\n" +
			"    DefaultLocation[] value() default { PARAMETER };\n" +
			"}\n" +
			"",
			"annotation/NNBDReturn.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"\n" +
			"public @interface NNBDReturn {\n" +
			"    DefaultLocation[] value() default { RETURN_TYPE };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeArg.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"\n" +
			"public @interface NNBDTypeArg {\n" +
			"    DefaultLocation[] value() default { TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeBound.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			"\n" +
			"public @interface NNBDTypeBound {\n" +
			"    DefaultLocation[] value() default { TYPE_BOUND };\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"    DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import annotation.NNBDField;\n" +
			"import annotation.NNBDParam;\n" +
			"import annotation.NNBDReturn;\n" +
			"import annotation.NNBDTypeArg;\n" +
			"import annotation.NNBDTypeBound;\n" +
			"import annotation.NonNull;\n" +
			"\n" +
			"interface C<T1, T2> {\n" +
			"}\n" +
			"\n" +
			"abstract class X {\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDField\n" +
			"    @NNBDTypeBound\n" +
			"    C<Object, ? extends Number> f1; // warning 1\n" +
			"\n" +
			"    @NonNull\n" +
			"    C<@NonNull Object, ? extends @NonNull Number> f2; // warning 2\n" +
			"\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeBound\n" +
			"    abstract Object m1(C<Object, ? extends Number> p1, Object p2);\n" +
			"\n" +
			"    abstract @NNBDReturn Object m2(@NNBDParam @NNBDTypeArg @NNBDTypeBound C<Object, ? extends Number> p1,\n" +
			"            @NNBDParam Object p2);\n" +
			"\n" +
			"    abstract @NonNull Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2);\n" +
			"}\n" +
			"\n" +
			"class ExplicitNonNull extends X {\n" +
			"    Object m1(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 3 on return type\n" +
			"        f1 = null; // warning 4\n" +
			"        f2 = null; // warning 5\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m2(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 6 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 7 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"@NNBDParam\n" +
			"@NNBDTypeArg\n" +
			"@NNBDTypeBound\n" +
			"class OnClass extends X {\n" +
			"    Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 8 on return type\n" +
			"        f1 = null; // warning 9\n" +
			"        f2 = null; // warning 10\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 11 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 12 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"class Test {\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDTypeBound\n" +
			"    X onField = new X() {\n" +
			"        Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 13 on return type\n" +
			"            f1 = null; // warning 14\n" +
			"            f2 = null; // warning 15\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"\n" +
			"        Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 16 on return type\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"\n" +
			"        Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 17 on return type\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"    };\n" +
			"\n" +
			"    {\n" +
			"        @NNBDParam\n" +
			"        @NNBDTypeArg\n" +
			"        @NNBDTypeBound\n" +
			"        X onLocal = new X() {\n" +
			"            Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 18 on return type\n" +
			"                f1 = null; // warning 19\n" +
			"                f2 = null; // warning 20\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 21 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 22 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDTypeBound\n" +
			"    void onMethod() {\n" +
			"        X l1 = new X() {\n" +
			"            Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 23 on return type\n" +
			"                f1 = null; // warning 24\n" +
			"                f2 = null; // warning 25\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 26 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 27 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		"----------\n" +
		"1. ERROR in test\\Test.java (at line 17)\n" +
		"	C<Object, ? extends Number> f1; // warning 1\n" +
		"	                            ^^\n" +
		"The @NonNull field f1 may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in test\\Test.java (at line 20)\n" +
		"	C<@NonNull Object, ? extends @NonNull Number> f2; // warning 2\n" +
		"	                                              ^^\n" +
		"The @NonNull field f2 may not have been initialized\n" +
		"----------\n" +
		"3. ERROR in test\\Test.java (at line 35)\n" +
		"	Object m1(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 3 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"4. ERROR in test\\Test.java (at line 36)\n" +
		"	f1 = null; // warning 4\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"5. ERROR in test\\Test.java (at line 37)\n" +
		"	f2 = null; // warning 5\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"6. ERROR in test\\Test.java (at line 43)\n" +
		"	Object m2(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 6 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"7. ERROR in test\\Test.java (at line 49)\n" +
		"	Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 7 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"8. ERROR in test\\Test.java (at line 60)\n" +
		"	Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 8 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"9. ERROR in test\\Test.java (at line 61)\n" +
		"	f1 = null; // warning 9\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"10. ERROR in test\\Test.java (at line 62)\n" +
		"	f2 = null; // warning 10\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"11. ERROR in test\\Test.java (at line 68)\n" +
		"	Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 11 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"12. ERROR in test\\Test.java (at line 74)\n" +
		"	Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 12 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"13. ERROR in test\\Test.java (at line 86)\n" +
		"	Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 13 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"14. ERROR in test\\Test.java (at line 87)\n" +
		"	f1 = null; // warning 14\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"15. ERROR in test\\Test.java (at line 88)\n" +
		"	f2 = null; // warning 15\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"16. ERROR in test\\Test.java (at line 94)\n" +
		"	Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 16 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"17. ERROR in test\\Test.java (at line 100)\n" +
		"	Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 17 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"18. ERROR in test\\Test.java (at line 112)\n" +
		"	Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 18 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"19. ERROR in test\\Test.java (at line 113)\n" +
		"	f1 = null; // warning 19\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"20. ERROR in test\\Test.java (at line 114)\n" +
		"	f2 = null; // warning 20\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"21. ERROR in test\\Test.java (at line 120)\n" +
		"	Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 21 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"22. ERROR in test\\Test.java (at line 126)\n" +
		"	Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 22 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"23. ERROR in test\\Test.java (at line 139)\n" +
		"	Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 23 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"24. ERROR in test\\Test.java (at line 140)\n" +
		"	f1 = null; // warning 24\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"25. ERROR in test\\Test.java (at line 141)\n" +
		"	f2 = null; // warning 25\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"26. ERROR in test\\Test.java (at line 147)\n" +
		"	Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 26 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"27. ERROR in test\\Test.java (at line 153)\n" +
		"	Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 27 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n"
	);
}

// same as testBug530971, but X is read via class file
public void testBug530971_BTB() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NNBDField,annotation.NNBDParam,annotation.NNBDReturn,annotation.NNBDTypeArg,annotation.NNBDTypeBound");
	customOptions.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	customOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NNBDField.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			" \n" +
			"public @interface NNBDField {\n" +
			"	DefaultLocation[] value() default { FIELD };\n" +
			"}\n" +
			"",
			"annotation/NNBDParam.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"public @interface NNBDParam {\n" +
			"    DefaultLocation[] value() default { PARAMETER };\n" +
			"}\n" +
			"",
			"annotation/NNBDReturn.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"\n" +
			"public @interface NNBDReturn {\n" +
			"    DefaultLocation[] value() default { RETURN_TYPE };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeArg.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"\n" +
			"public @interface NNBDTypeArg {\n" +
			"    DefaultLocation[] value() default { TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeBound.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			"\n" +
			"public @interface NNBDTypeBound {\n" +
			"    DefaultLocation[] value() default { TYPE_BOUND };\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"    DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runConformTestWithLibs(
		false,
		new String[] {
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import annotation.NNBDField;\n" +
			"import annotation.NNBDParam;\n" +
			"import annotation.NNBDReturn;\n" +
			"import annotation.NNBDTypeArg;\n" +
			"import annotation.NNBDTypeBound;\n" +
			"import annotation.NonNull;\n" +
			"\n" +
			"interface C<T1, T2> {\n" +
			"}\n" +
			"\n" +
			"@SuppressWarnings(\"null\")\n" +
			"abstract class X {\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDField\n" +
			"    @NNBDTypeBound\n" +
			"    C<Object, ? extends Number> f1; // warning 1\n" +
			"\n" +
			"    @NonNull\n" +
			"    C<@NonNull Object, ? extends @NonNull Number> f2; // warning 2\n" +
			"\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeBound\n" +
			"    abstract Object m1(C<Object, ? extends Number> p1, Object p2);\n" +
			"\n" +
			"    abstract @NNBDReturn Object m2(@NNBDParam @NNBDTypeArg @NNBDTypeBound C<Object, ? extends Number> p1,\n" +
			"            @NNBDParam Object p2);\n" +
			"\n" +
			"    abstract @NonNull Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2);\n" +
			"}\n"
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import annotation.NNBDField;\n" +
			"import annotation.NNBDParam;\n" +
			"import annotation.NNBDReturn;\n" +
			"import annotation.NNBDTypeArg;\n" +
			"import annotation.NNBDTypeBound;\n" +
			"import annotation.NonNull;\n" +
			"\n" +
			"interface C_IGNORED<T1, T2> {\n" +
			"}\n" +
			"\n" +
			"abstract class X_IGNORED {\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDField\n" +
			"    @NNBDTypeBound\n" +
			"    C<Object, ? extends Number> f1; // warning 1\n" +
			"\n" +
			"    @NonNull\n" +
			"    C<@NonNull Object, ? extends @NonNull Number> f2; // warning 2\n" +
			"\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeBound\n" +
			"    abstract Object m1(C<Object, ? extends Number> p1, Object p2);\n" +
			"\n" +
			"    abstract @NNBDReturn Object m2(@NNBDParam @NNBDTypeArg @NNBDTypeBound C<Object, ? extends Number> p1,\n" +
			"            @NNBDParam Object p2);\n" +
			"\n" +
			"    abstract @NonNull Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2);\n" +
			"}\n" +
			"\n" +
			"class ExplicitNonNull extends X {\n" +
			"    Object m1(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 3 on return type\n" +
			"        f1 = null; // warning 4\n" +
			"        f2 = null; // warning 5\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m2(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 6 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 7 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"@NNBDParam\n" +
			"@NNBDTypeArg\n" +
			"@NNBDTypeBound\n" +
			"class OnClass extends X {\n" +
			"    Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 8 on return type\n" +
			"        f1 = null; // warning 9\n" +
			"        f2 = null; // warning 10\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 11 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"\n" +
			"    Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 12 on return type\n" +
			"        f1 = p1;\n" +
			"        f2 = p1;\n" +
			"        return p2;\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"class Test {\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDTypeBound\n" +
			"    X onField = new X() {\n" +
			"        Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 13 on return type\n" +
			"            f1 = null; // warning 14\n" +
			"            f2 = null; // warning 15\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"\n" +
			"        Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 16 on return type\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"\n" +
			"        Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 17 on return type\n" +
			"            f1 = p1;\n" +
			"            f2 = p1;\n" +
			"            return p2;\n" +
			"        }\n" +
			"    };\n" +
			"\n" +
			"    {\n" +
			"        @NNBDParam\n" +
			"        @NNBDTypeArg\n" +
			"        @NNBDTypeBound\n" +
			"        X onLocal = new X() {\n" +
			"            Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 18 on return type\n" +
			"                f1 = null; // warning 19\n" +
			"                f2 = null; // warning 20\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 21 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 22 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"\n" +
			"    @NNBDParam\n" +
			"    @NNBDTypeArg\n" +
			"    @NNBDTypeBound\n" +
			"    void onMethod() {\n" +
			"        X l1 = new X() {\n" +
			"            Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 23 on return type\n" +
			"                f1 = null; // warning 24\n" +
			"                f2 = null; // warning 25\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 26 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"\n" +
			"            Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 27 on return type\n" +
			"                f1 = p1;\n" +
			"                f2 = p1;\n" +
			"                return p2;\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		"----------\n" +
		"1. ERROR in test\\Test.java (at line 17)\n" +
		"	C<Object, ? extends Number> f1; // warning 1\n" +
		"	                            ^^\n" +
		"The @NonNull field f1 may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in test\\Test.java (at line 20)\n" +
		"	C<@NonNull Object, ? extends @NonNull Number> f2; // warning 2\n" +
		"	                                              ^^\n" +
		"The @NonNull field f2 may not have been initialized\n" +
		"----------\n" +
		"3. ERROR in test\\Test.java (at line 35)\n" +
		"	Object m1(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 3 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"4. ERROR in test\\Test.java (at line 36)\n" +
		"	f1 = null; // warning 4\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"5. ERROR in test\\Test.java (at line 37)\n" +
		"	f2 = null; // warning 5\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"6. ERROR in test\\Test.java (at line 43)\n" +
		"	Object m2(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 6 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"7. ERROR in test\\Test.java (at line 49)\n" +
		"	Object m3(@NonNull C<@NonNull Object, ? extends @NonNull Number> p1, @NonNull Object p2) { // warning 7 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"8. ERROR in test\\Test.java (at line 60)\n" +
		"	Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 8 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"9. ERROR in test\\Test.java (at line 61)\n" +
		"	f1 = null; // warning 9\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"10. ERROR in test\\Test.java (at line 62)\n" +
		"	f2 = null; // warning 10\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"11. ERROR in test\\Test.java (at line 68)\n" +
		"	Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 11 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"12. ERROR in test\\Test.java (at line 74)\n" +
		"	Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 12 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"13. ERROR in test\\Test.java (at line 86)\n" +
		"	Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 13 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"14. ERROR in test\\Test.java (at line 87)\n" +
		"	f1 = null; // warning 14\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"15. ERROR in test\\Test.java (at line 88)\n" +
		"	f2 = null; // warning 15\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"16. ERROR in test\\Test.java (at line 94)\n" +
		"	Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 16 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"17. ERROR in test\\Test.java (at line 100)\n" +
		"	Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 17 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"18. ERROR in test\\Test.java (at line 112)\n" +
		"	Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 18 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"19. ERROR in test\\Test.java (at line 113)\n" +
		"	f1 = null; // warning 19\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"20. ERROR in test\\Test.java (at line 114)\n" +
		"	f2 = null; // warning 20\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"21. ERROR in test\\Test.java (at line 120)\n" +
		"	Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 21 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"22. ERROR in test\\Test.java (at line 126)\n" +
		"	Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 22 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"23. ERROR in test\\Test.java (at line 139)\n" +
		"	Object m1(C<Object, ? extends Number> p1, Object p2) { // warning 23 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m1(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"24. ERROR in test\\Test.java (at line 140)\n" +
		"	f1 = null; // warning 24\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"25. ERROR in test\\Test.java (at line 141)\n" +
		"	f2 = null; // warning 25\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull C<@NonNull Object,? extends @NonNull Number>\' but the provided value is null\n" +
		"----------\n" +
		"26. ERROR in test\\Test.java (at line 147)\n" +
		"	Object m2(C<Object, ? extends Number> p1, Object p2) { // warning 26 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m2(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n" +
		"27. ERROR in test\\Test.java (at line 153)\n" +
		"	Object m3(C<Object, ? extends Number> p1, Object p2) { // warning 27 on return type\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with \'@NonNull Object\' returned from X.m3(C<Object,? extends Number>, Object) (mismatching null constraints)\n" +
		"----------\n"
	);
}
public void testBug530971_redundant() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NNBDField,annotation.NNBDParam,annotation.NNBDReturn,annotation.NNBDTypeArg,annotation.NNBDTypeBound");
	customOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NNBDField.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			" \n" +
			"public @interface NNBDField {\n" +
			"	DefaultLocation[] value() default { FIELD };\n" +
			"}\n" +
			"",
			"annotation/NNBDParam.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"public @interface NNBDParam {\n" +
			"    DefaultLocation[] value() default { PARAMETER };\n" +
			"}\n" +
			"",
			"annotation/NNBDReturn.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"\n" +
			"public @interface NNBDReturn {\n" +
			"    DefaultLocation[] value() default { RETURN_TYPE };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeArg.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"\n" +
			"public @interface NNBDTypeArg {\n" +
			"    DefaultLocation[] value() default { TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeBound.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			"\n" +
			"public @interface NNBDTypeBound {\n" +
			"    DefaultLocation[] value() default { TYPE_BOUND };\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"    DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	Runner runner = new Runner();
	runner.shouldFlushOutputDirectory = false;
	runner.testFiles =
		new String[] {
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import annotation.*;\n" +
			"\n" +
			"@NNBDReturn\n" +
			"@NNBDParam\n" +
			"@NNBDField\n" +
			"abstract class X {\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NNBDField // warning 1\n" +
			"    abstract class OnClass {\n" +
			"    }\n" +
			"\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NNBDField // warning 2\n" +
			"    Object onField = \"\";\n" +
			"\n" +
			"    {\n" +
			"        @NNBDReturn\n" +
			"        @NNBDParam\n" +
			"        @NNBDField // warning 3\n" +
			"        Object onLocal;\n" +
			"    }\n" +
			"\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NNBDField // warning 4\n" +
			"    abstract void onMethod();\n" +
			"\n" +
			"    abstract void m(//\n" +
			"            @NNBDReturn //\n" +
			"            @NNBDParam //\n" +
			"            @NNBDField // warning 5\n" +
			"            Object onParameter);\n" +
			"}\n" +
			"",
		};
	runner.customOptions = customOptions;
	runner.expectedCompilerLog =
		"----------\n" +
		"1. WARNING in test\\X.java (at line 11)\n" +
		"	@NNBDField // warning 1\n" +
		"	^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type X\n" +
		"----------\n" +
		"2. WARNING in test\\X.java (at line 17)\n" +
		"	@NNBDField // warning 2\n" +
		"	^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type X\n" +
		"----------\n" +
		"3. WARNING in test\\X.java (at line 23)\n" +
		"	@NNBDField // warning 3\n" +
		"	^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type X\n" +
		"----------\n" +
		"4. WARNING in test\\X.java (at line 29)\n" +
		"	@NNBDField // warning 4\n" +
		"	^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type X\n" +
		"----------\n" +
		"5. WARNING in test\\X.java (at line 35)\n" +
		"	@NNBDField // warning 5\n" +
		"	^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type X\n" +
		"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
public void testBug530971_locally_redundant() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NNBDField,annotation.NNBDParam,annotation.NNBDReturn,annotation.NNBDTypeArg,annotation.NNBDTypeBound");
	customOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS\n" +
			"}\n" +
			"",
			"annotation/NNBDField.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			" \n" +
			"public @interface NNBDField {\n" +
			"	DefaultLocation[] value() default { FIELD };\n" +
			"}\n" +
			"",
			"annotation/NNBDParam.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"\n" +
			"public @interface NNBDParam {\n" +
			"    DefaultLocation[] value() default { PARAMETER };\n" +
			"}\n" +
			"",
			"annotation/NNBDReturn.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"\n" +
			"public @interface NNBDReturn {\n" +
			"    DefaultLocation[] value() default { RETURN_TYPE };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeArg.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"\n" +
			"public @interface NNBDTypeArg {\n" +
			"    DefaultLocation[] value() default { TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/NNBDTypeBound.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			"\n" +
			"public @interface NNBDTypeBound {\n" +
			"    DefaultLocation[] value() default { TYPE_BOUND };\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.FIELD;\n" +
			"import static annotation.DefaultLocation.PARAMETER;\n" +
			"import static annotation.DefaultLocation.RETURN_TYPE;\n" +
			"import static annotation.DefaultLocation.TYPE_ARGUMENT;\n" +
			"import static annotation.DefaultLocation.TYPE_BOUND;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"    DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD, TYPE_BOUND, TYPE_ARGUMENT };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runConformTestWithLibs(
		false,
		new String[] {
			"testredundant/TestRedundantOnSame.java",
			"package testredundant;\n" +
			"\n" +
			"import annotation.DefaultLocation;\n" +
			"import annotation.NNBDField;\n" +
			"import annotation.NNBDParam;\n" +
			"import annotation.NNBDReturn;\n" +
			"import annotation.NonNullByDefault;\n" +
			"\n" +
			"@NNBDReturn\n" +
			"@NNBDParam\n" +
			"@NNBDField\n" +
			"@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER, DefaultLocation.FIELD })\n" +
			"abstract class TestRedundantOnSame {\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })\n" +
			"    abstract class OnClass {\n" +
			"    }\n" +
			"\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })\n" +
			"    Object onField = \"\";\n" +
			"\n" +
			"    {\n" +
			"        @NNBDReturn\n" +
			"        @NNBDParam\n" +
			"        @NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })\n" +
			"        Object onLocal;\n" +
			"    }\n" +
			"\n" +
			"    @NNBDReturn\n" +
			"    @NNBDParam\n" +
			"    @NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })\n" +
			"    abstract void onMethod();\n" +
			"\n" +
			"    abstract void m(//\n" +
			"            @NNBDReturn //\n" +
			"            @NNBDParam //\n" +
			"            @NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER }) //\n" +
			"            Object onParameter);\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
}
public void testBug518839() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullApi");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NonNullFields");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);

	runConformTestWithLibs(
		new String[] {
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullApi.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"@TypeQualifierDefault({ElementType.METHOD, ElementType.PARAMETER})\n" +
			"public @interface NonNullApi {\n" +
			"}\n" +
			"",
			"annotation/NonNullFields.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"@TypeQualifierDefault(ElementType.FIELD)\n" +
			"public @interface NonNullFields {\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
			"annotation/TypeQualifierDefault.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"public @interface TypeQualifierDefault {\n" +
			"    ElementType[] value();\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"nn_api/NNApi.java",
			"package nn_api;\n" +
			"\n" +
			"public class NNApi {\n" +
			"    public String f;\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) { // warning\n" +
			"            //\n" +
			"        }\n" +
			"        return null; // warning\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_api/package-info.java",
			"@annotation.NonNullApi\n" +
			"package nn_api;\n" +
			"",
			"nn_api_and_fields/NNApiAndFields.java",
			"package nn_api_and_fields;\n" +
			"\n" +
			"public class NNApiAndFields {\n" +
			"    public String f; // warning 1\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) { // warning 2\n" +
			"            //\n" +
			"        }\n" +
			"        return null; // warning 3\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_api_and_fields/package-info.java",
			"@annotation.NonNullApi\n" +
			"@annotation.NonNullFields\n" +
			"package nn_api_and_fields;\n" +
			"",
			"nn_fields/NNFields.java",
			"package nn_fields;\n" +
			"\n" +
			"public class NNFields {\n" +
			"    public String f; // warning\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) {\n" +
			"            //\n" +
			"        }\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_fields/package-info.java",
			"@annotation.NonNullFields\n" +
			"package nn_fields;\n" +
			"",
		},
		customOptions,
		"----------\n" +
		"1. ERROR in nn_api\\NNApi.java (at line 7)\n" +
		"	if (p != null) { // warning\n" +
		"	    ^\n" +
		"Redundant null check: The variable p is specified as @NonNull\n" +
		"----------\n" +
		"2. ERROR in nn_api\\NNApi.java (at line 10)\n" +
		"	return null; // warning\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in nn_api_and_fields\\NNApiAndFields.java (at line 4)\n" +
		"	public String f; // warning 1\n" +
		"	              ^\n" +
		"The @NonNull field f may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in nn_api_and_fields\\NNApiAndFields.java (at line 7)\n" +
		"	if (p != null) { // warning 2\n" +
		"	    ^\n" +
		"Redundant null check: The variable p is specified as @NonNull\n" +
		"----------\n" +
		"3. ERROR in nn_api_and_fields\\NNApiAndFields.java (at line 10)\n" +
		"	return null; // warning 3\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in nn_fields\\NNFields.java (at line 4)\n" +
		"	public String f; // warning\n" +
		"	              ^\n" +
		"The @NonNull field f may not have been initialized\n" +
		"----------\n"
	);
}
public void testBug518839_BTB() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullApi");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "annotation.NonNullFields");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);

	runConformTestWithLibs(
		new String[] {
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullApi.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"@TypeQualifierDefault({ElementType.METHOD, ElementType.PARAMETER})\n" +
			"public @interface NonNullApi {\n" +
			"}\n" +
			"",
			"annotation/NonNullFields.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"@TypeQualifierDefault(ElementType.FIELD)\n" +
			"public @interface NonNullFields {\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
			"annotation/TypeQualifierDefault.java",
			"package annotation;\n" +
			"\n" +
			"import java.lang.annotation.ElementType;\n" +
			"\n" +
			"public @interface TypeQualifierDefault {\n" +
			"    ElementType[] value();\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	// compile with jdt annotations, so no warnings are created.
	runConformTestWithLibs(
		false,
		new String[] {
			"nn_api/NNApi.java",
			"package nn_api;\n" +
			"\n" +
			"public class NNApi {\n" +
			"    public String f;\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) { // warning\n" +
			"            //\n" +
			"        }\n" +
			"        return null; // warning\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_api/package-info.java",
			"@annotation.NonNullApi\n" +
			"package nn_api;\n" +
			"",
			"nn_api_and_fields/NNApiAndFields.java",
			"package nn_api_and_fields;\n" +
			"\n" +
			"public class NNApiAndFields {\n" +
			"    public String f; // warning 1\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) { // warning 2\n" +
			"            //\n" +
			"        }\n" +
			"        return null; // warning 3\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_api_and_fields/package-info.java",
			"@annotation.NonNullApi\n" +
			"@annotation.NonNullFields\n" +
			"package nn_api_and_fields;\n" +
			"",
			"nn_fields/NNFields.java",
			"package nn_fields;\n" +
			"\n" +
			"public class NNFields {\n" +
			"    public String f; // warning\n" +
			"\n" +
			"    public Object m(Object p) {\n" +
			"        if (p != null) {\n" +
			"            //\n" +
			"        }\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"",
			"nn_fields/package-info.java",
			"@annotation.NonNullFields\n" +
			"package nn_fields;\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"btbtest/BTBTest.java",
			"package btbtest;\n" +
			"\n" +
			"import nn_api.NNApi;\n" +
			"import nn_api_and_fields.NNApiAndFields;\n" +
			"import nn_fields.NNFields;\n" +
			"\n" +
			"public class BTBTest {\n" +
			"    void api(NNApi p) {\n" +
			"        if (p.m(null) == null) { // 2 warnings\n" +
			"        }\n" +
			"        p.f = null;\n" +
			"    }\n" +
			"\n" +
			"    void apiAndFields(NNApiAndFields p) {\n" +
			"        if (p.m(null) == null) { // 2 warnings\n" +
			"        }\n" +
			"        p.f = null; // warning\n" +
			"    }\n" +
			"\n" +
			"    void fields(NNFields p) {\n" +
			"        if (p.m(null) == null) {\n" +
			"        }\n" +
			"        p.f = null; // warning\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		"----------\n" +
		"1. ERROR in btbtest\\BTBTest.java (at line 9)\n" +
		"	if (p.m(null) == null) { // 2 warnings\n" +
		"	    ^^^^^^^^^\n" +
		"Null comparison always yields false: The method m(Object) cannot return null\n" +
		"----------\n" +
		"2. ERROR in btbtest\\BTBTest.java (at line 9)\n" +
		"	if (p.m(null) == null) { // 2 warnings\n" +
		"	        ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n" +
		"3. ERROR in btbtest\\BTBTest.java (at line 15)\n" +
		"	if (p.m(null) == null) { // 2 warnings\n" +
		"	    ^^^^^^^^^\n" +
		"Null comparison always yields false: The method m(Object) cannot return null\n" +
		"----------\n" +
		"4. ERROR in btbtest\\BTBTest.java (at line 15)\n" +
		"	if (p.m(null) == null) { // 2 warnings\n" +
		"	        ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n" +
		"5. ERROR in btbtest\\BTBTest.java (at line 17)\n" +
		"	p.f = null; // warning\n" +
		"	      ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"6. ERROR in btbtest\\BTBTest.java (at line 23)\n" +
		"	p.f = null; // warning\n" +
		"	      ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n"
	);
}
public void testBug531040() {
	if (this.complianceLevel < ClassFileConstants.JDK10)
		return;
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class Test {\n" +
			"	void test() {\n" +
			"		var list1 = new ArrayList<@NonNull String>();\n" +
			"		list1.add(null);\n" +
			"		@NonNull String val = \"\";\n" +
			"		var list2 = getList(val);\n" +
			"		list2.add(null);\n" +
			"	}\n" +
			"	<T> List<T> getList(T... in) {\n" +
			"		return Arrays.asList(in);\n" +
			"	}\n" +
			"}\n" +
			""
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 8)\n" +
		"	list1.add(null);\n" +
		"	          ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 11)\n" +
		"	list2.add(null);\n" +
		"	          ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"3. WARNING in Test.java (at line 13)\n" +
		"	<T> List<T> getList(T... in) {\n" +
		"	                         ^^\n" +
		"Type safety: Potential heap pollution via varargs parameter in\n" +
		"----------\n"
	);
}
public void testBug533339() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class Test {\n" +
			"\n" +
			"	interface Foo {\n" +
			"\n" +
			"		@Nullable\n" +
			"		String getString();\n" +
			"	}\n" +
			"\n" +
			"	class Bar {\n" +
			"\n" +
			"		Bar(@NonNull String s) {\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	Bar hasWarning(Foo foo) {\n" +
			"		@NonNull String s = checkNotNull(foo.getString());\n" +
			"		return new Bar(s);// Null type mismatch: required '@NonNull String' but the provided value is inferred as @Nullable\n" +
			"	}\n" +
			"\n" +
			"	Bar hasNoWarning(Foo foo) {\n" +
			"		return new Bar(checkNotNull(foo.getString()));// no warning when s is inlined\n" +
			"	}\n" +
			"	static <T> T checkNotNull(T reference) {\n" +
			"		if (reference == null) throw new NullPointerException();\n" +
			"		return reference;\n" +
			"	}\n" +
			"}\n"
		};
	runner.classLibraries = this.LIBS;
	runner.expectedCompilerLog =
		"----------\n" +
		"1. WARNING in Test.java (at line 19)\n" +
		"	@NonNull String s = checkNotNull(foo.getString());\n" +
		"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'\n" +
		"----------\n" +
		"2. WARNING in Test.java (at line 24)\n" +
		"	return new Bar(checkNotNull(foo.getString()));// no warning when s is inlined\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'\n" +
		"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
public void testBug534516() {
	runConformTestWithLibs(
			new String[] {
				"testbug/nullannotations/Utility.java",
				"package testbug.nullannotations;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"public class Utility {\n" +
				"\n" +
				"	public static String massageString(final String input) {\n" +
				"		return input + \" .\";\n" +
				"	}\n" +
				"\n" +
				"	private Utility() {\n" +
				"\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
	runConformTestWithLibs(
			false,
			new String[] {
				"testbug/nullannotations/ApplyIfNonNullElseGetBugDemo.java",
				"package testbug.nullannotations;\n" +
				"\n" +
				"import java.util.function.Function;\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"public class ApplyIfNonNullElseGetBugDemo {\n" +
				"\n" +
				"	public static <T, U> U applyIfNonNullElse(@Nullable T value, @NonNull Function<@NonNull ? super T, ? extends U> function, U fallbackValue) {\n" +
				"		if (value != null)\n" +
				"			return function.apply(value);\n" +
				"		return fallbackValue;\n" +
				"	}\n" +
				"\n" +
				"	public static void main(final @Nullable String[] args) {\n" +
				"		final @Nullable String arg = args.length == 0 ? null : args[0];\n" +
				"		System.out.println(applyIfNonNullElse(arg, Utility::massageString, \"\")); // incorrect warning here\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"",
			},
			getCompilerOptions(),
			""
		);
}
public void testBug536459() {
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.Nullable;\n" +
				"\n" +
				"public class X {\n" +
				"    static void x() {\n" +
				"        @Nullable String x1 = \"\";\n" +
				"        @Nullable String[] x2 = { \"\" };\n" +
				"    }\n" +
				"}\n"
			},
			getCompilerOptions(),
			"");
}
public void testBug536555() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
	runner.testFiles =
			new String[] {
				"Foo.java",
				"public class Foo\n" +
				"{\n" +
				"	/** Test {@link #foo(boolean)}. */\n" +
				"	public static final String TEST = \"foo\";\n" +
				"\n" +
				"	public void foo(@SuppressWarnings(TEST) final boolean test)\n" +
				"	{\n" +
				"		System.out.println(test);\n" +
				"	}\n" +
				"}\n"
			};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in Foo.java (at line 6)\n" +
			"	public void foo(@SuppressWarnings(TEST) final boolean test)\n" +
			"	                                  ^^^^\n" +
			"Unsupported @SuppressWarnings(\"foo\")\n" +
			"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
public void testBug540264() {
	runNegativeTest(
		true,
		new String[] {
			"example/Example.java",
			"package example;\n" +
			"\n" +
			"public abstract class Example {\n" +
			"    void f() {\n" +
			"        for (X.Y<Z> entry : x) {\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		this.LIBS,
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in example\\Example.java (at line 5)\n" +
		"	for (X.Y<Z> entry : x) {\n" +
		"	     ^\n" +
		"X cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in example\\Example.java (at line 5)\n" +
		"	for (X.Y<Z> entry : x) {\n" +
		"	         ^\n" +
		"Z cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in example\\Example.java (at line 5)\n" +
		"	for (X.Y<Z> entry : x) {\n" +
		"	                    ^\n" +
		"x cannot be resolved to a variable\n" +
		"----------\n",
		JavacTestOptions.DEFAULT
	);
}
public void testBug542707_1() {
	if (!checkPreviewAllowed()) return; // switch expression
	// switch expression has a functional type with interesting type inference and various null issues:
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	runner.customOptions.put(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles = new String[] {
		"X.java",
		"import org.eclipse.jdt.annotation.*;\n" +
		"import java.util.function.*;\n" +
		"interface IN0 {}\n" +
		"interface IN1 extends IN0 {}\n" +
		"interface IN2 extends IN0 {}\n" +
		"public class X {\n" +
		"	@NonNull IN1 n1() { return new IN1() {}; }\n" +
		"	IN2 n2() { return null; }\n" +
		"	<M> void m(@NonNull Supplier<@NonNull M> m2) { }\n" +
		"	void testSw(int i) {\n" +
		"		m(switch(i) {\n" +
		"			case 1 -> this::n1;\n" +
		"			case 2 -> () -> n1();\n" +
		"			case 3 -> null;\n" +
		"			case 4 -> () -> n2();\n" +
		"			default -> this::n2; });\n" +
		"	}\n" +
		"}\n"
	};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 14)\n" +
			"	case 3 -> null;\n" +
			"	          ^^^^\n" +
			"Null type mismatch: required \'@NonNull Supplier<@NonNull IN0>\' but the provided value is null\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 15)\n" +
			"	case 4 -> () -> n2();\n" +
			"	                ^^^^\n" +
			"Null type safety (type annotations): The expression of type \'IN2\' needs unchecked conversion to conform to \'@NonNull IN0\'\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 16)\n" +
			"	default -> this::n2; });\n" +
			"	           ^^^^^^^^\n" +
			"Null type safety at method return type: Method descriptor Supplier<IN0>.get() promises \'@NonNull IN0\' but referenced method provides \'IN2\'\n" +
			"----------\n";
	runner.runNegativeTest();
}
public void testBug499714() {
	runNegativeTestWithLibs(
		new String[] {
			"Type.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"interface Type<@Nullable K> {\n" +
			"    K get();\n" +
			"\n" +
			"    static <@Nullable T> void x(Type<T> t) {\n" +
			"        t.get().toString();\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in Type.java (at line 7)\n" +
		"	t.get().toString();\n" +
		"	^^^^^^^\n" +
		"Potential null pointer access: The method get() may return null\n" +
		"----------\n");
}
public void testBug482242_simple() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Test {\n" +
			"    static void dangerous(List<String> list) {\n" +
			"        list.add(null);\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        List<@NonNull String> l = new ArrayList<>();\n" +
			"        dangerous(l);\n" +
			"        for (String string : l)\n" +
			"            System.out.println(string.toLowerCase());\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. ERROR in Test.java (at line 9)\n" +
		"	dangerous(l);\n" +
		"	          ^\n" +
		"Unsafe null type conversion (type annotations): The value of type \'List<@NonNull String>\' is made accessible using the less-annotated type \'List<String>\'\n" +
		"----------\n");
}
public void testBug482242_intermediate() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public class Test {\n" +
			"    public static void main(String[] args) {\n" +
			"        ArrayList<@NonNull String> list = new ArrayList<>();\n" +
			"        collect(list, null);\n" +
			"        for (String s : list)\n" +
			"            System.out.println(s.toUpperCase());\n" +
			"    }\n" +
			"    static void collect(List<@NonNull String> list, String string) {\n" +
			"        list.add(string);     // (1)\n" +
			"        insert(list, string); // (2)\n" +
			"    }\n" +
			"    static void insert(List<? super String> l, String s) {\n" +
			"        l.add(s);\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. WARNING in Test.java (at line 12)\n" +
		"	list.add(string);     // (1)\n" +
		"	         ^^^^^^\n" +
		"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 13)\n" +
		"	insert(list, string); // (2)\n" +
		"	       ^^^^\n" +
		"Unsafe null type conversion (type annotations): The value of type \'List<@NonNull String>\' is made accessible using the less-annotated type \'List<? super String>\'\n" +
		"----------\n");
}
public void testBug482242_annotatedTypeVariable() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"interface List<T extends @NonNull Object> {\n" +
			"	void add(T elem);" +
			"}\n" +
			"public class Test {\n" +
			"    public static void test(List<@NonNull String> list) {\n" +
			"        collect(list, null);\n" +
			"    }\n" +
			"    static void collect(List<@NonNull String> list, String string) {\n" +
			"        insert(list, string);\n" +
			"    }\n" +
			"    static void insert(List<? super String> l, String s) {\n" + // type error at declaration site, no need to signal at the call site
			"        l.add(s);\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. ERROR in Test.java (at line 12)\n" +
		"	static void insert(List<? super String> l, String s) {\n" +
		"	                        ^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'? super String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'\n" +
		"----------\n" +
		"2. WARNING in Test.java (at line 13)\n" +
		"	l.add(s);\n" +
		"	      ^\n" +
		"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'capture#of ? super String\'\n" +
		"----------\n");
}
public void testBug482242_boundedWildcard() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_ANNOTATED_TYPE_ARGUMENT_TO_UNANNOTATED, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"interface List<T extends @NonNull Object> {\n" +
			"	void add(T elem);" +
			"}\n" +
			"public class Test {\n" +
			"    public static void test(List<@NonNull String> list) {\n" +
			"        collect(list, null);\n" +
			"    }\n" +
			"    static void collect(List<@NonNull String> list, String string) {\n" +
			"        insert(list, string);\n" +
			"    }\n" +
			"    static void insert(List<? super @Nullable String> l, String s) {\n" +
			"        l.add(s);\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. ERROR in Test.java (at line 10)\n" +
		"	insert(list, string);\n" +
		"	       ^^^^\n" +
		"Null type mismatch (type annotations): required \'List<? super @Nullable String>\' but this expression has type \'List<@NonNull String>\'\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 12)\n" +
		"	static void insert(List<? super @Nullable String> l, String s) {\n" +
		"	                        ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null constraint mismatch: The type \'? super @Nullable String\' is not a valid substitute for the type parameter \'T extends @NonNull Object\'\n" +
		"----------\n" +
		"3. WARNING in Test.java (at line 13)\n" +
		"	l.add(s);\n" +
		"	      ^\n" +
		"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'capture#of ? super @Nullable String\'\n" +
		"----------\n");
}
public void testBug560213source() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.testFiles = new String[] {
		"nullEnumSort/MyEnum.java",
		"package nullEnumSort;\n" +
		"\n" +
		"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
		"\n" +
		"@NonNullByDefault\n" +
		"enum MyEnum {\n" +
		"    x\n" +
		"}\n",
		"nullEnumSort/EnumProblem.java",
		"package nullEnumSort;\n" +
		"\n" +
		"import java.util.Collections;\n" +
		"import java.util.List;\n" +
		"\n" +
		"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
		"\n" +
		"@NonNullByDefault\n" +
		"public class EnumProblem {\n" +
		"    void f(List<MyEnum> list) {\n" +
		"        Collections.sort(list);\n" +
		"    }\n" +
		"\n}"
	};
	runner.runConformTest();
}
public void testBug560213binary() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles = new String[] {
		"nullEnumSort/MyEnum.java",
		"package nullEnumSort;\n" +
		"\n" +
		"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
		"\n" +
		"@NonNullByDefault\n" +
		"enum MyEnum {\n" +
		"    x\n" +
		"}\n"
	};
	runner.classLibraries = this.LIBS;
	runner.runConformTest();

	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
		"nullEnumSort/EnumProblem.java",
		"package nullEnumSort;\n" +
		"\n" +
		"import java.util.Collections;\n" +
		"import java.util.List;\n" +
		"\n" +
		"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
		"\n" +
		"@NonNullByDefault\n" +
		"public class EnumProblem {\n" +
		"    void f(List<MyEnum> list) {\n" +
		"        Collections.sort(list);\n" +
		"    }\n" +
		"\n}"
	};
	runner.runConformTest();
}
public void testBug560310() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles =
		new String[] {
			"confusing/Confusing.java",
			"package confusing;\n" +
			"\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"public abstract class Confusing {\n" +
			"    abstract int unannotated(ArrayList<String> list);\n" +
			"\n" +
			"    @NonNullByDefault\n" +
			"    public void f(boolean x) {\n" +
			"        ArrayList<String> list = x ? null : new ArrayList<>();\n" +
			"\n" +
			"        while (true) {\n" +
			"            unannotated(list);\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		};
	runner.classLibraries = this.LIBS;
	runner.javacTestOptions = Excuse.EclipseHasSomeMoreWarnings;
	runner.expectedCompilerLog =
		"----------\n" +
		"1. INFO in confusing\\Confusing.java (at line 15)\n" +
		"	unannotated(list);\n" +
		"	            ^^^^\n" +
		"Unsafe null type conversion (type annotations): The value of type \'ArrayList<@NonNull String>\' is made accessible using the less-annotated type \'ArrayList<String>\'\n" +
		"----------\n";
	runner.runWarningTest();
}
public void testBug560310try_finally() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.testFiles =
		new String[] {
			"confusing/Confusing.java",
			"package confusing;\n" +
			"\n" +
			"import java.util.ArrayList;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"public abstract class Confusing {\n" +
			"    abstract int unannotated(ArrayList<String> list);\n" +
			"\n" +
			"    @NonNullByDefault\n" +
			"    public void f(boolean x) {\n" +
			"        ArrayList<String> list = x ? null : new ArrayList<>();\n" +
			"\n" +
			"        try {\n" +
			"            unannotated(list);\n" +
			"        } finally {\n" +
			"            unannotated(list);\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		};
	runner.classLibraries = this.LIBS;
	runner.javacTestOptions = Excuse.EclipseHasSomeMoreWarnings;
	runner.expectedCompilerLog =
		"----------\n" +
		"1. INFO in confusing\\Confusing.java (at line 15)\n" +
		"	unannotated(list);\n" +
		"	            ^^^^\n" +
		"Unsafe null type conversion (type annotations): The value of type \'ArrayList<@NonNull String>\' is made accessible using the less-annotated type \'ArrayList<String>\'\n" +
		"----------\n" +
		"2. INFO in confusing\\Confusing.java (at line 17)\n" +
		"	unannotated(list);\n" +
		"	            ^^^^\n" +
		"Unsafe null type conversion (type annotations): The value of type \'ArrayList<@NonNull String>\' is made accessible using the less-annotated type \'ArrayList<String>\'\n" +
		"----------\n";
	runner.runWarningTest();
}
public void testBug562347_561280c9() {
	runNegativeTestWithLibs(
		new String[] {
			"Example.java",
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Example {\n" +
			"    static <X> X f(X x) {\n" +
			"        return x;\n" +
			"    }\n" +
			"\n" +
			"    public void g() {\n" +
			"        Object x0, x1, x2, x3, x4, x5, x6, x7, x8, x9;\n" +
			"        Object x10, x11, x12, x13, x14, x15, x16, x17, x18, x19;\n" +
			"        Object x20, x21, x22, x23, x24, x25, x26, x27, x28, x29;\n" +
			"        Object x30, x31, x32, x33, x34, x35, x36, x37, x38, x39;\n" +
			"        Object x40, x41, x42, x43, x44, x45, x46, x47, x48, x49;\n" +
			"        Object x50, x51, x52, x53, x54, x55, x56, x57, x58, x59;\n" +
			"        Object x60;\n" +
			"        Object x61;\n" +
			"        for (Map.Entry<String, String> entry : new HashMap<String, String>().entrySet()) {\n" +
			"            if (f(entry.getKey()) != null) {\n" +
			"                continue;\n" +
			"            }\n" +
			"            String x = \"asdf\";\n" +
			"            x.hashCode();\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Example.java (at line 22)\n" +
		"	if (f(entry.getKey()) != null) {\n" +
		"	    ^^^^^^^^^^^^^^^^^\n" +
		"Redundant null check: comparing \'@NonNull String\' against null\n" +
		"----------\n" +
		"2. WARNING in Example.java (at line 25)\n" +
		"	String x = \"asdf\";\n" +
		"	^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n");
}
public void testBug562347() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"NotificationListingHolder.java",
			"@SuppressWarnings(\"unused\")\n" +
			"public final class NotificationListingHolder {\n" +
			"    private String f1,f2,f3,f4;\n" +
			"\n" +
			"    private void setupActionButtons() {\n" +
			"        Test listItemNotificationsBinding2;\n" +
			"        boolean z;\n" +
			"        String a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a11,a12,a13,a14,a15,a16,a17,a18,a19,a20,a21,a22,a23,a24,a25,a26,a27,a28,a29,a30,a31,a32,a33,a34,a35,a36,a37,a38,a39,a40,a41,a42,a43,a44,a45,a46,a47,a48,a49,a50,a51,a52,a53,a54,a55,a56,a57,a58;\n" +
			"        if (z) {\n" +
			"            String button4 = listItemNotificationsBinding2.field;\n" +
			"            if (listItemNotificationsBinding2 != null) {\n" +
			"                return;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"class Test {\n" +
			"    public final String field;\n" +
			"}"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in NotificationListingHolder.java (at line 9)\n" +
		"	if (z) {\n" +
		"	    ^\n" +
		"The local variable z may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in NotificationListingHolder.java (at line 10)\n" +
		"	String button4 = listItemNotificationsBinding2.field;\n" +
		"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The local variable listItemNotificationsBinding2 may not have been initialized\n" +
		"----------\n" +
		"3. ERROR in NotificationListingHolder.java (at line 11)\n" +
		"	if (listItemNotificationsBinding2 != null) {\n" +
		"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The local variable listItemNotificationsBinding2 may not have been initialized\n" +
		"----------\n" +
		"4. ERROR in NotificationListingHolder.java (at line 11)\n" +
		"	if (listItemNotificationsBinding2 != null) {\n" +
		"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Redundant null check: The variable listItemNotificationsBinding2 cannot be null at this location\n" +
		"----------\n" +
		"5. ERROR in NotificationListingHolder.java (at line 19)\n" +
		"	public final String field;\n" +
		"	                    ^^^^^\n" +
		"The blank final field field may not have been initialized\n" +
		"----------\n";
	runner.classLibraries = this.LIBS;
	runner.runNegativeTest();
}
public void testBug578300() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"A.java",
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"public class A {\n" +
			"\n" +
			"	@Nullable\n" +
			"	public A next;\n" +
			"\n" +
			"	@Nullable\n" +
			"	public A previous;\n" +
			"\n" +
			"	public void disconnectOK() {\n" +
			"		this.next.previous = null; // Potential null pointer access: this expression has a '@Nullable' type\n" +
			"		this.next = null;\n" +
			"	}\n" +
			"\n" +
			"	public void disconnectKO() {\n" +
			"		next.previous = null; // <-- Expected the same error here since we are accessing the same field\n" +
			"		next = null;\n" +
			"	}\n" +
			"}\n"
		};
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in A.java (at line 11)\n" +
			"	this.next.previous = null; // Potential null pointer access: this expression has a \'@Nullable\' type\n" +
			"	     ^^^^\n" +
			"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 16)\n" +
			"	next.previous = null; // <-- Expected the same error here since we are accessing the same field\n" +
			"	^^^^\n" +
			"Potential null pointer access: this expression has a \'@Nullable\' type\n" +
			"----------\n";
	runner.runNegativeTest();
}

public void testRequireNonNull() {
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	@Nullable Object o;\n" +
			"	@NonNull X foo(X x) {\n" +
			"		return java.util.Objects.requireNonNull(x);\n" + // was: Unsafe interpretation of method return type as '@NonNull' based on substitution 'T=@NonNull X'. Declaring type 'Objects' doesn't seem to be designed with null type annotations in mind
			"	}\n" +
			"	public static void main(String... args) {\n" +
			"		try {\n" +
			"			new X().foo(null);\n" +
			"		} catch (NullPointerException e) {\n" +
			"			System.out.print(\"caught\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		};
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_NONNULL_TYPEVAR_FROM_LEGACY_INVOCATION, JavaCore.ERROR);
	runner.classLibraries = this.LIBS;
	runner.expectedCompilerLog = "";
	runner.expectedOutputString = "caught";
	runner.expectedErrorString = "";
	runner.runConformTest();
}

public void testBug522142_redundant1() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Foo.java",
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;" +
			"import org.eclipse.jdt.annotation.*;" +
			"@NonNullByDefault({PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT})\n" +
			"interface Foo<A> {\n" +
			"    interface Bar<@NonNull A> extends Iterable<Foo<A>> {\n" +
			"    }\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in Foo.java (at line 3)\n" +
			"	interface Bar<@NonNull A> extends Iterable<Foo<A>> {\n" +
			"	              ^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n";
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.runWarningTest();
}

public void testBug522142_redundant2() {
	// challenge ArrayQualifiedTypeReference:
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Foo.java",
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;" +
			"import org.eclipse.jdt.annotation.*;" +
			"@NonNullByDefault({PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT})\n" +
			"class Foo {\n" +
			"	java.util.List<java.lang.String @NonNull[]> f1 = new java.util.ArrayList<>();\n" +
			"	java.util.List<java.lang.String @NonNull[][]> f2 = new java.util.ArrayList<>();\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in Foo.java (at line 3)\n" +
			"	java.util.List<java.lang.String @NonNull[]> f1 = new java.util.ArrayList<>();\n" +
			"	                                ^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n" +
			"2. WARNING in Foo.java (at line 4)\n" +
			"	java.util.List<java.lang.String @NonNull[][]> f2 = new java.util.ArrayList<>();\n" +
			"	                                ^^^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n";
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.runWarningTest();
}

public void testBug522142_redundant3() {
	// challenge ArrayQualifiedTypeReference:
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Foo.java",
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;" +
			"import org.eclipse.jdt.annotation.*;" +
			"@NonNullByDefault({PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT})\n" +
			"class Foo {\n" +
			"	java.util.List<java.lang. @NonNull String> f = new java.util.ArrayList<>();\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in Foo.java (at line 3)\n" +
			"	java.util.List<java.lang. @NonNull String> f = new java.util.ArrayList<>();\n" +
			"	                          ^^^^^^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n";
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.runWarningTest();
}
public void testBug522142_bogusError() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Foo.java",
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;" +
			"import org.eclipse.jdt.annotation.*;" +
			"@NonNullByDefault({PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT})\n" +
			"interface Foo<A> {\n" +
			"    interface Bar<A> extends Iterable<Foo<A>> {\n" +
			"    }\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
			"";
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.runConformTest();
}
public void testBug499596() throws Exception {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Foo.java",
			"import static org.eclipse.jdt.annotation.DefaultLocation.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"\n" +
			"@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD, TYPE_PARAMETER, TYPE_BOUND, TYPE_ARGUMENT, ARRAY_CONTENTS })\n" +
			"abstract class Foo {\n" +
			"	abstract <T> Collection<T> singleton(T t);\n" +
			"	Collection<String[]> from0(@NonNull String @NonNull [] @NonNull... elements) { // <-- 3 warnings here, ok\n" +
			"		return singleton(elements[0]);\n" +
			"	}\n" +
			"	Collection<String[]> from1(String @NonNull [] @NonNull... elements) { // <-- 2 warnings here, ok\n" +
			"		return singleton(elements[0]);\n" +
			"	}\n" +
			"	Collection<String[]> from2(String [] @NonNull... elements) { // <-- 1 warning here, ok\n" +
			"		return singleton(elements[0]);\n" +
			"	}\n" +
			"	Collection<String[]> from3(String []... elements) {\n" +
			"		return singleton(elements[0]);\n" +
			"	}\n" +
			"	@NonNullByDefault({}) // cancel outer default\n" +
			"	Collection<@NonNull String @NonNull[]> from4(String []... elements) {\n" +
			"		return singleton(elements[0]); // <-- should warn\n" +
			"	}\n" +
			"}\n"
		};
	// Expectations:
	// from0 .. from3:
	// 		declarations should show the indicated number of warnings
	// 		statements are OK, since everything is covered by the outer @NNBD
	// from4 should flag the statement (only)
	runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in Foo.java (at line 8)\n" +
			"	Collection<String[]> from0(@NonNull String @NonNull [] @NonNull... elements) { // <-- 3 warnings here, ok\n" +
			"	                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n" +
			"2. WARNING in Foo.java (at line 8)\n" +
			"	Collection<String[]> from0(@NonNull String @NonNull [] @NonNull... elements) { // <-- 3 warnings here, ok\n" +
			"	                                           ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n" +
			"3. WARNING in Foo.java (at line 8)\n" +
			"	Collection<String[]> from0(@NonNull String @NonNull [] @NonNull... elements) { // <-- 3 warnings here, ok\n" +
			"	                                                       ^^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n" +
			"4. WARNING in Foo.java (at line 11)\n" +
			"	Collection<String[]> from1(String @NonNull [] @NonNull... elements) { // <-- 2 warnings here, ok\n" +
			"	                                  ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n" +
			"5. WARNING in Foo.java (at line 11)\n" +
			"	Collection<String[]> from1(String @NonNull [] @NonNull... elements) { // <-- 2 warnings here, ok\n" +
			"	                                              ^^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n" +
			"6. WARNING in Foo.java (at line 14)\n" +
			"	Collection<String[]> from2(String [] @NonNull... elements) { // <-- 1 warning here, ok\n" +
			"	                                     ^^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n" +
			"7. WARNING in Foo.java (at line 22)\n" +
			"	return singleton(elements[0]); // <-- should warn\n" +
			"	                 ^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type \'String[]\' needs unchecked conversion to conform to \'@NonNull String @NonNull[]\'\n" +
			"----------\n";
	runner.customOptions = getCompilerOptions();
	runner.classLibraries = this.LIBS;
	runner.runWarningTest();
}
public void testRedundantNonNull_field() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"test1/Foo.java",
			"package test1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Foo {\n" +
			"    @NonNull Object f=new Object();\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. WARNING in test1\\Foo.java (at line 5)\n" +
			"	@NonNull Object f=new Object();\n" +
			"	^^^^^^^^^^^^^^^\n" +
			"The nullness annotation is redundant with a default that applies to this location\n" +
			"----------\n";
	runner.classLibraries = this.LIBS;
	runner.runWarningTest();
}
public void testGH1007_srikanth() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"SubClass.java",
			"// ECJ error in next line: Type mismatch: cannot convert from Class<SubClass> to Class<? extends SuperClass>[]\n" +
			"@AnnotationWithArrayInitializer(annotationArgument = SubClass.class)\n" +
			"class AnnotatedClass2 extends AnnotatedSuperClass {}\n" +
			"\n" +
			"//ECJ error in next line: Type mismatch: cannot convert from Class<SubClass> to Class<? extends SuperClass>\n" +
			"@AnnotationWithArrayInitializer(annotationArgument = {SubClass.class})\n" +
			"class AnnotatedClass extends AnnotatedSuperClass {}\n" +
			"\n" +
			"\n" +
			"class AnnotatedSuperClass {}\n" +
			"\n" +
			"@interface AnnotationWithArrayInitializer {\n" +
			"    Class<? extends SuperClass>[] annotationArgument();\n" +
			"}\n" +
			"\n" +
			"class SubClass extends SuperClass {}\n" +
			"abstract class SuperClass {}"
		};
	runner.runConformTest();
}
public void testGH854() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Annot.java",
			"public @interface Annot {\n" +
			"    Class<? extends Init<? extends Configuration>>[] inits(); \n" +
			"}\n",
			"Configuration.java",
			"public interface Configuration {\n" +
			"}\n",
			"Init.java",
			"public interface Init<C extends Configuration> {\n" +
			"}\n",
			"App.java",
			"interface I<T> {}\n" +
			"class IImpl<T> implements I<String>, Init<Configuration> {}\n" +
			"@Annot(inits = {App.MyInit.class})\n" +
			"public class App {\n" +
			"	static class MyInit extends IImpl<Configuration> {}\n" +
			"}\n"
		};
	runner.runConformTest();
}
// duplicate of #1077
public void testGH476() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Controller.java",
			"public class Controller<T> {\n" +
			"    final static String ENDPOINT = \"controll\";\n" +
			"}\n",
			"RequestMapping.java",
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Retention;\n" +
			"import java.lang.annotation.RetentionPolicy;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target(ElementType.TYPE)\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"public @interface RequestMapping {\n" +
			"	String name() default \"\";\n" +
			"	String[] value() default {};\n" +
			"}\n",
			"CtlImpl.java",
			"@RequestMapping(CtlImpl.CTL_ENDPOINT)\n" +
			"public class CtlImpl extends Controller<String> {\n" +
			"    final static String CTL_ENDPOINT = ENDPOINT + \"/ctl\";\n" +
			"    static String value;\n" +
			"}\n"
		};
	runner.runConformTest();
}
public void testVSCodeIssue3076() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"demo/cache/AbstractCache.java",
			"package demo.cache;\n" +
			"\n" +
			"public abstract class AbstractCache {\n" +
			"    public enum Expiry {\n" +
			"        ONE, TWO, THREE\n" +
			"    }\n" +
			"\n" +
			"    protected abstract void cacheThis(int param1, Expiry param2);\n" +
			"}\n",
			"demo/Annot.java",
			"package demo;\n" +
			"public @interface Annot {\n" +
			"	String defaultProperty();\n" +
			"}\n",
			"demo/cache/MyCache.java",
			"package demo.cache;\n" +
			"\n" +
			"import demo.Annot;\n" +
			"\n" +
			"/**\n" +
			" * This annotation is what causes the confusion around the nested Expiry type.\n" +
			" *\n" +
			" * If you comment out this annotation the language server has no problem\n" +
			" * figuring it out.\n" +
			" *\n" +
			" * It can be *any* annotation.\n" +
			" * So it would seem that referring to your own class outside of the\n" +
			" * class definition is what triggers this particular bug.\n" +
			" */\n" +
			"@Annot(defaultProperty = MyCache.DEFAULT_PROPERTY_NAME)\n" +
			"public class MyCache extends AbstractCache {\n" +
			"    public static final String DEFAULT_PROPERTY_NAME = \"WHATEVER\";\n" +
			"\n" +
			"    @Override\n" +
			"    protected void cacheThis(int param1, Expiry param2) {\n" +
			"        throw new UnsupportedOperationException(\"Unimplemented method 'doSomethingElse'\");\n" +
			"    }\n" +
			"}\n"
		};
	runner.runConformTest();
}
public void testGH986() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"mypackage/Example.java",
			"package mypackage;\n" +
			"\n" +
			"import java.io.Serializable;\n" +
			"\n" +
			"@Deprecated(since = Example.SINCE)\n" +
			"public class Example<T> implements Serializable {\n" +
			"	\n" +
			"	static final String SINCE = \"...\";\n" +
			"\n" +
			"	private T target;\n" +
			"\n" +
			"}"
		};
	runner.runConformTest();
}
public void testGHjdtls2386() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"ConfigurableApplicationContext.java",
			"public class ConfigurableApplicationContext { }\n",
			"ApplicationContextInitializer.java",
			"public interface ApplicationContextInitializer<T> {\n" +
			"	void initialize(T context);\n" +
			"}\n",
			"ContextConfiguration.java",
			"""
			import static java.lang.annotation.ElementType.*;
			import static java.lang.annotation.RetentionPolicy.*;
			import java.lang.annotation.*;

			@Target(TYPE)
			@Retention(RUNTIME)
			public @interface ContextConfiguration {
				Class<? extends ApplicationContextInitializer<?>>[] initializers();
			}
			""",
			"AbstractTest.java",
			"""
			@ContextConfiguration(initializers = {AbstractTest.Initializer.class})
			public abstract class AbstractTest {

			  static class Initializer
			      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

			    @Override
			    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

			    }
			  }
			}
			"""
		};
	runner.runConformTest();
}
}
