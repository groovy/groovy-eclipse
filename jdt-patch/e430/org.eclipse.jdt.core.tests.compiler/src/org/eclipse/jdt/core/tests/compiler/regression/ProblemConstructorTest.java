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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 185682 - Increment/decrement operators mark local variables as read
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class ProblemConstructorTest extends AbstractRegressionTest {

public ProblemConstructorTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public static Class testClass() {
	return ProblemConstructorTest.class;
}

public void test001() {
	this.runNegativeTest(
		new String[] {
			"prs/Test1.java",
			"package prs;	\n" +
			"import java.io.IOException;	\n" +
			"public class Test1 {	\n" +
			"String s = 3;	\n" +
			"Test1() throws IOException {	\n" +
			"}	\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in prs\\Test1.java (at line 4)\n" +
		"	String s = 3;	\n" +
		"	           ^\n" +
		"Type mismatch: cannot convert from int to String\n" +
		"----------\n",
		null,
		true,
		null,
		true,
		false,
		false);
	runConformTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"prs/Test2.java",
			"package prs;	\n" +
			"import java.io.IOException;	\n" +
			"public class Test2 {	\n" +
			"public void foo() {	\n" +
			"try {	\n" +
			"Test1 t = new Test1();	\n" +
			"System.out.println();	\n" +
			"} catch(IOException e)	\n" +
			"{	\n" +
			"e.printStackTrace();	\n" +
			"}	\n" +
			"}	\n" +
			"}"
		},
		// compiler results
		"" /* expected compiler log */,
		// runtime results
		null /* do not check output string */,
		null /* do not check error string */,
		// javac options
		JavacTestOptions.SKIP /* skip javac tests */);
}
// 49843
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public X();\n" +
			"    public Y();\n" +
			"    \n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public X();\n" +
		"	       ^^^\n" +
		"This method requires a body instead of a semicolon\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	public Y();\n" +
		"	       ^^^\n" +
		"Return type for the method is missing\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	public Y();\n" +
		"	       ^^^\n" +
		"This method requires a body instead of a semicolon\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=163443
