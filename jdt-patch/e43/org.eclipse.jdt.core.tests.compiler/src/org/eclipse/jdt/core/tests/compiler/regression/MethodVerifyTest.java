/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 388795 - [compiler] detection of name clash depends on order of super interfaces
 *								bug 395681 - [compiler] Improve simulation of javac6 behavior from bug 317719 after fixing bug 388795
 *								bug 409473 - [compiler] JDT cannot compile against JRE 1.8
 *								Bug 410325 - [1.7][compiler] Generified method override different between javac and eclipse compiler
 *	   Andy Clement - Contribution for
 *								bug 406928 - computation of inherited methods seems damaged (affecting @Overrides)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class MethodVerifyTest extends AbstractComparableTest {
	static {
//		TESTS_NAMES = new String[] { "testBug406928" };
//		TESTS_NUMBERS = new int[] { 213 };
//		TESTS_RANGE = new int[] { 190, -1};
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

	protected Map getCompilerOptions() {
		Map compilerOptions = super.getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		return compilerOptions;
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
				"1. ERROR in J.java (at line 1)\n" +
				"	public class J<T> implements I<A> { public void foo(T t) {} }\n" +
				"	             ^\n" +
				"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" +
				"----------\n" +
				"2. ERROR in J.java (at line 1)\n" +
				"	public class J<T> implements I<A> { public void foo(T t) {} }\n" +
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
			"1. ERROR in J.java (at line 1)\n" +
			"	public class J<T> implements I<A> { public void foo(T t) {} }\n" +
			"	             ^\n" +
			"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" +
			"----------\n" +
			"2. ERROR in J.java (at line 1)\n" +
			"	public class J<T> implements I<A> { public void foo(T t) {} }\n" +
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
			"1. ERROR in J.java (at line 1)\n" +
			"	public class J<T> implements I<A> { public void foo(T t) {} }\n" +
			"	             ^\n" +
			"The type J<T> must implement the inherited abstract method I<A>.foo(A)\n" +
			"----------\n" +
			"2. ERROR in J.java (at line 1)\n" +
			"	public class J<T> implements I<A> { public void foo(T t) {} }\n" +
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
			"1. ERROR in X.java (at line 1)\n" +
			"	abstract class X6 extends A implements I {}\n" +
			"	               ^^\n" +
			"The type X6 must implement the inherited abstract method I.foo() to override A.foo()\n" +
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
			"1. ERROR in A.java (at line 2)\n" +
			"	class B extends A { @Override short get(short i, short s) {return i; } }\n" +
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
			"1. ERROR in ALL.java (at line 4)\n" +
			"	class C extends B { @Override public A foo() { return null; } }\n" +
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
			"1. ERROR in A.java (at line 3)\n" +
			"	abstract class A implements I { void foo(G<A> x) {} }\n" +
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
			"1. ERROR in A.java (at line 3)\n" +
			"	abstract class A implements I { I foo(G<A> x) { return null; } }\n" +
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
			"1. ERROR in X.java (at line 1)\n" +
			"	abstract class X1 extends A implements I {}\n" +
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
			"1. ERROR in A.java (at line 2)\n" +
			"	class Y3 extends A { @Override void foo(Class<Object> s) {} }\n" +
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
			"1. ERROR in X.java (at line 1)\n" +
			"	abstract class X1 extends A implements I {}\n" +
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
			"1. ERROR in X.java (at line 1)\n" +
			"	abstract class X2 extends A implements I {}\n" +
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
			"1. ERROR in X.java (at line 1)\n" +
			"	abstract class X3 extends A implements I {}\n" +
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
			"1. ERROR in X.java (at line 1)\n" +
			"	abstract class X4 extends A implements I {}\n" +
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
			"1. ERROR in X.java (at line 1)\n" +
			"	class X5 extends A implements I { public <T> void foo(Class<T> s) {} }\n" +
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
			"1. ERROR in X.java (at line 3)\n" +
			"	class Z extends Y { void test(X<Number> a) {} }\n" +
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
			"1. ERROR in B.java (at line 2)\n" +
			"	@Override void foo(java.util.Map<String, Class<?>> m) { } \n" +
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
			"1. ERROR in B.java (at line 2)\n" +
			"	@Override void foo(java.util.Map<String, Class<?>> m) { } \n" +
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
			"1. ERROR in A.java (at line 5)\n" +
			"	class B extends A<String> {}\n" +
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
			"1. ERROR in A.java (at line 5)\n" +
			"	public <E extends Object> void m(E e) {}\n" +
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
			"1. ERROR in A.java (at line 8)\n" +
			"	public <E extends Object> void m(E e) {}\n" +
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
			"1. ERROR in X.java (at line 2)\n" +
			"	class Y<T> extends X<T> { void test(Object o, T t) {} }\n" +
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
			"1. ERROR in X.java (at line 1)\n" +
			"	interface X { long hashCode(); }\n" +
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
			"1. ERROR in X.java (at line 3)\n" +
			"	public class X<T extends I&J> {}\n" +
			"	               ^\n" +
			"The return types are incompatible for the inherited methods I.foo(), J.foo()\n" +
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
				"class A { public Object foo() { return null; } }\n" +
				"public class X<T extends A&I> {}\n" +
				"interface J extends I { Object foo(); }\n" +
				"class Y<T extends I&J> {}\n" +
				"class Z<T extends J&I> {}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	public class X<T extends A&I> {}\n" + 
			"	               ^\n" + 
			"The return types are incompatible for the inherited methods I.foo(), A.foo()\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	interface J extends I { Object foo(); }\n" + 
			"	                        ^^^^^^\n" + 
			"The return type is incompatible with I.foo()\n" + 
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
		if (this.complianceLevel < ClassFileConstants.JDK1_7) {
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
		} else {
			this.runNegativeTest(
					new String[] {
							"Y.java",
							"abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" +
									"	public abstract boolean equalTo(Number other);\n" +
									"}\n" +
									"interface Equivalent<T> { boolean equalTo(T other); }\n" +
									"interface EqualityComparable<T> { boolean equalTo(T other); }\n"
					},
					"----------\n" + 
					"1. ERROR in Y.java (at line 1)\n" + 
					"	abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" + 
					"	               ^\n" + 
					"Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(T) of type Equivalent<T> but does not override it\n" + 
					"----------\n");
		}
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
			this.complianceLevel < ClassFileConstants.JDK1_7 ?
			"----------\n" +
			"1. ERROR in Y.java (at line 2)\n" +
			"	public abstract boolean equalTo(Object other);\n" +
			"	                        ^^^^^^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type EqualityComparable<T> but does not override it\n" +
			"----------\n" +
			"2. ERROR in Y.java (at line 2)\n" +
			"	public abstract boolean equalTo(Object other);\n" +
			"	                        ^^^^^^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type Equivalent<T> but does not override it\n" +
			"----------\n" :
			// name clash: equalTo(java.lang.Object) in Y and equalTo(T) in Equivalent<java.lang.String> have the same erasure, yet neither overrides the other
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	abstract class Y implements Equivalent<String>, EqualityComparable<Integer> {\n" + 
			"	               ^\n" + 
			"Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(T) of type Equivalent<T> but does not override it\n" + 
			"----------\n" + 
			"2. ERROR in Y.java (at line 2)\n" + 
			"	public abstract boolean equalTo(Object other);\n" + 
			"	                        ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type EqualityComparable<T> but does not override it\n" + 
			"----------\n" + 
			"3. ERROR in Y.java (at line 2)\n" + 
			"	public abstract boolean equalTo(Object other);\n" + 
			"	                        ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method equalTo(Object) of type Y has the same erasure as equalTo(T) of type Equivalent<T> but does not override it\n" + 
			"----------\n"
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
		this.runNegativeTest(
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
			this.complianceLevel < ClassFileConstants.JDK1_7 ? 
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	class YYY implements J, I { public void foo(A a) {} }\n" + 
			"	                                            ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	class XXX implements I, J { public void foo(A a) {} }\n" + 
			"	                                            ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 6)\n" + 
			"	class ZZZ implements K { public void foo(A a) {} }\n" + 
			"	                                         ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 7)\n" + 
			"	interface I { void foo(A a); }\n" + 
			"	                       ^\n" + 
			"A is a raw type. References to generic type A<T> should be parameterized\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 9)\n" + 
			"	interface K extends I { void foo(A<String> a); }\n" + 
			"	                             ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type K has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n" : 
				"----------\n" + 
				"1. WARNING in X.java (at line 4)\n" + 
				"	class YYY implements J, I { public void foo(A a) {} }\n" + 
				"	                                            ^\n" + 
				"A is a raw type. References to generic type A<T> should be parameterized\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 5)\n" + 
				"	class XXX implements I, J { public void foo(A a) {} }\n" + 
				"	                                            ^\n" + 
				"A is a raw type. References to generic type A<T> should be parameterized\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 6)\n" + 
				"	class ZZZ implements K { public void foo(A a) {} }\n" + 
				"	                                         ^\n" + 
				"A is a raw type. References to generic type A<T> should be parameterized\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 7)\n" + 
				"	interface I { void foo(A a); }\n" + 
				"	                       ^\n" + 
				"A is a raw type. References to generic type A<T> should be parameterized\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	interface K extends I { void foo(A<String> a); }\n" + 
				"	                             ^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method foo(A<String>) of type K has the same erasure as foo(A) of type I but does not override it\n" + 
				"----------\n");
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
			"----------\n" + 
			"8. ERROR in XX.java (at line 6)\n" + 
			"	interface K extends I { void foo(A<String> a); }\n" + 
			"	                             ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type K has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n"
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
			"1. ERROR in X.java (at line 1)\n" +
			"	public abstract class X extends Y implements I { }\n" +
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
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X extends H<Number> { void foo(A<?> a) {} }\n" +
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
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X { void test(E<Integer,Integer> e) { e.id(new Integer(2)); } }\n" +
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
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X { void test(E<Integer,Integer> e) { e.id(new Integer(111)); } }\n" +
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
			"1. ERROR in X.java (at line 4)\n" +
			"	m.id(new Integer(111));\n" +
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
			this.complianceLevel < ClassFileConstants.JDK1_7 ?
			"----------\n" + 
			"1. WARNING in p\\X.java (at line 6)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Type safety: A generic array of List<Object> is created for a varargs parameter\n" + 
			"----------\n" + 
			"2. WARNING in p\\X.java (at line 6)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Type safety: Unchecked invocation foo(List) of the generic method foo(List<T>...) of type Z\n" + 
			"----------\n" + 
			"3. WARNING in p\\X.java (at line 6)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	                 ^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<Object>\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in p\\Y.java (at line 4)\n" + 
			"	List l = null;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" :
				"----------\n" + 
				"1. WARNING in p\\X.java (at line 6)\n" + 
				"	public X() { foo(data.l); }\n" + 
				"	             ^^^^^^^^^^^\n" + 
				"Type safety: A generic array of List<Object> is created for a varargs parameter\n" + 
				"----------\n" + 
				"2. WARNING in p\\X.java (at line 6)\n" + 
				"	public X() { foo(data.l); }\n" + 
				"	             ^^^^^^^^^^^\n" + 
				"Type safety: Unchecked invocation foo(List) of the generic method foo(List<T>...) of type Z\n" + 
				"----------\n" + 
				"3. WARNING in p\\X.java (at line 6)\n" + 
				"	public X() { foo(data.l); }\n" + 
				"	                 ^^^^^^\n" + 
				"Type safety: The expression of type List needs unchecked conversion to conform to List<Object>\n" + 
				"----------\n" + 
				"----------\n" + 
				"1. WARNING in p\\Y.java (at line 4)\n" + 
				"	List l = null;\n" + 
				"	^^^^\n" + 
				"List is a raw type. References to generic type List<E> should be parameterized\n" + 
				"----------\n" + 
				"2. WARNING in p\\Y.java (at line 5)\n" + 
				"	public static <T> void foo(T... e) {}\n" + 
				"	                                ^\n" + 
				"Type safety: Potential heap pollution via varargs parameter e\n" + 
				"----------\n" + 
				"----------\n" + 
				"1. WARNING in p\\Z.java (at line 4)\n" + 
				"	public static <T> void foo(List<T>... e) {}\n" + 
				"	                                      ^\n" + 
				"Type safety: Potential heap pollution via varargs parameter e\n" + 
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
			this.complianceLevel < ClassFileConstants.JDK1_7 ?
			"----------\n" + 
			"1. WARNING in p\\X.java (at line 5)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Type safety: A generic array of List<Object> is created for a varargs parameter\n" + 
			"----------\n" + 
			"2. WARNING in p\\X.java (at line 5)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Type safety: Unchecked invocation foo(List) of the generic method foo(List<T>...) of type Y\n" + 
			"----------\n" + 
			"3. WARNING in p\\X.java (at line 5)\n" + 
			"	public X() { foo(data.l); }\n" + 
			"	                 ^^^^^^\n" + 
			"Type safety: The expression of type List needs unchecked conversion to conform to List<Object>\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in p\\Y.java (at line 4)\n" + 
			"	List l = null;\n" + 
			"	^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" :
				"----------\n" + 
				"1. WARNING in p\\X.java (at line 5)\n" + 
				"	public X() { foo(data.l); }\n" + 
				"	             ^^^^^^^^^^^\n" + 
				"Type safety: A generic array of List<Object> is created for a varargs parameter\n" + 
				"----------\n" + 
				"2. WARNING in p\\X.java (at line 5)\n" + 
				"	public X() { foo(data.l); }\n" + 
				"	             ^^^^^^^^^^^\n" + 
				"Type safety: Unchecked invocation foo(List) of the generic method foo(List<T>...) of type Y\n" + 
				"----------\n" + 
				"3. WARNING in p\\X.java (at line 5)\n" + 
				"	public X() { foo(data.l); }\n" + 
				"	                 ^^^^^^\n" + 
				"Type safety: The expression of type List needs unchecked conversion to conform to List<Object>\n" + 
				"----------\n" + 
				"----------\n" + 
				"1. WARNING in p\\Y.java (at line 4)\n" + 
				"	List l = null;\n" + 
				"	^^^^\n" + 
				"List is a raw type. References to generic type List<E> should be parameterized\n" + 
				"----------\n" + 
				"2. WARNING in p\\Y.java (at line 5)\n" + 
				"	public static <T> void foo(T... e) {}\n" + 
				"	                                ^\n" + 
				"Type safety: Potential heap pollution via varargs parameter e\n" + 
				"----------\n" + 
				"3. WARNING in p\\Y.java (at line 6)\n" + 
				"	public static <T> void foo(List<T>... e) {}\n" + 
				"	                                      ^\n" + 
				"Type safety: Potential heap pollution via varargs parameter e\n" + 
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
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X1.java (at line 2)\n" + 
				"	public class X1 extends LinkedHashMap<String, String> {\n" + 
				"	             ^^\n" + 
				"The serializable class X1 does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n" + 
				"2. WARNING in X1.java (at line 3)\n" + 
				"	public Object putAll(Map<String,String> a) { return null; }\n" + 
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method putAll(Map<String,String>) of type X1 has the same erasure as putAll(Map<? extends K,? extends V>) of type HashMap<K,V> but does not override it\n" + 
				"----------\n":
					"----------\n" + 
					"1. WARNING in X1.java (at line 2)\n" + 
					"	public class X1 extends LinkedHashMap<String, String> {\n" + 
					"	             ^^\n" + 
					"The serializable class X1 does not declare a static final serialVersionUID field of type long\n" + 
					"----------\n" + 
					"2. ERROR in X1.java (at line 3)\n" + 
					"	public Object putAll(Map<String,String> a) { return null; }\n" + 
					"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
					"Name clash: The method putAll(Map<String,String>) of type X1 has the same erasure as putAll(Map<? extends K,? extends V>) of type HashMap<K,V> but does not override it\n" + 
					"----------\n";
		this.runNegativeTest(
			new String[] {
				"X1.java",
				"import java.util.*;\n" +
				"public class X1 extends LinkedHashMap<String, String> {\n" +
				"    public Object putAll(Map<String,String> a) { return null; }\n" +
				"}\n"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:4: name clash: putAll(Map<String,String>) in X1 and putAll(Map<? extends K,? extends V>) in HashMap have the same erasure, yet neither overrides the other
        public Object putAll(Map<String,String> a) { return null; }
                      ^
  where K,V are type-variables:
    K extends Object declared in class HashMap
    V extends Object declared in class HashMap
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048a() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X2.java (at line 2)\n" + 
				"	public Object foo(I<String> z) { return null; }\n" + 
				"	              ^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method foo(I<String>) of type X2 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
				"----------\n":		
					"----------\n" + 
					"1. ERROR in X2.java (at line 2)\n" + 
					"	public Object foo(I<String> z) { return null; }\n" + 
					"	              ^^^^^^^^^^^^^^^^\n" + 
					"Name clash: The method foo(I<String>) of type X2 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
					"----------\n";
		this.runNegativeTest(
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
			expectedCompilerLog
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X2 and foo(I<? extends T>) in Y have the same erasure, yet neither overrides the other
    public Object foo(I<String> z) { return null; }
                  ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
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
			"1. ERROR in X3.java (at line 2)\n" +
			"	public void foo(I<String> z) {}\n" +
			"	            ^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(I<String>) of type X3 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" +
			"----------\n"
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X3 and foo(I<? extends T>) in Y have the same erasure, yet neither overrides the other
    public void foo(I<String> z) {}
                ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048c() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X4.java (at line 2)\n" + 
				"	public String foo(I<String> z) { return null; }\n" + 
				"	              ^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method foo(I<String>) of type X4 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X4.java (at line 2)\n" + 
					"	public String foo(I<String> z) { return null; }\n" + 
					"	              ^^^^^^^^^^^^^^^^\n" + 
					"Name clash: The method foo(I<String>) of type X4 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
					"----------\n";
		this.runNegativeTest(
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
			expectedCompilerLog
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X4 and foo(I<? extends T>) in Y have the same erasure, yet neither overrides the other
    public String foo(I<String> z) { return null; }
                  ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048d() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X5.java (at line 2)\n" + 
				"	public Object foo(I<String> z) { return null; }\n" + 
				"	              ^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method foo(I<String>) of type X5 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X5.java (at line 2)\n" + 
					"	public Object foo(I<String> z) { return null; }\n" + 
					"	              ^^^^^^^^^^^^^^^^\n" + 
					"Name clash: The method foo(I<String>) of type X5 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
					"----------\n";
		this.runNegativeTest(
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
			expectedCompilerLog
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X5 and foo(I<? extends T>) in Y have the
 same erasure, yet neither overrides the other
    public Object foo(I<String> z) { return null; }
                  ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048e() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X6.java (at line 2)\n" + 
				"	public void foo(I<String> z) {}\n" + 
				"	            ^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method foo(I<String>) of type X6 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X6.java (at line 2)\n" + 
					"	public void foo(I<String> z) {}\n" + 
					"	            ^^^^^^^^^^^^^^^^\n" + 
					"Name clash: The method foo(I<String>) of type X6 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
					"----------\n";
		this.runNegativeTest(
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
			expectedCompilerLog
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X6 and foo(I<? extends T>) in Y have the same erasure, yet neither overrides the other
    public void foo(I<String> z) {}
                ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85900
	public void test048f() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X7.java (at line 2)\n" + 
				"	public String foo(I<String> z) { return null; }\n" + 
				"	              ^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method foo(I<String>) of type X7 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X7.java (at line 2)\n" + 
					"	public String foo(I<String> z) { return null; }\n" + 
					"	              ^^^^^^^^^^^^^^^^\n" + 
					"Name clash: The method foo(I<String>) of type X7 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" + 
					"----------\n";
		this.runNegativeTest(
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
			expectedCompilerLog
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X7 and foo(I<? extends T>) in Y have the same erasure, yet neither overrides the other
    public String foo(I<String> z) { return null; }
                  ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
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
			"1. ERROR in X8.java (at line 2)\n" +
			"	public Object foo(I<String> z) { return null; }\n" +
			"	              ^^^^^^^^^^^^^^^^\n" +
			"Name clash: The method foo(I<String>) of type X8 has the same erasure as foo(I<? extends T>) of type Y<T> but does not override it\n" +
			"----------\n"
		);
/* javac 7
X.java:2: name clash: foo(I<String>) in X8 and foo(I<? extends T>) in Y have the  same erasure, yet neither overrides the other
    public Object foo(I<String> z) { return null; }
                  ^
  where T is a type-variable:
    T extends Object declared in class Y
1 error
 */
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
			"1. ERROR in X.java (at line 6)\n" +
			"	@Override T id(T x) { return x; }\n" +
			"	            ^^^^^^^\n" +
			"Method id(T) has the same erasure id(A) as another method in type Y<T>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	@Override T id(T x) { return x; }\n" +
			"	            ^^^^^^^\n" +
			"Name clash: The method id(T) of type Y<T> has the same erasure as id(A) of type X<T> but does not override it\n" +
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
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X.java (at line 2)\n" + 
				"	public static <S extends A> S foo() { System.out.print(\"A\"); return null; }\n" + 
				"	                              ^^^^^\n" + 
				"Duplicate method foo() in type X\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	public static <N extends B> N foo() { System.out.print(\"B\"); return null; }\n" + 
				"	                              ^^^^^\n" + 
				"Duplicate method foo() in type X\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 7)\n" + 
				"	new X().<B>foo();\n" + 
				"	^^^^^^^^^^^^^^^^\n" + 
				"The static method foo() from the type X should be accessed in a static way\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X.java (at line 2)\n" + 
					"	public static <S extends A> S foo() { System.out.print(\"A\"); return null; }\n" + 
					"	                              ^^^^^\n" + 
					"Duplicate method foo() in type X\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 3)\n" + 
					"	public static <N extends B> N foo() { System.out.print(\"B\"); return null; }\n" + 
					"	                              ^^^^^\n" + 
					"Duplicate method foo() in type X\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 6)\n" + 
					"	X.<B>foo();\n" + 
					"	     ^^^\n" + 
					"Bound mismatch: The generic method foo() of type X is not applicable for the arguments (). The inferred type B is not a valid substitute for the bounded parameter <S extends A>\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 7)\n" + 
					"	new X().<B>foo();\n" + 
					"	           ^^^\n" + 
					"Bound mismatch: The generic method foo() of type X is not applicable for the arguments (). The inferred type B is not a valid substitute for the bounded parameter <S extends A>\n" + 
					"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static <S extends A> S foo() { System.out.print(\"A\"); return null; }\n" +
				"	public static <N extends B> N foo() { System.out.print(\"B\"); return null; }\n" +
				"	public static void main(String[] args) {\n" +
				"		X.<A>foo();\n" +
				"		X.<B>foo();\n" +
				"		new X().<B>foo();\n" +
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B {}"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <N>foo() and <S>foo() have the same erasure
        public static <N extends B> N foo() { System.out.print("B"); return null; }
                                      ^
  where N,S are type-variables:
    N extends B declared in method <N>foo()
    S extends A declared in method <S>foo()
X.java:6: method foo in class X cannot be applied to given types
                X.<B>foo();
                 ^
  required: no arguments
  found: no arguments
X.java:7: method foo in class X cannot be applied to given types
                new X().<B>foo();
                       ^
  required: no arguments
  found: no arguments
3 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94754
	public void test050a() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X.java (at line 2)\n" + 
				"	public static <S extends A> void foo() { System.out.print(\"A\"); }\n" + 
				"	                                 ^^^^^\n" + 
				"Duplicate method foo() in type X\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	public static <N extends B> N foo() { System.out.print(\"B\"); return null; }\n" + 
				"	                              ^^^^^\n" + 
				"Duplicate method foo() in type X\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	X.foo();\n" + 
				"	  ^^^\n" + 
				"The method foo() is ambiguous for the type X\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	foo();\n" + 
				"	^^^\n" + 
				"The method foo() is ambiguous for the type X\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X.java (at line 2)\n" + 
					"	public static <S extends A> void foo() { System.out.print(\"A\"); }\n" + 
					"	                                 ^^^^^\n" + 
					"Duplicate method foo() in type X\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 3)\n" + 
					"	public static <N extends B> N foo() { System.out.print(\"B\"); return null; }\n" + 
					"	                              ^^^^^\n" + 
					"Duplicate method foo() in type X\n" + 
					"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static <S extends A> void foo() { System.out.print(\"A\"); }\n" +
				"	public static <N extends B> N foo() { System.out.print(\"B\"); return null; }\n" +
				"	static void test () {\n" +
				"		X.foo();\n" +
				"		foo();\n" +
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B {}"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <N>foo() and <S>foo() have the same erasure
        public static <N extends B> N foo() { System.out.print("B"); return null; }
                                      ^
  where N,S are type-variables:
    N extends B declared in method <N>foo()
    S extends A declared in method <S>foo()
1 error
 */
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423 - variation
	public void test050b() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
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
				"3. WARNING in X.java (at line 7)\n" + 
				"	<T extends Y> T foo(Object o) {  return null; } // duplicate\n" + 
				"	                ^^^^^^^^^^^^^\n" + 
				"Duplicate method foo(Object) in type X.C2\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 8)\n" + 
				"	<T extends Z> T foo(Object o) {  return null; } // duplicate\n" + 
				"	                ^^^^^^^^^^^^^\n" + 
				"Duplicate method foo(Object) in type X.C2\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 11)\n" + 
				"	A<Y> foo(Object o) {  return null; } // duplicate\n" + 
				"	     ^^^^^^^^^^^^^\n" + 
				"Duplicate method foo(Object) in type X.C3\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 12)\n" + 
				"	A<Z> foo(Object o) {  return null; } // duplicate\n" + 
				"	     ^^^^^^^^^^^^^\n" + 
				"Duplicate method foo(Object) in type X.C3\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 15)\n" + 
				"	Y foo(Object o) {  return null; } // duplicate\n" + 
				"	  ^^^^^^^^^^^^^\n" + 
				"Duplicate method foo(Object) in type X.C4\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 16)\n" + 
				"	<T extends Z> T foo(Object o) {  return null; } // duplicate\n" + 
				"	                ^^^^^^^^^^^^^\n" + 
				"Duplicate method foo(Object) in type X.C4\n" + 
				"----------\n":
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
					"3. ERROR in X.java (at line 7)\n" + 
					"	<T extends Y> T foo(Object o) {  return null; } // duplicate\n" + 
					"	                ^^^^^^^^^^^^^\n" + 
					"Duplicate method foo(Object) in type X.C2\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 8)\n" + 
					"	<T extends Z> T foo(Object o) {  return null; } // duplicate\n" + 
					"	                ^^^^^^^^^^^^^\n" + 
					"Duplicate method foo(Object) in type X.C2\n" + 
					"----------\n" + 
					"5. ERROR in X.java (at line 11)\n" + 
					"	A<Y> foo(Object o) {  return null; } // duplicate\n" + 
					"	     ^^^^^^^^^^^^^\n" + 
					"Duplicate method foo(Object) in type X.C3\n" + 
					"----------\n" + 
					"6. ERROR in X.java (at line 12)\n" + 
					"	A<Z> foo(Object o) {  return null; } // duplicate\n" + 
					"	     ^^^^^^^^^^^^^\n" + 
					"Duplicate method foo(Object) in type X.C3\n" + 
					"----------\n" + 
					"7. ERROR in X.java (at line 15)\n" + 
					"	Y foo(Object o) {  return null; } // duplicate\n" + 
					"	  ^^^^^^^^^^^^^\n" + 
					"Duplicate method foo(Object) in type X.C4\n" + 
					"----------\n" + 
					"8. ERROR in X.java (at line 16)\n" + 
					"	<T extends Z> T foo(Object o) {  return null; } // duplicate\n" + 
					"	                ^^^^^^^^^^^^^\n" + 
					"Duplicate method foo(Object) in type X.C4\n" + 
					"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	class C1 {\n" +
				"		Y foo(Object o) {  return null; } // duplicate\n" +
				"		Z foo(Object o) {  return null; } // duplicate\n" +
				"	}\n" +
				"	class C2 {\n" +
				"		<T extends Y> T foo(Object o) {  return null; } // duplicate\n" +
				"		<T extends Z> T foo(Object o) {  return null; } // duplicate\n" +
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
				"class A<T> {}\n" +
				"class Y {}\n" +
				"class Z {}"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:4: foo(Object) is already defined in X.C1
                Z foo(Object o) {  return null; } // duplicate
                  ^
X.java:8: name clash: <T#1>foo(Object) and <T#2>foo(Object) have the same erasure
                <T extends Z> T foo(Object o) {  return null; } // duplicate
                                ^
  where T#1,T#2 are type-variables:
    T#1 extends Z declared in method <T#1>foo(Object)
    T#2 extends Y declared in method <T#2>foo(Object)
X.java:12: foo(Object) is already defined in X.C3
                A<Z> foo(Object o) {  return null; } // duplicate
                     ^
X.java:16: foo(Object) is already defined in X.C4
                <T extends Z> T foo(Object o) {  return null; } // duplicate
                                ^
4 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423 - variation
	public void test050c() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
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
				"----------\n" + 
				"3. WARNING in X.java (at line 7)\n" + 
				"	<T extends Y> T foo(A<Y> o) {  return null; } // ok\n" + 
				"	                ^^^^^^^^^^^\n" + 
				"Method foo(A<Y>) has the same erasure foo(A<T>) as another method in type X.C6\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 8)\n" + 
				"	<T extends Z> T foo(A<Z> o) {  return null; } // ok\n" + 
				"	                ^^^^^^^^^^^\n" + 
				"Method foo(A<Z>) has the same erasure foo(A<T>) as another method in type X.C6\n" + 
				"----------\n":
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
					"----------\n" + 
					"3. ERROR in X.java (at line 7)\n" + 
					"	<T extends Y> T foo(A<Y> o) {  return null; } // ok\n" + 
					"	                ^^^^^^^^^^^\n" + 
					"Method foo(A<Y>) has the same erasure foo(A<T>) as another method in type X.C6\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 8)\n" + 
					"	<T extends Z> T foo(A<Z> o) {  return null; } // ok\n" + 
					"	                ^^^^^^^^^^^\n" + 
					"Method foo(A<Z>) has the same erasure foo(A<T>) as another method in type X.C6\n" + 
					"----------\n";
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
				"class A<T> {}\n" +
				"class Y {}\n" +
				"class Z {}"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:4: name clash: foo(A<Z>) and foo(A<Y>) have the same erasure
                A<Z> foo(A<Z> o) {  return null; } // duplicate
                     ^
X.java:8: name clash: <T#1>foo(A<Z>) and <T#2>foo(A<Y>) have the same erasure
                <T extends Z> T foo(A<Z> o) {  return null; } // ok
                                ^
  where T#1,T#2 are type-variables:
    T#1 extends Z declared in method <T#1>foo(A<Z>)
    T#2 extends Y declared in method <T#2>foo(A<Y>)
2 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423 - variation
	public void test050d() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X.java (at line 3)\n" + 
				"	<T extends Y, U> T foo(Object o) {  return null; } // ok\n" + 
				"	                   ^^^^^^^^^^^^^\n" + 
				"Duplicate method foo(Object) in type X.C7\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 4)\n" + 
				"	<T extends Z> T foo(Object o) {  return null; } // ok\n" + 
				"	                ^^^^^^^^^^^^^\n" + 
				"Duplicate method foo(Object) in type X.C7\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X.java (at line 3)\n" + 
					"	<T extends Y, U> T foo(Object o) {  return null; } // ok\n" + 
					"	                   ^^^^^^^^^^^^^\n" + 
					"Duplicate method foo(Object) in type X.C7\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 4)\n" + 
					"	<T extends Z> T foo(Object o) {  return null; } // ok\n" + 
					"	                ^^^^^^^^^^^^^\n" + 
					"Duplicate method foo(Object) in type X.C7\n" + 
					"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	class C7 {\n" +
				"		<T extends Y, U> T foo(Object o) {  return null; } // ok\n" +
				"		<T extends Z> T foo(Object o) {  return null; } // ok\n" +
				"	}\n" +
				"}\n" +
				"class A<T> {}\n" +
				"class Y {}\n" +
				"class Z {}"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:4: name clash: <T#1>foo(Object) and <T#2,U>foo(Object) have the same erasure
                <T extends Z> T foo(Object o) {  return null; } // ok
                                ^
  where T#1,T#2,U are type-variables:
    T#1 extends Z declared in method <T#1>foo(Object)
    T#2 extends Y declared in method <T#2,U>foo(Object)
    U extends Object declared in method <T#2,U>foo(Object)
1 error
 */
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050e() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X.java (at line 2)\n" + 
				"	<N extends B> N a(A<String> s) { return null; }\n" + 
				"	                ^^^^^^^^^^^^^^\n" + 
				"Method a(A<String>) has the same erasure a(A<T>) as another method in type X\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	<N> Object a(A<Number> n) { return null; }\n" + 
				"	           ^^^^^^^^^^^^^^\n" + 
				"Method a(A<Number>) has the same erasure a(A<T>) as another method in type X\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 4)\n" + 
				"	<N extends B> void b(A<String> s) {}\n" + 
				"	                   ^^^^^^^^^^^^^^\n" + 
				"Method b(A<String>) has the same erasure b(A<T>) as another method in type X\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 5)\n" + 
				"	<N extends B> B b(A<Number> n) { return null; }\n" + 
				"	                ^^^^^^^^^^^^^^\n" + 
				"Method b(A<Number>) has the same erasure b(A<T>) as another method in type X\n" + 
				"----------\n" + 
				"5. WARNING in X.java (at line 6)\n" + 
				"	void c(A<String> s) {}\n" + 
				"	     ^^^^^^^^^^^^^^\n" + 
				"Method c(A<String>) has the same erasure c(A<T>) as another method in type X\n" + 
				"----------\n" + 
				"6. WARNING in X.java (at line 7)\n" + 
				"	B c(A<Number> n) { return null; }\n" + 
				"	  ^^^^^^^^^^^^^^\n" + 
				"Method c(A<Number>) has the same erasure c(A<T>) as another method in type X\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X.java (at line 2)\n" + 
					"	<N extends B> N a(A<String> s) { return null; }\n" + 
					"	                ^^^^^^^^^^^^^^\n" + 
					"Method a(A<String>) has the same erasure a(A<T>) as another method in type X\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 3)\n" + 
					"	<N> Object a(A<Number> n) { return null; }\n" + 
					"	           ^^^^^^^^^^^^^^\n" + 
					"Method a(A<Number>) has the same erasure a(A<T>) as another method in type X\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 4)\n" + 
					"	<N extends B> void b(A<String> s) {}\n" + 
					"	                   ^^^^^^^^^^^^^^\n" + 
					"Method b(A<String>) has the same erasure b(A<T>) as another method in type X\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 5)\n" + 
					"	<N extends B> B b(A<Number> n) { return null; }\n" + 
					"	                ^^^^^^^^^^^^^^\n" + 
					"Method b(A<Number>) has the same erasure b(A<T>) as another method in type X\n" + 
					"----------\n" + 
					"5. ERROR in X.java (at line 6)\n" + 
					"	void c(A<String> s) {}\n" + 
					"	     ^^^^^^^^^^^^^^\n" + 
					"Method c(A<String>) has the same erasure c(A<T>) as another method in type X\n" + 
					"----------\n" + 
					"6. ERROR in X.java (at line 7)\n" + 
					"	B c(A<Number> n) { return null; }\n" + 
					"	  ^^^^^^^^^^^^^^\n" + 
					"Method c(A<Number>) has the same erasure c(A<T>) as another method in type X\n" + 
					"----------\n";
		this.runNegativeTest(
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
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <N#1>a(A<Number>) and <N#2>a(A<String>) have the same erasure
        <N> Object a(A<Number> n) { return null; }
                   ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>a(A<Number>)
    N#2 extends B declared in method <N#2>a(A<String>)
X.java:5: name clash: <N#1>b(A<Number>) and <N#2>b(A<String>) have the same erasure
        <N extends B> B b(A<Number> n) { return null; }
                        ^
  where N#1,N#2 are type-variables:
    N#1 extends B declared in method <N#1>b(A<Number>)
    N#2 extends B declared in method <N#2>b(A<String>)
X.java:7: name clash: c(A<Number>) and c(A<String>) have the same erasure
        B c(A<Number> n) { return null; }
          ^
3 errors
 */
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
		);
/* javac 7
X.java:3: name clash: <N#1>a(A<Number>) and <N#2>a(A<String>) have the same erasure
        <N> B a(A<Number> n) { return null; }
              ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>a(A<Number>)
    N#2 extends B declared in method <N#2>a(A<String>)
1 error
 */
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
		);
/* javac 7
X.java:3: name clash: <N#1>b(A<Number>) and <N#2>b(A<String>) have the same erasure
        <N extends B> B b(A<Number> n) { return null; }
                        ^
  where N#1,N#2 are type-variables:
    N#1 extends B declared in method <N#1>b(A<Number>)
    N#2 extends B declared in method <N#2>b(A<String>)
1 error
 */
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
		);
/* javac 7
X.java:3: name clash: c(A<Number>) and c(A<String>) have the same erasure
        B c(A<Number> n) { return null; }
          ^
1 error
 */
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=90423
	public void test050i() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X.java (at line 2)\n" + 
				"	<N extends B> N a(A<Number> s) { return null; }\n" + 
				"	                ^^^^^^^^^^^^^^\n" + 
				"Duplicate method a(A<Number>) in type X\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	<N> Object a(A<Number> n) { return null; }\n" + 
				"	           ^^^^^^^^^^^^^^\n" + 
				"Duplicate method a(A<Number>) in type X\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 4)\n" + 
				"	<N extends B> N b(A<Number> s) { return null; }\n" + 
				"	                ^^^^^^^^^^^^^^\n" + 
				"Method b(A<Number>) has the same erasure b(A<T>) as another method in type X\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 5)\n" + 
				"	<N> Object b(A<String> n) { return null; }\n" + 
				"	           ^^^^^^^^^^^^^^\n" + 
				"Method b(A<String>) has the same erasure b(A<T>) as another method in type X\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X.java (at line 2)\n" + 
					"	<N extends B> N a(A<Number> s) { return null; }\n" + 
					"	                ^^^^^^^^^^^^^^\n" + 
					"Duplicate method a(A<Number>) in type X\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 3)\n" + 
					"	<N> Object a(A<Number> n) { return null; }\n" + 
					"	           ^^^^^^^^^^^^^^\n" + 
					"Duplicate method a(A<Number>) in type X\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 4)\n" + 
					"	<N extends B> N b(A<Number> s) { return null; }\n" + 
					"	                ^^^^^^^^^^^^^^\n" + 
					"Method b(A<Number>) has the same erasure b(A<T>) as another method in type X\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 5)\n" + 
					"	<N> Object b(A<String> n) { return null; }\n" + 
					"	           ^^^^^^^^^^^^^^\n" + 
					"Method b(A<String>) has the same erasure b(A<T>) as another method in type X\n" + 
					"----------\n";
		this.runNegativeTest(
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
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <N#1>a(A<Number>) and <N#2>a(A<Number>) have the same erasure
        <N> Object a(A<Number> n) { return null; }
                   ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>a(A<Number>)
    N#2 extends B declared in method <N#2>a(A<Number>)
X.java:5: name clash: <N#1>b(A<String>) and <N#2>b(A<Number>) have the same erasure
        <N> Object b(A<String> n) { return null; }
                   ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>b(A<String>)
    N#2 extends B declared in method <N#2>b(A<Number>)
2 errors
 */
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
		);
/* javac 7
X.java:3: name clash: <N#1>a(A<Number>) and <N#2>a(A<Number>) have the same erasure
        <N> B a(A<Number> n) { return null; }
              ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>a(A<Number>)
    N#2 extends B declared in method <N#2>a(A<Number>)
X.java:5: name clash: <N#1>b(A<String>) and <N#2>b(A<Number>) have the same erasure
        <N> B b(A<String> n) { return null; }
              ^
  where N#1,N#2 are type-variables:
    N#1 extends Object declared in method <N#1>b(A<String>)
    N#2 extends B declared in method <N#2>b(A<Number>)
2 errors
 */
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
		);
/* javac 7
X.java:3: <N>a(A<Number>) is already defined in X
                <N extends B> B a(A<Number> n) { return null; }
                                ^
  where N is a type-variable:
    N extends B declared in method <N>a(A<Number>)
X.java:5: <N>b(A<Number>) is already defined in X
                <N extends B> B b(A<Number> n) { return null; }
                                ^
  where N is a type-variable:
    N extends B declared in method <N>b(A<Number>)
2 errors
 */
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
			"1. ERROR in X.java (at line 2)\n" + 
			"	void a(A<Number> s) {}\n" + 
			"	     ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	B a(A<Number> n) { return null; }\n" + 
			"	  ^^^^^^^^^^^^^^\n" + 
			"Duplicate method a(A<Number>) in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\n" + 
			"	Object b(A<Number> s) {}\n" + 
			"	       ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 4)\n" + 
			"	Object b(A<Number> s) {}\n" + 
			"	       ^^^^^^^^^^^^^^\n" + 
			"This method must return a result of type Object\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 5)\n" + 
			"	B b(A<Number> n) { return null; }\n" + 
			"	  ^^^^^^^^^^^^^^\n" + 
			"Duplicate method b(A<Number>) in type X\n" + 
			"----------\n"
		);
/* javac 7
X.java:3: a(A<Number>) is already defined in X
                 B a(A<Number> n) { return null; }
                   ^
X.java:5: b(A<Number>) is already defined in X
                 B b(A<Number> n) { return null; }
                   ^
2 errors
 */
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
		);
/* javac 7
X.java:1: X is not abstract and does not override abstract method <T>foo(T) in I
class X implements I {
^
  where T is a type-variable:
    T extends Object declared in method <T>foo(T)
1 error
 */
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
		);
/* javac 7
X.java:3: name clash: foo(A<Integer>) and foo(A<String>) have the same erasure
        void foo(A<Integer> a) {}
             ^
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89470
	public void test051b() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X.java (at line 2)\n" + 
				"	void foo(A<String> a) {}\n" + 
				"	     ^^^^^^^^^^^^^^^^\n" + 
				"Method foo(A<String>) has the same erasure foo(A<T>) as another method in type X\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	Object foo(A<Integer> a) { return null; }\n" + 
				"	       ^^^^^^^^^^^^^^^^^\n" + 
				"Method foo(A<Integer>) has the same erasure foo(A<T>) as another method in type X\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X.java (at line 2)\n" + 
					"	void foo(A<String> a) {}\n" + 
					"	     ^^^^^^^^^^^^^^^^\n" + 
					"Method foo(A<String>) has the same erasure foo(A<T>) as another method in type X\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 3)\n" + 
					"	Object foo(A<Integer> a) { return null; }\n" + 
					"	       ^^^^^^^^^^^^^^^^^\n" + 
					"Method foo(A<Integer>) has the same erasure foo(A<T>) as another method in type X\n" + 
					"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo(A<String> a) {}\n" +
				"	Object foo(A<Integer> a) { return null; }\n" +
				"}\n" +
				"class A<T> {}\n",
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: foo(A<Integer>) and foo(A<String>) have the same erasure
        Object foo(A<Integer> a) { return null; }
               ^
1 error
 */
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
		);
/* javac 7
X.java:3: a(Object) is already defined in X
        <T> T a(T x) {  return null; }
              ^
1 error
 */
	}
	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054a() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X.java (at line 2)\n" + 
				"	<T1, T2> String aaa(X x) {  return null; }\n" + 
				"	                ^^^^^^^^\n" + 
				"Method aaa(X) has the same erasure aaa(X) as another method in type X\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	<T extends X> T aaa(T x) {  return null; }\n" + 
				"	                ^^^^^^^^\n" + 
				"Method aaa(T) has the same erasure aaa(X) as another method in type X\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 4)\n" + 
				"	<T> String aa(X x) {  return null; }\n" + 
				"	           ^^^^^^^\n" + 
				"Method aa(X) has the same erasure aa(X) as another method in type X\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 5)\n" + 
				"	<T extends X> T aa(T x) {  return null; }\n" + 
				"	                ^^^^^^^\n" + 
				"Method aa(T) has the same erasure aa(X) as another method in type X\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 6)\n" + 
				"	String a(X x) {  return null; }\n" + 
				"	       ^^^^^^\n" + 
				"Method a(X) has the same erasure a(X) as another method in type X\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 7)\n" + 
				"	<T extends X> T a(T x) {  return null; }\n" + 
				"	                ^^^^^^\n" + 
				"Method a(T) has the same erasure a(X) as another method in type X\n" + 
				"----------\n" + 
				"7. WARNING in X.java (at line 8)\n" + 
				"	<T> String z(X x) { return null; }\n" + 
				"	           ^^^^^^\n" + 
				"Duplicate method z(X) in type X\n" + 
				"----------\n" + 
				"8. WARNING in X.java (at line 9)\n" + 
				"	<T, S> Object z(X x) { return null; }\n" + 
				"	              ^^^^^^\n" + 
				"Duplicate method z(X) in type X\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X.java (at line 2)\n" + 
					"	<T1, T2> String aaa(X x) {  return null; }\n" + 
					"	                ^^^^^^^^\n" + 
					"Method aaa(X) has the same erasure aaa(X) as another method in type X\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 3)\n" + 
					"	<T extends X> T aaa(T x) {  return null; }\n" + 
					"	                ^^^^^^^^\n" + 
					"Method aaa(T) has the same erasure aaa(X) as another method in type X\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 4)\n" + 
					"	<T> String aa(X x) {  return null; }\n" + 
					"	           ^^^^^^^\n" + 
					"Method aa(X) has the same erasure aa(X) as another method in type X\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 5)\n" + 
					"	<T extends X> T aa(T x) {  return null; }\n" + 
					"	                ^^^^^^^\n" + 
					"Method aa(T) has the same erasure aa(X) as another method in type X\n" + 
					"----------\n" + 
					"5. ERROR in X.java (at line 6)\n" + 
					"	String a(X x) {  return null; }\n" + 
					"	       ^^^^^^\n" + 
					"Method a(X) has the same erasure a(X) as another method in type X\n" + 
					"----------\n" + 
					"6. ERROR in X.java (at line 7)\n" + 
					"	<T extends X> T a(T x) {  return null; }\n" + 
					"	                ^^^^^^\n" + 
					"Method a(T) has the same erasure a(X) as another method in type X\n" + 
					"----------\n" + 
					"7. ERROR in X.java (at line 8)\n" + 
					"	<T> String z(X x) { return null; }\n" + 
					"	           ^^^^^^\n" + 
					"Duplicate method z(X) in type X\n" + 
					"----------\n" + 
					"8. ERROR in X.java (at line 9)\n" + 
					"	<T, S> Object z(X x) { return null; }\n" + 
					"	              ^^^^^^\n" + 
					"Duplicate method z(X) in type X\n" + 
					"----------\n";
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
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <T>aaa(T) and <T1,T2>aaa(X) have the same erasure
        <T extends X> T aaa(T x) {  return null; }
                        ^
  where T,T1,T2 are type-variables:
    T extends X declared in method <T>aaa(T)
    T1 extends Object declared in method <T1,T2>aaa(X)
    T2 extends Object declared in method <T1,T2>aaa(X)
X.java:5: name clash: <T#1>aa(T#1) and <T#2>aa(X) have the same erasure
        <T extends X> T aa(T x) {  return null; }
                        ^
  where T#1,T#2 are type-variables:
    T#1 extends X declared in method <T#1>aa(T#1)
    T#2 extends Object declared in method <T#2>aa(X)
X.java:7: a(X) is already defined in X
        <T extends X> T a(T x) {  return null; }
                        ^
X.java:9: name clash: <T#1,S>z(X) and <T#3>z(X) have the same erasure
        <T, S> Object z(X x) { return null; }
                      ^
  where T#1,S,T#3 are type-variables:
    T#1 extends Object declared in method <T#1,S>z(X)
    S extends Object declared in method <T#1,S>z(X)
    T#3 extends Object declared in method <T#3>z(X)
4 errors
 */
	}
	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054b() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X.java (at line 2)\n" + 
				"	Object foo(X<T> t) { return null; }\n" + 
				"	       ^^^^^^^^^^^\n" + 
				"Duplicate method foo(X<T>) in type X<T>\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	<S> String foo(X<T> s) { return null; }\n" + 
				"	           ^^^^^^^^^^^\n" + 
				"Duplicate method foo(X<T>) in type X<T>\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X.java (at line 2)\n" + 
					"	Object foo(X<T> t) { return null; }\n" + 
					"	       ^^^^^^^^^^^\n" + 
					"Duplicate method foo(X<T>) in type X<T>\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 3)\n" + 
					"	<S> String foo(X<T> s) { return null; }\n" + 
					"	           ^^^^^^^^^^^\n" + 
					"Duplicate method foo(X<T>) in type X<T>\n" + 
					"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"		 Object foo(X<T> t) { return null; }\n" +
				"		 <S> String foo(X<T> s) { return null; }\n" +
				"}\n"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <S>foo(X<T>) and foo(X<T>) have the same erasure
        <S> String foo(X<T> s) { return null; }
                   ^
  where S,T are type-variables:
    S extends Object declared in method <S>foo(X<T>)
    T extends Object declared in class X
1 error
 */
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
			"1. ERROR in X.java (at line 2)\n" +
			"	<T1 extends X<T1>> void dupT() {}\n" +
			"	                        ^^^^^^\n" +
			"Duplicate method dupT() in type X<T>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	<T2 extends X<T2>> Object dupT() {return null;}\n" +
			"	                          ^^^^^^\n" +
			"Duplicate method dupT() in type X<T>\n" +
			"----------\n"
		);
