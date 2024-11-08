/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *								bug 388739 - [1.8][compiler] consider default methods when detecting whether a class needs to be declared abstract
 *								bug 399567 - [1.8] Different error message from the reference compiler
 *								bug 401796 - [1.8][compiler] don't treat default methods as overriding an independent inherited abstract method
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
  *								Bug 423505 - [1.8] Implement "18.5.4 More Specific Method Inference"
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AmbiguousMethodTest extends AbstractComparableTest {

	static {
//		TESTS_NAMES = new String [] { "test010a" };
	}
	public AmbiguousMethodTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return AmbiguousMethodTest.class;
	}

	@Override
	protected Map getCompilerOptions() {
		Map compilerOptions = super.getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		return compilerOptions;
	}
	public void test000() {
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test { public static void main(String[] args) { new B().foo(new C()); } }\n" +
				"class A { void foo(A a) {} }\n" +
				"class B extends A { void foo(B b) { System.out.println(1); } }\n" +
				"class C extends B {}"
			},
			"1"
		);
	}
	public void test000a() {
		this.runConformTest(
				new String[] {
					"Test.java",
					"public class Test { public static void main(String[] args) { new Subtype<String>().foo(1, \"works\"); } }\n" +
					"class Supertype<T1> { <U1> void foo(U1 u, T1 t) {} }\n" +
					"class Subtype <T2> extends Supertype<T2> { <U3> void foo(U3 u, T2 t) { System.out.println(t); } }"
				},
				"works"
			);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test001() {
		this.runConformTest(
			new String[] {
				"C.java",
				"public class C { public static void main(String[] args) { new B().m(\"works\"); } }\n" +
				"class B extends A { @Override <T extends Comparable<T>> void m(T t) { System.out.println(t); } }\n" +
				"abstract class A { abstract <T extends Comparable<T>> void m(T t); }"
			},
			"works"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test002() {
		if (this.complianceLevel < ClassFileConstants.JDK1_7) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static interface I1<E1> { void method(E1 o); }\n" +
				"	static interface I2<E2> { void method(E2 o); }\n" +
				"	static interface I3<E3, E4> extends I1<E3>, I2<E4> {}\n" +
				"	static class Class1 implements I3<String, String> {\n" +
				"		public void method(String o) { System.out.println(o); }\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		I3<String, String> i = new Class1();\n" +
				"		i.method(\"works\");\n" +
				"	}\n" +
				"}"
			},
			"works");
		} else {
			this.runNegativeTest(
					new String[] {
						"X.java",
						"public class X {\n" +
						"	static interface I1<E1> { void method(E1 o); }\n" +
						"	static interface I2<E2> { void method(E2 o); }\n" +
						"	static interface I3<E3, E4> extends I1<E3>, I2<E4> {}\n" +
						"	static class Class1 implements I3<String, String> {\n" +
						"		public void method(String o) { System.out.println(o); }\n" +
						"	}\n" +
						"	public static void main(String[] args) {\n" +
						"		I3<String, String> i = new Class1();\n" +
						"		i.method(\"works\");\n" +
						"	}\n" +
						"}"
					},
					"----------\n" +
					"1. ERROR in X.java (at line 4)\n" +
					"	static interface I3<E3, E4> extends I1<E3>, I2<E4> {}\n" +
					"	                 ^^\n" +
					"Name clash: The method method(E2) of type X.I2<E2> has the same erasure as method(E1) of type X.I1<E1> but does not override it\n" +
					"----------\n");
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
	public void test002a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static interface I1<E> { void method(E o); }\n" +
				"	static interface I2<E> { void method(E o); }\n" +
				"	static interface I3<E> extends I1<E>, I2<E> {}\n" +
				"	static class Class1 implements I3<String> {\n" +
				"		public void method(String o) { System.out.println(o); }\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		I3<String> i = new Class1();\n" +
				"		i.method(\"works\");\n" +
				"	}\n" +
				"}"
			},
			"works"
		);
	}
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { void foo() { new BB().test(); } }\n" +
				"class AA<T> { void test() {} }\n" +
				"class BB extends AA<CC> { <U> void test() {} }\n" +
				"class CC {}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X { void foo() { new BB().test(); } }\n" +
			"	                                       ^^^^\n" +
			"The method test() is ambiguous for the type BB\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	class BB extends AA<CC> { <U> void test() {} }\n" +
			"	                                   ^^^^^^\n" +
			"Name clash: The method test() of type BB has the same erasure as test() of type AA<T> but does not override it\n" +
			"----------\n"
		);
	}
	public void test003a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void singleMatch() { System.out.print(new BB().test(new N(), new Integer(1))); }\n" +
				"	void betterMatch() { System.out.print(new CC().test(new N(), new Integer(1))); }\n" +
				"	void worseMatch() { System.out.print(new DD().test(new N(), new Integer(1))); }\n" +
				"	public static void main(String[] s) {\n" +
				"		new X().singleMatch();\n" +
				"		new X().betterMatch();\n" +
				"		new X().worseMatch();\n" +
				"	}\n" +
				"}\n" +
				"class AA<T> { int test(T t, Number num) { return 1; } }\n" +
				"class BB extends AA<N> { @Override int test(N n, Number num) { return 2; } }\n" +
				"class CC extends AA<M> { <U extends Number> int test(N n, U u) { return 3; } }\n" +
				"class DD extends AA<N> { <U extends Number> int test(M m, U u) { return 4; } }\n" +
				"class M {}\n" +
				"class N extends M {}",
			},
			"231"
		);
	}
	public void test003b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void ambiguous() { new BB().test(new N()); }\n" +
				"	void exactMatch() { new CC().test(new N()); }\n" +
				"}\n" +
				"class AA<T> { void test(T t) {} }\n" +
				"class BB extends AA<N> { <U> void test(N n) {} }\n" +
				"class CC extends AA<N> { @Override void test(N n) {} }\n" +
				"class M {}\n" +
				"class N extends M {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\r\n" +
			"	void ambiguous() { new BB().test(new N()); }\r\n" +
			"	                            ^^^^\n" +
			"The method test(N) is ambiguous for the type BB\n" +
			"----------\n"
		);
	}
	public void test003c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void ambiguous() { new BB().test(new N(), Integer.valueOf(1)); }\n" +
				"}\n" +
				"class AA<T> { void test(T t, Integer i) {} }\n" +
				"class BB extends AA<M> { <U extends Number> void test(N n, U u) {} }\n" +
				"class M {}\n" +
				"class N extends M {}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	void ambiguous() { new BB().test(new N(), Integer.valueOf(1)); }\n" +
			"	                            ^^^^\n" +
			"The method test(N, Integer) is ambiguous for the type BB\n" +
			"----------\n"
		);
	}
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void test(M<Integer> m) {\n" +
				"		m.id(Integer.valueOf(111));\n" +
				"	}\n" +
				"}\n" +
				"class C<T extends Number> { public void id(T t) {} }\n" +
				"class M<TT> extends C<Integer> { public <ZZ> void id(Integer i) {} }\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	m.id(Integer.valueOf(111));\n" +
			"	  ^^\n" +
			"The method id(Integer) is ambiguous for the type M<Integer>\n" +
			"----------\n"
			// reference to id is ambiguous, both method id(A) in C<java.lang.Integer> and method id(B) in M<java.lang.Integer,java.lang.Integer> match
		);
	}
	public void test004a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void test(M<Integer> m) {\n" +
				"		m.id(new Integer(111));\n" +
				"	}\n" +
				"}\n" +
				"class C<T extends Number> { public void id(T t) {} }\n" +
				"class M<TT> extends C<Integer> { public void id(Integer i) {} }\n"
			},
			""
		);
	}
	public void test005() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
				"----------\n" +
				"1. WARNING in X.java (at line 2)\n" +
				"	<S extends A> void foo() { }\n" +
				"	                   ^^^^^\n" +
				"Duplicate method foo() in type X\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 3)\n" +
				"	<N extends B> N foo() { return null; }\n" +
				"	                ^^^^^\n" +
				"Duplicate method foo() in type X\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	new X().foo();\n" +
				"	        ^^^\n" +
				"The method foo() is ambiguous for the type X\n" +
				"----------\n":
					"----------\n" +
					"1. ERROR in X.java (at line 2)\n" +
					"	<S extends A> void foo() { }\n" +
					"	                   ^^^^^\n" +
					"Duplicate method foo() in type X\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 3)\n" +
					"	<N extends B> N foo() { return null; }\n" +
					"	                ^^^^^\n" +
					"Duplicate method foo() in type X\n" +
					"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"		 <S extends A> void foo() { }\n" +
				"		 <N extends B> N foo() { return null; }\n" +
				"		 void test () {\n" +
				"		 	new X().foo();\n" +
				"		 }\n" +
				"}\n" +
				"class A {}\n" +
				"class B {}\n"
			},
			expectedCompilerLog);
