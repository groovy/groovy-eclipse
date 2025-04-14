/*******************************************************************************
 * Copyright (c) 2011, 2016 IBM Corporation.
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
 *								Bug 424205 - [1.8] Cannot infer type for diamond type with lambda on method invocation
 *								Bug 429203 - [1.8][compiler] NPE in AllocationExpression.binding
 *								Bug 456508 - Unexpected RHS PolyTypeBinding for: <code-snippet>
 *								Bug 462083 - [1.8][inference] Java 8 generic return type mismatch with interface involving type parameter.
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GenericsRegressionTest_1_7 extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testBug456508" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public GenericsRegressionTest_1_7(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
}
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String> x = new X<>();\n" +
			"		x.testFunction(\"SUCCESS\");\n" +
			"	}\n" +
			"	public void testFunction(T param){\n" +
			"		System.out.println(param);\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
public void test001a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String> x = new X<>();\n" +
			"		x.testFunction(1);\n" +
			"	}\n" +
			"	public void testFunction(T param){\n" +
			"		System.out.println(param);\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	x.testFunction(1);\n" +
		"	  ^^^^^^^^^^^^\n" +
		"The method testFunction(String) in the type X<String> is not applicable for the arguments (int)\n" +
		"----------\n");
}
public void test001b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public static void main(String[] args) {\n" +
			"		java.util.ArrayList<String> x = new java.util.ArrayList<>();\n" +
			"		x.add(\"\");\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
// fields
public void test001b_1() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	static java.util.ArrayList<String> x = new java.util.ArrayList<>();\n" +
			"	public static void main(String[] args) {\n" +
			"		X.x.add(\"\");\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
public void test001c() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public static void main(String[] args) {\n" +
			"		java.util.ArrayList<String> x = new java.util.ArrayList<>();\n" +
			"		x.add(1);\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	x.add(1);\n" +
		"	  ^^^\n" +
		"The method add(int, String) in the type ArrayList<String> is not applicable for the arguments (int)\n" +
		"----------\n");
}
// fields
public void test001c_1() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	static java.util.ArrayList<String> x = new java.util.ArrayList<>();\n" +
			"	public static void main(String[] args) {\n" +
			"		X.x.add(1);\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	X.x.add(1);\n" +
		"	    ^^^\n" +
		"The method add(int, String) in the type ArrayList<String> is not applicable for the arguments (int)\n" +
		"----------\n");
}
public void test001d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"public class X<T> {" +
				"	public void ab(ArrayList<String> al){\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X<String> x = new X<>();\n" +
				"		x.ab(new ArrayList<>());\n" +
				"	}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	x.ab(new ArrayList<>());\n" +
			"	  ^^\n" +
			"The method ab(ArrayList<String>) in the type X<String> is not applicable for the arguments (ArrayList<Object>)\n" +
			"----------\n");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.util.ArrayList;\n" +
					"public class X<T> {" +
					"	public void ab(ArrayList<String> al){\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		X<String> x = new X<>();\n" +
					"		x.ab(new ArrayList<>());\n" +
					"	}\n" +
					"}",
				},
				"SUCCESS");
	}
}
public void test001e() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"public class X<T> {" +
				"	public void ab(ArrayList<T> al){\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X<String> x = new X<>();\n" +
				"		x.ab(new ArrayList<>());\n" +
				"	}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	x.ab(new ArrayList<>());\n" +
			"	  ^^\n" +
			"The method ab(ArrayList<String>) in the type X<String> is not applicable for the arguments (ArrayList<Object>)\n" +
			"----------\n");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.util.ArrayList;\n" +
					"public class X<T> {" +
					"	public void ab(ArrayList<T> al){\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		X<String> x = new X<>();\n" +
					"		x.ab(new ArrayList<>());\n" +
					"	}\n" +
					"}",
				},
				"SUCCESS");
	}
}
public void test001f() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	class X2<T>{\n" +
			"		void methodx(T param){\n" +
			"			System.out.println(param);\n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String>.X2<String> x = new X<>().new X2<>();\n" +
			"		x.methodx(\"SUCCESS\");\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	class X2<T>{\n" +
		"	         ^\n" +
		"The type parameter T is hiding the type T\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	X<String>.X2<String> x = new X<>().new X2<>();\n" +
		"	                         ^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.X2<String> to X<String>.X2<String>\n"
		:
		"Cannot infer type arguments for X2<>\n"
		) +
		"----------\n");
}
// fields
public void test001f_1() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	class X2<T>{\n" +
			"		void methodx(T param){\n" +
			"			System.out.println(param);\n" +
			"		}\n" +
			"	}\n" +
			"	X<String>.X2<String> x;\n" +
			"	public static void main(String[] args) {\n" +
			"		X test = new X();\n" +
			"		test.x = new X<>().new X2<>();\n" +
			"		test.x.methodx(\"SUCCESS\");\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
public void test001g() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	class X2<K>{\n" +
			"		void methodx(T param, K param2){\n" +
			"			System.out.println(param);\n" +
			"			System.out.println(param2);\n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String>.X2<Integer> x = new X<>().new X2<>();\n" +
			"		x.methodx(\"SUCCESS\",1);\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	X<String>.X2<Integer> x = new X<>().new X2<>();\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.X2<Integer> to X<String>.X2<Integer>\n"
		:
		"Cannot infer type arguments for X2<>\n"
		) +
		"----------\n");
}
public void test001g_1() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	class X2<K>{\n" +
			"		void methodx(T param, K param2){\n" +
			"			System.out.println(param);\n" +
			"			System.out.println(param2);\n" +
			"		}\n" +
			"	}\n" +
			"	X<String>.X2<Integer> x;\n" +
			"	public static void main(String[] args) {\n" +
			"		X test = new X();" +
			"		test.x = new X<>().new X2<>();\n" +
			"		test.x.methodx(\"SUCCESS\",1);\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS\n1");
}
public void test001h() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	class X2<T>{\n" +
			"		void methodx(T param){\n" +
			"			System.out.println(param);\n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String>.X2<String> x = new X<>().new X2<>();\n" +
			"		x.methodx(1);\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	class X2<T>{\n" +
		"	         ^\n" +
		"The type parameter T is hiding the type T\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	X<String>.X2<String> x = new X<>().new X2<>();\n" +
		"	                         ^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.X2<String> to X<String>.X2<String>\n"
		:
		"Cannot infer type arguments for X2<>\n"
		) +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	x.methodx(1);\n" +
		"	  ^^^^^^^\n" +
		"The method methodx(String) in the type X<String>.X2<String> is not applicable for the arguments (int)\n" +
		"----------\n");
}
public void test001h_1() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	class X2<T>{\n" +
			"		void methodx(T param){\n" +
			"			System.out.println(param);\n" +
			"		}\n" +
			"	}\n" +
			"	X<String>.X2<String> x;\n" +
			"	public static void main(String[] args) {\n" +
			"		X test = new X();\n" +
			"		test.x = new X<>().new X2<>();\n" +
			"		test.x.methodx(1);\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	class X2<T>{\n" +
		"	         ^\n" +
		"The type parameter T is hiding the type T\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 9)\n" +
		"	X test = new X();\n" +
		"	^\n" +
		"X is a raw type. References to generic type X<T> should be parameterized\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 9)\n" +
		"	X test = new X();\n" +
		"	             ^\n" +
		"X is a raw type. References to generic type X<T> should be parameterized\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 10)\n" +
		"	test.x = new X<>().new X2<>();\n" +
		"	     ^\n" +
		"Type safety: The field x from the raw type X is assigned a value of type X<Object>.X2<Object>. References to generic type X<T> should be parameterized\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 11)\n" +
		"	test.x.methodx(1);\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The method methodx(Object) belongs to the raw type X.X2. References to generic type X<T>.X2<T> should be parameterized\n" +
		"----------\n");
}
public void test001h_2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	class X2<T>{\n" +
			"		void methodx(T param){\n" +
			"			System.out.println(param);\n" +
			"		}\n" +
			"	}\n" +
			"	X<String>.X2<String> x;\n" +
			"	public static void main(String[] args) {\n" +
			"		X test = new X();\n" +
			"		test.x = new X<>().new X2<>();\n" +
			"		test.x.methodx(1);\n" +
			"	}\n" +
			"}",
		},
		"1");
}
public void test001i() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	class X2<K>{\n" +
			"		class X22<I>{\n" +
			"			void methodx(T param, K param2, I param3){\n" +
			"				System.out.println(param);\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String> test = new X<>();" +
			"		X<String>.X2<Integer>.X22<X<String>> x = new X<>().new X2<>().new X22<>();\n" +
			"		x.methodx(\"SUCCESS\", 1, test);\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	X<String> test = new X<>();		X<String>.X2<Integer>.X22<X<String>> x = new X<>().new X2<>().new X22<>();\n" +
		"	                           		                                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.X2<Object>.X22<X<String>> to X<String>.X2<Integer>.X22<X<String>>\n"
		:
		"Cannot infer type arguments for X22<>\n"
		) +
		"----------\n");
}
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X<>();\n" +
			"		x.testFunction(\"SUCCESS\");\n" +
			"	}\n" +
			"	public void testFunction(T param){\n" +
			"		System.out.println(param);\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	X x = new X<>();\n" +
		"	^\n" +
		"X is a raw type. References to generic type X<T> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	x.testFunction(\"SUCCESS\");\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The method testFunction(Object) belongs to the raw type X. References to generic type X<T> should be parameterized\n" +
		"----------\n");
}
public void test003() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public static void main(String[] args) {\n" +
			"		new X<>().testFunction(\"SUCCESS\");\n" +
			"	}\n" +
			"	public void testFunction(T param){\n" +
			"		System.out.println(param);\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}

public void test004b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	class X2<U> {\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X<>().new X2<>(){\n" +
			"			void newMethod(){\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"}",
		},
		this.complianceLevel < ClassFileConstants.JDK9 ?
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	new X<>().new X2<>(){\n" +
		"	              ^^\n" +
		"\'<>\' cannot be used with anonymous classes\n" +
		"----------\n":
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	void newMethod(){\n" +
			"	     ^^^^^^^^^^^\n" +
			"The method newMethod() of type new X<Object>.X2<Object>(){} must override or implement a supertype method\n" +
			"----------\n");
}
public void test004c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	class X2<U> {\n" +
			"		U f1;" +
			"		public void setF(U a){\n" +
			"			this.f1 = a;" +
			"			System.out.println(this.f1);\n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X<>().new X2<Integer>(){\n" +
			"			void newMethod(){\n" +
			"			}\n" +
			"		}.setF(1);\n" +
			"	}\n" +
			"}",
		},
		"1");
}

public void test006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X1<T> {\n" +
			"	int abc = 1;\n" +
			"	public void testFunction(T param){\n" +
			"		System.out.println(param + \"X1\");\n" +
			"	}\n" +
			"}\n" +
			"public class X<T> extends X1<T> {\n" +
			"	public static void main(String[] args) {\n" +
			"		X1<String> x = new X<>();\n" +
			"		x.testFunction(\"SUCCESS\");\n" +
			"	}\n" +
			"	public void testFunction(T param){\n" +
			"		System.out.println(param);\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
// shows the difference between using <> and the raw type - different semantics
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	T field1;" +
			"	public X(T param){\n" +
			"		field1 = param;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X.testFunction(new X<>(\"hello\").getField());\n" + // prints 1
			"		X.testFunction(new X(\"hello\").getField());\n" + //prints 2
			"	}\n" +
			"	public static void testFunction(String param){\n" +
			"		System.out.println(1);\n" +
			"	}\n" +
			"	public static void testFunction(Object param){\n" +
			"		System.out.println(2);\n" +
			"	}\n" +
			"	public T getField(){\n" +
			"		return field1;" +
			"	}\n" +
			"}",
		},
		"1\n" +
		"2");
}
public void test007a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	public X(){\n" +
				"	}\n" +
				"	public X(T param){\n" +
				"		System.out.println(param);\n" +
				"	}\n" +
				"	public static void testFunction(X<String> param){\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		X.testFunction(new X<>());\n" +
				"		X.testFunction(new X(\"hello\"));\n" +
				"	}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	X.testFunction(new X<>());\n" +
			"	  ^^^^^^^^^^^^\n" +
			"The method testFunction(X<String>) in the type X is not applicable for the arguments (X<Object>)\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 12)\n" +
			"	X.testFunction(new X(\"hello\"));\n" +
			"	               ^^^^^^^^^^^^^^\n" +
			"Type safety: The constructor X(Object) belongs to the raw type X. References to generic type X<T> should be parameterized\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 12)\n" +
			"	X.testFunction(new X(\"hello\"));\n" +
			"	               ^^^^^^^^^^^^^^\n" +
			"Type safety: The expression of type X needs unchecked conversion to conform to X<String>\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 12)\n" +
			"	X.testFunction(new X(\"hello\"));\n" +
			"	                   ^\n" +
			"X is a raw type. References to generic type X<T> should be parameterized\n" +
			"----------\n");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X<T> {\n" +
					"	public X(){\n" +
					"	}\n" +
					"	public X(T param){\n" +
					"		System.out.println(param);\n" +
					"	}\n" +
					"	public static void testFunction(X<String> param){\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		X.testFunction(new X<>());\n" +
					"		X.testFunction(new X(\"hello\"));\n" +
					"	}\n" +
					"}",
				},
				"SUCCESS\n" +
				"hello\n" +
				"SUCCESS");
	}
}
//shows the difference between using <> and the raw type - different semantics
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	T field1;\n" +
			"	public X(T param){\n" +
			"		field1 = param;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<?> x1 = new X(1).get(\"\");\n" + // ok - passing String where Object is expected
			"		X<?> x2 = new X<>(1).get(\"\");\n" + // bad - passing String where Integer is expected
			"	}\n" +
			"	public X<T> get(T t){\n" +
			"		return this;" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 7)\n" +
		"	X<?> x1 = new X(1).get(\"\");\n" +
		"	          ^^^^^^^^\n" +
		"Type safety: The constructor X(Object) belongs to the raw type X. References to generic type X<T> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	X<?> x1 = new X(1).get(\"\");\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"Type safety: The method get(Object) belongs to the raw type X. References to generic type X<T> should be parameterized\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 7)\n" +
		"	X<?> x1 = new X(1).get(\"\");\n" +
		"	              ^\n" +
		"X is a raw type. References to generic type X<T> should be parameterized\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 8)\n" +
		"	X<?> x2 = new X<>(1).get(\"\");\n" +
		"	                     ^^^\n" +
		"The method get(Integer) in the type X<Integer> is not applicable for the arguments (String)\n" +
		"----------\n");
}

public void test0014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<J,K> {\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String,Integer> x = new X<>();\n" +
			"		x.testFunction(\"SUCCESS\", 123);\n" +
			"	}\n" +
			"	public void testFunction(J param, K param2){\n" +
			"		System.out.println(param);\n" +
			"		System.out.println(param2);\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS\n" +
		"123");
}
public void test0014a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<J,K> {\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String,Integer> x = new X<>();\n" +
			"		x.testFunction(123, \"SUCCESS\");\n" +
			"	}\n" +
			"	public void testFunction(J param, K param2){\n" +
			"		System.out.println(param);\n" +
			"		System.out.println(param2);\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	x.testFunction(123, \"SUCCESS\");\n" +
		"	  ^^^^^^^^^^^^\n" +
		"The method testFunction(String, Integer) in the type X<String,Integer> is not applicable for the arguments (int, String)\n" +
		"----------\n");
}
public void test0015() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	X(){\n" +
			"		System.out.println(\"const.1\");\n" +
			"	}\n" +
			"	X (T t) {\n" +
			"		System.out.println(\"const.2\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String> x = new X<>();\n" +
			"		X<String> x2 = new X<>(\"\");\n" +
			"	}\n" +
			"}",
		},
		"const.1\nconst.2");
}
// To verify that <> cannot be used with explicit type arguments to generic constructor.
public void test0016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	<E> X(){\n" +
			"		System.out.println(\"const.1\");\n" +
			"	}\n" +
			"	<K,J> X (Integer i) {\n" +
			"		System.out.println(\"const.2\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String> x = new <String>X<>();\n" +
			"		X<String> x2 = new <String, Integer>X<>(1);\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	X<String> x = new <String>X<>();\n" +
		"	                   ^^^^^^\n" +
		"Explicit type arguments cannot be used with \'<>\' in an allocation expression\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	X<String> x2 = new <String, Integer>X<>(1);\n" +
		"	                    ^^^^^^^^^^^^^^^\n" +
		"Explicit type arguments cannot be used with \'<>\' in an allocation expression\n" +
		"----------\n");
}
public void test0016a() {
	this.runConformTest(  // javac fails to compile this, looks buggy
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	<E> X(){\n" +
			"		System.out.println(\"const.1\");\n" +
			"	}\n" +
			"	<K,J> X (Integer i) {\n" +
			"		System.out.println(\"const.2\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String> x = new X<>();\n" +
			"		X<String> x2 = new X<>(1);\n" +
			"	}\n" +
			"}",
		},
		"const.1\nconst.2");
}
// To verify that <> cannot be used with explicit type arguments to a generic constructor.
public void test0016b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	X<String> x;\n" +
			"	X<String> x2;\n" +
			"	<E> X(){\n" +
			"		System.out.println(\"const.1\");\n" +
			"	}\n" +
			"	<K,J> X (Integer i) {\n" +
			"		System.out.println(\"const.2\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<Integer> test = new <String>X<>();\n" +
			"		test.x = new <String>X<>();\n" +
			"		test.x2 = new <String, Integer>X<>(1);\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	X<Integer> test = new <String>X<>();\n" +
		"	                       ^^^^^^\n" +
		"Explicit type arguments cannot be used with \'<>\' in an allocation expression\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	test.x = new <String>X<>();\n" +
		"	              ^^^^^^\n" +
		"Explicit type arguments cannot be used with \'<>\' in an allocation expression\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 13)\n" +
		"	test.x2 = new <String, Integer>X<>(1);\n" +
		"	               ^^^^^^^^^^^^^^^\n" +
		"Explicit type arguments cannot be used with \'<>\' in an allocation expression\n" +
		"----------\n");
}
//To verify that a parameterized invocation of a generic constructor works even if <> is used
//to elide class type parameters. This test handles fields
public void test0016c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	X<String> x;\n" +
			"	X<String> x2;\n" +
			"	<E> X(){\n" +
			"		System.out.println(\"const.1\");\n" +
			"	}\n" +
			"	<K,J> X (Integer i) {\n" +
			"		System.out.println(\"const.2\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<Integer> test = new X<>();\n" +
			"		test.x = new X<>();\n" +
			"		test.x2 = new X<>(1);\n" +
			"	}\n" +
			"}",
		},
		"const.1\nconst.1\nconst.2");
}
// To verify that <> cannot be used with explicit type arguments to generic constructor.
public void test0017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	X(int i){\n" +
			"		System.out.println(\"const.1\");\n" +
			"	}\n" +
			"	<K,J> X (Integer i) {\n" +
			"		System.out.println(\"const.2\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String> x = new X<>(1);\n" +
			"		X<String> x2 = new <String, Integer>X<>(1);\n" +
			"		Integer i = 1;\n" +
			"		X<String> x3 = new <String, Integer>X<>(i);\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	X<String> x2 = new <String, Integer>X<>(1);\n" +
		"	                    ^^^^^^^^^^^^^^^\n" +
		"Explicit type arguments cannot be used with \'<>\' in an allocation expression\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	X<String> x3 = new <String, Integer>X<>(i);\n" +
		"	                    ^^^^^^^^^^^^^^^\n" +
		"Explicit type arguments cannot be used with \'<>\' in an allocation expression\n" +
		"----------\n"
);
}
// To verify that a parameterized invocation of a non-generic constructor works even if <> is used
// to elide class type parameters.
public void test0017a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	X(int i){\n" +
			"		System.out.println(\"const.1\");\n" +
			"	}\n" +
			"	<K,J> X (Integer i) {\n" +
			"		System.out.println(\"const.2\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String> x = new X<>(1);\n" +
			"		X<String> x2 = new X<>(1);\n" +
			"		Integer i = 1;\n" +
			"		X<String> x3 = new X<>(i);\n" +
			"	}\n" +
			"}",
		},
		"const.1\nconst.1\nconst.2");
}
// To verify that the correct constructor is found by parameter substitution in the diamond case
public void test0018() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	X(T t){\n" +
			"		System.out.println(\"const.1\");\n" +
			"	}\n" +
			"	X (T t, Integer i) {\n" +
			"		System.out.println(\"const.2\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X<>(\"\");\n" +
			"		X x2 = new X<>(\"\",1);\n" +
			"	}\n" +
			"}",
		},
		"const.1\nconst.2");
}
// To verify that the correct constructor is found by parameter substitution
// in the diamond case -- fields
public void test0018b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	X f1;\n" +
			"	X f2;\n" +
			"	X(T t){\n" +
			"		System.out.println(\"const.1\");\n" +
			"	}\n" +
			"	X (T t, Integer i) {\n" +
			"		System.out.println(\"const.2\");\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = new X<>(\"\");\n" +
			"		x.f1 = new X<>(\"\");\n" +
			"		x.f2 = new X<>(\"\",1);\n" +
			"	}\n" +
			"}",
		},
		"const.1\nconst.1\nconst.2");
}
public void test0019() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   String s = new String<>(\"junk\");\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	String s = new String<>(\"junk\");\n" +
		"	               ^^^^^^\n" +
		"The type String is not generic; it cannot be parameterized with arguments <>\n" +
		"----------\n");
}
// check inference at method argument position.
public void test0020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"    Zork z;\n" +
			"    public X(T t) {}\n" +
			"	 int f(X<String> p) {return 0;}\n" +
			"	 int x = f(new X<>(\"\"));\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//check inference at method argument position.
public void test0021() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"import java.util.ArrayList;\n" +
				"class X<T> {\n" +
				"  public X(T t) {}\n" +
				"  int f(List<String> p) {return 0;}\n" +
				"  int x = f(new ArrayList<>());\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	int x = f(new ArrayList<>());\n" +
			"	        ^\n" +
			"The method f(List<String>) in the type X<T> is not applicable for the arguments (ArrayList<Object>)\n" +
			"----------\n");
	} else {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.List;\n" +
					"import java.util.ArrayList;\n" +
					"class X<T> {\n" +
					"  public X(T t) {}\n" +
					"  int f(List<String> p) {return 0;}\n" +
					"  int x = f(new ArrayList<>());\n" +
					"}\n",
				},
				"");
	}
}
public void test0022() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"\n" +
			"class StringKeyHashMap<V> extends HashMap<String, V>  {  \n" +
			"}\n" +
			"\n" +
			"class IntegerValueHashMap<K> extends HashMap<K, Integer>  {  \n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"    Map<String, Integer> m1 = new StringKeyHashMap<>();\n" +
			"    Map<String, Integer> m2 = new IntegerValueHashMap<>();\n" +
			"}\n"
		},
		"");
}
public void test0023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.HashMap;\n" +
			"import java.util.Map;\n" +
			"\n" +
			"class StringKeyHashMap<V> extends HashMap<String, V>  {  \n" +
			"}\n" +
			"\n" +
			"class IntegerValueHashMap<K> extends HashMap<K, Integer>  {  \n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"    Map<String, Integer> m1 = new StringKeyHashMap<>(10);\n" +
			"    Map<String, Integer> m2 = new IntegerValueHashMap<>();\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	class StringKeyHashMap<V> extends HashMap<String, V>  {  \n" +
		"	      ^^^^^^^^^^^^^^^^\n" +
		"The serializable class StringKeyHashMap does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	class IntegerValueHashMap<K> extends HashMap<K, Integer>  {  \n" +
		"	      ^^^^^^^^^^^^^^^^^^^\n" +
		"The serializable class IntegerValueHashMap does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 11)\n" +
		"	Map<String, Integer> m1 = new StringKeyHashMap<>(10);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for StringKeyHashMap<>\n" +
		"----------\n");
}
// check inference at return expression.
public void test0024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"class X<T> {\n" +
			"  public X() {}\n" +
			"  X<String> f(List<String> p) {return new X<>();}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	import java.util.ArrayList;\n" +
		"	       ^^^^^^^^^^^^^^^^^^^\n" +
		"The import java.util.ArrayList is never used\n" +
		"----------\n");
}
// check inference at cast expression.
public void test0025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"class X<T> {\n" +
			"  public X() {}\n" +
			"  void f(List<String> p) { Object o = (X<String>) new X<>();}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	void f(List<String> p) { Object o = (X<String>) new X<>();}\n" +
		"	                                    ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot cast from X<Object> to X<String>\n" +
		"----------\n");
}
// Test various scenarios.
public void test0026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"public class X<T> {\n" +
			"	X(T t) {}\n" +
			"   X(String s) {}\n" +
			"   X(List<?> l) {}\n" +
			"   X<T> idem() { return this; }\n" +
			"   X<Number> x = new X<>(1);\n" +
			"   X<Integer> x2 = new X<>(1);\n" +
			"   List<?> list = new ArrayList<>();\n" +
			"   X<?> x3 = new X<>(1);\n" +
			"   X<Object> x4 = new X<>(1).idem();\n" +
			"   X<Object> x5 = new X<>(1);\n" +
			"   int m(X<String> xs) { return 0; }\n" +
			"   int i = m(new X<>(\"\"));\n" +
			"   X<?> x6 = new X<>(list);\n" +
			"}\n"
		},
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	X<Number> x = new X<>(1);\n" +
		"	              ^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from X<Integer> to X<Number>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	X<Object> x4 = new X<>(1).idem();\n" +
		"	               ^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from X<Integer> to X<Object>\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 13)\n" +
		"	X<Object> x5 = new X<>(1);\n" +
		"	               ^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from X<Integer> to X<Object>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 15)\n" +
		"	int i = m(new X<>(\"\"));\n" +
		"	        ^\n" +
		"The method m(X<String>) in the type X<T> is not applicable for the arguments (X<Object>)\n" +
		"----------\n" :
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	X<Object> x4 = new X<>(1).idem();\n" +
			"	               ^^^^^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from X<Integer> to X<Object>\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=344655
public void test0027() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"       class Y<U> {\n" +
			"	    <K,J> Y (Integer i) {\n" +
			"	    }\n" +
			"	}\n" +
			"\n" +
			"	<K,J> X (Integer i) {\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		X<String> x = new <String, Integer> X<>(1);\n" +
			"		X<String> x2 = x.new <String, Integer> Y<>(1);\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	X<String> x = new <String, Integer> X<>(1);\n" +
		"	                   ^^^^^^^^^^^^^^^\n" +
		"Explicit type arguments cannot be used with \'<>\' in an allocation expression\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	X<String> x2 = x.new <String, Integer> Y<>(1);\n" +
		"	                      ^^^^^^^^^^^^^^^\n" +
		"Explicit type arguments cannot be used with \'<>\' in an allocation expression\n" +
		"----------\n"
);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345239
public void test0028() {
	String[] testFiles = new String[] {
			"X.java",
			"public class X<T> {\n" +
			"     X<String> x = new X<> () {}\n;" +
			"     class Y<U> {\n" +
			"	  }\n" +
			"     X<String>.Y<String> y = x.new Y<>() {};\n" +
			"}\n"
		};
	if (this.complianceLevel < ClassFileConstants.JDK9) {
		this.runNegativeTest(
			testFiles,
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	X<String> x = new X<> () {}\n" +
			"	                  ^\n" +
			"\'<>\' cannot be used with anonymous classes\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	X<String>.Y<String> y = x.new Y<>() {};\n" +
			"	                              ^\n" +
			"\'<>\' cannot be used with anonymous classes\n" +
			"----------\n");
	} else {
		this.runConformTest(testFiles);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345359
public void test0029() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    X(T t) {}\n" +
			"    X<String> f2 = new X<>(new Y()); \n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	X<String> f2 = new X<>(new Y()); \n" +
		"	                           ^\n" +
		"Y cannot be resolved to a type\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345359
public void test0029a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    class I<T> {\n" +
			"        I(T t) {}\n" +
			"    }\n" +
			"    X.I<String> f = new X().new I<>(new Y()); \n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	X.I<String> f = new X().new I<>(new Y()); \n" +
		"	                                    ^\n" +
		"Y cannot be resolved to a type\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346026
public void test0030() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class C {}\n" +
			"interface I {}\n" +
			"public class X<T extends C & I> {\n" +
			"    X() {}\n" +
			"    X f = new X<>();\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	X f = new X<>();\n" +
		"	^\n" +
		"X is a raw type. References to generic type X<T> should be parameterized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346026
public void test0031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class C {}\n" +
			"interface I {}\n" +
			"public class X<T extends C & I> {\n" +
			"    X() {}\n" +
			"    X<?> f = new X<>();\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346026
public void test0032() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class C {}\n" +
			"interface I {}\n" +
			"public class X<T extends C & I> {\n" +
			"    static <U extends C & I> X<U> getX() {\n" +
			"        return null;\n" +
			"    }\n" +
			"    X<?> f2 = getX();\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346026
public void test0033() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class C {}\n" +
			"interface I {}\n" +
			"public class X<T extends C & I> {\n" +
			"    static <U extends C & I> X<U> getX() {\n" +
			"        return null;\n" +
			"    }\n" +
			"    X f2 = getX();\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 7)\n" +
		"	X f2 = getX();\n" +
		"	^\n" +
		"X is a raw type. References to generic type X<T> should be parameterized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0034() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    X(T t) {}\n" +
			"    X() {}\n" +
			"    void foo(T a) {\n" +
			"	 System.out.println(a);\n" +
			"	 }\n" +
			"	 class Y<K>{\n" +
			"		Y(T t,K k) {}\n" +
			"	 }\n" +
			"    public static void main(String[] args) {\n" +
			"		X<Integer> x1 = new X<>(1,1);\n" +
			"		X<Integer> x2 = new X<>(1);\n" +
			"		X<Integer> x3 = new X<>();\n" +
			"		X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();\n" +
			"		X<Integer>.Y<String> y2 = new X<>(1,1).new Y<>(1);\n" +
			"		X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);\n" +
			"		X<Integer>.Y<String> y4 = new X<>(1).new Y<>(\"\",\"\");\n" +
			"		X<Integer>.Y<String> y5 = new X<>(1).new Y<>(1,\"\");\n" +
			"		X<Integer>.Y<String> y6 = new X<>().new Y<>(1,\"\");\n" +
			"		X<Integer>.Y<String> y7 = new X<>().new Y<>(1,1);\n" +
			"	 }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	X<Integer> x1 = new X<>(1,1);\n" +
		"	                ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();\n" +
		"	                          ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 15)\n" +
		"	X<Integer>.Y<String> y2 = new X<>(1,1).new Y<>(1);\n" +
		"	                          ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 16)\n" +
		"	X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 17)\n" +
		"	X<Integer>.Y<String> y4 = new X<>(1).new Y<>(\"\",\"\");\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 19)\n" +
		"	X<Integer>.Y<String> y6 = new X<>().new Y<>(1,\"\");\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<String> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n" +
		"7. ERROR in X.java (at line 20)\n" +
		"	X<Integer>.Y<String> y7 = new X<>().new Y<>(1,1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<Integer> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0034b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    X(T t) {}\n" +
			"    X() {}\n" +
			"    void foo(T a) {\n" +
			"	 System.out.println(a);\n" +
			"	 }\n" +
			"	 class Y<K>{\n" +
			"		Y(T t,K k) {}\n" +
			"		Y(K k) {}\n" +
			"	 }\n" +
			"    public static void main(String[] args) {\n" +
			"		X<String>.Y<String> y42 = new X<>(\"\").new Y<>(\"\");\n" +
			"		X<String>.Y<String> y41 = new X<>(\"\").new Y<>(\"\",\"\");\n" +
			"		X<Integer>.Y<String> y4 = new X<>(1).new Y<>(\"\",\"\");\n" +
			"	 }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 14)\n" +
		"	X<Integer>.Y<String> y4 = new X<>(1).new Y<>(\"\",\"\");\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0035() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    X(T t) {}\n" +
			"    X() {}\n" +
			"	 @SafeVarargs\n" +
			"    X(String abc, String abc2, T... t) {}\n" +
			"    void foo(T a) {\n" +
			"	 System.out.println(a);\n" +
			"	 }\n" +
			"	 class Y<K>{\n" +
			"		@SafeVarargs\n" +
			"		Y(T t,String abc, K... k) {}\n" +
			"	 }\n" +
			"    public static void main(String[] args) {\n" +
			"		X<Integer> x1 = new X<>(1,1);\n" +
			"		X<Integer> x2 = new X<>(1);\n" +
			"		X<Integer> x3 = new X<>();\n" +
			"		X<Integer> x4 = new X<>(\"\",\"\");\n" +
			"		X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
			"		X<Integer> x6 = new X<>(\"\",\"\",1);\n" +
			"		X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();\n" +
			"		X<Integer>.Y<String> y2 = new X<>(\"\",1).new Y<>(\"\");\n" +
			"		X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);\n" +
			"		X<Integer>.Y<String> y4 = new X<>(1).new Y<>(1,\"\");\n" +
			"		X<Integer>.Y<String> y5 = new X<>(1).new Y<>(1,\"\",\"\");\n" +
			"		X<Integer>.Y<String> y6 = new X<>().new Y<>(1,\"\",1);\n" +
			"		X<Integer>.Y<String> y7 = new X<>().new Y<>(\"\",\"\",1);\n" +
			"	 }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 14)\n" +
		"	X<Integer> x1 = new X<>(1,1);\n" +
		"	                ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 18)\n" +
		"	X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
		"	                ^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<String> to X<Integer>\n"
		:
		"Cannot infer type arguments for X<>\n"
		) +
		"----------\n" +
		"3. ERROR in X.java (at line 20)\n" +
		"	X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();\n" +
		"	                          ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 21)\n" +
		"	X<Integer>.Y<String> y2 = new X<>(\"\",1).new Y<>(\"\");\n" +
		"	                          ^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 22)\n" +
		"	X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 25)\n" +
		"	X<Integer>.Y<String> y6 = new X<>().new Y<>(1,\"\",1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<Integer> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n" +
		"7. ERROR in X.java (at line 26)\n" +
		"	X<Integer>.Y<String> y7 = new X<>().new Y<>(\"\",\"\",1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<Integer> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0036() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    X(T t) {}\n" +
			"    X() {}\n" +
			"	 @SafeVarargs\n" +
			"    X(String abc, String abc2, T... t) {}\n" +
			"    void foo(T a) {\n" +
			"	 System.out.println(a);\n" +
			"	 }\n" +
			"	 class Y<K>{\n" +
			"		@SafeVarargs\n" +
			"		Y(T t,String abc, K... k) {}\n" +
			"	 }\n" +
			"	X<Integer> x1 = new X<>(1,1);\n" +
			"	X<Integer> x2 = new X<>(1);\n" +
			"	X<Integer> x3 = new X<>();\n" +
			"	X<Integer> x4 = new X<>(\"\",\"\");\n" +
			"	X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
			"	X<Integer> x6 = new X<>(\"\",\"\",1);\n" +
			"	X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();\n" +
			"	X<Integer>.Y<String> y2 = new X<>(\"\",1).new Y<>(\"\");\n" +
			"	X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);\n" +
			"	X<Integer>.Y<String> y4 = new X<>(1).new Y<>(1,\"\");\n" +
			"	X<Integer>.Y<String> y5 = new X<>(1).new Y<>(1,\"\",\"\");\n" +
			"	X<Integer>.Y<String> y6 = new X<>().new Y<>(1,\"\",1);\n" +
			"	X<Integer>.Y<String> y7 = new X<>().new Y<>(\"\",\"\",1);\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	X<Integer> x1 = new X<>(1,1);\n" +
		"	                ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 17)\n" +
		"	X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
		"	                ^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<String> to X<Integer>\n"
		:
		"Cannot infer type arguments for X<>\n"
		) +
		"----------\n" +
		"3. ERROR in X.java (at line 19)\n" +
		"	X<Integer>.Y<String> y1 = new X<>(1,1).new Y<>();\n" +
		"	                          ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 20)\n" +
		"	X<Integer>.Y<String> y2 = new X<>(\"\",1).new Y<>(\"\");\n" +
		"	                          ^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 21)\n" +
		"	X<Integer>.Y<String> y3 = new X<>(1).new Y<>(1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 24)\n" +
		"	X<Integer>.Y<String> y6 = new X<>().new Y<>(1,\"\",1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<Integer> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n" +
		"7. ERROR in X.java (at line 25)\n" +
		"	X<Integer>.Y<String> y7 = new X<>().new Y<>(\"\",\"\",1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<Integer> to X<Integer>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0034a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    X(T t) {}\n" +
			"    X() {}\n" +
			"    void foo(T a) {\n" +
			"	 System.out.println(a);\n" +
			"	 }\n" +
			"    public static void main(String[] args) {\n" +
			"		X<Integer> x1 = new X<>(1,1);\n" +
			"		X<Integer> x2 = new X<>(1);\n" +
			"		X<Integer> x3 = new X<>();\n" +
			"	 }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	X<Integer> x1 = new X<>(1,1);\n" +
		"	                ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0035a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    X(T t) {}\n" +
			"    X() {}\n" +
			"	 @SafeVarargs\n" +
			"    X(String abc, String abc2, T... t) {}\n" +
			"    void foo(T a) {\n" +
			"	 	System.out.println(a);\n" +
			"	 }\n" +
			"    public static void main(String[] args) {\n" +
			"		X<Integer> x1 = new X<>(1,1);\n" +
			"		X<Integer> x2 = new X<>(1);\n" +
			"		X<Integer> x3 = new X<>();\n" +
			"		X<Integer> x4 = new X<>(\"\",\"\");\n" +
			"		X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
			"		X<Integer> x6 = new X<>(\"\",\"\",1);\n" +
			"	 }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	X<Integer> x1 = new X<>(1,1);\n" +
		"	                ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
		"	                ^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<String> to X<Integer>\n"
		:
		"Cannot infer type arguments for X<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0036a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    X(T t) {}\n" +
			"    X() {}\n" +
			"	 @SafeVarargs\n" +
			"    X(String abc, String abc2, T... t) {}\n" +
			"    void foo(T a) {\n" +
			"	 System.out.println(a);\n" +
			"	 }\n" +
			"	X<Integer> x1 = new X<>(1,1);\n" +
			"	X<Integer> x2 = new X<>(1);\n" +
			"	X<Integer> x3 = new X<>();\n" +
			"	X<Integer> x4 = new X<>(\"\",\"\");\n" +
			"	X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
			"	X<Integer> x6 = new X<>(\"\",\"\",1);\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	X<Integer> x1 = new X<>(1,1);\n" +
		"	                ^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 13)\n" +
		"	X<Integer> x5 = new X<>(\"\",\"\",\"\");\n" +
		"	                ^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<String> to X<Integer>\n"
		:
		"Cannot infer type arguments for X<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345559
public void test0037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    X(T t) {}\n" +
			"    X() {}\n" +
			"	 @SafeVarargs\n" +
			"    X(String abc, String abc2, T... t) {}\n" +
			"    void foo(T a) {\n" +
			"	 System.out.println(a);\n" +
			"	 }\n" +
			"	 class Y<K>{\n" +
			"		@SafeVarargs\n" +
			"		Y(T t,String abc, K... k) {}\n" +
			"	 }\n" +
			"    public static void main(String[] args) {\n" +
			"		X<Integer>.Y<String> y1 = new X<>().new Y<>(1);\n" +
			"		X<Integer>.Y<String> y2 = new X<>(1).new Y<>(1);\n" +
			"		X<Integer>.Y<String> y3 = new X<>(\"\",\"\",1).new Y<>(1);\n" +
			"		X<Integer>.Y<String> y4 = new X<>(1,\"\").new Y<>(1,\"\");\n" +
			"	 }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 14)\n" +
		"	X<Integer>.Y<String> y1 = new X<>().new Y<>(1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 15)\n" +
		"	X<Integer>.Y<String> y2 = new X<>(1).new Y<>(1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 16)\n" +
		"	X<Integer>.Y<String> y3 = new X<>(\"\",\"\",1).new Y<>(1);\n" +
		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for Y<>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 17)\n" +
		"	X<Integer>.Y<String> y4 = new X<>(1,\"\").new Y<>(1,\"\");\n" +
		"	                          ^^^^^^^^^^^^^\n" +
		"Cannot infer type arguments for X<>\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=341795
public void test0038() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface A {\n" +
			"    <T extends B & C> T getaMethod();\n" +
			"    <T extends B & C> void setaMethod(T param);\n" +
			"}\n" +
			"class B {\n" +
			"}\n" +
			"interface C {\n" +
			"}\n" +
			"public class X {\n" +
			"    public void someMethod(A aInstance) {\n" +
			"        aInstance.getaMethod();\n" +
			"    }\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319436
public void test0039() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.Serializable;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    createObject();\n" +
			"  }\n" +
			"  private static <T extends Comparable<?> & Serializable> T createObject() {\n" +
			"    return null;\n" +
			"  }\n" +
			"}\n"
		},
		"");
}
public void test0042() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I<T> {}\n" +
			"public class X {\n" +
			"    <T extends I<T>> void m() { }\n" +
			"    { m(); } \n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0043() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T>  {\n" +
			"    class Y<Z>  {\n" +
			"        Y(T a, Z b) {\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        X<String>.Y<String>  x1 = new X<String>().new Y<String>(\"\",\"\");\n" +
			"        X<String>.Y<String>  x2 = new X<String>().new Y<>(\"\",\"\");\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n"
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0044() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"    class Y<Z> {\n" +
			"         Y(T a, Z b) {\n" +
			"         }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        X<String>.Y<String> x = new X<>().new Y<>(\"\",\"\");\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	X<String>.Y<String> x = new X<>().new Y<>(\"\",\"\");\n" +
		"	                        ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"Type mismatch: cannot convert from X<Object>.Y<String> to X<String>.Y<String>\n"
		:
		"Cannot infer type arguments for Y<>\n"
		) +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    class Y<T, Z> {\n" +
			"         Y(T a, Z b) {\n" +
			"         }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        X.Y<String, String> x = new X().new Y<>(\"\",\"\");\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n"
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0046() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"    class Y<Z> {\n" +
			"         Y(T a, Z b) { \n" +
			"         }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        X<String>.Y<String> x = new X<String>().new Y<>(\"\",\"\");\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n"
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0047() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"    class Y<Z> {\n" +
			"         Y(T a, Z b) {\n" +
			"         }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        X<String>.Y<String> x1 = new X<String>().new Y<String>(\"\",\"\"); \n" +
			"        X<String>.Y<String> x2 = new X<String>().new Y<>(\"\",\"\"); // javac wrong error \n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n"
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0048() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"    <T> X(T t) {\n" +
			"    }\n" +
			"    X<String> x = new X<>(\"\"); \n" +
			"    Zork z;\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	<T> X(T t) {\n" +
		"	 ^\n" +
		"The type parameter T is hiding the type T\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0049() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"     class Y<Z> {\n" +
			"          Y(T a, Z b) {\n" +
			"          }\n" +
			"     }\n" +
			"   public static void main(String[] args) {\n" +
			"       X<Object>.Y<String> x1 = new X<Object>().new Y<String>(new Object(),\"\");\n" +
			"       X<Object>.Y<String> x2 = new X<>().new Y<String>(new Object(),\"\");\n" +
			"       X<Object>.Y<String> x3 = new X<Object>().new Y<>(new Object(),\"\");\n" +
			"       X<Object>.Y<String> x4 = new X<>().new Y<>(new Object(),\"\");\n" +
			"     }\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0050() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T extends Comparable<T>> {\n" +
			"     class Y<Z> {\n" +
			"          Y(T a, Z b) {\n" +
			"          }\n" +
			"     }\n" +
			"   public static void main(String[] args) {\n" +
			"       X<String>.Y<String> x1 = new X<String>().new Y<>(\"\",\"\");\n" +
			"     }\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=345968
public void test0051() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T extends Comparable<T>> {\n" +
			"     class Y<Z> {\n" +
			"          Y(Integer a, Z b) {\n" +
			"          }\n" +
			"          Y(T a, Z b) {\n" +
			"          }\n" +
			"     }\n" +
			"   public static void main(String[] args) {\n" +
			"       X<String>.Y<String> x1 = new X<String>().new Y<>(\"\",\"\");\n" +
			"     }\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0052() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<E> {\n" +
			"    X(E e) {}\n" +
			"    X() {}\n" +
			"    public static void main(String[] args) {\n" +
			"        X<Number> x = new X<Number>(1);\n" +
			"        X<String> x2 = new X<String>(\"SUCCESS\");\n" +
			"        X<Integer> x3 = new X<Integer>(1);\n" +
			"        X<AX> x4 = new X<AX>(new AX());\n" +
			"		 X<? extends AX> x5 = new X<AX<String>>(new AX<String>());\n" +
			"		 X<?> x6 = new X<AX<String>>(new AX<String>());\n" +
			"		 X<Class<? extends Object>> x7 = new X<Class<? extends Object>>();\n" +
			"	}\n" +
			"}\n" +
			"class AX<T>{}\n"
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	X<String> x2 = new X<String>(\"SUCCESS\");\n" +
			"	                   ^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	X<Integer> x3 = new X<Integer>(1);\n" +
			"	                    ^\n" +
			"Redundant specification of type arguments <Integer>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	X<AX> x4 = new X<AX>(new AX());\n" +
			"	               ^\n" +
			"Redundant specification of type arguments <AX>\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 9)\n" +
			"	X<? extends AX> x5 = new X<AX<String>>(new AX<String>());\n" +
			"	                         ^\n" +
			"Redundant specification of type arguments <AX<String>>\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 9)\n" +
			"	X<? extends AX> x5 = new X<AX<String>>(new AX<String>());\n" +
			"	                                           ^^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 10)\n" +
			"	X<?> x6 = new X<AX<String>>(new AX<String>());\n" +
			"	              ^\n" +
			"Redundant specification of type arguments <AX<String>>\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 10)\n" +
			"	X<?> x6 = new X<AX<String>>(new AX<String>());\n" +
			"	                                ^^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 11)\n" +
			"	X<Class<? extends Object>> x7 = new X<Class<? extends Object>>();\n" +
			"	                                    ^\n" +
			"Redundant specification of type arguments <Class<? extends Object>>\n" +
			"----------\n"
		: // additional error at line 5 due to better inference:
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	X<Number> x = new X<Number>(1);\n" +
			"	                  ^\n" +
			"Redundant specification of type arguments <Number>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	X<String> x2 = new X<String>(\"SUCCESS\");\n" +
			"	                   ^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	X<Integer> x3 = new X<Integer>(1);\n" +
			"	                    ^\n" +
			"Redundant specification of type arguments <Integer>\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	X<AX> x4 = new X<AX>(new AX());\n" +
			"	               ^\n" +
			"Redundant specification of type arguments <AX>\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 9)\n" +
			"	X<? extends AX> x5 = new X<AX<String>>(new AX<String>());\n" +
			"	                         ^\n" +
			"Redundant specification of type arguments <AX<String>>\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 9)\n" +
			"	X<? extends AX> x5 = new X<AX<String>>(new AX<String>());\n" +
			"	                                           ^^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 10)\n" +
			"	X<?> x6 = new X<AX<String>>(new AX<String>());\n" +
			"	              ^\n" +
			"Redundant specification of type arguments <AX<String>>\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 10)\n" +
			"	X<?> x6 = new X<AX<String>>(new AX<String>());\n" +
			"	                                ^^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 11)\n" +
			"	X<Class<? extends Object>> x7 = new X<Class<? extends Object>>();\n" +
			"	                                    ^\n" +
			"Redundant specification of type arguments <Class<? extends Object>>\n" +
			"----------\n"
		),
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0052b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<E> {\n" +
			"	 E eField;\n" +
			"	 E get() { return this.eField; }\n" +
			"    X(E e) {}\n" +
			"    X(int e, String e2) {}\n" +
			"    public static void main(String[] args) {\n" +
			"        X<Number> x = new X<Number>(1);\n" +
			"        X<String> x2 = new X<String>(\"SUCCESS\");\n" +
			"        X<String> x22 = new X<String>(1,\"SUCCESS\");\n" +
			"        X<Integer> x3 = new X<Integer>(1);\n" +
			"        String s = foo(new X<String>(\"aaa\"));\n" +
			"        String s2 = foo(new X<String>(1,\"aaa\"));\n" +
			"	}\n" +
			"    static String foo(X<String> x) {\n" +
			"		return x.get();\n" +
			"    }\n" +
			"}\n"
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	X<String> x2 = new X<String>(\"SUCCESS\");\n" +
			"	                   ^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	X<String> x22 = new X<String>(1,\"SUCCESS\");\n" +
			"	                    ^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	X<Integer> x3 = new X<Integer>(1);\n" +
			"	                    ^\n" +
			"Redundant specification of type arguments <Integer>\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 11)\n" +
			"	String s = foo(new X<String>(\"aaa\"));\n" +
			"	                   ^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 12)\n" +
			"	String s2 = foo(new X<String>(1,\"aaa\"));\n" +
			"	                    ^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n"
		: // additional error at line 7 due to better inference
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	X<Number> x = new X<Number>(1);\n" +
			"	                  ^\n" +
			"Redundant specification of type arguments <Number>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	X<String> x2 = new X<String>(\"SUCCESS\");\n" +
			"	                   ^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 9)\n" +
			"	X<String> x22 = new X<String>(1,\"SUCCESS\");\n" +
			"	                    ^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 10)\n" +
			"	X<Integer> x3 = new X<Integer>(1);\n" +
			"	                    ^\n" +
			"Redundant specification of type arguments <Integer>\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 11)\n" +
			"	String s = foo(new X<String>(\"aaa\"));\n" +
			"	                   ^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 12)\n" +
			"	String s2 = foo(new X<String>(1,\"aaa\"));\n" +
			"	                    ^\n" +
			"Redundant specification of type arguments <String>\n" +
			"----------\n"
		),
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0052c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<E> {\n" +
			"	X(String abc, String def) {}\n" +
			"	void foo() {\n" +
			"		X<Integer> x = new X<Integer>(\"\",\"\");\n" +
			"		foo3(new X<Integer>(\"\",\"\"));\n" +
			"	}\n" +
			"	X<Integer> foo2() {\n" +
			"		return new X<Integer>(\"\",\"\");\n" +
			"	}\n" +
			"	void foo3(X<Integer> x) {}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	X<Integer> x = new X<Integer>(\"\",\"\");\n" +
		"	                   ^\n" +
		"Redundant specification of type arguments <Integer>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	foo3(new X<Integer>(\"\",\"\"));\n" +
		"	         ^\n" +
		"Redundant specification of type arguments <Integer>\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 8)\n" +
		"	return new X<Integer>(\"\",\"\");\n" +
		"	           ^\n" +
		"Redundant specification of type arguments <Integer>\n" +
		"----------\n",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0052d() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<E> {\n" +
			"    X(E e) {}\n" +
			"    X() {}\n" +
			"    public static void main(String[] args) {\n" +
			"        X<Number> x = new X<Number>(1);\n" +
			"	}\n" +
			"}\n" +
			"class AX<T>{}\n"
		},
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"" :
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	X<Number> x = new X<Number>(1);\n" +
		"	                  ^\n" +
		"Redundant specification of type arguments <Number>\n" +
		"----------\n",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0053() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"Z.java",
			"public class Z <T extends ZB> { \n" +
			"    public static void main(String[] args) {\n" +
			"        foo(new Z<ZB>());\n" +
			"    }\n" +
			"    static void foo(Z<ZB> z) {\n" +
			"    }\n" +
			"}\n" +
			"class ZB {\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in Z.java (at line 3)\n" +
		"	foo(new Z<ZB>());\n" +
		"	        ^\n" +
		"Redundant specification of type arguments <ZB>\n" +
		"----------\n",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0054() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"public class Y<V> {\n" +
			"  public static <W extends ABC> Y<W> make(Class<W> clazz) {\n" +
			"    return new Y<W>();\n" +
			"  }\n" +
			"}\n" +
			"class ABC{}\n"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 3)\n" +
		"	return new Y<W>();\n" +
		"	           ^\n" +
		"Redundant specification of type arguments <W>\n" +
		"----------\n",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0055() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<A> {\n" +
			"  class Inner<B> { }\n" +
			"  static class Inner2<C> { }\n" +
			"\n" +
			"  void method() {\n" +
			"    X<String>.Inner<Integer> a= new X<String>().new Inner<Integer>();\n" +
			"    X<String>.Inner<Integer> a1= new X<String>().new Inner<>();\n" +	// do not warn. Removing String from X<String> not possible
			"    Inner<Integer> b= new X<A>().new Inner<Integer>();\n" +
			"    Inner<Integer> c= new Inner<Integer>();\n" +
			"    X<A>.Inner<Integer> e= new X<A>().new Inner<Integer>();\n" +
			"    X<A>.Inner<Integer> f= new Inner<Integer>();\n" +
			"    X.Inner2<Integer> d3 = new X.Inner2<Integer>();\n" +
			"  }\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	X<String>.Inner<Integer> a= new X<String>().new Inner<Integer>();\n" +
		"	                                                ^^^^^\n" +
		"Redundant specification of type arguments <Integer>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	Inner<Integer> b= new X<A>().new Inner<Integer>();\n" +
		"	                                 ^^^^^\n" +
		"Redundant specification of type arguments <Integer>\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	Inner<Integer> c= new Inner<Integer>();\n" +
		"	                      ^^^^^\n" +
		"Redundant specification of type arguments <Integer>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 10)\n" +
		"	X<A>.Inner<Integer> e= new X<A>().new Inner<Integer>();\n" +
		"	                                      ^^^^^\n" +
		"Redundant specification of type arguments <Integer>\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 11)\n" +
		"	X<A>.Inner<Integer> f= new Inner<Integer>();\n" +
		"	                           ^^^^^\n" +
		"Redundant specification of type arguments <Integer>\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 12)\n" +
		"	X.Inner2<Integer> d3 = new X.Inner2<Integer>();\n" +
		"	                             ^^^^^^\n" +
		"Redundant specification of type arguments <Integer>\n" +
		"----------\n",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
// qualified allocation
public void test0056() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X <T> {\n" +
			"	void foo1() {\n" +
			"		X<String>.Item<Thread> i = new X<Exception>().new Item<Thread>();\n" +
			"	}\n" +
			"	void foo2() {\n" +
			"		X<Exception>.Item<Thread> j = new X<Exception>.Item<Thread>();\n" +
			"	}\n" +
			"	class Item <E> {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	X<String>.Item<Thread> i = new X<Exception>().new Item<Thread>();\n" +
		"	                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from X<Exception>.Item<Thread> to X<String>.Item<Thread>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	X<String>.Item<Thread> i = new X<Exception>().new Item<Thread>();\n" +
		"	                                                  ^^^^\n" +
		"Redundant specification of type arguments <Thread>\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	X<Exception>.Item<Thread> j = new X<Exception>.Item<Thread>();\n" +
		"	                                  ^^^^^^^^^^^^^^^^^\n" +
		"Cannot allocate the member type X<Exception>.Item<Thread> using a parameterized compound name; use its simple name and an enclosing instance of type X<Exception>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 6)\n" +
		"	X<Exception>.Item<Thread> j = new X<Exception>.Item<Thread>();\n" +
		"	                                               ^^^^\n" +
		"Redundant specification of type arguments <Thread>\n" +
		"----------\n",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
// qualified allocation
public void test0056b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X <T> {\n" +
			"	static class X1<Z> {\n" +
			"		X1(Z z){}\n" +
			"	}\n" +
			"	X1<Integer> x1 = new X.X1<Integer>(1);\n" +
			"	X1<Number> x2 = new X.X1<Number>(1);\n" +
			"}\n"
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	X1<Integer> x1 = new X.X1<Integer>(1);\n" +
			"	                       ^^\n" +
			"Redundant specification of type arguments <Integer>\n" +
			"----------\n"
		: // additional error at line 6 due to better inference:
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	X1<Integer> x1 = new X.X1<Integer>(1);\n" +
			"	                       ^^\n" +
			"Redundant specification of type arguments <Integer>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	X1<Number> x2 = new X.X1<Number>(1);\n" +
			"	                      ^^\n" +
			"Redundant specification of type arguments <Number>\n" +
			"----------\n"
		),
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
// qualified allocation
public void test0056c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X <T> {\n" +
			"	X(T t){}\n" +
			"	class X1<Z> {\n" +
			"		X1(Z z){}\n" +
			"	}\n" +
			"	X<Integer>.X1<Number> x1 = new X<Integer>(1).new X1<Number>(1);\n" +
			"}\n"
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	X<Integer>.X1<Number> x1 = new X<Integer>(1).new X1<Number>(1);\n" +
			"	                               ^\n" +
			"Redundant specification of type arguments <Integer>\n" +
			"----------\n"
		: // additional error (2.) at inner allocation due to better inference:
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	X<Integer>.X1<Number> x1 = new X<Integer>(1).new X1<Number>(1);\n" +
			"	                               ^\n" +
			"Redundant specification of type arguments <Integer>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	X<Integer>.X1<Number> x1 = new X<Integer>(1).new X1<Number>(1);\n" +
			"	                                                 ^^\n" +
			"Redundant specification of type arguments <Number>\n" +
			"----------\n"
		),
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0057() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void test() {\n" +
			"		Pair<Double, Integer> p = new InvertedPair<Integer, Double>();\n" +
			"	}\n" +
			"}\n" +
			"class Pair<A, B> {\n" +
			"}\n" +
			"class InvertedPair<A, B> extends Pair<B, A> {\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	Pair<Double, Integer> p = new InvertedPair<Integer, Double>();\n" +
		"	                              ^^^^^^^^^^^^\n" +
		"Redundant specification of type arguments <Integer, Double>\n" +
		"----------\n",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0058() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X {\n" +
			"    public void test(boolean param) {\n" +
			"        ArrayList<?> ls = (param) \n" +
			"        		? new ArrayList<String>()\n" +
			"        		: new ArrayList<Object>();\n" +
			"        		\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	: new ArrayList<Object>();\n" +
		"	      ^^^^^^^^^\n" +
		"Redundant specification of type arguments <Object>\n" +
		"----------\n",
		null,
		false,
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340747
public void test0059() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class X<T> {\n" +
			"	 X(List<? extends T> p) {}\n" +
			"    Object x = new X<CharSequence>((ArrayList<String>) null);\n" +
			"}\n"
		},
		"",
		null,
		false,
		customOptions);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=361441
public void test0061() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.net.URI;" +
			"import java.nio.file.FileSystems;" +
			"import java.util.Collections;\n" +
			"public class X {\n" +
			"	 public static void foo() {\n" +
			"    	URI uri = URI.create(\"http://www.eclipse.org\");\n" +
			"		FileSystems.<String, Object>newFileSystem(uri, Collections.emptyMap());\n" +
			"	 }\n" +
			"}\n"
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	FileSystems.<String, Object>newFileSystem(uri, Collections.emptyMap());\n" +
			"	                            ^^^^^^^^^^^^^\n" +
			"The method newFileSystem(URI, Map<String,?>) in the type FileSystems is not applicable for the arguments (URI, Map<Object,Object>)\n" +
			"----------\n"
		: // with better inference, method is applicable, but then we have another problem:
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	FileSystems.<String, Object>newFileSystem(uri, Collections.emptyMap());\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Unhandled exception type IOException\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	FileSystems.<String, Object>newFileSystem(uri, Collections.emptyMap());\n" +
			"	             ^^^^^^^^^^^^^^\n" +
			"Unused type arguments for the non generic method newFileSystem(URI, Map<String,?>) of type FileSystems; it should not be parameterized with arguments <String, Object>\n" +
			"----------\n"
		));
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428220, [1.8][compiler] Javadoc processing interferes with type inference.
public void test428220() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	runNegativeTest(
		new String[] {
			"X.java",
			"public class HashMap<K, V> {\n" +
			"	static class Node<K, V> {\n" +
			"		Node(int hash, K key, V value, Node<K, V> next) {}\n" +
			"	}\n" +
			"	/** @see #put(Object, Object) */\n" +
			"	public V put(K key, V value) {	return null; }\n" +
			"\n" +
			"	Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {\n" +
			"		return new Node<>(hash, key, value, next); // Error\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class HashMap<K, V> {\n" +
		"	             ^^^^^^^\n" +
		"The public type HashMap must be defined in its own file\n" +
		"----------\n", null, true, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428220, [1.8][compiler] Javadoc processing interferes with type inference.