/* javac 7
X.java:3: <T1>dupT() is already defined in X
        <T2 extends X<T2>> Object dupT() {return null;}
                                  ^
  where T1 is a type-variable:
    T1 extends X<T1> declared in method <T1>dupT()
1 error
 */
	}
	// more duplicate tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=94897
	public void test054d() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. WARNING in X.java (at line 2)\n" + 
				"	<T> T a(A<T> t) {return null;}\n" + 
				"	      ^^^^^^^^^\n" + 
				"Method a(A<T>) has the same erasure a(A<T>) as another method in type X\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	<T> String a(A<Object> o) {return null;}\n" + 
				"	           ^^^^^^^^^^^^^^\n" + 
				"Method a(A<Object>) has the same erasure a(A<T>) as another method in type X\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 4)\n" + 
				"	<T> T aa(A<T> t) {return null;}\n" + 
				"	      ^^^^^^^^^^\n" + 
				"Method aa(A<T>) has the same erasure aa(A<T>) as another method in type X\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 5)\n" + 
				"	String aa(A<Object> o) {return null;}\n" + 
				"	       ^^^^^^^^^^^^^^^\n" + 
				"Method aa(A<Object>) has the same erasure aa(A<T>) as another method in type X\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X.java (at line 2)\n" + 
					"	<T> T a(A<T> t) {return null;}\n" + 
					"	      ^^^^^^^^^\n" + 
					"Method a(A<T>) has the same erasure a(A<T>) as another method in type X\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 3)\n" + 
					"	<T> String a(A<Object> o) {return null;}\n" + 
					"	           ^^^^^^^^^^^^^^\n" + 
					"Method a(A<Object>) has the same erasure a(A<T>) as another method in type X\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 4)\n" + 
					"	<T> T aa(A<T> t) {return null;}\n" + 
					"	      ^^^^^^^^^^\n" + 
					"Method aa(A<T>) has the same erasure aa(A<T>) as another method in type X\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 5)\n" + 
					"	String aa(A<Object> o) {return null;}\n" + 
					"	       ^^^^^^^^^^^^^^^\n" + 
					"Method aa(A<Object>) has the same erasure aa(A<T>) as another method in type X\n" + 
					"----------\n";
		this.runNegativeTest(
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
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <T#1>a(A<Object>) and <T#2>a(A<T#2>) have the same erasure

        <T> String a(A<Object> o) {return null;}
                   ^
  where T#1,T#2 are type-variables:
    T#1 extends Object declared in method <T#1>a(A<Object>)
    T#2 extends Object declared in method <T#2>a(A<T#2>)
X.java:5: name clash: aa(A<Object>) and <T>aa(A<T>) have the same erasure
        String aa(A<Object> o) {return null;}
               ^
  where T is a type-variable:
    T extends Object declared in method <T>aa(A<T>)
2 errors
 */
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
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	new X<Object>().foo(\"X\");\n" + 
				"	                ^^^\n" + 
				"The method foo(String) is ambiguous for the type X<Object>\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	new X<Object>().foo2(\"X\");\n" + 
				"	                ^^^^\n" + 
				"The method foo2(String) is ambiguous for the type X<Object>\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 6)\n" + 
				"	<T> T foo(T t) {return null;}\n" + 
				"	      ^^^^^^^^\n" + 
				"Method foo(T) has the same erasure foo(Object) as another method in type X<A>\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 7)\n" + 
				"	void foo(A a) {}\n" + 
				"	     ^^^^^^^^\n" + 
				"Method foo(A) has the same erasure foo(Object) as another method in type X<A>\n" + 
				"----------\n" + 
				"5. WARNING in X.java (at line 8)\n" + 
				"	<T> T foo2(T t) {return null;}\n" + 
				"	      ^^^^^^^^^\n" + 
				"Method foo2(T) has the same erasure foo2(Object) as another method in type X<A>\n" + 
				"----------\n" + 
				"6. WARNING in X.java (at line 9)\n" + 
				"	<T> void foo2(A a) {}\n" + 
				"	         ^^^^^^^^^\n" + 
				"Method foo2(A) has the same erasure foo2(Object) as another method in type X<A>\n" + 
				"----------\n":
					"----------\n" + 
					"1. ERROR in X.java (at line 6)\n" + 
					"	<T> T foo(T t) {return null;}\n" + 
					"	      ^^^^^^^^\n" + 
					"Method foo(T) has the same erasure foo(Object) as another method in type X<A>\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 7)\n" + 
					"	void foo(A a) {}\n" + 
					"	     ^^^^^^^^\n" + 
					"Method foo(A) has the same erasure foo(Object) as another method in type X<A>\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 8)\n" + 
					"	<T> T foo2(T t) {return null;}\n" + 
					"	      ^^^^^^^^^\n" + 
					"Method foo2(T) has the same erasure foo2(Object) as another method in type X<A>\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 9)\n" + 
					"	<T> void foo2(A a) {}\n" + 
					"	         ^^^^^^^^^\n" + 
					"Method foo2(A) has the same erasure foo2(Object) as another method in type X<A>\n" + 
					"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A> {\n" +
				"	void test() {\n" +
				"		new X<Object>().foo(\"X\");\n" +
				"		new X<Object>().foo2(\"X\");\n" +
				"	}\n" +
				"	<T> T foo(T t) {return null;}\n" +
				"	void foo(A a) {}\n" +
				"	<T> T foo2(T t) {return null;}\n" +
				"	<T> void foo2(A a) {}\n" +
				"}\n"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:7: name clash: foo(A) and <T>foo(T) have the same erasure
        void foo(A a) {}
             ^
  where A,T are type-variables:
    A extends Object declared in class X
    T extends Object declared in method <T>foo(T)
X.java:9: name clash: <T#1>foo2(A) and <T#3>foo2(T#3) have the same erasure
        <T> void foo2(A a) {}
                 ^
  where T#1,A,T#3 are type-variables:
    T#1 extends Object declared in method <T#1>foo2(A)
    A extends Object declared in class X
    T#3 extends Object declared in method <T#3>foo2(T#3)
2 errors
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94898
	public void test058b() {
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	new X<Object>().foo(\"X\");\n" + 
				"	                ^^^\n" + 
				"The method foo(String) is ambiguous for the type X<Object>\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	new X<Object>().foo2(\"X\");\n" + 
				"	                ^^^^\n" + 
				"The method foo2(String) is ambiguous for the type X<Object>\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 6)\n" + 
				"	<T> T foo(T t) {return null;}\n" + 
				"	      ^^^^^^^^\n" + 
				"Name clash: The method foo(T) of type X<A> has the same erasure as foo(A) of type Y<A> but does not override it\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 7)\n" + 
				"	<T> T foo2(T t) {return null;}\n" + 
				"	      ^^^^^^^^^\n" + 
				"Name clash: The method foo2(T) of type X<A> has the same erasure as foo2(A) of type Y<A> but does not override it\n" + 
				"----------\n":
					"----------\n" +
					"1. ERROR in X.java (at line 3)\n" + 
					"	new X<Object>().foo(\"X\");\n" + 
					"	                ^^^\n" + 
					"The method foo(String) is ambiguous for the type X<Object>\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 4)\n" + 
					"	new X<Object>().foo2(\"X\");\n" + 
					"	                ^^^^\n" + 
					"The method foo2(String) is ambiguous for the type X<Object>\n" + 
					"----------\n" + 
					"3. ERROR in X.java (at line 6)\n" + 
					"	<T> T foo(T t) {return null;}\n" + 
					"	      ^^^^^^^^\n" + 
					"Name clash: The method foo(T) of type X<A> has the same erasure as foo(A) of type Y<A> but does not override it\n" + 
					"----------\n" + 
					"4. ERROR in X.java (at line 7)\n" + 
					"	<T> T foo2(T t) {return null;}\n" + 
					"	      ^^^^^^^^^\n" + 
					"Name clash: The method foo2(T) of type X<A> has the same erasure as foo2(A) of type Y<A> but does not override it\n" + 
					"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A> extends Y<A> {\n" +
				"	void test() {\n" +
				"		new X<Object>().foo(\"X\");\n" +
				"		new X<Object>().foo2(\"X\");\n" +
				"	}\n" +
				"	<T> T foo(T t) {return null;}\n" +
				"	<T> T foo2(T t) {return null;}\n" +
				"}\n" +
				"class Y<A> {\n" +
				"	void foo(A a) {}\n" +
				"	<T> void foo2(A a) {}\n" +
				"}"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: reference to foo is ambiguous, both method foo(A) in Y and method <T>foo(T) in X match
                new X<Object>().foo("X");
                               ^
  where A,T are type-variables:
    A extends Object declared in class Y
    T extends Object declared in method <T>foo(T)
X.java:4: reference to foo2 is ambiguous, both method <T#1>foo2(A) in Y and method <T#3>foo2(T#3) in X match
                new X<Object>().foo2("X");
                               ^
  where T#1,A,T#3 are type-variables:
    T#1 extends Object declared in method <T#1>foo2(A)
    A extends Object declared in class Y
    T#3 extends Object declared in method <T#3>foo2(T#3)
X.java:6: name clash: <T>foo(T) in X and foo(A) in Y have the same erasure, yet neither overrides the other
        <T> T foo(T t) {return null;}
              ^
  where T,A are type-variables:
    T extends Object declared in method <T>foo(T)
    A extends Object declared in class Y
X.java:7: name clash: <T#1>foo2(T#1) in X and <T#2>foo2(A) in Y have the same erasure, yet neither overrides the other
        <T> T foo2(T t) {return null;}
              ^
  where T#1,T#2,A are type-variables:
    T#1 extends Object declared in method <T#1>foo2(T#1)
    T#2 extends Object declared in method <T#2>foo2(A)
    A extends Object declared in class Y
4 errors
 */
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
			"1. ERROR in X.java (at line 2)\n" +
			"	public class X implements I<A> {\n" +
			"	             ^\n" +
			"The type X must implement the inherited abstract method I<A>.x3()\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	public class X implements I<A> {\n" +
			"	             ^\n" +
			"The type X must implement the inherited abstract method I<A>.x2()\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	public <N extends Number> void x2() {}\n" +
			"	                               ^^^^\n" +
			"Name clash: The method x2() of type X has the same erasure as x2() of type I<V> but does not override it\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	public <N extends Number> void x3() {}\n" +
			"	                               ^^^^\n" +
			"Name clash: The method x3() of type X has the same erasure as x3() of type I<V> but does not override it\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 9)\n" +
			"	<N extends String> void x2();\n" +
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
			"1. ERROR in D.java (at line 2)\n" +
			"	@Override void m(Number t) {}\n" +
			"	               ^^^^^^^^^^^\n" +
			"Name clash: The method m(Number) of type D has the same erasure as m(T) of type A<T> but does not override it\n" +
			"----------\n" +
			"2. ERROR in D.java (at line 2)\n" +
			"	@Override void m(Number t) {}\n" +
			"	               ^^^^^^^^^^^\n" +
			mustOverrideMessage("m(Number)", "D") +
			"----------\n" +
			"3. WARNING in D.java (at line 6)\n" +
			"	class B<S extends Integer> extends A<S> { @Override void m(S t) {} }\n" +
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
			"1. ERROR in NumericArray5.java (at line 5)\n" +
			"	@Override public void add(Number n, Integer i) {}\n" +
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
			"The return types are incompatible for the inherited methods I.finalize(), Object.finalize()\n" +
			"----------\n" +
			"4. ERROR in A.java (at line 6)\n" +
			"	abstract class A implements J {}\n" +
			"	               ^\n" +
			"The return types are incompatible for the inherited methods I.hashCode(), Object.hashCode()\n" +
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
			"1. ERROR in A.java (at line 2)\n" +
			"	class B<V> extends A<V> { @Override <T1, S1 extends K & I<T1>> void foo(V v, T1 t, S1 s) { } }\n" +
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
			"1. ERROR in A.java (at line 2)\n" +
			"	<T, S extends J & I<T>> void foo() { }\n" +
			"	                             ^^^^^\n" +
			"Duplicate method foo() in type A\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 3)\n" +
			"	<T, S extends I<T> & J> void foo() { }\n" +
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
			"1. ERROR in A.java (at line 2)\n" +
			"	<T, S extends J & I<T>> void foo() { }\n" +
			"	                             ^^^^^\n" +
			"Duplicate method foo() in type A\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 3)\n" +
			"	<T, S extends I<T> & K> void foo() { }\n" +
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
			"1. ERROR in I.java (at line 3)\n" +
			"	interface K extends I, J { K foo(); }\n" +
			"	          ^\n" +
			"The return type is incompatible with J.foo(), I.foo()\n" +
			"----------\n" +
			"2. ERROR in I.java (at line 7)\n" +
			"	interface O extends L, M, N { K getI(); }\n" +
			"	          ^\n" +
			"The return type is incompatible with N.getI(), M.getI(), L.getI()\n" +
			"----------\n" +
			"3. ERROR in I.java (at line 8)\n" +
			"	interface P extends L, M, N {}\n" +
			"	          ^\n" +
			"The return type is incompatible with N.getI(), M.getI(), L.getI()\n" +
			"----------\n" +
			"4. ERROR in I.java (at line 10)\n" +
			"	abstract class Y implements L, M, N {}\n" +
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
			"1. WARNING in A.java (at line 2)\n" +
			"	class B extends A { void x(A<String> s) {} }\n" +
			"	                ^\n" +
			"A is a raw type. References to generic type A<E> should be parameterized\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 2)\n" +
			"	class B extends A { void x(A<String> s) {} }\n" +
			"	                         ^^^^^^^^^^^^^^\n" +
			"Name clash: The method x(A<String>) of type B has the same erasure as x(A) of type A but does not override it\n" +
			"----------\n" +
			"3. WARNING in A.java (at line 3)\n" +
			"	class C extends A { @Override void x(A s) {} }\n" +
			"	                ^\n" +
			"A is a raw type. References to generic type A<E> should be parameterized\n" +
			"----------\n" +
			"4. WARNING in A.java (at line 3)\n" +
			"	class C extends A { @Override void x(A s) {} }\n" +
			"	                                     ^\n" +
			"A is a raw type. References to generic type A<E> should be parameterized\n" +
			"----------\n" +
			"5. WARNING in A.java (at line 4)\n" +
			"	class D extends A { void x(A<Object> s) {} }\n" +
			"	                ^\n" +
			"A is a raw type. References to generic type A<E> should be parameterized\n" +
			"----------\n" +
			"6. ERROR in A.java (at line 4)\n" +
			"	class D extends A { void x(A<Object> s) {} }\n" +
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
			"1. WARNING in X.java (at line 2)\n" +
			"	public A<String> foo() { return null; }\n" +
			"	       ^\n" +
			"Type safety: The return type A<String> for foo() from the type X<U> needs unchecked conversion to conform to A<Object> from the type I\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	public <S> A<U> bar() { return null; }\n" +
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
			"1. ERROR in A.java (at line 1)\n" +
			"	public class A<T1 extends A.M> implements I<T1> {\n" +
			"	             ^\n" +
			"The type A<T1> must implement the inherited abstract method I<T1>.foo4(U, A.M)\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 2)\n" +
			"	public void foo4(Number n, T1 m) {}\n" +
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
			this.complianceLevel < ClassFileConstants.JDK1_7 ?
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
			"----------\n":
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
				"3. ERROR in Parent.java (at line 9)\n" + 
				"	static void staticCase1(Collection<String> c) {}\n" + 
				"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method staticCase1(Collection<String>) of type Child has the same erasure as staticCase1(Collection) of type Parent but does not hide it\n" + 
				"----------\n" + 
				"4. WARNING in Parent.java (at line 10)\n" + 
				"	static void staticCase2(Collection c) {}\n" + 
				"	                        ^^^^^^^^^^\n" + 
				"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
				"----------\n" + 
				"5. ERROR in Parent.java (at line 11)\n" + 
				"	void instanceCase1(Collection<String> c) {}\n" + 
				"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method instanceCase1(Collection<String>) of type Child has the same erasure as instanceCase1(Collection) of type Parent but does not override it\n" + 
				"----------\n" + 
				"6. WARNING in Parent.java (at line 12)\n" + 
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
			"1. WARNING in Parent.java (at line 3)\n" +
			"	static void staticMismatchCase1(Collection c) {}\n" +
			"	                                ^^^^^^^^^^\n" +
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" +
			"----------\n" +
			"2. WARNING in Parent.java (at line 5)\n" +
			"	void mismatchCase1(Collection c) {}\n" +
			"	                   ^^^^^^^^^^\n" +
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" +
			"----------\n" +
			"3. ERROR in Parent.java (at line 9)\n" +
			"	void staticMismatchCase1(Collection c) {}\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"This instance method cannot override the static method from Parent\n" +
			"----------\n" +
			"4. WARNING in Parent.java (at line 9)\n" +
			"	void staticMismatchCase1(Collection c) {}\n" +
			"	                         ^^^^^^^^^^\n" +
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" +
			"----------\n" +
			"5. ERROR in Parent.java (at line 10)\n" +
			"	void staticMismatchCase2(Collection<String> c) {}\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"This instance method cannot override the static method from Parent\n" +
			"----------\n" +
			"6. ERROR in Parent.java (at line 11)\n" +
			"	static void mismatchCase1(Collection c) {}\n" +
			"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"This static method cannot hide the instance method from Parent\n" +
			"----------\n" +
			"7. WARNING in Parent.java (at line 11)\n" +
			"	static void mismatchCase1(Collection c) {}\n" +
			"	                          ^^^^^^^^^^\n" +
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" +
			"----------\n" +
			"8. ERROR in Parent.java (at line 12)\n" +
			"	static void mismatchCase2(Collection<String> c) {}\n" +
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
			"2. ERROR in X.java (at line 3)\n" +
			"	abstract class X2 extends CX implements IX {}\n" +
			"	               ^^\n" +
			"The static method foo(Object) conflicts with the abstract method in IX\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 3)\n" +
			"	abstract class X2 extends CX implements IX {}\n" +
			"	                          ^^\n" +
			"CX is a raw type. References to generic type CX<T> should be parameterized\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 3)\n" +
			"	abstract class X2 extends CX implements IX {}\n" +
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
			"1. WARNING in X.java (at line 11)\n" +
			"	Collection<Interface2> doStuff();\n" +
			"	^^^^^^^^^^\n" +
			"Type safety: The return type Collection<Interface2> for doStuff() from the type Interface4 needs unchecked conversion to conform to Collection<Interface1> from the type Interface3\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 14)\n" +
			"	Zork z;\n" +
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
			"    0  aload_0 [this]\n" +
			"    1  invokevirtual X.bar() : java.lang.String [21]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"  \n" +
			"  // Method descriptor #17 ()Ljava/lang/Object;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  protected bridge synthetic java.lang.Object foo() throws java.lang.Exception;\n" +
			"    0  aload_0 [this]\n" +
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
			"6. ERROR in DataSet.java (at line 6)\n" +
			"	public <S> S[] toArray(S[] s) {\n" +
			"	               ^^^^^^^^^^^^^^\n" +
			"Name clash: The method toArray(S[]) of type DataSet<T> has the same erasure as toArray(Object[]) of type List but does not override it\n" +
			"----------\n" +
			"7. ERROR in DataSet.java (at line 6)\n" +
			"	public <S> S[] toArray(S[] s) {\n" +
			"	               ^^^^^^^^^^^^^^\n" +
			"Name clash: The method toArray(S[]) of type DataSet<T> has the same erasure as toArray(Object[]) of type Collection but does not override it\n" +
			"----------\n" +
			"8. ERROR in DataSet.java (at line 9)\n" +
			"	public Object[] toArray(Object[] o) {\n" +
			"	                ^^^^^^^^^^^^^^^^^^^\n" +
			"Method toArray(Object[]) has the same erasure toArray(Object[]) as another method in type DataSet<T>\n" +
			"----------\n" +
			"9. WARNING in DataSet.java (at line 14)\n" +
			"	public boolean addAll(Collection c) {	return false; }\n" +
			"	                      ^^^^^^^^^^\n" +
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" +
			"----------\n" +
			"10. WARNING in DataSet.java (at line 15)\n" +
			"	public boolean addAll(int index, Collection c) {	return false; }\n" +
			"	                                 ^^^^^^^^^^\n" +
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" +
			"----------\n" +
			"11. WARNING in DataSet.java (at line 18)\n" +
			"	public boolean containsAll(Collection c) { return false; }\n" +
			"	                           ^^^^^^^^^^\n" +
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" +
			"----------\n" +
			"12. WARNING in DataSet.java (at line 22)\n" +
			"	public Iterator iterator() {	return null; }\n" +
			"	       ^^^^^^^^\n" +
			"Iterator is a raw type. References to generic type Iterator<E> should be parameterized\n" +
			"----------\n" +
			"13. WARNING in DataSet.java (at line 24)\n" +
			"	public ListIterator listIterator() {	return null; }\n" +
			"	       ^^^^^^^^^^^^\n" +
			"ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized\n" +
			"----------\n" +
			"14. WARNING in DataSet.java (at line 25)\n" +
			"	public ListIterator listIterator(int index) {	return null; }\n" +
			"	       ^^^^^^^^^^^^\n" +
			"ListIterator is a raw type. References to generic type ListIterator<E> should be parameterized\n" +
			"----------\n" +
			"15. WARNING in DataSet.java (at line 28)\n" +
			"	public boolean removeAll(Collection c) {	return false; }\n" +
			"	                         ^^^^^^^^^^\n" +
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" +
			"----------\n" +
			"16. WARNING in DataSet.java (at line 29)\n" +
			"	public boolean retainAll(Collection c) {	return false; }\n" +
			"	                         ^^^^^^^^^^\n" +
			"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" +
			"----------\n" +
			"17. WARNING in DataSet.java (at line 32)\n" +
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
	String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	Integer getX(List<Integer> l) {\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method getX(List<Integer>) has the same erasure getX(List<E>) as another method in type X\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 6)\n" + 
			"	String getX(List<String> l) {\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method getX(List<String>) has the same erasure getX(List<E>) as another method in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 11)\n" + 
			"	Integer getX(List<Integer> l) {\n" + 
			"	        ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Duplicate method getX(List<Integer>) in type Y\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 14)\n" + 
			"	String getX(List<Integer> l) {\n" + 
			"	       ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Duplicate method getX(List<Integer>) in type Y\n" + 
			"----------\n":
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	Integer getX(List<Integer> l) {\n" + 
				"	        ^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method getX(List<Integer>) has the same erasure getX(List<E>) as another method in type X\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	String getX(List<String> l) {\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method getX(List<String>) has the same erasure getX(List<E>) as another method in type X\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 11)\n" + 
				"	Integer getX(List<Integer> l) {\n" + 
				"	        ^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Duplicate method getX(List<Integer>) in type Y\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 14)\n" + 
				"	String getX(List<Integer> l) {\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Duplicate method getX(List<Integer>) in type Y\n" + 
				"----------\n";
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
		expectedCompilerLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159973
public void test102() {
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
   	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	Map options = getCompilerOptions();
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
	String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
			"----------\n" + 
			"1. WARNING in Bar.java (at line 2)\n" + 
			"	int getThing(V v) { return 1; }\n" + 
			"	    ^^^^^^^^^^^^^\n" + 
			"Method getThing(V) has the same erasure getThing(Object) as another method in type Foo<V,E>\n" + 
			"----------\n" + 
			"2. WARNING in Bar.java (at line 3)\n" + 
			"	boolean getThing(E e) { return true; }\n" + 
			"	        ^^^^^^^^^^^^^\n" + 
			"Method getThing(E) has the same erasure getThing(Object) as another method in type Foo<V,E>\n" + 
			"----------\n":
				"----------\n" + 
				"1. ERROR in Bar.java (at line 2)\n" + 
				"	int getThing(V v) { return 1; }\n" + 
				"	    ^^^^^^^^^^^^^\n" + 
				"Method getThing(V) has the same erasure getThing(Object) as another method in type Foo<V,E>\n" + 
				"----------\n" + 
				"2. ERROR in Bar.java (at line 3)\n" + 
				"	boolean getThing(E e) { return true; }\n" + 
				"	        ^^^^^^^^^^^^^\n" + 
				"Method getThing(E) has the same erasure getThing(Object) as another method in type Foo<V,E>\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"Bar.java",
			"class Foo<V, E> {\n" +
			"	int getThing(V v) { return 1; }\n" +
			"	boolean getThing(E e) { return true; }\n" +
			"}\n" +
			"public class Bar<V,E> extends Foo<V,E> {}"
		},
		expectedCompilerLog
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
public void test124() {
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
			"    try {\n" +
			"        System.out.println(choose(\"a\", \"b\"));\n" +
			"    } catch (StackOverflowError e) {\n" +
			"        System.out.println(\"Stack Overflow\");\n" +
			"    }\n" +
			"  }\n" +
			"}"},
			this.complianceLevel <= ClassFileConstants.JDK1_6 ? "ab" : "Stack Overflow");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150655
// variant
public void test125() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static <T> String choose(String one, String two) {\n" +
			"    return one;\n" +
			"  }\n" +
			"  public static <T> T choose(T one, T two) {\n" +
			"    return two;\n" +
			"  }\n" +
			"  public static void main(String args[]) {\n" +
			"    System.out.println(choose(\"a\", \"b\") + X.<String>choose(\"a\", \"b\"));\n" +
			"  }\n" +
			"}"
		},
		"aa"
	);
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
		"----------\n",
		null,
		true,
		null,
		"java.lang.ClassCastException");
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
			"A.java",
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
		"1. WARNING in A.java (at line 5)\n" +
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
		"1. WARNING in X.java (at line 3)\n" +
		"	public A foo(Number n) { return null; }\n" +
		"	       ^\n" +
		"Type safety: The return type A for foo(Number) from the type X2 needs unchecked conversion to conform to T from the type I\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
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
			"class Y <U> extends Z { }\n" +
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
		"Type safety: The return type A<XX> for foo(Number) from the type J needs unchecked conversion to conform to A<Exception&Cloneable> from the type I\n" + 
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
		"1. WARNING in X.java (at line 3)\n" +
		"	public XX foo(Number n) { return null; }\n" +
		"	       ^^\n" +
		"Type safety: The return type XX for foo(Number) from the type X needs unchecked conversion to conform to T from the type I\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 9)\n" +
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
		"The return types are incompatible for the inherited methods J.foo(Number), K.foo(Number)\n" +
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
// See that this test case exhibits the bug 345947
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
		"1. ERROR in PurebredCatShopImpl.java (at line 6)\n" + 
		"	<V extends Pet> List<? extends Cat> getPets();\n" + 
		"	                                    ^^^^^^^^^\n" + 
		"Name clash: The method getPets() of type CatShop has the same erasure as getPets() of type PetShop but does not override it\n" + 
		"----------\n" + 
		"2. WARNING in PurebredCatShopImpl.java (at line 10)\n" + 
		"	public List<Pet> getPets() { return null; }\n" + 
		"	       ^^^^\n" + 
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
		this.complianceLevel < ClassFileConstants.JDK1_7 ?
		"----------\n" + 
		"1. ERROR in B.java (at line 2)\n" + 
		"	static void a(X x) {}\n" + 
		"	            ^^^^^^\n" + 
		"This static method cannot hide the instance method from A\n" + 
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
		"----------\n" :
			"----------\n" + 
			"1. ERROR in B.java (at line 2)\n" + 
			"	static void a(X x) {}\n" + 
			"	            ^^^^^^\n" + 
			"This static method cannot hide the instance method from A\n" + 
			"----------\n" + 
			"2. ERROR in B.java (at line 3)\n" + 
			"	static void b(Y<String> y) {}\n" + 
			"	            ^^^^^^^^^^^^^^\n" + 
			"Name clash: The method b(Y<String>) of type B has the same erasure as b(Y<Integer>) of type A but does not hide it\n" + 
			"----------\n" + 
			"3. ERROR in B.java (at line 5)\n" + 
			"	static void d(Y<String> y) {}\n" + 
			"	            ^^^^^^^^^^^^^^\n" + 
			"Name clash: The method d(Y<String>) of type B has the same erasure as d(Y<Integer>) of type A but does not hide it\n" + 
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
			"3. ERROR in C.java (at line 5)\n" + 
			"	void d(Y<String> y) {}\n" + 
			"	     ^^^^^^^^^^^^^^\n" + 
			"Name clash: The method d(Y<String>) of type C has the same erasure as d(Y<Integer>) of type A but does not hide it\n" + 
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
		"The return types are incompatible for the inherited methods I.foo(), J.foo()\n" +
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
		"The return types are incompatible for the inherited methods I.foo(), J.foo()\n" +
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
		"The return types are incompatible for the inherited methods A.foo(), B.foo()\n" +
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
		"The return types are incompatible for the inherited methods AFoo.bar(), BFoo.bar()\n" +
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
		"The return types are incompatible for the inherited methods AFoo.bar(), BFoo.bar()\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 16)\n" + 
		"	abstract class Y extends X {}\n" + 
		"	               ^\n" + 
		"The return types are incompatible for the inherited methods AFoo.bar(), BFoo.bar()\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 17)\n" + 
		"	class Z extends X {}\n" + 
		"	      ^\n" + 
		"The type Z must implement the inherited abstract method BFoo.bar()\n" + 
		"----------\n");
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
		"1. ERROR in Concrete.java (at line 12)\n" +
		"	class Concrete extends HalfConcrete implements I<Object, String> {}\n" +
		"	      ^^^^^^^^\n" +
		"The type Concrete must implement the inherited abstract method I<Object,String>.foo(String) to override HalfGenericSuper.foo(String)\n" +
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

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239066
public void test170() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingSynchronizedOnInheritedMethod, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X { synchronized void foo() {} }\n" +
			"class Y extends X { @Override void foo() { } }"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y extends X { @Override void foo() { } }\n" +
		"	                                   ^^^^^\n" +
		"The method Y.foo() is overriding a synchronized method without being synchronized\n" +
		"----------\n",
	null,
	false,
	options);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239066 - variation