/* javac 7
X.java:3: name clash: <N>foo() and <S>foo() have the same erasure
                 <N extends B> N foo() { return null; }
                                 ^
  where N,S are type-variables:
    N extends B declared in method <N>foo()
    S extends A declared in method <S>foo()
1 error
 */
	}
	public void test006() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	new Y<Object>().foo(\"X\");\n" +
		"	                ^^^\n" +
		"The method foo(Object) is ambiguous for the type Y<Object>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	new Y<Object>().foo2(\"X\");\n" +
		"	                ^^^^\n" +
		"The method foo2(Object) is ambiguous for the type Y<Object>\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 10)\n" +
		"	void foo(T2 t) {}\n" +
		"	     ^^^^^^^^^\n" +
		"Name clash: The method foo(T2) of type Y<T2> has the same erasure as foo(U1) of type X<T> but does not override it\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 11)\n" +
		"	<U3> void foo2(T2 t) {}\n" +
		"	          ^^^^^^^^^^\n" +
		"Name clash: The method foo2(T2) of type Y<T2> has the same erasure as foo2(U2) of type X<T> but does not override it\n" +
		"----------\n":
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	new Y<Object>().foo(\"X\");\n" +
			"	                ^^^\n" +
			"The method foo(Object) is ambiguous for the type Y<Object>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	new Y<Object>().foo2(\"X\");\n" +
			"	                ^^^^\n" +
			"The method foo2(Object) is ambiguous for the type Y<Object>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	void foo(T2 t) {}\n" +
			"	     ^^^^^^^^^\n" +
			"Name clash: The method foo(T2) of type Y<T2> has the same erasure as foo(U1) of type X<T> but does not override it\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 11)\n" +
			"	<U3> void foo2(T2 t) {}\n" +
			"	          ^^^^^^^^^^\n" +
			"Name clash: The method foo2(T2) of type Y<T2> has the same erasure as foo2(U2) of type X<T> but does not override it\n" +
			"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"   void test() {\n" +
				"   	new Y<Object>().foo(\"X\");\n" +
				"   	new Y<Object>().foo2(\"X\");\n" +
				"   }\n" +
				"	<U1> U1 foo(U1 t) {return null;}\n" +
				"	<U2> U2 foo2(U2 t) {return null;}\n" +
				"}\n" +
				"class Y<T2> extends X<T2> {\n" +
				"	void foo(T2 t) {}\n" +
				"	<U3> void foo2(T2 t) {}\n" +
				"}\n"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: reference to foo is ambiguous, both method <U1>foo(U1) in X and method
 foo(T2) in Y match
        new Y<Object>().foo("X");
                       ^
  where U1,T2 are type-variables:
    U1 extends Object declared in method <U1>foo(U1)
    T2 extends Object declared in class Y
X.java:4: reference to foo2 is ambiguous, both method <U2>foo2(U2) in X and meth
od <U3>foo2(T2) in Y match
        new Y<Object>().foo2("X");
                       ^
  where U2,U3,T2 are type-variables:
    U2 extends Object declared in method <U2>foo2(U2)
    U3 extends Object declared in method <U3>foo2(T2)
    T2 extends Object declared in class Y
X.java:10: name clash: foo(T2) in Y and <U1>foo(U1) in X have the same erasure,
yet neither overrides the other
        void foo(T2 t) {}
             ^
  where T2,U1 are type-variables:
    T2 extends Object declared in class Y
    U1 extends Object declared in method <U1>foo(U1)
X.java:11: name clash: <U3>foo2(T2) in Y and <U2>foo2(U2) in X have the same era
sure, yet neither overrides the other
        <U3> void foo2(T2 t) {}
                  ^
  where U3,T2,U2 are type-variables:
    U3 extends Object declared in method <U3>foo2(T2)
    T2 extends Object declared in class Y
    U2 extends Object declared in method <U2>foo2(U2)
4 errors
 */
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=129056
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"B.java",
				"public class B {\n" +
				"   public static void main(String[] args) {\n" +
				"   	new M().foo(Integer.valueOf(1), 2);\n" +
				"   	new N().foo(Integer.valueOf(1), 2);\n" +
				"   }\n" +
				"}" +
				"interface I { void foo(Number arg1, Number arg2); }\n" +
				"class M {\n" +
				"	public void foo(int arg1, int arg2) {}\n" +
				"	public void foo(Number arg1, Number arg2) {}\n" +
				"}\n" +
				"class N implements I {\n" +
				"	public void foo(int arg1, int arg2) {}\n" +
				"	public void foo(Number arg1, Number arg2) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in B.java (at line 3)\r\n" +
			"	new M().foo(Integer.valueOf(1), 2);\r\n" +
			"	        ^^^\n" +
			"The method foo(int, int) is ambiguous for the type M\n" +
			"----------\n" +
			"2. ERROR in B.java (at line 4)\r\n" +
			"	new N().foo(Integer.valueOf(1), 2);\r\n" +
			"	        ^^^\n" +
			"The method foo(int, int) is ambiguous for the type N\n" +
			"----------\n"
			// reference to foo is ambiguous, both method foo(int,int) in M and method foo(java.lang.Number,java.lang.Number) in M match
			// reference to foo is ambiguous, both method foo(int,int) in N and method foo(java.lang.Number,java.lang.Number) in N match
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123943 - case 1
	public void test008() {
		this.runConformTest(
			new String[] {
				"AA.java",
				"public class AA {\n" +
				"   public static void main(String[] a) { System.out.print(new C().test(new T())); }\n" +
				"}" +
				"class S {}\n" +
				"class T extends S {}\n" +
				"class B { <U extends S> int test(U u) {return -1;} }\n" +
				"class C extends B { @Override int test(S s) {return 1;} }"
			},
			"1"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123943 - case 1
	public void test008a() {
		this.runNegativeTest(
			new String[] {
				"A.java",
				"public class A { void check() { new C().test(new T()); } }\n" +
				"class S {}\n" +
				"class T extends S {}\n" +
				"class B { int test(S s) {return 1;} }\n" +
				"class C extends B { <U extends S> int test(U u) {return -1;} }"
			},
			"----------\n" +
			"1. ERROR in A.java (at line 1)\n" +
			"	public class A { void check() { new C().test(new T()); } }\n" +
			"	                                        ^^^^\n" +
			"The method test(T) is ambiguous for the type C\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 5)\n" +
			"	class C extends B { <U extends S> int test(U u) {return -1;} }\n" +
			"	                                      ^^^^^^^^^\n" +
			"Name clash: The method test(U) of type C has the same erasure as test(S) of type B but does not override it\n" +
			"----------\n"
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123943 - case 2
	// see also Bug 399567 - [1.8] Different error message from the reference compiler
	public void test009() {
		String[] testFiles =
				new String[] {
				"T.java",
				"import java.util.*;\n" +
						"public class T {\n" +
						"   void test() {\n" +
						"   	OrderedSet<String> os = null;\n" +
						"   	os.add(\"hello\");\n" +
						"   	OrderedSet<Integer> os2 = null;\n" +
						"   	os2.add(1);\n" +
						"   }\n" +
						"}\n" +
						"interface OrderedSet<E> extends List<E>, Set<E> { boolean add(E o); }\n"
		};
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			this.runConformTest(testFiles, "");
		else
			this.runNegativeTest(
				testFiles,
				"----------\n" +
				"1. WARNING in T.java (at line 5)\n" +
				"	os.add(\"hello\");\n" +
				"	^^\n" +
				"Null pointer access: The variable os can only be null at this location\n" +
				"----------\n" +
				"2. WARNING in T.java (at line 7)\n" +
				"	os2.add(1);\n" +
				"	^^^\n" +
				"Null pointer access: The variable os2 can only be null at this location\n" +
				"----------\n" +
				"3. ERROR in T.java (at line 10)\n" +
				"	interface OrderedSet<E> extends List<E>, Set<E> { boolean add(E o); }\n" +
				"	          ^^^^^^^^^^\n" +
				"Duplicate default methods named spliterator with the parameters () and () are inherited from the types Set<E> and List<E>\n" +
				"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123943 variant to make it pass on JRE8
	public void test009a() {
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		this.runConformTest(
			new String[] {
				"T.java",
				"import java.util.*;\n" +
				"public class T {\n" +
				"   void test() {\n" +
				"   	OrderedSet<String> os = null;\n" +
				"   	os.add(\"hello\");\n" +
				"   	OrderedSet<Integer> os2 = null;\n" +
				"   	os2.add(1);\n" +
				"   }\n" +
				"}\n" +
				"interface OrderedSet<E> extends List<E>, Set<E> {\n" +
				"	boolean add(E o);\n" +
				"   default Spliterator<E> spliterator() { return null; }\n" +
    			"}\n"
			},
			""
		);
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=121024
public void test010a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  	interface Listener {}\n" +
			"  	interface ErrorListener {}\n" +
			"  	static <L1 extends Listener & ErrorListener> Object createParser(L1 l) { return null; }\n" +
			"  	static <L2 extends ErrorListener & Listener> Object createParser(L2 l) { return null; }\n" +
			"   public static void main(String[] s) {\n" +
			"   	class A implements Listener, ErrorListener {}\n" +
			"   	createParser(new A());\n" +
			"   }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	createParser(new A());\n" +
		"	^^^^^^^^^^^^\n" +
		"The method createParser(A) is ambiguous for the type X\n" +
		"----------\n"
	);
// javac 7 randomly picks which ever method is second
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=121024
public void test010b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  	interface Listener {}\n" +
			"  	interface ErrorListener {}\n" +
			"  	static <L1 extends Listener> int createParser(L1 l) { return 1; }\n" +
			"  	static <L2 extends ErrorListener & Listener> int createParser(L2 l) { return 2; }\n" +
			"   public static void main(String[] s) {\n" +
			"   	class A implements Listener, ErrorListener {}\n" +
			"   	System.out.print(createParser(new A()));\n" +
			"   }\n" +
			"}"
		},
		"2"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=121024
public void test010c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  	interface Listener {}\n" +
			"  	interface ErrorListener {}\n" +
			"  	static int createParser(Listener l) { return 1; }\n" +
			"  	static <L extends ErrorListener & Listener> int createParser(L l) { return 2; }\n" +
			"   public static void main(String[] s) {\n" +
			"   	class A implements Listener, ErrorListener {}\n" +
			"   	System.out.print(createParser(new A()));\n" +
			"   }\n" +
			"}"
		},
		"2"
	);
}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106090
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<A extends Number> extends Y<A> {\n" +
				"	<T> void foo(A n, T t) throws ExOne {}\n" +
				"	void test(X<Integer> x) throws ExTwo { x.foo(Integer.valueOf(1), Integer.valueOf(2)); }\n" +
				"	void test2(X x) throws ExTwo { x.foo(Integer.valueOf(1), Integer.valueOf(2)); }\n" +
				"}\n" +
				"class Y<C extends Number> {\n" +
				"	void foo(C x, C n) throws ExTwo {}\n" +
				"}\n" +
				"class ExOne extends Exception {static final long serialVersionUID = 1;}\n" +
				"class ExTwo extends Exception {static final long serialVersionUID = 2;}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	void test2(X x) throws ExTwo { x.foo(Integer.valueOf(1), Integer.valueOf(2)); }\n" +
			"	           ^\n" +
			"X is a raw type. References to generic type X<A> should be parameterized\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	void test2(X x) throws ExTwo { x.foo(Integer.valueOf(1), Integer.valueOf(2)); }\n" +
			"	                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The method foo(Number, Number) belongs to the raw type Y. References to generic type Y<C> should be parameterized\n" +
			"----------\n"
			// test2 - warning: [unchecked] unchecked call to foo(C,C) as a member of the raw type Y
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106090
	public void test011a() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
		"----------\n" +
		"1. WARNING in Combined.java (at line 2)\n" +
		"	<T extends Comparable<T>> void pickOne(T value) throws ExOne {}\n" +
		"	                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Erasure of method pickOne(T) is the same as another method in type Combined<A,B>\n" +
		"----------\n" +
		"2. WARNING in Combined.java (at line 3)\n" +
		"	<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Erasure of method pickOne(Comparable<T>) is the same as another method in type Combined<A,B>\n" +
		"----------\n":
			"----------\n" +
			"1. ERROR in Combined.java (at line 2)\n" +
			"	<T extends Comparable<T>> void pickOne(T value) throws ExOne {}\n" +
			"	                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Erasure of method pickOne(T) is the same as another method in type Combined<A,B>\n" +
			"----------\n" +
			"2. ERROR in Combined.java (at line 3)\n" +
			"	<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}\n" +
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Erasure of method pickOne(Comparable<T>) is the same as another method in type Combined<A,B>\n" +
			"----------\n";
		this.runNegativeTest(
			new String[] {
				"Combined.java",
				"public class Combined<A, B> {\n" +
				"	<T extends Comparable<T>> void pickOne(T value) throws ExOne {}\n" +
				"	<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}\n" +
				"	void pickOne(Combined<Integer,Integer> c) throws ExOne { c.pickOne(\"test\"); }\n" +
				"	<T extends Number> void pickTwo(Number n, T t) throws ExOne {}\n" +
				"	void pickTwo(A x, Number n) throws ExTwo {}\n" +
				"	void pickTwo(Combined<Integer,Integer> c) throws ExTwo { c.pickTwo(Integer.valueOf(1), 2); }\n" +
				"}\n" +
				"class ExOne extends Exception {static final long serialVersionUID = 1;}\n" +
				"class ExTwo extends Exception {static final long serialVersionUID = 2;}"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <T#1>pickOne(Comparable<T#1>) and <T#2>pickOne(T#2) have the same erasure
        <T> T pickOne(Comparable<T> value) throws ExTwo { return null;}
              ^
  where T#1,T#2 are type-variables:
    T#1 extends Object declared in method <T#1>pickOne(Comparable<T#1>)
    T#2 extends Comparable<T#2> declared in method <T#2>pickOne(T#2)
1 error
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106090
	public void test011b() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
		"----------\n" +
		"1. WARNING in Test1.java (at line 2)\n" +
		"	<T extends Comparable<T>> void pickOne(T value) throws ExOne {}\n" +
		"	                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Erasure of method pickOne(T) is the same as another method in type Test1<AA,BB>\n" +
		"----------\n" +
		"2. WARNING in Test1.java (at line 3)\n" +
		"	<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}\n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Erasure of method pickOne(Comparable<T>) is the same as another method in type Test1<AA,BB>\n" +
		"----------\n" +
		"3. WARNING in Test1.java (at line 4)\n" +
		"	void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) \"test\"); }\n" +
		"	                                                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked invocation pickOne(Comparable) of the generic method pickOne(T) of type Test1<Integer,Integer>\n" +
		"----------\n" +
		"4. WARNING in Test1.java (at line 4)\n" +
		"	void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) \"test\"); }\n" +
		"	                                                                  ^^^^^^^^^^\n" +
		"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" +
		"----------\n":
			"----------\n" +
			"1. ERROR in Test1.java (at line 2)\n" +
			"	<T extends Comparable<T>> void pickOne(T value) throws ExOne {}\n" +
			"	                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Erasure of method pickOne(T) is the same as another method in type Test1<AA,BB>\n" +
			"----------\n" +
			"2. ERROR in Test1.java (at line 3)\n" +
			"	<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}\n" +
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Erasure of method pickOne(Comparable<T>) is the same as another method in type Test1<AA,BB>\n" +
			"----------\n" +
			"3. WARNING in Test1.java (at line 4)\n" +
			"	void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) \"test\"); }\n" +
			"	                                                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked invocation pickOne(Comparable) of the generic method pickOne(T) of type Test1<Integer,Integer>\n" +
			"----------\n" +
			"4. WARNING in Test1.java (at line 4)\n" +
			"	void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) \"test\"); }\n" +
			"	                                                                  ^^^^^^^^^^\n" +
			"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" +
			"----------\n";
		this.runNegativeTest(
			new String[] {
				"Test1.java",
				"public class Test1<AA, BB> {\n" +
				"	<T extends Comparable<T>> void pickOne(T value) throws ExOne {}\n" +
				"	<T> T pickOne(Comparable<T> value) throws ExTwo { return null;}\n" +
				"	void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) \"test\"); }\n" +
				"}\n" +
				"class ExOne extends Exception {static final long serialVersionUID = 1;}\n" +
				"class ExTwo extends Exception {static final long serialVersionUID = 2;}"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:3: name clash: <T#1>pickOne(Comparable<T#1>) and <T#2>pickOne(T#2) have the same erasure
        <T> T pickOne(Comparable<T> value) throws ExTwo { return null;}
              ^
  where T#1,T#2 are type-variables:
    T#1 extends Object declared in method <T#1>pickOne(Comparable<T#1>)
    T#2 extends Comparable<T#2> declared in method <T#2>pickOne(T#2)
X.java:4: warning: [unchecked] unchecked method invocation: method pickOne in class Test1 is applied to given types
        void pickOne2(Test1<Integer,Integer> c) throws ExOne { c.pickOne((Comparable) "test"); }
                                                                        ^
  required: T
  found: Comparable
  where T is a type-variable:
    T extends Comparable<T> declared in method <T>pickOne(T)
1 error
1 warning
 */
	}
	public void test012() {
		this.runConformTest(
			new String[] {
				"XX.java",
				"public class XX {\n" +
				"	public static void main(String[] s) { System.out.println(new B().id(new Integer(1))); }\n" +
				"}\n" +
				"class A<T extends Number> { public int id(T t) {return 2;} }\n" +
				"class B extends A<Integer> { public int id(Integer i) {return 1;} }"
			},
			"1"
		);
	}
	public void test012a() {
		this.runNegativeTest(
			new String[] {
				"XX.java",
				"public class XX {\n" +
				"	public static void main(String[] s) { System.out.println(new B().id(Integer.valueOf(1))); }\n" +
				"}\n" +
				"class A<T extends Number> { public int id(T t) {return 2;} }\n" +
				"class B extends A<Integer> { public <ZZ> int id(Integer i) {return 1;} }"
			},
			"----------\n" +
			"1. ERROR in XX.java (at line 2)\r\n" +
			"	public static void main(String[] s) { System.out.println(new B().id(Integer.valueOf(1))); }\r\n" +
			"	                                                                 ^^\n" +
			"The method id(Integer) is ambiguous for the type B\n" +
			"----------\n"
			// reference to id is ambiguous, both method id(T) in A<java.lang.Integer> and method <ZZ>id(java.lang.Integer) in B match
		);
	}
	public void test013() {
			this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	<E extends A> void m(E e) { System.out.print(1); }\n" +
				"	<E extends B> void m(E e) { System.out.print(2); }\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().m(new A());\n" +
				"		new X().m(new B());\n" +
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B extends A {}\n"
			},
			"12"
		);
	}
	public void test014() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void a(G x) { System.out.print(1); }\n" +
				"	void b(F x) { System.out.print(2); }\n" +
				"	public static void main(String[] args) {\n" +
				"		H<C> h = null;\n" +
				"		G<C> g = null;\n" +
				"		new X().a(h);\n" +
				"		new X().a(g);\n" +
				"		new X().b(h);\n" +
				"		new X().b(g);\n" +
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" +
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"1122"
		);
	}
	public void test014a() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void a(G<C> x) { System.out.print(1); }\n" +
				"	void b(F<C> x) { System.out.print(2); }\n" +
				"	public static void main(String[] args) {\n" +
				"		H h = null;\n" +
				"		G g = null;\n" +
				"		new X().a(h);\n" +
				"		new X().a(g);\n" +
				"		new X().b(h);\n" +
				"		new X().b(g);\n" +
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" +
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"1122"
		);
	}
	public void test014b() {
		this.runConformTest(
			new String[] {
				"X0.java",
				"public class X0 {\n" +
				"	void two(G x) { System.out.print(1); }\n" +
				"	void two(F<A> x) { System.out.print(2); }\n" +
				"	void three(G x) { System.out.print(3); }\n" +
				"	void three(F<B> x) { System.out.print(4); }\n" +
				"	public static void main(String[] args) {\n" +
				"		H<C> h = null;\n" +
				"		new X0().two(h);\n" +
				"		new X0().three(h);\n" +
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" +
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"13"
		);
	}
	public void test014c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void a(G x) {}\n" +
				"	void a(F<C> x) {}\n" +
				"	void b(G<C> x) {}\n" +
				"	void b(F x) {}\n" +
				"	public static void main(String[] args) {\n" +
				"		H<C> h = null;\n" +
				"		new X().a(h);\n" +
				"		new X().b(h);\n" +
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" +
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 2)\r\n" +
			"	void a(G x) {}\r\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\r\n" +
			"	void b(F x) {}\r\n" +
			"	       ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\r\n" +
			"	new X().a(h);\r\n" +
			"	        ^\n" +
			"The method a(G) is ambiguous for the type X\n" +
			"----------\n"
			// reference to a is ambiguous, both method a(G) in X and method a(F<C>) in X match
		);
	}
	public void test014d() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void one(G<B> x) {}\n" +
				"	void one(F<B> x) {}\n" +
				"	public static void main(String[] args) {\n" +
				"		H<C> h = null;\n" +
				"		new X().one(h);\n" + // no match
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" +
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\r\n" +
			"	new X().one(h);\r\n" +
			"	        ^^^\n" +
			"The method one(G<B>) in the type X is not applicable for the arguments (H<C>)\n" +
			"----------\n"
		);
	}
	public void test014e() {
		this.runConformTest(
			new String[] {
				"X1.java",
				"public class X1 {\n" +
				"	void two(G<C> x) { System.out.print(1); }\n" +
				"	void two(F<B> x) { System.out.print(2); }\n" +
				"	void three(G<B> x) { System.out.print(3); }\n" +
				"	void three(F<C> x) { System.out.print(4); }\n" +
				"	void four(G<C> x) { System.out.print(5); }\n" +
				"	void four(F<C> x) { System.out.print(6); }\n" +
				"	public static void main(String[] args) {\n" +
				"		H<C> h = null;\n" +
				"		new X1().two(h);\n" +
				"		new X1().three(h);\n" +
				"		new X1().four(h);\n" +
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" +
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"145"
		);
	}
	public void test014f() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	<E1, E2 extends B> void three(G<E2> x) {}\n" +
				"	<E3 extends C> void three(F<E3> x) {}\n" +
				"	public static void main(String[] args) {\n" +
				"		H<C> h = null;\n" +
				"		new X().three(h);\n" +
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" +
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\r\n" +
			"	new X().three(h);\r\n" +
			"	        ^^^^^\n" +
			"The method three(G<C>) is ambiguous for the type X\n" +
			"----------\n"
		);
	}
	public void test014g() {
		this.runConformTest(
			new String[] {
				"X3.java",
				"public class X3 {\n" +
				"	<E1, E2 extends B> void one(G<E2> x) { System.out.print(1); }\n" +
				"	<E3 extends B> void one(F<E3> x) { System.out.print(2); }\n" +
				"	<E1, E2 extends C> void two(G<E2> x) { System.out.print(3); }\n" +
				"	<E3 extends B> void two(F<E3> x) { System.out.print(4); }\n" +
				"	<E1, E2 extends C> void four(G<E2> x) { System.out.print(5); }\n" +
				"	<E3 extends C> void four(F<E3> x) { System.out.print(6); }\n" +
				"	public static void main(String[] args) {\n" +
				"		H<C> h = null;\n" +
				"		new X3().one(h);\n" +
				"		new X3().two(h);\n" +
				"		new X3().four(h);\n" +
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" +
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"135"
		);
	}
	public void test014h() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void x(G x) { System.out.print(true); }\n" +
				"	void x(F x) { System.out.print(false); }\n" +
				"	void x2(G<C> x) { System.out.print(true); }\n" +
				"	void x2(F<C> x) { System.out.print(false); }\n" +
				"	void a(G x) {}\n" +
				"	void a(F<C> x) {}\n" +
				"	void a2(G x) {}\n" +
				"	<T extends C> void a2(F<T> x) {}\n" +
				"	void a3(G x) {}\n" +
				"	<T extends F<C>> void a3(T x) {}\n" +
				"	void a4(G x) {}\n" +
				"	<T extends C, S extends F<T>> void a4(S x) {}\n" +
				"	<T extends G> void a5(T x) {}\n" +
				"	void a5(F<C> x) {}\n" +
				"	<T extends G> void a6(T x) {}\n" +
				"	<T extends C, S extends F<T>> void a6(S x) {}\n" +
				"	void b(G<C> x) { System.out.print(true); }\n" +
				"	void b(F x) { System.out.print(false); }\n" +
				"	void b2(G<C> x) { System.out.print(true); }\n" +
				"	<T extends F> void b2(T x) { System.out.print(false); }\n" +
				"	<T extends C> void b3(G<T> x) { System.out.print(true); }\n" +
				"	void b3(F x) { System.out.print(false); }\n" +
				"	<T extends G<C>> void b4(T x) { System.out.print(true); }\n" +
				"	void b4(F x) { System.out.print(false); }\n" +
				"	<T extends C, S extends G<T>> void b5(S x) { System.out.print(true); }\n" +
				"	void b5(F x) { System.out.print(false); }\n" +
				"	void c(G x) { System.out.print(true); }\n" +
				"	<T extends C> void c(F x) { System.out.print(false); }\n" +
				"	public static void main(String[] args) {\n" +
				"		H<C> h = null;\n" +
				"		H hraw = null;\n" +
				"		new X().x(h);\n" +
				"		new X().x(hraw);\n" +
				"		new X().x2(h);\n" +
				"		new X().x2(hraw);\n" +
				"		new X().b(h);\n" +
				"		new X().b(hraw);\n" +
				"		new X().b2(h);\n" +
				"		new X().b2(hraw);\n" +
				"		new X().b3(h);\n" +
				"		new X().b3(hraw);\n" +
				"		new X().b4(h);\n" +
				"		new X().b4(hraw);\n" +
				"		new X().b5(h);\n" +
				"		new X().b5(hraw);\n" +
				"		new X().c(h);\n" +
				"		new X().c(hraw);\n" +
				"	}\n" +
				"}\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends B {}\n" +
				"class F<T1> {} \n" +
				"class G<T2> extends F<T2> {}\n" +
				"class H<T3> extends G<T3> {}"
			},
			"truetruetruetruetruetruetruetruetruetruetruetruetruetruetruetrue"
		);
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"public class Y extends X {\n" +
				"	public static void ambiguousCases() {\n" +
				"		H<C> h = null;\n" +
				"		H hraw = null;\n" +
				"		new X().a(h);\n" +
				"		new X().a(hraw);\n" +
				"		new X().a2(h);\n" +
				"		new X().a2(hraw);\n" +
				"		new X().a3(h);\n" +
				"		new X().a3(hraw);\n" +
				"		new X().a4(h);\n" +
				"		new X().a4(hraw);\n" +
				"		new X().a5(h);\n" +
				"		new X().a5(hraw);\n" +
				"		new X().a6(h);\n" +
				"		new X().a6(hraw);\n" +
				"	}\n" +
				"}\n"
			},
			(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"----------\n" +
			"1. WARNING in Y.java (at line 4)\n" +
			"	H hraw = null;\n" +
			"	^\n" +
			"H is a raw type. References to generic type H<T3> should be parameterized\n" +
			"----------\n" +
			"2. ERROR in Y.java (at line 5)\n" +
			"	new X().a(h);\n" +
			"	        ^\n" +
			"The method a(G) is ambiguous for the type X\n" +
			"----------\n" +
			"3. ERROR in Y.java (at line 6)\n" +
			"	new X().a(hraw);\n" +
			"	        ^\n" +
			"The method a(G) is ambiguous for the type X\n" +
			"----------\n" +
			"4. ERROR in Y.java (at line 7)\n" +
			"	new X().a2(h);\n" +
			"	        ^^\n" +
			"The method a2(G) is ambiguous for the type X\n" +
			"----------\n" +
			"5. ERROR in Y.java (at line 8)\n" +
			"	new X().a2(hraw);\n" +
			"	        ^^\n" +
			"The method a2(G) is ambiguous for the type X\n" +
			"----------\n" +
			"6. ERROR in Y.java (at line 9)\n" +
			"	new X().a3(h);\n" +
			"	        ^^\n" +
			"The method a3(G) is ambiguous for the type X\n" +
			"----------\n" +
			"7. ERROR in Y.java (at line 10)\n" +
			"	new X().a3(hraw);\n" +
			"	        ^^\n" +
			"The method a3(G) is ambiguous for the type X\n" +
			"----------\n" +
			"8. ERROR in Y.java (at line 11)\n" +
			"	new X().a4(h);\n" +
			"	        ^^\n" +
			"The method a4(G) is ambiguous for the type X\n" +
			"----------\n" +
			"9. ERROR in Y.java (at line 12)\n" +
			"	new X().a4(hraw);\n" +
			"	        ^^\n" +
			"The method a4(G) is ambiguous for the type X\n" +
			"----------\n" +
			"10. ERROR in Y.java (at line 13)\n" +
			"	new X().a5(h);\n" +
			"	        ^^\n" +
			"The method a5(H<C>) is ambiguous for the type X\n" +
			"----------\n" +
			"11. ERROR in Y.java (at line 14)\n" +
			"	new X().a5(hraw);\n" +
			"	        ^^\n" +
			"The method a5(H) is ambiguous for the type X\n" +
			"----------\n" +
			"12. ERROR in Y.java (at line 15)\n" +
			"	new X().a6(h);\n" +
			"	        ^^\n" +
			"The method a6(H<C>) is ambiguous for the type X\n" +
			"----------\n" +
			"13. ERROR in Y.java (at line 16)\n" +
			"	new X().a6(hraw);\n" +
			"	        ^^\n" +
			"The method a6(H) is ambiguous for the type X\n" +
			"----------\n"
			: // in 1.8 fewer of the calls are ambiguous
				"----------\n" +
				"1. WARNING in Y.java (at line 4)\n" +
				"	H hraw = null;\n" +
				"	^\n" +
				"H is a raw type. References to generic type H<T3> should be parameterized\n" +
				"----------\n" +
				"2. ERROR in Y.java (at line 5)\n" +
				"	new X().a(h);\n" +
				"	        ^\n" +
				"The method a(G) is ambiguous for the type X\n" +
				"----------\n" +
				"3. ERROR in Y.java (at line 6)\n" +
				"	new X().a(hraw);\n" +
				"	        ^\n" +
				"The method a(G) is ambiguous for the type X\n" +
				"----------\n" +
				"4. ERROR in Y.java (at line 7)\n" +
				"	new X().a2(h);\n" +
				"	        ^^\n" +
				"The method a2(G) is ambiguous for the type X\n" +
				"----------\n" +
				"5. ERROR in Y.java (at line 8)\n" +
				"	new X().a2(hraw);\n" +
				"	        ^^\n" +
				"The method a2(G) is ambiguous for the type X\n" +
				"----------\n" +
				"6. ERROR in Y.java (at line 13)\n" +
				"	new X().a5(h);\n" +
				"	        ^^\n" +
				"The method a5(H<C>) is ambiguous for the type X\n" +
				"----------\n" +
				"7. ERROR in Y.java (at line 14)\n" +
				"	new X().a5(hraw);\n" +
				"	        ^^\n" +
				"The method a5(H) is ambiguous for the type X\n" +
				"----------\n"),
			null,
			false
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=262209
	public void test014i() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface I<T> {}\n" +
				"interface J<T> extends I<T> {}\n" +
				"\n" +
				"class X {\n" +
				"	void a(G x) {}\n" +
				"	<T extends C, S extends F<T>> void a(S x) {}\n" +
				"\n" +
				"	void b(G x) {}\n" +
				"	void b(F x) {}\n" +
				"\n" +
				"	void c(G x) {}\n" +
				"	void c(F<?> x) {}\n" +
				"\n" +
				"	void d(G x) {}\n" +
				"	void d(F<C> x) {}\n" +
				"\n" +
				"	void e(G x) {}\n" +
				"	<T extends C> void e(F<T> x) {}\n" +
				"\n" +
				"	void f(G x) {}\n" +
				"	<S extends F> void f(S x) {}\n" +
				"\n" +
				"	void g(G x) {}\n" +
				"	<S extends F & J<S>> void g(S x) {}\n" +
				"\n" +
				"	<T extends G> void a2(T x) {}\n" +
				"	<T extends C, S extends F<T>> void a2(S x) {}\n" +
				"\n" +
				"	<T extends G> void b2(T x) {}\n" +
				"	void b2(F x) {}\n" +
				"\n" +
				"	<T extends G> void c2(T x) {}\n" +
				"	void c2(F<?> x) {}\n" +
				"\n" +
				"	<T extends G> void d2(T x) {}\n" +
				"	void d2(F<C> x) {}\n" +
				"\n" +
				"	<T extends G> void e2(T x) {}\n" +
				"	<T extends C> void e2(F<T> x) {}\n" +
				"\n" +
				"	<T extends G> void f2(T x) {}\n" +
				"	<S extends F & J> void f2(S x) {}\n" +
				"\n" +
				"	<T extends G> void g2(T x) {}\n" +
				"	<S extends F & J<S>> void g2(S x) {}\n" +
				"\n" +
				"	void test() {\n" +
				"		X x = new X();\n" +
				"		H<C> h = null;\n" +
				"		H hraw = null;\n" +
				"\n" +
				"		x.a(h);\n" +
				"		x.a(hraw);\n" +
				"\n" +
				"		x.b(h);\n" +
				"		x.b(hraw);\n" +
				"\n" +
				"		x.c(h);\n" +
				"		x.c(hraw);\n" +
				"\n" +
				"		x.d(h);\n" +
				"		x.d(hraw);\n" +
				"\n" +
				"		x.e(h);\n" +
				"		x.e(hraw);\n" +
				"\n" +
				"		x.f(h);\n" +
				"		x.f(hraw);\n" +
				"\n" +
				"		x.g(h);\n" +
				"		x.g(hraw);\n" +
				"\n" +
				"		x.a2(h);\n" +
				"		x.a2(hraw);\n" +
				"\n" +
				"		x.b2(h);	\n" +
				"		x.b2(hraw);\n" +
				"\n" +
				"		x.c2(h);\n" +
				"		x.c2(hraw);\n" +
				"\n" +
				"		x.d2(h);\n" +
				"		x.d2(hraw);\n" +
				"\n" +
				"		x.e2(h);\n" +
				"		x.e2(hraw);\n" +
				"\n" +
				"		x.f2(h);\n" +
				"		x.f2(hraw);\n" +
				"\n" +
				"		x.g2(h);	\n" +
				"		x.g2(hraw);\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class A {}\n" +
				"class B extends A {}\n" +
				"class C extends B implements I {}\n" +
				"class F<T1> {} \n" +
				"class G<T2> extends F<T2> implements J<T2> {}\n" +
				"class H<T3> extends G<T3> {}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	void a(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 8)\n" +
			"	void b(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 9)\n" +
			"	void b(F x) {}\n" +
			"	       ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 11)\n" +
			"	void c(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 14)\n" +
			"	void d(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 17)\n" +
			"	void e(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 20)\n" +
			"	void f(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 21)\n" +
			"	<S extends F> void f(S x) {}\n" +
			"	           ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"9. WARNING in X.java (at line 23)\n" +
			"	void g(G x) {}\n" +
			"	       ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"10. WARNING in X.java (at line 24)\n" +
			"	<S extends F & J<S>> void g(S x) {}\n" +
			"	           ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"11. WARNING in X.java (at line 26)\n" +
			"	<T extends G> void a2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"12. WARNING in X.java (at line 29)\n" +
			"	<T extends G> void b2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"13. WARNING in X.java (at line 30)\n" +
			"	void b2(F x) {}\n" +
			"	        ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"14. WARNING in X.java (at line 32)\n" +
			"	<T extends G> void c2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"15. WARNING in X.java (at line 35)\n" +
			"	<T extends G> void d2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"16. WARNING in X.java (at line 38)\n" +
			"	<T extends G> void e2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"17. WARNING in X.java (at line 41)\n" +
			"	<T extends G> void f2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"18. WARNING in X.java (at line 42)\n" +
			"	<S extends F & J> void f2(S x) {}\n" +
			"	           ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"19. WARNING in X.java (at line 42)\n" +
			"	<S extends F & J> void f2(S x) {}\n" +
			"	               ^\n" +
			"J is a raw type. References to generic type J<T> should be parameterized\n" +
			"----------\n" +
			"20. WARNING in X.java (at line 44)\n" +
			"	<T extends G> void g2(T x) {}\n" +
			"	           ^\n" +
			"G is a raw type. References to generic type G<T2> should be parameterized\n" +
			"----------\n" +
			"21. WARNING in X.java (at line 45)\n" +
			"	<S extends F & J<S>> void g2(S x) {}\n" +
			"	           ^\n" +
			"F is a raw type. References to generic type F<T1> should be parameterized\n" +
			"----------\n" +
			"22. WARNING in X.java (at line 50)\n" +
			"	H hraw = null;\n" +
			"	^\n" +
			"H is a raw type. References to generic type H<T3> should be parameterized\n" +
			"----------\n" +
			(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"23. ERROR in X.java (at line 52)\n" +
			"	x.a(h);\n" +
			"	  ^\n" +
			"The method a(G) is ambiguous for the type X\n" +
			"----------\n" +
			"24. ERROR in X.java (at line 53)\n" +
			"	x.a(hraw);\n" +
			"	  ^\n" +
			"The method a(G) is ambiguous for the type X\n" +
			"----------\n" +
			"25. ERROR in X.java (at line 58)\n" +
			"	x.c(h);\n" +
			"	  ^\n" +
			"The method c(G) is ambiguous for the type X\n" +
			"----------\n" +
			"26. ERROR in X.java (at line 59)\n" +
			"	x.c(hraw);\n" +
			"	  ^\n" +
			"The method c(G) is ambiguous for the type X\n" +
			"----------\n" +
			"27. ERROR in X.java (at line 61)\n" +
			"	x.d(h);\n" +
			"	  ^\n" +
			"The method d(G) is ambiguous for the type X\n" +
			"----------\n" +
			"28. ERROR in X.java (at line 62)\n" +
			"	x.d(hraw);\n" +
			"	  ^\n" +
			"The method d(G) is ambiguous for the type X\n" +
			"----------\n" +
			"29. ERROR in X.java (at line 64)\n" +
			"	x.e(h);\n" +
			"	  ^\n" +
			"The method e(G) is ambiguous for the type X\n" +
			"----------\n" +
			"30. ERROR in X.java (at line 65)\n" +
			"	x.e(hraw);\n" +
			"	  ^\n" +
			"The method e(G) is ambiguous for the type X\n" +
			"----------\n" +
			"31. ERROR in X.java (at line 71)\n" +
			"	x.g(hraw);\n" +
			"	  ^\n" +
			"The method g(G) is ambiguous for the type X\n" +
			"----------\n" +
			"32. ERROR in X.java (at line 73)\n" +
			"	x.a2(h);\n" +
			"	  ^^\n" +
			"The method a2(H<C>) is ambiguous for the type X\n" +
			"----------\n" +
			"33. ERROR in X.java (at line 74)\n" +
			"	x.a2(hraw);\n" +
			"	  ^^\n" +
			"The method a2(H) is ambiguous for the type X\n" +
			"----------\n" +
			"34. ERROR in X.java (at line 79)\n" +
			"	x.c2(h);\n" +
			"	  ^^\n" +
			"The method c2(H<C>) is ambiguous for the type X\n" +
			"----------\n" +
			"35. ERROR in X.java (at line 80)\n" +
			"	x.c2(hraw);\n" +
			"	  ^^\n" +
			"The method c2(H) is ambiguous for the type X\n" +
			"----------\n" +
			"36. ERROR in X.java (at line 82)\n" +
			"	x.d2(h);\n" +
			"	  ^^\n" +
			"The method d2(H<C>) is ambiguous for the type X\n" +
			"----------\n" +
			"37. ERROR in X.java (at line 83)\n" +
			"	x.d2(hraw);\n" +
			"	  ^^\n" +
			"The method d2(H) is ambiguous for the type X\n" +
			"----------\n" +
			"38. ERROR in X.java (at line 85)\n" +
			"	x.e2(h);\n" +
			"	  ^^\n" +
			"The method e2(H<C>) is ambiguous for the type X\n" +
			"----------\n" +
			"39. ERROR in X.java (at line 86)\n" +
			"	x.e2(hraw);\n" +
			"	  ^^\n" +
			"The method e2(H) is ambiguous for the type X\n" +
			"----------\n" +
			"40. ERROR in X.java (at line 92)\n" +
			"	x.g2(hraw);\n" +
			"	  ^^\n" +
			"The method g2(H) is ambiguous for the type X\n" +
			"----------\n" +
			"41. WARNING in X.java (at line 98)\n"
			: // fewer ambiguities in 1.8
				"23. ERROR in X.java (at line 61)\n" +
				"	x.d(h);\n" +
				"	  ^\n" +
				"The method d(G) is ambiguous for the type X\n" +
				"----------\n" +
				"24. ERROR in X.java (at line 62)\n" +
				"	x.d(hraw);\n" +
				"	  ^\n" +
				"The method d(G) is ambiguous for the type X\n" +
				"----------\n" +
				"25. ERROR in X.java (at line 64)\n" +
				"	x.e(h);\n" +
				"	  ^\n" +
				"The method e(G) is ambiguous for the type X\n" +
				"----------\n" +
				"26. ERROR in X.java (at line 65)\n" +
				"	x.e(hraw);\n" +
				"	  ^\n" +
				"The method e(G) is ambiguous for the type X\n" +
				"----------\n" +
				"27. ERROR in X.java (at line 82)\n" +
				"	x.d2(h);\n" +
				"	  ^^\n" +
				"The method d2(H<C>) is ambiguous for the type X\n" +
				"----------\n" +
				"28. ERROR in X.java (at line 83)\n" +
				"	x.d2(hraw);\n" +
				"	  ^^\n" +
				"The method d2(H) is ambiguous for the type X\n" +
				"----------\n" +
				"29. ERROR in X.java (at line 85)\n" +
				"	x.e2(h);\n" +
				"	  ^^\n" +
				"The method e2(H<C>) is ambiguous for the type X\n" +
				"----------\n" +
				"30. ERROR in X.java (at line 86)\n" +
				"	x.e2(hraw);\n" +
				"	  ^^\n" +
				"The method e2(H) is ambiguous for the type X\n" +
				"----------\n" +
				"31. WARNING in X.java (at line 98)\n"
			) +
			"	class C extends B implements I {}\n" +
			"	                             ^\n" +
			"I is a raw type. References to generic type I<T> should be parameterized\n" +
			"----------\n"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
	public void test015() {
		this.runConformTest(
			new String[] {
				"E.java",
				"public class E {\n" +
				"	public static void main(String[] s) {\n" +
				"		IJ ij = new K();\n" +
				"		try { ij.m(); } catch(E11 e) {}\n" +
				"	}\n" +
				"}\n" +
				"interface I { void m() throws E1; }\n" +
				"interface J { void m() throws E11; }\n" +
				"interface IJ extends I, J {}\n" +
				"class K implements IJ { public void m() {} }\n" +
				"class E1 extends Exception { static final long serialVersionUID = 1; }\n" +
				"class E11 extends E1 { static final long serialVersionUID = 2; }\n" +
				"class E2 extends Exception { static final long serialVersionUID = 3; }"
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
	public void _test016() {
		this.runConformTest(
			new String[] {
				"E.java",
				"public class E {\n" +
				"	public static void main(String[] s) {\n" +
				"		IJ ij = new K();\n" +
				"		try { ij.m(); } catch(E11 e) {}\n" +
				"	}\n" +
				"}\n" +
				"interface I { void m() throws E1; }\n" +
				"interface J { void m() throws E2, E11; }\n" +
				"interface IJ extends I, J {}\n" +
				"class K implements IJ { public void m() {} }\n" +
				"class E1 extends Exception { static final long serialVersionUID = 1; }\n" +
				"class E11 extends E1 { static final long serialVersionUID = 2; }\n" +
				"class E2 extends Exception { static final long serialVersionUID = 3; }"
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
	public void _test016a() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"public class E {\n" +
				"	public static void main(String[] s) {\n" +
				"		IJ ij = new K();\n" +
				"		ij.m();\n" +
				"		try { ij.m(); } catch(E2 e) {}\n" +
				"	}\n" +
				"}\n" +
				"interface I { void m() throws E1; }\n" +
				"interface J { void m() throws E2, E11; }\n" +
				"interface IJ extends I, J {}\n" +
				"class K implements IJ { public void m() {} }\n" +
				"class E1 extends Exception { static final long serialVersionUID = 1; }\n" +
				"class E11 extends E1 { static final long serialVersionUID = 2; }\n" +
				"class E2 extends Exception { static final long serialVersionUID = 3; }"
			},
			"----------\n" +
			"1. ERROR in E.java (at line 4)\r\n" +
			"	ij.m();\r\n" +
			"	^^^^^^\n" +
			"Unhandled exception type E11\n" +
			"----------\n" +
			"2. ERROR in E.java (at line 5)\r\n" +
			"	try { ij.m(); } catch(E2 e) {}\r\n" +
			"	      ^^^^^^\n" +
			"Unhandled exception type E11\n" +
			"----------\n" +
			"3. ERROR in E.java (at line 5)\r\n" +
			"	try { ij.m(); } catch(E2 e) {}\r\n" +
			"	                      ^^\n" +
			"Unreachable catch block for E2. This exception is never thrown from the try statement body\n" +
			"----------\n"
			// 4: unreported exception E11; must be caught or declared to be thrown
			// 5: exception E2 is never thrown in body of corresponding try statement
			// 5: unreported exception E11; must be caught or declared to be thrown
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149893
	public void test017() {
		this.runConformTest(
			new String[] {
				"AbstractFilter.java",
				"import java.util.*;\n" +
				"public class AbstractFilter<T> implements IFilter<T> {\n" +
				"	public final <E extends T> boolean selekt(E obj) { return true; }\n" +
				"	public final <E extends T> List<E> filter(List<E> elements) {\n" +
				"		if ((elements == null) || (elements.size() == 0)) return elements;\n" +
				"		List<E> okElements = new ArrayList<E>(elements.size());\n" +
				"		for (E obj : elements) {\n" +
				"			if (selekt(obj)) okElements.add(obj);\n" +
				"		}\n" +
				"		return okElements;" +
				"	}\n" +
				"}\n" +
				"interface IFilter<T> {\n" +
				"	<E extends T> boolean selekt(E obj);\n" +
				"	<E extends T> List<E> filter(List<E> elements);\n" +
				"}"
			},
			""
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	public void test018() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
			return;
	this.runConformTest(
		new String[] {
			"Y.java",
			"class X<T extends Object> {\n" +
			"  public static <U extends Object> X<U> make(Class<U> clazz) {\n" +
			"    System.out.print(false);\n" +
			"    return new X<U>();\n" +
			"  }\n" +
			"}\n" +
			"public class Y<V extends String> extends X<V> {\n" +
			"  public static <W extends String> Y<W> make(Class<W> clazz) {\n" +
			"    System.out.print(true);\n" +
			"    return new Y<W>();\n" +
			"  }\n" +
			"  public static void main(String[] args) throws Exception {\n" +
			"    Y.make(String.class);\n" +
			"  }\n" +
			"}"
		},
		"true");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	// in fact, <W extends String> Y<W> make(Class<W> clazz) is the most
	// specific method according to JLS 15.12.2.5
	public void test019() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;

	this.runConformTest(
		new String[] {
			"Y.java",
			"class X<T extends Object> {\n" +
			"  public static <U extends Object> X<U> make(Class<U> clazz) {\n" +
			"    System.out.print(false);\n" +
			"    return new X<U>();\n" +
			"  }\n" +
			"}\n" +
			"public class Y<V extends String> extends X<V> {\n" +
			"  public static <W extends String> Y<W> make(Class<W> clazz) {\n" +
			"    System.out.print(true);\n" +
			"    return new Y<W>();\n" +
			"  }\n" +
			"  public static void main(String[] args) throws Exception {\n" +
			"    Y.make(getClazz());\n" +
			"  }\n" +
			"  public static Class getClazz() {\n" +
			"    return String.class;\n" +
			"  }\n" +
			"}"
		},
		"true");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	public void test020() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
		new String[] {
			"Y.java",
			"class X<T extends Object> {\n" +
			"  public static <U extends Object> X<U> make(Class<U> clazz) {\n" +
			"    System.out.print(true);\n" +
			"    return new X<U>();\n" +
			"  }\n" +
			"}\n" +
			"public class Y<V extends String> extends X<V> {\n" +
			"  public static <W extends String> Y<W> make(Class<W> clazz) {\n" +
			"    System.out.print(false);\n" +
			"    return new Y<W>();\n" +
			"  }\n" +
			"  public static void main(String[] args) throws Exception {\n" +
			"    Y.make(getClazz().newInstance().getClass());\n" +
			"  }\n" +
			"  public static Class getClazz() {\n" +
			"    return String.class;\n" +
			"  }\n" +
			"}"
		},
		"true");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	// variant: having both methods in the same class should not change anything
	public void test021() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
		"----------\n" +
		"1. WARNING in Y.java (at line 3)\n" +
		"	public class Y<V extends String> extends X<V> {\n" +
		"	                         ^^^^^^\n" +
		"The type parameter V should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		"2. WARNING in Y.java (at line 4)\n" +
		"	public static <W extends String> Y<W> make(Class<W> clazz) {\n" +
		"	                         ^^^^^^\n" +
		"The type parameter W should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		"3. WARNING in Y.java (at line 4)\n" +
		"	public static <W extends String> Y<W> make(Class<W> clazz) {\n" +
		"	                                      ^^^^^^^^^^^^^^^^^^^^\n" +
		"Erasure of method make(Class<W>) is the same as another method in type Y<V>\n" +
		"----------\n" +
		"4. WARNING in Y.java (at line 8)\n" +
		"	public static <U extends Object> X<U> make(Class<U> clazz) {\n" +
		"	                                      ^^^^^^^^^^^^^^^^^^^^\n" +
		"Erasure of method make(Class<U>) is the same as another method in type Y<V>\n" +
		"----------\n" +
		"5. WARNING in Y.java (at line 13)\n" +
		"	Y.make(getClazz());\n" +
		"	^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: Unchecked invocation make(Class) of the generic method make(Class<W>) of type Y\n" +
		"----------\n" +
		"6. WARNING in Y.java (at line 13)\n" +
		"	Y.make(getClazz());\n" +
		"	       ^^^^^^^^^^\n" +
		"Type safety: The expression of type Class needs unchecked conversion to conform to Class<String>\n" +
		"----------\n" +
		"7. WARNING in Y.java (at line 15)\n" +
		"	public static Class getClazz() {\n" +
		"	              ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n":
			"----------\n" +
			"1. WARNING in Y.java (at line 3)\n" +
			"	public class Y<V extends String> extends X<V> {\n" +
			"	                         ^^^^^^\n" +
			"The type parameter V should not be bounded by the final type String. Final types cannot be further extended\n" +
			"----------\n" +
			"2. WARNING in Y.java (at line 4)\n" +
			"	public static <W extends String> Y<W> make(Class<W> clazz) {\n" +
			"	                         ^^^^^^\n" +
			"The type parameter W should not be bounded by the final type String. Final types cannot be further extended\n" +
			"----------\n" +
			"3. ERROR in Y.java (at line 4)\n" +
			"	public static <W extends String> Y<W> make(Class<W> clazz) {\n" +
			"	                                      ^^^^^^^^^^^^^^^^^^^^\n" +
			"Erasure of method make(Class<W>) is the same as another method in type Y<V>\n" +
			"----------\n" +
			"4. ERROR in Y.java (at line 8)\n" +
			"	public static <U extends Object> X<U> make(Class<U> clazz) {\n" +
			"	                                      ^^^^^^^^^^^^^^^^^^^^\n" +
			"Erasure of method make(Class<U>) is the same as another method in type Y<V>\n" +
			"----------\n" +
			"5. WARNING in Y.java (at line 13)\n" +
			"	Y.make(getClazz());\n" +
			"	^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: Unchecked invocation make(Class) of the generic method make(Class<W>) of type Y\n" +
			"----------\n" +
			"6. WARNING in Y.java (at line 13)\n" +
			"	Y.make(getClazz());\n" +
			"	       ^^^^^^^^^^\n" +
			"Type safety: The expression of type Class needs unchecked conversion to conform to Class<String>\n" +
			"----------\n" +
			"7. WARNING in Y.java (at line 15)\n" +
			"	public static Class getClazz() {\n" +
			"	              ^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n";
		this.runNegativeTest(
			new String[] {
				"Y.java",
				"class X<T extends Object> {\n" +
				"}\n" +
				"public class Y<V extends String> extends X<V> {\n" +
				"  public static <W extends String> Y<W> make(Class<W> clazz) {\n" +
				"    System.out.print(true);\n" +
				"    return new Y<W>();\n" +
				"  }\n" +
				"  public static <U extends Object> X<U> make(Class<U> clazz) {\n" +
				"    System.out.print(false);\n" +
				"    return new X<U>();\n" +
				"  }\n" +
				"  public static void main(String[] args) throws Exception {\n" +
				"    Y.make(getClazz());\n" +
				"  }\n" +
				"  public static Class getClazz() {\n" +
				"    return String.class;\n" +
				"  }\n" +
				"}"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:8: name clash: <U>make(Class<U>) and <W>make(Class<W>) have the same erasure
  public static <U extends Object> X<U> make(Class<U> clazz) {
                                        ^
  where U,W are type-variables:
    U extends Object declared in method <U>make(Class<U>)
    W extends String declared in method <W>make(Class<W>)
X.java:13: warning: [unchecked] unchecked conversion
    Y.make(getClazz());
                   ^
  required: Class<W#1>
  found:    Class
  where W#1,W#2 are type-variables:
    W#1 extends String declared in method <W#2>make(Class<W#2>)
    W#2 extends String declared in method <W#2>make(Class<W#2>)
X.java:13: warning: [unchecked] unchecked method invocation: method make in class Y is applied to given types
    Y.make(getClazz());
          ^
  required: Class<W>
  found: Class
  where W is a type-variable:
    W extends String declared in method <W>make(Class<W>)
1 error
2 warnings
 */
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147647
	// variant: using instances triggers raw methods, which are ambiguous
	public void test022() {
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6182950
		String expectedCompilerLog = (this.complianceLevel == ClassFileConstants.JDK1_6)?
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	class Y<V extends String> extends X<V> {\n" +
		"	                  ^^^^^^\n" +
		"The type parameter V should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	public <W extends String> Y<W> make(Class<W> clazz) {\n" +
		"	                  ^^^^^^\n" +
		"The type parameter W should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 4)\n" +
		"	public <W extends String> Y<W> make(Class<W> clazz) {\n" +
		"	                               ^^^^^^^^^^^^^^^^^^^^\n" +
		"Erasure of method make(Class<W>) is the same as another method in type Y<V>\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 7)\n" +
		"	public <U extends Object> X<U> make(Class<U> clazz) {\n" +
		"	                               ^^^^^^^^^^^^^^^^^^^^\n" +
		"Erasure of method make(Class<U>) is the same as another method in type Y<V>\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 12)\n" +
		"	Y y = new Y();\n" +
		"	^\n" +
		"Y is a raw type. References to generic type Y<V> should be parameterized\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 12)\n" +
		"	Y y = new Y();\n" +
		"	          ^\n" +
		"Y is a raw type. References to generic type Y<V> should be parameterized\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 13)\n" +
		"	y.make(String.class);\n" +
		"	  ^^^^\n" +
		"The method make(Class) is ambiguous for the type Y\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 14)\n" +
		"	y.make(getClazz());\n" +
		"	  ^^^^\n" +
		"The method make(Class) is ambiguous for the type Y\n" +
		"----------\n" +
		"9. ERROR in X.java (at line 15)\n" +
		"	y.make(getClazz().newInstance().getClass());\n" +
		"	  ^^^^\n" +
		"The method make(Class) is ambiguous for the type Y\n" +
		"----------\n" +
		"10. WARNING in X.java (at line 17)\n" +
		"	public static Class getClazz() {\n" +
		"	              ^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n":
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	class Y<V extends String> extends X<V> {\n" +
			"	                  ^^^^^^\n" +
			"The type parameter V should not be bounded by the final type String. Final types cannot be further extended\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	public <W extends String> Y<W> make(Class<W> clazz) {\n" +
			"	                  ^^^^^^\n" +
			"The type parameter W should not be bounded by the final type String. Final types cannot be further extended\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	public <W extends String> Y<W> make(Class<W> clazz) {\n" +
			"	                               ^^^^^^^^^^^^^^^^^^^^\n" +
			"Erasure of method make(Class<W>) is the same as another method in type Y<V>\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	public <U extends Object> X<U> make(Class<U> clazz) {\n" +
			"	                               ^^^^^^^^^^^^^^^^^^^^\n" +
			"Erasure of method make(Class<U>) is the same as another method in type Y<V>\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 12)\n" +
			"	Y y = new Y();\n" +
			"	^\n" +
			"Y is a raw type. References to generic type Y<V> should be parameterized\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 12)\n" +
			"	Y y = new Y();\n" +
			"	          ^\n" +
			"Y is a raw type. References to generic type Y<V> should be parameterized\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 13)\n" +
			"	y.make(String.class);\n" +
			"	^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The method make(Class) belongs to the raw type Y. References to generic type Y<V> should be parameterized\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 14)\n" +
			"	y.make(getClazz());\n" +
			"	^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The method make(Class) belongs to the raw type Y. References to generic type Y<V> should be parameterized\n" +
			"----------\n" +
			"9. WARNING in X.java (at line 15)\n" +
			"	y.make(getClazz().newInstance().getClass());\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Type safety: The method make(Class) belongs to the raw type Y. References to generic type Y<V> should be parameterized\n" +
			"----------\n" +
			"10. WARNING in X.java (at line 17)\n" +
			"	public static Class getClazz() {\n" +
			"	              ^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends Object> {\n" +
				"}\n" +
				"class Y<V extends String> extends X<V> {\n" +
				"  public <W extends String> Y<W> make(Class<W> clazz) {\n" +
				"    return new Y<W>();\n" +
				"  }\n" +
				"  public <U extends Object> X<U> make(Class<U> clazz) {\n" +
				"    return new X<U>();\n" +
				"  }\n" +
				"  @SuppressWarnings({\"deprecation\"})\n" +
				"  public static void main(String[] args) throws Exception {\n" +
				"    Y y = new Y();\n" +
				"    y.make(String.class);\n" +
				"    y.make(getClazz());\n" +
				"    y.make(getClazz().newInstance().getClass());\n" +
				"  }\n" +
				"  public static Class getClazz() {\n" +
				"    return String.class;\n" +
				"  }\n" +
				"}"
			},
			expectedCompilerLog
		);
/* javac 7
X.java:7: name clash: <U>make(Class<U>) and <W>make(Class<W>) have the same erasure
  public <U extends Object> X<U> make(Class<U> clazz) {
                                 ^
  where U,W are type-variables:
    U extends Object declared in method <U>make(Class<U>)
    W extends String declared in method <W>make(Class<W>)
X.java:12: warning: [unchecked] unchecked call to <W>make(Class<W>) as a member of the raw type Y
    y.make(String.class);
          ^
  where W is a type-variable:
    W extends String declared in method <W>make(Class<W>)
X.java:13: warning: [unchecked] unchecked call to <W>make(Class<W>) as a member of the raw type Y
    y.make(getClazz());
          ^
  where W is a type-variable:
    W extends String declared in method <W>make(Class<W>)
X.java:14: warning: [unchecked] unchecked call to <W>make(Class<W>) as a member of the raw type Y
    y.make(getClazz().newInstance().getClass());
          ^
  where W is a type-variable:
    W extends String declared in method <W>make(Class<W>)
1 error
3 warnings
 */
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159711
public void test023() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"public class X {\n" +
			"  public static void staticFoo(Collection<?> p) {\n" +
			"    System.out.print(1);\n" +
			"  }\n" +
			"  public static <T extends List<?>> void staticFoo(T p) {\n" +
			"    System.out.print(2);\n" +
			"  }\n" +
			"  public void foo(Collection<?> p) {\n" +
			"    System.out.print(1);\n" +
			"  }\n" +
			"  public <T extends List<?>> void foo(T p) {\n" +
			"    System.out.print(2);\n" +
			"  }\n" +
			"  public void foo2(Collection<?> p) {\n" +
			"    System.out.print(1);\n" +
			"  }\n" +
			"  public void foo2(List<?> p) {\n" +
			"    System.out.print(2);\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    staticFoo(new ArrayList<String>(Arrays.asList(\"\")));\n" +
			"    new X().foo(new ArrayList<String>(Arrays.asList(\"\")));\n" +
			"    new X().foo2(new ArrayList<String>(Arrays.asList(\"\")));\n" +
			"  }\n" +
			"}"
		},
		"222");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159711
// self contained variant
public void test024() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void foo(L1<?> p) {\n" +
			"    System.out.println(1);\n" +
			"  }\n" +
			"  public static <T extends L2<?>> void foo(T p) {\n" +
			"    System.out.println(2);\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    foo(new L3<String>());\n" +
			"  }\n" +
			"}",
			"L1.java",
			"public interface L1<T> {\n" +
			"}",
			"L2.java",
			"public interface L2<T> extends L1<T> {\n" +
			"}",
			"L3.java",
			"public class L3<T> implements L2<T> {\n" +
			"  public L3() {\n" +
			"  }\n" +
			"}",
		},
		"2");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162026
public void test025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  J m = new Y();\n" +
			"  void foo() {\n" +
			"    m.foo(1.0f);\n" +
			"  }\n" +
			"}",
			"I.java",
			"public interface I {\n" +
			"  <T extends Number> T foo(final Number p);\n" +
			"}",
			"J.java",
			"public interface J extends I {\n" +
			"  Float foo(final Number p);\n" +
			"}",
			"Y.java",
			"public class Y implements J {\n" +
			"  public Float foo(final Number p){\n" +
			"    return null;\n" +
			"  }\n" +
			"}",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162026
// variant
public void test026() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    (new Y()).foo(1.0f);\n" +
			"  }\n" +
			"}",
			"I.java",
			"public interface I {\n" +
			"  <T extends Number> T foo(final Number p);\n" +
			"}",
			"J.java",
			"public interface J extends I {\n" +
			"  Float foo(final Number p);\n" +
			"}",
			"Y.java",
			"public class Y implements J {\n" +
			"  public Float foo(final Number p){\n" +
			"    return null;" +
			"  }\n" +
			"}",
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162026
// variant
public void test027() {
	this.runNegativeTest(
		new String[] {
			"J.java",
			"public interface J {\n" +
			"  <T extends Number> T foo(final Number p);\n" +
			"  Float foo(final Number p);\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in J.java (at line 2)\n" +
		"	<T extends Number> T foo(final Number p);\n" +
		"	                     ^^^^^^^^^^^^^^^^^^^\n" +
		"Duplicate method foo(Number) in type J\n" +
		"----------\n" +
		"2. ERROR in J.java (at line 3)\n" +
		"	Float foo(final Number p);\n" +
		"	      ^^^^^^^^^^^^^^^^^^^\n" +
		"Duplicate method foo(Number) in type J\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
public void test028() {
	this.runConformTest(
		new String[] { /* test files */
			"X.java",
			"interface Irrelevant {}\n" +
			"interface I {\n" +
			"  Object foo(Number n);\n" +
			"}\n" +
			"interface J extends Irrelevant, I {\n" +
			"  String foo(Number n);\n" +
			"}\n" +
			"interface K {\n" +
			"  Object foo(Number n);\n" +
			"}\n" +
			"public abstract class X implements J, K {\n" +
			"  void foo() {\n" +
			"    foo(0.0f);\n" +
			"  }\n" +
			"}"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - simplified
public void test029() {
	this.runConformTest(
		new String[] { /* test files */
			"X.java",
			"interface J {\n" +
			"  String foo(Number n);\n" +
			"}\n" +
			"interface K {\n" +
			"  Object foo(Number n);\n" +
			"}\n" +
			"public abstract class X implements J, K {\n" +
			"  void foo() {\n" +
			"    foo(0.0f);\n" +
			"  }\n" +
			"}"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - same return type
public void test030() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface J {\n" +
			"  Object foo(Number n);\n" +
			"}\n" +
			"interface K {\n" +
			"  Object foo(Number n);\n" +
			"}\n" +
			"public abstract class X implements J, K {\n" +
			"  void foo() {\n" +
			"    foo(0.0f);\n" +
			"  }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant
public void test031() {
	this.runConformTest(
		new String[] { /* test files */
			"X.java",
			"interface Irrelevant {}\n" +
			"interface I {\n" +
			"  Object foo(Number n);\n" +
			"}\n" +
			"interface J extends Irrelevant, I {\n" +
			"  String foo(Number n);\n" +
			"}\n" +
			"interface K {\n" +
			"  Object foo(Number n);\n" +
			"}\n" +
			"public abstract class X implements Irrelevant, I, J, K {\n" +
			"  void foo() {\n" +
			"    foo(0.0f);\n" +
			"  }\n" +
			"}"
		});
}
// tests 32-34 were moved to MethodVerifyTest 134-140

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - the inheriting class implements foo
public void test035() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"  Object foo(Number n);\n" +
			"}\n" +
			"abstract class J {\n" +
			"  abstract String foo(Number n);\n" +
			"}\n" +
			"public class X extends J implements I {\n" +
			"  void bar() {\n" +
			"    foo(0.0f);\n" + // calls X#foo(Number)
			"  }\n" +
			"  public String foo(Number n) {\n" +
			"    return null;\n" +
			"  }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - extending instead of implementing
public void test037() {
	this.runConformTest(
 		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"interface I {\n" +
			"  Object foo(Number n);\n" +
			"}\n" +
			"abstract class J {\n" +
			"  abstract String foo(Number n);\n" +
			"}\n" +
			"public abstract class X extends J implements I {\n" +
			"  void bar() {\n" +
			"    foo(0.0f);\n" +
			"  }\n" +
			"}"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - no promotion of parameter from float to Number
public void test038() {
	this.runConformTest(
		new String[] { /* test files */
			"X.java",
			"interface I {\n" +
			"  Object foo(float f);\n" +
			"}\n" +
			"abstract class J {\n" +
			"  public abstract String foo(float f);\n" +
			"}\n" +
			"public abstract class X extends J implements I {\n" +
			"  void bar() {\n" +
			"    foo(0.0f);\n" +
			"  }\n" +
			"}"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - an explicit cast solves the issue
public void test039() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"  Object foo(float f);\n" +
			"}\n" +
			"abstract class J {\n" +
			"  public abstract String foo(float f);\n" +
			"}\n" +
			"public abstract class X extends J implements I {\n" +
			"  void bar() {\n" +
			"    String s = ((J) this).foo(0.0f);\n" +
			"  }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - an explicit cast solves the issue
public void test040() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"  Object foo(float f);\n" +
			"}\n" +
			"abstract class J {\n" +
			"  public abstract String foo(float f);\n" +
			"}\n" +
			"public abstract class X extends J implements I {\n" +
			"  void bar() {\n" +
			"    Object o = ((I) this).foo(0.0f);\n" +
			"  }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - connecting return types
public void test041() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"  Object foo(float f);\n" +
			"}\n" +
			"abstract class J {\n" +
			"  public abstract String foo(float f);\n" +
			"}\n" +
			"public abstract class X extends J implements I {\n" +
			"  void bar() {\n" +
			"    String s = ((I) this).foo(0.0f);\n" +
			"    s = this.foo(0.0f);\n" + // without cast a different overload is selected, returning String
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	String s = ((I) this).foo(0.0f);\n" +
		"	           ^^^^^^^^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from Object to String\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - a further inheriting class implements String foo
public void test042() {
	this.runConformTest(
 		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"interface I {\n" +
			"  Object foo(float f);\n" +
			"}\n" +
			"abstract class J {\n" +
			"  public abstract String foo(float f);\n" +
			"}\n" +
			"public abstract class X extends J implements I {\n" +
			"  void bar() {\n" +
			"    foo(0.0f);\n" +
			"  }\n" +
			"}\n" +
			"class Z extends X {\n" +
			"  @Override" +
			"  public String foo(float f) {\n" +
			"    return null;\n" +
			"  }\n" +
			"}"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162065
// variant - a further inheriting class implements Object foo
public void test043() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"  Object foo(float f);\n" +
			"}\n" +
			"abstract class J {\n" +
			"  public abstract String foo(float f);\n" +
			"}\n" +
			"public abstract class X extends J implements I {\n" +
			"  void bar() {\n" +
			"    foo(0.0f);\n" +
			"  }\n" +
			"}\n" +
			"class Z extends X {\n" +
			"  @Override\n" +
			"  public Object foo(float f) {\n" +  // cannot override String foo
			"    return null;\n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 14)\n" +
		"	public Object foo(float f) {\n" +
		"	       ^^^^^^\n" +
		"The return type is incompatible with J.foo(float)\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163370
public void test044() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I<E> {}\n" +
			"class Y<E> {}\n" +
			"public class X<E extends Y<E>> implements I<E> {\n" +
			"  public static <E extends Y<E>> X<E> bar(X<E> s) {\n" +
			"    return null;\n" +
			"  }\n" +
			"  public static <E extends Y<E>> X<E> bar(I<E> c) {\n" +
			"    return null;\n" +
			"  }\n" +
			"  public static <E extends Y<E>> X<E> foo(X<E> s) {\n" +
			"    X<E> result = bar(s);\n" +
			"    return result;\n" +
			"  }\n" +
			"}"
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165620
public void test045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"abstract class Y<T> implements I<T> {\n" +
			"}\n" +
			"interface I<T> { \n" +
			"}\n" +
			"interface J<T> {\n" +
			"}\n" +
			"class X {\n" +
			"  public static <V extends J<? super V>> V foo(final I<V> a)\n" +
			"  {\n" +
			"    return null;\n" +
			"  }\n" +
			"  public static <V extends J<? super V>> V foo(final Y<V> a)\n" +
			"  {\n" +
			"    return null;\n" +
			"  }\n" +
			"  public static <V extends J<? super V>> void test(final Y<V> a)\n" +
			"  {\n" +
			"    foo(a);\n" +
			"  }\n" +
			"}"
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163370
// variant
public void test046() {
	this.runConformTest(
		new String[] {
			"X.java",
			"abstract class Y<T, U> implements I<T, U> {\n" +
			"}\n" +
			"interface I<T, U> { \n" +
			"}\n" +
			"interface J<T, U> {\n" +
			"}\n" +
			"class X {\n" +
			"  public static <V extends J<V, W>, W extends J<V, W>> V foo(final I<V, W> a)\n" +
			"  {\n" +
			"    return null;\n" +
			"  }\n" +
			"  public static <V extends J<V, W>, W extends J<V, W>> V foo(final Y<V, W> a)\n" +
			"  {\n" +
			"    return null;\n" +
			"  }\n" +
			"  public static <V extends J<V, W>, W extends J<V, W>> void test(final Y<V, W> a)\n" +
			"  {\n" +
			"    foo(a);\n" +
			"  }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163590
public void test047() {
	this.runNegativeTest(
 		// test directory preparation
		new String[] { /* test files */
			"X.java",
			"public class X<T extends I & J> {\n" +
			"  void foo(T t) {\n" +
			"  }\n" +
			"}\n" +
			"interface I {\n" +
			"  public int method();\n" +
			"}\n" +
			"interface J {\n" +
			"  public boolean method();\n" +
			"}\n"
		},
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X<T extends I & J> {\n" +
		"	               ^\n" +
		"The return types are incompatible for the inherited methods I.method(), J.method()\n" +
		"----------\n",
		// javac options
	  	JavacTestOptions.JavacHasABug.JavacBug5061359 /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163590
// Variant: javac complains as well if we attempt to use method, but noone
// complains upon bar or CONSTANT.
public void test048() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T extends I & J> {\n" +
			"  void foo(T t) {\n" +
			"    t.method();\n" +
			"    t.bar();\n" +
			"    if (t.CONSTANT > 0);\n" +
			"  }\n" +
			"}\n" +
			"interface I {\n" +
			"  public int method();\n" +
			"  void bar();\n" +
			"}\n" +
			"interface J {\n" +
			"  public boolean method();\n" +
			"  static final int CONSTANT = 0;\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X<T extends I & J> {\n" +
		"	               ^\n" +
		"The return types are incompatible for the inherited methods I.method(), J.method()\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	t.method();\n" +
		"	  ^^^^^^\n" +
		"The method method() is ambiguous for the type T\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 5)\n" +
		"	if (t.CONSTANT > 0);\n" +
		"	      ^^^^^^^^\n" +
		"The static field J.CONSTANT should be accessed in a static way\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163590
// can't implement both interfaces though
public void test049() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I {\n" +
			"  public int method();\n" +
			"}\n" +
			"interface J {\n" +
			"  public boolean method();\n" +
			"}\n" +
			"class X implements I, J {\n" +
			"  public int method() {\n" +
			"    return 0;\n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	public int method() {\n" +
		"	       ^^^\n" +
		"The return type is incompatible with J.method()\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163590
// variant: secure the legal case
public void test050() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T extends I & J> {\n" +
			"  void foo(T t) {\n" +
			"  }\n" +
			"}\n" +
			"interface I {\n" +
			"  public int method();\n" +
			"}\n" +
			"interface J {\n" +
			"  public int method();\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166355
public void test051() {
	this.runNegativeTest(
		false /* skipJavac */,
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
				null : JavacTestOptions.EclipseHasABug.EclipseBug427719,
		new String[] { /* test files */
			"X.java",
			"interface I<T> {\n" +
			"}\n" +
			"class Y {\n" +
			"  void bar(I<?> x) {\n" +
			"  }\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"  void foo() {\n" +
			"    bar(new Z());\n" +
			"  }\n" +
			"  void bar(Z x) {\n" +
			"  }\n" +
			"  private static final class Z implements I {\n" +
			"  }\n" +
			"}\n"
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	bar(new Z());\n" +
		"	^^^\n" +
		"The method bar(X.Z) is ambiguous for the type X\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 13)\n" +
		"	private static final class Z implements I {\n" +
		"	                                        ^\n" +
		"I is a raw type. References to generic type I<T> should be parameterized\n" +
		"----------\n"
		: this.complianceLevel < ClassFileConstants.JDK11 ?
			// in 1.8 bar(Z) is recognized as being more specific than bar(I<#RAW>)
			"----------\n" +
			"1. WARNING in X.java (at line 9)\n" +
			"	bar(new Z());\n" +
			"	    ^^^^^^^\n" +
			"Access to enclosing constructor X.Z() is emulated by a synthetic accessor method\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 13)\n" +
			"	private static final class Z implements I {\n" +
			"	                                        ^\n" +
			"I is a raw type. References to generic type I<T> should be parameterized\n" +
			"----------\n"
			:
				"----------\n" +
				"1. WARNING in X.java (at line 13)\n" +
				"	private static final class Z implements I {\n" +
				"	                                        ^\n" +
				"I is a raw type. References to generic type I<T> should be parameterized\n" +
				"----------\n"));
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166355
// variant
public void test052() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface I<T> {\n" +
			"}\n" +
			"class Y {\n" +
			"  void bar(I<?> x) {\n" +
			"  }\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"  void foo() {\n" +
			"    bar(new Z());\n" +
			"  }\n" +
			"  void bar(Z x) {\n" +
			"  }\n" +
			"  private static final class Z implements I<String> {\n" +
			"  }\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166355
// variant
public void test053() {
	this.runNegativeTest(
		false /* skipJavac */,
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
				null : JavacTestOptions.EclipseHasABug.EclipseBug427719,
		new String[] { /* test files */
			"X.java",
			"interface I<T> {\n" +
			"}\n" +
			"class Y {\n" +
			"  void bar(I<?> x) {\n" +
			"  }\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"  void foo() {\n" +
			"    bar(new Z(){});\n" +
			"  }\n" +
			"  void bar(Z x) {\n" +
			"  }\n" +
			"  private static class Z implements I {\n" +
			"  }\n" +
			"}\n"
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	bar(new Z(){});\n" +
		"	^^^\n" +
		"The method bar(X.Z) is ambiguous for the type X\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 13)\n" +
		"	private static class Z implements I {\n" +
		"	                                  ^\n" +
		"I is a raw type. References to generic type I<T> should be parameterized\n" +
		"----------\n"
		:  this.complianceLevel < ClassFileConstants.JDK11 ?
			// in 1.8 bar(Z) is recognized as being more specific than bar(I<#RAW>)
			"----------\n" +
			"1. WARNING in X.java (at line 9)\n" +
			"	bar(new Z(){});\n" +
			"	        ^^^\n" +
			"Access to enclosing constructor X.Z() is emulated by a synthetic accessor method\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 13)\n" +
			"	private static class Z implements I {\n" +
			"	                                  ^\n" +
			"I is a raw type. References to generic type I<T> should be parameterized\n" +
			"----------\n" :
				"----------\n" +
				"1. WARNING in X.java (at line 13)\n" +
				"	private static class Z implements I {\n" +
				"	                                  ^\n" +
				"I is a raw type. References to generic type I<T> should be parameterized\n" +
				"----------\n"));
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166355
// variant
public void test054() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void bar(Z x) {\n" +
			"    System.out.println(\"bar(Z)\");\n" +
			"  }\n" +
			"  void bar(I<?> x) {\n" +
			"    System.out.println(\"bar(I)\");\n" +
			"  }\n" +
			"  public static void main(String args[]) {\n" +
			"    (new X()).bar(new Z());\n" +
			"  }\n" +
			"}\n" +
			"interface I<T> {}\n" +
			"class Z implements I<Object> {}"
		},
		"bar(Z)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166355
// variant
public void _test055() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I<T> {}\n" +
			"class X {\n" +
			"  void bar(Z x) {\n" +
			"    System.out.println(\"bar(Z)\");\n" +
			"  }\n" +
			"  void bar(I<?> x) {\n" +
			"    System.out.println(\"bar(I)\");\n" +
			"  }\n" +
			"  public static void main(String args[]) {\n" +
			"    (new X()).bar(new Z());\n" +
			"  }\n" +
			"}\n" +
			"class Z implements I {}"
		},
		"ERR");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184190
public void test056() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"  void bar(X x) {\n" +
			"    ZA z = ZA.foo(x);\n" +
			"    z.toString();\n" +
			"  }\n" +
			"}\n" +
			"class Y<T> {\n" +
			"  public static <U> Y<U> foo(X<U> x) {\n" +
			"    return null;\n" +
			"  }\n" +
			"}\n" +
			"class YA<T extends A> extends Y<T> {\n" +
			"  public static <U extends A> YA<U> foo(X<U> x) {\n" +
			"    return (YA<U>) Y.foo(x);\n" +
			"  }\n" +
			"}\n" +
			"class ZA<T extends B> extends YA<T> {\n" +
			"  public static <U extends B> ZA<U> foo(X<U> x) {\n" +
			"    return (ZA<U>) Y.foo(x);\n" +
			"  }\n" +
			"}\n" +
			"abstract class A  {\n" +
			"}\n" +
			"abstract class B extends A {\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=186382
public void test057() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends Y {\n" +
			"	@Override <T4, G4 extends I<T4>> T4 foo(G4 g) { return super.foo(g); }\n" +
			"}\n" +
			"class Y extends Z {\n" +
			"	@Override <T3, G3 extends I<T3>> T3 foo(G3 g) { return super.foo(g); }\n" +
			"}\n" +
			"class Z {\n" +
			"	<T2, G2 extends I<T2>> T2 foo(G2 g) { return null; }\n" +
			"}\n" +
			"interface I<T1> {}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=188741
public void test058() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends A {\n" +
			"	void x(G g) { System.out.print(1); }\n" +
			"	void x(G g, G g2) { System.out.print(1); }\n" +
			"	public static void main(String[] s) {\n" +
			"		H h = new H();\n" +
			"		new X().x(h);\n" +
			"		new X().x(h, h);\n" +
			"	}\n" +
			"}\n" +
			"class A<T> {\n" +
			"	void x(T t) { System.out.print(2); }\n" +
			"	<U> void x(T t, U u) { System.out.print(2); }\n" +
			"}\n" +
			"class F<T> {}\n" +
			"class G<T> extends F<T> {}\n" +
			"class H<T> extends G<T> {}"
		},
		"11");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=188741
public void test058a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends java.util.ArrayList {\n" +
			"	private static final long serialVersionUID = 1L;\n" +
			"	public void add(Comparable o) {}\n" +
			"	public void test() { add(\"hello\"); }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=188960
public void test059() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X<T> {\n" +
			"	X() {}\n" +
			"	X(String s) {}\n" +
			"	X(T t) {}\n" +
			"	void foo(String s) {}\n" +
			"	void foo(T t) {}\n" +
			"}\n" +
			"class NoErrorSubclass extends X<String> {}\n" +
			"class StringOnlySubClass extends X<String> {\n" +
			"	StringOnlySubClass(String s) { super(s); }\n" +
			"	@Override void foo(String s) { super.foo(s); }\n" +
			"}\n" +
			"class Test {\n" +
			"	Object o = new X<String>(\"xyz\");\n" +
			"	void test(X<String> x) { x.foo(\"xyz\"); }\n" +
			"}"
		},
		// no error is reported against duplicate constructors - but the call is reported as ambiguous
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	class NoErrorSubclass extends X<String> {}\n" +
		"	      ^^^^^^^^^^^^^^^\n" +
		"Duplicate methods named foo with the parameters (T) and (String) are defined by the type X<String>\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	StringOnlySubClass(String s) { super(s); }\n" +
		"	                               ^^^^^^^^^\n" +
		"The constructor X<String>(String) is ambiguous\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 11)\n" +
		"	@Override void foo(String s) { super.foo(s); }\n" +
		"	                                     ^^^\n" +
		"The method foo(String) is ambiguous for the type X<String>\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 14)\n" +
		"	Object o = new X<String>(\"xyz\");\n" +
		"	           ^^^^^^^^^^^^^^^^^^^^\n" +
		"The constructor X<String>(String) is ambiguous\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 15)\n" +
		"	void test(X<String> x) { x.foo(\"xyz\"); }\n" +
		"	                           ^^^\n" +
		"The method foo(String) is ambiguous for the type X<String>\n" +
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=191029
public void test059a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.TreeMap;\n" +
			"class X {\n" +
			"	void test(TreeMap<String, Object> tm) {\n" +
			"		TreeMap copy = new TreeMap(tm);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	TreeMap copy = new TreeMap(tm);\n" +
		"	^^^^^^^\n" +
		"TreeMap is a raw type. References to generic type TreeMap<K,V> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	TreeMap copy = new TreeMap(tm);\n" +
		"	               ^^^^^^^^^^^^^^^\n" +
		"Type safety: The constructor TreeMap(SortedMap) belongs to the raw type TreeMap. References to generic type TreeMap<K,V> should be parameterized\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 4)\n" +
		"	TreeMap copy = new TreeMap(tm);\n" +
		"	                   ^^^^^^^\n" +
		"TreeMap is a raw type. References to generic type TreeMap<K,V> should be parameterized\n" +
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=189933
public void test060() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public void bar(K<T, Object> p) {\n" +
			"		new Y(p);\n" +
			"		new Y((J<T, Object>) p);\n" +
			"		new Y((I<T, Object>) p);\n" +
			"	}\n" +
			"}\n" +
			"class Y<T, U> {\n" +
			"	Y(I<? extends T, ? extends U> p) {}\n" +
			"	Y(J<T, ? extends U> p) {}\n" +
			"}\n" +
			"interface I<T, U> {}\n" +
			"interface J<T, U> extends I<T, U> {}\n" +
			"interface K<T, U> extends I<T, U>, J<T, U> {}"
		},
		""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=189933
// variant
public void test061() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X<T> {\n" +
			"	public void bar(K<T, Object> p) {\n" +
			"		new Y(p);\n" +
			"		new Y((J<T, Object>) p);\n" +
			"		new Y((I<T, Object>) p);\n" +
			"	}\n" +
			"}\n" +
			"class Y<T, U> {\n" +
			"	Y(I<? extends T, ? extends U> p) {}\n" +
			"	Y(J<T, ? extends U> p) {}\n" +
			"}\n" +
			"interface I<T, U> {}\n" +
			"interface J<T, U> {}\n" +
			"interface K<T, U> extends I<T, U>, J<T, U> {}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	new Y(p);\n" +
		"	^^^^^^^^\n" +
		"The constructor Y(I) is ambiguous\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 3)\n" +
		"	new Y(p);\n" +
		"	    ^\n" +
		"Y is a raw type. References to generic type Y<T,U> should be parameterized\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 4)\n" +
		"	new Y((J<T, Object>) p);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The constructor Y(J) belongs to the raw type Y. References to generic type Y<T,U> should be parameterized\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 4)\n" +
		"	new Y((J<T, Object>) p);\n" +
		"	    ^\n" +
		"Y is a raw type. References to generic type Y<T,U> should be parameterized\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 5)\n" +
		"	new Y((I<T, Object>) p);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The constructor Y(I) belongs to the raw type Y. References to generic type Y<T,U> should be parameterized\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 5)\n" +
		"	new Y((I<T, Object>) p);\n" +
		"	    ^\n" +
		"Y is a raw type. References to generic type Y<T,U> should be parameterized\n" +
		"----------\n"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=193265
public void test062() {
	this.runConformTest(
		new String[] {
			"X.java",
			"enum E implements I {\n" +
			"	F;\n" +
			"}\n" +
			"interface I {}\n" +
			"interface Spec {\n" +
			"	<T1 extends Enum<T1> & I> void method(T1 t);\n" +
			"}\n" +
			"abstract class X implements Spec {\n" +
			"	public <T2 extends Enum<T2> & I> void method(T2 t) {}\n" +
			"	void test() { method(E.F); }\n" +
			"}"
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=196254
public void test063() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"interface I<R> {}\n" +
			"class X<T extends I> {\n" +
			"	void method(X<?> that) {}\n" +
			"}\n" +
			"class Y<T extends I> extends X<T> {\n" +
			"	@Override void method(X<? extends I> that) { System.out.print(1); }\n" +
			"}\n" +
			"public class Test {\n" +
			"	public static void main(String[] args) { new Y().method((X) null); }\n" +
			"}"
		},
		"1"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=198120
public void test064() {
	this.runConformTest(
		new String[] {
			"A.java",
			"interface I<E> {\n" +
			"	void x(I<? extends E> i);\n" +
			"}\n" +
			"public abstract class A implements I {\n" +
			"	public void x(I i) {}\n" +
			"}\n" +
			"class B extends A {\n" +
			"	void y(A a) { super.x(a); }\n" +
			"}"
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200547
public void test065() {
	this.runConformTest(
		new String[] {
			"A.java",
			"public abstract class A {\n" +
			"	abstract <T extends Number & Comparable<T>> void m(T x);\n" +
			"}\n" +
			"class B extends A {\n" +
			"	@Override <T extends Number & Comparable<T>> void m(T x) {}\n" +
			"}"
		},
		""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=214558
public void test066() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"import java.util.*;\n" +
			"public class A {\n" +
			"	void foo(Collection<Object[]> c) {}\n" +
			"	void foo(Collection<Object[]> c, Object o) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		new B().foo(new ArrayList());\n" +
			"		new B().foo(new ArrayList(), args[0]);\n" +
			"	}\n" +
			"}\n" +
			"class B extends A {\n" +
			"	void foo(ArrayList a) {}\n" +
			"	void foo(ArrayList a, Object o) {}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 6)\n" +
		"	new B().foo(new ArrayList());\n" +
		"	        ^^^\n" +
		"The method foo(ArrayList) is ambiguous for the type B\n" +
		"----------\n" +
		"2. WARNING in A.java (at line 6)\n" +
		"	new B().foo(new ArrayList());\n" +
		"	                ^^^^^^^^^\n" +
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
		"----------\n" +
		"3. ERROR in A.java (at line 7)\n" +
		"	new B().foo(new ArrayList(), args[0]);\n" +
		"	        ^^^\n" +
		"The method foo(ArrayList, Object) is ambiguous for the type B\n" +
		"----------\n" +
		"4. WARNING in A.java (at line 7)\n" +
		"	new B().foo(new ArrayList(), args[0]);\n" +
		"	                ^^^^^^^^^\n" +
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
		"----------\n" +
		"5. WARNING in A.java (at line 11)\n" +
		"	void foo(ArrayList a) {}\n" +
		"	         ^^^^^^^^^\n" +
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
		"----------\n" +
		"6. WARNING in A.java (at line 12)\n" +
		"	void foo(ArrayList a, Object o) {}\n" +
		"	         ^^^^^^^^^\n" +
		"ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized\n" +
		"----------\n"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=214558 - positive case
public void test067() {
	this.runConformTest(
		new String[] {
			"A.java",
			"import java.util.*;\n" +
			"public class A {\n" +
			"	void foo(Collection<Object[]> c) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		new B().foo(new ArrayList<Object>());\n" +
			"	}\n" +
			"}\n" +
			"class B extends A {\n" +
			"	void foo(ArrayList<Object> a) {System.out.print(1);}\n" +
			"}"
		},
		"1"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279
public void test068() {
	this.runConformTest(
		new String[] {
			"X.java",
			"interface A { X<? extends A> foo(); }\n" +
			"interface B extends A { X<? extends B> foo(); }\n" +
			"interface C extends B, A {}\n" +
			"interface D extends A, B {}\n" +
			"public class X<T> {\n" +
			"	public void bar() {\n" +
			"		C c = null;\n" +
			"		X<? extends B> c_b = c.foo();\n" +
			"		D d = null;\n" +
			"		 X<? extends B> d_b = d.foo();\n" +
			"	}\n" +
			"}"
		},
		""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279 - variation
public void test069() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface A { X<? extends B> foo(); }\n" +
			"interface B extends A { X<? extends A> foo(); }\n" +
			"interface C extends B, A {}\n" +
			"interface D extends A, B {}\n" +
			"public class X<T> {\n" +
			"	void test(C c, D d) {\n" +
			"		X<? extends B> c_b = c.foo();\n" +
			"		 X<? extends B> d_b = d.foo();\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	interface B extends A { X<? extends A> foo(); }\n" +
		"	                        ^^^^^^^^^^^^^^\n" +
		"The return type is incompatible with A.foo()\n" +
		"----------\n"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279 - variation
public void test070() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface A { X<? extends A> foo(); }\n" +
			"interface B { X<? extends B> foo(); }\n" +
			"interface C extends B, A {}\n" +
			"interface D extends A, B {}\n" +
			"public class X<T> {\n" +
			"	public static void main(String[] args) {\n" +
			"		C c = null;\n" +
			"		X<? extends B> c_b = c.foo();\n" +
			"		D d = null;\n" +
			"		 X<? extends B> d_b = d.foo();\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	interface C extends B, A {}\n" +
		"	          ^\n" +
		"The return types are incompatible for the inherited methods B.foo(), A.foo()\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	interface D extends A, B {}\n" +
		"	          ^\n" +
		"The return types are incompatible for the inherited methods A.foo(), B.foo()\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 8)\n" +
		"	X<? extends B> c_b = c.foo();\n" +
		"	                       ^^^\n" +
		"The method foo() is ambiguous for the type C\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 10)\n" +
		"	X<? extends B> d_b = d.foo();\n" +
		"	                       ^^^\n" +
		"The method foo() is ambiguous for the type D\n" +
		"----------\n"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279 - variation
public void test071() {
	this.runConformTest(
		new String[] {
			"Y.java",
			"interface I {\n" +
			"	Integer a();\n" +
			"	Float b();\n" +
			"}\n" +
			"interface J {\n" +
			"	Integer a();\n" +
			"	Double c();\n" +
			"}\n" +
			"abstract class X {\n" +
			"	public abstract Float b();\n" +
			"	public Double c() { return null; }\n" +
			"}\n" +
			"abstract class Y extends X implements I, J {\n" +
			"	void test() {\n" +
			"		Integer i = a();\n" +
			"		Float f = b();\n" +
			"		Double d = c();\n" +
			"	}\n" +
			"}"
		},
		""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279 - variation
public void test072() {
	this.runConformTest(
		new String[] {
			"Y.java",
			"interface I {\n" +
			"	Number a();\n" +
			"	Number b();\n" +
			"}\n" +
			"interface J {\n" +
			"	Integer a();\n" +
			"	Number c();\n" +
			"}\n" +
			"abstract class X {\n" +
			"	public abstract Float b();\n" +
			"	public Double c() { return null; }\n" +
			"}\n" +
			"abstract class Y extends X implements I, J {\n" +
			"	void test() {\n" +
			"		Integer i = a();\n" +
			"		Float f = b();\n" +
			"		Double d = c();\n" +
			"	}\n" +
			"}\n" +
			"abstract class Y2 extends X implements J, I {\n" +
			"	void test() {\n" +
			"		Integer i = a();\n" +
			"		Float f = b();\n" +
			"		Double d = c();\n" +
			"	}\n" +
			"}"
		},
		"" // javac reports 4 ambiguous errors, 2 each of a() & b() even tho the return types are sustitutable
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251279 - variation
public void test073() {
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"interface I {\n" +
			"	int a();\n" +
			"	int b();\n" +
			"}\n" +
			"interface J {\n" +
			"	byte a();\n" +
			"	int c();\n" +
			"}\n" +
			"abstract class X {\n" +
			"	public abstract byte b();\n" +
			"	public byte c() { return 1; }\n" +
			"}\n" +
			"abstract class Y extends X implements I, J {\n" +
			"	void test() {\n" +
			"		byte a = a();\n" +
			"		byte b = b();\n" +
			"		byte c = c();\n" +
			"	}\n" +
			"}\n" +
			"abstract class Y2 extends X implements J, I {\n" +
			"	void test() {\n" +
			"		byte a = a();\n" +
			"		byte b = b();\n" +
			"		byte c = c();\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 13)\n" +
		"	abstract class Y extends X implements I, J {\n" +
		"	               ^\n" +
		"The return types are incompatible for the inherited methods J.c(), X.c()\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 13)\n" +
		"	abstract class Y extends X implements I, J {\n" +
		"	               ^\n" +
		"The return types are incompatible for the inherited methods I.b(), X.b()\n" +
		"----------\n" +
		"3. ERROR in Y.java (at line 13)\n" +
		"	abstract class Y extends X implements I, J {\n" +
		"	               ^\n" +
		"The return types are incompatible for the inherited methods I.a(), J.a()\n" +
		"----------\n" +
		"4. ERROR in Y.java (at line 20)\n" +
		"	abstract class Y2 extends X implements J, I {\n" +
		"	               ^^\n" +
		"The return types are incompatible for the inherited methods J.c(), X.c()\n" +
		"----------\n" +
		"5. ERROR in Y.java (at line 20)\n" +
		"	abstract class Y2 extends X implements J, I {\n" +
		"	               ^^\n" +
		"The return types are incompatible for the inherited methods I.b(), X.b()\n" +
		"----------\n" +
		"6. ERROR in Y.java (at line 20)\n" +
		"	abstract class Y2 extends X implements J, I {\n" +
		"	               ^^\n" +
		"The return types are incompatible for the inherited methods J.a(), I.a()\n" +
		"----------\n"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=206930
public void test074() {
	this.runNegativeTest(
		false /* skipJavac */,
		this.complianceLevel < ClassFileConstants.JDK1_8 ?
				null : JavacTestOptions.EclipseHasABug.EclipseBug427719,
		new String[] {
			"Y.java",
			"interface I<T> {}\n" +
			"class A {\n" +
			"	void a(I x) {}\n" +
			"	void b(I<?> x) {}\n" +
			"	void b(I<?>[] x) {}\n" +
			"	<U> void c(I<?> x) {}\n" +
			"}\n" +
			"class B extends A {}\n" +
			"class C extends B implements I {\n" +
			"	void a(C c) {}\n" +
			"	void b(C c) {}\n" +
			"	void b(C[] c) {}\n" +
			"	void c(C c) {}\n" +
			"}\n" +
			"class D extends C {\n" +
			"    void test() {\n" +
			"        a(new C());\n" +
			"        a(new D());\n" +
			"        b(new C());\n" + // ambiguous b(I<?>) in A and b(C) in C match
			"        b(new D());\n" + // ambiguous b(I<?>) in A and b(C) in C match
			"        b(new C[0]);\n" + // ambiguous b(I<?>[]) in A and b(C[]) in C match
			"        b(new D[0]);\n" + // ambiguous b(I<?>[]) in A and b(C[]) in C match
			"        c(new C());\n" + // ambiguous <U>c(I<?>) in A and c(C) in C match
			"        c(new D());\n" + // ambiguous <U>c(I<?>) in A and c(C) in C match
			"    }\n" +
			"}\n" +
			"class A2<T> {\n" +
			"	void a(I x) {}\n" +
			"	void b(I<?> x) {}\n" +
			"	<U> void c(I<?> x) {}\n" +
			"	void d(I<T> x) {}\n" +
			"}\n" +
			"class B2 extends A2 {}\n" +
			"class C2 extends B2 implements I {\n" +
			"	void a(C2 c) {}\n" +
			"	void b(C2 c) {}\n" +
			"	void c(C2 c) {}\n" +
			"	void d(C2 c) {}\n" +
			"}\n" +
			"class D2 extends C2 {\n" +
			"    void test() {\n" +
			"        a(new C2());\n" +
			"        a(new D2());\n" +
			"        b(new C2());\n" +
			"        b(new D2());\n" +
			"        c(new C2());\n" +
			"        c(new D2());\n" +
			"        d(new C2());\n" +
			"        d(new D2());\n" +
			"    }\n" +
			"}\n" +
			"public class Y {}\n"
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"----------\n" +
		"1. WARNING in Y.java (at line 3)\n" +
		"	void a(I x) {}\n" +
		"	       ^\n" +
		"I is a raw type. References to generic type I<T> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in Y.java (at line 9)\n" +
		"	class C extends B implements I {\n" +
		"	                             ^\n" +
		"I is a raw type. References to generic type I<T> should be parameterized\n" +
		"----------\n" +
		"3. ERROR in Y.java (at line 19)\n" +
		"	b(new C());\n" +
		"	^\n" +
		"The method b(C) is ambiguous for the type D\n" +
		"----------\n" +
		"4. ERROR in Y.java (at line 20)\n" +
		"	b(new D());\n" +
		"	^\n" +
		"The method b(C) is ambiguous for the type D\n" +
		"----------\n" +
		"5. ERROR in Y.java (at line 21)\n" +
		"	b(new C[0]);\n" +
		"	^\n" +
		"The method b(C[]) is ambiguous for the type D\n" +
		"----------\n" +
		"6. ERROR in Y.java (at line 22)\n" +
		"	b(new D[0]);\n" +
		"	^\n" +
		"The method b(C[]) is ambiguous for the type D\n" +
		"----------\n" +
		"7. ERROR in Y.java (at line 23)\n" +
		"	c(new C());\n" +
		"	^\n" +
		"The method c(C) is ambiguous for the type D\n" +
		"----------\n" +
		"8. ERROR in Y.java (at line 24)\n" +
		"	c(new D());\n" +
		"	^\n" +
		"The method c(C) is ambiguous for the type D\n" +
		"----------\n" +
		"9. WARNING in Y.java (at line 28)\n" +
		"	void a(I x) {}\n" +
		"	       ^\n" +
		"I is a raw type. References to generic type I<T> should be parameterized\n" +
		"----------\n" +
		"10. WARNING in Y.java (at line 33)\n" +
		"	class B2 extends A2 {}\n" +
		"	                 ^^\n" +
		"A2 is a raw type. References to generic type A2<T> should be parameterized\n" +
		"----------\n" +
		"11. WARNING in Y.java (at line 34)\n" +
		"	class C2 extends B2 implements I {\n" +
		"	                               ^\n" +
		"I is a raw type. References to generic type I<T> should be parameterized\n" +
		"----------\n"
		: // no ambiguities in 1.8
			"----------\n" +
			"1. WARNING in Y.java (at line 3)\n" +
			"	void a(I x) {}\n" +
			"	       ^\n" +
			"I is a raw type. References to generic type I<T> should be parameterized\n" +
			"----------\n" +
			"2. WARNING in Y.java (at line 9)\n" +
			"	class C extends B implements I {\n" +
			"	                             ^\n" +
			"I is a raw type. References to generic type I<T> should be parameterized\n" +
			"----------\n" +
			"3. WARNING in Y.java (at line 28)\n" +
			"	void a(I x) {}\n" +
			"	       ^\n" +
			"I is a raw type. References to generic type I<T> should be parameterized\n" +
			"----------\n" +
			"4. WARNING in Y.java (at line 33)\n" +
			"	class B2 extends A2 {}\n" +
			"	                 ^^\n" +
			"A2 is a raw type. References to generic type A2<T> should be parameterized\n" +
			"----------\n" +
			"5. WARNING in Y.java (at line 34)\n" +
			"	class C2 extends B2 implements I {\n" +
			"	                               ^\n" +
			"I is a raw type. References to generic type I<T> should be parameterized\n" +
			"----------\n")
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=266421
public void test075() {
	this.runNegativeTest(
		new String[] {
			"C.java",
			"abstract class A<T extends Comparable> {\n" +
			"	abstract int x(T val);\n" +
			"}\n" +
			"class B<T extends Comparable> extends A<T> {\n" +
			"	@Override int x(T val) { return 0; }\n" +
			"}\n" +
			"class C extends B<Double> {\n" +
			"    int test(Double val) { return x(val); }\n" +
			"}"
		},
		"----------\n" +
		"1. WARNING in C.java (at line 1)\n" +
		"	abstract class A<T extends Comparable> {\n" +
		"	                           ^^^^^^^^^^\n" +
		"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in C.java (at line 4)\n" +
		"	class B<T extends Comparable> extends A<T> {\n" +
		"	                  ^^^^^^^^^^\n" +
		"Comparable is a raw type. References to generic type Comparable<T> should be parameterized\n" +
		"----------\n"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=268837
// See that this test case exhibits the bug 345947
public void test076() {
	String output = (this.complianceLevel == ClassFileConstants.JDK1_6)?
			"----------\n" +
			"1. WARNING in X.java (at line 8)\n" +
			"	<U> J<String> b();\n" +
			"	              ^^^\n" +
			"Name clash: The method b() of type J<E> has the same erasure as b() of type I<E> but does not override it\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 15)\n" +
			"	J<Integer> b = ints.a();\n" +
			"	               ^^^^^^^^\n" +
			"Type mismatch: cannot convert from J<String> to J<Integer>\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 16)\n" +
			"	J<Object> c = ints.a();\n" +
			"	              ^^^^^^^^\n" +
			"Type mismatch: cannot convert from J<String> to J<Object>\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 17)\n" +
			"	J d = ints.a();\n" +
			"	^\n" +
			"J is a raw type. References to generic type J<E> should be parameterized\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 19)\n" +
			"	I<Integer> f = ints.a();\n" +
			"	               ^^^^^^^^\n" +
			"Type mismatch: cannot convert from J<String> to I<Integer>\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 20)\n" +
			"	I<Object> g = ints.a();\n" +
			"	              ^^^^^^^^\n" +
			"Type mismatch: cannot convert from J<String> to I<Object>\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 21)\n" +
			"	I h = ints.a();\n" +
			"	^\n" +
			"I is a raw type. References to generic type I<E> should be parameterized\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 24)\n" +
			"	ints.b();\n" +
			"	     ^\n" +
			"The method b() is ambiguous for the type J<Integer>\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 25)\n" +
			"	J<String> a = ints.b();\n" +
			"	                   ^\n" +
			"The method b() is ambiguous for the type J<Integer>\n" +
			"----------\n" +
			"10. ERROR in X.java (at line 26)\n" +
			"	J<Integer> b = ints.b();\n" +
			"	                    ^\n" +
			"The method b() is ambiguous for the type J<Integer>\n" +
			"----------\n" +
			"11. ERROR in X.java (at line 27)\n" +
			"	J<Object> c = ints.b();\n" +
			"	                   ^\n" +
			"The method b() is ambiguous for the type J<Integer>\n" +
			"----------\n" +
			"12. WARNING in X.java (at line 28)\n" +
			"	J d = ints.b();\n" +
			"	^\n" +
			"J is a raw type. References to generic type J<E> should be parameterized\n" +
			"----------\n" +
			"13. ERROR in X.java (at line 28)\n" +
			"	J d = ints.b();\n" +
			"	           ^\n" +
			"The method b() is ambiguous for the type J<Integer>\n" +
			"----------\n" +
			"14. ERROR in X.java (at line 29)\n" +
			"	I<String> e = ints.b();\n" +
			"	                   ^\n" +
			"The method b() is ambiguous for the type J<Integer>\n" +
			"----------\n" +
			"15. ERROR in X.java (at line 30)\n" +
			"	I<Integer> f = ints.b();\n" +
			"	                    ^\n" +
			"The method b() is ambiguous for the type J<Integer>\n" +
			"----------\n" +
			"16. ERROR in X.java (at line 31)\n" +
			"	I<Object> g = ints.b();\n" +
			"	                   ^\n" +
			"The method b() is ambiguous for the type J<Integer>\n" +
			"----------\n" +
			"17. WARNING in X.java (at line 32)\n" +
			"	I h = ints.b();\n" +
			"	^\n" +
			"I is a raw type. References to generic type I<E> should be parameterized\n" +
			"----------\n" +
			"18. ERROR in X.java (at line 32)\n" +
			"	I h = ints.b();\n" +
			"	           ^\n" +
			"The method b() is ambiguous for the type J<Integer>\n" +
			"----------\n" +
			"19. WARNING in X.java (at line 39)\n" +
			"	J d = ints.c();\n" +
			"	^\n" +
			"J is a raw type. References to generic type J<E> should be parameterized\n" +
			"----------\n" +
			"20. WARNING in X.java (at line 43)\n" +
			"	I h = ints.c();\n" +
			"	^\n" +
			"I is a raw type. References to generic type I<E> should be parameterized\n" +
			"----------\n":
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	<U> J<String> b();\n" +
				"	              ^^^\n" +
				"Name clash: The method b() of type J<E> has the same erasure as b() of type I<E> but does not override it\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 15)\n" +
				"	J<Integer> b = ints.a();\n" +
				"	               ^^^^^^^^\n" +
				"Type mismatch: cannot convert from J<String> to J<Integer>\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 16)\n" +
				"	J<Object> c = ints.a();\n" +
				"	              ^^^^^^^^\n" +
				"Type mismatch: cannot convert from J<String> to J<Object>\n" +
				"----------\n" +
				"4. WARNING in X.java (at line 17)\n" +
				"	J d = ints.a();\n" +
				"	^\n" +
				"J is a raw type. References to generic type J<E> should be parameterized\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 19)\n" +
				"	I<Integer> f = ints.a();\n" +
				"	               ^^^^^^^^\n" +
				"Type mismatch: cannot convert from J<String> to I<Integer>\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 20)\n" +
				"	I<Object> g = ints.a();\n" +
				"	              ^^^^^^^^\n" +
				"Type mismatch: cannot convert from J<String> to I<Object>\n" +
				"----------\n" +
				"7. WARNING in X.java (at line 21)\n" +
				"	I h = ints.a();\n" +
				"	^\n" +
				"I is a raw type. References to generic type I<E> should be parameterized\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 24)\n" +
				"	ints.b();\n" +
				"	     ^\n" +
				"The method b() is ambiguous for the type J<Integer>\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 25)\n" +
				"	J<String> a = ints.b();\n" +
				"	                   ^\n" +
				"The method b() is ambiguous for the type J<Integer>\n" +
				"----------\n" +
				"10. ERROR in X.java (at line 26)\n" +
				"	J<Integer> b = ints.b();\n" +
				"	                    ^\n" +
				"The method b() is ambiguous for the type J<Integer>\n" +
				"----------\n" +
				"11. ERROR in X.java (at line 27)\n" +
				"	J<Object> c = ints.b();\n" +
				"	                   ^\n" +
				"The method b() is ambiguous for the type J<Integer>\n" +
				"----------\n" +
				"12. WARNING in X.java (at line 28)\n" +
				"	J d = ints.b();\n" +
				"	^\n" +
				"J is a raw type. References to generic type J<E> should be parameterized\n" +
				"----------\n" +
				"13. ERROR in X.java (at line 28)\n" +
				"	J d = ints.b();\n" +
				"	           ^\n" +
				"The method b() is ambiguous for the type J<Integer>\n" +
				"----------\n" +
				"14. ERROR in X.java (at line 29)\n" +
				"	I<String> e = ints.b();\n" +
				"	                   ^\n" +
				"The method b() is ambiguous for the type J<Integer>\n" +
				"----------\n" +
				"15. ERROR in X.java (at line 30)\n" +
				"	I<Integer> f = ints.b();\n" +
				"	                    ^\n" +
				"The method b() is ambiguous for the type J<Integer>\n" +
				"----------\n" +
				"16. ERROR in X.java (at line 31)\n" +
				"	I<Object> g = ints.b();\n" +
				"	                   ^\n" +
				"The method b() is ambiguous for the type J<Integer>\n" +
				"----------\n" +
				"17. WARNING in X.java (at line 32)\n" +
				"	I h = ints.b();\n" +
				"	^\n" +
				"I is a raw type. References to generic type I<E> should be parameterized\n" +
				"----------\n" +
				"18. ERROR in X.java (at line 32)\n" +
				"	I h = ints.b();\n" +
				"	           ^\n" +
				"The method b() is ambiguous for the type J<Integer>\n" +
				"----------\n" +
				"19. WARNING in X.java (at line 39)\n" +
				"	J d = ints.c();\n" +
				"	^\n" +
				"J is a raw type. References to generic type J<E> should be parameterized\n" +
				"----------\n" +
				"20. WARNING in X.java (at line 43)\n" +
				"	I h = ints.c();\n" +
				"	^\n" +
				"I is a raw type. References to generic type I<E> should be parameterized\n" +
				"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I<E> {\n" +
			"	I<String> a();\n" +
			"	I<String> b();\n" +
			"	<T1> I<T1> c();\n" +
			"}\n" +
			"interface J<E> extends I<E> {\n" +
			"	J<String> a();\n" +
			"	<U> J<String> b();\n" +
			"	<T2> J<T2> c();\n" +
			"}\n" +
			"class X {\n" +
			"	void a(J<Integer> ints) {\n" +
			"		ints.a();\n" +
			"		J<String> a = ints.a();\n" +
			"		J<Integer> b = ints.a();\n" + // incompatible types
			"		J<Object> c = ints.a();\n" + // incompatible types
			"		J d = ints.a();\n" +
			"		I<String> e = ints.a();\n" +
			"		I<Integer> f = ints.a();\n" + // incompatible types
			"		I<Object> g = ints.a();\n" + // incompatible types
			"		I h = ints.a();\n" +
			"	}\n" +
			"	void b(J<Integer> ints) {\n" +
			"		ints.b();\n" + // ambiguous
			"		J<String> a = ints.b();\n" + // ambiguous
			"		J<Integer> b = ints.b();\n" + // ambiguous
			"		J<Object> c = ints.b();\n" + // ambiguous
			"		J d = ints.b();\n" + // ambiguous
			"		I<String> e = ints.b();\n" + // ambiguous
			"		I<Integer> f = ints.b();\n" + // ambiguous
			"		I<Object> g = ints.b();\n" + // ambiguous
			"		I h = ints.b();\n" + // ambiguous
			"	}\n" +
			"	void c(J<Integer> ints) {\n" +
			"		ints.c();\n" +
			"		J<String> a = ints.c();\n" +
			"		J<Integer> b = ints.c();\n" +
			"		J<Object> c = ints.c();\n" +
			"		J d = ints.c();\n" +
			"		I<String> e = ints.c();\n" +
			"		I<Integer> f = ints.c();\n" +
			"		I<Object> g = ints.c();\n" +
			"		I h = ints.c();\n" +
			"	}\n" +
			"}"
		},
		output
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=270194
public void test077() {
	this.runConformTest(
		new String[] {
			"X.java",
			"abstract class X implements I {\n" +
			"	public <A extends J<A, D>, D extends J<A, D>> A method(A arg) { return null; }\n" +
			"	void test(Y<String> c) { method(c); }\n" +
			"}\n" +
			"interface I {\n" +
			"	<A extends J<A,D>, D extends J<A,D>> A method(A arg);\n" +
			"}\n" +
			"interface J<A extends J<A,D>, D extends J<A,D>> {}\n" +
			"class Y<E> implements J<Y<E>, Y<E>> {}"
		},
		""
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=287592
public void test078() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	class Field<T> { T value; }\n" +
			"	<T> void a(T value) {}\n" +
			"	<T> void a(Field<T> field) {}\n" +
			"	<T extends Number> void b(T value) {}\n" +
			"	<T> void b(Field<T> field) {}\n" +
			"	void c(String value) {}\n" +
			"	void c(Field<String> field) {}\n" +
			"	void test(X x) {\n" +
			"		x.a(null);\n" +
			"		x.<String>a(null);\n" +
			"		x.b(null);\n" +
			"		x.<Integer>b(null);\n" +
			"		x.c(null);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	x.b(null);\n" +
		"	  ^\n" +
		"The method b(Number) is ambiguous for the type X\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 13)\n" +
		"	x.<Integer>b(null);\n" +
		"	           ^\n" +
		"The method b(Integer) is ambiguous for the type X\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 14)\n" +
		"	x.c(null);\n" +
		"	  ^\n" +
		"The method c(String) is ambiguous for the type X\n" +
		"----------\n"
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=292350
// See that this test case exhibits the bug 345947
public void test079() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface I<T> {}\n" +
			"class A {}\n" +
			"class B extends A {}\n" +
			"interface One {\n" +
			"    I<B> x() throws IllegalAccessError;\n" +
			"    <T extends A> I<T> y() throws IllegalAccessError;\n" +
			"}\n" +
			"interface Two extends One {\n" +
			"    <T extends A> I<T> x() throws IllegalAccessError;\n" +
			"    I<B> y() throws IllegalAccessError;\n" +
			"}\n" +
			"class X {\n" +
			"    void x(Two t) { t.x(); }\n" +
			"    void y(Two t) { t.y(); }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	<T extends A> I<T> x() throws IllegalAccessError;\n" +
		"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Name clash: The method x() of type Two has the same erasure as x() of type One but does not override it\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	I<B> y() throws IllegalAccessError;\n" +
		"	^\n" +
		"Type safety: The return type I<B> for y() from the type Two needs unchecked conversion to conform to I<A> from the type One\n" +
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=293384
public void test080() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<Tout extends Object> {\n" +
			"   static public abstract class BaseA {};\n" +
			"	static public abstract class BaseB extends BaseA {};\n" +
			"	static public class Real extends BaseB {};\n" +
			"	static BaseA ask(String prompt) {\n" +
			"	    Real impl = new Real();\n" +
			"	    return (BaseA) ask(prompt, impl);\n" +
			"	}\n" +
			"	static BaseA ask(String prompt, Real impl) {\n" +
			"	    return null;\n" +
			"	}\n" +
			"	static <T extends BaseA> T ask(String prompt, T impl) {\n" +
			"	    return null;\n" +
			"	}\n" +
			"	static public void main(String[] args) {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}\n"
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test081() {
	this.runConformTest(
		new String[] {
			"C.java",
			"class A<ModelType extends D, ValueType> implements I<ModelType, ValueType> {\n" +
			"    public void doSet(ModelType valueGetter) {\n" +
			"        this.set((ValueType) valueGetter.getObject());\n" +
			"    }\n" +
			"    public void set(Object object) {\n" +
			"        System.out.println(\"In A.set(Object)\");\n" +
			"    }\n" +
			"}\n" +
			"class B extends A<E, CharSequence> {\n" +
			"	public void set(CharSequence string) {\n" +
			"        System.out.println(\"In B.set(CharSequence)\");\n" +
			"    }\n" +
			"}\n" +
			"public class C extends B {\n" +
			"    static public void main(String[] args) {\n" +
			"        C c = new C();\n" +
			"        c.run();\n" +
			"    }\n" +
			"    public void run() {\n" +
			"        E e = new E<String>(String.class);\n" +
			"        this.doSet(e);\n" +
			"    }\n" +
			"}\n" +
			"class D {\n" +
			"    public Object getObject() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"class E<Type extends CharSequence> extends D {\n" +
			"    private Class<Type> typeClass;\n" +
			"    public E(Class<Type> typeClass) {\n" +
			"        this.typeClass = typeClass;\n" +
			"    }\n" +
			"    public Type getObject() {\n" +
			"        try {\n" +
			"            return (Type) typeClass.newInstance();\n" +
			"        } catch (Exception e) {\n" +
			"            throw new RuntimeException(e);\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"interface I<ModelType, ValueType> {\n" +
			"    public void doSet(ModelType model);\n" +
			"    public void set(ValueType value);\n" +
			"}\n"

		},
		"In B.set(CharSequence)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test082() {
	this.runConformTest(
		new String[] {
			"C.java",
			"class A<ModelType extends D, ValueType> extends I<ModelType, ValueType> {\n" +
			"    public void doSet(ModelType valueGetter) {\n" +
			"        this.set((ValueType) valueGetter.getObject());\n" +
			"    }\n" +
			"    public void set(Object object) {\n" +
			"        System.out.println(\"In A.set(Object)\");\n" +
			"    }\n" +
			"}\n" +
			"class B extends A<E, CharSequence> {\n" +
			"	public void set(CharSequence string) {\n" +
			"        System.out.println(\"In B.set(CharSequence)\");\n" +
			"    }\n" +
			"}\n" +
			"public class C extends B {\n" +
			"    static public void main(String[] args) {\n" +
			"        C c = new C();\n" +
			"        c.run();\n" +
			"    }\n" +
			"    public void run() {\n" +
			"        E e = new E<String>(String.class);\n" +
			"        this.doSet(e);\n" +
			"    }\n" +
			"}\n" +
			"class D {\n" +
			"    public Object getObject() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"class E<Type extends CharSequence> extends D {\n" +
			"    private Class<Type> typeClass;\n" +
			"    public E(Class<Type> typeClass) {\n" +
			"        this.typeClass = typeClass;\n" +
			"    }\n" +
			"    public Type getObject() {\n" +
			"        try {\n" +
			"            return (Type) typeClass.newInstance();\n" +
			"        } catch (Exception e) {\n" +
			"            throw new RuntimeException(e);\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"abstract class I<ModelType, ValueType> {\n" +
			"    public abstract void doSet(ModelType model);\n" +
			"    public abstract void set(ValueType value);\n" +
			"}\n"

		},
		"In B.set(CharSequence)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test083() {
	this.runConformTest(
		new String[] {
			"C.java",
			"class A<ModelType extends D, ValueType> implements I<ModelType, ValueType> {\n" +
			"    public void doSet(ModelType valueGetter) {\n" +
			"        this.set((ValueType) valueGetter.getObject());\n" +
			"    }\n" +
			"    public void set(Object object) {\n" +
			"        System.out.println(\"In A.set(Object)\");\n" +
			"    }\n" +
			"}\n" +
			"class B extends A<E, CharSequence> implements I<E, CharSequence> {\n" +
			"	public void set(CharSequence string) {\n" +
			"        System.out.println(\"In B.set(CharSequence)\");\n" +
			"    }\n" +
			"}\n" +
			"public class C extends B {\n" +
			"    static public void main(String[] args) {\n" +
			"        C c = new C();\n" +
			"        c.run();\n" +
			"    }\n" +
			"    public void run() {\n" +
			"        E e = new E<String>(String.class);\n" +
			"        this.doSet(e);\n" +
			"    }\n" +
			"}\n" +
			"class D {\n" +
			"    public Object getObject() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"class E<Type extends CharSequence> extends D {\n" +
			"    private Class<Type> typeClass;\n" +
			"    public E(Class<Type> typeClass) {\n" +
			"        this.typeClass = typeClass;\n" +
			"    }\n" +
			"    public Type getObject() {\n" +
			"        try {\n" +
			"            return (Type) typeClass.newInstance();\n" +
			"        } catch (Exception e) {\n" +
			"            throw new RuntimeException(e);\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"interface I<ModelType, ValueType> {\n" +
			"    public void doSet(ModelType model);\n" +
			"    public void set(ValueType value);\n" +
			"}\n"

		},
		"In B.set(CharSequence)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test084() {
	this.runConformTest(
		new String[] {
			"C.java",
			"abstract class A<ModelType extends D, ValueType> implements I<ModelType, ValueType> {\n" +
			"    public void doSet(ModelType valueGetter) {\n" +
			"        this.set((ValueType) valueGetter.getObject());\n" +
			"    }\n" +
			"    public void set(Object object) {\n" +
			"        System.out.println(\"In A.set(Object)\");\n" +
			"    }\n" +
			"}\n" +
			"class B extends A<E, CharSequence> {\n" +
			"}\n" +
			"public class C extends B {\n" +
			"    static public void main(String[] args) {\n" +
			"        C c = new C();\n" +
			"        c.run();\n" +
			"    }\n" +
			"    public void run() {\n" +
			"        E e = new E<String>(String.class);\n" +
			"        this.doSet(e);\n" +
			"    }\n" +
			"}\n" +
			"class D {\n" +
			"    public Object getObject() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"class E<Type extends CharSequence> extends D {\n" +
			"    private Class<Type> typeClass;\n" +
			"    public E(Class<Type> typeClass) {\n" +
			"        this.typeClass = typeClass;\n" +
			"    }\n" +
			"    public Type getObject() {\n" +
			"        try {\n" +
			"            return (Type) typeClass.newInstance();\n" +
			"        } catch (Exception e) {\n" +
			"            throw new RuntimeException(e);\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"interface I<ModelType, ValueType> {\n" +
			"    public void doSet(ModelType model);\n" +
			"    public void set(ValueType value);\n" +
			"}\n"

		},
		"In A.set(Object)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test085() {
	this.runConformTest(
		new String[] {
			"C.java",
			"class A<ModelType extends D, ValueType> implements I<ModelType, ValueType> {\n" +
			"    public void doSet(ModelType valueGetter) {\n" +
			"        this.set((ValueType) valueGetter.getObject());\n" +
			"    }\n" +
			"    public void set(Object object) {\n" +
			"        System.out.println(\"In A.set(Object)\");\n" +
			"    }\n" +
			"}\n" +
			"class B extends A<E, CharSequence> {\n" +
			"}\n" +
			"public class C extends B {\n" +
			"    static public void main(String[] args) {\n" +
			"        C c = new C();\n" +
			"        c.run();\n" +
			"    }\n" +
			"    public void run() {\n" +
			"        E e = new E<String>(String.class);\n" +
			"        this.doSet(e);\n" +
			"    }\n" +
			"}\n" +
			"class D {\n" +
			"    public Object getObject() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"class E<Type extends CharSequence> extends D {\n" +
			"    private Class<Type> typeClass;\n" +
			"    public E(Class<Type> typeClass) {\n" +
			"        this.typeClass = typeClass;\n" +
			"    }\n" +
			"    public Type getObject() {\n" +
			"        try {\n" +
			"            return (Type) typeClass.newInstance();\n" +
			"        } catch (Exception e) {\n" +
			"            throw new RuntimeException(e);\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"interface I<ModelType, ValueType> {\n" +
			"    public void doSet(ModelType model);\n" +
			"    public void set(ValueType value);\n" +
			"}\n"

		},
		"In A.set(Object)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302358
public void test086() {
	this.runConformTest(
		new String[] {
			"C.java",
			"class A<ModelType extends D, ValueType> {\n" +
			"    public void doSet(ModelType valueGetter) {\n" +
			"        this.set((ValueType) valueGetter.getObject());\n" +
			"    }\n" +
			"    public void set(Object object) {\n" +
			"        System.out.println(\"In A.set(Object)\");\n" +
			"    }\n" +
			"}\n" +
			"class B extends A<E, CharSequence> {\n" +
			"	public void set(CharSequence string) {\n" +
			"        System.out.println(\"In B.set(CharSequence)\");\n" +
			"    }\n" +
			"}\n" +
			"public class C extends B {\n" +
			"    static public void main(String[] args) {\n" +
			"        C c = new C();\n" +
			"        c.run();\n" +
			"    }\n" +
			"    public void run() {\n" +
			"        E e = new E<String>(String.class);\n" +
			"        this.doSet(e);\n" +
			"    }\n" +
			"}\n" +
			"class D {\n" +
			"    public Object getObject() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"class E<Type extends CharSequence> extends D {\n" +
			"    private Class<Type> typeClass;\n" +
			"    public E(Class<Type> typeClass) {\n" +
			"        this.typeClass = typeClass;\n" +
			"    }\n" +
			"    public Type getObject() {\n" +
			"        try {\n" +
			"            return (Type) typeClass.newInstance();\n" +
			"        } catch (Exception e) {\n" +
			"            throw new RuntimeException(e);\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"interface I<ModelType, ValueType> {\n" +
			"    public void doSet(ModelType model);\n" +
			"    public void set(ValueType value);\n" +
			"}\n"

		},
		"In A.set(Object)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321485
public void test087() {
	String source =
		"import java.util.Collection;\n" +
		"import java.util.List;\n" +
		"public class X {\n" +
		"    public static <T> List<T> with(List<? extends T> p) { return null; } \n" +
		"    public static <T> Collection<T> with(Collection<T> p) { return null; }\n" +
		"    static { with(null); }\n" +
		"} \n";
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		this.runNegativeTest( // FIXME: Eclipse has a bug
			new String[] { "X.java", source },
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	static { with(null); }\n" +
			"	         ^^^^\n" +
			"The method with(List<? extends Object>) is ambiguous for the type X\n" +
			"----------\n"
		);
	} else {
		this.runConformTest(
			new String[] { "X.java", source }
		);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=354579
public void test088a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    int foo () { return 0; } \n" +
			"    double foo() { return 0.0; }\n" +
			"} \n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int foo () { return 0; } \n" +
		"	    ^^^^^^\n" +
		"Duplicate method foo() in type X\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	double foo() { return 0.0; }\n" +
		"	       ^^^^^\n" +
		"Duplicate method foo() in type X\n" +
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=354579
public void test088b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public interface X {\n" +
			"    int foo (); \n" +
			"    double foo();\n" +
			"} \n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int foo (); \n" +
		"	    ^^^^^^\n" +
		"Duplicate method foo() in type X\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	double foo();\n" +
		"	       ^^^^^\n" +
		"Duplicate method foo() in type X\n" +
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354579
public void test089() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"public class X {\n" +
			"    int m2(List<Integer> a) {return 0;} \n" +
			"    double m2(List<Integer> b) {return 0.0;}\n" +
			"} \n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	int m2(List<Integer> a) {return 0;} \n" +
		"	    ^^^^^^^^^^^^^^^^^^^\n" +
		"Duplicate method m2(List<Integer>) in type X\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	double m2(List<Integer> b) {return 0.0;}\n" +
		"	       ^^^^^^^^^^^^^^^^^^^\n" +
		"Duplicate method m2(List<Integer>) in type X\n" +
		"----------\n"
	);
}
public void testBug426521() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"import java.util.List;\n" +
			"\n" +
			"class Test {\n" +
			"    <U> void m(List<U> l, U v) { }\n" +
			"\n" +
			"    <V> void m(List<V> l1, List<V> l2) { }\n" +
			"\n" +
			"    void test(List<Object> l) {\n" +
			"        m(l, l); //JDK 6/7 give ambiguity here - EJC compiles ok\n" +
			"    }\n" +
			"}\n"
		},
		this.complianceLevel < ClassFileConstants.JDK1_8 ? "" :
		"----------\n" +
		"1. ERROR in Test.java (at line 9)\n" +
		"	m(l, l); //JDK 6/7 give ambiguity here - EJC compiles ok\n" +
		"	^\n" +
		"The method m(List<Object>, Object) is ambiguous for the type Test\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428845
public void testBug428845() {
	runNegativeTest(
			new String[] {
				"AmbiguousTest.java",
				"import java.io.File;\n" +
				"public class AmbiguousTest {\n" +
				"  static interface IInterface {\n" +
				"    public void method(File file);\n" +
				"  }\n" +
				"  static abstract class AbstractClass implements IInterface {\n" +
				"    public void method(File file) {\n" +
				"      System.err.println(\"file\");\n" +
				"    }\n" +
				"    public void method(String string) {\n" +
				"      System.err.println(\"string\");\n" +
				"    }\n" +
				"  }\n" +
				"  private static AbstractClass newAbstractClass() {\n" +
				"    return new AbstractClass() {};\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    newAbstractClass().method(null);\n" +
				"  }\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in AmbiguousTest.java (at line 18)\n" +
			"	newAbstractClass().method(null);\n" +
			"	                   ^^^^^^\n" +
			"The method method(File) is ambiguous for the type AmbiguousTest.AbstractClass\n" +
			"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=458563 - invalid ambiguous method error on Java 8 that isn't seen on Java 7 (or with javac)
public void testBug458563() {
	runConformTest(
		new String[] {
			"X.java",
			"interface IStoredNode<T> extends INodeHandle<DocumentImpl>, NodeHandle { }\n" +
			"interface NodeHandle extends INodeHandle<DocumentImpl> { }\n" +
			"class DocumentImpl implements INodeHandle<DocumentImpl> {\n" +
			"	public Object getNodeId() {return null;}\n" +
			"}\n" +
			"interface INodeHandle<D> {\n" +
			"    public Object  getNodeId();\n" +
			"}\n" +
			"public class X {\n" +
			"	public void foo(IStoredNode bar) {\n" +
			"		bar.getNodeId();\n" +
			"	}\n" +
			"}"
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=458563 - invalid ambiguous method error on Java 8 that isn't seen on Java 7 (or with javac)
public void testBug458563a() {
	runConformTest(
		new String[] {
			"X.java",
			"interface IStoredNode<T> extends INodeHandle<DocumentImpl>, NodeHandle { }\n" +
			"interface NodeHandle extends INodeHandle<DocumentImpl> { }\n" +
			"class DocumentImpl implements INodeHandle<DocumentImpl> {\n" +
			"	public Object getNodeId() {return null;}\n" +
			"}\n" +
			"interface INodeHandle<D> {\n" +
			"    public Object  getNodeId();\n" +
			"}\n" +
			"public class X {\n" +
			"	public void foo(IStoredNode<?> bar) {\n" +
			"		bar.getNodeId();\n" +
			"	}\n" +
			"}"
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=466730 - Java 8: single method with generics is ambiguous when using import static ...* and inheritance
public void testBug466730() {
	runConformTest(
		new String[] {
			"bug/Base.java",
			"package bug;\n" +
			"public class Base {\n" +
			"	public static Object works() {\n" +
			"        throw new IllegalStateException();\n" +
			"	}\n" +
			"    public static <T> T fails() {\n" +
			"        throw new IllegalStateException();\n" +
			"    }\n" +
			"}\n",
			"bug/Derived.java",
			"package bug;\n" +
			"public class Derived extends Base {}\n",
			"bug/StaticImportBug.java",
			"package bug;\n" +
			"import static bug.Base.*;\n" +
			"import static bug.Derived.*;\n" +
			"public class StaticImportBug {\n" +
			"	void m() {\n" +
			"		java.util.Objects.requireNonNull(works());\n" +
			"		java.util.Objects.requireNonNull(fails());\n" +
			"	}\n" +
			"}\n"
	});
}
}
