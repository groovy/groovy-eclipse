/*******************************************************************************
 * Copyright (c) 2012, 2015 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     IBM Corporation
 *     Till Brychcy - Contribution for
 *								Bug 467032 - TYPE_USE Null Annotations: IllegalStateException with annotated arrays of Enum when accessed via BinaryTypeBinding
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NullTypeAnnotationTest extends AbstractNullAnnotationTest {

	public NullTypeAnnotationTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testBug456497" };
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
				"X.java",
				  "import org.eclipse.jdt.annotation.*;\n" +
				  "import java.util.List;\n" +
				  "public class X {\n" +
				  "    void foo(@Nullable List<@NonNull Object> l) {\n" +
				  "        System.out.print(l.get(0).toString()); // problem: l may be null\n" +
				  "        l.add(null); // problem: cannot insert 'null' into this list\n" +
				  "    }\n" +
				  "    void bar(@Nullable List<java.lang.@NonNull Object> l) {\n" +
				  "        System.out.print(l.get(0).toString()); // problem: l may be null\n" +
				  "        l.add(0, null); // problem: cannot insert 'null' into this list\n" +
				  "    }\n" +
				  "    void bar2(@Dummy java.util.@Nullable List<java.lang.@NonNull Object> l2) {\n" +
				  "        System.out.print(l2.get(0).toString()); // problem: l2 may be null\n" +
				  "        l2.add(0, null); // problem: cannot insert 'null' into this list\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	System.out.print(l.get(0).toString()); // problem: l may be null\n" + 
			"	                 ^\n" + 
			"Potential null pointer access: this expression has a '@Nullable' type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	l.add(null); // problem: cannot insert \'null\' into this list\n" + 
			"	      ^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	System.out.print(l.get(0).toString()); // problem: l may be null\n" + 
			"	                 ^\n" + 
			"Potential null pointer access: this expression has a '@Nullable' type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	l.add(0, null); // problem: cannot insert \'null\' into this list\n" + 
			"	         ^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 13)\n" + 
			"	System.out.print(l2.get(0).toString()); // problem: l2 may be null\n" + 
			"	                 ^^\n" + 
			"Potential null pointer access: this expression has a '@Nullable' type\n" + 
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
				  "    }\n" +
				  "    void bar(A<@Nullable Object>.I<@NonNull Object> i) {\n" + // legal instantiation
				  "        @NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in A.java (at line 10)\n" + 
			"	@NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts\n" + 
			"	                    ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required '@NonNull Object' but this expression has type '@Nullable Object'\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 10)\n" + 
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
			"Potential null pointer access: this expression has a '@Nullable' type\n" + 
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
		runNegativeTestWithLibs(
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
			"----------\n");
	}

	// bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
	// annotation on leaf type in 1-dim array
	public void testArrayType_01() {
		runNegativeTestWithLibs(
			new String[] {
				"Wrapper.java",
				  "public class Wrapper<T> {\n" +
				  "	T content;" +
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
			"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
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
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
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
    		"Potential null pointer access: this expression has a '@Nullable' type\n" + 
    		"----------\n" + 
    		"5. WARNING in A.java (at line 15)\n" + 
    		"	string = maybeStringArray[0]; // problems: indexing nullable array & unchecked conversion\n" + 
    		"	         ^^^^^^^^^^^^^^^^^^^\n" + 
    		"Null type safety (type annotations): The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'\n" + 
    		"----------\n" + 
    		"6. ERROR in A.java (at line 17)\n" + 
    		"	maybeStringArray[0] = null; 	 // problem: indexing nullable array\n" + 
    		"	^^^^^^^^^^^^^^^^\n" + 
    		"Potential null pointer access: this expression has a '@Nullable' type\n" + 
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
    		"Null type safety (type annotations): The expression of type 'String [] @NonNull[]' needs unchecked conversion to conform to \'@NonNull Object\'\n" + 
		    "----------\n" + 
			"2. WARNING in A.java (at line 7)\n" + 
    		"	array = maybeArrays; 	// problem: unchecked conversion\n" + 
    		"	        ^^^^^^^^^^^\n" + 
    		"Null type safety (type annotations): The expression of type 'String [] @Nullable[]' needs unchecked conversion to conform to \'@NonNull Object\'\n" + 
			"----------\n" + 
			"3. ERROR in A.java (at line 13)\n" + 
			"	realArrays[0] = null; 	// problem: cannot assign null to @NonNull array\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Null type mismatch: required \'String @NonNull[]\' but the provided value is null\n" + 
			"----------\n" + 
			"4. ERROR in A.java (at line 14)\n" +
			"	array = maybeArrays[0]; 	// problem: element can be null\n" +
			"	        ^^^^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required '@NonNull Object' but this expression has type 'String @Nullable[]'\n" + 
			"----------\n" + 
			"5. WARNING in A.java (at line 19)\n" +
			"	array = realArrays[0][0]; // problem: unchecked conversion\n" +
			"	        ^^^^^^^^^^^^^^^^\n" +
    		"Null type safety (type annotations): The expression of type 'String' needs unchecked conversion to conform to \'@NonNull Object\'\n" + 
			"----------\n" + 
			"6. ERROR in A.java (at line 21)\n" +
			"	array = maybeArrays[0][0]; // problems: indexing nullable array & unchecked conversion\n" +
			"	        ^^^^^^^^^^^^^^\n" +
    		"Potential null pointer access: array element may be null\n" + 
			"----------\n" + 
			"7. WARNING in A.java (at line 21)\n" +
			"	array = maybeArrays[0][0]; // problems: indexing nullable array & unchecked conversion\n" +
			"	        ^^^^^^^^^^^^^^^^^\n" +
			"Null type safety (type annotations): The expression of type 'String' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
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
			"	^^^^^^^^^^^^^\n" + 
			"Null type mismatch: required \'String @NonNull[]\' but the provided value is inferred as @Nullable\n" + 
			"----------\n" + 
			"2. WARNING in A.java (at line 5)\n" + 
			"	realArrays[0] = unknownArrays[0];	// problems: inner array is unspecified, outer can be null\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Null type safety (type annotations): The expression of type 'String[]' needs unchecked conversion to conform to \'String @NonNull[]\'\n" + 
			"----------\n" + 
			"3. ERROR in A.java (at line 5)\n" + 
			"	realArrays[0] = unknownArrays[0];	// problems: inner array is unspecified, outer can be null\n" + 
			"	                ^^^^^^^^^^^^^\n" + 
			"Potential null pointer access: this expression has a \'@Nullable\' type\n" + 
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
			getCompilerOptions(),
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
		runNegativeTestWithLibs(
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
			"----------\n");
	}

	// https://bugs.eclipse.org/403457 - [1.8][compiler] NPE in WildcardBinding.signature
	// variant with null annotations
	public void testBug403457_2() {
		runNegativeTestWithLibs(
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
			"----------\n");
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
				"Potential null pointer access: this expression has a '@Nullable' type\n" + 
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
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import java.util.ArrayList;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
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
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		runConformTestWithLibs(
				new String[] {
					"p/X1.java",
					"package p;\n" +
					"import java.util.ArrayList;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
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
					"		x.<Y1, @NonNull Object>foo(this, new Object());\n" + // incompatible: V is constrained to @Nullable via superclass
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
				"----------\n" + 
				"3. ERROR in Y1.java (at line 8)\n" + 
				"	x.<Y1, @NonNull Object>foo(this, new Object());\n" + 
				"	       ^^^^^^^^^^^^^^^\n" + 
				"Null constraint mismatch: The type \'@NonNull Object\' is not a valid substitute for the type parameter \'V extends @Nullable Object'\n" + 
				"----------\n"
				);
	}

	// storing and decoding null-type-annotations to/from classfile: CLASS_TYPE_PARAMETER_BOUND & METHOD_TYPE_PARAMETER_BOUND
	// variant: qualified type references
	public void testBinary06b() {
		Map customOptions = getCompilerOptions();
		customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		customOptions.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
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
					"		x.<Y1, java.lang.@NonNull Object>foo(this, new Object());\n" + // incompatible: V is constrained to @Nullable via superclass
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
				"----------\n" + 
				"3. ERROR in Y1.java (at line 6)\n" + 
				"	x.<Y1, java.lang.@NonNull Object>foo(this, new Object());\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Null constraint mismatch: The type \'@NonNull Object\' is not a valid substitute for the type parameter \'V extends @Nullable Object\'\n" + 
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
					"p/X1.java",
					"package p;\n" +
					"import java.util.*;\n" +
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
					"		x.<@NonNull Y1, @NonNull Object>foo(this, new Object())\n" + // @NonNull Object conflicts with "V extends @Nullable Object"
					"			.get(0).put(null, null);\n" + // second null is illegal
					"	}\n" +
					"}\n"
				}, 
				customOptions,
				"----------\n" + 
				"1. ERROR in Y1.java (at line 5)\n" + 
				"	x.<@NonNull Y1, @NonNull Object>foo(this, new Object())\n" + 
				"	                ^^^^^^^^^^^^^^^\n" + 
				"Null constraint mismatch: The type \'@NonNull Object\' is not a valid substitute for the type parameter \'V extends @Nullable Object\'\n" + 
				"----------\n" + 
				"2. ERROR in Y1.java (at line 6)\n" + 
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
				"	^^^^^^^^^^^^^^\n" + 
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
				"Null type mismatch (type annotations): required \'List<@Nullable ? extends X1>\' but this expression has type \'ArrayList<@NonNull X1>\', corresponding supertype is \'List<@NonNull X1>\'\n" + 
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
				"	^^^^^^^^^^^^^^\n" + 
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
				"Null type mismatch (type annotations): required \'List<@Nullable ? extends X1>\' but this expression has type \'ArrayList<@NonNull X1>\', corresponding supertype is \'List<@NonNull X1>\'\n" + 
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
				"	^^^^^^^^^\n" + 
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
		runNegativeTestWithLibs(
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
		runNegativeTestWithLibs(
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
		runNegativeTestWithLibs(
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
			getCompilerOptions(),
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
		runNegativeTestWithLibs(
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
		runNegativeTestWithLibs(
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
			"The nullness annotation \'NonNull\' is not applicable at this location\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	@NonNull X.Inner foo(@NonNull X.Inner arg) {\n" + 
			"	^^^^^^^^\n" + 
			"The nullness annotation \'NonNull\' is not applicable at this location\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	@NonNull X.Inner foo(@NonNull X.Inner arg) {\n" + 
			"	                     ^^^^^^^^\n" + 
			"The nullness annotation \'NonNull\' is not applicable at this location\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 6)\n" + 
			"	@NonNull X.Inner local = arg;\n" + 
			"	^^^^^^^^\n" + 
			"The nullness annotation \'NonNull\' is not applicable at this location\n" + 
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
		runNegativeTestWithLibs(
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
			"----------\n" + 
			"1. ERROR in p\\X.java (at line 6)\n" + 
			"	if (!(arg instanceof List<@NonNull X>))\n" + 
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot perform instanceof check against parameterized type List<X>. Use the form List<?> instead since further generic type information will be erased at runtime\n" + 
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
			"----------\n");
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
			getCompilerOptions(),
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
		runNegativeTestWithLibs(
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
		runNegativeTestWithLibs(
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
			"----------\n");
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
		Map options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_DEPRECATION, JavaCore.IGNORE);
		runConformTestWithLibs(
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
			},
			options,
			"");
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
		runNegativeTestWithLibs(
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
			"----------\n"); 
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
			"Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'List<@NonNull ? extends @NonNull String>\', corresponding supertype is 'List<String>'\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	ls.add(null);\n" + 
			"	       ^^^^\n" + 
			"Null type mismatch: required \'@NonNull ? extends @NonNull String\' but the provided value is null\n" + 
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
		runNegativeTestWithLibs(
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
			getCompilerOptions(),
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
			"----------\n");
	}

	public void testBug416180() {
		runConformTestWithLibs(
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
			"");
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
			"Null type safety (type annotations): The expression of type \'X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'\n" + 
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
			"Null type safety (type annotations): The expression of type \'X<String>\' needs unchecked conversion to conform to \'X<@Nullable String>\'\n" + 
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
			"1. WARNING in X.java (at line 10)\n" + 
			"	s.foo(new ArrayList<String>()); // (1)\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'@NonNull List<@NonNull String>\', corresponding supertype is 'List<String>'\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 11)\n" + 
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
		runNegativeTestWithLibs(
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
			"2. ERROR in X.java (at line 7)\n" + 
			"	@NonNull String @NonNull [][] s2 = new @NonNull String [] @NonNull [] { null, { null} }; // problem at both nulls\n" + 
			"	                                                                        ^^^^\n" + 
			"Null type mismatch: required \'@NonNull String @NonNull[]\' but the provided value is null\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 7)\n" + 
			"	@NonNull String @NonNull [][] s2 = new @NonNull String [] @NonNull [] { null, { null} }; // problem at both nulls\n" + 
			"	                                                                                ^^^^\n" + 
			"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" + 
			"----------\n");
	}

	// https://bugs.eclipse.org/417758 - [1.8][null] Null safety compromise during array creation.
	// three-dim array with annotations on dimensions, also assignment has a problem
	public void testArray3() {
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
			getCompilerOptions(),
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	@NonNull String [][] @NonNull [] s = new @NonNull String []@NonNull [][] { null, { {null}, null/*ok*/ } };\n" + 
			"	                                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Null type safety (type annotations): The expression of type \'@NonNull String [] @NonNull[] []\' needs unchecked conversion to conform to \'@NonNull String [] [] @NonNull[]\'\n" + 
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
			"1. WARNING in X.java (at line 4)\n" + 
			"	consume(t); // error, t can be null\n" + 
			"	        ^\n" + 
			"Null type safety (type annotations): The expression of type \'T\' needs unchecked conversion to conform to \'@NonNull T\'\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	consumeObject(t); // error, t can be null\n" + 
			"	              ^\n" + 
			"Null type safety (type annotations): The expression of type \'T\' needs unchecked conversion to conform to \'@NonNull Object\'\n" + 
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
			"1. WARNING in X.java (at line 4)\n" + 
			"	consume(t); // error, t can be null\n" + 
			"	        ^\n" + 
			"Null type safety (type annotations): The expression of type \'T extends Number\' needs unchecked conversion to conform to \'@NonNull T extends Number\'\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	consumeObject(t); // error, t can be null\n" + 
			"	              ^\n" + 
			"Null type safety (type annotations): The expression of type \'T extends Number\' needs unchecked conversion to conform to \'@NonNull Object\'\n" + 
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
		runNegativeTestWithLibs(
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
		runNegativeTestWithLibs(
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
			getCompilerOptions(),
			"",
			"78912345678");		
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422134, [1.8] NPE in NullAnnotationMatching with inlined lambda expression used with a raw type
	public void test422134() {
		runNegativeTestWithLibs(
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
			getCompilerOptions(),
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
			"----------\n");		
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
	runNegativeTestWithLibs(
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
		"----------\n");
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
		"5. ERROR in X.java (at line 8)\n" + 
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
		"2. ERROR in C.java (at line 15)\n" + 
		"	la1 = lb2; // ERR2\n" + 
		"	      ^^^\n" + 
		"Null type mismatch (type annotations): required \'List<? extends @NonNull A>\' but this expression has type \'List<@Nullable B>\'\n" + 
		"----------\n" + 
		"3. ERROR in C.java (at line 17)\n" + 
		"	la2.add(null); // ERR3\n" + 
		"	        ^^^^\n" + 
		"Null type mismatch: required \'? extends @Nullable A\' but the provided value is null\n" + 
		"----------\n" + 
		"4. ERROR in C.java (at line 19)\n" + 
		"	return la2.get(0); // ERR4\n" + 
		"	       ^^^^^^^^^^\n" + 
		"Null type mismatch (type annotations): required \'@NonNull A\' but this expression has type \'capture#of ? extends @Nullable A\'\n" + 
		"----------\n" + 
		"5. ERROR in C.java (at line 25)\n" + 
		"	<T extends @Nullable A> T mExtends1(List<T> t) { return null; /*ERR5*/ }\n" + 
		"	                                                        ^^^^\n" + 
		"Null type mismatch: required \'T extends @Nullable A\' but the provided value is null\n" + 
		"----------\n" + 
		"6. ERROR in C.java (at line 26)\n" + 
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
	runNegativeTestWithLibs(
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
		getCompilerOptions(),
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
		"----------\n");
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
		"Null type mismatch (type annotations): required \'List<@NonNull Person>\' but this expression has type \'ArrayList<@Nullable Person>\', corresponding supertype is \'List<@Nullable Person>\'\n" + 
		"----------\n");
}
public void testBug430219() {
    runNegativeTestWithLibs(
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
	   "----------\n");
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
		"Null type mismatch (type annotations): required \'List<@NonNull Number>\' but this expression has type \'ArrayList<@Nullable Number>\', corresponding supertype is \'List<@Nullable Number>\'\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 10)\n" + 
		"	in.add(null); // ERR\n" + 
		"	       ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 11)\n" + 
		"	return new ArrayList<java.lang.@Nullable Number>(); // ERR\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Null type mismatch (type annotations): required \'List<@NonNull Number>\' but this expression has type \'ArrayList<@Nullable Number>\', corresponding supertype is \'List<@Nullable Number>\'\n" + 
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
		"Null type mismatch (type annotations): \'null\' is not compatible to the free type variable '? extends Number'\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 7)\n" + 
		"	needNN(in.get(0)); // NOK, cannot assume nonnull\n" + 
		"	       ^^^^^^^^^\n" + 
		"Null type safety (type annotations): The expression of type \'capture#of ? extends Number\' needs unchecked conversion to conform to \'@NonNull Number\'\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 8)\n" + 
		"	return new ArrayList<@Nullable T>(); // NOK, cannot assume nullable for T in List<T>\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Null type mismatch (type annotations): required \'List<T>\' but this expression has type \'ArrayList<@Nullable T>\', corresponding supertype is \'List<@Nullable T>\'\n" + 
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
		"1. ERROR in X.java (at line 9)\n" + 
		"	return null; // ERR\n" + 
		"	       ^^^^\n" + 
		"Null type mismatch: required \'T extends @NonNull Number\' but the provided value is null\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 13)\n" + 
		"	@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg\n" + 
		"	                                    ^^^^^^^^^^^^^^^^^^\n" + 
		"Null type safety (type annotations): The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Integer\'\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 13)\n" + 
		"	@NonNull Number nnn = inner.process(Integer.valueOf(3), new ArrayList<@Nullable Integer>()); // WARN on 1. arg; ERR on 2. arg\n" + 
		"	                                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Null type mismatch (type annotations): required \'List<? extends @NonNull Number>\' but this expression has type \'ArrayList<@Nullable Integer>\', corresponding supertype is \'List<@Nullable Integer>\'\n" + 
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
		"Null type mismatch (type annotations): required \'List<@NonNull Number>\' but this expression has type \'ArrayList<@Nullable Number>\', corresponding supertype is \'List<@Nullable Number>\'\n" + 
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
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	@NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"	Number test(Number in) {\n" +
			"		return new Integer(13);\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		new String[] {
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y {\n" +
			"	@NonNull Number test(X x) {\n" +
			"		return x.test(null); // both OK\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}

// apply null default to field - also test mixing of explicit annotation with default @NonNull (other annot is not rendered in error)
public void testDefault04_bin() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.TYPE_USE) @Retention(RetentionPolicy.CLASS) @interface Important {}\n" +
			"@NonNullByDefault(DefaultLocation.FIELD)\n" +
			"public class X {\n" +
			"	@Important Number field = new Double(1.1d);\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"	void test(X x) {\n" +
			"		x.field = null; // ERR\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" + 
		"1. ERROR in Y.java (at line 3)\n" + 
		"	x.field = null; // ERR\n" + 
		"	          ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Number\' but the provided value is null\n" + 
		"----------\n");}

