/*******************************************************************************
 * Copyright (c) 2013, 2016 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 425156 - [1.8] Lambda as an argument is flagged with incompatible error
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
@SuppressWarnings({ "rawtypes" })
public class OverloadResolutionTest8 extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test007"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public OverloadResolutionTest8(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
public static Class testClass() {
	return OverloadResolutionTest8.class;
}

public void test001() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int foo(int [] a);\n" +
				"}\n" +
				"interface J  {\n" +
				"	int foo(int a);\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo((a)->a.length));\n" +
				"	}\n" +
				"	static String foo(I i) {\n" +
				"		return(\"foo(I)\");\n" +
				"	}\n" +
				"	static String foo(J j) {\n" +
				"		return(\"foo(J)\");\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	System.out.println(foo((a)->a.length));\n" +
			"	                   ^^^\n" +
			"The method foo(I) is ambiguous for the type X\n" +
			"----------\n"
			);
}
public void test002() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"goo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"goo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		final boolean x = true;\n" +
				"		goo(()-> goo((I)null));\n" +
				"	}\n" +
				"	int f() {\n" +
				"		final boolean x = true;\n" +
				"		while (x);\n" +
				"	}\n" +
				"}\n",
			},
			"goo(I)");
}
public void test003() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   static final boolean f = true;\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"goo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		final boolean x = true;\n" +
				"		goo(()-> { \n" +
				"			final boolean y = true;\n" +
				"			while (y); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (x); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (f); \n" +
				"			});\n" +
				"	}\n" +
				"}\n",
			},
			"goo(J)\n" +
			"goo(J)\n" +
			"goo(J)");
}

public void test004() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   static boolean f = true;\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"goo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean x = true;\n" +
				"		goo(()-> { \n" +
				"			boolean y = true;\n" +
				"			while (y); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (x); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (f); \n" +
				"			});\n" +
				"	}\n" +
				"}\n",
			},
			// none of the lambdas is compatible because none is value-compatible, whereas foo() needs to return int.
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	goo(()-> { \n" +
			"	^^^\n" +
			"The method goo(J) in the type X is not applicable for the arguments (() -> {})\n" +
			"----------\n2. ERROR in X.java (at line 15)\n" +
			"	goo(()-> { \n" +
			"	^^^\n" +
			"The method goo(J) in the type X is not applicable for the arguments (() -> {})\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 18)\n" +
			"	goo(()-> { \n" +
			"	^^^\n" +
			"The method goo(J) in the type X is not applicable for the arguments (() -> {})\n" +
			"----------\n");
}
public void test005() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   final boolean f = true;\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"goo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		final boolean x = true;\n" +
				"		goo(()-> { \n" +
				"			final boolean y = true;\n" +
				"			while (y); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (x); \n" +
				"			});\n" +
				"		goo(()-> { \n" +
				"			while (f); \n" +
				"			});\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 18)\n" +
			"	goo(()-> { \n" +
			"	^^^\n" +
			"The method goo(J) in the type X is not applicable for the arguments (() -> {})\n" + // because lambda has errors -> not valueCompatible
			"----------\n" +
			"2. ERROR in X.java (at line 19)\n" +
			"	while (f); \n" +
			"	       ^\n" +
			"Cannot make a static reference to the non-static field f\n" +
			"----------\n");
}
public void test006() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" public static interface StringToInt {\n" +
				"  	int stoi(String s);\n" +
				" }\n" +
				" public static interface ReduceInt {\n" +
				"     int reduce(int a, int b);\n" +
				" }\n" +
				" void foo(StringToInt s) { }\n" +
				" void bar(ReduceInt r) { }\n" +
				" void bar() {\n" +
				"     bar((int x, int y) -> x+y); //SingleVariableDeclarations are OK\n" +
				"     foo(s -> s.length());\n" +
				"     foo((s) -> s.length());\n" +
				"     foo((String s) -> s.length()); //SingleVariableDeclaration is OK\n" +
				"     bar((x, y) -> x+y);\n" +
				" }\n" +
				"}\n",
			},
			"");
}
public void test007() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"goo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		goo(()-> 10); \n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	goo(()-> 10); \n" +
			"	^^^\n" +
			"The method goo(J) in the type X is not applicable for the arguments (() -> {})\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	goo(()-> 10); \n" +
			"	         ^^\n" +
			"Void methods cannot return a value\n" +
			"----------\n");
}
public void test008() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Object foo();\n" +
				"}\n" +
				"interface J  {\n" +
				"	String foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(()->null));\n" +
				"	}\n" +
				"	static String foo(I i) {\n" +
				"		return(\"foo(I)\");\n" +
				"	}\n" +
				"	static String foo(J j) {\n" +
				"		return(\"foo(J)\");\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test009() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Object foo();\n" +
				"}\n" +
				"interface J  {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(()-> {}));\n" +
				"	}\n" +
				"	static String foo(I i) {\n" +
				"		return(\"foo(I)\");\n" +
				"	}\n" +
				"	static String foo(J j) {\n" +
				"		return(\"foo(J)\");\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test010() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Object foo();\n" +
				"}\n" +
				"interface J  {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(()-> foo(()->null)));\n" +
				"	}\n" +
				"	static String foo(I i) {\n" +
				"		return(\"foo(I)\");\n" +
				"	}\n" +
				"	static String foo(J j) {\n" +
				"		return(\"foo(J)\");\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test011() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int foo();\n" +
				"}\n" +
				"interface J  {\n" +
				"	String foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(()-> \"Hello\" ));\n" +
				"	}\n" +
				"	static String foo(I i) {\n" +
				"		return(\"foo(I)\");\n" +
				"	}\n" +
				"	static String foo(J j) {\n" +
				"		return(\"foo(J)\");\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test012() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int foo();\n" +
				"}\n" +
				"interface J  {\n" +
				"	String foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(()-> 1234 ));\n" +
				"	}\n" +
				"	static String foo(I i) {\n" +
				"		return(\"foo(I)\");\n" +
				"	}\n" +
				"	static String foo(J j) {\n" +
				"		return(\"foo(J)\");\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test013() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int foo();\n" +
				"}\n" +
				"interface J  {\n" +
				"	Integer foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(foo(()-> 1234 ));\n" +
				"	}\n" +
				"	static String foo(I i) {\n" +
				"		return(\"foo(I)\");\n" +
				"	}\n" +
				"	static String foo(J j) {\n" +
				"		return(\"foo(J)\");\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test014() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				" \n" +
				"	static void foo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	\n" +
				"	static void foo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(()-> new Integer(10));\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test015() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"interface I {\n" +
				"	Integer foo();\n" +
				"}\n" +
				"public class X {\n" +
				" \n" +
				"	static void foo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	\n" +
				"	static void foo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(()-> new Integer(10));\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test016() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface O {\n" +
				"	Object foo();\n" +
				"}\n" +
				"interface S {\n" +
				"	String foo();\n" +
				"}\n" +
				"interface I {\n" +
				"	O foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	S foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(()-> ()-> \"String\");\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test017() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(()-> new Integer(10));\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test018() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	X [] foo(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I x) {\n" +
				"            System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	I i = X[]::new;\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(X[]::new);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test019() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I x) {\n" +
				"            System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	I i = X[]::new;\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(X[]::new);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}

public void test020() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Y foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	Y foo();\n" +
				"}\n" +
				"class Y {\n" +
				"	Y() {\n" +
				"	}\n" +
				"	\n" +
				"	Y(int x) {\n" +
				"	}\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(Y::new);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 20)\n" +
			"	foo(Y::new);\n" +
			"	^^^\n" +
			"The method foo(I) is ambiguous for the type X\n" +
			"----------\n");
}
public void test021() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Y foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	Y foo();\n" +
				"}\n" +
				"class Y {\n" +
				"	private Y() {\n" +
				"	}\n" +
				"	\n" +
				"	Y(int x) {\n" +
				"	}\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"       System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"       System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(Y::new);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test022() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Y foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	Y foo();\n" +
				"}\n" +
				"class Y {\n" +
				"	Y(float f) {\n" +
				"       System.out.println(\"Y(float)\");\n" +
				"	}\n" +
				"	\n" +
				"	Y(int x) {\n" +
				"       System.out.println(\"Y(int)\");\n" +
				"	}\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"       i.foo(10);\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"       j.foo();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(Y::new);\n" +
				"	}\n" +
				"}\n",
			},
			"Y(int)");
}
public void test023() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Y foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	Y foo();\n" +
				"}\n" +
				"class Y {\n" +
				"	Y(int ... x) {\n" +
				"	}\n" +
				"	\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(Y::new);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 18)\n" +
			"	foo(Y::new);\n" +
			"	^^^\n" +
			"The method foo(I) is ambiguous for the type X\n" +
			"----------\n");
}
public void test024() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Y foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	Y foo(int x);\n" +
				"}\n" +
				"class Y {\n" +
				"	Y(int x) {\n" +
				"	}\n" +
				"	\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(Y::new);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 18)\n" +
			"	foo(Y::new);\n" +
			"	^^^\n" +
			"The method foo(I) is ambiguous for the type X\n" +
			"----------\n");
}
public void test025() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Y foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	X foo(int x);\n" +
				"}\n" +
				"class Y extends X {\n" +
				"    Y(int x) {\n" +
				"    }\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"            System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"            System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(Y::new);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test026() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Y foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	X foo(int x);\n" +
				"}\n" +
				"class Y extends X {\n" +
				"    <T> Y(int x) {\n" +
				"    }\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"            System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"            System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(Y::new);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 19)\n" +
			"	foo(Y::new);\n" +
			"	^^^\n" +
			"The method foo(I) is ambiguous for the type X\n" +
			"----------\n");
}
public void test027() { // javac bug: 8b115 complains of ambiguity here.
	this.runConformTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.JavacDoesNotCompileCorrectSource,
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Y foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	X foo(int x);\n" +
				"}\n" +
				"class Y extends X {\n" +
				"    <T> Y(int x) {\n" +
				"    }\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"            System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"            System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(Y::<String>new);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test028() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Y [] foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	X [] foo();\n" +
				"}\n" +
				"class Y extends X {\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(Y []::new);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test029() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Y [] foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	X [] foo();\n" +
				"}\n" +
				"class Y extends X {\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(X []::new);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 17)\n" +
			"	foo(X []::new);\n" +
			"	^^^\n" +
			"The method foo(I) in the type X is not applicable for the arguments (X[]::new)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 17)\n" +
			"	foo(X []::new);\n" +
			"	    ^^^^^^^^^\n" +
			"Constructed array X[] cannot be assigned to Y[] as required in the interface descriptor  \n" +
			"----------\n");
}
public void test030() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Y [] foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	X [] foo(int x);\n" +
				"}\n" +
				"class Y extends X {\n" +
				"}\n" +
				"public class X {\n" +
				"	static void foo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void foo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(X []::new);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401850, [1.8][compiler] Compiler fails to type poly allocation expressions in method invocation contexts
public void test031() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	void foo(X<String> s) {\n" +
				"       System.out.println(\"foo(X<String>)\");\n" +
				"   }\n" +
				"	public static void main(String[] args) {\n" +
				"		new X<String>().foo(new X<>());\n" +
				"	}\n" +
				"}\n",
			},
			"foo(X<String>)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401850, [1.8][compiler] Compiler fails to type poly allocation expressions in method invocation contexts
// FAIL: we no longer see that both methods are applicable...
// inference starts with X#RAW, finds the second method, then infers the diamond to Object and sees that foo is not ambiguous
public void _test032() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"    void foo(X<String> s, Object o) {\n" +
				"        System.out.println(\"foo(X<String>)\");\n" +
				"    }\n" +
				"    void foo(X xs, String s) {\n" +
				"        System.out.println(\"foo(X<String>)\");\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"        new X<String>().foo(new X<>(), \"Hello\");\n" +
				"    }\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	void foo(X xs, String s) {\n" +
			"	         ^\n" +
			"X is a raw type. References to generic type X<T> should be parameterized\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	new X<String>().foo(new X<>(), \"Hello\");\n" +
			"	                ^^^\n" +
			"The method foo(X<String>, Object) is ambiguous for the type X<String>\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401850, [1.8][compiler] Compiler fails to type poly allocation expressions in method invocation contexts
public void test033() {
	this.runConformTest(
			new String[] {
				"X.java",
				"class Y<T> {}\n" +
				"public class X<T> extends Y<T> {\n" +
				"    void foo(X<String> s) {\n" +
				"        System.out.println(\"foo(X<String>)\");\n" +
				"    }\n" +
				"    void foo(Y<String> y) {\n" +
				"        System.out.println(\"foo(Y<String>)\");\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"        new X<String>().foo(new X<>());\n" +
				"    }\n" +
				"}\n",
			},
			"foo(X<String>)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422050, [1.8][compiler] Overloaded method call with poly-conditional expression rejected by the compiler
public void test422050() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I { \n" +
				"	int foo(); \n" +
				"}\n" +
				"interface J { \n" +
				"	double foo(); \n" +
				"}\n" +
				"public class X {\n" +
				"	static int foo(I i) {\n" +
				"		return 0;\n" +
				"	}\n" +
				"	static int foo(J j) {\n" +
				"		return 1;\n" +
				"	}\n" +
				"	public static void main(String argv[]) {\n" +
				"		System.out.println(foo (() -> true ? 0 : 1));\n" +
				"	}\n" +
				"}\n",
			},
			"0");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test400871() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	static int foo() {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(X::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test400871a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo(int x);\n" +
				"}\n" +
				"class Y {\n" +
				"	int foo(int y) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	int foo(int x) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test400871b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo(int x);\n" +
				"}\n" +
				"class Y {\n" +
				"	<T> int foo(int y) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	int foo(int x) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test400871c() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo(int x);\n" +
				"}\n" +
				"class Y {\n" +
				"	<T> int foo(String y) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	int foo(int x) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 23)\n" +
			"	goo(new X()::foo);\n" +
			"	^^^\n" +
			"The method goo(I) is ambiguous for the type X\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test400871d() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo(int x);\n" +
				"}\n" +
				"class Y {\n" +
				"	int foo(String y) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	<T> int foo(int x) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 23)\n" +
			"	goo(new X()::foo);\n" +
			"	^^^\n" +
			"The method goo(I) is ambiguous for the type X\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400871, [1.8][compiler] Overhaul overload resolution to reconcile with JLS8 15.12.2
public void test4008712() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo(int x);\n" +
				"}\n" +
				"class Y {\n" +
				"	int foo(String y) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	<T> int foo(int x) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 23)\n" +
			"	goo(new X()::foo);\n" +
			"	^^^\n" +
			"The method goo(I) is ambiguous for the type X\n" +
			"----------\n");
}
public void test4008712e() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo(int x);\n" +
				"}\n" +
				"class Y {\n" +
				"	int foo(int y) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712f() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo(int x);\n" +
				"}\n" +
				"class Y {\n" +
				"	int foo(int ... x) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 20)\n" +
			"	goo(new X()::foo);\n" +
			"	^^^\n" +
			"The method goo(I) is ambiguous for the type X\n" +
			"----------\n");
}
public void test4008712g() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo(int x);\n" +
				"}\n" +
				"class Y {\n" +
				"	private int foo(int x) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	private int foo(int x) {\n" +
			"	            ^^^^^^^^^^\n" +
			"The method foo(int) from the type Y is never used locally\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 20)\n" +
			"	goo(new X()::foo);\n" +
			"	^^^\n" +
			"The method goo(I) in the type X is not applicable for the arguments (new X()::foo)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 20)\n" +
			"	goo(new X()::foo);\n" +
			"	    ^^^^^^^^^^^^\n" +
			"The type X does not define foo(int) that is applicable here\n" +
			"----------\n");
}
public void test4008712h() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo(int x);\n" +
				"}\n" +
				"class Y {\n" +
				"	public <T> int foo(int x) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 20)\n" +
			"	goo(new X()::foo);\n" +
			"	^^^\n" +
			"The method goo(I) is ambiguous for the type X\n" +
			"----------\n");
}
public void test4008712i() { // javac bug: 8b115 complains of ambiguity here.
	this.runConformTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.JavacDoesNotCompileCorrectSource,
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo(int x);\n" +
				"}\n" +
				"class Y {\n" +
				"	public <T> int foo(int x) {\n" +
				"		 return 0;\n" +
				"	}\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::<String>foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712j() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(String x);\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo(String x);\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<T> {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X<String>()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test4008712k() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(String x);\n" +
				"}\n" +
				"interface J {\n" +
				"	String foo(String x);\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<T> {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X<String>()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712l() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(String x);\n" +
				"}\n" +
				"interface J {\n" +
				"	String foo(String x);\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X<String>()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712m() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(String x);\n" +
				"}\n" +
				"interface J {\n" +
				"	String foo(String x);\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"   public void foo() {}\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X<String>()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 21)\n" +
			"	goo(new X<String>()::foo);\n" +
			"	^^^\n" +
			"The method goo(I) is ambiguous for the type X<T>\n" +
			"----------\n");
}
public void test4008712n() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(String x);\n" +
				"}\n" +
				"interface J {\n" +
				"	String foo(String x);\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"   public String foo(String s) { return null; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X<String>()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712o() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(String x);\n" +
				"}\n" +
				"interface J {\n" +
				"	String foo(String x);\n" +
				"}\n" +
				"interface K<T> {\n" +
				"	public T foo(T x);\n" +
				"}\n" +
				"class Y<T> implements K {\n" +
				"	public Object foo(Object x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"   public Object foo(Object s) { return null; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X<String>()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test4008712p() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(String x);\n" +
				"}\n" +
				"interface J {\n" +
				"	String foo(String x);\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"   public String foo(String s) { return null; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 21)\n" +
			"	goo(new X()::foo);\n" +
			"	^^^\n" +
			"The method goo(I) is ambiguous for the type X<T>\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 21)\n" +
			"	goo(new X()::foo);\n" +
			"	        ^\n" +
			"X is a raw type. References to generic type X<T> should be parameterized\n" +
			"----------\n");
}
public void test4008712q_raw() {
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(String x);\n" +
				"}\n" +
				"interface J {\n" +
				"	String foo(String x);\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 20)\n" +
			"	goo(new X()::foo);\n" +
			"	^^^\n" +
			"The method goo(I) is ambiguous for the type X<T>\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 20)\n" +
			"	goo(new X()::foo);\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Type safety: The method foo(Object) belongs to the raw type Y. References to generic type Y<T> should be parameterized\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 20)\n" +
			"	goo(new X()::foo);\n" +
			"	        ^\n" +
			"X is a raw type. References to generic type X<T> should be parameterized\n" +
			"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.JavacCompilesIncorrectSource;
	runner.runNegativeTest();
}
public void test4008712q_diamond() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(String x);\n" +
				"}\n" +
				"interface J {\n" +
				"	String foo(String x);\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X<>()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712r() {
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	String foo();\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X[0]::clone);\n" +
				"	}\n" +
				"}\n",
			};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 20)\n" +
			"	goo(new X[0]::clone);\n" +
			"	^^^\n" +
			"The method goo(I) is ambiguous for the type X<T>\n" +
			"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.JavacCompilesIncorrectSource;
	runner.runNegativeTest();
}
public void test4008712s() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	String foo();\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X[0]::toString);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712t() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Class foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	Object foo();\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X[0]::getClass);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test4008712u() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(I::clone);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 20)\n" +
			"	goo(I::clone);\n" +
			"	^^^\n" +
			"The method goo(I) in the type X<T> is not applicable for the arguments (I::clone)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 20)\n" +
			"	goo(I::clone);\n" +
			"	    ^^^^^^^^\n" +
			"The type I does not define clone() that is applicable here\n" +
			"----------\n");
}
public void test4008712v() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"       I i = () -> {};\n" +
				"		goo(i::hashCode);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712w() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"       I i = () -> {};\n" +
				"		goo(i::clone);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 21)\n" +
			"	goo(i::clone);\n" +
			"	^^^\n" +
			"The method goo(I) in the type X<T> is not applicable for the arguments (i::clone)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 21)\n" +
			"	goo(i::clone);\n" +
			"	    ^^^^^^^^\n" +
			"The type I does not define clone() that is applicable here\n" +
			"----------\n");
}
public void test4008712x() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo(String x);\n" +
				"}\n" +
				"interface J {\n" +
				"	String foo(String x);\n" +
				"}\n" +
				"class Y<T> {\n" +
				"	public T foo(T x) {\n" +
				"		 return null;\n" +
				"	}\n" +
				"   private void foo() {}\n" +
				"}\n" +
				"public class X<T> extends Y<String> {\n" +
				"   public String foo(String s) { return null; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X<String>()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712y() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   public int foo() { return 0; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712z() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	long foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   public int foo() { return 0; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712za() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	long foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	int foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   public long foo() { return 0; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test4008712zb() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   public long foo() { return 0; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 16)\n" +
			"	goo(new X()::foo);\n" +
			"	^^^\n" +
			"The method goo(I) is ambiguous for the type X\n" +
			"----------\n");
}
public void test4008712zc() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	Integer foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   public long foo() { return 0; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 16)\n" +
			"	goo(new X()::foo);\n" +
			"	^^^\n" +
			"The method goo(I) in the type X is not applicable for the arguments (new X()::foo)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 16)\n" +
			"	goo(new X()::foo);\n" +
			"	    ^^^^^^^^^^^^\n" +
			"The type of foo() from the type X is long, this is incompatible with the descriptor\'s return type: int\n" +
			"----------\n");
}
public void test4008712zd() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	Long foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   public long foo() { return 0; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712ze() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	Integer foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   public int foo() { return 0; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test4008712zf() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	int foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	Integer foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   public Integer foo() { return 0; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void test4008712zg() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	Long foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   public Integer foo() { return 0; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(I)");
}
public void test4008712zh() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	Integer foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	Long foo();\n" +
				"}\n" +
				"public class X {\n" +
				"   public Long foo() { return 0L; }\n" +
				"	static void goo(I i) {\n" +
				"		System.out.println(\"foo(I)\");\n" +
				"	}\n" +
				"	static void goo(J j) {\n" +
				"		System.out.println(\"foo(J)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) { \n" +
				"		goo(new X()::foo);\n" +
				"	}\n" +
				"}\n",
			},
			"foo(J)");
}
public void testVarargs() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	static void goo(I ... i) {\n" +
				"		i[0].foo();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		goo(()->{ System.out.println(\"Lambda\");});\n" +
				"	}\n" +
				"}\n",
			},
			"Lambda");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401850,  [1.8][compiler] Compiler fails to type poly allocation expressions in method invocation contexts
public void test401850() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	void foo(X<String> s) {\n" +
				"		System.out.println(\"foo(X<String>)\");\n" +
				"	}\n" +
				"	void foo(int x) {\n" +
				"		System.out.println(\"foo(int)\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X<String>().foo(new X<>());\n" +
				"	}\n" +
				"}\n",
			},
			"foo(X<String>)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427072,  [1.8][compiler] Regression since fix of bug 423505: Method is ambiguous for type X
public void test427072() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"    Object m(X t);\n" +
				"}\n" +
				"interface J extends I {\n" +
				"}\n" +
				"public class X {\n" +
				"    int foo()  { return 0; }\n" +
				"    int test() {\n" +
				"        return foo(X::foo);\n" +
				"    }\n" +
				"    int foo(I i) {return 0;}\n" +
				"    int foo(J j) { return 1;}\n" +
				"    public static void main(String args[]) {\n" +
				"        X x = new X();\n" +
				"        int i = x.test();\n" +
				"        System.out.println(i);\n" +
				"    }\n" +
				"}\n",
			},
			"1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427072,  [1.8][compiler] Regression since fix of bug 423505: Method is ambiguous for type X
public void test427072a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"    Object m(X t);\n" +
				"}\n" +
				"interface J extends I {\n" +
				"}\n" +
				"public class X {\n" +
				"    int foo()  { return 0; }\n" +
				"    int test() {\n" +
				"        return foo((x) -> x);\n" +
				"    }\n" +
				"    int foo(I i) {return 0;}\n" +
				"    int foo(J j) { return 1;}\n" +
				"    public static void main(String args[]) {\n" +
				"        X x = new X();\n" +
				"        int i = x.test();\n" +
				"        System.out.println(i);\n" +
				"    }\n" +
				"}\n",
			},
			"1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427072,  [1.8][compiler] Regression since fix of bug 423505: Method is ambiguous for type X
public void test427072b() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"    Object m(X t);\n" +
				"}\n" +
				"interface J extends I {\n" +
				"}\n" +
				"public class X {\n" +
				"    int foo()  { return 0; }\n" +
				"    int test() {\n" +
				"        return foo(true ? (x) -> x : X::foo);\n" +
				"    }\n" +
				"    int foo(I i) {return 0;}\n" +
				"    int foo(J j) { return 1;}\n" +
				"    public static void main(String args[]) {\n" +
				"        X x = new X();\n" +
				"        int i = x.test();\n" +
				"        System.out.println(i);\n" +
				"    }\n" +
				"}\n",
			},
			"1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427072,  [1.8][compiler] Regression since fix of bug 423505: Method is ambiguous for type X
public void test427072c() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"    Object m(X t);\n" +
				"}\n" +
				"interface J extends I {\n" +
				"}\n" +
				"public class X {\n" +
				"    int foo1()  { return 0; }\n" +
				"    int foo2()  { return 0; }\n" +
				"    int test() {\n" +
				"        return foo(true ? X::foo1 : X::foo2);\n" +
				"    }\n" +
				"    int foo(I i) {return 0;}\n" +
				"    int foo(J j) { return 1;}\n" +
				"    public static void main(String args[]) {\n" +
				"        X x = new X();\n" +
				"        int i = x.test();\n" +
				"        System.out.println(i);\n" +
				"    }\n" +
				"}\n",
			},
			"1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427628,  regression : The method * is ambiguous for the type *
public void test427628() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   public static void main(String [] args) {\n" +
				"       new X().error(null);\n" +
				"   }\n" +
				"	public void error(I i) {\n" +
				"		test(i!=null?i.getJ():null);\n" +
				"	}\n" +
				"	public void test(I i) {\n" +
				"       System.out.println(\"I\");\n" +
				"	}\n" +
				"	public void test(J j) {\n" +
				"       System.out.println(\"J\" + j);\n" +
				"	}\n" +
				"	public class I{\n" +
				"		public J getJ() {\n" +
				"			return null;\n" +
				"		}\n" +
				"	}\n" +
				"	public class J{}\n" +
				"}\n",
			},
			"Jnull");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427628,  regression : The method * is ambiguous for the type *
public void test427628a() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   public static void main(String [] args) {\n" +
				"       new X().error(null);\n" +
				"   }\n" +
				"	public void error(I i) {\n" +
				"		test(i!=null?i.getJ():null);\n" +
				"	}\n" +
				"	public void test(I i) {\n" +
				"       System.out.println(\"I\");\n" +
				"	}\n" +
				"	public void test(K k) {\n" +
				"       System.out.println(\"K\" + j);\n" +
				"	}\n" +
				"	public class I{\n" +
				"		public J getJ() {\n" +
				"			return null;\n" +
				"		}\n" +
				"	}\n" +
				"	public class J{}\n" +
				"	public class K{}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	test(i!=null?i.getJ():null);\n" +
			"	^^^^\n" +
			"The method test(X.I) in the type X is not applicable for the arguments (((i != null) ? i.getJ() : null))\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	test(i!=null?i.getJ():null);\n" +
			"	             ^^^^^^^^\n" +
			"Type mismatch: cannot convert from X.J to X.I\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 12)\n" +
			"	System.out.println(\"K\" + j);\n" +
			"	                         ^\n" +
			"j cannot be resolved to a variable\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427628,  regression : The method * is ambiguous for the type *
public void test427628b() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void setSetting(String key, String value) {\n" +
				"	}\n" +
				"	public void setSetting(String key, Integer value) {\n" +
				"	    setSetting(key, value == null ? null : Integer.toString(value));\n" +
				"	}\n" +
				"}\n",
			},
			"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421922, [1.8][compiler] Varargs & Overload - Align to JLS8
public void _test421922() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.Arrays;\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        test(1);\n" +
				"    }\n" +
				"    public static void test(int... a) {\n" +
				"        System.out.print(\"int ... = \");\n" +
				"        System.out.println(Arrays.toString(a));\n" +
				"    }\n" +
				"    public static <T> void test(Object... a) {\n" +
				"        System.out.print(\"Object ... = \");\n" +
				"        System.out.println(Arrays.toString(a));\n" +
				"    }\n" +
				"}\n",
			},
			"int ... = [1]");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427748, [1.8][compiler] Cannot convert from Boolean to boolean on generic return type
public void test427748() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String [] args) {\n" +
				"    getLog(doit(baction));\n" +
				"  }\n" +
				"  private static interface Action<T> {T run();}\n" +
				"  private static Action<Boolean> baction = () -> true;\n" +
				"  static void getLog(int override) {}\n" +
				"  static void getLog(boolean override) {\n" +
				"      System.out.println(\"OK\");\n" +
				"  }\n" +
				"  private static <T> T doit(Action<T> action) { return action.run(); }\n" +
				"}\n",
			},
			"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427808, [1.8] Correct super() invocation is not inferred when argument is a conditional expression
public void test427808() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends Foo {\n" +
				"	public X(I i) {\n" +
				"		super(i != null ?  i.toString() : null);\n" +
				"    }\n" +
				"   public static void main(String [] args) {\n" +
				"       new X(null);\n" +
				"   }\n" +
				"}\n" +
				"class Foo implements I {\n" +
				"	Foo(I i) {}\n" +
				"	Foo(String string){ System.out.println(\"OK\"); }\n" +
				"}\n" +
				"interface I {}\n",
			},
			"OK");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429985,  [1.8][compiler] Resolution of right method signature
public void test429985() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.function.Supplier;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		// This does not compile with ECJ\n" +
				"		test(() -> \"hi\");\n" +
				"	}\n" +
				"	// Note: when removing this code the main function compiles with ECJ\n" +
				"	static void test(String message) {\n" +
				"	}\n" +
				"	static void test(Supplier<String> messageSupplier) {\n" +
				"       System.out.println(messageSupplier.get());\n" +
				"	}\n" +
				"}\n",
			},
			"hi");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429985,  [1.8][compiler] Resolution of right method signature
public void test429985a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.function.Supplier;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		// This does not compile with ECJ\n" +
				"		test(() -> \"hi\");\n" +
				"	}\n" +
				"	static void test(Supplier<String> messageSupplier) {\n" +
				"       System.out.println(messageSupplier.get());\n" +
				"	}\n" +
				"	// Note: when removing this code the main function compiles with ECJ\n" +
				"	static void test(String message) {\n" +
				"	}\n" +
				"}\n",
			},
			"hi");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448801, [1.8][compiler] Scope.mSMB & 15.12.3 Compile-Time Step 3
public void test448801() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	private class Y {\n" +
				"	}\n" +
				"	public X(Y ...ys) {\n" +
				"	}\n" +
				"	public void foo(Y ...ys) {\n" +
				"	}\n" +
				"	public void goo() {\n" +
				"	}\n" +
				"}\n",
				"Z.java",
				"interface I {\n" +
				"	static void ifoo() {\n" +
				"	}\n" +
				"}\n" +
				"abstract class ZSuper {\n" +
				"	void zSuperFoo() {\n" +
				"	}\n" +
				"	abstract void goo();\n" +
				"}\n" +
				"public class Z extends ZSuper implements I {\n" +
				"	void goo() {\n" +
				"		super.zSuperFoo();\n" +
				"		super.goo();\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X x = new X();\n" +
				"		x.foo();\n" +
				"		System.out.println(x.goo());\n" +
				"		goo();\n" +
				"		Z.goo();\n" +
				"		zoo();\n" +
				"		new Z().ifoo();\n" +
				"		super.zSuperFoo();\n" +
				"	}\n" +
				"	class ZZ {\n" +
				"		class ZZZ {\n" +
				"			void zoo() {\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Z.java (at line 13)\n" +
			"	super.goo();\n" +
			"	^^^^^^^^^^^\n" +
			"Cannot directly invoke the abstract method goo() for the type ZSuper\n" +
			"----------\n" +
			"2. ERROR in Z.java (at line 16)\n" +
			"	X x = new X();\n" +
			"	      ^^^^^^^\n" +
			"The constructor X(X.Y...) of type X is not applicable as the formal varargs element type X.Y is not accessible here\n" +
			"----------\n" +
			"3. ERROR in Z.java (at line 17)\n" +
			"	x.foo();\n" +
			"	  ^^^\n" +
			"The method foo(X.Y...) of type X is not applicable as the formal varargs element type X.Y is not accessible here\n" +
			"----------\n" +
			"4. ERROR in Z.java (at line 18)\n" +
			"	System.out.println(x.goo());\n" +
			"	           ^^^^^^^\n" +
			"The method println(boolean) in the type PrintStream is not applicable for the arguments (void)\n" +
			"----------\n" +
			"5. ERROR in Z.java (at line 19)\n" +
			"	goo();\n" +
			"	^^^\n" +
			"Cannot make a static reference to the non-static method goo() from the type Z\n" +
			"----------\n" +
			"6. ERROR in Z.java (at line 20)\n" +
			"	Z.goo();\n" +
			"	^^^^^^^\n" +
			"Cannot make a static reference to the non-static method goo() from the type Z\n" +
			"----------\n" +
			"7. ERROR in Z.java (at line 21)\n" +
			"	zoo();\n" +
			"	^^^\n" +
			"The method zoo() is undefined for the type Z\n" +
			"----------\n" +
			"8. ERROR in Z.java (at line 22)\n" +
			"	new Z().ifoo();\n" +
			"	        ^^^^\n" +
			"The method ifoo() is undefined for the type Z\n" +
			"----------\n" +
			"9. ERROR in Z.java (at line 23)\n" +
			"	super.zSuperFoo();\n" +
			"	^^^^^\n" +
			"Cannot use super in a static context\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450415, [1.8][compiler] Failure to resolve overloaded call.
public void test450415() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"interface I {\n" +
				"	String foo();\n" +
				"}\n" +
				"interface J {\n" +
				"	List<String> foo();\n" +
				"}\n" +
				"public class X {\n" +
				"    static void goo(I i) {\n" +
				"    	System.out.println(\"goo(I)\");\n" +
				"    }\n" +
				"    static void goo(J j) {\n" +
				"    	System.out.println(\"goo(J)\");\n" +
				"    }\n" +
				"    static <T> List<T> loo() {\n" +
				"    	return null;\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"		goo(()->loo());\n" +
				"	}\n" +
				"}\n"
			},
			"goo(J)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450415, [1.8][compiler] Failure to resolve overloaded call.
public void test450415a() {
	this.runConformTest(
			new String[] {
				"X.java",
				"interface I {\n" +
				"	void foo();\n" +
				"}\n" +
				"public class X {\n" +
				"	static <T> void foo() {\n" +
				"		class Y {\n" +
				"			void goo(T t) {\n" +
				"				System.out.println(\"T\");\n" +
				"			}\n" +
				"			void goo(I i) {\n" +
				"				System.out.println(\"I\");\n" +
				"			}\n" +
				"		}\n" +
				"		new Y().goo(()->{});\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		foo();\n" +
				"	}\n" +
				"}\n"
			},
			"I");
}
public void test482440a() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"class Test {\n" +
			"\n" +
			"    // generic method\n" +
			"    interface ConsumerA {\n" +
			"        <T> void accept(int i);\n" +
			"    }\n" +
			"\n" +
			"    // non-generic\n" +
			"    interface ConsumerB {\n" +
			"        void accept(int i);\n" +
			"    }\n" +
			"\n" +
			"    // A before B\n" +
			"    void execute1(ConsumerA c) {}\n" +
			"    void execute1(ConsumerB c) {}\n" +
			"\n" +
			"    // B before A\n" +
			"    void execute2(ConsumerB c) {}\n" +
			"    void execute2(ConsumerA c) {}\n" +
			"\n" +
			"    void test() {\n" +
			"        execute1(x -> {});  // compiles in Eclipse\n" +
			"        execute2(x -> {});  // doesn't compile\n" +
			"    }\n" +
			"\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 22)\n" +
		"	execute1(x -> {});  // compiles in Eclipse\n" +
		"	^^^^^^^^\n" +
		"The method execute1(Test.ConsumerA) is ambiguous for the type Test\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 23)\n" +
		"	execute2(x -> {});  // doesn\'t compile\n" +
		"	^^^^^^^^\n" +
		"The method execute2(Test.ConsumerB) is ambiguous for the type Test\n" +
		"----------\n");
}
public void test482440b() {
	runConformTest(
		new String[] {
			"Test.java",
			"class Test {\n" +
			"\n" +
			"    // generic method\n" +
			"    interface ConsumerA {\n" +
			"        <T> void accept(int i);\n" +
			"    }\n" +
			"\n" +
			"    // non-generic\n" +
			"    interface ConsumerB {\n" +
			"        void accept(int i);\n" +
			"    }\n" +
			"\n" +
			"    // A before B\n" +
			"    void execute1(ConsumerA c) {}\n" +
			"    void execute1(ConsumerB c) {}\n" +
			"\n" +
			"    // B before A\n" +
			"    void execute2(ConsumerB c) {}\n" +
			"    void execute2(ConsumerA c) {}\n" +
			"\n" +
			"    void test() {\n" +
			"        execute1((int x) -> {});  // compiles in Eclipse\n" +
			"        execute2((int x) -> {});  // doesn't compile\n" +
			"    }\n" +
			"\n" +
			"}\n"
		});
}
}