public void test171() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingSynchronizedOnInheritedMethod, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public enum X {\n" + 
			"  FOO { @Override void foo() { super.foo(); } };\n"+
			"  synchronized void foo() { }\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	FOO { @Override void foo() { super.foo(); } };\n" +
		"	                     ^^^^^\n" +
		"The method new X(){}.foo() is overriding a synchronized method without being synchronized\n" +
		"----------\n",
		null,
		false,
		options);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239066 - variation
public void test172() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingSynchronizedOnInheritedMethod, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void bar() { new X() { @Override void foo() {} }; }\n"+
			"  synchronized void foo() { }\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	void bar() { new X() { @Override void foo() {} }; }\n"+
		"	                                      ^^^^^\n" +
		"The method new X(){}.foo() is overriding a synchronized method without being synchronized\n" +
		"----------\n",
		null,
		false,
		options);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239066 - variation
public void test173() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingSynchronizedOnInheritedMethod, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X { synchronized void foo() {} }\n" +
			"class Y extends X {}\n" +
			"class Z extends Y { @Override void foo() {} }\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	class Z extends Y { @Override void foo() {} }\n" +
		"	                                   ^^^^^\n" +
		"The method Z.foo() is overriding a synchronized method without being synchronized\n" +
		"----------\n",
		null,
		false,
		options);
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
		"The type X must implement the inherited abstract method I.m() to override Y.m()\n" + 
		"----------\n"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=38751
