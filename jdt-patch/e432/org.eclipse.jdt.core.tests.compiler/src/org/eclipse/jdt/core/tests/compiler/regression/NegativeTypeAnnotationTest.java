/*******************************************************************************
 * Copyright (c) 2011, 2017 IBM Corporation and others.
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

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.EclipseJustification;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NegativeTypeAnnotationTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 35 };
//		TESTS_NAMES = new String [] { "test0390882b" };
	}
	public static Class testClass() {
		return NegativeTypeAnnotationTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public NegativeTypeAnnotationTest(String testName){
		super(testName);
	}
	public void test001() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker2 Object {}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X extends @Marker2 Object {}\n" +
				"	                        ^^^^^^^\n" +
				"Marker2 cannot be resolved to a type\n" +
				"----------\n");
	}
	public void test002() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.io.Serializable;\n" +
					"public class X implements @Marker2 Serializable {\n" +
					"	private static final long serialVersionUID = 1L;\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public class X implements @Marker2 Serializable {\n" +
				"	                           ^^^^^^^\n" +
				"Marker2 cannot be resolved to a type\n" +
				"----------\n");
	}
	public void test003() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker Object {}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X extends @Marker Object {}\n" +
				"	                        ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	public void test004() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<@Marker T> {}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X<@Marker T> {}\n" +
				"	                ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	public void test005() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X<@Marker T> {}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X<@Marker T> {}\n" +
				"	                ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	public void test006() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"class Y {}\n",
				"X.java",
				"public class X extends @A(id=\"Hello, World!\") @B @C('(') Y {\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X extends @A(id=\"Hello, World!\") @B @C(\'(\') Y {\n" +
		"	                        ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X extends @A(id=\"Hello, World!\") @B @C(\'(\') Y {\n" +
		"	                                               ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 1)\n" +
		"	public class X extends @A(id=\"Hello, World!\") @B @C(\'(\') Y {\n" +
		"	                                                  ^\n" +
		"C cannot be resolved to a type\n" +
		"----------\n");
	}
	public void test007() throws Exception {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"interface I {}\n",
				"J.java",
				"interface J {}\n",
				"X.java",
				"public class X implements @A(id=\"Hello, World!\") I, @B @C('(') J {}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X implements @A(id=\"Hello, World!\") I, @B @C(\'(\') J {}\n" +
		"	                           ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X implements @A(id=\"Hello, World!\") I, @B @C(\'(\') J {}\n" +
		"	                                                     ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 1)\n" +
		"	public class X implements @A(id=\"Hello, World!\") I, @B @C(\'(\') J {}\n" +
		"	                                                        ^\n" +
		"C cannot be resolved to a type\n" +
		"----------\n");
	}
	public void test010() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"class Y<T> {}\n",
				"X.java",
				"public class X extends @A(\"Hello, World!\") Y<@B @C('(') String> {\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X extends @A(\"Hello, World!\") Y<@B @C(\'(\') String> {\n" +
		"	                        ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X extends @A(\"Hello, World!\") Y<@B @C(\'(\') String> {\n" +
		"	                                              ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 1)\n" +
		"	public class X extends @A(\"Hello, World!\") Y<@B @C(\'(\') String> {\n" +
		"	                                                 ^\n" +
		"C cannot be resolved to a type\n" +
		"----------\n");
	}
	public void test011() throws Exception {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"interface I<T> {}\n",
				"J.java",
				"interface J<T> {}\n",
				"X.java",
				"public class X implements I<@A(\"Hello, World!\") String>,  @B J<@C('(') Integer> {}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X implements I<@A(\"Hello, World!\") String>,  @B J<@C(\'(\') Integer> {}\n" +
		"	                             ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X implements I<@A(\"Hello, World!\") String>,  @B J<@C(\'(\') Integer> {}\n" +
		"	                                                           ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 1)\n" +
		"	public class X implements I<@A(\"Hello, World!\") String>,  @B J<@C(\'(\') Integer> {}\n" +
		"	                                                                ^\n" +
		"C cannot be resolved to a type\n" +
		"----------\n");
	}
	// throws
	public void test012() throws Exception {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"class E extends RuntimeException {\n" +
				"	private static final long serialVersionUID = 1L;\n" +
				"}\n",
				"E1.java",
				"class E1 extends RuntimeException {\n" +
				"	private static final long serialVersionUID = 1L;\n" +
				"}\n",
				"E2.java",
				"class E2 extends RuntimeException {\n" +
				"	private static final long serialVersionUID = 1L;\n" +
				"}\n",
				"X.java",
				"public class X {\n" +
				"	void foo() throws @A(\"Hello, World!\") E, E1, @B @C('(') E2 {}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo() throws @A(\"Hello, World!\") E, E1, @B @C(\'(\') E2 {}\n" +
		"	                   ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	void foo() throws @A(\"Hello, World!\") E, E1, @B @C(\'(\') E2 {}\n" +
		"	                                              ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	void foo() throws @A(\"Hello, World!\") E, E1, @B @C(\'(\') E2 {}\n" +
		"	                                                 ^\n" +
		"C cannot be resolved to a type\n" +
		"----------\n");
	}
	// method receiver
	public void test013() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo(@B(3) X this) {}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void foo(@B(3) X this) {}\n" +
		"	          ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// method return type
	public void test014() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@B(3) int foo() {\n" +
				"		return 1;\n" +
				"	}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@B(3) int foo() {\n" +
		"	 ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// field type
	public void test015() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@B(3) int field;\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@B(3) int field;\n" +
		"	 ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// method parameter
	public void test016() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	int foo(@B(3) String s) {\n" +
				"		return s.length();\n" +
				"	}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int foo(@B(3) String s) {\n" +
		"	         ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// method parameter generic or array
	public void test017() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	int foo(String @B(3) [] s) {\n" +
				"		return s.length;\n" +
				"	}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int foo(String @B(3) [] s) {\n" +
		"	                ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// field type generic or array
	public void test018() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	int @B(3) [] field;\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int @B(3) [] field;\n" +
		"	     ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// class type parameter
	public void test019() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<@A @B(3) T> {}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X<@A @B(3) T> {}\n" +
		"	                ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X<@A @B(3) T> {}\n" +
		"	                   ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// method type parameter
	public void test020() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	<@A @B(3) T> void foo(T t) {}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	<@A @B(3) T> void foo(T t) {}\n" +
		"	  ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	<@A @B(3) T> void foo(T t) {}\n" +
		"	     ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// class type parameter bound
	public void test021() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z {}",
				"X.java",
				"public class X<T extends @A Z & @B(3) Cloneable> {}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X<T extends @A Z & @B(3) Cloneable> {}\n" +
		"	                          ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X<T extends @A Z & @B(3) Cloneable> {}\n" +
		"	                                 ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// class type parameter bound generic or array
	public void test022() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}\n" +
		"	                            ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 1)\n" +
		"	public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}\n" +
		"	                                      ^\n" +
		"C cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 1)\n" +
		"	public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}\n" +
		"	                                            ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 1)\n" +
		"	public class X<T extends Y<@A String @C[][]@B[]> & @B(3) Cloneable> {}\n" +
		"	                                                    ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// method type parameter bound
	public void test023() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z {}",
				"X.java",
				"public class X {\n" +
				"	<T extends @A Z & @B(3) Cloneable> void foo(T t) {}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	<T extends @A Z & @B(3) Cloneable> void foo(T t) {}\n" +
		"	            ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	<T extends @A Z & @B(3) Cloneable> void foo(T t) {}\n" +
		"	                   ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// class type parameter bound generic or array
	public void test024() throws Exception {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z {}",
				"Y.java",
				"public class Y<T> {}",
				"X.java",
				"public class X {\n" +
				"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" +
		"	              ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" +
		"	                   ^\n" +
		"C cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 2)\n" +
		"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" +
		"	                         ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 2)\n" +
		"	<T extends Y<@A Z @C[][]@B[]> & @B(3) Cloneable> void foo(T t) {}\n" +
		"	                                 ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// local variable + generic or array
	public void test025() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo(String s) {\n" +
				"		@C int i;\n" +
				"		@A String [] @B(3)[] tab = new String[][] {};\n" +
				"		if (tab != null) {\n" +
				"			i = 0;\n" +
				"			System.out.println(i + tab.length);\n" +
				"		} else {\n" +
				"			System.out.println(tab.length);\n" +
				"		}\n" +
				"		i = 4;\n" +
				"		System.out.println(-i + tab.length);\n" +
				"	}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	@C int i;\n" +
		"	 ^\n" +
		"C cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	@A String [] @B(3)[] tab = new String[][] {};\n" +
		"	 ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	@A String [] @B(3)[] tab = new String[][] {};\n" +
		"	              ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// type argument constructor call
	public void test026() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	<T> X(T t) {\n" +
				"	}\n" +
				"	public Object foo() {\n" +
				"		X x = new <@A @B(1) String>X(null);\n" +
				"		return x;\n" +
				"	}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	X x = new <@A @B(1) String>X(null);\n" +
		"	            ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	X x = new <@A @B(1) String>X(null);\n" +
		"	               ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// type argument constructor call generic or array
	public void test027() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	<T> X(T t) {\n" +
				"	}\n" +
				"	public Object foo() {\n" +
				"		X x = new <@A @B(1) String>X(null);\n" +
				"		return x;\n" +
				"	}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	X x = new <@A @B(1) String>X(null);\n" +
		"	            ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	X x = new <@A @B(1) String>X(null);\n" +
		"	               ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n");
	}
	// type argument method call and generic or array
	public void test028() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	static <T, U> T foo(T t, U u) {\n" +
				"		return t;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(X.<@A @B(1) String[], @C('-') X>foo(new String[]{\"SUCCESS\"}, null)[0]);\n" +
				"	}\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	System.out.println(X.<@A @B(1) String[], @C(\'-\') X>foo(new String[]{\"SUCCESS\"}, null)[0]);\n" +
		"	                       ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	System.out.println(X.<@A @B(1) String[], @C(\'-\') X>foo(new String[]{\"SUCCESS\"}, null)[0]);\n" +
		"	                          ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	System.out.println(X.<@A @B(1) String[], @C(\'-\') X>foo(new String[]{\"SUCCESS\"}, null)[0]);\n" +
		"	                                          ^\n" +
		"C cannot be resolved to a type\n" +
		"----------\n");
	}
	public void test029() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X extends @Marker2 Object {}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X extends @Marker2 Object {}\n" +
				"	                        ^^^^^^^\n" +
				"Marker2 cannot be resolved to a type\n" +
				"----------\n");
	}
	public void test030() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.io.Serializable;\n" +
					"public class X implements @Marker2 Serializable {\n" +
					"	private static final long serialVersionUID = 1L;\n" +
					"}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public class X implements @Marker2 Serializable {\n" +
				"	                           ^^^^^^^\n" +
				"Marker2 cannot be resolved to a type\n" +
				"----------\n");
	}
	public void test031() throws Exception {
		this.runNegativeTest(
				new String[] {
					"Marker.java",
					"import java.lang.annotation.Target;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"@Target(TYPE)\n" +
					"@interface Marker {}",
					"X.java",
					"public class X<@Marker T> {}",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n" +
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X<@Marker T> {}\n" +
				"	               ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n");
	}
	public void test032() throws Exception {
		this.runConformTest(
				new String[] {
					"Marker.java",
					"@interface Marker {}",
					"X.java",
					"public class X<@Marker T> {}",
				},
				"");

	}
	public void test033() throws Exception {
		this.runNegativeTest(
				new String[] {
					"Marker.java",
					"@interface Marker {}",
					"Y.java",
					"public class Y {}",
					"X.java",
					"public class X extends @Marker Y {}",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X extends @Marker Y {}\n" +
				"	                       ^^^^^^^\n" +
				"Annotation types that do not specify explicit target element types cannot be applied here\n" +
				"----------\n");
	}
	// check locations
	public void test034() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.List;\n" +
				"public class X {\n" +
				"	@H String @E[] @F[] @G[] field;\n" +
				"	@A Map<@B String, @C List<@D Object>> field2;\n" +
				"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	@H String @E[] @F[] @G[] field;\n" +
		"	 ^\n" +
		"H cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	@H String @E[] @F[] @G[] field;\n" +
		"	           ^\n" +
		"E cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	@H String @E[] @F[] @G[] field;\n" +
		"	                ^\n" +
		"F cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	@H String @E[] @F[] @G[] field;\n" +
		"	                     ^\n" +
		"G cannot be resolved to a type\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 5)\n" +
		"	@A Map<@B String, @C List<@D Object>> field2;\n" +
		"	 ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 5)\n" +
		"	@A Map<@B String, @C List<@D Object>> field2;\n" +
		"	        ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 5)\n" +
		"	@A Map<@B String, @C List<@D Object>> field2;\n" +
		"	                   ^\n" +
		"C cannot be resolved to a type\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 5)\n" +
		"	@A Map<@B String, @C List<@D Object>> field2;\n" +
		"	                           ^\n" +
		"D cannot be resolved to a type\n" +
		"----------\n" +
		"9. ERROR in X.java (at line 6)\n" +
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" +
		"	 ^\n" +
		"A cannot be resolved to a type\n" +
		"----------\n" +
		"10. ERROR in X.java (at line 6)\n" +
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" +
		"	        ^\n" +
		"B cannot be resolved to a type\n" +
		"----------\n" +
		"11. ERROR in X.java (at line 6)\n" +
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" +
		"	                   ^\n" +
		"H cannot be resolved to a type\n" +
		"----------\n" +
		"12. ERROR in X.java (at line 6)\n" +
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" +
		"	                             ^\n" +
		"E cannot be resolved to a type\n" +
		"----------\n" +
		"13. ERROR in X.java (at line 6)\n" +
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" +
		"	                                  ^\n" +
		"F cannot be resolved to a type\n" +
		"----------\n" +
		"14. ERROR in X.java (at line 6)\n" +
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" +
		"	                                       ^\n" +
		"G cannot be resolved to a type\n" +
		"----------\n");
	}
	// check locations
	public void test035() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Map;\n" +
				"import java.util.List;\n" +
				"public class X {\n" +
				"	@H java.lang.String @E[] @F[] @G[] field;\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	@H java.lang.String @E[] @F[] @G[] field;\n" +
		"	 ^\n" +
		"H cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	@H java.lang.String @E[] @F[] @G[] field;\n" +
		"	                     ^\n" +
		"E cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	@H java.lang.String @E[] @F[] @G[] field;\n" +
		"	                          ^\n" +
		"F cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	@H java.lang.String @E[] @F[] @G[] field;\n" +
		"	                               ^\n" +
		"G cannot be resolved to a type\n" +
		"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383884 -- Compiler tolerates illegal dimension annotation in class literal expressions
	public void test036() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" +
				"    System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" +
				"    System.out.println(int [] [] [] [] [].class);\n" +
				"    System.out.println(X [] [] [] [] [].class);\n" +
				"  }\n" +
				"}\n" +
				"@interface Empty {\n" +
				"}\n" +
				"@interface NonEmpty {\n" +
				"}\n",
		},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" +
			"	                       ^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" +
			"	                                       ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	System.out.println(int @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" +
			"	                                                              ^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 4)\n" +
			"	System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" +
			"	                     ^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 4)\n" +
			"	System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" +
			"	                                     ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 4)\n" +
			"	System.out.println(X @NonEmpty [] [] @NonEmpty @Empty [] [] @NonEmpty[].class); // illegal!\n" +
			"	                                                            ^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n");
	}
	public void test037() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"@interface Marker {}\n" +
					"@Marker	// line 2: Don't complain \n" +
					"public class X<@Marker T>  extends @Marker Object{		// 3: Complain only on super type and not on class type parameter\n" +
					"	public @Marker Object foo(@Marker Object obj) {  // 4: Don't complain on both\n" +
					"		return null;\n" +
					"	}\n" +
					"}\n",
				},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	public class X<@Marker T>  extends @Marker Object{		// 3: Complain only on super type and not on class type parameter\n" +
				"	                                   ^^^^^^^\n" +
				"Annotation types that do not specify explicit target element types cannot be applied here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383950
	// [1.8][compiler] Type annotations must have target type meta annotation TYPE_USE
	public void test038() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.Target;\n" +
						"import static java.lang.annotation.ElementType.*;\n" +
						"@Target({PACKAGE, TYPE, METHOD, FIELD, CONSTRUCTOR, PARAMETER, LOCAL_VARIABLE})\n" +
						"@interface Marker {}\n" +
						"public class X<@Marker T>  extends @Marker Object{		// 3: Complain \n" +
						"}\n",
					},
					"----------\n" +
					"1. ERROR in X.java (at line 5)\n" +
					"	public class X<@Marker T>  extends @Marker Object{		// 3: Complain \n" +
					"	               ^^^^^^^\n" +
					"The annotation @Marker is disallowed for this location\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 5)\n" +
					"	public class X<@Marker T>  extends @Marker Object{		// 3: Complain \n" +
					"	                                   ^^^^^^^\n" +
					"The annotation @Marker is disallowed for this location\n" +
					"----------\n");
	}
	// JSR 308: "It is not permitted to annotate the type name in an import statement."
	public void test039() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import @Marker java.lang.String; // Compilation error \n" +
						"public class X { \n" +
						"}\n" +
						"@interface Marker {}\n"
					},
					"----------\n" +
					"1. ERROR in X.java (at line 1)\n" +
					"	import @Marker java.lang.String; // Compilation error \n" +
					"	       ^^^^^^^\n" +
					"Syntax error, type annotations are illegal here\n" +
					"----------\n");
	}
	// Test that type name can't be left out in a cast expression with an annotations
	public void test040() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X { \n" +
						"	public void foo(Object myObject) {\n" +
						"		String myString = (@NonNull) myObject;" +
						"	}\n" +
						"}\n" +
						"@interface NonNull {}\n"
					},
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	String myString = (@NonNull) myObject;	}\n" +
					"	                   ^\n" +
					"Syntax error on token \"@\", delete this token\n" +
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385111
	// [1.8][compiler] Compiler fails to flag undefined annotation type.
	public void test0385111() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.util.ArrayList;\n" +
						"import java.util.List;\n" +
						"public class X {\n" +
						"    public void foo(String fileName) {\n" +
						"        List<String> l = new @MissingTypeNotIgnored ArrayList<String>();\n" +
						"        List<String> l1 = new @MissingTypeIgnored ArrayList<>();\n" +
						"    }\n" +
						"}\n",
					},
					"----------\n" +
					"1. ERROR in X.java (at line 5)\n" +
					"	List<String> l = new @MissingTypeNotIgnored ArrayList<String>();\n" +
					"	                      ^^^^^^^^^^^^^^^^^^^^^\n" +
					"MissingTypeNotIgnored cannot be resolved to a type\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 6)\n" +
					"	List<String> l1 = new @MissingTypeIgnored ArrayList<>();\n" +
					"	                       ^^^^^^^^^^^^^^^^^^\n" +
					"MissingTypeIgnored cannot be resolved to a type\n" +
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385111
	// Test to exercise assorted cleanup along with bug fix.
	public void test0385111a() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    public void foo(String fileName) {\n" +
						"        try (@Annot X x = null; @Annot X x2 = null) {\n"+
						"        } catch (@Annot NullPointerException | @Annot UnsupportedOperationException e) {\n" +
						"        }\n" +
						"    }\n" +
						"}\n",
					},
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	try (@Annot X x = null; @Annot X x2 = null) {\n" +
					"	      ^^^^^\n" +
					"Annot cannot be resolved to a type\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 3)\n" +
					"	try (@Annot X x = null; @Annot X x2 = null) {\n" +
					"	            ^\n" +
					"The resource type X does not implement java.lang.AutoCloseable\n" +
					"----------\n" +
					"3. ERROR in X.java (at line 3)\n" +
					"	try (@Annot X x = null; @Annot X x2 = null) {\n" +
					"	                         ^^^^^\n" +
					"Annot cannot be resolved to a type\n" +
					"----------\n" +
					"4. ERROR in X.java (at line 3)\n" +
					"	try (@Annot X x = null; @Annot X x2 = null) {\n" +
					"	                               ^\n" +
					"The resource type X does not implement java.lang.AutoCloseable\n" +
					"----------\n" +
					"5. ERROR in X.java (at line 4)\n" +
					"	} catch (@Annot NullPointerException | @Annot UnsupportedOperationException e) {\n" +
					"	          ^^^^^\n" +
					"Annot cannot be resolved to a type\n" +
					"----------\n" +
					"6. ERROR in X.java (at line 4)\n" +
					"	} catch (@Annot NullPointerException | @Annot UnsupportedOperationException e) {\n" +
					"	                                        ^^^^^\n" +
					"Annot cannot be resolved to a type\n" +
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913() {
		this.runNegativeTest(
				new String[]{
						"X.java",
						"public class X {\n" +
						"	public void foo(Object obj, X this) {}\n" +
						"	public void foo(Object obj1, X this, Object obj2) {}\n" +
						"	public void foo(Object obj, Object obj2, Object obj3, X this) {}\n" +
						"	class Y {\n" +
						"		Y(Object obj, Y Y.this){}\n" +
						"	}\n" +
						"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public void foo(Object obj, X this) {}\n" +
				"	                              ^^^^\n" +
				"Only the first formal parameter may be declared explicitly as 'this'\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	public void foo(Object obj1, X this, Object obj2) {}\n" +
				"	                               ^^^^\n" +
				"Only the first formal parameter may be declared explicitly as 'this'\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 4)\n" +
				"	public void foo(Object obj, Object obj2, Object obj3, X this) {}\n" +
				"	                                                        ^^^^\n" +
				"Only the first formal parameter may be declared explicitly as 'this'\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	Y(Object obj, Y Y.this){}\n" +
				"	                  ^^^^\n" +
				"Only the first formal parameter may be declared explicitly as 'this'\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913b() {
		this.runNegativeTest(
				new String[] {
						"Outer.java",
						"public class Outer {\n" +
						"    Outer(Outer Outer.this) {}\n" +
						"    Outer(Outer this, int i) {}\n" +
						"    class Inner<K,V> {\n" +
						"        class InnerMost<T> {\n" +
						"            InnerMost(Outer.Inner this) {}\n" +
						"            InnerMost(Outer.Inner Outer.Inner.this, int i, float f) {}\n" +
						"            InnerMost(Outer Outer.this, float f) {}\n" +
						"            InnerMost(Outer.Inner<K,V>.InnerMost<T> Outer.Inner.InnerMost.this, Object obj) {}\n" +
						"            InnerMost(Inner<K,V> Outer.Inner.InnerMost.this, int i) {}\n" +
						"            InnerMost(Outer.Inner<K, V> this, float f, int i) {}\n" +
						"            InnerMost(Outer.Inner<K,V> Inner.this, long l) {}\n" +
						"        }\n" +
						"    }\n" +
						"}\n"},
						"----------\n" +
						"1. ERROR in Outer.java (at line 2)\n" +
						"	Outer(Outer Outer.this) {}\n" +
						"	                  ^^^^\n" +
						"Explicit 'this' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" +
						"----------\n" +
						"2. ERROR in Outer.java (at line 3)\n" +
						"	Outer(Outer this, int i) {}\n" +
						"	            ^^^^\n" +
						"Explicit 'this' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" +
						"----------\n" +
						"3. WARNING in Outer.java (at line 6)\n" +
						"	InnerMost(Outer.Inner this) {}\n" +
						"	          ^^^^^^^^^^^\n" +
						"Outer.Inner is a raw type. References to generic type Outer.Inner<K,V> should be parameterized\n" +
						"----------\n" +
						"4. ERROR in Outer.java (at line 6)\n" +
						"	InnerMost(Outer.Inner this) {}\n" +
						"	          ^^^^^^^^^^^\n" +
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>\n" +
						"----------\n" +
						"5. ERROR in Outer.java (at line 6)\n" +
						"	InnerMost(Outer.Inner this) {}\n" +
						"	                      ^^^^\n" +
						"The explicit 'this' parameter is expected to be qualified with Inner\n" +
						"----------\n" +
						"6. WARNING in Outer.java (at line 7)\n" +
						"	InnerMost(Outer.Inner Outer.Inner.this, int i, float f) {}\n" +
						"	          ^^^^^^^^^^^\n" +
						"Outer.Inner is a raw type. References to generic type Outer.Inner<K,V> should be parameterized\n" +
						"----------\n" +
						"7. ERROR in Outer.java (at line 7)\n" +
						"	InnerMost(Outer.Inner Outer.Inner.this, int i, float f) {}\n" +
						"	          ^^^^^^^^^^^\n" +
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>\n" +
						"----------\n" +
						"8. ERROR in Outer.java (at line 7)\n" +
						"	InnerMost(Outer.Inner Outer.Inner.this, int i, float f) {}\n" +
						"	                      ^^^^^^^^^^^^^^^^\n" +
						"The explicit 'this' parameter is expected to be qualified with Inner\n" +
						"----------\n" +
						"9. ERROR in Outer.java (at line 8)\n" +
						"	InnerMost(Outer Outer.this, float f) {}\n" +
						"	          ^^^^^\n" +
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>\n" +
						"----------\n" +
						"10. ERROR in Outer.java (at line 8)\n" +
						"	InnerMost(Outer Outer.this, float f) {}\n" +
						"	                ^^^^^^^^^^\n" +
						"The explicit 'this' parameter is expected to be qualified with Inner\n" +
						"----------\n" +
						"11. ERROR in Outer.java (at line 9)\n" +
						"	InnerMost(Outer.Inner<K,V>.InnerMost<T> Outer.Inner.InnerMost.this, Object obj) {}\n" +
						"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>\n" +
						"----------\n" +
						"12. ERROR in Outer.java (at line 9)\n" +
						"	InnerMost(Outer.Inner<K,V>.InnerMost<T> Outer.Inner.InnerMost.this, Object obj) {}\n" +
						"	                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
						"The explicit 'this' parameter is expected to be qualified with Inner\n" +
						"----------\n" +
						"13. ERROR in Outer.java (at line 10)\n" +
						"	InnerMost(Inner<K,V> Outer.Inner.InnerMost.this, int i) {}\n" +
						"	                     ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
						"The explicit 'this' parameter is expected to be qualified with Inner\n" +
						"----------\n" +
						"14. ERROR in Outer.java (at line 11)\n" +
						"	InnerMost(Outer.Inner<K, V> this, float f, int i) {}\n" +
						"	                            ^^^^\n" +
						"The explicit 'this' parameter is expected to be qualified with Inner\n" +
						"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913c() {
		this.runNegativeTest(
				new String[] {
						"Outer.java",
						"public class Outer {\n" +
						"    class Inner<K,V> {\n" +
						"        class InnerMost<T> {\n" +
						"            public void foo(Outer Outer.this) {}\n" +
						"            public void foo(Inner<K,V> Inner.this, int i) {}\n" +
						"            public void foo(InnerMost this, int i, int j) {}\n" +
						"            public void foo(Inner.InnerMost<T> this, Object obj) {}\n" +
						"            public void foo(InnerMost<T> this, float f) {}\n" +
						"            public void foo(Inner<K,V>.InnerMost<T> this, long l) {}\n" +
						"            public void foo(Outer.Inner<K,V>.InnerMost<T> this, float f, float ff) {}\n" +
						"            public void foo(InnerMost<T> Outer.Inner.InnerMost.this, int i, float f) {}\n" +
						"        }\n" +
						"    }\n" +
						"}\n"},
						"----------\n" +
						"1. ERROR in Outer.java (at line 4)\n" +
						"	public void foo(Outer Outer.this) {}\n" +
						"	                ^^^^^\n" +
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>\n" +
						"----------\n" +
						"2. ERROR in Outer.java (at line 4)\n" +
						"	public void foo(Outer Outer.this) {}\n" +
						"	                      ^^^^^^^^^^\n" +
						"The explicit 'this' parameter for a method cannot have a qualifying name\n" +
						"----------\n" +
						"3. ERROR in Outer.java (at line 5)\n" +
						"	public void foo(Inner<K,V> Inner.this, int i) {}\n" +
						"	                ^^^^^\n" +
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>\n" +
						"----------\n" +
						"4. ERROR in Outer.java (at line 5)\n" +
						"	public void foo(Inner<K,V> Inner.this, int i) {}\n" +
						"	                           ^^^^^^^^^^\n" +
						"The explicit 'this' parameter for a method cannot have a qualifying name\n" +
						"----------\n" +
						"5. WARNING in Outer.java (at line 6)\n" +
						"	public void foo(InnerMost this, int i, int j) {}\n" +
						"	                ^^^^^^^^^\n" +
						"Outer.Inner.InnerMost is a raw type. References to generic type Outer.Inner<K,V>.InnerMost<T> should be parameterized\n" +
						"----------\n" +
						"6. ERROR in Outer.java (at line 6)\n" +
						"	public void foo(InnerMost this, int i, int j) {}\n" +
						"	                ^^^^^^^^^\n" +
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>\n" +
						"----------\n" +
						"7. ERROR in Outer.java (at line 7)\n" +
						"	public void foo(Inner.InnerMost<T> this, Object obj) {}\n" +
						"	                ^^^^^^^^^^^^^^^\n" +
						"The member type Outer.Inner.InnerMost<T> must be qualified with a parameterized type, since it is not static\n" +
						"----------\n" +
						"8. ERROR in Outer.java (at line 7)\n" +
						"	public void foo(Inner.InnerMost<T> this, Object obj) {}\n" +
						"	                ^^^^^^^^^^^^^^^\n" +
						"The declared type of the explicit 'this' parameter is expected to be Outer.Inner<K,V>.InnerMost<T>\n" +
						"----------\n" +
						"9. ERROR in Outer.java (at line 11)\n" +
						"	public void foo(InnerMost<T> Outer.Inner.InnerMost.this, int i, float f) {}\n" +
						"	                             ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
						"The explicit 'this' parameter for a method cannot have a qualifying name\n" +
						"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913
	public void test0383913d() {
		this.runNegativeTest(
				new String[] {
						"Outer.java",
						"import java.lang.annotation.Target;\n" +
						"import static java.lang.annotation.ElementType.*;\n" +
						"public class Outer {\n" +
						"    class Inner<K,V> {\n" +
						"		public Inner(@Missing Outer Outer.this) {}\n" +
						"        class InnerMost<T> {\n" +
						"            public void bar() {\n" +
						"                new AnonymousInner() {\n" +
						"                    public void foobar(AnonymousInner this) {}\n" +
						"                };\n" +
						"            }\n" +
						"            void bar(int i) {\n" +
						"                class Local {\n" +
						"                    public int hashCode(Local this, int k) { return 0; }\n" +
						"                    public int hashCode(Outer.Local this) { return 0; }\n" +
						"                }\n" +
						"            }\n" +
						"        }\n" +
						"    }\n" +
						"    static class StaticNested {\n" +
						"        public StaticNested(@Marker Outer.StaticNested Outer.StaticNested.this) {}\n" +
						"    }\n" +
						"    public static void foo(@Marker Outer this) {}\n" +
						"    public void foo(@Missing Outer this, int i) {}\n" +
						"}\n" +
						"interface AnonymousInner {\n" +
						"    public void foobar(AnonymousInner this);\n" +
						"}\n" +
						"@Target(TYPE_USE)\n" +
						"@interface Marker {}",

						"java/lang/annotation/ElementType.java",
						"package java.lang.annotation;\n" +
						"public enum ElementType {\n" +
						"    TYPE,\n" +
						"    FIELD,\n" +
						"    METHOD,\n" +
						"    PARAMETER,\n" +
						"    CONSTRUCTOR,\n" +
						"    LOCAL_VARIABLE,\n" +
						"    ANNOTATION_TYPE,\n" +
						"    PACKAGE,\n" +
						"    TYPE_PARAMETER,\n" +
						"    TYPE_USE\n" +
						"}\n"
					},
							"----------\n" +
							"1. ERROR in Outer.java (at line 5)\n" +
							"	public Inner(@Missing Outer Outer.this) {}\n" +
							"	              ^^^^^^^\n" +
							"Missing cannot be resolved to a type\n" +
							"----------\n" +
							"2. ERROR in Outer.java (at line 9)\n" +
							"	public void foobar(AnonymousInner this) {}\n" +
							"	                                  ^^^^\n" +
							"Explicit \'this\' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" +
							"----------\n" +
							"3. ERROR in Outer.java (at line 15)\n" +
							"	public int hashCode(Outer.Local this) { return 0; }\n" +
							"	                    ^^^^^^^^^^^\n" +
							"Outer.Local cannot be resolved to a type\n" +
							"----------\n" +
							"4. ERROR in Outer.java (at line 21)\n" +
							"	public StaticNested(@Marker Outer.StaticNested Outer.StaticNested.this) {}\n" +
							"	                    ^^^^^^^\n" +
							"Type annotations are not allowed on type names used to access static members\n" +
							"----------\n" +
							"5. ERROR in Outer.java (at line 21)\n" +
							"	public StaticNested(@Marker Outer.StaticNested Outer.StaticNested.this) {}\n" +
							"	                                                                  ^^^^\n" +
							"Explicit \'this\' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" +
							"----------\n" +
							"6. ERROR in Outer.java (at line 23)\n" +
							"	public static void foo(@Marker Outer this) {}\n" +
							"	                                     ^^^^\n" +
							"Explicit \'this\' parameter is allowed only in instance methods of non-anonymous classes and inner class constructors\n" +
							"----------\n" +
							"7. ERROR in Outer.java (at line 24)\n" +
							"	public void foo(@Missing Outer this, int i) {}\n" +
							"	                 ^^^^^^^\n" +
							"Missing cannot be resolved to a type\n" +
							"----------\n");
	}
	public void test0383908() {
		this.runNegativeTest(
				new String[]{"X.java",
				"public class X { \n" +
				"	void foo(X this) {}\n" +
				"   void foo() {}\n" +
				"}\n" +
				"class Y {\n" +
				"	void foo(Y this) {}\n" +
				"	public static void main(String[] args) {\n" +
				"		new Y().foo();\n" +
				"	}\n" +
				"}"},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	void foo(X this) {}\n" +
				"	     ^^^^^^^^^^^\n" +
				"Duplicate method foo() in type X\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	void foo() {}\n" +
				"	     ^^^^^\n" +
				"Duplicate method foo() in type X\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested package names.
	public void test383596() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
				"public class X {\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
			"	           ^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
			"	                        ^^^^^^^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 1)\n" +
			"	package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
			"	                                           ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested package names.
	public void test383596a() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
				"public class X {\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
			"	^^^^^^^\n" +
			"Package annotations must be in file package-info.java\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
			"	                   ^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 1)\n" +
			"	@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
			"	                                ^^^^^^^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 1)\n" +
			"	@Marker package p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
			"	                                                   ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested import names.
	public void test039b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
				"public class X {\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
			"	       ^\n" +
			"The import p cannot be resolved\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
			"	          ^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 1)\n" +
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
			"	                       ^^^^^^^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 1)\n" +
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z. z2;\n" +
			"	                                          ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested import names.
	public void test383596b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
				"public class X {\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
			"	       ^\n" +
			"The import p cannot be resolved\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
			"	          ^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 1)\n" +
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
			"	                       ^^^^^^^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 1)\n" +
			"	import p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
			"	                                          ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested static import names.
	public void test041() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;\n" +
						"public class X {\n" +
						"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;\n" +
				"	              ^\n" +
				"The import p cannot be resolved\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 1)\n" +
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;\n" +
				"	                 ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 1)\n" +
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;\n" +
				"	                              ^^^^^^^^^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 1)\n" +
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.z2;\n" +
				"	                                                 ^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on nested static import names.
	public void test042() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
						"public class X {\n" +
						"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
				"	              ^\n" +
				"The import p cannot be resolved\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 1)\n" +
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
				"	                 ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 1)\n" +
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
				"	                              ^^^^^^^^^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 1)\n" +
				"	import static p. @Marker q.x. @Marker @Marker y. @Marker @Marker @Marker z.*;\n" +
				"	                                                 ^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit this.
	// Much water has flown under the bridge. The grammar itself does not allow annotations in qualified name in explicit this.
	// We now use the production UnannotatableName instead of plain Name.
	public void test043() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   class Y {\n" +
			    "       class Z {\n" +
				"           Z(X. @Marker Y  Y.this) {\n" +
				"           }\n" +
				"       }\n" +
				"    }\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Z(X. @Marker Y  Y.this) {\n" +
			"	      ^^^^^^\n" +
			"Marker cannot be resolved to a type\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call -- super form
	public void test044() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static X x;\n" +
						"	public class InnerBar {\n" +
						"	}\n" +
						"	public class SubInnerBar extends InnerBar {\n" +
						"		SubInnerBar() {\n" +
						"			X.@Marker x. @Marker @Marker @Marker x.super();\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	X.@Marker x. @Marker @Marker @Marker x.super();\n" +
				"	  ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	X.@Marker x. @Marker @Marker @Marker x.super();\n" +
				"	             ^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"3. WARNING in X.java (at line 7)\n" +
				"	X.@Marker x. @Marker @Marker @Marker x.super();\n" +
				"	                                     ^\n" +
				"The static field X.x should be accessed in a static way\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call, super form with explicit type arguments
	public void test045() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	static X x;\n" +
						"	public class InnerBar {\n" +
						"	}\n" +
						"	public class SubInnerBar extends InnerBar {\n" +
						"		SubInnerBar() {\n" +
						"			X.@Marker x. @Marker @Marker @Marker x.<String>super();\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	X.@Marker x. @Marker @Marker @Marker x.<String>super();\n" +
				"	  ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	X.@Marker x. @Marker @Marker @Marker x.<String>super();\n" +
				"	             ^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"3. WARNING in X.java (at line 7)\n" +
				"	X.@Marker x. @Marker @Marker @Marker x.<String>super();\n" +
				"	                                     ^\n" +
				"The static field X.x should be accessed in a static way\n" +
				"----------\n" +
				"4. WARNING in X.java (at line 7)\n" +
				"	X.@Marker x. @Marker @Marker @Marker x.<String>super();\n" +
				"	                                        ^^^^^^\n" +
				"Unused type arguments for the non generic constructor X.InnerBar() of type X.InnerBar; it should not be parameterized with arguments <String>\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call - this form
	public void test046() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	Bar bar;\n" +
						"	class Bar {\n" +
						"		//static Bar x;\n" +
						"		public class InnerBar {\n" +
						"			InnerBar(Bar x) {\n" +
						"			}\n" +
						"		}\n" +
						"		public class SubInnerBar extends InnerBar {\n" +
						"			SubInnerBar() {\n" +
						"				X. @Marker bar.this();\n" +
						"			}\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	X. @Marker bar.this();\n" +
				"	^^^^^^^^^^^^^^\n" +
				"Illegal enclosing instance specification for type X.Bar.SubInnerBar\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 11)\n" +
				"	X. @Marker bar.this();\n" +
				"	^^^^^^^^^^^^^^\n" +
				"Cannot make a static reference to the non-static field X.bar\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 11)\n" +
				"	X. @Marker bar.this();\n" +
				"	   ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in explicit constructor call, this form with explicit type arguments
	public void test047() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	Bar bar;\n" +
						"	class Bar {\n" +
						"		//static Bar x;\n" +
						"		public class InnerBar {\n" +
						"			InnerBar(Bar x) {\n" +
						"			}\n" +
						"		}\n" +
						"		public class SubInnerBar extends InnerBar {\n" +
						"			SubInnerBar() {\n" +
						"				X.@Marker bar.<String>this();\n" +
						"			}\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	X.@Marker bar.<String>this();\n" +
				"	^^^^^^^^^^^^^\n" +
				"Illegal enclosing instance specification for type X.Bar.SubInnerBar\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 11)\n" +
				"	X.@Marker bar.<String>this();\n" +
				"	^^^^^^^^^^^^^\n" +
				"Cannot make a static reference to the non-static field X.bar\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 11)\n" +
				"	X.@Marker bar.<String>this();\n" +
				"	  ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"4. WARNING in X.java (at line 11)\n" +
				"	X.@Marker bar.<String>this();\n" +
				"	               ^^^^^^\n" +
				"Unused type arguments for the non generic constructor X.Bar.SubInnerBar() of type X.Bar.SubInnerBar; it should not be parameterized with arguments <String>\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations on Qualified name in PrimaryNoNewArray
	public void test048() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	X bar;\n" +
						"	private void foo(X x) {\n" +
						"		System.out.println((x. @Marker bar));\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	System.out.println((x. @Marker bar));\n" +
				"	                       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified this.
	public void test049() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	class Y {\n" +
						"		class Z {\n" +
						"			void foo() {\n" +
						"				Object o = X.@Marker Y.this; \n" +
						"			}\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	Object o = X.@Marker Y.this; \n" +
				"	             ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified super.
	public void test050() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public class Y  {\n" +
						"		public void foo() {\n" +
						"			X. @Marker Y.super.hashCode();\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	X. @Marker Y.super.hashCode();\n" +
				"	   ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in Name.class
	public void test051() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public class Y  {\n" +
						"		public void foo() {\n" +
						"			Class<?> c = X. @Marker @Illegal Y.class;\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	Class<?> c = X. @Marker @Illegal Y.class;\n" +
				"	                ^^^^^^^^^^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in Name [].class.
	public void test052() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"	public class Y  {\n" +
						"		public void foo() {\n" +
						"			Class<?> c = X. @Marker @Another Y @YetMore [].class;\n" +
						"		}\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	Class<?> c = X. @Marker @Another Y @YetMore [].class;\n" +
				"	                ^^^^^^^^^^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	Class<?> c = X. @Marker @Another Y @YetMore [].class;\n" +
				"	                                   ^^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in binary expressions with qualified names.
	public void test053() throws Exception {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"public class X {\n" +
						"    static int x;\n" +
						"    static boolean fb;\n" +
						"	 public void foo(boolean b) {\n" +
						"		x = (X.@Marker x * 10);\n" +
						"		x = (X.@Marker x / 10);\n" +
						"		x = (X.@Marker x % 10);\n" +
						"		x = (X.@Marker x + 10);\n" +
						"		x = (X.@Marker x - 10);\n" +
						"		x = (X.@Marker x << 10);\n" +
						"		x = (X.@Marker x >> 10);\n" +
						"		x = (X.@Marker x >>> 10);\n" +
						"		b = (X.@Marker x < 10);\n" +
						"		b = (X.@Marker x > 10);\n" +
						"		b = (X.@Marker x <= 10);\n" +
						"		b = (X.@Marker x >= 10);\n" +
						"		b = (X.@Marker x instanceof Object);\n" +
						"		b = (X.@Marker x == 10);\n" +
						"		b = (X.@Marker x != 10);\n" +
						"		x = (X.@Marker x & 10);\n" +
						"		x = (X.@Marker x ^ 10);\n" +
						"		x = (X.@Marker x | 10);\n" +
						"		fb = (X.@Marker fb && true);\n" +
						"		fb = (X.@Marker fb || true);\n" +
						"		x = (X.@Marker fb ? 10 : 10);\n" +
						"	 }\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	x = (X.@Marker x * 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	x = (X.@Marker x / 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	x = (X.@Marker x % 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	x = (X.@Marker x + 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 9)\n" +
				"	x = (X.@Marker x - 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 10)\n" +
				"	x = (X.@Marker x << 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 11)\n" +
				"	x = (X.@Marker x >> 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 12)\n" +
				"	x = (X.@Marker x >>> 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 13)\n" +
				"	b = (X.@Marker x < 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"10. ERROR in X.java (at line 14)\n" +
				"	b = (X.@Marker x > 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"11. ERROR in X.java (at line 15)\n" +
				"	b = (X.@Marker x <= 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"12. ERROR in X.java (at line 16)\n" +
				"	b = (X.@Marker x >= 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"13. ERROR in X.java (at line 17)\n" +
				"	b = (X.@Marker x instanceof Object);\n" +
				"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Incompatible conditional operand types int and Object\n" +
				"----------\n" +
				"14. ERROR in X.java (at line 17)\n" +
				"	b = (X.@Marker x instanceof Object);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"15. ERROR in X.java (at line 18)\n" +
				"	b = (X.@Marker x == 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"16. ERROR in X.java (at line 19)\n" +
				"	b = (X.@Marker x != 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"17. ERROR in X.java (at line 20)\n" +
				"	x = (X.@Marker x & 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"18. ERROR in X.java (at line 21)\n" +
				"	x = (X.@Marker x ^ 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"19. ERROR in X.java (at line 22)\n" +
				"	x = (X.@Marker x | 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"20. ERROR in X.java (at line 23)\n" +
				"	fb = (X.@Marker fb && true);\n" +
				"	        ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"21. ERROR in X.java (at line 24)\n" +
				"	fb = (X.@Marker fb || true);\n" +
				"	        ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"22. ERROR in X.java (at line 25)\n" +
				"	x = (X.@Marker fb ? 10 : 10);\n" +
				"	       ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in annotations with qualified names.
	   This test is disabled. Now the grammar itself forbids annotations in the said place by using the production
	   AnnotationName ::= '@' UnannotatableName. We don't want to add tests that will be fragile and unstable due to
	   syntax. If a construct is provably not parsed at the grammar level, that ought to be good enough.
	*/
	public void test054() throws Exception {
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified names used as annotation values.
	public void test055() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"@interface Annot {\n" +
					"	String bar();\n" +
					"}\n" +
					"@Annot(bar = X. @Marker s)\n" +
					"public class X {\n" +
					"	final static String s = \"\";\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	@Annot(bar = X. @Marker s)\n" +
				"	                ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified names that are postfix expressions.
	public void test056() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static int x;\n" +
					"    int foo() {\n" +
					"        return X.@Marker x;\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	return X.@Marker x;\n" +
				"	         ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified names used in array access.
	public void test057() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static int x[];\n" +
					"    int foo() {\n" +
					"        return X.@Marker x[0];\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	return X.@Marker x[0];\n" +
				"	         ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name with type arguments used in method invocation.
	public void test058() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static X x;\n" +
					"    int foo() {\n" +
					"        return X.@Marker x.<String> foo();\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	return X.@Marker x.<String> foo();\n" +
				"	         ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 4)\n" +
				"	return X.@Marker x.<String> foo();\n" +
				"	                    ^^^^^^\n" +
				"Unused type arguments for the non generic method foo() of type X; it should not be parameterized with arguments <String>\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name used in method invocation.
	public void test059() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static X x;\n" +
					"    int foo() {\n" +
					"        return X.@Marker x. @Blah foo();\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	return X.@Marker x. @Blah foo();\n" +
				"	         ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	return X.@Marker x. @Blah foo();\n" +
				"	                    ^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name used in class instance creation
	public void test060() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static Y y;\n" +
					"    class Y {\n" +
					"        class Z {\n" +
					"            void foo() {\n" +
					"                Z z = X. @Marker y.new Z();\n" +
					"            }\n" +
					"        }\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	Z z = X. @Marker y.new Z();\n" +
				"	         ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383596 -- reject annotations in qualified name used in class instance creation
	public void test061() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static X x;\n" +
					"    X getX() {\n" +
					"        return (X.@Marker x);\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	return (X.@Marker x);\n" +
				"	          ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	public void test062() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public <T> @Marker Object foo() {\n" +
					"		return null;" +
					"	}\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	public <T> @Marker Object foo() {\n" +
				"	           ^^^^^^^\n" +
				"Annotation types that do not specify explicit target element types cannot be applied here\n" +
				"----------\n");
	}
	public void test063() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Object o = @Marker int.class;\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	Object o = @Marker int.class;\n" +
				"	           ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	public void test064() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"@interface X {\n" +
					"	<T> @Marker String foo();\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	<T> @Marker String foo();\n" +
				"	    ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	<T> @Marker String foo();\n" +
				"	                   ^^^^^\n" +
				"Annotation attributes cannot be generic\n" +
				"----------\n");
	}
	public void test065() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Object o = new <String> @Marker X();\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				},
				"----------\n" +
				"1. WARNING in X.java (at line 2)\n" +
				"	Object o = new <String> @Marker X();\n" +
				"	                ^^^^^^\n" +
				"Unused type arguments for the non generic constructor X() of type X; it should not be parameterized with arguments <String>\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	Object o = new <String> @Marker X();\n" +
				"	                        ^^^^^^^\n" +
				"Annotation types that do not specify explicit target element types cannot be applied here\n" +
				"----------\n");
	}
	public void test066() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Object o = new X().new <String> @Marker X();\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	Object o = new X().new <String> @Marker X();\n" +
				"	                                ^^^^^^^\n" +
				"Annotation types that do not specify explicit target element types cannot be applied here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	Object o = new X().new <String> @Marker X();\n" +
				"	                                ^^^^^^^^^\n" +
				"X.X cannot be resolved to a type\n" +
				"----------\n");
	}
	public void test067() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Object o = x.new <String> @Marker X() {};\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	Object o = x.new <String> @Marker X() {};\n" +
				"	           ^\n" +
				"x cannot be resolved to a variable\n" +
				"----------\n");
	}
	public void test068() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	Object o = new <String> @Marker X() {};\n" +
					"}\n" +
					"@interface Marker {\n" +
					"}\n"
				},
				"----------\n" +
				"1. WARNING in X.java (at line 2)\n" +
				"	Object o = new <String> @Marker X() {};\n" +
				"	                ^^^^^^\n" +
				"Unused type arguments for the non generic constructor X() of type X; it should not be parameterized with arguments <String>\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	Object o = new <String> @Marker X() {};\n" +
				"	                        ^^^^^^^\n" +
				"Annotation types that do not specify explicit target element types cannot be applied here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385293
	public void test069() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"class X<final T> {\n" +
					"	Object o = (Object) (public X<final String>) null;\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	class X<final T> {\n" +
				"	        ^^^^^\n" +
				"Syntax error on token \"final\", delete this token\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	Object o = (Object) (public X<final String>) null;\n" +
				"	                     ^^^^^^\n" +
				"Syntax error on token \"public\", delete this token\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 2)\n" +
				"	Object o = (Object) (public X<final String>) null;\n" +
				"	                              ^^^^^\n" +
				"Syntax error on token \"final\", delete this token\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388085
	public void test0388085() {
		this.runNegativeTest(
				new String[] {"X.java",
						"class X {\n" +
						"	public void main() {\n" +
						"		final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;" +
						"		one = null;\n" +
						"	}\n" +
						"}\n" +
						"class One<R> {}\n" +
						"class Two<S> {}\n" +
						"class Three<T> {}\n" +
						"class Four<U, V> {}\n"},
							"----------\n" +
							"1. ERROR in X.java (at line 3)\n" +
							"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" +
							"	           ^^^^^^\n" +
							"Marker cannot be resolved to a type\n" +
							"----------\n" +
							"2. ERROR in X.java (at line 3)\n" +
							"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" +
							"	                                 ^^^^^^\n" +
							"Marker cannot be resolved to a type\n" +
							"----------\n" +
							"3. ERROR in X.java (at line 3)\n" +
							"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" +
							"	                                                                      ^^^^^^\n" +
							"Marker cannot be resolved to a type\n" +
							"----------\n" +
							"4. ERROR in X.java (at line 3)\n" +
							"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" +
							"	                                                                                              ^^^^^^\n" +
							"Marker cannot be resolved to a type\n" +
							"----------\n");
	}
	public void test0388085a() {
		this.runNegativeTest(
				new String[] {"X.java",
						"import java.lang.annotation.Target;\n" +
						"import static java.lang.annotation.ElementType.*;\n" +
						"class X {\n" +
						"	public void main() {\n" +
						"		final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;" +
						"		one = null;\n" +
						"	}\n" +
						"}\n" +
						"class One<R> {}\n" +
						"class Two<S> {}\n" +
						"class Three<T> {}\n" +
						"class Four<U, V> {}\n" +
						"@interface Marker {}"},
						"----------\n" +
						"1. ERROR in X.java (at line 5)\n" +
						"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" +
						"	          ^^^^^^^\n" +
						"Annotation types that do not specify explicit target element types cannot be applied here\n" +
						"----------\n" +
						"2. ERROR in X.java (at line 5)\n" +
						"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" +
						"	                                ^^^^^^^\n" +
						"Annotation types that do not specify explicit target element types cannot be applied here\n" +
						"----------\n" +
						"3. ERROR in X.java (at line 5)\n" +
						"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" +
						"	                                                                     ^^^^^^^\n" +
						"Annotation types that do not specify explicit target element types cannot be applied here\n" +
						"----------\n" +
						"4. ERROR in X.java (at line 5)\n" +
						"	final One<@Marker ? extends Two<@Marker ? super Three<? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;		one = null;\n" +
						"	                                                                                             ^^^^^^^\n" +
						"Annotation types that do not specify explicit target element types cannot be applied here\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=390882
	public void test0390882() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.Target;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"public class X {    \n " +
					"	Object o1 = (@Marker java.lang.Integer) null;   // 1. Right.\n" +
					"	Object o2 = (java. @Marker lang.Integer) null;  // 2. Wrong.\n" +
					"	Object o3 = (java.lang. @Marker Integer) null;  // 3. Legal.\n" +
					"	public void foo(java. @Marker lang.Integer arg) {}\n" +
					"	public void bar(java.lang. @Marker Integer arg) {}\n" +
					"	public void foobar(@Marker java.lang.Integer arg) {}\n" +
					"}\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Marker {}\n",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n" +
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	Object o1 = (@Marker java.lang.Integer) null;   // 1. Right.\n" +
				"	             ^^^^^^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	Object o2 = (java. @Marker lang.Integer) null;  // 2. Wrong.\n" +
				"	                   ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	public void foo(java. @Marker lang.Integer arg) {}\n" +
				"	                      ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 9)\n" +
				"	public void foobar(@Marker java.lang.Integer arg) {}\n" +
				"	                   ^^^^^^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n");
	}
	public void test0390882a() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.Target;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"public class X {    \n " +
					"	Object o1 = (java. @Marker @Annot lang.Integer) null;  // 1. Wrong.\n" +
					"	Object o2 = (java.lang. @Marker @Annot Integer) null;  // 2. Legal\n" +
					"	Object o3 = (java.@lang lang) null;  // 3. Wrong.\n" +
					"}\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Marker {}\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Annot {}",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n" +
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	Object o1 = (java. @Marker @Annot lang.Integer) null;  // 1. Wrong.\n" +
				"	                   ^^^^^^^^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	Object o3 = (java.@lang lang) null;  // 3. Wrong.\n" +
				"	             ^^^^^^^^^^^^^^^\n" +
				"java.lang cannot be resolved to a type\n" +
				"----------\n");
	}
	public void test0390882b() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.Target;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"public class X {    \n " +
					"	Object o1 = (@Marker @Annot java.util.List<String>) null; 	// 1. Wrong.\n" +
					"	Object o2 = (java. @Marker @Annot lang.Integer[]) null;		// 2. Wrong.\n" +
					"	Object o3 = (@Marker @Annot java.util.List<String>[]) null; // 3. Wrong.\n" +
					"	Object o4 = (java.util.List<String> @Marker @Annot []) null; // 4. Right.\n" +
					"	Object o5 = (java.lang.Integer @Marker @Annot []) null;	// 5. Right.\n" +
					"}\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Marker {}\n" +
					"@Target(TYPE_USE)\n" +
					"@interface Annot {}",

					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n" +
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	Object o1 = (@Marker @Annot java.util.List<String>) null; 	// 1. Wrong.\n" +
				"	             ^^^^^^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	Object o1 = (@Marker @Annot java.util.List<String>) null; 	// 1. Wrong.\n" +
				"	                     ^^^^^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	Object o2 = (java. @Marker @Annot lang.Integer[]) null;		// 2. Wrong.\n" +
				"	                   ^^^^^^^^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	Object o3 = (@Marker @Annot java.util.List<String>[]) null; // 3. Wrong.\n" +
				"	             ^^^^^^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 6)\n" +
				"	Object o3 = (@Marker @Annot java.util.List<String>[]) null; // 3. Wrong.\n" +
				"	                     ^^^^^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385137
	public void test0385137() {
		this.runNegativeTest(
				new String[]{ "A.java",
				"package p;" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"public class A<T> { \n" +
				"	static class B<T> {" +
				"		static class C<K, V> {" +
				"		}	" +
				"	}\n" +
				"   public void foo() {\n" +
				"		Object o = (@Marker @Annot A.@Marker B.@Marker C) null;\n" +
				"		Object o2 = (@Marker p.@Marker A.@Marker B.@Marker C) null;\n" +
				"   }\n" +
				"}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Annot {}\n",

				"java/lang/annotation/ElementType.java",
				"package java.lang.annotation;\n" +
				"public enum ElementType {\n" +
				"    TYPE,\n" +
				"    FIELD,\n" +
				"    METHOD,\n" +
				"    PARAMETER,\n" +
				"    CONSTRUCTOR,\n" +
				"    LOCAL_VARIABLE,\n" +
				"    ANNOTATION_TYPE,\n" +
				"    PACKAGE,\n" +
				"    TYPE_PARAMETER,\n" +
				"    TYPE_USE\n" +
				"}\n"},
					"----------\n" +
					"1. ERROR in A.java (at line 6)\n" +
					"	Object o = (@Marker @Annot A.@Marker B.@Marker C) null;\n" +
					"	            ^^^^^^^^^^^^^^\n" +
					"Type annotations are not allowed on type names used to access static members\n" +
					"----------\n" +
					"2. WARNING in A.java (at line 6)\n" +
					"	Object o = (@Marker @Annot A.@Marker B.@Marker C) null;\n" +
					"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"A.B.C is a raw type. References to generic type A.B.C<K,V> should be parameterized\n" +
					"----------\n" +
					"3. ERROR in A.java (at line 6)\n" +
					"	Object o = (@Marker @Annot A.@Marker B.@Marker C) null;\n" +
					"	                             ^^^^^^^\n" +
					"Type annotations are not allowed on type names used to access static members\n" +
					"----------\n" +
					"4. ERROR in A.java (at line 7)\n" +
					"	Object o2 = (@Marker p.@Marker A.@Marker B.@Marker C) null;\n" +
					"	             ^^^^^^^\n" +
					"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
					"----------\n" +
					"5. WARNING in A.java (at line 7)\n" +
					"	Object o2 = (@Marker p.@Marker A.@Marker B.@Marker C) null;\n" +
					"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"A.B.C is a raw type. References to generic type A.B.C<K,V> should be parameterized\n" +
					"----------\n" +
					"6. ERROR in A.java (at line 7)\n" +
					"	Object o2 = (@Marker p.@Marker A.@Marker B.@Marker C) null;\n" +
					"	                       ^^^^^^^\n" +
					"Type annotations are not allowed on type names used to access static members\n" +
					"----------\n" +
					"7. ERROR in A.java (at line 7)\n" +
					"	Object o2 = (@Marker p.@Marker A.@Marker B.@Marker C) null;\n" +
					"	                                 ^^^^^^^\n" +
					"Type annotations are not allowed on type names used to access static members\n" +
					"----------\n");
	}
	public void test0385137a() {
		this.runNegativeTest(
				new String[]{"A.java",
				"package p;" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"public class A { \n" +
				"	static class B<T> {" +
				"		static class C<K, V> {" +
				"		}	" +
				"	}\n" +
				"   public void foo() {\n" +
				"		Object o1 = (@Marker p.@Marker A.@Marker B.@Marker C[]) null;\n" +
				"		Object o2 = (@Marker @Annot A.@Annot B.C<Integer, String>) null;\n" +
				"		Object o5 = (@Marker @Annot A.B<String>[]) null;\n" +
				"   }\n" +
				"}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@Target(TYPE_USE)\n" +
				"@interface Annot {}\n",

				"java/lang/annotation/ElementType.java",
				"package java.lang.annotation;\n" +
				"public enum ElementType {\n" +
				"    TYPE,\n" +
				"    FIELD,\n" +
				"    METHOD,\n" +
				"    PARAMETER,\n" +
				"    CONSTRUCTOR,\n" +
				"    LOCAL_VARIABLE,\n" +
				"    ANNOTATION_TYPE,\n" +
				"    PACKAGE,\n" +
				"    TYPE_PARAMETER,\n" +
				"    TYPE_USE\n" +
				"}\n",
				},
				"----------\n" +
					"1. ERROR in A.java (at line 6)\n" +
					"	Object o1 = (@Marker p.@Marker A.@Marker B.@Marker C[]) null;\n" +
					"	             ^^^^^^^\n" +
					"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
					"----------\n" +
					"2. ERROR in A.java (at line 6)\n" +
					"	Object o1 = (@Marker p.@Marker A.@Marker B.@Marker C[]) null;\n" +
					"	                       ^^^^^^^\n" +
					"Type annotations are not allowed on type names used to access static members\n" +
					"----------\n" +
					"3. ERROR in A.java (at line 6)\n" +
					"	Object o1 = (@Marker p.@Marker A.@Marker B.@Marker C[]) null;\n" +
					"	                                 ^^^^^^^\n" +
					"Type annotations are not allowed on type names used to access static members\n" +
					"----------\n" +
					"4. ERROR in A.java (at line 7)\n" +
					"	Object o2 = (@Marker @Annot A.@Annot B.C<Integer, String>) null;\n" +
					"	             ^^^^^^^^^^^^^^\n" +
					"Type annotations are not allowed on type names used to access static members\n" +
					"----------\n" +
					"5. ERROR in A.java (at line 7)\n" +
					"	Object o2 = (@Marker @Annot A.@Annot B.C<Integer, String>) null;\n" +
					"	                              ^^^^^^\n" +
					"Type annotations are not allowed on type names used to access static members\n" +
					"----------\n" +
					"6. ERROR in A.java (at line 8)\n" +
					"	Object o5 = (@Marker @Annot A.B<String>[]) null;\n" +
					"	             ^^^^^^^^^^^^^^\n" +
					"Type annotations are not allowed on type names used to access static members\n" +
					"----------\n");
	}
	public void testBug391196() {
		this.runNegativeTest(
				new String[]{
					"p/Bug391196.java",
					"package p;\n" +
					"public class Bug391196 {\n" +
					"	@Marker\n" +
					"	public class X<@Marker @Marker2 T> {\n" +
					"		@Marker @Marker2 X(@Marker int i) {}\n" +
					"		@Unresolved X() {}\n" +
					"	}\n" +
					"	@Marker\n" +
					"	enum Color {RED, BLUE}\n" +
					"	@Marker\n" +
					"	interface Inter {}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface Marker {}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface Marker2 {}\n" +
					"}\n",
					"java/lang/annotation/ElementType.java",
					"package java.lang.annotation;\n" +
					"public enum ElementType {\n" +
					"    TYPE,\n" +
					"    FIELD,\n" +
					"    METHOD,\n" +
					"    PARAMETER,\n" +
					"    CONSTRUCTOR,\n" +
					"    LOCAL_VARIABLE,\n" +
					"    ANNOTATION_TYPE,\n" +
					"    PACKAGE,\n" +
					"    TYPE_PARAMETER,\n" +
					"    TYPE_USE\n" +
					"}\n",
				},
				"----------\n" +
				"1. ERROR in p\\Bug391196.java (at line 6)\n" +
				"	@Unresolved X() {}\n" +
				"	 ^^^^^^^^^^\n" +
				"Unresolved cannot be resolved to a type\n" +
				"----------\n");
	}
	public void testBug391315() {
		this.runNegativeTest(
				new String[]{
				"X.java",
				"class X<T> {\n" +
				"	X<@Marker ?> l;\n" +
				"	X<@Marker2 ?> l2;\n" +
				"	X<@Marker3 ?> l3;\n" +
				"	class Y {\n" +
				"		void Y1(Y this) {}\n" +
				"	}\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n" +
				"@interface Marker3 {}\n",
				"java/lang/annotation/ElementType.java",
				"package java.lang.annotation;\n" +
				"public enum ElementType {\n" +
				"    TYPE,\n" +
				"    FIELD,\n" +
				"    METHOD,\n" +
				"    PARAMETER,\n" +
				"    CONSTRUCTOR,\n" +
				"    LOCAL_VARIABLE,\n" +
				"    ANNOTATION_TYPE,\n" +
				"    PACKAGE,\n" +
				"    TYPE_PARAMETER,\n" +
				"    TYPE_USE\n" +
				"}\n"},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	X<@Marker ?> l;\n" +
				"	  ^^^^^^^\n" +
				"The annotation @Marker is disallowed for this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	X<@Marker3 ?> l3;\n" +
				"	  ^^^^^^^^\n" +
				"Annotation types that do not specify explicit target element types cannot be applied here\n" +
				"----------\n");
	}
	public void testBug391315a() {
		this.runNegativeTest(
				new String[]{
				"X.java",
				"public class X<@Marker T> {\n" +
				"	@Marker T t;\n" +
				"	T t2 = (@Marker T) null;\n" +
				"}\n" +
				"class X2<@Marker2 T> {\n" +
				"	@Marker2 T t;\n" +
				"	T t2 = (@Marker2 T) null;\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
				"@interface Marker2 {}",
				"java/lang/annotation/ElementType.java",
				"package java.lang.annotation;\n" +
				"public enum ElementType {\n" +
				"    TYPE,\n" +
				"    FIELD,\n" +
				"    METHOD,\n" +
				"    PARAMETER,\n" +
				"    CONSTRUCTOR,\n" +
				"    LOCAL_VARIABLE,\n" +
				"    ANNOTATION_TYPE,\n" +
				"    PACKAGE,\n" +
				"    TYPE_PARAMETER,\n" +
				"    TYPE_USE\n" +
				"}\n"},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	@Marker2 T t;\n" +
				"	^^^^^^^^\n" +
				"The annotation @Marker2 is disallowed for this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	T t2 = (@Marker2 T) null;\n" +
				"	        ^^^^^^^^\n" +
				"The annotation @Marker2 is disallowed for this location\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391500
	public void testBug391500() {
		this.runNegativeTest(
				new String[]{
				"X.java",
				"public class X {\n" +
				"	class Y {\n" +
				"		class Z {\n" +
				"		}\n" +
				"		Z z1 = new @Marker X().new @Marker Y().new @Marker Z();\n" +
				"		Z z3 = new @Marker Z(){};\n" +
				"	};\n" +
				"}\n"},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	Z z1 = new @Marker X().new @Marker Y().new @Marker Z();\n" +
				"	            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	Z z1 = new @Marker X().new @Marker Y().new @Marker Z();\n" +
				"	                            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	Z z1 = new @Marker X().new @Marker Y().new @Marker Z();\n" +
				"	                                            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	Z z3 = new @Marker Z(){};\n" +
				"	            ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	public void testBug391464() {
		this.runNegativeTest(
				new String[]{
				"X.java",
				"public class X<T> {\n" +
				"	public void foo() {\n" +
				"		Object o = (X @Marker []) null;\n" +
				"		o = (java.lang.String @Marker []) null;\n" +
				"		o = (X<String> @Marker []) null;\n" +
				"		o = (java.util.List<String> @Marker []) null;\n" +
				"		if (o == null) return;\n" +
				"	}" +
				"}\n"},
				"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	Object o = (X @Marker []) null;\n" +
				"	               ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	o = (java.lang.String @Marker []) null;\n" +
				"	                       ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	o = (X<String> @Marker []) null;\n" +
				"	                ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	o = (java.util.List<String> @Marker []) null;\n" +
				"	                             ^^^^^^\n" +
				"Marker cannot be resolved to a type\n" +
				"----------\n");
	}
	public void testBug391464_2() {
		this.runNegativeTest(
				new String[]{
				"X.java",
				"public class X  {\n" +
				"	class Y {\n" +
				"		class Z {}\n" +
				"	}\n" +
				"	@M X.@M Y.@Unreported Z z = null;\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface M {\n" +
				"}\n",

				"java/lang/annotation/ElementType.java",
				"package java.lang.annotation;\n" +
				"public enum ElementType {\n" +
				"    TYPE,\n" +
				"    FIELD,\n" +
				"    METHOD,\n" +
				"    PARAMETER,\n" +
				"    CONSTRUCTOR,\n" +
				"    LOCAL_VARIABLE,\n" +
				"    ANNOTATION_TYPE,\n" +
				"    PACKAGE,\n" +
				"    TYPE_PARAMETER,\n" +
				"    TYPE_USE\n" +
				"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	@M X.@M Y.@Unreported Z z = null;\n" +
				"	           ^^^^^^^^^^\n" +
				"Unreported cannot be resolved to a type\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391108
	public void testBug391108() {
		this.runNegativeTest(
				new String[]{
						"X.java",
						"public class X {\n" +
						"	@Marker @Marker2 @Marker3 public void foo() {}\n" +
						"	@Marker @Marker2 @Marker3 void foo2() {}\n" +
						"}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface Marker {}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.METHOD)\n" +
						"@interface Marker2 {}\n" +
						"@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.METHOD})\n" +
						"@interface Marker3 {}",
						"java/lang/annotation/ElementType.java",
						"package java.lang.annotation;\n" +
						"public enum ElementType {\n" +
						"    TYPE,\n" +
						"    FIELD,\n" +
						"    METHOD,\n" +
						"    PARAMETER,\n" +
						"    CONSTRUCTOR,\n" +
						"    LOCAL_VARIABLE,\n" +
						"    ANNOTATION_TYPE,\n" +
						"    PACKAGE,\n" +
						"    TYPE_PARAMETER,\n" +
						"    TYPE_USE\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	@Marker @Marker2 @Marker3 public void foo() {}\n" +
				"	^^^^^^^\n" +
				"Type annotation is illegal for a method that returns void\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 3)\n" +
				"	@Marker @Marker2 @Marker3 void foo2() {}\n" +
				"	^^^^^^^\n" +
				"Type annotation is illegal for a method that returns void\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=392119
	public void test392119() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"@Marker78 @Marker8 @Marker7\n" +
				"public class X {\n" +
				"    Zork z;\n" +
				"}\n" +
				"@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.TYPE})\n" +
				"@interface Marker78 {\n" +
				"}\n" +
				"@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE})\n" +
				"@interface Marker7 {\n" +
				"}\n" +
				"@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE})\n" +
				"@interface Marker8 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
		String expectedOutput =
				"  RuntimeInvisibleAnnotations: \n" +
				"    #24 @Marker78(\n" +
				"    )\n" +
				"    #25 @Marker8(\n" +
				"    )\n" +
				"    #26 @Marker7(\n" +
				"    )\n" +
				"  Attribute: MissingTypes Length: 4\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=392119, variant with explicit class file retention.
	public void test392119b() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"@Marker78 @Marker8 @Marker7\n" +
				"public class X {\n" +
				"    Zork z;\n" +
				"}\n" +
				"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)\n" +
				"@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.TYPE})\n" +
				"@interface Marker78 {\n" +
				"}\n" +
				"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)\n" +
				"@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE})\n" +
				"@interface Marker7 {\n" +
				"}\n" +
				"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)\n" +
				"@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE})\n" +
				"@interface Marker8 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
		String expectedOutput =
				"  RuntimeInvisibleAnnotations: \n" +
				"    #24 @Marker78(\n" +
				"    )\n" +
				"    #25 @Marker8(\n" +
				"    )\n" +
				"    #26 @Marker7(\n" +
				"    )\n" +
				"  Attribute: MissingTypes Length: 4\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=392119, variant with explicit runtime retention.
	public void test392119c() throws Exception {
		this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"@Marker78 @Marker8 @Marker7\n" +
				"public class X {\n" +
				"    Zork z;\n" +
				"}\n" +
				"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n" +
				"@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.TYPE})\n" +
				"@interface Marker78 {\n" +
				"}\n" +
				"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n" +
				"@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE})\n" +
				"@interface Marker7 {\n" +
				"}\n" +
				"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n" +
				"@java.lang.annotation.Target ({java.lang.annotation.ElementType.TYPE_USE})\n" +
				"@interface Marker8 {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
		String expectedOutput =
				"  RuntimeVisibleAnnotations: \n" +
				"    #24 @Marker78(\n" +
				"    )\n" +
				"    #25 @Marker8(\n" +
				"    )\n" +
				"    #26 @Marker7(\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=394355
	public void testBug394355() {
		this.runNegativeTest(
			new String[]{
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"public class X {\n" +
				"	public void foo(@Marker @Marker2 X this) {}\n" +
				"	class Y {\n" +
				"		Y(@Marker @Marker2 X X.this) {}\n" +
				"	}\n" +
				"}\n" +
				"@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@Target ({METHOD, PARAMETER, TYPE, PACKAGE, FIELD, CONSTRUCTOR, LOCAL_VARIABLE, TYPE_PARAMETER})\n" +
				"@interface Marker2 {}",
				"java/lang/annotation/ElementType.java",
				"package java.lang.annotation;\n" +
				"public enum ElementType {\n" +
				"    TYPE,\n" +
				"    FIELD,\n" +
				"    METHOD,\n" +
				"    PARAMETER,\n" +
				"    CONSTRUCTOR,\n" +
				"    LOCAL_VARIABLE,\n" +
				"    ANNOTATION_TYPE,\n" +
				"    PACKAGE,\n" +
				"    TYPE_PARAMETER,\n" +
				"    TYPE_USE\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	public void foo(@Marker @Marker2 X this) {}\n" +
			"	                        ^^^^^^^^\n" +
			"The annotation @Marker2 is disallowed for this location\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	Y(@Marker @Marker2 X X.this) {}\n" +
			"	          ^^^^^^^^\n" +
			"The annotation @Marker2 is disallowed for this location\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399453
	public void testBug399453() {
		this.runNegativeTest(
				new String[]{
					"X.java",
					"import java.lang.annotation.Target;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"public class X {\n" +
					"	public void foo() {\n" +
					"		int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] @Marker @Marker2 [];\n" +
					"		int @Marker [][][] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker X.bar2(2)] @Marker @Marker2 [];\n" +
					"	}\n" +
					"	public int bar() {\n" +
					"		return 2;\n" +
					"	}\n" +
					"	public static int bar2(int k) {\n" +
					"		return k;\n" +
					"	}\n" +
					"}\n" +
					"@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface Marker {}\n" +
					"@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface Marker2 {}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] @Marker @Marker2 [];\n" +
				"	                                                                               ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	int @Marker [][][] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker X.bar2(2)] @Marker @Marker2 [];\n" +
				"	                                                                               ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399453
	public void testBug391894() {
		this.runNegativeTest(
				new String[]{
					"X.java",
					"import java.lang.annotation.Target;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"public class X {\n" +
					"	public void foo() {\n" +
					"		int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;\n" +
					"		int @Marker [] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;\n" +
					"	}\n" +
					"	public int bar() {\n" +
					"		return 2;\n" +
					"	}\n" +
					"}\n" +
					"@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface Marker {}\n" +
					"@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface Marker2 {}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;\n" +
				"	                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type mismatch: cannot convert from int[][] to int[][][]\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;\n" +
				"	                                                                               ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	int @Marker [] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;\n" +
				"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Type mismatch: cannot convert from int[][] to int[]\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	int @Marker [] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [@Marker bar()] ;\n" +
				"	                                                                           ^^^^^^^\n" +
				"Syntax error, type annotations are illegal here\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402618, [1.8][compiler] Compiler fails to resolve type annotations on method/constructor references
	public void test402618() {
		this.runNegativeTest(
				new String[]{
					"X.java",
					"import java.util.List;\n" +
					"interface I {\n" +
					"	void foo(List<String> l);\n" +
					"}\n" +
					"\n" +
					"public class X {\n" +
					"	public void main(String[] args) {\n" +
					"		I i = @Readonly List<@English String>::<@NonNegative Integer>size;\n" +
					"	}\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	I i = @Readonly List<@English String>::<@NonNegative Integer>size;\n" +
				"	       ^^^^^^^^\n" +
				"Readonly cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	I i = @Readonly List<@English String>::<@NonNegative Integer>size;\n" +
				"	                      ^^^^^^^\n" +
				"English cannot be resolved to a type\n" +
				"----------\n" +
				"3. WARNING in X.java (at line 8)\n" +
				"	I i = @Readonly List<@English String>::<@NonNegative Integer>size;\n" +
				"	                                        ^^^^^^^^^^^^^^^^^^^^\n" +
				"Unused type arguments for the non generic method size() of type List<String>; it should not be parameterized with arguments <Integer>\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	I i = @Readonly List<@English String>::<@NonNegative Integer>size;\n" +
				"	                                         ^^^^^^^^^^^\n" +
				"NonNegative cannot be resolved to a type\n" +
				"----------\n");
		}
	public void testBug403132() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	class Y {\n" +
					"		class Z {\n" +
					"			public Z (@A X.@B Y Y.this, String str) {}\n" +
					"    	 	public void foo (@A X.@B Y.@C Z this, String str) {}\n" +
					"		}\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	public Z (@A X.@B Y Y.this, String str) {}\n" +
				"	           ^\n" +
				"A cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	public Z (@A X.@B Y Y.this, String str) {}\n" +
				"	                ^\n" +
				"B cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	public void foo (@A X.@B Y.@C Z this, String str) {}\n" +
				"	                  ^\n" +
				"A cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 5)\n" +
				"	public void foo (@A X.@B Y.@C Z this, String str) {}\n" +
				"	                       ^\n" +
				"B cannot be resolved to a type\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 5)\n" +
				"	public void foo (@A X.@B Y.@C Z this, String str) {}\n" +
				"	                            ^\n" +
				"C cannot be resolved to a type\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=403410
	public void testBug403410() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.Target;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface A {}\n" +
					"public class X {\n" +
					"	class Y {\n" +
					"		public Y (final @A X X.this) {}\n" +
					"		public Y (static @A X X.this, int i) {}\n" +
					"		public void foo(final @A Y this) {}\n" +
					"		public void foo(static @A Y this, int i) {}\n" +
					"}\n}"},
					"----------\n" +
					"1. ERROR in X.java (at line 7)\n" +
					"	public Y (final @A X X.this) {}\n" +
					"	          ^^^^^^^^^^^^^^^^^\n" +
					"Syntax error, modifiers are not allowed here\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 8)\n" +
					"	public Y (static @A X X.this, int i) {}\n" +
					"	          ^^^^^^^^^^^^^^^^^^\n" +
					"Syntax error, modifiers are not allowed here\n" +
					"----------\n" +
					"3. ERROR in X.java (at line 9)\n" +
					"	public void foo(final @A Y this) {}\n" +
					"	                ^^^^^^^^^^^^^^^\n" +
					"Syntax error, modifiers are not allowed here\n" +
					"----------\n" +
					"4. ERROR in X.java (at line 10)\n" +
					"	public void foo(static @A Y this, int i) {}\n" +
					"	                ^^^^^^^^^^^^^^^^\n" +
					"Syntax error, modifiers are not allowed here\n" +
					"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=403581,  [1.8][compiler] Compile error on varargs annotations.
	public void test403581() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.util.List;\n" +
					"public class X {\n" +
					"	void foo(List<String> @Marker ... ls) {}\n" +
					"}\n" +
					"@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface Marker {\n" +
					"}\n"
				},
				"----------\n" +
				"1. WARNING in X.java (at line 3)\n" +
				"	void foo(List<String> @Marker ... ls) {}\n" +
				"	                                  ^^\n" +
				"Type safety: Potential heap pollution via varargs parameter ls\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=392671, [1.8][recovery] NPE with a method with explicit this and a following incomplete parameter
	public void test392671() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"class X {\n" +
					"    public void foobar(X this, int, int k) {} // NPE!\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	class X {\n" +
				"	        ^\n" +
				"Syntax error, insert \"}\" to complete ClassBody\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	public void foobar(X this, int, int k) {} // NPE!\n" +
				"	                           ^^^\n" +
				"Syntax error, insert \"... VariableDeclaratorId\" to complete FormalParameter\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 3)\n" +
				"	}\n" +
				"	^\n" +
				"Syntax error on token \"}\", delete this token\n" +
				"----------\n");
	}
	// [1.8][compiler] Missing expected error for incorrect placement of type annotation (https://bugs.eclipse.org/bugs/show_bug.cgi?id=406587)
	public void test406587() {
		this.runNegativeTest(
				new String[] {
					"p/X.java",
					"package p;\n" +
					"import java.lang.annotation.*;\n" +
					"public class X {\n" +
					"	@B(1) @A(1) String field1;\n" +
					"	@B @A X.Y field3;\n" +
					"	@A @B p.X.Y field4;\n" +
					"	@B(1) @A(1) java.lang.@A(1) @B(1) String field2;\n" +
					"	public @B(1) @A(1) java.lang. @A(1) @B(1)  String foo(@A(1) @B(1) java.lang. @A(1) @B(1) String str1) {\n" +
					"		@A(1) @B(1)  String local1;\n" +
					"		@A(1) @B(1) java.lang.  @B(1) @A(1) String local2;\n" +
					"		@B @A X.Y local3;\n" +
					"		@B @A p.X.Y local4;\n" +
					"		@B @A p.q.X local5;\n" +
					"		return null;\n" +
					"	}\n" +
					"	class Y {}" +
					"}\n" +
					"@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})\n" +
					"@interface A {\n" +
					"	int value() default -1;\n" +
					"}\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface B {\n" +
					"	int value() default -1;\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in p\\X.java (at line 6)\n" +
				"	@A @B p.X.Y field4;\n" +
				"	   ^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"2. ERROR in p\\X.java (at line 7)\n" +
				"	@B(1) @A(1) java.lang.@A(1) @B(1) String field2;\n" +
				"	^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"3. ERROR in p\\X.java (at line 7)\n" +
				"	@B(1) @A(1) java.lang.@A(1) @B(1) String field2;\n" +
				"	                      ^^\n" +
				"The annotation @A is disallowed for this location\n" +
				"----------\n" +
				"4. ERROR in p\\X.java (at line 8)\n" +
				"	public @B(1) @A(1) java.lang. @A(1) @B(1)  String foo(@A(1) @B(1) java.lang. @A(1) @B(1) String str1) {\n" +
				"	       ^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"5. ERROR in p\\X.java (at line 8)\n" +
				"	public @B(1) @A(1) java.lang. @A(1) @B(1)  String foo(@A(1) @B(1) java.lang. @A(1) @B(1) String str1) {\n" +
				"	                              ^^\n" +
				"The annotation @A is disallowed for this location\n" +
				"----------\n" +
				"6. ERROR in p\\X.java (at line 8)\n" +
				"	public @B(1) @A(1) java.lang. @A(1) @B(1)  String foo(@A(1) @B(1) java.lang. @A(1) @B(1) String str1) {\n" +
				"	                                                            ^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"7. ERROR in p\\X.java (at line 8)\n" +
				"	public @B(1) @A(1) java.lang. @A(1) @B(1)  String foo(@A(1) @B(1) java.lang. @A(1) @B(1) String str1) {\n" +
				"	                                                                             ^^\n" +
				"The annotation @A is disallowed for this location\n" +
				"----------\n" +
				"8. ERROR in p\\X.java (at line 10)\n" +
				"	@A(1) @B(1) java.lang.  @B(1) @A(1) String local2;\n" +
				"	      ^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"9. ERROR in p\\X.java (at line 10)\n" +
				"	@A(1) @B(1) java.lang.  @B(1) @A(1) String local2;\n" +
				"	                              ^^\n" +
				"The annotation @A is disallowed for this location\n" +
				"----------\n" +
				"10. ERROR in p\\X.java (at line 12)\n" +
				"	@B @A p.X.Y local4;\n" +
				"	^^\n" +
				"Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)\n" +
				"----------\n" +
				"11. ERROR in p\\X.java (at line 13)\n" +
				"	@B @A p.q.X local5;\n" +
				"	      ^^^\n" +
				"p.q cannot be resolved to a type\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417076, Eclipse compiler rejects multiple annotations for varargs.
	public void test417076() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Target;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface A {\n" +
					"}\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface B {\n" +
					"}\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface C {\n" +
					"}\n" +
					"public class X {\n" +
					"	public @A String foo(int @B @C @D ... args) {\n" +
					"	      return null;\n" +
					"	}\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 13)\n" +
				"	public @A String foo(int @B @C @D ... args) {\n" +
				"	                                ^\n" +
				"D cannot be resolved to a type\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417076, Eclipse compiler rejects multiple annotations for varargs.
	public void test417076b() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Target;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface A {\n" +
					"}\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface B {\n" +
					"}\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface C {\n" +
					"}\n" +
					"public class X {\n" +
					"	public @A String foo(int @B @C @A ... args) {\n" +
					"	      return null;\n" +
					"	}\n" +
					"	public @A String goo(int @B @C @A ... args) {\n" +
					"	}\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 16)\n" +
				"	public @A String goo(int @B @C @A ... args) {\n" +
				"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"This method must return a result of type String\n" +
				"----------\n");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// This is the basic test case which demonstrated the issue for a local variable.
	// We correctly identified the problem in function bar but failed to do so for foo.
	public void test415308a() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Target;\n" +
					"\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface Illegal {\n" +
					"}\n" +
					"class Y {\n" +
					"	static class Z {\n" +
					"		Z() {}\n" +
					"	}\n" +
					"}\n" +
					"class X {\n" +
					"	Y.Z foo() {\n" +
					"		@Illegal Y.Z z = null;\n" +
					"		return z;\n" +
					"	}\n" +
					"	Y.Z bar() {\n" +
					"		Y.Z z = (@Illegal Y.Z)null;\n" +
					"		return z;\n" +
					"	}\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 14)\n" +
				"	@Illegal Y.Z z = null;\n" +
				"	^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 18)\n" +
				"	Y.Z z = (@Illegal Y.Z)null;\n" +
				"	         ^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// This test case is similar to test415308a. SimpleTypes on which annotations are applied are modified to array
	// types.
	public void test415308a2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface Illegal {\n" +
						"}\n" +
						"class Y {\n" +
						"	static class Z {\n" +
						"		Z() {}\n" +
						"	}\n" +
						"}\n" +
						"class X {\n" +
						"	Y.Z[] foo() {\n" +
						"		@Illegal Y.Z[] z = null;\n" +
						"		return z;\n" +
						"	}\n" +
						"	Y.Z[] bar() {\n" +
						"		Y.Z[] z = (@Illegal Y.Z[])null;\n" +
						"		return z;\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 14)\n" +
				"	@Illegal Y.Z[] z = null;\n" +
				"	^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 18)\n" +
				"	Y.Z[] z = (@Illegal Y.Z[])null;\n" +
				"	           ^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// Testing type use annotations on nested types.
	// We check all the qualifiers as we look for a static type. This test checks if we are able to
	// go beyond 1 level as part of the loop.
	public void test415308b() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface Illegal {\n" +
						"}\n" +
						"class Y {\n" +
						"	static class YY {\n" +
						"		class Z {\n" +
						"			Z() {}\n" +
						"		}\n" +
						"	}\n" +
						"}\n" +
						"class X {\n" +
						"	Y.YY.Z foo() {\n" +
						"		@Illegal Y.YY.Z z = null;\n" +
						"		return z;\n" +
						"	}\n" +
						"	Y.YY.Z foo2() {\n" +
						"		Y.@Illegal YY.Z z = null;\n" +
						"		return z;\n" +
						"	}\n" +
						"	Y.YY.Z foo3() {\n" +
						"		Y.YY.@Illegal Z z = null;\n" +
						"		return z;\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 16)\n" +
				"	@Illegal Y.YY.Z z = null;\n" +
				"	^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// This test case is similar to test415308a. SimpleTypes on which annotations are applied are modified to array
	// types.
	public void test415308b2() {
		Runner runner = new Runner();
		runner.testFiles =
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface Illegal {\n" +
						"}\n" +
						"class Y {\n" +
						"	static class YY {\n" +
						"		class Z {\n" +
						"			Z() {}\n" +
						"		}\n" +
						"	}\n" +
						"}\n" +
						"class X {\n" +
						"	Y.YY.Z[] foo() {\n" +
						"		@Illegal Y.YY.Z[] z = null;\n" +
						"		return z;\n" +
						"	}\n" +
						"	Y.YY.Z[] foo2() {\n" +
						"		Y.@Illegal YY.Z[] z = null;\n" +
						"		return z;\n" +
						"	}\n" +
						"	Y.YY.Z[] foo3() {\n" +
						"		Y.YY.@Illegal Z[] z = null;\n" +
						"		return z;\n" +
						"	}\n" +
						"}\n"
				};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. ERROR in X.java (at line 16)\n" +
				"	@Illegal Y.YY.Z[] z = null;\n" +
				"	^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n";
		runner.javacTestOptions = EclipseJustification.EclipseBug561549;
		runner.runNegativeTest();
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// The test case is to validate that we report errors for only type annotations and nothing else in case of
	// of parameter types.
	public void test415308c() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface IllegalTypeUse {\n" +
						"}\n" +
						"@Target({ElementType.TYPE_USE, ElementType.PARAMETER})\n" +
						"@interface LegalTypeUseParam {\n" +
						"}\n" +
						"@Target(ElementType.PARAMETER)\n" +
						"@interface LegalParam {\n" +
						"}\n" +
						"class Y {\n" +
						"	static class Z {\n" +
						"		Z() {}\n" +
						"	}\n" +
						"}\n" +
						"class X {\n" +
						"	Y.Z foo(@LegalParam Y.Z z) { //Legal\n" +
						"		return z;\n" +
						"	}\n" +
						"	Y.Z foo2(@LegalTypeUseParam Y.Z z) { //Legal\n" +
						"		return z;\n" +
						"	}\n" +
						"	Y.Z foo3(@IllegalTypeUse @LegalParam Y.Z z) { //Illegal\n" +
						"		return z;\n" +
						"	}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 25)\n" +
				"	Y.Z foo3(@IllegalTypeUse @LegalParam Y.Z z) { //Illegal\n" +
				"	         ^^^^^^^^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n");
	}
	//[1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	//The test case is to validate type use annotation for class fields.
	public void test415308d() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface Illegal {\n" +
						"}\n" +
						"class Y {\n" +
						"	static class Z {\n" +
						"		Z() {}\n" +
						"	}\n" +
						"}\n" +
						"class X {\n" +
						"   @Illegal \n" +
						"	Y.Z z;\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 13)\n" +
				"	@Illegal \n" +
				"	^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n");
	}
	//[1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	//The test case checks for annotations which are not exclusively TYPE_USE. We should not report a error.
	public void test415308d2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"\n" +
						"@Target({ElementType.TYPE_USE, ElementType.FIELD})\n" +
						"@interface Legal {\n" +
						"}\n" +
						"class Y {\n" +
						"	static class Z {\n" +
						"		Z() {}\n" +
						"	}\n" +
						"}\n" +
						"class X {\n" +
						"   @Legal \n" +
						"	Y.Z z;\n" +
						"}\n"
				},
				"");
	}
	//[1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	//The test case is to validate type use annotation for class fields.
	//We check all the qualifiers as we look for a static type. This test checks if we are able to
	//go beyond 1 level as part of the loop.
	public void test415308e() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface Illegal {\n" +
						"}\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface Illegal2 {\n" +
						"}\n" +
						"@Target(ElementType.FIELD)\n" +
						"@interface Legal {\n" +
						"}\n" +
						"class Y {\n" +
						"	static class YY {\n" +
						"		class Z {\n" +
						"			Z() {}\n" +
						"		}\n" +
						"	}\n" +
						"}\n" +
						"class X {\n" +
						"   @Legal @Illegal @Illegal2\n" +
						"	Y.YY.Z z;\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 21)\n" +
				"	@Legal @Illegal @Illegal2\n" +
				"	       ^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 21)\n" +
				"	@Legal @Illegal @Illegal2\n" +
				"	                ^^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// The test case is to validate type use annotations on return types for methods.
	public void test415308f() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface Illegal {\n" +
						"}\n" +
						"class Y {\n" +
						"	static class Z {\n" +
						"		Z() {}\n" +
						"	}\n" +
						"}\n" +
						"class X {\n" +
						"   public @Illegal Y.Z foo() { return null;}\n" +
						"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 13)\n" +
				"	public @Illegal Y.Z foo() { return null;}\n" +
				"	       ^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n");
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// The test case is a array version of test415308f.
	public void test415308f2() {
		Runner runner = new Runner();
		runner.testFiles =
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface Illegal {\n" +
						"}\n" +
						"class Y {\n" +
						"	static class Z {\n" +
						"		Z() {}\n" +
						"	}\n" +
						"}\n" +
						"class X {\n" +
						"   public @Illegal Y.Z[] foo() { return null;}\n" +
						"}\n"
				};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. ERROR in X.java (at line 13)\n" +
				"	public @Illegal Y.Z[] foo() { return null;}\n" +
				"	       ^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n";
		runner.javacTestOptions = EclipseJustification.EclipseBug561549;
		runner.runNegativeTest();
	}
	// [1.8][compiler] Illegal type annotations not rejected (https://bugs.eclipse.org/bugs/show_bug.cgi?id=415308)
	// The test case is used to test enums with type annotations.
	public void test415308g() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Target;\n" +
					"\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface Illegal {\n" +
					"}\n" +
					"class Y {\n" +
					"	enum A { B }\n" +
					"}\n" +
					"class X {\n" +
					"	@Illegal Y.A foo(@Illegal Y.A a) {\n" +
					"		return a;\n" +
					"	}\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	@Illegal Y.A foo(@Illegal Y.A a) {\n" +
				"	^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 11)\n" +
				"	@Illegal Y.A foo(@Illegal Y.A a) {\n" +
				"	                 ^^^^^^^^\n" +
				"Type annotations are not allowed on type names used to access static members\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418041, NPE during AST creation.
	public void test418041() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Target;\n" +
					"import java.util.List;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface Readonly {\n" +
					"}\n" +
					"class UnmodifiableList<T> implements\n" +
					"@Readonly List<@Readonly T> { }\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.addAll(int, Collection<? extends T>)\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.addAll(Collection<? extends T>)\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.lastIndexOf(Object)\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.subList(int, int)\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.contains(Object)\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.get(int)\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.retainAll(Collection<?>)\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.clear()\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.indexOf(Object)\n" +
				"----------\n" +
				"10. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.toArray(T[])\n" +
				"----------\n" +
				"11. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.toArray()\n" +
				"----------\n" +
				"12. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.isEmpty()\n" +
				"----------\n" +
				"13. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.listIterator(int)\n" +
				"----------\n" +
				"14. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.listIterator()\n" +
				"----------\n" +
				"15. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.add(int, T)\n" +
				"----------\n" +
				"16. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.add(T)\n" +
				"----------\n" +
				"17. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.set(int, T)\n" +
				"----------\n" +
				"18. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.size()\n" +
				"----------\n" +
				"19. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.containsAll(Collection<?>)\n" +
				"----------\n" +
				"20. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.remove(int)\n" +
				"----------\n" +
				"21. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.remove(Object)\n" +
				"----------\n" +
				"22. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.removeAll(Collection<?>)\n" +
				"----------\n" +
				"23. ERROR in X.java (at line 7)\n" +
				"	class UnmodifiableList<T> implements\n" +
				"	      ^^^^^^^^^^^^^^^^\n" +
				"The type UnmodifiableList<T> must implement the inherited abstract method List<T>.iterator()\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418041, NPE during AST creation.
	public void test418041a() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface Marker {}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 1)\n" +
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" +
				"	                                          ^\n" +
				"Y cannot be resolved to a type\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 1)\n" +
				"	public class X <@Marker T extends @Marker Y<@Marker ?>, @Marker Q extends @Marker Integer> {\n" +
				"	                                                                          ^^^^^^^^^^^^^^^\n" +
				"The type parameter Q should not be bounded by the final type Integer. Final types cannot be further extended\n" +
				"----------\n");
	}
	public void testWildcardCapture() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.util.List;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface NonNull {\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface Nullable {\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<@Nullable ? extends X> lx1 = null;\n" +
				"		List<@NonNull ? extends X> lx2 = null;\n" +
				"		lx1 = lx2;\n" +
				"		lx1.add(lx2.get(0));\n" +
				"		lx1.add(lx1.get(0));\n" +
				"       getAdd(lx1, lx2);\n" +
				"	}\n" +
				"	static <@NonNull P>  void getAdd(List<P> p1, List<P> p2) {\n" +
				"		p1.add(p2.get(0));\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 18)\n" +
			"	lx1.add(lx2.get(0));\n" +
			"	    ^^^\n" +
			"The method add(capture#3-of ? extends X) in the type List<capture#3-of ? extends X> is not applicable for the arguments (capture#4-of ? extends X)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 19)\n" +
			"	lx1.add(lx1.get(0));\n" +
			"	    ^^^\n" +
			"The method add(capture#5-of ? extends X) in the type List<capture#5-of ? extends X> is not applicable for the arguments (capture#6-of ? extends X)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 20)\n" +
			"	getAdd(lx1, lx2);\n" +
			"	^^^^^^\n" +
			"The method getAdd(List<P>, List<P>) in the type X is not applicable for the arguments (List<capture#7-of ? extends X>, List<capture#8-of ? extends X>)\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=414038, [1.8][compiler] CCE in resolveAnnotations
	public void testBug414038() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface NonNull { int[].class value() default 0;}\n" +
				"public class X extends @NonNull() Object {    \n" +
				"    public static int i = 0; \n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	@interface NonNull { int[].class value() default 0;}\n" +
			"	                          ^^^^^^\n" +
			"Syntax error on tokens, delete these tokens\n" +
			"----------\n");
	}
	public void testGenericConstructor() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Annotation;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"} \n" +
				"public class X { \n" +
				"\n" +
				"	<P> @T X() {\n" +
				"	}\n" +
				"   @T <P> X(X x) {\n" +
				"   }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	<P> @T X() {\n" +
			"	    ^\n" +
			"Syntax error on token \"@\", delete this token\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419833, [1.8] NPE in CompilationUnitProblemFinder and ASTNode
	public void test419833() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n" +
				"class S {\n" +
				"}\n" +
				"interface I {\n" +
				"}\n" +
				"public class X extends @T S implements @T  {\n" +
				"	public int foo() {\n" +
				"       return 0;\n" +
				"	}	\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	public class X extends @T S implements @T  {\n" +
			"	                                       ^\n" +
			"Syntax error on token \"@\", delete this token\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420038,  [1.8][compiler] Tolerate type annotations on array dimensions of class literals for now for compatibility.
	public void test420038() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Class<?> c = int @T [].class; \n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	Class<?> c = int @T [].class; \n" +
			"	                 ^^\n" +
			"Syntax error, type annotations are illegal here\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420284, [1.8][compiler] IllegalStateException from TypeSystem.cacheDerivedType
	public void test420284() {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.Serializable;\n" +
				"import java.util.List;\n" +
				"public class X {\n" +
				"    void foo(Object o) {\n" +
				"        Integer i = (Integer & Serializable) o;\n" +
				"        List<@NonNull Integer> l;\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	List<@NonNull Integer> l;\n" +
			"	      ^^^^^^^\n" +
			"NonNull cannot be resolved to a type\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391521, [1.8][compiler] Error highlighting is not accurate for type references with type annotations
	public void test391521() {
		runNegativeTest(
			new String[] {
				"X.java",
				"class Y {}\n" +
				"public class X {\n" +
				"    Y y1 = (@Marker Z) null;\n" +
				"    Y y2 = new @Marker Z();\n" +
				"    Y[] y3 = (@Marker Z[]) null;\n" +
				"    Y[] y4 = new @Marker Z[0];\n" +
				"    Y[] y5 = (@Marker Y.Z) null;\n" +
				"    Y[] y6 = new @Marker Y.  Z();\n" +
				"    Y[] y7 = (@Marker Y.Z[]) null;\n" +
				"    Y[] y8 = new @Marker Y[0].  Z;\n" +
				"    Y[] y9 = new @Marker Y.  Z[0];\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker{}\n" +
				"\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Y y1 = (@Marker Z) null;\n" +
			"	                ^\n" +
			"Z cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	Y y2 = new @Marker Z();\n" +
			"	                   ^\n" +
			"Z cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	Y[] y3 = (@Marker Z[]) null;\n" +
			"	                  ^\n" +
			"Z cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 6)\n" +
			"	Y[] y4 = new @Marker Z[0];\n" +
			"	                     ^\n" +
			"Z cannot be resolved to a type\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 7)\n" +
			"	Y[] y5 = (@Marker Y.Z) null;\n" +
			"	                  ^^^\n" +
			"Y.Z cannot be resolved to a type\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 8)\n" +
			"	Y[] y6 = new @Marker Y.  Z();\n" +
			"	                     ^^^^^\n" +
			"Y.Z cannot be resolved to a type\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 9)\n" +
			"	Y[] y7 = (@Marker Y.Z[]) null;\n" +
			"	                  ^^^\n" +
			"Y.Z cannot be resolved to a type\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 10)\n" +
			"	Y[] y8 = new @Marker Y[0].  Z;\n" +
			"	                            ^\n" +
			"Z cannot be resolved or is not a field\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 11)\n" +
			"	Y[] y9 = new @Marker Y.  Z[0];\n" +
			"	                     ^^^^^\n" +
			"Y.Z cannot be resolved to a type\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=414038, [1.8][compiler] CCE in resolveAnnotations
	public void test414038() {
		runNegativeTest(
			new String[] {
					"X.java",
					"import java.lang.annotation.*;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface NonNull { int[].class value() default 0;}\n" +
					"public class X extends @NonNull() Object {    \n" +
					"    public static int i = 0; \n" +
					"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	@interface NonNull { int[].class value() default 0;}\n" +
			"	                          ^^^^^^\n" +
			"Syntax error on tokens, delete these tokens\n" +
			"----------\n",
			true);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=421791,  [1.8][compiler] TYPE_USE annotations should be allowed on annotation type declarations
	public void test421791() {
		runNegativeTest(
				new String[] {
						"X.java",
						"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface T {}\n" +
						"@T\n" +
						"@interface T2 {}\n" +
						"public class X {}\n"
				},
				"",
				true);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424977,  [1.8][compiler]ArrayIndexIndexOutOfBoundException in annotated wrong<> code
	public void testBug426977() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"public class X {\n" +
				"    test.@A Outer<>.@A Inner<> i;\n" +
				"}\n" +
				"class Outer<T> {\n" +
				"    class Inner {}\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface A {}\n"
			},
			"----------\n" +
			"1. ERROR in test\\X.java (at line 6)\n" +
			"	test.@A Outer<>.@A Inner<> i;\n" +
			"	^^^^^^^^^^^^^\n" +
			"Incorrect number of arguments for type Outer<T>; it cannot be parameterized with arguments <>\n" +
			"----------\n",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424977,  [1.8][compiler] ArrayIndexIndexOutOfBoundException in annotated wrong<> code
	public void testBug426977a() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"public class X {\n" +
				"    test.@A Outer<Object>.@A Inner<> i;\n" +
				"}\n" +
				"class Outer<T> {\n" +
				"    class Inner {}\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface A {}\n"
			},
			"----------\n" +
			"1. ERROR in test\\X.java (at line 6)\n" +
			"	test.@A Outer<Object>.@A Inner<> i;\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The type Outer<Object>.Inner is not generic; it cannot be parameterized with arguments <>\n" +
			"----------\n",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425599, [1.8][compiler] ISE when trying to compile qualified and annotated class instance creation
	public void test425599() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"public class X {\n" +
				"    Object ax = new @A Outer().new Middle<String>();\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE) @interface A {}\n" +
				"class Outer {\n" +
				"    class Middle<E> {}\n" +
				"}\n"
			},
			"",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427955, [1.8][compiler] NPE in TypeSystem.getUnannotatedType
	public void test427955() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" +
				" * @param <K> unused\n" +
				" * @param <V> unused\n" +
				" */\n" +
				"public class X {}\n" +
				"class Outer<K, V> {\n" +
				"  void method() {\n" +
				"    //Internal compiler error: java.lang.NullPointerException at\n" +
				"    // org.eclipse.jdt.internal.compiler.lookup.TypeSystem.getUnannotatedType(TypeSystem.java:76)\n" +
				"    new Inner<>(null);\n" +
				"  }\n" +
				"  final class Inner<K2, V2> {\n" +
				"    /**\n" +
				"     * @param next unused \n" +
				"     */\n" +
				"    Inner(Inner<K2, V2> next) {}\n" +
				"  }\n" +
				"}\n"
			},
			"",
			null,
			true,
			customOptions);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419827,  [1.8] Annotation with TYPE_USE as target is not allowed to use container with target TYPE
	public void test419827a() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Repeatable;\n" +
					"import java.lang.annotation.Target;\n" +
					"\n" +
					"@Target({ElementType.TYPE_USE})\n" +
					"@Repeatable(FooContainer.class)\n" +
					"@interface Foo {}\n" +
					"@Target({ElementType.TYPE, ElementType.TYPE_USE})\n" +
					"@interface FooContainer {\n" +
					"	Foo[] value();\n" +
					"}\n" +
					"public class X{}\n"
				},
				"",
				true);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419827,  [1.8] Annotation with TYPE_USE as target is not allowed to use container with target TYPE
	// Although the target of FooContainer is different from that of Foo, Foo container cannot be used in any place where
	// Foo can't be used.
	public void test419827b() {
		runNegativeTest(
				new String[] {
					"X.java",
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Repeatable;\n" +
					"import java.lang.annotation.Target;\n" +
					"\n" +
					"@Target({ElementType.TYPE_USE})\n" +
					"@Repeatable(FooContainer.class)\n" +
					"@interface Foo {}\n" +
					"@Target({ElementType.TYPE})\n" +
					"@interface FooContainer {\n" +
					"	Foo[] value();\n" +
					"}\n" +
					"public class X{}\n"
				},
				"",
				true);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=552082
	public void test552082_comment_0() throws Exception {
		this.runNegativeTest(
				new String[] {
					"EclipseReturnValueAnnotationTest.java",
					"class EclipseReturnValueAnnotationTest {\n" +
					"    \n" +
					"    @interface SomeAnnotation {}\n" +
					"\n" +
					"     public @SomeAnnotation String foo(Object anything) {\n" +
					"         return \"foo\";\n" +
					"     }\n" +
					"\n" +
					"     public  <T>  @SomeAnnotation String bar(T anything) { // Error - type annotation position\n" +
					"         return \"bar\";\n" +
					"     }\n" +
					"\n" +
					"     public @SomeAnnotation <T> String baz(T anything) {  // OK - declaration annotation on method \n" +
					"         return \"baz\";\n" +
					"     }\n" +
					"}\n",
				},
				"----------\n" +
				"1. ERROR in EclipseReturnValueAnnotationTest.java (at line 9)\n" +
				"	public  <T>  @SomeAnnotation String bar(T anything) { // Error - type annotation position\n" +
				"	             ^^^^^^^^^^^^^^^\n" +
				"Annotation types that do not specify explicit target element types cannot be applied here\n" +
				"----------\n");
	}
}