public void test003() {
	this.runNegativeTest(
		new String[] {
			"Example.java",
			"class Example {\n" +
			"  private Example() {\n" +
			"  }\n" +
			"  public Example(int i) {\n" +
			"  }\n" +
			"}\n" +
			"class E1 {\n" +
			"    private E1(int i) {}\n" +
			"    private E1(long l) {}\n" +
			"}\n" +
			"class E2 {\n" +
			"    private E2(int i) {}\n" +
			"}\n" +
			"class E3 {\n" +
			"    public E3(int i) {}\n" +
			"    Zork z;\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in Example.java (at line 2)\n" +
		"	private Example() {\n" +
		"	        ^^^^^^^^^\n" +
		"The constructor Example() is never used locally\n" +
		"----------\n" +
		"2. ERROR in Example.java (at line 16)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201912, test to make sure that unused public members of
// private class (including constructors, fields, types and methods) get warned about.
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	private class M { \n" + // expect unused field, method, constructor and type warnings
			"       private int state = 0;\n" +
			"       public int unusedMethod() { return this.state; }\n" +
			"       public M (int state) { this.state = state;} \n" +
			"       public int unusedField = 0;\n" +
			"       public class N {}\n" +
			"	}\n" +
			"	private class N { \n" +  // No warnings should come from within here
			"       private int state = 0;\n" +
			"       public int usedMethod() { new O(); return new N(this.state + this.usedField).state; }\n" +
			"       public N (int state) { this.state = state;} \n" +
			"       public int usedField = 0;\n" +
			"       public class O {}\n" +
			"	}\n" +
			"	public class P { \n" + // No warnings should come from within here.
			"       private int state = 0;\n" +
			"       public int unusedMethod() { return this.state; }\n" +
			"       public P (int state) { this.state = state;} \n" +
			"       public int unusedField = 0;\n" +
			"       public class N {}\n" +
			"	}\n" +
			"	public M foo(M m, N n) {\n" +
			"   n.usedMethod(); return m;\n" +
			"	}\n" +
			"} \n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	public int unusedMethod() { return this.state; }\n" +
		"	           ^^^^^^^^^^^^^^\n" +
		"The method unusedMethod() from the type X.M is never used locally\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	public M (int state) { this.state = state;} \n" +
		"	       ^^^^^^^^^^^^^\n" +
		"The constructor X.M(int) is never used locally\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\n" +
		"	public int unusedField = 0;\n" +
		"	           ^^^^^^^^^^^\n" +
		"The value of the field X.M.unusedField is not used\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 7)\n" +
		"	public class N {}\n" +
		"	             ^\n" +
		"The type X.M.N is never used locally\n" +
		"----------\n"
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=264991, wrong unused warning reported. Test to ensure that
// we DON'T complain about the constructor of B not being used (as its removal would result in a compile
// error since its base class does not have a no-arg constructor for the synthesized default constructor
// to invoke.
public void test005() {
	String[]  testFiles = new String[] {
			"A.java",
			"public class A {\n" +
			"	public A(String s) {\n" +
			"		B.test();\n" +
			"	}\n" +
			"\n" +
			"	private static class B extends A {\n" +
			"		public B () { super(\"\"); }\n" +
			"	private static void test() {};\n" +
			"	}\n" +
			"}\n"
			};
	if (!isMinimumCompliant(ClassFileConstants.JDK11)) {
		this.runNegativeTest(testFiles,
				"----------\n" +
				"1. WARNING in A.java (at line 3)\n" +
				"	B.test();\n" +
				"	^^^^^^^^\n" +
				"Access to enclosing method test() from the type A.B is emulated by a synthetic accessor method\n" +
				"----------\n");
	} else {
		this.runConformTest(testFiles);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265142, wrong unused warning reported. Test to ensure that
//we DO complain about the constructor of B not being used when its base class has a no-arg constructor
public void test006() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"----------\n" +
			"1. WARNING in A.java (at line 8)\n" +
			"	public B () { super(\"\"); }\n" +
			"	       ^^^^\n" +
			"The constructor A.B() is never used locally\n" +
			"----------\n"
			:
			"----------\n" +
			"1. WARNING in A.java (at line 3)\n" +
			"	B.test();\n" +
			"	^^^^^^^^\n" +
			"Access to enclosing method test() from the type A.B is emulated by a synthetic accessor method\n" +
			"----------\n" +
			"2. WARNING in A.java (at line 8)\n" +
			"	public B () { super(\"\"); }\n" +
			"	       ^^^^\n" +
			"The constructor A.B() is never used locally\n" +
			"----------\n";
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public class A {\n" +
		    "	public A(String s) {\n" +
		    "		B.test();\n" +
		    "	}\n" +
		    "	public A() {}\n" +
		    "\n" +
		    "	private static class B extends A {\n" +
		    "		public B () { super(\"\"); }\n" +
		    "		private static void test() {};\n" +
		    "   }\n" +
			"}\n"
		},
		errMessage);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265142, wrong unused warning reported. Test to ensure that
//we can compile the program successfully after deleting the unused constructor.
public void test007() {
	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
		    "	public A(String s) {\n" +
		    "		B.test();\n" +
		    "	}\n" +
		    "	public A() {}\n" +
		    "\n" +
		    "	private static class B extends A {\n" +
		    "		private static void test() {};\n" +
		    "	}\n" +
			"}\n"
		});
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265142, wrong unused warning reported. Test to ensure that
//we DON'T complain about unused constructor when the super class's default constructor is not visible.
public void test008() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"	public A(String s) {this();}\n" +
			"	private A() {}\n" +
			"}\n" +
			"class C {\n" +
			"	private static class B extends A {\n" +
			"		public B () { super(\"\"); }\n" +
			"		static void foo() {}\n" +
			"	}\n" +
			"	C() {\n" +
			"		B.foo();\n" +
			"	}\n" +
			"}\n"
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=265142, wrong unused warning reported. Test to ensure that
//we DO complain about unused constructor when the super class's default constructor is visible.
public void test009() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"	public A(String s) {}\n" +
			"	protected A() {}\n" +
			"}\n" +
			"class C {\n" +
			"	private static class B extends A {\n" +
			"		public B () { super(\"\"); }\n" +
			"		static void foo() {}\n" +
			"	}\n" +
			"	C() {\n" +
			"		B.foo();\n" +
			"	}\n" +
			"}\n"
			},
			"----------\n" +
			"1. WARNING in A.java (at line 7)\n" +
			"	public B () { super(\"\"); }\n" +
			"	       ^^^^\n" +
			"The constructor C.B() is never used locally\n" +
			"----------\n");
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	private class Y {\n" +
			"		static final int i = 10;\n" +
			"		public Y() {}\n" +
			"		public Y(int x) {System.out.println(x);}\n" +
			"   }\n" +
			"\n" +
			"	public void zoo() {\n" +
			"		System.out.println(Y.i);\n" +
			"		Y y = new Y(5);\n" +
			"		System.out.println(y);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	public Y() {}\n" +
		"	       ^^^\n" +
		"The constructor X.Y() is never used locally\n" +
		"----------\n",
		null,
		true,
		null
	);
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	private static class Y {\n" +
			"		static final int i = 10;\n" +
			"		public Y() {}\n" +
			"		public Y(int x) {System.out.println(x);}\n" +
			"   }\n" +
			"\n" +
			"	public void zoo() {\n" +
			"		System.out.println(Y.i);\n" +
			"		Y y = new Y(5);\n" +
			"		System.out.println(y);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	public Y() {}\n" +
		"	       ^^^\n" +
		"The constructor X.Y() is never used locally\n" +
		"----------\n",
		null,
		true,
		null
	);
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.Externalizable;\n" +
			"import java.io.IOException;\n" +
			"import java.io.ObjectInput;\n" +
			"import java.io.ObjectOutput;\n" +
			"public class X {\n" +
			"	private static class Y implements Externalizable {\n" +
			"		static final int i = 10;\n" +
			"		public Y() {}\n" +
			"		public Y(int x) {System.out.println(x);}\n" +
			"\n" +
			"		@Override\n" +
			"		public void writeExternal(ObjectOutput out) throws IOException {\n" +
			"		}\n" +
			"\n" +
			"		@Override \n" +
			"		public void readExternal(ObjectInput in) throws IOException,\n" +
			"		ClassNotFoundException {\n" +
			"		}\n" +
			"	}\n" +
			"	public void zoo() {\n" +
			"		System.out.println(Y.i);\n" +
			"		Y y = new Y(5);\n" +
			"		System.out.println(y);\n" +
			"	}\n" +
			"}"
		},
		"",
		null,
		true,
		null
	);
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.Externalizable;\n" +
			"import java.io.IOException;\n" +
			"import java.io.ObjectInput;\n" +
			"import java.io.ObjectOutput;\n" +
			"public class X {\n" +
			"	private class Y implements Externalizable {\n" +
			"		static final int i = 10;\n" +
			"		public Y() {}\n" +
			"		public Y(int x) {System.out.println(x);}\n" +
			"\n" +
			"		@Override\n" +
			"		public void writeExternal(ObjectOutput out) throws IOException {\n" +
			"		}\n" +
			"\n" +
			"		@Override \n" +
			"		public void readExternal(ObjectInput in) throws IOException,\n" +
			"		ClassNotFoundException {\n" +
			"		}\n" +
			"	}\n" +
			"	public void zoo() {\n" +
			"		System.out.println(Y.i);\n" +
			"		Y y = new Y(5);\n" +
			"		System.out.println(y);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 8)\n" +
		"	public Y() {}\n" +
		"	       ^^^\n" +
		"The constructor X.Y() is never used locally\n" +
		"----------\n",
		null,
		true,
		null
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=408038,
//Classes which implement Externalizable should not have an unused constructor warning
//The test case is not directly related to the bug. It was discovered as a result
//of the bug. Please see comment 16 bullet 4 in bugzilla.
public void test408038e() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	int i;\n" +
			"	private X(int x) {i = x;}\n" +
			"	X() {}\n" +
			"	public int foo() {\n" +
			"		X x = new X();\n" +
			"		return x.i;\n" +
			"	}\n" +
			"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	private X(int x) {i = x;}\n" +
			"	        ^^^^^^^^\n" +
			"The constructor X(int) is never used locally\n" +
			"----------\n");
}
}