public void test175() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingHashCodeMethod, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"A.java",
			"class A {\n" +
			"	@Override public boolean equals(Object o) { return true; }\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in A.java (at line 1)\n" + 
		"	class A {\n" + 
		"	      ^\n" + 
		"The type A should also implement hashCode() since it overrides Object.equals()\n" + 
		"----------\n",
	null,
	false,
	options);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=38751
public void test176() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingHashCodeMethod, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"A.java",
			"class A {\n" +
			"	@Override public boolean equals(Object o) { return true; }\n" +
			"	@Override public int hashCode() { return 1; }\n" +
			"}\n" +
			"class B extends A {\n" +
			"	@Override public boolean equals(Object o) { return false; }\n" +
			"}"
		},
		"",
	null,
	false,
	options);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251091
// Srikanth, Aug 10th 2010. This test does not elicit any name clash error from javac 5 or javac6
// javac7 reports "X.java:7: name clash: foo(Collection<?>) in X and foo(Collection) in A have the
// same erasure, yet neither overrides the other"
// After the fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=322001, we match
// JDK7 (7b100) behavior. (earlier we would issue an extra name clash)
public void test177() {
	if (new CompilerOptions(getCompilerOptions()).complianceLevel >= ClassFileConstants.JDK1_6) { // see test187()
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
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
				"5. WARNING in X.java (at line 7)\n" + 
				"	@Override public X foo(Collection<?> c) { return this; }\n" + 
				"	                   ^^^^^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method foo(Collection<?>) of type X has the same erasure as foo(Collection) of type A but does not override it\n" + 
				"----------\n":
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
					"Name clash: The method foo(Collection<?>) of type X has the same erasure as foo(Collection) of type A but does not override it\n" + 
					"----------\n";
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
			expectedCompilerLog
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
			"Name clash: The method foo(Collection<?>) of type X has the same erasure as foo(Collection) of type A but does not override it\n" + 
			"----------\n" +
			"6. ERROR in X.java (at line 7)\n" + 
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

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=163093
public void test179() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface Adaptable {\n" + 
			"	public Object getAdapter(Class clazz);	\n" + 
			"}\n" + 
			"\n" + 
			"public class X implements Adaptable {\n" + 
			"	@Override\n" + 
			"	public Object getAdapter(Class<?> clazz) {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 2)\n" + 
		"	public Object getAdapter(Class clazz);	\n" + 
		"	                         ^^^^^\n" + 
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	public class X implements Adaptable {\n" + 
		"	             ^\n" + 
		"The type X must implement the inherited abstract method Adaptable.getAdapter(Class)\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 7)\n" + 
		"	public Object getAdapter(Class<?> clazz) {\n" + 
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Name clash: The method getAdapter(Class<?>) of type X has the same erasure as getAdapter(Class) of type Adaptable but does not override it\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 7)\n" + 
		"	public Object getAdapter(Class<?> clazz) {\n" + 
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		mustOverrideMessage("getAdapter(Class<?>)", "X") + 
		"----------\n"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255035
public void test180() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class S {\n" +
			"	String foo() { return null; }\n" +
			"}\n" +
			"class X extends S {\n" +
			"	foo() { return null; }\n" +
			"	@Override String foo() { return null; }\n" + // should keep this definition
			"	Number foo() { return null; }\n" +
			"	void test() { foo(); }\n" + // no secondary error
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	foo() { return null; }\n" + 
		"	^^^^^\n" + 
		"Return type for the method is missing\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	foo() { return null; }\n" + 
		"	^^^^^\n" + 
		"Duplicate method foo() in type X\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 6)\n" + 
		"	@Override String foo() { return null; }\n" + 
		"	                 ^^^^^\n" + 
		"Duplicate method foo() in type X\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 7)\n" + 
		"	Number foo() { return null; }\n" + 
		"	       ^^^^^\n" + 
		"Duplicate method foo() in type X\n" + 
		"----------\n"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249134
public void test181() {
	this.runNegativeTest(
		new String[] {
			"I.java",
			"interface I {\n" +
			"	String m();\n" +
			"	Object n();\n" +
			"	String o();\n" +
			"	Object p();\n" +
			"}\n" +
			"abstract class A {\n" +
			"	public abstract Object m();\n" +
			"	public abstract String n();\n" +
			"	abstract Object o();\n" +
			"	abstract String p();\n" +
			"}\n" +
			"class A2 {\n" +
			"	public abstract Object m();\n" +
			"	public abstract String n();\n" +
			"	abstract Object o();\n" +
			"	abstract String p();\n" +
			"}\n",
			"B.java",
			"class B extends A implements I {}",
			"B2.java",
			"class B2 extends A2 implements I {}"
		},
		"----------\n" + 
		"1. ERROR in I.java (at line 13)\n" + 
		"	class A2 {\n" + 
		"	      ^^\n" + 
		"The type A2 must be an abstract class to define abstract methods\n" + 
		"----------\n" + 
		"2. ERROR in I.java (at line 14)\n" + 
		"	public abstract Object m();\n" + 
		"	                       ^^^\n" + 
		"The abstract method m in type A2 can only be defined by an abstract class\n" + 
		"----------\n" + 
		"3. ERROR in I.java (at line 15)\n" + 
		"	public abstract String n();\n" + 
		"	                       ^^^\n" + 
		"The abstract method n in type A2 can only be defined by an abstract class\n" + 
		"----------\n" + 
		"4. ERROR in I.java (at line 16)\n" + 
		"	abstract Object o();\n" + 
		"	                ^^^\n" + 
		"The abstract method o in type A2 can only be defined by an abstract class\n" + 
		"----------\n" + 
		"5. ERROR in I.java (at line 17)\n" + 
		"	abstract String p();\n" + 
		"	                ^^^\n" + 
		"The abstract method p in type A2 can only be defined by an abstract class\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in B.java (at line 1)\n" + 
		"	class B extends A implements I {}\n" + 
		"	      ^\n" + 
		"The type B must implement the inherited abstract method A.p()\n" + 
		"----------\n" + 
		"2. ERROR in B.java (at line 1)\n" + 
		"	class B extends A implements I {}\n" + 
		"	      ^\n" + 
		"The type B must implement the inherited abstract method I.o() to override A.o()\n" + 
		"----------\n" + 
		"3. ERROR in B.java (at line 1)\n" + 
		"	class B extends A implements I {}\n" + 
		"	      ^\n" + 
		"The type B must implement the inherited abstract method A.n()\n" + 
		"----------\n" + 
		"4. ERROR in B.java (at line 1)\n" + 
		"	class B extends A implements I {}\n" + 
		"	      ^\n" + 
		"The type B must implement the inherited abstract method I.m() to override A.m()\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in B2.java (at line 1)\n" + 
		"	class B2 extends A2 implements I {}\n" + 
		"	      ^^\n" + 
		"The type B2 must implement the inherited abstract method I.o() to override A2.o()\n" + 
		"----------\n" + 
		"2. ERROR in B2.java (at line 1)\n" + 
		"	class B2 extends A2 implements I {}\n" + 
		"	      ^^\n" + 
		"The type B2 must implement the inherited abstract method I.m() to override A2.m()\n" + 
		"----------\n",
		null,
		true,
		null,
		true,
		false,
		false
	);
	this.runNegativeTest(
		new String[] {
			"B.java",
			"class B extends A implements I {}",
			"B2.java",
			"class B2 extends A2 implements I {}"
		},
		"----------\n" + 
		"1. ERROR in B.java (at line 1)\n" + 
		"	class B extends A implements I {}\n" + 
		"	      ^\n" + 
		"The type B must implement the inherited abstract method A.p()\n" + 
		"----------\n" + 
		"2. ERROR in B.java (at line 1)\n" + 
		"	class B extends A implements I {}\n" + 
		"	      ^\n" + 
		"The type B must implement the inherited abstract method I.o() to override A.o()\n" + 
		"----------\n" + 
		"3. ERROR in B.java (at line 1)\n" + 
		"	class B extends A implements I {}\n" + 
		"	      ^\n" + 
		"The type B must implement the inherited abstract method A.n()\n" + 
		"----------\n" + 
		"4. ERROR in B.java (at line 1)\n" + 
		"	class B extends A implements I {}\n" + 
		"	      ^\n" + 
		"The type B must implement the inherited abstract method I.m() to override A.m()\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in B2.java (at line 1)\n" + 
		"	class B2 extends A2 implements I {}\n" + 
		"	      ^^\n" + 
		"The type B2 must implement the inherited abstract method I.o() to override A2.o()\n" + 
		"----------\n" + 
		"2. ERROR in B2.java (at line 1)\n" + 
		"	class B2 extends A2 implements I {}\n" + 
		"	      ^^\n" + 
		"The type B2 must implement the inherited abstract method I.m() to override A2.m()\n" + 
		"----------\n",
		null,
		false
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249134
public void test182() {
	this.runNegativeTest(
		new String[] {
			"I.java",
			"interface I {\n" +
			"	String m();\n" +
			"	Object n();\n" +
			"}\n" +
			"class A {\n" +
			"	public Object m() { return null; }\n" +
			"	public String n() { return null; }\n" +
			"}\n" +
			"abstract class A2 {\n" +
			"	public Object m() { return null; }\n" +
			"	public String n() { return null; }\n" +
			"}\n",
			"B.java",
			"class B extends A implements I {}",
			"B2.java",
			"class B2 extends A2 implements I {}"
		},
		"----------\n" + 
		"1. ERROR in B.java (at line 1)\n" + 
		"	class B extends A implements I {}\n" + 
		"	      ^\n" + 
		"The type B must implement the inherited abstract method I.m() to override A.m()\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in B2.java (at line 1)\n" + 
		"	class B2 extends A2 implements I {}\n" + 
		"	      ^^\n" + 
		"The type B2 must implement the inherited abstract method I.m() to override A2.m()\n" + 
		"----------\n"
	);
	this.runNegativeTest(
		new String[] {
			"B.java",
			"class B extends A implements I {}",
			"B2.java",
			"class B2 extends A2 implements I {}"
		},
		"----------\n" + 
		"1. ERROR in B.java (at line 1)\n" + 
		"	class B extends A implements I {}\n" + 
		"	      ^\n" + 
		"The type B must implement the inherited abstract method I.m() to override A.m()\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in B2.java (at line 1)\n" + 
		"	class B2 extends A2 implements I {}\n" + 
		"	      ^^\n" + 
		"The type B2 must implement the inherited abstract method I.m() to override A2.m()\n" + 
		"----------\n",
		null,
		false
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=262208
public void test183() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class XX {\n" +
			"	<T extends C, S extends G<T>> void a(S gC) {}\n" +
			"	<T extends C, S extends G<T>> void b(T c) {}\n" +
			"	<T extends C> void c(G<T> gC) {}\n" +
			"	<T extends C, S extends G<T>> void d(S gC) {}\n" +
			"}\n" +
			"class X extends XX {\n" +
			"	@Override void a(G g) {}\n" +
			"	@Override void b(C c) {}\n" +
			"	@Override void c(G g) {}\n" +
			"	@Override <T extends C, S extends G<C>> void d(S gc) {}\n" +
			"}\n" +
			"class C {}\n" +
			"class G<T2> {}"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 8)\n" + 
		"	@Override void a(G g) {}\n" + 
		"	                 ^\n" + 
		"G is a raw type. References to generic type G<T2> should be parameterized\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 10)\n" + 
		"	@Override void c(G g) {}\n" + 
		"	                 ^\n" + 
		"G is a raw type. References to generic type G<T2> should be parameterized\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 11)\n" + 
		"	@Override <T extends C, S extends G<C>> void d(S gc) {}\n" + 
		"	                                             ^^^^^^^\n" + 
		"Name clash: The method d(S) of type X has the same erasure as d(S) of type XX but does not override it\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 11)\n" + 
		"	@Override <T extends C, S extends G<C>> void d(S gc) {}\n" + 
		"	                                             ^^^^^^^\n" + 
		mustOverrideMessage("d(S)", "X") + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=264881
public void test184() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"class A<U extends Number> {\n" +
			"	<T extends A<Number>> T a() { return null; }\n" +
			"	<T extends Number> U num() { return null; }\n" +
			"	<T> T x() { return null; }\n" +
			"	<T extends Number> T y() { return null; }\n" +
			"	<T extends Integer> T z() { return null; }\n" +
			"}\n" +
			"class B extends A<Double> {\n" +
			"	@Override A a() { return null; }\n" +
			"	@Override Double num() { return 1.0; }\n" +
			"	@Override Integer x() { return 1; }\n" +
			"	@Override Integer y() { return 1; }\n" +
			"	@Override Integer z() { return 1; }\n" +
			"}\n" +
			"class C extends A {\n" +
			"	@Override A a() { return null; }\n" +
			"	@Override Double num() { return 1.0; }\n" +
			"	@Override Integer x() { return 1; }\n" +
			"	@Override Integer y() { return 1; }\n" +
			"	@Override Integer z() { return 1; }\n" +
			"}\n" +
			"class M {\n" +
			"	<T extends M> Object m(Class<T> c) { return null; }\n" +
			"	<T extends M> Object n(Class<T> c) { return null; }\n" +
			"}\n" +
			"class N<V> extends M {\n" +
			"	@Override <T extends M> T m(Class<T> c) { return null; }\n" +
			"	@Override <T extends M> V n(Class<T> c) { return null; }\n" +
			"}"
		},
		"----------\n" + 
		"1. WARNING in A.java (at line 6)\n" + 
		"	<T extends Integer> T z() { return null; }\n" + 
		"	           ^^^^^^^\n" + 
		"The type parameter T should not be bounded by the final type Integer. Final types cannot be further extended\n" + 
		"----------\n" + 
		"2. WARNING in A.java (at line 9)\n" + 
		"	@Override A a() { return null; }\n" + 
		"	          ^\n" + 
		"A is a raw type. References to generic type A<U> should be parameterized\n" + 
		"----------\n" + 
		"3. WARNING in A.java (at line 9)\n" + 
		"	@Override A a() { return null; }\n" + 
		"	          ^\n" + 
		"Type safety: The return type A for a() from the type B needs unchecked conversion to conform to T from the type A<U>\n" + 
		"----------\n" + 
		"4. WARNING in A.java (at line 11)\n" + 
		"	@Override Integer x() { return 1; }\n" + 
		"	          ^^^^^^^\n" + 
		"Type safety: The return type Integer for x() from the type B needs unchecked conversion to conform to T from the type A<U>\n" + 
		"----------\n" + 
		"5. WARNING in A.java (at line 12)\n" + 
		"	@Override Integer y() { return 1; }\n" + 
		"	          ^^^^^^^\n" + 
		"Type safety: The return type Integer for y() from the type B needs unchecked conversion to conform to T from the type A<U>\n" + 
		"----------\n" + 
		"6. WARNING in A.java (at line 13)\n" + 
		"	@Override Integer z() { return 1; }\n" + 
		"	          ^^^^^^^\n" + 
		"Type safety: The return type Integer for z() from the type B needs unchecked conversion to conform to T from the type A<U>\n" + 
		"----------\n" + 
		"7. WARNING in A.java (at line 15)\n" + 
		"	class C extends A {\n" + 
		"	                ^\n" + 
		"A is a raw type. References to generic type A<U> should be parameterized\n" + 
		"----------\n" + 
		"8. WARNING in A.java (at line 16)\n" + 
		"	@Override A a() { return null; }\n" + 
		"	          ^\n" + 
		"A is a raw type. References to generic type A<U> should be parameterized\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=267088
public void test185() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"interface I { I hello(); }\n" +
			"interface J { J hello(); }\n" +
			"class A implements I, J {}"
		},
		"----------\n" + 
		"1. ERROR in A.java (at line 3)\n" + 
		"	class A implements I, J {}\n" + 
		"	      ^\n" + 
		"The type A must implement the inherited abstract method J.hello()\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=271303
public void test186() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" +
			"public class A { void m() {} }\n",
			"p2/B.java",
			"package p2;\n" +
			"public class B extends p1.A { void m() {} }\n",
			"p1/C.java",
			"package p1;\n" +
			"public class C extends p2.B { @Override void m() {} }"
		},
		"----------\n" + 
		"1. WARNING in p2\\B.java (at line 2)\n" + 
		"	public class B extends p1.A { void m() {} }\n" + 
		"	                                   ^^^\n" + 
		"The method B.m() does not override the inherited method from A since it is private to a different package\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. WARNING in p1\\C.java (at line 2)\n" + 
		"	public class C extends p2.B { @Override void m() {} }\n" + 
		"	                                             ^^^\n" + 
		"The method C.m() does not override the inherited method from B since it is private to a different package\n" + 
		"----------\n"
	);
}
// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=?
public void test187() {
	String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6 )?
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	double f(List<Integer> l) {return 0;}\n" + 
			"	       ^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method f(List<Integer>) of type Y has the same erasure as f(List<String>) of type X but does not override it\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 13)\n" + 
			"	int f(List<String> l) {return 0;}\n" + 
			"	    ^^^^^^^^^^^^^^^^^\n" + 
			"Method f(List<String>) has the same erasure f(List<E>) as another method in type XX\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 14)\n" + 
			"	double f(List<Integer> l) {return 0;}\n" + 
			"	       ^^^^^^^^^^^^^^^^^^\n" + 
			"Method f(List<Integer>) has the same erasure f(List<E>) as another method in type XX\n" + 
			"----------\n":
				"----------\n" + 
				"1. ERROR in X.java (at line 6)\n" + 
				"	double f(List<Integer> l) {return 0;}\n" + 
				"	       ^^^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method f(List<Integer>) of type Y has the same erasure as f(List<String>) of type X but does not override it\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 13)\n" + 
				"	int f(List<String> l) {return 0;}\n" + 
				"	    ^^^^^^^^^^^^^^^^^\n" + 
				"Method f(List<String>) has the same erasure f(List<E>) as another method in type XX\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 14)\n" + 
				"	double f(List<Integer> l) {return 0;}\n" + 
				"	       ^^^^^^^^^^^^^^^^^^\n" + 
				"Method f(List<Integer>) has the same erasure f(List<E>) as another method in type XX\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"class X {\n" +
			"    int f(List<String> l) {return 0;}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"    double f(List<Integer> l) {return 0;}\n" +// name clash in 7
			"}\n" +
			"interface I {\n" +
			"	double f(List<Integer> l);\n" +
			"}\n" +
			"abstract class Z extends X implements I {}\n" +
			"class XX {\n" +
			"    int f(List<String> l) {return 0;}\n" +
    			"double f(List<Integer> l) {return 0;}\n" +// name clash in 1.5 & 7
			"}"
		},
		expectedCompilerLog
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=279836
public void test188() {
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"abstract class Y<T extends Number> implements I<T> {\n" +
			"	public T get(T element) { return null; }\n" +
			"}\n" +
			"interface I<T> { T get(T element); }\n" +
			"class Z extends Y {}"
		},
		"----------\n" + 
		"1. WARNING in Y.java (at line 5)\n" + 
		"	class Z extends Y {}\n" + 
		"	                ^\n" + 
		"Y is a raw type. References to generic type Y<T> should be parameterized\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284431
public void test189() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface Interface {\n" + 
			"    void foo() throws CloneNotSupportedException, InterruptedException;\n" + 
			"}\n" + 
			"abstract class AbstractClass1 {\n" + 
			"    public abstract void foo() throws ClassNotFoundException, CloneNotSupportedException;\n" + 
			"}\n" + 
			"abstract class AbstractClass2 extends AbstractClass1  implements Interface {\n" + 
			"	void bar() {\n" + 
			"        try {\n" + 
			"        	foo();\n" + 
			"        } catch (CloneNotSupportedException e) {\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n" + 
			"\n" + 
			"class X extends AbstractClass2 {\n" + 
			"	@Override\n" + 
			"	public void foo() throws CloneNotSupportedException {\n" + 
			"	}\n" + 
			"}\n" + 
			"class Y extends AbstractClass2 {\n" +
			"	@Override public void foo() throws ClassNotFoundException, CloneNotSupportedException {}\n" +
			"}\n" +
			"class Z extends AbstractClass2 {\n" +
			"	@Override public void foo() throws CloneNotSupportedException, InterruptedException {}\n" +
			"}\n" +
			"class All extends AbstractClass2 {\n" +
			"	@Override public void foo() throws ClassNotFoundException, CloneNotSupportedException, InterruptedException {}\n" +
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 22)\n" + 
		"	@Override public void foo() throws ClassNotFoundException, CloneNotSupportedException {}\n" + 
		"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Exception ClassNotFoundException is not compatible with throws clause in Interface.foo()\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 25)\n" + 
		"	@Override public void foo() throws CloneNotSupportedException, InterruptedException {}\n" + 
		"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Exception InterruptedException is not compatible with throws clause in AbstractClass1.foo()\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 28)\n" + 
		"	@Override public void foo() throws ClassNotFoundException, CloneNotSupportedException, InterruptedException {}\n" + 
		"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Exception ClassNotFoundException is not compatible with throws clause in Interface.foo()\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 28)\n" + 
		"	@Override public void foo() throws ClassNotFoundException, CloneNotSupportedException, InterruptedException {}\n" + 
		"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Exception InterruptedException is not compatible with throws clause in AbstractClass1.foo()\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test190() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" + 
			"public class A {\n" + 
			"	void foo() {}\n" + 
			"}",
			"p1/B.java",
			"package p1;\n" + 
			"public class B extends p2.C {\n" + 
			"	@Override public int foo() { return 0; }\n" + 
			"}",
			"p2/C.java",
			"package p2;\n" + 
			"public class C extends p1.A {\n" + 
			"	public int foo() { return 1; }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in p1\\B.java (at line 3)\n" + 
		"	@Override public int foo() { return 0; }\n" + 
		"	                 ^^^\n" + 
		"The return type is incompatible with A.foo()\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. WARNING in p2\\C.java (at line 3)\n" + 
		"	public int foo() { return 1; }\n" + 
		"	           ^^^^^\n" + 
		"The method C.foo() does not override the inherited method from A since it is private to a different package\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test191() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" + 
			"public class A {\n" + 
			"	static void foo() {}\n" + 
			"}",
			"p1/B.java",
			"package p1;\n" + 
			"public class B extends p2.C {\n" + 
			"	public static int foo() { return 0; }\n" + 
			"}",
			"p2/C.java",
			"package p2;\n" + 
			"public class C extends p1.A {\n" + 
			"	public static int foo() { return 1; }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in p1\\B.java (at line 3)\n" + 
		"	public static int foo() { return 0; }\n" + 
		"	              ^^^\n" + 
		"The return type is incompatible with A.foo()\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test192() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" + 
			"public class A {\n" + 
			"	void foo() {}\n" + 
			"}",
			"p1/B.java",
			"package p1;\n" + 
			"public class B extends p2.C {\n" + 
			"	public static int foo() { return 0; }\n" + 
			"}",
			"p2/C.java",
			"package p2;\n" + 
			"public class C extends p1.A {\n" + 
			"	public static int foo() { return 1; }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in p1\\B.java (at line 3)\n" + 
		"	public static int foo() { return 0; }\n" + 
		"	                  ^^^^^\n" + 
		"This static method cannot hide the instance method from A\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test193() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" + 
			"public class A {\n" + 
			"	void foo() {}\n" + 
			"}",
			"p1/B.java",
			"package p1;\n" + 
			"public class B extends p2.C {\n" + 
			"	@Override public int foo() { return 0; }\n" + 
			"}",
			"p2/C.java",
			"package p2;\n" + 
			"public class C extends p1.A {\n" + 
			"	public static int foo() { return 1; }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in p1\\B.java (at line 3)\n" + 
		"	@Override public int foo() { return 0; }\n" + 
		"	                 ^^^\n" + 
		"The return type is incompatible with A.foo()\n" + 
		"----------\n" + 
		"2. ERROR in p1\\B.java (at line 3)\n" + 
		"	@Override public int foo() { return 0; }\n" + 
		"	                     ^^^^^\n" + 
		"This instance method cannot override the static method from C\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test194() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" + 
			"public class A {\n" + 
			"	static void foo() {}\n" + 
			"}",
			"p1/B.java",
			"package p1;\n" + 
			"public class B extends p2.C {\n" + 
			"	public int foo() { return 0; }\n" + 
			"}",
			"p2/C.java",
			"package p2;\n" + 
			"public class C extends p1.A {\n" + 
			"	public static int foo() { return 1; }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in p1\\B.java (at line 3)\n" + 
		"	public int foo() { return 0; }\n" + 
		"	           ^^^^^\n" + 
		"This instance method cannot override the static method from A\n" + 
		"----------\n" + 
		"2. ERROR in p1\\B.java (at line 3)\n" + 
		"	public int foo() { return 0; }\n" + 
		"	           ^^^^^\n" + 
		"This instance method cannot override the static method from C\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test195() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" + 
			"public class A {\n" + 
			"	static void foo() {}\n" + 
			"}",
			"p1/B.java",
			"package p1;\n" + 
			"public class B extends p2.C {\n" + 
			"	@Override public int foo() { return 0; }\n" + 
			"}",
			"p2/C.java",
			"package p2;\n" + 
			"public class C extends p1.A {\n" + 
			"	public int foo() { return 1; }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in p1\\B.java (at line 3)\n" + 
		"	@Override public int foo() { return 0; }\n" + 
		"	                     ^^^^^\n" + 
		"This instance method cannot override the static method from A\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test196() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" + 
			"public class A {\n" + 
			"	static void foo() {}\n" + 
			"}",
			"p1/B.java",
			"package p1;\n" + 
			"public class B extends p2.C {\n" + 
			"	public static int foo() { return 0; }\n" + 
			"}",
			"p2/C.java",
			"package p2;\n" + 
			"public class C extends p1.A {\n" + 
			"	public int foo() { return 1; }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in p1\\B.java (at line 3)\n" + 
		"	public static int foo() { return 0; }\n" + 
		"	              ^^^\n" + 
		"The return type is incompatible with A.foo()\n" + 
		"----------\n" + 
		"2. ERROR in p1\\B.java (at line 3)\n" + 
		"	public static int foo() { return 0; }\n" + 
		"	                  ^^^^^\n" + 
		"This static method cannot hide the instance method from C\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284482
public void test197() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" + 
			"public class A {\n" + 
			"	void foo() {}\n" + 
			"}",
			"p1/B.java",
			"package p1;\n" + 
			"public class B extends p2.C {\n" + 
			"	public static int foo() { return 0; }\n" + 
			"}",
			"p2/C.java",
			"package p2;\n" + 
			"public class C extends p1.A {\n" + 
			"	public int foo() { return 1; }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in p1\\B.java (at line 3)\n" + 
		"	public static int foo() { return 0; }\n" + 
		"	                  ^^^^^\n" + 
		"This static method cannot hide the instance method from A\n" + 
		"----------\n" + 
		"2. ERROR in p1\\B.java (at line 3)\n" + 
		"	public static int foo() { return 0; }\n" + 
		"	                  ^^^^^\n" + 
		"This static method cannot hide the instance method from C\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. WARNING in p2\\C.java (at line 3)\n" + 
		"	public int foo() { return 1; }\n" + 
		"	           ^^^^^\n" + 
		"The method C.foo() does not override the inherited method from A since it is private to a different package\n" + 
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284948
public void test198() {
	if (new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6) return;

	this.runConformTest(
		new String[] {
			"MyAnnotation.java",
			"@interface MyAnnotation {\n" + 
			"    MyEnum value();\n" + 
			"}",
			"MyClass.java",
			"public class MyClass implements MyInterface {\n" + 
			"    @Override public void foo() {}\n" + 
			"}",
			"MyEnum.java",
			"enum MyEnum implements Runnable {\n" + 
			"	G {\n" + 
			"		@Override public void methodA() {\n" + 
			"			new Runnable() {\n" + 
			"				@Override public void run() {}\n" + 
			"			};\n" + 
			"		}\n" + 
			"	},\n" + 
			"	D {\n" + 
			"		@Override public void methodA() {}\n" + 
			"	},\n" + 
			"	A {\n" + 
			"		@Override public void methodA() {}\n" + 
			"		@Override public void methodB() {}\n" + 
			"	},\n" + 
			"	B {\n" + 
			"		@Override public void methodA() {}\n" + 
			"	},\n" + 
			"	C {\n" + 
			"		@Override public void methodA() {}\n" + 
			"		@Override public void methodB() {}\n" + 
			"	},\n" + 
			"	E {\n" + 
			"		@Override public void methodA() {}\n" + 
			"	},\n" + 
			"	F {\n" + 
			"		@Override public void methodA() {}\n" + 
			"	};\n" + 
			"	private MyEnum() {}\n" + 
			"	public void methodA() {}\n" + 
			"	public void methodB() {}\n" + 
			"	@Override public void run() {}\n" + 
			"}",
			"MyInterface.java",
			"interface MyInterface {\n" + 
			"    @MyAnnotation(MyEnum.D) public void foo();\n" + 
			"}"
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284785
public void test199() {
	this.runConformTest(
		new String[] {
			"Bar.java",
			"public interface Bar {\n" + 
			"	void addError(String message, Object... arguments);\n" + 
			"	void addError(Throwable t);\n" + 
			"}",
		},
		""
	);
	this.runConformTest(
		false,
		new String[] {
			"Foo.java",
			"public class Foo {\n" + 
			"	void bar(Bar bar) {\n" + 
			"		bar.addError(\"g\");\n" + 
			"	}\n" + 
			"}"
		},
		"",
		"",
		"",
		JavacTestOptions.SKIP
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285088
public void test200() {
	String errorMessage =
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	int foo(Collection bar) { return 0; }\n" + 
				"	    ^^^^^^^^^^^^^^^^^^^\n" + 
				"Method foo(Collection) has the same erasure foo(Collection<E>) as another method in type X\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	int foo(Collection bar) { return 0; }\n" + 
				"	        ^^^^^^^^^^\n" + 
				"Collection is a raw type. References to generic type Collection<E> should be parameterized\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	double foo(Collection<String> bar) {return 0; }\n" + 
				"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method foo(Collection<String>) has the same erasure foo(Collection<E>) as another method in type X\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Collection;\n" +
			"class X {\n" +
			"	int foo(Collection bar) { return 0; }\n" +
			"	double foo(Collection<String> bar) {return 0; }\n" +
			"}"
		},
		errorMessage
	);
/* javac 7
X.java:4: foo(Collection) is already defined in X
        double foo(Collection<String> bar) {return 0; }
               ^
1 error
 */
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=286228
public void test201() {
	this.runConformTest(
		new String[] {
			"A.java",
			"interface I {}\n" +
			"interface J<T1> { J<T1> get(); }\n" +
			"interface K<T2 extends J<? extends I>> { T2 get(); }\n" +
			"interface A<T3 extends K<T3> & J<? extends I>> extends J<I> {}\n" +
			"interface B<T4 extends J<? extends I> & K<T4>> extends J<I> {}"
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=284280
public void test202() {
	this.runConformTest(
		new String[] {
			"SubClass.java",
			"interface MyInterface <T0 extends Object> {\n" +
			"	String testMe(T0 t);\n" +
			"}\n" +
			"abstract class AbstractSuperClass<T1 extends AbstractSuperClass> implements MyInterface<T1> {\n" +
			"	public String testMe(T1 o) { return null; }\n" +
			"}\n" +
			"class SubClass extends AbstractSuperClass<SubClass> {\n" +
			"   @Override public String testMe(SubClass o) {\n" +
			"      return super.testMe(o);\n" +
			"   }\n" +
			"}"
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=292240
public void test203() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {}\n" +
			"interface Y<T extends I> extends java.util.Comparator<T> {\n" +
			"	public int compare(T o1, T o2);\n" +
			"}\n" +
			"class X implements Y {\n" +
			"	public int compare(Object o1, Object o2) {\n" +
			"		return compare((I) o1, (I) o2);\n" +
			"	}\n" +
			"	public int compare(I o1, I o2) { return 0; }\n" +
			"}"
		},
		""
	);
}
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=293615 (bad name clash error)
// No user vs user clash or user vs synthetic clash in this test
public void test204() {
	this.runConformTest(
		new String[] {
			"OverrideBug.java",
			"import java.util.List;\n" +
			"interface Map<K, V> {\n" + 
			"	public V put(K key, V value);\n" +
			"}\n" +
			"public class OverrideBug<K, V> implements Map<K, List<V>> {\n" +
			"public List<V> put(final K arg0, final List<V> arg1) {\n" +
			"    return null;\n" +
			"}\n" +
			"public List<V> put(final K arg0, final V arg1) {\n" +
			"    return null;\n" +
			"}\n" +
			"}"
		},
		"");
}
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=293615 (bad name clash error)
// verify that we report user vs bridge clash properly.
public void test204a() {
	this.runNegativeTest(
		new String[] {
			"OverrideBug.java",
			"import java.util.List;\n" +
			"interface Map<K, V> {\n" + 
			"	public V put(K key, V value);\n" +
			"}\n" +
			"public class OverrideBug<K, V> implements Map<K, List<V>> {\n" +
			"public List<V> put(final K arg0, final List<V> arg1) {\n" +
			"    return null;\n" +
			"}\n" +
			"public V put(final K arg0, final V arg1) {\n" +
			"    return null;\n" +
			"}\n" +
			"}"
		},
		"----------\n" + 
		"1. ERROR in OverrideBug.java (at line 9)\n" + 
		"	public V put(final K arg0, final V arg1) {\n" + 
		"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Name clash: The method put(K, V) of type OverrideBug<K,V> has the same erasure as put(K, V) of type Map<K,V> but does not override it\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=298362
public void test205() {
	this.runConformTest(
		new String[] {
			"Tester.java",
			"import java.lang.reflect.Method;\n" +
			"\n" +
			"public class Tester {\n" +
			"\n" +
			" public static interface Converter<T> {\n" +
			"   T convert(String input);\n" +
			" }\n" +
			"\n" +
			" public static abstract class EnumConverter<T extends Enum<T>> implements Converter<Enum<T>> {\n" +
			"   public final T convert(String input) {\n" +
			"     return null;\n" +
			"   }\n" +
			" }\n" +
			"\n" +
			" public static class SomeEnumConverter extends EnumConverter<Thread.State> {\n" +
			" }\n" +
			"\n" +
			" public static void main(String[] args) throws Exception {\n" +
			"   Method m = SomeEnumConverter.class.getMethod(\"convert\", String.class);\n" +
			"   System.out.println(m.getGenericReturnType());\n" +
			" }\n" +
			"\n" +
			"}\n"

		},
		"T");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=298362 (variation)
public void test206() {
	this.runConformTest(
		new String[] {
			"Tester.java",
			"import java.lang.reflect.Method;\n" +
			"\n" +
			"public class Tester {\n" +
			"\n" +
			" public static interface Converter<T> {\n" +
			"   T convert(String input);\n" +
			" }\n" +
			"\n" +
			" public static abstract class EnumConverter<T extends Enum<T>> implements Converter<T> {\n" +
			"   public final T convert(String input) {\n" +
			"     return null;\n" +
			"   }\n" +
			" }\n" +
			"\n" +
			" public static class SomeEnumConverter extends EnumConverter<Thread.State> {\n" +
			" }\n" +
			"\n" +
			" public static void main(String[] args) throws Exception {\n" +
			"   Method m = SomeEnumConverter.class.getMethod(\"convert\", String.class);\n" +
			"   System.out.println(m.getGenericReturnType());\n" +
			" }\n" +
			"\n" +
			"}\n"

		},
		"T");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=298362 (variation)
// Note that this test prints "T" with javac5 and "class java.lang.Object with javac 6,7
public void test207() {
	this.runConformTest(
		new String[] {
			"Tester.java",
			"import java.lang.reflect.Method;\n" +
			"\n" +
			"public class Tester {\n" +
			"\n" +
			" public static interface Converter<T> {\n" +
			"   T convert(String input);\n" +
			" }\n" +
			"\n" +
			" public static abstract class EnumConverter<T extends Enum<T>, K> implements Converter<T> {\n" +
			"   public final T convert(K input) {\n" +
			"     return null;\n" +
			"   }\n" +
			" }\n" +
			"\n" +
			" public static class SomeEnumConverter extends EnumConverter<Thread.State, String> {\n" +
			" }\n" +
			"\n" +
			" public static void main(String[] args) throws Exception {\n" +
			"   Method m = SomeEnumConverter.class.getMethod(\"convert\", String.class);\n" +
			"   System.out.println(m.getGenericReturnType());\n" +
			" }\n" +
			"\n" +
			"}\n"

		},
		"class java.lang.Object");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=288658, make sure a bridge method
// is generated when a public method is inherited from a non-public class into a
// public class.
public void test208() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"import java.lang.annotation.Annotation;\n"+ 
			"import java.lang.annotation.Retention;\n"+ 
			"import java.lang.annotation.RetentionPolicy;\n"+ 
			"import java.lang.reflect.Method;\n"+ 
			"\n"+ 
			"public class Test extends Super {\n"+ 
			"    public static void main(String[] args) {\n"+ 
			"        try {\n"+ 
			"            Method m = Test.class.getMethod(\"setFoo\", String.class);\n"+
			"            Annotation a = m.getAnnotation(Anno.class);\n"+ 
			"            System.out.println(\"Annotation was \" + (a == null ? \"not \" : \"\") +\n"+ 
			"\"found\");\n"+ 
			"        } catch (Exception e) {\n"+ 
			"            e.printStackTrace();\n"+ 
			"        }\n"+ 
			"    }\n"+ 
			"}\n"+ 
			"\n"+ 
			"class Super {\n"+ 
			"    @Anno\n"+ 
			"    public void setFoo(String foo) {}\n"+ 
			"}\n"+ 
			"\n"+ 
			"@Retention(RetentionPolicy.RUNTIME)\n"+ 
			"@interface Anno {\n"+ 
			"\n"+ 
			"}\n"
		},
		this.complianceLevel <= ClassFileConstants.JDK1_5 ? "Annotation was found" : "Annotation was not found");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=288658, make sure a bridge method
// is generated when a public method is inherited from a non-public class into a
// public class.
public void test208a() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"import java.lang.annotation.Annotation;\n"+ 
			"import java.lang.annotation.Retention;\n"+ 
			"import java.lang.annotation.RetentionPolicy;\n"+ 
			"import java.lang.reflect.Method;\n"+ 
			"\n"+ 
			"public class Test extends Super {\n"+
			"    public void setFoo() {}\n" +
			"    public static void main(String[] args) {\n"+ 
			"        try {\n"+ 
			"            Method m = Test.class.getMethod(\"setFoo\", String.class);\n"+
			"            Annotation a = m.getAnnotation(Anno.class);\n"+ 
			"            System.out.println(\"Annotation was \" + (a == null ? \"not \" : \"\") +\n"+ 
			"\"found\");\n"+ 
			"        } catch (Exception e) {\n"+ 
			"            e.printStackTrace();\n"+ 
			"        }\n"+ 
			"    }\n"+ 
			"}\n"+ 
			"\n"+ 
			"class Super {\n"+ 
			"    @Anno\n"+ 
			"    public void setFoo(String foo) {}\n"+ 
			"}\n"+ 
			"\n"+ 
			"@Retention(RetentionPolicy.RUNTIME)\n"+ 
			"@interface Anno {\n"+ 
			"\n"+ 
			"}\n"
		},
		this.complianceLevel <= ClassFileConstants.JDK1_5 ? "Annotation was found" : "Annotation was not found");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322001
public void test209() {
	this.runNegativeTest(
		new String[] {
			"Concrete.java",
			"class Bar extends Zork {}\n" +
			"class Foo {}\n" +
			"\n" +
			"interface Function<F, T> {\n" +
			"    T apply(F f);\n" +
			"}\n" +
			"interface Predicate<T> {\n" +
			"    boolean apply(T t);\n" +
			"}\n" +
			"\n" +
			"public class Concrete implements Predicate<Foo>, Function<Bar, Boolean> {\n" +
			"    public Boolean apply(Bar two) {\n" +
			"        return null;\n" +
			"    }\n" +
			"    public boolean apply(Foo foo) {\n" +
			"        return false;\n" +
			"    }\n" +

			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Concrete.java (at line 1)\n" + 
		"	class Bar extends Zork {}\n" + 
		"	                  ^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321548
public void test210() {
	this.runNegativeTest(
		new String[] {
			"ErasureTest.java",
			"interface Interface1<T> {\n" +
			"    public void hello(T greeting);\n" +
			"}\n" +
			"interface Interface2<T> {\n" +
			"    public int hello(T greeting);\n" +
			"}\n" +
			"public class ErasureTest extends Zork implements Interface1<String>, Interface2<Double> {\n" +
			"    public void hello(String greeting) { }\n" +
			"    public int hello(Double greeting) {\n" +
			"        return 0;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in ErasureTest.java (at line 7)\n" + 
		"	public class ErasureTest extends Zork implements Interface1<String>, Interface2<Double> {\n" + 
		"	                                 ^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83162
public void test211() {
	this.runNegativeTest(
		new String[] {
			"SomeClass.java",
			"interface Equivalent<T> {\n" +
			"	boolean equalTo(T other);\n" +
			"}\n" +
			"\n" +
			"interface EqualityComparable<T> {\n" +
			"	boolean equalTo(T other);\n" +
			"}\n" +
			"\n" +
			"public class SomeClass implements Equivalent<String>, EqualityComparable<Integer> {\n" +
			"	public boolean equalTo(String other) {\n" +
			"		return true;\n" +
			"	}\n" +
			"	public boolean equalTo(Integer other) {\n" +
			"		return true;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in SomeClass.java (at line 9)\n" + 
		"	public class SomeClass implements Equivalent<String>, EqualityComparable<Integer> {\n" + 
		"	             ^^^^^^^^^\n" + 
		"Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(T) of type Equivalent<T> but does not override it\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=323693
public void test212() {
	this.runNegativeTest(
		new String[] {
			"Derived.java",
			"class Base<T> {\n" +
			"    T foo(T x) {\n" +
			"        return x;\n" +
			"    }\n" +
			"}\n" +
			"interface Interface<T>{\n" +
			"    T foo(T x);\n" +
			"}\n" +
			"public class Derived extends Base<String> implements Interface<Integer> {\n" +
			"    public Integer foo(Integer x) {\n" +
			"        return x;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Derived.java (at line 9)\n" + 
		"	public class Derived extends Base<String> implements Interface<Integer> {\n" + 
		"	             ^^^^^^^\n" + 
		"Name clash: The method foo(T) of type Interface<T> has the same erasure as foo(T) of type Base<T> but does not override it\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850 
public void test213() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Y.java",
			"public abstract class Y implements I<Y> {\n" + 
			"		public final Y foo(Object o, J<Y> j) {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	public final void bar(Object o, J<Y> j, Y y) {\n" + 
			"	}\n" + 
			"}",
			"I.java",
			"public interface I<S> {\n" + 
			"	public S foo(Object o, J<S> j);\n" + 
			"	public void bar(Object o, J<S> j, S s);\n" + 
			"}",
			"J.java",
			"public interface J<S> {}"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);
	
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public Object foo() {\n" + 
			"		return new Y() {};\n" + 
			"	}\n" + 
			"}"
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850 
public void test213a() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Y.java",
			"public abstract class Y implements I<Y> {\n" + 
			"		public final Y foo(Object o, J<Y, I<Y>> j) {\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"	public final void bar(Object o, J<Y, String> j, Y y) {\n" + 
			"	}\n" + 
			"}",
			"I.java",
			"public interface I<S> {\n" + 
			"	public S foo(Object o, J<S, I<S>> j);\n" + 
			"	public void bar(Object o, J<S, String> j, S s);\n" + 
			"}",
			"J.java",
			"public interface J<S, T> {}"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);
	
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public Object foo() {\n" + 
			"		return new Y() {};\n" + 
			"	}\n" + 
			"}"
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850
public void test213b() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"ConsoleSession.java",
			"public abstract class ConsoleSession implements ServiceFactory<Object> {\n" +
			"	public final void ungetService(Bundle bundle, ServiceRegistration<Object> registration, Object service) {\n" +
			"	}\n" +
			"	\n" +
			"	public final Object getService(Bundle bundle, ServiceRegistration<Object> registration) {\n" +
			"		return this;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class Bundle {}\n" +
			"\n" +
			"interface ServiceFactory<S> {\n" +
			"	public void ungetService(Bundle b, ServiceRegistration<S> registration, S service);\n" +
			"	public S getService(Bundle bundle, ServiceRegistration<S> registration);\n" +
			"}\n" +
			"\n" +
			"interface ServiceRegistration<T> {\n" +
			"\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
					"OSGiConsole.java",
					"public class OSGiConsole {\n" +
					"	OSGiConsole() {\n" +
					"		new ConsoleSession() {\n" +
					"		};\n" +
					"	}\n" +
					"}\n",
			},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850
public void test213c() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"ConsoleSession.java",
			"public abstract class ConsoleSession implements ServiceFactory<ConsoleSession> {\n" +
			"	public final void ungetService(Bundle bundle, ServiceRegistration<ConsoleSession> registration, ConsoleSession service) {\n" +
			"	}\n" +
			"	\n" +
			"	public final ConsoleSession getService(Bundle bundle, ServiceRegistration<ConsoleSession> registration) {\n" +
			"		return this;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class Bundle {}\n" +
			"\n" +
			"interface ServiceFactory<S> {\n" +
			"	public void ungetService(Bundle b, ServiceRegistration<S> registration, S service);\n" +
			"	public S getService(Bundle bundle, ServiceRegistration<S> registration);\n" +
			"}\n" +
			"\n" +
			"interface ServiceRegistration<T> {\n" +
			"\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
					"OSGiConsole.java",
					"public class OSGiConsole {\n" +
					"	OSGiConsole() {\n" +
					"		new ConsoleSession() {\n" +
					"		};\n" +
					"	}\n" +
					"}\n",
			},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
public void test326354() {
	this.runConformTest(
			new String[] {
					"X.java",
					"public class X extends Y<I>  implements I {\n" +
					"    public static void main(String[] args) {\n" +
					"        ((I) new X()).foo(null);\n" +
					"    }\n" +
					"}\n" +
					"\n" +
					"interface I {\n" +
					"    public void foo(I i);\n" +
					"}\n" +
					" \n" +
					"abstract class Y<T> {\n" +
					"	   public void foo(T t) {}\n" +
					"}\n"
			},
			""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328827 
public void test328827() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Map.java",
			"public interface Map<K,V> {}\n",
			
			"EventProperties.java",
			"public class EventProperties implements Map<String, Object> {}\n",
			
			"Event.java",
			"public class Event {\n" +
			"    public Event(Map<String, ?> properties) {}\n" +
			"}"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);
	
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
				"Map.java",
				"public interface Map {}\n",
				
				"X.java",
				"public class X {\n" +
				"    public void start() {\n" +
				"        Event event = new Event(new EventProperties());\n" + 
				"	}\n" +
				"}"
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=329584 
public void test329584() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"I.java",
			"public interface I {\n" +
			"	void foo(Object o[], Dictionary<Object, Object> dict);\n" +
			"}",
			"Dictionary.java",
			"public class Dictionary<U, V> {}\n",
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);
	
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements I {\n" +
			"	public void foo(Object o[], Dictionary dict) {}\n" +
			"}",
			"Dictionary.java",
			"public class Dictionary {}\n",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=329588 
public void test329588() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"	public O<?> foo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}",
			"O.java",
			"public class O<V> {}\n",
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);
	
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(A a) {\n" +
			"		O o = (O) a.foo();\n" + 
			"		System.out.println(o);\n" + 
			"	}\n" +
			"}",
			"O.java",
			"public class O {}\n",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330445 
public void test330445() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Y.java",
			"import java.util.Map;\n" + 
			"public class Y {\n" + 
			"	static void foo(Map<String, String> map) {\n" + 
			"	}\n" + 
			"}",
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);
	
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.Properties;\n" + 
			"public class X {\n" + 
			"    static void bar(Object[] args) {\n" + 
			"        Y.foo(new Properties());\n" + 
			"    }\n" + 
			"}",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330435 
public void test330435() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"	public static <T> B<T> asList(T... tab) {\n" + 
			"		return null;\n" + 
			"	}\n" +
			"}",
			"B.java",
			"public interface B<V> {\n" +
			"	<T> T[] toArray(T[] tab);\n" + 
			"}\n",
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);
	    
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	String[] foo(Object[] args) {\n" + 
			"		String[] a = A.asList(args).toArray(new String[0]);\n" + 
			"		return a;\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	String[] a = A.asList(args).toArray(new String[0]);\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Object[] to String[]\n" + 
		"----------\n",
		null,
		false,
		compilerOptions14);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330264 
public void test330264() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"BundleContext.java",
			"public interface BundleContext {\n" +
			"    <S> S getService(ServiceReference<S> reference);\n" +
			"}\n",
			"ServiceReference.java",
			"public interface ServiceReference<S> extends Comparable<Object> {}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);
	    
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runNegativeTest(
		new String[] {
			"Activator.java",
			"public class Activator  {\n" +
			"    public void start(BundleContext context, ServiceReference ref) {\n" +
			"        Runnable r = context.getService(ref);\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in Activator.java (at line 3)\n" + 
		"	Runnable r = context.getService(ref);\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Object to Runnable\n" + 
		"----------\n",
		null,
		false,
		compilerOptions14);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446
public void test331446() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Test.java",
			"import java.util.Comparator;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"	public static <T> void assertEquals(String message,\n" + 
			"			Comparator<T> comparator, List<T> expected, List<T> actual) {\n" + 
			"		if (expected.size() != actual.size()) {\n" + 
			"			//failNotEquals(message, expected, actual);\n" + 
			"		}\n" + 
			"		for (int i = 0, l = expected.size(); i < l; i++) {\n" + 
			"			assertEquals(message, comparator, expected.get(i), actual.get(i));\n" + 
			"		}\n" + 
			"	}\n" + 
			"	public static <T> void assertEquals(String message,\n" + 
			"			Comparator<T> comparator, T expected, T actual) {\n" + 
			"		if (comparator.compare(expected, actual) == 0) {\n" + 
			"			return;\n" + 
			"		}\n" + 
			"		//failNotEquals(message, expected, actual);\n" + 
			"	}\n" + 
			"}\n" + 
			""
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Comparator;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void testAmbiguity() {\n" + 
			"		Comparator comparator = new Comparator() {\n" + 
			"			\n" + 
			"			public int compare(Object o1, Object o2) {\n" + 
			"				return 0;\n" + 
			"			}\n" + 
			"		};\n" + 
			"		Test.assertEquals(\"Test\", comparator, new ArrayList(), new ArrayList());\n" + 
			"	}\n" + 
			"}\n" + 
			"",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446
public void test331446a() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_4);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
	this.runConformTest(
		new String[] {
			"Test.java",
			"import java.util.Comparator;\n" + 
			"import java.util.List;\n" + 
			"\n" + 
			"public class Test {\n" + 
			"	public static  void assertEquals(String message,\n" + 
			"			Comparator comparator, List expected, List actual) {\n" + 
			"		if (expected.size() != actual.size()) {\n" + 
			"			//failNotEquals(message, expected, actual);\n" + 
			"		}\n" + 
			"		for (int i = 0, l = expected.size(); i < l; i++) {\n" + 
			"			assertEquals(message, comparator, expected.get(i), actual.get(i));\n" + 
			"		}\n" + 
			"	}\n" + 
			"	public static void assertEquals(String message,\n" + 
			"			Comparator comparator, Object expected, Object actual) {\n" + 
			"		if (comparator.compare(expected, actual) == 0) {\n" + 
			"			return;\n" + 
			"		}\n" + 
			"		//failNotEquals(message, expected, actual);\n" + 
			"	}\n" + 
			"}\n" + 
			""
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"import java.util.Comparator;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void testAmbiguity() {\n" + 
			"		Comparator comparator = new Comparator() {\n" + 
			"			\n" + 
			"			public int compare(Object o1, Object o2) {\n" + 
			"				return 0;\n" + 
			"			}\n" + 
			"		};\n" + 
			"		Test.assertEquals(\"Test\", comparator, new ArrayList(), new ArrayList());\n" + 
			"	}\n" + 
			"}\n" + 
			"",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446 (all 1.4)
public void test331446b() {
	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
	this.runConformTest(
		new String[] {
			"Project.java",
			"class List{}\n" +
			"public class Project {\n" +
			"    static  void foo(List expected) {}\n" +
			"    public static void foo(Object expected) {}\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions14,
		null);

	this.runConformTest(
		new String[] {
			"Client.java",
			"public class Client {\n" +
			"    Client(List l) {\n" +
			"        Project.foo(l);\n" +
			"    }\n" +
			"}\n"
			},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446 (1.4/1.5 mix)
public void test331446c() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Project.java",
			"class List<T> {}\n" +
			"public class Project {\n" +
			"    static <T> void foo(List<T> expected) {}\n" +
			"    public static <T> void foo(T expected) {}\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"Client.java",
			"public class Client {\n" +
			"    Client(List l) {\n" +
			"        Project.foo(l);\n" +
			"    }\n" +
			"}\n"
			},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446 (all 1.5)
public void test331446d() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Project.java",
			"class List<T> {}\n" +
			"public class Project {\n" +
			"    static <T> void foo(List<T> expected) {}\n" +
			"    public static <T> void foo(T expected) {}\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);
	this.runConformTest(
		new String[] {
			"Client.java",
			"public class Client {\n" +
			"    Client(List l) {\n" +
			"        Project.foo(l);\n" +
			"    }\n" +
			"}\n"
			},
		"",
		null,
		false,
		null,
		compilerOptions15,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446
public void test1415Mix() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Abstract.java",
			"abstract class Generic<T> {\n" +
			"	abstract void foo(T t);\n" +
			"}\n" +
			"public abstract class Abstract extends Generic<String> {\n" +
			"}"
			},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runNegativeTest(
		new String[] {
			"Concrete.java",
			"public class Concrete extends Abstract {\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in Concrete.java (at line 1)\n" + 
		"	public class Concrete extends Abstract {\n" + 
		"	             ^^^^^^^^\n" + 
		"The type Concrete must implement the inherited abstract method Generic<String>.foo(String)\n" + 
		"----------\n",
		null,
		false,
		compilerOptions14);	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=331446
public void test1415Mix2() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"Abstract.java",
			"abstract class Generic<T> {\n" +
			"	abstract void foo(T t);\n" +
			"}\n" +
			"public abstract class Abstract extends Generic<String> {\n" +
			"}"
			},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
				"Concrete.java",
				"public class Concrete extends Abstract {\n" +
				"    void foo(String s) {}\n" +
				"}",
		},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=332744 (all 1.5+)
public void test332744() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"EList.java",
			"import java.util.List;\n" +
			"public interface EList<E> extends List<E> {\n" +
			"}\n",
			"FeatureMap.java",
			"public interface FeatureMap extends EList<FeatureMap.Entry> {\n" +
			"    interface Entry {\n" +
			"    }\n" +
			"}\n",
			"InternalEList.java",
			"public interface InternalEList<E> extends EList<E> {\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	this.runConformTest(
		new String[] {
			"Client.java",
			"public class Client {\n" +
			"    Client(FeatureMap fm) {\n" +
			"		InternalEList e = (InternalEList) fm;\n" +
			"	}\n" +
			"}\n"
			},
		"",
		null,
		false,
		null,
		compilerOptions15,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=332744 (1.4/1.5 mix)
public void test332744b() {
	Map compilerOptions15 = getCompilerOptions();
	compilerOptions15.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
	compilerOptions15.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"EList.java",
			"import java.util.List;\n" +
			"public interface EList<E> extends List<E> {\n" +
			"}\n",
			"FeatureMap.java",
			"public interface FeatureMap extends EList<FeatureMap.Entry> {\n" +
			"    interface Entry {\n" +
			"    }\n" +
			"}\n",
			"InternalEList.java",
			"public interface InternalEList<E> extends EList<E> {\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		compilerOptions15,
		null);

	Map compilerOptions14 = getCompilerOptions();
	compilerOptions14.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
	compilerOptions14.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
	compilerOptions14.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
	compilerOptions14.put(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"Client.java",
			"public class Client {\n" +
			"    Client(FeatureMap fm) {\n" +
			"		InternalEList e = (InternalEList) fm;\n" +
			"	}\n" +
			"}\n"
			},
		"",
		null,
		false,
		null,
		compilerOptions14,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=339447
public void test339447() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements Cloneable {\n" + 
			"	public synchronized X clone() {\n" + 
			"		return this;\n" + 
			"	}\n" + 
			"}", // =================
		},
		"");
	// 	ensure bridge methods have target method modifiers, and inherited thrown exceptions
	String expectedOutput =
			"  public bridge synthetic java.lang.Object clone() throws java.lang.CloneNotSupportedException;";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322740
public void test322740() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Base  {\n" +
			"    boolean equalTo(Object other) {return false;}\n" +
			"}\n" +
			"interface EqualityComparable<T> {\n" +
			"    boolean equalTo(T other);\n" +
			"}\n" +
			"public class X extends Base implements EqualityComparable<String> {\n" +
			"    public boolean equalTo(String other) {\n" +
			"        return true;\n" +
			"    }\n" +
			"    public static void main(String args[]) {\n" +
			"        new X().equalTo(args);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	public class X extends Base implements EqualityComparable<String> {\n" + 
		"	             ^\n" + 
		"Name clash: The method equalTo(T) of type EqualityComparable<T> has the same erasure as equalTo(Object) of type Base but does not override it\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=334306
public void test334306() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> {}\n" +
			"interface I {\n" +
			"    void foo(X<Number> p);\n" +
			"}\n" +
			"interface J extends I {\n" +
			"    void foo(X<Integer> p);\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	void foo(X<Integer> p);\n" + 
		"	     ^^^^^^^^^^^^^^^^^\n" + 
		"Name clash: The method foo(X<Integer>) of type J has the same erasure as foo(X<Number>) of type I but does not override it\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=342819
public void test342819() throws Exception {
	this.runNegativeTest(
		new String[] {
			"TwoWayDTOAdapter.java",
			"public interface TwoWayDTOAdapter<A, B> extends DTOAdapter <A, B>{\n" +
			"    public A convert(B b);\n" +
			"}\n",
			"DTOAdapter.java",
			"public interface DTOAdapter<A, B> {\n" +
			"    public B convert(A a);\n" +
			"}\n",
			"TestAdapter.java",
			"public class TestAdapter implements TwoWayDTOAdapter<Long, Integer> {\n" +
			"    public Long convert(Integer b) {\n" +
			"        return null;\n" +
			"    }\n" +
			"    public Integer convert(Long a) {\n" +
			"        return null;\n" +
			"    }\n" +
			"}"
		},
		"----------\n" + 
		"1. ERROR in TwoWayDTOAdapter.java (at line 2)\n" + 
		"	public A convert(B b);\n" + 
		"	         ^^^^^^^^^^^^\n" + 
		"Name clash: The method convert(B) of type TwoWayDTOAdapter<A,B> has the same erasure as convert(A) of type DTOAdapter<A,B> but does not override it\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"    void f(String s) {}\n" +
			"}\n" +
			"class B<T> extends A<T> {\n" +
			"    void f(T t) {}\n" +
			"}\n" +
			"public class X extends B<String> {\n" +
			"    void foo(X x) {\n" +
			"        x.f(\"\");\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	public class X extends B<String> {\n" + 
		"	             ^\n" + 
		"Duplicate methods named f with the parameters (T) and (String) are inherited from the types B<String> and A<String>\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	x.f(\"\");\n" + 
		"	  ^\n" + 
		"The method f(String) is ambiguous for the type X\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029b() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface A<T> {\n" +
			"    void f(String s);\n" +
			"}\n" +
			"interface B<T> extends A<T> {\n" +
			"    void f(T t);\n" +
			"}\n" +
			"public class X implements B<String> {\n" +
			"    public void f(String t) {\n" +
			"        Zork z;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	Zork z;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029c() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"    void f(String s) {}\n" +
			"}\n" +
			"interface B<T> {\n" +
			"    void f(T t);\n" +
			"}\n" +
			"public class X extends A<String> implements B<String> {\n" +
			"    public void f(String t) {\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 8)\n" + 
		"	public void f(String t) {\n" + 
		"	            ^^^^^^^^^^^\n" + 
		"The method f(String) of type X should be tagged with @Override since it actually overrides a superclass method\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029d() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"    void f(T s) {}\n" +
			"}\n" +
			"interface B<T> {\n" +
			"    void f(String t);\n" +
			"}\n" +
			"public class X extends A<String> implements B<String> {\n" +
			"    public void f(String t) {\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 8)\n" + 
		"	public void f(String t) {\n" + 
		"	            ^^^^^^^^^^^\n" + 
		"The method f(String) of type X should be tagged with @Override since it actually overrides a superclass method\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029e() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"    void f(String s) {}\n" +
			"}\n" +
			"class B<T> extends A<T> {\n" +
			"    void f(T t) {}\n" +
			"}\n" +
			"public class X extends B<String> {\n" +
			"    	void f(String s) {\n" +
			"       }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 8)\n" + 
		"	void f(String s) {\n" + 
		"	     ^^^^^^^^^^^\n" + 
		"The method f(String) of type X should be tagged with @Override since it actually overrides a superclass method\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346029
public void test346029f() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class A<T> {\n" +
			"    void f(String s) {}\n" +
			"}\n" +
			"class B<T> extends A<T> {\n" +
			"    void f(T t) {}\n" +
			"}\n" +
			"public class X extends B<String> {\n" +
			"	void f(String s) {\n" +
			"		super.f(s);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 8)\n" + 
		"	void f(String s) {\n" + 
		"	     ^^^^^^^^^^^\n" + 
		"The method f(String) of type X should be tagged with @Override since it actually overrides a superclass method\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	super.f(s);\n" + 
		"	      ^\n" + 
		"The method f(String) is ambiguous for the type B<String>\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353089
public void test353089() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"interface A {\n" +
			"int get(List<String> l);\n" +
			"}\n" +
			"interface B  {\n" +
			"int get(List<Integer> l);\n" +
			"}\n" +
			"interface C  extends A, B { \n" +
			"int get(List l);      // name clash error here\n" +
         "}\n" +
			"public class X {\n" +
         "    public static void main(String [] args) {\n" +
			"        System.out.println(\"Built OK\");\n" +
         "    }\n" +
			"}"
		},
		"Built OK");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=353089
public void test353089b() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"interface I {\n" +
			"    void a(List<String> i, List<String> j);\n" +
			"    void b(List<String> i, List<String> j);\n" +
			"    void c(List i, List<String> j);\n" +
			"}\n" +
			"interface X extends I {\n" +
			"    public void a(List<String> i, List j);\n" +
			"    public void b(List i, List j);\n" +
			"    public void c(List i, List j);\n" +
			"    public void d(Zork z);\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	void c(List i, List<String> j);\n" + 
		"	       ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	public void a(List<String> i, List j);\n" + 
		"	            ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Name clash: The method a(List<String>, List) of type X has the same erasure as a(List<String>, List<String>) of type I but does not override it\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 8)\n" + 
		"	public void a(List<String> i, List j);\n" + 
		"	                              ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 9)\n" + 
		"	public void b(List i, List j);\n" + 
		"	              ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 9)\n" + 
		"	public void b(List i, List j);\n" + 
		"	                      ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"6. WARNING in X.java (at line 10)\n" + 
		"	public void c(List i, List j);\n" + 
		"	              ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"7. WARNING in X.java (at line 10)\n" + 
		"	public void c(List i, List j);\n" + 
		"	                      ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"8. ERROR in X.java (at line 11)\n" + 
		"	public void d(Zork z);\n" + 
		"	              ^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=353089
public void test353089c() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"interface IFtest {\n" +
			"    public void doTest(Integer i, List<String> pList, List<String> pList2);\n" +
			"}\n" +
			"interface Impl extends IFtest {\n" +
			"    public void doTest(Integer i, List<String> iList, List iList2);\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	public void doTest(Integer i, List<String> iList, List iList2);\n" + 
		"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Name clash: The method doTest(Integer, List<String>, List) of type Impl has the same erasure as doTest(Integer, List<String>, List<String>) of type IFtest but does not override it\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 6)\n" + 
		"	public void doTest(Integer i, List<String> iList, List iList2);\n" + 
		"	                                                  ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	public <T extends List> T foo() { return null; }\n" + 
			"	                  ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	public <T extends List> T foo() { return null; }\n" + 
			"	                          ^^^^^\n" + 
			"Duplicate method foo() in type X\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 5)\n" + 
			"	public <T extends Set> T foo() { return null; }\n" + 
			"	                  ^^^\n" + 
			"Set is a raw type. References to generic type Set<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 5)\n" + 
			"	public <T extends Set> T foo() { return null; }\n" + 
			"	                         ^^^^^\n" + 
			"Duplicate method foo() in type X\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 6)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n":
				"----------\n" + 
				"1. WARNING in X.java (at line 4)\n" + 
				"	public <T extends List> T foo() { return null; }\n" + 
				"	                  ^^^^\n" + 
				"List is a raw type. References to generic type List<E> should be parameterized\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	public <T extends List> T foo() { return null; }\n" + 
				"	                          ^^^^^\n" + 
				"Duplicate method foo() in type X\n" + 
				"----------\n" + 
				"3. WARNING in X.java (at line 5)\n" + 
				"	public <T extends Set> T foo() { return null; }\n" + 
				"	                  ^^^\n" + 
				"Set is a raw type. References to generic type Set<E> should be parameterized\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	public <T extends Set> T foo() { return null; }\n" + 
				"	                         ^^^^^\n" + 
				"Duplicate method foo() in type X\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 6)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.Set;\n" +
			"class X {\n" +
			"    public <T extends List> T foo() { return null; }\n" +
			"	 public <T extends Set> T foo() { return null; }\n" +
			"	 Zork z;\n" +
			"}\n"
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719a() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	public Integer same(List<Integer> a) { return null; }\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method same(List<Integer>) has the same erasure same(List<E>) as another method in type X\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	public String same(List<String> b) { return null; }\n" + 
			"	              ^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method same(List<String>) has the same erasure same(List<E>) as another method in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n":
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	public Integer same(List<Integer> a) { return null; }\n" + 
				"	               ^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method same(List<Integer>) has the same erasure same(List<E>) as another method in type X\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	public String same(List<String> b) { return null; }\n" + 
				"	              ^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method same(List<String>) has the same erasure same(List<E>) as another method in type X\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.Set;\n" +
			"class X {\n" +
			"    public Integer same(List<Integer> a) { return null; }\n" +
			"	 public String same(List<String> b) { return null; }\n" +
			"	 Zork z;\n" +
			"}\n"
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719b() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public static String doIt(final List<String> arg) { return null; }\n" + 
			"	                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method doIt(List<String>) has the same erasure doIt(List<E>) as another method in type X\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	public static CharSequence doIt(final List<CharSequence> arg) { return null; }\n" + 
			"	                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method doIt(List<CharSequence>) has the same erasure doIt(List<E>) as another method in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n":
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	public static String doIt(final List<String> arg) { return null; }\n" + 
				"	                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method doIt(List<String>) has the same erasure doIt(List<E>) as another method in type X\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	public static CharSequence doIt(final List<CharSequence> arg) { return null; }\n" + 
				"	                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method doIt(List<CharSequence>) has the same erasure doIt(List<E>) as another method in type X\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"class X {\n" +
			"    public static String doIt(final List<String> arg) { return null; }\n" +
			"	 public static CharSequence doIt(final List<CharSequence> arg) { return null; }\n" +
			"	 Zork z;\n" +
			"}\n"
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719c() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	protected static <T extends String> T same(Collection<? extends T> p_col) { return null; }\n" + 
			"	                            ^^^^^^\n" + 
			"The type parameter T should not be bounded by the final type String. Final types cannot be further extended\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 3)\n" + 
			"	protected static <T extends String> T same(Collection<? extends T> p_col) { return null; }\n" + 
			"	                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method same(Collection<? extends T>) has the same erasure same(Collection<E>) as another method in type X\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 4)\n" + 
			"	protected static <T extends Number> T same(Collection<? extends T> p_col) { return null; }\n" + 
			"	                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method same(Collection<? extends T>) has the same erasure same(Collection<E>) as another method in type X\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n":
				"----------\n" + 
				"1. WARNING in X.java (at line 3)\n" + 
				"	protected static <T extends String> T same(Collection<? extends T> p_col) { return null; }\n" + 
				"	                            ^^^^^^\n" + 
				"The type parameter T should not be bounded by the final type String. Final types cannot be further extended\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	protected static <T extends String> T same(Collection<? extends T> p_col) { return null; }\n" + 
				"	                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method same(Collection<? extends T>) has the same erasure same(Collection<E>) as another method in type X\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	protected static <T extends Number> T same(Collection<? extends T> p_col) { return null; }\n" + 
				"	                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method same(Collection<? extends T>) has the same erasure same(Collection<E>) as another method in type X\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Collection;\n" +
			"class X {\n" +
			"    protected static <T extends String> T same(Collection<? extends T> p_col) { return null; }\n" +
			"	 protected static <T extends Number> T same(Collection<? extends T> p_col) { return null; }\n" +
			"	 Zork z;\n" +
			"}\n"
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719d() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public static boolean foo(List<String> x) { return true; }\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^\n" + 
			"Method foo(List<String>) has the same erasure foo(List<E>) as another method in type X\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	public static int foo(List<Integer> x) { return 2; }\n" + 
			"	                  ^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method foo(List<Integer>) has the same erasure foo(List<E>) as another method in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n":
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	public static boolean foo(List<String> x) { return true; }\n" + 
				"	                      ^^^^^^^^^^^^^^^^^^^\n" + 
				"Method foo(List<String>) has the same erasure foo(List<E>) as another method in type X\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	public static int foo(List<Integer> x) { return 2; }\n" + 
				"	                  ^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method foo(List<Integer>) has the same erasure foo(List<E>) as another method in type X\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"class X {\n" +
			"    public static boolean foo(List<String> x) { return true; }\n" +
			"	 public static int foo(List<Integer> x) { return 2; }\n" +
			"	 Zork z;\n" +
			"}\n"
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719e() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public String getFirst (ArrayList<String> ss) { return ss.get(0); }\n" + 
			"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method getFirst(ArrayList<String>) has the same erasure getFirst(ArrayList<E>) as another method in type X\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	public Integer getFirst (ArrayList<Integer> ss) { return ss.get(0); }\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method getFirst(ArrayList<Integer>) has the same erasure getFirst(ArrayList<E>) as another method in type X\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n":
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	public String getFirst (ArrayList<String> ss) { return ss.get(0); }\n" + 
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method getFirst(ArrayList<String>) has the same erasure getFirst(ArrayList<E>) as another method in type X\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	public Integer getFirst (ArrayList<Integer> ss) { return ss.get(0); }\n" + 
				"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method getFirst(ArrayList<Integer>) has the same erasure getFirst(ArrayList<E>) as another method in type X\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"class X {\n" +
			"    public String getFirst (ArrayList<String> ss) { return ss.get(0); }\n" +
			"	 public Integer getFirst (ArrayList<Integer> ss) { return ss.get(0); }\n" +
			"	 Zork z;\n" +
			"}\n"
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719f() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public static <R extends Object> X<R> forAccountSet(List list) { return null; }\n" + 
			"	                                      ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method forAccountSet(List) has the same erasure forAccountSet(List<E>) as another method in type X<Z>\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 3)\n" + 
			"	public static <R extends Object> X<R> forAccountSet(List list) { return null; }\n" + 
			"	                                                    ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 4)\n" + 
			"	public static <R extends Object> ChildX<R> forAccountSet(List<R> list) { return null; }\n" + 
			"	                                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method forAccountSet(List<R>) has the same erasure forAccountSet(List<E>) as another method in type X<Z>\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 5)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n":
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	public static <R extends Object> X<R> forAccountSet(List list) { return null; }\n" + 
				"	                                      ^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method forAccountSet(List) has the same erasure forAccountSet(List<E>) as another method in type X<Z>\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	public static <R extends Object> X<R> forAccountSet(List list) { return null; }\n" + 
				"	                                                    ^^^^\n" + 
				"List is a raw type. References to generic type List<E> should be parameterized\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	public static <R extends Object> ChildX<R> forAccountSet(List<R> list) { return null; }\n" + 
				"	                                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method forAccountSet(List<R>) has the same erasure forAccountSet(List<E>) as another method in type X<Z>\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"class X<Z> {\n" +
			"    public static <R extends Object> X<R> forAccountSet(List list) { return null; }\n" +
			"	 public static <R extends Object> ChildX<R> forAccountSet(List<R> list) { return null; }\n" +
			"	 Zork z;\n" +
			"}\n" +
			"class ChildX<Z> extends X<Z>{}\n"
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719g() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	public static int[] doIt(Collection<int[]> col) { return new int[1]; }\n" + 
			"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method doIt(Collection<int[]>) has the same erasure doIt(Collection<E>) as another method in type X<Z>\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	public static int[][] doIt(Collection<int[][]> col) { return new int[0][0]; }\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method doIt(Collection<int[][]>) has the same erasure doIt(Collection<E>) as another method in type X<Z>\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 5)\n" + 
			"	public int[] doIt2(Collection<int[]> col) { return new int[0]; }\n" + 
			"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method doIt2(Collection<int[]>) has the same erasure doIt2(Collection<E>) as another method in type X<Z>\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 6)\n" + 
			"	public int[][] doIt2(Collection<int[][]> col) { return new int[0][0]; }\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method doIt2(Collection<int[][]>) has the same erasure doIt2(Collection<E>) as another method in type X<Z>\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 7)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n":
				"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	public static int[] doIt(Collection<int[]> col) { return new int[1]; }\n" + 
				"	                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method doIt(Collection<int[]>) has the same erasure doIt(Collection<E>) as another method in type X<Z>\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 4)\n" + 
				"	public static int[][] doIt(Collection<int[][]> col) { return new int[0][0]; }\n" + 
				"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method doIt(Collection<int[][]>) has the same erasure doIt(Collection<E>) as another method in type X<Z>\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 5)\n" + 
				"	public int[] doIt2(Collection<int[]> col) { return new int[0]; }\n" + 
				"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method doIt2(Collection<int[]>) has the same erasure doIt2(Collection<E>) as another method in type X<Z>\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 6)\n" + 
				"	public int[][] doIt2(Collection<int[][]> col) { return new int[0][0]; }\n" + 
				"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Method doIt2(Collection<int[][]>) has the same erasure doIt2(Collection<E>) as another method in type X<Z>\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 7)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Collection;\n" +
			"class X<Z> {\n" +
			"    public static int[] doIt(Collection<int[]> col) { return new int[1]; }\n" +
			"	 public static int[][] doIt(Collection<int[][]> col) { return new int[0][0]; }\n" +
			"	 public int[] doIt2(Collection<int[]> col) { return new int[0]; }\n" +
			"	 public int[][] doIt2(Collection<int[][]> col) { return new int[0][0]; }\n" +
			"	 Zork z;\n" +
			"}\n" +
			"class ChildX<Z> extends X<Z>{}\n"
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
public void testBug317719h() throws Exception {
	String output = this.complianceLevel == ClassFileConstants.JDK1_6 ?
			"----------\n" + 
			"1. WARNING in Test.java (at line 3)\n" + 
			"	public class Test<Key, Value> extends HashMap<Key, Collection<Value>> {\n" + 
			"	             ^^^^\n" + 
			"The serializable class Test does not declare a static final serialVersionUID field of type long\n" + 
			"----------\n" + 
			"2. WARNING in Test.java (at line 4)\n" + 
			"	public Collection<Value> put(Key k, Value v) { return null; }\n" + 
			"	                         ^^^^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method put(Key, Value) of type Test<Key,Value> has the same erasure as put(K, V) of type HashMap<K,V> but does not override it\n" + 
			"----------\n" + 
			"3. WARNING in Test.java (at line 5)\n" + 
			"	public Collection<Value> get(Key k) { return null; }\n" + 
			"	                         ^^^^^^^^^^\n" + 
			"Name clash: The method get(Key) of type Test<Key,Value> has the same erasure as get(Object) of type HashMap<K,V> but does not override it\n" + 
			"----------\n" + 
			"4. ERROR in Test.java (at line 6)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n":
				"----------\n" + 
				"1. WARNING in Test.java (at line 3)\n" + 
				"	public class Test<Key, Value> extends HashMap<Key, Collection<Value>> {\n" + 
				"	             ^^^^\n" + 
				"The serializable class Test does not declare a static final serialVersionUID field of type long\n" + 
				"----------\n" + 
				"2. ERROR in Test.java (at line 4)\n" + 
				"	public Collection<Value> put(Key k, Value v) { return null; }\n" + 
				"	                         ^^^^^^^^^^^^^^^^^^^\n" + 
				"Name clash: The method put(Key, Value) of type Test<Key,Value> has the same erasure as put(K, V) of type HashMap<K,V> but does not override it\n" + 
				"----------\n" + 
				"3. ERROR in Test.java (at line 5)\n" + 
				"	public Collection<Value> get(Key k) { return null; }\n" + 
				"	                         ^^^^^^^^^^\n" + 
				"Name clash: The method get(Key) of type Test<Key,Value> has the same erasure as get(Object) of type HashMap<K,V> but does not override it\n" + 
				"----------\n" + 
				"4. ERROR in Test.java (at line 6)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"Test.java",
			"import java.util.Collection;\n" +
			"import java.util.HashMap;\n" +
			"public class Test<Key, Value> extends HashMap<Key, Collection<Value>> {\n" +
			"    public Collection<Value> put(Key k, Value v) { return null; }\n" +
			"	 public Collection<Value> get(Key k) { return null; }\n" +
			"	 Zork z;\n" +
			"}\n"
		},
		output);
}
public void test345949a() throws Exception {
	if (new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_7) return;
	this.runNegativeTest(
		new String[] {
			"Sub.java",
			"class A<T> {}\n" +
			"class Super {\n" +
			"    public static void foo(A<Number> p) {}\n" +
			"}\n" +
			"public class Sub extends Super {\n" +
			"	 public static void foo(A<Integer> p) {}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Sub.java (at line 6)\n" + 
		"	public static void foo(A<Integer> p) {}\n" + 
		"	                   ^^^^^^^^^^^^^^^^^\n" + 
		"Name clash: The method foo(A<Integer>) of type Sub has the same erasure as foo(A<Number>) of type Super but does not hide it\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=355838
public void testBug355838() throws Exception {
	String output = 		
			"----------\n" + 
			"1. ERROR in ErasureBug.java (at line 4)\n" + 
			"	public String output(List<String> integers) {\n" + 
			"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method output(List<String>) has the same erasure output(List<E>) as another method in type ErasureBug\n" + 
			"----------\n" + 
			"2. ERROR in ErasureBug.java (at line 7)\n" + 
			"	public String output(List doubles) {\n" + 
			"	              ^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method output(List) has the same erasure output(List<E>) as another method in type ErasureBug\n" + 
			"----------\n" + 
			"3. WARNING in ErasureBug.java (at line 7)\n" + 
			"	public String output(List doubles) {\n" + 
			"	                     ^^^^\n" + 
			"List is a raw type. References to generic type List<E> should be parameterized\n" + 
			"----------\n" + 
			"4. WARNING in ErasureBug.java (at line 10)\n" + 
			"	public static void main(String[] args) { new ErasureBug().output(new ArrayList()); }\n" + 
			"	                                                                 ^^^^^^^^^^^^^^^\n" + 
			"Type safety: The expression of type ArrayList needs unchecked conversion to conform to List<String>\n" + 
			"----------\n" + 
			"5. WARNING in ErasureBug.java (at line 10)\n" + 
			"	public static void main(String[] args) { new ErasureBug().output(new ArrayList()); }\n" + 
			"	                                                                     ^^^^^^^^^\n" + 
			"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" + 
			"----------\n";
	this.runNegativeTest(
		new String[] {
			"ErasureBug.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"public class ErasureBug {\n" +
			"    public String output(List<String> integers) {\n" +
			"		return \"1\";\n" +
			"	 }\n" +
			"    public String output(List doubles) {\n" +
			"		return \"2\";\n" +
			"	 }\n" +
			"	 public static void main(String[] args) { new ErasureBug().output(new ArrayList()); }\n" +
			"}\n"
		},
		output);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=288658, make sure a bridge method
// is generated when a public method is inherited from a non-public class into a
// public class if the non public class happens to be defined in a named package.
public void test288658() {
	this.runConformTest(
		new String[] {
			"pkg/Test.java",
			"package pkg;\n" +
			"import java.lang.annotation.Annotation;\n"+ 
			"import java.lang.annotation.Retention;\n"+ 
			"import java.lang.annotation.RetentionPolicy;\n"+ 
			"import java.lang.reflect.Method;\n"+ 
			"\n"+ 
			"public class Test extends Super {\n"+ 
			"    public static void main(String[] args) {\n"+ 
			"        try {\n"+ 
			"            Method m = Test.class.getMethod(\"setFoo\", String.class);\n"+
			"            Annotation a = m.getAnnotation(Anno.class);\n"+ 
			"            System.out.println(\"Annotation was \" + (a == null ? \"not \" : \"\") +\n"+ 
			"\"found\");\n"+ 
			"        } catch (Exception e) {\n"+ 
			"            e.printStackTrace();\n"+ 
			"        }\n"+ 
			"    }\n"+ 
			"}\n"+ 
			"\n"+ 
			"class Super {\n"+ 
			"    @Anno\n"+ 
			"    public void setFoo(String foo) {}\n"+ 
			"}\n"+ 
			"\n"+ 
			"@Retention(RetentionPolicy.RUNTIME)\n"+ 
			"@interface Anno {\n"+ 
			"\n"+ 
			"}\n"
		},
		this.complianceLevel <= ClassFileConstants.JDK1_5 ? "Annotation was found" : "Annotation was not found");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=288658, make sure a bridge method
// is generated when a public method is inherited from a non-public class into a
// public class if the non public class happens to be defined in a named package.
public void test288658a() {
	this.runConformTest(
		new String[] {
			"pkg/Test.java",
			"package pkg;\n" +
			"import java.lang.annotation.Annotation;\n"+ 
			"import java.lang.annotation.Retention;\n"+ 
			"import java.lang.annotation.RetentionPolicy;\n"+ 
			"import java.lang.reflect.Method;\n"+ 
			"\n"+ 
			"public class Test extends Super {\n"+
			"    public void setFoo() {}\n" +
			"    public static void main(String[] args) {\n"+ 
			"        try {\n"+ 
			"            Method m = Test.class.getMethod(\"setFoo\", String.class);\n"+
			"            Annotation a = m.getAnnotation(Anno.class);\n"+ 
			"            System.out.println(\"Annotation was \" + (a == null ? \"not \" : \"\") +\n"+ 
			"\"found\");\n"+ 
			"        } catch (Exception e) {\n"+ 
			"            e.printStackTrace();\n"+ 
			"        }\n"+ 
			"    }\n"+ 
			"}\n"+ 
			"\n"+ 
			"class Super {\n"+ 
			"    @Anno\n"+ 
			"    public void setFoo(String foo) {}\n"+ 
			"}\n"+ 
			"\n"+ 
			"@Retention(RetentionPolicy.RUNTIME)\n"+ 
			"@interface Anno {\n"+ 
			"\n"+ 
			"}\n"
		},
		this.complianceLevel <= ClassFileConstants.JDK1_5 ? "Annotation was found" : "Annotation was not found");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354229
public void test354229() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"interface A {\n" +
			"int get(List<String> l);\n" +
			"}\n" +
			"interface B  {\n" +
			"int get(List<Integer> l);\n" +
			"}\n" +
			"interface C  extends A, B { \n" +
			"//int get(List l);      // name clash error here\n" +
			"    Zork z;\n" +
			"}\n"
		},
		this.complianceLevel <= ClassFileConstants.JDK1_6 ?
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" :
					"----------\n" + 
					"1. ERROR in X.java (at line 8)\n" + 
					"	interface C  extends A, B { \n" + 
					"	          ^\n" + 
					"Name clash: The method get(List<Integer>) of type B has the same erasure as get(List<String>) of type A but does not override it\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 10)\n" + 
					"	Zork z;\n" + 
					"	^^^^\n" + 
					"Zork cannot be resolved to a type\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354229
public void test354229b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"interface A {\n" +
			"int get(List<String> l);\n" +
			"}\n" +
			"interface B  {\n" +
			"int get(List<Integer> l);\n" +
			"}\n" +
			"interface C  extends A, B { \n" +
			"    int get(List l);      // name clash error here\n" +
			"    Zork z;\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 9)\n" + 
		"	int get(List l);      // name clash error here\n" + 
		"	        ^^^^\n" + 
		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	Zork z;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354229
public void test354229c() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n" +
			"    <T> T e(Action<T> p);\n" +
			"}\n" +
			"interface Y {\n" +
			"    <S, T> S e(Action<S> t);\n" +
			"}\n" +
			"interface E extends X, Y {\n" +
			"}\n" +
			"class Action<T> {\n" +
			"    Zork z;\n" +
			"}\n"

		},
		this.complianceLevel < ClassFileConstants.JDK1_7 ? 
				"----------\n" + 
				"1. ERROR in X.java (at line 10)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" : 
					"----------\n" + 
					"1. ERROR in X.java (at line 7)\n" + 
					"	interface E extends X, Y {\n" + 
					"	          ^\n" + 
					"Name clash: The method e(Action<S>) of type Y has the same erasure as e(Action<T>) of type X but does not override it\n" + 
					"----------\n" + 
					"2. ERROR in X.java (at line 10)\n" + 
					"	Zork z;\n" + 
					"	^^^^\n" + 
					"Zork cannot be resolved to a type\n" + 
					"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354229
public void test354229d() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface X {\n" +
			"    <T> T e(Action<T> p);\n" +
			"    <S, T> S e(Action<S> t);\n" +
			"}\n" +
			"class Action<T> {\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	<T> T e(Action<T> p);\n" + 
		"	      ^^^^^^^^^^^^^^\n" + 
		"Method e(Action<T>) has the same erasure e(Action<T>) as another method in type X\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	<S, T> S e(Action<S> t);\n" + 
		"	         ^^^^^^^^^^^^^^\n" + 
		"Method e(Action<S>) has the same erasure e(Action<T>) as another method in type X\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/406928 - computation of inherited methods seems damaged (affecting @Overrides)
public void testBug406928() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6) return;
	this.runConformTest(
		new String[] {
			"TestPointcut.java",
			"interface MethodMatcher {\n"+
			"	boolean matches();\n"+
			"}\n"+
			"abstract class StaticMethodMatcher implements MethodMatcher { }\n"+
			"abstract class StaticMethodMatcherPointcut extends StaticMethodMatcher { }\n"+
			"\n"+
			"class TestPointcut extends StaticMethodMatcherPointcut {\n"+
			"	@Override\n"+
			"	public boolean matches() { return false; } \n"+
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/409473 - [compiler] JDT cannot compile against JRE 1.8
// Test failed when running on a JRE 1.8 b90
public void testBug409473() {
    this.runConformTest(
        new String[] {
            "Foo.java",
            "public abstract class Foo<E> implements java.util.List<E> { } "
        });
}
// https://bugs.eclipse.org/410325 - [1.7][compiler] Generified method override different between javac and eclipse compiler
public void testBug410325() {
	runConformTest(
		new String[] {
			"Main.java",
			"public class Main {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		F3 f3 = new F3();\n" + 
			"		SubSub sub = new SubSub();\n" + 
			"		sub.foo(f3);\n" + 
			"\n" + 
			"		Sub<F3> sub2 = sub;\n" + 
			"		Base<F3> base = sub;\n" + 
			"		sub2.foo(f3);\n" + 
			"		base.foo(f3);\n" + 
			"\n" + 
			"		F2 f2 = new F2();\n" + 
			"		sub2.foo(f2);\n" + 
			"	}\n" + 
			"\n" + 
			"	public static class F1 {\n" + 
			"	}\n" + 
			"\n" + 
			"	public static class F2 extends F1 {\n" + 
			"	}\n" + 
			"\n" + 
			"	public static class F3 extends F2 {\n" + 
			"		public void bar() {\n" + 
			"			System.out.println(\"bar in F3\");\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	public static abstract class Base<T extends F1> {\n" + 
			"		public abstract void foo(T bar);\n" + 
			"	}\n" + 
			"\n" + 
			"	public static abstract class Sub<T extends F2> extends Base<T> {\n" + 
			"		@Override\n" + 
			"		public void foo(F2 bar) {\n" + 
			"			System.out.println(getClass().getSimpleName() + \": F2 + \"\n" + 
			"					+ bar.getClass().getSimpleName());\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	public static class SubSub extends Sub<F3> {\n" + 
			"	}\n" + 
			"}"
		});
}
// https://bugs.eclipse.org/410325 - [1.7][compiler] Generified method override different between javac and eclipse compiler
// test from duplicate bug 411811
public void testBug411811() {
	runConformTest(
		new String[] {
			"FaultyType.java",
			"    class ParamType {}\n" + 
			"\n" + 
			"    abstract class AbstractType<T extends ParamType> {\n" + 
			"        public abstract void foo(T t);\n" + 
			"    }\n" + 
			"\n" + 
			"    abstract class SubAbstractType<T extends ParamType> extends AbstractType<T> {\n" + 
			"        @Override public void foo(ParamType t) {}\n" + 
			"    }\n" + 
			"\n" + 
			"    class SubParamType extends ParamType {}\n" + 
			"    \n" + 
			"public class FaultyType extends SubAbstractType<SubParamType> {}"
		});
}
// https://bugs.eclipse.org/410325 - [1.7][compiler] Generified method override different between javac and eclipse compiler
// test from duplicate bug 415600
public void testBug415600() {
	runConformTest(
		new String[] {
			"A.java",
			"import java.io.Reader;\n" + 
			"import java.io.StringReader;\n" + 
			"\n" + 
			"public abstract class A<E extends Reader> {\n" + 
			"	protected abstract void create(E element);\n" + 
			"}\n" + 
			"\n" + 
			"abstract class B<T extends Reader> extends A<T> {\n" + 
			"	public void create(Reader element) { }\n" + 
			"}\n" + 
			"\n" + 
			"class C extends B<StringReader> { }\n"
		});
}
}
