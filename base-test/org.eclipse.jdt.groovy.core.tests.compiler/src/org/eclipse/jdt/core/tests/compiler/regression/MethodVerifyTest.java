/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class MethodVerifyTest extends AbstractComparableTest {
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 121 };
//		TESTS_RANGE = new int[] { 113, -1};
	}

	public MethodVerifyTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}
	
	public static Class testClass() {
		return MethodVerifyTest.class;
	}

	String mustOverrideMessage(String method, String type) {
		return "The method " + method + " of type " + type +
			(new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
				? " must override a superclass method\n"
				: " must override or implement a supertype method\n");
	}
	public void test001() {
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> extends X<A> { public void foo(T t) {} }\n" +
				"class X<U> { public void foo(U u) {} }\n" +
				"class A {}\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	public class Y<T> extends X<A> { public void foo(T t) {} }\n" + 
			"	                                             ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in Y<T> and foo(U) in X<A> have the same erasure, yet neither overrides the other
		);
	}
	
	public void test001a() {
		this.runNegativeTest(
				new String[] {
					"J.java",
					"public class J<T> implements I<A> { public void foo(T t) {} }\n" +
					"interface I<U> { public void foo(U u); }\n" +
					"class A {}\n"
				},
				"----------\n" + 
				"1. ERROR in J.java (at line 1)\r\n" + 
				"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
				"	             ^\n" + 
				"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" + 
				"----------\n" + 
				"2. ERROR in J.java (at line 1)\r\n" + 
				"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
				"	                                                ^^^^^^^^\n" + 
				"Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
				"----------\n"
				// J is not abstract and does not override abstract method foo(A) in I
			);
	}
	public void test001b() {
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n" +
				"class X<U> { public void foo(U u) {} }\n"
			},
			"----------\n" + 
			"1. WARNING in YY.java (at line 1)\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\n" + 
			"	                           ^\n" + 
			"X is a raw type. References to generic type X<U> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in YY.java (at line 1)\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\n" + 
			"	                                           ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type YY<T> has the same erasure as foo(Object) of type X but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in YY<T> and foo(U) in X have the same erasure, yet neither overrides the other
		);
	}
	public void test001c() {
		this.runNegativeTest(
				new String[] {
						"JJ.java",
						"public class JJ<T> implements I { public void foo(T t) {} }\n" +
						"interface I<U> { public void foo(U u); }\n"
				},
				"----------\n" + 
				"1. ERROR in JJ.java (at line 1)\n" + 
				"	public class JJ<T> implements I { public void foo(T t) {} }\n" + 
				"	             ^^\n" + 
				"The type JJ<T> must implement the inherited abstract method I.foo(Object)\n" + 
				"----------\n" + 
				"2. WARNING in JJ.java (at line 1)\n" + 
				"	public class JJ<T> implements I { public void foo(T t) {} }\n" + 
				"	                              ^\n" + 
				"I is a raw type. References to generic type I<U> should be parameterized\n" + 
				"----------\n" + 
				"3. ERROR in JJ.java (at line 1)\n" + 
				"	public class JJ<T> implements I { public void foo(T t) {} }\n" + 
				"	                                              ^^^^^^^^\n" + 
				"Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(Object) of type I but does not override it\n" + 
				"----------\n"
				// JJ is not abstract and does not override abstract method foo(java.lang.Object) in I
		);
	}
	public void test001d() {
		this.runConformTest(
				new String[] {
						"YYY.java",
						"public class YYY<T> extends X<T> { public void foo(T t) {} }\n" +
						"class X<U> { public void foo(U u) {} }\n"
				},
				""
		);
	}
	public void test001e() {
		this.runConformTest(
				new String[] {
						"JJJ.java",
						"public class JJJ<T> implements I<T> { public void foo(T t) {} }\n" +
						"interface I<U> { public void foo(U u); }\n"
				},
				""
		);
	}

	public void test002() { // separate files
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> extends X<A> { public void foo(T t) {} }\n" +
				"class A {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	public class Y<T> extends X<A> { public void foo(T t) {} }\n" + 
			"	                                             ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in Y<T> and foo(U) in X<A> have the same erasure, yet neither overrides the other
		);
	}
	public void test002a() { // separate files
		this.runNegativeTest(
			new String[] {
				"J.java",
				"public class J<T> implements I<A> { public void foo(T t) {} }\n" +
				"class A {}\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n"
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	             ^\n" + 
			"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	                                                ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// J is not abstract and does not override abstract method foo(A) in I
		);
	}
	public void test002b() { // separate files
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n"
			},
			"----------\n" + 
			"1. WARNING in YY.java (at line 1)\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\n" + 
			"	                           ^\n" + 
			"X is a raw type. References to generic type X<U> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in YY.java (at line 1)\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\n" + 
			"	                                           ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type YY<T> has the same erasure as foo(Object) of type X but does not override it\n" + 
			"----------\n"
			// name clash: foo(T) in YY<T> and foo(U) in X have the same erasure, yet neither overrides the other
		);
	}
	public void test002c() { // separate files
		this.runNegativeTest(
			new String[] {
				"JJ.java",
				"public class JJ<T> implements I { public void foo(T t) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n"
			},
			"----------\n" + 
			"1. ERROR in JJ.java (at line 1)\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\n" + 
			"	             ^^\n" + 
			"The type JJ<T> must implement the inherited abstract method I.foo(Object)\n" + 
			"----------\n" + 
			"2. WARNING in JJ.java (at line 1)\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\n" + 
			"	                              ^\n" + 
			"I is a raw type. References to generic type I<U> should be parameterized\n" + 
			"----------\n" + 
			"3. ERROR in JJ.java (at line 1)\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\n" + 
			"	                                              ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(Object) of type I but does not override it\n" + 
			"----------\n"
			// JJ is not abstract and does not override abstract method foo(java.lang.Object) in I
		);
	}
	public void test002d() { // separate files
		this.runConformTest(
			new String[] {
				"YYY.java",
				"public class YYY<T> extends X<T> { public void foo(T t) {} }\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n"
			},
			""
		);
	}
	public void test002e() { // separate files
		this.runConformTest(
			new String[] {
				"JJJ.java",
				"public class JJJ<T> implements I<T> { public void foo(T t) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n"
			},
			""
		);
	}

	public void test003() { // pick up superTypes as binaries
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y<T> extends X<A> { public void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	public class Y<T> extends X<A> { public void foo(T t) {} }\n" + 
			"	                                             ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(U) of type X<U> but does not override it\n" + 
			"----------\n",
			// name clash: foo(T) in Y<T> and foo(U) in X<A> have the same erasure, yet neither overrides the other
			null,
			false,
			null
		);
	}
	public void test003a() { // pick up superTypes as binaries
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"J.java",
				"public class J<T> implements I<A> { public void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	             ^\n" + 
			"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in J.java (at line 1)\r\n" + 
			"	public class J<T> implements I<A> { public void foo(T t) {} }\r\n" + 
			"	                                                ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type J<T> has the same erasure as foo(U) of type I<U> but does not override it\n" + 
			"----------\n",
			// J is not abstract and does not override abstract method foo(A) in I
			null,
			false,
			null
		);
	}
	public void test003b() {
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"YY.java",
				"public class YY<T> extends X { public void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. WARNING in YY.java (at line 1)\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\n" + 
			"	                           ^\n" + 
			"X is a raw type. References to generic type X<U> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in YY.java (at line 1)\n" + 
			"	public class YY<T> extends X { public void foo(T t) {} }\n" + 
			"	                                           ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type YY<T> has the same erasure as foo(Object) of type X but does not override it\n" + 
			"----------\n",
			// name clash: foo(T) in YY<T> and foo(U) in X have the same erasure, yet neither overrides the other
			null,
			false,
			null
		);
	}
	public void test003c() {
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"JJ.java",
				"public class JJ<T> implements I { public void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in JJ.java (at line 1)\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\n" + 
			"	             ^^\n" + 
			"The type JJ<T> must implement the inherited abstract method I.foo(Object)\n" + 
			"----------\n" + 
			"2. WARNING in JJ.java (at line 1)\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\n" + 
			"	                              ^\n" + 
			"I is a raw type. References to generic type I<U> should be parameterized\n" + 
			"----------\n" + 
			"3. ERROR in JJ.java (at line 1)\n" + 
			"	public class JJ<T> implements I { public void foo(T t) {} }\n" + 
			"	                                              ^^^^^^^^\n" + 
			"Name clash: The method foo(T) of type JJ<T> has the same erasure as foo(Object) of type I but does not override it\n" + 
			"----------\n",
			// JJ is not abstract and does not override abstract method foo(java.lang.Object) in I
			null,
			false,
			null
		);
	}
	public void test003d() {
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runConformTest(
			new String[] {
				"YYY.java",
				"public class YYY<T> extends X<T> { public void foo(T t) {} }\n"
			},
			"",
			null,
			false,
			null
		);
	}
	public void test003e() {
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public void foo(U u) {} }\n",
				"I.java",
				"interface I<U> { public void foo(U u); }\n",
			},
			""
		);
		this.runConformTest(
			new String[] {
				"JJJ.java",
				"public class JJJ<T> implements I<T> { public void foo(T t) {} }\n"
			},
			"",
			null,
			false,
			null
		);
	}

	public void test004() { // all together
		this.runNegativeTest(
			new String[] {
				"ALL.java",
				"class A {}\n" +
				"class B {}\n" +
				"class X<U> { public U foo() {return null;} }\n" +
				"interface I<U> { public U foo(); }\n" +

				"class J<T> implements I<B> { public T foo() {return null;} }\n" +
				"class K<T> implements I<T> { public T foo() {return null;} }\n" +
				"class L<T> implements I { public T foo() {return null;} }\n" +

				"class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }\n" +
				"class Z<T> extends X<T> { @Override public T foo() { return super.foo(); } }\n" +
				"class W<T> extends X { @Override public T foo() { return super.foo(); } }\n",
			},
			"----------\n" + 
			"1. ERROR in ALL.java (at line 5)\n" + 
			"	class J<T> implements I<B> { public T foo() {return null;} }\n" + 
			"	                                    ^\n" + 
			"The return type is incompatible with I<B>.foo()\n" + 
			"----------\n" + 
			"2. WARNING in ALL.java (at line 7)\n" + 
			"	class L<T> implements I { public T foo() {return null;} }\n" + 
			"	                      ^\n" + 
			"I is a raw type. References to generic type I<U> should be parameterized\n" + 
			"----------\n" + 
			"3. ERROR in ALL.java (at line 8)\n" + 
			"	class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }\n" + 
			"	                                           ^\n" + 
			"The return type is incompatible with X<A>.foo()\n" + 
			"----------\n" + 
			"4. ERROR in ALL.java (at line 8)\n" + 
			"	class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }\n" + 
			"	                                                            ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from A to T\n" + 
			"----------\n" + 
			"5. WARNING in ALL.java (at line 10)\n" + 
			"	class W<T> extends X { @Override public T foo() { return super.foo(); } }\n" + 
			"	                   ^\n" + 
			"X is a raw type. References to generic type X<U> should be parameterized\n" + 
			"----------\n" + 
			"6. ERROR in ALL.java (at line 10)\n" + 
			"	class W<T> extends X { @Override public T foo() { return super.foo(); } }\n" + 
			"	                                                         ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to T\n" + 
			"----------\n"
			/*
			ALL.java:5: J is not abstract and does not override abstract method foo() in I
			ALL.java:5: foo() in J cannot implement foo() in I; attempting to use incompatible return type
			ALL.java:8: foo() in Y cannot override foo() in X; attempting to use incompatible return type
			ALL.java:8: incompatible types
			found   : A
			required: T
			class Y<T> extends X<A> { public T foo() { return super.foo(); } }
			                                                           ^
			ALL.java:10: incompatible types
			found   : java.lang.Object
			required: T
			class W<T> extends X { public T foo() { return super.foo(); } }
			 */
		);
	}

	public void test005() { // separate files
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public U foo() {return null;} }\n",
				"I.java",
				"interface I<U> { public U foo(); }\n",

				"J.java",
				"class J<T> implements I<B> { public T foo() {return null;} }\n",
				"K.java",
				"class K<T> implements I<T> { public T foo() {return null;} }\n",
				"L.java",
				"class L<T> implements I { public T foo() {return null;} }\n",

				"Y.java",
				"class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }\n",
				"Z.java",
				"class Z<T> extends X<T> { @Override public T foo() { return super.foo(); } }\n",
				"W.java",
				"class W<T> extends X { @Override public T foo() { return super.foo(); } }\n",
			},
			"----------\n" + 
			"1. ERROR in J.java (at line 1)\n" + 
			"	class J<T> implements I<B> { public T foo() {return null;} }\n" + 
			"	                                    ^\n" + 
			"The return type is incompatible with I<B>.foo()\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in L.java (at line 1)\n" + 
			"	class L<T> implements I { public T foo() {return null;} }\n" + 
			"	                      ^\n" + 
			"I is a raw type. References to generic type I<U> should be parameterized\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }\n" + 
			"	                                           ^\n" + 
			"The return type is incompatible with X<A>.foo()\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 1)\n" + 
			"	class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }\n" + 
			"	                                                            ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from A to T\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in W.java (at line 1)\n" + 
			"	class W<T> extends X { @Override public T foo() { return super.foo(); } }\n" + 
			"	                   ^\n" + 
			"X is a raw type. References to generic type X<U> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in W.java (at line 1)\n" + 
			"	class W<T> extends X { @Override public T foo() { return super.foo(); } }\n" + 
			"	                                                         ^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Object to T\n" + 
			"----------\n"
			/*
			J.java:1: J is not abstract and does not override abstract method foo() in I
			J.java:1: foo() in J cannot implement foo() in I; attempting to use incompatible return type
			W.java:1: incompatible types
			found   : java.lang.Object
			required: T
			class W<T> extends X { public T foo() { return super.foo(); } }
			Y.java:1: foo() in Y cannot override foo() in X; attempting to use incompatible return type
			Y.java:1: incompatible types
			found   : A
			required: T
			class Y<T> extends X<A> { public T foo() { return super.foo(); } }
			 */
		);
	}

	public void test006() { // pick up superTypes as binaries
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {}\n",
				"B.java",
				"class B {}\n",
				"X.java",
				"class X<U> { public U foo() {return null;} }\n",
				"I.java",
				"interface I<U> { public U foo(); }\n",
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"J.java",
				"class J<T> implements I<B> { public T foo() {return null;} }\n",
				"K.java",
				"class K<T> implements I<T> { public T foo() {return null;} }\n",
				"L.java",
				"class L<T> implements I { public T foo() {return null;} }\n",

				"Y.java",
				"class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }\n",
				"Z.java",
				"class Z<T> extends X<T> { @Override public T foo() { return super.foo(); } }\n",
				"W.java",
				"class W<T> extends X { @Override public T foo() { return super.foo(); } }\n",
				},
				"----------\n" + 
				"1. ERROR in J.java (at line 1)\n" + 
				"	class J<T> implements I<B> { public T foo() {return null;} }\n" + 
				"	                                    ^\n" + 
				"The return type is incompatible with I<B>.foo()\n" + 
				"----------\n" + 
				"----------\n" + 
				"1. WARNING in L.java (at line 1)\n" + 
				"	class L<T> implements I { public T foo() {return null;} }\n" + 
				"	                      ^\n" + 
				"I is a raw type. References to generic type I<U> should be parameterized\n" + 
				"----------\n" + 
				"----------\n" + 
				"1. ERROR in Y.java (at line 1)\n" + 
				"	class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }\n" + 
				"	                                           ^\n" + 
				"The return type is incompatible with X<A>.foo()\n" + 
				"----------\n" + 
				"2. ERROR in Y.java (at line 1)\n" + 
				"	class Y<T> extends X<A> { @Override public T foo() { return super.foo(); } }\n" + 
				"	                                                            ^^^^^^^^^^^\n" + 
				"Type mismatch: cannot convert from A to T\n" + 
				"----------\n" + 
				"----------\n" + 
				"1. WARNING in W.java (at line 1)\n" + 
				"	class W<T> extends X { @Override public T foo() { return super.foo(); } }\n" + 
				"	                   ^\n" + 
				"X is a raw type. References to generic type X<U> should be parameterized\n" + 
				"----------\n" + 
				"2. ERROR in W.java (at line 1)\n" + 
				"	class W<T> extends X { @Override public T foo() { return super.foo(); } }\n" + 
				"	                                                         ^^^^^^^^^^^\n" + 
				"Type mismatch: cannot convert from Object to T\n" + 
				"----------\n",
			/*
			J.java:1: J is not abstract and does not override abstract method foo() in I
			J.java:1: foo() in J cannot implement foo() in I; attempting to use incompatible return type
			W.java:1: incompatible types
			found   : java.lang.Object
			required: T
			class W<T> extends X { public T foo() { return super.foo(); } }
			Y.java:1: foo() in Y cannot override foo() in X; attempting to use incompatible return type
			Y.java:1: incompatible types
			found   : A
			required: T
			class Y<T> extends X<A> { public T foo() { return super.foo(); } }
			 */
			null,
			false,
			null
		);
	}

	public void test007() { // simple covariance cases
		this.runConformTest(
			new String[] {
				"A.java",
				"abstract class A implements I {}\n" +
				"interface I extends J { String foo(); }\n" +
				"interface J { Object foo(); }\n",
				"X.java",
				"abstract class X1 extends A implements J {}\n"
			},
			""
		);
	}
	public void test007a() { // simple covariance cases
		this.runNegativeTest(
			new String[] {
				"A.java",
				"abstract class A implements I {}\n" +
				"interface I extends J { Object foo(); }\n" + // with javac only this type gets an error
				"interface J { String foo(); }\n",
				"X.java",
				"abstract class X2 extends A implements J {}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\n" + 
			"	interface I extends J { Object foo(); }\n" + 
			"	                        ^^^^^^\n" + 
			"The return type is incompatible with J.foo()\n" + 
			"----------\n"
		);
	}
	public void test007b() { // simple covariance cases
		this.runConformTest(
			new String[] {
				"A.java",
				"abstract class A implements I {}\n" +
				"interface I { String foo(); }\n",
				"X.java",
				"abstract class X3 extends A implements J {}\n" +
				"interface J { Object foo(); }\n"
			},
			""
		);
	}
	public void test007c() { // simple covariance cases
		this.runConformTest(
			new String[] {
				"A.java",
				"abstract class A implements I {}\n" +
				"interface I { Object foo(); }\n",
				"X.java",
				"abstract class X4 extends A implements J {}\n" +
				"interface J { String foo(); }\n"
			},
			""
		);
	}
	public void test007d() { // simple covariance cases
		this.runConformTest(
			new String[] {
				"A.java",
				"class A { public String foo() { return null; } }\n" +
				"interface I { Object foo(); }\n",
				"X.java",
				"abstract class X5 extends A implements I {}\n"
			},
			""
		);
	}
	public void test007e() { // simple covariance cases
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public Object foo() { return null; } }\n" +
				"interface I { String foo(); }\n",
				"X.java",
				"abstract class X6 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X6 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"The return type is incompatible with I.foo(), A.foo()\n" + 
			"----------\n"
		);
	}
	public void test007f() { // simple covariance cases
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { int get(short i, short s) { return i; } }\n" +
				"class B extends A { @Override short get(short i, short s) {return i; } }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	class B extends A { @Override short get(short i, short s) {return i; } }\r\n" + 
			"	                              ^^^^^\n" + 
			"The return type is incompatible with A.get(short, short)\n" + 
			"----------\n"
		);
	}
	
	public void test008() { // covariance test
		this.runNegativeTest(
			new String[] {
				"ALL.java",
				"interface I { I foo(); }\n" +
				"class A implements I { public A foo() { return null; } }\n" +
				"class B extends A { @Override public B foo() { return null; } }\n" +
				"class C extends B { @Override public A foo() { return null; } }\n" +
				"class D extends B implements I {}\n",
			},
			"----------\n" + 
			"1. ERROR in ALL.java (at line 4)\r\n" + 
			"	class C extends B { @Override public A foo() { return null; } }\r\n" + 
			"	                                     ^\n" + 
			"The return type is incompatible with B.foo()\n" + 
			"----------\n"
			// foo() in C cannot override foo() in B; attempting to use incompatible return type
		);
	}

	public void test009() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class G<T> {}\n" +
				"interface I { void foo(G<I> x); }\n" +
				"abstract class A implements I { void foo(G<A> x) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 3)\r\n" + 
			"	abstract class A implements I { void foo(G<A> x) {} }\r\n" + 
			"	                                     ^^^^^^^^^^^\n" + 
			"Name clash: The method foo(G<A>) of type A has the same erasure as foo(G<I>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(G<A>) in A and foo(G<I>) in I have the same erasure, yet neither overrides the other
		);
	}
	public void test009a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class G<T> {}\n" +
				"interface I { I foo(G<I> x); }\n" +
				"abstract class A implements I { I foo(G<A> x) { return null; } }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 3)\r\n" + 
			"	abstract class A implements I { I foo(G<A> x) { return null; } }\r\n" + 
			"	                                  ^^^^^^^^^^^\n" + 
			"Name clash: The method foo(G<A>) of type A has the same erasure as foo(G<I>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: foo(G<A>) in A and foo(G<I>) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test010() { // executable bridge method case
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public X foo() {\n" +
				"        System.out.println(\"Did NOT add bridge method\");\n" +
				"        return this;\n" +
				"    }\n" +
				"    public static void main(String[] args) throws Exception {\n" +
				"        X x = new A();\n" +
				"        x.foo();\n" +
				"        System.out.print(\" + \");\n" +
				"        I i = new A();\n" +
				"        i.foo();\n" +
				"    }\n" +
				"}\n" +
				"interface I {\n" +
				"    public I foo();\n" +
				"}\n" +
				"class A extends X implements I {\n" +
				"    public A foo() {\n" +
				"        System.out.print(\"Added bridge method\");\n" +
				"        return this;\n" +
				"    }\n" +
				"}\n"
			},
			"Added bridge method + Added bridge method"
		);
	}

	public void test011() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> void foo(T t) {} }\n" +
				"interface I { <T> void foo(T t); }\n",
				"X.java",
				"abstract class X1 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X1 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"The inherited method A.foo(T) cannot hide the public abstract method in I\n" + 
			"----------\n"
			// <T>foo(T) in A cannot implement <T>foo(T) in I; attempting to assign weaker access privileges; was public
		);
	}
	public void test011a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> void foo(T t) {} }\n" +
				"interface I { <T> void foo(T t); }\n",
				"X.java",
				"abstract class X2 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	abstract class X2 extends A implements I {}\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(T) of type A has the same erasure as foo(T) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T,S>foo(T) in A and <T>foo(T) in I have the same erasure, yet neither overrides the other
		);
	}
	public void test011b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> void foo(T t) {} }\n" +
				"interface I { <T, S> void foo(T t); }\n",
				"X.java",
				"abstract class X3 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	abstract class X3 extends A implements I {}\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(T) of type A has the same erasure as foo(T) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(T) in A and <T,S>foo(T) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test012() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(T s) {} }\n" +
				"class Y1 extends A { @Override void foo(Object s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\n" + 
			"	class Y1 extends A { @Override void foo(Object s) {} }\n" + 
			"	                                    ^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
			// foo(java.lang.Object) in Y1 cannot override <T>foo(T) in A; attempting to assign weaker access privileges; was public
		);
	}		
	public void test012a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(T[] s) {} }\n" +
				"class Y2 extends A { @Override void foo(Object[] s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\n" + 
			"	class Y2 extends A { @Override void foo(Object[] s) {} }\n" + 
			"	                                    ^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
			// foo(java.lang.Object[]) in Y2 cannot override <T>foo(T[]) in A; attempting to assign weaker access privileges; was public
		);
	}
	public void test012b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public void foo(Class<Object> s) {} }\n" +
				"class Y3 extends A { @Override void foo(Class<Object> s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	class Y3 extends A { @Override void foo(Class<Object> s) {} }\r\n" + 
			"	                                    ^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
			// foo(java.lang.Class<java.lang.Object>) in Y3 cannot override foo(java.lang.Class<java.lang.Object>) in A; attempting to assign weaker access privileges; was public
		);
	}

	public void test013() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runConformTest(
			new String[] {
				"A.java",
				"class A { public <T> void foo(Class<T> s) {} }\n" +
				"interface I { <T> void foo(Class<T> s); }\n",
				"X.java",
				"abstract class X0 extends A implements I {}\n"
			},
			""
		);
	}
	public void test013a() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> void foo(Class<T> s) {} }\n" +
				"interface I { <T> void foo(Class<T> s); }\n",
				"X.java",
				"abstract class X1 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X1 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T,S>foo(java.lang.Class<T>) in A and <T>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
	}
	public void test013b() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> void foo(Class<T> s) {} }\n" +
				"interface I { <T, S> void foo(Class<T> s); }\n",
				"X.java",
				"abstract class X2 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X2 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(java.lang.Class<T>) in A and <T,S>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
	}
	public void test013c() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S> S foo(Class<T> s) { return null; } }\n" +
				"interface I { <T> Object foo(Class<T> s); }\n",
				"X.java",
				"abstract class X3 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X3 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T,S>foo(java.lang.Class<T>) in A and <T>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
	}
	public void test013d() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T> Object foo(Class<T> s) { return null; } }\n" +
				"interface I { <T, S> S foo(Class<T> s); }\n",
				"X.java",
				"abstract class X4 extends A implements I {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	abstract class X4 extends A implements I {}\r\n" + 
			"	               ^^\n" + 
			"Name clash: The method foo(Class<T>) of type A has the same erasure as foo(Class<T>) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(java.lang.Class<T>) in A and <T,S>foo(java.lang.Class<T>) in I have the same erasure, yet neither overrides the other
		);
	}
	public void test013e() {
		// javac 1.5.0 will only issue 1 name clash per compile... doesn't matter how many source files are involved
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { public <T, S> void foo(Class<T> s) {} }\n" +
				"interface I { <T> void foo(Class<T> s); }\n",

				"X.java",
				"class X5 extends A implements I { public <T> void foo(Class<T> s) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	class X5 extends A implements I { public <T> void foo(Class<T> s) {} }\r\n" + 
			"	                                                  ^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(Class<T>) of type X5 has the same erasure as foo(Class<T>) of type A but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(java.lang.Class<T>) in X5 and <T,S>foo(java.lang.Class<T>) in A have the same erasure, yet neither overrides the other
		);
	}

	public void test014() { // name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A a) {} }\n" + 
				"class Y extends X { void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
	}
	public void test014a() { // name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A[] a) {} }\n" + 
				"class Y extends X { void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
	}
	public void test014b() { // name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A<String>[] a) {} }\n" + 
				"class Y extends X { void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
	}
	public void test014c() { // name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"class X { void foo(A<String> a) {} }\n" + 
				"class Y extends X { void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
	}
	public void test014d() { // name clash tests
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X { void foo(A a) {} }\n" + 
				"class Y extends X { void foo(A<String> a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	class X { void foo(A a) {} }\n" + 
			"	                   ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X { void foo(A<String> a) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type X but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in Y and foo(A) in X have the same erasure, yet neither overrides the other
		);
	}
	public void test014e() { // name clash tests
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X { void foo(A[] a) {} }\n" + 
				"class Y extends X { void foo(A<String>[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	class X { void foo(A[] a) {} }\n" + 
			"	                   ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X { void foo(A<String>[] a) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>[]) of type Y has the same erasure as foo(A[]) of type X but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>[]) in Y and foo(A[]) in X have the same erasure, yet neither overrides the other
		);
	}

	public void test015() { // more name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A a); }\n" + 
				"class Y { public void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
	}
	public void test015a() { // more name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A[] a); }\n" + 
				"class Y { public void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
	}
	public void test015b() { // more name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A<String>[] a); }\n" + 
				"class Y { public void foo(A[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
	}
	public void test015c() { // more name clash tests
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A<String> a); }\n" + 
				"class Y { public void foo(A a) {} }\n" + 
				"class A<T> {}\n" 
			},
			""
		);
	}
	public void test015d() { // more name clash tests
		this.runNegativeTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A a); }\n" + 
				"class Y { public void foo(A<String> a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	abstract class X extends Y implements I { }\n" + 
			"	               ^\n" + 
			"Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 2)\n" + 
			"	interface I { void foo(A a); }\n" + 
			"	                       ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in Y and foo(A) in I have the same erasure, yet neither overrides the other
		);
	}
	public void test015e() { // more name clash tests
		this.runNegativeTest(
			new String[] {
				"X.java",
				"abstract class X extends Y implements I { }\n" + 
				"interface I { void foo(A[] a); }\n" + 
				"class Y { public void foo(A<String>[] a) {} }\n" + 
				"class A<T> {}\n" 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	abstract class X extends Y implements I { }\n" + 
			"	               ^\n" + 
			"Name clash: The method foo(A<String>[]) of type Y has the same erasure as foo(A[]) of type I but does not override it\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 2)\n" + 
			"	interface I { void foo(A[] a); }\n" + 
			"	                       ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>[]) in Y and foo(A[]) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test016() { // 73971
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<E extends A> void m(E e) { System.out.print(\"A=\"+e.getClass()); }\n" + 
				"	<E extends B> void m(E e) { System.out.print(\"B=\"+e.getClass()); }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new X().m(new A());\n" +
				"		new X().m(new B());\n" + 
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n"
			},
			"A=class AB=class B"
		);
	}
	public void test016b() { // 73971
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	static <E extends A> void m(E e) { System.out.print(\"A=\"+e.getClass()); }\n" + 
				"	static <E extends B> void m(E e) { System.out.print(\"B=\"+e.getClass()); }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		m(new A());\n" +
				"		m(new B());\n" + 
				"	}\n" + 
				"}\n" +
				"class A {}\n" + 
				"class B extends A {}\n"
			},
			"A=class AB=class B"
		);
	}

	public void test017() { // 77785
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X<T> {}\n" + 
				"class Y { void test(X<? extends Number> a) {} }\n" + 
				"class Z extends Y { void test(X<Number> a) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	class Z extends Y { void test(X<Number> a) {} }\r\n" + 
			"	                         ^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(X<Number>) of type Z has the same erasure as test(X<? extends Number>) of type Y but does not override it\n" + 
			"----------\n"
			// name clash: test(X<java.lang.Number>) in Z and test(X<? extends java.lang.Number>) in Y have the same erasure, yet neither overrides the other
		);
	}
	public void test017a() { // 77785
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X<T> {}\n" + 
				"class Y { void test(X<Number> a) {} }\n" + 
				"class Z extends Y { void test(X<? extends Number> a) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	class Z extends Y { void test(X<? extends Number> a) {} }\n" + 
			"	                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(X<? extends Number>) of type Z has the same erasure as test(X<Number>) of type Y but does not override it\n" + 
			"----------\n"
			// name clash: test(X<? extends java.lang.Number>) in Z and test(X<java.lang.Number>) in Y have the same erasure, yet neither overrides the other
		);
	}

	public void test018() { // 77861
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X implements Comparable<X> {\n" + 
				"	public int compareTo(Object o) { return 0; }\n" + 
				"	public int compareTo(X o) { return 1; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public int compareTo(Object o) { return 0; }\n" + 
			"	           ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method compareTo(Object) of type X has the same erasure as compareTo(T) of type Comparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: compareTo(java.lang.Object) in X and compareTo(T) in java.lang.Comparable<X> have the same erasure, yet neither overrides the other
		);
	}

	public void test019() { // 78140
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A {\n" + 
				"	<T> T get() { return null; } \n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	<T> T get() { return null; } \n" + 
				"}\n"
			},
			""
		);
	}

	public void test020() { // 78232
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		AbstractBase ab = new AbstractBase();\n" + 
				"		Derived d = new Derived();\n" + 
				"		AbstractBase ab2 = new Derived();\n" + 
				"		Visitor<String, String> v = new MyVisitor();\n" + 
				"		System.out.print(ab.accept(v, ab.getClass().getName()));\n" + 
				"		System.out.print('+');\n" + 
				"		System.out.print(d.accept(v, d.getClass().getName()));\n" + 
				"		System.out.print('+');\n" + 
				"		System.out.print(ab2.accept(v, ab2.getClass().getName()));\n" + 
				"	}\n" + 
				"	static class MyVisitor implements Visitor<String, String> {\n" + 
				"		public String visitBase(AbstractBase ab, String obj) { return \"Visited base: \" + obj; }\n" + 
				"		public String visitDerived(Derived d, String obj) { return \"Visited derived: \" + obj; }\n" + 
				"	}\n" + 
				"}\n" + 
				"interface Visitor<R, T> {\n" + 
				"	R visitBase(AbstractBase ab, T obj);\n" + 
				"	R visitDerived(Derived d, T obj);\n" + 
				"}\n" + 
				"interface Visitable {\n" + 
				"	<R, T> R accept(Visitor<R, T> v, T obj);\n" + 
				"}\n" + 
				"class AbstractBase implements Visitable {\n" + 
				"	public <R, T> R accept(Visitor<R, T> v, T obj) { return v.visitBase(this, obj); }\n" + 
				"}\n" + 
				"class Derived extends AbstractBase implements Visitable {\n" + 
				"	public <R, T> R accept(Visitor<R, T> v, T obj) { return v.visitDerived(this, obj); }\n" + 
				"}\n"
			},
			"Visited base: AbstractBase+Visited derived: Derived+Visited derived: Derived"
		);
	}

	public void test021() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"public class A {\n" + 
				"	public void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n",
				"B.java",
				"class B extends A {\n" + 
				"	@Override void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 2)\r\n" + 
			"	@Override void foo(java.util.Map<String, Class<?>> m) { } \r\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n"
		);
		// now save A & pick it up as a binary type
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A {\n" + 
				"	public void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n"
			},
			""
		);
		this.runNegativeTest(
			new String[] {
				"B.java",
				"class B extends A {\n" + 
				"	@Override void foo(java.util.Map<String, Class<?>> m) { } \n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 2)\r\n" + 
			"	@Override void foo(java.util.Map<String, Class<?>> m) { } \r\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from A\n" + 
			"----------\n",
			null,
			false,
			null
		);
	}

	public void test022() { // 77562
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.util.*;\n" + 
				"class A { List getList() { return null; } }\n" + 
				"class B extends A { @Override List<String> getList() { return null; } }\n"
			},
			""
		);
	}
	public void test022a() { // 77562
		this.runNegativeTest(
			new String[] {
				"A.java",
				"import java.util.*;\n" + 
				"class A { List<String> getList() { return null; } }\n" + 
				"class B extends A { @Override List getList() { return null; } }\n"
			},
			"----------\n" + 
			"1. WARNING in A.java (at line 3)\n" + 
			"	class B extends A { @Override List getList() { return null; } }\n" + 
			"	                              ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in A.java (at line 3)\n" + 
			"	class B extends A { @Override List getList() { return null; } }\n" + 
			"	                              ^^^^\n" + 
			"Type safety: The return type List for getList() from the type B needs unchecked conversion to conform to List<String> from the type A\n" + 
			"----------\n"
			// unchecked warning on B.getList()
		);
	}

	public void test023() { // 80739
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A<T> {\n" + 
				"	void foo(T t) {}\n" + 
				"	void foo(String i) {}\n" + 
				"}\n" + 
				"class B extends A<String> {}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 5)\r\n" + 
			"	class B extends A<String> {}\r\n" + 
			"	      ^\n" + 
			"Duplicate methods named foo with the parameters (String) and (T) are defined by the type A<String>\n" + 
			"----------\n"
			// methods foo(T) from A<java.lang.String> and foo(java.lang.String) from A<java.lang.String> are inherited with the same signature
		);
	}

	public void test024() { // 80626
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	public void m(Object e) {}\n" + 
				"}\n"
			},
			""
			// no complaint
		);
	}
	public void test024a() { // 80626
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {\n" + 
				"	public void m(Object e) {}\n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 5)\r\n" + 
			"	public <E extends Object> void m(E e) {}\r\n" + 
			"	                               ^^^^^^\n" + 
			"Name clash: The method m(E) of type B has the same erasure as m(Object) of type A but does not override it\n" + 
			"----------\n"
			// name clash: <E>m(E) in B and m(java.lang.Object) in A have the same erasure, yet neither overrides the other
		);
	}
	public void test024b() { // 80626
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n" + 
				"class B extends A {\n" + 
				"	@Override public void m(Object e) {}\n" + 
				"}\n" + 
				"class C extends B {\n" + 
				"	public <E extends Object> void m(E e) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 8)\r\n" + 
			"	public <E extends Object> void m(E e) {}\r\n" + 
			"	                               ^^^^^^\n" + 
			"Name clash: The method m(E) of type C has the same erasure as m(Object) of type B but does not override it\n" + 
			"----------\n"
			// name clash: <E>m(E) in C and m(java.lang.Object) in B have the same erasure, yet neither overrides the other
		);
	}

	public void test025() { // 81618
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new B().test();\n" + 
				"	}\n" + 
				"}\n" +
				"class A {\n" + 
				"	<T extends Number> T test() { return null; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	@Override Integer test() { return 1; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 10)\n" + 
			"	@Override Integer test() { return 1; }\n" + 
			"	          ^^^^^^^\n" + 
			"Type safety: The return type Integer for test() from the type B needs unchecked conversion to conform to T from the type A\n" + 
			"----------\n"
			// warning: test() in B overrides <T>test() in A; return type requires unchecked conversion
		);
	}
	public void test025a() { // 81618
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new B().test();\n" + 
				"	}\n" + 
				"}\n" +
				"class A {\n" + 
				"	<T extends Number> T[] test() { return null; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	@Override Integer[] test() { return new Integer[] {2}; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 10)\n" + 
			"	@Override Integer[] test() { return new Integer[] {2}; }\n" + 
			"	          ^^^^^^^^^\n" + 
			"Type safety: The return type Integer[] for test() from the type B needs unchecked conversion to conform to T[] from the type A\n" + 
			"----------\n"
			// warning: test() in B overrides <T>test() in A; return type requires unchecked conversion
		);
	}
	public void test025b() { // 81618
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new B().<Integer>test(new Integer(1)));\n" + 
				"	}\n" + 
				"}\n" +
				"class A {\n" + 
				"	<T> T test(T t) { return null; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	@Override <T> T test(T t) { return t; }\n" + 
				"}\n"
			},
			"1"
		);
	}
	public void test025c() { // 81618
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(new B().<Number>test(1));\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T> {\n" + 
				"	<U> T test(U u) { return null; }\n" + 
				"}\n" +
				"class B extends A<Integer> {\n" + 
				"	@Override <U> Integer test(U u) { return 1; }\n" + 
				"}\n"
			},
			"1"
		);
	}
	public void test025d() { // 81618
		this.runConformTest(
			new String[] {
				"A.java",
				"import java.util.concurrent.Callable;\n" + 
				"public class A {\n" + 
				"	public static void main(String[] args) throws Exception {\n" + 
				"		Callable<Integer> integerCallable = new Callable<Integer>() {\n" + 
				"			public Integer call() { return new Integer(1); }\n" + 
				"		};\n" + 
				"		System.out.println(integerCallable.call());\n" + 
				"	}\n" + 
				"}\n"
			},
			"1"
		);
	}
	public void test025e() { // 81618
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"interface X<T extends X> { T x(); }\n" +
				"abstract class Y<S extends X> implements X<S> { public abstract S x(); }\n" +
				"abstract class Z implements X { public abstract X x(); }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	interface X<T extends X> { T x(); }\n" + 
			"	                      ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 2)\n" + 
			"	abstract class Y<S extends X> implements X<S> { public abstract S x(); }\n" + 
			"	                           ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 3)\n" + 
			"	abstract class Z implements X { public abstract X x(); }\n" + 
			"	                            ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 3)\n" + 
			"	abstract class Z implements X { public abstract X x(); }\n" + 
			"	                                                ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
		);
	}		
	public void test025f() { // 81618
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"interface X<T extends X> { T[] x(); }\n" +
				"abstract class Y<S extends X> implements X<S> { public abstract S[] x(); }\n" +
				"abstract class Z implements X { public abstract X[] x(); }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	interface X<T extends X> { T[] x(); }\n" + 
			"	                      ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 2)\n" + 
			"	abstract class Y<S extends X> implements X<S> { public abstract S[] x(); }\n" + 
			"	                           ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 3)\n" + 
			"	abstract class Z implements X { public abstract X[] x(); }\n" + 
			"	                            ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 3)\n" + 
			"	abstract class Z implements X { public abstract X[] x(); }\n" + 
			"	                                                ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
		);
	}

	public void test026() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.print(\n" + 
				"			new B().test().getClass() + \" & \"\n" + 
				"			+ new C().test().getClass() + \" & \"\n" + 
				"			+ new D().test().getClass());\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T extends Number> {\n" + 
				"	A<T> test() { return this; }\n" + 
				"}\n" +
				"class B extends A {\n" + 
				"	A test() { return super.test(); }\n" + 
				"}\n" +
				"class C extends A<Integer> {\n" + 
				"	A<Integer> test() { return super.test(); }\n" + 
				"}\n" +
				"class D<U, V extends Number> extends A<V> {\n" + 
				"	A<V> test() { return super.test(); }\n" + 
				"}\n"
			},
			"class B & class C & class D"
		);
	}
	public void test026a() {
		this.runConformTest(
			new String[] {
				"A.java",
				"public abstract class A<E> {\n" + 
				"	public abstract A<E> test();\n" + 
				"}\n" +
				"class H<K,V> {\n" + 
				"	class M extends A<K> {\n" + 
				"		public A<K> test() { return null; }\n" + 
				"	}\n" +
				"}\n"
			},
			""
		);
	}		
	public void test026b() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends java.util.AbstractMap {\n" + 
				"	public java.util.Set entrySet() { return null; }\n" + 
				"}\n"
			},
			""
		);
	}		
	public void test026c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.print(new C().test().getClass());\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T extends Number> {\n" + 
				"	A<T> test() { return this; }\n" + 
				"}\n" +
				"class C extends A<Integer> {\n" + 
				"	@Override A test() { return super.test(); }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 10)\n" + 
			"	@Override A test() { return super.test(); }\n" + 
			"	          ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 10)\n" + 
			"	@Override A test() { return super.test(); }\n" + 
			"	          ^\n" + 
			"Type safety: The return type A for test() from the type C needs unchecked conversion to conform to A<T> from the type A<T>\n" + 
			"----------\n"
			// warning: test() in C overrides test() in A; return type requires unchecked conversion
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82102
	public void test027() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X { <T> void test() {} }\n" + 
				"class Y extends X { void test() {} }\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82102
	public void test027a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void test() {} }\n" + 
				"class Y extends X { <T> void test() {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X { <T> void test() {} }\n" + 
			"	                             ^^^^^^\n" + 
			"Name clash: The method test() of type Y has the same erasure as test() of type X but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo() in Y and foo() in X have the same erasure, yet neither overrides the other
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82102
	public void test027b() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> { void test(T o) {} }\n" + 
				"class Y<T> extends X<T> { void test(Object o) {} }\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82102
	public void test027c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> { void test(T o, T t) {} }\n" + 
				"class Y<T> extends X<T> { void test(Object o, T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	class Y<T> extends X<T> { void test(Object o, T t) {} }\r\n" + 
			"	                               ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Object, T) of type Y<T> has the same erasure as test(T, T) of type X<T> but does not override it\n" + 
			"----------\n"
			// name clash: test(java.lang.Object,T) in Y<T> and test(T,T) in X<T> have the same erasure, yet neither overrides the other
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82102
	public void test027d() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void test() {\n" + 
				"		Pair<Double, Integer> p = new InvertedPair<Integer, Double>();\n" + 
				"		p.setA(new Double(1.1));\n" + 
				"	}\n" + 
				"}\n" +
				"class Pair<A, B> {\n" + 
				"	public void setA(A a) {}\n" + 
				"}\n" +
				"class InvertedPair<A, B> extends Pair<B, A> {\n" + 
				"	public void setA(A a) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	public void setA(A a) {}\n" + 
			"	            ^^^^^^^^^\n" + 
			"Name clash: The method setA(A) of type InvertedPair<A,B> has the same erasure as setA(A) of type Pair<A,B> but does not override it\n" + 
			"----------\n"
			// name clash: setA(A) in InvertedPair<A,B> and setA(A) in Pair<B,A> have the same erasure, yet neither overrides the other
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81727
	public void test028() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements I<X>{\n" + 
				"	public X foo() { return null; }\n" + 
				"}\n" +
				"interface I<T extends I> { T foo(); }\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81568
	public void test029() {
		this.runConformTest(
			new String[] {
				"I.java",
				"public interface I {\n" + 
				"	public I clone();\n" + 
				"}\n" +
				"interface J extends I {}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81535
	public void test030() {
		java.util.Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);	

		this.runConformTest(
			new String[] {
				"X.java",
				"import java.io.OutputStreamWriter;\n" + 
				"import java.io.PrintWriter;\n" + 
				"public class X extends PrintWriter implements Runnable {\n" + 
				"	public X(OutputStreamWriter out, boolean flag) { super(out, flag); }\n" +
				"	public void run() {}\n" +
				"}\n"
			},
			"",
			null, // use default class-path
			false, // do not flush previous output dir content
			null, // no special vm args
			options,
			null
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80743
	public void test031() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X { long hashCode(); }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	interface X { long hashCode(); }\r\n" + 
			"	              ^^^^\n" + 
			"The return type is incompatible with Object.hashCode()\n" + 
			"----------\n"
			// hashCode() in X cannot override hashCode() in java.lang.Object; attempting to use incompatible return type
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80736 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=113273
	public void test032() {
		// NOTE: javac only reports these errors when the problem type follows the bounds
		// if the type X is defined first, then no errors are reported
		this.runConformTest(
			new String[] {
				"X.java",
				"interface I { Integer foo(); }\n" +
				"interface J { Integer foo(); }\n" +
				"public class X<T extends I&J> implements I {\n" +
				"	public Integer foo() { return null; }\n" +
				"}"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80736 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=113273
	public void test032a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I { Float foo(); }\n" +
				"interface J { Integer foo(); }\n" +
				"public class X<T extends I&J> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	public class X<T extends I&J> {}\r\n" + 
			"	               ^\n" + 
			"The return type is incompatible with J.foo(), I.foo()\n" + 
			"----------\n"
			// types J and I are incompatible; both define foo(), but with unrelated return types
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80736 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=113273
	public void test032b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I { String foo(); }\n" +
				"class A { public Object foo() { return null; } }" +
				"public class X<T extends A&I> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	class A { public Object foo() { return null; } }public class X<T extends A&I> {}\r\n" + 
			"	                                                               ^\n" + 
			"The return type is incompatible with I.foo(), A.foo()\n" + 
			"----------\n"
			// foo() in A cannot implement foo() in I; attempting to use incompatible return type
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80745
	public void test033() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface I { Number foo(); }\n" +
				"interface J { Integer foo(); }\n" +
				"public class X implements I, J {\n" +
				"	public Integer foo() {return 1;}\n" +
				"	public static void main(String argv[]) {\n" +
				"		I i = null;\n" +
				"		J j = null;\n" +
				"		System.out.print(i instanceof J);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(j instanceof I);\n" +
				"	}\n" +
				"}\n"
			},
			"false=false"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80745
	public void test033a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface I { Number foo(A a); }\n" +
				"interface J<T> { Integer foo(A<T> a); }\n" +
				"class A<T>{}\n" +
				"public class X implements I, J {\n" +
				"	public Integer foo(A a) {return 1;}\n" +
				"	public static void main(String argv[]) {\n" +
				"		I i = null;\n" +
				"		J j = null;\n" +
				"		System.out.print(i instanceof J);\n" +
				"		System.out.print('=');\n" +
				"		System.out.print(j instanceof I);\n" +
				"	}\n" +
				"}\n"
			},
			"false=false"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034() {
		this.runConformTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable<E>> { void test(E element); }\n" +
				"class A implements I<Integer> { public void test(Integer i) {} }\n" +
				"public class B extends A { public void test(String i) {} }\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034a() {
		this.runConformTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable> { void test(E element); }\n" +
				"class A { public void test(Integer i) {} }\n" +
				"public class B extends A implements I<Integer> {}\n" +
				"class C extends B { public void test(Object i) {} }\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034b() {
		this.runNegativeTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable> { void test(E element); }\n" +
				"class A { public void test(Integer i) {} }\n" +
				"public class B extends A implements I<Integer> { public void test(Comparable i) {} }\n"
			},
			"----------\n" + 
			"1. WARNING in B.java (at line 1)\n" + 
			"	interface I<E extends Comparable> { void test(E element); }\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in B.java (at line 3)\n" + 
			"	public class B extends A implements I<Integer> { public void test(Comparable i) {} }\n" + 
			"	                                                             ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type I<E> but does not override it\n" + 
			"----------\n" + 
			"3. WARNING in B.java (at line 3)\n" + 
			"	public class B extends A implements I<Integer> { public void test(Comparable i) {} }\n" + 
			"	                                                                  ^^^^^^^^^^\n" + 
			"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" + 
			"----------\n"
			// name clash: test(java.lang.Comparable) in B and test(E) in I<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034c() {
		this.runNegativeTest(
			new String[] {
				"B.java",
				"interface I<E extends Comparable<E>> { void test(E element); }\n" +
				"class A implements I<Integer> { public void test(Integer i) {} }\n" +
				"public class B extends A { public void test(Comparable i) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in B.java (at line 3)\n" + 
			"	public class B extends A { public void test(Comparable i) {} }\n" + 
			"	                                       ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type I<E> but does not override it\n" + 
			"----------\n" + 
			"2. WARNING in B.java (at line 3)\n" + 
			"	public class B extends A { public void test(Comparable i) {} }\n" + 
			"	                                            ^^^^^^^^^^\n" + 
			"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" + 
			"----------\n"
			// name clash: test(java.lang.Comparable) in B and test(E) in I<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81332
	public void test034d() {
		this.runNegativeTest(
			new String[] {
				"B.java",
				"abstract class AA<E extends Comparable> { abstract void test(E element); }\n" +
				"class A extends AA<Integer> { @Override public void test(Integer i) {} }\n" +
				"public class B extends A { public void test(Comparable i) {} }\n"
			},
			"----------\n" + 
			"1. WARNING in B.java (at line 1)\n" + 
			"	abstract class AA<E extends Comparable> { abstract void test(E element); }\n" + 
			"	                            ^^^^^^^^^^\n" + 
			"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in B.java (at line 3)\n" + 
			"	public class B extends A { public void test(Comparable i) {} }\n" + 
			"	                                       ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method test(Comparable) of type B has the same erasure as test(E) of type AA<E> but does not override it\n" + 
			"----------\n" + 
			"3. WARNING in B.java (at line 3)\n" + 
			"	public class B extends A { public void test(Comparable i) {} }\n" + 
			"	                                            ^^^^^^^^^^\n" + 
			"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" + 
			"----------\n"
			// name clash: test(java.lang.Comparable) in B and test(E) in AA<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80626
	public void test035() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"interface I<U>{ int compareTo(U o); }\n" +
				"abstract class F<T extends F<T>> implements I<T>{ public final int compareTo(T o) { return 0; } }\n" +
				"public class E extends F<E> { public int compareTo(Object o) { return 0; } }\n"
			},
			"----------\n" + 
			"1. ERROR in E.java (at line 3)\n" + 
			"	public class E extends F<E> { public int compareTo(Object o) { return 0; } }\n" + 
			"	                                         ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method compareTo(Object) of type E has the same erasure as compareTo(U) of type I<U> but does not override it\n" + 
			"----------\n"
			// name clash: compareTo(java.lang.Object) in E and compareTo(U) in I<E> have the same erasure, yet neither overrides the other
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80626
	public void test035a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" +
				"	;\n" +
				"	public int compareTo(Object o) { return 0; }\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	public int compareTo(Object o) { return 0; }\n" + 
			"	           ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method compareTo(Object) of type X has the same erasure as compareTo(T) of type Comparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: compareTo(java.lang.Object) in X and compareTo(T) in java.lang.Comparable<X> have the same erasure, yet neither overrides the other
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036() { // 2 interface cases
		// no bridge methods are created in these conform cases so no name clashes can occur
		this.runConformTest(
			new String[] {
				"X.java",
				"class X implements Equivalent, EqualityComparable {\n" +
				"	public boolean equalTo(Object other) { return true; }\n" +
				"}\n" +
				"abstract class Y implements Equivalent, EqualityComparable {}\n" +
				"class Z extends Y {\n" +
				"	public boolean equalTo(Object other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036a() { // 2 interface cases
		this.runConformTest(
			new String[] {
				"X.java",
				"class X implements Equivalent, EqualityComparable {\n" +
				"	public boolean equalTo(Comparable other) { return true; }\n" +
				"	public boolean equalTo(Number other) { return true; }\n" +
				"}\n" +
				"abstract class Y implements Equivalent, EqualityComparable {}\n" +
				"class Z extends Y {\n" +
				"	public boolean equalTo(Comparable other) { return true; }\n" +
				"	public boolean equalTo(Number other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T extends Comparable> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T extends Number> { boolean equalTo(T other); }\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036b() { // 2 interface cases
		this.runConformTest(
			new String[] {
				"X.java",
				"class X<S> implements Equivalent<S>, EqualityComparable<S> {\n" +
				"	public boolean equalTo(S other) { return true; }\n" +
				"}\n" +
				"abstract class Y<S> implements Equivalent<S>, EqualityComparable<S> {}\n" +
				"class Z<U> extends Y<U> {\n" +
				"	public boolean equalTo(U other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036c() { // 2 interface cases
		this.runConformTest(
			new String[] {
				"X.java",
				"class X<T extends Comparable, S extends Number> implements Equivalent<T>, EqualityComparable<S> {\n" +
				"	public boolean equalTo(T other) { return true; }\n" +
				"	public boolean equalTo(S other) { return true; }\n" +
				"}\n" +
				"abstract class Y<T extends Comparable, S extends Number> implements Equivalent<T>, EqualityComparable<S> {}\n" +
				"class Z<U extends Comparable, V extends Number> extends Y<U, V> {\n" +
				"	public boolean equalTo(U other) { return true; }\n" +
				"	public boolean equalTo(V other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T extends Comparable> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<S extends Number> { boolean equalTo(S other); }\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036d() { // 2 interface cases
		// in these cases, bridge methods are needed once abstract/concrete methods are defiined (either in the abstract class or a concrete subclass)
		this.runConformTest(
			new String[] {
				"Y.java",
				"abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" +
				"	public abstract boolean equalTo(Number other);\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			""
			// no bridge methods are created here since Y does not define an equalTo(?) method which equals an inherited equalTo method
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036e() { // 2 interface cases
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" +
				"	public abstract boolean equalTo(Object other);\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 2)\n" + 
			"	public abstract boolean equalTo(Object other);\n" + 
			"	                        ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type Equivalent<T> but does not override it\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 2)\n" + 
			"	public abstract boolean equalTo(Object other);\n" + 
			"	                        ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type EqualityComparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: equalTo(java.lang.Object) in Y and equalTo(T) in Equivalent<java.lang.String> have the same erasure, yet neither overrides the other
		);
	}		
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036f() { // 2 interface cases
		// NOTE: javac has a bug, reverse the implemented interfaces & the name clash goes away
		// but eventually when a concrete subclass must define the remaining method, the error shows up
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" +
				"	public abstract boolean equalTo(String other);\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" + 
			"	               ^\n" + 
			"Name clash: The method equalTo(T) of type Equivalent<T> has the same erasure as equalTo(T) of type EqualityComparable<T> but does not override it\n" + 
			"----------\n"
			// name clash: equalTo(T) in Equivalent<java.lang.String> and equalTo(T) in EqualityComparable<java.lang.Integer> have the same erasure, yet neither overrides the other
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
	public void test036g() { // 2 interface cases
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"abstract class Y implements EqualityComparable<Integer>, Equivalent<String> {\n" +
				"	public boolean equalTo(Integer other) { return true; }\n" +
				"}\n" +
				"interface Equivalent<T> { boolean equalTo(T other); }\n" +
				"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	abstract class Y implements EqualityComparable<Integer>, Equivalent<String> {\n" + 
			"	               ^\n" + 
			"Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(T) of type Equivalent<T> but does not override it\n" + 
			"----------\n"
			// name clash: equalTo(T) in EqualityComparable<java.lang.Integer> and equalTo(T) in Equivalent<java.lang.String> have the same erasure, yet neither overrides the other
		);
	}

	public void test037() { // test inheritance scenarios
		this.runConformTest(
			new String[] {
				"X.java",
				"public abstract class X implements I, J { }\n" +
				"abstract class Y implements J, I { }\n" +
				"abstract class Z implements K { }\n" +

				"class YYY implements J, I { public void foo(A a) {} }\n" +
				"class XXX implements I, J { public void foo(A a) {} }\n" +
				"class ZZZ implements K { public void foo(A a) {} }\n" +

				"interface I { void foo(A a); }\n" +
				"interface J { void foo(A<String> a); }\n" +
				"interface K extends I { void foo(A<String> a); }\n" +
				"class A<T> {}"
			},
			""
		);
	}
	public void test037a() { // test inheritance scenarios
		this.runNegativeTest(
			new String[] {
				"XX.java",
				"public abstract class XX implements I, J { public abstract void foo(A<String> a); }\n" +
				"interface I { void foo(A a); }\n" +
				"interface J { void foo(A<String> a); }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in XX.java (at line 1)\n" + 
			"	public abstract class XX implements I, J { public abstract void foo(A<String> a); }\n" + 
			"	                                                                ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type XX has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n" + 
			"2. WARNING in XX.java (at line 2)\n" + 
			"	interface I { void foo(A a); }\n" + 
			"	                       ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in XX and foo(A) in I have the same erasure, yet neither overrides the other
		);
	}
	public void test037b() { // test inheritance scenarios
		this.runNegativeTest(
			new String[] {
				"XX.java",
				"public class XX implements I, J { public void foo(A<String> a) {} }\n" +
				"class YY implements J, I { public void foo(A<String> a) {} }\n" +
				"class ZZ implements K { public void foo(A<String> a) {} }\n" +

				"interface I { void foo(A a); }\n" +
				"interface J { void foo(A<String> a); }\n" +
				"interface K extends I { void foo(A<String> a); }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in XX.java (at line 1)\n" + 
			"	public class XX implements I, J { public void foo(A<String> a) {} }\n" + 
			"	             ^^\n" + 
			"The type XX must implement the inherited abstract method I.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in XX.java (at line 1)\n" + 
			"	public class XX implements I, J { public void foo(A<String> a) {} }\n" + 
			"	                                              ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type XX has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n" + 
			"3. ERROR in XX.java (at line 2)\n" + 
			"	class YY implements J, I { public void foo(A<String> a) {} }\n" + 
			"	      ^^\n" + 
			"The type YY must implement the inherited abstract method I.foo(A)\n" + 
			"----------\n" + 
			"4. ERROR in XX.java (at line 2)\n" + 
			"	class YY implements J, I { public void foo(A<String> a) {} }\n" + 
			"	                                       ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type YY has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n" + 
			"5. ERROR in XX.java (at line 3)\n" + 
			"	class ZZ implements K { public void foo(A<String> a) {} }\n" + 
			"	      ^^\n" + 
			"The type ZZ must implement the inherited abstract method I.foo(A)\n" + 
			"----------\n" + 
			"6. ERROR in XX.java (at line 3)\n" + 
			"	class ZZ implements K { public void foo(A<String> a) {} }\n" + 
			"	                                    ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type ZZ has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n" + 
			"7. WARNING in XX.java (at line 4)\n" + 
			"	interface I { void foo(A a); }\n" + 
			"	                       ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n"
			// XX/YY/ZZ is not abstract and does not override abstract method foo(A) in I
		);
	}
	public void test037c() { // test inheritance scenarios
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X extends Y implements I { }\n" +
				"interface I { void foo(A a); }\n" +
				"class Y { void foo(A<String> a) {} }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public abstract class X extends Y implements I { }\n" + 
			"	                      ^\n" + 
			"Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 2)\n" + 
			"	interface I { void foo(A a); }\n" + 
			"	                       ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) in Y and foo(A) in I have the same erasure, yet neither overrides the other
		);
	}
	public void test037d() { // test inheritance scenarios
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X extends Y implements I { }\n" +
				"interface I { void foo(A<String> a); }\n" +
				"class Y { void foo(A a) {} }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public abstract class X extends Y implements I { }\n" + 
			"	                      ^\n" + 
			"The inherited method Y.foo(A) cannot hide the public abstract method in I\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 3)\n" + 
			"	class Y { void foo(A a) {} }\n" + 
			"	                   ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n"
			// foo(A) in Y cannot implement foo(A<java.lang.String>) in I; attempting to assign weaker access privileges; was public
		);
	}
	public void test037e() { // test inheritance scenarios
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X extends Y implements I { }\n" +
				"interface I { <T, S> void foo(T t); }\n" +
				"class Y { <T> void foo(T t) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public abstract class X extends Y implements I { }\r\n" + 
			"	                      ^\n" + 
			"Name clash: The method foo(T) of type Y has the same erasure as foo(T) of type I but does not override it\n" + 
			"----------\n"
			// name clash: <T>foo(T) in Y and <T,S>foo(T) in I have the same erasure, yet neither overrides the other
		);
	}

	public void test038() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends H<Object> { void foo(A<?> a) { super.foo(a); } }\n" +
				"class H<T extends Object> { void foo(A<? extends T> a) {} }\n" +
				"class A<T> {}"
			},
			""
		);
	}
	public void test038a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends H<Number> { void foo(A<?> a) {} }\n" +
				"class H<T extends Number> { void foo(A<? extends T> a) {} }\n" +
				"class A<T> {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public class X extends H<Number> { void foo(A<?> a) {} }\r\n" + 
			"	                                        ^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<?>) of type X has the same erasure as foo(A<? extends T>) of type H<T> but does not override it\n" + 
			"----------\n"
			// name clash: foo(A<?>) in X and foo(A<? extends T>) in H<java.lang.Number> have the same erasure, yet neither overrides the other
			// with    public class X extends H<Number> { void foo(A<?> a) { super.foo(a); } }
			// foo(A<? extends java.lang.Number>) in H<java.lang.Number> cannot be applied to (A<capture of ?>)
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83573
	public void test039() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"   public static void main(String[] args) {\n" + 
				"      Test test = new Test();\n" + 
				"      This test2 = new Test();\n" + 
				"      System.out.println(test.get());\n" + 
				"   }\n" + 
				"   interface This {\n" + 
				"      public Object get();\n" + 
				"   }\n" + 
				" \n" + 
				"   interface That extends This {\n" + 
				"      public String get();\n" + 
				" \n" + 
				"   }\n" + 
				" \n" + 
				"   static class Test implements That {\n" + 
				" \n" + 
				"      public String get() {\n" + 
				"         return \"That\";\n" + 
				" \n" + 
				"      }\n" + 
				"   }\n" + 
				"}\n"
			},
			"That"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83218
	public void test040() {
		this.runNegativeTest(
			new String[] {
				"Base.java",
				"interface Base<E> { Base<E> proc(); }\n" +
				"abstract class Derived<D> implements Base<D> { public abstract Derived<D> proc(); }\n"
			},
			"" // no warnings
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83218
	public void test040a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T extends Number> T test() { return null; } }\n" +
				"class B extends A { @Override Integer test() { return 1; } }\n"
			},
			"----------\n" + 
			"1. WARNING in A.java (at line 2)\n" + 
			"	class B extends A { @Override Integer test() { return 1; } }\n" + 
			"	                              ^^^^^^^\n" + 
			"Type safety: The return type Integer for test() from the type B needs unchecked conversion to conform to T from the type A\n" + 
			"----------\n"
			// warning: test() in B overrides <T>test() in A; return type requires unchecked conversion
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83218
	public void test040b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"import java.util.*;\n" + 
				"class A { List<String> getList() { return null; } }\n" + 
				"class B extends A { @Override List getList() { return null; } }\n"
			},
			"----------\n" + 
			"1. WARNING in A.java (at line 3)\n" + 
			"	class B extends A { @Override List getList() { return null; } }\n" + 
			"	                              ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in A.java (at line 3)\n" + 
			"	class B extends A { @Override List getList() { return null; } }\n" + 
			"	                              ^^^^\n" + 
			"Type safety: The return type List for getList() from the type B needs unchecked conversion to conform to List<String> from the type A\n" + 
			"----------\n"
			// unchecked warning on B.getList()
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83218
	public void test040c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X<T> { X<T> x(); }\n" +
				"abstract class Y<S> implements X<S> { public abstract X x(); }\n" + // warning: x() in Y implements x() in X; return type requires unchecked conversion
				"abstract class Z implements X { public abstract X x(); }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	abstract class Y<S> implements X<S> { public abstract X x(); }\n" + 
			"	                                                      ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 2)\n" + 
			"	abstract class Y<S> implements X<S> { public abstract X x(); }\n" + 
			"	                                                      ^\n" + 
			"Type safety: The return type X for x() from the type Y<S> needs unchecked conversion to conform to X<T> from the type X<T>\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 3)\n" + 
			"	abstract class Z implements X { public abstract X x(); }\n" + 
			"	                            ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 3)\n" + 
			"	abstract class Z implements X { public abstract X x(); }\n" + 
			"	                                                ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83218
	public void test040d() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface X<T> { X<T>[] x(); }\n" +
				"abstract class Y<S> implements X<S> { public abstract X[] x(); }\n" + // warning: x() in Y implements x() in X; return type requires unchecked conversion
				"abstract class Z implements X { public abstract X[] x(); }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	abstract class Y<S> implements X<S> { public abstract X[] x(); }\n" + 
			"	                                                      ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 2)\n" + 
			"	abstract class Y<S> implements X<S> { public abstract X[] x(); }\n" + 
			"	                                                      ^^^\n" + 
			"Type safety: The return type X[] for x() from the type Y<S> needs unchecked conversion to conform to X<T>[] from the type X<T>\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 3)\n" + 
			"	abstract class Z implements X { public abstract X[] x(); }\n" + 
			"	                            ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 3)\n" + 
			"	abstract class Z implements X { public abstract X[] x(); }\n" + 
			"	                                                ^\n" + 
			"X is a raw type. References to generic type X<T> should be parameterized\n" + 
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test041() { // inherited cases for bridge methods, varargs clashes, return type conversion checks
		runConformTest(
			true,
			new String[] {
				"X.java",
				"public class X { public void foo(String... n) {} }\n" +
				"interface I { void foo(String[] n); }\n" +
				"class Y extends X implements I { }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	class Y extends X implements I { }\n" + 
			"	      ^\n" + 
			"Varargs methods should only override or be overridden by other varargs methods unlike X.foo(String...) and I.foo(String[])\n" + 
			"----------\n",
			null,
			null,
			JavacTestOptions.EclipseJustification.EclipseBug83902
			// warning: foo(java.lang.String...) in X cannot implement foo(java.lang.String[]) in I; overridden method has no '...'
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test041a() { // inherited cases for bridge methods, varargs clashes, return type conversion checks
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"public class X { public void foo(String[] n) {} }\n" +
				"interface I { void foo(String... n); }\n" +
				"class Y extends X implements I { }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	class Y extends X implements I { }\n" + 
			"	      ^\n" + 
			"Varargs methods should only override or be overridden by other varargs methods unlike X.foo(String[]) and I.foo(String...)\n" + 
			"----------\n",
			null,
			null,
			JavacTestOptions.EclipseJustification.EclipseBug83902
			// warning: foo(java.lang.String[]) in X cannot implement foo(java.lang.String...) in I; overriding method is missing '...'
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test041b() { // inherited cases for bridge methods, varargs clashes, return type conversion checks
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public Y foo() {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"		return null;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		((I) new Y()).foo();\n" +
				"	}\n" +
				"}\n" +
				"interface I { X foo(); }\n" +
				"class Y extends X implements I { }\n"
			},
			"SUCCESS"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test041c() { // inherited cases for bridge methods, varargs clashes, return type conversion checks
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { public A foo() { return null; } }\n" +
				"interface I { A<String> foo(); }\n" +
				"class Y extends X implements I { }\n" +
				"class A<T> { }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	public class X { public A foo() { return null; } }\n" + 
			"	                        ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 3)\n" + 
			"	class Y extends X implements I { }\n" + 
			"	      ^\n" + 
			"Type safety: The return type A for foo() from the type X needs unchecked conversion to conform to A<String> from the type I\n" + 
			"----------\n"
			// warning: foo() in X implements foo() in I; return type requires unchecked conversion
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test041d() { // inherited cases for bridge methods, varargs clashes, return type conversion checks
		this.runConformTest(
			true,
			new String[] {
				"X.java",
				"public class X { public Object foo() { return null; } }\n" +
				"interface I { <T> T foo(); }\n" +
				"class Y extends X implements I { }\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	class Y extends X implements I { }\n" + 
			"	      ^\n" + 
			"Type safety: The return type Object for foo() from the type X needs unchecked conversion to conform to T from the type I\n" + 
			"----------\n",
			null, null,
			JavacTestOptions.EclipseJustification.EclipseBug83902b
			// NOTE: javac issues an error & a warning which contradict each other
			// if the method Object foo() is implemented in Y then only the warning is issued, so X should be allowed to implement the method
			// Y is not abstract and does not override abstract method <T>foo() in I
			// warning: foo() in X implements <T>foo() in I; return type requires unchecked conversion
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85930
	public void test042() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface Callable<T>\n" + 
				"{\n" + 
				"    public enum Result { GOOD, BAD };\n" + 
				"    public Result call(T arg);\n" + 
				"}\n" + 
				"\n" + 
				"public class X implements Callable<String>\n" + 
				"{\n" + 
				"    public Result call(String arg) { return Result.GOOD; } // Warning line\n" + 
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72704
	public void test043() { // ambiguous message sends because of substitution from 2 different type variables
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void test(E<Integer,Integer> e) { e.id(new Integer(1)); } }\n" +
				"abstract class C<A> { public abstract void id(A x); }\n" +
				"interface I<B> { void id(B x); }\n" +
				"abstract class E<A, B> extends C<A> implements I<B> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	abstract class E<A, B> extends C<A> implements I<B> {}\n" + 
			"	               ^\n" + 
			"Name clash: The method id(A) of type C<A> has the same erasure as id(B) of type I<B> but does not override it\n" + 
			"----------\n",
			JavacTestOptions.EclipseJustification.EclipseBug72704
			// javac won't report it until C.id() is made concrete or implemented in E
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72704
	public void test043a() { // ambiguous message sends because of substitution from 2 different type variables
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void test(E<Integer,Integer> e) { e.id(new Integer(2)); } }\n" +
				"abstract class C<A extends Number> { public abstract void id(A x); }\n" +
				"interface I<B> { void id(B x); }\n" +
				"abstract class E<A extends Number, B> extends C<A> implements I<B> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public class X { void test(E<Integer,Integer> e) { e.id(new Integer(2)); } }\r\n" + 
			"	                                                     ^^\n" + 
			"The method id(Integer) is ambiguous for the type E<Integer,Integer>\n" + 
			"----------\n"
			// reference to id is ambiguous, both method id(A) in C<java.lang.Integer> and method id(B) in I<java.lang.Integer> match
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72704
	public void test043b() { // ambiguous message sends because of substitution from 2 different type variables
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void test(E<Integer,Integer> e) { e.id(new Integer(111)); } }\n" +
				"abstract class C<A extends Number> { public void id(A x) {} }\n" +
				"interface I<B> { void id(B x); }\n" +
				"class E<A extends Number, B> extends C<A> implements I<B> { public void id(B b) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public class X { void test(E<Integer,Integer> e) { e.id(new Integer(111)); } }\r\n" + 
			"	                                                     ^^\n" + 
			"The method id(Integer) is ambiguous for the type E<Integer,Integer>\n" + 
			"----------\n"
			// reference to id is ambiguous, both method id(A) in C<java.lang.Integer> and method id(B) in E<java.lang.Integer,java.lang.Integer> match
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=72704
	public void test043c() { // ambiguous message sends because of substitution from 2 different type variables
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void test(E<Integer,Integer> e) { e.id(new Integer(111)); }\n" +
				"	void test(M<Integer,Integer> m) {\n" +
				"		m.id(new Integer(111));\n" +
				"		((E<Integer, Integer>) m).id(new Integer(111));\n" +
				"	}\n" +
				"	void test(N<Integer> n) { n.id(new Integer(111)); }\n" +
				"}\n" +
				"abstract class C<A extends Number> { public void id(A x) {} }\n" +
				"interface I<B> { void id(B x); }\n" +
				"abstract class E<A extends Number, B> extends C<A> implements I<B> {}\n" +
				"class M<A extends Number, B> extends E<A, B> { public void id(B b) {} }\n" +
				"abstract class N<T extends Number> extends E<T, Number> { @Override public void id(T n) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\r\n" + 
			"	m.id(new Integer(111));\r\n" + 
			"	  ^^\n" + 
			"The method id(Integer) is ambiguous for the type M<Integer,Integer>\n" + 
			"----------\n"
			// reference to id is ambiguous, both method id(A) in C<java.lang.Integer> and method id(B) in M<java.lang.Integer,java.lang.Integer> match
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97161
	public void test043d() {
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static p.Y.*;\n" +
				"import static p.Z.*;\n" +
				"public class X {\n" +
				"	Y data = null;\n" +
				"	public X() { foo(data.l); }\n" +
				"}\n",
				"p/Y.java",
				"package p;\n" +
				"import java.util.List;\n" +
				"public class Y {\n" +
				"	List l = null;\n" +
				"	public static <T> void foo(T... e) {}\n" +
				"}\n",
				"p/Z.java",
				"package p;\n" +
				"import java.util.List;\n" +
				"public class Z {\n" +
				"	public static <T> void foo(List<T>... e) {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\X.java (at line 6)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Type safety: Unchecked invocation foo(List...) of the generic method foo(List<T>...) of type Z\n" + 
			"----------\n" + 
			"2. WARNING in p\\X.java (at line 6)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	                 ^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<T>\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in p\\Y.java (at line 4)\n" + 
			"	List l = null;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n"
			// unchecked conversion warnings
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97161
	public void test043e() {
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import static p.Y.*;\n" +
				"public class X {\n" +
				"	Y data = null;\n" +
				"	public X() { foo(data.l); }\n" +
				"}\n",
				"p/Y.java",
				"package p;\n" +
				"import java.util.List;\n" +
				"public class Y {\n" +
				"	List l = null;\n" +
				"	public static <T> void foo(T... e) {}\n" +
				"	public static <T> void foo(List<T>... e) {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in p\\X.java (at line 5)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Type safety: Unchecked invocation foo(List...) of the generic method foo(List<T>...) of type Y\n" + 
			"----------\n" + 
			"2. WARNING in p\\X.java (at line 5)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	                 ^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<T>\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in p\\Y.java (at line 4)\n" + 
			"	List l = null;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n"
			// unchecked conversion warnings
		);
	}

	public void test043f() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void test(M<Integer,Integer> m) {\n" +
				"		m.id(new Integer(111), new Integer(112));\n" +
				"	}\n" +
				"}\n" +
				"abstract class C<T1 extends Number> { public <U1 extends Number> void id(T1 x, U1 u) {} }\n" +
				"interface I<T2> { }\n" +
				"abstract class E<T3 extends Number, T4> extends C<T3> implements I<T4> {}\n" +
				"class M<T5 extends Number, T6> extends E<T5, T6> { public <U2 extends Number> void id(T5 b, U2 u) {} }\n"
			},
			""
		);
	}
	public void test043g() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void test(M<Integer,Integer> m) {\n" +
				"		m.id(new Integer(111));\n" +
				"	}\n" +
				"}\n" +
				"abstract class C<T1 extends Number> { public void id(T1 x) {} }\n" +
				"interface I<T2> { void id(T2 x); }\n" +
				"abstract class E<T3 extends Number, T4> extends C<T3> implements I<T4> {}\n" +
				"class M<T5 extends Number, T6> extends E<T5, T6> { public void id(T6 b) {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	m.id(new Integer(111));\n" + 
			"	  ^^\n" + 
			"The method id(Integer) is ambiguous for the type M<Integer,Integer>\n" + 
			"----------\n"
			// reference to id is ambiguous, both method id(A) in C<java.lang.Integer> and method id(B) in M<java.lang.Integer,java.lang.Integer> match
		);
	}

	// ensure AccOverriding remains when attempting to override final method 
	public void test044() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { final void foo() {} }\n" + 
				"class XS extends X { @Override void foo() {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class XS extends X { @Override void foo() {} }\n" + 
			"	                                    ^^^^^\n" + 
			"Cannot override the final method from X\n" + 
			"----------\n"
		);
	}
	// ensure AccOverriding remains when attempting to override final method 
	public void test044a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { public void foo() {} }\n" + 
				"class XS extends X { @Override void foo() {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class XS extends X { @Override void foo() {} }\n" + 
			"	                                    ^^^^^\n" + 
			"Cannot reduce the visibility of the inherited method from X\n" + 
			"----------\n"
		);
	}
	// ensure AccOverriding remains when attempting to override final method 
	public void test044b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void foo() {} }\n" + 
				"class XS extends X { @Override void foo() throws ClassNotFoundException {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class XS extends X { @Override void foo() throws ClassNotFoundException {} }\n" + 
			"	                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Exception ClassNotFoundException is not compatible with throws clause in X.foo()\n" + 
			"----------\n"
		);
	}
	// ensure AccOverriding remains when attempting to override final method 
	public void test044c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void foo() {} }\n" + 
				"class XS extends X { @Override int foo() {} }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class XS extends X { @Override int foo() {} }\n" + 
			"	                               ^^^\n" + 
			"The return type is incompatible with X.foo()\n" + 
			"----------\n"
		);
	}

	public void test045() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class Foo {}\n" + 
				"\n" + 
				"interface Bar {\n" + 
				"  Foo get(Class<?> c);\n" + 
				"}\n" + 
				"public class X implements Bar {\n" + 
				"  public Foo get(Class c) { return null; }\n" + 
				"}\n"
			},
			""
		);
	}

	// ensure no unchecked warning
	public void test046() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface IX <T> {\n" + 
				"	public T doSomething();\n" + 
				"}\n" + 
				"public class X implements IX<Integer> {\n" + 
				"   Zork z;\n" +
				"	public Integer doSomething() {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	Zork z;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87157
	public void test047() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface Interface {\n" + 
				"    Number getValue();\n" + 
				"}\n" + 
				"class C1 {\n" + 
				"    public Double getValue() {\n" + 
				"        return 0.0;\n" + 
				"    }\n" + 
				"}\n" + 
				"public class X extends C1 implements Interface{\n" + 
				"    public static void main(String[] args) {\n" + 
				"        Interface i=new X();\n" + 
				"        System.out.println(i.getValue());\n" + 
				"    }\n" + 
				"}\n"
			},
		"0.0");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048() {
		this.runConformTest(
			new String[] {
				"X1.java",
				"import java.util.*;\n" + 
				"public class X1 extends LinkedHashMap<String, String> {\n" + 
				"    public Object putAll(Map<String,String> a) { return null; }\n" + 
				"}\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048a() {
		this.runConformTest(
			new String[] {
				"X2.java",
				"public class X2 extends Y<String> {\n" + 
				"    public Object foo(I<String> z) { return null; }\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public void foo(I<? extends T> a) {}\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public void foo(I<? extends T> a);\n" + 
				"}\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048b() {
		this.runNegativeTest(
			new String[] {
				"X3.java",
				"public class X3 extends Y<String> {\n" + 
				"    public void foo(I<String> z) {}\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public void foo(I<? extends T> a) {}\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public void foo(I<? extends T> a);\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X3.java (at line 2)\r\n" + 
			"	public void foo(I<String> z) {}\r\n" + 
			"	            ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(I<String>) of type X3 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
			"----------\n"
			// name clash: foo(I<java.lang.String>) in X and foo(I<? extends T>) in Y<java.lang.String> have the same erasure, yet neither overrides the other
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048c() {
		this.runConformTest(
			new String[] {
				"X4.java",
				"public class X4 extends Y<String> {\n" + 
				"    public String foo(I<String> z) { return null; }\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public Object foo(I<? extends T> a) { return null; }\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public Object foo(I<? extends T> a);\n" + 
				"}\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048d() {
		this.runConformTest(
			new String[] {
				"X5.java",
				"public class X5 extends Y<String> {\n" + 
				"    public Object foo(I<String> z) { return null; }\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public String foo(I<? extends T> a) { return null; }\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public String foo(I<? extends T> a);\n" + 
				"}\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048e() {
		this.runConformTest(
			new String[] {
				"X6.java",
				"public class X6 extends Y<String> {\n" + 
				"    public void foo(I<String> z) {}\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public Object foo(I<? extends T> a) { return null; }\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public Object foo(I<? extends T> a);\n" + 
				"}\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048f() {
		this.runConformTest(
			new String[] {
				"X7.java",
				"public class X7 extends Y<String> {\n" + 
				"    public String foo(I<String> z) { return null; }\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public T foo(I<? extends T> a) { return null; }\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public T foo(I<? extends T> a);\n" + 
				"}\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048g() {
		this.runNegativeTest(
			new String[] {
				"X8.java",
				"public class X8 extends Y<String> {\n" + 
				"    public Object foo(I<String> z) { return null; }\n" + 
				"}\n" +
				"class Y<T> implements I<T> {\n" + 
				"    public T foo(I<? extends T> a) { return null; }\n" + 
				"}\n" +
				"interface I<T> {\n" +
				"    public T foo(I<? extends T> a);\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X8.java (at line 2)\r\n" + 
			"	public Object foo(I<String> z) { return null; }\r\n" + 
			"	              ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(I<String>) of type X8 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
			"----------\n"
			// name clash: foo(I<java.lang.String>) in X7 and foo(I<? extends T>) in Y<java.lang.String> have the same erasure, yet neither overrides the other
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88094
	public void test049() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"	T id(T x) { return x; }\n" + 
				"	A id(A x) { return x; }\n" + 
				"}\n" +
				"class Y<T extends A> extends X<T> {\n" + 
				"	@Override T id(T x) { return x; }\n" + 
				"	@Override A id(A x) { return x; }\n" + 
				"}\n" + 
				"class A {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	class Y<T extends A> extends X<T> {\n" + 
			"	      ^\n" + 
			"Duplicate methods named id with the parameters (A) and (T) are defined by the type X<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	@Override T id(T x) { return x; }\n" + 
			"	            ^^^^^^^\n" + 
			"Method id(T) has the same erasure id(A) as another method in type Y<T>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 7)\n" + 
			"	@Override A id(A x) { return x; }\n" + 
			"	            ^^^^^^^\n" + 
			"Method id(A) has the same erasure id(A) as another method in type Y<T>\n" + 
			"----------\n"
			// id(T) is already defined in Y
			// id(java.lang.String) in Y overrides id(T) in X; return type requires unchecked conversion
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88094
	public void test049a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"	T id(T x) { return x; }\n" + 
				"	A id(A x) { return x; }\n" + 
				"}\n" +
				"class Y<T extends A> extends X<T> {}\n" + 
				"class A {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	class Y<T extends A> extends X<T> {}\n" + 
			"	      ^\n" + 
			"Duplicate methods named id with the parameters (A) and (T) are defined by the type X<T>\n" + 
			"----------\n"
			// methods id(T) from X<T> and id(A) from X<T> are inherited with the same signature
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94754
	public void test050() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 public static <S extends A> S foo() { System.out.print(\"A\"); return null; }\n" + 
				"		 public static <N extends B> N foo() { System.out.print(\"B\"); return null; }\n" + 
				"		 public static void main(String[] args) {\n" + 
				"		 	X.<A>foo();\n" + 
				"		 	X.<B>foo();\n" + 
				"		 	new X().<B>foo();\n" + 
				"		 }\n" + 
				"}\n" + 
				"class A {}\n" + 
				"class B {}\n"
			},
			"ABB"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94754
	public void test050a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 public static <S extends A> void foo() { System.out.print(\"A\"); }\n" + 
				"		 public static <N extends B> N foo() { System.out.print(\"B\"); return null; }\n" + 
				"		 static void test () {\n" + 
				"		 	X.foo();\n" + 
				"		 	foo();\n" + 
				"		 }\n" + 
				"}\n" + 
				"class A {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\r\n" + 
			"	X.foo();\r\n" + 
			"	  ^^^\n" + 
			"The method foo() is ambiguous for the type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\r\n" + 
			"	foo();\r\n" + 
			"	^^^\n" + 
			"The method foo() is ambiguous for the type X\n" + 
			"----------\n"
			// both references are ambiguous
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423 - variation
	public void test050b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	class C1 {\n" + 
				"		Y foo(Object o) {  return null; } // duplicate\n" + 
				"		Z foo(Object o) {  return null; } // duplicate\n" + 
				"	}\n" + 
				"	class C2 {\n" + 
				"		<T extends Y> T foo(Object o) {  return null; } // ok\n" + 
				"		<T extends Z> T foo(Object o) {  return null; } // ok\n" + 
				"	}\n" + 
				"	class C3 {\n" + 
				"		A<Y> foo(Object o) {  return null; } // duplicate\n" + 
				"		A<Z> foo(Object o) {  return null; } // duplicate\n" + 
				"	}\n" + 
				"	class C4 {\n" + 
				"		Y foo(Object o) {  return null; } // duplicate\n" + 
				"		<T extends Z> T foo(Object o) {  return null; } // duplicate\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T> {}" +
				"class Y {}" +
				"class Z {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Y foo(Object o) {  return null; } // duplicate\n" + 
			"	  ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C1\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	Z foo(Object o) {  return null; } // duplicate\n" + 
			"	  ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C1\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 11)\n" + 
			"	A<Y> foo(Object o) {  return null; } // duplicate\n" + 
			"	     ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C3\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 12)\n" + 
			"	A<Z> foo(Object o) {  return null; } // duplicate\n" + 
			"	     ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C3\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 15)\n" + 
			"	Y foo(Object o) {  return null; } // duplicate\n" + 
			"	  ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C4\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 16)\n" + 
			"	<T extends Z> T foo(Object o) {  return null; } // duplicate\n" + 
			"	                ^^^^^^^^^^^^^\n" + 
			"Duplicate method foo(Object) in type X.C4\n" + 
			"----------\n"
			// foo(java.lang.Object) is already defined in X.C1
			// foo(java.lang.Object) is already defined in X.C3
			// foo(java.lang.Object) is already defined in X.C4
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423 - variation
	public void test050c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	class C5 {\n" + 
				"		A<Y> foo(A<Y> o) {  return null; } // duplicate\n" + 
				"		A<Z> foo(A<Z> o) {  return null; } // duplicate\n" + 
				"	}\n" + 
				"	class C6 {\n" + 
				"		<T extends Y> T foo(A<Y> o) {  return null; } // ok\n" + 
				"		<T extends Z> T foo(A<Z> o) {  return null; } // ok\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T> {}" +
				"class Y {}" +
				"class Z {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	A<Y> foo(A<Y> o) {  return null; } // duplicate\n" + 
			"	     ^^^^^^^^^^^\n" + 
			"Method foo(A<Y>) has the same erasure foo(A<T>) as another method in type X.C5\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	A<Z> foo(A<Z> o) {  return null; } // duplicate\n" + 
			"	     ^^^^^^^^^^^\n" + 
			"Method foo(A<Z>) has the same erasure foo(A<T>) as another method in type X.C5\n" + 
			"----------\n"
			// name clash: foo(A<Y>) and foo(A<Z>) have the same erasure
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423 - variation
	public void test050d() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	class C7 {\n" + 
				"		<T extends Y, U> T foo(Object o) {  return null; } // ok\n" + 
				"		<T extends Z> T foo(Object o) {  return null; } // ok\n" + 
				"	}\n" + 
				"}\n" +
				"class A<T> {}" +
				"class Y {}" +
				"class Z {}"
			},
			""
		);
	}	

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050e() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> N a(A<String> s) { return null; }\n" + 
				"		 <N> Object a(A<Number> n) { return null; }\n" + 
				"		 <N extends B> void b(A<String> s) {}\n" + 
				"		 <N extends B> B b(A<Number> n) { return null; }\n" + 
				"		 void c(A<String> s) {}\n" + 
				"		 B c(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050f() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> N a(A<String> s) { return null; }\n" + 
				"		 <N> B a(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	<N extends B> N a(A<String> s) { return null; }\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Method a(A<String>) has the same erasure a(A<T>) as another method in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	<N> B a(A<Number> n) { return null; }\n" + 
			"	      ^^^^^^^^^^^^^^\n" + 
			"Method a(A<Number>) has the same erasure a(A<T>) as another method in type X\n" + 
			"----------\n"
			// name clash: <N>a(A<java.lang.String>) and <N>a(A<java.lang.Number>) have the same erasure
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050g() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> N b(A<String> s) { return null; }\n" + 
				"		 <N extends B> B b(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	<N extends B> N b(A<String> s) { return null; }\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Method b(A<String>) has the same erasure b(A<T>) as another method in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	<N extends B> B b(A<Number> n) { return null; }\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Method b(A<Number>) has the same erasure b(A<T>) as another method in type X\n" + 
			"----------\n"
			// name clash: <N>b(A<java.lang.String>) and <N>b(A<java.lang.Number>) have the same erasure
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050h() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 B c(A<String> s) { return null; }\n" + 
				"		 B c(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	B c(A<String> s) { return null; }\n" + 
			"	  ^^^^^^^^^^^^^^\n" + 
			"Method c(A<String>) has the same erasure c(A<T>) as another method in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	B c(A<Number> n) { return null; }\n" + 
			"	  ^^^^^^^^^^^^^^\n" + 
			"Method c(A<Number>) has the same erasure c(A<T>) as another method in type X\n" + 
			"----------\n"
			// name clash: c(A<java.lang.String>) and c(A<java.lang.Number>) have the same erasure
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050i() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> N a(A<Number> s) { return null; }\n" + 
				"		 <N> Object a(A<Number> n) { return null; }\n" + 
				"		 <N extends B> N b(A<Number> s) { return null; }\n" + 
				"		 <N> Object b(A<String> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050j() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> N a(A<Number> s) { return null; }\n" + 
				"		 <N> B a(A<Number> n) { return null; }\n" + 
				"		 <N extends B> N b(A<Number> s) { return null; }\n" + 
				"		 <N> B b(A<String> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	<N extends B> N a(A<Number> s) { return null; }\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	<N> B a(A<Number> n) { return null; }\n" + 
			"	      ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\n" + 
			"	<N extends B> N b(A<Number> s) { return null; }\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Method b(A<Number>) has the same erasure b(A<T>) as another method in type X\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\n" + 
			"	<N> B b(A<String> n) { return null; }\n" + 
			"	      ^^^^^^^^^^^^^^\n" + 
			"Method b(A<String>) has the same erasure b(A<T>) as another method in type X\n" + 
			"----------\n"
			// name clash: <N>a(A<java.lang.Number>) and <N>a(A<java.lang.Number>) have the same erasure
			// name clash: <N>b(A<java.lang.Number>) and <N>b(A<java.lang.String>) have the same erasure
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050k() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 <N extends B> void a(A<Number> s) {}\n" + 
				"		 <N extends B> B a(A<Number> n) { return null; }\n" + 
				"		 <N extends B> Object b(A<Number> s) { return null; }\n" + 
				"		 <N extends B> B b(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	<N extends B> void a(A<Number> s) {}\n" + 
			"	                   ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	<N extends B> B a(A<Number> n) { return null; }\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\n" + 
			"	<N extends B> Object b(A<Number> s) { return null; }\n" + 
			"	                     ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\n" + 
			"	<N extends B> B b(A<Number> n) { return null; }\n" + 
			"	                ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n"
			// <N>a(A<java.lang.Number>) is already defined in X
			// <N>b(A<java.lang.Number>) is already defined in X
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050l() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"		 void a(A<Number> s) {}\n" + 
				"		 B a(A<Number> n) { return null; }\n" + 
				"		 Object b(A<Number> s) {}\n" + 
				"		 B b(A<Number> n) { return null; }\n" + 
				"}\n" +
				"class A<T> {}\n" + 
				"class B {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	void a(A<Number> s) {}\r\n" + 
			"	     ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\r\n" + 
			"	B a(A<Number> n) { return null; }\r\n" + 
			"	  ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\r\n" + 
			"	Object b(A<Number> s) {}\r\n" + 
			"	       ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\r\n" + 
			"	B b(A<Number> n) { return null; }\r\n" + 
			"	  ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n"
			// a(A<java.lang.Number>) is already defined in X
			// b(A<java.lang.Number>) is already defined in X
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89470
	public void test051() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements I {\n" + 
				"		 public <T extends I> void foo(T t) {}\n" + 
				"}\n" +
				"interface I {\n" + 
				"		 <T> void foo(T t);\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X implements I {\n" + 
			"	             ^\n" + 
			"The type X must implement the inherited abstract method I.foo(T)\n" + 
			"----------\n"
			// X is not abstract and does not override abstract method <T>foo(T) in I
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89470
	public void test051a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(A<String> a) {}\n" + 
				"	void foo(A<Integer> a) {}\n" +
				"}\n" + 
				"class A<T> {}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(A<String> a) {}\n" + 
			"	     ^^^^^^^^^^^^^^^^\n" + 
			"Method foo(A<String>) has the same erasure foo(A<T>) as another method in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	void foo(A<Integer> a) {}\n" + 
			"	     ^^^^^^^^^^^^^^^^^\n" + 
			"Method foo(A<Integer>) has the same erasure foo(A<T>) as another method in type X\n" + 
			"----------\n"
			// name clash: foo(A<java.lang.String>) and foo(A<java.lang.Integer>) have the same erasure
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89470
	public void test051b() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(A<String> a) {}\n" + 
				"	Object foo(A<Integer> a) { return null; }\n" +
				"}\n" + 
				"class A<T> {}\n",
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91728
	public void test052() {
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A<T> {\n" + 
				"	public A test() { return null; }\n" + 
				"	public A<T> test2() { return null; }\n" + 
				"	public A<X> test3() { return null; }\n" + 
				"	public <U> A<U> test4() { return null; }\n" + 
				"}\n" +
				"class B extends A<X> {\n" + 
				"	@Override public B test() { return null; }\n" + 
				"	@Override public B test2() { return null; }\n" + 
				"	@Override public B test3() { return null; }\n" + 
				"	@Override public <U> A<U> test4() { return null; }\n" + 
				"}\n" +
				"class X{}\n"
			},
			""
		);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91728
	public void test052a() {
		this.runNegativeTest(
				new String[] {
					"A.java",
					"public class A<T> {\n" + 
					"	public <U> A<U> test() { return null; }\n" + 
					"	public <U> A<U> test2() { return null; }\n" + 
					"	public <U> A<U> test3() { return null; }\n" + 
					"}\n" +
					"class B extends A<X> {\n" + 
					"	@Override public B test() { return null; }\n" + 
					"	@Override public A test2() { return null; }\n" + 
					"	@Override public A<X> test3() { return null; }\n" + 
					"}\n" +
					"class X{}\n"
				},
				"----------\n" + 
				"1. WARNING in A.java (at line 7)\n" + 
				"	@Override public B test() { return null; }\n" + 
				"	                 ^\n" + 
				"Type safety: The return type B for test() from the type B needs unchecked conversion to conform to A<Object> from the type A<X>\n" + 
				"----------\n" + 
				"2. WARNING in A.java (at line 8)\n" + 
				"	@Override public A test2() { return null; }\n" + 
				"	                 ^\n" + 
				"A is a raw type. References to generic type A<T> should be parameterized\n" + 
				"----------\n" + 
				"3. WARNING in A.java (at line 8)\n" + 
				"	@Override public A test2() { return null; }\n" + 
				"	                 ^\n" + 
				"Type safety: The return type A for test2() from the type B needs unchecked conversion to conform to A<U> from the type A<T>\n" + 
				"----------\n" + 
				"4. WARNING in A.java (at line 9)\n" + 
				"	@Override public A<X> test3() { return null; }\n" + 
				"	                 ^\n" + 
				"Type safety: The return type A<X> for test3() from the type B needs unchecked conversion to conform to A<Object> from the type A<X>\n" + 
				"----------\n"
				// warning: test() in B overrides <U>test() in A; return type requires unchecked conversion
				// warning: test2() in B overrides <U>test2() in A; return type requires unchecked conversion
				// warning: test3() in B overrides <U>test3() in A; return type requires unchecked conversion
			);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91728
	public void test053() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class X {\n" + 
				"	void test(A a) { B b = a.foo(); }\n" + 
				"	void test2(A<X> a) { B b = a.foo(); }\n" + 
				"	void test3(B b) { B bb = b.foo(); }\n" + 
				"}\n" +
				"class A<T> {\n" + 
				"	<U> A<U> foo() { return null; }\n" + 
				"}\n" +
				"class B extends A<X> {\n" + 
				"	@Override B foo() { return null; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	void test(A a) { B b = a.foo(); }\n" + 
			"	          ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	void test(A a) { B b = a.foo(); }\n" + 
			"	                       ^^^^^^^\n" + 
			"Type mismatch: cannot convert from A to B\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 3)\n" + 
			"	void test2(A<X> a) { B b = a.foo(); }\n" + 
			"	                           ^^^^^^^\n" + 
			"Type mismatch: cannot convert from A<Object> to B\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 10)\n" + 
			"	@Override B foo() { return null; }\n" + 
			"	          ^\n" + 
			"Type safety: The return type B for foo() from the type B needs unchecked conversion to conform to A<Object> from the type A<X>\n" + 
			"----------\n"
			// 2: incompatible types
			// 3: incompatible types; no instance(s) of type variable(s) U exist so that A<U> conforms to B
			// 10 warning: foo() in B overrides <U>foo() in A; return type requires unchecked conversion
		);
	}

	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void a(Object x) {}\n" +
				"	<T> T a(T x) {  return null; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void a(Object x) {}\n" + 
			"	     ^^^^^^^^^^^\n" + 
			"Method a(Object) has the same erasure a(Object) as another method in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	<T> T a(T x) {  return null; }\n" + 
			"	      ^^^^^^\n" + 
			"Method a(T) has the same erasure a(Object) as another method in type X\n" + 
			"----------\n"
			// a(X) is already defined in X
		);
	}
	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<T1, T2> String aaa(X x) {  return null; }\n" + 
				"	<T extends X> T aaa(T x) {  return null; }\n" + 
				"	<T> String aa(X x) {  return null; }\n" + 
				"	<T extends X> T aa(T x) {  return null; }\n" + 
				"	String a(X x) {  return null; }\n" + // dup
				"	<T extends X> T a(T x) {  return null; }\n" + 

				"	<T> String z(X x) { return null; }\n" + 
				"	<T, S> Object z(X x) { return null; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	String a(X x) {  return null; }\n" + 
			"	       ^^^^^^\n" + 
			"Method a(X) has the same erasure a(X) as another method in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	<T extends X> T a(T x) {  return null; }\n" + 
			"	                ^^^^^^\n" + 
			"Method a(T) has the same erasure a(X) as another method in type X\n" + 
			"----------\n"
			// a(X) is already defined in X
		);
	}
	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054b() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"		 Object foo(X<T> t) { return null; }\n" + 
				"		 <S> String foo(X<T> s) { return null; }\n" + 
				"}\n"
			},
			""
		);
	}
	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"		<T1 extends X<T1>> void dupT() {}\n" + 
				"		<T2 extends X<T2>> Object dupT() {return null;}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	<T1 extends X<T1>> void dupT() {}\r\n" + 
			"	                        ^^^^^^\n" + 
			"Duplicate method dupT() in type X<T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\r\n" + 
			"	<T2 extends X<T2>> Object dupT() {return null;}\r\n" + 
			"	                          ^^^^^^\n" + 
			"Duplicate method dupT() in type X<T>\n" + 
			"----------\n"
			// <T1>dupT() is already defined in X
		);
	}
	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054d() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	<T> T a(A<T> t) {return null;}\n" + 
				"	<T> String a(A<Object> o) {return null;}\n" +
				"	<T> T aa(A<T> t) {return null;}\n" + 
				"	String aa(A<Object> o) {return null;}\n" +
				"}\n" + 
				"class A<T> {}\n",
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=95933
	public void test055() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		A a = new C();\n" + 
				"		try { a.f(new Object()); } catch (ClassCastException e) {\n" +
				"			System.out.println(1);\n" +
				"		}\n" +
				"	}\n" + 
				"}\n" +
				"interface A<T> { void f(T x); }\n" + 
				"interface B extends A<String> { void f(String x); }\n" + 
				"class C implements B { public void f(String x) {} }\n"
			},
			"1"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97809
	public void test056() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"   public static String bind(String message, Object binding) { return null; }\n" + 
				"   public static String bind(String message, Object[] bindings) { return null; }\n" + 
				"}\n" + 
				"class Y extends X {\n" + 
				"   public static String bind(String message, Object binding) { return null; }\n" + 
				"   public static String bind(String message, Object[] bindings) { return null; }\n" + 
				"}\n" + 
				"class Z {\n" + 
				"   void bar() { Y.bind(\"\", new String[] {\"\"}); }\n" + 
				"}\n"
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84035
	public void test057() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"   public static void main(String[] args) {\n" + 
				"   	A<Integer> x = new A<Integer>();\n" + 
				"   	B<Integer> y = new B<Integer>();\n" + 
				"   	new X().print(x);\n" + 
				"   	new X().print(y);\n" + 
				"	}\n" +
				"	public <T extends IA<?>> void print(T a) { System.out.print(1); }\n" +
				"	public <T extends IB<?>> void print(T a) { System.out.print(2); }\n" +
				"}\n" +
				"interface IA<E> {}\n" + 
				"interface IB<E> extends IA<E> {}\n" + 
				"class A<E> implements IA<E> {}\n" + 
				"class B<E> implements IB<E> {}\n"
			},
			"12");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84035
	public void test057a() {
		this.runConformTest(
			new String[] {
				"XX.java",
				"public class XX {\n" + 
				"   public static void main(String[] args) {\n" + 
				"   	A<Integer> x = new A<Integer>();\n" + 
				"   	B<Integer> y = new B<Integer>();\n" + 
				"   	print(x);\n" + 
				"   	print(y);\n" + 
				"	}\n" +
				"	public static <T extends IA<?>> void print(T a) { System.out.print(3); }\n" +
				"	public static <T extends IB<?>> void print(T a) { System.out.print(4); }\n" +
				"}\n" +
				"interface IA<E> {}\n" + 
				"interface IB<E> extends IA<E> {}\n" + 
				"class A<E> implements IA<E> {}\n" + 
				"class B<E> implements IB<E> {}\n"
			},
			"34");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94898
	public void test058() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <B extends Number> {\n" + 
				"   public static void main(String[] args) {\n" + 
				"   	X<Integer> x = new X<Integer>();\n" + 
				"   	x.aaa(null);\n" + 
				"   	x.aaa(15);\n" + 
				"	}\n" +
				"	<T> T aaa(T t) { System.out.print('T'); return null; }\n" +
				"	void aaa(B b) { System.out.print('B'); }\n" +
				"}\n"
			},
			"BB");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94898
	public void test058a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A> {\n" + 
				"   void test() {\n" + 
				"   	new X<Object>().foo(\"X\");\n" + 
				"   	new X<Object>().foo2(\"X\");\n" + 
				"   }\n" + 
				"	<T> T foo(T t) {return null;}\n" +
				"	void foo(A a) {}\n" +
				"	<T> T foo2(T t) {return null;}\n" +
				"	<T> void foo2(A a) {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	new X<Object>().foo(\"X\");\r\n" + 
			"	                ^^^\n" + 
			"The method foo(String) is ambiguous for the type X<Object>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\r\n" + 
			"	new X<Object>().foo2(\"X\");\r\n" + 
			"	                ^^^^\n" + 
			"The method foo2(String) is ambiguous for the type X<Object>\n" + 
			"----------\n"
			// both references are ambiguous
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94898
	public void test058b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A> extends Y<A> {\n" + 
				"   void test() {\n" + 
				"   	new X<Object>().foo(\"X\");\n" + 
				"   	new X<Object>().foo2(\"X\");\n" + 
				"   }\n" + 
				"	<T> T foo(T t) {return null;}\n" +
				"	<T> T foo2(T t) {return null;}\n" +
				"}\n" +
				"class Y<A> {\n" +
				"	void foo(A a) {}\n" +
				"	<T> void foo2(A a) {}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\r\n" + 
			"	new X<Object>().foo(\"X\");\r\n" + 
			"	                ^^^\n" + 
			"The method foo(String) is ambiguous for the type X<Object>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\r\n" + 
			"	new X<Object>().foo2(\"X\");\r\n" + 
			"	                ^^^^\n" + 
			"The method foo2(String) is ambiguous for the type X<Object>\n" + 
			"----------\n"
			// both references are ambiguous
		);
	}

	public void test059() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {new B().foo(\"aa\");}\n" + 
				"}\n" +
				"class A { <U> void foo(U u) {System.out.print(false);} }\n" + 
				"class B extends A { <V> void foo(String s) {System.out.print(true);} }\n"
			},
			"true");
	}
	public void test059a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] args) {new B().foo(\"aa\");}\n" + 
				"}\n" +
				"class A { <U> void foo(String s) {System.out.print(true);} }\n" + 
				"class B extends A { <V> void foo(V v) {System.out.print(false);} }\n"
			},
			"true");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90619
	public void test060() {
		this.runConformTest(
			new String[] {
				"I.java",
				"import java.util.Iterator;\n" +
				"public interface I {\n" +
				"	void method(Iterator<Object> iter);\n" +
				"	public static class TestClass implements I {\n" +
				"		public void method(Iterator iter) {}\n" +
				"	}\n" +
				"}"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90619
	public void test060b() {
		this.runConformTest(
			new String[] {
				"I2.java",
				"import java.util.Iterator;\n" +
				"public interface I2 {\n" +
				"	void method(Iterator<Object>[] iter);\n" +
				"	public static class TestClass implements I2 {\n" +
				"		public void method(Iterator[] iter) {}\n" +
				"	}\n" +
				"}"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90619
	public void test060c() {
		this.runNegativeTest(
			new String[] {
				"I3.java",
				"import java.util.Iterator;\n" +
				"public interface I3 {\n" +
				"	void method(Iterator<Object>[] iter);\n" +
				"	public static class TestClass implements I3 {\n" +
				"		public void method(Iterator[][] iter) {}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in I3.java (at line 4)\n" + 
			"	public static class TestClass implements I3 {\n" + 
			"	                    ^^^^^^^^^\n" + 
			"The type I3.TestClass must implement the inherited abstract method I3.method(Iterator<Object>[])\n" + 
			"----------\n" + 
			"2. WARNING in I3.java (at line 5)\n" + 
			"	public void method(Iterator[][] iter) {}\n" + 
			"	                   ^^^^^^^^\n" + 
			"Iterator is a raw type. References to generic type Iterator<E> should be parameterized\n" + 
			"----------\n"
			// does not override abstract method method(java.util.Iterator<java.lang.Object>[]) in I3
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99106
	public void test061() {
		this.runNegativeTest(
			new String[] {
				"Try.java",
				"public class Try {\n" +
				"	public static void main(String[] args) {\n" +
				"		Ex<String> ex = new Ex<String>();\n" +
				"		ex.one(\"eclipse\", new Integer(1));\n" +
				"		ex.two(new Integer(1));\n" +
				"		ex.three(\"eclipse\");\n" +
				"		ex.four(\"eclipse\");\n" +
				"		System.out.print(',');\n" +
				"		Ex ex2 = ex;\n" +
				"		ex2.one(\"eclipse\", new Integer(1));\n" + // unchecked warning
				"		ex2.two(new Integer(1));\n" + // unchecked warning
				"		ex2.three(\"eclipse\");\n" + // unchecked warning
				"		ex2.four(\"eclipse\");\n" + // unchecked warning
				"	}\n" +
				"}\n" +
				"class Top<TC> {\n" +
				"	<TM> void one(TC cTop, TM mTop) { System.out.print(-1); }\n" +
				"	<TM> void two(TM mTop) { System.out.print(-2); }\n" +
				"	void three(TC cTop) { System.out.print(-3); }\n" +
				"	<TM> void four(TC cTop) { System.out.print(-4); }\n" +
				"}\n" +
				"class Ex<C> extends Top<C> {\n" +
				"	@Override <M> void one(C cEx, M mEx) { System.out.print(1); }\n" +
				"	@Override <M> void two(M mEx) { System.out.print(2); }\n" +
				"	@Override void three(C cEx) { System.out.print(3); }\n" +
				"	@Override <M> void four(C cEx) { System.out.print(4); }\n" +
				"}"				
			},
			"----------\n" + 
			"1. WARNING in Try.java (at line 9)\n" + 
			"	Ex ex2 = ex;\n" + 
			"	^^\n" + 
			"Ex is a raw type. References to generic type Ex<C> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Try.java (at line 10)\n" + 
			"	ex2.one(\"eclipse\", new Integer(1));\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method one(Object, Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Try.java (at line 11)\n" + 
			"	ex2.two(new Integer(1));\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method two(Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in Try.java (at line 12)\n" + 
			"	ex2.three(\"eclipse\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method three(Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized\n" + 
			"----------\n" + 
			"5. WARNING in Try.java (at line 13)\n" + 
			"	ex2.four(\"eclipse\");\n" + 
			"	^^^^^^^^^^^^^^^^^^^\n" + 
			"Type safety: The method four(Object) belongs to the raw type Ex. References to generic type Ex<C> should be parameterized\n" + 
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99106
	public void test062() {
		this.runNegativeTest(
			new String[] {
				"Errors.java",
				"public class Errors {\n" +
				"	void foo() {\n" +
				"		Ex<String> ex = new Ex<String>();\n" +
				"		ex.proof(\"eclipse\");\n" +
				"		ex.five(\"eclipse\");\n" +
				"		ex.six(\"eclipse\");\n" +
				"		Ex ex2 = ex;\n" +
				"		ex2.proof(\"eclipse\");\n" +
				"		ex2.five(\"eclipse\");\n" +
				"		ex2.six(\"eclipse\");\n" +
				"	}\n" +
				"}\n" +
				"class Top<TC> {\n" +
				"	<TM> void proof(Object cTop) {}\n" +
				"	<TM> void five(TC cTop) {}\n" +
				"	void six(TC cTop) {}\n" +
				"}\n" +
				"class Ex<C> extends Top<C> {\n" +
				"	@Override void proof(Object cTop) {}\n" +
				"	@Override void five(C cEx) {}\n" +
				"	@Override <M> void six(C cEx) {}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in Errors.java (at line 5)\n" + 
			"	ex.five(\"eclipse\");\n" + 
			"	   ^^^^\n" + 
			"The method five(String) is ambiguous for the type Ex<String>\n" + 
			"----------\n" + 
			"2. ERROR in Errors.java (at line 6)\n" + 
			"	ex.six(\"eclipse\");\n" + 
			"	   ^^^\n" + 
			"The method six(String) is ambiguous for the type Ex<String>\n" + 
			"----------\n" + 
			"3. WARNING in Errors.java (at line 7)\n" + 
			"	Ex ex2 = ex;\n" + 
			"	^^\n" + 
			"Ex is a raw type. References to generic type Ex<C> should be parameterized\n" + 
			"----------\n" + 
			"4. ERROR in Errors.java (at line 9)\n" + 
			"	ex2.five(\"eclipse\");\n" + 
			"	    ^^^^\n" + 
			"The method five(Object) is ambiguous for the type Ex\n" + 
			"----------\n" + 
			"5. ERROR in Errors.java (at line 10)\n" + 
			"	ex2.six(\"eclipse\");\n" + 
			"	    ^^^\n" + 
			"The method six(Object) is ambiguous for the type Ex\n" + 
			"----------\n" + 
			"6. ERROR in Errors.java (at line 20)\n" + 
			"	@Override void five(C cEx) {}\n" + 
			"	               ^^^^^^^^^^^\n" + 
			"Name clash: The method five(C) of type Ex<C> has the same erasure as five(TC) of type Top<TC> but does not override it\n" + 
			"----------\n" + 
			"7. ERROR in Errors.java (at line 20)\n" + 
			"	@Override void five(C cEx) {}\n" + 
			"	               ^^^^^^^^^^^\n" + 
			mustOverrideMessage("five(C)", "Ex<C>") + 
			"----------\n" + 
			"8. ERROR in Errors.java (at line 21)\n" + 
			"	@Override <M> void six(C cEx) {}\n" + 
			"	                   ^^^^^^^^^^\n" + 
			"Name clash: The method six(C) of type Ex<C> has the same erasure as six(TC) of type Top<TC> but does not override it\n" + 
			"----------\n" + 
			"9. ERROR in Errors.java (at line 21)\n" + 
			"	@Override <M> void six(C cEx) {}\n" + 
			"	                   ^^^^^^^^^^\n" + 
			mustOverrideMessage("six(C)", "Ex<C>") + 
			"----------\n"
			// 5: reference to five is ambiguous, both method <TM>five(TC) in Top<java.lang.String> and method five(C) in Ex<java.lang.String> match
			// 6: reference to six is ambiguous, both method six(TC) in Top<java.lang.String> and method <M>six(C) in Ex<java.lang.String> match
			// 9: reference to five is ambiguous, both method <TM>five(TC) in Top and method five(C) in Ex match
			// **** 9: warning: [unchecked] unchecked call to <TM>five(TC) as a member of the raw type Top
			// 10: reference to six is ambiguous, both method six(TC) in Top and method <M>six(C) in Ex match
			// **** 10: warning: [unchecked] unchecked call to six(TC) as a member of the raw type Top
			// 20: method does not override a method from its superclass
			// 21: method does not override a method from its superclass
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104551
	public void test063() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface IStructuredContentProvider<I, E extends I> {\n" + 
				"    public E[] getElements(I inputElement);\n" + 
				"    public E[] getChildren(E parent);\n" + 
				"}\n" + 
				"\n" + 
				"public class X implements IStructuredContentProvider {\n" + 
				"// eclipse error: The type X must implement the inherited\n" + 
				"// abstract method IStructuredContentProvider.getChildren(I)\n" + 
				"\n" + 
				"    public Object[] getElements(Object inputElement) {\n" + 
				"        // eclipse error: The return type is incompatible with\n" + 
				"        // IStructuredContentProvider.getElements(Object)\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"\n" + 
				"    public Object[] getChildren(Object parent) {\n" + 
				"        // eclipse error: Name clash: The method getChildren(Object) of type\n" + 
				"        // X has the same erasure as getChildren(E) of type\n" + 
				"        // IStructuredContentProvider<I,E> but does not override it\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104551 - variation
	public void test064() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface IStructuredContentProvider<I, E extends I> {\n" + 
				"    public E[] getElements(I inputElement);\n" + 
				"    public E[] getChildren(E parent);\n" + 
				"}\n" + 
				"\n" + 
				"public class X implements IStructuredContentProvider<Object,Object> {\n" + 
				"// eclipse error: The type X must implement the inherited\n" + 
				"// abstract method IStructuredContentProvider.getChildren(I)\n" + 
				"\n" + 
				"    public Object[] getElements(Object inputElement) {\n" + 
				"        // eclipse error: The return type is incompatible with\n" + 
				"        // IStructuredContentProvider.getElements(Object)\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"\n" + 
				"    public Object[] getChildren(Object parent) {\n" + 
				"        // eclipse error: Name clash: The method getChildren(Object) of type\n" + 
				"        // X has the same erasure as getChildren(E) of type\n" + 
				"        // IStructuredContentProvider<I,E> but does not override it\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104551 - variation
	public void test065() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" + 
				"\n" + 
				"interface IStructuredContentProvider<I, E extends List<String>> {\n" + 
				"    public E[] getElements(I inputElement);\n" + 
				"    public E[] getChildren(E parent);\n" + 
				"}\n" + 
				"\n" + 
				"public class X implements IStructuredContentProvider {\n" + 
				"// eclipse error: The type X must implement the inherited\n" + 
				"// abstract method IStructuredContentProvider.getChildren(I)\n" + 
				"\n" + 
				"    public List[] getElements(Object inputElement) {\n" + 
				"        // eclipse error: The return type is incompatible with\n" + 
				"        // IStructuredContentProvider.getElements(Object)\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"\n" + 
				"    public List[] getChildren(List parent) {\n" + 
				"        // eclipse error: Name clash: The method getChildren(Object) of type\n" + 
				"        // X has the same erasure as getChildren(E) of type\n" + 
				"        // IStructuredContentProvider<I,E> but does not override it\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=103849
	public void test066() {
		this.runConformTest(
			new String[] {
				"JukeboxImpl.java",
				"public class JukeboxImpl implements Jukebox {\n" + 
				"    public <M extends Music,A extends Artist<M>> A getArtist (M music){return null;}\n" + 
				"    void test () { getArtist(new Rock()); }\n" + 
				"}\n" + 
				"interface Jukebox {\n" + 
				"	<M extends Music, A extends Artist<M>> A getArtist (M music);\n" + 
				"}\n" + 
				"interface Music {}\n" + 
				"class Rock implements Music {}\n" + 
				"interface Artist<M extends Music> {}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107098
	public void test067() {
		this.runConformTest(
			new String[] {
				"NoErrors.java",
				"public class NoErrors {\n" + 
				"    public static void main(String[] args) { new B().foo2(1, 10); }\n" + 
				"}\n" + 
				"class A<T> {\n" + 
				"	<S1 extends T> void foo2(Number t, S1 s) { System.out.print(false); }\n" + 
				"}\n" + 
				"class B extends A<Number> {\n" + 
				"	<S2 extends Number> void foo2(Number t, S2 s) { System.out.print(true); }\n" + 
				"}\n"
			},
			"true");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107681
	public void test068() {
		this.runConformTest(
			new String[] {
				"ReflectionNavigator.java",
				"import java.lang.reflect.Type;\n" +
				"public class ReflectionNavigator implements Navigator<Type> {\n" + 
				"    public <T> Class<T> erasure(Type t) { return null; }\n" + 
				"}\n" + 
				"interface Navigator<TypeT> {\n" + 
				"	<T> TypeT erasure(TypeT x);\n" + 
				"}\n" + 
				"class Usage {\n" + 
				"	public void foo(ReflectionNavigator r, Type t) { r.erasure(t); }\n" + 
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108203
	public void test069() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.reflect.Type;\n" +
				"public class X implements I<A> {\n" + 
				"    public <N extends A> void x1() {}\n" + 
				"    public <N extends Number> void x2() {}\n" + 
				"    public <N extends Number> void x3() {}\n" + 
				"}\n" + 
				"interface I<V> {\n" + 
				"	<N extends V> void x1();\n" + 
				"	<N extends String> void x2();\n" + 
				"	<N extends Object> void x3();\n" + 
				"}\n" + 
				"class A {}\n" + 
				"class B<T> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\r\n" + 
			"	public class X implements I<A> {\r\n" + 
			"	             ^\n" + 
			"The type X must implement the inherited abstract method I<A>.x3()\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\r\n" + 
			"	public class X implements I<A> {\r\n" + 
			"	             ^\n" + 
			"The type X must implement the inherited abstract method I<A>.x2()\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\r\n" + 
			"	public <N extends Number> void x2() {}\r\n" + 
			"	                               ^^^^\n" + 
			"Name clash: The method x2() of type X has the same erasure as x2() of type I<V> but does not override it\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\r\n" + 
			"	public <N extends Number> void x3() {}\r\n" + 
			"	                               ^^^^\n" + 
			"Name clash: The method x3() of type X has the same erasure as x3() of type I<V> but does not override it\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 9)\r\n" + 
			"	<N extends String> void x2();\r\n" + 
			"	           ^^^^^^\n" + 
			"The type parameter N should not be bounded by the final type String. Final types cannot be further extended\n" + 
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101049
	public void test070() {
		this.runConformTest(
			true,
			new String[] {
				"BooleanFactory.java",
				"interface Factory<T> {\n" +
				"	<U extends T> U create(Class<U> c);\n" + 
				"}\n" + 
				"public class BooleanFactory implements Factory<Boolean> {\n" + 
				"	public <U extends Boolean> U create(Class<U> c) {\n" + 
				"		try { return c.newInstance(); } catch(Exception e) { return null; }\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in BooleanFactory.java (at line 5)\n" + 
			"	public <U extends Boolean> U create(Class<U> c) {\n" + 
			"	                  ^^^^^^^\n" + 
			"The type parameter U should not be bounded by the final type Boolean. Final types cannot be further extended\n" + 
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107045
	public void test071() {
		this.runNegativeTest(
			new String[] {
				"D.java",
				"class D extends B<Integer> {\n" +
				"	@Override void m(Number t) {}\n" + 
				"	@Override void m(Integer t) {}\n" + 
				"}\n" + 
				"class A<T extends Number> { void m(T t) {} }\n" +
				"class B<S extends Integer> extends A<S> { @Override void m(S t) {} }"
			},
			"----------\n" +
			"1. ERROR in D.java (at line 2)\r\n" + 
			"	@Override void m(Number t) {}\r\n" + 
			"	               ^^^^^^^^^^^\n" + 
			"Name clash: The method m(Number) of type D has the same erasure as m(T) of type A<T> but does not override it\n" + 
			"----------\n" + 
			"2. ERROR in D.java (at line 2)\r\n" + 
			"	@Override void m(Number t) {}\r\n" + 
			"	               ^^^^^^^^^^^\n" + 
			mustOverrideMessage("m(Number)", "D") + 
			"----------\n" + 
			"3. WARNING in D.java (at line 6)\r\n" + 
			"	class B<S extends Integer> extends A<S> { @Override void m(S t) {} }\r\n" + 
			"	                  ^^^^^^^\n" + 
			"The type parameter S should not be bounded by the final type Integer. Final types cannot be further extended\n" + 
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108780
	public void test072() {
		this.runConformTest(
			new String[] {
				"B.java",
				"class A<E> { E foo(E e) { return null; } }\n" + 
				"class B<T> extends A<T> {\n" +
				"	@Override T foo(Object arg0) { return null; }\n" +
				"}"
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111350
	public void test073() {
		this.runConformTest(
			new String[] {
				"NumericArray.java",
				"class Array<T> {\n" + 
				"	public void add(T t) { System.out.println(false); }\n" + 
				"}\n" + 
				"public class NumericArray<T extends Number> extends Array<T> {\n" +
				"	public static void main(String[] s) { new NumericArray<Integer>().add(1); }\n" +
				"	@Override public void add(Number n) { System.out.println(true); }\n" +
				"}"
			},
			"true"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111350
	public void test073a() {
		this.runConformTest(
			new String[] {
				"NumericArray2.java",
				"class Array<T> {\n" + 
				"	public T add(T t) { System.out.println(false); return null; }\n" + 
				"}\n" + 
				"public class NumericArray2<T extends Number> extends Array<T> {\n" +
				"	public static void main(String[] s) { new NumericArray2<Integer>().add(1); }\n" +
				"	@Override public T add(Number n) { System.out.println(true); return null; }\n" +
				"}"
			},
			"true"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111350
	public void test073b() {
		this.runConformTest(
			new String[] {
				"NumericArray3.java",
				"class Array<T> {\n" + 
				"	public <U extends Number> void add(U u) {}\n" + 
				"}\n" + 
				"public class NumericArray3<T extends Number> extends Array<T> {\n" +
				"	public static void main(String[] s) { new NumericArray3<Integer>().add(1); }\n" +
				"	@Override public void add(Number n) { System.out.println(true); }\n" +
				"}"
			},
			"true"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111350
	public void test073c() {
		this.runNegativeTest(
			new String[] {
				"NumericArray4.java",
				"class Array<T> {\n" + 
				"	public <U> void add(T t) {}\n" + 
				"}\n" + 
				"public class NumericArray4<T extends Number> extends Array<T> {\n" +
				"	@Override public <U> void add(Number n) {}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in NumericArray4.java (at line 5)\n" + 
			"	@Override public <U> void add(Number n) {}\n" + 
			"	                          ^^^^^^^^^^^^^\n" + 
			mustOverrideMessage("add(Number)", "NumericArray4<T>") + 
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111350
	public void test073d() {
		this.runNegativeTest(
			new String[] {
				"NumericArray5.java",
				"class Array<T> {\n" + 
				"	public <U> void add(T t, U u) {}\n" + 
				"}\n" + 
				"public class NumericArray5<T extends Number> extends Array<T> {\n" +
				"	@Override public void add(Number n, Integer i) {}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in NumericArray5.java (at line 5)\r\n" + 
			"	@Override public void add(Number n, Integer i) {}\r\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			mustOverrideMessage("add(Number, Integer)", "NumericArray5<T>") + 
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100970
	public void test074() {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"interface I {}\n" +
				"interface J extends I { @Override void clone(); }"
			},
			"----------\n" + 
			"1. WARNING in I.java (at line 2)\n" + 
			"	interface J extends I { @Override void clone(); }\n" + 
			"	                                  ^^^^\n" + 
			"The return type is incompatible with Object.clone(), thus this interface cannot be implemented\n" + 
			"----------\n" + 
			"2. ERROR in I.java (at line 2)\n" + 
			"	interface J extends I { @Override void clone(); }\n" + 
			"	                                       ^^^^^^^\n" + 
			mustOverrideMessage("clone()", "J") + 
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100970
	public void test074a() {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"interface I { @Override void clone(); }\n" +
				"interface J extends I {}"
			},
			"----------\n" + 
			"1. WARNING in I.java (at line 1)\n" + 
			"	interface I { @Override void clone(); }\n" + 
			"	                        ^^^^\n" + 
			"The return type is incompatible with Object.clone(), thus this interface cannot be implemented\n" + 
			"----------\n" + 
			"2. ERROR in I.java (at line 1)\n" + 
			"	interface I { @Override void clone(); }\n" + 
			"	                             ^^^^^^^\n" + 
			mustOverrideMessage("clone()", "I") + 
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100970
	public void test074b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"interface I {\n" +
				"	int finalize();\n" +
				"	float hashCode();\n" +
				"}\n" +
				"interface J extends I {}\n" +
				"abstract class A implements J {}"
			},
			"----------\n" + 
			"1. WARNING in A.java (at line 2)\n" + 
			"	int finalize();\n" + 
			"	^^^\n" + 
			"The return type is incompatible with Object.finalize(), thus this interface cannot be implemented\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 3)\n" + 
			"	float hashCode();\n" + 
			"	^^^^^\n" + 
			"The return type is incompatible with Object.hashCode()\n" + 
			"----------\n" + 
			"3. ERROR in A.java (at line 6)\n" + 
			"	abstract class A implements J {}\n" + 
			"	               ^\n" + 
			"The return type is incompatible with I.finalize(), Object.finalize()\n" + 
			"----------\n" + 
			"4. ERROR in A.java (at line 6)\n" + 
			"	abstract class A implements J {}\n" + 
			"	               ^\n" + 
			"The return type is incompatible with I.hashCode(), Object.hashCode()\n" + 
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107105
	public void test075() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A { <T, S extends J & I<T>> void foo() { } }\n" +
				"class B extends A { @Override <T1, S1 extends J & I<S1>> void foo() { } }\n" + // fails, name clash only shows up when Override is removed
				"class C extends A { @Override <T2, S2 extends J & I> void foo() { } }\n" + // fails, name clash only shows up when Override is removed
				"class D extends A { @Override <T3, S3 extends J & I<T3>> void foo() { } }\n" +
				"class E extends A { @Override <T4, S4 extends I<T4> & J> void foo() { } }\n" +
				"interface I<TT> {}\n" +
				"interface J {}"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\n" + 
			"	class B extends A { @Override <T1, S1 extends J & I<S1>> void foo() { } }\n" + 
			"	                                                              ^^^^^\n" + 
			"Name clash: The method foo() of type B has the same erasure as foo() of type A but does not override it\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 2)\n" + 
			"	class B extends A { @Override <T1, S1 extends J & I<S1>> void foo() { } }\n" + 
			"	                                                              ^^^^^\n" + 
			mustOverrideMessage("foo()", "B") + 
			"----------\n" + 
			"3. WARNING in A.java (at line 3)\n" + 
			"	class C extends A { @Override <T2, S2 extends J & I> void foo() { } }\n" + 
			"	                                                  ^\n" + 
			"I is a raw type. References to generic type I<TT> should be parameterized\n" + 
			"----------\n" + 
			"4. ERROR in A.java (at line 3)\n" + 
			"	class C extends A { @Override <T2, S2 extends J & I> void foo() { } }\n" + 
			"	                                                          ^^^^^\n" + 
			"Name clash: The method foo() of type C has the same erasure as foo() of type A but does not override it\n" + 
			"----------\n" + 
			"5. ERROR in A.java (at line 3)\n" + 
			"	class C extends A { @Override <T2, S2 extends J & I> void foo() { } }\n" + 
			"	                                                          ^^^^^\n" +  
			mustOverrideMessage("foo()", "C") +
			"----------\n"
			// A.java:2: method does not override a method from its superclass
			// A.java:3: method does not override a method from its superclass
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107105
	public void test075a() {
		this.runConformTest(
			// there is no name clash in this case AND no override error - there would be if the annotation was present
			new String[] {
				"A.java",
				"class A<U> { <S extends J> void foo(U u, S s) { } }\n" +
				"class B<V> extends A<V> { <S1 extends K> void foo(V v, S1 s) { } }\n" +
				"interface J {}\n" +
				"interface K extends J {}"
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=107105
	public void test075b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A<U> { <T, S extends J & I<T>> void foo(U u, T t, S s) { } }\n" +
				"class B<V> extends A<V> { @Override <T1, S1 extends K & I<T1>> void foo(V v, T1 t, S1 s) { } }\n" +
				"interface I<TT> {}\n" +
				"interface J {}\n" +
				"interface K extends J {}"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	class B<V> extends A<V> { @Override <T1, S1 extends K & I<T1>> void foo(V v, T1 t, S1 s) { } }\r\n" + 
			"	                                                                    ^^^^^^^^^^^^^^^^^^^^\n" + 
			mustOverrideMessage("foo(V, T1, S1)", "B<V>") + 
			"----------\n"
			// A.java:2: method does not override a method from its superclass
		);
	}
	public void test076() {
		this.runConformTest(
			new String[] {
				"A.java",
				"class A {\n" +
				"	<T, S extends J & I<S>> void foo(S s) { }\n" +
				"	<T, S extends I<T> & J > void foo(S s) { }\n" +
				"}\n" +
				"interface I<TT> {}\n" +
				"interface J {}\n"
			},
			""
		);
	}
	public void test076a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {\n" +
				"	<T, S extends J & I<T>> void foo() { }\n" +
				"	<T, S extends I<T> & J> void foo() { }\n" +
				"}\n" +
				"interface I<TT> {}\n" +
				"interface J {}\n"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	<T, S extends J & I<T>> void foo() { }\r\n" + 
			"	                             ^^^^^\n" + 
			"Duplicate method foo() in type A\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 3)\r\n" + 
			"	<T, S extends I<T> & J> void foo() { }\r\n" + 
			"	                             ^^^^^\n" + 
			"Duplicate method foo() in type A\n" + 
			"----------\n"
			// <T,S>foo() is already defined in A
		);
	}
	public void test076b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A {\n" +
				"	<T, S extends J & I<T>> void foo() { }\n" +
				"	<T, S extends I<T> & K> void foo() { }\n" +
				"}\n" +
				"interface I<TT> {}\n" +
				"interface J {}\n" +
				"interface K extends J {}"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 2)\r\n" + 
			"	<T, S extends J & I<T>> void foo() { }\r\n" + 
			"	                             ^^^^^\n" + 
			"Duplicate method foo() in type A\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 3)\r\n" + 
			"	<T, S extends I<T> & K> void foo() { }\r\n" + 
			"	                             ^^^^^\n" + 
			"Duplicate method foo() in type A\n" + 
			"----------\n"
			// name clash: <T,S>foo() and <T,S>foo() have the same erasure
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test077() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	Object o = new A<Integer>().foo(new Integer(1));\n" +
				"}\n" +
				"interface I<T1> { I<T1> foo(T1 t); }\n" +
				"interface J<T2> { J<T2> foo(T2 t); }\n" +
				"class B<T> { public A<T> foo(T t) { return new A<T>(); } }\n" +
				"class A<S> extends B<S> implements I<S>, J<S> {}"
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test077a() {
		this.runNegativeTest(
			new String[] {
				"I.java",
				"public interface I { I foo(); }\n" +
				"interface J { J foo(); }\n" +
				"interface K extends I, J { K foo(); }\n" +
				"interface L { K getI(); }\n" +
				"interface M { I getI(); }\n" +
				"interface N { J getI(); }\n" +
				"interface O extends L, M, N { K getI(); }\n" +
				"interface P extends L, M, N {}\n" +
				"class X implements L, M, N { public K getI() { return null; } }\n" +
				"abstract class Y implements L, M, N {}\n" +
				"abstract class Z implements L, M, N { public K getI() { return null; } }\n"
			},
			""
// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=241821
// Now if 1 of 3 methods is acceptable to the other 2 then no error is reported
/* See addtional comments in https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
			"----------\n" + 
			"1. ERROR in I.java (at line 3)\r\n" + 
			"	interface K extends I, J { K foo(); }\r\n" + 
			"	          ^\n" + 
			"The return type is incompatible with J.foo(), I.foo()\n" + 
			"----------\n" + 
			"2. ERROR in I.java (at line 7)\r\n" + 
			"	interface O extends L, M, N { K getI(); }\r\n" + 
			"	          ^\n" + 
			"The return type is incompatible with N.getI(), M.getI(), L.getI()\n" + 
			"----------\n" + 
			"3. ERROR in I.java (at line 8)\r\n" + 
			"	interface P extends L, M, N {}\r\n" + 
			"	          ^\n" + 
			"The return type is incompatible with N.getI(), M.getI(), L.getI()\n" + 
			"----------\n" + 
			"4. ERROR in I.java (at line 10)\r\n" + 
			"	abstract class Y implements L, M, N {}\r\n" + 
			"	               ^\n" + 
			"The return type is incompatible with N.getI(), M.getI(), L.getI()\n" + 
			"----------\n"
*/
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128560
	public void test078() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);			    
		customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
		customOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				"public abstract class X implements IAppendable {\n" + 
				"    public X append(char c) {\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"interface IAppendable {\n" + 
				"	IAppendable append(char c);\n" + 
				"}\n",
			},
			null,
			customOptions,
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public X append(char c) {\n" + 
			"	       ^\n" + 
			"The return type is incompatible with IAppendable.append(char)\n" + 
			"----------\n",
			JavacTestOptions.SKIP /* we are altering the compatibility settings */);		
	}			
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=81222
	public void test079() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"class A<E> { void x(A<String> s) {} }\n" +
				"class B extends A { void x(A<String> s) {} }\n" +
				"class C extends A { @Override void x(A s) {} }\n" +
				"class D extends A { void x(A<Object> s) {} }"
			},
			"----------\n" + 
			"1. WARNING in A.java (at line 2)\r\n" + 
			"	class B extends A { void x(A<String> s) {} }\r\n" + 
			"	                ^\n" + 
			"A is a raw type. References to generic type A<E> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 2)\r\n" + 
			"	class B extends A { void x(A<String> s) {} }\r\n" + 
			"	                         ^^^^^^^^^^^^^^\n" + 
			"Name clash: The method x(A<String>) of type B has the same erasure as x(A) of type A but does not override it\n" + 
			"----------\n" + 
			"3. WARNING in A.java (at line 3)\r\n" + 
			"	class C extends A { @Override void x(A s) {} }\r\n" + 
			"	                ^\n" + 
			"A is a raw type. References to generic type A<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in A.java (at line 3)\r\n" + 
			"	class C extends A { @Override void x(A s) {} }\r\n" + 
			"	                                     ^\n" + 
			"A is a raw type. References to generic type A<E> should be parameterized\n" + 
			"----------\n" + 
			"5. WARNING in A.java (at line 4)\r\n" + 
			"	class D extends A { void x(A<Object> s) {} }\r\n" + 
			"	                ^\n" + 
			"A is a raw type. References to generic type A<E> should be parameterized\n" + 
			"----------\n" + 
			"6. ERROR in A.java (at line 4)\r\n" + 
			"	class D extends A { void x(A<Object> s) {} }\r\n" + 
			"	                         ^^^^^^^^^^^^^^\n" + 
			"Name clash: The method x(A<Object>) of type D has the same erasure as x(A) of type A but does not override it\n" + 
			"----------\n"
			// name clash: x(A<java.lang.String>) in B and x(A<java.lang.String>) in A have the same erasure, yet neither overrides the other
			// name clash: x(A<java.lang.Object>) in D and x(A<java.lang.String>) in A have the same erasure, yet neither overrides the other
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=106880
	public void test080() {
		this.runNegativeTest(
			new String[] {
				"HashOrder.java",
				"public class HashOrder extends DoubleHash<String> {\n" +
				"	public static HashOrder create() { return null; }\n" +
				"}\n" +
				"class DoubleHash<T> {\n" +
				"	public static <U> DoubleHash<U> create() { return null; }\n" +
				"}"
			},
			"----------\n" + 
			"1. WARNING in HashOrder.java (at line 2)\n" + 
			"	public static HashOrder create() { return null; }\n" + 
			"	              ^^^^^^^^^\n" + 
			"Type safety: The return type HashOrder for create() from the type HashOrder needs unchecked conversion to conform to DoubleHash<Object> from the type DoubleHash<String>\n" + 
			"----------\n"
			// warning: create() in HashOrder overrides <U>create() in DoubleHash; return type requires unchecked conversion
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=125956
	public void test081() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X<U> implements I {\n" +
				"	public A<String> foo() { return null; }\n" +
				"	public <S> A<U> bar() { return null; }\n" +
				"}\n" +
				"interface I {\n" +
				"	<T> A<T> foo();\n" +
				"	<S> A<S> bar();\n" +
				"}\n" +
				"class A<V> {}"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\r\n" + 
			"	public A<String> foo() { return null; }\r\n" + 
			"	       ^\n" + 
			"Type safety: The return type A<String> for foo() from the type X<U> needs unchecked conversion to conform to A<Object> from the type I\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\r\n" + 
			"	public <S> A<U> bar() { return null; }\r\n" + 
			"	           ^^^^\n" + 
			"The return type is incompatible with I.bar()\n" + 
			"----------\n"
			// <S>bar() in X cannot implement <S>bar() in I; attempting to use incompatible return type
			// warning: foo() in X implements <T>foo() in I; return type requires unchecked conversion
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105339
	public void test082() {
		this.runNegativeTest(
			new String[] {
				"V.java",
				"public class V extends U { @Override public C<B> foo() { return null; } }\n" +
				"class U { public <T extends A> C<T> foo() { return null; } }\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C<T> {}"
			},
			"----------\n" + 
			"1. WARNING in V.java (at line 1)\n" + 
			"	public class V extends U { @Override public C<B> foo() { return null; } }\n" + 
			"	                                            ^\n" + 
			"Type safety: The return type C<B> for foo() from the type V needs unchecked conversion to conform to C<A> from the type U\n" + 
			"----------\n"
			// warning: foo() in V overrides <T>foo() in U; return type requires unchecked conversion
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132831
	public void test083() {
		this.runConformTest(
			new String[] {
				"C.java",
				"public class C extends p.B {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(((p.I) new C()).m() == null);\n" +
				"	}\n" +
				"}",
				"p/B.java",
				"package p;\n" +
				"public abstract class B extends A {}\n" +
				"abstract class A implements I {\n" +
				"	public A m() { return null; }\n" +
				"}",
				"p/I.java",
				"package p;\n" +
				"public interface I { I m(); }\n"
			},
			"true"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132841
	public void test084() {
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A<T1 extends A.M> implements I<T1> {\n" +
				"	public java.util.List<T1> f(M n) { return null; }\n" +
				"	static class M {}\n" +
				"}\n" +
				"interface I<T2> {\n" +
				"	java.util.List<T2> f(T2 t);\n" +
				"}"
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132841
	public void test084a() {
		this.runConformTest(
			new String[] {
				"A.java",
				"public class A<T1 extends A.M> implements I<T1> {\n" +
				"	public void foo(Number n, M m) {}\n" +
				"	public void foo2(Number n, M m) {}\n" +
				"	public void foo3(Number n, M m) {}\n" +
				"	static class M {}\n" +
				"}\n" +
				"interface I<T2> {\n" +
				"	<U extends Number> void foo(U u, T2 t);\n" +
				"	void foo2(Number n, T2 t);\n" +
				"	<U extends Number> void foo3(U u, A.M m);\n" +
				"}"
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=132841
	public void test084b() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"public class A<T1 extends A.M> implements I<T1> {\n" +
				"	public void foo4(Number n, T1 m) {}\n" +
				"	static class M {}\n" +
				"}\n" +
				"interface I<T2> {\n" +
				"	<U extends Number> void foo4(U u, A.M m);\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in A.java (at line 1)\r\n" + 
			"	public class A<T1 extends A.M> implements I<T1> {\r\n" + 
			"	             ^\n" + 
			"The type A<T1> must implement the inherited abstract method I<T1>.foo4(U, A.M)\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 2)\r\n" + 
			"	public void foo4(Number n, T1 m) {}\r\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo4(Number, T1) of type A<T1> has the same erasure as foo4(U, A.M) of type I<T2> but does not override it\n" + 
			"----------\n"
			// A is not abstract and does not override abstract method <U>foo4(U,A.M) in I
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=136543
	public void test085() {
		this.runNegativeTest(
			new String[] {
				"Parent.java",
				"import java.util.Collection;\n" +
				"public class Parent {\n" +
				"	static void staticCase1(Collection c) {}\n" +
				"	static void staticCase2(Collection<String> c) {}\n" +
				"	void instanceCase1(Collection c) {}\n" +
				"	void instanceCase2(Collection<String> c) {}\n" +
				"}\n" +
				"class Child extends Parent {\n" +
				"	static void staticCase1(Collection<String> c) {}\n" +
				"	static void staticCase2(Collection c) {}\n" +
				"	void instanceCase1(Collection<String> c) {}\n" +
				"	@Override void instanceCase2(Collection c) {}\n" +
				"}"
			},
			"----------\n" + 
			"1. WARNING in Parent.java (at line 3)\n" + 
			"	static void staticCase1(Collection c) {}\n" + 
			"	                        ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Parent.java (at line 5)\n" + 
			"	void instanceCase1(Collection c) {}\n" + 
			"	                   ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in Parent.java (at line 10)\n" + 
			"	static void staticCase2(Collection c) {}\n" + 
			"	                        ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"4. ERROR in Parent.java (at line 11)\n" + 
			"	void instanceCase1(Collection<String> c) {}\n" + 
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method instanceCase1(Collection<String>) of type Child has the same erasure as instanceCase1(Collection) of type Parent but does not override it\n" + 
			"----------\n" + 
			"5. WARNING in Parent.java (at line 12)\n" + 
			"	@Override void instanceCase2(Collection c) {}\n" + 
			"	                             ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n"
			// @Override is an error for instanceCase1
			// name clash: instanceCase1(Collection<String>) in Child and instanceCase1(Collection) in Parent have the same erasure, yet neither overrides the other
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=136543 - case 2
	public void test085b() {
		this.runNegativeTest(
			new String[] {
				"Parent.java",
				"import java.util.Collection;\n" +
				"public class Parent {\n" +
				"	static void staticMismatchCase1(Collection c) {}\n" +
				"	static void staticMismatchCase2(Collection<String> c) {}\n" +
				"	void mismatchCase1(Collection c) {}\n" +
				"	void mismatchCase2(Collection<String> c) {}\n" +
				"}\n" +
				"class Child extends Parent {\n" +
				"	void staticMismatchCase1(Collection c) {}\n" +
				"	void staticMismatchCase2(Collection<String> c) {}\n" +
				"	static void mismatchCase1(Collection c) {}\n" +
				"	static void mismatchCase2(Collection<String> c) {}\n" +
				"}"
			},
			"----------\n" + 
			"1. WARNING in Parent.java (at line 3)\r\n" + 
			"	static void staticMismatchCase1(Collection c) {}\r\n" + 
			"	                                ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in Parent.java (at line 5)\r\n" + 
			"	void mismatchCase1(Collection c) {}\r\n" + 
			"	                   ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"3. ERROR in Parent.java (at line 9)\r\n" + 
			"	void staticMismatchCase1(Collection c) {}\r\n" + 
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"This instance method cannot override the static method from Parent\n" + 
			"----------\n" + 
			"4. WARNING in Parent.java (at line 9)\r\n" + 
			"	void staticMismatchCase1(Collection c) {}\r\n" + 
			"	                         ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"5. ERROR in Parent.java (at line 10)\r\n" + 
			"	void staticMismatchCase2(Collection<String> c) {}\r\n" + 
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"This instance method cannot override the static method from Parent\n" + 
			"----------\n" + 
			"6. ERROR in Parent.java (at line 11)\r\n" + 
			"	static void mismatchCase1(Collection c) {}\r\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"This static method cannot hide the instance method from Parent\n" + 
			"----------\n" + 
			"7. WARNING in Parent.java (at line 11)\r\n" + 
			"	static void mismatchCase1(Collection c) {}\r\n" + 
			"	                          ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"8. ERROR in Parent.java (at line 12)\r\n" + 
			"	static void mismatchCase2(Collection<String> c) {}\r\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"This static method cannot hide the instance method from Parent\n" + 
			"----------\n"
			// staticMismatchCase1(java.util.Collection) in Child cannot override staticMismatchCase1(java.util.Collection) in Parent; overridden method is static
			// staticMismatchCase2(java.util.Collection<java.lang.String>) in Child cannot override staticMismatchCase2(java.util.Collection<java.lang.String>) in Parent; overridden method is static
			// mismatchCase1(java.util.Collection) in Child cannot override mismatchCase1(java.util.Collection) in Parent; overriding method is static
			// mismatchCase2(java.util.Collection<java.lang.String>) in Child cannot override mismatchCase2(java.util.Collection<java.lang.String>) in Parent; overriding method is static
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=136543 - case 3
	public void test085c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract class X<V> extends CX<V> implements IX<V> {}\n" +
				"class CX<T> { public static void foo(Object o) {} }\n" +
				"abstract class X2 extends CX implements IX {}\n" +
				"interface IX<U> { void foo(U u); }"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public abstract class X<V> extends CX<V> implements IX<V> {}\n" + 
			"	                      ^\n" + 
			"The static method foo(Object) conflicts with the abstract method in IX<V>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\r\n" + 
			"	abstract class X2 extends CX implements IX {}\r\n" + 
			"	               ^^\n" + 
			"The static method foo(Object) conflicts with the abstract method in IX\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 3)\r\n" + 
			"	abstract class X2 extends CX implements IX {}\r\n" + 
			"	                          ^^\n" + 
			"CX is a raw type. References to generic type CX<T> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 3)\r\n" + 
			"	abstract class X2 extends CX implements IX {}\r\n" + 
			"	                                        ^^\n" + 
			"IX is a raw type. References to generic type IX<U> should be parameterized\n" + 
			"----------\n"
			// line 1: foo(java.lang.Object) in CX cannot implement foo(U) in IX; overriding method is static
			// line 3: foo(java.lang.Object) in CX cannot implement foo(U) in IX; overriding method is static
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90438
	public void test086() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements I { public <T extends Object & Data> void copyData(T data) {} }\n" +
				"interface I { <A extends Data> void copyData(A data); }\n" +
				"interface Data {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X implements I { public <T extends Object & Data> void copyData(T data) {} }\n" + 
			"	             ^\n" + 
			"The type X must implement the inherited abstract method I.copyData(A)\n" + 
			"----------\n"
			// X is not abstract and does not override abstract method <A>copyData(A) in I
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90438 - case 2
	public void test086b() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X implements I { public <T> G<T> foo(Class<T> stuffClass) { return null; } }\n" +
				"interface I { <T extends Object> G<T> foo(Class<T> stuffClass); }\n" +
				"class G<T> {}"
			},
			""
		);
	}
	public void test087() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.Collection;\n" + 
				"\n" + 
				"interface Interface1 {\n" + 
				"}\n" + 
				"interface Interface2 extends Interface1 {\n" + 
				"}\n" + 
				"interface Interface3 {\n" + 
				"    <P extends Interface1> Collection<P> doStuff();\n" + 
				"}\n" + 
				"interface Interface4 extends Interface3 {\n" + 
				"    Collection<Interface2> doStuff();\n" + 
				"}\n" +
				"public class X {\n" + 
				"    Zork z;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 11)\r\n" + 
			"	Collection<Interface2> doStuff();\r\n" + 
			"	^^^^^^^^^^\n" + 
			"Type safety: The return type Collection<Interface2> for doStuff() from the type Interface4 needs unchecked conversion to conform to Collection<Interface1> from the type Interface3\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\r\n" + 
			"	Zork z;\r\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n"
		);
	}	
	//	https://bugs.eclipse.org/bugs/show_bug.cgi?id=142653 - variation
	public void test088() {
		this.runNegativeTest(
			new String[] {
				"X.java",//===================
				"import java.util.*;\n" +
				"public class X<T0> extends ArrayList<T0> implements I<T0>,Runnable {\n" + 
				"	\n" + 
				"	void foo() {\n" + 
				"		this.add(new Object());\n" + 
				"		this.add(null);\n" + 
				"	}\n" + 
				"}\n" + 
				"interface I<T1> extends Collection<String> {\n" + 
				"}\n" , // =================, // =================			
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public class X<T0> extends ArrayList<T0> implements I<T0>,Runnable {\n" + 
			"	             ^\n" + 
			"The interface Collection cannot be implemented more than once with different arguments: Collection<T0> and Collection<String>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	public class X<T0> extends ArrayList<T0> implements I<T0>,Runnable {\n" + 
			"	             ^\n" + 
			"The type X<T0> must implement the inherited abstract method Runnable.run()\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 2)\n" + 
			"	public class X<T0> extends ArrayList<T0> implements I<T0>,Runnable {\n" + 
			"	             ^\n" + 
			"The serializable class X does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\n" + 
			"	this.add(new Object());\n" + 
			"	     ^^^\n" + 
			"The method add(T0) in the type ArrayList<T0> is not applicable for the arguments (Object)\n" + 
			"----------\n"
		);
	}
	//	https://bugs.eclipse.org/bugs/show_bug.cgi?id=142653 - variation
	public void test089() {
		this.runNegativeTest(
			new String[] {
				"X.java",//===================
				"import java.util.*;\n" + 
				"public class X extends X2 {}\n" + 
				"abstract class X2 extends X3 implements List<String> {}\n" + 
				"abstract class X3 implements List<Thread> {}", // =================
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	abstract class X2 extends X3 implements List<String> {}\n" + 
			"	               ^^\n" + 
			"The interface List cannot be implemented more than once with different arguments: List<Thread> and List<String>\n" + 
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=147690
	public void test090() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"class XSuper {\n" + 
				"	Object foo() throws Exception { return null; }\n" + 
				"	protected Object bar() throws Exception { return null; }\n" + 
				"}\n" + 
				"public class X extends XSuper {\n" + 
				"	protected String foo() { return null; }\n" + 
				"	public String bar() { return null; }\n" + 
				"}", // =================
			},
			"");
		// 	ensure bridge methods have target method modifiers, and inherited thrown exceptions
		String expectedOutput =
			"  // Method descriptor #17 ()Ljava/lang/Object;\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  public bridge synthetic java.lang.Object bar() throws java.lang.Exception;\n" + 
			"    0  aload_0\n" + 
			"    1  invokevirtual X.bar() : java.lang.String [21]\n" + 
			"    4  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"  \n" + 
			"  // Method descriptor #17 ()Ljava/lang/Object;\n" + 
			"  // Stack: 1, Locals: 1\n" + 
			"  protected bridge synthetic java.lang.Object foo() throws java.lang.Exception;\n" + 
			"    0  aload_0\n" + 
			"    1  invokevirtual X.foo() : java.lang.String [23]\n" + 
			"    4  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n";
		
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148783	
	public void test091() {
		this.runNegativeTest(
			new String[] {
				"DataSet.java",//===================
				"import java.io.Serializable;\n" + 
				"import java.util.*;\n" + 
				"\n" + 
				"class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
				"	\n" + 
				"	public <S> S[] toArray(S[] s) {\n" + 
				"		return s;\n" + 
				"	}\n" + 
				"\n" + 
				"	public boolean add(Object o) { return false; }\n" + 
				"	public void add(int index, Object element) {}\n" + 
				"	public boolean addAll(Collection c) {	return false; }\n" + 
				"	public boolean addAll(int index, Collection c) {	return false; }\n" + 
				"	public void clear() {}\n" + 
				"	public boolean contains(Object o) {	return false; }\n" + 
				"	public boolean containsAll(Collection c) { return false; }\n" + 
				"	public Object get(int index) { return null; }\n" + 
				"	public int indexOf(Object o) { return 0; }\n" + 
				"	public boolean isEmpty() {	return false; }\n" + 
				"	public Iterator iterator() {	return null; }\n" + 
				"	public int lastIndexOf(Object o) {	return 0; }\n" + 
				"	public ListIterator listIterator() {	return null; }\n" + 
				"	public ListIterator listIterator(int index) {	return null; }\n" + 
				"	public boolean remove(Object o) {	return false; }\n" + 
				"	public Object remove(int index) {	return null; }\n" + 
				"	public boolean removeAll(Collection c) {	return false; }\n" + 
				"	public boolean retainAll(Collection c) {	return false; }\n" + 
				"	public Object set(int index, Object element) {	return false; }\n" + 
				"	public int size() {	return 0; }\n" + 
				"	public List subList(int fromIndex, int toIndex) {	return null; }\n" + 
				"	public Object[] toArray() {	return null; }\n" + 
				"	public boolean hasNext() {	return false; }\n" + 
				"	public Object next() {	return null; }\n" + 
				"	public void remove() {}\n" + 
				"}\n", // =================
			},
			"----------\n" + 
			"1. ERROR in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	      ^^^^^^^\n" + 
			"The type DataSet<T> must implement the inherited abstract method List.toArray(Object[])\n" + 
			"----------\n" + 
			"2. WARNING in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	      ^^^^^^^\n" + 
			"The serializable class DataSet does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"3. WARNING in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	                                           ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	                                                 ^^^^^^^^\n" + 
			"Iterator is a raw type. References to generic type Iterator<E> should be parameterized\n" + 
			"----------\n" + 
			"5. ERROR in DataSet.java (at line 6)\n" + 
			"	public <S> S[] toArray(S[] s) {\n" + 
			"	               ^^^^^^^^^^^^^^\n" + 
			"Name clash: The method toArray(S[]) of type DataSet<T> has the same erasure as toArray(Object[]) of type List but does not override it\n" + 
			"----------\n" + 
			"6. ERROR in DataSet.java (at line 6)\n" + 
			"	public <S> S[] toArray(S[] s) {\n" + 
			"	               ^^^^^^^^^^^^^^\n" + 
			"Name clash: The method toArray(S[]) of type DataSet<T> has the same erasure as toArray(Object[]) of type Collection but does not override it\n" + 
			"----------\n" + 
			"7. WARNING in DataSet.java (at line 12)\n" + 
			"	public boolean addAll(Collection c) {	return false; }\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"8. WARNING in DataSet.java (at line 13)\n" + 
			"	public boolean addAll(int index, Collection c) {	return false; }\n" + 
			"	                                 ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"9. WARNING in DataSet.java (at line 16)\n" + 
			"	public boolean containsAll(Collection c) { return false; }\n" + 
			"	                           ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"10. WARNING in DataSet.java (at line 20)\n" + 
			"	public Iterator iterator() {	return null; }\n" + 
			"	       ^^^^^^^^\n" + 
			"Iterator is a raw type. References to generic type Iterator<E> should be parameterized\n" + 
			"----------\n" + 
			"11. WARNING in DataSet.java (at line 22)\n" + 
			"	public ListIterator listIterator() {	return null; }\n" + 
			"	       ^^^^^^^^^^^^\n" + 
			"ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized\n" + 
			"----------\n" + 
			"12. WARNING in DataSet.java (at line 23)\n" + 
			"	public ListIterator listIterator(int index) {	return null; }\n" + 
			"	       ^^^^^^^^^^^^\n" + 
			"ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized\n" + 
			"----------\n" + 
			"13. WARNING in DataSet.java (at line 26)\n" + 
			"	public boolean removeAll(Collection c) {	return false; }\n" + 
			"	                         ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"14. WARNING in DataSet.java (at line 27)\n" + 
			"	public boolean retainAll(Collection c) {	return false; }\n" + 
			"	                         ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"15. WARNING in DataSet.java (at line 30)\n" + 
			"	public List subList(int fromIndex, int toIndex) {	return null; }\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n"
		);
	}	
	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148783 - variation
	public void test092() {
		this.runNegativeTest(
			new String[] {
				"DataSet.java",//===================
				"import java.io.Serializable;\n" + 
				"import java.util.*;\n" + 
				"\n" + 
				"class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
				"	\n" + 
				"	public <S extends T> S[] toArray(S[] s) {\n" + 
				"		return s;\n" + 
				"	}\n" + 
				"\n" + 
				"	public boolean add(Object o) { return false; }\n" + 
				"	public void add(int index, Object element) {}\n" + 
				"	public boolean addAll(Collection c) {	return false; }\n" + 
				"	public boolean addAll(int index, Collection c) {	return false; }\n" + 
				"	public void clear() {}\n" + 
				"	public boolean contains(Object o) {	return false; }\n" + 
				"	public boolean containsAll(Collection c) { return false; }\n" + 
				"	public Object get(int index) { return null; }\n" + 
				"	public int indexOf(Object o) { return 0; }\n" + 
				"	public boolean isEmpty() {	return false; }\n" + 
				"	public Iterator iterator() {	return null; }\n" + 
				"	public int lastIndexOf(Object o) {	return 0; }\n" + 
				"	public ListIterator listIterator() {	return null; }\n" + 
				"	public ListIterator listIterator(int index) {	return null; }\n" + 
				"	public boolean remove(Object o) {	return false; }\n" + 
				"	public Object remove(int index) {	return null; }\n" + 
				"	public boolean removeAll(Collection c) {	return false; }\n" + 
				"	public boolean retainAll(Collection c) {	return false; }\n" + 
				"	public Object set(int index, Object element) {	return false; }\n" + 
				"	public int size() {	return 0; }\n" + 
				"	public List subList(int fromIndex, int toIndex) {	return null; }\n" + 
				"	public Object[] toArray() {	return null; }\n" + 
				"	public boolean hasNext() {	return false; }\n" + 
				"	public Object next() {	return null; }\n" + 
				"	public void remove() {}\n" + 
				"}\n", // =================
			},
			"----------\n" + 
			"1. ERROR in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	      ^^^^^^^\n" + 
			"The type DataSet<T> must implement the inherited abstract method List.toArray(Object[])\n" + 
			"----------\n" + 
			"2. WARNING in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	      ^^^^^^^\n" + 
			"The serializable class DataSet does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"3. WARNING in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	                                           ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	                                                 ^^^^^^^^\n" + 
			"Iterator is a raw type. References to generic type Iterator<E> should be parameterized\n" + 
			"----------\n" + 
			"5. WARNING in DataSet.java (at line 12)\n" + 
			"	public boolean addAll(Collection c) {	return false; }\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"6. WARNING in DataSet.java (at line 13)\n" + 
			"	public boolean addAll(int index, Collection c) {	return false; }\n" + 
			"	                                 ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"7. WARNING in DataSet.java (at line 16)\n" + 
			"	public boolean containsAll(Collection c) { return false; }\n" + 
			"	                           ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"8. WARNING in DataSet.java (at line 20)\n" + 
			"	public Iterator iterator() {	return null; }\n" + 
			"	       ^^^^^^^^\n" + 
			"Iterator is a raw type. References to generic type Iterator<E> should be parameterized\n" + 
			"----------\n" + 
			"9. WARNING in DataSet.java (at line 22)\n" + 
			"	public ListIterator listIterator() {	return null; }\n" + 
			"	       ^^^^^^^^^^^^\n" + 
			"ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized\n" + 
			"----------\n" + 
			"10. WARNING in DataSet.java (at line 23)\n" + 
			"	public ListIterator listIterator(int index) {	return null; }\n" + 
			"	       ^^^^^^^^^^^^\n" + 
			"ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized\n" + 
			"----------\n" + 
			"11. WARNING in DataSet.java (at line 26)\n" + 
			"	public boolean removeAll(Collection c) {	return false; }\n" + 
			"	                         ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"12. WARNING in DataSet.java (at line 27)\n" + 
			"	public boolean retainAll(Collection c) {	return false; }\n" + 
			"	                         ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"13. WARNING in DataSet.java (at line 30)\n" + 
			"	public List subList(int fromIndex, int toIndex) {	return null; }\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n");
	}	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148783 - variation
	public void test093() {
		this.runNegativeTest(
			new String[] {
				"DataSet.java",//===================
				"import java.io.Serializable;\n" + 
				"import java.util.*;\n" + 
				"\n" + 
				"class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
				"	\n" + 
				"	public <S> S[] toArray(S[] s) {\n" + 
				"		return s;\n" + 
				"	}\n" + 
				"	public Object[] toArray(Object[] o) {\n" + 
				"		return o;\n" + 
				"	}\n" + 
				"	public boolean add(Object o) { return false; }\n" + 
				"	public void add(int index, Object element) {}\n" + 
				"	public boolean addAll(Collection c) {	return false; }\n" + 
				"	public boolean addAll(int index, Collection c) {	return false; }\n" + 
				"	public void clear() {}\n" + 
				"	public boolean contains(Object o) {	return false; }\n" + 
				"	public boolean containsAll(Collection c) { return false; }\n" + 
				"	public Object get(int index) { return null; }\n" + 
				"	public int indexOf(Object o) { return 0; }\n" + 
				"	public boolean isEmpty() {	return false; }\n" + 
				"	public Iterator iterator() {	return null; }\n" + 
				"	public int lastIndexOf(Object o) {	return 0; }\n" + 
				"	public ListIterator listIterator() {	return null; }\n" + 
				"	public ListIterator listIterator(int index) {	return null; }\n" + 
				"	public boolean remove(Object o) {	return false; }\n" + 
				"	public Object remove(int index) {	return null; }\n" + 
				"	public boolean removeAll(Collection c) {	return false; }\n" + 
				"	public boolean retainAll(Collection c) {	return false; }\n" + 
				"	public Object set(int index, Object element) {	return false; }\n" + 
				"	public int size() {	return 0; }\n" + 
				"	public List subList(int fromIndex, int toIndex) {	return null; }\n" + 
				"	public Object[] toArray() {	return null; }\n" + 
				"	public boolean hasNext() {	return false; }\n" + 
				"	public Object next() {	return null; }\n" + 
				"	public void remove() {}\n" + 
				"}\n", // =================
			},
			"----------\n" + 
			"1. ERROR in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	      ^^^^^^^\n" + 
			"The type DataSet<T> must implement the inherited abstract method List.toArray(Object[])\n" + 
			"----------\n" + 
			"2. WARNING in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	      ^^^^^^^\n" + 
			"The serializable class DataSet does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"3. WARNING in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	                                           ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in DataSet.java (at line 4)\n" + 
			"	class DataSet<T extends Number> implements List, Iterator, Serializable {\n" + 
			"	                                                 ^^^^^^^^\n" + 
			"Iterator is a raw type. References to generic type Iterator<E> should be parameterized\n" + 
			"----------\n" + 
			"5. ERROR in DataSet.java (at line 6)\n" + 
			"	public <S> S[] toArray(S[] s) {\n" + 
			"	               ^^^^^^^^^^^^^^\n" + 
			"Method toArray(S[]) has the same erasure toArray(Object[]) as another method in type DataSet<T>\n" + 
			"----------\n" + 
			"6. ERROR in DataSet.java (at line 9)\n" + 
			"	public Object[] toArray(Object[] o) {\n" + 
			"	                ^^^^^^^^^^^^^^^^^^^\n" + 
			"Method toArray(Object[]) has the same erasure toArray(Object[]) as another method in type DataSet<T>\n" + 
			"----------\n" + 
			"7. WARNING in DataSet.java (at line 14)\n" + 
			"	public boolean addAll(Collection c) {	return false; }\n" + 
			"	                      ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"8. WARNING in DataSet.java (at line 15)\n" + 
			"	public boolean addAll(int index, Collection c) {	return false; }\n" + 
			"	                                 ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"9. WARNING in DataSet.java (at line 18)\n" + 
			"	public boolean containsAll(Collection c) { return false; }\n" + 
			"	                           ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"10. WARNING in DataSet.java (at line 22)\n" + 
			"	public Iterator iterator() {	return null; }\n" + 
			"	       ^^^^^^^^\n" + 
			"Iterator is a raw type. References to generic type Iterator<E> should be parameterized\n" + 
			"----------\n" + 
			"11. WARNING in DataSet.java (at line 24)\n" + 
			"	public ListIterator listIterator() {	return null; }\n" + 
			"	       ^^^^^^^^^^^^\n" + 
			"ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized\n" + 
			"----------\n" + 
			"12. WARNING in DataSet.java (at line 25)\n" + 
			"	public ListIterator listIterator(int index) {	return null; }\n" + 
			"	       ^^^^^^^^^^^^\n" + 
			"ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized\n" + 
			"----------\n" + 
			"13. WARNING in DataSet.java (at line 28)\n" + 
			"	public boolean removeAll(Collection c) {	return false; }\n" + 
			"	                         ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"14. WARNING in DataSet.java (at line 29)\n" + 
			"	public boolean retainAll(Collection c) {	return false; }\n" + 
			"	                         ^^^^^^^^^^\n" + 
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
			"----------\n" + 
			"15. WARNING in DataSet.java (at line 32)\n" + 
			"	public List subList(int fromIndex, int toIndex) {	return null; }\n" + 
			"	       ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n"
		);
	}	

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146383
public void test094() {
	this.runNegativeTest(
		new String[] {
			"X.java",//===================
			"import java.util.ArrayList;\n" + 
			"import java.util.Arrays;\n" + 
			"class Y<T> {}\n" + 
			"public class X\n" + 
			"{\n" + 
			"  private static ArrayList<Y<X>> y = new ArrayList<Y<X>>();\n" + 
			"  void foo(Y[] array)\n" + 
			"  {\n" + 
			"    y.addAll(Arrays.asList(array));\n" + 
			"  }\n" + 
			"}\n", // =================
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 7)\n" + 
		"	void foo(Y[] array)\n" + 
		"	         ^\n" + 
		"Y is a raw type. References to generic type Y<T> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	y.addAll(Arrays.asList(array));\n" + 
		"	  ^^^^^^\n" + 
		"The method addAll(Collection<? extends Y<X>>) in the type ArrayList<Y<X>> is not applicable for the arguments (List<Y>)\n" + 
		"----------\n"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148957
public void test096() {
	this.runNegativeTest(
		new String[] {
			"ProblemClass.java",//===================
			"import java.util.Collection;\n" + 
			"import javax.swing.JLabel;\n" + 
			"interface SuperInterface {\n" + 
			"   public <A extends JLabel> void doIt(Collection<A> as);\n" + 
			"}\n" + 
			"\n" + 
			"public class ProblemClass implements SuperInterface {\n" + 
			"   public void doIt(Collection<? extends JLabel> as) {\n" + 
			"   }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in ProblemClass.java (at line 7)\n" + 
		"	public class ProblemClass implements SuperInterface {\n" + 
		"	             ^^^^^^^^^^^^\n" + 
		"The type ProblemClass must implement the inherited abstract method SuperInterface.doIt(Collection<A>)\n" + 
		"----------\n" + 
		"2. ERROR in ProblemClass.java (at line 8)\n" + 
		"	public void doIt(Collection<? extends JLabel> as) {\n" + 
		"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Name clash: The method doIt(Collection<? extends JLabel>) of type ProblemClass has the same erasure as doIt(Collection<A>) of type SuperInterface but does not override it\n" + 
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148957 - variation
public void test097() {
	this.runConformTest(
		new String[] {
			"ProblemClass.java",//===================
			"import java.util.Collection;\n" + 
			"import javax.swing.JLabel;\n" + 
			"interface SuperInterface {\n" + 
			"   public <A extends JLabel> void doIt(Collection<A> as);\n" + 
			"}\n" + 
			"\n" + 
			"public class ProblemClass implements SuperInterface {\n" + 
			"   public <B extends JLabel> void doIt(Collection<B> as) {\n" + 
			"   }\n" + 
			"}\n"
		},
		""
	);
}

// autoboxing mixed with type parameters substitution
public void test098() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" + 
			"public class X<A, B> {\n" + 
			"    public X(List<A> toAdd) {\n" + 
			"    }\n" + 
			"    public <L extends List<? super A>, LF extends Factory<L>> L \n" + 
			"            foo(B b, L l, LF lf) {\n" + 
			"        return l;\n" + 
			"    }\n" + 
			"    public static class ListFactory<T> implements Factory<List<T>> {\n" + 
			"        public List<T> create() {\n" + 
			"            return null;\n" + 
			"        }\n" + 
			"    }\n" + 
			"    public static interface Factory<T> {\n" + 
			"        public T create();\n" + 
			"    }\n" + 
			"    public static void bar() {\n" + 
			"        (new X<Long, Number>(new ArrayList<Long>())).\n" + 
			"            foo(1, (List<Number>) null, new ListFactory<Number>());\n" + 
			"    }\n" + 
			"}"
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=153874
public void test099() {
	Map customOptions= getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
	this.runConformTest(
		new String[] {
			"TestCharset.java",
			"import java.nio.charset.*;\n" + 
			"public class TestCharset extends Charset {\n" + 
			"	protected TestCharset(String n, String[] a) { super(n, a); }\n" + 
			"	public boolean contains(Charset cs) { return false; }\n" + 
			"	public CharsetDecoder newDecoder() { return null; }\n" + 
			"	public CharsetEncoder newEncoder() { return null; }\n" + 
			"}\n" ,
		},
		"",
		null,
		true,
		null,
		customOptions,
		null/*no custom requestor*/);
}

// name conflict
public void test100() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Collection;\n" + 
			"public class X<E> {\n" + 
			"  boolean removeAll(Collection<? extends E> c) {\n" + 
			"    return false;\n" + 
			"  }\n" + 
			"}\n",
			"Y.java",
			"import java.util.Collection;\n" + 
			"public class Y<E> extends X<E>\n" + 
			"{\n" + 
			"  <T extends E> boolean removeAll(Collection<T> c) {\n" + 
			"    return false;\n" + 
			"  }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in Y.java (at line 4)\n" + 
		"	<T extends E> boolean removeAll(Collection<T> c) {\n" + 
		"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Name clash: The method removeAll(Collection<T>) of type Y<E> has the same erasure as removeAll(Collection<? extends E>) of type X<E> but does not override it\n" + 
		"----------\n"
	);
}

// name conflict
public void test101() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" + 
			"public class X {\n" + 
			"    Integer getX(List<Integer> l) {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"    String getX(List<String> l) {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"    Integer getX(List<Integer> l) {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"    String getX(List<Integer> l) {\n" + 
			"        return null;\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 11)\n" + 
		"	Integer getX(List<Integer> l) {\n" + 
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Duplicate method getX(List<Integer>) in type Y\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 14)\n" + 
		"	String getX(List<Integer> l) {\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Duplicate method getX(List<Integer>) in type Y\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test102() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private interface ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnDerived extends ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnLeaf extends ReturnDerived {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface Interface {\n" + 
			"		ReturnBase bar();\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Implementation {\n" + 
			"		public final ReturnDerived bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Child extends Implementation implements Interface {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Grandchild extends Child implements Interface {\n" +
			"		@Override\n" + 
			"		public ReturnLeaf bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Grandchild();\n" + 
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 26)\n" + 
		"	public ReturnLeaf bar() {\n" + 
		"	                  ^^^^^\n" + 
		"Cannot override the final method from X.Implementation\n" + 
		"----------\n",
		null,
		true,
		options
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test103() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private interface ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnDerived extends ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface Interface {\n" + 
			"		ReturnBase bar();\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Implementation {\n" + 
			"		public final ReturnDerived bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Grandchild extends Child implements Interface {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Child extends Implementation implements Interface {\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Grandchild();\n" + 
			"	}\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertTrue("bar should be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);		
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test104() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private interface ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnDerived extends ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface Interface {\n" + 
			"		ReturnBase bar();\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Implementation {\n" + 
			"		public final ReturnDerived bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Child extends Implementation implements Interface {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Grandchild extends Child implements Interface {\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Grandchild();\n" + 
			"	}\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertTrue("bar should be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);		
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test105() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private interface ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnDerived extends ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Super {\n" + 
			"		ReturnBase bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Implementation extends Super {\n" + 
			"		public final ReturnDerived bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Child extends Implementation {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Grandchild extends Child {\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Grandchild();\n" + 
			"	}\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Implementation.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	int count = 0;
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		IMethodInfo methodInfo = methodInfos[i];
		if (new String(methodInfo.getName()).equals("bar")) {
			count++;
			if (Flags.isBridge(methodInfo.getAccessFlags())) {
				found = true;
			}
		}
	}
	assertEquals("Should have two method bar", 2, count);
	assertTrue("should have one bridge method", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test106() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private interface ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnDerived extends ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Super {\n" + 
			"		ReturnBase bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static abstract class Implementation extends Super {\n" + 
			"		public final ReturnDerived bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Child extends Implementation {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Grandchild extends Child {\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Grandchild();\n" + 
			"	}\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Implementation.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	int count = 0;
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		IMethodInfo methodInfo = methodInfos[i];
		if (new String(methodInfo.getName()).equals("bar")) {
			count ++;
			if (Flags.isBridge(methodInfo.getAccessFlags())) {
				found = true;
			}
		}
	}
	assertEquals("should have two methods bar", 2, count);
	assertTrue("should have one bridge method", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test107() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private interface ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnDerived extends ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface Interface<E> {\n" + 
			"		ReturnBase bar();\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Implementation<T> {\n" + 
			"		public final ReturnDerived bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Child<U> extends Implementation<U> implements Interface<U> {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Grandchild<V> extends Child<V> implements Interface<V> {\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Grandchild();\n" + 
			"	}\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertTrue("bar should be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test108() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private interface ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnDerived extends ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnLeaf extends ReturnDerived {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface Interface<E> {\n" + 
			"		ReturnBase bar();\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Implementation<T> {\n" + 
			"		public final ReturnDerived bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Child<U> extends Implementation<U> implements Interface<U> {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Grandchild<V> extends Child<V> implements Interface<V> {\n" +
			"		@Override\n" + 
			"		public ReturnLeaf bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Grandchild<String>();\n" + 
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 26)\n" + 
		"	public ReturnLeaf bar() {\n" + 
		"	                  ^^^^^\n" + 
		"Cannot override the final method from X.Implementation<V>\n" + 
		"----------\n",
		null,
		true,
		options
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test109() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private interface ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnDerived extends ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface Interface<E> {\n" + 
			"		ReturnBase bar();\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Implementation<T> {\n" + 
			"		public final ReturnDerived bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Grandchild<V> extends Child<V> implements Interface<V> {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Child<U> extends Implementation<U> implements Interface<U> {\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Grandchild();\n" + 
			"	}\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertTrue("bar should be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);		
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test110() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private interface ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnDerived extends ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface Interface<E> {\n" + 
			"		ReturnBase bar();\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Implementation<T> {\n" + 
			"		public final ReturnDerived bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Child<U> extends Implementation<U> implements Interface<U> {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Grandchild<V> extends Child<V> implements Interface<V> {\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Grandchild();\n" + 
			"	}\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertTrue("bar should be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);		
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test111() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private interface ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnDerived extends ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Super<E> {\n" + 
			"		ReturnBase bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Implementation<T> extends Super<T> {\n" + 
			"		public final ReturnDerived bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Child<U> extends Implementation<U> {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Grandchild<V> extends Child<V> {\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Grandchild();\n" + 
			"	}\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Implementation.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	int count = 0;
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		IMethodInfo methodInfo = methodInfos[i];
		if (new String(methodInfo.getName()).equals("bar")) {
			count++;
			if (Flags.isBridge(methodInfo.getAccessFlags())) {
				found = true;
			}
		}
	}
	assertEquals("should have two methods bar", 2, count);
	assertTrue("should have one bridge method", found);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test112() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	private interface ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private interface ReturnDerived extends ReturnBase {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Super<E> {\n" + 
			"		ReturnBase bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static abstract class Implementation<T> extends Super<T> {\n" + 
			"		public final ReturnDerived bar() {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Child<U> extends Implementation<U> {\n" + 
			"	}\n" + 
			"\n" + 
			"	private static class Grandchild<V> extends Child<V> {\n" + 
			"	}\n" + 
			"\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Grandchild();\n" + 
			"	}\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$Child.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	IMethodInfo[] methodInfos = reader.getMethodInfos();
	boolean found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Grandchild.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		if (new String(methodInfos[i].getName()).equals("bar")) {
			found = true;
			break;
		}
	}
	assertFalse("bar should not be there", found);
	
	fileX = new File(OUTPUT_DIR + File.separator  +"X$Implementation.class");
	reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.ALL_BUT_METHOD_BODIES);
	methodInfos = reader.getMethodInfos();
	int count = 0;
	found = false;
	for (int i = 0, max = methodInfos.length; i < max; i++) {
		IMethodInfo methodInfo = methodInfos[i];
		if (new String(methodInfo.getName()).equals("bar")) {
			count++;
			if (Flags.isBridge(methodInfo.getAccessFlags())) {
				found = true;
			}
		}
	}
	assertEquals("should have two methods bar", 2, count);
	assertTrue("should have one bridge method", found);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test113() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"abstract class Y {\n" + 
			"  abstract void foo();\n" + 
			"}\n" + 
			"public class X extends Y {\n" + 
			"  void foo() {\n" + 
			"    // should not complain for missing super call, since overriding \n" + 
			"    // abstract method\n" + 
			"  }\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test114() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */, 
		new String[] { /* test files */
			"X.java",
			"class Y {\n" + 
			"  void foo() {}\n" + 
			"}\n" + 
			"public class X extends Y {\n" + 
			"  @Override\n" +
			"  void foo() {\n" + 
			"  }\n" + 
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" + 
		"	void foo() {\n" + 
		"	     ^^^^^\n" + 
		"The method X.foo() is overriding a method without making a super invocation\n" + 
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test115() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"class Y {\n" + 
			"  void foo() {}\n" + 
			"}\n" + 
			"public class X extends Y {\n" + 
			"  @Override\n" +
			"  void foo() {\n" + 
			"    super.foo();\n" + 
			"  }\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test116() {
   	Map options = this.getCompilerOptions();
   	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Y {\n" + 
			"  Zork foo() {}\n" + 
			"}\n" + 
			"public class X extends Y {\n" + 
			"  @Override\n" +
			"  Object foo() {\n" +
			"     return new Y() {\n" +
			"         Object foo() {\n" +
			"            return null;\n" +
			"         }\n" +
			"     };" +
			"  }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Zork foo() {}\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	Object foo() {\n" + 
		"	^^^^^^\n" + 
		"The return type is incompatible with Y.foo()\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 6)\n" + 
		"	Object foo() {\n" + 
		"	       ^^^^^\n" + 
		"The method X.foo() is overriding a method without making a super invocation\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 8)\n" + 
		"	Object foo() {\n" + 
		"	^^^^^^\n" + 
		"The return type is incompatible with Y.foo()\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 8)\n" + 
		"	Object foo() {\n" + 
		"	       ^^^^^\n" + 
		"The method foo() of type new Y(){} should be tagged with @Override since it actually overrides a superclass method\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 8)\n" + 
		"	Object foo() {\n" + 
		"	       ^^^^^\n" + 
		"The method new Y(){}.foo() is overriding a method without making a super invocation\n" + 
		"----------\n",
		null,
		true,
		options	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test117() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */, 
		new String[] { /* test files */
			"X.java",
			"class Y {\n" + 
			"  Object foo() {\n" +
			"     return null;\n" +
			"  }\n" + 
			"}\n" + 
			"public class X extends Y {\n" + 
			"  @Override\n" +
			"  Object foo() {\n" +
			"     return new Y() {\n" +
   			"         @Override\n" +
			"         Object foo() {\n" +
			"            return null;\n" +
			"         }\n" +
			"     };" +
			"  }\n" + 
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 8)\n" + 
		"	Object foo() {\n" + 
		"	       ^^^^^\n" + 
		"The method X.foo() is overriding a method without making a super invocation\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 11)\n" + 
		"	Object foo() {\n" + 
		"	       ^^^^^\n" + 
		"The method new Y(){}.foo() is overriding a method without making a super invocation\n" + 
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test118() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */, 
		new String[] { /* test files */
			"X.java",
			"class Y<E> {\n" + 
			"	<U extends E> U foo() {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"public class X<T> extends Y<T> {\n" + 
			"	@Override\n" + 
			"	<V extends T> V foo() {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 9)\n" + 
		"	<V extends T> V foo() {\n" + 
		"	                ^^^^^\n" + 
		"The method X<T>.foo() is overriding a method without making a super invocation\n" + 
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156736
public void test119() {
	Map options = this.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */, 
		new String[] { /* test files */
			"X.java",
			"class Y<E> {\n" + 
			"	E foo() {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"public class X<T> extends Y<T> {\n" + 
			"	@Override\n" + 
			"	T foo() {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}"
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 9)\n" + 
		"	T foo() {\n" + 
		"	  ^^^^^\n" + 
		"The method X<T>.foo() is overriding a method without making a super invocation\n" + 
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=161541
public void test120() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	abstract class M<T extends CharSequence, S> {\n" + 
			"		void e(T t) {}\n" + 
			"		void e(S s) {}\n" +
			"	}\n" + 
			"	class N extends M<String, String> {}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	class N extends M<String, String> {}\n" + 
		"	      ^\n" + 
		"Duplicate methods named e with the parameters (S) and (T) are defined by the type X.M<String,String>\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=202830
public void test120a() {
	this.runConformTest(
		new String[] {
			"Bar.java",
			"class Foo<V, E> {\n" + 
			"	int getThing(V v) { return 1; }\n" + 
			"	boolean getThing(E e) { return true; }\n" +
			"}\n" +
			"public class Bar<V,E> extends Foo<V,E> {}"
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=173477
public void test121() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface Root {\n" + 
			"	public Root someMethod();\n" + 
			"}\n" + 
			"\n" + 
			"interface Intermediary extends Root {\n" + 
			"	public Leaf someMethod();\n" + 
			"}\n" + 
			"\n" + 
			"class Leaf implements Intermediary {\n" + 
			"	public Leaf someMethod() {\n" + 
			"		System.out.print(\"SUCCESS\");\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Leaf leafReference = new Leaf();\n" + 
			"		leafReference.someMethod();\n" + 
			"		Root rootReference = leafReference;\n" + 
			"		rootReference.someMethod(); /* throws error */\n" + 
			"	}\n" + 
			"}"
		},
		"SUCCESSSUCCESS"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=175987
public void test122() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  public void foo(Integer i, Y<String> l1, Y<String> l2);\n" + 
			"}\n" + 
			"public class X implements I {\n" + 
			"  public void foo(Integer i, Y<String> l1, Y l2) {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y<T> {\n" + 
			"}"},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	public class X implements I {\n" + 
		"	             ^\n" + 
		"The type X must implement the inherited abstract method I.foo(Integer, Y<String>, Y<String>)\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	public void foo(Integer i, Y<String> l1, Y l2) {\n" + 
		"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Name clash: The method foo(Integer, Y<String>, Y) of type X has the same erasure as foo(Integer, Y<String>, Y<String>) of type I but does not override it\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 5)\n" + 
		"	public void foo(Integer i, Y<String> l1, Y l2) {\n" + 
		"	                                         ^\n" + 
		"Y is a raw type. References to generic type Y<T> should be parameterized\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=175987
// variant that must pass because X#foo's signature is a subsignature of
// I#foo's.
public void test123() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  public void foo(Integer i, Y<String> l1, Y<String> l2);\n" + 
			"}\n" + 
			"public class X implements I {\n" + 
			"  public void foo(Integer i, Y l1, Y l2) {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y<T> {\n" + 
			"}"},
		""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150655
// **
public void _test124() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  public static String choose(String one, String two) {\n" + 
			"    return one + X.<String>choose(one, two);\n" + 
			"  }\n" + 
			"  public static <T> T choose(T one, T two) {\n" + 
			"    return two;\n" + 
			"  }\n" + 
			"  public static void main(String args[]) {\n" + 
			"    System.out.println(choose(\"a\", \"b\"));\n" + 
			"  }\n" + 
			"}"},
		"ab");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150655
// variant
public void test125() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  public static <T> String choose(String one, String two) {\n" + 
			"    return one + X.<String>choose(one, two);\n" + 
			"  }\n" + 
			"  public static <T> T choose(T one, T two) {\n" + 
			"    return two;\n" + 
			"  }\n" + 
			"  public static void main(String args[]) {\n" + 
			"    System.out.println(choose(\"a\", \"b\"));\n" + 
			"  }\n" + 
			"}"},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	return one + X.<String>choose(one, two);\n" + 
		"	                       ^^^^^^\n" + 
		"The method choose(String, String) is ambiguous for the type X\n" +  
		"----------\n",
		JavacTestOptions.EclipseHasABug.EclipseBug207935);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150655
// variant
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176171
// deprecated by GenericTypeTest#test1203.
//public void _test126() {
//	this.runNegativeTest(
//		new String[] {
//			"X.java",
//			"public class X extends Y {\n" + 
//			"  public static String foo(String one, String two) {\n" + // complain
//			"    return X.<String>foo(one, two);\n" + 
//			"  }\n" + 
//			"  public String bar(String one, String two) {\n" + // complain
//			"    return this.<String>bar(one, two);\n" + 
//			"  }\n" +
//			"  @Override\n" + 
//			"  public String foobar(String one, String two) {\n" + // OK
//			"    return this.<String>foobar(one, two);\n" + 
//			"  }\n" + 
//			"}\n" + 
//			"class Y {\n" + 
//			"  public <T> String foobar(String one, String two) {\n" + 
//			"    return null;\n" + 
//			"  }\n" + 
//			"}\n"},
//		"----------\n" + 
//		"1. ERROR in X.java (at line 3)\n" + 
//		"	return X.<String>foo(one, two);\n" + 
//		"	                 ^^^\n" + 
//		"The method foo(String, String) of type X is not generic; it cannot be parameterized with arguments <String>\n" + 
//		"----------\n" + 
//		"2. ERROR in X.java (at line 6)\n" + 
//		"	return this.<String>bar(one, two);\n" + 
//		"	                    ^^^\n" + 
//		"The method bar(String, String) of type X is not generic; it cannot be parameterized with arguments <String>\n" + 
//		"----------\n");
//}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=174445
public void test127() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  enum Enum1 {\n" + 
			"    value;\n" + 
			"  }\n" + 
			"  enum Enum2 {\n" + 
			"    value;\n" + 
			"  }\n" + 
			"  static abstract class A<T> {\n" + 
			"    abstract <U extends T> U foo();\n" + 
			"  }\n" + 
			"  static class B extends A<Enum<?>> {\n" + 
			"    @Override\n" + 
			"    Enum<?> foo() {\n" + 
			"      return Enum1.value;\n" + 
			"    }  \n" + 
			"  }\n" + 
			"  public static void main(String[] args) {\n" + 
			"    A<Enum<?>> a = new B();\n" + 
			"    Enum2 value = a.foo();\n" + 
			"  }\n" + 
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 13)\n" + 
		"	Enum<?> foo() {\n" + 
		"	^^^^\n" + 
		"Type safety: The return type Enum<?> for foo() from the type X.B needs unchecked conversion to conform to U from the type X.A<T>\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
public void test128() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I<U, V> {\n" + 
			"  U foo(Object o, V v);\n" + 
			"}\n" + 
			"public class X<U, V> implements I<U, V> {\n" + 
			"  public Object foo(Object o, Object v) { return null; }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	public Object foo(Object o, Object v) { return null; }\n" + 
		"	       ^^^^^^\n" + 
		"Type safety: The return type Object for foo(Object, Object) from the type X<U,V> needs unchecked conversion to conform to U from the type I<U,V>\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
// variant - Object is not a subtype of Z
public void test129() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I<U, V> {\n" + 
			"  U foo(Object o, V v);\n" + 
			"}\n" + 
			"public class X<U extends Z, V> implements I<U, V> {\n" + 
			"  public Object foo(Object o, Object v) { return null; }\n" + 
			"}\n" + 
			"class Z {}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	public Object foo(Object o, Object v) { return null; }\n" + 
		"	       ^^^^^^\n" + 
		"The return type is incompatible with I<U,V>.foo(Object, V)\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
// variant - Z<Object> is not a subtype of Z<U>, and |Z<U>| = Z, not Z<Object>
public void test130() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I<U, V> {\n" + 
			"  Z<U> foo(Object o, V v);\n" + 
			"}\n" + 
			"public class X<U, V> implements I<U, V> {\n" + 
			"  public Z<Object> foo(Object o, Object v) { return null; }\n" + 
			"}\n" + 
			"class Z<T> {}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	public Z<Object> foo(Object o, Object v) { return null; }\n" + 
		"	       ^^^^^^^^^\n" + 
		"The return type is incompatible with I<U,V>.foo(Object, V)\n" + 
		"----------\n",
		JavacTestOptions.EclipseJustification.EclipseBug180789
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
// variant - two interfaces
public void test131() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I<U, V> {\n" + 
			"  U foo();\n" + 
			"  U foo(Object o, V v);\n" + 
			"}\n" + 
			"interface X<U, V> extends I<U, V> {\n" + 
			"  Object foo();\n" + 
			"  Object foo(Object o, Object v);\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	Object foo();\n" + 
		"	^^^^^^\n" + 
		"The return type is incompatible with I<U,V>.foo()\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 7)\n" + 
		"	Object foo(Object o, Object v);\n" + 
		"	^^^^^^\n" + 
		"Type safety: The return type Object for foo(Object, Object) from the type X<U,V> needs unchecked conversion to conform to U from the type I<U,V>\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
// variant - type identity vs type equivalence
public void test132() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I<U> {\n" + 
			"  U foo(I<?> p);\n" + 
			"  U foo2(I<? extends Object> p);\n" + 
			"}\n" + 
			"public class X<U> implements I<U> {\n" + 
			"  public Object foo(I<? extends Object> p) { return null; }\n" + 
			"  public Object foo2(I<?> p) { return null; }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	public Object foo(I<? extends Object> p) { return null; }\n" + 
		"	       ^^^^^^\n" + 
		"The return type is incompatible with I<U>.foo(I<?>)\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	public Object foo2(I<?> p) { return null; }\n" + 
		"	       ^^^^^^\n" + 
		"The return type is incompatible with I<U>.foo2(I<? extends Object>)\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
// variant - if we detect a return type incompatibility, then skip any @Override errors 
public void test133() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"class A<U> {\n" + 
			"  U foo() { return null; }\n" + 
			"  U foo(U one) { return null; }\n" + 
			"  U foo(U one, U two) { return null; }\n" + 
			"}\n" + 
			"class B<U> extends A<U> {\n" + 
			"  @Override // does not override error\n" + 
			"  Object foo() { return null; } // cannot override foo(), incompatible return type error\n" +
			"  @Override // does not override error\n" + 
			"  Object foo(Object one) { return null; } // unchecked conversion warning\n" +
			"  @Override // does not override error\n" + 
			"  Object foo(Object one, U two) { return null; }\n" +
			"}\n" + 
			"class C<U> extends A<U> {\n" + 
			"  @Override // does not override error\n" + 
			"  Object foo(U one) { return null; } // cannot override foo(U), incompatible return type error\n" +
			"  @Override // does not override error\n" + 
			"  Object foo(U one, U two) { return null; } // cannot override foo(U), incompatible return type error\n" +
			"}"
		},
		"----------\n" + 
		"1. ERROR in A.java (at line 8)\n" + 
		"	Object foo() { return null; } // cannot override foo(), incompatible return type error\n" + 
		"	^^^^^^\n" + 
		"The return type is incompatible with A<U>.foo()\n" + 
		"----------\n" + 
		"2. WARNING in A.java (at line 10)\n" + 
		"	Object foo(Object one) { return null; } // unchecked conversion warning\n" + 
		"	^^^^^^\n" + 
		"Type safety: The return type Object for foo(Object) from the type B<U> needs unchecked conversion to conform to U from the type A<U>\n" + 
		"----------\n" + 
		"3. ERROR in A.java (at line 12)\n" + 
		"	Object foo(Object one, U two) { return null; }\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Name clash: The method foo(Object, U) of type B<U> has the same erasure as foo(U, U) of type A<U> but does not override it\n" + 
		"----------\n" + 
		"4. ERROR in A.java (at line 12)\n" + 
		"	Object foo(Object one, U two) { return null; }\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^\n" + 
		mustOverrideMessage("foo(Object, U)", "B<U>") +
		"----------\n" + 
		"5. ERROR in A.java (at line 16)\n" + 
		"	Object foo(U one) { return null; } // cannot override foo(U), incompatible return type error\n" + 
		"	^^^^^^\n" + 
		"The return type is incompatible with A<U>.foo(U)\n" + 
		"----------\n" + 
		"6. ERROR in A.java (at line 18)\n" + 
		"	Object foo(U one, U two) { return null; } // cannot override foo(U), incompatible return type error\n" + 
		"	^^^^^^\n" + 
		"The return type is incompatible with A<U>.foo(U, U)\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test134() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" + 
			"  <T extends Exception & Cloneable> T foo(Number n);\n" + 
			"}\n" + 
			"interface J extends I {\n" + 
			"  A foo(Number n);\n" +  // warning: overrides <T>foo(java.lang.Number) in I; return type requires unchecked conversion
			"}\n" + 
			"abstract class A extends Exception implements Cloneable {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	A foo(Number n);\n" + 
		"	^\n" + 
		"Type safety: The return type A for foo(Number) from the type J needs unchecked conversion to conform to T from the type I\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test135() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"abstract class X implements J {}\n" + 
			"class X2 implements J {\n" + 
			"  public A foo(Number n) { return null; }\n" +
			"}\n" + 
			"abstract class Y extends X {}\n" + 
			"interface I {\n" + 
			"  <T extends Exception & Cloneable> T foo(Number n);\n" + 
			"}\n" + 
			"interface J extends I {\n" + 
			"  A foo(Number n);\n" +  // warning: overrides <T>foo(java.lang.Number) in I; return type requires unchecked conversion
			"}\n" + 
			"abstract class A extends Exception implements Cloneable {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 10)\n" + 
		"	A foo(Number n);\n" + 
		"	^\n" + 
		"Type safety: The return type A for foo(Number) from the type J needs unchecked conversion to conform to T from the type I\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test136() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public abstract class X extends E {}\n" + 
			"class X2 extends E {\n" + 
			"  @Override public A foo(Number n) { return null; }\n" +
			"}\n" + 
			"abstract class Y extends X {}\n" + 
			"abstract class D {\n" + 
			"  abstract <T extends Exception & Cloneable> T foo(Number n);\n" + 
			"}\n" + 
			"abstract class E extends D {\n" + 
			"  @Override abstract A foo(Number n);\n" +  // warning: overrides <T>foo(java.lang.Number) in I; return type requires unchecked conversion
			"}\n" + 
			"abstract class A extends Exception implements Cloneable {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 10)\n" + 
		"	@Override abstract A foo(Number n);\n" + 
		"	                   ^\n" + 
		"Type safety: The return type A for foo(Number) from the type E needs unchecked conversion to conform to T from the type D\n" + 
		"----------\n"
		// javac reports warnings against X AND Y about E.foo(), as well as reporting the warning on E.foo() twice
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test137() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public abstract class X implements J {}\n" + 
			"interface I {\n" + 
			"  <T extends Y<T> & Cloneable> T foo(Number n);\n" + 
			"}\n" + 
			"interface J extends I {\n" + 
			"  XX foo(Number n);\n" + 
			"}\n" + 
			"class Z { }\n" +
			"class Y <U> extends Z { }" +
			"abstract class XX extends Y<XX> implements Cloneable {}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 6)\n" + 
		"	XX foo(Number n);\n" + 
		"	^^\n" + 
		"Type safety: The return type XX for foo(Number) from the type J needs unchecked conversion to conform to T from the type I\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test138() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public abstract class X implements J {}\n" + 
			"interface I {\n" + 
			"  <T extends Exception & Cloneable> A<T> foo(Number n);\n" +
			"}\n" +
			"interface J extends I {\n" +
			"  A<XX> foo(Number n);\n" +
			"}\n" + 
			"class A<T> { }" +			
			"abstract class XX extends Exception implements Cloneable {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 6)\n" + 
		"	A<XX> foo(Number n);\n" + 
		"	^\n" + 
		"Type safety: The return type A<XX> for foo(Number) from the type J needs unchecked conversion to conform to A<T> from the type I\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test139() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public abstract class X implements J {\n" + 
			"  void foo() {}\n" + 
			"  public XX foo(Number n) { return null; }\n" + 
			"}\n" + 
			"interface I {\n" + 
			"  <T extends Exception & Cloneable> T foo(Number n);\n" + 
			"}\n" + 
			"interface J extends I {\n" + 
			"  XX foo(Number n);\n" + 
			"}\n" + 
			"abstract class XX extends Exception implements Cloneable {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 9)\n" + 
		"	XX foo(Number n);\n" + 
		"	^^\n" + 
		"Type safety: The return type XX for foo(Number) from the type J needs unchecked conversion to conform to T from the type I\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162073
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184293
public void test140() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public abstract class X implements J, K {}\n" + 
			"interface I {\n" + 
			"  <T extends Exception & Cloneable> T foo(Number n);\n" + 
			"}\n" + 
			"interface J extends I {\n" + 
			"  XX foo(Number n);\n" + 
			"}\n" + 
			"interface K {\n" + 
			"  NullPointerException foo(Number n);\n" + 
			"}\n" + 
			"abstract class XX extends Exception implements Cloneable {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public abstract class X implements J, K {}\n" + 
		"	                      ^\n" + 
		"The return type is incompatible with K.foo(Number), J.foo(Number)\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 6)\n" + 
		"	XX foo(Number n);\n" + 
		"	^^\n" + 
		"Type safety: The return type XX for foo(Number) from the type J needs unchecked conversion to conform to T from the type I\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=186457
public void test141() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.nio.charset.Charset;\n" + 
			"import java.nio.charset.CharsetDecoder;\n" + 
			"import java.nio.charset.CharsetEncoder;\n" + 
			"public class X extends Charset {\n" + 
			"  public X(String name, String[] aliases) { super(name, aliases); }\n" + 
			"  @Override public CharsetEncoder newEncoder() { return null;  }\n" + 
			"  @Override public CharsetDecoder newDecoder() { return null;  }\n" + 
			"  @Override public boolean contains(Charset x) { return false; }\n" + 
			"  public int compareTo(Object obj) {\n" + 
			"    return compareTo((Charset) obj);\n" + 
			"  }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	public int compareTo(Object obj) {\n" + 
		"	           ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Name clash: The method compareTo(Object) of type X has the same erasure as compareTo(T) of type Comparable<T> but does not override it\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=186457
public void test142() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.nio.charset.Charset;\n" + 
			"import java.nio.charset.CharsetDecoder;\n" + 
			"import java.nio.charset.CharsetEncoder;\n" + 
			"public class X extends Charset {\n" + 
			"  public X(String name, String[] aliases) { super(name, aliases); }\n" + 
			"  public CharsetEncoder newEncoder() { return null;  }\n" + 
			"  public CharsetDecoder newDecoder() { return null;  }\n" + 
			"  public boolean contains(Charset x) { return false; }\n" + 
			"}"
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190748
public void test143() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public static void main(String[] s) { ((IBase) new Impl()).get(); }\n" +
			"}\n" + 
			"class Impl extends AImpl implements IBase, IEnhanced {}\n" + 
			"interface IBase {\n" + 
			"	IBaseReturn get();\n" + 
			"}\n" + 
			"interface IEnhanced extends IBase {\n" + 
			"	IEnhancedReturn get();\n" + 
			"}\n" + 
			"abstract class AImpl {\n" + 
			"	public IEnhancedReturn get() { return null; }\n" + 
			"}\n" + 
			"interface IBaseReturn {}\n" + 
			"interface IEnhancedReturn extends IBaseReturn {}"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=194034
public void test144() {
	this.runNegativeTest(
		new String[] {
			"PurebredCatShopImpl.java",
			"import java.util.List;\n" +
			"interface Pet {}\n" +
			"interface Cat extends Pet {}\n" + 
			"interface PetShop { List<Pet> getPets(); }\n" + 
			"interface CatShop extends PetShop {\n" + 
			"	<V extends Pet> List<? extends Cat> getPets();\n" + 
			"}\n" + 
			"interface PurebredCatShop extends CatShop {}\n" + 
			"class CatShopImpl implements CatShop {\n" + 
			"	public List<Pet> getPets() { return null; }\n" + 
			"}\n" + 
			"class PurebredCatShopImpl extends CatShopImpl implements PurebredCatShop {}"
		},
		"----------\n" + 
		"1. WARNING in PurebredCatShopImpl.java (at line 10)\n" + 
		"	public List<Pet> getPets() { return null; }\n" + 
		"	       ^^^^\n" + 
		"Type safety: The return type List<Pet> for getPets() from the type CatShopImpl needs unchecked conversion to conform to List<? extends Cat> from the type CatShop\n" + 
		"----------\n" + 
		"2. WARNING in PurebredCatShopImpl.java (at line 12)\n" + 
		"	class PurebredCatShopImpl extends CatShopImpl implements PurebredCatShop {}\n" + 
		"	      ^^^^^^^^^^^^^^^^^^^\n" + 
		"Type safety: The return type List<Pet> for getPets() from the type CatShopImpl needs unchecked conversion to conform to List<? extends Cat> from the type CatShop\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=195468
public void test145() {
	this.runConformTest(
		new String[] {
			"BaseImpl.java",
			"abstract class Base<Tvalue> implements BaseInterface<Tvalue>{ public void setValue(Object object) {} }\n" +
			"interface BaseInterface<Tvalue> { void setValue(Tvalue object); }\n" + 
			"class BaseImpl extends Base<String> { public void setValue(String object) {} }"
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=195802
public void test146() {
	this.runConformTest(
		new String[] {
			"BugB.java",
			"abstract class A<K> { void get(K key) {} }\n" +
			"abstract class B extends A<C> { <S> void get(C<S> type) {} }\n" +
			"class B2 extends A<C> { <S> void get(C<S> type) {} }\n" + 
			"class BugB extends B {}\n" + 
			"class NonBugB extends B2 {}\n" + 
			"class C<T> {}"
		},
		""
	);
}
public void test147() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface J<T> { <U1, U2> void foo(T t); }\n" + 
			"class Y<T> { public <U3> void foo(T t) {} }\n" + 
			"abstract class X<T> extends Y<T> implements J<T> {\n" + 
			"	@Override public void foo(Object o) {}\n" + 
			"}\n" + 
			"abstract class X1<T> extends Y<T> implements J<T> {\n" + 
			"	public <Ignored> void foo(Object o) {}\n" + 
			"}\n" +
			"abstract class X2<T> extends Y<T> implements J<T> {}\n" +
			"abstract class X3 extends Y<Number> implements J<String> {}\n" +
			"abstract class X4 extends Y<Number> implements J<String> {\n" +
			"	@Override public void foo(Number o) {}\n" +
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	abstract class X1<T> extends Y<T> implements J<T> {\n" + 
		"	               ^^\n" + 
		"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(T) of type J<T> but does not override it\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	public <Ignored> void foo(Object o) {}\n" + 
		"	                      ^^^^^^^^^^^^^\n" + 
		"Name clash: The method foo(Object) of type X1<T> has the same erasure as foo(T) of type Y<T> but does not override it\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 7)\n" + 
		"	public <Ignored> void foo(Object o) {}\n" + 
		"	                      ^^^^^^^^^^^^^\n" + 
		"Name clash: The method foo(Object) of type X1<T> has the same erasure as foo(T) of type J<T> but does not override it\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 9)\n" + 
		"	abstract class X2<T> extends Y<T> implements J<T> {}\n" + 
		"	               ^^\n" + 
		"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(T) of type J<T> but does not override it\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 10)\n" + 
		"	abstract class X3 extends Y<Number> implements J<String> {}\n" + 
		"	               ^^\n" + 
		"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(T) of type J<T> but does not override it\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 11)\n" + 
		"	abstract class X4 extends Y<Number> implements J<String> {\n" + 
		"	               ^^\n" + 
		"Name clash: The method foo(T) of type Y<T> has the same erasure as foo(T) of type J<T> but does not override it\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=204624
public void test148() {
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"abstract class X { abstract <T extends Object> T go(A<T> a); }\n" +
			"class Y extends X {\n" +
			"	@Override <T extends Object> T go(A a) { return null; }\n" +
			"}\n" + 
			"class A<T> {}"
		},
		"----------\n" + 
		"1. ERROR in Y.java (at line 2)\n" + 
		"	class Y extends X {\n" + 
		"	      ^\n" + 
		"The type Y must implement the inherited abstract method X.go(A<T>)\n" + 
		"----------\n" + 
		"2. ERROR in Y.java (at line 3)\n" + 
		"	@Override <T extends Object> T go(A a) { return null; }\n" + 
		"	                               ^^^^^^^\n" + 
		"Name clash: The method go(A) of type Y has the same erasure as go(A<T>) of type X but does not override it\n" + 
		"----------\n" + 
		"3. ERROR in Y.java (at line 3)\n" + 
		"	@Override <T extends Object> T go(A a) { return null; }\n" + 
		"	                               ^^^^^^^\n" + 
		mustOverrideMessage("go(A)", "Y") + 
		"----------\n" + 
		"4. WARNING in Y.java (at line 3)\n" + 
		"	@Override <T extends Object> T go(A a) { return null; }\n" + 
		"	                                  ^\n" + 
		"A is a raw type. References to generic type A<T> should be parameterized\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=208995
public void test149() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"class A {\n" +
			"	void a(X x) {}\n" +
			"	void b(Y<Integer> y) {}\n" +
			"	static void c(X x) {}\n" +
			"	static void d(Y<Integer> y) {}\n" +
			"}\n",
			"B.java",
			"class B extends A {\n" +
			"	static void a(X x) {}\n" + // cannot override a(X) in A; overriding method is static
			"	static void b(Y<String> y) {}\n" + // name clash
			"	static void c(X x) {}\n" +
			"	static void d(Y<String> y) {}\n" +
			"}\n",
			"B2.java",
			"class B2 extends A {\n" +
			"	static void b(Y<Integer> y) {}\n" + // cannot override b(Y<Integer>) in A; overriding method is static
			"	static void d(Y<Integer> y) {}\n" +
			"}\n",
			"C.java",
			"class C extends A {\n" +
			"	@Override void a(X x) {}\n" +
			"	void b(Y<String> y) {}\n" + // name clash
			"	void c(X x) {}\n" + // cannot override c(X) in A; overridden method is static
			"	void d(Y<String> y) {}\n" +
			"}\n",
			"C2.java",
			"class C2 extends A {\n" +
			"	@Override void b(Y<Integer> y) {}\n" +
			"	void d(Y<Integer> y) {}\n" + // cannot override b(Y<Integer>) in A; overridden method is static
			"}\n" + 
			"class X {}\n" + 
			"class Y<T> {}"
		},
		"----------\n" + 
		"1. ERROR in B.java (at line 2)\n" + 
		"	static void a(X x) {}\n" + 
		"	            ^^^^^^\n" + 
		"This static method cannot hide the instance method from A\n" + 
		"----------\n" + 
		"2. ERROR in B.java (at line 3)\n" + 
		"	static void b(Y<String> y) {}\n" + 
		"	            ^^^^^^^^^^^^^^\n" + 
		"Name clash: The method b(Y<String>) of type B has the same erasure as b(Y<Integer>) of type A but does not override it\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in B2.java (at line 2)\n" + 
		"	static void b(Y<Integer> y) {}\n" + 
		"	            ^^^^^^^^^^^^^^^\n" + 
		"This static method cannot hide the instance method from A\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in C.java (at line 3)\n" + 
		"	void b(Y<String> y) {}\n" + 
		"	     ^^^^^^^^^^^^^^\n" + 
		"Name clash: The method b(Y<String>) of type C has the same erasure as b(Y<Integer>) of type A but does not override it\n" + 
		"----------\n" + 
		"2. ERROR in C.java (at line 4)\n" + 
		"	void c(X x) {}\n" + 
		"	     ^^^^^^\n" + 
		"This instance method cannot override the static method from A\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in C2.java (at line 3)\n" + 
		"	void d(Y<Integer> y) {}\n" + 
		"	     ^^^^^^^^^^^^^^^\n" + 
		"This instance method cannot override the static method from A\n" + 
		"----------\n"
	);
}
public void test150() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	// DOESN\'T Compile\n" + 
			"	public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<java.util.Date> date){\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"	// Doesn\'t compile\n" + 
			"	public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<String> date){\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"	// Using vararg trick compiles ok use vararg to differentiate method signature\n" + 
			"	public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<java.util.Date> date, D1 ... notUsed){\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"	// Using vararg trick compiles ok use vararg to differentiate method signature\n" + 
			"	public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<String> date, D2 ... notUsed){\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"	class MyT<T>{}\n" + 
			"	class D1<T>{}\n" + 
			"	class D2<T>{}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<java.util.Date> date){\n" + 
		"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Method method3(X.D1<String>, X.D1<String>, X.D1<Date>) has the same erasure method3(X.D1<T>, X.D1<T>, X.D1<T>) as another method in type X\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<String> date){\n" + 
		"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Method method3(X.D1<String>, X.D1<String>, X.D1<String>) has the same erasure method3(X.D1<T>, X.D1<T>, X.D1<T>) as another method in type X\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 11)\n" + 
		"	public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<java.util.Date> date, D1 ... notUsed){\n" + 
		"	                                                                                    ^^\n" + 
		"X.D1 is a raw type. References to generic type X.D1<T> should be parameterized\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 15)\n" + 
		"	public MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<String> date, D2 ... notUsed){\n" + 
		"	                                                                            ^^\n" + 
		"X.D2 is a raw type. References to generic type X.D2<T> should be parameterized\n" + 
		"----------\n");
}	
public void test151() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.Date;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	// Using vararg trick compiles ok use vararg to differentiate method signature\n" + 
			"	public static MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<java.util.Date> date, D1... notUsed) {\n" + 
			"		System.out.print(\"#method3(D1<String>, D1<String>, D1<java.util.Date>, D1[])\");\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"\n" + 
			"	// Using vararg trick compiles ok use vararg to differentiate method signature\n" + 
			"	public static MyT<Void> method3(D1<String> harg, D1<String> oarg, D1<String> date, D2... notUsed) {\n" + 
			"		System.out.print(\"#method3(D1<String>, D1<String>, D1<java.util.Date>, D2[])\");\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"\n" + 
			"	/**\n" + 
			"	 * this java main demonstrates that compiler can differentiate between to\n" + 
			"	 * the 2 different methods.\n" + 
			"	 * @param args\n" + 
			"	 */\n" + 
			"	public static void main(String[] args) {\n" + 
			"		X x = new X();\n" + 
			"		D1<String> dString = x.new D1<String>();\n" + 
			"		D1<Date> dDate = x.new D1<Date>();\n" + 
			"		// calling first defined method\n" + 
			"		X.method3(dString, dString, dDate);\n" + 
			"		// calling second defined method\n" + 
			"		X.method3(dString, dString, dString);\n" + 
			"		// / will write out\n" + 
			"		// method3 called with this signature: D1<String> harg, D1<String> oarg,\n" + 
			"		// D1<java.util.Date> date\n" + 
			"		// method3 called with this signature: D1<String> harg, D1<String> oarg,\n" + 
			"		// D1<String> date\n" + 
			"	}\n" + 
			"	class MyT<T> {}\n" + 
			"	public class D1<T> {}\n" + 
			"	public class D2<T> {}\n" + 
			"}\n"
		},
		"#method3(D1<String>, D1<String>, D1<java.util.Date>, D1[])#method3(D1<String>, D1<String>, D1<java.util.Date>, D2[])");
}	

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=219625
public void test152() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static <T> void feedFoosValueIntoFoo(Foo<T> foo) {\n" +
			"		foo.doSomething(foo.getValue());\n" +
			"	}\n" +
			"	static void testTypedString() {\n" +
			"		ConcreteFoo foo = new ConcreteFoo();\n" +
			"		foo.doSomething(foo.getValue());\n" +
			"	}\n" +
			"	static void testGenericString() {\n" +
			"		feedFoosValueIntoFoo(new ConcreteFoo());\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		testTypedString();\n" +
			"		testGenericString();\n" +
			"		System.out.print(1);\n" +
			"	}\n" +
			"}\n" +
			"interface Foo<T> {\n" +
			"	T getValue();\n" +
			"	void doSomething(T o);\n" +
			"}\n" +
			"abstract class AbstractFoo<T> implements Foo<T> {\n" +
			"	public void doSomething(String o) {}\n" +
			"}\n" +
			"class ConcreteFoo extends AbstractFoo<String> {\n" +
			"	public String getValue() { return null; }\n" +
			"}"
		},
		"1"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986
public void test153() {
	this.runConformTest(
		new String[] {
			"test/impl/SubOneImpl.java", //--------------------------------------------
			"package test.impl;\n" + 
			"import test.intf.SubTwo;\n" + 
			"public abstract class SubOneImpl extends SuperTypeExtendImpl implements test.intf.SubOne\n" + 
			"{\n" + 
			"    public SubOneImpl plus(test.intf.SubOne attribute)\n" + 
			"    {\n" + 
			"        throw new RuntimeException(\"foo\");\n" + 
			"    }\n" + 
			"    public SubTwoImpl plus(SubTwo attribute)\n" + 
			"    {\n" + 
			"        throw new RuntimeException(\"foo\");\n" + 
			"    }\n" + 
			"}\n",
			"test/impl/SubSubOneImpl.java", //--------------------------------------------
			"package test.impl;\n" + 
			"public abstract class SubSubOneImpl extends SubOneImpl\n" + 
			"{\n" + 
			"}\n",
			"test/impl/SubTwoImpl.java", //--------------------------------------------
			"package test.impl;\n" + 
			"import test.intf.SubOne;\n" + 
			"public abstract class SubTwoImpl extends SuperTypeExtendImpl implements\n" + 
			"test.intf.SubTwo\n" + 
			"{\n" + 
			"    public SubTwoImpl plus(SubOne attribute)\n" + 
			"    {\n" + 
			"        throw new RuntimeException(\"foo\");\n" + 
			"    }\n" + 
			"    public SubTwoImpl plus(test.intf.SubTwo attribute)\n" + 
			"    {\n" + 
			"        throw new RuntimeException(\"foo\");\n" + 
			"    }\n" + 
			"}\n",
			"test/impl/SuperTypeExtend.java", //--------------------------------------------
			"package test.impl;\n" + 
			"import test.intf.SubOne;\n" + 
			"import test.intf.SubTwo;\n" + 
			"public interface SuperTypeExtend extends test.intf.SuperType\n" + 
			"{\n" + 
			"    public SuperTypeExtend plus(SubOne addend);\n" + 
			"    public SuperTypeExtend plus(SubTwo addend);\n" + 
			"}\n",
			"test/impl/SuperTypeExtendImpl.java", //--------------------------------------------
			"package test.impl;\n" + 
			"public abstract class SuperTypeExtendImpl implements SuperTypeExtend\n" + 
			"{\n" + 
			"}\n",
			"test/intf/SubOne.java", //--------------------------------------------
			"package test.intf;\n" + 
			"public interface SubOne<Owner> extends SuperType<Owner>\n" + 
			"{\n" + 
			"    public SubOne<Owner> plus(SubOne addend);\n" + 
			"    public SubTwo<Owner> plus(SubTwo addend);\n" + 
			"}\n",
			"test/intf/SubTwo.java", //--------------------------------------------
			"package test.intf;\n" + 
			"public interface SubTwo<Owner> extends SuperType<Owner>\n" + 
			"{\n" + 
			"    public SubTwo<Owner> plus(SubOne addend);\n" + 
			"    public SubTwo<Owner> plus(SubTwo addend);\n" + 
			"}\n",
			"test/intf/SuperType.java", //--------------------------------------------
			"package test.intf;\n" + 
			"public interface SuperType<Owner>\n" + 
			"{\n" + 
			"    public SuperType<Owner> plus(SubOne addend);\n" + 
			"    public SuperType<Owner> plus(SubTwo addend);\n" + 
			"}\n",
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test154() {
	this.runConformTest(
		new String[] {
			"test/impl/SubOneImpl.java", //--------------------------------------------
			"package test.impl;\n" +
			"public abstract class SubOneImpl extends SuperTypeExtendImpl implements test.impl.SubOne {\n" + 
			"	public SubOneImpl plus(test.impl.SubOne attribute) {\n" + 
			"		throw new RuntimeException(\"foo\");\n" + 
			"	}\n" + 
			"	public SubTwoImpl plus(SubTwo attribute) {\n" + 
			"		throw new RuntimeException(\"foo\");\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"abstract class SubSubOneImpl extends SubOneImpl {\n" + 
			"}\n" + 
			"\n" + 
			"abstract class SubTwoImpl extends SuperTypeExtendImpl implements test.impl.SubTwo {\n" + 
			"	public SubTwoImpl plus(SubOne attribute) {\n" + 
			"		throw new RuntimeException(\"foo\");\n" + 
			"	}\n" + 
			"	public SubTwoImpl plus(test.impl.SubTwo attribute) {\n" + 
			"		throw new RuntimeException(\"foo\");\n" + 
			"	}\n" + 
			"}\n" + 
			"\n" + 
			"interface SuperTypeExtend extends test.impl.SuperType {\n" + 
			"	public SuperTypeExtend plus(SubOne addend);\n" + 
			"	public SuperTypeExtend plus(SubTwo addend);\n" + 
			"}\n" + 
			"\n" + 
			"abstract class SuperTypeExtendImpl implements SuperTypeExtend {\n" + 
			"}\n" + 
			"\n" + 
			"interface SubOne<Owner> extends SuperType<Owner> {\n" + 
			"	public SubOne<Owner> plus(SubOne addend);\n" + 
			"	public SubTwo<Owner> plus(SubTwo addend);\n" + 
			"}\n" + 
			"\n" + 
			"interface SubTwo<Owner> extends SuperType<Owner> {\n" + 
			"	public SubTwo<Owner> plus(SubOne addend);\n" + 
			"	public SubTwo<Owner> plus(SubTwo addend);\n" + 
			"}\n" + 
			"\n" + 
			"interface SuperType<Owner> {\n" + 
			"	public SuperType<Owner> plus(SubOne addend);\n" + 
			"	public SuperType<Owner> plus(SubTwo addend);\n" + 
			"}\n",
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test155() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------------------
			"class A {}\n" + 
			"class B {}\n" + 
			"interface I {\n" + 
			"	A foo();\n" + 
			"}\n" + 
			"interface J {\n" + 
			"	B foo();\n" + 
			"}\n" + 
			"public abstract class X implements I, J {\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	public abstract class X implements I, J {\n" + 
		"	                      ^\n" + 
		"The return type is incompatible with J.foo(), I.foo()\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test156() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------------------
			"class Common {}\n" + 
			"class A extends Common {}\n" + 
			"class B extends Common {}\n" + 
			"interface I {\n" + 
			"	A foo();\n" + 
			"}\n" + 
			"interface J {\n" + 
			"	B foo();\n" + 
			"}\n" + 
			"public abstract class X implements I, J {\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	public abstract class X implements I, J {\n" + 
		"	                      ^\n" + 
		"The return type is incompatible with J.foo(), I.foo()\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test157() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------------------
			"interface A {\n" + 
			"	A foo();\n" + 
			"}\n" + 
			"interface B {\n" + 
			"	B foo();\n" + 
			"}\n" + 
			"interface C extends A, B {}\n" + 
			"\n" + 
			"class Root {\n" + 
			"	public C foo() { return null; }\n" + 
			"}\n" + 
			"public abstract class X extends Root implements A, B {\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	interface C extends A, B {}\n" + 
		"	          ^\n" + 
		"The return type is incompatible with B.foo(), A.foo()\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test158() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------------------
			"import java.io.Serializable;\n" + 
			"\n" + 
			"interface AFoo { \n" + 
			"	Serializable foo();\n" + 
			"	Serializable bar();\n" + 
			"}\n" + 
			"interface BFoo { \n" + 
			"	Cloneable foo(); \n" + 
			"	Cloneable bar(); \n" + 
			"}\n" + 
			"\n" + 
			"interface C extends Serializable, Cloneable {}\n" + 
			"\n" + 
			"class Root {\n" + 
			"	public C foo() { return null; }\n" + 
			"}\n" + 
			"public abstract class X extends Root implements AFoo, BFoo {\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 17)\n" + 
		"	public abstract class X extends Root implements AFoo, BFoo {\n" + 
		"	                      ^\n" + 
		"The return type is incompatible with BFoo.bar(), AFoo.bar()\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223986 - variation
public void test159() {
	this.runNegativeTest(
		new String[] {
			"X.java", //--------------------------------------------
			"import java.io.Serializable;\n" + 
			"\n" + 
			"interface AFoo { \n" + 
			"	Serializable foo();\n" + 
			"	Serializable bar();\n" + 
			"}\n" + 
			"interface BFoo { \n" + 
			"	Cloneable foo(); \n" + 
			"	Cloneable bar(); \n" + 
			"}\n" + 
			"interface C extends Serializable, Cloneable {}\n" + 
			"class Root {\n" + 
			"	public C foo() { return null; }\n" + 
			"}\n" + 
			"public abstract class X extends Root implements AFoo, BFoo {}\n" +
			"abstract class Y extends X {}\n" +
			"class Z extends X {}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 15)\n" + 
		"	public abstract class X extends Root implements AFoo, BFoo {}\n" + 
		"	                      ^\n" + 
		"The return type is incompatible with BFoo.bar(), AFoo.bar()\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 17)\n" + 
		"	class Z extends X {}\n" + 
		"	      ^\n" + 
		"The type Z must implement the inherited abstract method AFoo.bar()\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=208010
public void test160() {
	this.runConformTest(
		new String[] {
			"bar/X.java", //--------------------------------------------
			"package bar;" + 
			"public class X {\n" + 
			"	static void foo() {}\n" +
			"}",
			"foo/Y.java", //--------------------------------------------
			"package foo;" + 
			"public class Y extends bar.X {\n" + 
			"	static void foo() {}\n" +
			"}",
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227185
public void test161() {
	this.runConformTest(
		new String[] {
			"Concrete.java",
			"abstract class SuperAbstract<Owner, Type> {\n" + 
			"	abstract Object foo(Type other);\n" + 
			"}\n" + 
			"abstract class HalfGenericSuper<Owner> extends SuperAbstract<Owner, String> {\n" + 
			"	@Override abstract Object foo(String other);\n" + 
			"}\n" + 
			"abstract class AbstractImpl<Owner> extends HalfGenericSuper<Owner> {\n" + 
			"	@Override Object foo(String other) { return null; }\n" + 
			"}\n" +
			"class Concrete extends AbstractImpl{}" 
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227185 - variant
public void test162() {
	this.runConformTest(
		new String[] {
			"Concrete.java",
			"abstract class SuperAbstract<Owner, Type> {\n" + 
			"	abstract Object foo(Type other);\n" + 
			"}\n" + 
			"class HalfGenericSuper<Owner> extends SuperAbstract<Owner, String> {\n" + 
			"	@Override Object foo(String other) { return null; }\n" +
			"}\n" + 
			"abstract class AbstractImpl<Owner> extends HalfGenericSuper<Owner> {}\n" + 
			"class HalfConcrete extends HalfGenericSuper {}\n" + 
			"class Concrete extends AbstractImpl{}" 
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227185 - variant return types
public void test163() {
	this.runNegativeTest(
		new String[] {
			"Concrete.java",
			"abstract class SuperAbstract<Owner, Type> {\n" +
			"	abstract Type foo(Type other);\n" +
			"}\n" +
			"class HalfGenericSuper<Owner> extends SuperAbstract<Owner, String> {\n" +
			"	@Override Object foo(String other) { return null; }\n" +
			"}\n" + 
			"class Concrete extends HalfGenericSuper{}" 
		},
		"----------\n" + 
		"1. ERROR in Concrete.java (at line 5)\n" + 
		"	@Override Object foo(String other) { return null; }\n" + 
		"	          ^^^^^^\n" + 
		"The return type is incompatible with SuperAbstract<Owner,String>.foo(String)\n" + 
		"----------\n" + 
		"2. WARNING in Concrete.java (at line 7)\n" + 
		"	class Concrete extends HalfGenericSuper{}\n" + 
		"	                       ^^^^^^^^^^^^^^^^\n" + 
		"HalfGenericSuper is a raw type. References to generic type HalfGenericSuper<Owner> should be parameterized\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227185 - variant return types
public void test164() {
	this.runNegativeTest(
		new String[] {
			"Concrete.java",
			"interface I<Owner, Type> {\n" + 
			"	Type foo(Type other);\n" + 
			"	Owner foo2(Type other);\n" + 
			"	Object foo3(Type other);\n" + 
			"}\n" + 
			"class HalfGenericSuper {\n" + 
			"	public Object foo(String other) { return null; }\n" +
			"	public Integer foo2(String other) { return null; }\n" +
			"	public String foo3(String other) { return null; }\n" +
			"}\n" + 
			"class HalfConcrete extends HalfGenericSuper {}\n" + 
			"class Concrete extends HalfConcrete implements I<Object, String> {}" 
		},
		"----------\n" + 
		"1. ERROR in Concrete.java (at line 12)\r\n" + 
		"	class Concrete extends HalfConcrete implements I<Object, String> {}\r\n" + 
		"	      ^^^^^^^^\n" + 
		"The return type is incompatible with I<Object,String>.foo(String), HalfGenericSuper.foo(String)\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227185 - variant return types
public void test165() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { void foo() {} }\n" +
			"class Y extends X { @Override int foo() { return 1; } }" 
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	class Y extends X { @Override int foo() { return 1; } }\n" + 
		"	                              ^^^\n" + 
		"The return type is incompatible with X.foo()\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=238014
public void test166() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X extends A implements I<String> {}\n" +
			"interface I<T> { void foo(T item); }\n" +
			"class A {\n" +
			"	public void foo(Object item) {}\n" +
			"	public void foo(String item) {}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	class X extends A implements I<String> {}\n" + 
		"	      ^\n" + 
		"Name clash: The method foo(Object) of type A has the same erasure as foo(T) of type I<T> but does not override it\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=238817
public void test167() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X implements I<String>, J<String> {\n" +
			"	public <T3> void foo(T3 t, String s) {}\n" +
			"}\n" +
			"interface I<U1> { <T1> void foo(T1 t, U1 u); }\n" +
			"interface J<U2> { <T2> void foo(T2 t, U2 u); }\n"
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=236096
public void test168() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> extends Y {\n" +
			"	@Override <V> void foo(M m) { }\n" +
			"	@Override <V> M bar() { return null; }\n" +
			"}\n" +
			"class Y<T> {\n" +
			"	class M<V> {}\n" +
			"	<V> void foo(M<V> m) {}\n" +
			"	<V> M<V> bar() { return null; }\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 1)\n" + 
		"	class X<T> extends Y {\n" + 
		"	                   ^\n" + 
		"Y is a raw type. References to generic type Y<T> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	@Override <V> void foo(M m) { }\n" + 
		"	                   ^^^^^^^^\n" + 
		"Name clash: The method foo(Y.M) of type X<T> has the same erasure as foo(Y.M) of type Y but does not override it\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 2)\n" + 
		"	@Override <V> void foo(M m) { }\n" + 
		"	                   ^^^^^^^^\n" + 
		mustOverrideMessage("foo(Y.M)", "X<T>") +
		"----------\n" + 
		"4. WARNING in X.java (at line 2)\n" + 
		"	@Override <V> void foo(M m) { }\n" + 
		"	                       ^\n" + 
		"Y.M is a raw type. References to generic type Y<T>.M<V> should be parameterized\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 3)\n" + 
		"	@Override <V> M bar() { return null; }\n" + 
		"	              ^\n" + 
		"Y.M is a raw type. References to generic type Y<T>.M<V> should be parameterized\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 3)\n" + 
		"	@Override <V> M bar() { return null; }\n" + 
		"	                ^^^^^\n" + 
		"Name clash: The method bar() of type X<T> has the same erasure as bar() of type Y but does not override it\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 3)\n" + 
		"	@Override <V> M bar() { return null; }\n" + 
		"	                ^^^^^\n" + 
		mustOverrideMessage("bar()", "X<T>") +
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=243820
public void test169() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> {\n" +
			"	interface I<S> {}\n" +
			"	interface J { A foo(A a, I<String> i); }\n" +
			"	static class A {}\n" +
			"	static class B implements J {\n" +
			"		public R foo(A a, I i) { return null; }\n" +
			"	}\n" +
			"}\n" +
			"class R<T> extends X.A {}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 6)\n" + 
		"	public R foo(A a, I i) { return null; }\n" + 
		"	       ^\n" + 
		"R is a raw type. References to generic type R<T> should be parameterized\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 6)\n" + 
		"	public R foo(A a, I i) { return null; }\n" + 
		"	                  ^\n" + 
		"X.I is a raw type. References to generic type X<T>.I<S> should be parameterized\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=243820
public void test169a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> {\n" +
			"	abstract class B implements J {\n" +
			"		public R foo(X<String>.B b, I i) { return null; }\n" +
			"	}\n" +
			"}\n" +
			"interface I<S> {}\n" +
			"interface J { A foo(A a, I<String> i); }\n" +
			"class A {}\n" +
			"class R<T> extends A {}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	public R foo(X<String>.B b, I i) { return null; }\n" + 
		"	       ^\n" + 
		"R is a raw type. References to generic type R<T> should be parameterized\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 3)\n" + 
		"	public R foo(X<String>.B b, I i) { return null; }\n" + 
		"	                            ^\n" + 
		"I is a raw type. References to generic type I<S> should be parameterized\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249140
public void test174() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X extends Y implements I { }\n" +
			"abstract class Y { public abstract Object m(); }\n" +
			"abstract class A implements I, J { }\n" +
			"abstract class B implements J, I { }\n" +
			"interface I { String m(); }\n" +
			"interface J { Object m(); }\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	class X extends Y implements I { }\n" + 
		"	      ^\n" + 
		"The type X must implement the inherited abstract method Y.m()\n" + 
		"----------\n"
	);
}//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251091
public void test177() {
	if (new CompilerOptions(getCompilerOptions()).sourceLevel >= ClassFileConstants.JDK1_6) {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"interface I { I foo(Collection<?> c); }\n" +
				"class A extends LinkedHashMap {\n" +
				"	public A foo(Collection c) { return this; }\n" +
				"}\n" +
				"class X extends A implements I {\n" +
				"	@Override public X foo(Collection<?> c) { return this; }\n" +
				"}"
			},
			""
		);
	} else {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"interface I { I foo(Collection<?> c); }\n" +
				"class A extends LinkedHashMap {\n" +
				"	public A foo(Collection c) { return this; }\n" +
				"}\n" +
				"class X extends A implements I {\n" +
				"	@Override public X foo(Collection<?> c) { return this; }\n" +
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	class A extends LinkedHashMap {\n" +
			"	      ^\n" +
			"The serializable class A does not declare a static final serialVersionUID field of type long\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 3)\n" +
			"	class A extends LinkedHashMap {\n" +
			"	                ^^^^^^^^^^^^^\n" +
			"LinkedHashMap is a raw type. References to generic type LinkedHashMap<K,V> should be parameterized\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 4)\n" +
			"	public A foo(Collection c) { return this; }\n" +
			"	             ^^^^^^^^^^\n" +
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 6)\n" +
			"	class X extends A implements I {\n" +
			"	      ^\n" +
			"The serializable class X does not declare a static final serialVersionUID field of type long\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 7)\n" +
			"	@Override public X foo(Collection<?> c) { return this; }\n" +
			"	                   ^^^^^^^^^^^^^^^^^^^^\n" +
			"The method foo(Collection<?>) of type X must override a superclass method\n" +
			"----------\n"
		);
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=241821
public void test178() {
	this.runConformTest(
			new String[] {
				"I.java",
				"import java.util.*;\n" +
				"interface I<E> extends I1<E>, I2<E>, I3<E> {}\n" +
				"interface I1<E> { List<E> m(); }\n" +
				"interface I2<E> { Queue<E> m(); }\n" +
				"interface I3<E> { LinkedList<E> m(); }"
			},
			""
		);
}
}