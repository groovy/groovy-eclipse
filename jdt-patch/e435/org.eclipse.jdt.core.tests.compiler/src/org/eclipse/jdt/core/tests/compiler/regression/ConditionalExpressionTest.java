/*******************************************************************************
 * Copyright (c) 2005, 2021 IBM Corporation and others.
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

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "rawtypes" })
public class ConditionalExpressionTest extends AbstractRegressionTest {

	public ConditionalExpressionTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test003" };
//		TESTS_NUMBERS = new int[] { 65 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}
	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return ConditionalExpressionTest.class;
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100162
	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    final boolean isA = true;\n" +
				"    public static void main(String[] args) {\n" +
				"        X x = new X();\n" +
				"        System.out.print(x.isA ? \"SUCCESS\" : \"FAILURE\");\n" +
				"    }\n" +
				"}",
			},
			"SUCCESS"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=107193
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class RecipeElement {\n" +
				"    public static final RecipeElement[] NO_CHILDREN= new RecipeElement[0]; \n" +
				"}\n" +
				"class Ingredient extends RecipeElement { }\n" +
				"class X extends RecipeElement {\n" +
				"    private Ingredient[] fIngredients;\n" +
				"    public RecipeElement[] getChildren() {\n" +
				"        return fIngredients == null ? NO_CHILDREN : fIngredients;\n" +
				"    }\n" +
				"}",
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426078, Bug 426078 - [1.8] VerifyError when conditional expression passed as an argument
	public void test003() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	boolean isOdd(boolean what) {\n" +
				"		return square(what ? new Integer(1) : new Integer(2)) % 2 == 1; // trouble here\n" +
				"	}\n" +
				"	<T> int square(int i) {\n" +
				"		return i * i;\n" +
				"	}\n" +
				"	public static void main(String argv[]) {\n" +
				"		System.out.println(new X().isOdd(true));\n" +
				"	}\n" +
				"}\n",
			},
			"true"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423685, - [1.8] poly conditional expression must not use lub
	public void test004() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		if (this.complianceLevel < ClassFileConstants.JDK1_8) {
			this.runNegativeTest(
					new String[] {
						"X.java",
						"class A{/**/}\n" +
						"class B extends A {/**/}\n" +
						"class G<T> {\n" +
						"	G<B> gb=null;\n" +
						"	G<? super A> gsa=null;\n" +
						"	G<? super B> l = (true)? gsa : gb;\n" +
						"}\n" +
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(\"OK\");\n" +
						"	}\n" +
						"}\n",
					},
					"----------\n" +
					"1. ERROR in X.java (at line 6)\n" +
					"	G<? super B> l = (true)? gsa : gb;\n" +
					"	                 ^^^^^^^^^^^^^^^^\n" +
					"Type mismatch: cannot convert from G<capture#2-of ? extends Object> to G<? super B>\n" +
					"----------\n"
				);
		} else {
			this.runConformTest(
					new String[] {
							"X.java",
							"class A{/**/}\n" +
							"class B extends A {/**/}\n" +
							"class G<T> {\n" +
							"	G<B> gb=null;\n" +
							"	G<? super A> gsa=null;\n" +
							"	G<? super B> l = (true)? gsa : gb;\n" +
							"}\n" +
							"public class X {\n" +
							"	public static void main(String[] args) {\n" +
							"		System.out.println(\"OK\");\n" +
							"	}\n" +
							"}\n",
					},
					"OK"
					);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425181, - Cast expression in ternary operation reported as incompatible
	public void test005() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		if (this.complianceLevel < ClassFileConstants.JDK1_8) {
			this.runNegativeTest(
					new String[] {
						"X.java",
						"public class X {\n" +
						"    public static void main(String args[]) {\n" +
						"    	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" +
						"       System.out.println(\"OK\");\n" +
						"    }\n" +
						"}\n" +
						"interface I<T> {}\n" +
						"interface J<T> extends I<T> {}\n",
					},
					"----------\n" +
					"1. WARNING in X.java (at line 3)\n" +
					"	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" +
					"	          ^\n" +
					"J is a raw type. References to generic type J<T> should be parameterized\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 3)\n" +
					"	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" +
					"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Type mismatch: cannot convert from I<capture#1-of ? extends I> to I<? super J>\n" +
					"----------\n" +
					"3. WARNING in X.java (at line 3)\n" +
					"	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" +
					"	                           ^\n" +
					"I is a raw type. References to generic type I<T> should be parameterized\n" +
					"----------\n" +
					"4. WARNING in X.java (at line 3)\n" +
					"	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" +
					"	                                         ^\n" +
					"J is a raw type. References to generic type J<T> should be parameterized\n" +
					"----------\n"
				);
		} else {
			this.runConformTest(
					new String[] {
					"X.java",
					"public class X {\n" +
					"    public static void main(String args[]) {\n" +
					"    	I<? super J> i = true ? (I<I>) null : (I<J>) null; // Type mismatch reported\n" +
					"       System.out.println(\"OK\");\n" +
					"    }\n" +
					"}\n" +
					"interface I<T> {}\n" +
					"interface J<T> extends I<T> {}\n",
					},
					"OK"
					);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426315, - [1.8][compiler] UnsupportedOperationException with conditional expression
	public void test006() {
		this.runConformTest(
				new String[] {
					"X.java",
						"public class X {\n" +
						"	static int foo(Object x) {\n" +
						"		return 0;\n" +
						"	}\n" +
						"	static int foo(int e) { \n" +
						"		return 1; \n" +
						"	}\n" +
						" 	public static void main(String args[]) {\n" +
						" 		Object x = new Object();\n" +
						"		System.out.println(foo(true ? x : new int[0]) != 0);\n" +
						"	}\n" +
						"}\n",
				},
				"false"
				);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426680, - [1.8][compiler] Incorrect handling of poly conditional leads to CCE
	public void test007() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runNegativeTest(
				new String[] {
						"X.java",
						"interface BinaryOperation<T> {\n" +
						"    T operate(T x, T y);\n" +
						"}\n" +
						"class StringCatenation implements BinaryOperation<String> { \n" +
						"    public String operate(String x, String y) { return x + y; }\n" +
						"}\n" +
						"public class X {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	foo(false ? (a,b)->a+b :new StringCatenation());\n" +
						"    }\n" +
						"    static void foo(BinaryOperation<Integer> x) {\n" +
						"       x.operate(5, 15);\n" +
						"    }\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	foo(false ? (a,b)->a+b :new StringCatenation());\n" +
				"	                        ^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type mismatch: cannot convert from StringCatenation to BinaryOperation<Integer>\n" +
				"----------\n"
				);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426680, - [1.8][compiler] Incorrect handling of poly conditional leads to CCE
	public void test008() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runNegativeTest(
				new String[] {
						"X.java",
						"interface BinaryOperation<T> {\n" +
						"    T operate(T x, T y);\n" +
						"}\n" +
						"class StringCatenation implements BinaryOperation<String> { \n" +
						"    public String operate(String x, String y) { return x + y; }\n" +
						"}\n" +
						"public class X {\n" +
						"    public static void main(String argv[]) {\n" +
						"    	foo(false ? new StringCatenation() : (a,b)->a+b);\n" +
						"    }\n" +
						"    static void foo(BinaryOperation<Integer> x) {\n" +
						"       x.operate(5, 15);\n" +
						"    }\n" +
						"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 9)\n" +
				"	foo(false ? new StringCatenation() : (a,b)->a+b);\n" +
				"	            ^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type mismatch: cannot convert from StringCatenation to BinaryOperation<Integer>\n" +
				"----------\n"
				);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427207, - [1.8][bytecode] Runtime type problem: Instruction type does not match stack map
	// Reference poly conditional in assignment context
	public void test009() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"import java.util.function.Function;\n" +
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(test(1, X::intToSome));\n" +
						"	}\n" +
						"	static <T> Some test(T value, Function<T, Some> f) {\n" +
						"		return (value == null) ? new Nothing() : f.apply(value);\n" +
						"	}\n" +
						"	static SomeInt intToSome(int i) {\n" +
						"		return new SomeInt();\n" +
						"	}\n" +
						"	static abstract class Some {}\n" +
						"	static class SomeInt extends Some {\n" +
						"	    public String toString() {\n" +
						"			return \"SomeInt instance\";\n" +
						"        }\n" +
						"   }\n" +
						"	static class Nothing extends Some {}\n" +
						"}\n",
				},
				"SomeInt instance");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427207, - [1.8][bytecode] Runtime type problem: Instruction type does not match stack map
	// Reference poly conditional in poly invocation context
	public void test010() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"import java.util.function.Function;\n" +
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(test(1, X::intToSome));\n" +
						"	}\n" +
						"	static <T> Some test(T value, Function<T, Some> f) {\n" +
						"		return id((value == null) ? new Nothing<>() : f.apply(value));\n" +
						"	}\n" +
						"	static <T> T id(T t) {\n" +
						"		return t;\n" +
						"	}\n" +
						"	static SomeInt intToSome(int i) {\n" +
						"		return new SomeInt();\n" +
						"	}\n" +
						"	static abstract class Some {}\n" +
						"	static class SomeInt extends Some {\n" +
						"	    public String toString() {\n" +
						"		return \"SomeInt instance\";\n" +
						"            }\n" +
						"        }\n" +
						"	static class Nothing<T> extends Some {}\n" +
						"}\n",
				},
				"SomeInt instance");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427207, - [1.8][bytecode] Runtime type problem: Instruction type does not match stack map
	// Reference poly conditional in assignment context, order reversed.
	public void test011() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"import java.util.function.Function;\n" +
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(test(1, X::intToSome));\n" +
						"	}\n" +
						"	static <T> Some test(T value, Function<T, Some> f) {\n" +
						"		return (value == null) ? f.apply(value) : new Nothing();\n" +
						"	}\n" +
						"	static SomeInt intToSome(int i) {\n" +
						"		return new SomeInt();\n" +
						"	}\n" +
						"	static abstract class Some {}\n" +
						"	static class SomeInt extends Some {\n" +
						"	    public String toString() {\n" +
						"			return \"SomeInt instance\";\n" +
						"        }\n" +
						"   }\n" +
						"	static class Nothing<T> extends Some {\n" +
						"	    public String toString() {\n" +
						"			return \"Nothing instance\";\n" +
						"       }\n" +
						"   }\n" +
						"}\n",
				},
				"Nothing instance");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427207, - [1.8][bytecode] Runtime type problem: Instruction type does not match stack map
	// Reference poly conditional in poly invocation context, order reversed.
	public void test012() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"import java.util.function.Function;\n" +
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(test(1, X::intToSome));\n" +
						"	}\n" +
						"	static <T> Some test(T value, Function<T, Some> f) {\n" +
						"		return id((value == null) ? f.apply(value) : new Nothing<>());\n" +
						"	}\n" +
						"	static <T> T id(T t) {\n" +
						"		return t;\n" +
						"	}\n" +
						"	static SomeInt intToSome(int i) {\n" +
						"		return new SomeInt();\n" +
						"	}\n" +
						"	static abstract class Some {}\n" +
						"	static class SomeInt extends Some {\n" +
						"	    public String toString() {\n" +
						"		return \"SomeInt instance\";\n" +
						"            }\n" +
						"        }\n" +
						"	static class Nothing<T> extends Some {\n" +
						"	    public String toString() {\n" +
						"			return \"Nothing instance\";\n" +
						"       }\n" +
						"   }\n" +
						"}\n",
				},
				"Nothing instance");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427207, - [1.8][bytecode] Runtime type problem: Instruction type does not match stack map
	// Reference poly conditional in poly invocation context, interface types
	public void test013() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"import java.util.function.Function;\n" +
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(test(1, X::intToSome));\n" +
						"	}\n" +
						"	static <T> Some test(T value, Function<T, Some> f) {\n" +
						"		return id((value == null) ? new Nothing<>() : f.apply(value));\n" +
						"	}\n" +
						"	static <T> T id(T t) {\n" +
						"		return t;\n" +
						"	}\n" +
						"	static SomeInt intToSome(int i) {\n" +
						"		return new SomeInt();\n" +
						"	}\n" +
						"	static interface Some {}\n" +
						"	static class SomeInt implements Some {\n" +
						"		public String toString() {\n" +
						"			return \"SomeInt instance\";\n" +
						"		}\n" +
						"	}\n" +
						"	static class Nothing<T> implements Some {}\n" +
						"}",
				},
				"SomeInt instance");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427438, - NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode
	public void test014() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public X(Class clazz) {\n" +
						"	}\n" +
						"	public void error() {\n" +
						"		boolean test = false;\n" +
						"		int i = 1;\n" +
						"		new X(test ? (i == 2 ? D.class : E.class) : null);\n" +
						"	}\n" +
						"	public class D {\n" +
						"	}\n" +
						"	public class E {\n" +
						"	}\n" +
						"}\n",
				},
				this.complianceLevel < ClassFileConstants.JDK1_5 ? "" :
					"----------\n" +
					"1. WARNING in X.java (at line 2)\n" +
					"	public X(Class clazz) {\n" +
					"	         ^^^^^\n" +
					"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427438, - NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode
	public void test015() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public X(Class clazz) {\n" +
						"	}\n" +
						"	public void error() {\n" +
						"		boolean test = false;\n" +
						"		int i = 1;\n" +
						"		new X(test ? null : (i == 2 ? D.class : E.class));\n" +
						"	}\n" +
						"	public class D {\n" +
						"	}\n" +
						"	public class E {\n" +
						"	}\n" +
						"}\n",
				},
				this.complianceLevel < ClassFileConstants.JDK1_5 ? "" :
					"----------\n" +
					"1. WARNING in X.java (at line 2)\n" +
					"	public X(Class clazz) {\n" +
					"	         ^^^^^\n" +
					"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427625, - NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode
	public void test427625() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		Map<String,String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.util.Collection;\n" +
						"import java.util.List;\n" +
						"public class X {\n" +
						"	public void error(Collection<Object> c) {\n" +
						"		boolean b  =true;\n" +
						"		c.add(b ? Integer.valueOf(1)\n" +
						"		        : c==null ? null \n" +
						"				  : c instanceof List ? Integer.valueOf(1) \n" +
						"				                      : o()); \n" +
						"	}\n" +
						"	public Object o() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}\n",
				},
				"",
				null, true, options);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432487,  NullPointerException during compilation using jdk1.8.0
	public void testBug432487() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class Y {\n" +
				"	String f() {\n" +
				"		return \"\";\n" +
				"	}\n" +
				"}\n" +
				"public class X {\n" +
				"void f(String x) {}\n" +
				"	void bar(Y y) {\n" +
				"		f(y.f2() == 1 ? null : y.f());\n" +
				"	}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	f(y.f2() == 1 ? null : y.f());\n" +
			"	    ^^\n" +
			"The method f2() is undefined for the type Y\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=437444#c113, - Error building JRE8
	public void test437444_c113() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X extends Y {\n" +
					"    public X(Z[] n) {\n" +
					"        super((n == null) ? null : n.clone());\n" +
					"    }\n" +
					"}\n" +
					"class Y  {\n" +
					"    public Y(Z[] notifications) {\n" +
					"    }\n" +
					"}\n" +
					"interface Z {}\n",
			},
			"");
	}
	public void test437444_2() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runNegativeTest(
			new String[] {
					"X.java",
					"public class X extends Y {\n" +
					"    public X(int[] n) {\n" +
					"        super((n == null) ? null : n.clone());\n" +
					"    }\n" +
					"}\n" +
					"class Y  {\n" +
					"    public Y(int[] notifications) {\n" +
					"    }\n" +
					"}\n" +
					"interface Z {}\n",
			},
			"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=484425: [bytecode] Bad type on operand stack - compiler omitted instructions for unboxing null Boolean
	public void test484425() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5)
			return;
		this.runConformTest(
				new String[] {
						"Main.java",
						"public class Main {\n" +
						"	public static void main(String[] args) {\n" +
						"		try {\n" +
						"			if ((false) ? true: null);\n" +
						"		} catch(NullPointerException npe) {\n" +
						"			System.out.println(\"Success\");\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				},
				"Success");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2677
	// JDT Core throws ClassCastException: NullTypeBinding cannot be cast to class ArrayBinding
	public void testIssue2677() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
						"Test.java",
						"public class Test {\n" +
						"    public static int test(int[] arr) {\n" +
						"        return (arr == null ? null : arr)[0];\n" +
						"    }\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(test(new int[] { 42 }));\n" +
						"	}\n" +
						"}\n"
				},
				"42");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2677
	// JDT Core throws ClassCastException: NullTypeBinding cannot be cast to class ArrayBinding
	public void testIssue2677_2() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
						"Test.java",
						"public class Test {\n" +
						"    public static int test(int[] arr) {\n" +
						"        return (arr != null ? arr : null)[0];\n" +
						"    }\n" +
						"	public static void main(String[] args) {\n" +
						"		System.out.println(test(new int[] { 42 }));\n" +
						"	}\n" +
						"}\n"
				},
				"42");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3042
	// java.util.EmptyStackException: null when invoking a static method on a null string literal in a ternary operator
	public void testIssue3042() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {

							static void parseFailure(X o) {
								(o != null ? o : null).bar();
							}

							static void bar() {
								System.out.println("Bar!");
							}

							public static void main(String[] args) {
								parseFailure(new X());
								parseFailure(null);
							}
						}
						"""
				},
				"Bar!\nBar!");
	}

	public void testIssue3042_2() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {

							void parseFailure(X o) {
								(o != null ? o : null).bar();
							}

							void bar() {
								System.out.println("Bar!");
							}

							public static void main(String[] args) {
								new X().parseFailure(new X());
								try {
									new X().parseFailure(null);
								} catch (NullPointerException npe) {
									System.out.println("NPE!");
								}
							}
						}
						"""
				},
				"Bar!\nNPE!");
	}

	public void testIssue3042_3() {
		if (this.complianceLevel < ClassFileConstants.JDK14)
			return;
		this.runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {

							void parseSuccess(X o) {
								(o == null ? null : o).bar();
								((X) null).bar();
							}

							static void bar() {
								System.out.println("Bar!");
							}

							public static void main(String[] args) {
								new X().parseSuccess(new X());
								new X().parseSuccess(null);
							}
						}
						"""
				},
				"Bar!\nBar!\nBar!\nBar!");
	}
}
