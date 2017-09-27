/*******************************************************************************
 * Copyright (c) 2012, 2014 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;

public class NullTypeAnnotationTest extends AbstractNullAnnotationTest {

	public NullTypeAnnotationTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//			TESTS_NAMES = new String[] { "testNullTypeInference3b" };
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
				  "        public X foo(Y l) {\n" +
				  "            return null;\n" +
				  "        }\n" +
				  "    }\n" +
				  "    void bar(A<@Nullable Object>.I<@NonNull Object> i) {\n" + // legal instantiation
				  "        @NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts\n" +
				  "    }\n" +
				  "}\n"},
			"----------\n" + 
			"1. ERROR in A.java (at line 9)\n" + 
			"	@NonNull Object o = i.foo(null); // problems: argument and assignment violate null contracts\n" + 
			"	                    ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required '@NonNull Object' but this expression has type '@Nullable Object'\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 9)\n" + 
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
			"The return type is incompatible with the @NonNull return from A<Object>.I<String>.foo(String)\n" + 
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
				  "        unknownArrays = maybeArrays;			// problsm on outer dimension\n" +
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
			"4. ERROR in A.java (at line 7)\n" + 
			"	unknownArrays = maybeArrays;			// problsm on outer dimension\n" + 
			"	                ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required \'String @Nullable[] []\' but this expression has type \'String @NonNull[] @Nullable[]\'\n" + 
			"----------\n" + 
			"5. WARNING in A.java (at line 8)\n" + 
			"	realArrays = mixedArrays;			// problem on inner\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Null type safety (type annotations): The expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @NonNull[]\'\n" + 
			"----------\n" + 
			"6. WARNING in A.java (at line 9)\n" + 
			"	maybeArrays = mixedArrays;			// problem on inner\n" + 
			"	              ^^^^^^^^^^^\n" + 
			"Null type safety (type annotations): The expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @Nullable[]\'\n" + 
			"----------\n" + 
			"7. ERROR in A.java (at line 10)\n" + 
			"	consume(maybeArrays, mixedArrays, maybeArrays);\n" + 
			"	        ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required \'String @NonNull[] @NonNull[]\' but this expression has type \'String @NonNull[] @Nullable[]\'\n" + 
			"----------\n" + 
			"8. WARNING in A.java (at line 10)\n" + 
			"	consume(maybeArrays, mixedArrays, maybeArrays);\n" + 
			"	                     ^^^^^^^^^^^\n" + 
			"Null type safety (type annotations): The expression of type \'String @NonNull[] []\' needs unchecked conversion to conform to \'String @NonNull[] @Nullable[]\'\n" + 
			"----------\n" + 
			"9. ERROR in A.java (at line 10)\n" + 
			"	consume(maybeArrays, mixedArrays, maybeArrays);\n" + 
			"	                                  ^^^^^^^^^^^\n" + 
			"Null type mismatch (type annotations): required \'String @Nullable[] []\' but this expression has type \'String @NonNull[] @Nullable[]\'\n" + 
			"----------\n");
	}
	
	// https://bugs.eclipse.org/403216 - [1.8][null] TypeReference#captureTypeAnnotations treats type annotations as type argument annotations 
	public void testBug403216_1() {
		runConformTest(
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
			});
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
				"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'T\' which is constrained as \'@NonNull\'\n" + 
				"----------\n" + 
				"2. ERROR in Y1.java (at line 6)\n" + 
				"	x.<@NonNull Object>foo(new Object());\n" + 
				"	   ^^^^^^^^^^^^^^^\n" + 
				"Null constraint mismatch: The type \'@NonNull Object\' is not a valid substitute for the type parameter \'S\' which is constrained as \'@Nullable\'\n" + 
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
				"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'T\' which is constrained as \'@NonNull\'\n" + 
				"----------\n" + 
				"2. ERROR in Y1.java (at line 6)\n" + 
				"	X2<@NonNull String> strings;\n" + 
				"	   ^^^^^^^^^^^^^^^\n" + 
				"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'W\' which is constrained as \'@Nullable\'\n" + 
				"----------\n" + 
				"3. ERROR in Y1.java (at line 8)\n" + 
				"	x.<Y1, @NonNull Object>foo(this, new Object());\n" + 
				"	       ^^^^^^^^^^^^^^^\n" + 
				"Null constraint mismatch: The type '@NonNull Object' is not a valid substitute for the type parameter 'V' which is constrained as '@Nullable'\n" + 
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
				"Null constraint mismatch: The type \'@Nullable String\' is not a valid substitute for the type parameter \'T\' which is constrained as \'@NonNull\'\n" + 
				"----------\n" + 
				"2. ERROR in Y1.java (at line 4)\n" + 
				"	p.X2<java.lang.@NonNull String> strings;\n" + 
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Null constraint mismatch: The type \'@NonNull String\' is not a valid substitute for the type parameter \'W\' which is constrained as \'@Nullable\'\n" + 
				"----------\n" + 
				"3. ERROR in Y1.java (at line 6)\n" + 
				"	x.<Y1, java.lang.@NonNull Object>foo(this, new Object());\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Null constraint mismatch: The type '@NonNull Object' is not a valid substitute for the type parameter 'V' which is constrained as '@Nullable'\n" + 
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
				"Null constraint mismatch: The type '@NonNull Object' is not a valid substitute for the type parameter 'V' which is constrained as '@Nullable'\n" + 
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
					"		Object @NonNull[][] a = new Object[0][]; // unsafe\n" +
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
				"1. WARNING in Y1.java (at line 6)\n" + 
				"	Object @NonNull[][] a = new Object[0][]; // unsafe\n" + 
				"	                        ^^^^^^^^^^^^^^^\n" + 
				"Null type safety (type annotations): The expression of type \'Object[][]\' needs unchecked conversion to conform to \'Object @NonNull[] []\'\n" + 
				"----------\n" + 
				"2. ERROR in Y1.java (at line 7)\n" + 
				"	x.arrays(a)[0] = null; // illegal\n" + 
				"	^^^^^^^^^^^^^^\n" + 
				"Null type mismatch: required \'Object @NonNull[]\' but the provided value is null\n" +
				"----------\n" + 
				"3. ERROR in Y1.java (at line 8)\n" + 
				"	x.nesting(null, null); // 1st null is illegal\n" + 
				"	          ^^^^\n" + 
				"Null type mismatch: required \'X1.@NonNull Inner\' but the provided value is null\n" + 
				"----------\n" + 
				"4. ERROR in Y1.java (at line 11)\n" + 
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
					"		java.lang.Object @NonNull[][] a = new java.lang.Object[0][]; // unsafe\n" +
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
				"1. WARNING in Y1.java (at line 6)\n" + 
				"	java.lang.Object @NonNull[][] a = new java.lang.Object[0][]; // unsafe\n" + 
				"	                                  ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Null type safety (type annotations): The expression of type \'Object[][]\' needs unchecked conversion to conform to \'Object @NonNull[] []\'\n" + 
				"----------\n" + 
				"2. ERROR in Y1.java (at line 7)\n" + 
				"	x.arrays(a)[0] = null; // illegal\n" + 
				"	^^^^^^^^^^^^^^\n" + 
				"Null type mismatch: required \'Object @NonNull[]\' but the provided value is null\n" +
				"----------\n" + 
				"3. ERROR in Y1.java (at line 8)\n" + 
				"	x.nesting(null, null); // 1st null is illegal\n" + 
				"	          ^^^^\n" + 
				"Null type mismatch: required \'X1.@NonNull Inner\' but the provided value is null\n" + 
				"----------\n" + 
				"4. ERROR in Y1.java (at line 11)\n" + 
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
			"Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 8)\n" + 
			"	return f == 2 ? dubious : good;\n" + 
			"	                ^^^^^^^\n" + 
			"Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'List<@NonNull String>\'\n" + 
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
				"	void foo(X<Object> x) {\n" + 
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
			"Null type mismatch (type annotations): required \'Map<@Nullable String,@NonNull Object>\' but this expression has type \'X<Object>\', corresponding supertype is \'Map<@Nullable String,@Nullable Object>\'\n" + 
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
			"1. WARNING in PolyNull.java (at line 9)\n" + 
			"	return extract(i -> needNN(i), \"ola\");\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull String\'\n" + 
			"----------\n" + 
			"2. ERROR in PolyNull.java (at line 9)\n" + 
			"	return extract(i -> needNN(i), \"ola\");\n" + 
			"	                           ^\n" + 
			"Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n" + 
			"----------\n");
	}

	// conflicting annotations from type variable application and type variable substitution
	public void testNullTypeInference3() {
		runNegativeTestWithLibs(
			new String[] {
				"Generics.java",
				"import org.eclipse.jdt.annotation.*;\n" + 
				"\n" + 
				"public class Generics {\n" + 
				"	<X> X m(@Nullable X a) { return null; }\n" + 
				"	void test(@NonNull String in) {\n" + 
				"		@NonNull String s = m(in);\n" + 
				"		System.out.println(s.toLowerCase());\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new Generics().test(\"hallo\");\n" + 
				"	}\n" + 
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" + 
			"1. ERROR in Generics.java (at line 6)\n" + 
			"	@NonNull String s = m(in);\n" + 
			"	                      ^^\n" + 
			"Contradictory null annotations: method was inferred as \'@NonNull String m(@NonNull @Nullable String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" + 
			"----------\n");
	}

	// conflicting annotations from type variable application and type variable substitution
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
			"Contradictory null annotations: method was inferred as \'@NonNull @Nullable String m1(@NonNull @Nullable String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" + 
			"----------\n" + 
			"2. ERROR in Generics.java (at line 8)\n" + 
			"	@NonNull String s2 = m2(in);\n" + 
			"	                     ^^^^^^\n" + 
			"Contradictory null annotations: method was inferred as \'@NonNull @Nullable String m2(@NonNull String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" + 
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
			"1. ERROR in Generics.java (at line 14)\n" + 
			"	map2(inList, f);\n" + 
			"	     ^^^^^^\n" + 
			"Contradictory null annotations: method was inferred as \'Collection<@NonNull String> map2(Collection<@NonNull @Nullable Object>, Function<@NonNull @Nullable Object,@NonNull String>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" + 
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
			"1. ERROR in X.java (at line 12)\n" + 
			"	class Z extends X {\n" + 
			"	      ^\n" + 
			"The method foo1(List<@NonNull X>) from Z cannot implement the corresponding method from X due to incompatible nullness constraints\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 12)\n" + 
			"	class Z extends X {\n" + 
			"	      ^\n" + 
			"The method foo2(List<X>) from Z cannot implement the corresponding method from X due to incompatible nullness constraints\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 12)\n" + 
			"	class Z extends X {\n" + 
			"	      ^\n" + 
			"The method foo3(List<X>) from Z cannot implement the corresponding method from X due to incompatible nullness constraints\n" + 
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
			"1. ERROR in X.java (at line 18)\n" + 
			"	abstract class Z extends X {\n" + 
			"	               ^\n" + 
			"The method List<@NonNull X> foo1() from Z cannot implement the corresponding method from X due to incompatible nullness constraints\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 18)\n" + 
			"	abstract class Z extends X {\n" + 
			"	               ^\n" + 
			"The method List<@NonNull X> foo2() from Z cannot implement the corresponding method from X due to incompatible nullness constraints\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 18)\n" + 
			"	abstract class Z extends X {\n" + 
			"	               ^\n" + 
			"The method @NonNull List<X> foo3() from Z cannot implement the corresponding method from X due to incompatible nullness constraints\n" + 
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
			"Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'List<@NonNull ? extends @NonNull String>\'\n" + 
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
				"	@NonNull T bar2(@Nullable T t) { // argument: contradiction (1)\n" +
				"		return t;\n" +
				"	}\n" + 
				"	@Nullable T bar3(T t) { // return type: contradiction (2)\n" +
				"		@Nullable T l = t; // local: contradiction (3)\n" +
				"		return l;\n" +
				"	}\n" +
				"	class Inner {\n" +
				"		@Nullable T f; // field: contradiction (4)\n" +
				"	}\n" + 
				"	T bar3() {\n" +
				"		return null;\n" +
				"	}\n" + 
				"}\n"
			},
			getCompilerOptions(),
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	@NonNull T bar2(@Nullable T t) { // argument: contradiction (1)\n" + 
			"	                ^^^^^^^^^\n" + 
			"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\n" + 
			"	@Nullable T bar3(T t) { // return type: contradiction (2)\n" + 
			"	^^^^^^^^^\n" + 
			"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 15)\n" + 
			"	@Nullable T l = t; // local: contradiction (3)\n" + 
			"	^^^^^^^^^\n" + 
			"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 19)\n" + 
			"	@Nullable T f; // field: contradiction (4)\n" + 
			"	^^^^^^^^^\n" + 
			"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 22)\n" + 
			"	return null;\n" + 
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
			"1. ERROR in X.java (at line 10)\n" + 
			"	xs.foo(null);\n" + 
			"	^^^^^^^^^^^^\n" + 
			"Contradictory null annotations: method was inferred as \'@Nullable String foo(@NonNull @Nullable String)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" + 
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
			"Null type safety (type annotations): The expression of type \'ArrayList<String>\' needs unchecked conversion to conform to \'@NonNull List<@NonNull String>\'\n" + 
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
	public void testTypeVariable() {
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
			"Contradictory null annotations: method was inferred as \'void getAdd(List<@NonNull @Nullable capture#of @Nullable ? extends X>)\', but only one of \'@NonNull\' and \'@Nullable\' can be effective at any location\n" + 
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
		runConformTest(
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
		runConformTest(
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
			"Type safety: Unchecked invocation sort(ArrayList, (<no type> o1, <no type> o2) -> {\n" + 
			"  return o1.compareToIgnoreCase(o1);\n" + 
			"}) of the generic method sort(List<T>, Comparator<? super T>) of type Collections\n" + 
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
			null,
			true,
			getCompilerOptions());		
	}

	// should not try to analyze arguments of a polymorphic method call
	public void testBug424725() {
		runConformTest(
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
			});
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
	runConformTest(
		new String[] {
			"VarArgsMethodReferenceTest.java",
			"import java.util.function.Consumer;\n" + 
			"public class VarArgsMethodReferenceTest {\n" + 
			"  @SuppressWarnings(\"unused\") public static void main(String[] argv) {\n" + 
			"    Consumer<String> printffer;\n" + 
			"    printffer = System.out::printf;\n" + 
			"  }\n" + 
			"}"
		});
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
public void testBug392245_tmp_warning() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault(DefaultLocation.TYPE_ARGUMENT)\n" +
			"public class X {\n" +
			"	void m(Object o) {}\n" +
			"	void test() {\n" +
			"		m(null); // ERR\n" + // since @NonNullByDefault is still interpreted as all or nothing
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 2)\n" + 
		"	@NonNullByDefault(DefaultLocation.TYPE_ARGUMENT)\n" + 
		"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Arguments controling the details of the nullness default are not yet evaluated by the analysis.\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	m(null); // ERR\n" + 
		"	  ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
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
}