public void test428220a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
	runNegativeTest(
		new String[] {
			"X.java",
			"public class HashMap<K, V> {\n" +
			"	static class Node<K, V> {\n" +
			"		Node(int hash, K key, V value, Node<K, V> next) {}\n" +
			"	}\n" +
			"	/** @see #put(Object, Object) */\n" +
			"	public V put(K key, V value) {	return null; }\n" +
			"\n" +
			"	Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {\n" +
			"		return new Node<>(hash, key, value, next); // Error\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class HashMap<K, V> {\n" +
		"	             ^^^^^^^\n" +
		"The public type HashMap must be defined in its own file\n" +
		"----------\n", null, true, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442929, [1.8][compiler] ClassCastException during runtime where is no cast
public void test442929() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		shouldNotThrow();\n" +
			"	}\n" +
			"	static void shouldNotThrow() {\n" +
			"		final String[] array = { \"\" };\n" +
			"		final String[] expected = { \"\" };\n" +
			"		// throws\n" +
			"       try {\n" +
			"		    assertThat(op(array, \"\")).isEqualTo(expected);\n" +
			"       } catch (ClassCastException c) {\n" +
			"           System.out.println(\"Expected CCE\");\n" +
			"       }\n" +
			"	}\n" +
			"	static <T> T[] op(T[] array, T element) {\n" +
			"		return asArray(element);\n" +
			"	}\n" +
			"	@SafeVarargs\n" +
			"	static <T> T[] asArray(T... elements) {\n" +
			"		return elements;\n" +
			"	}\n" +
			"	static <T> ObjectArrayAssert<T> assertThat(T actual) {\n" +
			"		return new ObjectArrayAssert<>(actual);\n" +
			"	}\n" +
			"	static class ObjectArrayAssert<T> {\n" +
			"		ObjectArrayAssert(T actual) {\n" +
			"		}\n" +
			"		void isEqualTo(T expected) {\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"Expected CCE");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448028, [1.8] 1.8 cannot infer type arguments where 1.7 does
public void test448028() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		   new String[] {
			   "X.java",
			   "public class X {\n" +
			   "\n" +
			   "  public static interface I {/*empty*/}\n" +
			   "\n" +
			   "  public static class C\n" +
			   "    implements I {/*empty*/}\n" +
			   "\n" +
			   "  public static class W<T extends I>\n" +
			   "    implements I {\n" +
			   "\n" +
			   "    // --- problem is triggered only, when there is a vararg-parameter\n" +
			   "    public W(final T t, final Object... o) {\n" +
			   "      super();\n" +
			   "    }\n" +
			   "  }\n" +
			   "\n" +
			   "  // --- needed to trigger problem\n" +
			   "  public static final <T> T inspect(final T t) {\n" +
			   "    return t;\n" +
			   "  }\n" +
			   "\n" +
			   "  // --- this compiles ok when having JDK Compilance set to 1.7 !\n" +
			   "  public static final W<C> err1() {\n" +
			   "    final C c = new C();\n" +
			   "    final Object o = new Object();\n" +
			   "    return inspect(new W<>(c, o)); // - ERROR: Cannot infer type arguments for W<> F.java\n" +
			   "  }\n" +
			   "\n" +
			   "  public static final W<C> wrn1() {\n" +
			   "    final C c = new C();\n" +
			   "    final Object o = new Object();\n" +
			   "    // --- giving the type-parameter yields a warning\n" +
			   "    // --- comparing that to the error of method err1() it does not make much sense\n" +
			   "    return inspect(new W<C>(c, o)); // - WARNING: Redundant specification of type arguments <F.C> F.java\n" +
			   "  }\n" +
			   "\n" +
			   "  public static final W<C> ok1() {\n" +
			   "    final C c = new C();\n" +
			   "    // --- no extra vararg-paramaeter\n" +
			   "    return inspect(new W<>(c)); // - OK\n" +
			   "  }\n" +
			   "\n" +
			   "  public static final W<C> ok2() {\n" +
			   "    final C c = new C();\n" +
			   "    final Object o = new Object();\n" +
			   "    // --- no check-method\n" +
			   "    return new W<>(c, o); // - OK\n" +
			   "  }\n" +
			   "\n" +
			   "  public static final W<C> ok3() {\n" +
			   "    final C c = new C();\n" +
			   "    // --- no check-method\n" +
			   "    return new W<>(c); // - OK\n" +
			   "  }\n" +
			   "\n" +
			   "  public static final W<C> ok4() {\n" +
			   "    final C c = new C();\n" +
			   "    final Object o = new Object();\n" +
			   "    // --- this also compiles (my solution for now)\n" +
			   "    final W<C> w = new W<>(c, o);\n" +
			   "    return inspect(w);\n" +
			   "  }\n" +
			   "}\n",
		   },
		   "----------\n" +
			"1. ERROR in X.java (at line 34)\n" +
			"	return inspect(new W<C>(c, o)); // - WARNING: Redundant specification of type arguments <F.C> F.java\n" +
			"	                   ^\n" +
			"Redundant specification of type arguments <X.C>\n" +
			"----------\n",
			null, false, customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449619,  [1.8][compiler] Qualified <> allocation fails to compile.
public void test449619() {
	String source = "public class X {\n" +
			   "	public class Y<T> {\n" +
			   "	}\n" +
			   "	static void foo(Y<String> ys) {\n" +
			   "	}\n" +
			   "	public static void main(String[] args) {\n" +
			   "		foo(new X().new Y<>());\n" +
			   "	}\n" +
			   "}\n";
	if (this.complianceLevel >= ClassFileConstants.JDK1_8)
		this.runConformTest(
		   new String[] {
			   "X.java",
			   source,
		   },
		   "");
	else
		this.runNegativeTest(
		   new String[] {
			   "X.java",
			   source,
		   },
		   "----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	foo(new X().new Y<>());\n" +
			"	^^^\n" +
			"The method foo(X.Y<String>) in the type X is not applicable for the arguments (X.Y<Object>)\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429733, [1.8][bytecode] Bad type on operand stack
public void test429733() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		test(new Some<>(1.1d));\n" +
			"	}\n" +
			"	static <S> void test(Option<S> value) {\n" +
			"	}\n" +
			"	static interface Option<T> {\n" +
			"	}\n" +
			"	static class Some<T> implements Option<T> {\n" +
			"		Some(T value) {\n" +
			"         System.out.println(value);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"1.1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429733, [1.8][bytecode] Bad type on operand stack
public void test429733a() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		test(new Some<Double>(1.1d));\n" +
			"	}\n" +
			"	static <S> void test(Option<S> value) {\n" +
			"	}\n" +
			"	static interface Option<T> {\n" +
			"	}\n" +
			"	static class Some<T> implements Option<T> {\n" +
			"		Some(T value) {\n" +
			"         System.out.println(value);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"1.1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375394
public void test375394a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    B<C, ? extends C<C>, ? extends C<C>> b = new B<>();\n" +
			"}\n" +
			"class B <T, U extends C<T>, V extends U>{}\n" +
			"class C<T> {}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	B<C, ? extends C<C>, ? extends C<C>> b = new B<>();\n" +
		"	  ^\n" +
		"C is a raw type. References to generic type C<T> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 2)\n" +
		"	B<C, ? extends C<C>, ? extends C<C>> b = new B<>();\n" +
		"	                 ^\n" +
		"C is a raw type. References to generic type C<T> should be parameterized\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 2)\n" +
		"	B<C, ? extends C<C>, ? extends C<C>> b = new B<>();\n" +
		"	                                 ^\n" +
		"C is a raw type. References to generic type C<T> should be parameterized\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427728, [1.8] Type Inference rejects calls requiring boxing/unboxing
public void test427728b() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Collections;\n" +
			"import java.util.LinkedHashMap;\n" +
			"import java.util.Map;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		   Map<X, Integer> map = new LinkedHashMap<>();\n" +
			"		   map.put(null, X.getInt());\n" +
			"		   map.put(null, X.getint());\n" +
			"		}\n" +
			"		private static <T> int getInt() {\n" +
			"		   return 0;\n" +
			"		}\n" +
			"		private static int getint() {\n" +
			"			   return 0;\n" +
			"		}\n" +
			"}\n"
		},
		"");
}
public void testBug456508() {
	runNegativeTest(
		new String[] {
			"QueryAtom.java",
			"public class QueryAtom<T, P> {\n" +
			"	public QueryAtom(SingularAttribute<? super T, P> path) {\n" +
			"	}\n" +
			"}\n",
			"SubqueryIn.java",
			"public class SubqueryIn<S, P>  {\n" +
			"	public SubqueryIn(QueryAtom<S, P>... subqueryAtoms) {\n" +
			"	}\n" +
			"}\n",
			"Test.java",
			"class PAccount {}\n" +
			"class PGroepAccount {}\n" +
			"interface SingularAttribute<X, T> {}\n" +
			"\n" +
			"public class Test {\n" +
			"    public static volatile SingularAttribute<PGroepAccount, PAccount> account;\n" +
			"\n" +
			"	public void nietInGroep() {\n" +
			"		recordFilter(new SubqueryIn<>(new QueryAtom<>(account)));\n" +
			"	}\n" +
			"\n" +
			"	protected <P> void recordFilter(SubqueryIn<?, P> atom) {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in SubqueryIn.java (at line 2)\n" +
		"	public SubqueryIn(QueryAtom<S, P>... subqueryAtoms) {\n" +
		"	                                     ^^^^^^^^^^^^^\n" +
		"Type safety: Potential heap pollution via varargs parameter subqueryAtoms\n" +
		"----------\n" +
		"----------\n" +
		"1. WARNING in Test.java (at line 9)\n" +
		"	recordFilter(new SubqueryIn<>(new QueryAtom<>(account)));\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: A generic array of QueryAtom<PGroepAccount,PAccount> is created for a varargs parameter\n" +
		"----------\n");
}
public void testBug462083() {
	runConformTest(
		new String[] {
			"Java8InterfaceTest.java",
			"public abstract class Java8InterfaceTest\n" +
			"{\n" +
			"	public static interface Entity {}\n" +
			"\n" +
			"	public static interface Service<T1 extends Entity> {}\n" +
			"\n" +
			"    public static interface ServiceLocator<T2 extends Entity> {}\n" +
			"\n" +
			"    public static class ConcreteClass<T3 extends Entity, S extends Service<T3>> implements ServiceLocator<T3> {}\n" +
			"\n" +
			"    protected abstract <T4 extends Entity> ConcreteClass<T4, ?> getParameterized(T4 entity);\n" +
			"\n" +
			"    protected <T5 extends Entity> ServiceLocator<T5> getInterface(T5 entity)\n" +
			"    {\n" +
			"    	return getParameterized(entity);\n" +
			"    }\n" +
			"}\n"
		});
}
public void testBug469653() {
	String codeContent =
		"import java.util.*;\n" +
		"\n" +
		"class ImmutableList<E> {\n" +
		"	static <F> ImmutableList<F> copyOf(Iterable<? extends F> in) { return null; }\n" +
		"	ImmutableList<E> reverse() { return this; }\n" +
		"	Iterator<E> iterator() { return null; }\n" +
		"}\n" +
		"public class Code {\n" +
		"  public static void test() {\n" +
		"      Iterable<? extends String> services = null;\n" +
		"      Iterator<String> reverseServices = ImmutableList.copyOf(services).reverse().iterator();\n" +
		"  }\n" +
		"}";
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		runConformTest(
			new String[] { "Code.java", codeContent });
	} else {
		runNegativeTest(
			new String[] { "Code.java", codeContent },
			"----------\n" +
			"1. ERROR in Code.java (at line 11)\n" +
			"	Iterator<String> reverseServices = ImmutableList.copyOf(services).reverse().iterator();\n" +
			"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from Iterator<capture#1-of ? extends String> to Iterator<String>\n" +
			"----------\n");
	}
}
public void testBug488649_JDK6791481_ex1() {
	int count = 1;
	runNegativeTest(
		new String[] {
			"Test.java",
			"class Test<X> {\n" +
			"	X m(Class<X> c) {return null;}\n" +
			"	X x = m((Class)String.class);\n" +
			"}\n"
		},
		"----------\n" +
		(this.complianceLevel >= ClassFileConstants.JDK1_8
			?
		(count++)+". ERROR in Test.java (at line 3)\n" +
		"	X x = m((Class)String.class);\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Object to X\n" + // <- want to see this error, but at 1.7- we keep javac compatibility
		"----------\n"
			:
		""
		)+
		(count++)+". WARNING in Test.java (at line 3)\n" +
		"	X x = m((Class)String.class);\n" +
		"	        ^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The expression of type Class needs unchecked conversion to conform to Class<X>\n" +
		"----------\n" +
		(count++)+". WARNING in Test.java (at line 3)\n" +
		"	X x = m((Class)String.class);\n" +
		"	         ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n");
}
public void testGH1326() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	runner.testFiles = new String[] {
		"Foo.java",
		"""
		class Inner<T> {}
		class Outer<T> {
			Outer(T t) {}
			Outer<T> self() { return this; }
		}
		public class Foo {
			Outer<Inner<String>> x = new Outer<>(new Inner<String>()).self();
		}
		"""
	};
	runner.runConformTest();
}
public void testGH1326_alt() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	runner.testFiles = new String[] {
		"Foo.java",
		"""
		class Inner<T> {}
		class Outer<T> {
			Outer(Inner<String> t1, T t2) {}
			Outer<T> self() { return this; }
		}
		public class Foo {
			Inner<String> inner = new Inner<>();
			Outer<Inner<String>> x = new Outer<>(new Inner<String>(), inner).self();
			Outer<Inner<String>> xok = new Outer<>(new Inner<>(), inner).self();
		}
		"""
	};
	runner.expectedCompilerLog =
			this.complianceLevel >= ClassFileConstants.JDK1_8
			?
			"""
			----------
			1. ERROR in Foo.java (at line 8)
				Outer<Inner<String>> x = new Outer<>(new Inner<String>(), inner).self();
				                                         ^^^^^
			Redundant specification of type arguments <String>
			----------
			"""
			: // 1.7 inference is less capable:
			"""
			----------
			1. ERROR in Foo.java (at line 8)
				Outer<Inner<String>> x = new Outer<>(new Inner<String>(), inner).self();
				                                         ^^^^^
			Redundant specification of type arguments <String>
			----------
			2. ERROR in Foo.java (at line 9)
				Outer<Inner<String>> xok = new Outer<>(new Inner<>(), inner).self();
				                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Cannot infer type arguments for Outer<>
			----------
			""";
	runner.runNegativeTest();
}
public static Class testClass() {
	return GenericsRegressionTest_1_7.class;
}
}