// default default
public void testDefault05_bin() {
	runConformTestWithLibs(
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
		},
		getCompilerOptions(),
		"");
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
	runNegativeTestWithLibs(
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
		"Null type mismatch (type annotations): required \'List<? extends @NonNull Number>\' but this expression has type \'ArrayList<@Nullable Integer>\', corresponding supertype is \'List<@Nullable Integer>\'\n" + 
		"----------\n");
}
public void testBug431269() {
	runNegativeTestWithLibs(
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
		"----------\n");
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
	runConformTestWithLibs(
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
		"3. WARNING in X.java (at line 6)\n" + 
		"	@NonNull String s = m(() -> \"\");\n" + 
		"	                    ^^^^^^^^^^^\n" + 
		"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'\n" + 
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
	runNegativeTestWithLibs(
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
		getCompilerOptions(),
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
			"interface I<S, T extends @Nullable List<@NonNull List<S>>> {\n" +
			"}\n",
			"C.java",
			"import java.util.*;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class C implements I<@Nullable String, ArrayList<@NonNull List<@Nullable String>>> {}\n" +
			"class C1 {\n" +
			"	I<String, @Nullable ArrayList<@Nullable List<String>>> field;\n" +
			"}\n" +
			"class C2 implements I<@NonNull String, @Nullable ArrayList<@NonNull List<@Nullable String>>> {}\n" // FIXME: cross checking for contradictory substitution for 'S' NYI
		},
		getCompilerOptions(),
		"----------\n" + 
		"1. ERROR in C.java (at line 3)\n" + 
		"	public class C implements I<@Nullable String, ArrayList<@NonNull List<@Nullable String>>> {}\n" + 
		"	                                              ^^^^^^^^^\n" + 
		"Null constraint mismatch: The type \'ArrayList<@NonNull List<@Nullable String>>\' is not a valid substitute for the type parameter \'T extends @Nullable List<@NonNull List<S>>\'\n" + 
		"----------\n" + 
		"2. ERROR in C.java (at line 5)\n" + 
		"	I<String, @Nullable ArrayList<@Nullable List<String>>> field;\n" + 
		"	          ^^^^^^^^^^^^^^^^^^^\n" + 
		"Null constraint mismatch: The type \'@Nullable ArrayList<@Nullable List<String>>\' is not a valid substitute for the type parameter \'T extends @Nullable List<@NonNull List<S>>\'\n" + 
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
			"class C2 implements p.I<@NonNull String, @Nullable ArrayList<@NonNull List<@Nullable String>>> {}\n" // FIXME: cross checking for contradictory substitution for 'S' NYI
		},
		getCompilerOptions(),
		"----------\n" + 
		"1. ERROR in C.java (at line 3)\n" + 
		"	public class C implements p.I<@Nullable String, ArrayList<@NonNull List<@Nullable String>>> {}\n" + 
		"	                                                ^^^^^^^^^\n" + 
		"Null constraint mismatch: The type \'ArrayList<@NonNull List<@Nullable String>>\' is not a valid substitute for the type parameter \'T extends @Nullable List<@NonNull List<S>>\'\n" + 
		"----------\n" + 
		"2. ERROR in C.java (at line 5)\n" + 
		"	p.I<String, @Nullable ArrayList<@Nullable List<String>>> field;\n" + 
		"	            ^^^^^^^^^^^^^^^^^^^\n" + 
		"Null constraint mismatch: The type \'@Nullable ArrayList<@Nullable List<String>>\' is not a valid substitute for the type parameter \'T extends @Nullable List<@NonNull List<S>>\'\n" + 
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
		getCompilerOptions(),
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
	runConformTestWithLibs(
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
		"");
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
		"1. WARNING in X.java (at line 5)\n" + 
		"	@NonNull T localT = t; // err#1\n" + 
		"	                    ^\n" + 
		"Null type safety (type annotations): The expression of type \'T\' needs unchecked conversion to conform to \'@NonNull T\'\n" + 
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
		"1. WARNING in X.java (at line 8)\n" + 
		"	@NonNull T localT = t; // err#1\n" + 
		"	                    ^\n" + 
		"Null type safety (type annotations): The expression of type \'T\' needs unchecked conversion to conform to \'@NonNull T\'\n" + 
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
		"1. ERROR in Foo.java (at line 20)\n" + 
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
	runConformTestWithLibs(
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
		"");
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
	String jreDirectory = Util.getJREDirectory();
	String jfxJar = Util.toNativePath(jreDirectory + "/lib/ext/jfxrt.jar");
	this.runNegativeTestWithExtraLibs(
		new String[] {
			"BuildIdeMain.java",
			"import java.nio.file.Path;\n" +
			"import java.time.Instant;\n" +
			"import java.util.HashMap;\n" +
			"import java.util.stream.Stream;\n" +
			"import javafx.util.Pair;\n" +
			"\n" +
			"public class BuildIdeMain {\n" +
			"static void writeUpdates(Stream<Path> filter2, HashMap<Path, Pair<byte[], Instant>> ideFiles, HashMap<Path, Path> updateToFile) {\n" +
			"   filter2.map(p -> new Pair<>(updateToFile.get(p), p->ideFiles.get(p)));\n" +
			"}\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in BuildIdeMain.java (at line 9)\n" + 
		"	filter2.map(p -> new Pair<>(updateToFile.get(p), p->ideFiles.get(p)));\n" + 
		"	                                                 ^^^^^^^^^^^^^^^^^^\n" + 
		"The target type of this expression must be a functional interface\n" + 
		"----------\n",
		new String[]{jfxJar});
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
	this.runNegativeTestWithLibs(
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
		"The target type of this expression must be a functional interface\n" + 
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
	runNegativeTestWithLibs(
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
		"----------\n" + 
		"1. WARNING in Test.java (at line 12)\n" + 
		"	@NonNull Object o = x;\n" + 
		"	                    ^\n" + 
		"Null type safety (type annotations): The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Object\'\n" + 
		"----------\n");
}
// merging @NonNull & unannotated in arg-position must answer unannotated - swapped order
public void testBug446442_2b() {
	runNegativeTestWithLibs(
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
			"  public Integer m(Integer i) { return new Integer(0); }\n" + 
			"}\n" +
			"\n" + 
			"public class Test {\n" + 
			"	Baz baz= x -> null;\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Test.java (at line 11)\n" + 
		"	public Integer m(Integer i) { return new Integer(0); }\n" + 
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
		"3. ERROR in Test.java (at line 16)\n" + 
		"	Baz baz= x -> {\n" + 
		"		x.add(null);\n" + 
		"		x.get(0);\n" + 
		"		return x;\n" + 
		"	};\n" + 
		"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Contradictory null annotations: function type was inferred as \'ArrayList<@NonNull @Nullable Integer> (ArrayList<@Nullable @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" + 
		"----------\n" + 
		"4. ERROR in Test.java (at line 17)\n" + 
		"	x.add(null);\n" + 
		"	^^^^^^^^^^^\n" + 
		"Contradictory null annotations: method was inferred as \'boolean add(@Nullable @NonNull Integer)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" + 
		"----------\n" + 
		"5. ERROR in Test.java (at line 18)\n" + 
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
	runNegativeTestWithLibs(
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
		"----------\n");
}
public void testBug448709() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING); // ensure program is runnable
	runConformTestWithLibs(
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
		"1. WARNING in Test.java (at line 39)\n" + 
		"	map(optNullableString, testMethodRef);\n" + 
		"	                       ^^^^^^^^^^^^^\n" + 
		"Contradictory null annotations: method was inferred as \'@NonNull Optional<@NonNull Integer> map(@NonNull Optional<@Nullable String>, Function<@NonNull ? super @Nullable String,? extends @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" + 
		"----------\n" + 
		"2. WARNING in Test.java (at line 41)\n" + 
		"	map(optNullableString, Test::testMethod); // Error: Null type mismatch at parameter 1: required \'@NonNull String\' but provided \'@Nullable String\' via method descriptor Function<String,Integer>.apply(String)\n" + 
		"	                       ^^^^^^^^^^^^^^^^\n" + 
		"Contradictory null annotations: method was inferred as \'@NonNull Optional<@NonNull Integer> map(@NonNull Optional<@Nullable String>, Function<@NonNull ? super @Nullable String,? extends @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" + 
		"----------\n" + 
		"3. WARNING in Test.java (at line 43)\n" + 
		"	map(optNullableString, (s) -> Test.testMethod(s));\n" + 
		"	                       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Contradictory null annotations: method was inferred as \'@NonNull Optional<@NonNull Integer> map(@NonNull Optional<@Nullable String>, Function<@NonNull ? super @Nullable String,? extends @NonNull Integer>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" + 
		"----------\n" + 
		"4. WARNING in Test.java (at line 43)\n" + 
		"	map(optNullableString, (s) -> Test.testMethod(s));\n" + 
		"	                                              ^\n" + 
		"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" + 
		"----------\n",
		"1->2\n" +
		"1->2\n" +
		"1->2");
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
	runNegativeTestWithLibs(
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
	runNegativeTestWithLibs(
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
		"Null type mismatch (type annotations): required \'F0<@NonNull String>\' but this expression has type \'FI<@Nullable String>\', corresponding supertype is \'F0<@Nullable String>\'\n" + 
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
    runNegativeTestWithLibs(
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
            "----------\n" +
    		"1. WARNING in projA\\ClassWithRawUsage.java (at line 4)\n" + 
    		"	public java.util.List<GenericType> method() {\n" + 
    		"	                      ^^^^^^^^^^^\n" + 
            "GenericType is a raw type. References to generic type GenericType<T> should be parameterized\n" +
            "----------\n");
    runNegativeTestWithLibs(
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
	runNegativeTestWithLibs(
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
			"----------\n" +
			"1. WARNING in projA\\ClassWithRawUsage.java (at line 3)\n" +
			"	public java.util.List<@proj0.MyAnnotation GenericType> method() {\n" +
			"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"GenericType is a raw type. References to generic type GenericType<T> should be parameterized\n" +
			"----------\n");
	runNegativeTestWithLibs(
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
	runNegativeTestWithLibs(
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
		getCompilerOptions(),
		"----------\n" + 
		"1. ERROR in Bug.java (at line 3)\n" + 
		"	return o instanceof java.util.Iterator<java.lang. @MyAnnot @org.eclipse.jdt.annotation.Nullable String>;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Cannot perform instanceof check against parameterized type Iterator<String>. Use the form Iterator<?> instead since further generic type information will be erased at runtime\n" + 
		"----------\n" + 
		"2. ERROR in Bug.java (at line 3)\n" + 
		"	return o instanceof java.util.Iterator<java.lang. @MyAnnot @org.eclipse.jdt.annotation.Nullable String>;\n" + 
		"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Nullness annotations are not applicable at this location \n" + 
		"----------\n");
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
			new String[] {
				"Class2.java",
				"class Class2 {;\n"+
				"  Class1 x;"+
				"}\n"
			}, getCompilerOptions(), "");
}
public void testBug446217() {
	runConformTestWithLibs(
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
		},
		getCompilerOptions(), "");
}
public void testBug456584() {
	runConformTestWithLibs(
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
		getCompilerOptions(),
		"----------\n" + 
		"1. WARNING in Test.java (at line 9)\n" + 
		"	return Objects.requireNonNull(function.apply(input));\n" + 
		"	                              ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Null type safety (type annotations): The expression of type \'capture#of ? extends R\' needs unchecked conversion to conform to \'@NonNull capture#of ? extends R\'\n" + 
		"----------\n");
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
		"----------\n");
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
}
